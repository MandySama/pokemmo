'use strict';
const assert=require('node:assert/strict');
const {handle,battles}=require('./worker.cjs');
function pokemon(move,species=19,status=''){return {species,level:50,hp:1000,ability:50,gender:0,friendship:70,item:0,ivs:[0,0,0,0,0,0],evs:[0,0,0,0,0,0],stats:[1000,60,50,60,50,70],status,moves:[[move,20,20]]};}
function start(id,player,enemy){handle({action:'start',battle:id,party:[player],enemy:[enemy],seed:[1,2,3,4]});}
function decode(value){const b=Buffer.from(value,'base64');let at=10;const count=b[at++];for(let i=0;i<count;i++){at+=3;const n=b[at++];at+=n;}const n=b.readUInt16LE(at);at+=2;const events=[];for(let i=0;i<n;i++){events.push([b[at],b[at+1],b.readUInt16LE(at+2)]);at+=4;}return {events,status:b[b.length-1]};}
function move(id,revision=0){return decode(handle({action:'move',battle:id,revision,slot:1}));}
function has(result,kind,side,value){return result.events.some(e=>e[0]===kind&&e[1]===side&&(value===undefined||e[2]===value));}
try{
 start('status',pokemon(86),pokemon(150));let result=move('status');
 assert.ok(has(result,2,1,2));assert.equal(result.status,2);assert.equal(battles.get('status').battle.p2.active[0].status,'par');
 result=move('status',1);assert.ok(has(result,10,1,2),'repeat paralysis gives explicit failure');
 start('immune',pokemon(86),pokemon(150,27));result=move('immune');assert.ok(has(result,11,1));assert.equal(result.status,0);
 start('asleep',pokemon(33,19,'slp'),pokemon(150));result=move('asleep');assert.ok(has(result,12,0,3));assert.ok(!has(result,0,0));
 start('protect',pokemon(33),pokemon(182));result=move('protect');assert.ok(has(result,13,1,182));assert.equal(battles.get('protect').battle.p2.active[0].hp,1000);
 start('full',pokemon(105),pokemon(150));result=move('full');assert.ok(has(result,10,0));
 start('awake',pokemon(33,19,'slp'),pokemon(150));battles.get('awake').battle.p1.active[0].statusState.time=1;result=move('awake');assert.ok(has(result,9,0,3));assert.ok(has(result,0,0));
 start('splash',pokemon(150),pokemon(150));result=move('splash');assert.ok(has(result,14,0));
 console.log('PASS real Showdown: status snapshot, repeated-status fail, immunity, sleep cant, Protect block, full-HP recovery fail, cure and Splash no effect');
}finally{for(const id of [...battles.keys()])handle({action:'close',battle:id});}
