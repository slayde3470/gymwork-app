package com.slayde.hasenheide.ui

import com.slayde.hasenheide.data.날기록
import com.slayde.hasenheide.data.다음으로
import com.slayde.hasenheide.data.세션종목
import com.slayde.hasenheide.data.세트
import com.slayde.hasenheide.data.운동세션
import com.slayde.hasenheide.data.종목기록
import com.slayde.hasenheide.data.찬것
import com.slayde.hasenheide.data.칸
import com.slayde.hasenheide.data.휴식중
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 앱 옮기기 2단계 W — 운동 화면의 순수 계산 (WorkoutScreen.kt 아래 '순수 계산' 묶음).
 * 시안 v21: 큰 주 단추 · 쉼 게이지 문구 · 마지막 세트 휴식 · 세트 지우기/되돌리기 · 지표 · 칸 폭.
 * '뒤죽박죽 사용' — 빠르게 두 번 누르기 · 범위 밖 번호 · 지우고 순서 바꾼 뒤 되돌리기
 */
class WorkoutPortTest {
    private val 때 = 1_000_000L

    private fun 줄(이름: String, 세트: Int = 3, 한것: Int = 0, 마감: Boolean = false, 슈퍼: String? = null) =
        세션종목(이름, 세트, 세트, 20.0, 10, 60, 기록 = List(한것) { 세트(20.0, 10) } + List(세트 - 한것) { null },
            휴식들 = List(세트) { 60 }, 마감 = 마감, 슈퍼 = 슈퍼)

    private fun 세션(vararg 줄들: 세션종목, i: Int = 0, s: Int = 0, 휴: 휴식중? = null) =
        운동세션("r", "R", 500L, i, s, 20.0, 10, 줄들.toList(), 휴)

    // ─────────────── 큰 주 단추 (v21 ④) ───────────────

    @Test fun 주단추_차례() {
        val S = 세션(줄("A"), 줄("B"))
        assertEquals(운주.완료, 운주상태(S, 0))
        assertEquals(운주.건너뛰기, 운주상태(S.copy(휴식 = 휴식중(0, 때 + 60_000, 종목 = 0)), 0))
        // 보는 종목을 다 끝냄 → 다음 종목 (쉬는 중이어도)
        val T = 세션(줄("A", 3, 3), 줄("B"), 휴 = 휴식중(2, 때 + 60_000, 종목 = 0))
        assertEquals(운주.다음종목, 운주상태(T, 0))
        assertEquals(운주.건너뛰기, 운주상태(T, 1))
        // 모두 끝(마감 포함) → 마무리
        assertEquals(운주.마무리, 운주상태(세션(줄("A", 3, 3), 줄("B", 3, 1, 마감 = true)), 1))
        assertEquals("세트 완료하기", 운주.완료.글)
    }

    @Test fun 주단추_빠르게_두번_눌러도_체크풀기가_안됨() {
        val S = 세션(줄("A"), 줄("B"))
        val r1 = 운주누름(S, 0, 때)
        assertNotNull(r1.세션.종목들[0].기록.칸(0)); assertNotNull(r1.세션.휴식)
        assertFalse(r1.건너뜀)
        // 두 번째 누름은 '그 순간' 세션으로 — 쉬는 중이니 건너뛰기
        val r2 = 운주누름(r1.세션, r1.본, 때 + 100)
        assertNotNull(r2.세션.종목들[0].기록.칸(0)); assertNull(r2.세션.휴식); assertTrue(r2.건너뜀)
        assertEquals(1, r2.세션.s)
    }

    @Test fun 주단추_완료는_보는종목의_첫빈칸() {
        // 지금 종목은 A 지만 B 를 보고 있다 → B 의 0세트를 체크
        val S = 세션(줄("A"), 줄("B"))
        val r = 운주누름(S, 1, 때)
        assertNull(r.세션.종목들[0].기록.칸(0)); assertNotNull(r.세션.종목들[1].기록.칸(0))
        assertEquals(1, r.본)
    }

    // ─────────────── 마지막 세트도 휴식 게이지 (v18 ③) ───────────────

    @Test fun 마지막세트_체크해도_마무리화면으로_안감() {
        val S = 세션(줄("A", 3, 3), 줄("B", 2, 1), i = 1, s = 1)
        val t = 운체크(S, 1, 1, 때)
        assertFalse(t.끝화면); assertNull(t.끝시각)
        val h = assertNotNull(t.휴식)
        assertEquals(1, h.종목); assertEquals(1, h.k); assertEquals(1, h.다음i)
        assertEquals(운주.마무리, 운주상태(t, 1))
        assertEquals(쉼종류.마무리, 쉼글(t, 1, 1))
        // 휴식이 끝나도(데이터 '다음으로') 마무리 화면으로 넘어가지 않고 그 자리에 머문다
        val u = t.다음으로(때 + 70_000)
        assertFalse(u.끝화면); assertNull(u.휴식)
        assertEquals(운주.마무리, 운주상태(u, 1))
        // 큰 단추 → 마무리 화면
        assertTrue(운주누름(u, 1, 때 + 80_000).세션.끝화면)
    }

    @Test fun 체크_범위밖은_그대로() {
        val S = 세션(줄("A"))
        assertEquals(S, 운체크(S, 5, 0, 때)); assertEquals(S, 운체크(S, 0, 9, 때)); assertEquals(S, 운체크(S, 0, -1, 때))
    }

    @Test fun 체크풀기는_데이터와_같다() {
        val S = 세션(줄("A", 3, 1), i = 0, s = 1)
        val t = 운체크(S, 0, 0, 때)
        assertNull(t.종목들[0].기록.칸(0)); assertEquals(0, t.s)
    }

    // ─────────────── 쉼 게이지 · 건너뛰기 · 다음 종목 ───────────────

    @Test fun 쉼글_넘김() {
        val S = 세션(줄("A", 3, 3), 줄("B"), 휴 = 휴식중(2, 때 + 60_000, 종목 = 0))
        assertEquals(쉼종류.넘김, 쉼글(S, 0, 0))
        assertEquals(쉼종류.건너뛰기, 쉼글(세션(줄("A", 3, 1), 줄("B"), 휴 = 휴식중(0, 때 + 60_000, 종목 = 0)), 0, 0))
    }

    @Test fun 건너뛰기_종목끝이면_다음남은종목으로() {
        // A 끝 · B 끝 · C 남음 · D 남음, A 에서 쉬는 중 → 뒤쪽 먼저: B 는 끝났으니 C
        val S = 세션(줄("A", 3, 3), 줄("B", 3, 3), 줄("C"), 줄("D"), i = 0, s = 3, 휴 = 휴식중(2, 때 + 60_000, 종목 = 0))
        val r = 휴식건너뛰기(S, 0, 때)
        assertNull(r.세션.휴식); assertEquals(2, r.본); assertTrue(r.건너뜀)
        // 다른 종목을 보고 있으면 보는 칸은 그대로
        assertEquals(3, 휴식건너뛰기(S, 3, 때).본)
        // 쉬는 중이 아니면 아무것도 안 한다
        val 안 = 휴식건너뛰기(S.copy(휴식 = null), 0, 때)
        assertFalse(안.건너뜀); assertEquals(0, 안.본)
    }

    @Test fun 다음종목_뒤없으면_앞으로() {
        val S = 세션(줄("A"), 줄("B", 3, 3), 줄("C", 3, 3), i = 0)
        assertEquals(0, 다음종목가기(S, 2, 때).본)
        assertEquals(-1, 다음남은(세션(줄("A", 3, 3)), 0))
        // 마감한 종목은 남은 것으로 치지 않는다
        assertEquals(-1, 다음남은(세션(줄("A", 3, 3), 줄("B", 3, 0, 마감 = true)), 0))
    }

    @Test fun 슈퍼세트_보는칸이_따라감() {
        val S = 세션(줄("A", 슈퍼 = "x"), 줄("B", 슈퍼 = "x"), 줄("C"), i = 1)
        assertEquals(1, 본따라감(S, 0))   // 같은 묶음 → 지금 종목(B)으로
        assertEquals(2, 본따라감(S, 2))   // 다른 종목을 보면 그대로
        assertEquals(0, 본따라감(S.copy(i = 0, 휴식 = 휴식중(0, 때, 종목 = 0)), 1))
    }

    // ─────────────── 세트 지우기 · 되돌리기 ───────────────

    @Test fun 세트지우기_하나면_안지움() {
        assertNull(운세트지우기(세션(줄("A", 1)), 0, 0))
        assertNull(운세트지우기(세션(줄("A")), 0, 7))
        assertNull(운세트지우기(세션(줄("A")), 4, 0))
    }

    @Test fun 세트지우기_아래쉬는줄_번호당김() {
        // A: 0 · 1 한 것, 1 에서 쉬는 중. 0 을 지우면 휴식은 0 줄로
        val S = 세션(줄("A", 3, 2), i = 0, s = 2, 휴 = 휴식중(1, 때 + 60_000, 총초 = 60, 종목 = 0))
        val (t, z) = assertNotNull(운세트지우기(S, 0, 0))
        assertEquals(0, t.휴식?.k); assertEquals(1, t.s)
        assertEquals(S, 세트되살리기(t, z, 때))
    }

    @Test fun 세트지우기_지금세트면_올라온줄값으로() {
        val e = 줄("A").copy(예정값 = listOf(세트(20.0, 10), 세트(30.0, 8), 세트(40.0, 6)))
        val S = 세션(e, i = 0, s = 1).copy(무게 = 30.0, 횟수 = 8)
        val (t, z) = assertNotNull(운세트지우기(S, 0, 1))
        assertEquals(1, t.s); assertEquals(40.0, t.무게); assertEquals(6, t.횟수)
        assertEquals(S, 세트되살리기(t, z, 때))
    }

    @Test fun 세트지우기_쉬던줄_되돌리면_휴식도() {
        val S = 세션(줄("A", 3, 2), i = 0, s = 2, 휴 = 휴식중(1, 때 + 60_000, 총초 = 60, 종목 = 0))
        val (t, z) = assertNotNull(운세트지우기(S, 0, 1))
        assertNull(t.휴식)
        assertEquals(S, 세트되살리기(t, z, 때))
        // 휴식이 그 사이 끝났으면 휴식은 안 살린다
        assertNull(세트되살리기(t, z, 때 + 120_000).휴식)
    }

    @Test fun 세트지우기_순서바뀐뒤_되돌려도_같은종목에() {
        val S = 세션(줄("A", 3, 1), 줄("B"), i = 0, s = 1)
        val (t, z) = assertNotNull(운세트지우기(S, 0, 2))
        val 바뀐 = t.copy(종목들 = t.종목들.reversed(), i = 1)
        val u = 세트되살리기(바뀐, z, 때)
        assertEquals(3, u.종목들[1].세트); assertEquals(3, u.종목들[1].기록.size)
        assertEquals(3, u.종목들[0].세트)
        // 다른 운동이면 아무것도 안 한다
        assertEquals(t.copy(시작시각 = 9L), 세트되살리기(t.copy(시작시각 = 9L), z, 때))
        // 그 종목이 빠졌으면 그대로
        val 뺌 = t.copy(종목들 = listOf(t.종목들[1]), i = 0)
        assertEquals(뺌, 세트되살리기(뺌, z, 때))
    }

    // ─────────────── 지표 (1RM · 1주 · 최고) ───────────────

    @Test fun 지표_최고와_1주() {
        val 기록 = mapOf(
            "2026-10-01" to 날기록("r", "R", true, listOf(종목기록("벤치", listOf(세트(100.0, 1), 세트(60.0, 10, 1))))),   // 워밍업(60×10=80) 은 뺀다
            "2026-09-01" to 날기록("r", "R", true, listOf(종목기록("벤치", listOf(세트(110.0, 1))))),
            "2026-10-05" to 날기록("r", "R", true, listOf(종목기록("벤치", listOf(세트(200.0, 1))))),   // 오늘 — 뺀다
            "2026-10-02~2" to 날기록("r", "R", true, listOf(종목기록("다른", listOf(세트(300.0, 1)), 종id = "x"))),
        )
        assertEquals(110.0 to 100.0, 운최고(기록, "벤치", "2026-10-05", 맨 = false))
        assertEquals(300.0 to 300.0, 운최고(기록, "x", "2026-10-05", 맨 = false))
        assertEquals((null as Double?) to (null as Double?), 운최고(기록, "없음", "2026-10-05", 맨 = false))
        assertEquals(10.0, 운최고(mapOf("2026-10-01" to 날기록("r", "R", true, listOf(종목기록("턱걸이", listOf(세트(0.0, 10), 세트(0.0, 7)))))), "턱걸이", "2026-10-05", 맨 = true).first)
    }

    @Test fun 지표_오늘값과_차() {
        assertNull(운오늘값(emptyList(), false))
        assertNull(운오늘값(listOf(세트(50.0, 5, 1)), false))   // 워밍업만
        assertEquals(12.0, 운오늘값(listOf(세트(0.0, 12), 세트(0.0, 8)), true))
        assertEquals(2.5, 운차(102.4, 100.0)); assertEquals(-1.0, 운차(99.0, 100.0)); assertEquals(0.0, 운차(100.1, 100.0))
        assertNull(운차(null, 1.0)); assertNull(운차(1.0, null))
    }

    // ─────────────── 칸 폭 · 칸 줄 ───────────────

    @Test fun 격자폭_넉넉하면_비율대로_좁으면_최소() {
        val 최소 = listOf(44f, 72f, 56f, 86f); val fr = listOf(66f, 80f, 102f, 104f)
        val 넓 = 격자폭(704f, 최소, fr)
        assertEquals(132f, 넓[0], 0.01f); assertEquals(208f, 넓[3], 0.01f)
        val 좁 = 격자폭(250f, 최소, fr)   // 최소 합 258 보다 좁다
        assertEquals(최소, 좁)
        val 중 = 격자폭(300f, 최소, fr)
        assertEquals(300f, 중.sum(), 0.01f); assertTrue(중.zip(최소).all { (a, b) -> a >= b - 0.01f })
    }

    @Test fun 단추넷_폭() {
        val l = 단추폭(336f, 8f, listOf(0.125f, 0.475f, 0.275f, 0.125f))
        assertEquals(336f, l.sum() + 24f, 0.01f)
        assertEquals(39f, l[0], 0.01f); assertEquals(148.2f, l[1], 0.01f)
    }

    @Test fun 칸줄_보는칸_맨앞_붙박이더() {
        assertEquals(0f, 칸줄목표(0, 0f, 32f, 500f))
        assertEquals(120f, 칸줄목표(2, 152f, 32f, 500f))
        assertEquals(100f, 칸줄목표(9, 684f, 32f, 100f))   // 끝을 넘지 않는다
        assertEquals(0f, 칸줄목표(3, 228f, 32f, 0f))       // 넘치지 않으면 그대로
        assertEquals(228f, 더칸자리(228f, 0f, 400f, 44f))   // 칸이 적으면 마지막 칸 뒤
        assertEquals(356f, 더칸자리(684f, 0f, 400f, 44f))   // 넘치면 오른쪽 끝
    }

    @Test fun 첫빈칸() {
        assertEquals(1, 첫빈칸(줄("A", 3, 1)))
        assertEquals(-1, 첫빈칸(줄("A", 3, 3)))
        assertEquals(2, 첫빈칸(세션종목("A", 4, 4, 0.0, 1, 60, 기록 = listOf(세트(1.0, 1), 세트(1.0, 1)))))
        assertEquals(1, 줄("A", 3, 1).찬것().size)
    }

    // ─────────────── '+ 세트' 자동 스크롤 (10-06 v22 W ⑬) ───────────────

    @Test fun 세트더내림_넘을때만() {
        // 보이는 칸 400 · 넘김 0 · 끝 600 · 아래 여백 8
        assertNull(세트더내림(380, 0, 400, 600, 8))        // 단추가 다 보인다 → 그대로
        assertNull(세트더내림(400, 0, 400, 600, 8))        // 딱 아래끝 → 그대로
        assertEquals(52, 세트더내림(444, 0, 400, 600, 8))  // 44 넘었다 → 단추 + 여백이 보이게
        assertEquals(600, 세트더내림(1200, 0, 400, 600, 8)) // 끝을 넘지 않는다
        assertNull(세트더내림(500, 0, 400, 0, 8))           // 넘치지 않는 목록(끝 0) → 그대로
        assertNull(세트더내림(500, 300, 400, 600, 8))      // 이미 내려가 있어 보인다 → 그대로(올리지 않는다)
        assertEquals(152, 세트더내림(544, 100, 400, 600, 8))
        assertNull(세트더내림(408, 0, 400, 1, 8))           // 1 이하로만 움직일 때는 그대로
        assertNull(세트더내림(500, 0, 0, 600, 8))           // 아직 안 쟀다
    }
}
