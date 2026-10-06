"""v22 B 확인 — 새 종목 시트 ⑧ 그림 누름 팝업 · ⑨ 이름 바뀌면 근육 비움 · ⑩ 주동근 없으면 저장 막음 · ⑪ 팔굽혀펴기 = 가슴
쓰는 법: python3 _v22B.py HTML 사진폴더   (처음 화면에서 눌러 들어간다 · 사진 → 사진폴더/*.png)"""
import asyncio, sys, json, os
from playwright.async_api import async_playwright
HTML = os.path.abspath(sys.argv[1]); 사진 = sys.argv[2]; os.makedirs(사진, exist_ok=True)
결과 = {'통과': 0, '실패': []}
def 봄(이름, 조건, 값=None):
    if 조건: 결과['통과'] += 1
    else: 결과['실패'].append(f"{이름} → {값}")
    print(('  ✓ ' if 조건 else '  ✗ ') + 이름 + ('' if 값 is None else f"  {json.dumps(값, ensure_ascii=False)[:220]}"))

칸 = '#폰 [data-in="새종목"]'
상태 = "({이름:U.새종목, 칸:U.새종목칸, 근:U.새?{...U.새.근육}:null, 묶음:U.새?.묶음, 팝:U.새?.팝, 시트:U.시트?.종류||null})"
칠수 = "[...document.querySelectorAll('#폰 .새몸 path.몸근')].filter(p=>getComputedStyle(p).fill!=='rgb(214, 221, 217)').length"   # --근육(밝은 화면) 아닌 조각 수
토글 = "document.querySelector('#폰 .토스트')?.textContent||''"

async def 정함(pg, 이름, ms=250):
    await pg.fill(칸, 이름); await pg.click('#폰 .새찾기단추'); await pg.wait_for_timeout(ms)

async def main():
    async with async_playwright() as p:
        b = await p.chromium.launch()
        pg = await b.new_page(viewport={'width': 389, 'height': 860}); err = []
        pg.on('pageerror', lambda e: err.append(str(e)))
        await pg.goto('file://' + HTML); await pg.wait_for_timeout(600)
        await pg.evaluate("try{localStorage.clear()}catch(e){}")

        # ── ⑪ 기본 목록 ──
        x = await pg.evaluate("[S.종목표.find(t=>t.이름==='팔굽혀펴기')?.칸, S.종목표.find(t=>t.이름==='맨몸 스쿼트')?.칸, 종목사전.find(t=>t.이름==='팔굽혀펴기')?.칸, 종목사전.find(t=>t.이름==='맨몸 스쿼트')?.칸]")
        봄('⑪ 기본 S.종목표 · 사전: 팔굽혀펴기 = 가슴 · 맨몸 스쿼트 = 맨몸 그대로', x == ['가슴', '맨몸', '가슴', '맨몸'], x)

        # 종목 탭 → + 새 종목 만들기
        await pg.click('#폰 .탭줄 [data-t="종목"]'); await pg.wait_for_timeout(250)
        await pg.click('#폰 [data-act="새종목열기"]'); await pg.wait_for_timeout(400)
        봄('새 종목 시트 열림 · 빈 근육', (await pg.evaluate(상태))['근'] == {}, await pg.evaluate(상태))

        await 정함(pg, '푸시업')
        y = await pg.evaluate(상태)
        봄('⑪ 사전 고름(별칭 푸시업) → 팔굽혀펴기 · 카테고리 자동 = 가슴', y['이름'] == '팔굽혀펴기' and y['칸'] == '가슴', y)
        await 정함(pg, '맨몸 스쿼트')
        y = await pg.evaluate(상태)
        봄('⑪ 맨몸 스쿼트 → 카테고리 맨몸 그대로', y['칸'] == '맨몸', y)

        # ── ⑨ 이름이 바뀌면 비운다 · 정할 때 다시 채운다 ──
        await 정함(pg, '벤치프레스', 700)
        y = await pg.evaluate(상태); 벤치 = y['근']
        봄('⑨ 벤치프레스 정함 → 사전 근육(가슴 가운데 주동)', 벤치.get('chest_mid') == 'P', 벤치)
        await pg.click(칸); await pg.keyboard.press('End'); await pg.keyboard.type('x'); await pg.wait_for_timeout(700)
        y = await pg.evaluate(상태); n = await pg.evaluate(칠수)
        봄('⑨ 한 글자 더함 → 근육 비움 · 그림 칠 없음 · 시트 그대로 열림', y['근'] == {} and n == 0 and y['시트'] == '새종목', [y, n])
        await pg.keyboard.press('Backspace'); await pg.wait_for_timeout(100)
        await pg.click('#폰 .새찾기단추'); await pg.wait_for_timeout(300)
        y = await pg.evaluate(상태)
        봄('⑨ 다시 벤치프레스로 확인 → 사전 값 다시 채움', y['근'] == 벤치, y)
        await 정함(pg, '스쿼트', 300)
        y = await pg.evaluate(상태)
        봄('⑨ 사전 → 사전(벤치프레스 → 스쿼트) = 백 스쿼트 근육 · 가슴 없음', y['이름'] == '백 스쿼트' and y['근'].get('quads') == 'P' and 'chest_mid' not in y['근'], y)
        await 정함(pg, '레그프레스', 300)
        y = await pg.evaluate(상태)
        봄('⑨ 사전에 없는 이름(레그프레스) → 빈 채로(앞 종목 · 낱말 짐작 없음)', y['근'] == {} and y['칸'] is None, y)
        await 정함(pg, '벤치프레스', 300)
        await pg.fill(칸, '벤ㅊ'); await pg.wait_for_timeout(250)
        await pg.click('#폰 .새결과줄[data-v="벤치프레스"]'); await pg.wait_for_timeout(250)
        await 정함(pg, '다리를들어요', 300)
        y = await pg.evaluate(상태)
        봄('⑨ 사전 고름 뒤 사전에 없는 이름(다리를들어요) → 빈 채로', y['근'] == {}, y)

        # ── ⑩ 주동근 없으면 저장 막음 ──
        await pg.click('#폰 [data-act="새종목칸"][data-v="하체"]'); await pg.wait_for_timeout(200)
        개수 = await pg.evaluate("S.종목표.length")
        await pg.click('#폰 .새저장'); await pg.wait_for_timeout(150)
        t = await pg.evaluate(토글); y = await pg.evaluate(상태)
        봄('⑩ 주동근 없이 저장 → 막음 · 토스트 "근육 사진을 눌러서 목표 근육을 설정하세요"', t == '근육 사진을 눌러서 목표 근육을 설정하세요' and y['시트'] == '새종목' and await pg.evaluate("S.종목표.length") == 개수, [t, y['시트']])
        await (await pg.query_selector('#폰')).screenshot(path=사진 + '/1_저장막음.png')
        x = await pg.evaluate("(()=>{ const n=S.종목표.length, 새=U.새; U.새={...새, 근육:{}}; U.새종목='옛길'; 행동('종목만들기',{}); const m=S.종목표.length; U.새=새; 그리기(); return [n,m]; })()")
        봄('⑩ 옛 "종목만들기" 길도 주동근 없으면 안 만든다', x[0] == x[1], x)
        await pg.fill(칸, '다리를들어요'); await pg.click('#폰 .새찾기단추'); await pg.wait_for_timeout(250)

        # ── ⑧ 그림 누름 → 팝업 ──
        await pg.click('#폰 .새몸 path[data-act="새근육팝"][data-v="quads"] >> nth=0', force=True); await pg.wait_for_timeout(350)
        y = await pg.evaluate(상태)
        x = await pg.evaluate("""(()=>{ const 팝=document.querySelector('#폰 .새팝'); if(!팝) return null;
          return {제목:팝.querySelector('.새팝제목').textContent, 줄:[...팝.querySelectorAll('.새팝줄')].map(r=>r.querySelector('span').textContent), 누른:팝.querySelector('.새팝줄.누른 span')?.textContent,
            켬:[...팝.querySelectorAll('.새팝줄')].map(r=>r.querySelector('.칩.켬')?.textContent), 줄높:팝.querySelector('.새팝줄').getBoundingClientRect().height,
            누른바탕:getComputedStyle(팝.querySelector('.새팝줄.누른')).backgroundColor, 칩높:팝.querySelector('.칩').getBoundingClientRect().height,
            시트안:(()=>{ const a=팝.getBoundingClientRect(), s=document.querySelector('#폰 .시트').getBoundingClientRect(); return a.top>=s.top && a.bottom<=s.bottom+0.5 && a.left>=s.left && a.right<=s.right; })()}; })()""")
        봄('⑧ 그림(허벅지 앞) 누름 → 칠하지 않음 · 팝업 열림', y['근'] == {} and y['팝'] == 'quads' and x is not None, [y, x and x['제목']])
        봄('⑧ 팝업 = 하체 묶음 세부 부위 6줄 · 누른 줄 = 허벅지 앞 · --강조옅음 바탕', x and x['제목'] == '하체' and len(x['줄']) == 6 and x['누른'] == '허벅지 앞' and x['누른바탕'] == 'rgb(225, 233, 240)', x)
        봄('⑧ 줄마다 지금 값이 눌림(전부 빼기) · 칩 28 · 시트 안', x and all(k == '빼기' for k in x['켬']) and x['칩높'] == 28 and x['시트안'], x and [x['켬'], x['칩높'], x['시트안']])
        await pg.click('#폰 .새팝줄.누른 [data-v="P"]'); await pg.wait_for_timeout(700)
        y = await pg.evaluate(상태); n = await pg.evaluate(칠수)
        봄('⑧ [주동근] → 바로 그림 반영 · 팝업 그대로', y['근'] == {'quads': 'P'} and n > 0 and y['팝'] == 'quads' and await pg.query_selector('#폰 .새팝') is not None, [y['근'], n])
        await pg.click('#폰 .새팝줄.누른 [data-v="Y"]', force=True); await pg.wait_for_timeout(150)   # aria-disabled 라 force
        t = await pg.evaluate(토글); y = await pg.evaluate(상태)
        봄('⑧ 주동근 부위에 [협응근] → 막힘 · 토스트 · 값 그대로', y['근'] == {'quads': 'P'} and '이미 주동근' in t and await pg.evaluate("document.querySelector('#폰 .새팝줄.누른 .칩[data-v=\"Y\"]').classList.contains('막힘')"), [y['근'], t])
        await pg.click('#폰 [data-act="새팝역할"][data-k="hamstrings"][data-v="Y"]'); await pg.wait_for_timeout(300)
        await pg.click('#폰 [data-act="새팝역할"][data-k="glutes"][data-v="P"]'); await pg.wait_for_timeout(700)
        y = await pg.evaluate(상태)
        봄('⑧ 다른 줄 [협응근] · [주동근] → 반영 (허벅지 뒤 Y · 엉덩이 P)', y['근'] == {'quads': 'P', 'hamstrings': 'Y', 'glutes': 'P'}, y['근'])
        await pg.evaluate("document.getAnimations().forEach(a=>a.finish())")
        await (await pg.query_selector('#폰')).screenshot(path=사진 + '/2_팝업.png')
        await pg.click('#폰 [data-act="새팝역할"][data-k="glutes"][data-v="-"]'); await pg.wait_for_timeout(200)
        y = await pg.evaluate(상태)
        봄('⑧ [빼기] → 그 부위 뺌', 'glutes' not in y['근'], y['근'])
        await pg.click('#폰 .새팝제목'); await pg.wait_for_timeout(150)
        봄('⑧ 팝업 안 빈 곳 누름 → 안 닫힘', await pg.query_selector('#폰 .새팝') is not None)
        g = await pg.evaluate("(()=>{ const a=document.querySelector('#폰 .가림').getBoundingClientRect(), t=document.querySelector('#폰 .시트').getBoundingClientRect(); return [a.left+a.width/2, (a.top+t.top)/2]; })()")
        await pg.mouse.click(g[0], g[1]); await pg.wait_for_timeout(250)   # 시트 위 어두운 가림 = 팝업 바깥(시트 바깥)
        봄('⑧ 바깥(시트 위 가림) 누름 → 팝업만 닫힘 · 시트는 그대로', await pg.query_selector('#폰 .새팝') is None and (await pg.evaluate(상태))['시트'] == '새종목')
        await pg.click('#폰 .새몸 path[data-act="새근육팝"][data-v="calves"] >> nth=0', force=True); await pg.wait_for_timeout(300)
        r = await pg.evaluate("ㅁ=document.querySelector('#폰 .새찾기줄').getBoundingClientRect(), [ㅁ.left+ㅁ.width/2, ㅁ.top+ㅁ.height/2]")
        await pg.mouse.click(r[0], r[1]); await pg.wait_for_timeout(250)
        봄('⑧ 시트 안 팝업 바깥(이름 칸 자리) 누름 → 팝업만 닫힘 · 이름 칸 안 건드림', await pg.query_selector('#폰 .새팝') is None and (await pg.evaluate(상태))['시트'] == '새종목' and (await pg.evaluate(상태))['이름'] == '다리를들어요')
        await pg.click('#폰 .새몸 path[data-act="새근육팝"][data-v="chest_upper"] >> nth=0', force=True); await pg.wait_for_timeout(300)
        x = await pg.evaluate("[document.querySelector('#폰 .새팝제목')?.textContent, U.새.묶음]")
        await pg.click('#폰 .새팝 .버튼.주'); await pg.wait_for_timeout(250)
        봄('⑧ 가슴 조각 → 가슴 팝업 · 묶음 칩도 가슴 · [확인] → 닫힘', x == ['가슴', '가슴'] and await pg.query_selector('#폰 .새팝') is None, x)
        await pg.click('#폰 .칩[data-act="새근육"][data-v="chest_mid"]'); await pg.wait_for_timeout(200)
        y = await pg.evaluate(상태)
        봄('⑧ 그림 아래 부위 칩은 그대로 바로 토글(팝업 없음)', y['근'].get('chest_mid') == 'P' and await pg.query_selector('#폰 .새팝') is None, y['근'])

        await pg.wait_for_timeout(700); await (await pg.query_selector('#폰')).screenshot(path=사진 + '/4_팝업닫은뒤_그림.png')
        # 저장 → 다시 열면 빈 상태 · 같은 이름이면 저장된 근육
        저장근 = (await pg.evaluate(상태))['근']
        await pg.click('#폰 .새저장'); await pg.wait_for_timeout(500)
        봄('⑩ 주동근 있으면 저장됨', await pg.evaluate("S.종목표.some(t=>t.이름==='다리를들어요')") and (await pg.evaluate(상태))['시트'] is None)
        await pg.click('#폰 [data-act="새종목열기"]'); await pg.wait_for_timeout(60)
        n60 = await pg.evaluate(칠수)
        await pg.wait_for_timeout(500)
        y = await pg.evaluate(상태)
        봄('⑨ 연달아 새 종목 → 빈 상태(이름 · 근육 · 칸) · 그림이 앞 색에서 바래며 시작 안 함', y['근'] == {} and y['이름'] == '' and y['칸'] is None and n60 == 0 and y['팝'] is None, [y, n60])
        await (await pg.query_selector('#폰')).screenshot(path=사진 + '/3_다시열기빈.png')
        await 정함(pg, '다리를들어요', 300)
        y = await pg.evaluate(상태)
        봄('⑨ 저장된 같은 이름(사전에 없음) → 그 종목 근육 · 카테고리', y['근'] == 저장근 and y['칸'] == '하체', y)
        await pg.click('#폰 .시트 .닫기'); await pg.wait_for_timeout(400)

        # 편집 모드 — 이름을 바꿔도 근육 그대로
        k = await pg.evaluate("종목키(S.종목표.find(t=>t.이름==='벤치프레스'))")
        await pg.evaluate(f"행동('종목편집',{{v:{json.dumps(k, ensure_ascii=False)}}})"); await pg.wait_for_timeout(300)
        앞 = (await pg.evaluate(상태))['근']
        await pg.click(칸); await pg.keyboard.press('End'); await pg.keyboard.type('2'); await pg.wait_for_timeout(200)
        y = await pg.evaluate(상태)
        봄('⑨ 편집 모드: 이름이 바뀌어도 근육 그대로', y['근'] == 앞 and len(앞) > 0, [앞, y['근']])
        await pg.click('#폰 .시트 .닫기'); await pg.wait_for_timeout(400)

        # 루틴 화면 넣기 시트 경로에서도 같은 시트인지(열 수 있으면)
        await pg.click('#폰 .탭줄 [data-t="루틴"]'); await pg.wait_for_timeout(250)
        r = await pg.query_selector('#폰 [data-act="루틴열기"]')
        if r:
            await r.click(); await pg.wait_for_timeout(300)
            더 = await pg.query_selector('#폰 [data-act="시트"][data-t="종목넣기"]')
            if 더:
                await 더.click(); await pg.wait_for_timeout(400)
                await pg.click('#폰 [data-act="새종목열기"]'); await pg.wait_for_timeout(400)
                y = await pg.evaluate(상태)
                봄('루틴 → 종목 넣기 → 새 종목: 빈 상태', y['시트'] == '새종목' and y['근'] == {}, y)
                await 정함(pg, '다리를들어요2', 300); await pg.click('#폰 [data-act="새종목칸"][data-v="가슴"]'); await pg.wait_for_timeout(150)
                await pg.click('#폰 .새저장'); await pg.wait_for_timeout(150)
                봄('루틴 경로에서도 주동근 없으면 저장 막음', await pg.evaluate(토글) == '근육 사진을 눌러서 목표 근육을 설정하세요' and (await pg.evaluate(상태))['시트'] == '새종목')
            else: print('  · 루틴 화면에 종목 넣기 단추 못 찾음 — 건너뜀')
        else: print('  · 루틴 열기 단추 못 찾음 — 건너뜀')

        봄('pageerror 없음', not err, err)
        await b.close()
    print(f"\n통과 {결과['통과']} · 실패 {len(결과['실패'])}")
    for f in 결과['실패']: print('  ✗', f)

asyncio.run(main())
