@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.slayde.hasenheide.ui

import android.graphics.Matrix
import android.graphics.Region
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.slayde.hasenheide.data.같은세트들
import com.slayde.hasenheide.data.근육계산
import com.slayde.hasenheide.data.근육자료
import com.slayde.hasenheide.data.근육표
import com.slayde.hasenheide.data.무게글
import com.slayde.hasenheide.data.사전종목
import com.slayde.hasenheide.data.새종목id
import com.slayde.hasenheide.data.앱데이터
import com.slayde.hasenheide.data.이름추천
import com.slayde.hasenheide.data.종목
import com.slayde.hasenheide.data.종목근육
import com.slayde.hasenheide.data.종목기본세트
import com.slayde.hasenheide.data.종목더하기
import com.slayde.hasenheide.data.종목사전
import com.slayde.hasenheide.data.종목세트
import com.slayde.hasenheide.data.종목세트최대
import com.slayde.hasenheide.data.칸
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.그림칸
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.근육팝치수
import com.slayde.hasenheide.ui.theme.글꼴
import com.slayde.hasenheide.ui.theme.높이
import com.slayde.hasenheide.ui.theme.모서리
import com.slayde.hasenheide.ui.theme.부품치수
import com.slayde.hasenheide.ui.theme.선굵기
import com.slayde.hasenheide.ui.theme.움직임
import com.slayde.hasenheide.ui.theme.크기

/**
 * 새 종목 / 종목 편집 시트 (시안 v18 C ⑤ · v19 D · v20 ③⑤ `새종목시트`) — 종목 화면 · 종목 넣기 시트(루틴 · 운동 중)가 **같이** 쓴다.
 *
 * ── 2단계 소유: EX(종목). 매개변수(이름 · 꼴)는 바꾸지 않는다 — RT · W 는 부르기만 한다 ──
 *
 * 시안 순서: [이름 칸 ··· 돋보기] → 찾은 줄(초성 검색 `종목사전.찾기`) → (이름이 정해지면) 카테고리 칩 →
 * 운동 목표 부위(근육 그림 앞 · 뒤 — 누르면 그 묶음의 세부 부위 팝업 · 10-06 v22 ⑧) → 기본 세팅 세트 줄 → [저장].
 *  · 돋보기 = 찾기 켜기(칸에 손가락이 가도 켜진다) · 켜지면 그 자리가 [확인] — 친 이름으로 정한다 (자판 '완료' 도 같다)
 *  · 사전에서 고르면 칸 · 근육이 채워진다. 사전 칸이 카테고리에 없으면(예: '맨몸') 칸은 비운다 → 저장 전에 골라야 한다
 *  · 이름 칸 글자가 한 글자라도 바뀌면 근육 · 묶음 · 팝업을 비운다(편집은 그대로) — 정할 때 사전 → 저장된 같은 이름 → 빈 채로 (v22 ⑨)
 *  · 같은 이름도 저장한다(새 id). 칸 필수. **주동근이 없으면 저장하지 않고** '근육 사진을 눌러서…' 토스트 (10-06 v22 ⑩)
 *  · 편집([편집]): 값이 채워진 채 · 이름 검색은 이름만 바꾼다 · [저장] = 그 종목을 고친다. 플랜이 걸린 종목은 이름을 못 바꾼다
 *
 * @param 편집 null = 새 종목, 아니면 그 종목을 고친다
 * @param 처음이름 이름 칸에 미리 넣을 글 (넣기 시트에서 찾던 글)
 * @param 저장 저장된 종목(새 id 포함)을 돌려준다 — 부른 쪽이 바로 넣을 때 쓴다. 저장 뒤 [닫기] 도 부른다
 */
@Composable
fun 새종목시트(
    상태: 앱상태,
    닫기: () -> Unit,
    저장: (종목) -> Unit = {},
    편집: 종목? = null,
    처음이름: String = "",
) {
    val c = Local색.current
    val d = 상태.d
    var v by remember { mutableStateOf(if (편집 != null) d.편집초기(편집) else d.새초기(처음이름)) }
    var 저장됨 by remember { mutableStateOf(false) }   // 빠르게 두 번 눌러도 한 번만 저장
    val 초점 = remember { FocusRequester() }
    val 자판 = LocalFocusManager.current

    fun 확인(): Boolean {
        val (n, 오류) = v.확인(상태.d)
        if (오류 != null) { 상태.알림.토스트(오류); return false }
        v = n; return true
    }
    fun 저장하기() {
        if (저장됨) return
        if (v.찾는중 && !확인()) return
        상태.d.저장검사(v)?.let { 상태.알림.토스트(it); return }
        if (v.편집 != null) {
            val r = 상태.d.종목고침(v)
            val 오류 = r.오류
            if (오류 != null) { 상태.알림.토스트(오류); return }
            저장됨 = true
            val nd = r.d
            val t = r.종목
            if (nd != null && t != null) {
                상태.바꿈 { nd }
                상태.알림.토스트("저장했습니다 · ${t.이름}")
                저장(t)
            }
        } else {
            저장됨 = true
            val (nd, t) = 상태.d.새종목저장(v)
            상태.바꿈 { nd }
            상태.알림.토스트("만들었습니다 · ${t.이름}")
            저장(t)
        }
        닫기()
    }

    시트(if (편집 != null) "${편집.이름} 편집" else "새 종목", 닫기, 위끝고정 = true, 위끝 = 새시트치수.위끝) {
        // ── 이름 칸 + 돋보기/확인 (시안 .새찾기줄 40) ──
        val 모양 = RoundedCornerShape(모서리.작게)
        var 칸초점 by remember { mutableStateOf(false) }
        Row(
            Modifier.fillMaxWidth().height(높이.보통).clip(모양).background(c.면)
                .border(선굵기.보통, if (칸초점) c.강조 else c.속선, 모양),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.weight(1f).padding(horizontal = 간격.보통), contentAlignment = Alignment.CenterStart) {
                if (v.이름.isEmpty()) 글("종목 이름", 색 = c.옅음)
                BasicTextField(
                    value = v.이름,
                    onValueChange = { t -> v = v.이름바꿈(t) },   // 10-06 v22 ⑨ 한 글자라도 바뀌면 근육을 비운다
                    singleLine = true,
                    textStyle = 글꼴.보통(크기.본문).copy(color = c.글),
                    cursorBrush = SolidColor(c.강조),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { v = v.copy(찾는중 = true); if (확인()) 자판.clearFocus() }),
                    modifier = Modifier.fillMaxWidth().focusRequester(초점).onFocusChanged {
                        칸초점 = it.isFocused
                        if (it.isFocused && !v.찾는중) v = v.copy(찾는중 = true)
                    },
                )
            }
            Box(
                Modifier.width(새시트치수.찾기단추).fillMaxHeight().background(if (v.찾는중) c.강조 else c.면)
                    .눌림 {
                        if (!v.찾는중) { v = v.copy(찾는중 = true); try { 초점.requestFocus() } catch (_: Exception) { } }
                        else if (확인()) 자판.clearFocus()
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (v.찾는중) 글("확인", 크기값 = 크기.버튼, 색 = c.강조글, 굵기 = FontWeight.Bold)
                else Icon(아이콘.돋보기, "찾기", Modifier.size(새시트치수.돋보기), tint = c.흐림)
            }
        }
        // ── 찾은 줄 · 안내 (시안 `새찾기결과`) ──
        val 안내 = "초성으로도 찾습니다 · 예: ㅂㅊㅍ → 벤치프레스"
        if (!v.찾는중) {
            if (!v.고름) 글(안내, Modifier.padding(top = 간격.아주좁게), 크기값 = 크기.작게, 색 = c.옅음)
        } else if (종목사전.다듬(v.이름).isEmpty()) {
            글(안내, Modifier.padding(top = 간격.아주좁게), 크기값 = 크기.작게, 색 = c.옅음)
        } else {
            val l = remember(v.이름, d.종목표) { 종목사전.찾기(v.이름, d.종목표) }
            if (l.isEmpty()) 글("사전에 없습니다", Modifier.padding(top = 간격.아주좁게), 크기값 = 크기.작게, 색 = c.옅음)
            l.forEach { x ->
                key(x.이름) {
                    val 있 = d.종목표.any { it.이름 == x.이름 }
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 높이.보통)
                            .눌림 { v = v.사전고름(상태.d, x.이름); 자판.clearFocus() },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        글(x.이름, Modifier.weight(1f), 굵기 = FontWeight.Bold, 색 = if (있) c.옅음 else c.글)
                        Box(Modifier.width(간격.좁게))
                        글(if (있) "이미 있음" else x.칸, 크기값 = 크기.작게, 색 = c.옅음)
                    }
                    구분선()
                }
            }
        }
        if (v.고름) {
            새부위고르기(상태, v) { f -> v = f(v) }
            // ── 세트 줄 (저장하면 종목설정[id]) — 10-06 홍겸 님: '기본 세팅' 글씨는 뺐다 ──
            Box(Modifier.height(간격.좁게))
            // 10-06 ⑩: 세트를 더해 [+ 세트] 가 스크롤 칸 밑으로 내려가면 그 단추(표의 맨 아래)가 보이게 한 번 올린다.
            //          그린 뒤(두 프레임 기다려 자리가 정해진 다음) 재고 한 번만 움직인다
            val 세트끝 = remember { BringIntoViewRequester() }
            var 표크기 by remember { mutableStateOf(IntSize.Zero) }
            var 전세트수 by remember { mutableIntStateOf(v.세트.size) }
            val 단추px = with(LocalDensity.current) { 높이.낮게.toPx() }
            LaunchedEffect(v.세트.size) {
                val 늘었 = v.세트.size > 전세트수
                전세트수 = v.세트.size
                if (늘었) {
                    withFrameNanos { }; withFrameNanos { }
                    val h = 표크기.height.toFloat()
                    if (h > 0f) 세트끝.bringIntoView(Rect(0f, (h - 단추px).coerceAtLeast(0f), 표크기.width.toFloat(), h))
                }
            }
            Box(Modifier.fillMaxWidth().onSizeChanged { 표크기 = it }.bringIntoViewRequester(세트끝)) {
                새세트줄표(v.세트, 상태.d.설정.무게폭, { f -> v = v.copy(세트 = f(v.세트)) }, { 상태.알림.토스트(세트최대글) })
            }
            Box(Modifier.height(간격.보통))
            버튼("저장", { 저장하기() }, Modifier.fillMaxWidth(), 주요 = true)
            // 10-06 홍겸 님: 그림 어디를 눌러도 같은 팝업 — 위 카테고리(지금 값 선택) · 아래 근육 목록 (고르면 바로 그림에 · [확인] · 바깥 · 뒤로가기로 닫힘)
            if (v.팝 != null) 근육팝(v, { f -> v = f(v) }, { 상태.알림.토스트(it) }, d.카테고리)
        }
    }
}

/**
 * 근육 그림을 누르면 뜨는 팝업 (시안 v22 B ⑧ `새팝` · 10-06 홍겸 님 고침) — **그림 어디를 눌러도** 같은 팝업이 **화면 가운데**에 뜬다.
 * 위 = 카테고리 칩(그 종목의 지금 칸이 선택됨 · 새 종목이라 칸이 없으면 아무것도 선택 안 됨 · 고르면 그림이 그 칸으로 확대)
 * 아래 = 고를 수 있는 근육 목록 — 줄마다 [주동근][협응근][빼기] 중 하나(지금 값이 눌림). 누른 부위 줄 = 강조옅음 바탕 · 강조 굵은 글(빈 곳을 눌렀으면 없음).
 * 고르면 바로 그림에 반영되고 팝업은 그대로 — [확인] · 바깥 누름 · 뒤로가기로 닫힌다.
 * 이미 주동근인 부위의 [협응근]은 막힘(흐림 · 누르면 토스트).
 * 화면 전체를 덮는 투명한 막(바깥 = 닫힘, 시트까지 닫지 않는다) 위, 가운데에 상자를 둔다
 * 생김새: 큰 상자 = [카드](2 강조 테두리 · 모서리 16 · 안 여백 14) · 줄 32 · 칩 28 · [확인] 40 (카드 안 버튼 · U4-5)
 */
@Composable
private fun 근육팝(v: 새종목값, 고침: ((새종목값) -> 새종목값) -> Unit, 토스트: (String) -> Unit, 카테고리: List<String>) {
    val c = Local색.current
    val k = v.팝 ?: return
    // 목록 = 지금 묶음(누른 부위의 묶음 · 카테고리를 고르면 그 칸의 묶음) — 없으면 누른 부위의 묶음 · 첫 묶음
    val 묶 = 종목사전.세부부위.firstOrNull { it.first == v.묶음 }
        ?: 종목사전.세부부위.firstOrNull { k in it.second }
        ?: 종목사전.세부부위.firstOrNull() ?: return
    val 닫기 = { 고침 { it.팝닫기() } }
    val 보임 = remember { MutableTransitionState(false).apply { targetState = true } }
    Popup(popupPositionProvider = 전체자리, onDismissRequest = 닫기, properties = PopupProperties(focusable = true)) {
        AnimatedVisibility(visibleState = 보임, enter = fadeIn(tween(움직임.물음)), label = "근육팝") {
            Box(
                Modifier.fillMaxSize().눌림(닫기).padding(간격.보통),   // 팝업 창 = 보이는 화면(시스템 띠 빼고)
                contentAlignment = Alignment.Center,
            ) {
                카드(Modifier.눌림 { }) {   // 팝업 안 빈 곳 — 바깥 막으로 번지지 않게
                    칩줄(카테고리, v.칸, { p -> 고침 { it.칸고름(p) } })
                    Column(Modifier.padding(top = 간격.좁게).heightIn(max = 새시트치수.팝목록최대).verticalScroll(rememberScrollState())) {
                        묶.second.forEach { p ->
                            key(p) {
                                val 누른 = p == k
                                val 지금 = v.근육[p] ?: 팝빼기
                                Row(
                                    Modifier.padding(top = 간격.아주좁게).fillMaxWidth().heightIn(min = 높이.낮게)
                                        .clip(RoundedCornerShape(모서리.작게))
                                        .then(if (누른) Modifier.background(c.강조옅음) else Modifier)
                                        .padding(start = 간격.좁게, end = 간격.아주좁게),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(간격.아주좁게),
                                ) {
                                    글(근이름(p), Modifier.weight(1f), 색 = if (누른) c.강조 else c.글,
                                        굵기 = if (누른) FontWeight.Bold else FontWeight.Medium)
                                    팝역할들.forEach { (r, 이름) ->
                                        val 막힘 = 팝막힘(v.근육, p, r)
                                        팝칩(이름, 지금 == r, 막힘) {
                                            if (막힘) 토스트(주동근막힘글)
                                            else 고침 { it.팝역할(p, r) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    버튼("확인", 닫기, Modifier.fillMaxWidth().padding(top = 간격.좁게), 주요 = true, 작게 = true)
                }
            }
        }
    }
}

/** 팝업 칩 하나 — 칩줄과 같은 생김새(켬 = 강조 칠 · 끔 = 속선만 · 글 13 Medium)에 높이 28 · 최소 폭 56 · 막힘 흐림 */
@Composable
private fun 팝칩(글자: String, 켬: Boolean, 막힘: Boolean, 누름: () -> Unit) {
    val c = Local색.current
    val 바탕 = 색움직(if (켬) c.강조 else c.면, "팝칩")
    val 테두리 = 색움직(if (켬) c.강조 else c.속선, "팝칩테두리")
    Box(
        Modifier.height(높이.아주낮게).widthIn(min = 근육팝치수.칩폭).alpha(if (막힘) 근육팝치수.막힘투명 else 1f)
            .clip(CircleShape).background(바탕).border(선굵기.보통, 테두리, CircleShape)
            .눌림(누름).padding(horizontal = 간격.좁게),
        contentAlignment = Alignment.Center,
    ) { 글(글자, 크기값 = 크기.버튼, 색 = 색움직(if (켬) c.강조글 else c.흐림, "팝칩글"), 굵기 = FontWeight.Medium) }
}

/** 팝업 자리 — 창 전체 (안의 막이 화면을 덮고 상자는 아래에 붙는다) */
private object 전체자리 : PopupPositionProvider {
    override fun calculatePosition(anchorBounds: IntRect, windowSize: IntSize, layoutDirection: LayoutDirection, popupContentSize: IntSize) = IntOffset.Zero
}

/** 이 시트만 쓰는 치수 — Theme 에 없는 값 (보고서 '공용 고칠 것' — 합칠 때 부품치수로 옮긴다) */
internal object 새시트치수 {
    val 찾기단추 = 44.dp       // 시안 .새찾기단추 44 × 40
    val 돋보기 = 18.dp          // U3-6 기본 아이콘 (시안 22 — 지침에 없는 값이라 18)
    /** 10-06 홍겸 님: 시트 위끝 = 화면 높이의 5% (공용 [시트] 에 매개변수가 생기면 이 값을 넘긴다) */
    const val 위끝 = 0.05f
    val 팝목록최대 = 320.dp     // 팝업 근육 목록이 이보다 길면 안에서 넘긴다
    val 번호칸 = 28.dp          // 세트 번호 칸 — 머리 글 '세트' 가 …로 잘리지 않을 폭 (공용 16 → 28)
    /** 무게 칸을 좌우 15% 줄인다 (홍겸 님): 무게 · 횟수 · 휴식 = 83 : 70 : 79 에서 무게 몫이 15% 줄도록 몫을 다시 구한 값 */
    const val 무게줄임 = 0.85f
    val 열무게: Float = 무게줄임 * 종목치수.열무게 * (종목치수.열횟수 + 종목치수.열휴식) /
        (종목치수.열무게 + 종목치수.열횟수 + 종목치수.열휴식 - 무게줄임 * 종목치수.열무게)
}

/**
 * 새 종목 시트의 세트 줄 목록 — 종목 탭의 [세트줄표](ExerciseScreen.kt)와 같은 동작에 10-06 홍겸 님 고침 둘:
 *  · 머리 '세트' 가 …로 잘리지 않게 번호 칸을 넓히고, 무게 칸을 좌우 15% 줄인다([새시트치수])
 *  · 쳐 넣는 중(칸에 초점)에도 − ＋ 를 누르면 화면 숫자가 바로 바뀐다([새세트값칸] · 전에는 친 글이 남아 숫자가 안 바뀌었다)
 * 합칠 때 공용 [세트줄표] 로 되돌린다 (원인 · 고칠 곳은 [새세트값칸] 설명)
 */
@Composable
private fun 새세트줄표(세트: List<종목세트>, 무게폭: Double, 바꿈: ((List<종목세트>) -> List<종목세트>) -> Unit, 최대알림: () -> Unit) {
    val c = Local색.current
    Column(verticalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
        Row(horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
            글("세트", Modifier.width(새시트치수.번호칸), 크기값 = 크기.작게, 색 = c.옅음, 가운데 = true)
            글("무게 kg", Modifier.weight(새시트치수.열무게), 크기값 = 크기.작게, 색 = c.옅음, 가운데 = true)
            글("횟수", Modifier.weight(종목치수.열횟수), 크기값 = 크기.작게, 색 = c.옅음, 가운데 = true)
            글("휴식", Modifier.weight(종목치수.열휴식), 크기값 = 크기.작게, 색 = c.옅음, 가운데 = true)
            Box(Modifier.width(종목치수.지움칸))
        }
        val 하나 = 세트.size <= 1
        세트.forEachIndexed { k, x ->
            key(k) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                    글("${k + 1}", Modifier.width(새시트치수.번호칸), 크기값 = 크기.조금작게, 굵기 = FontWeight.Bold, 가운데 = true)
                    새세트값칸(무게글(x.w), Modifier.weight(새시트치수.열무게), "${k + 1}세트 무게", x.w > 0,
                        { 바꿈 { l -> 세트값바꿈(l, k, 'w', -1, 무게폭) } }, { 바꿈 { l -> 세트값바꿈(l, k, 'w', 1, 무게폭) } },
                        { g -> 바꿈 { l -> 세트글넣음(l, k, 'w', g) } })
                    새세트값칸("${x.r}", Modifier.weight(종목치수.열횟수), "${k + 1}세트 횟수", x.r > 1,
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

/**
 * − 값 ＋ (가운데 = 쳐서 넣는 칸) — 높이 28. 10-06 홍겸 님 버그: 쳐 넣는 칸(초점)이 켜져 있으면 − ＋ 를 눌러도 화면 숫자가 그대로였다
 * (35kg 에서 ＋ 3번 → 화면은 35 · 세트를 더하면 38 으로 복사됨).
 * 원인: ExerciseScreen.kt [세트값칸] 이 초점 동안 `value = 친글`(친 글 그대로)만 그리고, − ＋ 가 바꾼 저장값(값글)은 친글에 안 들어갔다
 *      (저장값은 바뀌어서 세트 복사에는 새 값이 쓰였다). Common.kt 숫자입력과는 무관.
 * 고침: − ＋ 를 누르면 [버튼값] 을 켜서 저장값을 보이고, 다시 치기 시작하면([onValueChange]) 끄고 친 글을 보인다. 초점이 나가면 끈다.
 */
@Composable
private fun 새세트값칸(
    값글: String, modifier: Modifier, 이름: String, 뺄수있음: Boolean,
    빼기: () -> Unit, 더하기: () -> Unit, 넣기: (String) -> Unit, 정수: Boolean = false,
) {
    val c = Local색.current
    val 모양 = RoundedCornerShape(모서리.작게)
    var 초점 by remember { mutableStateOf(false) }
    var 친글 by remember { mutableStateOf(값글) }
    var 버튼값 by remember { mutableStateOf(false) }   // − ＋ 로 바뀐 뒤 — 칸에 저장값을 보인다
    LaunchedEffect(값글, 초점) { if (!초점) 친글 = 값글 }
    val 자판 = LocalFocusManager.current
    Row(modifier.height(높이.아주낮게).clip(모양).border(선굵기.보통, c.속선, 모양), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(부품치수.값칸단추).fillMaxHeight().then(if (뺄수있음) Modifier.눌림 { 버튼값 = true; 빼기() } else Modifier), contentAlignment = Alignment.Center) {
            Icon(아이콘.빼기, "$이름 빼기", Modifier.size(종목치수.값그림), tint = if (뺄수있음) c.강조 else c.옅음)
        }
        BasicTextField(
            value = if (초점 && !버튼값) 친글 else 값글,
            onValueChange = { t -> 버튼값 = false; 친글 = t; 넣기(t) },
            singleLine = true,
            textStyle = 글꼴.보통(크기.버튼, FontWeight.Bold).copy(color = c.글, textAlign = TextAlign.Center, fontFeatureSettings = "tnum"),
            cursorBrush = SolidColor(c.강조),
            keyboardOptions = KeyboardOptions(keyboardType = if (정수) KeyboardType.Number else KeyboardType.Decimal, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { 자판.clearFocus() }),
            modifier = Modifier.weight(1f).onFocusChanged { 초점 = it.isFocused; if (!it.isFocused) 버튼값 = false },
        )
        Box(Modifier.width(부품치수.값칸단추).fillMaxHeight().눌림 { 버튼값 = true; 더하기() }, contentAlignment = Alignment.Center) {
            Icon(아이콘.더하기, "$이름 더하기", Modifier.size(종목치수.값그림), tint = c.강조)
        }
    }
}

/**
 * 카테고리 칩 → 운동 목표 부위 (그림 앞 · 뒤) — 시안 `새부위고르기`.
 * 10-06 ⑧⑪ 홍겸 님: 그림은 운동 화면 그림 칸과 같은 규격(높이 124 · 둥근 판). 카테고리를 고르기 전엔 전신, 고르면 상체 · 하체 확대.
 * 주동근 칩 · 역할 칩 · 요약 상자는 뺐다 — 그림의 근육을 누르면 [근육팝] (v22 ⑧ · 시안의 아래 칩 줄은 앱에서 이미 뺐다 · ⑪ 유지)
 */
@Composable
private fun 새부위고르기(상태: 앱상태, v: 새종목값, 고침: ((새종목값) -> 새종목값) -> Unit) {
    val d = 상태.d
    // 빠르게 연달아 눌러도 가장 새 값에서 (고침 = 지금 값 → 새 값). 10-06 v22 ⑧ 바로 칠하지 않고 팝업을 연다
    //  10-06 홍겸 님: 그림 어디를 눌러도 팝업 — 근육 조각이면 그 부위 줄 강조, 빈 곳이면 강조 없이 (카테고리 칩은 팝업 위로 옮겼다 · '카테고리' 글씨도 뺐다)
    val 누름 = { 키들: List<String> -> 고침 { cur -> if (키들.isEmpty()) cur.팝빈열기() else cur.팝열기(키들) } }
    이름표("운동 목표 부위", Modifier.padding(top = 간격.좁게, start = 간격.아주좁게))
    Box(Modifier.height(간격.아주좁게))
    val 단계 = remember(v.근육) { 새몸단계(v.근육) }
    val (앞상자, 뒤상자) = 근육계산.부위상자(v.칸)
    Row(Modifier.fillMaxWidth().height(그림칸.높이), horizontalArrangement = Arrangement.spacedBy(그림칸.사이)) {
        새몸칸(단계, d.설정.색표, 앞상자, false, 누름, Modifier.weight(1f))
        새몸칸(단계, d.설정.색표, 뒤상자, true, 누름, Modifier.weight(1f))
    }
}

/**
 * 근육 그림 한 장 (앞 또는 뒤) — 운동 화면 `그림판` 과 같은 둥근 판 + 몸그림(MuscleView) 그대로.
 * 근육 조각을 누르면 그 부위를 [누름]. 누른 자리 판정은 그림과 같은 칸(안쪽 여백 뒤)에서 한다
 */
@Composable
private fun 새몸칸(단계: Map<String, Double>, 색표: String, 자르기: FloatArray, 뒤: Boolean, 누름: (List<String>) -> Unit, modifier: Modifier) {
    val c = Local색.current
    val 누름최신 by rememberUpdatedState(누름)
    val 자르기최신 by rememberUpdatedState(자르기)
    val 모양 = RoundedCornerShape(그림칸.모서리)
    Box(modifier.fillMaxHeight().clip(모양).background(c.면).border(선굵기.보통, c.선, 모양)) {
        Box(
            Modifier.fillMaxSize().padding(간격.아주좁게).pointerInput(뒤) {
                detectTapGestures { o ->
                    누름최신(그림누른부위(o.x, o.y, size.width.toFloat(), size.height.toFloat(), 자르기최신, 뒤) ?: emptyList())
                }
            },
        ) { 몸그림(단계, 색표, 자르기, Modifier.fillMaxSize()) }
    }
}

/** 세부 부위 조각의 누르는 영역 — 그림 좌표 × 10 (Region 은 정수). 부위 = 조각의 근육 + 그 조각이 대신 그리는 세부 부위 */
private val 누름영역: List<Triple<Boolean, List<String>, Region>> by lazy {
    val 판 = Region(-1000, -1000, 10000, 10000)
    근육자료.조각.mapNotNull { p ->
        if (p.종류 != 'm' || p.근육 !in 종목사전.세부키) return@mapNotNull null
        try {
            val 길 = PathParser().parsePathString(p.d).toPath().asAndroidPath()
            길.transform(Matrix().apply { if (p.뒤) postTranslate(근육표.뒤옮김, 0f); postScale(10f, 10f) })
            Triple(p.뒤, (listOf(p.근육) + p.대신).filter { it in 종목사전.세부키 }.distinct(), Region().apply { setPath(길, 판) })
        } catch (_: Exception) { null }
    }
}

/** 누른 자리(칸 px) → 그 조각의 세부 부위들(첫째 = 조각의 근육). 몸그림과 같은 맞춤(가운데 · 비율 유지 · `근육계산.그림좌표`) */
private fun 그림누른부위(x: Float, y: Float, w: Float, h: Float, 자르기: FloatArray, 뒤: Boolean): List<String>? {
    val (gx, gy) = 근육계산.그림좌표(x, y, w, h, 자르기) ?: return null
    val ix = (gx * 10f).toInt()
    val iy = (gy * 10f).toInt()
    return 누름영역.lastOrNull { it.first == 뒤 && it.third.contains(ix, iy) }?.second
}

// ═════════════════════ 순수 계산 (시험: test/…/ui/ExerciseTest.kt) ═════════════════════

private val 역순 = mapOf("P" to 3, "S" to 2, "Y" to 1)
/** 그림 색 — 역할마다 한 단계 (주동 20 · 협응 5 · 시안 `새몸단계`) */
internal val 새몸단계값 = mapOf("P" to 20.0, "Y" to 5.0)
internal val 세트최대글 = "${종목세트최대}세트까지"
/** 주동근 없이 [저장] 을 눌렀을 때 — 저장하지 않는다 (10-06 ⑪ 홍겸 님 문구 그대로 · v22 ⑩ 막음) */
internal const val 근육설정안내 = "근육 사진을 눌러서 목표 근육을 설정하세요"
/** 이미 주동근인 부위에 [협응근] (시안 v20 ⑤ 문구 그대로) */
internal const val 주동근막힘글 = "이미 주동근으로 선택되어있습니다."
/** 팝업이 떠 있지만 누른 근육 부위는 없다 (그림의 빈 곳을 눌렀다) — [새종목값.팝] 의 값 */
internal const val 팝없음 = ""
/** 팝업의 [빼기] 값 (근육 맵에 없음) */
internal const val 팝빼기 = "-"
/** 팝업 줄의 칩 — 값 · 글 (시안 `팝역할`) */
internal val 팝역할들 = listOf("P" to "주동근", "Y" to "협응근", 팝빼기 to "빼기")

/** 시트의 값 — 화면 상태를 한 덩어리로 (copy 로만 바꾼다) */
internal data class 새종목값(
    val 이름: String = "",
    val 칸: String? = null,
    val 찾는중: Boolean = false,
    /** 이름이 정해졌나 — 정해지면 카테고리 · 부위 · 세팅이 보인다 */
    val 고름: Boolean = false,
    /** 사전에서 고른 이름 */
    val 사전: String? = null,
    /** 마지막으로 정한 이름 (v22 ⑨) — 이 이름 그대로 [확인] 하면(칸을 눌러 찾기만 켰다) 고친 근육을 그대로 둔다 */
    val 정한: String? = null,
    /** 그림에서 누른 세부 부위 — null 아니면 팝업이 떠 있다 (v22 ⑧) */
    val 팝: String? = null,
    val 근육: Map<String, String> = emptyMap(),
    val 묶음: String = "가슴",
    val 역할: String = "P",
    /** 카테고리를 손으로 골랐나 — 고른 뒤에는 사전이 비우지 않는다 */
    val 칸직접: Boolean = false,
    val 세트: List<종목세트> = emptyList(),
    /** 편집 중인 종목 id (null = 새 종목) */
    val 편집: String? = null,
    /** 편집에서만 — 1RM 목표 · 달력 이름 (앱에만 있는 칸) */
    val 목표글: String = "",
    val 달력글: String = "",
)

internal fun 근이름(id: String): String = (근육자료.이름[id] ?: id).replace(Regex(" \\(.*\\)"), "")

/** "주동근 : …" / "협응근 : …" 의 뒷부분 (협응 줄 = 보조 S + 협응 Y) — 시안 `근육두줄`. 없으면 "—" */
internal fun 근육두줄(m: Map<String, String>): Pair<String, String> {
    fun 줄(rs: Set<String>) = m.entries.filter { it.value in rs }.sortedByDescending { 역순[it.value] ?: 0 }
        .joinToString(", ") { 근이름(it.key) }.ifEmpty { "—" }
    return 줄(setOf("P")) to 줄(setOf("S", "Y"))
}

/** 그림에 칠할 단계 — 잎마다 역할의 단계 중 큰 것 (시안 `새몸그림`) */
internal fun 새몸단계(m: Map<String, String>): Map<String, Double> {
    val 단 = HashMap<String, Double>()
    for ((k, r) in m) for (l in 근육계산.잎(k)) 단[l] = maxOf(단[l] ?: 0.0, 새몸단계값[r] ?: 0.0)
    return 단
}

/** 기본 묶음 — 칸과 같은 묶음 · 없으면 첫 주동근의 묶음 · 없으면 첫 묶음 (시안 `기본묶음`) */
internal fun 새종목값.기본묶음(): String {
    val 묶들 = 종목사전.세부부위
    칸?.let { k -> if (묶들.any { it.first == k }) return k }
    val p = 근육.entries.firstOrNull { it.value == "P" }?.key
    return (p?.let { k -> 묶들.firstOrNull { k in it.second }?.first }) ?: 묶들[0].first
}

/** 새 종목 — 칸은 비워 두고 시작 (v19 D ③) · 세트 = 설정의 기본 세트 수 × 20kg · 10회 · 기본 휴식 */
internal fun 앱데이터.새초기(처음이름: String = ""): 새종목값 =
    새종목값(이름 = 처음이름, 찾는중 = 처음이름.isNotBlank(), 세트 = 같은세트들(설정.기본세트, 20.0, 10, 설정.기본휴식))

/** 편집 — 값을 채워서 (시안 `종목편집`) */
internal fun 앱데이터.편집초기(t: 종목): 새종목값 {
    val v = 새종목값(
        이름 = t.이름, 칸 = t.칸.takeIf { it in 카테고리 }, 고름 = true, 칸직접 = true, 편집 = t.id,
        근육 = 종목사전.둘역할(종목사전.세부로(종목근육(t.id, t.이름))),
        세트 = 종목기본세트(t.id),
        목표글 = t.목표1RM?.let { 무게글(it) } ?: "", 달력글 = t.달력이름 ?: "",
    )
    return v.copy(묶음 = v.기본묶음())
}

/** 사전 종목을 들인다 — 이름 · 칸(카테고리에 없으면 비움, 손으로 고른 칸은 둠) · 근육(P · Y 둘) (시안 `사전적용`) */
internal fun 새종목값.사전적용(x: 사전종목, 카테고리: List<String>): 새종목값 {
    val n = copy(
        이름 = x.이름, 칸 = if (x.칸 in 카테고리) x.칸 else if (칸직접) 칸 else null,
        근육 = 종목사전.둘역할(종목사전.사전근육(x)), 사전 = x.이름, 정한 = x.이름, 팝 = null,
    )
    return n.copy(묶음 = n.기본묶음())
}

/** [확인] — 친 이름으로 정한다 (시안 `새확인`). 오류면 (그대로, 글) */
internal fun 새종목값.확인(d: 앱데이터): Pair<새종목값, String?> {
    val n = 이름.trim()
    if (n.isEmpty()) return this to "이름을 넣어 주세요"
    if (편집 != null) return copy(이름 = n, 찾는중 = false) to null   // 편집은 이름만 바꾼다 (근육 · 카테고리 · 세팅은 그대로)
    var v = copy(이름 = n)
    // 10-06 v22 ⑨ 정한 이름 그대로면 고친 근육 그대로. 이름이 바뀌었으면 언제나 새로 채운다 —
    //  사전 이름(별칭 포함)이면 사전 값 · 아니고 저장된 같은 이름 종목이면 그 종목 근육 · 둘 다 아니면 빈 채로(낱말 짐작 안 함).
    //  (전에는 `if (!고름)` 이라 한 번 정한 뒤 다른 이름으로 바꾸면 앞 종목 근육이 남았다 — 시안과 같은 원인)
    if (!(고름 && 정한 == n)) {
        val x = 종목사전.목록.firstOrNull { it.이름 == n || n in it.별 }
        if (x != null) v = v.사전적용(x, d.카테고리)
        else {
            val 있 = d.종목표.firstOrNull { it.이름 == n }
            if (!칸직접) v = v.copy(칸 = 있?.칸?.takeIf { it in d.카테고리 })
            val m = if (있 != null) 종목사전.세부로(d.종목근육(있.id, 있.이름)) else emptyMap()
            v = v.copy(근육 = 종목사전.둘역할(m), 사전 = null, 팝 = null)
            v = v.copy(묶음 = v.기본묶음())
        }
    }
    return v.copy(정한 = v.이름, 찾는중 = false, 고름 = true) to null
}

/** 찾은 줄을 눌렀다 (시안 `새사전고름`) — 편집이면 이름만 */
internal fun 새종목값.사전고름(d: 앱데이터, 이름값: String): 새종목값 {
    if (편집 != null) return copy(이름 = 이름값, 찾는중 = false)
    val x = 종목사전.이름으로(이름값)
    val v = if (x != null) 사전적용(x, d.카테고리) else copy(이름 = 이름값, 고름 = false).확인(d).first
    return v.copy(찾는중 = false, 고름 = true)
}

/** 카테고리 칩 — 손으로 고름. 같은 묶음이 있으면 그리로 */
internal fun 새종목값.칸고름(k: String): 새종목값 =
    copy(칸 = k, 칸직접 = true, 묶음 = if (종목사전.세부부위.any { it.first == k }) k else 묶음)

/**
 * 이름 칸에 친 글 (v22 ⑨) — 한 글자라도 바뀌면 근육 · 사전 · 정한 이름 · 팝업 · 묶음을 비운다(편집은 그대로).
 * 카테고리는 그대로 둔다(손으로 고른 칸 · 확대 상태 유지) — 이름을 정할 때 [확인] 이 다시 채운다
 */
internal fun 새종목값.이름바꿈(t: String): 새종목값 {
    val v = copy(이름 = t, 찾는중 = true)
    if (t == 이름 || 편집 != null) return v
    val n = v.copy(근육 = emptyMap(), 사전 = null, 정한 = null, 팝 = null)
    return n.copy(묶음 = n.기본묶음())
}

/**
 * 그림의 근육 조각을 눌렀다 (v22 ⑧) — 칠하지 않고 팝업을 연다. [키들] 첫째 = 조각의 근육(누른 부위 줄로 강조).
 * 그 부위의 묶음으로 옮긴다. 근육이 아닌 곳(빈 목록)은 그대로
 */
internal fun 새종목값.팝열기(키들: List<String>): 새종목값 {
    val k = 키들.firstOrNull() ?: return this
    val g = 종목사전.세부부위.firstOrNull { k in it.second }?.first ?: return this
    return copy(팝 = k, 묶음 = g)
}

/** 그림의 근육이 아닌 곳을 눌렀다 (10-06) — 그래도 팝업은 뜬다. 누른 부위 강조 없이([팝없음]) 지금 묶음의 목록 */
internal fun 새종목값.팝빈열기(): 새종목값 = copy(팝 = 팝없음)

internal fun 새종목값.팝닫기(): 새종목값 = copy(팝 = null)

/** 팝업의 [협응근] 이 막혔나 — 이미 주동근인 부위 */
internal fun 팝막힘(m: Map<String, String>, 부위: String, 역할: String): Boolean = 역할 == "Y" && m[부위] == "P"

/** 팝업 칩 — 그 부위의 역할을 [역할](P · Y · [팝빼기]) 로. 막힌 것(주동근 → 협응근)은 그대로 */
internal fun 새종목값.팝역할(부위: String, 역할: String): 새종목값 {
    if (부위 !in 종목사전.세부키 || 팝막힘(근육, 부위, 역할)) return this
    val m = LinkedHashMap(근육)
    if (역할 == "P" || 역할 == "Y") m[부위] = 역할 else m.remove(부위)
    return copy(근육 = m)
}

/**
 * [저장] 전에 — 이름 · 칸(필수) · 주동근(필수) (시안 `새저장` 순서). 오류 글이 있으면 저장하지 않는다.
 * 10-06 v22 ⑩: 주동근이 없으면 막고 [근육설정안내] — 종목 탭 · 루틴 넣기 · 운동 중 넣기가 모두 이 시트로 만든다 (편집도 같다)
 */
internal fun 앱데이터.저장검사(v: 새종목값): String? = when {
    v.이름.trim().isEmpty() -> "이름을 넣어 주세요"
    v.칸 == null || v.칸 !in 카테고리 -> "반드시 카테고리를 지정해야 합니다"
    "P" !in v.근육.values -> 근육설정안내
    else -> null
}

/** 새 종목 저장 — 같은 이름도 새 id (`종목더하기`). 장비는 이름으로 짐작해 채운다(앱 원래 동작 · 시안에는 장비 칸이 없다) */
internal fun 앱데이터.새종목저장(v: 새종목값, 지금: Long = System.currentTimeMillis()): Pair<앱데이터, 종목> {
    val n = v.이름.trim()
    val e = 종목(n, v.칸 ?: "", 장비 = 이름추천.추측하기(n).장비 ?: "", id = 새종목id(지금), 근육 = LinkedHashMap(v.근육))
    val nd = 종목더하기(e, v.세트.ifEmpty { null }, 지금)
    return nd to nd.종목표.last()
}

/** 플랜이 걸린 종목인가 — 그 이름의 첫 종목이고 그 이름으로 만든 플랜이 있다 (플랜은 첫 종목에 붙는다) */
internal fun 앱데이터.플랜걸림(t: 종목): Boolean =
    종목표.firstOrNull { it.이름 == t.이름 }?.id == t.id && 플랜들.any { it.종목 == t.이름 }

/** 편집 저장 결과 — [d] null 이면 종목이 없어졌다(그냥 닫는다) · [오류] 가 있으면 저장하지 않는다 */
internal data class 고침결과(val d: 앱데이터?, val 종목: 종목?, val 오류: String? = null)

/**
 * 편집 저장 — 그 종목을 id 로 찾아 고친다 (시안 `종목고침`).
 *  · 플랜이 걸린 종목은 이름을 바꾸지 않는다
 *  · 옛 꼴 종목(id = 옛 이름)의 이름을 바꾸면 새 id 를 준다 — 종목설정 열쇠도 옮긴다 (사진은 종목에 붙어 있어 그대로)
 *  · 줄(루틴 · 운동 중) — 종id 가 같은 줄, 그리고 이 종목이 그 이름의 첫 종목이면 종id 없는 옛 줄(플랜 줄 빼고)도 새 id · 새 이름으로
 *  · 지난 기록도 같은 규칙으로 이름을 바꾼다 — 앱은 기록을 이름으로 찾는다 (앱 원래 `종목이름바꿈` 과 같은 뜻 · 시안은 그때 이름 그대로)
 */
internal fun 앱데이터.종목고침(v: 새종목값, 지금: Long = System.currentTimeMillis()): 고침결과 {
    val id = v.편집 ?: return 고침결과(null, null)
    val t = 종목표.firstOrNull { it.id == id } ?: return 고침결과(null, null)
    val n = v.이름.trim()
    val 옛 = t.이름
    if (n.isEmpty()) return 고침결과(null, null, "이름을 넣어 주세요")
    if (n != 옛 && 플랜걸림(t)) return 고침결과(null, null, "플랜이 있는 종목은 이름을 바꿀 수 없습니다")
    val 첫 = 종목표.firstOrNull { it.이름 == 옛 }?.id == t.id
    val 새id = if (n != 옛 && t.id == 옛) 새종목id(지금) else t.id
    fun 맞음(종id: String?, 이름: String, 플랜id: String?) =
        플랜id == null && (종id == t.id || (종id == null && 첫 && 이름 == 옛))
    val 새t = t.copy(
        id = 새id, 이름 = n, 부위 = v.칸 ?: t.부위, 근육 = LinkedHashMap(v.근육),
        목표1RM = v.목표글.replace(',', '.').trim().toDoubleOrNull()?.takeIf { it > 0 && it.isFinite() },
        달력이름 = v.달력글.trim().ifEmpty { null },
    )
    val 바뀜 = n != 옛 || 새id != t.id
    val nd = copy(
        종목표 = 종목표.map { if (it.id == t.id) 새t else it },
        종목설정 = (if (새id != t.id) 종목설정 - t.id else 종목설정) + (새id to v.세트.ifEmpty { 종목기본세트(t.id) }),
        루틴들 = if (!바뀜) 루틴들 else 루틴들.map { r ->
            r.copy(종목 = r.종목.map { if (맞음(it.종id, it.이름, it.플랜id)) it.copy(이름 = n, 종id = 새id) else it })
        },
        세션 = if (!바뀜) 세션 else 세션?.let { S ->
            S.copy(종목들 = S.종목들.map { if (맞음(it.종id, it.이름, it.플랜id)) it.copy(이름 = n, 종id = 새id) else it })
        },
        기록 = if (!바뀜) 기록 else 기록.mapValues { (_, rec) ->
            rec.copy(종목들 = rec.종목들.map {
                if (맞음(it.종id, it.이름, it.플랜id)) it.copy(
                    이름 = n, 종id = 새id,   // 10-05 감시관: 옛 줄에도 새 id — 이름만 바꾸면 같은 이름 다른 종목 기록과 섞이고, 지난 기록을 열쇠로 못 찾았다
                    묶음 = it.묶음?.split("+")?.map { x -> if (x == 옛) n else x }?.sorted()?.joinToString("+"),
                ) else it
            })
        },
    )
    return 고침결과(nd, 새t)
}

/** 이 종목을 쓰는 루틴 이름들 (종id 로 · 옛 줄은 그 이름의 첫 종목일 때 이름으로) */
internal fun 앱데이터.종목쓰는루틴(id: String): List<String> {
    val t = 종목표.firstOrNull { it.id == id } ?: return emptyList()
    val 첫 = 종목표.firstOrNull { it.이름 == t.이름 }?.id == t.id
    return 루틴들.filter { r -> r.종목.any { it.종id == id || (it.종id == null && 첫 && it.이름 == t.이름 && it.플랜id == null) } }.map { it.이름 }
}

/** 종목 지우기 — 종목 · 기본 세팅 · 이 종목을 가리키는 루틴 줄을 뺀다. 지난 기록은 그대로 (같은 이름이 남으면 옛 줄은 그쪽이 이어받는다) */
internal fun 앱데이터.종목지우기(id: String): 앱데이터 {
    val t = 종목표.firstOrNull { it.id == id } ?: return this
    val 첫 = 종목표.firstOrNull { it.이름 == t.이름 }?.id == t.id
    val 남은 = 종목표.filter { it.id != id }
    val 이름남음 = 남은.any { it.이름 == t.이름 }
    return copy(
        종목표 = 남은,
        종목설정 = 종목설정 - id,
        루틴들 = 루틴들.map { r ->
            r.copy(종목 = r.종목.filter { !(it.종id == id || (it.종id == null && 첫 && !이름남음 && it.이름 == t.이름 && it.플랜id == null)) })
        },
    )
}
