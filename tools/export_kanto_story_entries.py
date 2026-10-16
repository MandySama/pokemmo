"""Bind named scene triggers to exported native ROM event addresses."""
from pathlib import Path
import xml.etree.ElementTree as E
import sys
r=E.parse('data/story/kanto-replay.xml').getroot();rows=[x.split(',') for x in Path('target/opening-rom-events.csv').read_text().splitlines()];maps={m.get('key'):(m.get('group'),m.get('map')) for m in r.findall('map')}
order='town_stop lab_introduction choose_starter challenge pickup deliver capture route22_early_rival brock fossil dome_fossil cerulean_rival rocket bill cerulean_rocket misty ss_anne_boarding ss_anne_rival captain surge tower_rival game_corner_rocket lift_key hideout_giovanni erika tower_fuji fuji_flute safari_entry gold_teeth warden safari_prize koga silph_rival lapras_gift silph_giovanni sabrina mansion_key blaine giovanni route22_rival boulder_gate cascade_gate thunder_gate rainbow_gate soul_gate marsh_gate volcano_gate earth_gate lorelei bruno agatha lance champion hall_of_fame'.split()
alias={'leave_before_choice':'choose_starter','choose_again':'choose_starter','escort':'lab_introduction','rival_after':'pickup','dex':'choose_starter','ball':'choose_starter','aide':'choose_starter','mother':'pickup','pokedex':'deliver','go_to_city':'pickup','advice':'capture','no_ball':'capture','completed':'route22_early_rival','helix_fossil':'dome_fossil','fossil_lab':'blaine','vermilion_arrival':'ss_anne_boarding','nurse':'brock'}
out=['# scene,entry,npc,variable,expected,rank,primary']
for s in r.findall('scene'):
 g,m=maps[s.get('map')];node=s.get('node');npc=int(s.get('npc'));cs=[v for v in rows if v[0]=='coord' and v[1:3]==[g,m]];ns=[v for v in rows if v[0]=='npc' and v[1:3]==[g,m] and int(v[3])==npc];ms=[v for v in rows if v[0]=='map' and v[1:3]==[g,m] and v[3]=='2'];var=expected=0
 if node=='escort':continue
 if node=='lab_introduction':v=next(v for v in ms if v[4:6]==['16469','1']);entry=int(v[-1]);var,expected=map(int,v[4:6]);npc=0
 elif node=='pickup':v=next(v for v in ms if v[4:6]==['16471','0']);entry=int(v[-1]);var,expected=map(int,v[4:6]);npc=0
 elif node=='champion':v=next(v for v in ms if v[5]=='0');entry=int(v[-1]);var,expected=map(int,v[4:6]);npc=0
 elif node=='hall_of_fame':v=ms[0];entry=int(v[-1]);var,expected=map(int,v[4:6]);npc=0
 elif node=='bill':
  npc=2;entry=int(next(v[-1] for v in rows if v[0]=='npc' and v[1:4]==[g,m,'2']))
 elif npc and ns and int(ns[0][-1]):entry=int(ns[0][-1])
 else:
  candidates=[v for v in cs if v[3:5]==[s.get('x'),s.get('y')]]
  if node=='town_stop':candidates=[v for v in cs if v[3:5]==['12','1']]
  if node=='vermilion_arrival':candidates=[v for v in cs if v[3:5]==['22','33']]
  if node=='challenge':candidates=[v for v in candidates if v[7]=='3']
  if node=='leave_before_choice':candidates=[v for v in candidates if v[7]=='2']
  if node=='route22_rival':candidates=[v for v in candidates if v[7]=='3']
  if not candidates:raise ValueError(node)
  v=candidates[0];entry=int(v[-1]);var,expected=map(int,v[6:8]);npc=0
 rank=order.index(alias.get(node,node));primary=int(node in order)
 out.append(','.join(map(str,[s.get('id'),entry,npc,var,expected,rank,primary])))
Path('protocol/src/main/resources/maps/kanto-story-entries.csv').write_text('\n'.join(out)+'\n',encoding='utf-8')
print(len(out)-1)

extra=[]
for node,map_id,start_x,start_y in [('lorelei',75,6,11),('bruno',76,6,11),('agatha',77,6,11),('lance',78,23,12)]:
    event=next(v for v in rows if v[0]=='map' and v[1:4]==['1',str(map_id),'2'])
    extra.append(','.join(map(str,['before_'+node,event[-1],1,map_id,start_x,start_y,0,1])))
dimensions={(int(g),int(m)):(int(w),int(h)) for g,m,w,h in (line.split(",") for line in Path("protocol/src/main/resources/maps/fire-red.csv").read_text().splitlines() if line and not line.startswith("#"))}
def npc(after,g,m,identity):
    event=next(v for v in rows if v[0]=='npc' and v[1:4]==[str(g),str(m),str(identity)])
    y=int(event[6])+1;facing=1
    if y>=dimensions[g,m][1]:y=int(event[6])-1;facing=0
    extra.append(','.join(map(str,[after,event[-1],g,m,event[5],y,identity,facing])))
def bg(after,g,m,x,y):
    event=next(v for v in rows if v[0]=='bg' and v[1:5]==list(map(str,[g,m,x,y])))
    extra.append(','.join(map(str,[after,event[-1],g,m,x,y+1,0,1])))
bg('bill',30,0,4,5);npc('bill',30,0,1)
bg('game_corner_rocket',10,14,11,1)
for identity in (3,6,5):npc('game_corner_rocket',1,45,identity)
npc('hideout_giovanni',1,45,2)
for identity in (2,3,4):npc('erika',1,94,identity)
npc('koga',1,51,8)
for identity in (6,7):npc('-',4,3,identity)
rom=Path(sys.argv[1] if len(sys.argv)>1 else 'roms/firered.gba').read_bytes()
for event in rows:
    if event[0]=='bg' and event[1:3]==['9','6'] and rom[int(event[-1]):int(event[-1])+4]==bytes.fromhex('69160880'):
        bg('-',9,6,int(event[3]),int(event[4]))
Path('protocol/src/main/resources/maps/kanto-story-followups.csv').write_text('# after_node,entry,group,map,x,y,npc,facing\n'+'\n'.join(extra)+'\n',encoding='utf-8')
