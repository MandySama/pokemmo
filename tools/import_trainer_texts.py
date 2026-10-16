"""Add missing Chinese trainer introduction, defeat and post-battle text without reformatting existing XML."""
from pathlib import Path
import argparse
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser()
parser.add_argument("source", type=Path)
args = parser.parse_args()
root = Path(__file__).resolve().parents[1]
path = root / "data/strings/strings_zh.xml"
existing = {int(n.get("id")) for n in ET.parse(path).getroot().iter("string") if n.get("id")}
translations = {int(n.get("id")): n.text or "" for n in ET.parse(args.source).getroot().iter("string") if n.get("id")}
required = set()
for line in (root / "protocol/src/main/resources/maps/kanto-trainers.csv").read_text().splitlines():
    if line and not line.startswith("#"):
        required.update(int(value) for value in line.split(";")[0].split(",")[5:8] if int(value) > 0)
added = []
for number in sorted(required - existing):
    if number not in translations:
        raise ValueError(f"Missing Chinese trainer text {number}")
    node = ET.Element("string", id=str(number))
    node.text = translations[number]
    added.append("    " + ET.tostring(node, encoding="unicode"))
original = path.read_text(encoding="utf-8")
if added:
    path.write_text(original.replace("</strings>", "\n".join(added) + "\n</strings>"), encoding="utf-8")
print(f"Validated {len(required)} trainer texts; added {len(added)}")
