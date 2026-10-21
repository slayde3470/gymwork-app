"""v26 돌아가는 종목 칸 확인 — 389×860 · 사진 3장 + 검사 3개
쓰는 법: python3 test_v26.py 7day-v26.html 사진폴더"""
import asyncio, sys, os
from playwright.async_api import async_playwright
F = 'file://' + os.path.abspath(sys.argv[1]); 폴더 = sys.argv[2] if len(sys.argv) > 2 else '.'

async def main():
    async with async_playwright() as p:
        b = await p.chromium.launch(); pg = await b.new_page(viewport={'width': 389, 'height': 860})
        오류 = []; pg.on('pageerror', lambda e: 오류.append(str(e)))
        await pg.goto(F); await pg.wait_for_timeout(500)
        # 가슴 날 루틴 하나를 만들어 연다
        await pg.evaluate("""(()=>{ const 세=n=>종목기본세트(n), 종=n=>({이름:n, 종id:n, 세트:세(n)});
          S.루틴들.push({id:'r시험', 이름:'가슴 날', 휴식일:false, 자동생성:true, 종목:[종('인클라인 벤치프레스'), 종('벤치프레스'), 종('딥스'), 종('사이드 레터럴 레이즈')]});
          U.탭='루틴'; U.루틴열림='r시험'; U.루펼침.add(S.루틴들.at(-1).종목[1]); 그리기(); })()""")
        await pg.wait_for_timeout(200)
        await pg.click('[data-act="돌림추가"]'); await pg.wait_for_timeout(200)
        await pg.screenshot(path=f'{폴더}/v26_1_시트.png')
        await pg.click('.넣기칸[data-v="덤벨 플라이"]'); await pg.wait_for_timeout(200)
        await pg.click('[data-act="돌림추가"]'); await pg.wait_for_timeout(200)
        await pg.click('.넣기칸[data-v="데드리프트"]'); await pg.wait_for_timeout(200)
        await pg.screenshot(path=f'{폴더}/v26_2_펼침.png')
        칩 = await pg.eval_on_selector_all('.돌림칩줄 .칩', 'es=>es.map(e=>e.textContent)')
        await pg.click('.돌림칩줄 .칩:nth-child(2)'); await pg.wait_for_timeout(150)
        켬 = await pg.eval_on_selector('.돌림칩줄 .칩.켬', 'e=>e.textContent')
        await pg.click('.돌림칸 .루접줄'); await pg.wait_for_timeout(150)   # 접기
        접힘 = await pg.eval_on_selector('.돌림칸', 'e=>e.textContent.replace(/\\s+/g," ").trim()')
        await pg.screenshot(path=f'{폴더}/v26_3_접힘.png')
        결과 = [(f"칩 3개 {칩}", len(칩) == 3 and '다음' in 칩[0]),
                (f"칩 누르면 그 종목 보기 → {켬}", 켬.startswith('덤벨 플라이')),
                (f"접힘 = 다음 종목 + 돌아가며 1/3 → {접힘}", '벤치프레스' in 접힘 and '돌아가며 1/3' in 접힘)]
        for n, ok in 결과: print('✓' if ok else '✗', n)
        print('오류:', 오류 or '없음')
        await b.close()
asyncio.run(main())
