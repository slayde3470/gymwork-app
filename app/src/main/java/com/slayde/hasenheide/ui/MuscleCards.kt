package com.slayde.hasenheide.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.slayde.hasenheide.data.근육표
import com.slayde.hasenheide.data.종목사전
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.그림칸
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.선굵기
import com.slayde.hasenheide.ui.theme.크기

/*
 * 근육 그림 2장 (10-09 홍겸 님 — 새 규칙) — 종목 탭 펼친 상자 · 종목 넣기 시트의 펼친 상자 · 새 종목 시트의 '운동 목표 부위'가 같이 쓴다.
 *  · 왼쪽 = 반신 그림 (상반신 전면 · 상반신 후면 · 하반신 전면 · 하반신 후면 중 근육이 가장 많이 걸린 것)
 *  · 오른쪽 = 확대 그림 자리 — 확대 그림은 아직 없어서 빈 칸에 필요한 그림 이름만 적어 둔다
 *  · 근육이 하나도 없으면 가운데 한 칸에 전신 앞 · 뒤만
 * 반신 · 확대 고르기 표는 이 파일 한곳에만 둔다 (시험: test/…/ui/MuscleCardsTest.kt)
 */

/** 반신 그림 네 가지 — [상자] = 자르기 상자(data/Muscle.kt `근육표`) · [뒤] = 뒷모습 */
internal enum class 반신(val 글: String, val 상자: FloatArray, val 뒤: Boolean) {
    상전("상반신 전면", 근육표.앞위, false),
    상후("상반신 후면", 근육표.뒤위, true),
    하전("하반신 전면", 근육표.앞아래, false),
    하후("하반신 후면", 근육표.뒤아래, true),
}

/** 확대 그림 (아직 없음) — [이름] 은 칸 첫 줄 · [각도] 가 있으면 둘째 줄 앞에 '45° · ' */
internal enum class 확대(val 이름: String, val 각도: Boolean, val 둘째: String) {
    가슴("가슴 확대", true, "그림 준비 중"),
    팔어깨("팔~어깨 확대", true, "그림 준비 중"),
    등("등 확대", false, "그림 준비 중"),
    하체("굽힌 허벅지·종아리 확대", true, "그림 준비 중"),
    복근("복근 확대", false, "규칙 없음"),   // 복근만 있을 때의 확대는 홍겸 님 규칙이 아직 없다 (10-09 보고: 확인 필요)
    ;

    /** 둘째 줄 글 — 예 '45° · 그림 준비 중' */
    fun 둘째줄(): String = if (각도) "45° · $둘째" else 둘째
}

/** 고른 결과 — [반신] 이 null 이면 근육이 없다(전신 한 칸). [확대] 는 [반신] 이 있을 때만 */
internal data class 두장고름(val 반신: 반신?, val 확대: 확대?)

// ── 근육 id 묶음 (10-09 홍겸 님 표 그대로) ──
private val 상반신앞 = setOf(
    "chest_upper", "chest_mid", "chest_lower", "serratus", "delt_front", "delt_side", "biceps", "brachialis", "forearm", "abs", "obliques",
)
private val 상반신뒤 = setOf("lats", "rhomboids", "teres_major", "traps", "lower_back", "delt_rear", "triceps")
private val 하반신앞 = setOf("quads", "adductors", "shin")
private val 하반신뒤 = setOf("hamstrings", "glutes", "calves")

private val 가슴근 = setOf("chest_upper", "chest_mid", "chest_lower", "serratus")
private val 팔근 = setOf("biceps", "brachialis", "triceps", "forearm")
private val 어깨근 = setOf("delt_front", "delt_side", "delt_rear")
private val 등근 = setOf("lats", "rhomboids", "teres_major", "traps", "lower_back")
private val 하체근 = setOf("quads", "hamstrings", "glutes", "adductors", "calves", "shin")
private val 복근근 = setOf("abs", "obliques")

/** 점수 — 주동(P) 2 · 협응(Y) · 보조(S) 1 */
private fun 점수(역할: String): Int = if (역할 == "P") 2 else 1

/**
 * 반신 · 확대 고르기. [근육] = 근육 id → 역할(P · Y · S). 다른 층의 id 는 세부 부위로 옮겨(`종목사전.세부로`) 센다.
 *  · 반신: 네 가지 중 점수가 가장 큰 것 — 같으면 전면 먼저, 그 안에서 상반신 먼저 (상전 → 하전 → 상후 → 하후)
 *  · 확대: 가슴 근육과 팔 · 어깨가 함께 있으면 팔~어깨. 그 밖에는 무리 점수(가슴 · 팔/어깨 · 등 · 하체)가 가장 큰 것 — 같으면 가슴 → 등 → 팔~어깨 → 하체.
 *    어느 무리에도 안 걸리고 복근만 있으면 복근(규칙 없음)
 *  · 점수가 하나도 없으면 (null, null) = 전신 한 칸
 */
internal fun 근육두장고름(근육: Map<String, String>): 두장고름 {
    val m = 종목사전.세부로(근육)
    fun 합(ids: Set<String>) = m.entries.filter { it.key in ids }.sumOf { 점수(it.value) }
    fun 있음(ids: Set<String>) = m.keys.any { it in ids }
    val 반신점수 = listOf(반신.상전 to 합(상반신앞), 반신.하전 to 합(하반신앞), 반신.상후 to 합(상반신뒤), 반신.하후 to 합(하반신뒤))
    val 최고 = 반신점수.maxOf { it.second }
    if (최고 <= 0) return 두장고름(null, null)
    val 고른반신 = 반신점수.first { it.second == 최고 }.first

    val 팔어깨근 = 팔근 + 어깨근
    val 고른확대 = if (있음(가슴근) && 있음(팔어깨근)) 확대.팔어깨
    else {
        val 무리 = listOf(확대.가슴 to 합(가슴근), 확대.등 to 합(등근), 확대.팔어깨 to 합(팔어깨근), 확대.하체 to 합(하체근))
        val 큰 = 무리.maxOf { it.second }
        if (큰 > 0) 무리.first { it.second == 큰 }.first else if (합(복근근) > 0) 확대.복근 else null
    }
    return 두장고름(고른반신, 고른확대)
}

/**
 * 근육 그림 2장 한 줄 — 운동 중 그림 칸과 같은 크기 · 모서리 · 테(높이 124 · 사이 8 · 모서리 8).
 * @param 근육 근육 id → 역할 (칠하는 값은 새 종목 시트와 같은 [새몸단계])
 * @param 누름 그림 어디를 눌러도 부른다 — 반신 그림에서 근육 조각을 눌렀으면 그 부위들(첫째 = 조각의 근육), 아니면 빈 목록
 */
@Composable
internal fun 근육두장(근육: Map<String, String>, 색표: String, 누름: (List<String>) -> Unit, modifier: Modifier = Modifier) {
    val 단계 = remember(근육) { 새몸단계(근육) }
    val 고름 = remember(근육) { 근육두장고름(근육) }
    val 반 = 고름.반신
    Row(modifier.fillMaxWidth().height(그림칸.높이), horizontalArrangement = Arrangement.spacedBy(그림칸.사이)) {
        if (반 == null) {
            // 근육이 없다 — 가운데 한 칸(두 칸 중 하나와 같은 폭)에 전신 앞 · 뒤
            Box(Modifier.weight(0.5f))
            근육그림칸(단계, 색표, null, null, 누름, Modifier.weight(1f))
            Box(Modifier.weight(0.5f))
        } else {
            근육그림칸(단계, 색표, 반.상자, 반.뒤, 누름, Modifier.weight(1f), 설명 = "${반.글} 근육 고르기")
            빈확대칸(고름.확대, 누름, Modifier.weight(1f))
        }
    }
}

/**
 * 근육 그림 한 칸 — 둥근 판 + 몸그림. [자르기] null = 앞 · 뒤 전신.
 * 누른 자리 판정은 새 종목 시트의 [그림누른부위] 를 그대로 쓴다 ([뒤] null = 앞 · 뒤 둘 다 찾는다)
 */
@Composable
private fun 근육그림칸(
    단계: Map<String, Double>, 색표: String, 자르기: FloatArray?, 뒤: Boolean?, 누름: (List<String>) -> Unit, modifier: Modifier,
    설명: String = "근육 고르기",
) {
    val c = Local색.current
    val 누름최신 by rememberUpdatedState(누름)
    val 모양 = RoundedCornerShape(그림칸.모서리)
    val 판 = 자르기 ?: floatArrayOf(0f, 0f, 근육표.그림폭, 근육표.그림높이)
    Box(modifier.fillMaxHeight().clip(모양).background(c.면).border(선굵기.보통, c.선, 모양).semantics { contentDescription = 설명 }) {
        Box(
            Modifier.fillMaxSize().padding(간격.아주좁게).pointerInput(뒤, 자르기) {
                detectTapGestures { o ->
                    누름최신(그림누른부위(o.x, o.y, size.width.toFloat(), size.height.toFloat(), 판, 뒤) ?: emptyList())
                }
            },
        ) { 몸그림(단계, 색표, 자르기, Modifier.fillMaxSize()) }
    }
}

/** 확대 그림 자리 — 그림이 아직 없어 같은 크기 · 테의 빈 칸에 필요한 그림 이름만 (가운데 · 작고 흐린 글 두 줄). 눌러도 근육 고르기가 열린다 */
@Composable
private fun 빈확대칸(확대: 확대?, 누름: (List<String>) -> Unit, modifier: Modifier) {
    val c = Local색.current
    val 모양 = RoundedCornerShape(그림칸.모서리)
    Box(
        modifier.fillMaxHeight().clip(모양).background(c.면).border(선굵기.보통, c.선, 모양)
            .semantics { contentDescription = "${확대?.이름 ?: "확대"} 자리 · 근육 고르기" }
            .눌림 { 누름(emptyList()) },
        contentAlignment = Alignment.Center,
    ) {
        if (확대 != null) Column(
            Modifier.padding(horizontal = 간격.좁게), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(간격.아주좁게),
        ) {
            맞춤글(확대.이름, 최대 = 크기.아주작게, 색 = c.옅음)
            맞춤글(확대.둘째줄(), 최대 = 크기.아주작게, 색 = c.옅음)
        }
    }
}
