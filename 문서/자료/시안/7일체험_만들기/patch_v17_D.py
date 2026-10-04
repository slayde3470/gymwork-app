"""v17 D (10-04 홍겸 님) — 캘린더 · 아래띠 · 업적띠 · 토스트
1 기록이 2개 이상인 날 — 기록 이름 줄 앞 체크 칸 · 체크한 것만 삭제
2 날 칸 구분선 되살림 (v12 의 .칸날{overflow:hidden} 이 칸 밖 1px 에 그린 선을 잘랐다)
3 달 고르기 창에서 '2026년' 을 누르면 년 고르기
4 ‹ › 25% 작게 (캘린더 년월 띠 · 달 고르기 · 년 고르기)
5 띠 · 토스트 — 누른 단추 위에 · 띠 밖은 눌림 · 페이드 · 떠 있는 시간 ×0.75"""
import sys, pathlib
IN, OUT = sys.argv[1], sys.argv[2]
s = pathlib.Path(IN).read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:70]!r}: {c}번")
    s = s.replace(old, new)

# ── 1 기록 고르기 체크 칸 ──
바꿈('''function 기록머리(r){ const 달=기록달성(r);
  return `<div class="예머리 록머리"><b class="예이름">''',
'''/* v17 홍겸 님 '2번 이상 했을 때 어떤 기록을 지울지 고르게 · 체크박스' — 그날 기록이 2개 이상일 때만 이름 줄 앞에 체크 칸(기본 모두 켬).
   부품은 세트 완료 체크(.체크 28 · 켜면 강조 바탕)와 같은 것. 끈 기록 키는 U.록뺌 에 — 다른 날을 누르면 비운다 */
function 기록머리(r, rk, 여럿){ const 달=기록달성(r), 켬=!(U.록뺌||[]).includes(rk);
  const 칸 = 여럿 ? `<button class="체크 록고름 ${켬?"켬":""}" data-enter="록고름${rk}-${켬?1:0}" data-act="기록고름" data-v="${rk}" aria-pressed="${켬}" aria-label="${esc(r.이름)} 기록 ${켬?"지우기에서 빼기":"지우기에 넣기"}">${켬?아이콘.체크:""}</button>` : "";
  return `<div class="예머리 록머리">${칸}<b class="예이름">''')
바꿈('''for(const [rk,r] of 록) h+=기록머리(r)+기록상세(r, "록"+rk);''',
     '''for(const [rk,r] of 록) h+=기록머리(r, rk, 록.length>1)+기록상세(r, "록"+rk);''')
# 삭제 단추 — 체크가 하나도 없으면 흐리게(disabled)
바꿈('''<button class="버튼 채움 나쁨" data-act="기록지움" data-k="${k}">운동 기록 삭제</button></div>`; }''',
     '''<button class="버튼 채움 나쁨" data-act="기록지움" data-k="${k}"${록.length>1&&록.every(([rk])=>(U.록뺌||[]).includes(rk))?" disabled":""}>운동 기록 삭제</button></div>`; }''')
# 지우기 — 체크한 것만. 띠가 떠 있는 동안 또 지우면 앞의 것도 함께 되돌린다(되돌릴 대상이 사라지지 않게)
바꿈('''function 기록지우기(k){ const 록=기록목록(k); if(!록.length) return; const id=++기록지움번호;
  U.기록지움={id, k, 록}; for(const [rk] of 록) delete S.기록[rk]; U.예펼침=null;''',
'''function 기록지우기(k){ const 전=기록목록(k), 뺌=U.록뺌||[], 록=전.length>1 ? 전.filter(([rk])=>!뺌.includes(rk)) : 전; if(!록.length) return; const id=++기록지움번호;
  U.기록지움={id, k, 록:[...록, ...(U.기록지움?.록||[])]}; for(const [rk] of 록) delete S.기록[rk]; U.예펼침=null; U.록뺌=[];''')
바꿈('''setTimeout(()=>{ if(U.기록지움?.id!==id) return; U.기록지움=null;
    document.querySelectorAll("#폰 .록지움띠").forEach(el=>{ el.classList.add("나감띠"); setTimeout(()=>el.remove(),200); }); }, 6000); }''',
'''setTimeout(()=>{ if(U.기록지움?.id!==id) return; U.기록지움=null;
    document.querySelectorAll("#폰 .록지움띠").forEach(el=>{ el.classList.add("나감띠"); setTimeout(()=>el.remove(),200); }); }, 4500); }   // v17 6초 × 0.75''')
바꿈('''return `<div class="아래띠 록지움띠" role="status" data-enter="록지움${x.id}"><span class="채움">${날글(x.k)} 운동 기록을 지웠습니다</span>''',
'''const n=x.록.length, 한날=x.록.every(([rk])=>rk.split("~")[0]===x.k);
  return `<div class="아래띠 록지움띠" role="status" data-enter="록지움${x.id}"><span class="채움">${한날?날글(x.k)+" ":""}운동 기록${n>1?` ${n}개를`:"을"} 지웠습니다</span>''')
바꿈('''    case "기록되돌림": 기록되돌리기(); break;''',
'''    case "기록되돌림": 기록되돌리기(); break;
    case "기록고름": { const l=U.록뺌||[]; U.록뺌 = l.includes(d.v) ? l.filter(x=>x!==d.v) : [...l, d.v]; break; }   // v17''')
바꿈('''      else U.고른날=d.k; break;''', '''      else { if(U.고른날!==d.k) U.록뺌=[]; U.고른날=d.k; } break;''')
# 일차 되돌림(아침 상태로) 뒤에는 지난 되돌리기를 버린다 — 나중 상태의 기록이 아침 상태에 끼어들지 않게
바꿈('''U.되돌림=null; U.업적띠=null; }''', '''U.되돌림=null; U.업적띠=null; U.기록지움=null; U.록뺌=[]; }''')

# ── 3 년 고르기 ──
바꿈('''function 달고르기시트(){ const 오=오늘(), 이달=오.slice(0,7), 보=U.보는달||이달, 해=U.시트.해;
  return `<div class="머리 달머리">''',
'''/* v17 홍겸 님 '2026년을 누르면 년 단위로' — 머리의 'N년' 도 단추. 누르면 12해(3×4) 격자 · ‹ › 는 12해씩 · 고르면 그 해의 달 고르기로.
   보는 해 = 고름 표시 · 올해 = 오늘 표시 (달 칸과 같은 부품) */
function 년고르기(){ const 올=+오늘().slice(0,4), 보=+(U.보는달||오늘().slice(0,7)).slice(0,4), 시=U.시트.해시작;
  return `<div class="머리 달머리"><span></span><span class="년월"><button class="달넘김" data-act="해묶음넘김" data-d="-1" aria-label="이전 12년">‹</button><button class="년월글" data-act="해고르기" aria-expanded="true"><b>${시}~${시+11}</b></button><button class="달넘김" data-act="해묶음넘김" data-d="1" aria-label="다음 12년">›</button></span><button class="닫기" data-act="시트닫기">닫기</button></div>
    <div class="달칸들" role="group" aria-label="해 고르기">${Array.from({length:12},(_,i)=>{ const y=시+i;
      return `<button class="달칸 ${y===보?"고름":""} ${y===올?"오늘":""}" data-act="해고름" data-v="${y}"${y===보?` aria-current="true"`:""}><span>${y}</span></button>`; }).join("")}</div>`; }
function 달고르기시트(){ const 오=오늘(), 이달=오.slice(0,7), 보=U.보는달||이달, 해=U.시트.해;
  if(U.시트.해보기) return 년고르기();
  return `<div class="머리 달머리">''')
바꿈('''<b>${해}년</b><button class="달넘김" data-act="해넘김"''',
     '''<button class="년월글" data-act="해고르기" aria-haspopup="true" aria-expanded="false"><b>${해}년</b></button><button class="달넘김" data-act="해넘김"''')
바꿈('''    case "해넘김": U.시트.해 += +d.d; break;''',
'''    case "해넘김": U.시트.해 += +d.d; break;
    case "해고르기": U.시트.해보기 = !U.시트.해보기; U.시트.해시작 = U.시트.해 - 5; break;   // v17 고른 해가 여섯째 칸
    case "해묶음넘김": U.시트.해시작 += 12 * +d.d; break;
    case "해고름": U.시트.해 = +d.v; U.시트.해보기 = false; break;''')

# ── 5 띠 · 토스트 ──
바꿈('''  setTimeout(()=>{ if(U.업적띠?.id!==id) return; U.업적띠=null; document.querySelectorAll(".업적띠").forEach(el=>{ el.classList.add("나감띠"); setTimeout(()=>el.remove(),200); }); }, 5000); }''',
     '''  setTimeout(()=>{ if(U.업적띠?.id!==id) return; U.업적띠=null; document.querySelectorAll(".업적띠").forEach(el=>{ el.classList.add("나감띠"); setTimeout(()=>el.remove(),200); }); }, 3750); }   // v17 5초 × 0.75''')
바꿈('''  const el=document.createElement("div"); el.className="토스트"; el.setAttribute("role","status"); el.textContent=글; 폰.appendChild(el);
  clearTimeout(토스트타이머); 토스트타이머=setTimeout(()=>el.remove(),2000); }''',
'''  const el=document.createElement("div"); el.className="토스트"; el.setAttribute("role","status"); el.textContent=글; 폰.appendChild(el);
  띠자리[`토스트${++토스트번호}`]=null; el.dataset.자리=`토스트${토스트번호}`; 띠맞춤(폰);
  clearTimeout(토스트타이머); 토스트타이머=setTimeout(()=>{ el.classList.add("나감띠"); setTimeout(()=>el.remove(),200); },1500); }   // v17 2초 × 0.75 · 사라질 때 흐려짐
/* v17 홍겸 님 '삭제 · 업적 알림이 하필 중요 단추 자리에 · 누른 단추 위쪽에 · 떠 있어도 다른 단추를 누를 수 있게'
   — 폰 안 단추를 누르면 그 자리(폰 기준)를 기억한다. 띠 · 토스트가 처음 나타날 때 2초 안에 누른 단추가 있으면
   띠 아랫변 = 단추 윗변 − 8. 위로 넘치면 단추 아래(윗변 = 단추 아랫변 + 8). 한 번 정한 자리는 띠가 사라질 때까지 그대로(다시 그려도 안 뜀).
   눌린 단추가 없으면 지금 자리(CSS). 띠 바깥 · 띠 글자는 눌림을 막지 않고, 띠 안 [되돌리기] · [보기] 만 눌린다 (CSS) */
let 마지막누름=null, 토스트번호=0; const 띠자리={};
document.addEventListener("click", e=>{ const b=e.target.closest?.("#폰 button"); if(!b || b.closest(".아래띠")) return;
  const 폰=document.getElementById("폰").getBoundingClientRect(), q=b.getBoundingClientRect();
  마지막누름={t:q.top-폰.top, b:q.bottom-폰.top, 때:performance.now()}; }, true);
function 띠맞춤(폰){ const 지금=performance.now(), 폰R=폰.getBoundingClientRect();
  폰.querySelectorAll(".아래띠, .토스트").forEach(el=>{ const 키=el.dataset.자리||el.dataset.enter; if(!키) return;
    if(!띠자리[키]) 띠자리[키] = 마지막누름 && 지금-마지막누름.때<2000 ? {...마지막누름} : "그대로";
    const a=띠자리[키]; if(a==="그대로") return;
    const 부=(el.offsetParent||폰).getBoundingClientRect(), 위=부.top-폰R.top, h=el.offsetHeight, 높=부.height;
    let t = a.t - 8 - h - 위;                       // 단추 위
    if(t < 4) t = a.b + 8 - 위;                     // 위로 넘치면 단추 아래
    t = Math.max(4, Math.min(t, 높 - h - 4));
    el.style.top = t+"px"; el.style.bottom = "auto"; el.classList.add("누른위"); }); }''')
바꿈('''체험막대(); 맞춤하기(폰); 시계그리기(); 애니(폰); 저장();
}''', '''체험막대(); 맞춤하기(폰); 시계그리기(); 애니(폰); 띠맞춤(폰); 저장();
}''')

css = '''
/* ═══ 10-04 v17 D — 캘린더 · 아래띠 · 업적띠 · 토스트 (홍겸 님) ═══ */
/* 2 날 칸 구분선 — 선은 ::after(inset -1 → 칸 테두리 바깥 1px)에 그린다. v12 의 .칸날{overflow:hidden} 이 칸 안쪽(패딩 상자)만 남기고
   잘라 위 · 왼쪽 선이 통째로 안 보였다. 칸은 넘침을 자르지 않고(visible), 긴 글 … 처리는 칸 안 글(.칸날>*)이 그대로 한다.
   7칸 같은 폭은 minmax(0,1fr) + min-width:0 이 지킨다 */
.칸날{overflow:visible}
/* 1 기록 고르기 체크 칸 — 세트 완료 체크와 같은 부품(28). 이름 줄 맨 앞 */
.록머리 .록고름{flex:none}
.판단추 .버튼:disabled{opacity:.35;cursor:default}
/* 4 ‹ › 25% 작게 — 글자 28 → 21 · 칸 40 → 30 (좁은 화면 폭 32 → 24). 새 값(홍겸 님 비율 ×0.75 · 글자 단계 · 누르는 높이 단계 밖).
   띠 높이 40 이 늘지 않게 위아래 -1 겹침(새 값) */
.달넘김{width:30px;height:30px;margin-block:-1px;font-size:21px}
@media (max-width:400px){ .년월띠 .달넘김{width:24px} }
/* 3 달 고르기 창 머리 'N년' 단추 — 캘린더 년월 글자 단추와 같은 부품 */
.달머리 .년월글{font-size:18px}
/* 5 띠 · 토스트 — 페이드 인/아웃(투명도만) · 띠 밖과 띠 글자는 눌림을 막지 않고 띠 안 단추만 눌린다 */
@keyframes 사라짐{from{opacity:1}to{opacity:0}}
.아래띠,.토스트{pointer-events:none}
.아래띠 button{pointer-events:auto}
.아래띠.들어옴{animation:흐려짐 .2s ease-out both}
.토스트{left:0;right:0;margin-inline:auto;width:max-content;max-width:calc(100% - 24px);transform:none;animation:흐려짐 .2s ease-out both}
.아래띠.나감띠,.토스트.나감띠{animation:사라짐 .2s ease-in both}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]
pathlib.Path(OUT).write_text(s, encoding='utf-8'); print("v17 D →", OUT)
