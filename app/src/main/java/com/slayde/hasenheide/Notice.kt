package com.slayde.hasenheide

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import androidx.compose.ui.graphics.toArgb
import com.slayde.hasenheide.ui.theme.밝은색표

/**
 * 앱 밖 안내 팝업 (10-13 홍겸 님) — 앱을 벗어나 있을 때 화면 가운데에 잠깐 뜨는 글.
 * 토스트는 안드로이드 11 부터 위치를 못 바꾸고 아래쪽에만 떠서, 동그라미와 같은 '다른 앱 위에 표시' 권한으로 직접 띄운다.
 * 권한이 없으면 아무것도 하지 않는다. 눌러도 반응하지 않고(뒤의 앱이 그대로 눌린다) 몇 초 뒤 저절로 사라진다.
 */
object 밖알림 {
    private var 뷰: TextView? = null
    private var 창: WindowManager? = null
    private val 손 = Handler(Looper.getMainLooper())
    private val 치우기 = Runnable { 치움() }

    fun 띄움(ctx: Context, 글: String) {
        if (글.isBlank() || !떠있는동그라미.됨(ctx)) return
        val app = ctx.applicationContext
        val wm = app.getSystemService(WindowManager::class.java) ?: return
        치움()
        val 화면 = app.resources.displayMetrics
        val 밀도 = 화면.density
        val t = TextView(app).apply {
            text = 글
            setTextColor(밝은색표.강조글.toArgb())
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setLineSpacing(0f, 1.15f)
            maxWidth = 화면.widthPixels - (48 * 밀도).toInt()
            setPadding((22 * 밀도).toInt(), (16 * 밀도).toInt(), (22 * 밀도).toInt(), (16 * 밀도).toInt())
            background = GradientDrawable().apply { setColor(밝은색표.강조.toArgb()); cornerRadius = 16 * 밀도 }
            alpha = 0f
        }
        val p = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT,
        ).apply { gravity = Gravity.CENTER }
        try {
            wm.addView(t, p); 뷰 = t; 창 = wm
            t.animate().alpha(1f).setDuration(160).start()
            손.postDelayed(치우기, 보임ms)
        } catch (_: Exception) { 뷰 = null; 창 = null }
    }

    fun 치움() {
        손.removeCallbacks(치우기)
        val t = 뷰 ?: return
        뷰 = null
        try { 창?.removeView(t) } catch (_: Exception) { }
        창 = null
    }

    private const val 보임ms = 3500L   // 글이 떠 있는 시간 [추측: 폰에서 보고 조절]
}
