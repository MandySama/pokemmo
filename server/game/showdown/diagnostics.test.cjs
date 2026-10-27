'use strict';
const {test}=require('node:test');
const assert=require('node:assert/strict');
const {ShowdownSession}=require('./bridge.cjs');
const mon=(id,species,moves,extra={})=>({id,species,form:0,item:0,ability:7,level:50,gender:0,nature:0,
 ivs:[31,31,31,31,31,31],evs:[0,0,0,0,0,0],moves,pp:[-1,-1,-1,-1],ppUps:[0,0,0,0],hp:999,status:0,friendship:70,...extra});
const start=(s,own,wild)=>s.start({player:own,party:[own],wilds:Array.isArray(wild)?wild:[wild],seed:[1,2,3,4]});

test('diagnostic: Leftovers heals floor(maxHP/16) every turn without consumption',async()=>{
 const s=new ShowdownSession();try{
  await start(s,mon('own',151,[150,0,0,0],{hp:50}),mon('wild',151,[150,0,0,0]));
  const holder=s.stream.battle.sides[0].active[0];holder.setItem('leftovers');
  for(const hp of [60,70]){
   const r=await s.choose({type:'MOVE',index:0});
   assert.equal(r.pokemon.find(p=>p.id==='own').hp,hp);
   assert.equal(holder.item,'leftovers');
   assert.ok(r.events.some(e=>e.type==='HEAL'&&e.targetId==='own'&&e.hp===hp));
  }
 }finally{await s.close();}
});
test('diagnostic: Sitrus heals one quarter once and is consumed',async()=>{
 const s=new ShowdownSession();try{
  await start(s,mon('own',151,[150,0,0,0],{hp:80}),mon('wild',151,[150,0,0,0]));
  const holder=s.stream.battle.sides[0].active[0];holder.setItem('sitrusberry');
  const r=await s.choose({type:'MOVE',index:0});
  assert.equal(r.pokemon.find(p=>p.id==='own').hp,123);assert.equal(holder.item,'');
  assert.equal(r.logs.filter(l=>l.startsWith('|-enditem|')).length,1);
  const next=await s.choose({type:'MOVE',index:0});assert.ok(!next.events.some(e=>e.type==='HEAL'));
 }finally{await s.close();}
});
test('diagnostic: Life Orb applies one tenth max HP recoil',async()=>{
 const s=new ShowdownSession();try{
  await start(s,mon('own',151,[33,0,0,0]),mon('wild',143,[150,0,0,0]));
  s.stream.battle.sides[0].active[0].setItem('lifeorb');
  const r=await s.choose({type:'MOVE',index:0});
  assert.equal(r.pokemon.find(p=>p.id==='own').hp,158);
  assert.ok(r.logs.some(l=>l.includes('[from] item: Life Orb')));
 }finally{await s.close();}
});
test('diagnostic: Power Herb releases at the enemy in the same turn',async()=>{
 const s=new ShowdownSession();try{
  await start(s,mon('own',151,[13,0,0,0]),mon('wild',151,[150,0,0,0]));
  const holder=s.stream.battle.sides[0].active[0];holder.setItem('powerherb');
  const r=await s.choose({type:'MOVE',index:0});
  assert.equal(holder.item,'');assert.ok(!r.events.some(e=>e.type==='CHARGE'));
  assert.ok(r.events.some(e=>e.type==='MOVE'&&e.move===13&&e.targetId==='wild'));
  assert.ok(r.pokemon.find(p=>p.id==='wild').hp<175);
 }finally{await s.close();}
});
test('diagnostic: Water Absorb heals quarter HP; Levitate blocks ground damage',async()=>{
 for(const [ability,move,hp,expected] of [[11,55,50,93],[26,89,175,175]]){
  const s=new ShowdownSession();try{
   await start(s,mon('own',151,[move,0,0,0]),mon('wild',151,[150,0,0,0],{ability,hp}));
   const r=await s.choose({type:'MOVE',index:0});assert.equal(r.pokemon.find(p=>p.id==='wild').hp,expected);
  }finally{await s.close();}
 }
});
test('diagnostic: Intimidate lowers attack on entry; Speed Boost increments once per turn',async()=>{
 const s=new ShowdownSession();try{
  const r=await start(s,mon('own',151,[150,0,0,0],{ability:3}),mon('wild',130,[150,0,0,0],{ability:22}));
  assert.equal(r.pokemon.find(p=>p.id==='own').boosts.atk,-1);
  for(const spe of [1,2])assert.equal((await s.choose({type:'MOVE',index:0})).pokemon.find(p=>p.id==='own').boosts.spe,spe);
 }finally{await s.close();}
});

test('diagnostic: Transform submits the current copied slot',async()=>{
 const s=new ShowdownSession();try{
  await start(s,mon('own',132,[144,150,0,0]),mon('wild',25,[150,33,0,0]));
  const copy=await s.choose({type:'MOVE',index:0});
  assert.equal(copy.pokemon.find(p=>p.id==='own').moves[0].id,150);
  const used=await s.choose({type:'MOVE',index:0});
  assert.ok(used.actions.some(a=>a.casterId==='own'&&a.move===150));
 }finally{await s.close();}
});
test('diagnostic: Mimic submits the current copied slot',async()=>{
 const s=new ShowdownSession();try{
  await start(s,mon('own',151,[102,150,0,0]),mon('wild',25,[85,150,0,0],{level:5}));
  await s.choose({type:'MOVE',index:1});
  const copy=await s.choose({type:'MOVE',index:0});
  assert.equal(copy.pokemon.find(p=>p.id==='own').moves[0].id,85);
  const used=await s.choose({type:'MOVE',index:0});
  assert.ok(used.actions.some(a=>a.casterId==='own'&&a.move===85));
 }finally{await s.close();}
});
test('diagnostic: Imposter submits the copied slot after entry',async()=>{
 const s=new ShowdownSession();try{
  const initial=await start(s,mon('own',132,[33,0,0,0],{ability:150}),mon('wild',25,[150,33,0,0]));
  assert.equal(initial.pokemon.find(p=>p.id==='own').moves[0].id,150);
  assert.equal(initial.pokemon.find(p=>p.id==='own').moves[0].pp,5);
  const used=await s.choose({type:'MOVE',index:0});
  assert.ok(used.actions.some(a=>a.casterId==='own'&&a.move===150));
 }finally{await s.close();}
});
test('diagnostic: Future Sight prepares then animates at the delayed damage',async()=>{
 const s=new ShowdownSession();try{
  await start(s,mon('own',151,[248,150,0,0]),mon('wild',151,[150,0,0,0]));
  const first=await s.choose({type:'MOVE',index:0});
  assert.ok(first.events.some(e=>e.type==='CHARGE'&&e.move===248));
  assert.ok(!first.events.some(e=>e.type==='DAMAGE'));
  await s.choose({type:'MOVE',index:1});
  const hit=await s.choose({type:'MOVE',index:1});
  const release=hit.events.findIndex(e=>e.type==='MOVE'&&e.move===248&&e.casterId==='own'&&e.targetId==='wild');
  assert.ok(release>=0,hit.logs.join('\n'));
  assert.ok(hit.events.findIndex(e=>e.type==='DAMAGE'&&e.targetId==='wild')>release);
 }finally{await s.close();}
});
test('diagnostic: Bide prepares on storage turns and releases at the real target',async()=>{
 const s=new ShowdownSession();try{
  await start(s,mon('own',151,[117,150,0,0]),mon('wild',151,[33,0,0,0]));
  const first=await s.choose({type:'MOVE',index:0});
  assert.ok(first.events.some(e=>e.type==='CHARGE'&&e.move===117));
  const second=await s.choose({type:'MOVE',index:0});
  assert.ok(second.events.some(e=>e.type==='CHARGE'&&e.move===117));
  const third=await s.choose({type:'MOVE',index:0});
  const release=third.events.findIndex(e=>e.type==='MOVE'&&e.move===117&&e.casterId==='own'&&e.targetId==='wild');
  assert.ok(release>=0,third.logs.join('\n'));
  assert.ok(third.events.findIndex(e=>e.type==='DAMAGE'&&e.targetId==='wild')>release);
 }finally{await s.close();}
});
test('diagnostic: one Soundproof target does not cancel Growl on four valid targets',async()=>{
 const s=new ShowdownSession();try{
  await start(s,mon('own',151,[45,0,0,0]),Array.from({length:5},(_,i)=>mon('wild'+i,151,[150,0,0,0],{ability:i===4?43:7})));
  const result=await s.choose({type:'MOVE',index:0});
  assert.equal(result.events.filter(e=>e.type==='UNBOOST').length,4);
  assert.equal(result.events.find(e=>e.type==='MOVE'&&e.move===45).animationSuppressed,false);
 }finally{await s.close();}
});
