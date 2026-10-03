"""10-03 v9 R 운동 보고서 시험 (✎ mdot · wxgv · mxxq · 7jjc · ya8r · gytb) — python3 test_v9_R.py [파일]  기본 7day-v9R.html
   사진은 r9/ 폴더에 남긴다"""
import asyncio, sys, json, os, base64
from playwright.async_api import async_playwright
SP='/tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'
파일 = sys.argv[1] if len(sys.argv)>1 else '7day-v9R.html'
F='file://'+SP+파일; R9=SP+'r10/v9R/'; os.makedirs(R9, exist_ok=True)
글자크기 = {11,13,15,18,22,28}
일RM = lambda w,r: 0 if r<=0 else (w if r==1 else w*(1+r/30))
# 2×2 빨간 PNG — 프로필 사진 고르기 시험용
PNG = base64.b64decode('iVBORw0KGgoAAAANSUhEUgAAAAIAAAACCAIAAAD91JpzAAAAFklEQVR4nGP8z8DAwMDAxMDAwMDAAAANHQEDasKb6QAAAABJRU5ErkJggg==')
open(R9+'프로필.png','wb').write(PNG)

준비JS = """(()=>{ U.업적띠=null; S.기록={}; S.설정.보고서프로필끔=false; S.설정.큰운동=3; S.설정.닉네임='';
  운동시작('r1'); const ss=S.세션;
  /* 10칸 — r1 네 종목 + 여섯 (턱걸이는 맨몸) */
  [['덤벨 플라이',12,12],['펜들레이 로우',60,8],['랫풀다운',50,12],['바벨 컬',30,10],['트라이셉스 푸시다운',25,12],['턱걸이',0,6]]
    .forEach(([n,w,r])=>ss.종목.push({이름:n, 세트:세트들(3,w,r,60).map(s=>({...s,목r:s.r,완료:false}))}));
  const 셋=ss.종목.slice(0,3); 셋.forEach(e=>e.세트.slice(0,2).forEach(s=>s.완료=true));
  ss.종목.slice(3).forEach(e=>e.세트.forEach(s=>s.완료=true));
  /* 지난번(2일 전, 같은 루틴): 첫 종목 5kg 가볍게(▲) · 둘째 5kg 무겁게(▼) · 셋째 똑같이(표시 없음) · 턱걸이 2회 적게(▲2회) */
  const 옛=셋.map((e,i)=>({이름:e.이름, 세트:e.세트.slice(0,2).map(s=>({w:Math.max(0,s.w+[-5,5,0][i]), r:s.r, 완료:true}))}));
  옛.push({이름:'턱걸이', 세트:[{w:0,r:4,완료:true},{w:0,r:4,완료:true}]});
  S.기록[날더하기(오늘(),-2)]={루틴id:'r1', 이름:ss.이름, 초:1800, 시작시각:0, 종목:옛};
  /* 5일 전: 첫 종목을 아주 무겁게 — '지난번' 이 아니므로 칸에는 안 쓰이고, 상세의 최고 대비에만 */
  S.기록[날더하기(오늘(),-5)]={루틴id:'r1', 이름:ss.이름, 초:1800, 시작시각:0, 종목:[{이름:셋[0].이름, 세트:[{w:200,r:1,완료:true}]}]};
  /* 3일 전 하체 — 스쿼트 · 데드 1RM */
  S.기록[날더하기(오늘(),-3)]={루틴id:'r4', 이름:'하체', 초:1800, 시작시각:0, 종목:[{이름:'백 스쿼트', 세트:[{w:100,r:5,완료:true}]},{이름:'데드리프트', 세트:[{w:120,r:5,완료:true},{w:130,r:2,완료:false}]}]};
  S.시계+=0; 그리기();
  return {이름:ss.이름, 종:ss.종목.map(e=>({이름:e.이름, 플랜:!!e.플랜id, 세트:e.세트.map(s=>[s.w,s.r,s.완료])})), 옛, 오늘:오늘()}; })()"""

async def main():
  결과=[]
  def 봄(m,ok): 결과.append((m,bool(ok)))
  async with async_playwright() as p:
    b=await p.chromium.launch()
    for 폭,높이 in [(420,1080),(360,800)]:
      pg=await b.new_page(viewport={'width':폭,'height':높이}); 오류=[]; pg.on('pageerror',lambda e:오류.append(str(e)))
      await pg.goto(F); await pg.wait_for_timeout(700); t=f"[{폭}] "
      await pg.evaluate("try{localStorage.removeItem(저장키+'-프로필')}catch(e){}; 프로필사진=null; U.업적띠=null; 그리기();")
      # ── 설정 탭: '운동 보고서' 묶음 ──
      await pg.evaluate("U.탭='설정'; 그리기();")
      설=await pg.evaluate("""(()=>{ const 표=[...document.querySelectorAll('.이름표')].find(x=>x.textContent==='운동 보고서'), 카=표?.nextElementSibling;
        return {있음:!!카, 줄:[...(카?.querySelectorAll('.설정줄 .채움>div:first-child')||[])].map(x=>x.textContent), 스위치:!!카?.querySelector('.스위치.켬[data-f="보고서프로필끔"]'),
          칩:[...(카?.querySelectorAll('.칩')||[])].map(x=>x.textContent+(x.classList.contains('켬')?'*':'')), 입력:!!카?.querySelector('input.입력[data-pf="닉네임"]'),
          다음:카?.nextElementSibling?.textContent}; })()""")
      봄(t+f"mxxq 설정 '운동 보고서' 묶음 ({설['줄']} · 칩 {설['칩']})", 설['있음'] and 설['줄']==['보고서에 프로필 표시','큰 운동','닉네임'] and 설['스위치'] and 설['칩']==['3대*','4대','5대'] and 설['입력'] and 설['다음']=='데이터')
      for 일 in ["pg.fill('[data-pf=\"닉네임\"]','홍겸')", "pg.click('.칩[data-f=\"큰운동\"][data-v=\"5\"]')", "pg.click('.스위치[data-f=\"보고서프로필끔\"]')"]:
        try: await asyncio.wait_for(eval(일), 2)
        except Exception as e: print('   (건너뜀)', 일[:40], type(e).__name__)
        await pg.wait_for_timeout(100)
      설2=await pg.evaluate("({닉:S.설정.닉네임, 큰:S.설정.큰운동, 끔:S.설정.보고서프로필끔, 켬:document.querySelector('.스위치[data-f=\"보고서프로필끔\"]')?.classList.contains('켬'), 입력값:document.querySelector('[data-pf=\"닉네임\"]')?.value})")
      봄(t+f"mxxq 닉네임 입력 · 5대 · 스위치 끄기 ({설2})", 설2['닉']=='홍겸' and 설2['큰']==5 and 설2['끔']==True and not 설2['켬'] and 설2['입력값']=='홍겸')
      넘=await pg.evaluate("document.documentElement.scrollWidth>document.documentElement.clientWidth+1")
      봄(t+"설정 탭 가로 넘침 없음", not 넘)
      if 폭==360: await (await pg.query_selector('#폰')).screenshot(path=R9+'설정_360.png')
      await pg.evaluate("document.querySelector('[data-n=\"설6\"]')?.scrollIntoView({block:'center'})")
      if 폭==420: await (await pg.query_selector('#폰')).screenshot(path=R9+'설정_420.png')

      # ── 보고서 ──
      준비=await pg.evaluate(준비JS)
      await pg.click('.아랫줄 [data-act="끝내기"]'); await pg.wait_for_timeout(300)
      중간=await pg.evaluate("[...document.querySelectorAll('.결과수 [data-count]')].map(x=>x.textContent)")
      await pg.wait_for_timeout(4200)
      끝값=await pg.evaluate("[...document.querySelectorAll('.결과수 [data-count]')].map(x=>x.textContent)")
      봄(t+f"숫자가 0 부터 올라감 ({중간} → {끝값})", 중간[1]!=끝값[1])
      await pg.evaluate("S.설정.보고서프로필끔=false; S.설정.큰운동=3; S.설정.닉네임=''; U.업적띠=null; 그리기();"); await pg.wait_for_timeout(200)
      r=await pg.evaluate("""(()=>{ const q=s=>document.querySelector(s), R=e=>{ if(!e) return null; const r=e.getBoundingClientRect(); return {l:r.left,t:r.top,r:r.right,b:r.bottom,w:r.width,h:r.height}; };
        const 색=v=>{const d=document.createElement('i'); d.style.color=getComputedStyle(document.documentElement).getPropertyValue(v); document.body.appendChild(d); const c=getComputedStyle(d).color; d.remove(); return c;};
        const 바색=v=>{const d=document.createElement('i'); d.style.backgroundColor=getComputedStyle(document.documentElement).getPropertyValue(v); document.body.appendChild(d); const c=getComputedStyle(d).backgroundColor; d.remove(); return c;};
        const 띠=q('.결과띠'), 날=q('.보고날'), 루=q('.보고루틴'), 목=q('.보고목록'), 칸=[...document.querySelectorAll('.보고칸')], 틀=q('.결과틀');
        const 수=q('.보고루틴 .결과수')||q('.결과수:not(.큰수)'), 수칸=[...수.children];
        return {띠글:띠.querySelector('.결과띠속').innerText, 띠:R(띠), 날글:날?.textContent, 날:R(날), 날색:getComputedStyle(날).color, 날크기:getComputedStyle(날).fontSize, 강조글:색('--강조글'),
          수안:!!q('.보고루틴 .결과수'), 수글:수칸.map(x=>x.querySelector(':scope>span').textContent), 수선:수칸.map(x=>getComputedStyle(x).borderLeftWidth+' '+getComputedStyle(x).borderLeftStyle), 선색:수칸.slice(1).map(x=>getComputedStyle(x).borderLeftColor), 선:색('--선'),
          루글:루.innerText, 루:R(루), 루테:getComputedStyle(루).borderTopWidth, 루모:getComputedStyle(루).borderTopLeftRadius, 루카드:루.classList.contains('카드'),
          칸테:getComputedStyle(칸[0]).borderTopWidth, 칸모:getComputedStyle(칸[0]).borderTopLeftRadius, 목:R(목), 칸:칸.map(x=>({...R(x), 글:x.innerText.replace(/\\n/g,'|'), 줄:[...x.querySelectorAll('.보고줄')].map(z=>z.innerText.replace(/\\s+/g,' ')), 이름:x.querySelector('.보고이름 b').textContent,
            차:[...x.querySelectorAll('.보고차')].map(c=>({글:c.innerText, 색:getComputedStyle(c).color, 반:c.className}))})),
          오름:색('--오름'), 내림:색('--내림'), 프로필:!!q('.보고프로필'), 사진:R(q('.보고사진')), 사진모:getComputedStyle(q('.보고사진')).borderRadius, 닉:q('.보고닉')?.textContent,
          큰머리:q('.보고큰머리')?.innerText.replace(/\\n/g,'|'), 큰:[...document.querySelectorAll('.큰수>div')].map(x=>x.innerText.replace(/\\n/g,'|')),
          단추:[...document.querySelectorAll('.결과아래 button')].map(x=>x.dataset.act+':'+x.textContent),
          세로넘침:틀.scrollHeight>틀.clientHeight+1, 줄넘침:[...document.querySelectorAll('.보고칸,.보고줄,.보고프로필,.보고루틴 .줄,.결과수>div')].filter(x=>x.scrollWidth>x.clientWidth+1).map(x=>x.className+':'+x.innerText.slice(0,20)),
          크기:[...new Set([...document.querySelectorAll('.결과틀 *, .결과아래 *')].filter(x=>[...x.childNodes].some(n=>n.nodeType===3&&n.textContent.trim())).map(x=>parseFloat(getComputedStyle(x).fontSize)))],
          플:(()=>{ const c=칸.find(x=>x.querySelector('.플랜표')); if(!c) return null; const f=c.querySelector('.플랜표'), n=c.querySelector('.보고이름 b'), s=getComputedStyle(f);
            return {이름:n.textContent, 글:f.textContent, 바:s.backgroundColor, 색:s.color, 크기:s.fontSize, 굵기:s.fontWeight, 모:s.borderTopLeftRadius, 겹가로:Math.round(n.getBoundingClientRect().right-f.getBoundingClientRect().left), 위:Math.round(n.getBoundingClientRect().top-f.getBoundingClientRect().top)}; })(),
          강조바:바색('--강조'), 플랜칸수:칸.filter(x=>x.querySelector('.플랜표')).length,
          폰글:q('#폰').innerText}; })()""")
      if 폭==420: print(json.dumps({k:v for k,v in r.items() if k not in ('폰글','칸')}, ensure_ascii=False)[:2500]); print([c['글'] for c in r['칸']])
      오늘=준비['오늘']; y,m,d=오늘.split('-')
      # mdot
      봄(t+f"mdot 띠 = '운동 보고서' ('{r['띠글']}') · 루틴 이름 · 달성 글 없음", r['띠글']=='운동 보고서')
      봄(t+f"mdot 띠 두 줄 높이 ({r['띠']['h']:.0f}px)", r['띠']['h']>=56)
      봄(t+f"mdot 오른쪽 아래 날짜 '{r['날글']}' · 11 · 강조글", r['날글']==f"{y}.{m}.{d}." and r['날크기']=='11px' and r['날색']==r['강조글'] and r['띠']['r']-r['날']['r']<=13 and r['띠']['b']-r['날']['b']<=5 and r['날']['t']>r['띠']['t']+r['띠']['h']/2)
      봄(t+"mdot 루틴 상자에 'N종목 · 달성도' 줄 · 날짜 없음", '달성도' not in r['루글'] and '종목 ·' not in r['루글'] and '월 ' not in r['루글'] and '(' not in r['루글'])
      봄(t+f"mdot 루틴 상자 구분 (카드 {r['루테']}/{r['루모']} vs 칸 {r['칸테']}/{r['칸모']} · 틈 {r['목']['t']-r['루']['b']:.0f})", r['루카드'] and r['루테']=='2px' and r['칸테']=='1px' and r['루모']=='16px' and r['칸모']=='8px' and r['목']['t']-r['루']['b']>=8-0.5)
      봄(t+f"mdot 숫자 순서 세트 / 총 볼륨 / 운동 시간 ({r['수글']})", r['수글']==['세트','총 볼륨','운동 시간'])
      # wxgv
      봄(t+f"wxgv 단추 글 ({r['단추']})", r['단추']==['운동저장:운동 기록 저장하고 종료','운동으로:운동으로 돌아가기','운동버림:기록하지 않고 종료'])
      봄(t+f"wxgv 칸 사이 세로선 ({r['수선']})", r['수선'][0].startswith('0px') and all(x=='1px solid' for x in r['수선'][1:]) and all(c==r['선'] for c in r['선색']))
      # mxxq
      봄(t+f"mxxq 프로필 줄 · 동그란 사진 {r['사진']['w']:.0f}px · 닉네임 자리 '{r['닉']}'", r['프로필'] and round(r['사진']['w'])==48 and r['사진모']=='50%' and r['닉']=='닉네임')
      봄(t+"mxxq 숫자 묶음이 루틴 상자 안", r['수안'])
      w0,r0=[s for s in 준비['종'][0]['세트'] if s[2]][0][:2]
      벤=max([일RM(s[0],s[1]) for e in 준비['종'] if e['이름']=='벤치프레스' for s in e['세트'] if s[2] and s[0]>0]+[일RM(s['w'],s['r']) for e in 준비['옛'] if e['이름']=='벤치프레스' for s in e['세트']]+[200])
      기대=[('벤치',벤),('스쿼트',일RM(100,5)),('데드',일RM(120,5))]
      fmt=lambda v: f"{round(v):,}" if v>=100 or abs(v-round(v))<1e-9 else f"{v:.1f}"
      기대글=[f"{fmt(round(v*10)/10)}|{n}" for n,v in 기대]
      봄(t+f"mxxq 3대 1RM ({r['큰']} = {기대글}) · 합계 '{r['큰머리']}'", r['큰']==기대글 and r['큰머리'].startswith('3대 1RM 합계|') and r['큰머리'].endswith(fmt(round(sum(round(v*10)/10 for _,v in 기대)*10)/10)+'kg'))
      # 7jjc
      칸=r['칸']
      봄(t+f"7jjc 체크한 종목 10칸 ({len(칸)})", len(칸)==10 and [c['이름'] for c in 칸]==[e['이름'] for e in 준비['종']])
      xs=sorted(set(round(c['l']) for c in 칸))
      봄(t+f"7jjc 두 칸 격자 (왼쪽 {xs})", len(xs)==2 and all(abs(칸[i]['t']-칸[i+1]['t'])<1 for i in range(0,10,2)))
      if 폭==420: 봄(t+f"7jjc 10칸이 한 화면 (마지막 칸 아래 {칸[-1]['b']:.0f} ≤ 격자 아래 {r['목']['b']:.0f})", 칸[-1]['b']<=r['목']['b']+0.5)
      봄(t+"7jjc 칸에 '1주' '최고 대비' '대비' 글 없음", not any(('1주' in c['글'] or '대비' in c['글']) for c in 칸))
      e0,e1,e2=칸[0],칸[1],칸[2]
      ws=[s for s in 준비['종'][0]['세트'] if s[2]]; 기rm=max(일RM(w,r_) for w,r_,_ in ws)-max(일RM(w-5,r_) for w,r_,_ in ws); 기볼=5*sum(r_ for _,r_,_ in ws)
      봄(t+f"7jjc 오른 종목 ▲ 빨강 ({[c['글'] for c in e0['차']]} · 기대 ▲{fmt(round(기rm*10)/10)}kg ▲{fmt(기볼)}kg)", [c['글'] for c in e0['차']]==[f"▲{fmt(round(기rm*10)/10)}kg", f"▲{fmt(기볼)}kg"] and all(c['색']==r['오름'] for c in e0['차']))
      봄(t+f"7jjc 내린 종목 ▼ 파랑 ({[c['글'] for c in e1['차']]})", len(e1['차'])==2 and all(c['글'].startswith('▼') and c['글'].endswith('kg') and c['색']==r['내림'] for c in e1['차']))
      봄(t+f"7jjc 같은 종목 표시 없음 ({e2['글']})", len(e2['차'])==0)
      턱=칸[-1]
      봄(t+f"7jjc 맨몸은 횟수로 ({턱['글']})", '최고' in 턱['글'] and '6회' in 턱['글'] and [c['글'] for c in 턱['차']]==['▲2회','▲10회'])
      첫줄=[e0['이름']]+e0['줄']
      봄(t+f"7jjc 칸 = 이름 / '1RM 78kg ▲' / '볼륨 1,080kg ▲' ({첫줄})", len(첫줄)==3 and 첫줄[1].startswith('1RM ') and 첫줄[1].endswith('kg') and 첫줄[2].startswith('볼륨 ') and '▲' in 첫줄[1] and '▲' in 첫줄[2])
      # gytb
      플=r['플']
      봄(t+f"gytb 플랜 칸 [플랜] 표 ({플})", 플 and r['플랜칸수']==1 and 플['글']=='플랜' and 플['바']==r['강조바'] and 플['색']==r['강조글'] and 플['크기']=='11px' and 플['굵기']=='700' and 플['모']=='8px' and 4<=플['겹가로']<=6 and 플['위']>0)
      봄(t+f"결과 화면 세로 안 넘침 · 줄 가로 안 넘침 ({r['줄넘침']})", not r['세로넘침'] and not r['줄넘침'])
      봄(t+f"글자 크기 11·13·15·18·22·28 만 ({sorted(r['크기'])})", set(r['크기'])<=글자크기)
      await (await pg.query_selector('#폰')).screenshot(path=R9+f'보고서_{폭}.png')
      # ya8r 펼치기
      await pg.click('.보고칸[data-n="0"]'); await pg.wait_for_timeout(400)
      펼=await pg.evaluate("""(()=>{ const l=document.querySelector('.보고목록'), 상=l.querySelectorAll('.보고상세'), c=[...l.children];
        const d=상[0]; return {수:상.length, 자리:c.indexOf(d), 앞:d?.previousElementSibling?.dataset.n, 폭:Math.round(d?.getBoundingClientRect().width), 목폭:l.clientWidth,
          글:d?.innerText.replace(/\\n/g,'|'), 세트:[...(d?.querySelectorAll('.보고세트들 span')||[])].map(x=>x.textContent), 칩:[...(d?.querySelectorAll('.대비칩')||[])].map(x=>x.innerText.replace(/\\s+/g,' ')),
          열림:document.querySelector('.보고칸[data-n="0"]').getAttribute('aria-expanded'), 펼침:document.querySelector('.보고칸[data-n="0"]').classList.contains('펼침')}; })()""")
      if 폭==420: print('펼침', json.dumps(펼, ensure_ascii=False))
      세기대=[f"{k+1}세트 · {(str(int(w)) if float(w).is_integer() else f'{w:.1f}')+'kg × ' if w>0 else ''}{r_}회" for k,(w,r_,ok) in enumerate(준비['종'][0]['세트']) if ok]
      봄(t+f"ya8r 누르면 그 줄 아래(둘째 칸 뒤) 두 칸 폭 상세 ({펼['자리']} · {펼['폭']}/{펼['목폭']})", 펼['수']==1 and 펼['앞']=='1' and abs(펼['폭']-펼['목폭'])<=1 and 펼['열림']=='true' and 펼['펼침'])
      봄(t+f"ya8r 세트별 '세트 · 무게 × 횟수' ({펼['세트']})", 펼['세트']==세기대)
      봄(t+f"ya8r 최고 세트 · 1주 · 최고 대비 · 플랜 회차 ({펼['칩']})", '최고 세트 · ' in 펼['글'] and any('1주 대비' in c for c in 펼['칩']) and any('최고 대비' in c and '▼' in c for c in 펼['칩']) and '플랜' in 펼['글'] and '회차' in 펼['글'])
      if 폭==420: await (await pg.query_selector('#폰')).screenshot(path=R9+'펼침_420.png')
      await pg.click('.보고칸[data-n="3"]'); await pg.wait_for_timeout(300)
      바=await pg.evaluate("(()=>{ const l=document.querySelector('.보고목록'), 상=l.querySelectorAll('.보고상세'); return {수:상.length, 앞:상[0]?.previousElementSibling?.dataset.n, 이름:상[0]?.querySelector('b')?.textContent, 0:document.querySelector('.보고칸[data-n=\"0\"]').classList.contains('펼침')}; })()")
      봄(t+f"ya8r 다른 칸 → 그것만 펼침 (넷째 칸 뒤 {바})", 바['수']==1 and 바['앞']=='3' and 바['이름']==준비['종'][3]['이름'] and not 바['0'])
      await pg.click('.보고칸[data-n="3"]'); await pg.wait_for_timeout(250)
      접=await pg.evaluate("document.querySelectorAll('.보고상세').length")
      봄(t+f"ya8r 다시 누르면 접힘 ({접})", 접==0)
      # 넘긴 자리 그대로 · 상세 보이게
      await pg.evaluate("(()=>{ const l=document.querySelector('.보고목록'); l.scrollTop=l.scrollHeight; l.dispatchEvent(new Event('scroll')); })()"); await pg.wait_for_timeout(150)
      앞자리=await pg.evaluate("document.querySelector('.보고목록').scrollTop")
      await pg.click('.보고칸[data-n="8"]'); await pg.wait_for_timeout(900)
      뒤=await pg.evaluate("""(()=>{ const l=document.querySelector('.보고목록'), d=l.querySelector('.보고상세'), a=l.getBoundingClientRect(), b=d.getBoundingClientRect(), c=l.querySelector('.보고칸.펼침').getBoundingClientRect();
        return {top:l.scrollTop, 상위:b.top>=a.top-1, 상아래:b.bottom<=a.bottom+1, 칸보임:c.top>=a.top-1&&c.bottom<=a.bottom+1, 앞:d.previousElementSibling.dataset.n}; })()""")
      봄(t+f"ya8r 넘긴 뒤 눌러도 자리 그대로 ({앞자리} → {뒤['top']}) · 칸 보임 · 상세가 줄 뒤({뒤['앞']})", (앞자리==0 or 뒤['top']>0) and 뒤['칸보임'] and 뒤['앞']=='9' and (뒤['상아래'] or not 뒤['상위'] or 뒤['칸보임']))
      if 폭==360: await (await pg.query_selector('#폰')).screenshot(path=R9+'펼침끝_360.png')
      await pg.click('.보고칸[data-n="8"]'); await pg.wait_for_timeout(200)
      # mxxq 닉네임 · 5대 · 사진
      await pg.evaluate("S.설정.닉네임='홍겸'; S.설정.큰운동=5; 그리기();"); await pg.wait_for_timeout(150)
      p5=await pg.evaluate("({사진:document.querySelector('.보고사진').innerText, 닉:document.querySelector('.보고닉').textContent, 머리:document.querySelector('.보고큰머리 span')?.textContent, 큰:[...document.querySelectorAll('.큰수>div')].map(x=>x.innerText.replace(/\\n/g,'|')), 선:[...document.querySelectorAll('.큰수>div')].map(x=>getComputedStyle(x).borderLeftWidth), 넘:[...document.querySelectorAll('.큰수>div,.보고프로필')].filter(x=>x.scrollWidth>x.clientWidth+1).length})")
      ohp=fmt(round(max(일RM(s[0],s[1]) for e in 준비['종'] if e['이름']=='오버헤드 프레스' for s in e['세트'] if s[2])*10)/10); 로=fmt(round(일RM(60,8)*10)/10)
      봄(t+f"mxxq 5대 = +OHP {ohp} · +로우 {로} ({p5['큰']}) · 사이 선 {p5['선']} · 이름 첫 글자 '{p5['사진']}'", p5['머리']=='5대 1RM 합계' and len(p5['큰'])==5 and p5['큰'][3]==f"{ohp}|OHP" and p5['큰'][4]==f"{로}|로우" and p5['선']==['0px']+['1px']*4 and p5['사진']=='홍' and p5['닉']=='홍겸' and p5['넘']==0)
      await pg.set_input_files('input[data-pf="사진"]', R9+'프로필.png'); await pg.wait_for_timeout(500)
      사=await pg.evaluate("({img:!!document.querySelector('.보고사진 img'), 저장:!!localStorage.getItem(저장키+'-프로필')})")
      봄(t+f"mxxq 동그라미 눌러 사진 고르기 → 사진 · 브라우저에 남음 ({사})", 사['img'] and 사['저장'])
      if 폭==420: await (await pg.query_selector('#폰')).screenshot(path=R9+'5대_420.png')
      # 프로필 끄기 → 숫자 묶음이 띠 아래로
      await pg.evaluate("S.설정.보고서프로필끔=true; 그리기();"); await pg.wait_for_timeout(150)
      끔=await pg.evaluate("(()=>{ const 띠=document.querySelector('.결과띠'), 다=띠.nextElementSibling; return {프로필:!!document.querySelector('.보고프로필'), 다음:다.className, 안:!!document.querySelector('.보고루틴 .결과수'), 글:[...다.querySelectorAll('span:not(.올림수)')].map(x=>x.textContent), 넘:document.querySelector('.결과틀').scrollHeight>document.querySelector('.결과틀').clientHeight+1}; })()")
      봄(t+f"mxxq 프로필 끄면 숫자 묶음이 띠 바로 아래 ({끔})", not 끔['프로필'] and '결과수' in 끔['다음'] and not 끔['안'] and 끔['글']==['세트','총 볼륨','운동 시간'] and not 끔['넘'])
      if 폭==420: await (await pg.query_selector('#폰')).screenshot(path=R9+'프로필끔_420.png')
      await pg.evaluate("S.설정.보고서프로필끔=false; 그리기();")
      # 기록하지 않고 종료 — 두 번
      await pg.click('[data-act="운동버림"]'); await pg.wait_for_timeout(120)
      글1=await pg.evaluate("[document.querySelector('[data-act=\"운동버림\"]')?.textContent, !!S.세션]")
      봄(t+f"기록하지 않고 종료 한 번 → 글만 바뀜 ({글1})", 글1==['한 번 더 누르면 버립니다', True])
      await pg.click('[data-act="운동으로"]'); await pg.wait_for_timeout(120); await pg.click('.아랫줄 [data-act="끝내기"]'); await pg.wait_for_timeout(150)
      # 저장 → 저장된 보고서
      n0=await pg.evaluate("Object.keys(S.기록).length")
      await pg.click('[data-act="운동저장"]'); await pg.wait_for_timeout(300)
      키=await pg.evaluate("Object.keys(S.기록).filter(k=>k.startsWith(오늘())).sort().pop()")
      await pg.evaluate(f"U.업적띠=null; S.결과={{key:{json.dumps(키)}}}; 그리기();"); await pg.wait_for_timeout(300)
      저=await pg.evaluate("""({띠:document.querySelector('.결과띠속')?.innerText, 날:document.querySelector('.보고날')?.textContent, 아래:document.querySelector('.결과아래')?.innerText.replace(/\\n/g,'|'), 수:Object.keys(S.기록).length,
        칸:[...document.querySelectorAll('.보고칸')].map(x=>[...x.querySelectorAll('.보고차 [data-count]')].map(c=>c.dataset.count))[0]})""")
      봄(t+f"저장된 보고서: 띠 · 날짜 · '기록은 저장되었습니다' · 자기 자신과 안 견줌 ({저})", 저['띠']=='운동 보고서' and 저['날']==f"{y}.{m}.{d}." and 저['아래']=='기록은 저장되었습니다|확인' and 저['수']==n0+1 and 저['칸']==[c['글'][1:-2] for c in e0['차']])
      await pg.click('[data-act="결과확인"]'); await pg.wait_for_timeout(150)
      넘=await pg.evaluate("document.documentElement.scrollWidth>document.documentElement.clientWidth+1")
      봄(t+"페이지 가로 넘침 없음", not 넘)
      봄(t+"JS 오류 없음", not 오류)
      if 오류: print(오류[:3])
      await pg.close()
    # 어두운 화면 — 눈으로만
    pg=await b.new_page(viewport={'width':420,'height':1080}, color_scheme='dark'); await pg.goto(F); await pg.wait_for_timeout(600)
    await pg.evaluate(준비JS); await pg.evaluate("S.설정.닉네임='홍겸'"); await pg.click('.아랫줄 [data-act="끝내기"]'); await pg.wait_for_timeout(4200)
    await pg.click('.보고칸[data-n="0"]'); await pg.wait_for_timeout(400)
    await (await pg.query_selector('#폰')).screenshot(path=R9+'어둠_420.png'); await pg.close()
    await b.close()
  [print(("✅ " if ok else "❌ ")+m) for m,ok in 결과]; print(f"\n{sum(ok for _,ok in 결과)}/{len(결과)}")
asyncio.run(main())
