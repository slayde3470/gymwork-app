package com.slayde.hasenheide.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.slayde.hasenheide.data.종목
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.부품치수
import com.slayde.hasenheide.ui.theme.움직임
import com.slayde.hasenheide.ui.theme.크기
import kotlin.math.roundToInt

/**
 * 2열 격자에서 꾹 눌러 끌어 순서 바꾸기 — 10-07 홍겸 님: 루틴에 넣기 시트 · 종목 탭 (동작방식 D3-9 '꾹 눌러 끌기는 어디서나 같은 뜻').
 * [가로끌기판](Parts.kt)의 격자판. 칸 열쇠는 글자(종목 id 등).
 *  · 칸마다 `Modifier.격자끌기(판, 열쇠, 이름, 무리)` — 같은 [무리] 끼리만 놓는다(종목 탭 = 같은 카테고리)
 *  · 넘기는 칸에 `Modifier.격자끌기틀(판, 넘김)` (위 · 아래 끝에 가면 저절로 넘긴다), 그 위에 [격자끌기이름표]
 *  · 칸 자리는 끌기 **전에** 잰 것만 쓴다 (U5-6). 넘긴 만큼은 더해서 견준다
 *  · 끄는 칸은 투명도 0.35 · 놓을 자리는 그 칸의 왼쪽(앞) · 오른쪽(뒤) 3dp 강조 선 (U5-5)
 *  · 놓을 곳 없이 거의 안 움직이고 떼면 [제자리] (넣기 시트 = 꾹 = 하나 더 넣기)
 */
@Stable
class 격자끌기판 internal constructor(
    private val 옮김: (원: String, 대상: String, 뒤에: Boolean) -> Unit,
    private val 제자리: (원: String) -> Unit,
) {
    var 원 by mutableStateOf<String?>(null)
        private set
    var 이름 by mutableStateOf("")
        private set
    /** (대상 열쇠, 뒤에) */
    var 놓을곳 by mutableStateOf<Pair<String, Boolean>?>(null)
        private set
    /** 손가락 (화면 기준) */
    var 손 by mutableStateOf(Offset.Zero)
        private set
    internal val 자리 = mutableMapOf<String, Rect>()
    internal val 무리 = mutableMapOf<String, String>()
    internal var 넘김: ScrollState? = null
    internal var 틀: Rect? = null
    internal var 제자리폭 = 24f
    private var 시작넘김 = 0
    private var 굳은: Map<String, Rect> = emptyMap()
    private var 움직인 = 0f

    internal fun 시작(k: String, 이름: String, p: Offset) {
        굳은 = HashMap(자리); this.이름 = 이름; 손 = p; 움직인 = 0f
        시작넘김 = 넘김?.value ?: 0; 원 = k; 찾기()
    }
    internal fun 끌림(d: Offset) { 손 += d; 움직인 += d.getDistance(); 찾기() }
    internal fun 찾기() {
        val o = 원 ?: return
        val dy = ((넘김?.value ?: 0) - 시작넘김).toFloat()
        놓을곳 = 격자대상(굳은, 무리, o, 손.x, 손.y + dy)
    }
    internal fun 끝(놓음: Boolean) {
        val o = 원; val t = 놓을곳; val 조금 = 움직인 < 제자리폭
        원 = null; 놓을곳 = null
        if (!놓음 || o == null) return
        if (t != null) 옮김(o, t.first, t.second) else if (조금) 제자리(o)
    }
}

@Composable
fun remember격자끌기판(옮김: (원: String, 대상: String, 뒤에: Boolean) -> Unit, 제자리: (원: String) -> Unit = {}): 격자끌기판 {
    val 옮김최신 by rememberUpdatedState(옮김)
    val 제자리최신 by rememberUpdatedState(제자리)
    val 판 = remember { 격자끌기판({ a, b, c -> 옮김최신(a, b, c) }, { 제자리최신(it) }) }
    val 밀도 = LocalDensity.current
    SideEffect { 판.제자리폭 = with(밀도) { 부품치수.끌기끝.toPx() } * 0.75f }
    return 판
}

/** 넘기는 칸 — 끄는 동안 손이 위 · 아래 끝 가까이 가면 그쪽으로 넘긴다 */
fun Modifier.격자끌기틀(판: 격자끌기판, 넘김: ScrollState?): Modifier = composed {
    SideEffect { 판.넘김 = 넘김 }
    val 밀도 = LocalDensity.current
    val 끝px = with(밀도) { 부품치수.끌기끝.toPx() }
    val 한번 = with(밀도) { 부품치수.끌기넘김.toPx() }
    val 끄는중 = 판.원 != null
    LaunchedEffect(끄는중, 넘김) {
        if (!끄는중 || 넘김 == null) return@LaunchedEffect
        while (판.원 != null) {
            withFrameMillis { }
            val r = 판.틀 ?: continue
            val dy = when { 판.손.y < r.top + 끝px -> -한번; 판.손.y > r.bottom - 끝px -> 한번; else -> 0f }
            if (dy != 0f) { 넘김.scrollBy(dy); 판.찾기() }
        }
    }
    Modifier.onGloballyPositioned { if (판.원 == null) 판.틀 = it.boundsInRoot() }
}

/** 칸 하나 — 자리 재기 · 그리기 · 꾹 눌러 끌기를 한 번에. [켬] false 면 끌 수 없다(종목표에 없는 플랜 칸) */
fun Modifier.격자끌기(판: 격자끌기판, 열쇠: String, 이름: String, 무리: String = "", 켬: Boolean = true): Modifier =
    this.격자끌기자리(판, 열쇠, 무리, 켬).격자끌기손(판, 열쇠, 이름, 켬)

/**
 * 칸의 자리 · 그리기만 (끄는 칸 흐림 · 놓을 선). 잡는 곳이 칸 일부일 때(종목 탭 상자 머리) [격자끌기손] 과 나눠 단다 —
 * 안쪽에 길게 누름(눌림길게)이 있으면 그 안쪽에 손을 달아야 끄는 손이 끊기지 않는다
 */
fun Modifier.격자끌기자리(판: 격자끌기판, 열쇠: String, 무리: String = "", 켬: Boolean = true): Modifier = composed {
    if (!켬) return@composed Modifier
    val c = Local색.current
    SideEffect { 판.무리[열쇠] = 무리 }
    DisposableEffect(판, 열쇠) { onDispose { 판.자리.remove(열쇠); 판.무리.remove(열쇠) } }
    Modifier
        .onGloballyPositioned { if (판.원 == null) 판.자리[열쇠] = it.boundsInRoot() }
        .graphicsLayer { alpha = if (판.원 == 열쇠) 움직임.끌림투명 else 1f }
        .drawWithContent {
            drawContent()
            val t = 판.놓을곳
            if (t != null && t.first == 열쇠) {
                val w = 부품치수.놓을선.toPx()
                drawRect(c.강조, topLeft = Offset(if (t.second) size.width - w else 0f, 0f), size = Size(w, size.height))
            }
        }
}

/** 꾹 눌러 끌기 손 — 잡은 곳의 화면 자리는 스스로 잰다 */
fun Modifier.격자끌기손(판: 격자끌기판, 열쇠: String, 이름: String, 켬: Boolean = true): Modifier = composed {
    if (!켬) return@composed Modifier
    val 진동 = LocalHapticFeedback.current
    val 이름최신 by rememberUpdatedState(이름)
    val 내자리 = remember { arrayOf(Rect.Zero) }
    Modifier
        .onGloballyPositioned { if (판.원 == null) 내자리[0] = it.boundsInRoot() }
        .pointerInput(판, 열쇠) {
            detectDragGesturesAfterLongPress(
                onDragStart = { p ->
                    진동.performHapticFeedback(HapticFeedbackType.LongPress)
                    판.시작(열쇠, 이름최신, Offset(내자리[0].left + p.x, 내자리[0].top + p.y))
                },
                onDrag = { ch, d -> ch.consume(); 판.끌림(d) },
                onDragEnd = { 판.끝(true) },
                onDragCancel = { 판.끝(false) },
            )
        }
}

/** 끄는 동안 손가락 위에 따라오는 이름표 (강조 알약 · 루틴 상세와 같은 모양) — 넘기는 칸을 덮는 Box 안에 둔다 */
@Composable
fun BoxScope.격자끌기이름표(판: 격자끌기판) {
    if (판.원 == null) return
    val c = Local색.current
    var 나 by remember { mutableStateOf(Offset.Zero) }
    val 위틈 = with(LocalDensity.current) { 간격.넓게.toPx() }
    Box(Modifier.matchParentSize().onGloballyPositioned { 나 = it.positionInRoot() }) {
        Box(
            Modifier
                .layout { m, cons ->
                    val p = m.measure(cons.copy(minWidth = 0, minHeight = 0))
                    val x = (판.손.x - 나.x - p.width / 2f).coerceIn(0f, (cons.maxWidth - p.width).coerceAtLeast(0).toFloat())
                    val y = (판.손.y - 나.y - p.height - 위틈).coerceAtLeast(0f)
                    layout(cons.maxWidth, cons.maxHeight) { p.place(x.roundToInt(), y.roundToInt()) }
                }
                .clip(CircleShape)
                .background(c.강조)
                .padding(horizontal = 간격.넓게, vertical = 간격.좁게),
        ) { 글(판.이름, 크기값 = 크기.버튼, 색 = c.강조글, 굵기 = FontWeight.Bold) }
    }
}

// ═════════════════════ 순수 계산 (시험: test/…/ui/GridDragTest.kt) ═════════════════════

/** 손가락 아래 칸 — 자기 칸 · 다른 무리 칸은 빼고. 왼쪽 반 = 앞, 오른쪽 반 = 뒤 */
internal fun 격자대상(자리: Map<String, Rect>, 무리: Map<String, String>, 원: String, x: Float, y: Float): Pair<String, Boolean>? {
    val 내무리 = 무리[원] ?: ""
    val hit = 자리.entries.firstOrNull { (k, r) -> k != 원 && (무리[k] ?: "") == 내무리 && x >= r.left && x < r.right && y >= r.top && y < r.bottom }
        ?: return null
    return hit.key to (x > hit.value.center.x)
}

/** 종목표에서 [원] 종목을 [대상] 종목의 앞 · 뒤로 옮긴다 (id). 없거나 같으면 그대로 */
internal fun 종목옮김(표: List<종목>, 원: String, 대상: String, 뒤에: Boolean): List<종목> {
    if (원 == 대상) return 표
    val x = 표.firstOrNull { it.id == 원 } ?: return 표
    if (표.none { it.id == 대상 }) return 표
    val 남 = 표.filter { it.id != 원 }.toMutableList()
    val j = 남.indexOfFirst { it.id == 대상 }
    남.add(if (뒤에) j + 1 else j, x)
    return 남
}
