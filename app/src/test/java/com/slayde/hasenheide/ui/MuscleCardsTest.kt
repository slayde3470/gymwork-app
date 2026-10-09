package com.slayde.hasenheide.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * 근육 그림 2장 (10-09 홍겸 님 — MuscleCards.kt) 반신 · 확대 고르기 순수 함수.
 * 점수 = 주동(P) 2 · 협응(Y/S) 1. 반신 동점 = 전면 먼저, 그 안에서 상반신 먼저. 확대 동점 = 가슴 → 등 → 팔~어깨 → 하체
 */
class MuscleCardsTest {

    private fun 고름(vararg m: Pair<String, String>) = 근육두장고름(mapOf(*m))

    @Test fun 벤치프레스는_상반신_전면_팔어깨_확대() {
        val r = 고름("chest_mid" to "P", "delt_front" to "Y", "triceps" to "Y")
        assertEquals(반신.상전, r.반신)
        assertEquals(확대.팔어깨, r.확대)
    }

    @Test fun 가슴만이면_가슴_확대() {
        val r = 고름("chest_mid" to "P", "chest_upper" to "Y")
        assertEquals(반신.상전, r.반신)
        assertEquals(확대.가슴, r.확대)
    }

    @Test fun 스쿼트는_하반신_전면_하체_확대() {
        val r = 고름("quads" to "P", "glutes" to "Y", "hamstrings" to "Y", "adductors" to "Y")
        assertEquals(반신.하전, r.반신)
        assertEquals(확대.하체, r.확대)
    }

    @Test fun 데드리프트는_하반신_후면() {
        val r = 고름("hamstrings" to "P", "glutes" to "P", "lower_back" to "Y", "traps" to "Y")
        assertEquals(반신.하후, r.반신)   // 하후 4 · 상후 2
        assertEquals(확대.하체, r.확대)   // 하체 4 · 등 2
    }

    @Test fun 풀업은_상반신_후면_등_확대() {
        val r = 고름("lats" to "P", "rhomboids" to "Y", "biceps" to "Y")
        assertEquals(반신.상후, r.반신)
        assertEquals(확대.등, r.확대)
    }

    @Test fun 복근만이면_복근_확대_규칙없음() {
        val r = 고름("abs" to "P", "obliques" to "Y")
        assertEquals(반신.상전, r.반신)
        assertEquals(확대.복근, r.확대)
        assertEquals("규칙 없음", 확대.복근.둘째줄())
    }

    @Test fun 근육이_없으면_전신_한_칸() {
        val r = 근육두장고름(emptyMap())
        assertNull(r.반신)
        assertNull(r.확대)
    }

    @Test fun 반신_동점은_전면_먼저_그_안에서_상반신_먼저() {
        assertEquals(반신.상전, 고름("biceps" to "P", "triceps" to "P").반신)
        assertEquals(반신.상전, 고름("biceps" to "P", "quads" to "P").반신)
        assertEquals(반신.하전, 고름("quads" to "P", "lats" to "P").반신)
        assertEquals(반신.상후, 고름("lats" to "P", "glutes" to "P").반신)
    }

    @Test fun 확대_동점은_가슴_등_팔어깨_하체_순() {
        assertEquals(확대.등, 고름("lats" to "P", "quads" to "P").확대)
        assertEquals(확대.등, 고름("lats" to "P", "biceps" to "P").확대)
        assertEquals(확대.팔어깨, 고름("biceps" to "P", "quads" to "P").확대)
    }

    @Test fun 가슴이_팔_어깨와_함께_있으면_점수와_상관없이_팔어깨() {
        assertEquals(확대.팔어깨, 고름("chest_mid" to "P", "chest_upper" to "P", "delt_side" to "Y").확대)
        assertEquals(확대.팔어깨, 고름("chest_lower" to "Y", "forearm" to "Y").확대)
        // 가슴과 등만이면 점수로
        assertEquals(확대.가슴, 고름("chest_mid" to "P", "lats" to "Y").확대)
    }

    @Test fun 다른_층_근육은_세부_부위로_옮겨_센다() {
        // 큰 근육 id(대흉근)는 가슴 세 갈래로 내려간다
        val r = 고름("pectoralis_major" to "P")
        assertEquals(반신.상전, r.반신)
        assertEquals(확대.가슴, r.확대)
    }

    @Test fun 확대_그림_번호() {
        // 10-09 새 그림: 가슴 07 정면 · 팔~어깨 04 · 등 06 · 굽힌 다리 05 · 복근은 그림 없음
        assertEquals("07", 확대.가슴.번호)
        assertEquals("04", 확대.팔어깨.번호)
        assertEquals("06", 확대.등.번호)
        assertEquals("05", 확대.하체.번호)
        assertNull(확대.복근.번호)
        assertEquals("가슴 확대", 확대.가슴.이름)
    }
}
