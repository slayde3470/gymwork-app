package com.slayde.hasenheide.ui

import com.slayde.hasenheide.data.날기록
import com.slayde.hasenheide.data.루틴
import com.slayde.hasenheide.data.루틴종목
import com.slayde.hasenheide.data.목표세트
import com.slayde.hasenheide.data.세트
import com.slayde.hasenheide.data.세트종류
import com.slayde.hasenheide.data.앱데이터
import com.slayde.hasenheide.data.오늘볼륨
import com.slayde.hasenheide.data.종목기록
import com.slayde.hasenheide.data.한세트수
import com.slayde.hasenheide.data.흐른초
import java.time.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 캘린더 (앱 옮기기 2단계 C · 10-05) — CalendarScreen.kt 의 순수 계산.
 *  · 같은 날 기록 여러 개 — 골라 지우기 · 되돌리기(차례 그대로) · 여러 번 지우고 새것부터 되돌리기
 *  · 첫 기록을 지워 '~2' 만 남는 일이 없게 다시 놓기
 *  · 칸 글 · 옮길 수 있나 · 세트 글 · 두 칸 목록 배치 · 지난 기록 → 보고서 세션
 */
class CalendarTest {

    private fun 기록(이름: String, 달성: Boolean = true, w: Double = 60.0, n: Int = 3, 초: Int = 1800, 끝: Long = 0L) =
        날기록("r-$이름", 이름, 달성, listOf(종목기록("벤치프레스", List(n) { 세트(w, 10) })), 걸린초 = 초, 끝시각 = 끝)

    private val 하체 = 기록("하체")
    private val 가슴 = 기록("가슴")
    private val 등 = 기록("등")
    private val d0 = 앱데이터(
        기록 = mapOf("2026-10-03" to 기록("어제"), "2026-10-04" to 하체, "2026-10-04~2" to 가슴, "2026-10-04~3" to 등, "2026-10-05" to 기록("오늘")),
    )

    @Test fun 열쇠차례() {
        assertEquals(1, 캘열쇠차례("2026-10-04"))
        assertEquals(2, 캘열쇠차례("2026-10-04~2"))
        assertEquals(10, 캘열쇠차례("2026-10-04~10"))
        // 글자 순서로는 ~10 이 ~2 앞 — 차례는 숫자로
        val d = 앱데이터(기록 = (1..11).associate { i -> (if (i == 1) "2026-10-04" else "2026-10-04~$i") to 기록("$i") })
        assertEquals((1..11).map { "$it" }, d.캘기록목록("2026-10-04").map { it.second.루틴이름 })
    }

    @Test fun 기록목록과있음() {
        assertEquals(listOf("하체", "가슴", "등"), d0.캘기록목록("2026-10-04").map { it.second.루틴이름 })
        assertTrue(d0.캘기록있음("2026-10-04"))
        assertFalse(d0.캘기록있음("2026-10-06"))
        // '~2' 만 있어도 기록 있는 날
        val d = 앱데이터(기록 = mapOf("2026-10-07~2" to 가슴))
        assertTrue(d.캘기록있음("2026-10-07"))
    }

    // 10-06 홍겸 님 ④: 고른 기록만 지운다 (기본 = 아무것도 안 고름 · 하나뿐인 날도 골라야 지운다)
    @Test fun 하나뿐이어도_골라야_지운다() {
        assertNull(d0.캘기록지우기("2026-10-03", emptySet()))
        val (d, x) = d0.캘기록지우기("2026-10-03", setOf("2026-10-03"))!!
        assertFalse(d.캘기록있음("2026-10-03"))
        assertEquals(1, x.개수)
        assertEquals(d0.기록, d.캘기록되살림(x).기록)
    }

    @Test fun 여럿_골라_지우기_되돌리기() {
        // 하체(날짜 열쇠) · 등(~3)을 고른다 → 가슴이 첫 기록(날짜 열쇠)으로
        val (d, x) = d0.캘기록지우기("2026-10-04", setOf("2026-10-04", "2026-10-04~3"))!!
        assertEquals(2, x.개수)
        assertEquals(listOf("2026-10-04" to 가슴), d.캘기록목록("2026-10-04"))
        assertFalse(d.기록.containsKey("2026-10-04~2"))
        // 다른 날은 그대로
        assertEquals(d0.기록["2026-10-03"], d.기록["2026-10-03"])
        // 되돌리면 처음 차례 · 처음 열쇠 그대로
        assertEquals(d0.기록, d.캘기록되살림(x).기록)
    }

    @Test fun 고른_것이_없으면_안_지운다() {
        assertNull(d0.캘기록지우기("2026-10-04", emptySet()))
        // 그 날 기록에 없는 열쇠만 골랐어도 지우지 않는다
        assertNull(d0.캘기록지우기("2026-10-04", setOf("2026-10-05", "2026-10-04~9")))
        assertNull(d0.캘기록지우기("2026-10-09", setOf("2026-10-09")))
    }

    @Test fun 하나만_골라_지우면_나머지는_그대로() {
        val (d, x) = d0.캘기록지우기("2026-10-04", setOf("2026-10-04~2"))!!   // 가슴만
        assertEquals(1, x.개수)
        assertEquals(listOf("하체", "등"), d.캘기록목록("2026-10-04").map { it.second.루틴이름 })
        assertEquals(listOf("2026-10-04", "2026-10-04~2"), d.캘기록목록("2026-10-04").map { it.first })
    }

    @Test fun 여러_번_지우고_새것부터_되돌리기() {
        // ① 등만 지움 ② 남은 둘 중 하체 지움 ③ 마지막 가슴 지움 — 띠 하나로 합쳐져 새것부터 되돌린다
        val (d1, x1) = d0.캘기록지우기("2026-10-04", setOf("2026-10-04~3"))!!
        assertEquals(listOf("하체", "가슴"), d1.캘기록목록("2026-10-04").map { it.second.루틴이름 })
        val (d2, x2) = d1.캘기록지우기("2026-10-04", setOf("2026-10-04"))!!
        assertEquals(listOf("2026-10-04" to 가슴), d2.캘기록목록("2026-10-04"))
        val (d3, x3) = d2.캘기록지우기("2026-10-04", setOf("2026-10-04"))!!
        assertFalse(d3.캘기록있음("2026-10-04"))
        val 되 = d3.캘기록되살림(x3).캘기록되살림(x2).캘기록되살림(x1)
        assertEquals(d0.기록, 되.기록)
    }

    @Test fun 되돌리기_사이에_새_기록이_생겨도() {
        val (d, x) = d0.캘기록지우기("2026-10-04", setOf("2026-10-04"))!!   // 하체만 지움
        // 그 사이 '한 번 더' 로 새 기록이 ~3 에 생겼다
        val 새 = 기록("새것")
        val 사이 = d.copy(기록 = d.기록 + ("2026-10-04~3" to 새))
        val 되 = 사이.캘기록되살림(x)
        assertEquals(listOf("하체", "가슴", "등", "새것"), 되.캘기록목록("2026-10-04").map { it.second.루틴이름 })
        assertEquals(listOf("2026-10-04", "2026-10-04~2", "2026-10-04~3", "2026-10-04~4"), 되.캘기록목록("2026-10-04").map { it.first })
        // 같은 것을 두 번 되돌려도 둘이 되지 않는다
        assertEquals(되.기록, 되.캘기록되살림(x).기록)
    }

    // 10-06 홍겸 님 ③: 달을 넘기면 고른 날도 같은 '일' (없으면 그 달 마지막 날)
    @Test fun 달넘김_고른날() {
        assertEquals("2026-11-06", 캘달옮긴날("2026-10-06", YearMonth.of(2026, 11)))
        assertEquals("2026-09-06", 캘달옮긴날("2026-10-06", YearMonth.of(2026, 9)))
        assertEquals("2026-11-30", 캘달옮긴날("2026-10-31", YearMonth.of(2026, 11)))
        assertEquals("2026-02-28", 캘달옮긴날("2026-01-31", YearMonth.of(2026, 2)))
        assertEquals("2028-02-29", 캘달옮긴날("2028-01-31", YearMonth.of(2028, 2)))   // 윤년
        assertEquals("2026-12-31", 캘달옮긴날("2026-10-31", YearMonth.of(2026, 12)))
        assertEquals("2027-01-15", 캘달옮긴날("2026-12-15", YearMonth.of(2027, 1)))   // 해를 넘어도
        assertEquals("2026-11-04", 캘달옮긴날("2026-10-04~2", YearMonth.of(2026, 11)))
    }

    @Test fun 칸내용() {
        val 휴 = 루틴("휴", "휴식", 휴식일 = true)
        val 등루틴 = 루틴("등", "등", 종목 = listOf(루틴종목("턱걸이")))
        val d = d0.copy(
            루틴들 = listOf(휴, 등루틴), 예정 = mapOf("2026-10-06" to "등", "2026-10-07" to "휴", "2026-10-02" to "등"),
            미실시 = mapOf("2026-10-02" to "등"),
            기록 = d0.기록 + ("2026-10-01" to 기록("하체", 달성 = false)),
        )
        assertEquals(캘칸값("하체 +2", "달성", 캘칸종류.록), d.캘칸내용("2026-10-04"))
        assertEquals("미달성", d.캘칸내용("2026-10-01")?.상)
        assertEquals(캘칸값("등", "미실시", 캘칸종류.미), d.캘칸내용("2026-10-02"))
        assertEquals(캘칸값("등", null, 캘칸종류.예), d.캘칸내용("2026-10-06"))
        assertEquals(캘칸값("휴식", null, 캘칸종류.휴), d.캘칸내용("2026-10-07"))
        assertNull(d.캘칸내용("2026-10-20"))
        // 하나라도 미달성이면 미달성
        val d2 = d.copy(기록 = d.기록 + ("2026-10-04~2" to 기록("가슴", 달성 = false)))
        assertEquals("미달성", d2.캘칸내용("2026-10-04")?.상)
    }

    @Test fun 세트글() {
        assertEquals("", 캘세트글(emptyList()))
        assertEquals("60kg × 9회", 캘세트글(List(3) { 세트(60.0, 9) }))
        assertEquals("60~65kg × 8~10회", 캘세트글(listOf(세트(60.0, 10), 세트(65.0, 8))))
        assertEquals("맨몸 × 12회", 캘세트글(listOf(세트(0.0, 12))))
        assertEquals("0~2.5kg × 12회", 캘세트글(listOf(세트(0.0, 12), 세트(2.5, 12))))
    }

    @Test fun 두칸배치() {
        assertEquals(캘판배치값(1, false, 1), 캘판배치(1, false))
        assertEquals(캘판배치값(4, false, 2), 캘판배치(4, false))
        assertEquals(캘판배치값(5, false, 3), 캘판배치(5, false))
        assertEquals(캘판배치값(10, false, 5), 캘판배치(10, false))   // 10개까지는 다 보인다
        assertEquals(캘판배치값(9, true, 5), 캘판배치(11, false))     // 9개 + '외 2종목'
        assertEquals(캘판배치값(11, true, 6), 캘판배치(11, true))     // 펼침 11 + '접기'
        assertEquals(캘판배치값(0, false, 0), 캘판배치(0, false))
    }

    @Test fun 같은종목() {
        assertTrue(캘같은종목(null, "벤치프레스", "벤치프레스", "벤치프레스"))         // 옛 줄 ↔ 새 줄 — 이름으로
        assertTrue(캘같은종목("벤치프레스-2", "벤치프레스", "벤치프레스-2", "벤치프레스"))
        assertFalse(캘같은종목("벤치프레스", "벤치프레스", "벤치프레스-2", "벤치프레스"))   // 같은 이름 · 다른 종목
        assertFalse(캘같은종목(null, "스쿼트", null, "벤치프레스"))
    }

    @Test fun 날글() {
        assertEquals("10월 4일", 캘날글("2026-10-04~2"))
        assertEquals("일", 캘요일글("2026-10-04"))
        assertEquals("월", 캘요일글("2026-10-05"))
        assertEquals("토", 캘요일글("2026-10-03"))
    }

    @Test fun 보고서세션() {
        val 끝 = 1_790_000_000_000L
        val r = 루틴("r-하체", "하체", 종목 = listOf(루틴종목("벤치프레스", 세트 = 3)))
        val rec = 날기록("r-하체", "하체", true, listOf(
            종목기록("벤치프레스", listOf(세트(20.0, 10, 세트종류.워밍업), 세트(60.0, 10), 세트(60.0, 10), 세트(60.0, 9))),
        ), 걸린초 = 1500, 끝시각 = 끝)
        val d = 앱데이터(루틴들 = listOf(r), 기록 = mapOf("2026-10-04" to rec))
        val S = d.캘기록세션(rec, "2026-10-04")
        assertTrue(S.끝화면)
        assertEquals(끝, S.끝시각)
        assertEquals(1500L, S.흐른초(0L))
        assertEquals(3, S.한세트수())
        assertTrue(S.한세트수() >= S.목표세트())   // 달성
        assertEquals(60.0 * 10 + 60 * 10 + 60 * 9, S.오늘볼륨())
        // 미달성 기록은 결과에서도 미달성 — 계획을 모르면(루틴이 지워짐) 하나 늘려서
        val 못 = rec.copy(달성 = false)
        val S2 = 앱데이터(기록 = mapOf("2026-10-04" to 못)).캘기록세션(못, "2026-10-04")
        assertTrue(S2.한세트수() < S2.목표세트())
        // 계획이 있으면 그 계획으로 (2/3)
        val 둘 = 날기록("r-하체", "하체", false, listOf(종목기록("벤치프레스", List(2) { 세트(60.0, 10) })), 걸린초 = 600, 끝시각 = 끝)
        val S3 = d.캘기록세션(둘, "2026-10-04")
        assertEquals(2, S3.한세트수()); assertEquals(3, S3.목표세트())
        // 시각을 모르는 옛 기록도 죽지 않는다
        val 옛 = 날기록("x", "옛", true, emptyList())
        assertNotNull(앱데이터().캘기록세션(옛, "2026-10-01"))
    }
}
