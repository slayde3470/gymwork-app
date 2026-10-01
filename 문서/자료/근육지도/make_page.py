# page.tpl + JSON/SVG 파일 → muscle.html (근육지도 아티팩트)
# 사용: python3 make_page.py  (같은 폴더에 page.tpl, hasenheide-body.svg, muscle-catalog.json, color-scales.json, exercise-muscles.json)
import json
s=open('page.tpl',encoding='utf-8').read()
for k,f in [('__SVG__','hasenheide-body.svg'),('__CATALOG__','muscle-catalog.json'),('__SCALES__','color-scales.json'),('__EXERCISES__','exercise-muscles.json')]:
    t=open(f,encoding='utf-8').read().strip()
    s=s.replace(k,json.dumps(t if f.endswith('.svg') else json.loads(t),ensure_ascii=False))
open('muscle.html','w',encoding='utf-8').write(s)
print('ok',len(s))
