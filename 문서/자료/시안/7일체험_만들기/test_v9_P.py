"""10-03 ✎ 표시 6개(플랜 탭 · 결과 시트 · 플랜 고치기 · 종목 탭) 시험 (headless Chromium) — python3 test_v9_P.py [파일]  기본 7day-v9P.html
   4v24 [입력하지 않음] 제목 줄 오른쪽 끝 / 9zt1 판정 알약 제목 바로 옆 / dwyk 5×8~15 · 증량 글 삭제(계산 그대로)
   pt3d 처방 = 세트 · 무게 × 횟수 / q3hz 플랜 고치기 시트 / z2a8 종목 탭 [플랜] 표 · 펼친 칸 플랜 카드
   사진은 지금 폴더에 (SP/p9 안에서 돌린다)"""
import asyncio, sys, re
from playwright.async_api import async_playwright
이름 = sys.argv[1] if len(sys.argv)>1 else '7day-v9P.html'
SP='file:///tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'
F=SP+이름; 옛F=SP+'7day-v8.html'
색JS = """v=>{const d=document.createElement('i'); d.style.color=getComputedStyle(document.documentElement).getPropertyValue(v); document.body.appendChild(d); const c=getComputedStyle(d).color; d.remove(); return c;}"""
# 계산이 v8 과 같은지 — 예시 플랜 · 만든 플랜들의 회차표와 회차마다 처방(무게 · 횟수 · 세트)을 그대로 뽑는다
계산JS = """(()=>{ const 목록=[S.플랜들[0], S.플랜들[1],
    {...S.플랜들[0], id:'a', 종목:'백 스쿼트', 시작1RM:70, 목표무게:180, 한회:0},
    {...S.플랜들[0], id:'b', 종목:'오버헤드 프레스', 시작1RM:30, 목표무게:60, 한회:0, 방식번호:4},
    {...S.플랜들[0], id:'c', 종목:'데드리프트', 시작1RM:100, 목표무게:200, 한회:0, 방식번호:8},
    {...S.플랜들[0], id:'d', 종목:'벤치프레스', 시작1RM:60, 목표무게:120, 한회:0, 방식번호:6},
    {...S.플랜들[1], id:'e', 종목:'맨몸 스쿼트', 시작개수:15, 목표개수:120}];
  return 목록.map(p=>플랜회표(p).map(x=>[x.회, Math.round(x.목표값*1e4)/1e4, x.측정일, JSON.stringify(회처방(p,x.목표값,x.측정일))])); })()"""
async def main():
  결과=[]
  def 봄(m,ok): 결과.append((m,bool(ok)))
  async with async_playwright() as p:
    b=await p.chromium.launch()
    # ── dwyk 계산은 그대로 — v8 과 v9 의 회차표 · 처방 값이 똑같다 ──
    옛=await b.new_page(); await 옛.goto(옛F); await 옛.wait_for_timeout(1200); 옛값=await 옛.evaluate(계산JS); await 옛.close()
    새=await b.new_page(); await 새.goto(F); await 새.wait_for_timeout(1200); 새값=await 새.evaluate(계산JS)
    이중=await 새.evaluate("({이중:typeof 이중진행==='function' && typeof 이중==='object', 요약:typeof 방식요약})"); await 새.close()
    봄(f"dwyk 꾸준히 늘리기 계산 그대로 — 회차표 · 회차마다 처방이 v8 과 같음 (플랜 {len(새값)}개 · 회차 {sum(len(x) for x in 새값)}줄)", 옛값==새값 and all(len(x)>0 for x in 새값))
    봄(f"dwyk 이중진행 · 이중 남아 있음 · 방식요약 지움 ({이중})", 이중['이중'] and 이중['요약']=='undefined')
    for 폭,높이 in [(420,1080),(360,800)]:
      pg=await b.new_page(viewport={'width':폭,'height':높이}); 오류=[]; pg.on('pageerror',lambda e:오류.append(str(e)))
      await pg.goto(F); await pg.wait_for_timeout(1500); t=f"[{폭}] "
      await pg.evaluate("(()=>{U.업적띠=null; U.탭='플랜'; 그리기();})()"); await pg.wait_for_timeout(400); await pg.evaluate("window.색="+색JS)
      async def 사진(n):
        await (await pg.query_selector('#폰')).screenshot(path=f'v9P_{폭}_{n}.png')
      async def 넘침(안='#폰'):
        return await pg.evaluate("""(sel)=>{const 폰=document.getElementById('폰'), q=폰.getBoundingClientRect(); const 넘=[...document.querySelector(sel).querySelectorAll('*')].filter(e=>{const r=e.getBoundingClientRect(); return r.width>0 && (r.right>q.right+1||r.left<q.left-1) && !e.closest('.가로밀기,.운띠,.사진줄');}).map(e=>e.className||e.tagName);
          return {문서:document.documentElement.scrollWidth>innerWidth, 넘:넘.slice(0,4)};}""", 안)
      async def 폼비움(종목='벤치프레스'):
        await pg.evaluate(f"(()=>{{U.시트=null; U.플랜폼=폼초기(); U.플랜폼.종목='{종목}'; U.탭='플랜'; 그리기();}})()"); await pg.wait_for_timeout(250)
      async def 자리():
        return await pg.evaluate("""(()=>{ const 알=id=>document.getElementById(id), r=e=>e.getBoundingClientRect(), 가=e=>(r(e).top+r(e).bottom)/2;
          const 현=알('현재판정'), 목=알('목표판정'), 현카=현.closest('.카드'), 목카=목.closest('.카드'), 현제=현카.querySelector('b.작'), 목제=목카.querySelector('b.작');
          const 단=document.querySelector('[data-act="폼입력안함"]'), 칸=document.querySelector('.현재칸'), 안=알('폼안내');
          return {현틈:Math.round(r(현).left-r(현제).right), 목틈:Math.round(r(목).left-r(목제).right), 현같은줄:Math.abs(가(현)-가(현제))<3, 목같은줄:Math.abs(가(목)-가(목제))<3,
            단같은줄:Math.abs(가(단)-가(현제))<3, 단오른:Math.round(r(현카).right-r(단).right), 단위:r(단).bottom<=r(칸).top, 단높:Math.round(r(단).height),
            단글:단.textContent, 안글:안.textContent, 안보임:getComputedStyle(안).display!=='none', 안아래:!안.textContent||r(안).top>=r(칸).bottom,
            현글:현.textContent, 목글:목.textContent};})()""")
      # ── 4v24 · 9zt1 플랜 탭 ──
      await 폼비움()
      a=await 자리()
      봄(t+f"4v24 [입력하지 않음] = '현재 근력' 제목 줄 · 카드 오른쪽 끝(안쪽 {a['단오른']}px) · 입력 칸 위 · 높이 {a['단높']}", a['단같은줄'] and a['단오른']<=14 and a['단위'] and a['단높']==32 and a['단글']=='입력하지 않음')
      봄(t+f"4v24 안내 글 비어 있으면 숨김 ('{a['안글']}')", a['안글']=='' and not a['안보임'])
      await pg.fill('[data-in="폼"][data-f="현재무게"]','100'); await pg.fill('[data-in="폼"][data-f="현재횟수"]','15'); await pg.fill('[data-in="폼"][data-f="목표무게"]','100'); await pg.wait_for_timeout(120)
      a=await 자리()
      봄(t+f"9zt1 판정 알약이 제목 글자 바로 옆 (틈 현재 {a['현틈']} · 목표 {a['목틈']}) · '{a['현글']}' · '{a['목글']}'", a['현같은줄'] and a['목같은줄'] and a['현틈']==8 and a['목틈']==8 and a['현글']=='미친사람' and a['목글']=='상위 20%')
      봄(t+f"9zt1 · 4v24 한 줄: 현재 근력 [미친사람] ……… [입력하지 않음] (단추 오른쪽 끝 {a['단오른']})", a['단같은줄'] and a['단오른']<=14)
      봄(t+f"4v24 안내 '{a['안글']}' = 입력 칸 아래 한 줄", a['안글'].startswith('1RM 약') and a['안보임'] and a['안아래'])
      n=await 넘침(); 높=await pg.evaluate("(()=>{const n=document.querySelector('#폰 .넘김'); return n.scrollHeight-n.clientHeight;})()")
      봄(t+f"플랜 탭 가로 넘침 없음 · 세로 넘침 {높}px (420: 0 · 360 체험 폰: v8 보다 적거나 같게 < 95)", not n['문서'] and not n['넘'] and (높<=0 if 폭==420 else 높<95))
      await 사진('플랜탭')
      await pg.click('[data-act="폼입력안함"]'); await pg.wait_for_timeout(300)
      a=await 자리()
      봄(t+f"4v24 켜면 ✓ · 같은 자리 · 현재 판정 비움 · 안내 '{a['안글']}'", a['단글'].startswith('✓') and a['단같은줄'] and a['단오른']<=14 and a['현글']=='' and a['안글']=='1회차에 근력 측정')
      await 사진('플랜탭_입력안함')
      await pg.click('[data-act="폼입력안함"]'); await pg.wait_for_timeout(250)
      # 맨몸(턱걸이 · 어시스트) — 안내는 정자세 환산
      await 폼비움('턱걸이'); await pg.click('[data-act="폼값"][data-f="보조모드"][data-v="1"]'); await pg.wait_for_timeout(250)
      await pg.fill('[data-in="폼"][data-f="보조무게"]','20'); await pg.fill('[data-in="폼"][data-f="보조횟수"]','8'); await pg.fill('[data-in="폼"][data-f="목표개수"]','18'); await pg.wait_for_timeout(120)
      a=await 자리(); n=await 넘침()
      봄(t+f"턱걸이 어시스트: 판정 '{a['현글']}' · '{a['목글']}' 제목 옆 · 안내 '{a['안글']}' · 넘침 없음", a['현틈']==8 and a['목틈']==8 and '정자세' in a['안글'] and not n['문서'] and not n['넘'])
      await 사진('플랜탭_턱걸이')
      # ── dwyk 화면 글 ──
      await 폼비움()
      방=await pg.evaluate("""(()=>({줄:document.querySelector('.방식고름줄').innerText, 칩:!!document.querySelector('.방식고름줄 .칩'), 탭:document.querySelector('#폰 .넘김').innerText}))()""")
      봄(t+f"dwyk 훈련 방식 줄에 '5×8~15' 칩 없음 ('{방['줄'].replace(chr(10),' | ')}')", not 방['칩'] and '8~15' not in 방['줄'] and '8~15' not in 방['탭'])
      await pg.click('.방식고름줄'); await pg.wait_for_timeout(400)
      시=await pg.evaluate("""(()=>{const 속=document.querySelector('.방식속'); return {글:속.innerText, 시트:document.querySelector('.시트').innerText};})()""")
      봄(t+f"dwyk 훈련 방식 시트 1번: '근육과 기초 근력을 함께' 만 · 증량 글 없음 ({시['글'].replace(chr(10),' | ')[:70]})", '근육과 기초 근력을 함께' in 시['글'] and '15회가 되면' not in 시['시트'] and '8~15' not in 시['시트'] and '무게를 올리고' not in 시['시트'])
      await 사진('방식시트')
      # ── pt3d 처방 글 ──
      글=await pg.evaluate("""(()=>({한:처방글([{무게:71,횟수:8,세트:5}]), 여:처방글([{무게:50,횟수:5,세트:5,라벨:"월"},{무게:42.5,횟수:5,세트:4,라벨:"수"}]), 더:처방글([{무게:40,횟수:5,세트:1,라벨:"65%"},{무게:60,횟수:5,세트:1,라벨:"85%",더:true}]),
        변한:처방변화글([{무게:71,횟수:8,세트:5}],[{무게:71,횟수:10,세트:5}]), 변여:처방변화글([{무게:50,횟수:5,세트:5,라벨:"월"}],[{무게:50,횟수:5,세트:5,라벨:"월"}]),
        변여2:처방변화글([{무게:50,횟수:5,세트:5,라벨:"월"},{무게:42.5,횟수:5,세트:4,라벨:"수"}],[{무게:55,횟수:5,세트:5,라벨:"월"},{무게:42.5,횟수:5,세트:4,라벨:"수"}])}))()""")
      봄(t+f"pt3d 처방글 '{글['한']}'", 글['한']=='5세트 · 71kg × 8회')
      봄(t+f"pt3d 여러 줄 '{글['여']}' · '+' 유지 '{글['더']}'", 글['여']=='월 5세트 · 50kg × 5회 / 수 4세트 · 42.5kg × 5회' and 글['더']=='65% 1세트 · 40kg × 5회 / 85% 1세트 · 60kg × 5+회')
      봄(t+f"pt3d 결과 시트 변화 글 '{글['변한']}' · '{글['변여2']}'", 글['변한']=='5세트 · 71kg × 8회 → 71kg × 10회' and 글['변여2']=='월 5세트 · 50→55kg × 5회 / 수 4세트 · 42.5kg × 5회')
      await pg.evaluate("(()=>{U.시트=null; 그리기();})()"); await pg.wait_for_timeout(200)
      await pg.fill('[data-in="폼"][data-f="현재무게"]','70'); await pg.fill('[data-in="폼"][data-f="현재횟수"]','8'); await pg.fill('[data-in="폼"][data-f="목표무게"]','110')
      await pg.click('[data-act="플랜만들기"]'); await pg.wait_for_timeout(450)
      결=await pg.evaluate("""(()=>{const s=document.querySelector('.시트'), 블=[...s.querySelectorAll('.회블록')]; return {제목:s.querySelector('.머리 b').textContent, 처:블.map(x=>x.children[1].textContent), 글:s.innerText,
        넘:[...s.querySelectorAll('*')].filter(q=>{const r=q.getBoundingClientRect(), p=s.getBoundingClientRect(); return r.width>0&&(r.right>p.right+1||r.left<p.left-1);}).length};})()""")
      한=r'\d+세트 · [\d.]+kg × \d+\+?회'
      봄(t+f"pt3d 플랜 결과 시트 처방 = 세트 · 무게 × 횟수 ({결['처'][0]} | {결['처'][1]})", all(re.fullmatch(f'{한}( → [\\d.]+kg × \\d+\\+?회)?', x) for x in 결['처']) and not re.search(r'회 × \d+세트', 결['글']) and 결['넘']==0)
      await 사진('결과시트')
      await pg.click('[data-act="플랜저장"]'); await pg.wait_for_timeout(450)
      저=await pg.evaluate("({탭:U.탭, 펼:U.종목펼침, 카:[...document.querySelectorAll('.플랜카드')].map(c=>c.querySelector('b').textContent)})")
      봄(t+f"z2a8 플랜 저장 → 종목 탭 · 그 종목 줄이 펼쳐져 새 플랜 카드가 보임 ({저})", 저['탭']=='종목' and 저['펼']=='벤치프레스' and 저['카']==['벤치프레스','벤치프레스 2'])
      # 여러 줄 방식(매드카우 4번) 플랜 — 결과 시트 · 종목 탭 카드
      await 폼비움('백 스쿼트'); await pg.fill('[data-in="폼"][data-f="현재무게"]','80'); await pg.fill('[data-in="폼"][data-f="현재횟수"]','5'); await pg.fill('[data-in="폼"][data-f="목표무게"]','120')
      await pg.click('.방식고름줄'); await pg.wait_for_timeout(300); await pg.click('.방식띠[data-n="4"]'); await pg.wait_for_timeout(250); await pg.click('.방식속 [data-act="방식고름"]'); await pg.wait_for_timeout(300)
      await pg.click('[data-act="플랜만들기"]'); await pg.wait_for_timeout(450)
      여=await pg.evaluate("[...document.querySelectorAll('.시트 .회블록')].map(x=>x.children[1].textContent)")
      봄(t+f"pt3d 여러 줄 방식 결과 시트 ({여[0][:60]}…)", all(re.fullmatch(r'월 \d+세트 · [\d.→]+kg × \d+회 / 수 \d+세트 · [\d.→]+kg × \d+회 / 금 \d+세트 · [\d.→]+kg × \d+회', x) for x in 여))
      await pg.click('[data-act="플랜저장"]'); await pg.wait_for_timeout(450)
      카=await pg.evaluate("""(()=>{const c=[...document.querySelectorAll('.플랜카드')].at(-1), 처=[...c.children].find(x=>/회차|달성/.test(x.textContent)), r=처.getBoundingClientRect();
        return {글:처.textContent, 글자:parseFloat(getComputedStyle(처).fontSize), 넘:처.scrollWidth>처.clientWidth+1};})()""")
      봄(t+f"pt3d 종목 탭 여러 줄 방식 카드 '{카['글'][:50]}…' 글자 {카['글자']} (감김 · 줄지 않음)", re.match(r'\d+회차 월 \d+세트 · [\d.]+kg × \d+회 / 수', 카['글']) and 카['글자']==13 and not 카['넘'])
      await 사진('종목탭_여러줄방식')
      # 종목 탭 · 루틴 [플랜] 줄 처방도 같은 순서
      await pg.evaluate("(()=>{U.종목펼침='벤치프레스'; 그리기();})()"); await pg.wait_for_timeout(300)
      종처=await pg.evaluate("[...document.querySelectorAll('.플랜카드')].map(c=>[...c.children].find(x=>/회차|달성/.test(x.textContent))?.textContent)")
      봄(t+f"pt3d 종목 탭 플랜 카드 '{종처[0]}'", all(re.fullmatch(r'(측정 · )?\d+회차 '+한, x) for x in 종처))
      await pg.evaluate("(()=>{U.탭='루틴'; U.루틴열림='r1'; 그리기();})()"); await pg.wait_for_timeout(400)
      루=await pg.evaluate("document.querySelector('.루플랜글')?.textContent")
      봄(t+f"pt3d 루틴 [플랜] 줄 '{루}'", 루 and re.fullmatch(r'· (측정 · )?\d+회차 '+한, 루))
      # ── q3hz 플랜 고치기 ──
      await pg.evaluate("(()=>{U.루틴열림=null; U.탭='종목'; U.종목펼침='벤치프레스'; 그리기();})()"); await pg.wait_for_timeout(300)
      await pg.click('[data-act="플랜고치기"][data-v="p1"]'); await pg.wait_for_timeout(500)
      async def 고침():
        return await pg.evaluate("""(()=>{const s=document.querySelector('.시트'), r=e=>e.getBoundingClientRect(), 가=e=>(r(e).top+r(e).bottom)/2;
          const 표=[...s.querySelectorAll('.고침표')], 표글=표.map(x=>x.textContent), 목표=표.find(x=>x.textContent==='목표'), 칩=[...s.querySelectorAll('.고침첫줄 [data-act="고침목표방식"]')];
          const 현=표.find(x=>x.textContent==='현재 수행능력'), 지=현?.nextElementSibling, 단=s.querySelector('[data-act="고침입력안함"]'), 줄=현?.parentElement;
          const 닫=s.querySelector('.버튼[data-act="시트닫기"]'), 넣=s.querySelector('[data-act="측정넣기"]'), cs=getComputedStyle(닫);
          const 넘=[...s.querySelectorAll('*')].filter(e=>{const q=r(e), p=r(s); return q.width>0&&(q.right>p.right-13||q.left<p.left+13)&&!e.closest('.머리');}).map(e=>e.className||e.tagName).slice(0,4);
          return {제목:s.querySelector('.머리 b').textContent, 제목넘:(()=>{const b=s.querySelector('.머리 b'); return b.scrollWidth>b.clientWidth+1;})(), 표글,
            표모양:표.every(x=>{const c=getComputedStyle(x); return c.color===색('--글') && c.fontSize==='15px' && +c.fontWeight>=700;}),
            칩:칩.map(x=>x.textContent), 칩높:칩.map(x=>x.offsetHeight), 칩같은줄:칩.length?칩.every(x=>Math.abs(가(x)-가(목표))<3)&&r(칩[0]).left>r(목표).right&&r(칩[0]).left-r(목표).right<=8:true,
            지:지?.textContent, 지틈:지?Math.round(r(지).left-r(현).right):null, 지같은줄:지?Math.abs(가(지)-가(현))<3:false,
            단:단?.textContent, 단같은줄:단?Math.abs(가(단)-가(현))<3:false, 단오른:단?Math.round(r(줄).right-r(단).right):null, 단모양:단?.className,
            닫:[cs.borderTopWidth, cs.borderTopColor===색('--나쁨'), cs.color===색('--나쁨'), cs.backgroundColor===색('--면')], 넣:넣.textContent,
            글:s.innerText, 흐림:!!s.querySelector('.측정줄.흐린칸'), 넘, 스크롤:s.scrollHeight>s.clientHeight+1, 이름값:s.querySelector('[data-f="이름"]').value};})()""")
      g=await 고침()
      봄(t+f"q3hz 시트 제목 '{g['제목']}' (안 넘침)", g['제목']=='벤치프레스 플랜 고치기' and not g['제목넘'])
      봄(t+f"q3hz 이름표 {g['표글']} = 검은 글씨 15 Bold", g['표글']==['운동 이름','목표','현재 수행능력'] and g['표모양'] and '이름' not in [x for x in g['표글']] and g['이름값']=='벤치프레스')
      봄(t+f"q3hz '목표' 바로 뒤 같은 줄에 {g['칩']} · 칩 한 줄(높이 {g['칩높']})", g['칩']==['1RM','무게 × 횟수'] and g['칩같은줄'] and g['칩높']==[28,28])
      봄(t+f"q3hz '현재 수행능력' 바로 오른쪽 '{g['지']}' (틈 {g['지틈']}) · 줄 맨 오른쪽 [{g['단']}] (안쪽 {g['단오른']})", g['지'] and g['지'].startswith('지금 1RM') and g['지같은줄'] and g['지틈']==8 and g['단']=='입력하지 않음' and g['단같은줄'] and g['단오른']==0 and '안함단추' in g['단모양'] and '나쁨' in g['단모양'])
      봄(t+f"q3hz [입력] (넣기 아님) · [닫기] 테두리 2px · 테두리 · 글자 --나쁨 · 흰 바탕 ({g['닫']})", g['넣']=='입력' and '넣기' not in g['글'] and g['닫']==['2px',True,True,True])
      봄(t+f"q3hz 시트 안 넘침 · 스크롤 없음 ({g['넘']})", not g['넘'] and not g['스크롤'])
      봄(t+"dwyk 고침 시트에도 '8~15' 없음", '8~15' not in g['글'])
      await 사진('고침_1RM')
      await pg.click('.시트 [data-act="고침목표방식"][data-v="회"]'); await pg.wait_for_timeout(350)
      g=await 고침(); 봄(t+f"q3hz 무게 × 횟수 골라도 한 줄 · 칩 높이 {g['칩높']} · 안 넘침 ({g['넘']})", g['칩같은줄'] and g['칩높']==[28,28] and not g['넘'])
      await 사진('고침_회')
      # [입력] 동작 — 그대로
      await pg.fill('.시트 [data-f="측정무게"]','90'); await pg.fill('.시트 [data-f="측정횟수"]','3'); await pg.click('.시트 [data-act="측정넣기"]'); await pg.wait_for_timeout(300)
      봄(t+"q3hz [입력] 누르면 현재 수행능력 들어감 (예전 넣기와 같음)", await pg.evaluate("S.플랜들[0].측정들.some(m=>m.무게===90&&m.횟수===3) && U.고침.측정무게===''"))
      # [입력하지 않음] 켜면 측정 줄 흐림 · [입력] 막힘 · 저장해도 안 들어감
      await pg.click('.시트 [data-act="고침입력안함"]'); await pg.wait_for_timeout(300)
      g=await 고침()
      봄(t+f"q3hz [입력하지 않음] 켜면 ✓ · 측정 줄 흐림 · 같은 자리 ({g['단']})", g['단'].startswith('✓') and g['흐림'] and g['단같은줄'] and g['단오른']==0 and not g['넘'])
      await 사진('고침_입력안함')
      await pg.evaluate("(()=>{U.고침.측정무게='95'; U.고침.측정횟수='2';})()"); 전=await pg.evaluate("S.플랜들[0].측정들.length")
      await pg.evaluate("행동('측정넣기',{})"); 막=await pg.evaluate("S.플랜들[0].측정들.length")
      await pg.click('.시트 [data-act="고침저장"]'); await pg.wait_for_timeout(350)
      후=await pg.evaluate("({n:S.플랜들[0].측정들.length, 시트:U.시트})")
      봄(t+f"q3hz 켠 채로 [입력] · [저장] → 현재 수행능력 안 들어감 ({전} → {막} → {후['n']}) · 시트 닫힘", 전==막==후['n'] and not 후['시트'])
      # 끈 채로 적어 두고 [저장]만 → 함께 들어감
      await pg.click('[data-act="플랜고치기"][data-v="p1"]'); await pg.wait_for_timeout(450)
      g=await 고침(); 봄(t+"q3hz 다시 열면 [입력하지 않음] 꺼져 있음", g['단']=='입력하지 않음' and not g['흐림'])
      await pg.fill('.시트 [data-f="측정무게"]','92.5'); await pg.fill('.시트 [data-f="측정횟수"]','2'); await pg.click('.시트 [data-act="고침저장"]'); await pg.wait_for_timeout(350)
      봄(t+"q3hz 꺼진 채 적어 두고 [저장] → 현재 수행능력도 함께 들어감", await pg.evaluate("S.플랜들[0].측정들.some(m=>m.무게===92.5&&m.횟수===2) && !U.시트"))
      # 맨몸(턱걸이 p2) · 360 넘침
      await pg.evaluate("(()=>{U.종목펼침='턱걸이'; 그리기();})()"); await pg.wait_for_timeout(250)
      await pg.click('[data-act="플랜고치기"][data-v="p2"]'); await pg.wait_for_timeout(450)
      g=await 고침()
      봄(t+f"q3hz 맨몸(턱걸이): 제목 '{g['제목']}' · 이름표 {g['표글']} · '{g['지']}' · 안 넘침 ({g['넘']})", g['제목']=='턱걸이 플랜 고치기' and g['표글']==['운동 이름','목표','현재 수행능력'] and g['지'].startswith('지금 최대') and g['단오른']==0 and not g['넘'])
      await 사진('고침_맨몸')
      await pg.click('.시트 [data-act="시트닫기"].버튼'); await pg.wait_for_timeout(300)
      봄(t+"q3hz [닫기] 동작", await pg.evaluate("!U.시트"))
      # 긴 이름 — 제목이 한 줄에 맞춰 줄어든다
      await pg.evaluate("(()=>{S.플랜들[0].이름='인클라인 벤치프레스 연습 2'; 행동('플랜고치기',{v:'p1'}); 그리기();})()"); await pg.wait_for_timeout(450)
      g=await 고침(); 봄(t+f"q3hz 긴 이름 제목 '{g['제목']}' 한 줄 · 안 넘침", not g['제목넘'] and not g['넘'])
      await pg.evaluate("(()=>{S.플랜들[0].이름='벤치프레스'; U.시트=null; 그리기();})()"); await pg.wait_for_timeout(250)
      # ── z2a8 종목 탭 ──
      await pg.evaluate("(()=>{U.종목펼침=null; U.탭='종목'; 그리기();})()"); await pg.wait_for_timeout(350)
      종=await pg.evaluate("""(()=>{const 넘=document.querySelector('#폰 .넘김'), 표=[...넘.querySelectorAll('.이름표')].map(x=>x.textContent);
        const 줄=[...넘.querySelectorAll('.종목줄')], 플=줄.filter(x=>x.querySelector('.플랜표'));
        return {표, 카드:넘.querySelectorAll('.플랜카드').length, 플:플.map(x=>x.querySelector('.종목줄머리').innerText.replace(/\\n/g,' | ')), 줄수:줄.length};})()""")
      봄(t+f"z2a8 '운동 플랜' 묶음 없음 · 접힌 채로 플랜 카드 0 ({종['표']})", '운동 플랜' not in 종['표'] and 종['카드']==0)
      봄(t+f"z2a8 플랜 있는 종목 줄에만 [플랜] 표 ({종['플']})", sorted(x.split(' | ')[0] for x in 종['플'])==['백 스쿼트','벤치프레스','턱걸이'])
      딱=await pg.evaluate("""(()=>{const 줄=[...document.querySelectorAll('.종목줄')].find(x=>x.querySelector('.종목줄머리').dataset.v==='턱걸이'), b=줄.querySelector('.이름플랜>b'), 표=줄.querySelector('.플랜표'), r=e=>e.getBoundingClientRect(), cs=getComputedStyle(표);
        const 글=document.createRange(); 글.selectNodeContents(b); const 글r=글.getBoundingClientRect();
        return {겹침:Math.round((r(b).right-r(표).left)*10)/10, 위:Math.round((글r.top-r(표).top)*10)/10, 바탕:cs.backgroundColor===색('--강조'), 글자:cs.color===색('--강조글'), 크기:cs.fontSize, 굵:cs.fontWeight, 표글:표.textContent,
          가능:[...document.querySelectorAll('.종목줄머리')].find(x=>x.dataset.v==='데드리프트').innerText.includes('플랜 가능'), 턱가능:줄.innerText.includes('플랜 가능')};})()""")
      봄(t+f"z2a8 [플랜] 표 = 이름 오른쪽 끝 {딱['겹침']}px 겹침 · 글자 윗선보다 {딱['위']}px 위 · 강조 바탕 · 강조글 · {딱['크기']} {딱['굵']}", abs(딱['겹침']-5)<=0.6 and 딱['위']>=4 and 딱['바탕'] and 딱['글자'] and 딱['크기']=='11px' and int(딱['굵'])>=700 and 딱['표글']=='플랜')
      봄(t+"z2a8 플랜 없는 종목은 '플랜 가능' 그대로 · 플랜 있는 종목엔 없음", 딱['가능'] and not 딱['턱가능'])
      # 루틴 화면 [플랜] 표와 같은 모양
      같=await pg.evaluate("""(()=>{const 뽑=e=>{const c=getComputedStyle(e); return [c.backgroundColor,c.color,c.fontSize,c.fontWeight,c.lineHeight,c.paddingLeft,c.borderRadius,c.top,c.marginLeft].join('|');};
        const 종=뽑(document.querySelector('.종목줄 .플랜표')); U.탭='루틴'; U.루틴열림='r1'; 그리기(); const 루=뽑(document.querySelector('.루플랜 .플랜표')); U.탭='종목'; U.루틴열림=null; 그리기(); return {종, 루};})()""")
      봄(t+f"z2a8 루틴 화면 [플랜] 표와 같은 부품 · 같은 값 ({같['종']})", 같['종']==같['루'])
      await pg.wait_for_timeout(300); await 사진('종목탭')
      # 펼치면 플랜 카드 (같은 종목 플랜 여럿 → 플랜마다 하나)
      await pg.click('.종목줄머리[data-v="벤치프레스"]'); await pg.wait_for_timeout(450)
      펼=await pg.evaluate("""(()=>{const 줄=[...document.querySelectorAll('.종목줄')].find(x=>x.querySelector('.종목줄머리').dataset.v==='벤치프레스'), 속=줄.querySelector('.종목펼속'), 카=[...속.querySelectorAll('.플랜카드')];
        return {카:카.map(c=>c.querySelector('b').textContent), 첫:속.firstElementChild?.classList.contains('플랜카드'), 단추:카.every(c=>c.querySelector('[data-act="플랜고치기"]')&&c.querySelector('[data-act="플랜지움"]')),
          게이지:카.every(c=>c.querySelector('.게이지3')), 사진:!!속.querySelector('.사진줄'), 곁:줄.querySelector('.종목줄머리').innerText.replace(/\\n/g,' | ')};})()""")
      봄(t+f"z2a8 벤치프레스 줄 펼침 → 플랜 카드 {펼['카']} (맨 위) · 진행 막대 · [변경][지우기] · 사진 칸", 펼['카']==['벤치프레스','벤치프레스 2'] and 펼['첫'] and 펼['단추'] and 펼['게이지'] and 펼['사진'])
      봄(t+f"z2a8 플랜 2개 줄 오른쪽 '플랜 2개' ({펼['곁']})", '플랜 2개' in 펼['곁'])
      n=await 넘침(); 봄(t+f"z2a8 펼친 종목 탭 가로 넘침 없음 ({n})", not n['문서'] and not n['넘'])
      await 사진('종목탭_펼침')
      await pg.click('.플랜카드 [data-act="플랜고치기"]'); await pg.wait_for_timeout(400)
      봄(t+"z2a8 펼친 칸 [변경] → 플랜 고치기 시트", await pg.evaluate("U.시트?.종류==='고침' && U.고침.id==='p1'"))
      await pg.evaluate("(()=>{U.시트=null; 그리기();})()"); await pg.wait_for_timeout(200)
      # 지우기 두 번 → 그 줄 안에서 사라지고, 마지막 플랜까지 지우면 [플랜] 표도 없어짐
      p2id=await pg.evaluate("S.플랜들.find(p=>p.이름==='벤치프레스 2').id")
      await pg.click(f'[data-act="플랜지움"][data-v="{p2id}"]'); await pg.wait_for_timeout(200); await pg.click(f'[data-act="플랜지움"][data-v="{p2id}"]'); await pg.wait_for_timeout(300)
      지=await pg.evaluate("({카:[...document.querySelectorAll('.플랜카드')].map(c=>c.querySelector('b').textContent), 펼:U.종목펼침})")
      봄(t+f"z2a8 펼친 칸에서 [지우기] 두 번 → 그 플랜만 사라짐 ({지})", 지['카']==['벤치프레스'] and 지['펼']=='벤치프레스')
      # 종목표에 없는 종목의 플랜 → '기타'
      await pg.evaluate("(()=>{S.종목표=S.종목표.filter(x=>x.이름!=='펜들레이 로우'); S.플랜들.push({...S.플랜들[0], id:'고아', 이름:'펜들레이 로우', 종목:'펜들레이 로우', 시작1RM:60, 목표무게:90, 한회:0, 측정들:[]}); U.종목펼침='펜들레이 로우'; 그리기();})()"); await pg.wait_for_timeout(350)
      기=await pg.evaluate("""(()=>{const 표=[...document.querySelectorAll('#폰 .넘김 .이름표')], 끝=표.at(-1), 줄=[...document.querySelectorAll('.종목줄')].find(x=>x.querySelector('.종목줄머리').dataset.v==='펜들레이 로우');
        return {끝표:끝.textContent, 칸:줄?.parentElement.previousElementSibling?.textContent, 표:!!줄?.querySelector('.플랜표'), 카:줄?[...줄.querySelectorAll('.플랜카드 b')].map(x=>x.textContent):[]};})()""")
      봄(t+f"z2a8 종목표에 없는 종목의 플랜 → '기타' 칸 줄 · [플랜] 표 · 펼치면 카드 ({기})", 기['끝표']=='기타' and 기['칸']=='기타' and 기['표'] and 기['카']==['펜들레이 로우'])
      await 사진('종목탭_기타')
      봄(t+"JS 오류 없음", not 오류)
      if 오류: print(오류[:3])
      await pg.close()
    await b.close()
  [print(("✅ " if ok else "❌ ")+m) for m,ok in 결과]; print(f"\n{sum(ok for _,ok in 결과)}/{len(결과)}")
asyncio.run(main())
