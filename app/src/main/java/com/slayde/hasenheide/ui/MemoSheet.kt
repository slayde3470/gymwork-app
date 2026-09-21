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
import com.slayde.hasenheide.ui.theme.크기
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 수정 메모 — 써보면서 고칠 점을 바로 적어 둔다.
 * 어느 화면에서 적었는지가 함께 남는다. '전부 복사'로 Claude 에게 붙여넣으면 된다.
 */
@Composable
fun 메모시트(상태: 앱상태, 화면: String, 폰: 폰기능, onClose: () -> Unit) {
    val c = Local색.current
    var 새글 by remember { mutableStateOf("") }
    val 때 = DateTimeFormatter.ofPattern("M/d HH:mm").withZone(ZoneId.systemDefault())
    val 넣기 = {
        val t = 새글.trim()
        if (t.isNotEmpty()) {
            상태.바꿈 { it.copy(메모 = it.메모 + 수정메모(System.currentTimeMillis(), 화면, t)) }
            새글 = ""
        }
    }
    시트("수정 메모 · $화면", onClose) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            입력칸(새글, { 새글 = it }, Modifier.weight(1f), 안내 = "고칠 점을 적어 두세요", onDone = 넣기)
            Box(Modifier.padding(start = 6.dp)) { 버튼("넣기", 넣기, 주요 = true) }
        }
        Box(Modifier.height(10.dp))
        val 메모 = 상태.d.메모
        if (메모.isEmpty()) 글("아직 적은 메모가 없습니다", 색 = c.옅음, 크기값 = 크기.버튼)
        메모.reversed().forEach { m ->
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    글("${때.format(Instant.ofEpochMilli(m.시각))} · ${m.화면}", 크기값 = 크기.작게, 색 = c.옅음)
                    글(m.글, 줄 = 6)
                }
                아이콘버튼(아이콘.지우기, "메모 지우기", {
                    상태.지우고알림("메모를 지웠습니다") { d -> d.copy(메모 = d.메모.filter { it != m }) }
                }, 칠함 = false)
            }
            구분선()
        }
        if (메모.isNotEmpty()) {
            Box(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                버튼("전부 복사", {
                    폰.복사(메모.joinToString("\n") { "- [${m화면(it)}] ${it.글}" })
                }, Modifier.weight(1f), 주요 = true, 그림 = 아이콘.목록)
                버튼("전부 지우기", {
                    상태.지우고알림("메모 ${메모.size}개를 지웠습니다") { it.copy(메모 = emptyList()) }
                }, Modifier.weight(1f), 글색 = c.나쁨)
            }
        }
    }
}

private fun m화면(m: 수정메모) = m.화면
