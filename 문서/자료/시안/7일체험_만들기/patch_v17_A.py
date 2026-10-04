"""v17 A (10-04 홍겸 님) — 운동 화면 세트 줄 · 종목 탭 기본 세팅
① 체크하면 그 줄의 kg · 회 · 휴식 세 칸이 하나의 게이지 단추로 합쳐진다(± 없음). 높이 28 × 1.15 = 32.2 → 32.
   윗줄 남은 시간(15 Bold) · 아랫줄 '건너뛰기'(11 Bold). 누르면 건너뛰기(전과 같은 '휴식건너뛰기'). 끝나면 원래 칸으로
② 끝난 세트 줄 바탕 --면2 → --완료바탕(새 값 · --면2 와 --강조옅음 사이)
③ 그 종목 세트가 다 끝난 뒤의 휴식이면 아랫줄 = '건너뛰고 다음 운동으로 넘어가기' (v10 ai39 의 '다음 종목' 동작 그대로)
④ '+ 세트' 줄 = 마지막 세트의 다음다음 줄 (사이에 세트 줄 한 줄 높이 44 빈 칸)
⑤ 종목 탭 펼친 칸에 '기본 세팅'(세트 · 무게 · 횟수 · 휴식) → S.종목설정[이름]. 종목넣기(루틴 · 운동)가 이 값을 쓴다. 플랜넣기는 그대로"""
import sys, pathlib
IN, OUT = sys.argv[1], sys.argv[2]
s = pathlib.Path(IN).read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:70]!r}: {c}번")
    s = s.replace(old, new)

# ①③ 쉬는 줄 = [동그라미][게이지 단추 — kg·회·휴식 세 칸 자리][휴지통]
바꿈('''  return `<div class="세트줄 ${k===지금k&&!쉼?"지금":""} ${s.완료?"완료줄":""}"><button class="체크 세트번호 ${s.완료?"켬":""}" data-enter="체${i}-${k}-${s.완료?1:0}" data-act="체크" data-i="${i}" data-k="${k}" aria-label="${k+1}세트 ${s.완료?"체크 풀기":"완료"}" aria-pressed="${s.완료}">${s.완료?아이콘.체크:k+1}</button>${값("w",s.w)}${값("r",s.r)}
    <div class="값칸 휴칸 ${쉼?"쉼":""}">${휴(-1,"15초 줄이기")}${쉼?`<button class="쉼단추" data-act="휴식건너뛰기" aria-label="휴식 건너뛰기"><span class="밑"><b data-timer="휴식"></b><small>건너뛰기</small></span><span class="위" data-쉼바><b data-timer="휴식"></b><small>건너뛰기</small></span></button>`:`<span class="숫">${분초(s.휴)}</span>`}${휴(1,"15초 늘리기")}</div>''',
     '''  /* 10-04 v17 ① "체크 박스를 누르면 무게 · 횟수 · 휴식 박스가 하나로 합쳐지고 −＋ 가 사라지면서 하나의 게이지" —
     쉬는 동안 세 칸 자리(2~4번째 칸)에 게이지 단추 하나. 윗줄 남은 시간 · 아랫줄 건너뛰기. 끝나거나 누르면 원래 세 칸.
     ③ "마지막 세트만" — 그 종목 세트가 다 끝난 뒤의 휴식이고 남은 종목이 있으면 '건너뛰고 다음 운동으로 넘어가기'
        (누르면 휴식건너뛰기 → v10 ai39 의 '다음 안 끝난 종목으로' 그대로) */
  const 넘김=쉼 && U.본===i && ss.종목[i].세트.every(x=>x.완료) && ss.종목.some((x,j)=>j!==i&&x.세트.some(y=>!y.완료));
  const 쉼글=넘김?"건너뛰고 다음 운동으로 넘어가기":"건너뛰기";
  const 가운데 = 쉼 ? `<button class="쉼게이지 ${넘김?"넘김":""}" data-act="휴식건너뛰기" data-enter="쉼${i}-${k}" aria-label="휴식 ${쉼글}"><span class="밑"><b data-timer="휴식"></b><small>${쉼글}</small></span><span class="위" data-쉼바><b data-timer="휴식"></b><small>${쉼글}</small></span></button>`
    : `${값("w",s.w)}${값("r",s.r)}
    <div class="값칸 휴칸">${휴(-1,"15초 줄이기")}<span class="숫">${분초(s.휴)}</span>${휴(1,"15초 늘리기")}</div>`;
  return `<div class="세트줄 ${k===지금k&&!쉼?"지금":""} ${s.완료?"완료줄":""} ${쉼?"쉼줄":""}"><button class="체크 세트번호 ${s.완료?"켬":""}" data-enter="체${i}-${k}-${s.완료?1:0}" data-act="체크" data-i="${i}" data-k="${k}" aria-label="${k+1}세트 ${s.완료?"체크 풀기":"완료"}" aria-pressed="${s.완료}">${s.완료?아이콘.체크:k+1}</button>${가운데}''')

# ④ 마지막 세트 줄과 '+ 세트' 줄 사이에 세트 줄 한 줄 높이만큼 빈 칸
바꿈('''      ${e.세트.map((s,k)=>운세트줄(본,s,k,지금k)).join("")}
      <div class="줄"><button class="버튼 낮 채움" data-act="세트더" data-i="${본}">+ 세트</button></div></div>''',
     '''      ${e.세트.map((s,k)=>운세트줄(본,s,k,지금k)).join("")}
      <div class="세트빈줄" aria-hidden="true"></div>
      <div class="줄"><button class="버튼 낮 채움" data-act="세트더" data-i="${본}">+ 세트</button></div></div>''')

# ⑤ 종목 탭 — 펼친 칸에 '기본 세팅' 줄 (플랜 카드 다음 · 사진 앞)
바꿈('''function 종목줄(x){ const l=사진목록(x.이름), 펼=U.종목펼침===x.이름,''',
     '''/* 10-04 v17 ⑤ "종목탭에서 각 운동 세팅 — 세트 · 무게 · 횟수 · 휴식. 설정해두면 루틴이나 운동화면에서 추가하면 바로 적용".
   S.종목설정[이름] = {세트,w,r,휴}. 없으면 지금 기본값(설정의 기본 세트 · 20kg · 10회 · 설정의 기본 휴식).
   무게 ± = 설정의 무게 조절 폭 · 휴식 15초씩 0:15~5:00(운동 화면 휴식 칸과 같은 범위) · 세트 1~10.
   플랜 종목은 플랜 처방이 우선(플랜넣기는 이 값을 안 쓴다) */
const 종목세트최대=10;
function 종목기본(이름){ return {세트:S.설정.기본세트, w:20, r:10, 휴:S.설정.기본휴식, ...(S.종목설정?.[이름]||{})}; }
function 종목설정잡기(이름){ S.종목설정 ||= {}; return S.종목설정[이름] ||= 종목기본(이름); }
function 종목세팅(이름, 플){ const b=종목기본(이름), v=esc(이름);
  const 칸=(f,글,입,말)=>`<div class="값칸"><button data-act="종목설정값" data-v="${v}" data-f="${f}" data-d="-1" aria-label="${말} 빼기">−</button>${입?`<input data-in="종목설정" data-v="${v}" data-f="${f}" inputmode="decimal" aria-label="${말}" value="${글}">`:`<span class="숫">${글}</span>`}<button data-act="종목설정값" data-v="${v}" data-f="${f}" data-d="1" aria-label="${말} 더하기">＋</button></div>`;
  return `<div class="종설"><div class="종설제목"><b>기본 세팅</b><span class="아주작 옅음 한줄">${플?"플랜으로 넣으면 플랜 처방대로":"루틴 · 운동에 넣을 때 이 값으로"}</span></div>
    <div class="종설머리 아주작 옅음"><span>세트</span><span>무게 kg</span><span>횟수</span><span>휴식</span></div>
    <div class="종설줄">${칸("세트",b.세트,false,"세트")}${칸("w",kg(b.w),true,"무게")}${칸("r",b.r,true,"횟수")}${칸("휴",분초(b.휴),false,"휴식")}</div></div>`; }
function 종목줄(x){ const l=사진목록(x.이름), 펼=U.종목펼침===x.이름,''')
바꿈('''${펼?`<div class="종목펼속" data-enter="펼속${esc(x.이름)}">${플.map(([p,i])=>플랜카드(p,i)).join("")}<div class="사진줄">''',
     '''${펼?`<div class="종목펼속" data-enter="펼속${esc(x.이름)}">${플.map(([p,i])=>플랜카드(p,i)).join("")}${종목세팅(x.이름, 플.length>0)}<div class="사진줄">''')
# 누르기
바꿈('''    case "종목펼침": U.종목펼침 = U.종목펼침===d.v ? null : d.v; break;''',
     '''    case "종목펼침": U.종목펼침 = U.종목펼침===d.v ? null : d.v; break;
    case "종목설정값": { const b=종목설정잡기(d.v), dd=+d.d;   // 10-04 v17 ⑤
      if(d.f==="세트") b.세트=Math.max(1,Math.min(종목세트최대,b.세트+dd));
      else if(d.f==="w") b.w=Math.max(0,Math.round((b.w+dd*S.설정.무게폭)*10)/10);
      else if(d.f==="r") b.r=Math.max(1,b.r+dd);
      else b.휴=Math.max(휴식최소,Math.min(휴식최대,Math.round((b.휴+dd*휴식폭)/휴식폭)*휴식폭));
      break; }''')
바꿈('''  else if(w==="루값"){ const s=루틴(U.루틴열림).종목[+d.i].세트[+d.k]; if(d.f==="w") s.w=Math.max(0,숫(v)); else s.r=Math.max(1,Math.round(숫(v))); }''',
     '''  else if(w==="루값"){ const s=루틴(U.루틴열림).종목[+d.i].세트[+d.k]; if(d.f==="w") s.w=Math.max(0,숫(v)); else s.r=Math.max(1,Math.round(숫(v))); }
  else if(w==="종목설정"){ const b=종목설정잡기(d.v); if(d.f==="w") b.w=Math.max(0,숫(v)); else b.r=Math.max(1,Math.round(숫(v))); }   // 10-04 v17 ⑤''')
바꿈('''  if(w==="루틴이름"||w==="루값"||w==="몸") 그리기(); });''',
     '''  if(w==="루틴이름"||w==="루값"||w==="몸"||w==="종목설정") 그리기(); });''')
# 종목넣기(루틴 · 운동 둘 다) 가 이 값을 쓴다
바꿈('''      else { const 세=세트들(S.설정.기본세트,20,10,S.설정.기본휴식); r.종목.push''',
     '''      else { const b=종목기본(d.v), 세=세트들(b.세트,b.w,b.r,b.휴); r.종목.push''')   # 10-04 v17 ⑤ 종목 탭 기본 세팅

css = '''
/* ═══ 10-04 v17 A — 운동 세트 줄 · 종목 기본 세팅 ═══ */
/* ② 끝난 세트 줄 바탕 — '조금 더 진하게'. --면2 와 --강조옅음 의 가운데 (새 값). 지금 줄(.지금 = --강조옅음)은 그대로 */
:root{--완료바탕:#E9EEF4}
@media (prefers-color-scheme: dark){ :root:not([data-theme="light"]){--완료바탕:#1B2D3F}}
:root[data-theme="dark"]{--완료바탕:#1B2D3F}
.운세트들 .세트줄.완료줄{background:var(--완료바탕)}
/* ① 쉬는 줄 — kg · 회 · 휴식 세 칸 자리를 게이지 단추 하나가 차지. 높이 = 칸 28 × 1.15 = 32.2 → 32 (누르는 높이 32).
   줄 높이(동그라미 36 + 위아래 4)는 그대로라 체크 전후로 화면이 안 움직인다 */
.운세트들 .쉼게이지{grid-column:2/5;height:32px;min-width:0;border:1px solid var(--강조);border-radius:8px;position:relative;overflow:hidden;padding:0;background:var(--면)}
.운세트들 .쉼게이지 .밑,.운세트들 .쉼게이지 .위{position:absolute;inset:0;display:flex;flex-direction:column;align-items:center;justify-content:center;line-height:1.1}
.운세트들 .쉼게이지 .밑{color:var(--강조)}
.운세트들 .쉼게이지 .위{background:var(--강조);color:var(--강조글)}
.운세트들 .쉼게이지 b{font-size:15px;font-weight:700;font-variant-numeric:tabular-nums}
.운세트들 .쉼게이지 small{font-size:11px;font-weight:700;white-space:nowrap;max-width:100%;overflow:hidden;text-overflow:ellipsis;padding:0 4px}
/* ④ '+ 세트' = 마지막 세트의 다음다음 줄 — 사이에 세트 줄 한 줄(44 = 동그라미 36 + 위아래 4) 빈 칸 */
.운세트들>.세트빈줄{flex:none;height:44px}
/* ⑤ 종목 탭 기본 세팅 — 루틴 세트 줄과 같은 − 값 ＋ 칸(높이 28). 칸 폭 = 들어갈 글자 폭대로 (세트 '10' · 무게 '102.5' · 횟수 '12' · 휴식 '1:30') */
.종설{display:flex;flex-direction:column;gap:4px}
.종설제목{display:flex;align-items:baseline;gap:8px;font-size:13px;min-width:0}
.종설제목 b{flex:none}
.종설머리,.종설줄{display:grid;grid-template-columns:minmax(0,60fr) minmax(0,100fr) minmax(0,76fr) minmax(0,82fr);gap:4px}
.종설머리 span{text-align:center;white-space:nowrap}
.종설줄 .값칸{height:28px}
.종설줄 .값칸 button{width:20px;flex:none}
.종설줄 .값칸 input{font-size:13px}
.종설줄 .값칸 span{font-size:13px}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]
pathlib.Path(OUT).write_text(s, encoding='utf-8'); print("v17 A →", OUT)
