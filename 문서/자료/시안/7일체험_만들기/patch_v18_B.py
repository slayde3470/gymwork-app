"""v18 B (10-04 홍겸 님) — 운동 보고서 · 큰운동판 · 끝 단추줄 · 보고서 표시 방법 '보일 것'
1 루틴 상자를 누르면 '○○ 루틴 상세' 시트 (큰운동상세와 같은 틀 · 형태만 — '준비 중' 한 줄)
2 '루틴 결과' 스위치 없앰 — 루틴 상자는 늘 보인다 (S.설정.보고서보임.루틴 은 늘 true). 프로필 스위치 · 톱니 옮김은 그대로
3 끝 단추: '‹' 왼쪽 세로 가운데 + '운동으로/돌아가기' · '기록없이/종료하기' + '›' 오른쪽 세로 가운데. 두 줄 4글자 양끝 정렬
4 버림 확인: 바탕 --나쁨 · 글 --강조글 · '다시 누르면/저장하지 않습니다.' (11 · '›' 뺌)
5 파란 상자(.큰합) 너비 ×1.1 — 프로필 탭 · 보고서 같은 부품
"""
import sys, pathlib
IN, OUT = sys.argv[1], sys.argv[2]
s = pathlib.Path(IN).read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    k = s.count(old)
    if k != n: raise SystemExit(f"{k}번 (기대 {n}): {old[:80]}")
    s = s.replace(old, new)

# ── 2 루틴 결과는 끄고 켤 수 없다 — 저장된 값이 false 여도 늘 true ──
바꿈('''const 보=s.보고서보임; 보.프로필=보.프로필!==false; 보.루틴=보.루틴!==false; if(!보.프로필&&!보.루틴) 보.루틴=true;   // 둘 다 꺼진 값은 받지 않는다''',
     '''const 보=s.보고서보임; 보.프로필=보.프로필!==false; 보.루틴=true;   // v18 홍겸 님 "루틴결과는 끄고 킬수있는게 아님" — 늘 보인다''')
# 시트 '보일 것' — 프로필 줄만
바꿈('''    <div>${설정줄("프로필","",스위치(보임.프로필,"보고보임","프로필"))}<div class="구분"></div>${설정줄("루틴 결과","",스위치(보임.루틴,"보고보임","루틴"))}</div>''',
     '''    <div>${설정줄("프로필","",스위치(보임.프로필,"보고보임","프로필"))}</div>''')

# ── 1 루틴 상자 = 누르는 곳 (div 그대로 · role=button. 안의 톱니는 제 단추라 먼저 잡힌다) ──
바꿈('''    ${루틴보임?`<div class="보고상자 보고루틴${프로필보임?"":" 톱니있음"} 번호"${번("결2")} data-enter="보고루틴${상.키}">''',
     '''    ${루틴보임?`<div class="보고상자 보고루틴${프로필보임?"":" 톱니있음"} 번호"${번("결2")} data-enter="보고루틴${상.키}" data-act="시트" data-t="루틴상세" data-v="${esc(결과루틴이름(rec.이름))}" role="button" tabindex="0" aria-label="${esc(결과루틴이름(rec.이름))} 상세">''')
# 시트 — 큰운동상세와 같은 틀(머리 띠 + 닫기), 내용은 준비 중 한 줄
바꿈('''/* 프로필 동그라미 속 — 사진 / 닉네임 첫 글자 / 사람 그림''',
     '''/* v18 '○○ 루틴 상세' 시트 — 형태만 (홍겸 님 "형태만 만들어놔") */
function 루틴상세시트(글){
  return `<div class="머리"><b>${esc(글)} 상세</b><button class="닫기" data-act="시트닫기">닫기</button></div>
    <div class="아주작 옅음">자세한 기록은 준비 중입니다</div>`; }
/* 프로필 동그라미 속 — 사진 / 닉네임 첫 글자 / 사람 그림''')
바꿈('''  else if(종==="큰운동상세") 안 = 큰운동상세시트(U.시트.대상);   // v17 파란 상자 · 칸 누름''',
     '''  else if(종==="큰운동상세") 안 = 큰운동상세시트(U.시트.대상);   // v17 파란 상자 · 칸 누름
  else if(종==="루틴상세") 안 = 루틴상세시트(U.시트.대상);   // v18 보고서 루틴 상자 누름''')

# ── 3 · 4 끝 단추줄 ──
바꿈('''<button class="버튼 끝작" data-act="운동으로">‹ 운동으로 돌아가기</button><button class="버튼 주 끝큰" data-act="운동저장">운동 기록 저장하고 종료</button><button class="버튼 끝작 나쁨${ss.버림?" 확인중":""}" data-act="운동버림">${ss.버림?"한 번 더 누르면 버립니다":"기록하지 않고 종료 ›"}</button>''',
     '''<button class="버튼 끝작 끝왼" data-act="운동으로" aria-label="운동으로 돌아가기"><i class="끝살" aria-hidden="true">‹</i>${양끝글("운동으로","돌아가기")}</button><button class="버튼 주 끝큰" data-act="운동저장">운동 기록 저장하고 종료</button><button class="버튼 끝작 끝오 나쁨${ss.버림?" 확인중":""}" data-act="운동버림"${ss.버림?' aria-label="다시 누르면 저장하지 않습니다"':' aria-label="기록없이 종료하기"'}>${ss.버림?`<span class="끝확인글">다시 누르면<br>저장하지 않습니다.</span>`:`${양끝글("기록없이","종료하기")}<i class="끝살" aria-hidden="true">›</i>`}</button>''')
# 두 줄 4글자 — 글자마다 칸을 나눠 양끝 정렬 (글꼴이 글자 폭을 조금씩 달리 줘도 두 줄 폭이 같다)
바꿈('''function 결과뷰(rec, 저장키값){''',
     '''/* v18 홍겸 님 "8글자는 글자간격을 정렬" — 두 줄을 넓은 줄 폭에 맞춰 양끝 정렬 */
const 양끝글 = (...줄들) => `<span class="양끝" aria-hidden="true">${줄들.map(l=>`<span>${[...l].map(c=>`<i>${esc(c)}</i>`).join("")}</span>`).join("")}</span>`;
function 결과뷰(rec, 저장키값){''')

css = '''
/* ═══ 10-04 v18 B — 보고서 루틴 상자 상세 · 끝 단추 두 줄 · 버림 확인 빨강 · 파란 상자 ×1.1 ═══ */
/* 1 루틴 상자 = 누르는 곳 */
.보고상자.보고루틴[data-act]{cursor:pointer}
/* 3 끝 단추 — 살표는 단추 왼쪽/오른쪽 세로 가운데(4 안쪽), 글은 가운데 두 줄(52). 안쪽 여백은 v17 그대로 4 —
   여백을 늘리면 flex 가 여백만큼 더 줘서 1:2:1 이 깨진다. 글이 가운데라 살표(4~13)와 안 겹친다 (360 에서도 글 왼끝 15) */
.결과셋 .버튼.끝작{position:relative}
.결과셋 .끝살{position:absolute;top:50%;transform:translateY(-50%);font-style:normal;font-size:15px;line-height:1}
.결과셋 .끝왼 .끝살{left:4px}
.결과셋 .끝오 .끝살{right:4px}
.결과셋 .양끝{display:inline-grid;line-height:16px}
.결과셋 .양끝>span{display:flex;justify-content:space-between}
.결과셋 .양끝 i{font-style:normal}
/* 4 버림 확인 — 빨간 상자 · 흰 글. 25% 칸에 11 로 (줄 12 · 길면 세 줄 = 36 ≤ 40) · 살표 없음 */
.결과셋 .버튼.끝작.확인중{background:var(--나쁨);border-color:var(--나쁨);color:var(--강조글);font-size:11px;line-height:12px}
/* 5 파란 상자 너비 — v17 의 (칸 줄 − 30) × .85 에서 ×1.1 = × .935 */
.결과수.큰수 .큰합{width:calc((100% - 30px) * .935)}
'''
i = s.rfind('</style>')
s = s[:i] + css + s[i:]
pathlib.Path(OUT).write_text(s, encoding='utf-8')
print("OK", OUT)
