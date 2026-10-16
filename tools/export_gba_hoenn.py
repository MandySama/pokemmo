"""Export executable ROM instructions and movements, retaining every ROM address."""
from pathlib import Path
import re, struct, sys, gzip, hashlib
root = Path(__file__).resolve().parents[1]
rom = Path(sys.argv[1] if len(sys.argv)>1 else root/'roms/emerald.gba').read_bytes()
audit = Path(sys.argv[2]) if len(sys.argv)>2 else root/'target/emerald-script-audit'
schema = list(map(int,(root/'map-core/src/main/resources/gba-script-lengths.csv').read_text().strip().split(',')))
names = {}
table=(audit/'data/script_cmd_table.inc').read_text()
for name, op in re.findall(r'ScrCmd_(\w+)\s+@ 0x([0-9a-fA-F]+)',table): names[int(op,16)]=name
macros=(audit/'asm/macros/event.inc').read_text()
opcodes={symbol:int(code,16) for symbol,code in re.findall(r'script_cmd_table_entry\s+(SCR_OP_\w+)\s+\w+\s+@ 0x([0-9a-fA-F]+)',table)}
for body in re.findall(r'\.macro[^\n]*\n(.*?)\.endm',macros,re.S):
    match=re.search(r'\.byte\s+(SCR_OP_\w+)',body)
    if not match or '.if' in body:continue
    size=0
    for kind,args in re.findall(r'\.(byte|2byte|4byte)\s+([^\n@]+)',body[match.start():]):
        size+={'byte':1,'2byte':2,'4byte':4}[kind]*len(args.strip().split(','))
    if size:schema[opcodes[match[1]]]=size
schema[0x55]=schema[0x53]=schema[0x54]=3
for op in [0x39,0x3a,0x3b,0x3d,0x3e,0x3f,0xd1]:schema[op]=8
for op in [0x7d,0x7f,0x80,0x81,0x82,0x83,0x84]:schema[op]=4
schema[0x7e]=2;schema[0x85]=6;schema[0x79]=15;schema[0xc7]=2
u16=lambda p:struct.unpack_from('<H',rom,p)[0]
ptr=lambda p:struct.unpack_from('<I',rom,p)[0]-0x08000000
import json
groups=json.loads((audit/'data/maps/map_groups.json').read_text())
selected={'InsideOfTruck','LittlerootTown','LittlerootTown_BrendansHouse_1F','LittlerootTown_BrendansHouse_2F','LittlerootTown_MaysHouse_1F','LittlerootTown_MaysHouse_2F','LittlerootTown_ProfessorBirchsLab','Route101','OldaleTown','Route103'}
keys={(g,m) for g,name in enumerate(groups['group_order']) for m,n in enumerate(groups[name]) if n in selected}
events=[line for line in (root/'protocol/src/main/resources/maps/emerald-story-events.csv').read_text().strip().splitlines() if tuple(map(int,line.split(',')[1:3])) in keys]
initial=[]
for at in range(len(rom)-3):
    if rom[at]!=0x29 or at>=3 and rom[at-3]==0x29: continue
    p=at;flags=[]
    while p<len(rom)-3 and rom[p]==0x29:flags.append(u16(p+1));p+=3
    if len(flags)>100:initial.append(at)
if len(initial)!=1:raise ValueError('Cannot identify ROM new-game map flag script')
standard_npc=rom.find(bytes.fromhex('6a5a6700000000666d6c03'))
if standard_npc<0:raise ValueError('Cannot locate native standard dialogue script')
needle=struct.pack('<I',standard_npc+0x08000000)
tables=[];at=0
while True:
 at=rom.find(needle,at)
 if at<0:break
 start=at-8
 if start>=0 and 0<ptr(start)<len(rom) and rom[ptr(start+12)]==0x69 and rom[ptr(start+16)]==0x67:tables.append(start)
 at+=1
if len(tables)!=1:raise ValueError('Cannot identify standard script table: '+str(tables))
standards=[ptr(tables[0]+i*4) for i in range(11)]
roots=initial[:]+standards[:2]
for line in events:
    f=line.split(',')
    p=int(f[-1]);
    if p: roots.append(p)
nodes={}; moves={}; queue=roots[:]
while queue:
    pc=queue.pop()
    if pc in nodes:continue
    if not 0<pc<len(rom):raise ValueError(f'Invalid script address {pc:x}')
    op=rom[pc]; n=schema[op]
    if op==0x5c:
        kind=rom[pc+1];n=10 if kind==3 else 18 if kind in [1,2,4,7] else 22 if kind in [6,8] else 14
    if n==0:raise ValueError(f'Unknown instruction length {op:02x} at {pc:x}')
    data=rom[pc:pc+n];nodes[pc]=data
    if op in [4,5]: queue.append(ptr(pc+1))
    if op in [8,9] and rom[pc+1]<2:queue.append(standards[rom[pc+1]])
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
    if len(nodes)>50000:raise ValueError('Script budget')
out=root/'protocol/src/main/resources/maps/gba-hoenn-program.txt.gz'
lines=['sha256,'+hashlib.sha256(rom).hexdigest(),'init,'+str(initial[0])]+events
heal=rom.find(bytes.fromhex('010104000200000001030400020000000000140011000000'))
if heal<0:raise ValueError('Cannot identify ROM heal table')
lines += [f'heal,{i+1},{rom[heal+i*8]},{rom[heal+i*8+1]},{u16(heal+i*8+2)},{u16(heal+i*8+4)}' for i in range(2)]
lines += [f'std,{i},{at}' for i,at in enumerate(standards)]
lines += ['species,'+line for line in (root/'map-core/src/main/resources/gba-national-species.csv').read_text().splitlines() if line and not line.startswith('#')]
for pc,data in sorted(nodes.items()):lines.append(f'code,{pc},{data.hex()}')
for pc,data in sorted(moves.items()):lines.append(f'move,{pc},{data.hex()}')
specials=re.findall(r'^\s*def_special\s+(\w+)',(audit/'data/specials.inc').read_text(),re.M)
lines += [f'special,{i},{name}' for i,name in enumerate(specials)]
movement_names=re.findall(r'#define MOVEMENT_ACTION_(\w+)\s+0x([0-9a-fA-F]+)',(audit/'include/constants/event_object_movement.h').read_text())
lines += [f'movement,{int(code,16)},{name}' for name,code in movement_names]
with gzip.GzipFile(filename=str(out),mode='wb',mtime=0) as stream:stream.write(('\n'.join(lines)+'\n').encode())
(root/'target/hoenn-opcodes.txt').write_text('\n'.join(f'{op:02x} {names.get(op,"?")} {sum(1 for d in nodes.values() if d[0]==op)}' for op in sorted(set(d[0] for d in nodes.values()))))
print(f'ROM instructions={len(nodes)}, movements={len(moves)}, bytes={out.stat().st_size}')
