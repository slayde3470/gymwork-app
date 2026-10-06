package com.slayde.hasenheide.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * 이 앱의 디자인 값을 모아둔 파일.
 *
 * 웹 시제품에서 정한 '디자인 규칙'(기능명세 1-2)을 그대로 옮겼다.
 * 화면 코드에는 숫자를 직접 쓰지 않고 여기 이름을 가져다 쓴다.
 *  → "글씨 좀 키워줘"가 이 파일 한 줄 수정으로 끝난다.
 */

// ───────────────────────── 색 ─────────────────────────

@Immutable
data class 색표(
    val 바탕: Color,      // 화면 바탕
    val 면: Color,        // 카드
    val 면2: Color,       // 버튼 · 칩 바탕
    val 선: Color,        // 구분선 · 테두리
    val 속선: Color,      // 박스 안의 박스 테두리 (09-27: 큰 박스 = 굵은 중심색, 안쪽 박스 = 이 색)
    val 글: Color,        // 본문 글자
    val 흐림: Color,      // 보조 글자
    val 옅음: Color,      // 설명 글자
    val 강조: Color,      // 주요 버튼 · 선택 · 중심색 (예일 블루 #084B83)
    val 강조글: Color,    // 강조색 위의 글자
    val 강조옅음: Color,  // 지금 할 세트 줄 바탕
    val 좋음: Color,      // 달성
    val 좋음옅음: Color,  // 달성 알약 바탕
    val 나쁨: Color,      // 미달성 · 지우기
    val 휴식: Color,      // 휴식 · 슈퍼세트 — 09-27: 주황을 뺐다 → 중심색과 같다
    val 휴식옅음: Color,
    val 오름: Color,      // 향상도 오름 = 빨강 (주식처럼)
    val 내림: Color,      // 향상도 내림 = 중심색 (09-27)
    // 10-02 근육 그림 (07 근육지도) — 7일 체험 아티팩트 CSS --근육 --피부 --결 과 같은 값
    val 근육: Color = Color(0xFFD6DDD9),   // 칠하지 않은(0단계) 근육
    val 피부: Color = Color(0xFFB9C3BE),   // 머리 · 손 · 무릎 같은 바탕 조각
    val 결: Color = Color(0xFFAEB9B3),     // 근육 결 선
    // 10-02 업적 칭호 테두리 — 등급 색 (12-5 '등급 색'. 값은 문서에 없음 → 기본값 · 홍겸 님 확인 전)
    val 초보: Color = Color(0xFF3F8F2A),
    val 중급: Color = Color(0xFF1F6FB2),
    val 고급: Color = Color(0xFF6B3FA0),
    val 미친자: Color = Color(0xFFB3261E),
    val 유머: Color = Color(0xFFB7791F),   // 1-41 하나뿐 (말투 4단계 밖 · 스탯명세 8-4)
    val 히든: Color = Color(0xFF00796B),   // 히든 업적 (등급 없음)
    // 10-02 워밍업 세트 줄의 왼쪽 띠 — 20 B-4 · E-1 9번 '연두색' (감시관: 초록 '좋음' 은 달성에만 · D2-6)
    val 워밍업: Color = Color(0xFF7CB342),
    // 10-05 옮기기 1단계 — 시안 v18 A ① · v21 ⑦ CSS 변수와 같은 값
    val 완료바탕: Color = Color(0xFFEDF1F6),   // 끝난 세트 줄 (높이 가운데 50% 만 칠한다 · 시안 --완료바탕)
    val 지금깜빡: Color = Color(0xFFEEF3F7),   // 지금 줄 · 칸 점멸의 옅은 쪽 (강조옅음 ↔ 이 색 · 시안 --지금깜빡)
    val 노랑: Color = Color(0xFFE8A400),       // 누를 수 있다는 노란 점 (시안 --노랑)
    val 노랑테: Color = Color.Transparent,      // 노란 점 둘레 1 — 어두운 화면의 강조 단추 위에서만 (시안 --노랑테)
    // 10-05 검수: 나쁨(빨강) 바탕 위 글 — 밝음 · 어두움 모두 흰색 (목록 '어두운 화면의 빨간 버림 확인 글은 흰색' · 11_UI지침에 올릴 값)
    val 나쁨글: Color = Color(0xFFFFFFFF),
)

/**
 * 09-27 색 정돈 (시안 https://claude.ai/artifact/VSVZ8T9ucZjEvK1JCwiNne)
 *  · 가족 넷: 남(중심색 #084B83) · 먹(남을 어둡게) · 빨강 #B3261E · 초록(연두 쪽)
 *  · 옅은 색은 모두 제 가족 색을 흰색에 섞은 것. 주황은 뺐다
 */
val 밝은색표 = 색표(
    바탕 = Color(0xFFF8FAFB), 면 = Color(0xFFFFFFFF), 면2 = Color(0xFFF0F4F8), 선 = Color(0xFFD3DFE9), 속선 = Color(0xFFB5C9DA),
    글 = Color(0xFF000000), 흐림 = Color(0xFF35495B), 옅음 = Color(0xFF5E6B77),
    강조 = Color(0xFF084B83), 강조글 = Color(0xFFFFFFFF), 강조옅음 = Color(0xFFE1E9F0),
    좋음 = Color(0xFF4C9A1F), 좋음옅음 = Color(0xFFEAF3E4), 나쁨 = Color(0xFFB3261E),
    휴식 = Color(0xFF084B83), 휴식옅음 = Color(0xFFE1E9F0),
    오름 = Color(0xFFB3261E), 내림 = Color(0xFF084B83),
    근육 = Color(0xFFD6DDD9), 피부 = Color(0xFFB9C3BE), 결 = Color(0xFFAEB9B3),
    초보 = Color(0xFF3F8F2A), 중급 = Color(0xFF1F6FB2), 고급 = Color(0xFF6B3FA0), 미친자 = Color(0xFFB3261E), 유머 = Color(0xFFB7791F), 히든 = Color(0xFF00796B),
    워밍업 = Color(0xFF7CB342),
    완료바탕 = Color(0xFFEDF1F6), 지금깜빡 = Color(0xFFEEF3F7), 노랑 = Color(0xFFE8A400), 노랑테 = Color.Transparent,
)

val 어두운색표 = 색표(
    바탕 = Color(0xFF07121E), 면 = Color(0xFF111F2E), 면2 = Color(0xFF172738), 선 = Color(0xFF253A50), 속선 = Color(0xFF37526E),
    글 = Color(0xFFE5F0FA), 흐림 = Color(0xFFC5D1DB), 옅음 = Color(0xFF9BA7B3),
    강조 = Color(0xFF7FB3E6), 강조글 = Color(0xFF04213D), 강조옅음 = Color(0xFF1F3246),
    좋음 = Color(0xFF9BE06B), 좋음옅음 = Color(0xFF243A22), 나쁨 = Color(0xFFE57368),
    휴식 = Color(0xFF7FB3E6), 휴식옅음 = Color(0xFF1F3246),
    오름 = Color(0xFFE57368), 내림 = Color(0xFF7FB3E6),
    근육 = Color(0xFF4A5550), 피부 = Color(0xFF3A433F), 결 = Color(0xFF39423E),
    초보 = Color(0xFF8FD46A), 중급 = Color(0xFF7FB3E6), 고급 = Color(0xFFB79BE0), 미친자 = Color(0xFFE57368), 유머 = Color(0xFFE8B45A), 히든 = Color(0xFF5FC4B4),
    워밍업 = Color(0xFFB5E08A),
    완료바탕 = Color(0xFF162637), 지금깜빡 = Color(0xFF142536), 노랑 = Color(0xFFFFD54F), 노랑테 = Color(0xFF04213D),
)

val Local색 = staticCompositionLocalOf { 밝은색표 }

// ─────────────────────── 글자 ───────────────────────

/**
 * 글자 크기 — 여섯 단계만 쓴다: 11 · 13 · 15 · 18 · 22 · 28 (v0.5, 08 시안 1절. 예전 9단계)
 * 화면 코드가 쓰는 이름은 그대로 두고, 값만 여섯 단계 중 하나로 모았다.
 */
object 크기 {
    val 아주작게 = 11.sp   // 설명 · 단위 · 작은 이름표
    val 작게 = 11.sp
    val 조금작게 = 13.sp   // 보조 글 · 칩
    val 버튼 = 13.sp
    val 본문 = 15.sp       // 본문 · 목록 이름
    val 크게 = 18.sp       // 카드 · 시트 제목
    val 제목 = 22.sp       // 화면 제목
    val 큰숫자 = 22.sp
    val 아주큰숫자 = 28.sp // 큰 숫자
}

/** 글자 모양 — 자간은 기본 −0.01em, 제목 −0.02em */
object 글꼴 {
    fun 보통(크기값: androidx.compose.ui.unit.TextUnit, 굵기: FontWeight = FontWeight.Normal) =
        TextStyle(fontSize = 크기값, fontWeight = 굵기, letterSpacing = (-0.01).em, lineHeight = 크기값 * 1.4f)
    fun 제목(크기값: androidx.compose.ui.unit.TextUnit) =
        TextStyle(fontSize = 크기값, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em, lineHeight = 크기값 * 1.25f)
    /** 작은 이름표 — 약간 벌린다 */
    val 이름표 = TextStyle(fontSize = 크기.작게, fontWeight = FontWeight.Bold, letterSpacing = 0.04.em)
}

// ───────────────────── 간격 · 높이 · 모서리 ─────────────────────

object 간격 {
    val 아주좁게 = 4.dp
    val 좁게 = 8.dp
    val 보통 = 12.dp
    val 넓게 = 16.dp
    val 아주넓게 = 24.dp
}

/** 누르는 것의 높이 — 칩 28 · 작은 버튼 32 · 버튼·입력 40 · 목록 줄 44 (아래 탭 52) */
object 높이 {
    val 아주낮게 = 28.dp
    val 낮게 = 32.dp
    val 보통 = 40.dp
    val 높게 = 44.dp
}

/** 모서리 — 버튼 8 · 안쪽 상자 12 · 카드 16 (08 시안) */
object 모서리 {
    val 아주작게 = 8.dp   // 세트 번호 칸 같은 작은 것
    val 작게 = 8.dp       // 버튼 · 입력칸
    val 보통 = 16.dp      // 카드
    val 크게 = 16.dp      // 시트
}

/** 선 굵기 — 둘뿐 (11 지침 U3-5 · U7): 안쪽 상자 · 칩 · 입력칸 1 / 카드 · 고정 띠 2 */
object 선굵기 {
    val 보통 = 1.dp
    val 굵게 = 2.dp
}

/** 진행 막대 — 하나로 (11 지침 U3-8): 높이 8 · 끝 둥글게(= 모서리 4) · 바탕 면2 · 채움 강조 */
object 막대치수 {
    val 높이 = 8.dp
}

/** 스탯 화면 한 줄의 칸 폭 (10-02) — 이름 · 값 · 7일 전 대비 */
object 스탯치수 {
    val 이름 = 104.dp
    val 값 = 40.dp
    val 증감 = 56.dp
    /** 캘린더 년월 띠의 칭호 칩 — 넘으면 … (년월 글자 자리를 남긴다) */
    val 띠칩 = 120.dp
}

// ───────────────────── 움직임 (10-02 · UI 동작 전부) ─────────────────────

/**
 * 움직이는 시간(ms) · 크기 — 7일 체험 아티팩트 CSS `움직임 (UI 동작 전부)` 와 같은 값.
 * 숫자가 올라가는 움직임은 Motion.kt (자릿수 시간) 가 따로 맡는다.
 */
object 움직임 {
    const val 눌림 = 120          // 누르는 동안 작아지는 시간
    const val 눌림배율 = 0.96f    // 누르면 이만큼 작아진다
    const val 색 = 200            // 버튼 · 칩 색이 바뀌는 시간
    const val 스위치 = 200        // 스위치 손잡이
    const val 화면 = 200          // 탭 화면 바꿈 (페이드)
    const val 시트 = 300          // 시트가 올라오는 시간
    const val 시트닫기 = 170      // 시트가 내려가는 시간
    const val 물음 = 200          // 물음창 페이드
    const val 띠 = 240            // 아래띠가 올라오는 시간
    const val 펼침 = 250          // 카드 · 상자가 늘고 줄어드는 시간
    const val 게이지 = 700        // 게이지 폭
    const val 달 = 260            // 캘린더 달 넘김
    const val 근육색 = 600        // 근육 조각 색 (07 · 08 3절)
    const val 달밀기 = 0.25f      // 달 넘김 — 화면 폭의 이만큼 옆에서 들어온다
    const val 달흐림 = 0.6f       // 달 넘김 — 들어오기 시작할 때 이만큼 옅다
    const val 짧은띠 = 3_000      // 대표 칭호 알림처럼 짧게 보이는 아래띠 (ms)
    const val 사진접힘 = 250      // 나가는 사진이 오른쪽 끝으로 접히는 시간
    const val 사진펼침 = 450      // 새 사진이 펼쳐지는 시간
    const val 사진펼침늦춤 = 120  // 접히기 시작하고 이만큼 뒤에 펼친다
    const val 접힌폭 = 0.03f      // 다 접혔을 때의 가로 배율
    const val 톡시작 = 0.7f       // 세트 체크 — 작게 시작해
    const val 톡탄성 = 0.35f      // 살짝 넘쳤다가 돌아온다 (스프링 감쇠비)
    // 10-05 옮기기 1단계 — 알림 · 시트 · 당김 · 점멸 (시안 v17 D · v18 E ⑦ · v21 ⑥⑦). '× 0.75' 는 v17 홍겸 님 비율
    const val 토스트 = 1_500       // 토스트 (2초 × 0.75)
    const val 되돌림띠 = 4_500     // [되돌리기] 띠 (6초 × 0.75)
    const val 업적띠 = 3_750       // [보기] 업적 띠 (5초 × 0.75)
    const val 띠흐림 = 200         // 띠 · 토스트가 나타나고 사라지는 흐려짐
    const val 누름기억 = 2_000     // 이 안에 누른 단추가 있으면 띠를 그 단추 위에 띄운다
    const val 시트제자리 = 200     // 덜 끌어내린 시트가 제자리로 돌아가는 시간
    const val 당김돎 = 500         // 당겨서 새로고침 — 도는 시간
    const val 당김접힘 = 200       // 당김 줄이 접히는 시간
    const val 점멸 = 1_000         // 지금 줄 · 노란 점 깜빡임 한 번 (밝게 → 옅게 → 밝게)
    const val 점멸옅음 = 0.15f     // 노란 점이 가장 옅을 때
    const val 끌림투명 = 0.35f     // 끌고 있는 것 (11 지침 U5-5)
    /** 빨리 나와 끝에서 부드럽게 멈춘다 — 아티팩트 cubic-bezier(.2,.8,.2,1) */
    val 부드럽게 = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)
}

/** 운동 중 위쪽 그림 칸 · 종목 사진 크기 (08 시안 3 · 4절) */
object 그림칸 {
    val 높이 = 124.dp           // 평소
    val 사이 = 8.dp             // 11 지침 U3-1 (4 · 8 · 12 · 16 · 24)
    val 모서리 = 8.dp           // 안쪽 상자 (U3-4)
    val 닫기 = 24.dp            // ✕ 단추
    val 더미폭 = 3.dp           // 오른쪽 접힌 사진 더미 한 장 (줄 굵기 — 여백이 아니다)
    val 더미사이 = 1.dp         // 더미 줄 사이 (줄 굵기)
    const val 더미최대 = 9
    val 사진 = 72.dp            // 종목 탭 사진 칸
    val 작은사진 = 28.dp        // 종목 줄 왼쪽
    const val 사진긴변 = 1080   // 저장할 때 긴 변(px)
    const val 사진품질 = 85     // JPEG
    const val 정리유예ms = 120_000L  // 어느 종목에도 없는 사진 파일 — 이보다 오래된 것만 치운다 (넣는 중인 파일은 둔다)
    // 근육 그림 선 — 그림 좌표 단위 (아티팩트 CSS .몸근 · .몸결 과 같다)
    const val 근육선 = 1.2f     // 근육 조각 사이 테두리 (바탕색 면)
    const val 결선 = 0.7f       // 근육 결
    const val 결진하기 = 0.8f   // 결의 불투명도
}

/**
 * 공용 부품 치수 (10-05 옮기기 1단계 · ui/Parts.kt) — 시안 CSS 값 그대로.
 * 11 지침 단계(4 · 8 · 12 · 16 · 24 등) 밖의 값은 시안에서 홍겸 님이 정한 값이다 — 지침에 올릴 것 (보고서)
 */
object 부품치수 {
    // 시트 손잡이 막대 (v21 ⑥) · 끌어내려 닫기
    val 손잡이폭 = 36.dp
    val 손잡이두께 = 4.dp
    val 손잡이위 = 4.dp            // 시트 위끝에서
    val 손잡이모서리 = 2.dp
    val 끌기시작 = 8.dp            // 이만큼 움직여야 끌기로 본다 (시안 공통)
    val 시트닫기 = 80.dp           // 이보다 많이 끌어내리고 놓으면 닫힌다
    const val 시트위끝 = 0.1f      // 위끝 고정 시트 — 화면 높이의 이 지점에 위끝 (10-06 홍겸 님 20% → 10%)
    // 머리 띠 (= Motion.kt 띠 · 캘린더 년월 띠)
    val 띠세로여백 = 6.dp
    // 칩줄 ‹ › 동그라미 (v19 C ④ · v21 ③)
    val 칩화살 = 28.dp
    val 칩화살그림 = 16.dp
    const val 칩화살넘김 = 0.7f    // 한 번 누르면 줄 폭의 이만큼
    // − 값 ＋ 칸
    val 값칸단추 = 28.dp
    // 딱지 [플랜] · 같은 이름 번호 — 이름 오른쪽 위에 겹친다 (jmg1 · v19 D ②)
    val 딱지위 = (-6).dp
    val 번호딱지위 = (-5).dp
    val 딱지겹침 = (-5).dp
    val 딱지글높이 = 14.sp
    val 딱지옆 = 6.dp
    val 번호딱지옆 = 4.dp
    val 번호딱지폭 = 16.dp
    // 가로 줄 꾹 끌기 (시안 `끌` 운칸 · 업적)
    val 끌기끝 = 32.dp             // 손이 줄 끝에서 이 안에 들면 그쪽으로 넘긴다 (운동 칸 줄 오른쪽은 [＋] 때문에 76)
    val 끌기넘김 = 10.dp           // 한 번(한 화면 그림)에 넘기는 양
    val 놓을선 = 3.dp              // 놓을 자리 선 (11 지침 U5-5 '위아래 = 3dp 선' — 가로 줄은 왼쪽 · 오른쪽)
    // 노란 점 (v21 ⑦)
    val 노란점 = 6.dp
    val 노란점테 = 1.dp
    // 당겨서 새로고침 (v18 E ⑦)
    val 당김줄 = 40.dp             // 이만큼 당기고 놓으면 새로고침 · 도는 동안 줄 높이
    val 당김최대 = 56.dp
    const val 당김비율 = 0.5f      // 손가락이 움직인 만큼의 이 비율로 내려온다
    // 알림 띠 · 토스트 (v17 D 5)
    val 토스트위 = 50.dp           // 누른 단추가 없을 때 토스트 자리 (위에서)
    val 띠틈 = 8.dp                // 누른 단추와 띠 사이
    val 누름반 = 20.dp             // 누른 자리 → 단추 윗변 (높이 40 단추의 반 — 앱은 단추 테두리 대신 누른 점을 안다)
    // 아래 탭 (탭줄 · 시안 CSS .탭줄)
    val 탭위 = 6.dp
    val 탭아래 = 8.dp
    val 탭사이 = 2.dp
    val 탭그림 = 18.dp
    val 탭사진 = 28.dp
    val 탭사진고리 = 2.dp
    val 탭자간 = (-0.04).em           // 9칸 — 글을 자르지 않고 자간만 (시안 v17 C ④ · 11 지침 U2-3 의 예외)
}

/** 휴식 − ＋ 칸 — 15초씩 · 0:15 ~ 5:00 (시안 10-03 ypk0 `휴식폭 · 휴식최소 · 휴식최대`) */
object 휴식칸값 {
    const val 폭 = 15
    const val 최소 = 15
    const val 최대 = 300
}

/** 이름 맞춤 (시안 v20 ①) — 12자 넘으면 두 줄 · 15 → 13 → 11 → 자간 −0.2em 까지 · … 로 자르지 않는다 */
object 이름맞춤값 {
    const val 한줄최대 = 12
    val 글단계 = listOf(15f, 13f, 11f)
    const val 자간끝 = -0.2f
    const val 자간폭 = 0.01f
}

// ───────────────────────── 적용 ─────────────────────────

@Composable
fun 하젠하이데테마(
    어둡게: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val 색 = if (어둡게) 어두운색표 else 밝은색표
    val 기본 = if (어둡게) darkColorScheme(primary = 색.강조, onPrimary = 색.강조글, background = 색.바탕, surface = 색.면, onSurface = 색.글, onBackground = 색.글)
              else lightColorScheme(primary = 색.강조, onPrimary = 색.강조글, background = 색.바탕, surface = 색.면, onSurface = 색.글, onBackground = 색.글)
    CompositionLocalProvider(Local색 provides 색) {
        MaterialTheme(colorScheme = 기본, content = content)
    }
}

// ── 10-06 v22 R ──
/**
 * 운동 보고서 (시안 v22 D · v23 ②③ · v24 ①) — 11 지침 단계 밖의 값은 홍겸 님이 시안에서 정한 값이다 (지침에 올릴 것)
 */
object 보고떠값 {
    /** 떠 있는 ‹ › 동그라미 지름 — v22 D 40 → v23 ③ 25% 작게 */
    val 지름 = 30.dp
    /** 화살표 — 18 → 13.5 */
    val 화살 = 13.5.dp
    /** 화살표 선 굵기(그림 좌표 24 기준) — 2 → 3 (굵게) */
    const val 화살선 = 3f
    /** 좌우 · 아래 (U3-2) */
    val 옆 = 12.dp
    /** 루틴 상자 이름 칸 — 이름 글자(13 Bold) 7자 + 오른쪽 8 (v24 ①) */
    const val 이름칸글자 = 7
    val 이름칸뒤 = 8.dp
    /** 루틴 상자 위아래 안 여백 — 6 → 4 (v24 ① 높이 5% 줄임) · 좌우 8 (v23 ②) */
    val 루틴위아래 = 4.dp
    val 루틴옆 = 8.dp
    /** 프로필을 꺼서 톱니가 루틴 상자에 올 때 오른쪽 여백 (v23 ② .톱니있음 padding-right 32) */
    val 톱니있음옆 = 32.dp
}
