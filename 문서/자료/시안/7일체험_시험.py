import asyncio, json
from playwright.async_api import async_playwright
F='file:///tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/7day-new.html'
async def main():
  async with async_playwright() as p:
    b=await p.chromium.launch(); pg=await b.new_page(viewport={'width':420,'height':900})
    오류=[]; pg.on('pageerror',lambda e:오류.append('페이지: '+str(e))); pg.on('console',lambda m: m.type=='error' and 오류.append('콘솔: '+m.text))
    await pg.goto(F); await pg.wait_for_timeout(500)
    keys=await pg.evaluate("Object.keys(S)")
    rid=await pg.evaluate("""(()=>{ const L=S.루틴들||S.루틴||[]; const r=(Array.isArray(L)?L:Object.values(L)).find(r=>!r.휴식일&&r.종목&&r.종목.length>=3)||(Array.isArray(L)?L:Object.values(L)).find(r=>!r.휴식일&&r.종목&&r.종목.length); return r&&r.id; })()""")
    print("S 열쇠:", keys[:14]); print("시작할 루틴:", rid)
    await pg.evaluate(f"운동시작({json.dumps(rid)}); 그리기();"); await pg.wait_for_timeout(300)
    async def 자리():
      return await pg.evaluate("""(()=>{ const q=c=>{const e=document.querySelector(c); if(!e) return null; const r=e.getBoundingClientRect(); return [Math.round(r.top),Math.round(r.height)];};
        return {머리:q('.운머리'),세트:q('.운세트들'),아래:q('.아랫줄'),몸:q('.운몸'),띠:q('.운띠'), 본:U.본, 이름:document.querySelector('.운이름')?.textContent}; })()""")
    async def 누름(sel): await pg.click(sel); await pg.wait_for_timeout(150)
    결과=[]
    a=await 자리(); print("처음:", a)
    await pg.screenshot(path='shot1.png')
    # ① 체크 → 휴식 시작 → 자리 그대로?
    await 누름('.운세트들 [data-act="체크"]'); b1=await 자리()
    await pg.wait_for_timeout(1300); b2=await 자리()
    clip=await pg.evaluate("document.querySelector('[data-쉼바]')?.style.clipPath||'없음'")
    결과.append(("체크해도 화면 안 움직임", a['머리']==b1['머리'] and a['세트']==b1['세트'] and a['아래']==b1['아래']))
    결과.append(("휴식 도는 동안 안 움직임", b1['머리']==b2['머리'] and b1['세트']==b2['세트']))
    결과.append((f"휴식 칸 줄어듦 (clip {clip})", clip!='없음'))
    await pg.screenshot(path='shot2.png')
    # ② 지금 종목 세트를 다 체크 → 다른 종목으로 저절로 안 넘어감?
    본=b2['본']; n=await pg.evaluate("S.세션.종목[U.본].세트.length")
    for _ in range(n):
      left=await pg.query_selector_all('.운세트들 .체크:not(.켬)')
      if not left: break
      await left[0].click(); await pg.wait_for_timeout(120)
    c=await 자리(); 결과.append(("세트를 다 해도 종목이 저절로 안 바뀜", c['본']==본))
    # ③ 다음 · 이전 · 띠 누르기 — 머리 자리 그대로?
    await 누름('[data-act="다음종목"]'); d=await 자리()
    결과.append((f"다음 종목으로 ({c['이름']} → {d['이름']})", d['본']==본+1 and d['머리'][0]==a['머리'][0]))
    await 누름('[data-act="이전종목"]'); e=await 자리(); 결과.append(("이전 종목으로", e['본']==본))
    칸수=await pg.evaluate("document.querySelectorAll('.운칸').length")
    await 누름(f'.운칸:nth-child({칸수})'); f=await 자리(); 결과.append((f"띠 마지막 칸 → {f['이름']}", f['본']==칸수-1))
    # ④ 비선형: 체크·풀기·풀기·세트 더·빼기·다른 종목 체크·되돌아가기
    for sel in ['.운세트들 [data-act="체크"]']*3 + ['[data-act="세트더"]','[data-act="세트더"]','[data-act="세트빼기"]','.운세트들 [data-act="체크"]','[data-act="이전종목"]','.운세트들 .체크.켬','.운세트들 .체크.켬','[data-act="세트빼기"]','[data-act="다음종목"]','[data-act="세트값"][data-d="1"]','.운세트들 [data-act="체크"]']:
      el=await pg.query_selector(sel)
      if el: await el.click(); await pg.wait_for_timeout(100)
    g=await 자리(); 결과.append(("뒤엉킨 조작 뒤에도 화면 칸 자리 그대로", g['머리'][0]==a['머리'][0] and g['아래'][0]==a['아래'][0]))
    # ⑤ 근육 시트
    await 누름('[data-act="몸크게"]'); 시트=await pg.evaluate("!!document.querySelector('.시트 .큰몸 svg')")
    줄=await pg.evaluate("document.querySelectorAll('.몸표 tbody tr').length")
    await pg.screenshot(path='shot3.png')
    결과.append((f"그림 누르면 근육 시트 (표 {줄}줄)", 시트))
    await pg.click('.시트 [data-act="그림감추기"]'); await pg.wait_for_timeout(150)
    숨=await pg.evaluate("!document.querySelector('.운몸')"); 결과.append(("그림 감추기", 숨))
    await 누름('[data-act="배너보기"]'); 결과.append(("그림 다시 보기", await pg.evaluate("!!document.querySelector('.운몸')")))
    # ⑥ 끝내기 → 돌아가기 → 세트 더 → 저장
    await 누름('.아랫줄 [data-act="끝내기"]'); r1=await pg.evaluate("!!S.세션&&S.세션.끝화면")
    await 누름('[data-act="운동으로"]'); await 누름('[data-act="세트더"]'); await 누름('.아랫줄 [data-act="끝내기"]')
    n0=await pg.evaluate("Object.keys(S.기록).length"); await 누름('[data-act="운동저장"]'); n1=await pg.evaluate("Object.keys(S.기록).length")
    결과.append(("끝내기 → 돌아가기 → 세트 더 → 저장", r1 and n1==n0+1))
    # ⑦ 22 버그 #4 — 체크 없이 저장하면 기록이 생기지 않아야
    await pg.evaluate(f"운동시작({json.dumps(rid)}); 그리기();"); await pg.wait_for_timeout(200)
    await 누름('.아랫줄 [data-act="끝내기"]'); m0=await pg.evaluate("Object.keys(S.기록).length")
    await 누름('[data-act="운동저장"]'); m1=await pg.evaluate("Object.keys(S.기록).length")
    토=await pg.evaluate("document.querySelector('.토스트')?.textContent||''")
    결과.append((f"체크 없이 저장 → 기록 안 남음 ('{토}')", m1==m0))
    print(); [print(("✅ " if ok else "❌ ")+t) for t,ok in 결과]
    print("\n오류:", 오류 or "없음 ✅")
    await b.close()
asyncio.run(main())
