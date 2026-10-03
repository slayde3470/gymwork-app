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
# 10-03 저녁 표시 22개 + 근육 기준 공식 — 네 갈래(서로 다른 구역)를 이 순서로 쌓는다
python3 patch_v8_R.py 7day-v7.html _8a.html       # 루틴 화면 · 종목 넣기 · +/− 움직임(전체)
python3 patch_v8_W.py _8a.html _8b.html           # 운동 화면 띠 두 줄 · 번호 체크 · 휴지통 · 건너뛰기
python3 patch_v8_P.py _8b.html _8c.html           # 플랜 탭 · 주당 삭제 · 플랜 결과 · 같은 종목 여러 플랜
python3 patch_v8_C.py _8c.html 7day-v8.html       # 캘린더 두 칸 · 주말 색 · 스크롤바 · 근육 기준 공식
# 10-03 밤 표시 18개 — 네 갈래(서로 다른 구역)를 이 순서로 쌓는다
python3 patch_v9_W.py 7day-v8.html _9a.html       # 운동 세트 줄 · ✓ 초록 · 휴식 ± · 띠 칩 흰 박스
python3 patch_v9_R.py _9a.html _9b.html           # 운동 보고서 · 프로필 · 두 칸 10개 · 펼침 · 설정
python3 patch_v9_P.py _9b.html _9c.html           # 플랜 판정 자리 · 처방 글 세트→무게→횟수 · 고침 시트 · 종목 탭 [플랜]
python3 patch_v9_C.py _9c.html 7day-v9.html       # 기록 날 판 · 띠 단추 흰 박스 · 달 고르기 · 휴식력 저장
# 10-03 밤 표시 7개 — 보고서 · 프로필 탭(R) 위에 운동 세트 줄(W)
python3 patch_v10_R.py 7day-v9.html _10R.html     # 루틴 상자 · 프로필 상자 2×2 ▲ · 톱니 시트 · 상세 세트 순서 · 프로필 탭
python3 patch_v10_W.py _10R.html 7day-v10.html    # 체크 파랑 · 번호 보통 굵기 · 쉬는 동안 ± 감춤 · 끝난 줄 --면2
python3 patch_v11.py 7day-v10.html 7day-v11.html    # 프로필 탭 인스타 꼴 · 인증샷 · 캘린더 [운동 보고서]
python3 patch_v12.py 7day-v11.html 7day-v12.html    # 캘린더 칸 폭 같게 · 업적 달성 탭 · 가려진 칭호 보기
python3 patch_v13.py 7day-v12.html 7day-v13.html    # 운동 중 종목 넣기 · N대 파란 상자
python3 patch_v14.py 7day-v13.html 7day-v14.html    # 종목 칸 꾹 눌러 좌우 끌기 · [＋] 붙박이 · 보는 칸 맨 앞
