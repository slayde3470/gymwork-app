package com.slayde.hasenheide.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import com.slayde.hasenheide.data.돌림
import com.slayde.hasenheide.data.차례글
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.글꼴
import com.slayde.hasenheide.ui.theme.높이
import com.slayde.hasenheide.ui.theme.모서리
import com.slayde.hasenheide.ui.theme.선굵기
import com.slayde.hasenheide.ui.theme.크기

/**
 * 오늘 할 차례 고르기 (10-21 홍겸 님 · 시안 v27 `돌림바꿈`) — 운동 화면 이름 옆 [n/N ⇄] 를 누르면 뜬다. 고르는 일뿐이라 시트 (U5-3).
 * 고르면 [고르기] 를 부르고 닫는다. 고른 차례를 한 것으로 치고 다음엔 그 다음 차례가 나온다 (계산은 [돌림차례바꿈] · [돌림저장])
 */
@Composable
fun 돌림차례시트(상태: com.slayde.hasenheide.ui.앱상태, 칸id: String, 지금: Int?, 고르기: (Int) -> Unit, 닫기: () -> Unit) {
    val c = Local색.current
    val g = 상태.d.돌림(칸id)
    LaunchedEffect(g == null) { if (g == null) 닫기() }
    if (g == null) return
    val 모양 = RoundedCornerShape(모서리.작게)
    시트("오늘 할 차례", 닫기) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(간격.좁게)) {
            g.차례들.forEachIndexed { k, t ->
                val 지금차례 = k == 지금
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 높이.높게).clip(모양)
                        .background(if (지금차례) c.강조옅음 else c.면)
                        .border(선굵기.보통, if (지금차례) c.강조 else c.속선, 모양)
                        .눌림 { 고르기(k); 닫기() }
                        .padding(horizontal = 간격.보통),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(간격.좁게),
                ) {
                    알약("${k + 1}", c.강조)
                    Text(t.차례글(), Modifier.weight(1f), style = 글꼴.보통(크기.본문), color = c.글, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (지금차례) 글("지금", 크기값 = 크기.작게, 색 = c.옅음)
                }
            }
            글("고른 차례를 한 것으로 치고, 다음엔 그 다음 차례가 나옵니다", Modifier.padding(top = 간격.아주좁게), 크기값 = 크기.작게, 색 = c.옅음, 줄 = 2)
        }
    }
}
