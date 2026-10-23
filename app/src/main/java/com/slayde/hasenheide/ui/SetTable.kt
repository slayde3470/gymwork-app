package com.slayde.hasenheide.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.slayde.hasenheide.data.거리칸
import com.slayde.hasenheide.data.거리한칸
import com.slayde.hasenheide.data.기록방식
import com.slayde.hasenheide.data.무게글
import com.slayde.hasenheide.data.무게칸있음
import com.slayde.hasenheide.data.시간최대
import com.slayde.hasenheide.data.시간최소
import com.slayde.hasenheide.data.시간칸
import com.slayde.hasenheide.data.시간한칸
import com.slayde.hasenheide.ui.theme.Local색
import com.slayde.hasenheide.ui.theme.간격
import com.slayde.hasenheide.ui.theme.글꼴
import com.slayde.hasenheide.ui.theme.높이
import com.slayde.hasenheide.ui.theme.선굵기
import com.slayde.hasenheide.ui.theme.크기

/**
 * 세트 표 — 운동 중 화면(WorkoutScreen `운세트줄`)과 똑같은 모양의 공용 부품 (10-23 홍겸 님: "세트 세팅화면은 운동중 화면이랑 똑같이").
 * 루틴 펼친 칸 · 종목 탭 기본 세팅 · 새 종목 시트가 모두 이것을 쓴다. 칸 폭 · 글 크기 · 줄 높이 · − ＋ 칸이 운동 중과 한 계산이다.
 * 다른 점 둘: 번호 동그라미는 체크가 아니라 번호만 보이고 · 쉬는 게이지 · 타이머 단추는 없다.
 */
internal class 세트표칸(val 폭: List<Dp>, val 글크기: TextUnit)

/** 머리 줄(세트 · 무게 · 횟수 · 휴식) + 세트 줄들. 칸 폭은 이 부품이 놓인 폭에서 글자를 재서 정한다 (운동 화면과 같은 방법) */
@Composable
internal fun 세트표(
    modifier: Modifier = Modifier,
    무게머리: String = "kg", 횟수머리: String = "회",
    줄들: @Composable ColumnScope.(세트표칸) -> Unit,
) {
    val c = Local색.current
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val 남는 = maxWidth - 높이.아주낮게 - 간격.아주좁게 * 4
        val 측정 = rememberTextMeasurer()
        val 밀도 = LocalDensity.current
        val 글크기 = remember(밀도, 남는) {
            listOf(크기.본문, 크기.버튼).firstOrNull { 크 -> 세트칸최소(측정, 밀도, 크).sum() <= 남는.value } ?: 크기.작게
        }
        val 폭 = remember(남는, 글크기) { 격자폭(남는.value, 세트칸최소(측정, 밀도, 글크기), 운치수.줄비).map { it.dp } }
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(간격.아주좁게 * 운치수.세트줄간격비)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
                listOf("세트", 무게머리, 횟수머리, "휴식").forEachIndexed { x, t ->
                    Text(t, Modifier.width(폭[x]), style = 글꼴.보통(크기.작게), color = c.옅음, textAlign = TextAlign.Center, maxLines = 1)
                }
            }
            줄들(세트표칸(폭, 글크기))
        }
    }
}

/** 번호 동그라미 — 운동 중 번호 동그라미(36 · 테 2 강조 · 번호 15)와 같은 모양. 누르는 곳이 아니다 */
@Composable
internal fun 세트표번호(번호: Int) {
    val c = Local색.current
    Box(
        Modifier.size(운치수.번호).clip(CircleShape).background(c.면).border(선굵기.굵게, c.강조, CircleShape),
        contentAlignment = Alignment.Center,
    ) { Text("$번호", style = 글꼴.보통(크기.본문).copy(fontFeatureSettings = "tnum"), color = c.강조, maxLines = 1) }
}

/**
 * 세트 한 줄의 틀 — [번호 동그라미][값 칸 묶음][휴지통]. 값 칸 묶음 안에 [세트표값] 이 하나(보통 종목) 또는 여럿(슈퍼세트: 번호 하나, 종목마다 한 줄) 들어간다.
 * 번호 · 휴지통은 묶음 높이의 가운데에 선다.
 */
@Composable
internal fun 세트표틀(
    칸: 세트표칸, 번호: Int, 지울수있음: Boolean, 지움: () -> Unit,
    modifier: Modifier = Modifier,
    값들: @Composable ColumnScope.() -> Unit,
) {
    val c = Local색.current
    val 폭 = 칸.폭
    Row(
        modifier.fillMaxWidth().padding(vertical = 간격.아주좁게 * 운치수.세트줄간격비),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.아주좁게),
    ) {
        Box(Modifier.width(폭[0]), contentAlignment = Alignment.Center) { 세트표번호(번호) }
        Column(Modifier.width(폭[1] + 폭[2] + 폭[3] + 간격.아주좁게 * 2), verticalArrangement = Arrangement.spacedBy(간격.아주좁게), content = 값들)
        Box(
            Modifier.size(높이.아주낮게).alpha(if (지울수있음) 1f else 운치수.흐린단추).then(if (지울수있음) Modifier.눌림(지움) else Modifier)
                .semantics { contentDescription = "${번호}세트 지우기" },
            contentAlignment = Alignment.Center,
        ) { Icon(아이콘.지우기, null, Modifier.size(18.dp), tint = c.옅음) }
    }
}

/**
 * 한 종목의 세 칸 [무게 − ＋][횟수 − ＋][휴식 − ＋] — 운동 화면 `운세트줄` 의 칸 규칙 그대로:
 * 횟수만 · 시간은 무게 칸을 비우고(시간은 무게 칸 자리까지 넓은 시간 칸) · 유산소는 무게 칸이 거리 km · 시간 칸이 1분씩.
 * 값을 바꾸는 일(무게 폭 · 최소 · 최대)은 부르는 쪽이 한다 — 여기는 방향(-1 · +1)과 쳐 넣은 글만 넘긴다.
 * 슈퍼세트의 앞 종목은 [휴식보임] = false (마지막 종목만 쉰다 · 운동 화면과 같다)
 */
@Composable
internal fun 세트표값(
    칸: 세트표칸, 번호: Int, 방: 기록방식, 무게: Double, 횟수: Int, 휴식: Int,
    아래글: String?, 휴식보임: Boolean,
    무게바꿈: (Int) -> Unit, 무게넣기: (String) -> Unit,
    횟수바꿈: (Int) -> Unit, 횟수넣기: (String) -> Unit,
    시간바꿈: (Int) -> Unit, 휴식바꿈: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val 폭 = 칸.폭; val 글크기 = 칸.글크기
    val 버티기 = 방 == 기록방식.시간; val 거리줄 = 방.거리칸()
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(간격.아주좁게)) {
        if (방.무게칸있음()) {
            무게칸아래글(아래글, Modifier.width(폭[1])) {
                운값칸(무게글(무게), if (거리줄) "${번호}세트 거리" else "${번호}세트 무게", 소수 = true, 끝남 = false, 글크기, Modifier.fillMaxWidth(),
                    빼기 = { 무게바꿈(-1) }, 더하기 = { 무게바꿈(1) }, 넣기 = 무게넣기)
            }
        } else if (!방.시간칸()) Spacer(Modifier.width(폭[1]))
        if (버티기) {
            Row(Modifier.width(폭[1] + 폭[2] + 간격.아주좁게), horizontalArrangement = Arrangement.spacedBy(간격.아주좁게), verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(높이.아주낮게))
                운휴칸(횟수, false, 글크기, Modifier.weight(1f), 최소 = 방.시간최소(), 최대 = 방.시간최대(), 이름 = "${번호}세트 시간 5초") { 시간바꿈(it) }
            }
        } else if (거리줄) {
            운휴칸(횟수, false, 글크기, Modifier.width(폭[2]), 최소 = 방.시간최소(), 최대 = 방.시간최대(), 이름 = "${번호}세트 시간 1분") { 시간바꿈(it) }
        } else {
            운값칸("$횟수", "${번호}세트 횟수", 소수 = false, 끝남 = false, 글크기, Modifier.width(폭[2]),
                빼기 = { 횟수바꿈(-1) }, 더하기 = { 횟수바꿈(1) }, 넣기 = 횟수넣기)
        }
        Box(Modifier.width(폭[3]), contentAlignment = Alignment.Center) {
            if (휴식보임) 운휴칸(휴식, false, 글크기, Modifier.fillMaxWidth(), 이름 = "${번호}세트 휴식 15초") { 휴식바꿈(it) }
        }
    }
}

/** 보통 종목 세트 한 줄 = [세트표틀] + [세트표값] 하나 */
@Composable
internal fun 세트표값줄(
    칸: 세트표칸, 번호: Int, 방: 기록방식, 무게: Double, 횟수: Int, 휴식: Int,
    아래글: String?, 휴식보임: Boolean, 지울수있음: Boolean,
    무게바꿈: (Int) -> Unit, 무게넣기: (String) -> Unit,
    횟수바꿈: (Int) -> Unit, 횟수넣기: (String) -> Unit,
    시간바꿈: (Int) -> Unit, 휴식바꿈: (Int) -> Unit, 지움: () -> Unit,
    modifier: Modifier = Modifier,
) {
    세트표틀(칸, 번호, 지울수있음, 지움, modifier) {
        세트표값(칸, 번호, 방, 무게, 횟수, 휴식, 아래글, 휴식보임, 무게바꿈, 무게넣기, 횟수바꿈, 횟수넣기, 시간바꿈, 휴식바꿈)
    }
}
