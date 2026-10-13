package com.slayde.hasenheide

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

/**
 * 앱 밖 안내 글 (10-13 홍겸 님) — 앱을 벗어나 동그라미가 떠 있을 때 화면 아래쪽에 토스트처럼 잠깐 뜨는 한 줄 글.
 * 기본 토스트는 연달아 누르면 줄을 서서 늦게 뜨거나 빠지고, 글자 크기도 못 줄여서 직접 띄운다 (동그라미와 같은 '다른 앱 위에 표시' 권한).
 *  · 이미 떠 있으면 새로 쌓지 않고 글만 바꾸고 시간을 다시 센다 → 빠르게 눌러도 항상 마지막 글이 보인다
 *  · 한 줄에 안 들어가면 글자를 줄이고(14 → 9sp), 그래도 넘치면 … 으로 줄인다
 *  · 눌러도 반응하지 않는다(뒤의 앱이 그대로 눌린다)
 */
object 밖알림 {
    private var 뷰: TextView? = null
    private var 창: WindowManager? = null
    private val 손 = Handler(Looper.getMainLooper())
    private val 치우기 = Runnable { 치움() }

    fun 띄움(ctx: Context, 글: String) {
        if (글.isBlank() || !떠있는동그라미.됨(ctx)) return
        val app = ctx.applicationContext
        val 화면 = app.resources.displayMetrics
        val 밀도 = 화면.density
        val 가로여백 = 18 * 밀도
        val 쓸폭 = 화면.widthPixels - 32 * 밀도 - 가로여백 * 2     // 화면 좌우 16dp 를 비우고 안쪽 여백을 뺀 글 폭
        val 기존 = 뷰
        if (기존 != null) { 맞춤(기존, 글, 쓸폭, 화면.scaledDensity); 손.removeCallbacks(치우기); 손.postDelayed(치우기, 보임ms); return }
        val wm = app.getSystemService(WindowManager::class.java) ?: return
        val t = TextView(app).apply {
            setTextColor(0xFFFFFFFF.toInt())
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setPadding(가로여백.toInt(), (11 * 밀도).toInt(), 가로여백.toInt(), (11 * 밀도).toInt())
            background = GradientDrawable().apply { setColor(0xE6303030.toInt()); cornerRadius = 24 * 밀도 }
            alpha = 0f
        }
        맞춤(t, 글, 쓸폭, 화면.scaledDensity)
        val p = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT,
        ).apply { gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL; y = (88 * 밀도).toInt() }   // 기본 토스트 자리쯤
        try {
            wm.addView(t, p); 뷰 = t; 창 = wm
            t.animate().alpha(1f).setDuration(120).start()
            손.postDelayed(치우기, 보임ms)
        } catch (_: Exception) { 뷰 = null; 창 = null }
    }

    /** 한 줄에 들어가는 가장 큰 글자 크기로 맞춘다 (14sp → 9sp, 0.5 씩) */
    private fun 맞춤(t: TextView, 글: String, 쓸폭: Float, 글밀도: Float) {
        t.text = 글
        var sp = 최대sp
        while (sp > 최소sp) {
            t.paint.textSize = sp * 글밀도
            if (t.paint.measureText(글) <= 쓸폭) break
            sp -= 0.5f
        }
        t.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, sp.coerceAtLeast(최소sp))
        t.maxWidth = (쓸폭 + t.paddingLeft + t.paddingRight).toInt()
    }

    fun 치움() {
        손.removeCallbacks(치우기)
        val t = 뷰 ?: return
        뷰 = null
        try { 창?.removeView(t) } catch (_: Exception) { }
        창 = null
    }

    private const val 보임ms = 2500L   // 글이 떠 있는 시간 [추측: 폰에서 보고 조절]
    private const val 최대sp = 14f
    private const val 최소sp = 9f
}
