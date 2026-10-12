package com.slayde.hasenheide.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** 슈퍼세트 묶기는 종목 가운데에서 0.5초 멈출 때만 (10-12 홍겸 님) — [묶기멈춤] 판정 */
class DragPauseTest {
    private val 흔들 = 30f

    @Test fun 멈추지_않으면_안_묶인다() {
        val m = 묶기멈춤(500)
        assertNull(m.갱신(null, null, 100f, 0, 흔들))
        assertNull(m.갱신(2, null, 100f, 0, 흔들))      // 가운데에 막 들어옴 → 세기 시작
        assertNull(m.갱신(2, null, 100f, 499, 흔들))    // 0.499초 → 아직
        assertEquals(2, m.갱신(2, null, 100f, 500, 흔들))   // 0.5초 → 준비됨
        assertEquals(2, m.갱신(2, null, 100f, 900, 흔들))   // 계속 머물면 그대로
    }

    @Test fun 조금_흔들려도_멈춘_것() {
        val m = 묶기멈춤(500)
        m.갱신(1, null, 100f, 0, 흔들)
        assertNull(m.갱신(1, null, 120f, 300, 흔들))    // 20 움직임 (30 안)
        assertEquals(1, m.갱신(1, null, 90f, 500, 흔들))
    }

    @Test fun 많이_움직이면_다시_센다() {
        val m = 묶기멈춤(500)
        m.갱신(1, null, 100f, 0, 흔들)
        assertNull(m.갱신(1, null, 140f, 400, 흔들))    // 40 움직임 → 다시 시작
        assertNull(m.갱신(1, null, 140f, 800, 흔들))    // 시작한 지 400ms
        assertEquals(1, m.갱신(1, null, 140f, 900, 흔들))
    }

    @Test fun 칸이_바뀌거나_가운데를_벗어나면_다시_센다() {
        val m = 묶기멈춤(500)
        m.갱신(1, null, 100f, 0, 흔들)
        assertNull(m.갱신(2, null, 100f, 400, 흔들))    // 다른 칸으로
        assertNull(m.갱신(2, null, 100f, 800, 흔들))
        assertNull(m.갱신(null, null, 100f, 850, 흔들)) // 가운데를 벗어남
        assertNull(m.갱신(2, null, 100f, 900, 흔들))    // 다시 들어옴 → 처음부터
        assertNull(m.갱신(2, null, 100f, 1399, 흔들))
        assertEquals(2, m.갱신(2, null, 100f, 1400, 흔들))
    }

    @Test fun 놓고_다시_잡으면_처음부터() {
        val m = 묶기멈춤(500)
        m.갱신(3, null, 50f, 0, 흔들)
        assertEquals(3, m.갱신(3, null, 50f, 600, 흔들))
        m.비움()
        assertNull(m.갱신(3, null, 50f, 700, 흔들))
        assertEquals(3, m.갱신(3, null, 50f, 1200, 흔들))
    }

    @Test fun 가로로_움직여도_센다() {
        val m = 묶기멈춤(500)
        m.갱신(1, 10f, 100f, 0, 흔들)
        assertNull(m.갱신(1, 80f, 100f, 400, 흔들))     // x 가 70 움직임 → 다시
        assertNull(m.갱신(1, 80f, 100f, 800, 흔들))
        assertEquals(1, m.갱신(1, 80f, 100f, 900, 흔들))
    }
}
