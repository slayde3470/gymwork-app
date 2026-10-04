import asyncio, re, sys, os
from playwright.async_api import async_playwright
SP='/tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'
HTML = sys.argv[1] if len(sys.argv)>1 else SP+'7day-v18E.html'
os.makedirs(SP+'r18', exist_ok=True)
src=open(SP+'test_v10_R.py',encoding='utf-8').read()
준비=re.search(r'준비JS = """(.*?)"""', src, re.S).group(1)
결과=[]
def ok(이름, 참, 값=""): 결과.append(bool(참)); print(("✅" if 참 else "❌"), 이름, 값)
async def main():
  async with async_playwright() as p:
    b=await p.chromium.launch(); ctx=await b.new_context(viewport={'width':389,'height':860}, has_touch=True)
    pg=await ctx.new_page(); err=[]; pg.on('pageerror',lambda e:err.append(str(e)))
    cdp=await ctx.new_cdp_session(pg)
    async def 터치(x0,y0,x1,y1,n=10,잡기=0,끝=True):
      await cdp.send('Input.dispatchTouchEvent',{'type':'touchStart','touchPoints':[{'x':x0,'y':y0}]})
      if 잡기: await pg.wait_for_timeout(잡기)
      for i in range(1,n+1):
        await cdp.send('Input.dispatchTouchEvent',{'type':'touchMove','touchPoints':[{'x':x0+(x1-x0)*i/n,'y':y0+(y1-y0)*i/n}]}); await pg.wait_for_timeout(16)
      if 끝: await cdp.send('Input.dispatchTouchEvent',{'type':'touchEnd','touchPoints':[]})
    await pg.goto('file://'+HTML); await pg.wait_for_timeout(600)
    await pg.evaluate(준비); await pg.wait_for_timeout(200)
    await pg.evaluate("""(()=>{ const ss=S.세션; ss.종목.forEach(e=>e.세트.forEach(s=>s.완료=true)); 행동('운동저장',{}); 행동('결과확인',{});
      S.설정.닉네임='홍겸'; 업적소급&&업적소급(); U.업적띠=null;
      const c=document.createElement('canvas'); c.width=90;c.height=120; const x=c.getContext('2d');
      인증=[0,1,2,3,4].map(i=>{ x.fillStyle=['#c55','#5a5','#55c','#aa5','#5aa'][i]; x.fillRect(0,0,90,120); return {id:'t'+i, src:c.toDataURL(), 때:i, 고정:0}; });
      let n=0; for(const a of 업적표){ if(S.업적[a.번호]) continue; if(n++>=10) break; S.업적[a.번호]={날:'2026-09-'+String(10+n).padStart(2,'0'), 순:100+n}; }
      U.탭='캘린더'; 그리기(); window.그린수=0; const 원=그리기; 그리기=function(){ window.그린수++; return 원.apply(this,arguments); }; })()""")
    await pg.wait_for_timeout(300)
    폰=await pg.query_selector('#폰')
    # ① 탭줄
    탭=await pg.evaluate("[...document.querySelectorAll('.탭줄 button')].map(b=>b.dataset.t)")
    ok('① 탭 순서', 탭==['캘린더','검색','루틴','종목','플랜','메모','설정','프로필'], 탭)

    # ── 프로필 ──
    await pg.click('.탭줄 [data-t="프로필"]'); await pg.wait_for_timeout(900)
    닉=await pg.evaluate("""(()=>{const 줄=document.querySelector('.인닉줄'), 칸=document.querySelector('.인닉'), 합=document.querySelector('.인오른 .큰합'), 오=document.querySelector('.인오른');
      const a=칸.getBoundingClientRect(), h=합.getBoundingClientRect(), o=오.getBoundingClientRect(), z=줄.getBoundingClientRect();
      return {칸왼:+a.left.toFixed(2), 상자왼:+h.left.toFixed(2), 오른왼:+o.left.toFixed(2), 줄위:+(z.top-o.top).toFixed(2), 줄아래:+(z.bottom-o.top).toFixed(2), 상자위:+(h.top-o.top).toFixed(2), 닉왼:줄.style.getPropertyValue('--닉왼')}})()""")
    ok('④ 닉네임 칸 왼쪽 = 파란 상자 왼쪽', abs(닉['칸왼']-닉['상자왼'])<0.6, 닉)
    ok('④ 닉네임 줄 아래로 7', abs(닉['줄위']-7)<0.6, f"줄 위 {닉['줄위']} · 줄 아래 {닉['줄아래']} · 상자 위 {닉['상자위']}")
    # B 가 상자를 ×1.1 로 넓혀도 따라가는지 (흉내)
    await pg.add_style_tag(content='.결과수.큰수 .큰합{width:calc((100% - 30px) * .935)!important}'); await pg.evaluate("그리기()"); await pg.wait_for_timeout(100)
    닉2=await pg.evaluate("(()=>{const a=document.querySelector('.인닉').getBoundingClientRect(), h=document.querySelector('.인오른 .큰합').getBoundingClientRect(); return [+a.left.toFixed(2), +h.left.toFixed(2)]})()")
    ok('④ 상자 ×1.1 흉내에도 맞음', abs(닉2[0]-닉2[1])<0.6, 닉2)
    await pg.evaluate("document.querySelectorAll('style').forEach(s=>{ if(s.textContent.includes('.935')) s.remove(); }); 그리기()"); await pg.wait_for_timeout(100)
    # ② 업적 줄 — 터치(브라우저 스크롤) · 직접 밀기 보조 · 마우스 끌기 · 휠
    업=await pg.evaluate("(()=>{const l=document.querySelector('.인업적'); return [l.children.length, l.scrollWidth, l.clientWidth]})()")
    box=await (await pg.query_selector('.인업적')).bounding_box(); y=box['y']+box['height']/2
    await 터치(box['x']+250,y,box['x']+50,y); await pg.wait_for_timeout(300)
    sl=await pg.evaluate("document.querySelector('.인업적').scrollLeft")
    ok('② 터치 가로 밀기 (브라우저 스크롤)', sl>100 and not await pg.evaluate("window.업적직접밀기||0"), [업, sl, '직접밀기', await pg.evaluate("window.업적직접밀기||0")])
    await pg.evaluate("document.querySelector('.인업적').scrollLeft=0")
    st=await pg.add_style_tag(content='.인업적,.인업적 *{touch-action:pan-y!important}')   # 브라우저 가로 밀기를 막은 폰 흉내
    await 터치(box['x']+250,y,box['x']+50,y); await pg.wait_for_timeout(300)
    sl2=await pg.evaluate("document.querySelector('.인업적').scrollLeft")
    ok('② 브라우저가 안 밀 때 → 직접 밀기 보조', sl2>100 and await pg.evaluate("window.업적직접밀기||0")>0, [sl2, await pg.evaluate("window.업적직접밀기")])
    await pg.evaluate("document.querySelectorAll('style').forEach(s=>{ if(s.textContent.includes('touch-action:pan-y!important')) s.remove(); }); document.querySelector('.인업적').scrollLeft=0")
    ok('② 밀고 난 뒤 업적 화면 안 열림', await pg.evaluate("!U.스탯"))
    await pg.mouse.move(box['x']+250,y); await pg.mouse.down()
    for k in range(1,11): await pg.mouse.move(box['x']+250-20*k,y); await pg.wait_for_timeout(16)
    await pg.mouse.up(); await pg.wait_for_timeout(200)
    ok('② 마우스로 끌어 밀기 · 업적 화면 안 열림', await pg.evaluate("document.querySelector('.인업적').scrollLeft")>150 and await pg.evaluate("!U.스탯"), await pg.evaluate("document.querySelector('.인업적').scrollLeft"))
    await pg.evaluate("document.querySelector('.인업적').scrollLeft=0")
    await pg.mouse.move(box['x']+100,y); await pg.mouse.wheel(0,200); await pg.wait_for_timeout(200)
    ok('② 마우스 휠 → 가로', await pg.evaluate("document.querySelector('.인업적').scrollLeft")>0, await pg.evaluate("document.querySelector('.인업적').scrollLeft"))
    await pg.evaluate("document.querySelector('.인업적').scrollLeft=0")
    await pg.click('.인업[data-i="0"]'); await pg.wait_for_timeout(300)
    ok('② 그냥 누르면 업적 화면', await pg.evaluate("U.스탯?.보기==='업적'"))

    # ── ③ 업적 화면 체크 ──
    줄=await pg.evaluate("[document.querySelectorAll('.업적줄묶음').length, document.querySelectorAll('.업적보임').length, Object.keys(S.업적).length, document.querySelectorAll('.업적보임.켬').length]")
    ok('③ 달성한 업적마다 체크 칸 · 기본 전부 켬', 줄[0]==줄[1]==줄[2]==줄[3], 줄)
    체=await pg.evaluate("(()=>{const b=document.querySelector('.업적보임'), r=b.getBoundingClientRect(), n=b.querySelector('.업적보임네모').getBoundingClientRect(); return [Math.round(r.width), Math.round(r.height), Math.round(n.width), getComputedStyle(b).fontSize, b.textContent]})()")
    print('   체크 칸 [폭, 높이, 네모, 글, 글자]', 체)
    대앞=await pg.evaluate("S.대표칭호"); 첫=await pg.evaluate("document.querySelector('.업적보임').dataset.v")
    await pg.click('.업적보임'); await pg.wait_for_timeout(200)
    ok('③ 체크 끔 → 숨김 · 대표 칭호 그대로', await pg.evaluate(f"S.업적숨김['{첫}']===true") and await pg.evaluate("S.대표칭호")==대앞 and await pg.evaluate(f"document.querySelector('.업적보임[data-v=\"{첫}\"]').getAttribute('aria-checked')")=='false', [첫, 대앞, await pg.evaluate("S.대표칭호")])
    # 둘째도 끔
    둘=await pg.evaluate("document.querySelectorAll('.업적보임')[1].dataset.v"); await pg.click(f'.업적보임[data-v="{둘}"]'); await pg.wait_for_timeout(200)
    await 폰.screenshot(path=SP+'r18/E_업적체크.png')
    await pg.click('.업적줄묶음 .업적줄'); await pg.wait_for_timeout(200)
    ok('③ 줄 본문을 누르면 대표 칭호(원래대로)', await pg.evaluate("S.대표칭호")!=대앞, await pg.evaluate("S.대표칭호"))
    await pg.evaluate("U.스탯=null; U.탭='프로필'; 그리기()"); await pg.wait_for_timeout(300)
    보=await pg.evaluate("(()=>{ const 줄=[...document.querySelectorAll('.인업')].length, 전=업적목록().length; return [줄, 전] })()")
    ok('③ 프로필 업적 줄 = 숨긴 것 빼고', 보[0]==보[1]-2, 보)
    # 끌어 옮기기 — 숨긴 업적은 제자리
    전앞=await pg.evaluate("업적목록().map(a=>String(a.번호))")
    a=await (await pg.query_selector('.인업[data-i="0"]')).bounding_box(); c=await (await pg.query_selector('.인업[data-i="2"]')).bounding_box()
    await pg.mouse.move(a['x']+a['width']/2, a['y']+20); await pg.mouse.down(); await pg.wait_for_timeout(520)
    for k in range(1,11): await pg.mouse.move(a['x']+a['width']/2+(c['x']+c['width']*0.8-a['x']-a['width']/2)*k/10, a['y']+20); await pg.wait_for_timeout(20)
    await pg.mouse.up(); await pg.wait_for_timeout(300)
    전뒤=await pg.evaluate("업적목록().map(a=>String(a.번호))")
    숨자리=[i for i,k in enumerate(전앞) if k in (첫,둘)]
    보앞=[k for k in 전앞 if k not in (첫,둘)]; 보뒤=[k for k in 전뒤 if k not in (첫,둘)]
    ok('③ 끌어 옮김 — 보이는 것만 바뀌고 숨긴 것은 제자리', 보뒤==보앞[1:3]+[보앞[0]]+보앞[3:] and all(전뒤[i]==전앞[i] for i in 숨자리), [숨자리, 보앞[:4], 보뒤[:4]])
    await pg.evaluate("S.업적숨김={}; 그리기()"); await pg.wait_for_timeout(200)
    await pg.evaluate("U.시트=null; 그리기()"); await pg.wait_for_timeout(700)
    await 폰.screenshot(path=SP+'r18/E_프로필.png')

    # ── ⑤ SNS 링크 ──
    await pg.click('.인닉'); await pg.wait_for_timeout(100); await pg.click('.인링크단추'); await pg.wait_for_timeout(300)
    ok('⑤ 기본 칸 1개 + ＋', await pg.evaluate("document.querySelectorAll('[data-in=\"링크\"]').length")==1 and await pg.evaluate("!!document.querySelector('[data-act=\"링크칸더\"]')"))
    await pg.fill('[data-in="링크"] >> nth=0', 'instagram.com/hong')
    for _ in range(4): await pg.click('[data-act="링크칸더"]'); await pg.wait_for_timeout(150)
    칸=await pg.evaluate("[document.querySelectorAll('[data-in=\"링크\"]').length, document.querySelector('[data-in=\"링크\"]').value, document.activeElement?.dataset?.i]")
    ok('⑤ ＋ 4번 → 칸 5개 · 친 글 그대로 · 새 칸에 포커스', 칸[0]==5 and 칸[1]=='instagram.com/hong' and 칸[2]=='4', 칸)
    await pg.fill('[data-in="링크"] >> nth=1', 'youtube.com/@hong'); await pg.fill('[data-in="링크"] >> nth=3', 'javascript:alert(1)'); await pg.fill('[data-in="링크"] >> nth=4', 'https://x.com/hong')
    await pg.click('[data-act="링크저장"]'); await pg.wait_for_timeout(300)
    링=await pg.evaluate("S.설정.링크")
    ok('⑤ 저장 — 빈 칸 · http(s) 아닌 꼴 버림 · 3개 넘어도', 링==['https://instagram.com/hong','https://youtube.com/@hong','https://x.com/hong'], 링)
    await pg.click('.인닉'); await pg.wait_for_timeout(100); await pg.click('.인링크단추'); await pg.wait_for_timeout(300)
    ok('⑤ 다시 열면 칸 3개', await pg.evaluate("document.querySelectorAll('[data-in=\"링크\"]').length")==3)
    await pg.evaluate("U.시트=null; 그리기()")

    # ── ⑥ 돋보기 ──
    await pg.click('.탭줄 [data-t="검색"]'); await pg.wait_for_timeout(500)
    async def 찾재기():
      return await pg.evaluate("""(()=>{const n=document.querySelector('#폰 .넘김'), c=[...document.querySelectorAll('.찾칸')], r=c[0].getBoundingClientRect(), 끝=c[c.length-1].getBoundingClientRect(), nb=n.getBoundingClientRect();
        const bm=document.querySelector('.찾북마크').getBoundingClientRect(), 입=document.querySelector('.찾칸입력').getBoundingClientRect(), 아=document.querySelector('.찾칸입력 svg').getBoundingClientRect(), i=document.querySelector('.찾입력');
        return {칸:c.length, 폭:+r.width.toFixed(1), 높이:+r.height.toFixed(1), 비:+(r.height/r.width).toFixed(3), 스크롤:[n.scrollHeight,n.clientHeight], 마지막아래여유:+(nb.bottom-끝.bottom).toFixed(1),
          북마크:[Math.round(bm.width),Math.round(bm.height)], 검색칸:[Math.round(입.left-bm.right), Math.round(입.height)], 아이콘왼:Math.round(아.left-입.left), placeholder:i.placeholder, 설명없음:!/준비/.test(n.textContent)}})()""")
    찾=await 찾재기()
    ok('⑥ 12칸 · 3:4 · 스크롤 없음 · 설명 없음', 찾['칸']==12 and abs(찾['비']-4/3)<0.01 and 찾['스크롤'][0]<=찾['스크롤'][1] and 찾['설명없음'], 찾)
    await 폰.screenshot(path=SP+'r18/E_검색.png')
    await pg.set_viewport_size({'width':389,'height':640}); await pg.evaluate("그리기()"); await pg.wait_for_timeout(200)
    찾2=await 찾재기()
    ok('⑥ 낮은 폰(640) — 비율 그대로 줄어 스크롤 없음', 찾2['칸']==12 and abs(찾2['비']-4/3)<0.01 and 찾2['스크롤'][0]<=찾2['스크롤'][1], 찾2)
    await pg.set_viewport_size({'width':389,'height':860}); await pg.evaluate("그리기()"); await pg.wait_for_timeout(200)

    # ── ⑦ 당겨서 새로고침 ──
    async def 당김시험(이름, 탭, 하기, 기대=True, 사진=None):
      await pg.evaluate(f"U.시트=null; U.스탯=null; U.탭='{탭}'; 그리기(); window.당김새로고침=0"); await pg.wait_for_timeout(400)
      그전=await pg.evaluate("window.그린수")
      값=await 하기()
      if 사진: await 폰.screenshot(path=사진)
      await pg.wait_for_timeout(900)
      n=await pg.evaluate("window.당김새로고침||0"); 남음=await pg.evaluate("[document.querySelectorAll('.당김표').length, document.querySelectorAll('.당기는중').length]")
      ok(f'⑦ {이름}', (n==1 if 기대 else n==0) and 남음==[0,0], [값, '새로고침', n, '남은 표·당김', 남음])
    n=await (await pg.query_selector('#폰 .넘김')).bounding_box()
    async def 터치당김(dy, 잡기=0, 사진중=None):
      nb=await (await pg.query_selector('#폰 .넘김, #폰 .운세트들')).bounding_box(); x=nb['x']+nb['width']/2; y=nb['y']+60
      await 터치(x,y,x,y+dy,n=12,잡기=잡기,끝=False)
      중=await pg.evaluate("(()=>{const t=document.querySelector('.당김표'), n=document.querySelector('.당기는중'); return t?[Math.round(t.getBoundingClientRect().height), n?getComputedStyle(n.firstElementChild).transform:'', Math.round(t.getBoundingClientRect().top - n.getBoundingClientRect().top)]:null})()")
      await cdp.send('Input.dispatchTouchEvent',{'type':'touchEnd','touchPoints':[]})
      await pg.wait_for_timeout(150)
      도=await pg.evaluate("(()=>{const t=document.querySelector('.당김표'); return t?[Math.round(t.getBoundingClientRect().height), t.classList.contains('도는중'), getComputedStyle(t.querySelector('.당김아이콘')).animationName]:null})()")
      if 사진중: await 폰.screenshot(path=사진중)
      return {'끄는중[표높이,속 이동,표-영역]':중, '놓은뒤[표높이,도는중,애니]':도}
    await 당김시험('캘린더 터치 120 → 새로고침', '캘린더', lambda: 터치당김(120, 사진중=SP+'r18/E_당김.png'))
    await 당김시험('터치 50 (덜 끌기) → 안 함', '캘린더', lambda: 터치당김(50), False)
    await 당김시험('루틴 터치 → 새로고침', '루틴', lambda: 터치당김(130))
    await 당김시험('설정 터치 → 새로고침', '설정', lambda: 터치당김(130))
    await 당김시험('프로필 터치 → 새로고침', '프로필', lambda: 터치당김(130))
    await 당김시험('검색 터치(검색 칸 바로 아래에서) → 새로고침', '검색', lambda: 터치당김(130))
    async def 마우스당김():
      nb=await (await pg.query_selector('#폰 .넘김')).bounding_box(); x=nb['x']+nb['width']/2; y=nb['y']+60
      await pg.mouse.move(x,y); await pg.mouse.down()
      for k in range(1,13): await pg.mouse.move(x,y+10*k); await pg.wait_for_timeout(16)
      await pg.mouse.up(); await pg.wait_for_timeout(50); return await pg.evaluate("[U.루틴열림, 막음클릭]")
    await 당김시험('루틴 마우스 끌기 → 새로고침 · 루틴 안 열림', '루틴', 마우스당김)
    async def 휠당김(틱):
      nb=await (await pg.query_selector('#폰 .넘김')).bounding_box(); await pg.mouse.move(nb['x']+nb['width']/2, nb['y']+100)
      await pg.wait_for_timeout(300)
      for _ in range(틱): await pg.mouse.wheel(0,-100); await pg.wait_for_timeout(50)
      return 틱
    await 당김시험('설정 휠 위로 2번 → 새로고침', '설정', lambda: 휠당김(2))
    await 당김시험('설정 휠 위로 1번 → 안 함', '설정', lambda: 휠당김(1), False)
    async def 관성():
      await pg.evaluate("document.querySelector('#폰 .넘김').scrollTop=150"); nb=await (await pg.query_selector('#폰 .넘김')).bounding_box()
      await pg.mouse.move(nb['x']+nb['width']/2, nb['y']+100)
      for _ in range(6): await pg.mouse.wheel(0,-100); await pg.wait_for_timeout(40)
      return await pg.evaluate("document.querySelector('#폰 .넘김').scrollTop")
    await 당김시험('설정 아래에서 휠로 올라와 맨 위에 닿은 관성 → 안 함', '설정', 관성, False)
    async def 아래에서():
      await pg.evaluate("document.querySelector('#폰 .넘김').scrollTop=150"); return await 터치당김(130)
    await 당김시험('스크롤 내려간 상태에서 아래로 끌기 → 안 함', '설정', 아래에서, False)
    async def 시트에서():
      await pg.evaluate("U.시트={종류:'메모'}; 그리기()"); await pg.wait_for_timeout(300)
      sb=await (await pg.query_selector('.시트')).bounding_box(); x=sb['x']+sb['width']/2; y=sb['y']+30
      await 터치(x,y,x,y+130,n=12); return None
    await 당김시험('시트 안 → 안 함', '캘린더', 시트에서, False)
    async def 꾹날():
      await pg.evaluate("(()=>{ if(!document.querySelector('.칸날[data-drag]')){ const k=날더하기(오늘(),1); S.예정[k]=S.루틴들[0].id; 그리기(); } })()"); await pg.wait_for_timeout(200)
      d=await (await pg.query_selector('.칸날[data-drag]')).bounding_box(); x=d['x']+d['width']/2; y=d['y']+d['height']/2
      await 터치(x,y,x,y+130,n=12,잡기=520); await pg.wait_for_timeout(100); return await pg.evaluate("[U.집은날, U.고른날]")
    await 당김시험('꾹 눌러 날짜 끌기가 먼저 → 안 함', '캘린더', 꾹날, False)
    await pg.evaluate("새로고침끔.push('캘린더')")
    await 당김시험('새로고침끔=[캘린더] → 안 함', '캘린더', lambda: 터치당김(130), False)
    await pg.evaluate("새로고침끔.length=0")
    # 운동 화면 세트 목록
    await pg.evaluate("U.시트=null; 운동시작('r1'); U.탭='운동'; 그리기()"); await pg.wait_for_timeout(400)
    if await pg.evaluate("!!S.세션 && !!document.querySelector('.운세트들')"):
      async def 운당김():
        nb=await (await pg.query_selector('#폰 .운세트들')).bounding_box(); x=nb['x']+nb['width']*0.15; y=nb['y']+40
        await 터치(x,y,x,y+130,n=12); return await pg.evaluate("document.querySelector('.운세트들').scrollHeight>document.querySelector('.운세트들').clientHeight")
      await pg.evaluate("window.당김새로고침=0"); 그전=await pg.evaluate("window.그린수")
      값=await 운당김(); await pg.wait_for_timeout(900)
      ok('⑦ 운동 화면 세트 목록 터치 → 새로고침', await pg.evaluate("window.당김새로고침")==1 and await pg.evaluate("document.querySelectorAll('.당김표,.당기는중').length")==0, 값)
    else: print('   (운동 시작 못 함 — 운동 화면 당김은 확인 못 함)', await pg.evaluate("Object.keys(S.루틴들[0]||{})"))
    ok('오류 없음', not err, err)
    print(f"\n{sum(결과)}/{len(결과)} 통과")
    await b.close()
asyncio.run(main())
