# 엉덩이 둥글게 (홍겸 님 10-10 "납작 · 근육질 → 둥글둥글")
# (1) 부풀리기: 가로 kx · 세로 ky 따로 (세로를 더) (2) 몸 안에서만 잔 결 흐리게 (3) 공 음영: 위 가운데 밝게, 아래 C자 테두리만 그늘
import sys, json, numpy as np
from PIL import Image
from scipy import ndimage
src,dst=sys.argv[1],sys.argv[2]; warp=json.loads(sys.argv[3]); balls=json.loads(sys.argv[4])
a=np.asarray(Image.open(src).convert('RGB')).astype(float); H,W=a.shape[:2]
yy,xx=np.mgrid[0:H,0:W].astype(float); sx,sy=xx.copy(),yy.copy()
for cx,cy,rx,ry,kx,ky in warp:
    dx=(sx-cx)/rx; dy=(sy-cy)/ry; t2=dx*dx+dy*dy; g=np.where(t2<1,(1-t2)**2,0.0)
    sx=cx+(sx-cx)*(1-kx*g); sy=cy+(sy-cy)*(1-ky*g)
a=np.stack([ndimage.map_coordinates(a[...,c],[sy,sx],order=1,mode='nearest') for c in range(3)],-1)
body=(a.mean(2)<238).astype(float); body=ndimage.binary_erosion(body,iterations=3).astype(float)
def nblur(img,s):
    w=ndimage.gaussian_filter(body,s)+1e-6
    return np.stack([ndimage.gaussian_filter(img[...,c]*body,s)/w for c in range(3)],-1)
blur=nblur(a,6)
out=a.copy()
for cx,cy,rx,ry,lx,ly,hi,rimd in balls:
    dx=(xx-cx)/rx; dy=(yy-cy)/ry; r=np.sqrt(dx*dx+dy*dy)
    inner=np.clip((0.92-r)/0.25,0,1)*body                       # 테두리 홈은 남기고 안쪽만
    out=out*(1-inner[...,None]*0.9)+blur*(inner[...,None]*0.9)
    hl=np.exp(-(((dx+lx*0.35)**2+(dy+ly*0.45)**2)/0.22))*body     # 위쪽 하이라이트
    rim=np.exp(-((r-0.98)/0.09)**2)*np.clip(dy+0.2,0,1)*body      # 아래 · 바깥 C자 그늘
    out=out*(1+hi*hl-rimd*rim)[...,None]
Image.fromarray(out.clip(0,255).astype(np.uint8)).save(dst)
