"""Build numeric species/item mappings from pret/pokefirered constants and installed Showdown."""
import argparse
import json
from pathlib import Path
import re
import subprocess

parser = argparse.ArgumentParser()
parser.add_argument("source", type=Path)
args = parser.parse_args()
root = Path(__file__).resolve().parents[1]
out = root / "map-core/src/main/resources"
out.mkdir(parents=True, exist_ok=True)
national = {}
for line in (root / "tools/encounter-national-species.csv").read_text(encoding="utf-8").splitlines():
    if line and not line.startswith("#"):
        name, number = line.split(",")
        national[name] = int(number)
species = []
for name, number in re.findall(r"#define SPECIES_(\w+)\s+(\d+)\b", (args.source / "include/constants/species.h").read_text()):
    if name in national:
        species.append(f"{number},{national[name]}")
(out / "gba-national-species.csv").write_text("\n".join(species) + "\n")
dex = json.loads(subprocess.check_output(["node", "-e", "const {Dex}=require('./battle-engine/node_modules/pokemon-showdown');console.log(JSON.stringify(Object.fromEntries(Dex.items.all().filter(i=>i.num>0).map(i=>[i.id,i.num]))));"], cwd=root, text=True))
items = ["0,0"]
for name, number in re.findall(r"#define ITEM_(\w+)\s+(\d+)\b", (args.source / "include/constants/items.h").read_text()):
    key = name.lower().replace("_", "")
    if key in dex:
        items.append(f"{number},{dex[key]}")
(out / "gba-held-items.csv").write_text("\n".join(items) + "\n")
print(f"Species mappings: {len(species)}, item mappings: {len(items)}")
