const {ShowdownSession}=require('./bridge.cjs');
const {test}=require('node:test');const assert=require('node:assert/strict');
function pokemon(id,species,moves,extras={}){return {id,species,form:0,item:0,ability:0,level:50,gender:0,nature:0,ivs:[31,31,31,31,31,31],evs:[0,0,0,0,0,0],moves,pp:[-1,-1,-1,-1],ppUps:[0,0,0,0],hp:999,status:0,friendship:70,...extras};}
function encounter(player,wild,others=[]){return {player,party:[player,...others],wilds:[wild],seed:[1,2,3,4]};}
test('actual BattleStream turn spends PP and resolves damage, then can forfeit',async()=>{
 const session=new ShowdownSession();try{
  const player=pokemon('own',25,[85,98,0,0]),wild=pokemon('wild',129,[150,0,0,0]);
  const before=await session.start(encounter(player,wild));assert.equal(before.turn,1);
  const after=await session.choose({type:'MOVE',index:0});
  assert.ok(after.pokemon.find(p=>p.id==='wild').hp<before.pokemon.find(p=>p.id==='wild').hp);
  assert.equal(after.pokemon.find(p=>p.id==='own').moves[0].pp,14);
  assert.ok(after.logs.some(l=>l.startsWith('|move|')));
  if(!after.ended){const ended=await session.choose({type:'FORFEIT'});assert.ok(ended.ended);}
 }finally{await session.close();}
});
test('switch uses stable Pokemon identity and opponents take a turn',async()=>{
 const s=new ShowdownSession();try{const own=pokemon('one',25,[98,0,0,0]);await s.start(encounter(own,pokemon('wild',129,[150,0,0,0]),[pokemon('two',4,[52,0,0,0])]));
  const after=await s.choose({type:'SWAP',pokemonId:'two'});assert.equal(after.pokemon.find(p=>p.side===0&&p.active).id,'two');assert.ok(after.turn>=2);
 }finally{await s.close();}
});
test('reject invalid or unavailable move without consuming a turn',async()=>{
 const s=new ShowdownSession();try{const own=pokemon('one',25,[85,0,0,0],{pp:[0,0,0,0]});const before=await s.start(encounter(own,pokemon('wild',129,[150,0,0,0])));
  await assert.rejects(()=>s.choose({type:'MOVE',index:4}));assert.equal(s.stream.battle.turn,before.turn);
 }finally{await s.close();}
});

test('exhausted PP exposes Struggle and engine resolves recoil',async()=>{
 const s=new ShowdownSession();try{const own=pokemon('one',25,[85,0,0,0],{pp:[0,0,0,0]});
  const before=await s.start(encounter(own,pokemon('wild',143,[150,0,0,0])));
  assert.equal(before.pokemon.find(p=>p.id==='one').moves[0].id,165);
  const after=await s.choose({type:'MOVE',index:0});assert.ok(after.logs.some(l=>l.includes('|Struggle|')));
  assert.ok(after.pokemon.find(p=>p.id==='one').hp<before.pokemon.find(p=>p.id==='one').hp);
 }finally{await s.close();}
});

test('engine applies status rules rather than Java approximations',async()=>{
 const s=new ShowdownSession();try{const own=pokemon('one',25,[86,0,0,0]);await s.start(encounter(own,pokemon('wild',129,[150,0,0,0])));
  const state=await s.choose({type:'MOVE',index:0});assert.equal(state.pokemon.find(p=>p.id==='wild').status,'par');
 }finally{await s.close();}
});
test('fainted lead requests replacement while a living reserve remains',async()=>{
 const s=new ShowdownSession();try{const own=pokemon('one',1,[33,0,0,0],{level:1,hp:1});
  await s.start(encounter(own,pokemon('wild',150,[94,0,0,0],{level:100}),[pokemon('reserve',6,[52,0,0,0])]));
  const after=await s.choose({type:'MOVE',index:0});assert.ok(after.forceSwitch);assert.ok(!after.ended);
  const switched=await s.choose({type:'SWAP',pokemonId:'reserve'});assert.equal(switched.pokemon.find(p=>p.side===0&&p.active).id,'reserve');
 }finally{await s.close();}
});

test('test move allows an unlearned move for one turn without replacing saved moves or PP',async()=>{
 const s=new ShowdownSession();try{
  const own=pokemon('one',1,[33,45,73,22]),wild=pokemon('wild',143,[150,0,0,0]);
  const before=await s.start(encounter(own,wild));
  const after=await s.choose({type:'TEST_MOVE',index:57}); // Bulbasaur uses Surf, absent from its moves.
  assert.ok(after.actions.some(a=>a.casterId==='one'&&a.move===57));
  assert.ok(after.pokemon.find(p=>p.id==='wild').hp<before.pokemon.find(p=>p.id==='wild').hp);
  assert.deepEqual(after.pokemon.find(p=>p.id==='one').moves,before.pokemon.find(p=>p.id==='one').moves);
  assert.deepEqual(own.moves,[33,45,73,22]);
  await assert.rejects(()=>s.choose({type:'TEST_MOVE',index:999999}));
  await assert.rejects(()=>s.choose({type:'MOVE',index:4})); // Ordinary move submission still accepts only slots 0..3.
  const normal=await s.choose({type:'MOVE',index:0});assert.ok(normal.actions.some(a=>a.move===33));
 }finally{await s.close();}
});

test('ordered result events retain damage, stat changes and healing',async()=>{
 const s=new ShowdownSession();try{
  await s.start(encounter(pokemon('own',6,[41,14,45,105],{hp:20}),pokemon('wild',143,[150,0,0,0])));
  const hit=await s.choose({type:'MOVE',index:0});
  const damage=hit.events.filter(e=>e.type==='DAMAGE'&&e.targetId==='wild');
  assert.equal(damage.length,2);
  assert.ok(damage.every(e=>e.direct));
  assert.ok(damage[0].hp>damage[1].hp);
  assert.equal(damage[1].hp,hit.pokemon.find(p=>p.id==='wild').hp);
  assert.ok(hit.events.findIndex(e=>e.type==='MOVE'&&e.casterId==='own')<hit.events.indexOf(damage[0]));
  const up=await s.choose({type:'MOVE',index:1});
  assert.ok(up.events.some(e=>e.type==='BOOST'&&e.targetId==='own'&&e.stat==='atk'&&e.amount===2));
  const down=await s.choose({type:'MOVE',index:2});
  assert.ok(down.events.some(e=>e.type==='UNBOOST'&&e.targetId==='wild'&&e.stat==='atk'&&e.amount===1));
  const heal=await s.choose({type:'MOVE',index:3});
  assert.ok(heal.events.some(e=>e.type==='HEAL'&&e.targetId==='own'&&e.hp>20));
 }finally{await s.close();}
});

test('immune and failed moves expose native text results and suppress their animations',async()=>{
 for(const [move,species,outcome] of [[85,50,'IMMUNE'],[138,129,'IMMUNE'],[105,129,'FAIL'],[389,129,'FAIL']]){
  const s=new ShowdownSession();try{
   await s.start(encounter(pokemon('own',6,[move,0,0,0]),pokemon('wild',species,[150,0,0,0])));
   const result=await s.choose({type:'MOVE',index:0});
   const action=result.events.find(e=>e.type==='MOVE'&&e.casterId==='own');
   assert.equal(action.animationSuppressed,true,JSON.stringify(result.logs));
   assert.ok(result.events.some(e=>e.type===outcome),JSON.stringify({move,logs:result.logs}));
   assert.ok(!result.events.some(e=>e.type==='DAMAGE'||e.type==='HEAL'));
  }finally{await s.close();}
 }
});

test('Protect rejection suppresses the blocked attack rather than the protection move',async()=>{
 const s=new ShowdownSession();try{
  await s.start(encounter(pokemon('own',25,[33,0,0,0]),pokemon('wild',129,[182,0,0,0])));
  const result=await s.choose({type:'MOVE',index:0});
  assert.ok(result.events.some(e=>e.type==='BLOCK'&&e.targetId==='wild'),JSON.stringify(result.logs));
  assert.equal(result.events.find(e=>e.type==='MOVE'&&e.casterId==='own').animationSuppressed,true);
  assert.equal(result.events.find(e=>e.type==='MOVE'&&e.casterId==='wild').animationSuppressed,false);
 }finally{await s.close();}
});

test('Disable exposes its blocked move, start and end messages without consuming rejected choices',async()=>{
 const s=new ShowdownSession();try{
  await s.start(encounter(pokemon('own',135,[45,150,0,0]),pokemon('wild',143,[50,0,0,0])));
  const state=await s.choose({type:'MOVE',index:0});
  assert.ok(state.events.some(e=>e.type==='DISABLE_START'&&e.targetId==='own'&&e.move===45),JSON.stringify(state.logs));
  const disabled=state.pokemon.find(p=>p.id==='own').moves.find(m=>m.id===45);
  assert.equal(disabled.disabled,true);assert.match(disabled.disabledSource,/Disable/i);
  await assert.rejects(()=>s.choose({type:'MOVE',index:0}));
  assert.equal(s.stream.battle.turn,state.turn);
  let ended=false;
  for(let i=0;i<8&&!ended;i++){
   const next=await s.choose({type:'MOVE',index:1});assert.ok(next.turn>state.turn);
   ended=next.events.some(e=>e.type==='DISABLE_END'&&e.targetId==='own');
  }
  assert.ok(ended);

 }finally{await s.close();}
});

test('temporary test moves retain Disable on an existing saved move',async()=>{
 const s=new ShowdownSession();try{
  await s.start(encounter(pokemon('own',4,[33,45,0,0],{level:10,hp:20}),pokemon('wild',143,[50,0,0,0],{level:1})));
  const state=await s.choose({type:'TEST_MOVE',index:45});
  const disabled=state.pokemon.find(p=>p.id==='own').moves.find(m=>m.id===45);
  assert.equal(disabled.disabled,true,JSON.stringify(state.logs));
 }finally{await s.close();}
});

test('arbitrary species abilities execute engine mechanics rather than species defaults',async()=>{
 const s=new ShowdownSession();try{
  const before=await s.start(encounter(pokemon('own',4,[150,0,0,0],{ability:26}),pokemon('wild',143,[89,0,0,0])));
  assert.equal(s.stream.battle.sides[0].active[0].ability,'levitate');
  const result=await s.choose({type:'MOVE',index:0});
  assert.equal(result.pokemon.find(p=>p.id==='own').hp,before.pokemon.find(p=>p.id==='own').hp);
  assert.ok(result.events.some(e=>e.type==='IMMUNE'&&e.targetId==='own'));
 }finally{await s.close();}
});
