"""10-03 ✎ 표시 5개(플랜 탭 · 플랜 결과 시트) 시험 (headless Chromium) — python3 test_v8_P.py [파일]  기본 7day-v8P.html
   sjb8 현재 근력 · 목표 근력 · 판정 · 입력하지 않음 / 9rvs 주당 삭제 / 15lk 훈련 방식 / ksao 같은 종목 플랜 여럿 / e8am 결과 시트"""
import asyncio, sys, re
from playwright.async_api import async_playwright
이름 = sys.argv[1] if len(sys.argv)>1 else '7day-v8P.html'
F='file:///tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'+이름
색JS = """v=>{const d=document.createElement('i'); d.style.color=getComputedStyle(document.documentElement).getPropertyValue(v); document.body.appendChild(d); const c=getComputedStyle(d).color; d.remove(); return c;}"""
# 예전 계산(주 2회)을 그대로 옮긴 것 — 새 회차표가 같은 값인지 견준다
옛JS = """(p)=>{ const t=찾표(p.종목), 몸=S.몸, f=2, 측정주기=[4,4,8,12,12], 보=보정배수(p.종목)??1;
  const 포=x=>(1-Math.exp(-x/2))/(1-Math.exp(-1)), 배=(x,부)=>1+({상체:1.0,하체:0.2})[부]*(포(x)-1);
  const 속=(lv)=>(t.속도표?t.속도표[lv]:플랜표.기준속도[lv]*t.배)*나이배수(몸.나이)*배(f,t.부위)*보;
  const 시=지금진행값(p), 목=목표진행값(p), out=[]; let v=시, n=0; const 앞=기준회(p);
  while(v<목 && n<플랜표.최대회){ n++; const lv=수준보기(t,v,몸), 넉=속(lv); if(넉<=0) break; v*=Math.pow(1+넉/100,1/(4*f));
    const 회=n+앞; out.push([회, Math.round(v*1e6)/1e6, 회%Math.max(1,측정주기[lv]*f)===0]); }
  return out; }"""
async def main():
  결과=[]
  def 봄(m,ok): 결과.append((m,bool(ok)))
  async with async_playwright() as p:
    b=await p.chromium.launch()
    for 폭,높이 in [(420,1080),(360,800)]:
      pg=await b.new_page(viewport={'width':폭,'height':높이}); 오류=[]; pg.on('pageerror',lambda e:오류.append(str(e)))
      await pg.goto(F); await pg.wait_for_timeout(1500); t=f"[{폭}] "
      await pg.evaluate("(()=>{U.업적띠=null; U.탭='플랜'; 그리기();})()"); await pg.wait_for_timeout(400); await pg.evaluate("window.색="+색JS)
      async def 사진(n):
        if 폭==420 or n.startswith('360'): await (await pg.query_selector('#폰')).screenshot(path=f'v8P_{n}.png')
      async def 넘침():
        return await pg.evaluate("(()=>{const 폰=document.getElementById('폰'); const 넘=[...폰.querySelectorAll('*')].filter(e=>{const r=e.getBoundingClientRect(), q=폰.getBoundingClientRect(); return r.width>0 && (r.right>q.right+1||r.left<q.left-1) && !e.closest('.가로밀기,.운띠,.사진줄');}).map(e=>e.className||e.tagName); const 넘김=폰.querySelector('.넘김'); return {문서:document.documentElement.scrollWidth>innerWidth, 넘:넘.slice(0,4), 스크롤:넘김?넘김.scrollHeight>넘김.clientHeight+1:false};})()")
      async def 판정():
        return await pg.evaluate("({현:document.getElementById('현재판정')?.textContent, 목:document.getElementById('목표판정')?.textContent, 안:document.getElementById('폼안내')?.textContent})")
      async def 폼비움(종목='벤치프레스'):
        await pg.evaluate(f"(()=>{{U.시트=null; U.플랜폼=폼초기(); U.플랜폼.종목='{종목}'; U.탭='플랜'; 그리기();}})()"); await pg.wait_for_timeout(250)
      # ── sjb8 카드 이름 · 순서 · 판정 자리 ──
      카=await pg.evaluate("""(()=>{const 넘=document.querySelector('#폰 .넘김'), 카=[...넘.querySelectorAll(':scope .카드')];
        const 제=카.map(c=>c.querySelector('b.작')?.textContent);
        const 목=카.find(c=>c.querySelector('#목표판정')), 현=카.find(c=>c.querySelector('#현재판정'));
        return {제, 목위:목&&현&&목.getBoundingClientRect().bottom<현.getBoundingClientRect().top, 글:넘.innerText};})()""")
      봄(t+f"sjb8 카드 제목 {카['제']} — 목표 근력이 위 · 현재 근력이 아래", 카['제']==['목표 근력','현재 근력'] and 카['목위'])
      봄(t+"sjb8 '지금' · '목표' 단독 제목 없음", '지금' not in 카['제'] and '목표' not in 카['제'])
      판=await 판정(); 봄(t+f"sjb8 아무것도 안 넣으면 판정 비어 있음 ({판})", 판['현']=='' and 판['목']=='')
      await 사진('빈폼');
      if 폭==360: await 사진('360_빈폼')
      # 현재 근력 판정 — 벤치 · 남 · 80kg 경계 [0.6,0.9,1.25,1.6]
      for w,r,기대 in [('60','5','초보자'),('80','5','중급자'),('100','5','상급자'),('140','5','미친사람')]:
        await pg.fill('[data-in="폼"][data-f="현재무게"]', w); await pg.fill('[data-in="폼"][data-f="현재횟수"]', r); await pg.wait_for_timeout(80)
        판=await 판정(); 봄(t+f"sjb8 현재 {w}kg × {r}회 → '{판['현']}' (기대 {기대}) · 안내 '{판['안']}'", 판['현']==기대 and 판['안'].startswith('1RM 약'))
      # 목표 근력 상위 N% — 1RM 72(0.9 → 50%) · 100(1.25 → 80%) · 무게 × 횟수 75 × 10 (= 1RM 100)
      for w,기대 in [('72','상위 50%'),('100','상위 20%'),('160','상위 1%'),('24','상위 90%')]:
        await pg.fill('[data-in="폼"][data-f="목표무게"]', w); await pg.wait_for_timeout(80)
        판=await 판정(); 봄(t+f"sjb8 목표 1RM {w}kg → '{판['목']}' (기대 {기대})", 판['목']==기대)
      await pg.click('[data-act="폼값"][data-f="목표방식"][data-v="회"]'); await pg.wait_for_timeout(250)
      await pg.fill('[data-in="폼"][data-f="목표무게"]','75'); await pg.fill('[data-in="폼"][data-f="목표횟수"]','10'); await pg.wait_for_timeout(80)
      판=await 판정(); 봄(t+f"sjb8 목표 무게 × 횟수 75 × 10 → '{판['목']}' (1RM 100 → 상위 20%)", 판['목']=='상위 20%')
      자=await pg.evaluate("""(()=>{const 알=['목표판정','현재판정'].map(i=>document.getElementById(i)), 카=알.map(a=>a.closest('.카드'));
        return 알.map((a,i)=>{ const r=a.getBoundingClientRect(), c=카[i].getBoundingClientRect(), 제=카[i].querySelector('b.작').getBoundingClientRect(), cs=getComputedStyle(a);
          return {오른:Math.round(c.right-r.right), 같은줄:Math.abs((r.top+r.bottom)/2-(제.top+제.bottom)/2)<3, 바탕:cs.backgroundColor===색('--강조'), 글:cs.color===색('--강조글')}; });})()""")
      봄(t+f"sjb8 판정 알약 = 카드 제목 줄 오른쪽 끝 · 강조 바탕 · 강조글 ({자})", all(x['같은줄'] and x['오른']<=16 and x['바탕'] and x['글'] for x in 자))
      n=await 넘침(); 넘높=await pg.evaluate("(()=>{const n=document.querySelector('#폰 .넘김'); return n.scrollHeight-n.clientHeight;})()")
      봄(t+f"플랜 탭(바벨 · 무게 × 횟수) 가로 넘침 없음 · 세로 넘침 {넘높}px (420: 0 · 360 체험 폰 560: v7 95px 보다 적게)", not n['문서'] and not n['넘'] and (넘높<=0 if 폭==420 else 넘높<95))
      await 사진('판정');
      if 폭==360: await 사진('360_판정')
      # [입력하지 않음]
      안=await pg.evaluate("""(()=>{const b=document.querySelector('[data-act="폼입력안함"]'), cs=getComputedStyle(b), r=b.getBoundingClientRect();
        const 칸=document.querySelector('[data-in="폼"][data-f="현재무게"]').getBoundingClientRect();
        return {글:b.textContent, 바탕:cs.backgroundColor===색('--면'), 빨강:cs.color===색('--나쁨'), 테:cs.borderTopWidth, 높:Math.round(r.height), 아래:r.top>=칸.bottom, 같은카드:!!b.closest('.카드')?.querySelector('#현재판정')};})()""")
      봄(t+f"sjb8 [입력하지 않음] 현재 근력 입력 아래 줄 · 흰 바탕 · 빨간 글자 · 1px · 높이 {안['높']}", 안['글']=='입력하지 않음' and 안['바탕'] and 안['빨강'] and 안['테']=='1px' and 안['아래'] and 안['같은카드'] and 안['높'] in (28,32,40,44))
      await pg.fill('[data-in="폼"][data-f="목표무게"]','100'); await pg.fill('[data-in="폼"][data-f="목표횟수"]','1')
      await pg.click('[data-act="폼입력안함"]'); await pg.wait_for_timeout(300)
      켬=await pg.evaluate("""(()=>{const b=document.querySelector('[data-act="폼입력안함"]'), 칸=document.querySelector('.현재칸'), cs=getComputedStyle(칸);
        return {켬:U.플랜폼.입력안함, 글:b.textContent, 테:getComputedStyle(b).borderTopColor===색('--나쁨'), 흐림:+cs.opacity<0.6, 못누름:cs.pointerEvents==='none', 현:document.getElementById('현재판정').textContent, 안:document.getElementById('폼안내').textContent};})()""")
      봄(t+f"sjb8 켜면 ✓ · 빨간 테두리 · 입력 칸 흐림 · 현재 판정 비움 · 안내 '{켬['안']}'", 켬['켬'] and 켬['글'].startswith('✓') and 켬['테'] and 켬['흐림'] and 켬['못누름'] and 켬['현']=='' and '1회차에 근력 측정' in 켬['안'])
      await 사진('입력안함')
      await pg.click('[data-act="플랜만들기"]'); await pg.wait_for_timeout(450)
      건=await pg.evaluate("""(()=>{const x=U.플랜결과; if(!x) return {오류:U.플랜폼.오류}; const 첫=document.querySelector('.시트 .회블록');
        return {종류:U.시트?.종류, 먼저:x.p.측정먼저, 시작:x.p.시작1RM, 첫회:x.표[0].회, 첫측정:x.표[0].측정일, 첫글:첫.innerText.replace(/\\n/g,' | '), 요약:document.querySelector('.시트 .머리+div').textContent};})()""")
      봄(t+f"sjb8 입력 안 하고 만들기 → 1회차 = 근력 측정 · 시작 추정 {건.get('시작')} (80kg × 0.6) · '{건.get('첫글')}'", 건.get('종류')=='플랜결과' and 건.get('먼저') and abs(건.get('시작',0)-48)<0.01 and 건.get('첫회')==1 and 건.get('첫측정') and '1회차에 근력 측정' in 건.get('첫글',''))
      봄(t+f"sjb8 결과 요약 '추정' 표시 ({건.get('요약')})", '추정' in (건.get('요약') or ''))
      await 사진('입력안함결과')
      # 1회차를 하면 측정이 들어가고 회차표가 그 값으로 다시 걸어간다
      다시=await pg.evaluate("""(()=>{const p=JSON.parse(JSON.stringify(U.플랜결과.p)); p.id='시험'; S.플랜들.push(p);
        const 앞=다음회(p); p.한회=1; p.측정들.push({회:1,날:오늘(),무게:70,횟수:5}); const 뒤=플랜회표(p); S.플랜들=S.플랜들.filter(x=>x.id!=='시험');
        return {앞:[앞.회,앞.측정일], 뒤첫:뒤[0]?.회, 뒤측정1:뒤.some(x=>x.회===1), 시작:Math.round(지금진행값(p))};})()""")
      봄(t+f"sjb8 1회차 측정 뒤엔 2회차부터 · 측정값(1RM {다시['시작']})으로 다시 ({다시})", 다시['앞']==[1,True] and 다시['뒤첫']==2 and not 다시['뒤측정1'] and 다시['시작']==82)
      await pg.evaluate("(()=>{U.시트=null; 그리기();})()"); await pg.wait_for_timeout(250)
      await pg.click('[data-act="폼입력안함"]'); await pg.wait_for_timeout(300)
      끔=await pg.evaluate("({켬:U.플랜폼.입력안함, 글:document.querySelector('[data-act=\"폼입력안함\"]').textContent, 흐림:!!document.querySelector('.현재칸.흐린칸'), 현:document.getElementById('현재판정').textContent})")
      봄(t+f"sjb8 다시 누르면 풀림 · 판정 돌아옴 ({끔})", not 끔['켬'] and 끔['글']=='입력하지 않음' and not 끔['흐림'] and 끔['현']=='미친사람')
      # 맨몸 — 턱걸이 경계 [6,11,18,24]
      await 폼비움('턱걸이')
      await pg.fill('[data-in="폼"][data-f="정자세"]','8'); await pg.fill('[data-in="폼"][data-f="목표개수"]','18'); await pg.wait_for_timeout(80)
      판=await 판정(); 봄(t+f"sjb8 턱걸이 정자세 8회 → '{판['현']}' · 목표 18회 → '{판['목']}'", 판['현']=='초보자' and 판['목']=='상위 20%')
      await pg.click('[data-act="폼값"][data-f="보조모드"][data-v="1"]'); await pg.wait_for_timeout(250)
      await pg.fill('[data-in="폼"][data-f="보조무게"]','20'); await pg.fill('[data-in="폼"][data-f="보조횟수"]','8'); await pg.wait_for_timeout(80)
      판=await 판정(); 봄(t+f"sjb8 턱걸이 어시스트 → 판정 '{판['현']}' · 안내 '{판['안']}' · 방식 카드 없음", 판['현']=='초보자' and '정자세' in 판['안'] and await pg.evaluate("!document.querySelector('.방식고름줄')"))
      n=await 넘침(); 넘높=await pg.evaluate("(()=>{const n=document.querySelector('#폰 .넘김'); return n.scrollHeight-n.clientHeight;})()")
      봄(t+f"플랜 탭(턱걸이 · 어시스트) 가로 넘침 없음 · 세로 넘침 {넘높}px (420: 0 · 360 체험 폰 560: v7 42px 보다 적게)", not n['문서'] and not n['넘'] and (넘높<=0 if 폭==420 else 넘높<42))
      await 사진('턱걸이')
      if 폭==360: await 사진('360_턱걸이')
      await pg.click('[data-act="폼입력안함"]'); await pg.click('[data-act="플랜만들기"]'); await pg.wait_for_timeout(450)
      맨=await pg.evaluate("({종류:U.시트?.종류, p:U.플랜결과&&{시작:U.플랜결과.p.시작개수, 보조:U.플랜결과.p.보조모드, 먼저:U.플랜결과.p.측정먼저}, 첫:U.플랜결과&&U.플랜결과.표[0], 오류:U.플랜폼.오류})")
      봄(t+f"sjb8 턱걸이 입력 안 함 → 시작 추정 {맨['p']} · 1회차 측정", 맨['종류']=='플랜결과' and 맨['p']['시작']==6 and 맨['p']['보조']==0 and 맨['p']['먼저'] and 맨['첫']['측정일'])
      await 폼비움('턱걸이'); await pg.click('[data-act="폼입력안함"]'); await pg.fill('[data-in="폼"][data-f="목표개수"]','3'); await pg.click('[data-act="플랜만들기"]'); await pg.wait_for_timeout(350)
      작=await pg.evaluate("({종류:U.시트?.종류, 시작:U.플랜결과?.p.시작개수, 오류:U.플랜폼.오류})")
      봄(t+f"sjb8 목표가 초보자 시작값보다 작아도 막히지 않음 (목표 3 → 시작 {작['시작']})", 작['종류']=='플랜결과' and abs((작['시작'] or 0)-2.4)<0.01)
      # ── 9rvs 주당 삭제 ──
      await 폼비움()
      주=await pg.evaluate("""(()=>({글:document.querySelector('#폰 .넘김').innerText, 칩:document.querySelectorAll('[data-f="주당"]').length,
        없음:['빈도경고','빈도배수','포화','빈도세기'].filter(n=>{ try{ eval(n); return false; }catch(e){ return true; } }), 표준:플랜표.표준주당===undefined && 플랜표.측정주기===undefined, 폼:'주당' in U.플랜폼}))()""")
      봄(t+f"9rvs 플랜 탭에 '주당' 줄 · 칩 · 경고 글 없음", '주당' not in 주['글'] and 주['칩']==0 and '권장' not in 주['글'])
      봄(t+f"9rvs 지운 함수 · 값 {주['없음']} · 표준주당 · 측정주기 없음 · 폼에 주당 없음", len(주['없음'])==4 and 주['표준'] and not 주['폼'])
      같=await pg.evaluate("""(()=>{ const 옛=""" + 옛JS + """; const 새=p=>플랜회표(p).map(x=>[x.회, Math.round(x.목표값*1e6)/1e6, x.측정일]);
        const 목록=[S.플랜들[0], S.플랜들[1], {...S.플랜들[0], id:'a', 종목:'백 스쿼트', 시작1RM:70, 목표무게:180, 한회:0},
          {...S.플랜들[0], id:'b', 종목:'오버헤드 프레스', 시작1RM:30, 목표무게:60, 한회:0}, {...S.플랜들[1], id:'c', 종목:'맨몸 스쿼트', 시작개수:15, 목표개수:120}];
        const 결=목록.map(p=>{ const a=JSON.stringify(옛(p)), z=JSON.stringify(새(p)); return [p.종목, 옛(p).length, a===z]; });
        const 셋={...S.플랜들[0], 주당:3}, 하나={...S.플랜들[0], 주당:1}, 없음={...S.플랜들[0]}; delete 없음.주당;
        const 옛데이터 = JSON.stringify(새(셋))===JSON.stringify(새(S.플랜들[0])) && JSON.stringify(새(하나))===JSON.stringify(새(없음)) && 새(셋).length>0;
        return {결, 옛데이터, 주:플랜회표(S.플랜들[0]).some(x=>'주' in x)}; })()""")
      봄(t+f"9rvs 회차표가 예전 '주 2회' 값과 똑같음 (회 · 목표값 · 측정일) {같['결']}", all(x[2] and x[1]>0 for x in 같['결']))
      봄(t+f"9rvs 예전 플랜에 주당 1 · 3 이 남아 있어도 깨지지 않고 같은 표 · 회차표에 '주' 없음", 같['옛데이터'] and not 같['주'])
      # ── 15lk 훈련 방식 ──
      방=await pg.evaluate("""(()=>{const 줄=document.querySelector('.방식고름줄'), cs=getComputedStyle(줄), b=줄.querySelector('b'), r=줄.getBoundingClientRect(), 쌓=줄.parentElement.getBoundingClientRect();
        return {제:줄.firstElementChild?.textContent, 제글자:getComputedStyle(줄.firstElementChild).fontSize, 높:Math.round(r.height), 테:cs.borderTopWidth, 테색:cs.borderTopColor===색('--강조'), 글자:getComputedStyle(b).fontSize, 굵:getComputedStyle(b).fontWeight,
          이름:b.textContent, 칩:줄.querySelector('.칩')?.textContent, 화살:줄.querySelector('.화살')?.textContent, 꽉:Math.abs(r.width-쌓.width)<2, 잘림:b.scrollWidth>b.clientWidth+1, 카드수:document.querySelectorAll('#폰 .넘김 .카드').length};})()""")
      봄(t+f"15lk 훈련 방식 = 카드 하나 · 이름표 '{방['제']}' {방['제글자']} · 누르는 줄 높이 {방['높']} · 테두리 {방['테']} --강조 · 폭 꽉", 방['제']=='훈련 방식' and 방['제글자']=='11px' and 방['높']==44 and 방['테']=='1px' and 방['테색'] and 방['꽉'] and 방['카드수']==2)
      봄(t+f"15lk 방식 이름 '{방['이름']}' {방['글자']} {방['굵']} · 요약 칩 '{방['칩']}' · '{방['화살']}' · 안 잘림", 방['이름']=='1. 꾸준히 늘리기' and 방['글자']=='15px' and int(방['굵'])>=700 and 방['칩']=='5×8~15' and 방['화살']=='›' and not 방['잘림'])
      await pg.click('.방식고름줄'); await pg.wait_for_timeout(400)
      봄(t+"15lk 누르면 훈련 방식 시트", await pg.evaluate("U.시트?.종류==='방식'"))
      await pg.click('.방식띠[data-n="4"]'); await pg.wait_for_timeout(300); await pg.click('.방식속 [data-act="방식고름"]'); await pg.wait_for_timeout(350)
      긴=await pg.evaluate("(()=>{const b=document.querySelector('.방식고름줄 b'); return {이름:b.textContent, 잘림:b.scrollWidth>b.clientWidth+1, 칩:!!document.querySelector('.방식고름줄 .칩')};})()")
      봄(t+f"15lk 다른 방식 '{긴['이름']}' 안 잘림 · 요약 칩 없음", not 긴['잘림'] and not 긴['칩'])
      # ── ksao 같은 종목 플랜 여럿 ──
      await 폼비움()
      async def 벤치만들기():
        await pg.fill('[data-in="폼"][data-f="현재무게"]','70'); await pg.fill('[data-in="폼"][data-f="현재횟수"]','8'); await pg.fill('[data-in="폼"][data-f="목표무게"]','110')
        await pg.click('[data-act="플랜만들기"]'); await pg.wait_for_timeout(450)
        return await pg.evaluate("({오류:U.플랜폼?.오류, 종류:U.시트?.종류, 제목:document.querySelector('.시트 .머리 b')?.textContent})")
      x=await 벤치만들기()
      봄(t+f"ksao 벤치프레스 플랜이 있어도 막히지 않음 · 제목 '{x['제목']}'", x['종류']=='플랜결과' and not x['오류'] and x['제목']=='벤치프레스 2 플랜' and '이미 있습니다' not in (x['오류'] or ''))
      # ── e8am 결과 시트 ──
      e=await pg.evaluate("""(()=>{const s=document.querySelector('.시트'), 블=[...s.querySelectorAll('.회블록')], cs=getComputedStyle(s);
        const 묶=getComputedStyle(s.querySelector('.결과묶음들')), b0=getComputedStyle(블[0]);
        return {요약:s.querySelector('.머리+div').textContent, 줄수:블.map(x=>x.children.length), 머:블.map(x=>x.querySelector('b').textContent), 알:블.map(x=>x.querySelector('.알약').textContent),
          처:블.map(x=>x.children[1].textContent), 처높:블.map(x=>Math.round(x.children[1].getBoundingClientRect().height)), 처글자:블.map(x=>parseFloat(getComputedStyle(x.children[1]).fontSize)),
          시트틈:cs.rowGap, 묶틈:묶.rowGap, 안여백:[b0.paddingTop,b0.paddingBottom], 안틈:b0.rowGap, 글:s.innerText,
          넘:[...s.querySelectorAll('*')].filter(q=>{const r=q.getBoundingClientRect(), p=s.getBoundingClientRect(); return r.width>0&&(r.right>p.right+1||r.left<p.left-1);}).length};})()""")
      봄(t+f"e8am 묶음마다 2줄 (머리 · 처방 한 줄) {e['줄수'][:4]}…", all(n==2 for n in e['줄수']))
      봄(t+f"e8am 처방 '처음 → 끝' 한 줄 ({e['처'][1]} · 높이 {e['처높'][:3]} · 글자 ≥ 11)", all('→' in x or '×' in x for x in e['처']) and '→' in e['처'][1] and all(h<=20 for h in e['처높']) and min(e['처글자'])>=11)
      봄(t+f"e8am 알약 'N회차에 근력 측정' {e['알'][:2]} · 마지막 '{e['알'][-1]}'", all(re.fullmatch(r'\d+회차에 근력 측정',a) for a in e['알'][:-1]) and e['알'][-1] in ('목표',) or re.fullmatch(r'\d+회차에 근력 측정',e['알'][-1]))
      봄(t+f"e8am · 9rvs 머리 'N–M회차' {e['머'][:2]} · 주 표시 없음 · 요약 '{e['요약']}'", all(re.fullmatch(r'\d+(–\d+)?회차',h) for h in e['머']) and not re.search(r'\d+주|주 \d+회|약 \d', e['글']))
      봄(t+f"e8am 줄 간격 절반: 시트 {e['시트틈']} · 묶음 사이 {e['묶틈']} · 묶음 안 여백 {e['안여백']} · 줄 사이 {e['안틈']}", e['시트틈']=='4px' and e['묶틈']=='4px' and e['안여백']==['4px','4px'] and e['안틈'] in ('0px','normal'))
      봄(t+f"e8am 시트 가로 넘침 없음 ({e['넘']})", e['넘']==0)
      await 사진('결과');
      if 폭==360: await 사진('360_결과')
      await pg.click('[data-act="플랜저장"]'); await pg.wait_for_timeout(400)
      await 폼비움(); x=await 벤치만들기(); 셋=x['제목']
      await pg.click('[data-act="플랜저장"]'); await pg.wait_for_timeout(400)
      이=await pg.evaluate("S.플랜들.filter(p=>p.종목==='벤치프레스').map(p=>p.이름)")
      봄(t+f"ksao 세 번째 '{셋}' · 벤치 플랜 {이}", 셋=='벤치프레스 3 플랜' and 이==['벤치프레스','벤치프레스 2','벤치프레스 3'])
      종=await pg.evaluate("""(()=>{U.탭='종목'; 그리기(); const 카=[...document.querySelectorAll('.플랜카드')]; return {수:카.length, 이름:카.map(c=>c.querySelector('b').textContent), 메타:카.map(c=>c.querySelectorAll('.맞춤')[1]?.textContent)};})()""")
      봄(t+f"ksao 종목 탭 플랜 카드 {종['수']}개 {종['이름']}", 종['수']==4 and 종['이름'][2:]==['벤치프레스 2','벤치프레스 3'])
      봄(t+f"9rvs 플랜 카드 메타 글에 '주 N회' 없음 ({종['메타'][0]})", all(m and not re.search(r'주 \d회',m) and '속도' in m for m in 종['메타']))
      await 사진('종목탭')
      # 루틴 넣기 · 운동 화면은 플랜 id 로 — 같은 종목 플랜 둘을 한 루틴에 넣어도 따로 처방
      루=await pg.evaluate("""(()=>{const [a,z]=S.플랜들.filter(p=>p.종목==='벤치프레스').slice(1); a.한회=0; U.루틴열림='r1'; U.방금=[]; 행동('플랜넣기',{v:a.id}); 행동('플랜넣기',{v:z.id});
        const r=루틴('r1'), 줄=r.종목.filter(e=>e.플랜id===a.id||e.플랜id===z.id); U.루틴열림=null; U.방금=[];
        return {줄:줄.map(e=>[e.이름,e.플랜id]), 다름:줄.length===2&&줄[0].플랜id!==줄[1].플랜id, 정식:줄.map(정식이름)};})()""")
      봄(t+f"ksao 루틴에 같은 종목 플랜 둘 → 플랜 id 로 따로 {루['줄']} · 정식이름 {루['정식']}", 루['다름'] and 루['정식']==['벤치프레스','벤치프레스'])
      await pg.evaluate("(()=>{U.탭='플랜'; 그리기();})()")
      n=await 넘침(); 봄(t+f"플랜 탭 마지막 가로 넘침 없음 ({n})", not n['문서'] and not n['넘'])
      봄(t+"JS 오류 없음", not 오류)
      if 오류: print(오류[:3])
      await pg.close()
    await b.close()
  [print(("✅ " if ok else "❌ ")+m) for m,ok in 결과]; print(f"\n{sum(ok for _,ok in 결과)}/{len(결과)}")
asyncio.run(main())
