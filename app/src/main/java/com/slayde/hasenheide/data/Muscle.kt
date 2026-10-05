package com.slayde.hasenheide.data

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * 근육 지도 · 피로 계산 (10-02) — 규칙은 claude.ai 프로젝트 문서 claude/07_근육지도규칙.md.
 * 7일 체험 아티팩트의 근육 모듈(잎 · 종목근육 · 유효무게 · 잎볼륨 · 오늘단계 · 남은피로 · 지금단계 · 피로저장 · 단계색 · 확대상자 · 근육글)을
 * **같은 계산**으로 옮겼다. 표(근육 나무 · 그림 조각 · 종목 규칙 · 색표)는 MuscleData.kt — tools/근육지도_생성.py 가 만든다.
 *
 * 흐름
 *  · 운동을 저장할 때 — 잎(가장 잘게 나눈 근육)마다 오늘 오른 단계(0~20)를 피로로 쌓고, 이전 최대 볼륨을 올린다
 *  · 운동 중 그림 — 남은 피로 + 오늘 지금까지 오른 단계. 시간이 지나면 곧게 내려가 원래 색으로 돌아온다
 *
 * 이 파일은 안드로이드를 쓰지 않는다 → 시험(MuscleTest.kt)이 컴퓨터에서 돈다.
 */

/** 그림 조각 하나 (07 3절) — 뒤 = 뒷모습(x + 240) · 종류 'm' 근육 / 'b' 바탕 / 'x' 결 · 대신 = data-covers */
data class 몸조각(val 뒤: Boolean, val 종류: Char, val 근육: String, val 대신: List<String>, val 좌우: String, val d: String)

/** 잎 하나의 피로 — 시작 시각에 lv0 단계, 끝 시각에 0 (그 사이는 곧게 내려간다). 시각은 ms */
data class 피로상태(val lv0: Double, val 시작: Long, val 끝: Long)

/** 근육 계산에 넣는 종목 한 줄 — 끝낸 세트 · 계획한 세트 전부(처음 하는 근육의 기준). 워밍업은 넣지 않는다 */
data class 근육입력(
    val 이름: String, val 한것: List<세트>, val 계획: List<세트> = 한것,
    /** 종목 id (10-05 · 스키마 15) — 사용자 근육 역할을 찾을 때 */
    val 종id: String? = null,
    /** 사용자가 고른 근육 → 역할 (P · Y). null 이면 내장 규칙 (시안 `종목근육` — 종목표의 근육이 먼저) */
    val 근육: Map<String, String>? = null,
)

/** 설정 칩 값 · 문구 — 한곳에 (설정 '운동 중 그림' 카드) */
object 근육표 {
    const val 단계수 = 20
    /** 운동 중 위쪽 그림 칸 보기 (08 3절) */
    val 배너목록 = listOf("근육 2장", "근육 + 사진", "사진 1장", "사진 2장", "숨김")
    /** 근육 회복 시간(시간) — 6시간 ~ 7일 (07 5절) */
    val 회복시간목록 = listOf(6, 12, 24, 36, 48, 72, 96, 120, 144, 168)
    /** 색표 id → 칩 글 */
    val 색표목록 = listOf("heat" to "노랑→빨강", "red" to "빨강", "blue" to "파랑")
    /** 종목당 사진 수 (08 4절) */
    const val 사진최대 = 10
    /** 맨몸(0kg) 세트는 체중의 이만큼으로 센다 [제안] */
    const val 맨몸비율 = 0.6
    /** 확대 상자의 여백(그림 좌표) */
    const val 확대여백 = 28f
    /** 그림 전체 좌표계 */
    const val 그림폭 = 440f
    const val 그림높이 = 460f
    /** 뒷모습은 오른쪽으로 이만큼 옮겨 그린다 */
    const val 뒤옮김 = 240f
    val 역할이름 = listOf("P" to "주동근", "S" to "보조근", "Y" to "협응근")

    fun 회복시간글(h: Int): String = if (h < 48) "${h}시간" else "${h / 24}일"
}

object 근육계산 {

    private val 자식: Map<String, List<String>> by lazy {
        val m = LinkedHashMap<String, MutableList<String>>()
        근육자료.부모.forEach { (id, p) -> m.getOrPut(p) { mutableListOf() }.add(id) }
        m
    }
    private val 잎캐시 = HashMap<String, List<String>>()

    /** 잎 — 이 근육 아래 가장 잘게 나눈 근육들 (자식이 없으면 자기 자신). 값은 여기로 내려간다 (07 2절 ①) */
    fun 잎(id: String): List<String> = synchronized(잎캐시) {
        잎캐시[id] ?: (자식[id]?.takeIf { it.isNotEmpty() }?.flatMap { 잎(it) } ?: listOf(id)).also { 잎캐시[id] = it }
    }

    /** 종목 → 근육과 역할 — 이름 속 낱말로 먼저 찾고, 없으면 종목의 부위로 */
    fun 종목근육(이름: String, 부위: String?): Map<String, String> {
        for ((낱말, 표) in 근육자료.규칙) if (낱말.any { 이름.contains(it) }) return 표
        return 부위?.let { 근육자료.부위기본[it] } ?: emptyMap()
    }

    /** 맨몸 0kg 세트는 체중의 60% 로 센다 (체중을 모르면 1) */
    fun 유효무게(w: Double, 체중: Double): Double = if (w > 0) w else if (체중 > 0) 체중 * 근육표.맨몸비율 else 1.0

    /** 잎마다 유효 볼륨 = Σ 무게 × 횟수 × 역할 비중. 다=true 면 계획한 세트 전부 */
    fun 잎볼륨(들: List<근육입력>, 부위: (String) -> String?, 체중: Double, 다: Boolean = false): Map<String, Double> {
        val v = LinkedHashMap<String, Double>()
        for (e in 들) {
            val 합 = (if (다) e.계획 else e.한것).sumOf { 유효무게(it.w, 체중) * it.r }
            if (합 <= 0.0) continue
            for ((id, 역) in (e.근육?.takeIf { it.isNotEmpty() } ?: 종목근육(e.이름, 부위(e.이름)))) {
                val 비 = 근육자료.역할[역] ?: 0.0
                for (l in 잎(id)) v[l] = (v[l] ?: 0.0) + 합 * 비
            }
        }
        return v
    }

    /** 오늘 오른 단계 = 20 × 오늘 볼륨 ÷ 이전 최대 (처음이면 오늘 계획) — 20 에서 멈춘다 (07 4절) */
    fun 오늘단계(들: List<근육입력>, 부위: (String) -> String?, 체중: Double, 최대볼륨: Map<String, Double>): Map<String, Double> {
        val v = 잎볼륨(들, 부위, 체중)
        val 계 = 잎볼륨(들, 부위, 체중, 다 = true)
        val out = LinkedHashMap<String, Double>()
        for ((l, x) in v) {
            if (x <= 0.0) continue
            val 기 = 최대볼륨[l]?.takeIf { it > 0 } ?: 계[l]?.takeIf { it > 0 } ?: x
            out[l] = min(근육표.단계수.toDouble(), 근육표.단계수 * x / 기)
        }
        return out
    }

    /** 남은 피로 — lv0 에서 끝 시각까지 곧게 내려간다 (07 5절) */
    fun 남은피로(f: 피로상태?, t: Long): Double {
        if (f == null || t >= f.끝) return 0.0
        if (t <= f.시작) return f.lv0
        return f.lv0 * (f.끝 - t).toDouble() / (f.끝 - f.시작).toDouble()
    }

    /** 지금 단계 — 남은 피로 + 오늘 지금까지 오른 단계 (20 에서 멈춘다) */
    fun 지금단계(피로: Map<String, 피로상태>, 오늘: Map<String, Double>, t: Long): Map<String, Double> {
        val out = LinkedHashMap<String, Double>()
        for (l in 피로.keys + 오늘.keys) {
            val x = min(근육표.단계수.toDouble(), 남은피로(피로[l], t) + (오늘[l] ?: 0.0))
            if (x > 0.01) out[l] = x
        }
        return out
    }

    /**
     * 운동을 저장할 때 — 피로를 쌓고(회복 전이면 남은 시간 위에 더한다) 이전 최대를 올린다 (07 4·5절)
     *  · lv0 = min(20, 남은 + 오늘) · 끝 = 지금 + 남은 시간 + 회복시간 × (오늘 ÷ 20)
     *  · 이전 최대 = max(이전, 오늘 볼륨) — 줄어든 날은 내리지 않는다
     */
    fun 피로저장(
        피로: Map<String, 피로상태>, 최대볼륨: Map<String, Double>, 들: List<근육입력>,
        부위: (String) -> String?, 체중: Double, 회복시간: Int, t: Long,
    ): Pair<Map<String, 피로상태>, Map<String, Double>> {
        val v = 잎볼륨(들, 부위, 체중)
        val s = 오늘단계(들, 부위, 체중, 최대볼륨)
        val 휴 = 회복시간 * 3_600_000.0
        val 새피로 = LinkedHashMap(피로)
        for ((l, k) in s) {
            if (!(k > 0)) continue
            val f = 피로[l]
            val r = 남은피로(f, t)
            val 남 = if (f != null) max(0L, f.끝 - t) else 0L
            새피로[l] = 피로상태(min(근육표.단계수.toDouble(), r + k), t, t + 남 + (휴 * (k / 근육표.단계수)).roundToLong())
        }
        val 새최대 = LinkedHashMap(최대볼륨)
        for ((l, x) in v) 새최대[l] = max(새최대[l] ?: 0.0, x)
        return 새피로 to 새최대
    }

    /** 다 회복한 잎은 지운다 — 파일이 끝없이 커지지 않게 */
    fun 회복끝치움(피로: Map<String, 피로상태>, t: Long): Map<String, 피로상태> = 피로.filterValues { it.끝 > t }

    /** 색 — 20단계. 0 은 null(기본색). 0xRRGGBB (07 4절) */
    fun 단계색(k: Double, 표이름: String): Int? {
        val n = k.roundToInt()
        if (n <= 0) return null
        val st = 근육자료.색표[표이름] ?: 근육자료.색표.getValue("heat")
        val t = min(1.0, n / 근육표.단계수.toDouble())
        if (t <= st[0].first) return st[0].second
        for (i in 1 until st.size) {
            if (t <= st[i].first) {
                val (a, ca) = st[i - 1]; val (b, cb) = st[i]
                return 섞기(ca, cb, (t - a) / (b - a))
            }
        }
        return st.last().second
    }

    private fun 섞기(a: Int, b: Int, f: Double): Int {
        var out = 0
        for (sh in intArrayOf(16, 8, 0)) {
            val x = (a shr sh) and 0xFF; val y = (b shr sh) and 0xFF
            out = out or ((x + (y - x) * f).roundToInt().coerceIn(0, 255) shl sh)
        }
        return out
    }

    /** 조각마다 칠할 잎 — 자기 근육과 대신 맡은 근육 아래의 잎 (바탕 · 결은 빈 목록) */
    val 조각잎: List<List<String>> by lazy {
        근육자료.조각.map { p -> if (p.종류 == 'm') (listOf(p.근육) + p.대신).flatMap { 잎(it) } else emptyList() }
    }

    /** 조각마다 값 — 그 조각의 잎 중 가장 큰 값 = 가장 지친 타겟근육 (07 2절 ③) */
    fun 조각값(단계: Map<String, Double>): List<Double> =
        조각잎.map { l -> l.maxOfOrNull { 단계[it] ?: 0.0 }?.coerceAtLeast(0.0) ?: 0.0 }

    /** 조각마다 상자 [x0, y0, x1, y1] — 그림 좌표(뒷모습은 옮긴 뒤) */
    val 조각상자: List<FloatArray> by lazy {
        val 수 = Regex("-?\\d+(\\.\\d+)?")
        근육자료.조각.map { p ->
            val n = 수.findAll(p.d).map { it.value.toFloat() }.toList()
            var x0 = 1e9f; var y0 = 1e9f; var x1 = -1e9f; var y1 = -1e9f
            var i = 0
            while (i + 1 < n.size) { x0 = min(x0, n[i]); x1 = max(x1, n[i]); y0 = min(y0, n[i + 1]); y1 = max(y1, n[i + 1]); i += 2 }
            val o = if (p.뒤) 근육표.뒤옮김 else 0f
            floatArrayOf(x0 + o, y0, x1 + o, y1)
        }
    }

    /** 지금 종목이 쓰는 근육(주동 · 보조)만 크게 — 그 조각들의 상자를 정사각형으로 [x, y, 폭, 높이]. 없으면 null */
    fun 확대상자(이름: String, 부위: String?): FloatArray? {
        val 쓸잎 = 종목근육(이름, 부위).filter { it.value != "Y" }.keys.flatMap { 잎(it) }.toSet()
        val idx = 근육자료.조각.indices.filter { i -> 조각잎[i].any { it in 쓸잎 } }
        if (idx.isEmpty()) return null
        val 앞 = idx.filter { !근육자료.조각[it].뒤 }
        val 뒤 = idx.filter { 근육자료.조각[it].뒤 }
        val 쓸 = if (앞.size >= 뒤.size) 앞 else 뒤
        var x0 = 1e9f; var y0 = 1e9f; var x1 = -1e9f; var y1 = -1e9f
        쓸.forEach { val q = 조각상자[it]; x0 = min(x0, q[0]); y0 = min(y0, q[1]); x1 = max(x1, q[2]); y1 = max(y1, q[3]) }
        val s = max(x1 - x0, y1 - y0) + 근육표.확대여백
        val cx = (x0 + x1) / 2; val cy = (y0 + y1) / 2
        return floatArrayOf(cx - s / 2, cy - s / 2, s, s)
    }

    /** "주동근 가슴 가운데 · 보조근 가슴 윗부분, 어깨 앞" — 괄호 속 풀이는 뺀다 */
    fun 근육글(m: Map<String, String>): String {
        // 감시관: 협응근까지 쓰면 한 줄(D1-4)을 넘는다 → 주동근 · 보조근만
        val 묶 = 근육표.역할이름.filter { it.first != "Y" }.mapNotNull { (r, n) ->
            val l = m.filter { it.value == r }.keys.map { (근육자료.이름[it] ?: it).replace(Regex(" \\(.*\\)"), "") }
            if (l.isEmpty()) null else "$n ${l.joinToString(", ")}"
        }
        return 묶.joinToString(" · ").ifEmpty { "근육 정보 없음" }
    }
}

// ─────────────── 앱데이터와 잇기 ───────────────

/** 종목의 부위 (종목표에 없으면 null) */
fun 앱데이터.종목부위(이름: String): String? = 종목표.firstOrNull { it.이름 == 이름 }?.부위

/** 사용자가 고른 근육 역할을 채운다 (10-05 · 시안 `종목근육` — 종목표의 근육이 있으면 그것). 이미 있으면 그대로 */
fun 앱데이터.근육채움(들: List<근육입력>): List<근육입력> = 들.map { e ->
    if (e.근육 != null) e else e.copy(근육 = 종목찾기(e.종id, e.이름)?.근육?.takeIf { it.isNotEmpty() })
}

/** 종목의 근육 → 역할 — 사용자가 고른 것이 먼저, 없으면 내장 규칙 (이름 · 부위) */
fun 앱데이터.종목근육(종id: String?, 이름: String): Map<String, String> {
    val t = 종목찾기(종id, 이름)
    return t?.근육?.takeIf { it.isNotEmpty() } ?: 근육계산.종목근육(t?.이름 ?: 이름, t?.부위 ?: 종목부위(이름))
}

/** 운동 중 종목들 → 근육 계산에 넣을 줄. 워밍업(종류 1)은 뺀다. 계획 = 칸마다 보이는 값(기록 · 고친 값 · 기본값) */
fun 운동세션.근육입력들(): List<근육입력> = 종목들.map { e ->
    근육입력(
        e.이름,
        e.찬것().filter { it.종류 != 세트종류.워밍업 },
        (0 until e.총칸()).map { k -> 세트값(e, k, 지금이면 = false) }.filter { it.종류 != 세트종류.워밍업 },
        종id = e.종id,
    )
}

/** 지금 그림에 칠할 단계 — 남은 피로 + (운동 중이면) 오늘 지금까지 오른 단계 */
fun 앱데이터.근육단계(들: List<근육입력>?, t: Long): Map<String, Double> {
    val 오늘 = if (들 == null) emptyMap() else 근육계산.오늘단계(근육채움(들), { 종목부위(it) }, 몸.체중, 최대볼륨)
    return 근육계산.지금단계(피로, 오늘, t)
}

/** 운동을 저장할 때 피로를 쌓는다 — 운동을 끝낸 시각 [t] 에서 시작 */
fun 앱데이터.피로쌓기(들: List<근육입력>, t: Long): 앱데이터 {
    val (f, m) = 근육계산.피로저장(근육계산.회복끝치움(피로, t), 최대볼륨, 근육채움(들), { 종목부위(it) }, 몸.체중, 설정.회복시간, t)
    return copy(피로 = f, 최대볼륨 = m)
}

/** 종목 한 줄의 근육 글 (종목 탭에서 펼쳤을 때) */
fun 앱데이터.종목근육글(이름: String): String = 근육계산.근육글(종목근육(null, 이름))
