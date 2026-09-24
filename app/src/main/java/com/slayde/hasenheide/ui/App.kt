package com.slayde.hasenheide.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.mutableLongStateOf
import com.slayde.hasenheide.data.분초
import com.slayde.hasenheide.data.시분초
import com.slayde.hasenheide.data.흐른초
import com.slayde.hasenheide.data.휴식끝
import kotlin.math.max
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.slayde.hasenheide.data.앱데이터
import com.slayde.hasenheide.data.예정맞추기
import com.slayde.hasenheide.data.저장소
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.크기
import kotlinx.coroutines.delay
import java.io.File
import java.time.LocalDate
import java.util.concurrent.Executors

/**
 * 앱 전체의 상태를 쥐고 있는 곳.
 *
 * 자료는 '앱데이터' 하나. 고칠 때는 언제나 바꿈 { ... } 으로 새 값을 만들어 통째로 바꾸고,
 * 바꿀 때마다 폰 안의 파일에 조용히 저장한다 (저장은 뒤에서 — 화면이 멈추지 않게).
 */
class 앱상태(private val 파일: File) {
    var d by mutableStateOf(저장소.읽기(파일))
        private set
    var 오늘 by mutableStateOf(LocalDate.now().toString())
        private set

    private val 일꾼 = Executors.newSingleThreadExecutor()

    fun 바꿈(f: (앱데이터) -> 앱데이터) {
        d = f(d)
        val 찍은것 = d
        일꾼.execute { try { 저장소.쓰기(파일, 찍은것) } catch (_: Exception) { } }
    }

    /** 지우기는 묻지 않고 바로 — 5초 동안 되돌리기 (1-3) */
    var 되돌림 by mutableStateOf<Pair<String, 앱데이터>?>(null)
        private set
    fun 지우고알림(글: String, f: (앱데이터) -> 앱데이터) { val 전 = d; 바꿈(f); 되돌림 = 글 to 전 }
    fun 되돌리기() { val r = 되돌림 ?: return; 바꿈 { r.second }; 되돌림 = null }
    fun 되돌림치움() { 되돌림 = null }

    /** 날짜가 바뀌었는지 — 바뀌었으면 빠진 날을 반영해 예정을 맞춘다 */
    fun 날짜확인() {
        val 지금 = LocalDate.now().toString()
        if (지금 != 오늘 || d.예정.isEmpty()) { 오늘 = 지금 }
        바꿈 { it.예정맞추기(오늘) }
    }

    /** 백업 — 파일로 내보내고 가져온다 */
    fun 백업글(): String = 저장소.글로(d)
    fun 백업넣기(글: String): Boolean = try { val 새 = 저장소.글에서(글); 바꿈 { 새 }; true } catch (_: Exception) { false }
}

/** 폰이 해 주는 일(진동 · 소리 · 파일)을 화면 쪽에 넘겨주는 통로 */
class 폰기능(
    val 알림: () -> Unit,
    val 내보내기: () -> Unit,
    val 가져오기: () -> Unit,
    val 복사: (String) -> Unit,
    /** 참고 링크 열기 (09-24) */
    val 링크열기: (String) -> Unit,
)

enum class 탭(val 이름: String, val 그림: ImageVector) {
    캘린더("캘린더", 아이콘.달력), 루틴("루틴", 아이콘.루틴), 종목("종목", 아이콘.바벨), 설정("설정", 아이콘.톱니)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun 앱(상태: 앱상태, 폰: 폰기능) {
    val c = Local색.current
    var 지금탭 by remember { mutableStateOf(탭.캘린더) }
    var 메모열림 by remember { mutableStateOf(false) }
    // 운동 중이어도 다른 탭을 볼 수 있다 — 운동은 그대로 이어지고, 위의 띠로 돌아온다 (09-21 메모)
    var 운동보기 by remember { mutableStateOf(true) }
    val 세션 = 상태.d.세션
    LaunchedEffect(세션?.시작시각) { if (세션 != null) 운동보기 = true }

    // 앱을 켤 때 한 번, 그 뒤로 1분마다 날짜가 바뀌었는지 본다
    LaunchedEffect(Unit) { while (true) { 상태.날짜확인(); delay(60_000) } }
    // 되돌리기 띠는 5초 뒤 사라진다
    LaunchedEffect(상태.되돌림) { if (상태.되돌림 != null) { delay(5_000); 상태.되돌림치움() } }
    // 휴식 시계 — 다른 탭을 보고 있어도 돈다. 끝나면 알리고 다음으로
    LaunchedEffect(Unit) {
        while (true) {
            val 지금 = System.currentTimeMillis()
            val h = 상태.d.세션?.휴식
            if (h != null && !h.물음 && 지금 >= h.끝시각) {
                if (상태.d.설정.소리진동) 폰.알림()
                상태.바꿈 { dd -> dd.세션?.let { dd.copy(세션 = it.휴식끝(dd.설정, 지금)) } ?: dd }
            }
            delay(250)
        }
    }

    val 운동화면중 = 세션 != null && 운동보기
    val 화면이름 = if (운동화면중) "운동 중" else 지금탭.이름
    // 자판이 떠 있거나 숫자를 고치는 중이면 아래 탭을 숨긴다 (09-21 메모)
    val 탭숨김 = WindowInsets.isImeVisible || 입력중.수 > 0

    // 뒤로가기 (09-22 메모) — 숫자를 고치는 중이면 취소(숫자칸이 먼저 받는다), 시트면 닫기, 펼친 칸이면 접기(각 화면),
    // 그 밖에는 여기: 운동 중에 다른 탭 → 운동 화면 / 운동 화면 · 다른 탭 → 캘린더 / 캘린더 → 앱 나가기
    BackHandler(enabled = (세션 != null && !운동보기) || 운동화면중 || 지금탭 != 탭.캘린더) {
        when {
            세션 != null && !운동보기 && 지금탭 != 탭.캘린더 -> 운동보기 = true
            운동화면중 -> { 운동보기 = false; 지금탭 = 탭.캘린더 }
            else -> 지금탭 = 탭.캘린더
        }
    }

    // 빈 곳을 누르면 고치던 숫자칸을 취소한다 (09-22 메모). 버튼 · 칸이 받은 누름은 여기까지 오지 않는다
    Box(Modifier.fillMaxSize().background(c.바탕).pointerInput(Unit) { detectTapGestures(onTap = { 입력중.취소?.invoke() }) }) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (운동화면중) 운동화면(상태, 폰)
                else when (지금탭) {
                    탭.캘린더 -> 캘린더화면(상태, { 지금탭 = 탭.루틴 }, { 운동보기 = true })
                    탭.루틴 -> 루틴화면(상태, 폰)
                    탭.종목 -> 종목화면(상태)
                    탭.설정 -> 설정화면(상태, 폰)
                }
            }
            if (세션 != null && !운동보기) 운동중띠(세션) { 운동보기 = true }
            if (!탭숨김) {
                // 아래 탭 — 예전보다 25% 낮게 (09-21 메모). 맨 오른쪽은 '수정 메모'
                Row(
                    Modifier.fillMaxWidth().background(c.면).padding(top = 1.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    탭.entries.forEach { t ->
                        탭단추(t.이름, t.그림, !운동화면중 && 지금탭 == t) { 발자취.적기("${t.이름} 탭"); 지금탭 = t; 운동보기 = false }
                    }
                    탭단추("메모", 아이콘.연필, 메모열림) { 메모열림 = true }
                }
            }
        }
        if (메모열림) 메모시트(상태, 화면이름, 폰) { 메모열림 = false }
        // 되돌리기 띠는 맨 위에 — 메모 시트에서 지워도 보이게 (09-22 메모: 메모를 실수로 지웠는데 되돌릴 길이 안 보였다)
        상태.되돌림?.let { (글자, _) -> 아래띠(글자, "되돌리기", { 상태.되돌리기() }, 바깥 = Modifier.navigationBarsPadding().padding(bottom = if (탭숨김 || 메모열림) 0.dp else 46.dp)) }
    }
}

/** 다른 탭을 보는 동안 탭 위에 뜨는 띠 — 누르면 운동 화면으로 */
@Composable
private fun 운동중띠(S: com.slayde.hasenheide.data.운동세션, 돌아가기: () -> Unit) {
    val c = Local색.current
    var 지금 by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { 지금 = System.currentTimeMillis(); delay(1000) } }
    val h = S.휴식
    val 곁 = if (h != null && !h.물음) "휴식 ${분초(max(0L, (h.끝시각 - 지금 + 999) / 1000).toInt())}" else 시분초(S.흐른초(지금))
    Row(
        Modifier.fillMaxWidth().background(c.강조).눌림(돌아가기).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        글("운동 중 · ${S.루틴이름} · $곁", Modifier.weight(1f), 크기값 = 크기.버튼, 색 = c.강조글, 굵기 = FontWeight.Bold)
        글("돌아가기", 크기값 = 크기.버튼, 색 = c.강조글, 굵기 = FontWeight.Bold)
        Icon(아이콘.오른쪽, null, Modifier.size(16.dp), tint = c.강조글)
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.탭단추(이름: String, 그림: ImageVector, 켬: Boolean, onClick: () -> Unit) {
    val c = Local색.current
    Column(
        Modifier.weight(1f).눌림(onClick).padding(top = 4.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(그림, 이름, Modifier.size(18.dp), tint = if (켬) c.강조 else c.흐림)
        Box(Modifier.height(2.dp))
        글(이름, 크기값 = 크기.아주작게, 색 = if (켬) c.강조 else c.흐림, 굵기 = if (켬) FontWeight.Bold else FontWeight.Medium)
    }
}
