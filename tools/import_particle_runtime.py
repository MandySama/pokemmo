"""Build an isolated CPU particle runtime from a local client build.

Particle classes are copied byte for byte. Adapters exchange viewport dimensions and primitive channels and preserve the
original actor tween factories; no particle equations are replaced.
"""
from pathlib import Path
import argparse, hashlib, json, subprocess, zipfile

parser = argparse.ArgumentParser()
parser.add_argument('client', type=Path)
args = parser.parse_args()
root = Path(__file__).resolve().parents[1]
jar = args.client / 'target/pokemmo-1.0.0-shaded.jar'
base = args.client / 'src/main/java/cn/pokemmo/battle/animation/BaseBattleAnimation.java'
base_text = base.read_text(encoding='utf-8-sig')
pose_path = root / 'tools/battle-runtime/src/top/totodile/particlebridge/PoseBridge.java'
pose_text = pose_path.read_text(encoding='utf-8-sig')
factory_hashes = {}
def method_span(text, name):
    start = text.index('   public final pw_1 ' + name + '(')
    opening = text.index('{', start)
    depth, end = 1, opening + 1
    while depth:
        if text[end] == '{': depth += 1
        if text[end] == '}': depth -= 1
        end += 1
    return start, end
for name in ['EN', 'Xq0', 'WW', 'df0']:
    start, end = method_span(base_text, name)
    method = base_text[start:end]
    factory_hashes[name] = hashlib.sha256(method.encode('utf-8')).hexdigest()
    pose_start, pose_end = method_span(pose_text, name)
    pose_text = pose_text[:pose_start] + method + pose_text[pose_end:]
pose_path.write_text(pose_text, encoding='utf-8')
classes = root / 'target/particle-runtime-classes'
classes.mkdir(parents=True, exist_ok=True)
sources = sorted((root / 'tools/battle-runtime/src').rglob('*.java'))
subprocess.run(['javac', '-encoding', 'UTF-8', '-cp', str(jar), '-d', str(classes), *map(str, sources)], check=True)
log = root / 'target/particle-runtime-audit.log'
audit = subprocess.run(['java', f'-Xlog:class+load=info:file={log.as_posix()}', '-cp', str(classes) + ';' + str(jar),
                        'top.totodile.particlebridge.RuntimeAudit', str(root / 'data/battle/particles.zip'),
                        'moves', str(root / 'target/move-timeline-import/factory-durations.csv')], capture_output=True, text=True)
(root / 'target/particle-runtime-audit.txt').write_text(audit.stdout + audit.stderr, encoding='utf-8')
if audit.returncode:
    raise RuntimeError(audit.stdout + audit.stderr)
loaded = set()
for line in log.read_text(encoding='utf-8').splitlines():
    if '[class,load]' in line and ' source: file:' in line:
        name = line.split('[class,load] ', 1)[1].split(' source:', 1)[0]
        loaded.add(name.replace('.', '/') + '.class')
output = root / 'data/battle/particle-runtime.jar'
hashes = {}
with zipfile.ZipFile(jar) as source, zipfile.ZipFile(output, 'w', zipfile.ZIP_DEFLATED) as target:
    names = set(source.namelist())
    loaded.update(name for name in names if name.endswith('.class') and name.startswith(('com/badlogic/gdx/graphics/g3d/particles/', 'com/badlogic/gdx/math/', 'aurelienribon/tweenengine/equations/')))
    for name in sorted(loaded):
        if name not in names or name == 'f/k3_0.class' or name.endswith('/RuntimeAudit.class'): continue
        data = source.read(name)
        info = zipfile.ZipInfo(name, (2026, 1, 1, 0, 0, 0)); info.compress_type = zipfile.ZIP_DEFLATED
        target.writestr(info, data)
        hashes[name] = hashlib.sha256(data).hexdigest()
    for path in sorted(classes.rglob('*.class')):
        if path.name == 'RuntimeAudit.class': continue
        name = path.relative_to(classes).as_posix()
        info = zipfile.ZipInfo(name, (2026, 1, 1, 0, 0, 0)); info.compress_type = zipfile.ZIP_DEFLATED
        target.writestr(info, path.read_bytes())
manifest = {'source': jar.name, 'sourceSha256': hashlib.sha256(jar.read_bytes()).hexdigest(),
            'poseFactorySourceSha256': hashlib.sha256(base.read_bytes()).hexdigest(), 'poseFactories':factory_hashes,
            'classes': hashes, 'adapterSources': [str(p.relative_to(root)).replace('\\', '/') for p in sources if p.name != 'RuntimeAudit.java']}
(root / 'data/battle/particle-runtime.json').write_text(json.dumps(manifest, indent=2) + '\n', encoding='utf-8')
print(f'Imported {len(hashes)} unmodified classes; runtime {output.stat().st_size} bytes')
for line in audit.stdout.splitlines():
    if line.startswith(('Original pose factories passed=', 'CPU controllers passed=')): print(line)
