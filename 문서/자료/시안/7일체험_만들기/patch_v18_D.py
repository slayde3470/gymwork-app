"""v18 D (10-04 홍겸 님) — 루틴 화면 종목 상자 기본 접힘 · '종목 넣기' 시트 다시 짜기
① 루틴 안 종목 상자는 처음엔 이름 줄만(접힘). 이름 줄을 누르면 펼침/접힘 — 상자마다 따로 기억(U.루펼침, 상자 객체 기준이라 끌어 옮겨도 그대로)
② 넣기 시트 칩줄 아래에 [+ 새 종목 만들기] (행동 '새종목열기' 은 C 가 만든다)
③ '＋ · 있음 · ✓' 글자 대신 칸 오른쪽 체크 상자 — 들어간 개수만큼 ✓ (✓ / ✓✓ / ✓✓✓, 넷부터 ✓×4). 들어간 칸은 --강조옅음 바탕 + --강조 테.
   누르면 늘 하나 더 넣기(같은 종목 여러 번). 꾹 누르면(420ms) 맨 뒤의 것 하나 빼기.
   전에 있던 '방금 넣은 것을 다시 누르면 뺌' 은 없앴다(누름 = 더하기 하나로).
④ 최근 기록 글 빼고 2열 격자 — 가로 먼저(1|2 / 3|4) · 칸 왼쪽 번호(11 · 흐림) · [플랜] 딱지 유지
⑤ 넣기 시트 높이 고정 — 위끝 = 폰 높이 20% 지점. 칩을 바꿔 칸 수가 달라져도 띠 자리 그대로, 목록만 안에서 스크롤"""
import sys, pathlib
IN, OUT = sys.argv[1], sys.argv[2]
s = pathlib.Path(IN).read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:70]!r}: {c}번")
    s = s.replace(old, new)

# ── ① 루틴 종목 상자 접힘 ──
바꿈('''  const 하나 = e.세트.length<=1;
  return `<div class="종목상자" data-drag="종목줄" data-i="${i}" data-drop="종목줄"><div class="루머리"><b class="채움">${esc(e.이름)}</b>${빼기}</div>''',
'''  const 하나 = e.세트.length<=1;
  /* 10-04 v18 D ① 처음엔 접힘 — 이름 줄([이름 · N세트 ▾])만. 줄을 누르면 펼침/접힘. 줄은 div(끌기 살리려고 · 플랜 줄과 같은 까닭) */
  const 펼=U.루펼침.has(e), 머리=`<div class="루머리"><div class="채움 루접줄" data-act="루펼침" data-i="${i}" role="button" tabindex="0" aria-expanded="${펼}"><b>${esc(e.이름)}</b><span class="루플랜글 숫">· ${e.세트.length}세트</span><span class="접힘표 ${펼?"펼":""}">▾</span></div>${빼기}</div>`;
  if(!펼) return `<div class="종목상자 루접힘" data-drag="종목줄" data-i="${i}" data-drop="종목줄">${머리}</div>`;
  return `<div class="종목상자" data-drag="종목줄" data-i="${i}" data-drop="종목줄">${머리}''')
바꿈('''document.addEventListener("keydown", e=>{ if((e.key==="Enter"||e.key===" ") && e.target.matches?.(".루플랜줄[data-act]")){ e.preventDefault(); e.target.click(); } });''',
'''document.addEventListener("keydown", e=>{ if((e.key==="Enter"||e.key===" ") && e.target.matches?.(".루플랜줄[data-act]")){ e.preventDefault(); e.target.click(); } });
/* 10-04 v18 D ① 펼친 상자 — 상자 객체로 기억(끌어 옮기거나 앞 상자를 빼도 그대로). 처음엔 아무것도 안 펼침 */
U.루펼침 = new WeakSet();
document.addEventListener("keydown", e=>{ if((e.key==="Enter"||e.key===" ") && e.target.matches?.(".루접줄[data-act]")){ e.preventDefault(); e.target.click(); } });''')
바꿈('''    case "종목빼기": { const r=루틴(U.루틴열림); r.종목.splice(+d.i,1); break; }''',
'''    case "종목빼기": { const r=루틴(U.루틴열림); r.종목.splice(+d.i,1); break; }
    case "루펼침": { const e=루틴(U.루틴열림)?.종목[+d.i]; if(e){ if(U.루펼침.has(e)) U.루펼침.delete(e); else U.루펼침.add(e); } break; }   // v18 D ①''')

# ── ③ 누를 때마다 하나 더 · 꾹 누르면 하나 빼기 (기본 세트 값 줄 = else{…} 는 그대로) ──
바꿈('''      if(U.방금.includes(열)){ const k=r.종목.map(e=>e.이름===d.v&&!e.플랜id).lastIndexOf(true); if(k>=0) r.종목.splice(k,1); U.방금=U.방금.filter(x=>x!==열); }''',
     '''      if(d.뺌){ 넣은것뺌(r, e=>e.이름===d.v&&!e.플랜id); }   // v18 D ③ 꾹 누름 = 하나 빼기 · 그냥 누름 = 늘 하나 더''')
바꿈('''      if(U.방금.includes(열)){ const k=r.종목.map(e=>e.플랜id===d.v).lastIndexOf(true); if(k>=0) r.종목.splice(k,1); U.방금=U.방금.filter(x=>x!==열); }''',
     '''      if(d.뺌){ 넣은것뺌(r, e=>e.플랜id===d.v); }   // v18 D ③''')

# ── ②④ 넣기목록 = 2열 격자 · 번호 · 체크 상자 ──
i = s.index('function 넣기목록(r){'); j = s.index('/* w0fo 가장 최근에 한 날', i)
s = s[:i] + '''function 넣기목록(r){ const 칸=U.칸고름, 줄=[];
  for(const x of S.종목표){ if(칸!=="전체" && x.칸!==칸) continue; const 플=S.플랜들.filter(p=>p.종목===x.이름);
    if(플.length) 플.forEach(p=>줄.push({이름:p.이름, p, 종목:x.이름})); else 줄.push({이름:x.이름, 종목:x.이름}); }
  if(칸==="전체") S.플랜들.filter(p=>!S.종목표.some(x=>x.이름===p.종목)).forEach(p=>줄.push({이름:p.이름, p, 종목:p.종목}));   // 종목표에 없는 종목의 플랜도 잃지 않게
  줄.sort((a,b)=>a.이름.localeCompare(b.이름,"ko"));
  /* 10-04 v18 D ③④ 2열 격자(가로 먼저 1|2 / 3|4) · 칸 = [번호][이름[플랜]][체크 상자]. 최근 기록 글은 뺐다.
     체크 상자 = 이 루틴(운동)에 들어간 개수만큼 ✓ — 넷부터 '✓×4'. 들어간 칸은 진하게(--강조옅음). 누르면 하나 더 · 꾹 누르면 하나 빼기 */
  return 줄.map((z,n)=>{ const 수=z.p?r.종목.filter(e=>e.플랜id===z.p.id).length:r.종목.filter(e=>e.이름===z.이름&&!e.플랜id).length;
    return `<button class="넣기칸${수?" 들어감":""}" data-act="${z.p?"플랜넣기":"종목넣기"}" data-v="${z.p?z.p.id:esc(z.이름)}" aria-label="${esc(z.이름)}${z.p?" 플랜":""} · ${수}개 들어 있음 · 누르면 하나 더, 꾹 누르면 하나 빼기"><span class="넣기번 숫">${n+1}</span><span class="이름플랜"><b>${esc(z.이름)}</b>${z.p?플랜딱지:""}</span><span class="넣기체크" aria-hidden="true">${수>3?"✓×"+수:"✓".repeat(수)}</span></button>`; }).join("")
    || `<div class="빈칸">이 칸에 종목이 없습니다</div>`; }
/* v18 D ③ 꾹 눌러 하나 빼기 — 맨 뒤에 들어간 것부터. 운동 중이면 세트를 하나라도 끝낸 것 · 지금 하는 것 · 쉬는 중인 것은 안 뺀다 */
function 넣은것뺌(r, 맞음){ const ss=r===S.세션?r:null; let k=-1;
  for(let j=r.종목.length-1;j>=0;j--){ const e=r.종목[j]; if(!맞음(e)) continue;
    if(ss && (e.세트.some(x=>x.완료) || ss.지금.i===j || ss.휴식?.i===j)) continue; k=j; break; }
  if(k<0){ if(ss && r.종목.some(맞음)) setTimeout(()=>토스트("이미 시작한 종목은 뺄 수 없습니다")); return; }   // 다시 그린 뒤에 띄운다(그리기가 폰 안을 새로 쓴다)
  if(!ss){ r.종목.splice(k,1); return; }
  const 전=r.종목.slice(), 본e=U.본!=null?전[U.본]:null, 지e=전[ss.지금.i], 휴e=ss.휴식?전[ss.휴식.i]:null, 접=ss.접기;
  r.종목.splice(k,1); const 새=e=>r.종목.indexOf(e);
  if(U.본!=null) U.본 = 본e&&새(본e)>=0 ? 새(본e) : Math.min(k, r.종목.length-1);
  if(지e) ss.지금.i=새(지e); if(휴e) ss.휴식.i=새(휴e);
  if(접){ const m={}; 전.forEach((e,j)=>{ const t=새(e); if(접[j]!=null&&t>=0) m[t]=접[j]; }); ss.접기=m; }
  if(U.운지움){ if(U.운지움.i===k) U.운지움=null; else if(U.운지움.i>k) U.운지움.i--; }
  ss.마지막=S.시계; }
/* v18 D ③ 넣기 칸 꾹 누르기(420ms — 다른 꾹 누름과 같은 시간). 8px 넘게 움직이면 스크롤로 보고 그만둔다. 뗄 때 생기는 클릭은 막음클릭으로 막는다 */
let 넣기꾹=null;
document.addEventListener("pointerdown", e=>{ const b=e.target.closest?.("#폰 .넣기칸"); if(!b || (e.pointerType==="mouse"&&e.button!==0)) return;
  clearTimeout(넣기꾹?.t); 넣기꾹={x:e.clientX, y:e.clientY, t:setTimeout(()=>{ if(!넣기꾹) return; 넣기꾹=null; 막음클릭=true;
    if(navigator.vibrate) try{ navigator.vibrate(15); }catch(_){}
    행동(b.dataset.act, {...b.dataset, 뺌:"1"}); }, 420)}; });
document.addEventListener("pointermove", e=>{ if(넣기꾹 && Math.hypot(e.clientX-넣기꾹.x,e.clientY-넣기꾹.y)>8){ clearTimeout(넣기꾹.t); 넣기꾹=null; } });
for(const t of ["pointerup","pointercancel"]) document.addEventListener(t, ()=>{ if(넣기꾹){ clearTimeout(넣기꾹.t); 넣기꾹=null; } });
document.addEventListener("contextmenu", e=>{ if(e.target.closest?.(".넣기칸")) e.preventDefault(); });
/* v18 D ⑤ 목록만 스크롤 — 칸을 눌러 다시 그려도 보던 자리 그대로(칩을 바꾸거나 시트를 닫으면 맨 위부터) */
let 넣기자리=null;
document.addEventListener("scroll", e=>{ const t=e.target; if(t?.classList?.contains("넣기목록")) 넣기자리={키:t.dataset.k, 위:t.scrollTop}; }, true);
/* ⑤ 시트 위끝 = 폰 높이의 20% 지점. 가림은 탭줄 위까지라(탭줄 높이는 글자 · 기기 따라 다름) CSS 80% 로는 안 맞아 여기서 잰다 */
function 넣기시트높이(){ const 폰=document.getElementById("폰"), 시=폰?.querySelector(".시트:has(>.넣기목록)"), 가=시?.parentElement; if(!시||!가) return;
  const p=폰.getBoundingClientRect(), g=가.getBoundingClientRect(); 시.style.height=Math.max(0, g.bottom-(p.top+p.height*0.2))+"px"; }
function 넣기자리맞춤(){ 넣기시트높이(); const l=document.querySelector("#폰 .넣기목록"); if(!l){ 넣기자리=null; return; } if(넣기자리&&넣기자리.키===l.dataset.k) l.scrollTop=넣기자리.위; }
{ const 폰=document.getElementById("폰"); if(폰) new MutationObserver(넣기자리맞춤).observe(폰,{childList:true}); }
window.addEventListener("resize", 넣기시트높이);
''' + s[j:]
# 시트 안: 칩줄 → [+ 새 종목 만들기] 줄 → 격자
바꿈('''    안 = 머리(`${esc(r.이름)}에 넣기`) + `<div class="넣기칩">${칩줄("칸고름","",["전체",...S.카테고리],U.칸고름)}</div><div class="넣기목록">${넣기목록(r)}</div>`; 높=true; }''',
'''    안 = 머리(`${esc(r.이름)}에 넣기`) + `<div class="넣기칩">${칩줄("칸고름","",["전체",...S.카테고리],U.칸고름)}</div>`
      + `<div class="넣기새줄"><button class="버튼 낮 넣기새" data-act="새종목열기">+ 새 종목 만들기</button><span class="아주작 옅음">꾹 누르면 하나 빼기</span></div>`   // v18 D ② 행동은 C
      + `<div class="넣기목록" data-k="${esc(U.칸고름)}">${넣기목록(r)}</div>`; 높=true; }''')

css = '''
/* ═══ 10-04 v18 D — 루틴 종목 상자 접힘 · 종목 넣기 시트(2열 격자 · 체크 상자 · 높이 고정) ═══ */
/* ① 접힌 상자 = 머리 한 줄(플랜 상자와 같은 꼴) · 펼친 상자 머리도 같은 줄을 누른다 */
.종목상자.루접힘,.루넘김 .종목상자.루접힘{padding:0}
.종목상자.루접힘>.루머리,.루넘김 .종목상자.루접힘>.루머리{margin:0;border-radius:15px}
.루접줄{display:flex;align-items:center;gap:8px;min-width:0;min-height:32px;cursor:pointer;white-space:nowrap}
.루접줄>b{font-size:15px;font-weight:700;min-width:0;overflow:hidden;text-overflow:ellipsis;flex:0 1 auto}
.루접줄>.루플랜글{flex:none}
.루접줄>.접힘표{margin-left:auto;flex:none;font-size:13px;color:var(--흐림)}
/* ⑤ 높이 고정 — 위끝 = 폰 높이 20% 지점(가림 = 폰 전체). 머리 · 칩 · 새 종목 줄은 그대로, 목록만 스크롤 */
.시트:has(>.넣기목록){height:80%;max-height:none;overflow:hidden}   /* 높이는 넣기시트높이() 가 px 로 — 80% 는 그 전 잠깐 */
.시트:has(>.넣기목록)>*{flex:none}
/* ② [+ 새 종목 만들기] 줄 — 단추 32 · 오른쪽에 한 줄 도움말 */
.넣기새줄{display:flex;align-items:center;justify-content:space-between;gap:8px}
.넣기새{color:var(--강조)}
/* ④ 2열 격자 — 가로 먼저 */
.시트>.넣기목록{flex:1 1 auto;min-height:0;overflow-y:auto;display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:8px;align-content:start;padding-bottom:8px}
.넣기칸{display:flex;align-items:center;gap:4px;min-width:0;min-height:44px;padding:4px 8px;border:1px solid var(--선);border-radius:var(--r-작게);background:var(--면);text-align:left;-webkit-user-select:none;user-select:none;-webkit-touch-callout:none}
.넣기칸.들어감{background:var(--강조옅음);border-color:var(--강조)}
.넣기번{flex:none;min-width:12px;margin-right:4px;font-size:11px;color:var(--흐림);text-align:right}
.넣기칸 .이름플랜{flex:1 1 auto;padding-top:4px}
.넣기칸 .이름플랜>b{font-size:13px;font-weight:600;line-height:16px;white-space:normal;word-break:keep-all;overflow-wrap:anywhere;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical}
/* ③ 체크 상자 — 20 × 20 · 들어가면 강조 바탕에 ✓ 개수 */
.넣기체크{flex:none;min-width:20px;height:20px;padding:0 4px;border:1px solid var(--속선);border-radius:4px;display:inline-flex;align-items:center;justify-content:center;font-size:11px;font-weight:700;line-height:1;letter-spacing:-0.1em;white-space:nowrap}
.넣기칸.들어감 .넣기체크{background:var(--강조);border-color:var(--강조);color:var(--강조글)}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]
pathlib.Path(OUT).write_text(s, encoding='utf-8'); print("v18 D →", OUT)
