package com.slayde.hasenheide.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * 종목 넣기 시트 (시안 v19 `넣기목록` · v21) — 루틴 화면과 운동 화면이 **같이** 쓴다.
 *
 * ── 2단계 소유: RT(루틴) 도우미가 속을 채운다. 매개변수(이름 · 꼴)는 바꾸지 않는다 ──
 * W(운동) 도우미는 이 함수를 **부르기만** 한다. 새 매개변수가 꼭 필요하면 기본값을 단다.
 *
 * 시안 동작: 2열 상자 · 칩 한 줄 ‹ › · [전체] 기본 · 누름 = 넣기(이미 들어 있으면 빼기) · 꾹 = 하나 더 ·
 * 들어간 것은 ✓×n · 말풍선 · 위끝 20% 고정 · 맨 아래 [새 종목] → [새종목시트].
 *
 * @param 개수 종목 열쇠(`종목.id`) → 지금 들어 있는 개수 (✓×n)
 * @param 넣기 열쇠 하나를 넣는다 (꾹 = 한 번 더 부른다)
 * @param 빼기 열쇠 하나를 뺀다 (들어 있는 것을 누름)
 * @param 새종목만들기 true 면 맨 아래 [새 종목] — 만든 종목은 바로 [넣기] 로 넣는다
 */
@Composable
fun 종목넣기시트(
    상태: 앱상태,
    제목: String,
    개수: (열쇠: String) -> Int,
    넣기: (열쇠: String) -> Unit,
    빼기: (열쇠: String) -> Unit,
    닫기: () -> Unit,
    새종목만들기: Boolean = true,
) {
    var 새로 by remember { mutableStateOf(false) }
    // 1단계 틀 — 지금은 종목표를 한 줄씩 늘어놓기만 한다 (RT 도우미가 시안대로 바꾼다)
    시트(제목, 닫기, 위끝고정 = true) {
        상태.d.종목표.forEach { e ->
            val n = 개수(e.id)
            글(if (n > 0) "${e.이름} ✓×$n" else e.이름, androidx.compose.ui.Modifier.눌림 { if (n > 0) 빼기(e.id) else 넣기(e.id) })
        }
        if (새종목만들기) 글("＋ 새 종목", androidx.compose.ui.Modifier.눌림 { 새로 = true })
    }
    if (새로) 새종목시트(상태, 닫기 = { 새로 = false }, 저장 = { e -> 새로 = false; 넣기(e.id) })
}
