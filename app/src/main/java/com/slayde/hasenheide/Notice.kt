package com.slayde.hasenheide

import android.content.Context
import android.graphics.Outline
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

/**
 * 앱 밖 안내 팝업 (10-13 홍겸 님) — 앱을 벗어나 동그라미가 떠 있을 때 화면 **위쪽**에 잠깐 뜨는 한 줄 글. 맨 앞에 우리 운동 아이콘(동그랗게).
 * 기본 토스트는 연달아 누르면 줄을 서서 늦게 뜨거나 빠지고, 글자 크기도 못 줄여서 직접 띄운다 (동그라미와 같은 '다른 앱 위에 표시' 권한).
 *  · 이미 떠 있으면 새로 쌓지 않고 글만 바꾸고 시간을 다시 센다 → 빠르게 눌러도 항상 마지막 글이 보인다
 *  · 한 줄에 안 들어가면 글자를 줄이고(14 → 9sp), 그래도 넘치면 … 으로 줄인다
 *  · 새 종목으로 들어갈 때([길게])는 1.5배 오래 보인다
 *  · 눌러도 반응하지 않는다(뒤의 앱이 그대로 눌린다)
 */
object 밖알림 {
    private var 틀: LinearLayout? = null
    private var 글뷰: TextView? = null
    private var 요소: WindowManager.LayoutParams? = null
    private var 창: WindowManager? = null
    private val 손 = Handler(Looper.getMainLooper())
    private val 치우기 = Runnable { 치움() }

    /** [길게] = 새 종목으로 들어갈 때(1.5배) · [배너아래] = 휴식 끝 알림 배너(화면 위)와 같이 뜰 때 그 아래로 */
    fun 띄움(ctx: Context, 글: String, 길게: Boolean = false, 배너아래: Boolean = false) {
        if (글.isBlank() || !떠있는동그라미.됨(ctx)) return
        val app = ctx.applicationContext
        val 화면 = app.resources.displayMetrics
        val 밀도 = 화면.density
        val 옆 = 16 * 밀도; val 아이콘 = 24 * 밀도; val 사이 = 8 * 밀도
        val 쓸폭 = 화면.widthPixels - 32 * 밀도 - 옆 * 2 - 아이콘 - 사이     // 화면 좌우 16dp 를 비우고 안쪽 여백 · 아이콘을 뺀 글 폭
        val 위 = 상태바높이(app) + (if (배너아래) 92 else 10) * 밀도
        val 시간 = if (길게) (보임ms * 1.5f).toLong() else 보임ms
        val 기존 = 틀
        val g = 글뷰
        if (기존 != null && g != null) {
            맞춤(g, 글, 쓸폭, 화면.scaledDensity)
            요소?.let { it.y = 위.toInt(); try { 창?.updateViewLayout(기존, it) } catch (_: Exception) { } }
            손.removeCallbacks(치우기); 손.postDelayed(치우기, 시간); return
        }
        val wm = app.getSystemService(WindowManager::class.java) ?: return
        val 그림 = ImageView(app).apply {
            try { setImageDrawable(ContextCompat.getDrawable(app, R.mipmap.ic_launcher)) } catch (_: Exception) { }
            scaleType = ImageView.ScaleType.CENTER_CROP
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(v: View, o: Outline) { o.setOval(0, 0, v.width, v.height) }
            }
            clipToOutline = true
        }
        val t = TextView(app).apply {
            setTextColor(0xFFFFFFFF.toInt())
            typeface = Typeface.DEFAULT_BOLD
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }
        맞춤(t, 글, 쓸폭, 화면.scaledDensity)
        val 줄 = LinearLayout(app).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding((12 * 밀도).toInt(), (9 * 밀도).toInt(), 옆.toInt(), (9 * 밀도).toInt())
            background = GradientDrawable().apply { setColor(0xEB303030.toInt()); cornerRadius = 28 * 밀도 }
            addView(그림, LinearLayout.LayoutParams(아이콘.toInt(), 아이콘.toInt()).apply { rightMargin = 사이.toInt() })
            addView(t, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
            alpha = 0f
            translationY = -16 * 밀도
        }
        val p = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL; y = 위.toInt() }
        try {
            wm.addView(줄, p); 틀 = 줄; 글뷰 = t; 요소 = p; 창 = wm
            줄.animate().alpha(1f).translationY(0f).setDuration(160).start()   // 위에서 내려오듯
            손.postDelayed(치우기, 시간)
        } catch (_: Exception) { 틀 = null; 글뷰 = null; 요소 = null; 창 = null }
    }

    private fun 상태바높이(ctx: Context): Float {
        val id = ctx.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) ctx.resources.getDimensionPixelSize(id).toFloat() else 24 * ctx.resources.displayMetrics.density
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
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp.coerceAtLeast(최소sp))
        t.maxWidth = 쓸폭.toInt()
    }

    fun 치움() {
        손.removeCallbacks(치우기)
        val v = 틀 ?: return
        틀 = null; 글뷰 = null; 요소 = null
        try { 창?.removeView(v) } catch (_: Exception) { }
        창 = null
    }

    private const val 보임ms = 2500L   // 글이 떠 있는 시간 [추측: 폰에서 보고 조절]
    private const val 최대sp = 14f
    private const val 최소sp = 9f
}
