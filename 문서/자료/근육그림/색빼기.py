# 살색 빼고 하얗게: 채도 sat 배 · 밝기는 감마 gam(1보다 작으면 중간 톤이 밝아지고, 깊은 그늘은 덜 밝아져 경계가 남음)
import sys, numpy as np
from PIL import Image
src,dst,sat,gam=sys.argv[1],sys.argv[2],float(sys.argv[3]),float(sys.argv[4])
im=Image.open(src); a=np.asarray(im.convert('RGB')).astype(float)
L=(0.299*a[...,0]+0.587*a[...,1]+0.114*a[...,2])[...,None]
out=L+(a-L)*sat
Ln=255*(np.clip(L,0,255)/255)**gam
out=out*(Ln/np.maximum(L,1))
Image.fromarray(out.clip(0,255).astype(np.uint8)).save(dst)
