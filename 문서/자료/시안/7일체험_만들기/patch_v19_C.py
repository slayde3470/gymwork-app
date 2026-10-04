"""v19 C (10-04 홍겸 님) — '종목 넣기' 시트 · 루틴 화면 플랜 종목 줄
① 넣기 시트는 열 때마다 칩 = [전체]
② [+ 새 종목 만들기] = 시트 오른쪽 아래 구석에 떠 있는 작은 단추(32). 목록 아래에 단추 자리만큼 여백
③ 누름: 안 들어간 칸 = 하나 넣기 · 들어간 칸 = 하나 빼기(맨 뒤 것) · 꾹(420ms) = 하나 더 넣기.
   눌러서 넣은 순간 그 칸 위에 작은 말풍선 '꾸욱 누르면 한번 더 추가 됩니다.' (반투명 · 누름 통과 · 1.5초 뒤 흐려짐).
   '꾹 누르면 하나 빼기' 상시 설명은 지움. 운동 중 '이미 시작한 종목은 뺄 수 없음' 은 그대로(넣은것뺌)
④ 칩줄 = 한 줄 가로 스크롤(스크롤바 숨김 · 손가락 · 마우스 끌기 · 휠). 넘칠 때만 끝에 동그라미 ‹ › (28 · --면 · --선 테 · 그림자 없음).
   누르면 칩줄 폭의 70% 만큼 부드럽게. 끝에 닿은 쪽 단추는 숨김
⑤ 루틴 화면 플랜 종목 줄 = 이름[플랜] · N세트 · N회차 · 달성률 게이지(막대 + %) — 이 순서만"""
import sys, pathlib
IN, OUT = sys.argv[1], sys.argv[2]
s = pathlib.Path(IN).read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:70]!r}: {c}번")
    s = s.replace(old, new)

# ── ① 열 때마다 [전체] ──
바꿈('''if(d.t==="종목넣기") U.방금=[];''',
     '''if(d.t==="종목넣기"){ U.방금=[]; U.칸고름="전체"; }   /* v19 C ① 열 때마다 [전체] */''')

# ── ②④ 시트 안: 칩줄(+ ‹ › 단추) → 목록 → 떠 있는 [+ 새 종목 만들기] ──
바꿈('''    안 = 머리(`${esc(r.이름)}에 넣기`) + `<div class="넣기칩">${칩줄("칸고름","",["전체",...S.카테고리],U.칸고름)}</div>`
      + `<div class="넣기새줄"><button class="버튼 낮 넣기새" data-act="새종목열기">+ 새 종목 만들기</button><span class="아주작 옅음">꾹 누르면 하나 빼기</span></div>`   // v18 D ② 행동은 C
      + `<div class="넣기목록" data-k="${esc(U.칸고름)}">${넣기목록(r)}</div>`; 높=true; }''',
'''    /* v19 C ④ 칩줄 한 줄 + 넘칠 때만 끝에 ‹ › (숨김/보임은 칩화살맞춤) · ② 새 종목 단추는 오른쪽 아래 구석에 떠 있다 · ③ 상시 설명 지움 */
    안 = 머리(`${esc(r.이름)}에 넣기`) + `<div class="넣기칩">${칩줄("칸고름","",["전체",...S.카테고리],U.칸고름)}<button class="칩화살 왼" aria-label="칩 왼쪽 보기" hidden>${칩화살그림("M15 6l-6 6 6 6")}</button><button class="칩화살 오" aria-label="칩 오른쪽 보기" hidden>${칩화살그림("M9 6l6 6-6 6")}</button></div>`
      + `<div class="넣기목록" data-k="${esc(U.칸고름)}">${넣기목록(r)}</div>`
      + `<button class="버튼 낮 주 넣기새" data-act="새종목열기">+ 새 종목 만들기</button>`; 높=true; }''')

# ── ③ 누름 = 넣기/빼기 · 꾹 = 하나 더 ──
바꿈('''      if(d.뺌){ 넣은것뺌(r, e=>e.이름===d.v&&!e.플랜id); }   // v18 D ③ 꾹 누름 = 하나 빼기 · 그냥 누름 = 늘 하나 더''',
     '''      const 맞=e=>e.이름===d.v&&!e.플랜id;
      if(!d.더 && r.종목.some(맞)){ 넣은것뺌(r, 맞); }   // v19 C ③ 들어간 칸 누름 = 하나 빼기 · 안 들어간 칸 누름 · 꾹 = 하나 넣기''')
바꿈('''      if(d.뺌){ 넣은것뺌(r, e=>e.플랜id===d.v); }   // v18 D ③''',
     '''      const 맞=e=>e.플랜id===d.v;
      if(!d.더 && r.종목.some(맞)){ 넣은것뺌(r, 맞); }   // v19 C ③''')
바꿈('''U.방금.push(열); if(!운) 업적알림(업적판정("루틴")); } break; }''',
     '''U.방금.push(열); if(!운) 업적알림(업적판정("루틴")); if(!d.더) setTimeout(()=>넣기말풍선(열)); } break; }''', n=2)   # 다시 그린 뒤 그 칸 위에
바꿈('''    행동(b.dataset.act, {...b.dataset, 뺌:"1"}); }, 420)}; });''',
     '''    행동(b.dataset.act, {...b.dataset, 더:"1"}); }, 420)}; });   // v19 C ③ 꾹 = 하나 더''')
바꿈('''aria-label="${esc(z.이름)}${z.p?" 플랜":""} · ${수}개 들어 있음 · 누르면 하나 더, 꾹 누르면 하나 빼기"''',
     '''aria-label="${esc(z.이름)}${z.p?" 플랜":""} · ${수}개 들어 있음 · ${수?"누르면 하나 빼기":"누르면 넣기"}, 꾹 누르면 하나 더"''')

# ── ③④ 말풍선 · 칩줄 가로 스크롤 (넣기 시트 함수들 뒤에) ──
바꿈('''window.addEventListener("resize", 넣기시트높이);''',
'''window.addEventListener("resize", 넣기시트높이);
/* v19 C ③ 눌러서 넣은 칸 위에 작은 말풍선 — 반투명 · 누름 통과(pointer-events:none) · 1.5초 뒤 흐려짐.
   칸 위에 자리가 없으면(목록 첫 줄) 칸 아래. 시트 안에 붙인다(다시 그리면 같이 사라진다 — 토스트와 같음) */
let 넣기말타이머=0;
function 넣기말풍선(열){ const act=열.startsWith("p:")?"플랜넣기":"종목넣기", v=열.slice(2);
  const 칸=document.querySelector(`#폰 .넣기칸[data-act="${act}"][data-v="${CSS.escape(v)}"]`), 시=칸?.closest(".시트"), 목=시?.querySelector(".넣기목록"); if(!칸||!목) return;
  시.querySelectorAll(".넣기말").forEach(x=>x.remove());
  const el=document.createElement("div"); el.className="넣기말"; el.setAttribute("role","status"); el.textContent="꾸욱 누르면 한번 더 추가 됩니다."; 시.appendChild(el);
  const s=시.getBoundingClientRect(), c=칸.getBoundingClientRect(), l=목.getBoundingClientRect(), w=el.offsetWidth, h=el.offsetHeight;
  const 위=c.top-h-4>=l.top ? c.top-h-4 : c.bottom+4;
  el.style.top=(위-s.top)+"px"; el.style.left=Math.max(8, Math.min(s.width-w-8, c.left-s.left+c.width/2-w/2))+"px";
  clearTimeout(넣기말타이머); 넣기말타이머=setTimeout(()=>{ el.classList.add("나감"); setTimeout(()=>el.remove(),200); },1500); }
/* v19 C ④ 칩줄 — 한 줄 가로 스크롤. 넘칠 때만 ‹ › · 끝에 닿은 쪽은 숨김. 칸을 눌러 다시 그려도 보던 자리 그대로(같은 시트일 때) ·
   새로 열면 맨 앞. 고른 칩이 바뀌면(새 종목을 만들고 돌아오는 등) 그 칩이 보이게 */
function 칩화살그림(d){ return `<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="${d}"/></svg>`; }
function 칩화살맞춤(){ const 줄=document.querySelector("#폰 .넣기칩 .칩줄"); if(!줄) return; const 끝=줄.scrollWidth-줄.clientWidth, 칩=줄.parentElement;
  칩.querySelector(".칩화살.왼").hidden = !(끝>1 && 줄.scrollLeft>1);
  칩.querySelector(".칩화살.오").hidden = !(끝>1 && 줄.scrollLeft<끝-1); }
let 칩자리=null;
function 칩줄맞춤(){ const 줄=document.querySelector("#폰 .넣기칩 .칩줄"); if(!줄){ 칩자리=null; return; }
  const 같은=칩자리&&칩자리.시트===U.시트; if(같은) 줄.scrollLeft=칩자리.옆;
  if(!같은 || 칩자리.칸!==U.칸고름){ const 켬=줄.querySelector(".칩.켬"); if(켬){ const r=켬.getBoundingClientRect(), z=줄.getBoundingClientRect(), 가=32;   // 가 = 화살 28 + 틈 4
      if(r.left<z.left+가 && 줄.scrollLeft>0) 줄.scrollLeft-=z.left+가-r.left; else if(r.right>z.right-가) 줄.scrollLeft+=r.right-(z.right-가); } }
  칩자리={시트:U.시트, 칸:U.칸고름, 옆:줄.scrollLeft}; 칩화살맞춤(); }
{ const 폰=document.getElementById("폰"); if(폰) new MutationObserver(칩줄맞춤).observe(폰,{childList:true}); }
window.addEventListener("resize", 칩화살맞춤);
try{ document.fonts?.addEventListener("loadingdone", 칩화살맞춤); }catch(_){}
document.addEventListener("scroll", e=>{ const t=e.target; if(t?.matches?.("#폰 .넣기칩 .칩줄")){ 칩자리={시트:U.시트, 칸:U.칸고름, 옆:t.scrollLeft}; 칩화살맞춤(); } }, true);
document.addEventListener("click", e=>{ const b=e.target.closest?.("#폰 .칩화살"); if(!b) return; const 줄=b.parentElement.querySelector(".칩줄");
  줄.scrollBy({left:(b.classList.contains("왼")?-1:1)*줄.clientWidth*0.7, behavior:움직임줄임()?"auto":"smooth"}); });
/* 휠 — 위아래 굴림을 옆으로 */
document.addEventListener("wheel", e=>{ const 줄=e.target.closest?.("#폰 .넣기칩")?.querySelector(".칩줄"); if(!줄 || 줄.scrollWidth<=줄.clientWidth+1) return;
  if(Math.abs(e.deltaY)<=Math.abs(e.deltaX)) return; e.preventDefault(); 줄.scrollLeft+=e.deltaY; }, {passive:false});
/* 꾹 눌러 끌기(마우스 · 펜) — 운띠 끌기와 같은 방식. 8px 넘게 움직이면 끌기, 뗄 때 생기는 클릭(칩 고르기)은 막음클릭으로. 터치는 브라우저가 스크롤 */
let 칩끌=null;
document.addEventListener("pointerdown", e=>{ if(e.pointerType==="touch"||e.button!==0) return;
  const 줄=e.target.closest?.("#폰 .넣기칩 .칩줄"); if(!줄) return; 칩끌={줄, x:e.clientX, 옆:줄.scrollLeft, 켜짐:false}; });
document.addEventListener("pointermove", e=>{ if(!칩끌) return; const dx=e.clientX-칩끌.x;
  if(!칩끌.켜짐){ if(Math.abs(dx)<=8) return; 칩끌.켜짐=true; }
  e.preventDefault(); 칩끌.줄.scrollLeft=칩끌.옆-dx; });
const 칩끌끝=()=>{ if(!칩끌) return; if(칩끌.켜짐){ 막음클릭=true; setTimeout(()=>{ 막음클릭=false; }); } 칩끌=null; };   // 클릭이 안 생겨도 다음 누름을 먹지 않게
document.addEventListener("pointerup", 칩끌끝); document.addEventListener("pointercancel", 칩끌끝);''')

# ── ⑤ 루틴 플랜 종목 줄 (점 '·' 없이 · 칸 사이 4 — 360 폭에서 이름이 덜 잘리게)
바꿈('''${플랜딱지}</span><span class="루플랜글 숫">· ${글}</span></div>${빼기}</div></div>`; }''',
     '''${플랜딱지}</span><span class="루플랜글 숫">${글}</span></div>${빼기}</div></div>`; }''')
# ── ⑤ = 이름[플랜] · N세트 · N회차 · 달성률 게이지 ──
바꿈('''  if(e.플랜id){ const p=플랜(e.플랜id), x=p&&플랜처방(p), 글=x?`${x.계.측정일?"측정 · ":""}${x.계.회}회차 ${처방글(x.목)}`:"목표 달성";''',
'''  /* v19 C ⑤ 플랜 줄 = 이름[플랜] · N세트 · N회차 · 달성률 게이지(막대 + %) — 이 순서만. 무게 × 횟수 · 측정 표시는 뺐다 */
  if(e.플랜id){ const p=플랜(e.플랜id), x=p&&플랜처방(p), fr=p?플랜달성률(p):0, 퍼=Math.round(fr*100);
    const 글=`${줄세트(e).length}세트</span><span class="루플랜글 숫">${x?`${x.계.회}회차`:"목표 달성"}</span><span class="게이지3 루플랜게" aria-hidden="true"><i style="width:${퍼}%"></i></span><span class="루플랜퍼 숫" aria-label="달성률 ${퍼}%">${퍼}%`;''')
바꿈('''function 플랜카드(p,i){''',
'''/* v19 C ⑤ 달성률 = 플랜카드 게이지(fr)와 같은 식 — (지금 − 시작) ÷ (목표 − 시작), 0~1 */
function 플랜달성률(p){ const 시=시작진행값(p), 목=목표진행값(p), 현=현재값(p); return Math.max(0,Math.min(1,(현-시)/((목-시)||1))); }
function 플랜카드(p,i){''')

css = '''
/* ═══ 10-04 v19 C — 종목 넣기 시트(칩줄 가로 스크롤 · 떠 있는 새 종목 단추 · 말풍선) · 루틴 플랜 줄 ═══ */
/* ④ 칩줄 한 줄 — 들어가면 같은 폭으로 채우고(바탕 0 · 글자 폭 아래로는 안 줄어듦) 넘치면 옆으로 민다 */
.넣기칩{position:relative}
.넣기칩 .칩줄{flex-wrap:nowrap;overflow-x:auto;scrollbar-width:none;overscroll-behavior-x:contain}
.넣기칩 .칩줄::-webkit-scrollbar{display:none}
.넣기칩 .칩{flex:1 0 0;min-width:max-content;padding:0 8px}
.칩화살{position:absolute;top:0;bottom:0;margin:auto 0;width:28px;height:28px;border-radius:50%;background:var(--면);border:1px solid var(--선);color:var(--글);display:flex;align-items:center;justify-content:center;z-index:1}
.칩화살.왼{left:0}.칩화살.오{right:0}
.칩화살[hidden]{display:none}
/* ② 오른쪽 아래 구석에 떠 있는 단추 — 목록 아래 여백 = 단추 32 + 틈 8 (마지막 칸을 가리지 않게) */
.시트:has(>.넣기목록){position:relative}
.시트>.넣기새{position:absolute;right:16px;bottom:16px;z-index:2;border-radius:16px;color:var(--강조글)}
.시트>.넣기목록{padding-bottom:calc(32px + 8px)}
/* ③ 말풍선 — 반투명 · 누름 통과 */
.넣기말{position:absolute;z-index:4;pointer-events:none;background:var(--흐림);color:var(--바탕);font-size:11px;line-height:16px;padding:4px 8px;border-radius:8px;white-space:nowrap;opacity:.85;animation:넣기말들 .15s ease-out both}
.넣기말.나감{opacity:0;transition:opacity .2s}
@keyframes 넣기말들{from{opacity:0}}
/* ⑤ 플랜 줄 — 이름은 줄어들며 … · 게이지는 남는 폭 (최소 16) */
.루플랜줄{gap:4px}
.루플랜줄 .이름플랜{flex:0 1 auto;margin-right:4px}   /* 이름 ↔ 세트 = 8 (다른 줄과 같게) · 나머지 칸 사이 4 */
.루플랜줄>.루플랜글{flex:none}
.루플랜줄>.루플랜게{display:block;flex:1 1 0;min-width:16px}   /* 이름이 먼저 — 좁은 폰에선 게이지가 줄어든다 */
.루플랜퍼{flex:none;font-size:11px;font-weight:700;color:var(--흐림)}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]
pathlib.Path(OUT).write_text(s, encoding='utf-8'); print("v19 C →", OUT)
