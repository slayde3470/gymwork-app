package com.slayde.hasenheide.ui

import android.app.Application
import android.app.NotificationManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slayde.hasenheide.MainActivity
import com.slayde.hasenheide.data.루틴
import com.slayde.hasenheide.data.루틴종목
import com.slayde.hasenheide.data.앱데이터
import com.slayde.hasenheide.data.운동시작
import com.slayde.hasenheide.data.저장소
import com.slayde.hasenheide.data.종목
import com.slayde.hasenheide.운동알림
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.io.File

/** 임시 진단 (screens/notif) — 운동 중 알림이 실제로 올라가는지 가상 폰에서 본다 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class Probe_알림 {
    private val sb = StringBuilder()
    private fun 본다(제목: String) {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val nm = app.getSystemService(NotificationManager::class.java)
        val 목록 = shadowOf(nm).allNotifications
        sb.appendLine("[$제목] 알림 ${목록.size}개: " + 목록.joinToString(" | ") { n ->
            (n.extras.getCharSequence("android.title") ?: "?").toString() + " / 글=" + n.extras.getCharSequence("android.text") + " / 단추=" + (n.actions?.map { it.title } ?: emptyList<CharSequence>()) + " / 미디어=" + (n.extras.getParcelable<android.os.Parcelable>("android.mediaSession") != null) + " / 채널=" + n.channelId
        })
    }

    @Test fun 알림() {
        try {
            val app = ApplicationProvider.getApplicationContext<Application>()
            // 1. 알림만 직접
            운동알림.올림(app, "지금 · 직접", null, "세트 완료")
            본다("직접 올림")
            운동알림.치움(app)
            // 2. 진짜 액티비티 — 운동 중 자료를 넣고 홈으로 나가는 순서
            val r = 루틴("rA", "상체 A", 종목 = listOf(루틴종목("벤치프레스", 4, 60.0, 8, 120), 루틴종목("스쿼트", 3, 80.0, 6, 90)))
            val S = 운동시작(r, System.currentTimeMillis() - 60_000)!!
            val d = 앱데이터(종목표 = listOf(종목("벤치프레스", "가슴", "바벨"), 종목("스쿼트", "하체", "바벨")), 루틴들 = listOf(r)).copy(세션 = S)
            저장소.쓰기(File(app.filesDir, "hasenheide.json"), d)
            sb.appendLine("자료 저장됨: " + File(app.filesDir, "hasenheide.json").length() + "바이트")
            val c = Robolectric.buildActivity(MainActivity::class.java)
            c.create(); sb.appendLine("create 됨")
            c.start().resume().visible(); 본다("앞에 있음")
            c.pause(); 본다("pause 뒤")
            c.stop(); 본다("stop 뒤")
            c.start().resume(); 본다("다시 앞으로")
        } catch (e: Throwable) {
            sb.appendLine("오류: " + e.javaClass.name + ": " + e.message)
            sb.appendLine(e.stackTrace.take(15).joinToString("\n"))
        }
        File("screens").mkdirs()
        File("screens/_알림.txt").writeText(sb.toString())
        println(sb)
    }
}
