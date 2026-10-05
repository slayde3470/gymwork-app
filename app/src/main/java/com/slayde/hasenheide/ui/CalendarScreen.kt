package com.slayde.hasenheide.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.slayde.hasenheide.data.근육계산
import com.slayde.hasenheide.data.날기록
import com.slayde.hasenheide.data.날짜만
import com.slayde.hasenheide.data.대비결과
import com.slayde.hasenheide.data.두대비
import com.slayde.hasenheide.data.루틴
import com.slayde.hasenheide.data.건너뜀남김
import com.slayde.hasenheide.data.그날만바꾸기
import com.slayde.hasenheide.data.꽂기
import com.slayde.hasenheide.data.날휴식
import com.slayde.hasenheide.data.목표
import com.slayde.hasenheide.data.무게글
import com.slayde.hasenheide.data.사흘미리
import com.slayde.hasenheide.data.세션종목
import com.slayde.hasenheide.data.세트
import com.slayde.hasenheide.data.세트종류
import com.slayde.hasenheide.data.시간글
import com.slayde.hasenheide.data.앱데이터
import com.slayde.hasenheide.data.예상초
import com.slayde.hasenheide.data.예정루틴
import com.slayde.hasenheide.data.예정옮기기
import com.slayde.hasenheide.data.예정지우기
import com.slayde.hasenheide.data.예정초기화
import com.slayde.hasenheide.data.운동량
import com.slayde.hasenheide.data.운동세션
import com.slayde.hasenheide.data.운동시작
import com.slayde.hasenheide.data.한세트수
import com.slayde.hasenheide.data.목표세트
import com.slayde.hasenheide.data.조절적용
import com.slayde.hasenheide.data.조절해시작
import com.slayde.hasenheide.data.측정워밍업붙임
import com.slayde.hasenheide.data.콤마
import com.slayde.hasenheide.data.총세트
import com.slayde.hasenheide.data.플랜줄채움
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.글꼴
import com.slayde.hasenheide.ui.theme.높이
import com.slayde.hasenheide.ui.theme.모서리
import com.slayde.hasenheide.ui.theme.부품치수
import com.slayde.hasenheide.ui.theme.선굵기
import com.slayde.hasenheide.ui.theme.움직임
import com.slayde.hasenheide.ui.theme.크기
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.max

// ═══════════════════════════════════════════════════════════════════════════════
//  캘린더 (10-05 앱 옮기기 2단계 C) — 시안 7일체험.html v21 의 `캘린더` · `날판` · `달고르기시트` · `년고르기` · `기록지우기` 를 옮겼다.
//  v6 날짜 판(예정 이름표 없음 · 칩 세트/볼륨/예상 · [변경] 은 띠 · 운동 시작 맨 아래)
//  v7 [스탯] ‹ 년월 › [업적] · 구분선 · 날짜 띠 / v8 두 칸 목록 · 주말 색(일 빨강 · 토 파랑)
//  v9 기록 날 [한 번 더][운동 기록 삭제] · 달 고르기 / v11~16 [운동 보고서] · 7칸 같은 폭
//  v17 구분선 되살림 · 년 고르기 · ‹› · 기록 골라 지우기(체크) · 되돌림 띠는 알림판(누른 단추 위 · 페이드 · ×0.75)
// ═══════════════════════════════════════════════════════════════════════════════

// ─────────────── 순수 계산 (시험: test/.../ui/CalendarTest.kt · 합칠 때 data/ 로 옮길 것) ───────────────

/**
 * 기록 줄 ↔ 루틴 줄이 같은 종목인가 — 둘 다 종id 가 있으면 종id 로, 아니면 이름으로 (같은 이름 종목 · 옛 줄).
 * 이름이 같아도 종id 가 다르면 다른 종목이다
 */
internal fun 캘같은종목(a종id: String?, a이름: String, b종id: String?, b이름: String): Boolean =
    if (a종id != null && b종id != null) a종id == b종id else a이름 == b이름

/** 기록 열쇠의 차례 — "2026-10-04" = 1, "~2" = 2, "~3" = 3 … (글자 순서로는 ~10 이 ~2 앞에 온다) */
internal fun 캘열쇠차례(k: String): Int =
    if ('~' in k) k.substringAfter('~').toIntOrNull() ?: Int.MAX_VALUE else 1

/** 그 날 기록 전부 — 열쇠 차례대로 (시안 `기록목록`). 첫 기록을 지워 '~2' 만 남아도 그 날 기록으로 본다 */
internal fun 앱데이터.캘기록목록(날: String): List<Pair<String, 날기록>> =
    기록.filterKeys { 날짜만(it) == 날 }.toList().sortedBy { 캘열쇠차례(it.first) }

/** 그 날 기록이 하나라도 있나 (시안 `기록있음`) */
internal fun 앱데이터.캘기록있음(날: String): Boolean = 기록.keys.any { 날짜만(it) == 날 }

/** 그 날 기록을 이 차례로 다시 놓는다 — 날짜 · ~2 · ~3 … 빈 번호 없이 (첫 기록 열쇠가 늘 날짜가 되게 — data 쪽 함수들이 그렇게 본다) */
internal fun 앱데이터.캘기록놓기(날: String, 줄: List<날기록>): 앱데이터 {
    val 남 = 기록.filterKeys { 날짜만(it) != 날 }
    val 새 = 줄.mapIndexed { i, r -> (if (i == 0) 날 else "$날~${i + 1}") to r }
    return copy(기록 = (남 + 새).toSortedMap())
}

/** 지운 기록 한 묶음 — 되돌릴 때 원래 차례를 알려고 지우기 전 그 날 기록 전부와 지운 번호를 들고 있다 */
internal class 캘지움값(val 날: String, val 전: List<날기록>, val 지운: Set<Int>) {
    val 개수: Int get() = 지운.size
}

/**
 * 그 날 기록 지우기 (시안 v17 `기록지우기`) — 기록이 둘 이상이면 [뺌](체크를 끈 열쇠)은 남긴다. 하나뿐이면 그것을.
 * 지울 것이 없으면 null. 남은 기록은 날짜 · ~2 … 로 다시 놓는다
 */
internal fun 앱데이터.캘기록지우기(날: String, 뺌: Set<String>): Pair<앱데이터, 캘지움값>? {
    val 전 = 캘기록목록(날)
    if (전.isEmpty()) return null
    val 지울 = if (전.size > 1) 전.indices.filter { 전[it].first !in 뺌 }.toSet() else setOf(0)
    if (지울.isEmpty()) return null
    val 남 = 전.filterIndexed { i, _ -> i !in 지울 }.map { it.second }
    return 캘기록놓기(날, 남) to 캘지움값(날, 전.map { it.second }, 지울)
}

/**
 * 되돌리기 — 지운 기록을 원래 차례 자리에 되살린다. 그 사이 새로 생긴 기록은 뒤에 붙는다.
 * 이미 있는 기록은 두 번 넣지 않는다. 여러 번 지운 것을 새것부터 차례로 되돌리면 처음 차례 그대로다
 */
internal fun 앱데이터.캘기록되살림(x: 캘지움값): 앱데이터 {
    val 지금 = 캘기록목록(x.날).map { it.second }.toMutableList()
    val 줄 = mutableListOf<날기록>()
    x.전.forEachIndexed { i, r ->
        val j = 지금.indexOf(r)
        if (j >= 0) 줄 += 지금.removeAt(j) else if (i in x.지운) 줄 += r
    }
    줄 += 지금
    return 캘기록놓기(x.날, 줄)
}

/** 달력 칸 한 개의 글 (시안 `칸내용`) — 기록 > 미실시 > 예정. 기록이 여럿이면 '하체 +1' */
internal enum class 캘칸종류 { 록, 미, 휴, 예 }
internal data class 캘칸값(val 글: String, val 상: String?, val 종류: 캘칸종류)

internal fun 앱데이터.캘칸내용(k: String): 캘칸값? {
    val 록 = 캘기록목록(k)
    if (록.isNotEmpty()) {
        val 글 = 록[0].second.루틴이름 + if (록.size > 1) " +${록.size - 1}" else ""
        return 캘칸값(글, if (록.all { it.second.달성 }) "달성" else "미달성", 캘칸종류.록)
    }
    미실시[k]?.let { return 캘칸값(it, "미실시", 캘칸종류.미) }
    val r = 예정루틴(k) ?: return null
    return 캘칸값(r.이름, null, if (r.휴식일) 캘칸종류.휴 else 캘칸종류.예)
}

/** 꾹 눌러 옮기기 · 집어 옮기기가 되는가 (데이터 `예정옮기기` 와 같은 조건 + '~2' 만 남은 날도 기록 있는 날로) */
internal fun 앱데이터.캘옮길수있음(원: String, D: String, 오늘: String): Boolean =
    원 != D && 원 >= 오늘 && D >= 오늘 && !캘기록있음(원) && !캘기록있음(D) && 예정[원] != null

/** "60kg × 9회" · "60~65kg × 8~10회" · 모두 0kg 이면 "맨몸 × 12회" (시안 `세트글`) */
internal fun 캘세트글(세: List<세트>): String {
    if (세.isEmpty()) return ""
    val w = 세.map { it.w }; val r = 세.map { it.r }
    val 무 = if (w.all { it == 0.0 }) "맨몸" else (if (w.min() == w.max()) 무게글(w.min()) else "${무게글(w.min())}~${무게글(w.max())}") + "kg"
    val 회 = if (r.min() == r.max()) "${r.min()}" else "${r.min()}~${r.max()}"
    return "$무 × ${회}회"
}

/** 날짜 판 목록 칸 수 — 이보다 많으면 (칸-1)개 + '외 N종목' (시안 `날판칸`) */
internal const val 캘판칸 = 10
/** 날짜 판 두 칸 목록 — 보일 종목 수 · '외 N종목' 칸이 있나 · 왼쪽 칸의 줄 수(왼쪽을 위→아래로 먼저 채운다) */
internal data class 캘판배치값(val 보일: Int, val 접기칸: Boolean, val 행: Int)

internal fun 캘판배치(n: Int, 펼침: Boolean): 캘판배치값 {
    val 접 = n > 캘판칸
    val 보일 = if (접 && !펼침) 캘판칸 - 1 else n
    val 칸수 = 보일 + if (접) 1 else 0
    return 캘판배치값(보일, 접, (칸수 + 1) / 2)
}

/** 지난 기록 → 결과 화면에 넘길 운동세션 (끝난 모양). 달성 · 볼륨 · 시간이 기록과 같게 */
internal fun 앱데이터.캘기록세션(rec: 날기록, 날: String): 운동세션 {
    val 짝 = 루틴들.firstOrNull { it.id == rec.루틴id }
    val 초 = rec.걸린초.coerceAtLeast(0) * 1000L
    // 끝 = 기록의 끝 시각 (결과 화면이 이것으로 '방금 그 기록' 을 빼고 견준다). 시간 = 걸린초 (마무리에 머문 시간은 빠진 값)
    val 끝 = if (rec.끝시각 > 0) rec.끝시각
        else LocalDate.parse(날짜만(날)).atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + 초
    val 시작 = if (초 > 0) 끝 - 초 else if (rec.시작시각 > 0) rec.시작시각 else 끝
    val 들 = rec.종목들.map { e ->
        val n = e.세트들.size
        val 웜 = e.세트들.count { it.종류 == 세트종류.워밍업 }
        val 계획 = 짝?.종목?.firstOrNull { 캘같은종목(it.종id, it.이름, e.종id, e.이름) }?.세트
        val 계획세트 = if (rec.달성 || 계획 == null) n else max(n, 계획 + 웜)
        val 첫 = e.세트들.firstOrNull()
        세션종목(
            e.이름, n, 계획세트, 첫?.w ?: 0.0, 첫?.r ?: 0, 0,
            기록 = e.세트들, 예정값 = e.세트들, 임시 = e.임시, 슈퍼 = e.묶음, 플랜id = e.플랜id, 종id = e.종id,
        )
    }
    val 첫 = 들.firstOrNull()
    val S = 운동세션(rec.루틴id, rec.루틴이름, 시작, 0, 0, 첫?.무게 ?: 0.0, 첫?.횟수 ?: 0, 들, 끝화면 = true, 끝시각 = 끝, 마지막 = 끝)
    // 미달성 기록인데 계획을 몰라(루틴을 지웠거나 바꿈) 다 한 것으로 보이면 — 첫 정식 종목의 계획을 하나 늘려 '미달성' 이 되게
    if (!rec.달성 && S.한세트수() >= S.목표세트()) {
        val j = 들.indexOfFirst { !it.임시 }
        if (j >= 0) return S.copy(종목들 = 들.mapIndexed { i, e -> if (i == j) e.copy(계획세트 = e.계획세트 + (S.한세트수() - S.목표세트()) + 1) else e })
    }
    return S
}

internal fun 캘날글(k: String): String = LocalDate.parse(날짜만(k)).let { "${it.monthValue}월 ${it.dayOfMonth}일" }
internal fun 캘요일글(k: String): String = "일월화수목금토"[LocalDate.parse(날짜만(k)).dayOfWeek.value % 7].toString()

// ─────────────── 화면 값 ───────────────

/** 달력 칸 높이 (시안 `.칸날` 64 · 날짜 13 + 루틴 11 + 상태 11 이 들어간다) [Theme 에 없는 새 값 — 합칠 때 Theme.kt 로] */
private val 칸높이 = 64.dp
/** 끄는 중 위쪽 띠 좌·우 1/3 에 이만큼 머무르면 달이 넘어간다 (ms · 시안 600) */
private const val 달넘김머무름 = 600L
/** 날짜 판 기록 이름은 줄의 이만큼까지 (시안 `.예이름 max-width:40%`) */
private const val 이름폭비 = 0.4f
/** 칸 안 글 옆 · 요일 줄 위아래 (시안 `.칸날` padding 4px 2px · `.요일` padding 2px 0) */
private val 칸글옆 = 2.dp
/** 미실시 이름표 칸 폭 (시안 `.판줄 .이름` 54) */
private val 판이름폭 = 54.dp
private const val 해칸수 = 12
/** 집어 둔 날 (시안 `.칸날.집음 opacity .55`) · 꺼진 삭제 단추 (시안 `.판단추 .버튼:disabled opacity .35`) */
private const val 집음투명 = 0.55f
private const val 꺼짐투명 = 0.35f
private val 해범위 = 1..9999

/** 고른 날 · 보는 달 — 탭을 옮겼다 돌아와도 그대로 (시안 U.고른날 · U.보는달). 앱을 다시 켜면 오늘 */
private object 캘기억 {
    var 고른날: String? = null
    var 보는달: YearMonth? = null
}

private enum class 캘시트 { 변경, 루틴, 휴식, 달 }

// ═══════════════════════════════════════════════════════════════════════════════

/**
 * 캘린더 탭 — [스탯] ‹ 년월 › [업적] 띠 / 7칸 달력 / 날짜 판(띠 + 기록 · 예정) / 맨 아래 단추 줄.
 * 운동 보고서는 R 의 [결과화면] 을 그대로 쓴다.
 * [업적으로] — App.kt 가 아직 넘기지 않으면 스탯 화면을 연다 (공용 고칠 것)
 */
@Composable
fun 캘린더화면(상태: 앱상태, 루틴으로: () -> Unit, 운동으로: () -> Unit, 스탯으로: () -> Unit = {}, 업적으로: () -> Unit = 스탯으로) {
    val c = Local색.current
    val d = 상태.d
    val 오늘 = 상태.오늘
    val 이번달 = YearMonth.from(LocalDate.parse(오늘))
    var 보는달 by remember { mutableStateOf(캘기억.보는달 ?: 이번달) }
    var 고른날 by remember { mutableStateOf(캘기억.고른날 ?: 오늘) }
    SideEffect { 캘기억.보는달 = 보는달; 캘기억.고른날 = 고른날 }
    var 집은날 by remember { mutableStateOf<String?>(null) }
    var 열린 by remember { mutableStateOf<캘시트?>(null) }
    var 시트날 by remember { mutableStateOf(오늘) }
    var 록뺌 by remember { mutableStateOf<Set<String>>(emptySet()) }   // 기록이 여럿인 날 — 체크를 끈 기록 열쇠
    var 예펼침 by remember { mutableStateOf<String?>(null) }
    var 보고 by remember { mutableStateOf<운동세션?>(null) }

    fun 날고름(k: String) { if (k != 고른날) 록뺌 = emptySet(); 고른날 = k }
    fun 달넘김(n: Int) { 보는달 = 보는달.plusMonths(n.toLong()) }
    fun 옮김(원: String, 새: String) {
        val dd = 상태.d
        if (dd.캘옮길수있음(원, 새, 상태.오늘)) {
            상태.바꿈 { it.예정옮기기(원, 새, 상태.오늘) }; 날고름(새)
            상태.알림.토스트("${캘날글(새)}로 옮겼습니다")
        } else 상태.알림.토스트("그 날로는 옮길 수 없습니다")
    }
    // 날 칸을 눌렀다 — 집어 둔 예정이 있으면 그 날로 옮긴다 (시안 행동 '날')
    fun 날누름(k: String) {
        val 원 = 집은날
        if (원 == null) { 날고름(k); return }
        집은날 = null
        if (원 != k) 옮김(원, k)
    }
    fun 시작(S: 운동세션?) {
        when {
            상태.d.세션 != null -> { 운동으로(); 상태.알림.토스트("진행 중인 운동이 있습니다") }
            S == null -> 상태.알림.토스트("종목이 없는 루틴입니다")
            else -> 상태.바꿈 { it.copy(세션 = S) }   // App 이 세션을 보고 운동 화면으로 간다
        }
    }
    fun 기록지움(k: String) {
        val (새, x) = 상태.d.캘기록지우기(k, 록뺌) ?: return
        상태.바꿈 { 새.예정초기화(상태.오늘) }
        록뺌 = emptySet(); 예펼침 = null
        발자취.적기("${캘날글(k)} 운동 기록 삭제${if (x.개수 > 1) " (${x.개수}개)" else ""}")
        // 띠가 떠 있는 동안 또 지우면 한 띠로 합친다 — 글은 지운 기록 수 전부 (시안 `기록지움띠`)
        val 묶음 = "캘린더기록"
        if (상태.알림.목록.none { it.묶음 == 묶음 }) 캘지움띠.목록.clear()
        캘지움띠.목록 += x
        val 지운것 = 캘지움띠.목록.toList()
        상태.알림.되돌림(묶음, { _ ->
            val n = 지운것.sumOf { it.개수 }
            val 한날 = 지운것.all { it.날 == k }
            "${if (한날) 캘날글(k) + " " else ""}운동 기록${if (n > 1) " ${n}개를" else "을"} 지웠습니다"
        }) {
            캘지움띠.목록.remove(x)
            상태.바꿈 { it.캘기록되살림(x).예정초기화(상태.오늘) }
            고른날 = x.날; 캘기억.고른날 = x.날
            발자취.적기("${캘날글(x.날)} 운동 기록 되돌림")
        }
    }
    fun 보고서(k: String, rec: 날기록) {
        val S = 상태.d.캘기록세션(rec, k)
        // 결과 화면은 App 이 `결과` 로 그린다. 운동 중에는 App 이 운동 화면을 그리므로 이 화면 위에 직접 띄운다
        상태.바꿈 { it.copy(결과 = S) }
        if (상태.d.세션 != null) 보고 = S
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            년월띠(보는달, { 달넘김(-1) }, { 달넘김(1) }, { 시트날 = 오늘; 열린 = 캘시트.달 }, 스탯으로, 업적으로)
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
                // 달을 넘기면 다음 달은 오른쪽에서, 이전 달은 왼쪽에서 살짝 밀려 들어온다 (10-02).
                // 달력 조각은 하나로 둔다 — 꾹 눌러 끄는 중에 달이 넘어가도 끌기가 끊기지 않게
                val 밀기 = remember { Animatable(0f) }
                var 앞달 by remember { mutableStateOf(보는달) }
                LaunchedEffect(보는달) {
                    if (보는달 != 앞달) {
                        val 앞으로 = 보는달 > 앞달
                        앞달 = 보는달
                        밀기.snapTo(if (앞으로) 1f else -1f)
                        밀기.animateTo(0f, tween(움직임.달))
                    }
                }
                Column(
                    Modifier.graphicsLayer {
                        translationX = 밀기.value * size.width * 움직임.달밀기
                        alpha = 1f - abs(밀기.value) * 움직임.달흐림
                    }.padding(start = 간격.아주좁게, end = 간격.아주좁게, top = 간격.아주좁게, bottom = 간격.아주좁게),
                ) {
                    요일줄()
                    달력(d, 오늘, 보는달, 고른날, 집은날, Modifier.번호("캘2"),
                        on누름 = { 날누름(it) },
                        on옮김 = { 원, 새 -> 옮김(원, 새) },
                        on집음 = { 원, 달이동 -> 집은날 = 원; 날고름(원); if (달이동 != 0) 달넘김(달이동) },
                        on달넘김 = { 달넘김(it) })
                }
                날판(
                    상태, 고른날, 다른달 = 보는달 != 이번달, 록뺌 = 록뺌, 예펼침 = 예펼침,
                    on오늘 = { 보는달 = 이번달; 날고름(오늘) },
                    on변경 = { 시트날 = it; 열린 = 캘시트.변경 },
                    on루틴넣기 = { 시트날 = it; 열린 = 캘시트.루틴 },
                    on록고름 = { rk -> 록뺌 = if (rk in 록뺌) 록뺌 - rk else 록뺌 + rk },
                    on펼침 = { 키 -> 예펼침 = if (예펼침 == 키) null else 키 },
                )
            }
            판단추(상태, 고른날, 록뺌, 시작 = { 시작(it) }, 지움 = { 기록지움(it) }, 보고서 = { kk, rr -> 보고서(kk, rr) })
        }

        // 집어 둔 예정 — 날을 누르면 옮겨진다. 다른 달도 ‹ › 로 넘겨 누른다 (시안 `아래띠`). 뒤로가기 = 취소
        BackHandler(enabled = 집은날 != null && 열린 == null) { 집은날 = null }
        집은날?.let { 원 ->
            아래띠("${캘날글(원)} ${d.예정루틴(원)?.이름 ?: ""} — 옮길 날을 누르세요", "취소", { 집은날 = null })
        }

        val k = 시트날
        val 이날 = if (k == 오늘) "오늘" else "이 날"
        when (열린) {
            // 시안 `시트` '변경' — 다른 루틴으로 · 다른 날로 옮기기 · 휴식 · 예정 지우기
            캘시트.변경 -> 시트("${캘날글(k)} (${캘요일글(k)})", { 열린 = null }) {
                val r = d.예정루틴(k)
                고르기줄("다른 루틴으로", r?.이름) { 열린 = 캘시트.루틴 }
                고르기줄("다른 날로 옮기기") { 집은날 = k; 날고름(k); 열린 = null }
                if (r != null && !r.휴식일) 고르기줄("${이날}은 휴식") { 열린 = 캘시트.휴식 }
                빨간고르기줄("$이날 예정 지우기") {
                    열린 = null
                    val dd = 상태.d
                    val rid = dd.예정[k] ?: return@빨간고르기줄
                    val 이름 = dd.예정루틴(k)?.이름 ?: ""
                    val 전고정 = dd.예정고정[k]
                    상태.바꿈 { it.예정지우기(k, 상태.오늘) }
                    if (상태.d.예정.containsKey(k)) return@빨간고르기줄   // 지울 수 없는 날(지난 날 · 기록 있는 날)
                    발자취.적기("${캘날글(k)} 예정 지움")
                    상태.알림.되돌림("캘린더예정", { n -> if (n > 1) "예정 ${n}개를 지웠습니다" else "${캘날글(k)} $이름 예정을 지웠습니다" }) {
                        상태.바꿈 { dd2 ->
                            if (dd2.예정.containsKey(k) || dd2.캘기록있음(k) || dd2.루틴들.none { it.id == rid }) dd2
                            else dd2.copy(예정 = dd2.예정 + (k to rid), 예정고정 = if (전고정 == null) dd2.예정고정 - k else dd2.예정고정 + (k to 전고정))
                        }
                    }
                }
            }
            // 시안 '루틴고르기' — [이 날만][이 날부터 순서대로] · 루틴마다 예상 시간 · 지금 루틴 ✓
            캘시트.루틴 -> {
                var 이날만 by remember { mutableStateOf(true) }
                시트("${캘날글(k)} 루틴", { 열린 = null }) {
                    칩줄(listOf("이 날만", "이 날부터 순서대로"), if (이날만) "이 날만" else "이 날부터 순서대로", { 이날만 = it == "이 날만" })
                    Box(Modifier.height(간격.좁게))
                    d.루틴들.forEach { r ->
                        val 곁 = (if (r.휴식일) "휴식" else "예상 ${시간글(예상초(d.플랜줄채움(r)))}") + if (r.자동생성) "" else " · 수동"
                        고르기줄(r.이름, 곁, 오른쪽 = if (d.예정[k] == r.id) 아이콘.체크 else null) {
                            열린 = null
                            상태.바꿈 { if (이날만) it.그날만바꾸기(r.id, k, 상태.오늘) else it.꽂기(r.id, k, 상태.오늘) }
                            발자취.적기("${캘날글(k)} → ${r.이름}")
                            상태.알림.토스트("${캘날글(k)} ${r.이름}")
                        }
                    }
                    if (d.루틴들.isEmpty()) 고르기줄("루틴 만들러 가기") { 열린 = null; 루틴으로() }
                }
            }
            // 시안 '휴식' — 미루기 · 건너뛰기, 앞으로 사흘을 미리 보인다
            캘시트.휴식 -> 시트("${이날}은 휴식", { 열린 = null }) {
                listOf(true to "미루기", false to "건너뛰기").forEach { (미루기, 이름) ->
                    고르기줄(이름, "다음 날부터 ${d.사흘미리(미루기, k).joinToString(" → ")}") {
                        열린 = null
                        // 건너뛴 날을 남긴다 (업적 2-11 '하체 날 건너뛰기')
                        상태.바꿈 { if (미루기) it.날휴식(true, k, 상태.오늘) else it.건너뜀남김(k).날휴식(false, k, 상태.오늘) }
                        발자취.적기("${캘날글(k)} 휴식 ($이름)")
                        상태.알림.토스트("${캘날글(k)} 휴식 · $이름")
                    }
                }
            }
            캘시트.달 -> 달고르기(보는달, 이번달, { 보는달 = it; 열린 = null }, { 열린 = null })
            null -> {}
        }

        // 운동 중에 연 운동 보고서 — App 은 운동 중엔 결과를 그리지 않으므로 여기서 덮어 그린다.
        // 결과 화면의 [확인](결과 = null) · 뒤로가기 · 이 화면을 떠나면 닫힌다
        val 보는보고 = 보고
        if (보는보고 != null && 상태.d.결과 === 보는보고 && 상태.d.세션 != null) {
            BackHandler { 보고 = null; 상태.바꿈 { it.copy(결과 = null) } }
            DisposableEffect(보는보고) { onDispose { if (상태.d.결과 === 보는보고) 상태.바꿈 { it.copy(결과 = null) } } }
            Box(Modifier.fillMaxSize().background(c.바탕).눌림 { }) { 결과화면(상태, 보는보고) }
        }
    }
}

/** 기록 지우기 되돌림 띠에 합쳐진 것들 — 띠 글(지운 기록 수)을 만들려고. 띠가 사라지면 다음 지우기에서 비운다 */
private object 캘지움띠 { val 목록 = mutableListOf<캘지움값>() }

// ─────────────── 년월 띠 ───────────────

/** 시안 `.띠.년월띠` — [스탯] · ‹ 2026년 10월 › · [업적]. 년월은 언제나 한가운데, 누르면 달 고르기 */
@Composable
private fun 년월띠(달: YearMonth, 이전: () -> Unit, 다음: () -> Unit, 달고르기: () -> Unit, 스탯: () -> Unit, 업적: () -> Unit) {
    val c = Local색.current
    Box(
        Modifier.fillMaxWidth().heightIn(min = 높이.보통).background(c.강조)
            .padding(horizontal = 간격.보통, vertical = 부품치수.띠세로여백).번호("캘0"),
    ) {
        흰칩("스탯", 스탯, Modifier.align(Alignment.CenterStart).번호("캘칩"))
        Row(Modifier.align(Alignment.Center), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
            아이콘버튼(아이콘.왼쪽, "이전 달", 이전, 칠함 = false, 색 = c.강조글, 크기칸 = 높이.낮게)
            Box(Modifier.heightIn(min = 높이.낮게).clip(RoundedCornerShape(모서리.작게)).눌림(달고르기).padding(horizontal = 간격.아주좁게), contentAlignment = Alignment.Center) {
                띠글("${달.year}년 ${달.monthValue}월")
            }
            아이콘버튼(아이콘.오른쪽, "다음 달", 다음, 칠함 = false, 색 = c.강조글, 크기칸 = 높이.낮게)
        }
        흰칩("업적", 업적, Modifier.align(Alignment.CenterEnd))
    }
}

/** 띠 위 단추 (시안 `.작은흰`) — 흰 상자 · 파란 글씨. 어두운 화면에서는 색표가 뒤집혀 짙은 상자 · 밝은 파랑 글 */
@Composable
private fun 흰칩(글자: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Local색.current
    val 손 = remember { MutableInteractionSource() }
    val 배 = 눌림배율(손)
    Box(
        modifier.height(높이.아주낮게).배율(배).clip(RoundedCornerShape(모서리.작게)).background(c.강조글)
            .눌림손(손, onClick).padding(horizontal = 간격.좁게),
        contentAlignment = Alignment.Center,
    ) { 글(글자, 크기값 = 크기.조금작게, 색 = c.강조, 굵기 = FontWeight.Bold) }
}

// ─────────────── 달력 ───────────────

/** 일 = 나쁨(빨강) · 토 = 강조(파랑) (시안 v8 s48f `주말글`) */
@Composable
private fun 주말색(요일: Int, 보통: Color): Color {
    val c = Local색.current
    return when (요일) { 0 -> c.나쁨; 6 -> c.강조; else -> 보통 }
}

@Composable
private fun 요일줄() {
    val c = Local색.current
    Row(Modifier.fillMaxWidth().padding(vertical = 칸글옆)) {
        "일월화수목금토".forEachIndexed { i, w ->
            글("$w", Modifier.weight(1f), 크기값 = 크기.작게, 색 = 주말색(i, c.옅음), 가운데 = true)
        }
    }
}

/** 날 칸 사이 구분선 — 위 · 왼쪽(첫 칸 빼고) 1 선 (시안 v7 upy8 · v17 ②). 빈 칸에도 그어 반듯한 격자 */
private fun Modifier.칸선(색: Color, 첫칸: Boolean): Modifier = drawBehind {
    val w = 선굵기.보통.toPx()
    drawLine(색, Offset(0f, w / 2), Offset(size.width, w / 2), w)
    if (!첫칸) drawLine(색, Offset(w / 2, 0f), Offset(w / 2, size.height), w)
}

@Composable
private fun 달력(
    d: 앱데이터, 오늘: String, 달: YearMonth, 고른날: String, 집은날: String?, modifier: Modifier,
    on누름: (String) -> Unit, on옮김: (String, String) -> Unit, on집음: (String, Int) -> Unit, on달넘김: (Int) -> Unit,
) {
    val c = Local색.current
    val 첫 = 달.atDay(1)
    val 앞빈칸 = 첫.dayOfWeek.value % 7          // 일요일 = 0
    val 줄수 = (앞빈칸 + 달.lengthOfMonth() + 6) / 7
    // 꾹 눌러 옮기기 (2-3) — 예정만 있는 날(오늘 이후 · 기록 없음)을 꾹 눌러 다른 날에 놓는다
    var 끄는날 by remember { mutableStateOf<String?>(null) }
    var 놓을날 by remember { mutableStateOf<String?>(null) }
    var 손 by remember { mutableStateOf(Offset.Zero) }
    val 판데이터 by rememberUpdatedState(d)
    val 옮김 by rememberUpdatedState(on옮김)
    val 집음 by rememberUpdatedState(on집음)
    val 햅틱 = LocalHapticFeedback.current
    // 끄는 중에 달이 넘어가도 끌기가 끊기지 않게 — pointerInput 은 '오늘' 에만 묶고 칸 계산은 늘 지금 보이는 달로 (10-02)
    val 지금달 by rememberUpdatedState(달)
    val 달넘김 by rememberUpdatedState(on달넘김)
    val 범위 = rememberCoroutineScope()
    var 달타이머 by remember { mutableStateOf<Job?>(null) }
    var 타이머방향 by remember { mutableIntStateOf(0) }
    var 끌며넘김 by remember { mutableStateOf(false) }
    fun 칸날(p: Offset, 너비: Float, 줄px: Float): String? {
        val 달0 = 지금달
        val 앞0 = 달0.atDay(1).dayOfWeek.value % 7
        val 줄수0 = (앞0 + 달0.lengthOfMonth() + 6) / 7
        if (p.x < 0 || p.y < 0 || 너비 <= 0f) return null
        val 칸 = (p.x / (너비 / 7f)).toInt().coerceAtMost(6)
        val 줄 = (p.y / 줄px).toInt()
        val n = 줄 * 7 + 칸 - 앞0 + 1
        return if (줄 >= 줄수0 || n < 1 || n > 달0.lengthOfMonth()) null else 달0.atDay(n).toString()
    }
    /** 달력 위(요일 · 년월 띠)의 왼쪽(−1) · 오른쪽(+1) 1/3 위인가 — 0 = 아님 */
    fun 띠방향(p: Offset, 너비: Float): Int = if (p.y >= 0) 0 else if (p.x > 너비 * 2f / 3f) 1 else if (p.x < 너비 / 3f) -1 else 0
    fun 타이머끄기() { 달타이머?.cancel(); 달타이머 = null; 타이머방향 = 0 }
    fun 놓을수있음(원: String, k: String?) = k != null && 판데이터.캘옮길수있음(원, k, 오늘)
    Box(modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().pointerInput(오늘) {
            val 줄px = 칸높이.toPx()
            detectDragGesturesAfterLongPress(
                onDragStart = { p ->
                    끌며넘김 = false
                    val k = 칸날(p, size.width.toFloat(), 줄px)
                    if (k != null && k >= 오늘 && !판데이터.캘기록있음(k) && 판데이터.예정[k] != null) {
                        끄는날 = k; 놓을날 = k; 손 = p; 햅틱.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                },
                onDrag = { ch, _ ->
                    if (끄는날 != null) {
                        ch.consume(); 손 = ch.position; 놓을날 = 칸날(ch.position, size.width.toFloat(), 줄px)
                        // 띠 좌·우 1/3 에 0.6초 머무르면 끄는 중에 달이 넘어간다 — 손을 떼지 않고 다른 달 날짜에 놓는다
                        val 방 = 띠방향(ch.position, size.width.toFloat())
                        if (방 == 0) 타이머끄기()
                        else if (달타이머 == null || 타이머방향 != 방) {
                            타이머끄기(); 타이머방향 = 방
                            달타이머 = 범위.launch {
                                delay(달넘김머무름)
                                if (끄는날 != null) { 달넘김(방); 끌며넘김 = true }
                                달타이머 = null; 타이머방향 = 0
                            }
                        }
                    }
                },
                onDragEnd = {
                    타이머끄기()
                    val 원 = 끄는날; val 새 = 놓을날
                    if (원 != null && 새 != null && 새 != 원) 옮김(원, 새)
                    // 그 자리에서 뗐거나 달력 밖(위 띠)에서 뗐으면 '집어 둔' 상태 → 다른 달로 넘겨 누르면 옮겨진다.
                    // 띠 오른쪽에서 떼면 다음 달로, 왼쪽에서 떼면 이전 달로 (끄는 중에 이미 넘겼으면 또 넘기지 않는다)
                    else if (원 != null) 집음(원, if (끌며넘김) 0 else 띠방향(손, size.width.toFloat()))
                    끄는날 = null; 놓을날 = null; 끌며넘김 = false
                },
                onDragCancel = { 타이머끄기(); 끄는날 = null; 놓을날 = null; 끌며넘김 = false },
            )
        }) {
            for (줄 in 0 until 줄수) {
                Row(Modifier.fillMaxWidth().height(칸높이)) {
                    for (칸 in 0 until 7) {
                        val n = 줄 * 7 + 칸 - 앞빈칸 + 1
                        val 칸모양 = Modifier.weight(1f).fillMaxHeight()
                        if (n < 1 || n > 달.lengthOfMonth()) { Box(칸모양.칸선(c.선, 칸 == 0)); continue }
                        val k = 달.atDay(n).toString()
                        val 원 = 끄는날
                        val 표시 = when {
                            원 == null -> 0
                            k == 원 -> 1
                            k == 놓을날 -> if (놓을수있음(원, k)) 2 else 3
                            else -> 0
                        }
                        날칸(d.캘칸내용(k), n, 칸, k == 오늘, k == 고른날, k == 집은날, 표시, 칸모양) { on누름(k) }
                    }
                }
            }
        }
        // 손가락을 따라다니는 루틴 이름 (시안 `.유령` — 좋음 바탕 · 13 굵게 · 손가락 위)
        val 끄는 = 끄는날
        if (끄는 != null) {
            var 크기px by remember { mutableStateOf(IntOffset(0, 0)) }
            Box(
                Modifier
                    .offset { IntOffset((손.x - 크기px.x / 2f).toInt(), (손.y - 크기px.y * 1.4f).toInt()) }
                    .onSizeChanged { 크기px = IntOffset(it.width, it.height) }
                    .clip(CircleShape).background(c.좋음)
                    .padding(horizontal = 간격.보통, vertical = 간격.아주좁게),
            ) { 글(d.예정루틴(끄는)?.이름 ?: "옮기기", 크기값 = 크기.조금작게, 색 = c.강조글, 굵기 = FontWeight.Bold) }
        }
    }
}

/**
 * 날 칸 (시안 `.칸날`) — 날짜(13 · 오늘은 강조 알약) / 루틴 이름(11 · 예정 = 강조옅음 바탕 · 휴식 = 속선 테두리) / 상태(달성 · 미달성 · 미실시).
 * 고른 날 = 강조 테두리 + 강조옅음 바탕. 끄는 중: 1 = 들어 올린 날(흐림) · 2 = 놓을 수 있음(강조 바탕) · 3 = 못 놓음(빨간 테두리). 집어 둔 날 = 점선
 */
@Composable
private fun 날칸(값: 캘칸값?, 일: Int, 요일: Int, 오늘임: Boolean, 고름: Boolean, 집음: Boolean, 끌기표시: Int, modifier: Modifier, onClick: () -> Unit) {
    val c = Local색.current
    val 누름 by rememberUpdatedState(onClick)
    val 놓기 = 끌기표시 == 2
    val 테두리 by animateColorAsState(if (고름) c.강조 else Color.Transparent, tween(움직임.색), label = "고른날")
    val 바탕 by animateColorAsState(when { 놓기 -> c.강조; 고름 -> c.강조옅음; else -> Color.Transparent }, tween(움직임.색), label = "고른날바탕")
    val 강조글 = c.강조글
    Column(
        modifier
            .alpha(when { 끌기표시 == 1 -> 움직임.끌림투명; 집음 -> 집음투명; else -> 1f })
            .칸선(c.선, 요일 == 0)
            .background(바탕)
            .border(선굵기.보통, 테두리)
            .then(if (끌기표시 == 3) Modifier.border(선굵기.굵게, c.나쁨) else Modifier)
            .then(if (집음) Modifier.drawBehind {
                val w = 선굵기.굵게.toPx()
                drawRect(c.강조, style = Stroke(w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(w * 3, w * 2))))
            } else Modifier)
            // 칸 전체가 누르는 곳 — 손이 닿는 순간이 아니라 뗄 때 고른다 (꾹 눌러 끌기와 겹치지 않게)
            .pointerInput(Unit) { detectTapGestures(onTap = { 누름() }) }
            .padding(vertical = 간격.아주좁게, horizontal = 칸글옆),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val 숫자색 = if (놓기) 강조글 else 주말색(요일, c.글)
        if (오늘임) Box(Modifier.clip(RoundedCornerShape(모서리.작게)).background(if (놓기) 강조글 else c.강조).padding(horizontal = 간격.아주좁게)) {
            Text("$일", style = 글꼴.보통(크기.조금작게, FontWeight.Bold), color = if (놓기) c.강조 else 강조글, maxLines = 1)
        } else Text("$일", style = 글꼴.보통(크기.조금작게, FontWeight.Bold), color = 숫자색, maxLines = 1)
        if (값 != null) {
            val (칩바탕, 칩글) = when (값.종류) {
                캘칸종류.예 -> c.강조옅음 to c.강조
                캘칸종류.휴 -> Color.Transparent to c.옅음
                else -> Color.Transparent to (if (놓기) 강조글 else c.글)
            }
            Box(
                Modifier.clip(RoundedCornerShape(모서리.작게)).background(칩바탕)
                    .then(if (값.종류 == 캘칸종류.휴) Modifier.border(선굵기.보통, c.속선, RoundedCornerShape(모서리.작게)) else Modifier)
                    .padding(horizontal = 칸글옆),
            ) {
                Text(값.글, style = 글꼴.보통(크기.작게), color = 칩글, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
            }
            if (값.상 != null) {
                val 상색 = when { 놓기 -> 강조글; 값.상 == "달성" -> c.좋음; 값.상 == "미달성" -> c.나쁨; else -> c.옅음 }
                Text(값.상, style = 글꼴.보통(크기.작게, FontWeight.Bold), color = 상색, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

// ─────────────── 날짜 판 ───────────────

/** 위쪽 가는 선 + 위 8 — 판 안 이름 줄 사이 (시안 `.예머리` border-top · padding-top) */
private fun Modifier.윗선(켬: Boolean, 색: Color): Modifier =
    if (!켬) this else this.drawBehind { drawLine(색, Offset(0f, 0f), Offset(size.width, 0f), 선굵기.보통.toPx()) }.padding(top = 간격.좁게)

/**
 * 날짜 판 (시안 `날판`) — 띠 '10월 5일 (월) · 오늘' [오늘][변경 | 루틴 넣기] /
 * 기록마다 이름 줄([체크][이름][달성][세트][볼륨][시간]) + 두 칸 종목 목록 / 미실시 / 예정 이름 줄([이름][세트][볼륨][예상]) + 목록.
 * 단추 줄은 화면 맨 아래 [판단추] (넘겨도 붙박이)
 */
@Composable
private fun 날판(
    상태: 앱상태, k: String, 다른달: Boolean, 록뺌: Set<String>, 예펼침: String?,
    on오늘: () -> Unit, on변경: (String) -> Unit, on루틴넣기: (String) -> Unit, on록고름: (String) -> Unit, on펼침: (String) -> Unit,
) {
    val c = Local색.current
    val d = 상태.d
    val 오늘 = 상태.오늘
    val 록 = d.캘기록목록(k)
    val 예 = k >= 오늘 && !(k == 오늘 && 록.isNotEmpty())
    val r = d.예정루틴(k)
    Column(Modifier.fillMaxWidth().번호("캘1")) {
        // ── 띠 — 오른쪽 끝: 예정이 있으면 [변경] · 없으면 [루틴 넣기] · 기록만 있는 날은 비움. 다른 달을 볼 때 [오늘] ──
        Row(
            Modifier.fillMaxWidth().heightIn(min = 높이.보통).background(c.강조).padding(horizontal = 간격.보통, vertical = 부품치수.띠세로여백),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.좁게),
        ) {
            띠글("${캘날글(k)} (${캘요일글(k)})${if (k == 오늘) " · 오늘" else ""}", Modifier.weight(1f))
            if (다른달) 흰칩("오늘", on오늘)
            if (예) { if (r != null) 흰칩("변경", { on변경(k) }) else 흰칩("루틴 넣기", { on루틴넣기(k) }) }
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 간격.보통, vertical = 간격.좁게), verticalArrangement = Arrangement.spacedBy(간격.좁게)) {
            var 앞것 = false
            록.forEach { (rk, rec) ->
                기록머리(d, rec, 여럿 = 록.size > 1, 켬 = rk !in 록뺌, 선위 = 앞것) { on록고름(rk) }
                val 키 = "록$rk"
                종목목록(기록줄들(d, rec), 예펼침 == 키) { on펼침(키) }
                앞것 = true
            }
            if (록.isEmpty()) d.미실시[k]?.let { 이름 ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
                    글("미실시", Modifier.width(판이름폭), 크기값 = 크기.작게, 색 = c.옅음)
                    글(이름, Modifier.weight(1f), 굵기 = FontWeight.Bold)
                }
                앞것 = true
            }
            if (예) {
                if (r != null) {
                    val 실 = d.보일루틴(k, r)
                    예머리(d, 실, 선위 = 앞것)
                    if (!실.휴식일) {
                        val 키 = "예$k"
                        종목목록(실.종목.map { e -> e.이름 to 곁글(listOf("${e.세트}세트", 캘세트글((0 until e.세트).map { e.목표(it) }))) }, 예펼침 == 키) { on펼침(키) }
                    }
                } else Row(Modifier.fillMaxWidth().윗선(앞것, c.선)) { 글("예정 없음", 크기값 = 크기.조금작게, 색 = c.흐림) }
            }
        }
    }
}

/** 날짜 판에 보이는 그 날 루틴 — 플랜 줄은 지금 회차 처방으로, 그 날만 조절이 있으면 적용 (운동 시작 `조절해시작` 과 같은 차례) */
private fun 앱데이터.보일루틴(k: String, r: 루틴): 루틴 = 플랜줄채움(r).조절적용(조절[k], 설정.무게폭)

/** 칸 곁 글 — 조각 안에서는 줄을 바꾸지 않고 '·' 에서만 바꾼다 (시안 `.숫>span{white-space:nowrap}`) */
private fun 곁글(조각: List<String>): String = 조각.filter { it.isNotBlank() }.joinToString(" · ") { it.replace(' ', ' ') }

/** 기록의 종목 줄 — '4/5세트 · 60kg × 9회'. 워밍업은 세지 않는다. 계획을 모르면(루틴이 바뀌었거나 넣은 종목) '4세트' */
private fun 기록줄들(d: 앱데이터, rec: 날기록): List<Pair<String, String>> {
    val 짝 = d.루틴들.firstOrNull { it.id == rec.루틴id }
    return rec.종목들.map { e ->
        val 세 = e.세트들.filter { it.종류 != 세트종류.워밍업 }
        val 계획 = if (e.임시) null else 짝?.종목?.firstOrNull { 캘같은종목(it.종id, it.이름, e.종id, e.이름) }?.세트
        val 수 = if (계획 != null) "${세.size}/${max(계획, 세.size)}세트" else "${세.size}세트"
        e.이름 to 곁글(listOf(수, 캘세트글(세)))
    }
}

/** 시안 `.예칩 span` — 높이 28 · 속선 · 11 흐림 */
@Composable
private fun 판칩(글자: String) {
    val c = Local색.current
    Box(
        Modifier.height(높이.아주낮게).border(선굵기.보통, c.속선, RoundedCornerShape(모서리.작게)).padding(horizontal = 간격.좁게),
        contentAlignment = Alignment.Center,
    ) { Text(글자, style = 글꼴.보통(크기.작게).copy(fontFeatureSettings = "tnum"), color = c.흐림, maxLines = 1, softWrap = false) }
}

/** 시안 `.알약.달성` (좋음옅음 바탕 · 좋음) / `.알약.미달성` (나쁨 테두리 · 나쁨) — 11 굵게 */
@Composable
private fun 기록알약(달성: Boolean) {
    val c = Local색.current
    val 모양 = RoundedCornerShape(모서리.작게)
    Box(
        Modifier.clip(모양).then(if (달성) Modifier.background(c.좋음옅음) else Modifier.border(선굵기.보통, c.나쁨, 모양)).padding(horizontal = 간격.아주좁게),
    ) { Text(if (달성) "달성" else "미달성", style = 글꼴.보통(크기.작게, FontWeight.Bold), color = if (달성) c.좋음 else c.나쁨, maxLines = 1) }
}

/** 기록 고르기 체크 (시안 v17 `.체크.록고름`) — 세트 완료 체크와 같은 28 동그라미. 켜면 강조 바탕 · 체크 */
@Composable
private fun 록체크(켬: Boolean, 이름: String, onClick: () -> Unit) {
    val c = Local색.current
    val 바탕 = 색움직(if (켬) c.강조 else Color.Transparent, "록체크")
    val 테 = 색움직(if (켬) c.강조 else c.속선, "록체크테")
    Box(
        Modifier.size(높이.아주낮게).clip(CircleShape).background(바탕).border(선굵기.굵게, 테, CircleShape).눌림(onClick)
            .semanticsDesc("$이름 기록 ${if (켬) "지우기에서 빼기" else "지우기에 넣기"}"),
        contentAlignment = Alignment.Center,
    ) { if (켬) Icon(아이콘.체크, null, Modifier.size(그림작게), tint = c.강조글) }
}

private fun Modifier.semanticsDesc(글: String): Modifier = this.semantics { contentDescription = 글 }

/** 기록 이름 줄 (시안 `기록머리`) — [체크(여럿일 때)][이름 15 굵게 · 40% 까지][달성 알약][세트][볼륨][시간] */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun 기록머리(d: 앱데이터, rec: 날기록, 여럿: Boolean, 켬: Boolean, 선위: Boolean, on고름: () -> Unit) {
    val c = Local색.current
    val 량 = 운동량(rec)
    val 짝 = d.루틴들.firstOrNull { it.id == rec.루틴id }
    val 계획 = 짝?.let { r -> if (r.휴식일) null else 총세트(r) }
    // 달성이면 '13세트'(알약이 이미 다 했다고 말한다) · 미달성이면 '10/13세트'
    val 세트글 = if (!rec.달성 && 계획 != null && 계획 > 량.세트) "${량.세트}/${계획}세트" else "${량.세트}세트"
    BoxWithConstraints(Modifier.fillMaxWidth().윗선(선위, c.선)) {
        val 이름최대 = maxWidth * 이름폭비
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
            if (여럿) 록체크(켬, rec.루틴이름, on고름)
            Text(rec.루틴이름, Modifier.widthIn(max = 이름최대), style = 글꼴.보통(크기.본문, FontWeight.Bold), color = c.글, maxLines = 1, overflow = TextOverflow.Ellipsis)
            기록알약(rec.달성)
            FlowRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(간격.아주좁게), verticalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                판칩(세트글)
                판칩("볼륨 ${콤마(량.볼륨)}kg")
                if (rec.걸린초 > 0) 판칩(시간글(rec.걸린초))
            }
        }
    }
}

/** 예정 이름 줄 (시안 `.예머리`) — [이름][세트][볼륨][예상] · 휴식일이면 [휴식일] */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun 예머리(d: 앱데이터, r: 루틴, 선위: Boolean) {
    val c = Local색.current
    BoxWithConstraints(Modifier.fillMaxWidth().윗선(선위, c.선)) {
        val 이름최대 = maxWidth * 이름폭비
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
            Text(r.이름, Modifier.widthIn(max = 이름최대), style = 글꼴.보통(크기.본문, FontWeight.Bold), color = c.글, maxLines = 1, overflow = TextOverflow.Ellipsis)
            FlowRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(간격.아주좁게), verticalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                if (r.휴식일) 판칩("휴식일") else {
                    // 볼륨 — 맨몸 0kg 은 체중의 60% 로 센다 (시안 `유효무게` · 근육 계산과 같은 값)
                    val 볼 = r.종목.sumOf { e -> (0 until e.세트).sumOf { i -> e.목표(i).let { s -> 근육계산.유효무게(s.w, d.몸.체중) * s.r } } }
                    판칩("${총세트(r)}세트")
                    판칩("볼륨 ${콤마(볼)}kg")
                    판칩("예상 ${시간글(예상초(r))}")
                }
            }
        }
    }
}

/**
 * 두 칸 종목 목록 (시안 `날판틀` · v8 avk4) — 왼쪽 칸을 위→아래로 채운 뒤 오른쪽 칸. 칸 = 번호 · 이름(한 줄 …) / 흐린 세트 글.
 * 10개 넘으면 9개 + '외 N종목 ∨' (누르면 다 펼치고 '접기 ∧')
 */
@Composable
private fun 종목목록(줄들: List<Pair<String, String>>, 펼침: Boolean, on펼침: () -> Unit) {
    if (줄들.isEmpty()) return
    val c = Local색.current
    val 배 = 캘판배치(줄들.size, 펼침)
    val 칸수 = 배.보일 + if (배.접기칸) 1 else 0
    @Composable fun 칸(i: Int) {
        if (i < 배.보일) 목록칸(i, 줄들[i].first, 줄들[i].second) else 접기칸(펼침, 줄들.size - (캘판칸 - 1), on펼침)
    }
    Row(
        Modifier.fillMaxWidth().drawBehind { drawLine(c.선, Offset(0f, 0f), Offset(size.width, 0f), 선굵기.보통.toPx()) },
        horizontalArrangement = Arrangement.spacedBy(간격.보통),
    ) {
        Column(Modifier.weight(1f)) { for (i in 0 until 배.행) 칸(i) }
        Column(Modifier.weight(1f)) { for (i in 배.행 until 칸수) 칸(i) }
    }
}

private fun Modifier.아랫선(색: Color): Modifier = drawBehind {
    val w = 선굵기.보통.toPx()
    drawLine(색, Offset(0f, size.height - w / 2), Offset(size.width, size.height - w / 2), w)
}

@Composable
private fun 목록칸(i: Int, 이름: String, 곁: String) {
    val c = Local색.current
    Row(Modifier.fillMaxWidth().아랫선(c.선).padding(vertical = 간격.아주좁게), horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
        Text("${i + 1}", Modifier.width(간격.넓게), style = 글꼴.보통(크기.작게, FontWeight.Bold), color = c.옅음, textAlign = TextAlign.Center, maxLines = 1)
        Column(Modifier.weight(1f)) {
            Text(이름, style = 글꼴.보통(크기.조금작게), color = c.글, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (곁.isNotEmpty()) Text(곁, style = 글꼴.보통(크기.작게).copy(fontFeatureSettings = "tnum"), color = c.흐림, maxLines = 2)
        }
    }
}

@Composable
private fun 접기칸(펼침: Boolean, 남은: Int, onClick: () -> Unit) {
    val c = Local색.current
    Row(
        Modifier.fillMaxWidth().heightIn(min = 높이.낮게).아랫선(c.선).눌림(onClick),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.아주좁게),
    ) {
        Box(Modifier.width(간격.넓게))
        글(if (펼침) "접기" else "외 ${남은}종목", Modifier.weight(1f), 크기값 = 크기.조금작게, 색 = c.강조, 굵기 = FontWeight.Bold)
        Icon(아이콘.아래, if (펼침) "접기" else "펼치기", Modifier.size(그림보통).rotate(if (펼침) 180f else 0f), tint = c.강조)
    }
}

private val 그림보통 = 18.dp   // 11 지침 U3-6 기본 아이콘
private val 그림작게 = 16.dp   // 11 지침 U3-6 글 옆 · 체크 안

/**
 * 맨 아래 단추 줄 (시안 `.판단추` — 넘겨도 아래에 붙박이 · 높이 40).
 *  · 기록 날: [운동 보고서] [한 번 더(오늘 · 마지막 기록의 루틴)] [운동 기록 삭제(빨강)] — 여럿인데 체크가 다 꺼졌으면 삭제는 흐리게
 *  · 오늘 예정: [운동 시작]
 */
@Composable
private fun 판단추(
    상태: 앱상태, k: String, 록뺌: Set<String>,
    시작: (운동세션?) -> Unit, 지움: (String) -> Unit, 보고서: (String, 날기록) -> Unit,
) {
    val c = Local색.current
    val d = 상태.d
    val 오늘 = 상태.오늘
    val 록 = d.캘기록목록(k)
    val r = d.예정루틴(k)
    val 예시작 = 록.isEmpty() && k == 오늘 && r != null && !r.휴식일
    if (록.isEmpty() && !예시작) return
    Row(
        Modifier.fillMaxWidth().background(c.바탕).padding(horizontal = 간격.보통, vertical = 간격.좁게),
        horizontalArrangement = Arrangement.spacedBy(간격.좁게),
    ) {
        if (록.isNotEmpty()) {
            val 끝 = 록.last().second
            버튼("운동 보고서", { 상태.d.캘기록목록(k).lastOrNull()?.let { (kk, rr) -> 보고서(kk, rr) } }, Modifier.weight(1f).번호("캘3"), 작게 = true)
            val 다시 = if (k == 오늘) d.루틴들.firstOrNull { it.id == 끝.루틴id }?.takeIf { !it.휴식일 } else null
            if (다시 != null) 버튼("한 번 더", {
                // 측정일이면 플랜 줄 앞에 워밍업 (20 B-3 · 조절해시작과 같은 함수). 저장하면 그 날 '~2' 기록으로 따로 남는다
                val dd = 상태.d
                val rr = dd.루틴들.firstOrNull { it.id == 다시.id }
                시작(rr?.let { 운동시작(dd.측정워밍업붙임(dd.플랜줄채움(it)), System.currentTimeMillis()) })
            }, Modifier.weight(1f), 주요 = true, 작게 = true)
            val 꺼짐 = 록.size > 1 && 록.all { it.first in 록뺌 }
            버튼("운동 기록 삭제", { if (!꺼짐) 지움(k) }, Modifier.weight(1f).alpha(if (꺼짐) 꺼짐투명 else 1f), 작게 = true, 글색 = c.나쁨)
        } else if (r != null) {
            버튼("운동 시작", {
                val dd = 상태.d
                val 지금r = dd.예정루틴(k)
                if (지금r == null || 지금r.휴식일) return@버튼
                시작(dd.조절해시작(지금r, k, System.currentTimeMillis()))
            }, Modifier.weight(1f).번호("캘3"), 주요 = true, 작게 = true)
        }
    }
}

// ─────────────── 시트 ───────────────

/** 지우기처럼 빨간 고르기 줄 (시안 `.고르기.나쁨` — › 없음) */
@Composable
private fun 빨간고르기줄(이름: String, onClick: () -> Unit) {
    val c = Local색.current
    val 손 = remember { MutableInteractionSource() }
    val 배 = 눌림배율(손)
    Row(Modifier.fillMaxWidth().배율(배).눌림손(손, onClick).padding(vertical = 간격.보통), verticalAlignment = Alignment.CenterVertically) {
        글(이름, Modifier.weight(1f), 색 = c.나쁨)
    }
    구분선()
}

/**
 * 달 고르기 (시안 v9 xz0l `달고르기시트` · v17 `년고르기`) — ‹ 2026년 › (해 넘기기) · 1~12월 3×4.
 * '2026년' 을 누르면 12해 격자(‹ › 는 12해씩) → 해를 고르면 다시 달. 보는 달 · 해 = 고른 표시, 이번 달 · 올해 = 오늘 표시
 */
@Composable
private fun 달고르기(보는달: YearMonth, 이번달: YearMonth, 고름: (YearMonth) -> Unit, 닫기: () -> Unit) {
    var 해 by remember { mutableIntStateOf(보는달.year) }
    var 해보기 by remember { mutableStateOf(false) }
    var 해시작 by remember { mutableIntStateOf(보는달.year - 5) }
    시트(if (해보기) "해 고르기" else "달 고르기", 닫기) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(간격.아주좁게, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
            val 이전 = if (해보기) "이전 12년" else "이전 해"
            val 다음 = if (해보기) "다음 12년" else "다음 해"
            아이콘버튼(아이콘.왼쪽, 이전, {
                if (해보기) 해시작 = (해시작 - 해칸수).coerceIn(해범위.first, 해범위.last - 해칸수 + 1) else 해 = (해 - 1).coerceIn(해범위)
            }, 칠함 = false, 색 = Local색.current.강조)
            Box(
                Modifier.heightIn(min = 높이.보통).clip(RoundedCornerShape(모서리.작게))
                    .눌림 { if (!해보기) 해시작 = (해 - 5).coerceIn(해범위.first, 해범위.last - 해칸수 + 1); 해보기 = !해보기 }
                    .padding(horizontal = 간격.좁게),
                contentAlignment = Alignment.Center,
            ) { 글(if (해보기) "${해시작}~${해시작 + 해칸수 - 1}" else "${해}년", 크기값 = 크기.크게, 굵기 = FontWeight.Bold) }
            아이콘버튼(아이콘.오른쪽, 다음, {
                if (해보기) 해시작 = (해시작 + 해칸수).coerceIn(해범위.first, 해범위.last - 해칸수 + 1) else 해 = (해 + 1).coerceIn(해범위)
            }, 칠함 = false, 색 = Local색.current.강조)
        }
        Box(Modifier.height(간격.좁게))
        if (해보기) 고르기칸들((0 until 해칸수).map { "${해시작 + it}" }, (0 until 해칸수).indexOfFirst { 해시작 + it == 보는달.year }, (0 until 해칸수).indexOfFirst { 해시작 + it == 이번달.year }) { i ->
            해 = 해시작 + i; 해보기 = false
        } else 고르기칸들((1..12).map { "${it}월" }, if (보는달.year == 해) 보는달.monthValue - 1 else -1, if (이번달.year == 해) 이번달.monthValue - 1 else -1) { i ->
            고름(YearMonth.of(해, i + 1))
        }
    }
}

/** 3 × 4 고르기 칸 (시안 `.달칸`) — 높이 44 · 속선 · 면 · 15. 고름 = 강조 테두리 · 강조옅음 · 굵게, 오늘 = 강조 알약 */
@Composable
private fun 고르기칸들(글들: List<String>, 고름: Int, 오늘: Int, on: (Int) -> Unit) {
    val c = Local색.current
    val 모양 = RoundedCornerShape(모서리.작게)
    Column(verticalArrangement = Arrangement.spacedBy(간격.좁게)) {
        글들.chunked(3).forEachIndexed { 줄, 셋 ->
            Row(horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
                셋.forEachIndexed { j, t ->
                    val i = 줄 * 3 + j
                    val 고른 = i == 고름
                    Box(
                        Modifier.weight(1f).height(높이.높게).clip(모양).background(if (고른) c.강조옅음 else c.면)
                            .border(선굵기.보통, if (고른) c.강조 else c.속선, 모양).눌림 { on(i) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (i == 오늘) Box(Modifier.clip(모양).background(c.강조).padding(horizontal = 간격.좁게)) {
                            글(t, 색 = c.강조글, 굵기 = FontWeight.Bold)
                        } else 글(t, 굵기 = if (고른) FontWeight.Bold else FontWeight.Medium)
                    }
                }
            }
        }
    }
}

// ─────────────── 다른 화면이 쓰는 것 (이름 · 매개변수 그대로 둔다) ───────────────

/** "가슴날, 3개월 동안 18% 성장했습니다" — 오르면 빨강 내리면 파랑 (6-3) */
@Composable
fun 성장줄(대상: String?, 결과: 대비결과?, 기간: String, modifier: Modifier = Modifier) {
    val c = Local색.current
    val 글자 = buildAnnotatedString {
        // 대상이 null 이면 이름 없이 — 바로 위에 이름이 이미 있을 때 (09-21 메모: 이름이 두 번 나왔다)
        if (대상 != null) withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = c.글)) { append(대상) }
        val p = 결과?.볼륨?.pct
        if (p == null) { append(if (대상 != null) " · $기간 비교 기록 없음" else "$기간 비교 기록 없음"); return@buildAnnotatedString }
        append(if (대상 != null) ", $기간 " else "$기간 ")
        when {
            p > 0 -> { withStyle(SpanStyle(color = c.오름, fontWeight = FontWeight.Bold)) { append("${p}% 성장") }; append("했습니다") }
            p < 0 -> { withStyle(SpanStyle(color = c.내림, fontWeight = FontWeight.Bold)) { append("${-p}% 줄었") }; append("습니다") }
            else -> append("변화가 없습니다")
        }
    }
    Text(글자, modifier, style = 글꼴.보통(크기.버튼), color = c.흐림, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

/**
 * 향상도 한 줄 (09-26 시안 ① · 09-27) — "1RM 95kg [1주 대비 ▲ 5kg] [최고 대비 ▼ 5kg]"
 * 왼쪽 = 1주(지난 7일 중 가장 좋은 날), 오른쪽 = 최고(지난 기록 전부). 차이는 kg. 오르면 빨강 ▲, 내리면 중심색 ▼ (6-3)
 * 견줄 것이 없으면 그 칩은 나타나지 않는다. 칩 숫자는 움직인다 (영부터 · 빠르게 → 움직수)
 */
@Composable
fun 향상줄(앞글: String, 값: 두대비?, modifier: Modifier = Modifier, 영부터: Boolean = false, 빠르게: Boolean = false, 열쇠: Any? = null) {
    val c = Local색.current
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(앞글, Modifier.weight(1f, fill = false), style = 글꼴.보통(크기.작게), color = c.흐림, maxLines = 1, overflow = TextOverflow.Ellipsis)
        대비칩("1주 대비", 값?.주, 영부터, 빠르게, 열쇠)
        대비칩("최고 대비", 값?.최고, 영부터, 빠르게, 열쇠)
    }
}

@Composable
fun 알약(글자: String, 색: Color) {
    Box(Modifier.clip(CircleShape).background(색.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
        글(글자, 크기값 = 크기.작게, 색 = 색, 굵기 = FontWeight.Bold)
    }
}
