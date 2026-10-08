@file:OptIn(ExperimentalFoundationApi::class)

package com.slayde.hasenheide.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.graphicsLayer
import com.slayde.hasenheide.ui.theme.움직임
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
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
import com.slayde.hasenheide.ui.theme.막대치수
import com.slayde.hasenheide.ui.theme.선굵기
import com.slayde.hasenheide.ui.theme.부품치수
import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.detectVerticalDragGestures

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

// ─────────────── 움직임 부품 (10-02 · 홍겸 님 "ui동작들에 전부 애니메이션") ───────────────

/** 누르는 동안 살짝 작아진다 (0.96) — [눌림손] 에 같은 손을 넘긴다 */
@Composable
fun 눌림배율(손: MutableInteractionSource): State<Float> {
    val 눌림중 by 손.collectIsPressedAsState()
    return animateFloatAsState(if (눌림중) 움직임.눌림배율 else 1f, tween(움직임.눌림), label = "눌림")
}

/** 배율을 그리기에만 쓴다 — 자리 · 크기는 그대로 (값은 그릴 때 읽는다) */
fun Modifier.배율(s: State<Float>): Modifier = this.graphicsLayer { scaleX = s.value; scaleY = s.value }

/** 눌림과 같다 — 다만 손(interactionSource)을 밖에서 받아 [눌림배율] 과 짝짓는다 */
fun Modifier.눌림손(손: MutableInteractionSource, onClick: () -> Unit): Modifier = this.clickable(
    interactionSource = 손, indication = null, onClick = onClick,
)

/** 색이 바뀔 때 0.2초 동안 옮겨 간다 */
@Composable
fun 색움직(목표: Color, 이름: String = "색"): Color = animateColorAsState(목표, tween(움직임.색), label = 이름).value

@Composable
fun 카드(modifier: Modifier = Modifier, 안쪽: Dp = 14.dp, content: @Composable ColumnScope.() -> Unit) {
    val c = Local색.current
    Column(
        modifier
            .fillMaxWidth()
            // 10-02: 안의 것이 펼쳐지고 접힐 때 카드 높이가 부드럽게 따라간다
            .animateContentSize(tween(움직임.펼침))
            .clip(RoundedCornerShape(모서리.보통))
            .background(c.면)
            // 09-27: 큰 박스 = 굵은 중심색 테두리 (안쪽 박스는 가는 속선)
            .border(2.dp, c.강조, RoundedCornerShape(모서리.보통))
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
    // 10-02: 누르면 살짝 작아지고, 켜고 끌 때 색이 옮겨 간다
    val 손 = remember { MutableInteractionSource() }
    val 배 = 눌림배율(손)
    val 바탕 = 색움직(when { 켬 -> c.강조; 칠함 -> c.면2; else -> c.면2.copy(alpha = 0f) }, "아이콘버튼")
    val 글색 = 색움직(색 ?: if (켬) c.강조글 else c.흐림, "아이콘버튼글")
    Box(
        modifier
            .size(크기칸)
            .배율(배)
            .clip(RoundedCornerShape(모서리.아주작게))
            .background(바탕)
            .then(if (쓸수있음) Modifier.눌림손(손, onClick) else Modifier),
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
    /** 10-08 홍겸 님: 글이 넘치면 글자를 줄인다 (맞춤글 — 캘린더 아래 단추 줄) */
    줄임: Boolean = false,
) {
    val c = Local색.current
    val 손 = remember { MutableInteractionSource() }
    val 배 = 눌림배율(손)   // 10-02: 누르면 살짝 작아진다
    Row(
        modifier
            .height(if (낮게) 높이.낮게 else if (작게) 높이.보통 else 높이.높게)
            .배율(배)
            .clip(RoundedCornerShape(모서리.작게))
            .background(if (주요) c.강조 else c.면)
            .then(if (주요) Modifier else Modifier.border(1.dp, c.속선, RoundedCornerShape(모서리.작게)))
            .눌림손(손, onClick)
            .padding(horizontal = if (낮게) 8.dp else 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val 색 = 글색 ?: if (주요) c.강조글 else c.글
        if (그림 != null) {
            Icon(그림, null, Modifier.size(if (낮게) 14.dp else 16.dp), tint = 색)
            Box(Modifier.width(if (낮게) 4.dp else 6.dp))
        }
        val 글크기 = if (낮게) 크기.조금작게 else if (작게) 크기.버튼 else 크기.본문
        if (줄임) 맞춤글(text, 최대 = 글크기, 최소 = 크기.작게, 색 = 색, 굵기 = FontWeight.Bold)
        else 글(text, 크기값 = 글크기, 색 = 색, 굵기 = FontWeight.Bold)
    }
}

/** 칩 한 줄 — 넘치면 옆으로 밀어서 꺼낸다 (1-1). 넘칠 때만 오른쪽 끝이 흐려진다 (D2-8 · 10-02) */
@Composable
fun 칩줄(
    목록: List<String>, 선택: String?, onSelect: (String) -> Unit, modifier: Modifier = Modifier,
    /** 10-05: 밖에서 넘김을 쥘 때 (양끝 ‹ › 단추 — Parts.kt 화살칩줄). null 이면 전처럼 안에서 */
    밖넘김: androidx.compose.foundation.ScrollState? = null,
) {
    val c = Local색.current
    val 안넘김 = rememberScrollState()
    val 넘김 = 밖넘김 ?: 안넘김
    Row(
        modifier.fillMaxWidth().오른끝흐림(넘김).horizontalScroll(넘김),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        목록.forEach { p -> key(p) {
            val 켬 = p == 선택
            // 10-02: 누르면 살짝 작아지고, 고르면 색이 옮겨 간다 (켠 칩의 테두리는 바탕과 같은 색이라 보이지 않는다 — 전과 같은 모양)
            val 손 = remember { MutableInteractionSource() }
            val 배 = 눌림배율(손)
            val 바탕 = 색움직(if (켬) c.강조 else c.강조.copy(alpha = 0f), "칩")
            val 테두리 = 색움직(if (켬) c.강조 else c.속선, "칩테두리")
            Box(
                Modifier
                    .height(높이.낮게)
                    .배율(배)
                    .clip(CircleShape)
                    .background(바탕)
                    .border(1.dp, 테두리, CircleShape)   // 09-27: 안 고른 칩은 칠하지 않고 속선만
                    .눌림손(손) { onSelect(p) }
                    .padding(horizontal = 13.dp),
                contentAlignment = Alignment.Center,
            ) { 글(p, 크기값 = 크기.버튼, 색 = 색움직(if (켬) c.강조글 else c.흐림, "칩글"), 굵기 = FontWeight.Medium) }
        } }
    }
}

/** 켜고 끄기 */
@Composable
fun 스위치(켜짐: Boolean, onChange: (Boolean) -> Unit) {
    val c = Local색.current
    val 폭 = 46.dp; val 안 = 4.dp; val 손잡이 = 22.dp
    // 10-02: 손잡이가 미끄러지고 색이 옮겨 간다 (전에는 한 번에 건너뛰었다). 자리 · 크기는 그대로
    val 자리 by animateDpAsState(if (켜짐) 폭 - 안 * 2 - 손잡이 else 0.dp, tween(움직임.스위치), label = "스위치")
    val 바탕 by animateColorAsState(if (켜짐) c.강조 else c.선, tween(움직임.스위치), label = "스위치바탕")
    val 손잡이색 by animateColorAsState(if (켜짐) c.강조글 else c.면, tween(움직임.스위치), label = "스위치손잡이")
    val 손 = remember { MutableInteractionSource() }
    Box(
        Modifier
            .width(폭).height(높이.아주낮게)
            .clip(CircleShape)
            .background(바탕)
            .눌림손(손) { onChange(!켜짐) }
            .padding(안),
        contentAlignment = Alignment.CenterStart,
    ) { Box(Modifier.offset(x = 자리).size(손잡이).clip(CircleShape).background(손잡이색)) }
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
            .border(1.dp, c.속선, RoundedCornerShape(모서리.작게))
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

/**
 * 쪽 겹침 (10-08 홍겸 님) — 화면 단계가 깊어질수록 왼쪽에 책장이 [겹] 장 겹쳐 보인다.
 * 루틴 → 루틴 상세(1) → 종목 넣기(2) → 새 종목(3) · 종목 탭 → 새 종목 · 카테고리(1).
 * 왼쪽을 [겹] × 겹폭 만큼 비우고, 그 자리에 뒤 장들의 왼쪽 끝을 (면2 바탕 · 속선 테두리) 그린다.
 * [모서리] = 그 화면/시트의 위 모서리 (화면 = 0 · 시트 = 16)
 */
fun Modifier.쪽겹(겹: Int, 모서리값: androidx.compose.ui.unit.Dp = 0.dp): Modifier {
    if (겹 <= 0) return this
    return composed {
        val c = Local색.current
        this.padding(start = 부품치수.겹폭 * 겹).drawBehind {
            val w = 부품치수.겹폭.toPx()
            val r = 모서리값.toPx()
            val 선 = 선굵기.보통.toPx()
            // 뒤 장은 비운 왼쪽 자리에만 그린다 — 몸통 뒤에 칠해지면 바탕 없는 화면이 회색이 된다
            clipRect(left = -w * 겹, top = 0f, right = 0f, bottom = size.height + r) { for (i in 겹 downTo 1) {
                val x = -w * i
                val 크기 = androidx.compose.ui.geometry.Size(size.width - x, size.height + r)
                val 둥 = androidx.compose.ui.geometry.CornerRadius(r, r)
                drawRoundRect(c.면2, androidx.compose.ui.geometry.Offset(x, 0f), 크기, 둥)
                drawRoundRect(c.속선, androidx.compose.ui.geometry.Offset(x + 선 / 2, 선 / 2), androidx.compose.ui.geometry.Size(크기.width - 선, 크기.height - 선), 둥,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(선))
            } }
        }
    }
}

/**
 * 아래에서 올라오는 판 — '고르는 일'에만 쓴다 (1-1: 팝업은 고르기뿐)
 *  · 10-05 (시안 v21 ⑥): 맨 위 가운데 손잡이 막대 36 × 4 (속선) — 모든 시트에 저절로 붙는다.
 *    손잡이 · 제목 줄을 잡고 아래로 80 넘게 끌어 놓으면 닫힌다(✕ 와 같다), 덜 끌면 제자리로
 *  · [위끝고정] = 시트 위끝을 화면 높이의 10% 지점에 고정 (부품치수.시트위끝 · 종목 넣기 · 새 종목 시트)
 *    + 머리 = 띠 ([머리띠] — 강조 바탕 · 강조글 · 오른쪽 [닫기] · U4-8 · 10-06 ⑨)
 */
@Composable
fun 시트(
    제목: String, onClose: () -> Unit, 위끝고정: Boolean = false,
    /** 10-06 v22 RT: 띠 머리 오른쪽을 ✕ 대신 이 글 단추로 (종목 넣기 = "확인" · 동작은 닫기 그대로). null = ✕ */
    닫기글: String? = null,
    /** 10-07: 위끝 고정 시트의 위끝 비율 (새 종목 시트 = 5%) */
    위끝: Float = 부품치수.시트위끝,
    /** 10-08: 화면 단계 — 1 이상이면 왼쪽에 [겹] 장만큼 책장처럼 겹쳐 그린다 ([쪽겹]) */
    겹: Int = 0,
    /** 10-08: 띠 아닌 시트의 제목 줄을 글 대신 이것으로 (달 고르기 ‹ 2026 ›). null = [제목] 글 */
    제목칸: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = Local색.current
    // 10-02: 나타날 때 아래에서 올라오고, ✕ · 바깥 · 뒤로가기로 닫으면 내려간 뒤에 닫힌다.
    //        (안에서 무언가를 골라 화면이 시트를 바로 치우는 경우는 내려가는 움직임 없이 사라진다)
    val 보임 = remember { MutableTransitionState(false).apply { targetState = true } }
    var 닫는중 by remember { mutableStateOf(false) }
    val 닫기최신 by rememberUpdatedState(onClose)
    fun 닫기() { if (!닫는중) { 닫는중 = true; 보임.targetState = false } }
    LaunchedEffect(보임.isIdle, 보임.currentState) { if (닫는중 && 보임.isIdle && !보임.currentState) 닫기최신() }
    BackHandler(onBack = { 닫기() })
    // 끌어내린 만큼 (px) — 그릴 때만 읽는다
    var 끌림 by remember { mutableStateOf(0f) }
    val 범위 = rememberCoroutineScope()
    val 닫을px = with(LocalDensity.current) { 부품치수.시트닫기.toPx() }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(visibleState = 보임, enter = fadeIn(tween(움직임.시트)), exit = fadeOut(tween(움직임.시트닫기)), label = "시트가림") {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)).눌림 { 닫기() })
        }
        AnimatedVisibility(
            visibleState = 보임,
            enter = slideInVertically(tween(움직임.시트, easing = 움직임.부드럽게)) { it },
            exit = slideOutVertically(tween(움직임.시트닫기)) { it },
            label = "시트",
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .then(if (위끝고정) Modifier.fillMaxHeight(1f - 위끝) else Modifier.heightIn(max = 620.dp))
                    .graphicsLayer { translationY = 끌림 }
                    .쪽겹(겹, 모서리.크게)
                    .clip(RoundedCornerShape(topStart = 모서리.크게, topEnd = 모서리.크게))
                    .background(c.면)
                    .눌림 { }
                    .navigationBarsPadding()
                    .imePadding()
                    // 띠 머리(위끝고정)는 판 양끝까지 닿는다 — 옆 여백은 속에만
                    .then(if (위끝고정) Modifier else Modifier.padding(horizontal = 간격.넓게))
                    .padding(bottom = 16.dp),
            ) {
                // 머리 — 손잡이 막대 + 제목 줄. 여기를 잡고 끌어내린다 (시트 안 스크롤과 겹치지 않는다)
                Column(
                    Modifier.fillMaxWidth().pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { ch, dy -> ch.consume(); 끌림 = (끌림 + dy).coerceAtLeast(0f) },
                            onDragEnd = {
                                if (끌림 > 닫을px) 닫기()
                                else 범위.launch { animate(끌림, 0f, animationSpec = tween(움직임.시트제자리)) { v, _ -> 끌림 = v } }
                            },
                            onDragCancel = { 범위.launch { animate(끌림, 0f, animationSpec = tween(움직임.시트제자리)) { v, _ -> 끌림 = v } } },
                        )
                    },
                ) {
                    if (위끝고정) {
                        // 10-06 ⑨ (U4-8): 머리 = 띠 — 캘린더 년월 띠와 같은 값(강조 · 강조글 · 40 · 18 굵게) · 오른쪽 [닫기].
                        //  손잡이 막대는 띠 안 맨 위(위끝에서 4)에 얹는다 (시안 `.시트 .머리>.시트손잡이`). 띠는 속 위에 붙어 있다
                        // 10-08 홍겸 님: 손잡이를 내린 만큼(띠위더) 띠 위를 늘리고 제목도 그만큼 내린다
                        Box(Modifier.fillMaxWidth().background(c.강조).padding(top = 부품치수.띠위더)) {
                            머리띠(제목, 오른쪽 = {
                                if (닫기글 != null) 띠칩(닫기글, { 닫기() })   // U4-8 띠 위 단추 = 캘린더 스탯 칩 모양
                                else 아이콘버튼(아이콘.닫기, "닫기", { 닫기() }, 칠함 = false, 색 = c.강조글, 크기칸 = 높이.낮게)
                            })
                            Box(
                                Modifier.align(Alignment.TopCenter).offset(y = -부품치수.띠위더).padding(top = 부품치수.손잡이위)
                                    .size(부품치수.손잡이폭, 부품치수.손잡이두께).clip(RoundedCornerShape(부품치수.손잡이모서리)).background(c.속선),
                            )
                        }
                    } else {
                        // 손잡이 막대 36 × 4 — 위끝에서 4. 막대 아래 8 을 더해 제목 줄은 전과 같은 자리(위 16)
                        Box(
                            Modifier.padding(top = 부품치수.손잡이위, bottom = 간격.좁게).align(Alignment.CenterHorizontally)
                                .size(부품치수.손잡이폭, 부품치수.손잡이두께).clip(RoundedCornerShape(부품치수.손잡이모서리)).background(c.속선),
                        )
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            if (제목칸 != null) Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, content = 제목칸)
                            else 제목글(제목, Modifier.weight(1f), 크기값 = 크기.크게)
                            아이콘버튼(아이콘.닫기, "닫기", { 닫기() }, 크기칸 = 높이.낮게)
                        }
                    }
                }
                Column(
                    Modifier.then(if (위끝고정) Modifier.weight(1f).padding(horizontal = 간격.넓게) else Modifier).padding(top = 12.dp).verticalScroll(rememberScrollState()),
                    content = content,
                )
            }
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
    // 10-02: 흐려지며 나타나고, '아니오' · 바깥 · 뒤로가기로 닫으면 흐려지며 사라진 뒤에 닫힌다
    val 보임 = remember { MutableTransitionState(false).apply { targetState = true } }
    var 닫는중 by remember { mutableStateOf(false) }
    val 아니오최신 by rememberUpdatedState(on아니오)
    fun 아니오() { if (!닫는중) { 닫는중 = true; 보임.targetState = false } }
    LaunchedEffect(보임.isIdle, 보임.currentState) { if (닫는중 && 보임.isIdle && !보임.currentState) 아니오최신() }
    BackHandler(onBack = { 아니오() })
    AnimatedVisibility(
        visibleState = 보임,
        enter = fadeIn(tween(움직임.물음)), exit = fadeOut(tween(움직임.물음)),
        label = "물음창",
    ) {
        Box(
            Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)).눌림 { 아니오() },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                Modifier.fillMaxWidth(0.62f).clip(RoundedCornerShape(모서리.보통)).background(c.면).눌림 { }.padding(간격.넓게),
            ) {
                제목글(제목, 크기값 = 크기.크게)
                if (설명 != null) 글(설명, Modifier.padding(top = 4.dp), 크기값 = 크기.버튼, 색 = c.흐림, 줄 = 2)
                Row(Modifier.padding(top = 간격.넓게), horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                    버튼("아니오", { 아니오() }, Modifier.weight(1f), 작게 = true)
                    버튼(예, on예, Modifier.weight(1f), 작게 = true, 주요 = true)
                }
            }
        }
    }
}

/** 화면 아래(탭 위)에 뜨는 띠 — 되돌리기 · 집기 안내가 같은 자리 · 같은 모양 */
@Composable
fun BoxScope.아래띠(글자: String, 버튼글: String, on버튼: () -> Unit, 어둡게: Boolean = true, 바깥: Modifier = Modifier) {
    val c = Local색.current
    // 10-02: 아래에서 미끄러져 올라온다
    val 보임 = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = 보임,
        modifier = Modifier.align(Alignment.BottomCenter),
        enter = slideInVertically(tween(움직임.띠, easing = 움직임.부드럽게)) { it } + fadeIn(tween(움직임.띠)),
        label = "아래띠",
    ) {
        Row(
            바깥
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
}

/** 목록 한 줄 (시트 안에서 고르는 것) */
@Composable
fun 고르기줄(이름: String, 곁: String? = null, 오른쪽: ImageVector? = 아이콘.오른쪽, 흐림: Boolean = false, onClick: () -> Unit) {
    val c = Local색.current
    val 손 = remember { MutableInteractionSource() }
    val 배 = 눌림배율(손)   // 10-02: 누르면 살짝 작아진다
    Row(
        Modifier.fillMaxWidth().배율(배).눌림손(손, onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
            글(이름, Modifier.weight(1f, fill = false), 색 = 색움직(if (흐림) c.옅음 else c.글, "고르기줄"))
            // 10-01: 곁 글(예상 시간 등)은 말줄임 대신 줄여서 다 보인다
            if (곁 != null) { Box(Modifier.width(8.dp)); 맞춤글(곁, Modifier.weight(1f, fill = false), 색 = c.옅음) }
        }
        if (오른쪽 != null) Icon(오른쪽, null, Modifier.size(18.dp), tint = 색움직(if (흐림) c.옅음 else c.강조, "고르기줄그림"))
    }
    구분선()
}

// ─────────────── 화면 번호 (09-26 · 시험 기간용) ───────────────

/** 번호를 보일까 — 설정 → '화면 번호 보기'. 앱(App.kt)에서 내려준다 */
val Local번호 = androidx.compose.runtime.compositionLocalOf { false }

/**
 * 칸의 왼쪽 위에 작은 파란 번호 (운4 · 캘2 …) — "운4 좀 고쳐줘" 처럼 말할 수 있게 (09-26 홍겸 님: 시안 A, 파랑, 25% 작게).
 * 번호 목록은 문서/동작방식.md 부록 · 시안 https://claude.ai/artifact/9gY53qR5CN92q6cVYgWocn
 * 칸의 모양이나 자리를 바꾸지 않는다 — 다 그린 뒤 위에 덧그리기만 한다
 */
fun Modifier.번호(표: String): Modifier = this.composed {
    if (!Local번호.current) return@composed Modifier
    val 측정 = androidx.compose.ui.text.rememberTextMeasurer()
    Modifier.drawWithContent {
        drawContent()
        val 글 = 측정.measure(
            표,
            androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.SemiBold),
        )
        val h = 13.5.dp.toPx()
        val w = kotlin.math.max(h, 글.size.width + 6.dp.toPx())
        drawRoundRect(번호색, topLeft = Offset.Zero, size = Size(w, h), cornerRadius = CornerRadius(h / 2))
        drawText(글, topLeft = Offset((w - 글.size.width) / 2, (h - 글.size.height) / 2))
    }
}
private val 번호색 = Color(0xFF1E6FD9)


/**
 * 한 줄에 다 들어가게 — 넘치면 **자간을 좁히고, 그래도 넘치면 글자를 줄인다.** 말줄임(…)은 쓰지 않는다
 * (10-01 홍겸 님: "예상시간 뒤에 ... 으로 짜르지말고 차라리 글자크기를 줄여. 자간을 줄이든지")
 */
@Composable
fun 맞춤글(text: String, modifier: Modifier = Modifier, 최대: TextUnit = 크기.작게, 최소: TextUnit = 9.sp, 색: Color = Local색.current.글, 굵기: FontWeight = FontWeight.Normal) {
    var 지금크기 by remember(text, 최대) { mutableStateOf(최대) }
    var 좁힘 by remember(text, 최대) { mutableStateOf(false) }
    Text(
        text, modifier,
        style = 글꼴.보통(지금크기, 굵기).copy(letterSpacing = if (좁힘) (-0.05).em else (-0.01).em),
        color = 색, maxLines = 1, softWrap = false,
        onTextLayout = { r ->
            if (r.hasVisualOverflow) {
                if (!좁힘) 좁힘 = true
                else if (지금크기.value > 최소.value) 지금크기 = (지금크기.value - 0.5f).sp
            }
        },
    )
}

/**
 * 옆으로 미는 줄의 오른쪽 끝을 흐리게 — **넘칠 때만**, 끝까지 밀면 사라진다 (동작방식 D2-8 · 02 명세 1-1 · 10-02).
 * horizontalScroll **앞**에 둔다 (보이는 칸 기준으로 그린다).
 * 캘린더 '기록 갱신' 줄과 같은 방법 — 내용을 따로 한 겹에 그린 뒤 끝 [폭] 만큼 서서히 지운다
 */
fun Modifier.오른끝흐림(넘김: androidx.compose.foundation.ScrollState, 폭: Dp = 16.dp): Modifier = drawWithContent {
    if (넘김.value >= 넘김.maxValue) { drawContent(); return@drawWithContent }
    val 판 = drawContext.canvas
    판.saveLayer(androidx.compose.ui.geometry.Rect(Offset.Zero, size), androidx.compose.ui.graphics.Paint())
    drawContent()
    drawRect(
        androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color.Black, Color.Transparent), startX = size.width - 폭.toPx(), endX = size.width),
        blendMode = androidx.compose.ui.graphics.BlendMode.DstIn,
    )
    판.restore()
}

/** 줄 왼쪽에 세로 띠 하나 — 줄의 종류를 색으로 알린다 (운동 중 워밍업 세트 · 10-02). 줄의 안쪽 여백 자리에 그린다 */
fun Modifier.왼띠(색: Color, 폭: Dp = 3.dp): Modifier = drawWithContent {
    drawRect(색, size = Size(폭.toPx(), size.height))
    drawContent()
}

// ─────────────── 스탯 · 업적 (10-02) ───────────────

/**
 * 진행 막대 — 앱에서 하나로 (11 지침 U3-8): 높이 8 · 끝 둥글게 · 바탕 면2 · 채움 [색](기본 강조).
 * 값이 바뀌면 0.7초 동안 따라간다 (움직임.게이지)
 */
@Composable
fun 진행막대(비율: Float, modifier: Modifier = Modifier, 색: Color = Local색.current.강조) {
    val c = Local색.current
    val 보일 by animateFloatAsState(비율.coerceIn(0f, 1f), tween(움직임.게이지, easing = 움직임.부드럽게), label = "진행막대")
    Box(modifier.height(막대치수.높이).clip(CircleShape).background(c.면2)) {
        if (보일 > 0f) Box(Modifier.fillMaxWidth(보일).fillMaxHeight().clip(CircleShape).background(색))
    }
}

/**
 * 중심색 띠(캘린더 년월 띠) 위의 작은 칩 — 테두리만, 글자는 강조글 (U1-2).
 * 캘린더의 '대표 칭호 · 스탯' 단추 (10-02). 길면 이름이라 … 로 자른다 (U2-7)
 */
@Composable
fun 띠칩(글자: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Local색.current
    val 손 = remember { MutableInteractionSource() }
    val 배 = 눌림배율(손)
    Box(
        modifier
            .height(높이.아주낮게)
            .배율(배)
            .clip(CircleShape)
            .border(선굵기.보통, c.강조글, CircleShape)
            .눌림손(손, onClick)
            .padding(horizontal = 간격.좁게),
        contentAlignment = Alignment.Center,
    ) { 글(글자, 크기값 = 크기.조금작게, 색 = c.강조글, 굵기 = FontWeight.Bold) }
}
