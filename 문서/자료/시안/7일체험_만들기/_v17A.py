import asyncio, sys, json
from playwright.async_api import async_playwright
SP='/tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'
HTML=sys.argv[1] if len(sys.argv)>1 else SP+'7day-v17A.html'
상태=json.load(open(SP+'marks3/28f858e336f21768f689d786826649de.json'))
import os; os.makedirs(SP+'r17',exist_ok=True)
재기 = """(()=>{ const 목=document.querySelector('#폰 .운세트들'), 줄=[...목.querySelectorAll('.세트줄')];
  const R=el=>{const r=el.getBoundingClientRect(); return [Math.round(r.left),Math.round(r.right),Math.round(r.top),Math.round(r.bottom),Math.round(r.height)]};
  return 줄.map(z=>({cls:z.className.replace(/\\s+/g,' ').trim(), 줄:R(z), 바탕:getComputedStyle(z).backgroundColor,
    칸:[...z.querySelectorAll(':scope>.값칸,:scope>.쉼게이지')].map(R), pm:z.querySelectorAll('button[data-act="세트값"]').length,
    글:z.querySelector('.쉼게이지')?.querySelector('.밑').innerText.replace(/\\n/g,' / ')})); })()"""
async def main():
  async with async_playwright() as p:
    b=await p.chromium.launch(); pg=await b.new_page(viewport={'width':389,'height':860}); err=[]; pg.on('pageerror',lambda e:err.append(str(e)))
    await pg.goto('file://'+HTML); await pg.wait_for_timeout(600)
    await pg.evaluate("s=>{ S=JSON.parse(JSON.stringify(s.S)); Object.assign(U,s.U,{시트:null,업적띠:null}); U.본=0; 그리기(); }", 상태); await pg.wait_for_timeout(300)
    폰=await pg.query_selector('#폰')
    전=await pg.evaluate(재기); print('체크 전 1줄', 전[0])
    await pg.evaluate("행동('체크',{i:'0',k:'0'}); 시계그리기()"); await pg.wait_for_timeout(400)
    후=await pg.evaluate(재기)
    print('체크 후 1줄(쉼)', 후[0]); print('2줄(지금)', 후[1])
    g=후[0]['칸'][0]; k2=후[1]['칸']; print('게이지 높이', g[4], '왼쪽', g[0], '= kg칸 왼쪽', k2[0][0], '/ 오른쪽', g[1], '= 휴식칸 오른쪽', k2[2][1], '· 줄 높이 전/후', 전[0]['줄'][4], 후[0]['줄'][4])
    await 폰.screenshot(path=SP+'r17/A_쉼.png')
    await pg.click('#폰 .쉼게이지'); await pg.wait_for_timeout(300)
    x=await pg.evaluate(재기); print('건너뛴 뒤 1줄', x[0]['cls'], '칸', len(x[0]['칸']), 'pm', x[0]['pm'], '바탕', x[0]['바탕'], '휴식', await pg.evaluate("S.세션.휴식"))
    # 마지막 세트
    await pg.evaluate("for(const k of [1,2,3]){ 행동('체크',{i:'0',k:String(k)}); } 행동('휴식건너뛰기',{}); 행동('체크',{i:'0',k:'4'}); 시계그리기()"); await pg.wait_for_timeout(400)
    y=await pg.evaluate(재기); print('마지막 쉼', y[4]['글'], y[4]['칸'], 'U.본', await pg.evaluate("U.본"))
    print('완료줄 바탕', y[0]['바탕'], '· 지금줄 바탕(참고 --강조옅음)', await pg.evaluate("getComputedStyle(document.documentElement).getPropertyValue('--강조옅음')"))
    # + 세트 사이 빈 칸
    gap=await pg.evaluate("""(()=>{ const 목=document.querySelector('#폰 .운세트들'), 줄=[...목.querySelectorAll('.세트줄')], 더=목.querySelector('[data-act=세트더]').closest('.줄');
      const a=줄[줄.length-1].getBoundingClientRect(), p=줄[줄.length-2].getBoundingClientRect(), c=더.getBoundingClientRect(); return {줄간격:Math.round(a.top-p.top), 마지막아래_더위:Math.round(c.top-a.bottom), 더위_마지막위:Math.round(c.top-a.top)}; })()""")
    print('+ 세트 자리', gap)
    await pg.evaluate("document.querySelector('#폰 .운세트들').scrollTop=9999"); await pg.wait_for_timeout(200)
    await 폰.screenshot(path=SP+'r17/A_마지막.png')
    await pg.click('#폰 .쉼게이지'); await pg.wait_for_timeout(300)
    print('누른 뒤 U.본', await pg.evaluate("[U.본, S.세션.종목[U.본].이름, S.세션.휴식]"))
    # 다크
    await pg.evaluate("document.documentElement.dataset.theme='dark'"); await pg.wait_for_timeout(100)
    print('다크 --완료바탕', await pg.evaluate("getComputedStyle(document.documentElement).getPropertyValue('--완료바탕')"))
    await pg.evaluate("delete document.documentElement.dataset.theme")
    # ⑤ 종목 탭
    await pg.evaluate("U.탭='종목'; U.종목펼침='인클라인 벤치프레스'; 그리기()"); await pg.wait_for_timeout(300)
    await pg.evaluate("document.querySelector('#폰 .종설')?.scrollIntoView({block:'center'})")
    print('기본 세팅 처음', await pg.evaluate("[...document.querySelectorAll('#폰 .종설줄 .값칸')].map(c=>(c.querySelector('input')?.value??c.querySelector('span').textContent))"))
    for f,d,n in [('세트','1',2),('w','1',5),('r','-1',2),('휴','1',2)]:
      for _ in range(n): await pg.click(f'#폰 .종설줄 [data-act="종목설정값"][data-f="{f}"][data-d="{d}"]'); await pg.wait_for_timeout(60)
    print('누른 뒤', await pg.evaluate("[...document.querySelectorAll('#폰 .종설줄 .값칸')].map(c=>(c.querySelector('input')?.value??c.querySelector('span').textContent))"), await pg.evaluate("JSON.stringify(S.종목설정)"))
    inp=await pg.query_selector('#폰 .종설줄 input[data-f="w"]'); await inp.fill('102.5'); await inp.dispatch_event('change'); await pg.wait_for_timeout(200)
    print('102.5 넣음', await pg.evaluate("JSON.stringify(S.종목설정)"), '칸 넘침', await pg.evaluate("[...document.querySelectorAll('#폰 .종설줄 input,#폰 .종설줄 span')].map(e=>[e.scrollWidth,e.clientWidth])"))
    print('칸 폭', await pg.evaluate("[...document.querySelectorAll('#폰 .종설줄 .값칸')].map(c=>Math.round(c.getBoundingClientRect().width)+'x'+Math.round(c.getBoundingClientRect().height))"))
    await pg.evaluate("document.querySelector('#폰 .종설')?.scrollIntoView({block:'center'})"); await pg.wait_for_timeout(100)
    await 폰.screenshot(path=SP+'r17/A_종목.png')
    # 운동 화면에 넣기
    await pg.evaluate("U.탭='운동'; 그리기(); 행동('시트',{t:'종목넣기',v:'운동'}); 행동('종목넣기',{v:'인클라인 벤치프레스'}); U.시트=null; 그리기()")
    print('운동에 넣은 세트', await pg.evaluate("JSON.stringify(S.세션.종목.at(-1))"))
    await pg.evaluate("U.루틴열림=S.루틴들[0].id; U.탭='루틴'; 그리기(); 행동('시트',{t:'종목넣기'}); 행동('종목넣기',{v:'인클라인 벤치프레스'}); U.시트=null; 그리기()")
    print('루틴에 넣은 세트', await pg.evaluate("JSON.stringify(S.루틴들[0].종목.at(-1))"))
    print('설정 없는 종목', await pg.evaluate("행동('시트',{t:'종목넣기'}); 행동('종목넣기',{v:'바벨 컬'}); JSON.stringify(S.루틴들[0].종목.at(-1).세트)"))
    print('오류', err)
    await b.close()
asyncio.run(main())
