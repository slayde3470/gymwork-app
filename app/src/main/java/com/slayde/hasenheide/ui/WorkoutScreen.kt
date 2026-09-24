package com.slayde.hasenheide.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.slayde.hasenheide.data.값고치기
import com.slayde.hasenheide.data.다음으로
import com.slayde.hasenheide.data.다음종목으로
import com.slayde.hasenheide.data.달성도
import com.slayde.hasenheide.data.덜한가
import com.slayde.hasenheide.data.루틴달성도
import com.slayde.hasenheide.data.루틴성장
import com.slayde.hasenheide.data.마감풀기
import com.slayde.hasenheide.data.목표볼륨
import com.slayde.hasenheide.data.목표세트
import com.slayde.hasenheide.data.묶기
import com.slayde.hasenheide.data.묶음이름
import com.slayde.hasenheide.data.무게글
import com.slayde.hasenheide.data.볼륨
import com.slayde.hasenheide.data.분초
import com.slayde.hasenheide.data.불러오기
import com.slayde.hasenheide.data.세션종목
import com.slayde.hasenheide.data.세트값
import com.slayde.hasenheide.data.세트삭제
import com.slayde.hasenheide.data.세트추가
import com.slayde.hasenheide.data.세트휴식
import com.slayde.hasenheide.data.시분초
import com.slayde.hasenheide.data.식구
import com.slayde.hasenheide.data.앱데이터
import com.slayde.hasenheide.data.오늘볼륨
import com.slayde.hasenheide.data.운동세션
import com.slayde.hasenheide.data.운동저장
import com.slayde.hasenheide.data.유효세트
import com.slayde.hasenheide.data.일RM
import com.slayde.hasenheide.data.재개
import com.slayde.hasenheide.data.정식세트
import com.slayde.hasenheide.data.종목성장
import com.slayde.hasenheide.data.타이트하게
import com.slayde.hasenheide.data.종목으로
import com.slayde.hasenheide.data.종목추이
import com.slayde.hasenheide.data.지금기준
import com.slayde.hasenheide.data.지금종목
import com.slayde.hasenheide.data.직전기록
import com.slayde.hasenheide.data.찬것
import com.slayde.hasenheide.data.체크
import com.slayde.hasenheide.data.초읽기
import com.slayde.hasenheide.data.총칸
import com.slayde.hasenheide.data.칸
import com.slayde.hasenheide.data.콤마
import com.slayde.hasenheide.data.퍼센트
import com.slayde.hasenheide.data.한세트수
import com.slayde.hasenheide.data.흐른초
import com.slayde.hasenheide.data.휴식고치기
import com.slayde.hasenheide.data.휴식끝
import com.slayde.hasenheide.data.휴식보임
import com.slayde.hasenheide.data.휴식자리
import com.slayde.hasenheide.data.끝냄
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.글꼴
import com.slayde.hasenheide.ui.theme.높이
import com.slayde.hasenheide.ui.theme.모서리
import com.slayde.hasenheide.ui.theme.크기
import kotlinx.coroutines.delay
import kotlin.math.max

/**
 * 운동 실행 화면 (기능명세 5).
 *
 * 09-21 메모로 바뀐 것
 *  · 오늘 종목을 전부 한 목록에 — 지난 종목도 위로 올려 볼 수 있다. 지금 종목으로 저절로 옮겨 간다
 *  · 슈퍼세트는 한 상자에 번갈아(1A 1B 2A 2B …). 휴식은 마지막 종목 줄에만
 *  · 휴식은 세트 줄을 덮지 않고, 그 줄의 '휴식' 칸이 줄어들며 색이 바뀐다
 *  · 휴식 끝나는 '시각'을 기억하므로 화면이 꺼졌다 켜져도 어긋나지 않는다 (5-4)
 */
@Composable
fun 운동화면(상태: 앱상태, 폰: 폰기능) {
    val c = Local색.current
    val d = 상태.d
    val S = d.세션 ?: return
    var 지금 by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var 열린세트 by remember { mutableStateOf<String?>(null) }   // "j|k"
    var 켠칸 by remember { mutableStateOf<String?>(null) }
    var 열린시트 by remember { mutableStateOf<String?>(null) }   // 목록 · 추가 · 마칠까

    fun 바꿈(f: (운동세션) -> 운동세션) = 상태.바꿈 { dd -> dd.세션?.let { dd.copy(세션 = f(it)) } ?: dd }
    // 뒤로가기 — 열어 둔 세트 설정칸을 먼저 닫는다 (09-22 메모)
    BackHandler(enabled = 열린세트 != null && 열린시트 == null) { 열린세트 = null; 켠칸 = null }

    // 화면 시계 — 휴식 끝 알림은 앱 전체 시계(App.kt)가 맡는다. 다른 탭을 봐도 돈다
    LaunchedEffect(Unit) { while (true) { 지금 = System.currentTimeMillis(); delay(250) } }
    // 운동 중에는 화면을 켜 둔다 (설정에서 끌 수 있다)
    val 뷰 = LocalView.current
    DisposableEffect(d.설정.화면유지) {
        뷰.keepScreenOn = d.설정.화면유지
        onDispose { 뷰.keepScreenOn = false }
    }

    Box(Modifier.fillMaxSize()) {
        if (S.끝화면) 마무리(상태, S) else Column(Modifier.fillMaxSize()) {
            머리줄(S, 지금) { 바꿈 { it.끝냄(System.currentTimeMillis()) } }
            val 스크롤 = rememberScrollState()
            var 화면틀 by remember { mutableStateOf(Rect.Zero) }
            var 지금줄 by remember { mutableStateOf<Rect?>(null) }
            var 지금틀 by remember { mutableStateOf<Rect?>(null) }   // 지금 종목 상자의 자리 (상단 고정을 켤지 판단)
            val 지금머리 = S.식구(S.i).first()
            // 펼침을 따로 정해 둔 묶음 (묶음 첫 종목 번호 → 펼침 여부). 정해 두지 않으면 '지금 종목만 펼침'
            val 접기 = remember { mutableStateMapOf<Int, Boolean>() }
            // 종목이 바뀌면 다 되돌린다. 세트를 체크하면 지금 종목 말고는 다시 접는다 (09-24 메모)
            LaunchedEffect(S.i) { 접기.clear() }
            LaunchedEffect(S.한세트수()) {
                val 남길 = 접기[지금머리]
                접기.clear()
                if (남길 != null) 접기[지금머리] = 남길
            }
            // 지금 세트 줄이 **화면 밖에 있을 때만** 최소한으로 움직인다 (09-24 메모: 화면이 왔다갔다해서 피로하다)
            LaunchedEffect(S.i, S.s, S.한세트수(), S.휴식?.k) {
                delay(140)   // 접힘 애니메이션이 자리를 잡은 뒤에
                val 줄 = 지금줄 ?: return@LaunchedEffect
                if (화면틀.height <= 0f) return@LaunchedEffect
                val 위 = 화면틀.top + 8f
                val 아래 = 화면틀.bottom - 8f
                val 밀 = when {
                    줄.top < 위 -> 줄.top - 위
                    줄.bottom > 아래 -> minOf(줄.bottom - 아래, 줄.top - 위)
                    else -> 0f
                }
                if (밀 > 2f || 밀 < -2f) 스크롤.animateScrollBy(밀)
            }
            Box(Modifier.weight(1f)) {
            Column(Modifier.fillMaxSize().onGloballyPositioned { 화면틀 = it.boundsInRoot() }.verticalScroll(스크롤).padding(horizontal = 간격.보통)) {
                val 그린 = mutableSetOf<Int>()
                S.종목들.indices.forEach { j ->
                    if (j in 그린) return@forEach
                    val 식구 = S.식구(j)
                    그린.addAll(식구)
                    val 머리 = 식구.first()
                    val 접힘 = !(접기[머리] ?: (머리 == 지금머리))
                    Column(Modifier.onGloballyPositioned { b ->
                        val r = b.boundsInRoot()
                        if (머리 == 지금머리) 지금틀 = r
                        // 지금 종목이 아닌데 펼쳐 둔 것이 화면 밖으로 나가면 저절로 접는다 (09-24 메모)
                        else if (접기[머리] == true && (r.bottom < 화면틀.top || r.top > 화면틀.bottom)) 접기[머리] = false
                    }) {
                        종목묶음(상태, S, 식구, 지금, 열린세트, 켠칸, 접힘,
                            on접기 = { 접기[머리] = 접힘 },
                            on열기 = { key -> 입력중.취소?.invoke(); 열린세트 = if (열린세트 == key) null else key; 켠칸 = null },
                            on칸 = { key, f -> 열린세트 = key; 켠칸 = if (켠칸 == f) null else f },
                            on지금줄 = { 지금줄 = it },
                            바꿈 = ::바꿈)
                    }
                }
                Box(Modifier.height(16.dp))
            }
            // 지금 종목의 머리가 위로 밀려 사라지면 그 자리에 붙여 둔다 (09-24 홍겸 님 제안)
            val 고정 = 지금틀?.let { it.top < 화면틀.top - 2f && 화면틀.height > 0f } == true
            if (고정) 고정머리(S, Modifier.align(Alignment.TopCenter))
            }
            // 아랫줄 — 운동 목록 · 운동 추가 · 다음
            Row(Modifier.fillMaxWidth().background(c.면).padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                버튼("운동 목록", { 열린시트 = "목록" }, Modifier.weight(1f), 작게 = true, 그림 = 아이콘.목록)
                버튼("운동 추가", { 열린시트 = "추가" }, Modifier.weight(1f), 작게 = true, 그림 = 아이콘.더하기)
                버튼("다음", {
                    if (S.지금종목.덜한가()) 열린시트 = "마칠까" else { 바꿈 { it.다음종목으로(false, System.currentTimeMillis()) }; 열린세트 = null }
                }, Modifier.weight(1f), 작게 = true, 그림 = 아이콘.오른쪽)
            }
        }
        when (열린시트) {
            "목록" -> 시트("오늘 운동 목록", { 열린시트 = null }) {
                글("누르면 이동 · 하던 자리는 그대로", 크기값 = 크기.조금작게, 색 = c.옅음)
                // 오늘만 타이트하게 — 아랫줄을 늘리지 않으려고 여기에 둔다 (09-24)
                버튼("오늘만 타이트하게", { 열린시트 = "타이트" }, Modifier.fillMaxWidth().padding(top = 8.dp), 작게 = true)
                S.종목들.forEachIndexed { j, e ->
                    고르기줄(e.이름, "${e.찬것().size}/${e.총칸()}세트" + (if (e.마감) " · 마침" else "") + (if (e.임시) " · 오늘만" else ""),
                        if (j == S.i) 아이콘.체크 else 아이콘.오른쪽, 흐림 = e.마감) {
                        바꿈 { it.종목으로(j) }; 열린세트 = null; 열린시트 = null
                    }
                }
            }
            "추가" -> {
                var 부위 by remember { mutableStateOf("전체") }
                시트("운동 추가", { 열린시트 = null }) {
                    글("오늘만 · 루틴 통계 제외 · 종목 향상도엔 반영", 크기값 = 크기.조금작게, 색 = c.옅음)
                    Box(Modifier.height(8.dp))
                    칩줄(listOf("전체") + d.카테고리, 부위, { 부위 = it })
                    val 목록 = d.종목표.filter { 부위 == "전체" || it.부위 == 부위 }
                    if (목록.isEmpty()) 글("종목 탭에서 종목을 먼저 만들어 주세요", Modifier.padding(vertical = 12.dp), 크기값 = 크기.조금작게, 색 = c.옅음)
                    목록.forEach { e ->
                        고르기줄(e.이름, listOf(e.부위, e.장비).filter { it.isNotBlank() }.joinToString("·"), 아이콘.더하기) {
                            바꿈 { s ->
                                val 뒤 = s.식구(s.i).last() + 1
                                s.불러오기(e.이름, d.설정.기본휴식, d.설정.기본세트).종목으로(뒤)
                            }
                            열린시트 = null
                        }
                    }
                }
            }
            // 오늘만 타이트하게 (09-24 메모 · 업데이트 예정 ⑱) — 오늘 기록에만 적용, 루틴 원본은 그대로
            "타이트" -> {
                var 세트줄이기 by remember { mutableStateOf(true) }
                var 휴식줄이기 by remember { mutableStateOf(true) }
                시트("오늘만 타이트하게", { 열린시트 = null }) {
                    글("오늘 기록에만 적용 · 루틴 원본은 그대로", 크기값 = 크기.조금작게, 색 = c.옅음)
                    Box(Modifier.height(8.dp))
                    고름줄("덜 한 종목마다 마지막 세트 빼기", 세트줄이기) { 세트줄이기 = !세트줄이기 }
                    고름줄("남은 휴식 15초씩 줄이기", 휴식줄이기) { 휴식줄이기 = !휴식줄이기 }
                    val 뺄세트 = if (세트줄이기) S.종목들.count { it.덜한가() && it.총칸() > it.찬것().size + 0 } else 0
                    글("세트 ${S.종목들.sumOf { it.총칸() }} → ${S.종목들.sumOf { it.총칸() } - 뺄세트}", Modifier.padding(top = 8.dp), 크기값 = 크기.버튼, 색 = c.흐림)
                    Box(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                        버튼("취소", { 열린시트 = null }, Modifier.weight(1f), 작게 = true)
                        버튼("오늘만 적용", {
                            바꿈 { it.타이트하게(세트줄이기, if (휴식줄이기) 15 else 0) }
                            열린시트 = null
                        }, Modifier.weight(1f), 작게 = true, 주요 = true)
                    }
                }
            }
            "마칠까" -> 시트("운동이 완료되지 않았습니다", { 열린시트 = null }) {
                val e = S.지금종목
                글("${e.이름} ${e.총칸() - e.찬것().size}세트 남음 · 여기서 마칠까요?", 크기값 = 크기.버튼, 색 = c.흐림)
                Box(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    버튼("여기까지", { 바꿈 { it.다음종목으로(true, System.currentTimeMillis()) }; 열린시트 = null; 열린세트 = null }, Modifier.weight(1f), 주요 = true)
                    버튼("나중에 더", { 바꿈 { it.다음종목으로(false, System.currentTimeMillis()) }; 열린시트 = null; 열린세트 = null }, Modifier.weight(1f))
                }
                글("나중에 더 → 목록에 그대로 남습니다", Modifier.padding(top = 8.dp), 크기값 = 크기.작게, 색 = c.옅음)
            }
        }
    }
}

/** 고르는 줄 — 네모와 글씨 (09-24 타이트하게 시트) */
@Composable
private fun 고름줄(글자: String, 켬: Boolean, on누름: () -> Unit) {
    val c = Local색.current
    Row(
        Modifier.fillMaxWidth().height(높이.높게).눌림(on누름),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.좁게),
    ) {
        Box(
            Modifier.size(20.dp).clip(RoundedCornerShape(6.dp))
                .background(if (켬) c.강조 else c.면2).border(1.dp, if (켬) c.강조 else c.선, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center,
        ) { if (켬) Icon(아이콘.체크, null, Modifier.size(14.dp), tint = c.강조글) }
        글(글자, 크기값 = 크기.버튼)
    }
}

/**
 * 지금 하는 종목의 머리가 위로 사라졌을 때, 화면 맨 위에 붙여 두는 띠 (09-24 홍겸 님 제안).
 * 세트 번호만 위로 흘러가고 '무슨 종목을 하고 있는지'는 늘 보인다.
 */
@Composable
private fun 고정머리(S: 운동세션, modifier: Modifier) {
    val c = Local색.current
    val e = S.지금종목
    Row(
        modifier.fillMaxWidth().padding(horizontal = 간격.보통, vertical = 4.dp)
            .clip(RoundedCornerShape(모서리.작게))
            .background(c.면)
            .border(1.dp, c.강조.copy(alpha = 0.35f), RoundedCornerShape(모서리.작게))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        제목글(e.이름, Modifier.weight(1f), 크기값 = 크기.본문)
        글("${e.찬것().size}/${e.총칸()}세트", 크기값 = 크기.작게, 색 = c.흐림)
        글("${e.달성도()}%", 크기값 = 크기.작게, 색 = c.강조, 굵기 = FontWeight.Bold)
    }
}

@Composable
private fun 머리줄(S: 운동세션, 지금: Long, 끝내기: () -> Unit) {
    val c = Local색.current
    val 달 = S.루틴달성도()
    Column(Modifier.fillMaxWidth().background(c.면)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            제목글(S.루틴이름, 크기값 = 크기.버튼)
            글("${S.i + 1}/${S.종목들.size}", 크기값 = 크기.아주작게, 색 = c.흐림)
            글("달성도 ${달}%", 크기값 = 크기.아주작게, 색 = c.강조, 굵기 = FontWeight.Bold)
            글(시분초(S.흐른초(지금)), 크기값 = 크기.아주작게, 색 = c.흐림)
            글("${콤마(S.오늘볼륨())}/${콤마(S.목표볼륨())}", Modifier.weight(1f), 크기값 = 크기.아주작게, 색 = c.흐림)
            Box(
                Modifier.height(높이.아주낮게).clip(RoundedCornerShape(모서리.아주작게)).background(c.면2)
                    .border(1.dp, c.선, RoundedCornerShape(모서리.아주작게)).눌림(끝내기).padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center,
            ) { 글("운동 끝내기", 크기값 = 크기.작게, 색 = c.흐림, 굵기 = FontWeight.Bold) }
        }
        // 3px 막대 — 루틴 달성도
        Box(Modifier.fillMaxWidth().height(3.dp).background(c.면2)) {
            Box(Modifier.fillMaxWidth(달.coerceIn(0, 100) / 100f).height(3.dp).background(c.강조))
        }
    }
}

/** 종목 하나, 또는 슈퍼세트 묶음 하나 — 머리 + 세트 줄들 */
@Composable
private fun 종목묶음(
    상태: 앱상태, S: 운동세션, 식구: List<Int>, 지금: Long, 열린세트: String?, 켠칸: String?,
    접힘: Boolean, on접기: () -> Unit,
    on열기: (String) -> Unit, on칸: (String, String) -> Unit, on지금줄: (Rect) -> Unit, 바꿈: ((운동세션) -> 운동세션) -> Unit,
) {
    val c = Local색.current
    val 지금묶음 = S.i in 식구
    val 슈퍼 = 식구.size > 1
    Column(
        Modifier.fillMaxWidth().padding(top = 8.dp)
            .clip(RoundedCornerShape(모서리.작게))
            .then(
                if (슈퍼) Modifier.border(1.dp, c.휴식.copy(alpha = 0.45f), RoundedCornerShape(모서리.작게)).background(c.휴식옅음.copy(alpha = 0.4f))
                else if (지금묶음) Modifier.border(1.dp, c.강조.copy(alpha = 0.35f), RoundedCornerShape(모서리.작게))
                else Modifier
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .alpha(if (지금묶음) 1f else 0.8f),
    ) {
        // 이름을 누르면 그 종목으로, ∨ 를 누르면 펼치고 접는다 (09-24 메모)
        식구.forEachIndexed { n, j ->
            종목머리(상태, S, j, if (슈퍼) 글자표(n) else null, !접힘,
                on누름 = { if (j != S.i) 바꿈 { it.종목으로(j) } }, on접기 = on접기)
        }
        AnimatedVisibility(visible = !접힘) { Column {
            if (슈퍼) 글("슈퍼세트 · 덜 한 종목부터 · 모두 같아지면 휴식", Modifier.padding(start = 4.dp, bottom = 2.dp), 크기값 = 크기.아주작게, 색 = c.휴식)
            val 줄수 = 식구.maxOf { S.종목들[it].총칸() }
            for (k in 0 until 줄수) {
                식구.forEachIndexed { n, j ->
                    val e = S.종목들[j]
                    if (k >= e.총칸()) return@forEachIndexed
                    val key = "$j|$k"
                    세트줄(상태, S, j, k, if (슈퍼) "${k + 1}${글자표(n)}" else "${k + 1}", S.휴식보임(j), 지금,
                        열린세트 == key, 켠칸,
                        on열기 = { on열기(key) }, on칸 = { f -> on칸(key, f) }, on지금줄 = on지금줄, 바꿈 = 바꿈)
                }
            }
            if (지금묶음) {
                // ＋ — 맨 아래 세트를 베낀다 (슈퍼세트면 묶인 종목 전부에)
                Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier.size(높이.보통).clip(CircleShape).background(c.면).border(1.dp, c.선, CircleShape).눌림 { 바꿈 { it.세트추가(it.i) } },
                        contentAlignment = Alignment.Center,
                    ) { Icon(아이콘.더하기, "세트 추가", Modifier.size(20.dp), tint = c.강조) }
                }
                if (S.지금종목.마감) 버튼("남은 운동 마저 하기", { 바꿈 { it.마감풀기() } }, Modifier.fillMaxWidth().padding(bottom = 8.dp), 작게 = true)
            }
        } }
    }
}

/** 상자 안 루틴 이름 — '가슴, 팔' → '가슴, 팔 루틴'. 이름에 이미 '루틴'이 있으면 그대로 (09-21 메모) */
fun 루틴표시(이름: String): String = if (이름.contains("루틴")) 이름 else "$이름 루틴"

private fun 글자표(n: Int): String = ('A' + n).toString()

/** ▲7% · ▼2% (09-24) */
private fun 성장짧게(p: Int): String = (if (p >= 0) "▲" else "▼") + "${kotlin.math.abs(p)}%"

/**
 * 종목 머리 — 첫 줄은 이름, **둘째 줄에 정보** (09-24 메모: 한 줄에 같이 두면 종목 이름이 잘린다).
 * 오른쪽 ∨ 는 펼치기 · 접기. 어느 종목이든 언제나 있다 (09-24 메모: 열기만 있고 접기가 없었다).
 * 이름 쪽을 누르면 그 종목으로 간다.
 */
@Composable
private fun 종목머리(상태: 앱상태, S: 운동세션, j: Int, 표: String?, 펼침: Boolean, on누름: () -> Unit, on접기: () -> Unit) {
    val c = Local색.current
    val e = S.종목들[j]
    val 지금세트 = e.찬것()
    val 과거rm = 상태.d.기록.values.flatMap { r -> r.종목들.filter { it.이름 == e.이름 }.flatMap { it.세트들 } }.maxOfOrNull { 일RM(it.w, it.r) } ?: 0.0
    val rm = max(과거rm, 지금세트.maxOfOrNull { 일RM(it.w, it.r) } ?: 0.0)
    val 목표볼 = e.목표볼륨()
    val 뱃지 = listOfNotNull(if (e.임시) "오늘만" else null, if (e.마감) "마침" else null)
    val 지금것 = j == S.i
    // 향상도는 오늘 한 세트가 있을 때만 — 아직 시작도 안 한 종목에 지난 기록을 띄우지 않는다
    val 성장 = if (지금세트.isEmpty()) null else 상태.d.종목성장(e.이름, 상태.오늘, 지금세트, S.묶음이름(e))?.볼륨?.pct
    Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f).눌림(on누름), verticalAlignment = Alignment.CenterVertically) {
                글(표 ?: "%02d)".format(j + 1), 크기값 = 크기.조금작게, 색 = if (표 != null) c.휴식 else c.흐림, 굵기 = FontWeight.Bold)
                Box(Modifier.width(4.dp))
                제목글(e.이름, Modifier.weight(1f, fill = false), 크기값 = 크기.본문, 색 = if (지금것) c.글 else c.흐림)
                뱃지.forEach { b -> Box(Modifier.width(4.dp)); 알약(b, c.휴식) }
            }
            Box(Modifier.size(높이.낮게).눌림(on접기), contentAlignment = Alignment.Center) {
                Icon(아이콘.아래, if (펼침) "접기" else "펼치기", Modifier.size(16.dp).rotate(if (펼침) 180f else 0f), tint = c.옅음)
            }
        }
        Row(
            Modifier.fillMaxWidth().눌림(on누름).padding(top = 1.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (!펼침) 글("${지금세트.size}/${e.총칸()}세트", 크기값 = 크기.작게, 색 = c.흐림)
            else {
                글("${e.달성도()}%", 크기값 = 크기.작게, 색 = c.강조, 굵기 = FontWeight.Bold)
                글("1RM ${if (rm > 0) "%.1f".format(rm) else "—"}", 크기값 = 크기.작게, 색 = c.흐림)
                글("${콤마(볼륨(지금세트))}/${콤마(목표볼)}", 크기값 = 크기.작게, 색 = c.흐림)
            }
            if (성장 != null) 글(성장짧게(성장), 크기값 = 크기.작게, 색 = if (성장 >= 0) c.오름 else c.내림, 굵기 = FontWeight.Bold)
        }
    }
}

@Composable
private fun 세트줄(
    상태: 앱상태, S: 운동세션, j: Int, k: Int, 번호: String, 휴식보임: Boolean, 지금: Long,
    열림: Boolean, 켠칸: String?,
    on열기: () -> Unit, on칸: (String) -> Unit, on지금줄: (Rect) -> Unit,
    바꿈: ((운동세션) -> 운동세션) -> Unit,
) {
    val c = Local색.current
    val e = S.종목들[j]
    val rec = e.기록.칸(k)
    val 지금칸 = j == S.i && k == S.s
    val v = S.세트값(e, k)
    val 쉼 = e.세트휴식(k)
    val 폭 = 상태.d.설정.무게폭
    val 쉬는중 = S.휴식자리(j, k)
    val h = S.휴식
    Column(
        Modifier.fillMaxWidth().padding(vertical = 2.dp)
            .then(if (지금칸) Modifier.onGloballyPositioned { on지금줄(it.boundsInRoot()) } else Modifier)
            .clip(RoundedCornerShape(모서리.작게))
            .background(if (지금칸) c.강조옅음 else Color.Transparent)
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .alpha(if (rec == null && !지금칸 && !쉬는중) 0.55f else 1f),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // 체크
            Box(
                Modifier.size(높이.아주낮게).clip(CircleShape)
                    .background(if (rec != null) c.강조 else c.면2)
                    .border(1.dp, if (rec != null) c.강조 else c.선, CircleShape)
                    .눌림 { 바꿈 { it.체크(j, k, System.currentTimeMillis()) } },
                contentAlignment = Alignment.Center,
            ) { Icon(아이콘.체크, "$번호 세트 완료", Modifier.size(15.dp), tint = if (rec != null) c.강조글 else c.면) }
            // 몇 번째 세트
            Box(
                Modifier.size(width = 32.dp, height = 높이.아주낮게).clip(RoundedCornerShape(모서리.아주작게)).background(c.면)
                    .border(1.dp, c.선, RoundedCornerShape(모서리.아주작게)).눌림(on열기),
                contentAlignment = Alignment.Center,
            ) { 글(번호, 크기값 = 크기.버튼, 굵기 = FontWeight.Bold) }
            Row(Modifier.weight(1.6f).눌림(on열기), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Bottom) {
                글(무게글(v.w), 크기값 = 크기.크게, 색 = if (rec != null) c.글 else c.흐림)
                글("kg", 크기값 = 크기.작게, 색 = c.옅음)
            }
            Row(Modifier.weight(1f).눌림(on열기), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Bottom) {
                글("${v.r}", 크기값 = 크기.크게, 색 = if (rec != null) c.글 else c.흐림)
                글("회", 크기값 = 크기.작게, 색 = c.옅음)
            }
            // 휴식 칸 — 쉬는 동안 여기서 줄어든다. 초록 → 절반 이하 파랑 → 5초 이하 빨강 굵게 (09-21 메모)
            if (쉬는중 && h != null && !h.물음) {
                val 남은초 = max(0L, (h.끝시각 - 지금 + 999) / 1000).toInt()
                val 총 = if (h.총초 > 0) h.총초 else 쉼
                val 색 = when {
                    남은초 <= 5 -> c.나쁨
                    총 > 0 && 남은초 * 2 <= 총 -> c.내림
                    else -> c.좋음
                }
                Box(
                    Modifier.width(62.dp).height(높이.아주낮게).clip(RoundedCornerShape(모서리.아주작게)).background(색.copy(alpha = 0.14f))
                        .border(1.5.dp, 색, RoundedCornerShape(모서리.아주작게)),
                    contentAlignment = Alignment.Center,
                ) { 글(분초(남은초), 크기값 = 크기.크게, 색 = 색, 굵기 = if (남은초 <= 5) FontWeight.ExtraBold else FontWeight.Bold) }
                // 건너뛰기만 둔다 (±10초는 뺐다)
                Box(
                    Modifier.height(높이.아주낮게).clip(RoundedCornerShape(모서리.아주작게)).background(c.면2)
                        .border(1.dp, c.선, RoundedCornerShape(모서리.아주작게))
                        .눌림 { 바꿈 { it.다음으로(System.currentTimeMillis()) } }.padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center,
                ) { 글("건너뛰기", 크기값 = 크기.작게, 색 = c.흐림, 굵기 = FontWeight.Bold) }
            } else {
                Row(Modifier.width(62.dp).눌림(on열기), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.Bottom) {
                    if (휴식보임) {
                        글("휴식 ", 크기값 = 크기.아주작게, 색 = c.옅음)
                        글(분초(쉼), 크기값 = 크기.버튼, 색 = c.옅음)
                    }
                }
                아이콘버튼(아이콘.지우기, "$번호 세트 지우기", { 바꿈 { it.세트삭제(j, k) } }, 칠함 = false, 크기칸 = 높이.아주낮게)
                펼침단추(열림, on열기)
            }
        }
        // 휴식이 끝나고 '넘어갈까요?'를 묻는 중 (넘어가기 전 확인이 켜져 있을 때)
        if (쉬는중 && h != null && h.물음) {
            val 다음 = h.다음i?.let { S.종목들.getOrNull(it)?.이름 } ?: S.지금종목.이름
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                글("휴식 끝 · 다음 $다음", Modifier.weight(1f), 크기값 = 크기.버튼, 색 = c.휴식, 굵기 = FontWeight.Bold)
                버튼("30초 더", { 바꿈 { s -> s.copy(휴식 = s.휴식?.copy(끝시각 = System.currentTimeMillis() + 30_000, 총초 = 30, 물음 = false)) } }, 작게 = true)
                버튼("시작", { 바꿈 { it.다음으로(System.currentTimeMillis()) } }, 작게 = true, 주요 = true)
            }
        }
        if (열림) {
            val 칸들 = buildList {
                add(숫자칸("w", "무게", 무게글(v.w), "kg", 입력종류.소수,
                    { 바꿈 { it.값고치기(j, k, 새무게 = it.세트값(it.종목들[j], k).w - 폭) } },
                    { 바꿈 { it.값고치기(j, k, 새무게 = it.세트값(it.종목들[j], k).w + 폭) } },
                    { t -> t.replace(',', '.').toDoubleOrNull()?.let { w -> 바꿈 { it.값고치기(j, k, 새무게 = w) } } },
                    휠값.무게, { 무게글(it) }, v.w))
                add(숫자칸("r", "횟수", "${v.r}", "회", 입력종류.정수,
                    { 바꿈 { it.값고치기(j, k, 새횟수 = it.세트값(it.종목들[j], k).r - 1) } },
                    { 바꿈 { it.값고치기(j, k, 새횟수 = it.세트값(it.종목들[j], k).r + 1) } },
                    { t -> t.toIntOrNull()?.let { r -> 바꿈 { it.값고치기(j, k, 새횟수 = r) } } },
                    휠값.횟수, { "${it.toInt()}" }, v.r.toDouble()))
                if (휴식보임) add(숫자칸("t", "휴식", 분초(쉼), "", 입력종류.분초,
                    { 바꿈 { it.휴식고치기(j, k, it.종목들[j].세트휴식(k) - 5) } },
                    { 바꿈 { it.휴식고치기(j, k, it.종목들[j].세트휴식(k) + 5) } },
                    { t -> 초읽기(t)?.let { x -> 바꿈 { it.휴식고치기(j, k, x) } } },
                    휠값.휴식, { 분초(it.toInt()) }, 쉼.toDouble()))
            }
            숫자버튼줄(칸들, 켠칸, on칸, Modifier.padding(top = 8.dp, bottom = 4.dp))
        }
    }
}

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
private fun 마무리(상태: 앱상태, S: 운동세션) {
    val c = Local색.current
    val d = 상태.d
    val 기간 = d.지금기준().기간
    val 오늘 = 상태.오늘
    val 달성 = S.한세트수() >= S.목표세트()
    val 직전 = d.직전기록(S.루틴id, 오늘)?.second
    val 직전세트 = 직전?.let { 정식세트(it) }
    var 열린종목 by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(horizontal = 간격.넓게)) {
        // ── 제목 + 요약 (직전 같은 루틴 대비) ──
        Row(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            제목글("${S.루틴이름} ${if (달성) "달성" else "미달성"}", Modifier.weight(1f), 크기값 = 크기.제목, 색 = if (달성) c.글 else c.나쁨)
            Column(horizontalAlignment = Alignment.End) {
                val 세트 = S.한세트수()
                Text(buildAnnotatedString {
                    append("${세트}/${S.목표세트()}세트")
                    if (직전세트 != null) append(증감표((세트 - 직전세트.size).toDouble(), c.오름, c.내림) { "${it.toInt()}" })
                    append(" · ${시분초(S.흐른초(System.currentTimeMillis()))}")
                }, style = 글꼴.보통(크기.작게), color = c.흐림, maxLines = 1)
                val 볼 = S.오늘볼륨()
                Text(buildAnnotatedString {
                    append("볼륨 ${콤마(볼)}kg")
                    if (직전세트 != null) {
                        val p = 퍼센트(볼, 볼륨(직전세트))
                        if (p != null) append(증감표(p.toDouble(), c.오름, c.내림) { "${it.toInt()}%" })
                    }
                }, style = 글꼴.보통(크기.작게), color = c.흐림, maxLines = 1)
            }
        }
        // ── 종목 상자 — 이 안에서만 넘긴다 ──
        카드(Modifier.weight(1f), 안쪽 = 0.dp) {
            // 맨 위: 루틴 정보
            Column(Modifier.fillMaxWidth().background(c.면2).padding(horizontal = 16.dp, vertical = 12.dp)) {
                성장줄(루틴표시(S.루틴이름), d.루틴성장(S.루틴id, 오늘, S.유효세트()), 기간)
                글("${S.종목들.count { !it.임시 }}종목 · 달성도 ${S.루틴달성도()}%" + (직전?.let { " · 직전 ${직전세트?.size ?: 0}세트" } ?: ""),
                    크기값 = 크기.작게, 색 = c.옅음)
            }
            구분선()
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                S.종목들.filter { it.찬것().isNotEmpty() }.forEach { e ->
                    val 열림 = 열린종목 == e.이름
                    Row(Modifier.fillMaxWidth().눌림 { 열린종목 = if (열림) null else e.이름 }.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        성장줄(e.이름, d.종목성장(e.이름, 오늘, e.찬것(), S.묶음이름(e)), 기간, Modifier.weight(1f))
                        펼침단추(열림) { 열린종목 = if (열림) null else e.이름 }
                    }
                    if (열림) 종목그래프(d, e, 오늘)
                    구분선()
                }
            }
        }
        Box(Modifier.height(12.dp))
        버튼("기록 저장하고 끝내기", { 상태.바꿈 { it.운동저장(오늘, System.currentTimeMillis()) } }, Modifier.fillMaxWidth(), 주요 = true)
        Box(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            버튼("운동으로 돌아가기", { 상태.바꿈 { dd -> dd.copy(세션 = dd.세션?.재개(System.currentTimeMillis())) } }, Modifier.weight(1f), 작게 = true)
            버튼("기록 없이 끝내기", { 상태.지우고알림("기록 없이 끝냈습니다") { it.copy(세션 = null) } }, Modifier.weight(1f), 작게 = true, 글색 = c.나쁨)
        }
        Box(Modifier.height(12.dp))
    }
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
