"""Build a local map explorer without changing the inventory or relation workspace."""
from pathlib import Path
import argparse
import gzip
import hashlib
import json
import re
import shutil
import subprocess
import xml.etree.ElementTree as ET
from kanto_explorer_semantics import build_semantics

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'exports/fire-red-map-viewer'
SOURCE = ROOT / 'tools/kanto_explorer'


def build(rom):
    html = (OUT / 'index.html').read_text(encoding='utf-8')
    maps = json.loads(re.search(r'const maps=(.*?);\s*const \$=', html, re.S)[1])
    details = json.loads(re.search(r'const scriptDetails=(.*?);\s*const relationViewLabel=', html, re.S)[1])
    code, moves, names, standards = {}, {}, {}, {}
    digest = hashlib.sha256(rom.read_bytes()).hexdigest()
    with gzip.open(ROOT / 'protocol/src/main/resources/maps/gba-opening-program.txt.gz', 'rt', encoding='utf-8') as stream:
        for line in stream:
            parts = line.strip().split(',')
            if parts[0] == 'sha256' and parts[1] != digest:
                raise ValueError('ROM does not match the script program')
            if parts[0] in ('code', 'move'):
                (code if parts[0] == 'code' else moves)[parts[1]] = list(bytes.fromhex(parts[2]))
            elif parts[0] == 'movement':
                names[parts[1]] = parts[2]
            elif parts[0] == 'std':
                standards[parts[1]] = int(parts[2])
    strings = {int(e.get('id')): ''.join(e.itertext()) for e in
               ET.parse(ROOT / 'data/strings/strings_zh.xml').getroot().iter('string')}
    ids = json.loads((SOURCE / 'strings.json').read_text(encoding='utf-8'))
    relations = (OUT / 'relations-data.js').read_text(encoding='utf-8')
    relations = json.loads(relations[len('const gbaRelations='):].strip().removesuffix(';'))
    if relations['sourceHash'] != digest:
        raise ValueError('ROM does not match the event export')
    by_map = {m['group'] + '/' + m['map']: m for m in maps}
    for m in maps:
        m['background'] = []
    for n in relations['nodes'].values():
        if n['kind'] == 'background':
            by_map[n['map']]['background'].append(dict(n['record'], index=n['id'].rsplit(':', 1)[1]))
    semantic_labels = {k: strings[v] for k,v in ids.items()}
    semantics = build_semantics(ROOT, maps, code, standards, strings, semantic_labels)
    # Use the production decoder, so thumbnail hit targets share its coordinate system.
    subprocess.run(['java', '-cp', str(ROOT / 'map-core/target/classes'),
                    str(ROOT / 'tools/ExportKantoOverview.java'), str(rom), str(OUT)], check=True)
    payload = dict(maps=maps, details=details, code=code, moves=moves, movementNames=names,
                   standards=standards, texts={str(k): v for k, v in strings.items()},
                   labels=semantic_labels, semantics=semantics, sourceHash=digest,
                   positions=json.loads((OUT / 'kanto-positions.json').read_text()))
    data = json.dumps(payload, ensure_ascii=False, separators=(',', ':')).replace('</', '<\\/')
    (OUT / 'explorer-data.js').write_text('const kantoData=' + data + ';\n', encoding='utf-8')
    for name in ('explorer.html', 'explorer.css', 'explorer.js', 'explorer-semantics.js'):
        shutil.copyfile(SOURCE / name, OUT / name)
    print(f'Exported {len(maps)} maps, {len(code)} instructions, {len(moves)} movements')
    print('Semantic coverage:', semantics['coverage'])


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--rom', type=Path, default=ROOT / 'roms/firered.gba')
    build(parser.parse_args().rom.resolve())
