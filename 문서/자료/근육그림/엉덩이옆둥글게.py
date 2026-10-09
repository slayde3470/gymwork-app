# 엉덩이 좌우 바깥을 볼록하게 다시 칠함: 가운데에서 몸 바깥선까지 밝기가 매끈하게 줄어드는 공 모양 음영 (패인 그늘 없앰)
import sys, json, numpy as np
from PIL import Image
from scipy import ndimage
src,dst=sys.argv[1],sys.argv[2]; spec=json.loads(sys.argv[3])
a=np.asarray(Image.open(src).convert('RGB')).astype(float); H,W=a.shape[:2]
body=(a.mean(2)<238)
bw=ndimage.gaussian_filter(body.astype(float),12)+1e-6
base=np.stack([ndimage.gaussian_filter(a[...,c]*body,12)/bw for c in range(3)],-1)
wmap=np.zeros((H,W)); tgt=np.zeros_like(a)
for item in spec:
    cx,y0,y1,side,lo=item[:5]; mx=item[5] if len(item)>5 else 9999          # side -1 = 왼쪽 바깥, +1 = 오른쪽 바깥 · lo = 바깥선 밝기 비율
    for y in range(y0,y1):
        row=body[y]
        if not row[cx]: continue
        l=cx
        while l>0 and row[l-1]: l-=1
        r=cx
        while r<W-1 and row[r+1]: r+=1
        edge=l if side<0 else r
        if abs(cx-edge)>mx: edge=cx+side*mx
        span=abs(cx-edge)
        if span<8: continue
        vy=np.sin(np.pi*(y-y0)/(y1-y0))**0.8                  # 위아래 끝은 원래대로
        for x in range(min(edge,cx),max(edge,cx)+1):
            t=abs(x-edge)/span                                  # 0 = 바깥선, 1 = 가운데
            f=lo+(1-lo)*np.sin(t*np.pi/2)**0.6                  # 공 단면처럼
            hw=np.clip(t/0.04,0,1)*np.clip((1-t)/0.35,0,1)       # 바깥선 · 가운데 쪽은 서서히
            w=vy*hw
            if w>wmap[y,x]:
                wmap[y,x]=w; tgt[y,x]=base[y,cx]*f
ws=ndimage.gaussian_filter(wmap,3)+1e-6
tgt=np.stack([ndimage.gaussian_filter(tgt[...,c]*wmap,3)/ws for c in range(3)],-1)
wmap=np.clip(ws,0,1)*body
out=a*(1-wmap[...,None])+tgt*wmap[...,None]
# 칠한 곳에 원래 그림의 아주 약한 결만 살짝
Image.fromarray(out.clip(0,255).astype(np.uint8)).save(dst)
