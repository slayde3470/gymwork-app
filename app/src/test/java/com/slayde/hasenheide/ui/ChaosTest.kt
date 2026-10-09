package com.slayde.hasenheide.ui

import com.slayde.hasenheide.data.루틴
import com.slayde.hasenheide.data.루틴종목
import com.slayde.hasenheide.data.세션종목
import com.slayde.hasenheide.data.세트
import com.slayde.hasenheide.data.값고치기
import com.slayde.hasenheide.data.목표
import com.slayde.hasenheide.data.오늘반영
import com.slayde.hasenheide.data.운동세션
import com.slayde.hasenheide.data.운동시작
import com.slayde.hasenheide.data.총칸
import com.slayde.hasenheide.data.한세트수
import kotlin.test.Test
import kotlin.test.assertEquals
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
}
