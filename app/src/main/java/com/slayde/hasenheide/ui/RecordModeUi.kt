package com.slayde.hasenheide.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.slayde.hasenheide.data.기록방식
import com.slayde.hasenheide.data.무게글
import com.slayde.hasenheide.data.거리칸
import com.slayde.hasenheide.data.무게칸있음
import com.slayde.hasenheide.data.방식
import com.slayde.hasenheide.data.시간칸
import com.slayde.hasenheide.data.실제무게
import com.slayde.hasenheide.data.앱데이터
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.크기

/**
 * 기록 방식 1단계 화면 (10-12 · 문서/기록방식_설계.md) — 무게 칸 머리 글 + 칸 아래 작게 '= 69kg'.
 * 계산은 data/기록방식.kt 의 [실제무게] 하나만 쓴다 (여기서는 글로 바꿀 뿐).
 */

/**
 * 무게 칸 머리 글. 표의 줄이 모두 맨몸이면 '추가 kg' · 모두 어시스트면 '보조 kg', 그 밖(섞임 · 일반 · 플랜 줄뿐)은 [기본].
 * [방식들] 에는 플랜 줄을 넣지 않는다 (플랜 줄은 바꾸지 않는다)
 */
internal fun 무게머리글(방식들: Collection<기록방식>, 기본: String): String {
    if (방식들.isEmpty()) return 기본
    // 횟수만 · 시간 줄은 무게 칸이 없다 — 머리 글은 무게 칸이 있는 줄만 보고 정한다. 모두 없으면 머리도 비운다 (2단계)
    val 있는 = 방식들.filter { it.무게칸있음() }
    return when {
        있는.isEmpty() -> ""
        있는.all { it == 기록방식.맨몸 } -> "추가 kg"
        있는.all { it == 기록방식.어시스트 } -> "보조 kg"
        있는.all { it.거리칸() } -> "거리 km"   // 3단계 유산소: 무게 칸 자리가 거리
        else -> 기본
    }
}

/** 횟수 칸 머리 글 — 줄이 모두 시간(버티기)이면 '시간', 그 밖은 [기본] ('횟수' · '회') */
internal fun 횟수머리글(방식들: Collection<기록방식>, 기본: String = "횟수"): String =
    if (방식들.isNotEmpty() && 방식들.all { it.시간칸() }) "시간" else 기본

/**
 * 칸 아래 작은 글 '= 167kg' — 10-13 홍겸 님: **보이지 않는다.** 줄 높이가 달라져 줄이 안 맞았고, 입력하면 체중에 더하거나 빼서 기록하는 계산(실제무게)만 있으면 된다.
 * 부르는 곳이 많아 함수는 남기고 늘 null 을 돌려준다 (null 이면 칸만 그려져 높이가 변하지 않는다)
 */
@Suppress("UNUSED_PARAMETER")
internal fun 앱데이터.실제무게글(종id: String?, 이름: String, 플랜id: String?, 입력: Double): String? = null

/** 줄 하나의 방식 (플랜 줄은 null = 머리 글 계산에서 뺀다) */
internal fun 앱데이터.머리방식(종id: String?, 이름: String, 플랜id: String?): 기록방식? =
    if (플랜id != null) null else 방식(종id, 이름)

/** 무게 칸 + 그 아래 작은 글. [작은글] 이 null 이면 칸만 (높이가 변하지 않는다) */
@Composable
internal fun 무게칸아래글(작은글: String?, modifier: Modifier = Modifier, 칸: @Composable () -> Unit) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        칸()
        if (작은글 != null) 글(작은글, Modifier.fillMaxWidth(), 크기값 = 크기.작게, 색 = Local색.current.옅음, 가운데 = true)
    }
}
