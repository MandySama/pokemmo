"""Static event provenance. Reachable writes are possibilities, not quest completion edges."""
import ast
import re
from collections import defaultdict


def constants(path):
    values = {'TRAINER_FLAGS_END': 0x7ff}
    expressions = re.findall(r'^#define\s+(\w+)\s+([^\n/]+)', path.read_text(), re.M)
    def value(node):
        if isinstance(node, ast.Constant) and isinstance(node.value, int): return node.value
        if isinstance(node, ast.Name): return values[node.id]
        if isinstance(node, ast.BinOp):
            a, b = value(node.left), value(node.right)
            if isinstance(node.op, ast.Add): return a + b
            if isinstance(node.op, ast.Sub): return a - b
        raise ValueError()
    for _ in range(8):
        for name, expression in expressions:
            try: values[name] = value(ast.parse(expression.strip(), mode='eval').body)
            except (KeyError, ValueError, SyntaxError, TypeError): pass
    return values


def build_semantics(root, maps, code, standards, strings, labels):
    code = {int(k): v for k, v in code.items()}
    standards = {int(k): v for k, v in standards.items()}
    audit = root / 'target/firered-script-audit/include/constants'
    symbols = {kind: constants(audit / file) for kind, file in [('flag','flags.h'),('variable','vars.h')]}
    names = {kind: {v:k for k,v in values.items() if k.startswith('FLAG_' if kind=='flag' else 'VAR_')} for kind,values in symbols.items()}
    import xml.etree.ElementTree as ET
    catalog = ET.parse(root / 'data/story/kanto-replay.xml').getroot()
    scene_names = {s.get('id'):strings[int(s.get('name'))] for s in catalog.findall('scene')}
    scenes, stages = defaultdict(list), defaultdict(list)
    for line in (root / 'protocol/src/main/resources/maps/kanto-story-entries.csv').read_text().splitlines():
        if not line or line.startswith('#'): continue
        fields = line.split(','); name = scene_names.get(fields[0])
        if not name: continue
        scenes[int(fields[1])].append(name)
        if int(fields[3]): stages[f'{fields[3]}:{fields[4]}'].append(name)
    map_names = {}
    for m in maps:
        match=re.search(r'\(([^)]+)\)',m['name'])
        if match:
            normalized=re.sub(r'[^A-Z0-9]+','_',match[1].upper()).strip('_')
            map_names[normalized]=m['name'].split('(')[0]
    for m in catalog.findall('map'):
        normalized = re.sub(r'(?<=[a-z0-9])(?=[A-Z])','_',m.get('key')).upper()
        map_names[normalized] = strings.get(int(m.get('name')),m.get('key'))
    u16 = lambda b,n: b[n] | b[n+1]<<8
    ptr = lambda b,n: int.from_bytes(bytes(b[n:n+4]),'little') - 0x8000000
    cache = {}
    def analyze(start):
        if start in cache: return cache[start]
        queue, seen, reads, writes, texts, missing, effects = [start],set(),set(),set(),set(),set(),set()
        while queue and len(seen)<12000:
            pc=queue.pop()
            if not pc or pc in seen: continue
            seen.add(pc); b=code.get(pc)
            if not b: missing.add(pc); continue
            op=b[0]
            if op==0x2b and u16(b,1)<0x4000: reads.add(('flag',u16(b,1)))
            if op in (0x21,0x22):
                reads.add(('variable',u16(b,1)))
                if op==0x22: reads.add(('variable',u16(b,3)))
            if op in (0x29,0x2a) and u16(b,1)<0x4000: writes.add(('flag',u16(b,1),int(op==0x29)))
            if op in (0x16,0x17,0x18,0x19,0x1a): writes.add(('variable',u16(b,1),u16(b,3) if op==0x16 else None))
            if op in (0x0f,0x85,0x67,0x9b):
                at=ptr(b,2 if op in (0x0f,0x85) else 1)
                if at>0: texts.add(at)
            if op in (0x4f,0x50,0x57,0x63): effects.add('moves')
            if op in (0x53,0x54,0x55,0x56,0x58,0x59): effects.add('visibility')
            if op in (0x44,0x45,0x49): effects.add('items')
            if op in (0x79,0x7a): effects.add('pokemon')
            if op==0x5c: effects.add('battle')
            if op in (0x25,0x26): effects.add('special')
            if 0x39<=op<=0x3f: effects.add('warp');continue
            if op in (4,5): queue.append(ptr(b,1))
            if op in (6,7): queue.append(ptr(b,2))
            if op in (8,9,10,11): queue.append(standards.get(b[2 if op>=10 else 1],0))
            if op==0x5c and b[1] in (1,2,6,8): queue.append(ptr(b,14 if b[1] in (1,2) else 18))
            if op not in (2,3,5,8,12,13): queue.append(pc+len(b))
        result=dict(reads=list(reads),writes=list(writes),texts=sorted(texts),missing=sorted(missing),effects=sorted(effects),limited=bool(queue))
        cache[start]=result
        return result
    events, writers, aliases, text_sources, states = {},defaultdict(list),defaultdict(list),defaultdict(list),{}
    def state_key(kind,number,mkey):
        local = kind=='flag' and number<0x20 or kind=='variable' and 0x4000<=number<0x4010
        return f'{mkey+":" if local else ""}{kind}:{number}'
    def state(kind,number,mkey):
        key=state_key(kind,number,mkey)
        if key not in states:
            symbol=names[kind].get(number,''); label=labels.get('state_'+symbol)
            if kind=='variable' and number<0x4000: label=labels['literalValue']+' '+str(number)
            if not label and kind=='variable' and symbol.startswith('VAR_MAP_SCENE_'):
                suffix=symbol.removeprefix('VAR_MAP_SCENE_')
                match=next((n for n in sorted(map_names,key=len,reverse=True) if suffix.startswith(n)),None)
                if match: label=map_names[match].split('(')[0]+' · '+labels['progressState']
            states[key]=dict(kind=kind,number=number,symbol=symbol,label=label,
                temporary=key.startswith(mkey+':'),dynamic=kind=='variable' and number>=0x8000)
        return key
    for m in maps:
        mkey=f'{m["group"]}/{m["map"]}'
        for kind in ('npcs','coordinate-events','background','map-scripts','warps'):
            for e in m[kind]:
                eid=f'{mkey}:{kind}:{e["index"]}'; e['event_key']=eid
                start=int(e.get('script_offset','0'),16)
                raw=analyze(start) if start else dict(reads=[],writes=[],texts=[],missing=[],effects=[],limited=False)
                reads=[state(k,n,mkey) for k,n in raw['reads']]
                if e.get('variable') and int(e['variable']): reads.append(state('variable',int(e['variable']),mkey))
                if e.get('flag') and int(e['flag']): reads.append(state('flag',int(e['flag']),mkey))
                writes=[dict(state=state(k,n,mkey),value=v) for k,n,v in raw['writes'] if not(k=='variable' and n>=0x8000)]
                data=dict(id=eid,map=mkey,mapName=m['name'],kind=kind,index=e['index'],actor=e.get('id'),
                    x=e.get('x'),y=e.get('y'),entry=start,scenes=list(dict.fromkeys(scenes[start])),
                    reads=sorted(set(reads)),writes=writes,texts=raw['texts'],effects=raw['effects'],missing=raw['missing'],limited=raw['limited'])
                if e.get('variable') and int(e['variable']): data['gate']=dict(state=state('variable',int(e['variable']),mkey),value=int(e['value']))
                if e.get('flag') and int(e['flag']): data['visibleWhen']=dict(state=state('flag',int(e['flag']),mkey),value=0)
                events[eid]=data
                if start: aliases[start].append(eid)
                for write in writes: writers[write['state']].append(dict(event=eid,value=write['value']))
                for text in raw['texts']: text_sources[text].append(eid)
    for key,s in states.items():
        if not s['label'] and s['kind']=='flag':
            hidden=[v for v in events.values() if v.get('visibleWhen',{}).get('state')==key]
            if hidden:
                first=hidden[0];s['label']=f'{first["mapName"].split("(")[0]} NPC #{first["actor"]} · '+labels['hiddenState']
        if not s['label']: s['label']=labels['temporaryState'] if s['temporary'] else labels['resultState'] if s['dynamic'] else labels['unnamedState']
    gates=defaultdict(list)
    for eid,event in events.items():
        if 'gate' in event: gates[event['gate']['state']].append(eid)
    return dict(events=events,states=states,writers=dict(writers),aliases={str(k):v for k,v in aliases.items()},
                gates=dict(gates),
                textSources={str(k):v for k,v in text_sources.items()},stages={k:list(dict.fromkeys(v)) for k,v in stages.items()},
                coverage=dict(maps=len(maps),events=len(events),scripts=len(cache),states=len(states)))
