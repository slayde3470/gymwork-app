package com.slayde.hasenheide.ui

import com.slayde.hasenheide.data.날기록
import com.slayde.hasenheide.data.루틴
import com.slayde.hasenheide.data.루틴종목
import com.slayde.hasenheide.data.설정값
import com.slayde.hasenheide.data.세션종목
import com.slayde.hasenheide.data.세트
import com.slayde.hasenheide.data.세트종류
import com.slayde.hasenheide.data.앱데이터
import com.slayde.hasenheide.data.운동세션
import com.slayde.hasenheide.data.끝냄
import com.slayde.hasenheide.data.체크
import com.slayde.hasenheide.data.보고저장결과
import com.slayde.hasenheide.data.보고저장
import com.slayde.hasenheide.data.운동시작
import com.slayde.hasenheide.data.종목
import com.slayde.hasenheide.data.종목기록
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 운동 보고서 계산 (10-05 옮기기 2단계 R · ReportScreen.kt 의 internal fun) — 시안 v21 `결과뷰` · `큰운동값` 과 같은 값인지.
 * 뒤죽박죽 사용: 빈 기록 · 같은 날 여러 기록(~2 ~10) · 같은 이름 종목 · 같은 종목 여러 플랜 · 워밍업 · 맨몸.
 */
class ReportTest {

    private fun 기록(rid: String, vararg 종목: 종목기록, 초: Int = 1800) = 날기록(rid, "가슴", true, 종목.toList(), 초)
    private fun 줄(이름: String, vararg s: 세트, 종id: String? = null, 플랜id: String? = null) = 종목기록(이름, s.toList(), 종id = 종id, 플랜id = 플랜id)

    // ─────────────── 기록 차례 ───────────────

    @Test fun 기록순_같은날_숫자차례() {
        val l = listOf("2026-10-05~10", "2026-10-04", "2026-10-05~2", "2026-10-05").sortedWith { a, b -> 기록순(a, b) }
        assertEquals(listOf("2026-10-04", "2026-10-05", "2026-10-05~2", "2026-10-05~10"), l)
    }

    // ─────────────── 보고서 숫자 ───────────────

    @Test fun 빈기록_칸없음_죽지않음() {
        val d = 앱데이터()
        val v = d.보고계산(기록("r1"), null, "2026-10-05")
        assertEquals(0, v.세트); assertEquals(0.0, v.볼륨); assertTrue(v.칸들.isEmpty()); assertNull(v.루차)
        val 큰 = d.큰운동값(null, null, "2026-10-05")
        assertEquals(4, 큰.size); assertEquals("3대", 큰[0].글); assertTrue(큰.all { it.v == 0.0 && it.때 == null })
    }

    @Test fun 지난번대비_바로앞기록() {
        val d = 앱데이터(기록 = mapOf(
            "2026-10-01" to 기록("r1", 줄("벤치프레스", 세트(60.0, 10), 세트(60.0, 10))),
            "2026-10-03" to 기록("r1", 줄("벤치프레스", 세트(70.0, 5))),
        ))
        val 지금 = 기록("r1", 줄("벤치프레스", 세트(80.0, 5), 세트(80.0, 5)))
        val v = d.보고계산(지금, null, "2026-10-05")
        val k = v.칸들.single()
        // 지난번 = 10-03 (70×5 → 1RM 81.67 · 볼륨 350 을 1세트까지만)
        assertEquals(80 * (1 + 5 / 30.0) - 70 * (1 + 5 / 30.0), k.줄1.차!!, 1e-9)
        assertEquals(800.0 - 350.0, k.줄2.차!!, 1e-9)
        // 루틴 볼륨: 같은 루틴 지난번(10-03) 을 지금 세트 수(2)까지 잘라서 = 350
        assertEquals(800.0 - 350.0, v.루차!!, 1e-9)
        assertEquals(2, v.세트)
    }

    @Test fun 저장된보고서_자기와_뒤기록은_빼고() {
        val d = 앱데이터(기록 = mapOf(
            "2026-10-05" to 기록("r1", 줄("벤치프레스", 세트(60.0, 5))),
            "2026-10-05~2" to 기록("r1", 줄("벤치프레스", 세트(70.0, 5))),
            "2026-10-05~10" to 기록("r1", 줄("벤치프레스", 세트(90.0, 5))),
        ))
        val v = d.보고계산(d.기록["2026-10-05~2"]!!, "2026-10-05~2", "2026-10-05")
        // 견줄 것 = 10-05 (60) 만. ~10 (뒤 기록) 은 빼야 한다
        assertEquals((70.0 - 60.0) * (1 + 5 / 30.0), v.칸들.single().줄1.차!!, 1e-9)
        // 자기 자신만 있으면 견줄 것 없음
        val 첫 = d.보고계산(d.기록["2026-10-05"]!!, "2026-10-05", "2026-10-05")
        assertNull(첫.칸들.single().줄1.차); assertNull(첫.루차)
    }

    @Test fun 같은이름_다른종목은_섞지않음() {
        val 종목표 = listOf(종목("벤치프레스", "가슴", "바벨"), 종목("벤치프레스", "가슴", "덤벨", id = "종2"))
        val d = 앱데이터(종목표 = 종목표, 기록 = mapOf(
            "2026-10-01" to 기록("r1", 줄("벤치프레스", 세트(100.0, 1))),                  // 옛 줄(종id 없음) = 바벨
            "2026-10-02" to 기록("r1", 줄("벤치프레스", 세트(30.0, 1), 종id = "종2")),      // 덤벨
        ))
        val 바벨 = d.보고계산(기록("r1", 줄("벤치프레스", 세트(90.0, 1), 종id = "벤치프레스")), null, "2026-10-05").칸들.single()
        assertEquals(-10.0, 바벨.줄1.차!!, 1e-9)   // 덤벨(10-02) 을 건너뛰고 바벨(10-01) 과
        val 덤벨 = d.보고계산(기록("r1", 줄("벤치프레스", 세트(32.0, 1), 종id = "종2")), null, "2026-10-05").칸들.single()
        assertEquals(2.0, 덤벨.줄1.차!!, 1e-9)
    }

    @Test fun 같은종목_여러플랜은_플랜id로() {
        val d = 앱데이터(기록 = mapOf(
            "2026-10-01" to 기록("r1", 줄("스쿼트", 세트(100.0, 1), 플랜id = "p1")),
            "2026-10-02" to 기록("r1", 줄("스쿼트", 세트(60.0, 1), 플랜id = "p2")),
            "2026-10-03" to 기록("r1", 줄("스쿼트", 세트(50.0, 1))),
        ))
        val p1 = d.보고계산(기록("r1", 줄("스쿼트", 세트(105.0, 1), 플랜id = "p1")), null, "2026-10-05").칸들.single()
        assertEquals(5.0, p1.줄1.차!!, 1e-9)
        val 보통 = d.보고계산(기록("r1", 줄("스쿼트", 세트(55.0, 1))), null, "2026-10-05").칸들.single()
        assertEquals(5.0, 보통.줄1.차!!, 1e-9)
    }

    @Test fun 워밍업은_빼고_맨몸은_횟수로() {
        val d = 앱데이터(기록 = mapOf("2026-10-01" to 기록("r1", 줄("턱걸이", 세트(0.0, 8), 세트(0.0, 6)))))
        val 지금 = 기록("r1",
            줄("턱걸이", 세트(0.0, 10), 세트(0.0, 7)),
            줄("벤치프레스", 세트(40.0, 10, 세트종류.워밍업)),   // 워밍업만 → 칸 없음
        )
        val v = d.보고계산(지금, null, "2026-10-05")
        val k = v.칸들.single()
        assertTrue(k.맨몸)
        assertEquals("최고", k.줄1.이름); assertEquals("10회", k.줄1.값); assertEquals(2.0, k.줄1.차!!, 1e-9)
        assertEquals("합계", k.줄2.이름); assertEquals("17회", k.줄2.값); assertEquals(3.0, k.줄2.차!!, 1e-9)
        assertEquals(3, v.세트)   // 세트 수는 체크한 세트 전부 (워밍업 포함 — 전과 같게)
    }

    // ─────────────── 큰 운동 ───────────────

    @Test fun 큰운동_합계와_앞_때() {
        val d = 앱데이터(기록 = mapOf(
            "2026-10-01" to 기록("r1", 줄("백 스쿼트", 세트(100.0, 1)), 줄("데드리프트", 세트(120.0, 1))),
            "2026-10-03" to 기록("r1", 줄("벤치프레스", 세트(70.0, 1))),
        ), 설정 = 설정값(큰운동추가 = listOf("바벨 로우", "스내치", "오버헤드 프레스")))   // 표 순서 · 2개까지 → OHP · 로우
        val 지금 = 기록("r1", 줄("바벨 스쿼트", 세트(110.0, 1)), 줄("펜들레이 로우", 세트(60.0, 1)))
        val 큰 = d.큰운동값(지금, null, "2026-10-05")
        assertEquals(listOf("5대", "스쿼트", "벤치", "데드", "OHP", "로우"), 큰.map { it.글 })
        assertEquals(110.0, 큰[1].v); assertEquals(100.0, 큰[1].앞); assertEquals("2026-10-05", 큰[1].때)
        assertEquals(70.0, 큰[2].v); assertEquals("2026-10-03", 큰[2].때)
        assertEquals(110.0 + 70 + 120 + 0 + 60, 큰[0].v); assertEquals(100.0 + 70 + 120, 큰[0].앞)
        assertEquals("2026-10-05", 큰[0].때)
        // 프로필 탭(기록 없이) — 오늘까지 저장된 것만
        val 프 = d.큰운동값(null, null, "2026-10-05")
        assertEquals(100.0, 프[1].v); assertEquals(0.0, 프[5].v)
    }

    @Test fun 큰운동_같은값이면_먼저낸날() {
        val d = 앱데이터(기록 = mapOf(
            "2026-10-02" to 기록("r1", 줄("벤치프레스", 세트(80.0, 1))),
            "2026-10-01" to 기록("r1", 줄("벤치 프레스", 세트(80.0, 1))),
        ))
        assertEquals("2026-10-01", d.큰운동값(null, null, "2026-10-05")[2].때)
    }

    @Test fun 큰운동_저장된보고서는_그기록까지() {
        val d = 앱데이터(기록 = mapOf(
            "2026-10-01" to 기록("r1", 줄("벤치프레스", 세트(80.0, 1))),
            "2026-10-02" to 기록("r1", 줄("벤치프레스", 세트(90.0, 1))),
            "2026-10-03" to 기록("r1", 줄("벤치프레스", 세트(100.0, 1))),
        ))
        val 큰 = d.큰운동값(d.기록["2026-10-02"], "2026-10-02", "2026-10-02")
        assertEquals(90.0, 큰[2].v); assertEquals(80.0, 큰[2].앞)
    }

    // ─────────────── 상세 ───────────────

    @Test fun 상세_세트글과_대비() {
        val d = 앱데이터(기록 = mapOf(
            "2026-09-20" to 기록("r1", 줄("벤치프레스", 세트(100.0, 1))),   // 1주 밖
            "2026-10-01" to 기록("r1", 줄("벤치프레스", 세트(60.0, 5))),
        ))
        val k = d.보고계산(기록("r1", 줄("벤치프레스", 세트(60.0, 9), 세트(62.5, 8), 세트(40.0, 10, 세트종류.워밍업))), null, "2026-10-05").칸들.single()
        val 상 = d.상세계산(k, null, "2026-10-05")
        assertEquals(listOf("1세트 · 60kg × 9회", "2세트 · 62.5kg × 8회"), 상.세트글)
        assertEquals("최고 세트 · 62.5kg × 8회", 상.최고글)
        val rm = assertNotNull(상.rm)
        assertEquals(60 * (1 + 5 / 30.0), rm.주!!.과거, 1e-9)
        assertEquals(100.0, rm.최고!!.과거, 1e-9)
    }

    @Test fun 상세_맨몸_세트글() {
        val k = 앱데이터().보고계산(기록("r1", 줄("턱걸이", 세트(0.0, 6), 세트(0.0, 9))), null, "2026-10-05").칸들.single()
        val 상 = 앱데이터().상세계산(k, null, "2026-10-05")
        assertEquals(listOf("1세트 · 6회", "2세트 · 9회"), 상.세트글)
        assertEquals("최고 세트 · 9회", 상.최고글)
        assertNull(상.rm); assertNull(상.볼륨)
    }

    // ─────────────── 세션 → 기록 · 결과 열쇠 ───────────────

    @Test fun 세션기록_빈종목도_차례그대로() {
        val r = 루틴("r1", "가슴", 종목 = listOf(루틴종목("벤치프레스", 세트 = 3), 루틴종목("플라이", 세트 = 2)))
        var S: 운동세션 = 운동시작(r, 1_000L)!!
        S = S.copy(종목들 = S.종목들.mapIndexed { i, e: 세션종목 -> if (i == 1) e.copy(기록 = listOf(세트(10.0, 12), null)) else e }, 끝시각 = 61_000L)
        val rec = 세션기록(S, 99_000L)
        assertEquals(2, rec.종목들.size)
        assertTrue(rec.종목들[0].세트들.isEmpty())
        assertEquals(60, rec.걸린초)
        val v = 앱데이터().보고계산(rec, null, "2026-10-05", 세션총칸(S))
        assertEquals(1, v.칸들.single().번호)   // 펼침 열쇠 = 세션 차례
        assertEquals(2, v.칸들.single().총)
    }

    @Test fun 저장열쇠_같은끝시각_뒤의것() {
        val d = 앱데이터(기록 = mapOf(
            "2026-10-05" to 날기록("r1", "가슴", true, emptyList(), 끝시각 = 5L),
            "2026-10-05~2" to 날기록("r1", "가슴", true, emptyList(), 끝시각 = 9L),
            "2026-10-05~10" to 날기록("r2", "등", true, emptyList(), 끝시각 = 9L),
        ))
        val S = 운동세션("r1", "가슴", 0L, 끝시각 = 9L)
        assertEquals("2026-10-05~2", 저장열쇠(d, S))
        assertNull(저장열쇠(d, S.copy(루틴id = "r9")))
    }

    // ─────────────── 글 ───────────────

    @Test fun 글꼴들() {
        assertEquals("운동보고서_2026-10-05.png", 보고파일이름("2026-10-05~2"))
        assertEquals("2026.10.05.", 보고날글("2026-10-05"))
        assertEquals("—", 큰때글(null))
        assertEquals("362kg", 큰값글(큰칸("3대", 361.6, 0.0, null), 합 = true))
        assertEquals("78.3kg", 큰값글(큰칸("벤치", 78.3, 0.0, null), 합 = false))
        assertEquals("—", 큰값글(큰칸("벤치", 0.0, 0.0, null), 합 = false))
        assertEquals("▲3.7kg", 보고차글(3.66, "kg"))
        assertEquals("▼2회", 보고차글(-2.0, "회"))
        assertEquals("▲2,350kg", 보고차글(2350.0, "kg"))
        assertNull(보고차글(0.04, "kg")); assertNull(보고차글(null, "kg"))
    }

    // ─────────────── 10-06 v22 D — 보고서가 열릴 때 저장한 뒤에도 숫자는 자기 기록을 빼고 견준다 ───────────────

    /** 10-10: 기록은 운동을 시작한 날로 저장된다 → 시험 세션도 2026-10-06 한낮에 시작 */
    private val 틱: Long = java.time.LocalDate.of(2026, 10, 6).atTime(12, 0).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

    @Test fun 열릴때저장_자기기록빼고_견줌() {
        val r = 루틴("r1", "가슴", 종목 = listOf(루틴종목("벤치프레스", 세트 = 2, 무게 = 80.0, 횟수 = 5)))
        val d0 = 앱데이터(루틴들 = listOf(r), 기록 = mapOf("2026-10-03" to 기록("r1", 줄("벤치프레스", 세트(70.0, 5)))))
        var S: 운동세션 = 운동시작(r, (틱 + 1_000L))!!
        S = S.체크(0, 0, (틱 + 2_000L)).체크(0, 1, (틱 + 3_000L)).끝냄((틱 + 4_000L))
        val 전 = d0.보고계산(세션기록(S, (틱 + 4_000L)), null, "2026-10-06", 세션총칸(S))
        val (d1, 결과) = d0.copy(세션 = S).보고저장("2026-10-06", (틱 + 4_000L))
        assertEquals(보고저장결과.저장함, 결과)
        val 열쇠 = d1.세션!!.저장!!.열쇠
        val 후 = d1.보고계산(세션기록(d1.세션!!, (틱 + 4_000L)), 열쇠, "2026-10-06", 세션총칸(d1.세션!!))
        assertEquals(전.칸들.single().줄1.차!!, 후.칸들.single().줄1.차!!, 1e-9)   // 방금 저장한 자기와 견주면 0 이 된다
        assertEquals(전.루차!!, 후.루차!!, 1e-9)
        assertEquals(전.볼륨, 후.볼륨, 1e-9)
        // 큰 운동 — 앞 = 자기 기록 앞까지
        val 큰 = d1.큰운동값(d1.기록[열쇠], 열쇠, "2026-10-06")
        assertTrue(큰[2].v > 큰[2].앞)
    }
}
