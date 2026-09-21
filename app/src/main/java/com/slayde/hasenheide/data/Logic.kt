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

/** "3:00" 또는 "180" → 초 */
fun 초읽기(글: String): Int? {
    val t = 글.trim()
    if (t.contains(':')) {
        val (m, s) = t.split(':', limit = 2)
        return ((m.toIntOrNull() ?: 0) * 60 + (s.toIntOrNull() ?: 0)).coerceIn(0, 3600)
    }
    return t.toIntOrNull()?.coerceIn(0, 3600)
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

private fun 앱데이터.예정채움(시작날: String, 시작idx: Int, 바탕: Map<String, String>): Map<String, String> {
    val n = 루틴들.size
    if (n == 0) return 바탕
    val m = 바탕.toMutableMap()
    for (i in 0 until n * 회차) m[날더하기(시작날, i)] = 루틴들[((시작idx + i) % n + n) % n].id
    return m
}

/** 마지막으로 한 운동의 '다음'부터 다시 깐다 */
fun 앱데이터.예정초기화(오늘: String): 앱데이터 {
    if (루틴들.isEmpty()) return copy(예정 = emptyMap())
    val 마지막 = 기록.keys.maxOrNull()
    var idx = 0
    if (마지막 != null) {
        val i = 루틴들.indexOfFirst { it.id == 기록[마지막]!!.루틴id }
        idx = if (i < 0) 0 else (i + 1) % 루틴들.size
    }
    val 시작 = if (기록.containsKey(오늘)) 날더하기(오늘, 1) else 오늘
    return copy(예정 = 예정채움(시작, idx, emptyMap()))
}

/**
 * 앱을 켤 때 · 날짜가 바뀔 때 — 빠진 날을 반영해 예정을 맞춘다 (4. 순서 기준)
 * "오늘 할 것 = 마지막으로 완료한 것의 다음" — 며칠 빠져도 순서가 어긋나지 않는다.
 *  · 지난 날에 깔려 있었는데 기록이 없는 날(빠진 날)이 있으면, 그 중 첫 '운동 날'의 루틴부터 오늘 다시 깐다
 *  · 빠진 날이 전부 휴식일이면 쉰 것으로 치고 그 다음 차례부터
 *  · 빠진 날이 없고 앞으로의 예정도 있으면 그대로 둔다 (옮겨 둔 것을 지키기 위해)
 */
fun 앱데이터.예정맞추기(오늘: String): 앱데이터 {
    if (루틴들.isEmpty()) return copy(예정 = emptyMap())
    val 빠진 = 예정.filter { it.key < 오늘 && !기록.containsKey(it.key) }.toSortedMap()
    val 앞으로 = 예정.keys.any { it >= 오늘 }
    if (빠진.isEmpty()) return if (앞으로) copy(예정 = 예정.filterKeys { it >= 오늘 }) else 예정초기화(오늘)
    val 운동날 = 빠진.values.firstOrNull { rid -> 루틴(rid)?.휴식일 == false }
    val n = 루틴들.size
    val 시작idx = if (운동날 != null) 루틴들.indexOfFirst { it.id == 운동날 }
                  else (루틴들.indexOfFirst { it.id == 빠진.values.last() } + 1) % n
    if (시작idx < 0) return 예정초기화(오늘)
    val 시작 = if (기록.containsKey(오늘)) 날더하기(오늘, 1) else 오늘
    return copy(예정 = 예정채움(시작, 시작idx, emptyMap()))
}

/** 날짜 D 에 루틴을 꽂고 그 날부터 순서를 다시 깐다. 앞선 날은 건드리지 않는다 (2-3) */
fun 앱데이터.꽂기(rid: String, D: String, 오늘: String): 앱데이터 {
    if (D < 오늘) return this
    val idx = 루틴들.indexOfFirst { it.id == rid }
    if (idx < 0) return this
    val 남김 = 예정.filterKeys { it < D }
    return copy(예정 = 예정채움(D, idx, 남김))
}

/** 오늘 쉬기 — push: 오늘 루틴을 내일로 / skip: 오늘 루틴을 건너뛰기 (2-5) */
fun 앱데이터.오늘휴식(미루기: Boolean, 오늘: String): 앱데이터 {
    val idx = 루틴들.indexOfFirst { it.id == 예정[오늘] }
    if (idx < 0) return this
    val 남김 = 예정.filterKeys { it < 오늘 }
    val 시작 = if (미루기) idx else (idx + 1) % 루틴들.size
    return copy(예정 = 예정채움(날더하기(오늘, 1), 시작, 남김))
}
/** 휴식 물음에 보여줄 '앞으로 사흘' */
fun 앱데이터.사흘미리(미루기: Boolean, 오늘: String): List<String> {
    val idx = 루틴들.indexOfFirst { it.id == 예정[오늘] }
    if (idx < 0 || 루틴들.isEmpty()) return emptyList()
    val 시작 = if (미루기) idx else idx + 1
    val n = 루틴들.size
    return (0 until 3).map { 루틴들[((시작 + it) % n + n) % n].이름 }
}

fun 앱데이터.다음차례(오늘: String): 루틴? {
    val k = 예정.keys.filter { it >= 오늘 }.minOrNull()
    return if (k != null) 예정루틴(k) else 루틴들.firstOrNull()
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
fun 운동세션.목표볼륨(): Double = 정식().sumOf { it.무게 * it.횟수 * it.계획세트 }
fun 운동세션.루틴달성도(): Int = 목표세트().let { if (it == 0) 0 else (한세트수() * 100.0 / it).roundToInt() }
fun 세션종목.달성도(): Int = if (계획세트 == 0) 0 else (찬것().size * 100.0 / 계획세트).roundToInt()
/** 유효세트 — 계획 세트까지만 (추가한 세트는 향상도에서 뺀다) */
fun 운동세션.유효세트(): List<세트> = 정식().flatMap { it.찬것().take(it.계획세트) }
fun 운동세션.묶음이름(e: 세션종목): String? =
    e.슈퍼?.let { g -> 종목들.filter { it.슈퍼 == g }.map { it.이름 }.sorted().joinToString("+") }

fun 운동시작(r: 루틴, 지금: Long): 운동세션? {
    if (r.휴식일 || r.종목.isEmpty()) return null
    val 들 = r.종목.map {
        세션종목(it.이름, it.세트, it.세트, it.무게, it.횟수, it.휴식,
            휴식들 = List(it.세트) { _ -> it.휴식 }, 슈퍼 = it.슈퍼)
    }
    return 운동세션(r.id, r.이름, 지금, 0, 0, 들[0].무게, 들[0].횟수, 들)
}

private fun 운동세션.종목바꿈(idx: Int, f: (세션종목) -> 세션종목): 운동세션 =
    copy(종목들 = 종목들.mapIndexed { j, e -> if (j == idx) f(e) else e })

/** 자리 옮기기 — 그 세트의 값을 입력칸에 올린다 */
fun 운동세션.자리로(ni: Int, ns: Int): 운동세션 {
    val e = 종목들.getOrNull(ni) ?: return this
    val v = e.기록.칸(ns) ?: e.예정값.칸(ns) ?: 세트(e.무게, e.횟수)
    return copy(i = ni, s = ns, 무게 = v.w, 횟수 = v.r, 끝화면 = false)
}

/** 체크 결과 — 다음 할 일을 알려준다 */
fun 운동세션.체크(k: Int, 지금: Long): 운동세션 {
    val e = 지금종목
    if (e.기록.칸(k) != null) return 종목바꿈(i) { it.copy(기록 = it.기록.칸바꿈(k, null)) }   // 체크 풀기 — 그 칸만 빈다
    val 지금세트 = k == s
    val v = if (지금세트) 세트(무게, 횟수) else 세트값(e, k, false)
    var S = 종목바꿈(i) { it.copy(기록 = it.기록.칸바꿈(k, v)) }
    if (!지금세트) return S
    val ex = S.지금종목

    // 슈퍼세트 — 묶인 종목을 한 바퀴 돈 뒤에만 쉰다 (8-2)
    if (ex.슈퍼 != null) {
        val 식구 = S.종목들.withIndex().filter { it.value.슈퍼 == ex.슈퍼 && !it.value.마감 }.map { it.index }
        val 자리 = 식구.indexOf(S.i)
        if (자리 >= 0 && 자리 < 식구.size - 1) return S.copy(휴식 = null).자리로(식구[자리 + 1], k)
        if (자리 >= 0) {
            val 첫 = S.종목들[식구[0]]
            val 다음칸 = k + 1
            S = S.copy(s = 다음빈칸(ex, k))
            if (다음칸 < 첫.총칸()) return S.휴식시작(k, 지금, 식구[0], 다음칸)
        }
    }
    S = S.copy(s = 다음빈칸(S.지금종목, k))
    val 마지막 = S.s >= S.지금종목.총칸()
    if (마지막 && S.종목들.withIndex().all { (j, x) -> j == S.i || x.마감 || !x.덜한가() }) {
        return S.copy(휴식 = null, 끝화면 = true)
    }
    return S.휴식시작(k, 지금)
}

fun 운동세션.휴식시작(k: Int, 지금: Long, 다음i: Int? = null, 다음s: Int? = null): 운동세션 {
    val 쉴 = 지금종목.세트휴식(k)
    return copy(휴식 = 휴식중(k, 지금 + 쉴 * 1000L, false, 다음i, 다음s))
}

fun 운동세션.휴식조절(초: Int, 지금: Long): 운동세션 {
    val h = 휴식 ?: return this
    return copy(휴식 = h.copy(끝시각 = max(지금, h.끝시각 + 초 * 1000L), 물음 = false))
}

/** 휴식이 끝났을 때 — 넘어가기 전 확인이 켜져 있으면 묻는다 */
fun 운동세션.휴식끝(설정: 설정값): 운동세션 {
    val h = 휴식 ?: return this
    return if (설정.넘어가기전확인 || !설정.자동진행) copy(휴식 = h.copy(물음 = true)) else 다음으로()
}

/** 휴식을 치우고 다음 세트로 (슈퍼세트면 기다리던 자리로) */
fun 운동세션.다음으로(): 운동세션 {
    val h = 휴식
    val S = copy(휴식 = null)
    if (h?.다음i != null && h.다음s != null) return S.자리로(h.다음i, h.다음s)
    if (S.s >= S.지금종목.총칸()) {
        return if (S.i < S.종목들.size - 1) S.자리로(S.i + 1, 다음빈칸(S.종목들[S.i + 1], -1)) else S.copy(끝화면 = true)
    }
    return S.자리로(S.i, S.s)
}

fun 운동세션.종목으로(idx: Int): 운동세션 {
    if (idx !in 종목들.indices) return this
    return copy(휴식 = null).자리로(idx, 다음빈칸(종목들[idx], -1))
}

/** '다음' — 뒤쪽의 덜 한 종목 → 앞쪽 → 없으면 마무리 (5-1 ⑥) */
fun 운동세션.다음종목으로(끝낼까: Boolean): 운동세션 {
    var S = copy(휴식 = null)
    if (끝낼까) S = S.종목바꿈(i) { it.copy(마감 = true) }
    var n = S.종목들.withIndex().indexOfFirst { (j, e) -> j > S.i && !e.마감 && e.덜한가() }
    if (n < 0) n = S.종목들.withIndex().indexOfFirst { (j, e) -> j != S.i && !e.마감 && e.덜한가() }
    return if (n >= 0) S.종목으로(n) else S.copy(끝화면 = true)
}

/** ＋ — 맨 아래 세트를 그대로 베낀다. 슈퍼세트면 묶인 종목 전부에 (5-1 ③, 8-2) */
fun 운동세션.세트추가(): 운동세션 {
    val ex = 지금종목
    return copy(종목들 = 종목들.mapIndexed { j, x ->
        if (x !== ex && (ex.슈퍼 == null || x.슈퍼 != ex.슈퍼)) return@mapIndexed x
        val 끝 = x.총칸() - 1
        val 베낄 = if (끝 >= 0) 세트값(x, 끝, j == i) else 세트(x.무게, x.횟수)
        val 쉴 = if (끝 >= 0) x.세트휴식(끝) else x.휴식
        val 새 = x.총칸()
        x.copy(세트 = 새 + 1, 예정값 = x.예정값.칸바꿈(새, 베낄), 휴식들 = x.휴식들.칸바꿈(새, 쉴))
    })
}

fun 운동세션.세트삭제(k: Int): 운동세션 {
    val S = 종목바꿈(i) { e ->
        fun <T> List<T?>.뺌(): List<T?> = if (k in indices) toMutableList().also { it.removeAt(k) } else this
        e.copy(기록 = e.기록.뺌(), 휴식들 = e.휴식들.뺌(), 예정값 = e.예정값.뺌(), 세트 = max(1, e.세트 - 1))
    }
    return if (S.s > k) S.copy(s = S.s - 1) else S
}

/** 세트 값 고치기 — 한 세트면 기록을, 지금 세트면 입력값을, 아직이면 그 줄만 */
fun 운동세션.값고치기(k: Int, 새무게: Double? = null, 새횟수: Int? = null): 운동세션 {
    val e = 지금종목
    val w = 새무게?.let { 무게반올림(max(0.0, it)) }
    val r = 새횟수?.let { max(0, it) }
    val rec = e.기록.칸(k)
    return when {
        rec != null -> 종목바꿈(i) { it.copy(기록 = it.기록.칸바꿈(k, 세트(w ?: rec.w, r ?: rec.r))) }
        k == s -> copy(무게 = w ?: 무게, 횟수 = r ?: 횟수)
        else -> {
            val 이제 = 세트값(e, k)
            종목바꿈(i) { it.copy(예정값 = it.예정값.칸바꿈(k, 세트(w ?: 이제.w, r ?: 이제.r))) }
        }
    }
}
fun 운동세션.휴식고치기(k: Int, 초: Int): 운동세션 = 종목바꿈(i) { it.copy(휴식들 = it.휴식들.칸바꿈(k, 초.coerceIn(0, 600))) }

/** 운동 추가 — 오늘만. 지금 종목 바로 뒤에 끼운다 (5-2) */
fun 운동세션.불러오기(이름: String, 기본휴식: Int): 운동세션 {
    val 새 = 세션종목(이름, 3, 0, 20.0, 10, 기본휴식, 휴식들 = List(3) { 기본휴식 }, 임시 = true)
    return copy(종목들 = 종목들.toMutableList().also { it.add(i + 1, 새) })
}

fun 운동세션.마감풀기(): 운동세션 = 종목바꿈(i) { it.copy(마감 = false) }

/** 기록 저장 — 오늘 기록을 남기고, 내일부터 다음 차례로 다시 깐다 */
fun 앱데이터.운동저장(오늘: String, 지금: Long): 앱데이터 {
    val S = 세션 ?: return this
    val 들 = S.종목들.mapNotNull { e ->
        val 찬 = e.찬것()
        if (찬.isEmpty()) null else 종목기록(e.이름, 찬, e.임시, S.묶음이름(e))
    }
    val rec = 날기록(S.루틴id, S.루틴이름, S.한세트수() >= S.목표세트(), 들, ((지금 - S.시작시각) / 1000).toInt())
    val 새 = copy(기록 = 기록 + (오늘 to rec), 세션 = null)
    val i = 루틴들.indexOfFirst { it.id == S.루틴id }
    if (루틴들.isEmpty()) return 새
    val 남김 = 새.예정.filterKeys { it < 오늘 }
    return 새.copy(예정 = 새.예정채움(날더하기(오늘, 1), ((i + 1) % 루틴들.size + 루틴들.size) % 루틴들.size, 남김))
}

// ─────────────── 루틴 편집 ───────────────

fun 앱데이터.루틴바꿈(id: String, f: (루틴) -> 루틴): 앱데이터 =
    copy(루틴들 = 루틴들.map { if (it.id == id) f(it) else it })

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
