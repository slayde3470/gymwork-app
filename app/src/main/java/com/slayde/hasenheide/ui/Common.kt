@file:OptIn(ExperimentalFoundationApi::class)

package com.slayde.hasenheide.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.글꼴
import com.slayde.hasenheide.ui.theme.높이
import com.slayde.hasenheide.ui.theme.모서리
import com.slayde.hasenheide.ui.theme.크기

/**
 * 여러 화면이 같이 쓰는 부품.
 * '같은 동작은 같은 모양'(기능명세 1-3)을 지키려고 한곳에 모았다.
 */

/** 글자 — 설명 글은 한 줄을 넘지 않는다 (1-1). 넘치면 끝을 … 로 */
@Composable
fun 글(
    text: String,
    modifier: Modifier = Modifier,
    크기값: TextUnit = 크기.본문,
    색: Color = Local색.current.글,
    굵기: FontWeight = FontWeight.Normal,
    가운데: Boolean = false,
    줄: Int = 1,
) {
    Text(
        text, modifier = modifier, style = 글꼴.보통(크기값, 굵기), color = 색, maxLines = 줄,
        overflow = TextOverflow.Ellipsis, textAlign = if (가운데) TextAlign.Center else null,
    )
}

@Composable
fun 제목글(text: String, modifier: Modifier = Modifier, 크기값: TextUnit = 크기.제목, 색: Color = Local색.current.글) {
    Text(text, modifier = modifier, style = 글꼴.제목(크기값), color = 색, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

/** 누를 때 파란 빛(물결)을 칠하지 않는다 (1-3) */
fun Modifier.눌림(onClick: () -> Unit): Modifier = this.clickable(
    interactionSource = MutableInteractionSource(), indication = null, onClick = onClick,
)

/** 누르고 떼기 = 열기, 꾹 누르기 = 집기 (1-3) */
fun Modifier.눌림길게(onClick: () -> Unit, onLongClick: (() -> Unit)?): Modifier = this.combinedClickable(
    interactionSource = MutableInteractionSource(), indication = null, onClick = onClick, onLongClick = onLongClick,
)

@Composable
fun 카드(modifier: Modifier = Modifier, 안쪽: Dp = 14.dp, content: @Composable ColumnScope.() -> Unit) {
    val c = Local색.current
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(모서리.보통))
            .background(c.면)
            .border(1.dp, c.선, RoundedCornerShape(모서리.보통))
            .padding(안쪽),
        content = content,
    )
}

@Composable
fun 구분선(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).background(Local색.current.선))
}

/** 아이콘 버튼 — 네모 32. 칠함=false 는 '지우기'·'펼치기'처럼 바탕 없는 것 */
@Composable
fun 아이콘버튼(
    그림: ImageVector,
    설명: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    칠함: Boolean = true,
    켬: Boolean = false,
    색: Color? = null,
    크기칸: Dp = 높이.낮게,
    쓸수있음: Boolean = true,
) {
    val c = Local색.current
    val 바탕 = when { 켬 -> c.강조; 칠함 -> c.면2; else -> Color.Transparent }
    val 글색 = 색 ?: if (켬) c.강조글 else c.흐림
    Box(
        modifier
            .size(크기칸)
            .clip(RoundedCornerShape(모서리.아주작게))
            .background(바탕)
            .then(if (쓸수있음) Modifier.눌림(onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Icon(그림, 설명, Modifier.size(18.dp), tint = if (쓸수있음) 글색 else 글색.copy(alpha = 0.3f))
    }
}

/** 글 버튼 — 기본(48) · 작게(40). 주요=true 면 강조색으로 칠한다 */
@Composable
fun 버튼(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    주요: Boolean = false,
    작게: Boolean = false,
    그림: ImageVector? = null,
    글색: Color? = null,
) {
    val c = Local색.current
    Row(
        modifier
            .height(if (작게) 높이.보통 else 높이.높게)
            .clip(RoundedCornerShape(모서리.작게))
            .background(if (주요) c.강조 else c.면2)
            .then(if (주요) Modifier else Modifier.border(1.dp, c.선, RoundedCornerShape(모서리.작게)))
            .눌림(onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val 색 = 글색 ?: if (주요) c.강조글 else c.글
        if (그림 != null) {
            Icon(그림, null, Modifier.size(16.dp), tint = 색)
            Box(Modifier.width(6.dp))
        }
        글(text, 크기값 = if (작게) 크기.버튼 else 크기.본문, 색 = 색, 굵기 = FontWeight.Bold)
    }
}

/** 칩 한 줄 — 넘치면 옆으로 밀어서 꺼낸다 (1-1) */
@Composable
fun 칩줄(목록: List<String>, 선택: String?, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    val c = Local색.current
    Row(
        modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        목록.forEach { p ->
            val 켬 = p == 선택
            Box(
                Modifier
                    .height(높이.낮게)
                    .clip(CircleShape)
                    .background(if (켬) c.강조 else c.면2)
                    .눌림 { onSelect(p) }
                    .padding(horizontal = 13.dp),
                contentAlignment = Alignment.Center,
            ) { 글(p, 크기값 = 크기.버튼, 색 = if (켬) c.강조글 else c.흐림, 굵기 = FontWeight.Medium) }
        }
    }
}

/** 켜고 끄기 */
@Composable
fun 스위치(켜짐: Boolean, onChange: (Boolean) -> Unit) {
    val c = Local색.current
    Box(
        Modifier
            .width(46.dp).height(높이.아주낮게)
            .clip(CircleShape)
            .background(if (켜짐) c.강조 else c.선)
            .눌림 { onChange(!켜짐) }
            .padding(3.dp),
        contentAlignment = if (켜짐) Alignment.CenterEnd else Alignment.CenterStart,
    ) { Box(Modifier.size(22.dp).clip(CircleShape).background(if (켜짐) c.강조글 else c.면)) }
}

/** 설정 한 줄 — 이름 · 한 줄 설명 · 오른쪽 조작부 */
@Composable
fun 설정줄(이름: String, 설명: String? = null, 오른쪽: @Composable RowScope.() -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            글(이름, 굵기 = FontWeight.Medium)
            if (설명 != null) 글(설명, 크기값 = 크기.작게, 색 = Local색.current.옅음)
        }
        오른쪽()
    }
}

/** 작은 이름표(섹션 제목) */
@Composable
fun 이름표(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier, style = 글꼴.이름표, color = Local색.current.옅음, maxLines = 1)
}

/** 한 줄 글 입력칸 */
@Composable
fun 입력칸(
    값: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    안내: String = "",
    숫자: Boolean = false,
    onDone: (() -> Unit)? = null,
) {
    val c = Local색.current
    val 초점 = LocalFocusManager.current
    Box(
        modifier
            .height(높이.높게)
            .clip(RoundedCornerShape(모서리.작게))
            .background(c.면)
            .border(1.dp, c.선, RoundedCornerShape(모서리.작게))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (값.isEmpty()) 글(안내, 색 = c.옅음)
        BasicTextField(
            value = 값, onValueChange = onChange, singleLine = true,
            textStyle = 글꼴.보통(크기.본문).copy(color = c.글),
            cursorBrush = SolidColor(c.강조),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (숫자) KeyboardType.Decimal else KeyboardType.Text, imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onDone?.invoke(); 초점.clearFocus() }),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ─────────────── 숫자 버튼 줄 (1-3 · 4-4-1) ───────────────

/** 버튼 한 칸 — 평소엔 [이름표/값], 누르면 [− 값 ＋] */
data class 숫자칸(
    val 키: String,
    val 라벨: String,
    val 값글: String,
    val 단위: String,
    val 소수: Boolean = false,
    val 빼기: () -> Unit,
    val 더하기: () -> Unit,
    /** 직접 쳐 넣은 글 — 받는 쪽에서 숫자로 바꾼다 */
    val 넣기: (String) -> Unit,
)

@Composable
fun 숫자버튼줄(칸들: List<숫자칸>, 켠: String?, on고름: (String) -> Unit, modifier: Modifier = Modifier) {
    val c = Local색.current
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(모서리.작게))
            .background(c.면)
            .border(1.dp, c.선, RoundedCornerShape(모서리.작게))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        칸들.forEach { k ->
            if (k.키 == 켠) {
                Row(
                    Modifier
                        .weight(2.3f)
                        .height(높이.보통)
                        .clip(RoundedCornerShape(모서리.작게))
                        .background(c.면)
                        .border(1.5.dp, c.강조, RoundedCornerShape(모서리.작게)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.width(30.dp).fillMaxHeight().눌림(k.빼기), contentAlignment = Alignment.Center) {
                        Icon(아이콘.빼기, "${k.라벨} 빼기", Modifier.size(18.dp), tint = c.강조)
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        글(k.라벨, 크기값 = 크기.아주작게, 색 = c.옅음)
                        숫자입력(k.값글, k.소수, k.넣기)
                    }
                    Box(Modifier.width(30.dp).fillMaxHeight().눌림(k.더하기), contentAlignment = Alignment.Center) {
                        Icon(아이콘.더하기, "${k.라벨} 더하기", Modifier.size(18.dp), tint = c.강조)
                    }
                }
            } else {
                Column(
                    Modifier
                        .weight(1f)
                        .height(높이.보통)
                        .clip(RoundedCornerShape(모서리.작게))
                        .background(c.면2)
                        .눌림 { on고름(k.키) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    글(k.라벨, 크기값 = 크기.아주작게, 색 = c.옅음)
                    글(k.값글 + k.단위, 크기값 = 크기.버튼, 굵기 = FontWeight.Bold)
                }
            }
        }
    }
}

/** 직접 쳐 넣는 숫자 — 다 치고 '완료'를 누르거나 다른 곳을 누를 때 반영한다 */
@Composable
private fun 숫자입력(값글: String, 소수: Boolean, 넣기: (String) -> Unit) {
    val c = Local색.current
    val 초점 = LocalFocusManager.current
    var 글값 by remember(값글) { mutableStateOf(값글) }
    var 만졌나 by remember { mutableStateOf(false) }
    BasicTextField(
        value = 글값,
        onValueChange = { 글값 = it; 만졌나 = true },
        singleLine = true,
        textStyle = TextStyle(fontSize = 크기.본문, fontWeight = FontWeight.Bold, color = c.글, textAlign = TextAlign.Center),
        cursorBrush = SolidColor(c.강조),
        keyboardOptions = KeyboardOptions(
            keyboardType = if (소수) KeyboardType.Decimal else KeyboardType.Number, imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { if (만졌나) 넣기(글값); 만졌나 = false; 초점.clearFocus() }),
        modifier = Modifier
            .widthIn(max = 64.dp)
            .onFocusChanged { if (!it.isFocused && 만졌나) { 넣기(글값); 만졌나 = false } },
    )
}

// ─────────────── 시트 · 띠 ───────────────

/** 아래에서 올라오는 판 — '고르는 일'에만 쓴다 (1-1: 팝업은 고르기뿐) */
@Composable
fun 시트(제목: String, onClose: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val c = Local색.current
    BackHandler(onBack = onClose)
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)).눌림(onClose), contentAlignment = Alignment.BottomCenter) {
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
                .clip(RoundedCornerShape(topStart = 모서리.크게, topEnd = 모서리.크게))
                .background(c.면)
                .눌림 { }
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 간격.넓게, vertical = 14.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                제목글(제목, Modifier.weight(1f), 크기값 = 크기.크게)
                아이콘버튼(아이콘.닫기, "닫기", onClose, 크기칸 = 높이.낮게)
            }
            Column(Modifier.padding(top = 10.dp).verticalScroll(rememberScrollState()), content = content)
        }
    }
}

/** 화면 아래(탭 위)에 뜨는 띠 — 되돌리기 · 집기 안내가 같은 자리 · 같은 모양 */
@Composable
fun BoxScope.아래띠(글자: String, 버튼글: String, on버튼: () -> Unit, 어둡게: Boolean = true, 바깥: Modifier = Modifier) {
    val c = Local색.current
    Row(
        바깥
            .align(Alignment.BottomCenter)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(모서리.작게))
            .background(if (어둡게) c.글 else c.면)
            .border(1.dp, if (어둡게) c.글 else c.휴식, RoundedCornerShape(모서리.작게))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        글(글자, Modifier.weight(1f), 크기값 = 크기.버튼, 색 = if (어둡게) c.면 else c.휴식)
        Box(Modifier.width(10.dp))
        글(버튼글, Modifier.눌림(on버튼), 크기값 = 크기.버튼, 색 = if (어둡게) c.강조옅음 else c.휴식, 굵기 = FontWeight.Bold)
    }
}

/** 목록 한 줄 (시트 안에서 고르는 것) */
@Composable
fun 고르기줄(이름: String, 곁: String? = null, 오른쪽: ImageVector? = 아이콘.오른쪽, 흐림: Boolean = false, onClick: () -> Unit) {
    val c = Local색.current
    Row(
        Modifier.fillMaxWidth().눌림(onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
            글(이름, Modifier.weight(1f, fill = false), 색 = if (흐림) c.옅음 else c.글)
            if (곁 != null) { Box(Modifier.width(6.dp)); 글(곁, 크기값 = 크기.작게, 색 = c.옅음) }
        }
        if (오른쪽 != null) Icon(오른쪽, null, Modifier.size(18.dp), tint = if (흐림) c.옅음 else c.강조)
    }
    구분선()
}
