'use strict';
const {Battle, Dex} = require('pokemon-showdown');
const readline = require('node:readline');
const battles = new Map();
const stats = ['hp', 'atk', 'def', 'spa', 'spd', 'spe'];
const species = new Map(Dex.species.all().filter(s => !s.forme && s.num > 0).map(s => [s.num, s.name]));
const moves = new Map(Dex.moves.all().filter(m => m.num > 0).map(m => [m.num, m.name]));
const abilities = new Map(Dex.abilities.all().map(a => [a.num, a.name]));
const items = new Map(Dex.items.all().filter(i => i.num > 0).map(i => [i.num, i.name]));

function team(data) {
    return data.map((m, i) => {
        if (!species.has(m.species) || !abilities.has(m.ability)) throw Error('Unknown species/ability');
        if (m.item && !items.has(m.item)) throw Error('Unknown held item');
        return {name: `slot${i}`, species: species.get(m.species), level: m.level,
            ability: abilities.get(m.ability), item: m.item ? items.get(m.item) : '',
            gender: ['M', 'F', ''][m.gender], happiness: m.friendship,
            ivs: Object.fromEntries(stats.map((k,j) => [k,m.ivs[j]])),
            evs: Object.fromEntries(stats.map((k,j) => [k,m.evs[j]])),
            moves: m.moves.length ? m.moves.map(x => {
                if (!moves.has(x[0])) throw Error('Unknown move'); return moves.get(x[0]);
            }) : ['Struggle']};
    });
}
function create(input) {
    const initial = [input.party, input.enemy];
    const format = {...Dex.formats.get('gen9customgame'), id: 'pokemmosingles', ruleset: [],
        onBegin() {
            for (let side = 0; side < 2; side++) for (const p of this.sides[side].pokemon) {
                const m = initial[side][Number(p.name.slice(4))];
                p.maxhp = p.baseMaxhp = m.stats[0]; p.hp = m.hp;
                p.fainted = !m.hp;
                for (let j = 1; j < stats.length; j++) p.storedStats[stats[j]] = p.baseStoredStats[stats[j]] = m.stats[j];
                p.status = m.status;
                if (m.status) p.statusState = {id:m.status, target:p, ...(m.status === 'slp' ? {time:3,startTime:3} : {})};
                p.moveSlots.forEach((slot,j) => {if(m.moves[j]) {slot.pp=m.moves[j][1];slot.maxpp=m.moves[j][2];}});
                p.baseMoveSlots = p.moveSlots.slice();
            }
            // 首位必须是可战个体，保留 set.name 中的原始队伍槽位。
            for (const side of this.sides) {
                const alive = side.pokemon.findIndex(p => !p.fainted);
                if (alive < 0) throw Error('No alive Pokemon');
                [side.pokemon[0],side.pokemon[alive]] = [side.pokemon[alive],side.pokemon[0]];
                side.pokemon.forEach((p,i)=>p.position=i);
                side.pokemonLeft = side.pokemon.filter(p=>!p.fainted).length;
            }
        }};
    const battle = new Battle({format, seed: input.seed, p1:{name:'Player',team:team(input.party)},
        p2:{name:'Wild',team:team(input.enemy)}, send() {}});
    const session = {battle, revision:0, outcome:0, initial:input.party, allowRun:input.allowRun!==false}; battles.set(input.battle,session); return session;
}
function legal(session) {
    const side = session.battle.p1;
    if (session.outcome || side.activeRequest?.forceSwitch) return [];
    const list = side.activeRequest?.active?.[0]?.moves || [];
    return list.flatMap((m,i)=>!m.disabled && m.pp !== 0 ? [i+1] : []);
}
function ai(battle) {
    const side=battle.p2, request=side.activeRequest;
    if (!request || request.wait || battle.ended || side.isChoiceDone()) return;
    if (request.forceSwitch) {
        const index=side.pokemon.findIndex(p=>!p.fainted&&!p.isActive);
        if(index>=0) battle.choose('p2',`switch ${index+1}`);
    } else if(request.active) {
        const available=request.active[0].moves.flatMap((m,i)=>!m.disabled&&m.pp!==0?[i+1]:[]);
        battle.choose('p2',`move ${available.length?available[battle.random(available.length)]:1}`);
    }
}
function snapshot(s, accepted, from) {
    const b=s.battle, events=[];
    const log=b.log.slice(from);
    for(let index=0;index<log.length;index++) {
        let line=log[index];
        if(line.startsWith("|split|")){line=log[++index];index++;}
        const f=line.split('|'), side=f[2]?.startsWith('p2')?1:0;
        if(f[1]==='move') events.push([0,side,Dex.moves.get(f[3]).num]);
        else if(f[1]==='switch') events.push([5,side,Number(f[2].split('slot')[1])]);
        else if(f[1]==='faint') events.push([1,side,0]);
        else if(f[1]==='-status') events.push([2,side,['brn','par','slp','frz','psn','tox'].indexOf(f[3])+1]);
        else if(f[1]==='-miss') events.push([3,side,0]);
        else if(f[1]==='-damage'||f[1]==='-heal') events.push([4,side,parseInt(f[3],10)]);
    }
    if(b.ended && !s.outcome) s.outcome=b.winner==='Player'?1:b.winner==='Wild'?2:3;
    const party=[...b.p1.pokemon].sort((a,c)=>Number(a.name.slice(4))-Number(c.name.slice(4)));
    // 内部响应使用有界二进制；不向客户端透传引擎的隐藏信息或日志。
    const chunks=[];
    const u8=n=>chunks.push(Buffer.from([n]));
    const u16=n=>{const x=Buffer.alloc(2);x.writeUInt16LE(n);chunks.push(x);};
    u8(accepted?1:0);u16(s.revision);u8(s.outcome);u8(Number(b.p1.active[0].name.slice(4)));
    u8(b.p1.activeRequest?.forceSwitch?1:0);u16(b.p2.active[0].hp);u16(b.p2.active[0].maxhp);
    u8(party.length);
    for(const p of party) {u16(p.hp);u8(['','brn','par','slp','frz','psn','tox'].indexOf(p.status==='fnt'?'':p.status));u8(Math.max(1,s.initial[Number(p.name.slice(4))].moves.length));const original=s.initial[Number(p.name.slice(4))].moves;
        if(!original.length)u8(1);else for(const m of original){const base=p.baseMoveSlots.find(slot=>Dex.moves.get(slot.id).num===m[0]);u8(base?Math.min(m[2],base.pp):0);}}
    u16(events.length);for(const e of events){u8(e[0]);u8(e[1]);u16(e[2]);}
    const available=legal(s);u8(available.length);available.forEach(u8);
    u8(available.length);
    for(const slot of available) {
        const move=b.p1.activeRequest.active[0].moves[slot-1];
        u8(slot);u16(Math.max(0,Dex.moves.get(move.id).num));u8(move.pp??1);u8(move.maxpp??1);
    }
    u8(Number(b.p2.active[0].name.slice(4)));
    return Buffer.concat(chunks).toString('base64');
}
function handle(input) {
    if(input.action==='close') {const s=battles.get(input.battle);s?.battle.destroy();battles.delete(input.battle);return '';}
    let s=battles.get(input.battle), from=s?.battle.log.length||0;
    if(input.action==='start') {
        if(!s) {s=create(input);from=0;}
        return snapshot(s,true,from);
    }
    if(!s) throw Error('Unknown battle');
    if(input.revision!==s.revision||s.outcome)return snapshot(s,false,from);
    const b=s.battle;
    if(input.action==='run') {if(!s.allowRun)return snapshot(s,false,from);s.outcome=4;s.revision++;return snapshot(s,true,from);}
    let choice;
    if(input.action==='move') {
        if(!legal(s).includes(input.slot))return snapshot(s,false,from);
        choice=`move ${input.slot}`;
    } else if(input.action==='switch') {
        const index=b.p1.pokemon.findIndex(p=>p.name===`slot${input.slot}`);
        if(index<0||b.p1.pokemon[index].fainted||b.p1.pokemon[index].isActive)return snapshot(s,false,from);
        choice=`switch ${index+1}`;
    } else throw Error('Unknown operation');
    if(!b.choose('p1',choice))return snapshot(s,false,from);
    ai(b);
    for(let i=0;i<6&&b.p2.activeRequest?.forceSwitch&&!b.p2.isChoiceDone();i++)ai(b);
    s.revision++;
    return snapshot(s,true,from);
}
if(require.main===module) readline.createInterface({input:process.stdin}).on('line', line=>{
    try {const input=JSON.parse(line);process.stdout.write(`OK\t${handle(input)}\n`);}
    catch(error) {console.error(error.stack);process.stdout.write('ERROR\n');}
});
module.exports={handle,battles};
