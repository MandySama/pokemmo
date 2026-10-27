/* Adapter to the official BattleStream API; all battle rules stay in Showdown. */
'use strict';
const {BattleStream, Dex, Teams} = require('pokemon-showdown');
const readline = require('node:readline');
const {HordeBattleStream} = require('./horde.cjs');
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
  if (p.item > 0) throw new Error('Showdown pilot requires no held item (PokeMMO item mapping is not yet verified)');
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
  constructor() { this.stream=null;this.output=[];this.inputTeams=[];this.delayedMoves=new Map();this.bideActors=new Set(); }
  async write(text) { await this.stream.write(text);await nextTick(); }
  async start(input) {
    if (this.stream) throw new Error('Battle already started');
    if(!(input.trainer ? input.wilds.length>=1&&input.wilds.length<=6 : [1,3,5].includes(input.wilds.length))) throw new Error('Showdown supports 1, 3 or 5 wild Pokemon');
    const party=input.party.filter(p=>!p.egg);
    const lead=party.find(p=>p.id===input.player.id);
    if(!lead || lead.hp<=0) throw new Error('No living lead Pokemon');
    this.inputTeams=[[lead,...party.filter(p=>p.id!==lead.id)],input.wilds];
    const sets=this.inputTeams.map(team=>team.map((p,i)=>({...toSet(p),name:`slot${i}`})));
    this.stream=input.trainer || input.wilds.length===1 ? new BattleStream({keepAlive:true,noCatch:true})
      : new HordeBattleStream(input.wilds.length,{keepAlive:true,noCatch:true});
    this.reader=(async()=>{for await(const block of this.stream)this.output.push(block);})();
    await this.write('>start '+JSON.stringify({formatid:'gen5customgame@@@!Team Preview',seed:input.seed}));
    // Player data is encoded with Teams.pack, never inserted as user-controlled stream commands.
    await this.write('>player p1 '+JSON.stringify({name:'Player',team:Teams.pack(sets[0])}));
    await this.write('>player p2 '+JSON.stringify({name:input.trainer?'Trainer':'Wild',team:Teams.pack(sets[1])}));
    for(let side=0;side<2;side++){
      const s=this.stream.battle.sides[side];
      for(let i=0;i<s.pokemon.length;i++){
        const pokemon=s.pokemon[i], original=this.inputTeams[side][i];
        pokemon.adapterId=original.id;
        // Explicit development fixture only; ordinary encounter stats remain simulator-derived.
        if(side===1 && input.testEnemyHp===999999){pokemon.maxhp=999999;pokemon.baseMaxhp=999999;}
        pokemon.hp=side===1 && input.testEnemyHp===999999 ? 999999 : Math.min(original.hp,pokemon.maxhp);pokemon.fainted=pokemon.hp===0;
        const condition=statusName(original.status);
        if(condition) pokemon.setStatus(condition,pokemon,undefined,true);
        for(let slot=0;slot<pokemon.moveSlots.length;slot++){
          const move=pokemon.moveSlots[slot];
          const originalSlot=original.moves.indexOf(Dex.moves.get(move.id).num);
          // Imposter may already have transformed during the native switch-in.
          // Its copied slots have native temporary PP, not the saved set's PP Ups.
          if(pokemon.transformed||originalSlot<0)continue;
          const base=Dex.moves.get(move.id).pp;
          move.maxpp=Math.min(64,Math.floor(base*(5+original.ppUps[originalSlot])/5));
          move.pp=original.pp[originalSlot]<0 ? move.maxpp : Math.min(move.maxpp,original.pp[originalSlot]);
        }
      }
      s.pokemonLeft=s.pokemon.filter(p=>!p.fainted).length;
    }
    if(input.testEnemyHp===999999){
      // Test fixture: cancel chosen opponent actions using the simulator's own queue API.
      // Status, weather, abilities and residual events still resolve normally.
      this.stream.battle.onEvent('BeforeTurn',this.stream.battle.format,function(){
        for(const pokemon of this.sides[1].active)if(pokemon)this.queue.cancelAction(pokemon);
      });
    }
    this.stream.battle.makeRequest('move');await nextTick();
    return this.result();
  }
  async choose(action) {
    if(!this.stream || this.stream.battle.ended)throw new Error('No active battle');
    const side=this.stream.battle.sides[0];
    if(action.type==='TEST_MOVE')return this.testMove(action.index,action.targetId);
    if(action.type==='MOVE'){
      if(!Number.isInteger(action.index)||action.index<0||action.index>3)throw new Error('Invalid move slot');
      const struggle=side.activeRequest?.active?.[0]?.moves?.[0]?.id==='struggle';
      // Transform, Imposter and Mimic replace battle-local slots. Stored party moves
      // are not the authoritative choice list after such an effect.
      const pokemon=side.active[0],locked=!!pokemon.getLockedMove();
      const selected=pokemon.moveSlots[action.index];
      if(!selected&&!struggle&&!locked)throw new Error('Empty move slot');
      const id=struggle?165:selected?Dex.moves.get(selected.id).num:0;
      await this.write('>p1 move '+(struggle||locked?1:action.index+1)
        +(locked?'':this.targetChoice(id,action.targetId)));
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
  targetChoice(moveId,targetId){
    if(!targetId)return '';
    const battle=this.stream.battle,source=battle.sides[0].active[0];
    const target=battle.getAllActive().find(p=>p.adapterId===targetId);
    if(!target||target.fainted||!target.hp)throw new Error('Target is not a living active Pokemon');
    const move=lookup(Dex.moves,moveId,'move');
    if(!battle.actions.targetTypeChoices(move.target))throw new Error('This move does not accept a selected target');
    const loc=source.getLocOf(target);
    if(!battle.validTargetLoc(loc,source,move.target))throw new Error('Illegal move target');
    return ' '+loc;
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
  async testMove(id,targetId){
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
      // Two-turn/recharge requests expose a single forced move, not the temporary slot array.
      // Choose from the engine request so a fifth transient slot never leaks into a locked turn.
      const requestMoves=side.activeRequest?.active?.[0]?.moves||[];
      const locked=!!pokemon.getLockedMove();
      const requestedIndex=locked?0:requestMoves.findIndex(entry=>entry.id===move.id);
      if(requestedIndex<0)throw new Error('Test move is not available in the current engine request');
      await this.write('>p1 move '+(requestedIndex+1)+(locked?'':this.targetChoice(id,targetId)));
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
      status:p.status,boosts:{...p.boosts},stats:statKeys.map(k=>k==='hp'?p.maxhp:p.storedStats[k]),
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
      if(!ident.includes(': '))return battle.sides[side].active[ident.charCodeAt(2)-97]?.adapterId||'';
      return battle.sides[side].pokemon.find(p=>p.name===ident.split(': ').slice(1).join(': '))?.adapterId||'';
    };
    const events=[],actionLocations=new Map(),outcomeBoundaries=new Set();
    for(let logIndex=0;logIndex<logs.length;logIndex++){
      const line=logs[logIndex];
      const f=line.split('|'),kind=f[1],targetId=identify(f[2]);
      if(line==='|'||kind==='upkeep'||kind==='turn')outcomeBoundaries.add(events.length);
      if(kind==='move'){
        const target=identify(f[4])||targetId;
        const spread=f.find(value=>value.startsWith('[spread] '));
        const targetIds=spread?spread.slice(9).split(',').map(identify).filter(Boolean):[target];
        const event={type:'MOVE',casterId:targetId,targetId:target,targetIds,move:Dex.moves.get(f[3]).num};
        events.push(event);actionLocations.set(event,(f[4]||'').slice(0,3));
      }
      else if(['-damage','-heal','-sethp'].includes(kind)){
        const addHp=(ident,condition)=>{
          const id=identify(ident),p=pokemon.find(p=>p.id===id);if(!p)return;
          const health=condition.split(' ')[0].split('/');
          const trappingMove=f.includes('[partiallytrapped]')
            ?Dex.moves.get((f.find(value=>value.startsWith('[from] move: '))||'').slice(13)).num||0:0;
          events.push({type:kind==='-heal'?'HEAL':kind==='-sethp'?'SET_HP':'DAMAGE',targetId:id,
            move:trappingMove,
            hp:Number(health[0]),maxHp:health.length>1?Number(health[1]):p.maxHp,
            direct:kind==='-damage'&&!f.slice(4).some(v=>v.startsWith('[from]'))});
        };
        addHp(f[2],f[3]);if(kind==='-sethp'&&f[4]&&f[5])addHp(f[4],f[5]);
      }else if(kind==='-start'&&['Future Sight','Doom Desire','Bide'].includes((f[3]||'').replace(/^move: /,''))){
        const move=Dex.moves.get((f[3]||'').replace(/^move: /,'')).num;
        const action=events.findLast(e=>e.type==='MOVE'&&e.casterId===targetId&&e.move===move);
        if(action){
          // Reference FutureSightAnimation/BideAnimation have separate preparation
          // and release recipes, including releases without a native |move| line.
          action.type='CHARGE';
          if(move===117)this.bideActors.add(targetId);
          else this.delayedMoves.set(actionLocations.get(action)+':'+move,{casterId:targetId,move});
        }
      }else if(kind==='-activate'&&(f[3]||'').replace(/^move: /,'')==='Bide'&&this.bideActors.has(targetId)){
        events.push({type:'CHARGE',casterId:targetId,targetId,targetIds:[targetId],move:117});
      }else if(kind==='-end'&&['Future Sight','Doom Desire','Bide'].includes((f[3]||'').replace(/^move: /,''))){
        const move=Dex.moves.get((f[3]||'').replace(/^move: /,'')).num;
        if(move===117){
          if(this.bideActors.has(targetId)&&!f.includes('[silent]')){
            let victim='',failed=false;
            for(let next=logIndex+1;next<logs.length;next++){
              const part=logs[next].split('|');if(part[1]==='move'||part[1]==='turn')break;
              if(part[1]==='-fail')failed=true;
              if(part[1]==='-damage'){victim=identify(part[2]);break;}
            }
            if(victim||failed)events.push({type:'MOVE',casterId:targetId,targetId:victim||targetId,targetIds:[victim||targetId],move});
          }
          this.bideActors.delete(targetId);
        }else{
          const key=(f[2]||'').slice(0,3)+':'+move,stored=this.delayedMoves.get(key);
          if(stored){events.push({type:'MOVE',casterId:stored.casterId,targetId,targetIds:[targetId],move});this.delayedMoves.delete(key);}
        }
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
      else if(kind==='-crit')events.push({type:'CRIT',targetId});
      else if(kind==='-prepare'){
        const action=events.findLast(event=>event.type==='MOVE'&&event.casterId===targetId);
        if(action)action.type='CHARGE';
      }else if(kind==='-anim'){
        // Power Herb and native ChargeMove exceptions release within this turn.
        const action=events.findLast(event=>event.type==='CHARGE'&&event.casterId===targetId);
        if(action){
          action.type='MOVE';
          const releaseTarget=identify(f[4]);
          if(releaseTarget){action.targetId=releaseTarget;action.targetIds=[releaseTarget];}
        }
      }
      else if(kind==='faint')events.push({type:'FAINT',targetId});
    }
    // Failure flags suppress the release animation, but one immune target must not
    // suppress a spread move which actually affected another target.
    for(let i=0;i<events.length;i++)if(['MOVE','CHARGE'].includes(events[i].type)){
      let end=i+1;while(end<events.length&&!outcomeBoundaries.has(end)&&!['MOVE','CHARGE'].includes(events[end].type))end++;
      const results=events.slice(i+1,end);
      const rejected=results.some(e=>['FAIL','IMMUNE','MISS','BLOCK'].includes(e.type));
      const blockedTargets=new Set(results.filter(e=>e.type==='IMMUNE'||e.type==='BLOCK').map(e=>e.targetId));
      const affected=results.some(e=>e.type==='DAMAGE'&&e.direct
        ||!blockedTargets.has(e.targetId)&&['HEAL','SET_HP','BOOST','UNBOOST','SET_BOOST','STATUS','CURE_STATUS'].includes(e.type));
      events[i].animationSuppressed=rejected&&!affected;
    }
    const actions=events.filter(e=>['MOVE','CHARGE'].includes(e.type)).map(e=>({casterId:e.casterId,targetId:e.targetId,move:e.move}));
    return {events,actions,engine:'SHOWDOWN',turn:battle.turn,ended:battle.ended,winner:battle.winner||'',
      forceSwitch:!!battle.sides[0].activeRequest?.forceSwitch?.some(Boolean),pokemon,logs};
  }
  growth(party){
    for(const saved of party){
      const p=this.stream.battle.sides[0].pokemon.find(p=>p.adapterId===saved.id);
      if(!p)continue;
      const original=this.inputTeams[0].find(value=>value.id===saved.id);
      if(original&&!p.transformed){
        for(let i=0;i<saved.moves.length;i++)if(original.moves[i]===0&&saved.moves[i]>0){
          const move=lookup(Dex.moves,saved.moves[i],'move');
          if(!p.baseMoveSlots.some(slot=>slot.id===move.id)){
            const maxpp=Math.min(64,Math.floor(move.pp*(5+saved.ppUps[i])/5));
            const slot={move:move.name,id:move.id,pp:maxpp,maxpp,target:move.target,disabled:false,disabledSource:'',used:false};
            p.baseMoveSlots.push({...slot});
            if(p.moveSlots!==p.baseMoveSlots)p.moveSlots.push({...slot});
            p.moves.push(move.id);p.set.moves.push(move.name);
          }
        }
      }
      if(original){original.moves=[...saved.moves];original.pp=[...saved.pp];original.ppUps=[...saved.ppUps];}
      p.level=saved.level;p.set.level=saved.level;
      p.set.evs=Object.fromEntries(statKeys.map((key,i)=>[key,saved.evs[i]]));
      p.baseStoredStats=Object.fromEntries(statKeys.map((key,i)=>[key,saved.stats[i]]));
      if(!p.transformed)for(const key of statKeys.slice(1))p.storedStats[key]=p.baseStoredStats[key];
      p.baseMaxhp=saved.stats[0];p.maxhp=saved.stats[0];p.hp=Math.min(p.hp,p.maxhp);
    }
    return this.result();
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
        const result=request.op==='start'?await engine.start({...request.encounter,testEnemyHp:request.testEnemyHp}):request.op==='choose'?await engine.choose(request.action):request.op==='growth'?engine.growth(request.pokemon):(()=>{throw new Error('Unknown operation')})();
        process.stdout.write(JSON.stringify({ok:true,result})+'\n');
      }catch(error){process.stdout.write(JSON.stringify({ok:false,error:String(error.message)})+'\n');}
    }
    await engine.close();
  })().catch(error=>{console.error(error);process.exitCode=1;});
}
