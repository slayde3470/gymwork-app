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

    /** 앱으로 돌아오면 남은 안내를 치운다 (휴식 끝 자리 알림은 휴식알람.알림치움이 거둔다) */
    fun 치움(ctx: Context) {
        손.removeCallbacksAndMessages(null)
        try { ctx.getSystemService(NotificationManager::class.java)?.cancel(번호) } catch (_: Exception) { }
    }
}

/**
 * 운동 중 계속 남는 알림 (10-14 홍겸 님) — 앱을 벗어나 있는 동안 알림판 · **잠금 화면**에 남아서, 잠금을 풀지 않고 [세트 완료] / [휴식 끝] 단추를 누를 수 있다.
 * (다른 앱 위에 뜨는 동그라미는 안드로이드가 잠금 화면 위에는 못 그리게 막아 둔다)
 *  · 쉬는 중: 제목 = 다음 세트 안내 · 남은 시간은 알림 시계(폰이 알아서 줄어드는 숫자) · 단추 [휴식 끝]
 *  · 아니면: 제목 = 지금 할 세트 · 단추 [세트 완료]   · 운동이 다 끝나면: "모든 운동이 끝났습니다." (단추 없음)
 *  · 단추를 누르면 동그라미를 꾹 누른 것과 같다 ([단추눌림] = MainActivity 가 정한다)
 *  · 소리 · 진동 없는 조용한 알림 · 앱으로 돌아오면 치운다
 */
object 운동알림 {
    private const val 채널 = "운동중2"   // 10-15: 예전 "운동중"(조용함=LOW)은 폰의 잠금 화면에서 '무음 알림 숨기기'에 걸려 안 보였다 → 소리 없이 보통 중요도(DEFAULT)인 새 채널
    private const val 번호 = 9
    const val 동작 = "com.slayde.hasenheide.밖단추"
    var 단추눌림: (() -> Unit)? = null

    fun 올림(ctx: Context, 글: String, 쉼끝시각: Long?, 단추: String?) {
        val app = ctx.applicationContext
        val nm = try { app.getSystemService(NotificationManager::class.java) } catch (_: Exception) { null } ?: return
        try {
            try { nm.deleteNotificationChannel("운동중") } catch (_: Exception) { }
            val ch = NotificationChannel(채널, "운동 중", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "앱을 벗어나 있는 동안 지금 할 세트와 [세트 완료] · [휴식 끝] 단추를 보여 줍니다 (잠금 화면 포함)"
                setSound(null, null)
                enableVibration(false)
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            nm.createNotificationChannel(ch)
        } catch (_: Exception) { }
        // 10-14: 먼저 전부 넣은 알림을 올려 보고, 어딘가 걸려 터지면 단추 · 시계를 뺀 단순한 알림이라도 올린다 (그때는 오류 이름을 작은 글로 붙여 원인을 알 수 있게)
        // 10-15: 1순위 = 유튜브 같은 '미디어 알림' 모양 — 폰의 잠금 화면이 일반 알림을 숨겨도 미디어 조절 칸은 보여 준다
        try {
            nm.notify(번호, 미디어만듦(app, 글, 쉼끝시각, 단추))
            return
        } catch (_: Throwable) { }
        val 오류 = try {
            nm.notify(번호, 만듦(app, 글, 쉼끝시각, 단추, 단순 = false))
            return
        } catch (e: Throwable) { e.javaClass.simpleName + ":" + (e.message ?: "").take(60) }
        try {
            nm.notify(번호, 만듦(app, 글, null, null, 단순 = true, 오류 = 오류))
        } catch (_: Throwable) { }
    }

    private var 미디어세션: android.media.session.MediaSession? = null

    /** 미디어 알림 모양 — 글 = 제목, 단추 = 알림 단추. 미디어 세션(소리는 안 낸다)을 달아야 폰이 미디어 칸에 넣는다 */
    private fun 미디어만듦(app: Context, 글: String, 쉼끝시각: Long?, 단추: String?): Notification {
        val s = 미디어세션 ?: android.media.session.MediaSession(app, "운동중").also { 미디어세션 = it }
        s.setMetadata(
            android.media.MediaMetadata.Builder()
                .putString(android.media.MediaMetadata.METADATA_KEY_TITLE, 글)
                .putString(android.media.MediaMetadata.METADATA_KEY_ARTIST, "하젠하이데")
                .build()
        )
        s.setPlaybackState(
            android.media.session.PlaybackState.Builder()
                .setState(android.media.session.PlaybackState.STATE_PLAYING, android.media.session.PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1f)
                .setActions(0L)
                .build()
        )
        s.isActive = true
        val 열기 = PendingIntent.getActivity(
            app, 1,
            Intent(app, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val b = Notification.Builder(app, 채널)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(글)
            .setContentText("하젠하이데")
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setContentIntent(열기)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
        if (쉼끝시각 != null) b.setShowWhen(true).setWhen(쉼끝시각).setUsesChronometer(true).setChronometerCountDown(true)
        if (단추 != null) {
            val 누름 = PendingIntent.getBroadcast(
                app, 2,
                Intent(app, 운동알림수신기::class.java).setAction(동작).setPackage(app.packageName),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            val a = Notification.Action.Builder(android.graphics.drawable.Icon.createWithResource(app, R.mipmap.ic_launcher), 단추, 누름)
            if (android.os.Build.VERSION.SDK_INT >= 31) a.setAuthenticationRequired(false)
            b.addAction(a.build())
        }
        b.setStyle(Notification.MediaStyle().setMediaSession(s.sessionToken).setShowActionsInCompactView(*(if (단추 != null) intArrayOf(0) else intArrayOf())))
        return b.build()
    }

    private fun 만듦(app: Context, 글: String, 쉼끝시각: Long?, 단추: String?, 단순: Boolean, 오류: String? = null): Notification {
        val 열기 = PendingIntent.getActivity(
            app, 1,
            Intent(app, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val b = androidx.core.app.NotificationCompat.Builder(app, 채널)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(글)
            .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(열기)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setShowWhen(false)
        if (오류 != null) b.setContentText("오류 " + 오류)
        if (!단순) {
            if (쉼끝시각 != null) b.setShowWhen(true).setWhen(쉼끝시각).setUsesChronometer(true).setChronometerCountDown(true)
            if (단추 != null) {
                val 누름 = PendingIntent.getBroadcast(
                    app, 2,
                    Intent(app, 운동알림수신기::class.java).setAction(동작).setPackage(app.packageName),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
                val a = androidx.core.app.NotificationCompat.Action.Builder(R.mipmap.ic_launcher, 단추, 누름)
                // 안드로이드 12+ : 잠금을 풀지 않고도 누를 수 있게 (폰 설정에서 막혀 있으면 폰이 잠금 해제를 먼저 물을 수 있다)
                a.setAuthenticationRequired(false)
                b.addAction(a.build())
            }
        }
        return b.build()
    }

    fun 치움(ctx: Context) {
        try { ctx.getSystemService(NotificationManager::class.java)?.cancel(번호) } catch (_: Exception) { }
        try { 미디어세션?.release() } catch (_: Exception) { }
        미디어세션 = null
    }
}

/** 운동 중 알림의 [세트 완료] · [휴식 끝] 단추를 받는 곳 — 동그라미 꾹 누름과 같은 일을 한다 */
class 운동알림수신기 : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action != 운동알림.동작) return
        운동알림.단추눌림?.invoke()
    }
}
