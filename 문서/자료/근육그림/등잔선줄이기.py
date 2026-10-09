# 등 울퉁불퉁 줄이기 (홍겸 님 10-10: 체지방 더 · 근육 안 잔 선 흐리게 · 큰 칸 경계는 뚜렷)
# 중간 크기 굴곡(작은 혹 · 잔 선, 폭 약 s1~s2 픽셀)만 줄이고, 큰 음영(큰 칸 경계 골짜기 · 몸 둥근 명암)은 남김
import sys, numpy as np
from PIL import Image
from scipy import ndimage
src,dst=sys.argv[1],sys.argv[2]; s1,s2,k=float(sys.argv[3]),float(sys.argv[4]),float(sys.argv[5])
y0=int(sys.argv[6]) if len(sys.argv)>6 else 0; y1=int(sys.argv[7]) if len(sys.argv)>7 else 99999
im=Image.open(src); a=np.asarray(im.convert('RGB')).astype(float); H,W=a.shape[:2]
body=ndimage.binary_erosion(a.mean(2)<246,iterations=3).astype(float)
def nb(img,s):
    w=ndimage.gaussian_filter(body,s)+1e-6
    return np.stack([ndimage.gaussian_filter(img[...,c]*body,s)/w for c in range(3)],-1)
lo1=nb(a,s1); lo2=nb(a,s2)
mid=lo1-lo2                      # 중간 크기 굴곡
# 큰 경계 골짜기는 남기기: 아주 넓고 깊은 어두운 골은 덜 줄임
deep=np.clip((-mid.mean(2)-6)/10,0,1)
keep=ndimage.gaussian_filter(deep,2)
yy=np.arange(H)[:,None]; vm=((yy>=y0)&(yy<=y1)).astype(float); vm=ndimage.gaussian_filter(vm,[15,0])
mask=ndimage.gaussian_filter(body,3)*vm
out=a-k*(1-0.6*keep)[...,None]*mid*mask[...,None]
Image.fromarray(out.clip(0,255).astype(np.uint8)).save(dst)
