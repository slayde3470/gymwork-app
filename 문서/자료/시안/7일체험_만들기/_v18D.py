import asyncio, sys, json
from playwright.async_api import async_playwright
SP='/tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'
HTML=sys.argv[1] if len(sys.argv)>1 else SP+'7day-v18D.html'
J=lambda x: print(json.dumps(x, ensure_ascii=False))
async def main():
  async with async_playwright() as p:
    b=await p.chromium.launch(); pg=await b.new_page(viewport={'width':389,'height':860}); err=[]; pg.on('pageerror',lambda e:err.append(str(e)))
    await pg.goto('file://'+HTML); await pg.wait_for_timeout(600)
    await pg.evaluate("(()=>{ U.업적띠=null; U.시트=null; S.세션=null; U.탭='루틴'; U.루틴열림=S.루틴들.find(r=>!r.휴식일&&r.종목.length>=2).id; 그리기(); })()")
    await pg.wait_for_timeout(500)
    el=await pg.query_selector('#폰')
    # ① 접힘
    J({'①처음': await pg.evaluate("""(()=>{ const 상=[...document.querySelectorAll('.루넘김 .종목상자')]; return {상자:상.length, 접힘:상.filter(x=>x.classList.contains('루접힘')).length, 세트줄:document.querySelectorAll('.루세트').length,
      높이:상.map(x=>Math.round(x.getBoundingClientRect().height)), 머리글:상.map(x=>x.querySelector('.루머리').innerText.replace(/\\s+/g,' ').slice(0,30))}; })()""")})
    await el.screenshot(path=SP+'r18/D_루틴접힘.png')
    i0=await pg.evaluate("[...document.querySelectorAll('.루넘김 .종목상자')].findIndex(x=>x.querySelector('.루접줄'))")
    await pg.click(f'.루접줄[data-i="{i0}"]'); await pg.wait_for_timeout(350)
    J({'①한번누름': await pg.evaluate(f"""(()=>{{ const x=document.querySelectorAll('.루넘김 .종목상자')[{i0}]; return {{접힘:x.classList.contains('루접힘'), 세트줄:x.querySelectorAll('.루세트').length, aria:x.querySelector('.루접줄').getAttribute('aria-expanded'), 다른접힘:document.querySelectorAll('.루접힘').length}}; }})()""")})
    await el.screenshot(path=SP+'r18/D_루틴펼침.png')
    await pg.click(f'.루접줄[data-i="{i0}"]'); await pg.wait_for_timeout(350)
    J({'①두번누름 접힘': await pg.evaluate(f"document.querySelectorAll('.루넘김 .종목상자')[{i0}].classList.contains('루접힘')")})
    # 펼친 채 순서 바꾸기(배열 직접) → 펼침이 상자를 따라가는지
    await pg.click(f'.루접줄[data-i="{i0}"]'); await pg.wait_for_timeout(200)
    J({'①옮긴뒤': await pg.evaluate(f"""(()=>{{ const r=루틴(U.루틴열림), e=r.종목[{i0}]; r.종목.splice({i0},1); r.종목.push(e); 그리기(); const 상=[...document.querySelectorAll('.루넘김 .종목상자')]; return 상.map(x=>x.classList.contains('루접힘')?'접':'펼').join(''); }})()""")})
    n0=await pg.evaluate("루틴(U.루틴열림).종목.length")
    # ② ⑤ 시트 열기
    await pg.click('.루추가.위'); await pg.wait_for_timeout(500)
    def 띠(): return pg.evaluate("""(()=>{ const 폰=document.getElementById('폰').getBoundingClientRect(), 가=document.querySelector('.가림').getBoundingClientRect(), 시=document.querySelector('.시트').getBoundingClientRect(), 머=document.querySelector('.시트 .머리').getBoundingClientRect(), 목=document.querySelector('.넣기목록');
      return {폰위:폰.top, 폰높:폰.height, 가림:[가.top,가.bottom], 시트위:시.top, 시트아래:시.bottom, '위끝비':((시.top-폰.top)/폰.height).toFixed(3), 머리위:머.top, 칸수:목.querySelectorAll('.넣기칸').length, 목스크롤:[목.scrollHeight,목.clientHeight], 시트스크롤:document.querySelector('.시트').scrollHeight-document.querySelector('.시트').clientHeight}; })()""")
    a=await 띠(); J({'⑤전체':a})
    J({'②새종목단추': await pg.evaluate("""(()=>{ const b=document.querySelector('.넣기새'), c=document.querySelector('.넣기칩').getBoundingClientRect(), r=b.getBoundingClientRect(), l=document.querySelector('.넣기목록').getBoundingClientRect(); return {글:b.textContent, act:b.dataset.act, 높:r.height, 칩아래:c.bottom, 단추위:r.top, 단추아래:r.bottom, 목록위:l.top, 도움:document.querySelector('.넣기새줄 span').textContent, 글크기:getComputedStyle(b).fontSize}; })()""")})
    J({'④격자': await pg.evaluate("""(()=>{ const c=[...document.querySelectorAll('.넣기칸')].slice(0,4).map(x=>{const r=x.getBoundingClientRect(); return [x.querySelector('.넣기번').textContent, Math.round(r.left), Math.round(r.top), Math.round(r.width), Math.round(r.height)]});
      const x=document.querySelector('.넣기칸'); return {칸:c, 번호크기:getComputedStyle(x.querySelector('.넣기번')).fontSize, 번호색:getComputedStyle(x.querySelector('.넣기번')).color, 흐림:getComputedStyle(document.documentElement).getPropertyValue('--흐림'),
        이름크기:getComputedStyle(x.querySelector('b')).fontSize, 곁글:document.querySelectorAll('.넣기목록 .곁').length, 플랜딱지:document.querySelectorAll('.넣기칸 .플랜표').length,
        넘침:[...document.querySelectorAll('.넣기칸')].filter(x=>x.scrollWidth>x.clientWidth+1).map(x=>x.innerText.slice(0,12))}; })()""")})
    J({'③처음 들어감': await pg.evaluate("[...document.querySelectorAll('.넣기칸.들어감')].map(x=>x.querySelector('b').textContent+':'+x.querySelector('.넣기체크').textContent)")})
    # ③ 같은 칸 세 번 누르기
    t=await pg.evaluate("[...document.querySelectorAll('.넣기칸')].findIndex(x=>!x.classList.contains('들어감') && x.dataset.act==='종목넣기')")
    sel=f'.넣기칸 >> nth={t}'
    for k in range(3):
      await pg.locator('.넣기칸').nth(t).click(); await pg.wait_for_timeout(150)
      J({f'③누름{k+1}': await pg.evaluate(f"(()=>{{const x=document.querySelectorAll('.넣기칸')[{t}]; return [x.querySelector('b').textContent, x.querySelector('.넣기체크').textContent, x.classList.contains('들어감'), getComputedStyle(x).backgroundColor, 루틴(U.루틴열림).종목.length]}})()")})
    await pg.locator('.넣기칸').nth(t).click(); await pg.wait_for_timeout(150)
    J({'③4번': await pg.evaluate(f"document.querySelectorAll('.넣기칸')[{t}].querySelector('.넣기체크').textContent")})
    await el.screenshot(path=SP+'r18/D_넣기전체.png')
    # 꾹 누르기
    box=await pg.locator('.넣기칸').nth(t).bounding_box()
    await pg.mouse.move(box['x']+box['width']/2, box['y']+box['height']/2); await pg.mouse.down(); await pg.wait_for_timeout(600); await pg.mouse.up(); await pg.wait_for_timeout(200)
    J({'③꾹1': await pg.evaluate(f"[document.querySelectorAll('.넣기칸')[{t}].querySelector('.넣기체크').textContent, 루틴(U.루틴열림).종목.length]")})
    # 짧게 누르기는 더하기 (꾹 뒤 클릭 막음 확인 겸)
    await pg.locator('.넣기칸').nth(t).click(); await pg.wait_for_timeout(150)
    J({'③꾹뒤 짧게': await pg.evaluate(f"document.querySelectorAll('.넣기칸')[{t}].querySelector('.넣기체크').textContent")})
    for _ in range(4):
      await pg.mouse.move(box['x']+box['width']/2, box['y']+box['height']/2); await pg.mouse.down(); await pg.wait_for_timeout(550); await pg.mouse.up(); await pg.wait_for_timeout(150)
    J({'③꾹4번 → 0': await pg.evaluate(f"[document.querySelectorAll('.넣기칸')[{t}].querySelector('.넣기체크').textContent, document.querySelectorAll('.넣기칸')[{t}].classList.contains('들어감'), 루틴(U.루틴열림).종목.length, {n0}]")})
    # 꾹 누른 채 움직이면(스크롤) 안 뺌
    await pg.locator('.넣기칸').nth(t).click(); await pg.wait_for_timeout(150)
    await pg.mouse.move(box['x']+20, box['y']+20); await pg.mouse.down(); await pg.mouse.move(box['x']+20, box['y']+40, steps=3); await pg.wait_for_timeout(500); await pg.mouse.up(); await pg.wait_for_timeout(150)
    J({'③끌면 안뺌': await pg.evaluate(f"document.querySelectorAll('.넣기칸')[{t}].querySelector('.넣기체크').textContent")})
    # 플랜 칸
    pt=await pg.evaluate("[...document.querySelectorAll('.넣기칸')].findIndex(x=>x.dataset.act==='플랜넣기')")
    if pt>=0:
      c0=await pg.evaluate(f"document.querySelectorAll('.넣기칸')[{pt}].querySelector('.넣기체크').textContent")
      await pg.locator('.넣기칸').nth(pt).click(); await pg.wait_for_timeout(150)
      await pg.locator('.넣기칸').nth(pt).click(); await pg.wait_for_timeout(150)
      c2=await pg.evaluate(f"document.querySelectorAll('.넣기칸')[{pt}].querySelector('.넣기체크').textContent")
      pb=await pg.locator('.넣기칸').nth(pt).bounding_box()
      await pg.mouse.move(pb['x']+pb['width']/2, pb['y']+pb['height']/2); await pg.mouse.down(); await pg.wait_for_timeout(550); await pg.mouse.up(); await pg.wait_for_timeout(150)
      c3=await pg.evaluate(f"document.querySelectorAll('.넣기칸')[{pt}].querySelector('.넣기체크').textContent")
      J({'③플랜칸': [c0,c2,c3]})
    # ⑤ 스크롤 유지 · 칩 바꿔도 띠 자리
    await pg.evaluate("document.querySelector('.넣기목록').scrollTop=200")
    await pg.wait_for_timeout(100)
    vis=await pg.evaluate("(()=>{const l=document.querySelector('.넣기목록').getBoundingClientRect(); return [...document.querySelectorAll('.넣기칸')].findIndex(x=>x.getBoundingClientRect().top>l.top+4)})()")
    await pg.locator('.넣기칸').nth(vis).click(); await pg.wait_for_timeout(200)
    J({'⑤누른뒤 스크롤': await pg.evaluate("document.querySelector('.넣기목록').scrollTop")})
    await pg.locator('.넣기칩 .칩').nth(1).click(); await pg.wait_for_timeout(400)
    bb=await 띠(); J({'⑤칩2':{'칩':await pg.evaluate("document.querySelector('.넣기칩 .칩.켬, .넣기칩 [aria-pressed=true]')?.textContent||U.칸고름"),'시트위':bb['시트위'],'머리위':bb['머리위'],'칸수':bb['칸수'],'스크롤':await pg.evaluate("document.querySelector('.넣기목록').scrollTop")}})
    await el.screenshot(path=SP+'r18/D_넣기가슴.png')
    # 다크
    await pg.emulate_media(color_scheme='dark'); await pg.wait_for_timeout(200)
    await el.screenshot(path=SP+'r18/D_넣기다크.png'); await pg.emulate_media(color_scheme='light')
    # 운동 중 시트 — 시작한 종목은 꾹 눌러도 안 빠짐
    await pg.evaluate("U.시트=null; U.루틴열림=null; 운동시작(S.루틴들.find(r=>!r.휴식일&&r.종목.length>=2).id); const ss=S.세션; ss.종목[0].세트[0].완료=true; U.시트={종류:'종목넣기', 대상:'운동'}; U.칸고름='전체'; 그리기(); 1")
    await pg.wait_for_timeout(400)
    r=await pg.evaluate("""(()=>{ const ss=S.세션, 첫=ss.종목[0].이름; const x=[...document.querySelectorAll('.넣기칸')].find(c=>c.querySelector('b').textContent===첫); return {첫, i:[...document.querySelectorAll('.넣기칸')].indexOf(x), 체크:x?.querySelector('.넣기체크').textContent, n:ss.종목.length}; })()""")
    if r['i']>=0:
      await pg.locator('.넣기칸').nth(r['i']).scroll_into_view_if_needed(); bx=await pg.locator('.넣기칸').nth(r['i']).bounding_box()
      await pg.mouse.move(bx['x']+bx['width']/2, bx['y']+bx['height']/2); await pg.mouse.down(); await pg.wait_for_timeout(550); await pg.mouse.up(); await pg.wait_for_timeout(150)
      r2=await pg.evaluate("[S.세션.종목.length, document.querySelector('.토스트')?.textContent||'']")
      await pg.locator('.넣기칸').nth(r['i']).click(); await pg.wait_for_timeout(150)
      await pg.mouse.move(bx['x']+bx['width']/2, bx['y']+bx['height']/2); await pg.mouse.down(); await pg.wait_for_timeout(550); await pg.mouse.up(); await pg.wait_for_timeout(150)
      J({'③운동중': {'전':r, '시작한것 꾹':r2, '하나더→꾹':await pg.evaluate("[S.세션.종목.length, S.세션.지금.i, U.본]")}})
    print('err', err)
    await b.close()
asyncio.run(main())
