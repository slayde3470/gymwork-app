"""10-03 ✎ 표시 9개 반영 시험 (headless Chromium) — python3 test_v5.py [파일]  기본 7day-v5.html"""
import asyncio, sys, json
from playwright.async_api import async_playwright
F='file:///tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad/'+(sys.argv[1] if len(sys.argv)>1 else '7day-v5.html')
async def main():
  결과=[]; 사진=[]
  def 봄(m,ok): 결과.append((m,bool(ok)))
  async with async_playwright() as p:
    b=await p.chromium.launch()
    for 폭,높이 in [(420,1080),(360,800)]:
      pg=await b.new_page(viewport={'width':폭,'height':높이}); 오류=[]; pg.on('pageerror',lambda e:오류.append(str(e)))
      await pg.goto(F); await pg.wait_for_timeout(5600); t=f"[{폭}] "
      async def 누름(sel): await pg.click(sel); await pg.wait_for_timeout(450)
      # ── 캘린더 날짜 판 ──
      칩=await pg.evaluate("[...document.querySelectorAll('.판 .예머리 .예칩 span')].map(x=>x.textContent)")
      봄(t+f"244w 칩 순서 세트 · 볼륨 · 예상 ({' | '.join(칩)})", len(칩)==3 and 칩[0].endswith('세트') and 칩[1].startswith('볼륨') and 칩[2].startswith('예상'))
      이름표=await pg.evaluate("[...document.querySelectorAll('.판 .판줄 .이름')].map(x=>x.textContent)")
      봄(t+f"h8d9 '예정' 이름표 없음 (남은 이름표 {이름표})", '예정' not in 이름표)
      줄=await pg.evaluate("""(()=>{const m=document.querySelector('.판 .예머리'); return {이름:m.querySelector('.예이름')?.textContent, 변경:!!m.querySelector('[data-act="변경"]')};})()""")
      봄(t+f"h8d9 루틴 이름 줄에 칩 · 변경 ({줄})", 줄['이름'] and 줄['변경'])
      아래=await pg.evaluate("""(()=>{const b=document.querySelector('.판 [data-act="시작"]'), 넘=document.querySelector('#폰 .넘김').getBoundingClientRect(), r=b.getBoundingClientRect(), 판=document.querySelector('.판').getBoundingClientRect();
        return {혼자:b.parentElement.children.length===1, 아래틈:Math.round(넘.bottom-r.bottom), 폭비:+(r.width/(판.width-24)).toFixed(2)};})()""")
      봄(t+f"h8d9 운동 시작은 맨 아래 한 줄 ({아래})", 아래['혼자'] and 0<=아래['아래틈']<=12 and 아래['폭비']>0.97)
      넘침=await pg.evaluate("(()=>{const 판=document.querySelector('.판'); return 판.scrollWidth>판.clientWidth+1})()")
      봄(t+"날짜 판 가로 넘침 없음", not 넘침)
      if 폭==420: await (await pg.query_selector('#폰')).screenshot(path='v5_캘린더.png')
      # 내일(휴식 아님) — 운동 시작 없음 · 변경은 이름 줄
      await pg.evaluate("(()=>{U.고른날=날더하기(오늘(),1); 그리기();})()"); await pg.wait_for_timeout(200)
      내=await pg.evaluate("({시작:!!document.querySelector('.판 [data-act=\"시작\"]'), 변경:!!document.querySelector('.판 .예머리 [data-act=\"변경\"]')})")
      봄(t+f"다른 날: 운동 시작 없음 · 변경은 이름 줄 ({내})", not 내['시작'] and 내['변경'])
      await pg.evaluate("(()=>{U.고른날=null; 그리기();})()")
      # ── 종목 탭 ──
      await pg.evaluate("(()=>{U.탭='종목'; 그리기();})()"); await pg.wait_for_timeout(300)
      글=await pg.evaluate("[...document.querySelectorAll('[data-act=\"플랜고치기\"]')].map(x=>x.textContent)")
      봄(t+f"g2lt 플랜 카드 단추 '변경' ({글})", 글 and all(x=='변경' for x in 글))
      처=await pg.evaluate("처방글(회처방(S.플랜들[0], 다음회(S.플랜들[0]).목표값, false))")
      봄(t+f"꾸준히 늘리기 처방 5세트 ({처})", '× 5세트' in 처)
      await 누름('[data-act="플랜고치기"][data-v="p1"]')
      머=await pg.evaluate("""(()=>{const m=document.querySelector('.시트 .머리'), cs=getComputedStyle(m), 루=getComputedStyle(document.documentElement);
        const 색=v=>{const d=document.createElement('i'); d.style.color=루.getPropertyValue(v); document.body.appendChild(d); const c=getComputedStyle(d).color; d.remove(); return c;};
        return {바탕:cs.backgroundColor, 강조:색('--강조'), 글:cs.color, 강조글:색('--강조글'), 오른:[...m.querySelectorAll('button')].map(x=>x.textContent)};})()""")
      봄(t+f"h8m2 시트 머리 = 띠 (강조 바탕 · 강조글)", 머['바탕']==머['강조'] and 머['글']==머['강조글'])
      봄(t+f"0ylt 머리 오른쪽 '훈련 방식 ›' ({머['오른']})", 머['오른']==['훈련 방식 ›'])
      시글=await pg.evaluate("document.querySelector('.시트').innerText")
      봄(t+"0ylt '1. 꾸준히 늘리기 · 4×10' 글씨 없음", '꾸준히' not in 시글 and '4×10' not in 시글)
      봄(t+"xlcr '주당' 줄 없음", '주당' not in 시글)
      끝=await pg.evaluate("""(()=>{const s=document.querySelector('.시트 [data-act="고침저장"]'), c=[...document.querySelectorAll('.시트 [data-act="시트닫기"]')].find(x=>!x.classList.contains('가림'));
        if(!s||!c) return null; const a=s.getBoundingClientRect(), z=c.getBoundingClientRect(); return {같은줄:Math.abs(a.top-z.top)<2, 저장왼쪽:a.right<=z.left, 닫기:c.textContent};})()""")
      봄(t+f"0ylt 아래 줄 [저장][닫기] ({끝})", 끝 and 끝['같은줄'] and 끝['저장왼쪽'])
      칩=await pg.evaluate("[...document.querySelectorAll('.시트 [data-act=\"고침목표방식\"]')].map(x=>x.textContent+(x.classList.contains('켬')?'*':''))")
      봄(t+f"6yzt 목표 [1RM][무게 × 횟수] ({칩})", 칩==['1RM*','무게 × 횟수'])
      if 폭==420: await (await pg.query_selector('#폰')).screenshot(path='v5_고침.png')
      await 누름('.시트 [data-act="고침목표방식"][data-v="회"]')
      v=await pg.evaluate("({방:U.고침.목표방식, w:U.고침.목표무게, r:U.고침.목표횟수, 칸:!!document.querySelector('.시트 [data-f=\"목표횟수\"]')})")
      봄(t+f"6yzt 무게 × 횟수로 바꾸면 같은 실력으로 환산 ({v})", v['방']=='회' and v['칸'] and v['r']==10 and abs(v['w']-75)<=2.5)
      await 누름('.시트 [data-act="고침저장"]')
      pp=await pg.evaluate("({방:S.플랜들[0].목표방식, w:S.플랜들[0].목표무게, r:S.플랜들[0].목표횟수, rm:Math.round(목표1RM(S.플랜들[0])), 시트:!!U.시트})")
      봄(t+f"6yzt 저장 → 플랜 목표 {pp['w']}kg × {pp['r']}회 (1RM 약 {pp['rm']})", pp['방']=='회' and pp['r']==10 and 95<=pp['rm']<=105 and not pp['시트'])
      await 누름('[data-act="플랜고치기"][data-v="p1"]'); await 누름('.시트 [data-act="고침목표방식"][data-v="RM"]')
      v=await pg.evaluate("({방:U.고침.목표방식, w:U.고침.목표무게})"); 봄(t+f"6yzt 다시 1RM ({v})", v['방']=='RM' and 95<=v['w']<=105)
      # 훈련 방식 시트
      await 누름('.시트 .머리 [data-t="방식"]'); 열=await pg.evaluate("U.시트?.종류"); 봄(t+f"0ylt '훈련 방식 ›' 누르면 방식 시트 ({열})", 열=='방식')
      속=await pg.evaluate("(()=>{const s=document.querySelector('.방식속'); return s?{글:s.innerText, 강도:!!s.querySelector('[data-act=\"강도\"]'), 수:!!s.querySelector('.수칸')}:null})()")
      봄(t+f"vypy 1번: '자유' · 강도 칩 · 세트/횟수 칸 없음, 5×8~15 ({속 and 속['글'].splitlines()})", 속 and '자유' not in 속['글'] and not 속['강도'] and not 속['수'] and '5×8~15' in 속['글'] and '총' not in 속['글'])
      넘=await pg.evaluate("[...document.querySelectorAll('.방식속 > *')].filter(x=>x.scrollWidth>x.clientWidth+1).map(x=>x.textContent.slice(0,20))")
      봄(t+f"vypy 방식 설명 한 줄에 들어감 (넘친 것 {넘})", not 넘)
      if 폭==420: await (await pg.query_selector('#폰')).screenshot(path='v5_방식.png')
      await 누름('.시트 .머리 [data-act="방식닫기"]'); 봄(t+"방식 닫기 → 고침으로", await pg.evaluate("U.시트?.종류")=='고침')
      await 누름('.시트 [data-act="시트닫기"].버튼')
      # ── 이중 진행 계산 ──
      q=await pg.evaluate("""(()=>{const p=S.플랜들[0], 표=플랜회표(p), 목=표.map(x=>회처방(p,x.목표값,false)[0]);
        let 좋=true, 이유=''; for(let i=1;i<목.length;i++){ const a=목[i-1], z=목[i];
          if(z.무게<a.무게){좋=false;이유='무게 내려감 '+i;break;}
          if(z.무게===a.무게 && z.횟수<a.횟수){좋=false;이유='같은 무게에서 횟수 줄어듦 '+i;break;}
          if(z.무게>a.무게 && !(a.횟수>=13 && z.횟수<=9)){좋=false;이유='무게 오를 때 15회 근처 → 8회 근처가 아님 '+i+' '+JSON.stringify([a,z]);break;} }
        const 범=목.every(x=>x.세트===5&&x.횟수>=8&&x.횟수<=15);
        const 첫=회처방({...p,시작1RM:p.시작1RM},p.시작1RM,false)[0];
        const 큰=[]; for(let v=85; v<=140; v+=5){ const x=이중진행(v,{시작1RM:85}); 큰.push(`${v}:${x.무게}×${x.횟수}`); }
        return {회:표.length, 좋, 이유, 범, 첫:`${첫.무게}×${첫.횟수}`, 처음:목.slice(0,3).map(x=>x.무게+'×'+x.횟수), 끝:목.slice(-3).map(x=>x.무게+'×'+x.횟수), 큰};})()""")
      봄(t+f"이중 진행: 모든 회 5세트 · 8~15회 ({q['회']}회)", q['범'])
      봄(t+f"이중 진행: 무게는 안 내려가고, 같은 무게에선 횟수만 오르고, 무게가 오를 때 15→8 ({q['이유'] or '처음 '+str(q['처음'])+' … 끝 '+str(q['끝'])})", q['좋'])
      봄(t+f"이중 진행: 시작 실력이면 8회 ({q['첫']})", q['첫'].endswith('×8'))
      if 폭==420: print('사다리 (1RM:무게×횟수, 시작 85):', ' '.join(q['큰']))
      # ── 종목넣기 시트 ──
      await pg.evaluate("(()=>{U.탭='루틴'; U.루틴열림='r1'; U.시트={종류:'종목넣기'}; 그리기();})()"); await pg.wait_for_timeout(500)
      닫=await pg.evaluate("[...document.querySelectorAll('.시트 .머리 button')].map(x=>x.textContent)")
      봄(t+f"hm83 종목넣기 시트 '다 했음' → '닫기' ({닫})", 닫==['닫기'])
      await pg.evaluate("(()=>{U.시트=null; U.루틴열림=null; U.탭='캘린더'; 그리기();})()")
      # 다른 시트도 같은 머리
      await pg.evaluate("(()=>{U.시트={종류:'변경',k:오늘()}; 그리기();})()"); await pg.wait_for_timeout(500)
      같=await pg.evaluate("getComputedStyle(document.querySelector('.시트 .머리')).backgroundColor===getComputedStyle(document.querySelector('.띠')||document.body).backgroundColor")
      봄(t+"h8m2 다른 시트(변경) 머리도 같은 띠", 같)
      봄(t+"JS 오류 없음", not 오류)
      if 오류: print(오류[:3])
      await pg.close()
    await b.close()
  [print(("✅ " if ok else "❌ ")+m) for m,ok in 결과]; print(f"\n{sum(ok for _,ok in 결과)}/{len(결과)}")
asyncio.run(main())
