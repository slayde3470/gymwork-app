"""v20 (10-04 홍겸 님) — 종목 탭 상자 · 새 종목 시트
① 종목 이름 맞춤 — "종목이름이 잘리지 않도록 글씨크기를 줄여서라도 1줄로 표시" · "12글자가 넘으면 그땐 2줄로 표시"
   12글자(공백 포함 · 번호/플랜 딱지 빼고) 이하 = 한 줄, 넘으면 두 줄까지. 칸에 안 들어가면 15 → 13 → 11, 그래도 안 되면 자간을 좁힌다. '…' 없음
② 접힌 상자 = 이름(+딱지) · ▾ 만 — "접혀진 상태에서는 주동근, 협응근, 사진 박스 전부 표시되지 않게"
③ 펼친 상자에 [편집] → 새 종목 시트를 편집으로(제목 '○○ 편집' · 값이 채워진 채 · [저장] = 그 종목을 고침). 기본 종목도 고친다(근육 역할을 그 종목 데이터에)
④ 새 종목 시트 묶음 칩(가슴 · 등 …) 옆 개수 표시 삭제
⑤ 역할 = 주동근 · 협응근 둘(보조 S 없앰 — 저장값 P · Y, 옛 S 는 Y 로 읽음). 협응근 역할에서 이미 주동근인 부위는 흐리게 · 누르면 토스트
   '이미 주동근으로 선택되어있습니다.' (다시 그리지 않아 토스트가 남는다). 주동근 역할에서 협응근 부위를 누르면 주동으로 옮김(그대로). 그림 색도 P/Y 두 단계"""
import sys, pathlib
IN, OUT = sys.argv[1], sys.argv[2]
s = pathlib.Path(IN).read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:70]!r}: {c}번")
    s = s.replace(old, new)
def 구간(시작, 끝):
    """시작 ~ 끝(끝 글 포함) — 손으로 옮겨 적지 않고 원문에서 잘라 온다(전각 ＋ 같은 글자 실수 막기)"""
    i = s.index(시작); j = s.index(끝, i) + len(끝); return s[i:j]

# ── ② ③ 종목 탭 상자 ────────────────────────────────────────────────
바꿈('''    플=첫?S.플랜들.map((p,i)=>[p,i]).filter(([p])=>p.종목===x.이름):[], v=esc(키), 이=esc(x.이름), 번=같은이름번호(키,x.이름);''',
     '''    플=첫?S.플랜들.map((p,i)=>[p,i]).filter(([p])=>p.종목===x.이름):[], v=esc(키), 이=esc(x.이름), 번=같은이름번호(키,x.이름), 고칠=S.종목표.includes(x);   // v20 ③ '기타' 칸(플랜만 남은 종목)은 고칠 종목이 없다''')
옛머리 = 구간('''  return `<div class="종목칸 ${펼?"펼":""}"><div class="종목칸머리">''', '''${근육두줄(종목근육(키))}</button></div>\n''')
사진칸 = 구간('''<label class="작은사진 사진넣칸 ${l.length?"":"빈"}" aria-label="${이} 사진 넣기">''', '''</label>''')
assert 사진칸 in 옛머리
새머리 = ('''  /* 10-04 v20 ② 접힌 상자 = 이름(+번호 · 플랜 딱지) · ▾ 만. 사진 칸 · 주동근/협응근 줄은 펼쳤을 때만
     ① 이름은 맞춤이름() — 12글자 이하 한 줄 · 넘으면 두 줄, 칸에 맞게 줄인다(이름맞춤)
     ③ 펼치면 머리 오른쪽에 [편집] — 새 종목 시트를 편집으로 연다 */
  return `<div class="종목칸 ${펼?"펼":""}"><div class="종목칸머리">
    ${펼?`''' + 사진칸 + '''`:""}
    <button class="종목칸이름" data-act="종목펼침" data-v="${v}" aria-expanded="${펼}"><span class="종목칸첫줄"><span class="채움 종목줄이름">${맞춤이름(x.이름, 번호딱지(번)+(플.length?플랜딱지:""))}</span>${펼&&곁?`<span class="아주작 옅음">${곁}</span>`:""}<span class="아주작 옅음 접힘표 ${펼?"펼":""}">▾</span></span>${펼?근육두줄(종목근육(키)):""}</button>${펼&&고칠?`<button class="버튼 낮 종목편집" data-act="종목편집" data-v="${v}" aria-label="${이} 편집">편집</button>`:""}</div>
''')
바꿈(옛머리, 새머리)

바꿈('''   [사진 칸 28 = 사진 넣기][이름 · ▾ / 주동근 : … / 협응근 : …]. 이름 쪽을 누르면 펼친다(전과 같은 '종목펼침').''',
     '''   [사진 칸 28 = 사진 넣기][이름 · ▾ / 주동근 : … / 협응근 : …][편집] — v20 ② 사진 칸 · 근육 줄 · ③ [편집]은 펼쳤을 때만. 이름 쪽을 누르면 펼친다(전과 같은 '종목펼침').''')
# ── ① 이름 맞춤 (종목칸 바로 앞에) ──
바꿈('''/* ═══ 10-04 v18 C ②③ 종목 상자 — 종목마다 따로.''',
'''/* ═══ 10-04 v20 ① 종목 이름 맞춤 — 홍겸 님 "종목이름이 잘리지 않도록 글씨크기를 줄여서라도 1줄로 표시" · "12글자가 넘으면 그땐 2줄로 표시"
   맞춤이름(이름, 딱지) = <span.이름플랜><b.이름맞춤 data-줄>이름</b>딱지</span>. data-줄 = 1(12글자 이하 · 공백 포함, 딱지는 안 셈) · 2(넘으면).
   이름맞춤(root) — 그리기 · 창 크기 · 글꼴이 바뀔 때. 칸에 안 들어가면 글자 크기를 15 → 13 → 11(바탕보다 큰 단계는 건너뜀),
   11 에도 안 들어가면 자간을 0.01em 씩 −0.2em 까지 좁힌다. '…' 로 자르지 않는다.
   한 줄 이름이 −0.2em 에도 안 들어가면(딱지가 붙은 아주 좁은 칸 · 지금 종목에는 없음) 두 줄로 같은 순서를 다시 한다 — 잘리는 것보다 낫다 */
const 이름한줄최대=12, 이름글단계=[15,13,11], 이름자간끝=-0.2;
const 이름줄수 = 이름 => [...String(이름??"").trim()].length>이름한줄최대 ? 2 : 1;
const 맞춤이름 = (이름, 딱지="") => `<span class="이름플랜 맞춤이름"><b class="이름맞춤" data-줄="${이름줄수(이름)}">${esc(이름)}</b>${딱지}</span>`;
function 줄세기(el){ const r=document.createRange(); r.selectNodeContents(el); const 위=new Set(); for(const q of r.getClientRects()) if(q.width>0.5) 위.add(Math.round(q.top)); return 위.size; }
function 이름맞춤(root){ (root||document).querySelectorAll(".이름맞춤").forEach(el=>{
  el.style.fontSize=""; el.style.letterSpacing=""; let 줄=+el.dataset.줄||1; el.classList.toggle("두줄", 줄>1);
  if(!el.getClientRects().length) return;   // 안 보이는 것은 건너뜀
  const 바탕=parseFloat(getComputedStyle(el).fontSize)||15, 단계=이름글단계.filter(x=>x<=바탕+0.1);
  const 넘=()=> 줄>1 ? 줄세기(el)>줄 || el.scrollWidth>el.clientWidth+0.5 : el.scrollWidth>el.clientWidth+0.5;
  const 해봄=()=>{ el.style.letterSpacing=""; for(const fs of 단계){ el.style.fontSize=fs+"px"; if(!넘()) return true; }
    for(let n=2; n<=Math.round(-이름자간끝*100); n++){ el.style.letterSpacing=(-n/100)+"em"; if(!넘()) return true; } return false; };
  if(해봄() || 줄>1) return;
  줄=2; el.classList.add("두줄"); 해봄(); }); }
window.addEventListener("resize", ()=>{ const 폰=document.getElementById("폰"); if(폰) 이름맞춤(폰); });
try{ document.fonts?.addEventListener?.("loadingdone", ()=>{ const 폰=document.getElementById("폰"); if(폰) 이름맞춤(폰); }); }catch(e){}

/* ═══ 10-04 v18 C ②③ 종목 상자 — 종목마다 따로.''')
바꿈('''  체험막대(); 맞춤하기(폰); 시계그리기(); 애니(폰); 띠맞춤(폰); 저장();''',
     '''  체험막대(); 맞춤하기(폰); 이름맞춤(폰); 시계그리기(); 애니(폰); 띠맞춤(폰); 저장();   // v20 ① 이름맞춤''')

# ── ④ ⑤ 새 종목 시트 고르기 칸 ──
바꿈('''  const 수=g=>(세부부위.find(([x])=>x===g)?.[1]||[]).filter(k=>m[k]).length;
''', '')   # v20 ④ 묶음 칩 개수 — 쓰는 곳이 없어졌다
바꿈('''    <div class="새묶음">${칩줄("새묶음","",세부부위.map(([g])=>g),새.묶음,g=>수(g)?`${g} <b class="숫">${수(g)}</b>`:g)}</div>
    <div class="새역할줄">${칩줄("새역할","",["P","S","Y"],새.역할,r=>역할짧은[r])}</div>
    <div class="칩줄 새근육들">${묶[1].map(k=>`<button class="칩 근칩 ${m[k]?"역"+m[k]:""}" data-act="새근육" data-v="${k}" aria-pressed="${!!m[k]}">${esc(근이름(k))}${m[k]?`<small>${역할짧은[m[k]]}</small>`:""}</button>`).join("")}</div>''',
     '''    <div class="새묶음">${칩줄("새묶음","",세부부위.map(([g])=>g),새.묶음)}</div>
    <div class="새역할줄">${칩줄("새역할","",["P","Y"],새.역할,r=>역할이름[r])}</div>
    <div class="칩줄 새근육들">${묶[1].map(k=>{ const 막=새.역할==="Y"&&m[k]==="P"; return `<button class="칩 근칩 ${m[k]?"역"+m[k]:""}${막?" 막힘":""}" data-act="새근육" data-v="${k}" aria-pressed="${!!m[k]}"${막?' aria-disabled="true"':""}>${esc(근이름(k))}${m[k]?`<small>${역할짧은[m[k]]}</small>`:""}</button>`; }).join("")}</div>''')
# v20 ④ 개수 삭제 · ⑤ 역할 칩 둘 · 협응근 역할에서 주동근 부위 = 흐림(.막힘 · 누르면 토스트) — 설명은 아래 역할이름 위에

바꿈('''const 역할짧은 = {P:"주동", S:"보조", Y:"협응"}, 역순 = {P:3, S:2, Y:1};''',
     '''const 역할짧은 = {P:"주동", S:"보조", Y:"협응"}, 역순 = {P:3, S:2, Y:1};
/* 10-04 v20 ⑤ 새 종목 시트의 역할은 둘 — 주동근 P · 협응근 Y (홍겸 님 "주동근이랑 협응근으로만 나누고").
   사전 · 낱말 규칙 · 이미 있는 종목의 옛 보조(S)는 시트에 들일 때 협응(Y)으로 읽는다 → 저장값은 P · Y 뿐.
   (근육규칙의 S 0.5 는 그대로 — 시트에서 저장한 종목만 P 1.0 · Y 0.25). 칩 글은 이미 있는 역할이름(P 주동근 · Y 협응근) */
const 둘역할 = m => Object.fromEntries(Object.entries(m||{}).map(([k,r])=>[k, r==="P"?"P":"Y"]));''')
바꿈('''const 새몸단계 = {P:20, S:10, Y:5};''', '''const 새몸단계 = {P:20, Y:5};   // v20 ⑤ 두 단계(보조 없음)''')

# 시트에 근육을 들이는 세 곳 — 사전 · 직접 친 이름 · (편집은 아래 종목편집)
바꿈('''U.새.근육=사전근육(x); U.새.사전=x.이름; U.새.묶음=기본묶음(); }   // v19 D ③''',
     '''U.새.근육=둘역할(사전근육(x)); U.새.사전=x.이름; U.새.묶음=기본묶음(); }   // v20 ⑤ S → Y · v19 D ③''')
바꿈('''    if(!U.새.고름){ U.새.근육 = 있 ? 세부로(종목근육(종목키(있))) : 세부로(낱말근육(n)||{}); U.새.묶음=기본묶음(); } U.새.사전=null; }''',
     '''    if(!U.새.고름){ U.새.근육 = 둘역할(있 ? 세부로(종목근육(종목키(있))) : 세부로(낱말근육(n)||{})); U.새.묶음=기본묶음(); } U.새.사전=null; }   // v20 ⑤ S → Y''')
# ③ 편집 = 이름만 바꾼다(사전 · 낱말 규칙으로 근육 · 카테고리를 덮지 않는다)
바꿈('''function 새확인(){ const n=String(U.새종목||"").trim(); if(!n){ 토스트("이름을 넣어 주세요"); return false; }
''', '''function 새확인(){ const n=String(U.새종목||"").trim(); if(!n){ 토스트("이름을 넣어 주세요"); return false; }
  if(U.새.편집){ U.새종목=n; U.새.찾는중=false; return true; }   // v20 ③ 편집에서는 이름만 바꾼다(근육 · 카테고리 · 세팅은 그대로)
''')

# ③ 시트 머리 — 편집이면 '○○ 편집'(이름이 길면 이름만 … — 이름에만 자르기, U2-7)
바꿈('''  return `<div class="머리"><b>새 종목</b><button class="닫기" data-act="시트닫기">''',
     '''  return `<div class="머리">${새.편집?`<b class="편집제목"><span>${esc(새.편집이름)}</span> <i>편집</i></b>`:`<b>새 종목</b>`}<button class="닫기" data-act="시트닫기">''')

# ③ 고치기 저장
바꿈('''/* 이름 칸에 손가락이 가면 찾기가 켜진다 · Enter = [확인] */''',
'''/* ═══ 10-04 v20 ③ 이미 있는 종목 고치기 — 홍겸 님 "이미 만들어둔 종목에도 편집버튼 만들어서 세부내용 수정할수있게"
   종목 탭 펼친 상자의 [편집] → 새 종목 시트를 편집으로(U.새.편집 = 그 종목 열쇠). 이름 · 카테고리 · 운동 목표 부위(역할) · 기본 세팅이 채워진 채.
   [저장] = 그 종목을 고친다(새로 만들지 않는다). 기본 종목(처음부터 있던 · id 없는 종목)도 — 근육 역할은 S.종목표[].근육 에 남는다.
   이름을 바꾸면: id 있는 종목은 이름만(루틴 · 운동 줄 이름도). id 없는 종목은 새 id 를 주고 이름으로 묶여 있던
   기본 세팅 · 사진 · 루틴/운동 줄(플랜 줄 빼고)을 그 id 로 옮긴다. 지난 기록은 그때 이름 그대로 둔다.
   플랜이 걸린 종목(그 이름의 첫 종목 — 플랜은 거기에 붙는다)은 이름을 바꾸지 않는다 — 플랜 회차표가 종목 이름(플랜표)으로 이어져 있다 [Claude 판단 · 홍겸 님 확인 전] */
const 새종목id = ()=> "종"+Date.now().toString(36)+Math.random().toString(36).slice(2,6);
function 종목줄마다(f){ for(const r of S.루틴들) for(const e of r.종목) f(e); for(const e of S.세션?.종목||[]) f(e); }
function 종목고침(n){ const t=종목표찾기(U.새.편집), 칸=U.새종목칸; if(!t){ U.새종목=""; U.새=null; U.시트=null; return true; }
  const 옛=t.이름;
  if(n!==옛 && S.종목표.find(u=>u.이름===옛)===t && S.플랜들.some(p=>p.종목===옛)){ 토스트("플랜이 있는 종목은 이름을 바꿀 수 없습니다"); return false; }
  if(n!==옛 && !t.id){ const id=새종목id(); t.id=id;
    if(S.종목설정?.[옛]){ S.종목설정[id]=S.종목설정[옛]; delete S.종목설정[옛]; }
    if(사진함[옛]){ 사진함[id]=사진함[옛]; delete 사진함[옛]; 사진저장(); }
    종목줄마다(e=>{ if(!e.종id && !e.플랜id && e.이름===옛) e.종id=id; }); }
  t.이름=n; t.칸=칸; t.근육={...U.새.근육};
  if(t.id) 종목줄마다(e=>{ if(e.종id===t.id) e.이름=n; });
  S.종목설정 ||= {}; S.종목설정[종목키(t)]={세트:U.새.세트.map(x=>({w:x.w, r:x.r, 휴:x.휴}))};
  발자취(`종목 고침 · ${옛===n?n:옛+" → "+n}`);
  U.종목펼침=종목키(t); U.새종목=""; U.새=null; U.시트=null;
  if(U.종목칸고름!=="전체"&&U.종목칸고름!==칸) U.종목칸고름=칸;
  queueMicrotask(()=>토스트(`저장했습니다 · ${n}`)); return true; }   // 다시 그린 뒤에 띄운다
/* 이름 칸에 손가락이 가면 찾기가 켜진다 · Enter = [확인] */''')

# 행동
바꿈('''    case "종목펼침": U.종목펼침 = U.종목펼침===d.v ? null : d.v; break;''',
     '''    case "종목펼침": U.종목펼침 = U.종목펼침===d.v ? null : d.v; break;
    case "종목편집": { const t=종목표찾기(d.v); if(!t) break; const 키=종목키(t);   // 10-04 v20 ③ 새 종목 시트를 편집으로 — 값을 채워서
      U.새종목=t.이름; U.새종목칸=S.카테고리.includes(t.칸)?t.칸:null;
      U.새={...새초기(), 고름:true, 칸직접:true, 편집:키, 편집이름:t.이름, 근육:둘역할(세부로(종목근육(키))), 세트:종목기본세트(키).map(x=>({w:x.w, r:x.r, 휴:x.휴}))};
      U.새.묶음=기본묶음(); 새속자리=0; U.시트={종류:"새종목", 돌아감:null}; break; }''')
바꿈('''    case "새사전고름": { const x=종목사전.find(y=>y.이름===d.v);   // v19 D ② 이미 있는 이름도 고를 수 있다''',
     '''    case "새사전고름": { if(U.새?.편집){ U.새종목=d.v; U.새.찾는중=false; break; }   // v20 ③ 편집 = 이름만
      const x=종목사전.find(y=>y.이름===d.v);   // v19 D ② 이미 있는 이름도 고를 수 있다''')
바꿈('''    case "새근육": { const m=U.새.근육; if(m[d.v]===U.새.역할) delete m[d.v]; else m[d.v]=U.새.역할;''',
     '''    case "새근육": { const m=U.새.근육;
      if(U.새.역할==="Y" && m[d.v]==="P"){ 토스트("이미 주동근으로 선택되어있습니다."); 다시=false; break; }   // v20 ⑤ 다시 그리면 토스트가 지워진다 · 주동근 역할에서 협응근 부위를 누르면 주동으로 옮김(그대로)
      if(m[d.v]===U.새.역할) delete m[d.v]; else m[d.v]=U.새.역할;''')
바꿈('''      if(!Object.values(U.새.근육).includes("P")){ 토스트("주동근을 하나 이상 골라 주세요"); 다시=false; break; }
      const 칸=U.새종목칸, id="종"+Date.now().toString(36)+Math.random().toString(36).slice(2,6);''',
     '''      if(!Object.values(U.새.근육).includes("P")){ 토스트("주동근을 하나 이상 골라 주세요"); 다시=false; break; }
      if(U.새.편집){ if(!종목고침(n)) 다시=false; break; }   // v20 ③ 편집 = 그 종목을 고친다
      const 칸=U.새종목칸, id=새종목id();''')

css = '''
/* ═══ 10-04 v20 — 종목 탭 상자 · 새 종목 시트 ═══ */
/* ① 이름 맞춤 — 바탕 15(목록 이름). 한 줄 = 줄바꿈 없음 · '…' 없음(이름맞춤이 크기 · 자간을 줄인다) / 두 줄 = 낱말 단위로 줄바꿈 */
.종목칸 .종목줄이름{min-width:0}
.이름플랜>b.이름맞춤{min-width:0;overflow:hidden;text-overflow:clip;white-space:nowrap}
.이름플랜>b.이름맞춤.두줄{white-space:normal;word-break:keep-all;overflow-wrap:anywhere}
.종목칸 b.이름맞춤{font-size:15px}
/* ② 접힌 상자 = 이름 · ▾ 만 — 같은 줄 상자가 두 줄 이름 때문에 커지면 이름을 가운데로 */
.종목칸:not(.펼){display:flex;flex-direction:column;justify-content:center}
/* ③ [편집] — 펼친 상자 머리 오른쪽(작은 버튼 32) */
.종목편집{flex:none}
.시트 .머리 b.편집제목{display:flex;gap:4px;min-width:0;white-space:nowrap}
.편집제목>span{min-width:0;overflow:hidden;text-overflow:ellipsis}
.편집제목>i{flex:none;font-style:normal}
/* ⑤ 협응근 역할일 때 이미 주동근인 부위 — 흐리게(.칩:disabled 와 같은 .35) · 눌러도 안 바뀌고 토스트 */
.근칩.막힘{opacity:.35;cursor:default}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]
pathlib.Path(OUT).write_text(s, encoding='utf-8'); print("v20 →", OUT)
