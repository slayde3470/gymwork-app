"""10-03 운동 화면 ✎ 표시 7개 시험 (headless Chromium) — python3 test_v7_work.py [파일]  기본 7day-v7work.html
 0yyw 띠 · ozlo 플랜 n회차 · iiwd 근육 칩 삭제 · zx75 지표 아래 상자로 · 9a8l/h6tu 세트 목록 스크롤 · khmo 끌기 · i4gd 칸 줄 따라가기"""
import asyncio, sys, json, os
SP=os.path.dirname(os.path.abspath(__file__))
상태=json.load(open(SP+'/marks3/28f858e336f21768f689d786826649de.json'))   # ✎ 0yyw 의 '상태'
from playwright.async_api import async_playwright
F='file:///tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'+(sys.argv[1] if len(sys.argv)>1 else '7day-v7work.html')
글자들={11,13,15,18,22,28}
async def main():
  결과=[]
  def 봄(m,ok): 결과.append((m,bool(ok)))
  async with async_playwright() as p:
    b=await p.chromium.launch()
    for 폭,높이 in [(420,1080),(360,800)]:
      pg=await b.new_page(viewport={'width':폭,'height':높이}); 오류=[]; pg.on('pageerror',lambda e:오류.append(str(e)))
      await pg.goto(F); await pg.wait_for_timeout(5600); t=f"[{폭}] "
      async def 띠지움(): await pg.evaluate("(()=>{ if(U.업적띠){ U.업적띠=null; 그리기(); } })()")
      async def 누름(sel, 쉼=150): await pg.click(sel); await pg.wait_for_timeout(쉼); await 띠지움()
      async def js(x): return await pg.evaluate(x)
      async def 체크칸(j, 쉼=0):
        els=await pg.query_selector_all('.운세트들 .세트줄 [data-act="체크"]'); await els[j].click()
        if 쉼: await pg.wait_for_timeout(쉼); await 띠지움()
      # 홍겸 님이 0yyw 를 표시할 때의 상태(가슴·어깨 10종목, 막 시작)로 띄운다 — 종목 칸 줄이 넘치는 실제 상황
      await pg.evaluate("s=>{ S=s.S; Object.assign(U, s.U, {시트:null, 업적띠:null}); 그리기(); }", 상태); await pg.wait_for_timeout(400)
      봄(t+f"표시 때 상태로 운동 화면 ({await js('S.세션.종목.length')}종목)", await js("!!document.querySelector('.운머리') && S.세션.종목.length>=8"))
      # ── 0yyw 머리 줄 = 띠 ──
      머=await js("""(()=>{ const m=document.querySelector('.운머리'), 띠=document.createElement('div'); 띠.className='띠'; document.body.appendChild(띠);
        const a=getComputedStyle(m), z=getComputedStyle(띠), 이=getComputedStyle(m.querySelector('.운이름')), 나=getComputedStyle(m.querySelector('.운나감')), 시=getComputedStyle(m.querySelector('[data-timer="경과"]'));
        const r={바탕:a.backgroundColor===z.backgroundColor, 글:a.color===z.color, 높이:m.getBoundingClientRect().height, 여백:a.padding, 이름:[이.fontSize,이.fontWeight], 나감:나.color===z.color, 시계:[시.color===z.color, 시.fontSize]};
        띠.remove(); return r; })()""")
      봄(t+f"0yyw 머리 줄 = 띠 (강조 바탕 · 강조글 · {머})", 머['바탕'] and 머['글'] and 머['높이']>=40 and 머['여백']=='6px 12px' and 머['이름']==['18px','700'] and 머['나감'] and 머['시계']==[True,'18px'])
      # ── ozlo '플랜 n회차' 이름 옆 같은 줄 ──
      곁=await js("""(()=>{ const m=document.querySelector('.운머리'), 이=m.querySelector('.운이름'), 곁=m.querySelector('.운곁'); if(!곁) return null;
        const 줄=[...이.getClientRects()], 끝=줄[줄.length-1], g=곁.getBoundingClientRect();
        return {글:곁.textContent, 같은줄:Math.abs(끝.bottom-g.bottom)<6, 오른쪽:g.left>=끝.right, 크기:getComputedStyle(곁).fontSize}; })()""")
      봄(t+f"ozlo '플랜 n회차' 띠 안 이름 오른쪽 같은 줄 ({곁})", 곁 and 곁['글'].startswith('플랜') and 곁['같은줄'] and 곁['오른쪽'] and 곁['크기']=='13px')
      # ── iiwd 근육 칩 없음 ──
      봄(t+"iiwd 근육 칩 줄 없음", await js("!document.querySelector('#폰 .운근육') && !document.querySelector('#폰 .운머리 .운지표')"))
      # ── zx75 지표 두 줄이 아래 상자 맨 위 ──
      지=await js("""(()=>{ const 아=document.querySelector('.운아래'), 묶=아.querySelector('.운지표들'), 줄=[...묶.querySelectorAll('.운지표')];
        return {첫째:아.firstElementChild===묶, 줄수:줄.length, 글:줄.map(x=>x.textContent.replace(/\\s+/g,' ').trim()), 두줄:줄.length===2&&줄[1].getBoundingClientRect().top>=줄[0].getBoundingClientRect().bottom,
          넘침:줄.some(x=>x.scrollWidth>x.clientWidth+1), 크기:[...묶.querySelectorAll('span,b')].map(x=>parseFloat(getComputedStyle(x).fontSize))}; })()""")
      봄(t+f"zx75 지표 두 줄이 아래 상자 맨 위 ({' | '.join(지['글'])})", 지['첫째'] and 지['두줄'] and '1RM' in 지['글'][0] and '달성' in 지['글'][1] and not 지['넘침'])
      봄(t+f"zx75 지표 글자 크기 11 · 13 · 15 ... 안 ({sorted(set(지['크기']))})", set(지['크기'])<=글자들)
      # ▲ 오름 빨강 · ▼ 내림 파랑 — 첫 세트 무게를 크게 / 작게 해서 체크
      색=await js("""(()=>{ const 루=getComputedStyle(document.documentElement), 값=v=>{const d=document.createElement('i'); d.style.color=루.getPropertyValue(v); document.body.appendChild(d); const c=getComputedStyle(d).color; d.remove(); return c;};
        const ss=S.세션, s=ss.종목[U.본].세트[0], 원=s.w; s.w=300; 체크(U.본,0); U.업적띠=null; 그리기();
        const 오=[...document.querySelectorAll('.운아래 .운칩.오름')].map(x=>getComputedStyle(x).color);
        체크(U.본,0); s.w=5; 체크(U.본,0); U.업적띠=null; 그리기();
        const 내=[...document.querySelectorAll('.운아래 .운칩.내림')].map(x=>getComputedStyle(x).color);
        체크(U.본,0); s.w=원; ss.휴식=null; 그리기(); return {오, 내, 빨강:값('--나쁨'), 파랑:값('--강조')}; })()""")
      봄(t+f"zx75 ▲ 빨강 · ▼ 파랑 그대로 (▲ {len(색['오'])}개 · ▼ {len(색['내'])}개)", 색['오'] and 색['내'] and all(c==색['빨강'] for c in 색['오']) and all(c==색['파랑'] for c in 색['내']))
      if 폭==420: await (await pg.query_selector('#폰')).screenshot(path='v7w_처음.png')
      # 첫 체크 때 아래 상자 · 목록 칸이 안 움직임 (지표 높이 고정)
      앞=await js("[...document.querySelectorAll('.운머리,.배너,.운세트들,.운아래,.아랫줄')].map(x=>Math.round(x.getBoundingClientRect().top)+':'+Math.round(x.getBoundingClientRect().height))")
      await 누름('.운세트들 [data-act="체크"]'); 뒤=await js("[...document.querySelectorAll('.운머리,.배너,.운세트들,.운아래,.아랫줄')].map(x=>Math.round(x.getBoundingClientRect().top)+':'+Math.round(x.getBoundingClientRect().height))")
      봄(t+f"첫 체크에도 칸 자리 · 높이 그대로 ({앞} → {뒤})", 앞==뒤)
      await js("(()=>{ const ss=S.세션; ss.종목[U.본].세트.forEach(s=>s.완료=false); ss.휴식=null; ss.지금={i:U.본,k:0}; 그리기(); })()")
      # ── 이름 안 잘림 · 가로 넘침 없음 (모든 종목) ──
      이름=await js("""(()=>{ const 나쁨=[]; const n=S.세션.종목.length;
        for(let i=0;i<n;i++){ U.본=i; 그리기(); const m=document.querySelector('.운머리'), 이=m.querySelector('.운이름'), 글=m.querySelector('.운글');
          const 줄=new Set([...이.getClientRects()].map(r=>Math.round(r.top))).size;
          if(이.textContent!==S.세션.종목[i].이름 || 줄>2 || 글.scrollHeight>글.clientHeight+1 || 글.scrollWidth>글.clientWidth+1 || m.scrollWidth>m.clientWidth+1) 나쁨.push(i+':'+이.textContent+':'+줄+'줄');
          for(const c of ['#폰','.운아래','.운지표들','.아랫줄']){ const e=document.querySelector(c); if(e && e.scrollWidth>e.clientWidth+1) 나쁨.push(i+' 넘침 '+c); } }
        U.본=0; 그리기(); return 나쁨; })()""")
      봄(t+f"모든 종목 이름이 안 잘림(두 줄 안) · 가로 넘침 없음 ({이름 or '이상 없음'})", not 이름)
      if 폭==360:
        await js("(()=>{ U.본=S.세션.종목.findIndex(e=>e.이름.length>=10); if(U.본<0) U.본=0; 그리기(); })()"); await pg.wait_for_timeout(500)
        await (await pg.query_selector('#폰')).screenshot(path='v7w_360긴이름.png')
        await js("(()=>{ U.본=0; 그리기(); })()"); await pg.wait_for_timeout(500)
      # ── 9a8l · h6tu 세트 목록 ──
      for _ in range(4): await 누름('.운세트들 [data-act="세트더"]', 80)
      async def 목(): return await js("(()=>{const l=document.querySelector('.운세트들'); return {위:Math.round(l.scrollTop), 끝:l.scrollHeight-l.clientHeight, 높:l.clientHeight}})()")
      m0=await 목(); n=await js("S.세션.종목[U.본].세트.length")
      봄(t+f"세트 {n}개로 목록이 넘침 ({m0})", n>=7 and m0['끝']>0)
      # 위 절반 세트 체크 → 그대로
      위k=await js("""(()=>{ const l=document.querySelector('.운세트들'), 위=l.getBoundingClientRect().top;
        return [...l.querySelectorAll('.세트줄')].findIndex((r,j)=>{ const b=r.getBoundingClientRect(); return b.top+b.height/2-위<=l.clientHeight/2 && b.top>=위; }); })()""")
      await 체크칸(위k, 700); m1=await 목()
      봄(t+f"h6tu 위 절반({위k+1}세트) 체크 → 안 움직임 ({m0['위']} → {m1['위']})", 위k>=0 and m1['위']==m0['위'])
      # 절반 아래 세트 체크 → 다음 세트가 가운데쯤 (부드럽게)
      k=await js("""(()=>{ const l=document.querySelector('.운세트들'), 줄=[...l.querySelectorAll('.세트줄')], 위=l.getBoundingClientRect().top;
        return 줄.findIndex((r,j)=>{ const b=r.getBoundingClientRect(); return b.top+b.height/2-위>l.clientHeight/2 && b.bottom-위<=l.clientHeight+b.height/2 && !S.세션.종목[U.본].세트[j].완료 && j+1<줄.length && !S.세션.종목[U.본].세트[j+1].완료; }); })()""")
      await 체크칸(k)
      await pg.wait_for_timeout(40); 중=await 목(); await pg.wait_for_timeout(800); await 띠지움(); m2=await 목()
      가=await js(f"""(()=>{{ const l=document.querySelector('.운세트들'), r=l.querySelectorAll('.세트줄')[{k+1}], a=l.getBoundingClientRect(), b=r.getBoundingClientRect();
        return {{중심차:Math.round((b.top+b.height/2)-(a.top+l.clientHeight/2)), 줄높:Math.round(b.height)}}; }})()""")
      봄(t+f"h6tu 절반 아래 {k+1}세트 체크 → 다음 {k+2}세트가 가운데쯤 ({m1['위']} → {m2['위']} / 끝 {m2['끝']} · 가운데와 {가['중심차']}px)",
         m2['위']>m1['위'] and (abs(가['중심차'])<=가['줄높'] or m2['위']>=m2['끝']-1))
      봄(t+f"h6tu 부드럽게 움직임 (40ms 뒤 {중['위']})", m1['위']<=중['위']<m2['위'])
      # 다시 그려도(휴식 끝 · 값 바꾸기) 자리 그대로
      await js("(()=>{ S.세션.휴식=null; 그리기(); })()"); m3=await 목()
      await 누름('.운세트들 [data-act="세트값"][data-k="0"][data-d="1"]'); m4=await 목()
      봄(t+f"9a8l 다시 그려도 목록 자리 그대로 ({m2['위']} → {m3['위']} → {m4['위']})", m2['위']==m3['위']==m4['위'])
      # 맨 아래까지 내리고 맨 아래 세트 체크 → 맨 위로 안 튐
      await js("document.querySelector('.운세트들').scrollTop=99999"); await pg.wait_for_timeout(150); m5=await 목()
      await 체크칸(-1, 700); m6=await 목()
      봄(t+f"9a8l 맨 아래 세트 체크 → 맨 위로 안 튐 ({m5['위']} → {m6['위']})", m6['위']==m5['위'] and m5['위']>0)
      # 체크 풀기 → 안 움직임
      await 체크칸(-1, 700); m7=await 목()
      봄(t+f"체크 풀기 → 안 움직임 ({m6['위']} → {m7['위']})", m7['위']==m6['위'])
      if 폭==420:
        await js("document.querySelector('.운세트들').scrollTop=0"); await pg.wait_for_timeout(150)
        k=await js("""(()=>{ const l=document.querySelector('.운세트들'), 줄=[...l.querySelectorAll('.세트줄')], 위=l.getBoundingClientRect().top;
          return 줄.findIndex((r,j)=>{ const b=r.getBoundingClientRect(); return b.top+b.height/2-위>l.clientHeight/2 && b.bottom-위<=l.clientHeight && !S.세션.종목[U.본].세트[j].완료; }); })()""")
        await 체크칸(k, 900)
        await (await pg.query_selector('#폰')).screenshot(path='v7w_세트올림.png')
      # 다 보이는 종목: 맨 아래 세트 체크해도 절대 안 움직임
      i=await js("""(()=>{ for(let i=0;i<S.세션.종목.length;i++){ U.본=i; 그리기(); const l=document.querySelector('.운세트들'); if(l.scrollHeight<=l.clientHeight+1) return i; } return -1; })()""")
      if i>=0:
        앞=await js("[...document.querySelectorAll('.운세트들 .세트줄')].map(x=>Math.round(x.getBoundingClientRect().top))")
        await 체크칸(-1, 700)
        뒤=await js("[...document.querySelectorAll('.운세트들 .세트줄')].map(x=>Math.round(x.getBoundingClientRect().top))")
        봄(t+f"세트가 다 보이면 맨 아래 체크해도 안 움직임 (종목 {i+1})", 앞==뒤 and await js("document.querySelector('.운세트들').scrollTop")==0)
      else: 봄(t+"세트가 다 보이는 종목을 못 찾음", False)
      # ── i4gd 종목이 바뀌면 지금 칸이 보이게 가운데쯤 ──
      await js("(()=>{ S.세션.종목.forEach(e=>e.세트.forEach(s=>s.완료=false)); S.세션.휴식=null; U.본=0; 그리기(); })()"); await pg.wait_for_timeout(300)
      async def 칸(): return await js("""(()=>{ const 줄=document.querySelector('.운띠'), c=줄.querySelector('.운칸.지금'), a=줄.getBoundingClientRect(), b=c.getBoundingClientRect();
        return {본:U.본, 옆:Math.round(줄.scrollLeft), 끝:줄.scrollWidth-줄.clientWidth, 보임:b.left>=a.left-1&&b.right<=a.right+1, 중심차:Math.round((b.left+b.width/2)-(a.left+a.width/2))}; })()""")
      n=await js("S.세션.종목.length"); 나쁨=[]; 부드=None
      for j in range(1,n):
        await pg.click('.아랫줄 [data-act="본종목"]')
        if 부드 is None: await pg.wait_for_timeout(30); 부드=await 칸()
        await pg.wait_for_timeout(650); await 띠지움(); c=await 칸()
        if not (c['보임'] and (abs(c['중심차'])<=40 or c['옆'] in (0,c['끝']))): 나쁨.append(c)
      c=await 칸()
      봄(t+f"i4gd '다음'으로 끝까지 — 지금 칸이 늘 보이고 가운데쯤 (마지막 {c} · 어긋남 {나쁨 or '없음'})", not 나쁨 and c['본']==n-1)
      if 폭==420: await (await pg.query_selector('#폰')).screenshot(path='v7w_칸줄끝.png')
      await 누름('.아랫줄 [data-act="이전종목"]', 650); e=await 칸()
      봄(t+f"i4gd '이전' 도 따라감 ({e})", e['본']==n-2 and e['보임'])
      await 누름('.운띠 .운칸:first-child', 650); e=await 칸()
      봄(t+f"i4gd 칸 누르기 → 그 칸으로 · 줄도 따라감 ({e})", e['본']==0 and e['보임'] and e['옆']==0)
      # ── khmo 마우스로 누른 채 끌기 ──
      스=await js("getComputedStyle(document.querySelector('.운띠')).scrollbarWidth")
      봄(t+f"khmo 스크롤바 숨김 ({스})", 스=='none')
      box=await (await pg.query_selector('.운띠 .운칸:nth-child(3)')).bounding_box(); x0=box['x']+box['width']/2; y0=box['y']+box['height']/2
      앞=await 칸(); await pg.mouse.move(x0,y0); await pg.mouse.down()
      for s_ in range(1,11): await pg.mouse.move(x0-15*s_, y0); await pg.wait_for_timeout(16)
      await pg.mouse.up(); await pg.wait_for_timeout(250); 뒤=await 칸()
      봄(t+f"khmo 누른 채 왼쪽으로 150px 끌면 줄이 따라 움직임 ({앞['옆']} → {뒤['옆']} / 끝 {뒤['끝']})", 뒤['옆']==min(뒤['끝'],앞['옆']+150))
      봄(t+f"khmo 끌기 뒤 칸 고르기는 안 됨 (본 {앞['본']} → {뒤['본']})", 뒤['본']==앞['본'])
      await js("그리기()"); 다=await 칸()
      봄(t+f"khmo 다시 그려도 끌어 둔 자리 그대로 ({뒤['옆']} → {다['옆']})", 다['옆']==뒤['옆'])
      box=await (await pg.query_selector('.운띠 .운칸:nth-child(5)')).bounding_box()
      await pg.mouse.move(box['x']+box['width']/2, box['y']+box['height']/2); await pg.mouse.down(); await pg.mouse.move(box['x']+box['width']/2+4, box['y']+box['height']/2); await pg.mouse.up()
      await pg.wait_for_timeout(700); await 띠지움(); 짧=await 칸()
      봄(t+f"khmo 조금(8px 안) 움직인 누르기는 칸 고르기 ({짧})", 짧['본']==4 and 짧['보임'])
      # ── 다른 것 그대로 ──
      봄(t+"그림 감추면 띠 안 '그림' 단추(작은흰)", await js("(()=>{ S.세션.배너숨김=true; 그리기(); const b=document.querySelector('.운머리 [data-act=\"배너보기\"]'); const ok=!!b&&b.classList.contains('작은흰'); S.세션.배너숨김=false; 그리기(); return ok; })()"))
      봄(t+"JS 오류 없음", not 오류)
      if 오류: print(오류[:3])
      await pg.close()
    await b.close()
  [print(("✅ " if ok else "❌ ")+m) for m,ok in 결과]; print(f"\n{sum(ok for _,ok in 결과)}/{len(결과)}")
asyncio.run(main())
