# 엉덩이 좌우로만 둥글게 키우기 (위아래 길이는 그대로). 각 엉덩이 가운데(cx)를 기준으로 가로만 부풀림.
# 세로 가중 = 엉덩이 높이 안에서 sin 모양(위 · 아래 끝은 0) → 위아래는 그대로, 가운데 높이가 가장 넓게(둥근 옆선)
import sys, json, numpy as np
from PIL import Image
from scipy import ndimage
src,dst=sys.argv[1],sys.argv[2]; spec=json.loads(sys.argv[3])
im=Image.open(src); a=np.asarray(im.convert('RGB')).astype(float); H,W=a.shape[:2]
yy,xx=np.mgrid[0:H,0:W].astype(float); sx=xx.copy()
for cx,y0,y1,rx,k in spec:
    t=np.clip((yy-y0)/(y1-y0),0,1); vy=np.sin(np.pi*t)**0.7*((yy>=y0)&(yy<=y1))
    dx=(sx-cx)/rx; g=np.where(np.abs(dx)<1,(1-dx*dx)**2,0.0)
    sx=cx+(sx-cx)*(1-k*g*vy)
out=np.stack([ndimage.map_coordinates(a[...,c],[yy,sx],order=1,mode='nearest') for c in range(3)],-1)
Image.fromarray(out.clip(0,255).astype(np.uint8)).save(dst)
