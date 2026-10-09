package com.slayde.hasenheide.ui

import android.os.SystemClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.slayde.hasenheide.data.분초
import com.slayde.hasenheide.data.설정값
import com.slayde.hasenheide.data.앱데이터
import com.slayde.hasenheide.data.같은이름번호
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.글꼴
import com.slayde.hasenheide.ui.theme.높이
import com.slayde.hasenheide.ui.theme.모서리
import com.slayde.hasenheide.ui.theme.부품치수
import com.slayde.hasenheide.ui.theme.선굵기
import com.slayde.hasenheide.ui.theme.움직임
import com.slayde.hasenheide.ui.theme.이름맞춤값
import com.slayde.hasenheide.ui.theme.크기
import com.slayde.hasenheide.ui.theme.휴식칸값
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 공용 부품 (10-05 앱 옮기기 1단계) — 시안 v17 ~ v21 의 함수 · CSS 를 그대로 옮겼다.
 * 2단계 도우미들이 각자 화면에 붙인다. 값(색 · 치수 · 시간)은 전부 Theme.kt (부품치수 · 움직임 · 색표).
 * 사전: 문서/앱옮기기_기초.md
 */

// ═════════════════════ 1. 머리 띠 (가운데 글 띠) ═════════════════════

/**
 * 화면 · 시트 맨 위의 띠 — 글 가운데 (시안 `.띠.가운데띠` · 루틴 · 플랜 · 종목 · 소셜 · 설정이 같은 값).
 * 강조 바탕 · 강조글 · 높이 40 이상 · 여백 6/12 · 18 굵게 (11 지침 U4-8).
 * [회색] = 설정 화면 띠 (시안 `.띠.설정띠` — 바탕 = 선 색, 글 = 글 색).
 * [왼쪽] · [오른쪽] 은 띠 양끝에 붙는 단추 자리 (띠칩 · 아이콘버튼). 글은 언제나 띠 한가운데
 * 10-09 홍겸 님: 운동 보고서처럼 띠도 화면 양끝까지 닿지 않는다 — 좌우 12(아래 상자들과 같은 선) · 위 [위여백] · 모서리 8 (U3-2)
 */
@Composable
fun 머리띠(
    제목: String,
    modifier: Modifier = Modifier,
    회색: Boolean = false,
    위여백: Dp = 간격.보통,
    왼쪽: (@Composable RowScope.() -> Unit)? = null,
    오른쪽: (@Composable RowScope.() -> Unit)? = null,
) {
    val c = Local색.current
    val 글색 = if (회색) c.글 else c.강조글
    Box(
        modifier
            .padding(start = 간격.보통, end = 간격.보통, top = 위여백)
            .fillMaxWidth()
            .heightIn(min = 높이.보통)
            .clip(RoundedCornerShape(모서리.작게))
            .background(if (회색) c.선 else c.강조)
            .padding(horizontal = 간격.보통, vertical = 부품치수.띠세로여백),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            제목, Modifier.fillMaxWidth(), style = 글꼴.보통(크기.크게, FontWeight.Bold), color = 글색,
            maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
        )
        if (왼쪽 != null) Row(Modifier.align(Alignment.CenterStart), verticalAlignment = Alignment.CenterVertically, content = 왼쪽)
        if (오른쪽 != null) Row(Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically, content = 오른쪽)
    }
}

// ═════════════════════ 2. 알림 띠 · 토스트 ═════════════════════

/**
 * 알림 상태 하나 — 앱 어디서나 `상태.알림.토스트("…")` · `상태.알림.되돌림(…)` 로 부른다 (앱상태.알림).
 * 그리는 곳은 App.kt 맨 위 한 곳([알림자리]) — 화면이 다시 그려지거나 바뀌어도 알림은 사라지지 않는다
 * (시안의 알려진 버그 '행동 안에서 토스트를 띄운 뒤 다시 그리면 사라짐' 이 없다).
 *
 *  · 토스트 — 글만. 새 토스트가 오면 앞 것은 바로 사라진다 (시안 `토스트`) · 1.5초 (2초 × 0.75)
 *  · 되돌림 띠 — [되돌리기] 가 붙는다. **같은 [묶음] 이면 한 띠로 합친다** — 개수가 늘고, 누르면 전부 되돌린다
 *    (새것부터 차례로) · 시간은 다시 센다 · 4.5초 (6초 × 0.75)
 *  · 띠 — 다른 단추 하나 (업적 [보기] 등) · 3.75초 (5초 × 0.75)
 *  · 자리 — 나타나기 2초 안에 누른 단추가 있으면 **그 단추 위** (띠 아랫변 = 단추 윗변 − 8). 위로 넘치면 단추 아래.
 *    한 번 정한 자리는 사라질 때까지 그대로. 누른 단추가 없으면 토스트 = 위 50, 띠 = 아래(탭 위)
 *  · 띠 바깥 · 띠 글자는 누름을 막지 않는다 — 띠 안 단추만 눌린다 (아래 화면 단추를 그대로 누를 수 있다)
 *  · 나타날 때 · 사라질 때 0.2초 흐려짐
 */
@Stable
class 알림판 {
    enum class 꼴 { 토스트, 띠 }

    class 알림 internal constructor(
        val id: Long,
        val 꼴: 알림판.꼴,
        val 글: String,
        val 단추: String?,
        internal val 행동: (() -> Unit)?,
        val 묶음: String?,
        val 개수: Int,
        val 시간: Int,
        /** 누른 자리(App 맨 바깥 상자 기준 px) — null 이면 기본 자리 */
        internal val 누른y: Float?,
    )

    var 목록 by mutableStateOf<List<알림>>(emptyList())
        private set
    private var 번호 = 0L
    private var 누른y = 0f
    private var 누른때 = Long.MIN_VALUE / 2

    /** 화면 어디든 손가락이 닿으면 App 이 부른다 ([누름기억]) */
    fun 누름(y: Float) { 누른y = y; 누른때 = SystemClock.uptimeMillis() }

    /** 10-08 홍겸 님: 알림이 뜬 뒤 화면을 두 번 누르면(손을 뗄 때 셈) 토스트 · 띠를 바로 치운다 */
    private var 뗀수 = 0
    fun 뗌() {
        if (목록.isEmpty()) { 뗀수 = 0; return }
        if (++뗀수 >= 2) { 뗀수 = 0; 모두치움() }
    }
    private fun 자리(): Float? = if (SystemClock.uptimeMillis() - 누른때 < 움직임.누름기억) 누른y else null

    /** 글만 잠깐 — 앞 토스트는 바로 치운다 */
    fun 토스트(글: String, 시간: Int = 움직임.토스트) {
        뗀수 = 0
        목록 = 목록.filter { it.꼴 != 꼴.토스트 } + 알림(++번호, 꼴.토스트, 글, null, null, null, 1, 시간, 자리())
    }

    /**
     * 지운 뒤 [되돌리기] 띠 — 같은 [묶음] 의 띠가 떠 있으면 하나로 합친다.
     * [글] 은 합친 개수를 받아 띠 글을 만든다 (예: `{ n -> if (n > 1) "운동 기록 ${n}개를 지웠습니다" else "운동 기록을 지웠습니다" }`).
     * [되돌리기] 는 이번 것 하나만 되돌린다 — 합친 띠를 누르면 새것부터 차례로 전부 부른다
     */
    fun 되돌림(묶음: String, 글: (개수: Int) -> String, 되돌리기: () -> Unit) {
        val 옛 = 목록.firstOrNull { it.꼴 == 꼴.띠 && it.묶음 == 묶음 }
        val 개수 = (옛?.개수 ?: 0) + 1
        val 옛행동 = 옛?.행동
        val 합친: () -> Unit = if (옛행동 == null) 되돌리기 else ({ 되돌리기(); 옛행동() })
        // 10-08 홍겸 님: 되돌리기 띠는 누른 자리를 따라가지 않고 늘 같은 자리(탭줄 바로 위)에 — 화면마다 위 · 아래로 바뀌어 찾기 어려웠다
        뗀수 = 0
        목록 = 목록.filter { it !== 옛 } + 알림(++번호, 꼴.띠, 글(개수), "되돌리기", 합친, 묶음, 개수, 움직임.되돌림띠, null)
    }

    /** 단추 하나 붙은 띠 (업적 [보기] 등) — 되돌리기가 아닌 것. 같은 [묶음] 이면 바꿔 끼운다 */
    fun 띠(글: String, 단추: String, 행동: () -> Unit, 묶음: String? = null, 시간: Int = 움직임.업적띠) {
        val 남길 = if (묶음 == null) 목록 else 목록.filter { !(it.꼴 == 꼴.띠 && it.묶음 == 묶음) }
        뗀수 = 0
        목록 = 남길 + 알림(++번호, 꼴.띠, 글, 단추, 행동, 묶음, 1, 시간, null)
    }

    fun 치움(id: Long) { 목록 = 목록.filter { it.id != id } }
    fun 묶음치움(묶음: String) { 목록 = 목록.filter { it.묶음 != 묶음 } }
    fun 모두치움() { 목록 = emptyList() }
    internal fun 누름처리(a: 알림) { 치움(a.id); a.행동?.invoke() }
}

/** 손가락이 닿은 자리를 [알림판] 에 알린다 — 누름을 가로채지 않는다 (App 맨 바깥 상자에 붙인다) */
fun Modifier.누름기억(판: 알림판): Modifier = this.pointerInput(판) {
    awaitPointerEventScope {
        while (true) {
            val e = awaitPointerEvent(PointerEventPass.Initial)
            e.changes.firstOrNull { it.changedToDownIgnoreConsumed() }?.let { 판.누름(it.position.y) }
            if (e.changes.any { it.changedToUpIgnoreConsumed() }) 판.뗌()
        }
    }
}

/**
 * 알림을 그리는 곳 — App.kt 맨 바깥 상자(누름기억을 붙인 상자)를 꽉 채워 맨 위에 둔다.
 * [아래여백] = 누른 단추가 없을 때 띠가 뜨는 자리(화면 아래에서)
 */
@Composable
fun 알림자리(판: 알림판, 아래여백: Dp, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        판.목록.forEach { a -> key(a.id) { 알림한개(판, a, 아래여백) } }
    }
}

@Composable
private fun 알림한개(판: 알림판, a: 알림판.알림, 아래여백: Dp) {
    val c = Local색.current
    val 보임 = remember { MutableTransitionState(false).apply { targetState = true } }
    LaunchedEffect(a.id) { delay(a.시간.toLong()); 보임.targetState = false }
    LaunchedEffect(보임.isIdle, 보임.currentState) { if (보임.isIdle && !보임.currentState && !보임.targetState) 판.치움(a.id) }
    val 위안 = WindowInsets.statusBars
    val 아래안 = WindowInsets.navigationBars
    Layout(
        content = {
            AnimatedVisibility(visibleState = 보임, enter = fadeIn(tween(움직임.띠흐림)), exit = fadeOut(tween(움직임.띠흐림)), label = "알림") {
                if (a.꼴 == 알림판.꼴.토스트) {
                    Box(
                        Modifier.clip(RoundedCornerShape(모서리.작게)).background(c.흐림)
                            .padding(horizontal = 간격.보통, vertical = 부품치수.띠세로여백),
                    ) { Text(a.글, style = 글꼴.보통(크기.버튼), color = c.바탕, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                } else {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(모서리.작게)).background(c.흐림)
                            .padding(horizontal = 간격.보통, vertical = 간격.좁게),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(간격.보통),
                    ) {
                        Text(a.글, Modifier.weight(1f), style = 글꼴.보통(크기.버튼), color = c.바탕, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (a.단추 != null) Text(
                            a.단추, Modifier.눌림 { 판.누름처리(a) },
                            style = 글꼴.보통(크기.버튼, FontWeight.Bold).copy(textDecoration = TextDecoration.Underline),
                            color = c.바탕, maxLines = 1,
                        )
                    }
                }
            }
        },
    ) { ms, cons ->
        if (ms.isEmpty()) return@Layout layout(0, 0) {}
        val 옆 = 간격.보통.roundToPx()
        val 끝 = 간격.아주좁게.roundToPx()
        val 넓이 = (cons.maxWidth - 옆 * 2).coerceAtLeast(0)
        val p = ms.first().measure(Constraints(minWidth = if (a.꼴 == 알림판.꼴.띠) 넓이 else 0, maxWidth = 넓이))
        val 위px = 위안.getTop(this)
        val 아래px = 아래안.getBottom(this)
        val 반 = 부품치수.누름반.toPx(); val 틈 = 부품치수.띠틈.toPx()
        val 토스트위px = 부품치수.토스트위.roundToPx(); val 아래여백px = 아래여백.roundToPx()
        layout(cons.maxWidth, cons.maxHeight) {
            val h = p.height
            val y = a.누른y?.let { 누른 ->
                var t = 누른 - 반 - 틈 - h
                if (t < 끝) t = 누른 + 반 + 틈
                t.roundToInt()
            } ?: if (a.꼴 == 알림판.꼴.토스트) 위px + 토스트위px else cons.maxHeight - h - 아래px - 아래여백px
            p.place((cons.maxWidth - p.width) / 2, y.coerceIn(끝, (cons.maxHeight - h - 끝).coerceAtLeast(끝)))
        }
    }
}

// ═════════════════════ 3. 당겨서 새로고침 ═════════════════════

/**
 * 당겨서 새로고침 (시안 v18 E ⑦ · v19 A ②) — **띠 아래만**: 머리 띠는 이 바깥에 두고, 그 아래 넘기는 칸을 [content] 로 넘긴다.
 * [content] 안에 위아래로 넘기는 것(verticalScroll · LazyColumn)이 있어야 한다 — 비어 있어도 된다.
 *  · 맨 위에서 아래로 끌면 끈 만큼 × 0.5 (최대 56) 속이 내려오고, 위에 빈 줄에 도는 고리가 보인다
 *  · 40 넘게 끌고 놓으면 줄 40 에서 고리가 0.5초 돌고 → [새로고침] → 줄이 접힌다(0.2초). 덜 끌면 그냥 접힌다
 */
@Composable
fun 당겨새로고침(
    새로고침: () -> Unit,
    modifier: Modifier = Modifier,
    켬: Boolean = true,
    content: @Composable () -> Unit,
) {
    val c = Local색.current
    val 밀도 = LocalDensity.current
    val 줄px = with(밀도) { 부품치수.당김줄.toPx() }
    val 최대px = with(밀도) { 부품치수.당김최대.toPx() }
    var 당김 by remember { mutableFloatStateOf(0f) }
    var 도는중 by remember { mutableStateOf(false) }
    val 범위 = rememberCoroutineScope()
    val 새로고침최신 by rememberUpdatedState(새로고침)
    val 켬최신 by rememberUpdatedState(켬)
    val 연결 = remember {
        object : NestedScrollConnection {
            fun 접기() { 범위.launch { animate(당김, 0f, animationSpec = tween(움직임.당김접힘)) { v, _ -> 당김 = v } } }
            fun 놓음() {
                if (당김 >= 줄px) {
                    도는중 = true
                    범위.launch {
                        animate(당김, 줄px, animationSpec = tween(움직임.당김접힘)) { v, _ -> 당김 = v }
                        delay(움직임.당김돎.toLong())
                        새로고침최신()
                        도는중 = false
                        animate(당김, 0f, animationSpec = tween(움직임.당김접힘)) { v, _ -> 당김 = v }
                    }
                } else 접기()
            }
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (도는중 || 당김 <= 0f || available.y >= 0f || source != NestedScrollSource.UserInput) return Offset.Zero
                val 전 = 당김
                당김 = (당김 + available.y * 부품치수.당김비율).coerceAtLeast(0f)
                return Offset(0f, (당김 - 전) / 부품치수.당김비율)
            }
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (!켬최신 || 도는중 || available.y <= 0f || source != NestedScrollSource.UserInput) return Offset.Zero
                당김 = (당김 + available.y * 부품치수.당김비율).coerceAtMost(최대px)
                return Offset(0f, available.y)
            }
            override suspend fun onPreFling(available: Velocity): Velocity {
                if (도는중) return available
                if (당김 <= 0f) return Velocity.Zero
                놓음()
                return available
            }
        }
    }
    Box(modifier.clipToBounds().nestedScroll(연결)) {
        Box(Modifier.fillMaxSize().graphicsLayer { translationY = 당김 }) { content() }
        // 위의 빈 줄 — 높이는 그리기 직전(배치)에만 읽는다
        Box(
            Modifier.fillMaxWidth().clipToBounds().layout { m, cons ->
                val h = 당김.roundToInt().coerceAtLeast(0)
                val p = m.measure(cons.copy(minHeight = h, maxHeight = h))
                layout(p.width, h) { p.place(0, 0) }
            },
            contentAlignment = Alignment.Center,
        ) {
            val 돎 = if (도는중) {
                val t = rememberInfiniteTransition(label = "당김돎")
                t.animateFloat(0f, 360f, infiniteRepeatable(tween(움직임.당김돎, easing = LinearEasing), RepeatMode.Restart), label = "당김각")
            } else null
            Icon(
                아이콘.돌림, null,
                Modifier.size(부품치수.탭그림).graphicsLayer {
                    alpha = min(1f, 당김 / 줄px)
                    rotationZ = 돎?.value ?: (당김 / density * 6f)
                },
                tint = c.강조,
            )
        }
    }
}

// ═════════════════════ 4. 한 줄 칩줄 + ‹ › ═════════════════════

/**
 * 옆으로 넘기는 한 줄의 양끝 ‹ › 동그라미 (시안 `.칩화살` · v19 C ④ · v21 ③ 종목 칸 줄도 같은 부품).
 * 넘칠 때만, 가려진 쪽에만 보인다. 누르면 줄 폭의 70% 만큼 부드럽게 넘긴다.
 * [content] 는 [넘김] 으로 horizontalScroll 하는 줄 하나. [오른쪽비움] = 줄 오른쪽에 붙박인 것(운동 칸 줄의 [＋]) 자리
 */
@Composable
fun 화살줄(넘김: ScrollState, modifier: Modifier = Modifier, 오른쪽비움: Dp = 0.dp, content: @Composable () -> Unit) {
    val 범위 = rememberCoroutineScope()
    var 폭 by remember { mutableIntStateOf(0) }
    Box(modifier.onSizeChanged { 폭 = it.width }) {
        content()
        val 넘침 = 넘김.maxValue in 2 until Int.MAX_VALUE
        if (넘침 && 넘김.value > 1) 화살단추(아이콘.칩왼쪽, "왼쪽 보기", Modifier.align(Alignment.CenterStart)) {
            범위.launch { 넘김.animateScrollBy(-폭 * 부품치수.칩화살넘김) }
        }
        if (넘침 && 넘김.value < 넘김.maxValue - 1) 화살단추(아이콘.칩오른쪽, "오른쪽 보기", Modifier.align(Alignment.CenterEnd).padding(end = 오른쪽비움)) {
            범위.launch { 넘김.animateScrollBy(폭 * 부품치수.칩화살넘김) }
        }
    }
}

@Composable
private fun 화살단추(그림: ImageVector, 설명: String, modifier: Modifier, onClick: () -> Unit) {
    val c = Local색.current
    Box(
        modifier.size(부품치수.칩화살).clip(CircleShape).background(c.면).border(선굵기.보통, c.선, CircleShape).눌림(onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(그림, 설명, Modifier.size(부품치수.칩화살그림), tint = c.글) }
}

/** 하나 고르는 칩 한 줄 + 넘칠 때 양끝 ‹ › (칩줄 + 화살줄). 종목 넣기 시트 칸 칩 (시안 `.넣기칩`) */
@Composable
fun 화살칩줄(목록: List<String>, 선택: String?, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    val s = rememberScrollState()
    화살줄(s, modifier) { 칩줄(목록, 선택, onSelect, Modifier, 밖넘김 = s) }
}

// ═════════════════════ 5. − 값 ＋ 칸 ═════════════════════

/**
 * − 값 ＋ 한 칸 (시안 `.값칸`) — 테두리 속선 · 모서리 8 · 높이 40 · 양끝 누르는 칸 28 (강조) · 값 13 굵게 고정폭 숫자.
 * 끝에 닿은 쪽은 흐리게 하고 눌리지 않는다
 */
@Composable
fun 값칸(
    값글: String,
    빼기: () -> Unit,
    더하기: () -> Unit,
    modifier: Modifier = Modifier,
    이름: String = "",
    뺄수있음: Boolean = true,
    더할수있음: Boolean = true,
    칸높이: Dp = 높이.보통,
) {
    val c = Local색.current
    val 모양 = RoundedCornerShape(모서리.작게)
    Row(
        modifier.height(칸높이).clip(모양).border(선굵기.보통, c.속선, 모양),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        값칸단추(아이콘.빼기, "$이름 빼기", 뺄수있음, 빼기)
        Text(
            값글, Modifier.weight(1f), style = 글꼴.보통(크기.버튼, FontWeight.Bold).copy(fontFeatureSettings = "tnum"),
            color = c.글, maxLines = 1, textAlign = TextAlign.Center,
        )
        값칸단추(아이콘.더하기, "$이름 더하기", 더할수있음, 더하기)
    }
}

@Composable
private fun 값칸단추(그림: ImageVector, 설명: String, 켬: Boolean, onClick: () -> Unit) {
    val c = Local색.current
    Box(
        Modifier.width(부품치수.값칸단추).fillMaxHeight().then(if (켬) Modifier.눌림(onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) { Icon(그림, 설명, Modifier.size(그림크기), tint = if (켬) c.강조 else c.옅음) }
}

private val 그림크기 = 18.dp   // 11 지침 U3-6 기본 아이콘 (아이콘버튼 · −＋ 와 같다)

/** 휴식 − ＋ 칸 — 15초씩 · 0:15 ~ 5:00 (시안 10-03 ypk0 `휴식바꿈`). [바꿈] 은 새 초 */
@Composable
fun 휴식값칸(초: Int, 바꿈: (Int) -> Unit, modifier: Modifier = Modifier, 칸높이: Dp = 높이.보통) {
    값칸(
        분초(초), { 바꿈(휴식한칸(초, -1)) }, { 바꿈(휴식한칸(초, 1)) }, modifier, 이름 = "휴식",
        뺄수있음 = 초 > 휴식칸값.최소, 더할수있음 = 초 < 휴식칸값.최대, 칸높이 = 칸높이,
    )
}

/** 휴식 한 칸 옮기기 — 15초 눈금에 맞추고 범위 안으로. 범위 밖 값에서 반대로 튀지 않는다 (시안 `휴식바꿈`) */
fun 휴식한칸(전: Int, 방향: Int): Int {
    val 폭 = 휴식칸값.폭
    var 새 = (Math.round((전 + 방향 * 폭).toDouble() / 폭) * 폭).toInt().coerceIn(휴식칸값.최소, 휴식칸값.최대)
    if ((방향 < 0 && 새 > 전) || (방향 > 0 && 새 < 전)) 새 = 전
    return 새
}

// ═════════════════════ 6. 딱지 — [플랜] · 같은 이름 번호 ═════════════════════

/** [플랜] 딱지 (시안 jmg1 `.플랜표`) — 이름 오른쪽 위에 겹친다. 11 굵게 · 강조 바탕 · 강조글 */
@Composable
fun 플랜딱지(modifier: Modifier = Modifier) = 딱지글("플랜", false, modifier)

/** 같은 이름 번호 딱지 (시안 v19 D ② `.번호표`) — [n] 이 0 이면 그리지 않는다. 번호는 데이터 `같은이름번호` */
@Composable
fun 번호딱지(n: Int, modifier: Modifier = Modifier) { if (n > 0) 딱지글("$n", true, modifier) }

@Composable
private fun 딱지글(글: String, 번호: Boolean, modifier: Modifier) {
    val c = Local색.current
    Box(
        modifier
            .offset(y = if (번호) 부품치수.번호딱지위 else 부품치수.딱지위)
            .clip(RoundedCornerShape(모서리.작게))
            .background(c.강조)
            .then(if (번호) Modifier.widthIn(min = 부품치수.번호딱지폭) else Modifier)
            .padding(horizontal = if (번호) 부품치수.번호딱지옆 else 부품치수.딱지옆),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            글, style = 글꼴.보통(크기.작게, FontWeight.Bold).copy(lineHeight = 부품치수.딱지글높이, fontFeatureSettings = "tnum"),
            color = c.강조글, maxLines = 1, softWrap = false,
        )
    }
}

/** 이름 + [번호][플랜] 딱지 — 이름은 길면 … (U2-7 이름만). 딱지는 이름 오른쪽 위에 5 겹친다 (시안 `번호이름` · `이름플랜`) */
@Composable
fun 이름딱지(
    이름: String,
    modifier: Modifier = Modifier,
    번호: Int = 0,
    플랜: Boolean = false,
    크기값: TextUnit = 크기.본문,
    굵기: FontWeight = FontWeight.Bold,
    색: Color = Local색.current.글,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(이름, Modifier.weight(1f, fill = false), style = 글꼴.보통(크기값, 굵기), color = 색, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (번호 > 0 || 플랜) Row(Modifier.offset(x = 부품치수.딱지겹침), horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
            번호딱지(번호)
            if (플랜) 플랜딱지()
        }
    }
}

/** 종목 이름 + 같은 이름 번호 (+ 플랜) — 번호는 [앱데이터.같은이름번호] 로 */
@Composable
fun 종목이름딱지(d: 앱데이터, 종id: String?, 이름: String, modifier: Modifier = Modifier, 플랜: Boolean = false, 크기값: TextUnit = 크기.본문) =
    이름딱지(이름, modifier, d.같은이름번호(종id, 이름), 플랜, 크기값)

// ═════════════════════ 7. 가로 줄 꾹 눌러 끌기 ═════════════════════

/**
 * 가로 한 줄 안에서 꾹 눌러 끌어 순서 바꾸기 (시안 `끌` 의 '운칸' · '업적' — 운동 종목 칸 줄 · 프로필 업적 줄).
 * 놓으면 `옮김(원, 대상, 뒤에)` — 데이터 `목옮김(l, 원, 대상, 뒤에)` 과 같은 뜻 (대상 칸의 앞 · 뒤).
 *  · 칸마다 `Modifier.가로끌기(판, 번호)`, 줄에 `Modifier.가로끌기줄(판, 넘김)` (끝에 가면 저절로 넘긴다)
 *  · 칸 자리는 끌기 **전에** 잰 것만 쓴다 (11 지침 U5-6 · 움직이는 중에는 재지 않는다). 넘긴 만큼은 더해서 견준다
 *  · 끄는 칸은 투명도 0.35 · 놓을 자리는 그 칸의 왼쪽 · 오른쪽 3dp 강조 선 (U5-5)
 */
@Stable
class 가로끌기판 internal constructor(private val 옮김: (원: Int, 대상: Int, 뒤에: Boolean) -> Unit) {
    var 원 by mutableStateOf<Int?>(null)
        private set
    /** (대상 번호, 뒤에) */
    var 놓을곳 by mutableStateOf<Pair<Int, Boolean>?>(null)
        private set
    internal val 자리 = mutableMapOf<Int, Rect>()
    internal var 줄자리: Rect? = null
    internal var 넘김: ScrollState? = null
    internal var x = 0f
    private var 시작넘김 = 0

    internal fun 시작(i: Int, 손x: Float) { 원 = i; x = 손x; 시작넘김 = 넘김?.value ?: 0; 찾기() }
    internal fun 끌림(dx: Float) { x += dx; 찾기() }
    internal fun 찾기() {
        val 내용x = x + ((넘김?.value ?: 0) - 시작넘김)
        val 맞음 = 자리.entries.firstOrNull { (_, r) -> 내용x >= r.left && 내용x < r.right }
        놓을곳 = 맞음?.let { (j, r) -> j to (내용x > r.center.x) }
    }
    internal fun 끝(놓음: Boolean) {
        val o = 원; val t = 놓을곳
        원 = null; 놓을곳 = null
        if (놓음 && o != null && t != null) 옮김(o, t.first, t.second)
    }
}

@Composable
fun remember가로끌기판(옮김: (원: Int, 대상: Int, 뒤에: Boolean) -> Unit): 가로끌기판 {
    val 최신 by rememberUpdatedState(옮김)
    return remember { 가로끌기판 { a, b, c -> 최신(a, b, c) } }
}

/** 줄 하나 — 끄는 동안 손이 줄 왼끝 32 · 오른끝 [오른끝] 안에 들어가면 그쪽으로 넘긴다 (시안: 운동 칸 줄은 [＋] 때문에 76) */
fun Modifier.가로끌기줄(판: 가로끌기판, 넘김: ScrollState?, 오른끝: Dp = 부품치수.끌기끝): Modifier = composed {
    SideEffect { 판.넘김 = 넘김 }
    val 밀도 = LocalDensity.current
    val 왼px = with(밀도) { 부품치수.끌기끝.toPx() }
    val 오px = with(밀도) { 오른끝.toPx() }
    val 넘김px = with(밀도) { 부품치수.끌기넘김.toPx() }
    val 끄는중 = 판.원 != null
    LaunchedEffect(끄는중, 넘김) {
        if (!끄는중 || 넘김 == null) return@LaunchedEffect
        while (판.원 != null) {
            withFrameMillis { }
            val r = 판.줄자리 ?: continue
            val dx = when { 판.x < r.left + 왼px -> -넘김px; 판.x > r.right - 오px -> 넘김px; else -> 0f }
            if (dx != 0f) { 넘김.scrollBy(dx); 판.찾기() }
        }
    }
    Modifier.onGloballyPositioned { if (판.원 == null) 판.줄자리 = it.boundsInRoot() }
}

/** 칸 하나 — [번호] = 줄 안 차례 */
fun Modifier.가로끌기(판: 가로끌기판, 번호: Int, 켬: Boolean = true): Modifier = composed {
    if (!켬) return@composed Modifier
    val c = Local색.current
    val 진동 = LocalHapticFeedback.current
    DisposableEffect(판, 번호) { onDispose { 판.자리.remove(번호) } }
    Modifier
        .onGloballyPositioned { if (판.원 == null) 판.자리[번호] = it.boundsInRoot() }
        .graphicsLayer { alpha = if (판.원 == 번호) 움직임.끌림투명 else 1f }
        .drawWithContent {
            drawContent()
            val t = 판.놓을곳
            if (t != null && t.first == 번호 && 판.원 != 번호) {
                val w = 부품치수.놓을선.toPx()
                drawRect(c.강조, topLeft = Offset(if (t.second) size.width - w else 0f, 0f), size = Size(w, size.height))
            }
        }
        .pointerInput(판, 번호) {
            detectDragGesturesAfterLongPress(
                onDragStart = { p ->
                    진동.performHapticFeedback(HapticFeedbackType.LongPress)
                    판.시작(번호, (판.자리[번호]?.left ?: 0f) + p.x)
                },
                onDrag = { ch, d -> ch.consume(); 판.끌림(d.x) },
                onDragEnd = { 판.끝(true) },
                onDragCancel = { 판.끝(false) },
            )
        }
}

// ═════════════════════ 8. 이름 맞춤 글 ═════════════════════

/** 이름이 몇 줄인가 — 12자(공백 포함) 넘으면 2 (시안 v20 ① `이름줄수`) */
fun 이름줄수(이름: String): Int = if (이름.trim().let { it.codePointCount(0, it.length) } > 이름맞춤값.한줄최대) 2 else 1

/**
 * 종목 이름 — 잘리지 않게 (시안 v20 ① `맞춤이름` · `이름맞춤`). … 로 자르지 않는다.
 *  · 12자 이하는 한 줄, 넘으면 두 줄
 *  · 칸에 안 들어가면 글자 15 → 13 → 11 ([바탕크기] 보다 큰 단계는 건너뜀), 그래도 안 되면 자간을 −0.2em 까지 좁힌다
 *  · 한 줄 이름이 그래도 안 들어가면 두 줄로 같은 순서를 다시 한다
 *  · 칸 폭은 그릴 때 잰다 (글자를 한 번에 재서 고른다 — 깜빡이며 줄지 않는다)
 */
@Composable
fun 이름맞춤(
    이름: String,
    modifier: Modifier = Modifier,
    바탕크기: TextUnit = 크기.본문,
    색: Color = Local색.current.글,
    굵기: FontWeight = FontWeight.Bold,
    가운데: Boolean = false,
) {
    val 측정 = rememberTextMeasurer()
    BoxWithConstraints(modifier) {
        val w = constraints.maxWidth
        val (꼴, 줄) = remember(이름, w, 바탕크기, 굵기) { 이름꼴고르기(측정, 이름, w, 바탕크기, 굵기) }
        Text(
            이름, if (가운데) Modifier.fillMaxWidth() else Modifier, style = 꼴, color = 색, maxLines = 줄, softWrap = 줄 > 1, overflow = TextOverflow.Clip,
            textAlign = if (가운데) TextAlign.Center else null,
        )
    }
}

private fun 이름꼴고르기(측정: TextMeasurer, 이름: String, w: Int, 바탕: TextUnit, 굵기: FontWeight): Pair<TextStyle, Int> {
    val 줄0 = 이름줄수(이름)
    if (w == Constraints.Infinity || w <= 0) return 글꼴.보통(바탕, 굵기) to 줄0
    val 단계 = 이름맞춤값.글단계.filter { it <= 바탕.value + 0.1f }.ifEmpty { listOf(바탕.value) }
    val 끝꼴 = 글꼴.보통(단계.last().sp, 굵기).copy(letterSpacing = 이름맞춤값.자간끝.em)
    fun 넘침(st: TextStyle, 줄: Int): Boolean =
        측정.measure(이름, st, overflow = TextOverflow.Clip, softWrap = 줄 > 1, maxLines = 줄, constraints = Constraints(maxWidth = w)).hasVisualOverflow
    fun 해봄(줄: Int): TextStyle? {
        for (fs in 단계) { val st = 글꼴.보통(fs.sp, 굵기); if (!넘침(st, 줄)) return st }
        var n = 2
        while (n <= (-이름맞춤값.자간끝 * 100).roundToInt()) {
            val st = 글꼴.보통(단계.last().sp, 굵기).copy(letterSpacing = (-n * 이름맞춤값.자간폭).em)
            if (!넘침(st, 줄)) return st
            n++
        }
        return null
    }
    해봄(줄0)?.let { return it to 줄0 }
    if (줄0 > 1) return 끝꼴 to 줄0
    해봄(2)?.let { return it to 2 }
    return 끝꼴 to 2
}

// ═════════════════════ 9. 점멸 — 지금 줄 바탕 · 노란 점 · 끝난 줄 ═════════════════════

private val 점멸곡선 = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)   // CSS ease-in-out

/**
 * 점멸 박자 — 0(밝은 쪽 · 시작과 끝) → 1(옅은 쪽 · 가운데) → 0, 1초 (시안 v21 ⑦).
 * 시계로 재므로 앱의 모든 점멸이 같은 박자다 (다시 그려도 끊기지 않는다 — 시안 `--점멸늦춤`)
 */
@Composable
fun 점멸값(): State<Float> = produceState(0f) {
    while (true) {
        withFrameMillis { }
        val t = (System.currentTimeMillis() % 움직임.점멸) / 움직임.점멸.toFloat()
        value = if (t < 0.5f) 점멸곡선.transform(t * 2f) else 1f - 점멸곡선.transform((t - 0.5f) * 2f)
    }
}

/** 지금 세트 줄 · 지금 종목 칸 — 바탕만 강조옅음 ↔ 지금깜빡 (글자 · 테는 그대로). [켬] 이 아니면 아무것도 안 한다 */
fun Modifier.점멸바탕(켬: Boolean, 모양: Shape = RectangleShape): Modifier = composed {
    if (!켬) return@composed Modifier
    val c = Local색.current
    val p = 점멸값()
    Modifier.drawBehind { drawOutline(모양.createOutline(size, layoutDirection, this), lerp(c.강조옅음, c.지금깜빡, p.value)) }
}

/** 노란 점 — 누를 수 있다는 표시 (시안 `.노란점` 지름 6 · 둘레 1 노랑테). 깜빡일 때 투명도 1 ↔ 0.15 */
@Composable
fun 노란점(modifier: Modifier = Modifier, 깜빡: Boolean = true) {
    val c = Local색.current
    val p = 점멸값()
    Box(
        modifier
            .size(부품치수.노란점)
            .graphicsLayer { alpha = if (깜빡) 1f - (1f - 움직임.점멸옅음) * p.value else 1f }
            .drawBehind {
                val r = size.minDimension / 2
                drawCircle(c.노랑테, r + 부품치수.노란점테.toPx())
                drawCircle(c.노랑, r)
            },
    )
}

/** 끝난 세트 줄 — 줄 높이 가운데 50% 만 완료바탕으로 칠한다 (시안 v18 A ①, 위아래 25% 비움 · 모서리 8) */
fun Modifier.완료바탕(켬: Boolean): Modifier = composed {
    if (!켬) return@composed Modifier
    val c = Local색.current
    Modifier.drawBehind {
        val r = 모서리.작게.toPx()
        drawRoundRect(c.완료바탕, topLeft = Offset(0f, size.height * 0.25f), size = Size(size.width, size.height * 0.5f), cornerRadius = CornerRadius(r, r))
    }
}

// ═════════════════════ 10. 프로필 동그라미 ═════════════════════

/**
 * 프로필 동그라미 속 — 사진 / 닉네임 첫 글자 / 사람 그림 (시안 `프로필그림` · 보고서 · 프로필 탭 · 탭줄이 같이 쓴다).
 * 사진 파일은 [사진함] 폴더(filesDir/photos)에 둔다 — `설정.프로필사진` = 그 파일 이름. 파일이 없으면 다음 것으로.
 * 바탕 강조옅음 · 글 강조 · 테 1 선. [고리] = 켜졌을 때 둘레 2 강조 (탭줄 프로필 칸)
 */
@Composable
fun 프로필동그라미(설정: 설정값, 지름: Dp, modifier: Modifier = Modifier, 고리: Boolean = false, 그림: Dp = 그림크기) {
    val c = Local색.current
    val ctx = LocalContext.current
    val 사진 = 설정.프로필사진?.takeIf { it.isNotBlank() }
    val 있음 = remember(사진) { 사진 != null && 사진함.파일(ctx, 사진).exists() }
    val 첫 = 설정.닉네임.trim().let { if (it.isEmpty()) null else String(Character.toChars(it.codePointAt(0))) }
    Box(
        modifier
            .size(지름)
            .then(if (고리) Modifier.drawBehind { drawCircle(c.강조, size.minDimension / 2 + 부품치수.탭사진고리.toPx()) } else Modifier)
            .clip(CircleShape)
            .background(c.강조옅음)
            .border(선굵기.보통, c.선, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        when {
            있음 && 사진 != null -> 사진그림(사진, 지름, Modifier.fillMaxSize())
            첫 != null -> Text(첫, style = 글꼴.보통(크기.버튼, FontWeight.Bold), color = c.강조, maxLines = 1)
            else -> Icon(아이콘.프로필사람, null, Modifier.size(그림), tint = c.강조)
        }
    }
}
