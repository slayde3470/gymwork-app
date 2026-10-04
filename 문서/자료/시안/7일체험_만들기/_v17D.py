import asyncio, re, sys, json
from playwright.async_api import async_playwright
SP='/tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'
HTML=sys.argv[1] if len(sys.argv)>1 else SP+'7day-v17D.html'
src=open(SP+'test_v10_R.py',encoding='utf-8').read(); 준비=re.search(r'준비JS = """(.*?)"""', src, re.S).group(1)
async def main():
  async with async_playwright() as p:
    b=await p.chromium.launch(); pg=await b.new_page(viewport={'width':389,'height':860}); err=[]; pg.on('pageerror',lambda e:err.append(str(e)))
    await pg.goto('file://'+HTML); await pg.wait_for_timeout(600)
    await pg.evaluate(준비)
    # 지난날 하나에 기록 3개
    await pg.evaluate("""(()=>{ S.세션=null; U.업적띠=null; const k=날더하기(오늘(),-2), r=S.기록[k];
      S.기록[k+'~2']={...JSON.parse(JSON.stringify(r)), 이름:'등'}; S.기록[k+'~3']={...JSON.parse(JSON.stringify(r)), 이름:'팔'};
      U.탭='캘린더'; U.고른날=k; 그리기(); })()""")
    await pg.wait_for_timeout(400)
    el=await pg.query_selector('#폰')
    # 2 구분선
    print('구분선', await pg.evaluate("""(()=>{ const c=document.querySelectorAll('.달력 .칸날')[9], a=getComputedStyle(c,'::after');
      const r=c.getBoundingClientRect(); const x=document.elementFromPoint(r.left+r.width/2, r.top+0.5);
      return {overflow:getComputedStyle(c).overflow, after위:a.borderTopWidth+' '+a.borderTopColor, after왼:a.borderLeftWidth, 폭:[...document.querySelectorAll('.달력 .칸날')].slice(7,14).map(x=>Math.round(x.getBoundingClientRect().width*10)/10),
        고름테:getComputedStyle(document.querySelector('.칸날.고름')).borderTopColor, 강조:getComputedStyle(document.documentElement).getPropertyValue('--강조'),
        넘:[...document.querySelectorAll('.칸날 .루')].filter(x=>x.scrollWidth>x.clientWidth+1).length, 루말줄:getComputedStyle(document.querySelector('.칸날 .루')||c).textOverflow}; })()"""))
    # 픽셀로 선 확인: 칸 경계 한 줄 색
    # 4 ‹ ›
    print('‹ ›', await pg.evaluate("""(()=>{ const q=document.querySelector('.년월띠 .달넘김'), r=q.getBoundingClientRect(); return {w:r.width,h:r.height,fs:getComputedStyle(q).fontSize, 띠높:document.querySelector('.년월띠').getBoundingClientRect().height}; })()"""))
    # 1 체크 칸
    print('체크', await pg.evaluate("""[...document.querySelectorAll('.록고름')].map(x=>x.getAttribute('aria-pressed')+':'+Math.round(x.getBoundingClientRect().width))"""))
    await el.screenshot(path=SP+'r17/D_캘린더_체크.png')
    await pg.click('.록고름[data-v$="~2"]'); await pg.wait_for_timeout(300)
    print('끔', await pg.evaluate("[U.록뺌, document.querySelector('[data-act=기록지움]').disabled]"))
    # 다 끄면 disabled
    await pg.evaluate("U.록뺌=기록목록(U.고른날).map(x=>x[0]); 그리기(); 1")
    print('다끔 disabled', await pg.evaluate("document.querySelector('[data-act=기록지움]').disabled"))
    await pg.evaluate("U.록뺌=[날더하기(오늘(),-2)+'~2']; 그리기(); 1"); await pg.wait_for_timeout(200)
    btn=await pg.evaluate("(()=>{const r=document.querySelector('[data-act=기록지움]').getBoundingClientRect(); return [r.top,r.bottom]})()")
    await pg.click('[data-act=기록지움]'); await pg.wait_for_timeout(250)
    띠=await pg.evaluate("""(()=>{ const t=document.querySelector('.록지움띠'), r=t.getBoundingClientRect(); return {top:r.top,bottom:r.bottom,글:t.innerText,pe:getComputedStyle(t).pointerEvents,bpe:getComputedStyle(t.querySelector('button')).pointerEvents, 남은:기록목록(U.고른날).map(x=>x[1].이름), anim:getComputedStyle(t).animationName}; })()""")
    print('지움 단추', btn, '띠', 띠)
    await el.screenshot(path=SP+'r17/D_지움띠.png')
    # 띠 밖 누르기: 띠가 떠 있는 동안 다른 날 누르기 → 되돌리기 대상 유지
    t0=await pg.evaluate("performance.now()")
    await pg.click('.달력 .칸날[data-k="'+await pg.evaluate("오늘()")+'"]'); await pg.wait_for_timeout(200)
    print('다른날 눌림', await pg.evaluate("[U.고른날===오늘(), !!document.querySelector('.록지움띠'), document.querySelector('.록지움띠')?.getBoundingClientRect().top]"))
    # 띠 아래 깔린 것 누르기 — 띠 글자 자리 elementFromPoint
    print('띠글자밑', await pg.evaluate("""(()=>{ const t=document.querySelector('.록지움띠 .채움').getBoundingClientRect(); const e=document.elementFromPoint(t.left+5,t.top+t.height/2); return e.className.slice(0,30); })()"""))
    await pg.click('.록지움띠 [data-act=기록되돌림]'); await pg.wait_for_timeout(200)
    k2=await pg.evaluate("날더하기(오늘(),-2)")
    print('되돌림', await pg.evaluate(f"기록목록('{k2}').map(x=>x[1].이름)"))
    # 시간: 4500 뒤 사라짐
    await pg.click('[data-act=기록지움]'); await pg.wait_for_timeout(4300); a=await pg.evaluate("!!document.querySelector('.록지움띠')")
    await pg.wait_for_timeout(500); bb=await pg.evaluate("!!document.querySelector('.록지움띠')")
    print('4.3s 있음', a, '4.8s 없음', not bb)
    await pg.click('[data-act=기록되돌림]') if bb else None
    # 3 달 고르기 → 년 고르기
    await pg.click('[data-act=달고르기]'); await pg.wait_for_timeout(400)
    print('시트 ‹ ›', await pg.evaluate("(()=>{const q=document.querySelector('.달머리 .달넘김').getBoundingClientRect(); return [q.width,q.height,getComputedStyle(document.querySelector('.달머리 .달넘김')).fontSize, document.querySelector('.달머리').getBoundingClientRect().height, getComputedStyle(document.querySelector('.달머리 .년월글 b')).fontSize]})()"))
    await pg.click('[data-act=해고르기]'); await pg.wait_for_timeout(300)
    print('년격자', await pg.evaluate("[document.querySelector('.달머리 b').textContent, [...document.querySelectorAll('.달칸')].map(x=>x.textContent).join(','), document.querySelector('.달칸.고름')?.textContent]"))
    await el.screenshot(path=SP+'r17/D_년고르기.png')
    await pg.click('[data-act=해묶음넘김][data-d="1"]'); await pg.wait_for_timeout(200)
    print('다음12', await pg.evaluate("document.querySelector('.달머리 b').textContent"))
    await pg.click('[data-act=해고름][data-v="2033"]'); await pg.wait_for_timeout(200)
    print('2033 달고르기', await pg.evaluate("[document.querySelector('.달머리 b').textContent, document.querySelectorAll('.달칸[data-act=달고름]').length]"))
    await pg.click('[data-act=달고름][data-v="2033-05"]'); await pg.wait_for_timeout(300)
    print('보는달', await pg.evaluate("[U.보는달, document.querySelector('.년월글 b').textContent]"))
    # 5 업적띠 · 토스트 자리
    await pg.evaluate("U.보는달=null; U.고른날=오늘(); 그리기(); 1"); await pg.wait_for_timeout(200)
    await pg.click('[data-act=스탯열기][data-v=업적]'); await pg.wait_for_timeout(200)
    await pg.evaluate("업적알림([업적표[0].번호]); 그리기(); 1"); await pg.wait_for_timeout(250)
    print('업적띠', await pg.evaluate("(()=>{const t=document.querySelector('.업적띠'); const r=t.getBoundingClientRect(); return {top:r.top,bottom:r.bottom, 단추:document.querySelector('.스탯아래')?.getBoundingClientRect().top, 폰:document.getElementById('폰').getBoundingClientRect().top}})()"))
    await el.screenshot(path=SP+'r17/D_업적띠.png')
    await pg.wait_for_timeout(3700); print('업적 3.95s 없음', not await pg.evaluate("!!document.querySelector('.업적띠')"))
    # 토스트 — 버튼 누른 뒤
    await pg.evaluate("U.스탯=null; U.탭='캘린더'; 그리기(); 1"); await pg.wait_for_timeout(200)
    q=await pg.evaluate("(()=>{const r=document.querySelector('[data-act=기록지움]')?.getBoundingClientRect()||document.querySelector('.판 button').getBoundingClientRect(); return [r.top,r.bottom]})()")
    await pg.click('.판 button'); await pg.evaluate("토스트('시험 토스트')"); await pg.wait_for_timeout(100)
    print('토스트', q, await pg.evaluate("(()=>{const t=document.querySelector('.토스트'); const r=t.getBoundingClientRect(); return [r.top,r.bottom,r.left,r.right,getComputedStyle(t).pointerEvents]})()"))
    await pg.wait_for_timeout(1500); print('토스트 1.6s 없음', not await pg.evaluate("!!document.querySelector('.토스트')"))
    print('err', err)
    await b.close()
asyncio.run(main())
