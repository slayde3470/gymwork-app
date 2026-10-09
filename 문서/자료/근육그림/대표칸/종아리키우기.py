# 종아리 키우기 (홍겸 님 10-10 "종아리가 너무 작다"): 다리마다 자기 가운데를 기준으로 가로 배율, 종아리 가장 굵은 곳에서 최대
import sys, numpy as np
from PIL import Image; from scipy import ndimage
src,dst=sys.argv[1],sys.argv[2]; AMT=float(sys.argv[3]); YC=float(sys.argv[4]); SIG=float(sys.argv[5])
a=np.asarray(Image.open(src).convert('RGB')).astype(float); out=a.copy()
m=ndimage.binary_fill_holes(np.asarray(Image.open(src).convert('L'))<240)
W=a.shape[1]; xs=np.arange(W); C=384
def segs(row):
    x=np.where(row)[0]
    if len(x)==0: return []
    s=[];st=x[0];pv=x[0]
    for v in x[1:]:
        if v>pv+1: s.append((st,pv)); st=v
        pv=v
    s.append((st,pv)); return [(p,q) for p,q in s if q-p>2]
for y in range(int(YC-3*SIG),int(YC+3*SIG)):
    if y>=a.shape[0]: break
    f=1+AMT*np.exp(-((y-YC)/SIG)**2)
    L=[s for s in segs(m[y]) if abs((s[0]+s[1])/2-C)<=210]
    if len(L)<2: continue
    (p1,q1),(p2,q2)=L[0],L[-1]
    c1,c2=(p1+q1)/2,(p2+q2)/2
    np1,nq1=c1-(c1-p1)*f,c1+(q1-c1)*f; np2,nq2=c2-(c2-p2)*f,c2+(q2-c2)*f
    src_k=[0,p1,q1,p2,q2,W-1]; dst_k=[0,np1,nq1,np2,nq2,W-1]
    if not (nq1<np2): continue
    sx=np.interp(xs,dst_k,src_k)
    for ch in range(3): out[y,:,ch]=np.interp(sx,xs,a[y,:,ch],left=255,right=255)
Image.fromarray(out.clip(0,255).astype(np.uint8)).save(dst)
