"""운동 화면 — 10-03 홍겸 님 ✎ 표시 4개 반영 (v8 W).   python3 patch_v8_W.py 입력.html 출력.html

표시 → 고친 것
 r0yk "플랜 15회차 X → [플랜] 박스 · N회차 · N주 진행 중"
      → 띠 안 글을 '15회차 · 8주 진행 중' 으로 (주 = 플랜 만든 날부터 오늘까지 지난 주 + 1).
        플랜 종목이면 이름 끝 글자 오른쪽 위에 5px 겹친 작은 [플랜] 표 (강조 바탕 · 강조글 · 11 Bold · 모서리 8).
        띠도 강조 바탕이라 표에 1px 강조글 테두리를 둘렀다
 5gd1 "왼쪽으로 20px 이동" (띠 오른쪽 끝 타이머) → 타이머 오른쪽에 20px
 3ehg "종목 이름 옆으로 이동 · 띠를 키우고 1RM · 달성 · 볼륨이 한 줄로"
      → 아래 상자의 지표 두 줄을 띠의 둘째 줄로 올려 한 줄로 합쳤다:
        1RM 78kg [1주 ▼2][최고 ▼2] · 달성 100% · 볼륨 2,700/2,700kg
        띠 위라 글자 · 칩 테두리 · ▲▼ 모두 강조글 (빨강/파랑은 강조 바탕 위에서 안 읽힌다 → ▲▼ 기호가 방향을 말한다).
        좁으면 맞춤글(자간 → 글자 크기)로 줄인다. 줄 높이는 28 로 고정 — 값이 바뀌어도 띠 높이가 안 변한다.
        아래 상자에는 루틴 요약 · 진행 막대 · 종목 칸 줄만 남는다
 ai39 "휴식누르면 건너뛰기 어디감? · 마지막 세트 건너뛰기 → 다음 운동 · kg 박스 60% · 회 박스 75% ·
       체크는 맨 왼쪽 세트 번호 동그라미로 · 맨 오른쪽엔 휴지통"
      → 쉬는 동안 휴식 칸에 '0:43 건너뛰기' — 칸을 누르면 건너뛴다.
        그 종목 세트가 다 끝난 뒤의 휴식을 건너뛰면 다음 안 끝난 종목으로 넘어간다 (칸 줄도 따라간다)
      → 세트 번호가 동그라미 체크 단추 (누르면 체크/풀기 · 체크되면 채운 동그라미 + 번호)
      → 맨 오른쪽 = 휴지통 (묻지 않고 지우고 아래띠 '되돌리기' 5초 · U5-4). 세트가 하나뿐이면 꺼 둔다. '− 세트' 단추는 뺐다
      → kg 칸 = 지금(420 폭에서 130)의 약 60% = 78 · 회 칸 = 약 75% = 98. 남은 폭은 휴식 칸이 받는다('건너뛰기' 자리).
        '60% 줄여' 를 '60% 만큼 빼기'(→ 52) 로 읽으면 − 값 ＋ 가 안 들어가서 '지금의 60%' 로 읽었다.
        좁아진 칸에 값이 들어가게 − ＋ 폭을 28 → 20 으로 (높이는 그대로 40) · 긴 값('102.5')은 글자를 13 → 11 로 줄인다.
        더 좁은 폰에서는 같은 비율로 줄되 kg 76 · 회 64 · 휴식 56 아래로는 안 줄어든다
"""
import sys, pathlib
if len(sys.argv) != 3: raise SystemExit("쓰는 법: python3 patch_v8_W.py 입력.html 출력.html")
s = pathlib.Path(sys.argv[1]).read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:80]!r}: {c}번")
    s = s.replace(old, new)

# ══ 1. r0yk · 3ehg — 비교 칩 글 (단위는 앞의 1RM 값이 이미 말한다 · 띠 한 줄에 들어가게) ══
바꿈('''    return `<span class="운칩 ${d>0?"오름":d<0?"내림":""}">${이름} ${d===0?"유지":(d>0?"▲ ":"▼ ")+kg(Math.abs(d))+단위}</span>`; };''',
'''    return `<span class="운칩 ${d>0?"오름":d<0?"내림":""}">${이름} ${d===0?"유지":(d>0?"▲":"▼")+kg(Math.abs(d))}</span>`; };   // 10-03 ✎ 3ehg — 띠 한 줄에 들어가게 단위를 뺐다
  /* 10-03 ✎ r0yk — '플랜 15회차' → '15회차 · N주 진행 중'. 주 = 플랜 만든 날부터 오늘까지 지난 주 + 1 (만든 주 = 1주).
     [플랜] 표는 이름 끝 낱말에 붙인 빈 <i> (글은 CSS) — 끝 낱말과 같이 감기고, 이름 글(textContent)에는 안 섞인다 */
  const 플p=e.플랜id?플랜(e.플랜id):null, 주=플p&&플p.만든날?Math.floor(Math.max(0,날차(플p.만든날,오늘()))/7)+1:null;
  const 낱=e.이름.split(" "), 끝말=낱.pop();
  const 이름칸=e.플랜id?`<b class="운이름">${낱.length?esc(낱.join(" "))+" ":""}<span class="운끝말">${esc(끝말)}<i class="운플랜표" title="플랜"></i></span></b><span class="운곁">${e.회}회차${e.측정일?" · 측정일":""}${주?` · ${주}주 진행 중`:""}</span>`
    : `<b class="운이름">${esc(e.이름)}</b>`;''')

# ══ 2. r0yk · 5gd1 · 3ehg — 띠 두 줄: ‹ · 이름[플랜] · N회차 · 타이머 / 1RM · 1주 · 최고 · 달성 · 볼륨 ══
바꿈('''    <div class="운머리 띠 번호"${번("운0")}><button class="운나감" data-act="탭" data-t="캘린더" aria-label="나가기">‹</button>
      <div class="운글"><b class="운이름">${esc(e.이름)}</b>${e.플랜id?`<span class="운곁">플랜 ${e.회}회차${e.측정일?" · 측정일":""}</span>`:""}</div>
      <div class="운오른">${그림보임?"":`<button class="작은흰" data-act="배너보기">그림</button>`}<span class="숫 굵 큰" data-timer="경과"></span></div></div>''',
'''    <div class="운머리 띠 두줄 번호"${번("운0")}><div class="운첫줄"><button class="운나감" data-act="탭" data-t="캘린더" aria-label="나가기">‹</button>
      <div class="운글">${이름칸}</div>
      <div class="운오른">${그림보임?"":`<button class="작은흰" data-act="배너보기">그림</button>`}<span class="숫 굵 큰 운시계" data-timer="경과"></span></div></div>
      <div class="운수치 맞춤"><span>${맨?"최고":"1RM"} <b>${오==null?"—":kg(Math.round(오*2)/2)+단위}</b></span>${비("1주",지.주)}${비("최고",지.최고)}<i class="운점" aria-hidden="true">·</i><span>달성 <b>${e.세트.length?Math.round(완(e)/e.세트.length*100):0}%</b></span><i class="운점" aria-hidden="true">·</i><span>볼륨 <b>${콤마(Math.round(볼(e)))}</b><span class="운목표">/${콤마(Math.round(볼(e,true)))}</span>kg</span></div></div>''')

# 아래 상자 — 지표 두 줄을 뺀다 (띠로 올라갔다)
바꿈('''      <div class="운지표들"><div class="운지표"><span>${맨?"최고":"1RM"} <b>${오==null?"—":kg(Math.round(오*2)/2)+단위}</b></span>${비("1주",지.주)}${비("최고",지.최고)}</div>
        <div class="운지표"><span>달성 <b>${e.세트.length?Math.round(완(e)/e.세트.length*100):0}%</b></span><span>볼륨 <b>${콤마(Math.round(볼(e)))}</b>/${콤마(Math.round(볼(e,true)))}kg</span></div></div>
''', '')

# 쓰는 곳이 없어진 아래 상자 지표 묶음 CSS 를 지운다 (U4-6)
바꿈('''/* zx75 — 이 종목 지표 두 줄을 아래 상자 맨 위로. 루틴 진행과는 선 하나로 나눈다 */
.운지표들{display:flex;flex-direction:column;gap:4px;padding-bottom:8px;margin-bottom:8px;border-bottom:1px solid var(--선)}
.운지표들 .운지표{margin-top:0;min-width:0}
''', '''/* zx75 지표 두 줄 → 10-03 ✎ 3ehg 로 띠 둘째 줄로 옮겼다 (아래 '운동 화면 ✎ 표시 4개') */
''')

# ══ 3. ai39 — 세트 머리 · '− 세트' 빼기 · 지움 되돌리기 띠 ══
바꿈('''    <div class="운세트들"><div class="세트머리 아주작 옅음"><span>세트</span><span>kg</span><span>회</span><span>휴식</span><span>완료</span></div>
      ${e.세트.map((s,k)=>운세트줄(본,s,k,지금k)).join("")}
      <div class="줄"><button class="버튼 낮 채움" data-act="세트더" data-i="${본}">+ 세트</button>${e.세트.length>1?`<button class="버튼 낮" data-act="세트빼기" data-i="${본}">− 세트</button>`:""}</div></div>''',
'''    <div class="운세트들"><div class="세트머리 아주작 옅음"><span>세트</span><span>kg</span><span>회</span><span>휴식</span><span></span></div>
      ${e.세트.map((s,k)=>운세트줄(본,s,k,지금k)).join("")}
      <div class="줄"><button class="버튼 낮 채움" data-act="세트더" data-i="${본}">+ 세트</button></div></div>''')

바꿈('''<button class="버튼 주 채움" data-act="끝내기">마무리</button>`}</div>
    ${시트()}</div>`; }''',
'''<button class="버튼 주 채움" data-act="끝내기">마무리</button>`}</div>
    ${시트()}</div>${운지움띠()}`; }''')

# ══ 4. ai39 — 세트 줄: [번호 = 체크 동그라미] kg 회 휴식(건너뛰기) [휴지통] ══
바꿈('''function 운세트줄(i,s,k,지금k){ const ss=S.세션, 쉼=ss.휴식&&ss.휴식.i===i&&ss.휴식.k===k;''',
'''/* 10-03 ✎ ai39 — kg 칸이 좁아져 '102.5' 같은 긴 값은 글자를 줄여 넣는다 (15 → 13 → 11). 그린 뒤 · 칠 때마다 */
function 운입력맞춤(el){ el.style.fontSize=""; for(const fs of [13,11]){ if(el.scrollWidth<=el.clientWidth+1) break; el.style.fontSize=fs+"px"; } }
document.addEventListener("input", e=>{ if(e.target.matches?.(".운세트들 .값칸 input")) 운입력맞춤(e.target); });
/* 10-03 ✎ ai39 — 세트 줄 맨 오른쪽 휴지통 (선 아이콘 · stroke 2). 아이콘 객체는 다른 손이 고치는 중이라 따로 둔다 */
const 운휴지통 = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4 7h16M9 7V4h6v3M6 7l1 13h10l1-13"/></svg>`;
/* 10-03 ✎ ai39 — 지우기는 묻지 않고 지우고 아래띠로 5초 되돌리기 (U5-4 · 동작방식 D3-4) */
let 운지움번호 = 0;
function 운지움띠(){ const z=U.운지움, ss=S.세션; if(!z||!ss||z.세션!==ss.시작) return "";
  return `<div class="아래띠 운지움띠" role="status" data-enter="운지움${z.id}"><span class="채움">${esc(ss.종목[z.i]?.이름||"")} ${z.k+1}세트를 지웠습니다</span><button data-act="세트되돌림">되돌리기</button></div>`; }
function 세트지우기(i,k){ const ss=S.세션, x=ss?.종목[i]; if(!x||!x.세트[k]||x.세트.length<=1) return;   // 하나 남으면 지우지 않는다
  const id=++운지움번호; U.운지움={id, 세션:ss.시작, i, k, 세트:x.세트[k], 지금:{...ss.지금}, 휴식:ss.휴식?{...ss.휴식}:null};
  x.세트.splice(k,1); ss.마지막=S.시계;
  if(ss.휴식&&ss.휴식.i===i){ if(ss.휴식.k===k) ss.휴식=null; else if(ss.휴식.k>k) ss.휴식.k--; }
  if(ss.지금.i===i&&ss.지금.k>=k){ if(ss.지금.k>k) ss.지금.k--;
    else { const j=x.세트.findIndex((s,j)=>j>=k&&!s.완료); ss.지금 = j>=0 ? {i,k:j} : (다음세트(i,Math.max(0,k-1)) || {i,k:Math.min(k,x.세트.length-1)}); } }
  setTimeout(()=>{ if(U.운지움?.id!==id) return; U.운지움=null;
    document.querySelectorAll("#폰 .운지움띠").forEach(el=>{ el.classList.add("나감띠"); setTimeout(()=>el.remove(),200); }); }, 5000); }
function 세트되돌리기(){ const z=U.운지움, ss=S.세션; U.운지움=null; if(!z||!ss||z.세션!==ss.시작) return; const x=ss.종목[z.i]; if(!x) return;
  x.세트.splice(Math.min(z.k,x.세트.length),0,z.세트); ss.지금=z.지금;
  if(z.휴식 && z.휴식.끝>S.시계) ss.휴식=z.휴식; else if(ss.휴식&&ss.휴식.i===z.i&&ss.휴식.k>=z.k) ss.휴식.k++; }
function 운세트줄(i,s,k,지금k){ const ss=S.세션, 쉼=ss.휴식&&ss.휴식.i===i&&ss.휴식.k===k;''')

바꿈('''  return `<div class="세트줄 ${k===지금k&&!쉼?"지금":""}"><span class="k">${k+1}</span>${값("w",s.w)}${값("r",s.r)}
    ${쉼?`<button class="휴식칸" data-act="휴식건너뛰기" aria-label="휴식 건너뛰기"><span class="밑" data-timer="휴식"></span><span class="위" data-쉼바><span data-timer="휴식"></span></span></button>`:`<div class="휴식칸 빈 숫">${분초(s.휴)}</div>`}
    <button class="체크 ${s.완료?"켬":""}" data-enter="체${i}-${k}-${s.완료?1:0}" data-act="체크" data-i="${i}" data-k="${k}" aria-label="${k+1}세트 ${s.완료?"체크 풀기":"완료"}">${s.완료?아이콘.체크:""}</button></div>`; }''',
'''  /* 10-03 ✎ ai39 — 세트 번호가 곧 체크 동그라미(누르면 체크/풀기 · 체크되면 채운 동그라미 + 번호). 맨 오른쪽은 휴지통.
     쉬는 동안 휴식 칸 = '0:43 건너뛰기' (칸을 누르면 건너뛴다 — 전에는 시간만 보여 누를 수 있는 줄 몰랐다) */
  const 하나=ss.종목[i].세트.length<=1;
  return `<div class="세트줄 ${k===지금k&&!쉼?"지금":""}"><button class="체크 세트번호 ${s.완료?"켬":""}" data-enter="체${i}-${k}-${s.완료?1:0}" data-act="체크" data-i="${i}" data-k="${k}" aria-label="${k+1}세트 ${s.완료?"체크 풀기":"완료"}" aria-pressed="${s.완료}">${k+1}</button>${값("w",s.w)}${값("r",s.r)}
    ${쉼?`<button class="휴식칸 쉼" data-act="휴식건너뛰기" aria-label="휴식 건너뛰기"><span class="밑"><b data-timer="휴식"></b><small>건너뛰기</small></span><span class="위" data-쉼바><b data-timer="휴식"></b><small>건너뛰기</small></span></button>`:`<div class="휴식칸 빈 숫">${분초(s.휴)}</div>`}
    <button class="세트지움" data-act="세트지움" data-i="${i}" data-k="${k}" aria-label="${k+1}세트 지우기"${하나?" disabled":""}>${운휴지통}</button></div>`; }''')

# 다시 그린 직후 kg · 회 값 글자 맞추기 (운자리맞춤 안 — 운동 화면일 때만 돈다)
바꿈('''function 운자리맞춤(폰){ const ss=S.세션, 목=폰.querySelector(".운세트들"), 줄=폰.querySelector(".운띠"); if(!ss||!목||!줄) return;''',
'''function 운자리맞춤(폰){ const ss=S.세션, 목=폰.querySelector(".운세트들"), 줄=폰.querySelector(".운띠"); if(!ss||!목||!줄) return;
  목.querySelectorAll(".값칸 input").forEach(운입력맞춤);   // 10-03 ✎ ai39
  const 수=폰.querySelector(".운수치"); if(수 && 수.scrollWidth>수.clientWidth/0.9) 수.classList.add("좁");   // 10-03 ✎ 3ehg — 좁은 폰에서는 볼륨 목표를 감춘다''')

# ══ 5. ai39 — 행동: 건너뛰기 → 그 종목이 다 끝났으면 다음 종목으로 · 세트 지우기 · 되돌리기 ══
바꿈('''    case "휴식건너뛰기": ss.휴식=null; break;''',
'''    case "휴식건너뛰기": { const 쉼=ss.휴식; ss.휴식=null;
      /* 10-03 ✎ ai39 "마지막 세트에서 건너뛰기하면 자동으로 다음 운동으로" — 그 종목 세트가 다 끝난 뒤의 휴식이면
         다음 안 끝난 종목(뒤쪽 먼저, 없으면 앞쪽)으로. 칸 줄은 운자리맞춤()이 따라간다 */
      if(쉼 && U.본===쉼.i && ss.종목[쉼.i]?.세트.every(s=>s.완료)){
        const 남=x=>x.세트.some(s=>!s.완료), 뒤=ss.종목.findIndex((x,j)=>j>쉼.i&&남(x)), 앞=ss.종목.findIndex(남), 갈=뒤>=0?뒤:앞;
        if(갈>=0) U.본=갈; }
      break; }
    case "세트지움": 세트지우기(+d.i,+d.k); break;      // 10-03 ✎ ai39 — 묻지 않고 지우고 아래띠로 되돌린다 (U5-4)
    case "세트되돌림": 세트되돌리기(); break;''')

# ══ 6. CSS ══
css = '''
/* ═══ 10-03 운동 화면 ✎ 표시 4개 (r0yk · 5gd1 · 3ehg · ai39) ═══ */
/* 3ehg — 띠 두 줄. 첫 줄 = ‹ · 이름[플랜] · N회차 · 타이머 / 둘째 줄 = 이 종목 지표 한 줄 */
.운머리.띠.두줄{flex-direction:column;align-items:stretch;gap:4px}
.운첫줄{display:flex;align-items:center;gap:8px;min-height:32px;padding-top:4px}   /* 위 4 — 이름 위로 올라간 [플랜] 표가 띠 밖으로 안 나가게 */
/* 5gd1 — 타이머를 왼쪽으로 20px (홍겸 님이 준 값) */
.운머리 .운시계{margin-right:20px}
/* r0yk — [플랜] 표: 이름 끝 낱말 오른쪽 위에 5px 겹친다 (가로 −5px · 표 아래 끝이 글자 잉크 위 끝보다 5px 아래).
   띠도 강조 바탕이라 1px 강조글 테두리로 가른다. 끝 낱말과 표는 떨어져 감기지 않는다 */
.운머리 .운끝말{white-space:nowrap}
.운머리 .운플랜표{display:inline-block;position:relative;vertical-align:text-top;top:-8px;margin-left:-5px;font-style:normal;
  height:16px;line-height:14px;padding:0 4px;box-sizing:border-box;border:1px solid var(--강조글);border-radius:8px;background:var(--강조);color:var(--강조글);font-size:11px;font-weight:700}
.운머리 .운플랜표::before{content:"플랜"}
.운머리 .운곁{margin-left:8px}
/* 3ehg — 지표 한 줄. 높이 28 고정(값이 바뀌어도 띠가 안 움직인다) · 좁으면 맞춤글이 줄인다 — 그래서 글자는 em (11 · 15 = 15/11 em) */
.운머리 .운수치{display:flex;align-items:center;gap:4px;height:28px;min-width:0;white-space:nowrap;overflow:hidden;font-size:11px;color:var(--강조글);font-variant-numeric:tabular-nums}
.운머리 .운수치>span{flex:none}
.운머리 .운수치 b{font-size:calc(1em * 15 / 11);color:var(--강조글)}
.운머리 .운수치 .운칩{font-size:1em;padding:0 4px;color:var(--강조글);border-color:color-mix(in srgb,var(--강조글) 55%,transparent)}
.운머리 .운수치 .운점{font-style:normal;flex:none;margin:0 4px}
/* 좁은 폰(맞춤글로 10% 넘게 줄여야 할 때) — 볼륨 목표(/2,700)를 감춘다. 9px 까지 줄이면 못 읽는다 */
.운머리 .운수치.좁 .운목표{display:none}
/* ai39 — 세트 줄: [번호 동그라미 28] [kg] [회] [휴식 = 남는 폭] [휴지통 28].
   홍겸 님 폰(폭 420 · 목록 안쪽 396)에서 kg 78 · 회 98 = 지금(130)의 60% · 75%, 휴식 148.
   더 좁은 폰에서는 같은 비율로 줄되 kg 76 · 회 64 · 휴식 56 아래로는 안 줄어든다 (− 값 ＋ 가 들어갈 만큼) */
.운세트들 .세트머리,.운세트들 .세트줄{grid-template-columns:28px minmax(76px,78fr) minmax(64px,98fr) minmax(56px,148fr) 28px}
.운세트들 .값칸 button{width:20px;flex:none}
.운세트들 .세트번호{justify-self:center;font-size:13px;font-weight:700;color:var(--글);font-variant-numeric:tabular-nums}
.운세트들 .세트번호.켬{color:var(--강조글)}
.세트지움{width:28px;height:40px;display:flex;align-items:center;justify-content:center;color:var(--옅음)}
.세트지움 svg{width:18px;height:18px}
.세트지움:disabled{opacity:.35}
/* ai39 — 쉬는 동안 휴식 칸 = 남은 시간 + '건너뛰기'. 넓으면 한 줄, 좁으면 두 줄 (높이 40 그대로) */
.운세트들 .휴식칸.쉼 .밑,.운세트들 .휴식칸.쉼 .위{position:absolute;inset:0;display:flex;flex-wrap:wrap;align-items:center;align-content:center;justify-content:center;column-gap:4px;line-height:1.15}
.운세트들 .휴식칸.쉼 b{font-size:13px}
.운세트들 .휴식칸.쉼 small{font-size:11px;font-weight:700}
.운지움띠.나감띠{opacity:0;transition:opacity .2s}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]

pathlib.Path(sys.argv[2]).write_text(s, encoding='utf-8')
print("→", sys.argv[2], f"{len(s.encode()):,} 바이트")
