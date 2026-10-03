"""10-03 밤 v9 C 시험 — 캘린더 ✎ 표시 xdlh · xz0l + 근육 기준 휴식력 저장 (headless Chromium)
   python3 test_v9_C.py [파일]   기본 7day-v9C.html   (사진은 지금 폴더에 — c9/ 안에서 돌린다)"""
import asyncio, sys
from playwright.async_api import async_playwright
이름 = sys.argv[1] if len(sys.argv)>1 else '7day-v9C.html'
F='file:///tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'+이름
색JS = """v=>{const d=document.createElement('i'); d.style.color=getComputedStyle(document.documentElement).getPropertyValue(v); document.body.appendChild(d); const c=getComputedStyle(d).color; d.remove(); return c;}"""
넘침JS = """(()=>{const 폰=document.getElementById('폰'); const q=폰.getBoundingClientRect(); const 넘=[...폰.querySelectorAll('*')].filter(e=>{const r=e.getBoundingClientRect(); return r.width>0 && (r.right>q.right+1||r.left<q.left-1) && !e.closest('.가로밀기,.운띠,.사진줄');}).map(e=>e.className||e.tagName); return {문서:document.documentElement.scrollWidth>innerWidth, 넘:넘.slice(0,4)};})()"""
# 오늘 기록 n 개를 만든다 — 첫 기록을 본떠 (루틴 id 는 지금 있는 루틴)
오늘기록JS = """n=>{ const 원=Object.values(S.기록)[0], r0=S.루틴들.find(r=>!r.휴식일);
  for(const k of Object.keys(S.기록)) if(k.split('~')[0]===오늘()) delete S.기록[k];
  for(let i=0;i<n;i++){ const x=JSON.parse(JSON.stringify(원)); x.루틴id=r0.id; x.이름=i?'한 번 더 '+r0.이름:r0.이름; x.예시=false; S.기록[i?`${오늘()}~${i+1}`:오늘()]=x; }
  U.고른날=오늘(); U.보는달=null; U.예펼침=null; 그리기(); return r0.id; }"""
글자거리JS = """const 글자거리=()=>{ const 띠=document.querySelector('.년월띠'), 화=[...띠.querySelectorAll('.달넘김')], b=띠.querySelector('.년월 b');
  const 글=e=>{ const r=document.createRange(); r.selectNodeContents(e); return r.getBoundingClientRect(); }, B=글(b);
  return [Math.round(B.left-글(화[0]).right), Math.round(글(화[1]).left-B.right)]; };"""
판JS = """(()=>{ const 판=document.querySelector('.판'), 넘=document.querySelector('.캘넘김').getBoundingClientRect(), q=e=>e.getBoundingClientRect();
  const 머=[...판.querySelectorAll('.록머리')], 줄=판.querySelector('.록단추'), 단=줄?[...줄.querySelectorAll('button')]:[];
  return { 이름표:[...판.querySelectorAll('.판줄 .이름')].map(e=>e.textContent), 낮은한번더:[...판.querySelectorAll('.버튼.낮')].map(e=>e.textContent),
    머리:머.map(m=>({이름:m.querySelector('.예이름')?.textContent, 알약:m.querySelector('.알약')?.textContent, 칩:[...m.querySelectorAll('.예칩 span')].map(e=>e.textContent),
      높이:Math.round(q(m).height), 칩줄수:new Set([...m.querySelectorAll('.예칩 span')].map(e=>Math.round(q(e).top))).size})),
    목록:판.querySelectorAll(':scope > .날판목록').length, 목록줄:판.querySelectorAll('.날판목록 .예줄').length,
    단추:단.map(e=>e.textContent), 단추높이:단.map(e=>Math.round(q(e).height)), 단추act:단.map(e=>e.dataset.act),
    아래틈:줄?Math.round(넘.bottom-q(줄).bottom):null, 맨끝:줄?판.lastElementChild===줄:null, 붙박이:줄?getComputedStyle(줄).position:null,
    삭제색:단.length?getComputedStyle(단.at(-1)).color:null, 한번더바탕:단.length>1?getComputedStyle(단[0]).backgroundColor:null,
    같은폭:단.length>1?Math.abs(q(단[0]).width-q(단[1]).width)<1:null, 시작:!!판.querySelector('[data-act="시작"]') }; })()"""
async def main():
  결과=[]
  def 봄(m,ok): 결과.append((m,bool(ok)))
  async with async_playwright() as p:
    b=await p.chromium.launch()
    pg=await b.new_page(viewport={'width':420,'height':1080}); await pg.goto(F.replace(이름,'7day-v8.html')); await pg.wait_for_timeout(1000)
    v8거리=await pg.evaluate(글자거리JS+"글자거리()"); await pg.close()
    for 폭,높이 in [(420,1080),(360,800)]:
      pg=await b.new_page(viewport={'width':폭,'height':높이}); 오류=[]; pg.on('pageerror',lambda e:오류.append(str(e)))
      await pg.goto(F); await pg.wait_for_timeout(1200); t=f"[{폭}] "
      await pg.evaluate("(()=>{U.업적띠=null; 그리기();})()"); await pg.wait_for_timeout(400); await pg.evaluate("window.색="+색JS)
      async def 사진(n): await (await pg.query_selector('#폰')).screenshot(path=f'v9C_{폭}_{n}.png')

      # ══ xz0l 띠 위 단추 = 흰 상자 · 파란 글씨 ══
      w=await pg.evaluate("""(()=>{ const 단=[...document.querySelectorAll('#폰 .작은흰')], cs=e=>getComputedStyle(e);
        return {수:단.length, 글:단.map(e=>e.textContent), 바탕:단.every(e=>cs(e).backgroundColor===색('--강조글')), 글자:단.every(e=>cs(e).color===색('--강조')),
          테:단.every(e=>cs(e).borderTopColor===색('--강조글')&&cs(e).borderTopWidth==='1px'), 띠위:단.every(e=>getComputedStyle(e.closest('.띠,.머리')).backgroundColor===색('--강조'))}; })()""")
      봄(t+f"xz0l 캘린더 띠 단추 {w['글']} = 바탕 --강조글 · 글 --강조 · 테두리 1px 같은 색 · 강조 띠 위", w['수']>=3 and w['바탕'] and w['글자'] and w['테'] and w['띠위'])

      # ══ xz0l ‹ › 1.5배 · 10px 더 벌림 · 띠 높이 그대로 ══
      d=await pg.evaluate(글자거리JS+"""(()=>{ const 띠=document.querySelector('.년월띠'), r=띠.getBoundingClientRect(), 화=[...띠.querySelectorAll('.달넘김')], 제=띠.querySelector('.년월글'), b=제.querySelector('b');
        const q=e=>e.getBoundingClientRect(), 가=(q(b).left+q(b).right)/2-(r.left+r.right)/2;
        return {띠높이:Math.round(r.height), 화크기:화.map(e=>[Math.round(q(e).width),Math.round(q(e).height),getComputedStyle(e).fontSize]),
          틈:[Math.round(q(제).left-q(화[0]).right), Math.round(q(화[1]).left-q(제).right)], 가운데:Math.round(가*10)/10, 글자거리:글자거리(),
          안:[...띠.children].every(e=>q(e).left>=r.left+11&&q(e).right<=r.right-11), 겹침:q(띠.children[0]).right<=q(화[0]).left && q(화[1]).right<=q(띠.lastElementChild).left,
          제목:[b.textContent, getComputedStyle(b).fontSize, getComputedStyle(b).fontWeight, getComputedStyle(제).color===색('--강조글'), 제.tagName, 제.dataset.act]}; })()""")
      화폭, 틈 = (40,12) if 폭==420 else (32,8)   # 화면 400 이하는 좁은 폰 규칙
      봄(t+f"xz0l ‹ › 누르는 칸 {화폭}×40 · 글자 28 ({d['화크기']}) · 띠 높이 {d['띠높이']} 그대로", d['화크기']==[[화폭,40,'28px']]*2 and d['띠높이']==40)
      봄(t+f"xz0l ‹ › 와 년월 사이 틈 {틈} ({d['틈']}) · 글자 사이 눈에 보이는 거리 {d['글자거리']} (v8 = {v8거리}) · 년월 한가운데 (어긋남 {d['가운데']})", d['틈']==[틈,틈] and abs(d['가운데'])<=2 and (폭!=420 or 8<=d['글자거리'][0]-v8거리[0]<=12))
      봄(t+f"xz0l 띠 안에 다 들어감 · [스탯] [업적] 과 안 겹침 ({d['안']} · {d['겹침']})", d['안'] and d['겹침'])
      봄(t+f"xz0l 년월 글자 = 단추(달고르기) · 모양 그대로 18 Bold 강조글 ({d['제목']})", d['제목'][1:]==['18px','700',True,'BUTTON','달고르기'])
      await 사진('캘린더')

      # ══ xz0l 달 고르기 시트 ══
      await pg.click('.년월글'); await pg.wait_for_timeout(450)
      m=await pg.evaluate("""(()=>{ const 시=document.querySelector('.시트'); if(!시) return null; const 머=시.querySelector('.머리'), 칸=[...시.querySelectorAll('.달칸')], q=e=>e.getBoundingClientRect();
        const xs=[...new Set(칸.map(e=>Math.round(q(e).left)))], ys=[...new Set(칸.map(e=>Math.round(q(e).top)))];
        const 고=시.querySelector('.달칸.고름'), 오=시.querySelector('.달칸.오늘');
        return {머:머.textContent.replace(/\\s+/g,' ').trim(), 머띠:getComputedStyle(머).backgroundColor===색('--강조'), 머높이:Math.round(q(머).height), 수:칸.length, 열:xs.length, 행:ys.length,
          글:칸.map(e=>e.textContent).join(','), 높이:[...new Set(칸.map(e=>Math.round(q(e).height)))],
          고:고?.textContent, 고현:고?.getAttribute('aria-current'), 고테:고?getComputedStyle(고).borderTopColor===색('--강조'):null, 고바탕:고?getComputedStyle(고).backgroundColor===색('--강조옅음'):null,
          오:오?.textContent, 오바탕:오?getComputedStyle(오.querySelector('span')).backgroundColor===색('--강조'):null,
          해화:[...머.querySelectorAll('.달넘김')].map(e=>Math.round(q(e).height)), 가운데:Math.round(((q(머.querySelector('.년월 b')).left+q(머.querySelector('.년월 b')).right)/2-(q(머).left+q(머).right)/2)*10)/10,
          안:칸.every(e=>q(e).right<=q(시).right+0.5&&q(e).left>=q(시).left-0.5), 오늘달:오늘().slice(0,7)}; })()""")
      봄(t+f"xz0l 년월 누르면 달 고르기 시트 — 머리 띠 '{m and m['머']}' (강조 · 높이 {m and m['머높이']} · 해 한가운데 {m and m['가운데']})", m and m['머띠'] and m['머'].startswith('‹2026년›') and m['머'].endswith('닫기') and m['머높이']==40 and abs(m['가운데'])<=2)
      봄(t+f"xz0l 1~12월 3×4 ({m and m['열']}×{m and m['행']}) · 칸 높이 {m and m['높이']} · 시트 안", m and m['수']==12 and m['열']==3 and m['행']==4 and m['높이']==[44] and m['글'].startswith('1월,2월') and m['안'])
      봄(t+f"xz0l 보는 달 '{m and m['고']}' 표시(강조 테두리 · 강조옅음 · aria-current) · 오늘 달 '{m and m['오']}' 강조 바탕", m and m['고']=='10월' and m['고현']=='true' and m['고테'] and m['고바탕'] and m['오']=='10월' and m['오바탕'])
      await 사진('달고르기')
      await pg.click('.시트 .달머리 [data-d="1"]'); await pg.wait_for_timeout(250)
      y=await pg.evaluate("({머:document.querySelector('.달머리 .년월 b').textContent, 고:!!document.querySelector('.달칸.고름'), 오:!!document.querySelector('.달칸.오늘')})")
      봄(t+f"xz0l › 누르면 다음 해 ({y['머']}) · 그 해엔 보는 달 · 오늘 달 표시 없음", y['머']=='2027년' and not y['고'] and not y['오'])
      await pg.click('.달칸[data-v="2027-03"]'); await pg.wait_for_timeout(450)
      g=await pg.evaluate("({보:U.보는달, 시트:!!document.querySelector('.시트'), 띠:document.querySelector('.년월띠 .년월 b').textContent, 오늘단추:!!document.querySelector('.판 .판띠 [data-act=\"오늘달\"]'), 첫칸:document.querySelector('.칸날[data-k]').dataset.k})")
      봄(t+f"xz0l 3월 누르면 그 달로 · 시트 닫힘 ({g})", g['보']=='2027-03' and not g['시트'] and g['띠']=='2027년 3월' and g['첫칸']=='2027-03-01' and g['오늘단추'])
      await pg.click('.년월글'); await pg.wait_for_timeout(400)
      h=await pg.evaluate("({머:document.querySelector('.달머리 .년월 b').textContent, 고:document.querySelector('.달칸.고름')?.dataset.v})")
      봄(t+f"xz0l 다시 열면 보는 달의 해 ({h})", h['머']=='2027년' and h['고']=='2027-03')
      for _ in range(1): await pg.click('.시트 .달머리 [data-d="-1"]'); await pg.wait_for_timeout(200)
      await pg.click('.달칸[data-v="2026-10"]'); await pg.wait_for_timeout(400)
      봄(t+"xz0l 이번 달을 고르면 '오늘 달' 로 돌아옴 (U.보는달 null · [오늘] 단추 없음)", await pg.evaluate("U.보는달===null && !document.querySelector('.판 .판띠 [data-act=\"오늘달\"]')"))
      await pg.click('.년월글'); await pg.wait_for_timeout(400); await pg.click('.시트 .달머리 .닫기'); await pg.wait_for_timeout(400)
      봄(t+"xz0l 닫기 → 시트 닫히고 달 그대로", await pg.evaluate("!document.querySelector('.시트') && U.보는달===null"))

      # ══ xdlh 기록한 날 — 오늘 ══
      rid=await pg.evaluate(오늘기록JS, 1); await pg.wait_for_timeout(300)
      a=await pg.evaluate(판JS)
      봄(t+f"xdlh '기록' 이름표 칸 없음 ({a['이름표']}) · 작은 [한 번 더] 없음 ({a['낮은한번더']})", a['이름표']==[] and a['낮은한번더']==[])
      mh=a['머리'][0] if a['머리'] else {}
      봄(t+f"xdlh 이름 줄 = 이름 · 알약 · 칩 3 ({mh}) — 예정 날과 같은 부품(.예머리 · .예칩) · 달성이면 'N세트'", len(a['머리'])==1 and mh['알약']=='달성' and len(mh['칩'])==3 and mh['칩'][0].endswith('세트') and '/' not in mh['칩'][0] and mh['칩'][1].startswith('볼륨'))
      봄(t+f"xdlh 종목 두 칸 목록이 판에 바로 ({a['목록']}개 · {a['목록줄']}줄)", a['목록']==1 and a['목록줄']>0)
      봄(t+f"xdlh 오늘 맨 아래 [한 번 더][운동 기록 삭제] ({a['단추']} · 높이 {a['단추높이']} · 같은 폭 {a['같은폭']})", a['단추']==['한 번 더','운동 기록 삭제'] and a['단추높이']==[40,40] and a['같은폭'])
      봄(t+f"xdlh 운동 시작 자리 = 판 맨 끝 붙박이(sticky) · 아래 틈 {a['아래틈']}", a['맨끝'] and a['붙박이']=='sticky' and 0<=a['아래틈']<=12)
      c=await pg.evaluate("({나쁨:색('--나쁨'), 강조:색('--강조')})")
      봄(t+f"xdlh [운동 기록 삭제] 글자 --나쁨 · [한 번 더] 주 단추(강조 바탕)", a['삭제색']==c['나쁨'] and a['한번더바탕']==c['강조'])
      봄(t+f"xdlh 이름 줄 칩이 한 줄 (줄 수 {mh.get('칩줄수')} · 높이 {mh.get('높이')})", mh.get('칩줄수')==1)
      n=await pg.evaluate(넘침JS); 봄(t+f"xdlh 가로 넘침 없음 ({n})", not n['문서'] and not n['넘'])
      await 사진('오늘기록')
      # 미달성 기록 — 세트 칩 '완료/전체' · 알약 미달성 · 넘침 없음
      u=await pg.evaluate("""(()=>{ const r=S.기록[오늘()]; const s0=r.종목[0].세트[0]; s0.완료=false; 그리기(); const m=document.querySelector('.판 .록머리');
        const 칩=[...m.querySelectorAll('.예칩 span')]; const out={알약:m.querySelector('.알약').textContent, 세트:칩[0].textContent, 넘:칩.filter(e=>e.getBoundingClientRect().right>m.getBoundingClientRect().right+1).length, 높이:Math.round(m.getBoundingClientRect().height)};
        s0.완료=true; 그리기(); return out; })()""")
      봄(t+f"xdlh 미달성 기록 = 알약 '{u['알약']}' · 세트 칩 '{u['세트']}' (완료/전체) · 안 넘침 (줄 높이 {u['높이']})", u['알약']=='미달성' and '/' in u['세트'] and u['넘']==0)
      # 같은 날 두 번('한 번 더')
      await pg.evaluate(오늘기록JS, 2); await pg.wait_for_timeout(300); a=await pg.evaluate(판JS)
      봄(t+f"xdlh 같은 날 기록 2개 → 이름 줄 · 목록이 차례로 ({[x['이름'] for x in a['머리']]} · 목록 {a['목록']}) · 단추 줄은 하나", len(a['머리'])==2 and a['목록']==2 and a['단추']==['한 번 더','운동 기록 삭제'] and a['맨끝'])
      await 사진('오늘기록2')
      # 삭제 → 되돌리기
      await pg.click('.판 [data-act="기록지움"]'); await pg.wait_for_timeout(350)
      x=await pg.evaluate("""(()=>{ const 띠=document.querySelector('#폰 .록지움띠'); return {남은:Object.keys(S.기록).filter(k=>k.split('~')[0]===오늘()).length, 띠:띠?띠.textContent:null,
        띠단:띠?.querySelector('button')?.textContent, 판시작:!!document.querySelector('.판 [data-act="시작"]'), 록:!!document.querySelector('.판 .록머리')}; })()""")
      봄(t+f"xdlh [운동 기록 삭제] → 묻지 않고 그날 기록 2개 다 지움 · 아래띠 '{x['띠']}'", x['남은']==0 and x['띠'] and '운동 기록을 지웠습니다' in x['띠'] and x['띠단']=='되돌리기' and not x['록'])
      봄(t+f"xdlh 지운 뒤 오늘 판 = 예정 판으로 (운동 시작 {x['판시작']})", x['판시작'] or not await pg.evaluate("!!S.예정[오늘()]"))
      await 사진('지움')
      await pg.click('.록지움띠 button'); await pg.wait_for_timeout(350)
      x=await pg.evaluate("({남은:Object.keys(S.기록).filter(k=>k.split('~')[0]===오늘()).sort(), 띠:!!document.querySelector('#폰 .록지움띠'), 머:document.querySelectorAll('.판 .록머리').length})")
      봄(t+f"xdlh [되돌리기] → 2개 다 돌아옴 ({x['남은']}) · 띠 사라짐 · 판에 다시 2개", len(x['남은'])==2 and not x['띠'] and x['머']==2)
      if 폭==420:
        await pg.click('.판 [data-act="기록지움"]'); await pg.wait_for_timeout(6500)
        봄(t+"xdlh 되돌리기 띠는 6초 뒤 사라지고 기록은 지운 채", await pg.evaluate("!document.querySelector('#폰 .록지움띠') && U.기록지움===null && !기록있음(오늘())"))
        await pg.evaluate(오늘기록JS, 1)
      # 지난 날 — [운동 기록 삭제] 만
      k=await pg.evaluate("(()=>{const k=Object.keys(S.기록).map(x=>x.split('~')[0]).filter(x=>x<오늘()).sort()[0]; U.보는달=k.slice(0,7)===오늘().slice(0,7)?null:k.slice(0,7); U.고른날=k; 그리기(); return k;})()")
      await pg.wait_for_timeout(300); a=await pg.evaluate(판JS)
      봄(t+f"xdlh 지난 날({k}) 맨 아래 [운동 기록 삭제] 하나 · 꽉 찬 폭 · 운동 시작 없음 ({a['단추']})", a['단추']==['운동 기록 삭제'] and a['맨끝'] and not a['시작'] and a['이름표']==[])
      w2=await pg.evaluate("(()=>{const 판=document.querySelector('.판').getBoundingClientRect(), b=document.querySelector('.록단추 button').getBoundingClientRect(); return Math.round(판.width-b.width);})()")
      봄(t+f"xdlh 지난 날 삭제 단추 = 판 폭 - 좌우 여백 24 ({w2})", w2==24)
      await 사진('지난날')
      await pg.click('.판 [data-act="기록지움"]'); await pg.wait_for_timeout(300)
      봄(t+"xdlh 지난 날 지우면 그 칸 비고 아래띠", await pg.evaluate(f"!기록있음('{k}') && !!document.querySelector('#폰 .록지움띠')"))
      await pg.click('.록지움띠 button'); await pg.wait_for_timeout(300)
      봄(t+"xdlh 지난 날 되돌리기 → 기록 돌아옴", await pg.evaluate(f"기록있음('{k}')"))
      # [한 번 더] → 같은 루틴으로 운동 시작
      await pg.evaluate("(()=>{U.보는달=null; U.고른날=오늘(); 그리기();})()"); await pg.wait_for_timeout(200)
      await pg.click('.판 .록단추 [data-act="시작"]'); await pg.wait_for_timeout(400)
      s=await pg.evaluate("({세션:!!S.세션, 루틴:S.세션?.루틴id, 탭:U.탭})")
      봄(t+f"xdlh [한 번 더] → 그날 마지막 기록의 루틴으로 운동 시작 ({s})", s['세션'] and s['루틴']==rid and s['탭']=='운동')
      # 운동 화면 띠 [그림] 도 같은 모양 (그림을 감췄을 때만 보인다)
      await pg.evaluate("(()=>{S.세션.배너숨김=true; 그리기();})()"); await pg.wait_for_timeout(300)
      g=await pg.evaluate("(()=>{const e=[...document.querySelectorAll('#폰 .작은흰')].find(x=>x.textContent==='그림'); return e?[getComputedStyle(e).backgroundColor===색('--강조글'), getComputedStyle(e).color===색('--강조')]:null;})()")
      봄(t+f"xz0l 운동 띠 [그림] 도 흰 상자 · 파란 글 ({g})", g==[True,True])
      if 폭==420: await 사진('운동띠')
      await pg.evaluate("(()=>{S.세션=null; U.탭='캘린더'; 그리기();})()")
      # 루틴 띠 [‹ 루틴] · 플랜 고치기 시트 [훈련 방식 ›]
      await pg.evaluate("(()=>{U.탭='루틴'; U.루틴열림=S.루틴들[0].id; 그리기();})()"); await pg.wait_for_timeout(300)
      r=await pg.evaluate("(()=>{const e=document.querySelector('.루띠 .작은흰'); return e?[e.textContent, getComputedStyle(e).backgroundColor===색('--강조글'), getComputedStyle(e).color===색('--강조')]:null;})()")
      봄(t+f"xz0l 루틴 띠 {r}", r and r[1] and r[2])
      if 폭==420: await 사진('루틴띠')
      p2=await pg.evaluate("(()=>{U.루틴열림=null; U.탭='플랜'; 그리기(); if(!S.플랜들.length) return null; 행동('플랜고치기',{v:S.플랜들[0].id}); 그리기(); const e=document.querySelector('.시트 .머리 .작은흰'); return e?[e.textContent, getComputedStyle(e).backgroundColor===색('--강조글'), getComputedStyle(e).color===색('--강조')]:'없음';})()")
      봄(t+f"xz0l 플랜 고치기 시트 머리 {p2} (플랜이 없으면 건너뜀)", p2 is None or p2=='없음' or (p2[1] and p2[2]))
      if 폭==420 and p2 not in (None,'없음'): await pg.wait_for_timeout(400); await 사진('고침머리')
      await pg.evaluate("(()=>{U.시트=null; U.탭='캘린더'; 그리기();})()")
      # 어둡게
      await pg.evaluate("document.documentElement.dataset.theme='dark'"); await pg.wait_for_timeout(700)
      dk=await pg.evaluate("""(()=>{ const 단=[...document.querySelectorAll('#폰 .작은흰')]; return {수:단.length, 바탕:단.every(e=>getComputedStyle(e).backgroundColor===색('--강조글')), 글:단.every(e=>getComputedStyle(e).color===색('--강조')),
        띠:getComputedStyle(document.querySelector('.년월띠')).backgroundColor===색('--강조'), 짙음:색('--강조글')}; })()""")
      봄(t+f"xz0l 어둡게: 단추 {dk['수']}개 바탕 {dk['짙음']}(--강조글) · 글 --강조 · 띠와 반대", dk['수']>=2 and dk['바탕'] and dk['글'] and dk['띠'])
      await pg.evaluate(오늘기록JS, 1); await pg.wait_for_timeout(300); await 사진('어둡게')
      await pg.click('.년월글'); await pg.wait_for_timeout(450); await 사진('어둡게_달고르기')
      await pg.evaluate("(()=>{U.시트=null; document.documentElement.dataset.theme='light'; 그리기();})()")
      n=await pg.evaluate(넘침JS); 봄(t+f"가로 넘침 없음 ({n})", not n['문서'] and not n['넘'])
      봄(t+"JS 오류 없음", not 오류)
      if 오류: print(오류[:3])
      await pg.close()

    # ══ 휴식력 저장 — 가짜 db (window.claude 흉내) ══
    async def 새쪽(set실패=None, 늦게=0, db없음=False):
      pg=await b.new_page(viewport={'width':420,'height':1080}); 오류=[]; pg.on('pageerror',lambda e:오류.append(str(e)))
      if not db없음:
        await pg.add_init_script("""(([실패, 늦게])=>{ window.__쓴=[]; window.__듣기={};
          const 묶음=name=>({ doc:id=>({ set:async d=>{ if(실패&&id===실패) throw {code:'invalid_argument', message:'막힘'}; window.__쓴.push([name,id,JSON.parse(JSON.stringify(d)),performance.now()]); }, update:async d=>{} }),
            add:async d=>({id:'x'}), where(){ return this; }, onSnapshot(cb){ (window.__듣기[name]=window.__듣기[name]||[]).push(cb); cb({docs:[], size:0}); return ()=>{}; } });
          window.claude={ use: async n=> { if(늦게) await new Promise(r=>setTimeout(r,늦게)); return n==='db' ? {collection:묶음} : null; } }; })""" + f"({[set실패 or '', 늦게]!r})".replace("'",'"'))
      await pg.goto(F); await pg.wait_for_timeout(300 if 늦게 else 1200)
      await pg.evaluate("(()=>{U.업적띠=null; 그리기();})()"); await pg.click('#기준단추')
      return pg, 오류
    pg,오류=await 새쪽()
    await pg.fill('#휴식력칸','1.5'); await pg.wait_for_timeout(1100)
    쓴=await pg.evaluate("window.__쓴.filter(x=>x[0]==='thresholds').map(x=>[x[1],x[2].휴식력])")
    표=await pg.evaluate("document.getElementById('휴저장').hidden")
    봄(f"[db] 휴식력 1.5 → thresholds/settings 에 저장 (밑줄 없는 id) · '저장 안 됨' 숨김 ({쓴} · {표})", 쓴==[['settings',1.5]] and 표)
    # change(칸을 떠남) → 0.7초 기다리지 않고 바로
    t0=await pg.evaluate("performance.now()"); await pg.fill('#휴식력칸','2'); await pg.press('#휴식력칸','Tab'); await pg.wait_for_timeout(150)
    쓴=await pg.evaluate("window.__쓴.filter(x=>x[0]==='thresholds').map(x=>[x[1],x[2].휴식력,Math.round(x[3])])")
    봄(f"[db] 칸을 떠나면(change) 바로 저장 — 0.15초 안 ({쓴[-1][:2]})", 쓴[-1][:2]==['settings',2] and 쓴[-1][2]-t0<300)
    await pg.wait_for_timeout(900)
    봄(f"[db] 바로 저장한 뒤 0.7초 타이머가 또 쓰지 않음 (쓴 수 {len(await pg.evaluate('window.__쓴'))})", len(await pg.evaluate("window.__쓴.filter(x=>x[0]==='thresholds')"))==2)
    # 읽기 — settings 먼저, 없으면 _settings
    await pg.evaluate("document.activeElement.blur()")
    async def 줌(docs):
      await pg.evaluate("""docs=>{ const d=(id,x)=>({id, data:()=>x}); window.__듣기.thresholds.forEach(cb=>cb({docs:docs.map(([i,x])=>d(i,x))})); }""", docs); await pg.wait_for_timeout(150)
      return await pg.evaluate("[근육기준설정.휴식력, document.getElementById('휴식력칸').value]")
    r1=await 줌([['_settings',{'휴식력':3}],['chest',{'볼륨':None,'메모':'가슴'}]])
    r2=await 줌([['_settings',{'휴식력':3}],['settings',{'휴식력':2.5}]])
    r3=await 줌([['settings',{'휴식력':None}]])
    봄(f"[db] 옛 _settings 만 있으면 그것 ({r1}) · 둘 다면 settings ({r2}) · settings 가 null 이면 1.0 ({r3})", r1==[3,'3'] and r2==[2.5,'2.5'] and r3==[1,''])
    봄(f"[db] 설정 문서를 부위 줄로 읽지 않음 · 메모 칸 반영", await pg.evaluate("!근육기준값.settings && !근육기준값._settings && document.querySelector('.기준줄[data-k=\"chest\"] .기준메모').value==='가슴'"))
    봄("[db] JS 오류 없음", not 오류); await pg.close()
    # 저장 실패 → '저장 안 됨'
    pg,오류=await 새쪽(set실패='settings')
    await pg.fill('#휴식력칸','1.2'); await pg.wait_for_timeout(1100)
    f=await pg.evaluate("(()=>{const e=document.getElementById('휴저장'), r=e.getBoundingClientRect(), 칸=document.getElementById('휴식력칸').getBoundingClientRect(), 판=document.getElementById('기준칸').getBoundingClientRect(); return {보임:!e.hidden&&r.width>0, 글:e.textContent, title:e.title, 옆:Math.abs((r.top+r.bottom)/2-(칸.top+칸.bottom)/2)<4, 안:r.right<=판.right+1, 흐림:getComputedStyle(e).opacity, 크기:getComputedStyle(e).fontSize, 값:근육기준설정.휴식력};})()")
    봄(f"[db] 저장이 막히면 칸 옆에 흐린 '저장 안 됨' ({f})", f['보임'] and f['글']=='저장 안 됨' and 'invalid_argument' in f['title'] and f['옆'] and f['안'] and float(f['흐림'])<1 and f['크기']=='11px' and f['값']==1.2)
    await (await pg.query_selector('#기준칸')).screenshot(path='v9C_저장안됨.png')
    봄("[db 실패] JS 오류 없음", not 오류); await pg.close()
    # 저장소가 없음(claude.ai 밖) → '저장 안 됨' · 계산은 됨
    pg,오류=await 새쪽(db없음=True)
    await pg.fill('#휴식력칸','2'); await pg.wait_for_timeout(1000)
    봄("[db 없음] 저장소가 없으면 '저장 안 됨' · 계산엔 2 반영", await pg.evaluate("!document.getElementById('휴저장').hidden && 근육기준설정.휴식력===2"))
    봄("[db 없음] JS 오류 없음", not 오류); await pg.close()
    # 저장소가 늦게 붙음 → 붙는 대로 저장 · '저장 안 됨' 사라짐
    pg,오류=await 새쪽(늦게=2500)
    await pg.fill('#휴식력칸','1.8'); await pg.wait_for_timeout(900)
    전=await pg.evaluate("!document.getElementById('휴저장').hidden")
    await pg.wait_for_timeout(2200)
    후=await pg.evaluate("({숨:document.getElementById('휴저장').hidden, 쓴:window.__쓴.filter(x=>x[0]==='thresholds').map(x=>[x[1],x[2].휴식력])})")
    봄(f"[db 늦음] 붙기 전엔 '저장 안 됨'({전}) → 붙으면 저장 {후['쓴']} · 표시 사라짐", 전 and 후['쓴']==[['settings',1.8]] and 후['숨'])
    봄("[db 늦음] JS 오류 없음", not 오류); await pg.close()
    await b.close()
  [print(("✅ " if ok else "❌ ")+m) for m,ok in 결과]; print(f"\n{sum(ok for _,ok in 결과)}/{len(결과)}")
asyncio.run(main())
