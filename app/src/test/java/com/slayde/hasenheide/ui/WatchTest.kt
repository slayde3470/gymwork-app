package com.slayde.hasenheide.ui

import com.slayde.hasenheide.data.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/** 10-05 합치기 감시관(뒤죽박죽 사용) — 종목 이름 바꾸기가 다른 종목 기록과 섞이지 않고, 지난 기록을 새 열쇠로 찾는다 */
class WatchTest {

    @Test fun 이름바꿈_다른종목과_안섞임() {
        val d = 앱데이터(종목표 = listOf(종목("벤치프레스", "가슴"), 종목("덤벨프레스", "가슴")),
            기록 = mapOf("2026-10-01" to 날기록("r", "R", true, listOf(종목기록("덤벨프레스", listOf(세트(30.0, 10)))))))
        val nd = assertNotNull(d.종목고침(새종목값(이름 = "벤치프레스", 칸 = "가슴", 편집 = "덤벨프레스", 근육 = mapOf("chest_mid" to "P")), 5L).d)
        val 바뀐 = nd.종목표.first { it.이름 == "벤치프레스" && it.id != "벤치프레스" }
        val x = nd.기록.getValue("2026-10-01").종목들[0]
        assertEquals(바뀐.id, nd.종목찾기(x.종id, x.이름)?.id)
    }

    // 10-08: 운동 화면 지표 줄(운최고)을 지웠다 — 같은 뜻(지난 기록을 새 열쇠로 찾는다)을 열쇠 비교로 지킨다
    @Test fun 이름바꿈_지난기록_열쇠() {
        val d = 앱데이터(종목표 = listOf(종목("벤치프레스", "가슴")),
            기록 = mapOf("2026-10-01" to 날기록("r", "R", true, listOf(종목기록("벤치프레스", listOf(세트(100.0, 1)))))))
        val nd = assertNotNull(d.종목고침(새종목값(이름 = "벤치", 칸 = "가슴", 편집 = "벤치프레스", 근육 = mapOf("chest_mid" to "P")), 5L).d)
        assertEquals(nd.기록.getValue("2026-10-01").종목들[0].열쇠, nd.세션줄(nd.종목표[0].id).열쇠)
    }
}
