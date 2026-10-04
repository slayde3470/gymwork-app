import asyncio, sys, json, os, base64
from playwright.async_api import async_playwright
SP='/tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'
HTML=sys.argv[1] if len(sys.argv)>1 else SP+'7day-v18C.html'
상태=json.load(open(SP+'marks3/28f858e336f21768f689d786826649de.json'))
os.makedirs(SP+'r18',exist_ok=True)
PNG=SP+'r18/_t.png'
open(PNG,'wb').write(base64.b64decode('iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAIAAACQkWg2AAAAHUlEQVR4nGP8z0AaYBzVMKphVMOohlENoxqGqgYAH5gBIQF4ZW0AAAAASUVORK5CYII='))
R="el=>{const r=el.getBoundingClientRect(); return [Math.round(r.left),Math.round(r.right),Math.round(r.top),Math.round(r.height)]}"
async def main():
  async with async_playwright() as p:
    b=await p.chromium.launch(); pg=await b.new_page(viewport={'width':389,'height':860}); err=[]; pg.on('pageerror',lambda e:err.append(str(e)))
    await pg.goto('file://'+HTML); await pg.wait_for_timeout(600)
    await pg.evaluate("s=>{ S=JSON.parse(JSON.stringify(s.S)); Object.assign(U,s.U,{시트:null,업적띠:null,스탯:null}); S.세션=null; S.결과=null; U.탭='종목'; U.종목펼침=null; U.종목칸고름='전체'; 그리기(); }", 상태); await pg.wait_for_timeout(400)
    폰=await pg.query_selector('#폰')
    # ① 띠
    print('① 띠', await pg.evaluate(f"""(()=>{{ const t=document.querySelector('#폰 .종목띠'), b=t.querySelector('b'), cs=getComputedStyle(t);
      return {{자리:({R})(t), 글가운데:getComputedStyle(b).textAlign, 바탕:cs.backgroundColor, 강조:getComputedStyle(document.documentElement).getPropertyValue('--강조').trim(), 글크기:getComputedStyle(b).fontSize, h1:document.querySelectorAll('#폰 h1').length,
        글중심:Math.round((b.getBoundingClientRect().left+b.getBoundingClientRect().right)/2), 띠중심:Math.round((t.getBoundingClientRect().left+t.getBoundingClientRect().right)/2)}} }})()"""))
    # ② 격자
    g=await pg.evaluate(f"""(()=>{{ const c=[...document.querySelectorAll('#폰 .종목칸')];
      return {{상자:c.length, 왼쪽들:[...new Set(c.map(x=>Math.round(x.getBoundingClientRect().left)))], 폭:[...new Set(c.map(x=>Math.round(x.getBoundingClientRect().width)))], 높이:[...new Set(c.map(x=>Math.round(x.getBoundingClientRect().height)))],
        이름표:[...document.querySelectorAll('#폰 .종목칸표')].map(x=>x.textContent), 묶음상자:document.querySelectorAll('#폰 .종목넘김 .카드.쌓기').length,
        근줄:[...c[0].querySelectorAll('.근줄')].map(x=>[x.textContent, getComputedStyle(x).fontSize, x.scrollWidth>x.clientWidth?'…':'']),
        넘침:document.querySelector('#폰 .종목넘김').scrollWidth-document.querySelector('#폰 .종목넘김').clientWidth,
        칩:[...document.querySelectorAll('#폰 .종목칩 .칩')].map(x=>x.textContent+':'+Math.round(x.getBoundingClientRect().height)).join(' ')}} }})()""")
    print('② 격자', g)
    await 폰.screenshot(path=SP+'r18/C_탭.png')
    # 칩 필터
    await pg.click('#폰 .종목칩 [data-v="등"]'); await pg.wait_for_timeout(200)
    print('  칩 등 →', await pg.evaluate("[...document.querySelectorAll('#폰 .종목칸 .종목칸이름 b')].map(x=>x.textContent)"), '이름표', await pg.evaluate("document.querySelectorAll('#폰 .종목칸표').length"))
    await pg.click('#폰 .종목칩 [data-v="전체"]'); await pg.wait_for_timeout(200)
    # 오른쪽 열 상자 펼침
    이름=await pg.evaluate("(()=>{ const c=[...document.querySelectorAll('#폰 .종목칸')]; const L=Math.min(...c.map(x=>x.getBoundingClientRect().left)); return c.find(x=>x.getBoundingClientRect().left>L+20).querySelector('[data-act=종목펼침]').dataset.v; })()")
    await pg.click(f'#폰 .종목칸이름[data-v="{이름}"]'); await pg.wait_for_timeout(400)
    print('② 펼침', 이름, await pg.evaluate(f"""(()=>{{ const c=document.querySelector('#폰 .종목칸.펼'), 격=document.querySelector('#폰 .종목격자'); return {{상자:({R})(c), 격자:({R})(격), 사진추가단추:c.querySelectorAll('.사진추가').length, 세트줄:c.querySelectorAll('.종세트').length, 설명:c.querySelector('.종설제목').textContent}} }})()"""))
    # ③ 사진 칸 = file input
    inp=await pg.query_selector(f'#폰 .종목칸.펼 label.사진넣칸 input[type=file]')
    print('③ 사진 칸 input', inp is not None)
    await inp.set_input_files(PNG); await pg.wait_for_timeout(600)
    print('  넣은 뒤', await pg.evaluate(f"""(()=>{{ const c=document.querySelector('#폰 .종목칸.펼'); return {{칸사진:!!c.querySelector('label.사진넣칸 img'), 사진줄:c.querySelectorAll('.사진줄 .사진칸').length, 칸:({R})(c.querySelector('label.사진넣칸'))}} }})()"""))
    # ④ 기본 세팅 세트 줄
    펼=f'#폰 .종목칸.펼'
    print('④ 처음', await pg.evaluate(f"(()=>{{ const c=document.querySelector('{펼}'); return {{줄:c.querySelectorAll('.종세트').length, 휴지통막힘:c.querySelector('.종지움').disabled}} }})()"))
    await pg.click(f'{펼} [data-act="종세트값"][data-k="0"][data-f="w"][data-d="1"]'); await pg.click(f'{펼} [data-act="종세트값"][data-k="0"][data-f="휴"][data-d="1"]')
    await pg.click(f'{펼} [data-act="종세트더"]'); await pg.wait_for_timeout(100); await pg.click(f'{펼} [data-act="종세트더"]'); await pg.wait_for_timeout(100)
    await pg.click(f'{펼} [data-act="종세트값"][data-k="2"][data-f="r"][data-d="-1"]')
    i2=await pg.query_selector(f'{펼} input[data-in="종세트"][data-k="1"][data-f="w"]'); await i2.fill('102.5'); await i2.dispatch_event('change'); await pg.wait_for_timeout(200)
    print('  누른 뒤', await pg.evaluate(f"JSON.stringify(S.종목설정[{json.dumps(이름)}])"), '휴지통막힘', await pg.evaluate(f"[...document.querySelectorAll('{펼} .종지움')].map(x=>x.disabled)"))
    await pg.click(f'{펼} [data-act="종세트지움"][data-k="0"]'); await pg.wait_for_timeout(100)
    print('  1줄 지움', await pg.evaluate(f"JSON.stringify(S.종목설정[{json.dumps(이름)}].세트)"))
    print('  칸 크기', await pg.evaluate(f"""(()=>{{ const z=document.querySelector('{펼} .종세트'); return [...z.children].map(x=>Math.round(x.getBoundingClientRect().width)+'x'+Math.round(x.getBoundingClientRect().height)).join(' ')+' / 넘침 '+[...z.querySelectorAll('input,span')].filter(e=>e.scrollWidth>e.clientWidth+1).length }})()"""))
    await pg.evaluate(f"document.querySelector('{펼}').scrollIntoView({{block:'start'}})"); await pg.wait_for_timeout(150)
    await 폰.screenshot(path=SP+'r18/C_펼침.png')
    # 옛 꼴 옮김 + 종목넣기
    print('  옛 꼴', await pg.evaluate("S.종목설정['벤치프레스']={세트:3,w:60,r:8,휴:90}; JSON.stringify(종목기본세트('벤치프레스'))"))
    print('  운동에 넣기', await pg.evaluate("""(()=>{ const 앞=S.세션; S.세션={이름:'시험',종목:[],시작:0}; U.시트={종류:'종목넣기',대상:'운동'}; U.방금=[]; 행동('종목넣기',{v:'벤치프레스'}); const r=JSON.stringify(S.세션.종목.at(-1)); S.세션=앞; U.시트=null; return r; })()"""))
    print('  루틴에 넣기', await pg.evaluate(f"""(()=>{{ U.루틴열림=S.루틴들[0].id; U.시트={{종류:'종목넣기'}}; U.방금=[]; 행동('종목넣기',{{v:{json.dumps(이름)}}}); const r=JSON.stringify(S.루틴들[0].종목.at(-1)); U.시트=null; U.루틴열림=null; return r; }})()"""))
    # ⑤ 사전 · 초성
    print('⑤ 사전', await pg.evaluate("[종목사전.length, Object.fromEntries(S.카테고리.map(c=>[c,종목사전.filter(x=>x.칸===c).length])), 종목사전.filter(x=>!S.카테고리.includes(x.칸)).length, new Set(종목사전.map(x=>x.이름)).size]"))
    for q in ['ㅂㅊㅍ','벤ㅊ','프레스','벤치플','ohp','ㄹㅁㄴ','ㅅㄹㄹ','풀다운','ㅋㄹ']:
      print('  찾기', q, await pg.evaluate(f"종목찾기({json.dumps(q)}).map(x=>x.이름).join(', ')"))
    print('  세부 키', await pg.evaluate("세부부위.map(([g,l])=>g+':'+l.map(k=>k+'('+근이름(k)+')').join(' ')).join(' | ')"))
    print('  지도에 없는 키', await pg.evaluate("[...세부키].filter(k=>!조각잎.some(l=>잎(k).every(x=>l.includes(x))))"))
    print('  사전 근육 · 빈 것', await pg.evaluate("종목사전.filter(x=>!Object.values(사전근육(x)).includes('P')).map(x=>x.이름)"), '세부 키 밖', await pg.evaluate("종목사전.flatMap(x=>Object.keys(사전근육(x))).filter(k=>!세부키.has(k))"))
    # 시트 — 종목 탭에서
    await pg.evaluate("U.종목펼침=null; 그리기()"); await pg.click('#폰 [data-act="새종목열기"]'); await pg.wait_for_timeout(400)
    print('⑤ 시트 처음', await pg.evaluate(f"""(()=>{{ const 단=document.querySelector('#폰 .새찾기단추'); return {{종류:U.시트.종류, 돌아감:U.시트.돌아감, 단추:단.getAttribute('aria-label'), 단추자리:({R})(단), 칸줄:({R})(document.querySelector('#폰 .새찾기줄')), 머리:document.querySelector('#폰 .시트 .머리').textContent.trim(), 고르기:!!document.querySelector('#폰 .새근육들')}} }})()"""))
    await pg.click('#폰 .새찾기단추'); await pg.wait_for_timeout(200)
    print('  돋보기 누름', await pg.evaluate("[U.새.찾는중, document.querySelector('#폰 .새찾기단추').textContent, document.activeElement?.dataset?.in]"))
    await pg.keyboard.type('ㄹㅁㄴ'); await pg.wait_for_timeout(200)
    print('  ㄹㅁㄴ 침', await pg.evaluate("[document.activeElement?.dataset?.in, [...document.querySelectorAll('#폰 .새결과줄 b')].map(x=>x.textContent)]"))
    await pg.fill('#폰 .새찾기칸','ㄷㄷ'); await pg.wait_for_timeout(200)
    print('  ㄷㄷ', await pg.evaluate("[...document.querySelectorAll('#폰 .새결과줄')].map(x=>x.innerText.replace(/\\n/g,' '))"))
    await pg.fill('#폰 .새찾기칸','ㄹㅁㄴ'); await pg.wait_for_timeout(200)
    await 폰.screenshot(path=SP+'r18/C_시트찾기.png')
    await pg.click('#폰 .새결과줄[data-v="루마니안 데드리프트"]'); await pg.wait_for_timeout(300)
    print('  고름', await pg.evaluate("[U.새종목, U.새종목칸, JSON.stringify(U.새.근육), U.새.묶음, document.querySelector('#폰 .새찾기단추').getAttribute('aria-label'), document.querySelector('#폰 .새요약').innerText.replace(/\\n/g,' / ')]"))
    await pg.click('#폰 .새역할줄 [data-v="Y"]'); await pg.wait_for_timeout(100); await pg.click('#폰 .새근육들 [data-v="calves"]'); await pg.wait_for_timeout(100)
    await pg.click('#폰 .새역할줄 [data-v="S"]'); await pg.wait_for_timeout(100); await pg.click('#폰 .새근육들 [data-v="lower_back"]') if await pg.query_selector('#폰 .새근육들 [data-v="lower_back"]') else None
    await pg.click('#폰 .새묶음 [data-v="등"]'); await pg.wait_for_timeout(100); await pg.click('#폰 .새근육들 [data-v="lower_back"]'); await pg.wait_for_timeout(100)
    print('  바꿈 (종아리 협응 · 등 묶음에서 척추기립근 보조 다시 = 뺌)', await pg.evaluate("[JSON.stringify(U.새.근육), [...document.querySelectorAll('#폰 .새묶음 .칩')].map(x=>x.textContent).join(' '), document.querySelector('#폰 .새요약').innerText.replace(/\\n/g,' / ')]"))
    print('  칩 높이', await pg.evaluate("[...new Set([...document.querySelectorAll('#폰 .시트 .칩, #폰 .새저장, #폰 .새찾기줄')].map(x=>Math.round(x.getBoundingClientRect().height)))]"), '시트 넘침', await pg.evaluate("(()=>{const t=document.querySelector('#폰 .시트'); return [t.scrollWidth-t.clientWidth, t.scrollHeight, t.clientHeight]})()"))
    await 폰.screenshot(path=SP+'r18/C_시트부위.png')
    await pg.click('#폰 .새저장'); await pg.wait_for_timeout(300)
    print('  저장', await pg.evaluate("[U.시트, JSON.stringify(S.종목표.at(-1)), JSON.stringify(종목근육('루마니안 데드리프트'))]"))
    print('  피로 지도 잎볼륨', await pg.evaluate("Object.entries(잎볼륨([{이름:'루마니안 데드리프트',세트:[{w:100,r:10,완료:true}]}])).map(([k,v])=>k+':'+v).join(' ')"))
    print('  탭 상자', await pg.evaluate("(()=>{ const c=[...document.querySelectorAll('#폰 .종목칸')].find(x=>x.querySelector('[data-v=\"루마니안 데드리프트\"]')); return c&&[...c.querySelectorAll('.근줄')].map(x=>x.textContent); })()"))
    # 직접 만든 이름
    await pg.click('#폰 [data-act="새종목열기"]'); await pg.wait_for_timeout(300)
    await pg.click('#폰 .새찾기칸'); await pg.keyboard.type('나만의 버티기'); await pg.wait_for_timeout(150)
    print('  직접 · 침', await pg.evaluate("[U.새.찾는중, document.querySelector('#폰 .새찾기단추').textContent, document.querySelector('#폰 .새찾기결과').innerText]"))
    await pg.keyboard.press('Enter'); await pg.wait_for_timeout(300)
    print('  Enter', await pg.evaluate("[U.새.고름, U.새종목, JSON.stringify(U.새.근육), U.새.묶음]"))
    await pg.click('#폰 .새저장'); await pg.wait_for_timeout(200)
    print('  주동 없이 저장', await pg.evaluate("[U.시트?.종류, document.querySelector('#폰 .토스트')?.textContent]"))
    await pg.click('#폰 .새묶음 [data-v="복근"]'); await pg.click('#폰 .새근육들 [data-v="abs"]'); await pg.click('#폰 .새저장'); await pg.wait_for_timeout(300)
    print('  저장', await pg.evaluate("[JSON.stringify(S.종목표.at(-1)), document.querySelector('#폰 .토스트')?.textContent]"))
    # 돌아감 — 종목 넣기 시트에서
    await pg.evaluate("U.루틴열림=S.루틴들[0].id; U.탭='루틴'; U.시트={종류:'종목넣기'}; U.칸고름='가슴'; 그리기()"); await pg.wait_for_timeout(300)
    await pg.evaluate("행동('새종목열기',{})"); await pg.wait_for_timeout(300)
    print('⑤ 돌아감', await pg.evaluate("[U.시트.종류, U.시트.돌아감?.종류, document.querySelector('#폰 .시트 .머리 .닫기').textContent]"))
    await pg.click('#폰 .시트 .머리 .닫기'); await pg.wait_for_timeout(300)
    print('  닫기 →', await pg.evaluate("U.시트?.종류"))
    await pg.evaluate("행동('새종목열기',{})"); await pg.wait_for_timeout(200)
    await pg.click('#폰 .새찾기칸'); await pg.keyboard.type('ㅍㅇㅅ'); await pg.wait_for_timeout(150)
    print('  ㅍㅇㅅ', await pg.evaluate("[...document.querySelectorAll('#폰 .새결과줄 b')].map(x=>x.textContent)"))
    await pg.click('#폰 .새결과줄[data-v="페이스 풀"]'); await pg.wait_for_timeout(200); await pg.click('#폰 .새저장'); await pg.wait_for_timeout(300)
    print('  저장 →', await pg.evaluate("[U.시트?.종류, U.칸고름, [...document.querySelectorAll('#폰 .넣기줄 b')].map(x=>x.textContent).join(',')]"))
    # 어둡게
    await pg.evaluate("U.시트=null; U.탭='종목'; U.종목펼침='벤치프레스'; document.documentElement.dataset.theme='dark'; 그리기()"); await pg.wait_for_timeout(300)
    await 폰.screenshot(path=SP+'r18/C_어둠.png')
    print('오류', err)
    await b.close()
asyncio.run(main())
