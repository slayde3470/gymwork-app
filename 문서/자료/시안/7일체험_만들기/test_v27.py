"""v27 돌아가며 칸(같이 세기) 확인 — 389×860 · 사진 4장 + 검사 3개
쓰는 법: python3 test_v27.py 7day-v27.html 사진폴더"""
import asyncio, sys, os
from playwright.async_api import async_playwright
F = 'file://' + os.path.abspath(sys.argv[1]); 폴더 = sys.argv[2] if len(sys.argv) > 2 else '.'

async def main():
    async with async_playwright() as p:
        b = await p.chromium.launch(); pg = await b.new_page(viewport={'width': 389, 'height': 860})
        오류 = []; pg.on('pageerror', lambda e: 오류.append(str(e)))
        await pg.goto(F); await pg.wait_for_timeout(500)
        await pg.evaluate("""(()=>{ const 종=n=>({이름:n, 세트:종목기본세트(n)});
          S.돌림들.push({id:'g하체', 차례:[[종('시티드 레그 컬'),종('레그 익스텐션')],[종('맨몸 스쿼트')],[종('레그 프레스')]], 다음:0});
          S.돌림들.push({id:'g가슴', 차례:[[종('벤치프레스')],[종('체스트 프레스 머신')],[종('펙 덱 플라이')]], 다음:0});
          S.돌림들.push({id:'g등', 차례:[[종('데드리프트'),종('랫풀다운'),종('시티드 케이블 로우')],[종('어시스티드 풀업 머신'),종('시티드 케이블 로우')],[종('펜들레이 로우'),종('랫풀다운')]], 다음:0});
          const 돌=id=>({이름:'돌아가며', 돌림id:id, 세트:[]});
          S.루틴들.push({id:'r가슴', 이름:'가슴 날', 휴식일:false, 자동생성:true, 종목:[종('인클라인 벤치프레스'), 종('딥스'), 돌('g가슴'), 돌('g하체')]});
          S.루틴들.push({id:'r등', 이름:'등 날', 휴식일:false, 자동생성:true, 종목:[종('바벨 슈러그'), 돌('g등'), 돌('g하체')]});
          U.탭='루틴'; U.루틴열림='r가슴'; U.루펼침.add(S.루틴들.at(-2).종목[3]); 그리기(); })()""")
        await pg.wait_for_timeout(200)
        await pg.screenshot(path=f'{폴더}/v27_1_루틴.png')
        든곳 = await pg.eval_on_selector('.돌림든곳', 'e=>e.textContent')
        # 등 날 넣기 시트 — 맨 위 돌아가며 칸
        await pg.evaluate("U.루틴열림='r등'; U.시트={종류:'종목넣기', 대상:undefined, 모드:'이 날만'}; U.방금=[]; U.칸고름='전체'; 그리기();")
        await pg.wait_for_timeout(200)
        칸수 = await pg.eval_on_selector_all('.넣기돌림 .돌림넣칸', 'es=>es.length')
        await pg.screenshot(path=f'{폴더}/v27_2_넣기.png')
        # 운동 시작 → 하체 칸 = 1차례(컬 + 익스텐션) · ⇄
        await pg.evaluate("U.시트=null; 운동시작('r등'); U.본=1; 그리기();"); await pg.wait_for_timeout(300)
        이름들 = await pg.evaluate("S.세션.종목.map(x=>x.이름)")
        await pg.screenshot(path=f'{폴더}/v27_3_운동.png')
        await pg.click('.운돌림'); await pg.wait_for_timeout(200)
        await pg.screenshot(path=f'{폴더}/v27_4_차례.png')
        결과 = [(f"들어간 루틴 표시 → {든곳}", '가슴 날' in 든곳 and '등 날' in 든곳),
                (f"넣기 시트 맨 위 돌아가며 칸 {칸수}개", 칸수 == 3),
                (f"운동 = 다음 차례 종목들로 펼침 {이름들}", 이름들 == ['바벨 슈러그','데드리프트','랫풀다운','시티드 케이블 로우','시티드 레그 컬','레그 익스텐션'])]
        for n, ok in 결과: print('✓' if ok else '✗', n)
        print('오류:', 오류 or '없음')
        await b.close()
asyncio.run(main())
