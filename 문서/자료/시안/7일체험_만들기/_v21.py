"""v21 확인 — ① 종목 빼기 ✕ · ② 운동 중 새 종목 만들기 · ③ 칸 줄 ‹ › · ④ 단추 넷 · ⑤ 소셜 탭 · ⑥ 시트 손잡이 · 끌어 닫기 · ⑦ 점멸
쓰는 법: python3 _v21.py [html 절대경로]   (사진 → SP/r21/*.png 4장)"""
import asyncio, sys, json, os
from playwright.async_api import async_playwright
SP='/tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'
HTML=sys.argv[1] if len(sys.argv)>1 else SP+'7day-v21.html'
상태=json.load(open(SP+'marks3/28f858e336f21768f689d786826649de.json'))
os.makedirs(SP+'r21',exist_ok=True)
결과={'통과':0,'실패':[]}
def 봄(이름, 조건, 값=None):
  if 조건: 결과['통과']+=1
  else: 결과['실패'].append(f"{이름} → {값}")
  print(('  ✓ ' if 조건 else '  ✗ ')+이름+('' if 값 is None else f"  {json.dumps(값,ensure_ascii=False)[:260]}"))

준비 = """(s)=>{ S=JSON.parse(JSON.stringify(s.S)); Object.assign(U,s.U,{시트:null,업적띠:null,운지움:null}); 그리기(); }"""
도구 = """window.ㅁ = sel=>{ const e=typeof sel==='string'?document.querySelector(sel):sel; if(!e) return null; const r=e.getBoundingClientRect(); return {l:+r.left.toFixed(1),t:+r.top.toFixed(1),r:+r.right.toFixed(1),b:+r.bottom.toFixed(1),w:+r.width.toFixed(1),h:+r.height.toFixed(1)}; };
window.멈춤 = t=>document.getAnimations().forEach(a=>{ try{ if(!/깜빡$/.test(a.animationName)){ if(isFinite(a.effect.getComputedTiming().endTime)) a.finish(); return; }
  a.pause(); a.currentTime=t+(a.effect.getTiming().delay||0); }catch(_){} });   // 점멸만 멈춘다(늦춤(음수)을 빼고 주기 안 t ms) · 들어옴 같은 움직임은 끝낸다"""
async def 그림(pg, 이름):   # 점멸을 '켜진 쪽'(0)에 멈추고 찍는다
  await pg.evaluate("멈춤(0)"); await (await pg.query_selector('#폰')).screenshot(path=SP+'r21/'+이름)
async def 새로(pg):
  await pg.evaluate(준비, 상태); await pg.wait_for_timeout(300); await pg.evaluate(도구)

async def 한폭(b, W, H):
  print(f'\n════ {W}×{H} ════')
  pg=await b.new_page(viewport={'width':W,'height':H}); err=[]; pg.on('pageerror',lambda e:err.append(str(e)))
  await pg.goto('file://'+HTML); await pg.wait_for_timeout(600); await 새로(pg)

  # ── ④ 단추 넷 폭 · 모양 ──
  x=await pg.evaluate("""(()=>{ const 줄=document.querySelector('#폰 .운단추줄'), cs=getComputedStyle(줄), 안=줄.clientWidth-parseFloat(cs.paddingLeft)-parseFloat(cs.paddingRight);
    const 단=[...줄.children].map(e=>({글:e.innerText.replace(/\\n/g,'/'), 폭:+e.getBoundingClientRect().width.toFixed(2), 높:e.getBoundingClientRect().height, 끔:e.disabled}));
    const 끝=줄.querySelector('.운끝내기'); return {안, 틈:cs.columnGap, 단, 끝색:getComputedStyle(끝).color, 끝줄:줄세기(끝), 끝br:끝.innerHTML.includes('<br>')}; })()""")
  기대=[(x['안']-24)*p for p in (.125,.475,.275,.125)]
  print('   단추', [(d['글'],d['폭']) for d in x['단']], '기대', [round(v,2) for v in 기대], '줄 안 폭', x['안'])
  봄(f'{W} ④ 단추 넷 순서 = ‹ · 세트 완료하기 · 오늘 운동/끝내기 · ›', [d['글'] for d in x['단']]==['','세트 완료하기','오늘 운동/끝내기',''], [d['글'] for d in x['단']])
  봄(f'{W} ④ 폭 12.5 : 47.5 : 27.5 : 12.5 (틈 8 × 3 뺀 나머지 · ±0.5)', all(abs(d['폭']-v)<=0.5 for d,v in zip(x['단'],기대)) and x['틈']=='8px', [d['폭'] for d in x['단']])
  봄(f'{W} ④ 넷 + 틈 = 줄 안 폭', abs(sum(d['폭'] for d in x['단'])+24-x['안'])<=0.5, sum(d['폭'] for d in x['단'])+24)
  봄(f'{W} ④ 높이 40 · 오늘 운동<br>끝내기 두 줄 · --나쁨 글씨', all(d['높']==40 for d in x['단']) and x['끝br'] and x['끝줄']==2 and x['끝색']=='rgb(179, 38, 30)', [x['끝줄'],x['끝색']])
  봄(f'{W} ④ 처음 ‹ 꺼짐 · › 켜짐', x['단'][0]['끔'] and not x['단'][3]['끔'])
  봄(f'{W} ④ 옛 "다음 · 종목이름" 단추 없음', await pg.evaluate("![...document.querySelectorAll('#폰 .아랫줄 button')].some(b=>/다음 ·/.test(b.textContent))"))

  # ── ⑦ 점멸 ──
  a=await pg.evaluate("""(()=>{ const r=document.querySelector('#폰 .운세트들 .세트줄.지금'), c=document.querySelector('#폰 .운칸.지금'), d=document.querySelector('#폰 .운주 .노란점');
    const 애=e=>{ const s=getComputedStyle(e); return [s.animationName,s.animationDuration,s.animationIterationCount]; };
    멈춤(0); const 앞=[getComputedStyle(r).backgroundColor,getComputedStyle(c).backgroundColor,getComputedStyle(d).opacity,getComputedStyle(r.querySelector('input')).color,getComputedStyle(r).opacity];
    멈춤(500); const 가=[getComputedStyle(r).backgroundColor,getComputedStyle(c).backgroundColor,getComputedStyle(d).opacity,getComputedStyle(r.querySelector('input')).color,getComputedStyle(r).opacity];
    document.getAnimations().forEach(x=>x.play());
    const 점=ㅁ(d), 글=ㅁ('#폰 .운주글');
    return {줄:애(r), 칸:애(c), 점애:애(d), 앞, 가, 점, 글, 점색:getComputedStyle(d).backgroundColor}; })()""")
  print('   0ms', a['앞'], ' 500ms', a['가'])
  봄(f'{W} ⑦① 지금 세트 줄 · 지금 칸 = 지금깜빡 1s 무한', a['줄']==['지금깜빡','1s','infinite'] and a['칸']==['지금깜빡','1s','infinite'], [a['줄'],a['칸']])
  봄(f'{W} ⑦① 바탕 --강조옅음 ↔ --지금깜빡 (0 · 500ms)', a['앞'][0]=='rgb(225, 233, 240)' and a['가'][0]=='rgb(238, 243, 247)' and a['앞'][1]=='rgb(225, 233, 240)' and a['가'][1]=='rgb(238, 243, 247)', [a['앞'][:2],a['가'][:2]])
  봄(f'{W} ⑦① 글자색 · 투명도 그대로 (opacity 아님)', a['앞'][3]==a['가'][3] and a['앞'][4]==a['가'][4]=='1', [a['앞'][3:],a['가'][3:]])
  봄(f'{W} ⑦② 큰 단추 노란 점 6 × 6 · --노랑 · 1s 깜빡 · 글 왼쪽 틈 8', a['점']['w']==6 and a['점']['h']==6 and a['점색']=='rgb(232, 164, 0)' and a['점애']==['노란깜빡','1s','infinite'] and abs(a['글']['l']-a['점']['r']-8)<=0.5 and a['앞'][2]=='1' and float(a['가'][2])<0.2, [a['점'],a['글']['l'],a['앞'][2],a['가'][2]])

  # ── ① ✕ ──
  x=await pg.evaluate("""(()=>{ const 칸=document.querySelector('#폰 .운칸.지금'), 뺌=document.querySelectorAll('#폰 .운칸뺌'), b=뺌[0], g=b?.querySelector('svg'), R=ㅁ(칸), G=ㅁ(g), B=ㅁ(b);
    const 줄=document.querySelector('#폰 .운띠'), 보=ㅁ(줄);
    const rg=document.createRange(); rg.selectNodeContents(칸.querySelector('.ㅇ')); const 글끝=Math.max(...[...rg.getClientRects()].map(q=>q.right)), 글위=Math.min(...[...rg.getClientRects()].map(q=>q.top));
    const 중=[(G.l+G.r)/2,(G.t+G.b)/2], 밑=document.elementFromPoint(중[0],중[1]);
    return {수:뺌.length, 옆:b?.previousElementSibling===칸, B, G, R, 보위:보.t, 중, 맞:밑?.closest('.운칸뺌')===b, 색:getComputedStyle(b).color, 글끝, 글위, 원바탕:getComputedStyle(g).backgroundColor, 원테:getComputedStyle(g).boxShadow}; })()""")
  print('   ✕', json.dumps({k:x[k] for k in ('B','G','R','중','글끝')},ensure_ascii=False))
  봄(f'{W} ① ✕ 하나 · 지금 칸 바로 뒤', x['수']==1 and x['옆'])
  봄(f'{W} ① 누르는 칸 28 × 28 (줄 위 여백 8 밖은 잘림)', x['B']['w']==28 and x['B']['h']==28)
  봄(f'{W} ① 아이콘 14 (동그라미 16 안) · 가운데 = 칸 오른쪽 위 꼭짓점 ±1', x['G']['w']==16 and abs(x['중'][0]-x['R']['r'])<=1 and abs(x['중'][1]-x['R']['t'])<=1, [x['중'],[x['R']['r'],x['R']['t']]])
  봄(f'{W} ① 색 --옅음 · 동그라미 --면 · --선 테', x['색']=='rgb(94, 107, 119)' and x['원바탕']=='rgb(255, 255, 255)' and '211, 223, 233' in x['원테'], [x['색'],x['원바탕'],x['원테']])
  봄(f'{W} ① 동그라미가 줄 안에 다 보임(위 여백 8)', x['G']['t']>=x['보위']-0.5, [x['G']['t'],x['보위']])
  봄(f'{W} ① 아이콘 가운데를 누르면 ✕ 가 받는다', x['맞'])
  봄(f'{W} ① 이름 글자와 안 겹침(글 끝 ≤ 동그라미 왼쪽)', x['글끝']<=x['G']['l']+0.5, [x['글끝'],x['G']['l']])

  if W==389:
    # 세트 하나 체크(쉼) → ✕ → 아래띠 → 되돌리기
    await pg.click('#폰 .운주'); await pg.wait_for_timeout(250)
    y=await pg.evaluate("({휴:S.세션.휴식&&{i:S.세션.휴식.i,k:S.세션.휴식.k}, 지금:S.세션.지금, 주:document.querySelector('#폰 .운주글').textContent, 체:S.세션.종목[0].세트[0].완료})")
    봄('389 ④ [세트 완료하기] = 지금 종목 첫 세트 체크 → 쉼 · 단추 "건너뛰기"', y['체'] and y['휴']=={'i':0,'k':0} and y['주']=='건너뛰기', y)
    await pg.click('#폰 .운칸뺌'); await pg.wait_for_timeout(250)
    y=await pg.evaluate("""({n:S.세션.종목.length, 첫:S.세션.종목[0].이름, 본:U.본, 지금:S.세션.지금, 휴:S.세션.휴식, 띠:document.querySelector('#폰 .운지움띠')?.innerText, 칸:document.querySelector('#폰 .운칸.지금 .ㅇ').textContent, 뺌:document.querySelectorAll('#폰 .운칸뺌').length,
      띠R:ㅁ('#폰 .운지움띠'), 칸R:ㅁ('#폰 .운칸.지금')})""")
    봄('389 ① ✕ → 종목 9개 · 보는 칸 = 그 자리 종목(인클라인) · 지금 = 인클라인 1세트 · 그 종목 쉼 끝', y['n']==9 and y['첫']=='인클라인 벤치프레스' and y['본']==0 and y['지금']=={'i':0,'k':0} and y['휴'] is None, y)
    봄('389 ① 아래띠 "벤치프레스를 뺐습니다 / 되돌리기"', y['띠'] and y['띠'].replace('\n',' ').strip()=='벤치프레스를 뺐습니다 되돌리기', y['띠'])
    봄('389 ① 새 지금 칸에도 ✕', y['뺌']==1 and y['칸']=='인클라인 벤치프레스')
    await 그림(pg, '1_종목뺌띠_389.png')
    await pg.evaluate("document.getAnimations().forEach(a=>a.play())")
    await pg.click('#폰 .운지움띠 button'); await pg.wait_for_timeout(250)
    y=await pg.evaluate("""({n:S.세션.종목.length, 첫:S.세션.종목[0].이름, 체:S.세션.종목[0].세트.map(s=>s.완료), 본:U.본, 지금:S.세션.지금, 휴:S.세션.휴식&&{i:S.세션.휴식.i,k:S.세션.휴식.k}, 띠:!!document.querySelector('#폰 .운지움띠'), 주:document.querySelector('#폰 .운주글').textContent})""")
    봄('389 ① 되돌리기 → 10개 · 벤치프레스 맨 앞 · 체크한 1세트 그대로 · 지금 · 쉼 · 보는 칸 돌아옴', y['n']==10 and y['첫']=='벤치프레스' and y['체'][0] and not any(y['체'][1:]) and y['본']==0 and y['지금']=={'i':0,'k':1} and y['휴']=={'i':0,'k':0} and not y['띠'] and y['주']=='건너뛰기', y)
    # 가운데 종목 빼기 — 지금 · 쉼은 다른 종목(번호만 다시 맞춤)
    await pg.click('#폰 .운칸[data-i="2"]'); await pg.wait_for_timeout(600)
    await pg.click('#폰 .운칸뺌'); await pg.wait_for_timeout(250)
    y=await pg.evaluate("({이름:S.세션.종목.map(e=>e.이름).slice(0,4), 본:U.본, 지금:S.세션.지금, 휴:S.세션.휴식&&S.세션.휴식.i, 띠:document.querySelector('#폰 .운지움띠')?.innerText.split('\\n')[0]})")
    봄('389 ① 3번째(오버헤드 프레스) ✕ → 지금 · 쉼은 벤치프레스 그대로 · 보는 칸 = 그 자리(사이드)', y['이름'][2]=='사이드 레터럴 레이즈' and y['본']==2 and y['지금']=={'i':0,'k':1} and y['휴']==0 and y['띠']=='오버헤드 프레스를 뺐습니다', y)
    await pg.click('#폰 .운지움띠 button'); await pg.wait_for_timeout(250)
    y=await pg.evaluate("({이름:S.세션.종목.map(e=>e.이름).slice(0,4), 본:U.본, 휴:S.세션.휴식&&S.세션.휴식.i})")
    봄('389 ① 되돌리기 → 3번째 자리로', y['이름'][2]=='오버헤드 프레스' and y['본']==2 and y['휴']==0, y)
    y=await pg.evaluate("[을를('벤치프레스'),을를('풀업'),을를('랫 풀다운'),을를('Squat')]")
    봄('389 ① 을/를', y==['를','을','을','을(를)'], y)
    # 종목 하나면 ✕ 없음
    y=await pg.evaluate("(()=>{ const ss=S.세션, 원=ss.종목.slice(); ss.종목=[원[0]]; U.본=0; 그리기(); const n=document.querySelectorAll('#폰 .운칸뺌').length; ss.종목=원; 그리기(); return n; })()")
    봄('389 ① 종목이 하나면 ✕ 없음', y==0, y)
    # 종목이 적어 [＋]가 마지막 칸 바로 뒤일 때 · 마지막 칸을 볼 때 ✕ 가 [＋]에 안 가림 / 칸이 [＋] 밑으로 들어가면 ✕ 숨김
    y=await pg.evaluate("""(()=>{ const ss=S.세션, 원=ss.종목.slice(); ss.종목=원.slice(0,3); U.본=2; 그리기(); 멈춤(0);
      const g=document.querySelector('#폰 .운칸뺌 svg'), G=ㅁ(g), 더=ㅁ('#폰 .운더'), 밑=document.elementFromPoint(G.r-2,(G.t+G.b)/2)?.closest('.운칸뺌'), 숨1=getComputedStyle(document.querySelector('#폰 .운칸뺌')).visibility;
      ss.종목=원; U.본=5; 그리기(); const 줄=document.querySelector('#폰 .운띠'); 줄.scrollLeft=0; 운화살맞춤(); const 숨2=getComputedStyle(document.querySelector('#폰 .운칸뺌')).visibility;
      줄.scrollLeft=document.querySelector('#폰 .운칸.지금').offsetLeft-32; 운화살맞춤(); const 숨3=getComputedStyle(document.querySelector('#폰 .운칸뺌')).visibility; U.본=0; 그리기();
      document.getAnimations().forEach(a=>a.play()); return {G, 더l:더.l, 오른끝보임:!!밑, 숨1, 숨2, 숨3}; })()""")
    봄('389 ① 3종목 · 마지막 칸 볼 때 ✕ 동그라미 오른쪽 끝까지 보임([＋] 위)', y['오른끝보임'] and y['숨1']=='visible', y)
    봄('389 ① 칸이 [＋] 밑으로 들어가면 ✕ 숨김 · 돌아오면 보임', y['숨2']=='hidden' and y['숨3']=='visible', y)

  # ── ③ ‹ › ──
  await 새로(pg)
  x=await pg.evaluate("""(()=>{ const 줄=document.querySelector('#폰 .운띠'), 왼=document.querySelector('#폰 .운화살.왼'), 오=document.querySelector('#폰 .운화살.오'), cs=getComputedStyle(오);
    const 칸=ㅁ('#폰 .운칸.지금'), 더=ㅁ('#폰 .운더'), O=ㅁ(오);
    return {왼숨:왼.hidden, 오숨:오.hidden, O, 더, 칸중:(칸.t+칸.b)/2, 끝:줄.scrollWidth-줄.clientWidth, 폭:줄.clientWidth, 모양:[cs.width,cs.height,cs.borderRadius,cs.backgroundColor,cs.borderTopWidth+' '+cs.borderTopColor]}; })()""")
  print('   ›', x['O'], '[＋]', x['더'], x['모양'])
  봄(f'{W} ③ 처음(맨 앞) ‹ 숨김 · › 보임', x['왼숨'] and not x['오숨'], [x['왼숨'],x['오숨']])
  봄(f'{W} ③ › = [＋] 바로 왼쪽(틈 4) · 칸 높이 가운데', abs(x['O']['r']+4-x['더']['l'])<=0.5 and abs((x['O']['t']+x['O']['b'])/2-x['칸중'])<=0.5, [x['O']['r'],x['더']['l'],(x['O']['t']+x['O']['b'])/2,x['칸중']])
  봄(f'{W} ③ 동그라미 28 · --면 바탕 · --선 테 1', x['모양'][:4]==['28px','28px','50%','rgb(255, 255, 255)'] and x['모양'][4]=='1px rgb(211, 223, 233)', x['모양'])
  await pg.click('#폰 .운화살.오'); await pg.wait_for_timeout(700)
  y=await pg.evaluate("({옆:document.querySelector('#폰 .운띠').scrollLeft, 왼숨:document.querySelector('#폰 .운화살.왼').hidden, 오숨:document.querySelector('#폰 .운화살.오').hidden})")
  봄(f'{W} ③ › 누름 → 줄 폭 70% 이동 · ‹ 나타남', abs(y['옆']-x['폭']*0.7)<=1.5 and not y['왼숨'], [y['옆'], round(x['폭']*0.7,1)])
  await pg.click('#폰 .운화살.왼'); await pg.wait_for_timeout(700)
  y=await pg.evaluate("({옆:document.querySelector('#폰 .운띠').scrollLeft, 왼숨:document.querySelector('#폰 .운화살.왼').hidden})")
  봄(f'{W} ③ ‹ 누름 → 맨 앞 · ‹ 숨김', y['옆']<=1 and y['왼숨'], y)
  await pg.evaluate("(()=>{ const z=document.querySelector('#폰 .운띠'); z.scrollLeft=z.scrollWidth; })()"); await pg.wait_for_timeout(150)
  y=await pg.evaluate("({왼숨:document.querySelector('#폰 .운화살.왼').hidden, 오숨:document.querySelector('#폰 .운화살.오').hidden})")
  봄(f'{W} ③ 끝까지 밀면 › 숨김 · ‹ 보임', y['오숨'] and not y['왼숨'], y)
  await pg.evaluate("document.querySelector('#폰 .운띠').scrollLeft=0"); await pg.wait_for_timeout(150)
  # 마우스로 줄 끌기(손가락 밀기와 같은 scroll)
  c=await pg.evaluate("ㅁ('#폰 .운칸[data-i=\"2\"]')")
  await pg.mouse.move(c['l']+30,c['t']+40); await pg.mouse.down(); await pg.mouse.move(c['l']-60,c['t']+40,steps=6); await pg.mouse.up(); await pg.wait_for_timeout(150)
  y=await pg.evaluate("({옆:document.querySelector('#폰 .운띠').scrollLeft, 왼숨:document.querySelector('#폰 .운화살.왼').hidden, 본:U.본})")
  봄(f'{W} ③ 마우스 끌기로 밀어도 ‹ 나타남 (칸 고름 안 됨)', y['옆']>50 and not y['왼숨'] and y['본']==0, y)
  # 보는 칸을 맨 앞으로 당길 때 ‹ 에 안 가림
  await pg.evaluate("document.querySelector('#폰 .운띠').scrollLeft=0"); await pg.wait_for_timeout(100)
  await pg.click('#폰 .운칸[data-i="3"]'); await pg.wait_for_timeout(800)
  y=await pg.evaluate("({칸:ㅁ('#폰 .운칸.지금'), 왼:ㅁ('#폰 .운화살.왼'), 숨:document.querySelector('#폰 .운화살.왼').hidden})")
  봄(f'{W} ③ 4번째 칸을 누르면 맨 앞 · ‹(28) + 틈 4 뒤', not y['숨'] and abs(y['칸']['l']-(y['왼']['r']+4))<=1, [y['칸']['l'], y['왼']['r']])
  # 꾹 끌기 — 화살이 놓을 칸을 가리지 않고 순서가 바뀐다
  await pg.evaluate("document.querySelector('#폰 .운띠').scrollLeft=0"); await pg.wait_for_timeout(150)
  전=await pg.evaluate("S.세션.종목.map(e=>e.이름)")
  a1=await pg.evaluate("ㅁ('#폰 .운칸[data-i=\"1\"]')"); a3=await pg.evaluate("ㅁ('#폰 .운칸[data-i=\"2\"]')")
  await pg.mouse.move(a1['l']+36,a1['t']+50); await pg.mouse.down(); await pg.wait_for_timeout(520)
  await pg.mouse.move(a3['l']+60,a3['t']+50,steps=8); pe=await pg.evaluate("[getComputedStyle(document.querySelector('#폰 .운화살.오')).pointerEvents, !!document.querySelector('#폰 .끌림')]")
  await pg.mouse.up(); await pg.wait_for_timeout(200)
  후=await pg.evaluate("S.세션.종목.map(e=>e.이름)")
  봄(f'{W} ③ 꾹 끌기 중 ‹ › 누름 막힘 · 순서 바뀜', pe==['none',True] and 후[1]!=전[1] and sorted(후)==sorted(전), [pe, 전[1:4], 후[1:4]])

  # ── ④ 큰 단추 상태 ──
  await 새로(pg)
  async def 단():
    return await pg.evaluate("""(()=>{ const b=document.querySelector('#폰 .운주'), g=b.querySelector('.운주글'), 게=document.querySelector('#폰 .쉼게이지 .밑 small');
      return {글:g.textContent, act:b.dataset.act, fs:g.style.fontSize, 두줄:g.classList.contains('두줄'), 줄:줄세기(g), 넘:g.scrollWidth>g.clientWidth+0.5||g.scrollHeight>g.clientHeight+0.5, 게:게?.textContent||null, 본:U.본, 휴:!!S.세션.휴식, 점:!!b.querySelector('.노란점')}; })()""")
  상=[]
  z=await 단(); 상.append(z)
  for _ in range(4):   # 1~4세트: 체크 → 건너뛰기
    await pg.click('#폰 .운주'); await pg.wait_for_timeout(120); z1=await 단()
    await pg.click('#폰 .운주'); await pg.wait_for_timeout(120)
  상.append(z1)
  await pg.click('#폰 .운주'); await pg.wait_for_timeout(150); z2=await 단(); 상.append(z2)   # 5세트(마지막) 체크 → 쉬는 중
  if W==360:
    await pg.evaluate("document.getAnimations().forEach(a=>a.play())")
    await pg.evaluate("document.querySelector('#폰 .운띠').scrollLeft=120"); await pg.wait_for_timeout(200)
    await 그림(pg, '2_쉼넘김_360.png'); await pg.evaluate("document.getAnimations().forEach(a=>a.play())")
  await pg.click('#폰 .운주'); await pg.wait_for_timeout(200); z3=await 단(); 상.append(z3)                 # 다음 종목으로
  await pg.click('#폰 .운이전'); await pg.wait_for_timeout(200); z4=await 단(); 상.append(z4)               # 끝난 종목을 다시 봄(안 쉼)
  await pg.click('#폰 .운주'); await pg.wait_for_timeout(200); z5=await 단(); 상.append(z5)
  # 쉬는 중 + 다른(안 끝난) 종목을 봄 → 건너뛰기
  await pg.click('#폰 .운주'); await pg.wait_for_timeout(120); await pg.click('#폰 .운다음'); await pg.wait_for_timeout(200); z6=await 단(); 상.append(z6)
  for q in 상: print('   ', json.dumps(q,ensure_ascii=False))
  봄(f'{W} ④ 처음 = 세트 완료하기(체크) · 점', z['글']=='세트 완료하기' and z['act']=='체크' and z['점'], z)
  봄(f'{W} ④ 쉬는 중 = 건너뛰기(휴식건너뛰기) · 게이지 "건너뛰기"', z1['글']=='건너뛰기' and z1['act']=='휴식건너뛰기' and z1['게']=='건너뛰기', z1)
  봄(f'{W} ④ 이 종목 마지막 세트 뒤 쉬는 중 = 다음 종목으로 넘어가기 · 게이지 "건너뛰고 다음 운동으로 넘어가기"', z2['글']=='다음 종목으로 넘어가기' and z2['act']=='다음종목으로' and z2['게']=='건너뛰고 다음 운동으로 넘어가기' and z2['휴'], z2)
  봄(f'{W} ④ 누르면 다음 안 끝난 종목(2번째) · 쉼 끝 · 세트 완료하기', z3['본']==1 and not z3['휴'] and z3['글']=='세트 완료하기', z3)
  봄(f'{W} ④ 끝난 종목을 다시 보면(안 쉼) 다음 종목으로 넘어가기 → 누르면 2번째', z4['글']=='다음 종목으로 넘어가기' and z4['본']==0 and z5['본']==1, [z4['글'],z5['본']])
  봄(f'{W} ④ 쉬는 중 + 안 끝난 다른 종목을 봄 = 건너뛰기', z6['글']=='건너뛰기' and z6['본']==2 and z6['휴'], z6)
  봄(f'{W} ④ 글이 단추를 안 넘침(한 줄 15 → 13 → 두 줄 13 · 위아래 안 잘림)', all(not q['넘'] and q['줄']<=2 for q in 상), [(q['글'],q['fs'],q['줄']) for q in 상])
  # 운동 마무리 — 남은 세트 하나만 두고 체크
  await pg.evaluate("(()=>{ const ss=S.세션; ss.휴식=null; ss.종목.forEach((e,i)=>e.세트.forEach((s,k)=>{ s.완료=!(i===ss.종목.length-1&&k===e.세트.length-1); })); U.본=ss.종목.length-1; ss.지금={i:U.본,k:ss.종목[U.본].세트.length-1}; 그리기(); })()")
  await pg.wait_for_timeout(150); await pg.click('#폰 .운주'); await pg.wait_for_timeout(150); z7=await 단()
  봄(f'{W} ④ 마지막 종목 마지막 세트 뒤 = 운동 마무리(끝내기) · 게이지 "마무리하고 운동 보고서 화면으로 넘어가기"', z7['글']=='운동 마무리' and z7['act']=='끝내기' and z7['게']=='마무리하고 운동 보고서 화면으로 넘어가기' and not z7['넘'], z7)
  y=await pg.evaluate("({다음끔:document.querySelector('#폰 .운다음').disabled})")
  봄(f'{W} ④ 마지막 종목이면 › 꺼짐', y['다음끔'])
  await pg.click('#폰 .운주'); await pg.wait_for_timeout(250)
  봄(f'{W} ④ 운동 마무리 누름 → 운동 보고서', await pg.evaluate("S.세션.끝화면===true && !!document.querySelector('#폰 .보고')||S.세션.끝화면===true"))

  # ── ⑦② 쉼 게이지 노란 점 (세 문구 · 잘림 없음) ──
  await 새로(pg)
  g=[]
  for 꼴 in ['건너뛰기','넘김','마무리']:
    await pg.evaluate("""(꼴)=>{ const ss=S.세션; ss.종목.forEach(e=>e.세트.forEach(s=>s.완료=false)); U.본=0;
      if(꼴==='건너뛰기'){ ss.종목[0].세트[0].완료=true; }
      if(꼴==='넘김'){ ss.종목[0].세트.forEach(s=>s.완료=true); }
      if(꼴==='마무리'){ ss.종목.forEach(e=>e.세트.forEach(s=>s.완료=true)); }
      ss.휴식={i:0,k:0,끝:S.시계+90000,길이:90}; 그리기(); }""", 꼴)
    await pg.wait_for_timeout(120)
    g.append(await pg.evaluate("""(()=>{ const 게=document.querySelector('#폰 .쉼게이지'), 밑=게.querySelector('.밑'), 점=밑.querySelector('.노란점'), 글=밑.querySelector('.쉼글'), b=글.querySelector('b'), s=글.querySelector('small');
      const P=ㅁ(점), T=ㅁ(글), B=ㅁ(b), M=ㅁ(s);
      return {문구:s.textContent, 점수:게.querySelectorAll('.노란점').length, 점:[P.w,P.h], 점중:+((P.t+P.b)/2).toFixed(1), 두줄사이:+((B.b+M.t)/2).toFixed(1), 묶음중:+((T.t+T.b)/2).toFixed(1), 틈:+(Math.min(B.l,M.l)-P.r).toFixed(1), 잘림:s.scrollWidth>s.clientWidth+0.5, 자간:s.style.letterSpacing||'-', 게폭:ㅁ(게).w, 글폭:M.w,
        애:getComputedStyle(점).animationName, 색:getComputedStyle(점).backgroundColor, 위점:getComputedStyle(게.querySelector('.위 .노란점')).animationName}; })()"""))
  for q in g: print('   ', json.dumps(q,ensure_ascii=False))
  봄(f'{W} ⑦② 게이지 노란 점(밑 · 위 두 겹) 6 × 6 · --노랑 · 노란깜빡', all(q['점수']==2 and q['점']==[6,6] and q['색']=='rgb(232, 164, 0)' and q['애']==q['위점']=='노란깜빡' for q in g))
  봄(f'{W} ⑦② 점 = 두 줄 묶음 왼쪽(틈 8) · 세로는 두 줄 사이(윗줄 아랫변 ±0.5)', all(abs(q['틈']-8)<=0.5 and abs(q['점중']-q['두줄사이'])<=0.5 for q in g), [(q['점중'],q['두줄사이'],q['틈']) for q in g])
  봄(f'{W} ⑦② 세 문구 모두 안 잘림(…없음)', all(not q['잘림'] for q in g), [(q['문구'],q['잘림']) for q in g])

  print('   오류', err); 봄(f'{W} 페이지 오류 없음', not err, err)
  return pg

async def 탭시트(b):
  W,H=360,800
  print(f'\n════ {W}×{H} 탭줄 · 시트 ════')
  pg=await b.new_page(viewport={'width':W,'height':H}); err=[]; pg.on('pageerror',lambda e:err.append(str(e)))
  await pg.goto('file://'+HTML); await pg.wait_for_timeout(600); await 새로(pg)
  await pg.evaluate("S.세션=null; U.탭='캘린더'; 그리기()"); await pg.wait_for_timeout(200)
  x=await pg.evaluate("""(()=>{ const bs=[...document.querySelectorAll('#폰 .탭줄 > button')];
    return bs.map(b=>{ const s=b.querySelector('span:not(.탭사진)'), R=ㅁ(b), S_=s?ㅁ(s):null; return {t:b.dataset.t, 글:s?s.textContent:'', 폭:R.w, 글폭:S_?S_.w:0, 글l:S_?S_.l:null, 글r:S_?S_.r:null, l:R.l, r:R.r, fs:s?getComputedStyle(s).fontSize:null, 넘:s?s.scrollWidth>s.clientWidth+0.5:false}; }); })()""")
  print('   탭', [(q['글'] or q['t'], q['폭'], q['글폭']) for q in x])
  봄('⑤ 탭 9개 순서 = 캘린더 · 검색 · 루틴 · 종목 · 플랜 · 메모 · 소셜 · 설정 · 프로필', [q['t'] for q in x]==['캘린더','검색','루틴','종목','플랜','메모','소셜','설정','프로필'], [q['t'] for q in x])
  봄('⑤ 360 폭 글자 11 · 칸 안에 들어감 · 이웃 글자와 안 겹침', all(q['fs'] in (None,'11px') for q in x) and all(q['글l'] is None or (q['글l']>=q['l']-0.5 and q['글r']<=q['r']+0.5) for q in x)
     and all(x[i]['글r'] is None or x[i+1]['글l'] is None or x[i]['글r']<=x[i+1]['글l'] for i in range(len(x)-1)), [(q['글'],q['글l'],q['글r'],q['l'],q['r']) for q in x if q['글']])
  y=await pg.evaluate("(()=>{ const s=document.querySelector('#폰 .탭줄 [data-t=\"소셜\"] svg'); return {원:s.querySelectorAll('circle').length, 호:s.querySelector('path')?.getAttribute('d'), 굵:s.getAttribute('stroke-width'), 크:[ㅁ(s).w,ㅁ(s).h]}; })()")
  봄('⑤ 소셜 아이콘 = 머리 원 + 어깨 호 (선 2 · 18)', y['원']==1 and y['호'].startswith('M4 21a8 8') and y['굵']=='2' and y['크']==[18,18], y)
  await pg.click('#폰 .탭줄 [data-t="소셜"]'); await pg.wait_for_timeout(300)
  y=await pg.evaluate("""(()=>{ const 띠=document.querySelector('#폰 .소셜띠'), b=띠?.querySelector('b'), 넘=document.querySelector('#폰 .넘김');
    return {탭:U.탭, 켬:document.querySelector('#폰 .탭줄 [data-t="소셜"]').classList.contains('켬'), 글:b?.textContent, 가운데:b&&getComputedStyle(b).textAlign, 띠R:ㅁ(띠), 바탕:띠&&getComputedStyle(띠).backgroundColor, 안글:넘?.innerText.trim(), 안수:넘?.children.length}; })()""")
  봄('⑤ 소셜 화면 = 맨 위 띠 "소셜"(가운데 · --강조 · 높이 40) + 빈 화면', y['탭']=='소셜' and y['켬'] and y['글']=='소셜' and y['가운데']=='center' and y['띠R']['h']==40 and y['바탕']=='rgb(8, 75, 131)' and y['안글']=='' and y['안수']==0, y)
  await 그림(pg, '3_소셜_360.png')

  # ── ⑥ 시트 손잡이 — 여러 시트 ──
  await 새로(pg)
  오=await pg.evaluate("오늘()")
  종류들=[('메모',{}),('달고르기',{'해':2026}),('보고방식',{}),('링크',{}),('휴식',{'k':오}),('변경',{'k':오}),('루틴고르기',{'k':오}),('근육',{}),('종목넣기',{'대상':'운동'}),('새종목',{})]
  줄=[]
  for t,덧 in 종류들:
    r=await pg.evaluate("""([t,덧])=>{ U.시트=Object.assign({종류:t},덧); if(t==='종목넣기'){U.방금=[];U.칸고름='전체';} if(t==='새종목'){U.새=새초기();U.새종목='';} 그리기();
      const 시=document.querySelector('#폰 .시트'); if(!시) return {t, 없음:true}; const 막=시.querySelector('.시트손잡이'), M=ㅁ(막), 시R=ㅁ(시), cs=getComputedStyle(막);
      return {t, 수:시.querySelectorAll('.시트손잡이').length, 머리안:막.parentElement.classList.contains('머리'), 크:[M.w,M.h], 둥:cs.borderRadius, 색:cs.backgroundColor, 가운데:+((M.l+M.r)/2-(시R.l+시R.r)/2).toFixed(1), 위:+(M.t-시R.t).toFixed(1), 터치:getComputedStyle(시.querySelector(':scope>.머리')).touchAction}; }""", [t,덧])
    줄.append(r)
  for q in 줄: print('   ', json.dumps(q,ensure_ascii=False))
  봄('⑥ 시트 10종 모두 손잡이 하나 · 머리 띠 안 · 36 × 4 · 둥글기 2 · --속선', all(not q.get('없음') and q['수']==1 and q['머리안'] and q['크']==[36,4] and q['둥']=='2px' and q['색']=='rgb(181, 201, 218)' for q in 줄), [q['t'] for q in 줄 if q.get('없음') or q.get('수')!=1])
  봄('⑥ 손잡이 = 시트 가운데(±0.5) · 맨 위에서 4 · 머리 touch-action none', all(abs(q['가운데'])<=0.5 and abs(q['위']-4)<=0.5 and q['터치']=='none' for q in 줄), [(q['t'],q['가운데'],q['위']) for q in 줄])
  # 운동 중 [＋] → 넣기 시트 (사진) · ② 새 종목 만들기
  await 새로(pg)
  await pg.click('#폰 .운더'); await pg.wait_for_timeout(450)
  y=await pg.evaluate("""(()=>{ const b=document.querySelector('#폰 .시트 .넣기새'); if(!b) return null; const R=ㅁ(b), 시=ㅁ('#폰 .시트'), 밑=document.elementFromPoint((R.l+R.r)/2,(R.t+R.b)/2);
    return {글:b.textContent, R, 시, 맞:밑===b, 대상:U.시트.대상, 머리:document.querySelector('#폰 .시트 .머리 b').textContent}; })()""")
  봄('② 운동 중 넣기 시트에 [+ 새 종목 만들기] 보임 · 누를 수 있음(가려지지 않음)', y and y['글']=='+ 새 종목 만들기' and y['맞'] and y['대상']=='운동' and y['R']['b']<=y['시']['b'], y)
  await 그림(pg, '4_넣기시트_389.png' if False else '4_넣기시트_360.png')
  await pg.evaluate("document.getAnimations().forEach(a=>a.play())")
  await pg.click('#폰 .시트 .넣기새'); await pg.wait_for_timeout(450)
  await pg.fill('#폰 [data-in="새종목"]', '시험 머신 프레스'); await pg.wait_for_timeout(100)
  await pg.click('#폰 .새찾기단추'); await pg.wait_for_timeout(300)
  await pg.click('#폰 .새칸줄 .칩:has-text("가슴")'); await pg.wait_for_timeout(200)
  if not await pg.evaluate("Object.values(U.새.근육).includes('P')"):
    await pg.click('#폰 .새근육들 .근칩'); await pg.wait_for_timeout(150)
  y=await pg.evaluate("({종류:U.시트.종류, 돌:U.시트.돌아감?.종류, 닫:document.querySelector('#폰 .시트 .머리 .닫기')?.textContent})")
  봄('② 새 종목 시트 — 넣기 시트에서 열림 · 닫기 = 돌아가기', y['종류']=='새종목' and y['돌']=='종목넣기' and y['닫']=='돌아가기', y)
  await pg.click('#폰 .새저장'); await pg.wait_for_timeout(450)
  y=await pg.evaluate("""({종류:U.시트?.종류, 대상:U.시트?.대상, 칸:[...document.querySelectorAll('#폰 .넣기칸')].map(b=>b.querySelector('b').textContent).filter(t=>t.includes('시험')), 표:S.종목표.some(t=>t.이름==='시험 머신 프레스')})""")
  봄('② 저장 → 넣기 시트로 돌아옴 · 목록에 새 종목', y['종류']=='종목넣기' and y['대상']=='운동' and y['칸']==['시험 머신 프레스'] and y['표'], y)
  n0=await pg.evaluate("S.세션.종목.length")
  await pg.click('#폰 .넣기칸:has-text("시험 머신 프레스")'); await pg.wait_for_timeout(300)
  y=await pg.evaluate("({n:S.세션.종목.length, 끝:S.세션.종목[S.세션.종목.length-1].이름, 세트:S.세션.종목[S.세션.종목.length-1].세트.length})")
  봄('② 누르면 지금 운동에 들어감(세트 포함)', y['n']==n0+1 and y['끝']=='시험 머신 프레스' and y['세트']>=1, y)

  # ── ⑥ 끌어내려 닫기 ──
  async def 끌기(dy, 곳='#폰 .시트>.머리 b'):
    R=await pg.evaluate(f"ㅁ('{곳}')"); x0=R['l']+10; y0=(R['t']+R['b'])/2
    await pg.mouse.move(x0,y0); await pg.mouse.down(); await pg.mouse.move(x0,y0+dy,steps=10)
    중=await pg.evaluate("(()=>{ const s=document.querySelector('#폰 .시트'); return s?s.style.transform:null; })()")
    await pg.mouse.up(); await pg.wait_for_timeout(400)
    return 중
  # 덜 내림 → 제자리 (넣기 시트 · 지금 열려 있음)
  중=await 끌기(50)
  y=await pg.evaluate("({종류:U.시트?.종류, tf:document.querySelector('#폰 .시트')?.style.transform, 수:S.세션.종목.length})")
  봄('⑥ 머리를 50 끌면 따라 내려왔다가(42) 놓으면 제자리 · 닫히지 않음', 중=='translateY(42px)' and y['종류']=='종목넣기' and y['tf']=='' , [중,y])
  # 시트 안(목록)을 끌면 시트는 그대로 · 목록이 스크롤
  await pg.click('#폰 .넣기칩 .칩:has-text("전체")'); await pg.wait_for_timeout(300)   # 새 종목을 만든 뒤에는 그 카테고리 칩이 골라져 목록이 짧다
  R=await pg.evaluate("ㅁ('#폰 .넣기목록')"); await pg.mouse.move(R['l']+R['w']/2, R['t']+R['h']/2)
  print('   굴림 자리', await pg.evaluate(f"document.elementFromPoint({R['l']+R['w']/2},{R['t']+R['h']/2})?.className"))
  await pg.mouse.wheel(0,200); await pg.wait_for_timeout(400)
  y=await pg.evaluate("({종류:U.시트?.종류, 위:document.querySelector('#폰 .넣기목록')?.scrollTop, 시:document.querySelector('#폰 .시트').scrollTop, tf:document.querySelector('#폰 .시트').style.transform})")
  봄('⑥ 시트 안 굴림(스크롤)은 그대로 · 시트 안 움직임', y['종류']=='종목넣기' and (y['위'] or 0)+(y['시'] or 0)>0 and y['tf']=='', y)
  R=await pg.evaluate("ㅁ('#폰 .넣기목록')")
  await pg.mouse.move(R['l']+R['w']/2, R['t']+20); await pg.mouse.down(); await pg.mouse.move(R['l']+R['w']/2, R['t']+140, steps=10)
  tf=await pg.evaluate("document.querySelector('#폰 .시트').style.transform"); await pg.mouse.up(); await pg.wait_for_timeout(300)
  y=await pg.evaluate("({종류:U.시트?.종류, n:S.세션.종목.length})")
  봄('⑥ 목록 칸에서 아래로 끌면 시트는 안 움직임 · 안 닫힘', tf=='' and y['종류']=='종목넣기', [tf,y])
  # 새 종목 시트(넣기에서 연) 끌어 닫기 → 넣기 시트로
  await pg.click('#폰 .시트 .넣기새'); await pg.wait_for_timeout(450)
  await 끌기(120)
  y=await pg.evaluate("({종류:U.시트?.종류})")
  봄('⑥ 새 종목 시트 120 끌어 닫기 → 넣기 시트로 돌아감([돌아가기]와 같음)', y['종류']=='종목넣기', y)
  await 끌기(120)
  y=await pg.evaluate("({시트:U.시트, 가림:!!document.querySelector('#폰 .가림'), 탭:U.탭})")
  봄('⑥ 넣기 시트 120 끌어 닫기 → 닫힘', y['시트'] is None and not y['가림'], y)
  # 메모 시트(탭) — 닫기 단추를 잡고 끌어도 클릭으로 안 바뀜
  await pg.evaluate("S.세션=null; U.탭='캘린더'; 그리기()"); await pg.click('#폰 .탭줄 [data-t="메모"]'); await pg.wait_for_timeout(400)
  중=await 끌기(30, '#폰 .시트>.머리 .닫기')
  y=await pg.evaluate("({종류:U.시트?.종류})")
  봄('⑥ [닫기] 위에서 30 끌고 놓으면 클릭 안 됨 · 제자리', y['종류']=='메모' and 중=='translateY(22px)', [중,y])
  await pg.click('#폰 .시트>.머리 .닫기'); await pg.wait_for_timeout(400)
  봄('⑥ 그 뒤 [닫기] 누름은 그대로 됨', await pg.evaluate("U.시트===null"))
  print('   오류', err); 봄('탭 · 시트 페이지 오류 없음', not err, err)

async def 줄임어둠(b):
  print('\n════ 움직임 줄임 · 다크 ════')
  c=await b.new_context(viewport={'width':389,'height':860}, reduced_motion='reduce', color_scheme='dark'); pg=await c.new_page()
  await pg.goto('file://'+HTML); await pg.wait_for_timeout(500); await 새로(pg)
  await pg.evaluate("(()=>{ const ss=S.세션; ss.종목[0].세트[0].완료=true; ss.휴식={i:0,k:0,끝:S.시계+90000,길이:90}; 그리기(); })()"); await pg.wait_for_timeout(100)
  y=await pg.evaluate("""(()=>{ const a=e=>getComputedStyle(document.querySelector(e)).animationName;
    return {줄:a('#폰 .운세트들 .세트줄.지금'), 칸:a('#폰 .운칸.지금'), 점:a('#폰 .운주 .노란점'), 게점:a('#폰 .쉼게이지 .노란점'), 노랑:getComputedStyle(document.querySelector('#폰 .운주 .노란점')).backgroundColor, 테:getComputedStyle(document.querySelector('#폰 .운주 .노란점')).boxShadow,
      바탕:getComputedStyle(document.querySelector('#폰 .운칸.지금')).backgroundColor}; })()""")
  봄('⑦ 움직임 줄임이면 점멸 없음(줄 · 칸 · 점 넷 다)', y['줄']==y['칸']==y['점']==y['게점']=='none', y)
  봄('⑦ 다크 --노랑 #FFD54F · 테 #04213D · 지금 칸 --강조옅음(멈춤)', y['노랑']=='rgb(255, 213, 79)' and '4, 33, 61' in y['테'] and y['바탕']=='rgb(31, 50, 70)', y)
  # 움직임 줄임에서 시트 끌어 닫기 → 바로 닫힘
  await pg.evaluate("U.시트={종류:'근육'}; 그리기()"); await pg.wait_for_timeout(100)
  R=await pg.evaluate("ㅁ('#폰 .시트>.머리 b')")
  await pg.mouse.move(R['l']+5,R['t']+5); await pg.mouse.down(); await pg.mouse.move(R['l']+5,R['t']+125,steps=8); await pg.mouse.up(); await pg.wait_for_timeout(100)
  봄('⑥ 움직임 줄임 — 끌어 닫기 바로 닫힘', await pg.evaluate("U.시트===null"))
  await c.close()

async def 손가락(b):
  print('\n════ 손가락(터치 · CDP) 360 ════')
  c=await b.new_context(viewport={'width':360,'height':800}, has_touch=True, is_mobile=True); pg=await c.new_page(); cdp=await c.new_cdp_session(pg)
  await pg.goto('file://'+HTML); await pg.wait_for_timeout(500); await 새로(pg)
  async def 밀기(x0,y0,x1,y1,n=12):
    await cdp.send('Input.dispatchTouchEvent',{'type':'touchStart','touchPoints':[{'x':x0,'y':y0}]})
    for i in range(1,n+1):
      await cdp.send('Input.dispatchTouchEvent',{'type':'touchMove','touchPoints':[{'x':x0+(x1-x0)*i/n,'y':y0+(y1-y0)*i/n}]}); await pg.wait_for_timeout(16)
    await cdp.send('Input.dispatchTouchEvent',{'type':'touchEnd','touchPoints':[]}); await pg.wait_for_timeout(500)
  R=await pg.evaluate("ㅁ('#폰 .운칸[data-i=\"2\"]')")
  await 밀기(R['l']+40,R['t']+30,R['l']-110,R['t']+30)
  y=await pg.evaluate("({옆:document.querySelector('#폰 .운띠').scrollLeft, 왼숨:document.querySelector('#폰 .운화살.왼').hidden, 본:U.본})")
  봄('터치 ③ 손가락으로 칸 줄을 밀면 ‹ 나타남', y['옆']>40 and not y['왼숨'], y)
  await pg.evaluate("U.시트={종류:'근육'}; 그리기()"); await pg.wait_for_timeout(400)
  R=await pg.evaluate("ㅁ('#폰 .시트>.머리 b')")
  await 밀기(R['l']+10,(R['t']+R['b'])/2,R['l']+10,(R['t']+R['b'])/2+50,8)
  y1=await pg.evaluate("({시트:U.시트&&U.시트.종류, tf:document.querySelector('#폰 .시트')?.style.transform})")
  await 밀기(R['l']+10,(R['t']+R['b'])/2,R['l']+10,(R['t']+R['b'])/2+140,10)
  y2=await pg.evaluate("({시트:U.시트})")
  봄('터치 ⑥ 머리를 손가락으로 50 내리면 제자리 · 140 내리면 닫힘', y1['시트']=='근육' and y1['tf']=='' and y2['시트'] is None, [y1,y2])
  await c.close()

async def main():
  async with async_playwright() as p:
    b=await p.chromium.launch()
    for W,H in [(389,860),(360,800)]: await 한폭(b,W,H)
    await 탭시트(b); await 줄임어둠(b); await 손가락(b)
    await b.close()
  print(f"\n통과 {결과['통과']} · 실패 {len(결과['실패'])}")
  for f in 결과['실패']: print('  ✗', f)
asyncio.run(main())
