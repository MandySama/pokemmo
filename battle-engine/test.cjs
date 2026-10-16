'use strict';
const assert = require('node:assert/strict');
const {handle,battles}=require('./worker.cjs');
function pokemon(species, hp=100, move=33) {
    return {species,level:30,hp,ability:species===25?9:species===92?26:50,gender:0,friendship:70,item:0,
        ivs:[0,0,0,0,0,0],evs:[0,0,0,0,0,0],stats:[100,60,50,60,50,70],status:'',moves:[[move,20,20]]};
}
function start(id,party,enemy) {handle({action:'start',battle:id,party,enemy,seed:[1,2,3,4]});return battles.get(id);}
function move(id,revision,slot=1){return handle({action:'move',battle:id,revision,slot});}
try {
    const p=pokemon(25,43),enemy=pokemon(19);
    const s=start('damage',[p],[enemy]);
    assert.equal(s.battle.p1.active[0].hp,43,'initial HP must not heal');
    assert.equal(s.battle.p1.active[0].storedStats.atk,60,'stored game stats');
    move('damage',0);
    assert.equal(s.revision,1);assert.equal(s.battle.p1.active[0].moveSlots[0].pp,19);
    assert.equal(s.battle.p1.active[0].baseMoveSlots[0].pp,19,'persistent PP shares battle move slot');
    assert.ok(s.battle.p2.active[0].hp<100);assert.ok(s.battle.p1.active[0].hp<43);
    const unchanged=s.battle.p2.active[0].hp;move('damage',0);move('damage',1,4);
    assert.equal(s.revision,1);assert.equal(s.battle.p2.active[0].hp,unchanged,'stale/invalid cannot advance');
    const immune=start('immune',[pokemon(19)],[pokemon(92)]);move('immune',0);
    assert.equal(immune.battle.p2.active[0].hp,100,'normal attack ghost immunity');
    const status=start('status',[pokemon(25,100,86)],[pokemon(19)]);move('status',0);
    assert.equal(status.battle.p2.active[0].status,'par');
    const forced=start('forced',[pokemon(19,1),pokemon(25)],[pokemon(19)]);
    forced.battle.p2.active[0].storedStats.spe=200;
    move('forced',0);assert.ok(forced.battle.p1.activeRequest.forceSwitch);
    handle({action:'switch',battle:'forced',revision:1,slot:1});
    assert.equal(forced.battle.p1.active[0].name,'slot1');
    const lost=start('lost',[pokemon(19,1)],[pokemon(19)]);lost.battle.p2.active[0].storedStats.spe=200;
    move('lost',0);assert.equal(lost.outcome,2);
    const won=start('won',[pokemon(19)],[pokemon(19,1)]);won.battle.p1.active[0].storedStats.spe=200;
    move('won',0);assert.equal(won.outcome,1);
    const weakEnemy=pokemon(19);weakEnemy.stats[1]=1;
    const switching=start('switch',[pokemon(19),pokemon(25)],[weakEnemy]);
    handle({action:'switch',battle:'switch',revision:0,slot:1});assert.equal(switching.battle.p1.active[0].name,'slot1');
    move('switch',1);const spent=switching.battle.p1.active[0].moveSlots[0].pp;
    handle({action:'switch',battle:'switch',revision:2,slot:0});
    handle({action:'switch',battle:'switch',revision:3,slot:1});
    assert.equal(switching.battle.p1.active[0].moveSlots[0].pp,spent,'switching cannot restore PP');
    handle({action:'run',battle:'switch',revision:4});assert.equal(switching.outcome,4);
    const faint=pokemon(19,0);const entry=start('entry',[faint,pokemon(25)],[pokemon(19)]);
    assert.equal(entry.battle.p1.active[0].name,'slot1');
    const depleted=pokemon(19);depleted.moves[0][1]=0;
    const struggle=start('struggle',[depleted],[pokemon(19)]);move('struggle',0);
    assert.equal(struggle.battle.p1.active[0].moveSlots[0].pp,0);assert.equal(struggle.revision,1);
    const champion=pokemon(19);champion.stats[1]=200;champion.stats[5]=200;
    handle({action:'start',battle:'trainer',party:[champion],enemy:[pokemon(19,1),pokemon(25,1)],allowRun:false,seed:[1,2,3,4]});
    const trainer=battles.get('trainer');handle({action:'run',battle:'trainer',revision:0});
    assert.equal(trainer.outcome,0);assert.equal(trainer.revision,0,'trainer cannot run');
    move('trainer',0);assert.equal(trainer.battle.p2.active[0].name,'slot1','automatic enemy replacement');assert.equal(trainer.outcome,0);
    move('trainer',1);assert.equal(trainer.outcome,1,'whole enemy team must faint');
    console.log('PASS real Showdown: HP/stat restore, PP, damage, immunity, paralysis, stale/illegal choice, switch, forced switch, win/loss/run, faint lead, Struggle');
} finally {for(const id of [...battles.keys()])handle({action:'close',battle:id});assert.equal(battles.size,0);}
