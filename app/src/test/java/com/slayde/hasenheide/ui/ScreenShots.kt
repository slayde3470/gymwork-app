package com.slayde.hasenheide.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.click
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureScreenRoboImage
import com.slayde.hasenheide.data.날기록
import com.slayde.hasenheide.data.루틴
import com.slayde.hasenheide.data.루틴종목
import com.slayde.hasenheide.data.몸조건
import com.slayde.hasenheide.data.세트
import com.slayde.hasenheide.data.앱데이터
import com.slayde.hasenheide.data.예정맞추기
import com.slayde.hasenheide.data.운동시작
import com.slayde.hasenheide.data.세트추가
import com.slayde.hasenheide.data.저장소
import com.slayde.hasenheide.data.종목
import com.slayde.hasenheide.data.종목기록
import com.slayde.hasenheide.data.끝냄
import com.slayde.hasenheide.data.플랜만들기
import com.slayde.hasenheide.data.플랜폼값
import com.slayde.hasenheide.ui.theme.하젠하이데테마
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.LocalDate

/**
 * 화면 사진 (screens 가지 전용 — main 에는 넣지 않는다).
 * 지금 코드를 그대로 그려 화면마다 한 장씩 app/screens 폴더 로 남긴다.
 * 자료는 보기용 예시 (루틴 3개 · 지난 기록 5일 · 벤치 플랜 1개).
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w412dp-h891dp-xhdpi")
abstract class ShotBase {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    protected val 오늘 = LocalDate.now()

    /** 10-16: 한 걸음이 100초 넘게 멈추면 그때의 스레드 모습을 screens/_멈춤.txt 에 남기고 깨운다 (두 번째에도 안 풀리면 끊는다) — 한 판이 30분 멈춘 적이 있어서 */
    companion object {
        @Volatile var 마지막 = System.currentTimeMillis()
        @Volatile var 지금 = ""
        @Volatile var 본: Thread? = null
        private var 켰음 = false
        @Synchronized fun 지키기() {
            if (켰음) return
            켰음 = true
            Thread {
                var 횟수 = 0
                while (true) {
                    Thread.sleep(5000)
                    if (System.currentTimeMillis() - 마지막 > 100_000) {
                        횟수++
                        try {
                            val sb = StringBuilder("멈춤 $횟수 번째: $지금\n")
                            for ((t, st) in Thread.getAllStackTraces()) { sb.append("## ${t.name}\n"); st.take(30).forEach { sb.append("    $it\n") } }
                            File("screens/_멈춤.txt").apply { parentFile?.mkdirs() }.appendText(sb.toString() + "\n")
                        } catch (_: Throwable) { }
                        마지막 = System.currentTimeMillis()
                        if (횟수 >= 2) Runtime.getRuntime().halt(3) else 본?.interrupt()
                    }
                }
            }.apply { isDaemon = true; name = "지킴이" }.start()
        }
    }

    protected fun 예시(): 앱데이터 {
        val 종목들 = listOf(
            종목("벤치프레스", "가슴", "바벨"), 종목("인클라인 덤벨프레스", "가슴", "덤벨"),
            종목("오버헤드 프레스", "어깨", "바벨"), 종목("사이드 레터럴 레이즈", "어깨", "덤벨"),
            종목("스쿼트", "하체", "바벨"), 종목("루마니안 데드리프트", "하체", "바벨"), 종목("레그 프레스", "하체", "머신"),
            종목("풀업", "등", "맨몸"), 종목("바벨 로우", "등", "바벨"), 종목("랫풀다운", "등", "머신"),
        )
        val 루틴들 = listOf(
            루틴("rA", "상체 A", 종목 = listOf(루틴종목("벤치프레스", 4, 60.0, 8, 120), 루틴종목("인클라인 덤벨프레스", 3, 22.0, 10, 90),
                루틴종목("오버헤드 프레스", 3, 40.0, 8, 90), 루틴종목("사이드 레터럴 레이즈", 3, 8.0, 15, 60))),
            루틴("rB", "하체 B", 종목 = listOf(루틴종목("스쿼트", 4, 80.0, 6, 150), 루틴종목("루마니안 데드리프트", 3, 70.0, 8, 120),
                루틴종목("레그 프레스", 3, 140.0, 10, 90))),
            루틴("rC", "등 C", 종목 = listOf(루틴종목("풀업", 4, 0.0, 8, 120), 루틴종목("바벨 로우", 3, 60.0, 8, 90),
                루틴종목("랫풀다운", 3, 50.0, 12, 60), 루틴종목("케이블 트라이셉스 푸시다운", 3, 25.0, 12, 60))),
            루틴("rR", "휴식", 휴식일 = true)
        ).map { it.copy(자동생성 = true) }
        fun 기록(r: 루틴, 날: LocalDate): Pair<String, 날기록> {
            val 시작 = 날.atTime(19, 0).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            return 날.toString() to 날기록(r.id, r.이름, true,
                r.종목.map { e -> 종목기록(e.이름, List(e.세트) { 세트(e.무게, e.횟수) }) }, 3600, 시작, 시작 + 3_600_000)
        }
        val 기록들 = mapOf(
            기록(루틴들[0], 오늘.minusDays(6)), 기록(루틴들[1], 오늘.minusDays(5)), 기록(루틴들[2], 오늘.minusDays(4)),
            기록(루틴들[0], 오늘.minusDays(2)), 기록(루틴들[1], 오늘.minusDays(1)),
        )
        val 몸 = 몸조건(28, true, 75.0)
        val 플랜 = 플랜만들기(플랜폼값(종목 = "벤치프레스", 현재무게 = "60", 현재횟수 = "8", 목표무게 = "100"), 몸, emptyList(), "p1", 오늘.toString()).first
        val d = 앱데이터(종목표 = 종목들, 루틴들 = 루틴들, 기록 = 기록들, 몸 = 몸, 플랜들 = listOfNotNull(플랜))
        return d.copy(설정 = d.설정.copy(닉네임 = "홍겸")).예정맞추기(오늘.toString())
    }

    protected lateinit var 상태: 앱상태

    protected fun 켜기(업적둠: Boolean = false, 작은: Boolean = false, 바꾸기: (앱데이터) -> 앱데이터 = { it }) {
        // 달력 칸은 '뗀 지 300ms 안이면 누름 아님'을 SystemClock 으로 잰다 — 시험 시계가 0 근처면 첫 누름이 먹히지 않는다
        try { org.robolectric.shadows.ShadowSystemClock.advanceBy(java.time.Duration.ofSeconds(10)) } catch (_: Throwable) { }
        본 = Thread.currentThread(); 마지막 = System.currentTimeMillis(); 지금 = "켜기 ${this::class.simpleName}"; 지키기()
        val 파일 = File.createTempFile("hasenheide", ".json")
        저장소.쓰기(파일, 바꾸기(예시()))
        상태 = 앱상태(파일)
        val 폰 = 폰기능({}, {}, {}, {}, {})
        rule.mainClock.autoAdvance = false
        // 진짜 앱처럼 가장자리까지 그려야 Compose 가 '자판 없음'을 안다 (안 하면 자판이 떠 있다고 보고 탭줄을 숨긴다)
        rule.runOnUiThread { androidx.core.view.WindowCompat.setDecorFitsSystemWindows(rule.activity.window, false) }
        rule.setContent { 하젠하이데테마(어둡게 = false) { if (작은) 작은창(상태) else 앱(상태, 폰) } }
        if (업적둠) { 쉼(1500); return }
        쉼(1500); 상태.새업적치움(); 쉼(6000)
    }

    protected fun 쉼(ms: Long = 1200) { 마지막 = System.currentTimeMillis(); rule.mainClock.advanceTimeBy(ms); rule.waitForIdle(); rule.mainClock.advanceTimeBy(300) }

    /** 아래 탭줄 칸 (0 캘린더 · 1 검색 · 2 루틴 · 3 종목 · 4 플랜 · 5 메모 · 6 소셜 · 7 설정 · 8 프로필) */
    protected fun 탭(i: Int) {
        rule.onRoot().performTouchInput { click(Offset(width * (i + 0.5f) / 9f, height - 22f * 2f)) }
        쉼()
    }

    /** 못 찾았을 때 그때 화면과 칸 목록을 남긴다 (screens/_없음_*.png · _없음.txt) */
    protected fun 실패기록(무엇: String) {
        try { captureScreenRoboImage("screens/_없음_${this::class.simpleName}.png") } catch (_: Throwable) { }
        try {
            val 줄 = rule.onAllNodes(isRoot(), useUnmergedTree = true).fetchSemanticsNodes().flatMap { r ->
                val out = ArrayList<String>()
                fun 걷(n: androidx.compose.ui.semantics.SemanticsNode) {
                    val t = n.config.getOrNull(SemanticsProperties.Text)?.joinToString("|") { it.text }
                    val d = n.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString("|")
                    if (t != null || d != null) out.add("  ${n.boundsInRoot.top.toInt()},${n.boundsInRoot.bottom.toInt()} t=$t d=$d")
                    n.children.forEach { 걷(it) }
                }
                걷(r); out
            }
            File("screens/_없음.txt").apply { parentFile?.mkdirs() }.appendText("## ${this::class.simpleName} $무엇\n" + 줄.take(160).joinToString("\n") + "\n")
        } catch (_: Throwable) { }
    }

    /** 목록(Lazy)에 아직 안 그려진 칸을 찾아 목록을 밀어 준다 — 맨 위 칸 말고 마지막 목록부터 (10-16) */
    protected fun 찾아밀기(맞는것: androidx.compose.ui.test.SemanticsMatcher): Boolean {
        try {
            val 목록 = rule.onAllNodes(androidx.compose.ui.test.hasScrollAction())
            val n = 목록.fetchSemanticsNodes().size
            for (k in n - 1 downTo 0) {
                try { 목록[k].performScrollToNode(맞는것); 쉼(300); return true } catch (_: Throwable) { }
            }
        } catch (_: Throwable) { }
        return false
    }

    /** 칸이 화면 밖이면 위로 밀어 올린다 (최대 4번). performScrollTo 는 종목 고르기 시트에서 끝없이 돌아 한 판이 멈췄다 (10-16) */
    protected fun 보이게(칸: androidx.compose.ui.test.SemanticsNodeInteraction) {
        val 높이 = rule.activity.resources.displayMetrics.heightPixels
        var i = 0
        while (i < 4) {
            val b = 칸.fetchSemanticsNode().boundsInRoot
            if (b.top >= 0f && b.bottom <= 높이) break
            i++
            val 뿌리 = rule.onAllNodes(isRoot())
            뿌리[뿌리.fetchSemanticsNodes().size - 1].performTouchInput { swipeUp(startY = height * 0.75f, endY = height * 0.25f) }
            쉼(600)
        }
    }

    /** 글자로 찾아 누름 — 같은 글이 여럿이면 [몇째] (−1 = 마지막) */
    protected fun 누름(글: String, 몇째: Int = 0, 일부: Boolean = false) {
        지금 = "${this::class.simpleName} 누름 '$글'"; 마지막 = System.currentTimeMillis()
        var 들 = rule.onAllNodesWithText(글, substring = 일부, useUnmergedTree = true)
        var 밀기 = 0
        if (들.fetchSemanticsNodes().isEmpty()) { 찾아밀기(hasText(글, substring = 일부)); 들 = rule.onAllNodesWithText(글, substring = 일부, useUnmergedTree = true) }
        while (들.fetchSemanticsNodes().isEmpty() && 밀기 < 8) { 밀기++; rule.onAllNodes(isRoot())[0].performTouchInput { swipeUp(startY = height * 0.75f, endY = height * 0.25f) }; 쉼(); 들 = rule.onAllNodesWithText(글, substring = 일부, useUnmergedTree = true) }
        val n = 들.fetchSemanticsNodes().size
        if (n == 0) 실패기록("누름 $글")
        check(n > 0) { "'$글' 없음" }
        val 칸 = 들[if (몇째 < 0) n - 1 else 몇째]
        try { 보이게(칸) } catch (_: Throwable) { }
        칸.performClick()
        쉼()
    }

    /** 달력의 [일] 칸 — 칸 전체(눌리는 칸)의 눌림 동작을 바로 부른다 (글자 자리를 손가락으로 누르면 안 먹힌 적이 있다 · 10-16) */
    protected fun 날누름(일: Int) { 누름(일.toString()) }

    /** 설명(contentDescription)으로 찾아 누름 */
    protected fun 누름설명(글: String, 몇째: Int = 0, 일부: Boolean = false) {
        지금 = "${this::class.simpleName} 누름설명 '$글'"; 마지막 = System.currentTimeMillis()
        var 들 = rule.onAllNodesWithContentDescription(글, substring = 일부, useUnmergedTree = true)
        var 밀기 = 0
        if (들.fetchSemanticsNodes().isEmpty()) { 찾아밀기(hasContentDescription(글, substring = 일부)); 들 = rule.onAllNodesWithContentDescription(글, substring = 일부, useUnmergedTree = true) }
        while (들.fetchSemanticsNodes().isEmpty() && 밀기 < 8) { 밀기++; rule.onAllNodes(isRoot())[0].performTouchInput { swipeUp(startY = height * 0.75f, endY = height * 0.25f) }; 쉼(); 들 = rule.onAllNodesWithContentDescription(글, substring = 일부, useUnmergedTree = true) }
        val n = 들.fetchSemanticsNodes().size
        if (n == 0) 실패기록("누름설명 $글")
        check(n > 0) { "설명 '$글' 없음" }
        val 칸 = 들[if (몇째 < 0) n - 1 else 몇째]
        try { 보이게(칸) } catch (_: Throwable) { }
        칸.performClick()
        쉼()
    }

    /** 글자 칸에 쳐 넣기 — [설명] 이 있으면 그 칸, 없으면 첫 글 칸 */
    protected fun 쳐넣기(글: String, 설명: String? = null) {
        val 칸 = if (설명 != null) rule.onAllNodes(hasContentDescription(설명) and hasSetTextAction(), useUnmergedTree = true)[0]
            else rule.onAllNodes(hasSetTextAction(), useUnmergedTree = true)[0]
        칸.performClick(); 칸.performTextInput(글); 쉼()
    }

    /** [글] 이 보이는 자리에서 (dx, dy) px 떨어진 곳을 누름 */
    protected fun 옆누름(글: String, dx: Float, dy: Float) {
        val b = rule.onAllNodesWithText(글, useUnmergedTree = true)[0].fetchSemanticsNode().boundsInRoot
        rule.onRoot().performTouchInput { click(Offset(b.left + dx, b.bottom + dy)) }
        쉼()
    }

    /** 보기용 사진 파일 (filesDir/photos) — 단색 바탕에 원 */
    protected fun 사진(이름: String, 바탕: Int, 원: Int): String {
        val ctx = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val 폴더 = File(ctx.filesDir, "photos").apply { mkdirs() }
        val b = android.graphics.Bitmap.createBitmap(600, 800, android.graphics.Bitmap.Config.ARGB_8888)
        val cv = android.graphics.Canvas(b); cv.drawColor(바탕)
        cv.drawCircle(300f, 360f, 180f, android.graphics.Paint().apply { color = 원; isAntiAlias = true })
        File(폴더, 이름).outputStream().use { b.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, it) }
        return 이름
    }

    protected fun 바로찍(이름: String, ms: Long) { rule.mainClock.advanceTimeBy(ms); captureScreenRoboImage("screens/$이름.png"); 측정(이름) }

    protected fun 찍(이름: String) { 지금 = "찍 $이름"; 쉼(); captureScreenRoboImage("screens/$이름.png"); 측정(이름) }

    /**
     * 10-16 홍겸 님 "UI 전부 크기까지 아티팩트로": 사진과 같은 순간의 화면 요소 치수를 screens/이름.json 으로 남긴다.
     * 칸마다 [왼 위 폭 높이] (dp, 화면 맨 위 왼쪽 기준) · 종류 · 글 · 글자 크기(sp) · 굵기 · 색 · 자간(em) · 줄 높이(sp).
     * 실패해도 사진은 그대로 (try/catch).
     */
    protected fun 측정(이름: String) {
        try {
            val d = rule.activity.resources.displayMetrics.density
            val sb = StringBuilder("[")
            fun 문(t: String) = "\"" + t.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ") + "\""
            fun 반(v: Float) = (Math.round(v / d * 2f) / 2f).toString()
            fun 걷기(n: SemanticsNode) {
                val c = n.config
                val b = n.boundsInRoot
                val 글 = c.getOrNull(SemanticsProperties.Text)?.joinToString("") { it.text }
                val 설명 = c.getOrNull(SemanticsProperties.ContentDescription)?.joinToString(",")
                val 누름 = c.contains(SemanticsActions.OnClick)
                val 칸 = c.contains(SemanticsActions.SetText)
                if ((글 != null || 설명 != null || 누름 || 칸) && b.width > 0f && b.height > 0f) {
                    var 크기 = ""; var 굵기 = ""; var 색 = ""; var 자간 = ""; var 줄 = ""
                    val 행동 = c.getOrNull(SemanticsActions.GetTextLayoutResult)?.action
                    if (행동 != null) {
                        val l = ArrayList<TextLayoutResult>(); 행동(l)
                        val st = l.firstOrNull()?.layoutInput?.style
                        if (st != null) {
                            if (st.fontSize.isSpecified) 크기 = st.fontSize.value.toString()
                            st.fontWeight?.let { 굵기 = it.weight.toString() }
                            if (st.color.isSpecified) 색 = "%06X".format(st.color.toArgb() and 0xFFFFFF)
                            if (st.letterSpacing.isSpecified) 자간 = st.letterSpacing.value.toString()
                            if (st.lineHeight.isSpecified) 줄 = st.lineHeight.value.toString()
                        }
                    }
                    if (sb.length > 1) sb.append(',')
                    sb.append("[").append(반(b.left)).append(',').append(반(b.top)).append(',').append(반(b.width)).append(',').append(반(b.height))
                        .append(',').append(문(if (칸) "칸" else if (누름) "누름" else if (글 != null) "글" else "설명"))
                        .append(',').append(문(글 ?: "")).append(',').append(문(설명 ?: ""))
                        .append(',').append(문(크기)).append(',').append(문(굵기)).append(',').append(문(색)).append(',').append(문(자간)).append(',').append(문(줄)).append("]")
                }
                n.children.forEach { 걷기(it) }
            }
            rule.onAllNodes(isRoot(), useUnmergedTree = true).fetchSemanticsNodes().forEach { 걷기(it) }
            sb.append("]")
            File("screens/$이름.json").apply { parentFile?.mkdirs() }.writeText(sb.toString(), Charsets.UTF_8)
        } catch (t: Throwable) {
            File("screens/_측정오류.txt").apply { parentFile?.mkdirs() }.appendText("$이름: $t\n")
        }
    }

    /** 오늘도 상체 A 를 한 기록 (한 번 더 · 기록 삭제 단추) */
    protected fun 오늘기록(d: 앱데이터): 앱데이터 {
        val r = d.루틴들.first()
        val 시작 = System.currentTimeMillis() - 2 * 3_600_000
        return d.copy(기록 = d.기록 + (오늘.toString() to 날기록(r.id, r.이름, true,
            r.종목.map { e -> 종목기록(e.이름, List(e.세트) { 세트(e.무게, e.횟수) }) }, 3600, 시작, 시작 + 3_600_000)))
    }

    protected fun 세션넣기(d: 앱데이터, 몇세트: Int, 더: Int = 0): 앱데이터 {
        val r = d.루틴들.first()
        var S = 운동시작(r, System.currentTimeMillis() - 20 * 60_000)!!
        repeat(더) { S = S.세트추가(0) }
        val 첫 = S.종목들[0]
        return d.copy(세션 = S.copy(s = 몇세트, 종목들 = listOf(첫.copy(기록 = List(첫.세트) { k -> if (k < 몇세트) 세트(첫.무게, 첫.횟수) else null })) + S.종목들.drop(1)))
    }
}


@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s01_calendar : ShotBase() {
    @Test fun s01_calendar() { 켜기(); 찍("01_캘린더") }
}


@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s01b_calendarDay : ShotBase() {
    @Test fun s01b_calendarDay() { 켜기(); 날누름(오늘.minusDays(2).dayOfMonth); 찍("01b_캘린더_지난날") }
}


@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s02_search : ShotBase() {
    @Test fun s02_search() { 켜기(); 탭(1); 찍("02_검색") }
}


@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s03_routines : ShotBase() {
    @Test fun s03_routines() { 켜기(); 탭(2); 찍("03_루틴") }
}


@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s04_routineDetail : ShotBase() {
    @Test fun s04_routineDetail() { 켜기(); 탭(2); 누름("상체 A"); 찍("04_루틴_상세") }
}


@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s05_addSheet : ShotBase() {
    @Test fun s05_addSheet() { 켜기(); 탭(2); 누름("상체 A"); 누름("운동 종목 추가", -1); 찍("05_루틴_종목넣기시트") }
}


@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s06_exercises : ShotBase() {
    @Test fun s06_exercises() { 켜기(); 탭(3); 찍("06_종목") }
}


@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s07_exerciseOpen : ShotBase() {
    @Test fun s07_exerciseOpen() { 켜기(); 탭(3); 누름("벤치프레스"); 찍("07_종목_펼침") }
}


@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s08_newExercise : ShotBase() {
    @Test fun s08_newExercise() { 켜기(); 탭(3); 누름("+ 새 종목 만들기"); 찍("08_새종목시트") }
}


@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s09_plan : ShotBase() {
    @Test fun s09_plan() { 켜기(); 탭(4); 찍("09_플랜") }
}


@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s10_memo : ShotBase() {
    @Test fun s10_memo() { 켜기(); 탭(5); 찍("10_메모시트") }
}


@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s11_social : ShotBase() {
    @Test fun s11_social() { 켜기(); 탭(6); 찍("11_소셜") }
}


@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s12_settings : ShotBase() {
    @Test fun s12_settings() { 켜기(); 탭(7); 찍("12_설정") }
}

@Config(qualifiers = "w412dp-h2400dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s12b_settingsFull : ShotBase() {
    @Test fun s12b_settingsFull() { 켜기(); 탭(7); 찍("12b_설정_전체") }
}


@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s13_profile : ShotBase() {
    @Test fun s13_profile() { 켜기(); 탭(8); 찍("13_프로필") }
}


@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s14_status : ShotBase() {
    @Test fun s14_status() { 켜기(); 탭(8); 누름("스탯", -1); 찍("14_스테이터스") }
}


@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s15_achievements : ShotBase() {
    @Test fun s15_achievements() { 켜기(); 탭(8); 누름("업적", -1); 찍("15_도전과제") }
}

@Config(qualifiers = "w412dp-h2400dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s15b_achievementsFull : ShotBase() {
    @Test fun s15b_achievementsFull() { 켜기(); 탭(8); 누름("업적", -1); 찍("15b_도전과제_전체") }
}


@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s16_workout : ShotBase() {
    @Test fun s16_workout() { 켜기 { 세션넣기(it, 2) }; 찍("16_운동") }
}


@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s17_rest : ShotBase() {
    @Test fun s17_rest() { 켜기 { 세션넣기(it, 2) }; 누름("세트 완료하기"); 찍("17_운동_휴식") }
}


@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s18_report : ShotBase() {
    @Test fun s18_report() {
        켜기 { d ->
            val r = d.루틴들.first()
            val S = 운동시작(r, System.currentTimeMillis() - 55 * 60_000)!!
            val 다 = S.copy(종목들 = S.종목들.map { e -> e.copy(기록 = List(e.세트) { 세트(e.무게, e.횟수) }) })
            d.copy(세션 = 다.끝냄(System.currentTimeMillis()))
        }
        찍("18_운동_보고서")
    }
}


@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s19_workoutOtherTab : ShotBase() {
    @Test fun s19_workoutOtherTab() { 켜기 { 세션넣기(it, 1) }; 탭(0); 찍("19_운동중_다른탭") }
}


@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s01c_calendarTodayDone : ShotBase() {
    @Test fun s01c() { 켜기 { 오늘기록(it) }; 찍("01c_캘린더_오늘기록") }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s01d_calendarDeleteMode : ShotBase() {
    @Test fun s01d() { 켜기 { 오늘기록(it) }; 누름("기록 삭제"); 찍("01d_캘린더_기록삭제") }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s16b_addSetScroll : ShotBase() {
    @Test fun s16b() { 켜기 { 세션넣기(it, 2, 더 = 4) }; 누름설명("세트 추가"); 찍("16a_운동_세트추가_전"); 누름설명("세트 추가"); 찍("16b_운동_세트추가_스크롤") }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s16c_addSetFlash : ShotBase() {
    @Test fun s16c() { 켜기 { 세션넣기(it, 2) }
        rule.onAllNodesWithContentDescription("세트 추가", useUnmergedTree = true)[0].performClick()
        바로찍("16c_운동_세트추가_순간", 200) }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s07b_exerciseAddSet : ShotBase() {
    @Test fun s07b() { 켜기(); 탭(3); 누름("스쿼트")
        var 들 = rule.onAllNodesWithText("+ 세트", substring = true, useUnmergedTree = true)
        if (들.fetchSemanticsNodes().isEmpty()) { 찾아밀기(hasText("+ 세트", substring = true)); 들 = rule.onAllNodesWithText("+ 세트", substring = true, useUnmergedTree = true) }
        var 밀기 = 0
        while (들.fetchSemanticsNodes().isEmpty() && 밀기 < 8) { 밀기++; rule.onAllNodes(isRoot())[0].performTouchInput { swipeUp(startY = height * 0.75f, endY = height * 0.25f) }; 쉼(); 들 = rule.onAllNodesWithText("+ 세트", substring = true, useUnmergedTree = true) }
        try { 보이게(들[0]) } catch (_: Throwable) { }
        들[0].performClick()
        바로찍("07b_종목_세트추가_순간", 200) }
}


@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s05b_addSheetOpen : ShotBase() {
    @Test fun s05b_addSheetOpen() { 켜기(); 탭(2); 누름("상체 A"); 누름("운동 종목 추가", -1); 누름("스쿼트", -1); 찍("05b_루틴_넣기_펼침") }
}


@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s07c_exerciseOpenNoPlan : ShotBase() {
    @Test fun s07c_exerciseOpenNoPlan() { 켜기(); 탭(3); 누름("스쿼트"); 찍("07c_종목_펼침_스쿼트") }
}
