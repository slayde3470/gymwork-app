"""v14 (10-04 홍겸 님) — 운동 화면 아래 종목 칸
① 꾹 눌러 좌우로 끌면 운동 순서가 바뀐다 (다른 목록과 같은 '꾹 눌러 끌기' · D3-9)
② [＋] 칸은 순서와 상관없이 늘 오른쪽 끝에 붙박이(sticky)
③ 보고 있는 종목 칸이 줄 맨 앞(왼쪽)에 오게. 줄을 좌우로 밀어 봤더라도 이 종목의 세트 체크 · 휴식 · 무게 · 횟수 · 휴식 시간을
   만지면 다시 맨 앞으로"""
import sys, pathlib
IN, OUT = sys.argv[1], sys.argv[2]
s = pathlib.Path(IN).read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:70]!r}: {c}번")
    s = s.replace(old, new)

# ① 칸에 끌기 표시
바꿈('''<button class="운칸 ${i===본?"지금":""} ${다끝&&i!==본?"끝":""}" data-act="본종목" data-i="${i}"''',
     '''<button class="운칸 ${i===본?"지금":""} ${다끝&&i!==본?"끝":""}" data-act="본종목" data-i="${i}" data-drag="운칸" data-drop="운칸"''')
# 가로 줄이면 왼쪽/오른쪽 반으로 가른다
바꿈('''  const r=칸.getBoundingClientRect(), 아래=e.clientY>r.top+r.height/2; 끌.대상={i:+칸.dataset.i, 아래}; 칸.classList.add(아래?"선아래":"선위");''',
     '''  const r=칸.getBoundingClientRect(), 가로=끌.종류==="운칸", 아래=가로?e.clientX>r.left+r.width/2:e.clientY>r.top+r.height/2; 끌.대상={i:+칸.dataset.i, 아래}; 칸.classList.add(아래?"선아래":"선위");''')
# 끌다가 줄 끝에 닿으면 줄이 따라 밀린다
바꿈('''  const 밑=document.elementFromPoint(e.clientX,e.clientY);
  if(끌.종류==="날"){''', '''  const 밑=document.elementFromPoint(e.clientX,e.clientY);
  if(끌.종류==="운칸"){ const 줄=끌.el.closest(".운띠"), r=줄?.getBoundingClientRect(); if(r){ if(e.clientX<r.left+32) 줄.scrollLeft-=10; else if(e.clientX>r.right-76) 줄.scrollLeft+=10; } }
  if(끌.종류==="날"){''')
바꿈('''  else if(x.종류==="플랜"){ 옮김(S.플랜들,x.원,x.대상); }''', '''  else if(x.종류==="플랜"){ 옮김(S.플랜들,x.원,x.대상); }
  else if(x.종류==="운칸"){ const ss=S.세션; if(ss){ const 전=ss.종목.slice(), 본e=전[U.본], 지e=전[ss.지금.i], 휴e=ss.휴식?전[ss.휴식.i]:null, 접=ss.접기;
      if(옮김(ss.종목,x.원,x.대상)){ const 새=e=>ss.종목.indexOf(e);
        if(본e) U.본=새(본e); if(지e) ss.지금.i=새(지e); if(휴e) ss.휴식.i=새(휴e);
        if(접){ const n={}; 전.forEach((e,j)=>{ if(접[j]!=null) n[새(e)]=접[j]; }); ss.접기=n; }
        운자리.당김=true; 발자취("운동 순서 바꿈"); } } }''')
# ② [＋] 붙박이 — 끌기 대상 아님(이미 data-drag 없음)
# ③ 맨 앞으로: 이 종목 화면을 만지는 행동이면 표시
바꿈('''  if(a!=="루틴지움"&&a!=="플랜지움"&&a!=="처음부터"&&a!=="사진지움") U.확인=null;''',
     '''  if(a!=="루틴지움"&&a!=="플랜지움"&&a!=="처음부터"&&a!=="사진지움") U.확인=null;
  if(["체크","세트값","휴식건너뛰기","세트지움","세트더","세트빼기","본종목"].includes(a)) 운자리.당김=true;   // 10-04 보는 종목 칸을 다시 맨 앞으로''')
바꿈('''  else if(w==="세트값"){ const s=S.세션.종목[+d.i].세트[+d.k];''', '''  else if(w==="세트값"){ 운자리.당김=true; const s=S.세션.종목[+d.i].세트[+d.k];''')
바꿈('''  if(칸 && !같은종목){ const 끝=줄.scrollWidth-줄.clientWidth, 목표=Math.max(0,Math.min(끝, 칸.offsetLeft+칸.offsetWidth/2-줄.clientWidth/2));''',
     '''  const 당김=운자리.당김; 운자리.당김=false;
  if(칸 && (!같은종목 || 당김)){ const 첫=줄.querySelector(".운칸"), 끝=줄.scrollWidth-줄.clientWidth, 목표=Math.max(0,Math.min(끝, 칸.offsetLeft-(첫?첫.offsetLeft:0)));   // 10-04 보는 칸을 맨 앞(왼쪽)에''')
css = '''
/* ═══ 10-04 v14 — 종목 칸 꾹 눌러 좌우 끌기 · [＋] 오른쪽 붙박이 ═══ */
.운띠 .운칸[data-drag]{touch-action:pan-x}
.운칸.선위{box-shadow:inset 3px 0 0 var(--강조)}
.운칸.선아래{box-shadow:inset -3px 0 0 var(--강조)}
.운칸.끌림{opacity:.5}
.운띠 .운칸.운더{position:sticky;right:0;flex:0 0 44px;z-index:1;background:var(--면);box-shadow:-4px 0 0 var(--면2)}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]
pathlib.Path(OUT).write_text(s, encoding='utf-8'); print("v14 →", OUT)
