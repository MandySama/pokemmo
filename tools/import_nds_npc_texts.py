"""Import Chinese text-bank entries used by exported ordinary NDS NPCs."""
from pathlib import Path
import argparse
import gzip
import re
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser()
parser.add_argument("source", nargs="?", type=Path, default=Path("E:/PokeMMO/Pokemmo2Pro-Client/data/strings"))
args = parser.parse_args()
root = Path(__file__).resolve().parents[1]
destination = root / "data/strings/strings_zh.xml"
original = destination.read_text(encoding="utf-8")
existing = {int(n.get("id")) for n in ET.fromstring(original).iter("string")}
entries = []
for region, name in [(2, "white"), (3, "platinum"), (5, "heartgold")]:
    source = {(int(n.get("table_id")), int(n.get("entry_id"))): n.text or ""
              for n in ET.parse(args.source / f"ns_strings_zh_{name}.xml").getroot().iter("string")
              if n.get("block_id") == "0"}
    catalog = root / f"protocol/src/main/resources/maps/nds-npcs-{region}.csv.gz"
    with gzip.open(catalog, "rt", encoding="utf-8") as stream:
        rows = [list(map(int, line.split(","))) for line in stream]
    unavailable = set()
    for row in rows:
        text = row[-1]
        if not text:
            continue
        value = source.get((text >> 12 & 4095, text & 4095))
        if value is None or re.search(r"\{[0-9A-F]{2}\}", value):
            unavailable.add(text)
            row[-1] = 0
        elif text not in existing:
            node = ET.Element("string", id=str(text))
            node.text = value
            entries.append("    " + ET.tostring(node, encoding="unicode"))
            existing.add(text)
    if unavailable:
        with gzip.open(catalog, "wt", encoding="utf-8", newline="\n") as stream:
            for row in rows:
                stream.write(",".join(map(str, row)) + "\n")
    print(f"region={region} unsupported translations={len(unavailable)}")
if entries:
    destination.write_text(original.replace("</strings>", "\n".join(entries) + "\n</strings>"), encoding="utf-8")
print(f"Imported {len(entries)} ordinary NPC texts")
