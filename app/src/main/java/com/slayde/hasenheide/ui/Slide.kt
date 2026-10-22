package com.slayde.hasenheide.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.toSize
import com.slayde.hasenheide.ui.theme.움직임

// ═════════════════════ 끌 때 미리 미끄러지기 (10-22 홍겸 님) ═════════════════════
//
// 꾹 눌러 끌어 순서를 바꿀 때, 놓으면 어디로 가는지 **미리** 다른 칸들이 미끄러져 자리를 비켜 준다 (어느 화면이든 같다).
//  · 계산은 끌기 **전에** 잰 칸 자리만 쓴다 (U5-6) — 미끄러지는 칸을 다시 재지 않는다
//  · 그리기만 옮긴다(graphicsLayer) — 자리(레이아웃)는 그대로, 놓는 순간 0 으로 돌아가고 데이터가 새 순서로 바뀐다

/** 한 줄(세로면 위·아래 · 가로면 왼·오른쪽)로 늘어선 칸 하나의 한 축 구간 (화면 px) */
internal data class 칸구간(val 번호: Int, val 시작: Float, val 끝: Float)

/**
 * 끄는 칸 [원] 을 [대상] (대상 번호, 뒤에) 에 놓으면 칸마다 얼마나 비켜야 하나 (px · 양수 = 아래/오른쪽).
 *  · 사이에 있는 칸은 끄는 칸의 크기(+ 칸 사이 틈)만큼 반대쪽으로 비킨다
 *  · 끄는 칸 자신도 놓일 자리로 간다 (흐리게 남은 자리가 미리 옮겨 가 있다)
 *  · 놓아도 제자리이거나 [대상] 이 없으면 빈 표 (아무도 안 비킨다)
 */
internal fun 밀림(칸들: List<칸구간>, 원: Int, 대상: Pair<Int, Boolean>?): Map<Int, Float> {
    if (대상 == null) return emptyMap()
    val 줄 = 칸들.sortedBy { it.시작 }
    val po = 줄.indexOfFirst { it.번호 == 원 }
    val pt = 줄.indexOfFirst { it.번호 == 대상.first }
    if (po < 0 || pt < 0) return emptyMap()
    val 자리 = pt + if (대상.second) 1 else 0   // 끼워 넣을 자리 (옮기기 전 줄 기준)
    val 나 = 줄[po]
    val 표 = HashMap<Int, Float>()
    if (자리 > po + 1) {            // 뒤로 간다 — 지나는 칸들이 앞으로 당겨진다
        val 양 = 줄[po + 1].시작 - 나.시작   // 내 크기 + 틈
        for (q in po + 1 until 자리) 표[줄[q].번호] = -양
        표[원] = 줄[자리 - 1].끝 - 나.끝
    } else if (자리 < po) {         // 앞으로 간다 — 지나는 칸들이 뒤로 밀린다
        val 양 = 나.끝 - 줄[po - 1].끝       // 내 크기 + 틈
        for (q in 자리 until po) 표[줄[q].번호] = 양
        표[원] = 줄[자리].시작 - 나.시작
    }
    return 표
}

/**
 * [목표] 만큼 부드럽게 따라가는 값 — 끄는 동안만. 끝나면 (놓으면) 바로 0 (칸이 이미 새 자리에 있다).
 * 읽는 쪽은 [미끄러짐] 으로 그릴 때만 읽는다 (다시 짜지 않는다)
 */
@Composable
internal fun 미끄럼(목표: Float, 끄는중: Boolean): Animatable<Float, AnimationVector1D> {
    val 배 = remember { Animatable(0f) }
    LaunchedEffect(목표, 끄는중) {
        if (끄는중) 배.animateTo(목표, tween(움직임.미끄럼)) else 배.snapTo(0f)
    }
    return 배
}

/** [미끄럼] 값만큼 그리기만 옮긴다. 끄는 중이 아니면 0 — 놓는 그 프레임에 새 자리와 겹쳐 튀지 않게 값이 아니라 여기서 먼저 끊는다 */
internal fun Modifier.미끄러짐(배: Animatable<Float, AnimationVector1D>, 가로: Boolean = false, 끄는중: () -> Boolean): Modifier =
    this.graphicsLayer {
        val v = if (끄는중()) 배.value else 0f
        if (가로) translationX = v else translationY = v
    }

/**
 * 10-21 홍겸 님: [boundsInRoot] 는 스크롤 목록 밖으로 나간 · 걸친 칸을 **잘라서** 돌려준다 —
 * 화면 밖에 있던 칸은 높이 0 으로 잡혀, 자동으로 넘긴 뒤 그 칸 위에 놓아도 놓을 곳을 못 찾았다. 잘리지 않은 자리(위치 + 크기)로 잰다
 */
internal fun LayoutCoordinates.잘리지않은틀(): Rect = Rect(positionInRoot(), size.toSize())
