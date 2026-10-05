package com.slayde.hasenheide.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.slayde.hasenheide.data.같은이름번호
import com.slayde.hasenheide.data.루틴
import com.slayde.hasenheide.data.루틴바꿈
import com.slayde.hasenheide.data.루틴종목
import com.slayde.hasenheide.data.루틴줄
import com.slayde.hasenheide.data.목옮김
import com.slayde.hasenheide.data.목표
import com.slayde.hasenheide.data.무게글
import com.slayde.hasenheide.data.무게반올림
import com.slayde.hasenheide.data.묶음정리
import com.slayde.hasenheide.data.분초
import com.slayde.hasenheide.data.세기더함
import com.slayde.hasenheide.data.세기이름
import com.slayde.hasenheide.data.세트
import com.slayde.hasenheide.data.세트고침
import com.slayde.hasenheide.data.세트더하기
import com.slayde.hasenheide.data.세트빼기
import com.slayde.hasenheide.data.시간글
import com.slayde.hasenheide.data.앱데이터
import com.slayde.hasenheide.data.열쇠
import com.slayde.hasenheide.data.예상초
import com.slayde.hasenheide.data.예정맞추기
import com.slayde.hasenheide.data.예정초기화
import com.slayde.hasenheide.data.종목옮기기
import com.slayde.hasenheide.data.총세트
import com.slayde.hasenheide.data.다음회
import com.slayde.hasenheide.data.처방세트
import com.slayde.hasenheide.data.회처방
import com.slayde.hasenheide.data.플랜
import com.slayde.hasenheide.data.플랜줄채움
import com.slayde.hasenheide.data.플랜표
import com.slayde.hasenheide.data.휴식
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.글꼴
import com.slayde.hasenheide.ui.theme.높이
import com.slayde.hasenheide.ui.theme.모서리
import com.slayde.hasenheide.ui.theme.부품치수
import com.slayde.hasenheide.ui.theme.선굵기
import com.slayde.hasenheide.ui.theme.움직임
import com.slayde.hasenheide.ui.theme.크기
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 루틴 화면 (시안 v8 R · v10 · v18 D · v19 A/C · v20 · v21) — `루틴탭` · `루틴상세띠` · `루틴상세` · `루틴종목상자`.
 *
 *  · 목록 — 머리 띠 '루틴' · 카드(이름 · 'N번째'(자동) / '수동' · N종목 · N세트 · 예상) · [+ 루틴][+ 휴식일]. 카드 누름 = 열기 · 꾹 끌기 = 순서
 *  · 상세 — 띠 [‹ 루틴][이름 칸 + 흐린 예상 · 세트][자동 스위치] · [+ 종목 추가] · 종목 상자(처음엔 접힘) · 끝 [+ 종목 추가] · [이 루틴 지우기]
 *  · 종목 상자 — 머리 [이름 · N세트 ▾][✕]. 펼치면 세트 줄 [번호][− 무게 ＋][− 횟수 ＋][− 휴식 ＋][휴지통] · [+ 세트]
 *  · 플랜 상자 — 한 줄 [이름[플랜] · N세트 · N회차 · 게이지 · %][✕]. 줄을 누르면 플랜 고치기 (PS 몫 · 부르기만)
 *  · 당겨서 새로고침은 띠 아래에서만 (v19 A ②)
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun 루틴화면(상태: 앱상태, 폰: 폰기능) {
    val 열린 = 루틴기억.열린.value
    val r = 열린?.let { 상태.d.루틴(it) }
    // 열어 둔 루틴이 사라졌으면(지움 · 되돌리기) 목록으로
    LaunchedEffect(열린, r == null) { if (열린 != null && r == null) 루틴기억.열린.value = null }
    Box(Modifier.fillMaxSize()) {
        if (r != null) 루틴상세화면(상태, r) { 루틴기억.열린.value = null }
        else 루틴목록화면(상태) { 루틴기억.열린.value = it }
        플랜고치기자리(상태)   // 플랜 상자 누름 → 플랜 고치기 시트 (PlanScreen.kt · PS 몫)
    }
}

/** 탭을 옮겨 다녀와도 남는 것 (시안 `U.루틴열림` · `U.루펼침`) — 처음 쓸 때 만든다 */
private object 루틴기억 {
    val 열린 = mutableStateOf<String?>(null)
    /** 루틴 id → 펼친 상자 번호들 (처음엔 아무것도 안 펼침) */
    val 펼침 = mutableStateMapOf<String, Set<Int>>()
}

/** 받침에 따라 을/를 · 은/는 (09-24) — 다른 화면도 쓴다 */
fun 조사(말: String, 받침: String, 없음: String): String {
    val c = 말.lastOrNull()?.code ?: return 말 + 없음
    val 받 = c in 0xAC00..0xD7A3 && (c - 0xAC00) % 28 != 0
    return 말 + (if (받) 받침 else 없음)
}

/** 펼치기 · 접기 단추 — 다른 화면도 쓴다 (U4-4) */
@Composable
fun 펼침단추(열림: Boolean, onClick: () -> Unit) {
    val c = Local색.current
    // 10-02: 화살표가 돌아간다 (전에는 한 번에 뒤집혔다)
    val 각 by androidx.compose.animation.core.animateFloatAsState(if (열림) 180f else 0f,
        tween(움직임.펼침), label = "펼침단추")
    Box(Modifier.size(높이.낮게).눌림(onClick), contentAlignment = Alignment.Center) {
        Icon(아이콘.아래, if (열림) "접기" else "펼치기", Modifier.size(18.dp).graphicsLayer { rotationZ = 각 }, tint = c.옅음)
    }
}

/**
 * 보고서 루틴 상자를 누르면 여는 '○○ 상세' 시트 — 틀만 (시안 v18 `루틴상세시트` · 홍겸 님 "형태만 만들어놔").
 * R(보고서) 쪽은 이것을 **부르기만** 한다. (이름이 R 의 것과 겹치지 않게 '준비' 를 붙였다)
 */
@Composable
fun 루틴상세준비시트(이름: String, 닫기: () -> Unit) {
    시트("$이름 상세", 닫기) {
        글("자세한 기록은 준비 중입니다", 크기값 = 크기.작게, 색 = Local색.current.옅음)
    }
}

// ═════════════════════ 루틴 목록 (시안 `루틴탭`) ═════════════════════

@Composable
private fun 루틴목록화면(상태: 앱상태, 열기: (String) -> Unit) {
    val c = Local색.current
    val d = 상태.d
    val 넘김 = rememberScrollState()
    val 끌 = remember { 세로끌기() }
    val 진동 = LocalHapticFeedback.current
    fun 놓기() {
        val 원 = 끌.원 ?: return
        val t = 끌.대상(넘김.value)
        끌.끝()
        if (t == null || t.first == 원) return
        상태.바꿈 { dd ->
            val l = dd.루틴들.toMutableList()
            if (목옮김(l, 원, t.first, t.second)) dd.copy(루틴들 = l).예정초기화(상태.오늘) else dd
        }
        발자취.적기("루틴 순서 바꿈")
    }
    끌기자동넘김(끌, 넘김)
    Box(Modifier.fillMaxSize().onGloballyPositioned { 끌.틀 = it.boundsInRoot() }) {
        Column(Modifier.fillMaxSize()) {
            머리띠("루틴")
            당겨새로고침({ 발자취.적기("루틴 새로고침") }, Modifier.weight(1f).fillMaxWidth(), 켬 = 끌.원 == null) {
                Column(
                    Modifier.fillMaxSize().verticalScroll(넘김)
                        .padding(start = 간격.보통, end = 간격.보통, top = 간격.보통, bottom = 간격.보통),
                    verticalArrangement = Arrangement.spacedBy(간격.좁게),
                ) {
                    val 번째 = 자동번호(d.루틴들)
                    d.루틴들.forEachIndexed { i, r ->
                        key(r.id) {
                            루틴카드(상태, r, 번째[r.id], i, 끌, 놓을표 = if (끌.원 != null) 끌.표시(i, 넘김.value) else -1,
                                on열기 = { 발자취.적기("루틴 열기 · ${r.이름}"); 열기(r.id) },
                                on끌기시작 = { y -> 진동.performHapticFeedback(HapticFeedbackType.LongPress); 끌.시작(i, r.이름, y, 넘김.value, 상태.d.루틴들.size) },
                                on끌기끝 = { 놓기() })
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
                        val 판1 = remember루톡(true)
                        val 판2 = remember루톡(true)
                        버튼("루틴", { 판1.톡(); 루틴더하기(상태, false, 열기) }, Modifier.weight(1f).루톡(판1), 그림 = 아이콘.더하기)
                        버튼("휴식일", { 판2.톡(); 루틴더하기(상태, true, 열기) }, Modifier.weight(1f).루톡(판2), 그림 = 아이콘.더하기)
                    }
                }
            }
        }
        끌기이름표(끌)
    }
}

/** [+ 루틴] · [+ 휴식일] — 맨 아래에 더하고 바로 연다. 자동생성은 꺼진 채 (시안 '루틴추가' · '휴식일추가') */
private fun 루틴더하기(상태: 앱상태, 휴식일: Boolean, 열기: (String) -> Unit) {
    val id = "r" + System.currentTimeMillis()
    val 이름 = if (휴식일) "휴식일" else "새 루틴 ${상태.d.루틴들.size + 1}"
    상태.바꿈 { it.copy(루틴들 = it.루틴들 + 루틴(id, 이름, 휴식일 = 휴식일)) }
    발자취.적기("루틴 추가 · $이름")
    열기(id)
}

/** 루틴 카드 (시안 `.루틴카드`) — 속선 1 테 · 자동이면 왼쪽 4 강조 띠 · [이름][N번째 / 수동] · 한 줄 요약 */
@Composable
private fun 루틴카드(
    상태: 앱상태, r: 루틴, 번째: Int?, i: Int, 끌: 세로끌기, 놓을표: Int,
    on열기: () -> Unit, on끌기시작: (Float) -> Unit, on끌기끝: () -> Unit,
) {
    val c = Local색.current
    val 실 = 상태.d.플랜줄채움(r)
    val 모양 = RoundedCornerShape(모서리.보통)
    val 시작 by rememberUpdatedState(on끌기시작)
    val 끝 by rememberUpdatedState(on끌기끝)
    Column(
        Modifier.fillMaxWidth()
            .onGloballyPositioned { if (끌.원 == null) 끌.자리[i] = it.boundsInRoot() }
            .alpha(if (끌.원 == i) 움직임.끌림투명 else 1f)
            .clip(모양)
            .background(c.면)
            .border(선굵기.보통, c.속선, 모양)
            .then(if (r.자동생성) Modifier.왼띠(c.강조, 루틴값.자동띠) else Modifier)
            .놓을선(놓을표, c.강조)
            .semantics(mergeDescendants = true) { contentDescription = "${r.이름} 열기" }
            .눌림(on열기)
            .pointerInput(r.id, i) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { p -> 시작((끌.자리[i]?.top ?: 0f) + p.y) },
                    onDrag = { ch, 양 -> ch.consume(); 끌.y += 양.y },
                    onDragEnd = { 끝() },
                    onDragCancel = { 끌.끝() },
                )
            }
            .padding(start = if (r.자동생성) 간격.보통 + 루틴값.자동띠 else 간격.보통, end = 간격.보통, top = 간격.보통, bottom = 간격.보통),
        verticalArrangement = Arrangement.spacedBy(루틴값.카드줄틈),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
            Text(r.이름, Modifier.weight(1f), style = 글꼴.보통(크기.본문, FontWeight.Bold), color = c.글, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (번째 != null) 채운알약("${번째}번째") else 글("수동", 크기값 = 크기.작게, 색 = c.옅음)
        }
        맞춤글(
            if (r.휴식일) "휴식일" else "${r.종목.size}종목 · ${총세트(실)}세트 · 예상 ${시간글(예상초(실))}",
            최대 = 크기.조금작게, 색 = c.흐림,
        )
    }
}

/** 강조 바탕 알약 (시안 `.알약.강조`) — 11 굵게 · 강조글 */
@Composable
private fun 채운알약(글자: String) {
    val c = Local색.current
    Box(Modifier.clip(RoundedCornerShape(모서리.작게)).background(c.강조).padding(horizontal = 부품치수.딱지옆)) {
        글(글자, 크기값 = 크기.작게, 색 = c.강조글, 굵기 = FontWeight.Bold)
    }
}

// ═════════════════════ 루틴 상세 (시안 `루틴상세띠` · `루틴상세` · `루틴종목상자`) ═════════════════════

@Composable
private fun 루틴상세화면(상태: 앱상태, r: 루틴, 닫기: () -> Unit) {
    val c = Local색.current
    val d = 상태.d
    val rid = r.id
    val 넘김 = rememberScrollState()
    val 끌 = remember(rid) { 세로끌기() }
    val 진동 = LocalHapticFeedback.current
    val 초점 = LocalFocusManager.current
    var 넣기열림 by remember(rid) { mutableStateOf(false) }
    var 지움확인 by remember(rid) { mutableStateOf(false) }
    val 펼침 = 루틴기억.펼침[rid] ?: emptySet()
    fun 펼침바꿈(f: (Set<Int>) -> Set<Int>) { 루틴기억.펼침[rid] = f(루틴기억.펼침[rid] ?: emptySet()) }
    // 지우기 확인은 다른 것을 누르면 풀린다 (시안 U.확인). 손댐 = 그리고 쳐 넣던 값은 넣는다(초점을 놓아 칸이 값을 넣게)
    fun 확인풀기() { 지움확인 = false }
    fun 손댐() { 확인풀기(); 초점.clearFocus() }

    BackHandler { 초점.clearFocus(); 닫기() }

    fun 줄고침(j: Int, f: (루틴종목) -> 루틴종목) = 상태.바꿈 { dd ->
        dd.루틴바꿈(rid) { x -> if (j in x.종목.indices) x.copy(종목 = x.종목.mapIndexed { k, y -> if (k == j) f(y) else y }) else x }
    }
    fun 놓기() {
        val 원 = 끌.원 ?: return
        val t = 끌.대상(넘김.value)
        끌.끝()
        if (t == null || t.first == 원) return
        val 지금 = 상태.d.루틴(rid) ?: return
        val 차례 = 루틴옮김표(지금, 원, t.first, t.second)
        상태.바꿈 { dd -> dd.루틴바꿈(rid) { x -> x.종목옮기기(원, t.first, t.second).묶음정리() } }
        펼침바꿈 { 펼침맞춤(it, 차례) }
        발자취.적기("루틴 안 종목 순서 바꿈")
    }
    fun 종목뺌(j: Int) {
        손댐()
        val 지금 = 상태.d.루틴(rid) ?: return
        val e = 지금.종목.getOrNull(j) ?: return
        상태.바꿈 { dd -> dd.루틴바꿈(rid) { x -> 루줄빼기(x, j) } }
        펼침바꿈 { 펼침뺌(it, j) }
        발자취.적기("루틴에서 빼기 · ${e.이름}")
        상태.알림.되돌림("루종목빼기", { n -> if (n > 1) "종목 ${n}개를 뺐습니다" else "${조사(e.이름, "을", "를")} 뺐습니다" }) {
            상태.바꿈 { dd -> dd.루틴바꿈(rid) { x -> 루줄끼움(x, j, e) } }
            펼침바꿈 { 펼침끼움(it, j) }
        }
    }
    끌기자동넘김(끌, 넘김)

    Box(Modifier.fillMaxSize().onGloballyPositioned { 끌.틀 = it.boundsInRoot() }) {
        Column(Modifier.fillMaxSize()) {
            루틴상세띠(상태, r, { 초점.clearFocus(); 닫기() }, ::확인풀기)
            당겨새로고침({ 발자취.적기("루틴 새로고침") }, Modifier.weight(1f).fillMaxWidth(), 켬 = 끌.원 == null) {
                var 위단추아래 by remember(rid) { mutableIntStateOf(0) }
                var 끝단추보임 by remember(rid) { mutableStateOf(false) }
                val 끝몫 = with(LocalDensity.current) { 높이.보통.roundToPx() }
                // 끝 [+ 종목 추가] — 맨 아래까지 내렸을 때 위 단추가 화면 밖으로 나갈 때만 (시안 mqjf `루끝단추맞춤`). 그린 뒤 재고 한 번만 정한다
                LaunchedEffect(넘김.maxValue, 위단추아래, r.종목.size) {
                    if (넘김.maxValue == Int.MAX_VALUE) return@LaunchedEffect
                    끝단추보임 = r.종목.isNotEmpty() && 끝단추판정(넘김.maxValue, 끝몫, 끝단추보임, 위단추아래)
                }
                Column(
                    Modifier.fillMaxSize().verticalScroll(넘김)
                        .padding(start = 간격.보통, end = 간격.보통, bottom = 간격.보통),
                ) {
                    if (r.휴식일) {
                        넣기빈칸("휴식일", Modifier.padding(top = 간격.보통))
                    } else {
                        종목추가단추(Modifier.padding(top = 간격.보통, bottom = 간격.좁게).onGloballyPositioned {
                            위단추아래 = (it.positionInParent().y + it.size.height).roundToInt()   // 넘김과 상관없는 내용 안 자리
                        }) { 손댐(); 넣기열림 = true }
                        if (r.종목.isEmpty()) 넣기빈칸("종목을 넣어 주세요", Modifier.padding(bottom = 간격.좁게))
                        r.종목.forEachIndexed { j, e ->
                            // 같은 줄이 둘이어도(같은 종목 두 번) 자리로 가른다
                            key(j) {
                                val 표시 = if (끌.원 != null) 끌.표시(j, 넘김.value) else -1
                                val 자리줄 = Modifier.padding(bottom = 간격.좁게)
                                    .onGloballyPositioned { if (끌.원 == null) 끌.자리[j] = it.boundsInRoot() }
                                    .alpha(if (끌.원 == j) 움직임.끌림투명 else 1f)
                                val 끌기길 = 끌기손(끌, rid, j, { 놓기() }) { y ->
                                    손댐()
                                    진동.performHapticFeedback(HapticFeedbackType.LongPress)
                                    끌.시작(j, e.이름, y, 넘김.value, 상태.d.루틴(rid)?.종목?.size ?: 0)
                                }
                                val p = e.플랜id?.let { pid -> d.플랜들.firstOrNull { it.id == pid } }
                                if (e.플랜id != null) 플랜상자(상태, e, p, 자리줄, 표시, 끌기길,
                                    on고치기 = { 손댐(); if (p != null) { 발자취.적기("루틴에서 플랜 고치기 · ${p.이름}"); 플랜고침.value = p.id } },
                                    on빼기 = { 종목뺌(j) })
                                else 종목상자(상태, e, j in 펼침, 자리줄, 표시, 끌기길,
                                    on펼침 = { 손댐(); 펼침바꿈 { s -> if (j in s) s - j else s + j } },
                                    on빼기 = { 종목뺌(j) },
                                    on고침 = { f -> 확인풀기(); 줄고침(j, f) },
                                    on세트지움 = { k ->
                                        손댐()
                                        val 그줄 = 상태.d.루틴(rid)?.종목?.getOrNull(j)
                                        if (그줄 != null && 그줄.세트 > 1 && k in 0 until 그줄.세트) {
                                            val 뺀 = 그줄.목표(k); val 뺀휴 = 그줄.휴식(k)
                                            줄고침(j) { it.세트빼기(k) }
                                            발자취.적기("세트 지움 · ${그줄.이름} ${k + 1}세트")
                                            상태.알림.되돌림("루세트지움", { n -> if (n > 1) "세트 ${n}개를 지웠습니다" else "${그줄.이름} ${k + 1}세트를 지웠습니다" }) {
                                                상태.바꿈 { dd -> dd.루틴바꿈(rid) { x ->
                                                    val 둔 = x.종목.getOrNull(j)
                                                    if (둔 == null || 둔.이름 != 그줄.이름 || 둔.종id != 그줄.종id || 둔.플랜id != null) x
                                                    else x.copy(종목 = x.종목.mapIndexed { q, y -> if (q == j) 루세트끼움(y, k, 뺀, 뺀휴) else y })
                                                } }
                                            }
                                        }
                                    })
                            }
                        }
                        if (끝단추보임 && r.종목.isNotEmpty()) 종목추가단추(Modifier) { 손댐(); 넣기열림 = true }
                    }
                    // 이 루틴 지우기 — 한 번 더 누르면 지운다 (시안 '루틴지움') · 지운 뒤 [되돌리기]
                    버튼(
                        if (지움확인) "한 번 더 누르면 지웁니다" else "이 루틴 지우기",
                        {
                            초점.clearFocus()
                            if (!지움확인) { 지움확인 = true; return@버튼 }
                            지움확인 = false
                            루틴지우기(상태, rid)
                        },
                        Modifier.fillMaxWidth().padding(top = 간격.보통), 작게 = true, 글색 = c.나쁨,
                    )
                }
            }
        }
        끌기이름표(끌)
    }

    if (넣기열림) {
        val 이름 = r.이름
        종목넣기시트(
            상태, "${이름}에 넣기",
            개수 = { 키 -> 상태.d.루틴(rid)?.종목?.count { it.플랜id == null && it.열쇠 == 키 } ?: 0 },
            넣기 = { 키 -> 상태.바꿈 { dd -> dd.루틴바꿈(rid) { x -> x.copy(종목 = x.종목 + dd.루틴줄(키)) } } },
            빼기 = { 키 ->
                val 지금 = 상태.d.루틴(rid)
                val j = 지금?.종목?.indexOfLast { it.플랜id == null && it.열쇠 == 키 } ?: -1
                if (j >= 0) { 상태.바꿈 { dd -> dd.루틴바꿈(rid) { x -> 루줄빼기(x, j) } }; 펼침바꿈 { 펼침뺌(it, j) } }
            },
            닫기 = { 넣기열림 = false },
            플랜개수 = { pid -> 상태.d.루틴(rid)?.종목?.count { it.플랜id == pid } ?: 0 },
            플랜넣기 = { pid -> 상태.바꿈 { dd ->
                val p = dd.플랜들.firstOrNull { it.id == pid }
                if (p == null) dd else dd.루틴바꿈(rid) { x -> x.copy(종목 = x.종목 + 플랜줄(dd, p)) }
            } },
            플랜빼기 = { pid ->
                val j = 상태.d.루틴(rid)?.종목?.indexOfLast { it.플랜id == pid } ?: -1
                if (j >= 0) { 상태.바꿈 { dd -> dd.루틴바꿈(rid) { x -> 루줄빼기(x, j) } }; 펼침바꿈 { 펼침뺌(it, j) } }
            },
        )
    }
}

/** 루틴 지우기 — 그 루틴이 깔린 예정 · 고정을 지우고 순서를 다시 맞춘다 (시안 '루틴지움'). [되돌리기] 로 자리 · 예정까지 되살린다 */
private fun 루틴지우기(상태: 앱상태, rid: String) {
    val 전 = 상태.d
    val i = 전.루틴들.indexOfFirst { it.id == rid }
    if (i < 0) return
    val r = 전.루틴들[i]
    상태.바꿈 { dd ->
        dd.copy(
            루틴들 = dd.루틴들.filter { it.id != rid },
            예정 = dd.예정.filterValues { it != rid },
            예정고정 = dd.예정고정.filterValues { it != rid },
        ).예정맞추기(상태.오늘)
    }
    루틴기억.펼침.remove(rid)
    루틴기억.열린.value = null
    발자취.적기("루틴 지움 · ${r.이름}")
    상태.알림.되돌림("루틴지움", { n -> if (n > 1) "루틴 ${n}개를 지웠습니다" else "${조사(r.이름, "을", "를")} 지웠습니다" }) {
        상태.바꿈 { dd ->
            if (dd.루틴들.any { it.id == rid }) dd
            else {
                val l = dd.루틴들.toMutableList().also { it.add(min(i, it.size), r) }
                // 예정은 지우기 전 그대로 (그사이 다시 깔린 것을 지운 루틴 자리로 되돌린다)
                dd.copy(루틴들 = l, 예정 = 전.예정, 예정고정 = 전.예정고정).예정맞추기(상태.오늘)
            }
        }
    }
}

/** [+ 종목 추가] — 파란 넓은 단추 (시안 a8es `.버튼.주.넓.루추가`) */
@Composable
private fun 종목추가단추(modifier: Modifier, on누름: () -> Unit) {
    val 판 = remember루톡(true)
    버튼("종목 추가", { 판.톡(); 발자취.적기("종목 넣기 시트 열기"); on누름() }, modifier.fillMaxWidth().루톡(판), 주요 = true, 작게 = true, 그림 = 아이콘.더하기)
}

/**
 * 맨 위 띠 (시안 401d `루틴상세띠`) — [‹ 루틴][이름 칸 + 흐린 '예상 N분 · N세트'][자동 스위치].
 * 이름 칸은 띠 위에서도 고치는 칸으로 읽히게 흰(면) 바탕. 이름은 칸을 떠날 때(완료 · 다른 곳 누름 · 화면 나감) 넣는다
 */
@Composable
private fun 루틴상세띠(상태: 앱상태, r: 루틴, 닫기: () -> Unit, 손댐: () -> Unit) {
    val c = Local색.current
    val 실 = 상태.d.플랜줄채움(r)
    var 이름 by remember(r.id) { mutableStateOf(r.이름) }
    var 고치는중 by remember(r.id) { mutableStateOf(false) }
    // 밖에서 이름이 바뀌면(되돌리기 등) 고치는 중이 아닐 때만 따라간다
    LaunchedEffect(r.이름) { if (!고치는중) 이름 = r.이름 }
    val 최신 by rememberUpdatedState(이름)
    fun 넣기(새: String) {
        val t = 새.trim()
        if (t.isEmpty() || t == 상태.d.루틴(r.id)?.이름) return
        // 10-02: 이름이 실제로 바뀌었을 때만 센다 (업적 2-45)
        상태.바꿈 { dd -> dd.루틴바꿈(r.id) { x -> x.copy(이름 = t) }.세기더함(세기이름.루틴이름바꿈) }
        발자취.적기("루틴 이름 · $t")
    }
    DisposableEffect(r.id) { onDispose { if (고치는중) 넣기(최신) } }
    val 초점 = LocalFocusManager.current
    Row(
        Modifier.fillMaxWidth().heightIn(min = 높이.보통).background(c.강조)
            .padding(horizontal = 간격.보통, vertical = 부품치수.띠세로여백),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(간격.좁게),
    ) {
        // [‹ 루틴] — 띠 위 흰 단추 (시안 `.작은흰`)
        Box(
            Modifier.height(높이.아주낮게).clip(RoundedCornerShape(모서리.작게)).background(c.강조글)
                .눌림 { 손댐(); 닫기() }.padding(horizontal = 간격.좁게),
            contentAlignment = Alignment.Center,
        ) { 글("‹ 루틴", 크기값 = 크기.버튼, 색 = c.강조, 굵기 = FontWeight.Bold) }
        // 이름 칸 — 이름이 늘고(최소 64) 흐린 예상 글은 남는 만큼만 (시안 `.루이름칸` flex)
        앞늘림배치(
            Modifier.weight(1f).height(높이.낮게).clip(RoundedCornerShape(모서리.작게)).background(c.면)
                .padding(horizontal = 간격.좁게),
            최소 = 루틴값.이름칸최소,
            앞 = { BasicTextField(
                value = 이름,
                onValueChange = { 이름 = it.replace("\n", "") },
                singleLine = true,
                textStyle = 글꼴.보통(크기.본문, FontWeight.Bold).copy(color = c.글),
                cursorBrush = SolidColor(c.강조),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { 초점.clearFocus() }),
                modifier = Modifier.fillMaxWidth()
                    .semantics { contentDescription = "루틴 이름" }
                    .onFocusChanged { f ->
                        if (f.isFocused) { 고치는중 = true; 손댐() }
                        else if (고치는중) { 고치는중 = false; 넣기(이름); 이름 = 상태.d.루틴(r.id)?.이름 ?: 이름 }
                    },
            ) },
            뒤 = {
                if (!r.휴식일) Text(
                    "예상 ${시간글(예상초(실))} · ${총세트(실)}세트", style = 글꼴.보통(크기.작게).copy(fontFeatureSettings = "tnum"),
                    color = c.옅음, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false,
                )
            },
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
            글("자동", 크기값 = 크기.작게, 색 = c.강조글, 굵기 = FontWeight.Bold)
            띠스위치(r.자동생성) { v ->
                손댐()
                상태.바꿈 { dd -> dd.루틴바꿈(r.id) { x -> x.copy(자동생성 = v) }.예정초기화(상태.오늘) }
                발자취.적기("${r.이름} 자동 ${if (v) "켬" else "끔"}")
            }
        }
    }
}

/** [앞] 은 남는 자리를 다 쓰고(최소 [최소]), [뒤] 는 제 폭만큼 — 모자라면 뒤가 줄어든다. 사이 8 · 세로 가운데 */
@Composable
private fun 앞늘림배치(modifier: Modifier, 최소: Dp, 앞: @Composable () -> Unit, 뒤: @Composable () -> Unit) {
    Layout(contents = listOf(앞, 뒤), modifier = modifier) { 묶음, cons ->
        val 틈 = 간격.좁게.roundToPx()
        val 폭 = if (cons.hasBoundedWidth) cons.maxWidth else 최소.roundToPx() * 4
        val 풀 = cons.copy(minWidth = 0, minHeight = 0)
        val 뒤p = 묶음[1].firstOrNull()?.measure(풀.copy(maxWidth = max(0, 폭 - 최소.roundToPx() - 틈)))
        val 뒤몫 = if (뒤p == null || 뒤p.width == 0) 0 else 뒤p.width + 틈
        val 앞폭 = max(0, 폭 - 뒤몫)
        val 앞p = 묶음[0].first().measure(풀.copy(minWidth = 앞폭, maxWidth = 앞폭))
        val 높 = maxOf(앞p.height, 뒤p?.height ?: 0, cons.minHeight)
        layout(폭, 높) {
            앞p.place(0, (높 - 앞p.height) / 2)
            if (뒤p != null) 뒤p.place(앞폭 + 틈, (높 - 뒤p.height) / 2)
        }
    }
}

/**
 * 띠 위 스위치 (시안 `.루띠 .스위치`) — 켬: 강조글 바탕 · 강조 손잡이 / 끔: 강조글 테두리 · 강조글 손잡이.
 * 시안은 끔 바탕을 강조글 35% 로 섞지만 색을 깎지 않으려고(U1-3) 테두리로 그린다
 */
@Composable
private fun 띠스위치(켜짐: Boolean, onChange: (Boolean) -> Unit) {
    val c = Local색.current
    val 폭 = 루틴값.스위치폭; val 안 = 간격.아주좁게; val 손잡이 = 루틴값.스위치손잡이
    val 자리 by animateDpAsState(if (켜짐) 폭 - 안 * 2 - 손잡이 else 0.dp, tween(움직임.스위치), label = "띠스위치")
    val 바탕 by animateColorAsState(if (켜짐) c.강조글 else c.강조, tween(움직임.스위치), label = "띠스위치바탕")
    val 손잡이색 by animateColorAsState(if (켜짐) c.강조 else c.강조글, tween(움직임.스위치), label = "띠스위치손잡이")
    val 손 = remember { MutableInteractionSource() }
    Box(
        Modifier.width(폭).height(높이.아주낮게).clip(CircleShape).background(바탕)
            .border(선굵기.보통, c.강조글, CircleShape)
            .semantics { contentDescription = if (켜짐) "자동 켜짐" else "자동 꺼짐" }
            .눌림손(손) { onChange(!켜짐) }
            .padding(안),
        contentAlignment = Alignment.CenterStart,
    ) { Box(Modifier.offset(x = 자리).size(손잡이).clip(CircleShape).background(손잡이색)) }
}

/** 상자 머리 (시안 `.루머리`) — 강조옅음 바탕 · 높이 40 이상 · 여백 4 4 4 12. 모서리는 상자가 깎는다 */
@Composable
private fun 상자머리(content: @Composable RowScope.() -> Unit) {
    val c = Local색.current
    Row(
        Modifier.fillMaxWidth().heightIn(min = 높이.보통)
            .background(c.강조옅음)
            .padding(start = 간격.보통, end = 간격.아주좁게, top = 간격.아주좁게, bottom = 간격.아주좁게),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(간격.좁게),
        content = content,
    )
}

/**
 * 꾹 눌러 끌기 — 상자 머리의 이름 줄에 단다(✕ 단추에서는 시작하지 않는다 · 시안 data-drag="종목줄").
 * [시작] 은 손가락의 화면 y. 자리는 이 줄이 스스로 잰다
 */
@Composable
private fun 끌기손(끌: 세로끌기, rid: String, j: Int, 놓기: () -> Unit, 시작: (Float) -> Unit): Modifier {
    val 시작최신 by rememberUpdatedState(시작)
    val 놓기최신 by rememberUpdatedState(놓기)
    var 줄틀 by remember { mutableStateOf(Rect.Zero) }
    val 틀최신 by rememberUpdatedState(줄틀)
    return Modifier
        .onGloballyPositioned { if (끌.원 == null) 줄틀 = it.boundsInRoot() }
        .pointerInput(끌, rid, j) {
            detectDragGesturesAfterLongPress(
                onDragStart = { p -> 시작최신(틀최신.top + p.y) },
                onDrag = { ch, 양 -> ch.consume(); 끌.y += 양.y },
                onDragEnd = { 놓기최신() },
                onDragCancel = { 끌.끝() },
            )
        }
}

/** ✕ — 루틴에서 이 종목 빼기 (시안 `.닫기` ✕ · 강조) */
@Composable
private fun 빼기단추(이름: String, on누름: () -> Unit) {
    아이콘버튼(아이콘.닫기, "$이름 빼기", on누름, 칠함 = false, 색 = Local색.current.강조, 크기칸 = 높이.낮게)
}

/**
 * 보통 종목 상자 (시안 `루틴종목상자` · v18 D ① 접힘 · h627 세트 줄 · 9kht 높이 28).
 * 접힘 = 머리 한 줄 [이름(+번호) · N세트 ▾][✕] / 펼침 = 머리 + [세트 | 무게 kg | 횟수 | 휴식] + 세트 줄 + [+ 세트]
 */
@Composable
private fun 종목상자(
    상태: 앱상태, e: 루틴종목, 펼침: Boolean, modifier: Modifier, 표시: Int, 끌기길: Modifier,
    on펼침: () -> Unit, on빼기: () -> Unit, on고침: ((루틴종목) -> 루틴종목) -> Unit, on세트지움: (Int) -> Unit,
) {
    val c = Local색.current
    val 모양 = RoundedCornerShape(모서리.보통)
    Column(
        modifier.fillMaxWidth().clip(모양).background(c.면).border(선굵기.보통, c.속선, 모양).놓을선(표시, c.강조),
    ) {
        상자머리 {
            Row(
                Modifier.weight(1f).heightIn(min = 높이.낮게).then(끌기길)
                    .semantics(mergeDescendants = true) { contentDescription = "${e.이름} ${e.세트}세트 ${if (펼침) "접기" else "펼치기"}" }
                    .눌림(on펼침),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
                    종목이름딱지(상태.d, e.종id, e.이름, Modifier.weight(1f, fill = false))
                    Text("· ${e.세트}세트", style = 글꼴.보통(크기.조금작게).copy(fontFeatureSettings = "tnum"), color = c.흐림, maxLines = 1, softWrap = false)
                }
                펼침단추(펼침, on펼침)
            }
            빼기단추(e.이름, on빼기)
        }
        if (펼침) {
            val 폭 = 상태.d.설정.무게폭
            val 하나 = e.세트 <= 1
            Column(
                Modifier.fillMaxWidth().padding(start = 간격.아주좁게, end = 간격.아주좁게, top = 간격.아주좁게, bottom = 간격.좁게),
                verticalArrangement = Arrangement.spacedBy(간격.아주좁게),
            ) {
                세트칸줄(
                    { 글("세트", 크기값 = 크기.작게, 색 = c.옅음, 가운데 = true) },
                    { 글("무게 kg", 크기값 = 크기.작게, 색 = c.옅음, 가운데 = true) },
                    { 글("횟수", 크기값 = 크기.작게, 색 = c.옅음, 가운데 = true) },
                    { 글("휴식", 크기값 = 크기.작게, 색 = c.옅음, 가운데 = true) },
                    { },
                )
                for (k in 0 until e.세트) {
                    val v = e.목표(k)
                    val t = e.휴식(k)
                    세트칸줄(
                        { Text("${k + 1}", style = 글꼴.보통(크기.조금작게, FontWeight.Bold).copy(fontFeatureSettings = "tnum"), color = c.글, textAlign = TextAlign.Center, maxLines = 1) },
                        {
                            루값칸(무게글(v.w), "무게",
                                빼기 = { on고침 { it.세트고침(k, w = 무게반올림(max(0.0, it.목표(k).w - 폭))) } },
                                더하기 = { on고침 { it.세트고침(k, w = 무게반올림(it.목표(k).w + 폭)) } },
                                입력 = { s -> 루무게읽기(s)?.let { x -> on고침 { it.세트고침(k, w = x) } } },
                                소수 = true, 뺄수있음 = v.w > 0.0)
                        },
                        {
                            루값칸("${v.r}", "횟수",
                                빼기 = { on고침 { it.세트고침(k, r = max(1, it.목표(k).r - 1)) } },
                                더하기 = { on고침 { it.세트고침(k, r = it.목표(k).r + 1) } },
                                입력 = { s -> 루횟수읽기(s)?.let { x -> on고침 { it.세트고침(k, r = x) } } },
                                뺄수있음 = v.r > 1)
                        },
                        {
                            루값칸(분초(t), "휴식",
                                빼기 = { on고침 { it.세트고침(k, t = 휴식한칸(it.휴식(k), -1)) } },
                                더하기 = { on고침 { it.세트고침(k, t = 휴식한칸(it.휴식(k), 1)) } },
                                입력 = null)
                        },
                        {
                            // 세트 휴지통 — 묻지 않고 지우고 [되돌리기]. 하나뿐이면 꺼 둔다(종목째 빼기는 ✕)
                            Box(
                                Modifier.size(높이.아주낮게).clip(RoundedCornerShape(모서리.작게))
                                    .then(if (하나) Modifier else Modifier.눌림 { on세트지움(k) }),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(아이콘.지우기, "${k + 1}세트 지우기", Modifier.size(루틴값.휴지통그림),
                                    tint = if (하나) c.선 else c.옅음)
                            }
                        },
                    )
                }
                val 판 = remember루톡(true)
                버튼("세트", { 판.톡(); 발자취.적기("세트 더하기 · ${e.이름}"); on고침 { it.세트더하기() } },
                    Modifier.fillMaxWidth().루톡(판), 낮게 = true, 그림 = 아이콘.더하기)
            }
        }
    }
}

/** 세트 줄 칸 나눔 (시안 `.세트머리.루` · `.루세트` — 16 | 83fr | 70fr | 79fr | 28, 틈 4) */
@Composable
private fun 세트칸줄(
    번호: @Composable () -> Unit, 무게: @Composable () -> Unit, 횟수: @Composable () -> Unit,
    휴식: @Composable () -> Unit, 끝: @Composable () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
        Box(Modifier.width(루틴값.번호칸), contentAlignment = Alignment.Center) { 번호() }
        Box(Modifier.weight(루틴값.무게몫), contentAlignment = Alignment.Center) { 무게() }
        Box(Modifier.weight(루틴값.횟수몫), contentAlignment = Alignment.Center) { 횟수() }
        Box(Modifier.weight(루틴값.휴식몫), contentAlignment = Alignment.Center) { 휴식() }
        Box(Modifier.width(높이.아주낮게), contentAlignment = Alignment.Center) { 끝() }
    }
}

/**
 * − 값 ＋ 칸 (시안 `루값` · `.값칸` — 높이 28 · 양끝 24). 무게 · 횟수는 가운데를 눌러 바로 쳐 넣는다(칸을 떠날 때 넣음), 휴식은 글만.
 * 공용 [값칸] 에는 쳐 넣기 · ＋ 볼록 / − 오목이 없어 여기 따로 둔다 (합칠 때 공용으로)
 */
@Composable
private fun 루값칸(
    값글: String, 이름: String, 빼기: () -> Unit, 더하기: () -> Unit, 입력: ((String) -> Unit)?,
    소수: Boolean = false, 뺄수있음: Boolean = true,
) {
    val c = Local색.current
    val 모양 = RoundedCornerShape(모서리.작게)
    val 초점 = LocalFocusManager.current
    Row(
        Modifier.fillMaxWidth().height(높이.아주낮게).clip(모양).border(선굵기.보통, c.속선, 모양),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // − ＋ 를 누르면 먼저 쳐 넣던 값을 넣고(초점을 놓아서) 그 값에서 한 칸
        톡단추(아이콘.빼기, "$이름 빼기", false, 뺄수있음) { 초점.clearFocus(); 빼기() }
        if (입력 != null) {
            var 글자 by remember(값글) { mutableStateOf(값글) }
            var 만짐 by remember(값글) { mutableStateOf(false) }
            BasicTextField(
                value = 글자,
                onValueChange = { v -> 만짐 = true; 글자 = v.filter { it.isDigit() || (소수 && (it == '.' || it == ',')) }.take(6) },
                singleLine = true,
                textStyle = 글꼴.보통(크기.본문, FontWeight.Bold).copy(color = c.글, textAlign = TextAlign.Center, fontFeatureSettings = "tnum"),
                cursorBrush = SolidColor(c.강조),
                keyboardOptions = KeyboardOptions(keyboardType = if (소수) KeyboardType.Decimal else KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { 초점.clearFocus() }),
                modifier = Modifier.weight(1f).semantics { contentDescription = 이름 }
                    .onFocusChanged { f -> if (!f.isFocused && 만짐) { 만짐 = false; if (글자.isNotBlank()) 입력(글자); 글자 = 값글 } },
            )
        } else Text(
            값글, Modifier.weight(1f), style = 글꼴.보통(크기.조금작게, FontWeight.Bold).copy(fontFeatureSettings = "tnum"),
            color = c.글, maxLines = 1, softWrap = false, textAlign = TextAlign.Center,
        )
        톡단추(아이콘.더하기, "$이름 더하기", true, true) { 초점.clearFocus(); 더하기() }
    }
}

/** 값칸 양끝 − ＋ (폭 24 · 강조) — 누르면 ＋ 볼록 / − 오목 (시안 ls4v) */
@Composable
private fun 톡단추(그림: androidx.compose.ui.graphics.vector.ImageVector, 설명: String, 볼록: Boolean, 켬: Boolean, on누름: () -> Unit) {
    val c = Local색.current
    val 판 = remember루톡(볼록)
    Box(
        Modifier.width(루틴값.값단추).fillMaxHeight().루톡(판)
            .then(if (켬) Modifier.눌림 { 판.톡(); on누름() } else Modifier),
        contentAlignment = Alignment.Center,
    ) { Icon(그림, 설명, Modifier.size(루틴값.값단추그림), tint = if (켬) c.강조 else c.옅음) }
}

/**
 * 플랜 상자 (시안 c1gz · v19 C ⑤) — 한 줄 [이름[플랜] · N세트 · N회차 · 게이지 · %][✕].
 * 무게 · 횟수는 플랜이 정한다 — 손으로 고치지 않는다. 줄을 누르면 플랜 고치기 (플랜이 없어졌으면 누를 수 없다)
 */
@Composable
private fun 플랜상자(
    상태: 앱상태, e: 루틴종목, p: 플랜?, modifier: Modifier, 표시: Int, 끌기길: Modifier,
    on고치기: () -> Unit, on빼기: () -> Unit,
) {
    val c = Local색.current
    val d = 상태.d
    val 모양 = RoundedCornerShape(모서리.보통)
    val 세트수 = d.플랜줄채움(e).세트
    val 회 = p?.다음회(d.몸, d.향상기록들)
    val 비율 = p?.let { 플랜달성률(it) } ?: 0.0
    val 퍼 = (비율 * 100).roundToInt()
    Column(modifier.fillMaxWidth().clip(모양).border(선굵기.보통, c.속선, 모양).놓을선(표시, c.강조)) {
        상자머리 {
            플랜줄배치(
                Modifier.weight(1f).heightIn(min = 높이.낮게).then(끌기길)
                    .semantics(mergeDescendants = true) { contentDescription = "${e.이름} 플랜 고치기 · ${세트수}세트 · 달성률 ${퍼}%" }
                    .then(if (p != null) Modifier.눌림(on고치기) else Modifier),
                이름 = { 이름딱지(e.이름, 플랜 = true) },
                세트 = { 루플랜글("${세트수}세트") },
                회차 = { 루플랜글(if (회 != null) "${회.회}회차" else "목표 달성") },
                게이지 = { 진행막대(비율.toFloat(), Modifier.fillMaxWidth()) },
                퍼 = { Text("${퍼}%", style = 글꼴.보통(크기.작게, FontWeight.Bold).copy(fontFeatureSettings = "tnum"), color = c.흐림, maxLines = 1, softWrap = false) },
            )
            빼기단추(e.이름, on빼기)
        }
    }
}

@Composable
private fun 루플랜글(글자: String) {
    Text(글자, style = 글꼴.보통(크기.조금작게).copy(fontFeatureSettings = "tnum"), color = Local색.current.흐림, maxLines = 1, softWrap = false)
}

/**
 * 플랜 줄 배치 (시안 `.루플랜줄`) — 이름이 먼저: 세트 · 회차 · % 는 제 폭 그대로, 게이지는 남는 자리(최소 16),
 * 이름은 그 나머지 안에서 줄어든다(… 로). 틈 = 이름 뒤 8 · 나머지 4
 */
@Composable
private fun 플랜줄배치(
    modifier: Modifier,
    이름: @Composable () -> Unit, 세트: @Composable () -> Unit, 회차: @Composable () -> Unit,
    게이지: @Composable () -> Unit, 퍼: @Composable () -> Unit,
) {
    Layout(contents = listOf(이름, 세트, 회차, 게이지, 퍼), modifier = modifier) { 묶음, cons ->
        val 큰틈 = 간격.좁게.roundToPx(); val 틈 = 간격.아주좁게.roundToPx(); val 게최소 = 루틴값.게이지최소.roundToPx()
        val 풀 = cons.copy(minWidth = 0, minHeight = 0)
        val 세트p = 묶음[1].first().measure(풀)
        val 회p = 묶음[2].first().measure(풀)
        val 퍼p = 묶음[4].first().measure(풀)
        val 폭 = if (cons.hasBoundedWidth) cons.maxWidth else 세트p.width + 회p.width + 퍼p.width + 게최소 * 8
        val 고정 = 세트p.width + 회p.width + 퍼p.width + 큰틈 + 틈 * 3
        val 이름p = 묶음[0].first().measure(풀.copy(maxWidth = max(0, 폭 - 고정 - 게최소)))
        val 게폭 = max(게최소, 폭 - 고정 - 이름p.width)
        val 게p = 묶음[3].first().measure(풀.copy(minWidth = 게폭, maxWidth = 게폭))
        val 높 = maxOf(이름p.height, 세트p.height, 회p.height, 게p.height, 퍼p.height, cons.minHeight)
        layout(폭, 높) {
            var x = 0
            fun 놓(p: androidx.compose.ui.layout.Placeable, 뒤: Int) { p.place(x, (높 - p.height) / 2); x += p.width + 뒤 }
            놓(이름p, 큰틈); 놓(세트p, 틈); 놓(회p, 틈); 놓(게p, 틈); 놓(퍼p, 0)
        }
    }
}

// ═════════════════════ 끌기 (세로 목록 — 루틴 카드 · 루틴 안 종목) ═════════════════════

/**
 * 세로 목록 꾹 눌러 끌기 (시안 `끌` — '루틴' · '종목줄'). 놓을 곳 = 손가락 아래 칸의 위 반(앞) · 아래 반(뒤).
 * 칸 자리는 끌기 **전에** 잰 것만 쓰고, 넘긴 만큼만 더해 견준다 (U5-6)
 */
@Stable
private class 세로끌기 {
    var 원 by mutableStateOf<Int?>(null)
        private set
    var 이름 by mutableStateOf("")
        private set
    var y by mutableFloatStateOf(0f)
    var 틀 by mutableStateOf(Rect.Zero)
    /** 칸 자리 — 그리기에는 안 쓴다(끌기 시작할 때만) → 상태가 아닌 그냥 표 */
    val 자리 = HashMap<Int, Rect>()
    private var 굳은: Map<Int, Rect> = emptyMap()
    private var 시작넘김 = 0

    /** [수] = 지금 줄 수 — 그보다 큰 번호의 옛 자리(지운 줄)는 버린다 */
    fun 시작(i: Int, 이름: String, 손y: Float, 넘김: Int, 수: Int) {
        굳은 = 자리.filterKeys { it < 수 }; 시작넘김 = 넘김
        this.이름 = 이름; y = 손y; 원 = i
    }
    fun 끝() { 원 = null }
    /** (대상 번호, 뒤에) — 손가락 아래 칸이 없으면 null */
    fun 대상(넘김: Int): Pair<Int, Boolean>? {
        if (원 == null) return null
        val dy = (넘김 - 시작넘김).toFloat()
        return 굳은.entries.firstOrNull { (_, r) -> y >= r.top - dy && y < r.bottom - dy }?.let { (k, r) -> k to (y > r.center.y - dy) }
    }
    /** 이 칸에 그릴 놓을 표시 — 0 위 선 · 2 아래 선 · -1 없음 */
    fun 표시(i: Int, 넘김: Int): Int {
        val t = 대상(넘김) ?: return -1
        if (t.first != i || t.first == 원) return -1
        return if (t.second) 2 else 0
    }
}

/** 화면 위 · 아래 끝 가까이 끌면 저절로 넘긴다 */
@Composable
private fun 끌기자동넘김(끌: 세로끌기, 넘김: ScrollState) {
    val 밀도 = LocalDensity.current
    val 끝px = with(밀도) { 부품치수.끌기끝.toPx() }
    val 한번 = with(밀도) { 부품치수.끌기넘김.toPx() }
    LaunchedEffect(끌.원 != null) {
        while (끌.원 != null) {
            val y = 끌.y
            if (y < 끌.틀.top + 끝px) 넘김.scrollBy(-한번) else if (y > 끌.틀.bottom - 끝px) 넘김.scrollBy(한번)
            delay(16)
        }
    }
}

/** 끄는 동안 손가락을 따라오는 이름표 (강조 알약 · 1-3 '끌 때 이름표') */
@Composable
private fun 끌기이름표(끌: 세로끌기) {
    if (끌.원 == null) return
    val c = Local색.current
    val 밀도 = LocalDensity.current
    Box(
        Modifier
            .offset(y = with(밀도) { (끌.y - 끌.틀.top).toDp() } - 루틴값.이름표위)
            .padding(start = 루틴값.이름표옆)
            .clip(CircleShape)
            .background(c.강조)
            .padding(horizontal = 간격.넓게, vertical = 간격.좁게),
    ) { 글(끌.이름, 크기값 = 크기.버튼, 색 = c.강조글, 굵기 = FontWeight.Bold) }
}

/** 놓을 자리 — 칸 위 · 아래 끝에 3 강조 선 (U5-5) */
private fun Modifier.놓을선(표시: Int, 색: androidx.compose.ui.graphics.Color): Modifier =
    if (표시 < 0) this else this.drawWithContent {
        drawContent()
        val h = 부품치수.놓을선.toPx()
        drawRect(색, topLeft = Offset(0f, if (표시 == 2) size.height - h else 0f), size = Size(size.width, h))
    }

// ═════════════════════ ＋ 볼록 · − 오목 (시안 ls4v) ═════════════════════

/**
 * 누르면 ＋ 는 살짝 커졌다가, − 는 살짝 작아졌다가 돌아온다 — 0.2초 (45% 에서 끝값).
 * 끝값 = 볼록 min(1.12, 1 + 12/큰 변) · 오목 max(0.88, 1 − 12/큰 변) — 넓은 단추는 12 안쪽으로만.
 * 넣기 시트도 쓴다. 합칠 때 공용(Motion.kt)으로 옮긴다
 */
@Stable
internal class 루톡판(private val 볼록: Boolean, private val 범위: CoroutineScope, private val 넘침px: Float) {
    val 배 = Animatable(1f)
    var 큰변 = 0
    fun 톡() {
        val 크 = max(큰변, 1).toFloat()
        val 끝 = if (볼록) min(루틴값.볼록, 1f + 넘침px / 크) else max(루틴값.오목, 1f - 넘침px / 크)
        범위.launch {
            배.snapTo(1f)
            배.animateTo(끝, tween((루틴값.톡시간 * 0.45f).roundToInt()))
            배.animateTo(1f, tween((루틴값.톡시간 * 0.55f).roundToInt()))
        }
    }
}

@Composable
internal fun remember루톡(볼록: Boolean): 루톡판 {
    val 범위 = rememberCoroutineScope()
    val 넘침 = with(LocalDensity.current) { 루틴값.톡넘침.toPx() }
    return remember(볼록) { 루톡판(볼록, 범위, 넘침) }
}

internal fun Modifier.루톡(판: 루톡판): Modifier =
    this.onSizeChanged { 판.큰변 = max(it.width, it.height) }.graphicsLayer { scaleX = 판.배.value; scaleY = 판.배.value }

/** 루틴 화면 값 (시안 CSS) — Theme 에 없는 것만. 합칠 때 Theme.kt 로 옮긴다 */
internal object 루틴값 {
    val 자동띠 = 4.dp           // .루틴카드.자동 border-left 4
    val 카드줄틈 = 2.dp         // .루틴카드 gap 2
    val 이름칸최소 = 64.dp      // .루이름칸 input min-width 5em
    val 스위치폭 = 46.dp        // 공용 스위치와 같은 값
    val 스위치손잡이 = 20.dp    // 높이 28 − 안쪽 4 × 2
    val 번호칸 = 16.dp          // 세트 줄 첫 칸
    const val 무게몫 = 83f
    const val 횟수몫 = 70f
    const val 휴식몫 = 79f
    val 값단추 = 24.dp          // .루세트 .값칸 button (좁은 폰)
    val 값단추그림 = 18.dp      // U3-6 − ＋ 는 18
    val 휴지통그림 = 16.dp      // .루지움 svg 16
    val 게이지최소 = 16.dp      // .루플랜게 min-width
    val 이름표위 = 36.dp        // 끄는 이름표 — 손가락 위
    val 이름표옆 = 40.dp
    val 톡넘침 = 12.dp
    const val 톡시간 = 200
    const val 볼록 = 1.12f
    const val 오목 = 0.88f
}

// ═════════════════════ 순수 계산 (시험: ui/RoutineTest.kt) ═════════════════════

/** 자동생성 루틴의 차례 'N번째' — 목록 차례로 1, 2 … (휴식일도 센다 · 시안 `++순`). 수동 루틴은 없음 */
internal fun 자동번호(루틴들: List<루틴>): Map<String, Int> {
    var n = 0
    return 루틴들.filter { it.자동생성 }.associate { it.id to ++n }
}

/** 줄 하나 빼기 — 범위 밖이면 그대로. 혼자 남은 슈퍼세트 묶음은 푼다 */
internal fun 루줄빼기(r: 루틴, j: Int): 루틴 =
    if (j !in r.종목.indices) r else r.copy(종목 = r.종목.filterIndexed { k, _ -> k != j }).묶음정리()

/** 뺀 줄 되돌리기 — 그 자리(줄 수보다 크면 맨 끝)에 끼운다. 슈퍼세트 묶음은 되살리지 않는다 */
internal fun 루줄끼움(r: 루틴, j: Int, e: 루틴종목): 루틴 {
    val l = r.종목.toMutableList()
    l.add(j.coerceIn(0, l.size), e.copy(슈퍼 = null))
    return r.copy(종목 = l)
}

/** 지운 세트 되돌리기 — k 자리(세트 수보다 크면 맨 끝)에 그 무게 · 횟수 · 휴식으로 끼운다 */
internal fun 루세트끼움(e: 루틴종목, k: Int, s: 세트, 휴: Int): 루틴종목 {
    val 값 = (0 until e.세트).map { e.목표(it) }.toMutableList()
    val 휴들 = (0 until e.세트).map { e.휴식(it) }.toMutableList()
    val i = k.coerceIn(0, 값.size)
    값.add(i, s); 휴들.add(i, 휴)
    return e.copy(세트 = 값.size, 세트값 = 값, 휴식값 = 휴들, 무게 = 값[0].w, 횟수 = 값[0].r, 휴식 = 휴들[0])
}

/**
 * 끌어 옮긴 뒤의 차례 — 새 차례대로 옛 번호를 늘어놓은 것. [루틴.종목옮기기] 와 똑같이 움직인다(슈퍼세트 묶음은 통째로).
 * 줄마다 이름 대신 번호를 넣어 같은 함수를 돌린다 — 같은 종목이 둘이어도 섞이지 않는다
 */
internal fun 루틴옮김표(r: 루틴, from: Int, to: Int, 뒤에: Boolean): List<Int> {
    if (from !in r.종목.indices || to !in r.종목.indices) return r.종목.indices.toList()
    val 표 = r.copy(종목 = r.종목.mapIndexed { i, e -> e.copy(이름 = "$i") })
    return 표.종목옮기기(from, to, 뒤에).종목.map { it.이름.toInt() }
}

/** 펼친 번호들을 새 차례로 — [차례] = 새 자리마다 옛 번호 */
internal fun 펼침맞춤(펼침: Set<Int>, 차례: List<Int>): Set<Int> =
    차례.withIndex().filter { (_, 옛) -> 옛 in 펼침 }.map { it.index }.toSet()

/** j 번 줄을 뺐을 때 — 그 줄은 빠지고 뒤 번호는 하나씩 당긴다 */
internal fun 펼침뺌(펼침: Set<Int>, j: Int): Set<Int> = 펼침.filter { it != j }.map { if (it > j) it - 1 else it }.toSet()

/** j 자리에 줄을 끼웠을 때 — 그 뒤 번호는 하나씩 민다 (끼운 줄은 접힘) */
internal fun 펼침끼움(펼침: Set<Int>, j: Int): Set<Int> = 펼침.map { if (it >= j) it + 1 else it }.toSet()

/**
 * 끝 [+ 종목 추가] 를 보일까 (시안 mqjf `루끝단추맞춤`) — 끝 단추 없이 맨 아래까지 내렸을 때 위 단추가 화면 밖으로 나가면.
 * [최대] = 지금 넘길 수 있는 양 (끝 단추가 [보임] 이면 그 몫 [끝몫] 이 들어 있다) · [위아래] = 위 단추 아래끝(내용 위에서)
 */
internal fun 끝단추판정(최대: Int, 끝몫: Int, 보임: Boolean, 위아래: Int): Boolean = (최대 - (if (보임) 끝몫 else 0)) > 위아래

/** 달성률 = (지금 − 시작) ÷ (목표 − 시작), 0~1 (시안 v19 C ⑤ `플랜달성률` — 플랜 카드 게이지와 같은 식) */
internal fun 플랜달성률(p: 플랜): Double {
    val 시 = p.시작진행값; val 목 = p.목표진행값; val 현 = p.지금진행값
    return if (목 > 시) ((현 - 시) / (목 - 시)).coerceIn(0.0, 1.0) else 0.0
}

/** 쳐 넣은 무게 — 쉼표도 소수점으로, 0 아래는 0, 0.1 단위. 숫자가 아니면 null */
internal fun 루무게읽기(s: String): Double? = s.trim().replace(',', '.').toDoubleOrNull()?.let { 무게반올림(max(0.0, it)) }

/** 쳐 넣은 횟수 — 반올림, 1 아래는 1. 숫자가 아니면 null */
internal fun 루횟수읽기(s: String): Int? = s.trim().replace(',', '.').toDoubleOrNull()?.let { max(1, it.roundToInt()) }

/**
 * 플랜 한 줄을 루틴종목으로 (09-30) — 다음 회차 처방을 그대로 세트값에 채운다. 맨몸이면 휴식은 2분 (21 문서 6절).
 * 10-01: 측정일이라고 세트를 '측정' 으로 표시하지 않는다 — 측정은 저장할 때 가장 좋은 세트로 잡는다 (Plan.kt 플랜반영)
 */
private fun 플랜줄(d: 앱데이터, p: 플랜): 루틴종목 {
    val 계획 = p.다음회(d.몸, d.향상기록들)
    val 처방 = if (계획 == null) emptyList() else 회처방(p, 계획.목표값, d.설정.무게폭, d.몸, 계획.측정일, 계획.주)
    val 세트값 = 처방세트(처방)
    val 첫 = 세트값.firstOrNull()
    return 루틴종목(
        이름 = p.이름,
        세트 = 세트값.size.coerceAtLeast(1),
        무게 = 첫?.w ?: 0.0,
        횟수 = 첫?.r ?: 10,
        휴식 = if (p.횟수진행) 플랜표.맨몸휴식 else d.설정.기본휴식,
        세트값 = 세트값,
        플랜id = p.id,
    )
}
