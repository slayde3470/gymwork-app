package com.slayde.hasenheide.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slayde.hasenheide.data.남은초
import com.slayde.hasenheide.data.분초
import com.slayde.hasenheide.data.시분초
import com.slayde.hasenheide.data.찬것
import com.slayde.hasenheide.data.총칸
import com.slayde.hasenheide.data.흐른초
import com.slayde.hasenheide.ui.theme.Local색
import kotlinx.coroutines.delay

/**
 * 작은 창(PiP) 화면 (09-25 메모: 홈 버튼을 눌러도 운동을 볼 수 있게).
 *  · 쉬는 중 → 남은 휴식을 크게. 색은 운동 화면과 같다 (초록 → 절반 이하 파랑 → 5초 이하 빨강)
 *  · 쉬는 중이 아니면 → '운동 중' 과 지금 종목 · 세트. 창을 누르면 앱으로 돌아간다 (안드로이드가 해 준다)
 * 앱 화면 위에 덮어 그린다 — 뒤의 앱이 살아 있어야 휴식 시계와 알림이 돈다.
 */
@Composable
fun 작은창(상태: 앱상태) {
    val c = Local색.current
    var 지금 by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { 지금 = System.currentTimeMillis(); delay(250) } }
    val S = 상태.d.세션
    Column(
        Modifier.fillMaxSize().background(c.바탕).padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
    ) {
        if (S == null) {
            Text("운동이 끝났습니다", color = c.흐림, fontSize = 14.sp)
            return@Column
        }
        val h = S.휴식
        if (h != null && !h.물음) {
            val 남은 = h.남은초(지금)
            val 총 = if (h.총초 > 0) h.총초 else 남은
            val 색 = when {
                남은 <= 5 -> c.나쁨
                총 > 0 && 남은 * 2 <= 총 -> c.내림
                else -> c.좋음
            }
            Text("휴식", color = c.흐림, fontSize = 12.sp)
            Text(분초(남은), color = 색, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            val 다음 = (h.다음i ?: S.i).let { S.종목들.getOrNull(it)?.이름 }
            if (다음 != null) Text("다음 · $다음", color = c.흐림, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        } else {
            val e = S.종목들.getOrNull(S.i)
            Text("운동 중 · ${시분초(S.흐른초(지금))}", color = c.흐림, fontSize = 12.sp)
            if (e != null) {
                Text(e.이름, color = c.글, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${e.찬것().size}/${e.총칸()}세트", color = c.강조, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Text("눌러서 돌아가기", color = c.옅음, fontSize = 11.sp)
        }
    }
}
