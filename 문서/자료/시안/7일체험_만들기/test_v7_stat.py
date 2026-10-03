"""10-03 스탯 · 업적 ✎ 표시 6개 반영 시험 (headless Chromium) — python3 test_v7_stat.py [파일]  기본 7day-v7stat.html"""
import asyncio, sys, json
from playwright.async_api import async_playwright
F='file:///tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'+(sys.argv[1] if len(sys.argv)>1 else '7day-v7stat.html')
async def main():
  결과=[]
  def 봄(m,ok): 결과.append((m,bool(ok)))
  async with async_playwright() as p:
    b=await p.chromium.launch()
    for 폭,높이 in [(420,1080),(360,800)]:
      pg=await b.new_page(viewport={'width':폭,'height':높이}); 오류=[]; pg.on('pageerror',lambda e:오류.append(str(e)))
      await pg.goto(F); await pg.wait_for_timeout(700); t=f"[{폭}] "
      async def 해(js, 기다림=250): r=await pg.evaluate(js); await pg.wait_for_timeout(기다림); return r
      async def 누름(sel, 기다림=350): await pg.click(sel); await pg.wait_for_timeout(기다림)
      async def 사진(이름): await (await pg.query_selector('#폰')).screenshot(path=f'v7stat_{폭}_{이름}.png')
      async def 넘침():
        return await pg.evaluate("""(()=>{ const 폰=document.getElementById('폰'), 나=[];
          if(document.documentElement.scrollWidth>innerWidth+1) 나.push('페이지');
          폰.querySelectorAll('*').forEach(el=>{ if(el.closest('.가로밀기')||el.closest('svg')) return; const r=el.getBoundingClientRect(), f=폰.getBoundingClientRect();
            if(r.width>0 && (r.right>f.right+1 || r.left<f.left-1)) 나.push((el.className&&el.className.baseVal===undefined?el.className:el.tagName)+':'+(el.textContent||'').trim().slice(0,12)); });
          폰.querySelectorAll('.스탯칸,.체력칸,.스탯그래프 .줄,.그래프글,.스탯아래,.업적위 .칩줄,.띠').forEach(el=>{ if(el.scrollWidth>el.clientWidth+1) 나.push('안넘침:'+el.className+':'+el.textContent.trim().slice(0,12)); });
          return 나.slice(0,5); })()""")
      # ── 0. 캘린더에서 열기 — data-v 로 보기 고르기 · 띠 칩(값 없음)은 스탯 ──
      await 해("U.업적띠=null; 행동('스탯열기',{v:'업적'}); 그리기()")
      봄(t+"열기: 스탯열기 v=업적 → 업적 보기", await 해("U.스탯?.보기")=='업적')
      await 해("행동('탭',{t:'캘린더'})")
      칩=await pg.query_selector('.칭호칩[data-act="스탯열기"]')
      if 칩: await 칩.click(); await pg.wait_for_timeout(300)
      else: await 해("행동('스탯열기',{})")
      봄(t+"열기: 값 없는 스탯열기 → 스탯 보기 · 그래프는 닫힌 채", await 해("U.스탯?.보기==='스탯' && U.스탯.고름==null"))
      # ── 1. rm2x · wuyc — 맨 위 줄 없음 · [스탯][업적] 은 탭 줄 바로 위 · 탭 줄 보임 ──
      위=await 해("""(()=>{ const 폰=document.getElementById('폰'), 화=폰.querySelector('.화면'), 첫=화.firstElementChild, 아=화.querySelector('.스탯아래'), 탭=폰.querySelector('.탭줄');
        const a=아?.getBoundingClientRect(), z=탭?.getBoundingClientRect();
        return {머리:!!폰.querySelector('.스탯머리,[data-act="스탯닫기"]'), 첫:첫?.className, 탭:!!탭, 칩:[...(아?.querySelectorAll('[data-act="스탯보기"]')||[])].map(x=>x.textContent+(x.classList.contains('켬')?'*':'')),
          붙음: a&&z ? Math.round(z.top-a.bottom) : null, 풀이:!!폰.querySelector('.스탯풀이'), 글:화.innerText}; })()""")
      봄(t+f"rm2x 맨 위 줄(✕ · 스탯 · 업적) 없음 · 첫 칸 = {위['첫']}", not 위['머리'] and '스탯속' in (위['첫'] or ''))
      봄(t+f"wuyc [스탯][업적] 이 탭 줄 바로 위 ({위['칩']} · 틈 {위['붙음']}px)", 위['칩']==['스탯*','업적'] and 위['붙음']==0 and 위['탭'])
      봄(t+"wuyc 맨 아래 설명 줄(스탯풀이) 없음", not 위['풀이'] and '10월' not in 위['글'] and '→ 지금' not in 위['글'])
      # ── 2. gows — '일부' 없음 · 값 없는 칸 = 빈 게이지 + 현재 측정값 없음 ──
      g=await 해("""(()=>{ const {값}=스탯계산(오늘()), 없=스탯표.목록.filter(s=>값[s.이름]==null).map(s=>s.이름), 폰=document.getElementById('폰');
        const 칸=[...폰.querySelectorAll('.스탯칸,.체력칸')], 빈=칸.filter(c=>c.querySelector('.없음글'));
        return {없, 빈:빈.map(c=>c.dataset.v), 빈게이지:빈.every(c=>{ const m=c.querySelector('.막대'); return m && !m.querySelector('i') && c.querySelector('.없음글').textContent==='현재 측정값 없음'; }),
          일부:/일부/.test(폰.querySelector('.스탯속').innerText), 줄표:칸.some(c=>/—/.test(c.innerText)), 뒤:칸.some(c=>/입력 뒤|합의 뒤|세트 뒤|대화로 정함|기록이 아직/.test(c.innerText))}; })()""")
      봄(t+f"gows 값 없는 스탯 {len(g['없'])}개 = 빈 게이지 + '현재 측정값 없음' ({', '.join(g['빈'])})", sorted(g['없'])==sorted(g['빈']) and g['빈게이지'] and len(g['없'])>0)
      봄(t+"gows '—' · '~ 입력 뒤' 설명 없음", not g['줄표'] and not g['뒤'])
      봄(t+"gows '일부' 표시 없음", not g['일부'])
      n=await 넘침(); 봄(t+f"스탯 보기 가로 넘침 없음 {n}", not n)
      await 사진('스탯')
      # ── 3. wuyc 그래프 — 칸을 누르면 그래프 · [일][주][달][년] · 고정 축 · 막대는 점보다 15px 아래 ──
      await 누름('.스탯칸[data-v="근력"]')
      gr=await 해("""(()=>{ const g=document.querySelector('.스탯그래프'); if(!g) return null; const 넘=document.querySelector('.스탯속').getBoundingClientRect(), 칸=document.querySelector('.스탯칸.고름').getBoundingClientRect();
        return {이름:g.querySelector('.그래프이름').textContent, 칩:[...g.querySelectorAll('[data-act="스탯단위"]')].map(x=>x.textContent+(x.classList.contains('켬')?'*':'')),
          위글:[...g.querySelectorAll('svg text')].slice(0,2).map(x=>x.textContent), 점:g.querySelectorAll('svg circle').length, 선:!!g.querySelector('polyline.선'), 글:g.querySelector('.그래프글')?.textContent.replace(/\\s+/g,' ').trim(),
          보임: 칸.top>=넘.top-1 && 칸.bottom<=넘.bottom+1, 아래:Math.round(document.querySelector('.스탯아래').getBoundingClientRect().top-g.getBoundingClientRect().bottom)}; })()""")
      봄(t+f"wuyc 근력을 누르면 그래프 ({gr and gr['이름']} · {gr and gr['칩']})", gr and gr['이름']=='근력' and gr['칩']==['일*','주','달','년'])
      봄(t+f"wuyc 선 = 1RM · 고정 축 맨 위 = 3대 500 ({gr and gr['위글']})", gr and gr['선'] and gr['위글'][0]=='3대 500 수준 · 500kg' and gr['위글'][1]=='볼륨 12,000kg')
      봄(t+f"그래프는 [스탯][업적] 바로 위 · 누른 칸이 가려지지 않음 (틈 {gr and gr['아래']} · 보임 {gr and gr['보임']})", gr and gr['아래']==0 and gr['보임'])
      print(t+'근력 요약:', gr and gr['글'])
      await 사진('그래프근력')
      검사="""(()=>{ const g=document.querySelector('.스탯그래프 svg'); const 점=[...g.querySelectorAll('circle')].map(c=>[+c.getAttribute('cx'),+c.getAttribute('cy')]);
        const 막=[...g.querySelectorAll('rect.막')].map(r=>[+r.getAttribute('x')+(+r.getAttribute('width'))/2,+r.getAttribute('y')]); let 나쁨=[];
        for(const [x,y] of 막){ const p=점.find(([px])=>Math.abs(px-x)<0.6); if(p && y < p[1]+15-0.6) 나쁨.push([x,y,p[1]]); }
        const vb=g.viewBox.baseVal, 실=g.getBoundingClientRect();
        return {점:점.length, 막:막.length, 나쁨, 비:+(실.width/vb.width).toFixed(3), 칸:document.querySelector('.스탯그래프 [data-act="스탯단위"].켬')?.textContent}; })()"""
      모두=[]
      for 스탯 in ['근력','내 수행능력','밀기','당기기','하체','체력','균형','작업량','성실']:
        await 해(f"U.스탯.고름='{스탯}'; 그리기()",80)
        for 단위 in ['일','주','달','년']:
          await 누름(f'.스탯그래프 [data-act="스탯단위"][data-v="{단위}"]',120)
          r=await 해(검사,30); 모두.append((스탯,단위,r))
      봄(t+f"wuyc 막대 꼭대기는 같은 칸 점보다 15px 이상 아래 ({sum(r['막'] for *_,r in 모두)}개 막대)", all(not r['나쁨'] for *_,r in 모두))
      봄(t+"wuyc [일][주][달][년] 모두 그려지고 맨 오른쪽(지금) 칸에 점", all(r['점']>=1 and r['칸']==u for _,u,r in 모두))
      봄(t+f"그래프 1 단위 = 1px (viewBox 와 실제 폭 비 {모두[0][2]['비']})", all(abs(r['비']-1)<0.02 for *_,r in 모두))
      print(t+'칸별 점·막대:', ' '.join(f"{s}/{u}:{r['점']}·{r['막']}" for s,u,r in 모두 if s in ('근력','균형')))
      await 해("U.스탯.고름='밀기'; U.스탯.단위='일'; 그리기()")
      await 사진('그래프밀기')
      n=await 넘침(); 봄(t+f"그래프 열린 채 가로 넘침 없음 {n}", not n)
      # 값 없는 스탯
      await 해("U.스탯.고름=null; 그리기()"); await 누름('.스탯칸[data-v="폭발력"]')
      e=await 해("(()=>{ const g=document.querySelector('.스탯그래프 svg'); return {글:g.querySelector('.빈글')?.textContent, 점:g.querySelectorAll('circle').length, 막:g.querySelectorAll('rect').length}; })()")
      봄(t+f"값 없는 스탯 그래프 = 빈 판 + '현재 측정값 없음' ({e})", e['글']=='현재 측정값 없음' and e['점']==0 and e['막']==0)
      # 닫기 — 같은 칸 다시 · ✕
      await 누름('.스탯칸[data-v="폭발력"]'); 봄(t+"같은 칸을 다시 누르면 그래프 닫힘", await 해("!document.querySelector('.스탯그래프')"))
      await 누름('.체력칸'); 봄(t+"체력 칸도 그래프", await 해("document.querySelector('.스탯그래프 .그래프이름')?.textContent==='체력'"))
      await 누름('.스탯그래프 .닫기x'); 봄(t+"✕ 로 그래프 닫힘", await 해("!document.querySelector('.스탯그래프')"))
      # ── 4. 나가기 — 탭 줄 · Esc ──
      await 누름('.탭줄 [data-t="캘린더"]'); 봄(t+"rm2x 탭 줄 [캘린더] → 스탯 화면 닫힘", await 해("U.스탯===null && !!document.querySelector('.달력')"))
      await 해("행동('스탯열기',{v:'스탯'})"); await pg.keyboard.press('Escape'); await pg.wait_for_timeout(200)
      봄(t+"Esc → 닫힘", await 해("U.스탯===null"))
      # ── 5. 업적 — 띠 · 요약 줄 없음 · [달성][전체][+] ──
      await 해("행동('스탯열기',{v:'스탯'})"); await 누름('.스탯아래 [data-v="업적"]')
      u=await 해("""(()=>{ const 화=document.querySelector('#폰 .화면'), 띠=화.firstElementChild, cs=getComputedStyle(띠), 루=getComputedStyle(document.documentElement);
        const 색=v=>{const d=document.createElement('i'); d.style.color=루.getPropertyValue(v); document.body.appendChild(d); const c=getComputedStyle(d).color; d.remove(); return c;};
        const 얻=S.업적||{}, 풀=업적표.filter(a=>얻[a.번호]), 분모=업적표.filter(a=>!a.숨김).length+풀.filter(a=>a.숨김).length;
        const 칩=[...화.querySelector('.업적위 .칩줄').querySelectorAll('.칩')].map(x=>x.textContent+(x.classList.contains('켬')?'*':''));
        return {띠:띠.classList.contains('띠'), 글:띠.innerText.replace(/\\s+/g,' ').trim(), 바:cs.backgroundColor===색('--강조'), 글색:cs.color===색('--강조글'), 높:Math.round(띠.getBoundingClientRect().height),
          기대:`업적 ${풀.length}/${분모}`, 칩, 화글:화.innerText, 분류:U.스탯.분류}; })()""")
      봄(t+f"imv8 맨 위 띠 '{u['글']}' (기대 {u['기대']} · 숨은 것은 달성한 것만 전체에)", u['띠'] and u['글']==u['기대'] and u['바'] and u['글색'] and u['높']>=40)
      봄(t+"qat3 요약 줄(풀림 n/107 · 업적 · 숨은 n/n) 없음 · '풀림' 말 없음", '풀림' not in u['화글'] and '숨은 1/' not in u['화글'] and '/107' not in u['화글'])
      봄(t+f"jdyt 칩 [달성][전체][+] · 달성 먼저 ({u['칩']})", u['칩']==['달성*','전체','+'] and u['분류']=='달성')
      await 사진('업적')
      # 숨은 것 달성하면 분모도 +1, 보통 것은 분자만
      cnt=await 해("""(()=>{ const 글=()=>document.querySelector('#폰 .화면 > .띠').innerText.replace(/\\s+/g,' ').trim(); const 앞=글(); const 원=JSON.stringify(S.업적);
        const 숨=업적표.find(a=>a.숨김&&!S.업적[a.번호]), 보=업적표.find(a=>!a.숨김&&!S.업적[a.번호]);
        S.업적[숨.번호]={날:오늘(),순:900}; 그리기(); const 숨뒤=글(); S.업적[보.번호]={날:오늘(),순:901}; 그리기(); const 보뒤=글();
        S.업적=JSON.parse(원); 그리기(); return [앞,숨뒤,보뒤]; })()""")
      def 수(x): a,b=x.split(' ')[1].split('/'); return int(a),int(b)
      a,b1,c=[수(x) for x in cnt]
      봄(t+f"imv8 숨은 것 달성 → 분자 · 분모 +1, 보통 것 → 분자만 +1 ({' → '.join(cnt)})", b1==(a[0]+1,a[1]+1) and c==(a[0]+2,a[1]+1))
      # + → 분류 줄
      await 누름('.업적위 [data-act="업적분류더"]')
      더=await 해("(()=>{ const r=document.querySelector('.업적위 .가로밀기'); return r?{칩:[...r.querySelectorAll('.칩')].map(x=>x.textContent).slice(0,4), 수:r.querySelectorAll('.칩').length}:null; })()")
      봄(t+f"jdyt + 누르면 분류 칩 줄 ({더})", 더 and 더['칩'][0]=='숨은' and 더['칩'][1]=='3대 합계' and 더['수']>10)
      await 사진('업적더')
      await 누름('.업적위 .가로밀기 [data-v="3대 합계"]')
      f3=await 해("(()=>({분류:U.스탯.분류, 줄:document.querySelectorAll('.업적목록 .업적줄').length, 맞:[...document.querySelectorAll('.업적목록 .업적줄')].length===업적표.filter(a=>a.분류==='3대 합계').length, 더켬:document.querySelector('.업적위 [data-act=\"업적분류더\"]').classList.contains('켬')}))()")
      봄(t+f"jdyt 분류 고르면 그 분류만 · + 칩 켜짐 ({f3})", f3['분류']=='3대 합계' and f3['맞'] and f3['더켬'])
      await 누름('.업적위 [data-act="업적분류더"]'); 봄(t+"+ 다시 누르면 분류 줄 접힘", await 해("!document.querySelector('.업적위 .가로밀기')"))
      await 누름('.업적위 [data-v="전체"]'); 봄(t+"전체 → 모든 업적", await 해("document.querySelectorAll('.업적목록 .업적줄').length===업적표.length"))
      n=await 넘침(); 봄(t+f"업적 보기 가로 넘침 없음 {n}", not n)
      # 달성 띠 '보기' → 업적 · 달성
      await 해("행동('탭',{t:'캘린더'}); U.업적띠={목록:[Object.keys(S.업적)[0]],id:999}; 그리기()")
      await 누름('[data-act="업적띠보기"]')
      봄(t+"업적 알림 '보기' → 업적 · 달성", await 해("U.스탯?.보기==='업적' && U.스탯.분류==='달성' && document.querySelector('.업적위 .칩.켬')?.textContent==='달성'"))
      await 누름('.스탯아래 [data-v="스탯"]'); 봄(t+"아래 [스탯] → 스탯 보기", await 해("U.스탯.보기==='스탯' && !!document.querySelector('.스탯속')"))
      봄(t+"JS 오류 없음", not 오류)
      if 오류: print(오류[:3])
      await pg.close()
    await b.close()
  [print(("✅ " if ok else "❌ ")+m) for m,ok in 결과]; print(f"\n{sum(ok for _,ok in 결과)}/{len(결과)}")
asyncio.run(main())
