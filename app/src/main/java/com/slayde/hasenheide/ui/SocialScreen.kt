package com.slayde.hasenheide.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 소셜 탭 (10-05 옮기기 1단계 — 틀만). 2단계 PF 도우미가 채운다.
 *
 * 시안 `소셜탭()` (10-05 v21 ⑤ "그룹운동 및 챌린지 기능을 넣으려고해") —
 *  맨 위 띠(루틴 · 플랜과 같은 `.띠 가운데띠`) + 빈 화면 (설명 글 없음). 시안도 지금은 이것뿐
 */
@Composable
fun 소셜화면(상태: 앱상태) {
    Column(Modifier.fillMaxSize()) {
        머리띠("소셜")
        Box(Modifier.fillMaxWidth().weight(1f)) {
            // `.넘김.띠아래` — 띠 아래 틈 12 · 옆 12. 2단계에서 채운다
        }
    }
}
