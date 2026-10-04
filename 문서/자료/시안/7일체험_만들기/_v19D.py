import asyncio, sys, json, os
from playwright.async_api import async_playwright
SP='/tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'
HTML=sys.argv[1] if len(sys.argv)>1 else SP+'7day-v19D.html'
상태=json.load(open(SP+'marks3/28f858e336f21768f689d786826649de.json'))
os.makedirs(SP+'r19',exist_ok=True)
R="el=>{const r=el.getBoundingClientRect(); return [Math.round(r.left),Math.round(r.right),Math.round(r.top),Math.round(r.height)]}"
# 시트 위끝 · 머리 · 폰 20% 지점
잼 = """(()=>{ const 폰=document.getElementById('폰').getBoundingClientRect(), 시=document.querySelector('#폰 .시트'), 속=시&&시.querySelector('.새속');
  if(!시) return null; const r=시.getBoundingClientRect(), 머=시.querySelector('.머리').getBoundingClientRect();
  return {위:Math.round(r.top-폰.top), 목표:Math.round(폰.height*0.2), 머리위:Math.round(머.top-폰.top), 높이:Math.round(r.height),
    속:속?[속.scrollTop, 속.scrollHeight, 속.clientHeight]:null, 시트넘침:시.scrollHeight-시.clientHeight}; })()"""
누르면 = "(()=>{ const t=document.querySelector('#폰 .시트')?.innerText||''; return t.includes('누르면'); })()"
async def main():
  async with async_playwright() as p:
    b=await p.chromium.launch(); pg=await b.new_page(viewport={'width':389,'height':860}); err=[]; pg.on('pageerror',lambda e:err.append(str(e)))
    await pg.goto('file://'+HTML); await pg.wait_for_timeout(600)
    await pg.evaluate("s=>{ S=JSON.parse(JSON.stringify(s.S)); Object.assign(U,s.U,{시트:null,업적띠:null,스탯:null}); S.세션=null; S.결과=null; U.탭='종목'; U.종목펼침=null; U.종목칸고름='전체'; 그리기(); }", 상태); await pg.wait_for_timeout(400)
    폰=await pg.query_selector('#폰')
    # ── ① 처음 연 시트 ──
    await pg.click('#폰 .종목넘김 [data-act="새종목열기"]'); await pg.wait_for_timeout(400)
    print('① 처음', await pg.evaluate(잼), '누르면', await pg.evaluate(누르면), '칸', await pg.evaluate("U.새종목칸"))
    await pg.click('#폰 .새찾기칸'); await pg.keyboard.type('ㅂㅊ'); await pg.wait_for_timeout(250)
    print('① 검색 결과', await pg.evaluate(잼), await pg.evaluate("document.querySelectorAll('#폰 .새결과줄').length"), '누르면', await pg.evaluate(누르면))
    await 폰.screenshot(path=SP+'r19/D_시트찾기.png')
    await pg.fill('#폰 .새찾기칸','없는이름쿼리zz'); await pg.wait_for_timeout(200)
    print('⑤ 없음 글', await pg.evaluate("document.querySelector('#폰 .새찾기결과').innerText"), '누르면', await pg.evaluate(누르면))
    # ── 사전 종목 고르기 = 벤치프레스(이미 있는 이름) ──
    await pg.fill('#폰 .새찾기칸','ㅂㅊㅍ'); await pg.wait_for_timeout(200)
    await pg.click('#폰 .새결과줄[data-v="벤치프레스"]'); await pg.wait_for_timeout(300)
    print('② 벤치 고름(이미 있는 이름)', await pg.evaluate("[U.새종목, U.새종목칸, JSON.stringify(U.새.근육), U.새.고름, document.querySelector('#폰 .토스트')?.textContent||'']"))
    print('① 고른 뒤', await pg.evaluate(잼), '누르면', await pg.evaluate(누르면))
    print('④ 제목', await pg.evaluate("[...document.querySelectorAll('#폰 .시트 .새이름표')].map(x=>x.textContent)"), '그림', await pg.evaluate(f"[...document.querySelectorAll('#폰 .새몸칸 svg')].map(({R}))"))
    print('④ 칠한 조각', await pg.evaluate("[...document.querySelectorAll('#폰 .새몸칸 path.몸근')].filter(x=>!x.style.fill.includes('var(')).map(x=>x.dataset.v+':'+x.style.fill).join(' ')"))
    print('  단계색 P/S/Y', await pg.evaluate("[단계색(20),단계색(10),단계색(5)]"))
    # 칩으로 토글 (팔 · 보조 · 이두)
    await pg.click('#폰 .새묶음 [data-v="팔"]'); await pg.wait_for_timeout(100)
    await pg.click('#폰 .새역할줄 [data-v="S"]'); await pg.wait_for_timeout(100)
    await pg.click('#폰 .새근육들 [data-v="biceps"]'); await pg.wait_for_timeout(700)
    print('④ 칩 이두 보조 →', await pg.evaluate("[U.새.근육.biceps, [...new Set([...document.querySelectorAll('#폰 .새몸칸 path[data-v=biceps]')].map(x=>getComputedStyle(x).fill))]]"))
    # 그림 직접 누르기 — 뒤 그림 햄스트링(협응)
    await pg.click('#폰 .새역할줄 [data-v="Y"]'); await pg.wait_for_timeout(100)
    hb=await pg.evaluate(f"(()=>{{ const x=document.querySelectorAll('#폰 .새몸칸 path[data-v=hamstrings]')[0]; return ({R})(x); }})()")
    await pg.mouse.click((hb[0]+hb[1])/2, hb[2]+hb[3]/2); await pg.wait_for_timeout(700)
    print('④ 그림 햄스트링 누름 →', hb, await pg.evaluate("[U.새.근육.hamstrings, U.새.묶음, [...document.querySelectorAll('#폰 .새몸칸 path[data-v=hamstrings]')].map(x=>getComputedStyle(x).fill)[0], document.querySelector('#폰 .새요약').innerText.replace(/\\n/g,' / ')]"))
    await pg.mouse.click((hb[0]+hb[1])/2, hb[2]+hb[3]/2); await pg.wait_for_timeout(300)
    print('  한 번 더 →', await pg.evaluate("U.새.근육.hamstrings ?? '빠짐'"))
    print('① 칩 누른 뒤', await pg.evaluate(잼))
    # ⑥ 세트 줄
    print('⑥ 처음 세트', await pg.evaluate("[document.querySelectorAll('#폰 .새세팅 .종세트').length, JSON.stringify(U.새.세트), document.querySelector('#폰 .새세팅 .종지움').disabled]"))
    await pg.click('#폰 [data-act="새세트더"]'); await pg.wait_for_timeout(150); await pg.click('#폰 [data-act="새세트더"]'); await pg.wait_for_timeout(150)
    await pg.click('#폰 [data-act="새세트값"][data-k="0"][data-f="w"][data-d="1"]'); await pg.wait_for_timeout(100)
    await pg.click('#폰 [data-act="새세트값"][data-k="1"][data-f="휴"][data-d="1"]'); await pg.wait_for_timeout(100)
    i2=await pg.query_selector('#폰 input[data-in="새세트"][data-k="2"][data-f="r"]'); await i2.fill('7'); await i2.dispatch_event('change'); await pg.wait_for_timeout(200)
    print('⑥ 셋 + 값', await pg.evaluate("[document.querySelectorAll('#폰 .새세팅 .종세트').length, JSON.stringify(U.새.세트)]"), '① 자리', await pg.evaluate(잼))
    # 속 스크롤 → 다시 그려도 자리 그대로
    await pg.evaluate("document.querySelector('#폰 .새속').scrollTop=9999"); await pg.wait_for_timeout(150)
    앞=await pg.evaluate("document.querySelector('#폰 .새속').scrollTop")
    await pg.click('#폰 [data-act="새세트지움"][data-k="1"]'); await pg.wait_for_timeout(250)
    print('① 지운 뒤 스크롤', 앞, '→', await pg.evaluate("document.querySelector('#폰 .새속').scrollTop"), await pg.evaluate("JSON.stringify(U.새.세트)"), await pg.evaluate(잼))
    print('  칸 크기', await pg.evaluate("""(()=>{ const z=document.querySelector('#폰 .새세팅 .종세트'); return [...z.children].map(x=>Math.round(x.getBoundingClientRect().width)+'x'+Math.round(x.getBoundingClientRect().height)).join(' ') })()"""))
    await 폰.screenshot(path=SP+'r19/D_시트세트.png')
    await pg.evaluate("document.querySelector('#폰 .새속').scrollTop=0"); await pg.wait_for_timeout(150)
    await 폰.screenshot(path=SP+'r19/D_시트부위.png')
    # 저장 → 같은 이름 둘
    await pg.click('#폰 .새저장'); await pg.wait_for_timeout(300)
    print('② 저장', await pg.evaluate("[U.시트, S.종목표.filter(t=>t.이름==='벤치프레스').map(t=>(t.id||'(id 없음)')+' '+t.칸+' '+JSON.stringify(t.근육||null)).join(' | '), JSON.stringify(S.종목설정[S.종목표.at(-1).id])]"))
    # ③ 사전에 없는 이름 — 카테고리 비활성 · 저장 막기
    await pg.click('#폰 .종목넘김 [data-act="새종목열기"]'); await pg.wait_for_timeout(300)
    await pg.click('#폰 .새찾기칸'); await pg.keyboard.type('나만의 버티기'); await pg.keyboard.press('Enter'); await pg.wait_for_timeout(300)
    print('③ 사전에 없음', await pg.evaluate("[U.새종목칸, document.querySelector('#폰 .새칸줄').className, document.querySelectorAll('#폰 .새칸줄 .칩.켬').length, getComputedStyle(document.querySelector('#폰 .새칸줄 .칩')).borderStyle]"))
    await pg.click('#폰 .새묶음 [data-v="복근"]'); await pg.click('#폰 .새근육들 [data-v="abs"]'); await pg.wait_for_timeout(150)
    await pg.click('#폰 .새저장'); await pg.wait_for_timeout(400)
    print('③ 칸 없이 저장', await pg.evaluate("[U.시트?.종류, document.querySelector('#폰 .토스트')?.textContent, S.종목표.some(t=>t.이름==='나만의 버티기')]"))
    await pg.wait_for_timeout(600)
    print('  0.6초 뒤 토스트', await pg.evaluate("document.querySelector('#폰 .토스트')?.textContent"))
    await pg.click('#폰 .새칸줄 [data-v="맨몸"]'); await pg.wait_for_timeout(150)
    print('  고른 뒤', await pg.evaluate("[U.새종목칸, document.querySelector('#폰 .새칸줄').className]"))
    await pg.click('#폰 .새저장'); await pg.wait_for_timeout(300)
    print('  저장', await pg.evaluate("JSON.stringify(S.종목표.at(-1))"))
    # 같은 이름 셋째 — 넣기 시트에서 (돌아감)
    await pg.evaluate("U.루틴열림=S.루틴들[0].id; U.탭='루틴'; U.시트={종류:'종목넣기'}; U.칸고름='가슴'; 그리기()"); await pg.wait_for_timeout(300)
    await pg.click('#폰 .시트 [data-act="새종목열기"]'); await pg.wait_for_timeout(300)
    print('① 넣기에서 연 시트', await pg.evaluate(잼))
    await pg.click('#폰 .새찾기칸'); await pg.keyboard.type('벤치프레스'); await pg.keyboard.press('Enter'); await pg.wait_for_timeout(300)
    await pg.click('#폰 .새저장'); await pg.wait_for_timeout(300)
    print('② 셋째 저장 →', await pg.evaluate("[U.시트?.종류, S.종목표.filter(t=>t.이름==='벤치프레스').length]"))
    print('② 넣기 목록 번호', await pg.evaluate("[...document.querySelectorAll('#폰 .넣기칸')].filter(x=>x.querySelector('b').textContent==='벤치프레스').map(x=>[x.dataset.v, x.querySelector('.번호표')?.textContent||'-'])"))
    # 둘째 · 셋째를 넣는다
    ids=await pg.evaluate("S.종목표.filter(t=>t.이름==='벤치프레스'&&t.id).map(t=>t.id)")
    await pg.click(f'#폰 .넣기칸[data-v="{ids[0]}"]'); await pg.wait_for_timeout(200); await pg.click(f'#폰 .넣기칸[data-v="{ids[1]}"]'); await pg.wait_for_timeout(200); await pg.click(f'#폰 .넣기칸[data-v="{ids[1]}"]'); await pg.wait_for_timeout(200)
    print('② 넣은 뒤 체크', await pg.evaluate("[...document.querySelectorAll('#폰 .넣기칸')].filter(x=>x.querySelector('b').textContent==='벤치프레스').map(x=>(x.querySelector('.번호표')?.textContent||'-')+':'+x.querySelector('.넣기체크').textContent)"))
    print('  루틴 줄', await pg.evaluate("JSON.stringify(S.루틴들[0].종목.slice(-3).map(e=>({이름:e.이름,종id:e.종id,세트:e.세트.length,w:e.세트[0].w})))"))
    await 폰.screenshot(path=SP+'r19/D_넣기번호.png')
    # 번호 딱지 자리 (이름 b 와)
    print('② 딱지 자리(넣기)', await pg.evaluate("""(()=>{ const x=[...document.querySelectorAll('#폰 .넣기칸 .번호표')][1]; const b=x.previousElementSibling.getBoundingClientRect(), r=x.getBoundingClientRect(), cs=getComputedStyle(x);
      return {위차:Math.round(r.top-b.top), 겹침:Math.round(b.right-r.left), 크기:[Math.round(r.width),Math.round(r.height)], 바탕:cs.backgroundColor, 글:cs.color, 글크기:cs.fontSize, 굵기:cs.fontWeight, 둥글기:cs.borderRadius}; })()"""))
    # 루틴 화면 이름 줄
    await pg.evaluate("U.시트=null; 그리기()"); await pg.wait_for_timeout(300)
    print('② 루틴 상자', await pg.evaluate("[...document.querySelectorAll('#폰 .루접줄')].map(x=>x.querySelector('b').textContent+(x.querySelector('.번호표')?'#'+x.querySelector('.번호표').textContent:'')).join(', ')"))
    # 종목 탭
    await pg.evaluate("U.탭='종목'; U.종목칸고름='가슴'; 그리기()"); await pg.wait_for_timeout(300)
    print('② 종목 탭', await pg.evaluate("[...document.querySelectorAll('#폰 .종목칸')].map(x=>x.querySelector('.종목칸이름 b').textContent+(x.querySelector('.번호표')?'#'+x.querySelector('.번호표').textContent:'')+(x.querySelector('.플랜표:not(.번호표)')?'[플랜]':'')).join(', ')"))
    print('  딱지 잘림', await pg.evaluate("""(()=>{ const x=document.querySelector('#폰 .종목칸 .번호표'), c=x.closest('.종목줄이름'); const a=x.getBoundingClientRect(), k=c.getBoundingClientRect(); return {딱지위:Math.round(a.top), 칸위:Math.round(k.top), 잘림:a.top<k.top-0.5}; })()"""))
    await pg.click(f'#폰 .종목칸이름[data-v="{ids[0]}"]'); await pg.wait_for_timeout(300)
    print('② 둘째 펼침 · 기본 세팅', await pg.evaluate("[U.종목펼침, [...document.querySelectorAll('#폰 .종목칸.펼 .종세트')].length, [...document.querySelectorAll('#폰 .종목칸.펼 .근줄')].map(x=>x.textContent).join(' / ')]"))
    await 폰.screenshot(path=SP+'r19/D_종목탭번호.png')
    print('② 근육 따로', await pg.evaluate(f"[JSON.stringify(종목근육('벤치프레스')), JSON.stringify(종목근육({json.dumps(ids[0])}))]"))
    # 운동 — 루틴 시작
    await pg.evaluate("S.세션=null; 운동시작(S.루틴들[0].id); const k=S.세션.종목.findIndex(e=>e.종id); U.본=k; S.세션.지금.i=k; 그리기()"); await pg.wait_for_timeout(400)
    print('② 운동 머리', await pg.evaluate("[document.querySelector('#폰 .운이름')?.textContent, getComputedStyle(document.querySelector('#폰 .운번호표')||document.body).backgroundColor, S.세션.종목.filter(e=>e.종id).map(e=>e.종id).join(',')]"))
    print('  잎볼륨 둘째(이두 보조 · 햄 없음)', await pg.evaluate("(()=>{ const e=S.세션.종목.find(e=>e.종id); const v=잎볼륨([{...e, 세트:[{w:100,r:10,완료:true}]}]); return Object.keys(v).filter(k=>k.startsWith('biceps')).map(k=>k+':'+v[k]).join(' ')||'없음'; })()"))
    await 폰.screenshot(path=SP+'r19/D_운동번호.png')
    print('  기록 종id', await pg.evaluate("JSON.stringify(세션기록(S.세션).종목.filter(e=>e.종id).map(e=>e.종id))"))
    print('오류', err)
    await b.close()
asyncio.run(main())
