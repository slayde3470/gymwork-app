# 02 엉덩이: 몸 옆선(파란 곳)은 그대로, 엉덩이 안쪽 패인 그늘(빨간 곳)을 옆선 쪽으로 밀어 엉덩이 면을 넓히고 그늘을 옅게.
import sys, json, numpy as np
from PIL import Image
from scipy import ndimage
src,dst=sys.argv[1],sys.argv[2]; p=json.loads(sys.argv[3])
a=np.asarray(Image.open(src).convert('RGB')).astype(float); H,W=a.shape[:2]; xs=np.arange(W)
g=ndimage.gaussian_filter(a.mean(2),2)
y0,y1,crack,keep=p['y0'],p['y1'],p['crack'],p['keep']
def sil(y,side):
    r=g[y]<249; x=crack
    while 0<x<W-1 and r[x+side]: x+=side
    return x
rows=range(y0,y1+1)
hol={-1:[],1:[]}
for y in rows:
    for s in (-1,1):
        e=sil(y,s); lo,hi=(e+3,e+48) if s<0 else (e-48,e-3)
        hol[s].append(lo+int(np.argmin(g[y,lo:hi])))
for s in (-1,1): hol[s]=ndimage.uniform_filter1d(np.array(hol[s],float),15)
out=a.copy(); M=np.zeros((H,W))
for i,y in enumerate(rows):
    w=max(0.0,np.sin(np.pi*(y-y0)/(y1-y0)))**0.7
    eL,eR=sil(y,-1),sil(y,1); hL,hR=hol[-1][i],hol[1][i]
    nL=hL-(hL-eL)*(1-keep)*w; nR=hR+(eR-hR)*(1-keep)*w
    src_k=[0,eL,hL,crack,hR,eR,W-1]; dst_k=[0,eL,nL,crack,nR,eR,W-1]
    sx=np.interp(xs,dst_k,src_k)
    for c in range(3): out[y,:,c]=np.interp(sx,xs,a[y,:,c])
    for s_,e,n in ((-1,eL,nL),(1,eR,nR)):
        x0,x1=(e+2,int(n)+14) if s_<0 else (int(n)-14,e-2)
        M[y,min(x0,x1):max(x0,x1)+1]=w
body=(g<249).astype(float); body=ndimage.binary_erosion(body,iterations=2).astype(float)
bw=ndimage.gaussian_filter(body,9)+1e-6
blur=np.stack([ndimage.gaussian_filter(out[...,c]*body,9)/bw for c in range(3)],-1)
# 엉덩이 안쪽 밝기를 옆으로: 가로로 더 넓게 흐린 판
bw2=ndimage.gaussian_filter(body,[3,16])+1e-6
blur2=np.stack([ndimage.gaussian_filter(out[...,c]*body,[3,16])/bw2 for c in range(3)],-1)
tgt=0.5*blur+0.5*blur2
Ms=ndimage.gaussian_filter(M,4)*body*p['lift']
out=out*(1-Ms[...,None])+tgt*Ms[...,None]
Image.fromarray(out.clip(0,255).astype(np.uint8)).save(dst)
