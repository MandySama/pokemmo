/* Adapter to the official BattleStream API; all battle rules stay in Showdown. */
'use strict';
const {BattleStream, Dex, Teams} = require('pokemon-showdown');
const readline = require('node:readline');
const natureIds=['hardy','lonely','brave','adamant','naughty','bold','docile','relaxed','impish','lax','timid','hasty','serious','jolly','naive','modest','mild','quiet','bashful','rash','calm','gentle','sassy','careful','quirky'];
const statKeys = ['hp','atk','def','spa','spd','spe'];
const nextTick = () => new Promise(resolve => setImmediate(resolve));
const statusIds = new Map([[8,'psn'],[16,'brn'],[32,'frz'],[64,'par']]);
function statusName(value) { return value < 0 ? 'tox' : value & 7 ? 'slp' : statusIds.get(value) || ''; }
function lookup(data,num,label) {
  const result = data.all().find(entry => entry.num === num && entry.exists);
  if (!result) throw new Error(`Unsupported ${label} ID ${num}`);
  return result;
}
function toSet(p) {
  // Form and item namespaces must be reconciled against PokeMMO before enabling them.
  if (p.form !== 0) throw new Error(`Showdown pilot does not yet map Pokemon form ${p.species}:${p.form}`);
  if (p.item !== 0) throw new Error('Showdown pilot requires no held item (PokeMMO item mapping is not yet verified)');
  const species = Dex.species.all().find(s => s.num === p.species && !s.forme && s.exists);
  if (!species) throw new Error(`Unsupported species ${p.species}`);
  const moves = p.moves.filter(id => id > 0).map(id => lookup(Dex.moves,id,'move').name);
  if (!moves.length) throw new Error('Pokemon must have at least one move');
  return {name:p.name || species.name,species:species.name,level:p.level,
    gender:p.gender === 0 ? 'M' : p.gender === 1 ? 'F' : '',shiny:p.shiny||p.secretShiny,
    ability:p.ability > 0 ? lookup(Dex.abilities,p.ability,'ability').name : species.abilities['0'],
    nature:Dex.natures.get(natureIds[p.nature]).name,
    ivs:Object.fromEntries(statKeys.map((k,i)=>[k,p.ivs[i]])),
    evs:Object.fromEntries(statKeys.map((k,i)=>[k,p.evs[i]])),
    happiness:p.friendship,moves};
}
class ShowdownSession {
  constructor() { this.stream=null;this.output=[];this.inputTeams=[]; }
  async write(text) { await this.stream.write(text);await nextTick(); }
  async start(input) {
    if (this.stream) throw new Error('Battle already started');
    if(input.wilds.length !== 1) throw new Error('Showdown pilot supports singles only');
    const party=input.party.filter(p=>!p.egg);
    const lead=party.find(p=>p.id===input.player.id);
    if(!lead || lead.hp<=0) throw new Error('No living lead Pokemon');
    this.inputTeams=[[lead,...party.filter(p=>p.id!==lead.id)],input.wilds];
    const sets=this.inputTeams.map(team=>team.map((p,i)=>({...toSet(p),name:`slot${i}`})));
    this.stream=new BattleStream({keepAlive:true,noCatch:true});
    this.reader=(async()=>{for await(const block of this.stream)this.output.push(block);})();
    await this.write('>start '+JSON.stringify({formatid:'gen5customgame@@@!Team Preview',seed:input.seed}));
    // Player data is encoded with Teams.pack, never inserted as user-controlled stream commands.
    await this.write('>player p1 '+JSON.stringify({name:'Player',team:Teams.pack(sets[0])}));
    await this.write('>player p2 '+JSON.stringify({name:'Wild',team:Teams.pack(sets[1])}));
    for(let side=0;side<2;side++){
      const s=this.stream.battle.sides[side];
      for(let i=0;i<s.pokemon.length;i++){
        const pokemon=s.pokemon[i], original=this.inputTeams[side][i];
        pokemon.adapterId=original.id;
        pokemon.hp=Math.min(original.hp,pokemon.maxhp);pokemon.fainted=pokemon.hp===0;
        const condition=statusName(original.status);
        if(condition) pokemon.setStatus(condition,pokemon,undefined,true);
        for(let slot=0;slot<pokemon.moveSlots.length;slot++){
          const move=pokemon.moveSlots[slot];
          const originalSlot=original.moves.indexOf(Dex.moves.get(move.id).num);
          const base=Dex.moves.get(move.id).pp;
          move.maxpp=Math.min(64,Math.floor(base*(5+original.ppUps[originalSlot])/5));
          move.pp=original.pp[originalSlot]<0 ? move.maxpp : Math.min(move.maxpp,original.pp[originalSlot]);
        }
      }
      s.pokemonLeft=s.pokemon.filter(p=>!p.fainted).length;
    }
    this.stream.battle.makeRequest('move');await nextTick();
    return this.result();
  }
  async choose(action) {
    if(!this.stream || this.stream.battle.ended)throw new Error('No active battle');
    const side=this.stream.battle.sides[0];
    if(action.type==='TEST_MOVE')return this.testMove(action.index);
    if(action.type==='MOVE'){
      if(!Number.isInteger(action.index)||action.index<0||action.index>3)throw new Error('Invalid move slot');
      const struggle=side.activeRequest?.active?.[0]?.moves?.[0]?.id==='struggle';
      const original=this.inputTeams[0].find(p=>p.id===side.active[0].adapterId);
      const id=original.moves[action.index];
      const slot=side.active[0].moveSlots.findIndex(m=>Dex.moves.get(m.id).num===id);
      if(slot<0&&!struggle)throw new Error('Empty move slot');
      await this.write('>p1 move '+(struggle?1:slot+1));
    }else if(action.type==='SWAP'){
      const slot=side.pokemon.findIndex(p=>p.adapterId===action.pokemonId);
      if(slot<0)throw new Error('Unknown party member');
      await this.write('>p1 switch '+(slot+1));
    }else if(action.type==='FORFEIT'){
      await this.write('>forcelose p1');
    }else throw new Error('Unsupported battle action');
    const errors=this.output.flatMap(x=>x.split('\n')).filter(x=>x.startsWith('|error|'));
    if(errors.length){this.output=[];throw new Error(errors.join('\n'));}
    // Test opponent: Showdown's default legal choice, not a new battle-rules implementation.
    for(let guard=0;guard<8&&!this.stream.battle.ended;guard++){
      const opponent=this.stream.battle.sides[1];
      if(!opponent.activeRequest||opponent.activeRequest.wait||opponent.isChoiceDone())break;
      await this.write('>p2 default');
    }
    return this.result();
  }
  // Restore availability through Showdown's own DisableMove events (Battle.nextTurn),
  // including move-local restrictions, after swapping the test-only slot array.
  refreshMoveAvailability(pokemon){
    const battle=this.stream.battle;
    pokemon.maybeDisabled=false;
    for(const slot of pokemon.moveSlots){slot.disabled=false;slot.disabledSource='';}
    battle.runEvent('DisableMove',pokemon);
    for(const slot of pokemon.moveSlots){
      const move=battle.dex.getActiveMove(slot.id);
      battle.singleEvent('DisableMove',move,null,pokemon);
      if(move.flags.cantusetwice&&pokemon.lastMove?.id===slot.id)pokemon.disableMove(slot.id);
    }
  }
  // A temporary extra move slot uses normal Showdown choices/turn resolution, not direct runMove.
  async testMove(id){
    const battle=this.stream.battle,side=battle.sides[0],pokemon=side.active[0];
    if(side.requestState!=='move'||!pokemon||pokemon.fainted)throw new Error('A living active Pokemon must be choosing a move');
    if(!Number.isInteger(id)||id<=0)throw new Error('Invalid test move ID');
    const move=lookup(Dex.moves,id,'test move');
    const originalSlots=pokemon.moveSlots;
    const existing=originalSlots.find(slot=>slot.id===move.id);
    const temporary={move:move.name,id:move.id,pp:move.pp,maxpp:move.pp,target:move.target,
      disabled:!!existing?.disabled,disabledSource:existing?.disabledSource||'',used:false};
    // Remove a duplicate ID only in the transient slot array; stored moves and PP stay unchanged.
    pokemon.moveSlots=[...originalSlots.filter(slot=>slot.id!==move.id),temporary];
    this.refreshMoveAvailability(pokemon);
    this.stream.battle.makeRequest('move');await nextTick();
    try{
      await this.write('>p1 move '+pokemon.moveSlots.length);
      const errors=this.output.flatMap(x=>x.split('\n')).filter(x=>x.startsWith('|error|'));
      if(errors.length){this.output=[];throw new Error(errors.join('\n'));}
      for(let guard=0;guard<8&&!battle.ended;guard++){
        const opponent=battle.sides[1];
        if(!opponent.activeRequest||opponent.activeRequest.wait||opponent.isChoiceDone())break;
        await this.write('>p2 default');
      }
    }finally{
      pokemon.moveSlots=originalSlots;
      if(!battle.ended&&battle.sides[0].requestState==='move'){
        this.refreshMoveAvailability(pokemon);
        battle.makeRequest('move');
      }
      await nextTick();
    }
    return this.result();
  }
  result(){
    const battle=this.stream.battle;
    const pokemon=battle.sides.flatMap((side,sideIndex)=>side.pokemon.map(p=>({
      id:p.adapterId,side:sideIndex,active:side.active.includes(p),hp:p.hp,maxHp:p.maxhp,
      status:p.status,stats:statKeys.map(k=>k==='hp'?p.maxhp:p.storedStats[k]),
      moves:side.active.includes(p)&&side.activeRequest?.active?.[0]?.moves?.[0]?.id==='struggle'
        ? [{id:165,pp:1,maxPp:1,disabled:false,disabledSource:''}]
        : p.moveSlots.map(m=>({id:Dex.moves.get(m.id).num,pp:m.pp,maxPp:m.maxpp,
            disabled:!!m.disabled||!!(side.active.includes(p)&&side.activeRequest?.active?.[0]?.moves?.find(request=>request.id===m.id)?.disabled),
            disabledSource:m.disabledSource||''})),
    })));
    const logs=[];
    for(const block of this.output.splice(0)){
      if(!block.startsWith('update\n'))continue;
      const lines=block.split('\n').slice(1);
      for(let i=0;i<lines.length;i++){
        if(lines[i].startsWith('|split|')){if(lines[i+1])logs.push(lines[i+1]);i+=2;}
        else if(lines[i])logs.push(lines[i]);
      }
    }
    const identify=ident=>{
      const side=ident?.startsWith('p1')?0:ident?.startsWith('p2')?1:-1;
      if(side<0)return '';
      return battle.sides[side].pokemon.find(p=>p.name===ident.split(': ').slice(1).join(': '))?.adapterId||'';
    };
    const events=[];
    for(const line of logs){
      const f=line.split('|'),kind=f[1],targetId=identify(f[2]);
      if(kind==='move')events.push({type:'MOVE',casterId:targetId,targetId:identify(f[4])||targetId,move:Dex.moves.get(f[3]).num});
      else if(['-damage','-heal','-sethp'].includes(kind)){
        const addHp=(ident,condition)=>{
          const id=identify(ident),p=pokemon.find(p=>p.id===id);if(!p)return;
          const health=condition.split(' ')[0].split('/');
          events.push({type:kind==='-heal'?'HEAL':kind==='-sethp'?'SET_HP':'DAMAGE',targetId:id,
            hp:Number(health[0]),maxHp:health.length>1?Number(health[1]):p.maxHp,
            direct:kind==='-damage'&&!f.slice(4).some(v=>v.startsWith('[from]'))});
        };
        addHp(f[2],f[3]);if(kind==='-sethp'&&f[4]&&f[5])addHp(f[4],f[5]);
      }else if(['-boost','-unboost','-setboost'].includes(kind))events.push({
        type:kind==='-boost'?'BOOST':kind==='-unboost'?'UNBOOST':'SET_BOOST',targetId,stat:f[3],amount:Number(f[4])});
      else if(kind==='-status'||kind==='-curestatus')events.push({type:kind==='-status'?'STATUS':'CURE_STATUS',targetId,status:f[3]});
      else if(kind==='-activate'&&['Protect','Detect','Wide Guard','Quick Guard'].includes((f[3]||'').replace(/^move: /,'')))
        events.push({type:'BLOCK',targetId});
      else if(['-fail','-immune','-miss','-block'].includes(kind))events.push({
        type:kind==='-immune'?'IMMUNE':kind==='-miss'?'MISS':kind==='-block'?'BLOCK':'FAIL',
        targetId:kind==='-miss'?(identify(f[3])||targetId):targetId});
      else if((kind==='-start'||kind==='-end'||kind==='cant')&&(f[3]||'').replace(/^move: /,'')==='Disable')
        events.push({type:kind==='-start'?'DISABLE_START':kind==='-end'?'DISABLE_END':'MOVE_DISABLED',targetId,move:Dex.moves.get(f[4]||'').num||0});
      else if(kind==='faint')events.push({type:'FAINT',targetId});
    }
    // Failure flags suppress the release animation, but one immune target must not
    // suppress a spread move which actually affected another target.
    for(let i=0;i<events.length;i++)if(events[i].type==='MOVE'){
      let end=i+1;while(end<events.length&&events[end].type!=='MOVE')end++;
      const results=events.slice(i+1,end);
      const rejected=results.some(e=>['FAIL','IMMUNE','MISS','BLOCK'].includes(e.type));
      const immune=results.some(e=>e.type==='IMMUNE'||e.type==='BLOCK');
      const affected=results.some(e=>e.type==='DAMAGE'&&e.direct
        ||!immune&&['HEAL','SET_HP','BOOST','UNBOOST','SET_BOOST','STATUS','CURE_STATUS'].includes(e.type));
      events[i].animationSuppressed=rejected&&!affected;
    }
    const actions=events.filter(e=>e.type==='MOVE').map(e=>({casterId:e.casterId,targetId:e.targetId,move:e.move}));
    return {events,actions,engine:'SHOWDOWN',turn:battle.turn,ended:battle.ended,winner:battle.winner||'',
      forceSwitch:!!battle.sides[0].activeRequest?.forceSwitch?.some(Boolean),pokemon,logs};
  }
  async close(){if(this.stream){this.stream.destroy();await this.reader;this.stream=null;}}
}
module.exports={ShowdownSession,toSet};
if(require.main===module){
  const engine=new ShowdownSession();
  (async()=>{
    for await(const line of readline.createInterface({input:process.stdin,crlfDelay:Infinity})){
      try{
        const request=JSON.parse(line);
        if(request.op==='close'){await engine.close();process.stdout.write(JSON.stringify({ok:true})+'\n');break;}
        const result=request.op==='start'?await engine.start(request.encounter):request.op==='choose'?await engine.choose(request.action):(()=>{throw new Error('Unknown operation')})();
        process.stdout.write(JSON.stringify({ok:true,result})+'\n');
      }catch(error){process.stdout.write(JSON.stringify({ok:false,error:String(error.message)})+'\n');}
    }
    await engine.close();
  })().catch(error=>{console.error(error);process.exitCode=1;});
}
