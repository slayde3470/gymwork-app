package com.slayde.hasenheide.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.slayde.hasenheide.data.전환모드
import com.slayde.hasenheide.data.전환막힘
import com.slayde.hasenheide.data.종목전환
import com.slayde.hasenheide.data.줄전환모드
import com.slayde.hasenheide.data.플랜보조무게
import com.slayde.hasenheide.data.플랜전환
import com.slayde.hasenheide.data.플랜찾기
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.높이
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.slayde.hasenheide.data.기록방식
import com.slayde.hasenheide.data.무게글
import com.slayde.hasenheide.data.거리칸
import com.slayde.hasenheide.data.무게칸있음
import com.slayde.hasenheide.data.방식
import com.slayde.hasenheide.data.시간칸
import com.slayde.hasenheide.data.실제무게
import com.slayde.hasenheide.data.앱데이터
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.크기

/**
 * 기록 방식 1단계 화면 (10-12 · 문서/기록방식_설계.md) — 무게 칸 머리 글 + 칸 아래 작게 '= 69kg'.
 * 계산은 data/기록방식.kt 의 [실제무게] 하나만 쓴다 (여기서는 글로 바꿀 뿐).
 */

/**
 * 무게 칸 머리 글. 표의 줄이 모두 맨몸이면 '추가 kg' · 모두 어시스트면 '보조 kg', 그 밖(섞임 · 일반 · 플랜 줄뿐)은 [기본].
 * [방식들] 에는 플랜 줄을 넣지 않는다 (플랜 줄은 바꾸지 않는다)
 */
internal fun 무게머리글(방식들: Collection<기록방식>, 기본: String): String {
    if (방식들.isEmpty()) return 기본
    // 횟수만 · 시간 줄은 무게 칸이 없다 — 머리 글은 무게 칸이 있는 줄만 보고 정한다. 모두 없으면 머리도 비운다 (2단계)
    val 있는 = 방식들.filter { it.무게칸있음() }
    return when {
        있는.isEmpty() -> ""
        있는.all { it == 기록방식.맨몸 } -> "추가 kg"
        있는.all { it == 기록방식.어시스트 } -> "보조 kg"
        있는.all { it.거리칸() } -> "거리 km"   // 3단계 유산소: 무게 칸 자리가 거리
        else -> 기본
    }
}

/** 횟수 칸 머리 글 — 줄이 모두 시간(버티기)이면 '시간', 그 밖은 [기본] ('횟수' · '회') */
internal fun 횟수머리글(방식들: Collection<기록방식>, 기본: String = "횟수"): String =
    if (방식들.isNotEmpty() && 방식들.all { it.시간칸() }) "시간" else 기본

/**
 * 칸 아래 작은 글 '= 167kg' — 10-13 홍겸 님: **보이지 않는다.** 줄 높이가 달라져 줄이 안 맞았고, 입력하면 체중에 더하거나 빼서 기록하는 계산(실제무게)만 있으면 된다.
 * 부르는 곳이 많아 함수는 남기고 늘 null 을 돌려준다 (null 이면 칸만 그려져 높이가 변하지 않는다)
 */
@Suppress("UNUSED_PARAMETER")
internal fun 앱데이터.실제무게글(종id: String?, 이름: String, 플랜id: String?, 입력: Double): String? = null

/** 줄 하나의 방식 (플랜 줄은 null = 머리 글 계산에서 뺀다) */
internal fun 앱데이터.머리방식(종id: String?, 이름: String, 플랜id: String?): 기록방식? =
    if (플랜id != null) null else 방식(종id, 이름)

/** 무게 칸 + 그 아래 작은 글. [작은글] 이 null 이면 칸만 (높이가 변하지 않는다) */
@Composable
internal fun 무게칸아래글(작은글: String?, modifier: Modifier = Modifier, 칸: @Composable () -> Unit) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        칸()
        if (작은글 != null) 글(작은글, Modifier.fillMaxWidth(), 크기값 = 크기.작게, 색 = Local색.current.옅음, 가운데 = true)
    }
}

// ─────────────── ⇄ 방식 바꾸기 (10-19 홍겸 님) ───────────────

/**
 * ⇄ 한 줄 — 턱걸이 · 딥스 · 친업. [⇄ 일반] 을 누르면 일반 → 어시스트 → 중량 → 일반 (계산은 data/방식전환.kt).
 * [보조] 가 있으면(플랜 줄의 어시스트 · 중량) − 10kg ＋ 로 그 자리에서 고친다. [이름] 은 한 상자에 종목이 둘 이상일 때만 (슈퍼세트)
 */
@Composable
internal fun 방식전환줄(
    모드: 전환모드, 이름: String?, 보조: Double?, onClick: () -> Unit, on덜: () -> Unit, on더: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Local색.current
    Row(
        modifier.fillMaxWidth().height(높이.낮게),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.좁게),
    ) {
        if (이름 != null) 글(이름, Modifier.weight(1f, fill = false), 크기값 = 크기.작게, 색 = c.옅음)
        버튼(모드.name, onClick, Modifier.semantics { contentDescription = "${이름 ?: ""} 방식 바꾸기, 지금 ${모드.name}" }, 낮게 = true, 그림 = 아이콘.바꿈)
        if (보조 != null) {
            아이콘버튼(아이콘.빼기, "보조 무게 줄이기", on덜, 크기칸 = 높이.낮게)
            글("${if (모드 == 전환모드.어시스트) "보조" else "추가"} ${무게글(보조)}kg", 크기값 = 크기.버튼, 굵기 = FontWeight.Bold, 색 = c.글)
            아이콘버튼(아이콘.더하기, "보조 무게 늘리기", on더, 크기칸 = 높이.낮게)
        }
    }
}

/** 줄 하나의 ⇄ — 이 줄이 ⇄ 대상이 아니면 아무것도 그리지 않는다. 일반 줄은 종목 방식을, 플랜 줄은 플랜 보조모드를 바꾼다 */
@Composable
internal fun 전환행(상태: 앱상태, 종id: String?, 이름: String, 플랜id: String?, 이름보임: Boolean = false, modifier: Modifier = Modifier) {
    val d = 상태.d
    val 모드 = d.줄전환모드(종id, 이름, 플랜id) ?: return
    val 보조 = if (플랜id != null && 모드 != 전환모드.일반) d.플랜찾기(플랜id)?.보조무게 else null
    방식전환줄(
        모드, if (이름보임) 이름 else null, 보조,
        onClick = { 상태.방식바꾸기(종id, 이름, 플랜id) },
        on덜 = { 상태.보조무게바꾸기(플랜id, -1) }, on더 = { 상태.보조무게바꾸기(플랜id, 1) },
        modifier = modifier,
    )
}

/** ⇄ 를 눌렀다 — 운동 중 체크한 세트가 있으면 바꾸지 않고 띠로 알린다 (체크한 값의 뜻이 달라지므로) */
internal fun 앱상태.방식바꾸기(종id: String?, 이름: String, 플랜id: String?) {
    if (d.전환막힘(종id, 이름, 플랜id)) { 알림.토스트("체크한 세트가 있어 바꿀 수 없습니다"); return }
    발자취.적기("⇄ 방식 바꾸기 · $이름${if (플랜id != null) " (플랜)" else ""}")
    바꿈 { dd -> if (플랜id != null) dd.플랜전환(플랜id) else dd.종목전환(종id, 이름) }
}

internal fun 앱상태.보조무게바꾸기(플랜id: String?, 방향: Int) {
    if (플랜id == null) return
    if (d.플랜찾기(플랜id)?.let { d.전환막힘(null, it.이름, 플랜id) } == true) { 알림.토스트("체크한 세트가 있어 바꿀 수 없습니다"); return }
    발자취.적기("⇄ 보조 무게 ${if (방향 > 0) "＋" else "－"}")
    바꿈 { dd -> dd.플랜보조무게(플랜id, 방향) }
}
