package com.slayde.hasenheide.ui

import com.slayde.hasenheide.data.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.max
import kotlin.random.Random

/** 30일 추적 — 운동 시작 · 도중 종료 · 기록 지우기/되살리기 · 루틴 지우기 · 예정 바꾸기 · 자정 넘기기를 한 달 동안 마구 섞는다 */
internal class Mo(val rnd: Random, val 위반: MutableMap<String, MutableList<String>>, val 통계: MutableMap<String, Long>, val 플랜모드: Boolean = false) {
    val 존 = ZoneId.systemDefault()
    var 지금: Long = LocalDate.of(2026, 10, 1).atTime(8, 0).atZone(존).toInstant().toEpochMilli()
    var 앱오늘: String = 날글(지금)
    var 마지막틱 = 지금
    var 본 = 0
    var 번호 = 0
    val 로그 = ArrayList<String>()
    val 지움띠 = ArrayList<캘지움값>()
    class 루틴지움값(val r: 루틴, val i: Int, val 전: 앱데이터)
    val 루틴지움띠 = ArrayList<루틴지움값>()
    val 플랜지움띠 = ArrayList<지운플랜>()
    var d: 앱데이터
    val fz: Fz

    fun 날글(t: Long) = Instant.ofEpochMilli(t).atZone(존).toLocalDate().toString()
    fun 셈(k: String, n: Long = 1) { 통계[k] = (통계[k] ?: 0L) + n }

    init {
        fun 줄(이름: String, 세트: Int, 무게: Double, 횟수: Int, 휴식: Int, 슈퍼: String? = null) = 루틴종목(이름, 세트, 무게, 횟수, 휴식, 슈퍼)
        val r1 = 루틴("r1", "가슴", 종목 = listOf(줄("벤치", 4, 60.0, 10, 90), 줄("인클라인", 3, 40.0, 10, 60), 줄("컬", 3, 12.0, 12, 45, "A"), 줄("해머", 3, 10.0, 12, 45, "A")), 자동생성 = true)
        val r2 = 루틴("r2", "등", 종목 = listOf(줄("풀업", 3, 0.0, 8, 90), 줄("로우", 4, 50.0, 10, 90)), 자동생성 = true)
        val r3 = 루틴("r3", "하체", 종목 = listOf(줄("스쿼트", 5, 80.0, 5, 120), 줄("런지", 3, 20.0, 10, 60)), 자동생성 = true)
        val r4 = 루틴("r4", "휴식", 휴식일 = true, 자동생성 = true)
        val r5 = 루틴("r5", "팔", 종목 = listOf(줄("컬2", 3, 10.0, 12, 45)), 자동생성 = false)
        val 설 = 설정값(볼륨켬 = rnd.nextBoolean(), 볼륨언제 = if (rnd.nextBoolean()) "항상" else "성공", 볼륨배분 = if (rnd.nextBoolean()) "무게" else "횟수")
        d = 앱데이터(루틴들 = listOf(r1, r2, r3, r4, r5), 몸 = 몸조건(35, true, 80.0), 설정 = 설).예정초기화(앱오늘)
        if (플랜모드) {
            val 플 = 플랜(id = "p1", 이름 = "벤치프레스", 종목 = "벤치프레스", 시작1RM = 80.0, 목표무게 = 100.0, 목표횟수 = 5, 만든날 = "2026-09-01")
            var dd = d.copy(플랜들 = listOf(플))
            val 줄 = dd.플랜줄채움(루틴종목("벤치프레스", 플랜id = "p1"))
            dd = dd.copy(루틴들 = dd.루틴들.map { r -> if (r.id == "r1") r.copy(종목 = listOf(줄) + r.종목.drop(1)) else r })
            d = dd
        }
        fz = Fz(Random(rnd.nextInt()), 위반, false, false)
        fz.월모드 = true
        fz.로그 = 로그
    }

    // ─────────────── 기록 ───────────────
    fun 위반기록(이름: String, 설명: String) {
        val 목 = 위반.getOrPut(이름) { ArrayList() }
        val 글 = 설명 + "\n   조작(마지막 40): " + 로그.takeLast((System.getenv("LOGN") ?: "40").toInt()).joinToString(" → ") + "\n   [${로그.size}번째]"
        if (목.size < 3 || 로그.size < 목.maxOf { 끝번호(it) }) { 목.add(글); 목.sortBy { 끝번호(it) }; while (목.size > 3) 목.removeAt(목.size - 1) }
    }
    private fun 끝번호(s: String) = s.substringAfterLast('[').substringBefore('번').toIntOrNull() ?: Int.MAX_VALUE
    fun 기록들(x: 앱데이터) = x.기록.values.toList()
    fun 같은집합(a: List<날기록>, b: List<날기록>) = a.groupingBy { it }.eachCount() == b.groupingBy { it }.eachCount()
    fun 요약(): String = "앱오늘=$앱오늘 시계=${날글(지금)} 기록=${d.기록.keys.sorted()} 세션=${d.세션?.let { "루틴${it.루틴id} 끝화면${it.끝화면} 저장${it.저장?.열쇠}" } ?: "없음"} 예정=${d.예정.toSortedMap().entries.take(4).joinToString { it.key.takeLast(5) + ":" + it.value }}"

    // ─────────────── 하나씩 ───────────────
    fun 틱() {
        // 앱이 1분마다 하는 일 (날짜확인) — 화면 시계가 새로 맞춰진다
        val 실제 = 날글(지금)
        if (실제 != 앱오늘 || d.예정.isEmpty()) 앱오늘 = 실제
        val 전 = d
        d = d.오래된운동정리(지금).예정맞추기(앱오늘)
        마지막틱 = 지금
        if (전.세션 != null && d.세션 == null) 셈("자동종료")
        if (!(전.세션 != null)) 기록불변(전, d, "틱")
        else if (전.기록 != d.기록 && d.세션 != null) 위반기록("틱이기록을바꿈", "세션이 남아 있는데 틱이 기록을 바꿈 ${전.기록.keys} → ${d.기록.keys}")
        if (d.순번.isNotEmpty()) {
            val 지난 = d.예정.keys.filter { it < 앱오늘 }
            if (지난.isNotEmpty()) 위반기록("지난예정이남음", "앱오늘=$앱오늘 지난예정=$지난")
        }
    }

    fun 신선하게() { if (지금 - 마지막틱 >= 60_000) 틱() }
    /** 결과/마무리 화면에서 다른 탭을 누르면 앱이 먼저 보고끝(세션을 닫는다) — 캘린더·루틴 같은 화면은 그 뒤에만 열린다 */
    fun 탭떠남() {
        if (d.세션?.끝화면 == true) { d = d.보고끝(앱오늘, 지금); 로그.add("탭이동(보고끝)"); 셈("탭이동보고끝") }
        if (d.결과 != null) d = d.copy(결과 = null)
    }
    var 띠시각 = 0L
    fun 준비() {
        if (지금 - 띠시각 > 5000) { 지움띠.clear(); 루틴지움띠.clear(); 플랜지움띠.clear() }   // 되돌리기 띠는 5초
        신선하게(); 탭떠남()
    }

    fun 기록불변(전: 앱데이터, 후: 앱데이터, 무엇: String) {
        if (!같은집합(기록들(전), 기록들(후))) 위반기록("기록이이유없이바뀜", "$무엇: ${전.기록.keys} → ${후.기록.keys}")
    }

    fun 시간흐름() {
        val 이전 = 지금
        when (rnd.nextInt(8)) {
            0 -> 지금 += 5 * 60_000L
            1 -> 지금 += 30 * 60_000L
            2 -> 지금 += 2 * 3600_000L
            3 -> 지금 += 3 * 3600_000L + 1
            4 -> 지금 += 9 * 3600_000L
            5 -> 지금 += 24 * 3600_000L + rnd.nextInt(0, 12 * 3600_000)
            6 -> 지금 += 3 * 24 * 3600_000L   // 앱을 며칠 안 열었다
            else -> {
                val 자정 = LocalDate.parse(날글(지금)).plusDays(1).atStartOfDay(존).toInstant().toEpochMilli()
                지금 = max(지금 + 1, 자정 + rnd.nextInt(-90_000, 90_000))
            }
        }
        로그.add("시간+${(지금 - 이전) / 60000}분"); 셈("시간흐름")
        val 전 = d; 기록불변(전, d, "시간흐름")
    }

    fun 앱열기() {
        로그.add("앱열기"); 셈("앱열기")
        틱()
    }

    fun 운동시작() {
        신선하게()
        if (d.세션 != null) { 로그.add("시작불가(진행중)"); return }
        val 후보 = d.루틴들.filter { !it.휴식일 && it.종목.isNotEmpty() } + d.그날운동.filter { it.종목.isNotEmpty() }
        if (후보.isEmpty()) { 로그.add("시작불가(루틴없음)"); return }
        val 예 = d.예정루틴(앱오늘)?.takeIf { !it.휴식일 && it.종목.isNotEmpty() }
        val r = if (예 != null && rnd.nextInt(10) < 7) 예 else 후보[rnd.nextInt(후보.size)]
        val S = (if (rnd.nextInt(4) == 0) d.조절해시작(r, 앱오늘, 지금) else if (rnd.nextInt(12) == 0) 운동시작(d.한종목루틴(listOf("벤치", "스쿼트", "딥스")[rnd.nextInt(3)]), 지금) else 운동시작(r, 지금)) ?: return
        val 전 = d
        d = d.copy(세션 = S, 결과 = null); 본 = 0
        fz.지움.clear(); fz.뺌.clear()
        기록불변(전, d, "운동시작")
        로그.add("시작(${r.이름})"); 셈("운동시작")
    }

    fun 운동조작(최대: Int = 25) {
        if (d.세션 == null) { 운동시작(); if (d.세션 == null) return }
        val n = 1 + rnd.nextInt(최대)
        로그.add("운동조작x$n")
        fz.d = d; fz.지금 = 지금; fz.본 = 본; fz.오늘 = 앱오늘
        for (t in 0 until n) {
            fz.d = d; fz.지금 = 지금; fz.본 = 본; fz.오늘 = 앱오늘
            val 전기록 = d.기록.size
            val 전d2 = d
            val 막 = fz.조작()
            d = fz.d; 지금 = fz.지금; 본 = fz.본
            run {
                val 허용 = 전d2.세션?.let { s0 -> 전d2.저장기록열쇠(s0)?.let { 전d2.기록[it] } }
                val 후목록 = 기록들(d).toMutableList()
                val 사라짐 = 기록들(전d2).filter { r -> if (후목록.remove(r)) false else true }
                val 남의것 = 사라짐.filter { it != 허용 }
                if (남의것.isNotEmpty() || 사라짐.size > 1) 위반기록("운동조작이남의기록을지움", "막=$막 사라진=${사라짐.size} 허용=${허용 != null} 전=${전d2.기록.keys} 후=${d.기록.keys}")
            }
            if (d.기록.size > 전기록) 셈("기록저장")
            if (d.세션 == null) { 셈("세션끝"); 틱검사(); break }
            fz.검사(막)
            if (지금 - 마지막틱 >= 60_000) { 틱(); fz.d = d }
            검사("운동조작:$막")
        }
    }
    private fun 틱검사() {}

    fun 기록지우기() {
        준비()
        val 날들 = d.기록.keys.map { 날짜만(it) }.distinct()
        if (날들.isEmpty()) return
        val 날 = 날들[rnd.nextInt(날들.size)]
        val 열쇠들 = d.캘기록목록(날).map { it.first }
        val 골 = 열쇠들.filter { rnd.nextBoolean() }.toSet().ifEmpty { setOf(열쇠들[rnd.nextInt(열쇠들.size)]) }
        val 전 = d
        val (새, x) = d.캘기록지우기(날, 골) ?: return
        d = 새.예정초기화(앱오늘)
        지움띠.add(x); 띠시각 = 지금
        val 기대 = 기록들(전).toMutableList().also { l -> 골.forEach { k -> l.remove(전.기록[k]!!) } }
        if (!같은집합(기대, 기록들(d))) 위반기록("지우기가엉뚱한기록을지움", "고른=$골 전=${전.기록.keys} 후=${d.기록.keys}")
        로그.add("기록지움($날 ${골.size}개)"); 셈("기록지움", 골.size.toLong())
    }

    fun 기록되살림() {
        준비()
        if (지움띠.isEmpty()) return
        val x = 지움띠.removeAt(지움띠.size - 1)
        val 전 = d
        d = d.캘기록되살림(x).예정초기화(앱오늘)
        val 되살 = x.지운.map { x.전[it] }
        val 전목록 = 기록들(전)
        val 기대 = 전목록.toMutableList().also { l -> 되살.forEach { r -> if (r !in 전목록) l.add(r) } }
        if (!같은집합(기대, 기록들(d))) 위반기록("되살림이어긋남", "날=${x.날} 되살=${되살.size} 전=${전.기록.keys} 후=${d.기록.keys}")
        로그.add("기록되살림(${x.날})"); 셈("기록되살림", 되살.size.toLong())
    }

    fun 루틴삭제() {
        준비()
        if (d.루틴들.isEmpty()) return
        val 전 = d
        val i = rnd.nextInt(d.루틴들.size)
        val r = d.루틴들[i]; val rid = r.id
        d = d.copy(루틴들 = d.루틴들.filter { it.id != rid }, 예정 = d.예정.filterValues { it != rid }, 예정고정 = d.예정고정.filterValues { it != rid }).예정맞추기(앱오늘)
        루틴지움띠.add(루틴지움값(r, i, 전)); 띠시각 = 지금
        기록불변(전, d, "루틴삭제")
        로그.add("루틴삭제(${r.이름})"); 셈("루틴삭제")
    }

    fun 루틴되살림() {
        준비()
        if (루틴지움띠.isEmpty()) return
        val z = 루틴지움띠.removeAt(루틴지움띠.size - 1)
        val 전 = d
        d = if (d.루틴들.any { it.id == z.r.id }) d else {
            val l = d.루틴들.toMutableList().also { it.add(minOf(z.i, it.size), z.r) }
            d.copy(루틴들 = l, 예정 = z.전.예정.filterValues { id -> id == z.r.id || d.루틴(id) != null }, 예정고정 = z.전.예정고정).예정맞추기(앱오늘)
        }
        기록불변(전, d, "루틴되살림")
        로그.add("루틴되살림(${z.r.이름})"); 셈("루틴되살림")
    }

    fun 루틴수정() {
        준비()
        val 전 = d
        val 정식 = d.루틴들.filter { !it.휴식일 }
        when (rnd.nextInt(8)) {
            0 -> if (정식.isNotEmpty()) { val r = 정식[rnd.nextInt(정식.size)]; if (r.종목.isNotEmpty()) { val j = rnd.nextInt(r.종목.size); d = d.루틴바꿈(r.id) { x -> x.copy(종목 = x.종목.mapIndexed { q, e -> if (q == j && e.플랜id == null) e.copy(세트 = 1 + rnd.nextInt(6), 세트값 = emptyList(), 휴식값 = emptyList()) else e }) }; 로그.add("루틴세트수(${r.이름})") } }
            1 -> if (d.루틴들.isNotEmpty()) { val r = d.루틴들[rnd.nextInt(d.루틴들.size)]; d = d.루틴바꿈(r.id) { x -> x.copy(자동생성 = !x.자동생성) }.예정초기화(앱오늘); 로그.add("자동생성토글(${r.이름})") }
            2 -> if (d.루틴들.isNotEmpty()) { val r = d.루틴들[rnd.nextInt(d.루틴들.size)]; d = d.루틴바꿈(r.id) { x -> x.copy(이름 = x.이름 + "'") }; 로그.add("루틴이름(${r.이름})") }
            3 -> if (d.루틴들.isNotEmpty()) { val r = d.루틴들[rnd.nextInt(d.루틴들.size)]; d = d.루틴바꿈(r.id) { x -> x.copy(자동생성 = !x.자동생성) }.예정초기화(앱오늘); 로그.add("자동생성토글2(${r.이름})") }
            4 -> if (정식.isNotEmpty()) { val r = 정식[rnd.nextInt(정식.size)]; d = d.루틴바꿈(r.id) { x -> x.copy(종목 = x.종목.map { e -> if (e.플랜id != null) e else e.copy(무게 = e.무게 + 2.5, 세트값 = emptyList(), 휴식값 = emptyList()) }) }; 로그.add("루틴무게(${r.이름})") }
            5 -> { d = d.copy(루틴들 = d.루틴들 + 루틴("n${++번호}", "새${번호}", 종목 = listOf(루틴종목("새종목${번호}", 3, 30.0, 10, 60)), 자동생성 = rnd.nextBoolean())).예정초기화(앱오늘); 로그.add("루틴추가") }
            6 -> if (정식.isNotEmpty()) { val r = 정식[rnd.nextInt(정식.size)]; d = d.루틴바꿈(r.id) { x -> x.copy(종목 = x.종목.drop(1)) }; 로그.add("루틴종목빼기(${r.이름})") }
            else -> if (정식.isNotEmpty()) { val r = 정식[rnd.nextInt(정식.size)]; d = d.루틴바꿈(r.id) { x -> x.copy(종목 = x.종목 + 루틴종목("추가${++번호}", 3, 20.0, 10, 60)) }; 로그.add("루틴종목추가(${r.이름})") }
        }
        기록불변(전, d, "루틴수정")
        셈("루틴수정")
    }

    fun 예정조작() {
        준비()
        val 전 = d
        fun 날(n: Int) = 날더하기(앱오늘, n)
        when (rnd.nextInt(5)) {
            0 -> { d = d.예정지우기(날(rnd.nextInt(0, 8)), 앱오늘); 로그.add("예정지우기") }
            1 -> if (d.루틴들.isNotEmpty()) { d = d.그날만바꾸기(d.루틴들[rnd.nextInt(d.루틴들.size)].id, 날(rnd.nextInt(0, 8)), 앱오늘); 로그.add("그날만바꾸기") }
            2 -> { d = d.그날운동바꿈(날(rnd.nextInt(0, 6)), 앱오늘) { r -> r.copy(종목 = r.종목 + 루틴종목("특별${++번호}", 3, 25.0, 10, 60)) }; 로그.add("그날운동추가") }
            3 -> { d = d.그날운동바꿈(날(rnd.nextInt(0, 6)), 앱오늘) { r -> r.copy(종목 = r.종목.drop(1)) }; 로그.add("그날운동빼기") }
            else -> { d = d.copy(조절 = d.조절 + (앱오늘 to 오늘조절(볼륨 = 90 + rnd.nextInt(0, 25), 무게 = if (rnd.nextBoolean()) 0.0 else 2.5))); 로그.add("그날조절") }
        }
        기록불변(전, d, "예정조작")
        셈("예정조작")
    }

    fun 플랜조작() {
        준비()
        if (!플랜모드) return
        val 전 = d
        when (rnd.nextInt(3)) {
            0 -> { val x = d.플랜지우기("p1"); if (x != null) { d = x.first; 플랜지움띠.add(x.second); 띠시각 = 지금; 로그.add("플랜지움") } }
            1 -> if (플랜지움띠.isNotEmpty()) { val z = 플랜지움띠.removeAt(플랜지움띠.size - 1); d = d.플랜되살리기(z); 로그.add("플랜되살림") }
            else -> if (d.플랜찾기("p1") != null) { d = d.copy(플랜들 = d.플랜들.map { p -> if (p.id == "p1") p.copy(목표무게 = p.목표무게 + 2.5) else p }); d = d.copy(루틴들 = d.루틴들.map { d.플랜줄채움(it) }); 로그.add("플랜목표올림") }
        }
        기록불변(전, d, "플랜조작")
        셈("플랜조작")
    }

    fun 설정바꿈() {
        준비()
        val 전 = d
        d = d.copy(설정 = d.설정.copy(볼륨켬 = rnd.nextBoolean(), 볼륨언제 = if (rnd.nextBoolean()) "항상" else "성공", 볼륨배분 = if (rnd.nextBoolean()) "무게" else "횟수", 볼륨방식 = if (rnd.nextBoolean()) "%" else "kg"))
        기록불변(전, d, "설정")
        로그.add("설정바꿈")
    }

    fun 결과닫기() { if (d.결과 != null) { d = d.copy(결과 = null); 로그.add("결과닫기") } }

    /** 구식 되돌림 띠(지우고알림) — 5초 안에 다른 일이 끼면 그 일까지 통째로 되감는다 */
    fun 스냅되돌림() {
        준비()
        val 전 = d
        d = d.copy(메모 = emptyList())   // 메모/사진/종목을 지운 것으로 치고
        로그.add("지우고알림(스냅)")
        val 끼움 = rnd.nextInt(0, 3)
        val 시작기록 = d.기록
        val 시작세션 = d.세션
        repeat(끼움) {
            when (rnd.nextInt(4)) {
                0 -> { 지금 += rnd.nextInt(500, 2000); 운동조작(2) }
                1 -> { 기록지우기() }
                2 -> { 루틴수정() }
                else -> { 예정조작() }
            }
        }
        val 날아감 = (d.기록 != 시작기록) || (d.세션 != 시작세션) || (d.루틴들 != 전.루틴들) || (d.예정 != 전.예정)
        d = 전   // 되돌리기 = 스냅샷 통째 복원
        fz.지움.clear(); fz.뺌.clear(); 지움띠.clear()
        셈("스냅되돌림")
        if (날아감) { 셈("스냅이삼킴"); 위반기록("구식되돌림이끼어든변화까지되감음", "지우기 뒤 5초 안에 ${끼움}가지 일을 하고 되돌렸더니 그 일들도 사라짐 (기록 ${시작기록.keys} 세션 ${시작세션 != null})") }
        로그.add("스냅되돌림")
    }

    // ─────────────── 검사 ───────────────
    fun 검사(막: String) {
        val 기록 = d.기록
        val 날별 = 기록.keys.groupBy { 날짜만(it) }
        for ((날, 열쇠들) in 날별) {
            // 열쇠 모양 + 이 빠짐없이 이어지는가
            val 번호들 = 열쇠들.map { 캘열쇠차례(it) }.sorted()
            if (번호들 != (1..번호들.size).toList()) 위반기록("기록열쇠가끊김", "$날: ${열쇠들.sorted()}")
            if ('~' in 날) 위반기록("날짜모양이상", 날)
            // 같은 날 기록이 시간 순서대로인가
            val 차례 = 열쇠들.sortedBy { 캘열쇠차례(it) }.map { 기록[it]!!.끝시각 }.filter { it > 0 }
            if (차례 != 차례.sorted()) 위반기록("같은날순서가시간과다름", "$날: ${열쇠들.sortedBy { 캘열쇠차례(it) }.map { it.takeLast(2) + "@" + (기록[it]!!.끝시각 % 100000) }}")
        }
        val 고유 = HashSet<Pair<String, Long>>()
        for ((k, r) in 기록) {
            if (r.종목들.isEmpty() || r.종목들.any { it.세트들.isEmpty() }) 위반기록("빈기록", "$k ${r.루틴이름} 종목=${r.종목들.size}")
            if (r.종목들.any { e -> e.세트들.any { it.w < 0 || it.r < 0 } }) 위반기록("음수세트", k)
            if (r.걸린초 < 0) 위반기록("걸린초음수", k)
            if (r.시작시각 > 0 && r.끝시각 > 0 && r.끝시각 < r.시작시각) 위반기록("끝이시작보다앞", k)
            if (r.끝시각 > 0 && !고유.add(r.루틴id to r.끝시각)) 위반기록("같은기록이둘", "$k ${r.루틴이름} 끝=${r.끝시각 % 100000} 기록=${기록.keys.sorted()}")
        }
        for (k in d.미실시.keys) if (기록.keys.any { 날짜만(it) == k }) 위반기록("기록있는날이미실시", "$k 미실시=${d.미실시[k]} 기록=${기록.keys.filter { 날짜만(it) == k }}")
        d.예정.forEach { (k, v) -> if (k >= 앱오늘 && d.루틴(v) == null) 위반기록("유령예정", "$k → $v | 루틴들=${d.루틴들.map { it.id }} 전루틴=${전d?.루틴들?.map { it.id }} 전예정=${전d?.예정?.toSortedMap()} 후예정=${d.예정.toSortedMap()}") }
        if (플랜모드) {
            d.루틴들.forEach { r -> r.종목.forEach { e -> if (e.플랜id != null && d.플랜줄채움(e) != e) 위반기록("루틴플랜줄이처방과다름", "${r.이름} ${e.이름} 세트값=${e.세트값.map { it.w to it.r }} 처방=${d.플랜줄채움(e).세트값.map { it.w to it.r }}") } }
            d.플랜들.forEach { p -> if (p.한회 < 0 || p.측정들.size > p.한회 + 1) 위반기록("플랜회차이상", "한회=${p.한회} 측정=${p.측정들.size}") }
            val z = d.세션?.저장
            if (z != null) z.플랜.forEach { p0 -> d.플랜찾기(p0.id)?.let { p -> if (p.한회 !in p0.한회..(p0.한회 + 1)) 위반기록("보고저장플랜회차어긋남", "저장전 한회=${p0.한회} 지금=${p.한회}") } }
        }
        // 화면 쪽 계산이 예외를 던지지 않는가
        for ((k, r) in 기록) { d.캘기록세션(r, k); d.캘칸내용(날짜만(k)) }
    }

    var 전d: 앱데이터? = null
    fun 한사건() {
        전d = d
        val r = rnd.nextInt(100)
        when {
            r < 13 -> 시간흐름()
            r < 22 -> 앱열기()
            r < 33 -> 운동시작()
            r < 62 -> 운동조작()
            r < 70 -> 기록지우기()
            r < 77 -> 기록되살림()
            r < 80 -> 루틴삭제()
            r < 82 -> 루틴되살림()
            r < 86 -> 루틴수정()
            r < 91 -> 예정조작()
            r < 93 -> 설정바꿈()
            r < 95 -> if (플랜모드 && rnd.nextBoolean()) 플랜조작() else if (System.getenv("NOSNAP") == null) 스냅되돌림() else 결과닫기()
            else -> 결과닫기()
        }
        검사("사건")
    }

    fun 하루내내(): Boolean = true
}

fun mainMonth(args: Array<String>) {
    val 판수 = args.getOrNull(0)?.toInt() ?: 2000
    val 사건수 = args.getOrNull(1)?.toInt() ?: 300
    val 위반 = LinkedHashMap<String, MutableList<String>>()
    val 통계 = LinkedHashMap<String, Long>()
    var 죽음 = 0
    for (seed in 0 until 판수) {
        val m = Mo(Random(seed + 7_000_000), 위반, 통계, 플랜모드 = seed % 2 == 1)
        try {
            while (m.로그.size < 사건수 * 3 && 일수(m) < 30) m.한사건()
            통계["날수"] = (통계["날수"] ?: 0L) + 일수(m)
        } catch (ex: Throwable) {
            죽음++
            val k = "예외:" + ex.javaClass.simpleName + ":" + (ex.stackTrace.firstOrNull { it.className.contains("hasenheide") }?.let { "${it.fileName}:${it.lineNumber}" } ?: "")
            위반.getOrPut(k) { ArrayList() }.let { if (it.size < 3) it.add(ex.message + "\n   조작(마지막 40): " + m.로그.takeLast((System.getenv("LOGN") ?: "40").toInt()).joinToString(" → ")) }
        }
    }
    println("판수=$판수 예외종료=$죽음")
    println("통계: " + 통계.entries.joinToString(" ") { "${it.key}=${it.value}" })
    for ((k, v) in 위반) { println("## $k"); v.forEach { println("  - $it") } }
}

private fun 일수(m: Mo): Int = ((m.지금 - LocalDate.of(2026, 10, 1).atTime(8, 0).atZone(m.존).toInstant().toEpochMilli()) / 86_400_000L).toInt()

object M30 { @JvmStatic fun main(args: Array<String>) = mainMonth(args) }
