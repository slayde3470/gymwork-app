package com.slayde.hasenheide.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 검색(돋보기) 탭 (10-05 옮기기 1단계 — 틀만). 2단계 PF 도우미가 채운다.
 *
 * 시안 `검색탭()` (10-04 v17 ④ · v18 E ⑥ · v19 A ②) — 머리 띠 대신 맨 위 고정 줄:
 *  · `.찾위` 위 12 · 옆 12 · 아래 8 — [북마크 40 (아이콘.북마크)] [돋보기 + 흐린 '검색' 칸 40] (스크롤 밖에 고정)
 *  · `.찾판` 사진 칸 12 = 3열 × 4줄 · 3:4 · 사이 2 · 스크롤 없이 (자리만 — 지금은 빈 칸)
 *  · 다른 유저 · 피드 · 북마크는 나중 (자리만)
 */
@Composable
fun 검색화면(상태: 앱상태) {
    Column(Modifier.fillMaxSize()) {
        // 2단계: 찾줄 (북마크 · 검색 칸)
        Box(Modifier.fillMaxWidth().weight(1f)) {
            // 2단계: 찾판 (3 × 4 빈 사진 칸)
        }
    }
}
