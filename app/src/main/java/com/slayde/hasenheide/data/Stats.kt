package com.slayde.hasenheide.data

import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min

/**
 * 스탯 — '프린세스 메이커' 처럼 능력치가 얼마나 올랐는지 (12-2 · 12-1 ⑩ · 10-02 홍겸 님 "일단 다 업데이트")
 *
 * 근거: 스탯명세 1 · 2절 (claude.ai 프로젝트 문서 12-2 · 12-3 를 구현용으로 정리한 것).
 *  · 계산식은 거의 다 **[제안]** — 홍겸 님이 "나머지는 '일단' 다 좋아" 라고만 했다 (12-2 머리. 확정 아님)
 *  · 숫자 · 경계 · 문구는 전부 [스탯표] 한곳에 (플랜표와 같은 방식)
 *  · 값이 없으면 **0 이 아니라 null** — 화면엔 '—' 와 이유 한 줄
 *  · 누적 점수가 아니라 **그때그때 계산**한다. 하루 한 줄 [앱데이터.스탯기록] 에 남겨 7일 전 · 1년 전과 견준다
 *
 * 안드로이드 기능을 쓰지 않는다 → StatsTest 로 컴퓨터에서 시험한다.
 */

/** 이 스탯을 지금 계산할 수 있나 (스탯명세 7-1) */
enum class 스탯됨 { 지금, 일부, 나중 }

/**
 * 스탯 21개 — 화면 순서 그대로. 체력이 맨 앞 [확정 12-2].
 * 기록(스탯기록)에는 **enum 이름**(왼쪽)으로 남긴다 → 화면 이름(이름)을 바꿔도 지난 기록이 이어진다.
 */
enum class 스탯(val 이름: String, val 됨: 스탯됨) {
    체력("체력", 스탯됨.일부),
    근력("근력", 스탯됨.지금),
    수행능력("내 수행능력", 스탯됨.지금),
    밀기("밀기", 스탯됨.지금),
    당기기("당기기", 스탯됨.지금),
    하체("하체", 스탯됨.지금),
    폭발력("폭발력", 스탯됨.나중),
    근지구력("근지구력", 스탯됨.지금),
    심폐지구력("심폐지구력", 스탯됨.나중),
    코어("코어", 스탯됨.나중),
    체성분("체성분", 스탯됨.나중),
    균형("균형", 스탯됨.지금),
    다양성("다양성", 스탯됨.지금),
    성실("성실", 스탯됨.일부),
    /**
     * ★ 이름 미정 — '힘을 나타내는 말로' [확정 12-2] · 후보 **저력 · 기력 · 완력** [제안] → [홍겸 님 확인].
     * 정해지면 이 줄의 "작업량" 만 바꾼다 (기록은 enum 이름 '작업량' 으로 남아 이어진다)
     */
    작업량("작업량", 스탯됨.지금),
    회복력("회복력", 스탯됨.나중),
    의지력("의지력", 스탯됨.일부),
    목표진척도("목표 진척도", 스탯됨.지금),
    나이성별("나이 · 성별 대비", 스탯됨.지금),
    성장("처음 대비 성장", 스탯됨.지금),
    /** 마음 쪽 스탯 — 낮을수록 좋다. 오르내림 값을 대화로 정하기 전까지 계산하지 않는다 (스탯명세 3절) */
    스트레스("스트레스", 스탯됨.나중),
}

/**
 * 스탯 하나의 값.
 * @param 값 0 ~ 100. null 이면 화면에 '—' 와 [이유]
 * @param 덧글 값 옆에 붙는 짧은 글 (처음 대비 성장의 '+N%')
 */
data class 스탯값(val 스탯: 스탯, val 값: Double?, val 이유: String? = null, val 덧글: String? = null)

/** 스탯의 숫자 · 경계 · 문구 — 전부 여기 (스탯명세 1 · 2절. [제안] · [내 생각] 은 그 절의 표시를 따른다) */
object 스탯표 {
    const val 최대 = 100.0

    /** 근력 — 3대가 이만큼이면 100 (업적 1-6 '3대 700' 과 맞춤) [내 생각] */
    const val 근력만점 = 700.0
    /** 내 수행능력 — DOTS 를 이 값으로 나눈다 (DOTS 500 = 100) [내 생각] */
    const val DOTS나눔 = 5.0
    /** 스탯에 쓰는 1RM · 최다 연속 — 최근 며칠 안의 최고 [내 생각] (업적은 모든 기록의 최고) */
    const val 최근일 = 90
    /** 다양성 — 최근 며칠 · 종목 수 상한 · 부위 수 (기본카테고리 6) */
    const val 다양성일 = 28
    const val 다양성종목 = 12
    const val 다양성부위 = 6
    /** 성실 — 최근 며칠의 실행률 (복용률은 보충제 기능 뒤에) */
    const val 성실일 = 28
    /** 작업량 — 최근 며칠의 회당 평균 볼륨 ÷ 체중. 회당 볼륨이 체중의 이 배수면 100 [내 생각] */
    const val 작업량일 = 28
    const val 작업량만점배 = 100.0
    /** 의지력 — 최근 며칠 · 측정 세트 한 날 점수 · 기록 갱신한 날 점수 */
    const val 의지력일 = 90
    const val 측정점 = 5.0
    const val 갱신점 = 2.0
    /** 처음 대비 성장 — '지금' 은 최근 며칠의 최고 */
    const val 성장일 = 90
    /** 맨몸 정자세 — 체중 × 맨몸비 의 이만큼 이상이면 정자세 (5% 는 체중이 바뀐 몫, 스탯명세 2-3) */
    const val 정자세여유 = 0.95
    /** 화면의 '▲ +n' — 며칠 전과 견주나 */
    const val 비교일 = 7
    /** 체중을 이 시간 안에 또 고치면 한 줄로 친다 — 칸에 109 를 치는 동안 1 · 10 · 109 가 따로 남지 않게 */
    const val 체중묶음ms = 120_000L

    /** 이름 묶기 (스탯명세 2-1) — Names.kt 추천 이름과 플랜 종목 이름이 다르다. 친업 · 어시스트 풀업은 묶지 않는다 */
    val 별칭: Map<String, String> = mapOf("스쿼트" to "백 스쿼트", "풀업" to "턱걸이", "푸시업" to "팔굽혀펴기")
    /** 플랜 종목의 부위 — 종목표 · 추천 목록에 없을 때. 데드리프트는 Names.kt 처럼 '등' (스탯명세 8-10) */
    val 플랜부위: Map<String, String> = mapOf(
        "벤치프레스" to "가슴", "백 스쿼트" to "하체", "데드리프트" to "등", "오버헤드 프레스" to "어깨",
        "펜들레이 로우" to "등", "턱걸이" to "등", "팔굽혀펴기" to "가슴", "맨몸 스쿼트" to "하체",
    )

    val 삼대 = listOf("벤치프레스", "백 스쿼트", "데드리프트")
    val 밀기종목 = listOf("벤치프레스", "오버헤드 프레스")
    val 당기기종목 = listOf("펜들레이 로우", "턱걸이")
    /** 데드리프트는 부위로는 '등' 이지만 스탯 '하체' 에는 넣는다 [제안] (스탯명세 8-10) */
    val 하체종목 = listOf("백 스쿼트", "데드리프트")
    val 근지구력종목 = listOf("팔굽혀펴기", "맨몸 스쿼트")
    /** 체력 = 이 스탯들의 평균 — 값 있는 것만 [제안] */
    val 체력재료 = listOf(스탯.밀기, 스탯.당기기, 스탯.하체, 스탯.폭발력, 스탯.근지구력, 스탯.심폐지구력, 스탯.코어)
    /** 나이 · 성별 대비 = 이 스탯들의 평균 ÷ 나이배수 [제안] */
    val 나이재료 = listOf(스탯.밀기, 스탯.당기기, 스탯.하체, 스탯.근지구력)
    /** 균형 = 100 − (가장 높은 것 − 가장 낮은 것) — 부위 균형 [제안] (뜻 미정: 한 발 서기일 수도) */
    val 균형재료 = listOf(스탯.밀기, 스탯.당기기, 스탯.하체)

    // ── DOTS (스탯명세 2-5 · 출처 두 곳 대조 일치 — inchcalculator · benchflow SKILL.md) ──
    val DOTS남: List<Double> = listOf(-307.75076, 24.0900756, -0.1918759221, 0.0007391293, -0.000001093)
    val DOTS여: List<Double> = listOf(-57.96288, 13.6175032, -0.1126655495, 0.0005158568, -0.0000010706)
    const val DOTS체중아래 = 40.0
    const val DOTS남체중위 = 210.0
    const val DOTS여체중위 = 150.0

    // ── 값이 없을 때 보이는 이유 한 줄 ──
    const val 이유체중 = "설정에서 체중을 넣어 주세요"
    const val 이유나이 = "설정에서 나이를 넣어 주세요"
    const val 이유기록 = "아직 기록이 없습니다"
    const val 이유최근 = "최근 4주 기록이 없습니다"
    const val 이유삼대 = "벤치 · 스쿼트 · 데드 기록이 없습니다"
    const val 이유밀기 = "벤치 · 오버헤드 프레스 기록이 없습니다"
    const val 이유당기기 = "펜들레이 로우 · 턱걸이 기록이 없습니다"
    const val 이유하체 = "스쿼트 · 데드리프트 기록이 없습니다"
    const val 이유근지구력 = "팔굽혀펴기 · 맨몸 스쿼트 기록이 없습니다"
    const val 이유힘 = "밀기 · 당기기 · 하체 · 근지구력이 아직 없습니다"
    const val 이유균형 = "밀기 · 당기기 · 하체 중 둘이 필요합니다"
    const val 이유목표 = "켜 둔 플랜이나 1RM 목표가 없습니다"
    const val 이유성장 = "같은 종목을 두 날 이상 해야 합니다"
    val 이유나중: Map<스탯, String> = mapOf(
        스탯.폭발력 to "인상 · 용상 · 클린을 정한 뒤에 계산합니다",
        스탯.심폐지구력 to "유산소 시간 · 거리 기록이 필요합니다",
        스탯.코어 to "플랭크 시간 기록이 필요합니다",
        스탯.체성분 to "인바디가 필요합니다",
        스탯.회복력 to "안정시 심박 · 수면 기록이 필요합니다",
        스탯.스트레스 to "준비 중 — 오르내림 값을 정하고 있습니다",
    )
}

// ─────────────── 공통 계산 (스탯 · 업적이 같이 쓴다 · 스탯명세 2절) ───────────────

/** 2-1 종목 이름 묶기 — 플랜 줄이면 그 플랜의 바탕 종목, 아니면 별칭표, 아니면 이름 그대로 */
fun 앱데이터.정식이름(e: 종목기록): String =
    e.플랜id?.let { id -> 플랜들.firstOrNull { it.id == id }?.종목 } ?: 스탯표.별칭[e.이름] ?: e.이름

/** 맨몸 3종(횟수로 진행하는 종목)인가 — 이 종목들은 1RM 대신 정자세 최다 연속으로 본다 */
fun 맨몸횟수종목(정식: String): Boolean = 플랜표.찾기(정식)?.횟수진행 == true

/**
 * 2-2 세트 하나의 1RM — 워밍업(종류 1) 뺌 · 측정(종류 2) 포함 · **1 ≤ r ≤ 10 세트만** (플랜표.환산최대횟수).
 * 쓸 수 없는 세트면 null
 */
fun 스탯RM(s: 세트): Double? =
    if (s.종류 == 세트종류.워밍업 || s.r < 1 || s.r > 플랜표.환산최대횟수) null else 일RM(s.w, s.r)

/** 2-3 맨몸 정자세 — 무게 0(루틴 기록) 이거나 체중 × 맨몸비 × 0.95 이상. 어시스트 세트(체중 − 보조)는 빠진다 */
fun 정자세(s: 세트, 맨몸비: Double, 체중: Double): Boolean = s.w == 0.0 || s.w >= 체중 * 맨몸비 * 스탯표.정자세여유

/**
 * 기록 하나에서 종목(정식이름)마다 가장 높은 1RM.
 * **맨몸 3종은 넣지 않는다** — 맨몸 세트의 무게는 체중(유효 부하)이라 1RM 이 체중과 같게 나온다 (업적 2-25 가 턱걸이 한 번으로 풀린다)
 */
fun 앱데이터.날RM들(rec: 날기록): Map<String, Double> {
    val m = HashMap<String, Double>()
    rec.종목들.forEach { e ->
        val 이름 = 정식이름(e)
        if (맨몸횟수종목(이름)) return@forEach
        e.세트들.forEach { s -> 스탯RM(s)?.let { v -> if (v > (m[이름] ?: 0.0)) m[이름] = v } }
    }
    return m
}

/** 기록 하나에서 맨몸 3종(정식이름)마다 정자세 최다 연속 횟수. 그 종목을 했으면 정자세가 없어도 0 으로 넣는다 */
fun 앱데이터.날최다들(rec: 날기록): Map<String, Int> {
    val m = HashMap<String, Int>()
    rec.종목들.forEach { e ->
        val 이름 = 정식이름(e)
        val 표 = 플랜표.찾기(이름)?.takeIf { it.횟수진행 } ?: return@forEach
        val 세트들 = 기록세트(e.세트들)
        if (세트들.isEmpty()) return@forEach
        val 최다 = 세트들.filter { 정자세(it, 표.맨몸비, 몸.체중) }.maxOfOrNull { it.r } ?: 0
        m[이름] = max(m[이름] ?: 0, 최다)
    }
    return m
}

/**
 * 종목의 부위 — 종목표(사용자가 정한 것) → 추천 목록(Names.kt) → 플랜 종목 표 → 이름으로 추측.
 * 데드리프트는 '등' (Names.kt 그대로 · 스탯명세 8-10)
 */
fun 앱데이터.부위찾기(e: 종목기록): String? {
    val 정식 = 정식이름(e)
    return 종목표.firstOrNull { it.이름 == e.이름 }?.부위?.takeIf { it.isNotBlank() }
        ?: 종목표.firstOrNull { it.이름 == 정식 }?.부위?.takeIf { it.isNotBlank() }
        ?: 이름추천.기본.firstOrNull { it.이름 == e.이름 || it.이름 == 정식 }?.부위
        ?: 스탯표.플랜부위[정식]
        ?: 이름추천.추측하기(e.이름).부위
}

/**
 * 2-4 종목점수 (0 ~ 100) — 경계 넷 b0 < b1 < b2 < b3 (플랜표 그대로).
 * 입문 0~20 · 초급 20~40 · 중급 40~60 · 고급 60~80 · 상급 80~100 → 플랜의 수준 판정과 어긋나지 않는다
 */
fun 종목점수(x: Double, 경계: List<Double>): Double {
    if (경계.size < 4 || x <= 0.0) return 0.0
    if (x < 경계[0]) return 20 * x / 경계[0]
    for (i in 0..2) if (x < 경계[i + 1]) return 20.0 * (i + 1) + 20 * (x - 경계[i]) / (경계[i + 1] - 경계[i])
    return min(스탯표.최대, 80 + 20 * (x - 경계[3]) / (경계[3] - 경계[2]))
}

/** 2-5 DOTS — 체중을 남 40~210 · 여 40~150 으로 자른다 */
fun DOTS(합계: Double, 체중: Double, 남: Boolean): Double {
    val k = if (남) 스탯표.DOTS남 else 스탯표.DOTS여
    val w = 체중.coerceIn(스탯표.DOTS체중아래, if (남) 스탯표.DOTS남체중위 else 스탯표.DOTS여체중위)
    val 아래 = k[0] + k[1] * w + k[2] * w * w + k[3] * w * w * w + k[4] * w * w * w * w
    return if (아래 <= 0) 0.0 else 합계 * 500 / 아래
}

/** 날 ~ n 일 전 (날 포함 n 일) 의 첫날 */
private fun 부터(날: String, n: Int): String = 날더하기(날, -(n - 1))

/** 이 날까지의 앱데이터 — 7일 전 스탯을 다시 계산할 때 (기록 · 미실시만 자른다. 몸 · 플랜은 지금 값) */
fun 앱데이터.까지(날: String): 앱데이터 = copy(
    기록 = 기록.filterKeys { 날짜만(it) <= 날 },
    미실시 = 미실시.filterKeys { it <= 날 },
)

/** 최근 n 일 안(오늘 포함)에서 종목(정식이름)의 가장 높은 1RM · 맨몸 최다 연속 */
private class 최근값(val rm: Map<String, Double>, val 최다: Map<String, Int>)

private fun 앱데이터.최근값들(오늘: String, n: Int): 최근값 {
    val 첫 = 부터(오늘, n)
    val rm = HashMap<String, Double>(); val 최다 = HashMap<String, Int>()
    기록.forEach { (k, r) ->
        val d = 날짜만(k)
        if (d < 첫 || d > 오늘) return@forEach
        날RM들(r).forEach { (e, v) -> if (v > (rm[e] ?: 0.0)) rm[e] = v }
        날최다들(r).forEach { (e, v) -> if (v >= (최다[e] ?: -1)) 최다[e] = v }
    }
    return 최근값(rm, 최다)
}

/** 2-4 종목 하나의 점수 — 최근 90일 기록으로. 기록이 없거나(무게 종목은) 체중이 없으면 null */
fun 앱데이터.종목스탯점수(종목: String, 오늘: String): Double? = 종목점수0(종목, 최근값들(오늘, 스탯표.최근일))

private fun 앱데이터.종목점수0(종목: String, 값: 최근값): Double? {
    val 표 = 플랜표.찾기(종목) ?: return null
    if (표.횟수진행) {
        val r = 값.최다[종목] ?: return null
        return 종목점수(r.toDouble(), 표.횟수경계.map { it.toDouble() })
    }
    if (몸.체중 <= 0) return null
    val rm = 값.rm[종목] ?: return null
    return 종목점수(rm / 몸.체중, if (몸.남) 표.경계남 else 표.경계여)
}

private fun 평균(l: List<Double?>): Double? = l.filterNotNull().takeIf { it.isNotEmpty() }?.average()

/**
 * 스탯 21개 — 화면 순서 그대로 (스탯명세 1절).
 * '나중' 5개 · 스트레스는 계산하지 않는다 (값 null + 이유).
 */
fun 앱데이터.스탯들(오늘: String): List<스탯값> {
    val 최근 = 최근값들(오늘, 스탯표.최근일)
    fun 점(e: String) = 종목점수0(e, 최근)
    fun 평균점(l: List<String>) = 평균(l.map { 점(it) })
    val 값 = LinkedHashMap<스탯, 스탯값>()
    fun 넣(s: 스탯, v: Double?, 이유: String?, 덧글: String? = null) {
        값[s] = 스탯값(s, v?.coerceIn(0.0, 스탯표.최대), if (v == null) 이유 else null, 덧글)
    }
    // 무게 종목은 체중이 있어야 점수가 나온다 — 이유를 체중으로 알린다
    fun 힘이유(기본: String) = if (몸.체중 <= 0) 스탯표.이유체중 else 기본

    // 근력 · 내 수행능력 — 3대 = 종목마다 최근 90일 최고의 합
    val 삼 = 스탯표.삼대.mapNotNull { 최근.rm[it] }
    val 삼대 = 삼.sum()
    넣(스탯.근력, if (삼.isEmpty()) null else min(스탯표.최대, 삼대 / (스탯표.근력만점 / 스탯표.최대)), 스탯표.이유삼대)
    넣(스탯.수행능력, if (삼.isEmpty() || 몸.체중 <= 0) null else min(스탯표.최대, DOTS(삼대, 몸.체중, 몸.남) / 스탯표.DOTS나눔),
        if (몸.체중 <= 0) 스탯표.이유체중 else 스탯표.이유삼대)
    넣(스탯.밀기, 평균점(스탯표.밀기종목), 힘이유(스탯표.이유밀기))
    넣(스탯.당기기, 평균점(스탯표.당기기종목), 힘이유(스탯표.이유당기기))
    넣(스탯.하체, 평균점(스탯표.하체종목), 힘이유(스탯표.이유하체))
    넣(스탯.근지구력, 평균점(스탯표.근지구력종목), 스탯표.이유근지구력)
    listOf(스탯.폭발력, 스탯.심폐지구력, 스탯.코어, 스탯.체성분, 스탯.회복력).forEach { 넣(it, null, 스탯표.이유나중[it]) }

    // 균형 — 둘 이상 있을 때
    val 균 = 스탯표.균형재료.mapNotNull { 값[it]?.값 }
    넣(스탯.균형, if (균.size >= 2) 스탯표.최대 - (균.max() - 균.min()) else null, 스탯표.이유균형)

    // 다양성 — 최근 28일 부위 수 · 종목 수
    val 첫28 = 부터(오늘, 스탯표.다양성일)
    val 최근기록 = 기록.filterKeys { 날짜만(it) in 첫28..오늘 }.values
    val 부위들 = 최근기록.flatMap { r -> r.종목들.mapNotNull { 부위찾기(it) } }.filter { it in 앱데이터.기본카테고리 }.toSet()
    val 종목들 = 최근기록.flatMap { r -> r.종목들.map { 정식이름(it) } }.toSet()
    넣(스탯.다양성, if (기록.isEmpty()) null
        else 50.0 * 부위들.size / 스탯표.다양성부위 + 50.0 * min(종목들.size, 스탯표.다양성종목) / 스탯표.다양성종목, 스탯표.이유기록)

    // 성실 — 실행률 = 운동날 ÷ (운동날 + 미실시날). 보충제 복용률은 나중
    val 첫성실 = 부터(오늘, 스탯표.성실일)
    val 운동날수 = 기록.keys.map { 날짜만(it) }.filter { it in 첫성실..오늘 }.toSet().size
    val 미실시수 = 미실시.keys.filter { it in 첫성실..오늘 && it !in 기록 }.size
    넣(스탯.성실, if (운동날수 + 미실시수 == 0) null else 100.0 * 운동날수 / (운동날수 + 미실시수), 스탯표.이유최근)

    // 작업량(이름 미정) — 최근 28일 회당 평균 볼륨 ÷ 체중
    val 첫작업 = 부터(오늘, 스탯표.작업량일)
    val 회볼륨 = 기록.filterKeys { 날짜만(it) in 첫작업..오늘 }.values.map { r -> 볼륨(r.종목들.flatMap { it.세트들 }) }
    넣(스탯.작업량, if (회볼륨.isEmpty() || 몸.체중 <= 0) null else min(스탯표.최대, 회볼륨.average() / 몸.체중 / 스탯표.작업량만점배 * 스탯표.최대),
        if (몸.체중 <= 0) 스탯표.이유체중 else 스탯표.이유최근)

    // 의지력(일부) — 최근 90일: 측정 세트 한 날 × 5 + 기록 갱신한 날 × 2 (컨디션 칩 '가기 싫은데 간 날' 은 나중)
    val 첫의지 = 부터(오늘, 스탯표.의지력일)
    val 의지열쇠 = 기록.keys.filter { 날짜만(it) in 첫의지..오늘 }
    val 측정날 = 의지열쇠.filter { k -> 기록[k]!!.종목들.any { e -> e.세트들.any { it.종류 == 세트종류.측정 } } }.map { 날짜만(it) }.toSet().size
    val 갱신날 = 의지열쇠.filter { 기록갱신(it).isNotEmpty() }.map { 날짜만(it) }.toSet().size
    넣(스탯.의지력, if (기록.isEmpty()) null else min(스탯표.최대, 스탯표.측정점 * 측정날 + 스탯표.갱신점 * 갱신날), 스탯표.이유기록)

    // 목표 진척도 — 켜진 플랜마다 (지금 − 시작) ÷ (목표 − 시작), 종목 1RM 목표도 같은 식 → 평균 × 100
    val 진척 = mutableListOf<Double>()
    플랜들.filter { it.켬 }.forEach { p ->
        val 폭 = p.목표진행값 - p.시작진행값
        if (폭 > 0) 진척 += ((p.지금진행값 - p.시작진행값) / 폭).coerceIn(0.0, 1.0)
    }
    종목표.filter { (it.목표1RM ?: 0.0) > 0 }.forEach { e ->
        val 날별 = 기록.filterKeys { 날짜만(it) <= 오늘 }.toSortedMap().mapNotNull { (_, r) ->
            r.종목들.filter { it.이름 == e.이름 }.flatMap { it.세트들 }.mapNotNull { 스탯RM(it) }.maxOrNull()
        }
        val 시작 = 날별.firstOrNull() ?: return@forEach
        val 폭 = e.목표1RM!! - 시작
        if (폭 > 0) 진척 += ((날별.max() - 시작) / 폭).coerceIn(0.0, 1.0)
    }
    넣(스탯.목표진척도, if (진척.isEmpty()) null else 진척.average() * 100, 스탯표.이유목표)

    // 나이 · 성별 대비 — 평균(밀기, 당기기, 하체, 근지구력) ÷ 나이배수. 성별은 종목점수가 이미 반영
    val 나이평균 = 평균(스탯표.나이재료.map { 값[it]?.값 })
    넣(스탯.나이성별, if (몸.나이 <= 0 || 나이평균 == null) null else min(스탯표.최대, 나이평균 / 플랜표.나이배수(몸.나이)),
        if (몸.나이 <= 0) 스탯표.이유나이 else 스탯표.이유힘)

    // 처음 대비 성장 — 종목마다 최근 90일 최고 ÷ 첫 기록일 최고 − 1, 기록일 2개 이상인 종목 평균
    val 첫날RM = HashMap<String, Pair<String, Double>>()   // 종목 → (첫 날, 그 날 최고)
    val 날수 = HashMap<String, MutableSet<String>>()
    기록.keys.filter { 날짜만(it) <= 오늘 }.sorted().forEach { k ->
        val d = 날짜만(k)
        날RM들(기록[k]!!).forEach { (e, v) ->
            if (v <= 0) return@forEach
            날수.getOrPut(e) { mutableSetOf() }.add(d)
            val 첫 = 첫날RM[e]
            if (첫 == null) 첫날RM[e] = d to v else if (첫.first == d && v > 첫.second) 첫날RM[e] = d to v
        }
    }
    val 성장들 = 첫날RM.mapNotNull { (e, 첫) ->
        if ((날수[e]?.size ?: 0) < 2) return@mapNotNull null
        val 지금 = 최근.rm[e] ?: return@mapNotNull null
        지금 / 첫.second - 1
    }
    val 성장평균 = 성장들.takeIf { it.isNotEmpty() }?.average()?.times(100)
    넣(스탯.성장, 성장평균, 스탯표.이유성장, 성장평균?.let { (if (it >= 0) "+" else "") + "${Math.round(it)}%" })

    // 체력 — 힘 쪽 평균 (심폐 · 코어 · 폭발력은 나중이라 지금은 빠진다)
    넣(스탯.체력, 평균(스탯표.체력재료.map { 값[it]?.값 }), 스탯표.이유힘)
    넣(스탯.스트레스, null, 스탯표.이유나중[스탯.스트레스])

    return 스탯.entries.map { 값[it] ?: 스탯값(it, null, 스탯표.이유기록) }
}

/** 값 있는 스탯만 — 스탯기록에 남길 모양 (enum 이름 → 값) */
fun 앱데이터.스탯맵(오늘: String): Map<String, Double> =
    스탯들(오늘).mapNotNull { s -> s.값?.takeIf { it.isFinite() }?.let { s.스탯.name to it } }.toMap()

/**
 * 하루 한 줄 남기기 — 그 날의 마지막 값으로 덮는다 (앱 켤 때 · 날짜 바뀔 때 · 운동 저장 뒤).
 * 같으면 그대로 돌려준다 (저장을 다시 하지 않게)
 */
fun 앱데이터.스탯기록남김(오늘: String): 앱데이터 {
    val 지금 = 스탯맵(오늘)
    if (스탯기록[오늘] == 지금) return this
    return copy(스탯기록 = 스탯기록 + (오늘 to 지금))
}

/**
 * [일] 전의 스탯 — 스탯기록에 그 날(또는 그 앞 가장 가까운 날)이 있으면 그것, 없으면 그 날까지의 기록으로 다시 계산.
 * 화면의 '▲ +n' (7일 전 대비 · 12-1 ⑩ '얼마나 올랐는지 꼭')
 */
fun 앱데이터.스탯전(오늘: String, 일: Int = 스탯표.비교일): Map<String, Double> {
    val 그날 = 날더하기(오늘, -일)
    val 있는 = 스탯기록.keys.filter { it <= 그날 }.maxOrNull()
    if (있는 != null) return 스탯기록[있는]!!
    if (기록.keys.none { 날짜만(it) <= 그날 }) return emptyMap()
    return 까지(그날).스탯맵(그날)
}

/**
 * 체중을 바꿀 때 한 줄 남긴다 (설정 '체중' 칸).
 *  · [스탯표.체중묶음ms] 안에 또 바꾸면 마지막 줄을 고친다 — 칸에 치는 동안 1 · 10 · 109 가 따로 남지 않게
 *  · 직전 줄과 같은 값이면 남기지 않는다 · 0 이하는 남기지 않는다
 */
fun 앱데이터.체중기록잇기(kg: Double, 지금: Long): 앱데이터 {
    if (kg <= 0) return this
    val 끝 = 체중기록.lastOrNull()
    val 새 = if (끝 != null && 지금 - 끝.시각 in 0 until 스탯표.체중묶음ms) 체중기록.dropLast(1) + 체중값(지금, kg)
             else 체중기록 + 체중값(지금, kg)
    // 고친 줄이 그 앞 줄과 같아지면 지운다 (109 → 1 → 10 → 109 로 다시 친 것)
    val 정리 = if (새.size >= 2 && 새[새.size - 1].kg == 새[새.size - 2].kg) 새.dropLast(1) else 새
    return if (정리 == 체중기록) this else copy(체중기록 = 정리)
}

/** 날짜("2026-10-02") 의 0시 — 폰 시간대. 업적 달성 시각을 날짜로만 알 때 */
fun 날시각(날: String): Long = LocalDate.parse(날짜만(날)).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

/** 시각 → 날짜 글 ("2026-10-02") — 폰 시간대 */
fun 시각날(ms: Long): String = java.time.Instant.ofEpochMilli(ms).atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString()
