"""Import named Kanto scenes, replay actions and server checkpoint locations."""
from pathlib import Path
import sys, json, xml.etree.ElementTree as ET
ROOT = Path(__file__).resolve().parents[1]
SOURCE = Path(sys.argv[1]) if len(sys.argv) > 1 else Path("E:/PokeMMO/Pokemmo2Pro-Server")
sys.path.insert(0, str(SOURCE / "tools/web/admin_lib"))
from jsonc_io import loads, to_plain

def read(path): return to_plain(loads(path.read_text(encoding="utf-8-sig")))

def encode(parent, tag, value):
    node = ET.SubElement(parent, tag)
    if isinstance(value, dict):
        for key, item in value.items():
            if isinstance(item, (dict, list)): encode(node, key, item)
            else: node.set(key, str(item).lower() if isinstance(item, bool) else str(item))
    elif isinstance(value, list):
        for item in value:
            if isinstance(item, (dict, list)): encode(node, "entry", item)
            else: ET.SubElement(node, "entry", value=str(item))
    return node

NAMES = {
"town_stop":"博士拦路与带路", "escort":"跟随博士前往研究所", "lab_introduction":"研究所介绍初始宝可梦",
"choose_starter":"领取初始宝可梦", "challenge":"首次劲敌战", "leave_before_choice":"选择前尝试离开研究所",
"mother":"妈妈的问候与治疗", "choose_again":"博士提示选择初始宝可梦", "aide":"研究所助手说明", "ball":"研究所精灵球说明", "dex":"研究所图鉴说明", "rival_after":"开场完成后的劲敌对话",
"pickup":"领取博士的包裹", "deliver":"向博士交付包裹", "pokedex":"领取图鉴和精灵球", "advice":"博士的旅行建议", "go_to_city":"博士提示前往常磐市",
"capture":"老人捕获教学", "completed":"捕获教学完成后对话", "no_ball":"没有精灵球时的教学提示", "nurse":"宝可梦中心治疗",
"brock":"挑战小刚", "route22_early_rival":"22号道路初次劲敌战", "fossil":"月见山化石争夺战", "cerulean_rival":"华蓝市劲敌战", "cerulean_rocket":"追回火箭队抢走的招式机", "misty":"挑战小霞", "rocket":"金珠桥火箭队招募", "bill":"帮助比尔恢复人形", "ss_anne_rival":"圣安奴号劲敌战", "captain":"船长传授一字斩", "surge":"挑战马志士", "ss_anne_boarding":"登上圣安奴号", "vermilion_arrival":"抵达枯叶市",
"tower_fuji":"幽灵塔救出富士老人", "tower_rival":"幽灵塔劲敌战", "dome_fossil":"选择甲壳化石", "helix_fossil":"选择菊石化石", "fossil_lab":"复活化石宝可梦", "fuji_flute":"领取宝可梦之笛", "hideout_giovanni":"火箭队基地坂木战", "lift_key":"取得电梯钥匙", "game_corner_rocket":"发现游戏厅秘密入口", "erika":"挑战莉佳", "warden":"归还金牙并领取怪力", "gold_teeth":"取得金牙", "safari_entry":"进入野生原野区", "safari_prize":"秘密小屋领取冲浪", "koga":"挑战阿桔", "sabrina":"挑战娜姿", "silph_giovanni":"西尔佛公司坂木战", "lapras_gift":"领取拉普拉斯", "silph_rival":"西尔佛公司劲敌战", "mansion_key":"取得宝可梦屋秘密钥匙", "blaine":"挑战夏伯", "giovanni":"挑战常磐道馆坂木", "route22_rival":"联盟前22号道路劲敌战", "boulder_gate":"灰色徽章检查", "cascade_gate":"蓝色徽章检查", "earth_gate":"绿色徽章检查", "marsh_gate":"金色徽章检查", "rainbow_gate":"彩虹徽章检查", "soul_gate":"粉红徽章检查", "thunder_gate":"橙色徽章检查", "volcano_gate":"深红徽章检查", "lorelei":"挑战四天王科拿", "bruno":"挑战四天王希巴", "agatha":"挑战四天王菊子", "lance":"挑战四天王阿渡", "champion":"冠军劲敌战", "hall_of_fame":"进入名人堂"
}
TITLES = {"PalletTown":"真新镇", "PalletTown_PlayersHouse_1F":"真新镇·主角家一楼", "PalletTown_ProfessorOaksLab":"真新镇·大木研究所", "ViridianCity":"常磐市", "ViridianCity_Mart":"常磐市·友好商店", "ViridianCity_Gym":"常磐市·道馆", "CeruleanCity":"华蓝市", "CeruleanCity_Gym":"华蓝市·道馆", "CeladonCity_GameCorner":"玉虹市·游戏厅", "CeladonCity_Gym":"玉虹市·道馆", "CinnabarIsland_Gym":"红莲岛·道馆", "CinnabarIsland_PokemonLab_ExperimentRoom":"红莲岛·化石研究室", "FuchsiaCity_Gym":"浅红市·道馆", "FuchsiaCity_SafariZone_Entrance":"浅红市·原野区入口", "FuchsiaCity_WardensHouse":"浅红市·园长之家", "LavenderTown_VolunteerPokemonHouse":"紫苑镇·富士老人之家", "MtMoon_B2F":"月见山·地下二层", "PewterCity_Gym":"尼比市·道馆", "PokemonMansion_B1F":"宝可梦屋·地下一层", "PokemonTower_2F":"幽灵塔·二层", "PokemonTower_7F":"幽灵塔·七层", "RocketHideout_B4F":"火箭队基地·地下四层", "Route22":"22号道路", "Route22_NorthEntrance":"22号道路·联盟入口", "Route23":"23号道路", "Route24":"24号道路", "Route25_SeaCottage":"25号道路·比尔之家", "SSAnne_2F_Corridor":"圣安奴号·二层走廊", "SSAnne_CaptainsOffice":"圣安奴号·船长室", "SafariZone_SecretHouse":"野生原野区·秘密小屋", "SafariZone_West":"野生原野区·西区", "SaffronCity_Gym":"金黄市·道馆", "SilphCo_11F":"西尔佛公司·十一层", "SilphCo_7F":"西尔佛公司·七层", "VermilionCity":"枯叶市", "VermilionCity_Gym":"枯叶市·道馆", "PokemonLeague_AgathasRoom":"宝可梦联盟·菊子房间", "PokemonLeague_BrunosRoom":"宝可梦联盟·希巴房间", "PokemonLeague_ChampionsRoom":"宝可梦联盟·冠军房间", "PokemonLeague_HallOfFame":"宝可梦联盟·名人堂", "PokemonLeague_LancesRoom":"宝可梦联盟·阿渡房间", "PokemonLeague_LoreleisRoom":"宝可梦联盟·科拿房间"}
strings_path = ROOT / "data/strings/strings_zh.xml"
strings = ET.parse(strings_path).getroot()
by_id = {int(n.get("id")):n for n in strings.findall("string")}
# Import only referenced translated text; preserve existing project text.
translations = {}
for path in Path("E:/PokeMMO/Pokemmo2Pro-Client/data/strings").glob("*.xml"):
    try:
        for n in ET.parse(path).getroot().iter("string"):
            if n.get("id"): translations[int(n.get("id"))] = n.text or ""
    except ET.ParseError: pass
next_id = 980000
labels = {n.text: int(n.get("id")) for n in strings.findall("string") if 980000 <= int(n.get("id")) < 990000}
def label(text):
    global next_id
    if text in labels:return labels[text]
    while next_id in by_id:next_id+=1
    node=ET.SubElement(strings,"string", id=str(next_id));node.text=text
    by_id[next_id]=node;labels[text]=next_id;next_id+=1
    return next_id-1

root = ET.Element("replay-catalog")
map_nodes={}; map_data={}
for path in sorted((SOURCE / "resource/map/kanto").rglob("*.json")):
    d=read(path);key=path.stem;map_data[key]=d
    title=TITLES.get(key)
    if title is None:
        location=by_id.get(139912+int(d.get("unknownByte",0)))
        title=(location.text.split("(", 1)[0] if location is not None else key)
        if key.endswith("PokemonCenter_1F"):title += "·宝可梦中心"
    m=ET.SubElement(root,"map", key=key, group=str(d["bankId"]), map=str(d["mapId"]), name=str(label(title)), width=str(d["width"]), height=str(d["height"]))
    encode(m,"npcs",d.get("npcs",[]));map_nodes[key]=m

menu=ET.Element("story-commands")
existing_menu=ROOT/"data/maps/story-commands.xml"
if existing_menu.exists():
    for group in ET.parse(existing_menu).getroot().findall("map"):
        if group.get("region") != "0":menu.append(group)

menu_maps={}; entries=0
chapter_order = {name: i for i, name in enumerate(("pallet_town", "oak_parcel", "catch_tutorial", "pokemon_center_nurse", "early_route_to_vermilion", "mainline_to_champion"))}
for path in sorted((SOURCE / "resource/story/kanto").rglob("*.jsonc"), key=lambda p: chapter_order[p.parent.name]):
    d=read(path);ch=ET.SubElement(root,"chapter", id=d["id"])
    for field in ("actors","resources","text"):encode(ch,field,d.get(field,{}))
    nodes=ET.SubElement(ch,"nodes")
    for name,n in d["nodes"].items():
        nd=encode(nodes,"node",n);nd.set("id",name)
        for action in nd.find("actions"):
            if action.get("message"):
                message = action.attrib.pop("message")
                if "真新镇开场剧情已完成" in message:
                    message = "本次重播已完成，临时状态将随重播结束丢弃。"
                action.set("textId",str(label(message)))
    for value in d.get("text",{}).values():
        if value not in by_id:
            if value not in translations:raise ValueError(("Missing text",value,path))
            n=ET.SubElement(strings,"string",id=str(value));n.text=translations[value];by_id[value]=n
    for starter in d.get("resources",{}).get("starters",[]):
        value=starter["prompt"]
        if value not in by_id:
            if value not in translations:raise ValueError(("Missing starter text",value))
            n=ET.SubElement(strings,"string",id=str(value));n.text=translations[value];by_id[value]=n
    seen=set()
    for t in d["triggers"]:
        targets=[t["map"]] if "map"in t else [key for key in map_data if key.endswith(t["mapSuffix"])]
        for key in targets:
            name=t["startNode"]
            if (key,name) in seen:continue
            seen.add((key,name))
            m=map_nodes[key];md=map_data[key];npc=None
            if "entityIdx"in t:npc=next((n for n in md.get("npcs",[]) if n["entityIdx"]==t["entityIdx"]),None)
            elif "scriptSuffix" in t:npc=next((n for n in md.get("npcs",[]) if n.get("script", "").endswith(t["scriptSuffix"])),None)
            elif t["type"] == "MAP_READY":
                actor_name = next((a.get("actor") for a in d["nodes"][name]["actions"] if a.get("actor")), None)
                actor = d.get("actors", {}).get(actor_name, {})
                if actor.get("map") == key:npc=next((n for n in md.get("npcs",[]) if n["entityIdx"]==actor["entityIdx"]),None)
            if "script"in t:
                coord=next((c for c in md.get("coordScripts",[]) if c["script"]==t["script"]),{})
            else:coord=t
            if npc:x,y=npc["x"],min(md["height"]-1,npc["y"]+1)
            else:x,y=coord.get("x",md["width"]//2),coord.get("y",md["height"]//2)
            if name == "lab_introduction":x,y=6,11
            entry_id=d["id"]+"/"+key+"/"+name
            entry=ET.SubElement(root,"scene", id=entry_id, chapter=d["id"], map=key, node=name, name=str(label(NAMES[name])), x=str(x),y=str(y), npc=str(npc["entityIdx"]+1 if npc else 0))
            encode(entry,"conditions",t.get("conditions",[]))
            if key not in menu_maps:
                menu_maps[key]=ET.SubElement(menu,"map",region="0",group=m.get("group"),map=m.get("map"),name=m.get("name"))
            if name == "escort":continue
            story=ET.SubElement(menu_maps[key],"story",name=entry.get("name"),scene=entry_id)
            ET.SubElement(story,"command",action="4",name="9530")
            ET.SubElement(story,"command",action="2",name="9562")
            entries+=1
    encode(ch,"triggers",d["triggers"])
# Remove non-scene maps' redundant NPC data only when never used by chapter actors or map actions.
needed={s.get("map")for s in root.findall("scene")}
for chapter in root.findall("chapter"):
    for node in chapter.iter():
        if node.get("map"):needed.add(node.get("map"))
for m in list(root.findall("map")):
    if m.get("key") not in needed:root.remove(m)
for tree,path in ((root,ROOT/"data/story/kanto-replay.xml"),(menu,ROOT/"data/maps/story-commands.xml")):
    ET.indent(tree,space="    ");ET.ElementTree(tree).write(path,encoding="utf-8",xml_declaration=True)
maps_by_key = {m.get("key"): m for m in root.findall("map")}
scene_rows = ["# scene,group,map,pretrigger_x,pretrigger_y"]
for scene in root.findall("scene"):
    m = maps_by_key[scene.get("map")]
    y = "2" if scene.get("node") == "town_stop" else scene.get("y")
    scene_rows.append(",".join((scene.get("id"), m.get("group"), m.get("map"), scene.get("x"), y)))
(ROOT / "protocol/src/main/resources/maps/kanto-story-scenes.csv").write_text("\n".join(scene_rows) + "\n", encoding="utf-8")
# Append only newly generated language entries without rewriting existing file formatting.
original=strings_path.read_text(encoding="utf-8")
old_ids={int(n.get("id"))for n in ET.fromstring(original).findall("string")}
added="\n".join("    "+ET.tostring(n,encoding="unicode").strip()for n in strings.findall("string")if int(n.get("id"))not in old_ids)
if added: strings_path.write_text(original.replace("</strings>",added+"\n</strings>"),encoding="utf-8")
print(f"Imported {entries} named scene entries in {len(menu_maps)} maps, {len(root.findall('chapter'))} chapters")
