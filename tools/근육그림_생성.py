#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
새 근육 그림(제미나이 회색 그림 + 15칸 지도) → 앱 자원 만들기 (10-09)

언제 돌리나
  · 문서/자료/근육그림/그림/ 이나 대표칸/지도/ · 칸표.json 을 바꿨을 때

돌리는 법 (저장소 맨 위에서)
  python3 tools/근육그림_생성.py

읽는 것 — 문서/자료/근육그림/
  · 그림/NN_이름.png        회색 그림 (투명 배경)
  · 대표칸/지도/NN_이름.png  칸 지도 (칸 하나에 단색 하나 · 빈 곳 투명)
  · 색표.json · 대표칸/칸표.json

쓰는 것 — 손으로 고치지 않는다
  · app/src/main/assets/muscle/NN.webp      그림 (전신 높이 1032 · 확대 640 · 밝기만 남긴 회색)
  · app/src/main/assets/muscle/NN_map.png   칸 번호 판 (회색 한 장 · 값 = 칸번호(1~15) × 16 + 쪽(0 · L 1 · R 2) · 0 = 빈 곳)
  · app/src/main/java/com/slayde/hasenheide/ui/MusclePicData.kt   그림 이름 · 크기 · 칸 이름 · 칸에 든 근육
"""
import json
import os

import numpy as np
from PIL import Image

뿌리 = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
재료 = os.path.join(뿌리, "문서", "자료", "근육그림")
자원 = os.path.join(뿌리, "app", "src", "main", "assets", "muscle")
코드 = os.path.join(뿌리, "app", "src", "main", "java", "com", "slayde", "hasenheide", "ui", "MusclePicData.kt")

그림들 = ["01_전신앞", "02_전신뒤", "04_팔어깨확대", "05_굽힌다리확대", "06_등확대", "07_가슴정면"]
전신높이 = 1032   # 768×1376 → 579×1032 (폰 칸 높이 124dp 의 반신 자르기에도 넉넉)
확대긴변 = 640
밝기기준 = 92     # 몸 밝기의 이 백분위를 1.0 으로 (대표칸/다듬기.py 색입히기와 같다) — 앱은 여기에 색표 색을 곱한다
쪽번호 = {"": 0, "L": 1, "R": 2}

# 칸 값을 정하는 근육 — 칸표.json 의 '근육'(자리를 메우려고 넣은 것 포함)과 다른 것만 [홍겸 님 확인 대기]
# 전거근: 지도에서는 복부 자리를 메우지만 값은 가슴으로 · 능형근: 그림에 없어 승모가 대신
값옮김 = {"serratus": ("복부", "가슴"), "rhomboids": (None, "승모")}


def main():
    색 = json.load(open(os.path.join(재료, "색표.json"), encoding="utf-8"))["색"]
    칸 = json.load(open(os.path.join(재료, "대표칸", "칸표.json"), encoding="utf-8"))["칸"]
    대표 = {k["대표"]: i + 1 for i, k in enumerate(칸)}
    os.makedirs(자원, exist_ok=True)
    줄 = []
    판들 = {}
    for 이름 in 그림들:
        번호 = 이름[:2]
        im = Image.open(os.path.join(재료, "그림", 이름 + ".png")).convert("RGBA")
        지 = np.array(Image.open(os.path.join(재료, "대표칸", "지도", 이름 + ".png")).convert("RGBA"))
        판 = np.zeros(지.shape[:2], np.uint8)
        for c in np.unique(지[지[..., 3] > 0][:, :3], axis=0):
            e = 색["#%02x%02x%02x" % tuple(c)]
            sel = (지[..., 3] > 0) & np.all(지[..., :3] == c, axis=-1)
            판[sel] = 대표[e["근육"]] * 16 + 쪽번호[e["쪽"]]
        판 = Image.fromarray(판, "L")
        # 회색 한 가지로 — 밝기만 남기고 기준 백분위를 1.0 으로
        a = np.array(im).astype(np.float32)
        L = a[..., :3].mean(-1)
        L = np.clip(L / np.percentile(L[a[..., 3] >= 128], 밝기기준), 0, 1) * 255
        a[..., 0] = a[..., 1] = a[..., 2] = L
        im = Image.fromarray(a.astype(np.uint8), "RGBA")
        크기 = (확대긴변, 확대긴변) if im.size[0] == im.size[1] else (round(im.size[0] * 전신높이 / im.size[1]), 전신높이)
        im = im.resize(크기, Image.LANCZOS)
        판 = 판.resize(크기, Image.NEAREST)
        im.save(os.path.join(자원, 번호 + ".webp"), "WEBP", quality=82, method=6)
        판.save(os.path.join(자원, 번호 + "_map.png"), optimize=True)
        판들[번호] = (np.array(판), np.array(im)[..., 3])
        줄.append(f'        그림("{번호}", "{이름[3:]}", {im.size[0]}, {im.size[1]}),')
        print(f"■ {이름}: {im.size} · webp {os.path.getsize(os.path.join(자원, 번호 + '.webp')) // 1024}KB")

    칸줄 = []
    for i, k in enumerate(칸):
        근육 = list(k["근육"])
        for m, (뺄곳, 넣을곳) in 값옮김.items():
            if k["이름"] == 뺄곳 and m in 근육:
                근육.remove(m)
            if k["이름"] == 넣을곳 and m not in 근육:
                근육.append(m)
        ids = ", ".join(f'"{m}"' for m in 근육)
        칸줄.append(f'        칸("{k["이름"]}", listOf({ids})),   // {i + 1}')

    # 반신 자르기 상자 — 전신 앞 · 뒤 지도에서 잰다 (위 = 목 ~ 복부 · 팔 포함 / 아래 = 엉덩이 ~ 발끝) · 여백 12
    def 상자(칸번호들, 발까지):
        x0 = y0 = 10 ** 9; x1 = y1 = -1
        for k in ("01", "02"):
            m, a = 판들[k]
            ys, xs = np.nonzero(np.isin(m // 16, 칸번호들))
            x0, y0, x1, y1 = min(x0, xs.min()), min(y0, ys.min()), max(x1, xs.max()), max(y1, ys.max())
            if 발까지:
                y1 = max(y1, np.nonzero(a > 128)[0].max())
        x0, y0, x1, y1 = x0 - 12, y0 - 12, x1 + 12, y1 + 12
        return f"floatArrayOf({x0}f, {y0}f, {x1 - x0}f, {y1 - y0}f)"
    위 = 상자(list(range(1, 11)), False)
    아래 = 상자(list(range(11, 16)), True)

    kt = f"""package com.slayde.hasenheide.ui

// ⚠ tools/근육그림_생성.py 가 만든 파일 — 손으로 고치지 않는다 (10-09)
// 그림 = assets/muscle/번호.webp · 칸 번호 판 = assets/muscle/번호_map.png
// 판의 값 = 칸번호(1~15) × 16 + 쪽(0 좌우 없음 · 1 사람의 왼쪽 · 2 오른쪽) · 0 = 빈 곳

object 근육그림표 {{
    class 그림(val 번호: String, val 이름: String, val 폭: Int, val 높이: Int)
    /** 칸 하나 — [근육] = 이 칸 값을 정하는 근육 나무 id (아래 가지 포함 · 가장 큰 값) */
    class 칸(val 이름: String, val 근육: List<String>)

    val 그림들 = listOf(
{chr(10).join(줄)}
    )

    /** 반신 자르기 상자 [x, y, 폭, 높이] (전신 그림 픽셀 · 앞 · 뒤 같은 자리) */
    val 위상자 = {위}
    val 아래상자 = {아래}

    /** 칸번호 - 1 순서 */
    val 칸들 = listOf(
{chr(10).join(칸줄)}
    )
}}
"""
    open(코드, "w", encoding="utf-8").write(kt)
    print("■", os.path.relpath(코드, 뿌리))


if __name__ == "__main__":
    main()
