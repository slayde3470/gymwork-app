package com.slayde.hasenheide.ui

import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.text.font.FontWeight
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
import com.slayde.hasenheide.data.무게폭목록
import com.slayde.hasenheide.data.기본휴식목록
import com.slayde.hasenheide.data.기본세트목록
import com.slayde.hasenheide.data.진동세기목록
import com.slayde.hasenheide.data.진동시간목록
import com.slayde.hasenheide.data.무게글
import com.slayde.hasenheide.data.분초
import com.slayde.hasenheide.data.설정값
import com.slayde.hasenheide.data.근육표
import com.slayde.hasenheide.data.체중기록잇기
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.크기

@Composable
fun 설정화면(상태: 앱상태, 폰: 폰기능) {
    val c = Local색.current
    val s = 상태.d.설정
    fun 고침(f: (설정값) -> 설정값) = 상태.바꿈 { it.copy(설정 = f(it.설정)) }

    // 10-05 (시안 v19 A ①): 맨 위 = 회색 띠 '설정' (바탕 선 색 · 글 색). 당겨서 새로고침은 띠 아래만 (v19 A ②)
    Column(Modifier.fillMaxSize()) {
    머리띠("설정", 회색 = true)
    당겨새로고침({ }, Modifier.weight(1f).fillMaxWidth()) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 간격.보통)) {
        // ── 신체 정보 — 플랜이 이 값으로 수준을 판정한다 (09-29, 20 문서) ──
        이름표("신체 정보", Modifier.padding(top = 간격.보통, bottom = 8.dp))
        카드(Modifier.번호("설0"), 안쪽 = 14.dp) {
            val 몸 = 상태.d.몸
            Row(horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
                Column(Modifier.weight(1f)) {
                    글("나이", 크기값 = 크기.아주작게, 색 = c.옅음)
                    입력칸(if (몸.나이 > 0) 몸.나이.toString() else "", { v ->
                        상태.바꿈 { d -> d.copy(몸 = d.몸.copy(나이 = v.trim().toIntOrNull()?.coerceIn(0, 99) ?: 0)) }
                    }, Modifier.fillMaxWidth(), 안내 = "예: 35", 숫자 = true)
                }
                Column(Modifier.weight(1f)) {
                    글("체중 (kg)", 크기값 = 크기.아주작게, 색 = c.옅음)
                    입력칸(if (몸.체중 > 0) 무게글(몸.체중) else "", { v ->
                        // 10-02: 바꿀 때마다 체중기록에 한 줄 (치는 동안은 2분 안에 한 줄로 묶는다 · 업적 1-47 · 1-48 · 2-20)
                        상태.바꿈 { d -> (v.trim().toDoubleOrNull()?.coerceIn(0.0, 400.0) ?: 0.0).let { kg -> d.copy(몸 = d.몸.copy(체중 = kg)).체중기록잇기(kg, System.currentTimeMillis()) } }
                    }, Modifier.fillMaxWidth(), 안내 = "예: 109", 숫자 = true)
                }
            }
            Box(Modifier.height(간격.좁게))
            설정줄("성별") {
                칩줄(listOf("남", "여"), if (몸.남) "남" else "여", { 고른 ->
                    상태.바꿈 { d -> d.copy(몸 = d.몸.copy(남 = 고른 == "남")) }
                })
            }
        }

        Box(Modifier.height(간격.보통))
        이름표("운동 진행", Modifier.padding(bottom = 8.dp))
        카드(Modifier.번호("설1"), 안쪽 = 14.dp) {
            설정줄("자동 진행", "휴식 끝나면 다음 세트로") { 스위치(s.자동진행) { v -> 고침 { it.copy(자동진행 = v) } } }
            구분선()
            설정줄("넘어가기 전 확인", "넘어가기 전에 한 번 묻기") { 스위치(s.넘어가기전확인) { v -> 고침 { it.copy(넘어가기전확인 = v) } } }
            구분선()
            설정줄("소리 · 진동", "휴식이 끝나면 알립니다") { 스위치(s.소리진동) { v -> 고침 { it.copy(소리진동 = v) } } }
            // 진동 세기 · 길이 (09-25 메모). 고르면 한 번 울려서 느낌을 바로 알 수 있게
            if (s.소리진동) {
                설정줄("진동 세기", "고르면 한 번 울립니다") {}
                칩줄(진동세기목록.map { it.second }, 진동세기목록.firstOrNull { it.first == s.진동세기 }?.second, { t ->
                    val v = 진동세기목록.first { it.second == t }.first; 고침 { it.copy(진동세기 = v) }; 폰.진동미리()
                })
                Box(Modifier.height(12.dp))
                설정줄("진동 길이", "휴식이 끝날 때 울리는 시간") {}
                칩줄(진동시간목록.map { 초글(it) }, 초글(s.진동시간), { t ->
                    val v = 진동시간목록.first { 초글(it) == t }; 고침 { it.copy(진동시간 = v) }; 폰.진동미리()
                })
                Box(Modifier.height(12.dp))
            }
            구분선()
            설정줄("운동 중 화면 켜 두기", "휴식 중에 화면이 꺼지지 않게") { 스위치(s.화면유지) { v -> 고침 { it.copy(화면유지 = v) } } }
            구분선()
            // 시험 기간용 — 칸마다 작은 번호 (09-26). 시험이 끝나면 설정째 뺀다
            설정줄("화면 번호 보기", "칸마다 작은 번호 · 시험 기간용") { 스위치(s.번호보기) { v -> 고침 { it.copy(번호보기 = v) } } }
        }

        이름표("기록", Modifier.padding(top = 18.dp, bottom = 8.dp))
        카드(Modifier.번호("설2"), 안쪽 = 14.dp) {
            설정줄("무게 조절 폭", "＋ − 한 번에 · 직접 입력은 0.1 단위") {}
            // 09-25 메모: 1.25 (화면엔 1.3 으로 보였다) 를 빼고 0.1 을 넣었다. 기본 1
            칩줄(무게폭목록.map { 무게글(it) }, 무게글(s.무게폭), { t -> 고침 { it.copy(무게폭 = t.toDouble()) } })
            Box(Modifier.height(12.dp)); 구분선()
            설정줄("기본 세트", "새 종목을 넣을 때") {}
            칩줄(기본세트목록.map { "$it" }, "${s.기본세트}", { t -> 고침 { it.copy(기본세트 = t.toInt()) } })   // 1~5 (09-25 메모)
            Box(Modifier.height(12.dp)); 구분선()
            설정줄("기본 휴식", "새 종목을 넣을 때") {}
            칩줄(기본휴식목록.map { 분초(it) }, 분초(s.기본휴식), { t ->   // 0:30 ~ 3:00 (09-25 메모)
                val (m, x) = t.split(":"); 고침 { it.copy(기본휴식 = m.toInt() * 60 + x.toInt()) }
            })
            // 향상도 기준 칩은 뺐다 (09-26: 향상도는 언제나 지난 기록 중 최고와 견준다 · 1RM 과 전체 볼륨)
        }

        // 볼륨 자동 올리기 (09-24 · 업데이트 예정 ⑲ — 써보면서 다듬는다)
        이름표("볼륨 자동 올리기", Modifier.padding(top = 18.dp, bottom = 8.dp))
        카드(Modifier.번호("설3"), 안쪽 = 14.dp) {
            설정줄("자동으로 올리기", "루틴을 끝내면 다음 목표가 오릅니다") { 스위치(s.볼륨켬) { v -> 고침 { it.copy(볼륨켬 = v) } } }
            if (s.볼륨켬) {
                구분선()
                설정줄("얼마나", "한 번에 올릴 양") {}
                // 10-01: 단위를 바꾸면 그 단위에 없는 값은 가운데 값으로 (kg 20 → % 20 이 되지 않게)
                칩줄(listOf("%", "kg"), s.볼륨방식, { t -> 고침 {
                    val 목록 = if (t == "%") 설정표.볼륨퍼센트 else 설정표.볼륨킬로
                    it.copy(볼륨방식 = t, 볼륨값 = if (it.볼륨값 in 목록) it.볼륨값 else 목록[목록.size / 2])
                } })
                Box(Modifier.height(8.dp))
                칩줄(
                    // 10-01: 전에는 % 와 kg 가 같은 칩(1 · 2.5 · 5)이었다 — kg 는 더 크게 올릴 수 있게
                    (if (s.볼륨방식 == "%") 설정표.볼륨퍼센트 else 설정표.볼륨킬로).map { 무게글(it) },
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

        // 운동 중 그림 (10-02 · 08 시안 3절 · 07 근육지도 4·5절)
        이름표("운동 중 그림", Modifier.padding(top = 18.dp, bottom = 8.dp))
        카드(Modifier.번호("설5"), 안쪽 = 14.dp) {
            설정줄("보기", "위쪽 그림 칸") {}
            칩줄(근육표.배너목록, s.배너, { t -> 고침 { it.copy(배너 = t) } })
            Box(Modifier.height(12.dp)); 구분선()
            설정줄("근육 회복 시간", "가장 빨갛던 근육이 원래 색으로 돌아오는 시간") {}
            칩줄(근육표.회복시간목록.map { 근육표.회복시간글(it) }, 근육표.회복시간글(s.회복시간), { t ->
                val v = 근육표.회복시간목록.first { 근육표.회복시간글(it) == t }; 고침 { it.copy(회복시간 = v) }
            })
            Box(Modifier.height(12.dp)); 구분선()
            설정줄("색") {}
            칩줄(근육표.색표목록.map { it.second }, 근육표.색표목록.firstOrNull { it.first == s.색표 }?.second, { t ->
                val v = 근육표.색표목록.first { it.second == t }.first; 고침 { it.copy(색표 = v) }
            })
        }

        // 10-08 홍겸 님: 운동 중 다른 앱 위 동그라미 — 유튜브 작은 창과 같이 쓰려면 '다른 앱 위에 표시' 권한이 필요하다
        이름표("운동 중 작은 동그라미", Modifier.padding(top = 18.dp, bottom = 8.dp))
        카드(안쪽 = 14.dp) {
            var 됨 by remember { mutableStateOf(폰.떠있기됨()) }
            val 생애 = androidx.compose.ui.platform.LocalLifecycleOwner.current
            DisposableEffect(생애) {
                val 봄 = androidx.lifecycle.LifecycleEventObserver { _, e -> if (e == androidx.lifecycle.Lifecycle.Event.ON_RESUME) 됨 = 폰.떠있기됨() }
                생애.lifecycle.addObserver(봄); onDispose { 생애.lifecycle.removeObserver(봄) }
            }
            글("운동 중에 앱을 나가면 다른 앱 위에 동그라미가 떠요", 크기값 = 크기.조금작게, 색 = c.흐림, 줄 = 2)
            글("유튜브 작은 창과 같이 쓸 수 있어요 · 누르면 운동 화면으로", 크기값 = 크기.작게, 색 = c.옅음, 줄 = 2)
            Box(Modifier.height(12.dp))
            if (됨) 글("켜져 있습니다", 크기값 = 크기.조금작게, 색 = c.좋음, 굵기 = FontWeight.Bold)
            else 버튼("다른 앱 위에 표시 허용하기", 폰.떠있기켜기, Modifier.fillMaxWidth(), 주요 = true, 작게 = true)
        }

        이름표("데이터", Modifier.padding(top = 18.dp, bottom = 8.dp))
        카드(Modifier.번호("설4"), 안쪽 = 14.dp) {
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
    }
}

/** 500 → "0.5초", 1000 → "1초" */
private fun 초글(ms: Int): String = if (ms % 1000 == 0) "${ms / 1000}초" else "${ms / 1000.0}초"


/** 설정 칩 값 — 한곳에 모아 둔다 (10-01) */
object 설정표 {
    val 볼륨퍼센트 = listOf(1.0, 2.5, 5.0)   // 그대로 (요청은 kg 만)
    val 볼륨킬로 = listOf(1.0, 2.5, 5.0, 10.0, 20.0)
}
