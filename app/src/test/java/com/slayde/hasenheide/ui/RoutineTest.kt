package com.slayde.hasenheide.ui

import com.slayde.hasenheide.data.루틴
import com.slayde.hasenheide.data.루틴종목
import com.slayde.hasenheide.data.목표
import com.slayde.hasenheide.data.세트
import com.slayde.hasenheide.data.세트빼기
import com.slayde.hasenheide.data.종목
import com.slayde.hasenheide.data.묶을수있다
import com.slayde.hasenheide.data.새묶음이름
import com.slayde.hasenheide.data.슈퍼묶기
import com.slayde.hasenheide.data.슈퍼풀기
import com.slayde.hasenheide.data.종목옮기기
import com.slayde.hasenheide.data.플랜
import com.slayde.hasenheide.data.휴식
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 루틴 화면 · 종목 넣기 시트의 순수 계산 (10-05 앱 옮기기 2단계 RT).
 * 화면 코드(RoutineScreen.kt · AddExerciseSheet.kt) 안 `internal fun` 을 그대로 부른다.
 */
class RoutineTest {

    private fun 줄(이름: String, 세트: Int = 1, 슈퍼: String? = null, 플랜id: String? = null) =
        루틴종목(이름, 세트 = 세트, 슈퍼 = 슈퍼, 플랜id = 플랜id)

    // ─────────────── 넣기 목록 (시안 `넣기목록`) ───────────────

    private val 표 = listOf(
        종목("스쿼트", "하체"),
        종목("벤치프레스", "가슴"),
        종목("벤치프레스", "가슴", id = "종b2"),   // 같은 이름 둘째
        종목("랫풀다운", "등"),
    )
    private fun 플(id: String, 이름: String, 종목: String, 켬: Boolean = true) = 플랜(id = id, 이름 = 이름, 종목 = 종목, 시작1RM = 60.0, 켬 = 켬)

    @Test fun 넣기목록_종목표차례_전체() {
        // 10-07 홍겸 님: 가나다순 → 종목표 차례 (꾹 눌러 끌어 바꾼다 · 종목 탭과 같다)
        val l = 넣기칸들(표, emptyList(), 넣기전체, true)
        assertEquals(listOf("스쿼트", "벤치프레스", "벤치프레스", "랫풀다운"), l.map { it.이름 })
        // 같은 이름 — 만든 순서 번호 1 · 2, 하나뿐이면 0
        assertEquals(listOf(0, 1, 2, 0), l.map { it.번호 })
        assertEquals(listOf("스쿼트", "벤치프레스", "종b2", "랫풀다운"), l.map { it.키 })
    }

    @Test fun 넣기목록_칸으로_거름() {
        assertEquals(listOf("랫풀다운"), 넣기칸들(표, emptyList(), "등", true).map { it.이름 })
        assertTrue(넣기칸들(표, emptyList(), "어깨", true).isEmpty())
    }

    @Test fun 넣기목록_플랜이_있으면_플랜칸_먼저만든것에만() {
        val 플랜들 = listOf(플("p1", "벤치프레스", "벤치프레스"), 플("p2", "벤치프레스 2", "벤치프레스"))
        val l = 넣기칸들(표, 플랜들, 넣기전체, true)
        // 첫 벤치프레스 → 플랜 두 칸, 둘째 벤치프레스(종b2) → 그냥 종목 칸
        assertEquals(listOf("p1", "p2", null), l.filter { it.종목이름 == "벤치프레스" }.map { it.플랜id })   // 10-07 종목표 차례
        assertEquals("종b2", l.first { it.플랜id == null && it.종목이름 == "벤치프레스" }.키)
        // 플랜 이름이 종목 이름과 같으면 번호 딱지(1) · 다르면 없음
        assertEquals(1, l.first { it.플랜id == "p1" }.번호)
        assertEquals(0, l.first { it.플랜id == "p2" }.번호)
    }

    @Test fun 넣기목록_종목표에_없는_플랜은_전체에서만() {
        val 플랜들 = listOf(플("p9", "데드리프트", "데드리프트"))
        assertTrue(넣기칸들(표, 플랜들, 넣기전체, true).any { it.플랜id == "p9" })
        assertTrue(넣기칸들(표, 플랜들, "등", true).none { it.플랜id == "p9" })
    }

    @Test fun 넣기목록_꺼진플랜_플랜안씀() {
        val 플랜들 = listOf(플("p1", "스쿼트", "스쿼트", 켬 = false))
        assertNull(넣기칸들(표, 플랜들, 넣기전체, true).first { it.이름 == "스쿼트" }.플랜id)
        val 켠 = listOf(플("p1", "스쿼트", "스쿼트"))
        // 운동 화면처럼 플랜 길을 안 넘기면 플랜 칸 없이 종목 칸
        assertNull(넣기칸들(표, 켠, 넣기전체, false).first { it.이름 == "스쿼트" }.플랜id)
    }

    @Test fun 넣기목록_빈것() {
        assertTrue(넣기칸들(emptyList(), emptyList(), 넣기전체, true).isEmpty())
    }

    @Test fun 넣기칸_열쇠는_겹치지_않는다() {
        val l = 넣기칸들(표, listOf(플("벤치프레스", "벤치프레스", "벤치프레스")), 넣기전체, true)
        assertEquals(l.size, l.map { it.열쇠 }.toSet().size)
    }

    @Test fun 체크글_넷부터_곱() {
        assertEquals("", 체크글(0))
        assertEquals("✓", 체크글(1))
        assertEquals("✓✓✓", 체크글(3))
        assertEquals("✓×4", 체크글(4))
        assertEquals("✓×12", 체크글(12))
    }

    // ─────────────── 루틴 목록 ───────────────

    @Test fun 자동번호_수동은_없음() {
        val l = listOf(루틴("a", "가", 자동생성 = true), 루틴("b", "나"), 루틴("c", "쉼", 휴식일 = true, 자동생성 = true))
        assertEquals(mapOf("a" to 1, "c" to 2), 자동번호(l))
    }

    // ─────────────── 줄 빼기 · 되돌리기 ───────────────

    @Test fun 줄빼기_되돌리기_제자리() {
        val r = 루틴("r", "가슴", 종목 = listOf(줄("A"), 줄("B"), 줄("C")))
        val 뺀 = 루줄빼기(r, 1)
        assertEquals(listOf("A", "C"), 뺀.종목.map { it.이름 })
        assertEquals(listOf("A", "B", "C"), 루줄끼움(뺀, 1, r.종목[1]).종목.map { it.이름 })
        // 그사이 줄이 줄었으면 맨 끝에
        assertEquals(listOf("B"), 루줄끼움(루틴("r", "x"), 5, r.종목[1]).종목.map { it.이름 })
    }

    @Test fun 줄빼기_범위밖은_그대로_슈퍼세트_혼자면_푼다() {
        val r = 루틴("r", "x", 종목 = listOf(줄("A", 슈퍼 = "g"), 줄("B", 슈퍼 = "g")))
        assertEquals(r, 루줄빼기(r, 7))
        val 뺀 = 루줄빼기(r, 0)
        assertNull(뺀.종목.single().슈퍼)
        assertNull(루줄끼움(뺀, 0, r.종목[0]).종목[0].슈퍼)
    }

    @Test fun 같은종목_두줄에서_뒤의것만_뺀다() {
        val r = 루틴("r", "x", 종목 = listOf(줄("A", 세트 = 1), 줄("B"), 줄("A", 세트 = 3)))
        val j = r.종목.indexOfLast { it.이름 == "A" }
        assertEquals(listOf(1, 1), 루줄빼기(r, j).종목.map { it.세트 })
    }

    // ─────────────── 세트 지우기 · 되돌리기 ───────────────

    @Test fun 세트지움_되돌림_제자리() {
        val e = 루틴종목("벤치", 세트 = 3, 세트값 = listOf(세트(40.0, 10), 세트(50.0, 8), 세트(60.0, 5)), 휴식값 = listOf(60, 90, 120))
        val 뺀 = e.세트빼기(1)
        val 되 = 루세트끼움(뺀, 1, e.목표(1), e.휴식(1))
        assertEquals(3, 되.세트)
        assertEquals((0 until 3).map { e.목표(it) }, (0 until 3).map { 되.목표(it) })
        assertEquals((0 until 3).map { e.휴식(it) }, (0 until 3).map { 되.휴식(it) })
        // 맨 앞 세트를 되돌리면 대표값도 따라간다
        val 첫 = 루세트끼움(e.세트빼기(0), 0, e.목표(0), e.휴식(0))
        assertEquals(40.0, 첫.무게); assertEquals(10, 첫.횟수); assertEquals(60, 첫.휴식)
    }

    @Test fun 세트되돌림_자리가_없으면_끝에() {
        val e = 루틴종목("벤치", 세트 = 1)
        val 되 = 루세트끼움(e, 9, 세트(70.0, 3), 45)
        assertEquals(2, 되.세트)
        assertEquals(세트(70.0, 3), 되.목표(1))
        assertEquals(45, 되.휴식(1))
    }

    // ─────────────── 끌어 옮기기 · 펼침 따라가기 ───────────────

    @Test fun 옮김표는_종목옮기기와_같다() {
        val r = 루틴("r", "x", 종목 = listOf(줄("A"), 줄("B"), 줄("C"), 줄("D")))
        for (f in 0..3) for (t in 0..3) for (뒤 in listOf(false, true)) {
            val 표 = 루틴옮김표(r, f, t, 뒤)
            assertEquals(r.종목옮기기(f, t, 뒤).종목.map { it.이름 }, 표.map { r.종목[it].이름 }, "f=$f t=$t 뒤=$뒤")
        }
    }

    @Test fun 옮김표_같은이름_슈퍼세트() {
        val r = 루틴("r", "x", 종목 = listOf(줄("A"), 줄("S", 슈퍼 = "g"), 줄("T", 슈퍼 = "g"), 줄("A")))
        // 묶음(1, 2)을 맨 끝 뒤로 → A A S T
        assertEquals(listOf(0, 3, 1, 2), 루틴옮김표(r, 1, 3, true))
        assertEquals(listOf(0, 1, 2, 3), 루틴옮김표(r, 9, 0, false))
    }

    // ─────────────── 10-10 홍겸 님: 꾹 눌러 끌어 가운데에 놓으면 슈퍼세트 ───────────────

    @Test fun 끌기_가운데_40퍼센트만_묶기() {
        val 세로 = listOf(끌칸(0, 0f, 0f, 100f, 100f), 끌칸(1, 0f, 108f, 100f, 208f))
        assertNull(끌가운데(세로, null, 20f, 0f))           // 위 30% = 앞
        assertEquals(0, 끌가운데(세로, null, 30f, 0f))      // 경계(30%)부터 가운데
        assertEquals(0, 끌가운데(세로, null, 69f, 0f))
        assertNull(끌가운데(세로, null, 70f, 0f))           // 아래 30% = 뒤
        assertEquals(1, 끌가운데(세로, null, 158f, 0f))
        assertNull(끌가운데(세로, null, 104f, 0f))          // 틈
        assertEquals(1, 끌가운데(세로, null, 128f, 30f))    // 칸 1 은 30 위로: 78~178 → 가운데 108~148
        assertNull(끌가운데(세로, 50f, 50f, 0f))            // 2열 격자(x 있음)에는 가운데 묶기가 없다
    }

    @Test fun 묶기_가운데에_놓으면_대상_바로_뒤에_붙는다() {
        val r = 루틴("r", "x", 종목 = listOf(줄("A"), 줄("B"), 줄("C"), 줄("D")))
        val 새 = r.슈퍼묶기(0, 2, r.새묶음이름())
        assertEquals(listOf("B", "C", "A", "D"), 새.종목.map { it.이름 })
        assertEquals(listOf(null, "g1", "g1", null), 새.종목.map { it.슈퍼 })
        assertEquals(listOf(1, 2, 0, 3), 루틴묶음표(r, 0, 2))
        // 세 번째가 편입 — 묶음 이름은 그대로
        val 셋 = 새.슈퍼묶기(0, 1, 새.새묶음이름())
        assertEquals(listOf("C", "A", "B", "D"), 셋.종목.map { it.이름 })
        assertEquals(listOf("g1", "g1", "g1", null), 셋.종목.map { it.슈퍼 })
        assertEquals("g2", 새.새묶음이름())
    }

    @Test fun 묶기_묶음째_집어_다른_종목에() {
        val r = 루틴("r", "x", 종목 = listOf(줄("A", 슈퍼 = "g1"), 줄("B", 슈퍼 = "g1"), 줄("C"), 줄("D")))
        val 새 = r.슈퍼묶기(1, 3, r.새묶음이름())   // 묶음(A B)을 집어 D 에 → D 도 같은 이름이 아니라 새 묶음 g2 로 모두 한 묶음
        assertEquals(listOf("C", "D", "A", "B"), 새.종목.map { it.이름 })
        assertEquals(listOf(null, "g2", "g2", "g2"), 새.종목.map { it.슈퍼 })
    }

    @Test fun 묶기_플랜줄_자기자신_같은묶음은_안_된다() {
        val r = 루틴("r", "x", 종목 = listOf(줄("A"), 줄("P", 플랜id = "p"), 줄("S", 슈퍼 = "g1"), 줄("T", 슈퍼 = "g1")))
        assertFalse(r.묶을수있다(0, 1)); assertFalse(r.묶을수있다(1, 0))
        assertFalse(r.묶을수있다(0, 0)); assertFalse(r.묶을수있다(2, 3))
        assertFalse(r.묶을수있다(0, 9)); assertTrue(r.묶을수있다(0, 2))
        assertEquals(r, r.슈퍼묶기(0, 1, "g9")); assertEquals(r, r.슈퍼묶기(2, 3, "g9"))
    }

    @Test fun 같은_종목_두_줄도_하나도_사라지지_않는다() {
        // 똑같은 줄(값까지 같음)이 둘 — 값으로 찾으면 쌍둥이가 같이 빠져 한 줄이 사라졌다
        val r = 루틴("r", "x", 종목 = listOf(줄("A"), 줄("A"), 줄("B")))
        for (f in 0..2) for (t in 0..2) for (뒤 in listOf(false, true)) {
            val 새 = r.종목옮기기(f, t, 뒤)
            assertEquals(listOf("A", "A", "B"), 새.종목.map { it.이름 }.sorted(), "옮기기 f=$f t=$t")
        }
        for (f in 0..2) for (t in 0..2) {
            val 새 = r.슈퍼묶기(f, t, "g1")
            assertEquals(listOf("A", "A", "B"), 새.종목.map { it.이름 }.sorted(), "묶기 f=$f t=$t")
        }
        // A(0) 를 B 뒤로 → A B A
        assertEquals(listOf("A", "B", "A"), r.종목옮기기(0, 2, true).종목.map { it.이름 })
        // 둘째 A(1) 만 B 에 묶기 → 첫 A 는 그대로
        val 묶 = r.슈퍼묶기(1, 2, "g1")
        assertEquals(listOf("A", "B", "A"), 묶.종목.map { it.이름 })
        assertEquals(listOf(null, "g1", "g1"), 묶.종목.map { it.슈퍼 })
    }

    @Test fun 루틴합치기_휴식일과_자기자신은_안_된다() {
        val l = listOf(루틴("a", "가슴"), 루틴("b", "등"), 루틴("c", "쉼", 휴식일 = true))
        assertTrue(합칠수있다(l, 0, 1)); assertTrue(합칠수있다(l, 1, 0))
        assertFalse(합칠수있다(l, 0, 0)); assertFalse(합칠수있다(l, 0, 2)); assertFalse(합칠수있다(l, 2, 1)); assertFalse(합칠수있다(l, 0, 9))
    }

    @Test fun 슈퍼풀기는_그_묶음만() {
        val r = 루틴("r", "x", 종목 = listOf(줄("A", 슈퍼 = "g1"), 줄("B", 슈퍼 = "g1"), 줄("C", 슈퍼 = "g2"), 줄("D", 슈퍼 = "g2")))
        assertEquals(listOf(null, null, "g2", "g2"), r.슈퍼풀기("g1").종목.map { it.슈퍼 })
    }

    @Test fun 펼침이_종목을_따라간다() {
        // B(1)를 펼친 채 A 앞으로 옮김 → 새 차례 B A C, 펼침 = 0
        assertEquals(setOf(0), 펼침맞춤(setOf(1), listOf(1, 0, 2)))
        assertEquals(setOf(0, 2), 펼침뺌(setOf(0, 1, 3), 1))
        assertEquals(setOf(0, 2, 4), 펼침끼움(setOf(0, 1, 3), 1))
        assertEquals(setOf(0, 1, 3), 펼침뺌(펼침끼움(setOf(0, 1, 3), 2), 2))
    }

    // ─────────────── 끝 [+ 종목 추가] ───────────────

    // ─────────────── 10-06 v22 ④ 2열 격자 · 끌기 앞/뒤 ───────────────

    @Test fun 격자_모두_접힘이면_둘씩() {
        assertEquals(listOf(listOf(0, 1), listOf(2, 3), listOf(4)), 루격자줄(List(5) { false }))
        assertTrue(루격자줄(emptyList()).isEmpty())
    }

    @Test fun 격자_왼쪽을_펼치면_오른쪽_짝은_아래줄로() {
        // 0 펼침 → [0] 한 줄 전체 · 1 · 2 가 다음 줄
        assertEquals(listOf(listOf(0), listOf(1, 2), listOf(3)), 루격자줄(listOf(true, false, false, false)))
    }

    @Test fun 격자_오른쪽을_펼치면_다음줄_전체_앞줄_오른쪽은_빈칸() {
        // 1 펼침 → [0, 빈칸] · [1] · [2, 3] — 순서 그대로(뒤의 상자가 빈칸을 채우지 않는다)
        assertEquals(listOf(listOf(0), listOf(1), listOf(2, 3)), 루격자줄(listOf(false, true, false, false)))
    }

    @Test fun 격자_펼침이_이어지면_한줄씩() {
        assertEquals(listOf(listOf(0), listOf(1), listOf(2)), 루격자줄(listOf(true, true, false)))
        // 순서가 늘 그대로 — 줄을 이어 붙이면 0..n-1
        val 넓 = listOf(false, true, false, true, true, false, false, false, true)
        assertEquals(넓.indices.toList(), 루격자줄(넓).flatten())
        assertTrue(루격자줄(넓).all { it.size in 1..2 })
    }

    // 2열: 0 = (0..100, 0..50) 1 = (108..208, 0..50) / 2 = 펼친 상자 (0..208, 58..158)
    private val 칸들 = listOf(
        끌칸(0, 0f, 0f, 100f, 50f), 끌칸(1, 108f, 0f, 208f, 50f), 끌칸(2, 0f, 58f, 208f, 158f),
    )
    private val 반폭 = setOf(0, 1)

    @Test fun 끌기_반폭은_좌우로_앞뒤() {
        assertEquals(1 to false, 끌대상(칸들, 120f, 45f, 0f, 반폭))   // 1 의 왼쪽 반 = 앞 (아래쪽이어도)
        assertEquals(1 to true, 끌대상(칸들, 200f, 5f, 0f, 반폭))     // 1 의 오른쪽 반 = 뒤 (위쪽이어도)
        assertEquals(0 to true, 끌대상(칸들, 60f, 10f, 0f, 반폭))
    }

    @Test fun 끌기_펼친상자는_위아래로_앞뒤() {
        assertEquals(2 to false, 끌대상(칸들, 190f, 70f, 0f, 반폭))
        assertEquals(2 to true, 끌대상(칸들, 10f, 150f, 0f, 반폭))
    }

    @Test fun 끌기_틈이나_밖이면_없음_넘긴만큼_옮김() {
        assertNull(끌대상(칸들, 104f, 20f, 0f, 반폭))   // 두 상자 사이 틈
        assertNull(끌대상(칸들, 50f, 300f, 0f, 반폭))
        // 30 넘겼으면 칸이 30 위로 — y 40 은 펼친 상자(58-30=28 ~ 128) 위 반
        assertEquals(2 to false, 끌대상(칸들, 50f, 40f, 30f, 반폭))
    }

    @Test fun 끌기_세로목록은_x를_안본다() {
        val 세로 = listOf(끌칸(0, 0f, 0f, 100f, 50f), 끌칸(1, 0f, 58f, 100f, 108f))
        assertEquals(1 to true, 끌대상(세로, null, 100f, 0f, emptySet()))
        assertEquals(0 to false, 끌대상(세로, null, 10f, 0f, emptySet()))
    }

    // ─────────────── 10-06 v22 ⑦ 넣기 이름 · 연필 ───────────────

    @Test fun 넣기이름_낱말가운데서_끊기면_넘침() {
        assertFalse(넣기이름넘침("루마니안 데드리프트", false, listOf(5, 10)))   // '루마니안 |데드리프트' 띄어쓰기에서
        assertTrue(넣기이름넘침("루마니안 데드리프트", false, listOf(8, 10)))    // '데드리프/트' 가운데
        assertTrue(넣기이름넘침("스쿼트", true, listOf(3)))                       // 잘림
        assertFalse(넣기이름넘침("스쿼트", false, listOf(3)))
    }

    @Test fun 넣기칸_연필은_종목표_종목만() {
        val 플랜들 = listOf(플("p1", "스쿼트 12주", "스쿼트"), 플("p9", "턱걸이 플랜", "턱걸이"))
        val l = 넣기칸들(표, 플랜들, 넣기전체, true)
        assertEquals("종b2", l.first { it.키 == "종b2" }.편집키)
        assertEquals(표[0].id, l.first { it.플랜id == "p1" }.편집키)   // 플랜 칸 — 그 종목을 고친다
        assertNull(l.first { it.플랜id == "p9" }.편집키)               // 종목표에 없는 종목의 플랜 — 연필 없음
    }

    // ─────────────── 쳐 넣는 값 ───────────────

    @Test fun 무게_횟수_읽기() {
        assertEquals(62.5, 루무게읽기("62,5"))
        assertEquals(0.0, 루무게읽기("-3"))
        assertNull(루무게읽기(""))
        assertNull(루무게읽기("."))
        assertEquals(1, 루횟수읽기("0"))
        assertEquals(8, 루횟수읽기("7.6"))
        assertNull(루횟수읽기("x"))
    }

    // ─────────────── 플랜 달성률 ───────────────

    @Test fun 달성률_0에서_1() {
        val p = 플랜(id = "a", 이름 = "벤치", 종목 = "벤치프레스", 시작1RM = 60.0, 목표무게 = 100.0)
        assertEquals(0.0, 플랜달성률(p))
        // 목표가 시작보다 작으면(잘못 넣은 값) 0
        assertEquals(0.0, 플랜달성률(p.copy(목표무게 = 40.0)))
    }
}
