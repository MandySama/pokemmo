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
    const session = {battle, revision:0, outcome:0, initial:input.party, allowRun:input.allowRun!==false,
        originalSlots:new Map(battle.p1.pokemon.map(p=>[p.name,p.baseMoveSlots.slice()])),
        infinitePp:false,ppValues:new WeakMap()};
    battles.set(input.battle,session); return session;
}
// Test replacements belong to the battle. Learned moves and their PP retain their own slot objects.
function replaceMove(session, input) {
    const id=input.replacementMove||0;
    if(!id)return {rollback(){}};
    const b=session.battle,p=b.p1.active[0],index=input.slot-1;
    if(!Number.isInteger(id)||id<1||id>65535||!Number.isInteger(index)||index<0||index>=p.moveSlots.length
        ||b.p1.activeRequest?.forceSwitch||!b.p1.activeRequest?.active)return null;
    const original=session.originalSlots.get(p.name)[index];
    const restoring=id===65535||id===Dex.moves.get(original.id).num;
    const move=restoring?Dex.moves.get(original.id):Dex.moves.get(moves.get(id)||'');
    if(!move.exists||move.num<=0||move.isZ||move.isMax||move.pp<=0)return null;
    if(p.moveSlots[index].id===move.id) {
        if(id!==65535&&b.p1.activeRequest.active[0].moves[index]?.id!==move.id)return null;
        return {rollback(){}};
    }
    if(p.transformed||p.getLockedMove()||p.moveSlots.some((m,i)=>i!==index&&m.id===move.id))return null;
    const oldSlots=p.moveSlots.slice(),oldBase=p.baseMoveSlots.slice(),oldRequest=b.p1.activeRequest.active[0];
    const replacement=restoring?original:{move:move.name,id:move.id,pp:move.pp,maxpp:move.pp,target:move.target,
        disabled:false,disabledSource:'',used:false};
    p.moveSlots[index]=replacement;p.baseMoveSlots[index]=replacement;
    ppSlots(session);
    b.runEvent('DisableMove',p);
    for(const slot of p.moveSlots) {
        const active=b.dex.getActiveMove(slot.id);
        b.singleEvent('DisableMove',active,null,p);
        if(active.flags.cantusetwice&&p.lastMove?.id===slot.id)p.disableMove(slot.id);
    }
    b.p1.activeRequest.active[0]=p.getMoveRequestData();
    const result={rollback(){p.moveSlots=oldSlots;p.baseMoveSlots=oldBase;b.p1.activeRequest.active[0]=oldRequest;b.p1.clearChoice();}};
    if(b.p1.activeRequest.active[0].moves[index]?.id!==move.id){result.rollback();return null;}
    return result;
}
function legal(session) {
    const side = session.battle.p1;
    if (session.outcome || side.activeRequest?.forceSwitch) return [];
    const list = side.activeRequest?.active?.[0]?.moves || [];
    return list.flatMap((m,i)=>!m.disabled && m.pp !== 0 ? [i+1] : []);
}
function ppSlots(session) {
    for (const p of session.battle.p1.pokemon) for (const slot of new Set([...p.moveSlots, ...p.baseMoveSlots])) {
        if (session.ppValues.has(slot)) continue;
        const value = {pp: slot.pp}; session.ppValues.set(slot, value);
        Object.defineProperty(slot, 'pp', {
            enumerable:true, configurable:true,
            get(){return session.infinitePp ? slot.maxpp : value.pp;},
            set(pp){if(!session.infinitePp)value.pp=pp;},
        });
    }
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
    ppSlots(s);
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
        else if(f[1]==='-curestatus') events.push([9,side,['brn','par','slp','frz','psn','tox'].indexOf(f[3])+1]);
        else if(f[1]==='-fail') events.push([10,side,Math.max(0,['brn','par','slp','frz','psn','tox'].indexOf(f[3])+1)]);
        else if(f[1]==='-immune') events.push([11,side,0]);
        else if(f[1]==='cant') {
            const reason=(f[3]||'').replace(/^move: /,'').toLowerCase().replace(/[^a-z0-9]/g,'');
            const reasons=['brn','par','slp','frz','psn','tox','flinch','recharge','taunt','confusion','attract','nopp','disable','truant','gravity','throatchop'];
            events.push([12,side,Math.max(0,reasons.indexOf(reason)+1)]);
        }
        else if(f[1]==='-block'||f[1]==='-activate'&&['move: Protect','move: Detect','move: Spiky Shield','move: Baneful Bunker','move: Obstruct','move: Silk Trap','move: Burning Bulwark','move: Quick Guard','move: Wide Guard','move: Crafty Shield','move: Mat Block'].includes(f[3])) {
            events.push([13,side,(f[3]||'').startsWith('move: ')?Math.max(0,Dex.moves.get(f[3].slice(6)).num):0]);
        }
        else if(f[1]==='-nothing') events.push([14,side,0]);
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
        if(!original.length)u8(1);else for(let i=0;i<original.length;i++){
            const base=s.originalSlots.get(p.name)[i];u8(base?Math.min(original[i][2],s.ppValues.get(base)?.pp??base.pp):0);}}
    u16(events.length);for(const e of events){u8(e[0]);u8(e[1]);u16(e[2]);}
    const available=legal(s);u8(available.length);available.forEach(u8);
    const options=b.p1.activeRequest?.active?.[0]?.moves||[];
    u8(options.length);
    for(let i=0;i<options.length;i++) {
        const move=options[i];
        u8(i+1);u16(Math.max(0,Dex.moves.get(move.id).num));u8(move.pp??1);u8(move.maxpp??1);
    }
    u8(Number(b.p2.active[0].name.slice(4)));
    u8(Math.max(0,['','brn','par','slp','frz','psn','tox'].indexOf(b.p2.active[0].status)));
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
    if(input.action==='infinitePp') {
        if(typeof input.enabled!=='boolean')throw Error('Invalid infinite PP setting');
        s.infinitePp=input.enabled;ppSlots(s);
        if(!b.ended)b.makeRequest();
        s.revision++;return snapshot(s,true,from);
    }
    if(input.action==='run') {if(!s.allowRun)return snapshot(s,false,from);s.outcome=4;s.revision++;return snapshot(s,true,from);}
    let choice,replacement;
    if(input.action==='move') {
        replacement=replaceMove(s,input);
        if(!replacement)return snapshot(s,false,from);
        if(!legal(s).includes(input.slot)){replacement.rollback();return snapshot(s,false,from);}
        choice=`move ${input.slot}`;
    } else if(input.action==='switch') {
        const index=b.p1.pokemon.findIndex(p=>p.name===`slot${input.slot}`);
        if(index<0||b.p1.pokemon[index].fainted||b.p1.pokemon[index].isActive)return snapshot(s,false,from);
        choice=`switch ${index+1}`;
    } else throw Error('Unknown operation');
    if(!b.choose('p1',choice)){replacement?.rollback();return snapshot(s,false,from);}
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
