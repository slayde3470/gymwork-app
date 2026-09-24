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
        제목글("설정", Modifier.padding(top = 16.dp, bottom = 12.dp))

        이름표("운동 진행", Modifier.padding(bottom = 8.dp))
        카드(안쪽 = 14.dp) {
            설정줄("자동 진행", "휴식 끝나면 다음 세트로") { 스위치(s.자동진행) { v -> 고침 { it.copy(자동진행 = v) } } }
            구분선()
            설정줄("넘어가기 전 확인", "넘어가기 전에 한 번 묻기") { 스위치(s.넘어가기전확인) { v -> 고침 { it.copy(넘어가기전확인 = v) } } }
            구분선()
            설정줄("소리 · 진동", "휴식이 끝나면 알립니다") { 스위치(s.소리진동) { v -> 고침 { it.copy(소리진동 = v) } } }
            구분선()
            설정줄("운동 중 화면 켜 두기", "휴식 중에 화면이 꺼지지 않게") { 스위치(s.화면유지) { v -> 고침 { it.copy(화면유지 = v) } } }
        }

        이름표("기록", Modifier.padding(top = 18.dp, bottom = 8.dp))
        카드(안쪽 = 14.dp) {
            설정줄("무게 조절 폭", "＋ − 한 번에 · 직접 입력은 0.1 단위") {}
            칩줄(listOf(0.5, 1.0, 1.25, 2.5, 5.0).map { 무게글(it) }, 무게글(s.무게폭), { t -> 고침 { it.copy(무게폭 = t.toDouble()) } })
            Box(Modifier.height(12.dp)); 구분선()
            설정줄("기본 세트", "새 종목을 넣을 때") {}
            칩줄((1..10).map { "$it" }, "${s.기본세트}", { t -> 고침 { it.copy(기본세트 = t.toInt()) } })
            Box(Modifier.height(12.dp)); 구분선()
            설정줄("기본 휴식", "새 종목을 넣을 때") {}
            칩줄(listOf(60, 90, 120, 150, 180).map { 분초(it) }, 분초(s.기본휴식), { t ->
                val (m, x) = t.split(":"); 고침 { it.copy(기본휴식 = m.toInt() * 60 + x.toInt()) }
            })
            Box(Modifier.height(12.dp)); 구분선()
            설정줄("향상도 기준", "무엇과 견줄까요") {}
            칩줄(기준목록.map { it.짧 }, 기준목록.firstOrNull { it.k == s.기준 }?.짧, { t ->
                val k = 기준목록.first { it.짧 == t }.k; 고침 { it.copy(기준 = k) }
            })
        }

        // 볼륨 자동 올리기 (09-24 · 업데이트 예정 ⑲ — 써보면서 다듬는다)
        이름표("볼륨 자동 올리기", Modifier.padding(top = 18.dp, bottom = 8.dp))
        카드(안쪽 = 14.dp) {
            설정줄("자동으로 올리기", "루틴을 끝내면 다음 목표가 오릅니다") { 스위치(s.볼륨켬) { v -> 고침 { it.copy(볼륨켬 = v) } } }
            if (s.볼륨켬) {
                구분선()
                설정줄("얼마나", "한 번에 올릴 양") {}
                칩줄(listOf("%", "kg"), s.볼륨방식, { t -> 고침 { it.copy(볼륨방식 = t) } })
                Box(Modifier.height(8.dp))
                칩줄(
                    (if (s.볼륨방식 == "%") listOf(1.0, 2.5, 5.0) else listOf(1.0, 2.5, 5.0)).map { 무게글(it) },
                    무게글(s.볼륨값), { t -> 고침 { it.copy(볼륨값 = t.toDouble()) } },
                )
                Box(Modifier.height(12.dp)); 구분선()
                설정줄("언제", "성공 = 계획한 세트를 다 했을 때") {}
                칩줄(listOf("성공", "항상"), s.볼륨언제, { t -> 고침 { it.copy(볼륨언제 = t) } })
                Box(Modifier.height(12.dp)); 구분선()
                설정줄("어디에 붙일까", if (s.볼륨배분 == "횟수") "횟수를 올리다 ${s.횟수상한}회를 넘으면 무게로" else "무게를 올립니다") {}
                칩줄(listOf("횟수", "무게"), s.볼륨배분, { t -> 고침 { it.copy(볼륨배분 = t) } })
                if (s.볼륨배분 == "횟수") {
                    Box(Modifier.height(12.dp)); 구분선()
                    설정줄("횟수 상한", "여기를 넘으면 무게로 넘깁니다") {}
                    칩줄(listOf(8, 10, 12, 15, 20).map { "$it" }, "${s.횟수상한}", { t -> 고침 { it.copy(횟수상한 = t.toInt()) } })
                }
            }
        }

        이름표("데이터", Modifier.padding(top = 18.dp, bottom = 8.dp))
        카드(안쪽 = 14.dp) {
            글("기록은 이 폰 안에만 저장됩니다", 크기값 = 크기.조금작게, 색 = c.흐림)
            글("폰을 바꾸거나 앱을 지우기 전에 백업 파일로 내보내 두세요", 크기값 = 크기.작게, 색 = c.옅음)
            Box(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                버튼("백업 내보내기", 폰.내보내기, Modifier.weight(1f), 작게 = true)
                버튼("백업 가져오기", 폰.가져오기, Modifier.weight(1f), 작게 = true)
            }
        }

        글("하젠하이데 ${BuildConfig.VERSION_NAME} · 시험판", Modifier.fillMaxWidth().padding(top = 18.dp), 크기값 = 크기.작게, 색 = c.옅음, 가운데 = true)
        Box(Modifier.height(16.dp))   // 끝에 빈 공간을 두지 않는다 (09-21 메모)
    }
}
