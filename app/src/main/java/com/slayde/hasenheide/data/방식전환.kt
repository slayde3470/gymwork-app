package com.slayde.hasenheide.data

import kotlin.math.max
import kotlin.math.min

/**
 * ⇄ 단추 (10-19 홍겸 님) — 어시스트가 들어갈 수 있는 종목(턱걸이 · 딥스 · 친업)은 **일반 → 어시스트 → 중량 → 일반** 으로 돈다.
 *  · 일반   = 무게 칸 없음 (정자세 · 체중만)   — 줄: [기록방식.정자세] · 플랜: 보조모드 0
 *  · 어시스트 = 보조 kg 를 뺀 무게            — 줄: [기록방식.어시스트] · 플랜: 보조모드 1
 *  · 중량   = 체중 + 추가 kg                  — 줄: [기록방식.맨몸] · 플랜: 보조모드 2 (과부하)
 *
 * 두 갈래다 — 화면은 [앱데이터.줄전환모드] 로 단추를 보일지 정하고, 줄이 플랜 줄이면 [앱데이터.플랜전환], 일반 줄이면 [앱데이터.종목전환] 을 부른다.
 *  · 일반 줄의 방식은 **종목 하나의 성질**이다 ([종목.방식]) → 바꾸면 그 종목을 쓰는 모든 일반 줄(기본 세팅 · 루틴 · 그 날 운동 · 운동 중)이 같이 바뀐다
 *  · 플랜 줄의 방식은 **플랜 하나의 성질**이다 ([플랜.보조모드]) → 바꾸면 그 플랜 줄만 새 처방으로 다시 채운다. 같은 종목의 일반 줄은 그대로
 *  · 칸 값의 뜻이 바뀌므로(추가 ↔ 보조) 바꿀 때 무게 칸 값은 처음 값으로 되돌린다
 *  · 운동 중에는 그 종목에 **체크한 세트가 하나라도 있으면 바꾸지 않는다** ([앱데이터.전환막힘])
 *
 * 이 파일은 안드로이드를 쓰지 않는다 → 시험(ModeSwitchTest.kt)이 컴퓨터에서 돈다.
 */
enum class 전환모드 { 일반, 어시스트, 중량 }

object 전환표 {
    /** 어시스트로 바꿀 때 무게 칸의 처음 값(보조 kg) — 일반 줄 */
    const val 줄어시스트기본 = 10.0
    /** 어시스트 · 중량으로 바꿀 때 플랜의 처음 보조 무게(kg) */
    const val 플랜어시스트기본 = 10.0
    const val 플랜중량기본 = 5.0
}

fun 전환모드.다음(): 전환모드 = 전환모드.values()[(ordinal + 1) % 전환모드.values().size]

/** 이 모드로 두는 [종목.방식] 이름 */
fun 전환모드.방식이름(): String = when (this) {
    전환모드.일반 -> 기록방식.정자세.name
    전환모드.어시스트 -> 기록방식.어시스트.name
    전환모드.중량 -> 기록방식.맨몸.name
}

/** 플랜의 보조모드 숫자 (0 정자세 · 1 어시스트 · 2 과부하) ↔ 모드 */
fun 전환모드.보조모드(): Int = ordinal
fun 보조모드로(n: Int): 전환모드 = 전환모드.values().getOrElse(n) { 전환모드.일반 }

/** 이름이 ⇄ 를 쓰는 종목인가 — 사전의 다른 이름도 대표 이름으로 본다 ('풀업' = 턱걸이) */
fun 전환종목인가(이름: String): Boolean = (종목사전.정확히(이름)?.이름 ?: 이름) in 기록방식표.정자세들

/** 줄 하나가 ⇄ 를 보일 줄이면 지금 모드, 아니면 null (플랜 줄은 보조 옵션이 있는 플랜만, 일반 줄은 정자세 · 어시스트 · 맨몸 방식인 3종목만) */
fun 앱데이터.줄전환모드(종id: String?, 이름: String, 플랜id: String?): 전환모드? {
    if (플랜id != null) {
        val p = 플랜찾기(플랜id) ?: return null
        if (p.표?.보조옵션 != true) return null
        return 보조모드로(p.보조모드)
    }
    val t = 종목찾기(종id, 이름)
    if (!전환종목인가(t?.이름 ?: 이름)) return null
    return when (방식(종id, 이름)) {
        기록방식.정자세 -> 전환모드.일반
        기록방식.어시스트 -> 전환모드.어시스트
        기록방식.맨몸 -> 전환모드.중량
        else -> null   // 직접 '무게' 등으로 만든 같은 이름 종목은 바꾸지 않는다
    }
}

/** 운동 중 이 줄의 종목(플랜 줄이면 그 플랜 줄)에 체크한 세트가 있나 — 있으면 방식을 바꾸지 않는다 (체크한 값의 뜻이 달라지므로) */
fun 앱데이터.전환막힘(종id: String?, 이름: String, 플랜id: String?): Boolean {
    val s = 세션 ?: return false
    return s.종목들.any { e -> 같은줄(e.종id, e.이름, e.플랜id, 종id, 이름, 플랜id) && e.기록.any { it != null } }
}

private fun 앱데이터.같은줄(aId: String?, a이름: String, aP: String?, bId: String?, b이름: String, bP: String?): Boolean =
    if (aP != null || bP != null) aP == bP
    else (종목찾기(aId, a이름)?.id ?: 종목열쇠(aId, a이름)) == (종목찾기(bId, b이름)?.id ?: 종목열쇠(bId, b이름))

// ─────────────── 일반 줄 — 종목의 방식을 바꾼다 ───────────────

/**
 * 일반 줄의 ⇄ — 종목의 방식을 일반 → 어시스트 → 중량 → 일반 으로. 바뀌지 않으면(대상이 아님 · 체크한 세트가 있음) 그대로 돌려준다.
 * 무게 칸 값은 처음 값으로: 어시스트 = [전환표.줄어시스트기본] · 일반 · 중량 = 0
 */
fun 앱데이터.종목전환(종id: String?, 이름: String): 앱데이터 {
    val 지금 = 줄전환모드(종id, 이름, null) ?: return this
    if (전환막힘(종id, 이름, null)) return this
    val t = 종목찾기(종id, 이름) ?: return this
    val 새 = 지금.다음()
    val 값 = if (새 == 전환모드.어시스트) 전환표.줄어시스트기본 else 0.0
    val d1 = copy(종목표 = 종목표.map { if (it.id == t.id) it.copy(방식 = 새.방식이름()) else it })
    // 기본 세팅 — 있던 줄 수 · 횟수 · 휴식은 두고 무게만
    val 기본 = d1.종목기본세트(t.id).map { it.copy(w = 값) }
    val 맞는 = { e: 루틴종목 -> e.플랜id == null && d1.종목찾기(e.종id, e.이름)?.id == t.id }
    val 루틴바꿈 = { r: 루틴 ->
        if (r.종목.none(맞는)) r
        else r.copy(종목 = r.종목.map { e ->
            if (!맞는(e)) e else e.copy(무게 = 값, 세트값 = e.세트값.map { it.copy(w = 값) })
        })
    }
    var d = d1.copy(종목설정 = d1.종목설정 + (t.id to 기본), 루틴들 = d1.루틴들.map(루틴바꿈))
    d = d.그날운동도(루틴바꿈)
    return d.copy(세션 = d.세션?.let { s -> s.세션줄들바꿈(d, t.id, 값) })
}

private fun 운동세션.세션줄들바꿈(d: 앱데이터, 종목id: String, 값: Double): 운동세션 {
    val 새 = 종목들.map { e ->
        if (e.플랜id != null || d.종목찾기(e.종id, e.이름)?.id != 종목id) e
        else e.copy(무게 = 값, 예정값 = e.예정값.map { it?.copy(w = 값) })
    }
    val 지금줄 = 종목들.getOrNull(i)
    val 지금바뀜 = 지금줄 != null && 지금줄.플랜id == null && d.종목찾기(지금줄.종id, 지금줄.이름)?.id == 종목id
    return copy(종목들 = 새, 무게 = if (지금바뀜) 값 else 무게)
}

// ─────────────── 플랜 줄 — 플랜의 보조모드를 바꾼다 ───────────────

/** 플랜 ⇄ — 보조모드 0 → 1 → 2 → 0. 보조 무게는 [전환표] 의 처음 값 (일반은 0). 플랜 줄(루틴 · 그 날 운동 · 운동 중)을 새 처방으로 다시 채운다 */
fun 앱데이터.플랜전환(id: String): 앱데이터 {
    val p = 플랜찾기(id) ?: return this
    if (p.표?.보조옵션 != true) return this
    if (전환막힘(null, p.이름, id)) return this
    val 새 = 보조모드로(p.보조모드).다음()
    val 무게 = when (새) { 전환모드.어시스트 -> 전환표.플랜어시스트기본; 전환모드.중량 -> 전환표.플랜중량기본; else -> 0.0 }
    return 플랜바꿔채움(id, p.copy(보조모드 = 새.보조모드(), 보조무게 = 무게))
}

/** 플랜의 보조 무게 − ＋ 한 번 (어시스트 · 중량일 때만) — 한 칸 = 설정의 무게폭, 어시스트는 체중보다 작게 */
fun 앱데이터.플랜보조무게(id: String, 방향: Int): 앱데이터 {
    val p = 플랜찾기(id) ?: return this
    if (p.표?.보조옵션 != true || p.보조모드 == 0) return this
    if (전환막힘(null, p.이름, id)) return this
    val 폭 = if (설정.무게폭 > 0) 설정.무게폭 else 1.0
    val 위 = if (p.보조모드 == 1 && 몸.체중 > 0) max(폭, 몸.체중 - 폭) else 플랜보조최대
    val 새무게 = min(위, max(폭, 무게반올림(p.보조무게 + 방향 * 폭)))
    if (새무게 == p.보조무게) return this
    return 플랜바꿔채움(id, p.copy(보조무게 = 새무게))
}

private const val 플랜보조최대 = 200.0

private fun 앱데이터.플랜바꿔채움(id: String, 새: 플랜): 앱데이터 {
    val d1 = copy(플랜들 = 플랜들.map { if (it.id == id) 새 else it })
    var d = d1.copy(루틴들 = d1.루틴들.map { d1.플랜줄채움(it) })
    d = d.그날운동도 { r -> d.플랜줄채움(r) }
    return d.copy(세션 = d.세션?.let { s ->
        s.copy(종목들 = s.종목들.map { e ->
            if (e.플랜id != id || e.기록.any { it != null }) return@map e
            val 채움 = d.플랜줄채움(루틴종목(e.이름, 플랜id = id))
            // 줄 수가 달라지는 날(측정일 워밍업 등)은 건드리지 않는다
            if (채움.세트값.isEmpty() || 채움.세트값.size != e.총칸()) e
            else e.copy(무게 = 채움.세트값[0].w, 횟수 = 채움.세트값[0].r, 예정값 = 채움.세트값)
        })
    })
}

// ─────────────── 옛 폰 — 한 번만 ───────────────

/**
 * 10-19: 턱걸이 · 딥스 · 친업의 처음 방식이 '맨몸(추가 kg)' 에서 '일반(정자세)' 으로 바뀌었다.
 * 방식을 아직 안 고른 이 종목이 **0 아닌 추가 무게**를 기본 세팅 · 루틴에 갖고 있으면(= 추가 무게를 쓰던 폰) 중량 그대로 둔다.
 * 아무것도 안 쓰던 종목은 일반이 된다 (기록은 실제 무게로 남아 있어 그대로)
 */
fun 앱데이터.정자세이주(): 앱데이터 {
    val 새표 = 종목표.map { t ->
        if (t.방식 != null || !전환종목인가(t.이름)) return@map t
        val 설정쓰는 = 종목설정[t.id]?.any { it.w > 0.0 } == true
        val 루틴쓰는 = (루틴들 + 그날운동).any { r -> r.종목.any { e -> e.플랜id == null && 종목찾기(e.종id, e.이름)?.id == t.id && (e.무게 > 0.0 || e.세트값.any { it.w > 0.0 }) } }
        val 세션쓰는 = 세션?.종목들?.any { e -> e.플랜id == null && 종목찾기(e.종id, e.이름)?.id == t.id && (e.무게 > 0.0 || e.예정값.any { (it?.w ?: 0.0) > 0.0 } || e.기록.any { (it?.w ?: 0.0) > 0.0 }) } == true
        if (설정쓰는 || 루틴쓰는 || 세션쓰는) t.copy(방식 = 기록방식.맨몸.name) else t
    }
    return if (새표 == 종목표) this else copy(종목표 = 새표)
}
