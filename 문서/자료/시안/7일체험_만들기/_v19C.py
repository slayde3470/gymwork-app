import asyncio, sys, json
from playwright.async_api import async_playwright
SP='/tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'
HTML=sys.argv[1] if len(sys.argv)>1 else SP+'7day-v19C.html'
J=lambda x: print(json.dumps(x, ensure_ascii=False))
async def main():
  async with async_playwright() as p:
    b=await p.chromium.launch(); pg=await b.new_page(viewport={'width':389,'height':860}); err=[]; pg.on('pageerror',lambda e:err.append(str(e)))
    await pg.goto('file://'+HTML); await pg.wait_for_timeout(600)
    # 플랜이 든 루틴을 연다(없으면 첫 플랜을 넣는다)
    await pg.evaluate("""(()=>{ U.업적띠=null; U.시트=null; S.세션=null; U.탭='루틴';
      let r=S.루틴들.find(r=>!r.휴식일&&r.종목.some(e=>e.플랜id)); if(!r){ r=S.루틴들.find(r=>!r.휴식일&&r.종목.length>=2); const p=S.플랜들[0]; if(p){ const e={이름:p.이름, 플랜id:p.id, 세트:[]}; e.세트=줄세트(e); r.종목.push(e);} }
      U.루틴열림=r.id; 그리기(); })()""")
    await pg.wait_for_timeout(500)
    el=await pg.query_selector('#폰')
    # ⑤ 플랜 줄
    J({'⑤플랜줄': await pg.evaluate("""(()=>[...document.querySelectorAll('.루플랜줄')].map(x=>{ const i=+x.closest('.종목상자').dataset.i, e=루틴(U.루틴열림).종목[i], p=플랜(e.플랜id);
      const 카=Math.round(Math.max(0,Math.min(1,(현재값(p)-시작진행값(p))/((목표진행값(p)-시작진행값(p))||1)))*100);
      const 게=x.querySelector('.루플랜게'), r=게.getBoundingClientRect(), xr=x.getBoundingClientRect();
      return {글:x.innerText.replace(/\\s+/g,' '), 순서:[...x.children].map(c=>c.className), 세트:줄세트(e).length, 회:플랜처방(p)?.계.회, 카드식:카, 게이지폭:Math.round(r.width), 채움:게.querySelector('i').style.width, 줄넘침:x.scrollWidth>x.clientWidth+1, 오른끝:[Math.round(r.right), Math.round(xr.right)]}; }))()""")})
    await el.screenshot(path=SP+'r19/C_루틴플랜줄.png')
    # ① 칩 = 전체 / 다른 칩 고르고 닫고 다시 열기
    await pg.click('.루추가.위'); await pg.wait_for_timeout(500)
    a=await pg.evaluate("U.칸고름")
    await pg.locator('.넣기칩 .칩').nth(2).click(); await pg.wait_for_timeout(200)
    b2=await pg.evaluate("U.칸고름")
    await pg.evaluate("U.시트=null; 그리기()"); await pg.wait_for_timeout(200)
    await pg.click('.루추가.위'); await pg.wait_for_timeout(500)
    J({'①': {'처음':a, '고름':b2, '다시열기':await pg.evaluate("U.칸고름"), '켬칩':await pg.evaluate("document.querySelector('.넣기칩 .칩.켬').textContent")}})
    # ② 떠 있는 새 종목 단추
    J({'②': await pg.evaluate("""(()=>{ const 시=document.querySelector('.시트').getBoundingClientRect(), b=document.querySelector('.시트>.넣기새'), r=b.getBoundingClientRect(), l=document.querySelector('.넣기목록');
      l.scrollTop=l.scrollHeight; const 칸=[...l.querySelectorAll('.넣기칸')], 끝=칸[칸.length-1].getBoundingClientRect(), 끝2=칸[칸.length-2]?.getBoundingClientRect();
      return {글:b.textContent, act:b.dataset.act, 높:r.height, 폭:Math.round(r.width), 오른틈:Math.round(시.right-r.right), 아래틈:Math.round(시.bottom-r.bottom), 글크기:getComputedStyle(b).fontSize,
        맨끝칸아래:Math.round(Math.max(끝.bottom,끝2?.bottom||0)), 단추위:Math.round(r.top), 안가림:Math.max(끝.bottom,끝2?.bottom||0)<=r.top, 꾹설명남음:document.querySelector('.시트').innerText.includes('꾹 누르면 하나 빼기'), 새줄:!!document.querySelector('.넣기새줄')}; })()""")})
    await pg.wait_for_timeout(150)
    await el.screenshot(path=SP+'r19/C_넣기_끝까지.png')
    await pg.evaluate("document.querySelector('.넣기목록').scrollTop=0"); await pg.wait_for_timeout(150)
    # ③ 안 들어간 칸 누름 → 넣기 + 말풍선
    t=await pg.evaluate("[...document.querySelectorAll('.넣기칸')].findIndex((x,i)=>i>=2 && !x.classList.contains('들어감') && x.dataset.act==='종목넣기')")
    n0=await pg.evaluate("루틴(U.루틴열림).종목.length")
    체=lambda: pg.evaluate(f"(()=>{{const x=document.querySelectorAll('.넣기칸')[{t}]; return [x.querySelector('b').textContent, x.querySelector('.넣기체크').textContent, 루틴(U.루틴열림).종목.length]}})()")
    await pg.locator('.넣기칸').nth(t).click(); await pg.wait_for_timeout(250)
    J({'③누름(넣기)': await 체(), '말풍선': await pg.evaluate(f"""(()=>{{ const m=document.querySelector('.넣기말'); if(!m) return null; const r=m.getBoundingClientRect(), c=document.querySelectorAll('.넣기칸')[{t}].getBoundingClientRect(), cs=getComputedStyle(m);
      return {{글:m.textContent, 위:Math.round(r.top), 아래:Math.round(r.bottom), 칸위:Math.round(c.top), 가운데차:Math.round((r.left+r.right)/2-(c.left+c.right)/2), 좌우:[Math.round(r.left),Math.round(r.right)], pe:cs.pointerEvents, 투명:cs.opacity, 글크기:cs.fontSize}}; }})()""")})
    await el.screenshot(path=SP+'r19/C_말풍선.png')
    # 말풍선 아래 칸도 눌린다(누름 통과)
    under=await pg.evaluate("(()=>{ const m=document.querySelector('.넣기말').getBoundingClientRect(); const e=document.elementFromPoint((m.left+m.right)/2,(m.top+m.bottom)/2); return e?.closest('.넣기칸,.칩,.머리')?.className||e?.className; })()")
    await pg.wait_for_timeout(1900)
    J({'말풍선 아래 요소':under, '1.9초 뒤 말풍선': await pg.evaluate("!!document.querySelector('.넣기말')")})
    # 들어간 칸 누름 → 빼기
    await pg.locator('.넣기칸').nth(t).click(); await pg.wait_for_timeout(250)
    J({'③들어간칸 누름(빼기)': await 체(), '빼고 말풍선': await pg.evaluate("!!document.querySelector('.넣기말')")})
    # 꾹 → 하나 더 (0 → 1 → 2)
    async def 꾹(i):
      bx=await pg.locator('.넣기칸').nth(i).bounding_box()
      await pg.mouse.move(bx['x']+bx['width']/2, bx['y']+bx['height']/2); await pg.mouse.down(); await pg.wait_for_timeout(550); await pg.mouse.up(); await pg.wait_for_timeout(200)
    await 꾹(t); c1=await 체(); m1=await pg.evaluate("!!document.querySelector('.넣기말')")
    await 꾹(t); c2=await 체()
    J({'③꾹1':c1, '꾹 말풍선':m1, '③꾹2':c2})
    await pg.locator('.넣기칸').nth(t).click(); await pg.wait_for_timeout(200)
    J({'③✓✓ 누름 → 하나 빼기': await 체()})
    await pg.locator('.넣기칸').nth(t).click(); await pg.wait_for_timeout(200)
    J({'③다시 누름 → 0': await 체(), '처음 개수':n0})
    # 플랜 칸
    pt=await pg.evaluate("[...document.querySelectorAll('.넣기칸')].findIndex(x=>x.dataset.act==='플랜넣기')")
    if pt>=0:
      pc=lambda: pg.evaluate(f"document.querySelectorAll('.넣기칸')[{pt}].querySelector('.넣기체크').textContent")
      r0=await pc(); await pg.locator('.넣기칸').nth(pt).click(); await pg.wait_for_timeout(200); r1=await pc()
      await 꾹(pt); r2=await pc(); await pg.locator('.넣기칸').nth(pt).click(); await pg.wait_for_timeout(200); r3=await pc()
      J({'③플랜칸 [처음, 누름, 꾹, 누름]':[r0,r1,r2,r3]})
    J({'aria': await pg.evaluate(f"document.querySelectorAll('.넣기칸')[{t}].getAttribute('aria-label')")})
    # ④ 칩 늘리기
    await pg.evaluate("S.카테고리.push('코어','유산소','스트레칭','전신','기타'); U.시트=null; 그리기()"); await pg.wait_for_timeout(200)
    await pg.click('.루추가.위'); await pg.wait_for_timeout(500)
    st=lambda: pg.evaluate("""(()=>{ const 줄=document.querySelector('.넣기칩 .칩줄'), 왼=document.querySelector('.칩화살.왼'), 오=document.querySelector('.칩화살.오'), z=줄.getBoundingClientRect();
      return {줄수:new Set([...줄.children].map(c=>Math.round(c.getBoundingClientRect().top))).size, 칩:줄.children.length, 옆:Math.round(줄.scrollLeft), 폭:줄.clientWidth, 전체폭:줄.scrollWidth, 왼:!왼.hidden, 오:!오.hidden,
        오자리:오.hidden?null:[Math.round(오.getBoundingClientRect().right-z.right), Math.round(오.getBoundingClientRect().width), Math.round(오.getBoundingClientRect().height)], 스크롤바:줄.offsetHeight-줄.clientHeight}; })()""")
    J({'④처음':await st()})
    await el.screenshot(path=SP+'r19/C_칩줄_처음.png')
    await pg.click('.칩화살.오'); await pg.wait_for_timeout(700)
    J({'④› 누름':await st()})
    await el.screenshot(path=SP+'r19/C_칩줄_옮김.png')
    await pg.click('.칩화살.오'); await pg.wait_for_timeout(700); await pg.click('.칩화살.오') if await pg.evaluate("!document.querySelector('.칩화살.오').hidden") else None; await pg.wait_for_timeout(700)
    J({'④끝까지':await st()})
    await pg.click('.칩화살.왼'); await pg.wait_for_timeout(700)
    J({'④‹ 누름':await st()})
    # 칩 누름 → 다시 그려도 자리 그대로
    before=await st()
    vis=await pg.evaluate("(()=>{const z=document.querySelector('.넣기칩 .칩줄').getBoundingClientRect(); return [...document.querySelectorAll('.넣기칩 .칩')].findIndex(c=>{const r=c.getBoundingClientRect(); return r.left>z.left+40 && r.right<z.right-40;})})()")
    await pg.locator('.넣기칩 .칩').nth(vis).click(); await pg.wait_for_timeout(300)
    J({'④칩 누름 뒤':{'고름':await pg.evaluate("U.칸고름"), '전옆':before['옆'], '뒤':await st()}})
    # 휠
    await pg.evaluate("document.querySelector('.넣기칩 .칩줄').scrollLeft=0"); await pg.wait_for_timeout(100)
    z=await pg.locator('.넣기칩 .칩줄').bounding_box()
    await pg.mouse.move(z['x']+z['width']/2, z['y']+z['height']/2); await pg.mouse.wheel(0,120); await pg.wait_for_timeout(300)
    J({'④휠 120':await st()})
    # 마우스 끌기 (칩 위에서 시작 → 칩 고르기 안 됨)
    k0=await pg.evaluate("U.칸고름"); await pg.evaluate("document.querySelector('.넣기칩 .칩줄').scrollLeft=0"); await pg.wait_for_timeout(100)
    await pg.mouse.move(z['x']+z['width']/2, z['y']+z['height']/2); await pg.mouse.down(); await pg.mouse.move(z['x']+z['width']/2-100, z['y']+z['height']/2, steps=6); await pg.mouse.up(); await pg.wait_for_timeout(200)
    J({'④끌기 -100':{**(await st()), '칩바뀜':(await pg.evaluate("U.칸고름"))!=k0, '막음클릭남음':await pg.evaluate("막음클릭")}})
    await pg.locator('.넣기칸').nth(0).click(); await pg.wait_for_timeout(200)
    J({'끌기 뒤 다음 누름 먹힘?': await pg.evaluate("document.querySelectorAll('.넣기칸')[0].querySelector('.넣기체크').textContent")})
    await pg.emulate_media(color_scheme='dark'); await pg.wait_for_timeout(200)
    await el.screenshot(path=SP+'r19/C_칩줄_다크.png'); await pg.emulate_media(color_scheme='light')
    # 운동 중 — 시작한 종목은 눌러도 안 빠짐 · 꾹은 하나 더
    await pg.evaluate("U.시트=null; U.루틴열림=null; 운동시작(S.루틴들.find(r=>!r.휴식일&&r.종목.length>=2).id); const ss=S.세션; ss.종목[0].세트[0].완료=true; U.시트={종류:'종목넣기', 대상:'운동'}; U.칸고름='전체'; 그리기(); 1")
    await pg.wait_for_timeout(400)
    r=await pg.evaluate("""(()=>{ const ss=S.세션, 첫=ss.종목[0]; const 칸=[...document.querySelectorAll('.넣기칸')]; const i=칸.findIndex(c=>첫.플랜id?c.dataset.v===첫.플랜id:c.dataset.v===첫.이름); return {첫:첫.이름, i, n:ss.종목.length}; })()""")
    if r['i']>=0:
      await pg.locator('.넣기칸').nth(r['i']).scroll_into_view_if_needed(); await pg.locator('.넣기칸').nth(r['i']).click(); await pg.wait_for_timeout(200)
      a1=await pg.evaluate("[S.세션.종목.length, document.querySelector('.토스트')?.textContent||'']")
      await 꾹(r['i']); a2=await pg.evaluate("S.세션.종목.length")
      await pg.locator('.넣기칸').nth(r['i']).click(); await pg.wait_for_timeout(200); a3=await pg.evaluate("S.세션.종목.length")
      J({'③운동중':{'전':r, '시작한것 누름':a1, '꾹':a2, '다시 누름(새로 넣은 것 빠짐)':a3}})
    print('err', err)
    await b.close()
asyncio.run(main())
