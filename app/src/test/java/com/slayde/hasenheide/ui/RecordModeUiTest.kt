package com.slayde.hasenheide.ui

import com.slayde.hasenheide.data.기록방식
import com.slayde.hasenheide.data.몸조건
import com.slayde.hasenheide.data.앱데이터
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** 기록 방식 1단계 화면 글 (10-12) — 무게 칸 머리 글 · 칸 아래 '= 69kg'. 그리는 부분(Compose)은 안 본다 */
class RecordModeUiTest {
    private val d = 앱데이터(몸 = 몸조건(체중 = 70.0))

    @Test fun 머리글() {
        assertEquals("kg", 무게머리글(emptyList(), "kg"))
        assertEquals("추가 kg", 무게머리글(listOf(기록방식.맨몸), "무게 kg"))
        assertEquals("추가 kg", 무게머리글(listOf(기록방식.맨몸, 기록방식.맨몸), "kg"))
        assertEquals("보조 kg", 무게머리글(listOf(기록방식.어시스트), "무게 kg"))
        assertEquals("무게 kg", 무게머리글(listOf(기록방식.맨몸, 기록방식.어시스트), "무게 kg"))
        assertEquals("kg", 무게머리글(listOf(기록방식.맨몸, 기록방식.무게), "kg"))
        assertEquals("무게 kg", 무게머리글(listOf(기록방식.무게), "무게 kg"))
    }

    @Test fun 머리글_무게칸없는_방식() {
        assertEquals("", 무게머리글(listOf(기록방식.시간), "무게 kg"))
        assertEquals("", 무게머리글(listOf(기록방식.시간, 기록방식.횟수만), "kg"))
        assertEquals("추가 kg", 무게머리글(listOf(기록방식.맨몸, 기록방식.시간), "kg"))
        assertEquals("무게 kg", 무게머리글(listOf(기록방식.무게, 기록방식.횟수만), "무게 kg"))
        assertEquals("시간", 횟수머리글(listOf(기록방식.시간)))
        assertEquals("시간", 횟수머리글(listOf(기록방식.시간, 기록방식.시간), "회"))
        assertEquals("횟수", 횟수머리글(listOf(기록방식.시간, 기록방식.횟수만)))
        assertEquals("회", 횟수머리글(listOf(기록방식.무게), "회"))
        assertEquals("횟수", 횟수머리글(emptyList()))
    }

    @Test fun 머리글_유산소() {
        assertEquals("거리 km", 무게머리글(listOf(기록방식.유산소), "무게 kg"))
        assertEquals("거리 km", 무게머리글(listOf(기록방식.유산소, 기록방식.유산소, 기록방식.시간), "kg"))
        assertEquals("무게 kg", 무게머리글(listOf(기록방식.유산소, 기록방식.무게), "무게 kg"))   // 섞이면 기본
        assertEquals("시간", 횟수머리글(listOf(기록방식.유산소)))
        assertEquals("시간", 횟수머리글(listOf(기록방식.유산소, 기록방식.시간)))
        assertEquals("횟수", 횟수머리글(listOf(기록방식.유산소, 기록방식.무게)))
        assertNull(d.실제무게글(null, "러닝", null, 5.0))   // 칸 아래 '= kg' 글은 없다
    }

    @Test fun 머리방식_플랜줄은_뺀다() {
        assertNull(d.머리방식(null, "턱걸이", "플랜1"))
        assertEquals(기록방식.맨몸, d.머리방식(null, "푸시업", null))
    }

    @Test fun 칸아래글() {
        assertEquals("= 75kg", d.실제무게글(null, "푸시업", null, 5.0))
        assertEquals("= 70kg", d.실제무게글(null, "푸시업", null, 0.0))
        assertEquals("= 55.5kg", d.실제무게글(null, "어시스트 풀업", null, 14.5))
        assertEquals("= 0kg", d.실제무게글(null, "어시스트 풀업", null, 90.0))
        assertNull(d.실제무게글(null, "벤치프레스", null, 60.0))        // 일반 종목
        assertNull(d.실제무게글(null, "푸시업", "플랜1", 5.0))          // 플랜 줄
        assertNull(앱데이터().실제무게글(null, "푸시업", null, 5.0))     // 체중 0
        assertNull(d.실제무게글(null, "플랭크", null, 30.0))            // 시간 종목
    }
}
