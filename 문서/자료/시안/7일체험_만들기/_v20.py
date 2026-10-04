"""v20 확인 — 종목 탭 상자(이름 맞춤 · 접힘 · 편집) · 새 종목 시트(개수 없음 · 역할 둘 · 주동근 막힘)
쓰는 법: python3 _v20.py [html 절대경로]   (사진 → SP/r20/*.png)"""
import asyncio, sys, json, os
from playwright.async_api import async_playwright
SP='/tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'
HTML=sys.argv[1] if len(sys.argv)>1 else SP+'7day-v20.html'
상태=json.load(open(SP+'marks3/28f858e336f21768f689d786826649de.json'))
os.makedirs(SP+'r20',exist_ok=True)
결과={'통과':0,'실패':[]}
def 봄(이름, 조건, 값=None):
  if 조건: 결과['통과']+=1
  else: 결과['실패'].append(f"{이름} → {값}")
  print(('  ✓ ' if 조건 else '  ✗ ')+이름+('' if 값 is None else f"  {json.dumps(값,ensure_ascii=False)[:300]}"))

# 긴 이름 · 같은 이름 · 공백 없는 이름을 더한다(직접 만든 종목 꼴 — id · 근육)
더할=[("스미스 머신 벤치프레스","가슴"),("가나다라마바사아자차카타","가슴"),("스미스 머신 숄더 프레스","어깨"),
     ("오버헤드 트라이셉스 익스텐션","팔"),("로우 투 하이 케이블 플라이","가슴"),("벤치프레스","가슴"),("인클라인 체스트 프레스 머신","가슴")]
준비 = """([s,더할])=>{ S=JSON.parse(JSON.stringify(s.S)); Object.assign(U,s.U,{시트:null,업적띠:null,스탯:null}); S.세션=null; S.결과=null;
  더할.forEach(([n,c],i)=>S.종목표.push({id:'종시험'+i, 이름:n, 칸:c, 근육:{chest_mid:'P',triceps:'S'}}));
  U.탭='종목'; U.종목펼침=null; U.종목칸고름='전체'; 그리기(); }"""
이름표 = """(()=>[...document.querySelectorAll('#폰 .종목칸')].map(c=>{ const b=c.querySelector('.이름맞춤'), cs=getComputedStyle(b);
  return {이름:b.textContent, 글수:[...b.textContent].length, 줄:줄세기(b), 크기:cs.fontSize, 자간:b.style.letterSpacing||'-', 넘침:b.scrollWidth>b.clientWidth+0.5,
    자름:cs.textOverflow, 딱지:[...c.querySelectorAll('.플랜표')].map(x=>x.textContent).join('+'), 펼:c.classList.contains('펼'),
    사진:!!c.querySelector('.사진넣칸'), 근줄:c.querySelectorAll('.근줄').length, 편집:!!c.querySelector('[data-act=종목편집]'), 높이:Math.round(c.getBoundingClientRect().height)}; }))()"""

async def 종목탭검사(pg, W):
  l=await pg.evaluate(이름표)
  print(f'── {W} 접힌 상자 {len(l)}개')
  for x in l: print('   ', json.dumps({k:x[k] for k in ('이름','글수','줄','크기','자간','딱지','높이')},ensure_ascii=False))
  봄(f'{W} ② 접힌 상자에 사진 칸 · 근육 줄 · 편집 없음', all(not x['사진'] and x['근줄']==0 and not x['편집'] for x in l if not x['펼']))
  봄(f'{W} ① 12글자 이하 = 한 줄', all(x['줄']==1 for x in l if x['글수']<=12), [x['이름'] for x in l if x['글수']<=12 and x['줄']!=1])
  봄(f'{W} ① 12글자 넘음 = 두 줄 이하', all(x['줄']<=2 for x in l if x['글수']>12), [(x['이름'],x['줄']) for x in l if x['글수']>12])
  봄(f'{W} ① 가로 넘침 없음 · … 없음', all(not x['넘침'] and x['자름']=='clip' for x in l), [x['이름'] for x in l if x['넘침'] or x['자름']!='clip'])
  봄(f'{W} ① 글자 크기 = 15 · 13 · 11 중 하나', all(x['크기'] in ('15px','13px','11px') for x in l), sorted({x['크기'] for x in l}))
  return l

async def main():
  async with async_playwright() as p:
    b=await p.chromium.launch()
    for W,H in [(389,860),(360,800)]:
      pg=await b.new_page(viewport={'width':W,'height':H}); err=[]; pg.on('pageerror',lambda e:err.append(str(e)))
      await pg.goto('file://'+HTML); await pg.wait_for_timeout(600)
      await pg.evaluate(준비, [상태,더할]); await pg.wait_for_timeout(400)
      폰=await pg.query_selector('#폰')
      l=await 종목탭검사(pg, W)
      if W==389: await 폰.screenshot(path=SP+'r20/1_종목탭_접힘_389.png')
      # 이름 잘림 없는지 — 글자 끝이 칸 오른쪽(딱지 · ▾ 앞)을 넘지 않음
      끝=await pg.evaluate("""(()=>[...document.querySelectorAll('#폰 .종목칸')].map(c=>{ const b=c.querySelector('.이름맞춤'), r=document.createRange(); r.selectNodeContents(b);
        const 글끝=Math.max(...[...r.getClientRects()].map(q=>q.right)), 칸끝=c.querySelector('.접힘표').getBoundingClientRect().left; return [b.textContent, Math.round(글끝), Math.round(칸끝)]; }).filter(([,a,k])=>a>k))()""")
      봄(f'{W} ① 이름 글자가 ▾ 를 넘지 않음', not 끝, 끝)
      # 칸 폭이 바뀌면(창 크기) 다시 맞춤
      if W==360:
        await pg.set_viewport_size({'width':389,'height':860}); await pg.wait_for_timeout(300)
        r=await pg.evaluate("[...document.querySelectorAll('#폰 .이름맞춤')].filter(b=>b.scrollWidth>b.clientWidth+0.5).length")
        봄('창 389 로 넓힌 뒤 넘침 없음', r==0, r)
        await pg.set_viewport_size({'width':360,'height':800}); await pg.wait_for_timeout(300)

      # ── ② ③ 펼침 — 벤치프레스(기본 · 플랜) ──
      await pg.click('#폰 .종목칸이름[data-v="벤치프레스"]'); await pg.wait_for_timeout(400)
      x=await pg.evaluate("""(()=>{ const c=document.querySelector('#폰 .종목칸.펼'); const e=c.querySelector('[data-act=종목편집]'), n=c.querySelector('.종목칸이름');
        return {사진:!!c.querySelector('.사진넣칸'), 근줄:[...c.querySelectorAll('.근줄')].map(x=>x.textContent), 편집:e&&e.textContent, 편집높이:e&&Math.round(e.getBoundingClientRect().height),
          편집오른쪽:e&&Math.round(c.getBoundingClientRect().right-e.getBoundingClientRect().right), 이름크기:getComputedStyle(c.querySelector('.이름맞춤')).fontSize, 세트줄:c.querySelectorAll('.종세트').length}; })()""")
      print('  펼침', json.dumps(x,ensure_ascii=False))
      봄(f'{W} ② 펼치면 사진 칸 · 주동근/협응근 줄', x['사진'] and len(x['근줄'])==2, x)
      봄(f'{W} ③ 펼친 상자에 [편집] (32)', x['편집']=='편집' and x['편집높이']==32, x)
      if W==389: await 폰.screenshot(path=SP+'r20/2_종목탭_펼침_편집단추_389.png')

      # ── ③ 편집 시트 — 기본 종목(벤치프레스 · 플랜 있음) ──
      앞수=await pg.evaluate("S.종목표.length")
      await pg.click('#폰 .종목칸.펼 [data-act=종목편집]'); await pg.wait_for_timeout(400)
      y=await pg.evaluate("""(()=>{ const 시=document.querySelector('#폰 .시트'); return {제목:시.querySelector('.머리 b').textContent, 이름칸:시.querySelector('.새찾기칸').value, 칸:시.querySelector('.새칸줄 .칩.켬')?.textContent,
        역할칩:[...시.querySelectorAll('.새역할줄 .칩')].map(x=>x.textContent), 묶음칩:[...시.querySelectorAll('.새묶음 .칩')].map(x=>x.textContent),
        근육:JSON.stringify(U.새.근육), 세트:U.새.세트.length, 저장:!!시.querySelector('.새저장'), 편집:U.새.편집}; })()""")
      print('  편집 시트', json.dumps(y,ensure_ascii=False))
      봄(f'{W} ③ 제목 = 벤치프레스 편집 · 이름 · 카테고리 채움', y['제목']=='벤치프레스 편집', y['제목'])
      봄(f'{W} ③ 이름 칸 · 카테고리 · 저장 단추', y['이름칸']=='벤치프레스' and y['칸']=='가슴' and y['저장'], y)
      봄(f'{W} ④ 묶음 칩에 숫자 없음', all(not any(ch.isdigit() for ch in t) for t in y['묶음칩']), y['묶음칩'])
      봄(f'{W} ⑤ 역할 칩 = 주동근 · 협응근', y['역할칩']==['주동근','협응근'], y['역할칩'])
      봄(f'{W} ⑤ 들인 근육 역할 = P · Y 만', set(json.loads(y['근육']).values())<= {'P','Y'} and 'P' in json.loads(y['근육']).values(), y['근육'])
      # 협응근 역할 → 주동근 부위 흐림 · 누르면 토스트 · 안 바뀜
      await pg.click('#폰 .새묶음 [data-v="가슴"]'); await pg.wait_for_timeout(150)
      await pg.click('#폰 .새역할줄 [data-v="Y"]'); await pg.wait_for_timeout(200)
      # 사진에 그림 · 역할 칩 · 부위 칩이 함께 보이게 — 그림 윗변을 .새속 맨 위로(그 뒤 누름은 스크롤 없이)
      await pg.evaluate("(()=>{ const 속=document.querySelector('#폰 .새속'), 지=속.querySelector('.새지도'); 속.scrollTop+=지.getBoundingClientRect().top-속.getBoundingClientRect().top-4; })()"); await pg.wait_for_timeout(200)
      주=await pg.evaluate("Object.entries(U.새.근육).filter(([k,r])=>r==='P').map(([k])=>k)")
      막=await pg.evaluate("[...document.querySelectorAll('#폰 .새근육들 .근칩.막힘')].map(x=>[x.dataset.v, getComputedStyle(x).opacity, x.getAttribute('aria-disabled')])")
      print('  주동', 주, '흐림 칩', 막)
      가슴주=[k for k in 주 if k.startswith('chest') or k=='serratus']
      봄(f'{W} ⑤ 협응근 역할에서 주동근 부위 칩 = 흐림(.35 · aria-disabled)', 막 and sorted(m[0] for m in 막)==sorted(가슴주) and all(m[1]=='0.35' and m[2]=='true' for m in 막), [막,가슴주])
      k0=막[0][0]
      await pg.click(f'#폰 .새근육들 [data-v="{k0}"]', force=True); await pg.wait_for_timeout(150)   # aria-disabled 라 playwright 가 막는다 — 손가락은 눌린다
      t=await pg.evaluate(f"[document.querySelector('#폰 .토스트')?.textContent, U.새.근육['{k0}']]")
      봄(f'{W} ⑤ 누르면 토스트 · 역할 그대로 P', t==['이미 주동근으로 선택되어있습니다.','P'], t)
      if W==389: await 폰.screenshot(path=SP+'r20/3_편집시트_협응근_토스트_389.png')
      await pg.wait_for_timeout(500)
      봄(f'{W} ⑤ 0.65초 뒤에도 토스트 남음', await pg.evaluate("!!document.querySelector('#폰 .토스트')"))
      # 그림 조각을 눌러도 같은 막힘
      hb=await pg.evaluate(f"""(()=>{{ const x=[...document.querySelectorAll('#폰 .새몸칸 path[data-v="{k0}"]')][0]; if(!x) return null; const r=x.getBoundingClientRect(); return [r.left+r.width/2, r.top+r.height/2]; }})()""")
      if hb:
        await pg.evaluate("document.querySelectorAll('#폰 .토스트').forEach(x=>x.remove())")
        await pg.mouse.click(hb[0],hb[1]); await pg.wait_for_timeout(150)
        t2=await pg.evaluate(f"[document.querySelector('#폰 .토스트')?.textContent, U.새.근육['{k0}']]")
        봄(f'{W} ⑤ 그림 조각을 눌러도 같은 토스트', t2==['이미 주동근으로 선택되어있습니다.','P'], t2)
      # 협응근 역할로 빈 부위 → Y · 다시 누르면 빠짐
      await pg.click('#폰 .새묶음 [data-v="팔"]'); await pg.wait_for_timeout(150)
      await pg.click('#폰 .새근육들 [data-v="forearm"]'); await pg.wait_for_timeout(150)
      a1=await pg.evaluate("U.새.근육.forearm??'없음'")
      # 주동근 역할에서 협응근 부위 누름 → 주동으로 옮김
      await pg.click('#폰 .새역할줄 [data-v="P"]'); await pg.wait_for_timeout(150)
      await pg.click('#폰 .새근육들 [data-v="forearm"]'); await pg.wait_for_timeout(150)
      a2=await pg.evaluate("[U.새.근육.forearm??'없음', document.querySelectorAll('#폰 .근칩.막힘').length]")
      봄(f'{W} ⑤ 협응근으로 넣음 → 주동근 역할에서 누르면 P (막힘 없음)', a1=='Y' and a2==['P',0], [a1,a2])
      # 그림 색 두 단계
      색=await pg.evaluate("""(()=>{ const 색글=c=>{ const d=document.createElement('i'); d.style.color=c; document.body.appendChild(d); const v=getComputedStyle(d).color; d.remove(); return v; };
        const 단=[...new Set([...document.querySelectorAll('#폰 .새몸칸 path.몸근')].map(x=>x.style.fill).filter(f=>!f.includes('var(')))]; return {단, P:색글(단계색(20)), Y:색글(단계색(5))}; })()""")
      봄(f'{W} ⑤ 그림 색 = P · Y 두 단계', set(색['단'])<= {색['P'],색['Y']} and len(색['단'])>=1, 색)
      # 세트 하나 더 · 무게 + → 저장
      await pg.click('#폰 [data-act="새세트더"]'); await pg.wait_for_timeout(150)
      await pg.click('#폰 [data-act="새세트값"][data-k="0"][data-f="w"][data-d="1"]'); await pg.wait_for_timeout(150)
      기대세트=await pg.evaluate("JSON.stringify(U.새.세트)")
      # 이름을 바꾸려 하면(플랜이 걸린 기본 종목) 막힘
      await pg.fill('#폰 .새찾기칸','벤치프레스 바꿈'); await pg.wait_for_timeout(100)
      await pg.click('#폰 .새저장'); await pg.wait_for_timeout(200)
      t3=await pg.evaluate("[U.시트?.종류, document.querySelector('#폰 .토스트')?.textContent, S.종목표.filter(t=>t.이름==='벤치프레스 바꿈').length]")
      봄(f'{W} ③ 플랜 걸린 기본 종목 이름 바꾸기 = 막음(토스트)', t3==['새종목','플랜이 있는 종목은 이름을 바꿀 수 없습니다',0], t3)
      await pg.fill('#폰 .새찾기칸','벤치프레스'); await pg.wait_for_timeout(100)
      await pg.click('#폰 .새저장'); await pg.wait_for_timeout(300)
      z=await pg.evaluate("""(()=>{ const t=S.종목표.find(t=>t.이름==='벤치프레스'&&!t.id); return {시트:U.시트, 수:S.종목표.length, 근육:JSON.stringify(t.근육), 칸:t.칸, 세트:JSON.stringify(S.종목설정['벤치프레스'].세트), 펼:U.종목펼침,
        토스트:document.querySelector('#폰 .토스트')?.textContent, 근줄:[...document.querySelectorAll('#폰 .종목칸.펼 .근줄')].map(x=>x.textContent)}; })()""")
      print('  저장', json.dumps(z,ensure_ascii=False))
      봄(f'{W} ③ 저장 = 그 종목을 고침(새로 안 만듦)', z['시트'] is None and z['수']==앞수 and json.loads(z['근육']).get('forearm')=='P', z)
      봄(f'{W} ③ 기본 세팅도 저장', z['세트']==기대세트.replace(' ',''), [z['세트'],기대세트])
      봄(f'{W} ③ 저장 토스트 · 펼침 그대로', z['토스트']=='저장했습니다 · 벤치프레스' and z['펼']=='벤치프레스', [z['토스트'],z['펼']])

      # ── ③ 기본 종목 이름 바꾸기(플랜 없음 · 루틴에 있음) — 랫풀다운 → 와이드 랫풀다운 ──
      await pg.click('#폰 .종목칸이름[data-v="랫풀다운"]'); await pg.wait_for_timeout(300)
      await pg.evaluate("S.종목설정=S.종목설정||{}; S.종목설정['랫풀다운']={세트:[{w:55,r:12,휴:90}]}; 그리기()"); await pg.wait_for_timeout(200)
      await pg.click('#폰 .종목칸.펼 [data-act=종목편집]'); await pg.wait_for_timeout(300)
      시세트=await pg.evaluate("JSON.stringify(U.새.세트)")
      await pg.click('#폰 .새찾기칸'); await pg.wait_for_timeout(100)
      await pg.fill('#폰 .새찾기칸','와이드 랫풀다운'); await pg.wait_for_timeout(100)
      await pg.keyboard.press('Enter'); await pg.wait_for_timeout(200)
      n1=await pg.evaluate("[U.새종목, JSON.stringify(U.새.근육), U.새종목칸]")
      await pg.click('#폰 .새저장'); await pg.wait_for_timeout(300)
      r=await pg.evaluate("""(()=>{ const t=S.종목표.find(t=>t.이름==='와이드 랫풀다운'); const 줄=S.루틴들.flatMap(r=>r.종목).filter(e=>e.종id===t?.id);
        return {수:S.종목표.length, id:t?.id, 옛남음:S.종목표.some(t=>t.이름==='랫풀다운'), 설정:JSON.stringify(S.종목설정[t?.id]), 옛설정:!!S.종목설정['랫풀다운'], 줄:줄.map(e=>e.이름), 펼:U.종목펼침===t?.id}; })()""")
      print('  이름 바꿈', 시세트, n1, json.dumps(r,ensure_ascii=False))
      봄(f'{W} ③ 기본 종목 이름 바꿈 → id · 루틴 줄 · 기본 세팅 따라감', r['수']==앞수 and r['id'] and not r['옛남음'] and not r['옛설정'] and r['줄']==['와이드 랫풀다운'] and r['펼'] and '55' in (r['설정'] or ''), r)

      # ── 새 종목 만들기 — 사전 S → Y · 묶음 숫자 없음 ──
      await pg.click('#폰 .종목넘김 [data-act="새종목열기"]'); await pg.wait_for_timeout(300)
      await pg.click('#폰 .새찾기칸'); await pg.keyboard.type('인클라인 덤벨 플라이'); await pg.wait_for_timeout(150)
      await pg.click('#폰 .새결과줄[data-v="인클라인 덤벨 플라이"]'); await pg.wait_for_timeout(300)
      q=await pg.evaluate("[document.querySelector('#폰 .시트 .머리 b').textContent, JSON.stringify(U.새.근육), [...document.querySelectorAll('#폰 .새묶음 .칩')].map(x=>x.textContent).join(',')]")
      봄(f'{W} ⑤ 사전(chest_mid:S) → Y 로 들임 · 제목 새 종목', q[0]=='새 종목' and json.loads(q[1])=={'chest_upper':'P','chest_mid':'Y','delt_front':'Y'}, q)
      await pg.click('#폰 .새저장'); await pg.wait_for_timeout(300)
      봄(f'{W} 새 종목 저장은 그대로(하나 더)', await pg.evaluate("S.종목표.at(-1).이름==='인클라인 덤벨 플라이' && !!S.종목표.at(-1).id && !Object.values(S.종목표.at(-1).근육).includes('S')"))

      # ── 직접 만든 종목(id) 편집 · 이름 바꿈 → 루틴 줄 이름 따라감 ──
      await pg.evaluate("""(()=>{ const t=S.종목표.find(t=>t.id==='종시험0'); S.루틴들[0].종목.push({이름:t.이름, 종id:t.id, 세트:세트들(2,40,10,60)}); U.종목펼침='종시험0'; 그리기(); })()"""); await pg.wait_for_timeout(300)
      await pg.click('#폰 .종목칸.펼 [data-act=종목편집]'); await pg.wait_for_timeout(300)
      y2=await pg.evaluate("[document.querySelector('#폰 .시트 .머리 b').textContent, JSON.stringify(U.새.근육)]")
      await pg.fill('#폰 .새찾기칸','스미스 벤치'); await pg.click('#폰 .새저장'); await pg.wait_for_timeout(300)
      r2=await pg.evaluate("[S.종목표.find(t=>t.id==='종시험0').이름, S.루틴들[0].종목.at(-1).이름, JSON.stringify(S.종목표.find(t=>t.id==='종시험0').근육)]")
      봄(f'{W} ③ 직접 만든 종목 편집(옛 S → Y) · 이름 바꿈 → 루틴 줄도', y2[1]=='{"chest_mid":"P","triceps":"Y"}' and r2[:2]==['스미스 벤치','스미스 벤치'], [y2,r2])

      # ── '기타' 칸(플랜만 남은 종목)에는 편집 없음 ──
      await pg.evaluate("""S.플랜들.push({...S.플랜들[0], id:'p기타', 이름:'없는종목', 종목:'없는종목'}); U.종목펼침='없는종목'; 그리기()"""); await pg.wait_for_timeout(300)
      봄(f'{W} ③ 기타 칸 펼침에는 [편집] 없음', await pg.evaluate("!!document.querySelector('#폰 .종목칸.펼') && !document.querySelector('#폰 .종목칸.펼 [data-act=종목편집]')"))
      if W==360:
        await pg.evaluate("U.종목펼침=null; 그리기()"); await pg.wait_for_timeout(300)
        await 종목탭검사(pg, '360 (고친 뒤)')
        await 폰.screenshot(path=SP+'r20/4_종목탭_접힘_360.png')
      봄(f'{W} JS 오류 없음', not err, err)
      await pg.close()
    await b.close()
  print(f"\n통과 {결과['통과']} · 실패 {len(결과['실패'])}")
  for f in 결과['실패']: print('  ✗', f)
asyncio.run(main())
