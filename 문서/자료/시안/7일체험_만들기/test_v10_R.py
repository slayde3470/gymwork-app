"""10-03 v10 R 운동 보고서 · 프로필 탭 시험 (✎ 0vnw · oaee · pdb1 · ua0b · rvva) — python3 test_v10_R.py [파일]  기본 _10R.html
   사진은 r10/ 폴더에 남긴다. 폰 칸 420×860 (창 452×1080) 이 기준, 좁은 폰(창 360 → 폰 328)도 넘침만 본다"""
import asyncio, sys, json, os, base64
겹 = lambda a,b_: not(a['r']<=b_['l']+0.5 or b_['r']<=a['l']+0.5 or a['b']<=b_['t']+0.5 or b_['b']<=a['t']+0.5)
from playwright.async_api import async_playwright
SP='/tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'
파일 = sys.argv[1] if len(sys.argv)>1 else '_10R.html'
F='file://'+SP+파일; F9='file://'+SP+'7day-v9.html'; R10=SP+'r10/'; os.makedirs(R10, exist_ok=True)
글자크기 = {11,13,15,18,22,28}
# 2×2 빨간 PNG — 프로필 사진 고르기 시험용
open(R10+'프로필.png','wb').write(base64.b64decode('iVBORw0KGgoAAAANSUhEUgAAAAIAAAACCAIAAAD91JpzAAAAFklEQVR4nGP8z8DAwMDAxMDAwMDAAAANHQEDasKb6QAAAABJRU5ErkJggg=='))

# 준비 — r1(가슴·어깨) + 여섯 종목 + 백 스쿼트 120×5. 첫 종목(벤치) 은 세트 전부 체크
# 지난 기록: 3일 전 스쿼트 100×5(116.7) · 데드 120×5(140) / 2일 전 벤치 70×5(81.7)
#  → 3대 앞 338.4 · 지금 361.7 (▲23.3) · 스쿼트 140 (▲23.3) · 벤치 81.7(오늘 78 이라 그대로 → 표시 없음) · 데드 140(그대로)
준비JS = """(()=>{ U.업적띠=null; U.시트=null; S.기록={}; S.설정.닉네임=''; S.설정.보고서보임={프로필:true,루틴:true}; S.설정.큰운동추가=[];
  운동시작('r1'); const ss=S.세션;
  [['덤벨 플라이',12,12],['펜들레이 로우',60,8],['랫풀다운',50,12],['바벨 컬',30,10],['트라이셉스 푸시다운',25,12],['턱걸이',0,6],['백 스쿼트',120,5]]
    .forEach(([n,w,r])=>ss.종목.push({이름:n, 세트:세트들(3,w,r,60).map(s=>({...s,목r:s.r,완료:false}))}));
  ss.종목[0].세트.forEach(s=>s.완료=true); ss.종목.slice(1,3).forEach(e=>e.세트.slice(0,2).forEach(s=>s.완료=true));
  ss.종목.slice(3).forEach(e=>e.세트.forEach(s=>s.완료=true));
  S.기록[날더하기(오늘(),-3)]={루틴id:'r4', 이름:'하체', 초:1800, 시작시각:0, 종목:[{이름:'백 스쿼트', 세트:[{w:100,r:5,완료:true}]},{이름:'데드리프트', 세트:[{w:120,r:5,완료:true}]}]};
  S.기록[날더하기(오늘(),-2)]={루틴id:'r1', 이름:ss.이름, 초:1800, 시작시각:0, 종목:[{이름:'벤치프레스', 세트:[{w:70,r:5,완료:true}]}]};
  그리기(); return {벤치세트:ss.종목[0].세트.length}; })()"""

색JS = """window.__색=v=>{const d=document.createElement('i'); d.style.color=getComputedStyle(document.documentElement).getPropertyValue(v); document.body.appendChild(d); const c=getComputedStyle(d).color; d.remove(); return c;};
window.__바=v=>{const d=document.createElement('i'); d.style.backgroundColor=getComputedStyle(document.documentElement).getPropertyValue(v); document.body.appendChild(d); const c=getComputedStyle(d).backgroundColor; d.remove(); return c;};
window.__R=e=>{ if(!e) return null; const r=e.getBoundingClientRect(); return {l:r.left,t:r.top,r:r.right,b:r.bottom,w:r.width,h:r.height}; };
window.__겹=(a,b)=>!(a.r<=b.l+0.5||b.r<=a.l+0.5||a.b<=b.t+0.5||b.b<=a.t+0.5);
window.__넘=sel=>[...document.querySelectorAll(sel)].filter(x=>x.scrollWidth>x.clientWidth+1).map(x=>x.className+':'+x.innerText.slice(0,20));
window.__글자=root=>[...new Set([...root.querySelectorAll('*')].filter(x=>x.offsetParent!==null&&[...x.childNodes].some(n=>n.nodeType===3&&n.textContent.trim())).map(x=>parseFloat(getComputedStyle(x).fontSize)))];
1"""

# 보고서 한 장 읽기
읽기JS = """(()=>{ const q=s=>document.querySelector(s), R=__R;
  const 루=q('.보고루틴'), 프=q('.보고프로필'), 톱=q('.톱니단추'), 칸=[...document.querySelectorAll('.보고프로필 .큰수>div')];
  const 루수=루?[...루.querySelectorAll('.결과수>div')]:[];
  return {루:!!루, 루글:루?.innerText||'', 루알약:!!루?.querySelector('.알약'), 루카드:루?.classList.contains('카드'), 루높이:루?R(루).h:0,
    루테:루&&getComputedStyle(루).borderTopWidth, 루테색:루&&getComputedStyle(루).borderTopColor, 루모:루&&getComputedStyle(루).borderTopLeftRadius, 루바:루&&getComputedStyle(루).backgroundColor,
    칸테:getComputedStyle(q('.보고칸')).borderTopWidth, 칸모:getComputedStyle(q('.보고칸')).borderTopLeftRadius, 칸테색:getComputedStyle(q('.보고칸')).borderTopColor,
    루이름크기:루&&parseFloat(getComputedStyle(루.querySelector('.보고루틴이름')).fontSize), 루숫자크기:루수.length?parseFloat(getComputedStyle(루수[0].querySelector('b')).fontSize):0, 루꼬리크기:루수.length?parseFloat(getComputedStyle(루수[0].querySelector(':scope>span')).fontSize):0,
    루수글:루수.map(x=>x.querySelector(':scope>span').textContent),
    프:!!프, 프테:프&&getComputedStyle(프).borderTopWidth, 프테색:프&&getComputedStyle(프).borderTopColor, 프바:프&&getComputedStyle(프).backgroundColor, 프여백:프&&parseFloat(getComputedStyle(프).paddingTop), 프:R(프),
    사진:R(q('.보고프로필 .보고사진')), 사진모:q('.보고프로필 .보고사진')&&getComputedStyle(q('.보고프로필 .보고사진')).borderRadius, 닉크기:q('.보고닉')&&parseFloat(getComputedStyle(q('.보고닉')).fontSize), 닉:q('.보고닉')?.textContent,
    칸:칸.map(x=>{ const b=x.querySelector('.큰값 b')||x.querySelector('b'), s=x.querySelector(':scope>span'), c=x.querySelector('.보고차');
      return {값:b.textContent, 이름:s.textContent, 차:c?c.innerText:'', 차색:c?getComputedStyle(c).color:'', 차크기:c?getComputedStyle(c).fontSize:'', 차굵기:c?getComputedStyle(c).fontWeight:'',
        값크기:getComputedStyle(b).fontSize, 값굵기:getComputedStyle(b).fontWeight, 이름크기:getComputedStyle(s).fontSize, 이름색:getComputedStyle(s).color, 선:getComputedStyle(x).borderLeftWidth,
        b:R(b), s:R(s), 큰값:R(x.querySelector('.큰값')), 칸:R(x), 차R:R(c)}; }),
    톱:R(톱), 톱안:톱?.parentElement?.className||'', 톱그림:R(톱?.querySelector('svg')),
    합계줄:document.body.innerText.includes('1RM 합계'),
    넘:__넘('.보고프로필,.보고프로필 .큰수>div,.큰값,.보고프로필 .큰수>div>span,.보고루틴,.보고루틴 .줄,.보고루틴 .결과수>div,.보고칸,.보고줄'),
    세로넘침:q('.결과틀').scrollHeight>q('.결과틀').clientHeight+1, 가로넘침:document.documentElement.scrollWidth>document.documentElement.clientWidth+1,
    크기:__글자(q('.결과틀')), 오름:__색('--오름'), 내림:__색('--내림'), 선:__색('--선'), 흐림:__색('--흐림'), 면:__바('--면'), 면2:__바('--면2')}; })()"""

# ✎ 표시 도구가 쓰는 html2canvas 1.4.1 로 폰 칸 사진 — color 함수 등을 못 읽으면 오류가 난다
async def 표시사진(pg, 이름):
  if not await pg.evaluate("!!window.html2canvas"): await pg.add_script_tag(path=SP+'h2c.min.js')
  r=await pg.evaluate("""(async()=>{ try{ const 폰=document.getElementById('폰'); const c=await html2canvas(폰,{backgroundColor:getComputedStyle(폰).backgroundColor, scale:1.5, logging:false}); return c.toDataURL('image/png'); }catch(e){ return 'ERR:'+e.message; } })()""")
  if r.startswith('data:'): open(R10+이름,'wb').write(base64.b64decode(r.split(',',1)[1])); return True
  print('html2canvas', r); return False

async def 끝화면(pg):
  await pg.click('.아랫줄 [data-act="끝내기"]'); await pg.wait_for_timeout(4300)
  await pg.evaluate("U.업적띠=null; document.querySelectorAll('.업적띠').forEach(x=>x.remove()); 1")

async def main():
  결과=[]
  def 봄(m,ok): 결과.append((m,bool(ok)))
  async with async_playwright() as p:
    b=await p.chromium.launch()
    # ── v9 루틴 상자 크기 · 글자 (견줄 기준) ──
    pg=await b.new_page(viewport={'width':452,'height':1080}); await pg.goto(F9); await pg.wait_for_timeout(600); await pg.evaluate(색JS)
    await pg.evaluate("S.설정.보고서프로필끔=false; S.설정.큰운동=3;"); await pg.evaluate(준비JS); await 끝화면(pg)
    v9=await pg.evaluate(읽기JS); await pg.close()
    print('v9 루틴 상자', v9['루높이'], v9['루이름크기'], v9['루숫자크기'], v9['루꼬리크기'], '닉', v9['닉크기'], '사진', v9['사진'] and v9['사진']['w'])

    for 창,높이 in [(452,1080),(360,800)]:
      pg=await b.new_page(viewport={'width':창,'height':높이}); 오류=[]; pg.on('pageerror',lambda e:오류.append(str(e)))
      await pg.goto(F); await pg.wait_for_timeout(700); await pg.evaluate(색JS)
      await pg.evaluate("try{localStorage.removeItem(저장키+'-프로필')}catch(e){}; 프로필사진=null; U.업적띠=null; 그리기(); 1")
      폰=await pg.evaluate("__R(document.getElementById('폰'))"); 폭=round(폰['w']); t=f"[{폭}] "; 넓=창==452
      if 넓: 봄(t+f"폰 칸 420×860 ({폭}×{round(폰['h'])})", 폭==420 and round(폰['h'])==860)
      준비=await pg.evaluate(준비JS); await 끝화면(pg)
      r=await pg.evaluate(읽기JS)
      if 넓: print(json.dumps({k:v for k,v in r.items() if k not in ('칸',)}, ensure_ascii=False)[:1800]); print([(c['값'],c['차'],c['이름']) for c in r['칸']])
      # ── 0vnw 루틴 상자 ──
      봄(t+f"0vnw 루틴 상자 '달성' 없음 ({r['루글'][:30]!r})", r['루'] and not r['루알약'] and '달성' not in r['루글'])
      봄(t+f"0vnw 루틴 상자 = 종목 칸 테두리 ({r['루테']} {r['루모']} vs 칸 {r['칸테']} {r['칸모']}) · 색 --선 · .카드 아님", not r['루카드'] and r['루테']==r['칸테']=='1px' and r['루모']==r['칸모']=='8px' and r['루테색']==r['선']==r['칸테색'])
      봄(t+f"0vnw 루틴 상자 바탕 --면2 ({r['루바']} · 면 {r['면']})", r['루바']==r['면2'] and r['루바']!=r['면'])
      봄(t+f"0vnw 글자 한 단계씩 작게 (이름 {v9['루이름크기']}→{r['루이름크기']} · 숫자 {v9['루숫자크기']}→{r['루숫자크기']} · 꼬리 {v9['루꼬리크기']}→{r['루꼬리크기']})",
         r['루이름크기']==13 and r['루숫자크기']==15 and r['루꼬리크기']==11 and r['루이름크기']<v9['루이름크기'] and r['루숫자크기']<v9['루숫자크기'])
      if 넓: 봄(t+f"0vnw 루틴 상자 높이 줄어듦 ({v9['루높이']:.0f} → {r['루높이']:.0f})", r['루높이']<v9['루높이']-8)
      봄(t+f"0vnw 세트 / 총 볼륨 / 운동 시간 그대로 ({r['루수글']})", r['루수글']==['세트','총 볼륨','운동 시간'])
      # ── 0vnw 프로필 ──
      봄(t+f"0vnw 프로필 사진 58 ({r['사진']['w']:.0f}×{r['사진']['h']:.0f} · {r['사진모']}) · v9 {v9['사진']['w']:.0f}", round(r['사진']['w'])==58 and round(r['사진']['h'])==58 and r['사진모']=='50%')
      봄(t+f"0vnw 닉네임 {v9['닉크기']} → {r['닉크기']} (−10% 에 가장 가까운 목록 값)", r['닉크기']==11 and v9['닉크기']==13)
      봄(t+f"프로필 = 테두리 상자 (1px --선 · 바탕 --면 · 여백 {r['프여백']})", r['프'] and r['프테']=='1px' and r['프테색']==r['선'] and r['프바']==r['면'] and r['프여백']>=12)
      if 넓: 봄(t+f"프로필 상자 v9 보다 큼 ({v9['프']['h']:.0f} → {r['프']['h']:.0f})", r['프']['h']>v9['프']['h'])
      # ── pdb1 · ua0b 큰 운동 칸 ──
      칸=r['칸']
      봄(t+f"ua0b 칸 순서 3대 · 스쿼트 · 벤치 · 데드 ({[c['이름'] for c in 칸]})", [c['이름'] for c in 칸]==['3대','스쿼트','벤치','데드'])
      봄(t+f"ua0b '3대 1RM 합계' 줄 없음", not r['합계줄'])
      봄(t+f"ua0b 숫자 위 · 이름 아래 · 숫자 kg ({[c['값'] for c in 칸]})", all(c['b']['b']<=c['s']['t']+0.5 for c in 칸) and [c['값'] for c in 칸]==['362kg','140kg','81.7kg','140kg'])
      봄(t+f"ua0b 숫자 18 Bold · 이름 11 흐림", all(c['값크기']=='18px' and c['값굵기']=='700' and c['이름크기']=='11px' and c['이름색']==r['흐림'] for c in 칸))
      봄(t+f"ua0b 오른 칸 ▲ 단위 없이 --오름 · 11 Bold ({[(c['차'],c['차색']) for c in 칸]})", 칸[0]['차']=='▲23.3' and 칸[1]['차']=='▲23.3' and all(c['차색']==r['오름'] and c['차크기']=='11px' and c['차굵기']=='700' for c in 칸[:2]))
      봄(t+"ua0b 같으면 아무것도 없음 (벤치 · 데드)", 칸[2]['차']=='' and 칸[3]['차']=='')
      옆=['옆' if abs(c['차R']['t']-c['b']['t'])<8 and c['차R']['l']>=c['b']['r']-0.5 else '아래' if c['차R']['t']>=c['b']['b']-0.5 and c['차R']['b']<=c['s']['t']+0.5 and c['차R']['l']>=c['칸']['l'] and c['차R']['r']<=c['칸']['r'] else '?' for c in 칸[:2]]
      봄(t+f"ua0b ▲ 은 숫자에 붙음 — 옆, 칸 폭이 모자라면 숫자 바로 아래 · 이름 위 ({옆})", '?' not in 옆)
      # 홍겸 님 10-03 "가로 1줄에 3대/sbd" — 늘 한 줄
      if 넓: 봄(t+f"ua0b 4칸 한 줄 · 칸 사이 세로선 ({[c['선'] for c in 칸]})", len({round(c['칸']['t']) for c in 칸})==1 and [c['선'] for c in 칸]==['0px','1px','1px','1px'])
      else: 봄(t+f"ua0b 좁은 폰은 두 칸씩 (2×2) · 세로선 ({[c['선'] for c in 칸]})", len({round(c['칸']['t']) for c in 칸})==2 and [c['선'] for c in 칸]==['0px','1px','0px','1px'])
      봄(t+f"ua0b 톱니 = 프로필 상자 오른쪽 위 모서리 · 28 · 아이콘 18 ({r['톱']})", '보고프로필' in r['톱안'] and abs(r['톱']['r']-r['프']['r'])<=1.5 and abs(r['톱']['t']-r['프']['t'])<=1.5 and round(r['톱']['w'])==28 and round(r['톱']['h'])==28 and round(r['톱그림']['w'])==18)
      봄(t+"ua0b 톱니가 숫자 · 이름과 안 겹침", not any(겹(r['톱'],c['큰값']) or 겹(r['톱'],c['s']) for c in 칸))
      봄(t+f"줄 · 칸 글자 잘림 없음 ({r['넘']})", not r['넘'])
      봄(t+"페이지 가로 넘침 없음 · 보고서 세로 안 넘침", not r['가로넘침'] and not r['세로넘침'])
      if 넓:
        열=await pg.evaluate("(()=>{ const l=document.querySelector('.보고목록'), c=l.querySelectorAll('.보고칸'); return [c[9].getBoundingClientRect().bottom, l.getBoundingClientRect().bottom]; })()")
        봄(t+f"7jjc 종목 10칸이 한 화면 그대로 (10번째 칸 아래 {열[0]:.0f} ≤ 격자 아래 {열[1]:.0f})", 열[0]<=열[1]+0.5)
      봄(t+f"글자 크기 목록 안 ({sorted(r['크기'])})", set(r['크기'])<=글자크기)
      await (await pg.query_selector('#폰')).screenshot(path=R10+f'보고서_{폭}.png')
      # ▼ · 기록 없음 · 앞 기록 없음 — 지금 계산(가장 좋은 1RM)은 내려가지 않으므로 칸 함수에 직접 넣어 본다
      내=await pg.evaluate("""(()=>{ const d=document.createElement('div'); d.className='보고상자 보고프로필'; d.innerHTML=큰운동판([{글:'가',v:100,앞:103},{글:'나',v:100,앞:100},{글:'다',v:0,앞:0},{글:'라',v:50,앞:0}], true, false);
        document.querySelector('.결과틀').appendChild(d); const r=[...d.querySelectorAll('.큰수>div')].map(c=>{ const x=c.querySelector('.보고차'); return [c.querySelector('.큰값 b').textContent, x?x.innerText:'', x?getComputedStyle(x).color:'']; }); d.remove(); return r; })()""")
      봄(t+f"ua0b ▼ 단위 없이 --내림 · 같으면 없음 · 기록 없으면 — · 앞 기록 없으면 없음 ({내})", 내[0][:2]==['100kg','▼3'] and 내[0][2]==r['내림'] and 내[1]==['100kg','',''] and 내[2]==['—','',''] and 내[3]==['50kg','',''])

      # ── oaee 펼친 상세 — 왼쪽 1,2,3 / 오른쪽 4,5 ──
      await pg.click('.보고칸[data-n="0"]'); await pg.wait_for_timeout(500)
      세=await pg.evaluate("[...document.querySelectorAll('.보고세트들 span')].map(x=>({글:x.textContent.split(' ')[0], ...__R(x)}))")
      n=len(세); h=(n+1)//2; 왼=[x for x in 세 if abs(x['l']-세[0]['l'])<1]; 오=[x for x in 세 if x['l']>세[0]['l']+1]
      봄(t+f"oaee 세트 {n}개 → 왼쪽 {[x['글'] for x in 왼]} / 오른쪽 {[x['글'] for x in 오]}", n==준비['벤치세트']==5 and [x['글'] for x in 왼]==['1세트','2세트','3세트'] and [x['글'] for x in 오]==['4세트','5세트'] and all(왼[i]['t']<왼[i+1]['t'] for i in range(len(왼)-1)) and abs(오[0]['t']-왼[0]['t'])<1)
      if 넓: await (await pg.query_selector('#폰')).screenshot(path=R10+'상세_420.png')
      await pg.click('.보고칸[data-n="1"]'); await pg.wait_for_timeout(400)
      세2=await pg.evaluate("[...document.querySelectorAll('.보고세트들 span')].map(x=>({글:x.textContent.split(' ')[0], ...__R(x)}))")
      봄(t+f"oaee 2세트 → 왼쪽 1 / 오른쪽 2 ({[x['글'] for x in 세2]})", len(세2)==2 and 세2[1]['l']>세2[0]['l']+1 and abs(세2[1]['t']-세2[0]['t'])<1)
      await pg.click('.보고칸[data-n="1"]'); await pg.wait_for_timeout(300)

      # ── ua0b 톱니 → 시트 ──
      await pg.click('.보고프로필 .톱니단추'); await pg.wait_for_timeout(450)
      시=await pg.evaluate("""(()=>{ const 시=document.querySelector('.시트'), 머=시?.querySelector('.머리');
        return {있음:!!시, 제목:머?.querySelector('b')?.textContent, 머바:머&&getComputedStyle(머).backgroundColor, 강조:__바('--강조'),
          스위치:[...시.querySelectorAll('.스위치[data-act="보고보임"]')].map(x=>x.dataset.f+(x.classList.contains('켬')?'*':'')), 줄:[...시.querySelectorAll('.설정줄 .채움>div:first-child')].map(x=>x.textContent),
          칩:[...시.querySelectorAll('.큰운동칩줄 .칩')].map(x=>x.textContent+(x.classList.contains('켬')?'*':'')+(x.disabled?'!':'')), 표:[...시.querySelectorAll('.이름표')].map(x=>x.textContent), 최대:시.innerText.includes('최대 5개까지')}; })()""")
      봄(t+f"ua0b 시트 '결과 보고서 표시 방법' · 머리 = 띠 ({시['제목']})", 시['있음'] and 시['제목']=='결과 보고서 표시 방법' and 시['머바']==시['강조'])
      봄(t+f"ua0b 보일 것 = 프로필 · 루틴 결과 스위치 둘 다 켬 ({시['줄']} {시['스위치']})", 시['줄']==['프로필','루틴 결과'] and 시['스위치']==['프로필*','루틴*'])
      봄(t+f"ua0b 칩 7개 · SBD 켬 ({시['칩']} · {시['표']})", 시['칩']==['스쿼트*','벤치*','데드*','오버헤드 프레스','바벨 로우','스내치','클린 앤 저크'] and 시['표']==['보일 것','큰 운동 · 3대'] and not 시['최대'])
      await pg.click('.큰운동칩줄 .칩[data-v="벤치"]', force=True); await pg.wait_for_timeout(200)
      벤=await pg.evaluate("[document.querySelector('.큰운동칩줄 .칩[data-v=\"벤치\"]').classList.contains('켬'), JSON.stringify(S.설정.큰운동추가)]")
      봄(t+f"ua0b SBD 칩은 눌러도 그대로 ({벤})", 벤==[True,'[]'])
      await pg.click('.큰운동칩줄 .칩[data-v="오버헤드 프레스"]'); await pg.wait_for_timeout(200)
      넷=await pg.evaluate("({표:[...document.querySelectorAll('.시트 .이름표')].map(x=>x.textContent), 칸:[...document.querySelectorAll('.보고프로필 .큰수>div>span')].map(x=>x.textContent)})")
      봄(t+f"ua0b OHP 켜면 4대 · 칸 5개 ({넷})", 넷['표'][1]=='큰 운동 · 4대' and 넷['칸']==['4대','스쿼트','벤치','데드','OHP'])
      await pg.click('.큰운동칩줄 .칩[data-v="바벨 로우"]'); await pg.wait_for_timeout(250)
      다=await pg.evaluate("""(()=>{ const 시=document.querySelector('.시트'); return {칩:[...시.querySelectorAll('.큰운동칩줄 .칩')].map(x=>x.textContent+(x.classList.contains('켬')?'*':'')+(x.disabled?'!':'')),
        흐림:[...시.querySelectorAll('.큰운동칩줄 .칩:disabled')].map(x=>getComputedStyle(x).opacity), 표:[...시.querySelectorAll('.이름표')].map(x=>x.textContent)[1], 최대:[...시.querySelectorAll('.아주작')].map(x=>x.textContent), 추가:S.설정.큰운동추가}; })()""")
      봄(t+f"ua0b 로우까지 → 5대 · 셋째 추가 칩 흐리게 · '최대 5개까지' ({다})", 다['칩']==['스쿼트*','벤치*','데드*','오버헤드 프레스*','바벨 로우*','스내치!','클린 앤 저크!'] and 다['흐림']==['0.35','0.35'] and 다['표']=='큰 운동 · 5대' and '최대 5개까지' in 다['최대'] and 다['추가']==['오버헤드 프레스','바벨 로우'])
      await pg.evaluate("document.querySelector('.큰운동칩줄 .칩[data-v=\"스내치\"]').click()"); await pg.wait_for_timeout(150)
      봄(t+"ua0b 흐린 칩은 눌러도 안 늘어남", await pg.evaluate("S.설정.큰운동추가.length")==2)
      시글=await pg.evaluate("__글자(document.querySelector('.시트'))")
      봄(t+f"시트 글자 크기 목록 안 ({sorted(시글)})", set(시글)<=글자크기)
      await (await pg.query_selector('#폰')).screenshot(path=R10+f'시트5대_{폭}.png')
      if 넓: 봄(t+"✎ 표시 사진(html2canvas) — 보고서 + 시트", await 표시사진(pg, 'h2c_시트5대_420.png'))
      await pg.click('.시트 .머리 .닫기'); await pg.wait_for_timeout(400)
      r5=await pg.evaluate(읽기JS); 칸=r5['칸']
      봄(t+f"ua0b 5대 = 6칸 ({[(c['이름'],c['값'],c['차']) for c in 칸]})", [c['이름'] for c in 칸]==['5대','스쿼트','벤치','데드','OHP','로우'] and [c['값'] for c in 칸]==['482kg','140kg','81.7kg','140kg','44.3kg','76kg'] and 칸[0]['차']=='▲144' and 칸[4]['차']=='' and 칸[5]['차']=='')
      위=sorted({round(c['칸']['t']) for c in 칸})
      # 홍겸 님 10-03 "5개 선택하니까 1줄에 3개씩뜨네" → 6칸도 가로 한 줄
      if 넓: 봄(t+f"ua0b 6칸도 가로 한 줄 (줄 위 {위}) · 세로선 {[c['선'] for c in 칸]}", len(위)==1 and [c['선'] for c in 칸]==['0px']+['1px']*5)
      else: 봄(t+f"ua0b 좁은 폰 6칸 = 3칸씩 두 줄 (줄 위 {위})", len(위)==2 and [c['선'] for c in 칸]==['0px','1px','1px']*2)
      숫=await pg.evaluate("""[...document.querySelectorAll('.보고프로필 .큰수>div')].map(d=>{ const b=d.querySelector('.큰값 b'), z=getComputedStyle(d); return [b.getBoundingClientRect().width, d.clientWidth-parseFloat(z.paddingLeft)-parseFloat(z.paddingRight), getComputedStyle(b).fontSize]; })""")
      봄(t+f"ua0b 6칸 숫자가 칸 안에 다 들어감 · 한 줄 같은 크기 · 목록 값 ({[(round(a),round(c),f) for a,c,f in 숫]})", all(a<=c+0.5 for a,c,_ in 숫) and len({f for *_,f in 숫})==1 and int(float(숫[0][2][:-2])) in 글자크기)
      봄(t+"ua0b 6칸일 때도 톱니가 숫자와 안 겹침", not any(겹(r5['톱'],c['큰값']) for c in 칸))
      봄(t+f"5대 칸 글자 잘림 없음 · 세로 안 넘침 ({r5['넘']})", not r5['넘'] and not r5['세로넘침'] and not r5['가로넘침'])
      await (await pg.query_selector('#폰')).screenshot(path=R10+f'보고서5대_{폭}.png')
      # 5칸 (OHP 만) — 3칸 + 2칸
      await pg.evaluate("S.설정.큰운동추가=['오버헤드 프레스']; 그리기(); 1"); await pg.wait_for_timeout(200)
      다섯=await pg.evaluate("[...document.querySelectorAll('.보고프로필 .큰수>div')].map(x=>[Math.round(x.getBoundingClientRect().top), getComputedStyle(x).borderLeftWidth])")
      if 넓: 봄(t+f"ua0b 5칸도 가로 한 줄 ({다섯})", len(다섯)==5 and len({x[0] for x in 다섯})==1 and [x[1] for x in 다섯]==['0px']+['1px']*4)
      else: 봄(t+f"ua0b 좁은 폰 5칸 = 3 + 2 ({다섯})", len(다섯)==5 and 다섯[0][0]==다섯[2][0]<다섯[3][0]==다섯[4][0] and [x[1] for x in 다섯]==['0px','1px','1px','0px','1px'])
      await (await pg.query_selector('#폰')).screenshot(path=R10+f'보고서4대_{폭}.png')
      await pg.evaluate("S.설정.큰운동추가=[]; 그리기(); 1"); await pg.wait_for_timeout(200)

      # ── 프로필 끄기 → 톱니가 루틴 상자로 ──
      await pg.click('.보고프로필 .톱니단추'); await pg.wait_for_timeout(400)
      await pg.click('.시트 .스위치[data-f="프로필"]'); await pg.wait_for_timeout(250)
      끔=await pg.evaluate("""(()=>{ const 루=document.querySelector('.보고루틴'), 톱=document.querySelector('.톱니단추'), 차=루?.querySelector('.보고루차');
        return {프로필:!!document.querySelector('.보고프로필'), 보임:S.설정.보고서보임, 톱안:톱?.parentElement?.classList.contains('보고루틴'), 루:__R(루), 톱:__R(톱), 차:__R(차), 스위치:document.querySelector('.시트 .스위치[data-f="프로필"]').classList.contains('켬')}; })()""")
      봄(t+f"ua0b 프로필 끄면 프로필 상자 없음 ({끔['보임']})", not 끔['프로필'] and 끔['보임']=={'프로필':False,'루틴':True} and not 끔['스위치'])
      봄(t+"ua0b 톱니가 루틴 상자 오른쪽 위로 · 볼륨 ▲ 와 안 겹침", 끔['톱안'] and abs(끔['톱']['r']-끔['루']['r'])<=1.5 and abs(끔['톱']['t']-끔['루']['t'])<=1.5 and (끔['차'] is None or not 겹(끔['톱'],끔['차'])))
      # 둘 다 끄기 안 됨
      await pg.click('.시트 .스위치[data-f="루틴"]'); await pg.wait_for_timeout(200)
      둘=await pg.evaluate("({보임:S.설정.보고서보임, 토:document.querySelector('.토스트')?.textContent, 루:!!document.querySelector('.보고루틴')})")
      봄(t+f"ua0b 마지막 하나는 안 꺼짐 · 토스트 ({둘})", 둘['보임']=={'프로필':False,'루틴':True} and 둘['토']=='하나는 보여야 합니다' and 둘['루'])
      await pg.click('.시트 .머리 .닫기'); await pg.wait_for_timeout(400)
      if 넓: await (await pg.query_selector('#폰')).screenshot(path=R10+'프로필끔_420.png')
      # 프로필 켜고 루틴 끄기
      await pg.click('.보고루틴 .톱니단추'); await pg.wait_for_timeout(400)
      await pg.click('.시트 .스위치[data-f="프로필"]'); await pg.wait_for_timeout(200)
      await pg.click('.시트 .스위치[data-f="루틴"]'); await pg.wait_for_timeout(200)
      await pg.click('.시트 .머리 .닫기'); await pg.wait_for_timeout(400)
      루끔=await pg.evaluate("({루:!!document.querySelector('.보고루틴'), 프:!!document.querySelector('.보고프로필'), 톱:document.querySelector('.톱니단추')?.parentElement?.classList.contains('보고프로필'), 수:document.querySelectorAll('.결과틀 .결과수:not(.큰수)').length, 보임:S.설정.보고서보임})")
      봄(t+f"ua0b 루틴 결과 끄면 루틴 상자 없음 · 톱니는 프로필 ({루끔})", not 루끔['루'] and 루끔['프'] and 루끔['톱'] and 루끔['수']==0 and 루끔['보임']=={'프로필':True,'루틴':False})
      if 넓: await (await pg.query_selector('#폰')).screenshot(path=R10+'루틴끔_420.png')
      await pg.evaluate("S.설정.보고서보임={프로필:true,루틴:true}; 그리기(); 1")

      # ── 옛 값 옮기기 ──
      옮=await pg.evaluate("""(()=>{ const 옛=JSON.stringify(S.설정), 결=[];
        for(const [끔,큰] of [[true,5],[false,4],[false,3]]){ delete S.설정.보고서보임; delete S.설정.큰운동추가; S.설정.보고서프로필끔=끔; S.설정.큰운동=큰; 새칸채움();
          결.push([JSON.stringify(S.설정.보고서보임), JSON.stringify(S.설정.큰운동추가), '보고서프로필끔' in S.설정, '큰운동' in S.설정]); }
        S.설정=JSON.parse(옛); return 결; })()""")
      봄(t+f"옛 값 옮기기 (끔+5대 / 4대 / 3대 → {옮})", 옮==[['{"프로필":false,"루틴":true}','["오버헤드 프레스","바벨 로우"]',False,False],['{"프로필":true,"루틴":true}','["오버헤드 프레스"]',False,False],['{"프로필":true,"루틴":true}','[]',False,False]])

      # ── 저장 → 저장된 보고서 (그 기록 앞과 견줌) ──
      await pg.click('[data-act="운동저장"]'); await pg.wait_for_timeout(300)
      키=await pg.evaluate("Object.keys(S.기록).filter(k=>k.startsWith(오늘())).sort().pop()")
      await pg.evaluate(f"U.업적띠=null; S.결과={{key:{json.dumps(키)}}}; 그리기(); 1"); await pg.wait_for_timeout(4300)
      저=await pg.evaluate(읽기JS)
      봄(t+f"저장된 보고서도 그 기록 앞과 견줌 ({[(c['이름'],c['값'],c['차']) for c in 저['칸']]})", [(c['이름'],c['값'],c['차']) for c in 저['칸']]==[('3대','362kg','▲23.3'),('스쿼트','140kg','▲23.3'),('벤치','81.7kg',''),('데드','140kg','')])
      await pg.click('[data-act="결과확인"]'); await pg.wait_for_timeout(250)

      # ── rvva 탭줄 · 프로필 탭 ──
      await pg.evaluate("U.업적띠=null; 그리기(); 1")
      탭=await pg.evaluate("""(()=>{ const bs=[...document.querySelectorAll('.탭줄 button')], p=bs[bs.length-1], 사=p.querySelector('.탭사진');
        return {글:bs.map(x=>x.innerText.trim()), 끝:p.dataset.t, 라벨:p.getAttribute('aria-label'), 글칸:p.querySelectorAll(':scope>span:not(.탭사진)').length, 사:__R(사), 단:__R(p), 모:사&&getComputedStyle(사).borderRadius,
          그림:!!사?.querySelector('svg'), 고리:사&&getComputedStyle(사).boxShadow, 메모:bs.some(x=>x.dataset.t==='메모')}; })()""")
      봄(t+f"rvva 탭줄 여섯째 = 프로필 사진 칸 (글자 없음 · {탭['글']})", 탭['끝']=='프로필' and 탭['라벨']=='프로필' and 탭['글'][-1]=='' and 탭['글칸']==0 and not 탭['메모'] and 탭['그림'])
      봄(t+f"rvva 동그란 사진 28 · 탭 높이 가운데 ({탭['사']['w']:.0f} · 위아래 차 {abs((탭['사']['t']+탭['사']['b'])/2-(탭['단']['t']+탭['단']['b'])/2):.1f})", round(탭['사']['w'])==28 and round(탭['사']['h'])==28 and 탭['모']=='50%' and abs((탭['사']['t']+탭['사']['b'])/2-(탭['단']['t']+탭['단']['b'])/2)<=1)
      봄(t+f"rvva 꺼져 있으면 고리 없음 ({탭['고리']})", 탭['고리']=='none')
      await pg.click('.탭줄 [data-t="프로필"]'); await pg.wait_for_timeout(400)
      pr=await pg.evaluate("""(()=>{ const q=s=>document.querySelector(s), 큰=q('.프로필큰'), 톱=큰?.querySelector('.톱니단추'), 사=q('.탭사진'), h1=q('.넘김>h1');
        return {탭:U.탭, h1:h1?.textContent, h1크기:h1&&getComputedStyle(h1).fontSize, 사진:__R(q('.넘김 .보고사진')), 입력:!!q('.넘김 input[data-pf="닉네임"]'),
          칸:[...큰.querySelectorAll('.큰수>div')].map(x=>x.innerText.replace(/\\n/g,'|')), 차:큰.querySelectorAll('.보고차').length, 큰:__R(큰), 톱:__R(톱), 값:[...큰.querySelectorAll('.큰값')].map(__R),
          메모:q('.프로필메모')?.innerText.replace(/\\n/g,'|'), 고리:getComputedStyle(사).boxShadow, 강조:__색('--강조'), 켬:q('.탭줄 .탭프로필').classList.contains('켬'),
          넘:__넘('.넘김 .보고상자,.넘김 .큰수>div,.넘김 .큰값,.넘김 .큰수>div>span,.프로필메모'), 가로:document.documentElement.scrollWidth>document.documentElement.clientWidth+1, 크기:__글자(q('.넘김'))}; })()""")
      if 넓: print('프로필 탭', json.dumps(pr, ensure_ascii=False)[:900])
      봄(t+f"rvva 프로필 탭 열림 · 제목 h1 '프로필' {pr['h1크기']}", pr['탭']=='프로필' and pr['h1']=='프로필' and pr['h1크기']=='22px')
      봄(t+f"rvva 켜지면 사진 둘레 2px --강조 고리 ({pr['고리']})", pr['켬'] and pr['강조'] in pr['고리'] and '0px 0px 0px 2px' in pr['고리'])
      봄(t+f"rvva 사진 58 · 닉네임 입력 칸", round(pr['사진']['w'])==58 and pr['입력'])
      봄(t+f"rvva 큰 운동 칸 = 보고서와 같은 칸 · ▲▼ 없음 · 오늘까지 ({pr['칸']})", pr['칸']==['362kg|3대','140kg|스쿼트','81.7kg|벤치','140kg|데드'] and pr['차']==0)
      봄(t+"rvva 톱니 = 큰 운동 상자 오른쪽 위 · 숫자와 안 겹침", pr['톱'] and abs(pr['톱']['r']-pr['큰']['r'])<=1.5 and abs(pr['톱']['t']-pr['큰']['t'])<=1.5 and not any(겹(pr['톱'],v) for v in pr['값']))
      봄(t+f"rvva 메모 줄 ({pr['메모']})", pr['메모'] and pr['메모'].startswith('메모'))
      봄(t+f"프로필 탭 잘림 · 넘침 없음 ({pr['넘']}) · 글자 크기 {sorted(pr['크기'])}", not pr['넘'] and not pr['가로'] and set(pr['크기'])<=글자크기)
      if 넓: await (await pg.query_selector('#폰')).screenshot(path=R10+'프로필탭_420.png'); 봄(t+"✎ 표시 사진(html2canvas) — 프로필 탭", await 표시사진(pg, 'h2c_프로필탭_420.png'))
      # 닉네임 → 동그라미 첫 글자 (다시 그리지 않고)
      await pg.fill('.넘김 input[data-pf="닉네임"]','홍겸'); await pg.wait_for_timeout(150)
      닉=await pg.evaluate("({S:S.설정.닉네임, 사:document.querySelector('.넘김 .보고사진').innerText.trim(), 탭:document.querySelector('.탭사진').innerText.trim(), 입:!!document.querySelector('.넘김 .보고사진 input[data-pf=\"사진\"]'), 초점:document.activeElement?.dataset?.pf})")
      봄(t+f"rvva 닉네임 치면 저장 · 사진 자리 첫 글자 · 입력 칸 그대로 ({닉})", 닉=={'S':'홍겸','사':'홍','탭':'홍','입':True,'초점':'닉네임'})
      # 톱니 → 같은 시트
      await pg.click('.프로필큰 .톱니단추'); await pg.wait_for_timeout(400)
      봄(t+"rvva 프로필 탭 톱니 → 같은 시트", await pg.evaluate("document.querySelector('.시트 .머리 b')?.textContent")=='결과 보고서 표시 방법')
      await pg.click('.시트 .큰운동칩줄 .칩[data-v="스내치"]'); await pg.wait_for_timeout(200)
      await pg.click('.시트 .머리 .닫기'); await pg.wait_for_timeout(400)
      sn=await pg.evaluate("[...document.querySelectorAll('.프로필큰 .큰수>div')].map(x=>x.innerText.replace(/\\n/g,'|'))")
      봄(t+f"스내치 — 종목 목록에 없어서 — ({sn})", sn[-1]=='—|스내치' and sn[0]=='362kg|4대')
      await pg.evaluate("S.설정.큰운동추가=[]; 그리기(); 1")
      # 메모 줄 → 메모 시트
      await pg.click('.프로필메모'); await pg.wait_for_timeout(400)
      메=await pg.evaluate("({종류:U.시트?.종류, 제목:document.querySelector('.시트 .머리 b')?.textContent, 입력:!!document.querySelector('.시트 [data-in=\"메모글\"]')})")
      봄(t+f"rvva 메모 줄 → 메모 시트 ({메})", 메=={'종류':'메모','제목':'메모','입력':True})
      if 넓: await (await pg.query_selector('#폰')).screenshot(path=R10+'메모시트_420.png')
      await pg.click('.시트 .머리 .닫기'); await pg.wait_for_timeout(400)
      # 사진 고르기 (프로필 탭에서) → 탭줄 · 보고서 모두
      await pg.set_input_files('.넘김 input[data-pf="사진"]', R10+'프로필.png'); await pg.wait_for_timeout(600)
      사=await pg.evaluate("({탭:!!document.querySelector('.넘김 .보고사진 img'), 줄:!!document.querySelector('.탭사진 img'), 저장:!!localStorage.getItem(저장키+'-프로필')})")
      봄(t+f"rvva 사진 눌러 바꾸기 → 프로필 탭 · 탭줄 사진 · 브라우저에 남음 ({사})", 사=={'탭':True,'줄':True,'저장':True})
      if 넓: await (await pg.query_selector('#폰')).screenshot(path=R10+'프로필탭_사진_420.png')

      # ── 설정 탭 — 세 줄 빠짐 ──
      await pg.click('.탭줄 [data-t="설정"]'); await pg.wait_for_timeout(300)
      설=await pg.evaluate("""(()=>{ const 표=[...document.querySelectorAll('.이름표')].map(x=>x.textContent), 글=document.querySelector('.넘김').innerText;
        return {표, 보고서:표.includes('운동 보고서'), 프로필줄:글.includes('보고서에 프로필 표시'), 큰:!!document.querySelector('[data-f="큰운동"]'), 닉:!!document.querySelector('.넘김 [data-pf="닉네임"]'),
          데이터앞:(()=>{ const d=[...document.querySelectorAll('.이름표')].find(x=>x.textContent==='데이터'); return d?.previousElementSibling?.previousElementSibling?.textContent; })(),
          구분:[...document.querySelectorAll('.넘김 .카드')].filter(c=>c.lastElementChild?.classList.contains('구분')||c.firstElementChild?.classList.contains('구분')).length}; })()""")
      봄(t+f"rvva 설정 탭에 프로필 표시 · 큰 운동 · 닉네임 없음 ({설['표']})", not 설['보고서'] and not 설['프로필줄'] and not 설['큰'] and not 설['닉'] and 설['데이터앞']=='운동 중 그림' and 설['구분']==0)
      봄(t+"JS 오류 없음", not 오류)
      if 오류: print(오류[:3])
      await pg.close()

    # ── 어두운 화면 ──
    pg=await b.new_page(viewport={'width':452,'height':1080}); 오류=[]; pg.on('pageerror',lambda e:오류.append(str(e)))
    await pg.goto(F); await pg.wait_for_timeout(600); await pg.evaluate(색JS)
    await pg.evaluate("document.documentElement.dataset.theme='dark'; try{localStorage.removeItem(저장키+'-프로필')}catch(e){}; 프로필사진=null; 1")
    await pg.evaluate(준비JS); await pg.evaluate("S.설정.닉네임='홍겸'; S.설정.큰운동추가=['오버헤드 프레스','바벨 로우']; 1"); await 끝화면(pg)
    d=await pg.evaluate(읽기JS)
    봄(f"[어둠] 루틴 상자 바탕 = 어두운 --면2 ({d['루바']}) · 테두리 --선", d['루바']==d['면2']=='rgb(23, 39, 56)' and d['루테색']==d['선'])
    봄(f"[어둠] ▲ = 어두운 --오름 ({d['칸'][0]['차색']})", d['칸'][0]['차색']==d['오름']=='rgb(229, 115, 104)')
    await (await pg.query_selector('#폰')).screenshot(path=R10+'어둠_보고서_420.png')
    봄("[어둠] ✎ 표시 사진(html2canvas) — 보고서", await 표시사진(pg, 'h2c_어둠_보고서_420.png'))
    await pg.click('.보고프로필 .톱니단추'); await pg.wait_for_timeout(450)
    await (await pg.query_selector('#폰')).screenshot(path=R10+'어둠_시트_420.png')
    await pg.click('.시트 .머리 .닫기'); await pg.wait_for_timeout(400)
    await pg.click('[data-act="운동저장"]'); await pg.wait_for_timeout(300); await pg.evaluate("U.업적띠=null; S.결과=null; U.탭='프로필'; 그리기(); 1"); await pg.wait_for_timeout(300)
    await (await pg.query_selector('#폰')).screenshot(path=R10+'어둠_프로필탭_420.png')
    봄("[어둠] JS 오류 없음", not 오류)
    await pg.close()
    await b.close()

  # ── 색 함수 color-mix 를 새로 쓰지 않음 (✎ 표시 사진이 color 함수를 못 읽는다) ──
  옛=open(SP+'7day-v9.html',encoding='utf-8').read(); 새=open(SP+파일,encoding='utf-8').read()
  봄(f"color-mix 새로 안 씀 (v9 {옛.count('color-mix')} → {새.count('color-mix')})", 새.count('color-mix')<=옛.count('color-mix'))
  [print(("✅ " if ok else "❌ ")+m) for m,ok in 결과]; print(f"\n통과 {sum(ok for _,ok in 결과)}/{len(결과)}")
asyncio.run(main())
