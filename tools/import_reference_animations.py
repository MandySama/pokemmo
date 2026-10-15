"""Import move animation parameters and their external particle archive."""
from pathlib import Path
import argparse, collections, json, re, shutil, zipfile
parser=argparse.ArgumentParser();parser.add_argument('source',type=Path);args=parser.parse_args()
root=Path(__file__).resolve().parents[1]
out=[]
for path in (args.source/'src/main/java/cn/pokemmo/battle/animation/move').rglob('*.java'):
    source=path.read_text(encoding='utf-8');match=re.search(r'技能编号:\s*(\d+)',source)
    id=int(match[1]) if match else {'FairyWindAnimation':584,'PrecipiceBladesAnimation':619}[path.stem]
    env={};commands=[]
    pattern=r'\b(\w+)\s*=\s*(-?[\d.]+[fF]?|\w+)\s*;|this\.(fE0|Qh0|Wt|tP|EN|WW|Xq0|Ue0|dA0|E2|mf0|df0|Uv|wn0)\(([^;]*?)\)'
    for match in re.finditer(pattern,source):
        if match[1]:
            value=match[2];env[match[1]]=env.get(value,float(value.rstrip('fF')) if re.fullmatch(r'-?[\d.]+[fF]?',value) else None)
        else:
            if match[3]=='wn0':
                commands.append({'op':'wn0','resource':match[4].strip().strip(chr(34)),'args':[]});continue
            values=[]
            for value in match[4].split(','):
                value=re.sub(r'\((?:byte|short|int|float)\)','',value).strip()
                number=env.get(value)
                if re.fullmatch(r'-?[\d.]+[fF]?',value):number=float(value.rstrip('fF'))
                if value in ('true','false'):number=1 if value=='true' else 0
                if value=='bl ? 1 : 0':number=0
                values.append(number)
            commands.append({'op':match[3],'args':values})
    out.append({'id':id,'name':path.stem,'commands':commands})
out.sort(key=lambda item:item['id']);assert len(out)==561 and len({item['id'] for item in out})==561
folder=root/'data/battle';folder.mkdir(exist_ok=True)
(folder/'animations.json').write_text(json.dumps(out,separators=(',',':')),encoding='utf-8')
with zipfile.ZipFile(args.source/'data/sprites/particles.pak') as archive:
    contents={name:archive.read(name) for name in archive.namelist() if not name.endswith('/')}
for path in (args.source/'data/sprites/particles').rglob('*'):
    if path.is_file():contents[path.relative_to(args.source/'data/sprites/particles').as_posix()]=path.read_bytes()
with zipfile.ZipFile(folder/'particles.zip','w',zipfile.ZIP_DEFLATED) as archive:
    for name,data in contents.items():archive.writestr(name,data)
shutil.copyfile(args.source/'data/sprites/particles/particle/special/type_9.vfx',root/'data/particles/summary/type_9.vfx')
effects={}
with zipfile.ZipFile(folder/'particles.zip') as archive:
    for name in archive.namelist():
        if not name.endswith('.vfx'):continue
        source=archive.read(name).decode()
        source=re.sub(r'(?<=[{,])\s*([\w$]+)\s*:',lambda m:json.dumps(m[1])+':',source)
        def quote(match):
            value=match[1].strip()
            if value.startswith('"') or value in ('true','false','null') or re.fullmatch(r'-?[\d.]+(?:[Ee][+-]?\d+)?',value):return ':'+value
            return ':'+json.dumps(value)
        source=re.sub(r':([^{}\[\],]+)',quote,source)
        effects[name]=json.loads(source)
rows=[];named={}
for animation in out:
    id=animation['id'];cursor=0;end=0;entries=[]
    for command in animation['commands']:
        op=command['op'];values=command['args']
        if op=='wn0':
            name='custom/'+command['resource'];aps=named.setdefault(name,20000+len(named));key='particle/'+name+'.vfx'
            duration=0
            for index,controller in enumerate(effects[key]['resource']['controllers']):
                emitter=controller['emitter'];life=max(16,int(emitter.get('delay',{}).get('lowMax',0)+emitter.get('duration',{}).get('lowMax',0)+emitter.get('life',{}).get('highMax',0)))
                entries.append(f'E,{aps},{index},11,{cursor},{life},0.5');duration=max(duration,life)
            end=max(end,cursor+duration)
        elif op in ('Wt','tP'):
            value=values[1] if op=='Wt' else values[0]
            if value is not None:cursor+=int(value*1000)
        elif op in ('fE0','dA0'):
            values=values if op=='fE0' else [-1,*values[:4],values[5]]
            assert all(value is not None for value in values[:5]),(id,command)
            aps=int(values[1]);index=int(values[2]);target=int(values[3]);key=f"particle/{'custom' if aps>=1000 else 'auto'}/{aps}.vfx"
            controllers=effects[key]['resource']['controllers']
            if index>=len(controllers):continue
            emitter=controllers[index]['emitter'];duration=max(16,int(emitter.get('delay',{}).get('lowMax',0)+emitter.get('duration',{}).get('lowMax',0)+emitter.get('life',{}).get('highMax',0)))
            entries.append(f'E,{aps},{index},{target},{cursor},{duration},{values[5] or 0}')
            cursor+=duration
        else:
            if op=='EN' and len(values)>=7 and all(v is not None for v in values[2:5]):end=max(end,cursor+int((values[3]+values[4])*1000*max(1,values[2])))
            if op=='Xq0' and len(values)>=7 and all(v is not None for v in values[2:5]):end=max(end,cursor+int((values[3]+values[4])*2000*max(1,values[2])))
            if op=='WW' and len(values)>=4 and values[1] is not None:end=max(end,cursor+int(values[1]*1000))
            entries.append('C,'+op+','+str(cursor)+','+','.join('0' if value is None else str(value) for value in values))
    duration=1600 if id==33 else max(1000,cursor+300,end+100)
    rows.append(f'M,{id},{duration}');rows.extend(f'{id},'+entry for entry in entries)
path=root/'battle/src/main/resources/battle';path.mkdir(parents=True,exist_ok=True)
rows=[f'R,{id},{name}' for name,id in named.items()]+rows
(path/'animations.csv').write_text('\n'.join(rows)+'\n',encoding='utf-8')
print('Imported',len(out),'animations;',len(effects),'particle definitions')
