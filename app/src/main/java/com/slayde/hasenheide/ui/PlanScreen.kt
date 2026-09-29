package com.slayde.hasenheide.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.slayde.hasenheide.data.무게글
import com.slayde.hasenheide.data.기대속도
import com.slayde.hasenheide.data.남은주
import com.slayde.hasenheide.data.몸조건
import com.slayde.hasenheide.data.수준
import com.slayde.hasenheide.data.수준판정
import com.slayde.hasenheide.data.속도
import com.slayde.hasenheide.data.주처방내기
import com.slayde.hasenheide.data.주차
import com.slayde.hasenheide.data.플랜
import com.slayde.hasenheide.data.플랜종목
import com.slayde.hasenheide.data.플랜표
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.높이
import com.slayde.hasenheide.ui.theme.모서리
import com.slayde.hasenheide.ui.theme.크기
import java.time.LocalDate

/**
 * 플랜 화면 (아래 탭 '플랜', 09-28) — 합의된 다섯 종목의 주차별 처방을 보여 준다.
 *
 * ── 무엇을 보여 주나 ──
 *  · 몸 조건 한 줄 (나이 · 성별 · 체중) — 수준 판정과 속도에 쓰인다
 *  · 종목마다: 지금 1RM → 목표 1RM · 수준 알약 · 남은 주와 도달 예상 날짜
 *  · 줄을 누르면 펼침: 시작 측정값 · 속도 5단계 · 이번 주 처방 · 측정일
 *
 * ── 정직하게 ──
 * 예상 기간이 20년을 넘으면 날짜를 지어내지 않고 그렇게 적는다 (09-28 OHP 798주).
 * 숫자는 모두 `Plan.kt` 의 `플랜표` 한곳에서 온다 — 고칠 때는 그 표만.
 */
@Composable
fun 플랜화면(상태: 앱상태) {
    val c = Local색.current
    val d = 상태.d
    val 몸 = d.몸
    var 펼친 by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize()) {
        제목글("플랜", Modifier.번호("플1").padding(start = 간격.보통, top = 16.dp, bottom = 8.dp))
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(간격.보통)) {

            // ── 몸 조건 ──
            카드(Modifier.번호("플2")) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    글("몸 조건", Modifier.weight(1f), 크기값 = 크기.크게, 굵기 = FontWeight.Bold)
                    글("수준 판정 · 속도에 쓰입니다", 크기값 = 크기.아주작게, 색 = c.옅음)
                }
                Box(Modifier.height(간격.좁게))
                Row(horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
                    작은수(Modifier.weight(1f), "나이", "${몸.나이}", "세",
                        { 상태.바꿈 { it.copy(몸 = it.몸.copy(나이 = (it.몸.나이 - 1).coerceIn(10, 90))) } },
                        { 상태.바꿈 { it.copy(몸 = it.몸.copy(나이 = (it.몸.나이 + 1).coerceIn(10, 90))) } })
                    작은수(Modifier.weight(1f), "체중", 무게글(몸.체중), "kg",
                        { 상태.바꿈 { it.copy(몸 = it.몸.copy(체중 = (it.몸.체중 - 1).coerceIn(30.0, 250.0))) } },
                        { 상태.바꿈 { it.copy(몸 = it.몸.copy(체중 = (it.몸.체중 + 1).coerceIn(30.0, 250.0))) } })
                    버튼(if (몸.남) "남" else "여", { 상태.바꿈 { it.copy(몸 = it.몸.copy(남 = !it.몸.남)) } }, Modifier.width(56.dp), 작게 = true)
                }
            }

            Box(Modifier.height(간격.보통))

            // ── 합의된 종목들 ──
            플랜표.종목들.forEach { 표 ->
                val 플랜값 = d.플랜들.firstOrNull { it.종목 == 표.이름 }
                종목칸(상태, 표, 플랜값, 몸, 펼친 == 표.이름) { 펼친 = if (펼친 == 표.이름) null else 표.이름 }
                Box(Modifier.height(간격.좁게))
            }

            Box(Modifier.height(간격.좁게))

            // ── 아직 합의하지 않은 종목 ──
            카드(Modifier.번호("플4")) {
                글("아직 플랜을 짤 수 없는 종목", 크기값 = 크기.조금작게, 굵기 = FontWeight.Bold, 색 = c.옅음)
                Box(Modifier.height(간격.아주좁게))
                글(플랜표.예정종목.joinToString(" · "), 크기값 = 크기.아주작게, 색 = c.옅음)
                Box(Modifier.height(간격.아주좁게))
                글("근거를 찾고 합의한 뒤에 하나씩 넣습니다 (업데이트 예정 ㉒)", 크기값 = 크기.아주작게, 색 = c.옅음)
            }

            Box(Modifier.height(간격.보통))
            글("이것은 가이드라인입니다. 그날 컨디션에 맞춰 조절하세요.", 크기값 = 크기.아주작게, 색 = c.옅음)
            Box(Modifier.height(높이.높게))
        }
    }
}

/** 종목 한 칸 */
@Composable
private fun 종목칸(상태: 앱상태, 표: 플랜종목, 플랜값: 플랜?, 몸: 몸조건, 펼침: Boolean, on누름: () -> Unit) {
    val c = Local색.current
    val 목표1RM = 표.목표.환산1RM

    if (플랜값 == null) {
        // 아직 시작 안 한 종목 — 측정값을 넣으면 플랜이 생긴다
        카드(Modifier.번호("플3")) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                글(표.이름, Modifier.weight(1f), 크기값 = 크기.본문, 굵기 = FontWeight.Bold)
                버튼("시작", {
                    상태.바꿈 {
                        it.copy(플랜들 = it.플랜들 + 플랜(표.이름, 20.0, 1, 속도.보통, LocalDate.now().toString()))
                    }
                }, 주요 = true, 작게 = true)
            }
            글("목표 ${무게글(표.목표.무게)}kg × ${표.목표.횟수}회 · 환산 1RM ${무게글(목표1RM)}kg", 크기값 = 크기.아주작게, 색 = c.옅음)
        }
        return
    }

    // 측정일 기록이 있으면 그 값으로 (09-29 고침)
    val 지금1RM = 플랜값.지금1RM
    val 수준값 = 수준판정(표, 지금1RM, 몸)
    val 주수 = 남은주(표, 지금1RM, 목표1RM, 플랜값.속도단계, 몸)
    // 주차는 만든 날로부터 날짜로 센다 — 앱을 며칠 안 켜도 맞는다 (09-29 고침)
    val 이번주 = 플랜값.주차(상태.오늘)
    val 처방 = 주처방내기(표, 플랜값, 이번주, 몸, 상태.d.설정.무게폭)

    카드(Modifier.번호("플3")) {
        Row(Modifier.눌림(on누름), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    글(표.이름, 크기값 = 크기.본문, 굵기 = FontWeight.Bold, 색 = c.강조)
                    Box(Modifier.width(간격.좁게))
                    수준알약(수준값)
                }
                글("1RM ${무게글(지금1RM)} → ${무게글(목표1RM)}kg", 크기값 = 크기.아주작게, 색 = c.옅음)
            }
            Column(horizontalAlignment = Alignment.End) {
                글(남은주글(주수), 크기값 = 크기.조금작게, 굵기 = FontWeight.Bold)
                글(도달글(주수), 크기값 = 크기.아주작게, 색 = c.옅음)
            }
            Box(Modifier.width(간격.아주좁게))
            아이콘버튼(if (펼침) 아이콘.아래 else 아이콘.오른쪽, if (펼침) "접기" else "펼치기", on누름, 칠함 = false)
        }

        if (펼침) {
            구분선(Modifier.padding(vertical = 간격.좁게))

            // 이번 주 처방
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(모서리.작게)).background(c.강조옅음).padding(간격.좁게),
                verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    글("${이번주}주차 처방", 크기값 = 크기.아주작게, 색 = c.옅음)
                    글("${무게글(처방.무게)}kg × ${처방.횟수}회 × ${처방.세트}세트", 크기값 = 크기.본문, 굵기 = FontWeight.Bold)
                }
                if (처방.측정일) 알약("측정일", c.강조)
            }

            Box(Modifier.height(간격.좁게))

            // 시작 측정값
            val 폭 = 상태.d.설정.무게폭
            글(if (플랜값.마지막측정 != null) "시작 측정값 (지금은 ${플랜값.마지막측정!!.주}주차 측정값으로 계산합니다)" else "시작 측정값 (이 값으로 계산합니다)", 크기값 = 크기.아주작게, 색 = c.옅음)
            Box(Modifier.height(간격.아주좁게))
            Row(horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
                작은수(Modifier.weight(1f), "무게", 무게글(플랜값.시작무게), "kg",
                    { 고치기(상태, 표.이름) { p -> p.copy(시작무게 = (p.시작무게 - 폭).coerceAtLeast(0.0)) } },
                    { 고치기(상태, 표.이름) { p -> p.copy(시작무게 = p.시작무게 + 폭) } })
                작은수(Modifier.weight(1f), "횟수", "${플랜값.시작횟수}", "회",
                    { 고치기(상태, 표.이름) { p -> p.copy(시작횟수 = (p.시작횟수 - 1).coerceAtLeast(1)) } },
                    { 고치기(상태, 표.이름) { p -> p.copy(시작횟수 = (p.시작횟수 + 1).coerceAtMost(30)) } })
            }

            Box(Modifier.height(간격.좁게))

            // 속도 5단계
            글("속도", 크기값 = 크기.아주작게, 색 = c.옅음)
            Box(Modifier.height(간격.아주좁게))
            칩줄(속도.entries.map { it.이름 }, 플랜값.속도단계.이름, { 고른 ->
                속도.entries.firstOrNull { it.이름 == 고른 }?.let { sp -> 고치기(상태, 표.이름) { p -> p.copy(속도단계 = sp) } }
            })

            Box(Modifier.height(간격.좁게))
            구분선()
            Box(Modifier.height(간격.좁게))

            // 근거 · 비고
            글("4주당 기대 ${"%.2f".format(기대속도(표, 수준값, 플랜값.속도단계, 몸))}% · 측정 주기 ${플랜표.측정주기[수준값]}주 · 합의 문서 ${표.문서}",
                크기값 = 크기.아주작게, 색 = c.옅음)
            if (표.비고.isNotEmpty()) {
                Box(Modifier.height(간격.아주좁게))
                글(표.비고, 크기값 = 크기.아주작게, 색 = c.옅음)
            }
            if (표.다른날.isNotEmpty()) {
                Box(Modifier.height(간격.아주좁게))
                글("${표.다른날.joinToString(" · ")} 와 다른 날에", 크기값 = 크기.아주작게, 색 = c.옅음)
            }

            Box(Modifier.height(간격.아주좁게))
            // 아직 안 만든 것을 된 것처럼 보이지 않게 적어 둔다 (09-29)
            글("처방을 루틴에 저절로 넣는 것 · 측정일 입력은 다음 판", 크기값 = 크기.아주작게, 색 = c.나쁨)

            Box(Modifier.height(간격.좁게))
            Row(horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
                버튼("플랜 지우기", {
                    상태.지우고알림("${표.이름} 플랜을 지웠습니다") { it.copy(플랜들 = it.플랜들.filter { p -> p.종목 != 표.이름 }) }
                }, Modifier.weight(1f), 작게 = true, 글색 = c.나쁨)
            }
        }
    }
}

private fun 고치기(상태: 앱상태, 종목이름: String, f: (플랜) -> 플랜) {
    상태.바꿈 { d -> d.copy(플랜들 = d.플랜들.map { if (it.종목 == 종목이름) f(it) else it }) }
}

/** 남은 주 — 너무 멀면 지어내지 않는다 */
private fun 남은주글(주: Int): String = when {
    주 <= 0 -> "달성"
    주 >= 플랜표.최대주 -> "20년 이상"
    주 >= 260 -> "%.1f년".format(주 / 52.0)
    else -> "${주}주"
}

private fun 도달글(주: Int): String = when {
    주 <= 0 -> "목표를 넘었습니다"
    주 >= 플랜표.최대주 -> "목표를 낮추거나 속도를 올리세요"
    else -> {
        val 날 = LocalDate.now().plusWeeks(주.toLong())
        "${날.year}년 ${날.monthValue}월쯤"
    }
}

@Composable
private fun 수준알약(s: 수준) {
    val c = Local색.current
    Box(
        Modifier.clip(RoundedCornerShape(모서리.아주작게)).border(1.dp, c.속선, RoundedCornerShape(모서리.아주작게))
            .padding(horizontal = 6.dp, vertical = 1.dp),
    ) { 글(s.이름, 크기값 = 크기.아주작게, 색 = c.강조) }
}

/** 작은 − 값 ＋ 칸 — 플랜 화면에서만 쓰는 단순한 것 (휠·자판 없이) */
@Composable
private fun 작은수(modifier: Modifier, 이름: String, 값: String, 단위: String, 빼기: () -> Unit, 더하기: () -> Unit) {
    val c = Local색.current
    Column(modifier) {
        글(이름, 크기값 = 크기.아주작게, 색 = c.옅음)
        Row(
            Modifier.fillMaxWidth().height(높이.낮게).clip(RoundedCornerShape(모서리.작게))
                .border(1.dp, c.속선, RoundedCornerShape(모서리.작게)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            아이콘버튼(아이콘.빼기, "줄이기", 빼기, Modifier.width(30.dp), 칠함 = false)
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Bottom) {
                글(값, 크기값 = 크기.본문, 굵기 = FontWeight.Bold)
                글(단위, 크기값 = 크기.아주작게, 색 = c.옅음)
            }
            아이콘버튼(아이콘.더하기, "늘리기", 더하기, Modifier.width(30.dp), 칠함 = false)
        }
    }
}
