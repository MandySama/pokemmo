"""Compile local move recipes against a recording facade and the original timeline engine."""
from pathlib import Path
import argparse, hashlib, json, re, subprocess

parser = argparse.ArgumentParser()
parser.add_argument('source', type=Path)
args = parser.parse_args()
root = Path(__file__).resolve().parents[1]
work = root / 'target/move-timeline-import'
source_dir = work / 'src'
source_dir.mkdir(parents=True, exist_ok=True)
jar = args.source / 'target/pokemmo-1.0.0-shaded.jar'
recipes = []
hashes = {}
for path in sorted((args.source / 'src/main/java/cn/pokemmo/battle/animation/move').rglob('*.java')):
    text = path.read_text(encoding='utf-8-sig')
    match = re.search(r'技能编号:\s*(\d+)', text)
    move = int(match[1]) if match else {'FairyWindAnimation':584, 'PrecipiceBladesAnimation':619}[path.stem]
    package = re.search(r'package ([\w.]+);', text)[1]
    hashes[str(move)] = hashlib.sha256(path.read_bytes()).hexdigest()
    text = text.replace('import f.MU;', 'import recorder.MU;').replace('f.MU', 'recorder.MU')
    if 'import recorder.MU;' not in text:
        text = text.replace('import f.*;', 'import f.*;\nimport recorder.MU;')
    text = re.sub(r'\blpt4__4\b', 'recorder.WorldCallback', text).replace('import f.recorder.WorldCallback;', '')
    text = text.replace('fv_0.Jn(', 'recorder.Transform.Jn(')
    text = text.replace('tw0_0.PK0.tB0.t3', 'recorder.MU.terrain').replace('this.Vz0.zi0.T0', 'recorder.MU.item')
    target = source_dir / Path(*package.split('.')) / path.name
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(text, encoding='utf-8')
    recipes.append((move, package + '.' + path.stem, target))
assert len(recipes) == 561 and len({entry[0] for entry in recipes}) == 561
facades = sorted((root / 'tools/animation-import/src').rglob('*.java'))
sources = [entry[2] for entry in recipes] + facades
argfile = work / 'sources.txt'
argfile.write_text('\n'.join('"' + path.as_posix() + '"' for path in sources), encoding='utf-8')
manifest = work / 'moves.csv'
manifest.write_text('\n'.join(f'{move},{name}' for move, name, path in sorted(recipes)), encoding='utf-8')
classes = work / 'classes'
classes.mkdir(parents=True, exist_ok=True)
subprocess.run(['javac', '-encoding', 'UTF-8', '-cp', str(jar), '-d', str(classes), '@' + str(argfile)], check=True)
output = root / 'battle/src/main/resources/battle/animations.csv'
variants = output.with_name('animation-variants.csv')
subprocess.run(['java', '-cp', str(classes) + ';' + str(jar), 'recorder.Exporter', str(root / 'data/battle/particles.zip'), str(manifest), str(output), str(variants), str(work / 'factory-durations.csv')], check=True)
catalog = {}
for line in output.read_text(encoding='utf-8').splitlines():
    fields = line.split(',')
    if fields[0] == 'R': continue
    if fields[0] == 'M':
        catalog[int(fields[1])] = {'id':int(fields[1]), 'duration':int(fields[2]), 'commands':[], 'effects':[]}
    elif fields[1] == 'E':
        catalog[int(fields[0])]['effects'].append(dict(zip(['archive','controller','target','start','duration','adjustment','destination','position','travelDuration'], map(float, fields[2:]))))
    else:
        catalog[int(fields[0])]['commands'].append({'op':fields[2], 'start':int(fields[3]), 'args':list(map(float, fields[4:]))})
(root / 'data/battle/animations.json').write_text(json.dumps([catalog[key] for key in sorted(catalog)], separators=(',', ':')) + '\n', encoding='utf-8')
(root / 'data/battle/animation-source.json').write_text(json.dumps({'sourceJarSha256':hashlib.sha256(jar.read_bytes()).hexdigest(), 'recipes':hashes}, indent=2) + '\n', encoding='utf-8')
