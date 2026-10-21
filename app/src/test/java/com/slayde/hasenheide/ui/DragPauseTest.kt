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

    @Test fun 가장자리_자동넘김은_깊이_들어갈수록_빠르다() {
        // 화면 틀 위 100 · 아래 900 · 가장자리 띠 80 · 최대 10
        fun 양(y: Float) = 끌기넘김량(y, 100f, 900f, 80f, 10f)
        assertEquals(0f, 양(500f))                 // 가운데: 안 넘김
        assertEquals(0f, 양(180f))                 // 띠 경계(100+80): 아직
        assertEquals(0f, 양(820f))
        assertEquals(true, 양(179f) < 0f && 양(179f) > -2f)    // 막 들어섬: 최대의 15% 쯤
        assertEquals(-10f, 양(100f))               // 가장자리: 최대 (위쪽 = 음수)
        assertEquals(-10f, 양(50f))                // 밖으로 나가도 최대를 넘지 않는다
        assertEquals(true, 양(120f) < 양(160f))    // 깊이 들어갈수록 더 음수(빠름)
        assertEquals(10f, 양(900f))                // 아래쪽 = 양수
        assertEquals(10f, 양(990f))
        assertEquals(true, 양(880f) > 양(840f))
        assertEquals(0f, 끌기넘김량(500f, 0f, 1000f, 0f, 10f))   // 띠 0 이면 안 넘김
    }

    @Test fun 켜진_뒤에는_더_넓게_움직여도_켜_둔다() {
        val 켜진흔들 = 90f   // 켜기 전 30 · 켜진 뒤 90 (10dp · 30dp 비율)
        val m = 묶기멈춤(500)
        m.갱신(2, null, 100f, 0, 흔들)
        assertEquals(2, m.갱신(2, null, 100f, 500, 흔들))                                           // 켜짐
        assertEquals(2, m.갱신(2, null, 170f, 600, 흔들, 켜짐 = 2, 켜진흔들림px = 켜진흔들))           // 70 움직여도 (90 안) 그대로
        assertEquals(2, m.갱신(null, null, 160f, 700, 흔들, 켜짐 = 2, 켜진흔들림px = 켜진흔들))        // 가운데를 벗어나도 (후보 없음) 그대로
        assertNull(m.갱신(null, null, 220f, 800, 흔들, 켜짐 = 2, 켜진흔들림px = 켜진흔들))             // 120 움직임 → 꺼짐
    }

    @Test fun 켜지기_전에는_옛날처럼_좁다() {
        val m = 묶기멈춤(500)
        m.갱신(2, null, 100f, 0, 흔들)
        assertNull(m.갱신(2, null, 170f, 300, 흔들, 켜짐 = null, 켜진흔들림px = 90f))   // 아직 안 켜졌으니 70 움직이면 다시 센다
    }
}
