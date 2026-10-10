package com.slayde.hasenheide.ui

import android.app.Activity
import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.core.content.FileProvider
import com.slayde.hasenheide.data.기록세트
import com.slayde.hasenheide.data.kg글
import com.slayde.hasenheide.data.날기록
import com.slayde.hasenheide.data.날더하기
import com.slayde.hasenheide.data.날짜만
import com.slayde.hasenheide.data.다음회
import com.slayde.hasenheide.data.두대비
import com.slayde.hasenheide.data.목표단위
import com.slayde.hasenheide.data.목표세트
import com.slayde.hasenheide.data.무게글
import com.slayde.hasenheide.data.묶음이름
import com.slayde.hasenheide.data.볼륨
import com.slayde.hasenheide.data.분초
import com.slayde.hasenheide.data.비교
import com.slayde.hasenheide.data.세션종목
import com.slayde.hasenheide.data.세트
import com.slayde.hasenheide.data.세트종류
import com.slayde.hasenheide.data.실제진행값
import com.slayde.hasenheide.data.앱데이터
import com.slayde.hasenheide.data.운동세션
import com.slayde.hasenheide.data.웜칸수
import com.slayde.hasenheide.data.일RM
import com.slayde.hasenheide.data.재개
import com.slayde.hasenheide.data.보고끝
import com.slayde.hasenheide.data.시작날
import com.slayde.hasenheide.data.보고저장
import com.slayde.hasenheide.data.보고저장결과
import com.slayde.hasenheide.data.저장기록열쇠
import com.slayde.hasenheide.data.종목기록
import com.slayde.hasenheide.data.종목열쇠
import com.slayde.hasenheide.data.종목찾기
import com.slayde.hasenheide.data.같은이름번호
import com.slayde.hasenheide.data.찬것
import com.slayde.hasenheide.data.처방세트
import com.slayde.hasenheide.data.총칸
import com.slayde.hasenheide.data.측정날값
import com.slayde.hasenheide.data.콤마
import com.slayde.hasenheide.data.큰운동추가정리
import com.slayde.hasenheide.data.큰운동표
import com.slayde.hasenheide.data.퍼센트
import com.slayde.hasenheide.data.한세트수
import com.slayde.hasenheide.data.회처방
import com.slayde.hasenheide.data.흐른초
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.글꼴
import com.slayde.hasenheide.ui.theme.보고떠값
import com.slayde.hasenheide.ui.theme.높이
import com.slayde.hasenheide.ui.theme.모서리
import com.slayde.hasenheide.ui.theme.선굵기
import com.slayde.hasenheide.ui.theme.크기
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * 운동 보고서(결과) 화면 — 시안 v21 `결과뷰` · `큰운동판` · `보고방식시트` · `큰운동상세시트` · `루틴상세시트` (10-05 옮기기 2단계 R).
 *
 *  · 띠 '운동 보고서' + 날짜(제목 오른쪽 15 · 아래 15) · 왼쪽 카메라(갤러리 저장) · 오른쪽 공유 (v9 mdot · v17 · v19 B)
 *  · 프로필 상자 — 사진 58 + 닉네임 · 큰 운동 칸(N대 파란 상자 + SBD …, 늘 한 줄) · 톱니 (v10 · v11~16 · v17)
 *  · 루틴 상자 — 한 줄 [이름(7자 폭) · 볼륨 ▲▼ | 세트 | 총 볼륨 | 운동 시간] (누르면 루틴 상세 — 틀만) (v9 wxgv · v18 B · 10-06 v23 ② · v24 ①)
 *  · 종목 두 칸 격자 — 1RM · 볼륨 지난번 대비 ▲▼ · 누르면 그 줄 아래 상세(세트 세로 먼저) (v9 7jjc · ya8r · v10 oaee)
 *  · 끝내기 전 보고서 — 열리는 순간 저장(다시 끝내면 같은 기록에 덮어씀) · 아래 단추 줄 없음 · 떠 있는 ‹ (운동으로) › (스탯) (10-06 v22 D · v23 ③)
 *    저장된 보고서는 아래 [확인] (v17 · v18)
 *
 * 다른 파일이 부르는 것: [결과화면] (App) · [마무리] (운동화면) — 매개변수를 바꾸지 않는다.
 * 새로 둔 것: [기록보고서] (캘린더 [운동 보고서] 가 저장된 기록 하나를 열 때) · [보고방식시트] · [큰운동판] · [큰운동값] (프로필 탭이 같이 쓸 수 있게)
 */

// ═════════════════════ 시안 값 (합칠 때 Theme.kt 부품치수로) ═════════════════════

/** 시안 CSS 값 그대로. 11 지침 단계 밖의 값(47.6 · 15 · 58 · 64 · 15.3 …)은 시안에서 홍겸 님이 정한 값 */
private object 보고치수 {
    val 띠높이 = 47.6.dp          // v17 B 3 — 56 × 0.85
    val 날짜옆 = 15.dp            // v17 B 2 — 제목 글자 오른쪽 끝에서
    val 날짜위 = 15.dp            // v17 B 2 — 제목 위끝에서
    val 찍기폭 = 32.dp            // v19 B — 카메라 · 공유 누르는 칸 32 × 40 · 아이콘 18
    val 사진 = 58.dp              // v10 0vnw — 48 × 1.2
    val 사진칸 = 64.dp            // v10 — 사진 58 + 좌우 3
    val 사진그림 = 28.dp          // 사진 없을 때 사람 그림 (시안 .보고사진 svg)
    val 톱니칸 = 28.dp
    val 톱니그림 = 15.3.dp        // v17 B 8 — 18 × 0.85
    val 톱니비킴 = 24.dp          // 톱니 밑에 파란 상자 · 이름 줄이 깔리지 않게
    val 큰합옆 = 15.dp            // v16 — 파란 상자 좌우
    const val 큰합비율 = 0.935f   // v17 B 6 · v18 B 5 — 85% × 1.1
    val 칩옆 = 10.dp              // 시안 .칩 좌우 여백
    val 들어옴거리 = 6.dp
    val 좁은칸옆 = 1.dp           // 5~6칸이면 칸 좌우 1 (시안 .열5>div)
    const val 띠들어옴 = 1_600    // v17 B 1 — .8s × 2
    const val 상자들어옴 = 480    // v17 B 1 — .24s × 2
    const val 상세들어옴 = 240
    const val 찍는중흐림 = 0.4f   // 시안 .보고찍기:disabled
    const val 막힌칩 = 0.35f      // 시안 .칩:disabled
    const val 자간끝 = -0.06f     // 큰값맞춤 — 11 까지 줄여도 넘치면
}

private val 띠곡선 = CubicBezierEasing(0.22f, 0.61f, 0.36f, 1f)   // 시안 보고내려옴
private val 올라옴곡선 = CubicBezierEasing(0f, 0f, 0.58f, 1f)       // 시안 ease-out
private val 하나: () -> Float = { 1f }

// ═════════════════════ 순수 계산 (시험: app/src/test/java/com/slayde/hasenheide/ui/ReportTest.kt) ═════════════════════

/** 보고서 종목 칸의 한 줄 — "1RM 78kg ▼3.7kg" */
internal data class 보고줄(val 이름: String, val 값: String, val 차: Double?, val 단위: String)

/** 보고서 종목 칸 하나 — [번호] = 기록의 종목 차례(펼침 열쇠). [세트들] = 체크한 세트(워밍업 뺌) · [총] = 세트 칸 수 */
internal data class 보고칸(
    val 번호: Int, val 이름: String, val 종id: String?, val 플랜id: String?,
    val 세트들: List<세트>, val 총: Int, val 맨몸: Boolean, val 줄1: 보고줄, val 줄2: 보고줄,
)

/** 큰 운동 한 칸 — 맨 앞은 N대 합계. v = 이 기록까지 최고 추정 1RM · 앞 = 이 기록 앞까지 · 때 = 그 값을 처음 낸 날 */
internal data class 큰칸(val 글: String, val v: Double, val 앞: Double, val 때: String?)

internal data class 보고값(
    val 날: String, val 루틴이름: String, val 세트: Int, val 볼륨: Double, val 초: Int, val 루차: Double?, val 칸들: List<보고칸>,
)

internal data class 상세값(val 세트글: List<String>, val 최고글: String?, val rm: 두대비?, val 볼륨: 두대비?)

/** 기록 열쇠 차례 (시안 `키순`) — 날짜, 같은 날이면 '~2' '~3' … 숫자로 ("~10" 이 "~2" 뒤) */
internal fun 기록순(a: String, b: String): Int {
    val x = 날짜만(a); val y = 날짜만(b)
    if (x != y) return x.compareTo(y)
    fun n(k: String) = if ('~' in k) k.substringAfter('~').toIntOrNull() ?: 1 else 1
    return n(a).compareTo(n(b))
}

private fun 한자리(v: Double): Double = Math.round(v * 10) / 10.0
private fun 견줌차(a: Double, b: Double): Double? = if (b > 0) a - b else null
private fun 비교값(a: Double, b: Double?): 비교? = if (b == null || b <= 0.0) null else 비교(a, b, 퍼센트(a, b), a - b)

/** ▲3.7kg / ▼2회 — 0.05 보다 작으면 null (시안 `보고차`) */
internal fun 보고차글(d: Double?, 단위: String): String? {
    if (d == null || abs(d) < 0.05) return null
    return "${if (d > 0) "▲" else "▼"}${kg글(한자리(abs(d)))}$단위"
}

/** 줄이 가리키는 종목 — 종id 로 찾고 없으면 이름 (같은 이름 종목이 둘이어도 섞이지 않게) */
private fun 앱데이터.줄표준(종id: String?, 이름: String): String = 종목찾기(종id, 이름)?.id ?: 종목열쇠(종id, 이름)

/**
 * 같은 종목 줄인가 — **플랜 줄은 플랜id 로**(같은 종목 여러 플랜이 섞이지 않게), 그 밖은 종목 id 로.
 * 옛 줄(종id 없음)은 이름으로 찾은 종목의 id 로 견준다. 플랜 줄 ↔ 보통 줄은 섞지 않는다
 */
internal fun 앱데이터.같은줄(e: 종목기록, 종id: String?, 이름: String, 플랜id: String?): Boolean =
    if (플랜id != null || e.플랜id != null) e.플랜id == 플랜id else 줄표준(e.종id, e.이름) == 줄표준(종id, 이름)

/** 끝내기 전 세션을 기록 꼴로 (저장하지 않는다) — 빈 종목도 남겨 차례가 세션과 같다 */
internal fun 세션기록(S: 운동세션, 지금: Long): 날기록 {
    val 들 = S.종목들.map { e -> 종목기록(e.이름, e.찬것(), e.임시, S.묶음이름(e), 플랜id = e.플랜id, 종id = e.종id) }
    val 끝 = S.끝시각 ?: 지금
    val 걸린 = S.흐른초(지금)
    return 날기록(S.루틴id, S.루틴이름, S.한세트수() >= S.목표세트(), 들, 걸린.toInt(), 끝 - 걸린 * 1000, 끝)
}

/** 세션 종목마다 세트 칸 수(워밍업 칸 뺌) — 상세의 'm/n세트' */
internal fun 세션총칸(S: 운동세션): List<Int> = S.종목들.map { e: 세션종목 -> max(e.총칸() - e.웜칸수(), 기록세트(e.찬것()).size) }

/** 저장된 결과(App 의 `결과` 세션)가 가리키는 기록 열쇠 — 같은 루틴 · 같은 끝 시각, 여럿이면 뒤의 것 */
internal fun 저장열쇠(d: 앱데이터, S: 운동세션): String? =
    d.기록.entries.filter { (_, r) -> r.루틴id == S.루틴id && (S.끝시각 == null || r.끝시각 == S.끝시각) }
        .maxWithOrNull { a, b -> 기록순(a.key, b.key) }?.key

/**
 * 보고서 숫자 (시안 `결과뷰`).
 *  · 지난번 = 이 운동 바로 앞 기록 (저장된 보고서는 그 기록보다 앞만)
 *  · 루틴 볼륨 ▲▼ = 같은 루틴의 지난번, 지금 세트 수까지 잘라 견준다
 *  · 종목 칸 = 체크한 세트가 있는 종목. 1RM · 볼륨(맨몸은 최고 · 합계 횟수), 지난번 = 그 종목을 한 바로 앞 기록 (같은 줄 = [같은줄])
 *  · 워밍업 세트는 1RM · 볼륨 · 견주기에서 뺀다 (앱 규칙 · 시안에는 워밍업이 없다). 세트 수는 체크한 세트 전부
 */
internal fun 앱데이터.보고계산(rec: 날기록, 저장키: String?, 날: String, 총칸들: List<Int>? = null): 보고값 {
    val 앞 = 기록.entries.filter { (k, _) -> k != 저장키 && 날짜만(k) <= 날 && (저장키 == null || 기록순(k, 저장키) < 0) }
        .sortedWith { a, b -> 기록순(b.key, a.key) }
    val 전부 = rec.종목들.flatMap { it.세트들 }
    val n = 기록세트(전부).size
    val 루볼 = 볼륨(전부)
    val 루지 = 앞.firstOrNull { (_, r) -> r.루틴id == rec.루틴id && r.종목들.any { 기록세트(it.세트들).isNotEmpty() } }
    val 루차 = 루지?.let { 견줌차(루볼, 볼륨(기록세트(it.value.종목들.flatMap { e -> e.세트들 }).take(n))) }
    val 칸들 = rec.종목들.mapIndexedNotNull { i, e ->
        val 세 = 기록세트(e.세트들)
        if (세.isEmpty()) return@mapIndexedNotNull null
        val m = 세.size
        val 맨 = 세.all { it.w <= 0 }
        val 지난 = 앞.firstOrNull { (_, r) -> r.종목들.any { x -> 같은줄(x, e.종id, e.이름, e.플랜id) && 기록세트(x.세트들).isNotEmpty() } }
        val 전 = 지난?.value?.종목들?.filter { 같은줄(it, e.종id, e.이름, e.플랜id) }?.flatMap { 기록세트(it.세트들) } ?: emptyList()
        val 줄1: 보고줄
        val 줄2: 보고줄
        if (맨) {
            val 최회 = 세.maxOf { it.r }; val 합회 = 세.sumOf { it.r }
            줄1 = 보고줄("최고", "${최회}회", if (전.isEmpty()) null else (최회 - 전.maxOf { it.r }).toDouble(), "회")
            줄2 = 보고줄("합계", "${합회}회", if (전.isEmpty()) null else (합회 - 전.take(m).sumOf { it.r }).toDouble(), "회")
        } else {
            val rm = 세.maxOf { 일RM(it.w, it.r) }; val 볼 = 볼륨(세)
            줄1 = 보고줄("1RM", "${kg글(rm)}kg", if (전.isEmpty()) null else 견줌차(rm, 전.maxOf { 일RM(it.w, it.r) }), "kg")
            줄2 = 보고줄("볼륨", "${콤마(볼)}kg", if (전.isEmpty()) null else 견줌차(볼, 볼륨(전.take(m))), "kg")
        }
        보고칸(i, e.이름, e.종id, e.플랜id, 세, max(m, 총칸들?.getOrNull(i) ?: m), 맨, 줄1, 줄2)
    }
    return 보고값(날, rec.루틴이름, rec.종목들.sumOf { it.세트들.size }, 루볼, max(0, rec.걸린초), 루차, 칸들)
}

/**
 * 큰 운동 칸 (시안 `큰운동값`) — 앞 셋(스쿼트 · 벤치 · 데드) + 설정 `큰운동추가`(2개까지). 맨 앞은 'N대' 합계.
 * [rec] 가 없으면(프로필 탭) [날] 까지 저장된 기록만. 저장된 보고서([저장키])는 그 기록까지(앞 = 그 앞까지).
 * 기록의 이름 또는 그 줄이 가리키는 종목 이름이 표의 이름들에 들면 같은 것으로 센다. 워밍업은 뺀다
 */
internal fun 앱데이터.큰운동값(rec: 날기록?, 저장키: String?, 날: String): List<큰칸> {
    val 앞목록 = 기록.entries.filter { (k, _) -> if (저장키 != null) 기록순(k, 저장키) < 0 else 날짜만(k) <= 날 }
        .map { 날짜만(it.key) to it.value }
    val 끝목록 = if (rec != null) 앞목록 + (날 to rec) else 앞목록
    fun 맞음(e: 종목기록, 이름들: List<String>) = e.이름 in 이름들 || 종목찾기(e.종id, e.이름)?.이름 in 이름들
    fun 최고(목록: List<Pair<String, 날기록>>, 이름들: List<String>): Pair<Double, String?> {
        var v = 0.0; var 때: String? = null
        for ((dd, r) in 목록) for (e in r.종목들) if (맞음(e, 이름들)) for (x in 기록세트(e.세트들)) if (x.w > 0) {
            val m = 일RM(x.w, x.r)
            if (m > v || (m == v && 때 != null && dd < 때)) { v = m; 때 = dd }
        }
        return 한자리(v) to 때
    }
    val 추 = 큰운동추가정리(설정.큰운동추가)
    val 칸 = 큰운동표.filterIndexed { j, k -> j < 3 || k.키 in 추 }.map { k ->
        val 끝 = 최고(끝목록, k.이름들)
        큰칸(k.짧은, 끝.first, 최고(앞목록, k.이름들).first, 끝.second)
    }
    val 때들 = 칸.mapNotNull { it.때 }.sorted()
    return listOf(큰칸("${칸.size}대", 한자리(칸.sumOf { it.v }), 한자리(칸.sumOf { it.앞 }), 때들.lastOrNull())) + 칸
}

/** 펼친 상세 (시안 ya8r) — 세트 목록 · 최고 세트 · 1주 · 최고 대비 (그 기록 자신은 뺀다) */
internal fun 앱데이터.상세계산(칸: 보고칸, 저장키: String?, 날: String): 상세값 {
    val 과 = 기록.entries.filter { (k, _) -> k != 저장키 && 날짜만(k) <= 날 }
        .map { (k, r) -> k to r.종목들.filter { 같은줄(it, 칸.종id, 칸.이름, 칸.플랜id) }.flatMap { 기록세트(it.세트들) } }
        .filter { it.second.isNotEmpty() }
    val 주 = 과.filter { 날짜만(it.first) >= 날더하기(날, -7) && 날짜만(it.first) < 날 }
    val 세 = 칸.세트들
    val mx = 세.size
    fun rm(l: List<세트>) = l.maxOfOrNull { 일RM(it.w, it.r) } ?: 0.0
    val rmx = rm(세); val 볼x = 볼륨(세)
    val 좋 = 세.reduceOrNull { a, y -> if (일RM(y.w, y.r) > 일RM(a.w, a.r) || (y.w <= 0 && a.w <= 0 && y.r > a.r)) y else a }
    fun 세글(y: 세트) = "${if (y.w > 0) "${무게글(y.w)}kg × " else ""}${y.r}회"
    return 상세값(
        세.mapIndexed { k, y -> "${k + 1}세트 · ${세글(y)}" },
        좋?.let { "최고 세트 · ${세글(it)}" },
        if (rmx > 0) 두대비(rmx, 비교값(rmx, 주.maxOfOrNull { rm(it.second) }), 비교값(rmx, 과.maxOfOrNull { rm(it.second) })) else null,
        if (볼x > 0) 두대비(볼x, 비교값(볼x, 주.maxOfOrNull { 볼륨(it.second.take(mx)) }), 비교값(볼x, 과.maxOfOrNull { 볼륨(it.second.take(mx)) })) else null,
    )
}

/**
 * 플랜 줄 상세 끝 글 (시안 `플랜미리`) — **플랜id 로** 플랜을 찾는다.
 * 저장된 보고서: 'N회차 반영됨' (N = 지금 회차 − 이 기록 뒤에 그 플랜을 한 기록 수). 끝내기 전: 다음 회차와 견준 미리보기
 */
internal fun 앱데이터.플랜글(칸: 보고칸, 저장키: String?, 조절됨: Boolean): String? {
    val pid = 칸.플랜id ?: return null
    val p = 플랜들.firstOrNull { it.id == pid } ?: return null
    val 세 = 칸.세트들.filter { it.r > 0 }
    if (세.isEmpty()) return null
    if (저장키 != null) {
        val 뒤 = 기록.count { (k, r) -> 기록순(k, 저장키) > 0 && r.종목들.any { e -> e.플랜id == pid && e.세트들.any { it.종류 != 세트종류.워밍업 && it.r > 0 } } }
        val 회 = p.한회 - 뒤
        return if (회 > 0) "플랜 ${회}회차 반영됨" else "플랜 반영됨"
    }
    val 계 = p.다음회(몸, 향상기록들) ?: return "플랜 ${p.한회 + 1}회차"
    fun 값글(v: Double) = if (p.횟수진행) "${v.roundToInt()}${if (p.단위 == 목표단위.무게) "회" else p.단위.단위}" else "${무게글(Math.round(v * 2) / 2.0)}kg"
    val 처방 = 처방세트(회처방(p, 계.목표값, 설정.무게폭, 몸, 계.측정일, 계.주))
    val 실 = if (조절됨) null else 실제진행값(p, 계.목표값, 처방, 세, 몸)
    if (계.측정일) return "측정 ${값글(max(측정날값(p, 세, 몸) ?: 0.0, 실 ?: 0.0))}"
    if (실 == null || abs(실 - 계.목표값) <= 1e-6) return "플랜 ${계.회}회차 · 계획대로"
    return "플랜 ${계.회}회차 · ${if (실 < 계.목표값) "덜 함" else "더 함"} → ${값글(실)}에서 다시 계산"
}

/** 이미지 파일 이름 — 시안 `운동보고서_날.png` */
internal fun 보고파일이름(날: String): String = "운동보고서_${날짜만(날)}.png"

/** 띠 날짜 — "2026.10.05." */
internal fun 보고날글(날: String): String = 날짜만(날).split("-").joinToString(".") + "."

/** 상세 시트의 최고 기록 날짜 — 없으면 — */
internal fun 큰때글(때: String?): String = 때?.let { 보고날글(it) } ?: "—"

/** 상세 시트의 지금 값 — 합계는 반올림 정수, 칸은 kg글. 없으면 — (시안 `큰운동상세시트`) */
internal fun 큰값글(x: 큰칸, 합: Boolean): String = if (x.v <= 0) "—" else "${if (합) "${x.v.roundToInt()}" else kg글(x.v)}kg"

// ═════════════════════ 화면 — 들어가는 곳 ═════════════════════

/**
 * 운동 보고서 — 운동을 끝낸 화면(운동화면이 S.끝화면 일 때)과 저장된 뒤 다시 보는 [결과화면] 이 같이 쓴다.
 * 매개변수를 바꾸지 않는다 (운동화면 · App 이 부른다)
 */
@Composable
internal fun 마무리(상태: 앱상태, S: 운동세션, 저장됨: Boolean = false) {
    val d = 상태.d
    // 10-06 v22 D 13-1 — 보고서가 열리는 순간 저장한다(세션은 둔다). ‹ 로 돌아가 더 하고 다시 끝내면 같은 기록에 덮어쓴다.
    //   이번 끝내기(끝시각)를 이미 저장했으면 아무것도 안 한다 — 다시 그려도 · 앱을 껐다 켜도 한 번만 (data/Logic.kt 보고저장)
    if (!저장됨) {
        LaunchedEffect(S.시작시각, S.끝시각) {
            var 결과 = 보고저장결과.없음
            상태.바꿈 { dd ->
                val s = dd.세션
                if (s == null || !s.끝화면 || s.시작시각 != S.시작시각) return@바꿈 dd
                val (x, r) = dd.보고저장(상태.오늘, System.currentTimeMillis()); 결과 = r; x
            }
            when (결과) {
                보고저장결과.저장함, 보고저장결과.덮어씀 -> 상태.알림.토스트("저장했습니다")
                보고저장결과.체크없음 -> 상태.알림.토스트("체크한 세트가 없어 저장하지 않았습니다")
                else -> {}
            }
        }
    }
    // 끝내기 전(세션) 보고서도 방금 저장한 자기 기록이 있으면 그 열쇠로 견준다 — 자기 기록은 빼고 · 플랜은 '반영됨' (시안 v22 D)
    val 자기 = if (저장됨) null else d.저장기록열쇠(S)   // 10-10: 번호가 다시 매겨졌어도 끝 시각으로 찾는다
    val 저장키 = if (저장됨) remember(d.기록, S.루틴id, S.끝시각) { 저장열쇠(d, S) } else 자기
    val 날 = 저장키?.let { 날짜만(it) } ?: S.시작날(상태.오늘)   // 10-10: 시작한 날로 저장하므로 보고서도 그 날 기준
    // 저장된 결과를 보다가 그 기록이 지워져도 세션 값으로 그린다 · 끝내기 전은 늘 세션 값 (빈 종목도 차례대로)
    val rec = remember(S, 저장키, d.기록) { (if (저장됨) 저장키?.let { d.기록[it] } else null) ?: 세션기록(S, System.currentTimeMillis()) }
    val 총칸들 = remember(S, 저장됨) { if (!저장됨) 세션총칸(S) else null }
    // 10-08 홍겸 님: 저장된 뒤 보는 결과 화면(오래 손대지 않아 저절로 끝난 운동 등)도 [확인] 대신 떠 있는 ‹ (닫기) › (스테이터스)
    보고틀(상태, rec, 저장키, 날, 총칸들, 키 = if (저장됨) 저장키 ?: "결과${S.시작시각}" else "세션${S.시작시각}", 조절됨 = S.조절됨,
        떠있음 = if (저장됨) null else S, 기록닫기 = if (저장됨) ({ 상태.바꿈 { it.copy(결과 = null) } }) else null) {}
}

/** 저장된 뒤 한 번 보여 주는 결과 화면 (10-01) — App.kt 가 부른다 */
@Composable
fun 결과화면(상태: 앱상태, S: 운동세션) {
    Box(Modifier.fillMaxSize()) { 마무리(상태, S, 저장됨 = true) }
}

/**
 * 저장된 기록 하나의 보고서 (시안 캘린더 [운동 보고서] → `S.결과={key}`) — 캘린더가 화면을 덮어 띄운다.
 * 10-08 홍겸 님: 아래 [확인] 줄 대신 운동 끝 보고서와 같은 떠 있는 ‹ › — ‹ = [닫기] (캘린더로) · › = 스테이터스 화면.
 * 그 기록이 지워지면 [닫기]
 */
@Composable
fun 기록보고서(상태: 앱상태, 열쇠: String, 닫기: () -> Unit) {
    val rec = 상태.d.기록[열쇠]
    if (rec == null) { LaunchedEffect(열쇠) { 닫기() }; return }
    Box(Modifier.fillMaxSize().background(Local색.current.바탕).눌림 { }) {
        보고틀(상태, rec, 열쇠, 날짜만(열쇠), null, 키 = 열쇠, 조절됨 = false, 기록닫기 = 닫기) {}
    }
}

// ═════════════════════ 화면 — 틀 ═════════════════════

private sealed interface 보고시트 {
    data object 방식 : 보고시트
    data class 큰(val 글: String) : 보고시트
    data class 루틴(val 이름: String) : 보고시트
}

@Composable
private fun 보고틀(
    상태: 앱상태, rec: 날기록, 저장키: String?, 날: String, 총칸들: List<Int>?, 키: String, 조절됨: Boolean,
    떠있음: 운동세션? = null,
    기록닫기: (() -> Unit)? = null,   // 10-08: 캘린더로 연 기록 보고서 — 있으면 아래 줄 대신 떠 있는 ‹ (이것) › (스테이터스)
    아래: @Composable () -> Unit,
) {
    val 떠 = 떠있음 != null || 기록닫기 != null
    val c = Local색.current
    val d = 상태.d
    val ctx = LocalContext.current
    val 일꾼 = rememberCoroutineScope()
    val 값 = remember(d.기록, d.종목표, rec, 저장키, 날, 총칸들) { d.보고계산(rec, 저장키, 날, 총칸들) }
    val 큰 = remember(d.기록, d.종목표, d.설정.큰운동추가, rec, 저장키, 날) { d.큰운동값(rec, 저장키, 날) }
    var 펼침 by remember(키) { mutableStateOf<Int?>(null) }
    val 펼친것 = 펼침?.takeIf { n -> 값.칸들.any { it.번호 == n } }   // 그 칸이 사라지면 접힌 것으로
    var 시트 by remember { mutableStateOf<보고시트?>(null) }
    val 넘김 = remember(키) { ScrollState(0) }
    // 들어올 때 — 띠는 위에서 아래로 1.6초, 상자 · 칸은 아래에서 위로 0.48초 (v17 B 1). 보고서마다 한 번
    val 띠p = remember(키) { Animatable(0f) }
    val 상자p = remember(키) { Animatable(0f) }
    LaunchedEffect(키) {
        launch { 띠p.animateTo(1f, tween(보고치수.띠들어옴, easing = 띠곡선)) }
        상자p.animateTo(1f, tween(보고치수.상자들어옴, easing = 올라옴곡선))
    }

    // 프로필 사진 고르기 (시안 mxxq — 동그라미를 누르면). 사진은 사진함(filesDir/photos)에, 설정에는 이름만
    var 고르는중 by remember { mutableStateOf(false) }
    val 고르기 = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) { 고르는중 = false; return@rememberLauncherForActivityResult }
        일꾼.launch {
            val 이름 = withContext(Dispatchers.IO) { 사진함.넣기(ctx, listOf(uri)).firstOrNull() }
            고르는중 = false
            if (이름 == null) { 상태.알림.토스트("이 사진은 읽지 못했습니다"); return@launch }
            val 옛 = 상태.d.설정.프로필사진
            상태.바꿈 { it.copy(설정 = it.설정.copy(프로필사진 = 이름)) }
            if (옛 != null && 옛 != 이름) withContext(Dispatchers.IO) { 사진함.지우기(ctx, 옛) }
        }
    }
    val 사진고름 = {
        if (!고르는중) {
            고르는중 = true
            try { 고르기.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) } catch (_: Exception) { 고르는중 = false }
        }
    }

    // 이미지 저장 · 공유 (v19 B) — 화면 밖 복제본(스크롤 없이 종목 칸 전부 · 단추 · 톱니 없이)을 한 장으로 찍는다
    var 찍기 by remember { mutableStateOf<String?>(null) }   // "저장" · "공유"
    var 폭 by remember { mutableIntStateOf(0) }
    val 층 = rememberGraphicsLayer()

    Box(Modifier.fillMaxSize()) {
        val 할 = 찍기
        if (할 != null && 폭 > 0) {
            val 폭dp = with(LocalDensity.current) { 폭.toDp() }
            // 층에 기록만 하고 화면에는 그리지 않는다 (본문 아래 · 누름 · 읽어 주기 없음). 높이 제한 없이 · 움직임 없이 끝난 값으로
            Box(Modifier.fillMaxSize().clearAndSetSemantics { }) {
                Column(
                    Modifier.wrapContentSize(Alignment.TopStart, unbounded = true).requiredWidth(폭dp)
                        .drawWithContent { 층.record { this@drawWithContent.drawContent() } }   // 기록만 — 화면에는 그리지 않는다
                        .background(c.바탕)
                        .padding(start = 간격.보통, end = 간격.보통, top = 간격.보통, bottom = 간격.보통),
                    verticalArrangement = Arrangement.spacedBy(간격.좁게),
                ) {
                    보고본문(
                        상태, 값, 큰, 키, 저장키, 날, 조절됨, 찍는용 = true, 펼침 = 펼친것, on펼침 = {}, 넘김 = null,
                        띠p = 하나, 상자p = 하나, 찍는중 = false, on찍기 = {}, on시트 = {}, on사진 = {},
                    )
                }
            }
            LaunchedEffect(할) {
                // 복제본이 그려질 때까지 (몇 장면)
                var n = 0
                do { withFrameNanos { }; n++ } while ((층.size.width == 0 || n < 2) && n < 30)
                val 이름 = 보고파일이름(날)
                val 결과: String? = try {
                    if (층.size.width == 0) error("못 그림")
                    val 그림 = 층.toImageBitmap().asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, false)
                    withContext(NonCancellable + Dispatchers.IO) {
                        if (할 == "저장" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            if (갤러리저장(ctx, 그림, 이름)) "저장됨" else null
                        } else 보고캐시(ctx, 그림, 이름).path
                    }
                } catch (e: CancellationException) { throw e } catch (_: Throwable) { null }
                when {
                    결과 == null -> 상태.알림.토스트("이미지를 만들지 못했습니다")
                    결과 == "저장됨" -> 상태.알림.토스트("저장했습니다")
                    else -> {
                        // 공유 — 또는 API 29 아래(권한 없이 갤러리에 못 넣는 폰)의 저장은 공유 창으로 대신
                        if (할 == "저장") 상태.알림.토스트("이 기기에서는 저장 대신 공유합니다")
                        if (!공유열기(ctx, File(결과))) 상태.알림.토스트("공유할 앱이 없습니다")
                    }
                }
                찍기 = null
            }
        }

        Column(Modifier.fillMaxSize().background(c.바탕)) {
            Column(
                Modifier.weight(1f).fillMaxWidth().onSizeChanged { 폭 = it.width }
                    .padding(start = 간격.보통, end = 간격.보통, top = 간격.보통, bottom = 간격.좁게),
                verticalArrangement = Arrangement.spacedBy(간격.좁게),
            ) {
                보고본문(
                    상태, 값, 큰, 키, 저장키, 날, 조절됨, 찍는용 = false, 펼침 = 펼친것,
                    on펼침 = { n -> 펼침 = if (펼친것 == n) null else n },
                    넘김 = 넘김, 띠p = { 띠p.value }, 상자p = { 상자p.value }, 찍는중 = 찍기 != null,
                    on찍기 = { 무엇 -> if (찍기 == null && 폭 > 0) 찍기 = 무엇 }, on시트 = { 시트 = it }, on사진 = 사진고름,
                    // 10-06 v22 D 13-3 — 목록 끝이 떠 있는 ‹ › 에 가리지 않게 (단추 30 + 아래 12)
                    아래여백 = if (떠) 보고떠값.지름 + 보고떠값.옆 else 0.dp,
                )
            }
            // 저장된 보고서만 아래 [확인] 줄. 끝내기 전 보고서는 단추 셋을 없애고 떠 있는 ‹ › (10-06 v22 D 13-2)
            if (!떠) Column(
                Modifier.fillMaxWidth().background(c.면)
                    .drawBehind { drawRect(c.선, size = Size(size.width, 선굵기.보통.toPx())) }
                    .padding(horizontal = 간격.보통, vertical = 간격.좁게),
            ) { 아래() }
        }
        if (떠있음 != null) 보고떠단추(상태, 떠있음) else if (기록닫기 != null) 기록떠단추(상태, 기록닫기)

        when (val s = 시트) {
            null -> {}
            보고시트.방식 -> 보고방식시트(상태, { 시트 = null })
            is 보고시트.큰 -> 큰운동상세시트(s.글, 큰) { 시트 = null }
            is 보고시트.루틴 -> 루틴상세시트(s.이름) { 시트 = null }
        }
    }
}

// ═════════════════════ 화면 — 본문 (화면과 이미지가 같이 쓴다) ═════════════════════

/** 들어옴 — 그릴 때만 값을 읽는다 (움직이는 동안 화면 전체를 다시 짜지 않게) */
private fun Modifier.들어옴(p: () -> Float, 위에서: Boolean): Modifier =
    graphicsLayer { val v = p(); alpha = v; translationY = (if (위에서) -1f else 1f) * 보고치수.들어옴거리.toPx() * (1f - v) }

/** 상자 공통 — 1px 선 · 모서리 8 (시안 `.보고상자` · `.보고칸`) */
private fun Modifier.보고상자(바탕: Color, 테: Color): Modifier = this
    .clip(RoundedCornerShape(모서리.작게))
    .background(바탕)
    .border(선굵기.보통, 테, RoundedCornerShape(모서리.작게))

/** 칸 사이 세로선 (시안 `.결과수>div+div{border-left}`) */
private fun Modifier.세로선(켬: Boolean, 색: Color): Modifier =
    if (!켬) this else drawBehind { drawLine(색, Offset(0f, 0f), Offset(0f, size.height), 선굵기.보통.toPx()) }

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ColumnScope.보고본문(
    상태: 앱상태, 값: 보고값, 큰: List<큰칸>, 키: String, 저장키: String?, 날: String, 조절됨: Boolean,
    찍는용: Boolean, 펼침: Int?, on펼침: (Int) -> Unit, 넘김: ScrollState?,
    띠p: () -> Float, 상자p: () -> Float, 찍는중: Boolean,
    on찍기: (String) -> Unit, on시트: (보고시트) -> Unit, on사진: () -> Unit,
    아래여백: Dp = 0.dp,
) {
    val c = Local색.current
    val d = 상태.d
    val 보임 = d.설정.보고서보임
    val 움직 = !찍는용

    // ── 띠 '운동 보고서' + 날짜 · 카메라 · 공유 ──
    Box(
        Modifier.fillMaxWidth().heightIn(min = 보고치수.띠높이).들어옴(띠p, 위에서 = true)
            .clip(RoundedCornerShape(모서리.작게)).background(c.강조),
        contentAlignment = Alignment.Center,
    ) {
        // 제목은 가운데, 날짜는 제목 오른쪽 끝 + 15 · 위끝 + 15 (가운데 맞춤은 제목만으로)
        Layout(content = {
            Text("운동 보고서", style = 글꼴.보통(크기.크게, FontWeight.Bold), color = c.강조글, maxLines = 1, softWrap = false)
            Text(보고날글(날), style = 글꼴.보통(크기.작게), color = c.강조글, maxLines = 1, softWrap = false)
        }) { ms, _ ->
            val 제목 = ms[0].measure(Constraints())
            val 날짜 = ms[1].measure(Constraints())
            layout(제목.width, 제목.height) {
                제목.place(0, 0)
                날짜.place(제목.width + 보고치수.날짜옆.roundToPx(), 보고치수.날짜위.roundToPx())
            }
        }
        if (!찍는용) {
            찍기단추(아이콘.카메라, "보고서를 이미지로 저장", 찍는중, Modifier.align(Alignment.CenterStart)) { on찍기("저장") }
            찍기단추(아이콘.공유, "보고서 이미지 공유", 찍는중, Modifier.align(Alignment.CenterEnd)) { on찍기("공유") }
        }
    }

    // ── 프로필 상자 (v10 · 끄면 톱니가 루틴 상자로) ──
    if (보임.프로필) {
        Box(Modifier.fillMaxWidth().들어옴(상자p, false).보고상자(c.면, c.선)) {
            Row(
                Modifier.fillMaxWidth().padding(간격.보통),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.보통),
            ) {
                Column(Modifier.width(보고치수.사진칸), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                    프로필동그라미(
                        d.설정, 보고치수.사진, 그림 = 보고치수.사진그림,
                        modifier = if (찍는용) Modifier else Modifier.눌림(on사진).semantics { contentDescription = "프로필 사진 고르기"; role = Role.Button },
                    )
                    val 닉 = d.설정.닉네임.trim()
                    Text(
                        닉.ifEmpty { "닉네임" }, style = 글꼴.보통(크기.작게, FontWeight.Bold), color = if (닉.isEmpty()) c.옅음 else c.글,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
                큰운동판(큰, 차보임 = true, 톱니비킴 = true, on누름 = if (찍는용) null else ({ 글: String -> on시트(보고시트.큰(글)) }), modifier = Modifier.weight(1f), 움직 = 움직)
            }
            if (!찍는용) 톱니단추(Modifier.align(Alignment.TopEnd)) { on시트(보고시트.방식) }
        }
    }

    // ── 루틴 상자 — 늘 보인다 (v18 B 2). 누르면 루틴 상세(틀만) ──
    // 10-06 v23 ② · v24 ① 한 줄 [루틴 이름 | 세트 | 총 볼륨 | 운동 시간]. 이름 칸 = 이름 글자(13 Bold) 7자 + 오른쪽 8, 더 길면 … ·
    //   볼륨 차이는 이름 아래 작게 · 이름 칸과 수치 칸 사이도 수치 칸 사이와 같은 세로선 · 위아래 안 여백 4 · 좌우 8 ·
    //   프로필을 꺼서 톱니가 여기 오면 오른쪽 32
    val 루틴이름 = 루틴표시(값.루틴이름)
    val 톱니여기 = !보임.프로필 && !찍는용
    val 이름폭 = with(LocalDensity.current) { (크기.조금작게 * 보고떠값.이름칸글자).toDp() } + 보고떠값.이름칸뒤
    Box(
        Modifier.fillMaxWidth().들어옴(상자p, false).보고상자(c.면2, c.선)
            .then(if (찍는용) Modifier else Modifier.눌림 { on시트(보고시트.루틴(루틴이름)) }.semantics { contentDescription = "$루틴이름 상세"; role = Role.Button }),
    ) {
        Row(
            Modifier.fillMaxWidth().height(IntrinsicSize.Min)
                .padding(start = 보고떠값.루틴옆, end = if (!보임.프로필) 보고떠값.톱니있음옆 else 보고떠값.루틴옆, top = 보고떠값.루틴위아래, bottom = 보고떠값.루틴위아래),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.width(이름폭).padding(end = 보고떠값.이름칸뒤)) {
                Text(루틴이름, style = 글꼴.보통(크기.조금작게, FontWeight.Bold), color = c.글, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (보고차글(값.루차, "kg") != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                        Text("볼륨", style = 글꼴.보통(크기.작게), color = c.흐림, maxLines = 1)
                        보고차(값.루차, "kg", 움직, 키)
                    }
                }
            }
            // 세트 | 총 볼륨 | 운동 시간 — 0 부터 올라간다 (D5-9)
            val 세트 = if (움직) 움직수(값.세트.toDouble(), true, 열쇠 = 키) else 값.세트.toDouble()
            val 볼 = if (움직) 움직수(값.볼륨, true, 열쇠 = 키) else 값.볼륨
            val 초 = if (움직) 움직수(값.초.toDouble(), true, 열쇠 = 키) else 값.초.toDouble()
            listOf("${세트.roundToInt()}" to "세트", "${콤마(볼)}kg" to "총 볼륨", 분초(초.roundToInt()) to "운동 시간").forEach { (v, 이름) ->
                Column(
                    Modifier.weight(1f).fillMaxHeight().세로선(true, c.선),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                ) {
                    Text(v, style = 글꼴.보통(크기.본문, FontWeight.Bold), color = c.글, maxLines = 1, softWrap = false)
                    Text(이름, style = 글꼴.보통(크기.작게), color = c.흐림, maxLines = 1)
                }
            }
        }
        if (톱니여기) 톱니단추(Modifier.align(Alignment.TopEnd)) { on시트(보고시트.방식) }
    }

    // ── 종목 두 칸 격자 — 이 안에서만 넘긴다. 누르면 그 줄 아래 두 칸 폭으로 상세 (한 번에 하나) ──
    Column(
        (if (넘김 != null) Modifier.weight(1f).verticalScroll(넘김) else Modifier).fillMaxWidth().padding(bottom = 아래여백),
        verticalArrangement = Arrangement.spacedBy(간격.아주좁게),
    ) {
        if (값.칸들.isEmpty()) {
            Text("체크한 세트가 없습니다", Modifier.padding(horizontal = 간격.아주좁게, vertical = 간격.좁게), style = 글꼴.보통(크기.조금작게), color = c.옅음)
        }
        값.칸들.chunked(2).forEach { 줄 -> key(줄.first().번호) {
            val 열린 = 줄.firstOrNull { it.번호 == 펼침 }
            val 보기 = remember { BringIntoViewRequester() }
            Column(Modifier.fillMaxWidth().bringIntoViewRequester(보기), verticalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                    줄.forEach { k -> 종목칸(d, k, k.번호 == 펼침, 움직, 키, 찍는용, 상자p, Modifier.weight(1f)) { on펼침(k.번호) } }
                    if (줄.size == 1) Box(Modifier.weight(1f))
                }
                if (열린 != null) {
                    // 상세는 옅게 → 진하게. 다 그린 뒤 그 줄 + 상세가 보이게 올린다 (시안 ya8r — 움직임 중에 자리를 재지 않는다)
                    val p = remember(열린.번호) { Animatable(if (찍는용) 1f else 0f) }
                    LaunchedEffect(열린.번호) {
                        if (!찍는용) {
                            withFrameNanos { }
                            try { 보기.bringIntoView() } catch (e: CancellationException) { throw e } catch (_: Exception) { }
                            p.animateTo(1f, tween(보고치수.상세들어옴, easing = 올라옴곡선))
                        }
                    }
                    종목상세(d, 열린, 저장키, 날, 조절됨, 움직, 키, Modifier.들어옴({ p.value }, false))
                }
            }
        } }
    }
}

@Composable
private fun 찍기단추(그림: ImageVector, 설명: String, 막힘: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val c = Local색.current
    Box(
        modifier.size(보고치수.찍기폭, 높이.보통).graphicsLayer { alpha = if (막힘) 보고치수.찍는중흐림 else 1f }
            .눌림 { if (!막힘) onClick() }.semantics { contentDescription = 설명; role = Role.Button },
        contentAlignment = Alignment.Center,
    ) { Icon(그림, null, Modifier.size(18.dp), tint = c.강조글) }
}

@Composable
private fun 톱니단추(modifier: Modifier, onClick: () -> Unit) {
    val c = Local색.current
    Box(
        modifier.size(보고치수.톱니칸).눌림(onClick).semantics { contentDescription = "결과 보고서 표시 방법"; role = Role.Button },
        contentAlignment = Alignment.Center,
    ) { Icon(아이콘.톱니, null, Modifier.size(보고치수.톱니그림), tint = c.흐림) }
}

/** ▲3.7kg (오름) / ▼2kg (내림) — 숫자는 0 부터 올라간다 (시안 `보고차` · `올림수`) */
@Composable
private fun 보고차(d: Double?, 단위: String, 움직: Boolean, 키: Any?, 크기값: TextUnit = 크기.작게) {
    if (d == null || 보고차글(d, 단위) == null) return
    val c = Local색.current
    val a = 한자리(abs(d))
    val 보임 = if (움직) 움직수(a, true, 열쇠 = 키) else a
    Text(
        "${if (d > 0) "▲" else "▼"}${kg글(한자리(보임))}$단위", style = 글꼴.보통(크기값, FontWeight.Bold),
        color = if (d > 0) c.오름 else c.내림, maxLines = 1, softWrap = false,
    )
}

/** 종목 칸 (시안 `.보고칸`) — 이름 [번호][플랜] · 두 줄. 펼치면 강조 테두리 · 강조옅음 바탕 */
@Composable
private fun 종목칸(d: 앱데이터, k: 보고칸, 열림: Boolean, 움직: Boolean, 키: String, 찍는용: Boolean, 상자p: () -> Float, modifier: Modifier, onClick: () -> Unit) {
    val c = Local색.current
    Column(
        modifier.들어옴(상자p, false).보고상자(if (열림) c.강조옅음 else c.면, if (열림) c.강조 else c.선)
            .then(if (찍는용) Modifier else Modifier.눌림(onClick).semantics { contentDescription = "${k.이름} ${if (열림) "접기" else "펼치기"}"; role = Role.Button })
            .padding(간격.좁게),
        verticalArrangement = Arrangement.spacedBy(간격.아주좁게),
    ) {
        이름딱지(k.이름, 번호 = d.같은이름번호(k.종id, k.이름), 플랜 = k.플랜id != null, 크기값 = 크기.조금작게)
        listOf(k.줄1, k.줄2).forEach { l ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontSize = 크기.작게, color = c.흐림)) { append(l.이름) }
                        append(" "); append(l.값)
                    },
                    Modifier.weight(1f), style = 글꼴.보통(크기.조금작게), color = c.글, maxLines = 1, softWrap = false, overflow = TextOverflow.Clip,
                )
                보고차(l.차, l.단위, 움직, 키)
            }
        }
    }
}

/** 펼친 상세 (시안 `.보고상세`) — 이름 · m/n세트 · 세트(세로 먼저 두 칸) · 최고 세트 · 1주/최고 대비 · 플랜 */
@Composable
private fun 종목상세(d: 앱데이터, k: 보고칸, 저장키: String?, 날: String, 조절됨: Boolean, 움직: Boolean, 키: String, modifier: Modifier) {
    val c = Local색.current
    val 상 = remember(d.기록, d.종목표, k, 저장키, 날) { d.상세계산(k, 저장키, 날) }
    val 플글 = remember(d.플랜들, d.기록, k, 저장키, 조절됨) { d.플랜글(k, 저장키, 조절됨) }
    Column(
        modifier.fillMaxWidth().clip(RoundedCornerShape(모서리.작게)).background(c.면2).padding(horizontal = 간격.보통, vertical = 간격.좁게),
        verticalArrangement = Arrangement.spacedBy(간격.아주좁게),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            이름딱지(k.이름, Modifier.weight(1f, fill = false), 번호 = d.같은이름번호(k.종id, k.이름), 플랜 = k.플랜id != null, 크기값 = 크기.본문)
            Box(Modifier.weight(1f))
            Text("${k.세트들.size}/${k.총}세트", style = 글꼴.보통(크기.조금작게), color = c.흐림, maxLines = 1)
        }
        // 세트 — 왼쪽 칸 위→아래 먼저 (v10 oaee: 1,2,3 | 4,5)
        val 행 = ceil(상.세트글.size / 2.0).toInt()
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
            listOf(상.세트글.take(행), 상.세트글.drop(행)).forEach { 칸 ->
                Column(Modifier.weight(1f)) {
                    칸.forEach { Text(it, style = 글꼴.보통(크기.조금작게), color = c.글, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                }
            }
        }
        상.최고글?.let { Text(it, style = 글꼴.보통(크기.조금작게), color = c.글, maxLines = 1) }
        상.rm?.let { 향상줄("1RM ${kg글(it.지금)}kg", it, 영부터 = 움직, 열쇠 = 키) }
        상.볼륨?.let { 향상줄("볼륨 ${콤마(it.지금)}kg", it, 영부터 = 움직, 열쇠 = 키) }
        플글?.let { Text(it, style = 글꼴.보통(크기.작게), color = c.흐림, maxLines = 2, overflow = TextOverflow.Ellipsis) }
    }
}

// ═════════════════════ 큰 운동 칸 (보고서 · 프로필 탭이 같이 쓴다) ═════════════════════

/**
 * 큰 운동 판 (시안 `큰운동판`) — 위 파란 상자 'N대 Nkg ▲n'(반올림 정수), 아래 칸들(숫자 위 · 이름 아래 · 사이 세로선).
 * 칸 수와 상관없이 **늘 한 줄**. 숫자 18(3~4칸) · 15(5칸), 넘치면 한 줄 전체를 15 → 13 → 11 → 자간 좁힘 (시안 `큰값맞춤`).
 * [차보임] = 숫자 옆 ▲▼(단위 없이, 칸이 좁으면 숫자 아래). [톱니비킴] = 오른쪽 위 톱니 자리(24)를 비움.
 * [on누름] 이 있으면 파란 상자 · 칸을 누르면 '○○ 상세'
 */
@Composable
internal fun 큰운동판(칸: List<큰칸>, 차보임: Boolean, 톱니비킴: Boolean, on누름: ((String) -> Unit)?, modifier: Modifier = Modifier, 움직: Boolean = true) {
    val c = Local색.current
    val 합 = 칸.firstOrNull() ?: return
    val 들 = 칸.drop(1)
    val 기본 = if (들.size >= 5) 크기.본문 else 크기.크게
    val 단계들 = listOf(기본) + listOf(크기.본문, 크기.조금작게, 크기.작게).filter { it.value < 기본.value }
    val 판열쇠 = 들.map { it.v.roundToInt() }
    var 단계 by remember(판열쇠) { mutableIntStateOf(0) }
    var 좁힘 by remember(판열쇠) { mutableStateOf(false) }
    val 이번단계 = 단계.coerceIn(0, 단계들.lastIndex)
    fun 누름(글: String): Modifier = if (on누름 == null) Modifier else Modifier.눌림 { on누름(글) }.semantics { contentDescription = "$글 상세"; role = Role.Button }
    Column(
        modifier.padding(end = if (톱니비킴) 보고치수.톱니비킴 else 0.dp),
        verticalArrangement = Arrangement.spacedBy(간격.아주좁게),
    ) {
        // 파란 상자 — 너비 = (칸 줄 − 좌우 15) × 93.5%, 가운데
        Box(Modifier.fillMaxWidth().padding(horizontal = 보고치수.큰합옆), contentAlignment = Alignment.Center) {
            Row(
                Modifier.fillMaxWidth(보고치수.큰합비율).height(높이.아주낮게).clip(RoundedCornerShape(모서리.작게)).background(c.강조).then(누름(합.글)),
                horizontalArrangement = Arrangement.spacedBy(간격.아주좁게, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically,
            ) {
                val 합수 = 합.v.roundToInt()
                val 합차 = 합.v.roundToInt() - 합.앞.roundToInt()
                Text(
                    buildAnnotatedString {
                        append("${합.글} ")
                        if (합.v > 0) { append("$합수"); withStyle(SpanStyle(fontSize = 크기.작게)) { append("kg") } } else append("—")
                    },
                    style = 글꼴.보통(크기.조금작게, FontWeight.Bold), color = c.강조글, maxLines = 1, softWrap = false,
                )
                if (차보임 && 합.v > 0 && 합.앞 > 0 && 합차 != 0) {
                    Text("${if (합차 > 0) "▲" else "▼"}${abs(합차)}", style = 글꼴.보통(크기.작게, FontWeight.Bold), color = c.강조글, maxLines = 1, softWrap = false)
                }
            }
        }
        Row(Modifier.fillMaxWidth()) {
            val 옆 = if (들.size >= 5) 보고치수.좁은칸옆 else 간격.아주좁게
            들.forEachIndexed { i, x ->
                Column(
                    Modifier.weight(1f).세로선(i > 0, c.선).then(누름(x.글)).padding(horizontal = 옆),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    옆아니면아래 {
                        Text(
                            if (x.v > 0) "${x.v.roundToInt()}" else "—",
                            style = 글꼴.보통(단계들[이번단계], FontWeight.Bold).let { if (좁힘) it.copy(letterSpacing = 보고치수.자간끝.em) else it },
                            color = c.글, maxLines = 1, softWrap = false,
                            onTextLayout = { r ->
                                // 한 칸이라도 넘치면 줄 전체를 한 단계 줄인다 (같은 장면에 여러 칸이 넘쳐도 한 단계만)
                                if (r.hasVisualOverflow) {
                                    if (이번단계 < 단계들.lastIndex) 단계 = max(단계, 이번단계 + 1) else if (!좁힘) 좁힘 = true
                                }
                            },
                        )
                        if (차보임 && x.v > 0 && x.앞 > 0) 보고차((x.v.roundToInt() - x.앞.roundToInt()).toDouble(), "", 움직, null)
                    }
                    Text(x.글, style = 글꼴.보통(크기.작게), color = c.흐림, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

/** 둘이 들어가면 옆에(밑줄 맞춤 · 틈 4), 아니면 아래로 — 가운데 (시안 `.큰값{flex-wrap}`) */
@Composable
private fun 옆아니면아래(content: @Composable () -> Unit) {
    Layout(content) { ms, cs ->
        val 풂 = cs.copy(minWidth = 0, minHeight = 0)
        val ps = ms.map { it.measure(풂) }
        if (ps.size < 2) {
            val p = ps.firstOrNull()
            return@Layout layout(p?.width ?: 0, p?.height ?: 0) { p?.place(0, 0) }
        }
        val a = ps[0]; val b = ps[1]
        val 틈 = 간격.아주좁게.roundToPx()
        if (a.width + 틈 + b.width <= cs.maxWidth) {
            fun 밑(p: Placeable) = p[FirstBaseline].let { if (it == AlignmentLine.Unspecified) p.height else it }
            val ab = 밑(a); val bb = 밑(b)
            val 위 = max(ab, bb)
            val h = max(위 - ab + a.height, 위 - bb + b.height)
            layout(a.width + 틈 + b.width, h) { a.place(0, 위 - ab); b.place(a.width + 틈, 위 - bb) }
        } else {
            val w = max(a.width, b.width).coerceAtMost(cs.maxWidth)
            layout(w, a.height + b.height) { a.place((w - a.width) / 2, 0); b.place((w - b.width) / 2, a.height) }
        }
    }
}

// ═════════════════════ 아래 단추 ═════════════════════

/**
 * 10-06 v22 D 13-3 · v23 ③ — 끝내기 전 보고서의 떠 있는 동그라미. 화면 기준이라 목록을 넘겨도 제자리 (이미지에는 안 나온다 — 찍는 복제본 밖).
 *  · 왼쪽 아래 ‹ = 운동으로 돌아가기 (저장은 그대로 — 다시 끝내면 같은 기록에 덮어쓴다)
 *  · 오른쪽 아래 › = 스테이터스 화면 (보고서 위에 · ‹ 로 돌아온다 · 10-07)
 * 세션이 이미 바뀌었으면(두 번 빨리 누름 · 다른 곳에서 끝남) 아무것도 하지 않는다
 */
@Composable
private fun BoxScope.보고떠단추(상태: 앱상태, S: 운동세션) {
    fun 지금이면(f: () -> Unit) { val ss = 상태.d.세션; if (ss != null && ss.끝화면 && ss.시작시각 == S.시작시각) f() }
    떠동그라미(보고화살왼, "운동으로 돌아가기", Modifier.align(Alignment.BottomStart).padding(start = 보고떠값.옆, bottom = 보고떠값.옆)) {
        지금이면 { 상태.바꿈 { dd -> dd.copy(세션 = dd.세션?.재개(System.currentTimeMillis())) } }
    }
    // 10-07 홍겸 님: › = 스테이터스 화면을 보고서 위에 띄운다 (세션은 닫지 않는다 — 스테이터스의 ‹ 로 이 보고서에 돌아온다.
    //        이미 저장돼 있고, 탭을 누르면 App 이 전처럼 세션을 닫는다)
    떠동그라미(보고화살오른, "스테이터스 보기", Modifier.align(Alignment.BottomEnd).padding(end = 보고떠값.옆, bottom = 보고떠값.옆)) {
        지금이면 { 상태.스탯열기?.invoke() }
    }
}

/**
 * 10-08 홍겸 님 — 캘린더 [운동 보고서] 로 연 기록 보고서의 떠 있는 동그라미 (운동 끝 보고서와 같은 모양 · 같은 자리).
 *  · 왼쪽 아래 ‹ = 보고서 닫기 (캘린더로)
 *  · 오른쪽 아래 › = 스테이터스 화면 (운동 끝 보고서의 › 와 같은 [스탯열기] · 스테이터스의 ‹ 로 이 보고서에 돌아온다)
 */
@Composable
private fun BoxScope.기록떠단추(상태: 앱상태, 닫기: () -> Unit) {
    떠동그라미(보고화살왼, "보고서 닫기", Modifier.align(Alignment.BottomStart).padding(start = 보고떠값.옆, bottom = 보고떠값.옆)) {
        발자취.적기("기록 보고서 닫기"); 닫기()
    }
    떠동그라미(보고화살오른, "스테이터스 보기", Modifier.align(Alignment.BottomEnd).padding(end = 보고떠값.옆, bottom = 보고떠값.옆)) {
        발자취.적기("기록 보고서 · 스테이터스 보기"); 상태.스탯열기?.invoke()
    }
}

/** 10-07: 스테이터스 · 도전 과제 화면(보고서에서 들어왔을 때)도 보고서와 같은 떠 있는 ‹ › 를 쓴다. [왼] = ‹ */
@Composable
internal fun 보고떠동그라미(왼: Boolean, 설명: String, modifier: Modifier, onClick: () -> Unit) =
    떠동그라미(if (왼) 보고화살왼 else 보고화살오른, 설명, modifier, onClick)

/** 지름 30 · 면 바탕 · 속선 테 1 · 화살표 13.5 굵게(선 3) — 시안 `.화면>.보고떠` */
@Composable
private fun 떠동그라미(그림: ImageVector, 설명: String, modifier: Modifier, onClick: () -> Unit) {
    val c = Local색.current
    Box(
        modifier.size(보고떠값.지름).clip(CircleShape).background(c.면).border(선굵기.보통, c.속선, CircleShape)
            .눌림(onClick).semantics { contentDescription = 설명; role = Role.Button },
        contentAlignment = Alignment.Center,
    ) { Icon(그림, null, Modifier.size(보고떠값.화살), tint = c.글) }
}

/** 굵은 ‹ › (시안 `칩화살그림` 의 길 · 선 3) — Icons.kt 의 칩화살은 선 2 라 여기 따로 */
private fun 굵은화살(이름: String, 길: String): ImageVector = ImageVector.Builder(이름, 24.dp, 24.dp, 24f, 24f).addPath(
    pathData = addPathNodes(길), fill = null, stroke = SolidColor(Color.Black),
    strokeLineWidth = 보고떠값.화살선, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
).build()
private val 보고화살왼 = 굵은화살("reportL", "M15 6l-6 6 6 6")
private val 보고화살오른 = 굵은화살("reportR", "M9 6l6 6-6 6")

// ═════════════════════ 시트 ═════════════════════

/**
 * 결과 보고서 표시 방법 (시안 ua0b `보고방식시트`) — 보일 것: 프로필 스위치(루틴은 늘 보임 · v18 B 2) ·
 * 큰 운동: 스쿼트 · 벤치 · 데드 늘 켜짐 + 오버헤드 프레스 · 바벨 로우 · 스내치 · 클린 앤 저크 중 2개까지.
 * [아래] — 프로필 탭이 업적 정렬 같은 것을 덧붙일 자리 (시안 v17 ②)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun 보고방식시트(상태: 앱상태, 닫기: () -> Unit, 아래: @Composable ColumnScope.() -> Unit = {}) {
    val c = Local색.current
    val s = 상태.d.설정
    val 추가 = 큰운동추가정리(s.큰운동추가)
    시트("결과 보고서 표시 방법", 닫기) {
        이름표("보일 것")
        설정줄("프로필") {
            스위치(s.보고서보임.프로필) { v -> 상태.바꿈 { it.copy(설정 = it.설정.copy(보고서보임 = it.설정.보고서보임.copy(프로필 = v, 루틴 = true))) } }
        }
        Box(Modifier.height(간격.좁게))
        이름표("큰 운동 · ${3 + 추가.size}대")
        Box(Modifier.height(간격.좁게))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(간격.아주좁게), verticalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
            큰운동표.forEachIndexed { j, k ->
                val 늘 = j < 3
                val 켬 = 늘 || k.키 in 추가
                val 막 = !켬 && 추가.size >= 2
                Box(
                    Modifier.height(높이.아주낮게).graphicsLayer { alpha = if (막) 보고치수.막힌칩 else 1f }
                        .보고상자(색움직(if (켬) c.강조 else c.면, "큰칩"), 색움직(if (켬) c.강조 else c.속선, "큰칩테"))
                        .눌림 {
                            if (늘 || 막) return@눌림
                            상태.바꿈 { dd ->
                                val 옛 = 큰운동추가정리(dd.설정.큰운동추가)
                                val 새 = if (k.키 in 옛) 옛 - k.키 else if (옛.size < 2) 옛 + k.키 else 옛
                                dd.copy(설정 = dd.설정.copy(큰운동추가 = 큰운동추가정리(새)))
                            }
                        }
                        .semantics { contentDescription = "${k.키}${if (켬) " 켜짐" else ""}"; role = Role.Checkbox }
                        .padding(horizontal = 보고치수.칩옆),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        k.키, style = 글꼴.보통(크기.조금작게, if (켬) FontWeight.Bold else FontWeight.Medium),
                        color = if (켬) c.강조글 else c.글, maxLines = 1, softWrap = false,
                    )
                }
            }
        }
        if (추가.size >= 2) Text("최대 5개까지", Modifier.padding(top = 간격.좁게), style = 글꼴.보통(크기.작게), color = c.옅음)
        아래()
    }
}

/** '○○ 상세' (v17 B 7) — 지금 값 · 최고 기록 날짜. 자세한 내용은 준비 중 (목록: 틀만) */
@Composable
private fun 큰운동상세시트(글: String, 큰: List<큰칸>, 닫기: () -> Unit) {
    val c = Local색.current
    val x = 큰.firstOrNull { it.글 == 글 } ?: 큰칸(글, 0.0, 0.0, null)
    val 합 = x === 큰.firstOrNull()
    시트("$글 상세", 닫기) {
        설정줄("지금 값") { Text(큰값글(x, 합), style = 글꼴.보통(크기.본문, FontWeight.Bold), color = c.글) }
        구분선()
        설정줄("최고 기록 날짜") { Text(큰때글(x.때), style = 글꼴.보통(크기.본문), color = c.글) }
        Text("자세한 기록은 준비 중입니다", Modifier.padding(top = 간격.좁게), style = 글꼴.보통(크기.작게), color = c.옅음)
    }
}

/** '○○ 루틴 상세' (v18 B 1) — 형태만 */
@Composable
private fun 루틴상세시트(이름: String, 닫기: () -> Unit) {
    val c = Local색.current
    시트("$이름 상세", 닫기) {
        Text("자세한 기록은 준비 중입니다", style = 글꼴.보통(크기.작게), color = c.옅음)
    }
}

// ═════════════════════ 이미지 저장 · 공유 (v19 B — 앱에서는 MediaStore · ACTION_SEND) ═════════════════════

/** 갤러리(Pictures/하젠하이데)에 넣는다 — API 29 이상, 권한 없이. 실패하면 false (반쯤 쓴 항목은 지운다) */
private fun 갤러리저장(ctx: Context, 그림: Bitmap, 이름: String): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
    val cr = ctx.contentResolver
    val 값 = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, 이름)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/하젠하이데")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }
    val uri = try { cr.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), 값) } catch (_: Exception) { null } ?: return false
    return try {
        val 됨 = cr.openOutputStream(uri)?.use { 그림.compress(Bitmap.CompressFormat.PNG, 100, it) } ?: false
        if (!됨) error("쓰지 못함")
        cr.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
        true
    } catch (_: Exception) {
        try { cr.delete(uri, null, null) } catch (_: Exception) { }
        false
    }
}

/** 공유할 파일 — 앱 캐시 report/ (지난 것은 지운다). FileProvider 경로 = res/xml/report_paths.xml */
private fun 보고캐시(ctx: Context, 그림: Bitmap, 이름: String): File {
    val 폴더 = File(ctx.cacheDir, "report").apply { mkdirs() }
    폴더.listFiles()?.forEach { it.delete() }
    val f = File(폴더, 이름)
    FileOutputStream(f).use { if (!그림.compress(Bitmap.CompressFormat.PNG, 100, it)) error("쓰지 못함") }
    return f
}

/** 폰의 공유 창 (인스타 스토리 · 카카오톡 …은 거기서 고른다). 열 앱이 없으면 false */
private fun 공유열기(ctx: Context, f: File): Boolean = try {
    val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.files", f)
    val 보냄 = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        clipData = ClipData.newRawUri(f.name, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    ctx.startActivity(Intent.createChooser(보냄, "운동 보고서").apply { if (ctx !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
    true
} catch (_: Exception) { false }
