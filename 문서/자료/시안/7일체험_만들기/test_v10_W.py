"""v10 W 운동 세트 줄 시험 (✎ 29cb · j7s1) — python3 test_v10_W.py [파일]  기본 7day-v10.html · 사진은 r10/"""
import asyncio, sys, json, os
from playwright.async_api import async_playwright
SP=os.path.dirname(os.path.abspath(__file__)); R=SP+'/r10/'; os.makedirs(R, exist_ok=True)
F='file://'+SP+'/'+(sys.argv[1] if len(sys.argv)>1 else '7day-v10.html')
상태=json.load(open(SP+'/marks3/28f858e336f21768f689d786826649de.json'))   # 가슴·어깨 · 막 시작한 운동
async def main():
  결과=[]
  def 봄(m,ok): 결과.append((m,bool(ok))); print(('✅ ' if ok else '❌ ')+m)
  async with async_playwright() as p:
    b=await p.chromium.launch()
    for 테마 in ['light','dark']:
      pg=await b.new_page(viewport={'width':420,'height':860}); 오류=[]; pg.on('pageerror',lambda e:오류.append(str(e)))
      await pg.goto(F); await pg.wait_for_timeout(600); t=f"[{테마}] "
      js=pg.evaluate
      await js("([s,th])=>{ document.documentElement.dataset.theme=th; S=JSON.parse(JSON.stringify(s.S)); Object.assign(U, s.U, {시트:null, 업적띠:null}); U.본=0; 그리기(); }", [상태, 테마])
      await pg.wait_for_timeout(300)
      색=lambda v: f"""(()=>{{ const d=document.createElement('i'); d.style.color=getComputedStyle(document.documentElement).getPropertyValue('{v}'); document.body.appendChild(d); const x=getComputedStyle(d).color; d.remove(); return x; }})()"""
      강조, 강조글, 면2, 좋음옅음 = [await js(색(v)) for v in ['--강조','--강조글','--면2','--좋음옅음']]
      # 체크 전 — 번호 굵기
      r=await js("""(()=>{ const b=document.querySelector('#폰 .운세트들 .세트번호:not(.켬)'); const z=getComputedStyle(b); return {글:b.textContent.trim(), 굵기:z.fontWeight, 크기:z.fontSize}; })()""")
      봄(t+f"j7s1 동그라미 안 번호 보통 굵기 ({r})", r['굵기']=='400' and r['글'].isdigit())
      # 1세트 체크
      await pg.click('#폰 .운세트들 [data-act="체크"][data-k="0"]'); await pg.wait_for_timeout(500)
      await js("(()=>{ if(U.업적띠){ U.업적띠=null; 그리기(); } })()")
      r=await js("""(()=>{ const 줄=document.querySelectorAll('#폰 .운세트들 .세트줄')[0], 원=줄.querySelector('.세트번호'), z=getComputedStyle(원);
        const 휴=줄.querySelector('.휴칸'), 쉼=휴?.querySelector('.쉼단추'), 더=[...휴.querySelectorAll('button[data-act="세트값"]')];
        const hr=휴.getBoundingClientRect(), sr=쉼?쉼.getBoundingClientRect():null, hz=getComputedStyle(휴);
        return {켬:원.classList.contains('켬'), 바탕:z.backgroundColor, 테:z.borderTopColor, 글:z.color, svg:!!원.querySelector('svg'),
          줄바탕:getComputedStyle(줄).backgroundColor, 쉼:휴.classList.contains('쉼'), 더보임:더.map(x=>getComputedStyle(x).display),
          칸폭:hr.width-parseFloat(hz.borderLeftWidth)-parseFloat(hz.borderRightWidth)-parseFloat(hz.paddingLeft)-parseFloat(hz.paddingRight), 게이지폭:sr?sr.width:0,
          남은글:쉼?.querySelector('.위 b')?.textContent||'' }; })()""")
      봄(t+f"j7s1 체크 동그라미 = 강조 바탕 · 강조글 ✓ ({r['바탕']} / {r['글']})", r['켬'] and r['svg'] and r['바탕']==강조 and r['테']==강조 and r['글']==강조글)
      봄(t+f"29cb 끝난 줄 바탕 = --면2 (연초록 아님) ({r['줄바탕']})", r['줄바탕']==면2 and r['줄바탕']!=좋음옅음)
      봄(t+f"j7s1 쉬는 동안 − ＋ 사라짐 ({r['더보임']})", r['쉼'] and r['더보임']==['none','none'])
      봄(t+f"j7s1 게이지가 칸 전체 (칸 {r['칸폭']:.1f} · 게이지 {r['게이지폭']:.1f} · '{r['남은글']}')", r['게이지폭']>0 and abs(r['칸폭']-r['게이지폭'])<=1)
      # 쉼 끝나면 − ＋ 는 끝난 줄 규칙대로(감춤) · 지금 줄은 그대로 ±
      r=await js("""(()=>{ const 줄=document.querySelectorAll('#폰 .운세트들 .세트줄')[1]; return [...줄.querySelectorAll('.휴칸 button[data-act="세트값"]')].map(x=>getComputedStyle(x).display+'/'+getComputedStyle(x).visibility); })()""")
      봄(t+f"다음(지금) 줄 휴식 ± 는 그대로 보임 ({r})", all(x.split('/')[0]!='none' and x.split('/')[1]=='visible' for x in r))
      await pg.screenshot(path=R+f'운동_{테마}.png', clip=await js("(()=>{ const r=document.getElementById('폰').getBoundingClientRect(); return {x:r.x,y:r.y,width:r.width,height:r.height}; })()"))
      # 건너뛰기 → 쉼 끝
      await pg.click('#폰 .쉼단추'); await pg.wait_for_timeout(300)
      r=await js("""(()=>{ const 줄=document.querySelectorAll('#폰 .운세트들 .세트줄')[0]; return {쉼:!!줄.querySelector('.휴칸.쉼'), 바탕:getComputedStyle(줄).backgroundColor, 더:[...줄.querySelectorAll('.휴칸 button[data-act="세트값"]')].map(x=>getComputedStyle(x).visibility)}; })()""")
      봄(t+f"건너뛴 뒤 끝난 줄: ± 감춤 · 바탕 --면2 ({r})", not r['쉼'] and r['바탕']==면2 and r['더']==['hidden','hidden'])
      봄(t+"페이지 오류 없음 "+str(오류[:2]), not 오류)
      await pg.close()
    await b.close()
  ok=sum(1 for _,x in 결과 if x); print(f"통과 {ok}/{len(결과)}")
asyncio.run(main())
