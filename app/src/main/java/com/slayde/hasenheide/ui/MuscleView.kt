package com.slayde.hasenheide.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.LruCache
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.slayde.hasenheide.data.근육계산
import com.slayde.hasenheide.data.근육단계
import com.slayde.hasenheide.data.근육입력들
import com.slayde.hasenheide.data.근육자료
import com.slayde.hasenheide.data.근육표
import com.slayde.hasenheide.data.운동세션
import com.slayde.hasenheide.data.종목
import com.slayde.hasenheide.data.종목근육글
import com.slayde.hasenheide.data.종목부위
import com.slayde.hasenheide.data.지금종목
import com.slayde.hasenheide.data.찬것
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.그림칸
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.모서리
import com.slayde.hasenheide.ui.theme.움직임
import com.slayde.hasenheide.ui.theme.크기
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * 근육 그림 · 종목 사진 · 운동 중 위쪽 그림 칸 (10-02).
 *
 * 홍겸 님 10-02: "운동화면에 근육사진 넣어서 수행도 올라갈때마다 빨개지고, 휴식하면 다시 원래대로 돌아오는거"
 * 규칙 = claude.ai 프로젝트 문서 07_근육지도규칙.md · 08_시안결정.md 3·4절. 기준 구현 = 7일 체험 아티팩트 (배너 · 종목줄).
 * 계산은 data/Muscle.kt, 그림 조각은 data/MuscleData.kt (tools/근육지도_생성.py 가 만든다).
 */

// ─────────────── 근육 그림 ───────────────

/** 조각마다 Path — 처음 한 번만 만든다. 뒷모습은 오른쪽으로 240 옮겨 둔다 */
private val 근육길: List<Path> by lazy {
    근육자료.조각.map { p ->
        PathParser().parsePathString(p.d).toPath().also { if (p.뒤) it.translate(Offset(근육표.뒤옮김, 0f)) }
    }
}

/** 0xRRGGBB → 색 */
private fun 색으로(rgb: Int): Color = Color(red = (rgb shr 16) and 0xFF, green = (rgb shr 8) and 0xFF, blue = rgb and 0xFF)

/**
 * 몸 그림 — 단계(잎 id → 0~20)로 조각을 칠한다. 색이 바뀌면 0.6초 동안 옮겨 간다.
 * [자르기] = [x, y, 폭, 높이] (그림 좌표) — 주면 그 부분만 크게. 없으면 앞 · 뒤 전신
 */
@Composable
fun 몸그림(단계: Map<String, Double>, 색표이름: String, 자르기: FloatArray?, modifier: Modifier = Modifier) {
    val c = Local색.current
    val 값들 = remember(단계) { 근육계산.조각값(단계) }
    val 조각 = 근육자료.조각
    val 기본 = c.근육
    // 조각마다 색 — 근육 조각만. 조각 수는 늘 같으므로 자리가 흔들리지 않는다
    val 색들: List<State<Color>?> = 조각.mapIndexed { i, p ->
        if (p.종류 != 'm') null
        else animateColorAsState(근육계산.단계색(값들[i], 색표이름)?.let { 색으로(it) } ?: 기본, tween(움직임.근육색), label = "근육")
    }
    val 길 = 근육길
    val 바탕색 = c.피부; val 결색 = c.결; val 테두리 = c.면
    Canvas(modifier.clipToBounds()) {
        val vx = 자르기?.get(0) ?: 0f
        val vy = 자르기?.get(1) ?: 0f
        val vw = 자르기?.get(2) ?: 근육표.그림폭
        val vh = 자르기?.get(3) ?: 근육표.그림높이
        // viewBox 를 칸에 맞춘다 — 가운데 · 비율 유지 (SVG xMidYMid meet 와 같다)
        val s = min(size.width / vw, size.height / vh)
        val tx = (size.width - vw * s) / 2f - vx * s
        val ty = (size.height - vh * s) / 2f - vy * s
        withTransform({ translate(tx, ty); scale(s, s, pivot = Offset.Zero) }) {
            조각.forEachIndexed { i, p ->
                when (p.종류) {
                    'b' -> drawPath(길[i], 바탕색)
                    'x' -> drawPath(길[i], 결색, alpha = 그림칸.결진하기, style = Stroke(width = 그림칸.결선))
                    else -> {
                        drawPath(길[i], 색들[i]?.value ?: 기본)
                        drawPath(길[i], 테두리, style = Stroke(width = 그림칸.근육선, join = StrokeJoin.Round))
                    }
                }
            }
        }
    }
}

// ─────────────── 종목 사진 — filesDir/photos ───────────────

/**
 * 종목 사진 파일 (08 4절). 앱 안(filesDir/photos)에 긴 변 1080 이하 JPEG 로 줄여 둔다.
 * 종목에는 파일 이름만 적는다 → 백업 파일에는 이름만 들어가고 사진은 들어가지 않는다.
 */
object 사진함 {
    private const val 폴더이름 = "photos"
    // 감시관: 백업 파일의 이름에 '../' 가 있어도 photos 폴더 밖을 가리키지 않게 마지막 이름만 쓴다
    fun 파일(ctx: Context, 이름: String): File = File(File(ctx.filesDir, 폴더이름).also { it.mkdirs() }, File(이름).name)

    /**
     * 남은 파일 치우기 — 앱을 켤 때 한 번 (감시관: 지운 종목 · 되돌리기 기다리다 화면을 떠난 경우 · 넣다가 접은 경우 파일이 남았다).
     * 어느 종목에도 없는 사진 중 [그림칸.정리유예ms] 보다 오래된 것만 지운다 (방금 넣는 중인 파일은 둔다)
     */
    fun 정리(ctx: Context, 쓰는: Set<String>) {
        try {
            val 기준 = System.currentTimeMillis() - 그림칸.정리유예ms
            File(ctx.filesDir, 폴더이름).listFiles()?.forEach { f -> if (f.name !in 쓰는 && f.lastModified() < 기준) f.delete() }
        } catch (_: Exception) { }
    }

    /** 읽은 사진 — 이름@크기 → 그림 (약 24MB) */
    private val 기억 = object : LruCache<String, ImageBitmap>(24 * 1024 * 1024) {
        override fun sizeOf(key: String, value: ImageBitmap): Int = value.width * value.height * 4
    }

    fun 기억값(이름: String, 최대px: Int): ImageBitmap? = 기억.get("$이름@$최대px")

    /** 긴 변이 [최대px] 보다 작아지지 않는 만큼만 건너뛰며 읽는다 — IO 스레드에서 */
    fun 읽기(ctx: Context, 이름: String, 최대px: Int): ImageBitmap? {
        기억값(이름, 최대px)?.let { return it }
        val f = 파일(ctx, 이름)
        if (!f.exists()) return null
        val 겉 = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(f.path, 겉)
        var n = 1
        while (max(겉.outWidth, 겉.outHeight) / (n * 2) >= 최대px) n *= 2
        val b = BitmapFactory.decodeFile(f.path, BitmapFactory.Options().apply { inSampleSize = n }) ?: return null
        return b.asImageBitmap().also { 기억.put("$이름@$최대px", it) }
    }

    /** 고른 사진을 줄여서 넣는다 — IO 스레드에서. 넣은 파일 이름들 */
    fun 넣기(ctx: Context, 고른것: List<Uri>): List<String> = 고른것.mapIndexedNotNull { i, uri ->
        try {
            val cr = ctx.contentResolver
            val 겉 = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, 겉) }
            val 긴 = max(겉.outWidth, 겉.outHeight)
            if (긴 <= 0) return@mapIndexedNotNull null
            var n = 1
            while (긴 / (n * 2) >= 그림칸.사진긴변) n *= 2
            var b: Bitmap = cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = n }) }
                ?: return@mapIndexedNotNull null
            val 지금긴 = max(b.width, b.height)
            if (지금긴 > 그림칸.사진긴변) {
                val r = 그림칸.사진긴변.toFloat() / 지금긴
                b = Bitmap.createScaledBitmap(b, (b.width * r).roundToInt().coerceAtLeast(1), (b.height * r).roundToInt().coerceAtLeast(1), true)
            }
            // 사진기가 적어 둔 돌림(EXIF)대로 바로 세운다 — 그냥 읽으면 옆으로 누운 사진이 있다
            val 돌림 = cr.openInputStream(uri)?.use {
                when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
            if (돌림 != 0f) b = Bitmap.createBitmap(b, 0, 0, b.width, b.height, Matrix().apply { postRotate(돌림) }, true)
            val 이름 = "${System.currentTimeMillis()}_${i}.jpg"
            FileOutputStream(파일(ctx, 이름)).use { b.compress(Bitmap.CompressFormat.JPEG, 그림칸.사진품질, it) }
            이름
        } catch (_: Exception) { null }
    }

    fun 지우기(ctx: Context, 이름: String) {
        try { 파일(ctx, 이름).delete() } catch (_: Exception) { }
    }
}

/** 사진 한 장 — IO 스레드에서 읽고 기억한다. 읽는 동안은 빈 칸 */
@Composable
fun 사진그림(이름: String, 최대: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier, 설명: String? = null) {
    val ctx = LocalContext.current
    val px = with(LocalDensity.current) { 최대.roundToPx() }.coerceAtLeast(1)
    val 그림 by produceState(사진함.기억값(이름, px), 이름, px) {
        // 감시관: produceState 는 키가 바뀌어도 앞 값을 들고 있다 → 먼저 이 이름의 값으로 바꾸고 없으면 읽는다
        value = 사진함.기억값(이름, px)
        if (value == null) value = withContext(Dispatchers.IO) { 사진함.읽기(ctx, 이름, px) }
    }
    val b = 그림
    if (b != null) Image(b, 설명, modifier, contentScale = ContentScale.Crop)
    else Box(modifier.background(Local색.current.면2))
}

// ─────────────── 운동 중 위쪽 그림 칸 (08 3절 · 아티팩트 배너()) ───────────────

/** 그림 칸 기억 — 세션 동안만 (파일에 적지 않는다. 앱을 다시 켜면 처음으로) */
object 그림칸기억 {
    /** ✕ 로 감춘 운동(루틴 id). 다른 루틴으로 운동하면 다시 보인다.
     *  감시관: 시작 시각으로 기억하면 '운동 끝내기 → 돌아가기' 때 시작 시각이 바뀌어 다시 나타났다 */
    var 숨긴운동 by mutableStateOf("")
    private var 마지막키 = ""
    private var 씨값 = intArrayOf(0, 1)

    /** 세트를 끝낼 때마다 새 무작위 수 둘 — 같은 세트 수에서는 그대로 */
    fun 씨(키: String): IntArray {
        if (키 != 마지막키) { 마지막키 = 키; 씨값 = intArrayOf(Random.nextInt(1_000_000), Random.nextInt(1_000_000)) }
        return 씨값
    }
}

/** 칸 하나에 그릴 것 — 이 값이 바뀌면 옛 칸은 오른쪽 끝으로 접혀 들어가고 새 칸이 펼쳐진다 */
private data class 판(val 종류: String, val 종목: String = "", val 사진: String? = null, val 번호: Int = 0, val 수: Int = 0, val 끝낸: Int = 0)

/**
 * 운동 중 위쪽 그림 칸 — 근육 2장 · 근육 + 사진 · 사진 1장 · 사진 2장 (설정에서 고른다).
 *  · 쉬는 동안 칸이 커진다 (124 → 168)
 *  · 근육: '지금 몸'(앞 · 뒤 전신 = 남은 피로 + 오늘) · 지금 종목 확대. 세트를 끝낼 때마다 빨개지고, 쉬면 원래 색으로 돌아간다
 *  · 사진: 세트를 끝낼 때마다 무작위로 바뀐다. 오른쪽에 접힌 더미(남은 사진 수, 최대 9)
 */
@Composable
fun 운동그림칸(상태: 앱상태, S: 운동세션, 지금: Long, on숨김: () -> Unit) {
    val c = Local색.current
    val d = 상태.d
    val 모 = d.설정.배너
    val e = S.지금종목
    val 끝낸 = S.종목들.sumOf { it.찬것().size }
    val 높 by animateDpAsState(if (S.휴식 != null) 그림칸.쉬는높이 else 그림칸.높이, tween(움직임.배너높이, easing = 움직임.부드럽게), label = "그림칸높이")
    // 근육 단계 — 1분마다 다시 잰다 (회복은 시간 단위로 느리게 내려간다)
    val 분 = 지금 / 60_000L
    val 단계 = remember(S.종목들, d.피로, d.최대볼륨, d.몸.체중, d.종목표, 분) { d.근육단계(S.근육입력들(), 지금) }
    val 사진들 = d.종목표.firstOrNull { it.이름 == e.이름 }?.사진.orEmpty()
    val 씨 = 그림칸기억.씨("${S.시작시각}|${끝낸}")
    fun 사진판(칸: Int): 판 {
        val n = 사진들.size
        if (n == 0) return 판("사진없음", e.이름)
        val i0 = 씨[0] % n
        val i = if (칸 == 0) i0 else (씨[1] + (if (n > 1 && i0 == 씨[1] % n) 1 else 0)) % n
        return 판("사진", e.이름, 사진들[i], i, n, 끝낸)
    }
    val 칸들: List<판> = when (모) {
        "근육 2장" -> listOf(판("전신"), 판("확대", e.이름))
        "근육 + 사진" -> listOf(if (끝낸 % 2 == 1) 판("확대", e.이름) else 판("전신"), 사진판(0))
        "사진 1장" -> listOf(사진판(0))
        else -> listOf(사진판(0), 사진판(1))
    }
    val 더미수 = (사진들.size - (if (모 == "사진 2장") 2 else 1)).coerceIn(0, 그림칸.더미최대)
    Box(Modifier.fillMaxWidth().height(높).번호("운1.1").padding(start = 간격.보통, end = 간격.보통, top = 간격.좁게)) {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(그림칸.사이)) {
            칸들.forEachIndexed { 칸, p0 ->
                key(칸) {
                    Box(Modifier.weight(1f).fillMaxHeight()) {
                        접고펴는칸(p0) { p -> 그림판(p, 단계, d.설정.색표, if (p.종류 == "사진") 더미수 else 0, d.종목부위(p.종목)) }
                    }
                }
            }
        }
        // ✕ — 이 운동 동안 감춘다. 머리줄의 '그림' 으로 다시 보인다
        val 손 = remember { MutableInteractionSource() }
        val 배 = 눌림배율(손)
        Box(
            Modifier.align(Alignment.TopEnd).padding(top = 간격.아주좁게, end = 간격.아주좁게).size(그림칸.닫기).배율(배).clip(CircleShape)
                .background(c.면).눌림손(손) { 발자취.적기("그림 칸 감춤"); on숨김() },
            contentAlignment = Alignment.Center,
        ) { Icon(아이콘.닫기, "그림 감추기", Modifier.size(16.dp), tint = c.흐림) }
    }
}

/**
 * 칸이 바뀔 때 — 옛 칸은 오른쪽 끝으로 얇게 접혀 들어가고, 새 칸은 그 자리에서 펼쳐져 나온다 (08 3절).
 * 처음 보일 때는 움직이지 않는다
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun 접고펴는칸(대상: 판, 내용: @Composable (판) -> Unit) {
    AnimatedContent(
        targetState = 대상,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = {
            fadeIn(tween(움직임.사진펼침, delayMillis = 움직임.사진펼침늦춤), initialAlpha = 0.3f) togetherWith fadeOut(tween(움직임.사진접힘))
        },
        label = "그림판",
    ) { p ->
        val 가로 by transition.animateFloat(
            transitionSpec = {
                if (targetState == EnterExitState.Visible) tween(움직임.사진펼침, delayMillis = 움직임.사진펼침늦춤, easing = 움직임.부드럽게)
                else tween(움직임.사진접힘)
            },
            label = "접힘",
        ) { 때 -> if (때 == EnterExitState.Visible) 1f else 움직임.접힌폭 }
        Box(Modifier.fillMaxSize().graphicsLayer { scaleX = 가로; transformOrigin = TransformOrigin(1f, 0.5f) }) { 내용(p) }
    }
}

/** 칸 하나 — 둥근 판 + 왼쪽 아래 이름 (+ 오른쪽 사진 더미) */
@Composable
private fun 그림판(p: 판, 단계: Map<String, Double>, 색표이름: String, 더미: Int, 부위: String?) {
    val c = Local색.current
    val 모양 = RoundedCornerShape(그림칸.모서리)
    Box(Modifier.fillMaxSize().clip(모양).background(c.면).border(1.dp, c.선, 모양)) {
        when (p.종류) {
            "전신" -> 몸그림(단계, 색표이름, null, Modifier.fillMaxSize().padding(간격.아주좁게))
            "확대" -> 몸그림(단계, 색표이름, remember(p.종목, 부위) { 근육계산.확대상자(p.종목, 부위) }, Modifier.fillMaxSize().padding(간격.아주좁게))
            "사진" -> 사진그림(p.사진 ?: "", 그림칸.쉬는높이 * 2, Modifier.fillMaxSize(), "${p.종목} 사진")
            else -> 글("사진 없음", Modifier.align(Alignment.Center), 크기값 = 크기.작게, 색 = c.옅음, 가운데 = true)   // 설명 글은 한 줄
        }
        // 이름 — 지금 종목 이름은 잘리면 안 된다 (U5-8): 말줄임 없이 글자를 줄인다 (10-01 홍겸 님 "짜르지말고 차라리 글자크기를 줄여")
        val 제목 = when (p.종류) { "전신" -> "지금 몸"; "사진" -> "${p.번호 + 1}/${p.수}"; else -> p.종목 }
        Box(
            Modifier.align(Alignment.BottomStart).padding(start = 간격.좁게, bottom = 간격.아주좁게, end = 간격.아주넓게)
                .clip(RoundedCornerShape(그림칸.모서리)).background(c.면).padding(horizontal = 간격.아주좁게),
        ) { 맞춤글(제목, 최대 = 크기.아주작게, 색 = c.흐림) }
        if (더미 > 0) 사진더미(더미)
    }
}

/** 오른쪽 끝의 접힌 사진 더미 — 남은 사진 수만큼 얇은 줄 (최대 9) */
@Composable
private fun BoxScope.사진더미(n: Int) {
    val c = Local색.current
    Row(
        Modifier.align(Alignment.CenterEnd).fillMaxHeight().padding(end = 그림칸.더미폭, top = 간격.좁게, bottom = 간격.좁게),
        horizontalArrangement = Arrangement.spacedBy(그림칸.더미사이),
    ) {
        repeat(n) { Box(Modifier.width(그림칸.더미폭).fillMaxHeight().background(c.속선)) }
    }
}

// ─────────────── 종목 탭 — 사진 줄 (08 4절) ───────────────

/** 종목 줄 왼쪽의 작은 사진 (28) — 사진이 없으면 점선 빈 칸 */
@Composable
fun 작은사진(e: 종목) {
    val c = Local색.current
    val 모양 = RoundedCornerShape(그림칸.모서리)
    val 첫 = e.사진.firstOrNull()
    if (첫 != null) 사진그림(첫, 그림칸.작은사진 * 2, Modifier.size(그림칸.작은사진).clip(모양))
    else Box(Modifier.size(그림칸.작은사진).clip(모양).background(c.면2).border(1.dp, c.속선, 모양))
}

/**
 * 종목을 펼쳤을 때 맨 위 — 72 사진 칸 한 줄 (첫 칸 '사진 추가', 넘치면 옆으로 밀기) + 근육 한 줄.
 * 사진을 꾹 누르면 그 자리에 '지우기' 가 뜬다 → 누르면 지운다 (5초 되돌리기). 다른 곳을 누르면 그대로
 */
@Composable
fun 종목사진줄(상태: 앱상태, e: 종목) {
    val c = Local색.current
    val ctx = LocalContext.current
    val 일꾼 = rememberCoroutineScope()
    var 지울것 by remember(e.이름) { mutableStateOf<String?>(null) }
    var 넣는중 by remember { mutableStateOf(false) }
    val 남은칸 = (근육표.사진최대 - e.사진.size).coerceAtLeast(0)
    val 고르기 = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(근육표.사진최대)) { 고른것 ->
        if (고른것.isEmpty()) return@rememberLauncherForActivityResult
        넣는중 = true
        일꾼.launch {
            val 이름들 = withContext(Dispatchers.IO) { 사진함.넣기(ctx, 고른것.take(남은칸)) }
            발자취.적기("${e.이름} 사진 ${이름들.size}장 넣음")
            상태.바꿈 { d -> d.copy(종목표 = d.종목표.map { if (it.이름 == e.이름) it.copy(사진 = (it.사진 + 이름들).take(근육표.사진최대)) else it }) }
            넣는중 = false
        }
    }
    val 모양 = RoundedCornerShape(그림칸.모서리)
    val 넘김 = rememberScrollState()
    Row(Modifier.fillMaxWidth().오른끝흐림(넘김).horizontalScroll(넘김), horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
        if (남은칸 > 0) {
            val 손 = remember { MutableInteractionSource() }
            val 배 = 눌림배율(손)
            Column(
                Modifier.size(그림칸.사진).배율(배).clip(모양).border(1.dp, c.속선, 모양)
                    .눌림손(손) {
                        if (!넣는중) 고르기.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
            ) {
                Icon(아이콘.더하기, null, Modifier.size(18.dp), tint = c.강조)
                글(if (넣는중) "넣는 중" else "사진 추가", 크기값 = 크기.아주작게, 색 = c.흐림)
            }
        }
        e.사진.forEach { 이름 ->
            key(이름) {
                Box(
                    // 감시관: 꾹 누르기는 어디서나 '집기'(U5-1) → 지우기는 누르면 '지우기' 표시 · 한 번 더 누르면 지움
                    Modifier.size(그림칸.사진).clip(모양).눌림 {
                        if (지울것 == 이름) { 사진지우기(상태, e.이름, 이름); 지울것 = null } else 지울것 = 이름
                    },
                ) {
                    사진그림(이름, 그림칸.사진 * 2, Modifier.fillMaxSize(), "${e.이름} 사진")
                    val 보임 by animateFloatAsState(if (지울것 == 이름) 1f else 0f, tween(움직임.색), label = "지우기표")
                    if (보임 > 0f) Box(
                        Modifier.fillMaxSize().graphicsLayer { alpha = 보임 }.background(c.나쁨),
                        contentAlignment = Alignment.Center,
                    ) { 글("지우기", 크기값 = 크기.작게, 색 = c.면, 굵기 = FontWeight.Bold) }
                }
            }
        }
    }
    // 근육 한 줄 — 넘치면 자르지 않고 글자를 줄인다
    맞춤글(상태.d.종목근육글(e.이름), Modifier.fillMaxWidth().padding(top = 간격.좁게), 최대 = 크기.작게, 색 = c.흐림)
}

/** 사진 지우기 — 목록에서 빼고 아래띠로 되돌린다 (U5-4). 파일은 다음에 앱을 켤 때 `사진함.정리` 가 치운다 */
private fun 사진지우기(상태: 앱상태, 종목이름: String, 이름: String) {
    발자취.적기("$종목이름 사진 지움")
    상태.지우고알림("$종목이름 사진을 지웠습니다") { d ->
        d.copy(종목표 = d.종목표.map { if (it.이름 == 종목이름) it.copy(사진 = it.사진 - 이름) else it })
    }
}
