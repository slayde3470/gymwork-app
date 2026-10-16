package com.slayde.hasenheide

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper

/**
 * 앱 밖 안내 (10-13 홍겸 님) — 앱을 벗어나 동그라미를 꾹 눌렀을 때, 휴식이 끝났을 때 화면 **위쪽 알림 배너**(안드로이드 알림)로 한 줄을 알린다.
 * 직접 그린 팝업은 없앴다 — 알림 배너는 기본으로 우리 앱 아이콘이 앞에 붙고 모양이 폰마다 익숙하다.
 *  · 같은 번호로 다시 띄우면 새로 쌓지 않고 글만 바뀐다 → 빠르게 눌러도 항상 마지막 글이 보인다
 *  · 알림은 [보임ms] 뒤 저절로 거둔다 (새 종목으로 들어갈 때([길게])는 1.5배) — 배너가 떠 있는 시간은 폰이 정한다
 *  · 소리 · 진동은 없다 (진동 · 소리는 휴식 알람이 따로 낸다). 알림 권한을 거절했으면 뜨지 않는다
 *  · [휴식끝] = 휴식 알람의 '휴식 끝' 알림(번호 같음) 자리에 다음 세트 안내를 덮어 써서, 알림이 두 개 겹쳐 뜨지 않게 한다
 */
object 밖알림 {
    private const val 채널 = "밖안내"
    private const val 번호 = 8
    private const val 보임ms = 2500L   // 알림이 남아 있는 시간 [추측: 폰에서 보고 조절]
    private val 손 = Handler(Looper.getMainLooper())

    fun 띄움(ctx: Context, 글: String, 길게: Boolean = false, 휴식끝: Boolean = false) {
        if (글.isBlank()) return
        val app = ctx.applicationContext
        if (휴식끝) {
            // 휴식 알람 수신기가 같은 순간 '휴식 끝' 알림을 올린다 → 그것보다 늦게 올려 덮어쓴다
            손.postDelayed({ 올림(app, 글, 길게, 휴식알람.알림번호) }, 700L)
        } else {
            올림(app, 글, 길게, 번호)
        }
    }

    private fun 올림(app: Context, 글: String, 길게: Boolean, 번: Int) {
        try {
            val nm = app.getSystemService(NotificationManager::class.java) ?: return
            val ch = NotificationChannel(채널, "운동 안내", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "앱 밖에서 세트를 체크하거나 휴식이 끝났을 때 다음 세트를 알립니다"
                setSound(null, null)
                enableVibration(false)
                setShowBadge(false)
            }
            nm.createNotificationChannel(ch)
            val 열기 = PendingIntent.getActivity(
                app, 0,
                Intent(app, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            val n = Notification.Builder(app, 채널)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(글)   // 10-14: 폰의 위쪽 배너는 제목 · 내용을 한 줄로 이어 보여 줘서 둘로 나눠도 소용없었다 — 한 줄 하나로
                .setShowWhen(false)
                .setCategory(Notification.CATEGORY_STATUS)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(열기)
                .setAutoCancel(true)
                .setTimeoutAfter(if (길게) (보임ms * 1.5f).toLong() else 보임ms)
                .build()
            nm.notify(번, n)
        } catch (_: Exception) { }   // 알림 권한을 거절했으면 알림만 빠진다
    }

    /** 10-15: 잠금 화면 '운동 중' 알림(미디어 칸)은 빼기로 했다 — 이미 설치된 앱이 남긴 알림 · 채널을 한 번 치운다 */
    fun 옛운동알림정리(ctx: Context) {
        try {
            val nm = ctx.getSystemService(NotificationManager::class.java) ?: return
            nm.cancel(9)
            nm.deleteNotificationChannel("운동중")
            nm.deleteNotificationChannel("운동중2")
        } catch (_: Exception) { }
    }

    /** 앱으로 돌아오면 남은 안내를 치운다 (휴식 끝 자리 알림은 휴식알람.알림치움이 거둔다) */
    fun 치움(ctx: Context) {
        손.removeCallbacksAndMessages(null)
        try { ctx.getSystemService(NotificationManager::class.java)?.cancel(번호) } catch (_: Exception) { }
    }
}
