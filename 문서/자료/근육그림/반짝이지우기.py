# 제미나이 ✦ 표시 지우기: 오른쪽 아래 구석에서 주변보다 고르게 밝은 별 모양을 찾아 메움
import sys, numpy as np, cv2
from scipy import ndimage
src,dst=sys.argv[1],sys.argv[2]
im=cv2.imread(src); H,W=im.shape[:2]
g=cv2.cvtColor(im,cv2.COLOR_BGR2GRAY).astype(float)
y0,y1,x0,x1=840,970,840,970
sub=g[y0:y1,x0:x1]
bg=ndimage.median_filter(sub,size=45)
d=sub-bg
m=(d>6)&(sub<250)
lab,n=ndimage.label(m); sizes=ndimage.sum(m,lab,range(1,n+1)); k=int(np.argmax(sizes))+1
star=ndimage.binary_fill_holes(lab==k)
yy,xx=np.mgrid[0:star.shape[0],0:star.shape[1]]
# 별은 가운데에서 네 끝으로 뾰족 — 별 꼭지 사이 거리로 원을 정해 그 밖(근육 하이라이트 줄)은 빼기
cy,cx=904-y0,904-x0
star&=((np.sqrt(np.abs(xx-cx))+np.sqrt(np.abs(yy-cy)))<=np.sqrt(27))
mask=np.zeros((H,W),np.uint8); mask[y0:y1,x0:x1][star]=255
mask=cv2.dilate(mask,np.ones((7,7),np.uint8))
out=cv2.inpaint(im,mask,9,cv2.INPAINT_TELEA)
w=cv2.GaussianBlur(mask.astype(np.float32)/255,(0,0),2)[...,None]
blur=cv2.GaussianBlur(out,(0,0),2.5)
out=(out*(1-w)+blur*w).astype(np.uint8)
cv2.imwrite(dst,out); ys,xs=np.where(star); print(src.split('/')[-1],'별 픽셀',star.sum(),'범위',xs.min()+x0,xs.max()+x0,ys.min()+y0,ys.max()+y0)
