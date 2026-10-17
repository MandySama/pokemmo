"""Read FireRed event tables and native script references into an offline view dataset.

Only exports are written. ROMs, protocol catalogs, and game saves are never modified.
"""
from pathlib import Path
import argparse
import gzip
import hashlib
import json
import re
import struct
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'exports/fire-red-map-viewer'


def export(rom_path, audit):
    rom = rom_path.read_bytes()
    if rom[0xac:0xb0] not in (b'BPRE', b'BPRJ', b'BPRD', b'BPRF', b'BPRI', b'BPRS'):
        raise ValueError('FireRed ROM required')
    u8 = lambda at: rom[at]
    u16 = lambda at: struct.unpack_from('<H', rom, at)[0]
    u32 = lambda at: struct.unpack_from('<I', rom, at)[0]
    ptr = lambda at: u32(at) - 0x08000000
    valid = lambda at: 0 < at < len(rom)
    hx = lambda at: f'0x{at:08X}'
    html = (OUT / 'index.html').read_text(encoding='utf-8')
    maps = json.loads(re.search(r'const maps=(.*?);\s*const \$=', html, re.S)[1])
    texts = {int(e.get('id')): ''.join(e.itertext()) for e in
             ET.parse(ROOT / 'data/strings/strings_zh.xml').getroot().iter('string')}
    table_text = (audit / 'data/script_cmd_table.inc').read_text()
    commands = {int(op, 16): name for name, op in re.findall(r'ScrCmd_(\w+)\s+@ 0x([\da-fA-F]+)', table_text)}
    lengths = list(map(int, (ROOT / 'map-core/src/main/resources/gba-script-lengths.csv').read_text().strip().split(',')))
    for body in re.findall(r'\.macro[^\n]*\n(.*?)\.endm', (audit / 'asm/macros/event.inc').read_text(), re.S):
        match = re.search(r'\.byte\s+0x([\da-fA-F]+)', body)
        if not match or '.if' in body:
            continue
        size = sum({'byte': 1, '2byte': 2, '4byte': 4}[kind] * len(args.strip().split(','))
                   for kind, args in re.findall(r'\.(byte|2byte|4byte)\s+([^\n@]+)', body[match.start():]))
        if size:
            lengths[int(match[1], 16)] = size
    for op in [0x55, 0x53, 0x54]: lengths[op] = 3
    for op in [0x39, 0x3a, 0x3b, 0x3d, 0x3e, 0x3f, 0xd1]: lengths[op] = 8
    for op in [0x7d, 0x7f, 0x80, 0x81, 0x82, 0x83, 0x84]: lengths[op] = 4
    for op, size in {0x7e: 2, 0x85: 6, 0x79: 15, 0xc7: 2}.items(): lengths[op] = size
    standards, movement_names, specials = {}, {}, {}
    source_hash = hashlib.sha256(rom).hexdigest()
    with gzip.open(ROOT / 'protocol/src/main/resources/maps/gba-opening-program.txt.gz', 'rt', encoding='utf-8') as stream:
        for line in stream:
            f = line.strip().split(',')
            if f[0] == 'sha256' and f[1] != source_hash:
                raise ValueError('ROM does not match exported command definitions')
            if f[0] in ('std', 'movement', 'special'):
                (standards if f[0] == 'std' else movement_names if f[0] == 'movement' else specials)[int(f[1])] = int(f[2]) if f[0] == 'std' else f[2]

    nodes, edges, roots, map_nodes, diagnostics = {}, {}, [], {}, []
    def node(id, kind, title, detail=None, **extra):
        nodes.setdefault(id, dict(id=id, kind=kind, title=title, detail=detail or [], **extra))
        return id
    def edge(source, target, relation, label=''):
        id = source + '|' + relation + '|' + target
        edges[id] = dict(id=id, source=source, target=target, relation=relation, label=label or relation)
    def script(at):
        id = 'script:' + hx(at)
        if valid(at): roots.append(at)
        else: node(id, 'unknown', hx(at), ['Invalid script pointer'])
        return id
    def state(kind, address):
        return node(kind + ':' + hx(address), kind, kind + ' ' + hx(address), [hx(address)])

    # Locate the same native map table signature used by the map decoder.
    pattern = bytes.fromhex('80180068890B091808687047')
    hits = [m.start() for m in re.finditer(re.escape(pattern), rom)]
    if len(hits) != 1: raise ValueError('Map table signature is not unique')
    table = ptr(hits[0] + len(pattern))
    for m in maps:
        g, number = int(m['group']), int(m['map'])
        bank = ptr(table + g * 4)
        header = ptr(bank + number * 4)
        key = f'{g}/{number}'
        mid = node('map:' + key, 'map', m['name'] + ' · ' + key,
                   [f'{m["columns"]} × {m["rows"]}', 'header ' + hx(header)], map=key, dimensions=[int(m['columns']), int(m['rows'])])
        map_nodes[key] = [mid]
        def add_event(kind, index, record, title, address):
            id = node(f'{kind}:{key}:{index}', kind, title, [f'{k}: {v}' for k, v in record.items()], map=key, record=record, address=hx(address))
            edge(mid, id, 'contains'); map_nodes[key].append(id); return id
        events = ptr(header + 4)
        if valid(events):
            npc_table = ptr(events + 4)
            for i, record in enumerate(m['npcs']):
                at = npc_table + i * 24
                id = add_event('npc', i, record, f'NPC #{record["id"]} ({record["x"]}, {record["y"]})', at)
                entry = int(record['script_offset'], 16)
                if entry: edge(id, script(entry), 'script')
                if int(record['flag']): edge(id, state('flag', int(record['flag'])), 'visibility')
            warp_table = ptr(events + 8)
            for i, record in enumerate(m['warps']):
                id = add_event('warp', i, record, f'W #{i} ({record["x"]}, {record["y"]})', warp_table + i * 8)
                if record['dynamic_target'] == 'false':
                    destination = f'warp:{record["target_group"]}/{record["target_map"]}:{record["target_warp"]}'
                    edge(id, destination, 'warp')
            coordinate_table = ptr(events + 12)
            for i in range(u8(events + 2)):
                at = coordinate_table + i * 16
                record = dict(x=u16(at), y=u16(at + 2), elevation=u8(at + 4), variable=u16(at + 6), value=u16(at + 8), raw=hx(u32(at + 12)))
                kind = 'coordinate' if u32(at + 12) else 'weather'
                id = add_event(kind, i, record, f'{kind} #{i} ({record["x"]}, {record["y"]})', at)
                if kind == 'coordinate':
                    entry = ptr(at + 12); record['script_offset'] = hx(entry)
                    edge(id, script(entry), 'script'); edge(id, state('variable', record['variable']), 'condition', '= ' + str(record['value']))
            background_table = ptr(events + 16)
            for i in range(u8(events + 3)):
                at = background_table + i * 12; kind = u8(at + 5)
                record = dict(x=u16(at), y=u16(at + 2), elevation=u8(at + 4), kind=kind, raw=hx(u32(at + 8)))
                id = add_event('background', i, record, f'BG #{i} ({record["x"]}, {record["y"]})', at)
                if kind <= 4 and u32(at + 8):
                    entry = ptr(at + 8); record['script_offset'] = hx(entry); edge(id, script(entry), 'script')
                elif kind in (5, 6, 7):
                    record['item'] = u16(at + 8)
                    edge(id, node('item:' + str(record['item']), 'item', 'item ' + str(record['item']), ['Hidden item payload: ' + record['raw']]), 'item')
        for i, record in enumerate(m['map-scripts']):
            id = add_event('map-script', i, record, f'MAP #{i} · type {record["type"]}', header + 8)
            edge(id, script(int(record['script_offset'], 16)), 'script')
            if int(record['variable']): edge(id, state('variable', int(record['variable'])), 'condition', '= ' + record['value'])
        for i, record in enumerate(m['connections']):
            edge(mid, 'map:' + record['target_group'] + '/' + record['target_map'], 'connection:' + str(i), record['direction'] + ' / ' + record['offset'])

    pending, decoded = roots[:], set()
    branch_names = ['<', '==', '>', '<=', '>=', '!=']
    while pending:
        pc = pending.pop()
        if pc in decoded: continue
        decoded.add(pc)
        id = 'script:' + hx(pc)
        if not valid(pc):
            node(id, 'unknown', hx(pc), ['Invalid script pointer']); continue
        op = u8(pc); size = lengths[op]
        if op == 0x5c:
            kind = u8(pc + 1); size = 10 if kind == 3 else 18 if kind in (1, 2, 4, 7) else 22 if kind in (6, 8) else 14
        if size <= 0 or pc + size > len(rom):
            diagnostics.append(hx(pc)); node(id, 'unknown', hx(pc), [f'Unknown width for opcode {op:02X}']); continue
        name = commands.get(op, f'op_{op:02x}')
        detail = [hx(pc) + ' · ' + name, rom[pc:pc + size].hex(' ')]
        node(id, 'instruction', name + ' · ' + hx(pc), detail, address=hx(pc), opcode=op)
        def follow(target, relation, label=''):
            edge(id, 'script:' + hx(target), relation, label); pending.append(target)
        if op in (4, 5): follow(ptr(pc + 1), 'call' if op == 4 else 'goto')
        elif op in (6, 7):
            cond = u8(pc + 1); label = branch_names[cond] if cond < 6 else str(cond)
            follow(ptr(pc + 2), 'conditional-call' if op == 7 else 'branch', label)
            detail.append('comparison result ' + label + ' 0')
        elif op in (8, 9, 10, 11):
            slot = u8(pc + (2 if op in (10, 11) else 1))
            if slot in standards: follow(standards[slot], 'standard', str(slot))
        if op == 0x5c:
            node('trainer:' + str(u16(pc + 2)), 'trainer', 'trainer ' + str(u16(pc + 2)))
            edge(id, 'trainer:' + str(u16(pc + 2)), 'battle')
            if kind in (1, 2, 6, 8): follow(ptr(pc + (14 if kind in (1, 2) else 18)), 'post-battle')
        if op not in (2, 3, 5, 8, 0x0c, 0x0d): follow(pc + size, 'next')
        for offset in ([2] if op in (0x0f, 0x85) else [1] if op == 0x67 else [6, 10] if op == 0x5c else []):
            if offset + 4 > size: continue
            at = ptr(pc + offset)
            if valid(at):
                text = texts.get(at)
                tid = node('text:' + hx(at), 'text', hx(at), [text or 'Chinese text not in language catalog'], textId=at, localized=text is not None)
                edge(id, tid, 'text')
        if op in (0x29, 0x2a, 0x2b, 0xd0):
            edge(id, state('flag', u16(pc + 1)), 'reads' if op == 0x2b else 'writes', name)
        if op in (0x16, 0x17, 0x18, 0x19, 0x1a, 0x21, 0x22):
            a, b = u16(pc + 1), u16(pc + 3)
            edge(id, state('variable', a), 'reads' if op in (0x21, 0x22) else 'writes', name)
            detail.append(f'{hx(a)} , {hx(b)}')
            if op not in (0x16, 0x21) and b >= 0x4000: edge(id, state('variable', b), 'reads', name)
        if op in (0x25, 0x26):
            special = u16(pc + (3 if op == 0x26 else 1))
            edge(id, node('special:' + str(special), 'special', specials.get(special, str(special)), ['Native special; internal function is not decoded']), 'special')
        if op in (0x4f, 0x50):
            at = ptr(pc + 3); movement = []
            if valid(at):
                for index in range(4096):
                    if at + index >= len(rom): break
                    v = u8(at + index)
                    if v == 254: break
                    movement.append(movement_names.get(v, f'0x{v:02X}'))
                else: diagnostics.append('Movement budget: ' + hx(at))
            edge(id, node('movement:' + hx(at), 'movement', hx(at), movement), 'movement')
            detail.append('local actor ' + str(u16(pc + 1)))
            nodes[id]['localActor'] = u16(pc + 1)
        if op in (0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5b, 0x63, 0x65):
            detail.append('local actor ' + str(u16(pc + 1)))
            nodes[id]['localActor'] = u16(pc + 1)
            if op in (0x57, 0x63): detail.append(f'position ({u16(pc + 3)}, {u16(pc + 5)})')
        if len(decoded) > 100000: raise ValueError('Script decoding budget exceeded')

    # Aggregate uninterrupted fallthrough into basic blocks. Branches and calls stay explicit.
    incoming = {}
    outgoing = {}
    for e in edges.values():
        incoming.setdefault(e['target'], []).append(e)
        outgoing.setdefault(e['source'], []).append(e)
    control = {'next', 'goto', 'call', 'branch', 'conditional-call', 'standard', 'post-battle'}
    # Object IDs are map-local. Follow the map's roots only until a warp changes context.
    actor_edges = []
    for key, local_nodes in map_nodes.items():
        actors = {int(nodes[id]['record']['id']): id for id in local_nodes if nodes[id]['kind'] == 'npc'}
        queue = [e['target'] for id in local_nodes for e in outgoing.get(id, []) if e['relation'] == 'script']
        seen = set()
        while queue:
            id = queue.pop()
            if id in seen or id not in nodes: continue
            seen.add(id)
            n = nodes[id]
            if 'localActor' in n and n['localActor'] in actors and n['opcode'] not in (0x50, 0x54, 0x56):
                actor_edges.append((id, actors[n['localActor']]))
            if n.get('opcode') in (0x39, 0x3a, 0x3b, 0x3c, 0x3d, 0x3e, 0x3f): continue
            queue.extend(e['target'] for e in outgoing.get(id, []) if e['relation'] in control)
    for source, target in actor_edges:
        edge(source, target, 'affects', 'map-local object; possible static path')
    starts = {id for id, n in nodes.items() if n['kind'] == 'instruction' and (
        len(incoming.get(id, [])) != 1 or incoming[id][0]['relation'] != 'next')}
    for id, n in nodes.items():
        links = [e for e in outgoing.get(id, []) if e['relation'] in control]
        if any(e['relation'] != 'next' for e in links): starts.update(e['target'] for e in links)
    remap, blocks = {}, {}
    for start in sorted(starts):
        if start in remap or start not in nodes or nodes[start]['kind'] != 'instruction': continue
        current, members, lines = start, [], []
        while current not in remap and nodes.get(current, {}).get('kind') == 'instruction':
            remap[current] = start; members.append(current); lines.extend(nodes[current]['detail'])
            links = [e for e in outgoing.get(current, []) if e['relation'] in control]
            if len(links) != 1 or links[0]['relation'] != 'next' or links[0]['target'] in starts: break
            current = links[0]['target']
        blocks[start] = dict(nodes[start], kind='script', detail=lines, members=members)
    for id, n in list(nodes.items()):
        if n['kind'] == 'instruction' and id not in remap:
            blocks[id] = dict(n, kind='script', members=[id]); remap[id] = id
    result_nodes = {id: n for id, n in nodes.items() if n['kind'] != 'instruction'} | blocks
    result_edges = {}
    for e in edges.values():
        source, target = remap.get(e['source'], e['source']), remap.get(e['target'], e['target'])
        if source == target and e['relation'] == 'next': continue
        new = dict(e, source=source, target=target)
        new['id'] = source + '|' + e['relation'] + '|' + target
        result_edges[new['id']] = new
        if target not in result_nodes: result_nodes[target] = dict(id=target, kind='unknown', title=target, detail=['Unresolved reference'])
    ids_path = ROOT / 'tools/gba_view_ids.json'
    ids = json.loads(ids_path.read_text(encoding='utf-8'))
    payload = dict(version=1, sourceHash=source_hash, nodes=result_nodes, edges=list(result_edges.values()), maps=map_nodes,
                   strings={key: texts[int(value)] for key, value in ids.items()}, diagnostics=diagnostics,
                   coverage=dict(maps=len(maps), instructions=len(decoded), nodes=len(result_nodes), edges=len(result_edges)))
    serialized = json.dumps(payload, ensure_ascii=False, separators=(',', ':')).replace('</', '<\\/')
    (OUT / 'relations-data.js').write_text('const gbaRelations=' + serialized + ';\n', encoding='utf-8')
    for file in (ROOT / 'tools/gba_view').iterdir():
        if file.is_file(): (OUT / file.name).write_bytes(file.read_bytes())
    print(json.dumps(payload['coverage']), 'unresolved widths:', len(diagnostics))


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--rom', type=Path, default=ROOT / 'roms/firered.gba')
    parser.add_argument('--definitions', type=Path, default=ROOT / 'target/firered-script-audit')
    args = parser.parse_args()
    export(args.rom, args.definitions)
