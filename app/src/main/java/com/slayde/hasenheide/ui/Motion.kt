package com.slayde.hasenheide.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawOutline
import kotlinx.coroutines.launch
import androidx.compose.ui.composed
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slayde.hasenheide.data.kg글
import com.slayde.hasenheide.data.비교
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.글꼴
import com.slayde.hasenheide.ui.theme.크기
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * 움직임 · 띠 — 09-27 시안(https://claude.ai/artifact/VSVZ8T9ucZjEvK1JCwiNne)에서 정한 것.
 *
 * 숫자가 올라가는 시간은 **자릿수**로 정한다 (09-27 홍겸 님)
 *   1자리 1초 · 2자리 1.25초 · 3자리 1.5초 · 4자리 2초 · 5자리 2.5초 · 6자리 3초 (7자리부터 한 자리에 0.5초씩)
 *   운동 중 실시간으로 바뀌는 숫자는 자주 바뀌므로 각 구간에서 0.5초씩 뺀다 (0.5 · 0.75 · 1 · 1.5 · 2 · 2.5초)
 *   처음엔 빠르고 끝에서 느려진다(ease-out)
 */
fun 자릿수(v: Double): Int = abs(v).roundToLong().toString().length

fun 올림시간(v: Double, 빠르게: Boolean): Int {
    val n = 자릿수(v)
    val 초 = when (n) { 1 -> 1.0; 2 -> 1.25; 3 -> 1.5; 4 -> 2.0; 5 -> 2.5; 6 -> 3.0; else -> 3.0 + 0.5 * (n - 6) }
    return ((if (빠르게) 초 - 0.5 else 초) * 1000).toInt()
}

private val 끝에서느리게 = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)   // ease-out cubic

/**
 * 움직이는 숫자 — 지금 보일 값을 돌려준다.
 *  · 영부터 = true: 처음 보일 때 0 에서 목표까지 올라간다 (마무리 · 캘린더 판)
 *  · 영부터 = false: 처음엔 그 값 그대로, 값이 바뀔 때마다 옛 값 → 새 값 (운동 중 실시간)
 *  · 열쇠가 바뀌면 처음부터 다시 (다른 날을 고르면 다시 올라간다)
 */
@Composable
fun 움직수(목표: Double, 영부터: Boolean, 빠르게: Boolean = false, 열쇠: Any? = null): Double {
    val a = remember(열쇠) { Animatable(if (영부터) 0f else 목표.toFloat()) }
    LaunchedEffect(목표, 열쇠) {
        if (a.value != 목표.toFloat()) a.animateTo(목표.toFloat(), tween(올림시간(목표, 빠르게), easing = 끝에서느리게))
    }
    return a.value.toDouble()
}

/** 한 번만 0 → 1 로 (0.8초) — 제목이 왼쪽부터 드러날 때 */
@Composable
fun 드러남값(열쇠: Any? = Unit): Float {
    val a = remember(열쇠) { Animatable(0f) }
    LaunchedEffect(열쇠) { a.animateTo(1f, tween(800, easing = CubicBezierEasing(0.22f, 0.61f, 0.36f, 1f))) }
    return a.value
}

/** 왼쪽에서 오른쪽으로 드러나며 옅게 → 진하게 (09-27: 튀어나오기는 촌스럽다) */
fun Modifier.드러남(p: Float): Modifier = this
    .graphicsLayer { alpha = p; translationX = -6.dp.toPx() * (1 - p) }
    .drawWithContent { clipRect(right = size.width * p) { this@drawWithContent.drawContent() } }

/**
 * 띠 — 캘린더 년월 · 운동 중 이름 줄 · 마무리 제목이 모두 같은 규격 (09-27)
 * 높이 40 이상 · 여백 6/12 · 글자 18 굵게 · 중심색 바탕 + 흰 글자
 */
@Composable
fun 띠(modifier: Modifier = Modifier, 모서리값: RoundedCornerShape? = null, 가운데: Boolean = false, content: @Composable RowScope.() -> Unit) {
    val c = Local색.current
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp)
            .then(if (모서리값 != null) Modifier.clip(모서리값) else Modifier)
            .background(c.강조)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (가운데) Arrangement.Center else Arrangement.spacedBy(6.dp),
        content = content,
    )
}

/** 띠 글자 — 18 굵게 흰색 */
@Composable
fun 띠글(text: String, modifier: Modifier = Modifier, 가운데: Boolean = false) {
    val c = Local색.current
    Text(text, modifier, style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 23.sp),
        color = c.강조글, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = if (가운데) TextAlign.Center else null)
}

/** 달성 알약 — 옅은 초록 바탕 + 초록 글자 */
@Composable
fun 달성알약(달성: Boolean) {
    val c = Local색.current
    val 색 = if (달성) c.좋음 else c.나쁨
    Box(Modifier.clip(RoundedCornerShape(99.dp)).background(if (달성) c.좋음옅음 else c.나쁨.copy(alpha = 0.12f)).padding(horizontal = 7.dp, vertical = 1.dp)) {
        Text(if (달성) "달성" else "미달성", style = 글꼴.보통(10.5.sp, FontWeight.Bold), color = 색, maxLines = 1)
    }
}

/**
 * 대비 칩 — [1주 대비 ▲ 5kg] (09-27)
 *  · 비교할 것이 없으면 칩을 그리지 않는다
 *  · 차이가 없으면 '유지'
 *  · 숫자는 움직인다 (영부터 · 빠르게는 움직수와 같다). 0 을 지나면 ▲▼ 와 색이 바뀐다
 */
@Composable
fun 대비칩(이름: String, v: 비교?, 영부터: Boolean = false, 빠르게: Boolean = false, 열쇠: Any? = null) {
    if (v == null) return
    val c = Local색.current
    val 값 = 움직수(v.diff, 영부터, 빠르게, 열쇠)
    Row(
        Modifier.border(1.dp, c.속선, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(이름, style = 글꼴.보통(크기.아주작게), color = c.글, maxLines = 1)
        Box(Modifier.width(3.dp))
        when {
            값 > 0.05 -> Text("▲ ${kg글(값)}kg", style = 글꼴.보통(크기.아주작게, FontWeight.Bold), color = c.오름, maxLines = 1)
            값 < -0.05 -> Text("▼ ${kg글(값)}kg", style = 글꼴.보통(크기.아주작게, FontWeight.Bold), color = c.내림, maxLines = 1)
            else -> Text("유지", style = 글꼴.보통(크기.아주작게), color = c.흐림, maxLines = 1)
        }
    }
}

/**
 * 휴식 칸 (09-27) — 중심색이 꽉 찬 채로 시작해 남은 시간만큼 줄어든다 (단계별 색 없음).
 *  · 채워진 곳의 글자는 흰색, 빈 곳은 중심색. 경계에서 정확히 갈린다
 *  · 09-29 메모: **그라데이션을 뺐다.** 경계가 딱 떨어지는 쪽이 남은 시간을 읽기 쉽다
 */
@Composable
fun 휴식칸(남은비율: Float, 글자: String, modifier: Modifier = Modifier) {
    val c = Local색.current
    val 비율 by animateFloatAsState(남은비율.coerceIn(0f, 1f), tween(260, easing = LinearEasing), label = "휴식")
    val 강조 = c.강조; val 흰 = c.강조글; val 바탕 = c.면2
    val 모양 = RoundedCornerShape(8.dp)
    Box(
        modifier.clip(모양).background(바탕).clipToBounds()
            .drawBehind {
                val w = size.width * 비율
                if (w > 0f) drawRect(강조, topLeft = Offset.Zero, size = Size(w, size.height))
            },
        contentAlignment = Alignment.Center,
    ) {
        val 모양글 = 글꼴.보통(12.sp, FontWeight.Bold)
        // 빈 곳 — 중심색 글자 (경계 오른쪽만)
        Text(글자, Modifier.fillMaxWidth().drawWithContent { clipRect(left = size.width * 비율) { this@drawWithContent.drawContent() } },
            style = 모양글.copy(shadow = Shadow(바탕, blurRadius = 2f)), color = 강조, maxLines = 1, textAlign = TextAlign.Center)
        // 채운 곳 — 흰 글자 (경계 왼쪽만)
        Text(글자, Modifier.fillMaxWidth().drawWithContent { clipRect(right = size.width * 비율) { this@drawWithContent.drawContent() } },
            style = 모양글.copy(shadow = Shadow(강조, blurRadius = 2f)), color = 흰, maxLines = 1, textAlign = TextAlign.Center)
    }
}

/** 속선 테두리의 작은 상자 — 캘린더 판의 1RM · 볼륨 표 */
@Composable
fun 작은표(글자: String, 채움: Boolean = false) {
    val c = Local색.current
    Box(
        Modifier.clip(RoundedCornerShape(4.dp))
            .then(if (채움) Modifier.background(c.강조) else Modifier.border(1.dp, c.속선, RoundedCornerShape(4.dp)))
            .padding(horizontal = 4.dp),
    ) { Text(글자, style = 글꼴.보통(10.sp, FontWeight.Bold), color = if (채움) c.강조글 else c.글, maxLines = 1) }
}



// ═════════════════════ 10-08 홍겸 님: 세트를 더하면 — 새 줄 한 번 점멸 · 오목하게 눌렸다 제자리 ═════════════════════

/** 줄 목록의 바로 앞 개수 — 처음 그려지는 줄의 번호가 이 값 이상이면 '방금 더한 줄'. [열쇠] 가 바뀌면(다른 종목을 볼 때) 그때 개수부터 */
@Composable
fun 앞줄개수(개수: Int, 열쇠: Any? = Unit): Int {
    val 전 = remember(열쇠) { intArrayOf(개수) }
    val v = 전[0]
    androidx.compose.runtime.SideEffect { 전[0] = 개수 }
    return v
}

/**
 * 방금 더한 줄 (세트 줄 · 어디서나 같은 모양) — 처음 그려질 때 [새] 면 한 번만:
 * 줄 전체가 [움직임.새줄눌림] 로 눌렸다가 탄성으로 제자리 + 줄 바탕에 강조색이 한 번 켜졌다 꺼진다. 줄 modifier 맨 앞에 둔다
 */
fun Modifier.새줄효과(새: Boolean, 모양: androidx.compose.ui.graphics.Shape = RoundedCornerShape(com.slayde.hasenheide.ui.theme.모서리.작게)): Modifier =
    composed {
        val 켬 = remember { 새 }
        if (!켬) return@composed Modifier
        val c = com.slayde.hasenheide.ui.theme.Local색.current
        val 빛 = remember { Animatable(0f) }
        val 배 = remember { Animatable(1f) }
        LaunchedEffect(Unit) {
            kotlinx.coroutines.coroutineScope {
                launch {
                    배.animateTo(com.slayde.hasenheide.ui.theme.움직임.새줄눌림, tween(com.slayde.hasenheide.ui.theme.움직임.눌림))
                    배.animateTo(1f, androidx.compose.animation.core.spring(dampingRatio = com.slayde.hasenheide.ui.theme.움직임.톡탄성, stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow))
                }
                빛.animateTo(1f, tween(com.slayde.hasenheide.ui.theme.움직임.새줄빛올림))
                빛.animateTo(0f, tween(com.slayde.hasenheide.ui.theme.움직임.새줄빛내림))
            }
        }
        Modifier
            .graphicsLayer { scaleX = 배.value; scaleY = 배.value }
            .drawWithContent {
                drawContent()
                val a = 빛.value
                if (a > 0f) drawOutline(모양.createOutline(size, layoutDirection, this), c.강조.copy(alpha = com.slayde.hasenheide.ui.theme.움직임.새줄빛 * a))
            }
    }
