#!/bin/sh
# 7일 체험 쌓기 — 늘 깨끗한 바탕부터 (두 번 덧붙이는 사고를 막는다)
set -e
cd "$(dirname "$0")"
python3 patch7.py >/dev/null      # 7day-live.html → 7day-new.html (새 운동 화면 · UI 지침)
python3 patch_mark.py             # 7day-new.html  → 7day-mark.html (✎ 표시 도구)
python3 patch_v3.py               # 7day-mark.html → 7day-v3.html  (10-02 표시 7개)
python3 patch_v4.py               # 7day-v3.html   → 7day-v4.html  (10-03 날짜 판 접기 · 운동 시작 붙박이)
python3 patch_v5.py               # 7day-v4.html   → 7day-v5.html  (10-03 표시 9개 — 날짜 판 · 플랜 고치기 · 꾸준히 늘리기 5×8~15)
# 10-03 표시 22개 — 네 갈래로 나눠 만든 것을 이 순서로 쌓는다 (서로 다른 구역만 고친다)
python3 patch_v7_stat.py 7day-v5.html _7a.html   # 스탯 · 업적 (그래프 · 업적 셈 · [달성][전체][+])
python3 patch_v7_res.py  _7a.html _7b.html        # 결과 화면 ▲▼ · 숫자 움직임 ×1.35 · 근육 기준 칸
python3 patch_v7_cal.py  _7b.html _7c.html        # 캘린더 띠 · 구분선 · 날짜 띠 · 종목 추가 · 플랜 고치기
python3 patch_v7_work.py _7c.html 7day-v7.html    # 운동 화면 띠 · 지표 아래로 · 체크 스크롤 · 칸 줄 끌기
