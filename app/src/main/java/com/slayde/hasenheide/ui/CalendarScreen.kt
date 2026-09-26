package com.slayde.hasenheide.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.RowScope
import com.slayde.hasenheide.ui.theme.높이
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.IntOffset
import com.slayde.hasenheide.data.예정옮기기
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.slayde.hasenheide.data.대비결과
import com.slayde.hasenheide.data.비교
import com.slayde.hasenheide.data.루틴향상
import com.slayde.hasenheide.data.루틴최근향상
import com.slayde.hasenheide.data.두대비
import com.slayde.hasenheide.data.kg글
import com.slayde.hasenheide.data.날짜만
import com.slayde.hasenheide.data.한번더기록
import com.slayde.hasenheide.data.루틴
import com.slayde.hasenheide.data.루틴성장
import com.slayde.hasenheide.data.볼륨
import com.slayde.hasenheide.data.사흘미리
import com.slayde.hasenheide.data.시간글
import com.slayde.hasenheide.data.앱데이터
import com.slayde.hasenheide.data.예상초
import com.slayde.hasenheide.data.예정루틴
import com.slayde.hasenheide.data.오늘휴식
import com.slayde.hasenheide.data.운동시작
import com.slayde.hasenheide.data.정식세트
import com.slayde.hasenheide.data.지금기준
import com.slayde.hasenheide.data.총세트
import com.slayde.hasenheide.data.꽂기
import com.slayde.hasenheide.data.콤마
import com.slayde.hasenheide.data.시분초
import com.slayde.hasenheide.data.대비
import com.slayde.hasenheide.data.예정초기화
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.글꼴
import com.slayde.hasenheide.ui.theme.모서리
import com.slayde.hasenheide.ui.theme.크기
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

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
 * 향상도 한 줄 (09-26 시안 ①) — "1RM 95kg [1주 ▲5kg] [최고 ▼5kg]"
 * 왼쪽 = 1주(지난 7일 중 가장 좋은 날), 오른쪽 = 최고(지난 기록 전부). 차이는 kg. 오르면 빨강 ▲, 내리면 파랑 ▼ (6-3)
 */
@Composable
fun 향상줄(앞글: String, 값: 두대비?, modifier: Modifier = Modifier) {
    val c = Local색.current
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(앞글, Modifier.weight(1f, fill = false), style = 글꼴.보통(크기.작게), color = c.흐림, maxLines = 1, overflow = TextOverflow.Ellipsis)
        대비칩("1주", 값?.주)
        대비칩("최고", 값?.최고)
    }
}

@Composable
private fun 대비칩(이름: String, v: 비교?) {
    val c = Local색.current
    Row(
        Modifier.border(1.dp, c.선, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        글(이름, 크기값 = 크기.아주작게, 색 = c.옅음)
        Box(Modifier.width(3.dp))
        when {
            v == null -> 글("—", 크기값 = 크기.아주작게, 색 = c.옅음)
            v.diff > 0.05 -> 글("▲${kg글(v.diff)}kg", 크기값 = 크기.아주작게, 색 = c.오름, 굵기 = FontWeight.Bold)
            v.diff < -0.05 -> 글("▼${kg글(v.diff)}kg", 크기값 = 크기.아주작게, 색 = c.내림, 굵기 = FontWeight.Bold)
            else -> 글("같음", 크기값 = 크기.아주작게, 색 = c.흐림, 굵기 = FontWeight.Bold)
        }
    }
}

/** 오늘 칸을 두 번 누르면 시작할 수 있는 루틴 (예정돼 있고, 종목이 있고, 오늘 기록이 없을 때) */
private fun 오늘시작루틴(d: 앱데이터, 오늘: String): 루틴? =
    if (d.기록.containsKey(오늘)) null else d.예정루틴(오늘)?.takeIf { !it.휴식일 && it.종목.isNotEmpty() }

@Composable
fun 캘린더화면(상태: 앱상태, 루틴으로: () -> Unit, 운동으로: () -> Unit) {
    val c = Local색.current
    val d = 상태.d
    val 오늘 = 상태.오늘
    var 보는달 by remember { mutableStateOf(YearMonth.from(LocalDate.parse(오늘))) }
    var 고른날 by remember { mutableStateOf(오늘) }
    var 열린시트 by remember { mutableStateOf<String?>(null) }     // "루틴" | "휴식" | "시작"

    // 스크롤 없이 한 화면 (09-21 메모) — 달력이 남는 높이를 다 쓰고, 아래 판은 자기 안에서만 넘긴다
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(horizontal = 간격.보통).padding(top = 8.dp, bottom = 8.dp)) {
            // 달력은 보여 줄 것까지만 높이를 쓴다 (09-24 메모) — 남는 높이는 그냥 둔다
            카드(Modifier.weight(1f, fill = false), 안쪽 = 8.dp) {
                Row(Modifier.fillMaxWidth().번호("캘1"), verticalAlignment = Alignment.CenterVertically) {
                    아이콘버튼(아이콘.왼쪽, "이전 달", { 보는달 = 보는달.minusMonths(1) })
                    제목글("${보는달.year}년 ${보는달.monthValue}월", Modifier.weight(1f).padding(start = 12.dp), 크기값 = 크기.크게)
                    아이콘버튼(아이콘.오른쪽, "다음 달", { 보는달 = 보는달.plusMonths(1) })
                }
                Box(Modifier.height(4.dp))
                달력(d, 오늘, 보는달, 고른날, Modifier.번호("캘2"), on고름 = { 고른날 = it }, on두번 = { 고른날 = it; 열린시트 = "시작" },
                    on옮김 = { 원, 새날 -> 상태.바꿈 { it.예정옮기기(원, 새날, 오늘) }; 고른날 = 새날 })
            }
            Box(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().heightIn(max = 190.dp)) {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    날짜판(상태, 고른날, 루틴으로, 운동으로, { 열린시트 = "루틴" }, { 열린시트 = "휴식" }, { 열린시트 = "한번더" })
                }
            }
        }

        when (열린시트) {
            "시작" -> {
                val r = 오늘시작루틴(d, 오늘)
                if (d.세션 != null) { 열린시트 = null; 운동으로() }
                else if (r == null) 열린시트 = null
                else 시트("운동을 시작할까요?", { 열린시트 = null }) {
                    글("${r.이름} · ${r.종목.size}종목 · ${총세트(r)}세트 · 약 ${시간글(예상초(r))}", 크기값 = 크기.버튼, 색 = c.흐림)
                    Box(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        버튼("시작", {
                            val S = 운동시작(r, System.currentTimeMillis())
                            if (S != null) 상태.바꿈 { it.copy(세션 = S) }
                            열린시트 = null
                        }, Modifier.weight(1f), 주요 = true)
                        버튼("아니오", { 열린시트 = null }, Modifier.weight(1f))
                    }
                }
            }
            "루틴" -> 시트("${LocalDate.parse(고른날).monthValue}월 ${LocalDate.parse(고른날).dayOfMonth}일에 넣을 루틴", { 열린시트 = null }) {
                글("이 날부터 순서를 다시 깝니다 · 앞선 날은 그대로", 크기값 = 크기.조금작게, 색 = c.옅음)
                d.루틴들.forEach { r ->
                    고르기줄(r.이름, if (r.휴식일) "휴식일" else "${r.종목.size}종목 · ${총세트(r)}세트") {
                        상태.바꿈 { it.꽂기(r.id, 고른날, 오늘) }; 열린시트 = null
                    }
                }
            }
            // 오늘 기록이 있는 날 — 한 번 더 운동 (09-26 메모). 저장하면 그 날의 '한 번 더' 기록으로 따로 남는다
            "한번더" -> 시트("한 번 더 운동", { 열린시트 = null }) {
                글("오늘 기록은 그대로 두고 따로 남깁니다", 크기값 = 크기.조금작게, 색 = c.옅음)
                d.루틴들.filter { !it.휴식일 && it.종목.isNotEmpty() }.forEach { r ->
                    고르기줄(r.이름, "${r.종목.size}종목 · ${총세트(r)}세트 · 약 ${시간글(예상초(r))}") {
                        val S = 운동시작(r, System.currentTimeMillis())
                        if (S != null) 상태.바꿈 { it.copy(세션 = S) }
                        열린시트 = null
                    }
                }
            }
            "휴식" -> 시트("오늘 쉴까요?", { 열린시트 = null }) {
                val 미루기 = d.사흘미리(true, 오늘); val 건너 = d.사흘미리(false, 오늘)
                버튼("오늘 루틴을 내일로 미루기", { 상태.바꿈 { it.오늘휴식(true, 오늘) }; 열린시트 = null }, Modifier.fillMaxWidth(), 주요 = true)
                글("오늘 휴식 · 내일 ${미루기.getOrElse(0) { "" }} · 모레 ${미루기.getOrElse(1) { "" }} · 글피 ${미루기.getOrElse(2) { "" }}",
                    Modifier.padding(top = 4.dp, bottom = 12.dp), 크기값 = 크기.작게, 색 = c.옅음)
                버튼("오늘 루틴 건너뛰기", { 상태.바꿈 { it.오늘휴식(false, 오늘) }; 열린시트 = null }, Modifier.fillMaxWidth())
                글("오늘 휴식 · 내일 ${건너.getOrElse(0) { "" }} · 모레 ${건너.getOrElse(1) { "" }} · 글피 ${건너.getOrElse(2) { "" }}",
                    Modifier.padding(top = 4.dp, bottom = 4.dp), 크기값 = 크기.작게, 색 = c.옅음)
            }
        }
    }
}

@Composable
private fun 달력(d: 앱데이터, 오늘: String, 달: YearMonth, 고른날: String, modifier: Modifier, on고름: (String) -> Unit, on두번: (String) -> Unit,
                on옮김: (String, String) -> Unit) {
    val c = Local색.current
    Row(Modifier.fillMaxWidth()) {
        listOf("일", "월", "화", "수", "목", "금", "토").forEach {
            글(it, Modifier.weight(1f), 크기값 = 크기.작게, 색 = c.옅음, 가운데 = true)
        }
    }
    val 첫 = 달.atDay(1)
    val 앞빈칸 = 첫.dayOfWeek.value % 7          // 일요일 = 0
    val 칸수 = 앞빈칸 + 달.lengthOfMonth()
    val 줄수 = (칸수 + 6) / 7
    val 두번가능 = 오늘시작루틴(d, 오늘) != null
    // 줄 높이는 늘 같다 — 날짜를 고른다고 달력이 커졌다 작아졌다 하지 않는다 (09-24 메모)
    val 줄높이 = 50.dp
    // 꾹 눌러 옮기기 (2-3) — 예정만 있는 날(오늘 이후 · 기록 없음)을 꾹 눌러 다른 날에 놓는다
    var 끄는날 by remember { mutableStateOf<String?>(null) }
    var 놓을날 by remember { mutableStateOf<String?>(null) }
    var 손 by remember { mutableStateOf(Offset.Zero) }
    val 판데이터 by rememberUpdatedState(d)
    val 옮김 by rememberUpdatedState(on옮김)
    val 햅틱 = LocalHapticFeedback.current   // 들어 올렸을 때 '툭' — 옮기기가 시작된 걸 손으로 안다
    fun 칸날(p: Offset, 너비: Float, 줄px: Float): String? {
        if (p.x < 0 || p.y < 0 || 너비 <= 0f) return null
        val 칸 = (p.x / (너비 / 7f)).toInt().coerceAtMost(6)
        val 줄 = (p.y / 줄px).toInt()
        val n = 줄 * 7 + 칸 - 앞빈칸 + 1
        return if (줄 >= 줄수 || n < 1 || n > 달.lengthOfMonth()) null else 달.atDay(n).toString()
    }
    fun 옮길수있음(k: String?) = k != null && k >= 오늘 && !판데이터.기록.containsKey(k)
    Box(modifier.fillMaxWidth()) {
    Column(Modifier.fillMaxWidth().pointerInput(달, 오늘) {
        val 줄px = 줄높이.toPx()
        detectDragGesturesAfterLongPress(
            onDragStart = { p ->
                val k = 칸날(p, size.width.toFloat(), 줄px)
                if (옮길수있음(k) && 판데이터.예정[k!!] != null) { 끄는날 = k; 놓을날 = k; 손 = p; 햅틱.performHapticFeedback(HapticFeedbackType.LongPress) }
            },
            onDrag = { ch, _ ->
                if (끄는날 != null) { ch.consume(); 손 = ch.position; 놓을날 = 칸날(ch.position, size.width.toFloat(), 줄px) }
            },
            onDragEnd = {
                val 원 = 끄는날; val 새 = 놓을날
                if (원 != null && 새 != null && 새 != 원 && 옮길수있음(새)) 옮김(원, 새)
                끄는날 = null; 놓을날 = null
            },
            onDragCancel = { 끄는날 = null; 놓을날 = null },
        )
    }) {
        for (줄 in 0 until 줄수) {
            Row(Modifier.fillMaxWidth().height(줄높이).padding(top = 2.dp)) {
                for (칸 in 0 until 7) {
                    val n = 줄 * 7 + 칸 - 앞빈칸 + 1
                    if (n < 1 || n > 달.lengthOfMonth()) { Box(Modifier.weight(1f)); continue }
                    val 날 = 달.atDay(n)
                    val k = 날.toString()
                    val 표시 = when {
                        끄는날 == null -> 0
                        k == 끄는날 -> 1
                        k == 놓을날 -> if (옮길수있음(k)) 2 else 3
                        else -> 0
                    }
                    날칸(d, k, 날, 오늘, k == 고른날, Modifier.weight(1f).fillMaxHeight(),
                        두번 = if (k == 오늘 && 두번가능) ({ on두번(k) }) else null, 끌기표시 = 표시) { on고름(k) }
                }
            }
        }
    }
    // 손가락을 따라다니는 루틴 이름
    val 끄는 = 끄는날
    if (끄는 != null) {
        val c2 = Local색.current
        val 밀도 = LocalDensity.current
        val 이름 = d.예정루틴(끄는)?.let { if (it.휴식일) "휴식" else it.이름 } ?: ""
        Box(
            Modifier
                .offset { IntOffset((손.x - with(밀도) { 30.dp.toPx() }).toInt(), (손.y - with(밀도) { 44.dp.toPx() }).toInt()) }
                .clip(RoundedCornerShape(6.dp))
                .background(c2.강조)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) { 글(이름, 크기값 = 크기.작게, 색 = Color.White, 굵기 = FontWeight.Bold) }
    }
    }
}

@Composable
private fun 날칸(d: 앱데이터, k: String, 날: LocalDate, 오늘: String, 고름: Boolean, modifier: Modifier, 두번: (() -> Unit)?,
                끌기표시: Int = 0, onClick: () -> Unit) {
    val c = Local색.current
    val rec = d.기록[k]
    val 예 = if (rec == null && k >= 오늘) d.예정루틴(k) else null
    val 누름 by rememberUpdatedState(onClick)
    val 두번누름 by rememberUpdatedState(두번)
    val 숫자색 = when {
        날.dayOfWeek == DayOfWeek.SUNDAY -> c.나쁨
        날.dayOfWeek == DayOfWeek.SATURDAY -> c.내림
        else -> c.글
    }
    Column(
        modifier
            .padding(1.dp)
            .clip(RoundedCornerShape(모서리.아주작게))
            .then(if (고름) Modifier.border(1.5.dp, c.강조, RoundedCornerShape(모서리.아주작게)) else Modifier)
            // 끄는 중: 1 = 들어 올린 날(흐리게) · 2 = 놓을 수 있는 날 · 3 = 놓을 수 없는 날(지난 날 · 기록 있는 날)
            .then(when (끌기표시) {
                1 -> Modifier.alpha(0.35f)
                2 -> Modifier.background(c.강조옅음).border(2.dp, c.강조, RoundedCornerShape(모서리.아주작게))
                3 -> Modifier.border(2.dp, c.나쁨, RoundedCornerShape(모서리.아주작게))
                else -> Modifier
            })
            // 칸 전체가 누르는 곳. 오늘 칸만 두 번 누르기를 받는다
            //  · 두 번 누르기를 기다리느라 한 번 누름이 늦게 들어오던 것 → 손이 닿는 순간 고른다 (09-24 메모)
            .pointerInput(k, 두번 != null) {
                if (두번누름 != null) detectTapGestures(onPress = { 누름() }, onDoubleTap = { 두번누름?.invoke() })
                else detectTapGestures(onTap = { 누름() })
            }
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        글("${날.dayOfMonth}", 크기값 = 크기.조금작게, 색 = 숫자색, 굵기 = if (k == 오늘) FontWeight.Bold else FontWeight.Normal)
        val 이름: String?; val 바탕: Color; val 글색: Color; var 테두리 = false
        when {
            rec != null -> { 이름 = rec.루틴이름; 바탕 = if (rec.달성) c.강조옅음 else c.나쁨.copy(alpha = 0.12f); 글색 = if (rec.달성) c.좋음 else c.나쁨 }
            예 != null && 예.휴식일 -> { 이름 = "휴식"; 바탕 = c.면2; 글색 = c.옅음 }
            예 != null -> { 이름 = 예.이름; 바탕 = Color.Transparent; 글색 = c.흐림; 테두리 = true }
            else -> { 이름 = null; 바탕 = Color.Transparent; 글색 = c.흐림 }
        }
        if (이름 != null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(바탕)
                    .then(if (테두리) Modifier.border(1.dp, c.선, RoundedCornerShape(4.dp)) else Modifier)
                    .padding(horizontal = 2.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center,
            ) { 글(이름, 크기값 = 크기.아주작게, 색 = 글색, 굵기 = FontWeight.Bold) }
        }
        if (rec != null) 글("${정식세트(rec).size}세트", 크기값 = 크기.아주작게, 색 = c.옅음)
        if (d.일정[k]?.isNotEmpty() == true) Box(Modifier.padding(top = 2.dp).width(4.dp).height(4.dp).clip(CircleShape).background(c.휴식))
    }
}

/**
 * 날짜 아래 판 — 작게 (09-21 메모: 30% 가량 줄임).
 * 첫 줄에 이름 · 요약, 둘째 줄에 향상도, 셋째 줄에 버튼 넷(높이 32). 일정은 있을 때만 아래에.
 */
@Composable
private fun 날짜판(상태: 앱상태, k: String, 루틴으로: () -> Unit, 운동으로: () -> Unit, 다른루틴: () -> Unit, 쉬기: () -> Unit, 한번더: () -> Unit) {
    val c = Local색.current
    val d = 상태.d
    val 오늘 = 상태.오늘
    val 날 = LocalDate.parse(k)
    var 일정적기 by remember(k) { mutableStateOf(false) }
    val 일정버튼: @Composable RowScope.() -> Unit = { 버튼("일정", { 일정적기 = !일정적기 }, Modifier.weight(0.8f), 낮게 = true, 그림 = 아이콘.더하기) }
    카드(Modifier.번호("캘3"), 안쪽 = 10.dp) {
        val rec = d.기록[k]
        when {
            d.루틴들.isEmpty() -> {
                글("아직 루틴이 없습니다 · 만들면 순서대로 깔립니다", 크기값 = 크기.조금작게, 색 = c.옅음)
                Box(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    버튼("루틴 만들러 가기", 루틴으로, Modifier.weight(2f), 주요 = true, 낮게 = true, 그림 = 아이콘.더하기)
                    일정버튼()
                }
            }
            rec != null -> {
                val 세트들 = 정식세트(rec)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    제목글(rec.루틴이름, 크기값 = 크기.본문)
                    Box(Modifier.width(8.dp))
                    알약(if (rec.달성) "달성" else "미달성", if (rec.달성) c.좋음 else c.나쁨)
                    Box(Modifier.width(8.dp))
                    글("${세트들.size}세트 · ${콤마(볼륨(세트들))}kg · ${시분초(rec.걸린초.toLong())}", Modifier.weight(1f), 크기값 = 크기.작게, 색 = c.흐림)
                }
                향상줄("볼륨", d.루틴향상(rec.루틴id, 세트들, 날짜만(k), k), Modifier.padding(top = 2.dp))
                // 같은 날 '한 번 더' 한 운동 (09-26)
                d.한번더기록(k).forEach { (_, r2) ->
                    val s2 = 정식세트(r2)
                    글("한 번 더 · ${r2.루틴이름} · ${s2.size}세트 · ${콤마(볼륨(s2))}kg · ${시분초(r2.걸린초.toLong())}",
                        Modifier.padding(top = 2.dp), 크기값 = 크기.작게, 색 = c.흐림)
                }
                // 종목마다 세트를 늘어놓던 줄은 뺐다 (09-24 메모: 판이 넘쳐 버튼이 안 보였다. 판은 이름 · 요약 · 향상도 · 버튼만)
                Box(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 오늘이면 '한 번 더 운동' (09-26 메모) — 운동 중이면 '운동으로'
                    if (k == 오늘) {
                        if (d.세션 != null) 버튼("운동으로", 운동으로, Modifier.weight(1.2f), 주요 = true, 낮게 = true)
                        else 버튼("한 번 더 운동", 한번더, Modifier.weight(1.2f), 주요 = true, 낮게 = true)
                    }
                    버튼(if (k == 오늘) "기록 지우기" else "이 날 기록 지우기", {
                        // 그 날의 '한 번 더' 기록도 같이 지운다
                        상태.지우고알림("${날.monthValue}월 ${날.dayOfMonth}일 기록을 지웠습니다") { dd ->
                            dd.copy(기록 = dd.기록.filterKeys { it != k && !it.startsWith("$k~") }).예정초기화(오늘)
                        }
                    }, Modifier.weight(if (k == 오늘) 1f else 2f), 낮게 = true, 글색 = c.나쁨)
                    일정버튼()
                }
            }
            k < 오늘 -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                글("기록이 없는 날입니다", Modifier.weight(2f), 크기값 = 크기.조금작게, 색 = c.옅음)
                일정버튼()
            }
            else -> {
                val r = d.예정루틴(k)
                when {
                    r == null -> 글("예정된 루틴이 없습니다", 크기값 = 크기.조금작게, 색 = c.옅음)
                    r.휴식일 -> Row(verticalAlignment = Alignment.Bottom) {
                        제목글("휴식일", 크기값 = 크기.본문); Box(Modifier.width(8.dp))
                        글("순서에서 한 칸을 차지합니다", 크기값 = 크기.작게, 색 = c.옅음)
                    }
                    else -> {
                        Row(verticalAlignment = Alignment.Bottom) {
                            제목글(r.이름, Modifier.weight(1f, fill = false), 크기값 = 크기.본문)
                            Box(Modifier.width(8.dp))
                            글("${r.종목.size}종목 · ${총세트(r)}세트 · 약 ${시간글(예상초(r))}", 크기값 = 크기.작게, 색 = c.흐림)
                        }
                        val 지난 = d.루틴최근향상(r.id)
                        향상줄(if (지난 != null) "지난번 볼륨 ${콤마(지난.지금)}kg" else "지난번 기록 없음", 지난, Modifier.padding(top = 1.dp))
                    }
                }
                Box(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val 오늘운동 = k == 오늘 && r != null && !r.휴식일 && r.종목.isNotEmpty()
                    if (d.세션 != null && k == 오늘) 버튼("운동으로", 운동으로, Modifier.weight(1.2f), 주요 = true, 낮게 = true)
                    else if (오늘운동) 버튼("운동 시작", {
                        val S = 운동시작(r!!, System.currentTimeMillis())
                        if (S != null) 상태.바꿈 { it.copy(세션 = S) }
                    }, Modifier.weight(1.2f), 주요 = true, 낮게 = true)
                    버튼("다른 루틴", 다른루틴, Modifier.weight(1f), 낮게 = true)
                    if (k == 오늘 && r != null && !r.휴식일) 버튼("휴식", 쉬기, Modifier.weight(0.8f), 낮게 = true)
                    일정버튼()
                }
                if (k == 오늘 && r != null && !r.휴식일 && r.종목.isEmpty())
                    글("이 루틴에 종목이 없습니다 · 루틴 탭에서 넣어 주세요", Modifier.padding(top = 4.dp), 크기값 = 크기.작게, 색 = c.옅음)
            }
        }
        일정칸(상태, k, 일정적기) { 일정적기 = false }
    }
}

@Composable
fun 알약(글자: String, 색: Color) {
    Box(Modifier.clip(CircleShape).background(색.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
        글(글자, 크기값 = 크기.작게, 색 = 색, 굵기 = FontWeight.Bold)
    }
}

/** 일정 — 있을 때만 목록, 적을 때만 입력칸 (팝업 없음) */
@Composable
private fun 일정칸(상태: 앱상태, k: String, 적는중: Boolean, 다적음: () -> Unit) {
    val c = Local색.current
    val 것 = 상태.d.일정[k].orEmpty()
    var 새글 by remember(k) { mutableStateOf("") }
    것.forEachIndexed { i, t ->
        Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(8.dp).height(6.dp).clip(CircleShape).background(c.휴식))
            글(t, Modifier.weight(1f).padding(start = 8.dp), 크기값 = 크기.조금작게)
            아이콘버튼(아이콘.지우기, "일정 지우기", {
                상태.지우고알림("'$t' 일정을 지웠습니다") { d ->
                    val 남 = d.일정[k].orEmpty().filterIndexed { j, _ -> j != i }
                    d.copy(일정 = if (남.isEmpty()) d.일정 - k else d.일정 + (k to 남))
                }
            }, 칠함 = false, 크기칸 = 높이.아주낮게)
        }
    }
    if (적는중) {
        val 넣기 = {
            val t = 새글.trim()
            if (t.isNotEmpty()) 상태.바꿈 { d -> d.copy(일정 = d.일정 + (k to (d.일정[k].orEmpty() + t))) }
            새글 = ""; 다적음()
        }
        Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            입력칸(새글, { 새글 = it }, Modifier.weight(1f), 안내 = "일정 적기", onDone = 넣기)
            Box(Modifier.padding(start = 8.dp)) { 버튼("넣기", 넣기, 주요 = true, 작게 = true) }
        }
    }
}
