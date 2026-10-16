"""Validate exported GBA dialogue references and import missing localized entries."""
from pathlib import Path
import gzip
import sys
import argparse
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
destination = root / "data/strings/strings_zh.xml"
existing = {int(n.get("id")) for n in ET.parse(destination).getroot().iter("string")}
parser = argparse.ArgumentParser()
parser.add_argument("sources", nargs="+")
parser.add_argument("--ids", type=Path)
args = parser.parse_args()
source = {}
for path in args.sources:
    source.update({int(n.get("id")): n.text or "" for n in ET.parse(path).getroot().iter("string")})
required = set(map(int, args.ids.read_text().splitlines())) if args.ids else set()
with gzip.open(root / "protocol/src/main/resources/maps/gba-opening-program.txt.gz", "rt") as stream:
    for line in stream:
        if args.ids:
            break
        if not line.startswith("code,"):
            continue
        data = bytes.fromhex(line.strip().split(",")[2])
        offsets = [2] if data[0] == 0x0f else [1] if data[0] == 0x67 else [6, 10] if data[0] == 0x5c else []
        for offset in offsets:
            address = int.from_bytes(data[offset:offset + 4], "little") - 0x08000000
            if address > 0:
                required.add(address)
missing = sorted(required - existing)
unavailable = [number for number in missing if number not in source]
if unavailable:
    raise ValueError(f"Missing source translations: {unavailable}")
entries = []
for number in missing:
    node = ET.Element("string", id=str(number))
    node.text = source[number]
    entries.append("    " + ET.tostring(node, encoding="unicode"))
if entries:
    original = destination.read_text(encoding="utf-8")
    destination.write_text(original.replace("</strings>", "\n".join(entries) + "\n</strings>"), encoding="utf-8")
print(f"Validated {len(required)} GBA dialogue IDs; imported {len(entries)}")
