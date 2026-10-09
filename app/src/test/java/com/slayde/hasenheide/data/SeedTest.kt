package com.slayde.hasenheide.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * 10-09 홍겸 님 — 사전 종목 채우기 · 앱 카테고리 · 장비 · 비슷한 글자 검색.
 *  · 엔진(사전) 종목은 전부 종목 목록에 · 카테고리 지정 (맨몸 → 앱 카테고리)
 *  · 기본 세팅 1세트 · 10회 · 휴식 1분 · 머신 10 · 바벨 20 · 덤벨 2 (맨몸 0) — 10-09 홍겸 님 (전: 5세트 · 덤벨 5)
 *  · '팩' → 펙 덱 플라이 · '래터럴' → 레터럴
 */
class SeedTest {

    private val 이름들 = 종목사전.목록.map { it.이름 }.distinct()

    @Test fun 앱칸은_모두_앱카테고리() {
        종목사전.목록.forEach { x -> assertTrue(종목사전.앱칸(x) in 앱데이터.기본카테고리, "${x.이름} → ${종목사전.앱칸(x)}") }
        assertEquals("가슴", 종목사전.앱칸(종목사전.이름으로("딥스")!!))
        assertEquals("복근", 종목사전.앱칸(종목사전.이름으로("크런치")!!))
        assertEquals("등", 종목사전.앱칸(종목사전.이름으로("인버티드 로우")!!))
    }

    @Test fun 장비() {
        fun 장(n: String) = 종목사전.장비(종목사전.이름으로(n)!!)
        assertEquals("바벨", 장("벤치프레스"))
        assertEquals("덤벨", 장("덤벨 컬"))
        assertEquals("덤벨", 장("해머 컬"))
        assertEquals("머신", 장("펙 덱 플라이"))
        assertEquals("케이블", 장("랫풀다운"))
        assertEquals("스미스", 장("스미스 머신 스쿼트"))
        assertEquals("맨몸", 장("딥스"))
        assertEquals("맨몸", 장("턱걸이"))
    }

    @Test fun 빈데이터에_채움() {
        val d = 앱데이터().사전채움()
        assertEquals(사전채움판, d.사전채움)
        assertEquals(이름들.size, d.종목표.size)
        assertEquals(d.종목표.map { it.id }.distinct().size, d.종목표.size)
        d.종목표.forEach { assertTrue(it.부위 in d.카테고리, "${it.이름} 카테고리 ${it.부위}") }
        fun 세팅(n: String) = d.종목기본세트(n)
        // 10-09 홍겸 님: 1세트 · 10회 · 덤벨 2 · 바벨 20 · 머신 10
        assertEquals(listOf(종목세트(20.0, 10, 60)), 세팅("벤치프레스"))
        assertEquals(listOf(종목세트(10.0, 10, 60)), 세팅("펙 덱 플라이"))
        assertEquals(listOf(종목세트(2.0, 10, 60)), 세팅("덤벨 컬"))
        assertEquals(0.0, 세팅("딥스")[0].w)
        assertEquals("가슴", d.종목찾기("벤치프레스")?.부위)
        // 근육은 비워 둔다 → 내장 규칙(보조 0.5) 그대로
        assertTrue(d.종목표.all { it.근육.isEmpty() })
        // 데드리프트는 앱이 정해 둔 대로 등 (스탯 8-10)
        assertEquals("등", d.종목찾기("데드리프트")?.부위)
    }

    /** 플랜 종목 8개는 모두 사전에 있다 → 채우면 '기타' 칸이 생기지 않는다 */
    @Test fun 플랜종목은_사전에_있다() {
        listOf("벤치프레스", "백 스쿼트", "데드리프트", "오버헤드 프레스", "펜들레이 로우", "턱걸이", "팔굽혀펴기", "맨몸 스쿼트")
            .forEach { assertNotNull(종목사전.이름으로(it), it) }
    }

    @Test fun 한번만_지운것은_안살아남() {
        val d = 앱데이터().사전채움()
        val 지움 = d.copy(종목표 = d.종목표.filter { it.이름 != "리스트 컬" })
        assertEquals(지움, 지움.사전채움())
        assertFalse(지움.사전채움().종목표.any { it.이름 == "리스트 컬" })
    }

    @Test fun 있던종목은_그대로_칸만고침() {
        val 내것 = listOf(종목("벤치프레스", "가슴", "바벨"), 종목("딥스", ""))
        val d0 = 앱데이터(종목표 = 내것, 종목설정 = mapOf("벤치프레스" to listOf(종목세트(60.0, 5, 120))))
        val d = d0.사전채움()
        assertEquals(1, d.종목표.count { it.이름 == "벤치프레스" })
        assertEquals(listOf(종목세트(60.0, 5, 120)), d.종목설정["벤치프레스"])
        assertEquals("가슴", d.종목표.first { it.이름 == "딥스" }.부위)
        assertEquals(내것.size + 이름들.size - 2, d.종목표.size)
    }

    /** 띄어쓰기만 다르거나 다른 이름으로 이미 있으면 하나 더 만들지 않는다 */
    @Test fun 다른이름으로_있으면_안넣음() {
        val d = 앱데이터(종목표 = listOf(종목("펙덱", "가슴", "머신"), 종목("랫 풀다운", "등"))).사전채움()
        assertFalse(d.종목표.any { it.이름 == "펙 덱 플라이" })
        assertFalse(d.종목표.any { it.이름 == "랫풀다운" })
    }

    /** 이름을 바꾼 옛 종목(id = 사전 이름)이 있어도 사전 종목을 넣고, id 는 겹치지 않는다 */
    @Test fun 이름바꾼_옛종목() {
        val d = 앱데이터(종목표 = listOf(종목("내 벤치", "가슴", id = "벤치프레스"))).사전채움()
        val 벤치 = d.종목표.first { it.이름 == "벤치프레스" }
        assertTrue(벤치.id != "벤치프레스")
        assertEquals(d.종목표.map { it.id }.distinct().size, d.종목표.size)
        assertEquals(listOf(종목세트(20.0, 10, 60)), d.종목기본세트(벤치.id))
    }

    /** 옛 '기타' 상자(플랜에만 있던 종목)에 넣어 둔 세팅은 그대로 지킨다 */
    @Test fun 남은옛세팅은_지킴() {
        val d = 앱데이터(종목설정 = mapOf("벤치프레스" to listOf(종목세트(60.0, 5, 120)))).사전채움()
        assertEquals(listOf(종목세트(60.0, 5, 120)), d.종목기본세트("벤치프레스"))
    }

    /** 판 1 로 채운 데이터 (10-09) — 손대지 않은 옛 기본(1세트 · 덤벨 2 · 10-09)만 새 기본으로. 고친 세팅 · 지운 종목은 그대로 */
    @Test fun 판1_데이터는_옛기본만_고침() {
        val 옛 = 앱데이터(
            종목표 = listOf(종목("덤벨 컬", "팔", "덤벨"), 종목("벤치프레스", "가슴", "바벨"), 종목("펙 덱 플라이", "가슴", "머신")),
            종목설정 = mapOf(
                "덤벨 컬" to List(5) { 종목세트(5.0, 10, 60) },
                "벤치프레스" to List(5) { 종목세트(20.0, 10, 60) },
                "펙 덱 플라이" to listOf(종목세트(40.0, 12, 90)),   // 사용자가 고친 것
            ),
            사전채움 = 1,
        )
        val d = 옛.사전채움()
        assertEquals(사전채움판, d.사전채움)
        assertEquals(listOf(종목세트(2.0, 10, 60)), d.종목기본세트("덤벨 컬"))
        assertEquals(listOf(종목세트(20.0, 10, 60)), d.종목기본세트("벤치프레스"))
        assertEquals(listOf(종목세트(40.0, 12, 90)), d.종목기본세트("펙 덱 플라이"))
        assertEquals(3, d.종목표.size)   // 지운 사전 종목을 다시 넣지 않는다
        assertEquals(d, d.사전채움())
    }

    /** 세팅이 없는 종목 — 설정의 기본 세트 수 × 장비별 무게 · 10회 */
    @Test fun 세팅없는_종목은_장비별_무게() {
        val d = 앱데이터(종목표 = listOf(종목("내 덤벨", "팔", "덤벨"), 종목("내 케이블", "등", "케이블"), 종목("내 운동", "등")))
        assertEquals(2.0, d.종목기본세트("내 덤벨")[0].w)
        assertEquals(10.0, d.종목기본세트("내 케이블")[0].w)
        assertEquals(20.0, d.종목기본세트("내 운동")[0].w)
        assertEquals(10, d.종목기본세트("내 운동")[0].r)
    }

    @Test fun 지운카테고리는_되돌림() {
        val d = 앱데이터(카테고리 = listOf("가슴", "등")).사전채움()
        assertTrue(앱데이터.기본카테고리.all { it in d.카테고리 })
    }

    @Test fun 저장왕복() {
        val d = 앱데이터().사전채움()
        val e = 저장소.글에서(저장소.글로(d))
        assertEquals(사전채움판, e.사전채움)
        assertEquals(d.종목표.size, e.종목표.size)
        assertEquals(0, 저장소.글에서("{}").사전채움)
    }

    @Test fun 비슷한글자_검색() {
        assertTrue(종목사전.찾기("팩").any { it.이름 == "펙 덱 플라이" })
        assertTrue(종목사전.찾기("래터럴").any { it.이름 == "사이드 레터럴 레이즈" })
        assertTrue(종목사전.찾기("팩덱").any { it.이름 == "펙 덱 플라이" })
        // 똑같이 맞는 것이 있으면 그것만 (비슷한 것은 섞지 않는다)
        val 벤치 = 종목사전.찾기("벤치")
        assertTrue(벤치.any { it.이름 == "벤치프레스" })
        assertTrue(벤치.all { x -> (listOf(x.이름) + x.별).any { 종목사전.다듬(it).contains("벤치") } })
        assertEquals("벤치프레스", 종목사전.찾기("ㅂㅊㅍ").first().이름)
        // 한 글자 틀려도 (3글자 이상)
        assertTrue(종목사전.찾기("데드리프투").any { it.이름 == "데드리프트" })
    }
}
