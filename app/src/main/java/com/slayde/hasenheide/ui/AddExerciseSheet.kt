package com.slayde.hasenheide.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.slayde.hasenheide.data.같은이름번호
import com.slayde.hasenheide.data.칸
import com.slayde.hasenheide.data.종목
import com.slayde.hasenheide.data.플랜
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.글꼴
import com.slayde.hasenheide.ui.theme.높이
import com.slayde.hasenheide.ui.theme.모서리
import com.slayde.hasenheide.ui.theme.선굵기
import com.slayde.hasenheide.ui.theme.움직임
import com.slayde.hasenheide.ui.theme.이름맞춤값
import com.slayde.hasenheide.ui.theme.크기
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * 종목 넣기 시트 (시안 v18 D · v19 C `넣기목록` · v21) — 루틴 화면과 운동 화면이 **같이** 쓴다.
 *
 * ── 2단계 소유: RT(루틴) 도우미. 매개변수(이름 · 꼴)는 바꾸지 않는다 — 새 것은 기본값으로만 ──
 * W(운동) 도우미는 이 함수를 **부르기만** 한다.
 *
 * 10-08 홍겸 님: 이 시트의 목록은 **종목 탭 목록(11번)과 같은 화면**이다 — [종목묶음들](ExerciseScreen.kt)을 그대로 부른다.
 *  · 위끝 고정 · 머리 = 띠 · 띠 오른쪽 [확인](= 닫기) · 칩줄은 고정, 목록만 스크롤
 *  · 상자 오른쪽 끝 체크 상자 = 넣기(들어 있으면 하나 빼기 · 꾹 = 하나 더) · 들어간 상자는 강조옅음 바탕 + 강조 테
 *  · 상자를 누르면 펼침(종목 탭과 같다 — 기본 세팅 · 근육을 고치고 [저장]) · 꾹 눌러 끌면 순서 옮김
 *  · 오른쪽 아래 구석에 떠 있는 [+ 새 종목 만들기] → [새종목시트]. 저장하면 그 종목을 바로 [넣기] 로 넣고 칩 = [전체]
 *
 * @param 개수 종목 열쇠(`종목.id`) → 지금 들어 있는 개수 (✓×n). **누르는 순간에도 부른다** — 늘 지금 값을 돌려준다
 * @param 넣기 열쇠 하나를 넣는다 (꾹 = 한 번 더 부른다)
 * @param 빼기 열쇠 하나를 뺀다 (들어 있는 것을 누름)
 * @param 새종목만들기 true 면 [+ 새 종목 만들기]
 * @param 플랜개수 · 플랜넣기 · 플랜빼기 (10-05 RT · 기본 null) — 셋 다 넘기면 플랜 칸이 보인다(시안 '플랜넣기'). 열쇠 = 플랜 id
 */
@Composable
fun 종목넣기시트(
    상태: 앱상태,
    제목: String,
    개수: (열쇠: String) -> Int,
    넣기: (열쇠: String) -> Unit,
    빼기: (열쇠: String) -> Unit,
    닫기: () -> Unit,
    새종목만들기: Boolean = true,
    플랜개수: ((플랜id: String) -> Int)? = null,
    플랜넣기: ((플랜id: String) -> Unit)? = null,
    플랜빼기: ((플랜id: String) -> Unit)? = null,
    /** 10-08 홍겸 님: 책장 겹침 단계 — 이 시트의 단계(루틴 상세 안 = 2). 안의 새 종목 시트는 한 겹 더. 기본 0 = 운동 화면 */
    겹: Int = 0,
    /** 10-09 홍겸 님: 루틴 탭은 책장 겹침을 없앴다 → 안의 새 종목 시트도 0 으로 받는다 */
    새종목겹: Int = 겹 + 1,
) {
    var 새로 by remember { mutableStateOf(false) }
    // 시안 v19 C ① 열 때마다 [전체] — 시트를 새로 열면 처음 값 (새 종목 시트를 다녀와도 이 값은 남는다)
    var 칸고름 by remember { mutableStateOf(넣기전체) }
    if (새로) {
        // 시안 v18 C ⑤ 새 종목 시트는 넣기 시트를 '돌아감' 으로 — 닫거나 저장하면 넣기 시트로.
        // 10-06 ①: 저장했으면 그 종목을 바로 넣고(이 시트를 부른 쪽의 넣기) 칩 = [전체]
        새종목시트(상태, 닫기 = { 새로 = false }, 겹 = 새종목겹, 저장 = { e ->
            새로 = false
            발자취.적기("넣기 · 새 종목 넣기 · ${e.이름}")
            넣기(e.id)
            칸고름 = 넣기전체   // 알림은 새종목시트가 띄운 것(만들었습니다 / 근육 안내)을 그대로 둔다 — 덮으면 근육 안내가 사라진다
        })
    } else {
        val 플랜길 = if (플랜개수 != null && 플랜넣기 != null && 플랜빼기 != null) 플랜손(플랜개수, 플랜넣기, 플랜빼기) else null
        시트(제목, 닫기, 위끝고정 = true, 닫기글 = "확인", 겹 = 겹) {
            넣기속(상태, 칸고름, { 칸고름 = it }, 개수, 넣기, 빼기, 새종목만들기, { 새로 = true }, 플랜길)
        }
    }
}

/** 칩 맨 왼쪽 (시안 w0fo) */
internal const val 넣기전체 = "전체"

private class 플랜손(val 개수: (String) -> Int, val 넣기: (String) -> Unit, val 빼기: (String) -> Unit)

/**
 * 시트 속 (10-08 홍겸 님) — **종목 탭 목록(11번 화면)을 그대로** 쓴다: 칩줄 · 카테고리 이름표 · 2열 격자 · 펼침(12번 — 기본 세팅 고치기).
 * 다른 점은 상자마다 오른쪽 끝 체크 상자(누름 = 넣기 / 들어 있으면 하나 빼기 · 꾹 = 하나 더)와 들어간 상자의 강조옅음 바탕뿐.
 * 칩줄은 그대로 두고 목록만 스크롤한다 (시안 v18 D ⑤) — 공용 [시트] 는 속을 통째로 넘기므로 목록 칸의 높이를 여기서 잰다:
 * 시트 판 안쪽 높이 − 이 칸의 위끝 = 남은 자리. 재지 못하면(시트 모양이 바뀌면) 시트가 넘기는 대로 둔다.
 */
@Composable
private fun 넣기속(
    상태: 앱상태,
    칸고름: String,
    칸바꿈: (String) -> Unit,
    개수: (String) -> Int,
    넣기: (String) -> Unit,
    빼기: (String) -> Unit,
    새종목만들기: Boolean,
    새로열기: () -> Unit,
    플랜길: 플랜손?,
) {
    val d = 상태.d
    val 밀도 = LocalDensity.current
    var 칸높이 by remember { mutableStateOf<Dp?>(null) }
    var 못잼 by remember { mutableStateOf(false) }
    var 펼친 by remember { mutableStateOf<String?>(null) }
    val 판 = d.종목탭판(칸고름)
    // 꾹 눌러 끌어 종목 순서 바꾸기 — 종목 탭과 같은 판 (같은 카테고리 안 · 종목표 차례)
    val 끌판 = remember격자끌기판({ a, b, 뒤 ->
        발자취.적기("넣기 · 종목 순서 옮김")
        상태.바꿈 { it.copy(종목표 = 종목옮김(it.종목표, a, b, 뒤)) }
    })
    val 손 = 넣기손(상태, 개수, 넣기, 빼기, 플랜길)
    val 쓸 = 칸높이
    Box(
        Modifier.fillMaxWidth().이름칸밖누름()   // 10-08 홍겸 님: 이름 고치는 칸 밖을 누르면 저장
            .then(if (쓸 != null) Modifier.height(쓸) else Modifier)
            .onGloballyPositioned { co ->
                if (못잼) return@onGloballyPositioned
                // 이 칸 → 시트 속 칸(넘기는 칸) → 시트 판. 판 안쪽 높이에서 이 칸의 위끝을 빼면 남은 자리
                val 판틀 = co.parentLayoutCoordinates?.parentLayoutCoordinates
                if (판틀 == null) { 못잼 = true; return@onGloballyPositioned }
                val 남px = 판틀.size.height - 판틀.localPositionOf(co, Offset.Zero).y
                val h = with(밀도) { 남px.toDp() }
                if (h < 넣기값.최소높이 || h > 넣기값.최대높이) { 못잼 = true; return@onGloballyPositioned }
                val 옛 = 칸높이
                if (옛 == null || abs(h.value - 옛.value) >= 1f) 칸높이 = h
            },
    ) {
        Column(Modifier.fillMaxWidth().then(if (쓸 != null) Modifier.fillMaxHeight() else Modifier)) {
            칩줄(listOf(넣기전체) + 판.칸들, 판.고름, { 칸바꿈(it) })
            Spacer(Modifier.height(간격.좁게))
            // 칩을 바꾸면 맨 위부터 (시안 v18 D ⑤) · 칸을 눌러 다시 그려도 보던 자리 그대로
            key(판.고름) {
                val 넘김 = rememberScrollState()
                if (쓸 == null && !못잼) {
                    // 재는 중 — 한 번만 비워 둔다 (긴 목록을 먼저 그렸다가 줄이지 않게 · U5-6)
                    Spacer(Modifier.height(1.dp))
                } else Column(
                    Modifier.fillMaxWidth()
                        .then(if (쓸 != null) Modifier.weight(1f).격자끌기틀(끌판, 넘김).verticalScroll(넘김) else Modifier)
                        // 떠 있는 [새 종목] 단추 자리만큼 아래 여백 (시안 v19 C ②)
                        .padding(bottom = if (새종목만들기) 높이.낮게 + 간격.좁게 else 간격.좁게),
                    verticalArrangement = Arrangement.spacedBy(간격.좁게),
                ) {
                    종목묶음들(상태, 판, 끌판, 펼친, { k -> 펼친 = if (펼친 == k) null else k }, 손)
                }
            }
        }
        if (새종목만들기) {
            val 판톡 = remember루톡(true)
            버튼(
                "새 종목 만들기", { 판톡.톡(); 발자취.적기("넣기 · 새 종목 만들기"); 새로열기() },
                Modifier.align(Alignment.BottomEnd).루톡(판톡), 주요 = true, 낮게 = true, 그림 = 아이콘.더하기,
            )
        }
        격자끌기이름표(끌판)
    }
}

/**
 * 종목 탭 상자 → 넣기 (10-08). 플랜이 있는 종목(그 이름의 첫 종목 · 켠 플랜)은 플랜으로 넣는다(전: 플랜마다 칸 — 이제 상자 하나에 첫 플랜).
 * 플랜만 남은 종목('기타')은 플랜이 있을 때만 체크 상자. 개수는 누르는 순간의 값([상태.d])으로 센다
 */
private fun 넣기손(상태: 앱상태, 개수: (String) -> Int, 넣기: (String) -> Unit, 빼기: (String) -> Unit, 플랜길: 플랜손?): 종목넣기손 {
    fun 플(x: 종목칸값): List<플랜> = if (플랜길 == null) emptyList() else 상태.d.상자플랜(x).filter { it.켬 }
    return 종목넣기손(
        수 = { x ->
            val p = 플(x)
            if (p.isNotEmpty() && 플랜길 != null) p.sumOf { 플랜길.개수(it.id) } else x.종목?.let { 개수(it.id) } ?: 0
        },
        넣기 = { x ->
            val p = 플(x)
            발자취.적기("넣기 · ${x.이름}")
            if (p.isNotEmpty() && 플랜길 != null) 플랜길.넣기(p.first().id) else x.종목?.let { 넣기(it.id) }
        },
        빼기 = { x ->
            val p = 플(x)
            발자취.적기("넣기 빼기 · ${x.이름}")
            if (p.isNotEmpty() && 플랜길 != null) p.lastOrNull { 플랜길.개수(it.id) > 0 }?.let { 플랜길.빼기(it.id) }
            else x.종목?.let { 빼기(it.id) }
        },
        됨 = { x -> x.종목 != null || 플(x).isNotEmpty() },
        플랜카드 = 플랜길 != null,
    )
}

/** 빈 칸 안내 (시안 `.빈칸` — 속선 테 · 가운데 13 옅음) — 루틴 화면도 쓴다 */
@Composable
internal fun 넣기빈칸(글자: String, modifier: Modifier = Modifier) {
    val c = Local색.current
    val 모양 = RoundedCornerShape(모서리.작게)
    Box(
        modifier.fillMaxWidth().clip(모양).border(선굵기.보통, c.속선, 모양).padding(간격.넓게),
        contentAlignment = Alignment.Center,
    ) { 글(글자, 크기값 = 크기.조금작게, 색 = c.옅음, 가운데 = true, 줄 = 3) }   // 10-09: 빈 루틴 안내가 두 줄
}

/** 넣기 시트 값 (시안 CSS) — Theme 에 없는 것만 여기 모았다. 합칠 때 Theme.kt `부품치수` 로 옮긴다 */
internal object 넣기값 {
    val 번호폭 = 12.dp          // .넣기번 min-width
    val 체크 = 20.dp            // .넣기체크 20 × 20
    val 체크모서리 = 4.dp       // .넣기체크 border-radius
    val 딱지겹침 = (-5).dp      // 이름 오른쪽 위 5 겹침 (부품치수.딱지겹침 과 같은 값)
    val 최소높이 = 120.dp       // 잰 목록 높이가 이보다 작거나
    val 최대높이 = 2000.dp      // 크면 잘못 잰 것 — 시트가 넘기는 대로 둔다
    const val 말진하기 = 0.85f  // .넣기말 opacity
    val 연필폭 = 24.dp          // v22 ⑦ .넣기편집 누르는 칸 24 × 28
    val 연필그림 = 16.dp        // .넣기편집 svg 16
    // 10-08 홍겸 님 [11_UI지침에 올릴 값]
    const val 체크간격줄임 = 0.35f  // 접힌 줄: ∨ ↔ 체크 상자 눈에 보이는 간격(12)을 35% 줄인다 (→ 7.8)
    val 체크옮김 = 4.dp         // 접힌 줄: 체크 상자를 오른쪽으로 (띄어쓰기 한 칸)
}

// ═════════════════════ 순수 계산 (시험: ui/RoutineTest.kt) ═════════════════════

/**
 * 넣기 목록 한 칸. [키] = 종목 id(종목 칸) · [플랜id] = 플랜 칸. [번호] = 같은 이름 번호 딱지(0 = 없음).
 * [열쇠] = 화면에서 칸을 가르는 값 (말풍선 · 자리)
 */
internal data class 넣기칸(
    val 이름: String, val 키: String?, val 플랜id: String?, val 종목이름: String, val 번호: Int,
    /** v22 ⑦ 연필로 고칠 종목 id — 종목표에 없는 종목의 플랜은 null(연필 없음) */
    val 편집키: String? = null,
) {
    val 열쇠: String get() = if (플랜id != null) "p:$플랜id" else "e:$키"
}

/**
 * 넣기 목록 (시안 `넣기목록`) — 종목표 + 플랜. 칸 = 부위("전체" 면 모두).
 *  · 플랜이 있는 종목은 그 종목 칸 대신 플랜마다 한 칸 (같은 이름 종목이 여럿이면 먼저 만든 것에만)
 *  · [전체] 일 때는 종목표에 없는 종목의 플랜도 잃지 않게 칸을 낸다
 *  · 꺼 둔 플랜(켬 = false)은 넣지 않는다 (전 앱과 같게) · [플랜씀] = false 면 플랜 없이 종목만
 *  · 차례 = 종목표 차례 (10-07 홍겸 님: 꾹 눌러 끌어 바꾼다 · 종목 탭과 같다 · 전: 이름 가나다순). 종목표에 없는 플랜은 맨 뒤
 */
internal fun 넣기칸들(종목표: List<종목>, 플랜들: List<플랜>, 칸: String, 플랜씀: Boolean): List<넣기칸> {
    val 켠 = if (플랜씀) 플랜들.filter { it.켬 } else emptyList()
    val 줄 = mutableListOf<넣기칸>()
    for (x in 종목표) {
        if (칸 != 넣기전체 && x.칸 != 칸) continue
        val 첫 = 종목표.first { it.이름 == x.이름 } === x
        val 플 = if (첫) 켠.filter { it.종목 == x.이름 } else emptyList()
        if (플.isNotEmpty()) 플.forEach { p ->
            줄 += 넣기칸(p.이름, null, p.id, x.이름, if (p.이름 == x.이름) 같은이름번호(종목표, x.id, x.이름) else 0, 편집키 = x.id)
        }
        else 줄 += 넣기칸(x.이름, x.id, null, x.이름, 같은이름번호(종목표, x.id, x.이름), 편집키 = x.id)
    }
    if (칸 == 넣기전체) 켠.filter { p -> 종목표.none { it.이름 == p.종목 } }.forEach { p -> 줄 += 넣기칸(p.이름, null, p.id, p.종목, 0) }
    return 줄
}

/**
 * 넣기 이름이 넘쳤나 — 잘렸거나, 줄이 낱말 가운데서 바뀌었다(앞 글자 · 뒤 글자가 모두 띄어쓰기가 아님).
 * [줄끝] = 줄마다 끝 자리(다음 줄 첫 글자 번호)
 */
internal fun 넣기이름넘침(이름: String, 잘림: Boolean, 줄끝: List<Int>): Boolean =
    잘림 || 줄끝.dropLast(1).any { e -> e in 1 until 이름.length && !이름[e - 1].isWhitespace() && !이름[e].isWhitespace() }

/** 체크 상자 글 — 1~3 = ✓ 그 수만큼, 넷부터 ✓×n, 0 = "" (시안 v18 D ③) */
internal fun 체크글(n: Int): String = if (n <= 0) "" else if (n > 3) "✓×${n}" else "✓".repeat(n)
