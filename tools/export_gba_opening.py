"""Export executable ROM instructions and movements, retaining every ROM address."""
from pathlib import Path
import re, struct, sys, gzip, hashlib
root = Path(__file__).resolve().parents[1]
rom = Path(sys.argv[1] if len(sys.argv)>1 else root/'roms/firered.gba').read_bytes()
audit = Path(sys.argv[2]) if len(sys.argv)>2 else root/'target/firered-script-audit'
schema = list(map(int,(root/'map-core/src/main/resources/gba-script-lengths.csv').read_text().strip().split(',')))
names = {}
table=(audit/'data/script_cmd_table.inc').read_text()
for name, op in re.findall(r'ScrCmd_(\w+)\s+@ 0x([0-9a-fA-F]+)',table): names[int(op,16)]=name
macros=(audit/'asm/macros/event.inc').read_text()
for body in re.findall(r'\.macro[^\n]*\n(.*?)\.endm',macros,re.S):
    match=re.search(r'\.byte\s+0x([0-9a-fA-F]+)',body)
    if not match or '.if' in body: continue
    size=0
    for kind, args in re.findall(r'\.(byte|2byte|4byte)\s+([^\n@]+)',body[match.start():]):
        size += {'byte':1,'2byte':2,'4byte':4}[kind]*len(args.strip().split(','))
    if size: schema[int(match[1],16)]=size
schema[0x55]=schema[0x53]=schema[0x54]=3
for op in [0x39,0x3a,0x3b,0x3d,0x3e,0x3f,0xd1]:schema[op]=8
for op in [0x7d,0x7f,0x80,0x81,0x82,0x83,0x84]:schema[op]=4
schema[0x7e]=2;schema[0x85]=6;schema[0x79]=15;schema[0xc7]=2
u16=lambda p:struct.unpack_from('<H',rom,p)[0]
ptr=lambda p:struct.unpack_from('<I',rom,p)[0]-0x08000000
events=(root/'target/opening-rom-events.csv').read_text().strip().splitlines()
initial=[]
for at in range(len(rom)-3):
    if rom[at]!=0x29 or at>=3 and rom[at-3]==0x29: continue
    p=at;flags=[]
    while p<len(rom)-3 and rom[p]==0x29:flags.append(u16(p+1));p+=3
    if len(flags)>20 and 43 in flags and 44 in flags and 45 not in flags:initial.append(at)
if len(initial)!=1:raise ValueError('Cannot identify ROM new-game map flag script')
standard_npc=rom.find(bytes.fromhex('6a5a6700000000666d6c03'))
if standard_npc<0:raise ValueError('Cannot locate standard dialogue script')
needle=struct.pack('<I',standard_npc+0x08000000)
tables=[];at=0
while True:
    at=rom.find(needle,at)
    if at<0:break
    start=at-8
    if start>=0 and 0<ptr(start)<len(rom) and rom[ptr(start+12)]==0x69 and rom[ptr(start+16)]==0x67:tables.append(start)
    at+=1
if len(tables)!=1:raise ValueError('Cannot identify standard script table: '+str(tables))
standards=[ptr(tables[0]+i*4) for i in range(10)]
hidden_root=rom.find(bytes.fromhex('69c7033021058000000601'))
if hidden_root<0 or rom.count(bytes.fromhex('69c7033021058000000601'))!=1:raise ValueError('Cannot identify hidden item script')
finder_pattern=bytes.fromhex('69c7033004')+rom[hidden_root+16:hidden_root+20]
finder_root=rom.find(finder_pattern)
if finder_root<0 or rom.count(finder_pattern)!=1:raise ValueError('Cannot identify underfoot item script')
roots=initial[:]+standards+[hidden_root,finder_root]
for line in events:
    f=line.split(',')
    if f[0]=='hidden':continue
    if f[0]=='bg' and int(f[6])>=5:continue
    p=int(f[-1]);
    if p: roots.append(p)
nodes={}; moves={}; queue=roots[:]; parents={}
while queue:
    pc=queue.pop()
    if pc in nodes:continue
    if not 0<pc<len(rom):raise ValueError(f'Invalid script address {pc:x}')
    op=rom[pc]; n=schema[op]
    if op==0x5c:
        kind=rom[pc+1];n=10 if kind==3 else 18 if kind in [1,2,4,7] else 22 if kind in [6,8] else 14
    if n==0:
        trail=[pc]
        while trail[-1] in parents:trail.append(parents[trail[-1]])
        print('trail', [hex(v) for v in trail[:30]], 'root',hex(trail[-1]))
        raise ValueError(f'Unknown instruction length {op:02x} at {pc:x}')
    data=rom[pc:pc+n];nodes[pc]=data
    before=len(queue)
    if op in [8,9] and rom[pc+1]<len(standards):queue.append(standards[rom[pc+1]])
    if op in [4,5]: queue.append(ptr(pc+1))
    if op in [6,7]:queue.append(ptr(pc+2))
    if op in [0x4f,0x50]:
        p=ptr(pc+3);q=p
        while rom[q]!=0xfe:
            q+=1
            if q-p>4096:raise ValueError('Movement budget')
        moves[p]=rom[p:q+1]
    if op==0x5c and rom[pc+1] in [1,2]:queue.append(ptr(pc+14))
    if op==0x5c and rom[pc+1] in [6,8]:queue.append(ptr(pc+18))
    if op not in [2,3,5,8,0x0c,0x0d]:queue.append(pc+n)
    for child in queue[before:]:parents.setdefault(child,pc)
    if len(nodes)>500000:raise ValueError('Script budget')
out=root/'protocol/src/main/resources/maps/gba-opening-program.txt.gz'
lines=['sha256,'+hashlib.sha256(rom).hexdigest(),'init,'+str(initial[0])]+events
lines.append('hiddenroot,'+str(hidden_root))
lines.append('finderroot,'+str(finder_root))
lines += [f'std,{i},{at}' for i,at in enumerate(standards)]
tm_candidates=[at for at in range(len(rom)-116) if rom.startswith(struct.pack('<3H',264,337,352),at)]
tm_tables={rom[at:at+116] for at in tm_candidates}
if len(tm_tables)!=1:raise ValueError('Cannot identify native TM/HM move table')
lines += [f'tmmove,{289+i},{move}' for i,move in enumerate(struct.unpack('<58H',tm_tables.pop()))]
chars={chr(65+i):0xbb+i for i in range(26)}|{chr(97+i):0xd5+i for i in range(26)}|{str(i):0xa1+i for i in range(10)}|{' ':0,'é':0x1b,"'":0xb4,',':0xb8,'.':0xad,'!':0xab,'?':0xac,':':0xf0,'-':0xae}
for name,line in re.findall(r'PokedexRating_Text_(LessThan\d+|Complete)::\s*\.string "([^"\\]+)',(audit/'data/text/pokedex_rating.inc').read_text(encoding='utf-8')):
    pattern=bytearray()
    for char in line:
        if char not in chars:break
        pattern.append(chars[char])
    hits=[match.start() for match in re.finditer(re.escape(bytes(pattern)),rom)]
    if len(hits)!=1:raise ValueError('Cannot identify Pokedex rating '+name)
    lines.append(f'dexrating,{160 if name=="Complete" else int(name[8:])},{hits[0]}')

for pc,data in sorted(nodes.items()):lines.append(f'code,{pc},{data.hex()}')
for pc,data in sorted(moves.items()):lines.append(f'move,{pc},{data.hex()}')
specials=re.findall(r'^\s*def_special\s+(\w+)',(audit/'data/specials.inc').read_text(),re.M)
lines += [f'special,{i},{name}' for i,name in enumerate(specials)]
movement_names=re.findall(r'#define MOVEMENT_ACTION_(\w+)\s+0x([0-9a-fA-F]+)',(audit/'include/constants/event_object_movement.h').read_text())
lines += [f'movement,{int(code,16)},{name}' for name,code in movement_names]
with gzip.GzipFile(filename=str(out),mode='wb',mtime=0) as stream:stream.write(('\n'.join(lines)+'\n').encode())
(root/'target/opening-opcodes.txt').write_text('\n'.join(f'{op:02x} {names.get(op,"?")} {sum(1 for d in nodes.values() if d[0]==op)}' for op in sorted(set(d[0] for d in nodes.values()))))
print(f'ROM instructions={len(nodes)}, movements={len(moves)}, bytes={out.stat().st_size}')
