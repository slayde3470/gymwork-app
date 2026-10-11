// 10-11 홍겸 님: 처음 시작할 때 알림(권한) 안내 — 크게, 운동중 · 휴식중(60초부터) 떠다니는 단추가 움직이는 모습을 보여주고
//   "켜면 앱 밖에서도 바로 단추를 누르고 돌아올 수 있다"고 알려서 더 많이 켜게 한다
package com.slayde.hasenheide.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.slayde.hasenheide.R
import com.slayde.hasenheide.ui.theme.*
import kotlin.math.PI
import kotlin.math.sin

private const val 시연한바퀴 = 7000   // 운동중 2.8초 → 휴식중(60초부터 줄어듦) 4.2초
private const val 운동몫 = 0.4f

/** 안내 박스 — '켜기' = 권한 설정으로, '나중에' · 바깥 · 뒤로가기 = 닫기 */
@Composable
fun 알림안내창(on켜기: () -> Unit, on나중에: () -> Unit) {
    val c = Local색.current
    val 보임 = remember { MutableTransitionState(false).apply { targetState = true } }
    var 닫는중 by remember { mutableStateOf(false) }
    val 나중최신 by rememberUpdatedState(on나중에)
    fun 나중에() { if (!닫는중) { 닫는중 = true; 보임.targetState = false } }
    LaunchedEffect(보임.isIdle, 보임.currentState) { if (닫는중 && 보임.isIdle && !보임.currentState) 나중최신() }
    BackHandler(onBack = { 나중에() })
    val 바퀴 = rememberInfiniteTransition(label = "안내시연")
    val 위치 by 바퀴.animateFloat(0f, 1f, infiniteRepeatable(tween(시연한바퀴, easing = LinearEasing), RepeatMode.Restart), label = "시연위치")
    val 파도 by 바퀴.animateFloat(0f, 1f, infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Restart), label = "시연파도")
    val 휴식중 by remember { derivedStateOf { 위치 >= 운동몫 } }
    AnimatedVisibility(visibleState = 보임, enter = fadeIn(tween(움직임.물음)), exit = fadeOut(tween(움직임.물음)), label = "알림안내") {
        Box(
            Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)).눌림 { 나중에() },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                Modifier.fillMaxWidth(0.9f).clip(RoundedCornerShape(모서리.보통)).background(c.면).눌림 { }.padding(간격.넓게),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                제목글("알림을 켜면 이렇게 보여요", 크기값 = 크기.크게)
                Box(
                    Modifier.padding(top = 간격.보통).fillMaxWidth().height(170.dp).clip(RoundedCornerShape(모서리.보통)).background(c.면2),
                    contentAlignment = Alignment.Center,
                ) { 시연동그라미({ 위치 }, { 파도 }, Modifier.size(150.dp)) }
                글(
                    if (휴식중) "휴식중 · 60초부터 남은 시간이 줄어들어요" else "운동중 · 눌러 보라는 듯 숨을 쉬어요",
                    Modifier.padding(top = 간격.좁게), 크기값 = 크기.버튼, 색 = c.강조, 굵기 = FontWeight.Bold, 가운데 = true,
                )
                Column(Modifier.padding(top = 간격.보통).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(간격.좁게)) {
                    글("· 유튜브나 다른 앱을 보다가도 화면 위에 이 단추가 떠 있어요", 크기값 = 크기.버튼, 줄 = 2)
                    글("· 앱 밖에서 바로 단추를 누르고 운동으로 돌아올 수 있어요", 크기값 = 크기.버튼, 줄 = 2)
                    글("· 휴식이 끝나는 순간도 놓치지 않아요", 크기값 = 크기.버튼, 줄 = 2)
                }
                글("켜야만 보여요 · 설정에서 언제든 끌 수 있어요", Modifier.padding(top = 간격.좁게), 크기값 = 크기.버튼, 색 = c.흐림, 줄 = 2)
                Row(Modifier.padding(top = 간격.넓게), horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                    버튼("나중에", { 나중에() }, Modifier.weight(1f), 작게 = true)
                    버튼("켜기", on켜기, Modifier.weight(1f), 작게 = true, 주요 = true)
                }
            }
        }
    }
}

/** 떠다니는 단추(Bubble.kt)와 같은 그림 — 숨 쉬는 동그라미 · 퍼지는 고리 · 줄어드는 띠 */
@Composable
private fun 시연동그라미(위치: () -> Float, 파도: () -> Float, modifier: Modifier) {
    val c = Local색.current
    val ctx = LocalContext.current
    val 로고 = remember { try { ContextCompat.getDrawable(ctx, R.mipmap.ic_launcher) } catch (_: Exception) { null } }
    val 재기 = rememberTextMeasurer()
    Canvas(modifier) {
        val 칸 = size.width
        val r = 칸 * (33.17f / 92.15f)
        val 굵 = 칸 * (3.3f / 92.15f)
        val 중심 = Offset(size.width / 2, size.height / 2)
        val t = 파도()
        val p = 위치()
        val 숨 = 1f + 0.025f + 0.025f * sin(t * 2 * PI).toFloat()
        val 퍼짐 = 칸 / 2f - r
        if (p < 운동몫) {
            drawCircle(c.강조.copy(alpha = (1f - t) * 0.78f), r + 퍼짐 * t, 중심, style = Stroke(굵))
            drawCircle(c.강조, r * 숨, 중심)
            val 길 = Path().apply { addOval(Rect(중심, r)) }
            clipPath(길) {
                drawIntoCanvas { cv ->
                    로고?.let { it.setBounds((중심.x - r).toInt(), (중심.y - r).toInt(), (중심.x + r).toInt(), (중심.y + r).toInt()); it.draw(cv.nativeCanvas) }
                }
            }
        } else {
            val 비 = 1f - (p - 운동몫) / (1f - 운동몫)   // 1 → 0
            drawCircle(c.좋음.copy(alpha = (1f - t) * 0.78f), r + 퍼짐 * t, 중심, style = Stroke(굵))
            drawCircle(c.좋음, r * 숨, 중심)
            val 옛굵 = 굵
            val 띠굵 = 옛굵 * 1.25f
            val 띠반 = r - 옛굵 / 2f - 띠굵 / 2f
            val 왼위 = Offset(중심.x - 띠반, 중심.y - 띠반)
            val 띠크기 = Size(띠반 * 2, 띠반 * 2)
            drawArc(c.강조글.copy(alpha = 70 / 255f), 0f, 360f, false, 왼위, 띠크기, style = Stroke(띠굵, cap = StrokeCap.Round))
            drawArc(c.강조글, -90f + 360f * (1f - 비), 360f * 비, false, 왼위, 띠크기, style = Stroke(띠굵, cap = StrokeCap.Round))
            val 초 = (60 * 비).toInt().coerceIn(0, 60)
            val 글 = "%d:%02d".format(초 / 60, 초 % 60)
            val 결과 = 재기.measure(글, TextStyle(color = c.강조글, fontSize = (칸 * 18.4f / 92.15f / density).sp, fontWeight = FontWeight.Bold))
            drawText(결과, topLeft = Offset(중심.x - 결과.size.width / 2f, 중심.y - 결과.size.height / 2f))
        }
    }
}
