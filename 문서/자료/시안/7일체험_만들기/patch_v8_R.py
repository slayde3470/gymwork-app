"""7day-v7.html → 루틴 상세 · 종목 넣기 시트 · ＋/− 누름 움직임 (10-03 홍겸 님 ✎ 표시 10개)
   python3 patch_v8_R.py 입력.html 출력.html

표시 → 고친 것
 401d 루틴 "이동"  → 맨 위 줄 = 띠 [‹ 루틴][이름 칸 — 안쪽 오른쪽에 흐린 '예상 29분 · 17세트'][자동 ◯]
                    '자동으로 돌리기 / 캘린더에 순서대로 깔립니다' 줄은 없앴다
 a8es 루틴 "맨 위 띠 아래에 파란박스로 종목추가"  → 띠 바로 아래 [+ 종목 추가] (.버튼 주 넓)
 mqjf 루틴 "화면을 넘어갈 때는 맨 마지막 세트 아래에 +종목 추가"  → 목록 끝에도 [+ 종목 추가].
      맨 아래까지 내렸을 때 위 단추가 안 보일 때만 나온다 (짧은 루틴에서 같은 단추 두 개가 한 화면에 안 보이게)
 c1gz 루틴 "[플랜]은 이름 오른쪽 위 5px 겹쳐 · 1줄 · 루틴에서도 고칠 수 있게"
      → 플랜 상자 = 한 줄 '벤치프레스[플랜] · 15회차 60kg × 9회 × 5세트'. 그 줄을 누르면 종목 탭 [변경]과 같은 '플랜 고치기' 시트
 h627 루틴 "−세트는 휴식 오른쪽에 휴지통 · 세트마다"  → 세트 줄마다 휴식 칸 오른쪽 휴지통. 묻지 않고 지우고 아래띠 6초 [되돌리기] (U5-4).
      세트가 하나뿐이면 휴지통은 흐리게 꺼 둔다 (종목째 빼기는 ✕ 가 한다)
 9kht 루틴 "줄간격 70%"  → 세트 줄 높이 40 → 28 · 틈 4 그대로 (44 → 32, 73%)
 izyq 루틴 "종목이름란으로 각 칸 구분"  → 종목 상자 이름 줄 = 머리 (--강조옅음 바탕 · 이름 15 Bold · 높이 40)
 jmg1 시트 "운동플랜 박스칸 없애고 [플랜] 표시"  → '운동 플랜' 묶음 삭제. 목록의 플랜 종목 이름에 [플랜] 표 · 누르면 플랜 종목으로 넣는다
 w0fo 시트 "[전체] 맨 왼쪽 · 가나다순 · 줄간격 70% · 최근 기록 흐리게"
      → 칩줄 맨 왼쪽 [전체](처음 열면 전체) · 가나다순 · 줄 52.5 → 36.5 (70%) · 이름 옆에 흐리게 '최근 3세트 · 최고 60kg×9 · 1RM 78kg · 휴식 1:30'
 ls4v 전체 "+ 는 볼록, − 는 오목 · 모든 UI"  → 문서 전체 클릭을 window 잡기 단계 한곳에서 보고, 글자가 + / ＋ 로 시작하는 단추는 1.12배,
      − / - 로 시작하는 단추는 0.88배로 0.2초 (큰 단추는 6px 안쪽으로만). 움직임 줄이기 설정이면 하지 않는다
"""
import sys, pathlib
들, 날 = pathlib.Path(sys.argv[1]), pathlib.Path(sys.argv[2])
s = 들.read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:80]!r}: {c}번")
    s = s.replace(old, new)

# ══ 401d · mqjf — 루틴 상세: 띠는 넘김 칸 밖(캘린더 년월 띠처럼 늘 위에), 지운 세트 되돌리기 띠는 화면 위에 ══
바꿈('''function 루틴탭(){ const r=U.루틴열림&&루틴(U.루틴열림); if(r) return `<div class="넘김">${루틴상세(r)}</div>`;''',
     '''function 루틴탭(){ const r=U.루틴열림&&루틴(U.루틴열림);
  if(r){ queueMicrotask(루끝단추맞춤); return `${루틴상세띠(r)}<div class="넘김 루넘김">${루틴상세(r)}</div>${세트지움띠()}`; }''')

# ══ 루틴 상세 · 종목 상자 · 세트 줄 (401d · a8es · mqjf · c1gz · h627 · 9kht · izyq) ══
바꿈('''function 루틴상세(r){ const 실=실제루틴(r);
  return `<div class="줄" style="margin:12px 0 6px"><button class="버튼 낮" data-act="루틴닫기">‹ 루틴</button><input class="입력 채움" data-in="루틴이름" value="${esc(r.이름)}" aria-label="루틴 이름"></div>
    ${설정줄("자동으로 돌리기","캘린더에 순서대로 깔립니다",스위치(r.자동생성,"루틴자동",r.id))}
    ${r.휴식일?`<div class="빈칸">휴식일</div>`:`<div class="맞춤 아주작 옅음" style="margin-bottom:6px">예상 ${시간글(예상초(실))} · ${총세트(실)}세트</div>
    ${r.종목.map((e,i)=>루틴종목상자(e,i)).join("")||`<div class="빈칸">종목을 넣어 주세요</div>`}
    <button class="버튼 넓" data-act="시트" data-t="종목넣기" style="margin-top:4px">+ 종목</button>`}
    <button class="버튼 넓 나쁨" data-act="루틴지움" data-v="${r.id}" style="margin-top:12px">${U.확인==="루틴"+r.id?"한 번 더 누르면 지웁니다":"이 루틴 지우기"}</button>`; }''',
     '''/* ═══ 10-03 ✎ 표시 — 루틴 상세 · 종목 넣기 시트 · ＋/− 누름 움직임 (patch_v8_R) ═══ */
아이콘.휴지통 = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4 7h16M9 7V4h6v3M6 7l1 13h10l1-13M10 11v6M14 11v6"/></svg>`;   // h627
/* jmg1 · c1gz [플랜] 표 — 이름 오른쪽 위에 5px 겹친다 (홍겸 님이 준 값). 루틴 상자와 종목 넣기 시트가 같은 부품을 쓴다 */
const 플랜딱지 = `<span class="플랜표">플랜</span>`;
/* w0fo 종목 넣기 시트는 처음 열면 [전체] — 맨 왼쪽 칩이 기본. 한 번 고른 칸은 그대로 기억한다 */
U.칸고름 = "전체";

/* 401d 맨 위 띠 = [‹ 루틴][이름 칸 + 흐린 예상][자동 스위치]. 이름 칸은 띠 위에서도 '고치는 칸' 으로 읽히게 흰 바탕(--면) 입력.
   '캘린더에 순서대로 깔립니다' 설명 줄은 홍겸 님이 지웠다 — 스위치 옆 '자동' 두 글자만.
   스위치 루틴 id 는 넷째 인자(data-v)로 — v7 까지 셋째(data-f)로 넘겨 '루틴자동' 이 루틴(d.v)=undefined 로 멈췄다(스위치가 안 바뀜) */
function 루틴상세띠(r){ const 실=실제루틴(r);
  return `<div class="띠 루띠"><button class="작은흰" data-act="루틴닫기">‹ 루틴</button>
    <label class="루이름칸"><input data-in="루틴이름" value="${esc(r.이름)}" aria-label="루틴 이름">${r.휴식일?"":`<span class="루예상 숫">예상 ${시간글(예상초(실))} · ${총세트(실)}세트</span>`}</label>
    <label class="루자동"><span>자동</span>${스위치(r.자동생성,"루틴자동","",r.id)}</label></div>`; }
function 루틴상세(r){
  const 추가 = 곳=> `<button class="버튼 주 넓 루추가 ${곳}" data-act="시트" data-t="종목넣기">+ 종목 추가</button>`;
  return `${r.휴식일?`<div class="빈칸" style="margin-top:12px">휴식일</div>`:`${추가("위")}
    ${r.종목.map((e,i)=>루틴종목상자(e,i)).join("")||`<div class="빈칸">종목을 넣어 주세요</div>`}
    ${r.종목.length?추가("끝"):""}`}
    <button class="버튼 넓 나쁨" data-act="루틴지움" data-v="${r.id}" style="margin-top:12px">${U.확인==="루틴"+r.id?"한 번 더 누르면 지웁니다":"이 루틴 지우기"}</button>`; }
/* mqjf 끝 [+ 종목 추가] 는 맨 아래까지 내렸을 때 위 단추가 화면 밖으로 나갈 때만 보인다.
   그리기() 가 끝난 뒤(마이크로태스크) 재고 한 번만 정한다 — 끝 단추를 늘 그려 두고 필요 없으면 숨기는 쪽이라
   스크롤을 되살린 자리가 끝 단추 높이만큼 잘리지 않는다 */
function 루끝단추맞춤(){ const 넘=document.querySelector("#폰 .루넘김"), 위=넘?.querySelector(".루추가.위"), 끝=넘?.querySelector(".루추가.끝"); if(!위||!끝) return;
  끝.style.display=""; const 넘q=넘.getBoundingClientRect(), 위아래=위.getBoundingClientRect().bottom-넘q.top+넘.scrollTop;
  const 끝몫=끝.getBoundingClientRect().height+parseFloat(getComputedStyle(끝).marginTop||0), 끝없이최대=넘.scrollHeight-끝몫-넘.clientHeight;
  if(끝없이최대<=위아래) 끝.style.display="none"; }
/* c1gz 플랜 줄은 div 라 Enter · 스페이스로도 누를 수 있게 (단추와 같게) */
document.addEventListener("keydown", e=>{ if((e.key==="Enter"||e.key===" ") && e.target.matches?.(".루플랜줄[data-act]")){ e.preventDefault(); e.target.click(); } });
/* h627 지운 세트 되돌리기 — 아래띠 6초 (U5-4 묻지 않고 지우고 되돌린다) */
let 루지움번호=0;
function 세트지움띠(){ const x=U.세트지움; if(!x) return "";
  return `<div class="아래띠 루지움띠" role="status" data-enter="세트지움${x.id}"><span class="채움">${esc(x.이름)} ${x.k+1}세트를 지웠습니다</span><button data-act="루세트되돌림">되돌리기</button></div>`; }''')

# c1gz · izyq · h627 · 9kht — 종목 상자
바꿈('''function 루틴종목상자(e,i){
  const 머리 = `<div class="줄">${e.플랜id?`<span class="알약 강조">플랜</span>`:""}<b class="채움">${esc(e.이름)}</b><button class="닫기" data-act="종목빼기" data-i="${i}" aria-label="빼기">✕</button></div>`;
  if(e.플랜id){ const p=플랜(e.플랜id), x=p&&플랜처방(p);
    return `<div class="종목상자" data-drag="종목줄" data-i="${i}" data-drop="종목줄">${머리}<div class="맞춤 작 흐림">${x?`${x.계.측정일?"측정 · ":""}${x.계.회}회차 ${처방글(x.목)}`:"목표 달성"}</div></div>`; }
  return `<div class="종목상자" data-drag="종목줄" data-i="${i}" data-drop="종목줄">${머리}
    <div class="세트머리 루 아주작 옅음"><span>세트</span><span>무게 kg</span><span>횟수</span><span>휴식</span></div>
    ${e.세트.map((s,k)=>`<div class="루세트"><span class="k">${k+1}</span>${루값("w",s.w,i,k)}${루값("r",s.r,i,k)}${루값("휴",s.휴,i,k)}</div>`).join("")}
    <div class="줄"><button class="버튼 낮 채움" data-act="루세트" data-i="${i}" data-d="1">+ 세트</button>${e.세트.length>1?`<button class="버튼 낮" data-act="루세트" data-i="${i}" data-d="-1">− 세트</button>`:""}</div></div>`; }''',
     '''/* izyq 이름 줄 = 상자 머리 (--강조옅음 바탕 · 이름 15 Bold) — 흰 세트 칸들과 바탕이 달라 상자끼리 한눈에 갈린다
   c1gz 플랜 상자는 머리 한 줄뿐: '이름[플랜] · 15회차 60kg × 9회 × 5세트'. 그 줄을 누르면 종목 탭 [변경]과 같은 '플랜 고치기'.
        줄은 <button> 이 아니라 data-act 를 단 div — 상자를 꾹 눌러 끄는 동작(끌기는 단추 위에서 시작하지 않는다)을 살리려고 */
function 루틴종목상자(e,i){
  const 빼기 = `<button class="닫기" data-act="종목빼기" data-i="${i}" aria-label="빼기">✕</button>`;
  if(e.플랜id){ const p=플랜(e.플랜id), x=p&&플랜처방(p), 글=x?`${x.계.측정일?"측정 · ":""}${x.계.회}회차 ${처방글(x.목)}`:"목표 달성";
    return `<div class="종목상자 루플랜" data-drag="종목줄" data-i="${i}" data-drop="종목줄"><div class="루머리"><div class="채움 루플랜줄"${p?` data-act="플랜고치기" data-v="${p.id}" role="button" tabindex="0" aria-label="${esc(e.이름)} 플랜 고치기"`:""}><span class="이름플랜"><b>${esc(e.이름)}</b>${플랜딱지}</span><span class="루플랜글 숫">· ${글}</span></div>${빼기}</div></div>`; }
  /* h627 · 9kht 세트 줄 = [번호][무게][횟수][휴식][휴지통] · 높이 28. 세트가 하나면 휴지통은 꺼 둔다 */
  const 하나 = e.세트.length<=1;
  return `<div class="종목상자" data-drag="종목줄" data-i="${i}" data-drop="종목줄"><div class="루머리"><b class="채움">${esc(e.이름)}</b>${빼기}</div>
    <div class="세트머리 루 아주작 옅음"><span>세트</span><span>무게 kg</span><span>횟수</span><span>휴식</span><span></span></div>
    ${e.세트.map((s,k)=>`<div class="루세트"><span class="k">${k+1}</span>${루값("w",s.w,i,k)}${루값("r",s.r,i,k)}${루값("휴",s.휴,i,k)}<button class="루지움" data-act="루세트지움" data-i="${i}" data-k="${k}" aria-label="${k+1}세트 지우기"${하나?" disabled":""}>${아이콘.휴지통}</button></div>`).join("")}
    <button class="버튼 낮 넓" data-act="루세트" data-i="${i}" data-d="1">+ 세트</button></div>`; }''')

# ══ jmg1 · w0fo — 종목 넣기 시트 ══
바꿈('''  else if(종==="종목넣기"){ const r=루틴(U.루틴열림); if(!r){ U.시트=null; return ""; }
    const 플=S.플랜들.map(p=>{ const 넣=U.방금.includes("p:"+p.id), 몇=r.종목.filter(e=>e.플랜id===p.id).length, x=플랜처방(p);
      return `<button class="고르기" data-act="플랜넣기" data-v="${p.id}"><b class="${넣?"강조":""}">${esc(p.이름)}</b><span class="곁">${x?`${x.계.측정일?"측정 · ":""}${x.계.회}회차 ${처방글(x.목)}`:"목표 달성"}</span>${넣?"✓":몇?"있음":"＋"}</button>`; }).join("");
    const 목=S.종목표.filter(x=>x.칸===U.칸고름).map(x=>{ const 넣=U.방금.includes("e:"+x.이름), 있=r.종목.some(e=>e.이름===x.이름&&!e.플랜id);
      return `<button class="고르기" data-act="종목넣기" data-v="${esc(x.이름)}"><b class="${넣?"강조":""}">${esc(x.이름)}</b><span class="곁"></span>${넣?"✓":있?"있음":"＋"}</button>`; }).join("");
    안 = 머리(`${esc(r.이름)}에 넣기`) + (플?`<div class="아주작 옅음">운동 플랜</div>${플}`:"") + 칩줄("칸고름","",S.카테고리,U.칸고름) + 목; 높=true; }''',
     '''  else if(종==="종목넣기"){ const r=루틴(U.루틴열림); if(!r){ U.시트=null; return ""; }
    /* jmg1 '운동 플랜' 묶음 없음 — 플랜은 목록 안에서 [플랜] 표로. w0fo [전체] 맨 왼쪽 · 칩 7개가 360 폭 한 줄에 들어가게 같은 폭 */
    안 = 머리(`${esc(r.이름)}에 넣기`) + `<div class="넣기칩">${칩줄("칸고름","",["전체",...S.카테고리],U.칸고름)}</div><div class="넣기목록">${넣기목록(r)}</div>`; 높=true; }''')
바꿈('''/* ── 시트 ── */
function 시트(){''',
     '''/* ── 시트 ── */
/* jmg1 · w0fo 종목 넣기 목록 — 종목표 + 플랜. 플랜이 있는 종목은 그 줄이 플랜 줄이 된다(플랜이 여럿이면 플랜마다 한 줄).
   누르면 플랜 종목으로 넣는다(전과 같은 '플랜넣기'). 이름 가나다순. 이름 옆에 그 종목의 가장 최근 기록을 흐리게 */
function 넣기목록(r){ const 칸=U.칸고름, 줄=[];
  for(const x of S.종목표){ if(칸!=="전체" && x.칸!==칸) continue; const 플=S.플랜들.filter(p=>p.종목===x.이름);
    if(플.length) 플.forEach(p=>줄.push({이름:p.이름, p, 종목:x.이름})); else 줄.push({이름:x.이름, 종목:x.이름}); }
  if(칸==="전체") S.플랜들.filter(p=>!S.종목표.some(x=>x.이름===p.종목)).forEach(p=>줄.push({이름:p.이름, p, 종목:p.종목}));   // 종목표에 없는 종목의 플랜도 잃지 않게
  줄.sort((a,b)=>a.이름.localeCompare(b.이름,"ko"));
  return 줄.map(z=>{ const 열=z.p?"p:"+z.p.id:"e:"+z.이름, 넣=U.방금.includes(열), 있=z.p?r.종목.some(e=>e.플랜id===z.p.id):r.종목.some(e=>e.이름===z.이름&&!e.플랜id);
    return `<button class="고르기 넣기줄" data-act="${z.p?"플랜넣기":"종목넣기"}" data-v="${z.p?z.p.id:esc(z.이름)}"><span class="이름플랜"><b class="${넣?"강조":""}">${esc(z.이름)}</b>${z.p?플랜딱지:""}</span><span class="곁 숫">${최근기록글(z.종목)}</span>${넣?"✓":있?"있음":"＋"}</button>`; }).join("")
    || `<div class="빈칸">이 칸에 종목이 없습니다</div>`; }
/* w0fo 가장 최근에 한 날(체크한 세트만) — '최근 3세트 · 최고 60kg×9 · 1RM 78kg · 휴식 1:30'. 최고 = 가장 무거운 세트(같으면 횟수 많은 것).
   맨몸(0kg)은 '최고 12회' 만 · 기록이 없으면 비움 */
function 최근기록글(종목){ const 줄=Object.entries(S.기록).sort(([a],[b])=>키순(b,a));
  for(const [,rec] of 줄){ const 세=rec.종목.filter(e=>정식이름(e)===종목||e.이름===종목).flatMap(한세트); if(!세.length) continue;
    const 최=세.reduce((a,b)=>(+b.w>+a.w||(+b.w===+a.w&&b.r>a.r))?b:a), rm=Math.max(0,...세.map(RM값)), 휴=세.find(x=>x.휴>0)?.휴;
    return [`최근 ${세.length}세트`, 최.w>0?`최고 ${kg(+최.w)}kg×${최.r}`:`최고 ${최.r}회`, rm>0?`1RM ${kg(Math.round(rm*2)/2)}kg`:"", 휴?`휴식 ${분초(휴)}`:""].filter(Boolean).join(" · "); }
  return ""; }
function 시트(){''')

# ══ h627 세트 지우기 · 되돌리기 (행동) ══
바꿈('''    case "루세트": { const e=루틴(U.루틴열림).종목[+d.i]; if(+d.d>0){ const l=e.세트[e.세트.length-1]||{w:20,r:10,휴:S.설정.기본휴식}; e.세트.push({...l}); } else if(e.세트.length>1) e.세트.pop(); break; }''',
     '''    case "루세트": { const e=루틴(U.루틴열림).종목[+d.i]; if(+d.d>0){ const l=e.세트[e.세트.length-1]||{w:20,r:10,휴:S.설정.기본휴식}; e.세트.push({...l}); } else if(e.세트.length>1) e.세트.pop(); break; }
    /* h627 세트 줄 휴지통 — 묻지 않고 그 세트를 지우고, 아래띠 6초 [되돌리기] (U5-4). 마지막 하나는 지우지 않는다 */
    case "루세트지움": { const r=루틴(U.루틴열림), e=r?.종목[+d.i], k=+d.k; if(!e||e.세트.length<=1||!e.세트[k]) break;
      const 세트=e.세트.splice(k,1)[0], id=++루지움번호; U.세트지움={r:r.id, e, k, 세트, id, 이름:e.이름}; 발자취(`세트 지움 · ${e.이름} ${k+1}세트`);
      setTimeout(()=>{ if(U.세트지움?.id!==id) return; U.세트지움=null; document.querySelectorAll(".루지움띠").forEach(el=>{ el.classList.add("나감띠"); setTimeout(()=>el.remove(),200); }); }, 6000); break; }
    case "루세트되돌림": { const x=U.세트지움; U.세트지움=null; const r=x&&루틴(x.r); if(r&&r.종목.includes(x.e)){ x.e.세트.splice(Math.min(x.k,x.e.세트.length),0,x.세트); 발자취(`세트 되돌림 · ${x.이름}`); } break; }''')

# ══ ls4v — ＋ 볼록 · − 오목 (앱 전체, 한곳에서) ══
# 왜 window 잡기 단계인가: document 잡기 단계의 click 처리가 곧바로 그리기() 로 화면을 새로 그려 누른 단추가 사라진다.
#   window 잡기 단계는 그보다 먼저 돈다 → 여기서 누른 단추의 자리와 data-* 를 재 두고, 다시 그린 뒤(rAF) 같은 data-* 의 새 단추
#   (여럿이면 원래 자리에서 가장 가까운 것)에 움직임을 준다. '+ 세트' 처럼 다시 그린 뒤 자리가 내려간 단추에도 보인다.
# 왜 크기를 단추 크기로 나누나: 넓은 단추를 1.12배 하면 양옆이 20px 넘게 튀어나와 화면 밖으로 넘친다 → 늘 6px 안쪽으로만 부푼다/줄어든다
바꿈('''let 막음클릭=false;
document.addEventListener("click", e=>{''',
     '''let 막음클릭=false;
const 누름꼴 = el => { const t=(el?.textContent||"").trim(); return /^[+＋]/.test(t) ? "볼록" : /^[−\\-–]/.test(t) ? "오목" : ""; };
window.addEventListener("click", e=>{ if(막음클릭 || 움직임줄임()) return;
  const el=e.target.closest?.("button,label.사진추가"), 꼴=누름꼴(el); if(!꼴) return;
  const q=el.getBoundingClientRect(), x=q.left+q.width/2, y=q.top+q.height/2, 크=Math.max(q.width,q.height,1);
  const 배 = 꼴==="볼록" ? Math.min(1.12, 1+12/크) : Math.max(0.88, 1-12/크);
  const 표=[...el.attributes].filter(a=>a.name.startsWith("data-")&&a.name!=="data-enter").map(a=>`[${a.name}="${CSS.escape(a.value)}"]`).join("");
  requestAnimationFrame(()=>{ let 대=el.isConnected?el:null;
    if(!대 && 표){ let 멀=1e9; document.querySelectorAll(el.tagName+표).forEach(c=>{ if(누름꼴(c)!==꼴) return; const r=c.getBoundingClientRect(), m=Math.hypot(r.left+r.width/2-x, r.top+r.height/2-y); if(m<멀){ 멀=m; 대=c; } }); }
    if(!대){ const c=document.elementFromPoint(x,y)?.closest("button,label.사진추가"); if(누름꼴(c)===꼴) 대=c; }
    if(!대) return; 대.classList.remove("볼록","오목"); 대.style.setProperty("--눌림", 배); void 대.offsetWidth; 대.classList.add(꼴);
    대.addEventListener("animationend", ()=>대.classList.remove(꼴), {once:true}); });
}, true);
document.addEventListener("click", e=>{''')

css = '''
/* ═══ 10-03 ✎ 표시 — 루틴 상세 · 종목 넣기 시트 · ＋/− 누름 움직임 (patch_v8_R) ═══ */
/* 401d · a8es 맨 위 띠 — .띠 그대로(강조 바탕 · 강조글 · 40 · 6/12). 이름 칸은 흰 바탕 입력(높이 32 · 이름 15 Bold) + 안쪽 오른쪽에 흐린 예상 11 */
.루띠{flex:none}
.루이름칸{flex:1;min-width:0;height:32px;display:flex;align-items:center;gap:8px;padding:0 8px;background:var(--면);color:var(--글);border-radius:8px;cursor:text}
.루이름칸 input{flex:1 1 0;min-width:5em;height:100%;border:0;background:transparent;padding:0;font-size:15px;font-weight:700;color:var(--글)}
.루이름칸 input:focus-visible{outline:none}
.루이름칸:focus-within{outline:2px solid var(--강조글);outline-offset:1px}
/* 좁은 폰에서는 이름이 먼저 — 예상 글이 말줄임으로 줄어든다 (이름 칸은 최소 5글자 폭) */
.루예상{flex:0 1 auto;min-width:0;overflow:hidden;text-overflow:ellipsis;font-size:11px;color:var(--옅음);white-space:nowrap}
.루자동{display:flex;align-items:center;gap:4px;flex:none;font-size:11px;font-weight:700;cursor:pointer}
/* 띠 위 스위치 — 켬이 강조 바탕에 묻히지 않게: 켬 = 강조글 길 · 강조 손잡이, 끔 = 옅은 강조글 길 · 강조글 손잡이 */
.루띠 .스위치{background:color-mix(in srgb,var(--강조글) 35%,transparent)}
.루띠 .스위치 i{background:var(--강조글)}
.루띠 .스위치.켬{background:var(--강조글)}
.루띠 .스위치.켬 i{background:var(--강조)}
/* a8es · mqjf 파란 [+ 종목 추가] — 띠 바로 아래 · 목록 끝 */
.루추가.위{margin:12px 0 8px}
.루추가.끝{margin:0 0 0}
/* izyq 종목 상자 머리 — 상자 안쪽 여백(8)만큼 밖으로 꺼내 가장자리까지. 모서리 15 = 상자 16 − 테두리 1 */
.루머리{display:flex;align-items:center;gap:8px;min-height:40px;margin:-8px -8px 0;padding:4px 4px 4px 12px;background:var(--강조옅음);border-radius:15px 15px 0 0}
.루머리>b{font-size:15px;font-weight:700;min-width:0;overflow-wrap:anywhere}
.루머리 .닫기{flex:none;min-width:32px;min-height:32px}
/* c1gz 플랜 상자 = 머리 한 줄뿐 */
.종목상자.루플랜{padding:0}
.루플랜 .루머리{margin:0;border-radius:15px}
.루플랜줄{display:flex;align-items:center;gap:8px;min-width:0;min-height:32px;cursor:pointer;white-space:nowrap}
.루플랜줄 .이름플랜 b{font-size:15px;font-weight:700}
.루플랜글{font-size:13px;color:var(--흐림);min-width:0;overflow:hidden;text-overflow:ellipsis}
/* c1gz · jmg1 [플랜] 표 — 강조 바탕 · 강조글 · 11 Bold · 모서리 8. 이름 오른쪽 끝 5px · 위쪽 5px(글자 윗선 기준) 겹친다 */
.이름플랜{display:inline-flex;align-items:flex-start;flex:none;min-width:0;max-width:100%}
.이름플랜>b{min-width:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.플랜표{position:relative;top:-6px;margin-left:-5px;flex:none;font-size:11px;font-weight:700;line-height:14px;padding:0 6px;border-radius:8px;background:var(--강조);color:var(--강조글);white-space:nowrap}
/* h627 · 9kht 세트 줄 — [번호 16][무게][횟수][휴식][휴지통 28]. 칸 높이 40 → 28 · 틈 4 (줄 간격 44 → 32).
   세 칸 폭은 들어갈 글자 폭대로 83 : 70 : 79 (무게 '62.5' · 횟수 '12' · 휴식 '1:30') */
.세트머리.루,.루세트{grid-template-columns:16px minmax(0,83fr) minmax(0,70fr) minmax(0,79fr) 28px}
.세트머리.루 span{white-space:nowrap}
.루세트 .값칸{height:28px}
/* 360 폭(폰 칸 328)에서는 휴지통 칸 28 이 들어온 만큼 모자라 '10' 이 '1' 로 잘렸다 →
   이 화면 세트 줄만 ± 단추 폭 28 → 24 (높이 28 그대로) · 상자 좌우 여백 8 → 4 */
@media (max-width:400px){
  .루넘김 .종목상자:not(.루플랜){padding:8px 4px}
  .루넘김 .종목상자:not(.루플랜)>.루머리{margin:-8px -4px 0}
  .루세트 .값칸 button{width:24px}
}
.루지움{width:28px;height:28px;display:inline-flex;align-items:center;justify-content:center;color:var(--옅음);border-radius:8px}
.루지움 svg{width:16px;height:16px}
.루지움:disabled{opacity:.35;cursor:default}
.루지움띠.나감띠{opacity:0;transition:opacity .2s}
/* w0fo 종목 넣기 — 칩 7개 같은 폭 한 줄 · 목록 줄 높이 70% (줄 사이 틈 8 없앰 · 위아래 12 → 8: 52.5 → 36.5) */
.넣기칩 .칩줄{flex-wrap:nowrap}
.넣기칩 .칩{flex:1;justify-content:center;padding:0 4px;min-width:0}
.넣기목록{display:flex;flex-direction:column}
.넣기줄{padding:8px 0}
.넣기줄 .곁{text-overflow:ellipsis}
/* ls4v ＋ 볼록 · − 오목 — 0.2초. 배율(--눌림)은 단추 크기에 맞춰 JS 가 넣는다 (넓은 단추는 6px 안쪽) */
@keyframes 볼록{0%{transform:scale(1)}45%{transform:scale(var(--눌림,1.12))}100%{transform:scale(1)}}
@keyframes 오목{0%{transform:scale(1)}45%{transform:scale(var(--눌림,.88))}100%{transform:scale(1)}}
button.볼록,label.볼록{animation:볼록 .2s ease-out}
button.오목,label.오목{animation:오목 .2s ease-out}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]
날.write_text(s, encoding='utf-8')
print('✅', 날, len(s))
