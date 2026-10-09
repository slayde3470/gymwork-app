"""홍겸 님이 보기 그림 위에 그어 준 빨간 선을 뽑아 원래 그림 자리에 맞춘다.

  python 손선뽑기.py 찍은그림(캡처) 이름        예: python 손선뽑기.py 캡처.png 01_전신앞

선 긋는 법: 밑그림/이름.jpg (회색 그림)을 띄워 캡처한 뒤, 밝은 빨강이나 연두색 · 굵기 3 이상으로 칸마다 닫힌 모양을 긋는다.
결과: 손선/이름.png (원래 그림과 같은 크기 · 선 = 흰색). 다듬기.py 가 이 선을 '어디를 어떤 모양으로 나눌지'의 기준으로 쓴다.
선은 그대로 경계가 되지 않는다 — 다듬기.py 가 매끈한 곡선으로 다시 편다.
"""
import sys, os
import numpy as np
import cv2
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))


def 뽑기(shot_path, name):
    shot = np.array(Image.open(shot_path).convert("RGB")).astype(np.float32)
    sat = shot.max(-1) - shot.min(-1)                      # 색이 진한 정도
    if (sat > 90).mean() < 0.08:
        # 회색 밑그림(밑그림/) 위에 그은 선: 색이 진한 곳이 곧 선이다 (빨강 · 연두 · 파랑 무엇이든)
        m = sat > 90
    else:
        # 색을 입힌 보기 그림 위에 그은 가는 빨간 선: 둘레보다 붉은 정도가 튀는 곳 (칸에 입힌 붉은 색과 구별하려고)
        red = shot[..., 0] - (shot[..., 1] + shot[..., 2]) / 2
        th = cv2.morphologyEx(red, cv2.MORPH_TOPHAT, cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (7, 7)))
        m = th > 35
    n, lb, st, _ = cv2.connectedComponentsWithStats(m.astype(np.uint8), connectivity=8)
    line = np.isin(lb, [i for i in range(1, n) if st[i, cv2.CC_STAT_AREA] >= 12])
    orig = np.array(Image.open(os.path.join(HERE, "..", "그림", name + ".png")).convert("RGBA")).astype(np.float32)
    body_o = orig[..., 3] >= 128
    os.makedirs(os.path.join(HERE, "손선"), exist_ok=True)
    if shot.shape[:2] == body_o.shape:
        # 원래 그림과 같은 크기: 그 그림 위에 바로 그은 것이면(선 밖의 회색이 그대로면) 맞출 것 없이 그대로 쓴다
        a = orig[..., 3:] / 255
        gray = (orig[..., :3] * a + 255 * (1 - a)).mean(-1)
        rest = cv2.dilate(m.astype(np.uint8), np.ones((9, 9), np.uint8)) == 0
        if np.abs(shot.mean(-1) - gray)[rest].mean() < 6:
            Image.fromarray((line * 255).astype(np.uint8)).save(os.path.join(HERE, "손선", name + ".png"))
            print(f"손선/{name}.png — 선 {int(line.sum())}px · 같은 그림 위에 그은 선(그대로 씀)")
            return
    body_s = (shot.min(-1) < 235) | line
    body_s = cv2.morphologyEx(body_s.astype(np.uint8), cv2.MORPH_CLOSE, np.ones((5, 5), np.uint8)) > 0

    def bbox(a):
        ys, xs = np.nonzero(a)
        return xs.min(), xs.max(), ys.min(), ys.max()
    xo0, xo1, yo0, yo1 = bbox(body_o)
    xs0, xs1, ys0, ys1 = bbox(body_s)
    s0 = ((xo1 - xo0) / (xs1 - xs0) + (yo1 - yo0) / (ys1 - ys0)) / 2
    H, W = body_o.shape
    best = None
    for s in np.linspace(s0 * 0.98, s0 * 1.02, 17):          # 크기 · 자리를 조금씩 바꿔 가장 잘 겹치는 곳
        for dx in range(-6, 7, 2):
            for dy in range(-6, 7, 2):
                M = np.float32([[s, 0, (xo0 + xo1) / 2 - (xs0 + xs1) / 2 * s + dx], [0, s, (yo0 + yo1) / 2 - (ys0 + ys1) / 2 * s + dy]])
                w = cv2.warpAffine(body_s.astype(np.float32), M, (W, H)) > 0.5
                iou = (w & body_o).sum() / (w | body_o).sum()
                if best is None or iou > best[0]:
                    best = (iou, M)
    out = cv2.warpAffine(line.astype(np.float32), best[1], (W, H), flags=cv2.INTER_LINEAR) > 0.12
    Image.fromarray((out * 255).astype(np.uint8)).save(os.path.join(HERE, "손선", name + ".png"))
    print(f"손선/{name}.png — 선 {int(out.sum())}px · 겹침 {best[0]:.3f} · 배율 {best[1][0, 0]:.3f}")


if __name__ == "__main__":
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")
    뽑기(sys.argv[1], sys.argv[2])
