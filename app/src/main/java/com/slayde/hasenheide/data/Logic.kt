package com.slayde.hasenheide.data

import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 계산과 규칙 — 시제품(웹)에서 확정한 규칙을 그대로 옮긴 것.
 * 기능명세(claude/02_기능명세.md)의 절 번호를 주석에 달아 두었다.
 *
 * 모든 함수는 '앱데이터를 받아 새 앱데이터를 돌려준다'. 제자리에서 고치지 않는다.
 */

// ─────────────── 날짜 ───────────────

fun 날(k: String): LocalDate = LocalDate.parse(k)
fun 키(d: LocalDate): String = d.toString()
fun 날더하기(k: String, n: Int): String = 키(날(k).plusDays(n.toLong()))
fun 개월전(오늘: String, n: Int): String = 키(날(오늘).minusMonths(n.toLong()))

/** 목록의 k 번째 칸을 바꾼다. 모자라면 빈 칸(null)으로 늘린다 */
fun <T> List<T?>.칸바꿈(k: Int, v: T?): List<T?> {
    val m = toMutableList()
    while (m.size <= k) m.add(null)
    m[k] = v
    return m
}
fun <T> List<T?>.칸(k: Int): T? = if (k in indices) this[k] else null

fun 무게반올림(w: Double): Double = (w * 10).roundToInt() / 10.0
fun 무게글(w: Double): String = if (w == w.toLong().toDouble()) w.toLong().toString() else "%.1f".format(w)
fun 콤마(n: Number): String = "%,d".format(n.toDouble().roundToInt())
fun 분초(초: Int): String = "${초 / 60}:${(초 % 60).toString().padStart(2, '0')}"

/** 휴식 최대 — 99분 59초 (분 칸에 60 을 치면 60분이 되도록, 09-21 메모) */
const val 휴식최대 = 5999

/**
 * 휴식 남은 초 — 화면에 보일 값 (09-25 메모: 1:00 으로 정했는데 1:01 부터 줄었다)
 * 화면 시계는 0.25초마다 바뀌어서, 체크한 직후엔 시계가 체크 시각보다 조금 앞선 값을 들고 있다.
 * 그래서 올림 계산이 한 칸 크게 나왔다 → 처음 정한 길이를 넘지 않게 자른다
 */
fun 휴식중.남은초(지금: Long): Int {
    val 초 = max(0L, (끝시각 - 지금 + 999) / 1000).toInt()
    return if (총초 > 0) minOf(초, 총초) else 초
}

// ─────────────── 설정에서 고를 수 있는 값 (09-25 메모) ───────────────
val 무게폭목록 = listOf(0.1, 0.5, 1.0, 2.5, 5.0)          // 기본 1
val 기본휴식목록 = listOf(30, 60, 90, 120, 180)           // 기본 60초
val 기본세트목록 = (1..5).toList()                         // 기본 1
val 진동세기목록 = listOf(1 to "약", 2 to "중", 3 to "강")  // 기본 중
val 진동시간목록 = listOf(500, 1000, 2000, 3000)           // 기본 1초

/** "3:00" 또는 "180" → 초 */
fun 초읽기(글: String): Int? {
    val t = 글.trim()
    if (t.contains(':')) {
        val (m, s) = t.split(':', limit = 2)
        return ((m.toIntOrNull() ?: 0) * 60 + (s.toIntOrNull() ?: 0)).coerceIn(0, 휴식최대)
    }
    return t.toIntOrNull()?.coerceIn(0, 휴식최대)
}

/** 운동 중 흐른 시간 — "02분 41초" / 60분 넘으면 "1시간 02분 05초" (5-1) */
fun 시분초(초: Long): String {
    val s = max(0L, 초)
    val h = s / 3600; val m = (s % 3600) / 60; val x = s % 60
    val mm = m.toString().padStart(2, '0'); val xx = x.toString().padStart(2, '0')
    return if (h > 0) "${h}시간 ${mm}분 ${xx}초" else "${mm}분 ${xx}초"
}

// ─────────────── 볼륨 · 1RM ───────────────

fun 볼륨(세트들: List<세트>): Double = 세트들.sumOf { it.w * it.r }
/** 에플리(Epley) 식 — 무게 × (1 + 횟수/30) */
fun 일RM(w: Double, r: Int): Double = if (r > 0) w * (1 + r / 30.0) else 0.0

// ─────────────── 루틴 · 예상 시간 (4-3) ───────────────

const val 세트수행초 = 40
const val 종목전환초 = 60

fun 예상초(r: 루틴?): Int {
    if (r == null || r.휴식일) return 0
    val 본 = r.종목.sumOf { it.세트 * 세트수행초 + max(0, it.세트 - 1) * it.휴식 }
    return 본 + max(0, r.종목.size - 1) * 종목전환초
}
fun 시간글(초: Int): String {
    val m = max(0, (초 / 60.0).roundToInt())
    return if (m >= 60) "${m / 60}시간 ${(m % 60).toString().padStart(2, '0')}분" else "${m}분"
}
fun 총세트(r: 루틴?): Int = if (r == null || r.휴식일) 0 else r.종목.sumOf { it.세트 }

fun 앱데이터.루틴(id: String?): 루틴? = 루틴들.firstOrNull { it.id == id }
fun 앱데이터.예정루틴(k: String): 루틴? = 루틴(예정[k])

// ─────────────── 예정표 — 2회차까지 미리 깔기 (2-2) ───────────────

const val 회차 = 2

/**
 * 캘린더에 깔리는 순서 — **자동생성을 켠 루틴만** (09-25 메모).
 * 꺼 둔 루틴은 달력에 저절로 깔리지 않고, '다른 루틴' 으로 그 날에만 넣을 수 있다.
 */
val 앱데이터.순번: List<루틴> get() = 루틴들.filter { it.자동생성 }

private fun 앱데이터.예정채움(시작날: String, 시작idx: Int, 바탕: Map<String, String>): Map<String, String> {
    val 줄 = 순번
    val n = 줄.size
    if (n == 0) return 바탕
    val m = 바탕.toMutableMap()
    for (i in 0 until n * 회차) m[날더하기(시작날, i)] = 줄[((시작idx + i) % n + n) % n].id
    return m
}

/** 마지막으로 한 운동의 '다음'부터 다시 깐다 (자동생성 루틴으로 한 마지막 운동 기준) */
fun 앱데이터.예정초기화(오늘: String): 앱데이터 {
    val 줄 = 순번
    // 자동생성이 꺼진 루틴을 '다른 루틴' 으로 넣어 둔 날(오늘 이후)은 지키고 그 위에 다시 깐다
    val 수동 = 예정.filter { it.key >= 오늘 && 루틴(it.value)?.자동생성 == false }
    if (줄.isEmpty()) return copy(예정 = 수동)
    val 마지막 = 기록.keys.filter { k -> 줄.any { it.id == 기록[k]!!.루틴id } }.maxOrNull()
    var idx = 0
    if (마지막 != null) {
        val i = 줄.indexOfFirst { it.id == 기록[마지막]!!.루틴id }
        idx = if (i < 0) 0 else (i + 1) % 줄.size
    }
    val 시작 = if (기록.containsKey(오늘)) 날더하기(오늘, 1) else 오늘
    return copy(예정 = 예정채움(시작, idx, emptyMap()) + 수동)
}

/**
 * 앱을 켤 때 · 날짜가 바뀔 때 — 빠진 날을 반영해 예정을 맞춘다 (4. 순서 기준)
 * "오늘 할 것 = 마지막으로 완료한 것의 다음" — 며칠 빠져도 순서가 어긋나지 않는다.
 *  · 지난 날에 깔려 있었는데 기록이 없는 날(빠진 날)이 있으면, 그 중 첫 '운동 날'의 루틴부터 오늘 다시 깐다
 *  · 빠진 날이 전부 휴식일이면 쉰 것으로 치고 그 다음 차례부터
 *  · 빠진 날이 없고 앞으로의 예정도 있으면 그대로 둔다 (옮겨 둔 것을 지키기 위해)
 */
fun 앱데이터.예정맞추기(오늘: String): 앱데이터 {
    val 줄 = 순번
    if (줄.isEmpty()) return this
    // 빠진 날은 자동생성 루틴이 깔려 있던 날만 본다
    val 빠진 = 예정.filter { it.key < 오늘 && !기록.containsKey(it.key) && 루틴(it.value)?.자동생성 == true }.toSortedMap()
    val 앞으로 = 예정.any { it.key >= 오늘 && 루틴(it.value)?.자동생성 == true }
    if (빠진.isEmpty()) return if (앞으로) copy(예정 = 예정.filterKeys { it >= 오늘 }) else 예정초기화(오늘)
    val 운동날 = 빠진.values.firstOrNull { rid -> 루틴(rid)?.휴식일 == false }
    val n = 줄.size
    val 시작idx = if (운동날 != null) 줄.indexOfFirst { it.id == 운동날 }
                  else (줄.indexOfFirst { it.id == 빠진.values.last() } + 1) % n
    if (시작idx < 0) return 예정초기화(오늘)
    val 시작 = if (기록.containsKey(오늘)) 날더하기(오늘, 1) else 오늘
    val 수동 = 예정.filter { it.key >= 오늘 && 루틴(it.value)?.자동생성 == false }
    return copy(예정 = 예정채움(시작, 시작idx, emptyMap()) + 수동)
}

/** 날짜 D 에 루틴을 꽂고 그 날부터 순서를 다시 깐다. 앞선 날은 건드리지 않는다 (2-3) */
fun 앱데이터.꽂기(rid: String, D: String, 오늘: String): 앱데이터 {
    if (D < 오늘) return this
    val idx = 순번.indexOfFirst { it.id == rid }
    // 자동생성이 꺼진 루틴은 그 날에만 넣고 나머지 예정은 그대로 둔다
    if (idx < 0) return if (루틴(rid) != null) copy(예정 = 예정 + (D to rid)) else this
    val 남김 = 예정.filterKeys { it < D }
    return copy(예정 = 예정채움(D, idx, 남김))
}

/** 오늘 쉬기 — push: 오늘 루틴을 내일로 / skip: 오늘 루틴을 건너뛰기 (2-5) */
fun 앱데이터.오늘휴식(미루기: Boolean, 오늘: String): 앱데이터 {
    val idx = 순번.indexOfFirst { it.id == 예정[오늘] }
    if (idx < 0) return this
    val 남김 = 예정.filterKeys { it < 오늘 }
    val 시작 = if (미루기) idx else (idx + 1) % 순번.size
    return copy(예정 = 예정채움(날더하기(오늘, 1), 시작, 남김))
}
/** 휴식 물음에 보여줄 '앞으로 사흘' */
fun 앱데이터.사흘미리(미루기: Boolean, 오늘: String): List<String> {
    val 줄 = 순번
    val idx = 줄.indexOfFirst { it.id == 예정[오늘] }
    if (idx < 0 || 줄.isEmpty()) return emptyList()
    val 시작 = if (미루기) idx else idx + 1
    val n = 줄.size
    return (0 until 3).map { 줄[((시작 + it) % n + n) % n].이름 }
}

fun 앱데이터.다음차례(오늘: String): 루틴? {
    val k = 예정.keys.filter { it >= 오늘 }.minOrNull()
    return if (k != null) 예정루틴(k) else 순번.firstOrNull()
}

// ─────────────── 향상도 (6-2) ───────────────

data class 비교(val 지금: Double, val 과거: Double, val pct: Int?, val diff: Double)
data class 대비결과(val n: Int, val 볼륨: 비교, val 과거날: String)

/** 같은 세트 수까지만 잘라서 견준다 (8-3 왜곡 방지 ① ) */
fun 대비(현재: List<세트>, 과거: List<세트>, 과거날: String): 대비결과? {
    val n = min(현재.size, 과거.size)
    if (n == 0) return null
    val a = 볼륨(현재.take(n)); val b = 볼륨(과거.take(n))
    val pct = if (b != 0.0) ((a - b) / b * 100).roundToInt() else null
    return 대비결과(n, 비교(a, b, pct, a - b), 과거날)
}

data class 기준(val k: Int, val 짧: String, val 기간: String)
val 기준목록 = listOf(
    기준(0, "직전", "직전 기록 대비"), 기준(1, "1개월", "1개월 동안"), 기준(3, "3개월", "3개월 동안"),
    기준(6, "6개월", "6개월 동안"), 기준(12, "12개월", "12개월 동안"),
)
fun 앱데이터.지금기준(): 기준 = 기준목록.firstOrNull { it.k == 설정.기준 } ?: 기준목록[2]
fun 앱데이터.기준날(오늘: String): String = if (설정.기준 == 0) 날더하기(오늘, -1) else 개월전(오늘, 설정.기준)

/** 루틴 차원 — 불러온(임시) 종목은 뺀다 */
fun 정식세트(rec: 날기록): List<세트> = rec.종목들.filter { !it.임시 }.flatMap { it.세트들 }

fun 앱데이터.루틴성장(rid: String, 오늘: String, 지금세트들: List<세트>? = null): 대비결과? {
    val 기준일 = 기준날(오늘)
    val 과거날 = 기록.filter { it.value.루틴id == rid && it.key <= 기준일 }.keys.maxOrNull() ?: return null
    var 현재 = 지금세트들
    var 현재날 = 오늘
    if (현재 == null) {
        현재날 = 기록.filter { it.value.루틴id == rid }.keys.maxOrNull() ?: return null
        현재 = 정식세트(기록[현재날]!!)
    }
    if (현재날 == 과거날) return null
    return 대비(현재, 정식세트(기록[과거날]!!), 과거날)
}

/** 종목 차원 — 루틴을 가리지 않는다. 묶음(슈퍼세트)이 같은 날끼리만 */
fun 앱데이터.종목기록들(이름: String, 묶음: String?): List<Pair<String, List<세트>>> =
    기록.keys.sorted().mapNotNull { k ->
        val e = 기록[k]!!.종목들.firstOrNull { it.이름 == 이름 } ?: return@mapNotNull null
        if (e.묶음 != 묶음) null else k to e.세트들
    }

fun 앱데이터.종목성장(이름: String, 오늘: String, 지금세트들: List<세트>? = null, 묶음: String? = null): 대비결과? {
    val 들 = 종목기록들(이름, 묶음)
    val 기준일 = 기준날(오늘)
    val 과거 = 들.lastOrNull { it.first <= 기준일 } ?: return null
    val 최근 = if (!지금세트들.isNullOrEmpty()) 오늘 to 지금세트들 else 들.lastOrNull() ?: return null
    if (과거.first == 최근.first) return null
    return 대비(최근.second, 과거.second, 과거.first)
}

/** 종목 탭의 세 지표 — 최대 1RM / 단일세트 최고(무게×횟수) / 한 세션 볼륨 */
data class 세션지표(val 날: String, val rm: Double, val 최고: 세트, val 볼륨: Double)
data class 지표비교(val 지금: 세션지표, val 과거: 세션지표?)

fun 앱데이터.종목지표(이름: String, 오늘: String): 지표비교? {
    val 들 = 기록.keys.sorted().mapNotNull { k ->
        val 세트들 = 기록[k]!!.종목들.filter { it.이름 == 이름 }.flatMap { it.세트들 }
        if (세트들.isEmpty()) null else 세션지표(
            k, 세트들.maxOf { 일RM(it.w, it.r) }, 세트들.maxBy { it.w * it.r }, 볼륨(세트들),
        )
    }
    val 최근 = 들.lastOrNull() ?: return null
    val 기준일 = 기준날(오늘)
    val 과거 = 들.lastOrNull { it.날 <= 기준일 && it.날 != 최근.날 }
    return 지표비교(최근, 과거)
}

fun 퍼센트(지금: Double, 과거: Double): Int? = if (과거 != 0.0) ((지금 - 과거) / 과거 * 100).roundToInt() else null

// ─────────────── 운동 실행 (5) ───────────────

fun 세션종목.총칸(): Int = max(세트, 기록.size)
fun 세션종목.찬것(): List<세트> = 기록.filterNotNull()
fun 세션종목.덜한가(): Boolean = 찬것().size < 총칸()
fun 세션종목.세트휴식(k: Int): Int = 휴식들.칸(k) ?: 휴식

/** k 번째 세트에 보일 값 — 기록 → 따로 고친 값 → (지금 세트면) 입력 중인 값 → 종목 기본값 */
fun 운동세션.세트값(e: 세션종목, k: Int, 지금이면: Boolean = true): 세트 =
    e.기록.칸(k) ?: e.예정값.칸(k)
    ?: (if (지금이면 && e === 종목들.getOrNull(i) && k == s) 세트(무게, 횟수) else 세트(e.무게, e.횟수))

fun 다음빈칸(e: 세션종목, 부터: Int): Int {
    for (k in 부터 + 1 until e.총칸()) if (e.기록.칸(k) == null) return k
    return e.총칸()
}

val 운동세션.지금종목: 세션종목 get() = 종목들[i]
fun 운동세션.정식(): List<세션종목> = 종목들.filter { !it.임시 }
fun 운동세션.목표세트(): Int = 정식().sumOf { it.계획세트 }
fun 운동세션.한세트수(): Int = 정식().sumOf { it.찬것().size }
fun 운동세션.오늘볼륨(): Double = 정식().sumOf { 볼륨(it.찬것()) }
fun 운동세션.목표볼륨(): Double = 정식().sumOf { it.목표볼륨() }
/** 한 종목의 목표 볼륨 — 루틴에 정한 세트까지, 세트마다의 목표로 */
fun 세션종목.목표볼륨(): Double {
    val n = if (계획세트 > 0) 계획세트 else 세트
    return (0 until n).sumOf { k -> (예정값.칸(k) ?: 세트(무게, 횟수)).let { it.w * it.r } }
}
fun 운동세션.루틴달성도(): Int = 목표세트().let { if (it == 0) 0 else (한세트수() * 100.0 / it).roundToInt() }
fun 세션종목.달성도(): Int = if (계획세트 == 0) 0 else (찬것().size * 100.0 / 계획세트).roundToInt()
/** 유효세트 — 계획 세트까지만 (추가한 세트는 향상도에서 뺀다) */
fun 운동세션.유효세트(): List<세트> = 정식().flatMap { it.찬것().take(it.계획세트) }
fun 운동세션.묶음이름(e: 세션종목): String? =
    e.슈퍼?.let { g -> 종목들.filter { it.슈퍼 == g }.map { it.이름 }.sorted().joinToString("+") }

/** 슈퍼세트 식구의 번호들 (목록 순서). 묶음이 아니면 자기 하나 */
fun 운동세션.식구(j: Int): List<Int> {
    val g = 종목들.getOrNull(j)?.슈퍼 ?: return listOf(j)
    return 종목들.indices.filter { 종목들[it].슈퍼 == g }
}
/** 이 종목의 세트 줄에 휴식을 보일까 — 슈퍼세트는 마지막 종목만 (09-21 메모) */
fun 운동세션.휴식보임(j: Int): Boolean = 식구(j).last() == j

/** 운동한 시간(초) — 마무리 화면에 들어오면 멈춘다 */
fun 운동세션.흐른초(지금: Long): Long = ((끝시각 ?: 지금) - 시작시각) / 1000

// ─────────────── 루틴 목표 — 세트마다 따로 (4-2 · 5-5) ───────────────

/** k 번째 세트의 목표 무게 · 횟수 */
fun 루틴종목.목표(k: Int): 세트 = 세트값.getOrNull(k) ?: 세트(무게, 횟수)
/** k 번째 세트 뒤의 휴식(초) */
fun 루틴종목.휴식(k: Int): Int = 휴식값.getOrNull(k) ?: 휴식
/** 루틴 탭에서 고칠 때는 모든 세트를 한꺼번에 */
fun 루틴종목.모두무게(w: Double): 루틴종목 = copy(무게 = w, 세트값 = 세트값.map { it.copy(w = w) })
fun 루틴종목.모두횟수(r: Int): 루틴종목 = copy(횟수 = r, 세트값 = 세트값.map { it.copy(r = r) })
fun 루틴종목.모두휴식(t: Int): 루틴종목 = copy(휴식 = t, 휴식값 = 휴식값.map { t })
/** 세트별 목록을 세트 수만큼 채워 둔다 (비어 있으면 기본값으로) */
private fun 루틴종목.펼친(): 루틴종목 =
    copy(세트값 = (0 until 세트).map { 목표(it) }, 휴식값 = (0 until 세트).map { 휴식(it) })
/** k 번째 세트만 고치기 (09-22: 루틴도 세트마다) */
fun 루틴종목.세트고침(k: Int, w: Double? = null, r: Int? = null, t: Int? = null): 루틴종목 {
    if (k !in 0 until 세트) return this
    val e = 펼친()
    val 새 = e.copy(
        세트값 = e.세트값.mapIndexed { i, s -> if (i == k) 세트(w ?: s.w, r ?: s.r) else s },
        휴식값 = e.휴식값.mapIndexed { i, h -> if (i == k) t ?: h else h },
    )
    return 새.copy(무게 = 새.세트값[0].w, 횟수 = 새.세트값[0].r, 휴식 = 새.휴식값[0])
}
/** ＋ — 누른 줄을 베껴 **바로 아래에** 끼운다. 뒤 번호는 한 칸씩 밀린다 (09-24 메모) */
fun 루틴종목.세트끼우기(k: Int): 루틴종목 {
    val e = 펼친()
    val i = k.coerceIn(0, 세트 - 1)
    return e.copy(
        세트 = 세트 + 1,
        세트값 = e.세트값.toMutableList().also { it.add(i + 1, e.세트값[i]) },
        휴식값 = e.휴식값.toMutableList().also { it.add(i + 1, e.휴식값[i]) },
    )
}
/** − · 휴지통 — 그 줄을 뺀다 (09-24 메모) */
fun 루틴종목.세트빼기(k: Int): 루틴종목 {
    if (세트 <= 1) return this
    val e = 펼친()
    val i = k.coerceIn(0, 세트 - 1)
    return e.copy(
        세트 = 세트 - 1,
        세트값 = e.세트값.toMutableList().also { it.removeAt(i) },
        휴식값 = e.휴식값.toMutableList().also { it.removeAt(i) },
    )
}
/** 루틴에 정해둔 볼륨 (무게 × 횟수 합) */
fun 루틴종목.볼륨(): Double = (0 until 세트).sumOf { 목표(it).let { v -> v.w * v.r } }
/** 이 종목의 1RM — 지난 기록 중 가장 높은 값 (없으면 루틴 목표로) */
fun 앱데이터.종목1RM(이름: String, 지금: 루틴종목? = null): Double {
    val 과거 = 기록.values.flatMap { r -> r.종목들.filter { it.이름 == 이름 }.flatMap { it.세트들 } }.maxOfOrNull { 일RM(it.w, it.r) } ?: 0.0
    val 계획 = 지금?.let { e -> (0 until e.세트).maxOfOrNull { k -> e.목표(k).let { 일RM(it.w, it.r) } } } ?: 0.0
    return max(과거, 계획)
}

/** ＋ — 맨 아래 세트의 무게 · 횟수 · 휴식을 베껴 한 세트 늘린다 (다른 앱들처럼) */
fun 루틴종목.세트더하기(): 루틴종목 {
    val e = 펼친()
    return e.copy(세트 = 세트 + 1, 세트값 = e.세트값 + 목표(세트 - 1), 휴식값 = e.휴식값 + 휴식(세트 - 1))
}
/** − — 맨 아래 세트를 뺀다 (1세트 아래로는 안 줄어든다) */
fun 루틴종목.세트빼기(): 루틴종목 {
    if (세트 <= 1) return this
    val e = 펼친()
    return e.copy(세트 = 세트 - 1, 세트값 = e.세트값.dropLast(1), 휴식값 = e.휴식값.dropLast(1))
}
/** 루틴 줄 요약 — 세트마다 다르면 '60~62.5' 처럼 */
fun 루틴종목.요약(): String {
    val 목 = (0 until 세트).map { 목표(it) }
    val 무 = 목.map { it.w }; val 회 = 목.map { it.r }; val 휴 = (0 until 세트).map { 휴식(it) }
    fun 폭(a: Double, b: Double) = if (a == b) 무게글(a) else "${무게글(a)}~${무게글(b)}"
    fun 폭(a: Int, b: Int) = if (a == b) "$a" else "$a~$b"
    val 무글 = if (무.isEmpty()) 무게글(무게) else 폭(무.min(), 무.max())
    val 회글 = if (회.isEmpty()) "$횟수" else 폭(회.min(), 회.max())
    val 휴글 = if (휴.isEmpty() || 휴.min() == 휴.max()) 분초(휴.firstOrNull() ?: 휴식) else "${분초(휴.min())}~${분초(휴.max())}"
    return "${무글}kg×${회글}회×${세트}세트·$휴글"
}

fun 운동시작(r: 루틴, 지금: Long): 운동세션? {
    if (r.휴식일 || r.종목.isEmpty()) return null
    val 들 = r.종목.map {
        val 첫 = it.목표(0)
        세션종목(it.이름, it.세트, it.세트, 첫.w, 첫.r, it.휴식,
            예정값 = if (it.세트값.isEmpty()) emptyList() else List(it.세트) { k -> it.목표(k) },
            휴식들 = List(it.세트) { k -> it.휴식(k) }, 슈퍼 = it.슈퍼)
    }
    return 운동세션(r.id, r.이름, 지금, 0, 0, 들[0].무게, 들[0].횟수, 들)
}

private fun 운동세션.종목바꿈(idx: Int, f: (세션종목) -> 세션종목): 운동세션 =
    copy(종목들 = 종목들.mapIndexed { j, e -> if (j == idx) f(e) else e })

/** 자리 옮기기 — 그 세트의 값을 입력칸에 올린다 */
fun 운동세션.자리로(ni: Int, ns: Int): 운동세션 {
    val e = 종목들.getOrNull(ni) ?: return this
    val v = e.기록.칸(ns) ?: e.예정값.칸(ns) ?: 세트(e.무게, e.횟수)
    return copy(i = ni, s = ns, 무게 = v.w, 횟수 = v.r, 끝화면 = false, 끝시각 = null)
}

/** 마무리 화면으로 — 운동 시간을 여기서 멈춘다 */
fun 운동세션.끝냄(지금: Long): 운동세션 = copy(휴식 = null, 끝화면 = true, 끝시각 = 끝시각 ?: 지금)

/** 마무리 화면에서 운동으로 돌아가기 — 마무리 화면에 머문 시간은 빼고 이어서 잰다 */
fun 운동세션.재개(지금: Long): 운동세션 {
    val 멈춘 = 끝시각?.let { max(0L, 지금 - it) } ?: 0L
    return copy(끝화면 = false, 끝시각 = null, 시작시각 = 시작시각 + 멈춘)
}

/** 이 휴식이 (j, k) 세트 줄의 것인가 */
fun 운동세션.휴식자리(j: Int, k: Int): Boolean {
    val h = 휴식 ?: return false
    return h.k == k && (if (h.종목 >= 0) h.종목 == j else i == j)
}

/**
 * 체크 — j 번째 종목의 k 번째 세트.
 *  · 이미 한 세트면 → 체크 풀기. 그 칸만 비고, 값은 남겨 두며, 그 세트가 '지금 할 세트'가 된다
 *    (09-21 메모: 풀었다 다시 체크하면 휴식이 안 돌던 것 — 지금 세트가 아니어서였다)
 *  · 안 한 세트면 → 앞에 빈 칸이 있으면 그 자리로 당겨 온 뒤(1·2 하고 4 체크 → 1·2·3), 기록하고 휴식
 */
fun 운동세션.체크(j: Int, k: Int, 지금: Long): 운동세션 {
    val e0 = 종목들.getOrNull(j) ?: return this
    val rec0 = e0.기록.칸(k)
    if (rec0 != null) {
        var S = 종목바꿈(j) { it.copy(기록 = it.기록.칸바꿈(k, null), 예정값 = it.예정값.칸바꿈(k, rec0)) }
        if (S.휴식자리(j, k)) S = S.copy(휴식 = null)
        return S.자리로(j, k)
    }
    var T = this
    var kk = k
    val 첫빈 = (0 until k).firstOrNull { e0.기록.칸(it) == null }
    if (첫빈 != null) {
        // 입력 중이던 '지금 세트' 값을 먼저 그 줄에 적어 둔다 — 줄이 움직여도 값이 따라가게
        if (T.i == j && T.s < e0.총칸() && e0.기록.칸(T.s) == null) T = T.종목바꿈(j) { it.copy(예정값 = it.예정값.칸바꿈(T.s, 세트(T.무게, T.횟수))) }
        T = T.종목바꿈(j) { it.copy(기록 = it.기록.당김(첫빈, k), 예정값 = it.예정값.당김(첫빈, k), 휴식들 = it.휴식들.당김(첫빈, k)) }
        kk = 첫빈
    }
    // 당겼으면 그 칸 값을 새로 올려야 한다 (지금 세트 자리와 번호가 같아도 값은 옮겨 온 줄의 것)
    val S0 = if (첫빈 != null || j != T.i || kk != T.s) T.자리로(j, kk) else T
    return S0.지금체크(지금)
}

/** k 번째 칸을 f 번째 자리로 당기고, 그 사이 칸들은 한 칸씩 뒤로 */
private fun <T> List<T?>.당김(f: Int, k: Int): List<T?> {
    val m = toMutableList()
    while (m.size <= k) m.add(null)
    val x = m.removeAt(k)
    m.add(f, x)
    return m
}

/** 옛 호출 모양 — 지금 종목의 k 번째 */
fun 운동세션.체크(k: Int, 지금: Long): 운동세션 = 체크(i, k, 지금)

private fun 운동세션.지금체크(지금: Long): 운동세션 {
    val k = s
    val v = 세트(무게, 횟수)
    var S = 종목바꿈(i) { it.copy(기록 = it.기록.칸바꿈(k, v)) }
    val ex = S.지금종목

    // 슈퍼세트 (09-21 메모: 뒤죽박죽 체크해도 되게)
    //  · 덜 한 종목이 있으면 → 쉬지 않고 그 종목으로 (가장 덜 한 것, 같으면 목록 순서)
    //  · 모두 같은 수만큼 했으면 → 한 바퀴 끝. 마지막 종목의 휴식으로 쉰 뒤 가장 덜 한 종목부터
    if (ex.슈퍼 != null) {
        val 식구 = S.종목들.withIndex().filter { it.value.슈퍼 == ex.슈퍼 && !it.value.마감 }.map { it.index }
        if (S.i in 식구 && 식구.size > 1) {
            val 수 = 식구.associateWith { S.종목들[it].찬것().size }
            val 남은 = 식구.filter { S.종목들[it].덜한가() }
            if (남은.isNotEmpty()) {
                val 가장많이 = 수.values.max()
                val 뒤처진 = 남은.filter { 수.getValue(it) < 가장많이 }
                val 다음 = (if (뒤처진.isNotEmpty()) 뒤처진 else 남은).minBy { 수.getValue(it) }
                val 다음칸 = 다음빈칸(S.종목들[다음], -1)
                if (뒤처진.isNotEmpty()) return S.copy(휴식 = null).자리로(다음, 다음칸)
                // 한 바퀴 끝 — 휴식은 마지막 종목의 그 바퀴 세트 줄에서
                val 끝 = 식구.last()
                val 바퀴 = max(0, 수.getValue(끝) - 1)
                val 쉴 = S.종목들[끝].세트휴식(바퀴)
                S = S.copy(s = 다음빈칸(S.지금종목, k))
                return S.copy(휴식 = 휴식중(바퀴, 지금 + 쉴 * 1000L, false, 다음, 다음칸, 총초 = 쉴, 종목 = 끝))
            }
        }
    }
    S = S.copy(s = 다음빈칸(S.지금종목, k))
    // 모든 종목을 다 했으면 쉬지 않고 마무리
    if (S.종목들.all { it.마감 || !it.덜한가() }) return S.끝냄(지금)
    return S.휴식시작(k, 지금)
}

fun 운동세션.휴식시작(k: Int, 지금: Long, 다음i: Int? = null, 다음s: Int? = null): 운동세션 {
    val 쉴 = 지금종목.세트휴식(k)
    return copy(휴식 = 휴식중(k, 지금 + 쉴 * 1000L, false, 다음i, 다음s, 총초 = 쉴, 종목 = i))
}

fun 운동세션.휴식조절(초: Int, 지금: Long): 운동세션 {
    val h = 휴식 ?: return this
    return copy(휴식 = h.copy(끝시각 = max(지금, h.끝시각 + 초 * 1000L), 물음 = false))
}

/** 휴식이 끝났을 때 — 넘어가기 전 확인이 켜져 있으면 묻는다 */
fun 운동세션.휴식끝(설정: 설정값, 지금: Long = System.currentTimeMillis()): 운동세션 {
    val h = 휴식 ?: return this
    return if (설정.넘어가기전확인 || !설정.자동진행) copy(휴식 = h.copy(물음 = true)) else 다음으로(지금)
}

/** 휴식을 치우고 다음 세트로 (슈퍼세트면 기다리던 자리로). 이 종목을 다 했으면 덜 한 종목으로 */
fun 운동세션.다음으로(지금: Long = System.currentTimeMillis()): 운동세션 {
    val h = 휴식
    val S = copy(휴식 = null)
    if (h?.다음i != null && h.다음s != null) return S.자리로(h.다음i, h.다음s)
    if (S.s >= S.지금종목.총칸()) return S.다음종목으로(false, 지금)
    return S.자리로(S.i, S.s)
}

fun 운동세션.종목으로(idx: Int): 운동세션 {
    if (idx !in 종목들.indices) return this
    val 빈칸 = 다음빈칸(종목들[idx], -1)
    return copy(휴식 = null).자리로(idx, if (빈칸 < 종목들[idx].총칸()) 빈칸 else 0)
}

/** '다음' — 뒤쪽의 덜 한 종목 → 앞쪽 → 없으면 마무리 (5-1 ⑥) */
fun 운동세션.다음종목으로(끝낼까: Boolean, 지금: Long = System.currentTimeMillis()): 운동세션 {
    var S = copy(휴식 = null)
    if (끝낼까) S = S.종목바꿈(i) { it.copy(마감 = true) }
    var n = S.종목들.withIndex().indexOfFirst { (j, e) -> j > S.i && !e.마감 && e.덜한가() }
    if (n < 0) n = S.종목들.withIndex().indexOfFirst { (j, e) -> j != S.i && !e.마감 && e.덜한가() }
    return if (n >= 0) S.종목으로(n) else S.끝냄(지금)
}

/** ＋ — 맨 아래 세트를 그대로 베낀다. 슈퍼세트면 묶인 종목 전부에 (5-1 ③, 8-2) */
fun 운동세션.세트추가(j: Int = i): 운동세션 {
    val ex = 종목들.getOrNull(j) ?: return this
    return copy(종목들 = 종목들.mapIndexed { jj, x ->
        if (x !== ex && (ex.슈퍼 == null || x.슈퍼 != ex.슈퍼)) return@mapIndexed x
        val 끝 = x.총칸() - 1
        val 베낄 = if (끝 >= 0) 세트값(x, 끝, jj == i) else 세트(x.무게, x.횟수)
        val 쉴 = if (끝 >= 0) x.세트휴식(끝) else x.휴식
        val 새 = x.총칸()
        x.copy(세트 = 새 + 1, 예정값 = x.예정값.칸바꿈(새, 베낄), 휴식들 = x.휴식들.칸바꿈(새, 쉴))
    })
}

fun 운동세션.세트삭제(j: Int, k: Int): 운동세션 {
    var S = 종목바꿈(j) { e ->
        fun <T> List<T?>.뺌(): List<T?> = if (k in indices) toMutableList().also { it.removeAt(k) } else this
        e.copy(기록 = e.기록.뺌(), 휴식들 = e.휴식들.뺌(), 예정값 = e.예정값.뺌(), 세트 = max(1, e.세트 - 1))
    }
    if (S.휴식자리(j, k)) S = S.copy(휴식 = null)
    return if (j == S.i && S.s > k) S.copy(s = S.s - 1) else S
}
fun 운동세션.세트삭제(k: Int): 운동세션 = 세트삭제(i, k)

/** 세트 값 고치기 — 한 세트면 기록을, 지금 세트면 입력값을, 아직이면 그 줄만 */
fun 운동세션.값고치기(j: Int, k: Int, 새무게: Double? = null, 새횟수: Int? = null): 운동세션 {
    val e = 종목들.getOrNull(j) ?: return this
    val w = 새무게?.let { 무게반올림(max(0.0, it)) }
    val r = 새횟수?.let { max(0, it) }
    val rec = e.기록.칸(k)
    // 09-26 메모: 운동 중 무게 · 횟수를 바꿔도 화면이 안 바뀌었다.
    // 지금 세트는 '입력 중인 값(무게·횟수)'만 바꾸고 있었는데, 화면은 루틴에서 가져온 '세트별 값(예정값)'을 먼저 보여 줬다.
    // → 지금 세트도 예정값에 같이 적는다 (화면 · 체크 모두 같은 값)
    return when {
        rec != null -> 종목바꿈(j) { it.copy(기록 = it.기록.칸바꿈(k, 세트(w ?: rec.w, r ?: rec.r))) }
        else -> {
            val 이제 = 세트값(e, k)
            val 새 = 세트(w ?: 이제.w, r ?: 이제.r)
            val T = 종목바꿈(j) { it.copy(예정값 = it.예정값.칸바꿈(k, 새)) }
            if (j == i && k == s) T.copy(무게 = 새.w, 횟수 = 새.r) else T
        }
    }
}
fun 운동세션.값고치기(k: Int, 새무게: Double? = null, 새횟수: Int? = null): 운동세션 = 값고치기(i, k, 새무게, 새횟수)
fun 운동세션.휴식고치기(j: Int, k: Int, 초: Int): 운동세션 = 종목바꿈(j) { it.copy(휴식들 = it.휴식들.칸바꿈(k, 초.coerceIn(0, 휴식최대))) }
fun 운동세션.휴식고치기(k: Int, 초: Int): 운동세션 = 휴식고치기(i, k, 초)

/** 운동 추가 — 오늘만. 지금 종목(묶음이면 묶음 전체) 바로 뒤에 끼운다 (5-2) */
fun 운동세션.불러오기(이름: String, 기본휴식: Int, 세트수: Int = 3): 운동세션 {
    val n = max(1, 세트수)
    val 새 = 세션종목(이름, n, 0, 20.0, 10, 기본휴식, 휴식들 = List(n) { 기본휴식 }, 임시 = true)
    val 뒤 = 식구(i).last() + 1
    return copy(종목들 = 종목들.toMutableList().also { it.add(뒤, 새) })
}

/**
 * 오늘만 타이트하게 (09-24 메모 · 업데이트 예정 ⑱)
 *  · 세트빼기 = 아직 덜 한 종목마다 **아직 하지 않은 마지막 세트**를 하나 뺀다 (이미 한 세트는 건드리지 않는다)
 *  · 휴식줄임 = 아직 남은 세트의 휴식을 그만큼 줄인다 (0초 아래로는 안 내려간다)
 * 오늘 기록에만 적용된다 — 루틴 원본은 그대로 (명세 5-5 ①)
 */
fun 운동세션.타이트하게(세트빼기: Boolean, 휴식줄임: Int): 운동세션 {
    var S = this
    if (세트빼기) {
        S.종목들.indices.reversed().forEach { j ->
            val e = S.종목들[j]
            val 끝칸 = e.총칸() - 1
            if (e.덜한가() && 끝칸 >= 1 && e.기록.칸(끝칸) == null) S = S.세트삭제(j, 끝칸)
        }
    }
    if (휴식줄임 > 0) {
        S = S.copy(종목들 = S.종목들.mapIndexed { j, e ->
            e.copy(휴식들 = (0 until e.총칸()).map { k ->
                val 지금쉼 = e.휴식들.칸(k) ?: e.휴식
                if (e.기록.칸(k) != null) 지금쉼 else max(0, 지금쉼 - 휴식줄임)
            })
        })
    }
    return S
}

fun 운동세션.마감풀기(): 운동세션 = 종목바꿈(i) { it.copy(마감 = false) }

/** 기록 저장 — 오늘 기록을 남기고, 내일부터 다음 차례로 다시 깐다 */
fun 앱데이터.운동저장(오늘: String, 지금: Long): 앱데이터 {
    val S = 세션 ?: return this
    val 들 = S.종목들.mapNotNull { e ->
        val 찬 = e.찬것()
        if (찬.isEmpty()) null else 종목기록(e.이름, 찬, e.임시, S.묶음이름(e))
    }
    val rec = 날기록(S.루틴id, S.루틴이름, S.한세트수() >= S.목표세트(), 들, S.흐른초(지금).toInt())
    // 루틴 반영(5-5) 뒤에, 설정이 켜져 있으면 볼륨을 한 번 더 올린다 (09-24)
    val 올릴까 = 설정.볼륨켬 && (설정.볼륨언제 == "항상" || rec.달성)
    val 새 = copy(
        기록 = 기록 + (오늘 to rec), 세션 = null,
        루틴들 = 루틴들.map { if (it.id == S.루틴id) it.오늘반영(S).let { r -> if (올릴까) r.볼륨올리기(설정) else r } else it },
    )
    val 줄 = 순번
    val i = 줄.indexOfFirst { it.id == S.루틴id }
    // 자동생성이 꺼진 루틴으로 운동했으면 순서는 건드리지 않는다
    if (줄.isEmpty() || i < 0) return 새
    val 남김 = 새.예정.filterKeys { it < 오늘 }
    return 새.copy(예정 = 새.예정채움(날더하기(오늘, 1), (i + 1) % 줄.size, 남김))
}

/**
 * 오래 손대지 않은 운동은 저절로 끝낸다 (09-25 메모: 어제 시작한 운동이 다음 날까지 돌고 있었다)
 *  · 마지막으로 손댄 뒤 [한계] 가 지나면 끝낸다 (기본 3시간)
 *  · 체크한 세트가 있으면 **운동을 시작한 날**의 기록으로 저장한다 (루틴 반영 · 다음 차례도 저장과 같게)
 *    그 날에 이미 기록이 있으면 덮어쓰지 않고 이 운동은 버린다
 *  · 체크한 세트가 없으면 그냥 버린다
 */
fun 앱데이터.오래된운동정리(지금: Long, 한계: Long = 3 * 60 * 60 * 1000L): 앱데이터 {
    val S = 세션 ?: return this
    val 마지막 = if (S.마지막 > 0) S.마지막 else S.시작시각
    if (지금 - 마지막 < 한계) return this
    if (S.한세트수() == 0) return copy(세션 = null)
    val 날 = java.time.Instant.ofEpochMilli(S.시작시각).atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString()
    if (기록.containsKey(날)) return copy(세션 = null)
    return 운동저장(날, S.끝시각 ?: 마지막)
}

/**
 * 볼륨 자동 올리기 (09-24 · 업데이트 예정 ⑲ — 써보면서 다듬는다)
 *  · 언제: 설정이 "성공" 이면 그 날 계획한 세트를 다 끝냈을 때만, "항상" 이면 저장할 때마다
 *  · 얼마나: % 또는 kg
 *  · 어디에: "무게" = 세트마다 무게를 올린다 / "횟수" = 횟수를 1회씩 올리다 상한을 넘으면 무게로 넘기고 횟수를 되돌린다
 */
fun 루틴.볼륨올리기(s: 설정값): 루틴 {
    if (!s.볼륨켬) return this
    return copy(종목 = 종목.map { e ->
        val 목 = (0 until e.세트).map { e.목표(it) }
        if (목.isEmpty()) return@map e
        val 새목 = when (s.볼륨배분) {
            "무게" -> 목.map { v ->
                val 더할 = if (s.볼륨방식 == "%") v.w * s.볼륨값 / 100.0 else s.볼륨값
                세트(무게반올림(v.w + 더할), v.r)
            }
            else -> {
                // 횟수에 붙인다 — 한 세트라도 상한 아래면 모두 1회씩, 모두 상한이면 무게로 넘긴다
                if (목.any { it.r < s.횟수상한 }) 목.map { 세트(it.w, min(s.횟수상한, it.r + 1)) }
                else 목.map { v ->
                    val 더할 = if (s.볼륨방식 == "%") v.w * s.볼륨값 / 100.0 else s.볼륨값
                    세트(무게반올림(v.w + max(더할, 0.5)), max(1, s.횟수상한 - 2))
                }
            }
        }
        e.copy(무게 = 새목[0].w, 횟수 = 새목[0].r, 세트값 = 새목, 휴식값 = (0 until e.세트).map { e.휴식(it) })
    })
}

/**
 * 운동 중 바꾼 값을 다음 루틴에 (기능명세 5-5, 09-22 홍겸 님)
 *  ① 세트 수 · 오늘만 끼운 종목 → 넘어가지 않는다 (루틴 세트 수 그대로)
 *  ② 무게 · 횟수 · 휴식 → 넘어간다. 세트마다 따로, 체크한 세트만, 루틴에 있던 세트 번호까지만
 * 같은 이름이 루틴에 두 번 있으면 나오는 순서대로 짝짓는다.
 */
fun 루틴.오늘반영(S: 운동세션): 루틴 {
    val 정식 = S.종목들.filter { !it.임시 }
    val 쓴 = mutableMapOf<String, Int>()
    return copy(종목 = 종목.map { re ->
        val n = 쓴.getOrDefault(re.이름, 0); 쓴[re.이름] = n + 1
        val e = 정식.filter { it.이름 == re.이름 }.getOrNull(n) ?: return@map re
        var 바뀜 = false
        val 목 = (0 until re.세트).map { k -> e.기록.칸(k)?.also { if (it != re.목표(k)) 바뀜 = true } ?: re.목표(k) }
        val 휴 = (0 until re.세트).map { k -> if (e.기록.칸(k) != null) e.세트휴식(k).also { if (it != re.휴식(k)) 바뀜 = true } else re.휴식(k) }
        if (!바뀜) re
        else re.copy(무게 = 목.firstOrNull()?.w ?: re.무게, 횟수 = 목.firstOrNull()?.r ?: re.횟수, 휴식 = 휴.firstOrNull() ?: re.휴식,
            세트값 = 목, 휴식값 = 휴)
    })
}

// ─────────────── 마무리 화면 — 직전 대비 · 추이 그래프 (09-21 메모) ───────────────

/** 같은 루틴의 직전 기록 (오늘 것 제외) */
fun 앱데이터.직전기록(rid: String, 오늘: String): Pair<String, 날기록>? =
    기록.filter { it.value.루틴id == rid && it.key < 오늘 }.maxByOrNull { it.key }?.toPair()

enum class 묶기(val 이름: String) { 일("일"), 주("주"), 월("월") }
data class 점(val 날: String, val 값: Double)

/**
 * 한 종목의 변화 — 일: 한 세션씩 / 주: 월요일부터 한 주 / 월: 한 달.
 * 볼륨은 기간 안의 합, 1RM 은 기간 안의 최고. 오늘 아직 저장하지 않은 세트도 넣는다.
 */
fun 앱데이터.종목추이(이름: String, 단위: 묶기, 일RM으로: Boolean, 오늘: String, 오늘세트: List<세트>): List<점> {
    val 날별 = sortedMapOf<String, List<세트>>()
    기록.forEach { (k, rec) ->
        val s = rec.종목들.filter { it.이름 == 이름 }.flatMap { it.세트들 }
        if (s.isNotEmpty()) 날별[k] = s
    }
    if (오늘세트.isNotEmpty()) 날별[오늘] = 오늘세트
    fun 기간(k: String): String = when (단위) {
        묶기.일 -> k
        묶기.주 -> 키(날(k).minusDays((날(k).dayOfWeek.value - 1).toLong()))
        묶기.월 -> k.substring(0, 7)
    }
    val 모음 = linkedMapOf<String, MutableList<List<세트>>>()
    날별.forEach { (k, s) -> 모음.getOrPut(기간(k)) { mutableListOf() }.add(s) }
    val 점들 = 모음.map { (p, 들) ->
        점(p, if (일RM으로) 들.flatten().maxOf { 일RM(it.w, it.r) } else 들.sumOf { 볼륨(it) })
    }
    val 최대 = when (단위) { 묶기.일 -> 30; 묶기.주 -> 26; 묶기.월 -> 24 }
    return 점들.takeLast(최대)
}

/** 카테고리 지우기 — 그 부위의 종목도 함께 (종목 탭 ⚙ 와 같은 동작. 5초 되돌리기) */
fun 앱데이터.카테고리지우기(p: String): 앱데이터 {
    val 이름들 = 종목표.filter { it.부위 == p }.map { it.이름 }.toSet()
    return copy(
        카테고리 = 카테고리 - p,
        종목표 = 종목표.filter { it.부위 != p },
        루틴들 = 루틴들.map { r -> r.copy(종목 = r.종목.filter { it.이름 !in 이름들 }) },
    )
}

// ─────────────── 루틴 편집 ───────────────

fun 앱데이터.루틴바꿈(id: String, f: (루틴) -> 루틴): 앱데이터 =
    copy(루틴들 = 루틴들.map { if (it.id == id) f(it) else it })

/**
 * 루틴 합치기 (09-25 메모 · 09-26 답) — 두 루틴은 그대로 두고 **새 루틴을 하나 만든다**.
 *  · 위id 의 종목이 먼저, 아래id 의 종목이 뒤에
 *  · 같은 종목이 두 루틴에 다 있으면 **둘 다 넣는다** (09-26 홍겸 님)
 *  · 슈퍼세트 묶음은 그대로 옮긴다. 두 루틴의 묶음 이름이 겹치지 않게 새 이름을 붙인다
 *  · 새 루틴은 맨 아래, 자동생성 꺼짐. 휴식일은 합치지 않는다
 */
fun 앱데이터.루틴합치기(위id: String, 아래id: String, 새id: String): 앱데이터 {
    val a = 루틴(위id) ?: return this
    val b = 루틴(아래id) ?: return this
    if (위id == 아래id || a.휴식일 || b.휴식일) return this
    fun 옮김(r: 루틴, 표: String) = r.종목.map { e -> e.copy(슈퍼 = e.슈퍼?.let { "$새id-$표-$it" }) }
    val 새 = 루틴(새id, "${a.이름} + ${b.이름}", 종목 = 옮김(a, "a") + 옮김(b, "b"), 자동생성 = false)
    return copy(루틴들 = 루틴들 + 새)
}

/** 루틴 순서 옮기기 — 끌어서 다른 루틴의 위 · 아래 끝에 놓았을 때 (09-26) */
fun 앱데이터.루틴옮기기(집은id: String, 대상id: String, 뒤에: Boolean): 앱데이터 {
    if (집은id == 대상id) return this
    val 집은 = 루틴(집은id) ?: return this
    val 남은 = 루틴들.filter { it.id != 집은id }.toMutableList()
    val t = 남은.indexOfFirst { it.id == 대상id }
    if (t < 0) return this
    남은.add(if (뒤에) t + 1 else t, 집은)
    return copy(루틴들 = 남은)
}

/** 순서 옮기기 — 묶음이면 통째로. 모드: 앞(before) / 뒤(after) */
fun 루틴.종목옮기기(from: Int, to: Int, 뒤에: Boolean): 루틴 {
    val 대상 = 종목.getOrNull(to) ?: return this
    val 집은 = 종목[from]
    val 옮길 = if (집은.슈퍼 != null) 종목.filter { it.슈퍼 == 집은.슈퍼 } else listOf(집은)
    if (옮길.contains(대상)) return this
    val 남은 = 종목.filter { it !in 옮길 }.toMutableList()
    // 대상이 묶음이면 그 묶음 전체의 앞/뒤로
    val 대상식구 = if (대상.슈퍼 != null) 남은.filter { it.슈퍼 == 대상.슈퍼 } else listOf(대상)
    val at = if (뒤에) 남은.indexOf(대상식구.last()) + 1 else 남은.indexOf(대상식구.first())
    남은.addAll(at, 옮길)
    return copy(종목 = 남은)
}

/** 슈퍼세트로 묶기 — 대상(또는 그 묶음) 바로 뒤로 옮겨 붙인다 */
fun 루틴.슈퍼묶기(from: Int, to: Int, 새이름: String): 루틴 {
    val 대상 = 종목.getOrNull(to) ?: return this
    val 집은 = 종목[from]
    if (집은.슈퍼 != null && 집은.슈퍼 == 대상.슈퍼) return this
    val g = 대상.슈퍼 ?: 새이름
    val 옮길 = (if (집은.슈퍼 != null) 종목.filter { it.슈퍼 == 집은.슈퍼 } else listOf(집은)).map { it.copy(슈퍼 = g) }
    val 옛것 = if (집은.슈퍼 != null) 종목.filter { it.슈퍼 == 집은.슈퍼 } else listOf(집은)
    val 남은 = 종목.filter { it !in 옛것 }.map { if (it == 대상) it.copy(슈퍼 = g) else it }.toMutableList()
    val at = 남은.indexOfLast { it.슈퍼 == g } + 1
    남은.addAll(at, 옮길)
    return copy(종목 = 남은)
}

fun 루틴.슈퍼풀기(g: String): 루틴 = copy(종목 = 종목.map { if (it.슈퍼 == g) it.copy(슈퍼 = null) else it })

/** 한 명만 남은 묶음은 푼다 */
fun 루틴.묶음정리(): 루틴 {
    val 수 = 종목.groupingBy { it.슈퍼 }.eachCount()
    return copy(종목 = 종목.map { if (it.슈퍼 != null && (수[it.슈퍼] ?: 0) < 2) it.copy(슈퍼 = null) else it })
}
