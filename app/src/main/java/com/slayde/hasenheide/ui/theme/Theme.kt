package com.slayde.hasenheide.ui.theme

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
)

val 어두운색표 = 색표(
    바탕 = Color(0xFF07121E), 면 = Color(0xFF111F2E), 면2 = Color(0xFF172738), 선 = Color(0xFF253A50), 속선 = Color(0xFF37526E),
    글 = Color(0xFFE5F0FA), 흐림 = Color(0xFFC5D1DB), 옅음 = Color(0xFF9BA7B3),
    강조 = Color(0xFF7FB3E6), 강조글 = Color(0xFF04213D), 강조옅음 = Color(0xFF1F3246),
    좋음 = Color(0xFF9BE06B), 좋음옅음 = Color(0xFF243A22), 나쁨 = Color(0xFFE57368),
    휴식 = Color(0xFF7FB3E6), 휴식옅음 = Color(0xFF1F3246),
    오름 = Color(0xFFE57368), 내림 = Color(0xFF7FB3E6),
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
