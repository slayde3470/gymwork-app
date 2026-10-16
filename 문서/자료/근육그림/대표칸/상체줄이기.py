# 상체(가랑이 위)를 가로 · 세로 S 배로 — 다리는 그대로 (홍겸 님 10-10 "짧뚱 · 상체 5% 작게")
import sys, numpy as np
from PIL import Image; from scipy import ndimage
from 비율고치기 import segs, C
S=float(sys.argv[3]) if len(sys.argv)>3 else 0.95
YA=811          # 가랑이 높이(기준선)
T0,T1=740,811   # 이 사이에서 몸통 가로 배율이 S → 1 로 이어짐
def run(src,dst):
    a=np.asarray(Image.open(src).convert('RGB')).astype(float)
    m=ndimage.binary_fill_holes(np.asarray(Image.open(src).convert('L'))<240)
    H,W=m.shape; xs=np.arange(W)
    # 1) 가로: 원래 행마다
    hz=np.full_like(a,255.0)
    for y in range(H):
        sg=segs(m[y])
        if not sg: continue
        I=[s for s in sg if abs((s[0]+s[1])/2-C)<=210]; L=[s for s in sg if (s[0]+s[1])/2<C-210]; R=[s for s in sg if (s[0]+s[1])/2>C+210]
        if y<T0 or not I:
            f_t=S if y<YA else 1.0
            if L and R and I and y>=T0:
                pass
            else:
                sx=C+(xs-C)/f_t
                for ch in range(3): hz[y,:,ch]=np.interp(sx,xs,a[y,:,ch],left=255,right=255)
                continue
        f_t=S+(1-S)*min(max((y-T0)/(T1-T0),0),1) if y<YA else 1.0
        if L and R:
            Lp,Lq=L[0][0],L[-1][1]; Rp,Rq=R[0][0],R[-1][1]; Ip,Iq=I[0][0],I[-1][1]
            sc=lambda v:C+(v-C)*S
            src_k=[0,Lp,Lq,Ip,Iq,Rp,Rq,W-1]; dst_k=[0,sc(Lp),sc(Lq),C+(Ip-C)*f_t,C+(Iq-C)*f_t,sc(Rp),sc(Rq),W-1]
            sx=np.interp(xs,dst_k,src_k)
        else:
            sx=C+(xs-C)/f_t
        for ch in range(3): hz[y,:,ch]=np.interp(sx,xs,a[y,:,ch],left=255,right=255)
    # 2) 세로: 가랑이 위를 S 배로 눌러 가랑이 쪽으로
    out=np.full_like(a,255.0)
    for yo in range(H):
        ys=yo if yo>=YA else YA-(YA-yo)/S
        if ys<0: continue
        y0=int(np.floor(ys)); t=ys-y0; y1=min(y0+1,H-1)
        out[yo]=hz[y0]*(1-t)+hz[y1]*t
    Image.fromarray(out.clip(0,255).astype(np.uint8)).save(dst)
run(sys.argv[1],sys.argv[2])
