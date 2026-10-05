package com.slayde.hasenheide.ui

import androidx.compose.ui.unit.dp
import com.slayde.hasenheide.data.날기록
import com.slayde.hasenheide.data.몸조건
import com.slayde.hasenheide.data.설정값
import com.slayde.hasenheide.data.세트
import com.slayde.hasenheide.data.스탯
import com.slayde.hasenheide.data.앱데이터
import com.slayde.hasenheide.data.업적보임바꿈
import com.slayde.hasenheide.data.업적표
import com.slayde.hasenheide.data.인증사진
import com.slayde.hasenheide.data.인증순
import com.slayde.hasenheide.data.인증최대
import com.slayde.hasenheide.data.종목기록
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 옮기기 2단계 PF — 프로필 · 검색 · 스탯/업적 화면의 순수 계산 시험 (10-05).
 * 화면(Compose)은 폰에서만 보인다 — 여기서는 화면이 쓰는 계산만 본다.
 * '뒤죽박죽 사용' — 인증샷 8장 넘게 · 고정 3장 넘게 · 지운 뒤 되돌리기 · 끄는 중 줄이 바뀜 · 연달아 누름
 */
class ProfileTest {

    private fun 세(w: Double, r: Int, 종류: Int = 0) = 세트(w, r, 종류)
    private fun 종(이름: String, vararg s: 세트) = 종목기록(이름, s.toList())
    private fun 기(vararg e: 종목기록) = 날기록("r", "루틴", true, e.toList())
    private val 일반 = 업적표.목록.filter { !it.숨김 }.map { it.번호 }
    private val 숨은 = 업적표.목록.filter { it.숨김 }.map { it.번호 }

    // ─────────────── 업적 띠 n/m (숨은 업적은 달성했을 때만 분모) ───────────────

    @Test fun 업적분모_숨은것은_달성해야_셈() {
        val 빈 = 앱데이터()
        assertEquals(일반.size, 업적분모(빈))
        assertEquals(0, 업적풀린수(빈))
        val 하나 = 앱데이터(업적 = mapOf(일반[0] to 1L))
        assertEquals(일반.size, 업적분모(하나))
        assertEquals(1, 업적풀린수(하나))
        val 숨은하나 = 앱데이터(업적 = mapOf(일반[0] to 1L, 숨은[0] to 2L))
        assertEquals(일반.size + 1, 업적분모(숨은하나))
        assertEquals(2, 업적풀린수(숨은하나))
        // 표에 없는 번호(옛 백업)는 세지 않는다
        val 이상 = 앱데이터(업적 = mapOf("9-99" to 1L))
        assertEquals(0, 업적풀린수(이상))
        assertEquals(일반.size, 업적분모(이상))
    }

    @Test fun 달성쪽_달성한것_새것부터_그다음_못한것() {
        val d = 앱데이터(업적 = mapOf(일반[2] to 100L, 일반[0] to 300L, 숨은[0] to 200L))
        val l = 달성쪽(d)
        assertEquals(listOf(일반[0], 숨은[0], 일반[2]), l.take(3).map { it?.번호 })
        assertNull(l[3])   // 이름표 자리
        assertEquals(업적표.목록.size + 1, l.size)
        assertFalse(l.drop(4).any { it!!.번호 in d.업적 })
        // 같은 때면 표에서 뒤의 것부터
        val 같은때 = 앱데이터(업적 = mapOf(일반[0] to 5L, 일반[1] to 5L))
        assertEquals(listOf(일반[1], 일반[0]), 달성차례(같은때).map { it.번호 })
    }

    @Test fun 업적분류_숨은이_맨앞_짧은이름() {
        val l = 업적분류들()
        assertEquals("숨은", l.first())
        assertEquals(l.size, l.distinct().size)
        assertEquals("영화 · 드라마 패러디", 업적짧은분류("영화 · 드라마 패러디 (짧은 대사만 · 출시 전 확인)"))
        assertEquals("3대 합계", 업적짧은분류("3대 합계"))
        val 숨 = 업적거름(앱데이터(), "숨은")
        assertTrue(숨.all { it.숨김 } && 숨.size == 숨은.size)
        assertEquals(업적표.목록.size, 업적거름(앱데이터(), "전체").size)
    }

    @Test fun 잠긴칭호_가림글() {
        assertEquals("•• •••", 칭호가림글("쇠질 입문자"))
        assertEquals("", 칭호가림글(""))
    }

    // ─────────────── 스탯 ▲▼ · 막대 · 그래프 ───────────────

    @Test fun 스탯차_시안과_같게() {
        assertNull(스탯차글(null, 10.0, true))
        assertEquals("새로" to true, 스탯차글(30.0, null, true))
        assertNull(스탯차글(30.0, null, false))
        assertNull(스탯차글(30.0, 30.04, true))
        assertEquals("▲ +3" to true, 스탯차글(33.2, 30.0, true))
        assertEquals("▼ −0.5" to false, 스탯차글(29.5, 30.0, true))
        assertEquals("▲ +0.2" to true, 스탯차글(30.2, 30.0, true))
    }

    @Test fun 스탯막대_두칸이_넘치지_않음() {
        val (a, b) = 스탯막대두칸(60.0, 40.0)
        assertEquals(0.4f, a, 1e-4f); assertEquals(0.2f, b, 1e-4f)
        val (c, d) = 스탯막대두칸(30.0, 50.0)   // 내렸으면 오른 칸 없음
        assertEquals(0.3f, c, 1e-4f); assertEquals(0f, d, 1e-4f)
        val (e, f) = 스탯막대두칸(150.0, 90.0)  // 100 넘어도 합이 1 을 넘지 않는다
        assertTrue(e + f <= 1.0001f)
        val (g, h) = 스탯막대두칸(20.0, null)
        assertEquals(0f, g, 1e-4f); assertEquals(0.2f, h, 1e-4f)
    }

    @Test fun 그래프_기준_1RM과_막대위() {
        assertEquals(500.0, 스탯그래프표.선위(스탯.근력), 1e-9)          // 120 + 170 + 210
        assertEquals(500.0 * 24, 스탯그래프표.막위(스탯.근력), 1e-9)
        assertEquals(195.0, 스탯그래프표.선위(스탯.밀기), 1e-9)          // 벤치 120 + OHP 75
        assertEquals(110.0, 스탯그래프표.선위(스탯.당기기), 1e-9)        // 로우 110
        assertEquals(380.0, 스탯그래프표.선위(스탯.하체), 1e-9)          // 스쿼트 170 + 데드 210
        assertEquals(100.0, 스탯그래프표.선위(스탯.다양성), 1e-9)        // 점수
        assertEquals(10_000.0, 스탯그래프표.막위(스탯.다양성), 1e-9)
    }

    @Test fun 그래프칸_오늘이_맨오른쪽() {
        val 일 = 스탯그래프칸들("일", "2026-10-05")
        assertEquals(14, 일.size); assertEquals("2026-10-05", 일.last().시); assertEquals("10/5", 일.last().글)
        val 주 = 스탯그래프칸들("주", "2026-10-05")   // 10-05 = 월요일
        assertEquals(8, 주.size); assertEquals("2026-10-05", 주.last().시); assertEquals("2026-10-11", 주.last().끝)
        val 주2 = 스탯그래프칸들("주", "2026-10-04")  // 일요일 → 그 주 월요일 09-28
        assertEquals("2026-09-28", 주2.last().시)
        val 달 = 스탯그래프칸들("달", "2026-03-31")
        assertEquals(6, 달.size); assertEquals("2026-03-01", 달.last().시); assertEquals("2026-03-31", 달.last().끝)
        assertEquals("2025-10-01", 달.first().시); assertEquals("10월", 달.first().글)
        val 년 = 스탯그래프칸들("년", "2026-10-05")
        assertEquals(5, 년.size); assertEquals("2022", 년.first().글); assertEquals("2026-12-31", 년.last().끝)
    }

    @Test fun 그래프값_1RM합과_한번볼륨() {
        val d = 앱데이터(
            몸 = 몸조건(30, true, 80.0),
            기록 = mapOf(
                "2026-10-01" to 기(종("벤치프레스", 세(100.0, 1), 세(20.0, 10, 1)), 종("백 스쿼트", 세(100.0, 5))),
                "2026-10-03" to 기(종("데드리프트", 세(150.0, 1))),
                "2026-10-03~2" to 기(종("벤치프레스", 세(90.0, 1))),
            ),
        )
        val 칸 = d.스탯그래프값(스탯.근력, "일", "2026-10-05")
        val 첫 = 칸.first { it.시 == "2026-10-01" }
        // 1RM 합 = 벤치 100 + 스쿼트 100×(1+5/30)
        assertEquals(100.0 + 100.0 * (1 + 5 / 30.0), 첫.선!!, 1e-6)
        // 한 번 볼륨 — 워밍업은 뺀다: 100 + 500
        assertEquals(600.0, 첫.막!!, 1e-6)
        val 셋 = 칸.first { it.시 == "2026-10-03" }
        // 90일 창에 앞 기록도 든다: 벤치 100 + 스쿼트 116.7 + 데드 150
        assertEquals(100.0 + 100.0 * (1 + 5 / 30.0) + 150.0, 셋.선!!, 1e-6)
        // 같은 날 두 번 = 평균 (150 + 90) / 2
        assertEquals(120.0, 셋.막!!, 1e-6)
        assertNull(칸.first { it.시 == "2026-10-02" }.선)
        assertNull(칸.first { it.시 == "2026-10-02" }.막)
        // 미래 기록은 보지 않는다
        val 미래 = d.copy(기록 = d.기록 + ("2026-10-09" to 기(종("벤치프레스", 세(200.0, 1)))))
        assertEquals(칸, 미래.스탯그래프값(스탯.근력, "일", "2026-10-05"))
    }

    @Test fun 그래프값_점수스탯은_스탯기록() {
        val d = 앱데이터(스탯기록 = mapOf("2026-10-01" to mapOf("다양성" to 40.0), "2026-10-04" to mapOf("다양성" to 55.0, "성실" to 10.0)))
        val 주 = d.스탯그래프값(스탯.다양성, "주", "2026-10-05")
        assertEquals(55.0, 주[주.size - 2].선!!, 1e-9)   // 09-28 ~ 10-04 의 마지막
        assertNull(주.last().선)
    }

    @Test fun 그래프차_반올림_흔들림은_없음() {
        assertNull(스탯그래프차글(listOf(10.0)) { "$it" })
        assertNull(스탯그래프차글(listOf(10.0, 10.4)) { "$it" })
        assertEquals("▲ +5" to true, 스탯그래프차글(listOf(10.0, 15.0)) { "${it.toInt()}" })
        assertEquals("▼ −5" to false, 스탯그래프차글(listOf(15.0, 10.0)) { "${it.toInt()}" })
    }

    // ─────────────── 프로필 ───────────────

    @Test fun 닉네임_12자_줄바꿈없음() {
        assertEquals("가나다라마바사아자차카타", 닉네임자름("가나다라마바사아자차카타파하"))
        assertEquals("홍겸", 닉네임자름("홍\n겸"))
        assertEquals("", 닉네임자름(""))
    }

    @Test fun 큰운동값_오늘까지_최고와_합() {
        val d = 앱데이터(
            기록 = mapOf(
                "2026-10-01" to 기(종("백 스쿼트", 세(100.0, 5)), 종("벤치프레스", 세(80.0, 1), 세(200.0, 1, 1))),
                "2026-10-02" to 기(종("벤치 프레스", 세(80.0, 1)), 종("데드리프트", 세(0.0, 10))),
                "2026-10-09" to 기(종("데드리프트", 세(300.0, 1))),
            ),
        )
        val l = d.프로필큰운동값("2026-10-05")
        assertEquals(listOf("3대", "스쿼트", "벤치", "데드"), l.map { it.글 })
        assertEquals(116.7, l[1].v, 1e-9)
        assertEquals(80.0, l[2].v, 1e-9); assertEquals("2026-10-01", l[2].때)   // 같은 값이면 먼저 낸 날 · 워밍업 200 은 뺀다
        assertEquals(0.0, l[3].v, 1e-9); assertNull(l[3].때)                    // 무게 0 · 미래 기록 뺀다
        assertEquals(196.7, l[0].v, 1e-9); assertEquals("2026-10-01", l[0].때)
        // 추가는 표 순서로 2개까지 (옛 파일에 셋이 적혀 있어도)
        val 다섯 = d.copy(설정 = 설정값(큰운동추가 = listOf("클린 앤 저크", "오버헤드 프레스", "스내치"))).프로필큰운동값("2026-10-05")
        assertEquals(listOf("5대", "스쿼트", "벤치", "데드", "OHP", "스내치"), 다섯.map { it.글 })
    }

    @Test fun 큰운동_상세글() {
        assertEquals("—", 프로필큰값글(0.0, true))
        assertEquals("197kg", 프로필큰값글(196.7, true))
        assertEquals("117kg", 프로필큰값글(116.7, false))      // 100 넘으면 정수 (시안 차kg)
        assertEquals("82.5kg", 프로필큰값글(82.5, false))
        assertEquals("80kg", 프로필큰값글(80.0, false))
        assertEquals("2026.10.01.", 프로필큰날글("2026-10-01"))
        assertEquals("—", 프로필큰날글(null))
    }

    @Test fun 큰운동칩_앞셋은_늘켜짐_두개까지() {
        assertEquals(emptyList<String>(), 프로필큰칩바꿈(emptyList(), "스쿼트"))
        assertEquals(listOf("오버헤드 프레스"), 프로필큰칩바꿈(emptyList(), "오버헤드 프레스"))
        assertEquals(listOf("오버헤드 프레스", "스내치"), 프로필큰칩바꿈(listOf("스내치"), "오버헤드 프레스"))   // 표 순서
        assertNull(프로필큰칩바꿈(listOf("스내치", "바벨 로우"), "오버헤드 프레스"))
        assertEquals(listOf("스내치"), 프로필큰칩바꿈(listOf("스내치", "바벨 로우"), "바벨 로우"))
        // 연달아 같은 칩 — 켜고 끄기를 오간다
        var l = emptyList<String>()
        repeat(5) { l = 프로필큰칩바꿈(l, "스내치")!! }
        assertEquals(listOf("스내치"), l)
    }

    @Test fun 프로필업적줄_숨김과_정렬() {
        val a = 일반.take(4)
        var d = 앱데이터(업적 = mapOf(a[0] to 10L, a[1] to 30L, a[2] to 20L, "9-99" to 40L))
        assertEquals(listOf(a[1], a[2], a[0]), 프로필업적줄(d))           // 최신순 · 표에 없는 번호 뺌
        d = d.업적보임바꿈(a[2])
        assertEquals(listOf(a[1], a[0]), 프로필업적줄(d))
        d = d.업적보임바꿈(a[3])                                              // 얻지 않은 것은 못 숨긴다
        assertEquals(setOf(a[2]), d.업적숨김)
        d = d.copy(설정 = d.설정.copy(업적정렬 = "오래된순"))
        assertEquals(listOf(a[0], a[1]), 프로필업적줄(d))
    }

    @Test fun 업적끌기_끄는중_줄이_바뀌어도_같은것끼리() {
        val a = 일반.take(4)
        val d0 = 앱데이터(업적 = mapOf(a[0] to 10L, a[1] to 20L, a[2] to 30L))
        val 시작 = 프로필업적줄(d0)                                     // [a2, a1, a0]
        // 끄는 동안 새 업적이 생겨 맨 앞에 끼었다 — 그래도 a0 을 a2 앞으로
        val d1 = d0.copy(업적 = d0.업적 + (a[3] to 40L))
        val 뒤 = 업적끌어옮김(d1, 시작, 2, 0, false)
        assertEquals("직접", 뒤.설정.업적정렬)
        assertEquals(listOf(a[3], a[0], a[2], a[1]), 프로필업적줄(뒤))
        // 끄는 동안 끈 업적을 숨겼다 — 그대로
        val d2 = d0.업적보임바꿈(a[0])
        assertEquals(d2, 업적끌어옮김(d2, 시작, 2, 0, false))
        // 범위 밖 번호 — 그대로
        assertEquals(d0, 업적끌어옮김(d0, 시작, 5, 0, false))
        // 숨긴 업적은 제자리에 둔 채 보이는 것만 옮긴다
        val d3 = d0.업적보임바꿈(a[1])
        val 줄3 = 프로필업적줄(d3)                                      // [a2, a0]
        val 옮긴 = 업적끌어옮김(d3, 줄3, 1, 0, false)
        assertEquals(listOf(a[0], a[2]), 프로필업적줄(옮긴))
        assertEquals(setOf(a[1]), 옮긴.업적숨김)
        // 직접 정렬 뒤 정렬을 바꿨다 다시 '직접' — 순서 기억 (업적순서는 지우지 않는다)
        val 바꿈 = 옮긴.copy(설정 = 옮긴.설정.copy(업적정렬 = "가나다순")).copy(설정 = 옮긴.설정.copy(업적정렬 = "직접"))
        assertEquals(프로필업적줄(옮긴), 프로필업적줄(바꿈))
    }

    @Test fun 인증샷_8장_넘게_넣기() {
        val 이름들 = (1..11).map { "p$it.jpg" }
        val (d, n) = 인증여럿넣기(앱데이터(), 이름들, 1000L)
        assertEquals(인증최대, n)
        assertEquals(인증최대, d.인증샷.size)
        // 새것부터 — 마지막에 넣은 것이 맨 앞
        assertEquals("p8.jpg", 인증순(d.인증샷).first().파일)
        // 꽉 찬 데 또 — 하나도 안 들어간다
        val (d2, n2) = 인증여럿넣기(d, listOf("q.jpg"), 2000L)
        assertEquals(0, n2); assertEquals(d, d2)
        // 같은 파일 두 번 — 한 번만
        val (d3, n3) = 인증여럿넣기(앱데이터(), listOf("a.jpg", "a.jpg"), 0L)
        assertEquals(1, n3); assertEquals(1, d3.인증샷.size)
    }

    @Test fun 인증샷_지우고_되돌리기() {
        val x = 인증사진("a.jpg", 5L, 9L)
        val d = 앱데이터(인증샷 = listOf(x, 인증사진("b.jpg", 6L)))
        val 지운 = d.copy(인증샷 = d.인증샷.filter { it.파일 != "a.jpg" })
        val 살린 = 인증되살림(지운, x)!!
        assertEquals(인증순(d.인증샷), 인증순(살린.인증샷))                 // 고정도 그대로
        assertEquals(살린, 인증되살림(살린, x))                              // 두 번 눌러도 하나
        // 지운 사이 8장이 찼다 — 자리 없음
        val 꽉 = 지운.copy(인증샷 = (1..8).map { 인증사진("p$it", it.toLong()) })
        assertNull(인증되살림(꽉, x))
    }

    @Test fun 링크_다듬기와_보임글() {
        assertEquals(listOf("https://instagram.com/a", "http://x.y/"), 링크저장값(listOf(" instagram.com/a ", "", "javascript:alert(1)", "http://x.y/")))
        assertEquals("instagram.com/a", 링크보임글("https://instagram.com/a/"))
        assertEquals("x.y", 링크보임글("HTTP://x.y"))
    }

    @Test fun 닉네임줄_파란상자_왼쪽() {
        assertEquals((200f - 170f * 0.935f) / 2f, 닉왼(200f, 30f), 1e-4f)
        assertEquals(5f, 닉왼(10f, 30f), 1e-4f)    // 폭이 모자라도 음수가 아니다
        assertTrue(닉왼(0f, 30f) >= 0f)
    }

    // ─────────────── 검색 ───────────────

    @Test fun 검색판_4줄이_높이안에() {
        // 넓고 낮은 화면 — 높이에 맞춰 줄인다
        val w = 찾판폭(360.dp, 400.dp)
        val 칸 = (w - 4.dp) / 3
        assertTrue(칸 * 4f / 3f * 4 + 6.dp <= 400.dp + 0.5.dp)
        // 좁고 높은 화면 — 폭 그대로
        assertEquals(300.dp, 찾판폭(300.dp, 2000.dp))
        // 0 높이(자판이 다 덮음) — 폭 그대로 (음수 · 0 이 되지 않게)
        assertEquals(300.dp, 찾판폭(300.dp, 0.dp))
    }
}
