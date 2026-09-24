package com.slayde.hasenheide

import android.content.Intent
import android.net.Uri
import android.content.ClipData
import android.content.ClipboardManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.slayde.hasenheide.ui.앱
import com.slayde.hasenheide.ui.앱상태
import com.slayde.hasenheide.ui.폰기능
import com.slayde.hasenheide.ui.theme.하젠하이데테마
import java.io.File
import java.time.LocalDate

/**
 * 앱을 켰을 때 가장 먼저 실행되는 곳.
 *
 * 여기서는 '폰이 해 주는 일'만 한다 — 진동 · 소리 · 파일 고르기 · 복사.
 * 화면과 규칙은 ui/ 와 data/ 폴더에 있다.
 */
class MainActivity : ComponentActivity() {

    private val 상태 by lazy { 앱상태(File(filesDir, "hasenheide.json")) }

    /** 백업 내보내기 — 저장할 곳을 고르면 그 파일에 쓴다 */
    private val 내보내기창 = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@registerForActivityResult
        val 됐나 = try {
            contentResolver.openOutputStream(uri)?.use { it.write(상태.백업글().toByteArray()) }; true
        } catch (e: Exception) { false }
        알림글(if (됐나) "백업 파일을 저장했습니다" else "저장하지 못했습니다")
    }

    /** 백업 가져오기 — 파일을 고르면 지금 기록을 그 내용으로 바꾼다 */
    private val 가져오기창 = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        val 글 = try { contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) } } catch (e: Exception) { null }
        val 됐나 = 글 != null && 상태.백업넣기(글)
        알림글(if (됐나) "백업을 불러왔습니다" else "이 파일을 읽지 못했습니다")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 화면 가장자리(상태바·내비게이션바)까지 앱이 그려지게 한다
        enableEdgeToEdge()
        val 폰 = 폰기능(
            알림 = { 휴식끝알림() },
            내보내기 = { 내보내기창.launch("하젠하이데-백업-${LocalDate.now()}.json") },
            가져오기 = { 가져오기창.launch(arrayOf("application/json", "text/plain", "*/*")) },
            링크열기 = { 주소 ->
                val u = 주소.trim().let { if (it.startsWith("http")) it else "https://$it" }
                try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(u))) } catch (e: Exception) { 알림글("이 링크를 열지 못했습니다") }
            },
            복사 = { 글 ->
                val 판 = getSystemService(ClipboardManager::class.java)
                판?.setPrimaryClip(ClipData.newPlainText("수정 메모", 글))
                알림글("복사했습니다 · Claude 에게 붙여넣으세요")
            },
        )
        setContent {
            하젠하이데테마 { 앱(상태, 폰) }
        }
    }

    /** 휴식이 끝났을 때 — 진동 + 짧은 소리 */
    private fun 휴식끝알림() {
        try {
            val 진동 = getSystemService(Vibrator::class.java)
            if (진동 != null && 진동.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= 26) 진동.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 300), -1))
            }
        } catch (_: Exception) { }
        try {
            val 소리 = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
            소리.startTone(ToneGenerator.TONE_PROP_BEEP2, 400)
            window.decorView.postDelayed({ 소리.release() }, 800)
        } catch (_: Exception) { }
    }

    private fun 알림글(글: String) = Toast.makeText(this, 글, Toast.LENGTH_SHORT).show()
}
