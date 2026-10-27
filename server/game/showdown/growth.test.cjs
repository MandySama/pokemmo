const {test}=require('node:test');
const assert=require('node:assert/strict');
const {ShowdownSession}=require('./bridge.cjs');
const mon=(id,species,moves)=>({id,species,form:0,item:0,ability:65,level:8,gender:0,nature:0,ivs:[31,31,31,31,31,31],evs:[0,0,0,0,0,0],stats:[50,20,20,20,20,20],moves,pp:[9,10,-1,-1],ppUps:[0,0,0,0],hp:50,status:0,friendship:70});
test('empty-slot learning updates native request and does not refill existing PP or duplicate slots',async()=>{
 const session=new ShowdownSession();try{
  const own=mon('own',1,[33,45,0,0]);
  await session.start({player:own,party:[own],wilds:[mon('wild',143,[150,0,0,0])],seed:[1,2,3,4]});
  const grown={...own,level:9,moves:[33,45,22,0]};
  const result=session.growth([grown]);
  const actor=result.pokemon.find(p=>p.id==='own');assert.deepEqual(actor.moves.map(m=>m.id),[33,45,22]);assert.equal(actor.moves[0].pp,9);assert.ok(actor.moves[2].pp>0);
  session.growth([grown]);assert.equal(session.result().pokemon.find(p=>p.id==='own').moves.length,3);
  const turn=await session.choose({type:'MOVE',index:2});assert.ok(turn.events.some(e=>e.type==='MOVE'&&e.move===22));
 }finally{await session.close();}
});
