/* Read-only native data plus a separate presentation overlay. No game APIs are used. */
const $ = id => document.getElementById(id), data = gbaRelations;
const t = key => data.strings[key] || key;
const allEdges = new Map(data.edges.map(e => [e.id,e])), outgoing = new Map(), incoming = new Map();
for (const e of data.edges) {
    if (!outgoing.has(e.source)) outgoing.set(e.source, []); outgoing.get(e.source).push(e);
    if (!incoming.has(e.target)) incoming.set(e.target, []); incoming.get(e.target).push(e);
}
const params = new URLSearchParams(location.search);
const storageKey = 'gba-view:' + data.sourceHash;
const fresh = () => ({version:1,sourceHash:data.sourceHash,map:'3/0',scope:'map:3/0',hidden:[],redirects:{},positions:{},notes:{},groups:{},limit:80,reverse:false});
let view = fresh(), selected = null, selectedEdge = null, graph = [], graphEdges = [], history = [], future = [], storageAvailable = true;
function el(tag,text) { const n = document.createElement(tag); if (text !== undefined) n.textContent=text; return n; }
function button(text, action) { const b=el('button',text); b.onclick=action; return b; }
function status(text,error=false) { $('status').textContent=text; $('status').className=error?'error':''; }
function snapshot() { return JSON.stringify(view); }
function remember() { history.push(snapshot()); if(history.length>50) history.shift(); future=[]; }
function persist() {
    try { localStorage.setItem(storageKey,snapshot()); } catch { storageAvailable=false; }
    $('undo').disabled=!history.length; $('redo').disabled=!future.length;
}
function mutate(action) { remember(); action(); persist(); render(); status(t(storageAvailable?'draftSaved':'storageUnavailable')); }
function validate(input) {
    if (!input || input.version!==1 || input.sourceHash!==data.sourceHash) throw new Error(t('sourceMismatch'));
    if (!(input.map in data.maps) || !Object.hasOwn(data.nodes,input.scope)) throw new Error(t('invalidView'));
    const result=fresh(); result.map=input.map; result.scope=input.scope;
    if (!Array.isArray(input.hidden) || input.hidden.length>Object.keys(data.nodes).length) throw new Error(t('invalidView'));
    result.hidden=[...new Set(input.hidden)];
    for(const id of result.hidden) if(!Object.hasOwn(data.nodes,id)) throw new Error(t('invalidView'));
    for(const key of ['redirects','positions','notes','groups']) {
        if (!input[key] || typeof input[key]!=='object' || Array.isArray(input[key])) throw new Error(t('invalidView'));
        result[key]=Object.create(null);
        for(const [id,value] of Object.entries(input[key])) {
            if(key==='redirects') {
                if(!allEdges.has(id)||!Object.hasOwn(data.nodes,value)) throw new Error(t('invalidView'));
            } else {
                if(!Object.hasOwn(data.nodes,id)) throw new Error(t('invalidView'));
                if(key==='positions') {
                    if(!value || !Number.isFinite(value.x)||!Number.isFinite(value.y)||value.x<0||value.y<0||value.x>100000||value.y>100000) throw new Error(t('invalidView'));
                } else if(typeof value!=='string'||value.length>4000) throw new Error(t('invalidView'));
            }
            result[key][id]=value;
        }
    }
    result.limit=Number.isInteger(input.limit)?Math.max(20,Math.min(1000,input.limit)):80;
    result.reverse=input.reverse===true;
    return result;
}
try { const saved=localStorage.getItem(storageKey); if(saved) view=validate(JSON.parse(saved)); } catch { storageAvailable=false; }
if(params.get('map') in data.maps) { view.map=params.get('map'); view.scope='map:'+view.map; }
if(Object.hasOwn(data.nodes,params.get('scope'))) view.scope=params.get('scope');
document.title=t('title'); document.querySelectorAll('[data-t]').forEach(n=>n.textContent=t(n.dataset.t));
for(const key of Object.keys(data.maps)) { const option=el('option',data.nodes['map:'+key].title); option.value=key; $('map').append(option); }
for(const kind of ['all',...new Set(Object.values(data.nodes).map(n=>n.kind))]) { const option=el('option',t(kind)); option.value=kind; $('kind').append(option); }
$('coverage').textContent=t('coverage')+' '+data.coverage.maps+' / '+data.coverage.instructions+' · SHA-256 '+data.sourceHash.slice(0,12)+(data.diagnostics.length?' · '+t('incomplete')+' '+data.diagnostics.length:'');
function setScope(id) {
    selected=id; selectedEdge=null; view.scope=id;
    if(data.nodes[id].map) view.map=data.nodes[id].map;
    view.limit=80; persist(); render(); $('viewport').scrollTo(0,0);
}
function sourceList() {
    $('sourceList').replaceChildren();
    const query=$('search').value.trim().toLowerCase(), kind=$('kind').value;
    const local = new Set(data.maps[view.map]); let count=0;
    const candidates=query?Object.values(data.nodes):[...local].map(id=>data.nodes[id]);
    for(const n of candidates) {
        if(kind!=='all'&&n.kind!==kind) continue;
        if(query&&!(n.id+' '+n.title+' '+n.detail.join(' ')).toLowerCase().includes(query)) continue;
        if(count++>=100) break;
        $('sourceList').append(button(t(n.kind)+' · '+n.title,()=>setScope(n.id)));
    }
    if(count>100) $('sourceList').append(el('small',t('narrowSearch')));
}
function mapPreview() {
    $('map').value=view.map; $('mapImage').src='images/'+view.map.replace('/','-')+'.png';
    $('mapImage').alt=data.nodes['map:'+view.map].title; $('mapMarkers').replaceChildren();
    const map=data.nodes['map:'+view.map], [width,height]=map.dimensions;
    for(const id of data.maps[view.map]) {
        const n=data.nodes[id], r=n.record;
        if(!r||r.x===undefined) continue;
        const b=button('',()=>setScope(id)); b.title=t(n.kind)+' · '+n.title;
        b.className='map-marker '+n.kind; b.style.left=((+r.x+.5)/width*100)+'%'; b.style.top=((+r.y+.5)/height*100)+'%';
        $('mapMarkers').append(b);
    }
}
function buildGraph() {
    const hidden=new Set(view.hidden), seen=new Set(), pending=[[view.scope,0]], depths=new Map();
    graph=[]; let index=0;
    while(index<pending.length&&graph.length<view.limit) {
        const [id,depth]=pending[index++]; if(seen.has(id)||hidden.has(id)||!data.nodes[id]) continue;
        seen.add(id); depths.set(id,depth); graph.push(id);
        for(const e of outgoing.get(id)||[]) pending.push([view.redirects[e.id]||e.target,depth+1]);
        if(view.reverse) {
            for(const e of incoming.get(id)||[]) if((view.redirects[e.id]||e.target)===id)pending.push([e.source,depth+1]);
            for(const [edge,target] of Object.entries(view.redirects))if(target===id)pending.push([allEdges.get(edge).source,depth+1]);
        }
    }
    graphEdges=data.edges.filter(e=>seen.has(e.source)&&seen.has(view.redirects[e.id]||e.target));
    const rows=new Map();
    for(const id of graph) { const depth=depths.get(id), row=rows.get(depth)||0; rows.set(depth,row+1);
        if(!view.positions[id]) view.positions[id]={x:35+depth*315,y:35+row*140}; }
    $('more').disabled=index>=pending.length||view.limit>=1000;
    $('graphCount').textContent=graph.length+' '+t('nodes')+' · '+graphEdges.length+' '+t('edges')+(index<pending.length?' · '+t('partial'):'');
}
const colors={map:'#85bded',npc:'#72d5a4',warp:'#69b8ed',coordinate:'#e7c46b',background:'#c99be7',weather:'#e7c46b',script:'#89acdd',text:'#dba0a0',flag:'#e6bd6c',variable:'#e6bd6c',movement:'#9bcea3',unknown:'#e08080'};
function renderGraph() {
    buildGraph(); $('graphNodes').replaceChildren();
    let width=900,height=700;
    for(const id of graph) {
        const n=data.nodes[id], p=view.positions[id], box=el('div'); box.className='node'+(id===selected?' selected':'')+(view.groups[id]?' grouped':''); box.dataset.id=id;
        box.style.left=p.x+'px'; box.style.top=p.y+'px'; box.style.setProperty('--node-color',colors[n.kind]||'#aaa');
        if(view.groups[id]) { let hash=0; for(const c of view.groups[id]) hash=(hash*31+c.charCodeAt(0))|0; box.style.setProperty('--group-color','hsl('+Math.abs(hash%360)+' 60% 65%)'); }
        box.append(el('small',t(n.kind)),el('strong',n.title));
        const excerpt=el('div',(view.notes[id]||n.detail[0]||'').replaceAll('\\n',' ')); excerpt.className='excerpt'; box.append(excerpt);
        if(view.groups[id]) { const badge=el('small',view.groups[id]); badge.className='badge'; box.append(badge); }
        box.onpointerdown=event=>drag(event,id,box);
        box.onclick=()=>{selected=id;selectedEdge=null; renderGraph(); inspect();};
        box.ondblclick=()=>setScope(id);
        $('graphNodes').append(box); width=Math.max(width,p.x+320); height=Math.max(height,p.y+180);
    }
    $('canvas').style.width=width+'px'; $('canvas').style.height=height+'px';
    $('wires').setAttribute('width',width); $('wires').setAttribute('height',height);
    scale(width,height); wires();
}
function scale(width=parseFloat($('canvas').style.width),height=parseFloat($('canvas').style.height)) {
    const zoom=+$('zoom').value; $('canvas').style.transform='scale('+zoom+')'; $('sizer').style.width=width*zoom+'px'; $('sizer').style.height=height*zoom+'px';
}
function wires() {
    const svg=$('wires'), ns='http://www.w3.org/2000/svg'; svg.replaceChildren();
    const defs=document.createElementNS(ns,'defs'),marker=document.createElementNS(ns,'marker'),arrow=document.createElementNS(ns,'path');
    marker.id='arrow';marker.setAttribute('viewBox','0 0 10 10');marker.setAttribute('refX','9');marker.setAttribute('refY','5');
    marker.setAttribute('markerWidth','7');marker.setAttribute('markerHeight','7');marker.setAttribute('orient','auto-start-reverse');
    arrow.setAttribute('d','M 0 0 L 10 5 L 0 10 z');arrow.style.fill='#8da7bf';arrow.style.stroke='none';marker.append(arrow);defs.append(marker);svg.append(defs);
    for(const e of graphEdges) {
        const target=view.redirects[e.id]||e.target, a=view.positions[e.source], b=view.positions[target];
        if(!a||!b)continue;
        const x=a.x+245,y=a.y+48,tx=b.x,ty=b.y+48,curve=Math.max(45,Math.abs(tx-x)*.45);
        const path=document.createElementNS(ns,'path');
        path.setAttribute('d',`M ${x} ${y} C ${x+curve} ${y}, ${tx-curve} ${ty}, ${tx} ${ty}`);
        path.setAttribute('marker-end','url(#arrow)');
        path.classList.toggle('changed',target!==e.target); path.classList.toggle('selected',selectedEdge===e.id);
        path.dataset.edge=e.id; path.onclick=()=>{selected=e.source;selectedEdge=e.id;inspect();wires();};
        const title=document.createElementNS(ns,'title'); title.textContent=t(e.relation)+' → '+data.nodes[target].title;path.append(title);
        const text=document.createElementNS(ns,'text');text.setAttribute('x',(x+tx)/2);text.setAttribute('y',(y+ty)/2-6);text.textContent=t(e.relation)+(e.label!==e.relation?' '+e.label:'');
        svg.append(path,text);
    }
}
function drag(event,id,box) {
    if(event.button!==0)return; event.preventDefault();
    const old=snapshot(), start={x:event.clientX,y:event.clientY}, initial={...view.positions[id]}, zoom=+$('zoom').value;
    let moved=false; box.setPointerCapture(event.pointerId);
    box.onpointermove=e=>{
        const dx=(e.clientX-start.x)/zoom,dy=(e.clientY-start.y)/zoom;
        if(Math.abs(dx)+Math.abs(dy)<3&&!moved)return;moved=true;
        view.positions[id]={x:Math.max(0,initial.x+dx),y:Math.max(0,initial.y+dy)};
        box.style.left=view.positions[id].x+'px';box.style.top=view.positions[id].y+'px';wires();
    };
    box.onpointerup=()=>{box.onpointermove=null;box.onpointerup=null;
        if(moved){history.push(old);future=[];persist();renderGraph();status(t('draftSaved'));}
    };
}
function inspect() {
    $('selection').replaceChildren();$('links').replaceChildren();
    const n=data.nodes[selected];
    $('remove').disabled=!n; $('restoreNode').disabled=!n||!view.hidden.includes(selected);
    $('note').disabled=!n; $('group').disabled=!n;
    $('note').value=n?view.notes[selected]||'':'';$('group').value=n?view.groups[selected]||'':'';
    if(!n){$('raw').textContent='';return;}
    $('selection').append(el('strong',n.title),el('p',n.id));
    if(view.hidden.includes(n.id))$('selection').append(el('small',t('hiddenNode')));
    $('raw').textContent=n.detail.join('\n').replaceAll('\\n','\n');
    const links=[...(outgoing.get(selected)||[]),...(incoming.get(selected)||[])];
    for(const e of links.slice(0,150)) {
        const row=el('div');row.className='edge-row'; const target=view.redirects[e.id]||e.target;
        row.append(el('strong',t(e.relation)),el('div',e.source+' → '+target));
        if(target!==e.target)row.append(el('small',t('changedLink')+' '+e.target));
        row.append(button(t('locateSource'),()=>setScope(e.source)),button(t('locateTarget'),()=>setScope(target)));
        const input=el('input');input.value=target;input.setAttribute('aria-label',t('target'));input.placeholder=t('target');input.setAttribute('list','targetOptions');
        input.oninput=()=>targetSuggestions(input.value); row.append(input);
        row.append(button(t('redirect'),()=>{
            if(!Object.hasOwn(data.nodes,input.value)){status(t('invalidTarget'),true);return;}
            mutate(()=>{if(input.value===e.target)delete view.redirects[e.id];else view.redirects[e.id]=input.value;});
        }));
        if(target!==e.target)row.append(button(t('restoreLink'),()=>mutate(()=>delete view.redirects[e.id])));
        $('links').append(row);
    }
    if(links.length>150)$('links').append(el('p',t('manyReferences')));
}
const targetOptions=el('datalist');targetOptions.id='targetOptions';document.body.append(targetOptions);
function targetSuggestions(query='') {
    targetOptions.replaceChildren();let count=0;query=query.toLowerCase();
    for(const n of Object.values(data.nodes))if((n.id+' '+n.title).toLowerCase().includes(query)) {
        const o=el('option');o.value=n.id;o.label=n.title;targetOptions.append(o);if(++count>=60)break;
    }
}
function render() {
    $('reverse').checked=view.reverse;
    mapPreview();sourceList();$('scope').textContent=data.nodes[view.scope].title;renderGraph();inspect();
    $('hiddenList').replaceChildren();
    for(const id of view.hidden)$('hiddenList').append(button(t('restoreNode')+' · '+data.nodes[id].title,()=>mutate(()=>view.hidden=view.hidden.filter(n=>n!==id))));
    $('undo').disabled=!history.length;$('redo').disabled=!future.length;
}
$('map').onchange=()=>{view.map=$('map').value;setScope('map:'+view.map);};
$('mapScope').onclick=()=>setScope('map:'+view.map);
$('search').oninput=sourceList;$('kind').onchange=sourceList;$('zoom').onchange=()=>scale();
$('reverse').onchange=()=>mutate(()=>view.reverse=$('reverse').checked);
$('more').onclick=()=>{view.limit=Math.min(1000,view.limit+100);persist();renderGraph();};
$('layout').onclick=()=>mutate(()=>{for(const id of graph)delete view.positions[id];});
$('remove').onclick=()=>{if(selected)mutate(()=>{if(!view.hidden.includes(selected))view.hidden.push(selected);});};
$('restoreNode').onclick=()=>mutate(()=>view.hidden=view.hidden.filter(n=>n!==selected));
$('note').onchange=()=>{if(selected)mutate(()=>view.notes[selected]=$('note').value);};
$('group').onchange=()=>{if(selected)mutate(()=>view.groups[selected]=$('group').value);};
$('undo').onclick=()=>{if(!history.length)return;future.push(snapshot());view=validate(JSON.parse(history.pop()));persist();render();};
$('redo').onclick=()=>{if(!future.length)return;history.push(snapshot());view=validate(JSON.parse(future.pop()));persist();render();};
$('reset').onclick=()=>{if(confirm(t('resetConfirm')))mutate(()=>{const map=view.map,scope=view.scope;view=fresh();view.map=map;view.scope=scope;});};
$('save').onclick=()=>{
    persist();const blob=new Blob([JSON.stringify(view,null,2)],{type:'application/json'}),url=URL.createObjectURL(blob),a=el('a');
    a.href=url;a.download='gba-view-'+view.map.replace('/','-')+'.json';a.click();setTimeout(()=>URL.revokeObjectURL(url),1000);status(t('saved'));
};
$('import').onchange=async()=>{
    try{const file=$('import').files[0];if(!file)return;if(file.size>4*1024*1024)throw new Error(t('invalidView'));
        const candidate=validate(JSON.parse(await file.text()));remember();view=candidate;selected=null;persist();render();status(t('imported'));
    }catch(error){status(error.message,true);}finally{$('import').value='';}
};
$('viewport').onpointerdown=event=>{
    if(event.button!==0||event.target.closest('.node')||event.target.closest('path'))return;
    const p=$('viewport'),start={x:event.clientX,y:event.clientY,left:p.scrollLeft,top:p.scrollTop};p.setPointerCapture(event.pointerId);
    p.onpointermove=e=>{p.scrollLeft=start.left-(e.clientX-start.x);p.scrollTop=start.top-(e.clientY-start.y);};
    p.onpointerup=()=>{p.onpointermove=null;p.onpointerup=null;};
};
document.addEventListener('keydown',event=>{
    if(/INPUT|TEXTAREA|SELECT/.test(event.target.tagName))return;
    if((event.ctrlKey||event.metaKey)&&event.key==='z'){event.preventDefault();$(event.shiftKey?'redo':'undo').click();}
    if(event.key==='Delete')$('remove').click();
});
render();targetSuggestions();status(t('ready'));
