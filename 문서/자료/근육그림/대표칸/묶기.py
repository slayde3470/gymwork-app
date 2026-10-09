"""잘게 나눈 색칠 지도(근육 나무 id) → 대표 칸 지도.

  python 묶기.py [원본지도폴더]      (기본 ../지도 = 1차, 제미나이 그림용)

칸표.json 의 칸(지금 14칸)으로 색을 합친다. 칸에 없는 근육(전거근 · 극하근 …)은 비운다.
결과: 지도/ (칸 하나에 단색 하나) · 확인/ (그림 + 지도 겹친 사진). 검사 · 겹쳐보기는 ../지도도구.py 것.
"""
import sys, os, json, glob
import numpy as np
from PIL import Image, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.dirname(HERE))
import 지도도구 as 도구

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if not os.path.exists(도구.FONT):          # 지도도구.py 의 글꼴은 클라우드용 → 없으면 맑은 고딕 (지도도구.py 는 고치지 않는다)
    class _글꼴:
        @staticmethod
        def truetype(path, size, index=0):
            return ImageFont.truetype(r"C:\Windows\Fonts\malgunbd.ttf", size)
    도구.ImageFont = _글꼴


def 칸찾기():
    """근육 나무 id → 칸 (가지를 타고 올라가며 찾는다)."""
    cat = {m["id"]: m for m in json.load(open(도구.CATALOG, encoding="utf-8"))["muscles"]}
    칸 = json.load(open(os.path.join(HERE, "칸표.json"), encoding="utf-8"))["칸"]
    root = {r: k for k in 칸 for r in k["근육"]}
    out = {}
    for mid in cat:
        cur = mid
        while cur in cat and cur not in root:
            cur = cat[cur]["parent"]
        out[mid] = root.get(cur)
    return out, 칸


def 묶기(src_dir, 그림_dir):
    colors, by_key = 도구.색표읽기()
    of, 칸 = 칸찾기()
    이름표 = {by_key[(k["대표"], s)]: dict(colors[by_key[(k["대표"], s)]], 이름=k["이름"]) for k in 칸 for s in 도구.SIDES}
    for d in ("지도", "확인"):
        os.makedirs(os.path.join(HERE, d), exist_ok=True)
    for f in sorted(glob.glob(os.path.join(src_dir, "*.png"))):
        name = os.path.basename(f)[:-4]
        src = np.array(Image.open(f).convert("RGBA"))
        rgba = np.array(Image.open(os.path.join(그림_dir, name + ".png")).convert("RGBA"))
        key = src[..., 0].astype(np.int32) << 16 | src[..., 1].astype(np.int32) << 8 | src[..., 2]
        out = np.zeros_like(src)
        빈 = {}
        for v in np.unique(key[src[..., 3] > 0]):
            info = colors["#%06x" % v]
            m = (key == v) & (src[..., 3] > 0)
            k = of.get(info["근육"])
            if not k:
                빈[info["이름"]] = 빈.get(info["이름"], 0) + int(m.sum())
                continue
            out[m, :3] = 도구.hex2rgb(by_key[(k["대표"], info["쪽"])])
            out[m, 3] = 255
        Image.fromarray(out).save(os.path.join(HERE, "지도", name + ".png"), optimize=True)
        body = 도구.몸마스크(rgba)
        msgs, counts = 도구.검사(out, body, 이름표)
        도구.겹쳐보기(rgba, out, 이름표, os.path.join(HERE, "확인", name + ".jpg"), title=name + " — 대표 칸")
        print(f"■ {name}: {len(counts)}칸", *msgs, sep="\n  ")
        if 빈:
            print("  비운 근육:", ", ".join(f"{k} {v}" for k, v in sorted(빈.items(), key=lambda t: -t[1])))


if __name__ == "__main__":
    src = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "지도")
    그림 = sys.argv[2] if len(sys.argv) > 2 else os.path.join(HERE, "..", "그림")
    묶기(src, 그림)
