"""v24 — 10-06 홍겸 님: ① 보고서 루틴 상자 이름 칸 = 7글자 폭 · 상자 높이 5% 더 줄임 ② 보고서 종목 칸(요약) = 1RM · 볼륨 한 줄 · 오르내림은 화살표만
쓰는 법: python3 patch_v24.py IN.html OUT.html"""
import sys
s = open(sys.argv[1], encoding="utf-8").read()
def 바꿈(old, new):
    global s
    assert s.count(old) == 1, (s.count(old), old[:80])
    s = s.replace(old, new)

# ② 요약 칸 — 두 줄 [1RM 76kg ▼4kg] [볼륨 480kg ▼120kg] → 한 줄 [1RM 76kg▼ · 볼륨 480kg▼] (숫자 차이는 누르면 나오는 상세에)
바꿈('''      ${[줄1,줄2].map(([a,b,c])=>`<span class="보고줄"><span class="보고값"><i>${a}</i> ${b}</span>${c}</span>`).join("")}</button>`;''',
     '''      <span class="보고줄 보고한줄">${[줄1,줄2].map(([a,b,c])=>`<span class="보고값"><i>${a}</i> ${String(b).replace(/kg$/,"")}${보고화살(c)}</span>`).join("")}</span></button>`;   /* 10-06 v24 ② 한 줄 · 화살표만 · kg 은 뺀다(1RM · 볼륨은 kg — 상세에는 그대로) */''')
바꿈('''const 보고차 = (d, 단="kg") => {''',
     '''/* 10-06 v24 ② 요약 칸은 오르내림 화살표만 — 보고차() 가 만든 것에서 ▲/▼ 와 색만 남긴다(빈 글이면 그대로 빈 글) */
const 보고화살 = h => { const m=/class="보고차 (오름|내림)"/.exec(h||""); return m ? `<span class="보고차 보고화살 ${m[1]}" aria-label="${m[1]==="오름"?"올라감":"내려감"}">${m[1]==="오름"?"▲":"▼"}</span>` : ""; };
/* 10-06 v24 ② 한 줄이 칸보다 길면 글자를 0.5 씩 줄인다(13 → 최소 11) · 그린 뒤 한 번만 잰다 */
function 보고한줄맞춤(){ document.querySelectorAll("#폰 .보고한줄").forEach(줄=>{ 줄.style.fontSize=""; 줄.style.columnGap="";
  let f=parseFloat(getComputedStyle(줄).fontSize); if(줄.scrollWidth<=줄.clientWidth+0.5) return; 줄.style.columnGap="6px";
  while(f>11 && 줄.scrollWidth>줄.clientWidth+0.5){ f-=0.5; 줄.style.fontSize=f+"px"; } }); }
{ const 폰=document.getElementById("폰"); if(폰) new MutationObserver(()=>queueMicrotask(보고한줄맞춤)).observe(폰,{childList:true}); }
window.addEventListener("resize", 보고한줄맞춤);
const 보고차 = (d, 단="kg") => {''')

바꿈('''.보고찍틀 .보고떠{display:none}
</style>''', '''.보고찍틀 .보고떠{display:none}
/* ═══ 10-06 v24 ═══ */
/* ① 루틴 이름 칸 = 이름 글자(13 Bold) 7자 폭 + 오른쪽 8 → 세로선이 이름 바로 뒤. 더 길면 … */
.보고루틴>.보고루틴머리{flex:0 0 auto;width:calc(13px * 7 + 8px);font-size:13px}
/* ① 상자 높이 5% 더 줄임 — 위아래 안 여백 6 → 4 */
.보고상자.보고루틴{padding-top:4px;padding-bottom:4px}
/* ② 요약 칸 한 줄 — [1RM 76kg▼]  [볼륨 480kg▼] · 칸이 좁으면 글자 사이만 좁힌다(줄은 안 바꿈) */
.보고줄.보고한줄{flex-wrap:nowrap;gap:0 8px;justify-content:flex-start}
.보고한줄>.보고값{min-width:0;flex:none}
.보고한줄 .보고값 i{font-size:.85em}
.보고화살{margin-left:2px;font-size:10px}
</style>''')

open(sys.argv[2], "w", encoding="utf-8").write(s)
print("✓ v24 적용 →", sys.argv[2])
