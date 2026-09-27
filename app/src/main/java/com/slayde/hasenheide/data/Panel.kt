package com.slayde.hasenheide.data

import java.time.LocalDate
import kotlin.math.ceil
import kotlin.math.max

/**
 * 캘린더 날짜 판에 보이는 값 (09-27 시안 https://claude.ai/artifact/VSVZ8T9ucZjEvK1JCwiNne)
 *
 * 운동 후(기록 있는 날): 기록 갱신 · 운동량 · 향상도(지난 운동 대비 · 한 달 대비) · 1RM 목표
 * 운동 전(예정인 날):   운동량 · 오늘 목표 + 최근 한 달 추세 · 종목 목록
 */

// ─────────────── 기록 갱신 ───────────────

/** 종류 = "1RM" 또는 "볼륨". 값 = 이 날 값, 전최고 = 이 날 앞의 모든 기록 중 최고 */
data class 갱신(val 이름: String, val 종류: String, val 값: Double, val 전최고: Double) {
    val 차: Double get() = 값 - 전최고
}

/**
 * 이 날 기록에서 지난 기록 전부보다 좋아진 것 (09-27).
 * 처음 해 본 종목은 견줄 것이 없으니 넣지 않는다. 1RM 먼저, 볼륨 다음
 */
fun 앱데이터.기록갱신(k: String): List<갱신> {
    val rec = 기록[k] ?: return emptyList()
    val 앞 = 기록.filterKeys { it < k }.values
    val 결과 = mutableListOf<갱신>()
    val 볼륨들 = mutableListOf<갱신>()
    rec.종목들.map { it.이름 }.distinct().forEach { 이름 ->
        val 오늘 = rec.종목들.filter { it.이름 == 이름 }.flatMap { it.세트들 }
        if (오늘.isEmpty()) return@forEach
        val 전 = 앞.map { r -> r.종목들.filter { it.이름 == 이름 }.flatMap { it.세트들 } }.filter { it.isNotEmpty() }
        if (전.isEmpty()) return@forEach
        val rm = 오늘.maxOf { 일RM(it.w, it.r) }; val 전rm = 전.maxOf { l -> l.maxOf { 일RM(it.w, it.r) } }
        if (rm > 전rm + 0.05) 결과 += 갱신(이름, "1RM", rm, 전rm)
        val 볼 = 볼륨(오늘); val 전볼 = 전.maxOf { 볼륨(it) }
        if (볼 > 전볼 + 0.05) 볼륨들 += 갱신(이름, "볼륨", 볼, 전볼)
    }
    return 결과 + 볼륨들
}

// ─────────────── 운동량 · 시각 ───────────────

data class 운동량값(val 종목: Int, val 세트: Int, val 볼륨: Double)

fun 운동량(rec: 날기록): 운동량값 {
    val 세트들 = rec.종목들.flatMap { it.세트들 }
    return 운동량값(rec.종목들.map { it.이름 }.distinct().size, 세트들.size, 볼륨(세트들))
}

/** "오후 18:05 ~ 19:03" — 오전/오후는 앞에 한 번, 숫자는 24시간 (09-27 홍겸 님). 모르면 null */
fun 시각글(시작: Long, 끝: Long): String? {
    if (시작 <= 0 || 끝 <= 0) return null
    val z = java.time.ZoneId.systemDefault()
    val a = java.time.Instant.ofEpochMilli(시작).atZone(z).toLocalTime()
    val b = java.time.Instant.ofEpochMilli(끝).atZone(z).toLocalTime()
    fun hm(t: java.time.LocalTime) = "%02d:%02d".format(t.hour, t.minute)
    return "${if (a.hour < 12) "오전" else "오후"} ${hm(a)} ~ ${hm(b)}"
}

// ─────────────── 향상도 (루틴 볼륨) ───────────────

/** 지난 운동 대비 · 한 달 대비 — 같은 루틴의 기록과. 같은 세트 수까지 잘라 견준다 (왜곡 방지 ①) */
data class 루틴대비(val 지난: 비교?, val 한달: 비교?, val 한달날: String?)

fun 앱데이터.루틴두대비(k: String): 루틴대비 {
    val rec = 기록[k] ?: return 루틴대비(null, null, null)
    val 지금 = 정식세트(rec)
    if (지금.isEmpty()) return 루틴대비(null, null, null)
    val 같은 = 기록.filter { it.key < k && it.value.루틴id == rec.루틴id && 정식세트(it.value).isNotEmpty() }.toSortedMap()
    val 지난 = 같은.keys.lastOrNull()?.let { 대비(지금, 정식세트(같은[it]!!), it)?.볼륨 }
    val 한달전 = 개월전(날짜만(k), 1)
    val 한달k = 같은.keys.firstOrNull { 날짜만(it) >= 한달전 }
    val 한달 = 한달k?.let { 대비(지금, 정식세트(같은[it]!!), it)?.볼륨 }
    return 루틴대비(지난, 한달, 한달k)
}

/** 한 달 동안 같은 루틴의 기록 (이 날 포함) — 향상도 판 */
fun 앱데이터.루틴한달(rid: String, 끝날: String, 포함: Boolean): List<Pair<String, Double>> {
    val 한달전 = 개월전(끝날, 1)
    return 기록.filter { it.value.루틴id == rid && 날짜만(it.key) >= 한달전 && (if (포함) 날짜만(it.key) <= 끝날 else 날짜만(it.key) < 끝날) }
        .toSortedMap().map { (k, r) -> k to 볼륨(정식세트(r)) }.filter { it.second > 0 }
}

// ─────────────── 1RM 목표 ───────────────

/**
 * 1RM 목표 (09-27) — 지금 = 이 날까지의 최고 1RM.
 * 속도 = 최근 8주 동안 날마다의 최고 1RM 에 맞춘 직선의 기울기(1주당 kg). 두 날 이상 · 7일 이상 떨어져야 잰다
 * 남은주 = (목표 − 지금) / 속도, 올림. 이미 넘었으면 0, 속도가 없거나 0 이하면 null
 */
data class 목표현황(val 이름: String, val 목표: Double, val 지금: Double, val 주속도: Double?, val 남은주: Int?)

fun 앱데이터.목표현황(이름: String, 날: String): 목표현황? {
    val 목표 = 종목표.firstOrNull { it.이름 == 이름 }?.목표1RM ?: return null
    val 날별 = sortedMapOf<String, Double>()
    기록.forEach { (k, r) ->
        val d = 날짜만(k); if (d > 날) return@forEach
        val s = r.종목들.filter { it.이름 == 이름 }.flatMap { it.세트들 }
        if (s.isNotEmpty()) 날별[d] = max(날별[d] ?: 0.0, s.maxOf { 일RM(it.w, it.r) })
    }
    if (날별.isEmpty()) return 목표현황(이름, 목표, 0.0, null, null)
    val 지금 = 날별.values.max()
    val 시작 = 날더하기(날, -56)
    val 점 = 날별.filterKeys { it >= 시작 }.map { (d, v) -> (LocalDate.parse(d).toEpochDay() - LocalDate.parse(시작).toEpochDay()).toDouble() to v }
    var 속도: Double? = null
    if (점.size >= 2 && 점.last().first - 점.first().first >= 7) {
        val mx = 점.map { it.first }.average(); val my = 점.map { it.second }.average()
        val 위 = 점.sumOf { (x, y) -> (x - mx) * (y - my) }; val 아래 = 점.sumOf { (x, _) -> (x - mx) * (x - mx) }
        if (아래 > 0) 속도 = 위 / 아래 * 7
    }
    val 남은 = when {
        지금 >= 목표 -> 0
        속도 != null && 속도 > 0.05 -> ceil((목표 - 지금) / 속도).toInt()
        else -> null
    }
    return 목표현황(이름, 목표, 지금, 속도, 남은)
}

/** "2월 초" — 날 + n주 */
fun 도달달(날: String, 주: Int): String {
    val d = LocalDate.parse(날).plusWeeks(주.toLong())
    val 때 = when { d.dayOfMonth <= 10 -> "초"; d.dayOfMonth <= 20 -> "중순"; else -> "말" }
    return "${d.monthValue}월 $때"
}

/** 이 날 기록의 종목 중 1RM 목표가 있는 것들 */
fun 앱데이터.목표종목들(k: String): List<목표현황> {
    val rec = 기록[k] ?: return emptyList()
    return rec.종목들.map { it.이름 }.distinct().mapNotNull { 목표현황(it, 날짜만(k)) }
}

// ─────────────── 운동 전 — 오늘 목표 · 그 날만 조절 ───────────────

/** 그 날만 조절한 루틴 (09-27) — 무게 × 볼륨% + 무게, 세트 수 + 세트. 루틴 원본은 바꾸지 않는다 */
fun 루틴.조절적용(j: 오늘조절?, 폭: Double = 0.5): 루틴 {
    if (j == null || j.그대로 || 휴식일) return this
    fun 맞춤(w: Double): Double {
        val x = max(0.0, w * j.볼륨 / 100.0 + j.무게)
        val p = if (폭 > 0) 폭 else 0.5
        return 무게반올림(Math.round(x / p) * p)
    }
    return copy(종목 = 종목.map { e ->
        val n = max(1, e.세트 + j.세트)
        val 목 = (0 until n).map { k -> e.목표(k.coerceAtMost(e.세트 - 1)).let { 세트(맞춤(it.w), it.r) } }
        val 휴 = (0 until n).map { k -> e.휴식(k.coerceAtMost(e.세트 - 1)) }
        e.copy(세트 = n, 무게 = 목[0].w, 횟수 = 목[0].r, 휴식 = 휴[0], 세트값 = 목, 휴식값 = 휴)
    })
}

fun 루틴.계획볼륨(): Double = if (휴식일) 0.0 else 종목.sumOf { it.볼륨() }

/** 그 날 할 루틴 — 조절이 있으면 적용해서 */
fun 앱데이터.그날루틴(k: String): 루틴? = 예정루틴(k)?.조절적용(조절[k], 설정.무게폭)

/** 운동 시작 — 그 날 조절이 있으면 적용하고 '조절됨' 표시 */
fun 앱데이터.조절해시작(r: 루틴, k: String, 지금: Long): 운동세션? {
    val j = 조절[k]
    val 적용 = r.조절적용(j, 설정.무게폭)
    return 운동시작(적용, 지금)?.let { if (j != null && !j.그대로) it.copy(조절됨 = true) else it }
}

/** 최근 한 달 추세 — 같은 루틴 기록이 둘 이상일 때: 마지막 − 처음 */
fun 앱데이터.한달추세(rid: String, 날: String): Double? {
    val l = 루틴한달(rid, 날, 포함 = false)
    return if (l.size < 2) null else l.last().second - l.first().second
}

// ─────────────── 종목 하나만 (09-27 '변경' · '한 번 더') ───────────────

/** 종목 하나로 된 루틴 — 지난 기록이 있으면 그 세트 그대로, 없으면 20kg × 10회 × 3세트 */
fun 앱데이터.한종목루틴(이름: String): 루틴 {
    val 지난 = 기록.keys.sorted().lastOrNull { k -> 기록[k]!!.종목들.any { it.이름 == 이름 && it.세트들.isNotEmpty() } }
        ?.let { k -> 기록[k]!!.종목들.first { it.이름 == 이름 && it.세트들.isNotEmpty() }.세트들 }
    val 세트들 = 지난 ?: List(3) { 세트(20.0, 10) }
    val 휴 = 설정.기본휴식
    return 루틴("한종목:$이름", 이름, 종목 = listOf(
        루틴종목(이름, 세트들.size, 세트들[0].w, 세트들[0].r, 휴, 세트값 = 세트들, 휴식값 = List(세트들.size) { 휴 }),
    ))
}

/** "+10%" 같은 글 */
fun 퍼센트글(지금: Double, 과거: Double): String? = 퍼센트(지금, 과거)?.let { if (it >= 0) "+$it%" else "$it%" }

