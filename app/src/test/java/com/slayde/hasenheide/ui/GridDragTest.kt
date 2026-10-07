package com.slayde.hasenheide.ui

import androidx.compose.ui.geometry.Rect
import com.slayde.hasenheide.data.종목
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** 격자 꾹 눌러 끌기 (10-07 홍겸 님 — 넣기 시트 · 종목 탭) 순수 계산 */
class GridDragTest {
    private val 표 = listOf(종목("A", "가슴", id = "a"), 종목("B", "가슴", id = "b"), 종목("C", "등", id = "c"), 종목("D", "가슴", id = "d"))

    @Test fun 옮김_앞뒤() {
        assertEquals(listOf("b", "c", "a", "d"), 종목옮김(표, "a", "c", true).map { it.id })
        assertEquals(listOf("d", "a", "b", "c"), 종목옮김(표, "d", "a", false).map { it.id })
        assertEquals(listOf("a", "b", "d", "c"), 종목옮김(표, "d", "c", false).map { it.id })
    }

    @Test fun 옮김_없거나_같으면_그대로() {
        assertEquals(표, 종목옮김(표, "a", "a", true))
        assertEquals(표, 종목옮김(표, "x", "a", true))
        assertEquals(표, 종목옮김(표, "a", "x", true))
    }

    @Test fun 대상_반으로_앞뒤_같은무리만() {
        val 자리 = mapOf("a" to Rect(0f, 0f, 100f, 50f), "b" to Rect(110f, 0f, 210f, 50f), "c" to Rect(0f, 60f, 100f, 110f))
        val 무리 = mapOf("a" to "가슴", "b" to "가슴", "c" to "등")
        assertEquals("b" to false, 격자대상(자리, 무리, "a", 140f, 20f))
        assertEquals("b" to true, 격자대상(자리, 무리, "a", 200f, 20f))
        assertNull(격자대상(자리, 무리, "a", 50f, 80f))   // 다른 카테고리
        assertNull(격자대상(자리, 무리, "a", 50f, 20f))   // 자기 칸
        assertNull(격자대상(자리, 무리, "a", 105f, 20f))  // 틈
    }
}
