package com.slayde.hasenheide.ui

import com.slayde.hasenheide.data.*
import kotlin.random.Random
import kotlin.math.max

/** 무작위 조작 검사기 — 운동 중 조작을 마구 섞어 불변식이 깨지는 순간을 잡는다 */
internal class Fz(val rnd: Random, val 위반: MutableMap<String, MutableList<String>>, val 무편집: Boolean = false, val 플랜모드: Boolean = false) {
    var d: 앱데이터 = 앱데이터()
    var 본 = 0
    var 오늘 = "2026-10-10"
    var 월모드 = false
    var 딥스번호 = 0
    var 지금 = 1_000_000_000_000L
    val 지움 = ArrayList<지운세트>()
    val 뺌 = ArrayList<뺀종목>()
    var 로그 = ArrayList<String>()
    val 시작루틴: 루틴
    var 원본루틴: 루틴

    init {
        val 종목들 = listOf(
            루틴종목("벤치", 세트 = 3 + rnd.nextInt(3), 무게 = 60.0, 횟수 = 10, 휴식 = 90,
                세트값 = if (rnd.nextBoolean()) List(5) { 세트(60.0 + it * 2.5, 10) } else emptyList()),
            루틴종목("인클라인", 세트 = 3, 무게 = 40.0, 횟수 = 10, 휴식 = 60),
            루틴종목("컬", 세트 = 3, 무게 = 12.0, 횟수 = 12, 휴식 = 45, 슈퍼 = if (rnd.nextBoolean()) "A" else null),
            루틴종목("해머", 세트 = 3, 무게 = 10.0, 횟수 = 12, 휴식 = 45, 슈퍼 = if (rnd.nextBoolean()) "A" else null),
        ).let { l -> l.map { e -> if (e.세트값.isNotEmpty() && e.세트값.size != e.세트) e.copy(세트값 = e.세트값.take(e.세트)) else e } }
        // 슈퍼 둘이 하나만 켜지면 외톨이 → 묶음정리
        val 플랜 = 플랜(id = "p1", 이름 = "벤치프레스", 종목 = "벤치프레스", 시작1RM = 80.0, 목표무게 = 100.0, 목표횟수 = 5, 만든날 = "2026-09-01")
        var 루 = 루틴("r1", "가슴", 종목 = 종목들).묶음정리()
        d = 앱데이터(루틴들 = listOf(루), 플랜들 = if (플랜모드) listOf(플랜) else emptyList(), 몸 = 몸조건(35, true, 80.0))
        if (플랜모드) {
            루 = 루.copy(종목 = listOf(d.플랜줄채움(루틴종목("벤치프레스", 플랜id = "p1"))) + 루.종목.drop(1))
            d = d.copy(루틴들 = listOf(루))
        }
        시작루틴 = 루
        원본루틴 = 시작루틴
        val s = 운동시작(시작루틴, 지금)!!
        d = d.copy(세션 = s)
        // 측정일 워밍업처럼 앞 두 칸이 워밍업인 상황도 섞는다
        if (rnd.nextInt(4) == 0) {
            val w = d.세션!!.종목들[0]
            val 웜 = listOf(세트(24.0, 5, 세트종류.워밍업), 세트(36.0, 3, 세트종류.워밍업))
            val 새예정 = 웜 + (0 until w.세트).map { w.예정값.칸(it) ?: 세트(w.무게, w.횟수) }
            val 새e = w.copy(세트 = 새예정.size, 계획세트 = 새예정.size, 예정값 = 새예정, 휴식들 = List(웜.size) { 90 } + w.휴식들, 무게 = 웜[0].w, 횟수 = 웜[0].r, 원번호 = List(웜.size) { null } + w.원번호)
            d = d.copy(세션 = d.세션!!.copy(종목들 = listOf(새e) + d.세션!!.종목들.drop(1), 무게 = 웜[0].w, 횟수 = 웜[0].r))
        }
    }

    val S: 운동세션? get() = d.세션

    fun 같음(a: 운동세션, b: 운동세션) = a.copy(s = 0, 무게 = 0.0, 횟수 = 0, 마지막 = 0) == b.copy(s = 0, 무게 = 0.0, 횟수 = 0, 마지막 = 0)
    fun 요약(): String = 요약세션(S)
    fun 요약세션(s: 운동세션?): String {
        if (s == null) return "세션없음"
        return "i=${s.i} s=${s.s} 본=$본 끝화면=${s.끝화면} 휴식=${s.휴식?.let { "종목${it.종목} k${it.k} 다음(${it.다음i},${it.다음s})" }} | " +
            s.종목들.mapIndexed { j, e -> "[$j ${e.이름}${if (e.임시) "*" else ""} 세트${e.세트}/계획${e.계획세트} 기록${e.기록.map { if (it == null) "." else "X" }.joinToString("")} 예정${e.예정값.size} 휴${e.휴식들.size}${if (e.마감) " 마감" else ""}${e.슈퍼?.let { " 슈" + it } ?: ""}]" }.joinToString(" ")
    }

    fun 위반기록(이름: String, 설명: String) {
        위반.getOrPut(이름) { ArrayList() }.let { if (it.isEmpty() || (it.size < 3 && 로그.size < it.minOf { x -> x.count { c -> c == '→' } } + 2)) { it.add(설명 + "\n   상태: " + 요약() + "\n   조작: " + 로그.takeLast(60).joinToString(" → ")); it.sortBy { x -> x.count { c -> c == '→' } } } }
    }

    fun 플랜검사() {
        if (d.기록.size > 1) 위반기록("기록이둘", "기록 ${d.기록.size}개")
        if (!플랜모드) return
        val p = d.플랜들.firstOrNull() ?: return
        val 플랜세트 = d.기록.values.lastOrNull()?.종목들?.filter { it.플랜id == "p1" }?.sumOf { x -> x.세트들.count { it.종류 != 세트종류.워밍업 && it.r > 0 } } ?: 0
        val 기대 = if (d.기록.isNotEmpty() && 플랜세트 > 0) 1 else 0
        if (p.한회 != 기대) 위반기록("플랜회차어긋남", "한회=${p.한회} 기대=$기대 기록=${d.기록.size} 플랜세트=$플랜세트 향상=${d.향상기록들.size}")
        if (p.측정들.size > 1 || d.향상기록들.size > 1) 위반기록("측정이쌓임", "측정=${p.측정들.size} 향상=${d.향상기록들.size}")
        d.루틴들.forEach { r -> r.종목.forEach { e -> if (e.플랜id != null && d.플랜줄채움(e) != e) 위반기록("루틴플랜줄이처방과다름", "${e.이름} 세트값=${e.세트값.map { it.w to it.r }} 처방=${d.플랜줄채움(e).세트값.map { it.w to it.r }}") } }
    }

    fun 검사(막조작: String) {
        if (!월모드) 플랜검사()
        val s = S ?: return
        val n = s.종목들.size
        fun 나쁨(이름: String, 글: String) = 위반기록(이름, 글)
        if (n == 0) { 나쁨("종목0", "종목이 없다"); return }
        if (s.i !in 0 until n) { 나쁨("i범위", "i=${s.i} n=$n"); return }
        s.종목들.forEachIndexed { j, e ->
            if (e.세트 < 1) 나쁨("세트<1", "j=$j 세트=${e.세트}")
            if (e.기록.size > e.세트) 나쁨("기록>세트", "j=$j 세트=${e.세트} 기록=${e.기록.size}")
            if (e.예정값.size > e.총칸() + 0 && e.예정값.drop(e.총칸()).any { it != null }) 나쁨("예정값>총칸", "j=$j 예정=${e.예정값.size} 총칸=${e.총칸()}")
            if (e.휴식들.size > e.총칸() && e.휴식들.drop(e.총칸()).any { it != null }) 나쁨("휴식들>총칸", "j=$j")
            if (e.계획세트 > e.총칸() && !e.임시) 나쁨("계획>총칸", "j=$j 계획=${e.계획세트} 총칸=${e.총칸()}")
        }
        val e = s.지금종목
        if (!s.끝화면) {
            if (s.s !in 0..e.총칸()) 나쁨("s범위", "i=${s.i} s=${s.s} 총칸=${e.총칸()}")

        }
        s.휴식?.let { h ->
            val hj = if (h.종목 >= 0) h.종목 else s.i
            if (hj !in 0 until n) 나쁨("휴식종목범위", "h.종목=${h.종목} n=$n")
            else {
                val he = s.종목들[hj]
                if (h.k !in 0 until he.총칸()) 나쁨("휴식k범위", "h.k=${h.k} 총칸=${he.총칸()}")
                else if (he.기록.칸(h.k) == null && !h.물음) 나쁨("휴식줄이빈칸", "휴식이 안 한 줄 위에 있다 hj=$hj k=${h.k} 기록=${he.기록.map { it != null }}")
            }
            h.다음i?.let { ni -> if (ni !in 0 until n) 나쁨("다음i범위", "다음i=$ni n=$n") else h.다음s?.let { ns -> if (ns > s.종목들[ni].총칸()) 나쁨("다음s범위", "다음s=$ns 총칸=${s.종목들[ni].총칸()}") } }
        }
        if (본 !in 0 until n) 나쁨("본범위", "본=$본 n=$n")
        // 마무리 아닌데 모든 종목이 끝났고 휴식도 없다 → 화면이 멈춘다


    }

    fun 저장검사(전: 앱데이터, 후: 앱데이터, 라벨: String) {
        if (무편집 && !플랜모드 && 후.기록.size > 전.기록.size) {
            val 후r = 후.루틴들.firstOrNull { it.id == 원본루틴.id }
            if (후r != null) 원본루틴.종목.forEachIndexed { x, o ->
                val n = 후r.종목.firstOrNull { it.이름 == o.이름 } ?: return@forEachIndexed
                val 다름 = n.세트 != o.세트 || (0 until o.세트).any { k -> n.목표(k) != o.목표(k) || n.휴식(k) != o.휴식(k) }
                if (다름) 위반기록("무편집인데루틴이바뀜", "$라벨 ${o.이름} 원=${(0 until o.세트).map { o.목표(it).w }} 후=${(0 until n.세트).map { n.목표(it).w }}")
            }
        }
        if (후.기록.size > 전.기록.size) {
            val 새키 = (후.기록.keys - 전.기록.keys).firstOrNull() ?: 후.기록.keys.last()
            val 새 = 후.기록[새키]!!
            val 찬수 = 전.세션?.종목들?.sumOf { it.찬것().size } ?: 0
            if (새.종목들.sumOf { it.세트들.size } != 찬수) 위반기록("저장세트수불일치", "$라벨 기록=${새.종목들.sumOf { it.세트들.size }} 세션찬=$찬수")
            val 세 = 전.세션
            if (세 != null && 새.달성) {
                val 못한 = 세.정식().filter { e -> e.찬것().count { it.종류 != 세트종류.워밍업 } < (e.계획세트 - e.웜칸수()).coerceAtLeast(0) }
                if (못한.isNotEmpty()) 위반기록("달성인데모자란종목", "$라벨 ${못한.map { it.이름 + "계획" + it.계획세트 + "/한" + it.찬것().size }}")
            }
        }
        // 루틴 안 줄의 세트값/휴식값 길이
        for (r in 후.루틴들) for (e in r.종목) {
            if (e.세트값.isNotEmpty() && e.세트값.size != e.세트) 위반기록("루틴세트값길이", "$라벨 ${e.이름} 세트=${e.세트} 세트값=${e.세트값.size}")
            if (e.휴식값.isNotEmpty() && e.휴식값.size != e.세트) 위반기록("루틴휴식값길이", "$라벨 ${e.이름} 세트=${e.세트} 휴식값=${e.휴식값.size}")
            if (e.세트 < 1) 위반기록("루틴세트<1", "$라벨 ${e.이름}")
        }
    }

    fun 조작(): String {
        val s = S ?: return "없음"
        val n = s.종목들.size
        val j = rnd.nextInt(n)
        val 칸 = s.종목들[j].총칸()
        val k = rnd.nextInt(max(1, 칸))
        지금 += rnd.nextInt(1, 40_000).toLong()
        if (rnd.nextInt(8) == 0) { 지움.clear() }
        if (rnd.nextInt(8) == 0) { 뺌.clear() }
        fun 함(이름: String, f: (운동세션) -> 운결과?) {
            로그.add(이름)
            val r = f(s) ?: return
            d = d.copy(세션 = r.세션.copy(마지막 = 지금))
            본 = r.본.coerceIn(0, max(0, r.세션.종목들.size - 1))
        }
        fun 바(이름: String, f: (운동세션) -> 운동세션) = 함(이름) { 운결과(f(it), 본) }
        if (s.끝화면) {
            return when (rnd.nextInt(5)) {
                0, 1 -> { 로그.add("재개"); d = d.copy(세션 = s.재개(지금)); "재개" }
                2 -> { 로그.add("보고저장"); val (x, _) = d.보고저장(오늘, 지금); 저장검사(d, x, "보고저장"); d = x; "보고저장" }
                3 -> { 로그.add("보고끝"); val x = d.보고끝(오늘, 지금); 저장검사(d, x, "보고끝"); d = x; "보고끝" }
                else -> { 로그.add("보고저장2"); val (x, _) = d.보고저장(오늘, 지금); d = x; "보고저장" }
            }
        }
        return when (rnd.nextInt(100)) {
            in 0..19 -> { 함("체크($j,$k)") { x -> 운체크(x, j, k, 지금).let { 운결과(it, 본따라감(it, 본)) } }; "체크" }
            in 20..27 -> { 함("주누름") { x -> 운주누름(x, 본, 지금) }; "주" }
            in 28..32 -> { 함("휴식건너뛰기") { x -> 휴식건너뛰기(x, 본, 지금) }; "휴식건너뛰기" }
            in 33..40 -> { 바("세트추가($j)") { it.세트추가(j) }; "세트추가" }
            in 41..50 -> {
                로그.add("세트지우기($j,$k)")
                val r = 운세트지우기(s, j, k)
                if (r != null) {
                    val 복 = 세트되살리기(r.first, r.second, 지금)
                    if (s.휴식 == null && !같음(복, s)) 위반기록("지우고바로되살리면달라짐", "(j=$j,k=$k)\n   전: ${요약()}\n   복: ${요약세션(복)}")
                    d = d.copy(세션 = r.first.copy(마지막 = 지금)); 지움.add(r.second)
                }
                "세트지우기"
            }
            in 51..56 -> {
                if (지움.isNotEmpty()) {
                    val z = 지움.removeAt(지움.size - 1)
                    로그.add("세트되살리기"); d = d.copy(세션 = 세트되살리기(s, z, 지금))
                }
                "세트되살리기"
            }
            in 57..62 -> if (무편집) { "건너뜀" } else { 바("값고치기($j,$k)") { it.값고치기(j, k, 새무게 = if (rnd.nextBoolean()) rnd.nextInt(0, 200) / 2.0 else null, 새횟수 = if (rnd.nextBoolean()) rnd.nextInt(0, 30) else null) }; "값고치기" }
            in 63..65 -> if (무편집) { "건너뜀" } else { 바("휴식고치기($j,$k)") { it.휴식고치기(j, k, rnd.nextInt(0, 300)) }; "휴식고치기" }
            in 66..69 -> {
                로그.add("종목빼기($j)")
                val r = s.종목빼기(j, 본)
                if (r != null) {
                    val 복 = s.종목빼기(j, 본)!!.let { (a, z) -> a.세션.종목되돌리기(z, a.본, 지금).세션 }
                    if (s.휴식 == null && !같음(복, s)) 위반기록("종목빼고바로되돌리면달라짐", "(j=$j)\n   전: ${요약()}\n   복: ${요약세션(복)}")
                    d = d.copy(세션 = r.first.세션.copy(마지막 = 지금)); 본 = r.first.본 ?: 본; 뺌.add(r.second) }
                "종목빼기"
            }
            in 70..72 -> {
                if (뺌.isNotEmpty()) {
                    val z = 뺌.removeAt(뺌.size - 1)
                    로그.add("종목되돌리기"); val r = s.종목되돌리기(z, 본, 지금); d = d.copy(세션 = r.세션); 본 = r.본 ?: 본
                }
                "종목되돌리기"
            }
            in 73..76 -> {
                로그.add("종목넣기")
                val e = 세션종목("딥스${++딥스번호}", 3, 0, 20.0, 10, 60, 휴식들 = List(3) { 60 }, 임시 = true)
                val r = s.종목넣기(e, 본); d = d.copy(세션 = r.세션); 본 = r.본 ?: 본; "종목넣기"
            }
            in 77..78 -> {
                로그.add("넣은것빼기")
                val r = s.넣은것빼기(본) { it.임시 }
                if (r != null) { d = d.copy(세션 = r.세션); 본 = r.본 ?: 본 }
                "넣은것빼기"
            }
            in 79..81 -> {
                로그.add("종목옮기기")
                val r = s.종목옮기기(rnd.nextInt(n), rnd.nextInt(n), rnd.nextBoolean(), 본); d = d.copy(세션 = r.세션); 본 = r.본 ?: 본; "종목옮기기"
            }
            in 82..84 -> { 함("다음종목가기") { x -> 다음종목가기(x, 본, 지금) }; "다음종목가기" }
            in 85..86 -> { 바("종목으로($j)") { it.종목으로(j) }; "종목으로" }
            in 87..88 -> { 로그.add("본바꿈"); 본 = rnd.nextInt(n); "본바꿈" }
            in 89..90 -> { 바("끝냄") { it.끝냄(지금) }; "끝냄" }
            in 91..92 -> { 바("여기까지") { it.다음종목으로(true, 지금) }; "여기까지" }
            in 93..95 -> { 함("휴식끝") { x -> x.휴식끝(설정값(), 지금).let { y -> 운결과(y, 본) } }; "휴식끝" }
            in 96..97 -> { 바("휴식조절") { it.휴식조절(rnd.nextInt(-30, 60), 지금) }; "휴식조절" }
            else -> {
                // 3시간 방치 → 자동 끝
                if (rnd.nextInt(6) == 0) {
                    로그.add("방치3시간")
                    지금 += 3 * 60 * 60 * 1000L + 1
                    val x = d.오래된운동정리(지금); 저장검사(d, x, "방치"); d = x
                }
                "방치"
            }
        }
    }
}

fun main(args: Array<String>) {
    val 판수 = args.getOrNull(0)?.toInt() ?: 5000
    val 길이 = args.getOrNull(1)?.toInt() ?: 80
    val 위반 = LinkedHashMap<String, MutableList<String>>()
    var 죽음 = 0
    for (seed in 0 until 판수) {
        val f = Fz(Random(seed), 위반, 무편집 = seed % 2 == 0, 플랜모드 = seed % 3 == 1)
        try {
            for (t in 0 until 길이) {
                if (f.S == null) { f.플랜검사(); break }
                val 막 = f.조작()
                f.검사(막)
            }
        } catch (ex: Throwable) {
            죽음++
            val k = "예외:" + ex.javaClass.simpleName + ":" + (ex.stackTrace.firstOrNull { it.className.contains("hasenheide") }?.let { "${it.fileName}:${it.lineNumber}" } ?: "")
            위반.getOrPut(k) { ArrayList() }.let { if (it.size < 3) it.add(ex.message + "\n   조작: " + f.로그.takeLast(25).joinToString(" → ")) }
        }
    }
    println("판수=$판수 예외종료=$죽음")
    for ((k, v) in 위반) { println("## $k"); v.forEach { println("  - $it") } }
}
