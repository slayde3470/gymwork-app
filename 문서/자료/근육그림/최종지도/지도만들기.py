# 최종 그림 → 15칸 지도 (씨앗 점 + 음영 골짜기를 따라 자라기). python 지도만들기.py 01_전신정면
import sys, json, os, numpy as np, cv2
from PIL import Image
from skimage.segmentation import watershed
from scipy import ndimage
FONT='/usr/share/fonts/opentype/noto/NotoSansCJK-Bold.ttc'
HERE=os.path.dirname(os.path.abspath(__file__)); ROOT=os.path.dirname(HERE)
name=sys.argv[1]; cfg=json.load(open(os.path.join(HERE,'씨앗.json')))[name]
칸=json.load(open(os.path.join(ROOT,'대표칸','칸표.json')))['칸']; 대표={c['이름']:c['대표'] for c in 칸}
색=json.load(open(os.path.join(ROOT,'색표.json')))['색']; by={(v['근육'],v['쪽']):k for k,v in 색.items()}
im=np.asarray(Image.open(os.path.join(ROOT,'최종',name+'.png')).convert('RGB'))
g=cv2.cvtColor(im,cv2.COLOR_RGB2GRAY).astype(np.float32)
H,W=g.shape
body=ndimage.binary_fill_holes(cv2.GaussianBlur(g,(0,0),1.5)<246)
body=ndimage.binary_opening(body,iterations=2)
# 높이 = 음영 골짜기(어두운 띠) + 밝기 변화
gs=cv2.GaussianBlur(g,(0,0),2.0)
bh=cv2.morphologyEx(gs,cv2.MORPH_BLACKHAT,cv2.getStructuringElement(cv2.MORPH_ELLIPSE,(21,21)))
gx=cv2.Sobel(gs,cv2.CV_32F,1,0,3); gy=cv2.Sobel(gs,cv2.CV_32F,0,1,3); gm=np.hypot(gx,gy)
elev=bh/ (bh.max()+1e-6) + 0.5*gm/(gm.max()+1e-6)
names=list(cfg['씨앗'].keys()); markers=np.zeros((H,W),np.int32)
T=cfg.get('굵기',8); mir=cfg.get('대칭',False); cx0=cfg['가운데x']
def draw(pts,i):
    P=[(int(x),int(y)) for x,y in pts]
    if len(P)==1: cv2.circle(markers,P[0],T//2,i,-1)
    else: cv2.polylines(markers,[np.array(P,np.int32)],False,i,T)
for i,n in enumerate(names,1):
    for item in cfg['씨앗'][n]:
        pts=[item] if isinstance(item[0],(int,float)) else item
        draw(pts,i)
        if mir: draw([[2*cx0-x,y] for x,y in pts],i)
for n,items in cfg.get('추가',{}).items():
    for pts in items: draw(pts,names.index(n)+1)
markers[~body]=names.index('없음')+1
lab=watershed(elev,markers,mask=None)
lab[~body]=names.index('없음')+1
# 조각 다듬기: 칸마다 열고 닫기, 작은 섬 없애기
out=np.zeros_like(lab)
for i,n in enumerate(names,1):
    m=(lab==i).astype(np.uint8)
    m=cv2.morphologyEx(m,cv2.MORPH_OPEN,np.ones((5,5),np.uint8)); m=cv2.morphologyEx(m,cv2.MORPH_CLOSE,np.ones((7,7),np.uint8))
    out[(m>0)&(out==0)]=i
# 빈 곳은 가장 가까운 칸으로(몸 안만)
hole=(out==0)&body
if hole.any():
    idx=ndimage.distance_transform_edt(out==0,return_distances=False,return_indices=True)
    out[hole]=out[tuple(idx[:,hole])]
out[~body]=names.index('없음')+1
# 경계 매끈하게: 칸마다 흐린 뒤 가장 큰 것
B=np.stack([cv2.GaussianBlur((out==i).astype(np.float32),(0,0),cfg.get('매끈',3.0)) for i in range(1,len(names)+1)])
out=(np.argmax(B,0)+1).astype(np.int32); out[~body]=names.index('없음')+1
cx=cfg['가운데x']; front=cfg['앞뒤']=='앞'
rgba=np.zeros((H,W,4),np.uint8)
xs=np.arange(W)[None,:].repeat(H,0)
for i,n in enumerate(names,1):
    if n=='없음': continue
    m=out==i
    left_img=xs<cx
    # 앞모습: 그림 왼쪽 = 사람 오른쪽(R) / 뒷모습: 그림 왼쪽 = 사람 왼쪽(L)
    for side,sel in (('R' if front else 'L', m&left_img), ('L' if front else 'R', m&~left_img)):
        h=by[(대표[n],side)]; rgba[sel,:3]=[int(h[j:j+2],16) for j in (1,3,5)]; rgba[sel,3]=255
os.makedirs(os.path.join(HERE,'지도'),exist_ok=True); os.makedirs(os.path.join(HERE,'확인'),exist_ok=True)
Image.fromarray(rgba).save(os.path.join(HERE,'지도',name+'_map.png'))
# 확인: 그림 + 반투명 지도 + 경계선
ov=im.astype(np.float32).copy(); a=rgba[...,3:4]/255*0.45
ov=ov*(1-a)+rgba[...,:3]*a
edge=cv2.morphologyEx(out.astype(np.uint8),cv2.MORPH_GRADIENT,np.ones((3,3),np.uint8))>0
ov[edge&body]=[20,20,20]
Image.fromarray(ov.clip(0,255).astype(np.uint8)).save(os.path.join(HERE,'확인',name+'_확인.png'))
# 검사용: 칸마다 뚜렷한 색 + 이름
pal=[(0,0,0),(230,25,75),(60,180,75),(255,225,25),(0,130,200),(245,130,48),(145,30,180),(70,240,240),(240,50,230),(210,245,60),(250,190,212),(0,128,128),(220,190,255),(170,110,40),(128,0,0),(170,255,195)]
dv=im.astype(np.float32)*0.55
for i,n in enumerate(names,1):
    if n=='없음': continue
    dv[out==i]=dv[out==i]*0.5+np.array(pal[i%len(pal)])*0.5
dv[edge&body]=[0,0,0]
from PIL import ImageDraw, ImageFont
D=Image.fromarray(dv.clip(0,255).astype(np.uint8)); dr=ImageDraw.Draw(D)
try: F=ImageFont.truetype(FONT,16)
except Exception: F=None
for i,n in enumerate(names,1):
    if n=='없음': continue
    for sel in ((out==i)&(xs<cx),(out==i)&(xs>=cx)):
        if sel.sum()<200: continue
        yy,xx=np.where(sel); k=len(yy)//2; o=np.argsort(yy)[k]
        dr.text((int(np.median(xx))-12,int(np.median(yy))-8),n,fill=(0,0,0),font=F)
D.save(os.path.join(HERE,'확인',name+'_검사.png'))
cnt={n:int((out==i).sum()) for i,n in enumerate(names,1)}; print(name,cnt)
