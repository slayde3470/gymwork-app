"""10-03 v8 C 시험 — 캘린더 ✎ 표시 vj5s · avk4 · s48f + 근육 기준 공식 (headless Chromium)
   python3 test_v8_C.py [파일]   기본 7day-v8C.html
   스크롤바 시험이 진짜가 되도록 Playwright 기본 '--hide-scrollbars' 를 끄고 띄운다 (켜 두면 스크롤바가 안 생겨 버그가 안 보인다)"""
import asyncio, sys, json
from playwright.async_api import async_playwright
이름 = sys.argv[1] if len(sys.argv)>1 else '7day-v8C.html'
F='file:///tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'+이름
색JS = """v=>{const d=document.createElement('i'); d.style.color=getComputedStyle(document.documentElement).getPropertyValue(v); document.body.appendChild(d); const c=getComputedStyle(d).color; d.remove(); return c;}"""
종이름=['벤치프레스','인클라인 벤치프레스','오버헤드 프레스','사이드 레터럴 레이즈','딥스','케이블 플라이','페이스 풀','트라이셉스 푸시다운',
  '덤벨 플라이','바벨 컬','해머 컬','레그 익스텐션','카프 레이즈','크런치']
def 종목바꾸기(n): return f"""(()=>{{const r=루틴(S.예정[오늘()]); if(!window.원종목) window.원종목=JSON.stringify(r.종목); const 원=JSON.parse(window.원종목), 이름={종이름!r};
  r.종목=이름.slice(0,{n}).map((nm,i)=>{{const e=JSON.parse(JSON.stringify(원[i%원.length])); e.이름=nm; delete e.플랜id;
    e.세트=Array.from({{length:3+i%3}},(_,k)=>({{...e.세트[0], w:(+e.세트[0].w||20)+(i%2?2.5:0), r:12-k}})); return e;}}); U.예펼침=null; 그리기();}})()"""
목록JS = """(()=>{ const 목=document.querySelector('.판 .예목록'); if(!목) return null; const 칸=[...목.children], q=e=>e.getBoundingClientRect();
  const 줄=칸.filter(e=>e.classList.contains('예줄')), 단=목.querySelector('.예접기'), cs=getComputedStyle(목);
  const xs=[...new Set(칸.map(e=>Math.round(q(e).left)))].sort((a,b)=>a-b);
  const 열=e=>xs.indexOf(Math.round(q(e).left));
  const 이름=줄.map(e=>e.querySelector('.예이름칸')), 곁=줄.map(e=>e.querySelector('.숫'));
  return {줄:줄.length, 단추:단?단.textContent.trim():null, 펼:단?.getAttribute('aria-expanded')??null, 열수:xs.length, 격자:cs.display, 흐름:cs.gridAutoFlow,
    왼쪽번호:줄.filter(e=>열(e)===0).map(e=>+e.querySelector('.번').textContent), 오른번호:줄.filter(e=>열(e)===1).map(e=>+e.querySelector('.번').textContent),
    단추자리: 단? {열:열(단), 끝:칸.at(-1)===단, 맨아래: 칸.filter(e=>열(e)===열(단)).every(e=>q(e).top<=q(단).top+1)} : null,
    여백:줄.length?getComputedStyle(줄[0]).paddingTop+'/'+getComputedStyle(줄[0]).paddingBottom:null,
    이름한줄:이름.every(e=>q(e).height<=20), 이름글자:이름[0]?getComputedStyle(이름[0]).fontSize:null, 이름말줄임:이름[0]?getComputedStyle(이름[0]).textOverflow:null,
    곁아래:줄.every((e,i)=>q(곁[i]).top>=q(이름[i]).bottom-1), 곁글자:곁[0]?getComputedStyle(곁[0]).fontSize:null,
    곁잘림:곁.filter(e=>e.scrollWidth>e.clientWidth+1||q(e).right>q(e.closest('.예줄')).right+1).length,
    칸넘침:칸.filter(e=>q(e).right>q(목).right+1||q(e).left<q(목).left-1).length,
    칸높이:[...new Set(줄.map(e=>Math.round(q(e).height)))], 단추높이:단?Math.round(q(단).height):null,
    곁글:곁.map(e=>e.textContent) }; })()"""
async def main():
  결과=[]
  def 봄(m,ok): 결과.append((m,bool(ok)))
  async with async_playwright() as p:
    b=await p.chromium.launch()
    b2=await p.chromium.launch(ignore_default_args=['--hide-scrollbars'])   # vj5s 만 — 진짜 스크롤바가 생기는 브라우저
    for 폭,높이 in [(420,1080),(360,800)]:
      pg=await b2.new_page(viewport={'width':폭,'height':높이}); 오류=[]; pg.on('pageerror',lambda e:오류.append(str(e)))
      await pg.goto(F); await pg.wait_for_timeout(1200); t=f"[{폭}] "
      await pg.evaluate("(()=>{U.업적띠=null; 그리기();})()"); await pg.wait_for_timeout(400)
      async def 사진(n): await (await pg.query_selector('#폰')).screenshot(path=f'v8C_{폭}_{n}.png')
      async def 넘침():
        return await pg.evaluate("(()=>{const 폰=document.getElementById('폰'); const 넘=[...폰.querySelectorAll('*')].filter(e=>{const r=e.getBoundingClientRect(), q=폰.getBoundingClientRect(); return r.width>0 && (r.right>q.right+1||r.left<q.left-1) && !e.closest('.가로밀기,.운띠,.사진줄');}).map(e=>e.className||e.tagName); return {문서:document.documentElement.scrollWidth>innerWidth, 넘:넘.slice(0,4)};})()")

      # ── vj5s 날짜를 눌러도 스크롤바가 안 생기고 달력이 안 밀린다 ──
      v=await pg.evaluate("""async ()=>{ const out=[]; const 날들=[...document.querySelectorAll('.칸날[data-k]')].map(e=>e.dataset.k).filter((_,i)=>i%2===0).slice(0,14);
        const x0=()=>Math.round(document.querySelector('.칸날[data-k]').getBoundingClientRect().left*10)/10, 처음=x0();
        for(const k of 날들){ document.querySelector(`.칸날[data-k="${k}"]`).click();
          for(let f=0;f<18;f++){ await new Promise(r=>requestAnimationFrame(r)); const 넘=document.querySelector('.캘넘김');
            out.push([넘.offsetWidth-넘.clientWidth, x0(), 넘.scrollHeight>넘.clientHeight]); } }
        const 칸들=[...document.querySelectorAll('#폰 .넘김')].map(e=>getComputedStyle(e).scrollbarWidth);
        U.고른날=null; 그리기(); return {처음, 바:[...new Set(out.map(x=>x[0]))], x:[...new Set(out.map(x=>x[1]))], 넘친적:out.some(x=>x[2]), 칸들}; }""")
      봄(t+f"vj5s 날짜 14개를 눌러 프레임마다 잼: 스크롤바 폭 {v['바']} · 달력 첫 칸 x {v['x']} (처음 {v['처음']}) · 그동안 넘친 적 {v['넘친적']}", v['바']==[0] and v['x']==[v['처음']])
      봄(t+f"vj5s 폰 안 넘김 칸 모두 scrollbar-width:none ({v['칸들']})", v['칸들'] and all(x=='none' for x in v['칸들']))
      봄(t+"vj5s 페이지 JS 오류 없음", not 오류); await pg.close()
      pg=await b.new_page(viewport={'width':폭,'height':높이}); 오류=[]; pg.on('pageerror',lambda e:오류.append(str(e)))
      await pg.goto(F); await pg.wait_for_timeout(1200)
      await pg.evaluate("(()=>{U.업적띠=null; 그리기();})()"); await pg.wait_for_timeout(400); await pg.evaluate("window.색="+색JS)

      # ── s48f 일요일 빨강 · 토요일 파랑 ──
      w=await pg.evaluate("""(()=>{ const c=e=>getComputedStyle(e).color, 요=[...document.querySelectorAll('.달력>.요일')];
        const 칸=[...document.querySelectorAll('.달력>.칸날[data-k]')], 요일=e=>new Date(e.dataset.k+'T12:00').getDay(), 수=e=>c(e.querySelector('.일'));
        const 일=칸.filter(e=>요일(e)===0&&!e.classList.contains('오늘')), 토=칸.filter(e=>요일(e)===6&&!e.classList.contains('오늘')), 평=칸.filter(e=>요일(e)%6!==0&&!e.classList.contains('오늘'));
        const 오=document.querySelector('.칸날.오늘 .일');
        return {머리:요.map(c), 머리글:요.map(e=>e.textContent).join(''), 일:일.every(e=>수(e)===색('--나쁨'))&&일.length>=4, 토:토.every(e=>수(e)===색('--강조'))&&토.length>=4,
          평:평.every(e=>수(e)===색('--글')), 일수:일.length, 토수:토.length, 오늘:오?[c(오)===색('--강조글'), getComputedStyle(오).backgroundColor===색('--강조')]:null,
          오늘요일:요일(document.querySelector('.칸날.오늘')), 고름:getComputedStyle(document.querySelector('.칸날.고름')).borderTopColor===색('--강조'),
          나쁨:색('--나쁨'), 강조:색('--강조'), 옅음:색('--옅음')}; })()""")
      봄(t+f"s48f 요일 머리 '일' 빨강(--나쁨) · '토' 파랑(--강조) · 나머지 --옅음 그대로", w['머리'][0]==w['나쁨'] and w['머리'][6]==w['강조'] and all(x==w['옅음'] for x in w['머리'][1:6]) and w['머리글']=='일월화수목금토')
      봄(t+f"s48f 일요일 숫자 빨강 ({w['일수']}칸) · 토요일 숫자 파랑 ({w['토수']}칸) · 평일은 --글 그대로", w['일'] and w['토'] and w['평'])
      봄(t+f"s48f 오늘(요일 {w['오늘요일']}) 숫자는 강조 바탕 · 강조글 그대로 · 고른 날 테두리 그대로 ({w['오늘']})", w['오늘']==[True,True] and w['고름'])
      놓=await pg.evaluate("""(()=>{ const e=[...document.querySelectorAll('.달력>.칸날[data-k]')].find(e=>new Date(e.dataset.k+'T12:00').getDay()===6&&!e.classList.contains('오늘'));
        e.classList.add('놓기'); const r=getComputedStyle(e.querySelector('.일')).color===getComputedStyle(e).color; e.classList.remove('놓기'); return r; })()""")
      봄(t+"s48f 끌어 놓는 칸(강조 바탕)에서는 토요일 숫자도 강조글 — 파랑 위 파랑 안 됨", 놓)
      await 사진('캘린더')

      # ── avk4 날짜 판 목록 ──
      a=await pg.evaluate(목록JS)
      봄(t+f"avk4 4종목: 두 칸 격자 · 왼쪽 {a['왼쪽번호']} 오른쪽 {a['오른번호']} · 접기 없음", a['격자']=='grid' and a['흐름'].startswith('column') and a['왼쪽번호']==[1,2] and a['오른번호']==[3,4] and a['단추'] is None)
      봄(t+f"avk4 줄 위아래 여백 8 → 4 ({a['여백']}) · 칸 높이 {a['칸높이']}", a['여백']=='4px/4px')
      봄(t+f"avk4 칸 = 이름 한 줄(13 · 말줄임) / 다음 줄 흐린 세트 글(11) ({a['곁글'][:2]})", a['이름한줄'] and a['이름글자']=='13px' and a['이름말줄임']=='ellipsis' and a['곁아래'] and a['곁글자']=='11px')
      칩=await pg.evaluate("[...document.querySelectorAll('.판 .예머리 .예칩 span')].length"); 시작=await pg.evaluate("""(()=>{const b=document.querySelector('.판 [data-act="시작"]'), 넘=document.querySelector('#폰 .넘김').getBoundingClientRect(), r=b.getBoundingClientRect(); return Math.round(넘.bottom-r.bottom);})()""")
      봄(t+f"avk4 날짜 띠 · 칩 줄({칩}) · 운동 시작(맨 아래 틈 {시작}) 그대로", 칩==3 and 0<=시작<=12 and await pg.evaluate("!!document.querySelector('.판 .판띠')"))
      await 사진('4종목')
      for n,왼,오,단 in [(10,[1,2,3,4,5],[6,7,8,9,10],None),(11,[1,2,3,4,5],[6,7,8,9],'외 2종목'),(14,[1,2,3,4,5],[6,7,8,9],'외 5종목'),(5,[1,2,3],[4,5],None)]:
        await pg.evaluate(종목바꾸기(n)); await pg.wait_for_timeout(100); a=await pg.evaluate(목록JS)
        봄(t+f"avk4 {n}종목: 왼쪽 {a['왼쪽번호']} · 오른쪽 {a['오른번호']} · 10번째 칸 '{a['단추']}' {a['단추자리']}",
          a['왼쪽번호']==왼 and a['오른번호']==오 and a['단추']==단 and (단 is None or (a['단추자리']['열']==1 and a['단추자리']['끝'] and a['단추자리']['맨아래'])))
        봄(t+f"avk4 {n}종목: 칸 안 넘침 · 세트 글 안 잘림 (넘침 {a['칸넘침']} · 잘림 {a['곁잘림']})", a['칸넘침']==0 and a['곁잘림']==0 and a['이름한줄'])
        if n==14:
          await 사진('14종목_접힘'); 높=a['단추높이']
          봄(t+f"avk4 '외 N종목' 칸 누르는 높이 {높} ≥ 32 · 다른 칸과 같은 줄 높이", 높>=32)
          await pg.click('.판 .예접기'); await pg.wait_for_timeout(350); a=await pg.evaluate(목록JS)
          봄(t+f"avk4 펼침: 14개 다 + '{a['단추']}' (aria-expanded {a['펼']}) · 왼쪽 {a['왼쪽번호']}", a['줄']==14 and a['단추']=='접기' and a['펼']=='true' and a['단추자리']['끝'] and a['왼쪽번호']==[1,2,3,4,5,6,7,8])
          await 사진('14종목_펼침')
          if 폭==360:
            s=await pg.evaluate("(()=>{const 넘=document.querySelector('.캘넘김'); 넘.scrollTop=9999; return [넘.scrollTop>0, 넘.scrollHeight>넘.clientHeight];})()")
            봄(t+f"vj5s 펼쳐서 넘쳐도 손가락 · 바퀴 스크롤은 됨 ({s})", s[0] and s[1])
            await 사진('14종목_펼침_아래')
            await pg.evaluate("document.querySelector('.캘넘김').scrollTop=0")
          await pg.click('.판 .예접기'); await pg.wait_for_timeout(350); a=await pg.evaluate(목록JS)
          봄(t+f"avk4 접기: 다시 9개 + '{a['단추']}'", a['줄']==9 and a['단추']=='외 5종목')
          n2=await 넘침(); 봄(t+f"avk4 14종목 가로 넘침 없음 ({n2})", not n2['문서'] and not n2['넘'])
      await pg.evaluate("(()=>{const r=루틴(S.예정[오늘()]); r.종목=JSON.parse(window.원종목); 그리기();})()")
      # 기록한 날 — 같은 모양
      r=await pg.evaluate("""(()=>{const k=Object.keys(S.기록)[0]; const r=S.기록[k]; const e0=r.종목[0];
        r.종목=Array.from({length:12},(_,i)=>({...JSON.parse(JSON.stringify(e0)), 이름:'종목'+(i+1)})); U.고른날=k.split('~')[0]; 그리기(); return k;})()""")
      a=await pg.evaluate(목록JS); 판=await pg.evaluate("!!document.querySelector('.판 .판줄 .날판목록')")
      봄(t+f"avk4 기록한 날 12종목도 같은 모양: 왼쪽 {a['왼쪽번호']} · 오른쪽 {a['오른번호']} · '{a['단추']}' · '{a['곁글'][0]}'", 판 and a['왼쪽번호']==[1,2,3,4,5] and a['오른번호']==[6,7,8,9] and a['단추']=='외 3종목' and '/' in a['곁글'][0])
      봄(t+f"avk4 기록 목록 칸 안 넘침 · 안 잘림 ({a['칸넘침']} · {a['곁잘림']})", a['칸넘침']==0 and a['곁잘림']==0)
      await 사진('기록날')
      await pg.evaluate("(()=>{U.고른날=null; 그리기();})()")
      상수=await pg.evaluate("typeof 날판열!=='undefined' && 날판열===2 && 날판칸===10")
      봄(t+"avk4 칸 수 상수 날판열 = 2 · 날판칸 = 10 (한 곳)", 상수)
      # 날판열 = 1 로 바꾸면 한 줄 목록 — const 라 다시 정의는 못 하므로 CSS 변수만 1 로 바꿔 모양을 본다
      한=await pg.evaluate("""(()=>{ const 목=document.querySelector('.판 .예목록'); 목.style.setProperty('--열','1'); 목.style.setProperty('--행', 목.children.length);
        const xs=new Set([...목.children].map(e=>Math.round(e.getBoundingClientRect().left))); const r=xs.size; 그리기(); return r; })()""")
      봄(t+f"avk4 --열 1 이면 한 줄 목록 (열 {한})", 한==1)

      # ── 근육 기준 공식 ──
      m=await pg.evaluate("""(()=>{ const 저장=JSON.stringify(S.기록); S.기록={};
        const 벤=[{이름:'벤치프레스', 세트:[{w:60,r:9,완료:true,휴:60},{w:60,r:9,완료:true,휴:60},{w:50,r:9,완료:false,휴:60}]}];
        const 최=부위최대세트(벤), 단=오늘단계(벤);
        const 하=[{이름:'레그 프레스', 세트:[{w:120,r:12,완료:true}]}];
        const 최하=부위최대세트(하);
        근육기준값.chest={볼륨:2160, 메모:''}; const 단넣=오늘단계(벤); 근육기준값.chest=null;
        근육기준설정.휴식력=2; const 단휴=오늘단계(벤); 근육기준설정.휴식력=1;
        const 이두=공식기준('upper_arm_front', 최);
        S.기록=JSON.parse(저장); const 기록최=부위최대세트([]);
        return {가슴최대:최.chest&&최.chest.값, 기준:공식기준('chest',최), 단계:단.chest_mid, 윗:단.chest_upper, 넣은값단계:단넣.chest_mid, 휴2단계:단휴.chest_mid, 이두,
          하체:하체부위.map(k=>최하[k]&&최하[k].값*10), 하체글:하체부위.join(','), 하체가슴:!!최하.chest,
          기록가슴:기록최.chest, 기록등:기록최.back, 데드:[...주동부위('데드리프트')], 카프:[...주동부위('카프 레이즈')], 맨스:[...주동부위('맨몸 스쿼트')], 턱:[...주동부위('턱걸이')]}; })()""")
      봄(t+f"공식: 벤치 60×9 = 540 이 가슴 최대 → 기준 {m['기준']} (5,400)", m['가슴최대']==540 and abs(m['기준']-5400)<1e-6)
      봄(t+f"공식: 오늘 가슴(chest_mid) 1,080 → 단계 {m['단계']} (4) · 보조(S 0.5) 윗가슴 {m['윗']} (2)", abs(m['단계']-4)<1e-9 and abs(m['윗']-2)<1e-9)
      봄(t+f"부위 칸에 넣은 값(2,160)이 우선 → 단계 {m['넣은값단계']} (10)", abs(m['넣은값단계']-10)<1e-9)
      봄(t+f"휴식력 2 → 기준 10,800 → 단계 {m['휴2단계']} (2)", abs(m['휴2단계']-2)<1e-9)
      봄(t+f"하체 5부위({m['하체글']}) = 하체 종목 전체 — 레그 프레스 120×12 하나로 다섯 모두 14,400 ({m['하체']}) · 가슴엔 안 들어감", m['하체']==[14400]*5 and not m['하체가슴'])
      봄(t+f"주동 종목 기록 없는 부위(이두)는 공식 0 → 옛 규칙 ({m['이두']})", m['이두']==0)
      봄(t+f"지난 기록 전부를 본다 — 가슴 {m['기록가슴']} · 등 {m['기록등']}", m['기록가슴'] and m['기록가슴']['값']==600 and m['기록등']['값']==600)
      print('   주동부위 — 데드', m['데드'], '· 카프', m['카프'], '· 맨몸 스쿼트', m['맨스'], '· 턱걸이', m['턱'])

      # 체험 막대 근육 기준 칸 — 휴식력 칸 · 공식 글 · 그림 반영
      await pg.evaluate("(()=>{행동('시작',{v:'r1'}); const e=S.세션.종목[0]; e.세트.forEach((s,i)=>{s.w=60;s.r=9;s.완료=i<2;}); S.세션.종목=[e]; S.기록={}; 그리기();})()")
      await pg.click('#기준단추'); await pg.wait_for_timeout(300)
      u=await pg.evaluate("""(()=>{ const 칸=document.getElementById('기준칸'), 휴=document.getElementById('휴식력칸'), 줄=document.querySelector('.기준줄[data-k="chest"]');
        const 첫=칸.querySelector('.기준설정'), 목록=document.getElementById('기준목록');
        return {보임:!칸.hidden, 휴:!!휴, 위:첫&&첫.getBoundingClientRect().bottom<=목록.getBoundingClientRect().top+1, 자리글:휴?.placeholder,
          가슴:줄.querySelector('.기준지금').innerText.replace(/\\n/g,' | '), 등:document.querySelector('.기준줄[data-k="back"] .기준지금').innerText.replace(/\\n/g,' | '),
          넘침:[...칸.querySelectorAll('*')].filter(e=>e.getBoundingClientRect().right>칸.getBoundingClientRect().right+1).length}; })()""")
      봄(t+f"근육 기준 칸 맨 위 '휴식력' 숫자 칸 (자리글 {u['자리글']}) · 부위 목록보다 위", u['보임'] and u['휴'] and u['위'] and u['자리글']=='1.0')
      봄(t+f"가슴 줄 흐린 글 '{u['가슴']}'", u['가슴'].startswith('공식 5,400 = 540 × 10 × 1.0') and '벤치프레스 60kg × 9회' in u['가슴'] and '오늘 1,080 · 4단계' in u['가슴'])
      봄(t+f"주동 기록 없는 등 줄 '{u['등']}'", u['등'].startswith('공식 없음'))
      봄(t+f"근육 기준 칸 안 넘침 ({u['넘침']})", u['넘침']==0)
      색0=await pg.evaluate("[...document.querySelectorAll('#폰 path.몸근')].map(e=>e.style.fill).filter(f=>!f.includes('var')).sort().join('|')")
      await pg.fill('#휴식력칸','2'); await pg.wait_for_timeout(200)
      u2=await pg.evaluate("""(()=>({설정:근육기준설정.휴식력, 가슴:document.querySelector('.기준줄[data-k="chest"] .기준지금').innerText.replace(/\\n/g,' | '),
        단:지금단계(S.세션.종목, Date.now()).chest_mid}))()""")
      색1=await pg.evaluate("[...document.querySelectorAll('#폰 path.몸근')].map(e=>e.style.fill).filter(f=>!f.includes('var')).sort().join('|')")
      봄(t+f"휴식력 2 → '{u2['가슴']}' · 단계 {u2['단']}", u2['설정']==2 and u2['가슴'].startswith('공식 10,800 = 540 × 10 × 2.0') and '2단계' in u2['가슴'])
      봄(t+f"휴식력 바꾸면 근육 그림 색이 바로 바뀜 ({색0!=색1})", 색0 and 색0!=색1)
      await pg.fill('#휴식력칸',''); await pg.wait_for_timeout(200)
      봄(t+"휴식력 칸 비우면 1.0", await pg.evaluate("근육기준설정.휴식력===1"))
      await pg.fill('.기준줄[data-k="chest"] .기준수','2160'); await pg.wait_for_timeout(200)
      u3=await pg.evaluate("document.querySelector('.기준줄[data-k=\"chest\"] .기준지금').innerText.replace(/\\n/g,' | ')")
      봄(t+f"부위 칸에 숫자 → '{u3}'", u3.startswith('넣은 값 우선 · 공식 5,400') and '10단계' in u3)
      await (await pg.query_selector('#기준칸')).screenshot(path=f'v8C_{폭}_기준칸.png')
      await pg.fill('.기준줄[data-k="chest"] .기준수',''); await pg.click('#기준단추')
      봄(t+"JS 오류 없음", not 오류)
      if 오류: print(오류[:3])
      await pg.close()

    # ── db: 휴식력은 thresholds/_settings 에 저장 · 다른 곳에서 바꾸면 읽어 온다 (window.claude 흉내) ──
    pg=await b.new_page(viewport={'width':420,'height':1080}); 오류=[]; pg.on('pageerror',lambda e:오류.append(str(e)))
    await pg.add_init_script("""(()=>{ window.__쓴=[]; window.__듣기={};
      const 묶음=name=>({ doc:id=>({ set:async d=>{ window.__쓴.push([name,id,JSON.parse(JSON.stringify(d))]); }, update:async d=>{ window.__쓴.push([name,id,d]); } }),
        add:async d=>({id:'x'}), where(){ return this; }, onSnapshot(cb){ (window.__듣기[name]=window.__듣기[name]||[]).push(cb); cb({docs:[], size:0}); return ()=>{}; } });
      window.claude={ use: async n=> n==='db' ? {collection:묶음} : Promise.reject(new Error('없음')) }; })()""")
    await pg.goto(F); await pg.wait_for_timeout(1200)
    await pg.evaluate("(()=>{U.업적띠=null; 그리기();})()"); await pg.click('#기준단추')
    await pg.fill('#휴식력칸','1.5'); await pg.wait_for_timeout(1100)
    쓴=await pg.evaluate("window.__쓴.filter(x=>x[0]==='thresholds')")
    봄(f"[db] 휴식력 1.5 → thresholds/_settings 에 저장 ({[(x[1],x[2].get('휴식력')) for x in 쓴]})", any(x[1]=='_settings' and x[2].get('휴식력')==1.5 for x in 쓴))
    await pg.evaluate("""(()=>{ const d=(id,x)=>({id, data:()=>x}); window.__듣기.thresholds.forEach(cb=>cb({docs:[d('_settings',{휴식력:3}), d('chest',{볼륨:null, 메모:'가슴운동 단일종목 중 최대 세트볼륨 X10 X휴식력'})]})); })()""")
    await pg.wait_for_timeout(200)
    r=await pg.evaluate("({휴:근육기준설정.휴식력, 칸:document.getElementById('휴식력칸').value, 메모:document.querySelector('.기준줄[data-k=\"chest\"] .기준메모').value})")
    봄(f"[db] 다른 곳에서 _settings 휴식력 3 → 칸 · 계산에 반영 ({r})", r['휴']==3 and r['메모'].startswith('가슴운동'))
    await pg.fill('.기준줄[data-k="chest"] .기준수','5000'); await pg.wait_for_timeout(1100)
    쓴=await pg.evaluate("window.__쓴.filter(x=>x[0]==='thresholds'&&x[1]==='chest').at(-1)")
    봄(f"[db] 부위 문서에 공식 · 최대세트 · 휴식력도 남김 ({ {k:쓴[2].get(k) for k in ['볼륨','지금기준','기준출처','공식','최대세트','휴식력']} })", 쓴 and 쓴[2]['볼륨']==5000 and 쓴[2]['기준출처']=='넣은 값' and 'max' not in 쓴[2] and 쓴[2]['휴식력']==3 and 쓴[2]['최대세트']['종목']=='벤치프레스')
    봄("[db] JS 오류 없음", not 오류)
    if 오류: print(오류[:3])
    await b.close(); await b2.close()
  [print(("✅ " if ok else "❌ ")+m) for m,ok in 결과]; print(f"\n{sum(ok for _,ok in 결과)}/{len(결과)}")
asyncio.run(main())
