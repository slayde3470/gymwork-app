# 엉덩이 키우기 — 타원 부풀리기(가운데일수록 크게, 바깥으로 갈수록 0). 원본은 건드리지 않음.
import sys, json, numpy as np
from PIL import Image
from scipy import ndimage
src,dst,spec=sys.argv[1],sys.argv[2],json.loads(sys.argv[3])
im=Image.open(src).convert('RGB'); a=np.asarray(im).astype(float); H,W=a.shape[:2]
yy,xx=np.mgrid[0:H,0:W].astype(float)
sx,sy=xx.copy(),yy.copy()
for cx,cy,rx,ry,k in spec:      # 차례로
    dx=(sx-cx)/rx; dy=(sy-cy)/ry; t2=dx*dx+dy*dy
    g=np.where(t2<1,(1-t2)**2,0.0)
    sx=cx+(sx-cx)*(1-k*g); sy=cy+(sy-cy)*(1-k*g)
out=np.stack([ndimage.map_coordinates(a[...,c],[sy,sx],order=1,mode='nearest') for c in range(3)],-1)
Image.fromarray(out.clip(0,255).astype(np.uint8)).save(dst)
