'use strict';
const assert=require('node:assert/strict');
const {handle,battles}=require('./worker.cjs');
function pokemon(species,move=33){return {species,level:50,hp:1000,ability:50,gender:0,friendship:70,item:0,
    ivs:[0,0,0,0,0,0],evs:[0,0,0,0,0,0],stats:[1000,60,50,60,50,70],status:'',moves:[[move,20,20]]};}
function start(id,enemy=19){handle({action:'start',battle:id,party:[pokemon(19),pokemon(25)],enemy:[pokemon(enemy,45)],seed:[1,2,3,4]});return battles.get(id);}
function use(id,revision,replacementMove,slot=1){return handle({action:'move',battle:id,revision,slot,replacementMove});}
try{
    const s=start('replace',92);
    use('replace',0,247);
    assert.equal(s.battle.p1.active[0].moveSlots[0].id,'shadowball');
    assert.ok(s.battle.p2.active[0].hp<1000,'replacement Ghost move hits a Ghost target');
    assert.ok(s.battle.log.some(l=>l.startsWith('|move|p1')&&l.includes('|Shadow Ball|')));
    assert.equal(s.battle.p1.active[0].moveSlots[0].pp,14);
    assert.equal(s.originalSlots.get('slot0')[0].pp,20,'testing does not spend the learned move PP');
    use('replace',1,247);assert.equal(s.battle.p1.active[0].moveSlots[0].pp,13,'same selection cannot refill PP');
    const hp=s.battle.p2.active[0].hp;
    use('replace',1,52);assert.equal(s.revision,2);assert.equal(s.battle.p1.active[0].moveSlots[0].id,'shadowball','stale replacement rejected');
    use('replace',2,60000);use('replace',2,52,4);
    assert.equal(s.revision,2);assert.equal(s.battle.p2.active[0].hp,hp,'invalid replacement has no side effects');
    handle({action:'switch',battle:'replace',revision:2,slot:1});
    handle({action:'switch',battle:'replace',revision:3,slot:0});
    assert.equal(s.battle.p1.active[0].moveSlots[0].pp,13,'switching keeps test PP');
    use('replace',4,65535);
    assert.equal(s.battle.p1.active[0].moveSlots[0].id,'tackle');
    assert.equal(s.originalSlots.get('slot0')[0].pp,19);
    assert.equal(s.battle.p2.active[0].hp,hp,'restored Normal move is immune against Ghost');
    const status=start('replacement-status');use('replacement-status',0,86);
    assert.equal(status.battle.p2.active[0].status,'par','replacement status effect is real');
    const depleted=start('replacement-depleted');depleted.battle.p1.active[0].moveSlots[0].pp=0;
    use('replacement-depleted',0,52);assert.equal(depleted.revision,1,'new move has its own PP');
    const blocked=start('replacement-blocked');
    blocked.battle.p1.active[0].addVolatile('taunt');
    use('replacement-blocked',0,86);
    assert.equal(blocked.revision,0);assert.equal(blocked.battle.p1.active[0].moveSlots[0].id,'tackle','illegal replacement rolls back');
    console.log('PASS replacement move identity, real damage/type immunity/status, own PP, no refill, learned PP isolation, switch persistence, default restore, stale/invalid/disabled rollback');
}finally{for(const id of [...battles.keys()])handle({action:'close',battle:id});}
