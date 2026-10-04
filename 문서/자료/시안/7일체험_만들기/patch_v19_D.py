"""v19 D (10-04 홍겸 님) — 새 종목 시트 · 같은 이름 종목 · 번호 딱지
① 새 종목 시트 위끝 = 폰 높이 20% 지점 고정(넣기시트높이 와 같은 잼). 머리 · 이름 칸 · [저장]은 그대로, 가운데(.새속)만 스크롤.
   다시 그려도 보던 자리 그대로(검색 결과 · 부위 칩 · 세트 줄이 바뀌어도 띠 자리 그대로)
② 같은 이름 종목 허용 — 새로 만든 종목마다 고유 id(S.종목표[].id). 열쇠 = id(없으면 이름 — 옛 종목은 그대로 이름).
   종목설정 · 사진 · 펼침 · 넣기 목록 · 루틴/운동 줄(e.종id) · 기록 · 근육(종목근육) 이 열쇠로 찾는다.
   같은 이름이 둘 이상이면 이름 오른쪽 위 5px 겹쳐 번호 딱지(만든 순서 1, 2, …) — [플랜] 딱지와 같은 꼴
③ 카테고리 — 사전에서 고르면 자동. 사전에 없는 이름이면 아무것도 안 고른 흐린 칩줄. 고르기 전에 [저장] → 토스트 '반드시 카테고리를 지정해야 합니다'
④ '세부 부위' → '운동 목표 부위'. 근육 지도 앞 · 뒤 2장(몸조각 · 단계색 재사용 — 주동 20 · 보조 10 · 협응 5단계 색). 그림 부위를 눌러도 같은 토글
⑤ 시트 안 '누르면 …' 글 삭제
⑥ 기본 세팅 세트 줄([번호][kg][회][휴식][휴지통]) + [+ 세트] — 종목 탭 기본 세팅과 같은 부품. 저장하면 S.종목설정[id]"""
import sys, pathlib
IN, OUT = sys.argv[1], sys.argv[2]
s = pathlib.Path(IN).read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:70]!r}: {c}번")
    s = s.replace(old, new)

# ── ② 종목 열쇠 · 근육 ─────────────────────────────────────────────
바꿈('''function 종목근육(이름){ const 표0=S.종목표.find(x=>x.이름===이름); if(표0&&표0.근육&&Object.keys(표0.근육).length) return 표0.근육;   // 10-04 v18 C ⑤ 새 종목 시트에서 고른 근육
  for(const [k,m] of 근육규칙.규칙) if(k.some(w=>이름.includes(w))) return m;
  const 표=S.종목표.find(x=>x.이름===이름); return (표&&근육규칙.부위[표.칸])||{}; }''',
'''/* 10-04 v19 D ② 같은 이름 종목 — 새로 만든 종목은 고유 id. 열쇠 = id(없으면 이름 · 옛 종목은 이름 그대로).
   종목표찾기(열쇠) = id 로 먼저, 없으면 이름으로(같은 이름이면 먼저 만든 것) */
function 종목표찾기(k){ return S.종목표.find(x=>x.id&&x.id===k) || S.종목표.find(x=>x.이름===k); }
const 종목키 = t => t.id||t.이름, 줄키 = e => e.종id||e.이름;
function 종목근육(이름){ const 표0=종목표찾기(이름); if(표0&&표0.근육&&Object.keys(표0.근육).length) return 표0.근육;   // 10-04 v18 C ⑤ 새 종목 시트에서 고른 근육
  if(표0) 이름=표0.이름;   // v19 D ② id 로 왔으면 낱말 규칙은 이름으로
  for(const [k,m] of 근육규칙.규칙) if(k.some(w=>이름.includes(w))) return m;
  const 표=표0; return (표&&근육규칙.부위[표.칸])||{}; }''')
바꿈('''    for(const [id,역] of Object.entries(종목근육(e.이름))){ const 비=근육규칙.역할[역]||0;''',
     '''    for(const [id,역] of Object.entries(종목근육(줄키(e)))){ const 비=근육규칙.역할[역]||0;''')   # v19 D ② 같은 이름 둘의 근육이 섞이지 않게
바꿈('''function 주동부위(이름){ const 부=new Set(), 표=S.종목표.find(x=>x.이름===이름);''',
     '''function 주동부위(이름){ const 부=new Set(), 표=종목표찾기(이름);''')
바꿈('''  const 봄=(e,세트들,날)=>{ const 부=캐[e.이름]||(캐[e.이름]=주동부위(e.이름)); if(!부.size) return;''',
     '''  const 봄=(e,세트들,날)=>{ const 부=캐[줄키(e)]||(캐[줄키(e)]=주동부위(줄키(e))); if(!부.size) return;''')

# 루틴 → 운동 → 기록 으로 종id 를 넘긴다
바꿈('''    return {이름:e.이름, 플랜id:e.플랜id||null, 회:계?.회??null, 측정일:!!계?.측정일,''',
     '''    return {이름:e.이름, ...(e.종id?{종id:e.종id}:{}), 플랜id:e.플랜id||null, 회:계?.회??null, 측정일:!!계?.측정일,''')
바꿈('''  종목:ss.종목.map(e=>({이름:e.이름, 플랜id:e.플랜id, 회:e.회,''',
     '''  종목:ss.종목.map(e=>({이름:e.이름, 종id:e.종id, 플랜id:e.플랜id, 회:e.회,''')
바꿈('''const e=ss.종목.find(x=>x.이름===row.이름&&!x.플랜id); if(!e) return;''',
     '''const e=ss.종목.find(x=>줄키(x)===줄키(row)&&!x.플랜id); if(!e) return;''')

# 운동 그림 칸 — 확대 · 사진도 열쇠로
바꿈('''  const 확대=()=>{ const 상=확대상자(e.이름);''', '''  const 확대=()=>{ const 상=확대상자(줄키(e));''')
바꿈('''  const 사진=slot=>{ const l=사진목록(e.이름);''', '''  const 사진=slot=>{ const l=사진목록(줄키(e));''')

# ── 번호 딱지 helpers (플랜딱지 옆) ──
바꿈('''const 플랜딱지 = `<span class="플랜표">플랜</span>`;''',
'''const 플랜딱지 = `<span class="플랜표">플랜</span>`;
/* 10-04 v19 D ② 같은 이름이 둘 이상이면 만든 순서 번호(S.종목표 순서 = 만든 순서). 하나뿐이면 0(딱지 없음) */
function 같은이름번호(키, 이름){ const l=S.종목표.filter(t=>t.이름===이름); if(l.length<2) return 0; const i=l.findIndex(t=>종목키(t)===키); return i<0?0:i+1; }
const 번호딱지 = n => n ? `<span class="플랜표 번호표">${n}</span>` : "";
/* 이름 + 번호 딱지 — 번호가 없으면 예전 그대로 <b>이름</b> */
function 번호이름(이름, 키){ const n=같은이름번호(키??이름, 이름); return n ? `<span class="이름플랜 번호이름"><b>${esc(이름)}</b>${번호딱지(n)}</span>` : `<b>${esc(이름)}</b>`; }''')

# 루틴 상자 이름 줄(접힘 줄)
바꿈('''<div class="채움 루접줄" data-act="루펼침" data-i="${i}" role="button" tabindex="0" aria-expanded="${펼}"><b>${esc(e.이름)}</b>''',
     '''<div class="채움 루접줄" data-act="루펼침" data-i="${i}" role="button" tabindex="0" aria-expanded="${펼}">${번호이름(e.이름, 줄키(e))}''')

# 운동 화면 머리 이름
바꿈('''    : `<b class="운이름">${esc(e.이름)}</b>`;''',
     '''    : (운번=같은이름번호(줄키(e), e.이름)) ? `<b class="운이름">${낱.length?esc(낱.join(" "))+" ":""}<span class="운끝말">${esc(끝말)}<i class="운번호표">${운번}</i></span></b>`   // v19 D ② 같은 이름 번호
    : `<b class="운이름">${esc(e.이름)}</b>`;''')
바꿈('''  const 낱=e.이름.split(" "), 끝말=낱.pop();''', '''  const 낱=e.이름.split(" "), 끝말=낱.pop(); let 운번=0;''')

# 운동 화면 아래 종목 칸 줄 — 칸 오른쪽 위 구석에 번호
바꿈('''aria-label="${esc(x.이름)} ${k}/${m}세트"><div class="ㅇ">${esc(x.이름)}</div>''',
     '''aria-label="${esc(x.이름)} ${k}/${m}세트"><div class="ㅇ">${esc(x.이름)}</div>${(n=>n?`<i class="운칸번호" aria-hidden="true">${n}</i>`:"")(같은이름번호(줄키(x),x.이름))}''')

# ── 넣기 목록 — 열쇠 · 번호 · 플랜은 그 이름의 첫 종목에만 ──
바꿈('''  for(const x of S.종목표){ if(칸!=="전체" && x.칸!==칸) continue; const 플=S.플랜들.filter(p=>p.종목===x.이름);
    if(플.length) 플.forEach(p=>줄.push({이름:p.이름, p, 종목:x.이름})); else 줄.push({이름:x.이름, 종목:x.이름}); }''',
'''  for(const x of S.종목표){ if(칸!=="전체" && x.칸!==칸) continue; const 첫=S.종목표.find(t=>t.이름===x.이름)===x, 플=첫?S.플랜들.filter(p=>p.종목===x.이름):[];   // v19 D ② 같은 이름이면 플랜은 먼저 만든 것에만
    if(플.length) 플.forEach(p=>줄.push({이름:p.이름, p, 종목:x.이름})); else 줄.push({이름:x.이름, 키:종목키(x), 종목:x.이름}); }''')
바꿈('''  return 줄.map((z,n)=>{ const 수=z.p?r.종목.filter(e=>e.플랜id===z.p.id).length:r.종목.filter(e=>e.이름===z.이름&&!e.플랜id).length;''',
     '''  return 줄.map((z,n)=>{ const 수=z.p?r.종목.filter(e=>e.플랜id===z.p.id).length:r.종목.filter(e=>줄키(e)===z.키&&!e.플랜id).length;''')
바꿈('''data-v="${z.p?z.p.id:esc(z.이름)}" aria-label="${esc(z.이름)}${z.p?" 플랜":""} · ${수}개 들어 있음 · ${수?"누르면 하나 빼기":"누르면 넣기"}, 꾹 누르면 하나 더"><span class="넣기번 숫">${n+1}</span><span class="이름플랜"><b>${esc(z.이름)}</b>${z.p?플랜딱지:""}</span>''',
     '''data-v="${z.p?z.p.id:esc(z.키)}" aria-label="${esc(z.이름)}${z.p?" 플랜":""} · ${수}개 들어 있음 · ${수?"누르면 하나 빼기":"누르면 넣기"}, 꾹 누르면 하나 더"><span class="넣기번 숫">${n+1}</span><span class="이름플랜"><b>${esc(z.이름)}</b>${z.p?(z.이름===z.종목?번호딱지(같은이름번호(z.종목,z.종목)):"")+플랜딱지:번호딱지(같은이름번호(z.키,z.이름))}</span>''')
# 종목넣기 — 열쇠로
바꿈('''      const 맞=e=>e.이름===d.v&&!e.플랜id;
      if(!d.더 && r.종목.some(맞)){ 넣은것뺌(r, 맞); }   // v19 C ③ 들어간 칸 누름 = 하나 빼기 · 안 들어간 칸 누름 · 꾹 = 하나 넣기
      else { const 세=종목기본세트(d.v); r.종목.push({이름:d.v, 세트:운?운세트로(세):세});''',
     '''      const 맞=e=>줄키(e)===d.v&&!e.플랜id;   // v19 D ② d.v = 열쇠
      if(!d.더 && r.종목.some(맞)){ 넣은것뺌(r, 맞); }   // v19 C ③ 들어간 칸 누름 = 하나 빼기 · 안 들어간 칸 누름 · 꾹 = 하나 넣기
      else { const 세=종목기본세트(d.v), t=종목표찾기(d.v); r.종목.push({이름:t?t.이름:d.v, ...(t?.id?{종id:t.id}:{}), 세트:운?운세트로(세):세});''')

# ── 종목 탭 상자 — 열쇠 · 번호 ──
바꿈('''function 종목칸(x){ const l=사진목록(x.이름), 펼=U.종목펼침===x.이름, 플=S.플랜들.map((p,i)=>[p,i]).filter(([p])=>p.종목===x.이름), v=esc(x.이름);''',
     '''function 종목칸(x){ const 키=종목키(x), l=사진목록(키), 펼=U.종목펼침===키, 첫=!x.id||S.종목표.find(t=>t.이름===x.이름)===x,   // v19 D ② 열쇠 = id(없으면 이름)
    플=첫?S.플랜들.map((p,i)=>[p,i]).filter(([p])=>p.종목===x.이름):[], v=esc(키), 이=esc(x.이름), 번=같은이름번호(키,x.이름);''')
바꿈('''<label class="작은사진 사진넣칸 ${l.length?"":"빈"}" aria-label="${v} 사진 넣기">''',
     '''<label class="작은사진 사진넣칸 ${l.length?"":"빈"}" aria-label="${이} 사진 넣기">''')
바꿈('''<span class="채움 한줄 종목줄이름">${플.length?`<span class="이름플랜"><b>${v}</b>${플랜딱지}</span>`:`<b>${v}</b>`}</span>${펼&&곁?`<span class="아주작 옅음">${곁}</span>`:""}<span class="아주작 옅음 접힘표 ${펼?"펼":""}">▾</span></span>${근육두줄(종목근육(x.이름))}</button></div>''',
     '''<span class="채움 한줄 종목줄이름">${플.length||번?`<span class="이름플랜"><b>${이}</b>${번호딱지(번)}${플.length?플랜딱지:""}</span>`:`<b>${이}</b>`}</span>${펼&&곁?`<span class="아주작 옅음">${곁}</span>`:""}<span class="아주작 옅음 접힘표 ${펼?"펼":""}">▾</span></span>${근육두줄(종목근육(키))}</button></div>''')
바꿈('''${종목세팅(x.이름, 플.length>0)}''', '''${종목세팅(키, 플.length>0)}''')
바꿈('''aria-label="사진 ${j+1} 지우기"><img src="${src}" alt="">${U.확인===`사진${x.이름}${j}`?''',
     '''aria-label="사진 ${j+1} 지우기"><img src="${src}" alt="">${U.확인===`사진${키}${j}`?''')

# 검색 결과 — 직접 만든 같은 이름은 한 줄만
바꿈('''  for(const t of S.종목표) if(!종목사전.some(x=>x.이름===t.이름)){''',
     '''  for(const t of S.종목표) if(!종목사전.some(x=>x.이름===t.이름) && S.종목표.find(u=>u.이름===t.이름)===t){''')

# ── 새 종목 시트 ───────────────────────────────────────────────────
바꿈('''const 새초기 = ()=>({찾는중:false, 고름:false, 사전:null, 근육:{}, 묶음:"가슴", 역할:"P"});''',
     '''const 새초기 = ()=>({찾는중:false, 고름:false, 사전:null, 근육:{}, 묶음:"가슴", 역할:"P", 칸직접:false, 세트:세트들(S.설정.기본세트,20,10,S.설정.기본휴식)});   // v19 D ③ 칸직접 · ⑥ 세트''')
# ③ 사전에 없는 이름 = 카테고리 비움(직접 고른 게 있으면 그대로 · 같은 이름의 직접 만든 종목이 있으면 그 칸 · 근육)
바꿈('''  else { U.새종목=n; if(!U.새.고름){ U.새.근육=세부로(낱말근육(n)||{}); U.새.사전=null; U.새.묶음=기본묶음(); } }
  U.새.찾는중=false; U.새.고름=true; return true; }''',
     '''  else { U.새종목=n; const 있=S.종목표.find(t=>t.이름===n);   // v19 D ③ 사전에 없으면 카테고리 정보 없음 → 비움
    if(!U.새.칸직접) U.새종목칸 = 있&&S.카테고리.includes(있.칸) ? 있.칸 : null;
    if(!U.새.고름){ U.새.근육 = 있 ? 세부로(종목근육(종목키(있))) : 세부로(낱말근육(n)||{}); U.새.묶음=기본묶음(); } U.새.사전=null; }
  if(!U.새.고름) 새속자리=0; U.새.찾는중=false; U.새.고름=true; return true; }''')
바꿈('''function 사전적용(x){ U.새종목=x.이름; if(S.카테고리.includes(x.칸)) U.새종목칸=x.칸; U.새.근육=사전근육(x); U.새.사전=x.이름; U.새.묶음=기본묶음(); }''',
     '''function 사전적용(x){ U.새종목=x.이름; U.새종목칸 = S.카테고리.includes(x.칸) ? x.칸 : (U.새.칸직접 ? U.새종목칸 : null); U.새.근육=사전근육(x); U.새.사전=x.이름; U.새.묶음=기본묶음(); }   // v19 D ③ 사전 칸이 카테고리에 없으면 비움''')
# ⑤ '누르면' 글
바꿈('''  const l=종목찾기(q); if(!l.length) return `<div class="아주작 옅음 한줄">사전에 없습니다 · [확인]을 누르면 이 이름으로 만듭니다</div>`;''',
     '''  const l=종목찾기(q); if(!l.length) return `<div class="아주작 옅음 한줄">사전에 없습니다</div>`;   // v19 D ⑤''')
# ③④⑤⑥ 고르기 칸
바꿈('''  return `<div class="이름표 새이름표">카테고리</div>${칩줄("새종목칸","",S.카테고리,U.새종목칸)}
    <div class="이름표 새이름표">세부 부위</div>
    <div class="새묶음">${칩줄("새묶음","",세부부위.map(([g])=>g),새.묶음,g=>수(g)?`${g} <b class="숫">${수(g)}</b>`:g)}</div>
    <div class="새역할줄"><span class="아주작 옅음">누르면</span>${칩줄("새역할","",["P","S","Y"],새.역할,r=>역할짧은[r])}</div>
    <div class="칩줄 새근육들">${묶[1].map(k=>`<button class="칩 근칩 ${m[k]?"역"+m[k]:""}" data-act="새근육" data-v="${k}" aria-pressed="${!!m[k]}">${esc(근이름(k))}${m[k]?`<small>${역할짧은[m[k]]}</small>`:""}</button>`).join("")}</div>
    <div class="새요약">${근육두줄(m)}</div>
    <button class="버튼 주 넓 새저장" data-act="새저장">저장</button>`; }''',
'''  return `<div class="이름표 새이름표">카테고리</div><div class="새칸줄${U.새종목칸?"":" 비활성"}">${칩줄("새종목칸","",S.카테고리,U.새종목칸)}</div>
    <div class="이름표 새이름표">운동 목표 부위</div>
    <div class="새지도"><div class="새몸칸">${새몸그림(m,"f")}</div><div class="새몸칸">${새몸그림(m,"b")}</div><div class="새요약">${근육두줄(m)}</div></div>
    <div class="새묶음">${칩줄("새묶음","",세부부위.map(([g])=>g),새.묶음,g=>수(g)?`${g} <b class="숫">${수(g)}</b>`:g)}</div>
    <div class="새역할줄">${칩줄("새역할","",["P","S","Y"],새.역할,r=>역할짧은[r])}</div>
    <div class="칩줄 새근육들">${묶[1].map(k=>`<button class="칩 근칩 ${m[k]?"역"+m[k]:""}" data-act="새근육" data-v="${k}" aria-pressed="${!!m[k]}">${esc(근이름(k))}${m[k]?`<small>${역할짧은[m[k]]}</small>`:""}</button>`).join("")}</div>
    <div class="이름표 새이름표">기본 세팅</div>${새세트줄()}`; }
/* 10-04 v19 D ④ 운동 목표 부위 그림 — 몸조각(앞 'f' · 뒤 'b')을 그대로, 색은 피로 지도의 단계색으로 역할마다 한 단계씩
   주동 20단계(진하게) · 보조 10 · 협응 5(옅게). 세부 부위 조각은 눌러도 칩과 같은 토글(data-act="새근육") */
const 새몸단계 = {P:20, S:10, Y:5};
function 새몸그림(m, 면){ const 단={};
  for(const [k,r] of Object.entries(m||{})) for(const l of 잎(k)) 단[l]=Math.max(단[l]||0, 새몸단계[r]||0);
  let h=`<svg class="몸 새몸" viewBox="${면==="b"?"266 4 148 442":"26 4 148 442"}" preserveAspectRatio="xMidYMid meet" aria-label="근육 지도 ${면==="b"?"뒤":"앞"}">`;
  몸조각.forEach(([v,k,mm,c,d],i)=>{ if(v!==면) return; const tr=v==="b"?' transform="translate(240 0)"':'';
    if(k==="b") h+=`<path class="몸바탕" d="${d}"${tr}/>`;
    else if(k==="x") h+=`<path class="몸결" d="${d}"${tr}/>`;
    else { const 값=Math.max(0,...조각잎[i].map(l=>단[l]||0)), 색=단계색(값), 누=세부키.has(mm);
      h+=`<path class="몸근${누?" 새몸근":""}" d="${d}"${tr} data-anim-fill="새몸-${i}" style="fill:${색||"var(--근육)"}"${누?` data-act="새근육" data-v="${mm}"`:""}><title>${esc(근이름(mm))}</title></path>`; } });
  return h+"</svg>"; }
/* 10-04 v19 D ⑥ 기본 세팅 세트 줄 — 종목 탭 기본 세팅(종목세팅)과 같은 부품 · 값은 U.새.세트(저장하면 S.종목설정[id]) */
function 새세트줄(){ const 세=U.새.세트, 하나=세.length<=1;
  const 칸=(f,k,글,입,말)=>`<div class="값칸"><button data-act="새세트값" data-k="${k}" data-f="${f}" data-d="-1" aria-label="${k+1}세트 ${말} 빼기">−</button>${입?`<input data-in="새세트" data-k="${k}" data-f="${f}" inputmode="decimal" aria-label="${k+1}세트 ${말}" value="${글}">`:`<span class="숫">${글}</span>`}<button data-act="새세트값" data-k="${k}" data-f="${f}" data-d="1" aria-label="${k+1}세트 ${말} 더하기">＋</button></div>`;
  return `<div class="종설 새세팅"><div class="종세트머리 아주작 옅음"><span>세트</span><span>무게 kg</span><span>횟수</span><span>휴식</span><span></span></div>
    ${세.map((x,k)=>`<div class="종세트"><span class="k 숫">${k+1}</span>${칸("w",k,kg(x.w),true,"무게")}${칸("r",k,x.r,true,"횟수")}${칸("휴",k,분초(x.휴),false,"휴식")}<button class="종지움" data-act="새세트지움" data-k="${k}" aria-label="${k+1}세트 지우기"${하나?" disabled":""}>${아이콘.휴지통}</button></div>`).join("")}
    <button class="버튼 낮 넓" data-act="새세트더">+ 세트</button></div>`; }''')
# ① 시트 몸 — 머리 · 이름 칸 · [가운데 스크롤] · 저장
바꿈('''    <div class="새찾기결과">${새찾기결과()}</div>
    ${새.고름?새부위고르기():""}`; }''',
     '''    <div class="새속"><div class="새찾기결과">${새찾기결과()}</div>
    ${새.고름?새부위고르기():""}</div>
    ${새.고름?`<button class="버튼 주 넓 새저장" data-act="새저장">저장</button>`:""}`; }   // v19 D ① 가운데(.새속)만 스크롤 · 저장은 아래 고정
/* 10-04 v19 D ① 새 종목 시트 위끝 = 폰 높이의 20% 지점 — 넣기시트높이() 와 같은 잼(가림 아래끝 − 폰 위 − 폰 높이 × 0.2).
   다시 그려도 .새속 의 보던 자리 그대로(시트를 새로 열거나 이름을 정하면 맨 위부터) */
let 새속자리=0;
document.addEventListener("scroll", e=>{ if(e.target?.classList?.contains("새속")) 새속자리=e.target.scrollTop; }, true);
function 새시트높이(){ const 폰=document.getElementById("폰"), 시=폰?.querySelector(".시트:has(>.새속)"), 가=시?.parentElement; if(!시||!가) return;
  const p=폰.getBoundingClientRect(), g=가.getBoundingClientRect(); 시.style.height=Math.max(0, g.bottom-(p.top+p.height*0.2))+"px";
  const 속=시.querySelector(".새속"); if(속&&새속자리&&속.scrollTop!==새속자리) 속.scrollTop=새속자리; }
{ const 폰=document.getElementById("폰"); if(폰) new MutationObserver(새시트높이).observe(폰,{childList:true}); }
window.addEventListener("resize", 새시트높이);''')

# 행동
바꿈('''    case "새종목칸": U.새종목칸=d.v; if(U.새&&칸묶음[d.v]) U.새.묶음=칸묶음[d.v]; break;''',
     '''    case "새종목칸": U.새종목칸=d.v; if(U.새){ U.새.칸직접=true; if(칸묶음[d.v]) U.새.묶음=칸묶음[d.v]; } break;''')
바꿈('''U.새종목=""; U.새종목칸=S.카테고리[0]; U.새=새초기(); U.시트={종류:"새종목", 돌아감:돌}; break; }''',
     '''U.새종목=""; U.새종목칸=null; U.새=새초기(); 새속자리=0; U.시트={종류:"새종목", 돌아감:돌}; break; }   // v19 D ③ 카테고리는 비워 두고 시작''')
바꿈('''    case "새사전고름": { if(S.종목표.some(t=>t.이름===d.v)){ 토스트("이미 있습니다"); 다시=false; break; }
      const x=종목사전.find(y=>y.이름===d.v); if(x) 사전적용(x); else U.새종목=d.v; U.새.찾는중=false; U.새.고름=true; break; }''',
     '''    case "새사전고름": { const x=종목사전.find(y=>y.이름===d.v);   // v19 D ② 이미 있는 이름도 고를 수 있다
      if(x) 사전적용(x); else { U.새종목=d.v; U.새.고름=false; 새확인(); } U.새.찾는중=false; U.새.고름=true; 새속자리=0; break; }''')
바꿈('''    case "새근육": { const m=U.새.근육; if(m[d.v]===U.새.역할) delete m[d.v]; else m[d.v]=U.새.역할; break; }''',
     '''    case "새근육": { const m=U.새.근육; if(m[d.v]===U.새.역할) delete m[d.v]; else m[d.v]=U.새.역할;
      const g=세부부위.find(([,l])=>l.includes(d.v))?.[0]; if(g) U.새.묶음=g; break; }   // v19 D ④ 그림에서 눌러도 그 묶음으로
    /* v19 D ⑥ 새 종목 기본 세팅 — 종세트값 · 종세트지움 · 종세트더 와 같은 규칙, 값은 U.새.세트 */
    case "새세트값": { const x=U.새?.세트[+d.k], dd=+d.d; if(!x) break;
      if(d.f==="w") x.w=Math.max(0,Math.round((x.w+dd*S.설정.무게폭)*10)/10);
      else if(d.f==="r") x.r=Math.max(1,x.r+dd);
      else x.휴=Math.max(휴식최소,Math.min(휴식최대,Math.round((x.휴+dd*휴식폭)/휴식폭)*휴식폭));
      break; }
    case "새세트지움": if(U.새&&U.새.세트.length>1) U.새.세트.splice(+d.k,1); break;
    case "새세트더": { if(!U.새) break; const l=U.새.세트; if(l.length>=종목세트최대){ 토스트(`${종목세트최대}세트까지`); 다시=false; break; } l.push({...l[l.length-1]}); break; }''')
바꿈('''      if(!n){ 토스트("이름을 넣어 주세요"); 다시=false; break; } if(S.종목표.some(x=>x.이름===n)){ 토스트("이미 있습니다"); 다시=false; break; }
      if(!Object.values(U.새.근육).includes("P")){ 토스트("주동근을 하나 이상 골라 주세요"); 다시=false; break; }
      const 칸=U.새종목칸; S.종목표.push({이름:n, 칸, 근육:{...U.새.근육}}); 발자취(`종목 추가 · ${n}`);''',
     '''      if(!n){ 토스트("이름을 넣어 주세요"); 다시=false; break; }   // v19 D ② 같은 이름도 저장한다(고유 id)
      if(!U.새종목칸||!S.카테고리.includes(U.새종목칸)){ 토스트("반드시 카테고리를 지정해야 합니다"); 다시=false; break; }   // v19 D ③ 다시 그리지 않아 토스트가 남는다
      if(!Object.values(U.새.근육).includes("P")){ 토스트("주동근을 하나 이상 골라 주세요"); 다시=false; break; }
      const 칸=U.새종목칸, id="종"+Date.now().toString(36)+Math.random().toString(36).slice(2,6);
      S.종목표.push({id, 이름:n, 칸, 근육:{...U.새.근육}}); S.종목설정 ||= {}; S.종목설정[id]={세트:U.새.세트.map(x=>({w:x.w, r:x.r, 휴:x.휴}))}; 발자취(`종목 추가 · ${n}`);''')

# 입력
바꿈('''  else if(w==="종세트"){ const x=종목설정잡기(d.v).세트[+d.k]; if(x){ if(d.f==="w") x.w=Math.max(0,숫(v)); else x.r=Math.max(1,Math.round(숫(v))); } }   // 10-04 v18 C ④''',
     '''  else if(w==="종세트"){ const x=종목설정잡기(d.v).세트[+d.k]; if(x){ if(d.f==="w") x.w=Math.max(0,숫(v)); else x.r=Math.max(1,Math.round(숫(v))); } }   // 10-04 v18 C ④
  else if(w==="새세트"){ const x=U.새?.세트[+d.k]; if(x){ if(d.f==="w") x.w=Math.max(0,숫(v)); else x.r=Math.max(1,Math.round(숫(v))); } }   // v19 D ⑥''')
바꿈('''  if(w==="루틴이름"||w==="루값"||w==="몸"||w==="종세트") 그리기(); });''',
     '''  if(w==="루틴이름"||w==="루값"||w==="몸"||w==="종세트"||w==="새세트") 그리기(); });''')

css = '''
/* ═══ 10-04 v19 D — 새 종목 시트 · 같은 이름 번호 ═══ */
/* ① 높이 고정 — 위끝 = 폰 높이 20% 지점(새시트높이() 가 px 로 · 80% 는 그 전 잠깐). 머리 · 이름 칸 · 저장은 그대로, .새속 만 스크롤 */
.시트:has(>.새속){height:80%;max-height:none;overflow:hidden}
.시트:has(>.새속)>*{flex:none}
.시트>.새속{flex:1 1 auto;min-height:0;overflow-y:auto;display:flex;flex-direction:column;gap:8px;margin:0 -14px;padding:0 14px 8px}
.새속>*{flex:none}
/* ② 번호 딱지 — [플랜] 딱지와 같은 꼴(강조 바탕 · 강조글 · 11 Bold · 둥글기 8), 이름 오른쪽 위 5px 겹침 */
.플랜표.번호표{top:-5px;margin-left:-5px;min-width:16px;padding:0 4px;text-align:center;font-variant-numeric:tabular-nums}
.번호표+.플랜표{margin-left:4px}
.루접줄 .이름플랜{flex:0 1 auto}
.루접줄 .이름플랜>b{font-size:15px;font-weight:700}
.운머리 .운번호표{display:inline-block;position:relative;vertical-align:text-top;top:-5px;margin-left:-5px;font-style:normal;
  min-width:16px;height:16px;line-height:14px;padding:0 4px;box-sizing:border-box;border:1px solid var(--강조글);border-radius:8px;background:var(--강조);color:var(--강조글);font-size:11px;font-weight:700;text-align:center}
.종목줄이름{padding-top:8px;margin-top:-8px}   /* 딱지가 위로 5px 나와도 한 줄 칸(overflow:hidden)에 안 잘리게 */
/* ③ 카테고리를 아직 안 골랐을 때 — 흐린 칩(점선) */
.새칸줄.비활성 .칩{color:var(--옅음);border-style:dashed}
/* ② 운동 화면 아래 종목 칸(72 폭 · 이름 두 줄 잘림)은 칸 오른쪽 위 구석에 같은 딱지 */
.운칸:has(>.운칸번호){position:relative}
.운칸번호{position:absolute;top:4px;right:4px;min-width:16px;height:14px;line-height:14px;padding:0 4px;border-radius:8px;background:var(--강조);color:var(--강조글);font-size:11px;font-weight:700;font-style:normal;text-align:center}
/* ④ 운동 목표 부위 — 앞 · 뒤 그림(각 64 × 192) + 오른쪽에 주동근 · 협응근 두 줄 */
.새지도{display:flex;align-items:center;gap:8px;min-width:0}
.새몸칸{flex:none;width:64px;height:192px}
.새몸칸 svg{width:100%;height:100%;display:block}
.새몸근{cursor:pointer}
.새몸 .몸결,.새몸 .몸바탕{pointer-events:none}   /* 근육 결 선이 누름을 가로채지 않게 */
.새지도 .새요약{flex:1;align-self:stretch;justify-content:center}
.새지도 .근줄{white-space:normal;overflow:visible}
/* ⑥ 기본 세팅 세트 줄 — 종목 탭 .종세트 그대로 */
.새세팅{gap:4px}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]
pathlib.Path(OUT).write_text(s, encoding='utf-8'); print("v19 D →", OUT)
