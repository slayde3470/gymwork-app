package com.slayde.hasenheide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.하젠하이데테마

/**
 * 앱을 켰을 때 가장 먼저 실행되는 곳.
 *
 * 지금은 빌드와 설치가 제대로 되는지 확인하기 위한 화면만 있다.
 * 실제 기능(루틴, 기록, 타이머)은 다음 단계에서 붙인다.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 화면 가장자리(상태바·내비게이션바)까지 앱이 그려지게 한다
        enableEdgeToEdge()
        setContent {
            하젠하이데테마 {
                Scaffold(modifier = Modifier.fillMaxSize()) { 여백 ->
                    첫화면(Modifier.padding(여백))
                }
            }
        }
    }
}

@Composable
fun 첫화면(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(간격.보통),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("하젠하이데", style = MaterialTheme.typography.headlineLarge)
        Text(
            "운동 기록 앱",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 간격.좁게)
        )
        Text(
            "버전 ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 간격.넓게)
        )
        Text(
            "설치가 되었다면 빌드 통로가 뚫린 것입니다.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 간격.보통)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun 첫화면미리보기() {
    하젠하이데테마 { 첫화면() }
}
