"""제미나이 그림 위 15칸 지도 다듬기.

  python 다듬기.py [이름…]          (이름 없으면 6장 전부)

순서: ① 1차 지도(../지도)를 15칸으로 묶는다 → ② 고칠곳.json 의 고침을 차례로 적용
      → ③ 손선/이름.png 이 있으면(홍겸 님이 그어 준 선 — 손선뽑기.py) 그 선으로 나뉜 조각마다 한 칸으로 정한다 → ④ 경계를 매끈한 곡선으로
결과: 지도/ (칸 하나에 단색 하나) · 확인/ (경계선 · 이름표) · 보기/ (명암을 살려 색을 입힌 모습)

고칠곳.json — 그림마다 고침 목록. 좌표는 그림 픽셀(x, y). 점을 지나는 부드러운 곡선으로 닫힌 모양을 만든다.
  {"칸": "광배", "칠하기": [[x,y], …]}      그 모양 안을 이 칸으로 (다른 칸 위에도 덮어 칠한다)
  {"칸": "목", "칠하기": […], "빈곳만": true}   비어 있는 곳만 칠한다
  {"칸": "엉덩이", "남기기": […]}            이 칸을 그 모양 안쪽만 남긴다 (근육 자리만 남기고 둘레 살은 뺄 때)
  {"지우기": […]}                            그 모양 안을 비운다 ("칸" 을 적으면 그 칸만)
  "거울": true                               가운데 줄 기준으로 반대쪽에도 똑같이. 가운데 줄은 전신 그림이면 x 383.5, 다른 그림은 "축": x 로 적는다
  "곧게": true                               곡선 대신 곧은 다각형
쪽(L · R)은 전신 그림에서만 나누고, 모양이 그림의 어느 쪽에 있느냐로 정한다 (L = 사람의 왼쪽: 앞모습은 그림 오른쪽, 뒷모습은 그림 왼쪽).
"""
import sys, os, json, glob
import numpy as np
import cv2
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.dirname(HERE))
import 묶기
import 지도도구 as 도구

CX = 383.5              # 전신 그림의 가운데 줄
SIGMA = 7.0             # 경계를 매끈하게 펴는 정도(px) — 클수록 선이 둥글고 매끈하다
UP = 3                  # 경계를 다듬을 때 그림을 몇 배로 키워서 계산하나 (선을 더 잘게 쪼개 매끈하게)
FEATHER = 1.2           # 보기 그림에서 색 가장자리를 부드럽게 푸는 폭(px)
전신 = ("01_전신앞", "02_전신뒤")
# 보기 그림에 입히는 색(화면용 예시 — 지도의 색과는 다르다)
보기색 = {"목": (240, 200, 40), "어깨": (245, 130, 40), "가슴": (235, 60, 140), "이두": (60, 150, 240), "삼두": (40, 200, 190), "전완": (150, 110, 230),
        "복부": (120, 200, 50), "승모": (105, 85, 225), "광배": (185, 80, 200), "허리": (60, 190, 235), "엉덩이": (245, 190, 40), "대퇴사두": (225, 50, 55),
        "햄스트링": (245, 125, 40), "내전근": (240, 95, 165), "종아리": (70, 170, 95)}


def 모양(shape, pts, 곧게=False):
    m = np.zeros(shape, np.uint8)
    p = np.asarray(pts, float) if 곧게 else 도구.곡선(pts)
    cv2.fillPoly(m, [np.round(p).astype(np.int32)], 1)
    return m > 0


def 칸읽기(mapimg, colors, of):
    """잔 근육 지도 → {(칸, 쪽): 자리}."""
    key = mapimg[..., 0].astype(np.int32) << 16 | mapimg[..., 1].astype(np.int32) << 8 | mapimg[..., 2]
    out = {}
    for v in np.unique(key[mapimg[..., 3] > 0]):
        info = colors["#%06x" % v]
        k = of.get(info["근육"])
        if k:
            kk = (k["이름"], info["쪽"])
            out[kk] = out.get(kk, np.zeros(key.shape, bool)) | ((key == v) & (mapimg[..., 3] > 0))
    return out


def 고치기(cells, edits, body, name):
    shape = body.shape
    for e in edits:
        pts0 = e.get("칠하기") or e.get("남기기") or e.get("지우기")
        keep = {}                               # 남기기: 거울 두 쪽을 합친 모양 안쪽만 남긴다
        for mirror in ((False, True) if e.get("거울") else (False,)):
            ax = e.get("축", CX)
            pts = [(2 * ax - x, y) for x, y in pts0] if mirror else pts0
            m = 모양(shape, pts, e.get("곧게", False)) & body
            side = ""
            if name in 전신:
                right = np.mean([p[0] for p in pts]) > CX
                side = ("R" if right else "L") if "뒤" in name else ("L" if right else "R")
            if "칠하기" in e:
                key = (e["칸"], side)
                if e.get("빈곳만"):
                    for k2, v2 in cells.items():
                        m = m & ~v2
                else:
                    for k2 in cells:
                        if k2 != key:
                            cells[k2] = cells[k2] & ~m
                cells[key] = cells.get(key, np.zeros(shape, bool)) | m
            elif "남기기" in e:
                keep.setdefault((e["칸"], side), np.zeros(shape, bool))
                keep[(e["칸"], side)] |= m
            else:
                for k2 in cells:
                    if "칸" not in e or k2[0] == e["칸"]:
                        cells[k2] = cells[k2] & ~m
        for key, m in keep.items():
            if key in cells:
                cells[key] = cells[key] & m
    return cells


def 손선나누기(cells, body, line, name):
    """홍겸 님이 그은 선으로 몸을 조각내고, 조각마다 지금 지도에서 가장 많은 칸 하나로 정한다.
    선은 '어디를 어떤 모양으로 나눌지'만 정한다 — 선의 울퉁불퉁함은 뒤의 매끈하게()가 편다."""
    names = sorted({k[0] for k in cells})
    cur = np.zeros(body.shape, np.int32)                       # 0 = 빈 곳
    for i, nm in enumerate(names, start=1):
        for k, v in cells.items():
            if k[0] == nm:
                cur[v] = i
    # 선이 조금 끊긴 곳(약 18px 까지)은 이어 준다. 턱 밑처럼 선을 긋지 않은 목 둘레는 지금 지도의 목 테두리로 막는다
    line = cv2.morphologyEx(line.astype(np.uint8), cv2.MORPH_CLOSE, cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (19, 19)))
    line = cv2.dilate(line, np.ones((3, 3), np.uint8)) > 0
    if "목" in names:
        neck = (cur == names.index("목") + 1).astype(np.uint8)
        line |= cv2.morphologyEx(neck, cv2.MORPH_GRADIENT, np.ones((3, 3), np.uint8)) > 0
    n, lb = cv2.connectedComponents((body & ~line).astype(np.uint8), connectivity=4)
    new = cur.copy()
    report = []
    tri = names.index("삼두") + 1 if "삼두" in names else -1
    bi = names.index("이두") + 1 if "이두" in names else -1
    comps = []
    for c in range(1, n):
        m = lb == c
        area = int(m.sum())
        if area >= 150:
            comps.append((m, area, np.bincount(cur[m], minlength=len(names) + 1)))
    # 제 몫의 조각을 따로 가진 칸(그 조각의 85% 이상) — 다른 조각에 조금 걸친 그 칸은 '남은 자투리'로 본다
    own = {}
    for m, area, cnt in comps:
        top = int(cnt.argmax())
        if top and cnt[top] >= 0.85 * area:
            own[top] = max(own.get(top, 0), int(cnt[top]))
    for m, area, cnt in comps:
        top = int(cnt.argmax())
        if top == bi and cnt[tri] >= 0.05 * area:              # 앞에서 본 팔 바깥 띠: 이두 타원과 따로 그어 준 조각 = 삼두
            new[m] = tri
            report.append((area, "삼두"))
            continue
        big = [i for i in range(1, len(cnt)) if cnt[i] >= 0.10 * area and not (own.get(i, 0) > cnt[i])]
        if cnt[0] >= 0.35 * area:
            big.append(0)                                      # 빈 곳이 넓게 들어 있으면 빈 곳도 한몫
        if not big:
            big = [top]
        if len(big) == 1:
            new[m] = big[0]
            report.append((area, names[big[0] - 1] if big[0] else "빈 곳"))
            continue
        # 선으로 갈라 주지 않은 칸들(예: 어깨와 승모)이 한 조각에 섞여 있다 → 큰 칸들 사이 경계는 지금 것을 두고,
        # 조각 안에 조금 걸쳐 있는 다른 칸은 가장 가까운 큰 칸으로 넘긴다 (조각의 바깥 선은 그어 준 선을 따른다)
        ys, xs_ = np.nonzero(m)
        y0, y1, x0, x1 = ys.min(), ys.max() + 1, xs_.min(), xs_.max() + 1
        sub, subm = cur[y0:y1, x0:x1], m[y0:y1, x0:x1]
        d = np.stack([cv2.distanceTransform((~((sub == i) & subm)).astype(np.uint8), cv2.DIST_L2, 3) for i in big])
        near = np.array(big)[d.argmin(0)]
        fix = subm & ~np.isin(sub, big)
        patch = new[y0:y1, x0:x1]
        patch[fix] = near[fix]
        report.append((area, "+".join(names[i - 1] if i else "빈 곳" for i in big)))
    out = {}
    H, W = body.shape
    xs = np.arange(W)[None, :] > CX
    for i, nm in enumerate(names, start=1):
        m = new == i
        if name in 전신:
            m = m | m[:, ::-1]                                  # 좌우 같은 모양으로(손으로 그은 선의 좌우 차이를 없앤다)
            m = cv2.GaussianBlur(((new == i).astype(np.float32) + (new == i)[:, ::-1].astype(np.float32)) / 2, (0, 0), 2.0) > 0.5
            m &= body
            right, left = ("L", "R") if "앞" in name else ("R", "L")
            out[(nm, right)] = m & xs
            out[(nm, left)] = m & ~xs
        else:
            out[(nm, "")] = m
    print("  손선 조각:", ", ".join(f"{nm} {a}" for a, nm in sorted(report, reverse=True)[:40]))
    return out


def 매끈하게(cells, body):
    """칸마다 흐리게 한 뒤 가장 센 칸을 고른다(빈 곳도 한 칸으로 친다). UP 배로 키운 그림에서 계산해 선을 잘게 쪼갠다."""
    keys = [k for k in cells if cells[k].any()]
    none = body.copy()
    for k in keys:
        none &= ~cells[k]
    H, W = body.shape
    big = lambda m: cv2.resize(m.astype(np.float32), (W * UP, H * UP), interpolation=cv2.INTER_LINEAR)
    best = cv2.GaussianBlur(big(none), (0, 0), SIGMA * UP)
    arg = np.full(best.shape, -1, np.int32)
    for i, k in enumerate(keys):
        b = cv2.GaussianBlur(big(cells[k]), (0, 0), SIGMA * UP)
        u = b > best
        arg[u] = i
        best[u] = b[u]
    out, soft = {}, {}
    for i, k in enumerate(keys):
        frac = cv2.resize((arg == i).astype(np.float32), (W, H), interpolation=cv2.INTER_AREA)     # 픽셀 안에서 그 칸이 차지하는 몫
        soft[k] = frac * body
    stack = np.stack([soft[k] for k in keys] + [1 - sum(soft.values())])
    win = stack.argmax(0)
    for i, k in enumerate(keys):
        out[k] = (win == i) & body
    return out


def 색입히기(rgba, cells, 진하기=0.6):
    """명암은 그림 것 그대로 두고 색만 입힌다. 가장자리는 FEATHER 만큼 부드럽게 푼다(보기용 — 지도는 딱 떨어지는 단색)."""
    g = rgba.astype(np.float32)
    body = rgba[..., 3] >= 128
    L = g[..., :3].mean(-1) / 255.0
    L = np.clip(L / np.percentile(L[body], 92), 0, 1.25)[..., None]
    out = g.copy()
    for (nm, side), sel in cells.items():
        c = np.array(보기색[nm], np.float32)
        col = c * np.clip(L, 0, 1) + (255 - c) * np.clip(L - 1, 0, 0.25) * 2
        w = (cv2.GaussianBlur(sel.astype(np.float32), (0, 0), FEATHER) * body * 진하기)[..., None]
        out[..., :3] = out[..., :3] * (1 - w) + np.clip(col, 0, 255) * w
    bg = Image.new("RGBA", (g.shape[1], g.shape[0]), (255, 255, 255, 255))
    bg.alpha_composite(Image.fromarray(np.clip(out, 0, 255).astype(np.uint8)))
    return bg.convert("RGB")


def 다듬기(names):
    colors, by_key = 도구.색표읽기()
    of, 칸 = 묶기.칸찾기()
    대표 = {k["이름"]: k["대표"] for k in 칸}
    이름표 = {by_key[(k["대표"], s)]: dict(colors[by_key[(k["대표"], s)]], 이름=k["이름"]) for k in 칸 for s in 도구.SIDES}
    고칠곳 = json.load(open(os.path.join(HERE, "고칠곳.json"), encoding="utf-8"))
    for d in ("지도", "확인", "보기"):
        os.makedirs(os.path.join(HERE, d), exist_ok=True)
    for f in sorted(glob.glob(os.path.join(HERE, "..", "지도", "*.png"))):
        name = os.path.basename(f)[:-4]
        if names and name not in names and name[:2] not in names:
            continue
        rgba = np.array(Image.open(os.path.join(HERE, "..", "그림", name + ".png")).convert("RGBA"))
        body = 도구.몸마스크(rgba)
        cells = 칸읽기(np.array(Image.open(f).convert("RGBA")), colors, of)
        cells = 고치기(cells, 고칠곳.get(name, []), body, name)
        hand = os.path.join(HERE, "손선", name + ".png")
        if os.path.exists(hand):
            cells = 손선나누기(cells, body, np.array(Image.open(hand).convert("L")) > 127, name)
        cells = 매끈하게(cells, body)
        out = np.zeros(rgba.shape, np.uint8)
        for (nm, side), sel in cells.items():
            out[sel, :3] = 도구.hex2rgb(by_key[(대표[nm], side)])
            out[sel, 3] = 255
        Image.fromarray(out).save(os.path.join(HERE, "지도", name + ".png"), optimize=True)
        msgs, counts = 도구.검사(out, body, 이름표)
        도구.겹쳐보기(rgba, out, 이름표, os.path.join(HERE, "확인", name + ".jpg"), title=name + " — 15칸")
        색입히기(rgba, cells).save(os.path.join(HERE, "보기", name + ".jpg"), quality=90)
        print(f"■ {name}: {len(counts)}칸 · 고침 {len(고칠곳.get(name, []))}개", *msgs, sep="\n  ")


if __name__ == "__main__":
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")
    다듬기(sys.argv[1:])
