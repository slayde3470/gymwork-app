package com.slayde.hasenheide.ui

import com.slayde.hasenheide.data.종목사전
import java.io.File
import java.io.DataInputStream
import java.util.zip.Inflater
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

    /** 8비트 회색 PNG 읽기 (안드로이드 단위시험에는 ImageIO 가 없다) → (폭, 높이, 값들) */
    private fun 회색png(f: File): Triple<Int, Int, IntArray> {
        val inp = DataInputStream(f.inputStream().buffered())
        inp.skipBytes(8)
        var w = 0; var h = 0
        val idat = java.io.ByteArrayOutputStream()
        while (true) {
            val n = inp.readInt(); val t = String(ByteArray(4).also { inp.readFully(it) })
            val d = ByteArray(n).also { inp.readFully(it) }; inp.readInt()
            if (t == "IHDR") {
                w = java.nio.ByteBuffer.wrap(d, 0, 4).int; h = java.nio.ByteBuffer.wrap(d, 4, 4).int
                assertEquals(8, d[8].toInt()); assertEquals(0, d[9].toInt(), "회색 PNG 여야 한다")
            }
            if (t == "IDAT") idat.write(d)
            if (t == "IEND") break
        }
        val raw = ByteArray((w + 1) * h)
        Inflater().apply {
            setInput(idat.toByteArray())
            var o = 0
            while (o < raw.size && !finished()) o += inflate(raw, o, raw.size - o)
            end()
        }
        val out = IntArray(w * h)
        for (y in 0 until h) {
            val ft = raw[y * (w + 1)].toInt()
            for (x in 0 until w) {
                val r = raw[y * (w + 1) + 1 + x].toInt() and 0xFF
                val a = if (x > 0) out[y * w + x - 1] else 0
                val b = if (y > 0) out[(y - 1) * w + x] else 0
                val c = if (x > 0 && y > 0) out[(y - 1) * w + x - 1] else 0
                val pr = when (ft) {
                    0 -> 0; 1 -> a; 2 -> b; 3 -> (a + b) / 2
                    else -> { val p = a + b - c; val pa = Math.abs(p - a); val pb = Math.abs(p - b); val pc = Math.abs(p - c)
                        if (pa <= pb && pa <= pc) a else if (pb <= pc) b else c }
                }
                out[y * w + x] = (r + pr) and 0xFF
            }
        }
        return Triple(w, h, out)
    }

    private fun 판칸(번호: String): Set<String> {
        val f = listOf("src/main/assets/muscle/${번호}_map.png", "app/src/main/assets/muscle/${번호}_map.png").map(::File).first { it.exists() }
        val (_, _, 값들) = 회색png(f)
        val 칸 = HashSet<String>()
        for (v in 값들) {
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
