@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.slayde.hasenheide.ui

import androidx.activity.compose.BackHandler

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import com.slayde.hasenheide.ui.theme.글꼴
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.slayde.hasenheide.data.루틴
import com.slayde.hasenheide.data.루틴바꿈
import com.slayde.hasenheide.data.루틴합치기
import com.slayde.hasenheide.data.루틴최근향상
import com.slayde.hasenheide.data.루틴옮기기
import com.slayde.hasenheide.data.루틴성장
import com.slayde.hasenheide.data.루틴종목
import com.slayde.hasenheide.data.무게글
import com.slayde.hasenheide.data.무게반올림
import com.slayde.hasenheide.data.묶음정리
import com.slayde.hasenheide.data.분초
import com.slayde.hasenheide.data.슈퍼묶기
import com.slayde.hasenheide.data.슈퍼풀기
import com.slayde.hasenheide.data.시간글
import com.slayde.hasenheide.data.예상초
import com.slayde.hasenheide.data.예정초기화
import com.slayde.hasenheide.data.종목
import com.slayde.hasenheide.data.종목옮기기
import com.slayde.hasenheide.data.지금기준
import com.slayde.hasenheide.data.초읽기
import com.slayde.hasenheide.data.총세트
import com.slayde.hasenheide.data.다음차례
import com.slayde.hasenheide.data.이름추천
import com.slayde.hasenheide.data.요약
import com.slayde.hasenheide.data.휴식
import com.slayde.hasenheide.data.목표
import com.slayde.hasenheide.data.세트빼기
import com.slayde.hasenheide.data.종목1RM
import com.slayde.hasenheide.data.볼륨
import com.slayde.hasenheide.data.세트끼우기
import com.slayde.hasenheide.data.콤마
import com.slayde.hasenheide.data.세트더하기
import com.slayde.hasenheide.data.세트고침
import com.slayde.hasenheide.data.모두무게
import com.slayde.hasenheide.data.모두횟수
import com.slayde.hasenheide.data.모두휴식
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.높이
import com.slayde.hasenheide.ui.theme.모서리
import com.slayde.hasenheide.ui.theme.크기
import kotlin.math.max

/**
 * 끌고 있는 종목 — 꾹 눌러 집고 그대로 끌어서 놓는다 (8-2 · 09-21 메모).
 * y 는 화면 전체 기준 손가락 위치.
 */
private data class 끄는것(val rid: String, val j: Int, val 이름: String, val y: Float)
/** 놓일 곳 — 모드 0 = 앞으로, 1 = 슈퍼세트, 2 = 뒤로 */
private data class 놓을곳(val rid: String, val j: Int, val 모드: Int)

@Composable
fun 루틴화면(상태: 앱상태, 폰: 폰기능) {
    val c = Local색.current
    val d = 상태.d
    val 오늘 = 상태.오늘
    var 열린루틴 by remember { mutableStateOf<String?>(null) }
    var 이름고침 by remember { mutableStateOf<String?>(null) }
    var 열린종목 by remember { mutableStateOf<String?>(null) }   // "rid|j"
    var 켠칸 by remember { mutableStateOf<String?>(null) }
    var 참고열림 by remember { mutableStateOf<String?>(null) }   // "rid|j" — 참고 링크 칸
    var 물음 by remember { mutableStateOf<Pair<String, Int>?>(null) }   // 마지막 세트에서 − 를 눌렀을 때
    var 고르기 by remember { mutableStateOf<String?>(null) }     // 종목 추가 칸이 열린 루틴
    var 방금 by remember { mutableStateOf(listOf<String>()) }
    var 끌기 by remember { mutableStateOf<끄는것?>(null) }
    // 루틴 카드 끌기 — 다른 루틴 가운데에 놓으면 합치기, 위 · 아래 끝이면 순서 옮기기 (09-25 메모 · 09-26 시안)
    var 루틴끌기 by remember { mutableStateOf<끄는것?>(null) }
    val 머리자리 = remember { mutableStateMapOf<String, Rect>() }   // rid → 루틴 머리줄 위치
    var 합칠 by remember { mutableStateOf<Pair<String, String>?>(null) }   // 집은 루틴 · 놓은 루틴
    val 줄자리 = remember { mutableStateMapOf<String, Rect>() }   // "rid|j" → 화면 기준 위치
    var 화면틀 by remember { mutableStateOf(Rect.Zero) }
    val 스크롤 = rememberScrollState()
    val 진동 = LocalHapticFeedback.current
    fun 구조바뀜0(f: (com.slayde.hasenheide.data.앱데이터) -> com.slayde.hasenheide.data.앱데이터) = 상태.바꿈 { f(it).예정초기화(오늘) }

    // 뒤로가기 — 펼친 종목 · 종목 고르기 칸을 먼저 접는다 (09-22 메모)
    BackHandler(enabled = 열린종목 != null || 고르기 != null) {
        if (열린종목 != null) { 열린종목 = null; 켠칸 = null } else 고르기 = null
    }
    // 손가락 아래 줄을 찾아 위 30% · 가운데 · 아래 30% 로 가른다
    val 놓일 = 끌기?.let { g ->
        줄자리.entries.firstOrNull { (k, r) -> k.startsWith(g.rid + "|") && g.y >= r.top && g.y < r.bottom }?.let { (k, r) ->
            val j = k.substringAfter("|").toInt()
            val 비율 = (g.y - r.top) / max(1f, r.height)
            if (j == g.j) null else 놓을곳(g.rid, j, if (비율 < 0.3f) 0 else if (비율 > 0.7f) 2 else 1)
        }
    }
    // 루틴을 놓을 곳 — 손가락 아래 머리줄. 위 30% 앞 / 가운데 합치기 / 아래 30% 뒤 (모드 0 · 1 · 2)
    fun 루틴대상(g: 끄는것): Pair<String, Int>? =
        머리자리.entries.firstOrNull { (k, r) -> k != g.rid && g.y >= r.top && g.y < r.bottom }?.let { (k, r) ->
            val 비율 = (g.y - r.top) / max(1f, r.height)
            k to (if (비율 < 0.3f) 0 else if (비율 > 0.7f) 2 else 1)
        }
    val 루틴놓일 = 루틴끌기?.let { 루틴대상(it) }
    fun 루틴끌기끝() {
        val g = 루틴끌기 ?: return
        val t = 루틴대상(g)
        루틴끌기 = null
        if (t == null) return
        when (t.second) {
            0 -> 구조바뀜0 { it.루틴옮기기(g.rid, t.first, false) }
            2 -> 구조바뀜0 { it.루틴옮기기(g.rid, t.first, true) }
            else -> 합칠 = g.rid to t.first
        }
    }
    // 화면 끝 가까이 끌면 저절로 스크롤
    LaunchedEffect(끌기 != null || 루틴끌기 != null) {
        while (끌기 != null || 루틴끌기 != null) {
            val y = (끌기 ?: 루틴끌기)?.y ?: break
            if (y < 화면틀.top + 70f) 스크롤.scrollBy(-14f) else if (y > 화면틀.bottom - 70f) 스크롤.scrollBy(14f)
            delay(16)
        }
    }
    fun 끌기시작(rid: String, j: Int, 이름: String, 손y: Float) {
        진동.performHapticFeedback(HapticFeedbackType.LongPress)
        끌기 = 끄는것(rid, j, 이름, 손y)
    }
    fun 끌기끝() {
        val g = 끌기; val t = 놓일
        끌기 = null
        if (g == null || t == null) return
        상태.바꿈 { d ->
            d.루틴바꿈(g.rid) { r ->
                when (t.모드) {
                    0 -> r.종목옮기기(g.j, t.j, false)
                    2 -> r.종목옮기기(g.j, t.j, true)
                    else -> r.슈퍼묶기(g.j, t.j, "g" + System.currentTimeMillis())
                }.묶음정리()
            }
        }
        열린종목 = null; 켠칸 = null
    }

    fun 구조바뀜(f: (com.slayde.hasenheide.data.앱데이터) -> com.slayde.hasenheide.data.앱데이터) =
        상태.바꿈 { f(it).예정초기화(오늘) }

    Box(Modifier.fillMaxSize().onGloballyPositioned { 화면틀 = it.boundsInRoot() }) {
        // 좌우 기준선 하나 — 화면 글씨 · 카드 안 내용 · 버튼이 같은 선에서 시작한다 (명세 1-2-1)
        Column(Modifier.fillMaxSize().verticalScroll(스크롤).padding(horizontal = 간격.좁게)) {
            제목글("루틴", Modifier.번호("루1").padding(start = 간격.좁게, top = 16.dp))
            글("자동생성 루틴만 순서대로 달력에 깔립니다", Modifier.padding(start = 간격.좁게, top = 4.dp, bottom = 12.dp), 크기값 = 크기.조금작게, 색 = c.옅음)

            val 다음 = d.다음차례(오늘)
            d.루틴들.forEachIndexed { i, r ->
                val 열림 = 열린루틴 == r.id
                val 표시 = 루틴놓일?.takeIf { it.first == r.id }?.second ?: -1
                카드(Modifier.padding(bottom = 12.dp).alpha(if (루틴끌기?.rid == r.id) 0.35f else 1f), 안쪽 = 0.dp) {
                    // ── 머리줄 ── 꾹 눌러 끌면 루틴 옮기기 · 합치기 (09-26)
                    Row(
                        Modifier.fillMaxWidth()
                            .onGloballyPositioned { 머리자리[r.id] = it.boundsInRoot() }
                            .번호("루2")
                            .then(if (표시 == 1) Modifier.border(2.dp, c.강조, RoundedCornerShape(모서리.작게)) else Modifier)
                            .drawBehind {
                                if (표시 == 0) drawRect(c.강조, topLeft = Offset(0f, 0f), size = Size(size.width, 3.dp.toPx()))
                                if (표시 == 2) drawRect(c.강조, topLeft = Offset(0f, size.height - 3.dp.toPx()), size = Size(size.width, 3.dp.toPx()))
                            }
                            .padding(horizontal = 간격.좁게, vertical = 간격.좁게),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            Modifier.weight(1f).눌림 { 열린루틴 = if (열림) null else r.id; 열린종목 = null }
                                .pointerInput(r.id) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { p ->
                                            진동.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val 위 = 머리자리[r.id]?.top ?: 0f
                                            루틴끌기 = 끄는것(r.id, -1, r.이름, 위 + p.y)
                                        },
                                        onDrag = { change, 양 -> change.consume(); 루틴끌기 = 루틴끌기?.let { it.copy(y = it.y + 양.y) } },
                                        onDragEnd = { 루틴끌기끝() },
                                        onDragCancel = { 루틴끌기 = null },
                                    )
                                },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            글("${i + 1}", Modifier.width(22.dp), 크기값 = 크기.크게, 색 = c.흐림, 굵기 = FontWeight.Medium)
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    제목글(r.이름, Modifier.weight(1f, fill = false), 크기값 = 크기.크게)
                                    if (다음?.id == r.id) { Box(Modifier.width(8.dp)); 알약("다음", c.좋음) }
                                }
                                글((if (r.휴식일) "휴식일" else "${r.종목.size}종목 · ${총세트(r)}세트 · ${시간글(예상초(r))}") + (if (r.자동생성) " · 자동생성" else ""),
                                    크기값 = 크기.작게, 색 = if (r.자동생성) c.강조 else c.옅음)
                            }
                        }
                        아이콘버튼(아이콘.위로, "앞으로", { 구조바뀜 { dd -> dd.copy(루틴들 = 자리바꿈(dd.루틴들, i, i - 1)) } }, 쓸수있음 = i > 0)
                        Box(Modifier.width(4.dp))
                        아이콘버튼(아이콘.아래로, "뒤로", { 구조바뀜 { dd -> dd.copy(루틴들 = 자리바꿈(dd.루틴들, i, i + 1)) } }, 쓸수있음 = i < d.루틴들.size - 1)
                        Box(Modifier.width(4.dp))
                        아이콘버튼(아이콘.설정, "루틴 설정", { 이름고침 = if (이름고침 == r.id) null else r.id }, 켬 = 이름고침 == r.id)
                        펼침단추(열림) { 열린루틴 = if (열림) null else r.id; 열린종목 = null }
                    }
                    // ── 이름 고치기 · 지우기 ──
                    if (이름고침 == r.id) {
                        var 새이름 by remember(r.id) { mutableStateOf(r.이름) }
                        구분선()
                        Row(Modifier.padding(간격.좁게), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            입력칸(새이름, { 새이름 = it }, Modifier.weight(1f), 안내 = "루틴 이름")
                            버튼("저장", { 상태.바꿈 { it.루틴바꿈(r.id) { x -> x.copy(이름 = 새이름.trim().ifEmpty { x.이름 }) } }; 이름고침 = null }, 작게 = true, 주요 = true)
                            버튼("지우기", {
                                이름고침 = null
                                상태.지우고알림("${r.이름}을(를) 지웠습니다") { dd -> dd.copy(루틴들 = dd.루틴들.filter { it.id != r.id }).예정초기화(오늘) }
                            }, 작게 = true, 글색 = c.나쁨)
                        }
                        // 자동생성 — 켜면 캘린더에 순서대로 저절로 깔린다 (09-25 메모 · 새 루틴은 꺼져 있다)
                        Box(Modifier.padding(start = 간격.좁게, end = 간격.좁게, bottom = 간격.좁게)) {
                            설정줄("자동생성", "켜면 캘린더에 순서대로 깔립니다") {
                                스위치(r.자동생성) { v -> 구조바뀜 { it.루틴바꿈(r.id) { x -> x.copy(자동생성 = v) } } }
                            }
                        }
                    }
                    // ── 몸통 ──
                    if (열림) {
                        구분선()
                        Column(Modifier.padding(start = 간격.좁게, end = 간격.좁게, top = 간격.좁게, bottom = 간격.보통)) {
                            if (r.휴식일) 글("휴식일 · 순서에서 한 칸 차지", Modifier.padding(vertical = 8.dp), 크기값 = 크기.조금작게, 색 = c.옅음)
                            else {
                            val 지난 = d.루틴최근향상(r.id)
                            향상줄(if (지난 != null) "지난번 볼륨 ${콤마(지난.지금)}kg" else "지난번 기록 없음", 지난, Modifier.padding(vertical = 8.dp))
                            if (r.종목.isEmpty()) 글("아직 종목이 없습니다", Modifier.padding(vertical = 8.dp), 크기값 = 크기.조금작게, 색 = c.옅음)
                            // 슈퍼세트 묶음은 상자로 모아 그린다
                            var j = 0
                            while (j < r.종목.size) {
                                val g = r.종목[j].슈퍼
                                if (g == null) {
                                    val jj = j
                                    종목줄(상태, 폰, r, jj, 열린종목 == "${r.id}|$jj", 켠칸,
                                        끌림 = 끌기?.rid == r.id && 끌기?.j == jj, 표시 = 놓일?.takeIf { it.rid == r.id && it.j == jj }?.모드 ?: -1,
                                        참고열림 = 참고열림 == "${r.id}|$jj",
                                        on참고 = { 참고열림 = if (참고열림 == "${r.id}|$jj") null else "${r.id}|$jj" },
                                        on마지막세트 = { 물음 = r.id to jj },
                                        on열기 = { 입력중.취소?.invoke(); 고르기 = null; 열린종목 = if (열린종목 == "${r.id}|$jj") null else "${r.id}|$jj"; 켠칸 = null },
                                        on칸 = { k -> 고르기 = null; 열린종목 = "${r.id}|$jj"; 켠칸 = if (켠칸 == k) null else k },
                                        on자리 = { 줄자리["${r.id}|$jj"] = it },
                                        on끌기시작 = { y -> 끌기시작(r.id, jj, r.종목[jj].이름, y) },
                                        on끌기 = { dy -> 끌기 = 끌기?.let { it.copy(y = it.y + dy) } },
                                        on끌기끝 = { 끌기끝() },
                                        on끌기취소 = { 끌기 = null },
                                    )
                                    j++
                                } else {
                                    val 시작 = j
                                    while (j < r.종목.size && r.종목[j].슈퍼 == g) j++
                                    val 끝 = j
                                    Column(
                                        Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(모서리.작게))
                                            .border(1.dp, c.휴식.copy(alpha = 0.45f), RoundedCornerShape(모서리.작게))
                                            .background(c.휴식옅음.copy(alpha = 0.5f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                    ) {
                                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            글("슈퍼세트 · ${끝 - 시작}종목을 번갈아", Modifier.weight(1f), 크기값 = 크기.작게, 색 = c.휴식, 굵기 = FontWeight.Bold)
                                            글("풀기", Modifier.눌림 { 상태.바꿈 { it.루틴바꿈(r.id) { x -> x.슈퍼풀기(g) } } }.padding(4.dp), 크기값 = 크기.작게, 색 = c.휴식, 굵기 = FontWeight.Bold)
                                        }
                                        for (jj in 시작 until 끝) {
                                        종목줄(상태, 폰, r, jj, 열린종목 == "${r.id}|$jj", 켠칸,
                                            끌림 = 끌기?.rid == r.id && 끌기?.j == jj, 표시 = 놓일?.takeIf { it.rid == r.id && it.j == jj }?.모드 ?: -1,
                                        참고열림 = 참고열림 == "${r.id}|$jj",
                                        on참고 = { 참고열림 = if (참고열림 == "${r.id}|$jj") null else "${r.id}|$jj" },
                                        on마지막세트 = { 물음 = r.id to jj },
                                            on열기 = { 입력중.취소?.invoke(); 고르기 = null; 열린종목 = if (열린종목 == "${r.id}|$jj") null else "${r.id}|$jj"; 켠칸 = null },
                                            on칸 = { k -> 고르기 = null; 열린종목 = "${r.id}|$jj"; 켠칸 = if (켠칸 == k) null else k },
                                            on자리 = { 줄자리["${r.id}|$jj"] = it },
                                            on끌기시작 = { y -> 끌기시작(r.id, jj, r.종목[jj].이름, y) },
                                            on끌기 = { dy -> 끌기 = 끌기?.let { it.copy(y = it.y + dy) } },
                                            on끌기끝 = { 끌기끝() },
                                            on끌기취소 = { 끌기 = null },
                                        )
                                        }
                                    }
                                }
                            }
                            // ── 종목 추가 — 그 자리에서 여러 개 (4-5) ──
                            if (고르기 == r.id) {
                                안고르기(상태, r, 방금, { 방금 = it }) { 고르기 = null; 방금 = emptyList() }
                            } else {
                                Box(Modifier.height(12.dp))
                                버튼("종목 추가", { 발자취.적기("종목 추가 칸 열기"); 입력중.취소?.invoke(); 고르기 = r.id; 방금 = emptyList(); 열린종목 = null; 켠칸 = null }, Modifier.fillMaxWidth().번호("루4"), 작게 = true, 그림 = 아이콘.더하기)
                            }
                            }
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().번호("루5").padding(start = 간격.좁게, end = 간격.좁게, top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                버튼("루틴 추가", {
                    val id = "r" + System.currentTimeMillis()
                    val 번호 = d.루틴들.count { !it.휴식일 } + 1
                    구조바뀜 { it.copy(루틴들 = it.루틴들 + 루틴(id, "새 루틴 $번호")) }
                    열린루틴 = id; 이름고침 = id
                }, Modifier.weight(1f), 그림 = 아이콘.더하기)
                버튼("휴식일", {
                    구조바뀜 { it.copy(루틴들 = it.루틴들 + 루틴("r" + System.currentTimeMillis(), "휴식", 휴식일 = true)) }
                }, Modifier.weight(0.7f), 그림 = 아이콘.더하기)
            }
            Box(Modifier.height(16.dp))   // 끝에 빈 공간을 두지 않는다 (09-21 메모)
        }
        // 끄는 동안 손가락을 따라오는 초록 알약 (1-3 '끌 때 이름표')
        (끌기 ?: 루틴끌기)?.let { g ->
            val 밀도 = LocalDensity.current
            Box(
                Modifier
                    .offset(y = with(밀도) { (g.y - 화면틀.top).toDp() } - 36.dp)
                    .padding(start = 40.dp)
                    .clip(CircleShape)
                    .background(c.강조)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) { 글(g.이름, 크기값 = 크기.버튼, 색 = c.강조글, 굵기 = FontWeight.Bold) }
        }
        // 루틴 합치기 — 어느 쪽을 위로 둘지 고른다 (09-26 시안 · 가운데 창은 고르는 일)
        합칠?.let { (집은, 놓은) ->
            val a = d.루틴(집은); val b = d.루틴(놓은)
            if (a == null || b == null) 합칠 = null
            else 합치기창(a.이름, b.이름, "${a.종목.size + b.종목.size}종목",
                on고름 = { 앞이집은것 ->
                    합칠 = null
                    val 새id = "r" + System.currentTimeMillis()
                    구조바뀜0 { it.루틴합치기(if (앞이집은것) 집은 else 놓은, if (앞이집은것) 놓은 else 집은, 새id) }
                    열린루틴 = 새id
                },
                on취소 = { 합칠 = null })
        }
        // 마지막 한 세트에서 − · 휴지통 → 종목을 뺄까요? (09-24 메모, 화면 가운데)
        물음?.let { (rid, j) ->
            val 종 = d.루틴(rid)?.종목?.getOrNull(j)
            if (종 == null) 물음 = null
            else 물음창(
                제목 = "${조사(종.이름, "을", "를")} 뺄까요?",
                설명 = "마지막 한 세트입니다",
                예 = "빼기",
                on예 = {
                    물음 = null; 열린종목 = null; 켠칸 = null
                    상태.지우고알림("${조사(종.이름, "을", "를")} 뺐습니다") { dd ->
                        dd.루틴바꿈(rid) { x -> x.copy(종목 = x.종목.filterIndexed { k, _ -> k != j }).묶음정리() }
                    }
                },
                on아니오 = { 물음 = null },
            )
        }
    }
}

/** 받침에 따라 을/를 · 은/는 (09-24) */
fun 조사(말: String, 받침: String, 없음: String): String {
    val c = 말.lastOrNull()?.code ?: return 말 + 없음
    val 받 = c in 0xAC00..0xD7A3 && (c - 0xAC00) % 28 != 0
    return 말 + (if (받) 받침 else 없음)
}

private fun <T> 자리바꿈(l: List<T>, a: Int, b: Int): List<T> {
    if (b !in l.indices) return l
    val m = l.toMutableList(); val t = m[a]; m[a] = m[b]; m[b] = t
    return m
}

@Composable
fun 펼침단추(열림: Boolean, onClick: () -> Unit) {
    val c = Local색.current
    Box(Modifier.size(높이.낮게).눌림(onClick), contentAlignment = Alignment.Center) {
        Icon(아이콘.아래, if (열림) "접기" else "펼치기", Modifier.size(18.dp).rotate(if (열림) 180f else 0f), tint = c.옅음)
    }
}

@Composable
private fun 종목줄(
    상태: 앱상태, 폰: 폰기능, r: 루틴, j: Int, 열림: Boolean, 켠칸: String?,
    끌림: Boolean, 표시: Int, 참고열림: Boolean, on참고: () -> Unit, on마지막세트: () -> Unit,
    on열기: () -> Unit, on칸: (String) -> Unit, on자리: (Rect) -> Unit,
    on끌기시작: (Float) -> Unit, on끌기: (Float) -> Unit, on끌기끝: () -> Unit, on끌기취소: () -> Unit,
) {
    val c = Local색.current
    val e = r.종목[j]
    val 폭 = 상태.d.설정.무게폭
    fun 고침바로(f: (루틴종목) -> 루틴종목) = 상태.바꿈 { d -> d.루틴바꿈(r.id) { x -> x.copy(종목 = x.종목.mapIndexed { k, y -> if (k == j) f(y) else y }) } }
    // 마지막 한 세트를 빼려 하면 종목을 뺄지 묻는다 (09-24 메모)
    fun 세트빼기(k: Int) { 발자취.적기("${e.이름} ${k + 1}세트 빼기"); if (e.세트 <= 1) on마지막세트() else 고침바로 { it.세트빼기(k) } }
    var 줄틀 by remember { mutableStateOf(Rect.Zero) }
    // 누르는 동안 바뀌는 값은 최신 것을 쓴다 — pointerInput 을 다시 시작하지 않으려고 (4. 조심할 것)
    val 열기 by rememberUpdatedState(on열기)
    val 시작 by rememberUpdatedState(on끌기시작)
    val 이동 by rememberUpdatedState(on끌기)
    val 끝 by rememberUpdatedState(on끌기끝)
    val 취소 by rememberUpdatedState(on끌기취소)
    val 틀 by rememberUpdatedState(줄틀)
    Column(Modifier.fillMaxWidth().번호("루3").alpha(if (끌림) 0.35f else 1f)) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
                .onGloballyPositioned { 줄틀 = it.boundsInRoot(); on자리(줄틀) }
                // 놓일 곳 표시 — 가운데면 상자, 경계면 그 자리에 선
                .then(if (표시 == 1) Modifier.border(2.dp, c.휴식, RoundedCornerShape(모서리.아주작게)) else Modifier)
                .drawBehind {
                    if (표시 == 0) drawRect(c.휴식, topLeft = Offset(0f, 0f), size = Size(size.width, 3.dp.toPx()))
                    if (표시 == 2) drawRect(c.휴식, topLeft = Offset(0f, size.height - 3.dp.toPx()), size = Size(size.width, 3.dp.toPx()))
                }
                .pointerInput(r.id, j) { detectTapGestures(onTap = { 열기() }) }
                .pointerInput(r.id, j) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { p -> 시작(틀.top + p.y) },
                        onDrag = { change, 끌림양 -> change.consume(); 이동(끌림양.y) },
                        onDragEnd = { 끝() },
                        onDragCancel = { 취소() },
                    )
                }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 이름 옆에는 1RM · 볼륨만 (09-24 메모). 무게×횟수×세트 요약은 뺐다
            글(e.이름, Modifier.weight(1f), 굵기 = FontWeight.Medium)
            Box(Modifier.width(8.dp))
            Text(
                "1RM ${"%.1f".format(상태.d.종목1RM(e.이름, e))} · 볼륨 ${콤마(e.볼륨())}",
                style = 글꼴.보통(크기.작게), color = c.옅음, maxLines = 1, softWrap = false,
            )
            Box(Modifier.width(4.dp))
            // 참고 링크 — 주소는 감추고 적어 둔 글씨만. 있으면 아이콘에 색이 든다
            val 참고 = 상태.d.종목표.firstOrNull { it.이름 == e.이름 }
            아이콘버튼(아이콘.링크, "참고 링크", on참고, 켬 = 참고?.참고url != null, 칠함 = false, 크기칸 = 높이.아주낮게)
            펼침단추(열림, on열기)
        }
        if (참고열림) 참고칸(상태, 폰, e.이름) { on참고() }
        if (열림) {
            fun 고침(f: (루틴종목) -> 루틴종목) = 상태.바꿈 { d -> d.루틴바꿈(r.id) { x -> x.copy(종목 = x.종목.mapIndexed { k, y -> if (k == j) f(y) else y }) } }
            // 세트 줄 — 세트마다 [세트 n][무게][횟수][휴식] + 휴지통 (09-24 시안)
            Column(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                for (k in 0 until e.세트) {
                    val v = e.목표(k)
                    val t = e.휴식(k)
                    숫자버튼줄(
                        listOf(
                            // 세트 — − ＋ 만 (휠 · 자판 없음). ＋ 는 누른 줄을 베껴 바로 아래에
                            숫자칸("$k|s", "세트", "${k + 1}", "", 입력종류.버튼,
                                { 세트빼기(k) }, { 고침 { it.세트끼우기(k) } }, { },
                                폭 = 0.75f, 폭열림 = 1.07f),
                            숫자칸("$k|w", "무게", 무게글(v.w), "kg", 입력종류.소수,
                                { 고침 { it.세트고침(k, w = 무게반올림(max(0.0, it.목표(k).w - 폭))) } }, { 고침 { it.세트고침(k, w = 무게반올림(it.목표(k).w + 폭)) } },
                                { s -> s.replace(',', '.').toDoubleOrNull()?.let { x -> 고침 { it.세트고침(k, w = 무게반올림(max(0.0, x))) } } },
                                휠값.무게, { 무게글(it) }, v.w, 폭 = 1f, 폭열림 = 1.69f),
                            숫자칸("$k|r", "횟수", "${v.r}", "회", 입력종류.정수,
                                { 고침 { it.세트고침(k, r = max(1, it.목표(k).r - 1)) } }, { 고침 { it.세트고침(k, r = it.목표(k).r + 1) } },
                                { s -> s.toIntOrNull()?.let { x -> 고침 { it.세트고침(k, r = max(1, x)) } } },
                                휠값.횟수, { "${it.toInt()}" }, v.r.toDouble(), 폭 = 0.85f, 폭열림 = 1.6f),
                            숫자칸("$k|t", "휴식", 분초(t), "", 입력종류.분초,
                                { 고침 { it.세트고침(k, t = max(0, it.휴식(k) - 5)) } }, { 고침 { it.세트고침(k, t = it.휴식(k) + 5) } },
                                { s -> 초읽기(s)?.let { x -> 고침 { it.세트고침(k, t = x) } } },
                                휠값.휴식, { 분초(it.toInt()) }, t.toDouble(), 폭 = 1f, 폭열림 = 1.51f),
                        ),
                        켠칸, on칸,
                        오른쪽 = {
                            Box(Modifier.width(14.dp).height(높이.보통).눌림 { 세트빼기(k) }, contentAlignment = Alignment.Center) {
                                Icon(아이콘.지우기, "세트 ${k + 1} 지우기", Modifier.size(12.dp), tint = c.옅음)
                            }
                        },
                    )
                }
            }
        }
        구분선()
    }
}

/**
 * 참고 링크 칸 (09-24 메모) — 주소는 감추고 적어 둔 글씨만 보인다.
 * 종목표에 저장하므로 어느 루틴에서 넣어도 같이 보인다.
 */
@Composable
private fun 참고칸(상태: 앱상태, 폰: 폰기능, 종목이름: String, 닫기: () -> Unit) {
    val c = Local색.current
    val 것 = 상태.d.종목표.firstOrNull { it.이름 == 종목이름 }
    fun 넣기(글: String?, 주소: String?) = 상태.바꿈 { d ->
        val 있나 = d.종목표.any { it.이름 == 종목이름 }
        val 표 = if (있나) d.종목표.map { if (it.이름 == 종목이름) it.copy(참고글 = 글, 참고url = 주소) else it }
                 else d.종목표 + 종목(종목이름, d.카테고리.firstOrNull() ?: "", 참고글 = 글, 참고url = 주소)
        d.copy(종목표 = 표)
    }
    Column(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        val url = 것?.참고url
        if (url == null) {
            var 새주소 by remember(종목이름) { mutableStateOf("") }
            var 새글 by remember(종목이름) { mutableStateOf("") }
            이름표("참고 링크")
            Box(Modifier.height(4.dp))
            입력칸(새주소, { 새주소 = it }, Modifier.fillMaxWidth(), 안내 = "주소 (유튜브 등)")
            Box(Modifier.height(4.dp))
            입력칸(새글, { 새글 = it }, Modifier.fillMaxWidth(), 안내 = "보일 글씨 (예: 벤치프레스 자세)")
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                버튼("취소", 닫기, Modifier.weight(1f), 작게 = true)
                버튼("넣기", {
                    val u = 새주소.trim()
                    if (u.isNotEmpty()) { 넣기(새글.trim().ifEmpty { u }, u); 닫기() }
                }, Modifier.weight(1f), 작게 = true, 주요 = true)
            }
        } else {
            Row(
                Modifier.fillMaxWidth().height(높이.보통).clip(RoundedCornerShape(모서리.작게)).background(c.면2)
                    .눌림 { 폰.링크열기(url) }.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(아이콘.링크, null, Modifier.size(16.dp), tint = c.강조)
                Box(Modifier.width(8.dp))
                글(것.참고글 ?: url, Modifier.weight(1f), 크기값 = 크기.버튼, 색 = c.강조, 굵기 = FontWeight.Medium)
            }
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                버튼("지우기", { 넣기(null, null) }, Modifier.weight(1f), 작게 = true, 글색 = c.나쁨)
                버튼("닫기", 닫기, Modifier.weight(1f), 작게 = true)
            }
        }
    }
}

/** 세트 줄의 값 한 칸 — 누르면 그 값을 고치는 칸이 열린다 */
@Composable
private fun 세트값칸(글자: String, 켬: Boolean, modifier: Modifier, on누름: () -> Unit) {
    val c = Local색.current
    Box(modifier.fillMaxHeight().눌림(on누름), contentAlignment = Alignment.CenterEnd) {
        글(글자, Modifier.padding(end = 12.dp), 크기값 = 크기.버튼, 색 = if (켬) c.강조 else c.글, 굵기 = if (켬) FontWeight.Bold else FontWeight.Medium)
    }
}

/** 동그란 − ＋ 단추 */
@Composable
private fun 둥근단추(그림: androidx.compose.ui.graphics.vector.ImageVector, 설명: String, 켜짐: Boolean, on누름: () -> Unit) {
    val c = Local색.current
    Box(
        Modifier.size(높이.보통).clip(CircleShape).background(c.면).border(1.dp, if (켜짐) c.선 else c.면2, CircleShape)
            .then(if (켜짐) Modifier.눌림(on누름) else Modifier),
        contentAlignment = Alignment.Center,
    ) { Icon(그림, 설명, Modifier.size(20.dp), tint = if (켜짐) c.강조 else c.옅음) }
}

/** 루틴 안에서 바로 고르는 칸 — 누르면 바로 들어가고, 다시 누르면 빠진다 (4-5) */
@Composable
private fun 안고르기(상태: 앱상태, r: 루틴, 방금: List<String>, 방금바꿈: (List<String>) -> Unit, 완료: () -> Unit) {
    val c = Local색.current
    val d = 상태.d
    var 부위 by remember { mutableStateOf("전체") }
    var 새로 by remember { mutableStateOf(false) }
    var 새이름 by remember { mutableStateOf("") }
    var 새장비 by remember { mutableStateOf("") }
    var 새부위 by remember { mutableStateOf(d.카테고리.firstOrNull() ?: "") }
    var 새부위손댐 by remember { mutableStateOf(false) }
    var 새장비손댐 by remember { mutableStateOf(false) }
    val 있음 = r.종목.map { it.이름 }.toSet()
    // 자판이 올라오면 이 칸이 가려진다 → 열릴 때 · 새 종목 칸을 열 때 화면 위로 끌어올린다 (09-24 메모)
    val 끌어올림 = remember { BringIntoViewRequester() }
    LaunchedEffect(새로) { delay(if (새로) 350L else 120L); 끌어올림.bringIntoView() }

    Column(
        Modifier.fillMaxWidth().padding(top = 12.dp)
            .bringIntoViewRequester(끌어올림)
            .clip(RoundedCornerShape(모서리.작게))
            .background(c.면2)
            .border(1.dp, c.선, RoundedCornerShape(모서리.작게))
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            글("종목 추가", 굵기 = FontWeight.Bold)
            글(if (방금.isEmpty()) "누르면 추가" else "${방금.size}개 넣음", Modifier.weight(1f).padding(start = 8.dp), 크기값 = 크기.작게, 색 = c.옅음)
            버튼("완료", 완료, 작게 = true, 주요 = true)
        }
        Box(Modifier.height(8.dp))
        칩줄(listOf("전체") + d.카테고리, 부위, { 부위 = it })
        Box(Modifier.height(4.dp))
        // 새 종목 만들기 — 처음엔 종목이 하나도 없으므로 여기서 바로 만든다
        if (새로) {
            Column(Modifier.padding(vertical = 8.dp)) {
                종목이름칸(상태, 새이름, { t ->
                    새이름 = t
                    val g = 이름추천.추측하기(t)
                    if (!새부위손댐 && g.부위 != null && g.부위 in d.카테고리) 새부위 = g.부위
                    if (!새장비손댐) 새장비 = g.장비 ?: ""
                }) { 이름, 부, 장 ->
                    새이름 = 이름
                    if (부 in d.카테고리) 새부위 = 부
                    if (장.isNotBlank()) 새장비 = 장
                }
                Box(Modifier.height(8.dp))
                부위고르기(상태, 새부위, { 새부위 = it; 새부위손댐 = true })
                Box(Modifier.height(8.dp))
                장비고르기(상태, 새장비, { 새장비 = it; 새장비손댐 = true }, 이름추천.추측하기(새이름).장비후보)
                Box(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    버튼("취소", { 새로 = false; 새이름 = ""; 새장비 = "" }, Modifier.weight(1f), 작게 = true)
                    버튼("만들고 넣기", {
                        val 이름 = 새이름.trim()
                        if (이름.isNotEmpty()) {
                            상태.바꿈 { dd ->
                                val 표 = if (dd.종목표.any { it.이름 == 이름 }) dd.종목표 else dd.종목표 + 종목(이름, 새부위, 새장비.trim())
                                dd.copy(종목표 = 표).루틴바꿈(r.id) { x -> x.copy(종목 = x.종목 + 루틴종목(이름, 세트 = dd.설정.기본세트, 휴식 = dd.설정.기본휴식)) }
                            }
                            방금바꿈(방금 + 이름); 새이름 = ""; 새장비 = ""; 새로 = false; 새부위손댐 = false; 새장비손댐 = false
                        }
                    }, Modifier.weight(1.4f), 작게 = true, 주요 = true)
                }
            }
        } else {
            고르기줄("새 종목 만들기", null, 아이콘.더하기) { 새로 = true }
        }
        val 목록 = d.종목표.filter { 부위 == "전체" || it.부위 == 부위 }
        if (목록.isEmpty() && !새로) 글("이 부위에 종목이 없습니다", Modifier.padding(vertical = 12.dp), 크기값 = 크기.조금작게, 색 = c.옅음)
        Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
            목록.forEach { e ->
                val 넣음 = e.이름 in 방금
                // 이미 있는 종목도 한 번 더 넣을 수 있다 (09-21 메모). 방금 넣은 것을 다시 누르면 빠진다
                val 원래 = e.이름 in 있음 && !넣음
                val 몇개 = r.종목.count { it.이름 == e.이름 }
                Row(
                    Modifier.fillMaxWidth()
                        .then(Modifier.눌림 {
                            if (넣음) {
                                상태.바꿈 { dd -> dd.루틴바꿈(r.id) { x ->
                                    val k = x.종목.indexOfLast { it.이름 == e.이름 }
                                    if (k < 0) x else x.copy(종목 = x.종목.filterIndexed { i, _ -> i != k }).묶음정리()
                                } }
                                방금바꿈(방금 - e.이름)
                            } else {
                                상태.바꿈 { dd -> dd.루틴바꿈(r.id) { x -> x.copy(종목 = x.종목 + 루틴종목(e.이름, 세트 = dd.설정.기본세트, 휴식 = dd.설정.기본휴식)) } }
                                방금바꿈(방금 + e.이름)
                            }
                        })
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
                        글(e.이름, Modifier.weight(1f, fill = false), 색 = if (넣음) c.강조 else c.글, 굵기 = if (넣음) FontWeight.Bold else FontWeight.Normal)
                        Box(Modifier.width(8.dp))
                        글(listOf(e.부위, e.장비).filter { it.isNotBlank() }.joinToString("·"), 크기값 = 크기.작게, 색 = c.옅음)
                    }
                    when {
                        원래 -> Row(verticalAlignment = Alignment.CenterVertically) {
                            글(if (몇개 > 1) "있음 ×$몇개" else "있음", 크기값 = 크기.작게, 색 = c.옅음)
                            Box(Modifier.width(8.dp))
                            Icon(아이콘.더하기, "한 번 더 넣기", Modifier.size(18.dp), tint = c.강조)
                        }
                        넣음 -> Row(verticalAlignment = Alignment.CenterVertically) {
                            if (몇개 > 1) { 글("×$몇개", 크기값 = 크기.작게, 색 = c.강조); Box(Modifier.width(8.dp)) }
                            Icon(아이콘.체크, "넣음", Modifier.size(18.dp), tint = c.강조)
                        }
                        else -> Icon(아이콘.더하기, "넣기", Modifier.size(18.dp), tint = c.강조)
                    }
                }
                구분선()
            }
        }
    }
}

/** 루틴 합치기 창 — 화면 가운데. 어느 루틴을 위로 둘지 고른다 (09-26) */
@Composable
private fun 합치기창(가: String, 나: String, 요약: String, on고름: (Boolean) -> Unit, on취소: () -> Unit) {
    val c = Local색.current
    BackHandler(onBack = on취소)
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)).눌림(on취소),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.fillMaxWidth(0.72f).clip(RoundedCornerShape(모서리.보통)).background(c.면).눌림 { }.padding(간격.넓게),
        ) {
            제목글("새 루틴 만들기", 크기값 = 크기.크게)
            글("$가 + $나 · $요약 · 어느 쪽을 위로?", Modifier.padding(top = 4.dp), 크기값 = 크기.버튼, 색 = c.흐림, 줄 = 2)
            Column(Modifier.padding(top = 간격.넓게), verticalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                버튼("$가 먼저", { on고름(true) }, Modifier.fillMaxWidth(), 작게 = true, 주요 = true)
                버튼("$나 먼저", { on고름(false) }, Modifier.fillMaxWidth(), 작게 = true)
                버튼("취소", on취소, Modifier.fillMaxWidth(), 작게 = true)
            }
        }
    }
}
