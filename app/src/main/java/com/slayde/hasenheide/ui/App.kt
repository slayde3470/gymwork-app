package com.slayde.hasenheide.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import com.slayde.hasenheide.ui.theme.움직임
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import com.slayde.hasenheide.data.남은초
import com.slayde.hasenheide.data.오래된운동정리
import com.slayde.hasenheide.data.보고끝
import com.slayde.hasenheide.data.업적판정법
import com.slayde.hasenheide.data.저장소
import com.slayde.hasenheide.data.사전채움
import com.slayde.hasenheide.data.세기이름
import com.slayde.hasenheide.data.세기더함
import com.slayde.hasenheide.data.세기0
import com.slayde.hasenheide.data.스탯기록남김
import com.slayde.hasenheide.data.업적갱신
import com.slayde.hasenheide.data.업적글
import com.slayde.hasenheide.data.업적살필것
import com.slayde.hasenheide.data.체중기록시작
import com.slayde.hasenheide.data.스탯표
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.크기
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.모서리
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.slayde.hasenheide.ui.theme.글꼴
import com.slayde.hasenheide.ui.theme.부품치수
import com.slayde.hasenheide.ui.theme.선굵기
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
    private val 읽은 = 저장소.읽기(파일)
    var d by mutableStateOf(읽은.사전채움())   // 10-09: 사전 종목을 한 번 채운다
        private set
    var 오늘 by mutableStateOf(LocalDate.now().toString())
        private set

    private val 일꾼 = Executors.newSingleThreadExecutor()

    init {
        // 10-09 감시관: 채웠으면 바로 파일에 — 아무것도 안 바꾸고 끄면 다음에 또 채우던 것
        // 빈 데이터(처음 · 못 읽은 파일)는 덮어쓰지 않는다 — 못 읽은 원본 자리를 켜자마자 바꾸지 않게
        if (d !== 읽은 && 읽은 != 앱데이터()) { val 찍은것 = d; 일꾼.execute { try { 저장소.쓰기(파일, 찍은것) } catch (_: Exception) { } } }
    }

    /**
     * 토스트 · 알림 띠 (10-05 · Parts.kt [알림판]) — 앱 어디서나 `상태.알림.토스트("…")` ·
     * `상태.알림.되돌림(묶음, { n -> 글 }) { 되돌리기 }`. App 맨 위 한 곳에서 그리므로 화면이 다시 그려져도 사라지지 않는다
     */
    val 알림 = 알림판()

    /** 스탯 화면 열기 (10-06 v22 D 13-3 — 보고서 › · 캘린더 띠 [스탯] 과 같은 화면). 앱() 이 채운다 */
    var 스탯열기: (() -> Unit)? = null

    fun 바꿈(f: (앱데이터) -> 앱데이터) {
        val 전 = d
        d = 업적살핌(전, f(전))
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
        // 10-02: 앱을 켤 때 · 날짜가 바뀔 때 — 스탯 하루 한 줄 · 업적 전부 (처음엔 지난 기록으로 소급)
        val 하루처음 = 지금 != 오늘 || !업적본날
        if (지금 != 오늘 || d.예정.isEmpty()) { 오늘 = 지금 }
        // 오래 손대지 않은 운동은 끝낸다 (09-25 메모) — 켤 때 한 번, 그 뒤 1분마다
        바꿈 { it.오래된운동정리(System.currentTimeMillis()).예정맞추기(오늘) }
        if (하루처음) { 업적본날 = true; 하루살핌() }
    }

    // ─────────────── 스탯 · 업적 (10-02) ───────────────

    /** 새로 얻은 업적 번호 — 아래띠로 알린다 (팝업 금지 · 11 지침 U5-3) */
    var 새업적 by mutableStateOf<List<String>>(emptyList())
        private set
    fun 새업적치움() { 새업적 = emptyList() }
    private var 업적본날 = false

    /** 앱 켤 때 · 날짜 바뀔 때 — 체중기록 첫 줄 · 스탯 하루 한 줄 · 업적 전부 */
    private fun 하루살핌() {
        val 지금 = System.currentTimeMillis()
        바꿈 { dd ->
            // 10-02 감시관: 판정 · 스탯 계산에서 예외가 나도 켤 때마다 앱이 죽지 않게 (업적살핌과 같게)
            try {
                val (x, 번호들) = dd.체중기록시작(지금).스탯기록남김(오늘).업적갱신(오늘, 지금)
                if (번호들.isNotEmpty()) 새업적 = 새업적 + 번호들
                x
            } catch (_: Exception) { dd }
        }
    }

    /**
     * 몸 수치(설정 '신체 정보')를 고친 뒤 — 스탯 하루 한 줄 · 체중 업적 (스탯명세 2-6).
     * 치는 동안은 부르지 않는다: 앱() 이 마지막으로 고친 뒤 [스탯표.체중묶음ms] 가 지나면 부른다
     */
    fun 몸살핌() {
        바꿈 { dd ->
            try {
                val (x, 번호들) = dd.스탯기록남김(오늘).업적갱신(오늘, System.currentTimeMillis(), 업적글.체중업적)
                if (번호들.isNotEmpty()) 새업적 = 새업적 + 번호들
                x
            } catch (_: Exception) { dd }
        }
    }

    /**
     * 바꿀 때마다 — 운동을 저장했으면 스탯을 남기고, 바뀐 칸을 쓰는 업적만 다시 본다 (업적살필것).
     * 얻은 업적은 어떤 경우에도 빼지 않는다 (되돌리기 · 백업 가져오기에도 — 스탯명세 5절 '취소 안 함')
     */
    private fun 업적살핌(전: 앱데이터, 새0: 앱데이터): 앱데이터 {
        var 새 = 새0
        if (!새.업적.keys.containsAll(전.업적.keys)) 새 = 새.copy(업적 = 전.업적 + 새.업적)
        // 10-06 v22 D: 보고서가 열릴 때 저장하면 세션이 남고(저장 표시가 새로 붙는다), 다시 끝내면 같은 열쇠에 덮어쓴다 — 둘 다 '저장'
        val 보고저장함 = 새.세션?.저장 != null && 새.세션?.저장 != 전.세션?.저장
        val 저장함 = 전.기록.keys != 새.기록.keys || 보고저장함
        return try {
            if (저장함) 새 = 새.스탯기록남김(오늘)
            val 볼 = (if (보고저장함) 업적판정법.keys else 업적살필것(전, 새)) ?: return 새
            // 방금 저장한 운동 — 세션으로만 보는 업적(2-33)
            val 방금 = if (보고저장함) 새.세션 else if (저장함 && 전.세션 != null && 새.세션 == null) 전.세션 else null
            val (x, 번호들) = 새.업적갱신(오늘, System.currentTimeMillis(), 볼, 방금)
            if (번호들.isNotEmpty()) 새업적 = 새업적 + 번호들
            x
        } catch (_: Exception) { 새 }   // 판정이 잘못돼도 기록 저장은 막지 않는다
    }

    /** 백업 — 파일로 내보내고 가져온다 */
    //  · 내보낼 때 수정 메모는 뺀다 (09-24 메모: 백업에 메모까지 담을 필요 없다)
    //  · 그래서 가져올 때 파일에 메모가 없으면 지금 폰의 메모를 그대로 둔다 (가져오기로 메모가 지워지지 않게)
    //  · 10-02: 종목 사진은 **파일 이름만** 들어간다 (사진 파일은 filesDir/photos 에 남고 백업에는 없다 → 다른 폰에선 빈 칸)
    fun 백업글(): String = 저장소.글로(d.copy(메모 = emptyList()))
    fun 백업넣기(글: String): Boolean = try {
        val 새 = 저장소.백업글에서(글) ?: throw IllegalArgumentException("백업 아님")   // 10-05: 백업이 아닌 JSON 은 받지 않는다 (아래 catch → false)
        바꿈 { 옛 -> (if (새.메모.isEmpty()) 새.copy(메모 = 옛.메모) else 새).사전채움() }; true
    } catch (_: Exception) { false }
}

/** 폰이 해 주는 일(진동 · 소리 · 파일)을 화면 쪽에 넘겨주는 통로 */
class 폰기능(
    val 알림: () -> Unit,
    val 내보내기: () -> Unit,
    val 가져오기: () -> Unit,
    val 복사: (String) -> Unit,
    /** 참고 링크 열기 (09-24) */
    val 링크열기: (String) -> Unit,
    /** 설정에서 진동 세기 · 길이를 고를 때 한 번 울려 보기 (09-25) */
    val 진동미리: () -> Unit = {},
    /** 10-08: 운동 중 다른 앱 위에 동그라미 — 권한이 켜져 있나 · 권한 화면 열기 */
    val 떠있기됨: () -> Boolean = { false },
    val 떠있기켜기: () -> Unit = {},
)

/**
 * 아래 탭의 화면 (10-05 시안 v21 탭줄 9칸). 줄 차례는 [탭줄차례] — '메모' 는 화면이 아니라 보던 화면 위에 메모 시트를 띄운다.
 * 스탯 · 업적은 탭이 아니다 — 캘린더 띠의 [스탯] 칩 · 업적 띠 [보기] 로 연다 (전과 같다)
 */
enum class 탭(val 이름: String, val 그림: ImageVector?) {
    캘린더("캘린더", 아이콘.달력), 검색("검색", 아이콘.돋보기), 루틴("루틴", 아이콘.루틴), 종목("종목", 아이콘.바벨), 플랜("플랜", 아이콘.과녁),
    소셜("소셜", 아이콘.사람), 설정("설정", 아이콘.톱니),
    /** 글자 없이 동그란 사진 (설정.프로필사진 · 없으면 닉네임 첫 글자 · 사람 그림) */
    프로필("프로필", null),
}

/** 탭줄 차례 — 시안 v21 `탭줄()`: 캘린더 · 검색 · 루틴 · 종목 · 플랜 · 메모 · 소셜 · 설정 · 프로필. null = 메모 */
val 탭줄차례: List<탭?> = listOf(탭.캘린더, 탭.검색, 탭.루틴, 탭.종목, 탭.플랜, null, 탭.소셜, 탭.설정, 탭.프로필)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun 앱(상태: 앱상태, 폰: 폰기능) {
    val c = Local색.current
    var 지금탭 by remember { mutableStateOf(탭.캘린더) }
    var 메모열림 by remember { mutableStateOf(false) }
    // 쓰던 메모는 시트를 닫아도 남는다 — 화면을 확인하고 돌아와 이어 쓴다 (09-24 메모)
    val 메모초안값 = remember { 메모초안() }
    // 운동 중이어도 다른 탭을 볼 수 있다 — 운동은 그대로 이어지고, 위의 띠로 돌아온다 (09-21 메모)
    var 운동보기 by remember { mutableStateOf(true) }
    // 10-02: 스탯 · 업적 화면 — 캘린더 맨 위 띠의 칩 · 업적 알림 '보기' 로 연다. 전체를 덮는다
    var 스탯열림 by remember { mutableStateOf<Pair<String, String?>?>(null) }   // (처음 쪽, 보여 줄 업적 번호)
    // 10-07 홍겸 님: 운동 보고서 › 로 열었나 — 스테이터스 ‹ › · 도전 과제 ‹ 단추가 붙는다
    var 스탯보고길 by remember { mutableStateOf(false) }
    val 세션 = 상태.d.세션
    LaunchedEffect(세션?.시작시각) { if (세션 != null) 운동보기 = true }
    // 10-06 v22 D 13-3 — 보고서 › : 캘린더 탭 위에 스탯 화면
    // 10-07 홍겸 님: 보고서 위에 띄운다 (탭 · 운동 화면은 그대로 — ‹ 로 닫으면 보고서가 다시 보인다)
    SideEffect { 상태.스탯열기 = { 스탯보고길 = true; 스탯열림 = 스탯화면글.스탯 to null } }

    // 앱을 켤 때 한 번, 그 뒤로 1분마다 날짜가 바뀌었는지 본다
    LaunchedEffect(Unit) { while (true) { 상태.날짜확인(); delay(60_000) } }
    // 10-02 감시관: 어느 종목에도 없는 사진 파일을 앱을 켤 때 치운다 (지운 종목 · 되돌리기를 기다리다 떠난 경우)
    val 앱맥락 = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        val 쓰는 = 상태.d.종목표.flatMap { it.사진 }.toSet() + listOfNotNull(상태.d.설정.프로필사진) + 상태.d.인증샷.map { it.파일 }   // 10-05: 프로필 사진 · 인증샷도 지키기
        // 기록 파일을 못 읽어 빈 데이터로 켜졌을 때는 치우지 않는다 (사진을 다 지우게 된다)
        if (상태.d.종목표.isNotEmpty()) kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { 사진함.정리(앱맥락, 쓰는) }
    }
    // 되돌리기 띠는 5초 뒤 사라진다
    LaunchedEffect(상태.되돌림) { if (상태.되돌림 != null) { delay(5_000); 상태.되돌림치움() } }
    // 업적 알림 띠도 5초 뒤 (10-02) — 되돌리기 띠에 가려 있는 동안은 세지 않는다 (감시관: 가린 채로 사라졌다)
    LaunchedEffect(상태.새업적, 상태.되돌림 == null) { if (상태.새업적.isNotEmpty() && 상태.되돌림 == null) { delay(5_000); 상태.새업적치움() } }
    // 10-02 감시관: 체중 칸은 글자마다 저장된다 → 치는 동안('109' 를 고치다 '10')은 업적을 보지 않고,
    //   마지막으로 고친 뒤 [스탯표.체중묶음ms] 가 지나면 본다 (체중기록이 한 줄로 묶이는 시간과 같다)
    val 몸값 = 상태.d.몸 to 상태.d.체중기록
    val 처음몸값 = remember { 몸값 }
    LaunchedEffect(몸값) { if (몸값 != 처음몸값) { delay(스탯표.체중묶음ms); 상태.몸살핌() } }
    // 휴식 시계 — 다른 탭을 보고 있어도 돈다. 끝나면 알리고 다음으로
    LaunchedEffect(Unit) {
        while (true) {
            val 지금 = System.currentTimeMillis()
            val h = 상태.d.세션?.휴식
            if (h != null && !h.물음 && 지금 >= h.끝시각) {
                if (상태.d.설정.소리진동) 폰.알림()
                // 10-02: 휴식이 끝까지 돌았다 → '연속 건너뛰기' 를 0 으로 (업적 2-17)
                상태.바꿈 { dd -> dd.세션?.let { dd.copy(세션 = it.휴식끝(dd.설정, 지금)).세기0(세기이름.연속건너뜀) } ?: dd }
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
            운동화면중 -> {
                // 10-01 감시관: 결과 화면에서 뒤로가기로 나가면 저장되지 않았다 (탭을 누를 때와 다르게)
                if (세션?.끝화면 == true) 상태.바꿈 { it.보고끝(상태.오늘, System.currentTimeMillis()) }   // 10-06 v22 D 이미 저장됨 → 세션만 닫음
                운동보기 = false; 지금탭 = 탭.캘린더
            }
            else -> 지금탭 = 탭.캘린더
        }
    }

    // 탭줄 높이(px) — 알림 띠 · 아래띠가 탭 위에 뜨게 (10-05: 9칸 탭줄은 전(46)보다 조금 높다 → 잰 값으로)
    var 탭줄높이 by remember { mutableIntStateOf(0) }
    val 밀도 = LocalDensity.current
    val 탭줄dp = with(밀도) { 탭줄높이.toDp() }
    // 빈 곳을 누르면 고치던 숫자칸을 취소한다 (09-22 메모). 버튼 · 칸이 받은 누름은 여기까지 오지 않는다
    // 10-05: 누른 자리를 알림판에 알린다 — 토스트 · 띠가 누른 단추 위에 뜬다 (누름은 가로채지 않는다)
    Box(Modifier.fillMaxSize().background(c.바탕).누름기억(상태.알림).pointerInput(Unit) { detectTapGestures(onTap = { 입력중.취소?.invoke() }) }) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                // 10-02: 화면을 바꿀 때 0.2초 동안 흐려지며 넘어간다 (자리 · 배치는 그대로)
                val 화면키 = when {
                    운동화면중 -> "운동"
                    세션 == null && 상태.d.결과 != null -> "결과"
                    else -> 지금탭.name
                }
                Crossfade(targetState = 화면키, animationSpec = tween(움직임.화면), label = "화면") { 키 ->
                    Box(Modifier.fillMaxSize()) {
                        val 결과 = 상태.d.결과
                        if (키 == "운동") 운동화면(상태, 폰)
                        // 10-01: 저장된 운동의 결과를 한 번 보여 준다 (자동 종료 뒤 결과 화면이 안 나왔다)
                        else if (키 == "결과") { if (결과 != null) 결과화면(상태, 결과) }
                        else when (탭.valueOf(키)) {
                            탭.캘린더 -> 캘린더화면(상태, { 지금탭 = 탭.루틴 }, { 운동보기 = true }, { 스탯보고길 = false; 스탯열림 = 스탯화면글.스탯 to null }, { 스탯보고길 = false; 스탯열림 = 스탯화면글.업적 to null })
                            탭.루틴 -> 루틴화면(상태, 폰)
                            탭.플랜 -> 플랜화면(상태, { 지금탭 = 탭.설정 }, { 지금탭 = 탭.종목 })
                            탭.종목 -> 종목화면(상태)
                            탭.설정 -> 설정화면(상태, 폰)
                            탭.검색 -> 검색화면(상태)
                            탭.소셜 -> 소셜화면(상태)
                            탭.프로필 -> 프로필화면(상태)
                        }
                    }
                }
                // 10-07 홍겸 님: 스테이터스 · 도전 과제는 화면 칸 안(탭줄 위)에 — 아래 탭이 보인다.
                // key: 이미 열려 있을 때 '보기' 를 눌러도 그 업적 쪽으로 다시 연다 (10-02 감시관)
                스탯열림?.let { (처음, 볼) -> key(처음, 볼, 스탯보고길) { 스탯화면(상태, 처음, 볼, 탭줄위 = true, 보고길 = 스탯보고길) { 스탯열림 = null } } }
            }
            if (세션 != null && !운동보기) 운동중띠(세션) { 운동보기 = true }
            if (!탭숨김) {
                // 아래 탭 — 10-05 시안 v21 탭줄 9칸 (위 테두리 1 선 · 칸 위 6 아래 8 · 그림 18 · 사이 2 · 글 11).
                // 메모 = 보던 화면 위에 메모 시트 · 프로필 = 글자 없이 동그란 사진 28 (켜지면 둘레 2 강조)
                Row(
                    Modifier.fillMaxWidth().height(IntrinsicSize.Min).background(c.면)
                        .drawBehind { drawRect(c.선, size = Size(size.width, 선굵기.보통.toPx())) }
                        .onSizeChanged { 탭줄높이 = it.height },
                ) {
                    탭줄차례.forEach { t ->
                        if (t == null) 탭단추("메모", 아이콘.연필, 메모열림) { 메모열림 = true } else {
                        val 켬 = !운동화면중 && 지금탭 == t
                        val 누름 = {
                            발자취.적기("${t.이름} 탭")
                            // 10-01: 운동을 다 끝내고(결과 화면) 다른 탭으로 나가면 그때 저장한다 —
                            //        저장 버튼을 안 눌렀다고 기록이 안 남던 것 ("운동 안 하고 넘어갔더라도 기록은 되어야")
                            if (상태.d.세션?.끝화면 == true) 상태.바꿈 { it.보고끝(상태.오늘, System.currentTimeMillis()) }   // 10-06 v22 D 이미 저장됨 → 세션만 닫음
                            if (상태.d.결과 != null) 상태.바꿈 { it.copy(결과 = null) }
                            // 10-02: 설정 탭을 연 수 (업적 2-46)
                            if (t == 탭.설정 && 지금탭 != 탭.설정) 상태.바꿈 { it.세기더함(세기이름.설정진입) }
                            지금탭 = t; 운동보기 = false; 스탯열림 = null   // 10-07: 스테이터스 · 도전 과제는 탭줄 위 — 탭을 누르면 닫힌다
                        }
                        val 그림 = t.그림
                        if (그림 != null) 탭단추(t.이름, 그림, 켬, 누름)
                        else Box(
                            Modifier.weight(1f).fillMaxHeight().눌림(누름).semantics { contentDescription = t.이름 },
                            contentAlignment = Alignment.Center,
                        ) { 프로필동그라미(상태.d.설정, 부품치수.탭사진, 고리 = 켬) }
                        }
                    }
                }
            }
        }
        // key: 이미 열려 있을 때 '보기' 를 눌러도 그 업적 쪽으로 다시 연다 (10-02 감시관)
        if (메모열림) 메모시트(상태, 화면이름, 폰, 메모초안값) { 메모열림 = false }
        // 되돌리기 띠는 맨 위에 — 메모 시트에서 지워도 보이게 (09-22 메모: 메모를 실수로 지웠는데 되돌릴 길이 안 보였다)
        상태.되돌림?.let { (글자, _) -> 아래띠(글자, "되돌리기", { 상태.되돌리기() }, 바깥 = Modifier.navigationBarsPadding().padding(bottom = if (탭숨김 || 메모열림) 0.dp else 탭줄dp)) }
        // 10-02: 업적 달성 알림 — 팝업 대신 아래띠 (U5-3). '보기' 를 누르면 업적 화면
        if (상태.새업적.isNotEmpty() && 상태.되돌림 == null) {
            key(상태.새업적) {
                아래띠(업적글.알림(상태.새업적), "보기", { val 첫 = 상태.새업적.firstOrNull(); 상태.새업적치움(); 스탯보고길 = false; 스탯열림 = 스탯화면글.업적 to 첫 },
                    바깥 = Modifier.navigationBarsPadding().padding(bottom = if (탭숨김 || 메모열림) 0.dp else 탭줄dp))
            }
        }
        // 10-05: 토스트 · 알림 띠 — 맨 위. 띠 안 단추만 누름을 받는다
        알림자리(상태.알림, 아래여백 = (if (탭숨김) 0.dp else 탭줄dp) + 간격.보통)
    }
}

/** 다른 탭을 보는 동안 탭 위에 뜨는 띠 — 누르면 운동 화면으로 */
@Composable
private fun 운동중띠(S: com.slayde.hasenheide.data.운동세션, 돌아가기: () -> Unit) {
    val c = Local색.current
    var 지금 by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { 지금 = System.currentTimeMillis(); delay(1000) } }
    val h = S.휴식
    val 곁 = if (h != null && !h.물음) "휴식 ${분초(h.남은초(지금))}" else 시분초(S.흐른초(지금))
    Row(
        // 10-09 홍겸 님: 띠도 좌우 여백 12 · 모서리 8
        Modifier.padding(start = 간격.보통, end = 간격.보통, bottom = 간격.아주좁게).fillMaxWidth()
            .clip(RoundedCornerShape(모서리.작게)).background(c.강조).눌림(돌아가기).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        글("운동 중 · ${S.루틴이름} · $곁", Modifier.weight(1f), 크기값 = 크기.버튼, 색 = c.강조글, 굵기 = FontWeight.Bold)
        글("돌아가기", 크기값 = 크기.버튼, 색 = c.강조글, 굵기 = FontWeight.Bold)
        Icon(아이콘.오른쪽, null, Modifier.size(16.dp), tint = c.강조글)
    }
}

/** 탭 한 칸 — 시안 `.탭줄 button`: 위 6 · 아래 8 · 그림 18 · 사이 2 · 글 11 (켜면 강조 · 굵게). 9칸이라 글은 자르지 않고 자간 −0.04em (시안 v17 C ④) */
@Composable
private fun androidx.compose.foundation.layout.RowScope.탭단추(이름: String, 그림: ImageVector, 켬: Boolean, onClick: () -> Unit) {
    val c = Local색.current
    Column(
        Modifier.weight(1f).눌림(onClick).padding(top = 부품치수.탭위, bottom = 부품치수.탭아래),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(그림, 이름, Modifier.size(부품치수.탭그림), tint = if (켬) c.강조 else c.흐림)
        Box(Modifier.height(부품치수.탭사이))
        androidx.compose.material3.Text(
            이름, style = 글꼴.보통(크기.아주작게, if (켬) FontWeight.Bold else FontWeight.Medium).copy(letterSpacing = 부품치수.탭자간),
            color = if (켬) c.강조 else c.흐림, maxLines = 1, softWrap = false,
        )
    }
}
