package com.slayde.hasenheide.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 앱 옮기기 1단계 — 데이터 시험 (10-05 · 스키마 15).
 *  (a) 옛 스키마 14 JSON(v0.13 의 글로() 꼴을 손으로 적은 것)이 그대로 열리는지
 *  (b) 스키마 15 저장 → 읽기 왕복이 같은지
 *  (c) 빈 JSON · 이상한 값에도 죽지 않는지
 *  (d) 운동 중 종목 빼기 · 되돌리기 · 끌기 · 넣기의 번호 맞춤
 *  (e) 종목 사전 · 초성 검색
 * Storage 는 org.json 을 쓴다 → build.gradle.kts 에 testImplementation("org.json:json:…") (안드로이드 단위시험의 org.json 은 빈 껍데기다)
 */
class PortTest {

    // ─────────────── (a) 옛 스키마 14 ───────────────

    /** v0.13.0 의 글로() 가 만든 꼴 — 종목 id · 종id · 종목설정 · 프로필 설정이 없다. 한 종목은 시안에서 옮긴 근육(S 포함) */
    private val 옛14 = """
    {
     "스키마": 14,
     "종목표": [
      {"이름": "벤치프레스", "부위": "가슴", "장비": "바벨", "목표1RM": 100, "사진": ["p1.jpg"]},
      {"이름": "턱걸이", "부위": "등", "장비": "맨몸", "근육": {"lats": "P", "biceps": "S", "forearm": "Y", "abs": "X"}},
      {"이름": "벤치프레스", "부위": "가슴", "장비": "덤벨"}
     ],
     "카테고리": ["가슴", "등", "하체", "어깨", "팔", "복근"],
     "루틴들": [
      {"id": "r1", "이름": "가슴날", "휴식일": false, "자동생성": true, "종목": [
        {"이름": "벤치프레스", "세트": 3, "무게": 60, "횟수": 8, "휴식": 90, "세트값": [[60, 8], [60, 8], [55, 10, 2]], "휴식값": [90, 90, 120]},
        {"이름": "턱걸이", "세트": 3, "무게": 0, "횟수": 6, "휴식": 120, "슈퍼": "A"}
      ]}
     ],
     "기록": {
      "2026-10-01": {"루틴id": "r1", "루틴이름": "가슴날", "달성": true, "걸린초": 3600, "시작시각": 1759300000000, "끝시각": 1759303600000,
        "종목들": [{"이름": "벤치프레스", "세트들": [[60, 8], [40, 10, 1]], "임시": false}]}
     },
     "예정": {"2026-10-06": "r1"},
     "일정": {},
     "설정": {"자동진행": true, "넘어가기전확인": false, "소리진동": true, "화면유지": true, "무게폭": 2.5, "기본휴식": 90, "기준": 3, "기본세트": 3,
       "볼륨켬": false, "볼륨방식": "%", "볼륨값": 2.5, "볼륨언제": "성공", "볼륨배분": "횟수", "횟수상한": 12, "진동세기": 2, "진동시간": 1000,
       "번호보기": true, "배너": "근육 2장", "회복시간": 24, "색표": "heat"},
     "메모": [],
     "세션": {"루틴id": "r1", "루틴이름": "가슴날", "시작시각": 1759400000000, "i": 1, "s": 0, "무게": 0, "횟수": 6, "끝화면": false, "마지막": 1759400100000, "조절됨": false,
       "종목들": [
        {"이름": "벤치프레스", "세트": 3, "계획세트": 3, "무게": 60, "횟수": 8, "휴식": 90, "기록": [[60, 8], null, null], "예정값": [], "휴식들": [90, 90, 90], "임시": false, "마감": false},
        {"이름": "턱걸이", "세트": 3, "계획세트": 3, "무게": 0, "횟수": 6, "휴식": 120, "기록": [], "예정값": [], "휴식들": [120, 120, 120], "임시": false, "마감": false}
       ],
       "휴식": {"k": 0, "끝시각": 1759400190000, "물음": false, "총초": 90, "종목": 0}},
     "예정고정": {}, "미실시": {}, "조절": {},
     "플랜들": [], "몸": {"나이": 35, "남": true, "체중": 80}, "향상기록들": [],
     "피로": {"chest_mid": [12.5, 1759303600000, 1759350000000]}, "최대볼륨": {"chest_mid": 1500},
     "업적": {"1-1": 1759303600000}, "대표칭호": "3-1", "세기": {"백업": 1}, "쉰날": [], "건너뜀": {}, "체중기록": [[1759300000000, 80]], "스탯기록": {}
    }
    """.trimIndent()

    @Test fun 옛14_그대로열림() {
        val d = 저장소.글에서(옛14)
        // 종목 id — 옛 종목은 이름이 id. 같은 이름 둘째는 "-2"
        assertEquals(listOf("벤치프레스", "턱걸이", "벤치프레스-2"), d.종목표.map { it.id })
        assertEquals(100.0, d.종목표[0].목표1RM)
        assertEquals(listOf("p1.jpg"), d.종목표[0].사진)
        assertEquals("가슴", d.종목표[0].칸)
        // 근육 역할 — S → Y, 모르는 값(X) 은 버림, 없는 종목은 빈 맵
        assertEquals(mapOf("lats" to "P", "biceps" to "Y", "forearm" to "Y"), d.종목표[1].근육)
        assertTrue(d.종목표[0].근육.isEmpty())
        // 줄의 종id — 옛 줄은 null → 이름으로 찾는다 (같은 이름이면 먼저 만든 것)
        val r = d.루틴들[0]
        assertNull(r.종목[0].종id)
        assertEquals("벤치프레스", r.종목[0].열쇠)
        assertEquals("바벨", d.종목찾기(r.종목[0].종id, r.종목[0].이름)?.장비)
        assertEquals(세트(55.0, 10, 2), r.종목[0].세트값[2])
        assertEquals("A", r.종목[1].슈퍼)
        assertNull(d.기록.getValue("2026-10-01").종목들[0].종id)
        assertEquals(세트(40.0, 10, 1), d.기록.getValue("2026-10-01").종목들[0].세트들[1])
        // 세션 · 휴식 그대로
        val S = assertNotNull(d.세션)
        assertEquals(1, S.i); assertEquals(0, S.휴식?.종목); assertNull(S.종목들[0].종id)
        // 스키마 15 새 값은 기본값
        assertTrue(d.종목설정.isEmpty()); assertTrue(d.업적순서.isEmpty()); assertTrue(d.업적숨김.isEmpty()); assertTrue(d.인증샷.isEmpty())
        assertEquals(설정값().copy(무게폭 = 2.5, 기본휴식 = 90, 기본세트 = 3), d.설정)
        assertEquals("", d.설정.닉네임); assertNull(d.설정.프로필사진); assertEquals(보고서보임값(), d.설정.보고서보임)
        assertEquals("최신순", d.설정.업적정렬); assertTrue(d.설정.큰운동추가.isEmpty())
        // 그 밖의 옛 값
        assertEquals(mapOf("1-1" to 1759303600000L), d.업적)
        assertEquals(12.5, d.피로.getValue("chest_mid").lv0)
        // 종목설정이 없으면 설정의 기본 세트 (3세트 × 20kg × 10회 × 90초)
        assertEquals(같은세트들(3, 20.0, 10, 90), d.종목기본세트("벤치프레스"))
        // 같은 이름 번호 — 둘이면 1 · 2, 하나면 0. 옛 줄(종id 없음)은 먼저 만든 것
        assertEquals(1, d.같은이름번호(null, "벤치프레스"))
        assertEquals(2, d.같은이름번호("벤치프레스-2", "벤치프레스"))
        assertEquals(0, d.같은이름번호(null, "턱걸이"))
    }

    @Test fun 옛14_사용자근육이_피로에() {
        val d = 저장소.글에서(옛14)
        val 들 = listOf(근육입력("턱걸이", listOf(세트(0.0, 10))))
        val 채움 = d.근육채움(들)
        assertEquals(mapOf("lats" to "P", "biceps" to "Y", "forearm" to "Y"), 채움[0].근육)
        // 사용자 역할: biceps 는 Y(0.25) — 내장 규칙(biceps S 0.5)과 다르다
        val v = 근육계산.잎볼륨(채움, { d.종목부위(it) }, 80.0)
        val 볼 = 80.0 * 근육표.맨몸비율 * 10
        val 이두잎 = 근육계산.잎("biceps")
        assertEquals(볼 * 0.25, v.getValue(이두잎[0]), 1e-6)
        // 사용자 역할이 없는 종목은 내장 규칙 그대로 (기존 시험과 같은 값)
        val 벤 = d.근육채움(listOf(근육입력("벤치프레스", listOf(세트(100.0, 1)))))
        assertNull(벤[0].근육)
        assertEquals(근육계산.잎볼륨(listOf(근육입력("벤치프레스", listOf(세트(100.0, 1)))), { "가슴" }, 80.0),
            근육계산.잎볼륨(벤, { "가슴" }, 80.0))
    }

    // ─────────────── (b) 스키마 15 왕복 ───────────────

    @Test fun 스키마15_왕복() {
        val 벤2 = 종목("벤치프레스", "가슴", "덤벨", id = "종abc", 근육 = mapOf("chest_mid" to "P", "triceps" to "Y"))
        val d = 앱데이터(
            종목표 = listOf(종목("벤치프레스", "가슴", "바벨"), 벤2, 종목("플랭크", "맨몸", 사진 = listOf("a.jpg"))),
            루틴들 = listOf(루틴("r1", "가슴", 종목 = listOf(루틴종목("벤치프레스", 종id = "종abc", 세트값 = listOf(세트(50.0, 10)), 휴식값 = listOf(60)), 루틴종목("벤치프레스")))),
            기록 = mapOf("2026-10-04" to 날기록("r1", "가슴", true, listOf(종목기록("벤치프레스", listOf(세트(50.0, 10)), 종id = "종abc")), 100, 1L, 2L)),
            세션 = 운동세션("r1", "가슴", 10L, 1, 0, 20.0, 10, listOf(세션종목("벤치프레스", 1, 1, 20.0, 10, 60, 종id = "종abc"), 세션종목("벤치프레스", 1, 1, 20.0, 10, 60)),
                휴식중(0, 99L, 다음i = 1, 다음s = 0, 총초 = 60, 종목 = 0)),
            설정 = 설정값(닉네임 = "홍겸", 프로필사진 = "profile.jpg", 링크 = listOf("https://instagram.com/x", "https://youtube.com/y"),
                보고서보임 = 보고서보임값(프로필 = false), 큰운동추가 = listOf("오버헤드 프레스", "스내치"), 업적정렬 = "가나다순"),
            종목설정 = mapOf("종abc" to listOf(종목세트(40.0, 12, 60), 종목세트(45.0, 10, 90)), "벤치프레스" to 같은세트들(2, 60.0, 5, 180)),
            업적 = mapOf("1-1" to 5L, "1-2" to 6L),
            업적순서 = listOf("1-2", "1-1"),
            업적숨김 = setOf("1-2"),
            인증샷 = listOf(인증사진("c1.jpg", 300L, 0L), 인증사진("c0.jpg", 100L, 200L)),
        )
        val 글 = 저장소.글로(d)
        assertTrue(글.contains("\"스키마\": 15"))
        val e = 저장소.글에서(글)
        assertEquals(d.종목표, e.종목표)
        assertEquals(d.루틴들, e.루틴들)
        assertEquals(d.기록, e.기록)
        assertEquals(d.세션, e.세션)
        assertEquals(d.설정, e.설정)
        assertEquals(d.종목설정, e.종목설정)
        assertEquals(d.업적순서, e.업적순서)
        assertEquals(d.업적숨김, e.업적숨김)
        assertEquals(인증순(d.인증샷), e.인증샷)
        assertEquals(d.copy(인증샷 = 인증순(d.인증샷)), e)
        // 한 번 더 돌아도 같다
        assertEquals(e, 저장소.글에서(저장소.글로(e)))
    }

    // ─────────────── (c) 빈 값 · 이상한 값 ───────────────

    @Test fun 빈JSON() {
        val d = 저장소.글에서("{}")
        assertEquals(앱데이터(), d)
    }

    @Test fun 이상한값() {
        val 글 = """
        {"스키마": 15, "모르는필드": [1, 2, {"x": null}],
         "종목표": [{"이름": "", "부위": "", "id": "", "근육": {"": "P", "lats": "Q", "abs": 3}}, {"이름": "스쿼트", "id": null}],
         "루틴들": [{"id": "r", "이름": "", "종목": [{"이름": "스쿼트", "종id": "없는id", "세트": -3, "무게": -10}]}],
         "설정": {"닉네임": "열세글자를넘는아주긴닉네임입니다", "링크": ["", "javascript:alert(1)", "insta.com/me", 5], "보고서보임": "이상",
                 "큰운동추가": ["스쿼트", "클린 앤 저크", "스내치", "오버헤드 프레스", "모름"], "업적정렬": "엉뚱", "프로필사진": ""},
         "종목설정": {"a": [[-5, -2, -1], {"w": 30, "r": 0, "휴": 45}, "이상"], "b": {"세트": 30, "w": 50}, "c": {"세트": 0}, "d": "이상", "e": [],
                     "f": {"세트": [{"w": 10, "r": 8, "휴": 30}]}},
         "업적숨김": {"1-1": true, "1-2": false}, "업적순서": ["", "1-3"],
         "인증샷": [["a.jpg", 1, -5], ["", 2, 0], "이상", ["b.jpg", 3, 0], ["c.jpg", 4], ["d.jpg", 5], ["e.jpg", 6], ["f.jpg", 7], ["g.jpg", 8], ["h.jpg", 9], ["i.jpg", 10]]
        }
        """.trimIndent()
        val d = 저장소.글에서(글)
        assertEquals("", d.종목표[0].id.let { if (it.isEmpty()) "" else it })   // 이름도 비었으면 id 도 빈 글 (죽지 않는다)
        assertTrue(d.종목표[0].근육.isEmpty())
        assertEquals("스쿼트", d.종목표[1].id)
        // 없는 종id → 이름으로
        assertEquals("스쿼트", d.종목찾기(d.루틴들[0].종목[0].종id, d.루틴들[0].종목[0].이름)?.이름)
        // 설정 — 12자 · http(s) 만 · 보고서보임 기본 · 큰운동 표 순서 2개 · 정렬 기본 · 빈 사진 = 없음
        assertEquals(12, d.설정.닉네임.length)
        assertEquals(listOf("https://insta.com/me", "https://5"), d.설정.링크)
        assertEquals(보고서보임값(), d.설정.보고서보임)
        assertEquals(listOf("오버헤드 프레스", "스내치"), d.설정.큰운동추가)
        assertEquals("최신순", d.설정.업적정렬)
        assertNull(d.설정.프로필사진)
        // 종목설정 — 무게 0 이상 · 횟수 1 이상 · 휴식 0 이하는 기본(60) · 옛 꼴 n 줄(10까지) · 빈 것은 버림
        assertEquals(listOf(종목세트(0.0, 1, 60), 종목세트(30.0, 1, 45)), d.종목설정["a"])
        assertEquals(같은세트들(10, 50.0, 10, 60), d.종목설정["b"])
        assertEquals(같은세트들(1, 20.0, 10, 60), d.종목설정["c"])
        assertFalse("d" in d.종목설정); assertFalse("e" in d.종목설정)
        assertEquals(listOf(종목세트(10.0, 8, 30)), d.종목설정["f"])
        assertEquals(setOf("1-1"), d.업적숨김)
        assertEquals(listOf("1-3"), d.업적순서)
        // 인증샷 — 빈 이름 · 이상한 줄 버림, 8장까지 (새것부터)
        assertEquals(8, d.인증샷.size)
        assertEquals("i.jpg", d.인증샷[0].파일)
        assertTrue(d.인증샷.all { it.고정 >= 0 })
    }

    @Test fun 옛종목설정_v17() {
        assertEquals(같은세트들(3, 60.0, 8, 120), 옛종목설정(3, 60.0, 8, 120, 60))
        assertEquals(같은세트들(1, 20.0, 10, 90), 옛종목설정(-1, null, null, null, 90))
        val d = 앱데이터(설정 = 설정값(기본세트 = 2, 기본휴식 = 75))
        assertEquals(같은세트들(2, 20.0, 10, 75), d.종목기본세트("아무거나"))
        val d2 = d.copy(종목설정 = mapOf("x" to listOf(종목세트(-1.0, 0, 0))))
        assertEquals(listOf(종목세트(0.0, 1, 75)), d2.종목기본세트("x"))
    }

    // ─────────────── 종목 id ───────────────

    @Test fun 종목id() {
        var d = 앱데이터(종목표 = listOf(종목("벤치프레스", "가슴")))
        d = d.종목더하기(종목("벤치프레스", "가슴", "덤벨"), listOf(종목세트(30.0, 12, 60)), 지금 = 1000L)
        assertEquals(2, d.종목표.size)
        val 새 = d.종목표[1]
        assertTrue(새.id != "벤치프레스" && 새.id.startsWith("종"))
        assertEquals(listOf(종목세트(30.0, 12, 60)), d.종목기본세트(새.id))
        // 같은 시각에 또 만들어도 겹치지 않는다
        val d2 = d.종목더하기(종목("벤치프레스", "가슴", id = 새.id), 지금 = 1000L)
        assertEquals(3, d2.종목표.map { it.id }.toSet().size)
        // id 로 먼저, 없으면 이름
        assertEquals("덤벨", d.종목찾기(새.id, "벤치프레스")?.장비)
        assertEquals("", d.종목찾기(null, "벤치프레스")?.장비)
        assertEquals("", d.종목찾기("없음", "벤치프레스")?.장비)
        assertNull(d.종목찾기("없음", "없음"))
        assertEquals(2, 같은이름번호(d.종목표, 새.id, "벤치프레스"))
        // 줄 만들기 — 종id · 세트값 · 휴식값
        val 줄 = d.루틴줄(새.id)
        assertEquals(새.id, 줄.종id); assertEquals(1, 줄.세트); assertEquals(listOf(세트(30.0, 12)), 줄.세트값)
        val s = d.세션줄(새.id)
        assertEquals(새.id, s.종id); assertTrue(s.임시); assertEquals(0, s.계획세트)
        // 운동 시작 → 저장 에 종id 가 따라간다
        val r = 루틴("r", "가슴", 종목 = listOf(줄))
        val S = assertNotNull(운동시작(r, 0L))
        assertEquals(새.id, S.종목들[0].종id)
        val 끝 = d.copy(루틴들 = listOf(r), 세션 = S.체크(0, 1L)).운동저장("2026-10-05", 100L)
        assertEquals(새.id, 끝.기록.values.first().종목들[0].종id)
    }

    @Test fun 오늘반영_같은이름_다른종목() {
        val r = 루틴("r", "가슴", 종목 = listOf(루틴종목("벤치프레스", 1, 50.0, 10), 루틴종목("벤치프레스", 1, 20.0, 10, 종id = "종b")))
        var S = assertNotNull(운동시작(r, 0L))
        // 둘째 줄(종b)만 체크 — 첫째 줄에 잘못 붙지 않아야 한다
        S = S.값고치기(1, 0, 새무게 = 25.0).체크(1, 0, 1L)
        val 새 = r.오늘반영(S)
        assertEquals(50.0, 새.종목[0].무게)
        assertEquals(25.0, 새.종목[1].무게)
    }

    // ─────────────── 프로필 ───────────────

    @Test fun 프로필() {
        assertEquals("https://a.com", 링크주소(" a.com "))
        assertEquals("http://b.com", 링크주소("http://b.com"))
        assertEquals("", 링크주소("javascript:alert(1)"))
        assertEquals("", 링크주소("   "))
        assertEquals(listOf("바벨 로우", "스내치"), 큰운동추가정리(listOf("클린 앤 저크", "벤치", "바벨 로우", "스내치")))   // 표 순서 · 2개
        assertEquals(listOf("클린 앤 저크"), 큰운동추가정리(listOf("클린 앤 저크", "벤치")))
        var d = 앱데이터()
        repeat(8) { d = assertNotNull(d.인증넣기("p$it.jpg", it.toLong())) }
        assertNull(d.인증넣기("p9.jpg", 9L))
        d = assertNotNull(d.인증고정("p0.jpg", 100L))
        d = assertNotNull(d.인증고정("p1.jpg", 101L))
        d = assertNotNull(d.인증고정("p2.jpg", 102L))
        assertNull(d.인증고정("p3.jpg", 103L))
        assertEquals(listOf("p0.jpg", "p1.jpg", "p2.jpg", "p7.jpg"), 인증순(d.인증샷).take(4).map { it.파일 })
        d = assertNotNull(d.인증고정("p0.jpg", 104L))   // 고정 풀기
        assertEquals(0L, d.인증샷.first { it.파일 == "p0.jpg" }.고정)
    }

    @Test fun 업적차례() {
        val 칭호 = mapOf("a" to "다", "b" to "가", "c" to "나", "d" to "라")
        val f: (String) -> String? = { 칭호[it] }
        var d = 앱데이터(업적 = mapOf("a" to 1L, "b" to 3L, "c" to 2L, "d" to 4L, "모름" to 9L))
        assertEquals(listOf("d", "b", "c", "a"), d.업적차례(f))
        assertEquals(listOf("a", "c", "b", "d"), d.copy(설정 = 설정값(업적정렬 = "오래된순")).업적차례(f))
        assertEquals(listOf("b", "c", "a", "d"), d.copy(설정 = 설정값(업적정렬 = "가나다순")).업적차례(f))
        // 숨김 → 보이는 줄만 끌어 옮긴다 (숨긴 b 는 제자리)
        d = d.업적보임바꿈("b")
        assertFalse(d.업적보임("b"))
        assertEquals(d, d.업적보임바꿈("없는번호"))
        val 옮김 = d.업적옮기기(0, 2, true, f)   // 보이는 줄 d c a → c a d
        assertEquals("직접", 옮김.설정.업적정렬)
        assertEquals(listOf("c", "b", "a", "d"), 옮김.업적순서)
        assertEquals(listOf("c", "b", "a", "d"), 옮김.업적차례(f))
        // 순서에 없는 새 업적은 맨 앞
        val 새 = 옮김.copy(업적 = 옮김.업적 + ("e" to 10L))
        assertEquals(listOf("c", "b", "a", "d"), 새.업적차례(f))   // e 는 칭호가 없어 빠진다
        val 새2 = 옮김.copy(업적 = 옮김.업적 + ("x" to 10L))
        assertEquals(listOf("x", "c", "b", "a", "d"), 새2.업적차례 { 칭호[it] ?: if (it == "x") "새" else null })
    }

    // ─────────────── (d) 운동 중 순서 ───────────────

    private fun 줄(이름: String, 세트: Int = 3, 한것: Int = 0) =
        세션종목(이름, 세트, 세트, 20.0, 10, 60, 기록 = List(한것) { 세트(20.0, 10) } + List(세트 - 한것) { null })

    /** A(2/3 함) B(0/3) C(0/3) D(0/3), 지금 = B 0세트 */
    private fun 세션(i: Int = 1, s: Int = 0, 휴: 휴식중? = null) =
        운동세션("r", "R", 1000L, i, s, 20.0, 10, listOf(줄("A", 3, 2), 줄("B"), 줄("C"), 줄("D")), 휴)

    private fun 이름들(S: 운동세션) = S.종목들.joinToString("") { it.이름 }

    @Test fun 빼기_뒤() {
        val S = 세션()
        val (z, 뺀) = assertNotNull(S.종목빼기(3, 본 = 1))
        assertEquals("ABC", 이름들(z.세션)); assertEquals(1, z.세션.i); assertEquals(1, z.본)
        assertEquals(listOf(0, 1, 2, null), z.자리표)
        assertEquals("D", 뺀.e.이름)
    }

    @Test fun 빼기_앞이면_i_하나준다() {
        val S = 세션(i = 2, s = 1)
        val (z, _) = assertNotNull(S.종목빼기(0, 본 = 2))
        assertEquals("BCD", 이름들(z.세션))
        assertEquals(1, z.세션.i); assertEquals(1, z.세션.s); assertEquals("C", z.세션.지금종목.이름)
        assertEquals(1, z.본)
    }

    @Test fun 빼기_지금칸() {
        val S = 세션(i = 1)
        val (z, _) = assertNotNull(S.종목빼기(1, 본 = 1))
        // 뒤쪽의 남은 종목(C) 로
        assertEquals("ACD", 이름들(z.세션)); assertEquals("C", z.세션.지금종목.이름); assertEquals(0, z.세션.s)
        assertEquals(1, z.본)   // 뺀 자리에 온 종목
        // 맨 끝 종목이 지금이면 → 앞쪽의 남은 것 (A 는 3세트째가 남았다)
        val (z2, _) = assertNotNull(세션(i = 3).종목빼기(3, 본 = 3))
        assertEquals("A", z2.세션.지금종목.이름); assertEquals(2, z2.세션.s)
        assertEquals(2, z2.본)   // 끝이었으면 그 앞
    }

    @Test fun 빼기_다른칸_보는중() {
        // 지금 = B, 보는 칸 = D, D 를 뺀다 → 보는 칸은 뺀 자리(끝이었으니 그 앞)
        val (z, _) = assertNotNull(세션().종목빼기(3, 본 = 3))
        assertEquals(2, z.본); assertEquals("B", z.세션.지금종목.이름)
        // 보는 칸 = null (모름) → 뺀 자리
        val (z2, _) = assertNotNull(세션().종목빼기(2, 본 = null))
        assertEquals(2, z2.본)
    }

    @Test fun 빼기_쉬는중() {
        val 휴 = 휴식중(1, 99_000L, 다음i = 3, 다음s = 0, 총초 = 60, 종목 = 0)
        // 쉬던 종목(A)을 빼면 휴식을 치운다
        val (z, _) = assertNotNull(세션(i = 1, 휴 = 휴).종목빼기(0, 본 = 0))
        assertNull(z.세션.휴식)
        // 다른 종목(C)을 빼면 휴식은 남고, 다음i(D: 3) 는 2 로
        val (z2, _) = assertNotNull(세션(i = 1, 휴 = 휴).종목빼기(2, 본 = 1))
        assertEquals(0, z2.세션.휴식?.종목); assertEquals(2, z2.세션.휴식?.다음i)
        // 다음i 의 종목(D)을 빼면 다음i · 다음s 를 비운다
        val (z3, _) = assertNotNull(세션(i = 1, 휴 = 휴).종목빼기(3, 본 = 1))
        assertNull(z3.세션.휴식?.다음i); assertNull(z3.세션.휴식?.다음s); assertEquals(0, z3.세션.휴식?.종목)
        // 옛 휴식(종목 -1 = 지금 종목)
        val 옛 = 세션(i = 2, 휴 = 휴식중(0, 99_000L, 총초 = 60))
        val (z4, _) = assertNotNull(옛.종목빼기(0, 본 = 2))
        assertEquals(1, z4.세션.휴식?.종목)   // -1(지금 = C) → C 의 새 번호 1 로 적힌다
    }

    @Test fun 빼기_마지막하나() {
        val S = 운동세션("r", "R", 1L, 0, 0, 20.0, 10, listOf(줄("A")))
        assertNull(S.종목빼기(0, 0))
        assertNull(세션().종목빼기(9, 0))
    }

    @Test fun 빼기_되돌리기_원상태() {
        val 휴 = 휴식중(1, 99_000L, 다음i = 3, 다음s = 1, 총초 = 60, 종목 = 1)
        val 경우 = listOf(
            Triple(세션(i = 1), 1, 1), Triple(세션(i = 1), 0, 2), Triple(세션(i = 1), 3, 3), Triple(세션(i = 3, s = 2), 3, 0),
            Triple(세션(i = 1, s = 1, 휴 = 휴), 1, 1), Triple(세션(i = 1, s = 1, 휴 = 휴), 3, 1), Triple(세션(i = 1, s = 1, 휴 = 휴), 0, 0),
            Triple(세션(i = 2, 휴 = 휴식중(0, 99_000L, 총초 = 60, 종목 = 2)), 2, 2),
        )
        for ((S, j, 본) in 경우) {
            val S0 = S.copy(무게 = 37.5, 횟수 = 7)
            val (z, 뺀) = assertNotNull(S0.종목빼기(j, 본))
            val 되 = z.세션.종목되돌리기(뺀, z.본, 지금 = 50_000L)
            assertEquals(S0, 되.세션, "빼기 $j → 되돌리기 (i=${S.i})")
            // 보는 칸은 되돌린 종목 (시안 U.본=at)
            assertEquals(j, 되.본)
        }
    }

    @Test fun 되돌리기_휴식끝났으면_안살림() {
        val S = 세션(i = 1, 휴 = 휴식중(0, 10_000L, 총초 = 60, 종목 = 1))
        val (z, 뺀) = assertNotNull(S.종목빼기(1, 1))
        val 되 = z.세션.종목되돌리기(뺀, z.본, 지금 = 20_000L)
        assertNull(되.세션.휴식)
        assertEquals("ABCD", 이름들(되.세션)); assertEquals(1, 되.세션.i)
        // 다른 운동이면 아무것도 안 한다
        val 다른 = z.세션.copy(시작시각 = 5L)
        assertEquals(다른, 다른.종목되돌리기(뺀, 0, 0L).세션)
    }

    @Test fun 되돌리기_그사이_더뺐으면_끝에() {
        val S = 세션(i = 0, s = 2)
        val (z1, 뺀D) = assertNotNull(S.종목빼기(3, 0))
        val (z2, _) = assertNotNull(z1.세션.종목빼기(2, 0))
        val 되 = z2.세션.종목되돌리기(뺀D, z2.본, 0L)   // 자리 3 > 줄 수 2 → 맨 끝
        assertEquals("ABD", 이름들(되.세션)); assertEquals(0, 되.세션.i)
    }

    @Test fun 끌기() {
        val 휴 = 휴식중(0, 99_000L, 다음i = 2, 다음s = 0, 총초 = 60, 종목 = 1)
        val S = 세션(i = 1, s = 2, 휴 = 휴)
        // B 를 D 뒤로 → A C D B
        val z = S.종목옮기기(1, 3, 뒤에 = true, 본 = 2)
        assertEquals("ACDB", 이름들(z.세션))
        assertEquals("B", z.세션.지금종목.이름); assertEquals(2, z.세션.s); assertEquals(3, z.세션.i)
        assertEquals("B", z.세션.종목들[z.세션.휴식!!.종목].이름)
        assertEquals("C", z.세션.종목들[z.세션.휴식!!.다음i!!].이름)
        assertEquals("C", z.세션.종목들[z.본!!].이름)
        assertEquals(listOf(0, 3, 1, 2), z.자리표)
        // D 를 A 앞으로 → D A B C
        val z2 = S.종목옮기기(3, 0, 뒤에 = false, 본 = 3)
        assertEquals("DABC", 이름들(z2.세션)); assertEquals("B", z2.세션.지금종목.이름); assertEquals(0, z2.본)
        // 제자리 (B 를 A 뒤로) → 그대로
        val z3 = S.종목옮기기(1, 0, 뒤에 = true, 본 = 1)
        assertEquals(S, z3.세션); assertEquals(1, z3.본)
        // 끌었다 되돌려 끌면 원상태
        val z4 = z.세션.종목옮기기(3, 0, 뒤에 = true, 본 = z.본)
        assertEquals(S, z4.세션); assertEquals(2, z4.본)
    }

    @Test fun 넣기_넣은것빼기() {
        val S = 세션(i = 1)
        val z = S.종목넣기(줄("E"), 본 = 1)
        assertEquals("ABCDE", 이름들(z.세션)); assertEquals(1, z.세션.i); assertEquals(1, z.본)
        // 누름 = 하나 빼기 — 맨 뒤의 안 시작한 것
        val z2 = assertNotNull(z.세션.넣은것빼기(4) { it.이름 == "E" })
        assertEquals("ABCD", 이름들(z2.세션)); assertEquals(3, z2.본)
        // 지금 하는 것 · 세트를 한 것은 못 뺀다
        assertNull(S.넣은것빼기(1) { it.이름 == "B" })
        assertNull(S.넣은것빼기(1) { it.이름 == "A" })
        // 앞을 빼면 지금 번호가 준다
        val S3 = 운동세션("r", "R", 1L, 2, 0, 20.0, 10, listOf(줄("X"), 줄("A"), 줄("B")))
        val z3 = assertNotNull(S3.넣은것빼기(2) { it.이름 == "X" })
        assertEquals(1, z3.세션.i); assertEquals("B", z3.세션.지금종목.이름); assertEquals(1, z3.본)
    }

    // ─────────────── (e) 사전 · 초성 ───────────────

    @Test fun 사전() {
        val l = 종목사전.목록
        assertEquals(129, l.size)
        assertEquals(l.size, l.map { it.이름 }.toSet().size, "이름 중복")
        val 칸들 = setOf("가슴", "등", "하체", "어깨", "팔", "맨몸")
        assertTrue(l.all { it.칸 in 칸들 })
        assertEquals("맨몸", 종목사전.이름으로("크런치")?.칸)
        assertEquals("맨몸", 종목사전.이름으로("플랭크")?.칸)
        // 근육 키는 모두 세부 부위 · 역할은 P S Y
        assertTrue(l.all { x -> x.근?.all { (k, v) -> k in 종목사전.세부키 && v in setOf("P", "S", "Y") } ?: true })
        // 세부 부위 키가 근육 지도에 있다
        assertTrue(종목사전.세부키.all { 근육계산.잎(it).isNotEmpty() })
        assertEquals(listOf("바벨 벤치프레스", "플랫 벤치", "벤치"), 종목사전.이름으로("벤치프레스")?.별)
        assertEquals(mapOf("chest_upper" to "P", "chest_mid" to "S", "delt_front" to "Y"), 종목사전.이름으로("인클라인 덤벨 플라이")?.근)
        // 사전 근육 — 정해 둔 것 · 없으면 낱말 규칙을 세부 부위로
        assertEquals(mapOf("abs" to "P", "obliques" to "Y"), 종목사전.사전근육(종목사전.이름으로("데드 버그")!!))
        val 벤 = 종목사전.사전근육(종목사전.이름으로("벤치프레스")!!)
        assertEquals("P", 벤["chest_mid"]); assertEquals("S", 벤["triceps"])
        val 데드 = 종목사전.사전근육(종목사전.이름으로("데드리프트")!!)
        assertEquals("P", 데드["glutes"]); assertEquals("Y", 데드["forearm"]); assertEquals("Y", 데드["traps"])
        assertEquals(mapOf("a" to "P", "b" to "Y", "c" to "Y"), 종목사전.둘역할(mapOf("a" to "P", "b" to "S", "c" to "Y")))
        assertTrue(종목사전.사전근육(사전종목("이상한 이름", "없는칸")).isEmpty())
        assertEquals("데드리프트", 종목사전.정확히("컨벤셔널 데드리프트")?.이름)   // 다른 이름으로
        assertEquals("루마니안 데드리프트", 종목사전.정확히("rdl")?.이름)   // 대소문자 무시
        assertEquals("벤치프레스", 종목사전.정확히("벤치 프레스")?.이름)   // 띄어쓰기 무시
    }

    @Test fun 초성() {
        assertEquals("ㅂㅊㅍㄹㅅ", 종목사전.초성글("벤치프레스"))
        assertEquals('A', 종목사전.초성('A'))
        assertEquals("벤치프레스", 종목사전.찾기("ㅂㅊㅍㄹㅅ").first().이름)
        // 'ㅂㅊ' — 벤치프레스 · 벤치 딥스 둘 다 앞에서 맞고 길이도 같다 → 가나다(띄어쓰기가 앞) — 시안 localeCompare 와 같은 차례
        assertEquals(listOf("벤치 딥스", "벤치프레스"), 종목사전.찾기("ㅂㅊ").take(2).map { it.이름 })
        assertEquals("벤치프레스", 종목사전.찾기("벤ㅊㅍ").first().이름)   // 일반 글자 + 초성 섞어서
        assertEquals("벤치프레스", 종목사전.찾기("벤치 프레스").first().이름)   // 띄어쓰기 무시
        assertTrue(종목사전.찾기("ㄷㄷㄹㅍㅌ").any { it.이름 == "데드리프트" })
        // 치는 중 받침 — '벤치플' 은 '벤치프ㄹ' 로도 본다
        assertEquals(listOf("벤치플", "벤치프ㄹ"), 종목사전.검색꼴("벤치플"))
        assertEquals("벤치프레스", 종목사전.찾기("벤치플").first().이름)
        // 다른 이름으로도 (OHP → 오버헤드 프레스)
        assertEquals("오버헤드 프레스", 종목사전.찾기("ohp").first().이름)
        assertEquals("사이드 레터럴 레이즈", 종목사전.찾기("사레레").first().이름)
        // 8개까지 · 빈 검색은 없음
        assertTrue(종목사전.찾기("ㅍ").size <= 8)
        assertTrue(종목사전.찾기("").isEmpty())
        assertTrue(종목사전.찾기("   ").isEmpty())
        assertTrue(종목사전.찾기("ㅋㅋㅋㅋㅋㅋ").isEmpty())
        // 직접 만든 종목도 (같은 이름은 하나만)
        val 표 = listOf(종목("나만의 운동", "팔"), 종목("나만의 운동", "팔", id = "x"), 종목("벤치프레스", "가슴"))
        val 찾 = 종목사전.찾기("ㄴㅁㅇ", 표)
        assertEquals(1, 찾.count { it.이름 == "나만의 운동" })
        assertEquals("팔", 찾.first { it.이름 == "나만의 운동" }.칸)
    }

    // ─────────────── (f) 합치기 검수에서 나온 것 (10-05) ───────────────

    /** 체크한 세트가 하나도 없으면 탭 · 뒤로가기로 나가도 빈 기록을 남기지 않는다 (22 버그 #4) */
    @Test fun 빈운동_저장안함() {
        val r = 루틴("r1", "가슴날", 종목 = listOf(루틴종목("벤치프레스", 세트 = 2)))
        val S = assertNotNull(운동시작(r, 1_000L))
        val d = 앱데이터(루틴들 = listOf(r), 세션 = S).운동저장하기("2026-10-05", 2_000L)
        assertNull(d.세션)
        assertTrue(d.기록.isEmpty())
    }

    /** 플랜 '입력하지 않음'(측정먼저)이 저장 왕복에서 살아남는다 · 옛 꼴(열쇠 없음)은 false */
    @Test fun 플랜_측정먼저_왕복() {
        val d = 앱데이터(플랜들 = listOf(플랜("p", "벤치", "벤치프레스", 0.0, 측정먼저 = true)))
        val e = 저장소.글에서(저장소.글로(d))
        assertTrue(e.플랜들[0].측정먼저)
        val 옛 = 저장소.글로(d).replace("\"측정먼저\": true", "\"측정먼저\": false")
        assertFalse(저장소.글에서(옛).플랜들[0].측정먼저)
    }

    /** 백업 가져오기는 '스키마' 열쇠가 없는 JSON 을 받지 않는다 (전: 아무 JSON 이나 빈 데이터로 받아 통째로 지움) */
    @Test fun 백업아닌_JSON_거절() {
        assertNull(저장소.백업글에서("""{"name":"x"}"""))
        assertNull(저장소.백업글에서("이건 JSON 아님"))
        assertNotNull(저장소.백업글에서(저장소.글로(앱데이터())))
    }

    /** 못 읽는 기록 파일은 빈 데이터로 덮어쓰이기 전에 한 벌 남긴다 */
    @Test fun 깨진파일_한벌남김() {
        val 폴더 = kotlin.io.path.createTempDirectory("hz").toFile()
        val 파일 = java.io.File(폴더, "data.json").apply { writeText("{ 깨진 글") }
        val d = 저장소.읽기(파일)
        assertTrue(d.기록.isEmpty())
        val 남은 = 폴더.listFiles()!!.filter { it.name.startsWith("data.broken-") }
        assertEquals(1, 남은.size)
        assertEquals("{ 깨진 글", 남은[0].readText())
        폴더.deleteRecursively()
    }

    /** 운동 중 ＋로 넣은 종목만 체크해도 저장된다 — 손으로 끝내도 · 3시간 자동 종료도 (10-05 감시관 A) */
    @Test fun 넣은종목만_체크해도_저장() {
        val r = 루틴("r1", "가슴날", 종목 = listOf(루틴종목("벤치프레스", 세트 = 2)))
        val d0 = 앱데이터(종목표 = listOf(종목("벤치프레스", "가슴"), 종목("덤벨컬", "팔")), 루틴들 = listOf(r))
        var S = assertNotNull(운동시작(r, 1_000L))
        S = S.종목넣기(d0.세션줄("덤벨컬"), 0).세션.체크(1, 0, 2_000L)
        val d = d0.copy(세션 = S.끝냄(3_000L)).운동저장하기("2026-10-05", 3_000L)
        assertTrue(d.기록.isNotEmpty(), "넣은 종목 1세트가 버려졌다")
        val d2 = d0.copy(세션 = S.copy(마지막 = 2_000L)).오래된운동정리(2_000L + 4 * 3_600_000L)
        assertTrue(d2.기록.isNotEmpty(), "3시간 자동 종료에서 버려졌다")
    }

    /** 빼기 → 끝내기 → 운동으로 돌아가기 → 되돌리기 (10-05 감시관 B — 재개가 시작시각을 옮기던 것) */
    @Test fun 빼고_끝냈다_돌아와_되돌리기() {
        val S = 운동세션("r", "R", 1_000L, 0, 0, 20.0, 10, listOf(줄("A"), 줄("B", 3, 1)))
        val (z, 뺀) = assertNotNull(S.종목빼기(1, 1))
        val 재 = z.세션.끝냄(5_000L).재개(7_000L)
        assertEquals(1_000L, 재.시작시각)
        assertEquals(2_000L, 재.멈춘)
        assertEquals(5L, 재.흐른초(8_000L))   // (8000 − 시작 1000 − 멈춘 2000) / 1000
        val 되 = 재.종목되돌리기(뺀, 0, 8_000L)
        assertTrue(되.세션.종목들.any { it.이름 == "B" }, "B(1세트 함)가 되살아나지 않는다")
    }

    /** 멈춘 시간이 저장 왕복에서 살아남는다 */
    @Test fun 세션_멈춘_왕복() {
        val S = 운동세션("r", "R", 1_000L, 0, 0, 20.0, 10, listOf(줄("A")), 멈춘 = 12_345L)
        assertEquals(12_345L, 저장소.글에서(저장소.글로(앱데이터(세션 = S))).세션?.멈춘)
    }
}
