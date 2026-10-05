package com.slayde.hasenheide.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slayde.hasenheide.ui.theme.간격

/**
 * 소셜 탭 (10-05 옮기기 2단계 PF · 시안 v21 ⑤ `소셜탭` "그룹운동 및 챌린지 기능을 넣으려고해") —
 *  맨 위 띠(루틴 · 플랜과 같은 `.띠 가운데띠`) + 빈 화면 (설명 글 없음). 시안도 지금은 이것뿐.
 *  당겨서 새로고침은 띠 아래만 (v19 A ②) · 띠 아래 틈 12 · 옆 12 (`.넘김.띠아래`)
 */
@Composable
fun 소셜화면(상태: 앱상태) {
    Column(Modifier.fillMaxSize()) {
        머리띠("소셜")
        당겨새로고침({ 상태.날짜확인() }, Modifier.fillMaxWidth().weight(1f)) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 간격.보통, end = 간격.보통, top = 간격.보통, bottom = 간격.보통)) {
                Box(Modifier.fillMaxWidth())
            }
        }
    }
}
