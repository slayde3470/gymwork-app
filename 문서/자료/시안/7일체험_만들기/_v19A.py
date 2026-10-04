# v19 A 확인 — 루틴 · 플랜 · 설정 띠 / 당김은 띠 아래 · 띠 안 움직임 / 검색 줄 고정
# python3 _v19A.py [html 절대경로]   (사진: r19/A_*.png)  ※ <style> 은 건드리지 않는다
import asyncio, re, sys, os
from playwright.async_api import async_playwright
SP='/tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'
HTML = sys.argv[1] if len(sys.argv)>1 else SP+'7day-v19A.html'
사진 = len(sys.argv)<=2
os.makedirs(SP+'r19', exist_ok=True)
준비=re.search(r'준비JS = """(.*?)"""', open(SP+'test_v10_R.py',encoding='utf-8').read(), re.S).group(1)
결과=[]
def ok(이름, 참, 값=""): 결과.append(bool(참)); print(("✅" if 참 else "❌"), 이름, 값)

띠JS = """sel=>{ const b=document.querySelector('#폰 .띠.'+sel); if(!b) return null; const r=b.getBoundingClientRect(), t=b.querySelector('b'), cs=getComputedStyle(b);
  const rg=document.createRange(); rg.selectNodeContents(t); const tr=rg.getBoundingClientRect();
  const 넘=document.querySelector('#폰 .넘김'), 넘안=넘&&넘.contains(b);
  return {높이:+r.height.toFixed(1), 글:getComputedStyle(t).fontSize, 굵기:getComputedStyle(t).fontWeight, 정렬:getComputedStyle(t).textAlign,
    바탕:cs.backgroundColor, 글색:cs.color, 가운데어긋남:+((tr.left+tr.right)/2-(r.left+r.right)/2).toFixed(1), 넘김안:넘안, 위:+r.top.toFixed(1),
    넘김위틈: 넘? +(넘.getBoundingClientRect().top-r.bottom).toFixed(1):null, h1:!!document.querySelector('#폰 h1')}; }"""
색JS = """v=>{const d=document.createElement('i'); d.style.color=getComputedStyle(document.documentElement).getPropertyValue(v); document.body.appendChild(d); const c=getComputedStyle(d).color; d.remove(); return c;}"""

async def main():
  async with async_playwright() as p:
    b=await p.chromium.launch(); ctx=await b.new_context(viewport={'width':389,'height':860}, has_touch=True)
    pg=await ctx.new_page(); err=[]; pg.on('pageerror',lambda e:err.append(str(e)))
    cdp=await ctx.new_cdp_session(pg)
    await pg.goto('file://'+HTML); await pg.wait_for_timeout(600)
    await pg.evaluate(준비); await pg.wait_for_timeout(300)
    폰=await pg.query_selector('#폰')

    # ── 공용: 띠 아래에서 아래로 130 끌기 → 끄는 중 띠 위치 · 빈 줄 위치 · 새로고침 수 ──
    async def 당김(이름, 띠sel, 시작sel=None, 사진이름=None, 기대=True):
      await pg.evaluate("window.당김새로고침=0"); await pg.wait_for_timeout(900)
      전=await pg.evaluate("""([띠,시])=>{ const d=document.querySelector(띠); const a=시?document.querySelector(시):document.querySelector('#폰 .넘김, #폰 .운세트들');
        if(!a) return null; const ar=a.getBoundingClientRect(), dr=d?d.getBoundingClientRect():null;
        const y = (시&&시===띠) ? (ar.top+ar.bottom)/2 : Math.max(ar.top, dr?dr.bottom:0)+ (시?Math.min(30, ar.height/2):40);   // 띠 위 시작 = 띠 가운데
        return {띠위:dr?+dr.top.toFixed(1):null, 띠아래:dr?+dr.bottom.toFixed(1):null, x:ar.left+ar.width*0.3, y}; }""", [띠sel, 시작sel])
      if not 전: ok(f'{이름} — 시작 자리 없음', False); return
      x,y=전['x'],전['y']
      await cdp.send('Input.dispatchTouchEvent',{'type':'touchStart','touchPoints':[{'x':x,'y':y}]})
      for i in range(1,13):
        await cdp.send('Input.dispatchTouchEvent',{'type':'touchMove','touchPoints':[{'x':x,'y':y+130*i/12}]}); await pg.wait_for_timeout(16)
      중=await pg.evaluate("""띠=>{ const d=document.querySelector(띠), t=document.querySelector('.당김표'), n=document.querySelector('.당기는중');
        const dr=d?d.getBoundingClientRect():null, tr=t?t.getBoundingClientRect():null;
        const 첫=n?[...n.children].find(c=>!c.classList.contains('당김고정')):null;
        return {띠위:dr?+dr.top.toFixed(1):null, 띠변형:d?getComputedStyle(d).transform:null, 표위:tr?+tr.top.toFixed(1):null, 표높이:tr?Math.round(tr.height):null,
          속이동:첫?getComputedStyle(첫).transform:null, 영역:n?n.className.split(' ').slice(0,2).join('.'):null}; }""", 띠sel)
      if 사진이름 and 사진: await 폰.screenshot(path=SP+'r19/'+사진이름)
      await cdp.send('Input.dispatchTouchEvent',{'type':'touchEnd','touchPoints':[]})
      await pg.wait_for_timeout(1000)
      n=await pg.evaluate("window.당김새로고침||0"); 남=await pg.evaluate("document.querySelectorAll('.당김표,.당기는중').length")
      뒤=await pg.evaluate("띠=>{const d=document.querySelector(띠); return d?+d.getBoundingClientRect().top.toFixed(1):null}", 띠sel)
      if not 기대:
        ok(f'{이름}', n==0 and 남==0, {'새로고침':n, '중':중}); return
      띠그대로 = 전['띠위'] is None or (중['띠위']==전['띠위'] and 뒤==전['띠위'])
      표아래 = 중['표위'] is not None and (전['띠아래'] is None or 중['표위']>=전['띠아래']-0.6)
      ok(f'{이름}', 띠그대로 and 표아래 and n==1 and 남==0,
         {'띠 top 전/중/뒤':[전['띠위'],중['띠위'],뒤], '띠 bottom':전['띠아래'], '빈줄 top':중['표위'], '빈줄 높이':중['표높이'], '속 이동':중['속이동'], '영역':중['영역'], '새로고침':n})

    # ── 운동 화면 ──
    await pg.evaluate("U.탭='운동'; 그리기()")
    await 당김('② 운동 화면 — 머리 띠 그대로 · 빈 줄은 세트 목록 위', '#폰 .운머리', '#폰 .운세트들 .세트머리')
    # ── 보고서 (끝 화면) ──
    await pg.click('.아랫줄 [data-act="끝내기"]'); await pg.wait_for_timeout(4300)
    await pg.evaluate("U.업적띠=null; document.querySelectorAll('.업적띠').forEach(x=>x.remove()); 1")
    띠안=await pg.evaluate("!!document.querySelector('#폰 .넘김 .보고띠')")
    print('   보고서 띠가 스크롤 영역(.결과틀) 안에 있음:', 띠안)
    await 당김('② 보고서(끝 화면) — 띠 아래 프로필에서 끌기', '#폰 .보고띠', '#폰 .결과틀 > :not(.띠)', 'A_보고당김.png')
    await 당김('② 보고서(끝 화면) — 종목 목록에서 끌기', '#폰 .보고띠', '#폰 .보고목록')
    await 당김('② 보고서 — 띠 위에서 끌기 → 당김 아님', '#폰 .보고띠', '#폰 .보고띠', 기대=False)
    # ── 저장된 보고서 ──
    await pg.evaluate("행동('운동저장',{}); S.결과={key:Object.keys(S.기록).sort().pop()}; U.업적띠=null; 그리기()"); await pg.wait_for_timeout(500)
    print('   저장된 보고서 보임:', await pg.evaluate("!S.세션 && !!S.결과 && !!document.querySelector('#폰 .보고띠')"))
    await 당김('② 저장된 보고서 — 띠 그대로', '#폰 .보고띠', '#폰 .결과틀 > :not(.띠)')
    await pg.evaluate("행동('결과확인',{}); U.업적띠=null; 그리기()"); await pg.wait_for_timeout(400)

    # ── ① 띠 모양 ──
    async def 탭(t, 더=""):
      await pg.evaluate(f"U.시트=null; U.스탯=null; U.루틴열림=null; U.탭='{t}'; {더} 그리기()"); await pg.wait_for_timeout(500)
    띠값={}
    for t,cls in [('종목','종목띠'),('루틴','루틴목록띠'),('플랜','플랜띠'),('설정','설정띠')]:
      await 탭(t); 띠값[t]=await pg.evaluate(띠JS, cls); print('  ', t, 띠값[t])
    기=띠값['종목']
    for t in ['루틴','플랜','설정']:
      v=띠값[t]
      ok(f'① {t} — h1 없음 · 띠가 스크롤 영역 밖 맨 위 · 높이/글 = 종목 띠 · 글 가운데',
         v and not v['h1'] and not v['넘김안'] and v['높이']==기['높이'] and v['글']==기['글'] and v['굵기']==기['굵기'] and v['정렬']=='center' and abs(v['가운데어긋남'])<1 and v['위']==기['위'],
         {k:v[k] for k in ['높이','글','굵기','정렬','가운데어긋남','위','넘김위틈']} if v else None)
    for t in ['루틴','플랜']:
      ok(f'① {t} — 강조 바탕 · 강조글 (종목 띠와 같음)', 띠값[t]['바탕']==기['바탕'] and 띠값[t]['글색']==기['글색'], [띠값[t]['바탕'], 띠값[t]['글색']])
    선=await pg.evaluate(색JS,'--선'); 글=await pg.evaluate(색JS,'--글')
    ok('① 설정 — 회색(--선) 바탕 · 검정(--글) 글', 띠값['설정']['바탕']==선 and 띠값['설정']['글색']==글 and 글=='rgb(0, 0, 0)', [띠값['설정']['바탕'], 띠값['설정']['글색']])
    await 탭('설정')
    if 사진: await 폰.screenshot(path=SP+'r19/A_설정띠.png')
    await pg.emulate_media(color_scheme='dark'); await pg.wait_for_timeout(200)
    어=await pg.evaluate(띠JS,'설정띠'); 어선=await pg.evaluate(색JS,'--선'); 어글=await pg.evaluate(색JS,'--글')
    ok('① 설정 띠 다크 — --선 · --글 따라감', 어['바탕']==어선 and 어['글색']==어글, [어['바탕'], 어['글색']])
    await pg.emulate_media(color_scheme='light'); await pg.wait_for_timeout(200)
    await 탭('루틴', "U.루틴열림=S.루틴들[0].id;")
    상세=await pg.evaluate("[!!document.querySelector('#폰 .띠.루띠'), !!document.querySelector('#폰 .루틴목록띠')]")
    ok('① 루틴 하나 연 화면 — 원래 머리 띠 그대로 · 새 띠 없음', 상세==[True,False], 상세)

    # ── ② 화면마다 당김 ──
    await 탭('캘린더'); await 당김('② 캘린더 — 년월 띠 그대로', '#폰 .년월띠')
    await 탭('루틴');   await 당김('② 루틴 — 띠 그대로', '#폰 .루틴목록띠', None, 'A_루틴당김.png')
    await 탭('루틴', "U.루틴열림=S.루틴들[0].id;"); await 당김('② 루틴 하나 연 화면 — 띠 그대로', '#폰 .루띠')
    await 탭('종목');   await 당김('② 종목 — 띠 그대로', '#폰 .종목띠')
    await 탭('플랜');   await 당김('② 플랜 — 띠 그대로', '#폰 .플랜띠')
    await 탭('설정');   await 당김('② 설정 — 띠 그대로', '#폰 .설정띠')
    await 탭('프로필'); await 당김('② 프로필 — 띠 없음, 맨 위부터', '#폰 .없는띠')
    await pg.evaluate("U.스탯={보기:'업적'}; 그리기()"); await pg.wait_for_timeout(400)
    업띠=await pg.evaluate("!!document.querySelector('#폰 .화면>.띠')")
    if 업띠: await 당김('② 업적 — 업적 띠 그대로', '#폰 .화면>.띠')
    # 띠 위에서 끌기 (스크롤 영역 밖) → 당김 아님
    await 탭('설정'); await 당김('② 설정 띠 위에서 끌기 → 당김 아님', '#폰 .설정띠', '#폰 .설정띠', 기대=False)

    # ── ② 검색 ──
    await 탭('검색')
    검=await pg.evaluate("""(()=>{ const z=document.querySelector('#폰 .찾줄'), 넘=document.querySelector('#폰 .넘김.찾화면'), 판=document.querySelector('#폰 .찾판'), 칸=document.querySelector('#폰 .찾칸'), 화=document.querySelector('#폰 .화면');
      const zr=z.getBoundingClientRect(), hr=화.getBoundingClientRect(), pr=판.getBoundingClientRect(), kr=칸.getBoundingClientRect();
      return {줄넘김안:넘.contains(z), 줄위:+(zr.top-hr.top).toFixed(1), 줄아래:+(zr.bottom-hr.top).toFixed(1), 판위:+(pr.top-hr.top).toFixed(1), 판폭:+pr.width.toFixed(1), 판높이:+pr.height.toFixed(1),
        칸:[+kr.width.toFixed(1), +kr.height.toFixed(1)], 칸수:document.querySelectorAll('#폰 .찾칸').length, 스크롤:넘.scrollHeight-넘.clientHeight}; })()""")
    print('   검색', 검)
    ok('② 검색 — 북마크·검색 줄이 스크롤 영역 밖 (고정) · 사진 12장 · 스크롤 없음', not 검['줄넘김안'] and 검['칸수']==12 and 검['스크롤']<=0, 검)
    await 당김('② 검색 — 북마크·검색 줄 그대로 · 빈 줄은 사진 격자 위', '#폰 .찾위', None, 'A_검색당김.png')

    # ── ② 마우스 끌기 · 휠 (띠 그대로) ──
    async def 다른당김(이름, 탭이름, 띠sel, 종류):
      await 탭(탭이름); await pg.evaluate("window.당김새로고침=0"); await pg.wait_for_timeout(900)
      nb=await (await pg.query_selector('#폰 .넘김')).bounding_box(); x=nb['x']+nb['width']/2; y=nb['y']+60
      전=await pg.evaluate("s=>document.querySelector(s).getBoundingClientRect().top", 띠sel)
      if 종류=='마우스':
        await pg.mouse.move(x,y); await pg.mouse.down()
        for k in range(1,13): await pg.mouse.move(x,y+10*k); await pg.wait_for_timeout(16)
      else:
        await pg.mouse.move(x,y); await pg.wait_for_timeout(300)
        for _ in range(2): await pg.mouse.wheel(0,-100); await pg.wait_for_timeout(50)
      중=await pg.evaluate("s=>[document.querySelector(s).getBoundingClientRect().top, document.querySelector(s).getBoundingClientRect().bottom, document.querySelector('.당김표')?.getBoundingClientRect().top]", 띠sel)
      if 종류=='마우스': await pg.mouse.up()
      await pg.wait_for_timeout(1000)
      n=await pg.evaluate("window.당김새로고침||0")
      ok(f'② {이름}', 중[0]==전 and 중[2] is not None and 중[2]>=중[1]-0.6 and n==1 and await pg.evaluate("!U.루틴열림"), {'띠 top 전/중':[전,중[0]], '띠 bottom':중[1], '빈줄 top':중[2], '새로고침':n})
    await 다른당김('루틴 마우스 끌기 — 띠 그대로 · 루틴 안 열림', '루틴', '#폰 .루틴목록띠', '마우스')
    await 다른당김('설정 휠 위로 2번 — 띠 그대로', '설정', '#폰 .설정띠', '휠')
    ok('오류 없음', not err, err)
    print(f"\n{sum(결과)}/{len(결과)} 통과")
    await b.close()
asyncio.run(main())
