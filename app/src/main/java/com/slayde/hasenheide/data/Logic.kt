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

fun 날(k: String): LocalDate = LocalDate.parse(날짜만(k))

/**
 * 기록 열쇠 — 보통은 날짜("2026-09-26"). 같은 날 '한 번 더' 운동하면 "2026-09-26~2", "~3" … (09-26 메모)
 * 글자 순서로 견줘도 그 날 첫 기록 뒤 · 다음 날 앞에 온다
 */
fun 날짜만(k: String): String = k.substringBefore('~')
/** 그 날의 새 기록 열쇠 — 비어 있으면 날짜, 있으면 ~2, ~3 … */
fun 앱데이터.새기록열쇠(날: String): String {
    if (!기록.containsKey(날)) return 날
    var n = 2
    while (기록.containsKey("$날~$n")) n++
    return "$날~$n"
}
/** 그 날의 '한 번 더' 기록들 (첫 기록은 빼고) */
fun 앱데이터.한번더기록(날: String): List<Pair<String, 날기록>> =
    기록.filterKeys { it.startsWith("$날~") }.toList().sortedBy { it.first.substringAfter('~').toIntOrNull() ?: 0 }
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

/**
 * 볼륨(운동량) — **워밍업(종류 1)과 측정 세트(종류 2)는 빼고** 센다 (09-29, 20 문서 B-5).
 * 볼륨을 세는 곳이 앱 곳곳에 있지만 전부 이 함수를 불러 쓰므로 여기 한 곳만 고치면 된다.
 */
fun 볼륨(세트들: List<세트>): Double = 세트들.filter { it.종류 == 세트종류.본운동 }.sumOf { it.w * it.r }

/** 1RM 기록에 쓰는 세트 — 워밍업만 뺀다 (측정 세트는 들어간다) */
fun 기록세트(세트들: List<세트>): List<세트> = 세트들.filter { it.종류 != 세트종류.워밍업 }
/**
 * 에플리(Epley) 식 — 무게 × (1 + 횟수/30). 세계에서 가장 널리 쓰는 식 (09-27 홍겸 님)
 * 1회는 그 무게 자체다 (09-27: 100kg 1회가 103.3kg 으로 나왔다). 횟수에 제한은 두지 않는다
 */
fun 일RM(w: Double, r: Int): Double = when { r <= 0 -> 0.0; r == 1 -> w; else -> w * (1 + r / 30.0) }

// ─────────────── 루틴 · 예상 시간 (4-3) ───────────────

const val 세트수행초 = 40
/** 한 번(1회)에 드는 시간(초) — 10회 = 40초 (10-01 홍겸 님). 예상 시간은 세트마다 횟수 × 이 값 */
const val 회당초 = 4
const val 종목전환초 = 60

fun 예상초(r: 루틴?): Int {
    if (r == null || r.휴식일) return 0
    // 10-01: 세트 시간을 횟수에 비례시킨다 (전에는 횟수와 상관없이 세트당 40초)
    val 본 = r.종목.sumOf { e -> (0 until e.세트).sumOf { k -> e.목표(k).r * 회당초 } + (0 until max(0, e.세트 - 1)).sumOf { k -> e.휴식(k) } }
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
    return 고정덮기(m, 시작날)
}

/** 그 날만 손으로 바꾼 것을 다시 덮는다 (10-01) — "" 은 그 날 비움 */
private fun 앱데이터.고정덮기(m: MutableMap<String, String>, 시작날: String): Map<String, String> {
    예정고정.forEach { (k, v) -> if (k >= 시작날) { if (v.isEmpty()) m.remove(k) else if (루틴(v) != null) m[k] = v } }
    return m
}

/**
 * 예정이 있었는데 기록 없이 지나간 날을 '미실시' 로 남긴다 (09-27 캘린더).
 * 지난 예정은 곧 지워지므로(예정맞추기 · 예정초기화) 지우기 전에 담아 둔다. 휴식일은 뺀다
 */
fun 앱데이터.놓친날담기(오늘: String): 앱데이터 {
    val 놓친 = 예정.filter { it.key < 오늘 && !기록.containsKey(it.key) && !미실시.containsKey(it.key) }
        .mapNotNull { (k, rid) -> 루틴(rid)?.takeIf { !it.휴식일 }?.let { k to it.이름 } }
    // 10-02 (스키마 14): 휴식일 예정이던 지난 날에 기록이 없으면 '쉰날' 로 남긴다 (업적 1-38 · 스탯명세 7-2)
    val 쉼 = 예정.filter { it.key < 오늘 && !기록.containsKey(it.key) && it.key !in 쉰날 && 루틴(it.value)?.휴식일 == true }.keys
    if (쉼.isNotEmpty()) return copy(쉰날 = 쉰날 + 쉼, 미실시 = 미실시 + 놓친)
    return if (놓친.isEmpty()) this else copy(미실시 = 미실시 + 놓친)
}

/** 마지막으로 한 운동의 '다음'부터 다시 깐다 (자동생성 루틴으로 한 마지막 운동 기준) */
fun 앱데이터.예정초기화(오늘: String): 앱데이터 = 놓친날담기(오늘).예정초기화0(오늘)

private fun 앱데이터.예정초기화0(오늘: String): 앱데이터 {
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
fun 앱데이터.예정맞추기(오늘: String): 앱데이터 = 놓친날담기(오늘).예정맞추기0(오늘)

private fun 앱데이터.예정맞추기0(오늘: String): 앱데이터 {
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
    // 10-01 감시관: 수동 루틴도 그 날을 고정으로 남겨야 다음 저장 때 덮이지 않는다
    if (idx < 0) return if (루틴(rid) != null) copy(예정 = 예정 + (D to rid), 예정고정 = 예정고정 + (D to rid)) else this
    val 남김 = 예정.filterKeys { it < D }
    // '이 날부터 순서대로' 는 그 날의 손댄 표시를 지운다
    return copy(예정고정 = 예정고정 - D).let { it.copy(예정 = it.예정채움(D, idx, 남김)) }
}

/**
 * 달력에서 꾹 눌러 옮기기 (2-3, 09-26 메모) — 원 날의 루틴을 D 로.
 * 자동생성 루틴: D 부터 순서를 다시 깐다(꽂기). 원 날이 D 보다 앞이면 원 날은 비운다.
 * 꺼진 루틴: 원 날에서 빼고 D 에만 넣는다. 기록이 있는 날 · 지난 날은 건드리지 않는다.
 */
fun 앱데이터.예정옮기기(원: String, D: String, 오늘: String): 앱데이터 {
    if (원 == D || 원 < 오늘 || D < 오늘 || 기록.containsKey(원) || 기록.containsKey(D)) return this
    val rid = 예정[원] ?: return this
    val 자동 = 루틴(rid)?.자동생성 == true
    val 풀린 = copy(예정고정 = 예정고정 - 원 - D)
    if (!자동) return 풀린.copy(예정 = 예정 - 원 + (D to rid), 예정고정 = 풀린.예정고정 + (D to rid) + (원 to ""))
    val 옮김 = 풀린.꽂기(rid, D, 오늘)
    return if (원 < D) 옮김.copy(예정 = 옮김.예정 - 원, 예정고정 = 옮김.예정고정 + (원 to "")) else 옮김
}

/** 오늘 쉬기 — push: 오늘 루틴을 내일로 / skip: 오늘 루틴을 건너뛰기 (2-5) */
fun 앱데이터.오늘휴식(미루기: Boolean, 오늘: String): 앱데이터 = 날휴식(미루기, 오늘, 오늘)

/**
 * 그 날 쉬기 — 오늘만이 아니라 앞으로의 아무 날에나 (10-01 홍겸 님: 정해진 일정을 고칠 수 없었다)
 *  · 미루기: 그 날 루틴을 다음 날로, 뒤는 하루씩 밀린다 · 건너뛰기: 그 날 루틴을 빼고 다음 차례부터
 *  · 자동생성이 꺼진 루틴(그 날에만 넣은 것)이면 그 날만 비운다
 */
fun 앱데이터.날휴식(미루기: Boolean, D: String, 오늘: String): 앱데이터 {
    if (D < 오늘 || 기록.containsKey(D)) return this
    val idx = 순번.indexOfFirst { it.id == 예정[D] }
    if (idx < 0) return 예정지우기(D, 오늘)
    val 남김 = 예정.filterKeys { it < D }
    val 시작 = if (미루기) idx else (idx + 1) % 순번.size
    return copy(예정고정 = 예정고정 - D).let { it.copy(예정 = it.예정채움(날더하기(D, 1), 시작, 남김)) }
}

/** 그 날 예정만 지운다 — 앞뒤 순서는 그대로 (10-01) */
fun 앱데이터.예정지우기(D: String, 오늘: String): 앱데이터 =
    if (D < 오늘 || 기록.containsKey(D)) this else copy(예정 = 예정 - D, 예정고정 = 예정고정 + (D to ""))

/** 그 날만 다른 루틴으로 — 앞뒤 순서는 그대로 (10-01. '이 날부터 다시 깔기' 는 꽂기) */
fun 앱데이터.그날만바꾸기(rid: String, D: String, 오늘: String): 앱데이터 =
    if (D < 오늘 || 기록.containsKey(D) || 루틴(rid) == null) this else copy(예정 = 예정 + (D to rid), 예정고정 = 예정고정 + (D to rid))
/** 휴식 물음에 보여줄 '앞으로 사흘' — 오늘이 아닌 날에도 (10-01: 두 번째 인자는 쉬는 그 날) */
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

/**
 * 루틴 차원 — 불러온(임시) 종목은 뺀다.
 * 10-02: **워밍업 세트도 뺀다** — 측정일 워밍업이 생겨, '같은 세트 수까지 잘라 견주기'(8-3 ①)에서
 * 워밍업이 세트 수를 차지해 본 세트가 잘려 나가던 것을 막는다 (볼륨 값 자체는 볼륨() 이 이미 뺀다)
 */
fun 정식세트(rec: 날기록): List<세트> = rec.종목들.filter { !it.임시 }.flatMap { it.세트들 }.filter { it.종류 != 세트종류.워밍업 }

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
        // 1RM · 단일세트 최고는 워밍업을 뺀 것으로 센다 (09-29). 볼륨은 볼륨() 이 알아서 뺀다
        val 전부 = 기록[k]!!.종목들.filter { it.이름 == 이름 }.flatMap { it.세트들 }
        val 세트들 = 기록세트(전부)
        if (세트들.isEmpty()) null else 세션지표(
            날짜만(k), 세트들.maxOf { 일RM(it.w, it.r) }, 세트들.maxBy { it.w * it.r }, 볼륨(전부),
        )
    }
    val 최근 = 들.lastOrNull() ?: return null
    val 기준일 = 기준날(오늘)
    val 과거 = 들.lastOrNull { it.날 <= 기준일 && it.날 != 최근.날 }
    return 지표비교(최근, 과거)
}

/**
 * 향상도 — **1주 · 최고** 나란히 (09-26 홍겸 님 · 시안 ①)
 *  · 1RM — 지금 세트의 최고 1RM ↔ (1주) 지난 7일 중 가장 좋은 날 / (최고) 지난 기록 전부 중 최고
 *  · 볼륨 — 지금 볼륨 ↔ 날마다 '같은 세트 수까지' 자른 볼륨 중 가장 큰 것 (왜곡 방지 ①, 하던 중이어도 공정하게)
 *  · 1주 = 기준날 7일 전 ~ 전날 (기준날 당일은 뺀다)
 *  · [이전] 보다 앞선 기록만 본다 (글자 비교). 운동 중이면 "~" 를 넣어 모든 기록을 본다
 *  · 10-02 감사: **같은 [묶음](슈퍼세트) 상태 기록끼리만** 견준다 (02 명세 8-2 · 04 ⑦ — 단독 기록과 섞지 않는다).
 *    워밍업 세트는 1RM 에서도, 세트 수를 맞춰 자를 때도 뺀다 (2-23 · 기록세트)
 */
data class 두대비(val 지금: Double, val 주: 비교?, val 최고: 비교?)
data class 종목향상(val rm: 두대비, val 볼륨: 두대비)

private fun 견줌(a: Double, b: Double?): 비교? = if (b == null || b <= 0.0) null else 비교(a, b, 퍼센트(a, b), a - b)
private fun 한주안(k: String, 기준날: String): Boolean { val d = 날짜만(k); return d >= 날더하기(기준날, -7) && d < 기준날 }

fun 앱데이터.종목향상(이름: String, 지금: List<세트>, 기준날: String, 이전: String = "~", 묶음: String? = null): 종목향상? {
    val 본 = 기록세트(지금)
    if (본.isEmpty()) return null
    val 과거 = 기록.filterKeys { it < 이전 }
        .map { (k, r) -> k to 기록세트(r.종목들.filter { it.이름 == 이름 && it.묶음 == 묶음 }.flatMap { it.세트들 }) }.filter { it.second.isNotEmpty() }
    val 주 = 과거.filter { 한주안(it.first, 기준날) }
    val n = 본.size
    fun rm(l: List<세트>) = l.maxOf { 일RM(it.w, it.r) }
    val rm지금 = rm(본); val 볼지금 = 볼륨(본)
    return 종목향상(
        두대비(rm지금, 견줌(rm지금, 주.maxOfOrNull { rm(it.second) }), 견줌(rm지금, 과거.maxOfOrNull { rm(it.second) })),
        두대비(볼지금, 견줌(볼지금, 주.maxOfOrNull { 볼륨(it.second.take(n)) }), 견줌(볼지금, 과거.maxOfOrNull { 볼륨(it.second.take(n)) })),
    )
}

/** 루틴 차원 — 전체 볼륨만 (불러온 종목 뺌). 같은 루틴의 지난 기록과 */
fun 앱데이터.루틴향상(rid: String, 지금: List<세트>, 기준날: String, 이전: String = "~"): 두대비? {
    if (지금.isEmpty()) return null
    val n = 지금.size
    val 과거 = 기록.filter { it.key < 이전 && it.value.루틴id == rid }.map { it.key to 정식세트(it.value) }.filter { it.second.isNotEmpty() }
    val a = 볼륨(지금)
    return 두대비(a, 견줌(a, 과거.filter { 한주안(it.first, 기준날) }.maxOfOrNull { 볼륨(it.second.take(n)) }),
        견줌(a, 과거.maxOfOrNull { 볼륨(it.second.take(n)) }))
}

/** 루틴의 가장 최근 기록을 그 앞의 기록과 견준다 — 달력 예정 판 · 루틴 탭 */
fun 앱데이터.루틴최근향상(rid: String): 두대비? {
    val k = 기록.filter { it.value.루틴id == rid }.keys.maxOrNull() ?: return null
    return 루틴향상(rid, 정식세트(기록[k]!!), 날짜만(k), k)
}

/** 차이를 kg 로 — 100 이상이면 콤마 정수, 아니면 소수 한 자리까지 (0 은 떼고) */
fun kg글(x: Double): String {
    val a = kotlin.math.abs(x)
    return if (a >= 100 || a == Math.rint(a)) 콤마(a) else "%.1f".format(a)
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
// 10-02 감시관: 측정일 워밍업 세트는 달성(목표 세트 · 한 세트)에서 뺀다 — 워밍업을 건너뛰어도 '미달성' 이 되지 않게
fun 세션종목.웜칸수(): Int = 예정값.count { it?.종류 == 세트종류.워밍업 }
fun 운동세션.목표세트(): Int = 정식().sumOf { (it.계획세트 - it.웜칸수()).coerceAtLeast(0) }
fun 운동세션.한세트수(): Int = 정식().sumOf { e -> e.찬것().count { it.종류 != 세트종류.워밍업 } }
/** 체크한 세트 전부(워밍업 포함) — 기록을 남길지 · 결과 화면 '세트' 수 · 화면 맞추기 열쇠. 워밍업을 빼는 것은 달성 계산뿐이다 (10-02 감시관) */
fun 운동세션.찬세트수(): Int = 정식().sumOf { it.찬것().size }
fun 운동세션.오늘볼륨(): Double = 정식().sumOf { 볼륨(it.찬것()) }
fun 운동세션.목표볼륨(): Double = 정식().sumOf { it.목표볼륨() }
/** 한 종목의 목표 볼륨 — 루틴에 정한 세트까지, 세트마다의 목표로. 워밍업 세트는 넣지 않는다 (10-02 · 볼륨() 과 같은 규칙) */
fun 세션종목.목표볼륨(): Double {
    val n = if (계획세트 > 0) 계획세트 else 세트
    return (0 until n).sumOf { k -> 볼륨(listOf(예정값.칸(k) ?: 세트(무게, 횟수))) }
}
fun 운동세션.루틴달성도(): Int = 목표세트().let { if (it == 0) 0 else (한세트수() * 100.0 / it).roundToInt() }
/** 종목 달성도 — 루틴달성도와 같은 규칙으로 측정일 워밍업은 뺀다 (10-02 감시관: 전체는 '달성' 인데 종목 게이지가 100% 가 안 됐다) */
fun 세션종목.달성도(): Int = (계획세트 - 웜칸수()).let { n -> if (n <= 0) 0 else (찬것().count { it.종류 != 세트종류.워밍업 } * 100.0 / n).roundToInt() }
/** 유효세트 — 계획 세트까지만 (추가한 세트는 향상도에서 뺀다). 워밍업은 뺀다 (10-02 · 정식세트와 같은 규칙) */
fun 운동세션.유효세트(): List<세트> = 정식().flatMap { it.찬것().take(it.계획세트) }.filter { it.종류 != 세트종류.워밍업 }
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
/** 이 종목의 1RM — 지난 기록 중 가장 높은 값 (없으면 루틴 목표로). 워밍업 세트는 뺀다 (10-02 · 2-23) */
fun 앱데이터.종목1RM(이름: String, 지금: 루틴종목? = null): Double {
    val 과거 = 기록세트(기록.values.flatMap { r -> r.종목들.filter { it.이름 == 이름 }.flatMap { it.세트들 } }).maxOfOrNull { 일RM(it.w, it.r) } ?: 0.0
    val 계획 = 지금?.let { e -> 기록세트((0 until e.세트).map { e.목표(it) }).maxOfOrNull { 일RM(it.w, it.r) } } ?: 0.0
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
            휴식들 = List(it.세트) { k -> it.휴식(k) }, 슈퍼 = it.슈퍼, 플랜id = it.플랜id, 종id = it.종id)
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
    // 기록할 값 = 화면에 보이는 값 (세트값과 같은 차례: 따로 고친 값 → 입력 중인 값)
    val v = 지금종목.예정값.칸(k) ?: 세트(무게, 횟수)
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
                S = S.다음칸으로(다음빈칸(S.지금종목, k))
                return S.copy(휴식 = 휴식중(바퀴, 지금 + 쉴 * 1000L, false, 다음, 다음칸, 총초 = 쉴, 종목 = 끝))
            }
        }
    }
    S = S.다음칸으로(다음빈칸(S.지금종목, k))
    // 모든 종목을 다 했으면 쉬지 않고 마무리
    if (S.종목들.all { it.마감 || !it.덜한가() }) return S.끝냄(지금)
    return S.휴식시작(k, 지금)
}

/**
 * 지금 세트 자리를 옮기며 입력 값도 그 세트의 값으로 (09-27 메모)
 * 전에는 자리만 옮기고 입력 값(무게 · 횟수)은 방금 체크한 세트 것이 남아,
 * 쉬는 동안 다음 세트를 바로 체크하면 1세트에 바꾼 12회가 2~5세트에도 들어갔다
 */
private fun 운동세션.다음칸으로(ns: Int): 운동세션 {
    val e = 지금종목
    val v = e.기록.칸(ns) ?: e.예정값.칸(ns) ?: 세트(e.무게, e.횟수)
    return copy(s = ns, 무게 = v.w, 횟수 = v.r)
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
    // 10-02: 세트 종류(워밍업 등)는 그대로 둔다 — 값만 고친다 (전에는 고치면 본운동으로 바뀌었다)
    return when {
        rec != null -> 종목바꿈(j) { it.copy(기록 = it.기록.칸바꿈(k, rec.copy(w = w ?: rec.w, r = r ?: rec.r))) }
        else -> {
            val 이제 = 세트값(e, k)
            val 새 = 이제.copy(w = w ?: 이제.w, r = r ?: 이제.r)
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
        if (찬.isEmpty()) null else 종목기록(e.이름, 찬, e.임시, S.묶음이름(e), 플랜id = e.플랜id, 종id = e.종id)
    }
    val 끝 = S.끝시각 ?: 지금
    val 걸린 = S.흐른초(지금)
    val rec = 날기록(S.루틴id, S.루틴이름, S.한세트수() >= S.목표세트(), 들, 걸린.toInt(), 시작시각 = 끝 - 걸린 * 1000, 끝시각 = 끝)
    // 루틴 반영(5-5) 뒤에, 설정이 켜져 있으면 볼륨을 한 번 더 올린다 (09-24)
    // 그 날만 조절해서 한 운동은 루틴에 되돌려 적지 않는다 (09-27: 루틴은 그대로)
    val 올릴까 = !S.조절됨 && 설정.볼륨켬 && (설정.볼륨언제 == "항상" || rec.달성)
    // 오늘 이미 기록이 있으면 '한 번 더' 기록으로 따로 남긴다 (09-26 메모)
    val 열쇠 = 새기록열쇠(오늘)
    // 10-02: 근육 피로를 쌓는다 (07 근육지도 4·5절) — 워밍업 세트는 빼고, 운동을 끝낸 시각에서 회복이 시작된다
    val 근 = 피로쌓기(S.근육입력들(), 끝)
    val 새 = copy(
        피로 = 근.피로, 최대볼륨 = 근.최대볼륨,
        기록 = 기록 + (열쇠 to rec), 세션 = null, 조절 = 조절 - 오늘, 결과 = null,
        루틴들 = 루틴들.map { if (it.id == S.루틴id && !S.조절됨) it.오늘반영(S).let { r -> if (올릴까) r.볼륨올리기(설정) else r } else it },
    )
    val 줄 = 순번
    val i = 줄.indexOfFirst { it.id == S.루틴id }
    // 자동생성이 꺼진 루틴으로 운동했으면 순서는 건드리지 않는다
    if (줄.isEmpty() || i < 0) return 새
    val 남김 = 새.예정.filterKeys { it < 오늘 }
    return 새.copy(예정 = 새.예정채움(날더하기(오늘, 1), (i + 1) % 줄.size, 남김))
}

/**
 * 운동 저장 — 루틴 반영 · 다음 차례 · **플랜 회차** 까지 (10-01).
 * 화면은 이 함수를 부른다. [운동저장] 은 플랜을 모르는 옛 함수로 남겨 둔다 (시험이 그대로 돌게)
 */
fun 앱데이터.운동저장하기(오늘: String, 지금: Long): 앱데이터 {
    val S = 세션 ?: return this
    // 10-05 검수: 체크한 세트가 하나도 없으면 저장하지 않고 버린다 — 탭 · 뒤로가기로 나가도 빈 기록이 남지 않게 (22 버그 #4 · 시안 운동저장하기)
    if (S.찬세트수() == 0) return copy(세션 = null)
    val 들 = S.종목들.mapNotNull { e -> e.찬것().takeIf { it.isNotEmpty() }?.let { 종목기록(e.이름, it, e.임시, 플랜id = e.플랜id, 종id = e.종id) } }
    return 플랜반영(운동저장(오늘, 지금), 들, 오늘, S.조절됨)
}

/**
 * 오래 손대지 않은 운동은 저절로 끝낸다 (09-25 메모: 어제 시작한 운동이 다음 날까지 돌고 있었다)
 *  · 마지막으로 손댄 뒤 [한계] 가 지나면 끝낸다 (기본 3시간)
 *  · 체크한 세트가 있으면 **운동을 시작한 날**의 기록으로 저장한다 (루틴 반영 · 다음 차례도 저장과 같게)
 *    그 날에 이미 기록이 있으면 '한 번 더' 기록으로 남긴다 (09-26)
 *  · 체크한 세트가 없으면 그냥 버린다
 */
fun 앱데이터.오래된운동정리(지금: Long, 한계: Long = 3 * 60 * 60 * 1000L): 앱데이터 {
    val S = 세션 ?: return this
    val 마지막 = if (S.마지막 > 0) S.마지막 else S.시작시각
    if (지금 - 마지막 < 한계) return this
    if (S.찬세트수() == 0) return copy(세션 = null)   // 워밍업만 체크했어도 남긴다 (손으로 끝낼 때와 같게 · 10-02 감시관)
    val 날 = java.time.Instant.ofEpochMilli(S.시작시각).atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString()
    // 10-01: 저장하고 결과 화면을 **한 번 보여 준다** (전에는 조용히 저장만 해서 결과 화면이 안 나왔다)
    return 운동저장하기(날, S.끝시각 ?: 마지막).copy(결과 = S.끝냄(S.끝시각 ?: 마지막))   // 그 날 기록이 있으면 '한 번 더' 로 남는다
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
        if (e.플랜id != null) return@map e   // 10-01: 플랜 줄은 플랜이 올린다
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
    // 10-01: 플랜 줄은 빼고 짝짓는다 — 플랜 줄의 값은 플랜이 정한다 (운동 중 바꾼 값은 플랜 계산에 들어간다)
    val 정식 = S.종목들.filter { !it.임시 && it.플랜id == null }
    // 10-05 (스키마 15): 열쇠(종id, 없으면 이름)로 짝짓는다 — 같은 이름의 다른 종목이 섞이지 않게. 옛 줄은 둘 다 이름이라 전과 같다
    val 쓴 = mutableMapOf<String, Int>()
    return copy(종목 = 종목.map { re ->
        if (re.플랜id != null) return@map re
        val 열 = 종목열쇠(re.종id, re.이름)
        val n = 쓴.getOrDefault(열, 0); 쓴[열] = n + 1
        val e = 정식.filter { 종목열쇠(it.종id, it.이름) == 열 }.getOrNull(n) ?: return@map re
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
 * 10-02: 워밍업 세트는 뺀다 (2-23 · 기록세트) — 워밍업만 한 날은 점이 없다
 */
fun 앱데이터.종목추이(이름: String, 단위: 묶기, 일RM으로: Boolean, 오늘: String, 오늘세트: List<세트>): List<점> {
    val 날별 = sortedMapOf<String, List<세트>>()
    기록.forEach { (k, rec) ->
        val s = 기록세트(rec.종목들.filter { it.이름 == 이름 }.flatMap { it.세트들 })
        if (s.isNotEmpty()) 날별[k] = s
    }
    // 오늘 이미 기록이 있으면('한 번 더') 그 뒤에 붙인다 — 덮어쓰지 않게. '~~' 는 '~2' 보다 뒤로 정렬된다
    val 오늘것 = 기록세트(오늘세트)
    if (오늘것.isNotEmpty()) 날별[if (날별.containsKey(오늘)) "$오늘~~" else 오늘] = 오늘것
    fun 기간(k: String): String = when (단위) {
        묶기.일 -> 날짜만(k)      // 같은 날 '한 번 더' 는 그 날 하나로 (볼륨은 합, 1RM 은 최고)
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
    val 집은 = 종목.getOrNull(from) ?: return this
    // 플랜 종목은 슈퍼세트로 묶지 않는다 (01 ㉓-7 확정 · 10-01 감사에서 빠진 것을 찾음)
    if (집은.플랜id != null || 대상.플랜id != null) return this
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

// ═══════════════ 스키마 15 (10-05 · 앱 옮기기 1단계) — 종목 id · 종목설정 · 프로필 · 운동 중 순서 ═══════════════

// ─────────────── 종목 id · 같은 이름 (시안 v19 D ②) ───────────────

/** 줄(루틴종목 · 세션종목 · 종목기록)의 열쇠 — 종id 가 있으면 그것, 없으면 이름 (시안 `줄키`) */
fun 종목열쇠(종id: String?, 이름: String): String = 종id ?: 이름

val 루틴종목.열쇠: String get() = 종목열쇠(종id, 이름)
val 세션종목.열쇠: String get() = 종목열쇠(종id, 이름)
val 종목기록.열쇠: String get() = 종목열쇠(종id, 이름)

/**
 * 줄이 가리키는 종목 — **종id 로 먼저**, 없거나 못 찾으면 **이름으로** (같은 이름이면 먼저 만든 것 = 종목표에서 앞의 것).
 * 종id 가 null 인 옛 줄은 이름으로만 찾는다 (이름을 바꾼 옛 종목의 id(= 옛 이름)에 잘못 붙지 않게)
 */
fun 앱데이터.종목찾기(종id: String?, 이름: String): 종목? =
    (if (종id != null) 종목표.firstOrNull { it.id == 종id } else null) ?: 종목표.firstOrNull { it.이름 == 이름 }

/** 열쇠 하나로 (종목설정 열쇠 · 넣기 시트 칸) — id 로 먼저, 없으면 이름으로 (시안 `종목표찾기`) */
fun 앱데이터.종목찾기(열쇠: String): 종목? = 종목찾기(열쇠, 열쇠)

/**
 * 새 종목 id — "종" + 시각(36진수). 이미 있는 id(옛 종목은 이름이 id)와 겹치면 뒤에 -2, -3 … 을 붙인다.
 * 시안은 끝에 난수 4자를 더 붙이지만, 여기서는 겹침을 직접 살펴서 늘 다르다
 */
fun 앱데이터.새종목id(지금: Long = System.currentTimeMillis()): String {
    val 있는 = 종목표.map { it.id }.toHashSet()
    val 바탕 = "종" + 지금.toString(36)
    if (바탕 !in 있는) return 바탕
    var n = 2
    while ("$바탕-$n" in 있는) n++
    return "$바탕-$n"
}

/**
 * 종목 더하기 — **같은 이름도 더한다** (시안 v19 D ② "같은 이름도 저장한다(고유 id)").
 * id 가 이미 쓰이고 있으면(같은 이름의 기본 id 등) 새 id 를 준다. [세트] 를 주면 종목설정도 같이 넣는다
 */
fun 앱데이터.종목더하기(e: 종목, 세트: List<종목세트>? = null, 지금: Long = System.currentTimeMillis()): 앱데이터 {
    val t = if (종목표.any { it.id == e.id }) e.copy(id = 새종목id(지금)) else e
    return copy(종목표 = 종목표 + t, 종목설정 = if (세트.isNullOrEmpty()) 종목설정 else 종목설정 + (t.id to 세트))
}

/** 같은 이름 종목의 표시 번호 — 둘 이상이면 종목표 순서(= 만든 순서)로 1, 2 …, 하나뿐이거나 못 찾으면 0 = 딱지 없음 (시안 `같은이름번호`) */
fun 같은이름번호(종목표: List<종목>, 열쇠: String, 이름: String): Int {
    val l = 종목표.filter { it.이름 == 이름 }
    if (l.size < 2) return 0
    val i = l.indexOfFirst { it.id == 열쇠 }
    return if (i < 0) 0 else i + 1
}

/** 줄의 표시 번호 — 종id 가 없는 옛 줄은 이름으로 찾은 종목(먼저 만든 것)의 번호 */
fun 앱데이터.같은이름번호(종id: String?, 이름: String): Int =
    같은이름번호(종목표, 종목찾기(종id, 이름)?.id ?: 종목열쇠(종id, 이름), 이름)

// ─────────────── 종목설정 — 종목 기본 세팅 (시안 v18 C ④ `종목기본세트`) ───────────────

/** 기본 세팅 세트 줄 최대 (시안 `종목세트최대`) */
const val 종목세트최대 = 10

/** n 줄짜리 같은 세트 (시안 `세트들(n,w,r,휴)`) */
fun 같은세트들(n: Int, w: Double, r: Int, 휴: Int): List<종목세트> = List(max(0, n)) { 종목세트(w, r, 휴) }

/** 시안 v17 옛 꼴 {세트:n, w, r, 휴} → n 줄 (n 은 1~10). 빈 값은 20kg · 10회 · 기본 휴식 */
fun 옛종목설정(n: Int, w: Double?, r: Int?, 휴: Int?, 기본휴식: Int): List<종목세트> =
    같은세트들(n.coerceIn(1, 종목세트최대), w ?: 20.0, r ?: 10, 휴 ?: 기본휴식)

/**
 * 종목 기본 세팅 — 종목설정[열쇠] 가 있으면 그것(무게 0 이상 · 횟수 1 이상 · 휴식 0 이면 기본 휴식),
 * 없으면 설정의 기본 세트 수 × 20kg · 10회 · 기본 휴식. 루틴 · 운동에 종목을 넣을 때 이 목록을 그대로 쓴다 (플랜 넣기는 플랜 처방)
 */
fun 앱데이터.종목기본세트(열쇠: String): List<종목세트> {
    val b = 종목설정[열쇠]
    if (!b.isNullOrEmpty()) return b.map { 종목세트(max(0.0, it.w), max(1, it.r), if (it.휴 > 0) it.휴 else 설정.기본휴식) }
    return 같은세트들(설정.기본세트, 20.0, 10, 설정.기본휴식)
}

/** 종목 기본 세팅 → 루틴 줄 (세트값 · 휴식값을 줄마다) */
fun 앱데이터.루틴줄(열쇠: String): 루틴종목 {
    val t = 종목찾기(열쇠)
    val l = 종목기본세트(t?.id ?: 열쇠)
    return 루틴종목(t?.이름 ?: 열쇠, l.size, l[0].w, l[0].r, l[0].휴,
        세트값 = l.map { 세트(it.w, it.r) }, 휴식값 = l.map { it.휴 }, 종id = t?.id)
}

/** 종목 기본 세팅 → 운동 중 줄 (시안 `운세트로` · 운동 중 [＋] 넣기). 임시 = 오늘만 끼운 종목(루틴에 되돌려 적지 않는다) */
fun 앱데이터.세션줄(열쇠: String, 임시: Boolean = true): 세션종목 {
    val t = 종목찾기(열쇠)
    val l = 종목기본세트(t?.id ?: 열쇠)
    return 세션종목(t?.이름 ?: 열쇠, l.size, if (임시) 0 else l.size, l[0].w, l[0].r, l[0].휴,
        예정값 = l.map { 세트(it.w, it.r) }, 휴식들 = l.map { it.휴 }, 임시 = 임시, 종id = t?.id)
}

// ─────────────── 프로필 · 보고서 (시안 v10 ~ v18) ───────────────

/** 닉네임 최대 글자 (시안 maxlength 12) */
const val 닉네임최대 = 12
/** 인증샷 최대 · 그중 고정 최대 (시안 v17 ⑥) */
const val 인증최대 = 8
const val 인증고정최대 = 3
/** 업적 정렬 칩 (시안 `업적정렬`) — '직접' 은 칩이 아니라 끌어 옮겼을 때 */
val 업적정렬목록 = listOf("최신순", "오래된순", "가나다순", "직접")

/** SNS 주소 다듬기 — 앞에 아무것도 없으면 https:// 를 붙이고, http(s) 가 아닌 꼴(javascript: 등)은 "" (시안 `링크주소`) */
fun 링크주소(v0: String?): String {
    val v = (v0 ?: "").trim()
    return when {
        v.isEmpty() -> ""
        Regex("^https?://", RegexOption.IGNORE_CASE).containsMatchIn(v) -> v
        Regex("^[a-z][a-z0-9+.-]*:", RegexOption.IGNORE_CASE).containsMatchIn(v) -> ""
        else -> "https://$v"
    }
}

/** 인증샷 차례 — 고정한 것(고정한 순서)이 맨 앞, 나머지는 새것부터 (시안 `인증순`) */
fun 인증순(l: List<인증사진>): List<인증사진> =
    l.filter { it.고정 > 0 }.sortedBy { it.고정 } + l.filter { it.고정 <= 0 }.sortedByDescending { it.때 }

/** 인증샷 넣기 — 8장이 넘으면 null (화면이 "인증샷은 8장까지" 토스트) */
fun 앱데이터.인증넣기(파일: String, 때: Long): 앱데이터? =
    if (인증샷.size >= 인증최대) null else copy(인증샷 = 인증샷 + 인증사진(파일, 때))

/** 인증샷 고정 바꾸기 — 고정은 3장까지(넘으면 null → "고정은 3장까지") */
fun 앱데이터.인증고정(파일: String, 지금: Long): 앱데이터? {
    val x = 인증샷.firstOrNull { it.파일 == 파일 } ?: return this
    if (x.고정 <= 0 && 인증샷.count { it.고정 > 0 } >= 인증고정최대) return null
    return copy(인증샷 = 인증샷.map { if (it.파일 == 파일) it.copy(고정 = if (it.고정 > 0) 0L else 지금) else it })
}

/**
 * 보고서 큰 운동 표 (시안 `큰운동표`) — 키 · 짧은 글 · 같은 종목으로 보는 이름들.
 * 앞 셋(스쿼트 · 벤치 · 데드)은 늘 보이고, 4번째부터는 [설정값.큰운동추가] 로 2개까지 더한다
 */
data class 큰운동(val 키: String, val 짧은: String, val 이름들: List<String>)
val 큰운동표: List<큰운동> = listOf(
    큰운동("스쿼트", "스쿼트", listOf("백 스쿼트", "스쿼트", "바벨 스쿼트", "바벨 백 스쿼트")),
    큰운동("벤치", "벤치", listOf("벤치프레스", "벤치 프레스", "바벨 벤치프레스")),
    큰운동("데드", "데드", listOf("데드리프트", "컨벤셔널 데드리프트", "바벨 데드리프트")),
    큰운동("오버헤드 프레스", "OHP", listOf("오버헤드 프레스", "바벨 오버헤드 프레스", "밀리터리 프레스", "OHP")),
    큰운동("바벨 로우", "로우", listOf("바벨 로우", "바벨로우", "펜들레이 로우", "벤트오버 로우", "벤트오버 바벨 로우", "바벨 벤트오버 로우")),
    큰운동("스내치", "스내치", listOf("스내치", "파워 스내치", "바벨 스내치")),
    큰운동("클린 앤 저크", "C&J", listOf("클린 앤 저크", "클린앤저크", "클린 앤드 저크")),
)

/** 큰 운동 추가 다듬기 — 표 4번째부터의 키만 · 표 순서 · 2개까지 (시안 `보고설정`) */
fun 큰운동추가정리(l: List<String>): List<String> = 큰운동표.drop(3).map { it.키 }.filter { it in l }.take(2)

/** 이 업적을 프로필에 보일까 — 숨긴 것만 안 보인다 (시안 `업적보임`) */
fun 앱데이터.업적보임(번호: String): Boolean = 번호 !in 업적숨김

/** 업적 보이기 · 숨기기 바꾸기 — 얻은 업적만 (시안 "업적보임") */
fun 앱데이터.업적보임바꿈(번호: String): 앱데이터 =
    if (번호 !in 업적) this else copy(업적숨김 = if (번호 in 업적숨김) 업적숨김 - 번호 else 업적숨김 + 번호)

/**
 * 얻은 업적의 차례 (시안 `업적목록`) — 설정.업적정렬 대로. 숨긴 것도 들어 있다 (프로필 줄은 [업적보임] 으로 거른다).
 *  · 최신순(기본) — 얻은 시각이 늦은 것부터 (같으면 번호 뒤의 것부터)
 *  · 오래된순 — 그 반대 · 가나다순 — 칭호 가나다
 *  · 직접 — 업적순서 차례. 순서에 없는 새 업적은 맨 앞(새것이 더 앞)
 */
fun 앱데이터.업적차례(칭호: (String) -> String? = { 업적표.찾기(it)?.칭호 }): List<String> {
    val 키들 = 업적.keys.filter { 칭호(it) != null }
    val 새먼저 = compareByDescending<String> { 업적[it] ?: 0L }.thenByDescending { it }
    return when (설정.업적정렬) {
        "오래된순" -> 키들.sortedWith(새먼저).reversed()
        "가나다순" -> { val c = java.text.Collator.getInstance(java.util.Locale.KOREAN); 키들.sortedWith { a, b -> c.compare(칭호(a) ?: "", 칭호(b) ?: "") } }
        "직접" -> { val 순 = 업적순서.filter { it in 업적 && 칭호(it) != null }.distinct(); 키들.filter { it !in 순 }.sortedWith(새먼저) + 순 }
        else -> 키들.sortedWith(새먼저)
    }
}

/**
 * 프로필 업적 줄 끌어 옮기기 — [원] · [대상] 은 **보이는 줄**의 번호. 놓은 칸의 뒤쪽 반이면 [뒤에] (시안 끌끝 "업적").
 * 숨긴 업적은 제자리에 둔 채 보이는 것만 옮기고, 정렬을 '직접' 으로 바꾼다
 */
fun 앱데이터.업적옮기기(원: Int, 대상: Int, 뒤에: Boolean, 칭호: (String) -> String? = { 업적표.찾기(it)?.칭호 }): 앱데이터 {
    val 전 = 업적차례(칭호)
    val 목 = 전.filter { 업적보임(it) }.toMutableList()
    if (!목옮김(목, 원, 대상, 뒤에)) return this
    var j = 0
    return copy(업적순서 = 전.map { if (업적보임(it)) 목[j++] else it }, 설정 = 설정.copy(업적정렬 = "직접"))
}

/** 목록 안에서 하나 옮기기 — 대상 칸의 앞(뒤에=false) · 뒤(true)로. 바뀌었으면 true (시안 끌끝의 `옮김`) */
fun <T> 목옮김(l: MutableList<T>, 원: Int, 대상: Int, 뒤에: Boolean): Boolean {
    if (원 !in l.indices || 대상 !in l.indices) return false
    var 새 = 대상 + (if (뒤에) 1 else 0)
    if (원 < 새) 새--
    if (새 == 원) return false
    val v = l.removeAt(원)
    l.add(새, v)
    return true
}

// ─────────────── 운동 중 종목 순서 바꾸기 · 빼기 · 되돌리기 · 넣기 (시안 v21 ① · v14 끌기) ───────────────
//
// 운동 중 '번호' 는 넷이다 — 지금 종목(i · s), 쉬는 종목(휴식.종목 · 다음i), 화면에서 보는 칸(본 — 화면 상태).
// 줄이 움직이면 넷 모두 **같은 종목**을 가리키게 다시 맞춘다. 화면만의 칸 상태(접기 등)는 [운동자리.자리표] 로 옮긴다.

/**
 * 순서 함수의 결과.
 *  · [본] — 화면에서 보는 칸 번호 (화면이 들고 있는 값을 넣고, 이것으로 바꾼다)
 *  · [자리표] — 옛 번호 → 새 번호 (빠진 칸은 null). 접힘 같은 화면 상태를 옮길 때
 */
data class 운동자리(val 세션: 운동세션, val 본: Int?, val 자리표: List<Int?>)

/** 운동 중 뺀 종목 — 되돌리기에 쓴다 (시안 `U.운지움` 종류 "종목"). 화면이 5초 동안 들고 있는다 */
data class 뺀종목(
    /** 뺀 자리 */
    val i: Int,
    val e: 세션종목,
    /** 뺄 때의 지금 자리 · 입력 중이던 값 */
    val 지금i: Int,
    val 지금s: Int,
    val 무게: Double,
    val 횟수: Int,
    /** 뺄 때의 휴식 (종목 번호는 실제 번호로 맞춘 것) */
    val 휴식: 휴식중?,
    /** 이 운동의 시작 시각 — 다른 운동에 되돌리지 않게 */
    val 세션시작: Long,
)

/** 휴식의 종목 번호 — 옛 기록(-1)은 지금 종목 */
private fun 운동세션.휴식종목(h: 휴식중): Int = if (h.종목 >= 0) h.종목 else i

/** 입력 칸 값 — 그 세트에 적힌 것 · 따로 고친 것 · 종목 기본값 (자리로 와 같지만 끝화면은 건드리지 않는다) */
private fun 운동세션.자리값(ni: Int, ns: Int): 운동세션 {
    val e = 종목들.getOrNull(ni) ?: return copy(i = ni, s = ns)
    val v = e.기록.칸(ns) ?: e.예정값.칸(ns) ?: 세트(e.무게, e.횟수)
    return copy(i = ni, s = ns, 무게 = v.w, 횟수 = v.r)
}

/** 아직 남은 세트가 있는 종목 (시안 `남` — 앱은 '여기까지'(마감)한 종목도 끝난 것으로 본다) */
private fun 세션종목.남음(): Boolean = !마감 && 덜한가()

/** 번호를 새 순서로 — 휴식 · 다음i 도 함께. 휴식의 종목이 빠졌으면 휴식을 치우고, 다음i 가 빠졌으면 다음i · 다음s 를 비운다 */
private fun 운동세션.번호맞춤(새종목들: List<세션종목>, 표: List<Int?>, 새i: Int): 운동세션 {
    val h = 휴식?.let { h0 ->
        val 새h = 표.getOrNull(휴식종목(h0)) ?: return@let null
        val 다음 = h0.다음i?.let { 표.getOrNull(it) }
        h0.copy(종목 = 새h, 다음i = 다음, 다음s = if (다음 == null) null else h0.다음s)
    }
    return copy(종목들 = 새종목들, i = 새i, 휴식 = h)
}

/**
 * 운동 중 종목 빼기 — 지금 보는 칸 ✕ (시안 v21 `운종목빼기`). 묻지 않고 빼고, 화면이 [뺀종목] 을 들고 5초 [되돌리기].
 *  · 하나 남았으면 못 뺀다 (null) · 체크한 세트가 있어도 뺀다 (종목째 보관)
 *  · 지금 종목을 뺐으면 → 뒤쪽(그 자리부터)의 남은 종목 → 앞쪽 → 없으면 그 자리(끝이면 앞) 0세트
 *  · 지금보다 앞을 뺐으면 지금 번호가 하나 준다 · 쉬던 종목을 뺐으면 휴식을 치운다
 *  · 보는 칸 — 보던 종목이 남아 있으면 그 종목, 아니면 뺀 자리에 온 종목(끝이었으면 그 앞)
 */
fun 운동세션.종목빼기(j: Int, 본: Int?): Pair<운동자리, 뺀종목>? {
    if (j !in 종목들.indices || 종목들.size <= 1) return null
    val 쉼 = 휴식?.let { it.copy(종목 = 휴식종목(it)) }
    val z = 뺀종목(j, 종목들[j], i, s, 무게, 횟수, 쉼, 시작시각)
    val 새들 = 종목들.filterIndexed { k, _ -> k != j }
    val 표 = 종목들.indices.map { k -> if (k == j) null else if (k > j) k - 1 else k }
    var S = 번호맞춤(새들, 표, if (i == j) 0 else 표[i] ?: 0)
    if (i == j) {
        val 뒤 = 새들.withIndex().indexOfFirst { (k, x) -> k >= j && x.남음() }
        val 앞 = 새들.indexOfFirst { it.남음() }
        val n = if (뒤 >= 0) 뒤 else 앞
        S = if (n >= 0) S.자리값(n, 다음빈칸(새들[n], -1)) else S.자리값(min(j, 새들.size - 1), 0)
    }
    val 새본 = 본?.let { b -> 표.getOrNull(b) } ?: min(j, 새들.size - 1)
    return 운동자리(S, 새본, 표) to z
}

/**
 * 뺀 종목 되돌리기 (시안 v21 `운종목되돌리기`) — 뺀 자리(지금 줄 수보다 크면 맨 끝)에 그대로 다시 끼운다.
 *  · 뺀 종목이 지금 종목이었으면 지금 자리 · 입력 값도 그때로
 *  · 뺀 종목에서 쉬던 중이었고 그 휴식이 아직 안 끝났고 지금 쉬는 중이 아니면 휴식도 되살린다
 *  · 보는 칸 = 되돌린 종목. 빼기 → 바로 되돌리기 = 처음 상태 그대로
 *  · 다른 운동(시작 시각이 다름)이면 아무것도 하지 않는다
 */
fun 운동세션.종목되돌리기(z: 뺀종목, 본: Int?, 지금: Long): 운동자리 {
    if (z.세션시작 != 시작시각) return 운동자리(this, 본, 종목들.indices.toList())
    val at = min(max(0, z.i), 종목들.size)
    val 새들 = 종목들.toMutableList().also { it.add(at, z.e) }
    val 표 = 종목들.indices.map { k -> if (k >= at) k + 1 else k }
    var S = 번호맞춤(새들, 표, 표.getOrNull(i) ?: 0)
    if (z.지금i == z.i) S = S.copy(i = at, s = z.지금s, 무게 = z.무게, 횟수 = z.횟수)
    val h = z.휴식
    // 빼면서 비운 다음i(슈퍼세트가 돌아갈 자리 = 뺀 종목)를 같은 휴식이 그대로 이어지는 중이면 되살린다
    val 지금h = S.휴식
    if (h != null && 지금h != null && h.다음i == z.i && 지금h.다음i == null && 지금h.k == h.k && 지금h.끝시각 == h.끝시각 && 지금h.총초 == h.총초)
        S = S.copy(휴식 = 지금h.copy(다음i = at, 다음s = h.다음s))
    if (h != null && h.종목 == z.i && h.끝시각 > 지금 && 휴식 == null) {
        // 그때의 다음i 는 빼기 전 번호 — 빼기(뒤로 하나 당김) → 되돌리기(at 뒤로 하나 밂) 를 그대로 따라간다
        val 다음 = h.다음i?.let { o -> if (o == z.i) at else (if (o > z.i) o - 1 else o).let { p -> if (p >= at) p + 1 else p } }
        S = S.copy(휴식 = h.copy(종목 = at, 다음i = 다음, 다음s = if (다음 == null) null else h.다음s))
    }
    return 운동자리(S, at, 표)
}

/**
 * 운동 중 칸 줄 끌어 순서 바꾸기 (시안 v14 끌기 "운칸") — [원] 을 [대상] 칸의 앞 · 뒤([뒤에])로.
 * 지금 · 휴식 · 보는 칸은 **같은 종목**을 그대로 가리킨다. 안 바뀌었으면 그대로(자리표 = 제자리).
 * 슈퍼세트도 한 칸씩 옮긴다 (시안에는 슈퍼세트가 없다)
 */
fun 운동세션.종목옮기기(원: Int, 대상: Int, 뒤에: Boolean, 본: Int?): 운동자리 {
    val 번호들 = 종목들.indices.toMutableList()
    if (!목옮김(번호들, 원, 대상, 뒤에)) return 운동자리(this, 본, 종목들.indices.toList())
    val 표 = MutableList<Int?>(종목들.size) { null }
    번호들.forEachIndexed { 새, 옛 -> 표[옛] = 새 }
    val S = 번호맞춤(번호들.map { 종목들[it] }, 표, 표[i] ?: i)
    return 운동자리(S, 본?.let { 표.getOrNull(it) }, 표)
}

/** 운동 중 [＋] 넣기 — 맨 끝에 붙인다 (시안 "종목넣기" → r.종목.push). 다른 번호는 그대로 */
fun 운동세션.종목넣기(e: 세션종목, 본: Int?): 운동자리 =
    운동자리(copy(종목들 = 종목들 + e), 본, 종목들.indices.toList())

/**
 * 넣기 시트에서 누름 = 하나 빼기 (시안 v18 D ③ `넣은것뺌`) — [맞음] 인 줄 중 **맨 뒤**부터,
 * 세트를 하나라도 끝낸 것 · 지금 하는 것 · 쉬는 중인 것은 건너뛴다. 뺄 것이 없으면 null (화면: "이미 시작한 종목은 뺄 수 없습니다")
 */
fun 운동세션.넣은것빼기(본: Int?, 맞음: (세션종목) -> Boolean): 운동자리? {
    val k = 종목들.indices.reversed().firstOrNull { j ->
        val e = 종목들[j]
        맞음(e) && e.찬것().isEmpty() && j != i && (휴식?.let { 휴식종목(it) } != j)
    } ?: return null
    val 새들 = 종목들.filterIndexed { j, _ -> j != k }
    val 표 = 종목들.indices.map { j -> if (j == k) null else if (j > k) j - 1 else j }
    val S = 번호맞춤(새들, 표, 표[i] ?: i)
    return 운동자리(S, 본?.let { b -> 표.getOrNull(b) ?: min(k, 새들.size - 1) }, 표)
}
