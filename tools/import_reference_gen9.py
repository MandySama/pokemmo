"""Import the local extended catalog and assets without changing the source client."""
from pathlib import Path
import argparse, gzip, struct, json, zipfile, io, xml.etree.ElementTree as ET

class Reader:
    def __init__(self,b):self.b=b;self.o=0
    def read(self,f):v=struct.unpack_from('<'+f,self.b,self.o);self.o+=struct.calcsize('<'+f);return v[0] if len(v)==1 else v
    def skip(self,n):self.o+=n
    def parameter(self):
        self.read('B');kind=self.read('B')
        if kind&128:self.skip(1);kind&=127
        if kind==28:return
        if kind==30:self.skip(8)
        elif kind in (9,10,17):self.skip(4)
        elif kind in (5,18):
            while self.read('H'):pass
        else:self.skip(self.read('B')*2)

args=argparse.ArgumentParser();args.add_argument('source',type=Path);a=args.parse_args();root=Path(__file__).resolve().parents[1]
j=json.loads((a.source/'data/pokedex_data.json').read_text(encoding='utf-8'))
lookup={int(k):v['national_id'] for k,v in j['species'].items()};types={};learn={};moves={};gender={}
b=gzip.decompress((a.source/'data/data.pak').read_bytes());r=Reader(b);r.skip(12);blocks=[]
for _ in range(r.read('B')):
    kind=r.read('B');size=r.read('I');blocks.append((kind,b[r.o:r.o+size]));r.skip(size)
assert r.o==len(b)
def type_id(v):return 17 if v==9 else v-1 if v>=10 else v
for kind,payload in blocks:
    q=Reader(payload)
    if kind==10:
        for _ in range(q.read('H')):
            id=q.read('H');one,two=q.read('BB');q.skip(12+6+2);types[id]=[type_id(one),type_id(two)]
    elif kind==1:
        for _ in range(q.read('H')):
            id=q.read('H');learn[id]=[q.read('HB') for _ in range(q.read('B'))]
    elif kind==4:
        for _ in range(q.read('H')):
            id=q.read('H');category=q.read('B');flags=q.read('H');m=moves.setdefault(id,{'id':id,'category':{0:1,1:2,2:0}[category]})
            for bit,key,fmt in [(1,'accuracy','B'),(2,'power','H'),(4,'pp','B'),(8,'type','B'),(16,'flags','I'),(32,'target','B'),(64,'priority','B'),(256,'unknown','B')]:
                if flags&bit:m[key]=type_id(q.read(fmt)) if key=='type' else q.read(fmt)
            if flags&512:q.skip(q.read('B'));q.skip(1);q.skip(q.read('B'));q.skip(3)
            for _ in range(q.read('B')):
                q.skip(4)
                for _ in range(q.read('B')):q.parameter()
    elif kind==6:
        for _ in range(q.read('H')):
            id,flags=q.read('HH')
            if flags&1:q.skip(2)
            if flags&4:q.skip(12)
            if flags&8:q.skip(6)
            if flags&16:q.skip(2)
            if flags&32:q.skip(q.read('B')*2)
            if flags&128:q.skip(2)
            if flags&256:gender[id]=q.read('B')
    else:continue
    assert q.o==len(payload),(kind,q.o,len(payload))
# Existing move records supply unchanged Generation V metadata.
base=gzip.decompress((root/'protocol/src/main/resources/catalog/pokemon.dat.gz').read_bytes());q=io.BytesIO(base);magic,count=struct.unpack('>II',q.read(8));assert count==649
for _ in range(count):
    q.read(2+6+8);n=struct.unpack('>H',q.read(2))[0]
    for _ in range(n):
        level,id,pp,t,cat,power,accuracy=struct.unpack('>BHBBBBB',q.read(8))
        moves[id]={**{'id':id,'pp':pp,'type':t,'category':cat,'power':power,'accuracy':accuracy},**moves.get(id,{})}
for line in (root/'protocol/src/main/resources/catalog/moves-gen5.csv').read_text().splitlines():
    id,t,cat,power,accuracy,pp=map(int,line.split(','))
    moves[id]={**moves.get(id,{}),**{'id':id,'pp':pp,'type':t,'category':cat,'power':power,'accuracy':accuracy}}
expanded=[]
for internal,v in j['species'].items():
    internal=int(internal);id=v['national_id'];v=dict(v);v['internal_id']=internal;v['id']=id
    v['stats']=[v['stats'][i] for i in [0,1,2,4,5,3]];v['ev_yield']=[v['ev_yield'][i] for i in [0,1,2,4,5,3]]
    assert internal in types,(internal,id)
    v['types']=types[internal];v['gender']=255;v['growth']=1;v['friendship']=0
    v['moves']=[]
    for move,level in learn.get(internal,[]):
        if move not in moves:raise ValueError(('missing move',id,move))
        m=moves[move];assert all(k in m for k in ['pp','type','category','power','accuracy']),(id,move,m)
        v['moves'].append({'level':level,**{k:m[k] for k in ['id','pp','type','category','power','accuracy']}})
    expanded.append(v)
expanded.sort(key=lambda x:x['id']);assert [v['id'] for v in expanded]==list(range(650,1026))
method_ids={'LEVEL':4,'ITEM':8,'TRADE':5,'LEVEL_WITH_SKILL':21,'HAPPINESS':1,'HAPPINESS_NIGHT':3,'LEVEL_ITEM_DAY':19,'LEVEL_ITEM_NIGHT':20,'ITEM_MALE':16,'ITEM_FEMALE':17}
for v in expanded:
    v['evolutions']=[{'target':lookup.get(e['target_id'],e['target_id']),'method':method_ids[e['method']],'parameter':e.get('val',0)} for e in v.get('evolutions',[])]
cross=[]
for old,entries in j['cross_gen_evolutions'].items():
    for e in entries:
        target=lookup.get(e['target_id'],e['target_id']);source=lookup.get(int(old),int(old));
        cross.append({'from':source,'target':target,'method':method_ids[e['method']],'parameter':e.get('val',0)})
        if source>=650:
            expanded[source-650]['evolutions'].append({'target':target,'method':method_ids[e['method']],'parameter':e.get('val',0)})
(root/'data/catalog').mkdir(parents=True,exist_ok=True)
(root/'data/catalog/gen9.json').write_text(json.dumps(expanded,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
out=io.BytesIO();out.write(struct.pack('>II',0x504b4439,len(expanded)))
for v in expanded:
    out.write(struct.pack('>H',v['id']));out.write(bytes(v['stats']));out.write(bytes(v['types']));out.write(struct.pack('>HHH',*v['abilities']));out.write(bytes([v['gender'],v['growth'],v['friendship']]))
    out.write(struct.pack('>H',len(v['moves'])))
    for m in v['moves']:out.write(struct.pack('>BHBBBHB',m['level'],m['id'],m['pp'],m['type'],m['category'],m['power'],m['accuracy']))
(root/'protocol/src/main/resources/catalog/pokemon-gen9.dat.gz').write_bytes(gzip.compress(out.getvalue(),mtime=0))
extra=io.BytesIO();extra.write(struct.pack('>I',len(expanded)))
for v in expanded:
    extra.write(struct.pack('>HHHH',v['height'],v['weight'],*v['egg_groups']));extra.write(bytes(v['ev_yield']));extra.write(struct.pack('>H',len(v['evolutions'])))
    for e in v['evolutions']:extra.write(struct.pack('>HHH',e['target'],e['method'],e['parameter']))
extra.write(struct.pack('>H',len(cross)))
for e in cross:extra.write(struct.pack('>HHHH',e['from'],e['target'],e['method'],e['parameter']))
(root/'data/catalog/gen9.dat.gz').write_bytes(gzip.compress(extra.getvalue(),mtime=0))
# Normalize internal sprite names to national numbers inside a separate external archive.
(root/'data/resources').mkdir(parents=True,exist_ok=True)
with zipfile.ZipFile(a.source/'data/resources.zip') as src,zipfile.ZipFile(root/'data/resources/pokemon-gen9.zip','w',zipfile.ZIP_DEFLATED) as dst:
    for v in expanded:
        for suffix in ['front-n','front-s','back-n','back-s']:
            stem=f"sprites/battlesprites/{v['internal_id']:03d}-{suffix}"
            extension='gif' if stem+'.gif' in src.namelist() else 'png'
            dst.writestr(f"{v['id']}/{suffix}.{extension}",src.read(stem+'.'+extension))
        dst.writestr(f"{v['id']}/icon.png",src.read(f"sprites/monstericons/{v['internal_id']}-0.png"))
# Append language entries, preserving the hand-maintained XML layout.
path=root/'data/strings/strings_zh.xml';text=path.read_text(encoding='utf-8');existing={int(n.attrib['id']) for n in ET.fromstring(text).findall('string')};strings={}
for v in expanded:
    for offset,key in [(150000,'name'),(350000,'description'),(351000,'category')]:strings[offset+v['id']]=v[key]
for key,v in j['abilities'].items():
    if int(key)>164:strings[210000+int(key)]=v['name'];strings[220000+int(key)]=v['description']
ref=ET.parse(a.source/'data/strings/strings_zh.xml').getroot()
for n in ref.findall('string'):
    id=int(n.attrib['id'])
    if 110560<=id<=111000:strings[id]=n.text or ''
lines=[]
for id,value in sorted(strings.items()):
    if id not in existing:
        node=ET.Element('string',id=str(id));node.text=value;lines.append(ET.tostring(node,encoding='unicode'))
text=text.replace('</strings>','\n'.join(lines)+'\n</strings>');path.write_text(text,encoding='utf-8')
print('Imported',len(expanded),'species;',sum(len(v['moves']) for v in expanded),'learned moves;',len(lines),'language entries')
