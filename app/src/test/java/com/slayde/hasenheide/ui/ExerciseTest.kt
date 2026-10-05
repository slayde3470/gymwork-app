package com.slayde.hasenheide.ui

import com.slayde.hasenheide.data.루틴
import com.slayde.hasenheide.data.루틴종목
import com.slayde.hasenheide.data.앱데이터
import com.slayde.hasenheide.data.종목
import com.slayde.hasenheide.data.종목기본세트
import com.slayde.hasenheide.data.종목사전
import com.slayde.hasenheide.data.종목세트
import com.slayde.hasenheide.data.종목세트최대
import com.slayde.hasenheide.data.플랜
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 앱 옮기기 2단계 EX — 종목 탭 · 새 종목 시트의 순수 계산 (ExerciseScreen.kt · NewExerciseSheet.kt 의 internal fun).
 * 뒤죽박죽 사용: 같은 이름 종목 둘 · 플랜이 걸린 종목 이름 바꾸기 · 빈 이름 저장 · 빠르게 연달아 누르기 · 칸 없이 저장
 */
class ExerciseTest {

    private fun 바탕(): 앱데이터 = 앱데이터(
        종목표 = listOf(종목("벤치프레스", "가슴", "바벨"), 종목("턱걸이", "등", "맨몸")),
        루틴들 = listOf(루틴("r1", "가슴날", 종목 = listOf(루틴종목("벤치프레스"), 루틴종목("턱걸이")))),
    )

    // ─────────────── 격자 (CSS grid 2열 · dense) ───────────────

    @Test fun 격자_접힌것은두개씩() {
        assertEquals(listOf(listOf(0, 1), listOf(2, 3), listOf(4)), 격자줄(listOf(false, false, false, false, false)))
    }

    @Test fun 격자_펼친것은한줄_빈칸은뒤가채움() {
        // 0 접힘 · 1 펼침 · 2 접힘 → [0, 2] 가 첫 줄(빈 칸을 2가 채움) · [1] 둘째 줄
        assertEquals(listOf(listOf(0, 2), listOf(1)), 격자줄(listOf(false, true, false)))
        assertEquals(listOf(listOf(0), listOf(1, 2)), 격자줄(listOf(true, false, false)))
        assertEquals(listOf(listOf(0, 1), listOf(2)), 격자줄(listOf(false, false, true)))
        assertEquals(emptyList(), 격자줄(emptyList()))
    }

    // ─────────────── 종목 탭 판 ───────────────

    @Test fun 판_플랜만남은종목은기타() {
        val d = 바탕().copy(플랜들 = listOf(플랜("p1", "스쿼트 플랜", "백 스쿼트", 100.0)))
        val 판 = d.종목탭판("전체")
        assertEquals(listOf("가슴", "등", "하체", "어깨", "팔", "복근", "기타"), 판.칸들)
        assertEquals(listOf("가슴", "등", "기타"), 판.묶음.map { it.first })
        val 기타 = 판.묶음.last().second.single()
        assertNull(기타.종목)
        assertEquals("기타:백 스쿼트", 기타.열쇠)
        assertEquals(1, d.상자플랜(기타).size)
        // 없는 칸을 골라 두었으면 전체로
        assertEquals("전체", d.종목탭판("없는칸").고름)
        assertEquals(listOf("등"), d.종목탭판("등").묶음.map { it.first })
    }

    @Test fun 판_같은이름둘_플랜은첫종목에만() {
        val d0 = 바탕().copy(플랜들 = listOf(플랜("p1", "벤치 플랜", "벤치프레스", 80.0)))
        val (d, 둘째) = d0.새종목저장(새종목값(이름 = "벤치프레스", 칸 = "가슴", 근육 = mapOf("chest_mid" to "P")), 1000L)
        val 가슴 = d.종목탭판("전체").묶음.first { it.first == "가슴" }.second
        assertEquals(2, 가슴.size)
        assertNotEquals(가슴[0].열쇠, 가슴[1].열쇠)
        assertEquals(1, d.상자플랜(가슴[0]).size)
        assertEquals(0, d.상자플랜(가슴[1]).size)
        assertEquals(둘째.id, 가슴[1].열쇠)
    }

    // ─────────────── 세트 줄 ───────────────

    @Test fun 세트_값바꿈_끝을넘지않음() {
        val l = listOf(종목세트(0.0, 1, 60), 종목세트(20.0, 10, 90))
        assertEquals(0.0, 세트값바꿈(l, 0, 'w', -1, 2.5)[0].w)          // 0 아래로 안 감
        assertEquals(1, 세트값바꿈(l, 0, 'r', -1, 2.5)[0].r)            // 1 아래로 안 감
        assertEquals(22.5, 세트값바꿈(l, 1, 'w', 1, 2.5)[1].w)
        assertEquals(20.1, 세트값바꿈(l, 1, 'w', 1, 0.1)[1].w)          // 0.1 단위 흔들림 없음
        assertEquals(l, 세트값바꿈(l, 5, 'w', 1, 2.5))                  // 없는 줄은 그대로
    }

    @Test fun 세트_빠르게연달아_새값에서계산() {
        // 화면은 '지금 목록 → 새 목록' 을 넘긴다 → 연달아 눌러도 앞의 결과 위에 쌓인다
        var l = listOf(종목세트(20.0, 10, 60))
        repeat(5) { l = 세트값바꿈(l, 0, 'w', 1, 2.5) }
        assertEquals(32.5, l[0].w)
        repeat(20) { l = 세트더함(l) }
        assertEquals(종목세트최대, l.size)
        repeat(20) { l = 세트지움(l, 0) }
        assertEquals(1, l.size)                                           // 마지막 한 줄은 남는다
    }

    @Test fun 세트_글넣음_이상한값은무시() {
        val l = listOf(종목세트(20.0, 10, 60))
        assertEquals(l, 세트글넣음(l, 0, 'w', ""))
        assertEquals(l, 세트글넣음(l, 0, 'w', "."))
        assertEquals(l, 세트글넣음(l, 0, 'w', "abc"))
        assertEquals(42.5, 세트글넣음(l, 0, 'w', "42,5")[0].w)
        assertEquals(0.0, 세트글넣음(l, 0, 'w', "-5")[0].w)
        assertEquals(1, 세트글넣음(l, 0, 'r', "0")[0].r)
        assertEquals(8, 세트글넣음(l, 0, 'r', "8")[0].r)
    }

    @Test fun 세트_종목설정에저장() {
        val d = 바탕()
        val 기본 = d.종목기본세트("벤치프레스")
        val d2 = d.종목세트고침("벤치프레스") { 세트더함(it) }
        assertEquals(기본.size + 1, d2.종목기본세트("벤치프레스").size)
        assertEquals(기본, d2.종목기본세트("턱걸이"))   // 다른 종목은 그대로
    }

    // ─────────────── 새 종목 시트 ───────────────

    @Test fun 새_사전에서고르면칸근육채움_맨몸은칸비움() {
        val d = 바탕()
        val v = d.새초기().사전고름(d, "인클라인 벤치프레스")
        assertEquals("가슴", v.칸)
        assertTrue(v.고름)
        assertTrue("P" in v.근육.values)
        assertTrue(v.근육.values.all { it == "P" || it == "Y" })   // 보조 S 는 Y 로
        // 사전 칸 '맨몸' 은 앱 카테고리에 없다 → 비움 → 저장하려면 골라야
        val 크런치 = d.새초기().사전고름(d, "크런치")
        assertNull(크런치.칸)
        assertEquals("반드시 카테고리를 지정해야 합니다", d.저장검사(크런치))
        assertNull(d.저장검사(크런치.칸고름("복근")))
    }

    @Test fun 새_손으로고른칸은사전이비우지않음() {
        val d = 바탕()
        val v = d.새초기().칸고름("복근").copy(이름 = "크런치", 찾는중 = true).확인(d).first
        assertEquals("복근", v.칸)
    }

    @Test fun 새_빈이름_주동근없음() {
        val d = 바탕()
        assertEquals("이름을 넣어 주세요", d.새초기().copy(이름 = "   ").확인(d).second)
        assertEquals("이름을 넣어 주세요", d.저장검사(새종목값(이름 = " ", 칸 = "가슴", 근육 = mapOf("abs" to "P"))))
        assertEquals("주동근을 하나 이상 골라 주세요", d.저장검사(새종목값(이름 = "무엇", 칸 = "가슴", 근육 = mapOf("abs" to "Y"))))
    }

    @Test fun 새_사전에없는이름은직접종목() {
        val d = 바탕()
        val (v, 오류) = d.새초기().copy(이름 = "나만의 운동", 찾는중 = true).확인(d)
        assertNull(오류)
        assertTrue(v.고름)
        assertNull(v.칸)
        assertNull(v.사전)
    }

    @Test fun 새_같은이름도저장_번호다른id() {
        val d = 바탕()
        val v = 새종목값(이름 = "벤치프레스", 칸 = "가슴", 근육 = mapOf("chest_mid" to "P"), 세트 = listOf(종목세트(50.0, 5, 120)))
        val (d2, t) = d.새종목저장(v, 1000L)
        assertEquals(3, d2.종목표.size)
        assertNotEquals("벤치프레스", t.id)
        assertEquals(listOf(종목세트(50.0, 5, 120)), d2.종목설정[t.id])
        // 같은 시각에 또 만들어도 id 가 겹치지 않는다 (빠르게 연달아)
        val (d3, t2) = d2.새종목저장(v, 1000L)
        assertNotEquals(t.id, t2.id)
        assertEquals(d3.종목표.map { it.id }.distinct().size, d3.종목표.size)
    }

    @Test fun 새_근육누름_역할토글_막힘() {
        var v = 새종목값()
        v = v.근육누름("chest_mid").first
        assertEquals("P", v.근육["chest_mid"])
        v = v.근육누름("chest_mid").first
        assertNull(v.근육["chest_mid"])                        // 같은 역할 다시 = 뺀다
        v = v.근육누름("lats").first
        assertEquals("등", v.묶음)                              // 그 부위의 묶음으로
        val y = v.copy(역할 = "Y")
        val (그대로, 말) = y.근육누름("lats")
        assertEquals("이미 주동근으로 선택되어있습니다.", 말)
        assertEquals("P", 그대로.근육["lats"])
        assertEquals("Y", y.근육누름("biceps").first.근육["biceps"])
        // 주동 역할에서 협응 부위를 누르면 주동으로
        assertEquals("P", y.근육누름("biceps").first.copy(역할 = "P").근육누름("biceps").first.근육["biceps"])
    }

    @Test fun 새_근육두줄() {
        assertEquals("—" to "—", 근육두줄(emptyMap()))
        val (주, 협) = 근육두줄(mapOf("lats" to "P", "biceps" to "Y"))
        assertEquals("광배근", 주)
        assertTrue(협.isNotEmpty() && 협 != "—")
    }

    @Test fun 새_몸단계_주동이진하게() {
        val m = 새몸단계(mapOf("lats" to "P", "biceps" to "Y"))
        assertTrue(m.values.contains(20.0))
        assertTrue(m.values.contains(5.0))
    }

    @Test fun 사전_초성검색() {
        assertEquals("벤치프레스", 종목사전.찾기("ㅂㅊㅍ").first().이름)
    }

    // ─────────────── 편집 ───────────────

    @Test fun 편집_플랜걸린종목은이름못바꿈() {
        val d = 바탕().copy(플랜들 = listOf(플랜("p1", "벤치 플랜", "벤치프레스", 80.0)))
        val t = d.종목표[0]
        val v = d.편집초기(t).copy(이름 = "벤치")
        val r = d.종목고침(v)
        assertEquals("플랜이 있는 종목은 이름을 바꿀 수 없습니다", r.오류)
        assertNull(r.d)
        // 이름 그대로면 다른 것은 고친다
        val r2 = d.종목고침(d.편집초기(t).copy(근육 = mapOf("chest_upper" to "P")))
        assertNull(r2.오류)
        assertEquals(mapOf("chest_upper" to "P"), r2.d!!.종목표[0].근육)
    }

    @Test fun 편집_옛종목이름바꾸면새id_줄도따라감() {
        val d = 바탕().종목세트고침("벤치프레스") { listOf(종목세트(70.0, 5, 180)) }
        val t = d.종목표[0]
        val r = d.종목고침(d.편집초기(t).copy(이름 = "플랫 벤치"), 2000L)
        val nd = r.d!!
        val 새 = r.종목!!
        assertNotEquals("벤치프레스", 새.id)
        assertEquals("플랫 벤치", 새.이름)
        assertNull(nd.종목설정["벤치프레스"])
        assertEquals(listOf(종목세트(70.0, 5, 180)), nd.종목설정[새.id])
        assertEquals("플랫 벤치", nd.루틴들[0].종목[0].이름)
        assertEquals(새.id, nd.루틴들[0].종목[0].종id)
        assertEquals("턱걸이", nd.루틴들[0].종목[1].이름)   // 다른 줄은 그대로
    }

    @Test fun 편집_같은이름둘째를바꾸면첫째줄은그대로() {
        val (d, 둘째) = 바탕().새종목저장(새종목값(이름 = "벤치프레스", 칸 = "가슴", 근육 = mapOf("chest_mid" to "P")), 1000L)
        val r = d.종목고침(d.편집초기(둘째).copy(이름 = "덤벨 벤치"))
        val nd = r.d!!
        assertEquals("벤치프레스", nd.종목표[0].이름)
        assertEquals("덤벨 벤치", nd.종목표[2].이름)
        assertEquals(둘째.id, nd.종목표[2].id)                 // id 있는 종목은 id 그대로
        assertEquals("벤치프레스", nd.루틴들[0].종목[0].이름)   // 종id 없는 옛 줄 = 첫째 것
    }

    @Test fun 편집_없어진종목() {
        val d = 바탕()
        val r = d.종목고침(새종목값(이름 = "x", 칸 = "가슴", 편집 = "없음"))
        assertNull(r.d); assertNull(r.오류)
    }

    @Test fun 편집_빈이름() {
        val d = 바탕()
        assertEquals("이름을 넣어 주세요", d.편집초기(d.종목표[0]).copy(이름 = " ").확인(d).second)
    }

    @Test fun 편집초기_값채움() {
        val d = 바탕().copy(종목표 = listOf(종목("벤치프레스", "가슴", 목표1RM = 100.0, 달력이름 = "벤치", 근육 = mapOf("chest_mid" to "P", "triceps" to "S"))))
        val v = d.편집초기(d.종목표[0])
        assertEquals("가슴", v.칸)
        assertEquals("100", v.목표글)
        assertEquals("벤치", v.달력글)
        assertEquals("Y", v.근육["triceps"])   // 옛 S → Y
        assertEquals("벤치프레스", v.편집)
    }

    // ─────────────── 지우기 · 사진 ───────────────

    @Test fun 지우기_루틴줄도뺌_같은이름남으면옛줄은둠() {
        val d = 바탕()
        val nd = d.종목지우기("벤치프레스")
        assertEquals(listOf("턱걸이"), nd.루틴들[0].종목.map { it.이름 })
        assertEquals(listOf("가슴날"), d.종목쓰는루틴("벤치프레스"))
        val (d2, 둘째) = d.새종목저장(새종목값(이름 = "벤치프레스", 칸 = "가슴", 근육 = mapOf("chest_mid" to "P")), 1000L)
        val nd2 = d2.종목지우기("벤치프레스")
        assertEquals(2, nd2.루틴들[0].종목.size)              // 옛 줄은 남은 같은 이름이 이어받는다
        assertEquals(둘째.id, nd2.종목표.first { it.이름 == "벤치프레스" }.id)
    }

    @Test fun 사진_그종목하나만() {
        val (d, 둘째) = 바탕().새종목저장(새종목값(이름 = "벤치프레스", 칸 = "가슴", 근육 = mapOf("chest_mid" to "P")), 1000L)
        val nd = d.종목사진더함(둘째.id, listOf("a.jpg", "a.jpg", "b.jpg"))
        assertEquals(listOf("a.jpg", "b.jpg"), nd.종목표.first { it.id == 둘째.id }.사진)
        assertTrue(nd.종목표[0].사진.isEmpty())
        assertEquals(listOf("b.jpg"), nd.종목사진뺌(둘째.id, "a.jpg").종목표.first { it.id == 둘째.id }.사진)
        val 많이 = nd.종목사진더함(둘째.id, (1..20).map { "$it.jpg" })
        assertEquals(10, 많이.종목표.first { it.id == 둘째.id }.사진.size)
    }

    @Test fun 플랜걸림() {
        val (d, 둘째) = 바탕().copy(플랜들 = listOf(플랜("p1", "벤치 플랜", "벤치프레스", 80.0)))
            .새종목저장(새종목값(이름 = "벤치프레스", 칸 = "가슴", 근육 = mapOf("chest_mid" to "P")), 1000L)
        assertTrue(d.플랜걸림(d.종목표[0]))
        assertFalse(d.플랜걸림(둘째))
        assertNotNull(d.종목고침(d.편집초기(둘째).copy(이름 = "다른 벤치")).d)   // 둘째는 바꿀 수 있다
    }
}
