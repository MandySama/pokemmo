/* A writer is provenance for a possible state change, never proof of a mandatory quest. */
function semanticEvent(source) {return D.semantics.events[source?.e?.event_key];}
function semanticStateKey(condition) {
    const kind=condition.kind,number=condition.id;
    const local=kind==='flag'&&number<0x20||kind==='variable'&&number>=0x4000&&number<0x4010;
    return `${local?key(current)+':':''}${kind}:${number}`;
}
function semanticEventName(event) {
    if(event.scenes.length)return event.scenes.join(' / ');
    const kind=event.kind==='npcs'?L.talkOther+' #'+event.actor:event.kind==='coordinate-events'?L.walkTrigger
        :event.kind==='background'?L.interactTrigger:event.kind==='warps'?L.warps:L.mapAuto;
    return event.mapName.split('(')[0]+' · '+kind+(event.x!==undefined?` (${event.x}, ${event.y})`:' #'+event.index);
}
function semanticStateName(key) {
    const state=D.semantics.states[key];
    if(!state)return L.resultState;
    if(state.label!==L.unnamedState)return state.label;
    const writers=D.semantics.writers[key]||[];
    const named=writers.map(w=>D.semantics.events[w.event]).find(e=>e.scenes.length);
    return named?L.relatedRecord+'「'+named.scenes[0]+'」':state.label;
}
function semanticValue(key,value) {
    const state=D.semantics.states[key];
    if(state?.kind==='flag')return value?L.stateSet:L.stateUnset;
    const stages=D.semantics.stages[`${state?.number}:${value}`];
    return stages?.length?L.stageFor+'「'+stages.join(' / ')+'」':L.stateValue+' '+value;
}
function semanticBranch(b) {
    const c=b.condition;
    if(!c)return L.runtimeCheck;
    if(c.kind==='check')return c.meaning+' · '+(b.operator===1?L.answerYes:L.answerNo)+'？';
    if(c.kind==='flag'){
        const name=semanticStateName(semanticStateKey(c));
        return name+' · '+(b.operator===1?L.isSet:b.operator===0?L.isUnset:L.runtimeCheck);
    }
    if(c.kind==='variable'){
        if(c.id===0x800c&&!c.variable){
            const directions={1:L.directionDown,2:L.directionUp,3:L.directionLeft,4:L.directionRight};
            return L.playerFacing+' '+(b.operator===5?L.notEqual:L.equal)+' '+(directions[c.value]||c.value)+'？';
        }
        const name=c.meaning||semanticStateName(semanticStateKey(c));
        const value=c.variable?semanticStateName(semanticStateKey({kind:'variable',id:c.value}))
            : [L.resultYesNo,L.resultSpace,L.resultItem,L.resultMoney].includes(c.meaning)&&[0,1].includes(c.value)?(c.value?L.answerYes:L.answerNo):semanticValue(semanticStateKey(c),c.value);
        return `${name} · ${[L.less,L.equal,L.greater,L.lessEqual,L.greaterEqual,L.notEqual][b.operator]||L.equal} ${value}？`;
    }
    return L.runtimeCheck;
}
function semanticLink(parent,id) {
    const event=D.semantics.events[id];if(!event)return;
    button(parent,semanticEventName(event),()=>{
        const map=maps.get(event.map);if(!map)return;
        showMap(map);const record=map[event.kind].find(e=>e.event_key===id);
        if(record){select(event.kind,record);locateEvent(record);}
    },'event-link');
}
function lazyEventLinks(fold,ids) {
    let loaded=false;
    fold.addEventListener('toggle',()=>{if(fold.open&&!loaded){loaded=true;ids.forEach(id=>semanticLink(fold,id));}});
}
function semanticWriters(parent,key,value,exclude) {
    const all=(D.semantics.writers[key]||[]).filter(w=>w.event!==exclude&&(value===undefined||w.value===value||w.value===null));
    const ids=[...new Set(all.map(w=>w.event))];
    if(!ids.length){para(parent,L.noKnownProducer,'source');return;}
    const fold=el('details',undefined,'dependency-list');fold.append(el('summary',L.possibleProducers+' ('+ids.length+')'));
    para(fold,L.producerCaution,'source');lazyEventLinks(fold,ids);parent.append(fold);
}
function semanticGuide(parent,source) {
    const event=semanticEvent(source);if(!event)return;
    const box=el('section',undefined,'semantic-guide');heading(box,L.triggerAndUnlock);
    if(event.scenes.length)para(box,L.storyBelongs+'「'+event.scenes.join(' / ')+'」','activity-summary');
    let trigger=friendlySource(source,selected?.kind==='npcs'?selected.e:null);
    if(source.kind==='map-scripts')trigger=({1:L.mapLoad,2:L.mapFrame,3:L.mapEnter,4:L.mapWarpIn,5:L.mapResume,6:L.mapDive,7:L.mapReturn})[+source.e.type]||L.mapAuto;
    para(box,L.triggerMethod+'：'+trigger);
    if(event.gate){
        heading(box,L.requiredGate);
        para(box,semanticStateName(event.gate.state)+' · '+semanticValue(event.gate.state,event.gate.value));
        semanticWriters(box,event.gate.state,event.gate.value,event.id);
    }else para(box,L.noEntryGate,'source');
    if(event.visibleWhen){
        para(box,L.appearanceGate+'：'+semanticStateName(event.visibleWhen.state)+' · '+L.stateUnset);
        semanticWriters(box,event.visibleWhen.state,0,event.id);
    }
    const aliases=(D.semantics.aliases[event.entry]||[]).filter(id=>id!==event.id);
    if(aliases.length){const fold=el('details');fold.append(el('summary',L.sameEntry+' ('+aliases.length+')'));para(fold,L.sameEntryHint,'source');lazyEventLinks(fold,aliases);box.append(fold);}
    const shared=[...new Set(event.texts.flatMap(at=>D.semantics.textSources[at]||[]))].filter(id=>id!==event.id&&!aliases.includes(id));
    if(shared.length){const fold=el('details');fold.append(el('summary',L.sharedDialogue+' ('+shared.length+')'));para(fold,L.sharedDialogueHint,'source');lazyEventLinks(fold,shared);box.append(fold);}
    const persistent=event.reads.filter(k=>!D.semantics.states[k]?.dynamic&&k!==event.gate?.state&&k!==event.visibleWhen?.state);
    if(persistent.length){
        const fold=el('details');fold.append(el('summary',L.dialogueDepends+' ('+persistent.length+')'));para(fold,L.branchNotGate,'source');
        persistent.forEach(k=>{heading(fold,semanticStateName(k));if(D.semantics.states[k]?.temporary)para(fold,L.temporaryHint,'source');semanticWriters(fold,k,undefined,event.id);});box.append(fold);
    }
    const effects=event.effects.filter(k=>k!=='special').map(k=>L['effect_'+k]);
    if(effects.length)para(box,L.possibleEffects+'：'+effects.join('、'),'source');
    if(event.writes.length){
        const fold=el('details');fold.append(el('summary',L.afterChanges));para(fold,L.possibleChangeHint,'source');
        for(const write of event.writes){
            para(fold,semanticStateName(write.state)+' → '+(write.value===null?L.calculatedValue:semanticValue(write.state,write.value)),'source');
            const next=(D.semantics.gates[write.state]||[]).map(id=>D.semantics.events[id]).filter(e=>e.id!==event.id&&(write.value===null||e.gate.value===write.value));
            if(next.length){const links=el('details');links.append(el('summary',L.mayEnable+' ('+next.length+')'));lazyEventLinks(links,next.map(e=>e.id));fold.append(links);}
        }box.append(fold);
    }
    if(event.missing.length||event.limited||event.effects.includes('special')||event.effects.includes('battle'))para(box,L.dependencyIncomplete,'source');
    parent.append(box);
}
function dialogueContexts(parent,related) {
    heading(parent,L.dialogueContexts);
    para(parent,L.contextsHint,'source');
    if(!related.length){para(parent,L.noScript);return;}
    for(const source of related){
        const event=semanticEvent(source),box=el('details',undefined,'dialogue-context');
        box.append(el('summary',friendlySource(source,selected.e)));
        semanticGuide(box,source);
        button(box,L.inspectContext,()=>{
            sourceIndex=related.indexOf(source);choices={};inspect();locateEvent(source.e);
        });
        const texts=event?.texts||[];
        if(texts.length){const preview=el('details');preview.append(el('summary',L.possibleDialogue+' ('+texts.length+')'));para(preview,L.possibleDialogueHint,'source');dialogue(preview,texts.map(at=>({at,text:D.texts[at]||L.missingText})));box.append(preview);}
        parent.append(box);
    }
}
