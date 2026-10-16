"""Rebuild bounded NPC script instruction widths from pinned author-maintained definitions."""
from pathlib import Path
import argparse
import json
import re
import urllib.request
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
PINS = {'pt': '1dc13dcea40c04ab53f1f607de46023dc409ef20', 'hg': '708163e10169173d2bbab252538231721d983155', 'bw': '14f0e908a4dae9650ba1a52fd2b75fcd5ea7a011'}
SOURCES = {
    "pt_macros": ("pret/pokeplatinum", PINS["pt"], "asm/macros/scrcmd.inc"),
    "pt_commands": ("pret/pokeplatinum", PINS["pt"], "include/data/scripts/scrcmd.h"),
    "hg_macros": ("pret/pokeheartgold", PINS["hg"], "asm/macros/script.inc"),
    "hg_names": ("pret/pokeheartgold", PINS["hg"], "tools/py_scripts/scrcmd.json"),
    "bw_widths": ("Skareeg/SDSME", PINS["bw"], "Source/Map_Editor/Resources/ScriptsV/ScriptsBW.resx"),
    "bw_names": ("Skareeg/SDSME", PINS["bw"], "Source/Map_Editor/Resources/ScriptsV/ScriptNamesBW.resx"),
}
parser = argparse.ArgumentParser()
parser.add_argument("--cache", type=Path, default=ROOT / "target/nds-npc-command-sources")
args = parser.parse_args()
args.cache.mkdir(parents=True, exist_ok=True)
source = {}
for name, (repo, revision, path) in SOURCES.items():
    cache = args.cache / name
    if not cache.exists():
        cache.write_bytes(urllib.request.urlopen(f"https://raw.githubusercontent.com/{repo}/{revision}/{path}").read())
    source[name] = cache.read_text(encoding="utf-8-sig")


def macros(text):
    for name, body in re.findall(r"\.macro\s+(\w+)[^\n]*\n(.*?)\.endm", text, re.S):
        lines = [line.split("//")[0].strip() for line in body.splitlines()
                 if line.split("//")[0].strip() and not line.strip().startswith(("#", "/*", "*"))]
        if not lines:
            continue
        first = re.fullmatch(r"\.short\s+(\w+)", lines[0])
        if not first:
            continue
        sizes = []
        for line in lines[1:]:
            field = re.fullmatch(r"\.(byte|short|word|long)\s+(.+)", line)
            if not field:
                break
            sizes.extend([{"byte": 1, "short": 2, "word": 4, "long": 4}[field[1]]] * len(field[2].split(",")))
        else:
            yield first[1], name, sizes

rows = {}
pt_ids = {name: i for i, name in enumerate(re.findall(r"ScriptCommand\((SCRCMD_\w+)", source["pt_commands"]))}
for op, name, sizes in macros(source["pt_macros"]):
    if op in pt_ids:
        rows[(3, pt_ids[op])] = (name, sizes)
hg_names = json.loads(source["hg_names"])["commands"]
for op, name, sizes in macros(source["hg_macros"]):
    try:
        number = int(op, 0)
    except ValueError:
        continue
    if number < len(hg_names):
        rows[(5, number)] = (hg_names[number]["name"], sizes)
bw_names = {int(n.get("name"), 16): n.findtext("value") for n in ET.fromstring(source["bw_names"]).findall("data")}
for node in ET.fromstring(source["bw_widths"]).findall("data"):
    number = int(node.get("name"), 16)
    values = list(map(int, node.findtext("value").split()))
    if values[0] == len(values) - 1:
        rows[(2, number)] = (bw_names.get(number, "Unknown"), values[1:])
output = ROOT / "map-core/src/main/resources/nds"
output.mkdir(parents=True, exist_ok=True)
(output / "npc-script-commands.csv").write_text("".join(f"{r},{op},{name}," + ":".join(map(str, sizes)) + "\n"
    for (r, op), (name, sizes) in sorted(rows.items())), encoding="utf-8")
(output / "npc-script-sources.json").write_text(json.dumps(SOURCES, indent=2) + "\n", encoding="utf-8")
print(f"Generated {len(rows)} exact instruction widths")
