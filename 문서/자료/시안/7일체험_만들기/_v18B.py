# v18 B 확인 — 루틴 상세 시트 · 보일 것 · 끝 단추 · 버림 확인 · 파란 상자 너비.  python3 _v18B.py [html] [사진(1이면 찍음)]
import asyncio, re, sys, json
from playwright.async_api import async_playwright
SP='/tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'
HTML=sys.argv[1] if len(sys.argv)>1 else SP+'7day-v18B.html'
찍음=len(sys.argv)>2
src=open(SP+'test_v10_R.py',encoding='utf-8').read()
준비=re.search(r'준비JS = """(.*?)"""', src, re.S).group(1)
색=re.search(r'색JS = """(.*?)"""', src, re.S).group(1)
단추 = """[...document.querySelectorAll('.결과아래 button')].map(b=>{ const r=b.getBoundingClientRect(), z=getComputedStyle(b);
  const 살=[...b.querySelectorAll('.끝살')].map(i=>{ const q=i.getBoundingClientRect(); return {x:Math.round(q.left-r.left), 오:Math.round(r.right-q.right), 세로차:Math.round(((q.top+q.bottom)/2-(r.top+r.bottom)/2)*10)/10}; });
  const 줄=[...b.querySelectorAll('.양끝>span')].map(s=>{ const q=s.getBoundingClientRect(), is=[...s.querySelectorAll('i')].map(i=>Math.round(i.getBoundingClientRect().left-q.left)); return {w:Math.round(q.width*10)/10, 글자x:is}; });
  return {글:b.innerText.replace(/\\n/g,'/'), w:Math.round(r.width*10)/10, h:Math.round(r.height), 바탕:z.backgroundColor, 색:z.color, 크기:z.fontSize, 살, 줄,
    넘:b.scrollWidth>b.clientWidth+1||b.scrollHeight>b.clientHeight+1, 글높이:b.querySelector('.끝확인글,.양끝')?Math.round(b.querySelector('.끝확인글,.양끝').getBoundingClientRect().height):null,
    글폭:b.querySelector('.끝확인글,.양끝')?Math.round(b.querySelector('.끝확인글,.양끝').getBoundingClientRect().width):null}; })"""
async def 띄움(pg, 폭, 설정=""):
    await pg.set_viewport_size({'width':폭,'height':900})
    await pg.goto('file://'+HTML); await pg.evaluate("localStorage.clear()"); await pg.reload(); await pg.wait_for_timeout(500)
    await pg.evaluate(색); await pg.evaluate(준비); await pg.wait_for_timeout(100)
    await pg.evaluate("S.설정.닉네임='홍겸'; S.설정.큰운동추가=['오버헤드 프레스','바벨 로우']; "+설정+" S.세션.끝화면=true; 그리기(); 1"); await pg.wait_for_timeout(60)
async def main():
  async with async_playwright() as p:
    b=await p.chromium.launch(); pg=await b.new_page(viewport={'width':389,'height':900}); err=[]; pg.on('pageerror',lambda e:err.append(str(e)))
    out={}
    for 폭 in (389,360):
        await 띄움(pg,폭); await pg.wait_for_timeout(2000)
        out[f'단추{폭}']=await pg.evaluate(단추)
        out[f'큰합{폭}']=await pg.evaluate("(()=>{ const h=document.querySelector('.보고프로필 .큰합'), 판=h.parentElement; return {합:Math.round(h.getBoundingClientRect().width*10)/10, 판:판.clientWidth, 비:Math.round(h.getBoundingClientRect().width/(판.clientWidth-30)*1000)/1000, 넘:h.scrollWidth>h.clientWidth+1}; })()")
        if 찍음 and 폭==389: await (await pg.query_selector('#폰')).screenshot(path=SP+'r18/B_보고서.png')
        await pg.evaluate("행동('운동버림',{}); 1"); await pg.wait_for_timeout(50)
        out[f'버림{폭}']=await pg.evaluate(단추)
        if 찍음 and 폭==389: await (await pg.query_selector('.결과아래')).screenshot(path=SP+'r18/B_버림확인.png')
        if 찍음 and 폭==360: await (await pg.query_selector('.결과아래')).screenshot(path=SP+'r18/B_버림확인360.png')
    # 버림 두 번 → 끝남
    await pg.evaluate("행동('운동버림',{}); 1"); await pg.wait_for_timeout(50); out['버림두번']=await pg.evaluate("S.세션===null")
    # 1 루틴 상자 누름 → 시트
    await 띄움(pg,389); await pg.wait_for_timeout(600)
    await pg.click('.보고루틴 .보고루틴이름'); await pg.wait_for_timeout(400)
    out['루틴시트']=await pg.evaluate("[document.querySelector('.시트 .머리')?.innerText, document.querySelector('.시트')?.innerText, getComputedStyle(document.querySelector('.시트 .머리')).backgroundColor===__색('--강조')]")
    if 찍음: await (await pg.query_selector('#폰')).screenshot(path=SP+'r18/B_루틴상세.png')
    await pg.keyboard.press('Escape'); await pg.wait_for_timeout(150)
    out['Esc뒤']=await pg.evaluate("[!!document.querySelector('.보고띠'), !document.querySelector('.시트 .머리')]")
    # 2 시트 '보일 것' — 프로필 줄만
    await pg.click('.보고프로필 .톱니단추'); await pg.wait_for_timeout(300)
    out['보고방식']=await pg.evaluate("[document.querySelector('.시트 .머리')?.innerText, [...document.querySelectorAll('.시트 .설정줄')].map(x=>x.innerText.trim()), document.querySelectorAll('.시트 [data-act=\"보고보임\"]').length]")
    if 찍음: await (await pg.query_selector('#폰')).screenshot(path=SP+'r18/B_보고방식.png')
    # 프로필 끄기 → 톱니는 루틴 상자로 · 톱니 누르면 보고방식(루틴상세 아님)
    await pg.click('.시트 [data-act="보고보임"][data-f="프로필"]'); await pg.wait_for_timeout(150); await pg.evaluate("U.시트=null; 그리기(); 1"); await pg.wait_for_timeout(150)
    out['프로필끔']=await pg.evaluate("[!!document.querySelector('.보고프로필'), !!document.querySelector('.보고루틴 .톱니단추'), !!document.querySelector('.보고루틴')]")
    await pg.click('.보고루틴 .톱니단추'); await pg.wait_for_timeout(200)
    out['톱니→']=await pg.evaluate("U.시트&&U.시트.종류")
    # 예전 값 루틴:false 저장돼 있어도 루틴 상자 보임
    await 띄움(pg,389,"S.설정.보고서보임={프로필:true,루틴:false};")
    out['옛값루틴false']=await pg.evaluate("[!!document.querySelector('.보고루틴'), S.설정.보고서보임.루틴]")
    # 저장된 보고서에서도 루틴 상자 시트
    await pg.evaluate("const k=운동저장하기(); S.결과={key:k}; U.탭='캘린더'; 그리기(); 1"); await pg.wait_for_timeout(300)
    await pg.evaluate("document.querySelector('.보고루틴').click(); 1"); await pg.wait_for_timeout(200)
    out['저장뒤루틴시트']=await pg.evaluate("document.querySelector('.시트 .머리')?.innerText||null")
    await pg.evaluate("U.시트=null; 그리기(); 1"); out['저장뒤단추']=await pg.evaluate("[...document.querySelectorAll('.결과아래 button')].map(b=>b.innerText)")
    # 5 프로필 탭 파란 상자
    await pg.evaluate("S.결과=null; U.탭='프로필'; 그리기(); 1"); await pg.wait_for_timeout(300)
    out['프로필탭큰합']=await pg.evaluate("(()=>{ const h=document.querySelector('.큰합'); if(!h) return null; const 판=h.parentElement; return {합:Math.round(h.getBoundingClientRect().width*10)/10, 판:판.clientWidth, 비:Math.round(h.getBoundingClientRect().width/(판.clientWidth-30)*1000)/1000}; })()")
    if 찍음: await (await pg.query_selector('#폰')).screenshot(path=SP+'r18/B_프로필탭.png')
    # 어둡게 — 버림 확인 색
    ctx=await b.new_context(viewport={'width':389,'height':900}, color_scheme='dark'); p2=await ctx.new_page()
    await p2.goto('file://'+HTML); await p2.wait_for_timeout(400); await p2.evaluate(색); await p2.evaluate(준비)
    await p2.evaluate("S.세션.끝화면=true; 그리기(); 행동('운동버림',{}); 1"); await p2.wait_for_timeout(100)
    out['어둠버림']=await p2.evaluate("(()=>{ const b=document.querySelector('.확인중'), z=getComputedStyle(b); return [z.backgroundColor, __색('--나쁨'), z.color, __색('--강조글')]; })()")
    if 찍음: await (await p2.query_selector('.결과아래')).screenshot(path=SP+'r18/B_버림확인_어둠.png')
    out['오류']=err
    print(json.dumps(out,ensure_ascii=False))
    await b.close()
asyncio.run(main())
