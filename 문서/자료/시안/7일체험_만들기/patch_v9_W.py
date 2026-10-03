"""운동 화면 세트 줄 — 10-03 홍겸 님 ✎ 표시 4개 + 칩 통일 (v9 W).   python3 patch_v9_W.py 입력.html 출력.html

표시 → 고친 것
 epvt "kg · 회 · 휴식 박스 높이 30% 줄여 · 휴식칸도 30% 줄여도 · 세트 동그라미는 키우고 누를 수 있다는 시인성"
      → kg · 회 · 휴식 칸 높이 40 → 28 (70%). 휴식 칸 폭 148 → 104 (홍겸 님 폰 폭 420 기준 70%).
        세트 동그라미 28 → 36 · 2px 강조 테두리 · 강조색 번호 15 Bold · 흰 바탕 (= 테두리 단추 모양) · 누르는 동안 강조옅음
 up4z "세트 동그라미 좌우 여백 15px 늘리고 · 동그라미 크기 키우자 · 휴식 너비 줄였으니 공간 나올 거임"
      → 동그라미 칸 = 15 + 36 + 15 = 66 (왼쪽 여백 0 → 15, 오른쪽 4 → 19). 휴식에서 나온 44px 로 충당
 qnpi "누르면 체크 모양으로 · 완료된 줄은 '완료됐다' 는 느낌 · 시인성"
      → 체크하면 번호 대신 ✓(선 아이콘 18)이 든 초록(--좋음) 동그라미.
        끝난 줄 = 줄 바탕 --좋음옅음 · 칸 테두리와 ± 를 감춰 '적어 둔 기록' 처럼 (값은 그대로 읽힌다 · 눌러서 고칠 수는 있다).
        앱에서 '끝 · 달성' 은 초록(운동 칸 줄 막대 · 달성 알약)이라 같은 색을 썼다. 줄 높이 · 칸 폭은 체크 전후 같다
 ypk0 "운동 중 화면에서 휴식 조절 가능하게"
      → 휴식 칸 = − 1:30 ＋ (kg · 회 칸과 같은 부품). 15초씩 · 0:15 ~ 5:00.
        쉬는 동안에는 가운데가 '남은 시간 / 건너뛰기' (누르면 건너뛴다), ± 를 누르면 남은 시간도 같이 15초 늘고 준다
        (남은 시간보다 많이 줄이면 휴식이 바로 끝난다 — '휴식 끝' 알림은 원래 흐름 그대로)
 (통일) 띠 둘째 줄 1주 · 최고 칩 → 흰 박스 · 파란 글씨 (바탕 --강조글 · 글 --강조 · 11 Bold).
        흰 박스가 되어 ▲ 는 --오름(빨강) · ▼ 는 --내림(파랑) 색을 다시 쓴다
"""
import sys, pathlib
if len(sys.argv) != 3: raise SystemExit("쓰는 법: python3 patch_v9_W.py 입력.html 출력.html")
s = pathlib.Path(sys.argv[1]).read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:80]!r}: {c}번")
    s = s.replace(old, new)

# ══ 1. (통일) 1주 · 최고 칩 — ▲▼ 와 값만 <i> 로 감싸 오름 · 내림 색을 입힌다 ══
바꿈('''    return `<span class="운칩 ${d>0?"오름":d<0?"내림":""}">${이름} ${d===0?"유지":(d>0?"▲":"▼")+kg(Math.abs(d))}</span>`; };   // 10-03 ✎ 3ehg — 띠 한 줄에 들어가게 단위를 뺐다''',
'''    return `<span class="운칩 ${d>0?"오름":d<0?"내림":""}">${이름} ${d===0?"유지":`<i>${d>0?"▲":"▼"}${kg(Math.abs(d))}</i>`}</span>`; };   // 10-03 ✎ 3ehg — 띠 한 줄에 들어가게 단위를 뺐다 · v9 — 흰 칩이라 ▲▼ 에 색(<i>)''')

# ══ 2. epvt · up4z · qnpi · ypk0 — 세트 줄 ══
바꿈('''  const 하나=ss.종목[i].세트.length<=1;
  return `<div class="세트줄 ${k===지금k&&!쉼?"지금":""}"><button class="체크 세트번호 ${s.완료?"켬":""}" data-enter="체${i}-${k}-${s.완료?1:0}" data-act="체크" data-i="${i}" data-k="${k}" aria-label="${k+1}세트 ${s.완료?"체크 풀기":"완료"}" aria-pressed="${s.완료}">${k+1}</button>${값("w",s.w)}${값("r",s.r)}
    ${쉼?`<button class="휴식칸 쉼" data-act="휴식건너뛰기" aria-label="휴식 건너뛰기"><span class="밑"><b data-timer="휴식"></b><small>건너뛰기</small></span><span class="위" data-쉼바><b data-timer="휴식"></b><small>건너뛰기</small></span></button>`:`<div class="휴식칸 빈 숫">${분초(s.휴)}</div>`}
    <button class="세트지움"''',
'''  const 하나=ss.종목[i].세트.length<=1;
  /* 10-03 ✎ qnpi — 체크하면 번호 대신 ✓ · 끝난 줄은 '완료줄'(초록 바탕 · 칸 테두리와 ± 감춤).
     ✎ ypk0 — 휴식 칸도 kg · 회 와 같은 − 값 ＋ (15초씩). 쉬는 동안 가운데 = 남은 시간 / 건너뛰기 */
  const 휴=(d,글)=>`<button data-act="세트값" data-f="휴" data-i="${i}" data-k="${k}" data-d="${d}" aria-label="휴식 ${글}">${d<0?"−":"＋"}</button>`;
  return `<div class="세트줄 ${k===지금k&&!쉼?"지금":""} ${s.완료?"완료줄":""}"><button class="체크 세트번호 ${s.완료?"켬":""}" data-enter="체${i}-${k}-${s.완료?1:0}" data-act="체크" data-i="${i}" data-k="${k}" aria-label="${k+1}세트 ${s.완료?"체크 풀기":"완료"}" aria-pressed="${s.완료}">${s.완료?아이콘.체크:k+1}</button>${값("w",s.w)}${값("r",s.r)}
    <div class="값칸 휴칸 ${쉼?"쉼":""}">${휴(-1,"15초 줄이기")}${쉼?`<button class="쉼단추" data-act="휴식건너뛰기" aria-label="휴식 건너뛰기"><span class="밑"><b data-timer="휴식"></b><small>건너뛰기</small></span><span class="위" data-쉼바><b data-timer="휴식"></b><small>건너뛰기</small></span></button>`:`<span class="숫">${분초(s.휴)}</span>`}${휴(1,"15초 늘리기")}</div>
    <button class="세트지움"''')

# ══ 3. ypk0 — 휴식 ± (세트값 f="휴") ══
바꿈('''    case "세트값": { const s=ss.종목[+d.i].세트[+d.k]; if(d.f==="w") s.w=Math.max(0,Math.round((s.w+(+d.d)*S.설정.무게폭)*10)/10); else s.r=Math.max(0,s.r+(+d.d)); ss.마지막=S.시계; break; }''',
'''    case "세트값": { const s=ss.종목[+d.i].세트[+d.k]; if(d.f==="w") s.w=Math.max(0,Math.round((s.w+(+d.d)*S.설정.무게폭)*10)/10); else if(d.f==="휴") 휴식바꿈(+d.i,+d.k,+d.d); else s.r=Math.max(0,s.r+(+d.d)); ss.마지막=S.시계; break; }''')

바꿈('''/* 같은 종목의 지난 기록 중 최고 (오늘 앞) · 1주 = 7일 전 ~ 전날 (v0.6.10) */
function 운최고(''',
'''/* 10-03 ✎ ypk0 — 휴식 15초씩 · 0:15 ~ 5:00. 그 세트가 지금 쉬는 중이면 남은 시간도 같은 만큼 늘고 준다.
   남은 시간보다 많이 줄이면 끝 = 지금 → 다음 시계 틱이 '휴식 끝' 으로 닫는다 (원래 흐름) */
const 휴식폭=15, 휴식최소=15, 휴식최대=300;
function 휴식바꿈(i,k,d){ const ss=S.세션, s=ss?.종목[i]?.세트[k]; if(!s) return; const 전=+s.휴||0;
  let 새=Math.max(휴식최소,Math.min(휴식최대,Math.round((전+d*휴식폭)/휴식폭)*휴식폭));
  if((d<0&&새>전)||(d>0&&새<전)) 새=전;                                   // 범위 밖에서 반대로 튀지 않게
  const 차=새-전; s.휴=새; if(!차) return;
  const h=ss.휴식; if(h&&h.i===i&&h.k===k){ h.끝=Math.max(S.시계,h.끝+차*1000); h.길이=Math.max(1,h.길이+차); } }
/* 같은 종목의 지난 기록 중 최고 (오늘 앞) · 1주 = 7일 전 ~ 전날 (v0.6.10) */
function 운최고(''')

# ══ 4. CSS ══
css = '''
/* ═══ v9 W — 운동 화면 세트 줄 ✎ 표시 4개 (epvt · up4z · qnpi · ypk0) + 1주 · 최고 칩 ═══ */
/* 칩 — 흰 박스 · 파란 글씨 (작은흰과 같은 뜻). 흰 바탕이라 ▲ 오름(빨강) · ▼ 내림(파랑) 색이 다시 읽힌다 */
.운머리 .운수치 .운칩{background:var(--강조글);color:var(--강조);border-color:var(--강조글);font-weight:700}
.운머리 .운수치 .운칩 i{font-style:normal;margin-left:4px}   /* 칩이 inline-flex 라 '최고 ' 끝 빈칸이 사라진다 → 여백 4 로 띄운다 */
.운머리 .운수치 .운칩.오름 i{color:var(--오름)}
.운머리 .운수치 .운칩.내림 i{color:var(--내림)}
/* epvt · up4z — [동그라미 칸 66 = 여백 15 + 36 + 15] [kg] [회] [휴식] [휴지통 28].
   홍겸 님 폰(폭 420 · 목록 안쪽 396)에서 정확히 66 · kg 80 · 회 102 · 휴식 104 (= 지금 148 의 70%).
   휴식에서 나온 44 를 동그라미 여백 30 · kg · 회 가 나눠 받는다.
   더 좁은 폰에서는 다 같은 비율로 줄되 동그라미 칸 44(여백 4) · kg 72('102.5') · 회 56 · 휴식 86 아래로는 안 줄어든다.
   휴식 86 = 쉬는 동안 ± 사이에 '건너뛰기'(11 Bold 약 44) 가 들어가는 폭 */
.운세트들 .세트머리,.운세트들 .세트줄{grid-template-columns:minmax(44px,66fr) minmax(72px,80fr) minmax(56px,102fr) minmax(86px,104fr) 28px}
.운세트들 .세트번호{justify-self:center}
.운세트들 .값칸{height:28px}
.운세트들 .세트지움{height:28px}
/* epvt — 세트 동그라미 = 누르는 단추로 보이게: 36 · 2px 강조 테두리 · 강조 번호 15 Bold · 흰 바탕. 누르는 동안 강조옅음 */
.운세트들 .세트번호{width:36px;height:36px;border:2px solid var(--강조);background:var(--면);color:var(--강조);font-size:15px}
.운세트들 .세트번호:active:not(:disabled){background:var(--강조옅음)}
/* qnpi — 체크 = 초록 동그라미 + ✓ (선 아이콘 18). 앱에서 끝 · 달성은 초록 */
.운세트들 .세트번호.켬{background:var(--좋음);border-color:var(--좋음);color:var(--좋음글)}
.운세트들 .세트번호 svg{width:18px;height:18px}
/* qnpi — 끝난 줄: 초록 옅은 바탕 · 칸 테두리와 ± 를 감춰 '적어 둔 기록' 으로. 값 글자는 흐림 (읽히되 앞으로 할 줄보다 한 단계 뒤).
   ± 는 visibility 로만 감춰 칸 폭 · 값 자리가 체크 전후로 안 바뀐다 */
.운세트들 .세트줄.완료줄{background:var(--좋음옅음);border-radius:8px}
.운세트들 .완료줄 .값칸:not(.쉼){border-color:transparent}
.운세트들 .완료줄 .값칸:not(.쉼) button{visibility:hidden}
.운세트들 .완료줄 .값칸:not(.쉼) input,.운세트들 .완료줄 .값칸:not(.쉼) span{color:var(--흐림)}
/* ypk0 — 휴식 칸 = − 값 ＋ (kg · 회 칸과 같은 부품 · 같은 글자) */
.운세트들 .휴칸>span{font-size:15px;color:var(--글)}
/* ypk0 — 쉬는 동안: 강조 테두리 · 가운데 단추 = 남은 시간(13) / 건너뛰기(11). 남은 만큼 강조로 채워져 줄어든다 */
.운세트들 .휴칸.쉼{border-color:var(--강조)}
.운세트들 .값칸 .쉼단추{flex:1;width:auto;min-width:0;height:100%;position:relative;overflow:hidden;padding:0}
.운세트들 .쉼단추 .밑,.운세트들 .쉼단추 .위{position:absolute;inset:0;display:flex;flex-wrap:wrap;align-items:center;align-content:center;justify-content:center;column-gap:4px;line-height:1.05}
.운세트들 .쉼단추 .밑{color:var(--강조)}
.운세트들 .쉼단추 .위{background:var(--강조);color:var(--강조글)}
.운세트들 .쉼단추 b{font-size:13px;font-weight:700;font-variant-numeric:tabular-nums}
.운세트들 .쉼단추 small{font-size:11px;font-weight:700;white-space:nowrap}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]

pathlib.Path(sys.argv[2]).write_text(s, encoding='utf-8')
print("→", sys.argv[2], f"{len(s.encode()):,} 바이트")
