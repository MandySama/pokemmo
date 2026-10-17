"""Export ROM experience yields and trainer money multipliers, without ROM pixels."""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
rows = ['# kind,region,id,value; ROM base EXP and single trainer prize per last-party level']
for region, game, source in [(0, 'firered', 'firered-script-audit'), (1, 'emerald', 'emerald-source')]:
    rom = (ROOT / 'roms' / (game + '.gba')).read_bytes()
    audit = ROOT / 'target' / source
    constants = {n: int(v, 0) for n, v in re.findall(r'#define\s+(TRAINER_CLASS_\w+)\s+(0x[0-9A-Fa-f]+|\d+)\b', (audit / 'include/constants/trainers.h').read_text(encoding='utf-8'))}
    table = (audit / 'src/battle_main.c').read_text(encoding='utf-8').split('gTrainerMoneyTable[] =', 1)[1].split('};', 1)[0]
    pairs = [(constants[n], int(v)) for n, v in re.findall(r'\{(TRAINER_CLASS_\w+),\s*(\d+)\}', table)]
    pattern = bytes(v for pair in pairs for v in (*pair, 0, 0))
    assert rom.count(pattern) == 1, 'Money table must match ROM'
    rates = dict(pairs)
    anchor = 102 if region == 0 else 318
    name = bytes.fromhex('ccc3bdc5ff') if region == 0 else bytes.fromhex('bdbbc6d0c3c8ff')
    candidates = [m.start() - anchor * 40 - 4 for m in re.finditer(re.escape(name), rom)]
    candidates = [p for p in candidates if p >= 0 and rom[p+32] == 0 and rom[p + (anchor+1)*40+32] > 0 and rom[p + anchor*40+32] == (2 if region == 0 else 1) and 0x08000000 <= int.from_bytes(rom[p+anchor*40+36:p+anchor*40+40], 'little') < 0x08000000+len(rom)]
    assert len(candidates) == 1
    trainers = candidates[0]
    catalog = ROOT / 'protocol/src/main/resources/maps' / ('gba-opening-trainers.csv' if region == 0 else 'gba-hoenn-trainers.csv')
    ids = {int(line.split(';')[0].split(',')[3]) for line in catalog.read_text(encoding='utf-8').splitlines() if line and not line.startswith('#')}
    if region == 0:
        ids |= {int(line.split(';')[0].split(',')[3]) for line in (catalog.parent/'kanto-trainers.csv').read_text(encoding='utf-8').splitlines() if line and not line.startswith('#')}
    for id in sorted(ids):
        cls = rom[trainers + id*40 + 1]
        assert cls in rates, (region, id, cls)
        rows.append(f'money,{region},{id},{4*rates[cls]}')
    signature = bytes([45,49,49,45,65,65,12,3,45,64])
    assert rom.count(signature) == 1
    species = rom.index(signature) - 28
    for line in (ROOT/'map-core/src/main/resources/gba-national-species.csv').read_text(encoding='utf-8').splitlines():
        raw, national = map(int, line.split(','))
        if 1 <= national <= 386:
            rows.append(f'exp,{region},{national},{rom[species+raw*28+9]}')
(ROOT/'protocol/src/main/resources/catalog/battle-rewards.csv').write_text('\n'.join(rows)+'\n', encoding='utf-8')
print(f'Exported {len(rows)-1} ROM reward records')

