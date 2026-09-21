package com.slayde.hasenheide.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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

/** 2026년 공휴일 — 시제품과 같다. 안드로이드에서 자동으로 받기는 나중에 (업데이트 예정 ⑬) */
val 공휴일 = mapOf(
    "2026-01-01" to "신정", "2026-02-16" to "설 연휴", "2026-02-17" to "설날", "2026-02-18" to "설 연휴",
    "2026-03-01" to "삼일절", "2026-03-02" to "대체휴일", "2026-05-05" to "어린이날", "2026-05-24" to "부처님오신날",
    "2026-05-25" to "대체휴일", "2026-06-06" to "현충일", "2026-08-15" to "광복절", "2026-08-17" to "대체휴일",
    "2026-09-24" to "추석 연휴", "2026-09-25" to "추석", "2026-09-26" to "추석 연휴", "2026-10-03" to "개천절",
    "2026-10-05" to "대체휴일", "2026-10-09" to "한글날", "2026-12-25" to "성탄절",
)

/** "가슴날, 3개월 동안 18% 성장했습니다" — 오르면 빨강 내리면 파랑 (6-3) */
@Composable
fun 성장줄(대상: String, 결과: 대비결과?, 기간: String, modifier: Modifier = Modifier) {
    val c = Local색.current
    val 글자 = buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = c.글)) { append(대상) }
        val p = 결과?.볼륨?.pct
        if (p == null) { append(" · $기간 비교 기록 없음"); return@buildAnnotatedString }
        append(", $기간 ")
        when {
            p > 0 -> { withStyle(SpanStyle(color = c.오름, fontWeight = FontWeight.Bold)) { append("${p}% 성장") }; append("했습니다") }
            p < 0 -> { withStyle(SpanStyle(color = c.내림, fontWeight = FontWeight.Bold)) { append("${-p}% 줄었") }; append("습니다") }
            else -> append("변화가 없습니다")
        }
    }
    Text(글자, modifier, style = 글꼴.보통(크기.버튼), color = c.흐림, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

@Composable
fun 캘린더화면(상태: 앱상태, 루틴으로: () -> Unit) {
    val c = Local색.current
    val d = 상태.d
    val 오늘 = 상태.오늘
    var 보는달 by remember { mutableStateOf(YearMonth.from(LocalDate.parse(오늘))) }
    var 고른날 by remember { mutableStateOf(오늘) }
    var 열린시트 by remember { mutableStateOf<String?>(null) }     // "루틴" | "휴식"

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 간격.넓게)) {
            Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                제목글("하젠하이데", Modifier.weight(1f))
                글("${보는달.year}.${보는달.monthValue.toString().padStart(2, '0')}", 크기값 = 크기.조금작게, 색 = c.옅음)
            }
            카드(안쪽 = 10.dp) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    아이콘버튼(아이콘.왼쪽, "이전 달", { 보는달 = 보는달.minusMonths(1) })
                    제목글("${보는달.year}년 ${보는달.monthValue}월", Modifier.weight(1f).padding(start = 12.dp), 크기값 = 크기.크게)
                    아이콘버튼(아이콘.오른쪽, "다음 달", { 보는달 = 보는달.plusMonths(1) })
                }
                Box(Modifier.height(8.dp))
                달력(d, 오늘, 보는달, 고른날) { 고른날 = it }
            }
            Box(Modifier.height(10.dp))
            날짜판(상태, 고른날, 루틴으로, { 열린시트 = "루틴" }, { 열린시트 = "휴식" })
            Box(Modifier.height(90.dp))
        }

        when (열린시트) {
            "루틴" -> 시트("${LocalDate.parse(고른날).monthValue}월 ${LocalDate.parse(고른날).dayOfMonth}일에 넣을 루틴", { 열린시트 = null }) {
                글("이 날부터 순서를 다시 깝니다 · 앞선 날은 그대로", 크기값 = 크기.조금작게, 색 = c.옅음)
                d.루틴들.forEach { r ->
                    고르기줄(r.이름, if (r.휴식일) "휴식일" else "${r.종목.size}종목 · ${총세트(r)}세트") {
                        상태.바꿈 { it.꽂기(r.id, 고른날, 오늘) }; 열린시트 = null
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
private fun 달력(d: 앱데이터, 오늘: String, 달: YearMonth, 고른날: String, on고름: (String) -> Unit) {
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
    val 고른줄 = run {
        val g = LocalDate.parse(고른날)
        if (YearMonth.from(g) == 달) (앞빈칸 + g.dayOfMonth - 1) / 7 else -1
    }
    for (줄 in 0 until 줄수) {
        val 펼침 = 줄 == 고른줄                        // 누른 날이 있는 줄만 종목까지 (2-1)
        Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
            for (칸 in 0 until 7) {
                val n = 줄 * 7 + 칸 - 앞빈칸 + 1
                if (n < 1 || n > 달.lengthOfMonth()) { Box(Modifier.weight(1f)); continue }
                val 날 = 달.atDay(n)
                날칸(d, 날.toString(), 날, 오늘, 날.toString() == 고른날, 펼침, Modifier.weight(1f)) { on고름(날.toString()) }
            }
        }
    }
}

@Composable
private fun 날칸(d: 앱데이터, k: String, 날: LocalDate, 오늘: String, 고름: Boolean, 펼침: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val c = Local색.current
    val rec = d.기록[k]
    val 예 = if (rec == null && k >= 오늘) d.예정루틴(k) else null
    val 휴 = 공휴일[k]
    val 숫자색 = when {
        휴 != null || 날.dayOfWeek == DayOfWeek.SUNDAY -> c.나쁨
        날.dayOfWeek == DayOfWeek.SATURDAY -> c.내림
        else -> c.글
    }
    Column(
        modifier
            .padding(1.dp)
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(모서리.아주작게))
            .then(if (고름) Modifier.border(1.5.dp, c.강조, RoundedCornerShape(모서리.아주작게)) else Modifier)
            .눌림(onClick)
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
        if (휴 != null) 글(휴, 크기값 = 크기.아주작게, 색 = c.나쁨)
        if (rec != null) 글("${정식세트(rec).size}세트", 크기값 = 크기.아주작게, 색 = c.옅음)
        if (펼침) {
            val 종목들 = rec?.종목들?.map { it.이름 } ?: 예?.종목?.map { it.이름 } ?: emptyList()
            종목들.forEach { 이 ->
                val 짧게 = d.종목표.firstOrNull { it.이름 == 이 }?.달력이름 ?: 이
                Text(짧게, style = 글꼴.보통(크기.아주작게), color = c.흐림, maxLines = 1, overflow = TextOverflow.Clip, softWrap = false)
            }
        }
        if (d.일정[k]?.isNotEmpty() == true) Box(Modifier.padding(top = 2.dp).width(4.dp).height(4.dp).clip(CircleShape).background(c.휴식))
    }
}

@Composable
private fun 날짜판(상태: 앱상태, k: String, 루틴으로: () -> Unit, 다른루틴: () -> Unit, 쉬기: () -> Unit) {
    val c = Local색.current
    val d = 상태.d
    val 오늘 = 상태.오늘
    val 날 = LocalDate.parse(k)
    val 요일 = "일월화수목금토"[날.dayOfWeek.value % 7]
    val 머리 = (if (k == 오늘) "오늘 · " else "") + "${날.monthValue}월 ${날.dayOfMonth}일 ($요일)"
    카드 {
        글(머리, 크기값 = 크기.조금작게, 색 = c.옅음, 굵기 = FontWeight.Bold)
        Box(Modifier.height(6.dp))
        val rec = d.기록[k]
        when {
            d.루틴들.isEmpty() -> {
                글("아직 루틴이 없습니다", 굵기 = FontWeight.Bold)
                글("루틴을 만들면 순서대로 달력에 깔립니다", 크기값 = 크기.조금작게, 색 = c.옅음)
                Box(Modifier.height(10.dp))
                버튼("루틴 만들러 가기", 루틴으로, Modifier.fillMaxWidth(), 주요 = true, 그림 = 아이콘.더하기)
            }
            rec != null -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    제목글(rec.루틴이름, 크기값 = 크기.크게)
                    Box(Modifier.width(8.dp))
                    알약(if (rec.달성) "달성" else "미달성", if (rec.달성) c.좋음 else c.나쁨)
                }
                val 세트들 = 정식세트(rec)
                글("${세트들.size}세트 · 볼륨 ${콤마(볼륨(세트들))}kg · ${시분초(rec.걸린초.toLong())}", 크기값 = 크기.조금작게, 색 = c.흐림)
                val 앞 = d.기록.filter { it.value.루틴id == rec.루틴id && it.key < k }.keys.maxOrNull()
                val 결과 = 앞?.let { 대비(세트들, 정식세트(d.기록[it]!!), it) }
                성장줄(rec.루틴이름, 결과, "직전 대비", Modifier.padding(vertical = 6.dp))
                rec.종목들.forEach { e ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                        글(e.이름, Modifier.weight(1f), 크기값 = 크기.버튼)
                        글(e.세트들.joinToString(" ") { "${com.slayde.hasenheide.data.무게글(it.w)}×${it.r}" }, Modifier.weight(1.4f), 크기값 = 크기.작게, 색 = c.흐림)
                    }
                }
                Box(Modifier.height(8.dp))
                버튼("이 날 기록 지우기", {
                    상태.지우고알림("${날.monthValue}월 ${날.dayOfMonth}일 기록을 지웠습니다") { it.copy(기록 = it.기록 - k).예정초기화(오늘) }
                }, Modifier.fillMaxWidth(), 작게 = true, 글색 = c.나쁨)
            }
            k < 오늘 -> 글("기록이 없는 날입니다", 색 = c.옅음)
            else -> {
                val r = d.예정루틴(k)
                if (r == null) 글("예정된 루틴이 없습니다", 색 = c.옅음)
                else if (r.휴식일) { 제목글("휴식일", 크기값 = 크기.크게); 글("순서에서 한 칸을 차지합니다", 크기값 = 크기.조금작게, 색 = c.옅음) }
                else {
                    Row(verticalAlignment = Alignment.Bottom) {
                        제목글(r.이름, 크기값 = 크기.크게)
                        Box(Modifier.width(8.dp))
                        글("${r.종목.size}종목 · ${총세트(r)}세트 · 약 ${시간글(예상초(r))}", 크기값 = 크기.조금작게, 색 = c.흐림)
                    }
                    성장줄(r.이름, d.루틴성장(r.id, 오늘), d.지금기준().기간, Modifier.padding(top = 4.dp))
                }
                Box(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (k == 오늘 && r != null && !r.휴식일 && r.종목.isNotEmpty()) {
                        버튼("운동 시작", {
                            val S = 운동시작(r, System.currentTimeMillis())
                            if (S != null) 상태.바꿈 { it.copy(세션 = S) }
                        }, Modifier.weight(1.3f), 주요 = true, 작게 = true)
                    }
                    버튼("다른 루틴", 다른루틴, Modifier.weight(1f), 작게 = true)
                    if (k == 오늘 && r != null && !r.휴식일) 버튼("휴식", 쉬기, Modifier.weight(1f), 작게 = true)
                }
                if (k == 오늘 && r != null && !r.휴식일 && r.종목.isEmpty())
                    글("이 루틴에 종목이 없습니다 · 루틴 탭에서 넣어 주세요", Modifier.padding(top = 6.dp), 크기값 = 크기.작게, 색 = c.옅음)
            }
        }
        일정칸(상태, k)
    }
}

@Composable
fun 알약(글자: String, 색: Color) {
    Box(Modifier.clip(CircleShape).background(색.copy(alpha = 0.12f)).padding(horizontal = 9.dp, vertical = 3.dp)) {
        글(글자, 크기값 = 크기.작게, 색 = 색, 굵기 = FontWeight.Bold)
    }
}

/** 일정 — 그 자리에서 적는다 (팝업 없음) */
@Composable
private fun 일정칸(상태: 앱상태, k: String) {
    val c = Local색.current
    val 것 = 상태.d.일정[k].orEmpty()
    var 적는중 by remember(k) { mutableStateOf(false) }
    var 새글 by remember(k) { mutableStateOf("") }
    Box(Modifier.height(10.dp))
    구분선()
    것.forEachIndexed { i, t ->
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(6.dp).height(6.dp).clip(CircleShape).background(c.휴식))
            글(t, Modifier.weight(1f).padding(start = 8.dp), 크기값 = 크기.버튼)
            아이콘버튼(아이콘.지우기, "일정 지우기", {
                상태.지우고알림("'$t' 일정을 지웠습니다") { d ->
                    val 남 = d.일정[k].orEmpty().filterIndexed { j, _ -> j != i }
                    d.copy(일정 = if (남.isEmpty()) d.일정 - k else d.일정 + (k to 남))
                }
            }, 칠함 = false)
        }
    }
    Box(Modifier.height(8.dp))
    if (적는중) {
        val 넣기 = {
            val t = 새글.trim()
            if (t.isNotEmpty()) 상태.바꿈 { d -> d.copy(일정 = d.일정 + (k to (d.일정[k].orEmpty() + t))) }
            새글 = ""; 적는중 = false
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            입력칸(새글, { 새글 = it }, Modifier.weight(1f), 안내 = "일정 적기", onDone = 넣기)
            Box(Modifier.padding(start = 6.dp)) { 버튼("넣기", 넣기, 주요 = true, 작게 = true) }
        }
    } else {
        버튼("일정 추가", { 적는중 = true }, Modifier.fillMaxWidth(), 작게 = true, 그림 = 아이콘.더하기)
    }
}
