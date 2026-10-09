package com.slayde.hasenheide.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.slayde.hasenheide.data.근육계산
import com.slayde.hasenheide.data.종목사전
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.그림칸
import com.slayde.hasenheide.ui.theme.움직임
import kotlin.math.min

/*
 * 새 근육 그림 (10-09 홍겸 님 "다 바꿔") — 제미나이 회색 그림 + 15칸 지도.
 *  · 그림 · 지도 · 표는 tools/근육그림_생성.py 가 만든다 (assets/muscle/ · MusclePicData.kt)
 *  · 색: 회색 그림(밝기만)에 색을 곱한다 → 그림의 명암이 그대로 남는다.
 *    바탕(머리 · 손 · 발) = 색표 `피부` · 칸 = 0단계면 `근육`, 아니면 `근육` 과 단계색을 [그림칸.물들임] 만큼 섞은 색 (어두운 화면에서도 색표를 따른다 · U1-1)
 *  · 칸 값 = 칸에 든 근육의 잎 중 가장 큰 단계 (07 2절 ③) · 좌우는 같은 값
 *  · 누르기: 지도에서 누른 칸 → 그 칸의 세부 부위들 (첫째 = 팝업이 강조할 부위)
 */

/** 그림 한 장의 어느 부분을 보일지 — [번호] = 근육그림표 그림 번호 · [상자] = [x, y, 폭, 높이] (그림 픽셀 · null = 전체) */
internal class 그림보기(val 번호: String, val 상자: FloatArray? = null)

internal object 그림보기들 {
    val 전신 = listOf(그림보기("01"), 그림보기("02"))
    val 상전 = listOf(그림보기("01", 근육그림표.위상자))
    val 상후 = listOf(그림보기("02", 근육그림표.위상자))
    val 하전 = listOf(그림보기("01", 근육그림표.아래상자))
    val 하후 = listOf(그림보기("02", 근육그림표.아래상자))
}

/** 칸마다 세부 부위 키 (세부부위 24개 중 이 칸에 걸린 것) — 비면 눌러도 강조할 부위가 없다(목) */
internal val 칸세부키: List<List<String>> by lazy {
    근육그림표.칸들.map { k -> 종목사전.세부로(k.근육.associateWith { "P" }).keys.toList() }
}

/** 칸마다 잎 — 값은 이 잎들 중 가장 큰 단계 */
private val 칸잎: List<List<String>> by lazy { 근육그림표.칸들.map { k -> k.근육.flatMap { 근육계산.잎(it) }.distinct() } }

/** 칸마다 단계 (0~20) */
internal fun 칸단계(단계: Map<String, Double>): List<Double> =
    칸잎.map { l -> l.maxOfOrNull { 단계[it] ?: 0.0 }?.coerceAtLeast(0.0) ?: 0.0 }

/** 판의 값 → 칸 순번(0~14) · 빈 곳은 null */
internal fun 판값칸(v: Int): Int? = (v / 16 - 1).takeIf { it in 근육그림표.칸들.indices }

// ─────────────── 그림 읽기 (처음 한 번 · 앱이 끝날 때까지 둔다) ───────────────

/** 칸 하나의 조각 — 그 칸 자리만 남긴 회색 그림 (상자만큼 잘라 둠) */
private class 칸조각(val 칸: Int, val x: Int, val y: Int, val 그림: ImageBitmap)

private class 그림장(val 폭: Int, val 높이: Int, val 바탕: ImageBitmap, val 판: ByteArray, val 조각: List<칸조각>)

private object 근육그림함 {
    private val 캐시 = HashMap<String, 그림장?>()

    fun 얻기(ctx: Context, 번호: String): 그림장? = synchronized(캐시) {
        if (캐시.containsKey(번호)) return 캐시[번호]
        val g = try { 읽기(ctx, 번호) } catch (_: Exception) { null }
        캐시[번호] = g
        g
    }

    private fun 읽기(ctx: Context, 번호: String): 그림장 {
        val opt = BitmapFactory.Options().apply { inScaled = false; inPreferredConfig = Bitmap.Config.ARGB_8888 }
        val 바탕 = ctx.assets.open("muscle/$번호.webp").use { BitmapFactory.decodeStream(it, null, opt) }!!
        val 지도 = ctx.assets.open("muscle/${번호}_map.png").use { BitmapFactory.decodeStream(it, null, opt) }!!
        val w = 바탕.width; val h = 바탕.height
        val px = IntArray(w * h).also { 바탕.getPixels(it, 0, w, 0, 0, w, h) }
        val mp = IntArray(w * h).also { 지도.getPixels(it, 0, w, 0, 0, w, h) }
        val 판 = ByteArray(w * h) { i -> ((mp[i] shr 16) and 0xFF).toByte() }
        // 판의 값마다(칸 × 쪽) 상자 → 그 자리만 남긴 조각
        val 상자 = HashMap<Int, IntArray>()
        for (i in 판.indices) {
            val v = 판[i].toInt() and 0xFF
            if (판값칸(v) == null) continue
            val x = i % w; val y = i / w
            val b = 상자.getOrPut(v) { intArrayOf(x, y, x, y) }
            if (x < b[0]) b[0] = x; if (y < b[1]) b[1] = y; if (x > b[2]) b[2] = x; if (y > b[3]) b[3] = y
        }
        val 조각 = 상자.entries.sortedBy { it.key }.map { (v, b) ->
            val bw = b[2] - b[0] + 1; val bh = b[3] - b[1] + 1
            val out = IntArray(bw * bh)
            for (yy in 0 until bh) for (xx in 0 until bw) {
                val i = (b[1] + yy) * w + b[0] + xx
                if ((판[i].toInt() and 0xFF) == v) out[yy * bw + xx] = px[i]
            }
            칸조각(판값칸(v)!!, b[0], b[1], Bitmap.createBitmap(out, bw, bh, Bitmap.Config.ARGB_8888).asImageBitmap())
        }
        return 그림장(w, h, 바탕.asImageBitmap(), 판, 조각)
    }
}

// ─────────────── 맞춤 — 그리기와 누르기가 같은 자리를 쓴다 ───────────────

/** 상자들을 가로로 나란히 칸에 맞춘다 (가운데 · 비율 유지) → (배율, 왼쪽 시작, 위 시작) */
private fun 맞춤(상자들: List<FloatArray>, w: Float, h: Float): Triple<Float, Float, Float> {
    val 합폭 = 상자들.sumOf { it[2].toDouble() }.toFloat()
    val 최고 = 상자들.maxOf { it[3] }
    val s = min(w / 합폭, h / 최고)
    return Triple(s, (w - 합폭 * s) / 2f, (h - 최고 * s) / 2f)
}

private fun 상자(v: 그림보기, g: 그림장): FloatArray = v.상자 ?: floatArrayOf(0f, 0f, g.폭.toFloat(), g.높이.toFloat())

/**
 * 근육 그림 — [보기들] 을 나란히. 색이 바뀌면 0.6초 동안 옮겨 간다.
 * @param 누름 null 이면 누를 수 없다. 누르면 그 칸의 세부 부위들(빈 곳 · 목은 빈 목록)
 */
@Composable
internal fun 새몸그림(
    단계: Map<String, Double>, 색표이름: String, 보기들: List<그림보기>, modifier: Modifier = Modifier,
    누름: ((List<String>) -> Unit)? = null,
) {
    val c = Local색.current
    val ctx = LocalContext.current
    val 장들 = remember(보기들) { 보기들.map { 근육그림함.얻기(ctx, it.번호) } }
    val 값들 = remember(단계) { 칸단계(단계) }
    val 색들 = 값들.map { k ->
        val 목표 = 근육계산.단계색(k, 색표이름)?.let { lerp(c.근육, 색으로(it), 그림칸.물들임) } ?: c.근육
        animateColorAsState(목표, tween(움직임.근육색), label = "근육칸")
    }
    val 바탕필터 = remember(c.피부) { ColorFilter.tint(c.피부, BlendMode.Modulate) }
    val 누름최신 by rememberUpdatedState(누름)
    val 누름판 = if (누름 == null) Modifier else Modifier.pointerInput(보기들, 장들) {
        detectTapGestures { o ->
            누름최신?.invoke(누른칸(보기들, 장들, o.x, o.y, size.width.toFloat(), size.height.toFloat())?.let { 칸세부키[it] } ?: emptyList())
        }
    }
    Canvas(modifier.clipToBounds().then(누름판)) {
        if (장들.any { it == null }) return@Canvas   // 그림을 못 읽었다 — 빈 칸
        val 상자들 = 보기들.mapIndexed { i, v -> 상자(v, 장들[i]!!) }
        val (s, x0, y0) = 맞춤(상자들, size.width, size.height)
        var x = x0
        보기들.indices.forEach { i ->
            val g = 장들[i]!!; val b = 상자들[i]
            translate(x - b[0] * s, y0 - b[1] * s) {
                scale(s, s, pivot = Offset.Zero) {
                    clipRect(b[0], b[1], b[0] + b[2], b[1] + b[3]) {
                        drawImage(g.바탕, colorFilter = 바탕필터)
                        g.조각.forEach { k ->
                            drawImage(k.그림, Offset(k.x.toFloat(), k.y.toFloat()), colorFilter = ColorFilter.tint(색들[k.칸].value, BlendMode.Modulate))
                        }
                    }
                }
            }
            x += b[2] * s
        }
    }
}

/** 누른 자리 → 칸 순번 (그리기와 같은 맞춤) */
private fun 누른칸(보기들: List<그림보기>, 장들: List<그림장?>, px: Float, py: Float, w: Float, h: Float): Int? {
    if (장들.any { it == null }) return null
    val 상자들 = 보기들.mapIndexed { i, v -> 상자(v, 장들[i]!!) }
    val (s, x0, y0) = 맞춤(상자들, w, h)
    if (s <= 0f) return null
    var x = x0
    보기들.indices.forEach { i ->
        val g = 장들[i]!!; val b = 상자들[i]
        if (px >= x && px < x + b[2] * s) {
            val ix = (b[0] + (px - x) / s).toInt(); val iy = (b[1] + (py - y0) / s).toInt()
            if (ix < 0 || iy < 0 || ix >= g.폭 || iy >= g.높이) return null
            return 판값칸(g.판[iy * g.폭 + ix].toInt() and 0xFF)
        }
        x += b[2] * s
    }
    return null
}

/** 0xRRGGBB → 색 (색표 단계색) */
private fun 색으로(rgb: Int): Color = Color(red = (rgb shr 16) and 0xFF, green = (rgb shr 8) and 0xFF, blue = rgb and 0xFF)
