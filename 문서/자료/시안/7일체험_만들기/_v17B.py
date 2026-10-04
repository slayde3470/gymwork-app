# v17 B 확인 — 결과뷰(보고서) · 큰운동판 · 톱니 · 상세 시트.  python3 _v17B.py [html] [사진접두]
import asyncio, re, sys, json
from playwright.async_api import async_playwright
SP='/tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'
HTML=sys.argv[1] if len(sys.argv)>1 else SP+'7day-v17B.html'
사진=sys.argv[2] if len(sys.argv)>2 else ''
src=open(SP+'test_v10_R.py',encoding='utf-8').read()
준비=re.search(r'준비JS = """(.*?)"""', src, re.S).group(1)
색=re.search(r'색JS = """(.*?)"""', src, re.S).group(1)
재기 = """(()=>{ const q=s=>document.querySelector(s), R=__R;
  const 띠=q('.보고띠'), 제=q('.보고띠 .결과띠속 b'), 날=q('.보고날'), 합=q('.보고프로필 .큰합'), 판=q('.보고프로필 .결과수.큰수');
  const 안=e=>{ if(!e) return null; const z=getComputedStyle(e); return {이름:z.animationName, 시간:z.animationDuration, 늦춤:z.animationDelay}; };
  return {띠:R(띠), 제:R(제), 날:R(날), 날글:날?.textContent, 합:R(합), 판:R(판), 판안폭:판?판.clientWidth:0, 합글:합?.innerText,
    칸글:[...document.querySelectorAll('.보고프로필 .큰수 .큰값 b')].map(b=>b.textContent),
    왼:R(q('.보고띠 [data-v="업적"]')), 오:R(q('.보고띠 [data-v="스탯"]')),
    톱:R(q('.톱니단추 svg')), 톱니수:(q('.톱니단추 svg')?.innerHTML||'').length,
    저장글:!!q('.결과저장글'), 아래:[...document.querySelectorAll('.결과아래 button')].map(b=>({글:b.innerText, R:R(b), 줄:Math.round(b.scrollHeight), 넘:b.scrollWidth>b.clientWidth+1||b.scrollHeight>b.clientHeight+1})),
    움:{띠:안(띠), 띠속:안(q('.결과띠속')), 프:안(q('.보고프로필')), 루:안(q('.보고루틴')), 칸:안(q('.보고칸')), 칸2:안(q('.보고칸:nth-child(2)'))},
    들어옴:[...document.querySelectorAll('#폰 .들어옴')].map(e=>e.className.slice(0,30)),
    가로넘침:document.documentElement.scrollWidth>document.documentElement.clientWidth+1}; })()"""
async def 띄움(pg, 폭):
    await pg.set_viewport_size({'width':폭,'height':900})
    await pg.goto('file://'+HTML); await pg.evaluate("localStorage.clear()"); await pg.reload(); await pg.wait_for_timeout(500)
    await pg.evaluate(색); await pg.evaluate(준비); await pg.wait_for_timeout(100)
    await pg.evaluate("S.설정.닉네임='홍겸'; S.설정.큰운동추가=['오버헤드 프레스','바벨 로우']; S.세션.끝화면=true; 그리기(); 1"); await pg.wait_for_timeout(60)
async def main():
  async with async_playwright() as p:
    b=await p.chromium.launch(); pg=await b.new_page(viewport={'width':389,'height':900}); err=[]; pg.on('pageerror',lambda e:err.append(str(e)))
    out={}
    for 폭 in (389,360):
        await 띄움(pg,폭); out[f'처음{폭}']=await pg.evaluate(재기)
        await pg.wait_for_timeout(2500)
        if 사진 and 폭==389: await (await pg.query_selector('#폰')).screenshot(path=SP+f'r17/{사진}_보고서.png')
        if 사진 and 폭==360: await (await pg.query_selector('#폰')).screenshot(path=SP+f'r17/{사진}_보고서360.png')
        await pg.evaluate("행동('운동버림',{}); 1"); await pg.wait_for_timeout(50)
        if 사진 and 폭==389: await (await pg.query_selector('.결과아래')).screenshot(path=SP+f'r17/{사진}_버림확인.png')
        out[f'버림{폭}']=await pg.evaluate("[...document.querySelectorAll('.결과아래 button')].map(b=>({글:b.innerText, h:b.getBoundingClientRect().height, w:b.getBoundingClientRect().width, 넘:b.scrollWidth>b.clientWidth+1||b.scrollHeight>b.clientHeight+1}))")
    # 화살표 → 업적 / 스탯, 돌아오기
    await 띄움(pg,389)
    async def 화살(v):
        el=await pg.query_selector(f'.보고띠 [data-v="{v}"]')
        if not el: return '단추없음'
        await el.click(); await pg.wait_for_timeout(200)
        r={'스탯':await pg.evaluate("U.스탯&&U.스탯.보기"), '업적판':await pg.evaluate("!!document.querySelector('.업적목록')"), '스탯판':await pg.evaluate("!!document.querySelector('.스탯칸')")}
        if 사진 and v=='업적': await (await pg.query_selector('#폰')).screenshot(path=SP+f'r17/{사진}_업적.png')
        await pg.keyboard.press('Escape'); await pg.wait_for_timeout(150)
        r['Esc뒤보고서']=await pg.evaluate("!!document.querySelector('.보고띠')")
        await el.is_visible() if False else None
        el2=await pg.query_selector(f'.보고띠 [data-v="{v}"]'); await el2.click(); await pg.wait_for_timeout(150)
        await pg.evaluate("document.querySelector('.탭줄 [data-t=\"캘린더\"]')?.click(); 1"); await pg.wait_for_timeout(150)
        r['탭뒤보고서']=await pg.evaluate("!!document.querySelector('.보고띠')"); r['세션남음']=await pg.evaluate("!!S.세션&&S.세션.끝화면")
        return r
    out['업적']=await 화살('업적'); out['스탯']=await 화살('스탯')
    # 상세 시트 — 보고서
    await pg.evaluate("document.querySelector('.보고프로필 .큰합')?.click(); 1"); await pg.wait_for_timeout(400)
    out['합시트']=await pg.evaluate("[document.querySelector('.시트 .머리')?.innerText, document.querySelector('.시트')?.innerText]")
    if 사진: await (await pg.query_selector('#폰')).screenshot(path=SP+f'r17/{사진}_상세.png')
    await pg.evaluate("U.시트=null; 그리기(); 1")
    n=await pg.evaluate("document.querySelectorAll('.보고프로필 .큰수>div').length"); out['칸시트']=[]
    for i in range(n):
        await pg.evaluate(f"document.querySelectorAll('.보고프로필 .큰수>div')[{i}].click(); 1"); await pg.wait_for_timeout(100)
        out['칸시트'].append(await pg.evaluate("document.querySelector('.시트 .머리')?.innerText||null")); await pg.evaluate("U.시트=null; 그리기(); 1")
    # 저장 뒤 보기
    await pg.evaluate("const k=운동저장하기(); S.결과={key:k}; U.탭='캘린더'; 그리기(); 1"); await pg.wait_for_timeout(300)
    out['저장뒤']=await pg.evaluate(재기)
    await pg.evaluate("document.querySelector('.보고띠 [data-v=\"스탯\"]')?.click(); 1"); await pg.wait_for_timeout(150); out['저장뒤스탯']=await pg.evaluate("!!document.querySelector('.스탯칸')")
    await pg.evaluate("document.querySelector('.탭줄 [data-t=\"루틴\"]')?.click(); 1"); await pg.wait_for_timeout(150); out['저장뒤돌아옴']=await pg.evaluate("!!document.querySelector('.보고띠')")
    # 프로필 탭
    await pg.evaluate("S.결과=null; U.탭='프로필'; 그리기(); 1"); await pg.wait_for_timeout(300)
    out['프로필']=await pg.evaluate("(()=>{ const 합=document.querySelector('.큰합'), 판=합?.parentElement; return {합:__R(합), 판:__R(판), 글:합?.innerText, 톱:__R(document.querySelector('.톱니단추 svg'))}; })()")
    await pg.evaluate("document.querySelector('.큰수>div')?.click(); 1"); await pg.wait_for_timeout(400); out['프로필시트']=await pg.evaluate("document.querySelector('.시트 .머리')?.innerText||null")
    if 사진: await (await pg.query_selector('#폰')).screenshot(path=SP+f'r17/{사진}_프로필시트.png')
    await pg.evaluate("U.시트=null; U.탭='설정'; 그리기(); 1"); out['설정탭아이콘']=await pg.evaluate("__R(document.querySelector('.탭줄 [data-t=\"설정\"] svg'))")
    # 움직임 줄임
    ctx=await b.new_context(viewport={'width':389,'height':900}, reduced_motion='reduce'); p2=await ctx.new_page()
    await p2.goto('file://'+HTML); await p2.wait_for_timeout(400); await p2.evaluate(색); await p2.evaluate(준비)
    await p2.evaluate("S.세션.끝화면=true; 그리기(); 1"); await p2.wait_for_timeout(50)
    out['줄임']=await p2.evaluate("[...document.querySelectorAll('.보고띠,.보고프로필,.보고루틴,.보고칸')].map(e=>getComputedStyle(e).animationName+'/'+getComputedStyle(e).opacity).join(' ')")
    out['오류']=err
    print(json.dumps(out,ensure_ascii=False,indent=0))
    await b.close()
asyncio.run(main())
