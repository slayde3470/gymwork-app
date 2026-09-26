package com.slayde.hasenheide

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
import android.media.AudioAttributes
import android.util.Rational
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
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
import com.slayde.hasenheide.ui.작은창
import com.slayde.hasenheide.ui.Local번호
import androidx.compose.runtime.CompositionLocalProvider
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

    /** 작은 창(PiP)으로 떠 있는 중인가 (09-25 메모) */
    private val 작은창중 = mutableStateOf(false)

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
            진동미리 = { 진동(상태.d.설정.진동세기, 상태.d.설정.진동시간) },
            복사 = { 글 ->
                val 판 = getSystemService(ClipboardManager::class.java)
                판?.setPrimaryClip(ClipData.newPlainText("수정 메모", 글))
                알림글("복사했습니다 · Claude 에게 붙여넣으세요")
            },
        )
        setContent {
            하젠하이데테마 {
                // 작은 창일 때도 앱은 그대로 살아 있어야 휴식 시계 · 알림이 돈다 → 앱 위에 작은 창 화면을 덮는다
                CompositionLocalProvider(Local번호 provides 상태.d.설정.번호보기) {
                    Box(Modifier.fillMaxSize()) {
                        앱(상태, 폰)
                        if (작은창중.value) 작은창(상태)
                    }
                }
            }
        }
    }

    /**
     * 홈 버튼 등으로 앱을 벗어날 때 — 운동 중이면 작은 창(PiP)으로 띄운다 (09-25 메모)
     * 쉬는 중이면 남은 휴식이, 아니면 '운동 중' 과 지금 종목이 보인다. 창을 누르면 앱으로 돌아온다
     */
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val S = 상태.d.세션 ?: return
        if (S.끝화면 || Build.VERSION.SDK_INT < 26) return
        try {
            enterPictureInPictureMode(PictureInPictureParams.Builder().setAspectRatio(Rational(16, 9)).build())
        } catch (_: Exception) { }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        작은창중.value = isInPictureInPictureMode
    }

    /** 휴식이 끝났을 때 — 진동 + 짧은 소리 */
    private fun 휴식끝알림() {
        진동(상태.d.설정.진동세기, 상태.d.설정.진동시간)
        try {
            val 소리 = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
            소리.startTone(ToneGenerator.TONE_PROP_BEEP2, 400)
            window.decorView.postDelayed({ 소리.release() }, 800)
        } catch (_: Exception) { }
    }

    /**
     * 진동 — 세기(1 약 · 2 중 · 3 강) · 길이(ms) (09-25 메모)
     * '알람' 용도로 울린다 — 앱이 뒤에 있을 때 일반 진동은 폰이 막는 경우가 있어서 (09-25 메모: 화면 밖에서 안 울렸다)
     * 세기를 조절하지 못하는 폰이면 길이만 바뀐다
     */
    private fun 진동(세기: Int, 길이: Int) {
        try {
            val v = getSystemService(Vibrator::class.java) ?: return
            if (!v.hasVibrator() || Build.VERSION.SDK_INT < 26) return
            val 크기 = if (v.hasAmplitudeControl()) when (세기) { 1 -> 70; 3 -> 255; else -> 160 } else VibrationEffect.DEFAULT_AMPLITUDE
            val 효과 = VibrationEffect.createOneShot(길이.toLong().coerceIn(100, 5000), 크기)
            val 용도 = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
            @Suppress("DEPRECATION")
            v.vibrate(효과, 용도)
        } catch (_: Exception) { }
    }

    private fun 알림글(글: String) = Toast.makeText(this, 글, Toast.LENGTH_SHORT).show()
}
