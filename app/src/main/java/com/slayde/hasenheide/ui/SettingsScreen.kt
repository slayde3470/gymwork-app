package com.slayde.hasenheide.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.slayde.hasenheide.BuildConfig
import com.slayde.hasenheide.data.기준목록
import com.slayde.hasenheide.data.무게글
import com.slayde.hasenheide.data.분초
import com.slayde.hasenheide.data.설정값
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.크기

@Composable
fun 설정화면(상태: 앱상태, 폰: 폰기능) {
    val c = Local색.current
    val s = 상태.d.설정
    fun 고침(f: (설정값) -> 설정값) = 상태.바꿈 { it.copy(설정 = f(it.설정)) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 간격.넓게)) {
        제목글("설정", Modifier.padding(top = 14.dp, bottom = 12.dp))

        이름표("운동 진행", Modifier.padding(bottom = 6.dp))
        카드(안쪽 = 14.dp) {
            설정줄("자동 진행", "휴식 끝나면 다음 세트로") { 스위치(s.자동진행) { v -> 고침 { it.copy(자동진행 = v) } } }
            구분선()
            설정줄("넘어가기 전 확인", "넘어가기 전에 한 번 묻기") { 스위치(s.넘어가기전확인) { v -> 고침 { it.copy(넘어가기전확인 = v) } } }
            구분선()
            설정줄("소리 · 진동", "휴식이 끝나면 알립니다") { 스위치(s.소리진동) { v -> 고침 { it.copy(소리진동 = v) } } }
            구분선()
            설정줄("운동 중 화면 켜 두기", "휴식 중에 화면이 꺼지지 않게") { 스위치(s.화면유지) { v -> 고침 { it.copy(화면유지 = v) } } }
        }

        이름표("기록", Modifier.padding(top = 18.dp, bottom = 6.dp))
        카드(안쪽 = 14.dp) {
            설정줄("무게 조절 폭", "＋ − 한 번에 · 직접 입력은 0.1 단위") {}
            칩줄(listOf(0.5, 1.0, 1.25, 2.5, 5.0).map { 무게글(it) }, 무게글(s.무게폭), { t -> 고침 { it.copy(무게폭 = t.toDouble()) } })
            Box(Modifier.height(10.dp)); 구분선()
            설정줄("기본 휴식", "새 종목을 넣을 때") {}
            칩줄(listOf(60, 90, 120, 150, 180).map { 분초(it) }, 분초(s.기본휴식), { t ->
                val (m, x) = t.split(":"); 고침 { it.copy(기본휴식 = m.toInt() * 60 + x.toInt()) }
            })
            Box(Modifier.height(10.dp)); 구분선()
            설정줄("향상도 기준", "무엇과 견줄까요") {}
            칩줄(기준목록.map { it.짧 }, 기준목록.firstOrNull { it.k == s.기준 }?.짧, { t ->
                val k = 기준목록.first { it.짧 == t }.k; 고침 { it.copy(기준 = k) }
            })
        }

        이름표("데이터", Modifier.padding(top = 18.dp, bottom = 6.dp))
        카드(안쪽 = 14.dp) {
            글("기록은 이 폰 안에만 저장됩니다", 크기값 = 크기.조금작게, 색 = c.흐림)
            글("폰을 바꾸거나 앱을 지우기 전에 백업 파일로 내보내 두세요", 크기값 = 크기.작게, 색 = c.옅음)
            Box(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                버튼("백업 내보내기", 폰.내보내기, Modifier.weight(1f), 작게 = true)
                버튼("백업 가져오기", 폰.가져오기, Modifier.weight(1f), 작게 = true)
            }
        }

        글("하젠하이데 ${BuildConfig.VERSION_NAME} · 시험판", Modifier.fillMaxWidth().padding(top = 18.dp), 크기값 = 크기.작게, 색 = c.옅음, 가운데 = true)
        Box(Modifier.height(100.dp))
    }
}
