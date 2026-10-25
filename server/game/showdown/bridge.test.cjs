const {ShowdownSession}=require('./bridge.cjs');
const {test}=require('node:test');const assert=require('node:assert/strict');
function pokemon(id,species,moves,extras={}){return {id,species,form:0,item:0,ability:0,level:50,gender:0,nature:0,ivs:[31,31,31,31,31,31],evs:[0,0,0,0,0,0],moves,pp:[-1,-1,-1,-1],ppUps:[0,0,0,0],hp:999,status:0,friendship:70,...extras};}
function encounter(player,wild,others=[]){return {player,party:[player,...others],wilds:[wild],seed:[1,2,3,4]};}
for(const move of [20,35,83,128,250,328,463])test(`trapping residual damage retains source move ${move} on later turns`,async()=>{
 const session=new ShowdownSession();try{
  await session.start({...encounter(pokemon('own',95,[150,0,0,0]),pokemon('wild',129,[150,0,0,0])),testEnemyHp:999999});
  session.stream.battle.sides[0].active[0].boosts.accuracy=6;
  const first=await session.choose({type:'TEST_MOVE',index:move});
  assert.ok(first.events.some(event=>event.type==='DAMAGE'&&!event.direct&&event.move===move),first.logs.join('\n'));
  const later=await session.choose({type:'TEST_MOVE',index:150});
  assert.ok(later.events.some(event=>event.type==='DAMAGE'&&!event.direct&&event.move===move),later.logs.join('\n'));
  assert.ok(!later.events.some(event=>event.type==='MOVE'&&event.move===move));
  let expired;
  for(let turn=0;turn<6;turn++)expired=await session.choose({type:'TEST_MOVE',index:150});
  assert.ok(!expired.events.some(event=>event.type==='DAMAGE'&&event.move===move));
 }finally{await session.close();}
});
for(const testing of [false,true])test(`Razor Wind charges without damage then releases on the next turn (test=${testing})`,async()=>{
 const session=new ShowdownSession();try{
  await session.start({...encounter(pokemon('own',25,testing?[85,98,86,182]:[13,0,0,0]),pokemon('wild',129,[150,0,0,0])),testEnemyHp:999999});
  const choice=testing?{type:'TEST_MOVE',index:13}:{type:'MOVE',index:0};
  const charge=await session.choose(choice);
  assert.ok(charge.events.some(event=>event.type==='CHARGE'&&event.move===13));
  assert.equal(charge.pokemon.find(p=>p.id==='wild').hp,999999);
  const release=await session.choose(choice);
  assert.ok(release.events.some(event=>event.type==='MOVE'&&event.move===13));
  assert.ok(release.pokemon.find(p=>p.id==='wild').hp<999999);
 }finally{await session.close();}
});
test('guaranteed critical hit preserves target and precedes damage for client hints',async()=>{
 const session=new ShowdownSession();try{
  await session.start({...encounter(pokemon('own',68,[480,0,0,0]),pokemon('wild',129,[150,0,0,0])),testEnemyHp:999999});
  const state=await session.choose({type:'MOVE',index:0});
  const critical=state.events.findIndex(event=>event.type==='CRIT'&&event.targetId==='wild');
  const damage=state.events.findIndex(event=>event.type==='DAMAGE'&&event.targetId==='wild');
  assert.ok(critical>=0);assert.ok(damage>critical);
  assert.ok(state.pokemon.find(p=>p.id==='wild').hp<999999);
 }finally{await session.close();}
});
test('Power Herb skips the charging turn using native engine rules',async()=>{
 const session=new ShowdownSession();try{
  await session.start({...encounter(pokemon('own',25,[13,0,0,0]),pokemon('wild',129,[150,0,0,0])),testEnemyHp:999999});
  // Native fixture only: the application deliberately rejects unmapped held-item IDs.
  session.stream.battle.sides[0].active[0].setItem('powerherb');
  const state=await session.choose({type:'MOVE',index:0});
  assert.ok(state.events.some(event=>event.type==='MOVE'&&event.move===13),state.logs.join('\n'));
  assert.ok(!state.events.some(event=>event.type==='CHARGE'));
  assert.ok(state.pokemon.find(p=>p.id==='wild').hp<999999);
 }finally{await session.close();}
});
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

test('current stat stages are authoritative after increases, decreases, clearing and switching',async()=>{
 const s=new ShowdownSession();try{
  const own=pokemon('own',6,[14,45,114,150]);
  await s.start(encounter(own,pokemon('wild',143,[150,0,0,0]),[pokemon('reserve',25,[150,0,0,0])]));
  const up=await s.choose({type:'MOVE',index:0});assert.equal(up.pokemon.find(p=>p.id==='own').boosts.atk,2);
  const down=await s.choose({type:'MOVE',index:1});assert.equal(down.pokemon.find(p=>p.id==='wild').boosts.atk,-1);
  const clear=await s.choose({type:'MOVE',index:2});
  assert.equal(clear.pokemon.find(p=>p.id==='own').boosts.atk,0);assert.equal(clear.pokemon.find(p=>p.id==='wild').boosts.atk,0);
  await s.choose({type:'MOVE',index:0});
  const switched=await s.choose({type:'SWAP',pokemonId:'reserve'});
  assert.equal(switched.pokemon.find(p=>p.id==='own').boosts.atk,0);
 }finally{await s.close();}
});

test('entry ability stat stages are present in the initial snapshot',async()=>{
 const s=new ShowdownSession();try{
  const state=await s.start(encounter(pokemon('own',6,[150,0,0,0]),pokemon('wild',129,[150,0,0,0],{ability:22})));
  assert.equal(state.pokemon.find(p=>p.id==='own').boosts.atk,-1);
 }finally{await s.close();}
});

for(const count of [3,5])test(`experimental 1v${count} uses simultaneous wild actions and spread damage`,async()=>{
 const s=new ShowdownSession();try{
  const own=pokemon('own',6,[57,82,150,0]);
  const wilds=Array.from({length:count},(_,i)=>pokemon('wild'+i,143,[150,0,0,0]));
  const start=await s.start({...encounter(own,wilds[0]),wilds});
  assert.equal(s.stream.battle.sides[0].active.length,1);assert.equal(s.stream.battle.sides[1].active.length,count);
  const after=await s.choose({type:'MOVE',index:0});
  assert.equal(after.events.filter(e=>e.type==='DAMAGE'&&e.targetId.startsWith('wild')).length,count);
  assert.equal(after.events.find(e=>e.type==='MOVE'&&e.casterId==='own').targetIds.length,count);
  assert.equal(after.actions.filter(a=>a.casterId.startsWith('wild')).length,count);
  assert.ok(after.pokemon.filter(p=>p.side===1).every(p=>p.hp<start.pokemon.find(o=>o.id===p.id).hp));
  const aimed=await s.choose({type:'MOVE',index:1,targetId:'wild'+(count-1)});
  assert.equal(aimed.events.filter(e=>e.type==='DAMAGE').length,1);
  assert.equal(aimed.events.find(e=>e.type==='DAMAGE').targetId,'wild'+(count-1));
 }finally{await s.close();}
});

for(const count of [3,5])test(`1v${count} keeps fainted slots empty and ends after all wild Pokemon faint`,async()=>{
 const s=new ShowdownSession();try{
  const own=pokemon('own',6,[82,57,150,0],{level:100});
  const wilds=Array.from({length:count},(_,i)=>pokemon('wild'+i,10,[150,0,0,0],{level:1}));
  await s.start({...encounter(own,wilds[0]),wilds});
  const first=await s.choose({type:'MOVE',index:0,targetId:'wild0'});
  assert.equal(first.pokemon.find(p=>p.id==='wild0').hp,0);assert.ok(!first.ended);assert.ok(!first.forceSwitch);
  assert.equal(first.actions.filter(a=>a.casterId.startsWith('wild')).length,count-1);
  await assert.rejects(()=>s.choose({type:'MOVE',index:0,targetId:'wild0'}));
  const end=await s.choose({type:'MOVE',index:1});
  assert.ok(end.ended);assert.equal(end.winner,'Player');
  assert.equal(end.events.filter(e=>e.type==='FAINT').length,count-1);
 }finally{await s.close();}
});
test('five wild attackers use the normal damage engine and a fainted lead can be replaced',async()=>{
 const s=new ShowdownSession();try{
  const own=pokemon('own',143,[150,0,0,0],{level:100});
  const wilds=Array.from({length:5},(_,i)=>pokemon('wild'+i,10,[33,0,0,0],{level:5}));
  await s.start({...encounter(own,wilds[0]),wilds});
  const result=await s.choose({type:'MOVE',index:0});
  assert.equal(result.events.filter(e=>e.type==='DAMAGE'&&e.targetId==='own').length,5);
 }finally{await s.close();}
 const t=new ShowdownSession();try{
  const lead=pokemon('lead',1,[150,0,0,0],{level:1,hp:1});
  const wilds=Array.from({length:5},(_,i)=>pokemon('wild'+i,150,[94,0,0,0],{level:50}));
  await t.start({...encounter(lead,wilds[0],[pokemon('reserve',6,[150,0,0,0],{level:100})]),wilds});
  const result=await t.choose({type:'MOVE',index:0});assert.ok(result.forceSwitch);assert.ok(!result.ended);
  const replaced=await t.choose({type:'SWAP',pokemonId:'reserve'});
  assert.equal(replaced.pokemon.find(p=>p.side===0&&p.active).id,'reserve');
 }finally{await t.close();}
});

test('horde spread respects one immune target and still hits the others',async()=>{
 const s=new ShowdownSession();try{
  const own=pokemon('own',6,[57,0,0,0]);
  const wilds=Array.from({length:5},(_,i)=>pokemon('wild'+i,143,[150,0,0,0],i===4?{ability:11}:{}));
  const initial=await s.start({...encounter(own,wilds[0]),wilds});
  const result=await s.choose({type:'MOVE',index:0});
  assert.equal(result.events.filter(e=>e.type==='DAMAGE').length,4);
  assert.equal(result.pokemon.find(p=>p.id==='wild4').hp,initial.pokemon.find(p=>p.id==='wild4').hp);
  assert.equal(result.events.find(e=>e.type==='MOVE'&&e.casterId==='own').animationSuppressed,false);
 }finally{await s.close();}
});
test('horde target status and initial entry abilities use native rules',async()=>{
 const s=new ShowdownSession();try{
  const own=pokemon('own',6,[86,114,0,0],{ability:7});
  const wilds=Array.from({length:5},(_,i)=>pokemon('wild'+i,129,[86,0,0,0],i===0?{ability:22}:{}));
  const initial=await s.start({...encounter(own,wilds[0]),wilds});
  assert.equal(initial.pokemon.find(p=>p.id==='own').boosts.atk,-1);
  const result=await s.choose({type:'MOVE',index:0,targetId:'wild4'});
  assert.equal(result.pokemon.find(p=>p.id==='wild4').status,'par');
  assert.equal(result.pokemon.find(p=>p.id==='own').status,'');
  assert.ok(result.pokemon.filter(p=>p.side===1&&p.id!=='wild4').every(p=>p.status===''));
  const clear=await s.choose({type:'MOVE',index:1});
  assert.equal(clear.pokemon.find(p=>p.id==='own').boosts.atk,0);
 }finally{await s.close();}
});


test('effect-test opponent starts with 999999 HP and specified moves use real damage/status rules',async()=>{
 const s=new ShowdownSession();try{
  const own=pokemon('own',149,[182,0,0,0],{level:100}),wild=pokemon('wild',129,[150,0,0,0],{level:5});
  const initial=await s.start({...encounter(own,wild),testEnemyHp:999999});
  assert.equal(initial.pokemon.find(p=>p.id==='wild').hp,999999);
  assert.equal(initial.pokemon.find(p=>p.id==='wild').maxHp,999999);
  const hit=await s.choose({type:'TEST_MOVE',index:82});
  assert.equal(hit.pokemon.find(p=>p.id==='wild').hp,999959);
  assert.equal(hit.pokemon.find(p=>p.id==='wild').maxHp,999999);
  const status=await s.choose({type:'TEST_MOVE',index:86});
  assert.equal(status.pokemon.find(p=>p.id==='wild').status,'par');
  assert.equal(status.pokemon.find(p=>p.id==='own').moves[0].id,182);
 }finally{await s.close();}
 const normal=new ShowdownSession();try{
  const state=await normal.start(encounter(pokemon('own',25,[85,0,0,0]),pokemon('wild',129,[150,0,0,0])));
  assert.notEqual(state.pokemon.find(p=>p.id==='wild').maxHp,999999);
 }finally{await normal.close();}
});


test('999999 HP effect-test opponents never execute moves, ordinary battles still do',async()=>{
 for(const idle of [true,false]) {
  const s=new ShowdownSession();try{
   const own=pokemon('own',25,[182,0,0,0],{level:100}),wild=pokemon('wild',150,[14,0,0,0],{level:100});
   await s.start({...encounter(own,wild),...(idle?{testEnemyHp:999999}:{})});
   const turn=await s.choose({type:'TEST_MOVE',index:45});
   const enemyMoves=turn.actions.filter(a=>a.casterId==='wild');
   assert.equal(enemyMoves.length,idle?0:1);
   assert.equal(turn.pokemon.find(p=>p.id==='wild').boosts.atk,idle?-1:1);
   assert.ok(turn.turn>=2);
  }finally{await s.close();}
 }
});
