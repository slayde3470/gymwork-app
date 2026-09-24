package com.slayde.hasenheide.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.slayde.hasenheide.data.수정메모
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.높이
import com.slayde.hasenheide.ui.theme.크기
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 쓰던 메모 · 고치던 메모 — 시트를 닫아도 남는다 (09-24 메모: 화면을 확인하러 닫았더니 쓰던 글이 사라졌다).
 * 앱 화면(App.kt)이 들고 있으므로 시트를 다시 열면 이어서 쓸 수 있다. 앱을 완전히 끄면 사라진다.
 */
class 메모초안 {
    var 새글 by mutableStateOf("")
    var 고치는중 by mutableStateOf<Long?>(null)   // 고치고 있는 메모의 시각
    var 고친글 by mutableStateOf("")
}

/**
 * 수정 메모 — 써보면서 고칠 점을 바로 적어 둔다.
 * 어느 화면에서 적었는지, **적기 바로 전에 무슨 동작을 했는지(발자취 10가지)** 가 함께 남는다 (09-24 메모).
 * '전부 복사'로 Claude 에게 붙여넣으면 된다.
 */
@Composable
fun 메모시트(상태: 앱상태, 화면: String, 폰: 폰기능, 초안: 메모초안, onClose: () -> Unit) {
    val c = Local색.current
    // 메모를 누르면 전체가 펼쳐지고, 다시 누르면 접힌다 (09-24 메모: 요약돼서 뭐라 썼는지 안 보였다)
    var 펼친메모 by remember { mutableStateOf(setOf<Long>()) }
    var 펼친흔적 by remember { mutableStateOf<Long?>(null) }
    val 때 = DateTimeFormatter.ofPattern("M/d HH:mm").withZone(ZoneId.systemDefault())
    val 넣기 = {
        val t = 초안.새글.trim()
        if (t.isNotEmpty()) {
            // 메모를 적기 직전에 한 동작 10가지를 같이 담는다
            val 흔적 = 발자취.최근(10)
            상태.바꿈 { it.copy(메모 = it.메모 + 수정메모(System.currentTimeMillis(), 화면, t, 흔적)) }
            초안.새글 = ""
        }
    }
    시트("수정 메모 · $화면", onClose) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            입력칸(초안.새글, { 초안.새글 = it }, Modifier.weight(1f), 안내 = "고칠 점을 적어 두세요", onDone = 넣기)
            Box(Modifier.padding(start = 8.dp)) { 버튼("넣기", 넣기, 주요 = true) }
        }
        Box(Modifier.height(12.dp))
        val 메모 = 상태.d.메모
        if (메모.isEmpty()) 글("아직 적은 메모가 없습니다", 색 = c.옅음, 크기값 = 크기.버튼)
        메모.reversed().forEach { m ->
            if (초안.고치는중 == m.시각) {
                // ── 고치는 중 ──
                Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    글("${때.format(Instant.ofEpochMilli(m.시각))} · ${m.화면}", 크기값 = 크기.작게, 색 = c.옅음)
                    Box(Modifier.height(4.dp))
                    // 그 자리에서 여러 줄로 — 글 전체가 보여서 고칠 곳을 바로 찾는다 (09-24 메모)
                    입력칸(초안.고친글, { 초안.고친글 = it }, Modifier.fillMaxWidth(), 안내 = "메모 고치기", 여러줄 = true)
                    Box(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        버튼("취소", { 초안.고치는중 = null }, Modifier.weight(1f), 작게 = true)
                        버튼("저장", {
                            val t = 초안.고친글.trim()
                            if (t.isNotEmpty()) 상태.바꿈 { d ->
                                d.copy(메모 = d.메모.map { if (it.시각 == m.시각) it.copy(글 = t) else it })
                            }
                            초안.고치는중 = null
                        }, Modifier.weight(1f), 작게 = true, 주요 = true)
                    }
                }
            } else {
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        글("${때.format(Instant.ofEpochMilli(m.시각))} · ${m.화면}", 크기값 = 크기.작게, 색 = c.옅음)
                        val 펼침 = m.시각 in 펼친메모
                        글(m.글, Modifier.fillMaxWidth().눌림 { 펼친메모 = if (펼침) 펼친메모 - m.시각 else 펼친메모 + m.시각 },
                            줄 = if (펼침) Int.MAX_VALUE else 6)
                        if (m.흔적.isNotEmpty()) {
                            val 열림 = 펼친흔적 == m.시각
                            글(
                                if (열림) "직전 동작 접기" else "직전 동작 ${m.흔적.size}가지 보기",
                                Modifier.눌림 { 펼친흔적 = if (열림) null else m.시각 }.padding(top = 2.dp),
                                크기값 = 크기.작게, 색 = c.강조,
                            )
                            if (열림) m.흔적.forEach { 글(it, 크기값 = 크기.작게, 색 = c.옅음) }
                        }
                    }
                    아이콘버튼(아이콘.연필, "메모 고치기", { 초안.고치는중 = m.시각; 초안.고친글 = m.글 }, 칠함 = false, 크기칸 = 높이.낮게)
                    아이콘버튼(아이콘.지우기, "메모 지우기", {
                        상태.지우고알림("메모를 지웠습니다") { d -> d.copy(메모 = d.메모.filter { it.시각 != m.시각 }) }
                    }, 칠함 = false)
                }
            }
            구분선()
        }
        if (메모.isNotEmpty()) {
            Box(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                버튼("전부 복사", { 폰.복사(복사글(메모)) }, Modifier.weight(1f), 주요 = true, 그림 = 아이콘.목록)
                버튼("전부 지우기", {
                    상태.지우고알림("메모 ${메모.size}개를 지웠습니다") { it.copy(메모 = emptyList()) }
                }, Modifier.weight(1f), 글색 = c.나쁨)
            }
        }
    }
}

/** 붙여넣기용 글 — 메모 아래에 직전 동작을 들여쓰기해 붙인다 */
private fun 복사글(메모: List<수정메모>): String = 메모.joinToString("\n") { m ->
    val 머리 = "- [${m.화면}] ${m.글}"
    if (m.흔적.isEmpty()) 머리 else 머리 + m.흔적.joinToString("") { "\n    · $it" }
}
