#!/bin/sh
# 7일 체험 쌓기 — 늘 깨끗한 바탕부터 (두 번 덧붙이는 사고를 막는다)
set -e
cd "$(dirname "$0")"
python3 patch7.py >/dev/null      # 7day-live.html → 7day-new.html (새 운동 화면 · UI 지침)
python3 patch_mark.py             # 7day-new.html  → 7day-mark.html (✎ 표시 도구)
python3 patch_v3.py               # 7day-mark.html → 7day-v3.html  (10-02 표시 7개)
python3 patch_v4.py               # 7day-v3.html   → 7day-v4.html  (10-03 날짜 판 접기 · 운동 시작 붙박이)
