from pathlib import Path
import subprocess,json,hashlib
root=Path(__file__).resolve().parents[1]/'data/resources/pokemon-forms'
root.mkdir(parents=True,exist_ok=True)
records=[]
for form in ['megax','megay','gmax']:
    for directory,name in [('ani','front-n'),('ani-back','back-n'),('ani-shiny','front-s'),('ani-back-shiny','back-s')]:
        url=f'https://play.pokemonshowdown.com/sprites/{directory}/charizard-{form}.gif'
        p=root/'6'/form/(name+'.gif');p.parent.mkdir(parents=True,exist_ok=True)
        if not p.exists():
            temp=p.with_suffix('.tmp')
            subprocess.run(['curl.exe','-sS','-L','--fail','--max-time','60','-A','Mozilla/5.0',url,'-o',str(temp)],check=True)
            if not temp.read_bytes().startswith((b'GIF87a',b'GIF89a')): raise ValueError(url)
            temp.replace(p)
        records.append(dict(file=p.relative_to(root).as_posix(),url=url,sha256=hashlib.sha256(p.read_bytes()).hexdigest()))
(root/'sources.json').write_text(json.dumps({'source':'Pokemon Showdown sprite collection','rights':'Pokemon imagery belongs to its respective rights holders; public availability does not establish redistribution permission.','files':records},indent=2)+'\n',encoding='utf-8')
print('Verified',len(records),'GIF files')
