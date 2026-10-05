package com.slayde.hasenheide.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 프로필 탭 (10-05 옮기기 1단계 — 틀만). 2단계 PF 도우미가 채운다.
 *
 * 시안 `프로필탭()` (10-03 v10 rvva · v17 C · v18 E) — 머리 띠 없이 바로 아래 묶음들:
 *  · `프로필고르기("크게")` 사진 58 + 닉네임 칸(`설정.닉네임` 12자) + SNS 링크 단추(`설정.링크`) + `큰운동판` + `톱니단추`
 *  · `최근업적줄` — 얻은 업적 한 줄 · 꾹 끌어 순서(Parts.kt 가로끌기 · 데이터 업적순서옮김) · 숨김(`업적숨김`)
 *  · `인증판` — 인증샷 최대 8 · 3:4 · 한 줄 4칸 · 고정 3 (`인증샷` · 아이콘.핀)
 *  · 시트: `링크` · `링크목록` · `인증`
 * 탭줄의 프로필 칸 동그라미 = Parts.kt `프로필동그라미` (같은 것을 여기서도 쓴다)
 */
@Composable
fun 프로필화면(상태: 앱상태) {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // 2단계: 프묶음 셋 (인머리 · 인업적 · 인판)
        }
    }
}
