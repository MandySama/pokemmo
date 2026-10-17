/* Branch choices describe a preview path; no game state is read or written. */
'use strict';
const D = kantoData, L = D.labels, $ = id => document.getElementById(id);
const key = m => `${m.group}/${m.map}`, maps = new Map(D.maps.map(m => [key(m), m]));
const kinds = {npcs: ['N', '--mint'], warps: ['W', '--warp'], 'coordinate-events': ['E', '--event'], background: ['B', '--back']};
let current = null, selected = null, route = [], range = [], choices = {}, sourceIndex = -1;
let selectedCell = null;
const axisFrame = el('div', undefined, 'axis-frame');
const xAxis = el('div', undefined, 'map-axis x-axis');
const yAxis = el('div', undefined, 'map-axis y-axis');
const axisCorner = el('div', 'Y / X', 'axis-corner');
xAxis.setAttribute('aria-label', 'X');
yAxis.setAttribute('aria-label', 'Y');
$('stage').before(axisFrame);
axisFrame.append(axisCorner, xAxis, yAxis, $('stage'));
let axisLayout = '';
function drawAxes(size) {
    const layout = `${current.columns}/${current.rows}/${size}`;
    if (layout === axisLayout) return;
    axisLayout = layout;
    axisFrame.style.width = (+current.columns * size + 30) + 'px';
    axisFrame.style.setProperty('--tile-size', size + 'px');
    xAxis.replaceChildren(); yAxis.replaceChildren();
    for (let x = 0; x < +current.columns; x++) xAxis.append(el('span', String(x)));
    for (let y = 0; y < +current.rows; y++) yAxis.append(el('span', String(y)));
}
const coordinateReadout = el('div', undefined, 'coordinate-readout');
coordinateReadout.id = 'coordinateReadout';
coordinateReadout.setAttribute('aria-live', 'polite');
$('mapCaption').before(coordinateReadout);
const cellHighlight = el('div', undefined, 'cell-highlight');
cellHighlight.hidden = true;
$('stage').append(cellHighlight);
function showCoordinate(x, y) {
    selectedCell = [x, y];
    updateCoordinate();
}
function locateEvent(event) {
    if (event?.x === undefined || event?.y === undefined) {
        selectedCell = null;
        updateCoordinate();
        return;
    }
    showCoordinate(+event.x, +event.y);
    const viewport = $('viewport'), cell = cellHighlight.getBoundingClientRect(), bounds = viewport.getBoundingClientRect();
    if (cell.left < bounds.left || cell.right > bounds.left + viewport.clientWidth
            || cell.top < bounds.top || cell.bottom > bounds.top + viewport.clientHeight) {
        viewport.scrollBy({left: cell.left + cell.width / 2 - bounds.left - viewport.clientWidth / 2,
            top: cell.top + cell.height / 2 - bounds.top - viewport.clientHeight / 2});
    }
}
function updateCoordinate() {
    coordinateReadout.textContent = selectedCell
        ? `${L.clickedCoordinate} X: ${selectedCell[0]} · Y: ${selectedCell[1]}  (${L.zeroBased})`
        : L.clickCoordinateHint;
    cellHighlight.hidden = !selectedCell;
    if (selectedCell) {
        const size = 16 * +$('zoom').value;
        cellHighlight.style.cssText = `left:${selectedCell[0] * size}px;top:${selectedCell[1] * size}px;width:${size}px;height:${size}px`;
    }
}
const enabled = new Set(Object.keys(kinds));
const hex = n => '0x' + n.toString(16).toUpperCase().padStart(6, '0');
const u16 = (b, n) => b[n] | b[n + 1] << 8;
const ptr = (b, n) => (b[n] | b[n + 1] << 8 | b[n + 2] << 16 | b[n + 3] << 24) - 0x08000000;
function el(tag, text, className) {const n = document.createElement(tag); if (text !== undefined) n.textContent = text; if (className) n.className = className; return n;}
function para(parent, text, className) {parent.append(el('p', text, className));}
function button(parent, text, action, className) {const n = el('button', text, className); n.type = 'button'; n.onclick = action; parent.append(n); return n;}
function heading(parent, text) {parent.append(el('h3', text));}
function sources() {return ['npcs', 'coordinate-events', 'background', 'map-scripts'].flatMap(kind => current[kind].map(e => ({kind, e}))).filter(s => parseInt(s.e.script_offset, 16) > 0);}
function sourceLabel(s) {return `${L[s.kind]} #${s.e.id ?? s.e.index}` + (s.kind === 'map-scripts' ? ` · ${hex(+s.e.variable)} = ${s.e.value}` : '');}
function ownActor(b, op, s) {
    let id = u16(b, 1);
    if (id === 0x800f && s.kind === 'npcs') id = +s.e.id;
    const offset = op === 0x50 ? 7 : [0x54, 0x56, 0x58, 0x59].includes(op) ? 3 : -1;
    if (offset >= 0 && (b[offset] !== +current.map || b[offset + 1] !== +current.group)) return -1;
    return id;
}
function targets(s, npc) {
    const todo = [parseInt(s.e.script_offset, 16)], seen = new Set();
    while (todo.length && seen.size < 10000) {
        const pc = todo.pop(), b = D.code[pc];
        if (!b || seen.has(pc)) continue;
        seen.add(pc); const op = b[0];
        if ([0x4f, 0x50, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x63, 0x65].includes(op) && ownActor(b, op, s) === +npc.id) return true;
        if (+npc.flag && [0x29, 0x2a].includes(op) && u16(b, 1) === +npc.flag) return true;
        if (op >= 0x39 && op <= 0x3f) continue;
        if ([4, 5].includes(op)) todo.push(ptr(b, 1));
        if ([6, 7].includes(op)) todo.push(ptr(b, 2));
        if ([8, 9, 10, 11].includes(op)) todo.push(D.standards[b[op >= 10 ? 2 : 1]]);
        if (op === 0x5c && [1, 2, 6, 8].includes(b[1])) todo.push(ptr(b, [1, 2].includes(b[1]) ? 14 : 18));
        if (![2, 3, 5, 8, 0x0c, 0x0d].includes(op)) todo.push(pc + b.length);
    }
    return false;
}
function trace(s, npc) {
    const result = {cells: npc ? [[+npc.x, +npc.y]] : [], texts: [], branches: [], actions: [], warnings: [], visible: npc ? !+npc.flag : true};
    let pc = parseInt(s.e.script_offset, 16), x = +(npc?.x || 0), y = +(npc?.y || 0), facing = null, compare = L.unknownCondition, comparison = null, condition = null;
    const variables = new Map(), flags = new Map();
    const meanings = new Map();
    const stack = [], visits = {}, occurrences = {};
    const warn = text => {if (!result.warnings.includes(text)) result.warnings.push(text);};
    const addText = at => {if (at <= 0) return; const text = D.texts[at] || L.missingText; if (result.texts.at(-1)?.at !== at) result.texts.push({at, text});};
    for (let count = 0; pc > 0 && count < 1800; count++) {
        if ((visits[pc] = (visits[pc] || 0) + 1) > 4) {warn(L.loopLimit); break;}
        const b = D.code[pc];
        if (!b) {warn(L.missingCode + ' ' + hex(pc)); break;}
        const op = b[0], next = pc + b.length;
        if (op === 2 || op === 0x0c || op === 0x0d) break;
        if (op === 3) {pc = stack.pop() || 0; continue;}
        if (op >= 0x39 && op <= 0x3f) {warn(L.mapChanged); break;}
        if (op === 0x16) variables.set(u16(b, 1), u16(b, 3));
        if (op === 0x19) {variables.set(u16(b, 1), variables.get(u16(b, 3)));meanings.set(u16(b,1),meanings.get(u16(b,3)));}
        if ([0x17, 0x18].includes(op)) {const id=u16(b,1);variables.set(id,variables.has(id)?(variables.get(id)+(op===0x17?1:-1)*u16(b,3))&65535:undefined);}
        if ([0x29, 0x2a].includes(op)) flags.set(u16(b,1),op===0x29);
        if (op === 0x2b) {compare = `${L.flag} ${hex(u16(b, 1))}`;condition={kind:'flag',id:u16(b,1)};comparison=flags.has(u16(b,1))?(flags.get(u16(b,1))?0:-1):null;}
        else if (op === 0x21 || op === 0x22) {
            compare = `${L.variable} ${hex(u16(b, 1))} / ${hex(u16(b, 3))}`;
            condition={kind:'variable',id:u16(b,1),value:u16(b,3),variable:op===0x22,meaning:meanings.get(u16(b,1))};
            const left=variables.get(u16(b,1)),right=op===0x21?u16(b,3):variables.get(u16(b,3));
            comparison=left===undefined||right===undefined?null:Math.sign(left-right);
        } else if ([0x25, 0x26, 0x5c].includes(op)) {compare = L.unknownCondition;comparison=null;condition=null;variables.clear();warn(L.specialNotice);}
        if([0x25,0x26].includes(op))meanings.set(op===0x26?u16(b,1):0x800d,L.resultSpecial);
        if(op===0x6e)meanings.set(0x800d,L.resultYesNo);
        if([0x6f,0x70,0x71].includes(op))meanings.set(0x800d,L.resultMenu);
        if(op===0x46)meanings.set(0x800d,L.resultSpace);
        if(op===0x47)meanings.set(0x800d,L.resultItem);
        if(op===0x92)meanings.set(0x800d,L.resultMoney);
        if(op===0x60){condition={kind:'check',meaning:L.resultTrainer};comparison=null;compare=L.resultTrainer;}
        if (op === 0x0f || op === 0x85) addText(ptr(b, 2));
        if (op === 0x67) addText(ptr(b, 1));
        if (op === 0x5c && b[1] !== 3) {addText(ptr(b, 6)); addText(ptr(b, 10));}
        if (npc && [0x29, 0x2a].includes(op) && +npc.flag && u16(b, 1) === +npc.flag) {
            result.visible = op === 0x2a;
            result.actions.push(`${hex(pc)} · ${result.visible ? L.appears : L.disappears} · ${L.flag} ${hex(+npc.flag)}`);
        }
        if (npc && ownActor(b, op, s) === +npc.id) {
            if ([0x57, 0x63].includes(op)) {
                x = u16(b, 3); y = u16(b, 5); result.cells.push([x, y]);
                result.actions.push(`${hex(pc)} · ${L.reposition} (${x}, ${y})`);
            } else if ([0x53, 0x54, 0x55, 0x56, 0x58, 0x59].includes(op)) {
                result.visible = [0x55, 0x56, 0x58].includes(op);
                result.actions.push(`${hex(pc)} · ${result.visible ? L.appears : L.disappears}`);
            } else if (op === 0x65) result.actions.push(`${hex(pc)} · ${L.activityChange} ${b[3]}`);
            else if (op === 0x4f || op === 0x50) {
                const sequence = D.moves[ptr(b, 3)];
                if (!sequence) {warn(L.missingMovement); break;}
                const names = [];
                for (const move of sequence) {
                    if (move === 254) break;
                    const name = D.movementNames[move]; names.push(name || hex(move));
                    if (!name) {warn(L.unknownMovement); return result;}
                    if (name === 'SET_INVISIBLE') result.visible = false;
                    if (name === 'SET_VISIBLE') result.visible = true;
                    const direction = name.match(/_(DOWN|UP|LEFT|RIGHT)$/)?.[1];
                    if (direction) facing = direction;
                    const walking = /^(WALK_|PLAYER_RUN_)/.test(name) && !name.startsWith('WALK_IN_PLACE');
                    const jumping = name.startsWith('JUMP') && !name.includes('IN_PLACE') && !name.includes('SPECIAL');
                    if (walking || jumping) {
                        const dir = direction || facing;
                        if (!dir) {warn(L.unknownMovement); return result;}
                        const distance = name.includes('JUMP_2') ? 2 : 1;
                        x += (dir === 'LEFT' ? -1 : dir === 'RIGHT' ? 1 : 0) * distance;
                        y += (dir === 'UP' ? -1 : dir === 'DOWN' ? 1 : 0) * distance;
                        result.cells.push([x, y]);
                    } else if (!/^(FACE_|WALK_IN_PLACE|DELAY_|LOCK_|UNLOCK_|SET_|RESTORE_|ENABLE_|DISABLE_|EMOTE_|JUMP_IN_PLACE|AFFINE_|HIDE_|SHOW_|PAUSE_)/.test(name)) warn(L.specialNotice);
                }
                result.actions.push(`${hex(pc)} · ${names.join(' → ')}`);
            }
        }
        if ([6, 7, 10, 11].includes(op)) {
            const occurrence = occurrences[pc] = (occurrences[pc] || 0) + 1, id = `${pc}:${occurrence}`;
            const known = comparison === null ? false : [comparison<0,comparison===0,comparison>0,comparison<=0,comparison>=0,comparison!==0][b[1]];
            let take = Object.hasOwn(choices,id) ? choices[id] : known;
            if(signContext(s)&&condition?.id===0x800c&&condition.value===4&&b[1]===5){
                const opposite=result.branches.findLast(v=>v.condition?.id===0x800c&&v.condition.value===4&&v.operator===1);
                if(opposite)take=!opposite.take;
            }
            result.branches.push({id, text: `${hex(pc)} · ${compare} · ${['<', '=', '>', '≤', '≥', '≠'][b[1]] || b[1]}`, take, condition, operator:b[1]});
            if (take) {
                if (op === 7 || op === 11) stack.push(next);
                pc = op < 10 ? ptr(b, 2) : D.standards[b[2]];
            } else pc = next;
            continue;
        }
        if ([4, 5, 8, 9].includes(op)) {
            if (op === 4 || op === 9) stack.push(next);
            pc = op < 8 ? ptr(b, 1) : D.standards[b[1]];
            if (!pc) warn(L.missingCode);
            continue;
        }
        pc = next;
        if (count === 1799) warn(L.loopLimit);
    }
    return result;
}
function daily(npc) {
    const t = +npc.movement;
    const moving = t >= 2 && t <= 6 || t >= 0x19 && t <= 0x38 || t >= 0x3b && t <= 0x3e || t === 0x50;
    let rx = moving ? Math.max(1, +npc.range_x) : 0, ry = moving ? Math.max(1, +npc.range_y) : 0;
    if ([3, 4, 0x19, 0x1a].includes(t)) rx = 0;
    if ([5, 6, 0x1b, 0x1c].includes(t)) ry = 0;
    const cells = [];
    for (let y = +npc.y - ry; y <= +npc.y + ry; y++) for (let x = +npc.x - rx; x <= +npc.x + rx; x++) {
        if (x >= 0 && y >= 0 && x < +current.columns && y < +current.rows) cells.push([x, y]);
    }
    return cells;
}
function draw() {
    updateCoordinate();
    const size = 16 * +$('zoom').value;
    $('stage').style.width = +current.columns * size + 'px';
    $('stage').style.height = +current.rows * size + 'px';
    drawAxes(size);
    $('grid').style.backgroundSize = `${size}px ${size}px`; $('grid').hidden = !$('gridToggle').checked;
    $('route').setAttribute('viewBox', `0 0 ${+current.columns * 16} ${+current.rows * 16}`); $('route').replaceChildren();
    function svg(name, attrs, text) {const n = document.createElementNS('http://www.w3.org/2000/svg', name); for (const [k,v] of Object.entries(attrs)) n.setAttribute(k,v); if (text) n.textContent=text; $('route').append(n);}
    for (const [x,y] of range) svg('rect', {x:x*16,y:y*16,width:16,height:16,class:'range-cell'});
    const visited = new Set();
    route.forEach(([x,y], i) => {const k=`${x},${y}`; if(visited.has(k))return; visited.add(k); svg('rect',{x:x*16,y:y*16,width:16,height:16,class:'route-cell'}); svg('text',{x:x*16+8,y:y*16+10,'text-anchor':'middle',class:'route-label'},String(i));});
    if (route.length) {const [x,y] = route.at(-1); svg('circle',{cx:x*16+12,cy:y*16+4,r:2.5,class:'route-end'});}
    $('markers').replaceChildren(); const stacks = {};
    for (const [kind,[letter,color]] of Object.entries(kinds)) {
        if (!enabled.has(kind)) continue;
        for (const event of current[kind]) {
            const pos = `${event.x},${event.y}`, offset = (stacks[pos] || 0) * 5; stacks[pos] = (stacks[pos] || 0) + 1;
            const b = button($('markers'), letter, () => select(kind,event), 'marker' + (selected?.e === event ? ' active' : '') + (+event.flag ? ' conditional' : ''));
            b.style.cssText = `--color:var(${color});left:${+event.x*size+offset}px;top:${+event.y*size+offset}px;width:${size}px;height:${size}px`;
            b.title = b.ariaLabel = `${L[kind]} #${event.id ?? event.index} (${event.x}, ${event.y})`;
            b.dataset.kind = kind; b.dataset.index = event.index;
        }
    }
}
function dialogue(parent, texts) {
    heading(parent,L.dialogue);
    if (!texts.length) para(parent,L.noDialogue);
    texts.forEach(t => parent.append(el('div',t.text.replace(/\\[np]/g,'\n'),'dialogue')));
}
function activityLabel(npc) {
    const t=+npc.movement;
    if(t>=2&&t<=6||t===0x50)return L.randomWalk;
    if(t>=0x19&&t<=0x34)return L.patrolWalk;
    if(t>=0x35&&t<=0x38||t>=0x3b&&t<=0x3e)return L.copyWalk;
    if(t===1||t>=0xd&&t<=0x18)return L.turnInPlace;
    if(t===0||t>=7&&t<=12)return L.stays;
    return L.specialActivity;
}
function friendlySource(s,npc) {
    const scene=D.details[parseInt(s.e.script_offset,16)]?.scenes?.[0]?.name;
    let label=s.kind==='npcs'?(s.e===npc?L.talkSelf:L.talkOther+' #'+s.e.id)
        :s.kind==='coordinate-events'?`${L.walkTrigger} (${s.e.x}, ${s.e.y})`
        :s.kind==='background'?`${L.interactTrigger} (${s.e.x}, ${s.e.y})`
        :L.mapAuto+' #'+s.e.index;
    return scene?label+' · '+scene:label;
}
function signContext(source) {return key(current)==='3/0'&&parseInt(source?.e.script_offset,16)===0x16575c;}
function friendlyBranch(b,i,source) {
    const c=b.condition;
    // These meanings follow PalletTown's native script and its constant definitions.
    if(c?.kind==='variable'&&!c.variable&&c.id===0x4070&&b.operator===1) {
        const text=[L.signBefore,L.signJustShown,L.signFinished][c.value];
        if(text)return text;
    }
    if(key(current)==='3/0'&&c?.kind==='variable'&&c.id===0x4002&&c.value===1&&b.operator===1)return L.signReady;
    if(c?.kind==='flag'&&c.id===0x291&&b.operator===1)return L.signMoved;
    if(signContext(source)){
        if(c?.kind==='flag'&&c.id===2&&b.operator===1)return L.signAlreadyYielded;
        if(c?.kind==='variable'&&c.id===0x800c&&c.value===4&&!c.variable){
            if(b.operator===1)return L.playerFacingRight;
            if(b.operator===5)return L.playerNotFacingRight;
        }
    }
    return semanticBranch(b);
}
function branchExplanation(b,source){
    if(!signContext(source))return '';
    const c=b.condition;
    if(c?.kind==='flag'&&c.id===2&&b.operator===1)return L.signYieldExplanation;
    if(c?.kind==='variable'&&c.id===0x800c&&c.value===4&&!c.variable){
        if(b.operator===1)return L.signStepRight;
        if(b.operator===5)return L.signStepLeft;
    }
    if(c?.id===0x4070&&c.value===2&&b.operator===1)return L.signFinishedExplanation;
    if(c?.id===0x4070&&c.value===1&&b.operator===1)return L.signJustExplanation;
    if(c?.id===0x4002&&c.value===1&&b.operator===1)return L.signReadyExplanation;
    return '';
}
function technical(parent,e,source,result,info) {
    const box=el('details',undefined,'technical'), summary=el('summary',L.technical);
    box.append(summary);
    para(box,`${L.mapId} ${key(current)} · ${L.source} ${source?.e.script_offset||e.script_offset||'—'}`,'source');
    if(e.movement!==undefined)para(box,`${L.activity} ${e.movement_name||e.movement} · ${L.flag} ${hex(+e.flag)}`,'source');
    if(source?.e.variable!==undefined)para(box,`${L.triggerCondition} ${hex(+source.e.variable)} = ${source.e.value}`,'source');
    para(box,L.branchNotice,'source');
    if(result){para(box,L.originNotice,'source');result.branches.forEach(b=>para(box,b.text,'source'));result.actions.forEach(a=>para(box,a,'source'));result.warnings.forEach(w=>para(box,w,'source'));}
    (result?.texts||info?.texts||[]).forEach(t=>para(box,hex(t.at??parseInt(t.address,16))+' · '+t.text.slice(0,35),'source'));
    if(info?.missing?.length)para(box,L.missingCode+' '+info.missing.join(', '),'source');
    parent.append(box);
}
function inspect() {
    const panel = $('detail'); panel.replaceChildren(); route=[]; range=[];
    if (!selected) {para(panel,L.selectHint); draw(); return;}
    const {kind,e} = selected;
    heading(panel, `${L[kind]} #${e.id ?? e.index}`);
    if (e.x !== undefined) para(panel,`${L.position} (${e.x}, ${e.y})`, 'source');
    if (kind === 'warps') {
        const dest = maps.get(`${e.target_group}/${e.target_map}`);
        if (e.dynamic_target !== 'false' || !dest) para(panel,L.dynamicWarp);
        else {para(panel,`${L.destination} ${dest.name} · ${key(dest)}`); button(panel,L.enterMap,()=>{showMap(dest); const w=dest.warps.find(w=>+w.index===+e.target_warp); if(w){select('warps',w); const marker=$('markers').querySelector('.active'); marker?.scrollIntoView({block:'center',inline:'center'});}});}
        draw(); return;
    }
    if (kind === 'background' && e.item) para(panel,`${L.hiddenItem} #${e.item}`);
    const npc = kind === 'npcs' ? e : null;
    const related = npc ? sources().filter(s => s.e === e || targets(s,e)) : [{kind,e}];
    if(key(current)==='3/0'&&(npc&&+npc.id===1||parseInt(e.script_offset,16)===0x16587b)){
        const guide=el('section',undefined,'story-summary unlock-guide');
        heading(guide,L.unlockTitle);
        for(const [title,body] of [[L.unlockEarlyTitle,L.unlockEarly],[L.unlockReadyTitle,L.unlockReady],[L.unlockWaysTitle,L.unlockWays],[L.unlockAfterTitle,L.unlockAfter]]){
            heading(guide,title);para(guide,body);
        }
        para(guide,L.unlockSkip,'notice');
        panel.append(guide);
    }
    if (npc) {
        para(panel,activityLabel(e),'activity-summary');
        if (+e.flag) para(panel,L.conditionalPerson,'source');
        const label = el('label',L.scenario), chooser=el('select'); chooser.id='scenario'; label.append(chooser); panel.append(label);
        const base=el('option',L.daily); base.value=-1; chooser.append(base);
        related.forEach((s,i)=>{const o=el('option',friendlySource(s,e));o.value=i;chooser.append(o);});
        chooser.value=sourceIndex;
        chooser.onchange=()=>{sourceIndex=+chooser.value;choices={};inspect();locateEvent(sourceIndex<0?e:related[sourceIndex]?.e);};
        if (sourceIndex === -1) {
            range = daily(e); heading(panel,L.range);
            if(range.length>1) {
                const xs=range.map(p=>p[0]),ys=range.map(p=>p[1]);
                para(panel,`${L.horizontal} ${Math.max(...xs)-Math.min(...xs)+1} ${L.tiles} × ${L.vertical} ${Math.max(...ys)-Math.min(...ys)+1} ${L.tiles}`,'range-summary');
                para(panel,L.boundaryNotice,'source');
            } else para(panel,L.noDailyWalk);
            const info=D.details[parseInt(e.script_offset,16)];
            dialogueContexts(panel,related);
            technical(panel,e,null,null,info);
            draw();return;
        }
    }
    const source = related[sourceIndex < 0 ? 0 : sourceIndex];
    if (!source || !parseInt(source.e.script_offset,16)) {para(panel,L.noScript);draw();return;}
    const info=D.details[parseInt(source.e.script_offset,16)];
    semanticGuide(panel,source);
    if (!npc) para(panel,friendlySource(source,null),'source');
    if(signContext(source)){const story=el('section',undefined,'story-summary');heading(story,L.signStoryTitle);para(story,L.signStorySummary);panel.append(story);}
    const result=trace(source,npc);route=result.cells;
    if(result.branches.length) {
        heading(panel,L.branch);
        para(panel,L.conditionHint,'source');
        result.branches.forEach((b,i)=>{const label=el('label',undefined,'condition'),words=el('span'),input=el('select');words.append(el('span',friendlyBranch(b,i,source)));const explanation=branchExplanation(b,source);if(explanation)words.append(el('small',explanation,'branch-explanation'));label.append(words);input.className='branch-choice';input.dataset.branch=b.id;
            [L.notSatisfied,L.satisfied].forEach((text,i)=>{const o=el('option',text);o.value=i;input.append(o);});input.value=b.take?1:0;
            input.onchange=()=>{choices[b.id]=!!+input.value;
                if(signContext(source)&&b.condition?.id===0x800c&&b.condition.value===4){
                    for(const other of result.branches)if(other.condition?.id===0x800c&&other.condition.value===4&&other.operator!==b.operator)choices[other.id]=!choices[b.id];
                }
                inspect();panel.querySelector(`[data-branch="${b.id}"]`)?.focus({preventScroll:true});};label.append(input);panel.append(label);});
    }
    if(npc){const moved=route.some(p=>p[0]!==route[0][0]||p[1]!==route[0][1]);para(panel,(moved?`${L.pathSummary} (${route.at(-1).join(', ')})`:L.noMovement)+' · '+(result.visible?L.remainsVisible:L.endsHidden),'result-summary');}
    dialogue(panel,result.texts);
    if(result.warnings.some(w=>w!==L.mapChanged&&w!==L.specialNotice))para(panel,L.incompletePreview,'notice');
    para(panel,L.previewHint,'source');
    technical(panel,e,source,result,info);
    draw();
}
function select(kind,e) {selected={kind,e};sourceIndex=-1;choices={};if(e.x!==undefined&&e.y!==undefined)showCoordinate(+e.x,+e.y);inspect();}
function showMap(m) {
    current=m;selected=null;selectedCell=null;route=[];range=[];choices={};sourceIndex=-1;
    $('overview').hidden=true;$('mapView').hidden=false;$('mapName').textContent=m.name;$('mapKey').textContent=`KANTO / ${key(m)}`;
    $('mapSelect').value=key(m);$('mapImage').src=`images/${m.group}-${m.map}.png`;$('mapImage').alt=m.name;
    $('mapCaption').textContent=`${m.columns} × ${m.rows} · ${L.mapCaption}`;
    $('connections').replaceChildren();m.connections.forEach(c=>{const dest=maps.get(`${c.target_group}/${c.target_map}`);if(dest)button($('connections'),`${L.connection} → ${dest.name}`,()=>showMap(dest));});
    $('eventList').replaceChildren();
    for(const kind of [...Object.keys(kinds),'map-scripts']) for(const e of m[kind])button($('eventList'),`${L[kind]} #${e.id??e.index}`,()=>select(kind,e));
    $('viewport').scrollTop=0;$('viewport').scrollLeft=0;inspect();
}
function home() {$('overview').hidden=false;$('mapView').hidden=true;}
document.querySelectorAll('[data-text]').forEach(n=>n.textContent=L[n.dataset.text]);document.title=L.title;
$('regionImage').alt=L.overview;$('clear').ariaLabel=L.close;
const citySections=new Set();
const cities=D.maps.filter(m=>{if(+m.group!==3 || +m.section<88 || +m.section>98 || citySections.has(m.section))return false;citySections.add(m.section);return true;});
cities.forEach((m,i)=>{const position=D.positions[m.section];if(!position)return;
    const name=m.name.replace(/\(.*/,''), b=button($('cities'),String(i+1).padStart(2,'0'),()=>showMap(m),'city-dot');b.title=b.ariaLabel=name;b.style.left=position[0]/192*100+'%';b.style.top=position[1]/144*100+'%';
    const row=button($('cityList'),`${String(i+1).padStart(2,'0')}  ${name}`,()=>showMap(m),'city-row');row.append(el('span',`${m.npcs.length} NPC →`));});
for(const m of D.maps){const o=el('option',`${key(m)} · ${m.name}`);o.value=key(m);$('mapSelect').append(o);}
for(const [kind,[letter,color]] of Object.entries(kinds)){const label=el('label'),input=el('input');input.type='checkbox';input.checked=true;input.onchange=()=>{if(input.checked)enabled.add(kind);else enabled.delete(kind);draw();};label.style.setProperty('--color',`var(${color})`);label.append(input,el('span',`${letter} ${L[kind]}`));$('filters').append(label);}
$('mapSelect').onchange=()=>showMap(maps.get($('mapSelect').value));$('home').onclick=home;
$('stage').addEventListener('click', event => {
    if (!current || event.target.closest('.marker')) return;
    const bounds = $('stage').getBoundingClientRect();
    const x = Math.floor((event.clientX - bounds.left) / bounds.width * +current.columns);
    const y = Math.floor((event.clientY - bounds.top) / bounds.height * +current.rows);
    if (x >= 0 && y >= 0 && x < +current.columns && y < +current.rows) showCoordinate(x, y);
});
$('zoom').onchange=draw;$('gridToggle').onchange=draw;$('clear').onclick=()=>{selected=null;inspect();};
document.addEventListener('keydown',e=>{if(e.key==='Escape' && current && !$('mapView').hidden){selected=null;inspect();}});
home();
