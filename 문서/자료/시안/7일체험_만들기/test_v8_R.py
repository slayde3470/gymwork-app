"""10-03 ✎ 표시 10개(루틴 상세 · 종목 넣기 시트 · ＋/− 움직임) 시험 (headless Chromium) — python3 test_v8_R.py [파일]  기본 7day-v8R.html"""
import asyncio, sys
from playwright.async_api import async_playwright
이름 = sys.argv[1] if len(sys.argv)>1 else '7day-v8R.html'
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
      await pg.evaluate("(()=>{U.업적띠=null; U.탭='루틴'; U.루틴열림='r1'; 그리기();})()"); await pg.wait_for_timeout(400); await pg.evaluate("window.색="+색JS)
      async def 사진(n): await (await pg.query_selector('#폰')).screenshot(path=f'v8R_{폭}_{n}.png')
      async def 넘침():
        return await pg.evaluate("(()=>{const 폰=document.getElementById('폰'); const q=폰.getBoundingClientRect(); const 넘=[...폰.querySelectorAll('*')].filter(e=>{const r=e.getBoundingClientRect(); return r.width>0 && (r.right>q.right+1||r.left<q.left-1);}).map(e=>e.className||e.tagName); const 넘김=폰.querySelector('.넘김'); return {문서:document.documentElement.scrollWidth>innerWidth, 넘:넘.slice(0,4), 가로:넘김?넘김.scrollWidth>넘김.clientWidth+1:false};})()")
      # ── 401d 맨 위 띠 ──
      띠=await pg.evaluate("""(()=>{const 화=document.querySelector('#폰 .화면'), 띠=화.querySelector('.루띠'), 넘=화.querySelector('.넘김'), cs=getComputedStyle(띠), r=띠.getBoundingClientRect();
        const 뒤=띠.querySelector('[data-act="루틴닫기"]'), 칸=띠.querySelector('.루이름칸'), 입=칸.querySelector('input'), 예=칸.querySelector('.루예상'), 스=띠.querySelector('.스위치'), 자=띠.querySelector('.루자동');
        const 안=(a,z)=>{const x=a.getBoundingClientRect(), y=z.getBoundingClientRect(); return x.left>=y.left-0.5&&x.right<=y.right+0.5&&x.top>=y.top-0.5&&x.bottom<=y.bottom+0.5;};
        return {밖:띠.compareDocumentPosition(넘)&4 && !넘.contains(띠), 띠:띠.classList.contains('띠'), 바탕:cs.backgroundColor===색('--강조'), 글:cs.color===색('--강조글'), 높이:Math.round(r.height),
          순서:[뒤,칸,자].map(e=>Math.round(e.getBoundingClientRect().left)), 뒤글:뒤.textContent, 작은흰:뒤.classList.contains('작은흰'),
          예글:예?.textContent, 예안:예&&안(예,칸), 예오른:예&&예.getBoundingClientRect().left>입.getBoundingClientRect().left, 예크기:예&&getComputedStyle(예).fontSize, 예색:예&&getComputedStyle(예).color===색('--옅음'),
          칸바탕:getComputedStyle(칸).backgroundColor===색('--면'), 칸높이:Math.round(칸.getBoundingClientRect().height), 입글자:getComputedStyle(입).fontSize, 입값:입.value, 입폭:Math.round(입.getBoundingClientRect().width),
          자글:자.querySelector('span').textContent, 자크기:getComputedStyle(자.querySelector('span')).fontSize, 스안:안(스,띠),
          옛글:document.querySelector('#폰').innerText.includes('자동으로 돌리기')||document.querySelector('#폰').innerText.includes('순서대로 깔립니다')};})()""")
      봄(t+f"401d 맨 위 띠 = .띠 (강조 바탕 · 강조글 · 높이 {띠['높이']}) · 스크롤 칸 밖", 띠['밖'] and 띠['띠'] and 띠['바탕'] and 띠['글'] and 띠['높이']>=40)
      봄(t+f"401d 순서 [‹ 루틴][이름 칸][자동] ({띠['순서']}) · ‹ 루틴 = .작은흰", 띠['순서']==sorted(띠['순서']) and 띠['뒤글']=='‹ 루틴' and 띠['작은흰'])
      봄(t+f"401d 이름 칸 안쪽 오른쪽에 흐린 '{띠['예글']}' ({띠['예크기']} · --옅음)", 띠['예글'] and 띠['예글'].startswith('예상 ') and 띠['예글'].endswith('세트') and 띠['예안'] and 띠['예오른'] and 띠['예크기']=='11px' and 띠['예색'])
      봄(t+f"401d 이름 칸 흰 바탕 · 높이 {띠['칸높이']} · 글자 {띠['입글자']} · 이름 '{띠['입값']}' 폭 {띠['입폭']}", 띠['칸바탕'] and 띠['칸높이']==32 and 띠['입글자']=='15px' and 띠['입값']=='가슴·어깨' and 띠['입폭']>=70)
      봄(t+f"401d 스위치에 '자동' 이름표 ({띠['자크기']}) · 띠 안", 띠['자글']=='자동' and 띠['자크기']=='11px' and 띠['스안'])
      봄(t+"401d '자동으로 돌리기 / 캘린더에 순서대로 깔립니다' 줄 없음", not 띠['옛글'])
      # 자동 이름표를 눌러도 스위치가 한 번만 바뀐다 · 이름 고치기
      전=await pg.evaluate("루틴('r1').자동생성"); await pg.click('.루자동 span'); await pg.wait_for_timeout(250); 후=await pg.evaluate("루틴('r1').자동생성")
      봄(t+f"401d '자동' 글자 눌러도 스위치 ({전} → {후})", 전!=후)
      await pg.click('.루띠 .스위치'); await pg.wait_for_timeout(250); 봄(t+"스위치 직접 눌러 되돌림", await pg.evaluate("루틴('r1').자동생성")==전)
      await pg.click('.루예상'); 봄(t+"예상 글을 눌러도 이름 칸에 커서", await pg.evaluate("document.activeElement?.dataset?.in==='루틴이름'"))
      await pg.fill('.루이름칸 input','가슴·어깨 A'); await pg.keyboard.press('Tab'); await pg.wait_for_timeout(200)
      봄(t+"이름 고치기 동작", await pg.evaluate("루틴('r1').이름==='가슴·어깨 A'"))
      await pg.evaluate("(()=>{루틴('r1').이름='가슴·어깨'; 그리기();})()")
      # ── a8es 띠 바로 아래 파란 [+ 종목 추가] ──
      위=await pg.evaluate("""(()=>{const 넘=document.querySelector('#폰 .루넘김'), b=넘.firstElementChild, 띠=document.querySelector('.루띠').getBoundingClientRect(), r=b.getBoundingClientRect(), cs=getComputedStyle(b);
        return {글:b.textContent, 주:b.classList.contains('주'), 바탕:cs.backgroundColor===색('--강조'), 글색:cs.color===색('--강조글'), 틈:Math.round(r.top-띠.bottom), 높이:Math.round(r.height), 넓:Math.abs(r.width-넘.clientWidth+24)<2};})()""")
      봄(t+f"a8es 띠 바로 아래(틈 {위['틈']}) 파란 '{위['글']}' 넓은 단추 · 높이 {위['높이']}", 위['글']=='+ 종목 추가' and 위['주'] and 위['바탕'] and 위['글색'] and 0<=위['틈']<=12 and 위['높이']==40 and 위['넓'])
      await pg.click('.루추가.위'); await pg.wait_for_timeout(450); 봄(t+"a8es [+ 종목 추가] → 종목 넣기 시트", await pg.evaluate("U.시트?.종류")=='종목넣기')
      await pg.evaluate("(()=>{U.시트=null; 그리기();})()"); await pg.wait_for_timeout(200)
      # ── c1gz 플랜 상자 한 줄 · [플랜] 겹침 · 눌러 고치기 ──
      플=await pg.evaluate("""(()=>{const 상=document.querySelector('.종목상자.루플랜'), 머=상.querySelector('.루머리'), 이=머.querySelector('.이름플랜 b'), 표=머.querySelector('.플랜표'), 글=머.querySelector('.루플랜글');
        const a=이.getBoundingClientRect(), z=표.getBoundingClientRect(), g=글.getBoundingClientRect(), cs=getComputedStyle(표), 상r=상.getBoundingClientRect();
        return {줄수:상.children.length, 상높이:Math.round(상r.height), 겹가로:Math.round((a.right-z.left)*10)/10, 위:Math.round((a.top-z.top)*10)/10, 아래:Math.round((z.bottom-a.top)*10)/10,
          바탕:cs.backgroundColor===색('--강조'), 글색:cs.color===색('--강조글'), 크기:cs.fontSize, 굵기:cs.fontWeight, 모서리:cs.borderRadius, 한줄:Math.abs((g.top+g.bottom)/2-(a.top+a.bottom)/2)<3 && g.left>z.right-1,
          글:글.textContent, 옛알약:!!상.querySelector('.알약'), act:머.querySelector('[data-act="플랜고치기"]')?.dataset.v, 옛줄:상.querySelectorAll('.맞춤').length};})()""")
      봄(t+f"c1gz 플랜 상자 한 줄 (높이 {플['상높이']}) '{플['글']}'", 플['줄수']==1 and 플['상높이']<=44 and 플['한줄'] and 플['글'].startswith('· ') and '회차' in 플['글'] and 플['옛줄']==0)
      봄(t+f"c1gz [플랜] 이름 오른쪽 위 겹침 (가로 {플['겹가로']}px · 이름 위로 {플['위']} · 이름 윗선 아래로 {플['아래']})", abs(플['겹가로']-5)<=1 and 플['위']>0 and 플['아래']>0 and not 플['옛알약'])
      봄(t+f"c1gz [플랜] 강조 바탕 · 강조글 · {플['크기']} {플['굵기']} · 모서리 {플['모서리']}", 플['바탕'] and 플['글색'] and 플['크기']=='11px' and int(플['굵기'])>=700 and 플['모서리']=='8px')
      await pg.click('.루플랜줄'); await pg.wait_for_timeout(450)
      고=await pg.evaluate("({종류:U.시트?.종류, id:U.고침?.id, 저장:!!document.querySelector('.시트 [data-act=\"고침저장\"]')})")
      봄(t+f"c1gz 플랜 줄 누르면 '플랜 고치기' 시트 ({고})", 고['종류']=='고침' and 고['id']=='p1' and 고['저장'])
      await pg.evaluate("(()=>{U.시트=null; 그리기();})()"); await pg.wait_for_timeout(200)
      # ── izyq 종목 상자 머리 ──
      머=await pg.evaluate("""(()=>{const l=[...document.querySelectorAll('.루넘김 .종목상자')]; return l.map(상=>{const 머=상.querySelector('.루머리'), b=머.querySelector('b'), r=머.getBoundingClientRect(), q=상.getBoundingClientRect();
        return {바탕:getComputedStyle(머).backgroundColor===색('--강조옅음'), 크기:getComputedStyle(b).fontSize, 굵기:getComputedStyle(b).fontWeight, 꽉:Math.abs(r.width-(q.width-2))<1.5 && Math.abs(r.top-(q.top+1))<1.5, 높이:Math.round(r.height)};});})()""")
      봄(t+f"izyq 상자 {len(머)}개 모두 이름 줄 = 머리 (--강조옅음 · 15 Bold · 상자 폭 꽉 · 높이 {[m['높이'] for m in 머]})", 머 and all(m['바탕'] and m['크기']=='15px' and int(m['굵기'])>=700 and m['꽉'] and m['높이']>=40 for m in 머))
      # ── h627 · 9kht 세트 줄 ──
      세=await pg.evaluate("""(()=>{const 상=document.querySelectorAll('.루넘김 .종목상자')[1], 줄=[...상.querySelectorAll('.루세트')];
        const r=줄.map(x=>x.getBoundingClientRect()), 휴=줄.map(x=>x.querySelectorAll('.값칸')[2].getBoundingClientRect()), 지=줄.map(x=>x.querySelector('.루지움'));
        const 잘림=[...document.querySelectorAll('.루세트 .값칸')].filter(c=>{const v=c.querySelector('input,span'); return v.scrollWidth>v.clientWidth+1;}).length;
        const 머=상.querySelector('.세트머리'), 머칸=[...머.children].map(x=>Math.round(x.getBoundingClientRect().height));
        return {n:줄.length, 높이:[...new Set(r.map(x=>Math.round(x.height)))], 간격:[...new Set(r.slice(1).map((x,i)=>Math.round(x.top-r[i].top)))], 지움:지.every((b,i)=>b&&b.getBoundingClientRect().left>=휴[i].right-0.5),
          지크기:지.map(b=>Math.round(b.getBoundingClientRect().width)+'x'+Math.round(b.getBoundingClientRect().height))[0], 아이콘:지[0].querySelector('svg')?.getAttribute('stroke-width'), 아폭:Math.round(지[0].querySelector('svg').getBoundingClientRect().width),
          빼기세트:[...document.querySelectorAll('.루넘김 button')].filter(b=>b.textContent.includes('− 세트')).length, 잘림, 머칸, 더세트:상.querySelector('[data-act="루세트"][data-d="1"]')?.textContent};})()""")
      봄(t+f"9kht 세트 줄 높이 {세['높이']} · 간격 {세['간격']} (전 40 · 44 → 약 70%)", 세['높이']==[28] and 세['간격']==[32])
      봄(t+f"h627 세트 {세['n']}줄 모두 휴식 칸 오른쪽에 휴지통 ({세['지크기']} · 아이콘 {세['아폭']}px 선 {세['아이콘']})", 세['지움'] and 세['지크기']=='28x28' and 세['아이콘']=='2' and 세['아폭']==16)
      봄(t+f"h627 '− 세트' 단추 없음 · '+ 세트' 남음 · 세트머리 한 줄 {세['머칸']}", 세['빼기세트']==0 and 세['더세트']=='+ 세트' and max(세['머칸'])<=16)
      봄(t+f"세트 칸 숫자 안 잘림 ({세['잘림']}칸)", 세['잘림']==0)
      await 사진('상세')
      # 지우기 → 되돌리기
      전=await pg.evaluate("JSON.stringify(루틴('r1').종목[1].세트)")
      await pg.evaluate("(()=>{const s=루틴('r1').종목[1].세트; s[1].w=55; s[2].w=60; 그리기();})()"); 전=await pg.evaluate("JSON.stringify(루틴('r1').종목[1].세트)")
      await pg.click('.루넘김 .종목상자:nth-of-type(2) .루세트:nth-of-type(3) .루지움') if False else await pg.click('[data-act="루세트지움"][data-i="1"][data-k="1"]')
      await pg.wait_for_timeout(300)
      지=await pg.evaluate("({n:루틴('r1').종목[1].세트.length, w:루틴('r1').종목[1].세트.map(s=>s.w), 띠:document.querySelector('.루지움띠')?.innerText, 띠아래:(()=>{const a=document.querySelector('.루지움띠')?.getBoundingClientRect(), t=document.querySelector('.탭줄').getBoundingClientRect(); return a?Math.round(t.top-a.bottom):null;})()})")
      봄(t+f"h627 2세트 휴지통 → 묻지 않고 지움 ({지['w']}) · 아래띠 '{지['띠']}'", 지['n']==2 and 지['w']==[50,60] and 지['띠'] and '되돌리기' in 지['띠'] and 지['띠아래'] is not None and 지['띠아래']>=0)
      await 사진('지움띠')
      await pg.click('.루지움띠 [data-act="루세트되돌림"]'); await pg.wait_for_timeout(300)
      봄(t+"h627 [되돌리기] → 같은 자리 같은 값으로", await pg.evaluate("JSON.stringify(루틴('r1').종목[1].세트)")==전 and await pg.evaluate("!document.querySelector('.루지움띠') && !U.세트지움"))
      await pg.click('[data-act="루세트지움"][data-i="1"][data-k="0"]'); await pg.wait_for_timeout(200)
      await pg.evaluate("U.세트지움.id=-1"); 남=await pg.evaluate("!!U.세트지움")   # 6초 타이머가 이 띠를 지우는지 (id 를 바꿔 다른 띠로 보이게 하면 남아야 한다)
      await pg.evaluate("(()=>{U.세트지움=null; 그리기();})()")
      # 세트 하나뿐 → 휴지통 꺼짐
      await pg.evaluate("(()=>{const r=루틴('r1'); r.종목.push({이름:'바벨 컬', 세트:세트들(1,20,10,60)}); 그리기();})()"); await pg.wait_for_timeout(200)
      하=await pg.evaluate("(()=>{const i=루틴('r1').종목.length-1, b=document.querySelector(`[data-act=\"루세트지움\"][data-i=\"${i}\"]`); return {꺼짐:b.disabled, 흐림:+getComputedStyle(b).opacity<0.5, i};})()")
      await pg.evaluate(f"document.querySelector('[data-act=\"루세트지움\"][data-i=\"{하['i']}\"]').click()"); await pg.wait_for_timeout(200)
      봄(t+f"h627 세트 하나뿐이면 휴지통 꺼짐 · 눌러도 그대로", 하['꺼짐'] and 하['흐림'] and await pg.evaluate(f"루틴('r1').종목[{하['i']}].세트.length")==1)
      # ── mqjf 끝 [+ 종목 추가] ──
      async def 끝상태():
        return await pg.evaluate("""(()=>{const 넘=document.querySelector('#폰 .루넘김'), 끝=넘.querySelector('.루추가.끝'), 상=[...넘.querySelectorAll('.종목상자')].at(-1);
          const 보=끝&&getComputedStyle(끝).display!=='none'; const 넘침=넘.scrollHeight>넘.clientHeight+1;
          return {보, 넘침, 아래:보&&상?Math.round(끝.getBoundingClientRect().top-상.getBoundingClientRect().bottom):null, 주:끝?.classList.contains('주'), 다음:끝?.nextElementSibling?.textContent};})()""")
      for _ in range(3): await pg.evaluate("(()=>{루틴('r1').종목.push({이름:'덤벨 플라이', 세트:세트들(3,20,10,60)}); 그리기();})()")
      await pg.wait_for_timeout(200); 끝=await 끝상태()
      봄(t+f"mqjf 종목이 많아 넘치면 마지막 상자 바로 아래(틈 {끝['아래']}) 파란 [+ 종목 추가] · 그 아래 '이 루틴 지우기'", 끝['넘침'] and 끝['보'] and 끝['주'] and 끝['아래'] is not None and 0<=끝['아래']<=12 and 끝['다음'] and '지우기' in 끝['다음'])
      await pg.evaluate("(()=>{const 넘=document.querySelector('#폰 .루넘김'); 넘.scrollTop=넘.scrollHeight;})()"); await pg.wait_for_timeout(200)
      봄(t+"mqjf 맨 아래에서 위 단추는 안 보이고 끝 단추는 보임", await pg.evaluate("(()=>{const 넘=document.querySelector('#폰 .루넘김').getBoundingClientRect(), a=document.querySelector('.루추가.위').getBoundingClientRect(), z=document.querySelector('.루추가.끝').getBoundingClientRect(); return a.bottom<=넘.top && z.top>=넘.top && z.bottom<=넘.bottom;})()"))
      await 사진('끝단추')
      await pg.click('.루추가.끝'); await pg.wait_for_timeout(450); 봄(t+"mqjf 끝 단추 → 종목 넣기 시트", await pg.evaluate("U.시트?.종류")=='종목넣기')
      await pg.evaluate("(()=>{U.시트=null; 그리기();})()")
      n=await 넘침(); 봄(t+f"루틴 상세 가로 넘침 없음 ({n})", not n['문서'] and not n['넘'] and not n['가로'])
      await pg.evaluate("(()=>{U.루틴열림=null; 행동('루틴추가',{}); const r=루틴(U.루틴열림); r.종목.push({이름:'바벨 컬', 세트:세트들(2,20,10,60)}); 그리기();})()"); await pg.wait_for_timeout(200)
      끝=await 끝상태(); 봄(t+f"mqjf 짧은 루틴(1종목)은 끝 단추 숨김 (넘침 {끝['넘침']})", not 끝['보'])
      await 사진('짧은루틴')
      await pg.click('.루띠 [data-act="루틴닫기"]'); await pg.wait_for_timeout(200); 봄(t+"‹ 루틴 → 루틴 목록", await pg.evaluate("!U.루틴열림 && !document.querySelector('.루띠')"))
      # ── jmg1 · w0fo 종목 넣기 시트 ──
      await pg.evaluate("(()=>{S.루틴들=S.루틴들.filter(r=>r.id==='r1'||r.id==='r2'||r.id==='r3'||!r.이름.startsWith('새 루틴')); const r=루틴('r1'); r.종목=r.종목.slice(0,4); U.루틴열림='r1'; U.시트={종류:'종목넣기'}; U.방금=[]; 그리기();})()"); await pg.wait_for_timeout(500)
      시=await pg.evaluate("""(()=>{const s=document.querySelector('.시트'), 칩=[...s.querySelectorAll('.넣기칩 .칩')], 줄=[...s.querySelectorAll('.넣기줄')];
        const 이름=줄.map(x=>x.querySelector('b').textContent), 정렬=[...이름].sort((a,b)=>a.localeCompare(b,'ko'));
        const rr=줄.map(x=>x.getBoundingClientRect()), 플=줄.filter(x=>x.querySelector('.플랜표'));
        const 겹=플.map(x=>{const a=x.querySelector('b').getBoundingClientRect(), z=x.querySelector('.플랜표').getBoundingClientRect(); return [Math.round((a.right-z.left)*10)/10, z.top<a.top, z.top>=x.getBoundingClientRect().top-0.5];});
        return {칩:칩.map(c=>c.textContent+(c.classList.contains('켬')?'*':'')), 칩한줄:new Set(칩.map(c=>Math.round(c.getBoundingClientRect().top))).size===1, 이름, 정렬:JSON.stringify(이름)===JSON.stringify(정렬),
          간격:[...new Set(rr.slice(1).map((x,i)=>Math.round(x.top-rr[i].top)))], 높이:Math.round(rr[0].height), 운동플랜:s.innerText.includes('운동 플랜'),
          플:플.map(x=>[x.querySelector('b').textContent, x.dataset.act, x.dataset.v]), 겹,
          곁:줄.map(x=>[x.querySelector('b').textContent, x.querySelector('.곁').textContent]).filter(([,g])=>g).slice(0,3), 곁크기:getComputedStyle(줄[0].querySelector('.곁')).fontSize, 곁색:getComputedStyle(줄[0].querySelector('.곁')).color===색('--옅음'),
          스크롤:s.scrollHeight>s.clientHeight};})()""")
      봄(t+f"w0fo 칩 맨 왼쪽 [전체] · 처음 열면 전체 · 한 줄 ({시['칩']})", 시['칩'][0]=='전체*' and len(시['칩'])==7 and 시['칩한줄'])
      봄(t+f"jmg1 '운동 플랜' 묶음 없음 · 플랜 줄 {시['플']}", not 시['운동플랜'] and [x[0] for x in 시['플']]==['벤치프레스','턱걸이'] and all(x[1]=='플랜넣기' for x in 시['플']))
      봄(t+f"jmg1 [플랜] 이름 오른쪽 위 5px 겹침 · 줄 안 ({시['겹']})", 시['겹'] and all(abs(g[0]-5)<=1 and g[1] and g[2] for g in 시['겹']))
      봄(t+f"w0fo 가나다순 ({len(시['이름'])}줄 · {시['이름'][:4]}…)", 시['정렬'] and len(시['이름'])==15)
      봄(t+f"w0fo 줄 간격 {시['간격']} · 높이 {시['높이']} (전 52.5 → 70% ≈ 37)", 시['간격'] and max(시['간격'])<=37 and min(시['간격'])>=36 and 시['높이']>=32)
      봄(t+f"w0fo 최근 기록 흐리게 ({시['곁크기']} --옅음) {시['곁'][:2]}", 시['곁'] and all(g.startswith('최근 ') and '세트 · 최고 ' in g for _,g in 시['곁']) and 시['곁크기']=='11px' and 시['곁색'])
      n=await 넘침(); 봄(t+f"시트 가로 넘침 없음 ({n})", not n['문서'] and not n['넘'])
      await 사진('시트')
      # 플랜 줄 누르면 플랜 종목으로 · 다시 누르면 뺀다(전과 같음)
      전=await pg.evaluate("루틴('r1').종목.filter(e=>e.플랜id==='p2').length")
      await pg.click('.넣기줄[data-v="p2"]'); await pg.wait_for_timeout(250)
      후=await pg.evaluate("({n:루틴('r1').종목.filter(e=>e.플랜id==='p2').length, 표:document.querySelector('.넣기줄[data-v=\"p2\"]').textContent.trim().slice(-1)})")
      봄(t+f"jmg1 턱걸이[플랜] 누르면 플랜 종목으로 넣음 ({전} → {후['n']} · 표 '{후['표']}')", 후['n']==전+1 and 후['표']=='✓')
      await pg.click('.넣기줄[data-v="p2"]'); await pg.wait_for_timeout(250); 봄(t+"한 번 더 누르면 뺌", await pg.evaluate("루틴('r1').종목.filter(e=>e.플랜id==='p2').length")==전)
      await pg.click('.넣기줄[data-act="종목넣기"][data-v="바벨 컬"]'); await pg.wait_for_timeout(250)
      봄(t+"일반 종목 넣기 그대로", await pg.evaluate("루틴('r1').종목.some(e=>e.이름==='바벨 컬'&&!e.플랜id)"))
      await pg.click('.넣기칩 [data-v="등"]'); await pg.wait_for_timeout(250)
      등=await pg.evaluate("[...document.querySelectorAll('.넣기줄 b')].map(b=>b.textContent)")
      봄(t+f"칸 [등] 고르면 등만 · 가나다순 ({등})", 등==sorted(등, key=lambda x:x) or 등==['랫풀다운','턱걸이','펜들레이 로우'])
      await pg.click('.넣기칩 [data-v="전체"]'); await pg.wait_for_timeout(200)
      # 운동을 하나 저장하면 휴식도 나온다
      await pg.evaluate("(()=>{U.시트=null; 운동시작('r1'); const ss=S.세션; ss.종목.forEach(e=>e.세트.forEach(s=>s.완료=true)); ss.끝화면=true; ss.끝=S.시계+600e3; 운동저장하기(); S.결과=null; U.탭='루틴'; U.루틴열림='r1'; U.시트={종류:'종목넣기'}; U.방금=[]; U.업적띠=null; 그리기();})()"); await pg.wait_for_timeout(400)
      휴=await pg.evaluate("[...document.querySelectorAll('.넣기줄')].map(x=>[x.querySelector('b').textContent, x.querySelector('.곁').textContent]).filter(([n])=>n==='인클라인 벤치프레스'||n==='벤치프레스')")
      봄(t+f"w0fo 저장한 운동 뒤 '휴식' 도 ({휴})", 휴 and all('휴식 ' in g and '1RM ' in g for _,g in 휴))
      await 사진('시트_기록')
      await pg.evaluate("(()=>{U.시트=null; 그리기();})()"); await pg.wait_for_timeout(200)
      # ── ls4v ＋ 볼록 · − 오목 ──
      async def 눌러봄(sel, 찾기=None):
        await pg.click(sel); await pg.wait_for_timeout(40)
        return await pg.evaluate(f"(()=>{{const l=[...document.querySelectorAll('.볼록,.오목')]; return l.map(e=>[e.className.includes('볼록')?'볼록':'오목', e.textContent.trim().slice(0,6), getComputedStyle(e).animationName, getComputedStyle(e).getPropertyValue('--눌림')]);}})()")
      a=await 눌러봄('[data-act="루값"][data-f="r"][data-i="1"][data-k="0"][data-d="1"]')
      봄(t+f"ls4v 루틴 '＋' → 볼록 ({a})", len(a)==1 and a[0][0]=='볼록' and a[0][2]=='볼록' and abs(float(a[0][3])-1.12)<0.001)
      await pg.wait_for_timeout(300); 봄(t+"0.2초 뒤 클래스 빠짐", await pg.evaluate("!document.querySelector('.볼록,.오목')"))
      a=await 눌러봄('[data-act="루값"][data-f="w"][data-i="1"][data-k="0"][data-d="-1"]')
      봄(t+f"ls4v 루틴 '−' → 오목 ({a})", len(a)==1 and a[0][0]=='오목' and a[0][2]=='오목' and abs(float(a[0][3])-0.88)<0.001)
      await pg.wait_for_timeout(300)
      y0=await pg.evaluate("document.querySelector('[data-act=\"루세트\"][data-i=\"1\"]').getBoundingClientRect().top")
      a=await 눌러봄('[data-act="루세트"][data-i="1"][data-d="1"]')
      y1=await pg.evaluate("document.querySelector('[data-act=\"루세트\"][data-i=\"1\"]').getBoundingClientRect().top")
      봄(t+f"ls4v '+ 세트' — 다시 그려 {round(y1-y0)}px 내려간 새 단추가 볼록 (배율 {a[0][3] if a else '-'} · 넓은 단추는 6px 안쪽)", len(a)==1 and a[0][1].startswith('+ 세트') and y1>y0 and float(a[0][3])<1.05)
      await pg.wait_for_timeout(300)
      a=await 눌러봄('[data-act="종목빼기"][data-i="3"]')
      봄(t+f"✕ 는 움직이지 않음 ({a})", a==[])
      await pg.wait_for_timeout(300)
      # 다른 화면 — 운동 중 세트 값 ＋/− · 루틴 목록 + 루틴
      await pg.evaluate("(()=>{운동시작('r1'); U.탭='운동'; 그리기();})()"); await pg.wait_for_timeout(300)
      ws=await pg.evaluate("(()=>{const p=document.querySelector('#폰 button[aria-label=\"더하기\"]'), m=document.querySelector('#폰 button[aria-label=\"빼기\"]'); const sel=e=>e?[...e.attributes].filter(a=>a.name.startsWith('data-')).map(a=>`[${a.name}=\"${a.value}\"]`).join(''):null; return [sel(p),sel(m)];})()")
      if ws[0]:
        a=await 눌러봄('#폰 button'+ws[0]); 봄(t+f"ls4v 운동 화면 '＋' → 볼록 ({a})", len(a)==1 and a[0][0]=='볼록')
        await pg.wait_for_timeout(300)
      if ws[1]:
        a=await 눌러봄('#폰 button'+ws[1]); 봄(t+f"ls4v 운동 화면 '−' → 오목 ({a})", len(a)==1 and a[0][0]=='오목')
        await pg.wait_for_timeout(300)
      await pg.evaluate("(()=>{S.세션=null; U.탭='루틴'; U.루틴열림=null; 그리기();})()"); await pg.wait_for_timeout(200)
      a=await 눌러봄('[data-act="휴식일추가"]'); 봄(t+f"ls4v 루틴 목록 '+ 휴식일' (화면이 바뀌면 조용히 넘어감 · 오류 없음) {a}", not 오류)
      await pg.evaluate("(()=>{U.루틴열림=null; 그리기();})()"); await pg.wait_for_timeout(300)
      # 움직임 줄이기
      await pg.emulate_media(reduced_motion='reduce'); await pg.evaluate("(()=>{U.루틴열림='r1'; 그리기();})()"); await pg.wait_for_timeout(200)
      a=await 눌러봄('[data-act="루값"][data-f="r"][data-i="1"][data-k="0"][data-d="1"]'); 봄(t+f"ls4v 움직임 줄이기면 하지 않음 ({a})", a==[])
      await pg.emulate_media(reduced_motion='no-preference')
      봄(t+"JS 오류 없음", not 오류)
      if 오류: print(오류[:3])
      await pg.close()
    # 어두운 화면 — 띠 위 스위치 · 이름 칸 · [플랜] 표가 보이는지 눈으로
    pg=await b.new_page(viewport={'width':420,'height':1080}, color_scheme='dark'); await pg.goto(F); await pg.wait_for_timeout(1200)
    await pg.evaluate("(()=>{U.업적띠=null; U.탭='루틴'; U.루틴열림='r1'; 그리기();})()"); await pg.wait_for_timeout(400)
    await (await pg.query_selector('#폰')).screenshot(path='v8R_420_어둠.png'); await pg.close()
    await b.close()
  [print(("✅ " if ok else "❌ ")+m) for m,ok in 결과]; print(f"\n{sum(ok for _,ok in 결과)}/{len(결과)}")
asyncio.run(main())
