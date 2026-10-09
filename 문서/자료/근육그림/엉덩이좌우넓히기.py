# 엉덩이만 좌우로 넓히기 (홍겸 님 10-10): 엉덩이 높이(y0~y1)에서만, 가운데 골(crack)은 고정,
# 엉덩이 바깥선(= 몸 옆선)을 바깥으로 d 만큼. 엉덩이 아래(허벅지)는 손대지 않음 — 가중이 y1 에서 0.
import sys, json, numpy as np
from PIL import Image
src,dst=sys.argv[1],sys.argv[2]; p=json.loads(sys.argv[3])
a=np.asarray(Image.open(src).convert('RGB')).astype(float); H,W=a.shape[:2]; out=a.copy(); xs=np.arange(W)
y0,y1,crack=p['y0'],p['y1'],p['crack']
def curve(pts,y): ys,vs=zip(*pts); return float(np.interp(y,ys,vs))
for y in range(y0,y1+1):
    w=max(0.0,np.sin(np.pi*(y-y0)/(y1-y0)))**0.8
    L=curve(p['L'],y); R=curve(p['R'],y)
    nl=L-p['dL']*w; nr=R+p['dR']*w
    src_k=[0,L-p['gap'],L,crack,R,R+p['gap'],W-1]; dst_k=[0,L-p['gap'],nl,crack,nr,R+p['gap'],W-1]
    sx=np.interp(xs,dst_k,src_k)
    for c in range(3): out[y,:,c]=np.interp(sx,xs,a[y,:,c])
Image.fromarray(out.clip(0,255).astype(np.uint8)).save(dst)
