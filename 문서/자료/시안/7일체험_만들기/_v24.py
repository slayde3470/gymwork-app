"""v24 확인 — ① 루틴 상자 이름 칸 7글자 폭 · 높이 5% 줄임 ② 종목 요약 칸 한 줄 · 화살표만
쓰는 법: python3 _v24.py HTML 사진폴더 [견줄_HTML]   (389×860 · 360×780)"""
import asyncio, sys, json, os
from playwright.async_api import async_playwright
HTML, 사진 = sys.argv[1], sys.argv[2]; 전HTML = sys.argv[3] if len(sys.argv)>3 else None; os.makedirs(사진, exist_ok=True)
결과={'통과':0,'실패':[]}
def 봄(이름, 조건, 값=None):
  if 조건: 결과['통과']+=1
  else: 결과['실패'].append(f"{이름} → {값}")
  print(('  ✓ ' if 조건 else '  ✗ ')+이름+('' if 값 is None else f"  {json.dumps(값,ensure_ascii=False)[:300]}"))
ㅁ = """window.ㅁ=sel=>{ const e=typeof sel==='string'?document.querySelector(sel):sel; if(!e) return null; const r=e.getBoundingClientRect(); return {l:+r.left.toFixed(1),t:+r.top.toFixed(1),r:+r.right.toFixed(1),b:+r.bottom.toFixed(1),w:+r.width.toFixed(1),h:+r.height.toFixed(1)}; }"""
넣기 = """()=>{ const 하나=(이름,w,r)=>({이름, 세트:[{w,r,휴:30},{w,r,휴:30}]});
  S.루틴들.push({id:"rt", 이름:"시험 루틴", 자동생성:false, 휴식일:false, 종목:[하나("벤치프레스",60,8), 하나("오버헤드 프레스",30,8), 하나("레그 프레스",160,12), 하나("인클라인 벤치프레스",50,10), 하나("팔굽혀펴기",0,20), 하나("데드리프트",140,5)]});
  운동시작("rt"); S.세션.종목.forEach(e=>e.세트.forEach(s=>s.완료=true)); 그리기(); }"""

async def 보고서(b, html, W, H):
  pg=await b.new_page(viewport={'width':W,'height':H}); err=[]; pg.on('pageerror',lambda e:err.append(str(e)))
  await pg.goto('file://'+html); await pg.wait_for_timeout(600); await pg.evaluate(넣기); await pg.evaluate(ㅁ)
  for n in range(40):
    글=await pg.evaluate("document.querySelector('#폰 .운주')?.getAttribute('aria-label')")
    if not 글: break
    await pg.click('#폰 .운주'); await pg.wait_for_timeout(60)
    if 글=="운동 마무리": break
  await pg.wait_for_timeout(2600); return pg, err

async def 한폭(b, W, H):
  print(f'\n════ {W}×{H} ════')
  전높=None
  if 전HTML:
    pg,_=await 보고서(b, 전HTML, W, H); 전높=await pg.evaluate("ㅁ('#폰 .보고루틴').h"); await pg.close()
  pg,err=await 보고서(b, HTML, W, H)
  x=await pg.evaluate("""(()=>{ const 루=document.querySelector('#폰 .보고루틴'), 머=루.querySelector('.보고루틴머리'), 수=루.querySelector('.결과수'), 이=루.querySelector('.보고루틴이름');
    const 잼=document.createElement('b'); 잼.className='보고루틴이름'; 잼.style.cssText='position:absolute;visibility:hidden'; 잼.textContent='가나다라마바사'; 머.appendChild(잼); const 일곱=잼.getBoundingClientRect().width; 잼.remove();
    return {루:ㅁ(루), 머:ㅁ(머), 수:ㅁ(수), 이:ㅁ(이), 일곱, pl:parseFloat(getComputedStyle(루).paddingLeft)}; })()""")
  봄(f'{W} ① 세로선 = 이름 시작 + 7글자 + 8 (±2)', abs(x['수']['l']-(x['머']['l']+x['일곱']+8))<=2, {'선':x['수']['l'],'이름시작':x['머']['l'],'7글자':round(x['일곱'],1)})
  if 전높: 봄(f'{W} ① 상자 높이 5% 이상 줄어듦 ({전높} → {x["루"]["h"]})', x['루']['h']<=전높*0.95+0.1, [전높, x['루']['h']])
  y=await pg.evaluate("""[...document.querySelectorAll('#폰 .보고칸')].map(c=>{ const 줄=[...c.querySelectorAll('.보고줄')], 한=c.querySelector('.보고한줄');
    return {이름:c.querySelector('.보고이름 b').textContent, 줄수:줄.length, 높:한?ㅁ(한).h:null, 넘침:한?한.scrollWidth>한.clientWidth+0.5:null, 칸넘침:c.scrollWidth>c.clientWidth+0.5,
      글:한?한.textContent.replace(/\\s+/g,' ').trim():'', 화살:c.querySelectorAll('.보고화살').length, 숫차:/[▲▼]\\s*[0-9]/.test(c.textContent)}; })""")
  print('   칸', [(v['이름'],v['글']) for v in y])
  봄(f'{W} ② 요약 칸마다 줄 1개(한 줄 높이 ≤ 20)', all(v['줄수']==1 and v['높'] and v['높']<=20 for v in y), [(v['이름'],v['줄수'],v['높']) for v in y])
  봄(f'{W} ② 넘치지 않음', not any(v['넘침'] or v['칸넘침'] for v in y), [(v['이름'],v['넘침'],v['칸넘침']) for v in y if v['넘침'] or v['칸넘침']])
  봄(f'{W} ② 1RM · 볼륨(맨몸은 최고 · 합계) 둘 다 있음', all(('1RM' in v['글'] and '볼륨' in v['글']) or ('최고' in v['글'] and '합계' in v['글']) for v in y), [v['글'] for v in y])
  봄(f'{W} ② 화살표 옆 숫자 없음', not any(v['숫차'] for v in y), [v['글'] for v in y if v['숫차']])
  await (await pg.query_selector('#폰')).screenshot(path=f'{사진}/{W}_보고서.png')
  # 누르면 상세에는 숫자 차이가 그대로
  await pg.click('#폰 .보고칸'); await pg.wait_for_timeout(900)
  봄(f'{W} ② 누르면 상세가 열린다', await pg.evaluate("!!document.querySelector('#폰 .보고상세')"))
  봄(f'{W} pageerror 없음', not err, err)
  await pg.close()

async def main():
  async with async_playwright() as p:
    b=await p.chromium.launch()
    await 한폭(b,389,860); await 한폭(b,360,780)
    await b.close()
  print(f"\n통과 {결과['통과']} · 실패 {len(결과['실패'])}")
  for f in 결과['실패']: print('  ✗', f)
asyncio.run(main())
