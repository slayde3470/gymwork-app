import sys, numpy as np
from PIL import Image
from scipy import ndimage
HEAD_W, HEAD_H = 0.95, 0.95*0.90
SH, WA, HIP = 1.05, 0.95, 1.06
DIP = 0.07
TOP, CHIN, C, BODY0 = 31, 225, 384, 200
HX0, HX1 = 300, 469
def segs(row):
    x=np.where(row)[0]
    if len(x)==0: return []
    s=[];st=x[0];pv=x[0]
    for v in x[1:]:
        if v>pv+1: s.append((st,pv)); st=v
        pv=v
    s.append((st,pv)); return [(p,q) for p,q in s if q-p>2]
def lerp(y,y0,y1,a,b):
    if y<=y0: return a
    if y>=y1: return b
    return a+(b-a)*(y-y0)/(y1-y0)
def torso_f(y):
    return torso_base(y)*(1+DIP*np.exp(-((y-720)/55.0)**2))
def torso_base(y):
    if y<260: return lerp(y,BODY0,260,HEAD_W,1.0)
    if y<499: return lerp(y,260,300,1.0,SH)
    if y<600: return lerp(y,499,600,SH,WA)
    if y<650: return WA
    if y<740: return lerp(y,650,740,WA,HIP)
    if y<780: return HIP
    return lerp(y,780,850,HIP,1.0)
def run(src,dst):
    im=Image.open(src).convert('RGB'); a=np.asarray(im).astype(float)
    m=ndimage.binary_fill_holes(np.asarray(im.convert('L'))<240)
    H,W=m.shape; out=np.full_like(a,255.0); xs=np.arange(W)
    for y in range(BODY0,H):
        sg=segs(m[y]); f=torso_f(y)
        I=[s for s in sg if abs((s[0]+s[1])/2-C)<=210]; L=[s for s in sg if (s[0]+s[1])/2<C-210]; R=[s for s in sg if (s[0]+s[1])/2>C+210]
        if L and R and I:
            Lp,Lq=L[0][0],L[-1][1]; Rp,Rq=R[0][0],R[-1][1]; Ip,Iq=I[0][0],I[-1][1]
            dL=C+((Lp+Lq)/2-C)*SH-(Lp+Lq)/2; dR=C+((Rp+Rq)/2-C)*SH-(Rp+Rq)/2
            src_k=[0,Lp,Lq,Ip,Iq,Rp,Rq,W-1]
            sc=lambda v:C+(v-C)*SH
            dst_k=[0,sc(Lp),sc(Lq),C+(Ip-C)*f,C+(Iq-C)*f,sc(Rp),sc(Rq),W-1]
            sx=np.interp(xs,dst_k,src_k)
        else:
            sx=C+(xs-C)/f
        for ch in range(3): out[y,:,ch]=np.interp(sx,xs,a[y,:,ch],left=255,right=255)
    hb=a[TOP-5:CHIN+1, HX0:HX1]; hh=int(round(hb.shape[0]*HEAD_H)); hw=int(round(hb.shape[1]*HEAD_W))
    hs=np.asarray(Image.fromarray(hb.astype(np.uint8)).resize((hw,hh),Image.LANCZOS)).astype(float)
    y0=CHIN+1-hh; x0=C-int((C-HX0)*HEAD_W)
    reg=out[y0:CHIN+1, x0:x0+hw].copy()
    # 위쪽은 새 머리, 아래 목 근처는 새 머리와 밑그림 중 어두운 쪽
    k=np.clip((np.arange(hh)-(hh-(CHIN-BODY0)-8))/12,0,1)[:,None,None]
    # 머리 양옆 끝은 밑그림으로 서서히
    e=np.clip(np.minimum(np.arange(hw),hw-1-np.arange(hw))/10,0,1)[None,:,None]
    new=hs*(1-k)+np.minimum(reg,hs)*k
    out[y0:CHIN+1, x0:x0+hw]=new*e+reg*(1-e)
    Image.fromarray(out.clip(0,255).astype(np.uint8)).save(dst)
if __name__=='__main__': run(sys.argv[1],sys.argv[2])
