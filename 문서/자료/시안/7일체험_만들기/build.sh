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
python3 patch_v15.py 7day-v14.html 7day-v15.html    # N대 상자 kg −25% · 너비 −15% · 사이 −50% · 프로필 업적 −25% · 인증샷 글 지움
python3 patch_v16.py 7day-v15.html 7day-v16.html    # N대 상자 칸 줄 전체 − 좌우 15 · 사이 −35%
python3 patch_v17_A.py 7day-v16.html _17a.html   # 운동 세트 게이지 · 마지막 세트 문구 · + 세트 자리 · 종목 기본 세팅
python3 patch_v17_B.py _17a.html _17b.html       # 운동 보고서 움직임 · 띠 · 날짜 · ‹ › · 단추 한 줄 · 상세 시트 · 톱니
python3 patch_v17_C.py _17b.html _17c.html       # 프로필 · 업적 줄 정렬/끌기 · SNS 링크 · 인증샷 8장 · 탭줄 메모 · 검색
python3 patch_v17_D.py _17c.html _17d.html       # 캘린더 구분선 · 년 고르기 · ‹ › · 기록 골라 지우기 · 띠/토스트
python3 patch_v17_E.py _17d.html 7day-v17.html   # 큰 운동 칸 숫자 정수
python3 patch_v18_A.py 7day-v17.html _18a.html   # 끝난 줄 띠 · 게이지 틈 · 마무리 문구 · 운동 끝내기 빨강
python3 patch_v18_B.py _18a.html _18b.html       # 루틴 상세 · 루틴 끄기 없앰 · 끝 단추 두 줄 · 버림 빨강 · 상자 ×1.1
python3 patch_v18_C.py _18b.html _18c.html       # 종목 띠 · 종목 상자 2열 · 사진 칸 · 기본 세팅 세트 줄 · 새 종목(초성 검색 · 사전 129 · 부위/역할)
python3 patch_v18_D.py _18c.html _18d.html       # 루틴 접힘 · 넣기 2열 번호 · ✓×n · 꾹 빼기 · 넣기 높이 고정 · 새 종목 단추
python3 patch_v18_E.py _18d.html 7day-v18.html   # 탭 순서 · 업적 줄 밀기 · 업적 프로필 체크 · 닉네임 맞춤 · SNS + · 돋보기 · 당겨서 새로고침
python3 patch_v19_A.py 7day-v18.html _19a.html   # 루틴 · 플랜 · 설정 띠 · 새로고침은 띠 아래 · 검색 위 줄 고정
python3 patch_v19_B.py _19a.html _19b.html       # 보고서 띠 카메라(이미지 저장) · 공유
python3 patch_v19_C.py _19b.html _19c.html       # 넣기: 전체 기본 · 새 종목 단추 구석 · 누름=빼기/꾹=더 · 말풍선 · 칩 한 줄 ‹› · 루틴 플랜 줄
python3 patch_v19_D.py _19c.html 7day-v19.html   # 새 종목 시트 20% 고정 · 같은 이름 번호 · 칸 필수 · 운동 목표 부위 그림 · 세트 줄
python3 patch_v20.py 7day-v19.html 7day-v20.html   # 종목 이름 맞춤(12자) · 접힌 상자 간단 · 편집 · 주동/협응 둘 · 묶음 개수 지움
python3 patch_v21.py 7day-v20.html 7day-v21.html   # 운동 중 종목 빼기 ✕ · 칸 줄 ‹› · 단추 넷(세트 완료하기 상태) · 소셜 탭 · 시트 손잡이/끌어 닫기 · 점멸
