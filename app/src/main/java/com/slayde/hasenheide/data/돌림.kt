package com.slayde.hasenheide.data

import kotlin.math.max

// ─────────────── 돌아가며 칸 (10-21 홍겸 님 · 시안 v26 · v27) ───────────────
//
// 앱 전체에 [돌림칸] 이 여러 개 있고, 루틴은 그 칸을 '자리표시 줄'([루틴종목.돌림자리]) 로 가리킨다.
// 여러 루틴이 같은 칸을 쓰면 어느 루틴에서 하든 같은 차례를 잇는다 ("같이 세기").
// 한 차례 = 한 번에 하는 종목들(여러 개 가능). 칸의 종목 줄은 보통 루틴 줄과 같아서 세트 · 무게 · 횟수를 따로 고친다.
//
// ⚠ 루틴에 **저장된** 줄은 자리표시 그대로 둔다. 보여 주거나 운동을 시작할 때만 [실제루틴] 으로 펼친다
//   ([플랜줄채움] 의 결과는 여러 곳에서 저장하므로 거기에 펼침을 넣으면 자리표시가 사라진다).

fun 앱데이터.돌림(id: String): 돌림칸? = 돌림들.firstOrNull { it.id == id }

/** 지금 할 차례 번호 (범위 안으로) */
fun 돌림칸.지금차례(): Int {
    if (차례들.isEmpty()) return 0
    val 시작 = 다음.coerceIn(0, 차례들.size - 1)
    // 빈 차례(종목을 다 뺀 차례)는 건너뛴다 — 안 그러면 그 차례에서 영영 안 넘어간다. 전부 비면 그대로
    return (0 until 차례들.size).map { (시작 + it) % 차례들.size }.firstOrNull { 차례들[it].isNotEmpty() } ?: 시작
}

/** 루틴에 저장되는 자리표시 줄 — 이름 · 세트는 쓰지 않는다 */
fun 돌림자리줄(id: String): 루틴종목 = 루틴종목("돌아가며", 세트 = 0, 휴식 = 0, 돌림id = id, 돌림k = -1)

/** 자리표시 줄을 그 칸의 다음 차례 종목들로 바꾼다. 칸이 없거나 차례가 비면 줄만 뺀다 */
fun 앱데이터.돌림펼침(r: 루틴): 루틴 {
    if (r.종목.none { it.돌림자리 }) return r
    return r.copy(종목 = r.종목.flatMap { e ->
        if (!e.돌림자리) return@flatMap listOf(e)
        val c = 돌림(e.돌림id ?: "") ?: return@flatMap emptyList()
        if (c.차례들.isEmpty()) return@flatMap emptyList()
        val k = c.지금차례()
        c.차례들[k].map { it.copy(돌림id = c.id, 돌림k = k) }
    })
}

/** 화면에 보이거나 운동을 시작할 루틴 — 돌아가며 칸을 펼치고 플랜 줄을 지금 회차로 채운다 */
fun 앱데이터.실제루틴(r: 루틴): 루틴 = 플랜줄채움(돌림펼침(r))

/** 이 칸을 쓰는 루틴들 (시안 `.돌림든곳`) */
fun 앱데이터.돌림쓰는루틴(id: String): List<루틴> = 루틴들.filter { r -> r.종목.any { it.돌림id == id && it.돌림자리 } }

/**
 * 운동을 저장할 때 — 오늘 한 차례의 값을 그 칸에 되쓰고(루틴 줄과 같은 규칙) 다음 차례로 넘긴다.
 *  · 넘기는 때: 그 차례에서 한 세트라도 했거나, 설정 [설정값.돌림건너뜀] 이 "넘어감" 일 때. 아니면 다음에 그 차례를 다시 한다
 *  · 운동 중 차례를 바꿨으면 줄들의 돌림k 가 바꾼 차례라, 그 차례에서 이어서 센다
 *  · 그 날만 조절한 운동은 값을 되쓰지 않는다 (루틴과 같게) — 차례는 넘어간다
 */
internal fun 앱데이터.돌림저장(S: 운동세션, 올릴까: Boolean): 앱데이터 {
    val 쓴 = S.종목들.mapNotNull { it.돌림id }.toSet()
    if (쓴.isEmpty()) return this
    return copy(돌림들 = 돌림들.map { c ->
        if (c.id !in 쓴 || c.차례들.isEmpty()) return@map c
        val 줄 = S.종목들.filter { it.돌림id == c.id }
        val k = 줄.first().돌림k.coerceIn(0, c.차례들.size - 1)
        val 했나 = 줄.any { it.찬것().isNotEmpty() }
        var 차례들 = c.차례들
        if (!S.조절됨 && 했나) {
            val 오늘 = S.copy(종목들 = 줄.filter { it.돌림k == k })
            var t = 루틴("돌림", "돌림", 종목 = c.차례들[k]).오늘반영(오늘, 돌림만 = true)
            if (올릴까) t = t.볼륨올리기(설정) { e -> 방식(e.종id, e.이름) }
            차례들 = 차례들.mapIndexed { j, x -> if (j == k) t.종목 else x }
        }
        val 다음 = if (했나 || 설정.돌림건너뜀 == "넘어감") (k + 1) % c.차례들.size else c.지금차례()
        c.copy(차례들 = 차례들, 다음 = 다음)
    })
}

/**
 * 보고서 저장을 되감을 때 — 저장 전 칸으로. 다음 차례는 늘 돌리고, 종목 값은 그 사이 차례 구조(종목)가 안 바뀐 칸만 돌린다
 * (루틴 탭에서 고친 것을 덮지 않게)
 */
internal fun List<돌림칸>.되감음(앞: List<돌림칸>): List<돌림칸> {
    if (앞.isEmpty()) return this
    return map { c ->
        val 옛 = 앞.firstOrNull { it.id == c.id } ?: return@map c
        val 같다 = 옛.차례들.size == c.차례들.size && 옛.차례들.indices.all { j -> 옛.차례들[j].map { it.열쇠 } == c.차례들[j].map { it.열쇠 } }
        c.copy(차례들 = if (같다) 옛.차례들 else c.차례들, 다음 = 옛.다음.coerceIn(0, max(0, c.차례들.size - 1)))
    }
}

/**
 * 루틴 줄을 고치는 일(종목 이름 바꾸기 · 지우기 · 카테고리 지우기 · 방식 전환 …)을 돌아가며 칸 안 종목에도 똑같이.
 * 한 차례의 종목이 다 지워져 빈 차례가 되면 그 차례를 뺀다(다음 차례 번호도 맞춘다). 차례가 하나도 안 남으면 칸을 없애고 루틴의 자리표시 줄도 뺀다
 */
internal fun 앱데이터.돌림도(f: (루틴) -> 루틴): 앱데이터 {
    if (돌림들.isEmpty()) return this
    val 지움 = mutableSetOf<String>()
    val 새들 = 돌림들.mapNotNull { c ->
        val 고침 = c.차례들.map { t -> if (t.isEmpty()) t else f(루틴("돌림", "돌림", 종목 = t)).종목 }
        val 남김 = 고침.indices.filter { j -> 고침[j].isNotEmpty() || c.차례들[j].isEmpty() }
        if (남김.isEmpty() && c.차례들.isNotEmpty()) { 지움 += c.id; return@mapNotNull null }
        val 앞빠짐 = (0 until c.다음.coerceAtMost(고침.size)).count { it !in 남김 }
        val 새다음 = c.다음 - 앞빠짐
        c.copy(차례들 = 남김.map { 고침[it] }, 다음 = if (새다음 in 0 until 남김.size) 새다음 else 0)   // 끝 차례가 빠져 넘치면 한 바퀴 돌아 처음
    }
    if (지움.isEmpty()) return copy(돌림들 = 새들)
    fun 루틴.자리뺌() = copy(종목 = 종목.filterNot { it.돌림자리 && it.돌림id in 지움 })
    return copy(돌림들 = 새들, 루틴들 = 루틴들.map { it.자리뺌() }, 그날운동 = 그날운동.map { it.자리뺌() })
}

// ─────────────── 운동 중 차례 바꾸기 ───────────────

/**
 * 운동 중 이 칸의 오늘 차례를 [k] 번째로 바꾼다 (시안 v27 `⇄`).
 *  · 그 칸의 줄 중 **한 세트도 안 한 것**은 빼고, 첫 줄 자리에 [k] 차례 종목들을 끼운다
 *  · 이미 세트를 한 줄은 남긴다 — 그 칸에서 떨어져(돌아가며 아님) '오늘만 넣은 종목' 으로 (기록에는 남고 칸에는 되쓰지 않는다)
 *  · 이미 그 차례면 그대로. 바꾼 뒤 줄이 하나도 안 남으면 null (못 바꿈)
 *  · 지금 하던 줄이 빠졌으면 새 첫 줄로 간다. [본] = 화면에서 보는 칸 번호
 */
fun 앱데이터.돌림차례바꿈(id: String, k: Int, 본: Int?): 운동자리? {
    val S = 세션 ?: return null
    val c = 돌림(id) ?: return null
    if (k !in c.차례들.indices) return null
    val 옛 = S.종목들.indices.filter { S.종목들[it].돌림id == id }
    if (옛.isEmpty()) return null
    if (옛.all { S.종목들[it].돌림k == k }) return 운동자리(S, 본, S.종목들.indices.toList())
    val 자리 = 옛.first()
    val 새줄 = c.차례들[k].map { it.copy(돌림id = id, 돌림k = k).세션줄() }
    val 새들 = ArrayList<세션종목>()
    val 표 = MutableList<Int?>(S.종목들.size) { null }
    var 첫새 = -1
    for (j in S.종목들.indices) {
        if (j == 자리) { 첫새 = 새들.size; 새들.addAll(새줄) }
        val e = S.종목들[j]
        if (j in 옛) {
            if (e.찬것().isNotEmpty()) { 표[j] = 새들.size; 새들.add(e.copy(돌림id = null, 돌림k = 0, 임시 = true)) }
        } else { 표[j] = 새들.size; 새들.add(e) }
    }
    if (새들.isEmpty()) return null
    val 새i = 표.getOrNull(S.i) ?: 첫새.coerceAtLeast(0)
    var T = S.번호맞춤(새들, 표, 새i.coerceIn(0, 새들.size - 1))
    if (표.getOrNull(S.i) == null) T = T.자리값(T.i, 다음빈칸(T.종목들[T.i], -1))
    val 새본 = 본?.let { b -> 표.getOrNull(b) } ?: T.i
    return 운동자리(T, 새본, 표)
}

// ─────────────── 칸 만들기 · 고치기 (루틴 화면이 부른다) ───────────────

private fun 앱데이터.새돌림id(): String {
    var n = 돌림들.size + 1
    while (돌림들.any { it.id == "돌$n" }) n++
    return "돌$n"
}

/** 루틴의 [줄] 번째 종목을 첫 차례로 하는 새 칸을 만들어 그 자리에 자리표시 줄을 둔다. 플랜 줄 · 이미 칸인 줄은 못 한다(null) */
fun 앱데이터.돌림만들기(루틴id: String, 줄: Int): 앱데이터? {
    val r = 루틴(루틴id) ?: return null
    val e = r.종목.getOrNull(줄) ?: return null
    if (e.플랜id != null || e.돌림id != null) return null
    val id = 새돌림id()
    return copy(돌림들 = 돌림들 + 돌림칸(id, listOf(listOf(e.copy(돌림id = null, 돌림k = -1, 슈퍼 = null))), 0),   // 슈퍼세트 이름이 루틴 밖으로 새지 않게
        루틴들 = 루틴들.map { x -> if (x.id != 루틴id) x else x.copy(종목 = x.종목.mapIndexed { j, y -> if (j == 줄) 돌림자리줄(id) else y }) })
}

/** 칸의 [차례] 번째에 종목을 더한다. 차례가 칸 밖이면(예: -1) 새 차례로. 플랜 줄은 못 넣는다 */
fun 앱데이터.돌림종목더하기(id: String, 차례: Int, e: 루틴종목): 앱데이터 {
    if (e.플랜id != null) return this
    val 줄 = e.copy(돌림id = null, 돌림k = -1)
    return copy(돌림들 = 돌림들.map { c ->
        if (c.id != id) c
        else if (차례 in c.차례들.indices) c.copy(차례들 = c.차례들.mapIndexed { j, t -> if (j == 차례) t + 줄 else t })
        else c.copy(차례들 = c.차례들 + listOf(listOf(줄)))
    })
}

/** 칸의 [차례] 번째 [j] 번째 종목 줄을 고친다 (세트 더하기 · 빼기 · 목표 바꾸기가 루틴 줄과 같은 함수를 쓰게) */
fun 앱데이터.돌림줄바꿈(id: String, 차례: Int, j: Int, f: (루틴종목) -> 루틴종목): 앱데이터 =
    copy(돌림들 = 돌림들.map { c ->
        if (c.id != id) c
        else c.copy(차례들 = c.차례들.mapIndexed { a, t -> if (a != 차례) t else t.mapIndexed { b, e -> if (b == j) f(e) else e } })
    })

/** 칸의 [차례] 번째에서 [j] 번째 종목을 뺀다. 차례가 비어도 차례는 남긴다 (차례째 빼는 것은 [돌림차례빼기]) */
fun 앱데이터.돌림종목빼기(id: String, 차례: Int, j: Int): 앱데이터 =
    copy(돌림들 = 돌림들.map { c ->
        if (c.id != id) c
        else c.copy(차례들 = c.차례들.mapIndexed { a, t -> if (a != 차례) t else t.filterIndexed { b, _ -> b != j } })
    })

/** 칸의 [차례] 번째를 뺀다. 마지막 차례였으면 칸을 없애고 루틴의 자리표시 줄도 뺀다 */
fun 앱데이터.돌림차례빼기(id: String, 차례: Int): 앱데이터 {
    val c = 돌림(id) ?: return this
    if (차례 !in c.차례들.indices) return this
    if (c.차례들.size <= 1) return 돌림없애기(id)
    val 새 = c.차례들.filterIndexed { a, _ -> a != 차례 }
    val 밀림 = if (c.다음 > 차례) c.다음 - 1 else c.다음
    val 다음 = if (밀림 in 0 until 새.size) 밀림 else 0   // 끝 차례를 뺐으면 처음으로
    return copy(돌림들 = 돌림들.map { if (it.id == id) it.copy(차례들 = 새, 다음 = 다음) else it })
}

/** 칸을 통째로 없앤다 — 모든 루틴의 자리표시 줄도 뺀다 */
fun 앱데이터.돌림없애기(id: String): 앱데이터 {
    fun 루틴.자리뺌() = copy(종목 = 종목.filterNot { it.돌림자리 && it.돌림id == id })
    return copy(돌림들 = 돌림들.filter { it.id != id }, 루틴들 = 루틴들.map { it.자리뺌() }, 그날운동 = 그날운동.map { it.자리뺌() })
}

/** 루틴의 [줄] 자리(맨 끝이면 줄 수)에 이미 있는 칸을 넣는다. 같은 칸이 그 루틴에 이미 있으면 안 넣는다 */
fun 앱데이터.돌림넣기(루틴id: String, 칸id: String, 줄: Int = Int.MAX_VALUE): 앱데이터 {
    val r = 루틴(루틴id) ?: return this
    if (돌림(칸id) == null || r.종목.any { it.돌림id == 칸id }) return this
    val at = 줄.coerceIn(0, r.종목.size)
    return copy(루틴들 = 루틴들.map { x -> if (x.id != 루틴id) x else x.copy(종목 = x.종목.toMutableList().also { it.add(at, 돌림자리줄(칸id)) }) })
}

/** 루틴에서 칸을 뺀다 (자리표시 줄만 — 칸은 다른 루틴이 쓸 수 있어 남긴다) */
fun 앱데이터.돌림빼기(루틴id: String, 칸id: String): 앱데이터 =
    copy(루틴들 = 루틴들.map { x -> if (x.id != 루틴id) x else x.copy(종목 = x.종목.filterNot { it.돌림자리 && it.돌림id == 칸id }) })

/** 한 차례를 글로 — "레그 컬 + 레그 익스텐션" (빈 차례는 "빈 차례") */
fun List<루틴종목>.차례글(): String = if (isEmpty()) "빈 차례" else joinToString(" + ") { it.이름 }

/** 한 차례를 칩 글로 — 첫 종목 + 나머지 개수 ("레그 컬 +1") */
fun List<루틴종목>.차례칩글(): String = if (isEmpty()) "빈 차례" else first().이름 + (if (size > 1) " +${size - 1}" else "")
