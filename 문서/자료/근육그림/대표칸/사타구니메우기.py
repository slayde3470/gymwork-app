# 앞모습 사타구니 볼록한 곳을 평평하고 매끈하게 메움 (홍겸 님 10-10)
# 테두리(위 · 좌 · 우) 색을 안쪽으로 이어 칠하고, 아래로 갈수록 살짝 어둡게(가랑이 쪽 자연스러운 그늘)
import sys, numpy as np, cv2
src,dst,cx,cy,rx,ry=sys.argv[1],sys.argv[2],*map(float,sys.argv[3:7])
im=cv2.imread(src).astype(np.float32)
H,W=im.shape[:2]
yt,yb=int(cy-ry),int(cy+ry*0.85); xl,xr=int(cx-rx),int(cx+rx)
fill=im.copy()
top=im[yt-2,xl:xr+1]                     # 위 테두리
for y in range(yt,yb+1):
    t=(y-yt)/(yb-yt)
    left=im[y,xl-2]; right=im[y,xr+2]
    for x in range(xl,xr+1):
        u=(x-xl)/(xr-xl)
        v=top[x-xl]*(1-0.18*t)            # 위에서 내려오며 살짝 어둡게
        h=left*(1-u)+right*u
        wv=np.sin(np.pi*u)                # 가운데는 위 색, 가장자리는 옆 색
        fill[y,x]=v*wv+h*(1-wv)
fill=cv2.GaussianBlur(fill,(0,0),4)
mask=np.zeros((H,W),np.float32)
cv2.ellipse(mask,(int(cx),int(cy)),(int(rx),int(ry)),0,0,360,1.0,-1)
mask[yb:,:]=0
w=cv2.GaussianBlur(mask,(0,0),6)[:,:,None]
out=im*(1-w)+fill*w
cv2.imwrite(dst,out.clip(0,255).astype(np.uint8))
