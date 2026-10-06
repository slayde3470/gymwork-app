"""v22 A 확인 — 루틴 상세(① 「루틴」 띠 + [‹ 뒤로][이름][취소] · ② 취소 두 번 = 지움 · ③ 떠 있는 자동 · ④ 2열 상자 · ⑤ 휴지통)
   · 종목 넣기 시트(⑥ [확인] · ⑦ 연필 → 편집 → 넣기 시트로 돌아옴)
쓰는 법: python3 _v22A.py HTML 사진폴더   (처음 화면에서 직접 눌러 들어간다 · 389×860 · 360×800)"""
import asyncio, sys, json, os
from playwright.async_api import async_playwright
HTML=os.path.abspath(sys.argv[1]); 사진=os.path.abspath(sys.argv[2]) if len(sys.argv)>2 else os.path.dirname(HTML)+'/r22A'
os.makedirs(사진, exist_ok=True)
결과={'통과':0,'실패':[]}
def 봄(이름, 조건, 값=None):
  if 조건: 결과['통과']+=1
  else: 결과['실패'].append(f"{이름} → {값}")
  print(('  ✓ ' if 조건 else '  ✗ ')+이름+('' if 값 is None else f"  {json.dumps(값,ensure_ascii=False)[:260]}"))
도구 = """window.ㅁ = sel=>{ const e=typeof sel==='string'?document.querySelector(sel):sel; if(!e) return null; const r=e.getBoundingClientRect(); return {l:+r.left.toFixed(1),t:+r.top.toFixed(1),r:+r.right.toFixed(1),b:+r.bottom.toFixed(1),w:+r.width.toFixed(1),h:+r.height.toFixed(1)}; };
window.끝냄 = ()=>document.getAnimations().forEach(a=>{ try{ if(isFinite(a.effect.getComputedTiming().endTime)) a.finish(); }catch(_){} });"""
async def 찍기(pg, 이름):
  await pg.evaluate("끝냄()"); await pg.wait_for_timeout(80); await (await pg.query_selector('#폰')).screenshot(path=f'{사진}/{이름}')
async def 누름(pg, sel, 기다림=300):
  await pg.click(sel); await pg.wait_for_timeout(기다림); await pg.evaluate(도구)

async def 한폭(b, W, H):
  print(f'\n════ {W}×{H} ════')
  pg=await b.new_page(viewport={'width':W,'height':H}); err=[]; pg.on('pageerror',lambda e:err.append(str(e)))
  await pg.goto('file://'+HTML); await pg.wait_for_timeout(600); await pg.evaluate(도구)
  await 누름(pg, '#폰 .탭줄 button[data-t="루틴"]')
  # ── ① 루틴 열기 ──
  await 누름(pg, '#폰 .루틴카드[data-i="0"]')
  x=await pg.evaluate("""(()=>{ const 화=document.querySelector('#폰 .화면'), 첫=화.children[0], 둘=화.children[1], 폰=ㅁ('#폰');
    return {첫:첫.className, 첫글:첫.textContent.trim(), 첫위:ㅁ(첫).t-폰.t, 첫높:ㅁ(첫).h, 둘:둘.className, 둘위:ㅁ(둘).t-ㅁ(첫).b,
      단:[...둘.children].map(c=>c.tagName+':'+c.textContent.trim().split(/\\s+/)[0]), 자동띠:/자동/.test(둘.textContent), 둘높:ㅁ(둘).h}; })()""")
  봄(f'{W} ① 루틴 열기 → 맨 위 「루틴」 띠(.띠 가운데띠 루틴목록띠) 그대로 · 폰 맨 위', '루틴목록띠' in x['첫'] and x['첫글']=='루틴' and abs(x['첫위']-1)<=1.5, x)
  봄(f'{W} ① 그 바로 아래 줄 = [‹ 뒤로][이름 칸][취소] · 자동 없음', '루띠' in x['둘'] and abs(x['둘위'])<=0.5 and x['단'][0]=='BUTTON:‹' and x['단'][1].startswith('LABEL') and x['단'][2].startswith('BUTTON:취소') and not x['자동띠'], x)
  y=await pg.evaluate("document.querySelector('#폰 .루띠 [data-act=\"루틴닫기\"]').textContent.trim()")
  봄(f'{W} ① 뒤로 단추 글 = "‹ 뒤로"', y=='‹ 뒤로', y)
  await 찍기(pg, f'1_루틴상세_{W}.png')
  # +루틴 로도 같은 꼴
  await 누름(pg, '#폰 .루띠 [data-act="루틴닫기"]')
  봄(f'{W} ① [‹ 뒤로] → 루틴 목록', await pg.evaluate("!!document.querySelector('#폰 .루틴카드') && U.루틴열림==null"))
  await 누름(pg, '#폰 [data-act="루틴추가"]')
  x=await pg.evaluate("({첫:document.querySelector('#폰 .화면').children[0].className, 둘:document.querySelector('#폰 .화면').children[1].className, 이름:document.querySelector('#폰 .루이름칸 input').value, 빈:!!document.querySelector('#폰 .루넘김 .빈칸')})")
  봄(f'{W} ① [+ 루틴] → 「루틴」 띠 + 이름 줄 (새 루틴 · 빈칸)', '루틴목록띠' in x['첫'] and '루띠' in x['둘'] and x['이름'].startswith('새 루틴') and x['빈'], x)
  # ── ② 취소 두 번 = 지움 ──
  봄(f'{W} ② 「이 루틴 지우기」 큰 단추 없음', await pg.evaluate("![...document.querySelectorAll('#폰 button')].some(b=>/이 루틴 지우기/.test(b.textContent))"))
  n0=await pg.evaluate("S.루틴들.length")
  w0=await pg.evaluate("[ㅁ('#폰 .루취소').w, ㅁ('#폰 .루이름칸').w]")
  await 누름(pg, '#폰 .루취소')
  x=await pg.evaluate("""(()=>{ const b=document.querySelector('#폰 .루취소'), sp=[...b.children].map(s=>getComputedStyle(s).visibility);
    return {n:S.루틴들.length, 확:b.classList.contains('확인중'), 보임:[...b.children].filter((s,i)=>sp[i]==='visible').map(s=>s.textContent), 색:getComputedStyle(b).color, 폭:[ㅁ(b).w, ㅁ('#폰 .루이름칸').w], 열림:U.루틴열림!=null}; })()""")
  봄(f'{W} ② [취소] 한 번 → 안 지워짐 · "한 번 더" · --나쁨 글씨 · 폭 그대로', x['n']==n0 and x['확'] and x['보임']==['한 번 더'] and x['색']=='rgb(179, 38, 30)' and x['폭']==w0 and x['열림'], [x, w0])
  await 찍기(pg, f'2_취소한번_{W}.png')
  await 누름(pg, '#폰 .루취소')
  x=await pg.evaluate("({n:S.루틴들.length, 열림:U.루틴열림, 목록:!!document.querySelector('#폰 .루틴카드')})")
  봄(f'{W} ② [취소] 두 번 → 그 루틴 지움 · 목록으로', x['n']==n0-1 and x['열림'] is None and x['목록'], x)
  # 한 번 누르고 다른 것을 누르면 풀림
  await 누름(pg, '#폰 .루틴카드[data-i="1"]'); await 누름(pg, '#폰 .루취소'); await 누름(pg, '#폰 .루자동뜸 .스위치'); await 누름(pg, '#폰 .루자동뜸 .스위치')
  x=await pg.evaluate("({확:U.확인, 클:document.querySelector('#폰 .루취소').classList.contains('확인중')})")
  봄(f'{W} ② 한 번 누른 뒤 다른 단추(자동)를 누르면 처음 상태', not x['클'], x)
  # ── ③ 떠 있는 자동 ──
  await pg.evaluate("행동('탭',{t:'루틴'}); 행동('루틴열기',{v:S.루틴들[0].id})"); await pg.wait_for_timeout(300); await pg.evaluate(도구)
  x=await pg.evaluate("""(()=>{ const a=ㅁ('#폰 .루자동뜸'), 탭=ㅁ('#폰 .탭줄'), 폰=ㅁ('#폰'), cs=getComputedStyle(document.querySelector('#폰 .루자동뜸'));
    return {a, 탭t:탭.t, 폰r:폰.r, 위치:cs.position, 글:document.querySelector('#폰 .루자동뜸').textContent.trim(), 스:!!document.querySelector('#폰 .루자동뜸 .스위치')}; })()""")
  봄(f'{W} ③ 자동 = 오른쪽 아래에 떠 있음(absolute) · 탭줄 위 12 · 폰 오른쪽 안 12(±1.5)', x['위치']=='absolute' and abs(x['탭t']-x['a']['b']-12)<=1.5 and abs(x['폰r']-x['a']['r']-13)<=1.5 and x['글']=='자동' and x['스'] and x['a']['h']==40, x)
  # 종목을 많이 넣어 스크롤 → 제자리 · 끝이 안 가림
  await pg.evaluate("(()=>{ const r=루틴(U.루틴열림), l=r.종목.slice(1); for(let j=0;j<4;j++) l.forEach(e=>r.종목.push(JSON.parse(JSON.stringify(e)))); 그리기(); })()"); await pg.wait_for_timeout(200)
  a0=await pg.evaluate("ㅁ('#폰 .루자동뜸')")
  await pg.evaluate("(()=>{ const n=document.querySelector('#폰 .루넘김'); n.scrollTop=n.scrollHeight; })()"); await pg.wait_for_timeout(200)
  x=await pg.evaluate("""(()=>{ const n=document.querySelector('#폰 .루넘김'), 끝=[...n.querySelectorAll('.루격자>.종목상자, .루추가.끝')].filter(e=>getComputedStyle(e).display!=='none').pop();
    return {a:ㅁ('#폰 .루자동뜸'), 끝:ㅁ(끝), 끝종:끝.className, 넘:n.scrollTop, 높:n.scrollHeight-n.clientHeight}; })()""")
  봄(f'{W} ③ 맨 아래로 스크롤해도 자동 제자리', x['넘']>100 and x['a']==a0, [x['넘'],x['a'],a0])
  봄(f'{W} ③ 맨 끝(마지막 상자 · 끝 [+ 종목 추가])이 자동에 안 가림', x['끝']['b']<=x['a']['t']+0.5, x)
  await 찍기(pg, f'3_자동_스크롤끝_{W}.png')
  v0=await pg.evaluate("루틴(U.루틴열림).자동생성")
  await 누름(pg, '#폰 .루자동뜸 .스위치')
  봄(f'{W} ③ 자동 스위치 누름 → 켜짐/꺼짐 바뀜', await pg.evaluate("루틴(U.루틴열림).자동생성")==(not v0))
  await 누름(pg, '#폰 .루자동뜸 .스위치')
  # ── ④ 2열 격자 ──
  await pg.evaluate("(()=>{ const r=루틴(U.루틴열림); r.종목.splice(4); U.루펼침=new WeakSet(); 그리기(); document.querySelector('#폰 .루넘김').scrollTop=0; })()"); await pg.wait_for_timeout(200)
  상자 = """[...document.querySelectorAll('#폰 .루격자>.종목상자')].map(e=>{ const q=ㅁ(e); return {i:+e.dataset.i, l:q.l, t:q.t, w:q.w, 펼:e.classList.contains('루펼')}; })"""
  격=await pg.evaluate("ㅁ('#폰 .루격자')")
  x=await pg.evaluate(상자)
  반=(격['w']-8)/2
  봄(f'{W} ④ 접힌 상자 = 한 줄에 2개 (반 폭 · 같은 높이 줄)', all(abs(c['w']-반)<=1 for c in x) and x[0]['t']==x[1]['t'] and x[2]['t']==x[3]['t'] and x[1]['l']>x[0]['l'] and x[2]['t']>x[0]['t'], x)
  y=await pg.evaluate("""[...document.querySelectorAll('#폰 .루격자>.종목상자')].map(e=>{ const 이=e.querySelector('.루접줄 b, .루플랜줄 .이름플랜 b'), 글=[...e.querySelectorAll('.루플랜글,.루플랜퍼')], 휴=e.querySelector('.루빼기'), q=ㅁ(e), h=ㅁ(휴);
     return {이름:이.textContent, 이름줄:Math.round(ㅁ(이).h), 이름잘림:이.scrollWidth>이.clientWidth+1, 글잘림:글.filter(g=>g.scrollWidth>g.clientWidth+1).map(g=>g.textContent), 글밖:글.filter(g=>ㅁ(g).r>q.r+0.5).length, 휴안:h.l>=q.l&&h.r<=q.r+0.5&&h.t>=q.t-0.5, 게:e.querySelector('.루플랜게')?ㅁ(e.querySelector('.루플랜게')).w:null}; })""")
  print('   상자', y)
  봄(f'{W} ④ 반 폭 상자: 이름 한 줄(넘치면 …) · 세트/회차/% 글 안 잘림 · 휴지통 상자 안', all(c['이름줄']<=22 and not c['글잘림'] and c['글밖']==0 and c['휴안'] for c in y), y)
  봄(f'{W} ④ 플랜 상자 게이지 보임(≥ 16)', any(c['게'] and c['게']>=16 for c in y), [c['게'] for c in y])
  await 찍기(pg, f'4_격자_접힘_{W}.png')
  # 왼쪽(1번째) 펼침 → 줄 전체 · 오른쪽 짝은 아래 줄 왼쪽
  await 누름(pg, '#폰 .루격자>.종목상자[data-i="1"] .루접줄')   # 1번 = 인클라인(접힌 보통 종목 · 오른쪽 칸)
  x=await pg.evaluate(상자)
  봄(f'{W} ④ 오른쪽 상자(2번째) 펼침 → 다음 줄 전체 · 앞 줄 왼쪽에 1번째만 · 3번째는 그 아래 줄 왼쪽 · 순서 그대로', x[1]['펼'] and abs(x[1]['w']-격['w'])<=1 and x[1]['t']>x[0]['t'] and x[2]['t']>x[1]['t'] and abs(x[2]['l']-격['l'])<=0.5 and [c['i'] for c in x]==[0,1,2,3], x)
  y=await pg.evaluate("(()=>{ const e=document.querySelector('#폰 .루격자>.루펼'); return {세트줄:e.querySelectorAll('.루세트').length, 더:!!e.querySelector('[data-act=\"루세트\"]'), 머리폭:ㅁ(e.querySelector('.루머리')).w, 상자폭:ㅁ(e).w}; })()")
  봄(f'{W} ④ 펼친 상자 = 상세 설정(세트 줄 · + 세트) 보임', y['세트줄']>=1 and y['더'], y)
  await 찍기(pg, f'5_오른쪽펼침_{W}.png')
  await 누름(pg, '#폰 .루격자>.종목상자[data-i="1"] .루접줄')
  await 누름(pg, '#폰 .루격자>.종목상자[data-i="2"] .루접줄')   # 3번째 = 왼쪽 칸
  x=await pg.evaluate(상자)
  봄(f'{W} ④ 왼쪽 상자(3번째) 펼침 → 줄 전체 · 짝(4번째)은 아래 줄 왼쪽', x[2]['펼'] and abs(x[2]['w']-격['w'])<=1 and x[3]['t']>x[2]['t'] and abs(x[3]['l']-격['l'])<=0.5 and x[0]['t']==x[1]['t'], x)
  await 찍기(pg, f'6_왼쪽펼침_{W}.png')
  await 누름(pg, '#폰 .루격자>.종목상자[data-i="2"] .루접줄')
  # 꾹 끌기 — 1번째를 2번째 오른쪽 절반에 놓으면 2번째 뒤로
  if W==389:
    전=await pg.evaluate("루틴(U.루틴열림).종목.map(e=>e.이름)")
    a=await pg.evaluate("ㅁ('#폰 .루격자>.종목상자[data-i=\"0\"] .루플랜줄 .루플랜글, #폰 .루격자>.종목상자[data-i=\"0\"] .루접줄 .루플랜글')")
    c=await pg.evaluate("ㅁ('#폰 .루격자>.종목상자[data-i=\"1\"]')")
    await pg.mouse.move(a['l']+5,(a['t']+a['b'])/2); await pg.mouse.down(); await pg.wait_for_timeout(520)
    await pg.mouse.move(c['r']-20,(c['t']+c['b'])/2,steps=8)
    선=await pg.evaluate("[...document.querySelectorAll('#폰 .선위,.선아래')].map(e=>e.dataset.i+':'+[...e.classList].filter(k=>/^선/.test(k)))")
    await (await pg.query_selector('#폰')).screenshot(path=f'{사진}/7_끌기중_{W}.png')
    await pg.mouse.up(); await pg.wait_for_timeout(300)
    후=await pg.evaluate("루틴(U.루틴열림).종목.map(e=>e.이름)")
    봄(f'{W} ④ 꾹 끌기 — 반 폭 상자 오른쪽 절반 = 뒤에 놓음(선 오른쪽) · 순서 바뀜', 선==['1:선아래'] and 후==[전[1],전[0]]+전[2:], [선, 전, 후])
    await pg.evaluate("(()=>{ const r=루틴(U.루틴열림); const [a,b]=r.종목.splice(0,2); r.종목.unshift(b,a); 그리기(); })()"); await pg.evaluate(도구)
  # ── ⑤ 휴지통 ──
  x=await pg.evaluate("""(()=>{ const 휴=[...document.querySelectorAll('#폰 .루격자 .루빼기')], 참=document.createElement('i'); 참.innerHTML=아이콘.휴지통;
    return {수:휴.length, 상자:document.querySelectorAll('#폰 .루격자>.종목상자').length, 같음:휴.every(b=>b.querySelector('svg path')?.getAttribute('d')===참.querySelector('path').getAttribute('d')), 엑스:[...document.querySelectorAll('#폰 .루넘김 button')].filter(b=>/✕/.test(b.textContent)).length, 색:getComputedStyle(휴[0]).color, 크:ㅁ(휴[0]), 그림:ㅁ(휴[0].querySelector('svg'))}; })()""")
  봄(f'{W} ⑤ 상자마다 휴지통(아이콘.휴지통 · 32 칸 · 18 아이콘 · --옅음) · ✕ 없음', x['수']==x['상자'] and x['같음'] and x['엑스']==0 and x['색']=='rgb(94, 107, 119)' and x['크']['w']==32 and x['그림']['w']==18, x)
  n=await pg.evaluate("루틴(U.루틴열림).종목.length")
  await 누름(pg, '#폰 .루격자>.종목상자[data-i="3"] .루빼기')
  봄(f'{W} ⑤ 휴지통 누름 → 그 종목 빠짐', await pg.evaluate("루틴(U.루틴열림).종목.length")==n-1)
  # ── ⑥ 종목 넣기 시트 [확인] ──
  await 누름(pg, '#폰 .루추가.위', 500)
  x=await pg.evaluate("(()=>{ const b=document.querySelector('#폰 .시트>.머리 .닫기'); return {글:b.textContent.trim(), act:b.dataset.act, 엑스:[...document.querySelectorAll('#폰 .시트 button')].filter(b=>/✕/.test(b.textContent)).length}; })()")
  봄(f'{W} ⑥ 종목 넣기 시트 머리 오른쪽 = [확인] (data-act 시트닫기) · 시트 안 ✕ 없음', x['글']=='확인' and x['act']=='시트닫기' and x['엑스']==0, x)
  # ── ⑦ 연필 ──
  x=await pg.evaluate("""(()=>{ const 묶=[...document.querySelectorAll('#폰 .넣기묶음')], 칸=묶.map(m=>m.querySelector('.넣기칸')), 연=묶.map(m=>m.querySelector('.넣기편집'));
    const 겹=묶.filter((m,j)=>{ const c=ㅁ(칸[j]), p=연[j]&&ㅁ(연[j]), k=ㅁ(m.querySelector('.넣기체크')); return !p || p.l<k.r || p.r>c.r || p.t<c.t || p.b>c.b; }).length;
    const 참=document.createElement('i'); 참.innerHTML=아이콘.연필;
    const 이=칸.map(c=>c.querySelector('.이름플랜>b')), 잘=이.filter(b=>b.scrollHeight>b.clientHeight+1||b.scrollWidth>b.clientWidth+1||줄세기(b)>2||(!/ /.test(b.textContent)&&줄세기(b)>1&&!b.classList.contains('끊음'))).map(b=>b.textContent+':'+getComputedStyle(b).fontSize);
    return {묶:묶.length, 연:연.filter(Boolean).length, 같음:연.every(p=>p.querySelector('path').getAttribute('d')===참.querySelector('path').getAttribute('d')), 겹, 크:ㅁ(연[0]), 잘}; })()""")
  봄(f'{W} ⑦ 넣기 칸마다 연필(아이콘.연필) · 칸 안 · 체크 상자 오른쪽(안 겹침)', x['묶']>0 and x['연']==x['묶'] and x['같음'] and x['겹']==0 and x['크']['h']==28, x)
  봄(f'{W} ⑦ 이름 두 줄 안에 다 들어감(안 잘림 · 말줄임 없음 · 낱말 가운데서 안 끊김 — 가장 작게도 안 들어가는 낱말만 끊음)', not x['잘'], x['잘'])
  await 찍기(pg, f'8_넣기시트_{W}.png')
  전=await pg.evaluate("JSON.stringify(루틴(U.루틴열림).종목.map(e=>e.이름))")
  # 데드리프트 연필
  sel='#폰 .넣기편집[data-v="데드리프트"]'
  await 누름(pg, sel, 500)
  x=await pg.evaluate("({종:U.시트?.종류, 편집:U.새?.편집, 돌:U.시트?.돌아감?.종류, 단:document.querySelector('#폰 .시트>.머리 .닫기')?.textContent, 제목:document.querySelector('#폰 .시트>.머리 b')?.textContent, 같음:JSON.stringify(루틴(U.루틴열림).종목.map(e=>e.이름))})")
  봄(f'{W} ⑦ 연필 → 편집 시트(종목 탭 [편집]과 같은 새종목 · 편집) · 돌아감 = 종목 넣기 · 넣기는 그대로', x['종']=='새종목' and x['편집']=='데드리프트' and x['돌']=='종목넣기' and x['단']=='돌아가기' and x['같음']==전, x)
  await 찍기(pg, f'9_연필편집_{W}.png')
  await 누름(pg, '#폰 .시트>.머리 .닫기', 400)
  x=await pg.evaluate("({종:U.시트?.종류, 같음:JSON.stringify(루틴(U.루틴열림).종목.map(e=>e.이름)), 연:document.querySelectorAll('#폰 .넣기편집').length})")
  봄(f'{W} ⑦ [돌아가기] → 종목 넣기 시트로 · 넣기 그대로', x['종']=='종목넣기' and x['같음']==전 and x['연']>0, x)
  # 저장 → 돌아옴 (이름 바꿔 저장)
  await 누름(pg, sel, 500)
  await pg.evaluate("U.새종목='데드리프트 고침'; 행동('새저장',{})"); await pg.wait_for_timeout(400); await pg.evaluate(도구)
  x=await pg.evaluate("({종:U.시트?.종류, 이름:S.종목표.some(t=>t.이름==='데드리프트 고침'), 칸:!!document.querySelector('#폰 .넣기칸[data-v=\"데드리프트 고침\"], #폰 .넣기칸[aria-label^=\"데드리프트 고침\"]'), 같음:JSON.stringify(루틴(U.루틴열림).종목.map(e=>e.이름)), 토:document.querySelector('#폰 .토스트')?.textContent})")
  봄(f'{W} ⑦ 편집 [저장] → 종목 고쳐짐 · 종목 넣기 시트로 돌아옴 · 넣기 그대로', x['종']=='종목넣기' and x['이름'] and x['칸'] and x['같음']==전, x)
  # 끌어 닫기(머리 80 넘게) → 넣기 시트로
  await 누름(pg, '#폰 .넣기편집', 500)
  h=await pg.evaluate("ㅁ('#폰 .시트>.머리 b')")
  await pg.mouse.move(h['l']+10,(h['t']+h['b'])/2); await pg.mouse.down(); await pg.mouse.move(h['l']+10,(h['t']+h['b'])/2+140,steps=10); await pg.mouse.up(); await pg.wait_for_timeout(450)
  봄(f'{W} ⑦ 편집 시트 끌어 닫기 → 종목 넣기 시트로', await pg.evaluate("U.시트?.종류")=='종목넣기')
  # 꾹 누르기(연필 위) — 넣기 안 됨
  p=await pg.evaluate("ㅁ('#폰 .넣기편집')")
  await pg.mouse.move((p['l']+p['r'])/2,(p['t']+p['b'])/2); await pg.mouse.down(); await pg.wait_for_timeout(600); await pg.mouse.up(); await pg.wait_for_timeout(400)
  x=await pg.evaluate("({종:U.시트?.종류, 같음:JSON.stringify(루틴(U.루틴열림).종목.map(e=>e.이름))})")
  봄(f'{W} ⑦ 연필을 꾹 눌러도 넣기 안 됨(편집 시트만)', x['같음']==전 and x['종']=='새종목', x)
  await pg.evaluate("행동('시트닫기',{})"); await pg.wait_for_timeout(300)
  # 넣기 칸 누름은 그대로(넣기)
  n=await pg.evaluate("루틴(U.루틴열림).종목.length")
  await 누름(pg, '#폰 .넣기칸[data-v="랫풀다운"]', 300)
  봄(f'{W} ⑦ 칸(연필 밖) 누름은 그대로 넣기', await pg.evaluate("루틴(U.루틴열림).종목.length")==n+1)
  await 누름(pg, '#폰 .시트>.머리 .닫기', 400)
  봄(f'{W} ⑥ [확인] 누름 → 시트 닫힘', await pg.evaluate("U.시트")is None)
  # 종목 탭 [편집]은 그대로(돌아감 없음 → 닫힘)
  await pg.evaluate("행동('탭',{t:'종목'}); 행동('종목편집',{v:'바벨 컬'})"); await pg.wait_for_timeout(300)
  x=await pg.evaluate("({돌:U.시트?.돌아감, 단:document.querySelector('#폰 .시트>.머리 .닫기')?.textContent})")
  봄(f'{W} ⑦ 종목 탭 [편집]은 전과 같음(돌아감 없음 · [닫기])', x['돌'] is None and x['단']=='닫기', x)
  await pg.evaluate("U.새종목='바벨 컬'; 행동('새저장',{})"); await pg.wait_for_timeout(300)
  봄(f'{W} ⑦ 종목 탭 편집 저장 → 시트 닫힘(전과 같음)', await pg.evaluate("U.시트") is None)
  봄(f'{W} 페이지 오류 없음', not err, err)
  await pg.close()

async def main():
  async with async_playwright() as p:
    b=await p.chromium.launch()
    for W,H in [(389,860),(360,800)]: await 한폭(b,W,H)
    await b.close()
  print(f"\n통과 {결과['통과']} · 실패 {len(결과['실패'])}")
  for f in 결과['실패']: print('  ✗', f)
asyncio.run(main())
