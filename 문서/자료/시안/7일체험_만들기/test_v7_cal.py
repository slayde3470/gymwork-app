"""10-03 ✎ 표시 5개(캘린더 · 종목 · 플랜 고치기) 시험 (headless Chromium) — python3 test_v7_cal.py [파일]  기본 7day-v7cal.html"""
import asyncio, sys
from playwright.async_api import async_playwright
이름 = sys.argv[1] if len(sys.argv)>1 else '7day-v7cal.html'
F='file:///tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'+이름
색JS = """v=>{const d=document.createElement('i'); d.style.color=getComputedStyle(document.documentElement).getPropertyValue(v); document.body.appendChild(d); const c=getComputedStyle(d).color; d.remove(); return c;}"""
async def main():
  결과=[]
  def 봄(m,ok): 결과.append((m,bool(ok)))
  async with async_playwright() as p:
    b=await p.chromium.launch()
    for 폭,높이 in [(420,1080),(360,800)]:
      pg=await b.new_page(viewport={'width':폭,'height':높이}); 오류=[]; pg.on('pageerror',lambda e:오류.append(str(e)))
      await pg.goto(F); await pg.wait_for_timeout(1500); t=f"[{폭}] "
      await pg.evaluate("(()=>{U.업적띠=null; 그리기();})()"); await pg.wait_for_timeout(400); await pg.evaluate("window.색="+색JS)
      async def 사진(n):
        if 폭==420 or n.startswith('360'): await (await pg.query_selector('#폰')).screenshot(path=f'v7cal_{n}.png')
      async def 넘침():
        return await pg.evaluate("(()=>{const 폰=document.getElementById('폰'); const 넘=[...폰.querySelectorAll('*')].filter(e=>{const r=e.getBoundingClientRect(), q=폰.getBoundingClientRect(); return r.width>0 && (r.right>q.right+1||r.left<q.left-1) && !e.closest('.가로밀기,.운띠,.사진줄');}).map(e=>e.className||e.tagName); return {문서:document.documentElement.scrollWidth>innerWidth, 넘:넘.slice(0,4)};})()")
      # ── pd7g 년월 띠 ──
      띠=await pg.evaluate("""(()=>{const 띠=document.querySelector('.년월띠'), r=띠.getBoundingClientRect(), k=[...띠.children];
        const 제=띠.querySelector('.년월 b').getBoundingClientRect(), 가=(제.left+제.right)/2-(r.left+r.right)/2;
        const 안=k.every(e=>{const q=e.getBoundingClientRect(); return q.left>=r.left+11 && q.right<=r.right-11;});
        return {처음:[k[0].textContent,k[0].dataset.act,k[0].dataset.v], 끝:[k.at(-1).textContent,k.at(-1).dataset.act,k.at(-1).dataset.v], 가운데어긋남:Math.round(가*10)/10,
          칭호:!!띠.querySelector('.칭호칩'), 오늘:!!띠.querySelector('[data-act="오늘달"]'), 안, 화살:[...띠.querySelectorAll('.달넘김')].map(e=>Math.round(e.getBoundingClientRect().height))};})()""")
      봄(t+f"pd7g 왼쪽 끝 [스탯] ({띠['처음']})", 띠['처음']==['스탯','스탯열기','스탯'])
      봄(t+f"pd7g 오른쪽 끝 [업적] ({띠['끝']})", 띠['끝']==['업적','스탯열기','업적'])
      봄(t+f"pd7g 년월이 띠 한가운데 (어긋남 {띠['가운데어긋남']}px)", abs(띠['가운데어긋남'])<=2)
      봄(t+f"pd7g 칭호 칩 없음 · 띠 안에 다 들어감 · ‹ › 누르는 높이 32 ({띠['화살']})", not 띠['칭호'] and 띠['안'] and 띠['화살']==[32,32])
      # ── upy8 구분선 ──
      선=await pg.evaluate("""(()=>{const 선색=색('--선'); const 칸=[...document.querySelectorAll('.달력>.칸날')], 요=document.querySelectorAll('.달력>.요일').length;
        const a=x=>getComputedStyle(x,'::after'); const 열=x=>[...x.parentElement.children].indexOf(x)%7;
        return {격자:(요+칸.length)%7===0, 높이:[...new Set(칸.map(x=>Math.round(x.getBoundingClientRect().height)))],
          위:칸.filter(x=>!x.classList.contains('고름')).every(x=>a(x).borderTopWidth==='1px'&&a(x).borderTopColor===선색),
          왼:칸.filter(x=>!x.classList.contains('고름')&&열(x)!==0).every(x=>a(x).borderLeftWidth==='1px'),
          첫열:칸.filter(x=>열(x)===0).every(x=>a(x).borderLeftStyle==='none'||a(x).borderLeftWidth==='0px'),
          틈:getComputedStyle(document.querySelector('.달력')).columnGap,
          넘침:칸.filter(c=>c.scrollHeight>c.clientHeight+1||c.scrollWidth>c.clientWidth+1).length,
          오늘:getComputedStyle(document.querySelector('.칸날.오늘 .일')).backgroundColor===색('--강조'),
          고름:getComputedStyle(document.querySelector('.칸날.고름')).borderTopColor===색('--강조')};})()""")
      봄(t+f"upy8 칸 위 · 왼쪽 1px --선 (첫 열 왼쪽 없음 · 틈 {선['틈']})", 선['위'] and 선['왼'] and 선['첫열'] and 선['틈'] in ('0px','normal'))
      봄(t+f"upy8 끝 주까지 반듯한 격자 · 칸 높이 {선['높이']}", 선['격자'] and 선['높이']==[64])
      봄(t+f"upy8 칸 글씨 안 넘침 ({선['넘침']}칸) · 오늘 · 고른 날 표시 남음", 선['넘침']==0 and 선['오늘'] and 선['고름'])
      # ── c77y 날짜 판 띠 ──
      판=await pg.evaluate("""(()=>{const 판=document.querySelector('.판'), 띠=판.firstElementChild, cs=getComputedStyle(띠), b=띠.querySelector('b');
        const 단=[...띠.querySelectorAll('button')].map(x=>[x.textContent,x.dataset.act,x.className]);
        const 머=판.querySelector('.예머리'), pr=판.getBoundingClientRect(), dr=띠.getBoundingClientRect();
        return {띠:띠.classList.contains('띠'), 바탕:cs.backgroundColor===색('--강조'), 글:cs.color===색('--강조글'), 높이:Math.round(dr.height), 글자:getComputedStyle(b).fontSize, 굵기:getComputedStyle(b).fontWeight,
          꽉:Math.abs(dr.width-pr.width)<1, 단, 끝:띠.lastElementChild.dataset.act, 머단추:머?머.querySelectorAll('button').length:-1, 칩:[...판.querySelectorAll('.예머리 .예칩 span')].length};})()""")
      봄(t+f"c77y 날짜 판 제목 = 띠 (강조 바탕 · 강조글 · 높이 {판['높이']} · {판['글자']} {판['굵기']}) · 판 폭 꽉", 판['띠'] and 판['바탕'] and 판['글'] and 판['높이']>=40 and 판['글자']=='18px' and int(판['굵기'])>=700 and 판['꽉'])
      봄(t+f"c77y 띠 맨 오른쪽 [변경] (.작은흰) ({판['단']})", 판['끝']=='변경' and len(판['단'])==1 and '작은흰' in 판['단'][0][2])
      봄(t+f"c77y 루틴 이름 줄 = 이름 + 칩 3개 · 단추 없음 (단추 {판['머단추']} · 칩 {판['칩']})", 판['머단추']==0 and 판['칩']==3)
      아래=await pg.evaluate("""(()=>{const b=document.querySelector('.판 [data-act="시작"]'), 넘=document.querySelector('#폰 .넘김').getBoundingClientRect(), r=b.getBoundingClientRect();
        return {혼자:b.parentElement.children.length===1, 아래틈:Math.round(넘.bottom-r.bottom)};})()""")
      봄(t+f"운동 시작은 그대로 맨 아래 ({아래})", 아래['혼자'] and 0<=아래['아래틈']<=12)
      n=await 넘침(); 봄(t+f"캘린더 가로 넘침 없음 ({n})", not n['문서'] and not n['넘'])
      await 사진('캘린더')
      # 예정 없는 날 → [루틴 넣기] · 기록만 있는 지난 날 → 비움
      빈=await pg.evaluate("""(()=>{let k=오늘(); for(let i=1;i<60;i++){ const x=날더하기(오늘(),i); if(!S.예정[x]&&!기록있음(x)){k=x;break;} }
        U.고른날=k; 그리기(); const 띠=document.querySelector('.판 .판띠'); return {k, 단:[...띠.querySelectorAll('button')].map(x=>x.textContent+':'+x.dataset.act+':'+(x.dataset.t||'')), 머:document.querySelector('.판 .예머리')?.innerText};})()""")
      봄(t+f"c77y 예정 없는 날({빈['k']}) 띠 오른쪽 [루틴 넣기] ({빈['단']} · 이름 줄 '{빈['머']}')", 빈['단']==['루틴 넣기:시트:루틴고르기'] and 빈['머']=='예정 없음')
      await pg.click('.판 .판띠 [data-act="시트"]'); await pg.wait_for_timeout(450)
      봄(t+"[루틴 넣기] → 루틴 고르기 시트", await pg.evaluate("U.시트?.종류")=='루틴고르기')
      await pg.evaluate("(()=>{U.시트=null; 그리기();})()")
      지난=await pg.evaluate("""(()=>{const k=Object.keys(S.기록).map(x=>x.split('~')[0]).filter(x=>x<오늘()).sort()[0]; U.고른날=k; 그리기();
        return {k, 단:[...document.querySelectorAll('.판 .판띠 button')].map(x=>x.textContent)};})()""")
      봄(t+f"c77y 기록만 있는 지난 날({지난['k']}) 띠 오른쪽 비움 ({지난['단']})", 지난['k'] and 지난['단']==[])
      await pg.evaluate("(()=>{U.고른날=null; 그리기();})()"); await pg.wait_for_timeout(200)
      # 다른 달 — ‹ › 자리 그대로 · [오늘] 은 날짜 판 띠에
      자리0=await pg.evaluate("[...document.querySelectorAll('.년월띠 .달넘김')].map(e=>Math.round(e.getBoundingClientRect().left))")
      await pg.click('.년월띠 [data-d="1"]'); await pg.wait_for_timeout(450)
      자리1=await pg.evaluate("[...document.querySelectorAll('.년월띠 .달넘김')].map(e=>Math.round(e.getBoundingClientRect().left))")
      오=await pg.evaluate("({띠:!!document.querySelector('.년월띠 [data-act=\"오늘달\"]'), 판:[...document.querySelectorAll('.판 .판띠 button')].map(x=>x.textContent), 달:U.보는달})")
      봄(t+f"pd7g 다음 달({오['달']}): ‹ › 자리 그대로 ({자리0} → {자리1}, 3px 안쪽)", all(abs(a-z)<=3 for a,z in zip(자리0,자리1)))
      봄(t+f"pd7g [오늘] 은 날짜 판 띠에 ({오['판']})", not 오['띠'] and 오['판'][:1]==['오늘'])
      n=await 넘침(); 봄(t+f"다른 달 가로 넘침 없음 ({n})", not n['문서'] and not n['넘'])
      await 사진('다른달')
      await pg.click('.판 .판띠 [data-act="오늘달"]'); await pg.wait_for_timeout(450)
      봄(t+"[오늘] → 이번 달로", await pg.evaluate("U.보는달===null && !document.querySelector('.판 .판띠 [data-act=\"오늘달\"]')"))
      # 업적 · 스탯 단추가 행동을 부른다 (data-v 읽기는 다른 사람 몫 — 여기서는 스탯 화면이 열리는 것만)
      await pg.click('.년월띠 [data-v="업적"]'); await pg.wait_for_timeout(300)
      봄(t+f"[업적] 누르면 스탯 · 업적 화면 열림 (보기 {await pg.evaluate('U.스탯?.보기')})", await pg.evaluate("!!U.스탯"))
      await pg.evaluate("(()=>{U.스탯=null; 그리기();})()"); await pg.wait_for_timeout(200)
      # ── qk0z 종목 탭 ──
      await pg.evaluate("(()=>{U.탭='종목'; 그리기();})()"); await pg.wait_for_timeout(400)
      종=await pg.evaluate("""(()=>{const 넘=document.querySelector('#폰 .넘김'), h=넘.querySelector('h1'), 표=[...넘.querySelectorAll('.이름표')];
        const 넣=넘.querySelector('[data-act="종목만들기"]').getBoundingClientRect(), 플=넘.querySelector('.플랜카드')?.getBoundingClientRect(), q=넘.getBoundingClientRect();
        return {다음:h.nextElementSibling.textContent, 첫표:표[0].textContent, 플랜보다위:!플||넣.bottom<플.top, 보임:넣.bottom<=q.bottom, 끝표:표.at(-1).textContent};})()""")
      봄(t+f"qk0z 제목 바로 아래 '종목 추가' ({종['다음']}) · 플랜 카드보다 위 · 스크롤 없이 보임", 종['다음']=='종목 추가' and 종['플랜보다위'] and 종['보임'])
      봄(t+f"qk0z 맨 아래에는 없음 (마지막 이름표 '{종['끝표']}')", 종['끝표']!='종목 추가')
      await pg.fill('[data-in="새종목"]','풀업 테스트'); await pg.click('[data-act="종목만들기"]'); await pg.wait_for_timeout(300)
      봄(t+"종목 추가 넣기 동작", await pg.evaluate("S.종목표.some(x=>x.이름==='풀업 테스트')"))
      await 사진('종목')
      # ── bdtv 플랜 고치기 ──
      await pg.click('[data-act="플랜고치기"][data-v="p1"]'); await pg.wait_for_timeout(500)
      async def 고침상태():
        return await pg.evaluate("""(()=>{const s=document.querySelector('.시트'), 첫=s.querySelector('.고침첫줄'), q=f=>s.querySelector(`[data-f="${f}"]`)?.getBoundingClientRect();
          const 이=q('이름'), 목=q('목표무게')||q('목표개수'), 횟=q('목표횟수'), 측=['측정무게','측정횟수'].map(q).filter(Boolean), 넣=s.querySelector('[data-act="측정넣기"]').getBoundingClientRect();
          const 같=(a,z)=>a&&z&&Math.abs(a.top-z.top)<2;
          const 넘=[...s.querySelectorAll('*')].filter(e=>{const r=e.getBoundingClientRect(), p=s.getBoundingClientRect(); return r.width>0&&(r.right>p.right-13||r.left<p.left+13)&&!e.closest('.머리');}).map(e=>e.className||e.tagName).slice(0,4);
          const 이름칸=첫?.children[0]?.getBoundingClientRect(), 목칸=첫?.children[1]?.getBoundingClientRect();
          return {첫줄:!!첫, 이름왼목표오른:!!(이름칸&&목칸&&이름칸.right<=목칸.left&&Math.abs(이름칸.bottom-목칸.bottom)<2), 한줄:같(이,목)&&(!횟||같(이,횟)),
            칩:[...s.querySelectorAll('.고침첫줄 [data-act="고침목표방식"]')].map(x=>x.textContent+(x.classList.contains('켬')?'*':'')),
            측정한줄:측.every(r=>같(r,넣)), 측정수:측.length, 자리글:[...s.querySelectorAll('[data-f^="측정"]')].map(x=>x.placeholder),
            글:s.innerText, 회색:!!s.querySelector('.회색칸'), 넘, 스크롤:s.scrollHeight>s.clientHeight+1,
            이름표:s.querySelector('.고침이름표')?.innerText.replace(/\\n/g,' | '), 이름표글자:getComputedStyle(s.querySelector('.고침이름표')).fontSize};})()""")
      g=await 고침상태()
      봄(t+f"bdtv 첫 줄: 이름(왼쪽) | 목표(오른쪽) · 입력 칸 같은 높이 ({g['칩']})", g['첫줄'] and g['이름왼목표오른'] and g['한줄'] and g['칩']==['1RM*','무게 × 횟수'])
      봄(t+f"bdtv '현재 수행능력' 한 줄 이름표 11 ({g['이름표']} · {g['이름표글자']})", g['이름표'] and g['이름표'].startswith('현재 수행능력') and '지금 1RM' in g['이름표'] and g['이름표글자']=='11px')
      봄(t+f"bdtv 회색 큰 상자 없음 · '지금 실력' · '회차' 글 없음", not g['회색'] and '지금 실력' not in g['글'] and '회차' not in g['글'])
      봄(t+f"bdtv 측정 [무게][횟수][넣기] 한 줄 ({g['자리글']})", g['측정한줄'] and g['측정수']==2 and g['자리글']==['무게','횟수'])
      봄(t+f"bdtv 시트 안 넘침 · 스크롤 없음 ({g['넘']})", not g['넘'] and not g['스크롤'])
      await 사진('고침_1RM')
      await pg.click('.시트 [data-act="고침목표방식"][data-v="회"]'); await pg.wait_for_timeout(400)
      g=await 고침상태()
      봄(t+f"bdtv 무게 × 횟수: 무게 · 횟수 두 칸도 이름과 한 줄 ({g['칩']}) · 안 넘침 ({g['넘']})", g['한줄'] and g['칩']==['1RM','무게 × 횟수*'] and not g['넘'])
      v=await pg.evaluate("({w:U.고침.목표무게, r:U.고침.목표횟수})"); 봄(t+f"고침목표방식 환산 그대로 ({v})", v['r']==10 and abs(v['w']-75)<=2.5)
      await 사진('고침_회');
      if 폭==360: await 사진('360_고침_회')
      await pg.fill('.시트 [data-f="측정무게"]','90'); await pg.fill('.시트 [data-f="측정횟수"]','3')
      전=await pg.evaluate("S.플랜들[0].측정들.length"); await pg.click('.시트 [data-act="측정넣기"]'); await pg.wait_for_timeout(300)
      후=await pg.evaluate("({n:S.플랜들[0].측정들.length, 발:S.발자취.at(-1)})")
      봄(t+f"측정 넣기 동작 ({전} → {후['n']} · 발자취 '{후['발']}')", 후['n']>=전 and await pg.evaluate("S.플랜들[0].측정들.some(m=>m.무게===90&&m.횟수===3)"))
      await pg.click('.시트 [data-act="고침저장"]'); await pg.wait_for_timeout(400)
      봄(t+"저장 → 시트 닫힘", await pg.evaluate("!U.시트"))
      # 맨몸(턱걸이 p2)
      await pg.click('[data-act="플랜고치기"][data-v="p2"]'); await pg.wait_for_timeout(500)
      g=await 고침상태(); 보=await pg.evaluate("[...document.querySelectorAll('.시트 [data-f=\"보조모드\"]')].map(x=>x.textContent)")
      봄(t+f"bdtv 맨몸(턱걸이): 이름 | 목표 개수 한 줄 · 보조 칩 {보} · '{g['이름표']}' · 자리글 {g['자리글']}", g['첫줄'] and g['한줄'] and g['이름왼목표오른'] and len(보)==3 and '지금 최대' in (g['이름표'] or '') and g['자리글']==['정자세 개수'] and g['측정한줄'] and not g['넘'])
      await pg.click('.시트 [data-f="보조모드"][data-v="1"]'); await pg.wait_for_timeout(300)
      g=await 고침상태(); 봄(t+f"맨몸 어시스트 고르면 보조 무게 칸 · 안 넘침 ({g['넘']})", await pg.evaluate("!!document.querySelector('.시트 [data-f=\"보조무게\"]')") and not g['넘'])
      await 사진('고침_맨몸')
      await pg.click('.시트 [data-act="시트닫기"].버튼'); await pg.wait_for_timeout(300)
      봄(t+"JS 오류 없음", not 오류)
      if 오류: print(오류[:3])
      await pg.close()
    await b.close()
  [print(("✅ " if ok else "❌ ")+m) for m,ok in 결과]; print(f"\n{sum(ok for _,ok in 결과)}/{len(결과)}")
asyncio.run(main())
