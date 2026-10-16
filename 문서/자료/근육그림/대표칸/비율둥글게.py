import numpy as np, json
from PIL import Image; from scipy import ndimage
import 비율고치기 as warp
from 비율고치기 import segs, C
def halfw(f):
    m=ndimage.binary_fill_holes(np.asarray(Image.open(f).convert('L'))<240); d={}
    for y in range(560,880):
        I=[s for s in segs(m[y]) if abs((s[0]+s[1])/2-C)<=210]
        if I: d[y]=((C-I[0][0])+(I[-1][1]-C))/2
    return d
for name in ['front','back']:
    warp.run(f'{name}.png',f'{name}_new.png')
    h=halfw(f'{name}_new.png')
    yw=min(range(590,640),key=lambda y:h[y]); yh=max(range(740,800),key=lambda y:h[y])
    W0,W1=h[yw],h[yh]; extra={}
    for y in range(yw-40,yh+1):
        if y<yw:   # 허리 위쪽은 가장 가는 곳으로 부드럽게 이어짐
            t=(yw-y)/40; tgt=W0+(h[y]-W0)*(1-np.cos(np.pi*t))/2
        else:
            t=(y-yw)/(yh-yw); u=min(t/0.2,1.0); g=u*u*(3-2*u); tgt=W0+(W1-W0)*g*(1-(1-t)**2)
        extra[y]=float(tgt/h[y])
    print(name,yw,W0*2,yh,W1*2)
    warp.run(f'{name}.png',f'{name}_new.png',extra)
    json.dump(extra,open(f'extra_{name}.json','w'))

# 앞모습만: 허리 가장 가는 곳 → 허벅지 원래 폭(840 높이)까지 둥근 곡선 하나 — 홍겸 님 10-10 "허벅지가 더 커 보이지 않게"
ho=halfw('front.png'); warp.run('front.png','front_new.png'); hn=halfw('front_new.png')
yw=min(range(590,640),key=lambda y:hn[y]); W0=hn[yw]; yh=840; W1=ho[yh]; ex={}
for y in range(yw-40,yh+1):
    if y<yw: t=(yw-y)/40; tgt=W0+(hn[y]-W0)*(1-np.cos(np.pi*t))/2
    else:
        t=(y-yw)/(yh-yw); u=min(t/0.2,1.0); g=u*u*(3-2*u); tgt=W0+(W1-W0)*g*(1-(1-t)**2)
    ex[y]=float(tgt/hn[y])
warp.run('front.png','front_new.png',ex)
json.dump(ex,open('extra_front.json','w'))
