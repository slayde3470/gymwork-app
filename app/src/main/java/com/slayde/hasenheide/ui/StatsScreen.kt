package com.slayde.hasenheide.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.slayde.hasenheide.data.무게글
import com.slayde.hasenheide.data.무게반올림
import com.slayde.hasenheide.data.콤마
import com.slayde.hasenheide.data.스탯
import com.slayde.hasenheide.data.스탯값
import com.slayde.hasenheide.data.스탯들
import com.slayde.hasenheide.data.스탯전
import com.slayde.hasenheide.data.스탯표
import com.slayde.hasenheide.data.시각날
import com.slayde.hasenheide.data.앱데이터
import com.slayde.hasenheide.data.업적
import com.slayde.hasenheide.data.업적글
import com.slayde.hasenheide.data.업적단위
import com.slayde.hasenheide.data.업적진행들
import com.slayde.hasenheide.data.업적표
import com.slayde.hasenheide.data.대표칭호고름
import com.slayde.hasenheide.data.판정됨
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.높이
import com.slayde.hasenheide.ui.theme.모서리
import com.slayde.hasenheide.ui.theme.선굵기
import com.slayde.hasenheide.ui.theme.스탯치수
import com.slayde.hasenheide.ui.theme.색표
import com.slayde.hasenheide.ui.theme.크기
import kotlinx.coroutines.delay

/**
 * 스탯 · 업적 화면 (10-02 · 홍겸 님 "프린세스메이커처럼 스탯 · 업적 일단 다 업데이트")
 *
 * 캘린더 맨 위 띠의 칩으로 들어온다 — 화면 전체를 덮고 ✕ · 뒤로가기로 닫는다.
 * ⚠ 어느 탭에 둘지 · 스탯창 모양(방사형 · 막대 · 레벨)은 아직 정하지 않았다 (12-3 '대화로 정함') → 지금은 막대 [기본값]
 *
 *  · 스탯: 맨 위 체력 · 막대 + 값 + **7일 전 대비 ▲ +n** (12-1 ⑩ '얼마나 올랐는지 꼭') · 값이 없으면 '—' 와 이유 한 줄
 *  · 업적: 칩줄로 분류 고르기 (전체 · 일반 분류들 · 히든) · 달성 수 / 전체
 *    잠김은 흐리게(실루엣) + 조건 + 진행 막대 · 히든 잠김은 ??? · 풀림은 등급 색 테두리 + 문구 + 플레이버 + 달성 날짜
 *    풀린 칭호를 누르면 바로 대표 칭호 (다시 누르면 뗀다) — 아래띠로 알린다
 */
@Composable
fun 스탯화면(상태: 앱상태, 처음: String = 스탯화면글.스탯, 닫기: () -> Unit) {
    val c = Local색.current
    val d = 상태.d
    val 오늘 = 상태.오늘
    var 쪽 by remember { mutableStateOf(처음) }
    var 알림 by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(알림) { if (알림 != null) { delay(3_000); 알림 = null } }
    BackHandler { 닫기() }

    Box(Modifier.fillMaxSize().background(c.바탕).눌림 { }) {
        // 앱 전체를 덮으므로 위 · 아래 시스템 막대 자리를 스스로 비운다 (App 의 Column 과 같게)
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 간격.보통).padding(top = 간격.좁게, bottom = 간격.좁게)) {
            띠(모서리값 = RoundedCornerShape(모서리.작게)) {
                띠글(스탯화면글.제목, Modifier.weight(1f))
                아이콘버튼(아이콘.닫기, "닫기", 닫기, 칠함 = false, 색 = c.강조글, 크기칸 = 높이.아주낮게)
            }
            Box(Modifier.height(간격.좁게))
            칩줄(listOf(스탯화면글.스탯, 스탯화면글.업적), 쪽, { 쪽 = it })
            Box(Modifier.height(간격.좁게))
            if (쪽 == 스탯화면글.스탯) 스탯판(d, 오늘, Modifier.weight(1f))
            else 업적판(d, 오늘, Modifier.weight(1f)) { 칭호번호 ->
                val 전 = 상태.d.대표칭호
                상태.바꿈 { it.대표칭호고름(칭호번호) }
                val 이름 = 업적표.칭호찾기(칭호번호)?.칭호 ?: ""
                알림 = if (전 == 칭호번호) 스탯화면글.대표뗌 else "${스탯화면글.대표} 『$이름』"
            }
        }
        알림?.let { 아래띠(it, 스탯화면글.닫기, { 알림 = null }, 바깥 = Modifier.navigationBarsPadding()) }
    }
}

/** 이 화면의 글 — 한곳에 */
object 스탯화면글 {
    const val 제목 = "스탯 · 업적"
    const val 스탯 = "스탯"
    const val 업적 = "업적"
    const val 전체 = "전체"
    const val 대비 = "7일 전 대비"
    const val 없음 = "—"
    const val 대표 = "대표 칭호"
    const val 대표뗌 = "대표 칭호를 뗐습니다"
    const val 닫기 = "닫기"
}

// ─────────────── 스탯 ───────────────

@Composable
private fun 스탯판(d: 앱데이터, 오늘: String, modifier: Modifier) {
    val 들 = remember(d, 오늘) { d.스탯들(오늘) }
    val 전 = remember(d, 오늘) { d.스탯전(오늘) }
    카드(modifier, 안쪽 = 0.dp) {   // 줄마다 자기 여백을 준다 (U3-7)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(vertical = 간격.좁게)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 간격.보통, vertical = 간격.아주좁게)) {
                이름표(스탯화면글.스탯, Modifier.weight(1f))
                이름표(스탯화면글.대비)
            }
            들.forEach { s -> 스탯줄(s, 전[s.스탯.name], s.스탯 == 스탯.체력) }
        }
    }
}

/** 스탯 한 줄 — 이름 · 막대 · 값 · 7일 전 대비. 값이 없으면 이유 한 줄 · '—' */
@Composable
private fun 스탯줄(s: 스탯값, 전값: Double?, 큰: Boolean) {
    val c = Local색.current
    val v = s.값
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 간격.보통, vertical = 간격.아주좁게),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        글(s.스탯.이름, Modifier.width(스탯치수.이름), 크기값 = if (큰) 크기.본문 else 크기.조금작게,
            색 = if (v == null) c.옅음 else c.글, 굵기 = if (큰) FontWeight.Bold else FontWeight.Medium)
        if (v == null) {
            글(s.이유 ?: "", Modifier.weight(1f), 크기값 = 크기.작게, 색 = c.옅음)
            글(스탯화면글.없음, Modifier.width(스탯치수.값), 크기값 = 크기.조금작게, 색 = c.옅음, 가운데 = true)
            Box(Modifier.width(스탯치수.증감))
            return@Row
        }
        진행막대((v / 스탯표.최대).toFloat(), Modifier.weight(1f))
        글(s.덧글 ?: "${Math.round(v)}", Modifier.width(스탯치수.값), 크기값 = if (큰) 크기.본문 else 크기.조금작게,
            굵기 = FontWeight.Bold, 가운데 = true)
        val 차 = 전값?.let { v - it }
        Box(Modifier.width(스탯치수.증감), contentAlignment = Alignment.CenterEnd) {
            when {
                차 == null -> {}
                차 > 0.05 -> 글("▲ +${무게글(무게반올림(차))}", 크기값 = 크기.작게, 색 = c.오름, 굵기 = FontWeight.Bold)
                차 < -0.05 -> 글("▼ ${무게글(무게반올림(-차))}", 크기값 = 크기.작게, 색 = c.내림, 굵기 = FontWeight.Bold)
                else -> 글("유지", 크기값 = 크기.작게, 색 = c.흐림)
            }
        }
    }
}

// ─────────────── 업적 ───────────────

@Composable
private fun 업적판(d: 앱데이터, 오늘: String, modifier: Modifier, on대표: (String) -> Unit) {
    // 분류 칩 — 전체 · 일반 분류(번호판 순서) · 히든. 처음엔 첫 일반 분류 (107개를 한 화면 분량으로 나눈다)
    val 분류들 = remember { listOf(스탯화면글.전체) + 업적표.목록.filter { !it.숨김 }.map { it.분류 }.distinct() + 업적글.히든 }
    var 고른 by remember { mutableStateOf(분류들[1]) }
    val 진행 = remember(d, 오늘) { d.업적진행들(오늘) }
    val 목록 = 업적표.목록.filter {
        when (고른) { 스탯화면글.전체 -> true; 업적글.히든 -> it.숨김; else -> !it.숨김 && it.분류 == 고른 }
    }
    val 얻은수 = 목록.count { it.번호 in d.업적 }
    Column(modifier) {
        Row(Modifier.fillMaxWidth().padding(bottom = 간격.아주좁게)) {
            이름표("${고른} ${얻은수} / ${목록.size}", Modifier.weight(1f))
            if (고른 != 스탯화면글.전체) 이름표("${스탯화면글.전체} ${d.업적.size} / ${업적표.목록.size}")
        }
        칩줄(분류들, 고른, { 고른 = it })
        Box(Modifier.height(간격.좁게))
        LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
            items(목록, key = { it.번호 }) { a ->
                업적줄(a, d.업적[a.번호], d.대표칭호 == a.칭호번호, 진행[a.번호]) { on대표(a.칭호번호) }
            }
        }
    }
}

/** 등급 → 테두리 색 (Theme 색표 · 11 지침 U1) */
private fun 등급색(a: 업적, c: 색표): Color = when (a.등급) {
    "초보" -> c.초보; "중급" -> c.중급; "고급" -> c.고급; "미친자" -> c.미친자; "유머" -> c.유머
    else -> c.히든
}

private fun 날짜글(ms: Long): String = 시각날(ms).replace('-', '.')

private fun 수글(x: Double): String = if (x >= 1000) 콤마(x) else 무게글(무게반올림(x))

/**
 * 업적 한 줄.
 *  · 일반 잠김: 칭호 흐리게(실루엣) · 조건 · 진행 막대
 *  · 히든 잠김: ??? · ???
 *  · 풀림: 등급 색 굵은 테두리 · 칭호 · 문구 · 조건 · 플레이버(흐리고 작게) · 달성 날짜. 누르면 대표 칭호
 *  · 아직 판정하지 않는 업적은 오른쪽에 '준비 중'
 */
@Composable
private fun 업적줄(a: 업적, 얻은: Long?, 대표: Boolean, 진행: Pair<Double, Double>?, on누름: () -> Unit) {
    val c = Local색.current
    val 잠김 = 얻은 == null
    val 숨김 = 잠김 && a.숨김
    val 색 = 등급색(a, c)
    val 모양 = RoundedCornerShape(모서리.작게)
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 간격.아주좁게)
            .clip(모양)
            .background(c.면)
            .border(if (잠김) 선굵기.보통 else 선굵기.굵게, if (잠김) c.속선 else 색, 모양)
            .then(if (잠김) Modifier else Modifier.눌림(on누름))
            .padding(horizontal = 간격.보통, vertical = 간격.좁게),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            글(if (숨김) 업적글.숨은이름 else a.칭호, Modifier.weight(1f), 크기값 = 크기.본문,
                색 = if (잠김) c.옅음 else 색, 굵기 = FontWeight.Bold)
            val 곁 = when {
                !잠김 -> (if (대표) "${스탯화면글.대표} · " else "") + 날짜글(얻은!!)
                !a.판정됨 -> "${a.번호} · ${업적글.준비중}"
                else -> "${a.번호} · ${a.등급.ifEmpty { 업적글.히든 }}"
            }
            글(곁, 크기값 = 크기.작게, 색 = if (대표) c.강조 else c.옅음, 굵기 = if (대표) FontWeight.Bold else FontWeight.Normal)
        }
        if (숨김) {
            글(업적글.숨은이름, 크기값 = 크기.작게, 색 = c.옅음)
            return@Column
        }
        if (!잠김) 글(a.문구, 크기값 = 크기.조금작게, 색 = c.흐림)
        // 조건은 CSV 글 그대로라 긴 것이 있다 (1-1 '벤치프레스 · 백 스쿼트 · 데드리프트 1RM(…) 합계 200kg 이상') → 두 줄까지 (U2-8)
        글(a.조건, 크기값 = 크기.작게, 색 = c.옅음, 줄 = 2)
        if (잠김 && 진행 != null) {
            Row(Modifier.padding(top = 간격.아주좁게), verticalAlignment = Alignment.CenterVertically) {
                진행막대((진행.first / 진행.second).toFloat(), Modifier.weight(1f))
                Box(Modifier.width(간격.좁게))
                글("${수글(진행.first)} / ${수글(진행.second)}${업적단위(a.번호)}", 크기값 = 크기.작게, 색 = c.흐림)
            }
        }
        // 플레이버 — 흐리고 작게 (12-6 다크 소울 방식). 이야기 글이라 두 줄까지
        if (!잠김 && a.플레이버.isNotBlank()) 글(a.플레이버, Modifier.padding(top = 간격.아주좁게), 크기값 = 크기.작게, 색 = c.옅음, 줄 = 2)
    }
}
