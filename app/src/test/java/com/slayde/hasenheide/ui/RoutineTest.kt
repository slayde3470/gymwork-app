package com.slayde.hasenheide.ui

import com.slayde.hasenheide.data.루틴
import com.slayde.hasenheide.data.루틴종목
import com.slayde.hasenheide.data.목표
import com.slayde.hasenheide.data.세트
import com.slayde.hasenheide.data.세트빼기
import com.slayde.hasenheide.data.종목
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

    @Test fun 넣기목록_가나다순_전체() {
        val l = 넣기칸들(표, emptyList(), 넣기전체, true)
        assertEquals(listOf("랫풀다운", "벤치프레스", "벤치프레스", "스쿼트"), l.map { it.이름 })
        // 같은 이름 — 만든 순서 번호 1 · 2, 하나뿐이면 0
        assertEquals(listOf(0, 1, 2, 0), l.map { it.번호 })
        assertEquals(listOf("랫풀다운", "벤치프레스", "종b2", "스쿼트"), l.map { it.키 })
    }

    @Test fun 넣기목록_칸으로_거름() {
        assertEquals(listOf("랫풀다운"), 넣기칸들(표, emptyList(), "등", true).map { it.이름 })
        assertTrue(넣기칸들(표, emptyList(), "어깨", true).isEmpty())
    }

    @Test fun 넣기목록_플랜이_있으면_플랜칸_먼저만든것에만() {
        val 플랜들 = listOf(플("p1", "벤치프레스", "벤치프레스"), 플("p2", "벤치프레스 2", "벤치프레스"))
        val l = 넣기칸들(표, 플랜들, 넣기전체, true)
        // 첫 벤치프레스 → 플랜 두 칸, 둘째 벤치프레스(종b2) → 그냥 종목 칸
        assertEquals(listOf("p1", null, "p2"), l.filter { it.종목이름 == "벤치프레스" }.map { it.플랜id })
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

    @Test fun 펼침이_종목을_따라간다() {
        // B(1)를 펼친 채 A 앞으로 옮김 → 새 차례 B A C, 펼침 = 0
        assertEquals(setOf(0), 펼침맞춤(setOf(1), listOf(1, 0, 2)))
        assertEquals(setOf(0, 2), 펼침뺌(setOf(0, 1, 3), 1))
        assertEquals(setOf(0, 2, 4), 펼침끼움(setOf(0, 1, 3), 1))
        assertEquals(setOf(0, 1, 3), 펼침뺌(펼침끼움(setOf(0, 1, 3), 2), 2))
    }

    // ─────────────── 끝 [+ 종목 추가] ───────────────

    @Test fun 끝단추_위단추가_밖으로_나갈때만() {
        // 위 단추 아래끝 60. 넘길 수 있는 양이 60 이하면 맨 아래에서도 위 단추가 보인다
        assertFalse(끝단추판정(50, 40, false, 60))
        assertTrue(끝단추판정(200, 40, false, 60))
        // 끝 단추가 이미 보이면 그 몫(40)을 빼고 본다 — 깜빡거리지 않는다
        assertFalse(끝단추판정(90, 40, true, 60))
        assertTrue(끝단추판정(101, 40, true, 60))
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
