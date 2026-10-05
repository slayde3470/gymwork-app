package com.slayde.hasenheide.data

import java.text.Collator
import java.util.Locale

/**
 * 종목 사전 (10-05 · 앱 옮기기 1단계 · 시안 v18 C ⑤ `종목사전글`) — 새 종목 시트의 초성 검색에 쓴다.
 *
 * 시안의 129줄을 **그대로** 옮겼다 (이름 · 칸 · 다른 이름 · 근육). 시안 주석:
 *   "[확인 필요] 표준 종목명 · 근육 배정은 Claude 초안 (한국 헬스장에서 흔히 쓰는 표기). 출처 확인 전."
 *  · 칸 = 종목 카테고리 — 가슴 · 등 · 하체 · 어깨 · 팔 · 맨몸. 복근 종목은 '맨몸' (시안 카테고리에 '복근' 이 없다)
 *  · 근 = 세부 부위 id → 역할 (P 주동 · S 보조 · Y 협응). null 이면 낱말 규칙 → 칸 기본값을 세부 부위로 옮긴 것 ([종목사전.사전근육])
 *  · 새 종목 시트에 들일 때는 [종목사전.둘역할] 로 S → Y (v20 ⑤ 홍겸 님 "주동근이랑 협응근으로만")
 *
 * 이 파일은 안드로이드를 쓰지 않는다 → 시험(PortTest.kt)이 컴퓨터에서 돈다.
 */
data class 사전종목(
    val 이름: String,
    val 칸: String,
    /** 다른 이름 — 검색에만 쓴다 */
    val 별: List<String> = emptyList(),
    /** 세부 부위 id → 역할. null = 정해 두지 않음 → [종목사전.사전근육] 이 낱말 규칙으로 채운다 */
    val 근: Map<String, String>? = null,
)

object 종목사전 {

    val 목록: List<사전종목> = listOf(
        사전종목("벤치프레스", "가슴", listOf("바벨 벤치프레스", "플랫 벤치", "벤치")),
        사전종목("인클라인 벤치프레스", "가슴", listOf("인클라인 바벨 프레스")),
        사전종목("디클라인 벤치프레스", "가슴", listOf("디클라인 바벨 프레스")),
        사전종목("덤벨 벤치프레스", "가슴", listOf("덤벨 프레스")),
        사전종목("인클라인 덤벨 프레스", "가슴", listOf("인클라인 덤벨 벤치프레스")),
        사전종목("디클라인 덤벨 프레스", "가슴"),
        사전종목("스미스 머신 벤치프레스", "가슴", listOf("스미스 벤치")),
        사전종목("스미스 머신 인클라인 프레스", "가슴", listOf("스미스 인클라인")),
        사전종목("체스트 프레스 머신", "가슴", listOf("머신 체스트 프레스", "체스트 프레스")),
        사전종목("인클라인 체스트 프레스 머신", "가슴", listOf("머신 인클라인 프레스")),
        사전종목("케이블 체스트 프레스", "가슴"),
        사전종목("덤벨 플라이", "가슴", listOf("플랫 덤벨 플라이")),
        사전종목("인클라인 덤벨 플라이", "가슴", emptyList(), mapOf("chest_upper" to "P", "chest_mid" to "S", "delt_front" to "Y")),
        사전종목("펙 덱 플라이", "가슴", listOf("펙덱", "버터플라이", "머신 플라이")),
        사전종목("케이블 크로스오버", "가슴", listOf("케이블 플라이")),
        사전종목("로우 투 하이 케이블 플라이", "가슴", listOf("로우 케이블 플라이"), mapOf("chest_upper" to "P", "chest_mid" to "S", "delt_front" to "Y")),
        사전종목("덤벨 풀오버", "가슴", listOf("풀오버"), mapOf("chest_mid" to "P", "lats" to "S", "serratus" to "S", "triceps" to "Y")),
        사전종목("플로어 프레스", "가슴", listOf("바벨 플로어 프레스"), mapOf("chest_mid" to "P", "triceps" to "S", "delt_front" to "Y")),
        사전종목("턱걸이", "등", listOf("풀업", "와이드 그립 풀업")),
        사전종목("친업", "등", listOf("언더그립 턱걸이", "친 업")),
        사전종목("어시스티드 풀업 머신", "등", listOf("어시스트 풀업", "어시스티드 턱걸이")),
        사전종목("랫풀다운", "등", listOf("랫 풀다운", "와이드 그립 랫풀다운")),
        사전종목("클로즈그립 랫풀다운", "등", listOf("V바 랫풀다운", "클로즈 그립 랫풀다운")),
        사전종목("언더그립 랫풀다운", "등", listOf("리버스 그립 랫풀다운")),
        사전종목("스트레이트 암 풀다운", "등", listOf("암 풀다운", "스트레이트 암 케이블 풀다운"), mapOf("lats" to "P", "teres_major" to "S", "triceps" to "Y")),
        사전종목("바벨 로우", "등", listOf("벤트오버 로우", "벤트 오버 바벨 로우")),
        사전종목("펜들레이 로우", "등"),
        사전종목("덤벨 로우", "등", listOf("원암 덤벨 로우", "원 암 로우")),
        사전종목("시티드 케이블 로우", "등", listOf("시티드 로우", "케이블 로우")),
        사전종목("T바 로우", "등", listOf("티바 로우")),
        사전종목("머신 로우", "등", listOf("시티드 로우 머신", "로우 머신")),
        사전종목("체스트 서포티드 로우", "등", listOf("인클라인 벤치 로우")),
        사전종목("랙 풀", "등", listOf("랙풀"), mapOf("lower_back" to "P", "glutes" to "P", "traps" to "S", "hamstrings" to "S", "lats" to "Y", "forearm" to "Y")),
        사전종목("바벨 슈러그", "등", listOf("슈러그")),
        사전종목("덤벨 슈러그", "등"),
        사전종목("백 익스텐션", "등", listOf("하이퍼 익스텐션", "로만 체어")),
        사전종목("백 스쿼트", "하체", listOf("바벨 스쿼트", "스쿼트")),
        사전종목("프론트 스쿼트", "하체", listOf("프런트 스쿼트"), mapOf("quads" to "P", "glutes" to "S", "adductors" to "S", "lower_back" to "S", "abs" to "Y")),
        사전종목("고블릿 스쿼트", "하체"),
        사전종목("스미스 머신 스쿼트", "하체", listOf("스미스 스쿼트")),
        사전종목("핵 스쿼트", "하체", listOf("핵 스쿼트 머신")),
        사전종목("박스 스쿼트", "하체"),
        사전종목("불가리안 스플릿 스쿼트", "하체", listOf("불가리안", "스플릿 스쿼트")),
        사전종목("덤벨 런지", "하체", listOf("런지")),
        사전종목("워킹 런지", "하체"),
        사전종목("리버스 런지", "하체", emptyList(), mapOf("quads" to "P", "glutes" to "P", "adductors" to "S", "hamstrings" to "Y")),
        사전종목("사이드 런지", "하체", emptyList(), mapOf("adductors" to "P", "quads" to "P", "glutes" to "S")),
        사전종목("덤벨 스텝업", "하체", listOf("스텝업", "박스 스텝업"), mapOf("quads" to "P", "glutes" to "P", "hamstrings" to "Y")),
        사전종목("레그 프레스", "하체", listOf("레그 프레스 머신")),
        사전종목("레그 익스텐션", "하체"),
        사전종목("라잉 레그 컬", "하체", listOf("레그 컬")),
        사전종목("시티드 레그 컬", "하체"),
        사전종목("데드리프트", "하체", listOf("컨벤셔널 데드리프트", "바벨 데드리프트")),
        사전종목("스모 데드리프트", "하체", emptyList(), mapOf("glutes" to "P", "adductors" to "P", "quads" to "S", "hamstrings" to "S", "lower_back" to "S", "traps" to "Y", "forearm" to "Y")),
        사전종목("트랩바 데드리프트", "하체", listOf("헥스바 데드리프트")),
        사전종목("루마니안 데드리프트", "하체", listOf("RDL", "루마니안")),
        사전종목("스티프 레그 데드리프트", "하체", listOf("스티프 데드리프트")),
        사전종목("굿모닝", "하체", listOf("굿모닝 엑서사이즈")),
        사전종목("힙 쓰러스트", "하체", listOf("바벨 힙 쓰러스트", "힙쓰러스트")),
        사전종목("케이블 풀 스루", "하체", emptyList(), mapOf("glutes" to "P", "hamstrings" to "S")),
        사전종목("케이블 킥백", "하체", listOf("글루트 킥백", "힙 킥백"), mapOf("glutes" to "P", "hamstrings" to "Y")),
        사전종목("힙 어브덕션 머신", "하체", listOf("어브덕션", "힙 어브덕터"), mapOf("glutes" to "P")),
        사전종목("힙 어덕션 머신", "하체", listOf("어덕션", "힙 어덕터"), mapOf("adductors" to "P")),
        사전종목("스탠딩 카프 레이즈", "하체", listOf("카프 레이즈")),
        사전종목("시티드 카프 레이즈", "하체"),
        사전종목("노르딕 햄스트링 컬", "맨몸", listOf("노르딕 컬"), mapOf("hamstrings" to "P", "glutes" to "Y")),
        사전종목("오버헤드 프레스", "어깨", listOf("OHP", "밀리터리 프레스", "바벨 숄더 프레스")),
        사전종목("덤벨 숄더 프레스", "어깨", listOf("시티드 덤벨 숄더 프레스")),
        사전종목("머신 숄더 프레스", "어깨", listOf("숄더 프레스 머신")),
        사전종목("스미스 머신 숄더 프레스", "어깨", listOf("스미스 숄더 프레스")),
        사전종목("아놀드 프레스", "어깨", emptyList(), mapOf("delt_front" to "P", "delt_side" to "S", "triceps" to "S")),
        사전종목("비하인드 넥 프레스", "어깨", emptyList(), mapOf("delt_front" to "P", "delt_side" to "P", "triceps" to "S", "traps" to "Y")),
        사전종목("푸시 프레스", "어깨", listOf("푸쉬 프레스"), mapOf("delt_front" to "P", "triceps" to "S", "delt_side" to "S", "quads" to "Y", "glutes" to "Y")),
        사전종목("랜드마인 프레스", "어깨", emptyList(), mapOf("delt_front" to "P", "chest_upper" to "S", "triceps" to "S", "serratus" to "Y")),
        사전종목("사이드 레터럴 레이즈", "어깨", listOf("사레레", "사이드 레이즈", "덤벨 레터럴 레이즈")),
        사전종목("케이블 레터럴 레이즈", "어깨", listOf("케이블 사이드 레이즈")),
        사전종목("머신 레터럴 레이즈", "어깨", listOf("머신 사이드 레이즈")),
        사전종목("덤벨 프론트 레이즈", "어깨", listOf("프론트 레이즈", "프런트 레이즈")),
        사전종목("케이블 프론트 레이즈", "어깨"),
        사전종목("리어 델트 레이즈", "어깨", listOf("벤트오버 레터럴 레이즈", "벤트 오버 레이즈")),
        사전종목("리버스 펙 덱 플라이", "어깨", listOf("리버스 펙덱", "리어 델트 머신")),
        사전종목("케이블 리어 델트 플라이", "어깨"),
        사전종목("페이스 풀", "어깨", listOf("페이스풀"), mapOf("delt_rear" to "P", "rhomboids" to "S", "traps" to "S")),
        사전종목("업라이트 로우", "어깨", emptyList(), mapOf("delt_side" to "P", "traps" to "P", "delt_front" to "S", "biceps" to "Y")),
        사전종목("바벨 컬", "팔", listOf("바벨 바이셉스 컬")),
        사전종목("덤벨 컬", "팔", listOf("덤벨 바이셉스 컬", "얼터네이트 컬")),
        사전종목("EZ바 컬", "팔", listOf("이지바 컬")),
        사전종목("해머 컬", "팔", listOf("덤벨 해머 컬")),
        사전종목("프리처 컬", "팔", listOf("프리처 컬 머신")),
        사전종목("인클라인 덤벨 컬", "팔", emptyList(), mapOf("biceps" to "P", "brachialis" to "S", "forearm" to "Y")),
        사전종목("컨센트레이션 컬", "팔"),
        사전종목("케이블 컬", "팔", listOf("케이블 바이셉스 컬")),
        사전종목("스파이더 컬", "팔"),
        사전종목("리버스 컬", "팔", listOf("리버스 바벨 컬"), mapOf("forearm" to "P", "brachialis" to "P", "biceps" to "S")),
        사전종목("리스트 컬", "팔", listOf("손목 컬"), mapOf("forearm" to "P")),
        사전종목("트라이셉스 푸시다운", "팔", listOf("케이블 푸시다운", "푸쉬다운")),
        사전종목("로프 푸시다운", "팔", listOf("로프 트라이셉스 푸시다운")),
        사전종목("오버헤드 트라이셉스 익스텐션", "팔", listOf("덤벨 오버헤드 익스텐션"), mapOf("triceps" to "P")),
        사전종목("케이블 오버헤드 익스텐션", "팔", listOf("케이블 오버헤드 트라이셉스 익스텐션"), mapOf("triceps" to "P")),
        사전종목("스컬 크러셔", "팔", listOf("라잉 트라이셉스 익스텐션", "EZ바 스컬 크러셔"), mapOf("triceps" to "P")),
        사전종목("덤벨 킥백", "팔", listOf("트라이셉스 킥백")),
        사전종목("클로즈그립 벤치프레스", "팔", listOf("클로즈 그립 벤치프레스", "내로우 그립 벤치"), mapOf("triceps" to "P", "chest_mid" to "S", "delt_front" to "S")),
        사전종목("벤치 딥스", "맨몸", listOf("체어 딥스"), mapOf("triceps" to "P", "delt_front" to "S", "chest_lower" to "Y")),
        사전종목("팔굽혀펴기", "맨몸", listOf("푸시업", "푸쉬업")),
        사전종목("인클라인 푸시업", "맨몸", emptyList(), mapOf("chest_lower" to "P", "chest_mid" to "S", "triceps" to "S", "delt_front" to "Y")),
        사전종목("디클라인 푸시업", "맨몸", emptyList(), mapOf("chest_upper" to "P", "chest_mid" to "S", "delt_front" to "S", "triceps" to "S")),
        사전종목("다이아몬드 푸시업", "맨몸", listOf("클로즈 푸시업"), mapOf("triceps" to "P", "chest_mid" to "P", "delt_front" to "S")),
        사전종목("파이크 푸시업", "맨몸", emptyList(), mapOf("delt_front" to "P", "triceps" to "S", "delt_side" to "Y")),
        사전종목("딥스", "맨몸", listOf("평행봉 딥스")),
        사전종목("인버티드 로우", "맨몸", listOf("오스트레일리안 풀업")),
        사전종목("맨몸 스쿼트", "맨몸", listOf("에어 스쿼트")),
        사전종목("점프 스쿼트", "맨몸", emptyList(), mapOf("quads" to "P", "glutes" to "P", "calves" to "S")),
        사전종목("피스톨 스쿼트", "맨몸", emptyList(), mapOf("quads" to "P", "glutes" to "P", "adductors" to "S", "abs" to "Y")),
        사전종목("글루트 브릿지", "맨몸", listOf("브릿지", "힙 브릿지")),
        사전종목("플랭크", "맨몸"),
        사전종목("사이드 플랭크", "맨몸", emptyList(), mapOf("obliques" to "P", "abs" to "S", "glutes" to "Y")),
        사전종목("크런치", "맨몸"),
        사전종목("리버스 크런치", "맨몸", emptyList(), mapOf("abs" to "P", "obliques" to "Y")),
        사전종목("싯업", "맨몸", listOf("윗몸일으키기")),
        사전종목("행잉 레그 레이즈", "맨몸"),
        사전종목("라잉 레그 레이즈", "맨몸", listOf("레그 레이즈")),
        사전종목("바이시클 크런치", "맨몸", emptyList(), mapOf("abs" to "P", "obliques" to "P")),
        사전종목("러시안 트위스트", "맨몸"),
        사전종목("앱 롤아웃", "맨몸", listOf("AB 롤아웃", "휠 롤아웃"), mapOf("abs" to "P", "lats" to "S", "obliques" to "S", "delt_front" to "Y")),
        사전종목("마운틴 클라이머", "맨몸", emptyList(), mapOf("abs" to "P", "obliques" to "S", "delt_front" to "Y", "quads" to "Y")),
        사전종목("버피", "맨몸", listOf("버피 테스트"), mapOf("quads" to "P", "chest_mid" to "S", "delt_front" to "S", "triceps" to "Y", "abs" to "Y")),
        사전종목("데드 버그", "맨몸", listOf("데드버그"), mapOf("abs" to "P", "obliques" to "Y")),
        사전종목("할로우 바디 홀드", "맨몸", listOf("할로우 홀드"), mapOf("abs" to "P", "obliques" to "Y")),
        사전종목("슈퍼맨", "맨몸", listOf("슈퍼맨 익스텐션"), mapOf("lower_back" to "P", "glutes" to "S")),
    )

    /** 세부 부위 — 근육 지도에서 실제로 칠해지는 부위만 (시안 `세부부위`). 새 종목 시트의 묶음 칩 → 부위 칩 */
    val 세부부위: List<Pair<String, List<String>>> = listOf(
        "가슴" to listOf("chest_upper", "chest_mid", "chest_lower", "serratus"),
        "등" to listOf("lats", "rhomboids", "teres_major", "traps", "lower_back"),
        "어깨" to listOf("delt_front", "delt_side", "delt_rear"),
        "팔" to listOf("biceps", "brachialis", "triceps", "forearm"),
        "복근" to listOf("abs", "obliques"),
        "하체" to listOf("quads", "hamstrings", "glutes", "adductors", "calves", "shin"),
    )
    val 세부키: Set<String> = 세부부위.flatMap { it.second }.toSet()

    /** 역할의 세기 — 겹칠 때 센 쪽 (시안 `역순`) */
    private val 역순 = mapOf("P" to 3, "S" to 2, "Y" to 1)

    /** 이름이 사전 이름과 똑같은 것 (다른 이름은 보지 않는다) */
    fun 이름으로(이름: String): 사전종목? = 목록.firstOrNull { it.이름 == 이름 }

    /** 이름 또는 다른 이름이 (띄어쓰기 · 대소문자 빼고) 똑같은 것 — 시트에서 친 이름으로 정할 때 */
    fun 정확히(이름: String): 사전종목? {
        val n = 다듬(이름)
        if (n.isEmpty()) return null
        return 목록.firstOrNull { 다듬(it.이름) == n } ?: 목록.firstOrNull { x -> x.별.any { 다듬(it) == n } }
    }

    // ─────────────── 초성 검색 (시안 `초성` · `다듬` · `초성자리` · `검색꼴` · `종목찾기`) ───────────────

    private const val 초성표 = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ"
    private const val 받침표 = "ㄱㄲㄳㄴㄵㄶㄷㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅄㅅㅆㅇㅈㅊㅋㅌㅍㅎ"
    private val 빈칸 = Regex("\\s+")

    /** 글자 하나의 초성 — 한글 낱자가 아니면 그대로 */
    fun 초성(c: Char): Char {
        val n = c.code - 0xAC00
        return if (n in 0 until 11172) 초성표[n / 588] else c
    }

    /** 글 전체의 초성 — "벤치프레스" → "ㅂㅊㅍㄹㅅ" */
    fun 초성글(t: String): String = t.map { 초성(it) }.joinToString("")

    /** 소문자로 · 띄어쓰기 없앰 */
    fun 다듬(t: String?): String = (t ?: "").lowercase().replace(빈칸, "")

    /** 글 안에서 q 가 맞는 첫 자리 (q 의 초성 낱자는 그 자리 글자의 초성과 맞으면 된다). 없으면 -1 */
    fun 초성자리(글: String, q: String): Int {
        val a = 다듬(글)
        if (q.isEmpty()) return -1
        var i = 0
        while (i + q.length <= a.length) {
            var 맞 = true
            for (j in q.indices) {
                val x = a[i + j]; val y = q[j]
                if (!(x == y || (y in 초성표 && 초성(x) == y))) { 맞 = false; break }
            }
            if (맞) return i
            i++
        }
        return -1
    }

    /** 찾을 꼴들 — 치는 중인 마지막 글자에 받침이 있으면('벤치플') 받침을 다음 초성으로 뗀 꼴('벤치프ㄹ')도 */
    fun 검색꼴(q0: String): List<String> {
        val q = 다듬(q0)
        if (q.isEmpty()) return emptyList()
        val 꼴 = mutableListOf(q)
        val c = q.last()
        val n = c.code - 0xAC00
        if (n in 0 until 11172 && n % 28 != 0) {
            val 받 = 받침표[n % 28 - 1]
            if (받 in 초성표) 꼴.add(q.dropLast(1) + (c.code - n % 28).toChar() + 받)
        }
        return 꼴
    }

    private val 가나다: Collator by lazy { Collator.getInstance(Locale.KOREAN) }

    /**
     * 초성 검색 — 'ㅂㅊㅍ' → 벤치프레스 · '벤ㅊ' · '프레스' 처럼 섞어도 · 띄어쓰기 무시 · 부분 일치.
     * 점수: 앞에서 맞으면 0 · 가운데 2, 다른 이름으로 맞으면 +1. 점수 → 이름 길이 → 가나다 순. 최대 [최대] 개.
     * [종목표] 를 주면 사전에 없는 직접 만든 종목도 (같은 이름이면 먼저 만든 것 하나만) — 칸은 종목의 부위.
     */
    fun 찾기(q: String, 종목표: List<종목> = emptyList(), 최대: Int = 8): List<사전종목> {
        val 꼴 = 검색꼴(q)
        if (꼴.isEmpty()) return emptyList()
        fun 점수(이름들: List<String>): Int {
            var best = -1
            이름들.forEachIndexed { n, 글 ->
                for (qq in 꼴) {
                    val i = 초성자리(글, qq)
                    if (i < 0) continue
                    val 점 = (if (i == 0) 0 else 2) + (if (n > 0) 1 else 0)
                    if (best < 0 || 점 < best) best = 점
                }
            }
            return best
        }
        val 결 = mutableListOf<Pair<사전종목, Int>>()
        for (x in 목록) { val 점 = 점수(listOf(x.이름) + x.별); if (점 >= 0) 결.add(x to 점) }
        for (t in 종목표) {
            if (목록.any { it.이름 == t.이름 } || 종목표.first { it.이름 == t.이름 } !== t) continue
            val 점 = 점수(listOf(t.이름))
            if (점 >= 0) 결.add(사전종목(t.이름, t.부위) to 점)
        }
        return 결.sortedWith(compareBy<Pair<사전종목, Int>> { it.second }.thenBy { it.first.이름.length }
            .thenComparator { a, b -> 가나다.compare(a.first.이름, b.first.이름) })
            .take(최대).map { it.first }
    }

    // ─────────────── 근육 역할 (시안 `세부로` · `낱말근육` · `사전근육` · `둘역할`) ───────────────

    private fun 부모(id: String): String? = 근육자료.부모[id]

    /**
     * 근육 → 세부 부위 키로: 그 키면 그대로 · 조상이 세부 부위면 조상으로 · 자손이 세부 부위면 그 자손 모두로.
     * 같은 부위가 겹치면 센 역할 (P > S > Y)
     */
    fun 세부로(m: Map<String, String>): Map<String, String> {
        val o = LinkedHashMap<String, String>()
        fun 넣(k: String, r: String) { val 전 = o[k]; if (전 == null || (역순[r] ?: 0) > (역순[전] ?: 0)) o[k] = r }
        for ((id, r) in m) {
            if (id in 세부키) { 넣(id, r); continue }
            var p = 부모(id)
            while (p != null && p !in 세부키) p = 부모(p)
            if (p != null) { 넣(p, r); continue }
            for (k in 세부키) {
                var q2 = 부모(k)
                while (q2 != null && q2 != id) q2 = 부모(q2)
                if (q2 == id) 넣(k, r)
            }
        }
        return o
    }

    /** 이름 속 낱말 규칙 (근육자료.규칙 — 위에서부터 먼저 맞는 것) */
    fun 낱말근육(이름: String): Map<String, String>? =
        근육자료.규칙.firstOrNull { (낱말, _) -> 낱말.any { 이름.contains(it) } }?.second

    /** 사전 종목의 근육 — 정해 둔 것 · 없으면 낱말 규칙 → 칸 기본값을 세부 부위로 */
    fun 사전근육(x: 사전종목): Map<String, String> =
        x.근?.let { LinkedHashMap(it) } ?: 세부로(낱말근육(x.이름) ?: 근육자료.부위기본[x.칸] ?: emptyMap())

    /** 역할을 둘로 — P 는 P, 나머지(S · Y) 는 Y (v20 ⑤). 새 종목 시트 · 저장값은 P · Y 뿐 */
    fun 둘역할(m: Map<String, String>): Map<String, String> = m.mapValues { if (it.value == "P") "P" else "Y" }
}
