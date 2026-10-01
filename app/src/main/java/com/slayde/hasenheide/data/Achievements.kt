package com.slayde.hasenheide.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max

/**
 * 업적 · 칭호 판정 (10-02 · 12-5 ~ 12-10 · 스탯명세 5 · 6절)
 *
 * ── 무엇이 어디에 ──
 *  · 업적 107개의 글(이름 · 칭호 · 문구 · 조건 · 플레이버) = AchievementData.kt 의 [업적표] — CSV 에서 **만든** 파일
 *  · 조건을 채웠나 = 이 파일의 [업적판정법] (손으로 쓴 것). 문턱 숫자(200 · 300 …)도 여기 한곳에
 *
 * ── 규칙 (스탯명세 5절) ──
 *  · 판정 시점: 운동 저장 직후 · 앱 켤 때 · 날짜 바뀔 때 · 앱 동작(세기)을 셀 때
 *  · **한 번 얻으면 취소 안 함** — 기록을 지워도, 체중이 바뀌어도 [앱데이터.업적] 에서 빼지 않는다
 *  · 처음 넣을 때 **지난 기록으로 소급** — 달성일 = 조건을 처음 채운 기록의 날
 *    (세션으로만 보는 2-33 · 앱 동작 횟수는 소급하지 않는다)
 *  · 진행 막대: 판정식이 `X >= N` 꼴이면 X ÷ N ([업적진행])
 *
 * ── 번호판 (12-10) ──  업적 1-n · 히든 2-n · 칭호 3-n · 히든칭호 4-n. 1-n 을 얻으면 칭호 3-n
 *
 * 안드로이드 기능을 쓰지 않는다 → StatsTest 로 컴퓨터에서 시험한다.
 */

/** 업적 하나의 글 — AchievementData.kt 가 이 모양으로 107개를 만든다 */
data class 업적(
    val 번호: String,
    val 이름: String,
    val 칭호번호: String,
    val 칭호: String,
    /** 칭호 한 줄 문구 (12-10 따옴표 안 글) */
    val 문구: String,
    val 분류: String,
    /** 초보 · 중급 · 고급 · 미친자 · 유머. 히든은 "" (등급 없음) */
    val 등급: String,
    val 숨김: Boolean,
    /** 일반 설명 */
    val 조건: String,
    /** 전설 조각 — 흐리고 작게 (12-6 · 12-7). 대부분 아직 비어 있다 */
    val 플레이버: String,
    /** CSV 의 '지금 구현 가능' — 예 · 새 칸 · 나중 (만들 때의 표시. 실제로 판정하는지는 [판정됨]) */
    val 구현가능: String,
)

/** 이 업적을 지금 판정하나 — [업적판정법] 에 있으면 */
val 업적.판정됨: Boolean get() = 번호 in 업적판정법

/** 앱 동작 횟수의 이름 — [앱데이터.세기] 의 열쇠 (스탯명세 7-2) */
object 세기이름 {
    /** 휴식 '건너뛰기' 를 잇달아 누른 수 — 휴식이 끝까지 돌면 0 (2-17) */
    const val 연속건너뜀 = "연속건너뜀"
    /** 설정 탭을 연 수 (2-46) */
    const val 설정진입 = "설정진입"
    /** 백업 내보내기에 성공한 수 (2-47) */
    const val 백업 = "백업"
    /** 루틴 이름을 바꿔 저장한 수 — 모든 루틴 합 · 플랜 이름은 안 센다 (2-45) */
    const val 루틴이름바꿈 = "루틴이름바꿈"
}

fun 앱데이터.세기더함(이름: String, n: Int = 1): 앱데이터 = copy(세기 = 세기 + (이름 to (세기[이름] ?: 0) + n))
fun 앱데이터.세기0(이름: String): 앱데이터 = if ((세기[이름] ?: 0) == 0) this else copy(세기 = 세기 + (이름 to 0))

/** 건너뛴 날을 남긴다 — 캘린더 '이 루틴 건너뛰기' 바로 앞에서 부른다 (2-11) */
fun 앱데이터.건너뜀남김(날: String): 앱데이터 = 예정[날]?.let { copy(건너뜀 = 건너뜀 + (날 to it)) } ?: this

/** 옛 파일에 체중은 있는데 체중기록이 비어 있으면 지금 값으로 첫 줄을 남긴다 — 감량 업적(1-47 · 1-48)의 출발점 */
fun 앱데이터.체중기록시작(지금: Long): 앱데이터 =
    if (체중기록.isEmpty() && 몸.체중 > 0) copy(체중기록 = listOf(체중값(지금, 몸.체중))) else this

/** 0.1 단위 반올림 — '정확히 100.0kg' 같은 비교에 (스탯명세 6절) */
fun 반올림0_1(x: Double): Double = Math.round(x * 10) / 10.0

/** 업적 글 · 숫자 — 한곳에 */
object 업적글 {
    const val 달성 = "업적 달성"
    const val 숨은이름 = "???"
    const val 준비중 = "준비 중"
    /** 등급 없는(히든) 업적의 표시 이름 */
    const val 히든 = "히든"
    /** 진행 막대를 그릴 때 X ≥ N 을 비교하는 여유 (소수 오차) */
    const val 여유 = 1e-6
    fun 알림(번호들: List<String>): String {
        val 첫 = 번호들.firstOrNull()?.let { 업적표.찾기(it) } ?: return 달성
        val 나머지 = 번호들.size - 1
        return if (나머지 == 0) "$달성 · 칭호 『${첫.칭호}』" else "$달성 ${번호들.size}개 · 칭호 『${첫.칭호}』 외 ${나머지}개"
    }
}

// ─────────────── 판정 재료 — 기록을 한 번 훑어 둔다 ───────────────

/**
 * 판정에 쓰는 것을 미리 계산해 둔 것. 기록은 열쇠 순(= 날짜 순, 같은 날 '한 번 더' 는 뒤)으로 0 … n-1 번.
 * 'i 까지' = 0 … i 번 기록만 본 것 (소급할 때 그 순간의 상태)
 */
class 판정자료(val d: 앱데이터, val 오늘: String) {
    val 열쇠: List<String> = d.기록.keys.sorted()
    val 기록들: List<날기록> = 열쇠.map { d.기록.getValue(it) }
    val n: Int get() = 열쇠.size
    val 날들: List<String> = 열쇠.map { 날짜만(it) }
    val 체중: Double get() = d.몸.체중
    val 날집합: Set<String> by lazy { 날들.toSet() }
    private val 존 = java.time.ZoneId.systemDefault()

    private val 부위표 = HashMap<String, String>()
    private val 부위없음 = HashSet<String>()
    /** 종목의 부위 (스탯 부위찾기 와 같다 · 한 번 찾은 것은 기억) */
    fun 부위(e: 종목기록): String? {
        val k = e.이름 + "|" + (e.플랜id ?: "")
        부위표[k]?.let { return it }
        if (k in 부위없음) return null
        val p = d.부위찾기(e)
        if (p == null) 부위없음.add(k) else 부위표[k] = p
        return p
    }

    val 날RM: List<Map<String, Double>> by lazy { 기록들.map { d.날RM들(it) } }
    val 날최다: List<Map<String, Int>> by lazy { 기록들.map { d.날최다들(it) } }
    val 누적RM: List<Map<String, Double>> by lazy {
        val m = HashMap<String, Double>()
        날RM.map { x -> x.forEach { (e, v) -> if (v > (m[e] ?: 0.0)) m[e] = v }; HashMap(m) }
    }
    val 누적최다: List<Map<String, Int>> by lazy {
        val m = HashMap<String, Int>()
        날최다.map { x -> x.forEach { (e, v) -> if (v > (m[e] ?: -1)) m[e] = v }; HashMap(m) }
    }
    /** 기록마다 볼륨 (워밍업 · 측정 뺌 — Logic.kt 볼륨) */
    val 날볼륨: List<Double> by lazy { 기록들.map { r -> 볼륨(r.종목들.flatMap { it.세트들 }) } }
    val 날부위: List<Set<String>> by lazy { 기록들.map { r -> r.종목들.mapNotNull { 부위(it) }.toSet() } }
    val 날종목: List<Set<String>> by lazy { 기록들.map { r -> r.종목들.filter { 기록세트(it.세트들).isNotEmpty() }.map { d.정식이름(it) }.toSet() } }
    /** 기록마다 기록 갱신 수 — Panel.kt [기록갱신] 과 같은 규칙을 앞에서부터 한 번에 센다 (1RM · 볼륨 따로 셈) */
    val 갱신수: List<Int> by lazy { 갱신수세기() }
    val 누적갱신: List<Int> by lazy { var s = 0; 갱신수.map { s += it; s } }
    val 주들: List<String> by lazy { 날들.map { 주(it) } }
    /** 주(월요일) → 그 주 기록 번호들 */
    val 주색인: Map<String, List<Int>> by lazy { 주들.indices.groupBy { 주들[it] } }
    /** 종목(정식이름) → 그 종목 1RM 이 있는 기록 번호들 (순서대로) */
    val 종목색인: Map<String, List<Int>> by lazy {
        val m = HashMap<String, MutableList<Int>>()
        날RM.forEachIndexed { i, x -> x.keys.forEach { m.getOrPut(it) { mutableListOf() }.add(i) } }
        m
    }

    fun 최고RM(i: Int, 종목: String): Double = if (i < 0) 0.0 else 누적RM[i][종목] ?: 0.0
    fun 삼대(i: Int): Double = 스탯표.삼대.sumOf { 최고RM(i, it) }
    fun 최다(i: Int, 종목: String): Int = if (i < 0) 0 else 누적최다[i][종목] ?: 0
    fun 시작(i: Int): java.time.LocalDateTime? =
        기록들[i].시작시각.takeIf { it > 0 }?.let { java.time.Instant.ofEpochMilli(it).atZone(존).toLocalDateTime() }
    /** 시작 시각의 '시' — 시작시각 0(옛 기록)이면 null → 판정에서 뺀다 */
    fun 시작시(i: Int): Int? = 시작(i)?.hour
    /** 이 기록으로 얻었을 때의 달성 시각 — 끝 시각, 모르면 그 날 0시 */
    fun 시각(i: Int): Long = 기록들[i].끝시각.takeIf { it > 0 } ?: 날시각(날들[i])
    fun 주(날: String): String = LocalDate.parse(날).let { it.minusDays((it.dayOfWeek.value - 1).toLong()) }.toString()
    /** 끝 까지의 운동날 (겹침 없이 · 순서대로) */
    fun 운동날(끝: Int): List<String> = if (끝 < 0) emptyList() else 날들.subList(0, 끝 + 1).distinct()

    /** 끝 까지 · 부위가 들어간 기록이 있는 날 */
    fun 부위날(끝: Int, 부위: String): Set<String> = (0..끝).filter { 부위 in 날부위[it] }.map { 날들[it] }.toSet()

    /** 끝 까지 · 이어진 주(월~일)에서 조건(그 주 운동날들)을 채운 가장 긴 줄 */
    fun 가장긴주(끝: Int, 조건: (Set<String>) -> Boolean): Int {
        if (끝 < 0) return 0
        val m = HashMap<String, MutableSet<String>>()
        for (i in 0..끝) m.getOrPut(주들[i]) { mutableSetOf() }.add(날들[i])
        var w = LocalDate.parse(m.keys.min()); val 끝주 = LocalDate.parse(m.keys.max())
        var 줄 = 0; var 최대 = 0
        while (!w.isAfter(끝주)) {
            val s = m[w.toString()]
            if (s != null && 조건(s)) { 줄++; 최대 = max(최대, 줄) } else 줄 = 0
            w = w.plusWeeks(1)
        }
        return 최대
    }

    /** 끝 까지 · 하루도 빠짐없이 이어진 가장 긴 운동날 수 */
    fun 가장긴날(끝: Int): Int {
        var 최대 = 0; var 줄 = 0; var 앞: LocalDate? = null
        for (x in 운동날(끝)) {
            val t = LocalDate.parse(x)
            줄 = if (앞 != null && t == 앞.plusDays(1)) 줄 + 1 else 1
            최대 = max(최대, 줄); 앞 = t
        }
        return 최대
    }

    /** 1-33 — i 번 기록의 주까지 [주수] 주 연속: 기록 ≥ 1 · 그 주 기록 전부 달성 · 미실시 0 (i 까지 본 것) */
    fun 계획주(i: Int, 주수: Int): Boolean {
        val 끝주 = LocalDate.parse(주들[i])
        for (w in 0 until 주수) {
            val 주 = 끝주.minusWeeks(w.toLong()).toString()
            val 들 = 주색인[주]?.filter { it <= i } ?: return false
            if (들.isEmpty() || 들.any { !기록들[it].달성 }) return false
            val 상한 = minOf(LocalDate.parse(주).plusDays(6).toString(), 날들[i])
            if (d.미실시.keys.any { it >= 주 && it <= 상한 }) return false
        }
        return true
    }

    /** 끝 까지 · 같은 '시' 에 시작한 운동 수의 최대 (시작시각 0 은 뺌) */
    fun 같은시최다(끝: Int): Int = (0..끝).mapNotNull { 시작시(it) }.groupingBy { it }.eachCount().values.maxOrNull() ?: 0

    /** 끝 까지 · 한 해(1/1~12/31) 볼륨 합의 최대 */
    fun 한해볼륨최대(끝: Int): Double = (0..끝).groupBy { 날들[it].take(4) }.values.maxOfOrNull { l -> l.sumOf { 날볼륨[it] } } ?: 0.0

    /** 끝 까지 · 한 종목을 한 날 수의 최대 (같은 종목 · 정식이름) */
    fun 종목날최다(끝: Int): Int {
        val m = HashMap<String, MutableSet<String>>()
        for (i in 0..끝) 날종목[i].forEach { m.getOrPut(it) { mutableSetOf() }.add(날들[i]) }
        return m.values.maxOfOrNull { it.size } ?: 0
    }

    /** i 번이 '쉬다가 돌아온' 기록인가 — 직전 운동날과 [일] 일 이상 떨어짐 (2-14 · 2-51) */
    fun 돌아옴(i: Int, 일: Long): Boolean =
        i > 0 && 날들[i] != 날들[i - 1] && ChronoUnit.DAYS.between(LocalDate.parse(날들[i - 1]), LocalDate.parse(날들[i])) >= 일

    /** 2-26 — i 번 기록의 어떤 종목이 최근 4 기록일(날마다 최고) 에서 잇달아 떨어짐 a1 > a2 > a3 > a4 */
    fun 세번내림(i: Int): Boolean = 날RM[i].keys.any { e ->
        val 날최고 = LinkedHashMap<String, Double>()
        종목색인[e].orEmpty().filter { it <= i }.forEach { j -> 날최고[날들[j]] = max(날최고[날들[j]] ?: 0.0, 날RM[j][e] ?: 0.0) }
        val 끝4 = 날최고.values.toList().takeLast(4)
        끝4.size == 4 && (0 until 3).all { 끝4[it] > 끝4[it + 1] + 업적글.여유 }
    }

    /** 2-42 — i 번 기록의 어떤 종목 1RM 이 그 종목 직전 기록보다 딱 0.5kg 오름 */
    fun 반키로(i: Int): Boolean = 날RM[i].any { (e, v) ->
        val 앞 = 종목색인[e].orEmpty().lastOrNull { it < i } ?: return@any false
        반올림0_1(v - (날RM[앞][e] ?: 0.0)) == 0.5
    }

    /** 기록 i 에서 종목(정식이름)마다 본운동 세트 (무게, 횟수) 목록 */
    private fun 본세트들(i: Int): Map<String, List<Pair<Double, Int>>> =
        기록들[i].종목들.groupBy { d.정식이름(it) }.mapValues { (_, l) -> l.flatMap { it.세트들 }.filter { it.종류 == 세트종류.본운동 }.map { it.w to it.r } }
            .filterValues { it.isNotEmpty() }

    /** 2-40 — i 번 기록의 어떤 종목이 앞의 이어진 기록들과 본운동 세트가 모두 같고, [날수] 번 이상 · 첫날~끝날 [일수] 일 이상 */
    fun 같은세트(i: Int, 날수: Int, 일수: Long): Boolean = 본세트들(i).any { (e, l) ->
        val 앞들 = 종목색인[e].orEmpty().filter { it < i }.reversed()
        var 수 = 1; var 첫 = 날들[i]
        for (j in 앞들) { if (본세트들(j)[e] != l) break; 수++; 첫 = 날들[j] }
        수 >= 날수 && ChronoUnit.DAYS.between(LocalDate.parse(첫), LocalDate.parse(날들[i])) >= 일수
    }

    /** 2-48 — i 번 기록의 주까지 [주수] 주 연속, 주마다 기록이 있고 그 기록의 모든 종목 부위가 '가슴' */
    fun 가슴만(i: Int, 주수: Int): Boolean {
        val 끝주 = LocalDate.parse(주들[i])
        for (w in 0 until 주수) {
            val 들 = 주색인[끝주.minusWeeks(w.toLong()).toString()]?.filter { it <= i } ?: return false
            if (들.isEmpty()) return false
            if (들.any { j -> 기록들[j].종목들.isEmpty() || 기록들[j].종목들.any { 부위(it) != "가슴" } }) return false
        }
        return true
    }

    /** 2-49 — i 번 기록의 주(i 까지): 본세트 ≥ 10 이고 부위 '팔' 본세트가 절반 이상 */
    fun 팔주(i: Int): Boolean {
        var 본 = 0; var 팔 = 0
        주색인[주들[i]].orEmpty().filter { it <= i }.forEach { j ->
            기록들[j].종목들.forEach { e ->
                val c = e.세트들.count { it.종류 == 세트종류.본운동 }
                본 += c; if (부위(e) == "팔") 팔 += c
            }
        }
        return 본 >= 10 && 팔 >= 본 * 0.5
    }

    /** 1-41 — 운동날 사이(또는 마지막 운동날 ~ 어제) 빈 날이 [빈날] 이상이 된 날. 첫 기록 전은 세지 않는다 */
    fun 긴쉼(빈날: Long): Long? {
        val 날 = 운동날(n - 1)
        for (j in 1 until 날.size) {
            val a = LocalDate.parse(날[j - 1])
            if (ChronoUnit.DAYS.between(a, LocalDate.parse(날[j])) - 1 >= 빈날) return 날시각(a.plusDays(빈날 + 1).toString())
        }
        val a = LocalDate.parse(날.lastOrNull() ?: return null)
        return if (ChronoUnit.DAYS.between(a, LocalDate.parse(오늘)) - 1 >= 빈날) 날시각(a.plusDays(빈날 + 1).toString()) else null
    }

    private val 칠일 = 7 * 24 * 3_600_000L
    private val 체중들: List<체중값> by lazy { d.체중기록.sortedBy { it.시각 } }
    /** 체중기록 j 번째 시각의 감량 = 첫 체중 − 그 앞 7일 평균 */
    private fun 감량(j: Int): Double {
        val t = 체중들[j].시각
        return 체중들[0].kg - 체중들.filter { it.시각 in (t - 칠일)..t }.map { it.kg }.average()
    }
    /** 1-47 · 1-48 — 감량이 처음 [kg] 이상이 된 시각 */
    fun 체중감량(kg: Double): Long? = (1 until 체중들.size).firstOrNull { 감량(it) >= kg - 업적글.여유 }?.let { 체중들[it].시각 }
    /** 진행 막대 — 지금(마지막 줄) 감량 */
    fun 지금감량(): Double = if (체중들.size < 2) 0.0 else max(0.0, 감량(체중들.size - 1))
    /** 2-20 — 하루에 [번] 번째 체중을 적은 시각 (가장 이른 날) */
    fun 하루체중(번: Int): Long? = 체중들.groupBy { 시각날(it.시각) }.values.filter { it.size >= 번 }.minOfOrNull { it[번 - 1].시각 }

    /** 하체날 루틴 — 종목 줄 중 부위 '하체' 가 절반 이상 (2-11 [제안]) */
    private fun 하체날(r: 루틴?): Boolean {
        if (r == null || r.휴식일 || r.종목.isEmpty()) return false
        val 하 = r.종목.count { 부위(종목기록(it.이름, emptyList(), 플랜id = it.플랜id)) == "하체" }
        return 하 * 2 >= r.종목.size
    }
    /** 2-11 — 하체날을 건너뛰거나(건너뜀) 못 한(미실시) 날들. 미실시는 루틴 이름으로 찾는다 (이름이 바뀌면 못 찾는다) */
    fun 하체빠진날(): List<String> {
        val a = d.건너뜀.filter { 하체날(d.루틴(it.value)) }.keys
        val b = d.미실시.filter { (_, 이름) -> 하체날(d.루틴들.firstOrNull { it.이름 == 이름 }) }.keys
        return (a + b).sorted()
    }

    /** 2-32 — 루틴(또는 지금 운동)의 휴식이 [초] 이상인 줄이 있나 */
    fun 긴휴식(초: Int): Boolean =
        d.루틴들.any { r -> r.종목.any { e -> e.휴식 >= 초 || e.휴식값.any { it >= 초 } } } ||
            d.세션?.종목들?.any { e -> e.휴식 >= 초 || e.휴식들.any { (it ?: 0) >= 초 } } == true

    /** 1-44 — 1년(365일) 전 스탯기록과 견줘 둘 다 값이 있는 스탯이 모두 올랐나 (스트레스 뺌) */
    fun 일년상승(): Boolean {
        val 그날 = d.스탯기록.keys.filter { it <= 날더하기(오늘, -365) }.maxOrNull() ?: return false
        val 그때 = d.스탯기록[그날]!!
        val 지금 = d.스탯기록[오늘] ?: d.스탯맵(오늘)
        val 공통 = 그때.keys.filter { it in 지금 && it != 스탯.스트레스.name }
        return 공통.isNotEmpty() && 공통.all { 지금[it]!! > 그때[it]!! }
    }

    private fun 갱신수세기(): List<Int> {
        val 최고rm = HashMap<Pair<String, String?>, Double>()
        val 최고볼 = HashMap<Pair<String, String?>, Double>()
        return 기록들.map { rec ->
            var c = 0
            val 이번 = mutableListOf<Triple<Pair<String, String?>, Double, Double>>()
            rec.종목들.map { it.이름 to it.묶음 }.distinct().forEach { 열 ->
                val 세트들 = rec.종목들.filter { it.이름 == 열.first && it.묶음 == 열.second }.flatMap { it.세트들 }
                val 기 = 기록세트(세트들)
                if (기.isEmpty()) return@forEach
                val rm = 기.maxOf { 일RM(it.w, it.r) }; val 볼 = 볼륨(세트들)
                val 전rm = 최고rm[열]
                if (전rm != null) {
                    if (rm > 전rm + 0.05) c++
                    if (볼 > (최고볼[열] ?: 0.0) + 0.05) c++
                }
                이번 += Triple(열, rm, 볼)
            }
            이번.forEach { (열, rm, 볼) -> 최고rm[열] = max(최고rm[열] ?: rm, rm); 최고볼[열] = max(최고볼[열] ?: 볼, 볼) }
            c
        }
    }
}

// ─────────────── 판정법 · 진행법 ───────────────

/** 업적 하나를 어떻게 판정하나 */
sealed class 판정법 {
    /** 0 … i 번 기록으로 본다 — 기록이 늘어도 한 번 참이면 계속 참. 소급할 때 반으로 갈라 첫 기록을 찾는다 */
    class 누적(val 됨: (판정자료, Int) -> Boolean) : 판정법()
    /** i 번 기록에서 일어난 일 (앞 기록은 볼 수 있다). 소급할 때 앞에서부터 처음 일어난 기록을 찾는다 */
    class 사건(val 됨: (판정자료, Int) -> Boolean) : 판정법()
    /** 기록 밖의 칸(쉰날 · 체중기록 · 세기 · 루틴 …) — 처음 채운 시각, 아직이면 null */
    class 상태(val 언제: (판정자료, Long) -> Long?) : 판정법()
    /** 운동을 저장하는 순간의 세션으로만 — 날기록에 남지 않는 것 (소급 불가) */
    class 세션(val 됨: (운동세션) -> Boolean) : 판정법()
}

/** 진행 막대 — 지금 값 · 목표(null 이면 막대 없음) · 단위 */
class 진행법(val 값: (판정자료) -> Double, val 목표: (판정자료) -> Double?, val 단위: String)

private class 정의(val 법: 판정법, val 진행: 진행법? = null)

/** X(i 까지) ≥ N — 누적 판정 + 진행 막대 */
private fun 이상(단위: String, 목표: (판정자료) -> Double?, 값: (판정자료, Int) -> Double) = 정의(
    판정법.누적 { z, i -> 목표(z)?.let { g -> g > 0 && 값(z, i) >= g - 업적글.여유 } == true },
    진행법({ z -> if (z.n == 0) 0.0 else 값(z, z.n - 1) }, 목표, 단위),
)
private fun 이상(단위: String, N: Double, 값: (판정자료, Int) -> Double) = 이상(단위, { N }, 값)

private fun 삼대(N: Double) = 이상("kg", N) { z, i -> z.삼대(i) }
private fun 최고(종목: String, N: Double) = 이상("kg", N) { z, i -> z.최고RM(i, 종목) }
private fun 체중배(종목: String, 배: Double) = 이상("kg", { z -> if (z.체중 > 0) z.체중 * 배 else null }) { z, i -> z.최고RM(i, 종목) }
private fun 연속(종목: String, N: Int) = 이상("회", N.toDouble()) { z, i -> z.최다(i, 종목).toDouble() }
private fun 세기(이름: String, N: Int) = 정의(
    판정법.상태 { z, 지금 -> if ((z.d.세기[이름] ?: 0) >= N) 지금 else null },
    진행법({ z -> (z.d.세기[이름] ?: 0).toDouble() }, { N.toDouble() }, "번"),
)
private fun 사건(됨: (판정자료, Int) -> Boolean) = 정의(판정법.사건(됨))
private fun 날짜(월일: String) = 사건 { z, i -> z.날들[i].endsWith("-$월일") }
private fun 날RM같음(목표: (판정자료) -> Double?) = 사건 { z, i ->
    val g = 목표(z) ?: return@사건 false
    z.날RM[i].values.any { 반올림0_1(it) == 반올림0_1(g) }
}

/**
 * ★ 업적 판정표 — 번호 → 판정. **문턱 숫자는 여기 한곳에** (조건 글은 업적표.csv).
 * 여기 없는 번호는 '준비 중' (다른 기능이 먼저 있어야 한다 — 스탯명세 7-3).
 */
private val 정의표: Map<String, 정의> = linkedMapOf(
    // ── 3대 합계 (3대 = 종목마다 역대 최고 1RM 의 합 · 기준값은 [내 생각] 12-5) ──
    "1-1" to 삼대(200.0), "1-2" to 삼대(300.0), "1-3" to 삼대(400.0),
    "1-4" to 삼대(500.0), "1-5" to 삼대(600.0), "1-6" to 삼대(700.0),
    // ── 종목별 (별칭 '스쿼트' 는 백 스쿼트로 묶인다 · 에플리는 10회 이하 세트만) ──
    "1-7" to 최고("벤치프레스", 60.0), "1-8" to 최고("벤치프레스", 100.0), "1-9" to 최고("벤치프레스", 140.0),
    "1-10" to 최고("백 스쿼트", 140.0), "1-11" to 최고("백 스쿼트", 200.0),
    "1-12" to 최고("데드리프트", 180.0), "1-13" to 최고("데드리프트", 250.0),
    // 체중 = 판정 순간의 설정 체중 (그날 체중 칸이 생기면 그 기록의 체중으로 — CSV 메모)
    "1-14" to 체중배("오버헤드 프레스", 1.0), "1-15" to 체중배("벤치프레스", 1.0), "1-16" to 체중배("데드리프트", 2.5),
    // ── 맨몸 (정자세 최다 연속 · 어시스트 세트는 빠진다 · 스탯명세 2-3) ──
    "1-17" to 연속("턱걸이", 1), "1-18" to 연속("팔굽혀펴기", 20), "1-19" to 연속("턱걸이", 15),
    "1-20" to 연속("턱걸이", 30), "1-21" to 연속("턱걸이", 50),
    "1-22" to 연속("맨몸 스쿼트", 100), "1-23" to 연속("맨몸 스쿼트", 500),
    // ── 꾸준함 · 성실 ──
    // ⚠ 1-31 '첫 운동 기록' 과 1-45 '헬스장 첫 방문 기록' 은 앱에 장소가 없어 같은 순간에 둘 다 얻는다 (스탯명세 8-1 · 그대로 둠)
    "1-31" to 정의(판정법.누적 { _, _ -> true }),
    "1-32" to 이상("주", 4.0) { z, i -> z.가장긴주(i) { it.size >= 3 }.toDouble() },
    // '계획 달성' 뜻은 [제안] — 미실시는 v0.7.0 부터만 쌓였다 (CSV 메모)
    "1-33" to 사건 { z, i -> z.계획주(i, 12) },
    "1-34" to 이상("주", 52.0) { z, i -> z.가장긴주(i) { it.size >= 3 }.toDouble() },
    "1-35" to 이상("일", 365.0) { z, i -> z.가장긴날(i).toDouble() },
    "1-37" to 이상("번", 30.0) { z, i -> (0..i).count { j -> (z.시작시(j) ?: 99) < 5 }.toDouble() },
    // ── 스트레스 · 회복 ──
    "1-38" to 정의(
        판정법.상태 { z, _ -> z.d.쉰날.sorted().getOrNull(9)?.let { 날시각(it) } },
        진행법({ z -> z.d.쉰날.size.toDouble() }, { 10.0 }, "번"),
    ),
    // ⚠ 1-41 등급 '유머' 는 말투 규칙 4단계(12-5) 밖 — 색은 Theme 의 '유머' 로 따로 둠 (스탯명세 8-4)
    "1-41" to 정의(판정법.상태 { z, _ -> z.긴쉼(14) }),
    // ── 기록 갱신 (Panel.kt 기록갱신 규칙 · 한 날 한 종목 1RM · 볼륨 둘 다면 2번 [제안]) ──
    "1-42" to 정의(판정법.누적 { z, i -> z.누적갱신[i] >= 1 }),
    "1-43" to 이상("번", 50.0) { z, i -> z.누적갱신[i].toDouble() },
    // 스탯기록이 1년 쌓여야 한다 — 그 전에는 얻을 수 없다
    "1-44" to 정의(판정법.상태 { z, 지금 -> if (z.일년상승()) 지금 else null }),
    // ── 헬스장 생활 ──
    "1-45" to 정의(판정법.누적 { _, _ -> true }),
    "1-46" to 이상("번", 30.0) { z, i -> z.같은시최다(i).toDouble() },
    // ── 몸 · 체중 (처음 적은 체중 − 최근 7일 평균 · 체중기록) ──
    "1-47" to 정의(판정법.상태 { z, _ -> z.체중감량(5.0) }, 진행법({ z -> z.지금감량() }, { 5.0 }, "kg")),
    "1-48" to 정의(판정법.상태 { z, _ -> z.체중감량(20.0) }, 진행법({ z -> z.지금감량() }, { 20.0 }, "kg")),
    // ── 체형 · 부위 (데드리프트는 부위 '등' — 하체로 안 센다, 스탯명세 8-10) ──
    "1-53" to 이상("주", 12.0) { z, i -> val 하 = z.부위날(i, "하체"); z.가장긴주(i) { 날 -> 날.count { it in 하 } >= 2 }.toDouble() },

    // ═══════════ 히든 ═══════════
    // ⚠ 2-1 문구는 아직 없다 — 12-10 '(확정) 문구 미정' (스탯명세 8-5)
    "2-1" to 날짜("12-25"),
    "2-2" to 날짜("01-01"),
    "2-3" to 사건 { z, i -> z.날들[i].endsWith("-01-04") && "${z.날들[i].take(4)}-01-01" in z.날집합 },
    "2-6" to 사건 { z, i -> z.시작(i)?.let { it.dayOfWeek == DayOfWeek.FRIDAY && it.hour >= 22 } == true },
    "2-7" to 사건 { z, i -> z.기록들[i].걸린초 > 3 * 3600 },
    "2-9" to 이상("번", 3.0) { z, i -> (0..i).count { '~' in z.열쇠[it] }.toDouble() },
    "2-10" to 이상("일", 20.0) { z, i -> z.운동날(i).count { LocalDate.parse(it).dayOfWeek == DayOfWeek.MONDAY }.toDouble() },
    "2-11" to 정의(
        판정법.상태 { z, _ -> z.하체빠진날().getOrNull(4)?.let { 날시각(it) } },
        진행법({ z -> z.하체빠진날().size.toDouble() }, { 5.0 }, "번"),
    ),
    // 에플리 환산값도 포함 (75kg × 10회 = 100.0)
    "2-13" to 날RM같음 { 100.0 },
    // ⚠ 2-14 '탕아의 귀환' 과 2-51 '나 다시 돌아갈래' 는 조건이 같다 (12-6 · 12-9 에 따로 생김 · 스탯명세 8-2) — 둘 다 같은 순간에 얻는다
    "2-14" to 사건 { z, i -> z.돌아옴(i, 31) },
    "2-15" to 사건 { z, i -> (z.시작시(i) ?: -1) in 3..4 },
    "2-16" to 사건 { z, i -> z.기록들[i].걸린초 in 1..300 },
    "2-17" to 세기(세기이름.연속건너뜀, 10),
    "2-19" to 정의(판정법.상태 { z, 지금 -> if (z.d.루틴들.any { r -> r.종목.groupingBy { it.이름 }.eachCount().values.any { it >= 5 } }) 지금 else null }),
    "2-20" to 정의(판정법.상태 { z, _ -> z.하루체중(3) }),
    // '목표' = 그 회차 계획값 [제안] — 측정할 때 남는 향상기록의 예상값
    "2-24" to 정의(판정법.상태 { z, _ -> z.d.향상기록들.filter { it.예상값 > 0 && it.측정값 >= it.예상값 * 1.2 - 업적글.여유 }.minOfOrNull { it.날짜 }?.let { 날시각(it) } }),
    "2-25" to 날RM같음 { z -> z.체중.takeIf { it > 0 } },
    "2-26" to 사건 { z, i -> z.세번내림(i) },
    "2-30" to 사건 { z, i ->
        val 날 = LocalDate.parse(z.날들[i])
        날.dayOfMonth == 날.lengthOfMonth() && (0..i).count { z.날들[it] == z.날들[i] } >= 3
    },
    "2-31" to 사건 { z, i -> LocalDate.parse(z.날들[i]).let { it.dayOfMonth == 13 && it.dayOfWeek == DayOfWeek.FRIDAY } },
    "2-32" to 정의(판정법.상태 { z, 지금 -> if (z.긴휴식(600)) 지금 else null }),
    // 날기록에는 휴식이 남지 않음 → 저장하는 순간 세션으로만 (지난 기록 소급 불가)
    "2-33" to 정의(판정법.세션 { S -> S.종목들.sumOf { e -> e.기록.indices.count { k -> e.기록[k] != null && e.세트휴식(k) == 0 } } >= 5 }),
    // 2-16(5분 이하) 과 함께 얻을 수 있다
    "2-34" to 사건 { z, i -> z.기록들[i].걸린초 in 1..59 },
    "2-35" to 사건 { z, i -> z.기록들[i].let { r -> r.시작시각 > 0 && r.끝시각 > 0 && 시각날(r.끝시각) > 시각날(r.시작시각) } },
    // '무게 합계' = 볼륨(워밍업 · 측정 뺌) · 같은 날 '한 번 더' 까지 합친 날 단위 [제안]
    "2-37" to 사건 { z, i ->
        var 합 = 0.0; var j = i
        while (j >= 0 && z.날들[j] == z.날들[i]) { 합 += z.날볼륨[j]; j-- }
        반올림0_1(합) == 1000.0
    },
    "2-38" to 정의(
        판정법.누적 { z, i -> z.한해볼륨최대(i) >= 1_000_000.0 },
        진행법({ z -> (0 until z.n).filter { z.날들[it].take(4) == z.오늘.take(4) }.sumOf { z.날볼륨[it] } }, { 1_000_000.0 }, "kg"),
    ),
    "2-39" to 사건 { z, i -> 기록세트(z.기록들[i].종목들.flatMap { it.세트들 }).let { s -> s.size >= 3 && s.all { it.r == 10 } } },
    "2-40" to 사건 { z, i -> z.같은세트(i, 5, 70) },
    "2-41" to 날RM같음 { 69.0 },
    "2-42" to 사건 { z, i -> z.반키로(i) },
    "2-45" to 세기(세기이름.루틴이름바꿈, 10),
    "2-46" to 세기(세기이름.설정진입, 50),
    // 자동 백업이 생기면 자동은 안 센다 [제안]
    "2-47" to 세기(세기이름.백업, 30),
    "2-48" to 사건 { z, i -> z.가슴만(i, 4) },
    "2-49" to 사건 { z, i -> z.팔주(i) },
    "2-50" to 이상("일", 100.0) { z, i -> z.부위날(i, "복근").size.toDouble() },
    "2-51" to 사건 { z, i -> z.돌아옴(i, 31) },
    "2-54" to 이상("일", 100.0) { z, i -> z.종목날최다(i).toDouble() },
    // ── 아직 판정하지 않는 것 (다른 기능 · 새 화면이 먼저) ──
    // 1-24 · 1-25 플랭크 시간 / 1-26 ~ 1-30 둘레 · 인바디 / 1-36 · 1-49 · 2-22 보충제 / 1-39 · 2-27 컨디션 칩
    // 1-40 · 1-51 · 1-52 스트레스 / 1-50 플랜 회차별 측정 여부 / 2-4 생일 / 2-5 명절표(음력 날짜 확인 필요)
    // 2-8 · 2-18 · 2-36 · 2-52 · 2-53 운동세션 시각 목록 · PiP 시간 / 2-12 · 2-43 · 2-44 운동 메모
    // 2-21 팔둘레 / 2-23 수면 / 2-28 · 2-29 날씨 특보
    // ⚠ 2-8 은 글자 그대로면 거의 모든 날이 해당 — 뜻 확인 필요 (스탯명세 8-3)
)

/** 번호 → 판정법 (지금 판정하는 것만) */
val 업적판정법: Map<String, 판정법> = 정의표.mapValues { it.value.법 }
/** 번호 → 진행 막대 (X ≥ N 꼴만) */
val 업적진행법: Map<String, 진행법> = 정의표.filterValues { it.진행 != null }.mapValues { it.value.진행!! }

// ─────────────── 판정 · 진행 · 갱신 ───────────────

/** 처음 조건을 채운 시각 — 아직이면 null. 세션 판정은 [방금세션] 이 있을 때만 (그 순간 = 지금) */
private fun 달성시각(법: 판정법, z: 판정자료, 지금: Long, 방금세션: 운동세션?): Long? = when (법) {
    is 판정법.누적 -> {
        if (z.n == 0 || !법.됨(z, z.n - 1)) null
        else {
            var lo = 0; var hi = z.n - 1
            while (lo < hi) { val mid = (lo + hi) / 2; if (법.됨(z, mid)) hi = mid else lo = mid + 1 }
            z.시각(lo)
        }
    }
    is 판정법.사건 -> (0 until z.n).firstOrNull { 법.됨(z, it) }?.let { z.시각(it) }
    is 판정법.상태 -> 법.언제(z, 지금)
    is 판정법.세션 -> if (방금세션 != null && 법.됨(방금세션)) 지금 else null
}

/** 지금 이 업적의 조건을 채우고 있나 (얻었는지와 상관없이 · 판정하지 않는 번호는 false) */
fun 업적판정(
    번호: String, d: 앱데이터, 오늘: String = LocalDate.now().toString(),
    지금: Long = System.currentTimeMillis(), 방금세션: 운동세션? = null,
): Boolean {
    val 법 = 업적판정법[번호] ?: return false
    return 달성시각(법, 판정자료(d, 오늘), 지금, 방금세션) != null
}

/** 진행 막대 (0 ~ 1) — 판정식이 X ≥ N 꼴일 때만, 아니면 null */
fun 업적진행(번호: String, d: 앱데이터, 오늘: String = LocalDate.now().toString()): Double? =
    업적진행값(번호, 판정자료(d, 오늘))?.let { (v, g) -> (v / g).coerceIn(0.0, 1.0) }

/** 진행 막대의 (지금 값, 목표) — 화면의 '365 / 400kg' */
fun 업적진행값(번호: String, z: 판정자료): Pair<Double, Double>? {
    val p = 업적진행법[번호] ?: return null
    val g = p.목표(z)?.takeIf { it > 0 } ?: return null
    return p.값(z) to g
}

/** 화면용 — 모든 진행 막대를 판정자료 하나로 (번호 → (값, 목표)) */
fun 앱데이터.업적진행들(오늘: String): Map<String, Pair<Double, Double>> {
    val z = 판정자료(this, 오늘)
    return 업적진행법.keys.mapNotNull { k -> 업적진행값(k, z)?.let { k to it } }.toMap()
}

/** 진행 막대의 단위 (kg · 회 · 주 · 일 · 번) */
fun 업적단위(번호: String): String = 업적진행법[번호]?.단위 ?: ""

/**
 * 새로 얻은 업적을 [앱데이터.업적] 에 더한다 — 이미 얻은 것은 건드리지 않는다 (취소 안 함).
 * @param 대상 이 번호들만 본다 (null = 전부)
 * @param 방금세션 방금 저장한 운동 (세션으로만 보는 업적 2-33)
 * @return (새 앱데이터, 새로 얻은 번호들 — 업적표 순서)
 */
fun 앱데이터.업적갱신(
    오늘: String, 지금: Long, 대상: Collection<String>? = null, 방금세션: 운동세션? = null,
): Pair<앱데이터, List<String>> {
    val z = 판정자료(this, 오늘)
    val 새 = LinkedHashMap<String, Long>()
    업적판정법.forEach { (번호, 법) ->
        if (번호 in 업적) return@forEach
        if (대상 != null && 번호 !in 대상) return@forEach
        달성시각(법, z, 지금, 방금세션)?.let { 새[번호] = it }
    }
    if (새.isEmpty()) return this to emptyList()
    val 순서 = 업적표.목록.map { it.번호 }.filter { it in 새 }
    return copy(업적 = 업적 + 새) to 순서
}

/**
 * 바뀐 것을 보고 다시 볼 업적 — null 이면 볼 것 없음 (화면을 만질 때마다 다 보지 않으려고).
 * 기록이 바뀌면(운동 저장) 전부. 나머지는 그 칸을 쓰는 업적만
 */
fun 업적살필것(전: 앱데이터, 새: 앱데이터): Collection<String>? {
    if (전.기록.keys != 새.기록.keys) return 업적판정법.keys
    val 볼 = mutableSetOf<String>()
    if (전.세기 != 새.세기) 볼 += listOf("2-17", "2-45", "2-46", "2-47")
    if (전.쉰날 != 새.쉰날) 볼 += "1-38"
    if (전.건너뜀 != 새.건너뜀 || 전.미실시 != 새.미실시) 볼 += listOf("2-11", "1-33")
    if (전.체중기록 != 새.체중기록) 볼 += listOf("1-47", "1-48", "2-20")
    if (전.향상기록들 != 새.향상기록들) 볼 += "2-24"
    if (전.루틴들 !== 새.루틴들) 볼 += listOf("2-19", "2-32")
    if (전.세션 !== 새.세션 && 새.세션 != null) 볼 += "2-32"
    if (전.스탯기록 != 새.스탯기록) 볼 += "1-44"
    return 볼.takeIf { it.isNotEmpty() }
}

/** 대표 칭호 — 얻은 업적의 칭호만 달 수 있다. 같은 것을 다시 고르면 뗀다 */
fun 앱데이터.대표칭호고름(칭호번호: String): 앱데이터 {
    val a = 업적표.칭호찾기(칭호번호) ?: return this
    if (a.번호 !in 업적) return this
    return copy(대표칭호 = if (대표칭호 == 칭호번호) null else 칭호번호)
}

/** 대표 칭호의 이름 (없으면 null) */
val 앱데이터.대표칭호이름: String? get() = 대표칭호?.let { 업적표.칭호찾기(it) }?.칭호
