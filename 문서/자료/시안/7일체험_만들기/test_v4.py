"""10-03 날짜 판 — 종목이 많을 때 접기 · 운동 시작 붙박이 시험 (headless Chromium)
   python3 test_v4.py [파일]   기본 7day-v4.html"""
import asyncio, sys
from playwright.async_api import async_playwright
F='file:///tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'+(sys.argv[1] if len(sys.argv)>1 else '7day-v4.html')
이름=['벤치프레스','인클라인 벤치프레스','오버헤드 프레스','사이드 레터럴 레이즈','딥스','케이블 플라이','페이스 풀','트라이셉스 푸시다운']
상태JS="""(()=>{ const 넘=document.querySelector('#폰 .넘김').getBoundingClientRect(), b=document.querySelector('.판 [data-act="시작"]');
  const r=b?b.getBoundingClientRect():null, 위=r?document.elementFromPoint(r.left+r.width/2, r.top+r.height/2):null;
  const 판=document.querySelector('.판');
  return {줄:document.querySelectorAll('.판 .예목록 .예줄').length, 접기:document.querySelector('.판 .예접기')?.textContent.trim()??null,
    펼:document.querySelector('.판 .예접기')?.getAttribute('aria-expanded')??null, 아이콘:!!document.querySelector('.판 .예접기 svg'),
    단추보임: !!r && r.top>=넘.top-1 && r.bottom<=넘.bottom+1, 위에있는것: 위? (b.contains(위)?'단추':위.className||위.tagName):null,
    가로넘침: 판.scrollWidth>판.clientWidth+1, 단추바탕:b?getComputedStyle(b.parentElement).backgroundColor:null,
    폰바탕:getComputedStyle(document.getElementById('폰')).backgroundColor, 범위:[...document.querySelectorAll('.판 .예줄 .숫')].map(x=>x.textContent).find(t=>t.includes('~'))||null }; })()"""
def 종목바꾸기(n): return f"""(()=>{{const r=루틴(S.예정[오늘()]); const 원=r.종목, 이름={이름!r};
  r.종목=이름.slice(0,{n}).map((nm,i)=>{{const e=JSON.parse(JSON.stringify(원[i%원.length])); e.이름=nm; delete e.플랜id;
    e.세트=Array.from({{length:6}},(_,k)=>({{...e.세트[0], w:(+e.세트[0].w||20)+k*2.5, r:12-k}})); return e;}}); 그리기();}})()"""
async def main():
  결과=[]
  async with async_playwright() as p:
    b=await p.chromium.launch()
    for 높이,색 in [(900,'light'),(1080,'light'),(900,'dark')]:
      pg=await b.new_page(viewport={'width':420,'height':높이}, color_scheme=색)
      오류=[]; pg.on('pageerror',lambda e:오류.append(str(e)))
      await pg.goto(F); await pg.wait_for_timeout(500)
      await pg.wait_for_timeout(5600)   # 업적 달성 아래띠는 5초 뒤 사라진다 (그동안은 맨 아래를 잠깐 덮는다 — 되돌리기 띠와 같은 성격)
      원본=await pg.evaluate("JSON.stringify(루틴(S.예정[오늘()]).종목)")
      t=f"[{높이}·{색}] "
      a=await pg.evaluate(상태JS); 결과.append((t+f"4종목: 4줄 · 접기 없음 · 단추 보임 ({a})", a['줄']==4 and a['접기'] is None and a['단추보임']))
      await pg.evaluate(종목바꾸기(5)); a=await pg.evaluate(상태JS); 결과.append((t+f"5종목: 5줄 다 · 접기 없음 (줄 {a['줄']})", a['줄']==5 and a['접기'] is None))
      await pg.evaluate(종목바꾸기(6)); a=await pg.evaluate(상태JS); 결과.append((t+f"6종목: 4줄 + '{a['접기']}'", a['줄']==4 and a['접기']=='외 2종목'))
      await pg.evaluate(종목바꾸기(8)); a=await pg.evaluate(상태JS)
      결과.append((t+f"8종목: 4줄 + '{a['접기']}' · 선 아이콘", a['줄']==4 and a['접기']=='외 4종목' and a['아이콘'] and a['펼']=='false'))
      결과.append((t+f"8종목: 운동 시작 보임 · 가려지지 않음 ({a['위에있는것']})", a['단추보임'] and a['위에있는것']=='단추'))
      결과.append((t+f"세트마다 무게가 다르면 범위 ({a['범위']})", bool(a['범위'])))
      if 높이==900 and 색=='light': await pg.screenshot(path='v4_접힘.png')
      await pg.click('.판 .예접기'); await pg.wait_for_timeout(300); a=await pg.evaluate(상태JS)
      결과.append((t+f"펼침: 8줄 + '{a['접기']}' · 뒤집힘", a['줄']==8 and a['접기']=='접기' and a['펼']=='true' and await pg.evaluate("document.querySelector('.판 .예접기 .접힘표').classList.contains('펼')")))
      결과.append((t+f"펼쳐도 운동 시작 보임 · 가려지지 않음 ({a['위에있는것']})", a['단추보임'] and a['위에있는것']=='단추'))
      결과.append((t+f"붙박이 줄 바탕 = 폰 바탕 ({a['단추바탕']})", a['단추바탕']==a['폰바탕']))
      await pg.evaluate("document.querySelector('#폰 .넘김').scrollTop=99999"); await pg.wait_for_timeout(100); a=await pg.evaluate(상태JS)
      결과.append((t+"맨 아래로 내려도 단추 그대로 · 가로 넘침 없음", a['단추보임'] and a['위에있는것']=='단추' and not a['가로넘침']))
      if 높이==900 and 색=='light': await pg.screenshot(path='v4_펼침.png')
      await pg.click('.판 .예접기'); await pg.wait_for_timeout(300); a=await pg.evaluate(상태JS)
      결과.append((t+f"접기: 다시 4줄 ('{a['접기']}')", a['줄']==4 and a['접기']=='외 4종목'))
      # 기록한 날도 같은 규칙
      await pg.evaluate("""(()=>{const k=Object.keys(S.기록)[0]; const r=S.기록[k]; const e0=r.종목[0];
        r.종목=Array.from({length:7},(_,i)=>({...JSON.parse(JSON.stringify(e0)), 이름:'종목'+(i+1)})); U.고른날=k.split('~')[0]; 그리기();})()""")
      줄=await pg.evaluate("document.querySelectorAll('.판 .예목록 .예줄').length"); 접=await pg.evaluate("document.querySelector('.판 .예접기')?.textContent.trim()")
      결과.append((t+f"기록 날 7종목: {줄}줄 + '{접}'", 줄==4 and 접=='외 3종목'))
      결과.append((t+"JS 오류 없음", not 오류))
      await pg.close()
    await b.close()
  [print(("✅ " if ok else "❌ ")+m) for m,ok in 결과]
  print(f"\n{sum(ok for _,ok in 결과)}/{len(결과)}")
asyncio.run(main())
