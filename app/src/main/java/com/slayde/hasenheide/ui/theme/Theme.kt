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
    val 글: Color,        // 본문 글자
    val 흐림: Color,      // 보조 글자
    val 옅음: Color,      // 설명 글자
    val 강조: Color,      // 주요 버튼 · 선택
    val 강조글: Color,    // 강조색 위의 글자
    val 강조옅음: Color,  // 지금 할 세트 줄 바탕
    val 좋음: Color,      // 달성
    val 나쁨: Color,      // 미달성 · 지우기
    val 휴식: Color,      // 휴식 띠 · 슈퍼세트
    val 휴식옅음: Color,
    val 오름: Color,      // 향상도 오름 = 빨강 (주식처럼)
    val 내림: Color,      // 향상도 내림 = 파랑
)

val 밝은색표 = 색표(
    바탕 = Color(0xFFF7F8F7), 면 = Color(0xFFFFFFFF), 면2 = Color(0xFFF0F2F1), 선 = Color(0xFFDDE2E0),
    글 = Color(0xFF1A1C1B), 흐림 = Color(0xFF6B7F76), 옅음 = Color(0xFF9AA8A2),
    강조 = Color(0xFF2E5E4E), 강조글 = Color(0xFFFFFFFF), 강조옅음 = Color(0xFFE2EEE8),
    좋음 = Color(0xFF2E7D57), 나쁨 = Color(0xFFB3261E), 휴식 = Color(0xFFB06A1F), 휴식옅음 = Color(0xFFFBEEDD),
    오름 = Color(0xFFD92D20), 내림 = Color(0xFF1668DC),
)

val 어두운색표 = 색표(
    바탕 = Color(0xFF121413), 면 = Color(0xFF1E211F), 면2 = Color(0xFF272B29), 선 = Color(0xFF343936),
    글 = Color(0xFFE2E3E1), 흐림 = Color(0xFF9FB2A9), 옅음 = Color(0xFF6F7F78),
    강조 = Color(0xFF8FD0B8), 강조글 = Color(0xFF00382A), 강조옅음 = Color(0xFF213029),
    좋음 = Color(0xFF8FD0B8), 나쁨 = Color(0xFFF2B8B5), 휴식 = Color(0xFFE8B87A), 휴식옅음 = Color(0xFF302819),
    오름 = Color(0xFFFF7A6B), 내림 = Color(0xFF66B0FF),
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
