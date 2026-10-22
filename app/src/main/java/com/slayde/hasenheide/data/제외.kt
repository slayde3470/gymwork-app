package com.slayde.hasenheide.data

import java.time.LocalDate

// ═════════════════ 예정 칸(슬롯) · 제외 규칙 (10-22 홍겸 님) ═════════════════
//
// 한 날에 루틴을 여러 개 걸 수 있다 (묶음마다 한 칸). [앱데이터.예정] 의 열쇠:
//   "2026-10-22"    = 묶음 0 의 칸  (전과 똑같다 — 한 묶음뿐이면 열쇠가 바뀌는 일이 없다)
//   "2026-10-22#2"  = 묶음 1 의 칸,  "#3" = 묶음 2 의 칸
// 기록 열쇠의 "~2"(한 번 더) 와 겹치지 않게 '#' 를 쓴다. 날짜만 필요하면 [날짜만] — 글자 순서로 견주면
// 칸 열쇠는 그 날 날짜 뒤 · 다음 날 앞에 온다 (그래서 `key < 오늘` · `key >= 오늘` 은 그대로 맞다. 단 `key <= 어제` 는 안 맞는다 → [날짜만] 으로).

/** 묶음은 0, 1, 2 — 세 개까지 */
const val 최대묶음 = 3

/** 그 날 그 묶음의 칸 열쇠 */
fun 슬롯열쇠(날: String, 묶음: Int): String = if (묶음 <= 0) 날 else "$날#${묶음 + 1}"

/** 칸 열쇠가 어느 묶음의 칸인가 (날짜만 있으면 0) */
fun 슬롯묶음(k: String): Int = k.substringAfter('#', "").toIntOrNull()?.let { (it - 1).coerceAtLeast(0) } ?: 0

/** 그 날의 칸 열쇠들 — 묶음 순서대로 (예정에 있는 것만) */
fun 앱데이터.슬롯들(날: String): List<String> =
    예정.keys.filter { 날짜만(it) == 날 }.sortedBy { 슬롯묶음(it) }

/** 그 날 예정된 루틴들 — (칸 열쇠, 루틴), 묶음 순서. 지워진 루틴을 가리키는 칸은 뺀다 */
fun 앱데이터.예정루틴들(날: String): List<Pair<String, 루틴>> =
    슬롯들(날).mapNotNull { k -> 루틴(예정[k])?.let { k to it } }

/**
 * 그 날 칸 중 **이미 한** 칸들 — 기록과 칸을 짝짓는다.
 *  1) 같은 루틴으로 한 기록이 있는 칸
 *  2) 남은 기록(예정과 다른 것을 대신 했을 때)은 비어 있는 칸 하나를 채운다 — 단 **다른 묶음의 자동 루틴 기록은 대신하지 못한다**
 *     (집 이두를 했다고 헬스장 칸이 채워지면 안 된다). 한 묶음뿐이면 전처럼 "그 날 기록이 있으면 한 날" 이다
 */
fun 앱데이터.채운슬롯(날: String): Set<String> {
    val 칸들 = 슬롯들(날)
    if (칸들.isEmpty()) return emptySet()
    val 록 = 기록.filterKeys { 날짜만(it) == 날 }.toSortedMap().values.map { it.루틴id }.toMutableList()
    if (록.isEmpty()) return emptySet()
    val 채움 = LinkedHashSet<String>()
    for (k in 칸들) {
        val i = 록.indexOf(예정[k])
        if (i >= 0) { 채움 += k; 록.removeAt(i) }
    }
    for (k in 칸들) {
        if (k in 채움) continue
        val 칸묶음 = 루틴(예정[k])?.묶음 ?: 슬롯묶음(k)
        val i = 록.indexOfFirst { rid -> 루틴(rid).let { r -> r == null || !r.자동생성 || r.묶음 == 칸묶음 } }
        if (i >= 0) { 채움 += k; 록.removeAt(i) }
    }
    return 채움
}

/** 그 날 아직 안 한 칸들 (묶음 순서) */
fun 앱데이터.남은슬롯(날: String): List<String> = 슬롯들(날).let { 전부 -> 채운슬롯(날).let { c -> 전부.filter { it !in c } } }

/** 그 날 그 칸을 이미 했나 — 칸 열쇠 하나로 */
fun 앱데이터.칸함(k: String): Boolean = k in 채운슬롯(날짜만(k))

/** 이 날 기록이 있나 (한 번 더 포함) */
fun 앱데이터.날기록있음(날: String): Boolean = 기록.keys.any { 날짜만(it) == 날 }

/** 루틴을 처음 놓을 칸 — 자기 묶음의 칸이 비었으면 그것, 아니면 비어 있는 첫 칸. 가득 차면 null */
fun 앱데이터.놓을칸(날: String, r: 루틴): String? {
    val 쓴 = 슬롯들(날).toSet()
    val 내 = 슬롯열쇠(날, r.묶음)
    if (내 !in 쓴) return 내
    return (0 until 최대묶음).map { 슬롯열쇠(날, it) }.firstOrNull { it !in 쓴 }
}

// ─────────────── 제외 규칙 맞추기 ───────────────

/** 그 날이 이 규칙에 걸리나 */
fun 제외규칙.걸림(날: String): Boolean {
    val d = runCatching { LocalDate.parse(날짜만(날)) }.getOrNull() ?: return false
    return when (종류) {
        제외규칙.요일마다 -> d.dayOfWeek.value == 요일
        제외규칙.몇째주 -> d.dayOfWeek.value == 요일 && (
            if (주 == -1) d.plusDays(7).monthValue != d.monthValue else (d.dayOfMonth - 1) / 7 + 1 == 주)
        제외규칙.공휴일 -> 공휴일인가(날짜만(날))
        else -> false
    }
}

/** 이 루틴을 그 날에는 깔지 않나 */
fun 루틴.제외됨(날: String): Boolean = 제외.any { it.걸림(날) }

/** 규칙 한 줄을 사람이 읽는 말로 — 예: "매주 일요일" · "둘째 일요일" · "마지막 토요일" · "공휴일" */
fun 제외규칙.말(): String {
    val 요 = listOf("월", "화", "수", "목", "금", "토", "일").getOrElse(요일 - 1) { "?" } + "요일"
    return when (종류) {
        제외규칙.요일마다 -> "매주 $요"
        제외규칙.몇째주 -> (if (주 == -1) "마지막" else listOf("첫째", "둘째", "셋째", "넷째", "다섯째").getOrElse(주 - 1) { "${주}째" }) + " $요"
        제외규칙.공휴일 -> "공휴일"
        else -> "?"
    }
}
