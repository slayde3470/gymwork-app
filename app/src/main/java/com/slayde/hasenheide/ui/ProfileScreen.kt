@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.slayde.hasenheide.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slayde.hasenheide.data.기록세트
import com.slayde.hasenheide.data.날짜만
import com.slayde.hasenheide.data.닉네임최대
import com.slayde.hasenheide.data.링크주소
import com.slayde.hasenheide.data.무게글
import com.slayde.hasenheide.data.설정값
import com.slayde.hasenheide.data.앱데이터
import com.slayde.hasenheide.data.업적보임
import com.slayde.hasenheide.data.업적옮기기
import com.slayde.hasenheide.data.업적정렬목록
import com.slayde.hasenheide.data.업적차례
import com.slayde.hasenheide.data.업적표
import com.slayde.hasenheide.data.인증고정
import com.slayde.hasenheide.data.인증넣기
import com.slayde.hasenheide.data.인증사진
import com.slayde.hasenheide.data.인증순
import com.slayde.hasenheide.data.인증최대
import com.slayde.hasenheide.data.일RM
import com.slayde.hasenheide.data.콤마
import com.slayde.hasenheide.data.큰운동추가정리
import com.slayde.hasenheide.data.큰운동표
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.글꼴
import com.slayde.hasenheide.ui.theme.높이
import com.slayde.hasenheide.ui.theme.모서리
import com.slayde.hasenheide.ui.theme.선굵기
import com.slayde.hasenheide.ui.theme.크기
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * 프로필 탭 (10-05 옮기기 2단계 PF · 시안 v21 `프로필탭` · `최근업적줄` · `인증판` · 시트 `링크` · `링크목록` · `인증` · `보고방식` · `큰운동상세`)
 *
 * 머리 띠 없이 바로 아래 묶음 셋 — 들어올 때 묶음마다 상하좌우 중 하나에서 50 떨어져 들어온다 (v17 ①):
 *  · 인머리 — 사진 88(눌러 바꾸기) · 닉네임 칸(12자) · SNS 링크 · 큰 운동 칸(N대 파란 상자 + 칸) · 오른쪽 위 톱니(결과 보고서 표시 방법)
 *  · 업적 줄 — 달성한 업적 전부 한 줄 가로 스크롤 · 꾹 눌러 끌어 순서(정렬이 '직접' 으로) · 프로필에서 숨긴 것은 빠진다. 누르면 업적 화면
 *  · 인증판 — 인증샷 최대 8 · 3:4 · 한 줄 4칸 · 고정(최대 3)이 맨 앞 · 누르면 시트(고정 · 지우기)
 * 사진은 앱 안 파일(filesDir/photos · 사진함)로 줄여 복사하고 이름만 저장한다 (시안은 브라우저 저장)
 */
@Composable
fun 프로필화면(상태: 앱상태) {
    val c = Local색.current
    val d = 상태.d
    val ctx = LocalContext.current
    val 일꾼 = rememberCoroutineScope()
    var 시트 by remember { mutableStateOf<프로필시트?>(null) }
    var 업적열림 by remember { mutableStateOf(false) }
    var 사진넣는중 by remember { mutableStateOf(false) }
    var 인증넣는중 by remember { mutableStateOf(false) }

    // 프로필 사진 — 하나 골라 줄여서 앱 안에 복사 (옛 파일은 앱을 켤 때 사진함.정리가 치운다)
    val 사진고르기 = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) { 사진넣는중 = false; return@rememberLauncherForActivityResult }
        일꾼.launch {
            val 이름 = withContext(Dispatchers.IO) { 사진함.넣기(ctx, listOf(uri)) }.firstOrNull()
            사진넣는중 = false
            if (이름 == null) { 상태.알림.토스트(프로필글.못읽음); return@launch }
            발자취.적기("프로필 사진")
            프로필파일지킴.지킴(ctx, 이름)
            상태.d.설정.프로필사진?.let { 프로필파일지킴.놓음(ctx, it) }
            상태.바꿈 { it.copy(설정 = it.설정.copy(프로필사진 = 이름)) }
        }
    }
    // 인증샷 — 여러 장. 남은 칸만큼만 넣고, 넘치면 '8장까지'
    val 인증고르기 = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(인증최대)) { 고른것 ->
        if (고른것.isEmpty()) { 인증넣는중 = false; return@rememberLauncherForActivityResult }
        일꾼.launch {
            val 남은 = (인증최대 - 상태.d.인증샷.size).coerceAtLeast(0)
            val 이름들 = withContext(Dispatchers.IO) { 사진함.넣기(ctx, 고른것.take(남은)) }
            이름들.forEach { 프로필파일지킴.지킴(ctx, it) }
            var 넣은 = 0
            상태.바꿈 { dd -> 인증여럿넣기(dd, 이름들, System.currentTimeMillis()).also { (_, n) -> 넣은 = n }.first }
            이름들.filter { 이름 -> 상태.d.인증샷.none { it.파일 == 이름 } }.forEach { 프로필파일지킴.놓음(ctx, it) }
            인증넣는중 = false
            if (넣은 > 0) 발자취.적기("인증샷 ${넣은}장")
            when {
                고른것.size > 남은 || 넣은 < 이름들.size -> 상태.알림.토스트(프로필글.인증8장)
                이름들.size < 고른것.size -> 상태.알림.토스트(프로필글.못읽음)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        당겨새로고침({ 상태.날짜확인() }, Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(start = 간격.보통, end = 간격.보통, top = 간격.좁게, bottom = 간격.보통),
                verticalArrangement = Arrangement.spacedBy(간격.좁게),
            ) {
                프로필들어옴 {
                    프로필머리(
                        d, 사진넣는중,
                        사진누름 = { if (!사진넣는중) { 사진넣는중 = true; 사진고르기.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) } },
                        닉바꿈 = { v -> 상태.바꿈 { it.copy(설정 = it.설정.copy(닉네임 = 닉네임자름(v))) } },
                        시트열기 = { 시트 = it },
                    )
                }
                프로필들어옴 { 프로필업적한줄(상태) { 업적열림 = true } }
                프로필들어옴 {
                    프로필인증판(d, 인증넣는중, { 시트 = 프로필시트.인증(it) }) {
                        if (!인증넣는중) { 인증넣는중 = true; 인증고르기.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                    }
                }
            }
        }
        시트?.let { s -> 프로필시트그림(상태, s, { 시트 = it }) { 시트 = null } }
        // 시안 `스탯열기` 업적 — 탭줄 위 칸을 덮는다. 다른 탭을 누르면 이 화면과 함께 닫힌다
        if (업적열림) 스탯화면(상태, 스탯화면글.업적, null, 탭줄위 = true) { 업적열림 = false }
    }
}

/**
 * ⚠ App.kt 는 앱을 켤 때 `사진함.정리(종목 사진만)` 을 부른다 → 사진 폴더의 프로필 사진 · 인증샷 파일이 2분 뒤 켤 때 지워진다.
 * 합칠 때 App.kt 가 이 둘도 '쓰는 것' 에 넣기 전까지, 쓰는 파일은 고친 시각을 먼 뒤로 미뤄 정리에서 빠지게 한다.
 * 안 쓰게 된 파일(바꾼 옛 프로필 · 지운 인증샷)은 시각을 지금으로 돌려 다음 정리 때 치워지게 한다 — 보고서 '공용 고칠 것'
 */
internal object 프로필파일지킴 {
    private const val 먼뒤 = 100L * 365 * 24 * 3600 * 1000
    fun 지킴(ctx: android.content.Context, 이름: String) { try { 사진함.파일(ctx, 이름).setLastModified(System.currentTimeMillis() + 먼뒤) } catch (_: Exception) { } }
    fun 놓음(ctx: android.content.Context, 이름: String) { try { 사진함.파일(ctx, 이름).setLastModified(System.currentTimeMillis()) } catch (_: Exception) { } }
}

/** 이 화면의 글 — 한곳에 (시안 글 그대로) */
object 프로필글 {
    const val 닉네임 = "닉네임"
    const val 사진고르기 = "프로필 사진 고르기"
    const val 링크보기 = "SNS 링크 보기"
    const val 링크적기 = "SNS 링크 적기"
    const val 링크제목 = "SNS 링크"
    const val 링크안내 = "https://"
    const val 칸추가 = "＋ 칸 추가"
    const val 저장 = "저장"
    const val 보고방식 = "결과 보고서 표시 방법"
    const val 보일것 = "보일 것"
    const val 프로필 = "프로필"
    const val 최대5 = "최대 5개까지"
    const val 업적정렬 = "업적 정렬"
    const val 직접 = "지금은 꾹 눌러 옮긴 순서"
    const val 업적없음 = "아직 달성한 업적이 없습니다"
    const val 보일업적없음 = "프로필에 보일 업적이 없습니다"
    const val 인증샷 = "인증샷"
    const val 인증올리기 = "인증샷 올리기"
    const val 고정 = "맨 위에 고정"
    const val 고정풀기 = "고정 풀기"
    const val 지우기 = "지우기"
    const val 고정3장 = "고정은 3장까지"
    const val 인증8장 = "인증샷은 8장까지"
    const val 못읽음 = "이 사진은 읽지 못했습니다"
    const val 링크못엶 = "이 링크를 열지 못했습니다"
    const val 지금값 = "지금 값"
    const val 최고날 = "최고 기록 날짜"
    const val 준비중 = "자세한 기록은 준비 중입니다"
    const val 상세 = "상세"
    fun 인증지움(n: Int) = if (n > 1) "인증샷 ${n}장을 지웠습니다" else "인증샷을 지웠습니다"
}

/** 이 화면의 치수 · 시간 — 시안 CSS 값 그대로 (v11 · v15 · v16 · v17 홍겸 님 값). 지침 단계 밖 값은 합칠 때 Theme.kt 로 옮길 후보 */
private object 프로필치수 {
    val 사진 = 88.dp              // .보고사진.크게
    val 사진그림 = 28.dp           // 사람 그림 svg 28
    val 머리사이 = 간격.넓게        // .인머리 gap 16
    val 닉줄 = 높이.아주낮게        // 28
    val 닉내림 = 7.dp              // .인닉줄 top:7px (닉네임 줄 맞춤 · v18 E ④)
    val 닉최소 = 72.dp             // min-width 4em (18 × 4)
    val 링크칸 = 높이.아주낮게      // 28
    val 링크작은그림 = 15.dp        // .인링크보기 svg 15 · 흐리게 0.5
    const val 링크흐림 = 0.5f
    val 톱니 = 높이.아주낮게        // 28 · 아이콘 15.3
    val 톱니그림 = 15.dp
    val 큰합높이 = 높이.아주낮게    // 파란 상자 28
    val 큰합빼기 = 30.dp           // (칸 줄 − 30) × 0.935 (v17 5)
    const val 큰합비 = 0.935f
    val 큰값줄 = 22.dp
    val 큰이름줄 = 14.dp
    val 큰칸옆 = 간격.아주좁게      // 칸 좌우 여백 4 (5~6칸은 1)
    val 큰칸옆좁게 = 1.dp
    val 업칸 = 64.dp               // .인업 (v15)
    val 업동 = 48.dp               // .인동 (v15)
    val 판사이 = 2.dp              // .인판 gap 2
    const val 판열 = 4
    val 핀 = 24.dp
    val 핀그림 = 16.dp
    val 핀자리 = 간격.아주좁게
    val 들어옴 = 50.dp             // 묶음이 들어오는 거리
    const val 들어옴시간 = 480     // .24s × 2
}

/** 프로필 탭의 시트 — 한 번에 하나 */
sealed class 프로필시트 {
    data object 링크 : 프로필시트()
    data object 링크목록 : 프로필시트()
    data object 보고방식 : 프로필시트()
    data class 인증(val 파일: String) : 프로필시트()
    data class 큰운동(val 글: String) : 프로필시트()
}

// ═════════════════════════ 순수 계산 (시험: ui/ProfileTest.kt) ═════════════════════════

/** 닉네임 — 12자까지 (시안 maxlength 12 · 줄바꿈은 뺀다) */
internal fun 닉네임자름(v: String): String = v.replace("\n", "").replace("\r", "").take(닉네임최대)

/** 큰 운동 칸 하나 — 글(짧은 이름 · 맨 앞은 'N대') · 값(1RM · 합은 0.1 반올림) · 그 값을 처음 낸 날 */
internal data class 프로필큰칸(val 글: String, val v: Double, val 때: String?)

/**
 * 프로필의 큰 운동 값 (시안 `큰운동값(null, null)`) — 오늘까지 저장된 기록에서 종목마다 가장 좋은 추정 1RM.
 * 맨 앞 칸 = N대 합계(때 = 칸들 중 가장 늦은 날). 칸 = 스쿼트 · 벤치 · 데드 + 설정.큰운동추가(최대 2).
 * 워밍업 세트 · 무게 0 은 뺀다. 같은 값이면 먼저 낸 날
 */
internal fun 앱데이터.프로필큰운동값(오늘: String): List<프로필큰칸> {
    val 추 = 큰운동추가정리(설정.큰운동추가)
    val 목록 = 기록.entries.filter { 날짜만(it.key) <= 오늘 }.map { 날짜만(it.key) to it.value }
    val 칸 = 큰운동표.filterIndexed { j, x -> j < 3 || x.키 in 추 }.map { x ->
        var v = 0.0; var 때: String? = null
        for ((날, r) in 목록) for (e in r.종목들) if (e.이름 in x.이름들) for (s in 기록세트(e.세트들)) if (s.w > 0) {
            val m = 일RM(s.w, s.r)
            if (m > v || (m == v && 때 != null && 날 < 때)) { v = m; 때 = 날 }
        }
        프로필큰칸(x.짧은, Math.round(v * 10) / 10.0, 때)
    }
    val 합 = Math.round(칸.sumOf { it.v } * 10) / 10.0
    val 때들 = 칸.mapNotNull { it.때 }.sorted()
    return listOf(프로필큰칸("${칸.size}대", 합, 때들.lastOrNull())) + 칸
}

/** 상세 시트의 값 글 — 합은 정수, 칸은 시안 `차kg` (100 이상이거나 정수면 콤마 · 아니면 소수 한 자리) */
internal fun 프로필큰값글(v: Double, 합: Boolean): String = when {
    v <= 0 -> "—"
    합 -> "${v.roundToInt()}kg"
    v >= 100 || v == Math.round(v).toDouble() -> "${콤마(v)}kg"
    else -> "${무게글(Math.round(v * 10) / 10.0)}kg"
}

/** 상세 시트의 날 글 — "2026.10.05." */
internal fun 프로필큰날글(때: String?): String = 때?.let { it.replace('-', '.') + "." } ?: "—"

/** 프로필 업적 줄 — 정렬대로, 숨긴 것은 빼고 (시안 `최근업적줄`). 업적표에 없는 번호는 뺀다 */
internal fun 프로필업적줄(d: 앱데이터): List<String> = d.업적차례().filter { d.업적보임(it) && 업적표.찾기(it) != null }

/**
 * 끌어 옮기기 — 끌기 시작 때의 줄([시작키들]) 번호를 지금 줄 번호로 바꿔 [업적옮기기]. 끄는 동안 줄이 바뀌었어도(새 업적 · 숨김) 같은 업적끼리 옮긴다.
 * 끈 업적이나 놓은 자리 업적이 없어졌으면 그대로
 */
internal fun 업적끌어옮김(d: 앱데이터, 시작키들: List<String>, 원: Int, 대상: Int, 뒤에: Boolean): 앱데이터 {
    val k원 = 시작키들.getOrNull(원) ?: return d
    val k대상 = 시작키들.getOrNull(대상) ?: return d
    val 지금 = 프로필업적줄(d)
    val i = 지금.indexOf(k원); val j = 지금.indexOf(k대상)
    if (i < 0 || j < 0) return d
    return d.업적옮기기(i, j, 뒤에)
}

/** 인증샷 여럿 넣기 — 넘치는 것은 버린다. (새 데이터, 넣은 수). 같은 파일 이름은 두 번 넣지 않는다 */
internal fun 인증여럿넣기(d: 앱데이터, 이름들: List<String>, 지금: Long): Pair<앱데이터, Int> {
    var x = d; var n = 0
    이름들.forEachIndexed { i, 이름 ->
        if (x.인증샷.any { it.파일 == 이름 }) return@forEachIndexed
        val 새 = x.인증넣기(이름, 지금 + i) ?: return@forEachIndexed
        x = 새; n++
    }
    return x to n
}

/** 인증샷 되돌리기 — 지운 한 장을 다시 (이미 있거나 8장이면 그대로 · null = 자리 없음) */
internal fun 인증되살림(d: 앱데이터, x: 인증사진): 앱데이터? = when {
    d.인증샷.any { it.파일 == x.파일 } -> d
    d.인증샷.size >= 인증최대 -> null
    else -> d.copy(인증샷 = d.인증샷 + x)
}

/** 링크 목록에 보일 글 — 앞의 http(s):// 와 끝의 / 를 뗀다 (시안 `링크목록` 시트) */
internal fun 링크보임글(u: String): String = u.replace(Regex("^https?://", RegexOption.IGNORE_CASE), "").removeSuffix("/")

/** 저장할 링크 — 다듬고 빈 것 · http(s) 아닌 꼴은 버린다 (시안 "링크저장") */
internal fun 링크저장값(칸들: List<String>): List<String> = 칸들.map { 링크주소(it) }.filter { it.isNotEmpty() }

/** 큰 운동 칩 누름 — 앞 셋은 늘 켜짐, 나머지는 2개까지 (시안 "큰운동칩"). null = 이미 2개라 못 켬 */
internal fun 프로필큰칩바꿈(추가: List<String>, 키: String): List<String>? {
    val j = 큰운동표.indexOfFirst { it.키 == 키 }
    if (j < 3) return 큰운동추가정리(추가)
    val 지금 = 큰운동추가정리(추가)
    return when {
        키 in 지금 -> 지금 - 키
        지금.size >= 2 -> null
        else -> 큰운동추가정리(지금 + 키)
    }
}

/** 닉네임 줄 왼쪽 — 파란 상자 왼쪽과 맞춘다 (시안 `닉맞춤` · 상자 폭 = (칸 줄 − 30) × 0.935, 가운데) */
internal fun 닉왼(줄폭: Float, 빼기: Float, 비: Float = 0.935f): Float = ((줄폭 - (줄폭 - 빼기).coerceAtLeast(0f) * 비) / 2f).coerceAtLeast(0f)

// ═════════════════════════ 화면 ═════════════════════════

/** v17 ① 묶음이 상하좌우 중 하나에서 50 떨어져 들어온다 (0.48초 · 흐림 → 또렷). 자리는 offset 으로 — 끝나면 제자리를 다시 잰다 */
@Composable
private fun 프로필들어옴(content: @Composable () -> Unit) {
    val 방 = remember { listOf(0 to -1, 0 to 1, -1 to 0, 1 to 0)[Random.nextInt(4)] }
    val p = remember { Animatable(0f) }
    LaunchedEffect(Unit) { p.animateTo(1f, tween(프로필치수.들어옴시간, easing = androidx.compose.animation.core.FastOutSlowInEasing)) }
    Box(
        Modifier
            .offset {
                val r = (1f - p.value) * 프로필치수.들어옴.toPx()
                IntOffset((방.first * r).roundToInt(), (방.second * r).roundToInt())
            }
            .graphicsLayer { alpha = p.value },
    ) { content() }
}

/** 시안 `.인머리` — 사진 88 · 오른쪽(닉네임 줄 · 큰 운동 칸) · 톱니(오른쪽 위 모서리에 겹쳐) */
@Composable
private fun 프로필머리(d: 앱데이터, 사진넣는중: Boolean, 사진누름: () -> Unit, 닉바꿈: (String) -> Unit, 시트열기: (프로필시트) -> Unit) {
    val c = Local색.current
    Box(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(vertical = 간격.좁게), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(프로필치수.머리사이)) {
            큰프로필사진(d.설정, Modifier.눌림 { if (!사진넣는중) 사진누름() }.semantics { contentDescription = 프로필글.사진고르기; role = Role.Button })
            BoxWithConstraints(Modifier.weight(1f)) {
                val 왼 = with(androidx.compose.ui.platform.LocalDensity.current) { 닉왼(maxWidth.toPx(), 프로필치수.큰합빼기.toPx(), 프로필치수.큰합비).toDp() }
                Column(verticalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                    프로필닉줄(d.설정, 왼, 닉바꿈, { 시트열기(프로필시트.링크목록) }) { 시트열기(프로필시트.링크) }
                    프로필큰운동판(d) { 시트열기(프로필시트.큰운동(it)) }
                }
            }
        }
        Box(
            Modifier.align(Alignment.TopEnd).size(프로필치수.톱니).눌림 { 시트열기(프로필시트.보고방식) }
                .semantics { contentDescription = 프로필글.보고방식; role = Role.Button },
            contentAlignment = Alignment.Center,
        ) { Icon(아이콘.톱니, null, Modifier.size(프로필치수.톱니그림), tint = c.흐림) }
    }
}

/**
 * 사진 88 — 사진 / 닉네임 첫 글자(28 굵게) / 사람 그림 28 (시안 `프로필그림` · `.보고사진.크게`).
 * Parts.kt 프로필동그라미와 같은 규칙이지만 글자가 28 이라 여기서 그린다 → 공용 후보(프로필동그라미에 글자크기 인자)
 */
@Composable
private fun 큰프로필사진(설정: 설정값, modifier: Modifier) {
    val c = Local색.current
    val ctx = LocalContext.current
    val 사진 = 설정.프로필사진?.takeIf { it.isNotBlank() }
    val 있음 = remember(사진) { 사진 != null && 사진함.파일(ctx, 사진).exists() }
    val 첫 = 설정.닉네임.trim().let { if (it.isEmpty()) null else String(Character.toChars(it.codePointAt(0))) }
    Box(
        modifier.size(프로필치수.사진).clip(CircleShape).background(c.강조옅음).border(선굵기.보통, c.선, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        when {
            있음 && 사진 != null -> 사진그림(사진, 프로필치수.사진 * 2, Modifier.fillMaxSize())
            첫 != null -> Text(첫, style = 글꼴.보통(크기.아주큰숫자, FontWeight.Bold), color = c.강조, maxLines = 1)
            else -> Icon(아이콘.프로필사람, null, Modifier.size(프로필치수.사진그림), tint = c.강조)
        }
    }
}

/**
 * 시안 `.인닉줄` — 닉네임 칸(18 굵게 · 글 길이만큼 · 쓰는 동안은 줄 끝까지 · 밑줄 강조) + 링크.
 * 쓰지 않을 때: 링크가 있으면 흐린 작은 링크(→ 목록). 쓰는 동안: 오른쪽에 링크 단추(→ 적기)
 */
@Composable
private fun 프로필닉줄(설정: 설정값, 왼: Dp, 닉바꿈: (String) -> Unit, 링크보기: () -> Unit, 링크적기: () -> Unit) {
    val c = Local색.current
    val 초점 = LocalFocusManager.current
    var 쓰는중 by remember { mutableStateOf(false) }
    // 칸 안 글은 화면이 쥔다 — 저장된 값이 밖에서 바뀌면(백업 가져오기) 따라간다
    var 닉 by remember { mutableStateOf(설정.닉네임) }
    LaunchedEffect(설정.닉네임) { if (!쓰는중 || 설정.닉네임 != 닉네임자름(닉)) 닉 = 설정.닉네임 }
    val 링 = 설정.링크.isNotEmpty()
    Row(
        Modifier.fillMaxWidth().height(프로필치수.닉줄).offset(y = 프로필치수.닉내림).padding(start = 왼, end = 프로필치수.톱니),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.아주좁게),
    ) {
        Box(Modifier.weight(1f, fill = 쓰는중).widthIn(min = 프로필치수.닉최소), contentAlignment = Alignment.CenterStart) {
            if (닉.isEmpty()) Text(프로필글.닉네임, style = 글꼴.보통(크기.크게, FontWeight.Bold), color = c.옅음, maxLines = 1)
            BasicTextField(
                value = 닉,
                onValueChange = { v -> val x = 닉네임자름(v); 닉 = x; 닉바꿈(x) },
                singleLine = true,
                textStyle = 글꼴.보통(크기.크게, FontWeight.Bold).copy(color = c.글),
                cursorBrush = SolidColor(c.강조),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { 초점.clearFocus() }),
                modifier = Modifier.widthIn(min = 프로필치수.닉최소).then(if (쓰는중) Modifier.fillMaxWidth() else Modifier).onFocusChanged { 쓰는중 = it.isFocused }
                    .drawBehind { if (쓰는중) drawRect(c.강조, Offset(0f, size.height - 선굵기.보통.toPx()), Size(size.width, 선굵기.보통.toPx())) }
                    .semantics { contentDescription = 프로필글.닉네임 },
            )
        }
        if (쓰는중) Box(
            Modifier.size(프로필치수.링크칸).눌림 { 초점.clearFocus(); 링크적기() }.semantics { contentDescription = 프로필글.링크적기; role = Role.Button },
            contentAlignment = Alignment.Center,
        ) { Icon(아이콘.링크, null, Modifier.size(크기그림), tint = c.강조) }
        else if (링) Box(
            Modifier.size(프로필치수.링크칸).눌림(링크보기).semantics { contentDescription = 프로필글.링크보기; role = Role.Button },
            contentAlignment = Alignment.Center,
        ) { Icon(아이콘.링크, null, Modifier.size(프로필치수.링크작은그림).graphicsLayer { alpha = 프로필치수.링크흐림 }, tint = c.흐림) }
    }
}

private val 크기그림 = 18.dp   // 11 지침 U3-6 기본 아이콘

/**
 * 시안 `프로필큰운동판(큰운동값(null, null), false, false)` — 파란 상자 'N대 Nkg'(소수점 없이) 위 · 칸(숫자 굵게 · 이름 11 흐림) 아래 · 칸 사이 세로선.
 * 늘 한 줄. 숫자가 칸을 넘으면 한 줄 숫자 전부를 같은 크기로 15 → 13 → 11 (시안 `큰값맞춤`). 누르면 '○○ 상세'
 */
@Composable
private fun 프로필큰운동판(d: 앱데이터, 상세: (String) -> Unit) {
    val c = Local색.current
    val 오늘 = remember { java.time.LocalDate.now().toString() }
    val 칸 = remember(d.기록, d.설정.큰운동추가) { d.프로필큰운동값(오늘) }
    val 합 = 칸.first(); val 들 = 칸.drop(1)
    val 열 = maxOf(3, 들.size)
    val 바탕 = when { 열 >= 6 -> 크기.조금작게; 열 == 5 -> 크기.본문; else -> 크기.크게 }
    val 측정 = rememberTextMeasurer()
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
        BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                Modifier.width(((maxWidth - 프로필치수.큰합빼기) * 프로필치수.큰합비).coerceAtLeast(0.dp)).height(프로필치수.큰합높이).clip(RoundedCornerShape(모서리.작게)).background(c.강조)
                    .눌림 { 상세(합.글) }.semantics { contentDescription = "${합.글} ${프로필글.상세}"; role = Role.Button },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    buildAnnotatedString {
                        append("${합.글} ")
                        if (합.v > 0) { append("${합.v.roundToInt()}"); withStyle(SpanStyle(fontSize = 크기.작게)) { append("kg") } } else append("—")
                    },
                    style = 글꼴.보통(크기.조금작게, FontWeight.Bold), color = c.강조글, maxLines = 1, overflow = TextOverflow.Clip,
                )
            }
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val 옆 = if (열 >= 5) 프로필치수.큰칸옆좁게 else 프로필치수.큰칸옆
            val 칸폭 = with(androidx.compose.ui.platform.LocalDensity.current) { (maxWidth / 열 - 옆 * 2).toPx() }
            val 숫자들 = 들.map { if (it.v > 0) "${it.v.roundToInt()}" else "—" }
            val 맞는 = remember(숫자들, 칸폭, 바탕) {
                listOf(바탕.value, 15f, 13f, 11f).filter { it <= 바탕.value }.firstOrNull { fs ->
                    숫자들.all { 측정.measure(AnnotatedString(it), 글꼴.보통(fs.sp, FontWeight.Bold)).size.width <= 칸폭 }
                } ?: 11f
            }
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                들.forEachIndexed { i, x ->
                    Column(
                        Modifier.weight(1f).fillMaxHeight()
                            .drawBehind { if (i > 0) drawRect(c.선, size = Size(선굵기.보통.toPx(), size.height)) }
                            .눌림 { 상세(x.글) }.semantics { contentDescription = "${x.글} ${프로필글.상세}"; role = Role.Button }
                            .padding(horizontal = 옆),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(숫자들[i], Modifier.heightIn(min = 프로필치수.큰값줄), style = 글꼴.보통(맞는.sp, FontWeight.Bold), color = c.글, maxLines = 1, softWrap = false)
                        Text(x.글, Modifier.heightIn(min = 프로필치수.큰이름줄), style = 글꼴.보통(크기.작게), color = c.흐림, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

/**
 * 시안 `최근업적줄` — 달성한 업적 전부 · 한 줄 가로 스크롤 · 칸 64(동그라미 48 · 등급 색 테 2 · 칭호 첫 글자) + 칭호 11.
 * 누르면 업적 화면 · 꾹 눌러 좌우로 끌면 순서(Parts.kt 가로끌기 · 정렬이 '직접' 으로)
 */
@Composable
private fun 프로필업적한줄(상태: 앱상태, 열기: () -> Unit) {
    val c = Local색.current
    val d = 상태.d
    val 줄 = remember(d.업적, d.업적숨김, d.업적순서, d.설정.업적정렬) { 프로필업적줄(d) }
    if (줄.isEmpty()) {
        글(if (d.업적.keys.any { 업적표.찾기(it) != null }) 프로필글.보일업적없음 else 프로필글.업적없음,
            Modifier.padding(vertical = 간격.좁게), 크기값 = 크기.조금작게, 색 = c.옅음)
        return
    }
    val 넘김 = rememberScrollState()
    // 칸 수가 바뀌면 맨 앞으로 (시안: 업적 수가 바뀌면 업x = 0)
    var 전수 by remember { mutableStateOf(줄.size) }
    LaunchedEffect(줄.size) { if (줄.size != 전수) { 전수 = 줄.size; 넘김.scrollTo(0) } }
    // 끌기 시작 때의 줄(그때 그려져 있던 것) — 끄는 동안 줄이 바뀌어도 같은 업적끼리 옮긴다
    val 그린줄 by rememberUpdatedState(줄)
    var 시작줄 by remember { mutableStateOf(줄) }
    val 판 = remember가로끌기판 { 원, 대상, 뒤에 ->
        상태.바꿈 { 업적끌어옮김(it, 시작줄, 원, 대상, 뒤에) }
        발자취.적기("업적 순서 바꿈")
    }
    LaunchedEffect(판.원 != null) { if (판.원 != null) 시작줄 = 그린줄 }
    Row(
        Modifier.fillMaxWidth().가로끌기줄(판, 넘김).horizontalScroll(넘김).padding(vertical = 간격.아주좁게),
        horizontalArrangement = Arrangement.spacedBy(간격.좁게),
    ) {
        줄.forEachIndexed { i, 번호 ->
            val a = 업적표.찾기(번호)
            if (a != null) key(번호) {
                val 색 = 업적등급색(a, c)
                Column(
                    Modifier.width(프로필치수.업칸).가로끌기(판, i).눌림(열기).semantics { contentDescription = a.칭호; role = Role.Button },
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(간격.아주좁게),
                ) {
                    Box(
                        Modifier.size(프로필치수.업동).clip(CircleShape).background(c.면2).border(선굵기.굵게, 색, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        val 첫 = a.칭호.takeIf { it.isNotEmpty() }?.let { String(Character.toChars(it.codePointAt(0))) } ?: ""
                        Text(첫, style = 글꼴.보통(크기.본문, FontWeight.Bold), color = c.글, maxLines = 1)
                    }
                    Text(a.칭호, Modifier.widthIn(max = 프로필치수.업칸), style = 글꼴.보통(크기.작게), color = c.글, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

/**
 * 시안 `인증판` — 한 줄 4칸 · 3:4 · 사이 2 · 고정 3장이 맨 앞(핀) · 8장이 안 되면 끝에 [＋](점선).
 * 파일이 지워진 사진은 빈 칸으로 보인다 (눌러서 지울 수 있다)
 */
@Composable
private fun 프로필인증판(d: 앱데이터, 넣는중: Boolean, 열기: (String) -> Unit, 넣기: () -> Unit) {
    val c = Local색.current
    val 목 = remember(d.인증샷) { 인증순(d.인증샷).take(인증최대) }
    val 칸들: List<인증사진?> = 목 + (if (목.size < 인증최대) listOf(null) else emptyList())
    Column(verticalArrangement = Arrangement.spacedBy(프로필치수.판사이)) {
        칸들.chunked(프로필치수.판열).forEach { 줄 ->
            Row(horizontalArrangement = Arrangement.spacedBy(프로필치수.판사이)) {
                줄.forEach { x ->
                    val 칸 = Modifier.weight(1f).aspectRatio(3f / 4f)
                    if (x == null) Box(
                        칸.background(c.면2).점선둘레(c.속선, 0.dp).눌림 { if (!넣는중) 넣기() }
                            .semantics { contentDescription = 프로필글.인증올리기; role = Role.Button },
                        contentAlignment = Alignment.Center,
                    ) { 글(if (넣는중) "…" else "＋", 크기값 = 크기.큰숫자, 색 = c.강조) }
                    else key(x.파일) {
                        Box(칸.background(c.면2).눌림 { 열기(x.파일) }.semantics { contentDescription = 프로필글.인증샷 + if (x.고정 > 0) " (고정)" else ""; role = Role.Button }) {
                            사진그림(x.파일, 프로필치수.사진 * 2, Modifier.fillMaxSize())
                            if (x.고정 > 0) Box(
                                Modifier.align(Alignment.TopEnd).padding(프로필치수.핀자리).size(프로필치수.핀).clip(CircleShape).background(c.강조),
                                contentAlignment = Alignment.Center,
                            ) { Icon(아이콘.핀, null, Modifier.size(프로필치수.핀그림), tint = c.강조글) }
                        }
                    }
                }
                repeat(프로필치수.판열 - 줄.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

// ═════════════════════════ 시트 ═════════════════════════

@Composable
private fun 프로필시트그림(상태: 앱상태, s: 프로필시트, 바꿈: (프로필시트) -> Unit, 닫기: () -> Unit) {
    when (s) {
        프로필시트.링크 -> 프로필링크시트(상태, 닫기)
        프로필시트.링크목록 -> 프로필링크목록시트(상태, 닫기)
        프로필시트.보고방식 -> 프로필보고방식시트(상태, 닫기)
        is 프로필시트.인증 -> 프로필인증시트(상태, s.파일, 닫기)
        is 프로필시트.큰운동 -> 프로필큰상세시트(상태, s.글, 닫기)
    }
}

/** 시안 시트 `링크` (v18 E ⑤) — 기본 칸 1개 · ＋ 로 몇 개든 · [저장] (빈 칸 · http(s) 아닌 꼴은 버림) */
@Composable
private fun 프로필링크시트(상태: 앱상태, 닫기: () -> Unit) {
    val 칸들 = remember { mutableStateListOf<String>().apply { addAll(상태.d.설정.링크.ifEmpty { listOf("") }) } }
    시트(프로필글.링크제목, 닫기) {
        Column(verticalArrangement = Arrangement.spacedBy(간격.좁게)) {
            칸들.forEachIndexed { i, v ->
                key(i) {
                    프로필링크칸(v, { 칸들[i] = it }, "SNS 주소 ${i + 1}")
                }
            }
            버튼(프로필글.칸추가, { 칸들.add("") }, Modifier.fillMaxWidth(), 작게 = true)
            버튼(프로필글.저장, {
                val 새 = 링크저장값(칸들)
                상태.바꿈 { it.copy(설정 = it.설정.copy(링크 = 새)) }
                발자취.적기("SNS 링크 ${새.size}개")
                닫기()
            }, Modifier.fillMaxWidth(), 주요 = true, 작게 = true)
        }
    }
}

/** 주소 칸 — 입력칸(Common)과 같은 모양 · 자판은 주소용 */
@Composable
private fun 프로필링크칸(값: String, 바꿈: (String) -> Unit, 설명: String) {
    val c = Local색.current
    val 모양 = RoundedCornerShape(모서리.작게)
    Box(
        Modifier.fillMaxWidth().height(높이.보통).clip(모양).background(c.면).border(선굵기.보통, c.속선, 모양).padding(horizontal = 간격.보통),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (값.isEmpty()) 글(프로필글.링크안내, 색 = c.옅음)
        BasicTextField(
            값, 바꿈, Modifier.fillMaxWidth().semantics { contentDescription = 설명 }, singleLine = true,
            textStyle = 글꼴.보통(크기.본문).copy(color = c.글), cursorBrush = SolidColor(c.강조),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
        )
    }
}

/** 시안 시트 `링크목록` — [링크 그림] 주소(앞 https:// · 끝 / 뗌) › · 누르면 연다 */
@Composable
private fun 프로필링크목록시트(상태: 앱상태, 닫기: () -> Unit) {
    val c = Local색.current
    val ctx = LocalContext.current
    val 링 = 상태.d.설정.링크.map { 링크주소(it) }.filter { it.isNotEmpty() }
    시트(프로필글.링크제목, 닫기) {
        링.forEach { u ->
            Row(
                Modifier.fillMaxWidth().눌림 {
                    try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(u)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                    catch (_: Exception) { 상태.알림.토스트(프로필글.링크못엶) }
                }.padding(vertical = 간격.보통),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.좁게),
            ) {
                Icon(아이콘.링크, null, Modifier.size(크기그림), tint = c.강조)
                Text(링크보임글(u), Modifier.weight(1f), style = 글꼴.보통(크기.본문, FontWeight.Bold), color = c.글, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(아이콘.오른쪽, null, Modifier.size(크기그림), tint = c.흐림)
            }
            구분선()
        }
    }
}

/** 시안 시트 `보고방식` (프로필 탭) — 보일 것(프로필) · 큰 운동 칩(앞 셋 늘 켜짐 · 2개 더) · 업적 정렬 */
@Composable
private fun 프로필보고방식시트(상태: 앱상태, 닫기: () -> Unit) {
    val c = Local색.current
    val s = 상태.d.설정
    val 추 = 큰운동추가정리(s.큰운동추가)
    시트(프로필글.보고방식, 닫기) {
        이름표(프로필글.보일것, Modifier.padding(bottom = 간격.아주좁게))
        설정줄(프로필글.프로필) {
            스위치(s.보고서보임.프로필) { v -> 상태.바꿈 { it.copy(설정 = it.설정.copy(보고서보임 = it.설정.보고서보임.copy(프로필 = v, 루틴 = true))) } }
        }
        이름표("큰 운동 · ${3 + 추.size}대", Modifier.padding(top = 간격.좁게, bottom = 간격.좁게))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(간격.좁게), verticalArrangement = Arrangement.spacedBy(간격.좁게)) {
            큰운동표.forEachIndexed { j, x ->
                val 늘 = j < 3
                val 켬 = 늘 || x.키 in 추
                val 막 = !켬 && 추.size >= 2
                프로필켬칩(x.키, 켬, 막) {
                    if (늘) return@프로필켬칩
                    val 새 = 프로필큰칩바꿈(s.큰운동추가, x.키)
                    if (새 == null) 상태.알림.토스트(프로필글.최대5)
                    else 상태.바꿈 { it.copy(설정 = it.설정.copy(큰운동추가 = 새)) }
                }
            }
        }
        if (추.size >= 2) 글(프로필글.최대5, Modifier.padding(top = 간격.아주좁게), 크기값 = 크기.작게, 색 = c.옅음)
        이름표(프로필글.업적정렬, Modifier.padding(top = 간격.보통, bottom = 간격.좁게))
        칩줄(업적정렬목록.filter { it != "직접" }, s.업적정렬, { v -> 상태.바꿈 { it.copy(설정 = it.설정.copy(업적정렬 = v)) } })
        if (s.업적정렬 == "직접") 글(프로필글.직접, Modifier.padding(top = 간격.아주좁게), 크기값 = 크기.작게, 색 = c.옅음)
    }
}

/** 켜고 끄는 칩 — 칩줄의 칩과 같은 모양(32 · 둥근 · 켜면 강조). 여럿 켤 수 있어 칩줄 대신 (공용 후보) */
@Composable
private fun 프로필켬칩(글자: String, 켬: Boolean, 막: Boolean, onClick: () -> Unit) {
    val c = Local색.current
    val 바탕 = 색움직(if (켬) c.강조 else c.면, "켬칩")
    val 테 = 색움직(if (켬) c.강조 else c.속선, "켬칩테")
    Box(
        Modifier.height(높이.낮게).clip(CircleShape).background(바탕).border(선굵기.보통, 테, CircleShape)
            .then(if (막) Modifier else Modifier.눌림(onClick))
            .semantics { role = Role.Checkbox; stateDescription = if (켬) "켜짐" else "꺼짐" }
            .padding(horizontal = 간격.보통),
        contentAlignment = Alignment.Center,
    ) { 글(글자, 크기값 = 크기.버튼, 색 = if (켬) c.강조글 else if (막) c.옅음 else c.흐림, 굵기 = FontWeight.Medium) }
}

/** 시안 시트 `인증` — 큰 사진(맞춤 · 화면 반까지) · [맨 위에 고정 | 고정 풀기] [지우기]. 지우면 띠로 되돌린다 (U5-4) */
@Composable
private fun 프로필인증시트(상태: 앱상태, 파일: String, 닫기: () -> Unit) {
    val c = Local색.current
    val ctx = LocalContext.current
    val x = 상태.d.인증샷.firstOrNull { it.파일 == 파일 }
    var 눌림끝 by remember { mutableStateOf(false) }   // 빠르게 두 번 눌러도 한 번만
    val 화면높이 = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp
    시트(프로필글.인증샷, 닫기) {
        if (x == null) return@시트
        Box(Modifier.fillMaxWidth().heightIn(max = 화면높이 / 2).clip(RoundedCornerShape(모서리.작게)).background(c.면2), contentAlignment = Alignment.Center) {
            프로필사진크게(x.파일)
        }
        Row(Modifier.fillMaxWidth().padding(top = 간격.보통), horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
            버튼(if (x.고정 > 0) 프로필글.고정풀기 else 프로필글.고정, {
                if (눌림끝) return@버튼
                눌림끝 = true
                val 새 = 상태.d.인증고정(x.파일, System.currentTimeMillis())
                닫기()
                if (새 == null) 상태.알림.토스트(프로필글.고정3장) else 상태.바꿈 { dd -> dd.인증고정(x.파일, System.currentTimeMillis()) ?: dd }
            }, Modifier.weight(1f), 작게 = true)
            버튼(프로필글.지우기, {
                if (눌림끝) return@버튼
                눌림끝 = true
                상태.바꿈 { dd -> dd.copy(인증샷 = dd.인증샷.filter { it.파일 != x.파일 }) }
                프로필파일지킴.놓음(ctx, x.파일)
                발자취.적기("인증샷 지움")
                닫기()
                상태.알림.되돌림("인증지움", { n -> 프로필글.인증지움(n) }) {
                    val 새 = 인증되살림(상태.d, x)
                    if (새 == null) 상태.알림.토스트(프로필글.인증8장)
                    else { 프로필파일지킴.지킴(ctx, x.파일); 상태.바꿈 { dd -> 인증되살림(dd, x) ?: dd } }
                }
            }, Modifier.weight(1f), 작게 = true, 글색 = c.나쁨)
        }
    }
}

/** 큰 사진 — 잘라 내지 않고 맞춤 (시안 `.인큰사진` object-fit:contain) */
@Composable
private fun 프로필사진크게(이름: String) {
    val ctx = LocalContext.current
    val px = with(androidx.compose.ui.platform.LocalDensity.current) { 프로필치수.사진.roundToPx() * 6 }
    var 그림 by remember(이름) { mutableStateOf(사진함.기억값(이름, px)) }
    LaunchedEffect(이름) { if (그림 == null) 그림 = withContext(Dispatchers.IO) { 사진함.읽기(ctx, 이름, px) } }
    val b = 그림
    if (b != null) androidx.compose.foundation.Image(b, 프로필글.인증샷, Modifier.fillMaxWidth().aspectRatio(b.width.toFloat() / b.height.coerceAtLeast(1)), contentScale = ContentScale.Fit)
    else Box(Modifier.fillMaxWidth().aspectRatio(3f / 4f))
}

/** 시안 시트 `큰운동상세` — 형태만: 지금 값 · 최고 기록 날짜 · '자세한 기록은 준비 중입니다' */
@Composable
private fun 프로필큰상세시트(상태: 앱상태, 칸글: String, 닫기: () -> Unit) {
    val c = Local색.current
    val 오늘 = remember { java.time.LocalDate.now().toString() }
    val 값들 = remember(상태.d.기록, 상태.d.설정.큰운동추가) { 상태.d.프로필큰운동값(오늘) }
    val i = 값들.indexOfFirst { it.글 == 칸글 }
    val x = 값들.getOrNull(i) ?: 프로필큰칸(칸글, 0.0, null)
    시트("$칸글 ${프로필글.상세}", 닫기) {
        설정줄(프로필글.지금값) { 글(프로필큰값글(x.v, i == 0), 굵기 = FontWeight.Bold) }
        구분선()
        설정줄(프로필글.최고날) { 글(프로필큰날글(x.때)) }
        글(프로필글.준비중, 크기값 = 크기.작게, 색 = c.옅음)
    }
}
