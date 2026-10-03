import asyncio, re
from playwright.async_api import async_playwright
SP='/tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'
src=open(SP+'test_v10_R.py',encoding='utf-8').read(); 준비=re.search(r'준비JS = """(.*?)"""', src, re.S).group(1)
async def main():
  async with async_playwright() as p:
    b=await p.chromium.launch(); pg=await b.new_page(viewport={'width':389,'height':900}); err=[]; pg.on('pageerror',lambda e:err.append(str(e)))
    await pg.goto('file://'+SP+'7day-v12.html'); await pg.wait_for_timeout(600)
    await pg.evaluate(준비); await pg.evaluate("""(()=>{ S.세션.종목.forEach(e=>e.세트.forEach(s=>s.완료=true)); 행동('운동저장',{}); 행동('결과확인',{}); U.업적띠=null; U.탭='캘린더'; 그리기(); })()""")
    await pg.wait_for_timeout(300)
    w=await pg.evaluate("[...document.querySelectorAll('.달력 .칸날')].slice(0,7).map(x=>Math.round(x.getBoundingClientRect().width))"); print('칸폭', w)
    el=await pg.query_selector('#폰'); await el.screenshot(path=SP+'r10/v12_캘.png')
    await pg.click('[data-act="스탯열기"][data-v="업적"]'); await pg.wait_for_timeout(300)
    await pg.evaluate("U.스탯.분류='달성'; 그리기(); 1"); await pg.wait_for_timeout(300)
    print('달성탭', await pg.evaluate("[document.querySelectorAll('.업적줄.풀림').length, document.querySelectorAll('.업적목록 .업적줄:not(.풀림)').length, !!document.querySelector('.업적목록 .이름표')]"))
    await pg.click('.칭호표.잠김'); await pg.wait_for_timeout(200)
    print('보임', await pg.evaluate("[document.querySelectorAll('.칭호표.잠김.보임').length, getComputedStyle(document.querySelector('.칭호표.잠김.보임')).textShadow]"), err)
    await el.screenshot(path=SP+'r10/v12_업적.png')
    await b.close()
asyncio.run(main())
