package com.slayde.hasenheide

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
import android.media.AudioAttributes
import android.util.Rational
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import android.Manifest
import android.content.pm.PackageManager
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
import com.slayde.hasenheide.ui.근육그림미리읽기
import com.slayde.hasenheide.ui.알림안내창
import com.slayde.hasenheide.ui.Local번호
import androidx.compose.runtime.CompositionLocalProvider
import com.slayde.hasenheide.ui.앱상태
import com.slayde.hasenheide.ui.밖에서길게
import com.slayde.hasenheide.ui.밖보고서열기
import com.slayde.hasenheide.ui.운동다함
import com.slayde.hasenheide.ui.쉼남음
import com.slayde.hasenheide.ui.다음할세트
import com.slayde.hasenheide.ui.쉼끝글
import com.slayde.hasenheide.ui.다음시작글
import com.slayde.hasenheide.ui.세트안내글
import com.slayde.hasenheide.ui.쉬는종목
import com.slayde.hasenheide.data.세기더함
import com.slayde.hasenheide.data.세기이름
import com.slayde.hasenheide.data.남은초
import com.slayde.hasenheide.data.식구
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
        if (됐나) 상태.바꿈 { it.세기더함(세기이름.백업) }   // 10-02: 백업 내보내기 성공 수 (업적 2-47)
        알림글(if (됐나) "백업 파일을 저장했습니다" else "저장하지 못했습니다")
    }

    /** 백업 가져오기 — 파일을 고르면 지금 기록을 그 내용으로 바꾼다 */
    private val 가져오기창 = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        val 글 = try { contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) } } catch (e: Exception) { null }
        val 됐나 = 글 != null && 상태.백업넣기(글)
        알림글(if (됐나) "백업을 불러왔습니다" else "이 파일을 읽지 못했습니다")
    }

    /** 알림 권한 (안드로이드 13 이상) — 첫 휴식 때 한 번 묻는다. 거절해도 진동 · 소리는 난다 */
    private var 권한물음 = false
    /** 지금 걸어 둔 휴식 알람의 끝 시각 */
    private var 걸린끝: Long? = null
    /** 10-09: 처음 권한 물음에서 '켜기' → 알림 권한 답을 받은 뒤 이어서 '다른 앱 위에 표시' 화면을 연다 */
    private var 떠있기이어서 = false
    private val 알림권한창 = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (떠있기이어서) { 떠있기이어서 = false; 떠있기열기() }
    }
    private fun 떠있기열기() { if (!떠있는동그라미.됨(this)) 떠있는동그라미.권한열기(this) }

    /**
     * 10-09 홍겸 님: 앱을 처음 켤 때 권한을 켤지 한 번 묻는다.
     * '다른 앱 위에 표시'가 꺼져 있으면 휴식 시간이 PiP 로 뜨는데, 안드로이드 PiP 는 한 번에 하나뿐이라 유튜브 작은 창과 같이 못 뜬다.
     * 이미 둘 다 켜져 있으면 묻지 않는다. 답과 상관없이 한 번만 (폰 안 설정 파일에 기록 · 앱 기록 JSON 과 따로).
     */
    private val 처음물음 = mutableStateOf(false)
    private fun 알림됨() = Build.VERSION.SDK_INT < 33 || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    private fun 처음물음확인() {
        val 판 = getSharedPreferences("hasenheide_처음", MODE_PRIVATE)
        if (판.getBoolean("권한물음", false)) return
        판.edit().putBoolean("권한물음", true).apply()
        if (알림됨() && 떠있는동그라미.됨(this)) return
        처음물음.value = true
    }
    private fun 처음물음켜기() {
        처음물음.value = false
        if (!알림됨()) { 권한물음 = true; 떠있기이어서 = true; 알림권한창.launch(Manifest.permission.POST_NOTIFICATIONS) }
        else 떠있기열기()
    }
    private fun 알림권한묻기() {
        if (권한물음 || Build.VERSION.SDK_INT < 33) return
        권한물음 = true
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            알림권한창.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    /** 10-08: 떠 있는 동그라미 값 — 운동이 없거나 끝 화면이면 null (동그라미가 저절로 사라진다) */
    private fun 동그라미값(): 떠있는동그라미.값? {
        val S = 상태.d.세션 ?: return null
        if (S.끝화면) return null
        val h = S.휴식
        // 10-13 홍겸 님: 모든 운동이 끝나고 쉬는 시간도 끝났으면 운동보고서 아이콘
        val 보고서 = 운동다함(S) && !쉼남음(S, System.currentTimeMillis())
        // 10-13 홍겸 님: 지금(쉬는 중이면 다음에 할) 종목이 슈퍼세트면 동그라미 그림을 주황으로
        val 본 = (h?.다음i ?: h?.종목?.takeIf { it >= 0 } ?: S.i).coerceIn(0, maxOf(0, S.종목들.size - 1))
        val 슈퍼 = S.종목들.isNotEmpty() && S.식구(본).size > 1
        return if (h != null && !h.물음) 떠있는동그라미.값(h.남은초(System.currentTimeMillis()), h.총초, h.끝시각, 슈퍼, 보고서) else 떠있는동그라미.값(null, 0, 0L, 슈퍼, 보고서)
    }

    /**
     * 10-13 홍겸 님: 밖 동그라미를 꾹 누르면 — 쉬는 중이면 휴식 끝, 아니면 지금 세트 체크 (앱을 열지 않는다).
     * 앱이 뒤에 있으면 화면이 안 그려져 아래 휴식 알람 맞춤 효과가 늦게 돌 수 있으므로 여기서 바로 맞춘다.
     */
    private fun 동그라미길게() {
        val 글 = 상태.밖에서길게() ?: return
        val h = 상태.d.세션?.휴식
        알람맞추기(if (h != null && !h.물음 && 상태.d.설정.소리진동) h.끝시각 else null, 권한묻기 = false)
        if (글.글.isNotEmpty()) 밖알림.띄움(this, 글.글, 길게 = 글.새종목)   // 10-13: 화면 위 알림 배너 (Notice.kt)
        운동알림갱신()
    }

    private val 알림손 = android.os.Handler(android.os.Looper.getMainLooper())
    private val 알림갱신 = Runnable { 운동알림갱신() }

    /**
     * 10-14 홍겸 님: 앱 밖(잠금 화면 포함)에 남는 알림 — 지금 할 세트(또는 쉬는 중 다음 세트)와 [세트 완료] / [휴식 끝] 단추.
     * 쉬는 시간이 끝나는 때 한 번 더 고쳐 단추가 [세트 완료] 로 바뀐다
     */
    private fun 운동알림갱신() {
        알림손.removeCallbacks(알림갱신)
        val S = 상태.d.세션
        if (S == null || S.끝화면) { 운동알림.치움(this); return }
        val 지금 = System.currentTimeMillis()
        val 쉬는 = 쉼남음(S, 지금)
        if (운동다함(S) && !쉬는) { 운동알림.올림(this, "모든 운동이 끝났습니다.", null, null); return }
        val 글 = 다음할세트(S)?.let { (j, k) -> 세트안내글(상태.d, S, j, k) } ?: ""
        if (쉬는) {
            val h = S.휴식
            운동알림.올림(this, "다음 · $글", h?.끝시각, "휴식 끝")
            if (h != null) 알림손.postDelayed(알림갱신, (h.끝시각 - 지금).coerceAtLeast(0L) + 800L)
        } else 운동알림.올림(this, "지금 · $글", null, "세트 완료")
    }

    /** 10-13 홍겸 님: 쉬는 시간이 저절로 끝났을 때(앱 밖) — 다음에 할 세트를 토스트로. 동그라미를 눌러 끝낸 것은 기록에 쉼이 이미 없어서 여기 오지 않는다 */
    private fun 동그라미쉼끝() {
        val S = 상태.d.세션 ?: return
        val h = S.휴식 ?: return
        if (S.끝화면 || h.끝시각 > System.currentTimeMillis()) return
        val 글 = 쉼끝글(S, 상태.d)
        if (글.글.isNotEmpty()) 밖알림.띄움(this, 글.글, 길게 = 글.새종목, 휴식끝 = true)   // 휴식 알람의 '휴식 끝' 알림 자리에 다음 세트 안내를 덮어 쓴다
        운동알림갱신()
    }

    /** 10-13 홍겸 님: 밖 동그라미의 운동보고서 아이콘을 눌러 들어왔으면 보고서 화면을 연다 */
    private fun 보고서요청확인(i: Intent?) {
        if (i?.getBooleanExtra(보고서열기, false) != true) return
        i.removeExtra(보고서열기)
        상태.밖보고서열기()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        보고서요청확인(intent)
    }

    companion object { const val 보고서열기 = "보고서열기" }

    /** 휴식 끝 시각에 알람을 걸고(null 이거나 지났으면 푼다) — 앱 화면 효과와 동그라미 꾹 누름이 같이 쓴다 */
    private fun 알람맞추기(알람끝: Long?, 권한묻기: Boolean = true) {
        if (알람끝 != null && 알람끝 > System.currentTimeMillis()) {
            if (권한묻기) 알림권한묻기()   // 뒤에 있을 때는 권한 창을 띄우지 않는다
            휴식알람.맞춤(this, 알람끝, 상태.d.설정.진동세기, 상태.d.설정.진동시간)
            걸린끝 = 알람끝
        } else {
            // 휴식을 일찍 끝냈을 때만 푼다 — 제시각에 끝난 것이면 알람이 울리게 둔다
            val 전 = 걸린끝
            if (전 != null && System.currentTimeMillis() < 전 - 1000) 휴식알람.끔(this)
            걸린끝 = null
        }
    }

    /** 운동 중 자동 작은 창(PiP) — 떠 있는 동그라미 권한이 있으면 PiP 대신 동그라미를 쓴다 */
    private fun 작은창설정() {
        if (Build.VERSION.SDK_INT < 31) return
        val 켬 = 상태.d.세션 != null && 상태.d.세션?.끝화면 != true && !떠있는동그라미.됨(this)
        try {
            setPictureInPictureParams(PictureInPictureParams.Builder().setAspectRatio(Rational(16, 9)).setAutoEnterEnabled(켬).build())
        } catch (_: Exception) { }
    }

    override fun onStart() {
        super.onStart()
        밖알림.치움(this)
        알림손.removeCallbacks(알림갱신)
        운동알림.단추눌림 = null
        운동알림.치움(this)
        떠있는동그라미.치움()
    }



    override fun onStop() {
        super.onStop()
        // 10-08 홍겸 님: 운동 중에 앱을 벗어나면 다른 앱 위에 동그라미 (유튜브 작은 창과 같이 뜬다)
        if (!isChangingConfigurations && !(Build.VERSION.SDK_INT >= 26 && isInPictureInPictureMode)) 떠있는동그라미.띄움(this, 길게 = { 동그라미길게() }, 쉼끝 = { 동그라미쉼끝() }) { 동그라미값() }
        // 10-14: 잠금 화면에도 남는 [세트 완료] / [휴식 끝] 알림 (동그라미는 잠금 화면 위에 못 뜬다)
        if (!isChangingConfigurations && !(Build.VERSION.SDK_INT >= 26 && isInPictureInPictureMode)) { 운동알림.단추눌림 = { 동그라미길게() }; 운동알림갱신() }
    }

    override fun onResume() {
        super.onResume()
        작은창설정()
        휴식알람.앞에있음 = true
        휴식알람.알림치움(this)
    }

    override fun onPause() {
        휴식알람.앞에있음 = false
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        근육그림미리읽기(this)   // 10-11: 첫 종목 펼침 렉 — 근육 그림을 뒤에서 미리 읽어 둔다
        // 화면 가장자리(상태바·내비게이션바)까지 앱이 그려지게 한다
        enableEdgeToEdge()
        처음물음확인()
        val 폰 = 폰기능(
            // 앱이 앞에 있을 때만 여기서 울린다 — 뒤에 있거나 화면이 꺼져 있으면 휴식알람이 울린다 (두 번 울리지 않게)
            알림 = { if (휴식알람.앞에있음) 휴식끝알림() },
            내보내기 = { 내보내기창.launch("하젠하이데-백업-${LocalDate.now()}.json") },
            가져오기 = { 가져오기창.launch(arrayOf("application/json", "text/plain", "*/*")) },
            링크열기 = { 주소 ->
                val u = 주소.trim().let { if (it.startsWith("http")) it else "https://$it" }
                try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(u))) } catch (e: Exception) { 알림글("이 링크를 열지 못했습니다") }
            },
            진동미리 = { 진동(상태.d.설정.진동세기, 상태.d.설정.진동시간) },
            떠있기됨 = { 떠있는동그라미.됨(this) },
            떠있기켜기 = { 떠있는동그라미.권한열기(this) },
            복사 = { 글 ->
                val 판 = getSystemService(ClipboardManager::class.java)
                판?.setPrimaryClip(ClipData.newPlainText("수정 메모", 글))
                알림글("복사했습니다 · Claude 에게 붙여넣으세요")
            },
        )
        보고서요청확인(intent)
        setContent {
            // 쉬기 시작하면 끝 시각에 알람을 걸고, 휴식이 끝나거나 운동을 마치면 푼다 (09-26 메모: 화면이 꺼져 있어도)
            val h = 상태.d.세션?.휴식
            val 알람끝 = if (h != null && !h.물음 && 상태.d.설정.소리진동) h.끝시각 else null
            LaunchedEffect(알람끝) { 알람맞추기(알람끝) }
            // 10-01: PiP 가 안 뜨던 것 — [추측] 안드로이드 12 이상 제스처 이동에서는 onUserLeaveHint 가
            //        늦게 오거나 안 와서 작은 창으로 못 들어간다. 운동 중이면 '자동으로 작은 창' 을 켜 둔다
            val 작은창켬 = 상태.d.세션 != null && 상태.d.세션?.끝화면 != true
            LaunchedEffect(작은창켬) { 작은창설정() }
            하젠하이데테마 {
                // 작은 창일 때도 앱은 그대로 살아 있어야 휴식 시계 · 알림이 돈다 → 앱 위에 작은 창 화면을 덮는다
                CompositionLocalProvider(Local번호 provides 상태.d.설정.번호보기) {
                    Box(Modifier.fillMaxSize()) {
                        앱(상태, 폰)
                        if (작은창중.value) 작은창(상태)
                        else if (처음물음.value) 알림안내창(on켜기 = { 처음물음켜기() }, on나중에 = { 처음물음.value = false })
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
        if (떠있는동그라미.됨(this)) return   // 10-08: 동그라미를 쓰면 PiP 는 띄우지 않는다 (onStop 이 띄운다)
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
