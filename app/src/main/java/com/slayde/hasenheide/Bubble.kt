package com.slayde.hasenheide

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import com.slayde.hasenheide.ui.theme.밝은색표
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * 떠 있는 동그라미 (10-08 홍겸 님) — 운동 중에 앱을 벗어나면 다른 앱(유튜브 작은 창 등) 위에 작은 동그라미를 띄운다.
 * 안드로이드 작은 창(PiP)은 한 번에 하나뿐이라 유튜브 작은 창과 같이 못 떴다 → '다른 앱 위에 표시' 권한으로 띄운다.
 *  · 쉬는 중: 초록 동그라미에 남은 시간 + 줄어드는 고리 + 운동 중처럼 퍼지는 고리 · 숨 쉬는 동그라미 (10-09)
 *  · 운동 중: 앱 아이콘 + 바깥으로 퍼지는 고리(누를 수 있다는 표시)
 *  · 누르면 운동 화면으로 돌아간다 · 끌어서 옮길 수 있다
 * 권한이 없으면 전처럼 PiP 를 쓴다 (MainActivity).
 */
object 떠있는동그라미 {
    /** 보여 줄 값 — [쉼남은초] null = 운동 중(아이콘), 아니면 휴식 남은 초 */
    /** [쉼끝시각] = 휴식이 끝나는 시각(ms) — 고리가 초 단위로 뚝뚝 줄지 않고 매끄럽게 줄게 하려고 (10-11 홍겸 님) */
    data class 값(val 쉼남은초: Int?, val 쉼총초: Int, val 쉼끝시각: Long = 0L)

    private var 뷰: 동그라미뷰? = null
    private var 마지막x = -1
    private var 마지막y = -1

    fun 됨(ctx: Context): Boolean = Build.VERSION.SDK_INT >= 26 && Settings.canDrawOverlays(ctx)

    /** 설정 화면 — '다른 앱 위에 표시' 권한 화면을 연다 */
    fun 권한열기(ctx: Context) {
        try {
            ctx.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${ctx.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) { }
    }

    /** [값] 이 null 을 돌려주면(운동이 끝남) 저절로 치운다 */
    fun 띄움(ctx: Context, 값: () -> 값?) {
        if (!됨(ctx) || 뷰 != null || 값() == null) return
        val app = ctx.applicationContext
        val wm = app.getSystemService(WindowManager::class.java) ?: return
        val 밀도 = app.resources.displayMetrics.density
        val 칸 = (동그라미치수.칸 * 밀도).toInt()
        val p = WindowManager.LayoutParams(
            칸, 칸,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = if (마지막x >= 0) 마지막x else app.resources.displayMetrics.widthPixels - 칸 - (동그라미치수.옆 * 밀도).toInt()
            y = if (마지막y >= 0) 마지막y else (동그라미치수.처음위 * 밀도).toInt()
        }
        val v = 동그라미뷰(app, 값, p, wm)
        try { wm.addView(v, p); 뷰 = v; v.시작() } catch (_: Exception) { }
    }

    fun 치움() {
        val v = 뷰 ?: return
        뷰 = null
        v.멈춤()
        마지막x = v.p.x; 마지막y = v.p.y
        try { v.wm.removeView(v) } catch (_: Exception) { }
    }

    @SuppressLint("ViewConstructor")
    private class 동그라미뷰(
        ctx: Context, val 값: () -> 값?, val p: WindowManager.LayoutParams, val wm: WindowManager,
    ) : View(ctx) {
        private val 밀도 = ctx.resources.displayMetrics.density
        private val 손 = Handler(Looper.getMainLooper())
        private val 틱 = object : Runnable {
            override fun run() {
                if (값() == null) { 치움(); return }
                invalidate(); 손.postDelayed(this, 동그라미치수.틱)
            }
        }
        private val 강조 = 밝은색표.강조.toArgb()
        private val 강조글 = 밝은색표.강조글.toArgb()
        private val 좋음 = 밝은색표.좋음.toArgb()
        private val 붓 = Paint(Paint.ANTI_ALIAS_FLAG)
        private val 글붓 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 강조글; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD
            textSize = 동그라미치수.글 * 밀도
        }
        private val 그림 = try { ContextCompat.getDrawable(ctx, R.mipmap.ic_launcher) } catch (_: Exception) { null }
        private val 동그길 = Path()
        private val 고리칸 = RectF()

        fun 시작() { 손.post(틱) }
        fun 멈춤() { 손.removeCallbacks(틱) }

        override fun onDraw(c: Canvas) {
            val v = 값() ?: return
            val cx = width / 2f; val cy = height / 2f
            val r = 동그라미치수.반지름 * 밀도
            val 남 = v.쉼남은초
            if (남 != null && 남 > 0) {
                // 쉬는 중 — 초록 동그라미 · 남은 시간 · 줄어드는 고리 (홍겸 님이 보낸 초록 동그라미처럼)
                // 10-09 홍겸 님: 운동 중 아이콘처럼 바깥으로 퍼지는 고리 + 가운데 동그라미가 숨 쉬듯 커졌다 작아짐 (퍼지는 고리는 동그라미와 같은 초록)
                val t = (SystemClock.uptimeMillis() % 동그라미치수.한바퀴).toFloat() / 동그라미치수.한바퀴
                val 퍼짐 = (동그라미치수.칸 / 2f * 밀도 - r)
                붓.style = Paint.Style.STROKE; 붓.strokeWidth = 동그라미치수.고리 * 밀도; 붓.strokeCap = Paint.Cap.BUTT
                붓.color = 좋음; 붓.alpha = ((1f - t) * 200).toInt()
                c.drawCircle(cx, cy, r + 퍼짐 * t, 붓)
                // 10-11 홍겸 님: 층이 둘이다 — 안쪽(시간 · 고리 · 옅은 띠)은 크기 고정, 전체를 감싼 초록 동그라미(바깥)만 숨 쉬듯 줄었다 커진다
                val 숨 = 1f + 동그라미치수.숨폭 / 2 + 동그라미치수.숨폭 / 2 * sin(t * 2 * PI).toFloat()   // 1.0 ~ 1.05 — 안쪽 가장자리(1.0)보다 작아지지 않는다
                붓.style = Paint.Style.FILL; 붓.color = 좋음; 붓.alpha = 255
                c.drawCircle(cx, cy, r * 숨, 붓)
                // 10-11 홍겸 님: 띠를 25% 크게 — 바깥 가장자리(예전과 같은 자리)는 두고 안쪽으로 굵어진다 (바깥으로 가면 동그라미 숨쉬기가 가려진다)
                val 옛굵 = 동그라미치수.고리 * 밀도
                val 굵 = 옛굵 * 동그라미치수.띠늘림
                val 띠반 = r - 옛굵 / 2f - 굵 / 2f
                고리칸.set(cx - 띠반, cy - 띠반, cx + 띠반, cy + 띠반)
                붓.style = Paint.Style.STROKE; 붓.strokeWidth = 굵; 붓.strokeCap = Paint.Cap.ROUND
                붓.color = 강조글; 붓.alpha = 70
                c.drawArc(고리칸, 0f, 360f, false, 붓)
                붓.alpha = 255
                // 10-11 홍겸 님: 초마다 뚝뚝 줄지 않고 매끄럽게 — 남은 시간을 밀리초로 계산한다 (끝시각이 없으면 초 단위)
                val 남밀리 = if (v.쉼끝시각 > 0L) (v.쉼끝시각 - System.currentTimeMillis()).coerceAtLeast(0L).toFloat() else 남 * 1000f
                val 비 = if (v.쉼총초 > 0) (남밀리 / (v.쉼총초 * 1000f)).coerceIn(0f, 1f) else 1f
                // 10-09 홍겸 님: 시계 방향으로 줄어든다 — 끝은 12시에 두고 시작점이 시계 방향으로 따라간다
                c.drawArc(고리칸, -90f + 360f * (1f - 비), 360f * 비, false, 붓)
                val 글 = "%d:%02d".format(남 / 60, 남 % 60)
                c.drawText(글, cx, cy - (글붓.descent() + 글붓.ascent()) / 2, 글붓)
            } else {
                // 운동 중 — 바깥으로 퍼지며 옅어지는 고리 (누를 수 있다는 표시) + 앱 아이콘
                val t = (SystemClock.uptimeMillis() % 동그라미치수.한바퀴).toFloat() / 동그라미치수.한바퀴
                val 퍼짐 = (동그라미치수.칸 / 2f * 밀도 - r)
                붓.style = Paint.Style.STROKE; 붓.strokeWidth = 동그라미치수.고리 * 밀도
                붓.color = 강조; 붓.alpha = ((1f - t) * 200).toInt()
                c.drawCircle(cx, cy, r + 퍼짐 * t, 붓)
                // 10-11 홍겸 님: 로고는 크기 고정 — 로고를 감싼 동그라미(바깥)만 숨 쉬듯 줄었다 커진다
                val 숨 = 1f + 동그라미치수.숨폭 / 2 + 동그라미치수.숨폭 / 2 * sin(t * 2 * PI).toFloat()   // 1.0 ~ 1.05 — 로고 가장자리(1.0)보다 작아지지 않는다
                붓.style = Paint.Style.FILL; 붓.color = 강조; 붓.alpha = 255
                c.drawCircle(cx, cy, r * 숨, 붓)
                c.save()
                동그길.reset(); 동그길.addCircle(cx, cy, r, Path.Direction.CW)
                c.clipPath(동그길)
                그림?.let { it.setBounds((cx - r).toInt(), (cy - r).toInt(), (cx + r).toInt(), (cy + r).toInt()); it.draw(c) }
                c.restore()
            }
        }

        // ── 끌어서 옮기기 · 누르면 앱으로 ──
        private val 틈 = ViewConfiguration.get(ctx).scaledTouchSlop
        private var 아래x = 0f; private var 아래y = 0f; private var 처음x = 0; private var 처음y = 0; private var 끌었 = false

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(e: MotionEvent): Boolean {
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> { 아래x = e.rawX; 아래y = e.rawY; 처음x = p.x; 처음y = p.y; 끌었 = false }
                MotionEvent.ACTION_MOVE -> {
                    val dx = e.rawX - 아래x; val dy = e.rawY - 아래y
                    if (!끌었 && (abs(dx) > 틈 || abs(dy) > 틈)) 끌었 = true
                    if (끌었) { p.x = 처음x + dx.toInt(); p.y = 처음y + dy.toInt(); try { wm.updateViewLayout(this, p) } catch (_: Exception) { } }
                }
                MotionEvent.ACTION_UP -> if (!끌었) {
                    try {
                        context.startActivity(Intent(context, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP))
                    } catch (_: Exception) { }
                    치움()
                }
            }
            return true
        }
    }
}

/** 떠 있는 동그라미 치수 (dp · ms) [11_UI지침에 올릴 값] */
private object 동그라미치수 {
    const val 칸 = 95f        // 창 크기 (퍼지는 고리 자리 포함) · 10-11 홍겸 님 20% 키운 뒤 5% 줄임 (100 →)
    const val 반지름 = 34.2f  // 동그라미 · 5% 줄임 (36 →)
    const val 고리 = 3.4f     // 퍼지는 고리 굵기 · 5% 줄임 (3.6 →)
    const val 글 = 19f        // 남은 시간 글자 (sp 처럼 쓴다) · 5% 줄임 (20 →)
    const val 띠늘림 = 1.25f  // 휴식 줄어드는 띠 굵기 배수 — 바깥 가장자리는 그대로, 안쪽으로만 굵어진다 (10-11 홍겸 님 · 바깥 동그라미 숨쉬기가 보이게)
    const val 숨폭 = 0.05f    // 바깥 동그라미 숨쉬기 — 1.0배 ~ 1.05배 (10-11 홍겸 님)
    const val 옆 = 12f        // 처음 자리 — 오른쪽에서
    const val 처음위 = 160f   // 처음 자리 — 위에서
    const val 한바퀴 = 1600L  // 퍼지는 고리 한 번
    const val 틱 = 50L        // 다시 그리는 틈
}
