package com.slayde.hasenheide.ui

import com.slayde.hasenheide.data.루틴
import com.slayde.hasenheide.data.루틴종목
import com.slayde.hasenheide.data.세션종목
import com.slayde.hasenheide.data.세트
import com.slayde.hasenheide.data.값고치기
import com.slayde.hasenheide.data.끝냄
import com.slayde.hasenheide.data.보고끝
import com.slayde.hasenheide.data.보고저장
import com.slayde.hasenheide.data.앱데이터
import com.slayde.hasenheide.data.예정초기화
import com.slayde.hasenheide.data.오래된운동정리
import com.slayde.hasenheide.data.운동저장하기
import com.slayde.hasenheide.data.재개
import com.slayde.hasenheide.data.저장기록열쇠
import com.slayde.hasenheide.data.목표
import com.slayde.hasenheide.data.오늘반영
import com.slayde.hasenheide.data.운동세션
import com.slayde.hasenheide.data.운동시작
import com.slayde.hasenheide.data.총칸
import com.slayde.hasenheide.data.한세트수
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 10-10 마구잡이 조작 점검에서 잡은 버그의 고정 시험.
 * (22_버그_비선형조작 · 세트를 지우고 순서 없이 체크하고 되돌리는 흐름)
 */
class ChaosTest {
    private val 때 = 1_000_000L

    private val 루틴A = 루틴("r", "R", 종목 = listOf(
        루틴종목("벤치", 3, 20.0, 10, 60, 세트값 = listOf(세트(20.0, 10), 세트(30.0, 8), 세트(40.0, 6)), 휴식값 = listOf(60, 60, 60)),
        루틴종목("컬", 2, 12.0, 12, 60),
    ))

    private fun 시작() = assertNotNull(운동시작(루틴A, 때))

    @Test fun 시작하면_세트번호표가_붙는다() {
        assertEquals(listOf(0, 1, 2), 시작().종목들[0].원번호)
    }

    @Test fun 첫세트를_지우고_하나_체크해도_루틴_둘째값이_첫째로_안밀린다() {
        // 벤치 0 세트를 지우면 남은 칸은 [원래 1, 원래 2]. 첫 칸(원래 1 = 30x8)을 체크
        var S = 시작()
        S = 운세트지우기(S, 0, 0)!!.first
        assertEquals(listOf<Int?>(1, 2), S.종목들[0].원번호)
        S = 운체크(S, 0, 0, 때)
        S = S.값고치기(0, 0, 새무게 = 31.0, 새횟수 = null)
        val 새 = 루틴A.오늘반영(S)
        val 벤치 = 새.종목[0]
        // 루틴의 첫 세트(20x10)는 건드리지 않고, 두 번째 세트(30→31)만 바뀐다
        assertEquals(세트(20.0, 10), 벤치.목표(0))
        assertEquals(31.0, 벤치.목표(1).w)
        assertEquals(세트(40.0, 6), 벤치.목표(2))
    }

    @Test fun 지웠다_되살리면_원번호도_제자리() {
        val S = 시작()
        val (t, z) = assertNotNull(운세트지우기(S, 0, 1))
        assertEquals(listOf<Int?>(0, 2), t.종목들[0].원번호)
        assertEquals(S, 세트되살리기(t, z, 때))
    }

    @Test fun 지운뒤_지금세트가_한_줄_위에_있지_않다() {
        var S = 시작()
        S = 운체크(S, 0, 0, 때)
        S = 운세트지우기(S, 0, 0)!!.first
        val e = S.종목들[0]
        // 남은 칸 중 지금 세트(s)는 이미 한 줄이면 안 된다
        if (S.s < e.총칸() && !S.끝화면) assertNull(e.기록.getOrNull(S.s))
    }

    @Test fun 달성도는_계획을_넘지_않는다() {
        var S = 시작()
        S = 운세트지우기(S, 0, 2)!!.first
        for (k in 0 until S.종목들[0].총칸()) S = 운체크(S, 0, k, 때 + k)
        assertTrue(S.한세트수() <= S.종목들[0].계획세트 + S.종목들[1].계획세트)
        assertEquals(2, S.종목들[0].계획세트)
    }

    // ─────────────── 30일 추적 시험(Month 시험기)에서 잡은 것 (10-10) ───────────────

    private val T0 = 1_000_000_000_000L
    // 10-10: 보고서 저장은 운동을 시작한 날 열쇠로 들어간다 → T0 가 속한 날
    private val 날 = java.time.Instant.ofEpochMilli(T0).atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString()

    private fun 체크한세션(루틴: 루틴, t: Long): 운동세션 {
        var S = assertNotNull(운동시작(루틴, t))
        S = 운체크(S, 0, 0, t + 1000)
        return S.끝냄(t + 5000)
    }

    @Test fun 보고서에서_이어한_뒤_캘린더에서_앞_기록을_지워도_다시_끝내면_기록은_하나() {
        val 루 = 루틴("r", "R", 종목 = listOf(루틴종목("벤치", 3, 20.0, 10, 60)), 자동생성 = true)
        // 아침 운동 A 가 이미 있고, 저녁 운동 C 를 보고서에서 한 번 저장(~2) 한 뒤 ‹ 로 돌아왔다
        val d0 = 앱데이터(루틴들 = listOf(루)).copy(세션 = 체크한세션(루, T0)).운동저장하기(날, T0 + 6000)
        val d1 = d0.copy(세션 = 체크한세션(루, T0 + 100_000))
        val (d2, _) = d1.보고저장(날, T0 + 106_000)
        assertEquals(setOf(날, "$날~2"), d2.기록.keys)
        val d2b = d2.copy(세션 = d2.세션!!.재개(T0 + 107_000))
        // 캘린더에서 아침 기록 A 를 지운다 → C 가 첫 기록 열쇠로 옮겨 간다
        val (d3, _) = assertNotNull(d2b.캘기록지우기(날, setOf(날)))
        assertEquals(setOf(날), d3.기록.keys)
        assertEquals(날, d3.저장기록열쇠(d3.세션!!))   // 옛 열쇠(~2)가 아니라 지금 열쇠
        // 다시 끝내고 저장 → 덮어쓰기 (겹치지 않는다)
        val d4 = d3.copy(세션 = d3.세션!!.끝냄(T0 + 109_000)).보고저장(날, T0 + 110_000).first
        assertEquals(1, d4.기록.size)
    }

    @Test fun 이어하던_운동의_저장_기록을_지웠으면_다시_끝낼_때_새로_저장() {
        val 루 = 루틴("r", "R", 종목 = listOf(루틴종목("벤치", 3, 20.0, 10, 60)))
        val (d1, _) = 앱데이터(루틴들 = listOf(루)).copy(세션 = 체크한세션(루, T0)).보고저장(날, T0 + 6000)
        val d2 = d1.copy(세션 = d1.세션!!.재개(T0 + 7000))
        val (d3, _) = assertNotNull(d2.캘기록지우기(날, setOf(날)))
        assertNull(d3.저장기록열쇠(d3.세션!!))
        val d4 = d3.copy(세션 = d3.세션!!.끝냄(T0 + 9000)).보고저장(날, T0 + 10_000).first
        assertEquals(1, d4.기록.size)
    }

    @Test fun 자정을_걸쳐_저장한_날의_미실시는_지운다() {
        val 루 = 루틴("r", "R", 종목 = listOf(루틴종목("벤치", 3, 20.0, 10, 60)))
        val d = 앱데이터(루틴들 = listOf(루), 미실시 = mapOf(날 to "R")).copy(세션 = 체크한세션(루, T0))
        assertFalse(날 in d.운동저장하기(날, T0 + 6000).미실시)
    }

    @Test fun 기록을_되살리면_그_날의_미실시도_뺀다() {
        val 루 = 루틴("r", "R", 종목 = listOf(루틴종목("벤치", 3, 20.0, 10, 60)))
        val d0 = 앱데이터(루틴들 = listOf(루)).copy(세션 = 체크한세션(루, T0)).운동저장하기(날, T0 + 6000)
        val (지운, x) = assertNotNull(d0.캘기록지우기(날, setOf(날)))
        val 되 = 지운.copy(미실시 = 지운.미실시 + (날 to "R")).캘기록되살림(x)
        assertEquals(1, 되.기록.size)
        assertFalse(날 in 되.미실시)
    }

    @Test fun 다시_저장해도_지운_루틴이_예정에_되살아나지_않는다() {
        val a = 루틴("a", "A", 종목 = listOf(루틴종목("벤치", 3, 20.0, 10, 60)), 자동생성 = true)
        val b = 루틴("b", "B", 종목 = listOf(루틴종목("컬", 3, 10.0, 12, 60)), 자동생성 = true)
        val d0 = 앱데이터(루틴들 = listOf(a, b)).예정초기화(날)
        val (d1, _) = d0.copy(세션 = 체크한세션(a, T0)).보고저장(날, T0 + 6000)
        // 이어하다 말고 루틴 A(지금 하는 운동의 루틴)를 지운다
        val d2 = d1.copy(세션 = d1.세션!!.재개(T0 + 7000)).let { x ->
            x.copy(루틴들 = x.루틴들.filter { it.id != "a" }, 예정 = x.예정.filterValues { it != "a" })
        }
        val d3 = d2.copy(세션 = d2.세션!!.copy(마지막 = T0 + 7000)).오래된운동정리(T0 + 7000 + 4 * 3600_000L)
        assertTrue(d3.예정.values.all { id -> d3.루틴(id) != null }, "예정 ${d3.예정}")
    }

    // ─────────────── 시작한 날로 저장 (10-10 홍겸 님) ───────────────

    @Test fun 자정을_넘겨_끝낸_운동은_시작한_날로_저장된다() {
        val 루 = 루틴("r", "R", 종목 = listOf(루틴종목("벤치", 3, 20.0, 10, 60)))
        val 다음날 = java.time.LocalDate.parse(날).plusDays(1).toString()
        val S = 체크한세션(루, T0)
        // 보고서가 열리는 순간 — 앱은 '오늘'(= 자정 뒤 다음 날)을 넘기지만 기록은 시작한 날 열쇠
        val (d1, _) = 앱데이터(루틴들 = listOf(루)).copy(세션 = S).보고저장(다음날, T0 + 6000)
        assertEquals(setOf(날), d1.기록.keys)
        assertEquals(날, d1.세션?.저장?.열쇠)
        // 저장 전에 탭으로 나가는 길(보고끝)도 같다
        val d2 = 앱데이터(루틴들 = listOf(루)).copy(세션 = S).보고끝(다음날, T0 + 6000)
        assertEquals(setOf(날), d2.기록.keys)
        // 같은 날 먼저 한 운동이 있으면 '한 번 더' 열쇠 — 다음 날 열쇠가 아니다
        val d3 = d2.copy(세션 = 체크한세션(루, T0 + 100_000)).보고저장(다음날, T0 + 106_000).first
        assertEquals(setOf(날, "$날~2"), d3.기록.keys)
    }

    @Test fun 자정을_넘겨_끝낸_운동_뒤_다음날_예정은_다음_차례_미실시는_지운다() {
        val a = 루틴("a", "A", 종목 = listOf(루틴종목("벤치", 3, 20.0, 10, 60)), 자동생성 = true)
        val b = 루틴("b", "B", 종목 = listOf(루틴종목("컬", 3, 10.0, 12, 60)), 자동생성 = true)
        val 다음날 = java.time.LocalDate.parse(날).plusDays(1).toString()
        // 시작한 날 예정은 A · 다음 날은 B. 자정 뒤 앱이 시작한 날을 '미실시'로 먼저 담아 둔 상태
        val d0 = 앱데이터(루틴들 = listOf(a, b), 미실시 = mapOf(날 to "A")).예정초기화(날)
        val (d1, _) = d0.copy(세션 = 체크한세션(a, T0)).보고저장(다음날, T0 + 6000)
        assertEquals(setOf(날), d1.기록.keys)
        assertFalse(날 in d1.미실시, "시작한 날에 기록이 생겼는데 미실시가 남았다")
        assertEquals("b", d1.예정[다음날], "A 를 한 다음 차례는 B")
    }
}
