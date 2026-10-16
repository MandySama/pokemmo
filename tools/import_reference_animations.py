"""Import particle assets, compile move timelines, then build the isolated CPU runtime."""
from pathlib import Path
import argparse, subprocess, sys, zipfile

parser = argparse.ArgumentParser()
parser.add_argument('source', type=Path)
args = parser.parse_args()
root = Path(__file__).resolve().parents[1]
folder = root / 'data/battle'
folder.mkdir(parents=True, exist_ok=True)
with zipfile.ZipFile(args.source / 'data/sprites/particles.pak') as source:
    entries = {name:source.read(name) for name in source.namelist() if not name.endswith('/')}
for path in (args.source / 'data/sprites/particles').rglob('*'):
    if path.is_file(): entries[path.relative_to(args.source / 'data/sprites/particles').as_posix()] = path.read_bytes()
with zipfile.ZipFile(folder / 'particles.zip', 'w', zipfile.ZIP_DEFLATED) as output:
    for name in sorted(entries):
        info = zipfile.ZipInfo(name, (2026, 1, 1, 0, 0, 0))
        info.compress_type = zipfile.ZIP_DEFLATED
        output.writestr(info, entries[name])
for tool in ['import_move_timelines.py', 'import_particle_runtime.py']:
    subprocess.run([sys.executable, str(root / 'tools' / tool), str(args.source)], check=True)
print('Imported all move assets, timelines, and CPU runtime')
