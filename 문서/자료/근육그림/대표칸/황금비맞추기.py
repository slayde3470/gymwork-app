# 황금비 맞추기 (홍겸 님 10-10 "짧뚱"): 어깨 x0.90 (키/어깨 ≈ φ²) · 팔 굵기 x0.94 (이두 = 허벅지/φ)
# 전거근(갈비 옆) x0.92 · 허리 x0.95 · 머리 · 목 · 골반 · 다리 그대로. 입력은 상체 5% 줄인 비율기준 그림.
import sys, numpy as np
from PIL import Image; from scipy import ndimage
C=384
SH, ARM, RIB, WAIST = 0.90, 0.94, 0.92, 0.95
NECK0, NECK1 = 262, 330      # 이 사이에서 1.0 → SH (목 · 머리는 그대로)
SPLIT = 515                  # 팔이 몸통에서 떨어지는 줄
WY = 623                     # 허리 가장 가는 줄
HIPY = 700                   # 여기부터 골반 · 다리 그대로
def segs(row):
    x=np.where(row)[0]
    if len(x)==0: return []
    s=[];st=x[0];pv=x[0]
    for v in x[1:]:
        if v>pv+1: s.append((st,pv)); st=v
        pv=v
    s.append((st,pv)); return [(p,q) for p,q in s if q-p>2]
def lerp(y,y0,y1,a,b): return a if y<=y0 else b if y>=y1 else a+(b-a)*(y-y0)/(y1-y0)
def torso_f(y):
    if y<NECK0: return 1.0
    if y<SPLIT: return lerp(y,NECK0,NECK1,1.0,SH)
    if y<WY: return lerp(y,SPLIT,SPLIT+45,SH,RIB) if y<SPLIT+45 else lerp(y,SPLIT+45,WY,RIB,WAIST)
    return lerp(y,WY,HIPY,WAIST,1.0)
def run(src,dst):
    a=np.asarray(Image.open(src).convert('RGB')).astype(float)
    m=ndimage.binary_fill_holes(np.asarray(Image.open(src).convert('L'))<240)
    H,W=m.shape; xs=np.arange(W); out=a.copy()
    for y in range(NECK0,880):
        sg=segs(m[y]); f=torso_f(y)
        I=[s for s in sg if abs((s[0]+s[1])/2-C)<=210]; L=[s for s in sg if (s[0]+s[1])/2<C-210]; R=[s for s in sg if (s[0]+s[1])/2>C+210]
        if L and R and I:
            def arm(p,q):
                cm=(p+q)/2; nc=C+(cm-C)*SH; hw=(q-p)/2*lerp(y,SPLIT,SPLIT+30,SH,ARM); return nc-hw,nc+hw
            Lp,Lq=L[0][0],L[-1][1]; Rp,Rq=R[0][0],R[-1][1]; Ip,Iq=I[0][0],I[-1][1]
            fi=f if y<811 else 1.0
            a1,a2=arm(Lp,Lq); b1,b2=arm(Rp,Rq)
            src_k=[0,Lp,Lq,Ip,Iq,Rp,Rq,W-1]; dst_k=[0,a1,a2,C+(Ip-C)*fi,C+(Iq-C)*fi,b1,b2,W-1]
            sx=np.interp(xs,dst_k,src_k)
        elif y<811:
            sx=C+(xs-C)/f
        else:
            continue
        for ch in range(3): out[y,:,ch]=np.interp(sx,xs,a[y,:,ch],left=255,right=255)
    Image.fromarray(out.clip(0,255).astype(np.uint8)).save(dst)
if __name__=='__main__': run(sys.argv[1],sys.argv[2])
