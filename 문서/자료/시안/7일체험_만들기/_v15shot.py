import asyncio, re
from playwright.async_api import async_playwright
SP='/tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'
src=open(SP+'test_v10_R.py',encoding='utf-8').read()
준비=re.search(r'준비JS = """(.*?)"""', src, re.S).group(1)
async def main():
  async with async_playwright() as p:
    b=await p.chromium.launch(); pg=await b.new_page(viewport={'width':389,'height':900}); err=[]; pg.on('pageerror',lambda e:err.append(str(e)))
    await pg.goto('file://'+SP+'7day-v15.html'); await pg.wait_for_timeout(600)
    await pg.evaluate(준비); await pg.wait_for_timeout(200)
    await pg.evaluate("""(()=>{ const ss=S.세션; ss.종목.forEach(e=>e.세트.forEach(s=>s.완료=true)); 행동('운동저장',{}); 행동('결과확인',{});
      S.설정.닉네임='홍겸'; 업적소급&&업적소급();
      const c=document.createElement('canvas'); c.width=90;c.height=120; const x=c.getContext('2d');
      인증=[0,1,2,3,4].map(i=>{ x.fillStyle=['#c55','#5a5','#55c','#aa5','#5aa'][i]; x.fillRect(0,0,90,120); return {id:'t'+i, src:c.toDataURL(), 때:i, 고정:i===1?1:0}; });
      U.탭="프로필"; 그리기(); })()""")
    await pg.wait_for_timeout(400)
    el=await pg.query_selector('#폰'); await el.screenshot(path=SP+'r10/v15_프로필.png')
    print(await pg.evaluate("[...document.querySelectorAll('.인칸')].length"), await pg.evaluate("document.querySelectorAll('.인업').length"))
    # 시트
    await pg.click('.인칸[data-v="t3"]'); await pg.wait_for_timeout(300)
    await pg.click('[data-act="인증고정"]'); await pg.wait_for_timeout(200)
    print('고정', await pg.evaluate("인증순().map(x=>x.id+(x.고정?'*':''))"))
    # 캘린더
    await pg.evaluate("U.업적띠=null; U.탭='캘린더'; U.고른날=오늘(); 그리기(); 1"); await pg.wait_for_timeout(300)
    await el.screenshot(path=SP+'r10/v15_캘린더.png')
    await pg.click('[data-act="보고보기"]'); await pg.wait_for_timeout(400)
    print('보고서', await pg.evaluate("!!document.querySelector('.보고띠')"), await pg.evaluate("[...document.querySelectorAll('.결과아래 button')].map(b=>b.textContent)"))
    await pg.click('[data-act="결과확인"]'); await pg.wait_for_timeout(300)
    print('돌아옴', await pg.evaluate("U.탭"), err)
    await b.close()
asyncio.run(main())
