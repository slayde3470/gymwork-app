"""v22 C 확인 (10-06) — ⑫ 쉼 게이지 글(남은 시간 · 건너뛰기) 잘림 · ⑬ '+ 세트' 누르면 단추가 보이게 한 번 자동 스크롤
쓰는 법: python3 _v22C.py HTML 사진폴더   (389×860 · 360×780 · 사진 → 사진폴더/*.png)
처음 화면(캘린더)에서 [운동 시작] → 1세트 체크(쉬는 중) → 잰다 → '+ 세트' 를 여러 번 누르며 잰다.
웹 글꼴(IBM Plex Sans KR)은 이 컨테이너에서 못 받아 대체 글꼴로 잰다 — 폰과 글자 폭 · 높이가 조금 다를 수 있다"""
import asyncio, sys, json, os
from playwright.async_api import async_playwright
HTML, 폴더 = os.path.abspath(sys.argv[1]), sys.argv[2]
os.makedirs(폴더, exist_ok=True)
결과 = {'통과': 0, '실패': []}
def 봄(이름, 조건, 값=None):
  if 조건: 결과['통과'] += 1
  else: 결과['실패'].append(f"{이름} → {값}")
  print(('  ✓ ' if 조건 else '  ✗ ') + 이름 + ('' if 값 is None else f"  {json.dumps(값, ensure_ascii=False)[:300]}"))

# 쉼 게이지 재기 — 글 상자 · 글자 잉크(Range 사각형)가 게이지 안쪽(테두리 1 안)에 들어오는지, small 이 넘치지 않는지
잼 = """()=>{ const g=document.querySelector('#폰 .운세트들 .쉼게이지'); if(!g) return null;
  const G=g.getBoundingClientRect(), 안={t:G.top+1,b:G.bottom-1,l:G.left+1,r:G.right-1}, o={게이지:[+G.width.toFixed(1),+G.height.toFixed(1)], 줄:[]};
  for(const 층 of ['.밑','.위']){ const L=g.querySelector(층); const b=L.querySelector('b'), s=L.querySelector('small');
    for(const el of [b,s]){ const cs=getComputedStyle(el), R=el.getBoundingClientRect(), rg=document.createRange(); rg.selectNodeContents(el); const I=rg.getBoundingClientRect();
      o.줄.push({층, 글:el.textContent, 크기:cs.fontSize, 자간:cs.letterSpacing, 줄높이:cs.lineHeight, 상자:[+R.top.toFixed(1),+R.bottom.toFixed(1),+R.left.toFixed(1),+R.right.toFixed(1)],
        글줄:[+I.top.toFixed(1),+I.bottom.toFixed(1),+I.left.toFixed(1),+I.right.toFixed(1)], 넘침가로:el.scrollWidth-el.clientWidth, 넘침세로:el.scrollHeight-el.clientHeight,
        안에:I.top>=안.t-0.5&&I.bottom<=안.b+0.5&&I.left>=안.l-0.5&&I.right<=안.r+0.5 }); } }
  // 숫자 ↔ 글 사이: 숫자 바탕선 ~ 글 바탕선, 숫자 글줄 아래 ~ 글 글줄 위
  const L=g.querySelector('.밑'), b=L.querySelector('b'), s=L.querySelector('small');
  const 바탕=el=>{ const z=document.createElement('span'); z.style.cssText='display:inline-block;width:0;height:0;vertical-align:baseline'; el.appendChild(z); const y=z.getBoundingClientRect().top; z.remove(); return y; };
  const cv=document.createElement('canvas').getContext('2d'), 잉크=(el)=>{ const cs=getComputedStyle(el); const fs=parseFloat(cs.fontSize); cv.font=`${cs.fontWeight} 1000px ${cs.fontFamily}`; const m=cv.measureText(el.textContent); return {위:m.actualBoundingBoxAscent/1000*fs, 아래:m.actualBoundingBoxDescent/1000*fs}; };   // 1000px 로 재서 줄인다(작은 크기는 정수로 반올림돼서)
  const bb=바탕(b), sb=바탕(s), bi=잉크(b), si=잉크(s);
  o.바탕선거리=+(sb-bb).toFixed(2); o.잉크틈=+((sb-si.위)-(bb+bi.아래)).toFixed(2);
  o.잉크위=+(bb-bi.위-G.top).toFixed(2); o.잉크아래=+(G.bottom-(sb+si.아래)).toFixed(2);   // 게이지 위끝 ~ 숫자 잉크 위 · 글 잉크 아래 ~ 게이지 아래끝
  o.게이지넘침=[g.scrollHeight-g.clientHeight, g.scrollWidth-g.clientWidth];
  return o; }"""

더잼 = """()=>{ const 목=document.querySelector('#폰 .운세트들'), b=목.querySelector('[data-act="세트더"]'); const M=목.getBoundingClientRect(), B=b.getBoundingClientRect();
  return {위:+목.scrollTop.toFixed(1), 끝:목.scrollHeight-목.clientHeight, 단추아래:+(B.bottom-M.top).toFixed(1), 칸높이:목.clientHeight, 보임:B.bottom<=M.bottom+0.5&&B.top>=M.top-0.5, 세트:목.querySelectorAll('.세트줄').length}; }"""

# 사진 찍기 전 — 들어옴 같은 끝이 있는 움직임은 끝내고, 점멸(무한)만 '켜진 쪽'(0)에 멈춘다 (_v21.py 와 같은 방식)
멈춤 = """()=>document.getAnimations().forEach(a=>{ try{ if(isFinite(a.effect.getComputedTiming().endTime)){ a.finish(); return; } a.pause(); a.currentTime=(a.effect.getTiming().delay||0); }catch(_){} })"""

# '+ 세트' 를 누르고 같은 JS 안에서 바로 잰다 = 다시 그린 뒤 · 자동 스크롤(마이크로태스크) 전 자리 → '넘쳤나' 판정 기준
누르고잼 = """()=>{ document.querySelector('#폰 .운세트들 [data-act="세트더"]').click(); return (""" + 더잼 + """)(); }"""

# 스크롤 호출을 센다 (한 번 누를 때 한 번만 움직여야)
훅 = """()=>{ if(window.__훅) return; window.__훅=1; window.__스크롤=[]; const 원=Element.prototype.scrollTo;
  Element.prototype.scrollTo=function(...a){ if(this.classList?.contains('운세트들')) __스크롤.push(JSON.stringify(a[0])); return 원.apply(this,a); }; }"""

async def 한폭(b, W, H):
  print(f'\n════ {W}×{H} ════')
  pg = await b.new_page(viewport={'width': W, 'height': H}); err = []; pg.on('pageerror', lambda e: err.append(str(e)))
  await pg.goto('file://' + HTML); await pg.wait_for_timeout(800)
  await pg.click('#폰 [data-act="시작"]'); await pg.wait_for_timeout(500)
  await pg.evaluate(훅)
  await pg.click('#폰 .운세트들 [data-act="체크"][data-k="0"]'); await pg.wait_for_timeout(700)
  r = await pg.evaluate(잼)
  봄('쉬는 중 게이지가 있다', r is not None, None)
  if r:
    print('    ', json.dumps({k: r[k] for k in ['게이지','바탕선거리','잉크틈','잉크위','잉크아래','게이지넘침']}, ensure_ascii=False))
    for z in r['줄'][:2]: print('    ', json.dumps({k: z[k] for k in ['글','크기','자간','줄높이','상자','글줄','넘침가로','넘침세로']}, ensure_ascii=False))
    봄('게이지 세로 · 가로 안 넘침', r['게이지넘침'] == [0, 0], r['게이지넘침'])
    봄('숫자 · 건너뛰기 글줄이 게이지 안쪽에', all(z['안에'] for z in r['줄']), [(z['층'], z['글'], z['글줄']) for z in r['줄'] if not z['안에']])
    봄('글 상자가 글을 자르지 않음(scroll = client)', all(z['넘침가로'] <= 0 and z['넘침세로'] <= 0 for z in r['줄']), [(z['글'], z['넘침가로'], z['넘침세로']) for z in r['줄']])
    봄('숫자 잉크 위 · 글 잉크 아래 여유 ≥ 1px', r['잉크위'] >= 1 and r['잉크아래'] >= 1, [r['잉크위'], r['잉크아래']])
    봄('숫자와 글 잉크가 겹치지 않음', r['잉크틈'] > 0, r['잉크틈'])
  await pg.evaluate(멈춤)
  await (await pg.query_selector('#폰')).screenshot(path=f'{폴더}/{W}_쉼.png')
  g = await pg.query_selector('#폰 .운세트들 .쉼게이지')
  if g: await g.screenshot(path=f'{폴더}/{W}_쉼게이지.png')
  
  # ── ⑬ '+ 세트' ──
  움직임 = []
  for n in range(8):
    앞 = await pg.evaluate(더잼)
    await pg.evaluate("__스크롤.length=0")
    직후 = await pg.evaluate(누르고잼)       # 다시 그린 직후 · 자동 스크롤 전
    await pg.wait_for_timeout(900)
    뒤 = await pg.evaluate(더잼); 횟수 = await pg.evaluate("__스크롤.length")
    넘쳤나 = not 직후['보임']
    움직임.append((n, 앞['세트'], 뒤['세트'], 앞['위'], 뒤['위'], 뒤['보임'], 횟수))
    print(f"    +세트 {n+1}: 세트 {앞['세트']}→{뒤['세트']} · 위 {앞['위']}→{뒤['위']} (끝 {뒤['끝']}) · 단추보임 {뒤['보임']} · scrollTo {횟수}번")
    봄(f'+세트 {n+1} 뒤 단추가 보인다', 뒤['보임'], 뒤)
    봄(f'+세트 {n+1} scrollTo 한 번 이하', 횟수 <= 1, 횟수)
    if not 넘쳤나: 봄(f'+세트 {n+1} 단추가 칸 안이면 안 움직임', 뒤['위'] == 앞['위'], [앞['위'], 뒤['위']])
    if 넘쳤나: 봄(f'+세트 {n+1} 넘쳤으면 움직였다(부드럽게 · 한 번)', 뒤['위'] > 앞['위'] and 횟수 == 1, [앞['위'], 뒤['위'], 횟수])
    if n == 0 and 뒤['위'] == 0: await (await pg.query_selector('#폰')).screenshot(path=f'{폴더}/{W}_세트더_처음.png')
  봄('여러 번 누르는 동안 한 번이라도 넘쳐서 움직였다(시험이 그 경우까지 갔는지)', any(m[4] > m[3] for m in 움직임), 움직임)
  await pg.evaluate(멈춤)
  await (await pg.query_selector('#폰')).screenshot(path=f'{폴더}/{W}_세트더_끝.png')
  # 안 넘치는 경우 — 그림 칸을 감추고(✕ 와 같은 배너숨김) 세트를 2개로 줄여 목록이 짧을 때: '+ 세트' 를 눌러도 단추가 칸 안이면 움직이지 않는다
  await pg.evaluate("()=>{ const ss=S.세션, e=ss.종목[U.본??ss.지금.i]; e.세트.length=2; ss.배너숨김=true; 그리기(); }"); await pg.wait_for_timeout(400)
  for n in range(3):
    await pg.evaluate("__스크롤.length=0"); 앞 = await pg.evaluate(더잼)
    직후 = await pg.evaluate(누르고잼); await pg.wait_for_timeout(900)
    뒤 = await pg.evaluate(더잼); 횟수 = await pg.evaluate("__스크롤.length")
    print(f"    짧은 목록 +세트 {n+1}: 세트 {앞['세트']}→{뒤['세트']} · 위 {앞['위']}→{뒤['위']} · 누른 직후 단추아래 {직후['단추아래']}/{직후['칸높이']}(넘침 {not 직후['보임']}) · scrollTo {횟수}번")
    if 직후['보임']: 봄(f'짧은 목록 +세트 {n+1} — 단추가 칸 안 → 그대로(scrollTo 0)', 횟수 == 0 and 뒤['위'] == 앞['위'] and 뒤['보임'], [앞['위'], 뒤['위'], 횟수])
    else: 봄(f'짧은 목록 +세트 {n+1} — 넘침 → 한 번 내려 단추가 보임', 횟수 == 1 and 뒤['보임'], [앞['위'], 뒤['위'], 횟수])
  await pg.evaluate(멈춤); await (await pg.query_selector('#폰')).screenshot(path=f'{폴더}/{W}_짧은목록.png')
  봄('pageerror 없음', not err, err)
  await pg.close()

async def main():
  async with async_playwright() as p:
    b = await p.chromium.launch()
    for W, H in [(389, 860), (360, 780)]: await 한폭(b, W, H)
    await b.close()
  print(f"\n통과 {결과['통과']} · 실패 {len(결과['실패'])}")
  for x in 결과['실패']: print('  ✗', x)
asyncio.run(main())
