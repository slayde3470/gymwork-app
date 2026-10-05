package com.slayde.hasenheide.ui

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.slayde.hasenheide.data.날기록
import com.slayde.hasenheide.data.날더하기
import com.slayde.hasenheide.data.날짜만
import com.slayde.hasenheide.data.다음으로
import com.slayde.hasenheide.data.다음회
import com.slayde.hasenheide.data.덜한가
import com.slayde.hasenheide.data.무게글
import com.slayde.hasenheide.data.볼륨
import com.slayde.hasenheide.data.분초
import com.slayde.hasenheide.data.세기더함
import com.slayde.hasenheide.data.세기이름
import com.slayde.hasenheide.data.세션종목
import com.slayde.hasenheide.data.세션줄
import com.slayde.hasenheide.data.세트
import com.slayde.hasenheide.data.세트값
import com.slayde.hasenheide.data.세트삭제
import com.slayde.hasenheide.data.세트종류
import com.slayde.hasenheide.data.세트추가
import com.slayde.hasenheide.data.세트휴식
import com.slayde.hasenheide.data.식구
import com.slayde.hasenheide.data.앱데이터
import com.slayde.hasenheide.data.열쇠
import com.slayde.hasenheide.data.운동세션
import com.slayde.hasenheide.data.일RM
import com.slayde.hasenheide.data.자리로
import com.slayde.hasenheide.data.종목넣기
import com.slayde.hasenheide.data.종목되돌리기
import com.slayde.hasenheide.data.종목빼기
import com.slayde.hasenheide.data.종목옮기기
import com.slayde.hasenheide.data.지난주
import com.slayde.hasenheide.data.값고치기
import com.slayde.hasenheide.data.같은이름번호
import com.slayde.hasenheide.data.기록세트
import com.slayde.hasenheide.data.끝냄
import com.slayde.hasenheide.data.남은초
import com.slayde.hasenheide.data.넣은것빼기
import com.slayde.hasenheide.data.체크
import com.slayde.hasenheide.data.총칸
import com.slayde.hasenheide.data.찬것
import com.slayde.hasenheide.data.칸
import com.slayde.hasenheide.data.콤마
import com.slayde.hasenheide.data.플랜표
import com.slayde.hasenheide.data.휴식고치기
import com.slayde.hasenheide.data.휴식시작
import com.slayde.hasenheide.data.휴식자리
import com.slayde.hasenheide.data.휴식보임
import com.slayde.hasenheide.data.휴식중
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.글꼴
import com.slayde.hasenheide.ui.theme.높이
import com.slayde.hasenheide.ui.theme.막대치수
import com.slayde.hasenheide.ui.theme.모서리
import com.slayde.hasenheide.ui.theme.부품치수
import com.slayde.hasenheide.ui.theme.선굵기
import com.slayde.hasenheide.ui.theme.움직임
import com.slayde.hasenheide.ui.theme.크기
import com.slayde.hasenheide.ui.theme.휴식칸값
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 운동 실행 화면 — 시안 v21 `운동화면()` 을 옮겼다 (10-05 앱 옮기기 2단계 W).
 *
 *  · 맨 위 띠 두 줄(v7 · v8): ‹ 나가기 · 보는 종목 이름(잘리지 않게 · [플랜]/번호 딱지 · 'N회차 · N주 진행 중') · 시계 /
 *    지표 한 줄(1RM · [1주 ▲] · [최고 ▲] · 달성 · 볼륨 — 좁으면 목표를 감춘다)
 *  · 그림 칸(근육 · 사진) — 예전 그대로 (MuscleView `운동그림칸`)
 *  · 세트 줄(v8 · v9 · v10 · v17 · v18): 번호 동그라미 = 체크 · kg · 회 · 휴식(− ＋ 15초 · 0:15~5:00) · 휴지통.
 *    쉬는 줄은 세 칸 자리에 게이지 하나(남은 시간 · 문구 · 노란 점) · 끝난 줄은 가운데 띠 · 지금 줄은 1초 점멸
 *  · 아래 진행 상자(v14 · v16 · v21): 루틴 이름 · 종목 · 세트 · 볼륨 · 막대 / 종목 칸 줄(꾹 끌기 순서 · 보는 칸 ✕ 빼기 ·
 *    ‹ › · 오른쪽 붙박이 [＋] 넣기)
 *  · 맨 아래 단추 넷(v21 ④): [‹ 12.5%][큰 주 단추 47.5%][오늘 운동 끝내기 27.5% 빨강][› 12.5%]
 *
 * '보는 종목'(본)은 화면 상태다 — 손으로만 바뀐다(‹ › · 칸 누름 · 다음 종목으로). 체크 · 휴식 · 순서는 데이터(운동세션)가 쥔다.
 * 모든 행동은 누른 그 순간의 최신 세션으로 계산한다 — 빠르게 연달아 눌러도 옛 화면 값으로 두 번 하지 않는다.
 */
@Composable
fun 운동화면(상태: 앱상태, 폰: 폰기능) {
    val c = Local색.current
    val d = 상태.d
    val S = d.세션 ?: return
    if (S.끝화면) { 마무리(상태, S); return }

    var 지금 by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { 지금 = System.currentTimeMillis(); delay(250) } }
    // 운동 중에는 화면을 켜 둔다 (설정에서 끌 수 있다)
    val 뷰 = LocalView.current
    DisposableEffect(d.설정.화면유지) {
        뷰.keepScreenOn = d.설정.화면유지
        onDispose { 뷰.keepScreenOn = false }
    }

    val n = S.종목들.size
    if (n == 0) { 빈운동(상태); return }

    // 보는 종목 — 이 운동(시작 시각) 동안 기억한다. 탭을 오가도 그대로 (시안 `U.본`)
    val 본 = if (운보기.세션 == S.시작시각 && 운보기.본 in S.종목들.indices) 운보기.본 else S.i.coerceIn(0, n - 1)
    SideEffect { 운보기.세션 = S.시작시각; 운보기.본 = 본 }
    val e = S.종목들[본]
    // 칸 줄을 '보는 칸 맨 앞' 으로 다시 맞추라는 신호 (시안 `운자리.당김`)
    var 당김 by remember { mutableIntStateOf(0) }
    var 넣기열림 by remember { mutableStateOf(false) }

    /** 세션을 바꾸는 행동 — 최신 세션으로 계산하고, 보는 칸도 함께 옮긴다. 손댄 시각을 남긴다(오래 손대지 않으면 저절로 끝낸다) */
    fun 함(f: (운동세션) -> 운결과?) {
        운입력.확정?.invoke()   // 치던 kg · 회를 먼저 넣고 (체크 · 다른 단추를 눌렀을 때 친 값이 사라지지 않게)
        var 새본: Int? = null
        상태.바꿈 { dd ->
            val s = dd.세션 ?: return@바꿈 dd
            val r = f(s) ?: return@바꿈 dd
            새본 = r.본
            val 새 = dd.copy(세션 = r.세션.copy(마지막 = System.currentTimeMillis()))
            if (r.건너뜀) 새.세기더함(세기이름.연속건너뜀) else 새   // 업적 2-17 연속 건너뛰기
        }
        새본?.let { b -> 상태.d.세션?.let { s -> 운보기.본 = b.coerceIn(0, max(0, s.종목들.size - 1)) } }
    }
    fun 바꿈(f: (운동세션) -> 운동세션) = 함 { s -> 운결과(f(s), 운보기.본) }

    // 슈퍼세트 — 체크하면 지금 · 쉬는 줄이 묶음 안 다른 종목으로 간다. 같은 묶음을 보고 있으면 따라간다 (시안에는 슈퍼세트가 없다)
    LaunchedEffect(S.i, S.휴식?.종목, S.휴식 == null) { 운보기.본 = 본따라감(S, 운보기.본) }

    // 그림 칸 — ✕ 로 감추면 이 운동 동안 감추고, 띠의 [그림] 으로 다시 보인다 (10-02)
    val 그림켬 = d.설정.배너 != "숨김"
    val 그림숨김 = 그림칸기억.숨긴운동 == S.루틴id
    val 뒤로 = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            운머리(상태, S, 본, e, 지금, 그림보기 = if (그림켬 && 그림숨김) ({ 발자취.적기("그림 칸 다시 보임"); 그림칸기억.숨긴운동 = "" }) else null) {
                // ‹ = 캘린더로 나가기 (운동은 그대로 이어진다) — 앱의 뒤로가기와 같은 길
                발자취.적기("운동 화면 나가기"); 뒤로?.onBackPressed()
            }
            AnimatedVisibility(visible = 그림켬 && !그림숨김) {
                Box(Modifier.번호("운그림")) { 운동그림칸(상태, S.copy(i = 본), 지금) { 그림칸기억.숨긴운동 = S.루틴id } }
            }
            세트목록(상태, S, 본, 지금, Modifier.weight(1f), 함 = ::함, 바꿈 = ::바꿈, 당김 = { 당김++ })
            진행상자(상태, S, 본, 당김, Modifier.번호("운띠"), 함 = ::함, 고름 = { i -> 운보기.본 = i; 당김++ }, 넣기 = { 넣기열림 = true })
            단추줄(S, 본,
                이전 = { 운보기.본 = max(0, 운보기.본 - 1); 당김++ },
                다음 = { 상태.d.세션?.let { s -> 운보기.본 = min(s.종목들.size - 1, 운보기.본 + 1) }; 당김++ },
                주 = { 발자취.적기("큰 단추 ${운주상태(S, 본).글}"); 함 { s -> 운주누름(s, 운보기.본, System.currentTimeMillis()) }; 당김++ },
                끝내기 = { 발자취.적기("오늘 운동 끝내기"); 바꿈 { it.끝냄(System.currentTimeMillis()) } },
            )
        }
        // [＋] 종목 넣기 — 루틴과 같은 시트 (속은 RT 도우미 몫). 누름 = 넣기 / 들어간 것 누름 = 맨 뒤 하나 빼기 / 꾹 = 하나 더
        if (넣기열림) 종목넣기시트(
            상태, 제목 = "${S.루틴이름}에 넣기",
            개수 = { 열쇠 -> 상태.d.세션?.종목들?.count { it.열쇠 == 열쇠 && it.플랜id == null } ?: 0 },
            넣기 = { 열쇠 ->
                발자취.적기("운동 중 넣기 $열쇠")
                함 { s -> s.종목넣기(상태.d.세션줄(열쇠), 운보기.본).let { r -> 운결과(r.세션, r.본 ?: 운보기.본) } }
            },
            빼기 = { 열쇠 ->
                var 뺌 = false
                함 { s -> s.넣은것빼기(운보기.본) { it.열쇠 == 열쇠 && it.플랜id == null }?.let { 뺌 = true; 운결과(it.세션, it.본 ?: 운보기.본) } }
                if (뺌) 발자취.적기("운동 중 넣은 것 빼기 $열쇠") else 상태.알림.토스트("이미 시작한 종목은 뺄 수 없습니다")
            },
            닫기 = { 넣기열림 = false },
            // 10-05 검수: 운동 중에도 플랜 칸 (시안 넣기목록 '플랜넣기' · v13) — 루틴과 같은 처방 줄을 오늘만 넣는다
            플랜개수 = { pid -> 상태.d.세션?.종목들?.count { it.플랜id == pid } ?: 0 },
            플랜넣기 = { pid ->
                val p = 상태.d.플랜들.firstOrNull { it.id == pid }
                val 줄 = p?.let { 플랜줄(상태.d, it) }
                val e = 줄?.let { com.slayde.hasenheide.data.운동시작(com.slayde.hasenheide.data.루틴("넣기", "넣기", 종목 = listOf(it)), System.currentTimeMillis())?.종목들?.firstOrNull() }
                if (e != null) {
                    발자취.적기("운동 중 플랜 넣기 ${e.이름}")
                    함 { s -> s.종목넣기(e.copy(임시 = true, 계획세트 = 0), 운보기.본).let { r -> 운결과(r.세션, r.본 ?: 운보기.본) } }
                }
            },
            플랜빼기 = { pid ->
                var 뺌 = false
                함 { s -> s.넣은것빼기(운보기.본) { it.플랜id == pid }?.let { 뺌 = true; 운결과(it.세션, it.본 ?: 운보기.본) } }
                if (!뺌) 상태.알림.토스트("이미 시작한 종목은 뺄 수 없습니다")
            },
        )
    }
}

/** 보는 종목 — 화면을 떠났다 와도(다른 탭) 이 운동 동안은 그대로 (시안 `U.본` 은 화면 밖 상태다). 앱을 껐다 켜면 지금 종목부터 */
private object 운보기 {
    var 세션 by mutableLongStateOf(Long.MIN_VALUE)
    var 본 by mutableIntStateOf(0)
}

/** 지금 치고 있는 kg · 회 칸 — 다른 행동을 하기 전에 친 값을 넣는다 (한 번에 하나) */
private object 운입력 { var 확정: (() -> Unit)? = null }

/** 운동 화면에만 쓰는 값 (시안 v9 ~ v21) — 11 지침에 아직 이름이 없는 것. 합칠 때 Theme.kt `부품치수` 로 옮긴다 */
private object 운치수 {
    val 번호 = 36.dp                // 세트 번호 동그라미 (v10 · 줄 높이 44 = 36 + 위아래 4)
    val 값단추 = 20.dp              // 세트 줄 − ＋ 누르는 칸 폭 (시안 `.운세트들 .값칸 button`)
    val 칸폭 = 72.dp                // 아래 종목 칸 (시안 `.운칸 flex 72`)
    val 칸앞비움 = 32.dp            // 보는 칸을 맨 앞으로 넘길 때 ‹ 자리 (28 + 틈 4 · v21 ③)
    val 끌기오른끝 = 76.dp          // 끄는 손이 이 안에 들면 오른쪽으로 넘긴다 ([＋] 44 + 32)
    val 뺌동그라미 = 16.dp          // 보는 칸 ✕ (v21 ①)
    val 뺌그림 = 14.dp
    val 시계오른 = 20.dp            // 5gd1 타이머 왼쪽으로 20
    val 단추그림 = 20.dp            // 맨 아래 ‹ › (시안 `.운단추줄 svg`)
    val 점선 = 3.dp                 // [＋] 점선 테
    val 칸번호위 = 9.dp             // 칸 오른쪽 위 번호 딱지 (위 4 + 딱지가 스스로 올라가는 5)
    val 칸번호뺄수위 = 39.dp        // ✕ 가 있는 칸은 세트 수 줄 오른쪽 (34 + 5)
    val 칸번호옆 = 4.dp
    val 칸번호뺄수옆 = 7.dp
    const val 흐린단추 = 0.35f       // 못 누르는 ‹ › · 휴지통
    const val 끝칸흐림 = 0.55f       // 다 끝낸 칸 (보는 칸이 아닐 때)
    const val 게이지틱 = 260         // 쉼 게이지가 남은 시간을 따라가는 시간(ms) — 시계 0.25초마다
    const val 게이지줄 = 1.1f        // 게이지 두 줄의 줄 높이(em) — 32 높이에 시간 15 + 문구 11 이 들어가게 (시안 line-height 1.1)
    const val 칸이름줄 = 1.25f       // 아래 칸 이름 11 두 줄이 28 안에 (시안 `.운칸 .ㅇ line-height 1.25`)
    val 단추비 = listOf(0.125f, 0.475f, 0.275f, 0.125f)   // v21 ④
    val 줄최소 = listOf(44f, 72f, 56f, 86f)                  // 세트 줄 칸 — 번호 · kg · 회 · 휴식 (시안 v10 grid minmax)
    val 줄비 = listOf(66f, 80f, 102f, 104f)
}

// ═════════════════════ 맨 위 띠 ═════════════════════

/** 시안 `.운머리.띠.두줄` — 강조 바탕. 첫 줄 ‹ · 이름 · 그림 · 시계 / 둘째 줄 지표 (높이 28 고정) */
@Composable
private fun 운머리(상태: 앱상태, S: 운동세션, 본: Int, e: 세션종목, 지금: Long, 그림보기: (() -> Unit)?, 나가기: () -> Unit) {
    val c = Local색.current
    val d = 상태.d
    Column(
        Modifier.fillMaxWidth().background(c.강조).번호("운0")
            .padding(horizontal = 간격.보통, vertical = 부품치수.띠세로여백),
        verticalArrangement = Arrangement.spacedBy(간격.아주좁게),
    ) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 높이.낮게).padding(top = 간격.아주좁게),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.좁게),
        ) {
            Box(
                Modifier.height(높이.낮게).widthIn(min = 높이.아주낮게).눌림(나가기).semantics { contentDescription = "나가기" },
                contentAlignment = Alignment.Center,
            ) { Text("‹", style = 글꼴.보통(크기.크게, FontWeight.Bold), color = c.강조글) }
            운이름(d, S, 본, e, 상태.오늘, Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
                if (그림보기 != null) Box(
                    Modifier.clip(RoundedCornerShape(모서리.작게)).background(c.강조글).눌림(그림보기)
                        .padding(horizontal = 간격.좁게, vertical = 간격.아주좁게),
                ) { Text("그림", style = 글꼴.보통(크기.버튼, FontWeight.Bold), color = c.강조, maxLines = 1) }
                Text(
                    분초(((S.끝시각 ?: 지금) - S.시작시각 - S.멈춘).coerceAtLeast(0L).div(1000).toInt()),
                    Modifier.padding(end = 운치수.시계오른),
                    style = 글꼴.보통(크기.크게, FontWeight.Bold).copy(fontFeatureSettings = "tnum"), color = c.강조글, maxLines = 1,
                )
            }
        }
        운수치(d, S, e, 상태.오늘)
    }
}

/**
 * 종목 이름 — **잘리지 않는다**(U5-8 · 두 줄 넘어도 감긴다). 끝 낱말에 [플랜] · 같은 이름 번호 딱지가 붙어 같이 감기고,
 * 플랜이면 바로 뒤 같은 줄에 'N회차 · 측정일 · N주 진행 중'(13 · 통째로 감김 · 시안 ✎ ozlo · r0yk)
 */
@Composable
private fun 운이름(d: 앱데이터, S: 운동세션, 본: Int, e: 세션종목, 오늘: String, modifier: Modifier) {
    val c = Local색.current
    val p = e.플랜id?.let { id -> d.플랜들.firstOrNull { it.id == id } }
    val 번 = if (p != null) 0 else d.같은이름번호(e.종id, e.이름)
    val 딱 = if (p != null) "플랜" else if (번 > 0) "$번" else null
    val 곁 = when {
        p != null -> {
            val 계 = p.다음회(d.몸, d.향상기록들)
            listOfNotNull("${계?.회 ?: (p.한회 + 1)}회차", if (계?.측정일 == true) "측정일" else null,
                if (p.만든날.isNotBlank()) "${p.지난주(오늘)}주 진행 중" else null).joinToString(" · ")
        }
        S.식구(본).size > 1 -> "슈퍼세트 " + ('A' + S.식구(본).indexOf(본)).toString()
        else -> null
    }
    val 측정 = rememberTextMeasurer()
    val 밀도 = LocalDensity.current
    val 딱꼴 = 글꼴.보통(크기.작게, FontWeight.Bold).copy(lineHeight = 부품치수.딱지글높이, fontFeatureSettings = "tnum")
    val 딱폭 = remember(딱) {
        딱?.let { t -> with(밀도) { (측정.measure(t, 딱꼴).size.width + (간격.아주좁게 * 2 + 부품치수.딱지겹침).toPx()).toSp() } }
    }
    val 글 = buildAnnotatedString {
        append(e.이름.trim())
        if (딱 != null) { append("\u2060"); appendInlineContent("딱", 딱) }   // 끝 낱말과 딱지가 떨어져 감기지 않게
        if (곁 != null) withStyle(SpanStyle(fontSize = 크기.버튼, fontWeight = FontWeight.Normal, letterSpacing = (-0.01).em)) {
            append("  "); append(곁.replace(' ', '\u00A0'))
        }
    }
    val 붙임 = if (딱 == null || 딱폭 == null) emptyMap() else mapOf(
        "딱" to InlineTextContent(Placeholder(딱폭, 16.sp, PlaceholderVerticalAlign.TextTop)) { 띠딱지(딱, 딱꼴, 숫자 = p == null) },
    )
    Text(
        글, modifier, color = c.강조글, inlineContent = 붙임,
        style = 글꼴.제목(크기.크게),
    )
}

/** 띠 위 딱지 — 띠도 강조 바탕이라 1 강조글 테로 가른다 (시안 `.운플랜표` · `.운번호표`). 이름 오른쪽 위에 겹친다 */
@Composable
private fun 띠딱지(글: String, 꼴: TextStyle, 숫자: Boolean) {
    val c = Local색.current
    val 모양 = RoundedCornerShape(모서리.작게)
    Box(
        Modifier.wrapContentSize(Alignment.TopStart, unbounded = true)
            .offset(x = 부품치수.딱지겹침, y = if (숫자) 부품치수.번호딱지위 else 부품치수.딱지위)
            .clip(모양).background(c.강조).border(선굵기.보통, c.강조글, 모양)
            .padding(horizontal = 간격.아주좁게),
        contentAlignment = Alignment.Center,
    ) { Text(글, style = 꼴, color = c.강조글, maxLines = 1, softWrap = false) }
}

/**
 * 지표 한 줄 (시안 `.운수치` · ✎ zx75 · 3ehg) — 높이 28 고정(값이 바뀌어도 띠가 안 움직인다).
 * 1RM(맨몸은 최고 횟수) · [1주 ▲] · [최고 ▲] · 달성 · 볼륨/목표. 넘치면 목표를 감추고, 그래도 넘치면 줄여 넣는다
 */
@Composable
private fun 운수치(d: 앱데이터, S: 운동세션, e: 세션종목, 오늘: String) {
    val 맨 = 플랜표.찾기(e.이름)?.맨몸인가 == true
    val 오 = 운오늘값(e.찬것(), 맨)
    val (최고, 주) = remember(d.기록, e.열쇠, 오늘, 맨) { 운최고(d.기록, e.열쇠, 오늘, 맨) }
    val 달 = if (e.총칸() > 0) (e.찬것().size * 100.0 / e.총칸()).roundToInt() else 0
    val 볼 = 볼륨(e.찬것())
    val 목 = 볼륨((0 until e.총칸()).map { S.세트값(e, it) })
    val 줄: @Composable (Boolean) -> Unit = { 목표 -> 수치줄(맨, 오, 주, 최고, 달, 볼, 목, 목표) }
    SubcomposeLayout(Modifier.fillMaxWidth().height(높이.아주낮게)) { cons ->
        val 무한 = Constraints(maxHeight = cons.maxHeight)
        var 재 = subcompose("다") { 줄(true) }.map { it.measure(무한) }
        if ((재.maxOfOrNull { it.width } ?: 0) > cons.maxWidth) 재 = subcompose("목표없이") { 줄(false) }.map { it.measure(무한) }
        val 폭 = 재.maxOfOrNull { it.width } ?: 0
        val 배 = if (폭 > cons.maxWidth && 폭 > 0) cons.maxWidth.toFloat() / 폭 else 1f
        layout(cons.maxWidth, cons.maxHeight) {
            재.forEach { p ->
                val y = (cons.maxHeight - p.height) / 2
                if (배 < 1f) p.placeWithLayer(0, y) { scaleX = 배; scaleY = 배; transformOrigin = TransformOrigin(0f, 0.5f) }
                else p.place(0, y)
            }
        }
    }
}

@Composable
private fun 수치줄(맨: Boolean, 오: Double?, 주: Double?, 최고: Double?, 달: Int, 볼: Double, 목: Double, 목표보임: Boolean) {
    val c = Local색.current
    val 작 = 글꼴.보통(크기.작게).copy(fontFeatureSettings = "tnum")
    val 굵 = SpanStyle(fontSize = 크기.본문, fontWeight = FontWeight.Bold)
    fun 값(앞: String, b: String, 뒤: String = "") = buildAnnotatedString { append(앞); withStyle(굵) { append(b) }; append(뒤) }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
        val 단위 = if (맨) "회" else "kg"
        Text(값(if (맨) "최고 " else "1RM ", if (오 == null) "—" else 무게글(Math.round(오 * 2) / 2.0) + 단위), style = 작, color = c.강조글, maxLines = 1, softWrap = false)
        비교칩("1주", 운차(오, 주))
        비교칩("최고", 운차(오, 최고))
        Text("·", Modifier.padding(horizontal = 간격.아주좁게), style = 작, color = c.강조글)
        Text(값("달성 ", "$달%"), style = 작, color = c.강조글, maxLines = 1, softWrap = false)
        Text("·", Modifier.padding(horizontal = 간격.아주좁게), style = 작, color = c.강조글)
        Text(값("볼륨 ", 콤마(볼), (if (목표보임) "/${콤마(목)}" else "") + "kg"), style = 작, color = c.강조글, maxLines = 1, softWrap = false)
    }
}

/** [1주 ▲2.5] — 흰 칩 · 강조 글. 오르면 ▲ 오름 색, 내리면 ▼ 내림 색, 같으면 '유지', 견줄 것이 없으면 '—' (단위 없음 · v9) */
@Composable
private fun 비교칩(이름: String, 차: Double?) {
    val c = Local색.current
    val 모양 = RoundedCornerShape(모서리.작게)
    Row(
        Modifier.height(높이.아주낮게).clip(모양).background(c.강조글).border(선굵기.보통, c.강조글, 모양).padding(horizontal = 간격.아주좁게),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val 꼴 = 글꼴.보통(크기.작게, FontWeight.Bold).copy(fontFeatureSettings = "tnum")
        Text(if (차 == null) "$이름 —" else if (차 == 0.0) "$이름 유지" else 이름, style = 꼴, color = c.강조, maxLines = 1, softWrap = false)
        if (차 != null && 차 != 0.0) Text(
            (if (차 > 0) "▲" else "▼") + 무게글(abs(차)), Modifier.padding(start = 간격.아주좁게),
            style = 꼴, color = if (차 > 0) c.오름 else c.내림, maxLines = 1, softWrap = false,
        )
    }
}

// ═════════════════════ 세트 목록 ═════════════════════

/**
 * 보는 종목의 세트 줄 (시안 `.운세트들`) — 머리(세트 · kg · 회 · 휴식) · 줄들 · 빈 줄 하나 · [+ 세트].
 *  · 보는 종목이 바뀌면 맨 위부터
 *  · 넘치는 목록에서 보이는 칸 아래 절반의 줄을 체크하면, 다음 줄이 가운데쯤 오게 부드럽게 올린다 (✎ h6tu · 올리기만)
 */
@Composable
private fun 세트목록(
    상태: 앱상태, S: 운동세션, 본: Int, 지금: Long, modifier: Modifier,
    함: ((운동세션) -> 운결과?) -> Unit, 바꿈: ((운동세션) -> 운동세션) -> Unit, 당김: () -> Unit,
) {
    val c = Local색.current
    val e = S.종목들[본]
    val 목 = rememberScrollState()
    val 자 = remember { 줄자리() }
    var 다음줄 by remember { mutableStateOf<Triple<Int, Int, Long>?>(null) }   // (보는 칸, 줄, 누른 때) — 한 번만 쓴다
    val 본키 = "${S.시작시각}|$본|${e.열쇠}"
    LaunchedEffect(본키) { 목.scrollTo(0) }
    LaunchedEffect(다음줄) {
        val (b, k, _) = 다음줄 ?: return@LaunchedEffect
        if (b != 운보기.본) return@LaunchedEffect
        delay(움직임.색.toLong())   // 체크 색이 옮겨 가는 동안은 재지 않는다 (U5-6) — 줄 자리는 체크로 바뀌지 않는다
        val y = 자.위[k] ?: return@LaunchedEffect
        val 목표 = (y + 자.위끝 + 자.높이 / 2 - 자.보임 / 2).coerceIn(0, 목.maxValue)
        if (목표 > 목.value + 1) 목.animateScrollTo(목표)
    }
    val 지금k = 첫빈칸(e)
    val 무게폭 = 상태.d.설정.무게폭
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val 남는 = maxWidth - 간격.보통 * 2 - 높이.아주낮게 - 간격.아주좁게 * 4
        val 폭 = 격자폭(남는.value, 운치수.줄최소, 운치수.줄비).map { it.dp }
        val 위끝px = with(LocalDensity.current) { 간격.좁게.roundToPx() }
        SideEffect { 자.위끝 = 위끝px }   // 목록 위 여백 — 줄 자리(positionInParent)에는 빠져 있다
        Column(
            Modifier.fillMaxSize().onSizeChanged { 자.보임 = it.height }.verticalScroll(목)
                .padding(horizontal = 간격.보통, vertical = 간격.좁게),
            verticalArrangement = Arrangement.spacedBy(간격.아주좁게),
        ) {
            // 머리 — 세트 · kg · 회 · 휴식 (11 옅음)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                listOf("세트", "kg", "회", "휴식").forEachIndexed { x, t ->
                    Text(t, Modifier.width(폭[x]), style = 글꼴.보통(크기.작게), color = c.옅음, textAlign = TextAlign.Center, maxLines = 1)
                }
            }
            for (k in 0 until e.총칸()) {
                운세트줄(
                    S, 본, k, 지금k, 폭, 지금, 무게폭,
                    Modifier.onPlaced { 자.위[k] = it.positionInParent().y.roundToInt(); 자.높이 = it.size.height },
                    체크 = {
                        val 전 = 상태.d.세션?.종목들?.getOrNull(본)
                        val 체크함 = 전 != null && 전.기록.칸(k) == null
                        발자취.적기("${e.이름} ${k + 1}세트 ${if (체크함) "체크" else "체크 풀기"}")
                        // 보이는 칸 아래 절반의 줄을 체크하면 다음 빈 줄을 가운데로 (넘칠 때만)
                        val y = 자.위[k]
                        if (체크함 && 전 != null && y != null && 목.maxValue > 0 && y + 자.위끝 + 자.높이 / 2 - 목.value > 자.보임 / 2) {
                            val 다 = (k + 1 until 전.총칸()).firstOrNull { 전.기록.칸(it) == null }
                            if (다 != null) 다음줄 = Triple(본, 다, System.nanoTime())
                        }
                        함 { s -> 운체크(s, 본, k, System.currentTimeMillis()).let { 운결과(it, 본따라감(it, 본)) } }
                        당김()
                    },
                    쉼누름 = { 마무리 ->
                        if (마무리) { 발자취.적기("쉼 게이지 — 마무리"); 바꿈 { it.끝냄(System.currentTimeMillis()) } }
                        else { 발자취.적기("휴식 건너뛰기"); 함 { s -> 휴식건너뛰기(s, 운보기.본, System.currentTimeMillis()) } }
                        당김()
                    },
                    값 = { f -> 바꿈(f); 당김() },
                    지우기 = { 세트지우기(상태, 본, k) { 당김() } },
                )
            }
            Spacer(Modifier.height(높이.높게))   // v17 ④ '+ 세트' = 마지막 세트의 다음다음 줄
            버튼("+ 세트", { 발자취.적기("세트 추가"); 바꿈 { it.세트추가(본) }; 당김() }, Modifier.fillMaxWidth(), 낮게 = true)
        }
    }
}

/** 세트 목록의 줄 자리 (px) — 화면 맞추기에만 쓴다. 상태가 아니다 */
private class 줄자리 { val 위 = HashMap<Int, Int>(); var 높이 = 0; var 보임 = 0; var 위끝 = 0 }

/** 세트 지우기 — 묻지 않고 지우고 아래띠 [되돌리기] (U5-4 · 시안 `세트지우기`). 하나 남으면 지우지 않는다 */
private fun 세트지우기(상태: 앱상태, j: Int, k: Int, 다음: () -> Unit) {
    var z: 지운세트? = null
    var 이름 = ""
    상태.바꿈 { dd ->
        val s = dd.세션 ?: return@바꿈 dd
        val (t, zz) = 운세트지우기(s, j, k) ?: return@바꿈 dd
        z = zz; 이름 = s.종목들[j].이름
        dd.copy(세션 = t.copy(마지막 = System.currentTimeMillis()))
    }
    val 지운 = z ?: return
    발자취.적기("$이름 ${k + 1}세트 지우기")
    다음()
    상태.알림.되돌림("운세트지움", { n -> if (n > 1) "세트 ${n}개를 지웠습니다" else "$이름 ${k + 1}세트를 지웠습니다" }) {
        발자취.적기("세트 되돌림 · $이름")
        상태.바꿈 { dd -> dd.세션?.let { s -> dd.copy(세션 = 세트되살리기(s, 지운, System.currentTimeMillis())) } ?: dd }
    }
}

/**
 * 세트 한 줄 (시안 `운세트줄` · v8 ~ v21) — [번호 동그라미 = 체크] [kg] [회] [휴식 − ＋] [휴지통].
 * 쉬는 줄은 kg · 회 · 휴식 세 칸 자리에 게이지 단추 하나. 끝난 줄은 높이 가운데 띠 · 칸 테와 − ＋ 를 감춘다. 지금 줄은 1초 점멸
 */
@Composable
private fun 운세트줄(
    S: 운동세션, j: Int, k: Int, 지금k: Int, 폭: List<Dp>, 지금: Long, 무게폭: Double, modifier: Modifier,
    체크: () -> Unit, 쉼누름: (마무리: Boolean) -> Unit, 값: ((운동세션) -> 운동세션) -> Unit, 지우기: () -> Unit,
) {
    val c = Local색.current
    val e = S.종목들[j]
    val v = S.세트값(e, k)
    val 완료 = e.기록.칸(k) != null
    val 쉼 = S.휴식자리(j, k)
    val 모양 = RoundedCornerShape(모서리.작게)
    val 지금줄 = k == 지금k && !쉼
    Row(
        modifier.fillMaxWidth()
            .완료바탕(완료 && !지금줄)
            .clip(모양).점멸바탕(지금줄, 모양)
            // 10-02: 워밍업 세트 줄은 왼쪽에 연두 띠 — 볼륨 · 1RM · 플랜 반영에서 빠지는 줄
            .then(if (v.종류 == 세트종류.워밍업) Modifier.왼띠(c.워밍업) else Modifier)
            .padding(vertical = 간격.아주좁게),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.아주좁게),
    ) {
        Box(Modifier.width(폭[0]), contentAlignment = Alignment.Center) {
            번호동그라미(k + 1, 완료, "${k + 1}세트 ${if (완료) "체크 풀기" else "완료"}", 체크)
        }
        if (쉼) {
            val h = S.휴식!!
            val 총 = if (h.총초 > 0) h.총초 else e.세트휴식(k)
            val 비율 = if (h.물음 || 총 <= 0) 0f else ((h.끝시각 - 지금).toFloat() / (총 * 1000f))
            val 종류 = 쉼글(S, j, j)
            쉼게이지(
                비율, 분초(if (h.물음) 0 else h.남은초(지금)), 종류.글,
                Modifier.width(폭[1] + 폭[2] + 폭[3] + 간격.아주좁게 * 2),
            ) { 쉼누름(종류 == 쉼종류.마무리) }
        } else {
            운값칸(무게글(v.w), "무게", 소수 = true, 끝남 = 완료, Modifier.width(폭[1]),
                빼기 = { 값 { s -> s.값고치기(j, k, 새무게 = s.세트값(s.종목들[j], k).w - 무게폭) } },
                더하기 = { 값 { s -> s.값고치기(j, k, 새무게 = s.세트값(s.종목들[j], k).w + 무게폭) } },
                넣기 = { t -> t.replace(',', '.').toDoubleOrNull()?.let { w -> 값 { s -> s.값고치기(j, k, 새무게 = w) } } })
            운값칸("${v.r}", "횟수", 소수 = false, 끝남 = 완료, Modifier.width(폭[2]),
                빼기 = { 값 { s -> s.값고치기(j, k, 새횟수 = s.세트값(s.종목들[j], k).r - 1) } },
                더하기 = { 값 { s -> s.값고치기(j, k, 새횟수 = s.세트값(s.종목들[j], k).r + 1) } },
                넣기 = { t -> t.toIntOrNull()?.let { r -> 값 { s -> s.값고치기(j, k, 새횟수 = r) } } })
            Box(Modifier.width(폭[3]), contentAlignment = Alignment.Center) {
                // 슈퍼세트는 마지막 종목 줄에만 휴식 (09-21)
                if (S.휴식보임(j)) 운휴칸(e.세트휴식(k), 완료, Modifier.fillMaxWidth()) { 방향 ->
                    값 { s -> s.종목들.getOrNull(j)?.let { x -> s.휴식고치기(j, k, 휴식한칸(x.세트휴식(k), 방향)) } ?: s }
                }
            }
        }
        val 하나 = e.총칸() <= 1
        Box(
            Modifier.size(높이.아주낮게).alpha(if (하나) 운치수.흐린단추 else 1f).then(if (하나) Modifier else Modifier.눌림(지우기))
                .semantics { contentDescription = "${k + 1}세트 지우기" },
            contentAlignment = Alignment.Center,
        ) { Icon(아이콘.지우기, null, Modifier.size(18.dp), tint = c.옅음) }
    }
}

/** 번호 동그라미 = 체크 (v8 · v10) — 36 · 테 2 강조 · 번호 15 보통 굵기. 체크하면 강조로 채우고 ✓ (끝내는 순간 톡 튄다) */
@Composable
private fun 번호동그라미(번호: Int, 완료: Boolean, 설명: String, on누름: () -> Unit) {
    val c = Local색.current
    val 톡 = remember { Animatable(1f) }
    var 전완료 by remember { mutableStateOf(완료) }
    LaunchedEffect(완료) {
        if (완료 && !전완료) { 톡.snapTo(움직임.톡시작); 톡.animateTo(1f, spring(dampingRatio = 움직임.톡탄성, stiffness = Spring.StiffnessMedium)) }
        전완료 = 완료
    }
    Box(
        Modifier.size(운치수.번호).graphicsLayer { scaleX = 톡.value; scaleY = 톡.value }.clip(CircleShape)
            .background(색움직(if (완료) c.강조 else c.면, "번호바탕")).border(선굵기.굵게, c.강조, CircleShape)
            .눌림(on누름).semantics { contentDescription = 설명 },
        contentAlignment = Alignment.Center,
    ) {
        if (완료) Icon(아이콘.체크, null, Modifier.size(18.dp), tint = c.강조글)
        else Text("$번호", style = 글꼴.보통(크기.본문).copy(fontFeatureSettings = "tnum"), color = c.강조, maxLines = 1)
    }
}

/**
 * kg · 회 칸 (시안 `.값칸` 높이 28 · − 값 ＋ · 값은 바로 쳐 넣는다 — 자판 완료 · 칸을 떠나면 들어간다). 끝난 줄은 테 · − ＋ 를 감추고 글을 흐리게 (✎ qnpi).
 * 긴 값('102.5')은 글자를 줄여 넣는다 15 → 13 → 11 (✎ ai39)
 */
@Composable
private fun 운값칸(
    값글: String, 이름: String, 소수: Boolean, 끝남: Boolean, modifier: Modifier,
    빼기: () -> Unit, 더하기: () -> Unit, 넣기: (String) -> Unit,
) {
    val c = Local색.current
    val 모양 = RoundedCornerShape(모서리.작게)
    var 글 by remember(값글) { mutableStateOf(TextFieldValue(값글)) }
    var 잡힘 by remember { mutableStateOf(false) }
    var 새로 by remember { mutableStateOf(false) }   // 잡은 뒤 아직 안 쳤다 — 처음 친 글자가 옛 값을 바꾼다
    val 초점 = LocalFocusManager.current
    fun 걸러(t: String) = t.filter { it.isDigit() || (소수 && (it == '.' || it == ',')) }.take(6)
    fun 확정() {
        if (운입력.확정 != null) 운입력.확정 = null
        val t = 글.text.trim()
        if (t.isNotEmpty() && t != 값글) { 발자취.적기("$이름 입력 $t"); 넣기(t) } else 글 = TextFieldValue(값글)
    }
    DisposableEffect(잡힘) {
        val f: () -> Unit = { 초점.clearFocus() }
        val g: () -> Unit = { 잡힘 = false; 확정(); 초점.clearFocus() }
        if (잡힘) { 입력중.취소 = f; 운입력.확정 = g }
        onDispose { if (입력중.취소 === f) 입력중.취소 = null; if (운입력.확정 === g) 운입력.확정 = null }
    }
    Row(
        modifier.height(높이.아주낮게).clip(모양).border(선굵기.보통, if (끝남) Color.Transparent else c.속선, 모양),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        값단추(아이콘.빼기, "$이름 빼기", !끝남, onClick = 빼기)
        val 길이 = 글.text.length
        BasicTextField(
            value = 글,
            onValueChange = { t ->
                val 늘어난 = t.text.length - 글.text.length
                val x = if (새로 && 늘어난 > 0) {   // 처음 친 글자만 — 옛 값을 지운다
                    val 끝 = t.selection.start.coerceIn(0, t.text.length)
                    걸러(t.text.substring((끝 - 늘어난).coerceAtLeast(0), 끝))
                } else 걸러(t.text)
                if (t.text != 글.text) 새로 = false
                글 = if (x == t.text) t else TextFieldValue(x, TextRange(x.length))
            },
            modifier = Modifier.weight(1f).onFocusChanged { f ->
                if (f.isFocused && !잡힘) { 새로 = true; 글 = 글.copy(selection = TextRange(0, 글.text.length)) }
                if (!f.isFocused && 잡힘) { 잡힘 = false; 확정() }
                잡힘 = f.isFocused
            },
            singleLine = true,
            textStyle = 글꼴.보통(if (길이 >= 6) 크기.작게 else if (길이 == 5) 크기.버튼 else 크기.본문, FontWeight.Bold)
                .copy(color = if (끝남) c.흐림 else c.글, textAlign = TextAlign.Center, fontFeatureSettings = "tnum"),
            keyboardOptions = KeyboardOptions(keyboardType = if (소수) KeyboardType.Decimal else KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { 초점.clearFocus() }),
            cursorBrush = SolidColor(c.강조),
        )
        값단추(아이콘.더하기, "$이름 더하기", !끝남, onClick = 더하기)
    }
}

/** 휴식 칸 — kg · 회 와 같은 − 값 ＋ (15초씩 · 0:15 ~ 5:00 · ✎ ypk0 · v9). 값 15 · 끝난 줄은 테 · − ＋ 감춤 */
@Composable
private fun 운휴칸(초: Int, 끝남: Boolean, modifier: Modifier, 바꿈: (방향: Int) -> Unit) {
    val c = Local색.current
    val 모양 = RoundedCornerShape(모서리.작게)
    Row(
        modifier.height(높이.아주낮게).clip(모양).border(선굵기.보통, if (끝남) Color.Transparent else c.속선, 모양),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        값단추(아이콘.빼기, "휴식 15초 줄이기", !끝남, 초 > 휴식칸값.최소) { 바꿈(-1) }
        Text(
            분초(초), Modifier.weight(1f), style = 글꼴.보통(크기.본문).copy(fontFeatureSettings = "tnum"),
            color = if (끝남) c.흐림 else c.글, maxLines = 1, textAlign = TextAlign.Center, softWrap = false,
        )
        값단추(아이콘.더하기, "휴식 15초 늘리기", !끝남, 초 < 휴식칸값.최대) { 바꿈(1) }
    }
}

@Composable
private fun 값단추(그림: androidx.compose.ui.graphics.vector.ImageVector, 설명: String, 보임: Boolean, 켬: Boolean = true, onClick: () -> Unit) {
    val c = Local색.current
    Box(
        Modifier.width(운치수.값단추).fillMaxHeight().alpha(if (!보임) 0f else if (!켬) 운치수.흐린단추 else 1f)
            .then(if (보임 && 켬) Modifier.눌림 { 운입력.확정?.invoke(); onClick() }.semantics { contentDescription = 설명 } else Modifier),
        contentAlignment = Alignment.Center,
    ) { Icon(그림, null, Modifier.size(16.dp), tint = c.강조) }
}

/**
 * 쉼 게이지 (v17 ① · v18 ② · v21 ⑦) — 높이 32 · 안쪽 테 1 강조. 강조로 채운 쪽이 남은 시간만큼 왼쪽부터 줄어든다(경계에서 글 색이 갈린다).
 * [노란 점 | 남은 시간 15 / 문구 11]. 문구가 넘치면 자간만 −0.02 ~ −0.08em 으로 좁힌다
 */
@Composable
private fun 쉼게이지(남은비율: Float, 시간: String, 문구: String, modifier: Modifier, onClick: () -> Unit) {
    val c = Local색.current
    val 비율 by animateFloatAsState(남은비율.coerceIn(0f, 1f), tween(운치수.게이지틱, easing = LinearEasing), label = "쉼게이지")
    val 모양 = RoundedCornerShape(모서리.작게)
    val 속: @Composable (Color) -> Unit = { 색 ->
        Row(
            Modifier.fillMaxSize().padding(horizontal = 간격.아주좁게),
            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
        ) {
            노란점()
            Spacer(Modifier.width(간격.좁게))
            Column(Modifier.weight(1f, fill = false), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(시간, style = 글꼴.보통(크기.본문, FontWeight.Bold).copy(fontFeatureSettings = "tnum", lineHeight = 운치수.게이지줄.em), color = 색, maxLines = 1)
                자간맞춤글(문구, 글꼴.보통(크기.작게, FontWeight.Bold).copy(lineHeight = 운치수.게이지줄.em), 색)
            }
        }
    }
    Box(
        modifier.height(높이.낮게).clip(모양).background(c.면).눌림(onClick).semantics { contentDescription = "휴식 $시간 $문구" },
    ) {
        속(c.강조)
        Box(
            Modifier.matchParentSize()
                .drawWithContent { clipRect(right = size.width * 비율) { this@drawWithContent.drawContent() } }
                .background(c.강조),
        ) { 속(c.강조글) }
        Box(Modifier.matchParentSize().border(선굵기.보통, c.강조, 모양))
    }
}

/** 한 줄 글 — 넘치면 자간만 −0.02em 씩 −0.08em 까지, 그래도 넘치면 … (시안 v21 ⑦② `쉼글맞춤`) */
@Composable
private fun 자간맞춤글(글: String, 꼴: TextStyle, 색: Color) {
    var n by remember(글) { mutableIntStateOf(0) }
    Text(
        글, style = if (n == 0) 꼴 else 꼴.copy(letterSpacing = (-n / 100f).em), color = 색, maxLines = 1, softWrap = false,
        overflow = if (n >= 8) TextOverflow.Ellipsis else TextOverflow.Clip,
        onTextLayout = { r -> if (r.hasVisualOverflow && n < 8) n = if (n == 0) 2 else n + 1 },
    )
}

// ═════════════════════ 아래 진행 상자 · 종목 칸 줄 ═════════════════════

/**
 * 진행 상자 (시안 `.운아래`) — [루틴 이름 · 종목 n/m · 세트 n/m · 볼륨 n/m] · 막대 · 종목 칸 줄.
 * 칸 줄: 칸 72 · 틈 4 · 꾹 눌러 끌기 = 순서 · 보는 칸 오른쪽 위 ✕ = 빼기(되돌리기) · ‹ › · 오른쪽 붙박이 [＋] = 넣기
 */
@Composable
private fun 진행상자(
    상태: 앱상태, S: 운동세션, 본: Int, 당김: Int, modifier: Modifier,
    함: ((운동세션) -> 운결과?) -> Unit, 고름: (Int) -> Unit, 넣기: () -> Unit,
) {
    val c = Local색.current
    val d = 상태.d
    val n = S.종목들.size
    val 전 = S.종목들.sumOf { it.총칸() }
    val 끝낸 = S.종목들.sumOf { it.찬것().size }
    val 끝종 = S.종목들.count { it.총칸() > 0 && it.찬것().size >= it.총칸() }
    val 총볼 = S.종목들.sumOf { 볼륨(it.찬것()) }
    val 총목 = S.종목들.sumOf { x -> 볼륨((0 until x.총칸()).map { S.세트값(x, it) }) }
    Column(
        modifier.fillMaxWidth().background(c.면2)
            .drawBehind { drawRect(c.선, size = Size(size.width, 선굵기.보통.toPx())) }
            .padding(horizontal = 간격.보통, vertical = 간격.좁게),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.보통)) {
            글(S.루틴이름, Modifier.weight(1f), 크기값 = 크기.버튼, 굵기 = FontWeight.Bold)
            val 작 = 글꼴.보통(크기.작게).copy(fontFeatureSettings = "tnum")
            fun 값(앞: String, b: String, 뒤: String = "") = buildAnnotatedString {
                append(앞); withStyle(SpanStyle(color = c.글, fontWeight = FontWeight.Bold)) { append(b) }; append(뒤)
            }
            Text(값("종목 ", "$끝종/$n"), style = 작, color = c.흐림, maxLines = 1)
            Text(값("세트 ", "$끝낸/$전"), style = 작, color = c.흐림, maxLines = 1)
            Text(값("볼륨 ", 콤마(총볼), "/${콤마(총목)}"), style = 작, color = c.흐림, maxLines = 1)
        }
        진행막대(if (전 > 0) 끝낸.toFloat() / 전 else 0f, Modifier.fillMaxWidth().padding(top = 간격.좁게))
        칸줄(상태, S, 본, 당김, 함, 고름, 넣기)
    }
}

@Composable
private fun 칸줄(
    상태: 앱상태, S: 운동세션, 본: Int, 당김: Int,
    함: ((운동세션) -> 운결과?) -> Unit, 고름: (Int) -> Unit, 넣기: () -> Unit,
) {
    val c = Local색.current
    val d = 상태.d
    val n = S.종목들.size
    val 넘김 = rememberScrollState()
    val 밀도 = LocalDensity.current
    val 걸음 = with(밀도) { (운치수.칸폭 + 간격.아주좁게).toPx() }
    val 판 = remember가로끌기판 { 원, 대상, 뒤에 ->
        발자취.적기("운동 순서 바꿈")
        함 { s -> s.종목옮기기(원, 대상, 뒤에, 운보기.본).let { r -> 운결과(r.세션, r.본 ?: 운보기.본) } }
    }
    // 보는 칸을 맨 앞(왼쪽)에 — 앞에 칸이 있으면 ‹ 자리(32)를 비운다 (v16 · v21 ③). 처음엔 바로, 그 뒤로는 부드럽게
    var 처음 by remember { mutableStateOf(true) }
    val 본열쇠 = S.종목들.getOrNull(본)?.열쇠
    LaunchedEffect(S.시작시각, 본, 본열쇠, 당김, n) {
        withFrameNanos { }   // 칸이 늘고 준 뒤의 자리로 (U5-6 — 바뀌는 중에 재지 않는다)
        val 최대 = snapshotFlow { 넘김.maxValue }.first { it < Int.MAX_VALUE }
        val 목표 = 칸줄목표(본, 걸음, with(밀도) { 운치수.칸앞비움.toPx() }, 최대.toFloat()).roundToInt()
        if (abs(목표 - 넘김.value) > 1) { if (처음) 넘김.scrollTo(목표) else 넘김.animateScrollTo(목표) }
        처음 = false
    }
    Box(Modifier.fillMaxWidth().clipToBounds().padding(top = 간격.좁게)) {
    화살줄(넘김, Modifier.fillMaxWidth(), 오른쪽비움 = 높이.높게 + 간격.아주좁게) {
        var 폭px by remember { mutableStateOf(0f) }
        Box(Modifier.fillMaxWidth().onSizeChanged { 폭px = it.width.toFloat() }) {
            val 더px = with(밀도) { 높이.높게.toPx() }
            Row(
                Modifier.fillMaxWidth().가로끌기줄(판, 넘김, 운치수.끌기오른끝).horizontalScroll(넘김),
                horizontalArrangement = Arrangement.spacedBy(간격.아주좁게),
            ) {
                S.종목들.forEachIndexed { i, x ->
                    val 다끝 = x.총칸() > 0 && x.찬것().size >= x.총칸()
                    운칸(d, x, 지금칸 = i == 본, 끝 = 다끝 && i != 본, 뺄수 = i == 본 && n > 1,
                        Modifier.가로끌기(판, i).눌림 { 발자취.적기("${x.이름} 칸 고름"); 고름(i) }
                            .semantics { contentDescription = "${x.이름} ${x.찬것().size}/${x.총칸()}세트" })
                }
                Spacer(Modifier.width(높이.높게))   // [＋] 자리
            }
            // 붙박이 [＋] — 칸이 적으면 마지막 칸 바로 뒤, 넘치면 오른쪽 끝에 붙는다 (시안 position:sticky · v14)
            Box(Modifier.matchParentSize()) {
                Box(
                    Modifier.offset { IntOffset(더칸자리(n, 걸음, 넘김.value.toFloat(), 폭px, 더px).roundToInt(), 0) }
                        .width(높이.높게).fillMaxHeight()
                        .drawBehind {   // 왼쪽 4 — 밑으로 들어가는 칸을 가른다 (box-shadow −4 면2)
                            val w = 간격.아주좁게.toPx()
                            drawRect(c.면2, topLeft = Offset(-w, 0f), size = Size(w, size.height))
                        }
                        .clip(RoundedCornerShape(모서리.작게)).background(c.면)
                        .drawWithContent {
                            drawContent()
                            val r = 모서리.작게.toPx(); val t = 선굵기.보통.toPx(); val 점 = 운치수.점선.toPx()
                            drawRoundRect(c.속선, topLeft = Offset(t / 2, t / 2), size = Size(size.width - t, size.height - t), cornerRadius = CornerRadius(r, r),
                                style = Stroke(t, pathEffect = PathEffect.dashPathEffect(floatArrayOf(점, 점))))
                        }
                        .눌림 { 발자취.적기("운동 중 종목 넣기 열기"); 넣기() }.semantics { contentDescription = "종목 넣기" },
                    contentAlignment = Alignment.Center,
                ) { Text("＋", style = 글꼴.보통(크기.제목, FontWeight.Bold), color = c.강조) }
                // 보는 칸 ✕ — 칸 오른쪽 위 꼭짓점이 가운데. [＋] 위에 그린다. 칸이 [＋] 밑으로 들어가면 · 끄는 동안은 감춘다
                val 뺌px = with(밀도) { 높이.아주낮게.toPx() }
                val 칸끝 = 본 * 걸음 + with(밀도) { 운치수.칸폭.toPx() } - 넘김.value
                val 뺌보임 = n > 1 && 판.원 == null && 칸끝 >= 0f &&
                    칸끝 <= 더칸자리(n, 걸음, 넘김.value.toFloat(), 폭px, 더px) - with(밀도) { 간격.아주좁게.toPx() } + 0.5f
                if (뺌보임) {
                    Box(
                        Modifier.offset { IntOffset((칸끝 - 뺌px / 2).roundToInt(), (-뺌px / 2).roundToInt()) }
                            .size(높이.아주낮게)
                            .눌림 { 운종목빼기(상태, 함, 운보기.본) }
                            .semantics { contentDescription = "${S.종목들[본].이름} 운동에서 빼기" },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            Modifier.size(운치수.뺌동그라미).clip(CircleShape).background(c.면).border(선굵기.보통, c.선, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) { Icon(아이콘.닫기, null, Modifier.size(운치수.뺌그림), tint = c.옅음) }
                    }
                }
            }
        }
    }
    }
}

/** 운동 중 종목 빼기 (v21 ①) — 묻지 않고 빼고 아래띠 '○○을 뺐습니다 [되돌리기]'. 체크한 세트가 있어도 종목째 보관했다 그 자리에 되돌린다 */
private fun 운종목빼기(상태: 앱상태, 함: ((운동세션) -> 운결과?) -> Unit, 본: Int) {
    var 뺀: com.slayde.hasenheide.data.뺀종목? = null
    함 { s -> s.종목빼기(본, 운보기.본)?.let { (r, z) -> 뺀 = z; 운결과(r.세션, r.본 ?: 운보기.본) } }
    val z = 뺀 ?: return
    발자취.적기("종목 뺌 · ${z.e.이름}")
    상태.알림.되돌림("운종목뺌", { n -> if (n > 1) "종목 ${n}개를 뺐습니다" else "${조사(z.e.이름, "을", "를")} 뺐습니다" }) {
        발자취.적기("종목 되돌림 · ${z.e.이름}")
        함 { s -> s.종목되돌리기(z, 운보기.본, System.currentTimeMillis()).let { r -> 운결과(r.세션, r.본 ?: 운보기.본) } }
    }
}

/** 아래 종목 칸 하나 (시안 `.운칸`) — 이름 11 두 줄 · n/m · 막대. 보는 칸 = 테 2 강조 · 점멸, 다 끝낸 칸 = 흐림 · 막대 좋음 */
@Composable
private fun 운칸(d: 앱데이터, x: 세션종목, 지금칸: Boolean, 끝: Boolean, 뺄수: Boolean, modifier: Modifier) {
    val c = Local색.current
    val m = x.총칸(); val k = x.찬것().size
    val 모양 = RoundedCornerShape(모서리.작게)
    val 번 = d.같은이름번호(x.종id, x.이름)
    Box(
        modifier.width(운치수.칸폭).alpha(if (끝) 운치수.끝칸흐림 else 1f)
            .clip(모양).background(c.면).점멸바탕(지금칸, 모양)
            .border(if (지금칸) 선굵기.굵게 else 선굵기.보통, if (지금칸) c.강조 else c.선, 모양),
    ) {
        Column(Modifier.padding(start = 간격.좁게, end = 간격.좁게, top = 간격.아주좁게, bottom = 간격.좁게)) {
            Text(
                x.이름, Modifier.fillMaxWidth().height(높이.아주낮게), style = 글꼴.보통(크기.작게, FontWeight.Bold).copy(lineHeight = 운치수.칸이름줄.em),
                color = c.글, maxLines = 2, overflow = TextOverflow.Ellipsis,
            )
            Text("$k/$m", style = 글꼴.보통(크기.작게).copy(fontFeatureSettings = "tnum"), color = c.흐림, maxLines = 1)
            Box(Modifier.padding(top = 간격.아주좁게).fillMaxWidth().height(막대치수.높이).clip(CircleShape).background(c.면2)) {
                Box(Modifier.fillMaxWidth(if (m > 0) k.toFloat() / m else 0f).fillMaxHeight().background(if (끝) c.좋음 else c.강조))
            }
        }
        if (번 > 0) Box(
            Modifier.align(Alignment.TopEnd).padding(
                top = if (뺄수) 운치수.칸번호뺄수위 else 운치수.칸번호위, end = if (뺄수) 운치수.칸번호뺄수옆 else 운치수.칸번호옆,
            ),
        ) { 번호딱지(번) }
    }
}

// ═════════════════════ 맨 아래 단추 넷 ═════════════════════

/** [‹ 12.5%][큰 주 단추 47.5%][오늘 운동/끝내기 27.5% · 빨강][› 12.5%] (v21 ④) — 높이 40 · 틈 8 */
@Composable
private fun 단추줄(S: 운동세션, 본: Int, 이전: () -> Unit, 다음: () -> Unit, 주: () -> Unit, 끝내기: () -> Unit) {
    val c = Local색.current
    val n = S.종목들.size
    BoxWithConstraints(
        Modifier.fillMaxWidth().background(c.면).번호("운6")
            .drawBehind { drawRect(c.선, size = Size(size.width, 선굵기.보통.toPx())) }
            .padding(horizontal = 간격.보통, vertical = 간격.좁게),
    ) {
        val 폭 = 단추폭(maxWidth.value, 간격.좁게.value, 운치수.단추비).map { it.dp }
        Row(horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
            운단추(Modifier.width(폭[0]), 쓸수있음 = 본 > 0, 설명 = "이전 종목", onClick = 이전) {
                Icon(아이콘.칩왼쪽, null, Modifier.size(운치수.단추그림), tint = c.글)
            }
            val 상 = 운주상태(S, 본)
            운단추(Modifier.width(폭[1]), 주요 = true, 설명 = 상.글, onClick = 주) {
                노란점()
                운주글(상.글, Modifier.weight(1f, fill = false))
            }
            운단추(Modifier.width(폭[2]), onClick = 끝내기) {
                Text(
                    "오늘 운동\n끝내기", style = 글꼴.보통(크기.버튼, FontWeight.Bold), color = c.나쁨,
                    textAlign = TextAlign.Center, maxLines = 2, softWrap = false,
                )
            }
            운단추(Modifier.width(폭[3]), 쓸수있음 = 본 < n - 1, 설명 = "다음 종목", onClick = 다음) {
                Icon(아이콘.칩오른쪽, null, Modifier.size(운치수.단추그림), tint = c.글)
            }
        }
    }
}

/** 아래 단추 하나 — 높이 40 · 모서리 8 · 테 속선(주요 = 강조 바탕). 못 누를 때 흐림 0.35 */
@Composable
private fun 운단추(
    modifier: Modifier, 주요: Boolean = false, 쓸수있음: Boolean = true, 설명: String? = null,
    onClick: () -> Unit, content: @Composable RowScope.() -> Unit,
) {
    val c = Local색.current
    val 손 = remember { MutableInteractionSource() }
    val 배 = 눌림배율(손)
    val 모양 = RoundedCornerShape(모서리.작게)
    Row(
        modifier.height(높이.보통).배율(배).alpha(if (쓸수있음) 1f else 운치수.흐린단추)
            .clip(모양).background(if (주요) c.강조 else c.면)
            .then(if (주요) Modifier else Modifier.border(선굵기.보통, c.속선, 모양))
            .then(if (쓸수있음) Modifier.눌림손(손, onClick) else Modifier)
            .then(if (설명 != null) Modifier.semantics { contentDescription = 설명 } else Modifier)
            .padding(horizontal = if (주요) 간격.좁게 else 간격.아주좁게),
        horizontalArrangement = Arrangement.spacedBy(간격.좁게, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

/** 큰 주 단추 글 — 한 줄 15 → 13, 그래도 넘치면 13 두 줄 (… 없음 · v21 ④ `운주맞춤`) */
@Composable
private fun 운주글(글: String, modifier: Modifier) {
    val c = Local색.current
    var 단계 by remember(글) { mutableIntStateOf(0) }
    Text(
        글, modifier, style = 글꼴.보통(if (단계 == 0) 크기.본문 else 크기.버튼, FontWeight.Bold),
        color = c.강조글, maxLines = if (단계 < 2) 1 else 2, softWrap = 단계 >= 2,
        textAlign = if (단계 >= 2) TextAlign.Center else null, overflow = TextOverflow.Clip,
        onTextLayout = { r -> if (r.hasVisualOverflow && 단계 < 2) 단계++ },
    )
}

/** 종목이 하나도 없는 운동 (옛 파일 · 이상한 값) — 넣기 · 끝내기만 */
@Composable
private fun 빈운동(상태: 앱상태) {
    val c = Local색.current
    var 넣기열림 by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(간격.보통), verticalArrangement = Arrangement.spacedBy(간격.좁게)) {
            글("종목이 없습니다", 색 = c.흐림)
            버튼("종목 넣기", { 넣기열림 = true }, Modifier.fillMaxWidth(), 작게 = true)
            버튼("오늘 운동 끝내기", { 상태.바꿈 { dd -> dd.세션?.let { dd.copy(세션 = it.끝냄(System.currentTimeMillis())) } ?: dd } },
                Modifier.fillMaxWidth(), 작게 = true, 글색 = c.나쁨)
        }
        if (넣기열림) 종목넣기시트(
            상태, "종목 넣기", 개수 = { 열쇠 -> 상태.d.세션?.종목들?.count { it.열쇠 == 열쇠 } ?: 0 },
            넣기 = { 열쇠 -> 상태.바꿈 { dd -> dd.세션?.let { s -> dd.copy(세션 = s.종목넣기(dd.세션줄(열쇠), 0).세션) } ?: dd } },
            빼기 = { }, 닫기 = { 넣기열림 = false },
        )
    }
}

/** 상자 안 루틴 이름 — '가슴, 팔' → '가슴, 팔 루틴'. 이름에 이미 '루틴'이 있으면 그대로 (09-21 메모) */
fun 루틴표시(이름: String): String = if (이름.contains("루틴")) 이름 else "$이름 루틴"

// ═════════════════════ 순수 계산 (시험: test/…/ui/WorkoutPortTest.kt) ═════════════════════
// ── 순수 계산 시작 ── 이 아래는 data 만 쓴다(화면 부품 없음). 합칠 때 data/ 로 옮길 수 있다

/** 아직 할 세트가 남은 종목 — '여기까지'(마감)한 종목은 끝난 것으로 본다 (데이터 `종목빼기` 의 남음과 같다) */
internal fun 운남음(e: 세션종목): Boolean = !e.마감 && e.덜한가()

/** 그 종목의 첫 빈 세트 칸 (시안 `지금k = 세트.findIndex(!완료)`) — 없으면 -1 */
internal fun 첫빈칸(e: 세션종목): Int = (0 until e.총칸()).firstOrNull { e.기록.칸(it) == null } ?: -1

/** 보는 칸에서 갈 다음 종목 — 뒤쪽의 남은 종목 → 앞쪽 → 없으면 -1 (시안 `다음종목으로` · `휴식건너뛰기`) */
internal fun 다음남은(S: 운동세션, 본: Int): Int =
    S.종목들.indices.firstOrNull { it > 본 && 운남음(S.종목들[it]) }
        ?: S.종목들.indices.firstOrNull { it != 본 && 운남음(S.종목들[it]) } ?: -1

/** 쉬는 종목 번호 — 옛 기록(-1)은 지금 종목. 쉬는 중이 아니면 null */
internal fun 쉬는종목(S: 운동세션): Int? = S.휴식?.let { if (it.종목 >= 0) it.종목 else S.i }

/** 행동의 결과 — 새 세션 · 새 보는 칸 · 휴식을 건너뛰었나(업적 '연속 건너뛰기') */
internal data class 운결과(val 세션: 운동세션, val 본: Int, val 건너뜀: Boolean = false)

/** 큰 주 단추 (v21 ④) — 문구 차례: 마무리 > 다음 종목 > 건너뛰기 > 세트 완료하기 */
internal enum class 운주(val 글: String) {
    마무리("운동 마무리"), 다음종목("다음 종목으로 넘어가기"), 건너뛰기("건너뛰기"), 완료("세트 완료하기")
}

internal fun 운주상태(S: 운동세션, 본: Int): 운주 {
    if (S.종목들.none(::운남음)) return 운주.마무리
    val e = S.종목들.getOrNull(본)
    if (e == null || !운남음(e)) return 운주.다음종목
    if (S.휴식 != null) return 운주.건너뛰기
    return 운주.완료
}

/** 쉼 게이지 문구 (v17 ③ · v18 ③) — 차례: 마무리 > 다음 운동 > 건너뛰기 */
internal enum class 쉼종류(val 글: String) {
    마무리("마무리하고 운동 보고서 화면으로 넘어가기"), 넘김("건너뛰고 다음 운동으로 넘어가기"), 건너뛰기("건너뛰기")
}

/** 쉬는 줄(j)의 게이지 문구 — 모두 끝났으면 마무리, 보는 종목을 다 끝냈고 남은 종목이 있으면 넘김 */
internal fun 쉼글(S: 운동세션, 본: Int, j: Int): 쉼종류 {
    if (S.종목들.none(::운남음)) return 쉼종류.마무리
    val e = S.종목들.getOrNull(j) ?: return 쉼종류.건너뛰기
    return if (본 == j && !운남음(e) && S.종목들.indices.any { it != j && 운남음(S.종목들[it]) }) 쉼종류.넘김 else 쉼종류.건너뛰기
}

/**
 * 세트 체크 (시안 `체크`) — 데이터 체크 그대로, 다만 v18 ③ "마지막 세트도 똑같이 휴식 게이지":
 * 모든 세트를 끝내도 바로 마무리 화면으로 가지 않고 그 줄에서 쉰다(게이지 '마무리하고 운동 보고서 화면으로 넘어가기').
 * 범위 밖 번호는 아무것도 하지 않는다
 */
internal fun 운체크(S: 운동세션, j: Int, k: Int, 지금: Long): 운동세션 {
    val e = S.종목들.getOrNull(j) ?: return S
    if (k < 0 || k >= e.총칸()) return S
    val t = S.체크(j, k, 지금)
    if (!t.끝화면 || S.끝화면) return t
    val kk = (0 until k).firstOrNull { e.기록.칸(it) == null } ?: k
    // 다음i = 그 줄 — 휴식이 끝나도 데이터 '다음으로' 가 마무리 화면으로 넘기지 않고 이 자리에 머문다 (시안: 큰 단추 '운동 마무리')
    return t.copy(끝화면 = false, 끝시각 = S.끝시각).휴식시작(kk, 지금, 다음i = j, 다음s = kk)
}

/** 슈퍼세트 — 지금 · 쉬는 줄이 보는 칸과 같은 묶음 안 다른 종목이면 보는 칸이 따라간다 (묶음이 아니면 그대로) */
internal fun 본따라감(S: 운동세션, 본: Int): Int {
    val 갈 = 쉬는종목(S) ?: S.i
    return if (갈 != 본 && 갈 in S.종목들.indices && 본 in S.종목들.indices && 갈 in S.식구(본)) 갈 else 본
}

/** 휴식 건너뛰기 (시안 `휴식건너뛰기`) — 다음 세트로. 그 종목 세트를 다 끝낸 뒤의 휴식이었으면 보는 칸을 다음 남은 종목으로 */
internal fun 휴식건너뛰기(S: 운동세션, 본: Int, 지금: Long): 운결과 {
    if (S.휴식 == null) return 운결과(S, 본)
    val 쉰 = 쉬는종목(S)
    val t = S.다음으로(지금)
    val 끝남 = t.종목들.getOrNull(본)?.let { !운남음(it) } == true
    val 갈 = if (쉰 == 본 && 끝남) 다음남은(t, 본) else -1
    return 운결과(t, if (갈 >= 0) 갈 else 본, 건너뜀 = true)
}

/** '다음 종목으로 넘어가기' (v21 ④) — 보는 종목에서 쉬던 중이면 휴식을 끝내고, 보는 칸을 다음 남은 종목(뒤 → 앞)으로 */
internal fun 다음종목가기(S: 운동세션, 본: Int, 지금: Long): 운결과 {
    val t = if (S.휴식 != null && 쉬는종목(S) == 본) S.다음으로(지금) else S
    val 갈 = 다음남은(t, 본)
    return 운결과(t, if (갈 >= 0) 갈 else 본)
}

/** 큰 주 단추를 눌렀을 때 — 누른 순간의 세션으로 무엇을 할지 다시 정한다 (빠르게 두 번 눌러도 체크 → 체크 풀기가 되지 않는다) */
internal fun 운주누름(S: 운동세션, 본: Int, 지금: Long): 운결과 = when (운주상태(S, 본)) {
    운주.마무리 -> 운결과(S.끝냄(지금), 본)
    운주.다음종목 -> 다음종목가기(S, 본, 지금)
    운주.건너뛰기 -> 휴식건너뛰기(S, 본, 지금)
    운주.완료 -> 운체크(S, 본, 첫빈칸(S.종목들[본]), 지금).let { 운결과(it, 본따라감(it, 본)) }
}

/** 지운 세트 — 되돌리기에 쓴다 (시안 `U.운지움`). 그 칸이 목록에 있었는지까지 적어 빼기 → 되돌리기 = 원래 그대로 */
internal data class 지운세트(
    val j: Int, val 열쇠: String, val k: Int,
    val 기록: 세트?, val 예정: 세트?, val 휴: Int?,
    val 기록있음: Boolean, val 예정있음: Boolean, val 휴있음: Boolean,
    val 세트수: Int,
    /** 지우기 전 · 뒤의 지금 자리 */
    val i: Int, val s: Int, val 무게: Double, val 횟수: Int, val 후i: Int, val 후s: Int,
    /** 지운 줄에서 쉬던 휴식 (종목 번호는 실제 번호) */
    val 휴식: 휴식중?,
    val 세션시작: Long,
)

/**
 * 세트 지우기 (시안 `세트지우기`) — 데이터 `세트삭제` + 두 가지:
 *  · 지운 줄 아래에서 쉬던 휴식(· 슈퍼세트가 돌아갈 다음s)의 번호를 하나 당긴다 (데이터는 그대로 둬서 엉뚱한 줄에서 돌았다)
 *  · 지금 세트를 지웠으면 그 자리로 올라온 줄의 값을 입력 값으로 (데이터는 지운 줄 값이 남았다)
 * 종목에 한 줄만 남았으면 null (지우지 않는다)
 */
internal fun 운세트지우기(S: 운동세션, j: Int, k: Int): Pair<운동세션, 지운세트>? {
    val e = S.종목들.getOrNull(j) ?: return null
    if (k < 0 || k >= e.총칸() || e.총칸() <= 1) return null
    val 쉰 = 쉬는종목(S)
    val 지운쉼 = S.휴식?.takeIf { 쉰 == j && it.k == k }?.copy(종목 = j)
    var t = S.세트삭제(j, k)
    t.휴식?.let { h ->
        var h2 = h
        if (쉬는종목(t) == j && h.k > k) h2 = h2.copy(k = h.k - 1)
        if (h2.다음i == j && (h2.다음s ?: -1) > k) h2 = h2.copy(다음s = h2.다음s!! - 1)
        t = t.copy(휴식 = h2)
    }
    if (t.i == j && S.i == j && S.s == k) t = t.자리로(j, k).copy(끝화면 = t.끝화면, 끝시각 = t.끝시각)
    val z = 지운세트(
        j, e.열쇠, k, e.기록.칸(k), e.예정값.칸(k), e.휴식들.칸(k),
        k in e.기록.indices, k in e.예정값.indices, k in e.휴식들.indices, e.세트,
        S.i, S.s, S.무게, S.횟수, t.i, t.s, 지운쉼, S.시작시각,
    )
    return t to z
}

/** 지운 세트 되돌리기 — 그 자리에 다시 끼운다. 다른 운동이면 그대로. 그 사이 순서가 바뀌었으면 같은 종목(열쇠)을 찾아 끼운다 */
internal fun 세트되살리기(S: 운동세션, z: 지운세트, 지금: Long): 운동세션 {
    if (z.세션시작 != S.시작시각) return S
    val j = if (S.종목들.getOrNull(z.j)?.열쇠 == z.열쇠) z.j else S.종목들.indexOfFirst { it.열쇠 == z.열쇠 }
    if (j < 0) return S
    val e = S.종목들[j]
    fun <T> List<T?>.끼움(있음: Boolean, v: T?): List<T?> =
        if (!있음) this else toMutableList().also { m -> while (m.size < z.k) m.add(null); m.add(min(z.k, m.size), v) }
    val 새e = e.copy(
        기록 = e.기록.끼움(z.기록있음, z.기록), 예정값 = e.예정값.끼움(z.예정있음, z.예정), 휴식들 = e.휴식들.끼움(z.휴있음, z.휴),
        세트 = e.세트 + (z.세트수 - max(1, z.세트수 - 1)),
    )
    var t = S.copy(종목들 = S.종목들.mapIndexed { x, y -> if (x == j) 새e else y })
    t = when {
        j == z.j && S.i == z.후i && S.s == z.후s -> t.copy(i = z.i, s = z.s, 무게 = z.무게, 횟수 = z.횟수)
        S.i == j && S.s >= z.k -> t.copy(s = S.s + 1)
        else -> t
    }
    val h = t.휴식
    t = if (h != null) {
        var h2 = h
        if (쉬는종목(t) == j && h.k >= z.k) h2 = h2.copy(k = h.k + 1)
        if (h2.다음i == j && (h2.다음s ?: -1) >= z.k) h2 = h2.copy(다음s = h2.다음s!! + 1)
        t.copy(휴식 = h2)
    } else if (z.휴식 != null && z.휴식.끝시각 > 지금) t.copy(휴식 = z.휴식.copy(종목 = j)) else t
    return t
}

/** 오늘 이 종목의 값 — 맨몸은 최고 횟수, 아니면 최고 추정 1RM. 워밍업은 뺀다. 한 세트도 없으면 null (시안 `오`) */
internal fun 운오늘값(세트들: List<세트>, 맨: Boolean): Double? {
    val l = 기록세트(세트들)
    if (l.isEmpty()) return null
    return if (맨) l.maxOf { it.r.toDouble() } else l.maxOf { 일RM(it.w, it.r) }
}

/**
 * 지난 기록 중 최고 · 1주(7일 전 ~ 전날) 최고 (시안 `운최고`) — 오늘 앞 기록만. 같은 종목은 열쇠(종id · 옛 기록은 이름)로.
 * 결과 = (최고, 1주) · 없으면 null
 */
internal fun 운최고(기록: Map<String, 날기록>, 열쇠: String, 오늘: String, 맨: Boolean): Pair<Double?, Double?> {
    val 주앞 = 날더하기(오늘, -7)
    var 최고: Double? = null
    var 주: Double? = null
    for ((키, r) in 기록) {
        val 날 = 날짜만(키)
        if (날 >= 오늘) continue
        for (x in r.종목들) {
            if (x.열쇠 != 열쇠) continue
            for (s in 기록세트(x.세트들)) {
                val v = if (맨) s.r.toDouble() else 일RM(s.w, s.r)
                if (최고 == null || v > 최고) 최고 = v
                if (날 >= 주앞 && (주 == null || v > 주)) 주 = v
            }
        }
    }
    return 최고 to 주
}

/** 오늘 − 지난 값, 0.5 단위로 반올림 (시안 `Math.round((오-기)*2)/2`). 어느 쪽이든 없으면 null */
internal fun 운차(오: Double?, 기: Double?): Double? = if (오 == null || 기 == null) null else Math.round((오 - 기) * 2) / 2.0

/** CSS 격자 `minmax(최소, fr)` 칸 폭 — 남는 폭을 fr 비율로 나누되, 최소보다 작아지는 칸은 최소로 묶고 나머지를 다시 나눈다 */
internal fun 격자폭(남는: Float, 최소: List<Float>, fr: List<Float>): List<Float> {
    val 고정 = BooleanArray(fr.size)
    while (true) {
        val 남 = 남는 - fr.indices.filter { 고정[it] }.sumOf { 최소[it].toDouble() }.toFloat()
        val 합 = fr.indices.filter { !고정[it] }.sumOf { fr[it].toDouble() }.toFloat()
        if (합 <= 0f) return 최소
        val 단 = (남 / 합).coerceAtLeast(0f)
        val 모자람 = fr.indices.filter { !고정[it] && fr[it] * 단 < 최소[it] }
        if (모자람.isEmpty()) return fr.indices.map { if (고정[it]) 최소[it] else fr[it] * 단 }
        모자람.forEach { 고정[it] = true }
    }
}

/** 맨 아래 단추 넷의 폭 — 줄 안 폭에서 틈 × 3 을 뺀 나머지를 비율대로 (v21 ④ 12.5 : 47.5 : 27.5 : 12.5) */
internal fun 단추폭(줄: Float, 틈: Float, 비: List<Float>): List<Float> = 비.map { (줄 - 틈 * (비.size - 1)).coerceAtLeast(0f) * it }

/** 칸 줄을 어디로 넘길까 — 보는 칸이 맨 앞. 앞에 칸이 있으면 ‹ 자리만큼 비운다. 0 ~ 최대 안으로 */
internal fun 칸줄목표(본: Int, 걸음: Float, 앞비움: Float, 최대: Float): Float =
    if (본 <= 0) 0f else (본 * 걸음 - 앞비움).coerceIn(0f, 최대.coerceAtLeast(0f))

/** 붙박이 [＋] 의 왼쪽 자리 — 마지막 칸 뒤(틈 다음), 넘치면 줄 오른쪽 끝 (시안 position:sticky; right:0) */
internal fun 더칸자리(n: Int, 걸음: Float, 넘김: Float, 폭: Float, 더폭: Float): Float = min(n * 걸음 - 넘김, 폭 - 더폭)

// ── 순수 계산 끝 ──
