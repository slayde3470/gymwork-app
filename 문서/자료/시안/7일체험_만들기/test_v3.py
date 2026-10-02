import asyncio, json, sys
from playwright.async_api import async_playwright
F='file:///tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'+(sys.argv[1] if len(sys.argv)>1 else '7day-v3.html')
async def main():
  async with async_playwright() as p:
    b=await p.chromium.launch(); pg=await b.new_page(viewport={'width':420,'height':900})
    오류=[]; pg.on('pageerror',lambda e:오류.append('페이지: '+str(e)))
    await pg.goto(F); await pg.wait_for_timeout(500)
    결과=[]
    # ── 캘린더 ──
    넘침=await pg.evaluate("[...document.querySelectorAll('.칸날')].filter(c=>c.scrollHeight>c.clientHeight+1).map(c=>c.dataset.k)")
    결과.append((f"캘린더 칸 글씨 안 넘침 (넘친 칸 {len(넘침)})", not 넘침))
    줄=await pg.evaluate("document.querySelectorAll('.판 .예줄').length"); 종=await pg.evaluate("실제루틴(루틴(S.예정[오늘()])).종목.length")
    결과.append((f"날짜 판 종목 세로 목록 ({줄}줄 / 루틴 {종}종목)", 줄==종))
    잘림=await pg.evaluate("[...document.querySelectorAll('.판 .예줄 .숫')].filter(x=>x.scrollWidth>x.clientWidth+1).length")
    결과.append(("종목 줄 숫자가 안 잘림", 잘림==0))
    await pg.screenshot(path='v3_cal.png')
    # ── 운동 ──
    await pg.evaluate('운동시작("r1"); 그리기();'); await pg.wait_for_timeout(300)
    async def 자리():
      return await pg.evaluate("""(()=>{ const q=c=>{const e=document.querySelector(c); if(!e) return null; const r=e.getBoundingClientRect(); return [Math.round(r.top),Math.round(r.height)];};
        return {머리:q('.운머리'),그림:q('.배너'),세트:q('.운세트들'),띠:q('.운아래'),아래:q('.아랫줄'), 본:U.본, 이름:document.querySelector('.운이름')?.textContent}; })()""")
    async def 누름(sel): await pg.click(sel); await pg.wait_for_timeout(150)
    a=await 자리(); print("처음:", a)
    순서=a['머리'][0] < a['그림'][0] < a['세트'][0] < a['띠'][0] < a['아래'][0]
    결과.append(("배치: 종목 정보 → 그림 두 장 → 세트 → 종목 띠 → 단추", 순서))
    판수=await pg.evaluate("document.querySelectorAll('.배너 .그림판').length"); 결과.append((f"그림 두 장 ({판수})", 판수==2))
    결과.append(("이전 단추 있음 (첫 종목이라 꺼짐)", await pg.evaluate("!!document.querySelector('.아랫줄 [data-act=\"이전종목\"]')?.disabled")))
    await pg.screenshot(path='v3_work.png')
    await 누름('.운세트들 [data-act="체크"]'); b1=await 자리(); await pg.wait_for_timeout(1300); b2=await 자리()
    결과.append(("체크해도 화면 안 움직임", a['머리']==b1['머리'] and a['그림']==b1['그림'] and a['세트']==b1['세트'] and a['띠']==b1['띠']))
    결과.append(("휴식 도는 동안 안 움직임 (그림 칸 높이 고정)", b1==b2))
    본=b2['본']; n=await pg.evaluate("S.세션.종목[U.본].세트.length")
    for _ in range(n):
      left=await pg.query_selector_all('.운세트들 .체크:not(.켬)')
      if not left: break
      await left[0].click(); await pg.wait_for_timeout(120)
    c=await 자리(); 결과.append(("세트를 다 해도 종목이 저절로 안 바뀜", c['본']==본))
    await 누름('.아랫줄 [data-act="본종목"]'); d=await 자리(); 결과.append((f"다음 ({c['이름']} → {d['이름']})", d['본']==본+1 and d['아래'][0]==a['아래'][0]))
    await 누름('.아랫줄 [data-act="이전종목"]'); e=await 자리(); 결과.append(("이전", e['본']==본))
    칸수=await pg.evaluate("document.querySelectorAll('.운칸').length")
    await 누름(f'.운칸:nth-child({칸수})'); f=await 자리(); 결과.append((f"띠 마지막 칸 → {f['이름']}", f['본']==칸수-1))
    for sel in ['.운세트들 [data-act="체크"]']*3 + ['[data-act="세트더"]','[data-act="세트더"]','[data-act="세트빼기"]','.운세트들 [data-act="체크"]','.아랫줄 [data-act="이전종목"]','.운세트들 .체크.켬','.운세트들 .체크.켬','[data-act="세트빼기"]','.아랫줄 [data-act="본종목"]','[data-act="세트값"][data-d="1"]','.운세트들 [data-act="체크"]']:
      el=await pg.query_selector(sel)
      if el: await el.click(); await pg.wait_for_timeout(100)
    g=await 자리(); 결과.append(("뒤엉킨 조작 뒤에도 맨 위·맨 아래 자리 그대로", g['머리'][0]==a['머리'][0] and g['아래'][0]==a['아래'][0]))
    await 누름('.배너'); 결과.append(("그림 누르면 근육 시트", await pg.evaluate("!!document.querySelector('.시트 .큰몸 svg')")))
    await pg.click('.시트 [data-act="그림감추기"]'); await pg.wait_for_timeout(150)
    결과.append(("그림 감추기", await pg.evaluate("!document.querySelector('.배너')")))
    await 누름('[data-act="배너보기"]'); 결과.append(("그림 다시 보기", await pg.evaluate("!!document.querySelector('.배너')")))
    await 누름('.아랫줄 [data-act="끝내기"]'); r1=await pg.evaluate("!!S.세션&&S.세션.끝화면")
    await 누름('[data-act="운동으로"]'); await 누름('[data-act="세트더"]'); await 누름('.아랫줄 [data-act="끝내기"]')
    n0=await pg.evaluate("Object.keys(S.기록).length"); await 누름('[data-act="운동저장"]'); n1=await pg.evaluate("Object.keys(S.기록).length")
    결과.append(("끝내기 → 돌아가기 → 세트 더 → 저장", r1 and n1==n0+1))
    기줄=await pg.evaluate("document.querySelectorAll('.판 .예목록 .예줄').length"); 결과.append((f"기록 있는 날 판에도 종목 목록 ({기줄}줄)", 기줄>0))
    await pg.evaluate('운동시작("r1"); 그리기();'); await pg.wait_for_timeout(200)
    await 누름('.아랫줄 [data-act="끝내기"]'); m0=await pg.evaluate("Object.keys(S.기록).length")
    await 누름('[data-act="운동저장"]'); m1=await pg.evaluate("Object.keys(S.기록).length")
    결과.append(("체크 없이 저장 → 기록 안 남음", m1==m0))
    print(); [print(("✅ " if ok else "❌ ")+t) for t,ok in 결과]
    print(f"\n{sum(ok for _,ok in 결과)}/{len(결과)} · 오류:", 오류 or "없음")
    await b.close()
asyncio.run(main())
