package com.slayde.hasenheide.ui

import android.graphics.Matrix
import android.graphics.Region
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
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
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.글꼴
import com.slayde.hasenheide.ui.theme.높이
import com.slayde.hasenheide.ui.theme.모서리
import com.slayde.hasenheide.ui.theme.선굵기
import com.slayde.hasenheide.ui.theme.크기
import kotlin.math.min

/**
 * 새 종목 / 종목 편집 시트 (시안 v18 C ⑤ · v19 D · v20 ③⑤ `새종목시트`) — 종목 화면 · 종목 넣기 시트(루틴 · 운동 중)가 **같이** 쓴다.
 *
 * ── 2단계 소유: EX(종목). 매개변수(이름 · 꼴)는 바꾸지 않는다 — RT · W 는 부르기만 한다 ──
 *
 * 시안 순서: [이름 칸 ··· 돋보기] → 찾은 줄(초성 검색 `종목사전.찾기`) → (이름이 정해지면) 카테고리 칩 →
 * 운동 목표 부위(근육 그림 앞 · 뒤 + 주동근/협응근 요약 · 묶음 칩 · 역할 칩 · 부위 칩) → 기본 세팅 세트 줄 → [저장].
 *  · 돋보기 = 찾기 켜기(칸에 손가락이 가도 켜진다) · 켜지면 그 자리가 [확인] — 친 이름으로 정한다 (자판 '완료' 도 같다)
 *  · 사전에서 고르면 칸 · 근육이 채워진다. 사전 칸이 카테고리에 없으면(예: '맨몸') 칸은 비운다 → 저장 전에 골라야 한다
 *  · 같은 이름도 저장한다(새 id). 칸 필수 · 주동근 하나 이상
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

    시트(if (편집 != null) "${편집.이름} 편집" else "새 종목", 닫기, 위끝고정 = true) {
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
                    onValueChange = { t -> v = v.copy(이름 = t, 찾는중 = true) },
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
            // ── 기본 세팅 (종목 탭과 같은 세트 줄 · 저장하면 종목설정[id]) ──
            이름표("기본 세팅", Modifier.padding(top = 간격.보통, start = 간격.아주좁게))
            Box(Modifier.height(간격.아주좁게))
            세트줄표(v.세트, 상태.d.설정.무게폭, { f -> v = v.copy(세트 = f(v.세트)) }, { 상태.알림.토스트(세트최대글) })
            if (편집 != null) 편집그밖(상태, 편집, v, { v = it }) { 저장됨 = true; 닫기() }
            Box(Modifier.height(간격.보통))
            버튼("저장", { 저장하기() }, Modifier.fillMaxWidth(), 주요 = true)
        }
    }
}

/** 이 시트만 쓰는 치수 — Theme 에 없는 값 (보고서 '공용 고칠 것' — 합칠 때 부품치수로 옮긴다) */
internal object 새시트치수 {
    val 찾기단추 = 44.dp       // 시안 .새찾기단추 44 × 40
    val 돋보기 = 18.dp          // U3-6 기본 아이콘 (시안 22 — 지침에 없는 값이라 18)
    val 몸폭 = 64.dp           // 시안 .새몸칸 64 × 192
    val 몸높이 = 192.dp
    val 점선 = 4.dp             // 협응근 칩 점선 마디
    const val 막힘투명 = 0.35f  // 시안 .근칩.막힘 opacity .35
}

/** 카테고리 칩 → 운동 목표 부위 (그림 앞 · 뒤 + 요약 · 묶음 칩 · 역할 칩 · 부위 칩) — 시안 `새부위고르기` */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun 새부위고르기(상태: 앱상태, v: 새종목값, 고침: ((새종목값) -> 새종목값) -> Unit) {
    val c = Local색.current
    val d = 상태.d
    // 빠르게 연달아 눌러도 가장 새 값에서 (고침 = 지금 값 → 새 값)
    val 누름 = { k: String -> 고침 { cur -> val (n, 말) = cur.근육누름(k); if (말 != null) 상태.알림.토스트(말); n } }
    이름표("카테고리", Modifier.padding(top = 간격.좁게, start = 간격.아주좁게))
    Box(Modifier.height(간격.아주좁게))
    칩줄(d.카테고리, v.칸, { k -> 고침 { it.칸고름(k) } })
    이름표("운동 목표 부위", Modifier.padding(top = 간격.보통, start = 간격.아주좁게))
    Box(Modifier.height(간격.아주좁게))
    val 단계 = remember(v.근육) { 새몸단계(v.근육) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
        새몸칸(단계, d.설정.색표, false, 누름)
        새몸칸(단계, d.설정.색표, true, 누름)
        val (주, 협) = 근육두줄(v.근육)
        Column(
            Modifier.weight(1f).clip(RoundedCornerShape(모서리.작게)).background(c.면2).padding(간격.좁게),
            verticalArrangement = Arrangement.spacedBy(간격.아주좁게),
        ) {
            글("주동근 : $주", 크기값 = 크기.작게, 색 = c.흐림, 줄 = 6)
            글("협응근 : $협", 크기값 = 크기.작게, 색 = c.흐림, 줄 = 6)
        }
    }
    Box(Modifier.height(간격.좁게))
    칩줄(종목사전.세부부위.map { it.first }, v.묶음, { g -> 고침 { it.copy(묶음 = g) } })
    Box(Modifier.height(간격.좁게))
    칩줄(역할칩.map { it.second }, 역할칩.firstOrNull { it.first == v.역할 }?.second, { g -> 고침 { it.copy(역할 = 역할칩.first { r -> r.second == g }.first) } })
    Box(Modifier.height(간격.좁게))
    val 묶 = 종목사전.세부부위.firstOrNull { it.first == v.묶음 } ?: 종목사전.세부부위[0]
    FlowRow(horizontalArrangement = Arrangement.spacedBy(간격.아주좁게), verticalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
        묶.second.forEach { k ->
            key(k) {
                val r = v.근육[k]
                val 막 = v.역할 == "Y" && r == "P"
                val 모양 = CircleShape
                Row(
                    Modifier.height(높이.아주낮게)
                        .graphicsLayer { alpha = if (막) 새시트치수.막힘투명 else 1f }
                        .clip(모양)
                        .background(if (r == "P") c.강조 else c.면)
                        .then(
                            when (r) {
                                "P" -> Modifier.border(선굵기.보통, c.강조, 모양)
                                "Y" -> Modifier.점선테(c.강조)
                                else -> Modifier.border(선굵기.보통, c.속선, 모양)
                            },
                        )
                        .눌림 { 누름(k) }
                        .padding(horizontal = 간격.보통),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val 색 = when (r) { "P" -> c.강조글; "Y" -> c.강조; else -> c.흐림 }
                    글(근이름(k), 크기값 = 크기.버튼, 색 = 색, 굵기 = if (r != null) FontWeight.Bold else FontWeight.Medium)
                    if (r != null) {
                        Box(Modifier.width(간격.아주좁게))
                        글(역할짧은[r] ?: "", 크기값 = 크기.작게, 색 = 색, 굵기 = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/** 협응근 칩 — 점선 테두리 (시안 .근칩.역Y border-style:dashed) */
private fun Modifier.점선테(색: Color): Modifier = this.drawBehind {
    val 굵 = 선굵기.보통.toPx()
    val 마디 = 새시트치수.점선.toPx()
    drawRoundRect(
        색, topLeft = Offset(굵 / 2, 굵 / 2), size = Size(size.width - 굵, size.height - 굵),
        cornerRadius = CornerRadius(size.height / 2, size.height / 2),
        style = Stroke(width = 굵, pathEffect = PathEffect.dashPathEffect(floatArrayOf(마디, 마디))),
    )
}

/** 근육 그림 한 장 (앞 또는 뒤) — 몸그림(MuscleView) 그대로 + 세부 부위 조각을 누르면 그 부위를 [누름] (시안 `새몸그림`) */
@Composable
private fun 새몸칸(단계: Map<String, Double>, 색표: String, 뒤: Boolean, 누름: (String) -> Unit) {
    val 자르기 = if (뒤) 뒤자르기 else 앞자르기
    val 누름최신 by rememberUpdatedState(누름)
    Box(
        Modifier.size(새시트치수.몸폭, 새시트치수.몸높이).pointerInput(뒤) {
            detectTapGestures { o ->
                val k = 그림누른부위(o.x, o.y, size.width.toFloat(), size.height.toFloat(), 자르기, 뒤)
                if (k != null) 누름최신(k)
            }
        },
    ) { 몸그림(단계, 색표, 자르기, Modifier.fillMaxSize()) }
}

private val 앞자르기 = floatArrayOf(26f, 4f, 148f, 442f)   // 시안 viewBox "26 4 148 442"
private val 뒤자르기 = floatArrayOf(266f, 4f, 148f, 442f)  // 시안 viewBox "266 4 148 442"

/** 세부 부위 조각의 누르는 영역 — 그림 좌표 × 10 (Region 은 정수) */
private val 누름영역: List<Triple<Boolean, String, Region>> by lazy {
    val 판 = Region(-1000, -1000, 10000, 10000)
    근육자료.조각.mapNotNull { p ->
        if (p.종류 != 'm' || p.근육 !in 종목사전.세부키) return@mapNotNull null
        try {
            val 길 = PathParser().parsePathString(p.d).toPath().asAndroidPath()
            길.transform(Matrix().apply { if (p.뒤) postTranslate(근육표.뒤옮김, 0f); postScale(10f, 10f) })
            Triple(p.뒤, p.근육, Region().apply { setPath(길, 판) })
        } catch (_: Exception) { null }
    }
}

/** 누른 자리(칸 px) → 세부 부위 id. 몸그림과 같은 맞춤(가운데 · 비율 유지) */
private fun 그림누른부위(x: Float, y: Float, w: Float, h: Float, 자르기: FloatArray, 뒤: Boolean): String? {
    val s = min(w / 자르기[2], h / 자르기[3])
    if (s <= 0f) return null
    val tx = (w - 자르기[2] * s) / 2f - 자르기[0] * s
    val ty = (h - 자르기[3] * s) / 2f - 자르기[1] * s
    val gx = ((x - tx) / s * 10f).toInt()
    val gy = ((y - ty) / s * 10f).toInt()
    return 누름영역.lastOrNull { it.first == 뒤 && it.third.contains(gx, gy) }?.second
}

/**
 * 편집에서만 — 앱에만 있던 칸(1RM 목표 · 달력 표시 이름) + [이 종목 지우기].
 * 시안 v20 종목 상자에는 없어서(이름 · 장비 칸 · 지표 표도 없어졌다) 잃지 않게 편집 시트 아래에 둔다 (보고서 '시안과 다르게 한 것')
 */
@Composable
private fun 편집그밖(상태: 앱상태, 편집: 종목, v: 새종목값, 바꿈: (새종목값) -> Unit, 지운뒤: () -> Unit) {
    val c = Local색.current
    이름표("1RM 목표", Modifier.padding(top = 간격.보통, start = 간격.아주좁게))
    Row(Modifier.fillMaxWidth().padding(top = 간격.아주좁게), verticalAlignment = Alignment.CenterVertically) {
        입력칸(v.목표글, { 바꿈(v.copy(목표글 = it)) }, Modifier.weight(1f), 안내 = "비워두면 목표 없음", 숫자 = true)
        글("kg", Modifier.padding(horizontal = 간격.좁게), 크기값 = 크기.버튼, 색 = c.옅음)
    }
    이름표("달력 표시 이름", Modifier.padding(top = 간격.보통, start = 간격.아주좁게))
    입력칸(v.달력글, { 바꿈(v.copy(달력글 = it)) }, Modifier.fillMaxWidth().padding(top = 간격.아주좁게), 안내 = "비워두면 종목 이름을 따라갑니다")
    Box(Modifier.height(간격.보통))
    버튼("이 종목 지우기", {
        val t = 상태.d.종목표.firstOrNull { it.id == 편집.id }
        if (t != null) {
            val 쓰는곳 = 상태.d.종목쓰는루틴(t.id)
            상태.지우고알림(if (쓰는곳.isEmpty()) "${t.이름}을(를) 지웠습니다" else "${t.이름} · ${쓰는곳.joinToString("·")}에서도 뺐습니다") { it.종목지우기(t.id) }
        }
        지운뒤()
    }, Modifier.fillMaxWidth(), 작게 = true, 글색 = c.나쁨)
}

// ═════════════════════ 순수 계산 (시험: test/…/ui/ExerciseTest.kt) ═════════════════════

/** 역할 칩 — 주동근 P · 협응근 Y 둘 (v20 ⑤) */
internal val 역할칩 = listOf("P" to "주동근", "Y" to "협응근")
internal val 역할짧은 = mapOf("P" to "주동", "S" to "보조", "Y" to "협응")
private val 역순 = mapOf("P" to 3, "S" to 2, "Y" to 1)
/** 그림 색 — 역할마다 한 단계 (주동 20 · 협응 5 · 시안 `새몸단계`) */
internal val 새몸단계값 = mapOf("P" to 20.0, "Y" to 5.0)
internal val 세트최대글 = "${종목세트최대}세트까지"

/** 시트의 값 — 화면 상태를 한 덩어리로 (copy 로만 바꾼다) */
internal data class 새종목값(
    val 이름: String = "",
    val 칸: String? = null,
    val 찾는중: Boolean = false,
    /** 이름이 정해졌나 — 정해지면 카테고리 · 부위 · 세팅이 보인다 */
    val 고름: Boolean = false,
    /** 사전에서 고른 이름 (같은 사전을 다시 고르면 다시 채우지 않는다) */
    val 사전: String? = null,
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
        근육 = 종목사전.둘역할(종목사전.사전근육(x)), 사전 = x.이름,
    )
    return n.copy(묶음 = n.기본묶음())
}

/** [확인] — 친 이름으로 정한다 (시안 `새확인`). 오류면 (그대로, 글) */
internal fun 새종목값.확인(d: 앱데이터): Pair<새종목값, String?> {
    val n = 이름.trim()
    if (n.isEmpty()) return this to "이름을 넣어 주세요"
    if (편집 != null) return copy(이름 = n, 찾는중 = false) to null   // 편집은 이름만 바꾼다 (근육 · 카테고리 · 세팅은 그대로)
    val x = 종목사전.목록.firstOrNull { it.이름 == n || n in it.별 }
    var v = this
    if (x != null) {
        if (사전 != x.이름) v = v.사전적용(x, d.카테고리)
    } else {
        v = v.copy(이름 = n)
        val 있 = d.종목표.firstOrNull { it.이름 == n }
        if (!칸직접) v = v.copy(칸 = 있?.칸?.takeIf { it in d.카테고리 })
        if (!고름) {
            val m = if (있 != null) 종목사전.세부로(d.종목근육(있.id, 있.이름)) else 종목사전.세부로(종목사전.낱말근육(n) ?: emptyMap())
            v = v.copy(근육 = 종목사전.둘역할(m))
            v = v.copy(묶음 = v.기본묶음())
        }
        v = v.copy(사전 = null)
    }
    return v.copy(찾는중 = false, 고름 = true) to null
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
 * 부위를 눌렀다 (칩 · 그림) — 지금 역할로 넣고, 같은 역할이면 뺀다. 그 부위의 묶음으로 옮긴다.
 * 협응근 역할에서 이미 주동근인 부위는 그대로 + 글 (시안 v20 ⑤). 주동근 역할에서 협응근 부위는 주동으로 옮긴다
 */
internal fun 새종목값.근육누름(k: String): Pair<새종목값, String?> {
    if (역할 == "Y" && 근육[k] == "P") return this to "이미 주동근으로 선택되어있습니다."
    val m = LinkedHashMap(근육)
    if (m[k] == 역할) m.remove(k) else m[k] = 역할
    val g = 종목사전.세부부위.firstOrNull { k in it.second }?.first ?: 묶음
    return copy(근육 = m, 묶음 = g) to null
}

/** [저장] 전에 — 이름 · 칸(필수) · 주동근 (시안 `새저장`) */
internal fun 앱데이터.저장검사(v: 새종목값): String? = when {
    v.이름.trim().isEmpty() -> "이름을 넣어 주세요"
    v.칸 == null || v.칸 !in 카테고리 -> "반드시 카테고리를 지정해야 합니다"
    "P" !in v.근육.values -> "주동근을 하나 이상 골라 주세요"
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
        기록 = if (n == 옛) 기록 else 기록.mapValues { (_, rec) ->
            rec.copy(종목들 = rec.종목들.map {
                if (맞음(it.종id, it.이름, it.플랜id)) it.copy(
                    이름 = n, 종id = if (it.종id != null) 새id else null,
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
