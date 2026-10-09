"""격자 보기 — 그림 일부를 잘라 좌표 눈금을 그어 확대해 저장한다 (경계 좌표를 읽을 때 씀).
쓰는 법: python3 격자.py 그림.png 출력.png x0 y0 x1 y1 [간격=25] [배율=2]
"""
import sys
from PIL import Image, ImageDraw

src, dst = sys.argv[1], sys.argv[2]
x0, y0, x1, y1 = map(int, sys.argv[3:7])
step = int(sys.argv[7]) if len(sys.argv) > 7 else 25
k = float(sys.argv[8]) if len(sys.argv) > 8 else 2
im = Image.open(src).convert("RGBA")
bg = Image.new("RGBA", im.size, (255, 255, 255, 255))
im = Image.alpha_composite(bg, im).convert("RGB").crop((x0, y0, x1, y1))
im = im.resize((int((x1 - x0) * k), int((y1 - y0) * k)), Image.LANCZOS)
d = ImageDraw.Draw(im)
for x in range((x0 // step + 1) * step, x1, step):
    X = (x - x0) * k
    big = x % (step * 4) == 0
    d.line([(X, 0), (X, im.height)], fill=(255, 0, 0) if big else (255, 150, 150), width=1)
    d.text((X + 2, 2), str(x), fill=(200, 0, 0))
for y in range((y0 // step + 1) * step, y1, step):
    Y = (y - y0) * k
    big = y % (step * 4) == 0
    d.line([(0, Y), (im.width, Y)], fill=(0, 0, 255) if big else (150, 150, 255), width=1)
    d.text((2, Y + 2), str(y), fill=(0, 0, 200))
im.save(dst)
