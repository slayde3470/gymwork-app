"""v21 (10-05 홍겸 님) — 운동 화면 · 탭줄 · 시트
① "운동화면에서도 운동삭제 가능하게 해야해" — 아래 종목 칸 줄의 지금 보는 칸 오른쪽 위 구석 ✕(누르는 칸 28 · 아이콘 14 · --옅음).
   누르면 그 종목을 빼고 아래띠 '○○을(를) 뺐습니다 [되돌리기]'(세트 지우기 아래띠 · 되돌리기 부품 그대로). 종목이 하나면 ✕ 없음
② "종목추가 할수있게 해달라" — 운동 중 '종목 넣기' 시트의 [+ 새 종목 만들기]는 v18~v19 그대로 있다(확인만 · 고친 것 없음)
③ 진행 상황 상자 종목 칸 줄 ‹ › — 넣기 시트 칩줄과 같은 동그라미(.칩화살). 오른쪽이 가려지면 [＋] 바로 왼쪽에 ›, 왼쪽이 가려지면 줄 왼쪽 끝에 ‹
④ 맨 아래 단추 넷 [‹][큰 주 단추][오늘 운동/끝내기][›] = 12.5 : 47.5 : 27.5 : 12.5. 큰 주 단추 = 세트 완료하기 · 건너뛰기 · 다음 종목으로 넘어가기 · 운동 마무리
⑤ 탭줄에 '소셜'(사람 상반신 아이콘) — 메모와 설정 사이. 소셜 화면 = 맨 위 띠 '소셜' + 빈 화면
⑥ 아래에서 올라오는 모든 시트 맨 위 가운데 손잡이 막대(36 × 4 · 둥글기 2 · --속선) · 머리 띠를 잡고 80px 넘게 끌어내리면 닫힘
⑦ 점멸 — 지금 세트 줄 · 지금 종목 칸 바탕 1초 주기(--강조옅음 ↔ --지금깜빡) · 쉼 게이지 두 줄 왼쪽과 큰 주 단추 글 왼쪽에 노란 점(6 · 1초)
   움직임 줄임이면 점멸 없음"""
import sys, pathlib
IN, OUT = sys.argv[1], sys.argv[2]
s = pathlib.Path(IN).read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:70]!r}: {c}번")
    s = s.replace(old, new)

# ── ① ③ 종목 칸 줄: 지금 칸 ✕ · ‹ › ─────────────────────────────────────────
바꿈('''      <div class="운띠">${ss.종목.map((x,i)=>{ const m=x.세트.length, k=완(x), 다끝=m&&k===m;
        return `<button class="운칸 ${i===본?"지금":""} ${다끝&&i!==본?"끝":""}" data-act="본종목" data-i="${i}" data-drag="운칸" data-drop="운칸" aria-label="${esc(x.이름)} ${k}/${m}세트">''',
     '''      <div class="운띠틀"><div class="운띠">${ss.종목.map((x,i)=>{ const m=x.세트.length, k=완(x), 다끝=m&&k===m, 뺄=i===본&&n>1;   // v21 ① 지금 보는 칸에만 ✕ (종목이 하나면 없음)
        return `<button class="운칸 ${i===본?"지금":""} ${다끝&&i!==본?"끝":""}${뺄?" 뺄수":""}" data-act="본종목" data-i="${i}" data-drag="운칸" data-drop="운칸" aria-label="${esc(x.이름)} ${k}/${m}세트">''')
바꿈('''<div class="ㅁ"><i style="width:${m?k/m*100:0}%"></i></div></button>`; }).join("")}<button class="운칸 운더" data-act="시트" data-t="종목넣기" data-v="운동" aria-label="종목 넣기">＋</button></div></div>''',
     '''<div class="ㅁ"><i style="width:${m?k/m*100:0}%"></i></div></button>${뺄?`<button class="운칸뺌" data-act="운종목뺌" data-i="${i}" aria-label="${esc(x.이름)} 운동에서 빼기">${운뺌그림}</button>`:""}`; }).join("")}<button class="운칸 운더" data-act="시트" data-t="종목넣기" data-v="운동" aria-label="종목 넣기">＋</button></div><button class="칩화살 왼 운화살" aria-label="종목 칸 왼쪽 보기" hidden>${칩화살그림("M15 6l-6 6 6 6")}</button><button class="칩화살 오 운화살" aria-label="종목 칸 오른쪽 보기" hidden>${칩화살그림("M9 6l6 6-6 6")}</button></div></div>''')

# ── ④ 맨 아래 단추 넷 ─────────────────────────────────────────────────────
바꿈('''    <div class="아랫줄"><button class="버튼 운이전" data-act="이전종목" ${본>0?"":"disabled"}>‹ 이전</button><button class="버튼 운끝내기" data-act="끝내기">운동 끝내기</button>${갈곳>=0?`<button class="버튼 주 채움 한줄" data-act="본종목" data-i="${갈곳}">다음 · ${esc(ss.종목[갈곳].이름)} ›</button>`:`<button class="버튼 주 채움" data-act="끝내기">마무리</button>`}</div>''',
     '''    <div class="아랫줄 운단추줄"><button class="버튼 운이전" data-act="이전종목" aria-label="이전 종목" ${본>0?"":"disabled"}>${칩화살그림("M15 6l-6 6 6 6")}</button>${운주단추(ss,본)}<button class="버튼 운끝내기" data-act="끝내기">오늘 운동<br>끝내기</button><button class="버튼 운다음" data-act="다음종목" aria-label="다음 종목" ${본<n-1?"":"disabled"}>${칩화살그림("M9 6l6 6-6 6")}</button></div>''')
바꿈('''  /* 10-02 표시 4~7 — 맨 위: 지금 종목 정보(③) · 그 아래: 근육 그림 두 장(전신 · 부위) · 세트 ·
     아래: 루틴 진행 + 종목 띠(②) · 맨 아래: 이전 / 운동 끝내기 / 다음. 보는 종목(U.본)은 손으로만 바뀐다 */''',
     '''  /* 10-02 표시 4~7 — 맨 위: 지금 종목 정보(③) · 그 아래: 근육 그림 두 장(전신 · 부위) · 세트 ·
     아래: 루틴 진행 + 종목 띠(②) · 맨 아래: 이전 / 운동 끝내기 / 다음. 보는 종목(U.본)은 손으로만 바뀐다
     10-05 v21 ④ 맨 아래 = [‹][큰 주 단추(운주단추)][오늘 운동/끝내기][›]. '다음 · 종목이름' 단추는 없앴다 */''')

# 큰 주 단추 · 종목 빼기 · 을/를 — 운세트줄 바로 앞에
바꿈('''\nfunction 운세트줄(i,s,k,지금k){''', '''
/* ═══ 10-05 v21 ④ 큰 주 단추 — "'세트 완료하기' 버튼이면서, 휴식중엔 '건너뛰기', 운동의 마지막 세트가 끝나면 '다음 종목으로 넘어가기',
   마지막 운동의 마지막 세트가 끝나면 '운동 마무리'". 쉬는 중 + 마지막 세트일 때 문구 순서는 쉼 게이지와 같다(마무리 > 다음 종목 > 건너뛰기).
   행동은 시안에 있던 것 그대로: 체크(세트 동그라미) · 휴식건너뛰기 · 다음종목으로(게이지 '건너뛰고 다음 운동으로 넘어가기'와 같음) · 끝내기 ═══ */
function 운주상태(ss, 본){ const e=ss.종목[본], 남=x=>x.세트.some(s=>!s.완료);
  if(!ss.종목.some(남)) return {글:"운동 마무리", act:"끝내기"};
  if(!남(e)) return {글:"다음 종목으로 넘어가기", act:"다음종목으로"};
  if(ss.휴식) return {글:"건너뛰기", act:"휴식건너뛰기"};
  return {글:"세트 완료하기", act:"체크", i:본, k:e.세트.findIndex(s=>!s.완료)}; }
function 운주단추(ss, 본){ const x=운주상태(ss,본);
  return `<button class="버튼 주 운주" data-act="${x.act}"${x.act==="체크"?` data-i="${x.i}" data-k="${x.k}"`:""} aria-label="${x.글}"><i class="노란점" aria-hidden="true"></i><span class="운주글">${x.글}</span></button>`; }
/* 글은 한 줄 15 → 13, 그래도 넘치면 13 두 줄 (… 없음) */
/* v21 ⑦② 쉼 게이지 문구 — 노란 점(6 + 틈 8) 자리만큼 좁아져 긴 문구('마무리하고 운동 보고서 화면으로 넘어가기')가 360 폭에서 넘치면
   글자 11 은 그대로 두고 자간만 −0.02 ~ −0.08em 으로 좁힌다(이름맞춤과 같은 방식 · 그래도 넘치면 예전처럼 …) */
function 쉼글맞춤(폰){ 폰.querySelectorAll(".쉼게이지 small").forEach(el=>{ el.style.letterSpacing="";
  for(let n=2; n<=8 && el.scrollWidth>el.clientWidth+0.5; n++) el.style.letterSpacing=(-n/100)+"em"; }); }
function 운주맞춤(폰){ const el=폰.querySelector(".운주글"); if(!el||!el.getClientRects().length) return; el.classList.remove("두줄"); el.style.fontSize="";
  for(const fs of [15,13]){ el.style.fontSize=fs+"px"; if(el.scrollWidth<=el.clientWidth+0.5) return; }
  el.classList.add("두줄"); }
/* ═══ 10-05 v21 ① 운동 화면에서 종목 빼기 — 지금 보는 칸 ✕. 묻지 않고 빼고 아래띠 5초 [되돌리기] (세트 지우기와 같은 아래띠 · U.운지움 · U5-4).
   체크한 세트가 있어도 종목(객체)째 보관했다가 되돌리면 그 자리에 그대로. 지금 · 휴식 · 보는 종목 · 접기 번호는 v14 끌기처럼 종목 객체로 다시 맞춘다 ═══ */
const 운뺌그림 = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" aria-hidden="true"><path d="M6 6l12 12M18 6L6 18"/></svg>`;
const 을를 = 글=>{ const c=String(글).trim().slice(-1).charCodeAt(0)-0xAC00; return c>=0&&c<11172 ? (c%28?"을":"를") : "을(를)"; };
function 운종목빼기(i){ const ss=S.세션, e=ss?.종목[i]; if(!e||ss.종목.length<=1) return;
  const 전=ss.종목.slice(), 본e=U.본!=null?전[U.본]:null, 지e=전[ss.지금.i], 휴e=ss.휴식?전[ss.휴식.i]:null, 접=ss.접기||{};
  const id=++운지움번호; U.운지움={id, 종류:"종목", 세션:ss.시작, i, e, 지금:{...ss.지금}, 휴식:ss.휴식?{...ss.휴식}:null, 접기:{...접}};
  ss.종목.splice(i,1); const 새=x=>ss.종목.indexOf(x), 남=x=>x.세트.some(s=>!s.완료);
  U.본 = 본e&&새(본e)>=0 ? 새(본e) : Math.min(i, ss.종목.length-1);   // 뺀 칸 자리에 온 종목(맨 끝이었으면 그 앞)
  if(지e===e){ const 뒤=ss.종목.findIndex((x,j)=>j>=i&&남(x)), 앞=ss.종목.findIndex(남), j=뒤>=0?뒤:앞;
    ss.지금 = j>=0 ? {i:j, k:ss.종목[j].세트.findIndex(s=>!s.완료)} : {i:Math.min(i,ss.종목.length-1), k:0}; }
  else ss.지금.i=새(지e);
  if(휴e===e) ss.휴식=null; else if(휴e) ss.휴식.i=새(휴e);
  const m={}; 전.forEach((x,j)=>{ const t=새(x); if(접[j]!=null&&t>=0) m[t]=접[j]; }); ss.접기=m;
  ss.마지막=S.시계; 운자리.본=null; 발자취(`종목 뺌 · ${e.이름}`);   // 운자리.본=null — 같은 번호에 다른 종목이 와도 세트 목록을 맨 위부터
  setTimeout(()=>{ if(U.운지움?.id!==id) return; U.운지움=null;
    document.querySelectorAll("#폰 .운지움띠").forEach(el=>{ el.classList.add("나감띠"); setTimeout(()=>el.remove(),200); }); }, 5000); }
function 운종목되돌리기(z){ const ss=S.세션, at=Math.min(z.i, ss.종목.length);
  const 전=ss.종목.slice(), 지e=전[ss.지금.i], 휴e=ss.휴식?전[ss.휴식.i]:null, 접=ss.접기||{};
  ss.종목.splice(at,0,z.e); const 새=x=>ss.종목.indexOf(x);
  if(z.지금.i===z.i) ss.지금={i:at, k:z.지금.k}; else if(지e) ss.지금.i=새(지e);
  if(z.휴식 && z.휴식.i===z.i && z.휴식.끝>S.시계 && !ss.휴식) ss.휴식={...z.휴식, i:at}; else if(휴e) ss.휴식.i=새(휴e);
  const m={}; 전.forEach((x,j)=>{ if(접[j]!=null) m[새(x)]=접[j]; }); if(z.접기[z.i]!=null) m[at]=z.접기[z.i]; ss.접기=m;
  U.본=at; 운자리.본=null; ss.마지막=S.시계; 발자취(`종목 되돌림 · ${z.e.이름}`); }
function 운세트줄(i,s,k,지금k){''')
바꿈('''function 세트되돌리기(){ const z=U.운지움, ss=S.세션; U.운지움=null; if(!z||!ss||z.세션!==ss.시작) return; const x=ss.종목[z.i]; if(!x) return;''',
     '''function 세트되돌리기(){ const z=U.운지움, ss=S.세션; U.운지움=null; if(!z||!ss||z.세션!==ss.시작) return; if(z.종류==="종목"){ 운종목되돌리기(z); return; } const x=ss.종목[z.i]; if(!x) return;   // v21 ① 종목 빼기도 같은 [되돌리기]''')
바꿈('''<span class="채움">${esc(ss.종목[z.i]?.이름||"")} ${z.k+1}세트를 지웠습니다</span>''',
     '''<span class="채움">${z.종류==="종목"?`${esc(z.e.이름)}${을를(z.e.이름)} 뺐습니다`:`${esc(ss.종목[z.i]?.이름||"")} ${z.k+1}세트를 지웠습니다`}</span>''')
# 행동
바꿈('''    case "세트지움": 세트지우기(+d.i,+d.k); break;      // 10-03 ✎ ai39 — 묻지 않고 지우고 아래띠로 되돌린다 (U5-4)''',
     '''    case "세트지움": 세트지우기(+d.i,+d.k); break;      // 10-03 ✎ ai39 — 묻지 않고 지우고 아래띠로 되돌린다 (U5-4)
    case "운종목뺌": 운종목빼기(+d.i); break;   // v21 ①
    case "다음종목으로": { const 본=U.본??0; if(ss.휴식&&ss.휴식.i===본) ss.휴식=null;   // v21 ④ 게이지 '건너뛰고 다음 운동으로 넘어가기'(휴식건너뛰기)와 같은 곳 — 다음 안 끝난 종목(뒤쪽 먼저, 없으면 앞쪽)
      const 남=x=>x.세트.some(s=>!s.완료), 뒤=ss.종목.findIndex((x,j)=>j>본&&남(x)), 앞=ss.종목.findIndex(남), 갈=뒤>=0?뒤:앞; if(갈>=0) U.본=갈; break; }''')

# ③ ‹ › 맞춤 · 누름(칩화살 누름 처리에 .운띠 도) · 스크롤
바꿈('''document.addEventListener("click", e=>{ const b=e.target.closest?.("#폰 .칩화살"); if(!b) return; const 줄=b.parentElement.querySelector(".칩줄");''',
     '''document.addEventListener("click", e=>{ const b=e.target.closest?.("#폰 .칩화살"); if(!b) return; const 줄=b.parentElement.querySelector(".칩줄,.운띠"); if(!줄) return;   // v21 ③ 종목 칸 줄도 같은 부품''')
바꿈('''  if(t.classList.contains("운세트들")) 운자리.위=t.scrollTop; else if(t.classList.contains("운띠")) 운자리.옆=t.scrollLeft; }, true);''',
     '''  if(t.classList.contains("운세트들")) 운자리.위=t.scrollTop; else if(t.classList.contains("운띠")){ 운자리.옆=t.scrollLeft; 운화살맞춤(); } }, true);
/* 10-05 v21 ③ 종목 칸 줄 ‹ › — 넣기 시트 칩줄과 같은 부품(.칩화살 28 · --면 바탕 · --선 테). 오른쪽에 가려진 칸이 있으면 [＋] 바로 왼쪽에 ›,
   왼쪽이 가려졌으면 줄 왼쪽 끝에 ‹. 누르면 줄 폭의 70% 만큼 부드럽게(칩화살 누름 처리) · 끝에 닿으면 그쪽 숨김.
   손가락 밀기 · 마우스 끌기 · 꾹 끌기 중 밀림 · 부드러운 이동 모두 scroll 이벤트로 다시 맞춘다 */
function 운화살맞춤(){ const 줄=document.querySelector("#폰 .운띠틀>.운띠"); if(!줄) return; const 틀=줄.parentElement, 끝=줄.scrollWidth-줄.clientWidth;
  틀.querySelector(".운화살.왼").hidden = !(끝>1 && 줄.scrollLeft>1);
  틀.querySelector(".운화살.오").hidden = !(끝>1 && 줄.scrollLeft<끝-1);
  /* v21 ① ✕ 는 [＋] 위(z 2)에 그려 [＋] 바로 앞 칸(마지막 칸 · 종목이 적을 때)에서도 다 보이게. 대신 칸 오른쪽 끝이 [＋] 밑으로 들어가면 감춘다 */
  const 뺌=줄.querySelector(".운칸뺌"), 더=줄.querySelector(".운더");
  if(뺌&&더) 뺌.classList.toggle("밑에듦", 뺌.previousElementSibling.getBoundingClientRect().right > 더.getBoundingClientRect().left-4+0.5); }
window.addEventListener("resize", 운화살맞춤);''')
# 보는 칸을 맨 앞으로 당길 때 ‹(28 + 틈 4)에 가리지 않게
바꿈('''  if(칸 && (!같은종목 || 당김)){ const 첫=줄.querySelector(".운칸"), 끝=줄.scrollWidth-줄.clientWidth, 목표=Math.max(0,Math.min(끝, 칸.offsetLeft-(첫?첫.offsetLeft:0)));   // 10-04 보는 칸을 맨 앞(왼쪽)에''',
     '''  if(칸 && (!같은종목 || 당김)){ const 첫=줄.querySelector(".운칸"), 끝=줄.scrollWidth-줄.clientWidth, 앞=칸.offsetLeft-(첫?첫.offsetLeft:0), 목표=Math.max(0,Math.min(끝, 앞>0?앞-32:0));   // 10-04 보는 칸을 맨 앞(왼쪽)에 · v21 ③ 앞에 칸이 있으면 ‹(28 + 틈 4) 자리를 비운다''')

# 그린 뒤 맞춤 — 화살 · 큰 단추 글 · 점멸 위상
바꿈('''  체험막대(); 맞춤하기(폰); 이름맞춤(폰); 시계그리기(); 애니(폰); 띠맞춤(폰); 저장();   // v20 ① 이름맞춤''',
     '''  체험막대(); 맞춤하기(폰); 이름맞춤(폰); 운화살맞춤(); 운주맞춤(폰); 쉼글맞춤(폰); 시계그리기(); 애니(폰); 띠맞춤(폰); 저장();   // v20 ① 이름맞춤 · v21 ③④
  폰.style.setProperty("--점멸늦춤", -Math.round(performance.now()%1000)+"ms");   // v21 ⑦ 다시 그려도 깜빡임 박자가 끊기지 않게(모든 점멸이 같은 박자)''')

# ── ⑤ 소셜 탭 ────────────────────────────────────────────────────────────
바꿈('''["메모",아이콘.연필],["설정",아이콘.톱니]];''', '''["메모",아이콘.연필],["소셜",아이콘.사람],["설정",아이콘.톱니]];   // 10-05 v21 ⑤ 소셜 = 설정 왼쪽''')
바꿈('''    돋보기:a('<circle cx="11" cy="11" r="7"/><path d="M20 20l-4-4"/>'),''',
     '''    돋보기:a('<circle cx="11" cy="11" r="7"/><path d="M20 20l-4-4"/>'),
    사람:a('<circle cx="12" cy="7" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/>'),   // 10-05 v21 ⑤ 사람 상반신 — 머리 원 + 어깨 호''')
바꿈('''U.탭==="검색"?검색탭():설정탭();''', '''U.탭==="검색"?검색탭():U.탭==="소셜"?소셜탭():설정탭();''')
바꿈('''function 설정탭(){ const s=S.설정, 몸=S.몸;''', '''/* 10-05 v21 ⑤ "그룹운동 및 챌린지 기능을 넣으려고해" — 소셜 화면. 맨 위 띠(루틴 · 플랜과 같은 .띠 가운데띠) + 빈 화면 (설명 글 없음) */
function 소셜탭(){ return `<div class="띠 가운데띠 소셜띠"><b class="채움">소셜</b></div><div class="넘김 띠아래"></div>`; }
function 설정탭(){ const s=S.설정, 몸=S.몸;''')

# ── ⑥ 시트 손잡이 · 끌어내려 닫기 ───────────────────────────────────────────
바꿈('''role="dialog">${안}</div></div>`; }''', '''role="dialog">${손잡이붙임(안)}</div></div>`; }
/* 10-05 v21 ⑥ "가로줄을 쳐줘서 '접을 수 있다' 라는 인식" — 시트 맨 위 가운데 손잡이 막대. 머리 띠(.머리 · sticky) 안 맨 위에 얹는다(띠와 같이 붙어 있다).
   머리가 없는 시트면 맨 앞에 */
function 손잡이붙임(안){ const 막대=`<i class="시트손잡이" aria-hidden="true"></i>`, m=/^\\s*<div class="머리[^"]*"[^>]*>/.exec(안);
  return m ? 안.slice(0,m[0].length)+막대+안.slice(m[0].length) : 막대+안; }
/* 끌어내려 닫기 — 머리 띠(손잡이 포함)를 잡고 아래로. 8px 넘게 내려야 끌기(먼저 위로 움직이면 그만). 80px 넘게 내리고 놓으면 닫힘 =
   머리의 [닫기]와 같은 곳(방식 시트 → 고침 시트 · 새 종목 시트 → 종목 넣기 시트 · 그 밖 → 닫힘), 덜 내리면 제자리.
   머리만 잡히므로(머리 touch-action:none) 시트 안 스크롤과 겹치지 않는다. 뗄 때 생기는 클릭은 막음클릭으로 */
let 시트끌=null;
document.addEventListener("pointerdown", e=>{ if(e.pointerType==="mouse"&&e.button!==0) return;
  const 머=e.target.closest?.("#폰 .시트>.머리"); if(!머 || e.target.closest("input,textarea,select")) return;
  시트끌={시:머.parentElement, 가:머.parentElement.parentElement, y:e.clientY, dy:0, 켜짐:false, id:e.pointerId}; });
document.addEventListener("pointermove", e=>{ const x=시트끌; if(!x||e.pointerId!==x.id) return; const dy=e.clientY-x.y;
  if(!x.켜짐){ if(dy<-8){ 시트끌=null; return; } if(dy<=8) return; x.켜짐=true; x.y+=8; x.시.classList.add("끌었음","끄는중"); x.가.classList.add("끌었음"); }
  e.preventDefault(); x.dy=Math.max(0,e.clientY-x.y); x.시.style.transform=`translateY(${x.dy}px)`; }, {passive:false});
function 시트끌끝(e){ const x=시트끌; if(!x||e.pointerId!==x.id) return; 시트끌=null; if(!x.켜짐) return;
  막음클릭=true; setTimeout(()=>{ 막음클릭=false; });
  x.시.classList.remove("끄는중"); const 줄임=움직임줄임();
  if(x.dy>80 && e.type!=="pointercancel"){ const 방=!!x.시.querySelector(':scope>.머리 [data-act="방식닫기"]');
    const 끝=()=>{ if(방) 행동("방식닫기",{}); else if(U.시트?.종류==="새종목"&&U.시트.돌아감) 행동("시트닫기",{}); else { U.시트=null; 그리기(); } };
    if(줄임) return 끝();
    x.시.style.transition="transform .17s ease-in"; x.시.style.transform="translateY(100%)"; x.가.style.transition="opacity .17s ease-in"; x.가.style.opacity="0"; setTimeout(끝,170); }
  else { x.시.style.transition=줄임?"":"transform .2s ease-out"; x.시.style.transform=""; } }
document.addEventListener("pointerup", 시트끌끝); document.addEventListener("pointercancel", 시트끌끝);''')

# ── ⑦ 쉼 게이지 노란 점 ─────────────────────────────────────────────────────
바꿈('''<span class="밑"><b data-timer="휴식"></b><small>${쉼글}</small></span><span class="위" data-쉼바><b data-timer="휴식"></b><small>${쉼글}</small></span>''',
     '''<span class="밑"><span class="쉼글"><i class="노란점" aria-hidden="true"></i><b data-timer="휴식"></b><small>${쉼글}</small></span></span><span class="위" data-쉼바><span class="쉼글"><i class="노란점" aria-hidden="true"></i><b data-timer="휴식"></b><small>${쉼글}</small></span></span>''')

css = '''
/* ═══ 10-05 v21 — 운동 화면(종목 빼기 ✕ · 칸 줄 ‹ › · 단추 넷 · 점멸) · 소셜 탭 · 시트 손잡이 ═══ */
/* 새 색: --지금깜빡 = 지금 줄 · 칸 점멸의 옅은 쪽(--강조옅음 과 바탕 사이) · --노랑 = 누를 수 있다는 점 · --노랑테 = 밝은 바탕(다크의 강조 단추) 위 노란 점 테 */
:root{--지금깜빡:#EEF3F7; --노랑:#E8A400; --노랑테:transparent}
@media (prefers-color-scheme: dark){ :root:not([data-theme="light"]){--지금깜빡:#142536; --노랑:#FFD54F; --노랑테:#04213D}}
:root[data-theme="dark"]{--지금깜빡:#142536; --노랑:#FFD54F; --노랑테:#04213D}

/* ① 지금 보는 칸 ✕ — 칸 바로 뒤 형제 단추(단추 안에 단추를 둘 수 없다). 누르는 칸 28 × 28 의 가운데 = 칸 오른쪽 위 꼭짓점
   (왼쪽 여백 −18 · 오른쪽 −14 로 다음 칸 자리는 그대로 · 위 −14 — 줄 위 여백 8 밖은 잘려 실제로 누르는 높이는 22).
   아이콘 14 = 동그라미 16 안(--면 바탕 · --선 테 · ‹ › 와 같은 꼴). 꼭짓점에 걸쳐 이름 글자(칸 안쪽 9 안)와 안 겹친다.
   같은 이름 번호(오른쪽 위)는 세트 수 줄 오른쪽으로 */
.운띠>.운칸뺌{flex:0 0 28px;height:28px;margin:-14px -14px 0 -18px;align-self:flex-start;position:relative;z-index:2;display:flex;align-items:center;justify-content:center;color:var(--옅음)}
.운띠>.운칸뺌.밑에듦{visibility:hidden}   /* 칸이 [＋](붙박이) 밑으로 들어가면 ✕ 도 숨김 (운화살맞춤) */
.운칸뺌 svg{width:16px;height:16px;padding:1px;border-radius:50%;background:var(--면);box-shadow:0 0 0 1px var(--선);flex:none}
.운칸.뺄수 .운칸번호{top:34px;right:7px}

/* ③ 칸 줄 ‹ › — .칩화살 그대로. 위 8(.운띠 위 여백)을 빼고 칸 높이 가운데. › = [＋](44) + 틈 4 바로 왼쪽. [＋](z 1) · ✕(z 2) 위 */
.운띠틀{position:relative}
.운띠틀>.운화살{top:8px;z-index:3}
.운띠틀>.운화살.오{right:48px}
.운띠틀:has(.끌림)>.운화살,.운띠틀:has(.끌림) .운칸뺌{pointer-events:none}   /* 꾹 끌기 중에는 놓을 칸을 가리지 않게 */

/* ④ 맨 아래 단추 넷 — 폭 12.5 : 47.5 : 27.5 : 12.5 (줄 안 폭에서 틈 8 × 3 을 뺀 나머지를 이 비율로 · 안쪽 여백과 상관없이 정확히). 높이 40 그대로 */
.운단추줄>.버튼{min-width:0;padding:0 4px}
.운단추줄>.운이전,.운단추줄>.운다음{flex:none;width:calc((100% - 24px) * .125)}
.운단추줄>.운주{flex:none;width:calc((100% - 24px) * .475);padding:0 8px;gap:8px}
.운단추줄>.운끝내기{flex:none;width:calc((100% - 24px) * .275);font-size:13px;line-height:16px;text-align:center;white-space:nowrap}
.운단추줄 svg{width:20px;height:20px;flex:none}
.운단추줄>.버튼:disabled{opacity:.35;cursor:default}
.운주글{min-width:0;font-size:15px;font-weight:700;line-height:20px;white-space:nowrap;overflow:hidden}
.운주글.두줄{white-space:normal;word-break:keep-all;text-align:center;font-size:13px;line-height:16px}

/* ⑤ 탭 9개 — 360 폭(칸 36)에서도 글자 11 · 자간 −0.04em 그대로 들어간다 */

/* ⑥ 시트 손잡이 막대 36 × 4 · 둥글기 2 · --속선. 머리 띠 위끝에서 4 */
.시트 .머리>.시트손잡이{position:absolute;top:4px;left:50%;margin-left:-18px;width:36px;height:4px;border-radius:2px;background:var(--속선);pointer-events:none}
.시트>.시트손잡이{display:block;flex:none;align-self:center;width:36px;height:4px;border-radius:2px;background:var(--속선)}
.시트>.머리{touch-action:none;-webkit-user-select:none;user-select:none;cursor:grab}
.시트.끌었음,.가림.끌었음{animation:none!important}   /* 들어옴 움직임(fill both)이 끌기 transform 을 덮지 않게 */
.시트.끄는중{transition:none!important}
.시트.끄는중>.머리{cursor:grabbing}

/* ⑦ 점멸 — 1초 주기. 지금 세트 줄 · 지금 종목 칸 = 바탕만(--강조옅음 ↔ --지금깜빡 · 글자 · 테는 그대로). 노란 점 = 지름 6 */
@keyframes 지금깜빡{0%,100%{background-color:var(--강조옅음)}50%{background-color:var(--지금깜빡)}}
@keyframes 노란깜빡{0%,100%{opacity:1}50%{opacity:.15}}
.운세트들 .세트줄.지금,.운띠 .운칸.지금{animation:지금깜빡 1s ease-in-out var(--점멸늦춤,0s) infinite}
.노란점{flex:none;width:6px;height:6px;border-radius:50%;background:var(--노랑);box-shadow:0 0 0 1px var(--노랑테);animation:노란깜빡 1s ease-in-out var(--점멸늦춤,0s) infinite}
/* 쉼 게이지 — [점 | 시간 / 문구] 묶음을 가운데에. 격자 두 칸(점 6 · 틈 8 · 글). 점은 두 줄 묶음의 왼쪽, 세로는 두 줄 사이(윗줄 아랫변)에 가운데.
   문구 자리는 게이지 − 4 × 2 − 14 (예전: 게이지 − 문구 좌우 4 × 2) */
.운세트들 .쉼게이지 .밑,.운세트들 .쉼게이지 .위{padding:0 4px}
.운세트들 .쉼게이지 .쉼글{display:grid;grid-template-columns:6px minmax(0,auto);column-gap:8px;align-items:center;min-width:0;max-width:100%}
.운세트들 .쉼게이지 .쉼글>.노란점{grid-area:1/1;align-self:end;transform:translateY(50%)}
.운세트들 .쉼게이지 .쉼글>b{grid-area:1/2;justify-self:center}
.운세트들 .쉼게이지 .쉼글>small{grid-area:2/2;justify-self:center;padding:0}
@media (prefers-reduced-motion: reduce){ .운세트들 .세트줄.지금,.운띠 .운칸.지금,.노란점{animation:none!important} }
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]
pathlib.Path(OUT).write_text(s, encoding='utf-8'); print("v21 →", OUT)
