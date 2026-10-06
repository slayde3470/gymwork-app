@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.slayde.hasenheide.ui

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.slayde.hasenheide.data.근육계산
import com.slayde.hasenheide.data.기록세트
import com.slayde.hasenheide.data.날
import com.slayde.hasenheide.data.날RM들
import com.slayde.hasenheide.data.날기록
import com.slayde.hasenheide.data.날짜만
import com.slayde.hasenheide.data.대표칭호고름
import com.slayde.hasenheide.data.무게글
import com.slayde.hasenheide.data.무게반올림
import com.slayde.hasenheide.data.시각날
import com.slayde.hasenheide.data.스탯
import com.slayde.hasenheide.data.스탯값
import com.slayde.hasenheide.data.스탯들
import com.slayde.hasenheide.data.스탯전
import com.slayde.hasenheide.data.스탯표
import com.slayde.hasenheide.data.앱데이터
import com.slayde.hasenheide.data.업적
import com.slayde.hasenheide.data.업적단위
import com.slayde.hasenheide.data.업적보임
import com.slayde.hasenheide.data.업적보임바꿈
import com.slayde.hasenheide.data.업적진행들
import com.slayde.hasenheide.data.업적표
import com.slayde.hasenheide.data.정식이름
import com.slayde.hasenheide.data.종목기록
import com.slayde.hasenheide.data.콤마
import com.slayde.hasenheide.data.판정됨
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.보고떠값
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.글꼴
import com.slayde.hasenheide.ui.theme.높이
import com.slayde.hasenheide.ui.theme.막대치수
import com.slayde.hasenheide.ui.theme.모서리
import com.slayde.hasenheide.ui.theme.색표
import com.slayde.hasenheide.ui.theme.선굵기
import com.slayde.hasenheide.ui.theme.움직임
import com.slayde.hasenheide.ui.theme.크기
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 스탯 · 업적 화면 (10-05 옮기기 2단계 PF · 시안 v21 `스탯화면` · `스탯판` · `스탯그래프` · `업적판` · `업적줄`)
 *
 *  · 맨 위 줄 없음 (시안 rm2x). [스탯][업적] 은 아래 한 줄 — 반씩 나눠 엄지가 덜 움직이게
 *  · 스탯: 체력 큰 칸 + 나머지 2열 칸. 누르면 아래에 향상 그래프 [일][주][달][년] (시안 wuyc)
 *  · 업적: 띠 '업적 n/m' (숨은 업적은 달성했을 때만 m 에 셈 · 시안 jdyt) · [달성][전체][+] · 달성 → 못 한 것
 *    잠긴 칭호는 흐리게 가려져 있다가 누르면 보인다 (v12) · 달성한 줄 오른쪽 '프로필' 체크 (v18 E ③)
 *
 * [탭줄위] = 앱 탭줄 위(화면 칸 안)에 그려질 때 — 시안처럼 탭을 누르면 닫히므로 ✕ 가 없고 시스템 막대 자리도 비우지 않는다.
 * false(지금 App.kt 의 덮개) 면 아래 줄 오른쪽에 ✕ 를 둔다. 뒤로가기는 어느 쪽이든 닫는다
 */
@Composable
fun 스탯화면(
    상태: 앱상태, 처음: String = 스탯화면글.스탯, 볼업적: String? = null, 탭줄위: Boolean = false,
    /** 10-07 홍겸 님: 운동 보고서 › 로 들어왔을 때 — 스테이터스 ‹(보고서로) ›(도전 과제) · 도전 과제 ‹(스테이터스로) */
    보고길: Boolean = false,
    닫기: () -> Unit,
) {
    val c = Local색.current
    var 쪽 by remember { mutableStateOf(처음) }
    BackHandler { 닫기() }

    // 10-07 홍겸 님: 아래 [스탯][업적] 줄 없앰 · 위에 다른 화면처럼 띠 ('스테이터스' · '도전 과제' — 업적판은 자기 띠) · 아래 탭줄은 App 이 보인다
    Box(Modifier.fillMaxSize().background(c.바탕).눌림 { }) {
        Column(Modifier.fillMaxSize().then(if (탭줄위) Modifier else Modifier.statusBarsPadding().navigationBarsPadding())) {
            if (쪽 == 스탯화면글.스탯) 머리띠(스탯화면글.스테이터스)
            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (쪽 == 스탯화면글.스탯) 스탯판(상태)
                else 업적판(상태, 볼업적)
            }
        }
        if (보고길) {
            if (쪽 == 스탯화면글.스탯) {
                보고떠동그라미(true, 스탯화면글.보고서로, Modifier.align(Alignment.BottomStart).padding(start = 보고떠값.옆, bottom = 보고떠값.옆), 닫기)
                보고떠동그라미(false, 스탯화면글.도전과제, Modifier.align(Alignment.BottomEnd).padding(end = 보고떠값.옆, bottom = 보고떠값.옆)) { 쪽 = 스탯화면글.업적 }
            } else {
                보고떠동그라미(true, 스탯화면글.스테이터스, Modifier.align(Alignment.BottomStart).padding(start = 보고떠값.옆, bottom = 보고떠값.옆)) { 쪽 = 스탯화면글.스탯 }
            }
        }
    }
}

/** 이 화면의 글 — 한곳에 (시안 글 그대로) */
object 스탯화면글 {
    const val 스탯 = "스탯"
    const val 업적 = "업적"
    const val 닫기 = "닫기"
    const val 스테이터스 = "스테이터스"   // 10-07 홍겸 님: 스탯 화면 띠
    const val 도전과제 = "도전 과제"     // 10-07 홍겸 님: 업적 화면 띠
    const val 보고서로 = "운동 보고서로"
    const val 체력이유 = "힘 쪽만 · 심폐 · 코어 · 폭발력은 나중"
    const val 없음 = "현재 측정값 없음"
    const val 새로 = "새로"
    const val 그래프닫기 = "그래프 닫기"
    const val 달성 = "달성"
    const val 전체 = "전체"
    const val 더 = "+"
    const val 숨은 = "숨은"
    const val 대표 = "대표"
    const val 대표안내 = "달성한 칭호를 누르면 바뀝니다"
    const val 대표없음 = "달성한 칭호를 누르면 대표 칭호가 됩니다"
    const val 못한것 = "아직 달성하지 못한 업적"
    const val 빈목록 = "아직 없습니다"
    const val 숨은이름 = "???"
    const val 프로필 = "프로필"
    const val 프로필보이기 = "프로필에 보이기"
    const val 칭호보기 = "잠긴 칭호 보기"
    const val 판정준비 = "판정 준비 중"
    const val 새칸필요 = "새 칸 필요"
    const val 다른기능 = "다른 기능 먼저"
    const val 새칸 = "새 칸"
    const val 나중 = "나중"
    const val 대표칭호 = "대표 칭호"
    const val 대표뺌 = "대표 칭호를 뺐습니다"
    const val 한번볼륨 = "한 번 볼륨"
    const val 점수 = "점수"
    const val 일RM = "1RM"
    const val 일RM합 = "1RM 합"
}

/**
 * 이 화면의 치수 · 시간 — 시안 CSS 값 그대로. 11 지침 단계 밖의 값이 섞여 있다 (그래프 · 체크 상자) → 합칠 때 Theme.kt 로 옮길 후보
 */
private object 스탯치수2 {
    val 칸최소 = 높이.높게            // 스탯칸 min-height 44
    val 칭호표 = 높이.아주낮게         // 28
    val 보임칸 = 높이.높게             // 업적보임 폭 44
    val 체크상자 = 18.dp
    val 체크그림 = 12.dp
    val 체크모서리 = 4.dp
    val 점선 = 4.dp
    val 점선틈 = 3.dp
    val 가림흐림 = 4.dp               // 잠긴 칭호 흐림 (시안 text-shadow 8px)
    val 그림 = 18.dp                  // 11 지침 U3-6 기본 아이콘
    val 그래프높이 = 120.dp
    val 그래프낮은높이 = 100.dp
    const val 낮은폰 = 700             // 화면 높이(dp)가 이보다 작으면 그래프를 낮게
    val 그래프위 = 16.dp               // y0
    val 그래프아래 = 16.dp             // H − yB
    val 위글틈 = 5.dp
    val 아래글틈 = 2.dp
    val 막틈 = 15.dp                   // 막대는 같은 칸 점보다 이만큼 아래에서 잘린다
    val 막최소 = 4.dp
    val 막최대 = 16.dp
    val 점 = 3.dp
    val 끝점 = 4.dp
    val 선 = 2.dp
    val 끝글위 = 8.dp
    val 끝글아래 = 16.dp
    val 끝글여유 = 12.dp
    val 끝글오른 = 24.dp
    val 빈글 = 4.dp
    val 범선폭 = 16.dp
    val 범높이 = 8.dp
    const val 자람 = 945               // 스탯 막대 '더' 칸이 자라는 시간 (.7s × 올림배수 1.35)
    const val 자람늦춤 = 200
}

}

// ═══════════════════════════ 스탯 ═══════════════════════════

/** 시안 `스탯판` — 체력 큰 칸 · 나머지 2열. 고른 칸이 있으면 아래에 그래프 (스크롤 밖에 붙박이) */
@Composable
private fun 스탯판(상태: 앱상태) {
    val d = 상태.d
    val 오늘 = 상태.오늘
    val 들 = remember(d, 오늘) { d.스탯들(오늘) }
    val 전 = remember(d, 오늘) { d.스탯전(오늘) }
    var 고름 by remember { mutableStateOf<스탯?>(null) }
    var 단위 by remember { mutableStateOf(스탯그래프표.단위들.first()) }
    val 끌어올림 = remember { HashMap<스탯, BringIntoViewRequester>() }
    // 그래프가 열리며 줄어든 칸 안에서 누른 칸이 가려지지 않게 — 다 그린 다음 한 번만 (U5-6)
    LaunchedEffect(고름) { val s = 고름 ?: return@LaunchedEffect; withFrameNanos { }; withFrameNanos { }; 끌어올림[s]?.bringIntoView() }

    Column(Modifier.fillMaxSize()) {
        당겨새로고침({ 상태.날짜확인() }, Modifier.weight(1f).fillMaxWidth()) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(start = 간격.보통, end = 간격.보통, top = 간격.좁게, bottom = 간격.아주좁게),
                verticalArrangement = Arrangement.spacedBy(간격.좁게),
            ) {
                val 체 = 들.first { it.스탯 == 스탯.체력 }
                val r = 끌어올림.getOrPut(스탯.체력) { BringIntoViewRequester() }
                체력칸(체, 전[스탯.체력.name], 전.isNotEmpty(), 고름 == 스탯.체력, Modifier.bringIntoViewRequester(r)) {
                    고름 = if (고름 == 스탯.체력) null else 스탯.체력
                }
                // 2열 — 줄마다 두 칸 (시안 grid 1fr 1fr · 사이 4)
                Column(verticalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                    들.filter { it.스탯 != 스탯.체력 }.chunked(2).forEach { 줄 ->
                        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                            줄.forEach { s ->
                                val q = 끌어올림.getOrPut(s.스탯) { BringIntoViewRequester() }
                                스탯칸(s, 전[s.스탯.name], 전.isNotEmpty(), 고름 == s.스탯, Modifier.weight(1f).fillMaxHeight().bringIntoViewRequester(q)) {
                                    고름 = if (고름 == s.스탯) null else s.스탯
                                }
                            }
                            if (줄.size == 1) Box(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        고름?.let { s -> 스탯그래프(d, s, 단위, 오늘, { 단위 = it }) { 고름 = null } }
    }
}

/** 시안 `.체력칸` — 테 2 강조 · 모서리 16 · 여백 8/12. 18 굵게 '체력' · 22 굵게 값(0 부터 올라감) · ▲ · 막대 · 이유 한 줄 */
@Composable
private fun 체력칸(s: 스탯값, 앞: Double?, 전있음: Boolean, 고름: Boolean, modifier: Modifier, on누름: () -> Unit) {
    val c = Local색.current
    val 모양 = RoundedCornerShape(모서리.보통)
    val v = s.값
    Column(
        modifier.fillMaxWidth().clip(모양).background(if (고름) c.강조옅음 else c.면).border(선굵기.굵게, c.강조, 모양)
            .눌림(on누름).semantics { stateDescription = if (고름) "그래프 열림" else "" }
            .padding(horizontal = 간격.보통, vertical = 간격.좁게),
        verticalArrangement = Arrangement.spacedBy(간격.아주좁게),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            글(s.스탯.이름, Modifier.weight(1f), 크기값 = 크기.크게, 굵기 = FontWeight.Bold)
            if (v != null) {
                val 보일 = 움직수(v, 영부터 = true, 열쇠 = s.스탯)
                글("${보일.roundToInt()}", 크기값 = 크기.큰숫자, 굵기 = FontWeight.Bold)
                스탯차글(v, 앞, 전있음)?.let { (t, 오름) -> 글(t, Modifier.padding(start = 간격.좁게), 크기값 = 크기.작게, 색 = if (오름) c.오름 else c.내림, 굵기 = FontWeight.Bold) }
            }
        }
        if (v == null) 빈게이지() else 스탯막대(v, 앞, Modifier.fillMaxWidth())
        맞춤글(스탯화면글.체력이유, 최대 = 크기.작게, 색 = c.옅음)
    }
}

/** 시안 `.스탯칸` — 테 1 선(고르면 강조) · 모서리 8 · 여백 4/8 · 높이 44 이상. 13 이름 · 15 굵게 값 / 막대 + ▲ */
@Composable
private fun 스탯칸(s: 스탯값, 앞: Double?, 전있음: Boolean, 고름: Boolean, modifier: Modifier, on누름: () -> Unit) {
    val c = Local색.current
    val 모양 = RoundedCornerShape(모서리.작게)
    val v = s.값
    Column(
        modifier.heightIn(min = 스탯치수2.칸최소).clip(모양).background(if (고름) c.강조옅음 else c.면)
            .border(선굵기.보통, if (고름) c.강조 else c.선, 모양)
            .눌림(on누름).semantics { stateDescription = if (고름) "그래프 열림" else "" }
            .padding(horizontal = 간격.좁게, vertical = 간격.아주좁게),
        verticalArrangement = Arrangement.spacedBy(간격.아주좁게, Alignment.CenterVertically),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(s.스탯.이름, Modifier.weight(1f), style = 글꼴.보통(크기.조금작게, FontWeight.Medium), color = c.글, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (v != null) 글("${v.roundToInt()}", Modifier.padding(start = 간격.아주좁게), 크기값 = 크기.본문, 굵기 = FontWeight.Bold)
        }
        if (v == null) 빈게이지()
        else Row(verticalAlignment = Alignment.CenterVertically) {
            스탯막대(v, 앞, Modifier.weight(1f))
            스탯차글(v, 앞, 전있음)?.let { (t, 오름) -> 글(t, Modifier.padding(start = 간격.아주좁게), 크기값 = 크기.작게, 색 = if (오름) c.오름 else c.내림, 굵기 = FontWeight.Bold) }
        }
    }
}

/** 시안 `빈게이지` — 빈 막대 + '현재 측정값 없음' */
@Composable
private fun 빈게이지() {
    val c = Local색.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).height(막대치수.높이).clip(CircleShape).background(c.면2))
        글(스탯화면글.없음, Modifier.padding(start = 간격.아주좁게), 크기값 = 크기.작게, 색 = c.옅음)
    }
}

/**
 * 시안 `스탯막대(v, b)` — 0 ~ 100. 7일 전 값까지 강조, 그 위로 오른 만큼은 오름 색(화면에 들어올 때 자란다).
 * 진행막대(U3-8)와 같은 높이 · 바탕 · 모양 — 두 겹이 필요해서 여기서 그린다
 */
@Composable
private fun 스탯막대(v: Double, b: Double?, modifier: Modifier) {
    val c = Local색.current
    val (밑, 더) = 스탯막대두칸(v, b)
    val 자람 = remember { Animatable(0f) }
    LaunchedEffect(Unit) { 자람.animateTo(1f, tween(스탯치수2.자람, delayMillis = 스탯치수2.자람늦춤, easing = 움직임.부드럽게)) }
    Box(
        modifier.height(막대치수.높이).clip(CircleShape).background(c.면2).drawBehind {
            drawRect(c.강조, size = Size(size.width * 밑, size.height))
            if (더 > 0.0005f) drawRect(c.오름, topLeft = Offset(size.width * 밑, 0f), size = Size(size.width * 더 * 자람.value, size.height))
        },
    )
}

/** 막대 두 칸의 비율 (0 ~ 1) — 밑 = min(7일 전, 지금) · 더 = 지금 − 밑 (시안 `스탯막대`) */
internal fun 스탯막대두칸(v: Double, b: Double?): Pair<Float, Float> {
    val 밑 = if (b == null) 0.0 else min(b, v).coerceAtLeast(0.0)
    val 더 = max(0.0, v - 밑)
    return (밑 / 스탯표.최대).coerceIn(0.0, 1.0).toFloat() to (더 / 스탯표.최대).coerceIn(0.0, 1.0 - (밑 / 스탯표.최대).coerceIn(0.0, 1.0)).toFloat()
}

// ─────────────── 스탯 향상 그래프 (시안 wuyc `스탯그래프표` · `스탯그래프칸` · `그래프값` · `스탯그래프`) ───────────────

/**
 * 축은 자동이 아니라 고정: 맨 위 = '3대 합 500kg 정도' 수행능력 (그 스탯에 쓰는 종목의 그 수준 1RM 합).
 * 막대 맨 위 = 그 수준 사람의 한 번 볼륨(1RM × 24 = 75% × 4세트 × 8회). 1RM 과 무관한 스탯은 선 = 점수(0~100), 막대 맨 위 10,000kg
 */
internal object 스탯그래프표 {
    val 삼대500: Map<String, Double> = mapOf("벤치프레스" to 120.0, "백 스쿼트" to 170.0, "데드리프트" to 210.0, "오버헤드 프레스" to 75.0, "펜들레이 로우" to 110.0)
    val 종목: Map<스탯, List<String>> = mapOf(
        스탯.근력 to listOf("벤치프레스", "백 스쿼트", "데드리프트"),
        스탯.수행능력 to listOf("벤치프레스", "백 스쿼트", "데드리프트"),
        스탯.밀기 to listOf("벤치프레스", "오버헤드 프레스"),
        스탯.당기기 to listOf("펜들레이 로우"),
        스탯.하체 to listOf("백 스쿼트", "데드리프트"),
    )
    const val 볼륨배 = 24.0
    const val 전체볼륨 = 10_000.0
    const val 점수위 = 100.0
    val 칸수: Map<String, Int> = mapOf("일" to 14, "주" to 8, "달" to 6, "년" to 5)
    val 단위들 = listOf("일", "주", "달", "년")
    /** 1RM 을 볼 때 칸의 마지막 운동 날부터 며칠 앞까지 (스탯과 같은 셈 · 시안 스탯표.최근RM일) */
    const val 최근RM일 = 90L

    /** 선 맨 위 (kg 또는 점수) */
    fun 선위(s: 스탯): Double = 종목[s]?.sumOf { 삼대500[it] ?: 0.0 } ?: 점수위
    /** 막대 맨 위 (kg) — 1RM 스탯은 선위 × 24 */
    fun 막위(s: 스탯): Double = if (종목[s] != null) 선위(s) * 볼륨배 else 전체볼륨
}

/** 그래프 한 칸 — 시(첫날) ~ 끝(끝날) · 아래 글 · 선 값(없으면 null) · 막대 값(없으면 null) */
internal data class 스탯그래프칸(val 시: String, val 끝: String, val 글: String, val 선: Double? = null, val 막: Double? = null)

/** 묶음 칸 — 오늘이 든 칸이 맨 오른쪽. 주 = 월~일 (시안 `스탯그래프칸`) */
internal fun 스탯그래프칸들(단위: String, 오늘: String): List<스탯그래프칸> {
    val o = 날(오늘)
    val n = 스탯그래프표.칸수[단위] ?: 14
    fun 월일(x: LocalDate) = "${x.monthValue}/${x.dayOfMonth}"
    return (n - 1 downTo 0).map { i ->
        when (단위) {
            "주" -> { val w = o.minusDays(((o.dayOfWeek.value + 6) % 7).toLong()).minusWeeks(i.toLong()); 스탯그래프칸(w.toString(), w.plusDays(6).toString(), 월일(w)) }
            "달" -> { val x = o.withDayOfMonth(1).minusMonths(i.toLong()); 스탯그래프칸(x.toString(), x.withDayOfMonth(x.lengthOfMonth()).toString(), "${x.monthValue}월") }
            "년" -> { val y = o.year - i; 스탯그래프칸("$y-01-01", "$y-12-31", "$y") }
            else -> { val x = o.minusDays(i.toLong()); 스탯그래프칸(x.toString(), x.toString(), 월일(x)) }
        }
    }
}

/**
 * 칸마다 (시안 `그래프값`) —
 *  · 선: 1RM 스탯은 칸의 마지막 운동 날 기준 최근 90일 최고 1RM 합(스탯과 같은 셈), 점수 스탯은 칸의 마지막 스탯기록.
 *  · 막대: 그 칸 운동들의 한 번 평균 볼륨(워밍업 뺀 세트 · 맨몸은 유효무게). 기록이 없는 칸은 비운다
 */
internal fun 앱데이터.스탯그래프값(s: 스탯, 단위: String, 오늘: String): List<스탯그래프칸> {
    val 종 = 스탯그래프표.종목[s]
    val 줄 = 기록.entries.filter { 날짜만(it.key) <= 오늘 }.sortedBy { it.key }
    fun 든다(e: 종목기록) = 종 == null || 정식이름(e) in 종
    fun 함(r: 날기록) = r.종목들.any { 든다(it) && 기록세트(it.세트들).isNotEmpty() }
    fun 볼(r: 날기록) = r.종목들.filter { 든다(it) }.sumOf { e -> 기록세트(e.세트들).sumOf { 근육계산.유효무게(it.w, 몸.체중) * it.r } }
    return 스탯그래프칸들(단위, 오늘).map { c ->
        val 속 = 줄.filter { val d = 날짜만(it.key); d >= c.시 && d <= c.끝 && 함(it.value) }
        val 막 = if (속.isEmpty()) null else 속.sumOf { 볼(it.value) } / 속.size
        val 선: Double? = if (종 != null) {
            if (속.isEmpty()) null else {
                val 기 = 날짜만(속.last().key)
                val 창 = 줄.filter { val d = 날짜만(it.key); d <= 기 && ChronoUnit.DAYS.between(날(d), 날(기)) < 스탯그래프표.최근RM일 }
                val 최고 = HashMap<String, Double>()
                창.forEach { (_, r) -> 날RM들(r).forEach { (t, v) -> if (v > (최고[t] ?: 0.0)) 최고[t] = v } }
                종.sumOf { 최고[it] ?: 0.0 }.takeIf { it > 0 }
            }
        } else {
            스탯기록.keys.filter { it >= c.시 && it <= c.끝 }.sorted().asReversed().firstNotNullOfOrNull { 스탯기록[it]?.get(s.name) }
        }
        c.copy(선 = 선, 막 = 막)
    }
}

/** 시안 `스탯차` — 7일 전 대비. (글, 오름?) · 0.05 안쪽이면 null · 7일 전 값이 없으면 기록이 있을 때만 '새로' */
internal fun 스탯차글(v: Double?, b: Double?, 전있음: Boolean): Pair<String, Boolean>? {
    if (v == null) return null
    if (b == null) return if (전있음) 스탯화면글.새로 to true else null
    val d = v - b
    if (abs(d) < 0.05) return null
    val x = if (abs(d) >= 1) "${abs(d).roundToInt()}" else String.format(java.util.Locale.ROOT, "%.1f", abs(d))
    return (if (d > 0) "▲ +$x" else "▼ −$x") to (d > 0)
}

/** 시안 `그래프차` — 마지막 두 값의 차. 0.5 안쪽이면 null */
internal fun 스탯그래프차글(l: List<Double>, 글로: (Double) -> String): Pair<String, Boolean>? {
    if (l.size < 2) return null
    val d = l[l.size - 1] - l[l.size - 2]
    if (abs(d) < 0.5) return null
    return (if (d > 0) "▲ +" else "▼ −") + 글로(abs(d)) to (d > 0)
}

/** 시안 `스탯그래프` — 칩줄 위에 붙박이(위 칸만 스크롤) · 선 = 강조, 막대 = 속선 */
@Composable
private fun 스탯그래프(d: 앱데이터, s: 스탯, 단위: String, 오늘: String, 단위고름: (String) -> Unit, 닫기: () -> Unit) {
    val c = Local색.current
    val 값들 = remember(d, s, 단위, 오늘) { d.스탯그래프값(s, 단위, 오늘) }
    val 종 = 스탯그래프표.종목[s]
    val 선위 = 스탯그래프표.선위(s)
    val 막위 = 스탯그래프표.막위(s)
    val H = if (LocalConfiguration.current.screenHeightDp < 스탯치수2.낮은폰) 스탯치수2.그래프낮은높이 else 스탯치수2.그래프높이
    val 측정 = rememberTextMeasurer()
    val 빈 = 값들.none { it.선 != null }
    Column(
        Modifier.fillMaxWidth().background(c.면).drawBehind { drawRect(c.선, size = Size(size.width, 선굵기.보통.toPx())) }
            .padding(horizontal = 간격.보통, vertical = 간격.좁게),
        verticalArrangement = Arrangement.spacedBy(간격.좁게),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(s.이름, Modifier.weight(1f), style = 글꼴.보통(크기.본문, FontWeight.Bold), color = c.글, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(Modifier.width(IntrinsicSize.Max)) { 칩줄(스탯그래프표.단위들, 단위, 단위고름) }
            Box(Modifier.width(간격.좁게))
            Box(
                Modifier.size(높이.낮게).clip(RoundedCornerShape(모서리.작게)).border(선굵기.보통, c.속선, RoundedCornerShape(모서리.작게))
                    .눌림(닫기).semantics { contentDescription = 스탯화면글.그래프닫기; role = Role.Button },
                contentAlignment = Alignment.Center,
            ) { Icon(아이콘.닫기, null, Modifier.size(스탯치수2.그림), tint = c.글) }
        }
        val 위글 = if (종 != null) "3대 500 수준 · ${선위.roundToInt()}kg" else "${스탯화면글.점수} ${선위.roundToInt()}"
        val 오른글 = "볼륨 ${콤마(막위)}kg"
        val 글꼴11 = 글꼴.보통(크기.작게)
        Canvas(Modifier.fillMaxWidth().height(H).semantics { contentDescription = "${s.이름} ${단위} 단위 그래프" }) {
            그래프그림(값들, 선위, 막위, 위글, 오른글, 빈, 측정, 글꼴11, c)
        }
        if (!빈) {
            val 선들 = 값들.mapNotNull { it.선 }
            val 막들 = 값들.mapNotNull { it.막 }.filter { it > 0 }
            val 선글: (Double) -> String = { if (종 != null) 무게글(무게반올림(it)) + "kg" else "${it.roundToInt()}" }
            val 차글: (Double) -> String = { if (종 != null) 무게글(무게반올림(it)) else "${it.roundToInt()}" }
            Row(verticalAlignment = Alignment.CenterVertically) {
                범례선()
                Text(
                    buildAnnotatedString {
                        append(" ${if (종 != null) (if (종.size > 1) 스탯화면글.일RM합 else 스탯화면글.일RM) else 스탯화면글.점수} ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = c.글)) { append(선글(선들.last())) }
                        스탯그래프차글(선들, 차글)?.let { (t, 오름) -> withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = if (오름) c.오름 else c.내림)) { append(" $t") } }
                        append(" · ")
                    },
                    style = 글꼴11, color = c.흐림, maxLines = 1, softWrap = false,
                )
                범례막()
                Text(
                    buildAnnotatedString {
                        append(" ${스탯화면글.한번볼륨} ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = c.글)) { append(if (막들.isNotEmpty()) 콤마(막들.last()) + "kg" else "—") }
                        스탯그래프차글(막들) { 콤마(it) }?.let { (t, 오름) -> withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = if (오름) c.오름 else c.내림)) { append(" $t") } }
                    },
                    Modifier.weight(1f), style = 글꼴11, color = c.흐림, maxLines = 1, softWrap = false, overflow = TextOverflow.Clip,
                )
            }
        }
    }
}

@Composable
private fun 범례선() {
    val c = Local색.current
    Canvas(Modifier.size(스탯치수2.범선폭, 스탯치수2.범높이)) {
        drawLine(c.강조, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 스탯치수2.선.toPx(), StrokeCap.Round)
        drawCircle(c.강조, 스탯치수2.점.toPx(), Offset(size.width / 2, size.height / 2))
    }
}

@Composable
private fun 범례막() {
    val c = Local색.current
    Canvas(Modifier.size(스탯치수2.범높이)) { drawRect(c.속선) }
}

/** 시안 `스탯그래프` svg 를 그대로 — 기준선(점선) · 바닥 · 위 글 둘 · 막대 · 선 · 점 · 끝 값 · 아래 날짜 */
private fun DrawScope.그래프그림(
    값들: List<스탯그래프칸>, 선위: Double, 막위: Double, 위글: String, 오른글: String, 빈: Boolean,
    측정: TextMeasurer, 글꼴11: TextStyle, c: 색표,
) {
    val W = size.width; val H = size.height
    val y0 = 스탯치수2.그래프위.toPx(); val yB = H - 스탯치수2.그래프아래.toPx(); val pH = yB - y0
    val n = 값들.size.coerceAtLeast(1); val sw = W / n
    fun X(i: Int) = sw * (i + 0.5f)
    fun Y(v: Double) = yB - pH * (v / 선위).coerceIn(0.0, 1.0).toFloat()
    val 틈 = 스탯치수2.막틈.toPx()
    val 막폭 = (sw * 0.5f).coerceIn(스탯치수2.막최소.toPx(), 스탯치수2.막최대.toPx())
    val 걸음 = ceil(n / 5.0).toInt().coerceAtLeast(1)
    // SVG text 의 y 는 글자 바닥선 — 잰 글의 첫 바닥선만큼 올려 놓는다. 정렬 0 = 왼 · 1 = 가운데 · 2 = 오른
    fun 글(t: String, x: Float, 바닥: Float, 정렬: Int, 색: Color, 굵게: Boolean = false) {
        val 꼴 = if (굵게) 글꼴11.copy(fontWeight = FontWeight.Bold) else 글꼴11
        val r = 측정.measure(AnnotatedString(t), 꼴)
        val left = when (정렬) { 0 -> x; 1 -> x - r.size.width / 2f; else -> x - r.size.width }
        drawText(r, 색, Offset(left, 바닥 - r.firstBaseline))
    }
    val 점선 = PathEffect.dashPathEffect(floatArrayOf(스탯치수2.점선.toPx(), 스탯치수2.점선.toPx()))
    val 선굵 = 선굵기.보통.toPx()
    drawLine(c.선, Offset(0f, y0), Offset(W, y0), 선굵, pathEffect = 점선)
    drawLine(c.선, Offset(0f, yB), Offset(W, yB), 선굵)
    글(위글, 0f, y0 - 스탯치수2.위글틈.toPx(), 0, c.옅음)
    글(오른글, W, y0 - 스탯치수2.위글틈.toPx(), 2, c.옅음)
    val 점들 = ArrayList<Triple<Float, Float, Double>>()
    값들.forEachIndexed { i, k ->
        val 선값 = k.선
        val py = 선값?.let { Y(it) }
        val 막 = k.막
        if (!빈 && 막 != null && 막 > 0) {
            var 위 = yB - (pH - 틈) * (막 / 막위).coerceIn(0.0, 1.0).toFloat()
            if (py != null) 위 = max(위, py + 틈)   // 점보다 15 아래까지만
            if (yB - 위 > 0.5f) drawRect(c.속선, Offset(X(i) - 막폭 / 2, 위), Size(막폭, yB - 위))
        }
        if (py != null && 선값 != null) 점들 += Triple(X(i), py, 선값)
        if ((n - 1 - i) % 걸음 == 0) 글(k.글, X(i), H - 스탯치수2.아래글틈.toPx(), 1, c.옅음)
    }
    if (점들.size > 1) {
        val p = Path().apply { 점들.forEachIndexed { j, (x, y) -> if (j == 0) moveTo(x, y) else lineTo(x, y) } }
        drawPath(p, c.강조, style = Stroke(스탯치수2.선.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
    점들.forEachIndexed { j, (x, y) ->
        if (j == 점들.size - 1) {
            drawCircle(c.면, 스탯치수2.끝점.toPx(), Offset(x, y))
            drawCircle(c.강조, 스탯치수2.끝점.toPx(), Offset(x, y), style = Stroke(스탯치수2.선.toPx()))
        } else drawCircle(c.강조, 스탯치수2.점.toPx(), Offset(x, y))
    }
    점들.lastOrNull()?.let { (x, y, v) ->
        // 맨 위에 붙은 점은 값 글을 아래에
        val 끝y = if (y - 스탯치수2.끝글위.toPx() < y0 + 스탯치수2.끝글여유.toPx()) y + 스탯치수2.끝글아래.toPx() else y - 스탯치수2.끝글위.toPx()
        글("${v.roundToInt()}", x, 끝y, if (x > W - 스탯치수2.끝글오른.toPx()) 2 else 1, c.강조, 굵게 = true)
    }
    if (빈) 글(스탯화면글.없음, W / 2, (y0 + yB) / 2 + 스탯치수2.빈글.toPx(), 1, c.옅음)
}

// ═══════════════════════════ 업적 ═══════════════════════════

/** 시안 `업적판` 분모 — 숨은 업적은 달성해야 전체 수에 들어간다 (100개 · 숨은 10개 · 1개 달성 = 1/100, 숨은 것 달성 = 2/101) */
internal fun 업적분모(d: 앱데이터, 표: List<업적> = 업적표.목록): Int = 표.count { !it.숨김 } + 표.count { it.숨김 && it.번호 in d.업적 }

/** 달성한 업적 수 (표에 있는 것만) */
internal fun 업적풀린수(d: 앱데이터, 표: List<업적> = 업적표.목록): Int = 표.count { it.번호 in d.업적 }

/** 달성한 업적 — 새것부터 (같은 때면 표에서 뒤의 것부터 · 시안 `얻[b].순 - 얻[a].순`) */
internal fun 달성차례(d: 앱데이터, 표: List<업적> = 업적표.목록): List<업적> =
    표.withIndex().filter { it.value.번호 in d.업적 }
        .sortedWith(compareByDescending<IndexedValue<업적>> { d.업적[it.value.번호] ?: 0L }.thenByDescending { it.index })
        .map { it.value }

/** 분류 칩 — '숨은' + 표 차례대로 분류 (시안 `분류들`) */
internal fun 업적분류들(표: List<업적> = 업적표.목록): List<String> = listOf(스탯화면글.숨은) + 표.map { it.분류 }.distinct()

/** 분류 칩 글 — 괄호 덧말을 뗀다 (시안 `짧은분류`) */
internal fun 업적짧은분류(v: String): String = v.replace(Regex(" \\(.*\\)$"), "")

/** 고른 분류의 목록 (달성 = 새것부터 · 전체 = 표 · 숨은 · 분류) */
internal fun 업적거름(d: 앱데이터, 분류: String, 표: List<업적> = 업적표.목록): List<업적> = when (분류) {
    스탯화면글.달성 -> 달성차례(d, 표)
    스탯화면글.전체 -> 표
    스탯화면글.숨은 -> 표.filter { it.숨김 }
    else -> 표.filter { it.분류 == 분류 }
}

/** '달성' 쪽 목록 — 달성한 것(새것부터) · 이름표 · 못 한 것(표 차례). null = 이름표 자리 */
internal fun 달성쪽(d: 앱데이터, 표: List<업적> = 업적표.목록): List<업적?> = 달성차례(d, 표) + listOf(null) + 표.filter { it.번호 !in d.업적 }

/** 시안 `날글` — "10월 5일" */
private fun 업적날글(ms: Long): String = 시각날(ms).let { "${it.substring(5, 7).toInt()}월 ${it.substring(8, 10).toInt()}일" }

/** 등급 → 테두리 · 글 색 (시안 `.등급-*` · 숨은 = 히든 색) */
internal fun 업적등급색(a: 업적, c: 색표): Color = if (a.숨김) c.히든 else when (a.등급) {
    "초보" -> c.초보; "중급" -> c.중급; "고급" -> c.고급; "미친자" -> c.미친자; "유머" -> c.유머
    else -> c.히든
}

/** 시안 `업적판` — 띠 '업적 n/m' · 대표 안내 · [달성][전체][+] (+ 를 누르면 분류 줄) · 목록 */
@Composable
private fun 업적판(상태: 앱상태, 볼업적: String?) {
    val c = Local색.current
    val d = 상태.d
    var 분류 by remember { mutableStateOf(스탯화면글.달성) }
    var 더 by remember { mutableStateOf(false) }
    var 칭호보기 by remember { mutableStateOf<String?>(null) }
    val 진행 = remember(d, 상태.오늘) { d.업적진행들(상태.오늘) }
    val 분류들 = remember { 업적분류들() }
    val 갈래 = 분류 != 스탯화면글.달성 && 분류 != 스탯화면글.전체
    val 대 = d.대표칭호?.let { 업적표.칭호찾기(it) }

    Column(Modifier.fillMaxSize()) {
        // 시안 `.띠` (가운데띠 아님) — 왼쪽 '업적' · 오른쪽 n/m
        // 10-07 홍겸 님: 다른 화면처럼 가운데 띠 '도전 과제' · 오른쪽 n/m
        머리띠(스탯화면글.도전과제, 오른쪽 = { 띠글("${업적풀린수(d)}/${업적분모(d)}") })
        // 시안 `.업적위` — 바탕 면 · 아래 테 1 선 · 여백 8/12 · 사이 8
        Column(
            Modifier.fillMaxWidth().background(c.면).drawBehind { drawRect(c.선, Offset(0f, size.height - 선굵기.보통.toPx()), Size(size.width, 선굵기.보통.toPx())) }
                .padding(horizontal = 간격.보통, vertical = 간격.좁게),
            verticalArrangement = Arrangement.spacedBy(간격.좁게),
        ) {
            맞춤글(if (대 != null) "${스탯화면글.대표} 『${대.칭호}』 · ${스탯화면글.대표안내}" else 스탯화면글.대표없음, 최대 = 크기.작게, 색 = c.옅음)
            칩줄(listOf(스탯화면글.달성, 스탯화면글.전체, 스탯화면글.더), if (갈래) 스탯화면글.더 else 분류, { v ->
                if (v == 스탯화면글.더) { 더 = !더 } else { 분류 = v }
            })
            if (더) {
                val 짧은 = remember(분류들) { 분류들.map { 업적짧은분류(it) } }
                화살칩줄(짧은, if (갈래) 업적짧은분류(분류) else null, { v -> 분류들.getOrNull(짧은.indexOf(v))?.let { 분류 = it } })
            }
        }
        val 목록: List<업적?> = remember(d, 분류) { if (분류 == 스탯화면글.달성) 달성쪽(d) else 업적거름(d, 분류) }
        val 줄자리 = rememberLazyListState()
        // 분류를 바꾸면 맨 위부터. 알림 띠 '보기' 로 왔으면 그 줄까지 (처음 한 번만)
        var 처음내림 by remember { mutableStateOf(볼업적 != null) }
        LaunchedEffect(분류) {
            val b = 볼업적
            if (처음내림 && b != null) { 처음내림 = false; 목록.indexOfFirst { it?.번호 == b }.takeIf { it >= 0 }?.let { 줄자리.scrollToItem(it) } }
            else 줄자리.scrollToItem(0)
        }
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val 칭호최대 = (maxWidth - 간격.보통 * 2 - 스탯치수2.보임칸) * 0.58f
            당겨새로고침({ 상태.날짜확인() }, Modifier.fillMaxSize()) {
                if (목록.isEmpty()) {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(간격.보통)) { 빈칸글(스탯화면글.빈목록) }
                } else LazyColumn(Modifier.fillMaxSize(), state = 줄자리, contentPadding = PaddingValues(start = 간격.보통, end = 간격.보통, bottom = 간격.보통)) {
                    items(목록, key = { it?.번호 ?: "·이름표" }) { a ->
                        if (a == null) 이름표(스탯화면글.못한것, Modifier.padding(top = 간격.보통, bottom = 간격.아주좁게))
                        else 업적줄(a, d, 진행[a.번호], 칭호최대, 칭호보기 == a.번호,
                            on칭호보기 = { 칭호보기 = if (칭호보기 == a.번호) null else a.번호 },
                            on누름 = {
                                val 전 = 상태.d.대표칭호
                                상태.바꿈 { it.대표칭호고름(a.칭호번호) }
                                val 뗌 = 전 == a.칭호번호
                                발자취.적기(if (뗌) "대표 칭호 뺌" else "대표 칭호 『${a.칭호}』")
                                상태.알림.토스트(if (뗌) 스탯화면글.대표뺌 else "${스탯화면글.대표칭호} 『${a.칭호}』")
                            },
                            on보임 = { 상태.바꿈 { it.업적보임바꿈(a.번호) } },
                        )
                    }
                }
            }
        }
    }
}

/** 시안 `.빈칸` — 점선 테 · 모서리 8 · 가운데 13 옅음 */
@Composable
private fun 빈칸글(t: String, modifier: Modifier = Modifier) {
    val c = Local색.current
    Box(modifier.fillMaxWidth().점선둘레(c.속선).padding(간격.보통), contentAlignment = Alignment.Center) {
        글(t, 크기값 = 크기.조금작게, 색 = c.옅음, 가운데 = true)
    }
}

/** 점선 테두리 1 (모서리 8) — 시안 `border:1px dashed` */
internal fun Modifier.점선둘레(색: Color, 모서리값: Dp = 모서리.작게): Modifier = this.drawBehind {
    val w = 선굵기.보통.toPx()
    val r = 모서리값.toPx()
    drawRoundRect(
        색, topLeft = Offset(w / 2, w / 2), size = Size(size.width - w, size.height - w), cornerRadius = CornerRadius(r, r),
        style = Stroke(w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(스탯치수2.점선.toPx(), 스탯치수2.점선틈.toPx()))),
    )
}

/**
 * 시안 `업적줄` —
 *  · 달성: [칭호표(등급 색 테 2)] 번호 이름 [대표 | 등급] / 문구 / '10월 5일 달성 · 조건' / 플레이버 — 누르면 대표 칭호. 오른쪽 '프로필' 체크
 *  · 숨은 잠김: [???] 번호 ??? (나중 · 새 칸) 숨은
 *  · 잠김: [가려진 칭호 — 누르면 보임] 번호 이름 등급 / 조건 / (판정 준비 중 | 진행 막대)
 */
@Composable
private fun 업적줄(
    a: 업적, d: 앱데이터, 진행: Pair<Double, Double>?, 칭호최대: Dp, 보임: Boolean,
    on칭호보기: () -> Unit, on누름: () -> Unit, on보임: () -> Unit,
) {
    val c = Local색.current
    val 얻 = d.업적[a.번호]
    val 대 = d.대표칭호 == a.칭호번호
    val 등 = if (a.숨김) 스탯화면글.숨은 else a.등급
    val 숨김잠김 = a.숨김 && 얻 == null
    val 등색 = 업적등급색(a, c)
    val 아래선 = Modifier.drawBehind { drawRect(c.선, Offset(0f, size.height - 선굵기.보통.toPx()), Size(size.width, 선굵기.보통.toPx())) }

    val 머리: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
            칭호표(a, 얻 != null, 숨김잠김, 보임, 등색, 칭호최대, on칭호보기)
            글(a.번호, 크기값 = 크기.작게, 색 = c.옅음)
            Text(if (숨김잠김) 스탯화면글.숨은이름 else a.이름, Modifier.weight(1f), style = 글꼴.보통(크기.조금작게), color = c.흐림, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (숨김잠김 && !a.판정됨) 글(if (a.구현가능 == "새 칸") 스탯화면글.새칸 else 스탯화면글.나중, 크기값 = 크기.작게, 색 = c.옅음)
            if (대) 대표알약() else if (등.isNotEmpty()) 글(등, 크기값 = 크기.작게, 색 = 등색, 굵기 = FontWeight.Bold)
        }
    }

    if (얻 != null) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).then(아래선)) {
            Column(
                Modifier.weight(1f).눌림(on누름).padding(vertical = 간격.좁게),
                verticalArrangement = Arrangement.spacedBy(간격.아주좁게),
            ) {
                머리()
                if (a.문구.isNotBlank()) 글(a.문구, 크기값 = 크기.조금작게, 줄 = 2)
                글("${업적날글(얻)} 달성 · ${a.조건}", 크기값 = 크기.작게, 색 = c.흐림, 줄 = 3)
                if (a.플레이버.isNotBlank()) 글(
                    a.플레이버,
                    Modifier.drawBehind { drawRect(c.선, size = Size(선굵기.굵게.toPx(), size.height)) }.padding(start = 간격.좁게),
                    크기값 = 크기.작게, 색 = c.옅음, 줄 = 4,
                )
            }
            업적보임칸(d.업적보임(a.번호), on보임)
        }
        return
    }
    Column(Modifier.fillMaxWidth().then(아래선).padding(vertical = 간격.좁게), verticalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
        머리()
        if (숨김잠김) return@Column
        글(a.조건, 크기값 = 크기.작게, 색 = c.흐림, 줄 = 3)
        if (!a.판정됨) 글("${스탯화면글.판정준비} · ${if (a.구현가능 == "새 칸") 스탯화면글.새칸필요 else 스탯화면글.다른기능}", 크기값 = 크기.작게, 색 = c.옅음)
        else if (진행 != null && 진행.second > 0) {
            val 단위 = 업적단위(a.번호)
            val 단: (Double) -> String = { x -> if (단위 == "kg") 무게글(무게반올림(x)) else "${x.roundToInt()}" }
            Row(verticalAlignment = Alignment.CenterVertically) {
                진행막대((진행.first / 진행.second).toFloat(), Modifier.weight(1f))
                글("${단(min(진행.first, 진행.second))} / ${단(진행.second)}$단위", Modifier.padding(start = 간격.좁게), 크기값 = 크기.작게, 색 = c.흐림)
            }
        }
    }
}

/**
 * 시안 `.칭호표` — 높이 28 · 여백 0/8 · 모서리 8 · 13 굵게 · 폭은 줄의 58% 까지.
 * 달성 = 등급 색 테 2 · 숨은 잠김 = 점선 '???' · 잠김 = 점선 · 글이 흐리게 가려져 있다가 누르면 보인다 (v12)
 */
@Composable
private fun 칭호표(a: 업적, 얻음: Boolean, 숨김잠김: Boolean, 보임: Boolean, 등색: Color, 최대: Dp, on보기: () -> Unit) {
    val c = Local색.current
    val 모양 = RoundedCornerShape(모서리.작게)
    val 바깥 = Modifier.widthIn(max = 최대).height(스탯치수2.칭호표)
    val 안 = Modifier.padding(horizontal = 간격.좁게)
    when {
        얻음 -> Box(바깥.clip(모양).border(선굵기.굵게, 등색, 모양).then(안), contentAlignment = Alignment.Center) {
            Text(a.칭호, style = 글꼴.보통(크기.조금작게, FontWeight.Bold), color = c.글, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        숨김잠김 -> Box(바깥.점선둘레(c.속선).then(안), contentAlignment = Alignment.Center) {
            Text(스탯화면글.숨은이름, style = 글꼴.보통(크기.조금작게, FontWeight.Bold), color = c.옅음, maxLines = 1)
        }
        else -> Box(
            바깥.clip(모양).점선둘레(c.속선).눌림(on보기)
                .semantics { contentDescription = 스탯화면글.칭호보기; role = Role.Button; stateDescription = if (보임) a.칭호 else "" }.then(안),
            contentAlignment = Alignment.Center,
        ) {
            // 가림 — 안드로이드 12 아래는 흐림이 안 돼서 글자를 점으로 바꾼다
            val 흐림됨 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            val 글자 = if (보임 || 흐림됨) a.칭호 else 칭호가림글(a.칭호)
            Text(
                글자,
                if (!보임 && 흐림됨) Modifier.blur(스탯치수2.가림흐림, BlurredEdgeTreatment.Unbounded) else Modifier,
                style = 글꼴.보통(크기.조금작게, FontWeight.Bold), color = if (보임) c.흐림 else c.옅음, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** 흐림을 못 쓰는 폰 — 글자마다 점 (띄어쓰기는 그대로) */
internal fun 칭호가림글(t: String): String = buildString { t.codePoints().forEach { append(if (Character.isWhitespace(it)) " " else "•") } }

/** 시안 `.알약.강조` '대표' — 11 굵게 · 강조 바탕 · 강조글 · 모서리 8 */
@Composable
private fun 대표알약() {
    val c = Local색.current
    Box(Modifier.clip(RoundedCornerShape(모서리.작게)).background(c.강조).padding(horizontal = 간격.아주좁게)) {
        글(스탯화면글.대표, 크기값 = 크기.작게, 색 = c.강조글, 굵기 = FontWeight.Bold)
    }
}

/** 시안 `업적보임칸` — 폭 44 · 체크 상자 18 (켜면 강조 바탕 · 체크) · '프로필' 11 */
@Composable
private fun 업적보임칸(보: Boolean, on누름: () -> Unit) {
    val c = Local색.current
    val 모양 = RoundedCornerShape(스탯치수2.체크모서리)
    Column(
        Modifier.width(스탯치수2.보임칸).fillMaxHeight().눌림(on누름)
            .semantics { role = Role.Checkbox; contentDescription = 스탯화면글.프로필보이기; stateDescription = if (보) "켜짐" else "꺼짐" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(간격.아주좁게, Alignment.CenterVertically),
    ) {
        Box(
            Modifier.size(스탯치수2.체크상자).clip(모양).background(if (보) c.강조 else c.면).border(선굵기.굵게, if (보) c.강조 else c.속선, 모양),
            contentAlignment = Alignment.Center,
        ) { if (보) Icon(아이콘.체크, null, Modifier.size(스탯치수2.체크그림), tint = c.강조글) }
        글(스탯화면글.프로필, 크기값 = 크기.작게, 색 = if (보) c.흐림 else c.옅음)
    }
}
