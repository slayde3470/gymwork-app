package com.slayde.hasenheide.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 이 앱의 디자인 값을 모아둔 파일.
 *
 * 색 · 글자 크기 · 굵기 · 간격 · 모서리 둥글기를 전부 여기서만 정한다.
 * 화면 코드에는 숫자를 직접 쓰지 않고 여기 이름을 가져다 쓴다.
 *
 * 왜 이렇게 하나:
 *  1) "글씨 좀 키워줘"가 이 파일 한 줄 수정으로 끝난다
 *  2) 나중에 만들 "앱 안에서 디자인 바꾸기" 기능이 이 구조를 그대로 쓴다
 */

// ───────────────────────── 색 ─────────────────────────

/** 밝은 화면에서 쓰는 색 */
private val 밝은색 = lightColorScheme(
    primary = Color(0xFF2E5E4E),        // 주요 버튼, 강조
    onPrimary = Color(0xFFFFFFFF),      // 주요 버튼 위의 글자
    secondary = Color(0xFF6B7F76),      // 보조 요소
    background = Color(0xFFF7F8F7),     // 화면 바탕
    onBackground = Color(0xFF1A1C1B),   // 바탕 위의 글자
    surface = Color(0xFFFFFFFF),        // 카드, 목록 한 칸
    onSurface = Color(0xFF1A1C1B),
    error = Color(0xFFB3261E),
)

/** 어두운 화면(다크 모드)에서 쓰는 색 */
private val 어두운색 = darkColorScheme(
    primary = Color(0xFF8FD0B8),
    onPrimary = Color(0xFF00382A),
    secondary = Color(0xFFB0CCC1),
    background = Color(0xFF121413),
    onBackground = Color(0xFFE2E3E1),
    surface = Color(0xFF1E211F),
    onSurface = Color(0xFFE2E3E1),
    error = Color(0xFFF2B8B5),
)

// ─────────────────────── 글자 크기 ───────────────────────

/**
 * 화면 부위별 글자 모양.
 * 나중에 "여기 글씨만 키우고 싶다"고 할 때 해당 줄만 고치면 된다.
 */
val 글자 = Typography(
    // 큰 제목 — 화면 맨 위
    headlineLarge = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
    // 중간 제목 — 루틴 이름, 종목 이름
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    // 본문
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
    // 작은 설명
    bodySmall = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal),
    // 버튼 글자
    labelLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
)

/** 무게·횟수처럼 크게 보여야 하는 숫자 */
val 큰숫자 = TextStyle(fontSize = 44.sp, fontWeight = FontWeight.Bold)

/** 휴식 타이머 숫자 */
val 타이머숫자 = TextStyle(fontSize = 72.sp, fontWeight = FontWeight.Bold)

// ───────────────────── 간격 · 모서리 ─────────────────────

object 간격 {
    val 아주좁게 = 4.dp
    val 좁게 = 8.dp
    val 보통 = 16.dp
    val 넓게 = 24.dp
    val 아주넓게 = 40.dp
}

object 모서리 {
    val 작게 = 8.dp
    val 보통 = 16.dp
    val 둥글게 = 28.dp
}

/** 손가락으로 누르는 버튼의 최소 높이. 헬스장에서 급하게 눌러도 안 빗나가게 넉넉히. */
val 버튼높이 = 56.dp

// ───────────────────────── 적용 ─────────────────────────

@Composable
fun 하젠하이데테마(
    어둡게: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (어둡게) 어두운색 else 밝은색,
        typography = 글자,
        content = content
    )
}
