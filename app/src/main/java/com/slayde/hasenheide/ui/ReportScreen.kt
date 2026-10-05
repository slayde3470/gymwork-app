package com.slayde.hasenheide.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.slayde.hasenheide.data.kg글
import com.slayde.hasenheide.data.달성도
import com.slayde.hasenheide.data.루틴달성도
import com.slayde.hasenheide.data.루틴향상
import com.slayde.hasenheide.data.목표세트
import com.slayde.hasenheide.data.묶기
import com.slayde.hasenheide.data.묶음이름
import com.slayde.hasenheide.data.볼륨
import com.slayde.hasenheide.data.세션종목
import com.slayde.hasenheide.data.앱데이터
import com.slayde.hasenheide.data.오늘볼륨
import com.slayde.hasenheide.data.운동세션
import com.slayde.hasenheide.data.운동저장하기
import com.slayde.hasenheide.data.유효세트
import com.slayde.hasenheide.data.일RM
import com.slayde.hasenheide.data.재개
import com.slayde.hasenheide.data.정식세트
import com.slayde.hasenheide.data.종목추이
import com.slayde.hasenheide.data.종목향상
import com.slayde.hasenheide.data.지금기준
import com.slayde.hasenheide.data.직전기록
import com.slayde.hasenheide.data.찬것
import com.slayde.hasenheide.data.찬세트수
import com.slayde.hasenheide.data.콤마
import com.slayde.hasenheide.data.퍼센트
import com.slayde.hasenheide.data.한세트수
import com.slayde.hasenheide.data.흐른초
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.글꼴
import com.slayde.hasenheide.ui.theme.크기
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * 운동 보고서(결과) 화면 — 10-05 옮기기 1단계에서 WorkoutScreen.kt 에서 떼어 냈다 (동작 · 모양 그대로).
 *
 *  · [마무리] — 운동을 끝낸 화면(운동화면이 S.끝화면 일 때 부른다)과 저장된 뒤 다시 보는 [결과화면] 이 같이 쓴다
 *  · 2단계 R(보고서) 도우미가 이 파일만 고친다 — 시안 `결과뷰` · `큰운동판` · 보고서 단추 · 상세 시트
 *  · 운동 화면과 같이 쓰는 것(루틴표시 · 향상줄 · 띠 · 펼침단추 …)은 다른 파일에 그대로 있다
 */

// ─────────────── 마무리 — [루틴 이름] 달성 (09-21 메모) ───────────────

/** 증감 글자 — 늘면 빨강 ▲, 줄면 파랑 ▼ (6-3) */
private fun 증감표(차: Double, 오름: Color, 내림: Color, 글로: (Double) -> String): androidx.compose.ui.text.AnnotatedString =
    buildAnnotatedString {
        when {
            차 > 0 -> withStyle(SpanStyle(color = 오름, fontWeight = FontWeight.Bold)) { append(" ▲${글로(차)}") }
            차 < 0 -> withStyle(SpanStyle(color = 내림, fontWeight = FontWeight.Bold)) { append(" ▼${글로(-차)}") }
            else -> {}
        }
    }

/**
 * 마무리 화면 — 스크롤 없이 한 화면. 종목이 많으면 가운데 상자 안에서만 위아래로 넘긴다.
 * 운동 시간은 이 화면에 들어온 때에서 멈춘다.
 */
@Composable
internal fun 마무리(상태: 앱상태, S: 운동세션, 저장됨: Boolean = false) {
    val c = Local색.current
    // 10-01 감시관: 저장된 뒤 보는 결과는 **방금 저장한 그 기록을 빼고** 견준다 (자기 자신과 견줘 늘 ▲0 이던 것)
    val d = if (!저장됨) 상태.d else 상태.d.let { dd ->
        val 끝 = S.끝시각
        val 열쇠 = dd.기록.entries.lastOrNull { (_, r) -> r.루틴id == S.루틴id && (끝 == null || r.끝시각 == 끝) }?.key
        if (열쇠 == null) dd else dd.copy(기록 = dd.기록 - 열쇠)
    }
    val 기간 = d.지금기준().기간
    val 오늘 = 상태.오늘
    val 달성 = S.한세트수() >= S.목표세트()
    val 직전 = d.직전기록(S.루틴id, 오늘)?.second
    val 직전세트 = 직전?.let { 정식세트(it) }
    var 열린종목 by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(horizontal = 간격.넓게)) {
        // ── 제목 띠 (09-27) — "가슴 루틴 [달성]" 가운데. 왼쪽부터 드러나며 옅게 → 진하게 ──
        val 드 = 드러남값()
        띠(Modifier.padding(top = 12.dp), 모서리값 = RoundedCornerShape(12.dp), 가운데 = true) {
            Row(Modifier.드러남(드), verticalAlignment = Alignment.CenterVertically) {
                띠글(루틴표시(S.루틴이름), Modifier.weight(1f, fill = false))
                Box(Modifier.width(6.dp))
                달성알약(달성)
            }
        }
        // ── 세트 · 시간 · 볼륨 — 0 부터 올라간다 (자릿수 시간) ──
        val 세트 = S.찬세트수()   // 체크한 세트 전부 (워밍업 포함 — 0d495c6 전과 같게)
        val 초 = S.흐른초(System.currentTimeMillis())
        val 볼 = S.오늘볼륨()
        val 세트움 = 움직수(세트.toDouble(), 영부터 = true)
        val 초움 = 움직수(초.toDouble(), 영부터 = true)
        val 볼움 = 움직수(볼, 영부터 = true)
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
            listOf(
                "${세트움.roundToInt()}" to "세트",
                시계글(초움.roundToLong()) to "시간",
                "${콤마(볼움)}kg" to "볼륨",
            ).forEach { (v, 이름) ->
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    글(v, 크기값 = 크기.크게, 굵기 = FontWeight.Bold)
                    글(이름, 크기값 = 크기.아주작게, 색 = c.흐림)
                }
            }
        }
        // ── 종목 상자 — 이 안에서만 넘긴다 ──
        카드(Modifier.weight(1f), 안쪽 = 0.dp) {
            // 맨 위: 루틴 정보. 기본 볼륨 · 1RM 은 움직이지 않고, ▲▼ 칩만 0 부터 올라간다 (09-27)
            Column(Modifier.fillMaxWidth().background(c.면2).padding(horizontal = 12.dp, vertical = 10.dp)) {
                제목글(루틴표시(S.루틴이름), 크기값 = 크기.버튼)
                val 루향 = d.루틴향상(S.루틴id, S.유효세트(), 오늘)
                향상줄("볼륨 ${콤마(볼륨(S.유효세트()))}kg", 루향, Modifier.padding(top = 3.dp), 영부터 = true)
                글("${S.종목들.count { !it.임시 }}종목 · 달성도 ${S.루틴달성도()}%", Modifier.padding(top = 2.dp), 크기값 = 크기.작게, 색 = c.옅음)
            }
            구분선()
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                S.종목들.filter { it.찬것().isNotEmpty() }.forEach { e ->
                    val 열림 = 열린종목 == e.이름
                    Row(Modifier.fillMaxWidth().눌림 { 열린종목 = if (열림) null else e.이름 }.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        val 찬 = e.찬것()
                        val 향 = d.종목향상(e.이름, 찬, 오늘, 묶음 = S.묶음이름(e))   // 10-02: 같은 묶음끼리만 (02 8-2)
                        Column(Modifier.weight(1f)) {
                            제목글(e.이름, 크기값 = 크기.조금작게)
                            향상줄("1RM ${kg글(찬.maxOf { 일RM(it.w, it.r) })}kg", 향?.rm, Modifier.padding(top = 2.dp), 영부터 = true)
                            향상줄("볼륨 ${콤마(볼륨(찬))}kg", 향?.볼륨, Modifier.padding(top = 2.dp), 영부터 = true)
                        }
                        펼침단추(열림) { 열린종목 = if (열림) null else e.이름 }
                    }
                    if (열림) 종목그래프(d, e, 오늘)
                    구분선()
                }
            }
        }
        Box(Modifier.height(12.dp))
        if (저장됨) {
            // 10-01: 자동 종료 · 다른 탭으로 나가며 이미 저장된 운동 — 결과만 한 번 보여 준다
            글("기록은 저장되었습니다", Modifier.fillMaxWidth(), 크기값 = 크기.작게, 색 = c.옅음, 가운데 = true)
            Box(Modifier.height(6.dp))
            버튼("확인", { 상태.바꿈 { it.copy(결과 = null) } }, Modifier.fillMaxWidth(), 주요 = true)
            Box(Modifier.height(12.dp))
            return@Column
        }
        // 10-01: 저장할 때 플랜 회차 · 누적 운동량 · 측정까지 (운동저장하기)
        버튼("기록 저장하고 끝내기", { 상태.바꿈 { it.운동저장하기(오늘, System.currentTimeMillis()) } }, Modifier.fillMaxWidth(), 주요 = true)
        Box(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            버튼("운동으로 돌아가기", { 상태.바꿈 { dd -> dd.copy(세션 = dd.세션?.재개(System.currentTimeMillis())) } }, Modifier.weight(1f), 작게 = true)
            버튼("기록 없이 끝내기", { 상태.지우고알림("기록 없이 끝냈습니다") { it.copy(세션 = null) } }, Modifier.weight(1f), 작게 = true, 글색 = c.나쁨)
        }
        Box(Modifier.height(12.dp))
    }
}

/** 운동 시간 — "58:20" / 한 시간 넘으면 "1:02:05" */
private fun 시계글(초: Long): String {
    val s = max(0L, 초); val h = s / 3600; val m = (s % 3600) / 60; val x = s % 60
    return if (h > 0) "$h:${m.toString().padStart(2, '0')}:${x.toString().padStart(2, '0')}" else "$m:${x.toString().padStart(2, '0')}"
}

/** 종목 변화 그래프 — 일 · 주 · 월, 볼륨 / 1RM. 점을 누르면 그 값 */
@Composable
private fun 종목그래프(d: 앱데이터, e: 세션종목, 오늘: String) {
    val c = Local색.current
    var 단위 by remember { mutableStateOf(묶기.일) }
    var 일rm by remember { mutableStateOf(false) }
    val 점들 = d.종목추이(e.이름, 단위, 일rm, 오늘, e.찬것())
    var 고른 by remember(단위, 일rm) { mutableStateOf(점들.size - 1) }
    Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            칩줄(묶기.entries.map { it.이름 } + listOf("볼륨", "1RM"), null, { t ->
                when (t) { "볼륨" -> 일rm = false; "1RM" -> 일rm = true; else -> 단위 = 묶기.entries.first { it.이름 == t } }
            }, Modifier.weight(1f))
        }
        글("${단위.이름} 단위 · ${if (일rm) "최고 1RM(kg)" else "볼륨 합(kg)"}", Modifier.padding(top = 4.dp), 크기값 = 크기.작게, 색 = c.옅음)
        if (점들.size < 2) 글("기록이 두 번 이상 쌓이면 선이 그려집니다", Modifier.padding(vertical = 16.dp), 크기값 = 크기.조금작게, 색 = c.옅음)
        else 선그림(점들.map { it.값 }, 점들.map { it.날 }, 일rm, 고른) { 고른 = it }
    }
}

@Composable
private fun 선그림(값: List<Double>, 날들: List<String>, 일rm: Boolean, 고른: Int, on고름: (Int) -> Unit) {
    val c = Local색.current
    Column {
        val 최소 = 값.min(); val 최대 = 값.max()
        val 폭값 = if (최대 - 최소 < 1e-6) 1.0 else 최대 - 최소
        val 선색 = c.강조; val 바닥색 = c.선; val 면색 = c.면
        Canvas(
            Modifier.fillMaxWidth().height(120.dp).padding(vertical = 8.dp)
                .pointerInput(값.size) {
                    detectTapGestures { p ->
                        on고름(((p.x / size.width) * (값.size - 1)).let { Math.round(it) }.coerceIn(0, 값.size - 1))
                    }
                },
        ) {
            val w = size.width; val hgt = size.height
            fun xy(n: Int) = Offset(w * n / (값.size - 1).toFloat(), hgt - (hgt * ((값[n] - 최소) / 폭값)).toFloat())
            drawLine(바닥색, Offset(0f, hgt), Offset(w, hgt), strokeWidth = 1.dp.toPx())
            val 길 = Path().apply { 값.indices.forEach { n -> val q = xy(n); if (n == 0) moveTo(q.x, q.y) else lineTo(q.x, q.y) } }
            drawPath(길, 선색, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            val 고름 = 고른.coerceIn(0, 값.size - 1)
            값.indices.forEach { n ->
                val q = xy(n)
                val r = if (n == 고름) 5.dp.toPx() else 3.dp.toPx()
                drawCircle(면색, r + 2.dp.toPx(), q)
                drawCircle(선색, r, q)
            }
        }
        val 고름 = 고른.coerceIn(0, 값.size - 1)
        val 첫 = 값.first()
        val p = 퍼센트(값[고름], 첫)
        Row(verticalAlignment = Alignment.CenterVertically) {
            글("${날들[고름]} · ${if (일rm) "%.1f".format(값[고름]) else 콤마(값[고름])}kg", 크기값 = 크기.조금작게, 색 = c.글, 굵기 = FontWeight.Bold)
            if (p != null && 고름 > 0) Text(증감표(p.toDouble(), c.오름, c.내림) { "${it.toInt()}%" }, style = 글꼴.보통(크기.작게), maxLines = 1, overflow = TextOverflow.Clip)
            글("  첫 점 대비", 크기값 = 크기.작게, 색 = c.옅음)
        }
    }
}

/** 저장된 뒤 한 번 보여 주는 결과 화면 (10-01) — App.kt 가 부른다 */
@Composable
fun 결과화면(상태: 앱상태, S: 운동세션) {
    Box(Modifier.fillMaxSize()) { 마무리(상태, S, 저장됨 = true) }
}
