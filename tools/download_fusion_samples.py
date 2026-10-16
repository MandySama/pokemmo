"""下载三款 FusionDex 时装试用素材，保留原文件及作者来源。
不覆盖已有素材；不存在已核实的通用再分发授权。
从项目根目录运行：python tools/download_fusion_samples.py
"""
import urllib.request,re,pathlib,json,hashlib
root=pathlib.Path('data/cosmetics/pokemon');root.mkdir(parents=True,exist_ok=True)
rows=[]
for key,base,donor,artist in [('1.4',1,4,'el_keks'),('6.25',6,25,'phoenix99125'),('25.6',25,6,'toad900')]:
 page='https://www.fusiondex.org/'+key+'/'
 html=urllib.request.urlopen(page).read().decode()
 urls=re.findall(r'https://cdn\.fusiondex\.org/[^"\s<>]+',html)
 url=next(u.replace('&amp;','&') for u in urls if u.endswith('/'+key+'.png'))
 folder=root/key;folder.mkdir(exist_ok=True);file=folder/'front.png'
 raw=file.read_bytes() if file.exists() else urllib.request.urlopen(url).read();assert raw.startswith(b'\x89PNG\r\n\x1a\n')
 if not file.exists():file.write_bytes(raw)
 rows.append(dict(id=key,base=base,donor=donor,artist=artist,page=page,image=url,sha256=hashlib.sha256(raw).hexdigest(),license='No general reuse license verified; author permission not obtained. Local prototype only.',variants='Static normal front only; missing back/shiny/follower use original species.'))
(root/'sources.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2),encoding='utf-8')
print([(r['id'],r['image']) for r in rows])
