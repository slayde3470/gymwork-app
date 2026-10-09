package com.slayde.hasenheide.ui

// ⚠ tools/근육그림_생성.py 가 만든 파일 — 손으로 고치지 않는다 (10-09)
// 그림 = assets/muscle/번호.webp · 칸 번호 판 = assets/muscle/번호_map.png
// 판의 값 = 칸번호(1~15) × 16 + 쪽(0 좌우 없음 · 1 사람의 왼쪽 · 2 오른쪽) · 0 = 빈 곳

object 근육그림표 {
    class 그림(val 번호: String, val 이름: String, val 폭: Int, val 높이: Int)
    /** 칸 하나 — [근육] = 이 칸 값을 정하는 근육 나무 id (아래 가지 포함 · 가장 큰 값) */
    class 칸(val 이름: String, val 근육: List<String>)

    val 그림들 = listOf(
        그림("01", "전신앞", 576, 1032),
        그림("02", "전신뒤", 576, 1032),
        그림("04", "팔어깨확대", 640, 640),
        그림("05", "굽힌다리확대", 640, 640),
        그림("06", "등확대", 640, 640),
        그림("07", "가슴정면", 640, 640),
    )

    /** 반신 자르기 상자 [x, y, 폭, 높이] (전신 그림 픽셀 · 앞 · 뒤 같은 자리) */
    val 위상자 = floatArrayOf(15f, 109f, 543f, 442f)
    val 아래상자 = floatArrayOf(120f, 446f, 334f, 582f)

    /** 칸번호 - 1 순서 */
    val 칸들 = listOf(
        칸("목", listOf("scm", "scalenes", "splenius", "levator_scapulae")),   // 1
        칸("어깨", listOf("deltoid", "rotator_cuff")),   // 2
        칸("가슴", listOf("pectoralis_major", "pectoralis_minor", "serratus")),   // 3
        칸("광배", listOf("lats", "teres_major")),   // 4
        칸("승모", listOf("traps", "rhomboids")),   // 5
        칸("허리", listOf("lower_back", "multifidus", "quadratus_lumborum")),   // 6
        칸("이두", listOf("upper_arm_front")),   // 7
        칸("삼두", listOf("upper_arm_back")),   // 8
        칸("전완", listOf("forearm")),   // 9
        칸("복부", listOf("abs", "obliques", "transversus_abdominis")),   // 10
        칸("엉덩이", listOf("glutes")),   // 11
        칸("대퇴사두", listOf("quads", "sartorius")),   // 12
        칸("햄스트링", listOf("hamstrings")),   // 13
        칸("내전근", listOf("adductors")),   // 14
        칸("종아리", listOf("calves", "shin")),   // 15
    )
}
