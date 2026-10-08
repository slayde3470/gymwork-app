package com.slayde.hasenheide.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.slayde.hasenheide.data.근육계산
import com.slayde.hasenheide.data.종목부위
import com.slayde.hasenheide.data.분초
import com.slayde.hasenheide.ui.theme.휴식칸값
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slayde.hasenheide.data.근육표
import com.slayde.hasenheide.data.무게글
import com.slayde.hasenheide.data.앱데이터
import com.slayde.hasenheide.data.이름추천
import com.slayde.hasenheide.data.종목
import com.slayde.hasenheide.data.종목근육
import com.slayde.hasenheide.data.종목기본세트
import com.slayde.hasenheide.data.종목세트
import com.slayde.hasenheide.data.종목사전
import com.slayde.hasenheide.data.종목세트최대
import com.slayde.hasenheide.data.카테고리지우기
import com.slayde.hasenheide.data.같은이름번호
import com.slayde.hasenheide.data.칸
import com.slayde.hasenheide.data.플랜
import com.slayde.hasenheide.data.플랜줄정리
import com.slayde.hasenheide.data.플랜표
import com.slayde.hasenheide.data.속도출처글
import com.slayde.hasenheide.data.처방글
import com.slayde.hasenheide.data.회처방
import com.slayde.hasenheide.data.회표
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.글꼴
import com.slayde.hasenheide.ui.theme.그림칸
import com.slayde.hasenheide.ui.theme.높이
import com.slayde.hasenheide.ui.theme.모서리
import com.slayde.hasenheide.ui.theme.부품치수
import com.slayde.hasenheide.ui.theme.선굵기
import com.slayde.hasenheide.ui.theme.움직임
import com.slayde.hasenheide.ui.theme.크기
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt

/** 종목 이름을 바꾸면 루틴과 지난 기록의 이름도 함께 바꾼다 (기록이 끊기지 않게) — 다른 파일이 부를 수 있어 그대로 둔다 */
fun 앱데이터.종목이름바꿈(옛: String, 새: String): 앱데이터 {
    if (옛 == 새 || 새.isBlank() || 종목표.any { it.이름 == 새 }) return this
    return copy(
        종목표 = 종목표.map { if (it.이름 == 옛) it.copy(이름 = 새) else it },
        루틴들 = 루틴들.map { r -> r.copy(종목 = r.종목.map { if (it.이름 == 옛) it.copy(이름 = 새) else it }) },
        기록 = 기록.mapValues { (_, rec) -> rec.copy(종목들 = rec.종목들.map { if (it.이름 == 옛) it.copy(이름 = 새, 묶음 = it.묶음?.split("+")?.map { x -> if (x == 옛) 새 else x }?.sorted()?.joinToString("+")) else it }) },
        세션 = 세션?.let { S -> S.copy(종목들 = S.종목들.map { if (it.이름 == 옛) it.copy(이름 = 새) else it }) },
    )
}

/** 이 화면만 쓰는 치수 — Theme 에 없는 값 (보고서 '공용 고칠 것' — 합칠 때 부품치수로 옮긴다) */
internal object 종목치수 {
    val 이름글 = 14.sp   // 10-07 홍겸 님: 종목명 1 작게 · 굵게 빼기 (U2-1 단계 밖 — 지침에 올릴 값)
    val 펼침그림 = 18.dp // 10-07 홍겸 님: 접기·펼치기 그림을 다른 곳(펼침단추 · U3-6 기본 18)과 같게
    val 뜸자리 = 64.dp   // 오른쪽 아래 떠 있는 [새 종목 만들기] 에 마지막 줄이 가리지 않게 (단추 40 + 아래 12 + 12)
    val 번호칸 = 16.dp   // 시안 .종세트 첫 칸 16 (세트 번호)
    val 지움칸 = 28.dp   // 시안 .종지움 28
    val 지움그림 = 16.dp // 시안 .종지움 svg 16 (U3-6 작게)
    val 값그림 = 18.dp   // − ＋ (U3-6 기본 · Parts 값칸과 같다)
    const val 열무게 = 83f   // 시안 grid 83fr · 70fr · 79fr
    const val 열횟수 = 70f
    const val 열휴식 = 79f
    /** 10-09 홍겸 님: 종목을 펼치면 펼친 상자 밖을 어둡게 — 시트 가림막(Common.kt `시트`)과 같은 값 */
    val 가림 = Color.Black.copy(alpha = 0.35f)   // [11_UI지침에 올릴 값]
}

/**
 * 종목 탭 (시안 v18 C ①② · v19 D ② · v20 ①②③ `종목탭` · `종목칸`).
 *  · 맨 위 띠 '종목'(가운데) → [+ 새 종목 만들기] → 칩 필터([전체] + 카테고리 · 플랜만 남은 종목이 있으면 '기타')
 *  · 카테고리마다 [작은 이름표](전체일 때만) + 상자 2열 격자. 펼친 상자는 줄 전체, 빈 칸은 뒤 상자가 채운다(dense)
 *  · 접힌 상자 = 이름(+같은 이름 번호 · [플랜] 딱지) · ▾ 만. 펼치면 [사진 칸][이름 · 곁 · ▾ / 주동근 / 협응근][편집] →
 *    플랜 카드(그 종목의 플랜마다) → 기본 세팅 세트 줄 → 넣은 사진 줄(있을 때만 · 두 번 눌러 지우기)
 */
@Composable
fun 종목화면(상태: 앱상태) {
    val c = Local색.current
    val d = 상태.d
    var 고른칸 by remember { mutableStateOf("전체") }
    var 펼친 by remember { mutableStateOf<String?>(null) }
    var 카테고리시트 by remember { mutableStateOf(false) }
    // 새 종목 시트 — null 닫힘 · "" 새 종목 · 그 밖 = 편집할 종목 id
    var 새시트 by remember { mutableStateOf<String?>(null) }

    val 판 = d.종목탭판(고른칸)
    // 10-07 홍겸 님: 꾹 눌러 끌어 종목 순서 바꾸기 — 같은 카테고리 안에서만 (넣기 시트와 같은 차례 = 종목표 차례)
    val 넘김 = rememberScrollState()
    val 끌판 = remember격자끌기판({ a, b, 뒤 ->
        발자취.적기("종목 탭 · 순서 옮김")
        상태.바꿈 { it.copy(종목표 = 종목옮김(it.종목표, a, b, 뒤)) }
    })
    // 10-09 홍겸 님: 종목을 펼치면 펼친 상자 말고 나머지를 어둡게 (시트 가림막과 같은 값) · 어두운 곳을 누르면 접힌다.
    //  펼친 상자 자리([구멍])는 스크롤 안쪽 좌표로 재서(스크롤해도 안 변한다) 가림막 네 조각이 둘러싼다. 아래 탭줄은 App 이 그려서 못 어둡게 한다
    val 구멍틀 = remember { arrayOfNulls<LayoutCoordinates>(2) }   // [0] 스크롤 속 칸 · [1] 펼친 상자
    var 구멍 by remember { mutableStateOf<Rect?>(null) }
    fun 구멍재기() {
        val 속 = 구멍틀[0]; val 상 = 구멍틀[1]
        구멍 = if (속 != null && 상 != null && 속.isAttached && 상.isAttached) 속.localBoundingBoxOf(상, clipBounds = false) else null
    }
    LaunchedEffect(펼친) { if (펼친 == null) { 구멍틀[1] = null; 구멍 = null } }
    val 접기 = { 펼친 = null }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth()) {
                머리띠("종목", Modifier.번호("종1"), 오른쪽 = {
                    // 앱에만 있는 카테고리 관리 — 시안 띠에는 없다 (보고서 '시안과 다르게 한 것')
                    아이콘버튼(아이콘.설정, "카테고리 관리", { 카테고리시트 = true }, 칠함 = false, 색 = c.강조글)
                })
                if (펼친 != null) Box(Modifier.matchParentSize().background(종목치수.가림).눌림(접기))
            }
            당겨새로고침({ }, Modifier.weight(1f)) {
                BoxWithConstraints(Modifier.fillMaxSize()) {
                    val 판높이 = maxHeight
                    Column(Modifier.fillMaxSize().이름칸밖누름().격자끌기틀(끌판, 넘김).verticalScroll(넘김)) {
                        // 스크롤 속 칸 하나 — 목록이 짧아도 화면 끝까지 어둡게 하려고 판 높이 이상으로 둔다
                        Box(Modifier.fillMaxWidth().heightIn(min = 판높이).onGloballyPositioned { 구멍틀[0] = it; 구멍재기() }) {
                            Column(
                                Modifier.fillMaxWidth()
                                    .padding(start = 간격.보통, end = 간격.보통, top = 간격.보통, bottom = 종목치수.뜸자리),
                                verticalArrangement = Arrangement.spacedBy(간격.좁게),
                            ) {
                                칩줄(listOf("전체") + 판.칸들, 판.고름, { 고른칸 = it })
                                종목묶음들(상태, 판, 끌판, 펼친, { k -> 펼친 = if (펼친 == k) null else k },
                                    펼침틀 = { 구멍틀[1] = it; 구멍재기() })
                            }
                            val h = 구멍
                            if (펼친 != null && h != null) 펼침가림막(h, 접기, Modifier.matchParentSize())
                        }
                    }
                }
            }
        }
        격자끌기이름표(끌판)
        // 10-07 홍겸 님: [+ 새 종목 만들기] 오른쪽 아래 고정 · 파란 상자 흰 글 (루틴 화면 떠 있는 단추와 같은 모양)
        //  10-09 홍겸 님: 펼쳤을 때는 이 단추도 어둡게 — 누르면 접힌다 (새 종목은 열지 않는다)
        Box(Modifier.align(Alignment.BottomEnd).padding(간격.보통)) {
            버튼("+ 새 종목 만들기", { if (새시트 == null) 새시트 = "" }, Modifier.가림덮음(펼친 != null), 주요 = true)
            if (펼친 != null) Box(Modifier.matchParentSize().눌림(접기))
        }
        if (카테고리시트) 카테고리관리(상태) { 카테고리시트 = false }
        새시트?.let { 열린 ->
            val 편집 = if (열린.isEmpty()) null else d.종목표.firstOrNull { it.id == 열린 }
            if (열린.isNotEmpty() && 편집 == null) { LaunchedEffect(열린) { 새시트 = null } }
            else key(열린) {
                새종목시트(상태, 닫기 = { 새시트 = null }, 겹 = 1, 저장 = { e ->
                    if (편집 != null) 펼친 = e.id
                    if (고른칸 != "전체" && 고른칸 != e.칸) 고른칸 = e.칸
                }, 편집 = 편집)
            }
        }
        플랜고치기자리(상태)   // 펼친 상자 플랜 카드의 [변경] — 시트는 화면 전체를 덮도록 여기서
    }
}

/**
 * 넣기 손 (10-08 홍겸 님: 루틴 · 운동 중 [운동 종목 추가] 시트는 종목 탭 목록을 **그대로** 쓴다 — 다른 점은 상자마다 체크 상자 하나).
 * [수] 지금 들어 있는 개수 · [넣기] 하나 넣기 · [빼기] 하나 빼기 · [됨] 체크 상자를 달 수 있나 · [플랜카드] 펼쳤을 때 플랜 카드를 보이나
 */
internal class 종목넣기손(
    val 수: (종목칸값) -> Int,
    val 넣기: (종목칸값) -> Unit,
    val 빼기: (종목칸값) -> Unit,
    val 됨: (종목칸값) -> Boolean,
    val 플랜카드: Boolean,
)

/** 카테고리 묶음들 — 종목 탭과 [운동 종목 추가] 시트가 같이 쓴다 (10-08 홍겸 님 — 두 목록은 같은 화면이어야 한다) */
@Composable
internal fun 종목묶음들(
    상태: 앱상태, 판: 종목판, 끌판: 격자끌기판, 펼친: String?, 펼침: (String) -> Unit, 넣기: 종목넣기손? = null,
    /** 10-09 홍겸 님: 펼친 상자의 자리를 알려 준다 (종목 탭이 그 둘레를 어둡게) — 넣기 시트는 안 쓴다 */
    펼침틀: ((LayoutCoordinates) -> Unit)? = null,
) {
    val c = Local색.current
    if (판.묶음.isEmpty()) 글("없음", 크기값 = 크기.조금작게, 색 = c.옅음)
    판.묶음.forEach { (칸이름, l) ->
        key(칸이름) {
            Column(verticalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                if (판.고름 == "전체") 이름표(칸이름, Modifier.padding(start = 간격.아주좁게, top = 간격.아주좁게))
                종목격자(상태, l, 칸이름, 끌판, 펼친, 펼침, 넣기, 펼침틀)
            }
        }
    }
}

/** 2열 격자 — 접힌 상자는 한 칸, 펼친 상자는 줄 전체. 줄마다 접힌 이름이 두 줄이면 옆 상자도 같은 높이 */
@Composable
private fun 종목격자(상태: 앱상태, l: List<종목칸값>, 무리: String, 끌판: 격자끌기판, 펼친: String?, 펼침: (String) -> Unit, 넣기: 종목넣기손?, 펼침틀: ((LayoutCoordinates) -> Unit)?) {
    val 줄들 = 격자줄(l.map { it.열쇠 == 펼친 })
    val 두줄높이 = with(LocalDensity.current) { (크기.본문 * 1.4f * 2).toDp() }
    Column(verticalArrangement = Arrangement.spacedBy(간격.좁게)) {
        줄들.forEach { 줄 ->
            val 첫 = l[줄[0]]
            key(첫.열쇠) {
                if (줄.size == 1 && 첫.열쇠 == 펼친) 종목상자(상태, 첫, true, Modifier.fillMaxWidth().끌기칸(끌판, 첫, 무리).then(if (펼침틀 != null) Modifier.onGloballyPositioned { 펼침틀(it) } else Modifier), 0.dp, 끌판, 펼침, 넣기)
                else {
                    val 높이맞춤 = if (줄.any { 이름줄수(l[it].이름) > 1 }) 두줄높이 else 0.dp
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
                        줄.forEach { i -> key(l[i].열쇠) { 종목상자(상태, l[i], false, Modifier.weight(1f).끌기칸(끌판, l[i], 무리), 높이맞춤, 끌판, 펼침, 넣기) } }
                        if (줄.size == 1) Box(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/** 상자 자리 · 그리기 — 종목표에 있는 종목만(플랜만 남은 '기타' 상자는 못 끈다). 잡는 손은 상자 머리([종목상자]) */
private fun Modifier.끌기칸(끌판: 격자끌기판, x: 종목칸값, 무리: String): Modifier =
    this.격자끌기자리(끌판, x.종목?.id ?: x.열쇠, 무리, 켬 = x.종목 != null)

/**
 * 종목 상자 하나 (시안 `종목칸`). 10-08 홍겸 님: 펼친 상자 = [사진][이름 · ▾][삭제] → 기본 세팅(세트 · 무게 · 횟수 · 휴식)
 * → 근육 → [저장][취소]. 접으면 고치던 것은 버린다(= 취소).
 * 10-09 홍겸 님: 근육 = 그림 2장(글 없음 · MuscleCards.kt). 플랜이 있는 종목은 [사진][이름 · 0/139회 · ∧] → 플랜 카드
 * (게이지 → 다음 측정 → 근육 그림 2장 → [변경][지우기]) 만 — 기본 세팅 · [저장][취소] · [삭제] 는 없다 (플랜이 세팅한다).
 * [넣기] 가 있으면(종목 넣기 시트) 오른쪽 끝에 체크 상자 — 누르면 넣기 / 들어 있으면 하나 빼기 · 꾹 = 하나 더
 */
@Composable
private fun 종목상자(상태: 앱상태, x: 종목칸값, 펼: Boolean, modifier: Modifier, 이름높이: androidx.compose.ui.unit.Dp, 끌판: 격자끌기판, 펼침: (String) -> Unit, 넣기: 종목넣기손?) {
    val c = Local색.current
    val d = 상태.d
    val t = x.종목
    val 플 = d.상자플랜(x)
    val 번 = if (t != null) 같은이름번호(d.종목표, t.id, t.이름) else 0
    val 곁 = if (플.size > 1) "플랜 ${플.size}개" else ""   // 10-08 홍겸 님: '플랜 가능' 글은 없앴다
    var 이름고침 by remember(x.열쇠, 펼) { mutableStateOf(false) }   // 10-08 홍겸 님: 펼친 상자에서 이름을 누르면 그 자리에서 고친다
    val 모양 = RoundedCornerShape(모서리.작게)
    val 수 = if (넣기 != null && 넣기.됨(x)) 넣기.수(x) else -1   // -1 = 체크 상자 없음
    val 들어감 = 수 > 0
    // 10-09 홍겸 님: 플랜이 있는 종목(플랜 카드가 보이는 곳)의 펼친 상자는 플랜이 세팅한다 — 기본 세팅 · [저장][취소] · [삭제] 없음
    val 플랜모양 = 펼 && 플.isNotEmpty() && (넣기 == null || 넣기.플랜카드)
    Column(
        modifier.clip(모양).background(if (들어감) c.강조옅음 else c.면)
            .border(선굵기.보통, if (들어감) c.강조 else if (펼) c.속선 else c.선, 모양).padding(간격.좁게),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(간격.좁게), verticalAlignment = Alignment.Top) {
            if (펼 && t != null) 사진넣는칸(상태, t)
            // 10-07 꾹 눌러 끌기 — 손은 누름보다 안쪽(먼저 받아야 끊기지 않는다). 꾹 누르고 떼어도 펼치지 않게 길게 누름은 빈 동작
            Column(Modifier.weight(1f).눌림길게({ 펼침(x.열쇠) }, { }).격자끌기손(끌판, x.종목?.id ?: x.열쇠, x.이름, 켬 = x.종목 != null)) {
                Row(
                    Modifier.fillMaxWidth().heightIn(min = maxOf(높이.아주낮게, 이름높이)),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.아주좁게),
                ) {
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        if (펼 && t != null && 이름고침) 종목이름고침칸(상태, t, { 이름고침 = false }, 펼침, Modifier.weight(1f))
                        else {
                            if (펼 && t != null) Box(
                                Modifier.weight(1f, fill = false).heightIn(min = 높이.아주낮게)
                                    .semantics(mergeDescendants = true) { contentDescription = "${x.이름} 이름 고치기" }
                                    .눌림 { 이름고침 = true },
                                contentAlignment = Alignment.CenterStart,
                            ) { 이름맞춤(x.이름, Modifier, 바탕크기 = 종목치수.이름글, 굵기 = FontWeight.Medium) }
                            else 이름맞춤(x.이름, Modifier.weight(1f, fill = false), 바탕크기 = 종목치수.이름글, 굵기 = FontWeight.Medium)
                            if (번 > 0 || 플.isNotEmpty()) Row(Modifier.offset(x = 부품치수.딱지겹침), horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                                번호딱지(번)
                                if (플.isNotEmpty()) 플랜딱지()
                            }
                        }
                    }
                    if (펼 && 곁.isNotEmpty()) 글(곁, 크기값 = 크기.작게, 색 = c.옅음)
                    // 10-09 홍겸 님: '0/139회' 는 접기(∧) 바로 왼쪽 (플랜 카드 머리에서 옮겼다). 플랜이 둘 이상이면 카드마다 쓴다
                    if (플랜모양 && 플.size == 1) 글(플랜횟수글(상태, 플[0]), 크기값 = 크기.아주작게, 색 = c.옅음)
                    val 돌림 by animateFloatAsState(if (펼) 180f else 0f, tween(움직임.펼침), label = "접힘표")
                    Icon(아이콘.아래, if (펼) "접기" else "펼치기", Modifier.size(종목치수.펼침그림).graphicsLayer { rotationZ = 돌림 }, tint = c.옅음)
                }
            }
            // 10-08 홍겸 님: [편집] 자리 = [삭제] (묻지 않고 지우고 되돌리기 띠 · U5-4)
            if (펼 && t != null && !플랜모양) 버튼("삭제", { 종목삭제(상태, t) }, 낮게 = true, 글색 = c.나쁨)   // 10-09 홍겸 님: 플랜 종목은 삭제 없음
            // 10-08 홍겸 님: 펼친 상자 머리에는 체크 상자를 두지 않는다 (접힌 줄에만)
            if (!펼 && 넣기 != null && 수 >= 0) 넣기체크(x.이름, 수,
                누름 = { if (넣기.수(x) > 0) 넣기.빼기(x) else 넣기.넣기(x) },
                꾹 = { 넣기.넣기(x) })
        }
        if (펼) Column(Modifier.padding(top = 간격.좁게), verticalArrangement = Arrangement.spacedBy(간격.좁게)) {
            if (플랜모양) {
                // 10-05 검수: PlanScreen 의 끌 수 있는 카드 목록을 그 종목 플랜만으로 (전: 끌기 없는 따로 만든 카드)
                // 10-09 홍겸 님: 카드 차례 = 게이지 → 다음 측정 → 근육 그림 2장 → [변경][지우기]
                val 플id = 플.map { it.id }.toSet()
                플랜종목칸(상태, { it.id in 플id }, 아래여백 = false, 횟수보임 = 플.size > 1, 근육칸 = {
                    if (t != null) key(t.id) { 플랜종목근육(상태, t) } else 근육읽기(상태, x.이름)
                })
            } else {
                if (t != null) key(t.id) { 종목고치기(상태, t, 플.isNotEmpty()) }
                else {
                    종목세팅(상태, x.이름, 플.isNotEmpty())
                    근육읽기(상태, x.이름)
                }
            }
            if (t != null && t.사진.isNotEmpty()) 넣은사진줄(상태, t)
        }
    }
}

/**
 * 어두운 가림막 (10-09 홍겸 님) — 펼친 상자 [구멍] 둘레를 네 조각으로 덮는다(구멍 안은 비어 있어 누름이 상자로 간다).
 * 어두운 곳을 누르면 [접기]. 끌어서 스크롤하는 것은 막지 않는다. 좌표는 부르는 쪽 칸(스크롤 속) 안쪽 px
 */
@Composable
private fun 펼침가림막(구멍: Rect, 접기: () -> Unit, modifier: Modifier) {
    val 밀도 = LocalDensity.current
    fun dp(px: Float) = with(밀도) { px.coerceAtLeast(0f).toDp() }
    val 칠 = Modifier.background(종목치수.가림).눌림(접기)
    Column(modifier) {
        Box(Modifier.fillMaxWidth().height(dp(구멍.top)).then(칠))
        Row(Modifier.fillMaxWidth().height(dp(구멍.height))) {
            Box(Modifier.width(dp(구멍.left)).fillMaxHeight().then(칠))
            Spacer(Modifier.width(dp(구멍.width)))
            Box(Modifier.weight(1f).fillMaxHeight().then(칠))
        }
        Box(Modifier.fillMaxWidth().weight(1f).then(칠))
    }
}

/** 내용 위에 가림색을 덮는다 — 모양대로(둥근 단추면 둥글게). [켬] 이 꺼지면 아무것도 안 한다 */
private fun Modifier.가림덮음(켬: Boolean): Modifier =
    if (!켬) this
    else this.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent { drawContent(); drawRect(종목치수.가림, blendMode = BlendMode.SrcAtop) }

/** [삭제] — 종목 · 기본 세팅 · 이 종목을 쓰는 루틴 줄을 뺀다(지난 기록은 그대로). 묻지 않고 지우고 [되돌리기] */
private fun 종목삭제(상태: 앱상태, t: 종목) {
    발자취.적기("종목 삭제 · ${t.이름}")
    상태.지우고알림("${조사(t.이름, "을", "를")} 지웠습니다") { it.종목지우기(t.id) }
}

/**
 * 이름 고치는 칸 (10-08 홍겸 님) — 펼친 상자에서 이름을 누르면 그 자리에서 글 칸으로 바뀐다.
 * 자판 [완료] · 칸 밖 누름([이름칸밖누름]) · 화면 나감 때 저장. 빈 이름 · 같은 이름은 그대로.
 */
@Composable
private fun 종목이름고침칸(상태: 앱상태, t: 종목, 끝: () -> Unit, 펼침: (String) -> Unit, modifier: Modifier) {
    val c = Local색.current
    val 초점 = LocalFocusManager.current
    val 요청 = remember { FocusRequester() }
    var 글 by remember { mutableStateOf(TextFieldValue(t.이름, TextRange(0, t.이름.length))) }
    var 받음 by remember { mutableStateOf(false) }
    var 넣음 by remember { mutableStateOf(false) }
    val 글최신 by rememberUpdatedState(글.text)
    val t최신 by rememberUpdatedState(t)
    val 펼침최신 by rememberUpdatedState(펼침)
    fun 넣기() { if (넣음) return; 넣음 = true; 종목이름넣기(상태, t최신, 글최신, 펼침최신) }
    LaunchedEffect(Unit) { try { 요청.requestFocus() } catch (_: Exception) { } }
    DisposableEffect(Unit) { onDispose { 이름고침판.틀 = null; 넣기() } }   // 칸이 열린 채 시트가 닫히거나 탭이 바뀌어도 잃지 않는다
    val 모양 = RoundedCornerShape(모서리.작게)
    Row(
        modifier.height(높이.아주낮게).clip(모양).background(c.면).border(선굵기.보통, c.강조, 모양)
            .padding(horizontal = 간격.좁게).onGloballyPositioned { 이름고침판.틀 = it.boundsInRoot() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = 글,
            onValueChange = { v -> 글 = v.copy(text = v.text.replace("\n", "")) },
            singleLine = true,
            textStyle = 글꼴.보통(종목치수.이름글, FontWeight.Medium).copy(color = c.글),
            cursorBrush = SolidColor(c.강조),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { 초점.clearFocus() }),
            modifier = Modifier.fillMaxWidth().focusRequester(요청).semantics { contentDescription = "종목 이름" }
                .onFocusChanged { f ->
                    if (f.isFocused) 받음 = true
                    else if (받음) { 받음 = false; 넣기(); 끝() }
                },
        )
    }
}

/**
 * 이름 바꿔 넣기 — 새 종목 시트 [편집] 과 같은 길(`종목고침`)이라 저장 · 계산은 새로 만들지 않았다.
 * 빈 이름 · 같은 이름이면 아무 일 없다. 오류(플랜이 걸린 종목)는 토스트. 성공하면 토스트 "이름을 바꿨습니다 · ○○"
 */
private fun 종목이름넣기(상태: 앱상태, t: 종목, 새: String, 펼침: (String) -> Unit) {
    val n = 새.trim()
    val d = 상태.d
    val 지금 = d.종목표.firstOrNull { it.id == t.id } ?: return
    if (n.isEmpty() || n == 지금.이름) return
    // 10-08: `이름바꿈()` 은 근육을 비우므로 쓰지 않는다 — 편집초기 값에서 이름만 바꾼다
    val r = d.종목고침(d.편집초기(지금).copy(이름 = n))
    r.오류?.let { 상태.알림.토스트(it); return }
    val nd = r.d ?: return
    val nt = r.종목 ?: return
    상태.바꿈 { nd }
    발자취.적기("종목 이름 바꿈 · ${지금.이름} → ${nt.이름}")
    상태.알림.토스트("이름을 바꿨습니다 · ${nt.이름}")
    // 옛 꼴 종목(id = 옛 이름)은 새 id 를 받는다 — 펼친 상자가 접히지 않게 새 id 로 다시 펼친다
    if (nt.id != 지금.id) { 펼침(지금.id); 펼침(nt.id) }
}

/** 이름 고치는 칸의 자리 (화면 좌표) — [이름칸밖누름] 이 읽는다. 칸이 없으면 null */
internal object 이름고침판 { var 틀: Rect? = null }

/**
 * 이름 고치는 칸이 열려 있을 때 그 칸 **밖**을 누르면 초점을 놓는다(= 저장). 누름은 가로채지 않는다.
 * 목록을 넘기는 칸에 단다 — 칸 안을 누르면 아무 일 없다
 */
internal fun Modifier.이름칸밖누름(): Modifier = composed {
    val 초점 = LocalFocusManager.current
    val 내자리 = remember { arrayOf(Offset.Zero) }
    this.onGloballyPositioned { 내자리[0] = it.positionInRoot() }
        .pointerInput(Unit) {
            awaitEachGesture {
                val 눌림 = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                val 틀 = 이름고침판.틀
                if (틀 != null && !틀.contains(눌림.position + 내자리[0])) 초점.clearFocus()
            }
        }
}

/**
 * 넣기 체크 상자 (종목 넣기 시트 · 시안 `.넣기체크` 20) — 들어간 개수만큼 ✓ (넷부터 ✓×n). 누르는 칸 28.
 * 누름 = 안 들어 있으면 넣기 · 들어 있으면 하나 빼기 / 꾹 = 하나 더 넣기
 */
@Composable
private fun 넣기체크(이름: String, 수: Int, 누름: () -> Unit, 꾹: () -> Unit) {
    val c = Local색.current
    val 들어감 = 수 > 0
    val 상자 = RoundedCornerShape(넣기값.체크모서리)
    Box(
        // 10-08 홍겸 님: ∨ 와의 눈에 보이는 간격을 35% 줄이고(12 → 7.8) 체크 상자를 오른쪽으로 4 옮긴다.
        //  누르는 칸 28 은 그대로 — 차지하는 폭만 줄여, 같은 줄의 이름 칸이 그만큼 넓어진다 (∨ 가 오른쪽으로 간다)
        Modifier.layout { m, cons ->
            val p = m.measure(cons)
            val 여백 = (높이.아주낮게 - 넣기값.체크).roundToPx() / 2                       // 누르는 칸 안 쪽 여백 (4)
            val 옛간격 = 간격.좁게.roundToPx() + 여백                                     // ∨ ↔ 체크 상자 (12)
            val 시작 = (옛간격 * (1f - 넣기값.체크간격줄임)).roundToInt() - 간격.좁게.roundToPx()   // 상자 왼쪽 끝 (줄 사이 8 기준)
            val 폭 = p.width - 2 * 여백
            val 쓸폭 = (시작 + 폭 - (넣기값.체크옮김.roundToPx() - 여백)).coerceAtLeast(0)
            layout(쓸폭, p.height) { p.place(시작 - 여백, 0) }
        }.heightIn(min = 높이.아주낮게).widthIn(min = 높이.아주낮게).clip(RoundedCornerShape(모서리.작게))
            .semantics(mergeDescendants = true) {
                contentDescription = "$이름 · ${수}개 들어 있음 · ${if (들어감) "누르면 하나 빼기" else "누르면 넣기"}, 꾹 누르면 하나 더"
            }
            .눌림길게(누름, 꾹),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.heightIn(min = 넣기값.체크).widthIn(min = 넣기값.체크).clip(상자)
                .background(if (들어감) c.강조 else c.면)
                .border(선굵기.보통, if (들어감) c.강조 else c.속선, 상자)
                .padding(horizontal = 간격.아주좁게),
            contentAlignment = Alignment.Center,
        ) {
            if (들어감) Text(체크글(수), style = 글꼴.보통(크기.작게, FontWeight.Bold), color = c.강조글, maxLines = 1, softWrap = false)
        }
    }
}

/**
 * 펼친 종목 고치기 (10-08 홍겸 님) — 기본 세팅 · 근육을 **고쳐 두었다가** [저장] 때 넣는다. [취소] = 처음 값으로. 접으면 버린다.
 * 저장은 새 종목 시트 [편집] 과 같은 길(`종목고침` — 근육 · 칸 · 세트)이라 저장 · 계산은 새로 만들지 않았다
 */
@Composable
private fun 종목고치기(상태: 앱상태, t: 종목, 플랜있음: Boolean) {
    val c = Local색.current
    val d = 상태.d
    var 처음 by remember { mutableStateOf(d.편집초기(t)) }
    var v by remember { mutableStateOf(처음) }
    val 바뀜 = v.세트 != 처음.세트 || v.근육 != 처음.근육 || v.칸 != 처음.칸
    fun 저장() {
        // 10-08 홍겸 님: 이름은 머리에서 따로 고친다 — 저장은 지금 종목 이름을 그대로 (옛 이름으로 되돌리지 않게)
        val 저 = v.copy(이름 = t.이름)
        상태.d.저장검사(저)?.let { 상태.알림.토스트(it); return }
        val r = 상태.d.종목고침(저)
        r.오류?.let { 상태.알림.토스트(it); return }
        val nd = r.d ?: return
        val nt = r.종목 ?: return
        상태.바꿈 { nd }
        발자취.적기("종목 저장 · ${nt.이름}")
        상태.알림.토스트("저장했습니다 · ${nt.이름}")
        처음 = nd.편집초기(nt); v = 처음
    }
    Column(verticalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
            글("기본 세팅", 크기값 = 크기.조금작게, 굵기 = FontWeight.Bold)
            if (플랜있음) 맞춤글("플랜으로 넣으면 플랜 처방대로", Modifier.weight(1f), 최대 = 크기.작게, 색 = c.옅음)
        }
        기본세트표(v.세트, d.설정.무게폭, { f -> v = v.copy(세트 = f(v.세트)) }, { 상태.알림.토스트(세트최대글) })
    }
    // 10-09 홍겸 님: 근육 그림 2장 (글 없음) — 어디를 눌러도 근육 고르기 팝업 (근육 조각이면 그 부위 줄 강조)
    근육두장(v.근육, d.설정.색표, { 키들 -> v = if (키들.isEmpty()) v.팝빈열기() else v.팝열기(키들) })
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
        버튼("저장", { 저장() }, Modifier.weight(1f), 주요 = 바뀜, 작게 = true)
        버튼("취소", { v = 처음 }, 작게 = true, 글색 = if (바뀜) c.나쁨 else c.옅음)
    }
    if (v.팝 != null) 근육팝(v, { f -> v = f(v) }, { 상태.알림.토스트(it) }, d.카테고리)
}

/**
 * 플랜 종목의 근육 그림 2장 (10-09 홍겸 님) — 플랜 카드의 다음 측정 줄 아래. 기본 세팅 · [저장][취소] 가 없어서
 * 근육 고르기 팝업을 **닫을 때 고친 것이 있으면 바로 저장**한다 (저장은 [종목고치기] 와 같은 길 `종목고침`).
 * 주동근이 없어지면 저장하지 않고 안내 글 + 처음 값으로
 */
@Composable
private fun 플랜종목근육(상태: 앱상태, t: 종목) {
    val d = 상태.d
    var 처음 by remember { mutableStateOf(d.편집초기(t)) }
    var v by remember { mutableStateOf(처음) }
    fun 닫으며저장(n: 새종목값): 새종목값 {
        if (n.근육 == 처음.근육 && n.칸 == 처음.칸) return n
        val 저 = n.copy(이름 = t.이름)
        상태.d.저장검사(저)?.let { 상태.알림.토스트(it); return 처음 }
        val r = 상태.d.종목고침(저)
        r.오류?.let { 상태.알림.토스트(it); return 처음 }
        val nd = r.d ?: return n
        val nt = r.종목 ?: return n
        상태.바꿈 { nd }
        발자취.적기("종목 근육 저장 · ${nt.이름}")
        상태.알림.토스트("저장했습니다 · ${nt.이름}")
        처음 = nd.편집초기(nt)
        return 처음
    }
    근육두장(v.근육, d.설정.색표, { 키들 -> v = if (키들.isEmpty()) v.팝빈열기() else v.팝열기(키들) })
    if (v.팝 != null) 근육팝(v, { f ->
        val n = f(v)
        v = if (n.팝 == null) 닫으며저장(n) else n
    }, { 상태.알림.토스트(it) }, d.카테고리)
}

/** 고칠 종목이 없는(플랜만 남은) 상자의 근육 그림 2장 — 보기만 한다 (누름 없음) */
@Composable
private fun 근육읽기(상태: 앱상태, 이름: String) {
    val d = 상태.d
    val 근육 = remember(이름, d.종목표) { 종목사전.둘역할(종목사전.세부로(d.종목근육(null, 이름))) }
    근육두장(근육, d.설정.색표, { })
}

/** 기본 세팅 표 칸 폭 (10-08 홍겸 님) — 무게 '100.25' · 횟수 3자리 · 휴식 '10:00' 이 들어갈 만큼만. − ＋ 는 15% 작게 */
internal object 세트표치수 {
    val 번호 = 28.dp     // 세트 동그라미 (운동 중 화면처럼 · 높이.아주낮게)
    val 무게 = 96.dp     // 단추 24 × 2 + 글 48
    val 횟수 = 76.dp     // 단추 24 × 2 + 글 28
    val 휴식 = 88.dp     // 단추 24 × 2 + 글 40
    val 지움 = 20.dp     // 휴지통 칸 (28 → 20)
    val 단추 = 24.dp     // − ＋ 누르는 칸 (28 의 85%)
    val 그림 = 16.dp     // − ＋ 그림 (18 의 85% ≈ 16 · U3-6 작게)
}

/**
 * 기본 세팅 표 (10-08 홍겸 님 · 운동 중 화면과 비슷한 배치) — [세트 ◯][무게 kg][횟수][휴식][휴지통] + [+ 세트 추가하기].
 * 세트 번호는 강조 동그라미. 값 바꾸는 규칙은 [세트줄표] 와 같다(무게폭 · 1회 이상 · 휴식 15초 눈금)
 */
@Composable
private fun 기본세트표(세트: List<종목세트>, 무게폭: Double, 바꿈: ((List<종목세트>) -> List<종목세트>) -> Unit, 최대알림: () -> Unit) {
    val c = Local색.current
    Column(verticalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
        세트표줄(
            { 글("세트", 크기값 = 크기.작게, 색 = c.옅음, 가운데 = true) },
            { 글("무게 kg", 크기값 = 크기.작게, 색 = c.옅음, 가운데 = true) },
            { 글("횟수", 크기값 = 크기.작게, 색 = c.옅음, 가운데 = true) },
            { 글("휴식", 크기값 = 크기.작게, 색 = c.옅음, 가운데 = true) },
            { },
        )
        val 하나 = 세트.size <= 1
        val 앞 = 앞줄개수(세트.size)   // 방금 더한 줄은 한 번 점멸 · 눌렸다 제자리
        세트.forEachIndexed { k, x ->
            key(k) {
                세트표줄(
                    {
                        Box(
                            Modifier.size(세트표치수.번호).clip(CircleShape).border(선굵기.보통, c.강조, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) { 글("${k + 1}", 크기값 = 크기.조금작게, 색 = c.강조, 굵기 = FontWeight.Bold, 가운데 = true) }
                    },
                    {
                        세트값칸(무게글(x.w), Modifier.fillMaxWidth(), "${k + 1}세트 무게", x.w > 0,
                            { 바꿈 { l -> 세트값바꿈(l, k, 'w', -1, 무게폭) } }, { 바꿈 { l -> 세트값바꿈(l, k, 'w', 1, 무게폭) } },
                            { g -> 바꿈 { l -> 세트글넣음(l, k, 'w', g) } }, 단추폭 = 세트표치수.단추, 그림크기 = 세트표치수.그림)
                    },
                    {
                        세트값칸("${x.r}", Modifier.fillMaxWidth(), "${k + 1}세트 횟수", x.r > 1,
                            { 바꿈 { l -> 세트값바꿈(l, k, 'r', -1, 무게폭) } }, { 바꿈 { l -> 세트값바꿈(l, k, 'r', 1, 무게폭) } },
                            { g -> 바꿈 { l -> 세트글넣음(l, k, 'r', g) } }, 정수 = true, 단추폭 = 세트표치수.단추, 그림크기 = 세트표치수.그림)
                    },
                    {
                        세트값칸(분초(x.휴), Modifier.fillMaxWidth(), "${k + 1}세트 휴식", x.휴 > 휴식칸값.최소,
                            { 바꿈 { l -> l.getOrNull(k)?.let { 세트휴식(l, k, 휴식한칸(it.휴, -1)) } ?: l } },
                            { 바꿈 { l -> l.getOrNull(k)?.let { 세트휴식(l, k, 휴식한칸(it.휴, 1)) } ?: l } },
                            null, 단추폭 = 세트표치수.단추, 그림크기 = 세트표치수.그림, 더할수있음 = x.휴 < 휴식칸값.최대)
                    },
                    {
                        Box(
                            Modifier.size(세트표치수.지움, 높이.아주낮게).clip(RoundedCornerShape(모서리.작게))
                                .then(if (하나) Modifier else Modifier.눌림 { 바꿈 { l -> 세트지움(l, k) } }),
                            contentAlignment = Alignment.Center,
                        ) { Icon(아이콘.지우기, "${k + 1}세트 지우기", Modifier.size(종목치수.지움그림), tint = if (하나) c.선 else c.옅음) }
                    },
                    줄모양 = Modifier.새줄효과(k >= 앞),
                )
            }
        }
        버튼("+ 세트 추가하기", { if (세트.size >= 종목세트최대) 최대알림() else 바꿈 { l -> 세트더함(l) } }, Modifier.fillMaxWidth(), 낮게 = true)
    }
}

/** 기본 세팅 표 한 줄 — 칸 폭은 [세트표치수] 로 정하고 남는 자리는 칸 사이에 고르게 (SpaceBetween) */
@Composable
private fun 세트표줄(
    번호: @Composable () -> Unit, 무게: @Composable () -> Unit, 횟수: @Composable () -> Unit,
    휴식: @Composable () -> Unit, 끝: @Composable () -> Unit, 줄모양: Modifier = Modifier,
) {
    Row(줄모양.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Box(Modifier.width(세트표치수.번호), contentAlignment = Alignment.Center) { 번호() }
        Box(Modifier.width(세트표치수.무게), contentAlignment = Alignment.Center) { 무게() }
        Box(Modifier.width(세트표치수.횟수), contentAlignment = Alignment.Center) { 횟수() }
        Box(Modifier.width(세트표치수.휴식), contentAlignment = Alignment.Center) { 휴식() }
        Box(Modifier.width(세트표치수.지움), contentAlignment = Alignment.Center) { 끝() }
    }
}

/** 펼친 상자 머리의 사진 칸 28 — 첫 사진 · 없으면 점선 ＋. 누르면 사진 넣기 (시안 `.사진넣칸`) */
@Composable
private fun 사진넣는칸(상태: 앱상태, t: 종목) {
    val c = Local색.current
    val ctx = LocalContext.current
    val 일꾼 = rememberCoroutineScope()
    var 넣는중 by remember { mutableStateOf(false) }
    val 고르기 = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(근육표.사진최대)) { 고른것 ->
        if (고른것.isEmpty()) return@rememberLauncherForActivityResult
        val 남은 = (근육표.사진최대 - (상태.d.종목표.firstOrNull { it.id == t.id }?.사진?.size ?: 0)).coerceAtLeast(0)
        if (남은 == 0) { 상태.알림.토스트("사진은 ${근육표.사진최대}장까지"); return@rememberLauncherForActivityResult }
        넣는중 = true
        일꾼.launch {
            val 이름들 = withContext(Dispatchers.IO) { 사진함.넣기(ctx, 고른것.take(남은)) }
            발자취.적기("${t.이름} 사진 ${이름들.size}장 넣음")
            상태.바꿈 { it.종목사진더함(t.id, 이름들) }
            넣는중 = false
        }
    }
    val 모양 = RoundedCornerShape(그림칸.모서리)
    val 첫 = t.사진.firstOrNull()
    Box(
        Modifier.size(그림칸.작은사진).clip(모양)
            .then(if (첫 == null) Modifier.background(c.면2).border(선굵기.보통, c.속선, 모양) else Modifier)
            .눌림 {
                if (넣는중) return@눌림
                if (t.사진.size >= 근육표.사진최대) 상태.알림.토스트("사진은 ${근육표.사진최대}장까지")
                else 고르기.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
        contentAlignment = Alignment.Center,
    ) {
        if (첫 != null) 사진그림(첫, 그림칸.작은사진 * 2, Modifier.fillMaxSize(), "${t.이름} 사진 넣기")
        else 글("＋", 크기값 = 크기.조금작게, 색 = c.옅음)
    }
}

/** 넣은 사진 줄 72 — 누르면 '지우기' 표시 · 한 번 더 누르면 지운다(되돌리기 띠) (시안 `.사진줄` · '사진지움') */
@Composable
private fun 넣은사진줄(상태: 앱상태, t: 종목) {
    val c = Local색.current
    var 지울것 by remember(t.id) { mutableStateOf<String?>(null) }
    val 모양 = RoundedCornerShape(그림칸.모서리)
    val 넘김 = rememberScrollState()
    Row(Modifier.fillMaxWidth().오른끝흐림(넘김).horizontalScroll(넘김), horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
        t.사진.forEachIndexed { j, 이름 ->
            key(이름) {
                Box(
                    Modifier.size(그림칸.사진).clip(모양).눌림 {
                        if (지울것 == 이름) {
                            지울것 = null
                            발자취.적기("${t.이름} 사진 지움")
                            상태.지우고알림("${t.이름} 사진을 지웠습니다") { it.종목사진뺌(t.id, 이름) }
                        } else 지울것 = 이름
                    },
                ) {
                    사진그림(이름, 그림칸.사진 * 2, Modifier.fillMaxSize(), "사진 ${j + 1} 지우기")
                    val 보임 by animateFloatAsState(if (지울것 == 이름) 1f else 0f, tween(움직임.색), label = "지우기표")
                    if (보임 > 0f) Box(
                        Modifier.fillMaxSize().graphicsLayer { alpha = 보임 }.background(c.나쁨),
                        contentAlignment = Alignment.Center,
                    ) { 글("지우기", 크기값 = 크기.조금작게, 색 = c.나쁨글, 굵기 = FontWeight.Bold) }
                }
            }
        }
    }
}


/** 기본 세팅 (시안 `종목세팅`) — 세트 줄 목록. 값은 종목설정[열쇠] (없으면 설정 기본값) */
@Composable
private fun 종목세팅(상태: 앱상태, 열쇠: String, 플랜있음: Boolean) {
    val c = Local색.current
    Column(verticalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
            글("기본 세팅", 크기값 = 크기.조금작게, 굵기 = FontWeight.Bold)
            if (플랜있음) 맞춤글("플랜으로 넣으면 플랜 처방대로", Modifier.weight(1f), 최대 = 크기.작게, 색 = c.옅음)
        }
        세트줄표(
            상태.d.종목기본세트(열쇠), 상태.d.설정.무게폭,
            { f -> 상태.바꿈 { it.종목세트고침(열쇠, f) } },
            { 상태.알림.토스트(세트최대글) },
        )
    }
}

/**
 * 세트 줄 목록 [번호][무게 kg][횟수][휴식][휴지통] + [+ 세트] — 종목 탭 기본 세팅 · 새 종목 시트가 같이 쓴다 (시안 `.종세트`).
 * 무게 ± = 설정의 무게 조절 폭 · 횟수 1 이상 · 휴식 15초씩 0:15~5:00(휴식값칸) · 마지막 한 줄은 안 지운다 · + 세트 = 앞 줄 값 복사(10줄까지).
 * [바꿈] 은 '지금 목록 → 새 목록' 함수를 받는다 (빠르게 연달아 눌러도 가장 새 값에서 계산되게)
 */
@Composable
internal fun 세트줄표(세트: List<종목세트>, 무게폭: Double, 바꿈: ((List<종목세트>) -> List<종목세트>) -> Unit, 최대알림: () -> Unit) {
    val c = Local색.current
    Column(verticalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
        Row(horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
            글("세트", Modifier.width(종목치수.번호칸), 크기값 = 크기.작게, 색 = c.옅음, 가운데 = true)
            글("무게 kg", Modifier.weight(종목치수.열무게), 크기값 = 크기.작게, 색 = c.옅음, 가운데 = true)
            글("횟수", Modifier.weight(종목치수.열횟수), 크기값 = 크기.작게, 색 = c.옅음, 가운데 = true)
            글("휴식", Modifier.weight(종목치수.열휴식), 크기값 = 크기.작게, 색 = c.옅음, 가운데 = true)
            Box(Modifier.width(종목치수.지움칸))
        }
        val 하나 = 세트.size <= 1
        val 앞 = 앞줄개수(세트.size)   // 10-08: 방금 더한 줄은 한 번 점멸 · 눌렸다 제자리
        세트.forEachIndexed { k, x ->
            key(k) {
                Row(Modifier.새줄효과(k >= 앞), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                    글("${k + 1}", Modifier.width(종목치수.번호칸), 크기값 = 크기.조금작게, 굵기 = FontWeight.Bold, 가운데 = true)
                    세트값칸(무게글(x.w), Modifier.weight(종목치수.열무게), "${k + 1}세트 무게", x.w > 0,
                        { 바꿈 { l -> 세트값바꿈(l, k, 'w', -1, 무게폭) } }, { 바꿈 { l -> 세트값바꿈(l, k, 'w', 1, 무게폭) } },
                        { g -> 바꿈 { l -> 세트글넣음(l, k, 'w', g) } })
                    세트값칸("${x.r}", Modifier.weight(종목치수.열횟수), "${k + 1}세트 횟수", x.r > 1,
                        { 바꿈 { l -> 세트값바꿈(l, k, 'r', -1, 무게폭) } }, { 바꿈 { l -> 세트값바꿈(l, k, 'r', 1, 무게폭) } },
                        { g -> 바꿈 { l -> 세트글넣음(l, k, 'r', g) } }, 정수 = true)
                    휴식값칸(x.휴, { 새 -> 바꿈 { l -> 세트휴식(l, k, 새) } }, Modifier.weight(종목치수.열휴식), 칸높이 = 높이.아주낮게)
                    Box(
                        Modifier.size(종목치수.지움칸).clip(RoundedCornerShape(모서리.작게))
                            .then(if (하나) Modifier else Modifier.눌림 { 바꿈 { l -> 세트지움(l, k) } }),
                        contentAlignment = Alignment.Center,
                    ) { Icon(아이콘.지우기, "${k + 1}세트 지우기", Modifier.size(종목치수.지움그림), tint = if (하나) c.선 else c.옅음) }
                }
            }
        }
        버튼("+ 세트", { if (세트.size >= 종목세트최대) 최대알림() else 바꿈 { l -> 세트더함(l) } }, Modifier.fillMaxWidth(), 낮게 = true)
    }
}

/** − 값 ＋ (가운데 = 쳐서 넣는 칸) — 높이 28. 치는 동안은 친 글 그대로, 손을 떼면 저장된 값으로 (시안 `.종세트 .값칸`) */
@Composable
private fun 세트값칸(
    값글: String, modifier: Modifier, 이름: String, 뺄수있음: Boolean,
    빼기: () -> Unit, 더하기: () -> Unit, 넣기: ((String) -> Unit)?, 정수: Boolean = false,
    /** 10-08 기본 세팅 표: − ＋ 를 15% 작게 · [넣기] null = 쳐 넣지 않는 칸(휴식) · [더할수있음] = ＋ 끝 */
    단추폭: androidx.compose.ui.unit.Dp = 부품치수.값칸단추, 그림크기: androidx.compose.ui.unit.Dp = 종목치수.값그림, 더할수있음: Boolean = true,
) {
    val c = Local색.current
    val 모양 = RoundedCornerShape(모서리.작게)
    var 초점 by remember { mutableStateOf(false) }
    var 친글 by remember { mutableStateOf(값글) }
    // 10-07 홍겸 님: 자판으로 치는 중에 − ＋ 를 누르면 화면 숫자도 바로 바뀌게 (전: 화면은 35 그대로, 값만 38)
    var 버튼값 by remember { mutableStateOf(false) }
    LaunchedEffect(값글, 초점) { if (!초점 || 버튼값) { 친글 = 값글; 버튼값 = false } }
    val 자판 = LocalFocusManager.current
    Row(modifier.height(높이.아주낮게).clip(모양).border(선굵기.보통, c.속선, 모양), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(단추폭).fillMaxHeight().then(if (뺄수있음) Modifier.눌림 { 버튼값 = true; 빼기() } else Modifier), contentAlignment = Alignment.Center) {
            Icon(아이콘.빼기, "$이름 빼기", Modifier.size(그림크기), tint = if (뺄수있음) c.강조 else c.옅음)
        }
        val 판 = 가운데판(초점)   // 10-07 홍겸 님: 좌우로 밀어도 손 떼면 가운데 (NewExerciseSheet.kt)
        if (넣기 == null) Text(
            값글, Modifier.weight(1f), style = 글꼴.보통(크기.버튼, FontWeight.Bold).copy(fontFeatureSettings = "tnum"),
            color = c.글, maxLines = 1, softWrap = false, textAlign = TextAlign.Center,
        ) else key(판.번호) {
            BasicTextField(
                value = if (초점) 친글 else 값글,
                onValueChange = { t -> 친글 = t; 넣기(t) },
                singleLine = true,
                textStyle = 글꼴.보통(크기.버튼, FontWeight.Bold).copy(color = c.글, textAlign = TextAlign.Center, fontFeatureSettings = "tnum"),
                cursorBrush = SolidColor(c.강조),
                keyboardOptions = KeyboardOptions(keyboardType = if (정수) KeyboardType.Number else KeyboardType.Decimal, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { 자판.clearFocus() }),
                modifier = Modifier.weight(1f).then(판.손).onFocusChanged { 초점 = it.isFocused },
            )
        }
        Box(Modifier.width(단추폭).fillMaxHeight().then(if (더할수있음) Modifier.눌림 { 버튼값 = true; 더하기() } else Modifier), contentAlignment = Alignment.Center) {
            Icon(아이콘.더하기, "$이름 더하기", Modifier.size(그림크기), tint = if (더할수있음) c.강조 else c.옅음)
        }
    }
}

@Composable
private fun 카테고리관리(상태: 앱상태, 닫기: () -> Unit) {
    val c = Local색.current
    var 새 by remember { mutableStateOf("") }
    시트("카테고리", 닫기, 겹 = 1) {   // 10-08 홍겸 님: 종목 탭(0) 위 한 겹
        글("루틴의 종목 고르기에도 그대로 나옵니다", 크기값 = 크기.조금작게, 색 = c.옅음)
        상태.d.카테고리.forEach { p ->
            val 수 = 상태.d.종목표.count { it.부위 == p }
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                글(p, Modifier.weight(1f))
                글(if (수 > 0) "종목 ${수}개" else "", 크기값 = 크기.작게, 색 = c.옅음)
                아이콘버튼(아이콘.지우기, "카테고리 지우기", {
                    상태.지우고알림(if (수 > 0) "$p · 종목 ${수}개와 함께 지웠습니다" else "$p 카테고리를 지웠습니다") { it.카테고리지우기(p) }
                }, 칠함 = false)
            }
            구분선()
        }
        Box(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            입력칸(새, { 새 = it }, Modifier.weight(1f), 안내 = "새 카테고리 (예: 전완)")
            Box(Modifier.width(8.dp))
            버튼("추가", {
                val n = 새.trim()
                if (n.isNotEmpty() && n !in 상태.d.카테고리) 상태.바꿈 { it.copy(카테고리 = it.카테고리 + n) }
                새 = ""
            }, 주요 = true)
        }
    }
}

// ═════════════════════ 다른 파일이 부르는 옛 부품 — 이름 · 매개변수 그대로 (합칠 때 정리) ═════════════════════

/**
 * 1RM 목표 적기 (09-27) — 루틴의 종목 설정이 부른다. 종목표에 저장한다.
 * 비우면 목표 없음. 캘린더 판에 지금 속도 · 도달 예상이 보인다
 */
@Composable
fun 목표칸(상태: 앱상태, 종목이름: String) {
    val 것 = 상태.d.종목표.firstOrNull { it.이름 == 종목이름 }
    var 값 by remember(종목이름, 것?.목표1RM) { mutableStateOf(것?.목표1RM?.let { 무게글(it) } ?: "") }
    fun 저장() {
        val v = 값.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 && it.isFinite() }
        상태.바꿈 { d ->
            val 있나 = d.종목표.any { it.이름 == 종목이름 }
            d.copy(종목표 = if (있나) d.종목표.map { if (it.이름 == 종목이름) it.copy(목표1RM = v) else it }
                         else d.종목표 + 종목(종목이름, d.카테고리.firstOrNull() ?: "", 목표1RM = v))
        }
    }
    이름표("1RM 목표")
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        입력칸(값, { 값 = it }, Modifier.weight(1f), 안내 = "비워두면 목표 없음", 숫자 = true, onDone = { 저장() })
        글("kg", Modifier.padding(horizontal = 8.dp), 크기값 = 크기.버튼, 색 = Local색.current.옅음)
        버튼("저장", { 저장() }, 작게 = true)
    }
}

/**
 * 부위 고르기 — 새 종목을 만들 때. 칩 줄 끝의 ＋ 로 부위를 더하고, − 를 누르면 칩마다 ✕ 가 붙어 지울 수 있다.
 * (09-21 메모: 고정된 부위 외에는 만들 길이 안 보였다. 종목 탭 ⚙ 와 같은 목록을 쓴다)
 */
@Composable
fun 부위고르기(상태: 앱상태, 선택: String, on선택: (String) -> Unit) {
    val c = Local색.current
    var 더하는중 by remember { mutableStateOf(false) }
    var 지우는중 by remember { mutableStateOf(false) }
    var 새 by remember { mutableStateOf("") }
    val 넘김 = rememberScrollState()   // 10-02: 넘칠 때만 오른쪽 끝을 흐린다 (D2-8)
    Row(Modifier.fillMaxWidth().오른끝흐림(넘김).horizontalScroll(넘김), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        상태.d.카테고리.forEach { p ->
            val 켬 = p == 선택 && !지우는중
            Row(
                Modifier.height(높이.낮게).clip(CircleShape)
                    .background(if (지우는중) c.나쁨.copy(alpha = 0.10f) else if (켬) c.강조 else c.면2)
                    .눌림 {
                        if (지우는중) {
                            val 수 = 상태.d.종목표.count { it.부위 == p }
                            상태.지우고알림(if (수 > 0) "$p · 종목 ${수}개와 함께 지웠습니다" else "$p 부위를 지웠습니다") { it.카테고리지우기(p) }
                            if (p == 선택) on선택(상태.d.카테고리.firstOrNull() ?: "")
                        } else on선택(p)
                    }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                글(p, 크기값 = 크기.버튼, 색 = if (지우는중) c.나쁨 else if (켬) c.강조글 else c.흐림, 굵기 = FontWeight.Medium)
                if (지우는중) { Box(Modifier.width(4.dp)); Icon(아이콘.닫기, "지우기", Modifier.size(13.dp), tint = c.나쁨) }
            }
        }
        아이콘버튼(아이콘.더하기, "부위 더하기", { 더하는중 = !더하는중; 지우는중 = false }, 켬 = 더하는중)
        아이콘버튼(if (지우는중) 아이콘.체크 else 아이콘.빼기, if (지우는중) "지우기 끝" else "부위 지우기", { 지우는중 = !지우는중; 더하는중 = false }, 켬 = 지우는중)
    }
    if (더하는중) {
        val 넣기 = {
            val n = 새.trim()
            if (n.isNotEmpty() && n !in 상태.d.카테고리) 상태.바꿈 { it.copy(카테고리 = it.카테고리 + n) }
            if (n.isNotEmpty()) on선택(n)
            새 = ""; 더하는중 = false
        }
        Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            입력칸(새, { 새 = it }, Modifier.weight(1f), 안내 = "새 부위 (예: 전완)", onDone = 넣기)
            Box(Modifier.width(8.dp))
            버튼("추가", 넣기, 작게 = true, 주요 = true)
        }
    }
}

/**
 * 종목 이름 입력 + 추천 (09-21 메모).
 * 자음·모음 단위로 좁혀 가며, 띄어쓰기는 무시한다. 추천은 기본 종목 목록 + 내가 만든 종목.
 * 추천을 누르면 이름과 함께 부위 · 장비도 넘겨준다.
 * 있는것도=false 면 이미 만든 종목은 추천하지 않는다 (종목 탭에서 새로 만들 때 — 같은 이름은 못 만드니까)
 */
@Composable
fun 종목이름칸(상태: 앱상태, 이름: String, on이름: (String) -> Unit, 있는것도: Boolean = true, on고름: (String, String, String) -> Unit) {
    val c = Local색.current
    val 표 = 상태.d.종목표
    val 정보 = linkedMapOf<String, Pair<String, String>>()
    if (있는것도) 표.forEach { 정보[it.이름] = it.부위 to it.장비 }
    이름추천.기본.forEach { b -> if (b.이름 !in 정보 && 표.none { it.이름 == b.이름 }) 정보[b.이름] = b.부위 to b.장비 }
    // 추천에서 하나를 고르면 목록을 닫는다 — 이름을 다시 고치면 또 뜬다 (09-24 메모)
    var 고른것 by remember { mutableStateOf<String?>(null) }
    val 추천 = if (이름.isNotBlank() && 이름 == 고른것) emptyList() else 이름추천.찾기(이름, 정보.keys.toList())
    입력칸(이름, { t -> if (t != 이름) 고른것 = null; on이름(t) }, Modifier.fillMaxWidth(), 안내 = "종목 이름 (예: 벤치프레스)")
    if (추천.isNotEmpty()) {
        Column(
            Modifier.fillMaxWidth().padding(top = 4.dp).clip(RoundedCornerShape(모서리.작게))
                .background(c.면).border(1.dp, c.속선, RoundedCornerShape(모서리.작게)),
        ) {
            추천.forEachIndexed { i, n ->
                if (i > 0) 구분선()
                val (부, 장) = 정보.getValue(n)
                Row(Modifier.fillMaxWidth().눌림 { 고른것 = n; on고름(n, 부, 장) }.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    글(n, Modifier.weight(1f, fill = false), 크기값 = 크기.버튼, 굵기 = FontWeight.Medium)
                    Box(Modifier.width(8.dp))
                    글(listOf(부, 장).filter { it.isNotBlank() }.joinToString("·"), Modifier.weight(1f), 크기값 = 크기.작게, 색 = c.옅음)
                    if (표.any { it.이름 == n }) 글("내 종목", 크기값 = 크기.작게, 색 = c.강조)
                }
            }
        }
    }
}

/** 장비 고르기 — 선택지 + 전에 쓴 장비 + '직접 입력' (09-21 메모). 고른 것을 다시 누르면 비운다 */
@Composable
fun 장비고르기(상태: 앱상태, 선택: String, on선택: (String) -> Unit, 후보: List<String> = emptyList()) {
    val c = Local색.current
    // 이름으로 짐작한 후보를 앞에, 테두리로 표시
    val 목록 = (후보 + 이름추천.장비목록 + 상태.d.종목표.map { it.장비.trim() } + listOf(선택.trim())).filter { it.isNotBlank() }.distinct()
    var 직접 by remember { mutableStateOf(false) }
    var 새 by remember { mutableStateOf("") }
    val 넘김 = rememberScrollState()   // 10-02: 넘칠 때만 오른쪽 끝을 흐린다 (D2-8)
    Row(Modifier.fillMaxWidth().오른끝흐림(넘김).horizontalScroll(넘김), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        목록.forEach { p ->
            val 켬 = p == 선택.trim()
            Box(
                Modifier.height(높이.낮게).clip(CircleShape).background(if (켬) c.강조 else c.면2)
                    .then(if (!켬 && p in 후보) Modifier.border(1.dp, c.강조, CircleShape) else Modifier)
                    .눌림 { 직접 = false; on선택(if (켬) "" else p) }.padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) { 글(p, 크기값 = 크기.버튼, 색 = if (켬) c.강조글 else c.흐림, 굵기 = FontWeight.Medium) }
        }
        Box(
            Modifier.height(높이.낮게).clip(CircleShape).border(1.dp, if (직접) c.강조 else c.속선, CircleShape)
                .눌림 { 직접 = !직접 }.padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) { 글("직접 입력", 크기값 = 크기.버튼, 색 = if (직접) c.강조 else c.흐림, 굵기 = FontWeight.Medium) }
    }
    if (직접) {
        val 넣기 = { val n = 새.trim(); if (n.isNotEmpty()) on선택(n); 새 = ""; 직접 = false }
        Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            입력칸(새, { 새 = it }, Modifier.weight(1f), 안내 = "장비 이름", onDone = 넣기)
            Box(Modifier.width(8.dp))
            버튼("넣기", 넣기, 작게 = true, 주요 = true)
        }
    }
}

// ═════════════════════ 순수 계산 (시험: test/…/ui/ExerciseTest.kt) ═════════════════════

/** 상자 하나 — [종목] null = 종목표에 없고 플랜만 남은 종목('기타' 칸) */
internal data class 종목칸값(val 이름: String, val 칸: String, val 종목: 종목?) {
    /** 펼침 열쇠 — 종목 id, 플랜만 남은 것은 "기타:" + 이름 */
    val 열쇠: String get() = 종목?.id ?: "기타:$이름"
}

/** 종목 탭 판 — [칸들] 칩(전체 빼고) · [고름] 실제로 고른 칩 · [묶음] 카테고리마다 상자 (빈 카테고리는 뺀다) */
internal data class 종목판(val 칸들: List<String>, val 고름: String, val 묶음: List<Pair<String, List<종목칸값>>>)

/** 시안 `종목탭` — 플랜만 남은 종목은 '기타' (카테고리에 '기타' 가 있으면 거기에 더한다) */
internal fun 앱데이터.종목탭판(고른칸: String): 종목판 {
    val 남은 = 플랜들.map { it.종목 }.distinct().filter { n -> 종목표.none { it.이름 == n } }.map { 종목칸값(it, "기타", null) }
    val 칸들 = if (남은.isNotEmpty() && "기타" !in 카테고리) 카테고리 + "기타" else 카테고리
    val 고 = if (고른칸 in 칸들) 고른칸 else "전체"
    val 묶 = (if (고 == "전체") 칸들 else listOf(고)).map { c ->
        c to (종목표.filter { it.칸 == c }.map { 종목칸값(it.이름, it.칸, it) } + (if (c == "기타") 남은 else emptyList()))
    }.filter { it.second.isNotEmpty() }
    return 종목판(칸들, 고, 묶)
}

/** 상자의 플랜 — 그 이름의 첫 종목(또는 플랜만 남은 것)에만 붙는다 (시안 `첫`) */
internal fun 앱데이터.상자플랜(x: 종목칸값): List<플랜> {
    val t = x.종목
    val 첫 = t == null || 종목표.firstOrNull { it.이름 == t.이름 }?.id == t.id
    return if (첫) 플랜들.filter { it.종목 == x.이름 } else emptyList()
}

/**
 * 2열 격자 줄 나누기 — CSS grid 2열 · row dense 와 같다. 펼친 것(true)은 줄 전체를 차지하고,
 * 그 앞에 생긴 빈 칸은 뒤의 접힌 상자가 채운다. 결과 = 줄마다 상자 번호 (1개 또는 2개)
 */
internal fun 격자줄(펼침: List<Boolean>): List<List<Int>> {
    val 줄: MutableList<MutableList<Int?>> = mutableListOf()
    펼침.forEachIndexed { i, 넓 ->
        if (넓) {
            val r = 줄.indexOfFirst { it[0] == null && it[1] == null }
            if (r >= 0) { 줄[r][0] = i; 줄[r][1] = -1 } else 줄.add(mutableListOf(i, -1))
        } else {
            var 놓음 = false
            for (r in 줄) {
                if (r[0] == null) { r[0] = i; 놓음 = true; break }
                if (r[1] == null) { r[1] = i; 놓음 = true; break }
            }
            if (!놓음) 줄.add(mutableListOf(i, null))
        }
    }
    return 줄.map { r -> r.filterNotNull().filter { it >= 0 } }.filter { it.isNotEmpty() }
}

/** − ＋ 한 번 — 무게는 무게폭씩(0 아래로 안 감 · 0.1 단위로 맞춤), 횟수는 1씩(1 아래로 안 감) */
internal fun 세트값바꿈(l: List<종목세트>, k: Int, 칸: Char, 방향: Int, 무게폭: Double): List<종목세트> {
    val x = l.getOrNull(k) ?: return l
    val 새 = when (칸) {
        'w' -> x.copy(w = max(0.0, ((x.w + 방향 * 무게폭) * 10).roundToInt() / 10.0))
        'r' -> x.copy(r = max(1, x.r + 방향))
        else -> x
    }
    return l.mapIndexed { i, y -> if (i == k) 새 else y }
}

/** 쳐서 넣은 글 — 숫자가 아니면(빈 칸 · '.') 그대로. 무게 0 이상 · 횟수 1 이상 (시안 data-in="종세트") */
internal fun 세트글넣음(l: List<종목세트>, k: Int, 칸: Char, 글: String): List<종목세트> {
    val x = l.getOrNull(k) ?: return l
    val v = 글.replace(',', '.').trim().toDoubleOrNull() ?: return l
    if (v.isNaN() || v.isInfinite()) return l
    val 새 = when (칸) {
        'w' -> x.copy(w = max(0.0, (v * 100).roundToInt() / 100.0))
        'r' -> x.copy(r = max(1, v.roundToInt()))
        else -> x
    }
    return l.mapIndexed { i, y -> if (i == k) 새 else y }
}

internal fun 세트휴식(l: List<종목세트>, k: Int, 초: Int): List<종목세트> = l.mapIndexed { i, y -> if (i == k) y.copy(휴 = 초) else y }

/** 세트 줄 지우기 — 마지막 한 줄은 남긴다 */
internal fun 세트지움(l: List<종목세트>, k: Int): List<종목세트> = if (l.size <= 1 || k !in l.indices) l else l.filterIndexed { i, _ -> i != k }

/** + 세트 — 앞 줄 값 복사, 10줄까지 (넘으면 그대로) */
internal fun 세트더함(l: List<종목세트>): List<종목세트> = if (l.isEmpty() || l.size >= 종목세트최대) l else l + l.last()

/** 종목 기본 세팅 고치기 — 지금 값(없으면 설정 기본값)에 [f] 를 적용해 종목설정[열쇠] 에 넣는다 */
internal fun 앱데이터.종목세트고침(열쇠: String, f: (List<종목세트>) -> List<종목세트>): 앱데이터 {
    val 새 = f(종목기본세트(열쇠))
    if (새.isEmpty()) return this
    return copy(종목설정 = 종목설정 + (열쇠 to 새))
}

/** 사진 넣기 · 빼기 — 그 종목 하나만 (id 로 · 같은 이름 종목은 건드리지 않는다) */
internal fun 앱데이터.종목사진더함(id: String, 이름들: List<String>): 앱데이터 =
    copy(종목표 = 종목표.map { if (it.id == id) it.copy(사진 = (it.사진 + 이름들).distinct().take(근육표.사진최대)) else it })

internal fun 앱데이터.종목사진뺌(id: String, 이름: String): 앱데이터 =
    copy(종목표 = 종목표.map { if (it.id == id) it.copy(사진 = it.사진 - 이름) else it })
