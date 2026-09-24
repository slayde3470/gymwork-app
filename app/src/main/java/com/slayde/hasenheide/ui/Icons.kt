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
    val 톱니 = 설정

    /** 참고 링크 — 고리 (09-24) */
    val 링크 = 선("link", "M10 13a5 5 0 0 0 7.5.5l3-3a5 5 0 0 0-7-7l-1.5 1.5", "M14 11a5 5 0 0 0-7.5-.5l-3 3a5 5 0 0 0 7 7L12 19")
}
