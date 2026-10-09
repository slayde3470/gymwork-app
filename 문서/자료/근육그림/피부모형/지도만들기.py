# 피부 모형: 찍은 것 → 그림 · 지도 · 확인 · 색 입힌 보기 (임시)
import sys, os, json, numpy as np, cv2
from PIL import Image, ImageDraw, ImageFont
R, B, OUT = sys.argv[1], sys.argv[2], sys.argv[3]
sys.path.insert(0, os.path.join(R, "대표칸")); sys.path.insert(0, R)
import 묶기, 지도도구 as 도구
colors, by_key = 도구.색표읽기()
legend = json.load(open(os.path.join(B, "legend.json"), encoding="utf-8"))
pal = np.array([[int(h[i:i + 2], 16) for i in (1, 3, 5)] for h in legend])
names = list(legend.values())
이름표 = {h: dict(colors[h], 이름=legend[h][0]) for h in legend}
색 = {"목": (240, 200, 40), "어깨": (245, 130, 40), "가슴": (235, 60, 140), "이두": (60, 150, 240), "삼두": (40, 200, 190), "전완": (150, 110, 230),
      "복부": (120, 200, 50), "승모": (105, 85, 225), "광배": (185, 80, 200), "허리": (60, 190, 235), "엉덩이": (245, 190, 40), "대퇴사두": (225, 50, 55),
      "햄스트링": (245, 125, 40), "내전근": (240, 95, 165), "종아리": (70, 170, 95)}
os.makedirs(OUT, exist_ok=True)
sheets = {"회색": [], "색": [], "확인": []}
for n, nm in (("01", "01_전신앞"), ("02", "02_전신뒤")):
    clay = np.array(Image.open(os.path.join(B, f"clay_{n}.png")).convert("RGBA"))
    idm = np.array(Image.open(os.path.join(B, f"id_{n}.png")).convert("RGBA")).astype(int)
    body = clay[..., 3] >= 128
    d = ((idm[..., None, :3] - pal[None, None]) ** 2).sum(-1)
    k = d.argmin(-1); ok = (d.min(-1) < 300) & (idm[..., 3] > 0)
    # 경계를 한 번 고르게: 칸별 흐림 → 가장 센 칸
    best = np.full(k.shape, 0.25, np.float32); res = np.full(k.shape, -1)
    for i in range(len(pal)):
        p = cv2.GaussianBlur(((k == i) & ok).astype(np.float32), (0, 0), 1.5)
        u = p > best; res[u] = i; best[u] = p[u]
    res[~body] = -1
    m = np.zeros(clay.shape, np.uint8)
    for i in np.unique(res[res >= 0]):
        m[res == i, :3] = pal[i]; m[res == i, 3] = 255
    Image.fromarray(clay).save(os.path.join(OUT, f"그림_{nm}.png"), optimize=True)
    Image.fromarray(m).save(os.path.join(OUT, f"지도_{nm}.png"), optimize=True)
    msgs, counts = 도구.검사(m, body, 이름표)
    print("■", nm, len(counts), "칸", *msgs)
    도구.겹쳐보기(clay, m, 이름표, os.path.join(OUT, f"확인_{nm}.jpg"), title=nm + " — 피부 모형 15칸")
    g = clay.astype(np.float32)
    L = g[..., :3].mean(-1) / 255.0
    L = np.clip(L / np.percentile(L[body], 92), 0, 1.25)
    out = g.copy()
    for i in np.unique(res[res >= 0]):
        c = np.array(색[names[i][0]], np.float32); sel = res == i
        sh = L[sel][:, None]
        col = c * np.clip(sh, 0, 1) + (255 - c) * np.clip(sh - 1, 0, 0.25) * 2
        out[sel, :3] = g[sel, :3] * 0.4 + np.clip(col, 0, 255) * 0.6
    for key, arr in (("회색", g), ("색", out)):
        bg = Image.new("RGBA", (arr.shape[1], arr.shape[0]), (255, 255, 255, 255))
        bg.alpha_composite(Image.fromarray(arr.astype(np.uint8)))
        sheets[key].append(bg.convert("RGB").crop((130, 0, 870, arr.shape[0])))
for key in ("회색", "색"):
    ims = sheets[key]
    sh = Image.new("RGB", (sum(i.width for i in ims), ims[0].height), (255, 255, 255))
    x = 0
    for im in ims:
        sh.paste(im, (x, 0)); x += im.width
    sh.resize((sh.width * 3 // 4, sh.height * 3 // 4), Image.LANCZOS).save(os.path.join(OUT, f"피부모형_{key}.jpg"), quality=90)
