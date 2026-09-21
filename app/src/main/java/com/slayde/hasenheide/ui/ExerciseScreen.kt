package com.slayde.hasenheide.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import com.slayde.hasenheide.data.카테고리지우기
import com.slayde.hasenheide.data.이름추천
import androidx.compose.foundation.border
import com.slayde.hasenheide.ui.theme.높이
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.slayde.hasenheide.data.앱데이터
import com.slayde.hasenheide.data.종목
import com.slayde.hasenheide.data.종목지표
import com.slayde.hasenheide.data.무게글
import com.slayde.hasenheide.data.콤마
import com.slayde.hasenheide.data.퍼센트
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.모서리
import com.slayde.hasenheide.ui.theme.크기

/** 종목 이름을 바꾸면 루틴과 지난 기록의 이름도 함께 바꾼다 (기록이 끊기지 않게) */
fun 앱데이터.종목이름바꿈(옛: String, 새: String): 앱데이터 {
    if (옛 == 새 || 새.isBlank() || 종목표.any { it.이름 == 새 }) return this
    return copy(
        종목표 = 종목표.map { if (it.이름 == 옛) it.copy(이름 = 새) else it },
        루틴들 = 루틴들.map { r -> r.copy(종목 = r.종목.map { if (it.이름 == 옛) it.copy(이름 = 새) else it }) },
        기록 = 기록.mapValues { (_, rec) -> rec.copy(종목들 = rec.종목들.map { if (it.이름 == 옛) it.copy(이름 = 새, 묶음 = it.묶음?.split("+")?.map { x -> if (x == 옛) 새 else x }?.sorted()?.joinToString("+")) else it }) },
        세션 = 세션?.let { S -> S.copy(종목들 = S.종목들.map { if (it.이름 == 옛) it.copy(이름 = 새) else it }) },
    )
}

@Composable
fun 종목화면(상태: 앱상태) {
    val c = Local색.current
    val d = 상태.d
    var 부위 by remember { mutableStateOf("전체") }
    var 열린 by remember { mutableStateOf<String?>(null) }
    var 카테고리시트 by remember { mutableStateOf(false) }
    var 추가중 by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 간격.넓게)) {
            제목글("종목", Modifier.padding(top = 14.dp, bottom = 10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                칩줄(listOf("전체") + d.카테고리, 부위, { 부위 = it }, Modifier.weight(1f))
                Box(Modifier.width(6.dp))
                아이콘버튼(아이콘.설정, "카테고리 관리", { 카테고리시트 = true })
            }
            Box(Modifier.height(10.dp))
            val 목록 = d.종목표.filter { 부위 == "전체" || it.부위 == 부위 }
            카드(안쪽 = 0.dp) {
                if (목록.isEmpty()) 글(if (d.종목표.isEmpty()) "아직 종목이 없습니다 · 아래에서 만들어 주세요" else "이 부위에 종목이 없습니다",
                    Modifier.padding(14.dp), 크기값 = 크기.조금작게, 색 = c.옅음)
                목록.forEachIndexed { i, e ->
                    if (i > 0) 구분선()
                    종목한줄(상태, e, 열린 == e.이름) { 열린 = if (열린 == e.이름) null else e.이름 }
                }
            }
            Box(Modifier.height(10.dp))
            if (추가중) 새종목칸(상태, if (부위 == "전체") d.카테고리.firstOrNull() ?: "" else 부위) { 추가중 = false }
            else 버튼("종목 추가", { 추가중 = true }, Modifier.fillMaxWidth(), 그림 = 아이콘.더하기)
            Box(Modifier.height(16.dp))   // 끝에 빈 공간을 두지 않는다 (09-21 메모)
        }
        if (카테고리시트) 카테고리관리(상태) { 카테고리시트 = false }
    }
}

@Composable
private fun 종목한줄(상태: 앱상태, e: 종목, 열림: Boolean, on열기: () -> Unit) {
    val c = Local색.current
    val d = 상태.d
    val 지표 = d.종목지표(e.이름, 상태.오늘)
    val 볼pct = if (지표 != null && 지표.과거 != null) 퍼센트(지표.지금.볼륨, 지표.과거.볼륨) else null
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().눌림(on열기).padding(start = 14.dp, end = 6.dp, top = 11.dp, bottom = 11.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
                글(e.이름, Modifier.weight(1f, fill = false), 굵기 = FontWeight.Medium)
                Box(Modifier.width(6.dp))
                글(listOf(e.부위, e.장비).filter { it.isNotBlank() }.joinToString("·"), 크기값 = 크기.작게, 색 = c.옅음)
            }
            if (볼pct != null) 글(
                (if (볼pct > 0) "+" else "") + "$볼pct%", 크기값 = 크기.버튼, 굵기 = FontWeight.Bold,
                색 = if (볼pct > 0) c.오름 else if (볼pct < 0) c.내림 else c.흐림,
            )
            펼침단추(열림, on열기)
        }
        if (열림) Column(Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp)) {
            if (지표 == null) 글("아직 기록이 없습니다", 크기값 = 크기.조금작게, 색 = c.옅음)
            else {
                val 과 = 지표.과거
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(모서리.작게)).background(c.면2).padding(10.dp)) {
                    지표줄("지표", "지금", "이전", "늘어난 값", 머리 = true)
                    지표줄("최대 1RM", "%.1f".format(지표.지금.rm), 과?.let { "%.1f".format(it.rm) } ?: "—", 증감(지표.지금.rm, 과?.rm))
                    지표줄("단일세트 최고", "${무게글(지표.지금.최고.w)}×${지표.지금.최고.r}", 과?.let { "${무게글(it.최고.w)}×${it.최고.r}" } ?: "—",
                        증감(지표.지금.최고.w * 지표.지금.최고.r, 과?.let { it.최고.w * it.최고.r }))
                    지표줄("한 세션 볼륨", 콤마(지표.지금.볼륨), 과?.let { 콤마(it.볼륨) } ?: "—", 증감(지표.지금.볼륨, 과?.볼륨))
                }
                글(if (과 != null) "${과.날}과 견준 값" else "견줄 이전 기록이 없습니다", Modifier.padding(top = 6.dp), 크기값 = 크기.작게, 색 = c.옅음)
            }
            Box(Modifier.height(12.dp))
            var 이름 by remember(e.이름) { mutableStateOf(e.이름) }
            var 달력 by remember(e.이름) { mutableStateOf(e.달력이름 ?: "") }
            var 장비 by remember(e.이름) { mutableStateOf(e.장비) }
            이름표("종목 이름")
            입력칸(이름, { 이름 = it }, Modifier.fillMaxWidth().padding(top = 4.dp), onDone = {
                상태.바꿈 { it.종목이름바꿈(e.이름, 이름.trim()) }
            })
            Box(Modifier.height(10.dp))
            이름표("달력 표시 이름")
            입력칸(달력, { 달력 = it }, Modifier.fillMaxWidth().padding(top = 4.dp), 안내 = "비워두면 종목 이름을 따라갑니다", onDone = {
                상태.바꿈 { d -> d.copy(종목표 = d.종목표.map { if (it.이름 == e.이름) it.copy(달력이름 = 달력.trim().ifEmpty { null }) else it }) }
            })
            글("달력 칸에만 쓰는 짧은 이름", Modifier.padding(top = 2.dp), 크기값 = 크기.작게, 색 = c.옅음)
            Box(Modifier.height(10.dp))
            이름표("부위")
            칩줄(d.카테고리, e.부위, { p -> 상태.바꿈 { d -> d.copy(종목표 = d.종목표.map { if (it.이름 == e.이름) it.copy(부위 = p) else it }) } }, Modifier.padding(top = 4.dp))
            Box(Modifier.height(10.dp))
            이름표("장비")
            Box(Modifier.height(4.dp))
            장비고르기(상태, e.장비, { v -> 장비 = v; 상태.바꿈 { d -> d.copy(종목표 = d.종목표.map { if (it.이름 == e.이름) it.copy(장비 = v.trim()) else it }) } })
            글("이름은 고친 뒤 자판의 '완료'를 누르면 저장됩니다", Modifier.padding(top = 6.dp), 크기값 = 크기.작게, 색 = c.옅음)
            Box(Modifier.height(12.dp))
            버튼("이 종목 지우기", {
                val 쓰는곳 = d.루틴들.filter { r -> r.종목.any { it.이름 == e.이름 } }.map { it.이름 }
                상태.지우고알림(if (쓰는곳.isEmpty()) "${e.이름}을(를) 지웠습니다" else "${e.이름} · ${쓰는곳.joinToString("·")}에서도 뺐습니다") { dd ->
                    dd.copy(
                        종목표 = dd.종목표.filter { it.이름 != e.이름 },
                        루틴들 = dd.루틴들.map { r -> r.copy(종목 = r.종목.filter { it.이름 != e.이름 }) },
                    )
                }
            }, Modifier.fillMaxWidth(), 작게 = true, 글색 = c.나쁨)
        }
    }
}

private data class 증감글(val pct: Int?, val 차: Double)
private fun 증감(지금: Double, 과거: Double?): 증감글? = 과거?.let { 증감글(퍼센트(지금, it), 지금 - it) }

@Composable
private fun 지표줄(이름: String, 지금: String, 이전: String, 증: Any?, 머리: Boolean = false) {
    val c = Local색.current
    val 작은 = if (머리) 크기.아주작게 else 크기.버튼
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        글(이름, Modifier.weight(1.3f), 크기값 = if (머리) 크기.아주작게 else 크기.조금작게, 색 = if (머리) c.옅음 else c.흐림, 굵기 = FontWeight.Bold)
        글(지금, Modifier.weight(1f), 크기값 = 작은, 색 = if (머리) c.옅음 else c.글, 굵기 = FontWeight.Bold)
        글(이전, Modifier.weight(1f), 크기값 = if (머리) 크기.아주작게 else 크기.조금작게, 색 = c.옅음)
        when (증) {
            is String -> 글(증, Modifier.weight(0.9f), 크기값 = 크기.아주작게, 색 = c.옅음)
            is 증감글 -> {
                val p = 증.pct
                val 색 = if ((p ?: 0) > 0) c.오름 else if ((p ?: 0) < 0) c.내림 else c.흐림
                글(if (p == null) "—" else (if (p > 0) "+" else "") + "$p%", Modifier.weight(0.9f), 크기값 = 크기.버튼, 색 = 색, 굵기 = FontWeight.Bold)
            }
            else -> 글("—", Modifier.weight(0.9f), 크기값 = 크기.버튼, 색 = c.옅음)
        }
    }
}

/** 새 종목 — 이름 · 부위 · 장비 */
@Composable
fun 새종목칸(상태: 앱상태, 처음부위: String, 닫기: () -> Unit) {
    val d = 상태.d
    var 이름 by remember { mutableStateOf("") }
    var 부위 by remember { mutableStateOf(처음부위) }
    var 장비 by remember { mutableStateOf("") }
    카드 {
        이름표("새 종목")
        Box(Modifier.height(6.dp))
        종목이름칸(상태, 이름, { 이름 = it }, 있는것도 = false) { n, 부, 장 ->
            이름 = n
            if (부 in d.카테고리) 부위 = 부
            if (장.isNotBlank()) 장비 = 장
        }
        Box(Modifier.height(8.dp))
        부위고르기(상태, 부위, { 부위 = it })
        Box(Modifier.height(8.dp))
        장비고르기(상태, 장비, { 장비 = it })
        Box(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            버튼("취소", 닫기, Modifier.weight(1f), 작게 = true)
            버튼("만들기", {
                val n = 이름.trim()
                if (n.isNotEmpty() && d.종목표.none { it.이름 == n }) {
                    상태.바꿈 { it.copy(종목표 = it.종목표 + 종목(n, 부위, 장비.trim())) }
                    닫기()
                }
            }, Modifier.weight(1f), 작게 = true, 주요 = true)
        }
        if (이름.trim().isNotEmpty() && d.종목표.any { it.이름 == 이름.trim() })
            글("같은 이름의 종목이 이미 있습니다", Modifier.padding(top = 6.dp), 크기값 = 크기.작게, 색 = Local색.current.나쁨)
    }
}

@Composable
private fun 카테고리관리(상태: 앱상태, 닫기: () -> Unit) {
    val c = Local색.current
    var 새 by remember { mutableStateOf("") }
    시트("카테고리", 닫기) {
        글("루틴의 종목 고르기에도 그대로 나옵니다", 크기값 = 크기.조금작게, 색 = c.옅음)
        상태.d.카테고리.forEach { p ->
            val 수 = 상태.d.종목표.count { it.부위 == p }
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                글(p, Modifier.weight(1f))
                글(if (수 > 0) "종목 ${수}개" else "", 크기값 = 크기.작게, 색 = c.옅음)
                아이콘버튼(아이콘.지우기, "카테고리 지우기", {
                    상태.지우고알림(if (수 > 0) "$p · 종목 ${수}개와 함께 지웠습니다" else "$p 카테고리를 지웠습니다") { it.카테고리지우기(p) }
                }, 칠함 = false)
            }
            구분선()
        }
        Box(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            입력칸(새, { 새 = it }, Modifier.weight(1f), 안내 = "새 카테고리 (예: 전완)")
            Box(Modifier.width(6.dp))
            버튼("추가", {
                val n = 새.trim()
                if (n.isNotEmpty() && n !in 상태.d.카테고리) 상태.바꿈 { it.copy(카테고리 = it.카테고리 + n) }
                새 = ""
            }, 주요 = true)
        }
    }
}

/**
 * 부위 고르기 — 새 종목을 만들 때. 칩 줄 끝의 ＋ 로 부위를 더하고, − 를 누르면 칩마다 ✕ 가 붙어 지울 수 있다.
 * (09-21 메모: 고정된 부위 외에는 만들 길이 안 보였다. 종목 탭 ⚙ 와 같은 목록을 쓴다)
 */
@Composable
fun 부위고르기(상태: 앱상태, 선택: String, on선택: (String) -> Unit) {
    val c = Local색.current
    var 더하는중 by remember { mutableStateOf(false) }
    var 지우는중 by remember { mutableStateOf(false) }
    var 새 by remember { mutableStateOf("") }
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        상태.d.카테고리.forEach { p ->
            val 켬 = p == 선택 && !지우는중
            Row(
                Modifier.height(높이.낮게).clip(CircleShape)
                    .background(if (지우는중) c.나쁨.copy(alpha = 0.10f) else if (켬) c.강조 else c.면2)
                    .눌림 {
                        if (지우는중) {
                            val 수 = 상태.d.종목표.count { it.부위 == p }
                            상태.지우고알림(if (수 > 0) "$p · 종목 ${수}개와 함께 지웠습니다" else "$p 부위를 지웠습니다") { it.카테고리지우기(p) }
                            if (p == 선택) on선택(상태.d.카테고리.firstOrNull() ?: "")
                        } else on선택(p)
                    }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                글(p, 크기값 = 크기.버튼, 색 = if (지우는중) c.나쁨 else if (켬) c.강조글 else c.흐림, 굵기 = FontWeight.Medium)
                if (지우는중) { Box(Modifier.width(4.dp)); Icon(아이콘.닫기, "지우기", Modifier.size(13.dp), tint = c.나쁨) }
            }
        }
        아이콘버튼(아이콘.더하기, "부위 더하기", { 더하는중 = !더하는중; 지우는중 = false }, 켬 = 더하는중)
        아이콘버튼(if (지우는중) 아이콘.체크 else 아이콘.빼기, if (지우는중) "지우기 끝" else "부위 지우기", { 지우는중 = !지우는중; 더하는중 = false }, 켬 = 지우는중)
    }
    if (더하는중) {
        val 넣기 = {
            val n = 새.trim()
            if (n.isNotEmpty() && n !in 상태.d.카테고리) 상태.바꿈 { it.copy(카테고리 = it.카테고리 + n) }
            if (n.isNotEmpty()) on선택(n)
            새 = ""; 더하는중 = false
        }
        Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            입력칸(새, { 새 = it }, Modifier.weight(1f), 안내 = "새 부위 (예: 전완)", onDone = 넣기)
            Box(Modifier.width(6.dp))
            버튼("추가", 넣기, 작게 = true, 주요 = true)
        }
    }
}

/**
 * 종목 이름 입력 + 추천 (09-21 메모).
 * 자음·모음 단위로 좁혀 가며, 띄어쓰기는 무시한다. 추천은 기본 종목 목록 + 내가 만든 종목.
 * 추천을 누르면 이름과 함께 부위 · 장비도 넘겨준다.
 * 있는것도=false 면 이미 만든 종목은 추천하지 않는다 (종목 탭에서 새로 만들 때 — 같은 이름은 못 만드니까)
 */
@Composable
fun 종목이름칸(상태: 앱상태, 이름: String, on이름: (String) -> Unit, 있는것도: Boolean = true, on고름: (String, String, String) -> Unit) {
    val c = Local색.current
    val 표 = 상태.d.종목표
    val 정보 = linkedMapOf<String, Pair<String, String>>()
    if (있는것도) 표.forEach { 정보[it.이름] = it.부위 to it.장비 }
    이름추천.기본.forEach { b -> if (b.이름 !in 정보 && 표.none { it.이름 == b.이름 }) 정보[b.이름] = b.부위 to b.장비 }
    val 추천 = 이름추천.찾기(이름, 정보.keys.toList())
    입력칸(이름, on이름, Modifier.fillMaxWidth(), 안내 = "종목 이름 (예: 벤치프레스)")
    if (추천.isNotEmpty()) {
        Column(
            Modifier.fillMaxWidth().padding(top = 4.dp).clip(RoundedCornerShape(모서리.작게))
                .background(c.면).border(1.dp, c.선, RoundedCornerShape(모서리.작게)),
        ) {
            추천.forEachIndexed { i, n ->
                if (i > 0) 구분선()
                val (부, 장) = 정보.getValue(n)
                Row(Modifier.fillMaxWidth().눌림 { on고름(n, 부, 장) }.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                    글(n, Modifier.weight(1f, fill = false), 크기값 = 크기.버튼, 굵기 = FontWeight.Medium)
                    Box(Modifier.width(6.dp))
                    글(listOf(부, 장).filter { it.isNotBlank() }.joinToString("·"), Modifier.weight(1f), 크기값 = 크기.작게, 색 = c.옅음)
                    if (표.any { it.이름 == n }) 글("내 종목", 크기값 = 크기.작게, 색 = c.강조)
                }
            }
        }
    }
}

/** 장비 고르기 — 선택지 + 전에 쓴 장비 + '직접 입력' (09-21 메모). 고른 것을 다시 누르면 비운다 */
@Composable
fun 장비고르기(상태: 앱상태, 선택: String, on선택: (String) -> Unit) {
    val c = Local색.current
    val 목록 = (이름추천.장비목록 + 상태.d.종목표.map { it.장비.trim() } + listOf(선택.trim())).filter { it.isNotBlank() }.distinct()
    var 직접 by remember { mutableStateOf(false) }
    var 새 by remember { mutableStateOf("") }
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        목록.forEach { p ->
            val 켬 = p == 선택.trim()
            Box(
                Modifier.height(높이.낮게).clip(CircleShape).background(if (켬) c.강조 else c.면2)
                    .눌림 { 직접 = false; on선택(if (켬) "" else p) }.padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) { 글(p, 크기값 = 크기.버튼, 색 = if (켬) c.강조글 else c.흐림, 굵기 = FontWeight.Medium) }
        }
        Box(
            Modifier.height(높이.낮게).clip(CircleShape).border(1.dp, if (직접) c.강조 else c.선, CircleShape)
                .눌림 { 직접 = !직접 }.padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) { 글("직접 입력", 크기값 = 크기.버튼, 색 = if (직접) c.강조 else c.흐림, 굵기 = FontWeight.Medium) }
    }
    if (직접) {
        val 넣기 = { val n = 새.trim(); if (n.isNotEmpty()) on선택(n); 새 = ""; 직접 = false }
        Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            입력칸(새, { 새 = it }, Modifier.weight(1f), 안내 = "장비 이름", onDone = 넣기)
            Box(Modifier.width(6.dp))
            버튼("넣기", 넣기, 작게 = true, 주요 = true)
        }
    }
}
