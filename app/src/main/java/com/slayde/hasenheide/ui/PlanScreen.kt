package com.slayde.hasenheide.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import com.slayde.hasenheide.data.강도들
import com.slayde.hasenheide.data.플랜줄정리
import com.slayde.hasenheide.data.목표단위
import com.slayde.hasenheide.data.몸조건
import com.slayde.hasenheide.data.보정배수
import com.slayde.hasenheide.data.보조조절
import com.slayde.hasenheide.data.수준판정횟수
import com.slayde.hasenheide.data.속도출처글
import com.slayde.hasenheide.data.맨몸시작값
import com.slayde.hasenheide.data.자리옮김
import com.slayde.hasenheide.data.플랜이름바꾸기
import androidx.compose.ui.draw.drawBehind
import com.slayde.hasenheide.data.측정넣기
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.zIndex
import com.slayde.hasenheide.data.맨몸환산값
import com.slayde.hasenheide.data.유효부하
import com.slayde.hasenheide.data.무게글
import com.slayde.hasenheide.data.목표형식
import com.slayde.hasenheide.data.방식찾기
import com.slayde.hasenheide.data.빈도배수
import com.slayde.hasenheide.data.수준판정
import com.slayde.hasenheide.data.일RM
import com.slayde.hasenheide.data.지난주
import com.slayde.hasenheide.data.운동량글
import com.slayde.hasenheide.data.처방글
import com.slayde.hasenheide.data.콤마
import com.slayde.hasenheide.data.플랜
import com.slayde.hasenheide.data.플랜종목
import com.slayde.hasenheide.data.플랜표
import com.slayde.hasenheide.data.회계획
import com.slayde.hasenheide.data.회처방
import com.slayde.hasenheide.data.회표
import com.slayde.hasenheide.data.훈련방식
import com.slayde.hasenheide.data.훈련방식들
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.높이
import com.slayde.hasenheide.ui.theme.모서리
import com.slayde.hasenheide.ui.theme.크기
import kotlinx.coroutines.delay
import java.time.LocalDate

/**
 * 플랜 화면 (09-29 재설계 · 20 문서) — **플랜 종목을 하나 만드는 도구**다.
 *
 * 종목 고르기 → 나의 수행능력 → 운동 목표 → 훈련 방식 → 계산해 보기 → 결과 → 실행.
 * 실행하면 종목 탭에 **플랜 종목**이 생긴다. 이 화면에는 만들어 둔 플랜을 두지 않는다.
 *
 * 숫자는 모두 `Plan.kt` 의 `플랜표` 에서 온다 — 고칠 때는 그 표만.
 */
@Composable
fun 플랜화면(상태: 앱상태, 설정으로: () -> Unit, 종목으로: () -> Unit) {
    val c = Local색.current
    val d = 상태.d
    val 몸 = d.몸
    val 폭 = d.설정.무게폭

    var 고른 by remember { mutableStateOf<String?>(null) }
    var 안넣기 by remember { mutableStateOf(false) }
    var 현형식 by remember { mutableStateOf(목표형식.무게횟수) }
    var 현w by remember { mutableStateOf("") }
    var 현r by remember { mutableStateOf("") }
    var 현rm by remember { mutableStateOf("") }
    var 목형식 by remember { mutableStateOf(목표형식.RM) }
    var 목w by remember { mutableStateOf("") }
    var 목r by remember { mutableStateOf("") }
    var 목rm by remember { mutableStateOf("") }
    var 방식번호 by remember { mutableStateOf(1) }
    var 강도 by remember { mutableStateOf(1) }
    var 세트수 by remember { mutableStateOf(0) }       // 0 = 강도 프리셋
    var 직접횟수 by remember { mutableStateOf(0) }
    // ── 맨몸 3종 (21 문서) ──
    var 단위 by remember { mutableStateOf(목표단위.개수) }
    var 현개수 by remember { mutableStateOf("") }      // 지금 정자세로 몇 개
    var 목개수 by remember { mutableStateOf("") }      // 목표
    var 보조모드 by remember { mutableStateOf(0) }      // 0 정자세 · 1 어시스트 · 2 과부하
    var 보조무게 by remember { mutableStateOf("") }
    var 보조횟수 by remember { mutableStateOf("") }
    var 주당 by remember { mutableStateOf(플랜표.표준주당) }
    var 시트열림 by remember { mutableStateOf<String?>(null) }   // 방식 · 결과 · 이름
    var 계산중 by remember { mutableStateOf(false) }
    var 이름값 by remember { mutableStateOf("") }

    val 표 = 고른?.let { 플랜표.찾기(it) }
    val 수 = { s: String -> s.trim().toDoubleOrNull() ?: 0.0 }
    val 맨몸 = 표?.맨몸인가 == true

    // 종목을 바꾸면 단위를 그 종목 기본값으로 되돌린다
    LaunchedEffect(고른) { 표?.let { 단위 = it.기본단위; 보조모드 = 0 } }

    /**
     * 맨몸 시작값 — **늘 정자세 기준**이다 (21 문서 4절).
     * 어시스트로 적으면 에플리로 1RM 을 낸 뒤 정자세 횟수로 되돌린다:
     *   1RM = (체중−보조) × (1 + r/30) → 정자세 r = 30 × (1RM/체중 − 1)
     */
    // 10-01 고침: 어시스트로 넣은 값이 정자세 1회 미만(음수)이면 막혔다 → Plan.kt 맨몸시작값() (시험 있음)
    val 맨몸시작 = if (표 == null || !맨몸) null else 맨몸시작값(표, 몸, 보조모드, 수(현개수), 수(보조무게), 수(보조횟수))
    val 맨몸현재 = 맨몸시작 ?: 0.0
    val 맨몸날값 = if (표 == null || !맨몸 || 보조모드 == 0) 맨몸현재 else 맨몸환산값(표, 몸, 보조모드, 수(보조무게), 수(보조횟수))
    val 맨몸목표 = 수(목개수)
    val 현재1RM = when {
        표 == null -> 0.0
        안넣기 -> 플랜표.기본무게(표.장비, 몸.체중)
        현형식 == 목표형식.RM -> 수(현rm)
        else -> if (수(현w) > 0 && 수(현r) >= 1) 일RM(수(현w), 수(현r).toInt()) else 0.0
    }
    val 목표1RM = when {
        목형식 == 목표형식.RM -> 수(목rm)
        else -> if (수(목w) > 0 && 수(목r) >= 1) 일RM(수(목w), 수(목r).toInt()) else 0.0
    }
    val 보정 = 표?.let { 보정배수(d.향상기록들, it.이름) } ?: 1.0
    val 표들 = remember(표?.이름, 현재1RM, 목표1RM, 맨몸현재, 맨몸목표, 주당, 몸, 보정) {
        when {
            표 == null || !몸.찼나 -> emptyList()
            // 맨몸은 진행 변수가 횟수다. 1 미만은 1 로 본다 (곱셈으로 늘리므로 0 에서는 안 늘어난다)
            맨몸 -> if (맨몸시작 == null) emptyList() else 회표(표, 맨몸시작, 맨몸목표, 주당, 몸, 플랜표.최대회, 보정)
            else -> 회표(표, 현재1RM, 목표1RM, 주당, 몸, 플랜표.최대회, 보정)
        }
    }

    Column(Modifier.fillMaxSize()) {
        제목글("플랜", Modifier.padding(start = 간격.보통, top = 16.dp, bottom = 8.dp))
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(간격.보통)) {

            // ── 플1 · 종목 고르기 ──
            카드(Modifier.번호("플1")) {
                칩줄(플랜표.종목들.map { it.이름 }, 고른, { 이름 -> 고른 = if (고른 == 이름) null else 이름 })
            }

            if (표 == null) {
                Box(Modifier.height(간격.보통))
                글("아직 플랜을 짤 수 없는 종목: " + 플랜표.예정종목.joinToString(" · "), 크기값 = 크기.아주작게, 색 = c.옅음, 줄 = 4)
                Box(Modifier.height(높이.높게))
                return@Column
            }

            Box(Modifier.height(간격.좁게))

            // ── 신체 정보가 없으면 여기서 막는다 ──
            if (!몸.찼나) {
                val 빈 = listOfNotNull(if (몸.나이 > 0) null else "나이", if (몸.체중 > 0) null else "체중").joinToString(" · ")
                카드(Modifier.번호("플5")) {
                    글("신체 정보를 먼저 넣어 주세요", 크기값 = 크기.본문, 굵기 = FontWeight.Bold)
                    글("지금 $빈 이(가) 비어 있습니다", 크기값 = 크기.아주작게, 색 = c.옅음)
                    Box(Modifier.height(간격.좁게))
                    버튼("설정에서 입력하기", 설정으로, 주요 = true)
                }
                Box(Modifier.height(높이.높게))
                return@Column
            }

            // ── 플2 · 나의 수행능력 ──
            if (맨몸) 카드(Modifier.번호("플2")) {
                글("나의 수행능력 — 지금 정자세로 몇 개", 크기값 = 크기.아주작게, 색 = c.옅음)
                Box(Modifier.height(간격.아주좁게))
                // 턱걸이만 어시스트·과부하 (21 문서 4절 · 팔굽혀펴기는 넣지 않는다)
                if (표.보조옵션) {
                    Row(horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                        작은칩("정자세", 보조모드 == 0) { 보조모드 = 0 }
                        작은칩("어시스트", 보조모드 == 1) { 보조모드 = 1 }
                        작은칩("과부하", 보조모드 == 2) { 보조모드 = 2 }
                    }
                    Box(Modifier.height(간격.좁게))
                }
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
                        if (보조모드 == 0) {
                            글칸("정자세 최대 (${단위.단위})", 현개수, { 현개수 = it }, Modifier.weight(1f))
                        } else {
                            글칸(if (보조모드 == 1) "보조 무게 (kg)" else "추가 무게 (kg)", 보조무게, { 보조무게 = it }, Modifier.weight(1f))
                            글칸("횟수", 보조횟수, { 보조횟수 = it }, Modifier.weight(1f))
                        }
                    }
                    곁박스(
                        if (맨몸시작 != null) 수준판정횟수(표, 맨몸날값).이름 else "—",
                        if (맨몸시작 == null) "수준" else if (맨몸날값 < 1) "1${단위.단위} 미만" else "${무게글(맨몸날값)}${단위.단위}",
                        맨몸시작 != null,
                    )
                }
                if (보조모드 != 0 && 맨몸시작 != null && 맨몸날값 < 1) {
                    맞춤글("정자세 1${단위.단위} 미만 → 1${단위.단위}부터 시작", 색 = c.옅음)
                }
                if (보조모드 != 0 && 수(보조무게) > 0 && 수(보조횟수) >= 1) {
                    val 부하 = 표.유효부하(몸.체중, 보조조절(보조모드, 수(보조무게)))
                    글("유효 부하 ${무게글(부하)}kg · 환산 1RM ${무게글(부하 * (1 + 수(보조횟수) / 30.0))}kg",
                        크기값 = 크기.아주작게, 색 = c.흐림)
                }
                글("유효 부하 = 체중 × ${(표.맨몸비 * 100).toInt()}% — 볼륨을 이 값으로 셉니다", 크기값 = 크기.아주작게, 색 = c.옅음, 줄 = 2)
            } else 카드(Modifier.번호("플2")) {
                글("나의 수행능력", 크기값 = 크기.아주작게, 색 = c.옅음)
                Box(Modifier.height(간격.아주좁게))
                Row(horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                    작은칩("1RM", !안넣기 && 현형식 == 목표형식.RM) { 안넣기 = false; 현형식 = 목표형식.RM }
                    작은칩("무게 × 횟수", !안넣기 && 현형식 == 목표형식.무게횟수) { 안넣기 = false; 현형식 = 목표형식.무게횟수 }
                    작은칩("입력하지 않음", 안넣기) { 안넣기 = !안넣기 }
                }
                Box(Modifier.height(간격.좁게))
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
                        if (안넣기) {
                            글("${표.장비} 기준 ${무게글(플랜표.기본무게(표.장비, 몸.체중))}kg 으로 시작합니다",
                                Modifier.padding(bottom = 10.dp), 크기값 = 크기.아주작게, 색 = c.옅음, 줄 = 2)
                        } else if (현형식 == 목표형식.RM) {
                            글칸("1RM (kg)", 현rm, { 현rm = it }, Modifier.weight(1f))
                        } else {
                            글칸("무게 (kg)", 현w, { 현w = it }, Modifier.weight(1f))
                            글칸("횟수", 현r, { 현r = it }, Modifier.weight(1f))
                        }
                    }
                    곁박스(
                        if (현재1RM > 0) 수준판정(표, 현재1RM, 몸).이름 else "—",
                        if (현재1RM > 0) "${무게글(현재1RM)}kg" else "수준",
                        현재1RM > 0,
                    )
                }
            }

            Box(Modifier.height(간격.좁게))

            // ── 플3 · 운동 목표 ──
            if (맨몸) 카드(Modifier.번호("플3")) {
                글("운동 목표", 크기값 = 크기.아주작게, 색 = c.옅음)
                Box(Modifier.height(간격.아주좁게))
                Row(horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                    작은칩("개수", 단위 == 목표단위.개수) { 단위 = 목표단위.개수 }
                    작은칩("시간(초)", 단위 == 목표단위.시간) { 단위 = 목표단위.시간 }
                    작은칩("거리(m)", 단위 == 목표단위.거리) { 단위 = 목표단위.거리 }
                }
                Box(Modifier.height(간격.좁게))
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
                    글칸("목표 (${단위.단위})", 목개수, { 목개수 = it }, Modifier.weight(1f))
                    곁박스(
                        if (표들.isNotEmpty()) "${콤마(표들.last().주)}주" else "—",
                        "예상", 표들.isNotEmpty(),
                    )
                }
                글(
                    if (단위 == 목표단위.개수) "\"${목개수.ifBlank { "500" }}개\" 는 한 번에 연속 ${목개수.ifBlank { "500" }}개를 뜻합니다."
                    else "한 번에 이어서 ${목개수.ifBlank { "—" }}${단위.단위} 를 뜻합니다.",
                    크기값 = 크기.아주작게, 색 = c.옅음, 줄 = 2,
                )
                Box(Modifier.height(간격.아주좁게))
                글("향상 속도: ${속도출처글(d.향상기록들, 표.이름)}", 크기값 = 크기.아주작게, 색 = if (보정 == 1.0) c.옅음 else c.강조)
            } else 카드(Modifier.번호("플3")) {
                글("운동 목표", 크기값 = 크기.아주작게, 색 = c.옅음)
                Box(Modifier.height(간격.아주좁게))
                Row(horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                    작은칩("1RM", 목형식 == 목표형식.RM) { 목형식 = 목표형식.RM }
                    작은칩("무게 × 횟수", 목형식 == 목표형식.무게횟수) { 목형식 = 목표형식.무게횟수 }
                }
                Box(Modifier.height(간격.좁게))
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
                        if (목형식 == 목표형식.RM) {
                            글칸("목표 1RM (kg)", 목rm, { 목rm = it }, Modifier.weight(1f))
                        } else {
                            글칸("무게 (kg)", 목w, { 목w = it }, Modifier.weight(1f))
                            글칸("횟수", 목r, { 목r = it }, Modifier.weight(1f))
                        }
                    }
                    곁박스(
                        if (표들.isNotEmpty()) "${콤마(표들.last().주)}주" else "—",
                        "예상", 표들.isNotEmpty(),
                    )
                }
            }

            Box(Modifier.height(간격.좁게))

            // ── 플4 · 훈련 방식 ──
            카드(Modifier.번호("플4")) {
                글("훈련 방식", 크기값 = 크기.아주작게, 색 = c.옅음)
                고르기줄(방식찾기(방식번호).이름, 방식찾기(방식번호).수준글) { 시트열림 = "방식" }
            }

            Box(Modifier.height(간격.좁게))

            // ── 플5 · 신체 정보 ──
            카드(Modifier.번호("플5")) {
                글("신체 정보", 크기값 = 크기.아주작게, 색 = c.옅음)
                고르기줄("${몸.나이}세 · ${if (몸.남) "남" else "여"} · ${무게글(몸.체중)}kg", "설정", onClick = 설정으로)
            }

            Box(Modifier.height(간격.보통))
            val 막힘 = if (맨몸) when {
                맨몸시작 == null -> if (보조모드 == 0) "지금 할 수 있는 만큼을 넣어 주세요" else "무게와 횟수를 넣어 주세요"
                맨몸목표 <= 0 -> "목표를 넣어 주세요"
                맨몸목표 <= 맨몸현재 -> "목표가 지금 실력보다 낮습니다"
                else -> ""
            } else when {
                현재1RM <= 0 -> "수행능력을 넣어 주세요"
                목표1RM <= 0 -> "목표를 넣어 주세요"
                목표1RM <= 현재1RM -> "목표가 지금 실력보다 낮습니다"
                else -> ""
            }
            if (막힘.isNotEmpty()) {
                글(막힘, 크기값 = 크기.아주작게, 색 = c.나쁨)
                Box(Modifier.height(간격.아주좁게))
            }
            버튼("계산해 보기", { if (막힘.isEmpty()) 계산중 = true }, 주요 = true)

            Box(Modifier.height(높이.높게))
        }
    }

    // ── 계산 중 (1.7초) ──
    if (계산중) {
        계산중화면()
        LaunchedEffect(Unit) { delay(1700); 계산중 = false; 시트열림 = "결과" }
    }

    // ── 훈련 방식 고르기 ──
    if (시트열림 == "방식") {
        방식시트(
            방식번호, 강도, 세트수, 직접횟수, 맨몸,
            { 방식번호 = it }, { 강도 = it }, { 세트수 = it }, { 직접횟수 = it },
        ) { 시트열림 = null }
    }

    // ── 결과 ──
    if (시트열림 == "결과" && 표 != null) {
        결과시트(표, 표들,
            if (맨몸) 맨몸현재 else 현재1RM,
            if (맨몸) 맨몸목표 else 목표1RM,
            주당, { 주당 = it },
            방식번호, 강도, 세트수, 직접횟수, 단위, 보조모드, 수(보조무게), 몸, 폭,
            실행 = { 이름값 = 표.이름; 시트열림 = "이름" }, 닫기 = { 시트열림 = null })
    }

    // ── 이름 정하고 만들기 ──
    if (시트열림 == "이름" && 표 != null) {
        시트("플랜 종목 만들기", { 시트열림 = null }) {
            글("이름", 크기값 = 크기.아주작게, 색 = c.옅음)
            Box(Modifier.height(간격.아주좁게))
            입력칸(이름값, { 이름값 = it }, Modifier.fillMaxWidth(), 안내 = 표.이름)
            Box(Modifier.height(간격.좁게))
            글(
                if (맨몸) "슈퍼세트·드랍세트로 묶을 수 없고, 횟수는 플랜이 정합니다."
                else "슈퍼세트·드랍세트로 묶을 수 없고, 무게는 플랜이 정합니다.",
                크기값 = 크기.아주작게, 색 = c.옅음, 줄 = 2,
            )
            Box(Modifier.height(간격.보통))
            버튼("만들기", {
                val 새 = 플랜(
                    id = "p" + System.currentTimeMillis(),
                    이름 = 이름값.ifBlank { 표.이름 },
                    종목 = 표.이름,
                    시작1RM = if (맨몸) 0.0 else 현재1RM,
                    목표방식 = 목형식,
                    목표무게 = if (맨몸) 0.0 else if (목형식 == 목표형식.RM) 수(목rm) else 수(목w),
                    목표횟수 = if (목형식 == 목표형식.RM) 1 else 수(목r).toInt().coerceAtLeast(1),
                    주당 = 주당,
                    방식번호 = if (맨몸) 1 else 방식번호,
                    강도 = 강도,
                    세트수 = 세트수,
                    직접횟수 = 직접횟수,
                    만든날 = LocalDate.now().toString(),
                    // 맨몸 3종 (21 문서)
                    단위 = if (맨몸) 단위 else 목표단위.무게,
                    목표개수 = if (맨몸) 맨몸목표 else 0.0,
                    시작개수 = if (맨몸) 맨몸현재 else 0.0,
                    보조모드 = if (맨몸 && 표.보조옵션) 보조모드 else 0,
                    보조무게 = if (맨몸 && 표.보조옵션) 수(보조무게) else 0.0,
                )
                상태.바꿈 { it.copy(플랜들 = listOf(새) + it.플랜들) }
                시트열림 = null
                종목으로()
            }, 주요 = true)
        }
    }
}

// ═══════════════════ 부품 ═══════════════════

@Composable
private fun 작은칩(글자: String, 켜짐: Boolean, onClick: () -> Unit) {
    val c = Local색.current
    Box(
        Modifier.clip(RoundedCornerShape(모서리.작게))
            .then(if (켜짐) Modifier.background(c.강조) else Modifier.border(1.dp, c.속선, RoundedCornerShape(모서리.작게)))
            .눌림(onClick).padding(horizontal = 9.dp, vertical = 6.dp),
    ) { 글(글자, 크기값 = 크기.버튼, 굵기 = if (켜짐) FontWeight.Bold else FontWeight.Normal, 색 = if (켜짐) c.강조글 else c.흐림) }
}

/** 이름표 + 숫자 입력칸 — 비어 있을 때는 '—' 안내 */
@Composable
private fun 글칸(이름: String, 값: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val c = Local색.current
    Column(modifier) {
        글(이름, 크기값 = 크기.아주작게, 색 = c.옅음)
        입력칸(값, onChange, Modifier.fillMaxWidth(), 안내 = "—", 숫자 = true)
    }
}

/** 입력칸 오른쪽에 붙는 판정·예상 박스 */
@Composable
private fun 곁박스(큰: String, 작은: String, 참: Boolean) {
    val c = Local색.current
    Column(
        Modifier.width(88.dp).height(높이.높게).clip(RoundedCornerShape(모서리.작게))
            .background(if (참) c.강조옅음 else c.면2)
            .border(1.dp, if (참) c.강조 else c.속선, RoundedCornerShape(모서리.작게)),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        글(큰, 크기값 = 크기.조금작게, 굵기 = FontWeight.Bold, 색 = if (참) c.강조 else c.옅음)
        글(작은, 크기값 = 크기.아주작게, 색 = c.옅음)
    }
}

/** 계산 중 — 파란 화면에 바벨이 좌우로 (홍겸 님 09-29) */
@Composable
private fun 계산중화면() {
    val c = Local색.current
    var 오른쪽 by remember { mutableStateOf(false) }
    val x by animateFloatAsState(if (오른쪽) 1f else -1f, tween(900), label = "봉")
    LaunchedEffect(Unit) { while (true) { 오른쪽 = !오른쪽; delay(900) } }
    Box(Modifier.fillMaxSize().background(c.강조), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(Modifier.width(140.dp).height(46.dp)) {
                val w = size.width; val h = size.height; val 밀기 = x * w * 0.13f
                fun 막대(cx: Float, 높이: Float, 너비: Float) =
                    drawRect(Color.White, Offset(cx + 밀기, (h - 높이) / 2f), Size(너비, 높이))
                막대(w * 0.06f, h * 0.55f, w * 0.06f)
                막대(w * 0.16f, h * 0.90f, w * 0.08f)
                막대(w * 0.25f, h * 0.14f, w * 0.50f)
                막대(w * 0.76f, h * 0.90f, w * 0.08f)
                막대(w * 0.88f, h * 0.55f, w * 0.06f)
            }
            Box(Modifier.height(간격.보통))
            글("계산 중…", 크기값 = 크기.크게, 굵기 = FontWeight.Bold, 색 = c.강조글)
        }
    }
}

// ═══════════════════ 훈련 방식 시트 ═══════════════════

/**
 * 방식 목록 — 참고 이미지 배치를 중심색으로. 열 줄이 한 화면에 들어간다.
 * 5×5 를 고르면 그 줄이 **좌우로 갈라지며** 오른쪽에 매드카우가 나타난다 (홍겸 님 09-29).
 */
@Composable
private fun 방식시트(
    방식번호: Int, 강도: Int, 세트수: Int, 직접횟수: Int, 맨몸: Boolean,
    on방식: (Int) -> Unit, on강도: (Int) -> Unit, on세트수: (Int) -> Unit, on직접횟수: (Int) -> Unit,
    닫기: () -> Unit,
) {
    val c = Local색.current
    var 펼친 by remember { mutableStateOf<Int?>(null) }
    val 딸림방식 = 훈련방식들.firstOrNull { it.딸림임 }
    // 맨몸 3종은 1번(꾸준히 늘리기) 하나만 쓴다 (21 문서 0절 · 홍겸 님 09-30)
    val 보일것 = 훈련방식들.filter { !it.딸림임 && (!맨몸 || it.번호 == 1) }

    시트("훈련 방식", 닫기) {
        보일것.forEach { m ->
            val 갈라짐 = !맨몸 && m.딸림 != null && (방식번호 == m.번호 || 방식번호 == m.딸림)
            if (갈라짐 && 딸림방식 != null) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    방식띠(m, 방식번호 == m.번호, Modifier.weight(1f), 좁게 = true) {
                        on방식(m.번호); 펼친 = if (펼친 == m.번호) null else m.번호
                    }
                    방식띠(딸림방식, 방식번호 == 딸림방식.번호, Modifier.weight(1f), 좁게 = true, 새것 = true) {
                        on방식(딸림방식.번호); 펼친 = if (펼친 == 딸림방식.번호) null else 딸림방식.번호
                    }
                }
            } else {
                방식띠(m, 방식번호 == m.번호, Modifier.fillMaxWidth()) {
                    on방식(m.번호); 펼친 = if (펼친 == m.번호) null else m.번호
                }
            }
            // ★ '이 방식으로' 버튼은 **펼친 줄 안에** 있다 (홍겸 님 09-30 — 맨 밑에 두니 스크롤이 생겼다)
            if (펼친 == m.번호) 방식속(m, 강도, on강도, 세트수, 직접횟수, on세트수, on직접횟수, 맨몸, 닫기)
            if (갈라짐 && 딸림방식 != null && 펼친 == 딸림방식.번호)
                방식속(딸림방식, 강도, on강도, 세트수, 직접횟수, on세트수, on직접횟수, 맨몸, 닫기)
            Box(Modifier.height(2.dp))
        }
        // 10-01: 맨 아래 설명 글을 지웠다 (홍겸 님 — "맨아래 설명좀 없애라니까")
    }
}

/** 방식 한 줄 — 중심색 밴드 + 오른쪽으로 흐르는 실루엣 */
@Composable
private fun 방식띠(m: 훈련방식, 골름: Boolean, modifier: Modifier, 좁게: Boolean = false, 새것: Boolean = false, onClick: () -> Unit) {
    val c = Local색.current
    val 나타남 = 드러남값(m.번호)
    Box(
        modifier
            .heightIn(min = 40.dp)   // 10-01: 58 → 40 — 열 줄이 스크롤 없이 한 화면에
            .clip(RoundedCornerShape(모서리.작게))
            .background(Brush.horizontalGradient(listOf(c.강조, c.강조.copy(alpha = 0.82f))))
            // ★ 고른 줄 표시 (홍겸 님 09-30 — 흰 테두리는 밴드 끝에서 바탕과 섞여 보이지 않았다)
            //  · 밴드 **안쪽**으로 3dp 들여서 두르므로 고리 양쪽이 모두 중심색이다 → 반드시 보인다
            //  · 색은 `강조글` — 밝은 화면에서는 흰색, 어두운 화면에서는 짙은 남색으로 뒤집힌다
            .then(if (골름) Modifier.padding(3.dp).border(2.5.dp, c.강조글, RoundedCornerShape(모서리.작게)) else Modifier)
            .눌림(onClick)
            .then(if (새것) Modifier.드러남(나타남) else Modifier),
    ) {
        실루엣(m.번호, Modifier.matchParentSize())
        Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                글(m.이름, Modifier.weight(1f, fill = false), 크기값 = 크기.본문,
                    굵기 = FontWeight.Bold, 색 = Color.White)
                Box(Modifier.width(간격.아주좁게))
                Box(
                    Modifier.clip(RoundedCornerShape(모서리.아주작게))
                        .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(모서리.아주작게))
                        .padding(horizontal = 4.dp),
                ) { 글(m.수준글, 크기값 = 크기.아주작게, 색 = Color.White.copy(alpha = 0.8f)) }
            }
            // 10-01: 특징 줄은 펼쳤을 때만 (한 줄로 접어 열 줄이 한 화면에 들어가게)
        }
    }
}

/** 방식 상세 — 글씨가 오른쪽 5dp 에서 제자리로 밀려들며 나타난다 */
@Composable
private fun 방식속(
    m: 훈련방식, 강도: Int, on강도: (Int) -> Unit,
    세트수: Int, 직접횟수: Int, on세트수: (Int) -> Unit, on직접횟수: (Int) -> Unit,
    맨몸: Boolean, 이걸로: () -> Unit,
) {
    val c = Local색.current
    val p = 드러남값(m.번호 to "속")
    val 프리셋 = 강도들.getOrElse(강도) { 강도들[1] }
    val 지금세트 = if (세트수 > 0) 세트수 else 프리셋.세트
    val 지금횟수 = if (직접횟수 > 0) 직접횟수 else 프리셋.횟수
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(모서리.작게)).background(c.면2)
            .padding(10.dp).드러남(p),
    ) {
        // 10-01: 긴 설명 문단 대신 한 줄 — 펼쳐도 한 화면 안에 (설명 원문은 Plan.kt 훈련방식.설명 에 그대로 있다)
        글(m.특징, 크기값 = 크기.조금작게, 굵기 = FontWeight.Bold, 색 = c.글)
        Box(Modifier.height(간격.아주좁게))
        // ★ 세트와 횟수를 떼어냈다 (홍겸 님 09-30)
        //  · 강도 세 칩은 값을 **채워 주는 프리셋**일 뿐이다. 누른 뒤에도 자유롭게 고칠 수 있다
        //  · **횟수에 상한을 두지 않는다** — 4×10 과 5×8 은 총 40회로 같다
        if (m.강도단계 && !맨몸) {
            글("강도 — 눌러서 값을 채웁니다", 크기값 = 크기.아주작게, 색 = c.옅음)
            Box(Modifier.height(간격.아주좁게))
            Row(horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                강도들.forEachIndexed { i, s ->
                    작은칩("${s.이름} ${s.세트}×${s.횟수}", 강도 == i && 세트수 == 0 && 직접횟수 == 0) {
                        on강도(i); on세트수(0); on직접횟수(0)
                    }
                }
            }
            Box(Modifier.height(간격.좁게))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
                수칸("세트", 지금세트, 1, 20) { on세트수(it) }
                수칸("횟수", 지금횟수, 1, 200) { on직접횟수(it) }
                Column(Modifier.weight(1f)) {
                    글("총 반복", 크기값 = 크기.아주작게, 색 = c.옅음)
                    글("${지금세트 * 지금횟수}회", 크기값 = 크기.본문, 굵기 = FontWeight.Bold, 색 = c.강조)
                }
            }
            Box(Modifier.height(간격.좁게))
        }
        if (맨몸) {
            글("맨몸 처방", 크기값 = 크기.아주작게, 색 = c.옅음)
            글("보통 날 — 그 회차 목표의 ${(플랜표.맨몸보통비 * 100).toInt()}% × ${플랜표.맨몸세트}세트 · 휴식 ${플랜표.맨몸휴식 / 60}분",
                크기값 = 크기.아주작게, 색 = c.흐림, 줄 = 2)
            글("측정일 — 1세트 최대", 크기값 = 크기.아주작게, 색 = c.흐림)
            Box(Modifier.height(간격.좁게))
        }
        맞춤글("주 ${m.주당글}회 · 근거 ${m.근거.joinToString(" · ")}", 색 = c.흐림)
        Box(Modifier.height(간격.좁게))
        버튼("이 방식으로", 이걸로, Modifier.fillMaxWidth(), 주요 = true)
    }
}

/** − 값 ＋ 한 줄 — 상한은 넉넉하게만 둔다 (자판을 띄우지 않는다) */
@Composable
private fun 수칸(이름: String, 값: Int, 아래: Int, 위: Int, on바꿈: (Int) -> Unit) {
    val c = Local색.current
    Column {
        글(이름, 크기값 = 크기.아주작게, 색 = c.옅음)
        Box(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(높이.아주낮게).clip(RoundedCornerShape(모서리.아주작게)).background(c.면)
                    .border(1.dp, c.속선, RoundedCornerShape(모서리.아주작게))
                    .눌림 { on바꿈((값 - 1).coerceAtLeast(아래)) },
                contentAlignment = Alignment.Center,
            ) { 글("−", 크기값 = 크기.본문, 굵기 = FontWeight.Bold) }
            Box(Modifier.width(4.dp).height(1.dp))
            글("$값", Modifier.width(34.dp), 크기값 = 크기.본문, 굵기 = FontWeight.Bold)
            Box(
                Modifier.size(높이.아주낮게).clip(RoundedCornerShape(모서리.아주작게)).background(c.면)
                    .border(1.dp, c.속선, RoundedCornerShape(모서리.아주작게))
                    .눌림 { on바꿈((값 + 1).coerceAtMost(위)) },
                contentAlignment = Alignment.Center,
            ) { 글("＋", 크기값 = 크기.본문, 굵기 = FontWeight.Bold) }
        }
    }
}

@Composable
private fun 줄값(이름: String, 값: String) {
    val c = Local색.current
    글(이름, 크기값 = 크기.아주작게, 색 = c.옅음)
    글(값, 크기값 = 크기.아주작게, 색 = c.흐림, 줄 = 6)
    Box(Modifier.height(간격.아주좁게))
}

/** 방식마다 다른 실루엣 — 사진은 만들 수 없어 도형으로 그렸다 (나중에 바꿀 자리) */
@Composable
private fun 실루엣(번호: Int, modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val 흰 = Color.White.copy(alpha = 0.16f)
        val 왼 = w * 0.62f
        fun 네모(x: Float, y: Float, bw: Float, bh: Float) =
            drawRect(흰, Offset(왼 + x, y), Size(bw, bh))
        fun 동그라미(x: Float, y: Float, r: Float) = drawCircle(흰, r, Offset(왼 + x, y))
        val u = w * 0.030f
        when (번호 % 5) {
            0 -> { // 원판 셋
                동그라미(u * 3, h * 0.55f, u * 3.2f); 동그라미(u * 8, h * 0.62f, u * 2.4f); 동그라미(u * 11.6f, h * 0.70f, u * 1.7f)
            }
            1 -> { // 바벨 (가로)
                네모(u * 1, h * 0.46f, u * 11, u * 0.9f)
                네모(u * 0.4f, h * 0.30f, u * 1.2f, h * 0.38f); 네모(u * 11.2f, h * 0.30f, u * 1.2f, h * 0.38f)
            }
            2 -> { // 랙
                네모(u * 2, h * 0.10f, u * 1.1f, h * 0.80f); 네모(u * 10, h * 0.10f, u * 1.1f, h * 0.80f)
                네모(u * 1, h * 0.36f, u * 11, u * 0.8f)
            }
            3 -> { // 계단
                네모(u * 1, h * 0.70f, u * 3, h * 0.22f); 네모(u * 4.4f, h * 0.52f, u * 3, h * 0.40f)
                네모(u * 7.8f, h * 0.32f, u * 3, h * 0.60f)
            }
            else -> { // 덤벨
                네모(u * 3, h * 0.46f, u * 7, u * 1.0f)
                네모(u * 1.4f, h * 0.28f, u * 1.8f, h * 0.42f); 네모(u * 9.8f, h * 0.28f, u * 1.8f, h * 0.42f)
            }
        }
    }
}

// ═══════════════════ 결과 시트 ═══════════════════

@Composable
private fun 결과시트(
    표: 플랜종목, 표들: List<회계획>, 현재값: Double, 목표값: Double,
    주당: Int, on주당: (Int) -> Unit, 방식번호: Int, 강도: Int,
    세트수: Int, 직접횟수: Int, 단위: 목표단위, 보조모드: Int, 보조무게: Double, 몸: 몸조건, 폭: Double,
    실행: () -> Unit, 닫기: () -> Unit,
) {
    val c = Local색.current
    var 열린블록 by remember { mutableStateOf<Int?>(null) }
    val 맨몸 = 표.맨몸인가
    /** 그 회차 값에 붙는 단위 글 — 무게 종목은 kg, 맨몸은 개·초·m */
    fun 값글(v: Double): String = 무게글(v) + (if (맨몸) 단위.단위 else "kg")
    val 임시플랜 = remember(방식번호, 강도, 세트수, 직접횟수, 보조모드, 보조무게) {
        플랜(
            id = "", 이름 = "", 종목 = 표.이름, 시작1RM = if (맨몸) 0.0 else 현재값,
            방식번호 = 방식번호, 강도 = 강도, 세트수 = 세트수, 직접횟수 = 직접횟수,
            단위 = if (맨몸) 단위 else 목표단위.무게,
            시작개수 = if (맨몸) 현재값 else 0.0, 목표개수 = if (맨몸) 목표값 else 0.0,
            보조모드 = 보조모드, 보조무게 = 보조무게,
        )
    }

    시트(표.이름, 닫기) {
        if (표들.isEmpty()) {
            글("계산할 수 없습니다. 수행능력과 목표를 확인해 주세요.", 크기값 = 크기.조금작게, 색 = c.나쁨)
            return@시트
        }
        val 끝날 = LocalDate.now().plusWeeks(표들.last().주.toLong())

        // 주당 횟수 — 일곱 칸이 한 줄에
        글("주당 횟수", 크기값 = 크기.아주작게, 색 = c.옅음)
        Box(Modifier.height(간격.아주좁게))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            (1..7).forEach { f ->
                Box(
                    Modifier.weight(1f).height(32.dp).clip(RoundedCornerShape(모서리.작게))
                        .then(if (주당 == f) Modifier.background(c.강조) else Modifier.border(1.dp, c.속선, RoundedCornerShape(모서리.작게)))
                        .눌림 { on주당(f); 열린블록 = null },
                    contentAlignment = Alignment.Center,
                ) { 글("${f}회", 크기값 = 크기.아주작게, 굵기 = if (주당 == f) FontWeight.Bold else FontWeight.Normal, 색 = if (주당 == f) c.강조글 else c.흐림) }
            }
        }

        val 경고글 = 빈도경고(주당, 표)
        if (경고글.isNotEmpty()) {
            Box(Modifier.height(간격.좁게))
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(모서리.작게))
                    .background(c.면2).border(1.dp, c.속선, RoundedCornerShape(모서리.작게)).padding(9.dp),
            ) { 글(경고글, 크기값 = 크기.아주작게, 색 = c.흐림, 줄 = 12) }
        }

        Box(Modifier.height(간격.좁게))

        // 요약
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(모서리.작게)).background(c.강조옅음)
                .border(2.dp, c.강조, RoundedCornerShape(모서리.작게)).padding(10.dp),
        ) {
            요약줄("목표", 값글(목표값))
            요약줄("예상 도달 시간", "${콤마(표들.last().주)}주")
            글("${끝날.year}년 ${끝날.monthValue}월쯤 · ${"%.1f".format(표들.last().주 / 52.0)}년", 크기값 = 크기.아주작게, 색 = c.옅음)
            요약줄("필요 운동 횟수", "약 ${콤마(표들.size)}회")
        }

        Box(Modifier.height(간격.좁게))
        글("회차 — 측정일로 묶었습니다. 줄을 누르면 그 안이 펼쳐집니다", 크기값 = 크기.아주작게, 색 = c.옅음, 줄 = 2)
        Box(Modifier.height(간격.아주좁게))

        // 측정일을 경계로 블록
        val 블록들 = remember(표들) {
            val out = ArrayList<List<회계획>>(); var 시작 = 0
            표들.forEachIndexed { i, x -> if (x.측정일 || i == 표들.lastIndex) { out.add(표들.subList(시작, i + 1)); 시작 = i + 1 } }
            out
        }
        블록들.forEachIndexed { i, b ->
            val 첫 = b.first(); val 끝 = b.last(); val 열림 = 열린블록 == i
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(모서리.아주작게))
                    .background(if (끝.측정일) c.강조옅음 else c.면2)
                    .눌림 { 열린블록 = if (열림) null else i }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                글("${첫.회}~${끝.회}회", 크기값 = 크기.아주작게, 굵기 = if (끝.측정일) FontWeight.Bold else FontWeight.Normal)
                Box(Modifier.width(간격.아주좁게))
                글("${첫.주}~${끝.주}주", 크기값 = 크기.아주작게, 색 = c.옅음)
                Box(Modifier.weight(1f))
                글("${무게글(첫.목표값)} → ${값글(끝.목표값)}", 크기값 = 크기.아주작게)
                Box(Modifier.width(간격.아주좁게))
                글(끝.수준값.이름, 크기값 = 크기.아주작게, 색 = c.강조)
            }
            if (열림) {
                b.forEach { x ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        글("${x.회}회", Modifier.width(40.dp), 크기값 = 크기.아주작게, 색 = c.옅음)
                        글(값글(x.목표값), Modifier.width(62.dp), 크기값 = 크기.아주작게)
                        글(
                            (if (x.측정일) "측정 + " else "") +
                                처방글(회처방(임시플랜, x.목표값, 폭, 몸, x.측정일, x.주)),
                            Modifier.weight(1f), 크기값 = 크기.아주작게, 색 = c.흐림,
                        )
                    }
                }
            }
            Box(Modifier.height(1.dp))
        }

        Box(Modifier.height(간격.좁게))
        글(
            if (맨몸) "보통 날은 그 회차 목표의 ${(플랜표.맨몸보통비 * 100).toInt()}% × ${플랜표.맨몸세트}세트, 측정일은 1세트 최대입니다."
            else "처방은 ${방식찾기(방식번호).이름} 기준입니다. 측정일에도 본 운동을 그대로 합니다.",
            크기값 = 크기.아주작게, 색 = c.옅음, 줄 = 2,
        )
        Box(Modifier.height(간격.아주좁게))
        글("가이드라인입니다. 성공·실패에 따라 목표까지의 회차가 달라집니다.", 크기값 = 크기.아주작게, 색 = c.나쁨, 줄 = 2)

        Box(Modifier.height(간격.보통))
        버튼("이 플랜으로 실행", 실행, 주요 = true)
    }
}

@Composable
private fun 요약줄(이름: String, 값: String) {
    val c = Local색.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        글(이름, Modifier.weight(1f), 크기값 = 크기.조금작게, 색 = c.흐림)
        글(값, 크기값 = 크기.크게, 굵기 = FontWeight.Bold)
    }
}

/**
 * 주당 횟수 경고 (홍겸 님 09-29 문안 · 20 문서 A-4).
 * %는 부위별 빈도 배수에서 바로 계산한다 — 상체와 하체가 다르다.
 */
fun 빈도경고(f: Int, 표: 플랜종목): String {
    fun pp(x: Int): Int = ((빈도배수(x, 표.부위값) - 빈도배수(x - 1, 표.부위값)) * 100).toInt()
    return when (f) {
        1 -> "주 1회도 운동 효과가 분명히 있지만, 향상 속도가 절반 정도로 느려질 수 있습니다."
        3 -> "주 3회는 흔히 권장되는 범위이지만, 휴식과 컨디션 조절에 능숙한 경우에만 권장합니다."
        4 -> "운동 횟수를 늘리면 근력은 좋아지지만 얻을 수 있는 효율이 줄어듭니다. 운동 횟수를 주 4회로 늘려 얻는 추가적인 이득은 +${pp(4)}% 정도입니다. 휴식과 컨디션 조절에 유의해야 합니다."
        5 -> "운동 횟수를 주 5회로 늘려 얻는 추가적인 이득이 +${pp(5)}% 정도로 현저히 줄어듭니다. 휴식과 컨디션 조절이 매우 어렵습니다."
        6 -> "운동 횟수를 주 6회로 늘려 얻는 추가적인 이득이 단 +${pp(6)}% 이며, 회복이 따라오지 못하면 지속적인 피로와 근육통이 나타날 수 있습니다."
        7 -> "운동 횟수를 주 7회로 늘려 얻는 추가적인 이득은 단 +${pp(7)}% 뿐입니다. 쉬는 날이 하루도 없다는 뜻입니다. 이 정도를 실제로 지켜 낸 사례는 연구에서도 드뭅니다. 회복이 따라오지 못하면 지속적인 피로와 근육통이 먼저 나타나고, 최대근력은 가장 나중에 떨어집니다. 정말 주 7회까지 필요한지 한 번 더 생각해 보세요."
        else -> ""
    }
}

// ═══════════════════ 종목 탭에 보이는 플랜 종목 ═══════════════════

/**
 * 플랜 종목 줄 (종목 탭 맨 위) — 홍겸 님 09-29 확정.
 *  · 한 줄 · 기본 접힘 · **게이지는 접어도 보이고 % 가 가운데**
 *  · 펼치면 가로줄 바로 아래 **시작 – 현재 – 목표** 막대, 그 아래 박스 셋
 */
@Composable
fun 플랜종목칸(상태: 앱상태) {
    val d = 상태.d
    if (d.플랜들.isEmpty()) return
    val c = Local색.current
    var 펼친 by remember { mutableStateOf<String?>(null) }
    // 꾹 눌러 끌어 순서 바꾸기 (10-01 · 01 ㉓-7 · 동작방식 D3-9 — 루틴 탭과 같은 동작)
    var 끄는 by remember { mutableStateOf<String?>(null) }
    var 거리 by remember { mutableStateOf(0f) }
    var 놓을 by remember { mutableStateOf(-1) }   // 놓일 자리 (목록 번호)
    val 높이들 = remember { mutableStateMapOf<String, Int>() }
    val 진동 = LocalHapticFeedback.current

    카드(Modifier.번호("종3"), 안쪽 = 0.dp) {
        d.플랜들.forEachIndexed { i, p ->
            key(p.id) {
                if (i > 0) 구분선()
                val 원 = d.플랜들.indexOfFirst { it.id == 끄는 }
                // 놓일 곳 표시 — 루틴 탭과 같은 선 (위로 옮기면 그 줄 위에, 아래로면 그 줄 아래에)
                val 선 = if (끄는 != null && 놓을 == i && 놓을 != 원) (if (놓을 < 원) 0 else 2) else -1
                val 선색 = c.휴식
                Box(
                    Modifier.onSizeChanged { 높이들[p.id] = it.height }
                        .zIndex(if (끄는 == p.id) 1f else 0f)
                        .graphicsLayer { translationY = if (끄는 == p.id) 거리 else 0f; alpha = if (끄는 == p.id) 0.9f else 1f }
                        .drawBehind {
                            if (선 == 0) drawRect(선색, size = Size(size.width, 3.dp.toPx()))
                            if (선 == 2) drawRect(선색, topLeft = Offset(0f, size.height - 3.dp.toPx()), size = Size(size.width, 3.dp.toPx()))
                        },
                ) {
                    플랜한줄(상태, p, 펼친 == p.id, 끌림 = 끄는 == p.id,
                        끌기 = Modifier.pointerInput(p.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    진동.performHapticFeedback(HapticFeedbackType.LongPress)
                                    끄는 = p.id; 거리 = 0f; 펼친 = null
                                    놓을 = 상태.d.플랜들.indexOfFirst { it.id == p.id }
                                },
                                onDrag = { ch, 양 ->
                                    ch.consume()
                                    거리 += 양.y
                                    // 손가락이 지나간 줄 수만큼 놓일 자리를 옮긴다 (줄마다 높이가 다르다)
                                    val 목 = 상태.d.플랜들
                                    val idx = 목.indexOfFirst { it.id == p.id }
                                    var 남 = 거리; var t = idx
                                    while (남 > 0 && t < 목.lastIndex) { val h = (높이들[목[t + 1].id] ?: 1).toFloat(); if (남 < h / 2) break; 남 -= h; t++ }
                                    while (남 < 0 && t > 0) { val h = (높이들[목[t - 1].id] ?: 1).toFloat(); if (-남 < h / 2) break; 남 += h; t-- }
                                    놓을 = t
                                },
                                onDragEnd = {
                                    val idx = 상태.d.플랜들.indexOfFirst { it.id == p.id }
                                    if (idx >= 0 && 놓을 >= 0 && 놓을 != idx) 상태.바꿈 { it.copy(플랜들 = 자리옮김(it.플랜들, idx, 놓을)) }
                                    끄는 = null; 거리 = 0f; 놓을 = -1
                                },
                                onDragCancel = { 끄는 = null; 거리 = 0f; 놓을 = -1 },
                            )
                        },
                    ) { 펼친 = if (펼친 == p.id) null else p.id }
                }
            }
        }
    }
    Box(Modifier.height(12.dp))
}

@Composable
private fun 플랜한줄(상태: 앱상태, p: 플랜, 펼침: Boolean, 끌림: Boolean = false, 끌기: Modifier = Modifier, on누름: () -> Unit) {
    val c = Local색.current
    val 몸 = 상태.d.몸
    val 표 = p.표
    // 맨몸 종목은 진행 변수가 횟수다 (21 문서 0절) — 시작·현재·목표를 모두 그 단위로 본다
    val 단위글 = if (p.횟수진행) p.단위.단위 else "kg"
    val 시작 = p.시작진행값
    val 목표 = p.목표진행값
    val 지금 = p.지금진행값
    val 진행 = if (목표 > 시작) ((지금 - 시작) / (목표 - 시작)).coerceIn(0.0, 1.0) else 0.0
    val 보정 = 표?.let { 보정배수(상태.d.향상기록들, it.이름) } ?: 1.0
    // 10-01 감시관: 측정 뒤에는 그 측정값부터 다시 걸어간 표를 쓴다 (루틴 탭의 '측정 · N회차' 와 같은 숫자)
    val 전체 = if (표 != null && 몸.찼나) p.회표(몸, 상태.d.향상기록들) else emptyList()
    val 총회 = maxOf(전체.lastOrNull()?.회 ?: 0, p.한회)
    val 남은회 = (총회 - p.한회).coerceAtLeast(0)
    val 남은주 = if (p.주당 > 0) (남은회 + p.주당 - 1) / p.주당 else 0
    val 지난주 = p.지난주(상태.오늘)
    val 다음측정 = 전체.firstOrNull { it.회 > p.한회 && it.측정일 }?.회 ?: 총회

    Column(Modifier.then(if (끌림) Modifier.background(c.강조옅음) else Modifier).padding(horizontal = 10.dp, vertical = 8.dp)) {
        Row(Modifier.눌림(on누름).then(끌기), verticalAlignment = Alignment.CenterVertically) {
            글(p.이름, Modifier.weight(1f, fill = false), 크기값 = 크기.조금작게, 굵기 = FontWeight.Bold)
            Box(Modifier.width(4.dp))
            Box(
                Modifier.clip(RoundedCornerShape(모서리.아주작게)).background(c.강조).padding(horizontal = 4.dp),
            ) { 글("목표", 크기값 = 크기.아주작게, 굵기 = FontWeight.Bold, 색 = c.강조글) }
            Box(Modifier.width(4.dp))
            글(if (p.횟수진행) p.맨몸목표글 else p.목표글, 크기값 = 크기.아주작게, 색 = c.흐림)
            Box(Modifier.weight(1f))
            글("${p.한회}회", 크기값 = 크기.아주작게, 색 = c.강조)
            Box(Modifier.width(4.dp))
            글("${지난주}주차", 크기값 = 크기.아주작게, 색 = c.강조)
            Box(Modifier.width(4.dp))
            글(if (펼침) "▲" else "▼", 크기값 = 크기.아주작게, 색 = c.강조)
        }

        // 큰 게이지 — 접어도 보인다
        Box(Modifier.fillMaxWidth().padding(top = 6.dp).height(20.dp)
            .clip(RoundedCornerShape(10.dp)).background(c.면2)
            .border(1.dp, c.속선, RoundedCornerShape(10.dp))) {
            Box(Modifier.fillMaxWidth(진행.toFloat()).fillMaxHeight().background(c.강조))
            Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                글("${(진행 * 100).toInt()}%", 크기값 = 크기.아주작게, 굵기 = FontWeight.Bold)
            }
        }

        if (펼침) {
            구분선(Modifier.padding(top = 8.dp))
            // 시작 – 현재 – 목표 — 09-30: 10dp 위로 올렸다 (14 → 4)
            Box(Modifier.fillMaxWidth().padding(top = 4.dp).height(4.dp)
                .clip(RoundedCornerShape(2.dp)).background(c.면2)) {
                Box(Modifier.fillMaxWidth(진행.toFloat()).fillMaxHeight().background(c.강조))
            }
            Row(Modifier.fillMaxWidth().padding(top = 3.dp)) {
                글("시작 ${무게글(시작)}$단위글", 크기값 = 크기.아주작게, 색 = c.옅음)
                Box(Modifier.weight(1f))
                글("현재 ${무게글(지금)}$단위글", 크기값 = 크기.아주작게, 굵기 = FontWeight.Bold, 색 = c.강조)
                Box(Modifier.weight(1f))
                글("목표 ${무게글(목표)}$단위글", 크기값 = 크기.아주작게, 색 = c.옅음)
            }

            // 박스 셋 — 한 줄에
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                칸타일(Modifier.weight(1f), "${p.한회}회차", "${콤마(남은회)}회차 남음",
                    if (총회 > 0) p.한회.toFloat() / 총회 else 0f)
                칸타일(Modifier.weight(1f), 운동량글(p.누적볼륨), "누적 운동량",
                    if (총회 > 0) p.한회.toFloat() / 총회 else 0f)
                칸타일(Modifier.weight(1f), "${콤마(남은주)}주 남음", "누적 ${지난주}주",
                    if (지난주 + 남은주 > 0) 지난주.toFloat() / (지난주 + 남은주) else 0f)
            }

            Row(Modifier.fillMaxWidth().padding(top = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                글("다음 측정일", Modifier.weight(1f), 크기값 = 크기.아주작게, 색 = c.흐림)
                글("${(다음측정 - p.한회).coerceAtLeast(0)}회차 후 예정 (${다음측정}회차)", 크기값 = 크기.아주작게, 굵기 = FontWeight.Bold)
            }
            글(
                "주 ${p.주당}회 · ${방식찾기(p.방식번호).이름}" +
                    (if (표 != null) " · ${속도출처글(상태.d.향상기록들, 표.이름)}" else "") +
                    (if (p.보조모드 == 1) " · 어시스트 ${무게글(p.보조무게)}kg" else if (p.보조모드 == 2) " · 과부하 +${무게글(p.보조무게)}kg" else ""),
                Modifier.padding(top = 3.dp), 크기값 = 크기.아주작게, 색 = c.옅음, 줄 = 2,
            )
            if (!몸.찼나) 글("신체 정보가 비어 있어 기간을 셀 수 없습니다 (설정 → 신체 정보)", Modifier.padding(top = 3.dp), 크기값 = 크기.아주작게, 색 = c.나쁨, 줄 = 2)

            Box(Modifier.height(간격.좁게))
            // 10-01: 만든 뒤에도 목표 · 세부 내용을 고칠 수 있게 (홍겸 님)
            Row(horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
                버튼("고치기 · 지금 실력 넣기", { 플랜고침.value = p.id }, Modifier.weight(1.6f), 작게 = true, 주요 = true)
                버튼("지우기", {
                    상태.지우고알림("${p.이름} 플랜을 지웠습니다") { d -> d.copy(플랜들 = d.플랜들.filter { it.id != p.id }).플랜줄정리() }
                }, Modifier.weight(1f), 작게 = true, 글색 = c.나쁨)
            }
        }
    }
}

/** 고치는 중인 플랜 — 시트는 화면 맨 바깥(종목 화면 Box)에서 그린다. 카드 안에서 그리면 화면을 덮지 못한다 (10-01 감시관) */
internal val 플랜고침 = mutableStateOf<String?>(null)

@Composable
fun 플랜고치기자리(상태: 앱상태) {
    // 종목 화면을 떠나면 닫는다 — 다시 왔을 때 시트가 저절로 열리지 않게 (10-01 감시관)
    androidx.compose.runtime.DisposableEffect(Unit) { onDispose { 플랜고침.value = null } }
    val id = 플랜고침.value ?: return
    val p = 상태.d.플랜들.firstOrNull { it.id == id } ?: run { 플랜고침.value = null; return }
    플랜고치기시트(상태, p) { 플랜고침.value = null }
}

/**
 * 플랜 고치기 (10-01 홍겸 님 — "플랜 생성후에도 목표설정이나 세부내용 바꿀수있게").
 * 이름 · 목표 · 주당 · 훈련 방식 · 세트/횟수 · 보조 를 고친다.
 * 맨 아래 **지금 실력 넣기** — 측정일이 아니어도 언제든 (20 문서 C-2). 회차표가 그 값에서 다시 걸어간다
 */
@Composable
private fun 플랜고치기시트(상태: 앱상태, p: 플랜, 닫기: () -> Unit) {
    val c = Local색.current
    val 표 = p.표 ?: run { 닫기(); return }
    val 맨몸 = p.횟수진행
    val 수 = { s: String -> s.trim().toDoubleOrNull() ?: 0.0 }
    var 이름 by remember { mutableStateOf(p.이름) }
    var 목형식 by remember { mutableStateOf(p.목표방식) }
    var 목w by remember { mutableStateOf(무게글(p.목표무게)) }
    var 목r by remember { mutableStateOf("${p.목표횟수}") }
    var 단위 by remember { mutableStateOf(p.단위) }
    var 목개수 by remember { mutableStateOf(무게글(p.목표개수)) }
    var 주당 by remember { mutableStateOf(p.주당) }
    var 방식번호 by remember { mutableStateOf(p.방식번호) }
    var 강도 by remember { mutableStateOf(p.강도) }
    var 세트수 by remember { mutableStateOf(p.세트수) }
    var 직접횟수 by remember { mutableStateOf(p.직접횟수) }
    var 보조모드 by remember { mutableStateOf(p.보조모드) }
    var 보조무게 by remember { mutableStateOf(무게글(p.보조무게)) }
    var 실w by remember { mutableStateOf("") }
    var 실r by remember { mutableStateOf("") }
    var 방식열림 by remember { mutableStateOf(false) }

    시트("${p.이름} 고치기", 닫기) {
        글("이름", 크기값 = 크기.아주작게, 색 = c.옅음)
        입력칸(이름, { 이름 = it }, Modifier.fillMaxWidth(), 안내 = p.이름)   // 10-01: 글칸은 숫자 자판이라 한글을 못 넣었다
        Box(Modifier.height(간격.좁게))
        글("목표", 크기값 = 크기.아주작게, 색 = c.옅음)
        if (맨몸) {
            Row(horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                목표단위.entries.filter { it != 목표단위.무게 }.forEach { u -> 작은칩(u.이름, 단위 == u) { 단위 = u } }
            }
            Row { 글칸("목표 (${단위.단위})", 목개수, { 목개수 = it }, Modifier.weight(1f)) }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                작은칩("1RM", 목형식 == 목표형식.RM) { 목형식 = 목표형식.RM }
                작은칩("무게 × 횟수", 목형식 == 목표형식.무게횟수) { 목형식 = 목표형식.무게횟수 }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
                글칸(if (목형식 == 목표형식.RM) "목표 1RM (kg)" else "무게 (kg)", 목w, { 목w = it }, Modifier.weight(1f))
                if (목형식 == 목표형식.무게횟수) 글칸("횟수", 목r, { 목r = it }, Modifier.weight(1f))
            }
        }
        Box(Modifier.height(간격.좁게))
        글("주당 횟수", 크기값 = 크기.아주작게, 색 = c.옅음)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            (1..7).forEach { f -> Box(Modifier.weight(1f)) { 작은칩("${f}회", 주당 == f) { 주당 = f } } }
        }
        Box(Modifier.height(간격.좁게))
        if (!맨몸) 고르기줄("훈련 방식 · ${방식찾기(방식번호).이름}",
            if (방식번호 == 1) "${if (세트수 > 0) 세트수 else 강도들[강도].세트}×${if (직접횟수 > 0) 직접횟수 else 강도들[강도].횟수}" else null) { 방식열림 = true }
        if (표.보조옵션) {
            글("보조", 크기값 = 크기.아주작게, 색 = c.옅음)
            Row(horizontalArrangement = Arrangement.spacedBy(간격.아주좁게), verticalAlignment = Alignment.CenterVertically) {
                작은칩("정자세", 보조모드 == 0) { 보조모드 = 0 }
                작은칩("어시스트", 보조모드 == 1) { 보조모드 = 1 }
                작은칩("과부하", 보조모드 == 2) { 보조모드 = 2 }
            }
            if (보조모드 != 0) Row { 글칸(if (보조모드 == 1) "보조 무게 (kg)" else "추가 무게 (kg)", 보조무게, { 보조무게 = it }, Modifier.weight(1f)) }
        }
        Box(Modifier.height(간격.좁게))
        버튼("저장", {
            상태.바꿈 { d0 -> 플랜이름바꾸기(d0, p.id, 이름).let { d -> d.copy(플랜들 = d.플랜들.map { q ->
                if (q.id != p.id) q else q.copy(
                    목표방식 = 목형식,
                    목표무게 = if (맨몸) q.목표무게 else 수(목w).takeIf { it > 0 } ?: q.목표무게,
                    목표횟수 = if (맨몸 || 목형식 == 목표형식.RM) 1 else 수(목r).toInt().coerceAtLeast(1),
                    단위 = if (맨몸) 단위 else q.단위,
                    목표개수 = if (맨몸) (수(목개수).takeIf { it > 0 } ?: q.목표개수) else q.목표개수,
                    주당 = 주당, 방식번호 = if (맨몸) 1 else 방식번호, 강도 = 강도, 세트수 = 세트수, 직접횟수 = 직접횟수,
                    보조모드 = if (표.보조옵션) 보조모드 else 0, 보조무게 = if (표.보조옵션) 수(보조무게) else 0.0,
                )
            }) } }
            닫기()
        }, Modifier.fillMaxWidth(), 주요 = true)

        Box(Modifier.height(간격.보통))
        구분선()
        Box(Modifier.height(간격.좁게))
        글("지금 실력 넣기 — 측정일이 아니어도 됩니다", 크기값 = 크기.아주작게, 색 = c.옅음)
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
            if (!맨몸) 글칸("무게 (kg)", 실w, { 실w = it }, Modifier.weight(1f))
            글칸(if (맨몸) "정자세 최대 (${p.단위.단위})" else "횟수", 실r, { 실r = it }, Modifier.weight(1f))
            버튼("기록", {
                val r = 수(실r).toInt()
                val w = if (맨몸) 표.유효부하(상태.d.몸.체중) else 수(실w)
                if (r >= 1 && w > 0) {
                    // 10-02: 향상기록에 누적횟수 · 운동일수까지 (21 문서 5절 · Plan.kt 앱데이터.측정넣기)
                    상태.바꿈 { d -> d.측정넣기(p.id, 상태.오늘, w, r) }
                    닫기()
                }
            }, 작게 = true)
        }
    }
    if (방식열림) 방식시트(방식번호, 강도, 세트수, 직접횟수, false,
        { 방식번호 = it }, { 강도 = it }, { 세트수 = it }, { 직접횟수 = it }) { 방식열림 = false }
}

/** 박스 하나 — 큰 값 · 게이지 · 작은 글 */
@Composable
private fun 칸타일(modifier: Modifier, 큰: String, 밑: String, 비: Float) {
    val c = Local색.current
    Column(
        modifier.clip(RoundedCornerShape(모서리.작게)).background(c.면)
            .border(1.dp, c.속선, RoundedCornerShape(모서리.작게)).padding(6.dp),
    ) {
        글(큰, 크기값 = 크기.조금작게, 굵기 = FontWeight.Bold)
        Box(Modifier.fillMaxWidth().padding(vertical = 3.dp).height(4.dp)
            .clip(RoundedCornerShape(2.dp)).background(c.면2)) {
            Box(Modifier.fillMaxWidth(비.coerceIn(0f, 1f)).fillMaxHeight().background(c.강조))
        }
        글(밑, 크기값 = 크기.아주작게, 색 = c.옅음)
    }
}
