"""v22 D 확인 — 운동 보고서: 13-1 열릴 때 자동 저장 · 다시 끝내면 덮어씀 · 13-2 단추 셋 없음 · 13-3 떠 있는 ‹ ›
쓰는 법: python3 _v22D.py HTML 사진폴더   (389×860 · 사진 → 사진폴더/d1~d4.png)
처음 화면(빈 저장소)에서 시험용 루틴 하나를 S 에 넣고(evaluate) 운동을 시작한 뒤, 큰 주 단추(.운주)만 눌러 마지막 세트까지 끝낸다."""
import asyncio, sys, json, os
from playwright.async_api import async_playwright
HTML=os.path.abspath(sys.argv[1]); 사진=os.path.abspath(sys.argv[2]); os.makedirs(사진, exist_ok=True)
결과={'통과':0,'실패':[]}
def 봄(이름, 조건, 값=None):
  if 조건: 결과['통과']+=1
  else: 결과['실패'].append(f"{이름} → {값}")
  print(('  ✓ ' if 조건 else '  ✗ ')+이름+('' if 값 is None else f"  {json.dumps(값,ensure_ascii=False)[:300]}"))

# 시험 루틴 — 벤치프레스(플랜 p1 · 1세트로 줄임) + 맨 끝 인클라인(일반 · 볼륨 자동 올리기 확인용) + 칸 6개(목록이 넘치게)
넣기 = """()=>{ S.설정.볼륨켬=true; S.설정.볼륨언제="항상"; S.설정.볼륨배분="횟수";
  const 하나=(이름,w,r)=>({이름, 세트:[{w,r,휴:30}]});
  S.루틴들.push({id:"rt", 이름:"시험 루틴", 자동생성:false, 휴식일:false, 종목:[{이름:"벤치프레스", 플랜id:"p1", 세트:[]},
    하나("오버헤드 프레스",30,8), 하나("사이드 레터럴 레이즈",8,12), 하나("바벨 컬",30,10), 하나("트라이셉스 푸시다운",25,12), 하나("랫풀다운",50,12), 하나("레그 프레스",120,12),
    {이름:"인클라인 벤치프레스", 세트:[{w:50,r:10,휴:30},{w:50,r:10,휴:30}]}]});
  운동시작("rt"); S.세션.종목[0].세트=S.세션.종목[0].세트.slice(0,1); 그리기(); }"""
상태 = """()=>({세션:!!S.세션, 끝:S.세션?.끝화면??null, 기록수:Object.keys(S.기록).filter(k=>!S.기록[k].예시).length, 키들:Object.keys(S.기록).filter(k=>!S.기록[k].예시),
  저장키:S.세션?.저장?.key??null, 한회:S.플랜들.find(p=>p.id==="p1").한회, 측정수:S.플랜들.find(p=>p.id==="p1").측정들.length,
  인클:S.루틴들.find(r=>r.id==="rt")?.종목.at(-1).세트.map(t=>t.r), 향상:S.향상기록들.length, 탭:U.탭, 스탯:U.스탯&&U.스탯.보기,
  세트수:(k=>k?S.기록[k].종목.reduce((a,e)=>a+e.세트.length,0):null)(Object.keys(S.기록).filter(k=>!S.기록[k].예시).sort().at(-1)),
  토스트:document.querySelector('#폰 .토스트')?.textContent??null})"""
ㅁ = """sel=>{ const e=document.querySelector(sel); if(!e) return null; const r=e.getBoundingClientRect(); return {l:+r.left.toFixed(1),t:+r.top.toFixed(1),r:+r.right.toFixed(1),b:+r.bottom.toFixed(1),w:+r.width.toFixed(1),h:+r.height.toFixed(1)}; }"""

async def 끝까지(pg):   # 큰 주 단추만 눌러 '운동 마무리' → 보고서
  for n in range(80):
    글=await pg.evaluate("document.querySelector('#폰 .운주')?.getAttribute('aria-label')")
    await pg.click('#폰 .운주'); await pg.wait_for_timeout(60)
    if 글=="운동 마무리": return n
  return None
async def 찍기(pg, 이름):
  await pg.wait_for_timeout(2600)   # 들어옴 · 숫자 올라감 끝까지
  await (await pg.query_selector('#폰')).screenshot(path=os.path.join(사진,이름))
async def 새쪽(b):
  pg=await b.new_page(viewport={'width':389,'height':860}); err=[]; pg.on('pageerror',lambda e:err.append(str(e)))
  await pg.goto('file://'+HTML); await pg.wait_for_timeout(500); await pg.evaluate(넣기); await pg.wait_for_timeout(200)
  return pg, err

async def 본시험(b):
  print('\n════ 389×860 · 끝내기 → 보고서 → ‹ → 세트 더 → 다시 끝내기 → › ════')
  pg,err=await 새쪽(b)
  처음=await pg.evaluate(상태)
  봄('시작 — 세션 있음 · 시험 기록 0', 처음['세션'] and 처음['기록수']==0, 처음)
  n=await 끝까지(pg); await pg.wait_for_timeout(150)
  a=await pg.evaluate(상태)
  봄('13-1 마지막 세트 → [운동 마무리] → 보고서 열림 · 세션은 아직 있음', n is not None and a['세션'] and a['끝'] is True, [n,a['세션'],a['끝']])
  봄('13-1 보고서가 열리는 순간 저장 — 기록 1개 · 세션이 그 키를 기억', a['기록수']==1 and a['저장키']==a['키들'][0], a)
  봄('13-1 토스트 "저장했습니다"', a['토스트']=="저장했습니다", a['토스트'])
  봄('13-1 플랜 p1 회차 +1 · 인클라인 횟수 +1(볼륨 자동) — 한 번만', a['한회']==처음['한회']+1 and a['인클']==[11,11], [처음['한회'],a['한회'],a['인클']])
  x=await pg.evaluate("""(()=>({옛:[...document.querySelectorAll('#폰 [data-act="운동저장"],#폰 [data-act="운동버림"],#폰 .결과셋,#폰 .결과아래')].length,
    글:[...document.querySelectorAll('#폰 button')].some(b=>/저장하고 종료|기록없이|기록 없이 종료/.test(b.textContent)),
    왼:document.querySelector('#폰 .화면>.보고떠.왼')?.dataset.act, 오:document.querySelector('#폰 .화면>.보고떠.오')?.dataset.act,
    왼꼴:(e=>{ const c=getComputedStyle(e); return [c.borderRadius,c.position,c.backgroundColor,c.borderTopColor,c.borderTopWidth]; })(document.querySelector('#폰 .보고떠.왼')),
    svg:[...document.querySelectorAll('#폰 .보고떠 svg')].map(s=>s.getBoundingClientRect().width)}))()""")
  봄('13-2 단추 셋(돌아가기 · 저장하고 종료 · 기록 없이 종료)과 그 줄 없음', x['옛']==0 and not x['글'], x)
  봄('13-3 왼쪽 ‹ = 운동으로 · 오른쪽 › = 보고스탯', x['왼']=='운동으로' and x['오']=='보고스탯', [x['왼'],x['오']])
  봄('13-3 동그라미(50%) · absolute · 바탕 --면 · 테 1px --속선 · 화살표 18', x['왼꼴'][0]=='50%' and x['왼꼴'][1]=='absolute' and x['왼꼴'][2]=='rgb(255, 255, 255)' and x['왼꼴'][3]=='rgb(181, 201, 218)' and x['왼꼴'][4]=='1px' and x['svg']==[18,18], x)
  await pg.wait_for_timeout(2600)   # 칸 들어옴(translateY) 움직임이 끝난 뒤 잰다
  y=await pg.evaluate(f"""(()=>{{ const ㅁ={ㅁ}; const 화=ㅁ('#폰 .화면'), 왼=ㅁ('#폰 .보고떠.왼'), 오=ㅁ('#폰 .보고떠.오'), 탭=ㅁ('#폰 .탭줄'), l=document.querySelector('#폰 .보고목록');
    const 앞=[ㅁ('#폰 .보고떠.왼'),ㅁ('#폰 .보고떠.오')]; const 넘침=l.scrollHeight-l.clientHeight; l.scrollTop=l.scrollHeight; l.dispatchEvent(new Event('scroll'));
    const 뒤=[ㅁ('#폰 .보고떠.왼'),ㅁ('#폰 .보고떠.오')], 칸=[...l.querySelectorAll('.보고칸')].map(e=>e.getBoundingClientRect().bottom), 끝칸=Math.max(...칸);
    return {{화, 왼, 오, 탭, 넘침, 앞, 뒤, 끝칸, 목록아래:l.getBoundingClientRect().bottom, 패딩:getComputedStyle(l).paddingBottom, 위:document.elementFromPoint(왼.l+20,왼.t+20)?.closest('.보고떠')?.dataset.act}}; }})()""")
  봄('13-3 지름 40 · 왼쪽/오른쪽 12 · 아래 12(탭줄 위)', y['왼']['w']==40 and y['왼']['h']==40 and y['오']['w']==40 and abs(y['왼']['l']-y['화']['l']-12)<=0.5 and abs(y['화']['r']-y['오']['r']-12)<=0.5 and abs(y['화']['b']-y['왼']['b']-12)<=0.5 and y['화']['b']<=y['탭']['t']+0.5, y)
  봄('13-3 목록을 끝까지 넘겨도 단추 제자리(목록이 넘치는 상태에서)', y['넘침']>0 and y['앞']==y['뒤'], [y['넘침'],y['앞'],y['뒤']])
  봄('13-3 끝까지 넘기면 마지막 칸 아래끝 ≤ 단추 위 − 8 (가리지 않음) · 아래 여백 52', y['끝칸']<=y['왼']['t']-8+0.5 and y['패딩']=='52px', [y['끝칸'],y['왼']['t'],y['패딩']])
  봄('13-3 단추 자리를 누르면 단추가 받는다', y['위']=='운동으로', y['위'])
  await pg.evaluate("document.querySelector('#폰 .보고목록').scrollTop=0")
  await 찍기(pg,'d1_보고서.png')
  await pg.evaluate("(()=>{ const l=document.querySelector('#폰 .보고목록'); l.scrollTop=l.scrollHeight; })()")
  await pg.wait_for_timeout(200); await (await pg.query_selector('#폰')).screenshot(path=os.path.join(사진,'d2_보고서_끝까지.png'))

  # ‹ → 운동 → 세트 더 → 다시 끝
  await pg.click('#폰 .보고떠.왼'); await pg.wait_for_timeout(200)
  b1=await pg.evaluate(상태)
  봄('13-3 ‹ → 운동 화면으로(끝화면 false · 세션 그대로 · 기록 1개 그대로)', b1['세션'] and b1['끝'] is False and b1['기록수']==1 and b1['저장키']==a['저장키'], b1)
  await pg.click('#폰 [data-act="세트더"]'); await pg.wait_for_timeout(150)
  n2=await 끝까지(pg); await pg.wait_for_timeout(150)
  c=await pg.evaluate(상태)
  봄('13-1 다시 끝내면 기록은 여전히 1개 · 같은 키', n2 is not None and c['기록수']==1 and c['키들']==a['키들'] and c['저장키']==a['저장키'], [n2,c['키들'],a['키들']])
  봄('13-1 같은 기록이 새 내용으로 — 세트 수 +1', c['세트수']==a['세트수']+1, [a['세트수'],c['세트수']])
  봄('13-1 덮어써도 플랜 회차 +1 그대로(+2 아님) · 인클라인 11 그대로(12 아님) · 향상기록 수 그대로', c['한회']==a['한회'] and c['인클']==a['인클'] and c['향상']==a['향상'], [c['한회'],c['인클'],c['향상'],a['향상']])
  await 찍기(pg,'d3_다시끝냄.png')

  # › → 스탯
  await pg.click('#폰 .보고떠.오'); await pg.wait_for_timeout(300)
  d=await pg.evaluate(상태)
  화=await pg.evaluate("({판:!!document.querySelector('#폰 .스탯아래'), 켬:document.querySelector('#폰 .스탯아래 .켬')?.textContent, 결과:!!document.querySelector('#폰 .결과틀')})")
  봄('13-3 › → 세션 닫힘 · 스탯 화면(스탯 칩 켬) · 기록 1개 그대로', not d['세션'] and d['스탯']=='스탯' and 화['판'] and 화['켬']=='스탯' and not 화['결과'] and d['기록수']==1, [d,화])
  await 찍기(pg,'d4_스탯.png')
  봄('pageerror 없음', not err, err); await pg.close()

async def 곁시험(b):
  print('\n════ 곁가지 ════')
  # 체크 없이 끝내기 → 저장 안 함
  pg,err=await 새쪽(b)
  await pg.click('#폰 .운끝내기'); await pg.wait_for_timeout(150)
  a=await pg.evaluate(상태)
  봄('체크 없이 [오늘 운동 끝내기] → 저장 안 함 · 토스트 "체크한 세트가 없어…"', a['기록수']==0 and a['끝'] is True and a['저장키'] is None and (a['토스트'] or '').startswith('체크한 세트가 없어'), a)
  await pg.click('#폰 .보고떠.왼'); await pg.wait_for_timeout(100)
  n=await 끝까지(pg); a=await pg.evaluate(상태)
  봄('그 뒤 다 하고 끝내면 그때 저장 1개', a['기록수']==1, a)
  # ‹ 로 돌아가 체크를 전부 풀고 다시 끝내면 → 앞서 저장한 것도 되돌림
  한=a['한회']
  await pg.click('#폰 .보고떠.왼'); await pg.wait_for_timeout(100)
  await pg.evaluate("S.세션.종목.forEach(e=>e.세트.forEach(s=>s.완료=false)); S.세션.휴식=null; 그리기();")
  await pg.click('#폰 .운끝내기'); await pg.wait_for_timeout(150)
  c=await pg.evaluate(상태)
  봄('저장 뒤 체크를 다 풀고 다시 끝내면 기록 0 · 플랜 회차 되돌림 · 인클라인 10 되돌림', c['기록수']==0 and c['한회']==한-1 and c['인클']==[10,10], [c['기록수'],c['한회'],한,c['인클']])
  봄('pageerror 없음 (곁 1)', not err, err); await pg.close()
  # 보고서에서 탭을 누르면 — 이미 저장됨, 세션만 닫힘(기록 두 개 안 생김)
  pg,err=await 새쪽(b)
  await 끝까지(pg); await pg.click('#폰 .탭줄 [data-t="캘린더"]'); await pg.wait_for_timeout(150)
  a=await pg.evaluate(상태)
  봄('보고서에서 탭(캘린더) → 세션 닫힘 · 기록 1개(두 번 저장 안 함)', not a['세션'] and a['기록수']==1 and a['탭']=='캘린더', a)
  봄('pageerror 없음 (곁 2)', not err, err); await pg.close()

async def main():
  async with async_playwright() as p:
    b=await p.chromium.launch()
    await 본시험(b); await 곁시험(b)
    await b.close()
  print(f"\n통과 {결과['통과']} · 실패 {len(결과['실패'])}")
  for f in 결과['실패']: print('  ✗', f)
asyncio.run(main())
