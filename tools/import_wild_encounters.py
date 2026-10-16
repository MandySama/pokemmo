"""Build shared encounter catalogs from regional map definitions."""
from pathlib import Path
import csv, gzip, io, json, re, sys

ROOT = Path(__file__).resolve().parents[1]
SOURCE = Path(sys.argv[1]) if len(sys.argv) > 1 else Path("E:/PokeMMO/Pokemmo2Pro-Server")
OUT = ROOT / "protocol/src/main/resources/maps"
# GBA species constants use internal IDs after 251. Resolve their names to national IDs.
def normalize(value): return re.sub("[^A-Z0-9]", "", value.upper())
national = {normalize(line.split(",")[0]):int(line.split(",")[1])
           for line in (ROOT/"tools/encounter-national-species.csv").read_text().splitlines() if not line.startswith("#")}
species = {}
for name, value in re.findall(r"^#define SPECIES_([A-Z0-9_]+) (\d+)$", (SOURCE / "resource/encounter/species.h").read_text(), re.M):
    native = int(value)
    if native <= 251: species["SPECIES_" + name] = native
    elif normalize(name) in national: species["SPECIES_" + name] = national[normalize(name)]
locations = {}
for line in (OUT.parent / "catalog/pokemon-locations.csv").read_text().splitlines():
    if line.startswith("#") or not line: continue
    r,g,m,n = map(int, line.split(",")); locations[r,g,m] = n
land = [20,20,10,10,10,10,5,5,4,4,1,1]
water = [60,30,5,4,1]
for region, directory, output in [(0,"kanto","fire-red"),(1,"hoenn","emerald"),(3,"sinnoh","sinnoh")]:
    rows=[]; maps=set()
    sinnoh_names = {tuple(map(int,line.split(",")[:2])):int(line.split(",")[4])
                    for line in (OUT/"sinnoh.csv").read_text().splitlines()} if region==3 else {}
    for path in sorted((SOURCE / "resource/map" / directory).rglob("*.json")):
        d=json.loads(path.read_text(encoding="utf-8-sig")); encounters=d.get("wildEncounters",[])
        if not encounters: continue
        g=d["bankId"]-(50 if region==1 else 0); m=d["mapId"]
        selected=[e for e in encounters if region!=0 or e.get("base_label","").endswith("_FireRed")]
        if not selected: continue
        e=selected[0]; cave=int(d.get("mapType")=="UNDERGROUND")
        name=sinnoh_names[g,m] if region==3 else locations[region,g,m]
        fields={f["type"]:f.get("encounter_rates",[]) for f in d.get("encounterFields",[])}
        for key,kind,default in [("land_mons",0,land),("water_mons",3,water),("rock_smash_mons",7,water),("fishing_mons",5,[70,30,60,20,20,40,40,15,4,1])]:
            table=e.get(key)
            if not table or not table.get("encounter_rate"):continue
            weights=fields.get(key) or default
            for i,slot in enumerate(table["mons"]):
                raw=str(slot["species"])
                number=int(raw) if raw.isdigit() else species.get(raw)
                if not number or not 1<=number<=1025:raise ValueError((path,raw,number))
                actual=kind
                if key=="fishing_mons" and len(table["mons"])==10:actual=8 if i<2 else 9 if i<5 else 10
                weight=weights[i] if i<len(weights) else 1
                rows.append([region,g,m,name,actual,-1,-1,table["encounter_rate"],cave,number,slot["min_level"],slot["max_level"],weight])
                maps.add((g,m))
    text=io.StringIO();writer=csv.writer(text,lineterminator="\n");writer.writerow(["# region","group","map","name","kind","season","time","rate","cave","species","min","max","weight"]);writer.writerows(sorted(rows))
    (OUT/(output+"-encounters.csv.gz")).write_bytes(gzip.compress(text.getvalue().encode(),mtime=0))
    print(output,"maps",len(maps),"slots",len(rows))
