package com.slayde.hasenheide.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.글꼴
import com.slayde.hasenheide.ui.theme.높이
import com.slayde.hasenheide.ui.theme.모서리
import com.slayde.hasenheide.ui.theme.선굵기
import com.slayde.hasenheide.ui.theme.크기

/**
 * 검색(돋보기) 탭 (10-05 옮기기 2단계 PF · 시안 v21 `검색탭` · v17 ④ · v18 E ⑥ · v19 A ②)
 *
 *  · 맨 위 고정 줄(스크롤 밖) — 위 12 · 옆 12 · 아래 8: [북마크 40] [돋보기 + 흐린 '검색' 칸 40]
 *  · 사진 칸 12 = 3열 × 4줄 · 3:4 · 사이 2 · 스크롤 없이 — 낮은 폰에서는 칸 비율 그대로 판 전체를 줄인다
 *  · 다른 유저 사진 · 피드 · 북마크 · 검색은 아직 자리만 (목록 '옮기지 않는 것') — 눌러도 '준비 중' 만 알린다
 */
@Composable
fun 검색화면(상태: 앱상태) {
    Column(Modifier.fillMaxSize()) {
        검색줄(상태)
        당겨새로고침({ 상태.날짜확인() }, Modifier.fillMaxWidth().weight(1f)) {
            BoxWithConstraints(Modifier.fillMaxSize().padding(start = 간격.보통, end = 간격.보통, bottom = 간격.보통)) {
                val 판폭 = 찾판폭(maxWidth, maxHeight)
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                    찾판(Modifier.width(판폭))
                }
            }
        }
    }
}

/** 이 화면의 글 · 치수 — 한곳에 (시안 값 그대로) */
object 검색글 {
    const val 북마크 = "북마크"
    const val 검색 = "검색"
    const val 준비중 = "준비 중입니다"
    /** 사진 칸 — 3열 × 4줄 (시안 `찾최대` 12) */
    const val 열 = 3
    const val 줄 = 4
    val 사이 = 2.dp
    val 줄위 = 간격.보통
    val 줄아래 = 간격.좁게
}

/** 판 폭 — 4줄이 [높이] 안에 들어가게 (시안 `.찾판 width:min(100%, (100cqh − 6px) × 9/16 + 4px)`) */
internal fun 찾판폭(폭: Dp, 높이값: Dp): Dp {
    val 사이 = 검색글.사이
    val 칸높이 = (높이값 - 사이 * (검색글.줄 - 1)) / 검색글.줄
    val 맞춤 = 칸높이 * 3f / 4f * 검색글.열.toFloat() + 사이 * (검색글.열 - 1)
    return if (맞춤 < 폭 && 맞춤 > 0.dp) 맞춤 else 폭
}

/** 시안 `.찾위 .찾줄` — 북마크 40(아이콘 18 흐림) · 검색 칸 40(테 1 속선 · 모서리 8 · 바탕 면 · 돋보기 18 옅음 · 15) */
@Composable
private fun 검색줄(상태: 앱상태) {
    val c = Local색.current
    val 초점 = LocalFocusManager.current
    var 찾글 by rememberSaveable { mutableStateOf("") }
    var 쓰는중 by rememberSaveable { mutableStateOf(false) }
    val 모양 = RoundedCornerShape(모서리.작게)
    Row(
        Modifier.fillMaxWidth().padding(start = 간격.보통, end = 간격.보통, top = 검색글.줄위, bottom = 검색글.줄아래),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.좁게),
    ) {
        Box(
            Modifier.size(높이.보통).눌림 { 상태.알림.토스트(검색글.준비중) }.semantics { contentDescription = 검색글.북마크; role = Role.Button },
            contentAlignment = Alignment.Center,
        ) { Icon(아이콘.북마크, null, Modifier.size(그림18), tint = c.흐림) }
        Row(
            Modifier.weight(1f).height(높이.보통).clip(모양).background(c.면).border(선굵기.보통, if (쓰는중) c.강조 else c.속선, 모양)
                .padding(horizontal = 간격.보통),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.좁게),
        ) {
            Icon(아이콘.돋보기, null, Modifier.size(그림18), tint = c.옅음)
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (찾글.isEmpty()) 글(검색글.검색, 색 = c.옅음)
                BasicTextField(
                    찾글, { 찾글 = it }, Modifier.fillMaxWidth().onFocusChanged { 쓰는중 = it.isFocused }.semantics { contentDescription = 검색글.검색 },
                    singleLine = true, textStyle = 글꼴.보통(크기.본문).copy(color = c.글), cursorBrush = SolidColor(c.강조),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { 초점.clearFocus(); 상태.알림.토스트(검색글.준비중) }),
                )
            }
        }
    }
}

private val 그림18 = 18.dp   // 11 지침 U3-6 기본 아이콘

/** 시안 `.찾판` — 3열 · 3:4 · 사이 2 · 바탕 면2 (자리만) */
@Composable
private fun 찾판(modifier: Modifier) {
    val c = Local색.current
    Column(modifier, verticalArrangement = Arrangement.spacedBy(검색글.사이)) {
        repeat(검색글.줄) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(검색글.사이)) {
                repeat(검색글.열) { Box(Modifier.weight(1f).aspectRatio(3f / 4f).background(c.면2)) }
            }
        }
    }
}
