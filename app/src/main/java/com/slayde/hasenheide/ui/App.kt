package com.slayde.hasenheide.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
)

enum class 탭(val 이름: String, val 그림: ImageVector) {
    캘린더("캘린더", 아이콘.달력), 루틴("루틴", 아이콘.루틴), 종목("종목", 아이콘.바벨), 설정("설정", 아이콘.톱니)
}

@Composable
fun 앱(상태: 앱상태, 폰: 폰기능) {
    val c = Local색.current
    var 지금탭 by remember { mutableStateOf(탭.캘린더) }
    var 메모열림 by remember { mutableStateOf(false) }

    // 앱을 켤 때 한 번, 그 뒤로 1분마다 날짜가 바뀌었는지 본다
    LaunchedEffect(Unit) { while (true) { 상태.날짜확인(); delay(60_000) } }
    // 되돌리기 띠는 5초 뒤 사라진다
    LaunchedEffect(상태.되돌림) { if (상태.되돌림 != null) { delay(5_000); 상태.되돌림치움() } }

    val 운동중 = 상태.d.세션 != null
    val 화면이름 = if (운동중) "운동 중" else 지금탭.이름

    Box(Modifier.fillMaxSize().background(c.바탕)) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (운동중) 운동화면(상태, 폰)
                else when (지금탭) {
                    탭.캘린더 -> 캘린더화면(상태) { 지금탭 = 탭.루틴 }
                    탭.루틴 -> 루틴화면(상태)
                    탭.종목 -> 종목화면(상태)
                    탭.설정 -> 설정화면(상태, 폰)
                }
            }
            // 아래 탭 — 운동 중에도 살아 있다. 맨 오른쪽은 '수정 메모'(어느 화면에서든)
            Row(
                Modifier.fillMaxWidth().background(c.면).padding(top = 1.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                탭.entries.forEach { t ->
                    탭단추(t.이름, t.그림, !운동중 && 지금탭 == t) {
                        if (운동중) return@탭단추       // 운동 중에는 '운동 끝내기'로만 나간다
                        지금탭 = t
                    }
                }
                탭단추("메모", 아이콘.연필, 메모열림) { 메모열림 = true }
            }
        }
        상태.되돌림?.let { (글자, _) -> 아래띠(글자, "되돌리기", { 상태.되돌리기() }, 바깥 = Modifier.navigationBarsPadding().padding(bottom = 62.dp)) }
        if (메모열림) 메모시트(상태, 화면이름, 폰) { 메모열림 = false }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.탭단추(이름: String, 그림: ImageVector, 켬: Boolean, onClick: () -> Unit) {
    val c = Local색.current
    Column(
        Modifier.weight(1f).눌림(onClick).padding(top = 9.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(그림, 이름, Modifier.size(21.dp), tint = if (켬) c.강조 else c.흐림)
        Box(Modifier.height(3.dp))
        글(이름, 크기값 = 크기.작게, 색 = if (켬) c.강조 else c.흐림, 굵기 = if (켬) FontWeight.Bold else FontWeight.Medium)
    }
}
