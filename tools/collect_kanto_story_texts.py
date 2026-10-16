"""Collect localized dialogue for every branch reachable from the Kanto scene catalogue."""
from pathlib import Path
import gzip
import struct
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
source = Path(sys.argv[1]) if len(sys.argv) > 1 else Path('E:/PokeMMO/Pokemmo2Pro-Client/data/strings/strings_zh_dump.xml')
localized = {int(node.get('id')) for node in ET.parse(source).getroot().iter('string') if node.get('id')}
records = [line.split(',') for line in gzip.decompress((ROOT / 'protocol/src/main/resources/maps/gba-opening-program.txt.gz').read_bytes()).decode('utf-8').splitlines()]
code = {int(row[1]): bytes.fromhex(row[2]) for row in records if row[0] == 'code'}
standards = {int(row[1]): int(row[2]) for row in records if row[0] == 'std'}
areas = set()
roots = set()
for path in ('kanto-story-entries.csv', 'kanto-story-followups.csv'):
    for line in (ROOT / 'protocol/src/main/resources/maps' / path).read_text(encoding='utf-8').splitlines():
        if not line or line.startswith('#'):
            continue
        row = line.split(',')
        roots.add(int(row[1]))
        if path.endswith('followups.csv'):
            areas.add((int(row[2]), int(row[3])))
for line in (ROOT / 'protocol/src/main/resources/maps/kanto-story-scenes.csv').read_text(encoding='utf-8').splitlines():
    if line and not line.startswith('#'):
        row = line.split(',')
        areas.add((int(row[1]), int(row[2])))
for row in records:
    if row[0] in ('map', 'coord') and (int(row[1]), int(row[2])) in areas:
        roots.add(int(row[-1]))

pointer = lambda data, offset: struct.unpack_from('<I', data, offset)[0] - 0x08000000
required = {int(row[2]) for row in records if row[0] == 'dexrating'}
visited = set()
queue = list(roots)
while queue:
    address = queue.pop()
    if address in visited or address not in code:
        continue
    visited.add(address)
    data = code[address]
    opcode = data[0]
    offsets = [2] if opcode in (0x0f, 0x85) else [1] if opcode == 0x67 else [6] if opcode == 0x5c and data[1] == 3 else [6, 10] if opcode == 0x5c else []
    for offset in offsets:
        if offset + 4 <= len(data):
            text = pointer(data, offset)
            if text > 0 and text in localized:
                required.add(text)
    if opcode in (4, 5):
        queue.append(pointer(data, 1))
    if opcode in (6, 7):
        queue.append(pointer(data, 2))
    if opcode in (8, 9) and data[1] in standards:
        queue.append(standards[data[1]])
    if opcode == 0x5c and data[1] in (1, 2):
        queue.append(pointer(data, 14))
    if opcode == 0x5c and data[1] in (6, 8):
        queue.append(pointer(data, 18))
    if opcode not in (2, 3, 5, 8, 0x0c, 0x0d):
        queue.append(address + len(data))

output = ROOT / 'target/kanto-story-all-text-ids.txt'
output.write_text('\n'.join(map(str, sorted(required))) + '\n', encoding='utf-8')
print(f'Collected {len(required)} localized dialogue IDs from {len(visited)} reachable instructions')
