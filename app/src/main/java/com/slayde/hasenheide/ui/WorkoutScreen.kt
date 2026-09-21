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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.slayde.hasenheide.data.다음빈칸
import com.slayde.hasenheide.data.다음으로
import com.slayde.hasenheide.data.다음종목으로
import com.slayde.hasenheide.data.덜한가
import com.slayde.hasenheide.data.달성도
import com.slayde.hasenheide.data.루틴달성도
import com.slayde.hasenheide.data.루틴성장
import com.slayde.hasenheide.data.마감풀기
import com.slayde.hasenheide.data.목표볼륨
import com.slayde.hasenheide.data.목표세트
import com.slayde.hasenheide.data.무게글
import com.slayde.hasenheide.data.묶음이름
import com.slayde.hasenheide.data.분초
import com.slayde.hasenheide.data.불러오기
import com.slayde.hasenheide.data.세트값
import com.slayde.hasenheide.data.세트삭제
import com.slayde.hasenheide.data.세트추가
import com.slayde.hasenheide.data.세트휴식
import com.slayde.hasenheide.data.시분초
import com.slayde.hasenheide.data.오늘볼륨
import com.slayde.hasenheide.data.운동세션
import com.slayde.hasenheide.data.운동저장
import com.slayde.hasenheide.data.유효세트
import com.slayde.hasenheide.data.일RM
import com.slayde.hasenheide.data.종목성장
import com.slayde.hasenheide.data.종목으로
import com.slayde.hasenheide.data.지금기준
import com.slayde.hasenheide.data.지금종목
import com.slayde.hasenheide.data.찬것
import com.slayde.hasenheide.data.체크
import com.slayde.hasenheide.data.초읽기
import com.slayde.hasenheide.data.총칸
import com.slayde.hasenheide.data.콤마
import com.slayde.hasenheide.data.한세트수
import com.slayde.hasenheide.data.휴식고치기
import com.slayde.hasenheide.data.휴식끝
import com.slayde.hasenheide.data.휴식조절
import com.slayde.hasenheide.data.값고치기
import com.slayde.hasenheide.data.칸
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.높이
import com.slayde.hasenheide.ui.theme.모서리
import com.slayde.hasenheide.ui.theme.크기
import kotlinx.coroutines.delay
import kotlin.math.max

/**
 * 운동 실행 화면 (기능명세 5).
 * 휴식은 방금 끝낸 세트 줄 위에서 돈다. 끝나는 '시각'을 기억하므로 화면이 꺼졌다 켜져도 어긋나지 않는다 (5-4).
 */
@Composable
fun 운동화면(상태: 앱상태, 폰: 폰기능) {
    val c = Local색.current
    val d = 상태.d
    val S = d.세션 ?: return
    var 지금 by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var 열린세트 by remember { mutableStateOf(-1) }
    var 켠칸 by remember { mutableStateOf<String?>(null) }
    var 열린시트 by remember { mutableStateOf<String?>(null) }   // 목록 · 추가 · 마칠까

    fun 바꿈(f: (운동세션) -> 운동세션) = 상태.바꿈 { dd -> dd.세션?.let { dd.copy(세션 = f(it)) } ?: dd }

    // 시계 — 0.25초마다. 휴식이 끝나면 알리고 다음으로
    LaunchedEffect(Unit) {
        while (true) {
            지금 = System.currentTimeMillis()
            val s = 상태.d.세션
            val h = s?.휴식
            if (s != null && h != null && !h.물음 && 지금 >= h.끝시각) {
                if (상태.d.설정.소리진동) 폰.알림()
                상태.바꿈 { dd -> dd.세션?.let { dd.copy(세션 = it.휴식끝(dd.설정)) } ?: dd }
            }
            delay(250)
        }
    }
    // 운동 중에는 화면을 켜 둔다 (설정에서 끌 수 있다)
    val 뷰 = LocalView.current
    DisposableEffect(d.설정.화면유지) {
        뷰.keepScreenOn = d.설정.화면유지
        onDispose { 뷰.keepScreenOn = false }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            머리줄(S, 지금) { 바꿈 { it.copy(휴식 = null, 끝화면 = true) } }
            if (S.끝화면) 마무리(상태, S, 지금, 폰) else {
            val ex = S.지금종목
            종목머리(상태, S)
            구분선()
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 간격.넓게)) {
                Box(Modifier.height(8.dp))
                for (k in 0 until ex.총칸()) {
                    val h = S.휴식
                    if (h != null && h.k == k) 휴식띠(S, 지금, ::바꿈)
                    else 세트줄(상태, S, k, 열린세트 == k, 켠칸,
                        on체크 = { 바꿈 { it.체크(k, System.currentTimeMillis()) }; 열린세트 = -1 },
                        on열기 = { 열린세트 = if (열린세트 == k) -1 else k; 켠칸 = null },
                        on칸 = { f -> 열린세트 = k; 켠칸 = if (켠칸 == f) null else f },
                        on지우기 = { 바꿈 { it.세트삭제(k) }; if (열린세트 == k) 열린세트 = -1 },
                        바꿈 = ::바꿈)
                }
                // ＋ — 맨 아래 세트를 베낀다
                Box(Modifier.fillMaxWidth().padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier.size(높이.보통).clip(CircleShape).background(c.면).border(1.dp, c.선, CircleShape).눌림 { 바꿈 { it.세트추가() } },
                        contentAlignment = Alignment.Center,
                    ) { Icon(아이콘.더하기, "세트 추가", Modifier.size(20.dp), tint = c.강조) }
                }
                if (ex.마감) 버튼("남은 운동 마저 하기", { 바꿈 { it.마감풀기() } }, Modifier.fillMaxWidth(), 작게 = true)
                // 남은 운동
                val 남은 = S.종목들.withIndex().filter { (j, e) -> j != S.i && !e.마감 && e.덜한가() }
                if (남은.isNotEmpty()) {
                    구분선(Modifier.padding(top = 10.dp))
                    이름표("남은 운동", Modifier.padding(top = 10.dp, bottom = 2.dp))
                    남은.forEach { (j, e) ->
                        Row(Modifier.fillMaxWidth().눌림 { 바꿈 { it.종목으로(j) }; 열린세트 = -1 }.padding(vertical = 10.dp)) {
                            글(e.이름, Modifier.weight(1f))
                            글("${e.총칸() - e.찬것().size}세트", 크기값 = 크기.작게, 색 = c.옅음)
                        }
                        구분선()
                    }
                }
                Box(Modifier.height(16.dp))
            }
            // 아랫줄 — 운동 목록 · 운동 추가 · 다음
            Row(Modifier.fillMaxWidth().background(c.면).padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                버튼("운동 목록", { 열린시트 = "목록" }, Modifier.weight(1f), 작게 = true, 그림 = 아이콘.목록)
                버튼("운동 추가", { 열린시트 = "추가" }, Modifier.weight(1f), 작게 = true, 그림 = 아이콘.더하기)
                버튼("다음", {
                    if (S.지금종목.덜한가()) 열린시트 = "마칠까" else { 바꿈 { it.다음종목으로(false) }; 열린세트 = -1 }
                }, Modifier.weight(1f), 작게 = true, 그림 = 아이콘.오른쪽)
            }
            }
        }
        when (열린시트) {
            "목록" -> 시트("오늘 운동 목록", { 열린시트 = null }) {
                글("누르면 이동 · 하던 자리는 그대로", 크기값 = 크기.조금작게, 색 = c.옅음)
                S.종목들.forEachIndexed { j, e ->
                    고르기줄(e.이름, "${e.찬것().size}/${e.총칸()}세트" + (if (e.마감) " · 마침" else "") + (if (e.임시) " · 오늘만" else ""),
                        if (j == S.i) 아이콘.체크 else 아이콘.오른쪽, 흐림 = e.마감) {
                        바꿈 { it.종목으로(j) }; 열린세트 = -1; 열린시트 = null
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
                            바꿈 { it.불러오기(e.이름, d.설정.기본휴식).종목으로(it.i + 1) }; 열린시트 = null
                        }
                    }
                }
            }
            "마칠까" -> 시트("운동이 완료되지 않았습니다", { 열린시트 = null }) {
                val e = S.지금종목
                글("${e.이름} ${e.총칸() - e.찬것().size}세트 남음 · 여기서 마칠까요?", 크기값 = 크기.버튼, 색 = c.흐림)
                Box(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    버튼("여기까지", { 바꿈 { it.다음종목으로(true) }; 열린시트 = null; 열린세트 = -1 }, Modifier.weight(1f), 주요 = true)
                    버튼("나중에 더", { 바꿈 { it.다음종목으로(false) }; 열린시트 = null; 열린세트 = -1 }, Modifier.weight(1f))
                }
                글("나중에 더 → 남은 운동 목록에 남습니다", Modifier.padding(top = 8.dp), 크기값 = 크기.작게, 색 = c.옅음)
            }
        }
    }
}

@Composable
private fun 머리줄(S: 운동세션, 지금: Long, 끝내기: () -> Unit) {
    val c = Local색.current
    val 달 = S.루틴달성도()
    Column(Modifier.fillMaxWidth().background(c.면)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            제목글(S.루틴이름, 크기값 = 크기.버튼)
            글("${S.i + 1}/${S.종목들.size}", 크기값 = 크기.아주작게, 색 = c.흐림)
            글("달성도 ${달}%", 크기값 = 크기.아주작게, 색 = c.강조, 굵기 = FontWeight.Bold)
            글(시분초((지금 - S.시작시각) / 1000), 크기값 = 크기.아주작게, 색 = c.흐림)
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

@Composable
private fun 종목머리(상태: 앱상태, S: 운동세션) {
    val c = Local색.current
    val e = S.지금종목
    val 지금세트 = e.찬것()
    val 과거rm = 상태.d.기록.values.flatMap { r -> r.종목들.filter { it.이름 == e.이름 }.flatMap { it.세트들 } }.maxOfOrNull { 일RM(it.w, it.r) } ?: 0.0
    val rm = max(과거rm, 지금세트.maxOfOrNull { 일RM(it.w, it.r) } ?: 0.0)
    val 목표볼 = e.무게 * e.횟수 * (if (e.계획세트 > 0) e.계획세트 else e.세트)
    val 뱃지 = listOfNotNull(
        if (e.임시) "오늘만" else null, if (e.마감) "마침" else null,
        e.슈퍼?.let { g -> val 식구 = S.종목들.filter { it.슈퍼 == g }; "슈퍼 ${식구.indexOf(e) + 1}/${식구.size}" },
    )
    Row(Modifier.fillMaxWidth().padding(horizontal = 간격.넓게, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            글("%02d)".format(S.i + 1), 크기값 = 크기.조금작게, 색 = c.흐림, 굵기 = FontWeight.Bold)
            Box(Modifier.width(4.dp))
            제목글(e.이름, Modifier.weight(1f, fill = false), 크기값 = 크기.본문)
            뱃지.forEach { b -> Box(Modifier.width(4.dp)); 알약(b, c.휴식) }
        }
        글("달성도 ${e.달성도()}%", 크기값 = 크기.작게, 색 = c.강조, 굵기 = FontWeight.Bold)
        글("1RM ${if (rm > 0) "%.1f".format(rm) else "—"}", 크기값 = 크기.작게, 색 = c.흐림)
        글("${콤마(com.slayde.hasenheide.data.볼륨(지금세트))}/${콤마(목표볼)}", 크기값 = 크기.작게, 색 = c.흐림)
    }
}

@Composable
private fun 세트줄(
    상태: 앱상태, S: 운동세션, k: Int, 열림: Boolean, 켠칸: String?,
    on체크: () -> Unit, on열기: () -> Unit, on칸: (String) -> Unit, on지우기: () -> Unit,
    바꿈: ((운동세션) -> 운동세션) -> Unit,
) {
    val c = Local색.current
    val e = S.지금종목
    val rec = e.기록.칸(k)
    val 지금칸 = k == S.s
    val v = S.세트값(e, k)
    val 쉼 = e.세트휴식(k)
    val 폭 = 상태.d.설정.무게폭
    Column(
        Modifier.fillMaxWidth().padding(vertical = 2.dp)
            .clip(RoundedCornerShape(모서리.작게))
            .background(if (지금칸) c.강조옅음 else androidx.compose.ui.graphics.Color.Transparent)
            .padding(horizontal = 6.dp, vertical = 3.dp)
            .alpha(if (rec == null && !지금칸) 0.55f else 1f),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // 체크
            Box(
                Modifier.size(높이.아주낮게).clip(CircleShape)
                    .background(if (rec != null) c.강조 else c.면2)
                    .border(1.dp, if (rec != null) c.강조 else c.선, CircleShape)
                    .눌림(on체크),
                contentAlignment = Alignment.Center,
            ) { Icon(아이콘.체크, "${k + 1}세트 완료", Modifier.size(15.dp), tint = if (rec != null) c.강조글 else c.면) }
            // 몇 번째 세트
            Box(
                Modifier.size(width = 30.dp, height = 높이.아주낮게).clip(RoundedCornerShape(모서리.아주작게)).background(c.면)
                    .border(1.dp, c.선, RoundedCornerShape(모서리.아주작게)).눌림(on열기),
                contentAlignment = Alignment.Center,
            ) { 글("${k + 1}", 크기값 = 크기.버튼, 굵기 = FontWeight.Bold) }
            Row(Modifier.weight(1.6f).눌림(on열기), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Bottom) {
                글(무게글(v.w), 크기값 = 크기.크게, 색 = if (rec != null) c.글 else c.흐림)
                글("kg", 크기값 = 크기.작게, 색 = c.옅음)
            }
            Row(Modifier.weight(1f).눌림(on열기), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Bottom) {
                글("${v.r}", 크기값 = 크기.크게, 색 = if (rec != null) c.글 else c.흐림)
                글("회", 크기값 = 크기.작게, 색 = c.옅음)
            }
            Row(Modifier.width(62.dp).눌림(on열기), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.Bottom) {
                글("휴식 ", 크기값 = 크기.아주작게, 색 = c.옅음)
                글(분초(쉼), 크기값 = 크기.버튼, 색 = c.옅음)
            }
            아이콘버튼(아이콘.지우기, "${k + 1}세트 지우기", on지우기, 칠함 = false, 크기칸 = 높이.아주낮게)
            펼침단추(열림, on열기)
        }
        if (열림) {
            숫자버튼줄(
                listOf(
                    숫자칸("w", "무게", 무게글(v.w), "kg", true, { 바꿈 { it.값고치기(k, 새무게 = it.세트값(it.지금종목, k).w - 폭) } },
                        { 바꿈 { it.값고치기(k, 새무게 = it.세트값(it.지금종목, k).w + 폭) } },
                        { t -> t.replace(',', '.').toDoubleOrNull()?.let { w -> 바꿈 { it.값고치기(k, 새무게 = w) } } }),
                    숫자칸("r", "횟수", "${v.r}", "회", false, { 바꿈 { it.값고치기(k, 새횟수 = it.세트값(it.지금종목, k).r - 1) } },
                        { 바꿈 { it.값고치기(k, 새횟수 = it.세트값(it.지금종목, k).r + 1) } },
                        { t -> t.toIntOrNull()?.let { r -> 바꿈 { it.값고치기(k, 새횟수 = r) } } }),
                    숫자칸("t", "휴식", 분초(쉼), "", false, { 바꿈 { it.휴식고치기(k, it.지금종목.세트휴식(k) - 5) } },
                        { 바꿈 { it.휴식고치기(k, it.지금종목.세트휴식(k) + 5) } },
                        { t -> 초읽기(t)?.let { x -> 바꿈 { it.휴식고치기(k, x) } } }),
                ),
                켠칸, on칸, Modifier.padding(top = 6.dp, bottom = 4.dp),
            )
        }
    }
}

/** 휴식 띠 — 방금 끝낸 세트 줄 하나만 덮는다 (5-3-1) */
@Composable
private fun 휴식띠(S: 운동세션, 지금: Long, 바꿈: ((운동세션) -> 운동세션) -> Unit) {
    val c = Local색.current
    val h = S.휴식 ?: return
    val 남은초 = max(0L, (h.끝시각 - 지금 + 999) / 1000).toInt()
    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp)
            .clip(RoundedCornerShape(모서리.작게))
            .background(c.휴식옅음)
            .border(1.dp, c.휴식.copy(alpha = 0.4f), RoundedCornerShape(모서리.작게))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (!h.물음) {
            글("휴식", 크기값 = 크기.작게, 색 = c.휴식, 굵기 = FontWeight.Bold)
            제목글(분초(남은초), Modifier.weight(1f), 크기값 = 크기.큰숫자, 색 = c.휴식)
            띠단추("−10초") { 바꿈 { it.휴식조절(-10, System.currentTimeMillis()) } }
            띠단추("+10초") { 바꿈 { it.휴식조절(10, System.currentTimeMillis()) } }
            띠단추("건너뛰기", 진하게 = true) { 바꿈 { it.다음으로() } }
        } else {
            val 다음 = S.휴식?.다음i?.let { S.종목들.getOrNull(it)?.이름 } ?: S.지금종목.이름
            글("휴식 끝 · 다음 $다음", Modifier.weight(1f), 크기값 = 크기.버튼, 색 = c.휴식, 굵기 = FontWeight.Bold)
            띠단추("30초 더") { 바꿈 { s -> s.copy(휴식 = s.휴식?.copy(끝시각 = System.currentTimeMillis() + 30_000, 물음 = false)) } }
            띠단추("시작", 진하게 = true) { 바꿈 { it.다음으로() } }
        }
    }
}

@Composable
private fun 띠단추(글자: String, 진하게: Boolean = false, onClick: () -> Unit) {
    val c = Local색.current
    Box(
        Modifier.height(높이.낮게).clip(RoundedCornerShape(모서리.아주작게))
            .background(if (진하게) c.휴식 else c.면)
            .border(1.dp, c.휴식.copy(alpha = 0.4f), RoundedCornerShape(모서리.아주작게))
            .눌림(onClick).padding(horizontal = 9.dp),
        contentAlignment = Alignment.Center,
    ) { 글(글자, 크기값 = 크기.조금작게, 색 = if (진하게) c.면 else c.휴식, 굵기 = FontWeight.Bold) }
}

/** 마무리 — 오늘 운동 달성 (5 · 6-2) */
@Composable
private fun 마무리(상태: 앱상태, S: 운동세션, 지금: Long, 폰: 폰기능) {
    val c = Local색.current
    val d = 상태.d
    val 기간 = d.지금기준().기간
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 간격.넓게)) {
        Box(Modifier.height(24.dp))
        글(S.루틴이름, Modifier.fillMaxWidth(), 크기값 = 크기.조금작게, 색 = c.옅음, 가운데 = true)
        제목글(if (S.한세트수() >= S.목표세트()) "오늘 운동 달성" else "오늘 운동 끝", Modifier.fillMaxWidth().padding(top = 4.dp), 크기값 = 크기.큰숫자)
        글("${S.한세트수()} / ${S.목표세트()} 세트 · ${시분초((지금 - S.시작시각) / 1000)} · 볼륨 ${콤마(S.오늘볼륨())}kg",
            Modifier.fillMaxWidth().padding(top = 6.dp), 크기값 = 크기.조금작게, 색 = c.흐림, 가운데 = true)
        Box(Modifier.height(18.dp))
        카드 {
            성장줄(S.루틴이름, d.루틴성장(S.루틴id, 상태.오늘, S.유효세트()), 기간, Modifier.padding(vertical = 6.dp))
            S.종목들.filter { it.찬것().isNotEmpty() }.forEach { e ->
                구분선()
                성장줄(e.이름, d.종목성장(e.이름, 상태.오늘, e.찬것(), S.묶음이름(e)), 기간, Modifier.padding(vertical = 8.dp))
            }
        }
        Box(Modifier.height(18.dp))
        버튼("기록 저장하고 끝내기", { 상태.바꿈 { it.운동저장(상태.오늘, System.currentTimeMillis()) } }, Modifier.fillMaxWidth(), 주요 = true)
        Box(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            버튼("운동으로 돌아가기", { 상태.바꿈 { dd -> dd.copy(세션 = dd.세션?.copy(끝화면 = false)) } }, Modifier.weight(1f), 작게 = true)
            버튼("기록 없이 끝내기", { 상태.지우고알림("기록 없이 끝냈습니다") { it.copy(세션 = null) } }, Modifier.weight(1f), 작게 = true, 글색 = c.나쁨)
        }
        Box(Modifier.height(40.dp))
    }
}
