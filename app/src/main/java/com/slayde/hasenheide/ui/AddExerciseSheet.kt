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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
 * 시안 동작
 *  · 위끝 10% 고정(10-06 홍겸 님 ⑨) · 머리 = 띠 · 칩 한 줄 ‹ › (열 때마다 [전체]) · 칩 아래 목록만 스크롤
 *  · 2열 격자(가로 먼저 1|2 / 3|4) · 종목표 차례(꾹 눌러 끌어 바꿈 · 10-07) · 칸 = [번호][이름 + 번호/플랜 딱지][체크 상자]
 *  · 체크 상자 = 들어간 개수만큼 ✓ (넷부터 ✓×n) · 들어간 칸은 강조옅음 바탕 + 강조 테
 *  · 누름: 안 들어간 칸 = 하나 넣기(+ 말풍선) · 들어간 칸 = 하나 빼기 / 꾹 = 하나 더 넣기
 *  · 플랜이 있는 종목은 그 칸이 플랜 칸이 된다(플랜마다 한 칸) — [플랜개수] · [플랜넣기] · [플랜빼기] 를 넘긴 곳에서만
 *  · 오른쪽 아래 구석에 떠 있는 [+ 새 종목 만들기] → [새종목시트]. 저장하고 돌아오면 그 종목을 바로 [넣기] 로 넣고 칩 = [전체] (10-06 홍겸 님 ① — 여기서 만든 종목은 넣으려고 만든 것)
 *  · 10-06 v22 ⑥ 띠 오른쪽 위 = [확인] 글 단추 (동작은 닫기 그대로)
 *  · 10-06 v22 ⑦ 칸마다 연필 = 종목 탭 [편집]과 같은 편집 시트([새종목시트] 편집). 저장 · 닫기 · 끌어 닫기 → 이 시트로 돌아온다.
 *    연필은 넣기를 바꾸지 않는다. 고친 종목이 다른 칸으로 옮겨 갔으면 그 칸 칩을 골라 둔다(지금 칩이 [전체]가 아니면)
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
) {
    var 새로 by remember { mutableStateOf(false) }
    // 10-06 v22 ⑦ 연필로 연 편집 시트의 종목 id (null = 안 열림)
    var 편집id by remember { mutableStateOf<String?>(null) }
    // 시안 v19 C ① 열 때마다 [전체] — 시트를 새로 열면 처음 값 (새 종목 시트를 다녀와도 이 값은 남는다)
    var 칸고름 by remember { mutableStateOf(넣기전체) }
    val 편집중 = 편집id?.let { id -> 상태.d.종목표.firstOrNull { it.id == id } }
    if (편집id != null && 편집중 == null) LaunchedEffect(편집id) { 편집id = null }   // 그사이 종목이 없어졌으면 넣기 시트로
    if (편집중 != null) {
        // 종목 탭 [편집] 과 같은 길 (ExerciseScreen `새시트` — 새종목시트(편집 = 종목)). 닫기 · 끌어 닫기 · 저장 → 넣기 시트
        key(편집중.id) {
            새종목시트(상태, 닫기 = { 편집id = null }, 저장 = { e ->
                발자취.적기("넣기 · 종목 편집 저장 · ${e.이름}")
                if (칸고름 != 넣기전체 && 칸고름 != e.칸) 칸고름 = e.칸
            }, 편집 = 편집중)
        }
    } else if (새로) {
        // 시안 v18 C ⑤ 새 종목 시트는 넣기 시트를 '돌아감' 으로 — 닫거나 저장하면 넣기 시트로.
        // 10-06 ①: 저장했으면 그 종목을 바로 넣고(이 시트를 부른 쪽의 넣기) 칩 = [전체]
        새종목시트(상태, 닫기 = { 새로 = false }, 저장 = { e ->
            새로 = false
            발자취.적기("넣기 · 새 종목 넣기 · ${e.이름}")
            넣기(e.id)
            칸고름 = 넣기전체   // 알림은 새종목시트가 띄운 것(만들었습니다 / 근육 안내)을 그대로 둔다 — 덮으면 근육 안내가 사라진다
        })
    } else {
        val 플랜길 = if (플랜개수 != null && 플랜넣기 != null && 플랜빼기 != null) 플랜손(플랜개수, 플랜넣기, 플랜빼기) else null
        시트(제목, 닫기, 위끝고정 = true, 닫기글 = "확인") {
            넣기속(상태, 칸고름, { 칸고름 = it }, 개수, 넣기, 빼기, 새종목만들기, { 새로 = true }, 플랜길) { id ->
                발자취.적기("넣기 · 종목 편집 열기")
                편집id = id
            }
        }
    }
}

/** 칩 맨 왼쪽 (시안 w0fo) */
internal const val 넣기전체 = "전체"

private class 플랜손(val 개수: (String) -> Int, val 넣기: (String) -> Unit, val 빼기: (String) -> Unit)

/**
 * 시트 속 — 칩줄은 그대로 두고 목록만 스크롤한다 (시안 v18 D ⑤).
 * 공용 [시트] 는 속을 통째로 위아래로 넘기므로, 목록 칸의 높이를 여기서 잰다:
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
    편집열기: (종목id: String) -> Unit,
) {
    val c = Local색.current
    val d = 상태.d
    val 밀도 = LocalDensity.current
    var 칸높이 by remember { mutableStateOf<Dp?>(null) }
    var 못잼 by remember { mutableStateOf(false) }
    var 틀 by remember { mutableStateOf(Rect.Zero) }        // 이 칸 (화면 기준)
    var 목록틀 by remember { mutableStateOf(Rect.Zero) }    // 목록 칸 (화면 기준)
    val 칸자리 = remember { HashMap<String, Rect>() }   // 그리기에는 안 쓴다 — 누른 뒤 말풍선 자리에만
    // 말풍선 — (칸 열쇠, 몇 번째). 같은 칸을 또 눌러도 다시 1.5초
    var 말 by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var 말번호 by remember { mutableIntStateOf(0) }
    val 말흐림 = remember { Animatable(1f) }
    LaunchedEffect(말?.second) {
        if (말 == null) return@LaunchedEffect
        말흐림.snapTo(1f)
        delay(움직임.토스트.toLong())
        말흐림.animateTo(0f, tween(움직임.띠흐림))
        말 = null
    }
    LaunchedEffect(칸고름) { 말 = null }

    // 10-07 홍겸 님: 꾹 눌러 끌어 종목 순서 바꾸기 (종목 탭과 같은 차례 = 종목표 차례). 거의 안 움직이고 떼면 전처럼 '하나 더 넣기'
    val 지금줄 = 넣기칸들(d.종목표, d.플랜들, 칸고름, 플랜길 != null)
    val 줄최신 by rememberUpdatedState(지금줄)
    val 끌판 = remember격자끌기판(
        옮김 = { a, b, 뒤 ->
            val 원 = 줄최신.firstOrNull { it.열쇠 == a }?.편집키
            val 대 = 줄최신.firstOrNull { it.열쇠 == b }?.편집키
            if (원 != null && 대 != null && 원 != 대) { 발자취.적기("넣기 · 종목 순서 옮김"); 상태.바꿈 { 종목옮김(it.종목표, 원, 대, 뒤).let { l -> it.copy(종목표 = l) } } }
        },
        제자리 = { a ->
            val z = 줄최신.firstOrNull { it.열쇠 == a } ?: return@remember격자끌기판
            발자취.적기("넣기 하나 더 · ${z.이름}")
            if (z.플랜id != null) 플랜길?.넣기?.invoke(z.플랜id) else 넣기(z.키 ?: "")
        },
    )
    val 쓸 = 칸높이
    Box(
        Modifier.fillMaxWidth()
            .then(if (쓸 != null) Modifier.height(쓸) else Modifier)
            .onGloballyPositioned { co ->
                틀 = co.boundsInRoot()
                if (못잼) return@onGloballyPositioned
                // 이 칸 → 시트 속 칸(넘기는 칸) → 시트 판. 판 안쪽 높이에서 이 칸의 위끝을 빼면 남은 자리
                val 판 = co.parentLayoutCoordinates?.parentLayoutCoordinates
                if (판 == null) { 못잼 = true; return@onGloballyPositioned }
                val 남px = 판.size.height - 판.localPositionOf(co, Offset.Zero).y
                val h = with(밀도) { 남px.toDp() }
                if (h < 넣기값.최소높이 || h > 넣기값.최대높이) { 못잼 = true; return@onGloballyPositioned }
                val 옛 = 칸높이
                if (옛 == null || abs(h.value - 옛.value) >= 1f) 칸높이 = h
            },
    ) {
        Column(Modifier.fillMaxWidth().then(if (쓸 != null) Modifier.fillMaxHeight() else Modifier)) {
            화살칩줄(listOf(넣기전체) + d.카테고리, 칸고름, { 칸바꿈(it) })
            Spacer(Modifier.height(간격.좁게))
            // 칩을 바꾸면 맨 위부터 (시안 v18 D ⑤) · 칸을 눌러 다시 그려도 보던 자리 그대로
            key(칸고름) {
                val 넘김 = rememberScrollState()
                if (쓸 == null && !못잼) {
                    // 재는 중 — 한 번만 비워 둔다 (긴 목록을 먼저 그렸다가 줄이지 않게 · U5-6)
                    Spacer(Modifier.height(1.dp))
                } else Column(
                    Modifier.fillMaxWidth()
                        .then(if (쓸 != null) Modifier.weight(1f).격자끌기틀(끌판, 넘김).verticalScroll(넘김) else Modifier)
                        .onGloballyPositioned { 목록틀 = it.boundsInRoot() }
                        // 떠 있는 [새 종목] 단추 자리만큼 아래 여백 (시안 v19 C ②)
                        .padding(bottom = if (새종목만들기) 높이.낮게 + 간격.좁게 else 간격.좁게),
                    verticalArrangement = Arrangement.spacedBy(간격.좁게),
                ) {
                    val 줄들 = 지금줄
                    if (줄들.isEmpty()) 넣기빈칸("이 칸에 종목이 없습니다")
                    줄들.chunked(2).forEachIndexed { ri, 둘 ->
                        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
                            둘.forEachIndexed { ci, z ->
                                // 누르는 순간의 개수로 가른다 — 빠르게 두 번 눌러도 늦게 그려진 화면 값으로 하지 않는다
                                fun 수(): Int = if (z.플랜id != null) 플랜길?.개수?.invoke(z.플랜id) ?: 0 else 개수(z.키 ?: "")
                                fun 더() { if (z.플랜id != null) 플랜길?.넣기?.invoke(z.플랜id) else 넣기(z.키 ?: "") }
                                fun 뺌() { if (z.플랜id != null) 플랜길?.빼기?.invoke(z.플랜id) else 빼기(z.키 ?: "") }
                                key(z.열쇠) {
                                    넣기칸그림(
                                        z, ri * 2 + ci + 1, 수(),
                                        Modifier.weight(1f).fillMaxHeight().onGloballyPositioned { 칸자리[z.열쇠] = it.boundsInRoot() },
                                        끌판 = 끌판,
                                        on편집 = z.편집키?.let { k -> { 편집열기(k) } },
                                        on누름 = {
                                            if (수() > 0) { 발자취.적기("넣기 빼기 · ${z.이름}"); 뺌() }
                                            else { 발자취.적기("넣기 · ${z.이름}"); 더(); 말 = z.열쇠 to ++말번호 }
                                        },
                                        on꾹 = { },   // 꾹 = 끌기판이 맡는다(떼면 하나 더 · 끌면 순서 옮김)
                                    )
                                }
                            }
                            if (둘.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        if (새종목만들기) {
            val 판 = remember루톡(true)
            버튼(
                "새 종목 만들기", { 판.톡(); 발자취.적기("넣기 · 새 종목 만들기"); 새로열기() },
                Modifier.align(Alignment.BottomEnd).루톡(판), 주요 = true, 낮게 = true, 그림 = 아이콘.더하기,
            )
        }
        격자끌기이름표(끌판)
        // 말풍선 — 누른 칸 위(목록 첫 줄이라 자리가 없으면 아래) · 누름 통과 · 1.5초 뒤 흐려짐 (시안 v19 C ③)
        말?.let { (열쇠, _) ->
            val 칸 = 칸자리[열쇠]
            if (칸 != null) Box(Modifier.matchParentSize().clipToBounds()) {
                Box(
                    Modifier
                        .layout { m, cons ->
                            val p = m.measure(cons.copy(minWidth = 0, minHeight = 0))
                            val 틈 = 간격.아주좁게.roundToPx()
                            val 끝 = 간격.좁게.roundToPx()
                            val 위 = if (칸.top - p.height - 틈 >= 목록틀.top) 칸.top - p.height - 틈 else 칸.bottom + 틈
                            val 오른끝 = max(틀.left + 끝, 틀.right - p.width - 끝)
                            val 왼 = (칸.center.x - p.width / 2f).coerceIn(틀.left + 끝, 오른끝)
                            layout(cons.maxWidth, cons.maxHeight) { p.place((왼 - 틀.left).roundToInt(), (위 - 틀.top).roundToInt()) }
                        }
                        .graphicsLayer { alpha = 넣기값.말진하기 * 말흐림.value }
                        .clip(RoundedCornerShape(모서리.작게))
                        .background(c.흐림)
                        .padding(horizontal = 간격.좁게, vertical = 간격.아주좁게),
                ) { Text("꾸욱 누르면 한번 더 추가 됩니다.", style = 글꼴.보통(크기.작게), color = c.바탕, maxLines = 1, softWrap = false) }
            }
        }
    }
}

/**
 * 넣기 칸 하나 (시안 `.넣기칸` · v22 ⑦ `.넣기묶음`) — [번호 11 흐림][이름 13 굵게 두 줄까지 + 딱지][체크 상자][연필] · 높이 44 이상.
 * 연필(누르는 칸 24 × 28 · 그림 16 · 옅음)은 칸 오른쪽 끝 안쪽 4 에 겹친다 — 넣기 누름과 따로(연필은 넣기를 바꾸지 않는다).
 * 칸 오른쪽 여백 = 4 + 24 (연필이 없는 칸도 같게 — 체크 줄이 가지런). [on편집] null = 연필 없음(종목표에 없는 종목의 플랜)
 */
@Composable
private fun 넣기칸그림(z: 넣기칸, 번호: Int, 수: Int, modifier: Modifier, 끌판: 격자끌기판, on편집: (() -> Unit)?, on누름: () -> Unit, on꾹: () -> Unit) {
    val c = Local색.current
    Box(modifier) {
        넣기칸단추(z, 번호, 수, Modifier.fillMaxWidth().fillMaxHeight(), 끌판, on누름, on꾹)
        if (on편집 != null) Box(
            Modifier.align(Alignment.CenterEnd).padding(end = 간격.아주좁게)
                .size(넣기값.연필폭, 높이.아주낮게).clip(RoundedCornerShape(모서리.작게))
                .semantics { contentDescription = "${z.종목이름} 편집" }
                .눌림(on편집),
            contentAlignment = Alignment.Center,
        ) { Icon(아이콘.연필, null, Modifier.size(넣기값.연필그림), tint = c.옅음) }
    }
}

@Composable
private fun 넣기칸단추(z: 넣기칸, 번호: Int, 수: Int, modifier: Modifier, 끌판: 격자끌기판, on누름: () -> Unit, on꾹: () -> Unit) {
    val c = Local색.current
    val 들어감 = 수 > 0
    val 모양 = RoundedCornerShape(모서리.작게)
    Row(
        modifier
            .heightIn(min = 높이.높게)
            .clip(모양)
            .background(if (들어감) c.강조옅음 else c.면)
            .border(선굵기.보통, if (들어감) c.강조 else c.선, 모양)
            .semantics(mergeDescendants = true) {
                contentDescription = "${z.이름}${if (z.플랜id != null) " 플랜" else ""} · ${수}개 들어 있음 · " +
                    "${if (들어감) "누르면 하나 빼기" else "누르면 넣기"}, 꾹 누르면 하나 더 · 꾹 눌러 끌면 순서 옮김"
            }
            .눌림길게(on누름, on꾹)
            // 끌기는 누름(눌림길게)보다 안쪽 — 끄는 손을 먼저 받아야 끊기지 않는다
            .격자끌기(끌판, z.열쇠, z.이름, 켬 = z.편집키 != null)
            .padding(start = 간격.좁게, end = 간격.아주좁게 + 넣기값.연필폭, top = 간격.아주좁게, bottom = 간격.아주좁게),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(간격.아주좁게),
    ) {
        Text(
            "$번호", Modifier.widthIn(min = 넣기값.번호폭).padding(end = 간격.아주좁게),
            style = 글꼴.보통(크기.작게).copy(fontFeatureSettings = "tnum"), color = c.흐림, maxLines = 1, textAlign = TextAlign.End,
        )
        // 이름은 두 줄까지 — 딱지는 이름 오른쪽 위에 겹친다 (시안 `.이름플랜` · 위 4 띄움).
        // v22 ⑦ 연필 자리만큼 좁아진 이름 — 두 줄까지 13, 안 들어가면 11 → 자간 좁힘 · … 로 자르지 않는다
        Row(Modifier.weight(1f).padding(top = 간격.아주좁게), verticalAlignment = Alignment.Top) {
            넣기이름(z.이름, Modifier.weight(1f, fill = false))
            if (z.번호 > 0 || z.플랜id != null) Row(Modifier.offset(x = 넣기값.딱지겹침), horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                번호딱지(z.번호)
                if (z.플랜id != null) 플랜딱지()
            }
        }
        val 상자 = RoundedCornerShape(넣기값.체크모서리)
        Box(
            Modifier.heightIn(min = 넣기값.체크).widthIn(min = 넣기값.체크).clip(상자)
                .background(if (들어감) c.강조 else c.면)
                .border(선굵기.보통, if (들어감) c.강조 else c.속선, 상자)
                .padding(horizontal = 간격.아주좁게),
            contentAlignment = Alignment.Center,
        ) {
            if (들어감) Text(체크글(수), style = 글꼴.보통(크기.작게, FontWeight.Bold), color = c.강조글, maxLines = 1, softWrap = false)
        }
    }
}

/**
 * 넣기 칸 이름 (시안 v22 ⑦ `.넣기칸 b.이름맞춤` data-줄=2) — 두 줄까지 13 그대로. 넘치거나 낱말 가운데서 줄이 바뀌면
 * 11 → 자간 좁힘(이름맞춤값). 가장 작게도 안 되면 그대로 둔다(낱말 안에서 끊김 — 잘려 안 보이는 것보다 낫다).
 * 공용 [이름맞춤] 은 BoxWithConstraints 라 넣기 줄(IntrinsicSize.Min)에 못 넣어 여기서 글 재기로 한다
 */
@Composable
private fun 넣기이름(이름: String, modifier: Modifier) {
    val c = Local색.current
    // 단계 0 = 13 · 1 = 11 · 2.. = 11 + 자간 −0.01em × (단계 − 1)
    var 단계 by remember(이름) { mutableIntStateOf(0) }
    val 끝단계 = 1 + (-이름맞춤값.자간끝 / 이름맞춤값.자간폭).roundToInt()
    val 꼴 = if (단계 == 0) 글꼴.보통(크기.버튼, FontWeight.Bold)
        else 글꼴.보통(크기.작게, FontWeight.Bold).let { if (단계 >= 2) it.copy(letterSpacing = (-(단계 - 1) * 이름맞춤값.자간폭).em) else it }
    Text(
        이름, modifier, style = 꼴, color = c.글, maxLines = 2, overflow = TextOverflow.Clip,
        onTextLayout = { r -> if (단계 < 끝단계 && 넣기이름넘침(이름, r.hasVisualOverflow, (0 until r.lineCount).map { r.getLineEnd(it) })) 단계++ },
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
    ) { 글(글자, 크기값 = 크기.조금작게, 색 = c.옅음, 가운데 = true) }
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
