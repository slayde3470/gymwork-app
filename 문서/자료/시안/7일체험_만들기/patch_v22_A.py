"""v22 A (10-06 홍겸 님) — 루틴 화면 · 종목 넣기 시트
① 루틴 상세(+루틴 · 루틴 열기): 맨 위 「루틴」 띠(루틴 목록과 같은 .띠 가운데띠 루틴목록띠)가 그대로 남고, 그 아래 줄 = [‹ 뒤로][이름 칸 + 흐린 예상][취소]
② 「이 루틴 지우기」 큰 단추를 없애고 그 줄 오른쪽 [취소]가 지운다 — 두 번 눌러야 지워진다(두 번째는 '한 번 더' · --나쁨 글씨)
③ 「자동」 스위치 = 루틴 상세 화면 오른쪽 아래에 떠서 고정(스크롤해도 제자리 · 탭줄 위). 목록 맨 끝이 가리지 않게 아래 여백
④ 루틴 종목 상자 2열 격자. 펼친 상자는 한 줄 전체. 왼쪽을 펼치면 오른쪽 짝은 아래 줄로, 오른쪽을 펼치면 그 상자가 다음 줄 전체(순서는 그대로 · 앞 줄 오른쪽은 빈다)
   반 폭 상자 = 이름 한 줄(넘치면 …) + 둘째 줄 'N세트 ▾' (플랜 상자는 셋째 줄에 게이지 · %). 끌어서 순서 바꾸기는 반 폭 상자끼리 좌우로 판단
⑤ 종목 빼기 = ✕ → 휴지통 아이콘(아이콘.휴지통)
⑥ 종목 넣기 시트 띠 오른쪽 위 [닫기] → [확인] (동작 그대로 = 닫기)
⑦ 종목 넣기 시트 칸마다 연필(아이콘.연필) = 종목 탭 [편집]과 같은 편집 시트. 저장 · 닫기 · 끌어 닫기 → 종목 넣기 시트로 돌아온다. 연필은 넣기를 바꾸지 않는다
쓰는 법: python3 patch_v22_A.py IN.html OUT.html"""
import sys, pathlib
IN, OUT = sys.argv[1], sys.argv[2]
s = pathlib.Path(IN).read_text(encoding='utf-8')
def 바꿈(old, new):
    global s
    c = s.count(old)
    assert c == 1, f"❌ {old[:70]!r}: {c}번"
    s = s.replace(old, new)

# ── ① ③ 루틴 상세 = 「루틴」 띠 + [‹ 뒤로][이름][취소] 줄 + 떠 있는 자동 ─────────────
바꿈('''  if(r){ queueMicrotask(루끝단추맞춤); return `${루틴상세띠(r)}<div class="넘김 루넘김">${루틴상세(r)}</div>${세트지움띠()}`; }''',
     '''  /* 10-06 v22 A ① 상세에서도 맨 위 「루틴」 띠는 그대로(목록과 같은 띠) · 그 아래가 [‹ 뒤로][이름][취소] 줄
     ③ 자동 스위치는 화면 오른쪽 아래에 떠 있다(.화면 기준 · 탭줄 바로 위 · 스크롤과 상관없이 제자리) */
  if(r){ queueMicrotask(루끝단추맞춤); return `<div class="띠 가운데띠 루틴목록띠"><b class="채움">루틴</b></div>${루틴상세띠(r)}<div class="넘김 루넘김">${루틴상세(r)}</div><label class="루자동 루자동뜸"><span>자동</span>${스위치(r.자동생성,"루틴자동","",r.id)}</label>${세트지움띠()}`; }''')

바꿈('''function 루틴상세띠(r){ const 실=실제루틴(r);
  return `<div class="띠 루띠"><button class="작은흰" data-act="루틴닫기">‹ 루틴</button>''',
     '''/* 10-06 v22 A ① ② 이 줄 = [‹ 뒤로][이름 칸 + 흐린 예상][취소]. [취소] = 이 루틴 지우기 — 한 번 누르면 '한 번 더'(--나쁨) · 또 누르면 지운다.
   두 글을 한 칸에 겹쳐 두고 하나만 보이게 해서 글이 바뀌어도 단추 폭 · 이름 칸 폭이 그대로 */
function 루틴상세띠(r){ const 실=실제루틴(r), 확=U.확인==="루틴"+r.id;
  return `<div class="띠 루띠"><button class="작은흰" data-act="루틴닫기">‹ 뒤로</button>''')
바꿈('''    <label class="루자동"><span>자동</span>${스위치(r.자동생성,"루틴자동","",r.id)}</label></div>`; }''',
     '''    <button class="작은흰 루취소${확?" 확인중":""}" data-act="루틴지움" data-v="${r.id}" aria-label="${확?"한 번 더 누르면 이 루틴을 지웁니다":"이 루틴 지우기"}"><span>취소</span><span>한 번 더</span></button></div>`; }''')

# ── ② ④ 큰 지우기 단추 없앰 · 상자 2열 격자 ────────────────────────────────────
바꿈('''    ${r.종목.map((e,i)=>루틴종목상자(e,i)).join("")||`<div class="빈칸">종목을 넣어 주세요</div>`}
    ${r.종목.length?추가("끝"):""}`}
    <button class="버튼 넓 나쁨" data-act="루틴지움" data-v="${r.id}" style="margin-top:12px">${U.확인==="루틴"+r.id?"한 번 더 누르면 지웁니다":"이 루틴 지우기"}</button>`; }''',
     '''    ${r.종목.length?`<div class="루격자">${r.종목.map((e,i)=>루틴종목상자(e,i)).join("")}</div>`:`<div class="빈칸">종목을 넣어 주세요</div>`}
    ${r.종목.length?추가("끝"):""}`}`; }   /* 10-06 v22 A ② '이 루틴 지우기' 큰 단추 없음 — 띠 줄 [취소] · ④ 상자는 2열 격자(.루격자) */''')

# ── ④ ⑤ 상자 — 휴지통 · 반 폭 두 줄 · 펼친 상자 표시 ───────────────────────────
바꿈('''  const 빼기 = `<button class="닫기" data-act="종목빼기" data-i="${i}" aria-label="빼기">✕</button>`;''',
     '''  const 빼기 = `<button class="루빼기" data-act="종목빼기" data-i="${i}" aria-label="${esc(e.이름)} 빼기">${아이콘.휴지통}</button>`;   // 10-06 v22 A ⑤ ✕ → 휴지통''')
바꿈('''<span class="루플랜글 숫">${x?`${x.계.회}회차`:"목표 달성"}</span><span class="게이지3 루플랜게" aria-hidden="true">''',
     '''<span class="루플랜글 숫">${x?`${x.계.회}회차`:"목표 달성"}</span><i class="루줄바꿈" aria-hidden="true"></i><span class="게이지3 루플랜게" aria-hidden="true">''')   # v22 A ④ 반 폭 — 게이지 · % 는 셋째 줄
바꿈('''aria-expanded="${펼}">${번호이름(e.이름, 줄키(e))}<span class="루플랜글 숫">· ${e.세트.length}세트</span>''',
     '''aria-expanded="${펼}">${번호이름(e.이름, 줄키(e))}<span class="루플랜글 숫">${펼?"· ":""}${e.세트.length}세트</span>''')   # v22 A ④ 접힌(반 폭) 상자는 둘째 줄이라 '·' 없이
바꿈('''  return `<div class="종목상자" data-drag="종목줄" data-i="${i}" data-drop="종목줄">${머리}
    <div class="세트머리 루 아주작 옅음">''',
     '''  return `<div class="종목상자 루펼" data-drag="종목줄" data-i="${i}" data-drop="종목줄">${머리}
    <div class="세트머리 루 아주작 옅음">''')

# ── ④ 끌기 — 반 폭 상자 위에서는 좌우로 앞/뒤를 가른다(펼친 상자 위는 그대로 위아래) ──────
바꿈('''가로=끌.종류==="운칸"||끌.종류==="업적",''',
     '''가로=끌.종류==="운칸"||끌.종류==="업적"||(끌.종류==="종목줄"&&!!칸.closest(".루격자")&&!칸.classList.contains("루펼")),   /* 10-06 v22 A ④ 2열 */''')

# ── ⑥ 종목 넣기 시트 머리 [닫기] → [확인] ──────────────────────────────────────
바꿈('''    안 = 머리(`${esc(r.이름)}에 넣기`) + `<div class="넣기칩">''',
     '''    안 = 머리(`${esc(r.이름)}에 넣기`, "확인") + `<div class="넣기칩">''')   # 10-06 v22 A ⑥ 동작은 그대로(시트닫기)

# ── ⑦ 종목 넣기 칸마다 연필 ────────────────────────────────────────────────
바꿈('''    if(플.length) 플.forEach(p=>줄.push({이름:p.이름, p, 종목:x.이름})); else 줄.push({이름:x.이름, 키:종목키(x), 종목:x.이름}); }''',
     '''    if(플.length) 플.forEach(p=>줄.push({이름:p.이름, p, 종목:x.이름, 편:종목키(x)})); else 줄.push({이름:x.이름, 키:종목키(x), 종목:x.이름, 편:종목키(x)}); }   // 10-06 v22 A ⑦ 편 = 연필로 고칠 종목 열쇠''')
바꿈('''<span class="넣기번 숫">${n+1}</span><span class="이름플랜"><b>${esc(z.이름)}</b>${z.p?''',
     '''<span class="넣기번 숫">${n+1}</span><span class="이름플랜"><b class="이름맞춤" data-줄="2">${esc(z.이름)}</b>${z.p?''')   # v22 A ⑦ 연필 자리만큼 좁아진 이름 — 종목 탭 이름과 같은 맞춤(두 줄 → 글자 13 → 11 → 자간)
바꿈('''    return `<button class="넣기칸${수?" 들어감":""}"''',
     '''    /* 10-06 v22 A ⑦ 칸 = [넣기 단추 | 연필]. 단추 안에 단추를 둘 수 없어 묶음(.넣기묶음) 안에 형제로 — 연필은 칸 오른쪽 끝에 겹쳐 둔다.
       종목표에 없는 종목의 플랜(편 없음)은 연필 없음 */
    return `<div class="넣기묶음"><button class="넣기칸${수?" 들어감":""}"''')
바꿈('''<span class="넣기체크" aria-hidden="true">${수>3?"✓×"+수:"✓".repeat(수)}</span></button>`; }).join("")''',
     '''<span class="넣기체크" aria-hidden="true">${수>3?"✓×"+수:"✓".repeat(수)}</span></button>${z.편?`<button class="넣기편집" data-act="종목편집" data-v="${esc(z.편)}" aria-label="${esc(z.종목)} 편집">${아이콘.연필}</button>`:""}</div>`; }).join("")''')

# 편집 시트 — 종목 넣기 시트에서 열면 그 시트를 돌아감으로(닫기 · 끌어 닫기는 '시트닫기' 가 이미 돌아감을 본다)
바꿈('''      U.새.묶음=기본묶음(); 새속자리=0; U.시트={종류:"새종목", 돌아감:null}; break; }''',
     '''      U.새.묶음=기본묶음(); 새속자리=0; U.시트={종류:"새종목", 돌아감:U.시트?.종류==="종목넣기"?U.시트:null}; break; }   // 10-06 v22 A ⑦ 넣기 시트 연필 → 돌아올 곳''')

# 저장 — 종목고침()은 끝에서 시트를 닫는다. 본문(새 종목 시트 쪽)은 그대로 두고 겉에서 감싸 돌아감으로 보낸다
바꿈('''/* v18 D ③ 꾹 눌러 하나 빼기 — 맨 뒤에 들어간 것부터.''',
     '''/* 10-06 v22 A ⑦ 종목 넣기 시트에서 연필로 연 편집을 저장하면 넣기 시트로 돌아온다. 고친 종목이 다른 칸으로 옮겨 갔으면
   그 칸 칩을 골라 둔다(새 종목을 만들고 돌아올 때와 같게). 종목고침() 본문은 건드리지 않고 겉에서 감싼다 */
/* ⑦ 넣기 칸 이름 — 낱말 가운데서는 끊지 않고 줄여 맞춘다. 가장 작게(11 · 자간 끝)도 한 낱말이 칸 폭을 넘으면(플랜 딱지가 붙은 짧은 칸)
   그때만 낱말 안에서도 줄을 바꾸게(.끊음) 하고 한 번 더 맞춘다 — 잘려 안 보이는 것보다 낫다 */
이름맞춤 = (원 => function(root){ 원(root);
  (root||document).querySelectorAll(".넣기칸 b.이름맞춤:not(.끊음)").forEach(b=>{ if(b.scrollWidth>b.clientWidth+0.5){ b.classList.add("끊음"); 원(b.parentElement); } }); })(이름맞춤);
종목고침 = (원 => function(n){ const 돌=U.시트?.돌아감, 칸=U.새종목칸, 됨=원(n);
  if(됨 && 돌){ U.시트=돌; if(돌.종류==="종목넣기" && U.칸고름!=="전체" && U.칸고름!==칸) U.칸고름=칸; }
  return 됨; })(종목고침);
/* v18 D ③ 꾹 눌러 하나 빼기 — 맨 뒤에 들어간 것부터.''')

# ── CSS ──────────────────────────────────────────────────────────────────
# CSS 는 v21 끝 '@media (prefers-reduced-motion…) {…}' 줄 바로 앞에 넣는다 — 그 줄 + </style> 을 기준으로 쓰는 B · C · D 패치가 그대로 맞게
바꿈('''@media (prefers-reduced-motion: reduce){ .운세트들 .세트줄.지금,.운띠 .운칸.지금,.노란점{animation:none!important} }
</style>''', '''/* ═══ 10-06 v22 A — 루틴 상세(띠 줄 · 취소 · 떠 있는 자동 · 2열 상자 · 휴지통) · 종목 넣기 시트(확인 · 연필) ═══ */
/* ② [취소] — 두 글을 한 칸(격자 1/1)에 겹쳐 폭을 긴 쪽에 맞춘다. 두 번째 상태 = '한 번 더' · --나쁨 */
.루취소{display:grid}
.루취소>span{grid-area:1/1;text-align:center}
.루취소>span+span,.루취소.확인중>span:first-child{visibility:hidden}
.루취소.확인중>span+span{visibility:visible}
.루취소.확인중{color:var(--나쁨)}
/* ③ 떠 있는 자동 — .화면 오른쪽 아래 12 (탭줄 바로 위). 높이 40 · 모서리 16 · --면 바탕 · --속선 테 · 글 13 Bold.
   목록 아래 여백 = 12 + 40 + 12 (마지막 상자 · 끝 [+ 종목 추가]가 가리지 않게) */
.루자동뜸{position:absolute;right:12px;bottom:12px;z-index:4;height:40px;padding:0 8px 0 12px;gap:8px;background:var(--면);border:1px solid var(--속선);border-radius:16px;font-size:13px;color:var(--글)}
.넘김.루넘김{padding-bottom:calc(12px + 40px + 12px)}
/* ④ 2열 격자 — 가로 먼저. 펼친 상자(.루펼)는 한 줄 전체. 빈칸 채우기(dense)는 하지 않는다 = 순서 그대로
   (오른쪽 상자를 펼치면 다음 줄 전체로 내려가고 앞 줄 오른쪽은 빈다) */
.루격자{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:8px;margin-bottom:8px}
.루격자>.종목상자{margin-bottom:0;min-width:0}
.루격자>.루펼{grid-column:1/-1}
/* 반 폭 상자 = 머리만(상자를 꽉 채움 · 같은 줄 짝보다 낮으면 늘어난다). 1줄 이름(넘치면 …) · 2줄 N세트 ▾ (플랜: N세트 N회차 / 3줄 게이지 %) */
.루격자>.종목상자:not(.루펼){padding:0}
.루격자>.종목상자:not(.루펼)>.루머리{flex:1 1 auto;margin:0;border-radius:15px;align-items:center}
.루격자>.루접힘 .루접줄,.루격자>.루플랜 .루플랜줄{flex-wrap:wrap;column-gap:4px;row-gap:0}
.루격자>.루접힘 .루접줄>:first-child,.루격자>.루플랜 .루플랜줄>.이름플랜{flex:1 0 100%;max-width:100%;min-width:0;margin-right:0}
.루격자>.루접힘 .루접줄>b{overflow:hidden;text-overflow:ellipsis}
.루줄바꿈{display:none}
.루격자>.루플랜 .루줄바꿈{display:block;flex:1 0 100%;height:0}
.루격자>.루플랜 .루플랜줄>.루플랜게{flex:1 1 0;min-width:16px}
/* ⑤ 휴지통 — 누르는 칸 32 · 아이콘 18 · --옅음 (세트 줄 휴지통과 같은 색). 반 폭 상자에서는 오른쪽 위(이름 줄 높이) */
.루빼기{flex:none;width:32px;height:32px;display:inline-flex;align-items:center;justify-content:center;color:var(--옅음);border-radius:8px}
.루빼기 svg{width:18px;height:18px}
.루격자>.종목상자:not(.루펼) .루빼기{align-self:flex-start}
/* ④ 끌어 놓을 자리 — 머리가 상자를 덮으므로 선은 머리에. 반 폭 = 왼쪽/오른쪽 3 · 펼친 상자 = 위 3 (아래는 상자 바탕에 그대로) */
.루격자>.종목상자:not(.루펼).선위>.루머리{box-shadow:inset 3px 0 0 var(--강조)}
.루격자>.종목상자:not(.루펼).선아래>.루머리{box-shadow:inset -3px 0 0 var(--강조)}
.루격자>.루펼.선위>.루머리{box-shadow:inset 0 3px 0 var(--강조)}
/* ⑦ 종목 넣기 칸 + 연필 — 묶음이 격자 한 칸. 연필(누르는 칸 24 × 28 · 아이콘 16 · --옅음)은 칸 오른쪽 끝 안쪽 4 에 겹친다.
   넣기 단추 오른쪽 여백 = 4 + 24 → 체크 상자 바로 오른쪽이 연필 칸(눈에 보이는 틈 = 아이콘 둘레 4). 이름 자리를 덜 먹으려고 폭만 24
   (연필이 없는 칸도 같은 여백 = 체크 줄이 가지런) */
.넣기묶음{position:relative;display:grid;min-width:0}
.넣기묶음>.넣기칸{padding-right:28px}
.넣기편집{position:absolute;top:0;bottom:0;right:4px;margin:auto 0;width:24px;height:28px;display:flex;align-items:center;justify-content:center;color:var(--옅음);border-radius:8px}
.넣기편집 svg{width:16px;height:16px}
/* 이름은 맞춤이름 부품(이름맞춤)으로 — 두 줄까지 13 그대로, 안 들어가면 11 → 자간 좁힘. … 로 자르지 않는다(넣기칸의 2줄 자름 대신) */
.넣기칸 .이름플랜>b.이름맞춤{display:block;-webkit-line-clamp:none;font-size:13px;line-height:16px;max-height:none;overflow-wrap:normal}   /* 줄은 띄어쓰기에서만 바꾼다('데드리프/트' 처럼 낱말 가운데서 안 끊음) — 안 들어가면 글자를 줄인다 */
.넣기칸 .이름플랜>b.이름맞춤.끊음{overflow-wrap:anywhere}   /* 가장 작게도 안 들어가는 낱말만(이름맞춤 감싼 쪽이 붙인다) */
/* ═══ v22 A 끝 ═══ */
@media (prefers-reduced-motion: reduce){ .운세트들 .세트줄.지금,.운띠 .운칸.지금,.노란점{animation:none!important} }
</style>''')

pathlib.Path(OUT).write_text(s, encoding='utf-8')
print("✓ patch_v22_A →", OUT)
