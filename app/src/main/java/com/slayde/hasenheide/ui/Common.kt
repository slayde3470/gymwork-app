@file:OptIn(ExperimentalFoundationApi::class)

package com.slayde.hasenheide.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import com.slayde.hasenheide.data.분초
import com.slayde.hasenheide.data.초읽기
import com.slayde.hasenheide.data.휴식최대
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
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
    /** 32 높이 — 캘린더 아래 판처럼 자리가 좁은 곳 */
    낮게: Boolean = false,
) {
    val c = Local색.current
    Row(
        modifier
            .height(if (낮게) 높이.낮게 else if (작게) 높이.보통 else 높이.높게)
            .clip(RoundedCornerShape(모서리.작게))
            .background(if (주요) c.강조 else c.면2)
            .then(if (주요) Modifier else Modifier.border(1.dp, c.선, RoundedCornerShape(모서리.작게)))
            .눌림(onClick)
            .padding(horizontal = if (낮게) 8.dp else 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val 색 = 글색 ?: if (주요) c.강조글 else c.글
        if (그림 != null) {
            Icon(그림, null, Modifier.size(if (낮게) 14.dp else 16.dp), tint = 색)
            Box(Modifier.width(if (낮게) 4.dp else 6.dp))
        }
        글(text, 크기값 = if (낮게) 크기.조금작게 else if (작게) 크기.버튼 else 크기.본문, 색 = 색, 굵기 = FontWeight.Bold)
    }
}

/** 칩 한 줄 — 넘치면 옆으로 밀어서 꺼낸다 (1-1) */
@Composable
fun 칩줄(목록: List<String>, 선택: String?, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    val c = Local색.current
    Row(
        modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
            .padding(4.dp),
        contentAlignment = if (켜짐) Alignment.CenterEnd else Alignment.CenterStart,
    ) { Box(Modifier.size(22.dp).clip(CircleShape).background(if (켜짐) c.강조글 else c.면)) }
}

/** 설정 한 줄 — 이름 · 한 줄 설명 · 오른쪽 조작부 */
@Composable
fun 설정줄(이름: String, 설명: String? = null, 오른쪽: @Composable RowScope.() -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
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
    여러줄: Boolean = false,   // 긴 글을 고칠 때 — 줄을 바꿔 가며 전부 보인다 (09-24 메모: 한 줄이라 가로로 찾아야 했다)
) {
    val c = Local색.current
    val 초점 = LocalFocusManager.current
    Box(
        modifier
            .then(if (여러줄) Modifier.heightIn(min = 높이.높게) else Modifier.height(높이.높게))
            .clip(RoundedCornerShape(모서리.작게))
            .background(c.면)
            .border(1.dp, c.선, RoundedCornerShape(모서리.작게))
            .padding(horizontal = 12.dp, vertical = if (여러줄) 10.dp else 0.dp),
        contentAlignment = if (여러줄) Alignment.TopStart else Alignment.CenterStart,
    ) {
        if (값.isEmpty()) 글(안내, 색 = c.옅음)
        BasicTextField(
            value = 값, onValueChange = onChange, singleLine = !여러줄,
            textStyle = 글꼴.보통(크기.본문).copy(color = c.글),
            cursorBrush = SolidColor(c.강조),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (숫자) KeyboardType.Decimal else KeyboardType.Text,
                imeAction = if (여러줄) ImeAction.Default else ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onDone?.invoke(); 초점.clearFocus() }),
            modifier = if (여러줄) Modifier.fillMaxWidth().heightIn(max = 280.dp) else Modifier.fillMaxWidth(),
        )
    }
}

// ─────────────── 숫자 버튼 줄 (1-3 · 4-4-1 · 09-21 메모) ───────────────

enum class 입력종류 { 정수, 소수, 분초, 버튼 }   // 버튼 = 자판도 휠도 없이 − ＋ 만 (09-24)

/**
 * 버튼 한 칸 — 평소엔 [이름표/값], 누르면 [− 값 ＋] + 아래에 숫자 휠.
 *  · 누르자마자 자판이 뜬다. 무언가 치는 순간 옛 값이 지워진다 (치지 않으면 그대로)
 *  · 휠을 만지면 자판이 내려간다
 */
data class 숫자칸(
    val 키: String,
    val 라벨: String,
    val 값글: String,
    val 단위: String,
    val 종류: 입력종류 = 입력종류.정수,
    val 빼기: () -> Unit,
    val 더하기: () -> Unit,
    /** 직접 쳐 넣은 글 — 받는 쪽에서 숫자로 바꾼다 */
    val 넣기: (String) -> Unit,
    /** 휠에 늘어놓을 값들과 그 글 모양. 휠에서 고르면 넣기(글) 로 들어간다 */
    val 휠: List<Double> = emptyList(),
    val 휠글: (Double) -> String = { it.toString() },
    val 지금값: Double = 0.0,
    /** 칸 너비 — 닫혔을 때 · 열렸을 때 (09-24 시안에서 정한 값) */
    val 폭: Float = 1f,
    val 폭열림: Float = 1.69f,
)

/** 휠 값 목록 — 세트 1–50 · 무게 0–500(5kg) · 횟수 1–50 · 휴식 0–180초(10초) */
object 휠값 {
    val 세트 = (1..50).map { it.toDouble() }
    val 무게 = (0..100).map { it * 5.0 }
    val 횟수 = (1..50).map { it.toDouble() }
    val 휴식 = (0..18).map { it * 10.0 }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun 숫자버튼줄(
    칸들: List<숫자칸>,
    켠: String?,
    on고름: (String) -> Unit,
    modifier: Modifier = Modifier,
    /** 줄 오른쪽 끝에 붙일 것 (휴지통 등) */
    오른쪽: (@Composable () -> Unit)? = null,
) {
    val c = Local색.current
    val 끌어올림 = remember { BringIntoViewRequester() }
    val 켠칸 = 칸들.firstOrNull { it.키 == 켠 }
    val 이줄 = 켠칸 != null   // 이 줄에 실제로 열린 칸이 있을 때만 움직인다 (09-24: 모든 세트줄이 함께 끌려 올라왔다)
    LaunchedEffect(켠칸?.키) { if (이줄) { delay(350); 끌어올림.bringIntoView() } }

    // ── 닫기 (09-24 메모) — 빈 곳 누르기 · 뒤로가기 = '지금 값 그대로' 확정하고 닫는다 ──
    //  · − ＋ 로 바꾼 값은 누른 순간 이미 확정된 것이다 (되돌리지 않는다)
    //  · 자판으로 친 글은 '완료'를 누르지 않아도, 칸이 닫히며 초점이 빠질 때 들어간다
    //  · 휠은 숫자를 직접 눌러야만 들어간다 (그대로)
    val 고름최신 by rememberUpdatedState(on고름)
    fun 닫기() { 켠?.let { 고름최신(it) } }   // 같은 키를 다시 부르면 닫힌다
    val 닫기최신 by rememberUpdatedState({ 닫기() })
    BackHandler(enabled = 이줄) { 닫기최신() }
    DisposableEffect(켠칸?.키) {
        val f = { 닫기최신() }
        if (이줄) 입력중.취소 = f
        onDispose { if (입력중.취소 === f) 입력중.취소 = null }
    }
    // 테두리를 두르지 않는다 — 줄이 화면 기준선에 그대로 맞도록 (명세 1-2-1)
    Column(modifier.fillMaxWidth().bringIntoViewRequester(끌어올림)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
            칸들.forEach { k ->
                if (k.키 == 켠) {
                    Row(
                        Modifier
                            .weight(k.폭열림)
                            .height(높이.보통)
                            .clip(RoundedCornerShape(모서리.작게))
                            .background(c.면)
                            .border(1.5.dp, c.강조, RoundedCornerShape(모서리.작게)),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // − ＋ 가 빈자리를 나눠 갖는다 — 누를 수 있는 범위를 넓게 (09-24 시안)
                        Box(Modifier.weight(1f).fillMaxHeight().눌림 { 발자취.적기("${k.라벨} −"); k.빼기() }, contentAlignment = Alignment.Center) {
                            Icon(아이콘.빼기, "${k.라벨} 빼기", Modifier.size(18.dp), tint = c.강조)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            글(k.라벨, 크기값 = 크기.아주작게, 색 = c.옅음)
                            when (k.종류) {
                                입력종류.분초 -> 분초입력(k.키, k.값글, k.넣기) { on고름(k.키) }
                                입력종류.버튼 -> 글(k.값글, 크기값 = 크기.본문, 굵기 = FontWeight.Bold)
                                else -> 숫자입력(k.키, k.값글, k.종류, k.넣기) { on고름(k.키) }
                            }
                        }
                        Box(Modifier.weight(1f).fillMaxHeight().눌림 { 발자취.적기("${k.라벨} ＋"); k.더하기() }, contentAlignment = Alignment.Center) {
                            Icon(아이콘.더하기, "${k.라벨} 더하기", Modifier.size(18.dp), tint = c.강조)
                        }
                    }
                } else {
                    Column(
                        Modifier
                            .weight(k.폭)
                            .height(높이.보통)
                            .clip(RoundedCornerShape(모서리.작게))
                            .background(c.면2)
                            .눌림 { 발자취.적기("${k.라벨} 칸 누름"); on고름(k.키) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        글(k.라벨, 크기값 = 크기.아주작게, 색 = c.옅음)
                        글(k.값글 + k.단위, 크기값 = 크기.버튼, 굵기 = FontWeight.Bold)
                    }
                }
            }
            오른쪽?.invoke()
        }
        // 휠은 누른 칸 바로 아래, 그 칸 폭으로 (09-21 메모). '버튼' 칸(세트)은 휠이 없다
        if (켠칸 != null && 켠칸.휠.isNotEmpty() && 켠칸.종류 != 입력종류.버튼) {
            val 초점 = LocalFocusManager.current
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                칸들.forEach { k ->
                    if (k.키 == 켠) Box(Modifier.weight(k.폭열림)) {
                        key(k.키) {
                            // 휠: 돌려도 값은 그대로. 숫자를 눌러야 들어가고, 들어가면 칸이 닫힌다 (09-22 메모)
                            숫자휠(k.휠, k.지금값, k.휠글, 손댐 = { 초점.clearFocus() }) { v -> 발자취.적기("${k.라벨} 휠 ${k.휠글(v)}"); k.넣기(k.휠글(v)); 닫기() }
                        }
                    } else Box(Modifier.weight(k.폭))
                }
                if (오른쪽 != null) Box(Modifier.width(14.dp))
            }
        }
    }
    // 입력하는 동안은 아래 탭을 숨긴다 (09-21 메모)
    if (이줄) DisposableEffect(Unit) { 입력중.수++; onDispose { 입력중.수-- } }
}

/**
 * 발자취 — 방금 한 동작 20가지를 담아 둔다 (09-24 메모).
 * 메모를 적으면 **직전 10가지가 메모에 함께 저장**돼, 무엇을 하다 적었는지 알 수 있다.
 * 폰 안에만 있고, 메모에 붙은 것만 남는다. 자판처럼 이어지는 동작은 한 번으로 친다.
 */
object 발자취 {
    private val 것 = ArrayDeque<String>()
    fun 적기(글: String) {
        if (것.lastOrNull()?.substringAfter(' ') == 글) return   // 같은 동작이 잇따르면 한 번으로
        val t = java.time.LocalTime.now()
        것.addLast("%02d:%02d:%02d %s".format(t.hour, t.minute, t.second, 글))
        while (것.size > 20) 것.removeFirst()
    }
    fun 최근(n: Int = 10): List<String> = 것.toList().takeLast(n)
}

/** 지금 숫자를 고치는 칸이 몇 개 열려 있나 — 0 이 아니면 아래 탭을 숨긴다 */
object 입력중 {
    var 수 by mutableIntStateOf(0)
    /** 지금 열린 숫자칸을 취소하는 길 — 화면의 빈 곳을 누르면 App 이 이것을 부른다 */
    var 취소 by mutableStateOf<(() -> Unit)?>(null)
    /** 자판 '완료'로 닫았는지 — 자판이 내려가도 취소로 보지 않게 */
    var 완료누름 = false
}

/**
 * 직접 쳐 넣는 숫자.
 *  · 열리면 바로 초점 → 자판. 처음엔 글 전체가 선택돼 있어서, 무언가 치는 순간 옛 값이 지워진다
 *  · 아무것도 안 치고 닫으면 옛 값 그대로
 *  · 분초(휴식): 숫자 하나를 치면 '1:00' 이 되고 커서는 ':' 뒤 → 이어 치면 초가 채워진다
 */
@Composable
private fun 숫자입력(키: String, 값글: String, 종류: 입력종류, 넣기: (String) -> Unit, 닫기: () -> Unit) {
    val c = Local색.current
    val 초점 = LocalFocusManager.current
    val 요청 = remember { FocusRequester() }
    val 자판 = LocalSoftwareKeyboardController.current
    var tv by remember(키, 값글) { mutableStateOf(TextFieldValue(값글, TextRange(0, 값글.length))) }
    var 만졌나 by remember(키, 값글) { mutableStateOf(false) }
    var 분 by remember(키, 값글) { mutableStateOf("") }
    var 초 by remember(키, 값글) { mutableStateOf("") }
    LaunchedEffect(키) { try { 요청.requestFocus(); 자판?.show() } catch (_: Exception) { } }

    fun 분초모양() {
        val 글 = if (분.isEmpty()) "" else "$분:" + 초.padEnd(2, '0')
        tv = TextFieldValue(글, TextRange(if (분.isEmpty()) 0 else 2 + 초.length))
    }

    BasicTextField(
        value = tv,
        onValueChange = { 새 ->
            if (새.text == tv.text) { tv = 새; return@BasicTextField }   // 커서만 옮김
            만졌나 = true
            when (종류) {
                입력종류.분초 -> {
                    val 바꿈범위 = tv.selection.length
                    val 넣음 = 새.text.length > tv.text.length ||
                        (바꿈범위 > 0 && 새.text.length >= tv.text.length - 바꿈범위 + 1)
                    val 글자 = if (넣음) 새.text.getOrNull(새.selection.start - 1) else null
                    if (바꿈범위 > 0 && 바꿈범위 == tv.text.length) { 분 = ""; 초 = "" }   // 처음 — 옛 값 지우기
                    when {
                        글자 != null && 글자.isDigit() -> if (분.isEmpty()) 분 = 글자.toString() else if (초.length < 2) 초 += 글자
                        글자 != null -> {}
                        초.isNotEmpty() -> 초 = 초.dropLast(1)
                        else -> 분 = ""
                    }
                    분초모양()
                }
                else -> {
                    val 걸러 = 새.text.filter { it.isDigit() || (종류 == 입력종류.소수 && (it == '.' || it == ',')) }
                    tv = 새.copy(text = 걸러, selection = TextRange(minOf(새.selection.start, 걸러.length)))
                }
            }
        },
        singleLine = true,
        textStyle = TextStyle(fontSize = 크기.본문, fontWeight = FontWeight.Bold, color = c.글, textAlign = TextAlign.Center),
        cursorBrush = SolidColor(c.강조),
        keyboardOptions = KeyboardOptions(
            keyboardType = if (종류 == 입력종류.소수) KeyboardType.Decimal else KeyboardType.Number, imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = {
            입력중.완료누름 = true
            if (만졌나 && tv.text.isNotBlank()) { 발자취.적기("자판 입력 ${tv.text}"); 넣기(tv.text) }
            만졌나 = false; 초점.clearFocus(); 닫기()
        }),
        modifier = Modifier
            .widthIn(max = 64.dp)
            .focusRequester(요청)
            .onFocusChanged { if (!it.isFocused && 만졌나) { if (tv.text.isNotBlank()) { 발자취.적기("자판 입력 ${tv.text}"); 넣기(tv.text) }; 만졌나 = false } },
    )
}

/**
 * 숫자 휠 (09-22 메모로 바꿈)
 *  · 돌려도 값은 바뀌지 않는다. 숫자를 한 번 눌러야 그 값이 들어간다
 *  · 지금 값에 색이 칠해져 있다. 위아래 끝에 빈칸이 없다
 *  · 휠을 끄는 동안 바깥 화면은 움직이지 않는다
 *  · 누름은 손을 댄 자리로 판단한다 — 자판이 내려가며 칸이 움직여도 놓치지 않게
 */
@Composable
private fun 숫자휠(값들: List<Double>, 지금: Double, 글로: (Double) -> String, 손댐: () -> Unit, 고름: (Double) -> Unit) {
    val c = Local색.current
    val 칸 = 34.dp
    val 보일수 = minOf(5, 값들.size)
    val 가까운 = { v: Double -> 값들.indices.minByOrNull { abs(값들[it] - v) } ?: 0 }
    val 고른 = 가까운(지금)
    val 목록 = rememberLazyListState(maxOf(0, minOf(고른 - 보일수 / 2, 값들.size - 보일수)))
    val 고름최신 by rememberUpdatedState(고름)
    val 손댐최신 by rememberUpdatedState(손댐)
    // 휠이 끝에 닿아도 바깥 화면으로 스크롤을 넘기지 않는다
    val 가둠 = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource) = Offset(0f, available.y)
            override suspend fun onPostFling(consumed: Velocity, available: Velocity) = Velocity(0f, available.y)
        }
    }
    // − ＋ 나 직접 입력으로 값이 바뀌면 휠도 그 값이 보이게
    LaunchedEffect(지금) {
        val n = 가까운(지금)
        val 보이는 = 목록.layoutInfo.visibleItemsInfo.map { it.index }
        if (n !in 보이는 && !목록.isScrollInProgress) 목록.scrollToItem(maxOf(0, minOf(n - 보일수 / 2, 값들.size - 보일수)))
    }

    LazyColumn(
        state = 목록,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .height(칸 * 보일수)
            .clip(RoundedCornerShape(모서리.작게))
            .background(c.면2)
            .nestedScroll(가둠)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val 처음 = awaitFirstDown(requireUnconsumed = false)
                    val 돌던중 = 목록.isScrollInProgress          // 돌아가던 휠을 멈추는 손은 고름이 아니다
                    // 손을 댄 순간 그 자리에 있던 값을 기억해 둔다
                    val 그값 = 목록.layoutInfo.visibleItemsInfo
                        .firstOrNull { 처음.position.y >= it.offset && 처음.position.y < it.offset + it.size }?.index
                    var 움직임 = false
                    while (true) {
                        val e = awaitPointerEvent()
                        val ch = e.changes.firstOrNull { it.id == 처음.id } ?: break
                        if (!움직임 && (ch.position - 처음.position).getDistance() > viewConfiguration.touchSlop) { 움직임 = true; 손댐최신() }
                        if (!ch.pressed) {
                            if (!움직임 && !돌던중 && 그값 != null) { ch.consume(); 손댐최신(); 고름최신(값들[그값]) }
                            break
                        }
                    }
                }
            },
    ) {
        items(값들.size) { n ->
            val 켬 = n == 고른
            Box(
                Modifier.fillMaxWidth().height(칸).background(if (켬) c.강조옅음 else Color.Transparent),
                contentAlignment = Alignment.Center,
            ) {
                글(글로(값들[n]), 크기값 = if (켬) 크기.크게 else 크기.버튼, 색 = if (켬) c.강조 else c.흐림,
                    굵기 = if (켬) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

/**
 * 휴식 입력 — [분] : [초] 두 칸 (09-21 메모).
 *  · 열리면 초 칸에 커서. 초 칸에 치면 그게 전체 초가 된다 → 90 = 1:30, 60 = 1:00
 *  · 분 칸을 누르면 분으로 친다 → 60 = 60분 (초는 그대로)
 *  · 아무것도 안 치면 원래 값
 */
@Composable
private fun 분초입력(키: String, 값글: String, 넣기: (String) -> Unit, 닫기: () -> Unit) {
    val c = Local색.current
    val 초점 = LocalFocusManager.current
    val 자판 = LocalSoftwareKeyboardController.current
    val 원래 = 초읽기(값글) ?: 0
    val 분0 = 원래 / 60; val 초0 = 원래 % 60
    val 분요청 = remember { FocusRequester() }
    val 초요청 = remember { FocusRequester() }
    var 분 by remember(키, 값글) { mutableStateOf(TextFieldValue("$분0", TextRange(0, "$분0".length))) }
    var 초 by remember(키, 값글) { val t = 초0.toString().padStart(2, '0'); mutableStateOf(TextFieldValue(t, TextRange(0, t.length))) }
    var 분만짐 by remember(키, 값글) { mutableStateOf(false) }
    var 초만짐 by remember(키, 값글) { mutableStateOf(false) }
    LaunchedEffect(키) { try { 초요청.requestFocus(); 자판?.show() } catch (_: Exception) { } }

    fun 합(): String {
        val m = if (분만짐) 분.text.toIntOrNull() ?: 0 else if (초만짐) 0 else 분0
        val x = if (초만짐) 초.text.toIntOrNull() ?: 0 else 초0
        return 분초((m * 60 + x).coerceIn(0, 휴식최대))
    }
    fun 넣고치움() { if (분만짐 || 초만짐) { 발자취.적기("휴식 자판 입력 ${합()}"); 넣기(합()) }; 분만짐 = false; 초만짐 = false }

    val 모양 = TextStyle(fontSize = 크기.본문, fontWeight = FontWeight.Bold, color = c.글, textAlign = TextAlign.Center)
    val 자판설정 = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
    val 끝 = KeyboardActions(onDone = { 입력중.완료누름 = true; 넣고치움(); 초점.clearFocus(); 닫기() })
    Row(verticalAlignment = Alignment.CenterVertically) {
        BasicTextField(
            value = 분, onValueChange = { v -> if (v.text != 분.text) 분만짐 = true; 분 = v.copy(text = v.text.filter { it.isDigit() }.take(2)) },
            singleLine = true, textStyle = 모양, cursorBrush = SolidColor(c.강조), keyboardOptions = 자판설정, keyboardActions = 끝,
            modifier = Modifier.width(24.dp).focusRequester(분요청)
                .onFocusChanged { if (it.isFocused) 분 = 분.copy(selection = TextRange(0, 분.text.length)) else if (분만짐) 넣고치움() },
        )
        Text(":", style = 모양)
        BasicTextField(
            value = 초, onValueChange = { v ->
                if (v.text != 초.text) {
                    // 초 칸에 치기 시작하면 분 칸을 비운다 — 60 을 치는 동안 1:60 이 보이지 않게 (08 시안)
                    if (!초만짐 && !분만짐) 분 = TextFieldValue("")
                    초만짐 = true
                }
                초 = v.copy(text = v.text.filter { it.isDigit() }.take(4))
            },
            singleLine = true, textStyle = 모양, cursorBrush = SolidColor(c.강조), keyboardOptions = 자판설정, keyboardActions = 끝,
            modifier = Modifier.width(34.dp).focusRequester(초요청)
                .onFocusChanged { if (!it.isFocused && 초만짐) 넣고치움() },
        )
    }
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
                .padding(horizontal = 간격.넓게, vertical = 16.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                제목글(제목, Modifier.weight(1f), 크기값 = 크기.크게)
                아이콘버튼(아이콘.닫기, "닫기", onClose, 크기칸 = 높이.낮게)
            }
            Column(Modifier.padding(top = 12.dp).verticalScroll(rememberScrollState()), content = content)
        }
    }
}

/**
 * 고르는 물음 — 화면 **가운데**에, 좌우 여백을 두고 (09-24 메모).
 * 팝업은 '고르는 일'에만 쓴다 (1-1).
 */
@Composable
fun 물음창(제목: String, 설명: String? = null, 예: String, on예: () -> Unit, on아니오: () -> Unit) {
    val c = Local색.current
    BackHandler(onBack = on아니오)
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)).눌림(on아니오),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.fillMaxWidth(0.62f).clip(RoundedCornerShape(모서리.보통)).background(c.면).눌림 { }.padding(간격.넓게),
        ) {
            제목글(제목, 크기값 = 크기.크게)
            if (설명 != null) 글(설명, Modifier.padding(top = 4.dp), 크기값 = 크기.버튼, 색 = c.흐림, 줄 = 2)
            Row(Modifier.padding(top = 간격.넓게), horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                버튼("아니오", on아니오, Modifier.weight(1f), 작게 = true)
                버튼(예, on예, Modifier.weight(1f), 작게 = true, 주요 = true)
            }
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
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(모서리.작게))
            .background(if (어둡게) c.글 else c.면)
            .border(1.dp, if (어둡게) c.글 else c.휴식, RoundedCornerShape(모서리.작게))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        글(글자, Modifier.weight(1f), 크기값 = 크기.버튼, 색 = if (어둡게) c.면 else c.휴식)
        Box(Modifier.width(12.dp))
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
            if (곁 != null) { Box(Modifier.width(8.dp)); 글(곁, 크기값 = 크기.작게, 색 = c.옅음) }
        }
        if (오른쪽 != null) Icon(오른쪽, null, Modifier.size(18.dp), tint = if (흐림) c.옅음 else c.강조)
    }
    구분선()
}
