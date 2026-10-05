package com.slayde.hasenheide.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * 아이콘 한 벌 — 웹 시제품과 똑같은 선 아이콘 (선 굵기 2, 둥근 끝).
 * 이모지나 글자 기호를 쓰지 않는다 (기능명세 1-2).
 * 색은 쓰는 곳에서 tint 로 입힌다.
 */
object 아이콘 {
    private fun 선(이름: String, vararg 길: String): ImageVector {
        val b = ImageVector.Builder(이름, 24.dp, 24.dp, 24f, 24f)
        길.forEach {
            b.addPath(
                pathData = addPathNodes(it),
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
        return b.build()
    }

    /** SVG <circle> 을 path 로 — 반원 호 둘 */
    private fun 원(cx: Float, cy: Float, r: Float): String = "M${cx - r} ${cy}a$r $r 0 1 0 ${r * 2} 0a$r $r 0 1 0 ${-r * 2} 0z"

    val 지우기 = 선("trash", "M4 7h16M10 11v6M14 11v6M6 7l1 12a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2l1-12M9 7V4h6v3")
    val 설정 = 선("gear",
        "M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z",
        "M15 12a3 3 0 1 1-6 0a3 3 0 1 1 6 0z")
    val 아래 = 선("chevD", "M6 9l6 6 6-6")
    val 왼쪽 = 선("chevL", "M15 18l-6-6 6-6")
    val 오른쪽 = 선("chevR", "M9 18l6-6-6-6")
    val 위로 = 선("up", "M12 19V5M5 12l7-7 7 7")
    val 아래로 = 선("down", "M12 5v14M19 12l-7 7-7-7")
    val 닫기 = 선("x", "M18 6L6 18M6 6l12 12")
    val 더하기 = 선("plus", "M12 5v14M5 12h14")
    val 빼기 = 선("minus", "M5 12h14")
    val 체크 = 선("check", "M20 6L9 17l-5-5")
    val 목록 = 선("list", "M9 6h11M9 12h11M9 18h11M4.5 6h.01M4.5 12h.01M4.5 18h.01")
    val 연필 = 선("pencil", "M12 20h9", "M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4z")

    // 아래 탭
    val 달력 = 선("cal", "M8 2v4M16 2v4M3 10h18", "M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z")
    val 루틴 = 선("rou", "M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01")
    val 바벨 = 선("ex", "M6.5 6.5v11M17.5 6.5v11M3 9v6M21 9v6M6.5 12h11")
    /** 설정 탭 — 진짜 톱니 (시안 v21 `아이콘.톱니` · 이 8개에 가운데 원). 루틴 · 종목 화면의 작은 '설정' 단추는 [설정] 그대로 */
    val 톱니 = 선("cog",
        "M10.23 4.61L10.45 1.62A10.5 10.5 0 0 1 13.55 1.62L13.77 4.61A7.6 7.6 0 0 1 15.97 5.52L18.25 3.56A10.5 10.5 0 0 1 20.44 5.75L18.48 8.03A7.6 7.6 0 0 1 19.39 10.23L22.38 10.45A10.5 10.5 0 0 1 22.38 13.55L19.39 13.77A7.6 7.6 0 0 1 18.48 15.97L20.44 18.25A10.5 10.5 0 0 1 18.25 20.44L15.97 18.48A7.6 7.6 0 0 1 13.77 19.39L13.55 22.38A10.5 10.5 0 0 1 10.45 22.38L10.23 19.39A7.6 7.6 0 0 1 8.03 18.48L5.75 20.44A10.5 10.5 0 0 1 3.56 18.25L5.52 15.97A7.6 7.6 0 0 1 4.61 13.77L1.62 13.55A10.5 10.5 0 0 1 1.62 10.45L4.61 10.23A7.6 7.6 0 0 1 5.52 8.03L3.56 5.75A10.5 10.5 0 0 1 5.75 3.56L8.03 5.52A7.6 7.6 0 0 1 10.23 4.61Z",
        원(12f, 12f, 3f))
    /** 플랜 탭 — 목표(과녁) (09-28) */
    val 과녁 = 선("target", "M12 21a9 9 0 1 0 0-18a9 9 0 1 0 0 18z", "M12 16.5a4.5 4.5 0 1 0 0-9a4.5 4.5 0 1 0 0 9z", "M12 13.5a1.5 1.5 0 1 0 0-3a1.5 1.5 0 1 0 0 3z")

    /** 참고 링크 · SNS 링크 — 고리 (09-24 · 10-05 시안 v17 `아이콘.링크` 로 바꿈 — 같은 뜻은 같은 그림) */
    val 링크 = 선("link", "M10 14a4 4 0 0 0 5.7 0l3-3a4 4 0 0 0-5.7-5.7l-1 1", "M14 10a4 4 0 0 0-5.7 0l-3 3a4 4 0 0 0 5.7 5.7l1-1")

    // ── 10-05 옮기기 1단계 — 시안 v17 ~ v21 아이콘 (SVG path 그대로) ──
    /** 검색 탭 · 검색 칸 */
    val 돋보기 = 선("search", 원(11f, 11f, 7f), "M20 20l-4-4")
    /** 검색 화면 왼쪽 위 (v18 E ⑥) */
    val 북마크 = 선("bookmark", "M6 3h12v18l-6-4.5L6 21z")
    /** 보고서 이미지 저장 (v19 B) */
    val 카메라 = 선("camera", "M3 8.5A2 2 0 0 1 5 6.5h2.6L9.2 4h5.6l1.6 2.5H19a2 2 0 0 1 2 2V18a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z", 원(12f, 13f, 3.5f))
    /** 보고서 공유 (v19 B) */
    val 공유 = 선("share", 원(18f, 5f, 2.5f), 원(6f, 12f, 2.5f), 원(18f, 19f, 2.5f), "M8.2 13.3l7.6 4.4M15.8 6.3l-7.6 4.4")
    /** 인증샷 고정 */
    val 핀 = 선("pin", "M9 3h6l-1 6 3 3v2H7v-2l3-3z", "M12 14v7")
    /** 소셜 탭 — 사람 상반신 (v21 ⑤ 머리 원 + 어깨 호) */
    val 사람 = 선("person", 원(12f, 7f, 4f), "M4 21a8 8 0 0 1 16 0")
    /** 프로필 동그라미 속 — 사진 · 닉네임이 없을 때 (시안 `사람그림` · 보고서 · 프로필 탭 · 탭줄이 같이 쓴다) */
    val 프로필사람 = 선("avatar", 원(12f, 8f, 4f), "M4 21c0-4.4 3.6-8 8-8s8 3.6 8 8")
    /** 당겨서 새로고침 — 도는 고리 (시안 `돌림그림`) */
    val 돌림 = 선("spin", "M21 12a9 9 0 1 1-6.2-8.56")
    /** 칩줄 · 칸 줄 끝의 ‹ › (시안 `칩화살그림`) */
    val 칩왼쪽 = 선("chipL", "M15 6l-6 6 6 6")
    val 칩오른쪽 = 선("chipR", "M9 6l6 6-6 6")
}
