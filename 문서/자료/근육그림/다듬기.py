"""근육 밑그림 다듬기 — 원본/ 의 그림 6장 → 다듬은 그림(투명 배경).

하는 일 (그림마다 아래 '고칠곳' 표대로):
  1. ✦ 표시(제미나이 표시, 오른쪽 아래) 지우기 — 주변 색으로 메움
  2. 젖꼭지 · 성기 윤곽 지우기 — 작은 점은 메움(inpaint), 넓은 곳은 주변 회색으로 매끈하게 흐림
  3. 흰 배경 → 투명 (테두리는 반투명으로 부드럽게)
  4. 전신 앞 · 뒤(01 · 02)는 같은 크기 · 같은 위치로 맞춤 (머리 끝 · 발끝 · 가운데)

쓰는 법:  python3 다듬기.py [원본폴더=원본] [출력폴더=그림]
그림이 바뀌면 '고칠곳' 좌표만 고쳐서 다시 돌린다 (좌표는 원본 그림 픽셀 기준).
"""
import sys, os
import numpy as np
import cv2
from PIL import Image

SRC = sys.argv[1] if len(sys.argv) > 1 else "원본"
DST = sys.argv[2] if len(sys.argv) > 2 else "그림"

# 점 = (x, y, 반지름) → 메움 · 흐림 = (x, y, 가로반지름, 세로반지름) → 주변 회색으로 매끈하게
고칠곳 = {
    "01_전신앞": {"점": [(263, 430, 13), (500, 430, 13)],
                 "흐림": [(385, 765, 58, 44)]},
    "02_전신뒤": {},
    "04_팔어깨확대": {},
    "05_굽힌다리확대": {},
    "06_등확대": {},
    "07_가슴정면": {"점": [(278, 549, 18), (738, 549, 18)]},
}
전신 = ("01_전신앞", "02_전신뒤")
흰색기준 = 238          # 이보다 밝고 흰 덩어리면 배경
전신_캔버스 = (768, 1376)
전신_여백 = 20           # 위 · 아래 여백(px)


def 표시지우기(img):
    """오른쪽 아래 ✦(제미나이 표시)를 지운다. 6장 모두 같은 자리(오른쪽 · 아래 끝에서 약 119 · 118px, 반지름 약 28).
    흰 배경 위의 ✦ 는 배경을 딸 때 함께 사라지고, 몸 위의 ✦ 는 별 모양을 주변 색으로 메운다."""
    H, W = img.shape[:2]
    cx, cy, R = W - 119, H - 118, 31
    t = np.linspace(0, 2 * np.pi, 200)
    pts = np.stack([cx + R * np.cos(t) ** 3, cy + R * np.sin(t) ** 3], 1).astype(np.int32)
    mask = np.zeros(img.shape[:2], np.uint8)
    cv2.fillPoly(mask, [pts], 255)
    mask = cv2.dilate(mask, np.ones((9, 9), np.uint8))
    L = img.astype(np.int16).mean(axis=2)
    on_body = int(((L < 225) & (mask > 0)).sum())
    if on_body < 50:          # 흰 배경 위 → 배경 따기에서 사라짐
        return img, 0
    # ✦ 가 몸 윤곽에 걸쳐 있으면: ① 윤곽선을 이어 그리고 ② 몸 색은 몸에서만 끌어온다
    out_body = ((L < 흰색기준) & (mask == 0)).astype(np.uint8) * 255
    body_in = cv2.inpaint(out_body, mask, 9, cv2.INPAINT_TELEA) > 127
    near_bg = (cv2.dilate(mask, np.ones((61, 61), np.uint8)) > 0) & (L >= 흰색기준)
    mask2 = ((mask > 0) | near_bg).astype(np.uint8) * 255
    filled = cv2.inpaint(img, mask2, 9, cv2.INPAINT_TELEA)
    out = img.copy()
    star = mask > 0
    out[star & body_in] = filled[star & body_in]
    out[star & ~body_in] = 255
    return out, on_body


def 점메우기(img, pts):
    for (x, y, r) in pts:
        mask = np.zeros(img.shape[:2], np.uint8)
        cv2.circle(mask, (x, y), r, 255, -1)
        filled = cv2.inpaint(img, mask, r, cv2.INPAINT_TELEA)
        # 메운 곳을 살짝 흐려 경계를 지운다
        soft = cv2.GaussianBlur(filled, (0, 0), r / 3)
        a = np.zeros(img.shape[:2], np.float32)
        cv2.circle(a, (x, y), int(r * 1.3), 1.0, -1)
        a = cv2.GaussianBlur(a, (0, 0), r / 3)[..., None]
        img = (filled * (1 - a) + soft * a).astype(np.uint8)
    return img


def 매끈하게(img, body, ells):
    """타원 안을 몸 픽셀만으로 크게 흐려(흰 배경은 섞지 않음) 윤곽을 없앤다."""
    f = img.astype(np.float32)
    b = body.astype(np.float32)
    for (x, y, rx, ry) in ells:
        s = max(rx, ry) / 3.5
        num = cv2.GaussianBlur(f * b[..., None], (0, 0), s)
        den = cv2.GaussianBlur(b, (0, 0), s)[..., None] + 1e-6
        blur = num / den
        a = np.zeros(img.shape[:2], np.float32)
        cv2.ellipse(a, (x, y), (rx, ry), 0, 0, 360, 1.0, -1)
        a = cv2.GaussianBlur(a, (0, 0), s * 0.8)
        a = (a * b)[..., None]
        f = f * (1 - a) + blur * a
    return np.clip(f, 0, 255).astype(np.uint8)


def 배경마스크(img):
    L = img.astype(np.int16).mean(axis=2)
    sat = img.max(axis=2).astype(np.int16) - img.min(axis=2)
    white = ((L >= 흰색기준) & (sat < 12)).astype(np.uint8)
    n, lab, st, _ = cv2.connectedComponentsWithStats(white, connectivity=4)
    H, W = white.shape
    bg = np.zeros_like(white)
    for k in range(1, n):
        x, y, w, h, area = st[k]
        edge = x == 0 or y == 0 or x + w == W or y + h == H
        if edge or area > 400:          # 테두리에 닿거나 큰 흰 구멍(팔과 몸 사이)
            bg[lab == k] = 1
    return bg


def 투명하게(img):
    bg = 배경마스크(img)
    body = 1 - bg
    L = img.astype(np.float32).mean(axis=2)
    # 테두리 2px 만 밝기로 반투명 (흰 배경과 섞인 가장자리)
    ring = cv2.dilate(bg, np.ones((5, 5), np.uint8)) & body
    alpha = body.astype(np.float32)
    # 가장자리 픽셀의 '원래 몸 밝기'는 바로 안쪽 몸 밝기로 본다
    inner = cv2.erode(body, np.ones((5, 5), np.uint8)).astype(np.float32)
    Lin = cv2.GaussianBlur(L * inner, (0, 0), 3) / (cv2.GaussianBlur(inner, (0, 0), 3) + 1e-6)
    Lin = np.minimum(Lin, 235)
    a_ring = np.clip((255 - L) / (255 - Lin), 0, 1)
    alpha = np.where(ring == 1, np.maximum(a_ring, 0.0), alpha)
    alpha = np.where(bg == 1, 0, alpha)
    # 흰색과 섞인 색을 되돌림: C = (P - (1-a)*255) / a
    a3 = np.maximum(alpha, 1e-3)[..., None]
    rgb = (img.astype(np.float32) - (1 - alpha[..., None]) * 255) / a3
    rgb = np.where(alpha[..., None] > 0.02, rgb, 0)
    rgb = np.clip(rgb, 0, 255).astype(np.uint8)
    return np.dstack([rgb, (alpha * 255).astype(np.uint8)]), body


def 맞추기(rgba):
    """전신: 머리 끝~발끝을 같은 높이로, 가운데를 캔버스 가운데로."""
    W, H = 전신_캔버스
    ys, xs = np.where(rgba[..., 3] > 10)
    x0, x1, y0, y1 = xs.min(), xs.max() + 1, ys.min(), ys.max() + 1
    crop = Image.fromarray(rgba[y0:y1, x0:x1])
    k = (H - 2 * 전신_여백) / (y1 - y0)
    nw, nh = round(crop.width * k), H - 2 * 전신_여백
    crop = crop.resize((nw, nh), Image.LANCZOS)
    out = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    out.alpha_composite(crop, ((W - nw) // 2, 전신_여백))
    return out, dict(box=[int(x0), int(y0), int(x1), int(y1)], scale=round(k, 4))


def main():
    os.makedirs(DST, exist_ok=True)
    for name, fix in 고칠곳.items():
        img = np.array(Image.open(f"{SRC}/{name}.png").convert("RGB"))
        img, n = 표시지우기(img)
        img = 점메우기(img, fix.get("점", []))
        if fix.get("흐림"):
            img = 매끈하게(img, 1 - 배경마스크(img), fix["흐림"])
        rgba, _ = 투명하게(img)
        if name in 전신:
            out, info = 맞추기(rgba)
        else:
            out, info = Image.fromarray(rgba), {}
        out.save(f"{DST}/{name}.png", optimize=True)
        print(name, "✦ 지움" if n else "✦ 없음", n, info)


if __name__ == "__main__":
    main()
