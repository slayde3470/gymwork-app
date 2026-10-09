package com.slayde.hasenheide.ui

import com.slayde.hasenheide.data.종목사전
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 새 근육 그림 표 (10-09 · MusclePic.kt · MusclePicData.kt — tools/근육그림_생성.py 가 만든다).
 *  · 세부 부위 24개가 모두 어느 칸엔가 걸린다 (그림을 눌러 고를 수 있다)
 *  · 칸 번호 판의 값이 표와 맞다 (칸번호 1~15 × 16 + 쪽 0 · 1 · 2)
 *  · 확대 그림에는 정한 칸만 칠해져 있다 (대표칸/설명.md)
 */
class MusclePicTest {

    @Test fun 세부_부위는_모두_어느_칸엔가_걸린다() {
        val 걸린 = 칸세부키.flatten().toSet()
        val 빠진 = 종목사전.세부키 - 걸린
        assertTrue(빠진.isEmpty(), "칸에 없는 세부 부위: $빠진")
    }

    @Test fun 칸_값은_칸에_든_잎_중_가장_큰_단계() {
        val 가슴 = 근육그림표.칸들.indexOfFirst { it.이름 == "가슴" }
        val 값 = 칸단계(mapOf("chest_mid" to 12.0, "chest_upper" to 5.0))
        assertEquals(12.0, 값[가슴])
        assertEquals(0.0, 값[근육그림표.칸들.indexOfFirst { it.이름 == "광배" }])
    }

    @Test fun 전거근은_가슴_능형근은_승모_칸() {
        val 칸 = { 이름: String -> 칸세부키[근육그림표.칸들.indexOfFirst { it.이름 == 이름 }] }
        assertTrue("serratus" in 칸("가슴"))
        assertTrue("rhomboids" in 칸("승모"))
        assertTrue("serratus" !in 칸("복부"))
    }

    private fun 판칸(번호: String): Set<String> {
        val f = listOf("src/main/assets/muscle/${번호}_map.png", "app/src/main/assets/muscle/${번호}_map.png").map(::File).first { it.exists() }
        val im = ImageIO.read(f)
        val 칸 = HashSet<String>()
        for (y in 0 until im.height) for (x in 0 until im.width) {
            val v = im.raster.getSample(x, y, 0)
            if (v == 0) continue
            val i = 판값칸(v)
            assertTrue(i != null && v % 16 <= 2, "$번호 판에 표에 없는 값 $v")
            칸 += 근육그림표.칸들[i!!].이름
        }
        return 칸
    }

    @Test fun 확대_그림의_칸() {
        assertEquals(setOf("어깨", "이두", "삼두", "전완"), 판칸("04"))
        assertEquals(setOf("엉덩이", "대퇴사두", "햄스트링", "종아리"), 판칸("05"))
        assertEquals(setOf("승모", "광배", "삼두", "어깨", "허리"), 판칸("06"))
        assertEquals(setOf("가슴", "어깨"), 판칸("07"))
    }

    @Test fun 전신_그림은_15칸을_앞뒤로_다_가진다() {
        assertEquals(15, (판칸("01") + 판칸("02")).size)
    }
}
