"""찍은것/ (찍기.py 가 블렌더로 찍은 것) → 그림/ · 지도/ · 확인/

  python 지도만들기.py [01 02 …]        (번호 없으면 6장 전부)

- 그림/  회색 클레이 그림 (찍은 그대로)
- 지도/  색칠 지도 — 근육 하나에 단색 하나. 색은 ../색표.json, 근육 id 는 대응표.json 으로 바꾼다
- 확인/  그림 + 지도 겹친 사진 (../지도도구.py 의 겹쳐보기 · 검사를 그대로 쓴다)

지도의 경계는 손으로 긋지 않는다. 3D 모형의 근육 조각이 화면에 찍힌 자리 그대로다.
"""
import sys, os, json
import numpy as np
from PIL import Image, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.dirname(HERE))
import 지도도구 as 도구

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

# 지도도구.py 의 글꼴 주소는 클라우드(리눅스)용이다 → 그 글꼴이 없는 컴퓨터(윈도우)에서는 맑은 고딕으로 바꿔 끼운다.
# 지도도구.py 파일은 고치지 않는다.
if not os.path.exists(도구.FONT):
    class _글꼴:
        @staticmethod
        def truetype(path, size, index=0):
            return ImageFont.truetype(r"C:\Windows\Fonts\malgunbd.ttf", size)
    도구.ImageFont = _글꼴

이름 = {"01": "01_전신앞", "02": "02_전신뒤", "03": "03_가슴확대", "04": "04_팔어깨확대", "05": "05_굽힌다리확대", "06": "06_등확대"}
좌우그림 = {"01", "02"}                     # 전신만 좌우를 나눈다 (L = 사람의 왼쪽)
쪽글자 = {"left": "L", "right": "R"}
배옆근육 = {"external_oblique", "internal_oblique", "transversus_abdominis"}
LEVELS = np.array([0, 42, 85, 127, 170, 212, 255])


def 번호읽기(path):
    """조각 번호 그림 → 픽셀마다 조각 번호(0 = 아무것도 없음)."""
    im = np.array(Image.open(path).convert("RGBA")).astype(int)
    d = np.abs(im[..., :3, None] - LEVELS)
    if d.min(-1)[im[..., 3] > 0].max() > 6:
        sys.exit(f"⚠ {path}: 조각 색이 약속한 칸에서 벗어남 (찍기.py 의 색 설정 확인)")
    q = d.argmin(-1)
    idx = q[..., 0] * 49 + q[..., 1] * 7 + q[..., 2]
    idx[im[..., 3] == 0] = 0
    return idx


def 만들기(n):
    src = os.path.join(HERE, "찍은것")
    parts = {p["번호"]: p for p in json.load(open(os.path.join(src, "parts.json"), encoding="utf-8"))}
    표 = json.load(open(os.path.join(HERE, "대응표.json"), encoding="utf-8"))
    대응 = dict(표["대응"], **{k: v[0] for k, v in 표["짐작"].items()})
    colors, by_key = 도구.색표읽기()

    clay = np.array(Image.open(os.path.join(src, f"clay_{n}.png")).convert("RGBA"))
    body = 도구.몸마스크(clay)
    top, underA, underB = (번호읽기(os.path.join(src, f"id{t}_{n}.png")) for t in ("", "A", "B"))
    name_of = np.vectorize(lambda i: parts[i]["이름"] if i else "", otypes=[object])
    is_muscle = np.vectorize(lambda i: bool(i) and parts[i]["근육"], otypes=[bool])

    idx = top.copy()
    # ① 힘줄 · 근막(막)에 덮인 곳: 막 바로 아래가 근육이면 그 근육으로 친다 (장경인대 아래 외측광근, 허리 근막 아래 기립근 …)
    #    (단, 배 가운데 흰 줄 아래로 비치는 내복사 · 복횡근은 겉 근육이 아니라서 뺀다)
    a = (name_of(top) == "connective_tissue") & is_muscle(underA) & ~np.isin(name_of(underA), ["internal_oblique", "transversus_abdominis"])
    idx[a] = underA[a]
    # ② 배 옆 근육의 널힘줄에 덮인 복직근: 배 옆 근육을 걷어 냈을 때 복직근이 보이는 곳은 복직근으로 친다
    b = np.isin(name_of(idx), list(배옆근육)) & (name_of(underB) == "rectus_abdominis")
    idx[b] = underB[b]

    out = np.zeros(clay.shape, np.uint8)
    빈것 = {}
    for i in np.unique(idx[body]):
        if not i:
            continue
        p = parts[i]
        m = (idx == i) & body
        key = p["이름"] + (":" + p["부분"] if p["부분"] else "")
        if key not in 대응:
            빈것[p["이름"]] = 빈것.get(p["이름"], 0) + int(m.sum())
            continue
        side = 쪽글자[p["쪽"]] if n in 좌우그림 else ""
        out[m, :3] = 도구.hex2rgb(by_key[(대응[key], side)])
        out[m, 3] = 255

    for d in ("그림", "지도", "확인"):
        os.makedirs(os.path.join(HERE, d), exist_ok=True)
    Image.fromarray(clay).save(os.path.join(HERE, "그림", 이름[n] + ".png"), optimize=True)
    Image.fromarray(out).save(os.path.join(HERE, "지도", 이름[n] + ".png"), optimize=True)
    msgs, counts = 도구.검사(out, body, colors)
    도구.겹쳐보기(clay, out, colors, os.path.join(HERE, "확인", 이름[n] + ".jpg"), title=이름[n] + " (3D)")
    print(f"■ {이름[n]}: 근육 {len(counts)}칸", *msgs, sep="\n  ")
    print("  칠하지 않은 조각(px):", ", ".join(f"{k} {v}" for k, v in sorted(빈것.items(), key=lambda t: -t[1])))
    return counts


if __name__ == "__main__":
    for n in sys.argv[1:] or sorted(이름):
        만들기(n)
