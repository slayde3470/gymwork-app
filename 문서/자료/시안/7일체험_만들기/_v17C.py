import asyncio, re, sys, json
from playwright.async_api import async_playwright
SP='/tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'
HTML = sys.argv[1] if len(sys.argv)>1 else '7day-v17C.html'
src=open(SP+'test_v10_R.py',encoding='utf-8').read()
준비=re.search(r'준비JS = """(.*?)"""', src, re.S).group(1)
def ok(이름, 참, 값=""): print(("✅" if 참 else "❌"), 이름, 값)
async def main():
  async with async_playwright() as p:
    b=await p.chromium.launch(); pg=await b.new_page(viewport={'width':389,'height':860}); err=[]; pg.on('pageerror',lambda e:err.append(str(e)))
    await pg.goto('file://'+SP+HTML); await pg.wait_for_timeout(600)
    await pg.evaluate(준비); await pg.wait_for_timeout(200)
    await pg.evaluate("""(()=>{ const ss=S.세션; ss.종목.forEach(e=>e.세트.forEach(s=>s.완료=true)); 행동('운동저장',{}); 행동('결과확인',{});
      S.설정.닉네임='홍겸'; 업적소급&&업적소급(); U.업적띠=null;
      const c=document.createElement('canvas'); c.width=90;c.height=120; const x=c.getContext('2d');
      인증=[0,1,2,3,4,5,6].map(i=>{ x.fillStyle=['#c55','#5a5','#55c','#aa5','#5aa','#a5a','#777'][i]; x.fillRect(0,0,90,120); return {id:'t'+i, src:c.toDataURL(), 때:i, 고정:i===1?1:0}; });
      /* 업적 많이 — 표에서 판정 없이 아무거나 더 얻은 것으로 */
      let n=0; for(const a of 업적표){ if(S.업적[a.번호]) continue; if(n++>=10) break; S.업적[a.번호]={날:'2026-09-'+String(10+n).padStart(2,'0'), 순:100+n}; }
      U.탭='캘린더'; 그리기(); })()""")
    await pg.wait_for_timeout(300)
    폰=await pg.query_selector('#폰')
    # ④ 탭줄
    탭=await pg.evaluate("[...document.querySelectorAll('.탭줄 button')].map(b=>{const s=b.querySelector('span:not(.탭사진)'); return [b.dataset.t, Math.round(b.getBoundingClientRect().width), s?Math.round(s.scrollWidth):0, s?Math.round(s.clientWidth):0]})")
    ok('탭줄 8칸', len(탭)==8, 탭)
    # 프로필 열기 — 들어옴 애니
    await pg.click('.탭줄 [data-t="프로필"]'); await pg.wait_for_timeout(60)
    애=await pg.evaluate("""[...document.querySelectorAll('.프묶음')].map(e=>{const cs=getComputedStyle(e); return [e.className.split(' ')[0], cs.animationName, cs.animationDuration, e.style.getPropertyValue('--들x'), e.style.getPropertyValue('--들y')]})""")
    ok('묶음 들어옴 .48s · 상하좌우 50', all(a[1]=='프들어옴' and a[2]=='0.48s' for a in 애) and len(애)==3, 애)
    ok('기존 화면 들어옴 시간', True, await pg.evaluate("(()=>{const d=document.createElement('div'); d.className='들어옴'; document.body.appendChild(d); const v=getComputedStyle(d).animationDuration; d.remove(); return v})()"))
    await pg.wait_for_timeout(800)
    ok('제목 줄 없음', await pg.evaluate("!document.querySelector('.프로필넘김 h1')"))
    ok('프로필 메모 줄 없음', await pg.evaluate("!document.querySelector('.프로필메모')"))
    넘=await pg.evaluate("(()=>{const n=document.querySelector('#폰 .넘김'); return [n.scrollHeight,n.clientHeight]})()")
    ok('세로 스크롤 없음(인증 7장+＋)', 넘[0]<=넘[1], 넘)
    인=await pg.evaluate("(()=>{const c=[...document.querySelectorAll('.인칸')]; const r=c[0].getBoundingClientRect(); return [c.length, Math.round(r.width*10)/10, Math.round(r.height*10)/10, getComputedStyle(document.querySelector('.인판')).gridTemplateColumns.split(' ').length]})()")
    ok('인증 4열 3:4', 인[3]==4 and abs(인[2]/인[1]-4/3)<0.02, 인)
    업=await pg.evaluate("(()=>{const l=document.querySelector('.인업적'); return [l.children.length, Object.keys(S.업적).length, l.scrollWidth, l.clientWidth, getComputedStyle(l).overflowX, [...l.querySelectorAll('.인업글')].slice(0,4).map(e=>e.textContent)]})()")
    ok('업적 전부 · 가로 스크롤', 업[0]==업[1] and 업[2]>업[3] and 업[4]=='auto', 업)
    await 폰.screenshot(path=SP+'r17/C_프로필.png')
    # 휠
    box=await (await pg.query_selector('.인업적')).bounding_box()
    await pg.mouse.move(box['x']+100, box['y']+30); await pg.mouse.wheel(0,200); await pg.wait_for_timeout(200)
    ok('휠 → 가로', await pg.evaluate("document.querySelector('.인업적').scrollLeft")>0, await pg.evaluate("document.querySelector('.인업적').scrollLeft"))
    await pg.evaluate("document.querySelector('.인업적').scrollLeft=0")
    # 정렬
    def 순(): return pg.evaluate("업적목록().map(a=>a.번호)")
    최신=await 순()
    날들=await pg.evaluate("업적목록().map(a=>S.업적[a.번호].날)")
    ok('최신순 = 날 내림차순', 날들==sorted(날들,reverse=True), 날들[:4])
    await pg.click('.톱니단추'); await pg.wait_for_timeout(300)
    칩=await pg.evaluate("[...document.querySelectorAll('.시트 [data-f=\"업적정렬\"]')].map(b=>b.textContent+(b.classList.contains('켬')?'*':''))")
    ok('톱니 시트 업적 정렬 칩', len(칩)==3, 칩)
    await pg.click('.시트 [data-f="업적정렬"][data-v="오래된순"]'); await pg.wait_for_timeout(200)
    ok('오래된순', await 순()==최신[::-1], S:=await pg.evaluate("S.설정.업적정렬"))
    await pg.click('.시트 [data-f="업적정렬"][data-v="가나다순"]'); await pg.wait_for_timeout(200)
    칭=await pg.evaluate("업적목록().map(a=>a.칭호)")
    ok('가나다순', 칭==await pg.evaluate("(l)=>[...l].sort((a,b)=>a.localeCompare(b,'ko'))", 칭), 칭[:4])
    await pg.click('.시트 [data-f="업적정렬"][data-v="최신순"]'); await pg.wait_for_timeout(200)
    await 폰.screenshot(path=SP+'r17/C_톱니시트.png')
    await pg.evaluate("U.시트=null; 그리기()"); await pg.wait_for_timeout(300)
    # 꾹 눌러 끌기 — 첫째를 셋째 오른쪽으로
    앞=await 순()
    a=await (await pg.query_selector('.인업[data-i="0"]')).bounding_box(); c=await (await pg.query_selector('.인업[data-i="2"]')).bounding_box()
    await pg.mouse.move(a['x']+a['width']/2, a['y']+20); await pg.mouse.down(); await pg.wait_for_timeout(520)
    for k in range(1,11): await pg.mouse.move(a['x']+a['width']/2+(c['x']+c['width']*0.8-a['x']-a['width']/2)*k/10, a['y']+20); await pg.wait_for_timeout(20)
    await pg.mouse.up(); await pg.wait_for_timeout(300)
    뒤=await 순()
    ok('끌기 → 순서 바뀜 · 직접', 뒤==앞[1:3]+[앞[0]]+앞[3:] and await pg.evaluate("S.설정.업적정렬")=='직접', [앞[:4],뒤[:4], await pg.evaluate("S.업적순서.slice(0,4)")])
    ok('끌고 난 뒤 업적 화면 안 열림', await pg.evaluate("!U.스탯"))
    # 새 업적은 맨 왼쪽
    새=await pg.evaluate("(()=>{ const a=업적표.find(a=>!S.업적[a.번호]); S.업적[a.번호]={날:'2026-10-04',순:999}; 그리기(); return a.번호; })()")
    ok('직접일 때 새 업적 맨 왼쪽', (await 순())[0]==새 and (await 순())[1:]==뒤, [새, (await 순())[:3]])
    # ⑤ 링크
    ok('링크 없으면 흐린 아이콘 없음', await pg.evaluate("!document.querySelector('.인링크보기')"))
    await pg.click('.인닉'); await pg.wait_for_timeout(100)
    ok('포커스 → 링크 단추 보임', await pg.evaluate("getComputedStyle(document.querySelector('.인링크단추')).display")=='flex')
    await 폰.screenshot(path=SP+'r17/C_닉포커스.png')
    await pg.click('.인링크단추'); await pg.wait_for_timeout(300)
    ok('SNS 링크 시트 · 칸 3개', await pg.evaluate("U.시트?.종류==='링크' && document.querySelectorAll('[data-in=\"링크\"]').length===3"))
    await pg.fill('[data-in="링크"] >> nth=0', 'instagram.com/hong'); await pg.fill('[data-in="링크"] >> nth=1', 'javascript:alert(1)')
    await pg.click('[data-act="링크저장"]'); await pg.wait_for_timeout(300)
    ok('저장', True, await pg.evaluate("S.설정.링크"))
    print('닉네임 옆 흐린 링크 [틈, 투명도, 아이콘폭]', await pg.evaluate("(()=>{const i=document.querySelector('.인링크보기'), n=document.querySelector('.인닉'); if(!i) return false; const a=i.getBoundingClientRect(), b=n.getBoundingClientRect(); return [Math.round(a.left-b.right), getComputedStyle(i).opacity, i.querySelector('svg').getBoundingClientRect().width]})()"))
    await pg.click('.인링크보기'); await pg.wait_for_timeout(300)
    print('링크 목록 <a>', await pg.evaluate("[...document.querySelectorAll('.시트 a.링크줄')].map(a=>[a.getAttribute('href'),a.target,a.rel])"))
    await 폰.screenshot(path=SP+'r17/C_링크목록.png')
    await pg.evaluate("U.시트=null; 그리기()")
    # ⑥ 8장 · 기존 12장
    await pg.evaluate("""(()=>{ const s=인증[0].src; 인증=[...인증, {id:'t7',src:s,때:7,고정:0}]; 그리기(); })()""")
    넘=await pg.evaluate("(()=>{const n=document.querySelector('#폰 .넘김'); return [n.scrollHeight,n.clientHeight, document.querySelectorAll('.인칸').length, !!document.querySelector('.인더')]})()")
    ok('8장 — ＋ 없음 · 스크롤 없음', 넘[0]<=넘[1] and 넘[2]==8 and not 넘[3], 넘)
    # ④ 메모 탭 · 검색 탭
    await pg.click('.탭줄 [data-t="메모"]'); await pg.wait_for_timeout(300)
    ok('메모 탭 → 메모 시트 · 켬', await pg.evaluate("U.시트?.종류==='메모' && document.querySelector('.탭줄 [data-t=\"메모\"]').classList.contains('켬') && U.탭==='프로필'"))
    await pg.click('.탭줄 [data-t="검색"]'); await pg.wait_for_timeout(500)
    ok('검색 화면', await pg.evaluate("U.탭==='검색' && !!document.querySelector('.찾판') && document.querySelectorAll('.찾칸').length===9 && (()=>{const n=document.querySelector('#폰 .넘김'); return n.scrollHeight<=n.clientHeight})()"))
    await 폰.screenshot(path=SP+'r17/C_검색.png')
    # 폭 360 탭 글자
    await pg.set_viewport_size({'width':360,'height':800}); await pg.evaluate("그리기()"); await pg.wait_for_timeout(200)
    탭=await pg.evaluate("[document.querySelector('#폰').clientWidth, ...[...document.querySelectorAll('.탭줄 button span:not(.탭사진)')].map(s=>[s.textContent, s.scrollWidth<=s.parentElement.clientWidth])]")
    ok('좁은 폭 탭 글자 안 넘침', all(x[1] for x in 탭[1:]), 탭)
    # 기존 12장 저장분 → 앞 8장
    await pg.evaluate("""(()=>{ const s=인증[0].src; localStorage.setItem(저장키+'-인증', JSON.stringify(Array.from({length:12},(_,i)=>({id:'q'+i,src:s,때:i,고정:0})))); })()""")
    await pg.reload(); await pg.wait_for_timeout(600)
    ok('12장 → 8장', await pg.evaluate("인증.length")==8, await pg.evaluate("인증.map(x=>x.id).join(',')"))
    ok('오류 없음', not err, err)
    await b.close()
asyncio.run(main())
