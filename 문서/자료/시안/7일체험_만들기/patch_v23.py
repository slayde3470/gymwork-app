"""v23 — 10-06 홍겸 님: ② 보고서 루틴 상자 한 줄 ③ 보고서 ‹ › 25% 작게 · 굵게 ④ 이미지 찍기에 ‹ › 안 나오게(확인) ⑤ 루틴 화면 [+ 종목 추가] 오른쪽 아래 고정 · 자동 왼쪽 아래
쓰는 법: python3 patch_v23.py IN.html OUT.html"""
import sys
s = open(sys.argv[1], encoding="utf-8").read()
def 바꿈(old, new):
    global s
    assert s.count(old) == 1, (s.count(old), old[:80])
    s = s.replace(old, new)

# ⑤ 루틴 상세 — 목록 안 [+ 종목 추가](위 · 끝) 없앰 → 오른쪽 아래 떠 있는 단추. 자동은 왼쪽 아래
바꿈('''<label class="루자동 루자동뜸"><span>자동</span>${스위치(r.자동생성,"루틴자동","",r.id)}</label>${세트지움띠()}`; }''',
     '''<label class="루자동 루자동뜸"><span>자동</span>${스위치(r.자동생성,"루틴자동","",r.id)}</label>${r.휴식일?"":`<button class="버튼 주 루추가뜸" data-act="시트" data-t="종목넣기">+ 종목 추가</button>`}${세트지움띠()}`; }   /* 10-06 v23 ⑤ [+ 종목 추가] 오른쪽 아래 고정 · 자동 왼쪽 아래 */''')
바꿈('''  const 추가 = 곳=> `<button class="버튼 주 넓 루추가 ${곳}" data-act="시트" data-t="종목넣기">+ 종목 추가</button>`;
  return `${r.휴식일?`<div class="빈칸" style="margin-top:12px">휴식일</div>`:`${추가("위")}
    ${r.종목.length?`<div class="루격자">${r.종목.map((e,i)=>루틴종목상자(e,i)).join("")}</div>`:`<div class="빈칸">종목을 넣어 주세요</div>`}
    ${r.종목.length?추가("끝"):""}`}`; }''',
     '''  return `${r.휴식일?`<div class="빈칸" style="margin-top:12px">휴식일</div>`:`
    ${r.종목.length?`<div class="루격자" style="margin-top:12px">${r.종목.map((e,i)=>루틴종목상자(e,i)).join("")}</div>`:`<div class="빈칸" style="margin-top:12px">종목을 넣어 주세요</div>`}`}`; }   /* 10-06 v23 ⑤ 목록 안 [+ 종목 추가] 없음 — 오른쪽 아래 떠 있다 */''')
바꿈('.루자동뜸{position:absolute;right:12px;', '.루자동뜸{position:absolute;left:12px;')

# 끝 CSS
바꿈('''.화면>.결과틀.떠있음 .보고목록{padding-bottom:calc(40px + 12px)}
</style>''', '''.화면>.결과틀.떠있음 .보고목록{padding-bottom:calc(40px + 12px)}
/* ═══ 10-06 v23 ═══ */
/* ⑤ 루틴 상세 [+ 종목 추가] — 오른쪽 아래 12 에 떠 있다(자동과 같은 높이 40 · 모서리 16). 자동은 왼쪽 아래 */
.루추가뜸{position:absolute;right:12px;bottom:12px;z-index:4;height:40px;padding:0 16px;border-radius:16px}
/* ② 보고서 루틴 상자 = 한 줄 [루틴 이름 | 세트 | 총 볼륨 | 운동 시간] — 이름 칸도 수치 칸과 같은 세로선(--선)으로 나눈다 */
.보고상자.보고루틴{flex-direction:row;align-items:center;gap:0;padding:6px 8px}   /* .보고상자.보고루틴(1163)이 column 이라 같은 무게로 */
.보고루틴>.보고루틴머리{flex:0 1 32%;min-width:0;flex-direction:column;align-items:flex-start;gap:2px;padding-right:8px}
.보고루틴>.보고루틴머리>.채움{display:none}
.보고루틴>.보고루틴머리>.보고루틴이름{max-width:100%}
.보고루틴>.결과수{flex:1 1 0;min-width:0;align-self:stretch;align-items:center;border-left:1px solid var(--선)}
.보고상자.보고루틴.톱니있음{padding-right:32px}
.보고루틴.톱니있음 .보고루틴머리{padding-right:8px}
/* ③ 보고서 떠 있는 ‹ › — 25% 작게(40 → 30 · 화살표 18 → 13.5) · 굵게(선 2 → 3) */
.화면>.보고떠{width:30px;height:30px}
.화면>.보고떠 svg{width:13.5px;height:13.5px;stroke-width:3}
.화면>.결과틀.떠있음 .보고목록{padding-bottom:calc(30px + 12px)}
/* ④ 이미지로 찍을 때 ‹ › 는 안 나온다(찍는 틀 밖) — 혹시 들어와도 */
.보고찍틀 .보고떠{display:none}
</style>''')

open(sys.argv[2], "w", encoding="utf-8").write(s)
print("✓ v23 적용 →", sys.argv[2])
