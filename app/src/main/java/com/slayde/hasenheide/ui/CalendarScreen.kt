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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.sp
import com.slayde.hasenheide.data.갱신
import com.slayde.hasenheide.data.기록갱신
import com.slayde.hasenheide.data.운동량
import com.slayde.hasenheide.data.시각글
import com.slayde.hasenheide.data.루틴두대비
import com.slayde.hasenheide.data.루틴한달
import com.slayde.hasenheide.data.목표종목들
import com.slayde.hasenheide.data.도달달
import com.slayde.hasenheide.data.그날루틴
import com.slayde.hasenheide.data.조절해시작
import com.slayde.hasenheide.data.계획볼륨
import com.slayde.hasenheide.data.한달추세
import com.slayde.hasenheide.data.한종목루틴
import com.slayde.hasenheide.data.퍼센트글
import com.slayde.hasenheide.data.오늘조절
import com.slayde.hasenheide.data.운동세션
import com.slayde.hasenheide.data.무게글
import com.slayde.hasenheide.data.무게반올림
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

/** 오늘 칸을 두 번 누르면 시작할 수 있는 루틴 (예정돼 있고, 종목이 있고, 오늘 기록이 없을 때) — 그 날 조절이 있으면 적용한 모양 */
private fun 오늘시작루틴(d: 앱데이터, 오늘: String): 루틴? =
    if (d.기록.containsKey(오늘)) null else d.그날루틴(오늘)?.takeIf { !it.휴식일 && it.종목.isNotEmpty() }

private fun 날글(k: String): String = LocalDate.parse(날짜만(k)).let { "${it.monthValue}월 ${it.dayOfMonth}일" }

@Composable
fun 캘린더화면(상태: 앱상태, 루틴으로: () -> Unit, 운동으로: () -> Unit) {
    val c = Local색.current
    val d = 상태.d
    val 오늘 = 상태.오늘
    var 보는달 by remember { mutableStateOf(YearMonth.from(LocalDate.parse(오늘))) }
    var 고른날 by remember { mutableStateOf(오늘) }
    // "시작" · "루틴" · "휴식" · "한번더" · "변경" · "조절" · "갱신" · "향상" · "목표" · "추세" · "종목"
    var 열린시트 by remember { mutableStateOf<String?>(null) }
    fun 시작(S: 운동세션?) { if (S != null) 상태.바꿈 { it.copy(세션 = S) }; 열린시트 = null }

    // 스크롤 없이 한 화면 (09-21 메모) — 달력은 제 높이만, 아래 판이 남는 높이를 쓰고 넘치면 판 안에서만 넘긴다
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(horizontal = 간격.보통).padding(top = 8.dp, bottom = 8.dp)) {
            카드(Modifier, 안쪽 = 0.dp) {
                // 년 · 월 띠 (09-27) — 가운데 제목, 양옆 ◀ ▶
                띠(Modifier.번호("캘1")) {
                    아이콘버튼(아이콘.왼쪽, "이전 달", { 보는달 = 보는달.minusMonths(1) }, 칠함 = false, 색 = c.강조글, 크기칸 = 높이.아주낮게)
                    띠글("${보는달.year}년 ${보는달.monthValue}월", Modifier.weight(1f), 가운데 = true)
                    아이콘버튼(아이콘.오른쪽, "다음 달", { 보는달 = 보는달.plusMonths(1) }, 칠함 = false, 색 = c.강조글, 크기칸 = 높이.아주낮게)
                }
                Column(Modifier.padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 6.dp)) {
                    달력(d, 오늘, 보는달, 고른날, Modifier.번호("캘2"), on고름 = { 고른날 = it }, on두번 = { 고른날 = it; 열린시트 = "시작" },
                        on옮김 = { 원, 새날 -> 상태.바꿈 { it.예정옮기기(원, 새날, 오늘) }; 고른날 = 새날 })
                }
            }
            Box(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().weight(1f, fill = false)) {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    날짜판(상태, 고른날, 루틴으로, 운동으로, { 열린시트 = it }) { 시작(it) }
                }
            }
        }

        val k = 고른날
        val 이날 = if (k == 오늘) "오늘" else "이 날"
        when (열린시트) {
            "시작" -> {
                val r = 오늘시작루틴(d, 오늘)
                if (d.세션 != null) { 열린시트 = null; 운동으로() }
                else if (r == null) 열린시트 = null
                else 시트("운동을 시작할까요?", { 열린시트 = null }) {
                    글("${r.이름} · ${r.종목.size}종목 · ${총세트(r)}세트 · 약 ${시간글(예상초(r))}", 크기값 = 크기.버튼, 색 = c.흐림)
                    Box(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        버튼("시작", { 시작(d.예정루틴(오늘)?.let { d.조절해시작(it, 오늘, System.currentTimeMillis()) }) }, Modifier.weight(1f), 주요 = true)
                        버튼("아니오", { 열린시트 = null }, Modifier.weight(1f))
                    }
                }
            }
            "루틴" -> 시트("${날글(k)}에 넣을 루틴", { 열린시트 = null }) {
                글("이 날부터 순서를 다시 깝니다 · 앞선 날은 그대로", 크기값 = 크기.조금작게, 색 = c.옅음)
                d.루틴들.forEach { r ->
                    고르기줄(r.이름, if (r.휴식일) "휴식일" else "${r.종목.size}종목 · ${총세트(r)}세트") {
                        상태.바꿈 { it.꽂기(r.id, k, 오늘) }; 열린시트 = null
                    }
                }
            }
            // 한 번 더 운동 (09-27) — 할 루틴 또는 종목 하나를 고른다. 저장하면 그 날의 '한 번 더' 기록으로 따로 남는다
            "한번더" -> 시트("한 번 더 운동 — 무엇을 할까요?", { 열린시트 = null }) {
                이름표("루틴")
                d.루틴들.filter { !it.휴식일 && it.종목.isNotEmpty() }.forEach { r ->
                    고르기줄(r.이름, "${r.종목.size}종목 · ${총세트(r)}세트 · 예상 시간 ${시간글(예상초(r))}") {
                        시작(운동시작(r, System.currentTimeMillis()))
                    }
                }
                이름표("종목 하나만", Modifier.padding(top = 8.dp))
                고르기줄("종목 고르기", "부위별 종목 목록에서") { 열린시트 = "종목" }
                글("오늘 기록은 그대로 두고 따로 남깁니다", Modifier.padding(top = 6.dp), 크기값 = 크기.작게, 색 = c.옅음)
            }
            "변경" -> 시트("$이날 운동 바꾸기", { 열린시트 = null }) {
                고르기줄("다른 루틴으로", d.루틴들.joinToString(" · ") { it.이름 }) { 열린시트 = "루틴" }
                if (k == 오늘 && d.세션 == null) 고르기줄("종목 하나만", "개별 종목 운동") { 열린시트 = "종목" }
                val r = d.예정루틴(k)
                if (k == 오늘 && r != null && !r.휴식일) 고르기줄("오늘은 휴식", "내일로 미루기 · 건너뛰기") { 열린시트 = "휴식" }
            }
            "종목" -> {
                var 부위 by remember { mutableStateOf("전체") }
                시트("종목 고르기", { 열린시트 = null }) {
                    칩줄(listOf("전체") + d.카테고리, 부위, { 부위 = it })
                    val 목록 = d.종목표.filter { 부위 == "전체" || it.부위 == 부위 }
                    if (목록.isEmpty()) 글("종목 탭에서 종목을 먼저 만들어 주세요", Modifier.padding(vertical = 12.dp), 크기값 = 크기.조금작게, 색 = c.옅음)
                    목록.forEach { e ->
                        고르기줄(e.이름, listOf(e.부위, e.장비).filter { it.isNotBlank() }.joinToString("·")) {
                            시작(운동시작(d.한종목루틴(e.이름), System.currentTimeMillis()))
                        }
                    }
                }
            }
            "조절" -> 시트("${이날}만 조절", { 열린시트 = null }) {
                val j = d.조절[k] ?: 오늘조절()
                fun 고침(n: 오늘조절) = 상태.바꿈 { dd -> dd.copy(조절 = if (n.그대로) dd.조절 - k else dd.조절 + (k to n)) }
                val 폭 = d.설정.무게폭.coerceAtLeast(0.5)
                조절줄("전체 볼륨", "${j.볼륨}%", { 고침(j.copy(볼륨 = (j.볼륨 - 5).coerceAtLeast(50))) }, { 고침(j.copy(볼륨 = (j.볼륨 + 5).coerceAtMost(150))) })
                조절줄("무게", "${if (j.무게 > 0) "+" else ""}${무게글(j.무게)}kg", { 고침(j.copy(무게 = 무게반올림(j.무게 - 폭))) }, { 고침(j.copy(무게 = 무게반올림(j.무게 + 폭))) })
                조절줄("세트 수", "${if (j.세트 > 0) "+" else ""}${j.세트}", { 고침(j.copy(세트 = (j.세트 - 1).coerceAtLeast(-5))) }, { 고침(j.copy(세트 = (j.세트 + 1).coerceAtMost(5))) })
                글("루틴은 그대로 두고 $이날 운동에만 적용합니다", Modifier.padding(top = 6.dp), 크기값 = 크기.작게, 색 = c.옅음)
                if (!j.그대로) 버튼("처음대로", { 고침(오늘조절()) }, Modifier.fillMaxWidth().padding(top = 8.dp), 작게 = true)
            }
            "갱신" -> 시트("기록 갱신 · ${날글(k)}", { 열린시트 = null }) {
                d.기록갱신(k).forEach { g ->
                    판줄(g.이름, 넓게 = true) {
                        Text(buildAnnotatedString {
                            append("${g.종류} ${if (g.종류 == "1RM") kg글(g.값) else 콤마(g.값)}kg — 전 최고 ${if (g.종류 == "1RM") kg글(g.전최고) else 콤마(g.전최고)}kg ")
                            withStyle(SpanStyle(color = c.오름, fontWeight = FontWeight.Bold)) { append("▲ ${kg글(g.차)}kg") }
                        }, style = 글꼴.보통(12.sp), color = c.글, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                글("기록 갱신은 지난 기록 전부와 견줍니다", Modifier.padding(top = 6.dp), 크기값 = 크기.작게, 색 = c.옅음)
            }
            "향상" -> {
                val rec = d.기록[k]
                if (rec == null) 열린시트 = null else 시트("향상도 · ${루틴표시(rec.루틴이름)} 볼륨", { 열린시트 = null }) {
                    val 달 = d.루틴한달(rec.루틴id, 날짜만(k), 포함 = true).filter { it.first <= k }
                    막대(달.map { it.second }, 마지막점선 = false)
                    val 두 = d.루틴두대비(k)
                    달.forEach { (kk, v) ->
                        val 이것 = kk == k
                        판줄(날글(kk), 넓게 = true) {
                            Text(buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = if (이것) FontWeight.Bold else FontWeight.Normal)) { append("${콤마(v)}kg") }
                                if (이것) {
                                    두.지난?.let { append(" · 지난 운동 대비 "); 증감붙임(it.diff, c) }
                                    두.한달?.let { append(" · 한 달 대비 "); 증감붙임(it.diff, c); 퍼센트글(it.지금, it.과거)?.let { p -> append(" ($p)") } }
                                }
                            }, style = 글꼴.보통(12.sp), color = c.글, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
            "목표" -> 시트("1RM 목표", { 열린시트 = null }) {
                d.목표종목들(k).forEach { g ->
                    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        글(g.이름, 크기값 = 크기.버튼, 굵기 = FontWeight.Bold)
                        Box(Modifier.fillMaxWidth().padding(vertical = 4.dp).height(8.dp).clip(RoundedCornerShape(4.dp)).background(c.강조옅음)) {
                            Box(Modifier.fillMaxWidth((g.지금 / g.목표).toFloat().coerceIn(0f, 1f)).height(8.dp).background(c.강조))
                        }
                        Text(buildAnnotatedString {
                            append("${kg글(g.지금)} / ${kg글(g.목표)}kg")
                            g.주속도?.let { append(" · 속도 1주 ${if (it >= 0) "+" else "−"}${kg글(it)}kg") }
                            when (val w = g.남은주) {
                                null -> append(" · 예상 도달 —")
                                0 -> withStyle(SpanStyle(color = c.강조, fontWeight = FontWeight.Bold)) { append(" · 목표 도달") }
                                else -> { append(" · 예상 도달 "); withStyle(SpanStyle(color = c.강조, fontWeight = FontWeight.Bold)) { append("${도달달(날짜만(k), w)} (약 ${w}주)") } }
                            }
                        }, style = 글꼴.보통(12.sp), color = c.흐림, maxLines = 2)
                    }
                    구분선()
                }
                글("목표는 종목 탭 또는 루틴의 종목 설정에서 적습니다", Modifier.padding(top = 6.dp), 크기값 = 크기.작게, 색 = c.옅음)
            }
            "추세" -> {
                val r = d.그날루틴(k)
                if (r == null) 열린시트 = null else 시트("최근 한 달 · ${루틴표시(r.이름)} 볼륨", { 열린시트 = null }) {
                    val 달 = d.루틴한달(r.id, k, 포함 = false)
                    val 목표 = r.계획볼륨()
                    막대(달.map { it.second } + 목표, 마지막점선 = true)
                    달.forEach { (kk, v) -> 판줄(날글(kk), 넓게 = true) { 글("${콤마(v)}kg", 크기값 = 12.sp) } }
                    판줄(if (k == 오늘) "오늘 목표" else "목표", 넓게 = true) {
                        Text(buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("${콤마(목표)}kg") }
                            달.firstOrNull()?.let { (_, 첫) ->
                                append(" — 한 달 대비 "); 증감붙임(목표 - 첫, c); 퍼센트글(목표, 첫)?.let { p -> append(" ($p)") }
                            }
                            d.한달추세(r.id, k)?.let { append(" · ${추세글(it)}") }
                        }, style = 글꼴.보통(12.sp), color = c.글, maxLines = 1, overflow = TextOverflow.Ellipsis)
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

private fun 추세글(차: Double): String = when { 차 > 0.05 -> "상승 중"; 차 < -0.05 -> "하락 중"; else -> "유지" }

/** ▲ 320kg / ▼ 320kg / 유지 — 빨강 · 중심색 */
private fun androidx.compose.ui.text.AnnotatedString.Builder.증감붙임(차: Double, c: com.slayde.hasenheide.ui.theme.색표) {
    when {
        차 > 0.05 -> withStyle(SpanStyle(color = c.오름, fontWeight = FontWeight.Bold)) { append("▲ ${kg글(차)}kg") }
        차 < -0.05 -> withStyle(SpanStyle(color = c.내림, fontWeight = FontWeight.Bold)) { append("▼ ${kg글(차)}kg") }
        else -> append("유지")
    }
}

/** 오늘만 조절 한 줄 — 이름 · − · 값 · ＋ */
@Composable
private fun 조절줄(이름: String, 값: String, 빼기: () -> Unit, 더하기: () -> Unit) {
    val c = Local색.current
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        글(이름, Modifier.weight(1f), 크기값 = 크기.버튼)
        아이콘버튼(아이콘.빼기, "$이름 빼기", 빼기, 칠함 = false, 색 = c.강조, modifier = Modifier.border(1.dp, c.속선, RoundedCornerShape(6.dp)))
        글(값, Modifier.width(72.dp), 크기값 = 크기.버튼, 굵기 = FontWeight.Bold, 가운데 = true)
        아이콘버튼(아이콘.더하기, "$이름 더하기", 더하기, 칠함 = false, 색 = c.강조, modifier = Modifier.border(1.dp, c.속선, RoundedCornerShape(6.dp)))
    }
    구분선()
}

/** 작은 막대 그림 — 마지막 막대는 진하게(점선이면 아직 안 한 목표) */
@Composable
private fun 막대(값: List<Double>, 마지막점선: Boolean) {
    if (값.isEmpty()) return
    val c = Local색.current
    val 최대 = 값.max().takeIf { it > 0 } ?: 1.0
    val 최소 = 값.min() * 0.85
    Row(Modifier.fillMaxWidth().height(56.dp).padding(bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
        값.forEachIndexed { i, v ->
            val 끝 = i == 값.lastIndex
            val 비 = if (최대 - 최소 < 1e-6) 1f else ((v - 최소) / (최대 - 최소)).toFloat().coerceIn(0.15f, 1f)
            Box(
                Modifier.weight(1f).fillMaxHeight(비).clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                    .then(when {
                        끝 && 마지막점선 -> Modifier.border(1.5.dp, c.강조, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        끝 -> Modifier.background(c.강조)
                        else -> Modifier.background(c.강조옅음)
                    }),
            )
        }
    }
}

/**
 * 판의 한 줄 (09-27) — 왼쪽 이름 칸(52dp · 보통 간격 왼쪽 정렬 · 오른쪽에 │ 속선) + 값.
 * 누를 수 있으면 오른쪽 끝에 › (중심색). 줄 사이는 위쪽 가는 선
 */
@Composable
private fun 판줄(
    이름: String?, modifier: Modifier = Modifier, 넓게: Boolean = false, 윗선: Boolean = true,
    이름칸: (@Composable () -> Unit)? = null, onClick: (() -> Unit)? = null, 내용: @Composable RowScope.() -> Unit,
) {
    val c = Local색.current
    val 선색 = c.선; val 속선 = c.속선
    Row(
        modifier.fillMaxWidth().height(IntrinsicSize.Min)
            .then(if (윗선) Modifier.drawBehind { drawLine(선색, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx()) } else Modifier)
            .then(if (onClick != null) Modifier.눌림(onClick) else Modifier)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.width(if (넓게) 72.dp else 52.dp).fillMaxHeight()
                .drawBehind { drawLine(속선, Offset(size.width - 0.5.dp.toPx(), 0f), Offset(size.width - 0.5.dp.toPx(), size.height), 1.dp.toPx()) }
                .padding(end = 3.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (이름칸 != null) 이름칸() else Text(이름 ?: "", style = 글꼴.보통(12.sp), color = c.옅음, maxLines = 1, softWrap = false)
        }
        Box(Modifier.width(6.dp))
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), content = 내용)
        if (onClick != null) Text("›", Modifier.padding(start = 6.dp, end = 2.dp), style = 글꼴.보통(15.sp, FontWeight.Bold), color = c.강조)
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
    val 줄높이 = 56.dp   // 09-27: 칸을 늘렸다 (루틴 이름 아래 달성 · 미달성 · 미실시)
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
    val 안함 = if (rec == null && k < 오늘) d.미실시[k] else null
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
        // 09-29 메모: 칸 안의 것이 56dp 를 넘겨 '달성' 이 잘렸다 → 줄 높이를 글자에 딱 맞추고 간격을 1dp 로
        Text(
            "${날.dayOfMonth}",
            style = 글꼴.보통(크기.조금작게, if (k == 오늘) FontWeight.Bold else FontWeight.Normal).copy(lineHeight = 14.sp),
            color = 숫자색,
            maxLines = 1,
        )
        // 루틴 칩 (09-27) — 달력엔 초록을 쓰지 않는다
        //  · 한 날: 옅은 중심색 바탕 + 중심색 글자 · 휴식: 바탕 없이 속선 테두리(같은 크기 · 글자 · 색)
        //  · 예정: 속선 테두리 + 흐린 글자 · 미실시: 테두리만
        val 이름: String?; val 바탕: Color; val 글색: Color; var 테두리 = false
        when {
            rec != null -> { 이름 = rec.루틴이름; 바탕 = c.강조옅음; 글색 = c.강조 }
            안함 != null -> { 이름 = 안함; 바탕 = Color.Transparent; 글색 = c.옅음; 테두리 = true }
            예 != null && 예.휴식일 -> { 이름 = "휴식"; 바탕 = Color.Transparent; 글색 = c.강조; 테두리 = true }
            예 != null -> { 이름 = 예.이름; 바탕 = Color.Transparent; 글색 = c.흐림; 테두리 = true }
            else -> { 이름 = null; 바탕 = Color.Transparent; 글색 = c.흐림 }
        }
        if (이름 != null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 1.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(바탕)
                    .then(if (테두리) Modifier.border(1.dp, c.속선, RoundedCornerShape(4.dp)) else Modifier)
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    이름,
                    style = 글꼴.보통(크기.아주작게, FontWeight.Bold).copy(lineHeight = 13.sp),
                    color = 글색, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
        }
        // 루틴 이름 아래 — 달성 · 미달성 · 미실시 (09-27). 휴식은 적지 않는다
        val 상태글: Pair<String, Color>? = when {
            rec != null -> if (rec.달성) "달성" to c.강조 else "미달성" to c.나쁨
            안함 != null -> "미실시" to c.옅음
            else -> null
        }
        if (상태글 != null) Text(상태글.first, Modifier.padding(top = 1.dp), style = 글꼴.보통(9.sp, FontWeight.Bold).copy(lineHeight = 10.sp), color = 상태글.second, maxLines = 1)
        if (d.일정[k]?.isNotEmpty() == true) Box(Modifier.padding(top = 1.dp).width(4.dp).height(4.dp).clip(CircleShape).background(c.휴식))
    }
}

/**
 * 날짜 아래 판 (09-27 시안)
 *  · 운동 후(기록 있는 날): 이름 · 달성 · 시각 / 기록 갱신 / 운동량 / 향상도 / 1RM 목표 / [한 번 더 운동] [기록 삭제]
 *  · 운동 전(예정인 날): 이름 / 운동량 / 오늘 목표 + 최근 한 달 / 종목 전부 / [운동 시작] [변경] [설정]
 *  · 한 줄 = 왼쪽 이름 칸 + 값. › 가 있는 줄은 누르면 자세히
 */
@Composable
private fun 날짜판(상태: 앱상태, k: String, 루틴으로: () -> Unit, 운동으로: () -> Unit, 열기: (String) -> Unit, 시작: (운동세션?) -> Unit) {
    val c = Local색.current
    val d = 상태.d
    val 오늘 = 상태.오늘
    val 날 = LocalDate.parse(k)
    카드(Modifier.번호("캘3"), 안쪽 = 10.dp) {
        val rec = d.기록[k]
        when {
            d.루틴들.isEmpty() && rec == null -> {
                글("아직 루틴이 없습니다 · 만들면 순서대로 깔립니다", 크기값 = 크기.조금작게, 색 = c.옅음)
                Box(Modifier.height(8.dp))
                버튼("루틴 만들러 가기", 루틴으로, Modifier.fillMaxWidth(), 주요 = true, 낮게 = true, 그림 = 아이콘.더하기)
            }
            rec != null -> {
                // ── 머리: 이름(중심색 · 한 단계 큰 글자) · 달성 알약 · 시작 ~ 끝 시각 ──
                Row(Modifier.fillMaxWidth().padding(bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(rec.루틴이름, Modifier.weight(1f, fill = false), style = 글꼴.제목(17.sp).copy(fontWeight = FontWeight.ExtraBold), color = c.강조, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Box(Modifier.width(6.dp))
                    달성알약(rec.달성)
                    Box(Modifier.weight(1f))
                    시각글(rec.시작시각, rec.끝시각)?.let { 글(it, 크기값 = 크기.작게, 색 = c.흐림) }
                }
                // ── 기록 갱신 — 있을 때만 · 맨 위 · 1RM 줄 → 볼륨 줄 (최대 2줄) ──
                val 갱 = d.기록갱신(k)
                if (갱.isNotEmpty()) {
                    val rm = 갱.filter { it.종류 == "1RM" }; val 볼 = 갱.filter { it.종류 == "볼륨" }
                    판줄(null, 윗선 = false, 이름칸 = { 작은표("기록 갱신", 채움 = true) }, onClick = { 열기("갱신") }) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            if (rm.isNotEmpty()) 갱신줄("1RM", rm, k)
                            if (볼.isNotEmpty()) 갱신줄("볼륨", 볼, k)
                        }
                    }
                }
                // ── 운동량 ──
                val 량 = 운동량(rec)
                판줄("운동량", 윗선 = 갱.isNotEmpty()) {
                    글("${량.종목}종목 · ${량.세트}세트 · ${콤마(량.볼륨)}kg" + (if (rec.걸린초 > 0) " · 운동 시간 ${시간글(rec.걸린초)}" else ""), 크기값 = 12.sp)
                }
                // ── 향상도 — 지난 운동 대비 · 한 달 대비 (보일 때마다 0 부터 올라간다) ──
                val 두 = d.루틴두대비(k)
                판줄("향상도", onClick = { 열기("향상") }) {
                    if (두.지난 == null && 두.한달 == null) 글("견줄 기록 없음", 크기값 = 12.sp, 색 = c.옅음)
                    대비칩("지난 운동 대비", 두.지난, 영부터 = true, 열쇠 = k)
                    대비칩("한 달 대비", 두.한달, 영부터 = true, 열쇠 = k)
                }
                // ── 1RM 목표 — 목표를 적어 둔 종목이 이 날 있으면 (두 줄) ──
                d.목표종목들(k).firstOrNull()?.let { g ->
                    판줄("1RM 목표", onClick = { 열기("목표") }) {
                        Column(Modifier.weight(1f)) {
                            Text(buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(g.이름) }
                                append(" ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = c.강조, fontSize = 13.sp)) { append(kg글(g.지금)) }
                                withStyle(SpanStyle(color = c.옅음)) { append(" / ${kg글(g.목표)}kg") }
                            }, style = 글꼴.보통(12.sp), color = c.글, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            val 속 = g.주속도?.let { "속도 1주 ${if (it >= 0) "+" else "−"}${kg글(it)}kg" } ?: "속도 —"
                            val 남 = when (val w = g.남은주) { null -> "목표까지 —"; 0 -> "목표 도달"; else -> "목표까지 약 ${w}주" }
                            글("$속 · $남", 크기값 = 11.sp, 색 = c.흐림)
                        }
                    }
                }
                Box(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (k == 오늘) {
                        if (d.세션 != null) 버튼("운동으로", 운동으로, Modifier.weight(1f), 주요 = true, 낮게 = true)
                        else 버튼("한 번 더 운동", { 열기("한번더") }, Modifier.weight(1f), 주요 = true, 낮게 = true)
                    }
                    버튼("기록 삭제", {
                        // 그 날의 '한 번 더' 기록도 같이 지운다
                        상태.지우고알림("${날.monthValue}월 ${날.dayOfMonth}일 기록을 지웠습니다") { dd ->
                            dd.copy(기록 = dd.기록.filterKeys { it != k && !it.startsWith("$k~") }).예정초기화(오늘)
                        }
                    }, Modifier.weight(1f), 낮게 = true, 글색 = c.나쁨)
                }
            }
            k < 오늘 -> {
                val 안함 = d.미실시[k]
                if (안함 != null) Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(안함, style = 글꼴.제목(17.sp).copy(fontWeight = FontWeight.ExtraBold), color = c.강조, maxLines = 1)
                    Box(Modifier.width(8.dp)); 글("미실시 · 기록이 없는 날입니다", 크기값 = 크기.작게, 색 = c.옅음)
                } else 글("기록이 없는 날입니다", 크기값 = 크기.조금작게, 색 = c.옅음)
            }
            else -> {
                val 원 = d.예정루틴(k)
                val r = d.그날루틴(k)
                when {
                    r == null -> 글("예정된 루틴이 없습니다", 크기값 = 크기.조금작게, 색 = c.옅음)
                    r.휴식일 -> Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("휴식일", style = 글꼴.제목(17.sp).copy(fontWeight = FontWeight.ExtraBold), color = c.강조, maxLines = 1)
                        Box(Modifier.width(8.dp)); 글("순서에서 한 칸을 차지합니다", 크기값 = 크기.작게, 색 = c.옅음)
                    }
                    else -> {
                        Text(r.이름, Modifier.padding(bottom = 2.dp), style = 글꼴.제목(17.sp).copy(fontWeight = FontWeight.ExtraBold), color = c.강조, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        val 목표 = r.계획볼륨()
                        판줄("운동량", 윗선 = false) {
                            글("${r.종목.size}종목 · ${총세트(r)}세트 · ${콤마(목표)}kg · 예상 시간 ${시간글(예상초(r))}", 크기값 = 12.sp)
                        }
                        val 추 = d.한달추세(r.id, k)
                        판줄(if (k == 오늘) "오늘 목표" else "목표", onClick = { 열기("추세") }) {
                            글("${콤마(목표)}kg", 크기값 = 12.sp)
                            if (추 != null) Row(
                                Modifier.border(1.dp, c.속선, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                글("최근 한 달", 크기값 = 크기.아주작게)
                                Box(Modifier.width(3.dp))
                                when {
                                    추 > 0.05 -> 글("▲ 상승 중", 크기값 = 크기.아주작게, 색 = c.오름, 굵기 = FontWeight.Bold)
                                    추 < -0.05 -> 글("▼ 하락 중", 크기값 = 크기.아주작게, 색 = c.내림, 굵기 = FontWeight.Bold)
                                    else -> 글("유지", 크기값 = 크기.아주작게, 색 = c.흐림)
                                }
                            }
                        }
                        // 종목 — 전부, 두 칸으로 (번호 · 이름 · 세트 수)
                        val 줄 = (r.종목.size + 1) / 2
                        Column(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                            for (i in 0 until 줄) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                for (col in 0..1) {
                                    val n = i + col * 줄
                                    val e = r.종목.getOrNull(n)
                                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                        if (e != null) {
                                            글("${n + 1}", Modifier.width(16.dp), 크기값 = 12.sp, 색 = c.옅음)
                                            Box(Modifier.width(4.dp))
                                            글(e.이름, Modifier.weight(1f), 크기값 = 12.sp)
                                            글("${e.세트}", 크기값 = 12.sp, 색 = c.옅음)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Box(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val 오늘운동 = k == 오늘 && r != null && !r.휴식일 && r.종목.isNotEmpty()
                    if (d.세션 != null && k == 오늘) 버튼("운동으로", 운동으로, Modifier.weight(1.4f), 주요 = true, 낮게 = true)
                    else if (오늘운동) 버튼("운동 시작", { 시작(원?.let { d.조절해시작(it, k, System.currentTimeMillis()) }) }, Modifier.weight(1.4f), 주요 = true, 낮게 = true)
                    버튼("변경", { 열기("변경") }, Modifier.weight(1f), 낮게 = true)
                    if (r != null && !r.휴식일 && r.종목.isNotEmpty())
                        버튼("설정", { 열기("조절") }, Modifier.weight(1f), 낮게 = true, 글색 = if (d.조절[k] != null) c.강조 else null)
                }
                if (k == 오늘 && r != null && !r.휴식일 && r.종목.isEmpty())
                    글("이 루틴에 종목이 없습니다 · 루틴 탭에서 넣어 주세요", Modifier.padding(top = 4.dp), 크기값 = 크기.작게, 색 = c.옅음)
            }
        }
        일정칸(상태, k, false) { }
    }
}

/**
 * 기록 갱신 한 줄 — [1RM] 딥스 98kg(▲ 3) · 인클라인 36kg(▲ 1)
 * 넘치면 옆으로 끌어서 본다. 끝은 살짝 흐려 '더 있음'을 보인다 (09-27)
 */
@Composable
private fun 갱신줄(종류: String, 들: List<갱신>, k: String) {
    val c = Local색.current
    val 넘김 = rememberScrollState()
    val 차들 = 들.map { 움직수(it.차, 영부터 = true, 열쇠 = k) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        작은표(종류)
        Box(Modifier.width(5.dp))
        Box(
            Modifier.weight(1f)
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .drawWithContent {
                    drawContent()
                    if (넘김.value < 넘김.maxValue) drawRect(
                        Brush.horizontalGradient(listOf(Color.Black, Color.Transparent), startX = size.width - 14.dp.toPx(), endX = size.width),
                        blendMode = BlendMode.DstIn,
                    )
                },
        ) {
            Text(buildAnnotatedString {
                들.forEachIndexed { i, g ->
                    if (i > 0) append(" · ")
                    append("${g.이름} ${if (g.종류 == "1RM") kg글(g.값) else 콤마(g.값)}kg(")
                    withStyle(SpanStyle(color = c.오름, fontWeight = FontWeight.Bold)) { append("▲ ${kg글(차들[i])}") }
                    append(")")
                }
            }, Modifier.horizontalScroll(넘김), style = 글꼴.보통(12.sp), color = c.글, maxLines = 1, softWrap = false)
        }
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
