import asyncio, re
from playwright.async_api import async_playwright
SP='/tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'
src=open(SP+'test_v10_R.py',encoding='utf-8').read(); 준비=re.search(r'준비JS = """(.*?)"""', src, re.S).group(1)
async def main():
  async with async_playwright() as p:
    b=await p.chromium.launch(); pg=await b.new_page(viewport={'width':389,'height':900}); err=[]; pg.on('pageerror',lambda e:err.append(str(e)))
    await pg.goto('file://'+SP+'7day-v13.html'); await pg.wait_for_timeout(600)
    await pg.evaluate(준비); await pg.wait_for_timeout(200)
    el=await pg.query_selector('#폰')
    # 운동 화면 + 종목 넣기
    await pg.evaluate("U.탭='운동'; U.업적띠=null; 그리기(); 1"); await pg.wait_for_timeout(300)
    n0=await pg.evaluate("S.세션.종목.length")
    await pg.evaluate("document.querySelector('.운칸.운더').scrollIntoView()"); await pg.click('.운칸.운더'); await pg.wait_for_timeout(300)
    await pg.click('.넣기줄[data-act="종목넣기"]'); await pg.wait_for_timeout(200)
    print('종목', n0, '→', await pg.evaluate("S.세션.종목.length"), await pg.evaluate("JSON.stringify(S.세션.종목.at(-1).세트[0])"))
    await pg.click('.시트 .닫기'); await pg.wait_for_timeout(300)
    await el.screenshot(path=SP+'r10/v13_운동.png')
    # 보고서 3대 / 5대
    await pg.evaluate("S.세션.끝화면=true; 그리기(); 1"); await pg.wait_for_timeout(300)
    await el.screenshot(path=SP+'r10/v13_보고3.png')
    await pg.evaluate("S.설정.큰운동추가=['오버헤드 프레스','바벨 로우']; 그리기(); 1"); await pg.wait_for_timeout(300)
    r=await pg.evaluate("(()=>{const 합=document.querySelector('.보고프로필 .큰합').getBoundingClientRect(), c=[...document.querySelectorAll('.보고프로필 .큰수>div')].map(x=>x.getBoundingClientRect()); return [Math.round(합.left),Math.round(합.right),Math.round(c[0].left),Math.round(c[2].right), c.map(x=>Math.round(x.top))]})()")
    print('5대 상자', r, err)
    await el.screenshot(path=SP+'r10/v13_보고5.png')
    await b.close()
asyncio.run(main())
