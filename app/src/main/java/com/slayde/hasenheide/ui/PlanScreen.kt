package com.slayde.hasenheide.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.slayde.hasenheide.data.방식찾기
import com.slayde.hasenheide.data.무게글
import com.slayde.hasenheide.data.목표방식환산
import com.slayde.hasenheide.data.목표형식
import com.slayde.hasenheide.data.속도출처글
import com.slayde.hasenheide.data.숫
import com.slayde.hasenheide.data.자리옮김
import com.slayde.hasenheide.data.진행값글
import com.slayde.hasenheide.data.처방글
import com.slayde.hasenheide.data.처방변화글
import com.slayde.hasenheide.data.플랜
import com.slayde.hasenheide.data.플랜고치기
import com.slayde.hasenheide.data.플랜고침값
import com.slayde.hasenheide.data.플랜되살리기
import com.slayde.hasenheide.data.플랜만들기
import com.slayde.hasenheide.data.플랜지우기
import com.slayde.hasenheide.data.플랜폼값
import com.slayde.hasenheide.data.플랜표
import com.slayde.hasenheide.data.현재수행넣기
import com.slayde.hasenheide.data.회블록
import com.slayde.hasenheide.data.회처방
import com.slayde.hasenheide.data.회표
import com.slayde.hasenheide.data.고침값
import com.slayde.hasenheide.data.달성비
import com.slayde.hasenheide.data.현재진행값
import com.slayde.hasenheide.data.폼안내
import com.slayde.hasenheide.data.폼판정
import com.slayde.hasenheide.data.훈련방식
import com.slayde.hasenheide.data.훈련방식들
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.높이
import com.slayde.hasenheide.ui.theme.모서리
import com.slayde.hasenheide.ui.theme.부품치수
import com.slayde.hasenheide.ui.theme.선굵기
import com.slayde.hasenheide.ui.theme.움직임
import com.slayde.hasenheide.ui.theme.크기
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * 플랜 탭 (10-05 앱 옮기기 2단계 PS — 시안 v21 `플랜탭` · `플랜결과시트` · `방식시트` · `고침시트` · `플랜카드`).
 *
 * 맨 위 띠 '운동 플랜' → 종목 칩 → **목표 근력**(위) → **현재 근력**(아래 · [입력하지 않음]) → 훈련 방식 줄 → [플랜 만들기]
 *  → 결과 시트(회차 묶음 · [플랜 저장]) → 종목 탭에 플랜 카드가 생긴다.
 *  · v8 9rvs 주당 고르기 · 빈도 경고 없음 (회차만 센다) · sjb8 판정 알약 · ksao 같은 종목 플랜 여럿('벤치프레스 2')
 *  · v9 pt3d 처방 글은 세트 → 무게 → 횟수 · q3hz '○○ 플랜 고치기'
 *  · 계산은 전부 `data/Plan.kt` (시험: PlanTest `옮기기시월오일`)
 */
@Composable
fun 플랜화면(상태: 앱상태, 설정으로: () -> Unit, 종목으로: () -> Unit) {
    val c = Local색.current
    val d = 상태.d
    val f = 플랜폼상태.value
    val t = 플랜표.찾기(f.종목)
    var 오류 by remember { mutableStateOf("") }
    var 결과 by remember { mutableStateOf<플랜?>(null) }
    var 방식열림 by remember { mutableStateOf(false) }
    /** 칸을 고치면 오류 글을 지운다 (시안 `폼값`) */
    fun 폼(g: (플랜폼값) -> 플랜폼값) { 플랜폼상태.value = g(플랜폼상태.value); 오류 = "" }
    val 판 = 폼판정(f, d.몸)
    val 맨몸 = t?.맨몸인가 == true
    val 안함 = f.입력안함

    Column(Modifier.fillMaxSize()) {
        머리띠("운동 플랜")
        당겨새로고침({ }, Modifier.weight(1f).fillMaxWidth()) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(horizontal = 간격.보통).padding(top = 간격.보통, bottom = 간격.보통),
                verticalArrangement = Arrangement.spacedBy(간격.좁게),
            ) {
                // ── 플0 · 종목 — 바꾸면 칸을 모두 비운다 (시안 `폼값` 종목) ──
                칩줄(플랜표.종목들.map { it.이름 }, f.종목, { v -> if (v != f.종목) { 플랜폼상태.value = 플랜폼값(종목 = v); 오류 = "" } }, Modifier.번호("플0"))

                if (t != null) {
                    // ── 플2 · 목표 근력 (위) ──
                    카드(Modifier.번호("플2")) {
                        판정줄("목표 근력", 판.second)
                        Box(Modifier.height(간격.좁게))
                        if (맨몸) {
                            Row { 폼칸("목표 개수 (한 번에)", f.목표개수, "회", Modifier.weight(1f)) { v -> 폼 { it.copy(목표개수 = v) } } }
                        } else {
                            칩줄(listOf("1RM", "무게 × 횟수"), if (f.목표방식 == 목표형식.RM) "1RM" else "무게 × 횟수",
                                { v -> 폼 { it.copy(목표방식 = if (v == "1RM") 목표형식.RM else 목표형식.무게횟수) } })
                            Box(Modifier.height(간격.좁게))
                            Row(horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
                                폼칸("목표 무게", f.목표무게, "kg", Modifier.weight(1f)) { v -> 폼 { it.copy(목표무게 = v) } }
                                if (f.목표방식 == 목표형식.무게횟수) 폼칸("횟수", f.목표횟수, "회", Modifier.weight(1f)) { v -> 폼 { it.copy(목표횟수 = v) } }
                            }
                        }
                    }

                    // ── 플1 · 현재 근력 (아래) — [입력하지 않음] 은 제목 줄 맨 오른쪽 ──
                    카드(Modifier.번호("플1")) {
                        판정줄("현재 근력", 판.first) {
                            안함단추(안함) { 폼 { it.copy(입력안함 = !it.입력안함) } }   // sjb8 다시 누르면 풀린다
                        }
                        Box(Modifier.height(간격.좁게))
                        흐린칸(안함) {
                            if (맨몸) {
                                if (t.보조옵션) {
                                    칩줄(보조말, 보조말[f.보조모드.coerceIn(0, 2)], { v -> 폼 { it.copy(보조모드 = 보조말.indexOf(v).coerceAtLeast(0)) } })
                                    Box(Modifier.height(간격.좁게))
                                }
                                if (f.보조모드 == 0 || !t.보조옵션) {
                                    Row { 폼칸("정자세 개수", f.정자세, "회", Modifier.weight(1f)) { v -> 폼 { it.copy(정자세 = v) } } }
                                } else Row(horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
                                    폼칸(if (f.보조모드 == 1) "보조 무게" else "추가 무게", f.보조무게, "kg", Modifier.weight(1f)) { v -> 폼 { it.copy(보조무게 = v) } }
                                    폼칸("횟수", f.보조횟수, "회", Modifier.weight(1f)) { v -> 폼 { it.copy(보조횟수 = v) } }
                                }
                            } else Row(horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
                                폼칸("무게", f.현재무게, "kg", Modifier.weight(1f)) { v -> 폼 { it.copy(현재무게 = v) } }
                                폼칸("횟수", f.현재횟수, "회", Modifier.weight(1f)) { v -> 폼 { it.copy(현재횟수 = v) } }
                            }
                        }
                        val 안내 = 폼안내(f, d.몸)
                        if (안내.isNotEmpty()) 맞춤글(안내, Modifier.padding(top = 간격.아주좁게), 색 = c.흐림)
                    }

                    // ── 플3 · 훈련 방식 — 누르는 줄 하나 (v8 15lk · 맨몸은 1번 하나뿐이라 없다) ──
                    if (!맨몸) 방식고름줄(방식찾기(f.방식번호), Modifier.번호("플3")) { 방식열림 = true }

                    if (오류.isNotEmpty()) 글(오류, 크기값 = 크기.조금작게, 색 = c.나쁨, 줄 = 2)
                    버튼("플랜 만들기", {
                        val (p, 막힘) = 플랜만들기(플랜폼상태.value, 상태.d.몸, 상태.d.플랜들, "p" + System.currentTimeMillis(), 상태.오늘, 상태.d.향상기록들)
                        if (p == null) 오류 = 막힘 else { 오류 = ""; 결과 = p }
                    }, Modifier.fillMaxWidth(), 주요 = true)
                }
            }
        }
    }

    // ── 훈련 방식 시트 ──
    if (방식열림) 방식시트(f.방식번호, { n -> 폼 { it.copy(방식번호 = n) }; 방식열림 = false }) { 방식열림 = false }

    // ── 결과 시트 — 닫기(✕) = 고치러 돌아가기 (시안 머리의 [고치기]) ──
    결과?.let { p ->
        플랜결과시트(상태, p, { 결과 = null }) {
            // 빠르게 두 번 눌러도 한 번만 들어간다 (같은 id 면 넣지 않는다)
            상태.바꿈 { dd -> if (dd.플랜들.any { it.id == p.id }) dd else dd.copy(플랜들 = dd.플랜들 + p) }
            발자취.적기("플랜 저장 · ${p.이름}")
            결과 = null
            플랜폼상태.value = 플랜폼값()
            종목으로()
            상태.알림.토스트("종목 탭에 넣었습니다")
        }
    }
}

/** 플랜 탭 입력 — 탭을 옮겨도 남는다 (시안 `U.플랜폼` · 09-24 '쓰던 글이 사라졌다'). 앱을 끄면 처음으로 */
internal val 플랜폼상태 = mutableStateOf(플랜폼값())

private val 보조말 = listOf("정자세", "어시스트", "과부하")

// ═══════════════════ 플랜 탭 부품 ═══════════════════

/** 카드 제목 줄 — '목표 근력 [상위 20%]' · '현재 근력 [초보자] …… [입력하지 않음]' (시안 v9 9zt1 · 4v24) */
@Composable
private fun 판정줄(제목: String, 판정: String, 오른쪽: (@Composable RowScope.() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().heightIn(min = 높이.아주낮게), verticalAlignment = Alignment.CenterVertically) {
        글(제목, 크기값 = 크기.조금작게, 굵기 = FontWeight.Bold)
        if (판정.isNotEmpty()) { Box(Modifier.width(간격.좁게)); 판정알약(판정, 강조 = true) }
        if (오른쪽 != null) { Box(Modifier.weight(1f)); 오른쪽() }
    }
}

/** 작은 알약 — 강조(판정 · 측정) 또는 달성(목표). 11 굵게 (시안 `.알약.강조` · `.알약.달성`) */
@Composable
private fun 판정알약(글자: String, 강조: Boolean) {
    val c = Local색.current
    Box(
        Modifier.clip(RoundedCornerShape(모서리.작게)).background(if (강조) c.강조 else c.좋음옅음)
            .padding(horizontal = 간격.아주좁게),
    ) { 글(글자, 크기값 = 크기.아주작게, 굵기 = FontWeight.Bold, 색 = if (강조) c.강조글 else c.좋음) }
}

/** [입력하지 않음] — 흰 바탕 · 빨간 글 · 높이 32. 켜면 '✓' 와 빨간 테두리 (시안 `.안함단추`) */
@Composable
private fun 안함단추(켬: Boolean, onClick: () -> Unit) {
    val c = Local색.current
    Box(
        Modifier.height(높이.낮게).clip(RoundedCornerShape(모서리.작게)).background(c.면)
            .border(선굵기.보통, if (켬) c.나쁨 else c.속선, RoundedCornerShape(모서리.작게))
            .눌림(onClick).padding(horizontal = 간격.보통),
        contentAlignment = Alignment.Center,
    ) { 글((if (켬) "✓ " else "") + "입력하지 않음", 크기값 = 크기.버튼, 굵기 = FontWeight.Bold, 색 = c.나쁨) }
}

/** 흐린 칸 — 누를 수 없고 옅게 (시안 `.흐린칸` opacity .4 · pointer-events none) */
@Composable
private fun 흐린칸(켬: Boolean, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().graphicsLayer { alpha = if (켬) 흐림비 else 1f }) { content() }
        if (켬) Box(Modifier.matchParentSize().눌림 { })
    }
}
private const val 흐림비 = 0.4f

/** 이름표 + 숫자 칸 + 단위 (시안 `폼입력` — label.칸 · 입력줄 · em) */
@Composable
private fun 폼칸(이름: String, 값: String, 단위: String, modifier: Modifier = Modifier, 바꿈: (String) -> Unit) {
    val c = Local색.current
    Column(modifier) {
        글(이름, 크기값 = 크기.아주작게, 색 = c.옅음)
        단위칸(값, 단위, "", 바꿈)
    }
}

/** 숫자 입력칸 + 오른쪽 단위 글 (시안 `.입력줄`) */
@Composable
private fun 단위칸(값: String, 단위: String, 안내: String, 바꿈: (String) -> Unit, modifier: Modifier = Modifier) {
    val c = Local색.current
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
        입력칸(값, 바꿈, Modifier.weight(1f), 안내 = 안내, 숫자 = true)
        if (단위.isNotEmpty()) 글(단위, 크기값 = 크기.조금작게, 색 = c.옅음)
    }
}

/** 훈련 방식 줄 — 높이 44 · 1px 강조 테두리: [훈련 방식][1. 꾸준히 늘리기][›] (시안 v8 15lk `.방식고름줄`) */
@Composable
private fun 방식고름줄(m: 훈련방식, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val c = Local색.current
    Row(
        modifier.fillMaxWidth().height(높이.높게).clip(RoundedCornerShape(모서리.보통)).background(c.면)
            .border(선굵기.보통, c.강조, RoundedCornerShape(모서리.보통)).눌림(onClick).padding(horizontal = 간격.보통),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.좁게),
    ) {
        글("훈련 방식", 크기값 = 크기.아주작게, 색 = c.옅음)
        글("${m.번호}. ${m.이름}", Modifier.weight(1f), 크기값 = 크기.본문, 굵기 = FontWeight.Bold)
        Icon(아이콘.오른쪽, null, Modifier.size(18.dp), tint = c.옅음)
    }
}

// ═══════════════════ 결과 시트 ═══════════════════

/**
 * 플랜 결과 (시안 e8am `플랜결과시트`) — '추정 61kg → 120kg · 총 96회차' + 회차 묶음(측정일까지)마다
 * [N–M회차 · 'M회차에 근력 측정' / '목표'] + 처방 한 줄(처음 → 끝) + [플랜 저장]. 주 · 주당 표시 없음 (9rvs)
 */
@Composable
private fun 플랜결과시트(상태: 앱상태, p: 플랜, 닫기: () -> Unit, 저장: () -> Unit) {
    val c = Local색.current
    val d = 상태.d
    val t = p.표
    val 표 = remember(p, d.몸, d.향상기록들) { p.회표(d.몸, d.향상기록들) }
    val 묶음들 = remember(표) { 회블록(표) }
    시트("${p.이름} 플랜", 닫기) {
        if (표.isEmpty()) { 글("계산할 수 없습니다 — 값을 확인해 주세요", 크기값 = 크기.조금작게, 색 = c.나쁨); return@시트 }
        맞춤글("${if (p.측정먼저) "추정 " else ""}${진행값글(t, p.시작진행값)} → ${진행값글(t, p.목표진행값)} · 총 ${표.last().회}회차",
            최대 = 크기.조금작게, 굵기 = FontWeight.Bold)
        Box(Modifier.height(간격.좁게))
        Column(verticalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
            묶음들.forEach { b ->
                val a = b.first(); val z = b.last()
                val 앞 = 회처방(p, a.목표값, d.설정.무게폭, d.몸, false, a.주)
                val 뒤 = 회처방(p, z.목표값, d.설정.무게폭, d.몸, false, z.주)
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(모서리.작게))
                        .border(선굵기.보통, c.선, RoundedCornerShape(모서리.작게))
                        .padding(horizontal = 간격.보통, vertical = 간격.아주좁게),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        글(if (a.회 == z.회) "${a.회}회차" else "${a.회}–${z.회}회차", Modifier.weight(1f), 크기값 = 크기.아주작게, 굵기 = FontWeight.Bold)
                        if (z.측정일) 판정알약("${z.회}회차에 근력 측정", 강조 = true) else 판정알약("목표", 강조 = false)
                    }
                    처방줄(처방변화글(앞, 뒤), 여러줄 = 앞.size > 1)
                }
            }
        }
        Box(Modifier.height(간격.보통))
        버튼("플랜 저장", 저장, Modifier.fillMaxWidth(), 주요 = true)
    }
}

/** 처방 글 — 한 줄이면 맞춰 줄이고, 여러 줄 방식은 ' / ' 에서 감긴다 (시안 `처방감김`) */
@Composable
private fun 처방줄(글자: String, 여러줄: Boolean, modifier: Modifier = Modifier) {
    if (여러줄) 글(글자, modifier, 크기값 = 크기.조금작게, 줄 = 4)
    else 맞춤글(글자, modifier, 최대 = 크기.조금작게)
}

// ═══════════════════ 훈련 방식 시트 ═══════════════════

/**
 * 방식 목록 (시안 `방식시트` · `방식속`) — 열 줄이 모두 같은 띠. 처음엔 고른 방식이 펼쳐져 있다.
 * 펼친 속: 특징 · '주 2~3 · 근거' · (2번부터) '라벨 세트×횟수' · [이 방식으로]. 1번은 특징만 (v9 dwyk)
 */
@Composable
private fun 방식시트(방식번호: Int, 고름: (Int) -> Unit, 닫기: () -> Unit) {
    var 펼친 by remember { mutableStateOf(방식번호) }
    시트("훈련 방식", 닫기) {
        Column(verticalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
            훈련방식들.forEach { m ->
                key(m.번호) {
                    방식띠(m, m.번호 == 방식번호) { 펼친 = if (펼친 == m.번호) -1 else m.번호 }
                    if (펼친 == m.번호) 방식속(m) { 고름(m.번호) }
                }
            }
        }
    }
}

/** 방식 한 줄 — 강조 바탕 · 강조글 '1. 꾸준히 늘리기' + 수준. 고른 줄은 안쪽 고리 (시안 `.방식띠` · `.골름`) */
@Composable
private fun 방식띠(m: 훈련방식, 골름: Boolean, onClick: () -> Unit) {
    val c = Local색.current
    val 모양 = RoundedCornerShape(모서리.작게)
    Row(
        Modifier.fillMaxWidth().heightIn(min = 높이.보통).clip(모양).background(c.강조)
            .then(if (골름) Modifier.border(선굵기.굵게, c.강조글, 모양) else Modifier)
            .눌림(onClick).padding(horizontal = 간격.보통, vertical = 간격.아주좁게),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.좁게),
    ) {
        글("${m.번호}. ${m.이름}", Modifier.weight(1f), 크기값 = 크기.본문, 굵기 = FontWeight.Bold, 색 = c.강조글)
        글(m.수준글, 크기값 = 크기.아주작게, 색 = c.강조글)
    }
}

@Composable
private fun 방식속(m: 훈련방식, 이걸로: () -> Unit) {
    val c = Local색.current
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(모서리.작게)).background(c.면2).padding(간격.보통),
        verticalArrangement = Arrangement.spacedBy(간격.좁게),
    ) {
        글(m.특징, 크기값 = 크기.조금작게, 줄 = 2)
        맞춤글("주 ${m.주당글} · ${m.근거.joinToString(" · ")}", 색 = c.옅음)
        // dwyk 1번은 특징 글만 — 세트 × 횟수 · 증량 규칙 글 없음. 2번부터는 100kg 기준 세트 × 횟수 (무게는 안 보인다)
        if (m.번호 != 1) 글(m.세트(100.0, 1, 1).joinToString(" · ") { x ->
            "${if (x.라벨.isNotEmpty()) x.라벨 + " " else ""}${x.세트}×${x.횟수}${if (x.더하기) "+" else ""}"
        }, 크기값 = 크기.조금작게, 줄 = 2)
        버튼("이 방식으로", 이걸로, Modifier.fillMaxWidth(), 주요 = true)
    }
}

// ═══════════════════ 종목 탭에 보이는 플랜 카드 ═══════════════════

/**
 * 플랜 카드 하나 (시안 `플랜카드` — 종목 탭 펼친 칸 맨 위 · 플랜마다 하나).
 *  [이름 ……… 14/96회] · 게이지(지금 − 시작 ÷ 목표 − 시작) · [시작 61kg | 지금 70kg | 목표 120kg] ·
 *  '측정 · 15회차 5세트 · 62kg × 10회' (다음 회차 처방 · 없으면 '목표 달성') · '다음 측정 16회차 · 속도 기본표' · [변경][지우기]
 *  · [변경] = 플랜 고치기 시트 ([플랜고치기자리] 가 그린다) · [지우기] = 바로 지우고 [되돌리기] 띠 (동작방식 D3-4)
 * EX(종목 화면)가 플랜마다 부른다. [끌기] = 이름 줄에 붙일 꾹 눌러 끌기
 */
@Composable
fun 플랜카드(상태: 앱상태, p: 플랜, modifier: Modifier = Modifier, 끌기: Modifier = Modifier) {
    val c = Local색.current
    val d = 상태.d
    val t = p.표
    val 표 = remember(p, d.몸, d.향상기록들) { p.회표(d.몸, d.향상기록들) }
    val 끝 = 표.lastOrNull()?.회 ?: p.한회
    val 다 = 표.firstOrNull { it.회 > p.한회 }
    val 측 = 표.firstOrNull { it.회 > p.한회 && it.측정일 }
    val 현 = p.현재진행값(d.몸, d.향상기록들)
    카드(modifier.번호("종플")) {
        Row(끌기.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            글(p.이름, Modifier.weight(1f), 굵기 = FontWeight.Bold)
            글("${p.한회}/${끝}회", 크기값 = 크기.아주작게, 색 = c.옅음)
        }
        Box(Modifier.height(간격.좁게))
        진행막대(p.달성비(d.몸, d.향상기록들).toFloat(), Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth().padding(top = 간격.아주좁게)) {
            글("시작 ${진행값글(t, p.시작진행값)}", 크기값 = 크기.아주작게, 색 = c.흐림)
            글("지금 ${진행값글(t, 현)}", Modifier.weight(1f), 크기값 = 크기.아주작게, 색 = c.흐림, 가운데 = true)
            글("목표 ${진행값글(t, p.목표진행값)}", 크기값 = 크기.아주작게, 색 = c.흐림)
        }
        Box(Modifier.height(간격.아주좁게))
        if (다 == null) 맞춤글(if (t == null) "계산할 수 없는 종목입니다" else "목표 달성", 최대 = 크기.조금작게)
        else {
            val 목 = 회처방(p, 다.목표값, d.설정.무게폭, d.몸, 다.측정일, 다.주)
            처방줄("${if (다.측정일) "측정 · " else ""}${다.회}회차 ${처방글(목)}", 여러줄 = 목.size > 1)
        }
        맞춤글("${if (측 != null) "다음 측정 ${측.회}회차 · " else ""}속도 ${t?.let { 속도출처글(d.향상기록들, it.이름) } ?: "기본표"}",
            Modifier.padding(top = 간격.아주좁게), 색 = c.옅음)
        Box(Modifier.height(간격.좁게))
        Row(horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
            버튼("변경", { 플랜고침.value = p.id }, Modifier.weight(1f), 낮게 = true)
            버튼("지우기", { 플랜지움(상태, p.id) }, 낮게 = true, 글색 = c.나쁨)
        }
    }
}

/** 플랜을 지우고 [되돌리기] 띠 — 여러 개 지우면 한 띠로 합쳐 새것부터 전부 되돌린다 (Parts.kt 알림판) */
internal fun 플랜지움(상태: 앱상태, id: String) {
    val (새, 지운) = 상태.d.플랜지우기(id) ?: return
    if (플랜고침.value == id) 플랜고침.value = null
    상태.바꿈 { 새 }
    발자취.적기("플랜 지움 · ${지운.플랜값.이름}")
    상태.알림.되돌림("플랜지움", { n -> if (n > 1) "플랜 ${n}개를 지웠습니다" else "${지운.플랜값.이름} 플랜을 지웠습니다" }) {
        상태.바꿈 { it.플랜되살리기(지운) }
    }
}

/**
 * 플랜 카드 목록 (종목 탭 — 지금까지 ExerciseScreen 이 맨 위에서 부르던 것). 카드마다 이름 줄을 꾹 눌러 끌면 순서가 바뀐다
 * (동작방식 D3-9 · 11 지침 U5-5 — 끄는 것 0.35 · 놓을 자리 3dp 선). EX 가 종목 칸 안으로 옮기면 [플랜카드] 를 바로 부른다
 */
@Composable
fun 플랜종목칸(상태: 앱상태) {
    val d = 상태.d
    if (d.플랜들.isEmpty()) return
    val c = Local색.current
    var 끄는 by remember { mutableStateOf<String?>(null) }
    var 거리 by remember { mutableStateOf(0f) }
    var 놓을 by remember { mutableStateOf(-1) }   // 놓일 자리 (목록 번호)
    val 높이들 = remember { mutableStateMapOf<String, Int>() }
    val 진동 = LocalHapticFeedback.current

    Column(Modifier.번호("종3"), verticalArrangement = Arrangement.spacedBy(간격.좁게)) {
        d.플랜들.forEachIndexed { i, p ->
            key(p.id) {
                val 원 = d.플랜들.indexOfFirst { it.id == 끄는 }
                val 선 = if (끄는 != null && 놓을 == i && 놓을 != 원) (if (놓을 < 원) 0 else 2) else -1
                val 선색 = c.강조
                Box(
                    Modifier.onSizeChanged { 높이들[p.id] = it.height }
                        .zIndex(if (끄는 == p.id) 1f else 0f)
                        .graphicsLayer { translationY = if (끄는 == p.id) 거리 else 0f; alpha = if (끄는 == p.id) 움직임.끌림투명 else 1f }
                        .drawBehind {
                            val h = 부품치수.놓을선.toPx()
                            if (선 == 0) drawRect(선색, size = Size(size.width, h))
                            if (선 == 2) drawRect(선색, topLeft = Offset(0f, size.height - h), size = Size(size.width, h))
                        },
                ) {
                    플랜카드(상태, p, 끌기 = Modifier.pointerInput(p.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                진동.performHapticFeedback(HapticFeedbackType.LongPress)
                                끄는 = p.id; 거리 = 0f
                                놓을 = 상태.d.플랜들.indexOfFirst { it.id == p.id }
                            },
                            onDrag = { ch, 양 ->
                                ch.consume()
                                거리 += 양.y
                                // 손가락이 지나간 카드 수만큼 놓일 자리를 옮긴다 (카드마다 높이가 다르다)
                                val 목 = 상태.d.플랜들
                                val idx = 목.indexOfFirst { it.id == p.id }.coerceAtLeast(0)
                                var 남 = 거리; var k = idx
                                while (남 > 0 && k < 목.lastIndex) { val h = (높이들[목[k + 1].id] ?: 1).toFloat(); if (남 < h / 2) break; 남 -= h; k++ }
                                while (남 < 0 && k > 0) { val h = (높이들[목[k - 1].id] ?: 1).toFloat(); if (-남 < h / 2) break; 남 += h; k-- }
                                놓을 = k
                            },
                            onDragEnd = {
                                val idx = 상태.d.플랜들.indexOfFirst { it.id == p.id }
                                if (idx >= 0 && 놓을 >= 0 && 놓을 != idx) 상태.바꿈 { it.copy(플랜들 = 자리옮김(it.플랜들, idx, 놓을)) }
                                끄는 = null; 거리 = 0f; 놓을 = -1
                            },
                            onDragCancel = { 끄는 = null; 거리 = 0f; 놓을 = -1 },
                        )
                    })
                }
            }
        }
    }
    Box(Modifier.height(간격.보통))
}

// ═══════════════════ 플랜 고치기 시트 ═══════════════════

/**
 * 고치는 중인 플랜 id — 시트는 화면 맨 바깥에서 [플랜고치기자리] 가 그린다 (카드 안에서 그리면 화면을 덮지 못한다 · 10-01).
 * 종목 화면 · 루틴 화면(RT)이 `플랜고침.value = p.id` 로 열고, 자기 화면 맨 바깥에 `플랜고치기자리(상태)` 를 둔다
 */
internal val 플랜고침 = mutableStateOf<String?>(null)

@Composable
fun 플랜고치기자리(상태: 앱상태) {
    // 화면을 떠나면 닫는다 — 다시 왔을 때 시트가 저절로 열리지 않게 (10-01 감시관). 저장하지 않은 값은 버린다
    DisposableEffect(Unit) { onDispose { 플랜고침.value = null } }
    val id = 플랜고침.value ?: return
    // 그 사이 플랜이 지워졌으면 닫는다
    val p = 상태.d.플랜들.firstOrNull { it.id == id } ?: run { 플랜고침.value = null; return }
    플랜고치기시트(상태, p) { if (플랜고침.value == id) 플랜고침.value = null }
}

/**
 * '○○ 플랜 고치기' (시안 v9 q3hz `고침시트`) — 저장을 누르기 전에는 플랜에 닿지 않는다(닫으면 버린다).
 *  · [훈련 방식 ›] (무게 종목만) · 첫 줄 [운동 이름 | 목표 [1RM][무게 × 횟수] + 칸] — 바꾸면 같은 실력으로 환산 (v6 6yzt)
 *  · 턱걸이: [정자세][어시스트][과부하] + 보조 무게
 *  · '현재 수행능력  지금 1RM 82.5kg …… [입력하지 않음]' + 칸 + [입력] — 넣으면 회차표가 그 값에서 다시 걸어간다
 *  · [저장 | 닫기(빨강)] — 저장은 [입력하지 않음] 이 꺼져 있으면 적어 둔 현재 수행능력도 함께 넣는다
 */
@Composable
internal fun 플랜고치기시트(상태: 앱상태, p: 플랜, 닫기: () -> Unit) {
    val c = Local색.current
    val t = p.표
    val 맨몸 = p.횟수진행
    val 처음 = remember(p.id) { p.고침값() }
    var 이름 by remember(p.id) { mutableStateOf(처음.이름) }
    var 목표방식 by remember(p.id) { mutableStateOf(처음.목표방식) }
    var 목w by remember(p.id) { mutableStateOf(if (처음.목표무게 > 0) 무게글(처음.목표무게) else "") }
    var 목r by remember(p.id) { mutableStateOf("${처음.목표횟수}") }
    var 목개수 by remember(p.id) { mutableStateOf(if (처음.목표개수 > 0) 무게글(처음.목표개수) else "") }
    var 방식번호 by remember(p.id) { mutableStateOf(처음.방식번호) }
    var 보조모드 by remember(p.id) { mutableStateOf(처음.보조모드) }
    var 보조w by remember(p.id) { mutableStateOf(if (처음.보조무게 > 0) 무게글(처음.보조무게) else "") }
    var 안함 by remember(p.id) { mutableStateOf(false) }
    var 측w by remember(p.id) { mutableStateOf("") }
    var 측r by remember(p.id) { mutableStateOf("") }
    var 방식열림 by remember(p.id) { mutableStateOf(false) }
    var 끝남 by remember(p.id) { mutableStateOf(false) }   // 저장을 빠르게 두 번 눌러도 한 번만
    val 폭 = 상태.d.설정.무게폭

    시트("${p.이름} 플랜 고치기", 닫기) {
        // 시안은 머리 띠 오른쪽 [훈련 방식 ›] — 지금 시트 머리는 공용이라 맨 위 오른쪽에 둔다 (보고: 공용 고칠 것)
        if (!맨몸) Row(Modifier.fillMaxWidth().padding(bottom = 간격.좁게), horizontalArrangement = Arrangement.End) {
            버튼("훈련 방식 ›", { 방식열림 = true }, 낮게 = true)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(간격.좁게), verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(3f)) {
                고침표줄 { 고침표("운동 이름") }
                입력칸(이름, { 이름 = it }, Modifier.fillMaxWidth(), 안내 = p.종목)
            }
            Column(Modifier.weight(5f)) {
                고침표줄 {
                    고침표("목표")
                    if (!맨몸) {
                        Box(Modifier.width(간격.좁게))
                        칩줄(listOf("1RM", "무게 × 횟수"), if (목표방식 == 목표형식.RM) "1RM" else "무게 × 횟수", { v ->
                            val 새 = if (v == "1RM") 목표형식.RM else 목표형식.무게횟수
                            if (새 != 목표방식) {
                                val (w, r) = 목표방식환산(목표방식, 숫(목w), max(1, 숫(목r).roundToInt()), 새, 폭)
                                if (w > 0) 목w = 무게글(w)
                                목r = "$r"; 목표방식 = 새
                            }
                        }, Modifier.weight(1f))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                    if (맨몸) 단위칸(목개수, "회", "목표 개수", { 목개수 = it }, Modifier.weight(1f))
                    else {
                        단위칸(목w, "kg", if (목표방식 == 목표형식.RM) "목표 1RM" else "무게", { 목w = it }, Modifier.weight(1f))
                        if (목표방식 == 목표형식.무게횟수) 단위칸(목r, "회", "횟수", { 목r = it }, Modifier.weight(1f))
                    }
                }
            }
        }
        if (맨몸 && t?.보조옵션 == true) {
            Box(Modifier.height(간격.좁게))
            칩줄(보조말, 보조말[보조모드.coerceIn(0, 2)], { v -> 보조모드 = 보조말.indexOf(v).coerceAtLeast(0) })
            if (보조모드 != 0) Row(Modifier.padding(top = 간격.좁게)) {
                폼칸(if (보조모드 == 1) "보조 무게" else "추가 무게", 보조w, "kg", Modifier.weight(1f)) { 보조w = it }
            }
        }

        Box(Modifier.height(간격.보통))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
            고침표("현재 수행능력")
            맞춤글("지금 ${if (맨몸) "최대" else "1RM"} ${진행값글(t, p.지금진행값)}", Modifier.weight(1f), 최대 = 크기.조금작게, 색 = c.흐림)
            안함단추(안함) { 안함 = !안함 }   // q3hz 다시 누르면 풀린다
        }
        Box(Modifier.height(간격.좁게))
        흐린칸(안함) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
                if (맨몸) 단위칸(측r, "회", "정자세 개수", { 측r = it }, Modifier.weight(1f))
                else {
                    단위칸(측w, "kg", "무게", { 측w = it }, Modifier.weight(1f))
                    단위칸(측r, "회", "횟수", { 측r = it }, Modifier.weight(1f))
                }
                버튼("입력", {
                    if (안함 || 끝남) return@버튼   // [입력하지 않음] 이면 [입력] 도 막힌다
                    val 새 = 상태.d.현재수행넣기(p.id, 상태.오늘, 숫(측w), 숫(측r))
                    if (새 == null) 상태.알림.토스트(if (맨몸) "개수를 넣어 주세요" else "무게와 횟수를 넣어 주세요")
                    else {
                        상태.바꿈 { 새 }; 측w = ""; 측r = ""
                        발자취.적기("현재 수행능력 · ${p.이름}")
                        상태.알림.토스트("넣었습니다 · 회차표를 다시 계산했습니다")
                    }
                })
            }
        }

        Box(Modifier.height(간격.보통))
        Row(horizontalArrangement = Arrangement.spacedBy(간격.좁게)) {
            버튼("저장", {
                if (끝남) return@버튼
                val g = 플랜고침값(
                    이름 = 이름, 목표방식 = 목표방식, 목표무게 = 숫(목w), 목표횟수 = max(1, 숫(목r).roundToInt()), 목표개수 = 숫(목개수),
                    방식번호 = 방식번호, 강도 = p.강도, 세트수 = p.세트수, 직접횟수 = p.직접횟수, 보조모드 = 보조모드, 보조무게 = 숫(보조w),
                )
                val (고친, 오류) = 상태.d.플랜고치기(p.id, g)
                if (오류 != null) { 상태.알림.토스트(오류); return@버튼 }
                // 적어 둔 현재 수행능력이 있으면 함께 (비었으면 그냥 넘어간다)
                val 넣은 = if (안함) null else 고친.현재수행넣기(p.id, 상태.오늘, 숫(측w), 숫(측r))
                끝남 = true
                상태.바꿈 { 넣은 ?: 고친 }
                발자취.적기("플랜 고침 · ${고친.플랜들.firstOrNull { it.id == p.id }?.이름 ?: p.이름}")
                닫기()
                상태.알림.토스트(if (넣은 != null) "저장했습니다 · 회차표를 다시 계산했습니다" else "저장했습니다")
            }, Modifier.weight(1f), 주요 = true)
            닫기단추(닫기)
        }
    }
    if (방식열림) 방식시트(방식번호, { n -> 방식번호 = n; 방식열림 = false }) { 방식열림 = false }
}

/** 고침 시트 이름표 — 15 굵게 · 글 색 (시안 `.고침표`) */
@Composable
private fun 고침표(글자: String) = 글(글자, 굵기 = FontWeight.Bold)

/** 이름표 줄 — 높이 28 (칩이 있어도 없어도 같은 높이 · 시안 `.고침표줄`) */
@Composable
private fun 고침표줄(content: @Composable RowScope.() -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 높이.아주낮게).padding(bottom = 간격.아주좁게), verticalAlignment = Alignment.CenterVertically, content = content)
}

/** [닫기] — 2px 빨간 테두리 · 빨간 글 (시안 v9 q3hz `.닫기단추`) */
@Composable
private fun 닫기단추(onClick: () -> Unit) {
    val c = Local색.current
    Box(
        Modifier.height(높이.높게).clip(RoundedCornerShape(모서리.작게)).background(c.면)
            .border(선굵기.굵게, c.나쁨, RoundedCornerShape(모서리.작게)).눌림(onClick).padding(horizontal = 간격.넓게),
        contentAlignment = Alignment.Center,
    ) { 글("닫기", 굵기 = FontWeight.Bold, 색 = c.나쁨) }
}
