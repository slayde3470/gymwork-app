"""v23 확인 — ② 보고서 루틴 한 줄 ③ ‹ › 30 · 굵게 ④ 이미지 찍기에 ‹ › · 카메라 · 공유 없음 ⑤ 루틴 [+ 종목 추가] 오른쪽 아래 · 자동 왼쪽 아래
쓰는 법: python3 _v23.py HTML 사진폴더   (389×860 · 360×780)"""
import asyncio, sys, json, os
from playwright.async_api import async_playwright
HTML, 사진 = sys.argv[1], sys.argv[2]; os.makedirs(사진, exist_ok=True)
결과={'통과':0,'실패':[]}
def 봄(이름, 조건, 값=None):
  if 조건: 결과['통과']+=1
  else: 결과['실패'].append(f"{이름} → {값}")
  print(('  ✓ ' if 조건 else '  ✗ ')+이름+('' if 값 is None else f"  {json.dumps(값,ensure_ascii=False)[:260]}"))
ㅁ = """window.ㅁ=sel=>{ const e=typeof sel==='string'?document.querySelector(sel):sel; if(!e) return null; const r=e.getBoundingClientRect(); return {l:+r.left.toFixed(1),t:+r.top.toFixed(1),r:+r.right.toFixed(1),b:+r.bottom.toFixed(1),w:+r.width.toFixed(1),h:+r.height.toFixed(1)}; }"""
넣기 = """()=>{ const 하나=(이름,w,r)=>({이름, 세트:[{w,r,휴:30}]});
  S.루틴들.push({id:"rt", 이름:"시험 루틴", 자동생성:false, 휴식일:false, 종목:[하나("벤치프레스",60,8), 하나("오버헤드 프레스",30,8), 하나("사이드 레터럴 레이즈",8,12), 하나("바벨 컬",30,10),
    하나("트라이셉스 푸시다운",25,12), 하나("랫풀다운",50,12), 하나("레그 프레스",120,12), 하나("인클라인 벤치프레스",50,10), 하나("데드리프트",100,5), 하나("백 스쿼트",80,5)]}); 그리기(); }"""

async def 한폭(b, W, H):
  print(f'\n════ {W}×{H} ════')
  pg=await b.new_page(viewport={'width':W,'height':H}); err=[]; pg.on('pageerror',lambda e:err.append(str(e)))
  await pg.goto('file://'+HTML); await pg.wait_for_timeout(600); await pg.evaluate(넣기); await pg.evaluate(ㅁ)
  # ⑤ 루틴 상세
  await pg.evaluate("행동('탭',{t:'루틴'}); 행동('루틴열기',{v:'rt'})"); await pg.wait_for_timeout(400)
  await pg.evaluate("document.querySelector('#폰 .루넘김').scrollTop=99999"); await pg.wait_for_timeout(300)
  x=await pg.evaluate("""(()=>({추:ㅁ('#폰 .루추가뜸'), 자:ㅁ('#폰 .루자동뜸'), 화:ㅁ('#폰 .화면'), 탭:ㅁ('#폰 .탭줄'), 안추가:document.querySelectorAll('#폰 .루넘김 [data-t="종목넣기"]').length,
    끝:ㅁ([...document.querySelectorAll('#폰 .루격자>.종목상자')].pop())}))()""")
  봄(f'{W} ⑤ 목록 안 [+ 종목 추가] 없음', x['안추가']==0, x['안추가'])
  봄(f'{W} ⑤ [+ 종목 추가] 오른쪽 아래 12 · 자동 왼쪽 아래 12', x['추'] and abs(x['화']['r']-x['추']['r']-12)<1 and abs(x['자']['l']-x['화']['l']-12)<1 and abs(x['추']['b']-x['자']['b'])<1, [x['추'],x['자']])
  봄(f'{W} ⑤ 둘이 겹치지 않음', x['자']['r']+8<=x['추']['l'], [x['자']['r'],x['추']['l']])
  봄(f'{W} ⑤ 끝까지 내려도 마지막 상자가 단추 위', x['끝']['b']<=x['추']['t'], [x['끝']['b'],x['추']['t']])
  await (await pg.query_selector('#폰')).screenshot(path=f'{사진}/{W}_루틴끝.png')
  await pg.click('#폰 .루추가뜸'); await pg.wait_for_timeout(400)
  봄(f'{W} ⑤ 누르면 종목 넣기 시트', await pg.evaluate("U.시트?.종류")=="종목넣기", await pg.evaluate("U.시트?.종류"))
  await pg.evaluate("U.시트=null; 그리기()")
  # ②③ 보고서
  await pg.evaluate("행동('루틴닫기',{}); 운동시작('rt'); S.세션.종목.forEach(e=>e.세트.forEach(s=>s.완료=true)); 그리기()"); await pg.wait_for_timeout(300)
  for n in range(40):
    글=await pg.evaluate("document.querySelector('#폰 .운주')?.getAttribute('aria-label')")
    if not 글: break
    await pg.click('#폰 .운주'); await pg.wait_for_timeout(60)
    if 글=="운동 마무리": break
  await pg.wait_for_timeout(2600)
  x=await pg.evaluate("""(()=>{ const 루=document.querySelector('#폰 .보고루틴'), 머=루.querySelector('.보고루틴머리'), 수=루.querySelector('.결과수'), 이=루.querySelector('.보고루틴이름');
    const 떠=[...document.querySelectorAll('#폰 .보고떠')].map(e=>({...ㅁ(e), sw:getComputedStyle(e.querySelector('svg')).strokeWidth}));
    return {루:ㅁ(루), 머:ㅁ(머), 수:ㅁ(수), 선:getComputedStyle(수).borderLeftWidth, 이름넘침:이.scrollWidth>이.clientWidth, 이름:이.textContent, 떠, 화:ㅁ('#폰 .화면')}; })()""")
  봄(f'{W} ② 루틴 이름과 수치가 한 줄(세로 가운데가 같은 높이)', abs((x['머']['t']+x['머']['b'])/2-(x['수']['t']+x['수']['b'])/2)<3 and x['머']['r']<=x['수']['l']+0.5, [x['머'],x['수']])
  봄(f'{W} ② 사이 세로선 1px', x['선']=='1px', x['선'])
  봄(f'{W} ② 상자 높이 줄어듦(≤ 60)', x['루']['h']<=60, x['루']['h'])
  봄(f'{W} ② 이름 안 잘림', not x['이름넘침'], x['이름'])
  봄(f'{W} ③ ‹ › 30 × 30 · 선 3', len(x['떠'])==2 and all(abs(d['w']-30)<0.6 and abs(d['h']-30)<0.6 and d['sw']=='3px' for d in x['떠']), x['떠'])
  봄(f'{W} ③ 왼쪽 아래 · 오른쪽 아래 12', abs(x['떠'][0]['l']-x['화']['l']-12)<1 and abs(x['화']['r']-x['떠'][1]['r']-12)<1, x['떠'])
  await (await pg.query_selector('#폰')).screenshot(path=f'{사진}/{W}_보고서.png')
  # ④ 이미지 찍기 틀 — 찍는 함수가 만드는 틀과 같은 방법으로 복제해 무엇이 보이는지 본다
  x=await pg.evaluate("""(()=>{ const 원=document.querySelector('#폰 .결과틀'), 틀=document.createElement('div'); 틀.className='폰 번호끔 보고찍틀'; 틀.style.width=원.getBoundingClientRect().width+'px';
    const 복=원.cloneNode(true); [...복.children].forEach(c=>{ if(!c.matches('.보고띠, .보고상자, .보고목록')) c.remove(); }); 틀.appendChild(복); document.body.appendChild(틀);
    const 보임=sel=>[...틀.querySelectorAll(sel)].filter(e=>{ const cs=getComputedStyle(e); return cs.display!=='none'&&cs.visibility!=='hidden'; }).length;
    const r={떠:보임('.보고떠'), 카메라공유:보임('.보고찍기'), 톱니:보임('.톱니단추'), 루틴:보임('.보고루틴')}; 틀.remove(); return r; })()""")
  봄(f'{W} ④ 찍는 틀에 ‹ › · 카메라 · 공유 · 톱니 안 보임 (루틴 상자는 보임)', x['떠']==0 and x['카메라공유']==0 and x['톱니']==0 and x['루틴']==1, x)
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
