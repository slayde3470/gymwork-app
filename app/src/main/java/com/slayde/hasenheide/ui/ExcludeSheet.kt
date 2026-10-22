package com.slayde.hasenheide.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.slayde.hasenheide.data.제외규칙
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.높이
import com.slayde.hasenheide.ui.theme.모서리
import com.slayde.hasenheide.ui.theme.선굵기
import com.slayde.hasenheide.ui.theme.크기

// ═════════════════ 자동 생성에서 뺄 날 (10-22 홍겸 님) ═════════════════
//
// 루틴 상세 왼쪽 아래 [자동 ◯] 옆 [제외] 를 누르면 열리는 시트. 요일 × 몇째 주 표 한 장 + 공휴일 스위치.
// 누른 날에는 이 루틴을 달력에 깔지 않고, 차례는 그대로 다음 날로 넘어간다 (일요일 제외 → 토 가슴, 일 비움, 월 등).
// 값은 전부 11 지침의 것만: 칸 높이 32 · 모서리 8 · 선 1 · 사이 4/8/12 · 글 11/13.

/** 표의 줄 — (이름, 종류, 주). 종류 0 = 매주, 1 = 달의 몇째 주 (주 1~4, -1 = 마지막) */
private val 표줄 = listOf(
    Triple("매주", 제외규칙.요일마다, 0), Triple("첫째", 제외규칙.몇째주, 1), Triple("둘째", 제외규칙.몇째주, 2),
    Triple("셋째", 제외규칙.몇째주, 3), Triple("넷째", 제외규칙.몇째주, 4), Triple("마지막", 제외규칙.몇째주, -1),
)
private val 요일이름 = listOf("월", "화", "수", "목", "금", "토", "일")
private val 이름칸폭 = 48.dp

/** [규칙들] 중 이 칸(종류 · 요일 · 주)에 해당하는 것이 있나 */
private fun List<제외규칙>.있음(종류: Int, 요일: Int, 주: Int) = any { it.종류 == 종류 && it.요일 == 요일 && (종류 == 제외규칙.요일마다 || it.주 == 주) }

/** 규칙 하나를 켜거나 끈다 */
internal fun List<제외규칙>.눌러바꿈(종류: Int, 요일: Int, 주: Int): List<제외규칙> =
    if (있음(종류, 요일, 주)) filterNot { it.종류 == 종류 && it.요일 == 요일 && (종류 == 제외규칙.요일마다 || it.주 == 주) }
    else this + 제외규칙(종류, 요일, if (종류 == 제외규칙.요일마다) 0 else 주)

/** 공휴일 규칙을 켜거나 끈다 */
internal fun List<제외규칙>.공휴일바꿈(켬: Boolean): List<제외규칙> =
    filterNot { it.종류 == 제외규칙.공휴일 }.let { if (켬) it + 제외규칙(제외규칙.공휴일) else it }

/** 떠 있는 [제외] 단추에 적을 글 — 없으면 "제외", 있으면 "제외 3" */
internal fun 제외단추글(규칙수: Int): String = if (규칙수 == 0) "제외" else "제외 $규칙수"

/**
 * 제외 규칙 시트. [바꿈] 은 규칙 목록 전체를 새로 넘긴다 (화면이 저장하고 예정을 다시 깐다).
 */
@Composable
internal fun 제외시트(루틴이름: String, 지금: List<제외규칙>, 바꿈: (List<제외규칙>) -> Unit, 닫기: () -> Unit) {
    val c = Local색.current
    val 칸모양 = RoundedCornerShape(모서리.작게)
    시트("$루틴이름 · 깔지 않을 날", 닫기) {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(간격.보통)) {
            글("누른 날에는 이 루틴을 깔지 않고, 차례는 그대로 다음 날로 넘어갑니다", 크기값 = 크기.작게, 색 = c.옅음, 줄 = 2)
            Column(verticalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                // 요일 머리
                Row(horizontalArrangement = Arrangement.spacedBy(간격.아주좁게), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(이름칸폭))
                    요일이름.forEach { 요 -> 글(요, Modifier.weight(1f), 크기값 = 크기.작게, 색 = c.옅음, 가운데 = true) }
                }
                표줄.forEach { (이름, 종류, 주) ->
                    Row(horizontalArrangement = Arrangement.spacedBy(간격.아주좁게), verticalAlignment = Alignment.CenterVertically) {
                        글(이름, Modifier.width(이름칸폭), 크기값 = 크기.버튼, 색 = c.글, 굵기 = FontWeight.Medium)
                        for (요 in 1..7) {
                            val 켬 = 지금.있음(종류, 요, 주)
                            Box(
                                Modifier.weight(1f).height(높이.낮게).clip(칸모양)
                                    .background(if (켬) c.강조 else c.면)
                                    .border(선굵기.보통, if (켬) c.강조 else c.속선, 칸모양)
                                    .눌림 { 바꿈(지금.눌러바꿈(종류, 요, 주)) },
                                contentAlignment = Alignment.Center,
                            ) { if (켬) Icon(아이콘.체크, null, Modifier.size(16.dp), tint = c.강조글) }
                        }
                    }
                }
            }
            val 휴 = 지금.any { it.종류 == 제외규칙.공휴일 }
            설정줄("공휴일", "대체공휴일 · 선거일 포함 (2026~2030년 표)") { 스위치(휴) { 바꿈(지금.공휴일바꿈(it)) } }
            if (지금.isNotEmpty()) 버튼("모두 지우기", { 바꿈(emptyList()) }, Modifier.fillMaxWidth(), 낮게 = true)
        }
    }
}
