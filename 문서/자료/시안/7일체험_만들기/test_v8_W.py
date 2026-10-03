"""10-03 운동 화면 ✎ 표시 4개 시험 (headless Chromium) — python3 test_v8_W.py [파일]  기본 7day-v8W.html
 r0yk [플랜] 표 · N회차 · N주 진행 중 / 5gd1 타이머 왼쪽 20px / 3ehg 지표 한 줄을 띠로 / ai39 건너뛰기 · 다음 종목 · 번호 체크 · 휴지통 · kg 60% · 회 75%"""
import asyncio, sys, json, os
SP=os.path.dirname(os.path.abspath(__file__))
상태=json.load(open(SP+'/marks3/28f858e336f21768f689d786826649de.json'))   # ✎ 0yyw 때 상태 (가슴·어깨 10종목 · 막 시작)
from playwright.async_api import async_playwright
F='file://'+SP+'/'+(sys.argv[1] if len(sys.argv)>1 else '7day-v8W.html')
글자들={11,13,15,18,22,28}
async def main():
  결과=[]
  def 봄(m,ok): 결과.append((m,bool(ok)))
  async with async_playwright() as p:
    b=await p.chromium.launch()
    for 폭,높이 in [(420,1080),(360,800)]:
      pg=await b.new_page(viewport={'width':폭,'height':높이}); 오류=[]; pg.on('pageerror',lambda e:오류.append(str(e)))
      await pg.goto(F); await pg.wait_for_timeout(5600); t=f"[{폭}] "
      async def js(x, a=None): return await (pg.evaluate(x, a) if a is not None else pg.evaluate(x))
      async def 띠지움(): await js("(()=>{ if(U.업적띠){ U.업적띠=null; 그리기(); } })()")
      async def 누름(sel, 쉼=150): await pg.click(sel); await pg.wait_for_timeout(쉼); await 띠지움()
      async def 처음(): await js("s=>{ S=JSON.parse(JSON.stringify(s.S)); Object.assign(U, s.U, {시트:null, 업적띠:null, 운지움:null}); 그리기(); }", 상태); await pg.wait_for_timeout(300)
      async def 자리(): return await js("[...document.querySelectorAll('.운머리,.배너,.운세트들,.운아래,.아랫줄')].map(x=>Math.round(x.getBoundingClientRect().top)+':'+Math.round(x.getBoundingClientRect().height))")
      await 처음()
      i플=await js("S.세션.종목.findIndex(e=>e.플랜id)"); i보=await js("S.세션.종목.findIndex(e=>!e.플랜id)")
      봄(t+f"표시 때 상태 ({await js('S.세션.종목.length')}종목 · 플랜 종목 {i플+1}번)", i플>=0 and i보>=0)
      await js(f"(()=>{{ U.본={i플}; 그리기(); }})()")
      # ── r0yk ──
      r=await js("""(()=>{ const m=document.querySelector('.운머리'), e=S.세션.종목[U.본], p=플랜(e.플랜id), 주=Math.floor(날차(p.만든날,오늘())/7)+1;
        const 곁=m.querySelector('.운곁'), 표=m.querySelector('.운플랜표'), 끝=m.querySelector('.운끝말'), rg=document.createRange(); rg.selectNodeContents(끝.firstChild);
        const 글=[...rg.getClientRects()].pop(), 표r=표.getBoundingClientRect(), 띠=m.getBoundingClientRect(), z=getComputedStyle(표), 앞=getComputedStyle(표,'::before');
        const c=document.createElement('canvas').getContext('2d'), cs=getComputedStyle(m.querySelector('.운이름')); c.font=`${cs.fontWeight} ${cs.fontSize} ${cs.fontFamily}`; const mt=c.measureText(끝.firstChild.textContent);
        const 잉크위=글.top+(글.height-(mt.fontBoundingBoxAscent+mt.fontBoundingBoxDescent))/2+mt.fontBoundingBoxAscent-mt.actualBoundingBoxAscent;
        const 색=v=>{ const d=document.createElement('i'); d.style.color=getComputedStyle(document.documentElement).getPropertyValue(v); document.body.appendChild(d); const x=getComputedStyle(d).color; d.remove(); return x; };
        return {곁:곁.textContent, 기대:`${e.회}회차${e.측정일?" · 측정일":""} · ${주}주 진행 중`, 표글:앞.content, 바탕:z.backgroundColor===색('--강조'), 글색:z.color===색('--강조글'), 테:[z.borderTopWidth, z.borderTopColor===색('--강조글')],
          크기:[z.fontSize,z.fontWeight,z.borderTopLeftRadius], 가로겹침:Math.round((글.right-표r.left)*10)/10, 세로겹침:Math.round((표r.bottom-잉크위)*10)/10, 같은줄:표r.top<글.bottom&&표r.bottom>글.top-12,
          띠안:표r.top>=띠.top&&표r.right<=띠.right, 이름글:m.querySelector('.운이름').textContent===e.이름}; })()""")
      봄(t+f"r0yk 띠 글 '{r['곁']}' (플랜 빼고 N회차 · N주 진행 중)", r['곁']==r['기대'] and not r['곁'].startswith('플랜'))
      봄(t+f"r0yk [플랜] 표 = 강조 바탕 · 강조글 · 1px 강조글 테두리 · 11 Bold · 모서리 8 ({r['표글']} {r['크기']} {r['테']})", r['표글']=='"플랜"' and r['바탕'] and r['글색'] and r['테']==['1px',True] and r['크기']==['11px','700','8px'])
      봄(t+f"r0yk 표가 이름 끝 오른쪽 위에 5px 겹침 (가로 {r['가로겹침']} · 세로 {r['세로겹침']}) · 띠 안 · 이름 글은 그대로", abs(r['가로겹침']-5)<=0.6 and abs(r['세로겹침']-5)<=1 and r['띠안'] and r['같은줄'] and r['이름글'])
      봄(t+"r0yk 플랜 아닌 종목엔 표 · 회차 없음", await js(f"(()=>{{ U.본={i보}; 그리기(); const ok=!document.querySelector('.운머리 .운플랜표')&&!document.querySelector('.운머리 .운곁'); U.본={i플}; 그리기(); return ok; }})()"))
      # ── 5gd1 ──
      시=await js("(()=>{ const m=document.querySelector('.운머리').getBoundingClientRect(), s=document.querySelector('.운머리 [data-timer=\"경과\"]').getBoundingClientRect(); return Math.round(m.right-s.right); })()")
      봄(t+f"5gd1 타이머 오른쪽 끝 = 띠 끝에서 {시}px (여백 12 + 20)", 시==32)
      # ── 3ehg ──
      수=await js("""(()=>{ const m=document.querySelector('.운머리'), l=m.querySelector('.운수치'), ch=[...l.children], 위=new Set(ch.map(c=>{const r=c.getBoundingClientRect(); return Math.round(r.top+r.height/2);}));
        const 강=getComputedStyle(m).color;
        return {글:l.textContent.replace(/\\s+/g,' ').trim(), 띠안:m.contains(l), 한줄:위.size===1 && Math.round(l.getBoundingClientRect().height)===28, 넘침:l.scrollWidth>l.clientWidth+1,
          크기:[...new Set([...l.querySelectorAll('span,b')].filter(x=>x.offsetParent).map(x=>parseFloat(getComputedStyle(x).fontSize)))].sort((a,b)=>a-b), 바탕:getComputedStyle(l).fontSize,
          글색:[...l.querySelectorAll('span,b,i')].every(x=>getComputedStyle(x).color===강), 좁:l.classList.contains('좁'),
          아래:document.querySelector('.운아래').firstElementChild.className, 아래지표:!!document.querySelector('.운아래 .운지표, .운아래 .운지표들') || /1RM|달성/.test(document.querySelector('.운아래').textContent)}; })()""")
      봄(t+f"3ehg 지표가 띠 둘째 줄 한 줄 ({수['글']})", 수['띠안'] and 수['한줄'] and all(k in 수['글'] for k in ['1RM','1주','최고','달성','볼륨']))
      봄(t+f"3ehg 가로 넘침 없음 · 글자 {수['바탕']} ({수['크기']}{' · 좁은 폰: 볼륨 목표 감춤' if 수['좁'] else ''})", not 수['넘침'] and (폭!=420 or set(수['크기'])<=글자들))
      봄(t+"3ehg 띠 위 글 · ▲▼ 모두 강조글", 수['글색'])
      봄(t+f"3ehg 아래 상자 = 루틴 요약부터 (지표 없음 · 첫 칸 {수['아래']})", not 수['아래지표'] and '운요약' in 수['아래'])
      화=await js("""(()=>{ const ss=S.세션, s=ss.종목[U.본].세트[0], 원=s.w; s.w=300; 체크(U.본,0); U.업적띠=null; 그리기();
        const 오=[...document.querySelectorAll('.운수치 .운칩.오름')].map(x=>x.textContent); 체크(U.본,0); s.w=5; 체크(U.본,0); U.업적띠=null; 그리기();
        const 내=[...document.querySelectorAll('.운수치 .운칩.내림')].map(x=>x.textContent); 체크(U.본,0); s.w=원; ss.휴식=null; 그리기(); return {오,내}; })()""")
      봄(t+f"3ehg ▲ · ▼ 기호로 방향 ({화['오']} / {화['내']})", 화['오'] and 화['내'] and all('▲' in x for x in 화['오']) and all('▼' in x for x in 화['내']))
      if 폭==420: await (await pg.query_selector('#폰')).screenshot(path='v8w_처음.png')
      앞=await 자리(); await 누름('.운세트들 [data-act="체크"]'); 뒤=await 자리()
      봄(t+f"첫 체크에도 칸 자리 · 높이 그대로 ({앞} → {뒤})", 앞==뒤)
      # ── ai39 세트 줄 ──
      await 처음(); await js(f"(()=>{{ U.본={i플}; 그리기(); }})()")
      줄=await js("""(()=>{ const r=document.querySelector('.운세트들 .세트줄'), c=[...r.children];
        return {첫:c[0].tagName+'.'+c[0].className+'|'+c[0].dataset.act+'|'+c[0].textContent.trim(), 끝:c[c.length-1].className+'|'+c[c.length-1].dataset.act+'|'+!!c[c.length-1].querySelector('svg'),
          오른체크:[...document.querySelectorAll('.운세트들 .세트줄')].some(x=>x.lastElementChild.dataset.act==='체크'), 빼기:!!document.querySelector('#폰 [data-act="세트빼기"]'), 더:!!document.querySelector('.운세트들 [data-act="세트더"]'),
          머리:[...document.querySelector('.운세트들 .세트머리').children].map(x=>x.textContent)}; })()""")
      봄(t+f"ai39 맨 왼쪽 = 세트 번호 동그라미 체크 ({줄['첫']})", 줄['첫'].startswith('BUTTON.') and '세트번호' in 줄['첫'] and '|체크|1' in 줄['첫'])
      봄(t+f"ai39 맨 오른쪽 = 휴지통 ({줄['끝']}) · 오른쪽 체크 없음", 줄['끝']=='세트지움|세트지움|true' and not 줄['오른체크'])
      봄(t+f"ai39 '− 세트' 없음 · '+ 세트' 있음 · 머리 {줄['머리']}", not 줄['빼기'] and 줄['더'] and 줄['머리']==['세트','kg','회','휴식',''])
      동=await js("""(()=>{ const b=()=>document.querySelector('.운세트들 .세트줄 .세트번호'); b().click(); const a=[S.세션.종목[U.본].세트[0].완료, b().classList.contains('켬'), b().textContent.trim(), getComputedStyle(b()).backgroundColor, getComputedStyle(b()).borderRadius, b().offsetWidth];
        b().click(); return a.concat([S.세션.종목[U.본].세트[0].완료, b().classList.contains('켬')]); })()""")
      봄(t+f"ai39 번호 누르면 체크(채운 동그라미 + 번호) · 다시 누르면 풀기 ({동})", 동[0] is True and 동[1] and 동[2]=='1' and 동[4]=='50%' and 동[5]==28 and 동[6] is False and not 동[7])
      await js("(()=>{ S.세션.휴식=null; 그리기(); })()"); await 띠지움()
      # 칸 폭 — 홍겸 님 폰(폭 420)에서 kg 78 · 회 98 (지금 130 의 60% · 75%)
      칸=await js("""(()=>{ const 폰=document.getElementById('폰'), 원=폰.getAttribute('style'); 폰.style.flex='none'; 폰.style.width='420px'; 그리기();
        const w=s=>Math.round(document.querySelector('.운세트들 .세트줄 '+s).getBoundingClientRect().width); const r=[w('.값칸:nth-of-type(1)'),w('.값칸:nth-of-type(2)'),w('.휴식칸')];
        if(원==null) 폰.removeAttribute('style'); else 폰.setAttribute('style',원); 그리기(); return r; })()""")
      봄(f"[폰 420] ai39 kg 칸 {칸[0]} (130의 {칸[0]/130:.0%}) · 회 칸 {칸[1]} ({칸[1]/130:.0%}) · 휴식 {칸[2]}", abs(칸[0]-78)<=1 and abs(칸[1]-98)<=1)
      값=await js("""(()=>{ const s=S.세션.종목[U.본].세트[0], 원=s.w; s.w=102.5; 그리기(); const 넘=[...document.querySelectorAll('.운세트들 .값칸 input')].filter(x=>x.scrollWidth>x.clientWidth+1).map(x=>x.value);
        const 크=getComputedStyle(document.querySelector('.운세트들 .값칸 input')).fontSize; const w=s=>Math.round(document.querySelector('.운세트들 .세트줄 '+s).getBoundingClientRect().width);
        const 폭=[w('.값칸:nth-of-type(1)'),w('.값칸:nth-of-type(2)'),w('.휴식칸')]; s.w=원; 그리기(); return {넘, 크, 폭}; })()""")
      봄(t+f"ai39 칸 {값['폭']} · '102.5' 도 안 잘림 (글자 {값['크']}) · 넘친 값 {값['넘'] or '없음'}", not 값['넘'] and 값['폭'][0]>=76 and 값['폭'][1]>=64 and 값['폭'][2]>=56)
      # 쉬는 동안 건너뛰기 — 중간 세트
      await 누름('.운세트들 .세트줄:nth-of-type(2) .세트번호')
      쉼=await js("""(()=>{ const c=document.querySelector('.운세트들 .휴식칸.쉼'); if(!c) return null; const s=c.querySelector('.밑 small'), r=s.getBoundingClientRect(), cr=c.getBoundingClientRect();
        return {글:c.querySelector('.밑').textContent, 보임:r.width>0&&r.left>=cr.left-1&&r.right<=cr.right+1&&r.top>=cr.top-1&&r.bottom<=cr.bottom+1, 높이:Math.round(cr.height), 동작:c.dataset.act}; })()""")
      봄(t+f"ai39 쉬는 동안 휴식 칸에 '건너뛰기' ({쉼})", 쉼 and '건너뛰기' in 쉼['글'] and 쉼['보임'] and 쉼['높이']==40 and 쉼['동작']=='휴식건너뛰기')
      if 폭==420: await (await pg.query_selector('#폰')).screenshot(path='v8w_쉼.png')
      본0=await js("U.본"); await 누름('.운세트들 .휴식칸.쉼'); 건=await js("({휴식:S.세션.휴식, 본:U.본})")
      봄(t+f"ai39 중간 세트 건너뛰기 → 휴식 끝 · 종목 그대로 ({본0} → {건['본']})", 건['휴식'] is None and 건['본']==본0)
      # 마지막 세트 → 건너뛰면 다음 안 끝난 종목으로 · 칸 줄도 따라감
      n=await js("S.세션.종목[U.본].세트.length")
      for _ in range(n):
        el=await pg.query_selector('.운세트들 .세트번호:not(.켬)')
        if not el: break
        await el.click(); await pg.wait_for_timeout(80); await 띠지움()
      쉼2=await js("(()=>{ const h=S.세션.휴식; return h?{i:h.i,k:h.k,다:S.세션.종목[h.i].세트.every(s=>s.완료)}:null; })()")
      기대=await js(f"S.세션.종목.findIndex((x,j)=>j>{본0}&&x.세트.some(s=>!s.완료))")
      await 누름('.운세트들 .휴식칸.쉼', 700)
      칸줄=await js("(()=>{ const 줄=document.querySelector('.운띠'), c=줄.querySelector('.운칸.지금'), a=줄.getBoundingClientRect(), b=c.getBoundingClientRect(); return {본:U.본, 휴식:S.세션.휴식, 보임:b.left>=a.left-1&&b.right<=a.right+1, 칸:+c.dataset.i}; })()")
      봄(t+f"ai39 마지막 세트 휴식 건너뛰기 → 다음 종목 ({본0+1} → {칸줄['본']+1}, 기대 {기대+1}) · 칸 줄 따라감", 쉼2 and 쉼2['다'] and 칸줄['본']==기대 and 칸줄['휴식'] is None and 칸줄['보임'] and 칸줄['칸']==기대)
      # 맨 뒤 종목이 끝나면 앞쪽 안 끝난 종목으로
      뒤=await js("""(()=>{ const ss=S.세션, n=ss.종목.length-1; U.본=n; ss.종목[n].세트.forEach(s=>s.완료=false); 그리기(); ss.종목[n].세트.forEach((s,k)=>{ if(k<ss.종목[n].세트.length-1) s.완료=true; });
        체크(n, ss.종목[n].세트.length-1); 그리기(); document.querySelector('.운세트들 .휴식칸.쉼').click(); return {본:U.본, 기대:ss.종목.findIndex(x=>x.세트.some(s=>!s.완료))}; })()""")
      봄(t+f"ai39 맨 뒤 종목 건너뛰기 → 앞쪽 첫 안 끝난 종목 ({뒤})", 뒤['본']==뒤['기대'] and 뒤['본']>=0)
      # ── 휴지통 · 되돌리기 ──
      await 처음(); await js(f"(()=>{{ U.본={i플}; 그리기(); }})()")
      지=await js("""(()=>{ const x=S.세션.종목[U.본]; x.세트.forEach((s,k)=>{ s.w=60+k; s.r=5+k; }); 그리기(); const 앞=x.세트.map(s=>s.w+'x'+s.r);
        document.querySelectorAll('.운세트들 .세트지움')[1].click(); const 뒤=x.세트.map(s=>s.w+'x'+s.r), 띠=document.querySelector('#폰 .운지움띠');
        const 묻=!!document.querySelector('#폰 .가림, #폰 .시트'); return {앞, 뒤, 띠:띠?.textContent.trim(), 단추:띠?.querySelector('[data-act="세트되돌림"]')?.textContent, 묻}; })()""")
      봄(t+f"ai39 휴지통 → 묻지 않고 그 세트만 지움 ({지['앞']} → {지['뒤']})", 지['뒤']==지['앞'][:1]+지['앞'][2:] and not 지['묻'])
      봄(t+f"ai39 아래띠 '{지['띠']}'", 지['띠'] and '2세트' in 지['띠'] and 지['단추']=='되돌리기')
      if 폭==420: await (await pg.query_selector('#폰')).screenshot(path='v8w_지움.png')
      await 누름('#폰 .운지움띠 [data-act="세트되돌림"]')
      되=await js("({세트:S.세션.종목[U.본].세트.map(s=>s.w+'x'+s.r), 띠:!!document.querySelector('#폰 .운지움띠')})")
      봄(t+f"ai39 되돌리기 → 같은 자리로 ({되['세트']})", 되['세트']==지['앞'] and not 되['띠'])
      await js("document.querySelectorAll('.운세트들 .세트지움')[0].click()"); await pg.wait_for_timeout(5400)
      봄(t+"ai39 아래띠는 5초 뒤 사라짐", await js("!document.querySelector('#폰 .운지움띠') && U.운지움==null"))
      # 쉬는 세트보다 앞 세트를 지우면 휴식 칸이 같은 세트에 남는다 · 쉬는 세트를 지우면 휴식 끝
      휴=await js("""(()=>{ const ss=S.세션, i=U.본, x=ss.종목[i]; while(x.세트.length<4) x.세트.push({...x.세트[0], 완료:false}); x.세트.forEach(s=>s.완료=false); ss.휴식=null; 그리기();
        체크(i,2); 그리기(); const 그세트=x.세트[2]; document.querySelectorAll('.운세트들 .세트지움')[0].click();
        const a=ss.휴식&&x.세트[ss.휴식.k]===그세트 && document.querySelectorAll('.운세트들 .세트줄')[ss.휴식.k].querySelector('.휴식칸.쉼')!=null;
        document.querySelectorAll('.운세트들 .세트지움')[ss.휴식.k].click(); return {앞지움:!!a, 쉼지움:ss.휴식==null}; })()""")
      봄(t+f"ai39 지울 때 휴식 칸이 따라감 ({휴})", 휴['앞지움'] and 휴['쉼지움'])
      하=await js("""(()=>{ const x=S.세션.종목[U.본]; x.세트.splice(1); 그리기(); const b=document.querySelector('.운세트들 .세트지움'); const d=b.disabled; b.click(); 그리기(); return {꺼짐:d, 남음:x.세트.length}; })()""")
      봄(t+f"ai39 세트가 하나면 휴지통 꺼짐 · 안 지워짐 ({하})", 하['꺼짐'] and 하['남음']==1)
      # ── 그대로여야 하는 것: 새 체크 자리로 자동 스크롤 · 맨 아래 체크해도 안 튐 ──
      await 처음(); await js(f"(()=>{{ U.본={i플}; 그리기(); }})()")
      for _ in range(5): await 누름('.운세트들 [data-act="세트더"]', 80)
      async def 목(): return await js("(()=>{const l=document.querySelector('.운세트들'); return {위:Math.round(l.scrollTop), 끝:l.scrollHeight-l.clientHeight, 높:l.clientHeight}})()")
      m0=await 목()
      k=await js("""(()=>{ const l=document.querySelector('.운세트들'), 줄=[...l.querySelectorAll('.세트줄')], 위=l.getBoundingClientRect().top;
        return 줄.findIndex((r,j)=>{ const b=r.getBoundingClientRect(); return b.top+b.height/2-위>l.clientHeight/2 && b.bottom-위<=l.clientHeight+b.height/2 && j+1<줄.length; }); })()""")
      els=await pg.query_selector_all('.운세트들 .세트번호'); await els[k].click(); await pg.wait_for_timeout(800); await 띠지움(); m1=await 목()
      봄(t+f"h6tu 절반 아래 {k+1}세트 번호 체크 → 목록이 올라감 ({m0} → {m1['위']})", m0['끝']>0 and m1['위']>m0['위'])
      await js("document.querySelector('.운세트들').scrollTop=99999"); await pg.wait_for_timeout(150); m2=await 목()
      els=await pg.query_selector_all('.운세트들 .세트번호'); await els[-1].click(); await pg.wait_for_timeout(700); await 띠지움(); m3=await 목()
      봄(t+f"9a8l 맨 아래 세트 체크 → 맨 위로 안 튐 ({m2['위']} → {m3['위']})", m3['위']==m2['위'] and m2['위']>0)
      봄(t+f"세트 목록 높이 {m0['높']}px", m0['높']>0)
      # ── 모든 종목: 이름 안 잘림(두 줄) · 가로 넘침 없음 ──
      나=await js("""(()=>{ const 나쁨=[]; const n=S.세션.종목.length;
        for(let i=0;i<n;i++){ U.본=i; 그리기(); const m=document.querySelector('.운머리'), 이=m.querySelector('.운이름');
          const 줄=new Set([...이.getClientRects()].map(r=>Math.round(r.top))).size;
          if(이.textContent!==S.세션.종목[i].이름 || 줄>2) 나쁨.push(i+':'+이.textContent+':'+줄+'줄');
          for(const c of ['#폰','.운머리','.운첫줄','.운수치','.운세트들','.운아래','.아랫줄']){ const e=document.querySelector(c); if(e && e.scrollWidth>e.clientWidth+1) 나쁨.push(i+' 넘침 '+c); }
          document.querySelectorAll('.운세트들 .세트줄').forEach((r,k)=>{ if(r.scrollWidth>r.clientWidth+1) 나쁨.push(i+' 세트줄 '+k); }); }
        U.본=0; 그리기(); return 나쁨; })()""")
      봄(t+f"모든 종목 이름 안 잘림(두 줄 안) · 가로 넘침 없음 ({나 or '이상 없음'})", not 나)
      if 폭==360:
        await js(f"(()=>{{ U.본={i플}; 그리기(); }})()"); await pg.wait_for_timeout(300)
        await (await pg.query_selector('#폰')).screenshot(path='v8w_360.png')
      봄(t+"JS 오류 없음", not 오류)
      if 오류: print(오류[:3])
      await pg.close()
    await b.close()
  [print(("✅ " if ok else "❌ ")+m) for m,ok in 결과]; print(f"\n{sum(ok for _,ok in 결과)}/{len(결과)}")
asyncio.run(main())
