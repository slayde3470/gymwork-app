# 마구잡이 시험기 (10-10)

Gradle 시험이 아니다 — 사람이 하듯 조작을 무작위로 섞어 앞뒤가 맞는지 보는 도구. 클라우드에서 kotlinc 2.0.21 로 돌린다 (`LC_ALL=C.utf8`).

- `Fuzz.kt` — 운동 **한 번 안**의 조작 (체크 · 세트 지우기/되살리기 · 종목 빼기/넣기/옮기기 · 끝냄 · 보고저장 · 방치 3시간)
- `Month.kt` — **30일 추적**: 날짜(자정 포함) · 앱 열기(1분 틱) · 운동 시작/도중 종료/자동 종료 · 캘린더 기록 지우기/되살리기 · 루틴 지우기/되살리기/수정 · 예정 · 그 날 운동 · 플랜 · 구식 되돌림 띠

## 돌리는 법 (요약)
1. `data/*.kt` (Storage.kt 뺀 것) 을 kotlinc 로 `data.jar`
2. `WorkoutScreen.kt` 의 순수 계산 구간(`internal fun 운남음` ~ 끝)과 `CalendarScreen.kt` 의 `캘같은종목` ~ `캘요일글` 을 `package com.slayde.hasenheide.ui` 파일로 잘라 낸다
3. `kotlinc 잘라낸것들.kt Fuzz.kt Month.kt -cp data.jar -d fz.jar`
4. `java -cp fz.jar:data.jar:kotlin-stdlib.jar com.slayde.hasenheide.ui.M30 <판수> <사건수>` (30일), `...FuzzKt <판수> <길이>` (운동 한 번)
   - 환경 변수 `NOSNAP=1` 구식 되돌림 띠 끄기 · `LOGN=80` 위반 때 보여 줄 조작 수
5. 같은 기록 → 같은 결과 (판 번호 = 난수 씨앗). 고치기 전 코드로 돌려 옛 버그가 다시 잡히는지로 시험기 자체를 확인한다
