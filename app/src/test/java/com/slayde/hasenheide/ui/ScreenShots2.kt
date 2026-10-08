package com.slayde.hasenheide.ui

import com.slayde.hasenheide.data.세트
import com.slayde.hasenheide.data.끝냄
import com.slayde.hasenheide.data.수정메모
import com.slayde.hasenheide.data.인증사진
import com.slayde.hasenheide.data.종목기록
import com.slayde.hasenheide.data.운동시작
import com.slayde.hasenheide.data.플랜폼값
import com.slayde.hasenheide.data.휴식중
import org.junit.Test
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * 화면 사진 2 (10-08 홍겸 님 "빠짐없이") — 시트 · 팝업 · 띠 · 상태별 화면.
 * 한 클래스 = 한 흐름. 중간에 실패해도 그 앞 사진은 남는다.
 */

// ───────── 캘린더 ─────────
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s01e_monthPick : ShotBase() {
    @Test fun t() { 켜기(); 누름("${오늘.year}년 ${오늘.monthValue}월"); 찍("01e_캘린더_달고르기"); 누름("${오늘.year}년"); 찍("01f_캘린더_해고르기") }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s01g_change : ShotBase() {
    @Test fun t() { 켜기(); 누름("변경"); 찍("01g_캘린더_예정변경시트"); 누름("다른 루틴으로"); 찍("01h_캘린더_루틴고르기") }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s01i_rest : ShotBase() {
    @Test fun t() { 켜기(); 누름("변경"); 누름("은 휴식", 일부 = true); 찍("01i_캘린더_휴식시트") }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s01j_move : ShotBase() {
    @Test fun t() { 켜기(); 누름("변경"); 누름("다른 날로 옮기기"); 찍("01j_캘린더_다른날로옮기기") }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s01k_manyExercises : ShotBase() {
    @Test fun t() {
        val 날 = 오늘.minusDays(2)
        켜기 { d ->
            val r = d.기록[날.toString()]!!
            val 더 = listOf("벤치프레스", "인클라인 덤벨프레스", "오버헤드 프레스", "사이드 레터럴 레이즈", "딥스", "케이블 플라이",
                "펙덱 플라이", "트라이셉스 익스텐션", "덤벨 컬", "해머 컬", "페이스 풀", "크런치")
            d.copy(기록 = d.기록 + (날.toString() to r.copy(종목 = 더.map { 종목기록(it, List(3) { 세트(20.0, 10) }) })))
        }
        누름(날.dayOfMonth.toString()); 찍("01k_캘린더_종목많은날")
        누름("외 ", 일부 = true); 찍("01l_캘린더_종목펼침")
    }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s01m_recordReport : ShotBase() {
    @Test fun t() { 켜기 { 오늘기록(it) }; 누름("운동 보고서"); 찍("01m_캘린더_기록보고서") }
}

// ───────── 검색 ─────────
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s02b_searchTyped : ShotBase() {
    @Test fun t() { 켜기(); 탭(1); 쳐넣기("벤치", 설명 = "검색"); 찍("02b_검색_결과") }
}

// ───────── 루틴 ─────────
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s03b_routineDelete : ShotBase() {
    @Test fun t() { 켜기(); 탭(2); 누름설명("하체 B 지우기"); 찍("03b_루틴_지움_되돌리기띠") }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s04b_routineItemOpen : ShotBase() {
    @Test fun t() { 켜기(); 탭(2); 누름("상체 A"); 누름설명("벤치프레스 4세트 펼치기"); 찍("04b_루틴상세_종목펼침") }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s04c_routineRename : ShotBase() {
    @Test fun t() { 켜기(); 탭(2); 누름("상체 A"); 누름설명("루틴 이름"); 찍("04c_루틴상세_이름고치기") }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s04d_restRoutine : ShotBase() {
    @Test fun t() { 켜기(); 탭(2); 누름("휴식", -1); 찍("04d_루틴_휴식일상세") }
}

// ───────── 종목 ─────────
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s06b_category : ShotBase() {
    @Test fun t() { 켜기(); 탭(3); 누름설명("카테고리 관리"); 찍("06b_종목_카테고리관리"); 누름설명("부위 더하기"); 찍("06c_카테고리_부위더하기") }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s06d_categoryDelete : ShotBase() {
    @Test fun t() { 켜기(); 탭(3); 누름설명("카테고리 관리"); 누름설명("부위 지우기"); 찍("06d_카테고리_부위지우기") }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s07d_musclePopup : ShotBase() {
    @Test fun t() { 켜기(); 탭(3); 누름("벤치프레스"); 누름설명("벤치프레스 근육 고르기"); 찍("07d_종목_주동근협응근_팝업") }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s07e_planEdit : ShotBase() {
    @Test fun t() { 켜기(); 탭(3); 누름("벤치프레스"); 누름("변경"); 찍("07e_플랜고치기시트"); 누름("훈련 방식 ›"); 찍("07f_플랜고치기_훈련방식") }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s07g_exerciseDelete : ShotBase() {
    @Test fun t() { 켜기(); 탭(3); 누름("스쿼트"); 누름("삭제"); 찍("07g_종목_삭제_되돌리기띠") }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s07h_planDelete : ShotBase() {
    @Test fun t() { 켜기(); 탭(3); 누름("벤치프레스"); 누름("지우기", -1); 찍("07h_플랜_지움_되돌리기띠") }
}

// ───────── 새 종목 ─────────
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s08b_newFromDict : ShotBase() {
    @Test fun t() {
        켜기(); 탭(3); 누름("+ 새 종목 만들기"); 쳐넣기("덤벨 컬"); 찍("08b_새종목_사전찾기")
        누름("덤벨 컬", -1); 찍("08c_새종목_고른뒤")
        옆누름("운동 목표 부위", 150f, 200f); 찍("08d_새종목_주동근협응근_팝업")
    }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s08e_newCustom : ShotBase() {
    @Test fun t() {
        켜기(); 탭(3); 누름("+ 새 종목 만들기"); 쳐넣기("나만의 운동"); 누름("확인", -1); 찍("08e_새종목_직접이름")
        누름("저장", -1); 바로찍("08f_새종목_저장막힘_토스트", 300)
    }
}

// ───────── 플랜 ─────────
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s09b_planResult : ShotBase() {
    @Test fun t() {
        플랜폼상태.value = 플랜폼값(종목 = "백 스쿼트", 현재무게 = "80", 현재횟수 = "6", 목표무게 = "120")
        켜기(); 탭(4); 찍("09b_플랜_입력함"); 누름("플랜 만들기"); 찍("09c_플랜_결과시트")
        플랜폼상태.value = 플랜폼값()
    }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s09d_planMethod : ShotBase() {
    @Test fun t() {
        플랜폼상태.value = 플랜폼값(종목 = "백 스쿼트", 현재무게 = "80", 현재횟수 = "6", 목표무게 = "120")
        켜기(); 탭(4); 누름("훈련 방식"); 찍("09d_플랜_훈련방식시트")
        플랜폼상태.value = 플랜폼값()
    }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s09e_planBodyweight : ShotBase() {
    @Test fun t() {
        플랜폼상태.value = 플랜폼값(종목 = "턱걸이")
        켜기(); 탭(4); 찍("09e_플랜_맨몸종목")
        플랜폼상태.value = 플랜폼값()
    }
}

// ───────── 메모 ─────────
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s10b_memoList : ShotBase() {
    @Test fun t() {
        val 지금 = System.currentTimeMillis()
        켜기 { it.copy(메모 = listOf(수정메모(지금 - 3_600_000, "운동", "휴식 글자가 아래 잘림"), 수정메모(지금 - 600_000, "캘린더", "오늘 숫자만 굵게"))) }
        탭(5); 찍("10b_메모_목록"); 누름설명("메모 고치기"); 찍("10c_메모_고치기")
    }
}

// ───────── 프로필 ─────────
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s13b_profileLinks : ShotBase() {
    @Test fun t() {
        켜기 { it.copy(설정 = it.설정.copy(링크 = listOf("https://instagram.com/hasenheide", "https://youtube.com/@hasenheide"))) }
        탭(8); 찍("13b_프로필_링크있음"); 누름설명("SNS 링크 보기"); 찍("13c_프로필_링크목록")
    }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s13d_profileLinkWrite : ShotBase() {
    @Test fun t() { 켜기(); 탭(8); 누름설명("SNS 링크 적기"); 찍("13d_프로필_링크적기") }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s13e_profileReportWay : ShotBase() {
    @Test fun t() { 켜기(); 탭(8); 누름설명("결과 보고서 표시 방법"); 찍("13e_프로필_보고서표시방법") }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s13f_profileBig : ShotBase() {
    @Test fun t() { 켜기(); 탭(8); 누름설명("대 상세", 일부 = true); 찍("13f_프로필_큰운동상세") }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s13g_profilePhotos : ShotBase() {
    @Test fun t() {
        val 지금 = System.currentTimeMillis()
        val 얼굴 = 사진("p_face.jpg", 0xFFD6E4F0.toInt(), 0xFF084B83.toInt())
        val 인 = listOf(
            인증사진(사진("p_a.jpg", 0xFFEFE6D8.toInt(), 0xFFB5482B.toInt()), 지금 - 300_000, 고정 = 지금),
            인증사진(사진("p_b.jpg", 0xFFE3EEE3.toInt(), 0xFF2E7D32.toInt()), 지금 - 200_000),
            인증사진(사진("p_c.jpg", 0xFFECE3F0.toInt(), 0xFF6A3D8F.toInt()), 지금 - 100_000),
        )
        켜기 { it.copy(설정 = it.설정.copy(프로필사진 = 얼굴), 인증샷 = 인) }
        탭(8); 찍("13g_프로필_사진있음"); 누름설명("인증샷 (고정)"); 찍("13h_프로필_인증샷시트")
    }
}

// ───────── 스테이터스 · 도전 과제 ─────────
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s14b_statGraph : ShotBase() {
    @Test fun t() { 켜기(); 탭(8); 누름("스탯", -1); 누름("밀기"); 찍("14b_스테이터스_그래프") }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s15c_achievementMore : ShotBase() {
    @Test fun t() { 켜기(); 탭(8); 누름("업적", -1); 누름("+"); 찍("15c_도전과제_분류더보기") }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s15d_lockedTitle : ShotBase() {
    @Test fun t() { 켜기(); 탭(8); 누름("업적", -1); 누름설명("잠긴 칭호 보기"); 찍("15d_도전과제_잠긴칭호보기") }
}

// ───────── 운동 ─────────
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s16d_pictureHidden : ShotBase() {
    @Test fun t() { 그림칸기억.숨긴운동 = "rA"; 켜기 { 세션넣기(it, 2) }; 찍("16d_운동_그림감춤"); 그림칸기억.숨긴운동 = "" }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s16e_allDone : ShotBase() {
    @Test fun t() {
        켜기 { d ->
            val S = 운동시작(d.루틴들.first(), System.currentTimeMillis() - 50 * 60_000)!!
            val n = S.종목들.size
            d.copy(세션 = S.copy(i = n - 1, 종목들 = S.종목들.map { e -> e.copy(기록 = List(e.세트) { 세트(e.무게, e.횟수) }) }))
        }
        찍("16e_운동_다끝냄_마무리")
    }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s16f_workoutAdd : ShotBase() {
    @Test fun t() { 켜기 { 세션넣기(it, 1) }; 누름설명("종목 넣기"); 찍("16f_운동_종목넣기시트") }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s16g_nextExercise : ShotBase() {
    @Test fun t() { 켜기 { 세션넣기(it, 4) }; 누름설명("다음 종목"); 찍("16g_운동_다음종목") }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s16h_emptyWorkout : ShotBase() {
    @Test fun t() {
        켜기 { d -> d.copy(세션 = 운동시작(d.루틴들.first(), System.currentTimeMillis() - 60_000)!!.copy(종목들 = emptyList())) }
        찍("16h_운동_종목없음")
    }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s17b_restAsk : ShotBase() {
    @Test fun t() {
        켜기 { d -> val x = 세션넣기(d, 2); x.copy(세션 = x.세션!!.copy(휴식 = 휴식중(1, System.currentTimeMillis() - 5_000, 물음 = true, 총초 = 90, 종목 = 0))) }
        찍("17b_운동_휴식끝_물음")
    }
}

// ───────── 운동 보고서 ─────────
abstract class 보고서Base : ShotBase() {
    protected fun 보고서켜기() {
        켜기 { d ->
            val r = d.루틴들.first()
            val S = 운동시작(r, System.currentTimeMillis() - 55 * 60_000)!!
            val 다 = S.copy(종목들 = S.종목들.map { e -> e.copy(기록 = List(e.세트) { 세트(e.무게, e.횟수) }) })
            d.copy(세션 = 다.끝냄(System.currentTimeMillis()))
        }
    }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s18b_reportWay : 보고서Base() {
    @Test fun t() { 보고서켜기(); 누름설명("결과 보고서 표시 방법"); 찍("18b_보고서_표시방법시트") }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s18c_reportBig : 보고서Base() {
    @Test fun t() { 보고서켜기(); 누름설명("대 상세", 일부 = true); 찍("18c_보고서_큰운동상세") }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s18d_reportRoutine : 보고서Base() {
    @Test fun t() { 보고서켜기(); 누름설명("상체 A 상세"); 찍("18d_보고서_루틴상세") }
}

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s18e_reportOpen : 보고서Base() {
    @Test fun t() { 보고서켜기(); 누름설명("벤치프레스 펼치기"); 찍("18e_보고서_종목펼침") }
}

// ───────── 알림 · 작은 창 ─────────
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s20_newAchievement : ShotBase() {
    @Test fun t() { 켜기(업적둠 = true); 바로찍("20_새업적_알림띠", 0) }
}

@Config(qualifiers = "w320dp-h180dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s21_pip : ShotBase() {
    @Test fun t() { 켜기(작은 = true) { 세션넣기(it, 2) }; 찍("21_작은창_운동중") }
}

@Config(qualifiers = "w320dp-h180dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Shot_s21b_pipRest : ShotBase() {
    @Test fun t() {
        켜기(작은 = true) { d -> val x = 세션넣기(d, 2); x.copy(세션 = x.세션!!.copy(휴식 = 휴식중(1, System.currentTimeMillis() + 75_000, 총초 = 90, 종목 = 0))) }
        찍("21b_작은창_휴식중")
    }
}
