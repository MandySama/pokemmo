'use strict';
const assert=require('node:assert/strict');
const {handle,battles}=require('./worker.cjs');
function pokemon(species,move,pp,maxpp=35){return {species,level:50,hp:1000,ability:50,gender:0,friendship:70,item:0,
    ivs:[0,0,0,0,0,0],evs:[0,0,0,0,0,0],stats:[1000,60,50,60,50,70],status:'',moves:[[move,pp,maxpp]]};}
try {
    handle({action:'start',battle:'pp',party:[pokemon(19,33,0),pokemon(25,33,8)],enemy:[pokemon(19,45,40,40)],seed:[1,2,3,4]});
    const s=battles.get('pp');
    handle({action:'infinitePp',battle:'pp',revision:0,enabled:true});
    assert.equal(s.battle.p1.activeRequest.active[0].moves[0].id,'tackle','zero PP move replaces Struggle');
    assert.equal(s.battle.p1.active[0].moveSlots[0].pp,35);
    const result=handle({action:'move',battle:'pp',revision:1,slot:1});
    assert.equal(s.revision,2);assert.ok(s.battle.p2.active[0].hp<1000);
    assert.equal(s.battle.p1.active[0].moveSlots[0].pp,35,'infinite PP stays full');
    assert.equal(Buffer.from(result,'base64')[15],0,'stored original PP remains zero');
    assert.equal(s.battle.p2.active[0].moveSlots[0].pp,39,'enemy PP is still spent');
    handle({action:'switch',battle:'pp',revision:2,slot:1});
    handle({action:'move',battle:'pp',revision:3,slot:1,replacementMove:52});
    assert.equal(s.battle.p1.active[0].moveSlots[0].pp,25,'test replacements also infinite');
    handle({action:'infinitePp',battle:'pp',revision:4,enabled:false});
    assert.equal(s.battle.p1.active[0].moveSlots[0].pp,25,'test PP was not secretly consumed');
    handle({action:'move',battle:'pp',revision:5,slot:1,replacementMove:65535});
    assert.equal(s.battle.p1.active[0].moveSlots[0].pp,7,'restore learned pool then resume consumption');
    handle({action:'switch',battle:'pp',revision:6,slot:0});
    assert.equal(s.battle.p1.active[0].moveSlots[0].pp,0,'turning off restores exhausted original PP');
    assert.equal(s.battle.p1.activeRequest.active[0].moves[0].id,'struggle');
    handle({action:'infinitePp',battle:'pp',revision:7,enabled:true});
    s.battle.p1.active[0].addVolatile('taunt');
    const before=s.revision;
    handle({action:'move',battle:'pp',revision:before,slot:1,replacementMove:86});
    assert.equal(s.revision,before,'unlimited PP does not bypass Taunt');
    console.log('PASS infinite PP, zero PP usability, original pool isolation, enemy PP, switch, test replacement, disable restoration, Taunt');
} finally {for(const id of [...battles.keys()])handle({action:'close',battle:id});}
