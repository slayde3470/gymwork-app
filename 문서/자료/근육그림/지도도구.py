"""근육 색칠 지도 도구 — 그림 위에 근육 경계를 긋고, 색칠 지도를 만들고, 겹쳐 본다.

색칠 지도 = 그림과 같은 크기의 PNG. 근육 하나(전신은 좌우 따로)에 단색 하나(이름표 색), 근육 아닌 곳은 투명.
이름표 색은 '이 픽셀은 어느 근육'을 적는 열쇠일 뿐 화면에 보이는 색이 아니다. 색 → 근육은 `색표.json`.

쓰는 법 (이 파일이 있는 폴더에서):
  python3 지도도구.py 색표                 근육 나무(muscle-catalog.json) → 색표.json 만들기 (근육이 늘면 다시)
  python3 지도도구.py 그리기 [이름…]       경계/이름.json → 지도/이름.png + 확인/이름.jpg (이름 없으면 전부)
  python3 지도도구.py 정리                 경계 파일을 한 조각 = 한 줄로 다시 씀
  python3 지도도구.py 검사 지도.png 그림.png [출력.png]
                                          다른 데서 칠해 온 지도를 검사하고 겹쳐 본다
                                          (색표에 없는 색 · 반투명 픽셀 · 몸 밖에 칠한 곳 · 몸인데 비어 있는 곳)

경계 파일(경계/이름.json):
  {"그림": "그림/01_전신앞.png", "좌우": true,
   "조각": [ {"근육": "lats", "쪽": "L", "점": [[x,y], …]},          ← 다각형 (부드럽게: 점을 지나는 곡선)
             {"근육": "abs", "점": […], "곧게": true},               ← 곧은 다각형
             {"지우기": true, "점": […]} ]}                         ← 근육 아닌 곳(머리 · 손 · 무릎 · 발…)
  - 위에서부터 차례로 칠하고, 뒤에 오는 조각이 앞을 덮는다 → 큰 덩어리를 먼저, 작은 근육을 나중에
  - 몸 바깥(투명한 곳)은 자동으로 잘린다 → 몸 윤곽 쪽 점은 대충 밖으로 넉넉히 찍어도 된다
  - "쪽": "L" = 사람의 왼쪽(앞모습에서는 그림 오른쪽, 뒷모습에서는 그림 왼쪽), "R" = 사람의 오른쪽. 확대 그림은 쓰지 않는다
  - "양쪽": true + 맨 위 "축": x → 그 세로축으로 거울 조각을 한 벌 더 만든다(쪽 L ↔ R). 좌우가 다르면 따로 적는다
  - "짐작": "까닭" 을 적으면 그리기 결과와 설명.md 의 '짐작한 곳'에 모인다
"""
import sys, os, json, colorsys, glob
import numpy as np
import cv2
from PIL import Image, ImageDraw, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
CATALOG = os.path.join(HERE, "..", "근육지도", "muscle-catalog.json")
COLORS = os.path.join(HERE, "색표.json")
FONT = "/usr/share/fonts/opentype/noto/NotoSansCJK-Bold.ttc"
SIDES = ["", "L", "R"]
SIDE_KO = {"": "", "L": " 왼", "R": " 오"}


# ───────────────────────── 색표 ─────────────────────────
def 색표만들기():
    cat = json.load(open(CATALOG, encoding="utf-8"))
    used, out = set(), {}
    golden = 0.61803398875
    for i, m in enumerate(cat["muscles"]):
        h = (i * golden) % 1.0
        for j, side in enumerate(SIDES):
            # 쪽마다 밝기를 달리한다: 가운데 · 왼(밝게) · 오(어둡게)
            l, s = [(0.55, 0.85), (0.70, 0.80), (0.38, 0.90)][j]
            r, g, b = [round(v * 255) for v in colorsys.hls_to_rgb(h, l, s)]
            while (r, g, b) in used:            # 겹치면 한 칸씩 비킨다
                b = (b + 1) % 256
            used.add((r, g, b))
            out["#%02x%02x%02x" % (r, g, b)] = {"근육": m["id"], "쪽": side, "이름": m["name"]}
    doc = {"설명": "색칠 지도의 색 → 근육. 쪽: '' = 좌우 안 나눔(확대 그림), L = 사람의 왼쪽, R = 사람의 오른쪽. "
                  "근육 id 는 문서/자료/근육지도/muscle-catalog.json. 지도 픽셀은 이 색과 정확히 같다(테두리 섞임 없음).",
           "색": out}
    json.dump(doc, open(COLORS, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    print(f"색표.json — {len(out)}색 (근육 {len(cat['muscles'])} × 쪽 3)")


def 색표읽기():
    d = json.load(open(COLORS, encoding="utf-8"))["색"]
    by_key = {(v["근육"], v["쪽"]): k for k, v in d.items()}
    return d, by_key


def hex2rgb(h):
    return tuple(int(h[i:i + 2], 16) for i in (1, 3, 5))


# ───────────────────────── 그리기 ─────────────────────────
def 곡선(pts, n=12):
    """닫힌 Catmull-Rom 곡선 — 찍은 점을 모두 지나는 부드러운 경계."""
    p = np.asarray(pts, float)
    m = len(p)
    if m < 3:
        return p
    out = []
    for i in range(m):
        p0, p1, p2, p3 = p[(i - 1) % m], p[i], p[(i + 1) % m], p[(i + 2) % m]
        for t in np.linspace(0, 1, n, endpoint=False):
            t2, t3 = t * t, t * t * t
            out.append(0.5 * ((2 * p1) + (-p0 + p2) * t + (2 * p0 - 5 * p1 + 4 * p2 - p3) * t2 + (-p0 + 3 * p1 - 3 * p2 + p3) * t3))
    return np.array(out)


def 펼치기(spec):
    """"양쪽": true 조각을 거울(세로축 "축" 기준)로 한 벌 더 만든다. 쪽은 L ↔ R 로 바뀐다."""
    axis = spec.get("축")
    out = []
    for p in spec["조각"]:
        out.append(p)
        if p.get("양쪽"):
            q = dict(p)
            q["점"] = [[2 * axis - x, y] for x, y in p["점"]]
            q["쪽"] = {"L": "R", "R": "L"}.get(p.get("쪽", ""), "")
            q.pop("양쪽")
            out.append(q)
    return out


def 라벨맵(spec, shape, by_key):
    """조각을 차례로 칠한 정수 라벨 맵(0 = 비움) + 라벨 목록."""
    H, W = shape
    lab = np.zeros((H, W), np.int32)
    keys = [None]
    idx = {}
    errs = []
    for k, piece in enumerate(펼치기(spec)):
        pts = piece["점"]
        곧게 = piece.get("곧게", bool(piece.get("지우기")))   # 지우기 조각은 기본이 곧은 다각형
        poly = np.asarray(pts, float) if 곧게 else 곡선(pts)
        poly = np.round(poly).astype(np.int32)
        if piece.get("지우기"):
            cv2.fillPoly(lab, [poly], 0)
            continue
        key = (piece["근육"], piece.get("쪽", ""))
        if key not in by_key:
            errs.append(f"조각 {k}: 색표에 없는 근육/쪽 {key}")
            continue
        if key not in idx:
            idx[key] = len(keys)
            keys.append(key)
        cv2.fillPoly(lab, [poly], idx[key])
    return lab, keys, errs


def 몸마스크(rgba):
    return rgba[..., 3] >= 128


def 지도저장(lab, keys, by_key, body, path):
    H, W = lab.shape
    out = np.zeros((H, W, 4), np.uint8)
    for i, key in enumerate(keys):
        if key is None:
            continue
        m = (lab == i) & body
        out[m, :3] = hex2rgb(by_key[key])
        out[m, 3] = 255
    Image.fromarray(out).save(path, optimize=True)
    return out


def 겹쳐보기(rgba, mapimg, colors, out_path, title="", notes=()):
    """그림 + 지도 반투명(55%) + 경계선 + 이름표."""
    H, W = rgba.shape[:2]
    base = Image.new("RGBA", (W, H), (40, 40, 44, 255))
    base.alpha_composite(Image.fromarray(rgba))
    over = mapimg.copy()
    over[..., 3] = (over[..., 3] > 0) * 140
    base.alpha_composite(Image.fromarray(over))
    arr = np.array(base)
    # 경계선: 색이 바뀌는 곳
    key = mapimg[..., 0].astype(np.int32) << 16 | mapimg[..., 1].astype(np.int32) << 8 | mapimg[..., 2]
    key = np.where(mapimg[..., 3] > 0, key, -1)
    edge = np.zeros((H, W), bool)
    edge[:, 1:] |= key[:, 1:] != key[:, :-1]
    edge[1:, :] |= key[1:, :] != key[:-1, :]
    edge &= (mapimg[..., 3] > 0) | np.roll(mapimg[..., 3] > 0, 1, 0) | np.roll(mapimg[..., 3] > 0, 1, 1)
    edge = cv2.dilate(edge.astype(np.uint8), np.ones((2, 2), np.uint8)) > 0
    arr[edge] = (20, 20, 20, 255)
    im = Image.fromarray(arr)
    d = ImageDraw.Draw(im)
    fs = max(11, int(min(W, H) / 70))
    font = ImageFont.truetype(FONT, fs, index=1)
    for hexc in np.unique(key[key >= 0]):
        hx = "#%06x" % hexc
        info = colors.get(hx)
        m = (key == hexc).astype(np.uint8)
        n, lb, st, _ = cv2.connectedComponentsWithStats(m)
        if n <= 1:
            continue
        k = 1 + int(np.argmax(st[1:, cv2.CC_STAT_AREA]))
        dt = cv2.distanceTransform((lb == k).astype(np.uint8), cv2.DIST_L2, 5)
        y, x = np.unravel_index(np.argmax(dt), dt.shape)
        name = (info["이름"].split(" (")[0] + SIDE_KO[info["쪽"]]) if info else "?" + hx
        tw = d.textlength(name, font=font)
        d.rectangle([x - tw / 2 - 2, y - fs / 2 - 2, x + tw / 2 + 2, y + fs / 2 + 3], fill=(255, 255, 255, 215))
        d.text((x - tw / 2, y - fs / 2 - 1), name, font=font, fill=(0, 0, 0))
    if title or notes:
        lines = [title] + [f"· {t}" for t in notes]
        pad = fs + 4
        strip = Image.new("RGBA", (W, pad * len(lines) + 8), (255, 255, 255, 255))
        ds = ImageDraw.Draw(strip)
        for i, t in enumerate(lines):
            ds.text((6, 4 + i * pad), t, font=font, fill=(0, 0, 0))
        full = Image.new("RGBA", (W, H + strip.height), (255, 255, 255, 255))
        full.paste(im, (0, 0))
        full.paste(strip, (0, H))
        im = full
    im.convert("RGB").save(out_path, quality=88)


def 검사(mapimg, body, colors):
    """지도 검사 결과(글 목록)."""
    msgs = []
    a = mapimg[..., 3]
    semi = ((a > 0) & (a < 255)).sum()
    if semi:
        msgs.append(f"⚠ 반투명 픽셀 {semi}개 (테두리를 섞지 말 것)")
    key = mapimg[..., 0].astype(np.int32) << 16 | mapimg[..., 1].astype(np.int32) << 8 | mapimg[..., 2]
    vals, cnt = np.unique(key[a > 0], return_counts=True)
    bad = [("#%06x" % v, c) for v, c in zip(vals, cnt) if "#%06x" % v not in colors]
    if bad:
        msgs.append(f"⚠ 색표에 없는 색 {len(bad)}가지: " + ", ".join(f"{h}({c})" for h, c in bad[:8]))
    outside = ((a > 0) & ~body).sum()
    if outside:
        msgs.append(f"⚠ 몸 밖에 칠한 픽셀 {outside}개")
    empty = (body & (a == 0)).sum() / max(body.sum(), 1)
    msgs.append(f"몸 중 비워 둔 곳(근육 아님) {empty:.0%}")
    return msgs, {colors["#%06x" % v]["근육"] + SIDE_KO[colors["#%06x" % v]["쪽"]]: int(c)
                  for v, c in zip(vals, cnt) if "#%06x" % v in colors}


def 그리기(names):
    colors, by_key = 색표읽기()
    os.makedirs(os.path.join(HERE, "지도"), exist_ok=True)
    os.makedirs(os.path.join(HERE, "확인"), exist_ok=True)
    files = sorted(glob.glob(os.path.join(HERE, "경계", "*.json")))
    if names:
        files = [f for f in files if os.path.basename(f)[:-5] in names]
    for f in files:
        name = os.path.basename(f)[:-5]
        spec = json.load(open(f, encoding="utf-8"))
        rgba = np.array(Image.open(os.path.join(HERE, spec["그림"])).convert("RGBA"))
        body = 몸마스크(rgba)
        lab, keys, errs = 라벨맵(spec, body.shape, by_key)
        mapimg = 지도저장(lab, keys, by_key, body, os.path.join(HERE, "지도", name + ".png"))
        msgs, counts = 검사(mapimg, body, colors)
        guesses = [f"{p['근육']}{'(양쪽)' if p.get('양쪽') else SIDE_KO[p.get('쪽', '')]}: {p['짐작']}" for p in spec["조각"] if p.get("짐작")]
        # 좌우 그림인데 쪽이 빠진 조각 / 확대 그림인데 쪽을 쓴 조각
        for p in spec["조각"]:
            if p.get("지우기"):
                continue
            if spec.get("좌우") and p.get("쪽", "") == "":
                errs.append(f"좌우 그림인데 쪽이 없음: {p['근육']}")
        겹쳐보기(rgba, mapimg, colors, os.path.join(HERE, "확인", name + ".jpg"), title=name)
        print(f"■ {name}: 근육 {len(counts)}칸", *errs, *msgs, sep="\n  ")
        if guesses:
            print("  짐작한 곳:", *guesses, sep="\n   - ")


def 경계저장(spec, path):
    """경계 파일을 한 조각 = 한 줄로 저장(사람이 읽고 고치기 쉽게)."""
    head = {k: v for k, v in spec.items() if k != "조각"}
    lines = ["{"] + [f' {json.dumps(k, ensure_ascii=False)}: {json.dumps(v, ensure_ascii=False)},' for k, v in head.items()]
    lines.append(' "조각": [')
    body = [f'  {json.dumps(p, ensure_ascii=False)}' for p in spec["조각"]]
    lines.append(",\n".join(body))
    lines += [" ]", "}"]
    open(path, "w", encoding="utf-8").write("\n".join(lines) + "\n")


def 검사명령(map_path, img_path, out_path=None):
    colors, _ = 색표읽기()
    mapimg = np.array(Image.open(map_path).convert("RGBA"))
    rgba = np.array(Image.open(img_path).convert("RGBA"))
    if mapimg.shape != rgba.shape:
        print(f"⚠ 크기가 다름: 지도 {mapimg.shape[1]}×{mapimg.shape[0]} · 그림 {rgba.shape[1]}×{rgba.shape[0]}")
        return
    msgs, counts = 검사(mapimg, 몸마스크(rgba), colors)
    print(*msgs, sep="\n")
    print("칸:", ", ".join(sorted(counts)))
    if out_path:
        겹쳐보기(rgba, mapimg, colors, out_path, title=os.path.basename(map_path))


if __name__ == "__main__":
    cmd = sys.argv[1] if len(sys.argv) > 1 else ""
    if cmd == "색표":
        색표만들기()
    elif cmd == "그리기":
        그리기(sys.argv[2:])
    elif cmd == "정리":
        for f in sys.argv[2:] or sorted(glob.glob(os.path.join(HERE, "경계", "*.json"))):
            경계저장(json.load(open(f, encoding="utf-8")), f)
    elif cmd == "검사" and len(sys.argv) >= 4:
        검사명령(*sys.argv[2:5])
    else:
        print(__doc__)
