"""Add source-backed script inspection to the existing offline map inventory viewer."""
from pathlib import Path
import csv
import gzip
import json
import re
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
VIEW = ROOT / 'exports/fire-red-map-viewer/index.html'
RESOURCE = ROOT / 'protocol/src/main/resources/maps'


def build():
    html = VIEW.read_text(encoding='utf-8')
    maps = json.loads(re.search(r'const maps=(.*?);\s*const \$=', html, re.S)[1])
    strings = {int(e.get('id')): ''.join(e.itertext()) for e in
               ET.parse(ROOT / 'data/strings/strings_zh.xml').getroot().iter('string')}
    code, moves, standards, movement_names = {}, {}, {}, {}
    with gzip.open(RESOURCE / 'gba-opening-program.txt.gz', 'rt', encoding='utf-8') as stream:
        for line in stream:
            f = line.strip().split(',')
            if f[0] in ('code', 'move'):
                (code if f[0] == 'code' else moves)[int(f[1])] = bytes.fromhex(f[2])
            elif f[0] == 'std':
                standards[int(f[1])] = int(f[2])
            elif f[0] == 'movement':
                movement_names[int(f[1])] = f[2]
    audit = ROOT / 'target/firered-script-audit'
    names = {int(op, 16): name for name, op in re.findall(
        r'ScrCmd_(\w+)\s+@ 0x([\da-fA-F]+)',
        (audit / 'data/script_cmd_table.inc').read_text())}
    activity_names = {int(op, 16): name for name, op in re.findall(
        r'#define MOVEMENT_TYPE_(\w+)\s+0x([\da-fA-F]+)',
        (audit / 'include/constants/event_object_movement.h').read_text())}
    catalog = ET.parse(ROOT / 'data/story/kanto-replay.xml').getroot()
    scenes = {s.get('id'): strings.get(int(s.get('name')), s.get('node'))
              for s in catalog.findall('scene')}
    bindings = {}
    for line in (RESOURCE / 'kanto-story-entries.csv').read_text().splitlines():
        if not line or line.startswith('#'):
            continue
        f = line.split(',')
        bindings.setdefault(int(f[1]), []).append({'id': f[0], 'name': scenes.get(f[0], f[0])})
    u16 = lambda d, at: int.from_bytes(d[at:at + 2], 'little')
    ptr = lambda d, at: int.from_bytes(d[at:at + 4], 'little') - 0x08000000
    hx = lambda n: f'0x{n:08X}'
    analyses = {}

    def analyze(start):
        if start in analyses:
            return analyses[start]
        pending, seen, records, texts, missing = [start], set(), [], {}, set()
        while pending:
            pc = pending.pop()
            if pc in seen or pc <= 0:
                continue
            seen.add(pc)
            d = code.get(pc)
            if d is None:
                missing.add(hx(pc))
                continue
            op, next_pc = d[0], pc + len(d)
            edges = []
            if op in (4, 5):
                edges.append(ptr(d, 1))
            elif op in (6, 7):
                edges.append(ptr(d, 2))
            elif op in (8, 9):
                edges.append(standards.get(d[1], 0))
            elif op in (10, 11):
                edges.append(standards.get(d[2], 0))
            if op == 0x5c and d[1] in (1, 2, 6, 8):
                edges.append(ptr(d, 14 if d[1] in (1, 2) else 18))
            if op not in (2, 3, 5, 8, 0x0c, 0x0d):
                edges.append(next_pc)
            pending.extend(edges)
            offsets = [2] if op in (0x0f, 0x85) else [1] if op == 0x67 else [6, 10] if op == 0x5c else []
            for at in offsets:
                address = ptr(d, at)
                if address > 0:
                    texts[address] = {'address': hx(address), 'text': strings.get(address, '未收录中文文本'),
                                      'instruction': hx(pc)}
            # Retain comparisons and branch destinations alongside observable effects.
            if op in (4, 5, 6, 7, 0x16, 0x17, 0x18, 0x19, 0x21, 0x22,
                      0x29, 0x2a, 0x2b, 0x4f, 0x50, 0x53, 0x54, 0x55, 0x56,
                      0x57, 0x58, 0x59, 0x5b, 0x5c, 0x63, 0x65, 0x79, 0x44, 0x45):
                detail = '参数字节 ' + d[1:].hex(' ')
                actor = None
                if op in (0x4f, 0x50):
                    actor = u16(d, 1)
                    sequence = moves.get(ptr(d, 3), b'')
                    detail = f'物件 {actor}：' + ' → '.join(movement_names.get(v, hex(v)) for v in sequence if v != 0xfe)
                elif op in (0x57, 0x63):
                    actor = u16(d, 1)
                    detail = f'物件 {actor} 移至 ({u16(d, 3)}, {u16(d, 5)})'
                elif op in (0x53, 0x55, 0x5b, 0x65):
                    actor = u16(d, 1)
                    detail = f'物件 {actor}；' + detail
                elif op in (6, 7):
                    detail = f'比较结果条件 {d[1]} → {hx(ptr(d, 2))}；否则继续 {hx(next_pc)}'
                elif op in (0x29, 0x2a, 0x2b):
                    detail = f'标记 0x{u16(d, 1):04X}'
                elif op in (0x16, 0x17, 0x18, 0x19, 0x21, 0x22):
                    detail = f'变量/操作数 0x{u16(d, 1):04X}，0x{u16(d, 3):04X}'
                records.append({'address': hx(pc), 'command': names.get(op, hex(op)),
                                'detail': detail, 'actor': actor})
        result = {'texts': list(texts.values()), 'records': records, 'missing': sorted(missing),
                  'scenes': bindings.get(start, []), 'instructions': len(seen)}
        analyses[start] = result
        return result

    for m in maps:
        for npc in m['npcs']:
            npc['movement_name'] = activity_names.get(int(npc['movement']), '未知类型')
        for kind in ('npcs', 'coordinate-events', 'map-scripts'):
            for e in m[kind]:
                start = int(e['script_offset'], 16)
                if start:
                    analyze(start)
    payload = json.dumps({str(k): v for k, v in analyses.items()}, ensure_ascii=False).replace('</', '<\\/')
    map_payload = json.dumps(maps, ensure_ascii=False).replace('</', '<\\/')
    html = re.sub(r'const maps=.*?;\s*const \$=', lambda _: 'const maps=' + map_payload + ';\nconst $=', html, count=1, flags=re.S)
    # Keep the original inventory, map images, navigation, and marker controls.
    html = re.sub(r'\n<!-- inspection-extension -->.*?<!-- /inspection-extension -->', '', html, flags=re.S)
    extension = (ROOT / 'tools/fire_red_inspection.js').read_text(encoding='utf-8')
    editor_ids = json.loads((ROOT / 'tools/gba_view_ids.json').read_text(encoding='utf-8'))
    extension = 'const relationViewLabel=' + json.dumps(strings[editor_ids['openView']], ensure_ascii=False) + ';\n' + extension
    html = html.replace('</body>', '') if '</body>' in html else html
    block = '\n<!-- inspection-extension --><script>\nconst scriptDetails=' + payload + ';\n' + extension + '\n</script><!-- /inspection-extension -->\n'
    html = html.replace('</html>', block + '</html>')
    VIEW.write_text(html, encoding='utf-8')
    print(f'Updated {len(maps)} maps; {len(analyses)} script entries; '
          f'{sum(bool(a["texts"]) for a in analyses.values())} entries with text references')


if __name__ == '__main__':
    build()
