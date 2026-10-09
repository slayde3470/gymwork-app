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
        assertNull(d.저장검사(크런치.칸고름("복근")))   // 크런치는 사전 근육(복근 주동)이 있다
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
        // 10-06 v22 ⑩: 주동근이 없으면 저장을 막는다 (협응근만 · 비었음) — 이름 · 칸 다음 차례
        assertEquals("근육 사진을 눌러서 목표 근육을 설정하세요", 근육설정안내)
        assertEquals(근육설정안내, d.저장검사(새종목값(이름 = "무엇", 칸 = "가슴", 근육 = mapOf("abs" to "Y"))))
        assertEquals(근육설정안내, d.저장검사(새종목값(이름 = "무엇", 칸 = "가슴")))
        assertEquals("반드시 카테고리를 지정해야 합니다", d.저장검사(새종목값(이름 = "무엇")))
        assertNull(d.저장검사(새종목값(이름 = "무엇", 칸 = "가슴", 근육 = mapOf("abs" to "P"))))
        // 편집도 같다
        assertEquals(근육설정안내, d.저장검사(d.편집초기(d.종목표[0]).copy(근육 = emptyMap())))
    }

    @Test fun 새_사전에없는이름은직접종목() {
        val d = 바탕()
        val (v, 오류) = d.새초기().copy(이름 = "나만의 운동", 찾는중 = true).확인(d)
        assertNull(오류)
        assertTrue(v.고름)
        assertNull(v.칸)
        assertNull(v.사전)
        assertTrue(v.근육.isEmpty())   // 10-06 ⑪: 새 이름은 아무것도 칠하지 않는다 (낱말 짐작 안 함)
        // '컬' 낱말이 있어도 새 이름이면 비움 · 사전 종목(내장)은 미리 칠해진다
        assertTrue(d.새초기().copy(이름 = "나만의 컬", 찾는중 = true).확인(d).first.근육.isEmpty())
        assertTrue("P" in d.새초기().copy(이름 = "벤치프레스", 찾는중 = true).확인(d).first.근육.values)
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

    // ─────────────── v22 ⑧ 근육 그림 팝업 ───────────────

    @Test fun 팝_누르면칠하지않고열림_묶음으로() {
        val v = 새종목값(근육 = mapOf("chest_mid" to "P"))
        val 열린 = v.팝열기(listOf("lats"))
        assertEquals("lats", 열린.팝)
        assertEquals("등", 열린.묶음)
        assertEquals(v.근육, 열린.근육)                          // 칠하지 않는다
        // 대신 그리는 조각(가슴 아랫부분 = chest_lower + chest_mid) — 첫째(조각의 근육)가 누른 줄
        assertEquals("chest_lower", v.팝열기(listOf("chest_lower", "chest_mid")).팝)
        assertEquals(v, v.팝열기(emptyList()))                    // 근육 아닌 곳 = 그대로
        assertEquals(v, v.팝열기(listOf("없는부위")))
        assertNull(열린.팝닫기().팝)
    }

    @Test fun 팝_빈곳을눌러도열림_강조없이_묶음은그대로() {
        val v = 새종목값(칸 = "가슴", 묶음 = "가슴")
        val 열린 = v.팝빈열기()
        assertEquals(팝없음, 열린.팝)                  // 팝업은 뜨지만 누른 부위 강조는 없다
        assertEquals("가슴", 열린.묶음)                // 목록은 지금 묶음 그대로
        assertEquals("가슴", 열린.칸)                  // 카테고리도 그대로 (지금 칸이 선택된 채)
        assertNull(열린.팝닫기().팝)
        assertNull(새종목값().팝빈열기().칸)             // 새 종목 = 카테고리 선택 없음
    }

    @Test fun 팝_역할고르기_다른역할은흐리게만_눌러서옮김() {
        var v = 새종목값().팝열기(listOf("chest_mid"))
        v = v.팝역할("chest_mid", "P")
        assertEquals(mapOf("chest_mid" to "P"), v.근육)
        assertEquals("chest_mid", v.팝)                          // 고른 뒤에도 팝업은 그대로
        // 같은 묶음의 다른 줄도 바로
        v = v.팝역할("chest_upper", "Y")
        assertEquals("Y", v.근육["chest_upper"])
        // 10-09: 다른 역할이 이미 있는 부위의 칩은 흐리게만 (주동근 부위의 [협응근] · 협응근 부위의 [주동근])
        assertTrue(팝흐림(v.근육, "chest_mid", "Y"))
        assertTrue(팝흐림(v.근육, "chest_upper", "P"))
        assertFalse(팝흐림(v.근육, "chest_mid", "P"))
        assertFalse(팝흐림(v.근육, "chest_upper", "Y"))
        assertFalse(팝흐림(v.근육, "chest_mid", 팝빼기))
        assertFalse(팝흐림(v.근육, "chest_lower", "P"))            // 아무 역할도 없는 부위
        assertFalse(팝흐림(v.근육, "chest_lower", "Y"))
        // 흐려도 눌린다 — 한 부위는 한 역할만이라 옮겨 간다
        assertEquals("Y", v.팝역할("chest_mid", "Y").근육["chest_mid"])
        assertEquals("P", v.팝역할("chest_upper", "P").근육["chest_upper"])
        assertEquals(2, v.팝역할("chest_mid", "Y").근육.size)         // 부위 수는 그대로 (한 부위가 두 역할로 늘지 않는다)
        // [빼기] = 지움
        v = v.팝역할("chest_mid", 팝빼기)
        assertNull(v.근육["chest_mid"])
        assertEquals(v, v.팝역할("없는부위", "P"))                 // 세부 부위가 아니면 그대로
        assertEquals(listOf("P", "Y", 팝빼기), 팝역할들.map { it.first })
    }

    // ─────────────── v22 ⑨ 이름이 바뀌면 근육을 비운다 · 정할 때 사전 → 저장 종목 → 빈 채로 ───────────────

    @Test fun 이름_한글자라도바뀌면비움_편집은그대로() {
        val d = 바탕()
        val 정함 = d.새초기().copy(이름 = "벤치프레스", 찾는중 = true).확인(d).first.팝열기(listOf("chest_mid"))
        assertTrue("P" in 정함.근육.values)
        val 고침 = 정함.칸고름("가슴").이름바꿈("벤치프레")
        assertTrue(고침.근육.isEmpty())
        assertNull(고침.사전); assertNull(고침.정한); assertNull(고침.팝)
        assertEquals("가슴", 고침.칸)                               // 카테고리는 그대로
        assertTrue(고침.고름)                                       // 칸 · 그림은 그대로 보인다(비어서)
        // 같은 글(찾기만 켬)이면 비우지 않는다
        assertEquals(정함.근육, 정함.이름바꿈("벤치프레스").근육)
        // 편집은 이름만 바뀐다
        val 편 = d.편집초기(d.종목표[0])
        assertEquals(편.근육, 편.이름바꿈("벤치").근육)
    }

    @Test fun 이름_정할때_사전_저장종목_빈채로() {
        val 내것 = 종목("나만의 운동", "등", 근육 = mapOf("lats" to "P"))
        val d = 바탕().copy(종목표 = 바탕().종목표 + 내것)
        fun 정함(v: 새종목값, n: String) = v.이름바꿈(n).확인(d).first
        // 사전 이름(별칭 포함) → 사전 값
        var v = 정함(d.새초기(), "벤치프레스")
        assertEquals("벤치프레스", v.사전); assertEquals("벤치프레스", v.정한)
        assertTrue("P" in v.근육.values)
        // 한 번 정한 뒤 다른 이름으로 → 다시 규칙대로 (전: if(!고름) 때문에 앞 종목 근육이 남았다)
        v = 정함(v, "레그프레스")                                     // 사전에 없음 · 저장 종목 없음
        assertTrue(v.근육.isEmpty())
        assertNull(v.사전)
        v = 정함(v, "푸시업")                                        // 별칭 → 팔굽혀펴기
        assertEquals("팔굽혀펴기", v.이름)
        assertEquals("가슴", v.칸)                                   // v22 ⑪
        assertEquals("P", v.근육["chest_mid"])
        v = 정함(v, "나만의 운동")                                   // 저장된 같은 이름 종목 → 그 근육
        assertEquals(mapOf("lats" to "P"), v.근육)
        assertEquals("등", v.칸)
        v = 정함(v, "다리를들어요")                                    // 낱말 짐작 안 함
        assertTrue(v.근육.isEmpty())
        // 이름 그대로 [확인] (칸을 눌러 찾기만 켰다) → 고친 근육 그대로
        val 고친 = 정함(d.새초기(), "벤치프레스").팝역할("lats", "P")
        val 다시 = 고친.copy(찾는중 = true).확인(d).first
        assertEquals(고친.근육, 다시.근육)
        // 찾은 줄을 눌러도 같은 규칙
        assertEquals(mapOf("lats" to "P"), 고친.이름바꿈("나만").사전고름(d, "나만의 운동").근육)
    }

    @Test fun 이름_새시트는빈값() {
        val d = 바탕()
        val v = d.새초기()
        assertTrue(v.근육.isEmpty()); assertNull(v.팝); assertNull(v.정한); assertFalse(v.고름)
        // 넣기 시트에서 찾던 글로 열어도 [확인] 전엔 빈 채
        assertTrue(d.새초기("벤치").근육.isEmpty())
    }

    // ─────────────── v22 ⑪ 팔굽혀펴기 = 가슴 ───────────────

    @Test fun 팔굽혀펴기는가슴() {
        val d = 바탕()
        assertEquals("가슴", 종목사전.이름으로("팔굽혀펴기")?.칸)
        assertEquals("가슴", d.새초기().사전고름(d, "팔굽혀펴기").칸)
        assertEquals("가슴", d.새초기().copy(이름 = "푸쉬업", 찾는중 = true).확인(d).first.칸)
        assertEquals("맨몸", 종목사전.이름으로("맨몸 스쿼트")?.칸)      // 다른 맨몸 종목은 그대로
        assertNull(d.새초기().사전고름(d, "맨몸 스쿼트").칸)
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
