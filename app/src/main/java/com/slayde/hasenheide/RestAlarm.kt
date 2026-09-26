package com.slayde.hasenheide

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator

/**
 * 화면이 꺼져 있어도 휴식 끝을 알린다 (09-26 메모).
 *
 * 쉬기 시작할 때 폰의 '알람 시계'에 끝 시각을 걸어 둔다 → 그 시각에 폰이 앱을 깨워
 * 진동 · 소리 · 알림을 낸다. 앱이 꺼져 있거나 화면이 꺼져(절전 중) 있어도 울린다.
 * 앱이 앞에 떠 있을 때는 앱 안의 휴식 시계가 알리므로 여기서는 울리지 않는다 (두 번 울리지 않게).
 */
object 휴식알람 {
    /** 앱 화면이 앞에 떠 있는가 — MainActivity 의 onResume/onPause 에서 바꾼다 */
    @Volatile var 앞에있음 = false

    private const val 채널 = "휴식끝"
    const val 알림번호 = 7

    private fun 걸이(ctx: Context, 세기: Int, 길이: Int, 소리: Boolean): PendingIntent =
        PendingIntent.getBroadcast(
            ctx, 1,
            Intent(ctx, RestAlarmReceiver::class.java).putExtra("세기", 세기).putExtra("길이", 길이).putExtra("소리", 소리),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun 앱열기(ctx: Context): PendingIntent =
        PendingIntent.getActivity(
            ctx, 2,
            Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    /** 끝 시각에 알람을 건다. 같은 알람이 있으면 새 시각으로 바뀐다 */
    fun 맞춤(ctx: Context, 끝시각: Long, 세기: Int, 길이: Int, 소리: Boolean = true) {
        try {
            val am = ctx.getSystemService(AlarmManager::class.java) ?: return
            val p = 걸이(ctx, 세기, 길이, 소리)
            val 정확 = Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()
            // 알람 시계 방식: 절전(Doze) 중에도 제시각에 울린다
            if (정확) am.setAlarmClock(AlarmManager.AlarmClockInfo(끝시각, 앱열기(ctx)), p)
            else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, 끝시각, p)   // 권한이 없으면 조금 늦을 수 있다
        } catch (_: Exception) { }
    }

    fun 끔(ctx: Context) {
        try {
            ctx.getSystemService(AlarmManager::class.java)?.cancel(걸이(ctx, 0, 0, true))
        } catch (_: Exception) { }
    }

    /** 알림판에 남은 '휴식 끝' 알림을 치운다 — 앱으로 돌아오면 */
    fun 알림치움(ctx: Context) {
        try { ctx.getSystemService(NotificationManager::class.java)?.cancel(알림번호) } catch (_: Exception) { }
    }

    fun 알림띄움(ctx: Context) {
        try {
            val nm = ctx.getSystemService(NotificationManager::class.java) ?: return
            // 진동 · 소리는 앱이 직접 낸다(설정의 세기 · 길이대로) → 채널은 조용히, 대신 화면 위로 뜨게(높음)
            val ch = NotificationChannel(채널, "휴식 끝", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "휴식이 끝나면 알립니다"
                setSound(null, null)
                enableVibration(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            nm.createNotificationChannel(ch)
            val n = Notification.Builder(ctx, 채널)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("휴식 끝")
                .setContentText("다음 세트를 시작하세요")
                .setCategory(Notification.CATEGORY_ALARM)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(앱열기(ctx))
                .setAutoCancel(true)
                .build()
            nm.notify(알림번호, n)
        } catch (_: Exception) { }   // 알림 권한을 거절했으면 알림만 빠지고 진동 · 소리는 난다
    }

    /** 진동 — 세기(1 약 · 2 중 · 3 강) · 길이(ms). '알람' 용도로 울려 화면이 꺼져 있어도 울린다 */
    fun 진동(ctx: Context, 세기: Int, 길이: Int) {
        try {
            val v = ctx.getSystemService(Vibrator::class.java) ?: return
            if (!v.hasVibrator()) return
            val 크기 = if (v.hasAmplitudeControl()) when (세기) { 1 -> 70; 3 -> 255; else -> 160 } else VibrationEffect.DEFAULT_AMPLITUDE
            val 효과 = VibrationEffect.createOneShot(길이.toLong().coerceIn(100, 5000), 크기)
            val 용도 = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
            @Suppress("DEPRECATION")
            v.vibrate(효과, 용도)
        } catch (_: Exception) { }
    }

    /** 짧은 삐 소리 — 다 울린 뒤 끝(release)을 부른다 */
    fun 소리(끝: () -> Unit = {}) {
        try {
            val t = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
            t.startTone(ToneGenerator.TONE_PROP_BEEP2, 400)
            Handler(Looper.getMainLooper()).postDelayed({ t.release(); 끝() }, 800)
        } catch (_: Exception) { 끝() }
    }
}

/** 알람 시각이 되면 폰이 부르는 곳 */
class RestAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (휴식알람.앞에있음) return          // 앱이 앞에 있으면 앱 안의 시계가 알린다
        val 세기 = intent.getIntExtra("세기", 2)
        val 길이 = intent.getIntExtra("길이", 1000)
        휴식알람.진동(ctx, 세기, 길이)
        휴식알람.알림띄움(ctx)
        if (intent.getBooleanExtra("소리", true)) {
            val 기다림 = goAsync()
            휴식알람.소리 { 기다림.finish() }
        }
    }
}
