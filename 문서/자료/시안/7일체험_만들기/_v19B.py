# v19 B 확인 — 보고서 띠 카메라 · 공유, 이미지 한 장(종목 칸 전부).  python3 _v19B.py [html]
import asyncio, re, sys, json, os
from playwright.async_api import async_playwright
from PIL import Image
SP='/tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'
HTML=sys.argv[1] if len(sys.argv)>1 else SP+'7day-v19B.html'
R=SP+'r19/'; os.makedirs(R, exist_ok=True)
src=open(SP+'test_v10_R.py',encoding='utf-8').read()
준비=re.search(r'준비JS = """(.*?)"""', src, re.S).group(1)
색=re.search(r'색JS = """(.*?)"""', src, re.S).group(1)
H2C='https://cdnjs.cloudflare.com/ajax/libs/html2canvas/1.4.1/html2canvas.min.js'
# 종목 10개 이상 · 모든 세트 완료
열개 = """(()=>{ const ss=S.세션; [['레그 프레스',150,10],['레그 컬',40,12],['카프 레이즈',60,15]].forEach(([n,w,r])=>ss.종목.push({이름:n, 세트:세트들(3,w,r,60).map(s=>({...s,목r:s.r,완료:false}))}));
  ss.종목.forEach(e=>e.세트.forEach(s=>s.완료=true)); S.설정.닉네임='홍겸'; S.설정.큰운동추가=['오버헤드 프레스','바벨 로우']; ss.끝화면=true; 그리기(); return ss.종목.length; })()"""
측정 = """(()=>{ const 틀=document.querySelector('#폰 .결과틀'), 목=틀.querySelector('.보고목록'), 띠=틀.querySelector('.보고띠');
  const t=틀.getBoundingClientRect(), m=목.getBoundingClientRect(), z=getComputedStyle(틀);
  const 찍=[...띠.querySelectorAll('.보고찍기')].map(b=>{ const q=b.getBoundingClientRect(), d=띠.getBoundingClientRect(), s=b.querySelector('svg').getBoundingClientRect();
    return {자리:b.dataset.shot, w:q.width, h:q.height, 왼:Math.round(q.left-d.left), 오:Math.round(d.right-q.right), 아이콘:s.width, 색:getComputedStyle(b).color===__색('--강조글'), 이름:b.getAttribute('aria-label')}; });
  return {종목칸:목.querySelectorAll('.보고칸').length, 목록보임:목.clientHeight, 목록전체:목.scrollHeight, 스크롤필요:목.scrollHeight>목.clientHeight+1,
    기대높이:Math.round((m.top-t.top)+목.scrollHeight+parseFloat(z.paddingBottom)), 폭:Math.round(t.width), 넘김단추:document.querySelectorAll('.보고넘김').length, 찍, 날:띠.dataset.day}; })()"""
async def 띄움(pg, 폭=389):
    await pg.set_viewport_size({'width':폭,'height':900})
    await pg.goto('file://'+HTML); await pg.evaluate("localStorage.clear()"); await pg.reload(); await pg.wait_for_timeout(500)
    await pg.evaluate(색); await pg.evaluate(준비); await pg.wait_for_timeout(100)
    n=await pg.evaluate(열개); await pg.wait_for_timeout(2300); return n
async def 내려받음(pg, sel, 이름):
    async with pg.expect_download(timeout=15000) as d: await pg.click(sel)
    dl=await d.value; p=R+이름; await dl.save_as(p); return dl.suggested_filename, p
def 크기(p): im=Image.open(p); return im.size
async def main():
  async with async_playwright() as p:
    b=await p.chromium.launch(); out={}
    for 테마 in ('light','dark'):
      ctx=await b.new_context(viewport={'width':389,'height':900}, color_scheme=테마, accept_downloads=True); pg=await ctx.new_page(); err=[]
      pg.on('pageerror',lambda e:err.append(str(e)))
      await ctx.route(H2C, lambda r: r.fulfill(path=SP+'h2c.min.js', content_type='application/javascript'))
      t=테마[0]
      out[t+'종목수']=await 띄움(pg)
      m=await pg.evaluate(측정); out[t+'측정']=m
      await (await pg.query_selector('#폰')).screenshot(path=R+f'B_보고서_{테마}.png')
      if 테마=='light': await (await pg.query_selector('.보고띠')).screenshot(path=R+'B_띠.png')
      # 카메라 → 내려받기
      이름, 경로 = await 내려받음(pg, '.보고찍기[data-shot="저장"]', f'B_이미지_{테마}.png')
      await pg.wait_for_timeout(100)
      w,h=크기(경로); out[t+'카메라']={'파일':이름, '크기':[w,h], '높이/2':h/2, '기대높이':m['기대높이'], '폭/2':w/2, '토스트':await pg.evaluate("document.querySelector('.토스트')?.innerText||null"),
        '복제본남음':await pg.evaluate("!!document.querySelector('.보고찍틀')"), '단추풀림':await pg.evaluate("[...document.querySelectorAll('.보고찍기')].every(b=>!b.disabled)")}
      if 테마=='light':
        # 단추줄 · 탭줄이 이미지 아래쪽에 없는지 — 마지막 30px 줄에 단추 주색(--강조) 덩어리가 없는지 색 세기
        im=Image.open(경로).convert('RGB'); 강=await pg.evaluate("__바('--강조')"); rgb=tuple(int(x) for x in re.findall(r'\d+',강)[:3])
        아래=[im.getpixel((x,y)) for y in range(h-80,h) for x in range(0,w,4)]
        out['아래80px_강조색점']=sum(1 for c in 아래 if sum(abs(c[i]-rgb[i]) for i in range(3))<30)
        # 두 번 눌림 — 빠르게 두 번 → 내려받기 한 번
        dls=[]; pg.on('download', lambda d: dls.append(d.suggested_filename))
        await pg.evaluate("document.querySelector('.보고찍기[data-shot=\"저장\"]').click(); document.querySelector('.보고찍기[data-shot=\"저장\"]').click(); 1")
        await pg.wait_for_timeout(3000); out['두번눌림_내려받기수']=len(dls)
        # 공유 — 이 크로미움의 canShare
        out['canShare있음']=await pg.evaluate("typeof navigator.canShare")
        dls.clear(); await pg.click('.보고찍기[data-shot="공유"]'); await pg.wait_for_function("document.querySelector('.토스트')", timeout=10000)
        out['공유_토스트1']=await pg.evaluate("document.querySelector('.토스트')?.innerText||null")
        await pg.wait_for_timeout(2500); out['공유_대신저장']=dls[:]; out['공유_토스트2']=await pg.evaluate("document.querySelector('.토스트')?.innerText||null")
        # 공유 되는 기기 흉내 — share 에 넘긴 것 확인
        await pg.evaluate("""window.__공={}; navigator.canShare=d=>!!(d&&d.files&&d.files.length); navigator.share=async d=>{ window.__공={이름:d.files[0].name, 종류:d.files[0].type, 크기:d.files[0].size, 제목:d.title}; }; 1""")
        dls.clear(); await pg.click('.보고찍기[data-shot="공유"]'); await pg.wait_for_timeout(2000)
        out['공유됨']=await pg.evaluate("window.__공"); out['공유됨_내려받기']=dls[:]
        # 공유 창을 닫음(AbortError) → 아무것도 안 함
        await pg.evaluate("""navigator.share=async d=>{ const e=new Error('x'); e.name='AbortError'; throw e; }; document.querySelectorAll('.토스트').forEach(x=>x.remove()); 1""")
        dls.clear(); await pg.click('.보고찍기[data-shot="공유"]'); await pg.wait_for_timeout(2000)
        out['공유취소']={'내려받기':dls[:], '토스트':await pg.evaluate("document.querySelector('.토스트')?.innerText||null")}
        # 막힘(NotAllowedError) → 저장으로
        await pg.evaluate("""navigator.share=async d=>{ const e=new Error('x'); e.name='NotAllowedError'; throw e; }; 1""")
        dls.clear(); await pg.click('.보고찍기[data-shot="공유"]'); await pg.wait_for_timeout(2500)
        out['공유막힘']=dls[:]
        # claude.ai downloads 기능 흉내 — save 에 넘긴 것
        await pg.evaluate("""window.__저={}; window.claude={use:async n=> n==='downloads'?{save:async q=>{ window.__저={이름:q.filename, 크기:q.data.size}; return {status:'saved'}; }}:null}; 1""")
        dls.clear(); await pg.click('.보고찍기[data-shot="저장"]'); await pg.wait_for_timeout(2000)
        out['claude저장']=await pg.evaluate("window.__저"); out['claude저장_a내려받기']=dls[:]
        await 띄움(pg); await pg.evaluate("""window.claude={use:async n=>({save:async q=>{ throw {code:'declined', message:'no'}; }})}; 1""")
        await pg.click('.보고찍기[data-shot="저장"]'); await pg.wait_for_timeout(2000)
        out['claude거절_토스트']=await pg.evaluate("document.querySelector('.토스트')?.innerText||null")
        # 칸 하나 펼친 보고서 + 360 폭
        await 띄움(pg, 360); await pg.click('.보고칸 >> nth=4'); await pg.wait_for_timeout(600)
        await pg.evaluate("document.querySelector('.보고목록').scrollTop=9999; 1")
        m2=await pg.evaluate(측정); 이름2, 경로2 = await 내려받음(pg, '.보고찍기[data-shot="저장"]', 'B_이미지_360_펼침.png')
        out['360펼침']={'기대높이':m2['기대높이'], '크기':크기(경로2), '폭':m2['폭'], '찍':m2['찍']}
        # 저장된 보고서(캘린더에서 다시 보기) — 파일 이름 날짜 = 그 기록 날
        await 띄움(pg); await pg.evaluate("const k=운동저장하기(); S.결과={key:k}; U.탭='캘린더'; 그리기(); 1"); await pg.wait_for_timeout(2300)
        m3=await pg.evaluate(측정); 이름3, 경로3 = await 내려받음(pg, '.보고찍기[data-shot="저장"]', 'B_이미지_저장된보고서.png')
        out['저장된보고서']={'파일':이름3, '날':m3['날'], '기대높이':m3['기대높이'], '크기':크기(경로3), '단추':m3['찍']}
      out[t+'오류']=err; await ctx.close()
    print(json.dumps(out,ensure_ascii=False,indent=1))
    await b.close()
asyncio.run(main())
