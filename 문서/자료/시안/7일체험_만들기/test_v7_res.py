"""10-03 결과 화면(m66q) · 올림 속도(nasu) · 근육 기준 칸(cxi1) 시험 — python3 test_v7_res.py [파일]  기본 7day-v7res.html"""
import asyncio, sys, json
from playwright.async_api import async_playwright
파일 = sys.argv[1] if len(sys.argv)>1 else '7day-v7res.html'
F='file:///tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'+파일
글자크기 = {11,13,15,18,22,28}

# 가짜 window.claude — db 모음을 메모리에 (로컬에는 claude 가 없으므로)
가짜 = """(()=>{ const 저장소={}, 듣기=[];
  const 스냅=c=>{ const docs=Object.entries(저장소).filter(([k])=>k.startsWith(c+'/')).map(([k,v])=>({id:k.split('/')[1], exists:true, data:()=>v})); return {docs, size:docs.length, empty:!docs.length, docChanges:()=>[]}; };
  const 알림=c=>듣기.filter(([x])=>x===c).forEach(([x,f])=>f(스냅(c)));
  const 모음=c=>({ doc:id=>({ set:async d=>{ 저장소[c+'/'+id]=JSON.parse(JSON.stringify(d)); 알림(c); } }),
    add:async d=>{ 저장소[c+'/'+Math.random().toString(36).slice(2)]=d; 알림(c); },
    where:()=>({ onSnapshot:f=>{ return ()=>{}; } }),
    onSnapshot:(f)=>{ 듣기.push([c,f]); setTimeout(()=>f(스냅(c)),0); return ()=>{}; } });
  const db={collection:모음};
  window.__저장소=저장소; window.__밀어=(c,id,d)=>{ 저장소[c+'/'+id]=d; 알림(c); };
  window.claude={use:async n=> n==='db'?db:null};
})();"""

async def main():
  결과=[]
  def 봄(m,ok): 결과.append((m,bool(ok)))
  async with async_playwright() as p:
    b=await p.chromium.launch()
    for 폭,높이 in [(420,1080),(360,800)]:
      pg=await b.new_page(viewport={'width':폭,'height':높이}); 오류=[]; pg.on('pageerror',lambda e:오류.append(str(e)))
      await pg.goto(F); await pg.wait_for_timeout(700); t=f"[{폭}] "
      await pg.evaluate("U.업적띠=null; 그리기();")
      넘=await pg.evaluate("document.documentElement.scrollWidth>document.documentElement.clientWidth+1")
      봄(t+"페이지 가로 넘침 없음", not 넘)
      # ── nasu 속도 ──
      표=await pg.evaluate("[5,45,450,4500,45000,450000].map(v=>Math.round(자릿수시간(v)))")
      봄(t+f"nasu 자릿수 시간 = 앱 표 × 1.35 ({표} ms)", 표==[1350,1688,2025,2700,3375,4050])
      게=await pg.evaluate("""(()=>{ const g=document.createElement('div'); g.className='게이지3'; g.innerHTML='<i></i>'; const s=document.createElement('div'); s.className='스탯칸 들어옴'; s.innerHTML='<i class="더"></i>';
        document.body.append(g,s); const r={게이지:getComputedStyle(g.firstChild).transitionDuration, 스탯막대:getComputedStyle(s.firstChild).animationDuration, 늦춤:getComputedStyle(s.firstChild).animationDelay}; g.remove(); s.remove(); return r; })()""")
      봄(t+f"nasu 게이지 채움 0.7 → 0.945초 ({게})", 게['게이지']=='0.945s' and 게['스탯막대']=='0.945s')
      # ── 지난 기록 만들기: 2일 전 같은 루틴 — 첫 종목은 5kg 가볍게(▲), 둘째는 5kg 무겁게(▼), 셋째는 똑같이(표시 없음) ──
      준비=await pg.evaluate("""(()=>{ S.기록={}; 운동시작('r1'); const ss=S.세션, 종=ss.종목.slice(0,3);
        종.forEach((e,i)=>e.세트.slice(0,2).forEach(s=>s.완료=true));
        const 옛=종.map((e,i)=>({이름:e.이름, 세트:e.세트.slice(0,2).map(s=>({w:Math.max(0,s.w+[-5,5,0][i]), r:s.r, 완료:true}))}));
        S.기록[날더하기(오늘(),-2)]={루틴id:'r1', 이름:ss.이름, 초:1800, 시작시각:0, 종목:옛}; 그리기();
        return {이름:ss.이름, 종:종.map(e=>({이름:e.이름, w:e.세트[0].w, r:e.세트[0].r, w2:e.세트[1].w, r2:e.세트[1].r}))}; })()""")
      if 폭==420: print('준비', 준비)
      await pg.click('.아랫줄 [data-act="끝내기"]'); await pg.wait_for_timeout(250)
      중간=await pg.evaluate("[...document.querySelectorAll('.결과수 [data-count]')].map(x=>[x.textContent, x.dataset.count])")
      await pg.wait_for_timeout(120)
      띠중=await pg.evaluate("getComputedStyle(document.querySelector('.결과띠속')).opacity")
      if 폭==420: await (await pg.query_selector('#폰')).screenshot(path='v7res_중간.png')
      await pg.wait_for_timeout(3600)
      끝값=await pg.evaluate("[...document.querySelectorAll('.결과수 [data-count]')].map(x=>[x.textContent, x.dataset.count])")
      봄(t+f"m66q 세 숫자가 0 부터 올라감 (0.25초 {[x[0] for x in 중간]} → 끝 {[x[0] for x in 끝값]})", all(a[0]!=c[0] for a,c in zip(중간[2:],끝값[2:])) and 끝값[0][0]=='6')
      봄(t+f"m66q 제목 띠가 드러남 (0.37초 불투명도 {띠중})", float(띠중)<1)
      await pg.evaluate("U.업적띠=null; 그리기();"); await pg.wait_for_timeout(200)
      r=await pg.evaluate("""(()=>{ const q=s=>document.querySelector(s), 글=s=>q(s)?.innerText??null;
        const 종=[...document.querySelectorAll('.결과종목')].map(x=>({이름:x.querySelector('.결과이름')?.textContent, 줄:[...x.querySelectorAll('.향줄')].map(z=>({앞:z.querySelector('.향글').textContent, 칩:[...z.querySelectorAll('.대비칩')].map(c=>({글:c.innerText.replace(/\\s+/g,' '), 색:getComputedStyle(c.querySelector('b')).color, 반:c.querySelector('b').className}))}))}));
        const 색=v=>{const d=document.createElement('i'); d.style.color=getComputedStyle(document.documentElement).getPropertyValue(v); document.body.appendChild(d); const c=getComputedStyle(d).color; d.remove(); return c;};
        const 틀=q('.결과틀'), 목=q('.결과목록');
        return {띠:글('.결과띠'), 수:[...document.querySelectorAll('.결과수>div')].map(x=>x.innerText.replace(/\\n/g,'|')), 위:글('.결과위'), 종,
          오름:색('--오름'), 내림:색('--내림'), 전부:글('#폰'), 단추:[...document.querySelectorAll('.결과아래 button')].map(x=>x.dataset.act+':'+x.textContent),
          세로넘침:틀.scrollHeight>틀.clientHeight+1, 가로넘침:[...document.querySelectorAll('.결과틀 *')].filter(x=>x.scrollWidth>x.clientWidth+1&&getComputedStyle(x).overflowX!=='visible'&&!x.classList.contains('결과목록')).map(x=>x.className),
          향넘침:[...document.querySelectorAll('.향줄')].filter(x=>x.scrollWidth>x.clientWidth+1).length,
          크기:[...new Set([...document.querySelectorAll('.결과틀 *, .결과아래 *')].filter(x=>[...x.childNodes].some(n=>n.nodeType===3&&n.textContent.trim())).map(x=>parseFloat(getComputedStyle(x).fontSize)))],
          기대:(()=>{ const ss=S.세션, e=ss.종목, 일=(w,r)=>일RM(w,r); return {rm0:일(e[0].세트[0].w,e[0].세트[0].r)-일(Math.max(0,e[0].세트[0].w-5),e[0].세트[0].r)}; })()}; })()""")
      if 폭==420: print(json.dumps({k:v for k,v in r.items() if k!='전부'}, ensure_ascii=False)[:1500])
      이름=준비['이름']; 루=이름 if '루틴' in 이름 else 이름+' 루틴'
      봄(t+f"m66q 제목 띠 [{r['띠']}]", 루 in r['띠'] and ('달성' in r['띠']))
      봄(t+f"m66q 세 숫자 세트 · 시간 · 볼륨 ({r['수']})", len(r['수'])==3 and r['수'][0]=='6|세트' and r['수'][1].endswith('|시간') and r['수'][2].endswith('kg|볼륨'))
      봄(t+"m66q 카드 위: 루틴 이름 · 볼륨 · N종목 · 달성도", 루 in r['위'] and '볼륨' in r['위'] and '종목 · 달성도' in r['위'])
      종=r['종']
      봄(t+f"m66q 세트 한 종목만 목록 ({[x['이름'] for x in 종]})", [x['이름'] for x in 종]==[x['이름'] for x in 준비['종']])
      e0,e1,e2=종[0],종[1],종[2]
      봄(t+f"m66q 오른 종목: 1RM · 볼륨 칩이 ▲ 빨강 ({e0['줄']})", all(len(z['칩'])==2 and all(c['글'].find('▲')>0 and c['색']==r['오름'] for c in z['칩']) for z in e0['줄']))
      rm글=await pg.evaluate(f"차kg(Math.round({r['기대']['rm0']}*10)/10)")
      봄(t+f"m66q ▲ 숫자 = 차이 kg (1RM ▲ {rm글}kg)", e0['줄'][0]['칩'][0]['글'].endswith(f"▲ {rm글}kg"))
      봄(t+f"m66q 내린 종목: ▼ 파랑 ({e1['줄']})", all(len(z['칩'])==2 and all('▼' in c['글'] and c['색']==r['내림'] for c in z['칩']) for z in e1['줄']))
      봄(t+f"m66q 같은 종목: 칩 없음 ({e2['줄']})", all(len(z['칩'])==0 for z in e2['줄']))
      칩글=' '.join(c['글'] for x in 종 for z in x['줄'] for c in z['칩'])+' '.join(z['앞'] for x in 종 for z in x['줄'])
      향글=await pg.evaluate("[...document.querySelectorAll('.향줄')].map(x=>x.innerText).join(' ')")
      봄(t+"m66q 비교 줄에 '유지' · '+' · '−' · '%' 글자 없음 (▲▼ 숫자만)", not any(x in 향글 for x in ['유지','%','+','−']) and '▲' in 향글 and '▼' in 향글)
      봄(t+f"m66q 단추 ({r['단추']})", r['단추']==['운동저장:기록 저장하고 끝내기','운동으로:운동으로 돌아가기','운동버림:기록 없이 끝내기'])
      봄(t+f"결과 화면 세로로 안 넘침 (종목 목록만 넘김) · 가로 넘침 없음 ({r['가로넘침']}, 향줄 {r['향넘침']})", not r['세로넘침'] and not r['가로넘침'] and r['향넘침']==0)
      봄(t+f"글자 크기 11·13·15·18 만 ({sorted(r['크기'])})", set(r['크기'])<=글자크기)
      await (await pg.query_selector('#폰')).screenshot(path=f'v7res_결과_{폭}.png')
      e0칩=await pg.evaluate("document.querySelector('.결과종목 .대비칩 [data-count]')?.dataset.count")
      # ── 기록 없이 끝내기: 두 번 ──
      n0=await pg.evaluate("Object.keys(S.기록).length")
      await pg.click('[data-act="운동버림"]'); await pg.wait_for_timeout(150)
      글1=await pg.evaluate("[document.querySelector('[data-act=\"운동버림\"]')?.textContent, !!S.세션]")
      봄(t+f"한 번 누르면 글만 바뀜 ({글1})", 글1==['한 번 더 누르면 버립니다', True])
      await pg.click('[data-act="운동으로"]'); await pg.wait_for_timeout(150); await pg.click('.아랫줄 [data-act="끝내기"]'); await pg.wait_for_timeout(150)
      글2=await pg.evaluate("document.querySelector('[data-act=\"운동버림\"]')?.textContent")
      봄(t+f"돌아갔다 오면 처음 글로 ({글2})", 글2=='기록 없이 끝내기')
      # ── 저장 → 저장된 결과 화면(자기 자신과 견주지 않음) ──
      await pg.click('[data-act="운동저장"]'); await pg.wait_for_timeout(300)
      키=await pg.evaluate("Object.keys(S.기록).filter(k=>k.startsWith(오늘())).sort().pop()")
      await pg.evaluate(f"U.업적띠=null; S.결과={{key:{json.dumps(키)}}}; 그리기();"); await pg.wait_for_timeout(300)
      s=await pg.evaluate("""(e0칩=>({칩:[...document.querySelectorAll('.결과종목')][0]?.querySelector('.대비칩 [data-count]')?.dataset.count, 앞칩:e0칩, 아래:document.querySelector('.결과아래')?.innerText.replace(/\\n/g,'|'), 수:Object.keys(S.기록).length}))""", e0칩)
      봄(t+f"저장된 결과: 자기 자신 빼고 견줌 (첫 칩 {s['앞칩']} = {s['칩']}) · '기록은 저장되었습니다' + 확인", s['칩'] and s['칩']==s['앞칩'] and s['아래']=='기록은 저장되었습니다|확인' and s['수']==n0+1)
      if 폭==360: await (await pg.query_selector('#폰')).screenshot(path='v7res_저장됨_360.png')
      await pg.click('[data-act="결과확인"]'); await pg.wait_for_timeout(200)
      # 버리기 두 번 → 기록 안 남음
      await pg.evaluate("U.업적띠=null; 운동시작('r1'); S.세션.종목[0].세트[0].완료=true; 그리기();"); await pg.click('.아랫줄 [data-act="끝내기"]'); await pg.wait_for_timeout(150)
      m0=await pg.evaluate("Object.keys(S.기록).length")
      await pg.click('[data-act="운동버림"]'); await pg.wait_for_timeout(100); await pg.click('[data-act="운동버림"]'); await pg.wait_for_timeout(200)
      v=await pg.evaluate("({세션:!!S.세션, 수:Object.keys(S.기록).length, 탭:U.탭})")
      봄(t+f"두 번 누르면 기록 없이 끝남 ({v})", not v['세션'] and v['수']==m0 and v['탭']=='캘린더')
      # ── cxi1 근육 기준 칸 ──
      단=await pg.evaluate("""(()=>{ const a=document.getElementById('표시단추'), b=document.getElementById('기준단추'); return {옆:a.nextElementSibling===b, 같은모양:b.classList.contains('작은'), 글:b.textContent}; })()""")
      봄(t+f"cxi1 [근육 기준] 단추가 ✎ 표시 옆 · 같은 .작은 ({단})", 단['옆'] and 단['같은모양'])
      await pg.click('#기준단추'); await pg.wait_for_timeout(200)
      칸=await pg.evaluate("""(()=>{ const c=document.getElementById('기준칸'); return {보임:!c.hidden&&c.offsetHeight>0, 줄:[...c.querySelectorAll('.기준줄 b')].map(x=>x.textContent), 안내:c.querySelector('.기준안내').textContent,
         넘:[...c.querySelectorAll('.기준줄')].filter(x=>x.scrollWidth>x.clientWidth+1).length, 눌림:document.getElementById('기준단추').getAttribute('aria-pressed'),
         페이지넘침:document.documentElement.scrollWidth>document.documentElement.clientWidth+1}; })()""")
      봄(t+f"cxi1 칸 열림 · 부위 {len(칸['줄'])}개 ({' · '.join(칸['줄'])})", 칸['보임'] and len(칸['줄'])==12 and 칸['눌림']=='true')
      봄(t+f"cxi1 설명 한 줄 '{칸['안내']}'", 칸['안내']=='시안에서만 쓰는 임시 칸 — 넣은 값은 Claude 가 읽어 갑니다')
      봄(t+"cxi1 칸 가로 넘침 없음", 칸['넘']==0 and not 칸['페이지넘침'])
      # 운동 중: 벤치 두 세트 → 가슴 단계 → 기준 넣기 → 단계가 그 값으로
      await pg.evaluate("U.업적띠=null; 운동시작('r1'); S.세션.종목[0].세트[0].완료=true; S.세션.종목[0].세트[1].완료=true; 그리기();"); await pg.wait_for_timeout(300)
      앞=await pg.evaluate("""(()=>{ const v=잎볼륨(S.세션.종목), 단=오늘단계(S.세션.종목), l='chest_mid'; return {v:v[l], 단:단[l], 최:S.최대볼륨[l]||0, 글:[...document.querySelectorAll('.기준줄[data-k="chest"] .기준지금 span')].map(x=>x.textContent).join(' / ')}; })()""")
      await pg.fill('.기준줄[data-k="chest"] .기준수', '10000'); await pg.wait_for_timeout(300)
      뒤=await pg.evaluate("""(()=>{ const 단=오늘단계(S.세션.종목); return {단:단['chest_mid'], 위:단['chest_upper'], 어깨:단['delt_front'], 단추:document.getElementById('기준단추').textContent}; })()""")
      앞어깨=await pg.evaluate("(()=>{ const 기=근육기준값.chest; 근육기준값.chest=null; const x=오늘단계(S.세션.종목).delt_front; 근육기준값.chest=기; return x; })()")
      봄(t+f"cxi1 가슴 10000kg 넣으면 단계 = 20 × {round(앞['v'])} ÷ 10000 ({앞['단']:.1f} → {뒤['단']:.2f}) · 옆 글 '{앞['글']}'", abs(뒤['단']-20*앞['v']/10000)<1e-6 and 뒤['단']<앞['단'] and '최대' in 앞['글'] and '오늘' in 앞['글'])
      봄(t+f"cxi1 다른 부위(어깨)는 그대로 ({앞어깨:.2f} = {뒤['어깨']:.2f}) · 단추 '{뒤['단추']}'", abs(앞어깨-뒤['어깨'])<1e-9 and 뒤['단추']=='근육 기준 · 1')
      그림=await pg.evaluate("[...document.querySelectorAll('.배너 path.몸근')].map(x=>x.style.fill).filter(f=>f&&!f.includes('var')).length")
      if 폭==420: await pg.screenshot(path='v7res_기준_420.png')
      await pg.fill('.기준줄[data-k="chest"] .기준수', ''); await pg.wait_for_timeout(300)
      되=await pg.evaluate("오늘단계(S.세션.종목)['chest_mid']")
      봄(t+f"cxi1 비우면 지금 규칙으로 ({되:.1f})", abs(되-앞['단'])<1e-9)
      await pg.fill('.기준줄[data-k="chest"] .기준수', '50'); await pg.wait_for_timeout(900)
      빨=await pg.evaluate("[오늘단계(S.세션.종목)['chest_mid'], [...document.querySelectorAll('.배너 path.몸근')].some(x=>x.style.fill===단계색(20)||getComputedStyle(x).fill===getComputedStyle(Object.assign(document.createElement('i'),{})).fill)]")
      그림색=await pg.evaluate("(()=>{ const c=단계색(20), d=document.createElement('i'); d.style.color=c; document.body.appendChild(d); const rgb=getComputedStyle(d).color; d.remove(); return [...document.querySelectorAll('.배너 path.몸근')].some(x=>getComputedStyle(x).fill===rgb); })()")
      봄(t+f"cxi1 작은 값(50kg)이면 바로 20단계 · 그림에 가장 빨강 ({빨[0]:.0f}단계, 그림 {그림색})", 빨[0]==20 and 그림색)
      if 폭==360: await pg.screenshot(path='v7res_기준_360.png')
      await pg.fill('.기준줄[data-k="chest"] .기준수', '')
      봄(t+"JS 오류 없음 (window.claude 없음 → 메모리만)", not 오류)
      if 오류: print(오류[:3])
      await pg.close()
    # ── db 저장 (가짜 window.claude) ──
    pg=await b.new_page(viewport={'width':420,'height':1080}); 오류=[]; pg.on('pageerror',lambda e:오류.append(str(e)))
    await pg.add_init_script(가짜); await pg.goto(F); await pg.wait_for_timeout(700)
    await pg.evaluate("U.업적띠=null; 운동시작('r1'); S.세션.종목[0].세트[0].완료=true; 그리기();")
    await pg.click('#기준단추'); await pg.fill('.기준줄[data-k="chest"] .기준수', '5000'); await pg.fill('.기준줄[data-k="chest"] .기준메모', '벤치 5세트 + 플라이 3세트 = 가장 빨강')
    await pg.wait_for_timeout(1200)
    d=await pg.evaluate("window.__저장소['thresholds/chest']")
    print('db 문서:', json.dumps(d, ensure_ascii=False))
    봄(f"[db] thresholds/chest 문서 ({d and {k:d[k] for k in ['부위','키','볼륨','메모']}})", d and d['부위']=='가슴' and d['키']=='chest' and d['볼륨']==5000 and d['메모'].startswith('벤치') and 'chest' and d.get('지금기준') is not None)
    await pg.evaluate("window.__밀어('thresholds','back',{부위:'등',키:'back',볼륨:3000,메모:'다른 곳에서'})"); await pg.wait_for_timeout(300)
    x=await pg.evaluate("({값:document.querySelector('.기준줄[data-k=\"back\"] .기준수').value, 메모:document.querySelector('.기준줄[data-k=\"back\"] .기준메모').value, 기:근육기준값.back, 단추:document.getElementById('기준단추').textContent})")
    봄(f"[db] 다른 곳에서 쓴 값이 칸 · 계산에 들어옴 ({x})", x['값']=='3000' and x['메모']=='다른 곳에서' and x['기']['볼륨']==3000 and x['단추']=='근육 기준 · 2')
    봄("[db] JS 오류 없음", not 오류)
    await pg.close(); await b.close()
  [print(("✅ " if ok else "❌ ")+m) for m,ok in 결과]; print(f"\n{sum(ok for _,ok in 결과)}/{len(결과)}")
asyncio.run(main())
