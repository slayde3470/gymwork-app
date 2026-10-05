package com.slayde.hasenheide.data

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.math.max

/**
 * 저장 — 앱데이터 전체를 JSON 파일 하나로 폰 안에 둔다.
 *
 * ── 왜 이렇게 단순하게 ──
 *  · 첫 버전은 '폰 안에 저장'으로 정했다 (2026-09-21). 서버(Supabase)는 써보면서 나중에.
 *  · 같은 글자를 '백업 파일'로 내보내고 다시 가져올 수 있다 → 폰을 바꿔도 기록을 옮길 수 있다.
 *  · 안드로이드에 기본으로 들어 있는 org.json 만 쓴다 (따로 받아야 하는 라이브러리가 없다).
 *
 * 쓰는 도중 앱이 꺼져도 파일이 깨지지 않게, 임시 파일에 먼저 쓰고 이름을 바꾼다.
 */
object 저장소 {

    fun 읽기(파일: File): 앱데이터 =
        try { if (파일.exists()) 글에서(파일.readText()) else 앱데이터() } catch (e: Exception) { 앱데이터() }

    fun 쓰기(파일: File, d: 앱데이터) {
        val 임시 = File(파일.parentFile, 파일.name + ".tmp")
        임시.writeText(글로(d))
        if (!임시.renameTo(파일)) { 파일.delete(); 임시.renameTo(파일) }
    }

    // ─────────────── 앱데이터 → 글 ───────────────

    fun 글로(d: 앱데이터): String {
        val o = JSONObject()
        o.put("스키마", 앱데이터.스키마)
        o.put("종목표", JSONArray().also { a -> d.종목표.forEach { a.put(종목to(it)) } })
        o.put("카테고리", JSONArray().also { a -> d.카테고리.forEach { a.put(it) } })
        o.put("루틴들", JSONArray().also { a -> d.루틴들.forEach { a.put(루틴to(it)) } })
        o.put("기록", JSONObject().also { m -> d.기록.forEach { (k, v) -> m.put(k, 날기록to(v)) } })
        o.put("예정", JSONObject().also { m -> d.예정.forEach { (k, v) -> m.put(k, v) } })
        o.put("일정", JSONObject().also { m -> d.일정.forEach { (k, v) -> m.put(k, JSONArray().also { a -> v.forEach { a.put(it) } }) } })
        o.put("설정", 설정to(d.설정))
        o.put("메모", JSONArray().also { a ->
            d.메모.forEach { m ->
                a.put(JSONObject().put("시각", m.시각).put("화면", m.화면).put("글", m.글)
                    .also { o2 -> if (m.흔적.isNotEmpty()) o2.put("흔적", JSONArray().also { x -> m.흔적.forEach { x.put(it) } }) })
            }
        })
        d.세션?.let { o.put("세션", 세션to(it)) }
        d.결과?.let { o.put("결과", 세션to(it)) }   // 스키마 11 (10-01) — 저장 뒤 한 번 보여 줄 결과 화면
        o.put("예정고정", JSONObject().also { m -> d.예정고정.forEach { (k, v) -> m.put(k, v) } })
        o.put("미실시", JSONObject().also { m -> d.미실시.forEach { (k, v) -> m.put(k, v) } })
        o.put("조절", JSONObject().also { m -> d.조절.forEach { (k, v) -> m.put(k, JSONObject().put("볼륨", v.볼륨).put("무게", v.무게).put("세트", v.세트)) } })
        // 운동 플랜 (09-28, 스키마 8)
        o.put("플랜들", JSONArray().also { a -> d.플랜들.forEach { a.put(플랜to(it)) } })
        o.put("몸", JSONObject().put("나이", d.몸.나이).put("남", d.몸.남).put("체중", d.몸.체중))
        // 향상 데이터 (09-30, 스키마 10 · 21 문서 5절) — 이 줄들이 있어야 속도표를 다시 짤 수 있다
        o.put("향상기록들", JSONArray().also { a -> d.향상기록들.forEach { a.put(향상to(it)) } })
        // 근육 피로 (10-02, 스키마 13 · 07 근육지도 4·5절) — 잎 id → [lv0, 시작, 끝] · 잎 id → 이전 최대 볼륨
        o.put("피로", JSONObject().also { m -> d.피로.forEach { (k, f) -> m.put(k, JSONArray().put(f.lv0).put(f.시작).put(f.끝)) } })
        o.put("최대볼륨", JSONObject().also { m -> d.최대볼륨.forEach { (k, v) -> m.put(k, v) } })
        // 스탯 · 업적 (10-02, 스키마 14 · 스탯명세 7-2)
        o.put("업적", JSONObject().also { m -> d.업적.forEach { (k, v) -> m.put(k, v) } })
        d.대표칭호?.let { o.put("대표칭호", it) }
        o.put("세기", JSONObject().also { m -> d.세기.forEach { (k, v) -> m.put(k, v) } })
        o.put("쉰날", JSONArray().also { a -> d.쉰날.sorted().forEach { a.put(it) } })
        o.put("건너뜀", JSONObject().also { m -> d.건너뜀.forEach { (k, v) -> m.put(k, v) } })
        o.put("체중기록", JSONArray().also { a -> d.체중기록.forEach { w -> a.put(JSONArray().put(w.시각).put(w.kg)) } })
        o.put("스탯기록", JSONObject().also { m -> d.스탯기록.forEach { (날, 값) -> m.put(날, JSONObject().also { x -> 값.forEach { (k, v) -> x.put(k, v) } }) } })
        // 스키마 15 (10-05) — 종목설정 (열쇠 → [[w, r, 휴], …]) · 업적 순서 · 숨김 · 인증샷 ([파일, 때, 고정])
        o.put("종목설정", JSONObject().also { m -> d.종목설정.forEach { (k, l) -> m.put(k, JSONArray().also { a -> l.forEach { x -> a.put(JSONArray().put(x.w).put(x.r).put(x.휴)) } }) } })
        o.put("업적순서", JSONArray().also { a -> d.업적순서.forEach { a.put(it) } })
        o.put("업적숨김", JSONArray().also { a -> d.업적숨김.sorted().forEach { a.put(it) } })
        o.put("인증샷", JSONArray().also { a -> d.인증샷.forEach { x -> a.put(JSONArray().put(x.파일).put(x.때).put(x.고정)) } })
        return o.toString(1)
    }

    private fun 종목to(e: 종목) = JSONObject().put("이름", e.이름).put("부위", e.부위).put("장비", e.장비)
        .also { if (e.달력이름 != null) it.put("달력이름", e.달력이름) }
        .also { if (e.참고url != null) it.put("참고url", e.참고url) }
        .also { if (e.참고글 != null) it.put("참고글", e.참고글) }
        .also { if (e.목표1RM != null) it.put("목표1RM", e.목표1RM) }
        // 10-02 (스키마 13): 종목 사진 — 파일 **이름만** 적는다. 사진 파일은 백업에 들어가지 않는다 (filesDir/photos 에 그대로)
        .also { if (e.사진.isNotEmpty()) it.put("사진", JSONArray().also { a -> e.사진.forEach { a.put(it) } }) }
        // 스키마 15 (10-05): 종목 id · 근육 역할 (P · Y)
        .put("id", e.id)
        .also { if (e.근육.isNotEmpty()) it.put("근육", JSONObject().also { m -> e.근육.forEach { (k, v) -> m.put(k, v) } }) }

    private fun 루틴종목to(e: 루틴종목) = JSONObject().put("이름", e.이름).put("세트", e.세트).put("무게", e.무게)
        .put("횟수", e.횟수).put("휴식", e.휴식).also { if (e.슈퍼 != null) it.put("슈퍼", e.슈퍼) }
        .also { if (e.세트값.isNotEmpty()) it.put("세트값", 세트들to(e.세트값)) }
        .also { if (e.휴식값.isNotEmpty()) it.put("휴식값", JSONArray().also { h -> e.휴식값.forEach { h.put(it) } }) }
        .also { if (e.플랜id != null) it.put("플랜id", e.플랜id) }
        .also { if (e.종id != null) it.put("종id", e.종id) }

    private fun 루틴to(r: 루틴) = JSONObject().put("id", r.id).put("이름", r.이름).put("휴식일", r.휴식일).put("자동생성", r.자동생성)
        .put("종목", JSONArray().also { a -> r.종목.forEach { a.put(루틴종목to(it)) } })

    // 종류는 본운동(0) 이 아닐 때만 적는다 — 옛 파일과 모양이 같아 읽는 쪽이 안 깨진다
    private fun 세트to(s: 세트) = JSONArray().put(s.w).put(s.r).also { if (s.종류 != 0) it.put(s.종류) }

    private fun 세트들to(l: List<세트?>) = JSONArray().also { a -> l.forEach { a.put(if (it == null) JSONObject.NULL else 세트to(it)) } }

    private fun 날기록to(r: 날기록) = JSONObject().put("루틴id", r.루틴id).put("루틴이름", r.루틴이름).put("달성", r.달성)
        .put("걸린초", r.걸린초)
        .also { if (r.시작시각 > 0) it.put("시작시각", r.시작시각).put("끝시각", r.끝시각) }
        .put("종목들", JSONArray().also { a ->
            r.종목들.forEach { e ->
                a.put(JSONObject().put("이름", e.이름).put("세트들", 세트들to(e.세트들)).put("임시", e.임시)
                    .also { if (e.묶음 != null) it.put("묶음", e.묶음) }
                    .also { if (e.플랜id != null) it.put("플랜id", e.플랜id) }
                    .also { if (e.종id != null) it.put("종id", e.종id) })
            }
        })

    // 스키마 9 (09-29 재설계) — 회 기준 · 목표 직접 입력 · 훈련 방식
    private fun 플랜to(p: 플랜) = JSONObject().put("id", p.id).put("이름", p.이름).put("종목", p.종목)
        .put("시작1RM", p.시작1RM).put("목표방식", p.목표방식.name).put("목표무게", p.목표무게).put("목표횟수", p.목표횟수)
        .put("주당", p.주당).put("방식", p.방식번호).put("강도", p.강도)
        // 홍겸 님 09-30 — 세트·횟수를 직접 정한 값 (0 이면 강도 프리셋)
        .put("세트수", p.세트수).put("직접횟수", p.직접횟수)
        // 맨몸 3종 (21 문서)
        .put("단위", p.단위.name).put("목표개수", p.목표개수).put("시작개수", p.시작개수)
        .put("보조모드", p.보조모드).put("보조무게", p.보조무게)
        .put("만든날", p.만든날).put("한회", p.한회).put("누적볼륨", p.누적볼륨)
        .put("워밍업수", p.워밍업수).put("켬", p.켬)
        .put("측정들", JSONArray().also { a ->
            p.측정들.forEach { m -> a.put(JSONObject().put("회", m.회).put("날", m.날).put("무게", m.무게).put("횟수", m.횟수)) }
        })
        // 스키마 12 (10-01) — 매 회차 자기조절의 출발점
        .also { o -> p.재기준?.let { r -> o.put("재기준", JSONObject().put("회", r.회).put("날", r.날).put("값", r.값)) } }

    // 스키마 10 (09-30, 21 문서 5절)
    private fun 향상to(g: 향상기록) = JSONObject().put("날짜", g.날짜).put("종목", g.종목)
        .put("측정값", g.측정값).put("체중", g.체중).put("유효부하", g.유효부하)
        .put("누적횟수", g.누적횟수).put("운동일수", g.운동일수).put("예상값", g.예상값)

    private fun 향상from(o: JSONObject) = 향상기록(
        날짜 = o.optString("날짜"), 종목 = o.optString("종목"),
        측정값 = o.optDouble("측정값", 0.0), 체중 = o.optDouble("체중", 0.0),
        유효부하 = o.optDouble("유효부하", 0.0), 누적횟수 = o.optInt("누적횟수", 0),
        운동일수 = o.optInt("운동일수", 0), 예상값 = o.optDouble("예상값", 0.0),
    )

    private fun 설정to(s: 설정값) = JSONObject().put("자동진행", s.자동진행).put("넘어가기전확인", s.넘어가기전확인)
        .put("소리진동", s.소리진동).put("화면유지", s.화면유지).put("무게폭", s.무게폭)
        .put("기본휴식", s.기본휴식).put("기준", s.기준).put("기본세트", s.기본세트)
        .put("볼륨켬", s.볼륨켬).put("볼륨방식", s.볼륨방식).put("볼륨값", s.볼륨값)
        .put("볼륨언제", s.볼륨언제).put("볼륨배분", s.볼륨배분).put("횟수상한", s.횟수상한)
        .put("진동세기", s.진동세기).put("진동시간", s.진동시간).put("번호보기", s.번호보기)
        .put("배너", s.배너).put("회복시간", s.회복시간).put("색표", s.색표)   // 10-02 (스키마 13) 운동 중 그림
        // 10-05 (스키마 15) 프로필 · 보고서 — 시안 S.설정 의 이름 그대로
        .put("닉네임", s.닉네임).also { o -> s.프로필사진?.let { o.put("프로필사진", it) } }
        .put("링크", JSONArray().also { a -> s.링크.forEach { a.put(it) } })
        .put("보고서보임", JSONObject().put("프로필", s.보고서보임.프로필).put("루틴", s.보고서보임.루틴))
        .put("큰운동추가", JSONArray().also { a -> s.큰운동추가.forEach { a.put(it) } })
        .put("업적정렬", s.업적정렬)

    private fun 세션to(S: 운동세션) = JSONObject().put("루틴id", S.루틴id).put("루틴이름", S.루틴이름)
        .put("시작시각", S.시작시각).put("i", S.i).put("s", S.s).put("무게", S.무게).put("횟수", S.횟수)
        .put("끝화면", S.끝화면).put("마지막", S.마지막).put("조절됨", S.조절됨)
        .also { o -> if (S.끝시각 != null) o.put("끝시각", S.끝시각) }
        .put("종목들", JSONArray().also { a ->
            S.종목들.forEach { e ->
                a.put(JSONObject().put("이름", e.이름).put("세트", e.세트).put("계획세트", e.계획세트)
                    .put("무게", e.무게).put("횟수", e.횟수).put("휴식", e.휴식)
                    .put("기록", 세트들to(e.기록)).put("예정값", 세트들to(e.예정값))
                    .put("휴식들", JSONArray().also { h -> e.휴식들.forEach { h.put(it ?: JSONObject.NULL) } })
                    .put("임시", e.임시).put("마감", e.마감).also { if (e.슈퍼 != null) it.put("슈퍼", e.슈퍼) }
                    .also { if (e.플랜id != null) it.put("플랜id", e.플랜id) }
                    .also { if (e.종id != null) it.put("종id", e.종id) })
            }
        })
        .also { o ->
            S.휴식?.let { h ->
                o.put("휴식", JSONObject().put("k", h.k).put("끝시각", h.끝시각).put("물음", h.물음).put("총초", h.총초).put("종목", h.종목)
                    .also { if (h.다음i != null) it.put("다음i", h.다음i) }
                    .also { if (h.다음s != null) it.put("다음s", h.다음s) })
            }
        }

    // ─────────────── 글 → 앱데이터 ───────────────

    fun 글에서(글: String): 앱데이터 {
        val o = JSONObject(글)
        val 판 = o.optInt("스키마", 1)
        return 앱데이터(
            종목표 = 목록(o.optJSONArray("종목표")) { a, i ->
                a.getJSONObject(i).let { 종목(it.getString("이름"), it.optString("부위"), it.optString("장비"), 글또는널(it, "참고글"), 글또는널(it, "참고url"), 글또는널(it, "달력이름"),
                    if (it.has("목표1RM") && !it.isNull("목표1RM")) it.getDouble("목표1RM") else null)
                    .copy(사진 = 목록(it.optJSONArray("사진")) { x, j -> x.getString(j) },   // 스키마 13 (10-02) — 옛 파일은 빈 목록
                        // 스키마 15 (10-05) — 옛 파일은 id = 이름 · 역할 없음. 옛 "S"(보조) 는 "Y"(협응) 로, 모르는 값은 버린다
                        id = 글또는널(it, "id")?.takeIf { x -> x.isNotBlank() } ?: it.getString("이름"),
                        근육 = 역할읽기(it.optJSONObject("근육"))) }
            }.id겹침풀기(),
            카테고리 = if (o.has("카테고리")) 목록(o.optJSONArray("카테고리")) { a, i -> a.getString(i) } else 앱데이터.기본카테고리,
            루틴들 = 목록(o.optJSONArray("루틴들")) { a, i -> 루틴from(a.getJSONObject(i)) },
            기록 = 사전(o.optJSONObject("기록")) { m, k -> 날기록from(m.getJSONObject(k)) },
            예정 = 사전(o.optJSONObject("예정")) { m, k -> m.getString(k) },
            일정 = 사전(o.optJSONObject("일정")) { m, k -> 목록(m.optJSONArray(k)) { a, i -> a.getString(i) } },
            설정 = o.optJSONObject("설정")?.let { 설정from(it, 판) } ?: 설정값(),
            메모 = 목록(o.optJSONArray("메모")) { a, i ->
                a.getJSONObject(i).let { m ->
                    수정메모(m.optLong("시각"), m.optString("화면"), m.optString("글"),
                        목록(m.optJSONArray("흔적")) { x, j -> x.getString(j) })
                }
            },
            세션 = o.optJSONObject("세션")?.let { 세션from(it) },
            결과 = o.optJSONObject("결과")?.let { 세션from(it) },
            예정고정 = 사전(o.optJSONObject("예정고정")) { m, k -> m.getString(k) },
            미실시 = 사전(o.optJSONObject("미실시")) { m, k -> m.getString(k) },
            조절 = 사전(o.optJSONObject("조절")) { m, k -> m.getJSONObject(k).let { j -> 오늘조절(j.optInt("볼륨", 100), j.optDouble("무게", 0.0), j.optInt("세트", 0)) } },
            // 스키마 8 — 옛 파일에는 없다 → 빈 목록 · 기본 몸조건 (시험으로 확인)
            플랜들 = 목록(o.optJSONArray("플랜들")) { a, i -> 플랜from(a.getJSONObject(i)) },
            // 09-29: 기본값을 비워 두었다. 비면 화면이 "설정에서 넣어 주세요" 로 안내한다 (01 ⑳)
            몸 = o.optJSONObject("몸")?.let { j -> 몸조건(j.optInt("나이", 0), j.optBoolean("남", true), j.optDouble("체중", 0.0)) } ?: 몸조건(),
            // 스키마 10 — 옛 파일에는 없다 → 빈 목록 (기본표를 쓴다)
            향상기록들 = 목록(o.optJSONArray("향상기록들")) { a, i -> 향상from(a.getJSONObject(i)) },
            // 스키마 13 (10-02) — 옛 파일에는 없다 → 빈 피로 · 빈 최대 (처음 운동하는 것처럼)
            피로 = 사전(o.optJSONObject("피로")) { m, k -> m.getJSONArray(k).let { a -> 피로상태(a.optDouble(0, 0.0), a.optLong(1, 0L), a.optLong(2, 0L)) } },
            최대볼륨 = 사전(o.optJSONObject("최대볼륨")) { m, k -> m.optDouble(k, 0.0) },
            // 스키마 14 (10-02) — 옛 파일에는 없다 → 빈 업적 · 대표 칭호 없음 · 빈 기록 (다음 판정 때 지난 기록으로 소급한다)
            업적 = 사전(o.optJSONObject("업적")) { m, k -> m.optLong(k, 0L) }.filterValues { it > 0 },
            대표칭호 = 글또는널(o, "대표칭호"),
            세기 = 사전(o.optJSONObject("세기")) { m, k -> m.optInt(k, 0) },
            쉰날 = 목록(o.optJSONArray("쉰날")) { a, i -> a.getString(i) }.toSet(),
            건너뜀 = 사전(o.optJSONObject("건너뜀")) { m, k -> m.getString(k) },
            체중기록 = 목록(o.optJSONArray("체중기록")) { a, i -> a.getJSONArray(i).let { w -> 체중값(w.optLong(0, 0L), w.optDouble(1, 0.0)) } }.filter { it.kg > 0 },
            스탯기록 = 사전(o.optJSONObject("스탯기록")) { m, k -> 사전(m.getJSONObject(k)) { x, s -> x.optDouble(s, 0.0) } },
            // 스키마 15 (10-05) — 옛 파일에는 없다 → 빈 값 (종목설정이 없으면 설정의 기본 세트로)
            종목설정 = 사전(o.optJSONObject("종목설정")) { m, k -> 종목설정읽기(m.opt(k), 설정값0(o, 판).기본휴식) }.filterValues { it.isNotEmpty() },
            업적순서 = 목록(o.optJSONArray("업적순서")) { a, i -> a.optString(i, "") }.filter { it.isNotBlank() },
            업적숨김 = 목록(o.optJSONArray("업적숨김")) { a, i -> a.optString(i, "") }.filter { it.isNotBlank() }.toSet()
                + 사전(o.optJSONObject("업적숨김")) { m, k -> m.optBoolean(k, false) }.filterValues { it }.keys,   // 시안 꼴 {번호: true} 도
            인증샷 = 목록(o.optJSONArray("인증샷")) { a, i -> a.optJSONArray(i)?.let { x -> 인증사진(x.optString(0, ""), x.optLong(1, 0L), x.optLong(2, 0L).coerceAtLeast(0L)) } }
                .filterNotNull().filter { it.파일.isNotBlank() }.let { 인증순(it).take(인증최대) },
        ).플랜줄정리()   // 10-01: 지운 플랜의 줄 · 슈퍼세트로 묶인 플랜 줄을 풀어 둔다
    }

    private fun <T> 목록(a: JSONArray?, f: (JSONArray, Int) -> T): List<T> =
        if (a == null) emptyList() else (0 until a.length()).map { f(a, it) }

    private fun <T> 사전(m: JSONObject?, f: (JSONObject, String) -> T): Map<String, T> {
        if (m == null) return emptyMap()
        val 결과 = sortedMapOf<String, T>()
        val 열쇠 = m.keys()
        while (열쇠.hasNext()) { val k = 열쇠.next(); 결과[k] = f(m, k) }
        return 결과
    }

    /** 근육 역할 맵 — P 는 P, S(옛 보조) · Y 는 Y, 그 밖의 값 · 빈 열쇠는 버린다 (스키마 15) */
    private fun 역할읽기(m: JSONObject?): Map<String, String> =
        사전(m) { x, k -> x.optString(k, "") }.filter { it.key.isNotBlank() }
            .mapNotNull { (k, v) -> when (v) { "P" -> k to "P"; "S", "Y" -> k to "Y"; else -> null } }.toMap()

    /**
     * 종목설정 한 칸 — 새 꼴 [[w, r, 휴], …] · 시안 꼴 {세트: [{w, r, 휴}…]} · 시안 v17 옛 꼴 {세트: n, w, r, 휴} (n 줄로)
     * 무게는 0 이상 · 횟수 1 이상 · 휴식 0 이하는 기본 휴식. 10줄까지
     */
    private fun 종목설정읽기(v: Any?, 기본휴식: Int): List<종목세트> {
        fun 줄(w: Double, r: Int, 휴: Int) = 종목세트(if (w.isNaN() || w < 0) 0.0 else w, max(1, r), if (휴 > 0) 휴 else 기본휴식)
        val l: List<종목세트> = when (v) {
            is JSONArray -> 목록(v) { a, i ->
                when (val x = a.opt(i)) {
                    is JSONArray -> 줄(x.optDouble(0, 0.0), x.optInt(1, 1), x.optInt(2, 0))
                    is JSONObject -> 줄(x.optDouble("w", 0.0), x.optInt("r", 1), x.optInt("휴", 0))
                    else -> null
                }
            }.filterNotNull()
            is JSONObject -> when (val 세 = v.opt("세트")) {
                is JSONArray -> 종목설정읽기(세, 기본휴식)
                is Number -> 옛종목설정(세.toInt(), if (v.has("w")) v.optDouble("w", 20.0) else null,
                    if (v.has("r")) v.optInt("r", 10) else null, if (v.has("휴")) v.optInt("휴", 기본휴식) else null, 기본휴식)
                    .map { 줄(it.w, it.r, it.휴) }
                else -> emptyList()
            }
            else -> emptyList()
        }
        return l.take(종목세트최대)
    }

    /** 종목설정을 읽을 때 기본 휴식이 필요해서 — 설정만 먼저 읽는다 */
    private fun 설정값0(o: JSONObject, 판: Int): 설정값 = o.optJSONObject("설정")?.let { 설정from(it, 판) } ?: 설정값()

    /** 같은 id 가 둘 이상이면 뒤의 것에 "-2" … 를 붙인다 — 옛 파일의 같은 이름 종목(id = 이름)이 서로 다른 종목으로 남게 */
    private fun List<종목>.id겹침풀기(): List<종목> {
        val 쓴 = HashSet<String>()
        val 모두 = map { it.id }.toHashSet()
        return map { e ->
            if (쓴.add(e.id)) e
            else {
                var n = 2
                while ("${e.id}-$n" in 모두 || "${e.id}-$n" in 쓴) n++
                val id = "${e.id}-$n"; 쓴.add(id); e.copy(id = id)
            }
        }
    }

    private fun 글또는널(o: JSONObject, k: String): String? = if (o.has(k) && !o.isNull(k)) o.getString(k) else null
    private fun 수또는널(o: JSONObject, k: String): Int? = if (o.has(k) && !o.isNull(k)) o.getInt(k) else null

    private fun 루틴from(o: JSONObject) = 루틴(
        o.getString("id"), o.getString("이름"), o.optBoolean("휴식일"),
        목록(o.optJSONArray("종목")) { a, i ->
            a.getJSONObject(i).let {
                루틴종목(it.getString("이름"), it.optInt("세트", 3), it.optDouble("무게", 20.0), it.optInt("횟수", 10),
                    it.optInt("휴식", 90), 글또는널(it, "슈퍼"),
                    세트들from(it.optJSONArray("세트값")).filterNotNull(),
                    목록(it.optJSONArray("휴식값")) { h, j -> h.getInt(j) },
                    글또는널(it, "플랜id"), 종id = 글또는널(it, "종id"))
            }
        },
        // 스키마 5 까지는 모든 루틴이 캘린더에 깔렸다 → 옛 루틴은 켜진 채로 옮긴다 (달력이 갑자기 비지 않게)
        자동생성 = o.optBoolean("자동생성", true),
    )

    /**
     * 스키마 8(주 기준) 의 플랜도 읽는다 — 그때 표에 박혀 있던 목표를 그대로 옮긴다.
     * 옛 플랜은 목표를 표에서 받았지만 지금은 플랜마다 따로 가지므로, 여기서 한 번 옮겨 준다.
     */
    private val 옛목표 = mapOf(
        "벤치프레스" to (100.0 to 12), "백 스쿼트" to (140.0 to 10), "데드리프트" to (220.0 to 5),
        "오버헤드 프레스" to (100.0 to 5), "펜들레이 로우" to (140.0 to 5),
    )

    private fun 플랜from(o: JSONObject): 플랜 {
        val 종목이름 = o.getString("종목")
        val 옛 = !o.has("id")
        val 옛목 = 옛목표[종목이름] ?: (0.0 to 1)
        return 플랜(
            id = o.optString("id", "").ifBlank { "p" + System.nanoTime() + 종목이름.hashCode() },
            이름 = o.optString("이름", "").ifBlank { 종목이름 },
            종목 = 종목이름,
            시작1RM = if (옛) 일RM(o.optDouble("시작무게", 20.0), o.optInt("시작횟수", 1)) else o.optDouble("시작1RM", 0.0),
            목표방식 = if (옛) 목표형식.무게횟수 else (목표형식.entries.firstOrNull { it.name == o.optString("목표방식") } ?: 목표형식.RM),
            목표무게 = if (옛) 옛목.first else o.optDouble("목표무게", 0.0),
            목표횟수 = if (옛) 옛목.second else o.optInt("목표횟수", 1),
            주당 = o.optInt("주당", 플랜표.표준주당),
            방식번호 = o.optInt("방식", 1),
            강도 = o.optInt("강도", 1),
            만든날 = o.optString("만든날"),
            한회 = o.optInt("한회", 0),
            누적볼륨 = o.optDouble("누적볼륨", 0.0),
            측정들 = 목록(o.optJSONArray("측정들")) { a, i ->
                a.getJSONObject(i).let {
                    // 옛 파일은 '주' 로 적혀 있다 — 회로 읽되 주당 횟수를 모르니 그대로 둔다
                    측정(if (it.has("회")) it.optInt("회") else it.optInt("주"), it.optString("날"), it.optDouble("무게"), it.optInt("횟수"))
                }
            },
            워밍업수 = o.optInt("워밍업수", 0),
            켬 = o.optBoolean("켬", true),
            재기준 = o.optJSONObject("재기준")?.let { r -> 재기준점(r.optInt("회"), r.optString("날"), r.optDouble("값", 0.0)) }?.takeIf { it.값 > 0 },
            // 홍겸 님 09-30 — 없으면 0 (강도 프리셋을 따른다)
            세트수 = o.optInt("세트수", 0),
            직접횟수 = o.optInt("직접횟수", 0),
            // 맨몸 3종 (21 문서) — 옛 플랜은 무게 종목이다
            단위 = 목표단위.entries.firstOrNull { it.name == o.optString("단위") }
                ?: (플랜표.찾기(종목이름)?.기본단위 ?: 목표단위.무게),
            목표개수 = o.optDouble("목표개수", 0.0),
            시작개수 = o.optDouble("시작개수", 0.0),
            보조모드 = o.optInt("보조모드", 0),
            보조무게 = o.optDouble("보조무게", 0.0),
        )
    }

    private fun 세트from(a: JSONArray) = 세트(a.getDouble(0), a.getInt(1), if (a.length() > 2) a.optInt(2, 0) else 0)

    private fun 세트들from(a: JSONArray?): List<세트?> =
        목록(a) { x, i -> if (x.isNull(i)) null else 세트from(x.getJSONArray(i)) }

    private fun 날기록from(o: JSONObject) = 날기록(
        o.getString("루틴id"), o.optString("루틴이름"), o.optBoolean("달성"),
        목록(o.optJSONArray("종목들")) { a, i ->
            a.getJSONObject(i).let { e ->
                종목기록(e.getString("이름"), 세트들from(e.optJSONArray("세트들")).filterNotNull(), e.optBoolean("임시"), 글또는널(e, "묶음"),
                    플랜id = 글또는널(e, "플랜id"), 종id = 글또는널(e, "종id"))
            }
        },
        o.optInt("걸린초"),
        o.optLong("시작시각", 0L), o.optLong("끝시각", 0L),
    )

    /** 스키마 1(v0.2) 의 무게폭 2.5 는 고른 값이 아니라 기본값이었다 → 새 기본값 1 로 (09-21 메모) */
    private fun 설정from(o: JSONObject, 판: Int): 설정값 {
        // 09-25 메모: 고를 수 있는 값이 바뀌었다 — 목록에 없는 값(1.25 등)은 기본값으로
        val 폭 = o.optDouble("무게폭", 1.0).let { if (판 < 2 && it == 2.5) 1.0 else it }.let { if (it in 무게폭목록) it else 1.0 }
        return 설정값(
            자동진행 = o.optBoolean("자동진행", true), 넘어가기전확인 = o.optBoolean("넘어가기전확인", false),
            소리진동 = o.optBoolean("소리진동", true), 화면유지 = o.optBoolean("화면유지", true), 무게폭 = 폭,
            // 09-24: 기본 휴식은 1분으로. 옛 판의 90초는 60초로 옮긴다
            기본휴식 = (if (판 < 4) 60 else o.optInt("기본휴식", 60)).let { if (it in 기본휴식목록) it else 60 },
            기본세트 = (if (판 < 3) 1 else o.optInt("기본세트", 1)).coerceIn(1, 5),
            볼륨켬 = o.optBoolean("볼륨켬", false),
            볼륨방식 = o.optString("볼륨방식", "%").ifBlank { "%" },
            볼륨값 = o.optDouble("볼륨값", 2.5),
            볼륨언제 = o.optString("볼륨언제", "성공").ifBlank { "성공" },
            볼륨배분 = o.optString("볼륨배분", "횟수").ifBlank { "횟수" },
            횟수상한 = o.optInt("횟수상한", 12),
            기준 = o.optInt("기준", 3),
            진동세기 = o.optInt("진동세기", 2).coerceIn(1, 3),
            진동시간 = o.optInt("진동시간", 1000).let { if (it in 진동시간목록) it else 1000 },
            번호보기 = o.optBoolean("번호보기", true),
            // 10-02 (스키마 13) — 옛 파일 · 목록에 없는 값은 기본값
            배너 = o.optString("배너", "근육 2장").let { if (it in 근육표.배너목록) it else "근육 2장" },
            회복시간 = o.optInt("회복시간", 24).let { if (it in 근육표.회복시간목록) it else 24 },
            색표 = o.optString("색표", "heat").let { v -> if (근육표.색표목록.any { it.first == v }) v else "heat" },
            // 10-05 (스키마 15) — 시안 S.설정 이름 · 기본값 그대로. 옛 꼴(보고서프로필끔 · 큰운동 4/5)도 옮긴다 (시안 보고설정)
            닉네임 = o.optString("닉네임", "").take(닉네임최대),
            프로필사진 = 글또는널(o, "프로필사진")?.takeIf { it.isNotBlank() },
            링크 = 목록(o.optJSONArray("링크")) { a, i -> 링크주소(a.optString(i, "")) }.filter { it.isNotEmpty() },
            보고서보임 = 보고서보임값(o.optJSONObject("보고서보임")?.optBoolean("프로필", true) ?: !o.optBoolean("보고서프로필끔", false), 루틴 = true),
            큰운동추가 = 큰운동추가정리(
                if (o.has("큰운동추가")) 목록(o.optJSONArray("큰운동추가")) { a, i -> a.optString(i, "") }
                else when (o.optInt("큰운동", 0)) { 5 -> listOf("오버헤드 프레스", "바벨 로우"); 4 -> listOf("오버헤드 프레스"); else -> emptyList() }),
            업적정렬 = o.optString("업적정렬", "최신순").let { if (it in 업적정렬목록) it else "최신순" },
        )
    }

    private fun 세션from(o: JSONObject) = 운동세션(
        o.getString("루틴id"), o.optString("루틴이름"), o.optLong("시작시각"), o.optInt("i"), o.optInt("s"),
        o.optDouble("무게", 0.0), o.optInt("횟수"),
        목록(o.optJSONArray("종목들")) { a, i ->
            a.getJSONObject(i).let { e ->
                세션종목(
                    e.getString("이름"), e.optInt("세트"), e.optInt("계획세트"), e.optDouble("무게", 0.0), e.optInt("횟수"),
                    e.optInt("휴식", 90), 세트들from(e.optJSONArray("기록")), 세트들from(e.optJSONArray("예정값")),
                    목록(e.optJSONArray("휴식들")) { h, j -> if (h.isNull(j)) null else h.getInt(j) },
                    e.optBoolean("임시"), e.optBoolean("마감"), 글또는널(e, "슈퍼"),
                    플랜id = 글또는널(e, "플랜id"), 종id = 글또는널(e, "종id"),
                )
            }
        },
        o.optJSONObject("휴식")?.let { h -> 휴식중(h.getInt("k"), h.getLong("끝시각"), h.optBoolean("물음"), 수또는널(h, "다음i"), 수또는널(h, "다음s"), h.optInt("총초", 0), h.optInt("종목", -1)) },
        o.optBoolean("끝화면"),
        if (o.has("끝시각") && !o.isNull("끝시각")) o.getLong("끝시각") else null,
        o.optLong("마지막", 0L),
        o.optBoolean("조절됨", false),
    )
}
