# 엉덩이만 둥글게 (홍겸 님 10-10)
#  edge 쪽: 엉덩이 바깥이 곧 몸 옆선 → 엉덩이 높이에서만 옆선을 둥글게 밖으로(d) + 안쪽 패인 그늘을 새 옆선까지 밀고 메움
#  inner 쪽: 엉덩이 바깥이 몸 안의 골(groove) → 몸 옆선(골반 · 앞쪽)은 그대로, 골만 옆선 쪽으로 밀고 메움
import sys, json, numpy as np
from PIL import Image
from scipy import ndimage
src,dst=sys.argv[1],sys.argv[2]; p=json.loads(sys.argv[3])
a=np.asarray(Image.open(src).convert('RGB')).astype(float); H,W=a.shape[:2]; xs=np.arange(W)
g=ndimage.gaussian_filter(a.mean(2),2)
y0,y1,crack=p['y0'],p['y1'],p['crack']
cv=lambda pts,y: float(np.interp(y,*zip(*pts)))
def sil(y,s):
    r=g[y]<249; x=crack
    while 0<x<W-1 and r[x+s]: x+=s
    return x
HOL={}
for side in p['sides']:
    if side['mode']=='edge' and side.get('hollow',True):
        s_=side['s']; hs=[]
        for y in range(y0,y1+1):
            e=cv(side['sil'],y) if 'sil' in side else sil(y,s_)
            lo,hi=(int(e)+3,int(e)+48) if s_<0 else (int(e)-48,int(e)-3)
            hs.append(lo+int(np.argmin(g[y,lo:hi])))
        HOL[s_]=ndimage.median_filter(np.array(hs,float),21); HOL[s_]=ndimage.uniform_filter1d(HOL[s_],15)
out=a.copy(); M=np.zeros((H,W))
for y in range(y0,y1+1):
    w=max(0.0,np.sin(np.pi*(y-y0)/(y1-y0)))**p.get('pow',0.8)
    ks=[(0,0),(W-1,W-1),(crack,crack+p.get('dC',0)*w)]
    for side in p['sides']:
        s=side['s']; e=cv(side['sil'],y) if 'sil' in side else sil(y,s)
        if side['mode']=='edge':
            if side.get('hollow',True):
                h=HOL[s][y-y0]
            else: h=e-s*20
            ne=e+s*side['d']*w; nh=h+(ne-h)*(1-side['keep'])*w
            ks+=[(e+s*side['gap'],e+s*side['gap']),(e,ne),(h,nh)]
            a0,a1=(ne+1,nh-s*18)
        elif side['mode']=='fixed':
            ks+=[(e,e)]; continue
        else:
            gr=cv(side['groove'],y); ng=gr+(e-gr)*(1-side['keep'])*w
            ks+=[(e,e),(gr,ng)]
            a0,a1=(ng+s*18,e-s*1)
        M[y,int(min(a0,a1)):int(max(a0,a1))+1]=max(M[y,int(min(a0,a1))],w)
    ks=sorted(ks,key=lambda k:k[1]); src_k=[k[0] for k in ks]; dst_k=[k[1] for k in ks]
    sx=np.interp(xs,dst_k,src_k)
    for c in range(3): out[y,:,c]=np.interp(sx,xs,a[y,:,c])
body=ndimage.binary_erosion(ndimage.gaussian_filter(out.mean(2),2)<249,iterations=2).astype(float)
bw=ndimage.gaussian_filter(body,9)+1e-6; bw2=ndimage.gaussian_filter(body,[3,16])+1e-6
tgt=0.5*np.stack([ndimage.gaussian_filter(out[...,c]*body,9)/bw for c in range(3)],-1)+0.5*np.stack([ndimage.gaussian_filter(out[...,c]*body,[3,16])/bw2 for c in range(3)],-1)
Ms=ndimage.gaussian_filter(M,4)*body*p['lift']
out=out*(1-Ms[...,None])+tgt*Ms[...,None]
Image.fromarray(out.clip(0,255).astype(np.uint8)).save(dst)
