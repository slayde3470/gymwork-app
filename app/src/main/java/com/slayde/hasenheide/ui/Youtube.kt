package com.slayde.hasenheide.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slayde.hasenheide.ui.theme.Local색
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * 참고 링크가 유튜브면 작은 그림(썸네일)을 보인다 (09-27 · 업데이트 예정 "유튜브면 썸네일").
 *  · 유튜브 주소에서 영상 번호(11자)를 꺼내 img.youtube.com 의 그림을 받는다 — 따로 받는 라이브러리 없이
 *  · 한 번 받은 그림은 앱이 켜져 있는 동안 기억한다. 못 받으면(인터넷 없음 등) 그림 없이 글씨만
 */
fun 유튜브번호(주소: String): String? {
    val u = 주소.trim()
    if (!u.contains("youtu")) return null
    val 틀 = listOf(
        Regex("""youtu\.be/([A-Za-z0-9_-]{11})"""),
        Regex("""[?&]v=([A-Za-z0-9_-]{11})"""),
        Regex("""youtube\.com/(?:shorts|embed|live|v)/([A-Za-z0-9_-]{11})"""),
    )
    return 틀.firstNotNullOfOrNull { it.find(u)?.groupValues?.get(1) }
}

private object 썸네일창고 { val 것 = mutableMapOf<String, ImageBitmap>() }

@Composable
fun 유튜브썸네일(주소: String, modifier: Modifier = Modifier) {
    val 번호 = remember(주소) { 유튜브번호(주소) } ?: return
    val c = Local색.current
    var 그림 by remember(번호) { mutableStateOf(썸네일창고.것[번호]) }
    var 실패 by remember(번호) { mutableStateOf(false) }
    LaunchedEffect(번호) {
        if (그림 != null) return@LaunchedEffect
        val b = withContext(Dispatchers.IO) {
            try {
                val 연결 = URL("https://img.youtube.com/vi/$번호/mqdefault.jpg").openConnection() as HttpURLConnection
                연결.connectTimeout = 5000; 연결.readTimeout = 5000
                연결.inputStream.use { BitmapFactory.decodeStream(it) }?.asImageBitmap()
            } catch (_: Exception) { null }
        }
        if (b != null) { 썸네일창고.것[번호] = b; 그림 = b } else 실패 = true
    }
    if (실패) return
    val 모양 = RoundedCornerShape(6.dp)
    Box(modifier.clip(모양).background(c.면2).border(1.dp, c.속선, 모양), contentAlignment = Alignment.Center) {
        그림?.let { Image(it, "영상 그림", Modifier.matchParentSize(), contentScale = ContentScale.Crop) }
        // 가운데 ▶ — 누르면 영상이 열린다는 표시
        Box(Modifier.size(22.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.55f)), contentAlignment = Alignment.Center) {
            Text("▶", color = Color.White, fontSize = 10.sp)
        }
    }
}
