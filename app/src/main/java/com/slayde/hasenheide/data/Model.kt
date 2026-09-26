package com.slayde.hasenheide.data

/**
 * 앱이 다루는 모든 자료의 모양.
 *
 * ── 왜 전부 data class(바꿀 수 없는 값)인가 ──
 * 화면(Compose)은 "값이 바뀌었나?"를 보고 다시 그린다.
 * 값을 제자리에서 고치면 화면이 바뀐 줄 모를 수 있다.
 * 그래서 고칠 때는 언제나 새 값을 만들어 통째로 바꾼다 (copy).
 *
 * 이 파일에는 안드로이드 기능을 쓰지 않는다 → 컴퓨터에서 따로 검사할 수 있다.
 */

/** 종목 — 벤치프레스 같은 운동 하나 */
data class 종목(
    val 이름: String,
    val 부위: String,
    val 장비: String = "",
    /** 참고 링크 — 주소는 감추고 이 글씨만 보인다 (09-24) */
    val 참고글: String? = null,
    val 참고url: String? = null,
    /** 달력 칸에만 쓰는 짧은 이름. 비어 있으면 종목 이름을 따라간다 */
    val 달력이름: String? = null,
)

/** 루틴 안에 들어 있는 종목 한 줄 — 세트 · 무게 · 횟수 · 휴식(초) */
data class 루틴종목(
    val 이름: String,
    val 세트: Int = 3,
    val 무게: Double = 20.0,
    val 횟수: Int = 10,
    val 휴식: Int = 90,
    /** 같은 값을 가진 종목끼리 슈퍼세트로 묶인다 */
    val 슈퍼: String? = null,
    /** 세트마다 따로 정한 목표 (기능명세 4-2 · 5-5). 비어 있거나 모자라면 위의 무게 · 횟수를 쓴다 */
    val 세트값: List<세트> = emptyList(),
    /** 세트마다 따로 정한 휴식(초). 비어 있거나 모자라면 위의 휴식을 쓴다 */
    val 휴식값: List<Int> = emptyList(),
)

/** 루틴 — 가슴날처럼 하루에 하는 운동 묶음. 휴식일도 루틴 한 칸이다 */
data class 루틴(
    val id: String,
    val 이름: String,
    val 휴식일: Boolean = false,
    val 종목: List<루틴종목> = emptyList(),
    /** 캘린더에 순서대로 저절로 깔까 (09-25 메모). 새 루틴은 꺼져 있다. 옛 루틴은 켜진 채로 옮긴다 */
    val 자동생성: Boolean = false,
)

/** 한 세트 — 무게(w) × 횟수(r) */
data class 세트(val w: Double, val r: Int)

/** 그 날 한 종목의 기록 */
data class 종목기록(
    val 이름: String,
    val 세트들: List<세트>,
    /** 오늘만 끼운 종목(운동 추가) — 루틴 차원 계산에서 뺀다 */
    val 임시: Boolean = false,
    /** 슈퍼세트로 했으면 묶음 이름 "덤벨컬+해머컬". 같은 묶음끼리만 견준다 */
    val 묶음: String? = null,
)

/** 하루 기록 */
data class 날기록(
    val 루틴id: String,
    val 루틴이름: String,
    val 달성: Boolean,
    val 종목들: List<종목기록>,
    val 걸린초: Int = 0,
)

/** 설정 */
data class 설정값(
    val 자동진행: Boolean = true,
    val 넘어가기전확인: Boolean = false,
    val 소리진동: Boolean = true,
    val 화면유지: Boolean = true,
    /** 무게 ＋ − 한 번의 폭 */
    val 무게폭: Double = 1.0,
    /** 새 종목을 넣을 때의 휴식(초) */
    val 기본휴식: Int = 60,   // 09-24: 기본 휴식 1분
    /** 새 종목을 넣을 때의 세트 수 (09-21 메모: 보통 5세트) */
    val 기본세트: Int = 1,   // 09-22: 새 종목은 1세트로 시작, ＋ 로 늘린다
    // ── 볼륨 자동 올리기 (09-24, 업데이트 예정 ⑲) ──
    /** 루틴을 성공하면 다음 루틴 목표를 저절로 올린다 */
    val 볼륨켬: Boolean = false,
    /** "%" 또는 "kg" */
    val 볼륨방식: String = "%",
    val 볼륨값: Double = 2.5,
    /** "성공" = 계획한 세트를 다 끝낸 날만 · "항상" */
    val 볼륨언제: String = "성공",
    /** 어디에 붙일까 — "횟수"(상한을 넘으면 무게로) · "무게" */
    val 볼륨배분: String = "횟수",
    /** 횟수에 붙일 때의 상한 */
    val 횟수상한: Int = 12,
    /** 향상도 기준: 0 = 직전 같은 루틴, 1 · 3 · 6 · 12 = 몇 개월 전 */
    val 기준: Int = 3,
    /** 휴식 끝 진동 세기 1 약 · 2 중 · 3 강 (09-25 메모) */
    val 진동세기: Int = 2,
    /** 휴식 끝 진동 길이(ms) (09-25 메모) */
    val 진동시간: Int = 1000,
    /** 화면 번호 보기 — 시험 기간 동안 칸마다 작은 번호 (09-26) */
    val 번호보기: Boolean = true,
)

/** 써보면서 적는 수정 메모 — 어느 화면에서 적었는지 함께 남긴다 */
data class 수정메모(
    val 시각: Long,
    val 화면: String,
    val 글: String,
    /** 메모를 적기 바로 전에 한 동작들 (09-24 메모) — 무엇을 하다 적었는지 알 수 있게 */
    val 흔적: List<String> = emptyList(),
)

// ─────────────── 운동 중 ───────────────

/** 운동 중인 종목 한 줄. 기록 칸은 비어 있을 수 있다(체크를 풀면 그 칸만 빈다) */
data class 세션종목(
    val 이름: String,
    val 세트: Int,
    /** 루틴에 원래 정해둔 세트 수 — 달성도의 분모. 운동 중 추가해도 안 바뀐다 */
    val 계획세트: Int,
    val 무게: Double,
    val 횟수: Int,
    val 휴식: Int,
    val 기록: List<세트?> = emptyList(),
    /** 아직 안 한 세트의 값을 따로 고쳐둔 것 */
    val 예정값: List<세트?> = emptyList(),
    /** 세트마다의 휴식(초) */
    val 휴식들: List<Int?> = emptyList(),
    val 임시: Boolean = false,
    /** '여기까지'로 마친 종목 */
    val 마감: Boolean = false,
    val 슈퍼: String? = null,
)

/** 방금 끝낸 세트 줄 위에서 도는 휴식 */
data class 휴식중(
    val k: Int,
    val 끝시각: Long,
    /** 휴식이 끝나고 '다음으로 갈까요?'를 묻는 중 */
    val 물음: Boolean = false,
    /** 슈퍼세트 한 바퀴 뒤 돌아갈 자리 */
    val 다음i: Int? = null,
    val 다음s: Int? = null,
    /** 처음 정한 휴식 길이(초) — 남은 비율로 색을 바꾼다 */
    val 총초: Int = 0,
    /** 어느 종목의 세트 줄에서 도는지. -1 = 옛 기록(지금 종목) */
    val 종목: Int = -1,
)

data class 운동세션(
    val 루틴id: String,
    val 루틴이름: String,
    val 시작시각: Long,
    /** 지금 종목 번호 */
    val i: Int = 0,
    /** 지금 할 세트 번호 */
    val s: Int = 0,
    /** 지금 할 세트의 무게 · 횟수 (체크하면 이 값으로 기록된다) */
    val 무게: Double = 0.0,
    val 횟수: Int = 0,
    val 종목들: List<세션종목> = emptyList(),
    val 휴식: 휴식중? = null,
    /** 마무리 화면을 보는 중 */
    val 끝화면: Boolean = false,
    /** 마무리 화면에 들어온 시각 — 여기서 운동 시간이 멈춘다 */
    val 끝시각: Long? = null,
    /** 마지막으로 손댄 시각 — 오래 손대지 않으면 저절로 끝낸다 (09-25 메모). 0 = 모름(옛 파일) → 시작시각 */
    val 마지막: Long = 0L,
)

/** 앱 전체 자료 — 이것 하나를 통째로 파일에 저장한다 */
data class 앱데이터(
    val 종목표: List<종목> = emptyList(),
    val 카테고리: List<String> = 기본카테고리,
    val 루틴들: List<루틴> = emptyList(),
    /** "2026-09-21" → 그 날 기록 */
    val 기록: Map<String, 날기록> = emptyMap(),
    /** "2026-09-22" → 루틴 id (앞으로 할 날에 미리 깔아둔 것) */
    val 예정: Map<String, String> = emptyMap(),
    val 일정: Map<String, List<String>> = emptyMap(),
    val 설정: 설정값 = 설정값(),
    val 메모: List<수정메모> = emptyList(),
    val 세션: 운동세션? = null,
) {
    companion object {
        /** 자료 구조가 바뀌면 올린다 — 옛 백업 파일을 읽을 때 구분하려고 */
        const val 스키마 = 6
        val 기본카테고리 = listOf("가슴", "등", "하체", "어깨", "팔", "복근")
    }
}
