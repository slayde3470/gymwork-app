package com.slayde.hasenheide.ui

import com.slayde.hasenheide.data.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * 플랜 종목 목표일까지 돌리기 — 8종목 × 훈련방식 × 수행 모습(정상 · 미달 · 상회 · 들쭉날쭉 · 혼합 · 뒤죽박죽)
 * 실행: java -cp fz.jar:data.jar:kotlin-stdlib.jar com.slayde.hasenheide.ui.P30 [시작씨앗] [씨앗수] [모드...]
 */
internal enum class 모습 { 정상, 미달, 상회, 들쭉날쭉, 혼합, 뒤죽박죽, 느림, 빠름, 컨디션, 과대, 과소 }

internal class 사례(val 종목: String, val 방식: Int, val 모습: 모습, val 씨앗: Int)

internal class 결과값(val 사례: 사례) {
    var 회수 = 0
    var 처음회표 = 0
    var 끝남 = false
    var 정체 = false
    var 늦음 = 0
    var 측정수 = 0
    var 처음목표 = 0.0
    var 마지막진행 = 0.0
    val 경고 = ArrayList<String>()
    var 로그 = ArrayList<String>()
}

internal class P30(val c: 사례, val 위반: MutableMap<String, MutableList<String>>) {
    val rnd = Random(c.씨앗 * 7919 + c.방식 * 104729 + c.종목.hashCode())
    val 존 = ZoneId.systemDefault()
    var 날 = LocalDate.of(2026, 3, 2)
    var 지금 = 날.atTime(12, 0).atZone(존).toInstant().toEpochMilli()
    val 몸 = 몸조건(나이 = listOf(19, 25, 33, 45, 55, 65)[rnd.nextInt(6)], 남 = rnd.nextBoolean(), 체중 = 50.0 + rnd.nextInt(0, 60))
    val t = 플랜표.찾기(c.종목)!!
    var d: 앱데이터
    val r = 결과값(c)
    val 로그 = ArrayList<String>()
    var 번호 = 0

    fun 날글() = 날.toString()
    fun 틱일(n: Int = 1 + rnd.nextInt(3)) { 날 = 날.plusDays(n.toLong()); 지금 = 날.atTime(12, 0).atZone(존).toInstant().toEpochMilli() }

    fun 기록(이름: String, 설명: String) {
        val 목 = 위반.getOrPut("${c.모습}:$이름") { ArrayList() }
        if (목.size < 3) 목.add("[${c.종목} 방식${c.방식} ${c.모습} 씨앗${c.씨앗}] $설명\n    최근: " + 로그.takeLast(12).joinToString(" → "))
    }

    init {
        val 맨몸 = t.맨몸인가
        var f = 플랜폼값(종목 = c.종목, 방식번호 = if (맨몸) 1 else c.방식, 강도 = rnd.nextInt(3))
        if (!맨몸 && c.방식 == 1 && rnd.nextInt(3) == 0) f = f.copy(세트수 = 2 + rnd.nextInt(5), 직접횟수 = 3 + rnd.nextInt(10))
        val 입력안함 = rnd.nextInt(6) == 0
        if (맨몸) {
            val 시작 = 1 + rnd.nextInt(t.횟수경계.last())
            val 목 = (시작 * (1.5 + rnd.nextDouble() * 3.0)).roundToInt().coerceAtLeast(시작 + 2)
            f = f.copy(정자세 = 시작.toString(), 목표개수 = 목.toString(), 입력안함 = 입력안함)
            if (t.보조옵션 && rnd.nextBoolean()) f = f.copy(보조모드 = 1 + rnd.nextInt(2), 보조무게 = (5 + rnd.nextInt(30)).toString(), 보조횟수 = (3 + rnd.nextInt(8)).toString(), 정자세 = "")
        } else {
            val 경계 = if (몸.남) t.경계남 else t.경계여
            val 비 = 경계[0] * 0.8 + rnd.nextDouble() * (경계[3] * 1.05 - 경계[0] * 0.8)
            val rm = 몸.체중 * 비
            val 횟 = 3 + rnd.nextInt(8)
            val w = (rm / (1 + 횟 / 30.0) / 2.5).roundToInt() * 2.5
            val 시작 = 일RM(max(w, 20.0), 횟)
            val 목표 = ((시작 * (1.12 + rnd.nextDouble() * 0.6)) / 2.5).roundToInt() * 2.5
            f = f.copy(현재무게 = max(w, 20.0).toString(), 현재횟수 = 횟.toString(), 목표무게 = 목표.toString(), 입력안함 = 입력안함)
            if (rnd.nextInt(3) == 0) f = f.copy(목표방식 = 목표형식.무게횟수, 목표횟수 = (2 + rnd.nextInt(6)).toString(), 목표무게 = (목표 / (1 + 4 / 30.0) / 2.5).roundToInt().times(2.5).toString())
        }
        val (p, 오류) = 플랜만들기(f, 몸, emptyList(), "p1", 날글())
        if (p == null) throw 막힘("플랜만들기 실패: $오류 폼=$f 몸=$몸")
        var dd = 앱데이터(몸 = 몸, 플랜들 = listOf(p), 설정 = 설정값(볼륨켬 = false))
        val 줄 = dd.플랜줄채움(루틴종목(p.이름, 플랜id = "p1"))
        val 다른 = listOf(루틴종목("컬", 3, 12.0, 12, 45), 루틴종목("레이즈", 3, 8.0, 15, 45))
        val rr = 루틴("rp", "플랜날", 종목 = if (c.모습 == 모습.혼합 || c.모습 == 모습.뒤죽박죽) listOf(줄) + 다른 else listOf(줄))
        val r2 = 루틴("rx", "다른날", 종목 = listOf(루틴종목(c.종목, 4, 40.0, 8, 90), 루틴종목("딥스", 3, 0.0, 10, 60)))
        d = dd.copy(루틴들 = listOf(rr, r2)).예정초기화(날글())
        r.처음회표 = p.회표(몸, d.향상기록들).size
        r.처음목표 = p.목표진행값
    }

    class 막힘(m: String) : Exception(m)

    // ── 한 번 운동하는 모습 ──
    fun 플랜() = d.플랜찾기("p1")

    /** 세션 한 번: 시작 → (모습대로) 수행 → 저장. 반환 = 저장함 */
    fun 한번(수행: (회계획, Int, 세트) -> 세트?): Boolean {
        현재마지막 = -1
        val p = 플랜() ?: return false
        val 계획 = p.다음회(d.몸, d.향상기록들) ?: return false
        val 루 = d.루틴("rp")!!
        val S0 = d.조절해시작(루, 날글(), 지금) ?: return false
        var S = S0
        val j플랜 = S.종목들.indexOfFirst { it.플랜id == "p1" }
        if (j플랜 < 0) { 기록("플랜줄없음", "세션에 플랜 줄이 없다"); return false }
        // 처방과 세션이 같은가
        val 처방 = 처방세트(회처방(p, 계획.목표값, d.설정.무게폭, d.몸, 계획.측정일, 계획.주))
        val 본 = (0 until S.종목들[j플랜].세트).map { k -> 세트값세션(S, j플랜, k) }.filter { it.종류 != 세트종류.워밍업 }
        if (d.조절[날글()] == null && 본.map { it.w to it.r } != 처방.map { it.w to it.r }) 기록("세션처방이플랜과다름", "회${계획.회} 세션=${본.map { it.w to it.r }} 처방=${처방.map { it.w to it.r }}")
        if (계획.측정일 && !p.횟수진행 && S.종목들[j플랜].세트 == 처방.size) 기록("측정일워밍업없음", "회${계획.회}")
        if (본.isEmpty()) { 기록("처방비었음", "회${계획.회}"); return false }
        현재마지막 = (0 until S.종목들[j플랜].세트).last { 세트값세션(S, j플랜, it).종류 != 세트종류.워밍업 }
        var 체크한본 = 0
        // 다른 줄(혼합): 체크
        for (j in S.종목들.indices) {
            for (k in 0 until S.종목들[j].세트) {
                val 예 = 세트값세션(S, j, k)
                if (j == j플랜 && 예.종류 != 세트종류.워밍업) {
                    val 해 = 수행(계획, k, 예)
                    if (해 == null) continue   // 이 세트는 안 한다
                    if (해.w != 예.w) S = S.값고치기(j, k, 새무게 = 해.w)
                    if (해.r != 예.r) S = S.값고치기(j, k, 새횟수 = 해.r)
                    if (해.r > 0) 체크한본++
                }
                S = S.체크(j, k, 지금 + (j * 10 + k) * 1000L)
            }
        }
        val 전 = d
        d = d.copy(세션 = S).운동저장하기(날글(), 지금 + 3_600_000L)
        d = d.copy(세션 = null)
        r.회수++
        if (체크한본 == 0) { // 한 세트도 안 했으면 플랜은 그대로여야 한다
            if (플랜() != 전.플랜찾기("p1")) 기록("안했는데플랜이바뀜", "회${계획.회}")
            return false
        }
        검사(전, d, 계획, 처방)
        return true
    }

    fun 세트값세션(S: 운동세션, j: Int, k: Int): 세트 {
        val e = S.종목들[j]
        return e.예정값.getOrNull(k) ?: 세트(e.무게, e.횟수)
    }

    // ── 매번 지키는 것 ──
    var 앞한회 = 0
    var 알려진 = 0
    var 현재마지막 = -1
    fun 검사(전: 앱데이터, 후: 앱데이터, 계획: 회계획, 처방: List<세트>) {
        val p0 = 전.플랜찾기("p1")!!; val p1 = 후.플랜찾기("p1") ?: run { 기록("플랜사라짐", "저장 뒤 플랜이 없다"); return }
        로그.add("회${계획.회}${if (계획.측정일) "★" else ""}")
        if (p1.한회 != p0.한회 + 1) 기록("한회가1이아님", "${p0.한회} → ${p1.한회}")
        if (!p1.누적볼륨.isFinite() || p1.누적볼륨 < p0.누적볼륨) 기록("누적볼륨이상", "${p0.누적볼륨} → ${p1.누적볼륨}")
        if (계획.측정일) {
            r.측정수++
            val 조절12 = 전.조절[날글()] != null && 처방.isNotEmpty() && 처방.all { it.r > 플랜표.환산최대횟수 }
            if (p1.측정들.size != p0.측정들.size + 1) { if (조절12) 알려진 += 1 else 기록("측정일에측정이안쌓임", "회${계획.회} 측정 ${p0.측정들.size} → ${p1.측정들.size}") }
            val m = p1.측정들.lastOrNull()
            if (m != null && p1.측정들.size == p0.측정들.size + 1) {
                if (m.회 != 계획.회) 기록("측정회어긋남", "측정회=${m.회} 계획회=${계획.회} 한회=${p1.한회}")
                if (!(m.무게 > 0 && m.횟수 >= 1)) 기록("측정값이상", "$m")
            }
            if (후.향상기록들.size != 전.향상기록들.size + 1 && p1.측정들.size > p0.측정들.size) 기록("향상기록안쌓임", "${전.향상기록들.size} → ${후.향상기록들.size}")
        } else if (p1.측정들.size != p0.측정들.size) 기록("보통날에측정이쌓임", "회${계획.회}")
        listOf(p1.지금진행값, p1.목표진행값, p1.시작진행값).forEach { if (!it.isFinite() || it <= 0) 기록("진행값이상", "$it ${p1}") }
        // 루틴 줄이 다음 처방과 같은가
        후.루틴들.forEach { rr -> rr.종목.forEach { e -> if (e.플랜id != null && 후.플랜줄채움(e) != e) 기록("루틴플랜줄이처방과다름", "${rr.이름} ${e.이름}") } }
        val 다음 = p1.다음회(후.몸, 후.향상기록들)
        if (다음 != null) {
            if (다음.회 != p1.한회 + 1 && p1.기준회 <= p1.한회) 기록("다음회번호어긋남", "다음=${다음.회} 한회=${p1.한회} 기준회=${p1.기준회}")
            val 새처방 = 회처방(p1, 다음.목표값, 후.설정.무게폭, 후.몸, 다음.측정일, 다음.주)
            if (새처방.isEmpty() || 새처방.any { !(it.무게 > 0) || it.횟수 < 1 || it.세트 < 1 }) 기록("다음처방이상", "회${다음.회} $새처방")
            if (!p1.횟수진행 && 새처방.any { it.무게 > p1.목표1RM * 1.05 }) 기록("처방무게가목표를넘음", "회${다음.회} $새처방 목표1RM=${p1.목표1RM}")
            if (다음.측정일 != (다음.회 % 측정간격(다음.수준값) == 0 || (p1.측정먼저 && 다음.회 == 1))) 기록("측정일표시이상", "회${다음.회}")
        }
    }

    // ── 수행 모습 ──
    fun 정확(g: 회계획, k: Int, 예: 세트): 세트? = 예
    fun 바꿈(예: 세트, 무게배: Double, 횟수더: Int): 세트 {
        val w = if (t.맨몸인가) 예.w else max(2.5, 예.w * 무게배)
        return 세트(w, max(1, 예.r + 횟수더), 예.종류)
    }
    fun 미달(g: 회계획, k: Int, 예: 세트): 세트? {
        val 빈번 = g.측정일 && rnd.nextInt(10) < 7
        if (!빈번) return if (rnd.nextInt(8) == 0) 바꿈(예, 1.0, -1) else 예
        return if (rnd.nextBoolean()) 바꿈(예, 0.97 - rnd.nextDouble() * 0.1, 0) else 바꿈(예, 1.0, -(1 + rnd.nextInt(2)))
    }
    fun 상회(g: 회계획, k: Int, 예: 세트): 세트? {
        if (!g.측정일) return 예
        return if (rnd.nextBoolean()) 바꿈(예, 1.03 + rnd.nextDouble() * 0.12, 0) else 바꿈(예, 1.0, 1 + rnd.nextInt(3))
    }
    fun 들쭉(g: 회계획, k: Int, 예: 세트): 세트? {
        if (!g.측정일) return if (rnd.nextInt(15) == 0) null else 예
        val f = 0.8 + rnd.nextDouble() * 0.4
        return when (rnd.nextInt(3)) { 0 -> 바꿈(예, f, 0); 1 -> 바꿈(예, 1.0, ((f - 1) * 예.r).roundToInt()); else -> if (rnd.nextInt(6) == 0) null else 예 }
    }

    // ── 한 줄로 끝까지 ──
    fun 달리기(): 결과값 {
        val 한계 = max(300, r.처음회표 * 4 + 100)
        if (c.모습 == 모습.뒤죽박죽) {
            repeat(320) { 뒤죽박죽한번(); 틱일(if (rnd.nextInt(5) == 0) 0 else 1 + rnd.nextInt(3)) }
            r.끝남 = true
            return r
        }
        while (r.회수 < 한계) {
            val 계획 = 플랜()?.다음회(d.몸, d.향상기록들) ?: run { r.끝남 = true; null } ?: break
            when (c.모습) {
                모습.정상 -> 정상한번()
                모습.미달 -> 한번(::미달)
                모습.상회 -> 한번(::상회)
                모습.들쭉날쭉 -> 한번(::들쭉)
                모습.혼합 -> 혼합한번()
                모습.뒤죽박죽 -> 뒤죽박죽한번()
                else -> 능력한번()
            }
            if (rnd.nextInt(4) != 0) 틱일(1 + rnd.nextInt(3)) else 틱일(1)
        }
        if (!r.끝남) { r.정체 = true }
        r.마지막진행 = 플랜()?.지금진행값 ?: 0.0
        if (r.끝남 && A > 0) {
            val p = 플랜()!!
            val 목 = if (p.횟수진행) p.목표개수 else p.목표1RM
            val 지금실력 = if (p.횟수진행) A else A
            if (지금실력 < 목 * (if (p.횟수진행) 0.8 else 0.93)) 기록("실력이목표에못미치는데플랜끝남", "실력=${"%.1f".format(지금실력)} 목표=${"%.1f".format(목)} 회수=${r.회수}")
            if (넘은회 >= 0 && r.회수 - 넘은회 > 80) 기록("실력은넘었는데플랜이한참계속됨", "실력이 목표를 넘은 회=${넘은회} 플랜 끝=${r.회수}")
            r.늦음 = if (넘은회 >= 0) r.회수 - 넘은회 else 0
        }
        return r
    }

    // ── 실력 모델 (진짜 실력이 따로 있고, 그 실력으로 할 수 있는 만큼만 한다) ──
    var A = 0.0            // 무게 종목: 1RM(kg) · 맨몸: 정자세 최대 연속 횟수
    var 속도배 = 1.0
    var 잡음 = 0.0
    var 넘은회 = -1
    var 실력준비 = false
    fun 실력rm(): Double { val p = 플랜()!!; return if (p.횟수진행) (p.표!!.유효부하(d.몸.체중)) * (1 + A / 30.0) else A }
    fun 능력한번(): Boolean {
        val p = 플랜() ?: return false
        if (!실력준비) {
            실력준비 = true
            val (f, 배, 노) = when (c.모습) { 모습.느림 -> Triple(0.3, 1.0, 0.0); 모습.빠름 -> Triple(2.5, 1.0, 0.0); 모습.컨디션 -> Triple(1.0, 1.0, 0.06); 모습.과대 -> Triple(1.0, 0.82, 0.0); 모습.과소 -> Triple(1.0, 1.2, 0.0); else -> Triple(1.0, 1.0, 0.04) }
            속도배 = f; 잡음 = 노
            A = p.시작진행값 * 배
        }
        val 목 = if (p.횟수진행) p.목표개수 else p.목표1RM
        if (넘은회 < 0 && A >= 목) 넘은회 = r.회수
        val 오늘 = A * (1 + (rnd.nextDouble() * 2 - 1) * 잡음)
        val 정부하 = p.표?.유효부하(d.몸.체중) ?: 0.0
        val 오늘rm = if (p.횟수진행) 정부하 * (1 + 오늘 / 30.0) else 오늘
        val ok = 한번 { g, k, 예 ->
            val 가능 = 30.0 * (오늘rm / 예.w - 1)
            if (가능 < 1.0) {
                // 너무 무겁다 → 가볍게 해서 처방 횟수만큼
                세트(max(1.0, ((오늘rm / (1 + 예.r / 30.0)) * 10).toInt() / 10.0), 예.r, 예.종류)
            } else {
                val 끝세트 = g.측정일 && k == 현재마지막
                val 한계 = if (끝세트) (if (p.횟수진행) 200.0 else 12.0) else 예.r.toDouble()
                세트(예.w, max(1, min(한계, 가능).toInt()), 예.종류)
            }
        }
        if (ok) {
            val q = 플랜() ?: return true
            val 후 = q.회표(d.몸, d.향상기록들)
            val 비 = if (후.size >= 2) 후[1].목표값 / 후[0].목표값 - 1 else 0.004
            A *= 1 + 속도배 * max(0.0, 비)
        }
        return ok
    }

    val 처음표 = ArrayList<Double>()
    val 실행목표 = ArrayList<Double>()
    fun 정상한번() {
        val p = 플랜()!!
        val g = p.다음회(d.몸, d.향상기록들)!!
        실행목표.add(g.목표값)
        val 전 = p
        val ok = 한번(::정확)
        val 후 = 플랜()!!
        if (ok) {
            if (후.재기준 != null) 기록("정확히했는데재기준생김", "회${g.회} 재기준=${후.재기준} 목표=${g.목표값}")
            if (g.측정일) {
                val m = 후.측정들.last()
                val 비 = 후.지금1RM / g.목표값
                val 맨몸 = p.횟수진행
                // 정확히 했으면 측정값은 그 회 목표와 (반올림 한도 안에서) 같아야 한다
                val 한도 = if (맨몸) max(0.5, g.목표값 * 0.0) else g.목표값 * 0.012
                val 값 = if (맨몸) m.횟수.toDouble() else m.환산1RM
                if (abs(값 - g.목표값) > 한도 + 0.5 * (if (맨몸) 1.0 else 0.0) + 1e-6) 기록("정확히했는데측정이목표와다름", "회${g.회} 목표=${"%.2f".format(g.목표값)} 측정=${"%.2f".format(값)} ($m)")
                if (맨몸) { val 깎임 = g.목표값 - 값; r.경고.add("깎임:${"%.2f".format(깎임)}") }
            }
        }
    }

    // ── 다른 종목과 섞어서 ──
    fun 혼합한번() {
        // 같은 날 다른 루틴(같은 종목 이름의 보통 줄)도 한다
        val 전 = d
        if (rnd.nextInt(3) == 0) {
            val rx = d.루틴("rx")!!
            val S = 운동시작(rx, 지금) ?: return
            var s2 = S
            for (j in s2.종목들.indices) for (k in 0 until s2.종목들[j].세트) s2 = s2.체크(j, k, 지금 + (j * 10 + k) * 1000L)
            d = d.copy(세션 = s2).운동저장하기(날글(), 지금 + 1_800_000L).copy(세션 = null)
            val a = 전.플랜찾기("p1")!!; val b = d.플랜찾기("p1")!!
            if (a.한회 != b.한회 || a.측정들 != b.측정들 || a.재기준 != b.재기준) 기록("다른루틴이플랜을올림", "한회 ${a.한회} → ${b.한회}")
            로그.add("다른루틴")
            d.루틴들.forEach { rr -> rr.종목.forEach { e -> if (e.플랜id != null && d.플랜줄채움(e) != e) 기록("다른루틴뒤플랜줄이처방과다름", rr.이름) } }
            틱일(0)
        }
        // 플랜 줄을 안 하고 다른 것만 하는 날
        if (rnd.nextInt(5) == 0) {
            val 루 = d.루틴("rp")!!
            var S = 운동시작(루, 지금) ?: return
            val j플 = S.종목들.indexOfFirst { it.플랜id == "p1" }
            for (j in S.종목들.indices) if (j != j플) for (k in 0 until S.종목들[j].세트) S = S.체크(j, k, 지금 + (j * 10 + k) * 1000L)
            val a = d.플랜찾기("p1")!!
            d = d.copy(세션 = S).운동저장하기(날글(), 지금 + 1_800_000L).copy(세션 = null)
            val b = d.플랜찾기("p1")!!
            if (a.한회 != b.한회) 기록("플랜줄안했는데회가오름", "한회 ${a.한회} → ${b.한회}")
            로그.add("플랜줄빼고")
            return
        }
        // 같은 날 플랜 날을 두 번 한다
        능력한번()
        if (rnd.nextInt(6) == 0) { 로그.add("같은날또"); 능력한번() }
    }

    // ── 뒤죽박죽 ──
    var 지움띠 = ArrayList<지운플랜>()
    fun 뒤죽박죽한번() {
        val n = rnd.nextInt(100)
        val p = 플랜()
        when {
            p != null && p.다음회(d.몸, d.향상기록들) == null -> {
                val (x, 오류) = d.플랜고치기("p1", p.고침값().copy(목표무게 = p.목표무게 * 1.25 + 1, 목표개수 = p.목표개수 * 1.3 + 1))
                d = x; 로그.add("끝나서목표올림" + (오류?.let { "(막힘)" } ?: ""))
            }
            p == null -> { if (지움띠.isNotEmpty()) { d = d.플랜되살리기(지움띠.removeLast()); 로그.add("되살림") } else throw 막힘("플랜이 없고 되살릴 것도 없다") }
            n < 45 -> 한번(listOf(::정확, ::들쭉, ::미달, ::상회)[rnd.nextInt(4)])
            n < 52 -> { // 중간에 그만둠: 체크 없이 끝냄 / 일부만
                val S = d.조절해시작(d.루틴("rp")!!, 날글(), 지금) ?: return
                var s2 = S
                val j = s2.종목들.indexOfFirst { it.플랜id == "p1" }
                val 몇 = rnd.nextInt(0, max(1, s2.종목들[j].세트))
                for (k in 0 until 몇) s2 = s2.체크(j, k, 지금 + k * 1000L)
                val a = d.플랜찾기("p1")!!
                d = d.copy(세션 = s2).운동저장하기(날글(), 지금 + 1_000_000L).copy(세션 = null)
                val b = d.플랜찾기("p1")!!
                if (몇 == 0 && s2.종목들.none { it.찬것().isNotEmpty() } && a != b) 기록("한세트도안했는데플랜이바뀜", "$a → $b")
                로그.add("중간그만(${몇}세트)")
            }
            n < 58 -> { // 목표 올리기 / 내리기
                val 새 = p.고침값().copy(목표무게 = max(1.0, p.목표무게 * (if (rnd.nextBoolean()) 1.1 else 0.95)), 목표개수 = max(2.0, p.목표개수 * (if (rnd.nextBoolean()) 1.2 else 0.9)))
                val (x, 오류) = d.플랜고치기("p1", 새)
                d = x; 로그.add("목표바꿈" + (오류?.let { "(막힘)" } ?: ""))
                if (오류 == null) 줄검사("목표바꿈")
            }
            n < 64 && !p.횟수진행 -> { // 방식 바꿈
                val 번호들 = 훈련방식들.map { it.번호 }
                val 새 = p.고침값().copy(방식번호 = 번호들[rnd.nextInt(번호들.size)], 강도 = rnd.nextInt(3), 세트수 = 0, 직접횟수 = 0)
                val (x, 오류) = d.플랜고치기("p1", 새); d = x; 로그.add("방식→${새.방식번호}" + (오류?.let { "(막힘)" } ?: ""))
                if (오류 == null) 줄검사("방식바꿈")
            }
            n < 69 -> { // 지금 실력 다시 넣기
                val 원 = p.지금진행값
                val 배 = 0.7 + rnd.nextDouble() * 0.7
                val x = if (p.횟수진행) d.현재수행넣기("p1", 날글(), 0.0, max(1.0, 원 * 배)) else { val 새rm = 원 * 배; val r = 1 + rnd.nextInt(8); d.현재수행넣기("p1", 날글(), 새rm / (1 + r / 30.0), r.toDouble()) }
                if (x != null) { d = x; 로그.add("실력넣기×${"%.2f".format(배)}"); 줄검사("실력넣기") } else 로그.add("실력넣기(막힘)")
            }
            n < 75 -> { val x = d.플랜지우기("p1"); if (x != null) { d = x.first; 지움띠.add(x.second); 로그.add("플랜지움") } }
            n < 80 -> { if (지움띠.isNotEmpty()) { d = d.플랜되살리기(지움띠.removeLast()); 로그.add("플랜되살림"); 줄검사("되살림") } }
            n < 86 -> { // 보고서 열었다 닫았다 (저장 → 되감기 → 다시 저장)
                val S = d.조절해시작(d.루틴("rp")!!, 날글(), 지금) ?: return
                var s2 = S
                for (j in s2.종목들.indices) for (k in 0 until s2.종목들[j].세트) if (rnd.nextInt(4) != 0) s2 = s2.체크(j, k, 지금 + (j * 10 + k) * 1000L)
                var q = d.copy(세션 = s2.끝냄(지금 + 4_000_000L))
                val 전회 = q.플랜찾기("p1")?.한회 ?: 0
                val 횟 = 1 + rnd.nextInt(3)
                var 고친 = false; var 고친목표 = 0.0; var 고친방식 = 0
                repeat(횟) {
                    q = q.보고저장(날글(), 지금 + 4_100_000L).first
                    if (!고친 && rnd.nextInt(3) == 0 && q.플랜찾기("p1") != null) {   // 보고서 열어 둔 채 / 운동으로 돌아가 플랜을 고친다
                        val pp = q.플랜찾기("p1")!!
                        val 새 = pp.고침값().copy(목표무게 = pp.목표무게 * 1.07, 목표개수 = pp.목표개수 * 1.07, 방식번호 = if (pp.횟수진행) 1 else 훈련방식들[rnd.nextInt(훈련방식들.size)].번호)
                        val (x, 오류) = q.플랜고치기("p1", 새)
                        if (오류 == null) { q = x; 고친 = true; 고친목표 = x.플랜찾기("p1")!!.목표무게 + x.플랜찾기("p1")!!.목표개수; 고친방식 = x.플랜찾기("p1")!!.방식번호 }
                    }
                    if (rnd.nextBoolean()) q = q.copy(세션 = q.세션?.재개(지금 + 4_200_000L)?.끝냄(지금 + 4_300_000L + it))   // 다시 운동으로 갔다 끝냄
                }
                q = q.보고끝(날글(), 지금 + 4_500_000L)
                if (고친) q.플랜찾기("p1")?.let { z -> if (z.목표무게 + z.목표개수 != 고친목표 || z.방식번호 != 고친방식) 기록("보고서다시열면고친플랜이되돌아감", "고친 목표합=$고친목표 방식=$고친방식 → 지금 ${z.목표무게 + z.목표개수} 방식=${z.방식번호}") }
                val 후회 = q.플랜찾기("p1")?.한회 ?: 0
                val 한것 = s2.종목들.any { it.플랜id == "p1" && it.찬것().any { x -> x.종류 != 세트종류.워밍업 && x.r > 0 } }
                if (후회 != 전회 + (if (한것) 1 else 0)) 기록("보고서여러번열면회가이상", "한회 $전회 → $후회 (${횟}번 · 플랜줄체크=$한것)")
                d = q.copy(세션 = null); 로그.add("보고서${횟}번")
                줄검사("보고서")
            }
            n < 90 -> { // 그날만 조절로 가볍게 / 무겁게 하는 날
                val 조절 = 오늘조절(볼륨 = 70 + rnd.nextInt(0, 60))
                d = d.copy(조절 = d.조절 + (날글() to 조절)); 로그.add("조절${조절.볼륨}")
                한번(::정확)
            }
            n < 94 -> { // 기록 지우기 (플랜 상태는 그대로 — 기록만 사라진다)
                val 키 = d.기록.keys.sorted().lastOrNull()
                if (키 != null) { d = d.copy(기록 = d.기록 - 키); 로그.add("기록지움") }
            }
            else -> { // 몸무게 / 나이 바뀜
                d = d.copy(몸 = d.몸.copy(체중 = d.몸.체중 + rnd.nextInt(-3, 4))); 로그.add("체중바뀜")   // 화면은 늘 플랜줄채움으로 새로 계산해 보여 준다 → 저장된 줄이 낡아도 괜찮다
            }
        }
        d.플랜찾기("p1")?.let { q -> if (q.한회 < 0) 기록("한회음수", "${q.한회}") }
    }

    fun 줄검사(무엇: String) {
        val 후 = d
        후.루틴들.forEach { rr -> rr.종목.forEach { e -> if (e.플랜id != null && 후.플랜줄채움(e) != e) 기록("${무엇}뒤플랜줄이처방과다름", "${rr.이름} ${e.이름} ${e.세트값.map { it.w to it.r }} ← ${후.플랜줄채움(e).세트값.map { it.w to it.r }}") } }
        후.플랜들.forEach { p -> val g = p.다음회(후.몸, 후.향상기록들); if (g != null) { val 처 = 회처방(p, g.목표값, 후.설정.무게폭, 후.몸, g.측정일, g.주); if (처.isEmpty()) 기록("${무엇}뒤처방비었음", p.이름) } }
    }
}

internal fun 하나(c: 사례, 위반: MutableMap<String, MutableList<String>>): 결과값? = try {
    P30(c, 위반).달리기()
} catch (e: P30.막힘) {
    위반.getOrPut("막힘") { ArrayList() }.let { if (it.size < 3) it.add("[${c.종목} 방식${c.방식} ${c.모습} 씨앗${c.씨앗}] ${e.message}") }; null
} catch (e: Throwable) {
    위반.getOrPut("예외:${e.javaClass.simpleName}") { ArrayList() }.let { if (it.size < 3) it.add("[${c.종목} 방식${c.방식} ${c.모습} 씨앗${c.씨앗}] ${e.message}\n" + e.stackTrace.take(6).joinToString("\n   ")) }; null
}

object P30Main {
    @JvmStatic fun main(a: Array<String>) {
        val 시작 = a.getOrNull(0)?.toIntOrNull() ?: 0
        val 수 = a.getOrNull(1)?.toIntOrNull() ?: 5
        val 모음 = if (a.size > 2) a.drop(2).map { 모습.valueOf(it) } else 모습.values().toList()
        val 위반 = LinkedHashMap<String, MutableList<String>>()
        val 방식들 = 훈련방식들.map { it.번호 }
        var 사례수 = 0; var 정체 = 0; var 끝 = 0
        val 요약 = LinkedHashMap<String, MutableList<결과값>>()
        for (모 in 모음) for (t in 플랜표.종목들) {
            val 방식 = if (t.맨몸인가) listOf(1) else 방식들
            for (b in 방식) for (s in 시작 until 시작 + 수) {
                val x = 하나(사례(t.이름, b, 모, s), 위반) ?: continue
                사례수++; if (x.정체) 정체++ else 끝++
                요약.getOrPut("${모}") { ArrayList() }.add(x)
            }
        }
        println("=== 사례 ${사례수}개 · 끝까지 ${끝} · 정체(한계 넘김) ${정체} ===")
        for ((k, l) in 요약) {
            val 비 = l.map { it.회수.toDouble() / max(1, it.처음회표) }
            println("$k: 사례 ${l.size} · 정체 ${l.count { it.정체 }} · 회수/처음회표 평균 ${"%.2f".format(비.average())} 최소 ${"%.2f".format(비.min())} 최대 ${"%.2f".format(비.max())} · 실력이 목표를 넘은 뒤 플랜이 더 간 회수: 평균 ${"%.1f".format(l.map { it.늦음 }.average())} 최대 ${l.maxOf { it.늦음 }}")
        }
        if (System.getenv("이상") != null) 요약.forEach { (k, l) -> l.filter { x -> val b = x.회수.toDouble() / max(1, x.처음회표); b > 1.25 || b < 0.8 }.sortedByDescending { it.정체 }.take(8).forEach { x -> println("  [이상비율] $k ${x.사례.종목} 방식${x.사례.방식} 씨앗${x.사례.씨앗}: 회수 ${x.회수} / 처음회표 ${x.처음회표} 정체=${x.정체} 처음목표=${"%.1f".format(x.처음목표)} 마지막진행=${"%.1f".format(x.마지막진행)} 측정 ${x.측정수}") } }
        println()
        if (위반.isEmpty()) println("위반 없음")
        for ((k, l) in 위반.toSortedMap()) { println("■ $k (${l.size}+)"); l.forEach { println("  - $it") } }
    }
}
