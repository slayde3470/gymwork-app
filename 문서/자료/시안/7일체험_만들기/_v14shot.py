import asyncio, re
from playwright.async_api import async_playwright
SP='/tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'
상태=__import__('json').load(open(SP+'marks3/28f858e336f21768f689d786826649de.json'))
async def main():
  async with async_playwright() as p:
    b=await p.chromium.launch(); pg=await b.new_page(viewport={'width':389,'height':860}); err=[]; pg.on('pageerror',lambda e:err.append(str(e)))
    await pg.goto('file://'+SP+'7day-v14.html'); await pg.wait_for_timeout(600)
    await pg.evaluate("s=>{ S=JSON.parse(JSON.stringify(s.S)); Object.assign(U, s.U, {시트:null, 업적띠:null}); U.본=0; 그리기(); }", 상태); await pg.wait_for_timeout(300)
    이름=lambda: pg.evaluate("S.세션.종목.map(e=>e.이름).slice(0,4)")
    print('처음', await 이름(), await pg.evaluate("document.querySelector('.운띠').scrollLeft"))
    # 셋째 칸 보기 → 맨 앞?
    await pg.evaluate("행동('본종목',{i:'3'})"); await pg.wait_for_timeout(500)
    r=await pg.evaluate("(()=>{const 줄=document.querySelector('.운띠'), 칸=줄.querySelector('.운칸.지금'); return [Math.round(칸.getBoundingClientRect().left-줄.getBoundingClientRect().left), 줄.scrollLeft]})()"); print('4번째 보기 → 칸 왼쪽 거리', r)
    # 줄을 밀어 놓고 → 세트값 만지면 다시 맨 앞
    await pg.evaluate("document.querySelector('.운띠').scrollLeft=0"); await pg.wait_for_timeout(200)
    await pg.click('#폰 .운세트들 [data-act="세트값"][data-f="w"][data-d="1"]'); await pg.wait_for_timeout(500)
    print('무게 + 뒤', await pg.evaluate("(()=>{const 줄=document.querySelector('.운띠'), 칸=줄.querySelector('.운칸.지금'); return Math.round(칸.getBoundingClientRect().left-줄.getBoundingClientRect().left)})()"))
    # 끌기: 1번째 칸을 3번째 칸 오른쪽으로
    await pg.evaluate("행동('본종목',{i:'0'})"); await pg.wait_for_timeout(400)
    a=await (await pg.query_selector('.운칸[data-i="0"]')).bounding_box(); c=await (await pg.query_selector('.운칸[data-i="2"]')).bounding_box()
    await pg.mouse.move(a['x']+30,a['y']+30); await pg.mouse.down(); await pg.wait_for_timeout(550)
    for t in range(1,11): await pg.mouse.move(a['x']+30+(c['x']+c['width']*0.8-a['x']-30)*t/10, a['y']+30); await pg.wait_for_timeout(20)
    await pg.mouse.up(); await pg.wait_for_timeout(400)
    print('끈 뒤', await 이름(), 'U.본', await pg.evaluate("[U.본, S.세션.종목[U.본].이름, S.세션.지금.i]"))
    await (await pg.query_selector('#폰')).screenshot(path=SP+'r10/v14.png'); print(err)
    await b.close()
asyncio.run(main())
