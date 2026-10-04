import asyncio, sys, json
from playwright.async_api import async_playwright
SP='/tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'
HTML=sys.argv[1] if len(sys.argv)>1 else SP+'7day-v18A.html'
상태=json.load(open(SP+'marks3/28f858e336f21768f689d786826649de.json'))
from PIL import Image
def 틈(path):
  """게이지 사진(2배)에서 채운 쪽 세로 한 줄(x=24)을 위→아래로 — 강조색이 아닌 줄 수 (위아래 바깥 줄 2개 빼고)"""
  im=Image.open(path).convert('RGB'); w,h=im.size; col=[im.getpixel((24,y)) for y in range(h)]
  강=col[h//2]; 안=[y for y in range(h) if col[y]==강]; a,b=min(안),max(안)
  return {'강조':강,'채움 첫·끝 줄':(a,b),'사진 높이':h,'채움 안 다른 색 줄':[(y,col[y]) for y in range(a,b+1) if col[y]!=강],'채움 밖':[(y,col[y]) for y in range(h) if y<a or y>b]}
재기 = """(()=>{ const 목=document.querySelector('#폰 .운세트들'), 줄=[...목.querySelectorAll('.세트줄')];
  return 줄.map(z=>{ const r=z.getBoundingClientRect(), b=getComputedStyle(z,'::before'); const g=z.querySelector('.쉼게이지');
    return {cls:z.className.replace(/\\s+/g,' ').trim(), 줄높이:r.height, 바탕:getComputedStyle(z).backgroundColor, 띠:b.content==='none'?null:[b.top,b.bottom,b.height,b.backgroundColor],
      글:g?g.querySelector('.밑 small').textContent:null, 글넘침:g?[g.querySelector('.밑 small').scrollWidth,g.querySelector('.밑 small').clientWidth]:null, act:g?.dataset.act}; }); })()"""
async def main():
  async with async_playwright() as p:
    b=await p.chromium.launch(); pg=await b.new_page(viewport={'width':389,'height':860}, device_scale_factor=2); err=[]; pg.on('pageerror',lambda e:err.append(str(e)))
    await pg.goto('file://'+HTML); await pg.wait_for_timeout(600)
    await pg.evaluate("s=>{ S=JSON.parse(JSON.stringify(s.S)); Object.assign(U,s.U,{시트:null,업적띠:null}); U.본=0; 그리기(); }", 상태); await pg.wait_for_timeout(300)
    폰=await pg.query_selector('#폰')
    print('종목 수', await pg.evaluate("S.세션.종목.map(x=>x.이름+' '+x.세트.length)"))
    # ① ② 첫 세트 체크 → 쉼
    await pg.evaluate("행동('체크',{i:'0',k:'0'}); 시계그리기()"); await pg.wait_for_timeout(400)
    for z in await pg.evaluate(재기): print(' ', z)
    print('--완료바탕', await pg.evaluate("getComputedStyle(document.documentElement).getPropertyValue('--완료바탕')"))
    g=await pg.query_selector('#폰 .쉼게이지'); await g.screenshot(path=SP+'r18/A_게이지2배.png'); print('② 게이지(밝음)', 틈(SP+'r18/A_게이지2배.png'))
    print('게이지 위치', await pg.evaluate("(()=>{const g=document.querySelector('#폰 .쉼게이지'),r=g.getBoundingClientRect(),cs=getComputedStyle(g);return [r.top,r.height,cs.borderTopWidth,cs.boxShadow]})()"))
    # 여러 소수점 위치에서도 — 목록을 0.25px 씩 밀어 본다
    for d in ['0.25','0.5','0.75']:
      await pg.evaluate(f"document.querySelector('#폰 .운세트들').style.paddingTop='calc(8px + {d}px)'"); await pg.wait_for_timeout(100)
      await g.screenshot(path=SP+'r18/_g.png'); t=틈(SP+'r18/_g.png'); print('  +'+d+'px', '다른 색 줄', t['채움 안 다른 색 줄'], '밖', t['채움 밖'])
    await pg.evaluate("document.querySelector('#폰 .운세트들').style.paddingTop=''")
    await 폰.screenshot(path=SP+'r18/A_완료줄.png')
    # 다크
    await pg.evaluate("document.documentElement.dataset.theme='dark'"); await pg.wait_for_timeout(150)
    print('다크 --완료바탕', await pg.evaluate("getComputedStyle(document.documentElement).getPropertyValue('--완료바탕')"), '띠', (await pg.evaluate(재기))[0]['띠'])
    await g.screenshot(path=SP+'r18/_gd.png'); t=틈(SP+'r18/_gd.png'); print('② 게이지(다크) 다른 색 줄', t['채움 안 다른 색 줄'], '밖', t['채움 밖'])
    await pg.evaluate("delete document.documentElement.dataset.theme"); await pg.wait_for_timeout(100)
    # ④ 끝내기 글씨색
    print('④ 운동 끝내기', await pg.evaluate("(()=>{const b=document.querySelector('#폰 .아랫줄 .운끝내기'); return [b.textContent, getComputedStyle(b).color, getComputedStyle(document.documentElement).getPropertyValue('--나쁨')]})()"))
    # ③ 모든 종목의 모든 세트를 끝낸다 — 마지막 종목 마지막 세트는 화면에서 눌러서
    await pg.evaluate("""(()=>{ const ss=S.세션; ss.휴식=null; ss.종목.forEach((x,i)=>x.세트.forEach((s,k)=>{ s.완료=true; })); const L=ss.종목.length-1, K=ss.종목[L].세트.length-1; ss.종목[L].세트[K].완료=false; ss.지금={i:L,k:K}; U.본=L; 그리기(); })()"""); await pg.wait_for_timeout(300)
    L=await pg.evaluate("S.세션.종목.length-1"); K=await pg.evaluate("S.세션.종목.at(-1).세트.length-1")
    await pg.click(f'#폰 [data-act="체크"][data-i="{L}"][data-k="{K}"]'); await pg.wait_for_timeout(400)
    await pg.evaluate("시계그리기()"); await pg.wait_for_timeout(100)
    print('③ 마지막 체크 뒤 휴식', await pg.evaluate("JSON.stringify(S.세션.휴식)"))
    z=[x for x in await pg.evaluate(재기) if x['글']]; print('③ 게이지 줄', z)
    await pg.evaluate("document.querySelector('#폰 .운세트들').scrollTop=9999"); await pg.wait_for_timeout(200)
    await 폰.screenshot(path=SP+'r18/A_마무리.png')
    g2=await pg.query_selector('#폰 .쉼게이지'); await g2.screenshot(path=SP+'r18/A_마무리게이지2배.png')
    await pg.click('#폰 .쉼게이지'); await pg.wait_for_timeout(400)
    print('③ 누른 뒤', await pg.evaluate("[S.세션?.끝화면, S.세션?.휴식, !!document.querySelector('#폰 .결과틀')]"))
    await 폰.screenshot(path=SP+'r18/_보고서.png')
    # ③ 회귀 — 남은 종목이 있을 때 마지막 세트는 여전히 '건너뛰고 다음 운동으로 넘어가기'
    await pg.evaluate("s=>{ S=JSON.parse(JSON.stringify(s.S)); Object.assign(U,s.U,{시트:null,업적띠:null}); U.탭='운동'; U.본=0; 그리기(); }", 상태); await pg.wait_for_timeout(300)
    await pg.evaluate("(()=>{ const x=S.세션.종목[0]; x.세트.forEach((s,k)=>{ if(k<x.세트.length-1) s.완료=true; }); 그리기(); 행동('체크',{i:'0',k:String(x.세트.length-1)}); 그리기(); 시계그리기(); })()"); await pg.wait_for_timeout(300)
    print('회귀 넘김', [x for x in await pg.evaluate(재기) if x['글']])
    print('오류', err)
    await b.close()
asyncio.run(main())
