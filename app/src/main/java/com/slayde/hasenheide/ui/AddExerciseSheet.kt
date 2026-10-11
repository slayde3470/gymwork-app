package com.slayde.hasenheide.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.slayde.hasenheide.data.같은이름번호
import com.slayde.hasenheide.data.칸
import com.slayde.hasenheide.data.종목
import com.slayde.hasenheide.data.플랜
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.글꼴
import com.slayde.hasenheide.ui.theme.높이
import com.slayde.hasenheide.ui.theme.모서리
import com.slayde.hasenheide.ui.theme.선굵기
import com.slayde.hasenheide.ui.theme.움직임
import com.slayde.hasenheide.ui.theme.이름맞춤값
import com.slayde.hasenheide.ui.theme.크기
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * 종목 넣기 시트 (시안 v18 D · v19 C `넣기목록` · v21) — 루틴 화면과 운동 화면이 **같이** 쓴다.
 *
 * ── 2단계 소유: RT(루틴) 도우미. 매개변수(이름 · 꼴)는 바꾸지 않는다 — 새 것은 기본값으로만 ──
 * W(운동) 도우미는 이 함수를 **부르기만** 한다.
 *
 * 10-08 홍겸 님: 이 시트의 목록은 **종목 탭 목록(11번)과 같은 화면**이다 — [종목묶음들](ExerciseScreen.kt)을 그대로 부른다.
 *  · 위끝 고정 · 머리 = 띠 · 띠 오른쪽 [확인](= 닫기) · 칩줄은 고정, 목록만 스크롤
 *  · 상자 맨 앞(이름 왼쪽) 체크 상자 = 넣기(들어 있으면 하나 빼기 · 꾹 = 하나 더) · 들어간 상자는 강조옅음 바탕 + 강조 테
 *  · 상자를 누르면 펼침(종목 탭과 같다 — 기본 세팅 · 근육을 고치고 [저장]) · 꾹 눌러 끌면 순서 옮김
 *  · 오른쪽 아래 구석에 떠 있는 [+ 새 종목 만들기] → [새종목시트]. 저장하면 그 종목을 바로 [넣기] 로 넣고 칩 = [전체]
 *
 * @param 개수 종목 열쇠(`종목.id`) → 지금 들어 있는 개수 (✓×n). **누르는 순간에도 부른다** — 늘 지금 값을 돌려준다
 * @param 넣기 열쇠 하나를 넣는다 (꾹 = 한 번 더 부른다)
 * @param 빼기 열쇠 하나를 뺀다 (들어 있는 것을 누름)
 * @param 새종목만들기 true 면 [+ 새 종목 만들기]
 * @param 플랜개수 · 플랜넣기 · 플랜빼기 (10-05 RT · 기본 null) — 셋 다 넘기면 플랜 칸이 보인다(시안 '플랜넣기'). 열쇠 = 플랜 id
 */
@Composable
fun 종목넣기시트(
    상태: 앱상태,
    제목: String,
    개수: (열쇠: String) -> Int,
    넣기: (열쇠: String) -> Unit,
    빼기: (열쇠: String) -> Unit,
    닫기: () -> Unit,
    새종목만들기: Boolean = true,
    플랜개수: ((플랜id: String) -> Int)? = null,
    플랜넣기: ((플랜id: String) -> Unit)? = null,
    플랜빼기: ((플랜id: String) -> Unit)? = null,
    /** 10-08 홍겸 님: 책장 겹침 단계 — 이 시트의 단계(루틴 상세 안 = 2). 안의 새 종목 시트는 한 겹 더. 기본 0 = 운동 화면 */
    겹: Int = 0,
    /** 10-09 홍겸 님: 루틴 탭은 책장 겹침을 없앴다 → 안의 새 종목 시트도 0 으로 받는다 */
    새종목겹: Int = 겹 + 1,
) {
    var 새로 by remember { mutableStateOf(false) }
    // 시안 v19 C ① 열 때마다 [전체] — 시트를 새로 열면 처음 값 (새 종목 시트를 다녀와도 이 값은 남는다)
    var 칸고름 by remember { mutableStateOf(넣기전체) }
    if (새로) {
        // 시안 v18 C ⑤ 새 종목 시트는 넣기 시트를 '돌아감' 으로 — 닫거나 저장하면 넣기 시트로.
        // 10-06 ①: 저장했으면 그 종목을 바로 넣고(이 시트를 부른 쪽의 넣기) 칩 = [전체]
        새종목시트(상태, 닫기 = { 새로 = false }, 겹 = 새종목겹, 저장 = { e ->
            새로 = false
            발자취.적기("넣기 · 새 종목 넣기 · ${e.이름}")
            넣기(e.id)
            칸고름 = 넣기전체   // 알림은 새종목시트가 띄운 것(만들었습니다 / 근육 안내)을 그대로 둔다 — 덮으면 근육 안내가 사라진다
        })
    } else {
        val 플랜길 = if (플랜개수 != null && 플랜넣기 != null && 플랜빼기 != null) 플랜손(플랜개수, 플랜넣기, 플랜빼기) else null
        var 펼침 by remember { mutableStateOf(false) }
        시트(제목, 닫기, 위끝고정 = true, 닫기글 = "확인", 겹 = 겹, 속늦춤 = true, 머리가림 = 펼침) {
            넣기속(상태, 칸고름, { 칸고름 = it }, 개수, 넣기, 빼기, 새종목만들기, { 새로 = true }, 플랜길, { 펼침 = it })
        }
    }
}

/** 칩 맨 왼쪽 (시안 w0fo) */
internal const val 넣기전체 = "전체"

private class 플랜손(val 개수: (String) -> Int, val 넣기: (String) -> Unit, val 빼기: (String) -> Unit)

/**
 * 시트 속 (10-08 홍겸 님) — **종목 탭 목록(11번 화면)을 그대로** 쓴다: 칩줄 · 카테고리 이름표 · 2열 격자 · 펼침(12번 — 기본 세팅 고치기).
 * 다른 점은 상자마다 맨 앞(이름 왼쪽) 체크 상자(누름 = 넣기 / 들어 있으면 하나 빼기 · 꾹 = 하나 더)와 들어간 상자의 강조옅음 바탕뿐.
 * 칩줄은 그대로 두고 목록만 스크롤한다 (시안 v18 D ⑤) — 공용 [시트] 는 속을 통째로 넘기므로 목록 칸의 높이를 여기서 잰다:
 * 시트 판 안쪽 높이 − 이 칸의 위끝 = 남은 자리. 재지 못하면(시트 모양이 바뀌면) 시트가 넘기는 대로 둔다.
 */
@Composable
private fun 넣기속(
    상태: 앱상태,
    칸고름: String,
    칸바꿈: (String) -> Unit,
    개수: (String) -> Int,
    넣기: (String) -> Unit,
    빼기: (String) -> Unit,
    새종목만들기: Boolean,
    새로열기: () -> Unit,
    플랜길: 플랜손?,
    펼침바뀜: (Boolean) -> Unit = {},
) {
    val d = 상태.d
    val 밀도 = LocalDensity.current
    var 칸높이 by remember { mutableStateOf<Dp?>(null) }
    var 못잼 by remember { mutableStateOf(false) }
    var 펼친 by remember { mutableStateOf<String?>(null) }
    val 펼침최신 by rememberUpdatedState(펼침바뀜)
    LaunchedEffect(펼친 != null) { 펼침최신(펼친 != null) }   // 10-11: 시트 머리 띠도 같이 어둡게
    // 10-11 홍겸 님: 펼치면 펼친 상자 말고 나머지를 어둡게 (종목 탭 [펼침가림막] 을 그대로 쓴다) · 어두운 곳을 누르면 접힌다.
    //  펼친 상자 자리([구멍])는 스크롤 속 칸 기준으로 재서 스크롤해도 안 변한다. 한 번에 하나만 펼침은 전부터 그랬다([펼친] 하나)
    val 구멍틀 = remember { arrayOfNulls<LayoutCoordinates>(2) }   // [0] 스크롤 속 칸 · [1] 펼친 상자
    val 구멍상태 = remember { mutableStateOf<Rect?>(null) }   // 10-11(2차): 펼치는 동안 프레임마다 바뀌므로 구성에서 읽지 않는다 (읽으면 목록 전체가 매 프레임 다시 그려져 렉)
    var 구멍 by 구멍상태
    var 마지막 by remember { mutableStateOf<마지막누름?>(null) }   // 10-11 홍겸 님: 방금 접은 종목 — 반짝이고 화면 밖이면 안으로 되돌린다
    val 마지막틀 = remember { arrayOfNulls<LayoutCoordinates>(1) }
    var 접힘정리 by remember { mutableStateOf(false) }
    val 펼치전 = remember { intArrayOf(-1) }   // 10-11 홍겸 님: 펼치기 직전 스크롤 자리 — 접으면 거기로 돌아간다
    var 보임px by remember { mutableIntStateOf(0) }   // 목록이 보이는 칸의 높이 (어둡게 · 스크롤 여유에 쓴다)
    fun 구멍재기() {
        val 속 = 구멍틀[0]; val 상 = 구멍틀[1]
        구멍 = if (속 != null && 상 != null && 속.isAttached && 상.isAttached) 속.localBoundingBoxOf(상, clipBounds = false) else null
    }
    val 접기 = { 펼친?.let { k -> 마지막 = 마지막누름(k) { c -> 마지막틀[0] = c } }; 펼친 = null }
    val 판톡 = remember루톡(true)
    val 판 = d.종목탭판(칸고름)
    // 꾹 눌러 끌어 종목 순서 바꾸기 — 종목 탭과 같은 판 (같은 카테고리 안 · 종목표 차례)
    val 끌판 = remember격자끌기판({ a, b, 뒤 ->
        발자취.적기("넣기 · 종목 순서 옮김")
        상태.바꿈 { it.copy(종목표 = 종목옮김(it.종목표, a, b, 뒤)) }
    })
    val 손 = 넣기손(상태, 개수, 넣기, 빼기, 플랜길)
    val 쓸 = 칸높이
    Box(
        Modifier.fillMaxWidth().이름칸밖누름()   // 10-08 홍겸 님: 이름 고치는 칸 밖을 누르면 저장
            .then(if (쓸 != null) Modifier.height(쓸) else Modifier)
            .onGloballyPositioned { co ->
                if (못잼) return@onGloballyPositioned
                // 이 칸 → 시트 속 칸(넘기는 칸) → 시트 판. 판 안쪽 높이에서 이 칸의 위끝을 빼면 남은 자리
                val 판틀 = co.parentLayoutCoordinates?.parentLayoutCoordinates
                if (판틀 == null) { 못잼 = true; return@onGloballyPositioned }
                val 남px = 판틀.size.height - 판틀.localPositionOf(co, Offset.Zero).y
                val h = with(밀도) { 남px.toDp() }
                if (h < 넣기값.최소높이 || h > 넣기값.최대높이) { 못잼 = true; return@onGloballyPositioned }
                val 옛 = 칸높이
                if (옛 == null || abs(h.value - 옛.value) >= 1f) 칸높이 = h
            },
    ) {
        Column(Modifier.fillMaxWidth().then(if (쓸 != null) Modifier.fillMaxHeight() else Modifier)) {
            Box(Modifier.fillMaxWidth()) {
                칩줄(listOf(넣기전체) + 판.칸들, 판.고름, { 칸바꿈(it) })
                if (펼친 != null) Box(Modifier.matchParentSize().background(종목치수.넣기가림).눌림(접기))   // 칩줄도 어둡게 (누르면 접힌다)
            }
            Spacer(Modifier.height(간격.좁게))
            // 칩을 바꾸면 맨 위부터 (시안 v18 D ⑤) · 칸을 눌러 다시 그려도 보던 자리 그대로
            key(판.고름) {
                val 넘김 = rememberScrollState()
                // 10-11 홍겸 님: 펼침이 끝난 뒤(애니메이션 중에는 재지 않는다) 펼친 상자 맨 위(= 접힌 때의 이름 줄)가 목록 맨 위에 오도록 **한 번만** 스크롤.
                //  접힐 때는 구멍만 비운다. 다른 종목으로 바꾸면 이 효과가 다시 돌아 새 자리로 한 번 움직인다
                LaunchedEffect(펼친) {
                    if (펼친 == null) {
                        구멍틀[1] = null; 구멍 = null
                        // 10-11 홍겸 님: 접은 뒤 방금 누른 상자가 화면 밖으로 나가 있으면 안으로 부드럽게 되돌린다 (그 상자는 반짝인다)
                        if (마지막 != null) {
                            접힘정리 = true
                            withFrameNanos { }
                            withFrameNanos { }
                            val 돌아감 = 펼치전[0]; 펼치전[0] = -1
                            if (돌아감 >= 0) {
                                if (돌아감 != 넘김.value) 넘김.animateScrollTo(돌아감.coerceAtMost(넘김.maxValue))
                            } else {
                                val 속 = 구멍틀[0]; val 틀 = 마지막틀[0]
                                if (속 != null && 틀 != null && 속.isAttached && 틀.isAttached) {
                                    val r = 속.localBoundingBoxOf(틀, clipBounds = false)
                                    val 위 = 넘김.value
                                    if (r.top < 위 || r.bottom > 위 + 보임px) 넘김.animateScrollTo((r.top - 보임px * 0.25f).roundToInt().coerceAtLeast(0))
                                }
                            }
                            접힘정리 = false
                        }
                    } else {
                        // 10-11 홍겸 님(2차): 펼친 뒤에 움직이면 렉처럼 보인다 → 펼침과 동시에, 새 자리를 바로(애니메이션 없이) 맞춘다.
                        //  이름 줄이 맨 위에 와서 위 카테고리는 화면 밖 · 상자는 거기서 위 → 아래로 펼쳐진다. 이전 상자는 바로 접히므로 두 프레임 뒤 한 번만 잰다
                        if (펼치전[0] < 0) 펼치전[0] = 넘김.value
                        withFrameNanos { }
                        withFrameNanos { }
                        구멍재기()
                        구멍?.let { 넘김.scrollTo(it.top.roundToInt().coerceAtLeast(0)) }
                    }
                }
                if (쓸 == null && !못잼) {
                    // 재는 중 — 한 번만 비워 둔다 (긴 목록을 먼저 그렸다가 줄이지 않게 · U5-6)
                    Spacer(Modifier.height(1.dp))
                } else {
                    // 떠 있는 [새 종목] 단추 자리만큼 아래 여백 (시안 v19 C ②) — 펼쳤을 때는 단추가 목록 맨 끝으로 가므로 뺀다
                    val 아래여백 = if (새종목만들기 && 펼친 == null) 높이.낮게 + 간격.좁게 else 간격.좁게
                    val 아래여백px = with(밀도) { 아래여백.roundToPx() }
                    Column(
                        Modifier.fillMaxWidth()
                            .then(if (쓸 != null) Modifier.weight(1f).onSizeChanged { 보임px = it.height }.격자끌기틀(끌판, 넘김).verticalScroll(넘김) else Modifier)
                            .padding(bottom = 아래여백),
                    ) {
                        // 스크롤 속 칸 하나 — 목록이 짧아도 보이는 칸 끝까지 어둡게 하려고 보이는 높이 이상으로 둔다.
                        //  10-11(2차): [보임px] · [구멍] 은 배치 단계에서만 읽는다 (구성에서 읽으면 목록 전체가 다시 그려져 렉)
                        Box(
                            Modifier.fillMaxWidth()
                                .layout { m, c ->
                                    val 최소 = minOf(max(c.minHeight, (보임px - 아래여백px).coerceAtLeast(0)), c.maxHeight)
                                    val q = m.measure(c.copy(minHeight = 최소))
                                    layout(q.width, q.height) { q.place(0, 0) }
                                }
                                .onGloballyPositioned { 구멍틀[0] = it; 구멍재기() },
                        ) {
                            Column(Modifier.fillMaxWidth()) {
                                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(간격.좁게)) {
                                    종목묶음들(
                                        상태, 판, 끌판, 펼친,
                                        { k -> if (펼친 == k) 접기() else { 마지막 = null; 마지막틀[0] = null; 펼친 = k } },
                                        손, 펼침틀 = { 구멍틀[1] = it; 구멍재기() }, 마지막 = 마지막,
                                    )
                                    if (펼친 != null && 새종목만들기) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        버튼(
                                            "새 종목 만들기", { 판톡.톡(); 발자취.적기("넣기 · 새 종목 만들기"); 새로열기() },
                                            Modifier.루톡(판톡), 주요 = true, 낮게 = true, 그림 = 아이콘.더하기,
                                        )
                                    }
                                }
                                // 펼친 상자를 맨 위로 올릴 수 있게 아래를 비워 둔다 (펼친 상자가 보이는 칸보다 작을 때만) · 접은 직후 화면을 되돌리는 동안도 유지
                                Box(
                                    Modifier.fillMaxWidth().layout { _, c ->
                                        val h = if (펼친 != null || 접힘정리) (보임px - (구멍상태.value?.height ?: 0f).roundToInt()).coerceAtLeast(0) else 0
                                        layout(if (c.hasBoundedWidth) c.maxWidth else 0, h) { }
                                    },
                                )
                            }
                            if (펼친 != null) 가림막자리(구멍상태, 접기, Modifier.matchParentSize())
                        }
                    }
                }
            }
        }
        if (새종목만들기 && 펼친 == null) {
            버튼(
                "새 종목 만들기", { 판톡.톡(); 발자취.적기("넣기 · 새 종목 만들기"); 새로열기() },
                Modifier.align(Alignment.BottomEnd).루톡(판톡), 주요 = true, 낮게 = true, 그림 = 아이콘.더하기,
            )
        }
        격자끌기이름표(끌판)
    }
}

/**
 * 종목 탭 상자 → 넣기 (10-08). 플랜이 있는 종목(그 이름의 첫 종목 · 켠 플랜)은 플랜으로 넣는다(전: 플랜마다 칸 — 이제 상자 하나에 첫 플랜).
 * 플랜만 남은 종목('기타')은 플랜이 있을 때만 체크 상자. 개수는 누르는 순간의 값([상태.d])으로 센다
 */
private fun 넣기손(상태: 앱상태, 개수: (String) -> Int, 넣기: (String) -> Unit, 빼기: (String) -> Unit, 플랜길: 플랜손?): 종목넣기손 {
    fun 플(x: 종목칸값): List<플랜> = if (플랜길 == null) emptyList() else 상태.d.상자플랜(x).filter { it.켬 }
    return 종목넣기손(
        수 = { x ->
            val p = 플(x)
            if (p.isNotEmpty() && 플랜길 != null) p.sumOf { 플랜길.개수(it.id) } else x.종목?.let { 개수(it.id) } ?: 0
        },
        넣기 = { x ->
            val p = 플(x)
            발자취.적기("넣기 · ${x.이름}")
            if (p.isNotEmpty() && 플랜길 != null) 플랜길.넣기(p.first().id) else x.종목?.let { 넣기(it.id) }
        },
        빼기 = { x ->
            val p = 플(x)
            발자취.적기("넣기 빼기 · ${x.이름}")
            if (p.isNotEmpty() && 플랜길 != null) p.lastOrNull { 플랜길.개수(it.id) > 0 }?.let { 플랜길.빼기(it.id) }
            else x.종목?.let { 빼기(it.id) }
        },
        됨 = { x -> x.종목 != null || 플(x).isNotEmpty() },
        플랜카드 = 플랜길 != null,
    )
}

/** 빈 칸 안내 (시안 `.빈칸` — 속선 테 · 가운데 13 옅음) — 루틴 화면도 쓴다 */
@Composable
internal fun 넣기빈칸(글자: String, modifier: Modifier = Modifier) {
    val c = Local색.current
    val 모양 = RoundedCornerShape(모서리.작게)
    Box(
        modifier.fillMaxWidth().clip(모양).border(선굵기.보통, c.속선, 모양).padding(간격.넓게),
        contentAlignment = Alignment.Center,
    ) { 글(글자, 크기값 = 크기.조금작게, 색 = c.옅음, 가운데 = true, 줄 = 3) }   // 10-09: 빈 루틴 안내가 두 줄
}

/** 넣기 시트 값 (시안 CSS) — Theme 에 없는 것만 여기 모았다. 합칠 때 Theme.kt `부품치수` 로 옮긴다 */
internal object 넣기값 {
    val 번호폭 = 12.dp          // .넣기번 min-width
    val 체크 = 20.dp            // .넣기체크 20 × 20
    val 체크모서리 = 4.dp       // .넣기체크 border-radius
    val 딱지겹침 = (-5).dp      // 이름 오른쪽 위 5 겹침 (부품치수.딱지겹침 과 같은 값)
    val 최소높이 = 120.dp       // 잰 목록 높이가 이보다 작거나
    val 최대높이 = 2000.dp      // 크면 잘못 잰 것 — 시트가 넘기는 대로 둔다
    const val 말진하기 = 0.85f  // .넣기말 opacity
    val 연필폭 = 24.dp          // v22 ⑦ .넣기편집 누르는 칸 24 × 28
    val 연필그림 = 16.dp        // .넣기편집 svg 16
}

// ═════════════════════ 순수 계산 (시험: ui/RoutineTest.kt) ═════════════════════

/**
 * 넣기 목록 한 칸. [키] = 종목 id(종목 칸) · [플랜id] = 플랜 칸. [번호] = 같은 이름 번호 딱지(0 = 없음).
 * [열쇠] = 화면에서 칸을 가르는 값 (말풍선 · 자리)
 */
internal data class 넣기칸(
    val 이름: String, val 키: String?, val 플랜id: String?, val 종목이름: String, val 번호: Int,
    /** v22 ⑦ 연필로 고칠 종목 id — 종목표에 없는 종목의 플랜은 null(연필 없음) */
    val 편집키: String? = null,
) {
    val 열쇠: String get() = if (플랜id != null) "p:$플랜id" else "e:$키"
}

/**
 * 넣기 목록 (시안 `넣기목록`) — 종목표 + 플랜. 칸 = 부위("전체" 면 모두).
 *  · 플랜이 있는 종목은 그 종목 칸 대신 플랜마다 한 칸 (같은 이름 종목이 여럿이면 먼저 만든 것에만)
 *  · [전체] 일 때는 종목표에 없는 종목의 플랜도 잃지 않게 칸을 낸다
 *  · 꺼 둔 플랜(켬 = false)은 넣지 않는다 (전 앱과 같게) · [플랜씀] = false 면 플랜 없이 종목만
 *  · 차례 = 종목표 차례 (10-07 홍겸 님: 꾹 눌러 끌어 바꾼다 · 종목 탭과 같다 · 전: 이름 가나다순). 종목표에 없는 플랜은 맨 뒤
 */
internal fun 넣기칸들(종목표: List<종목>, 플랜들: List<플랜>, 칸: String, 플랜씀: Boolean): List<넣기칸> {
    val 켠 = if (플랜씀) 플랜들.filter { it.켬 } else emptyList()
    val 줄 = mutableListOf<넣기칸>()
    for (x in 종목표) {
        if (칸 != 넣기전체 && x.칸 != 칸) continue
        val 첫 = 종목표.first { it.이름 == x.이름 } === x
        val 플 = if (첫) 켠.filter { it.종목 == x.이름 } else emptyList()
        if (플.isNotEmpty()) 플.forEach { p ->
            줄 += 넣기칸(p.이름, null, p.id, x.이름, if (p.이름 == x.이름) 같은이름번호(종목표, x.id, x.이름) else 0, 편집키 = x.id)
        }
        else 줄 += 넣기칸(x.이름, x.id, null, x.이름, 같은이름번호(종목표, x.id, x.이름), 편집키 = x.id)
    }
    if (칸 == 넣기전체) 켠.filter { p -> 종목표.none { it.이름 == p.종목 } }.forEach { p -> 줄 += 넣기칸(p.이름, null, p.id, p.종목, 0) }
    return 줄
}

/**
 * 넣기 이름이 넘쳤나 — 잘렸거나, 줄이 낱말 가운데서 바뀌었다(앞 글자 · 뒤 글자가 모두 띄어쓰기가 아님).
 * [줄끝] = 줄마다 끝 자리(다음 줄 첫 글자 번호)
 */
internal fun 넣기이름넘침(이름: String, 잘림: Boolean, 줄끝: List<Int>): Boolean =
    잘림 || 줄끝.dropLast(1).any { e -> e in 1 until 이름.length && !이름[e - 1].isWhitespace() && !이름[e].isWhitespace() }

/** 체크 상자 글 — 1~3 = ✓ 그 수만큼, 넷부터 ✓×n, 0 = "" (시안 v18 D ③) */
internal fun 체크글(n: Int): String = if (n <= 0) "" else if (n > 3) "✓×${n}" else "✓".repeat(n)

/** 펼친 상자 둘레의 어두운 막 — 구멍(펼친 상자 자리)을 이 안에서만 읽어, 펼치는 동안 이 작은 부분만 다시 그린다. 구멍을 아직 못 쟀으면 전부 어둡게 */
@Composable
private fun 가림막자리(구멍: State<Rect?>, 접기: () -> Unit, modifier: Modifier) {
    val h = 구멍.value
    if (h != null) 펼침가림막(h, 접기, modifier, 종목치수.넣기가림)
    else Box(modifier.background(종목치수.넣기가림).눌림(접기))
}
