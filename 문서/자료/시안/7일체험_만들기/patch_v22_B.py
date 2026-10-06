"""v22 B (10-06 홍겸 님) — 새 종목 시트
⑧ 근육 그림(svg 조각)을 누르면 바로 칠하지 않고 작은 팝업 — 누른 조각이 속한 묶음의 세부 부위가 줄줄이, 줄마다 [주동근][협응근][빼기].
   지금 값이 눌린 상태 · 누른 부위 줄은 --강조옅음 바탕 + --강조 굵은 글. 고르면 바로 그림에 반영(팝업은 그대로) · [확인] 이나 바깥 누름으로 닫힘.
   이미 주동근인 부위에 [협응근] = 막힘(흐림 · 토스트 — 기존 규칙). 그림 아래 묶음 · 역할 · 부위 칩 줄은 그대로(누르면 바로 토글)
⑨ 이름 칸 글자가 한 글자라도 바뀌면 근육 · 묶음을 비운다(편집이면 그대로). 이름을 정할 때 — 사전 이름이면 사전 값,
   아니고 이미 저장된 같은 이름 종목이 있으면 그 종목 근육, 둘 다 아니면 빈 채로(낱말 짐작 안 함).
   원인: 새확인 의 if(!U.새.고름) — 한 번 정한 뒤에는 사전에 '똑같이' 있는 이름(별칭 포함)만 다시 채우고, 그 밖의 이름
   ('레그프레스' · '바벨스쿼트' · '다리를들어요')은 앞 종목 근육이 그대로 남았다. 또 그림 조각 움직임 키(새몸-i)가 시트를 새로 열어도 남아
   새 시트가 앞 종목 색에서 0.6초 동안 바래며 시작했다 → 새초기 에서 그 키를 지운다
⑩ 저장할 때 주동근이 없으면 '근육 사진을 눌러서 목표 근육을 설정하세요'. 쓰이지 않는 옛 '종목만들기' 길도 막음
⑪ 팔굽혀펴기 카테고리 = 가슴 (기본 S.종목표 · 종목사전 칸). 사전 칸 '맨몸'은 카테고리 목록에 있어 자동 지정은 됐다 — 값이 맨몸이었을 뿐
쓰는 법: python3 patch_v22_B.py IN.html OUT.html"""
import sys, pathlib
IN, OUT = sys.argv[1], sys.argv[2]
s = pathlib.Path(IN).read_text(encoding='utf-8')
def 바꿈(old, new):
    global s
    c = s.count(old)
    assert c == 1, f"❌ {old[:80]!r}: {c}번"
    s = s.replace(old, new)

# ── ⑪ 팔굽혀펴기 = 가슴 ───────────────────────────────────────────────────
바꿈('''["팔굽혀펴기","맨몸"]''', '''["팔굽혀펴기","가슴"]''')
바꿈('''\n팔굽혀펴기|맨몸|푸시업/푸쉬업|\n''', '''\n팔굽혀펴기|가슴|푸시업/푸쉬업|\n''')

# ── ⑨ 새초기 — 그림 조각 움직임 키를 지운다(앞 시트 색에서 바래며 시작하지 않게) ──
바꿈('''const 새초기 = ()=>({찾는중:false, 고름:false, 사전:null, 근육:{}, 묶음:"가슴", 역할:"P", 칸직접:false, 세트:세트들(S.설정.기본세트,20,10,S.설정.기본휴식)});''',
     '''const 새초기 = ()=>{ for(const k in 앞값) if(k.startsWith("fill새몸-")) delete 앞값[k];   /* 10-06 v22 B ⑨ 새 시트는 빈 그림에서 바로 — 앞 종목 색에서 바래며 시작하지 않게 */
  return {찾는중:false, 고름:false, 사전:null, 정한:null, 팝:null, 근육:{}, 묶음:"가슴", 역할:"P", 칸직접:false, 세트:세트들(S.설정.기본세트,20,10,S.설정.기본휴식)}; };   // v22 B ⑨ 정한 = 마지막으로 정한 이름 · ⑧ 팝 = 그림에서 누른 부위''')

# ── ⑨ 사전적용 · 새확인 ──────────────────────────────────────────────────
바꿈('''U.새.근육=둘역할(사전근육(x)); U.새.사전=x.이름; U.새.묶음=기본묶음(); }''',
     '''U.새.근육=둘역할(사전근육(x)); U.새.사전=x.이름; U.새.정한=x.이름; U.새.묶음=기본묶음(); }''')
바꿈('''  const x=종목사전.find(y=>y.이름===n||y.별.includes(n));
  if(x){ if(U.새.사전!==x.이름) 사전적용(x); }
  else { U.새종목=n; const 있=S.종목표.find(t=>t.이름===n);   // v19 D ③ 사전에 없으면 카테고리 정보 없음 → 비움
    if(!U.새.칸직접) U.새종목칸 = 있&&S.카테고리.includes(있.칸) ? 있.칸 : null;
    if(!U.새.고름){ U.새.근육 = 둘역할(있 ? 세부로(종목근육(종목키(있))) : 세부로(낱말근육(n)||{})); U.새.묶음=기본묶음(); } U.새.사전=null; }   // v20 ⑤ S → Y
  if(!U.새.고름) 새속자리=0; U.새.찾는중=false; U.새.고름=true; return true; }''',
     '''  /* 10-06 v22 B ⑨ 정한 이름 그대로면(칸을 눌러 찾기만 켰다) 고친 근육 그대로. 이름이 바뀌었으면 언제나 새로 채운다 —
     사전 이름(별칭 포함)이면 사전 값 · 아니고 이미 저장된 같은 이름 종목이면 그 종목 근육 · 둘 다 아니면 빈 채로(낱말 짐작 안 함) */
  if(!(U.새.고름 && U.새.정한===n)){ const x=종목사전.find(y=>y.이름===n||y.별.includes(n));
    if(x) 사전적용(x);
    else { U.새종목=n; const 있=S.종목표.find(t=>t.이름===n);   // v19 D ③ 사전에 없으면 카테고리 정보 없음 → 비움
      if(!U.새.칸직접) U.새종목칸 = 있&&S.카테고리.includes(있.칸) ? 있.칸 : null;
      U.새.근육 = 있 ? 둘역할(세부로(종목근육(종목키(있)))) : {}; U.새.묶음=기본묶음(); U.새.사전=null; }   // v20 ⑤ S → Y
    새속자리=0; }
  U.새.정한=U.새종목; U.새.찾는중=false; U.새.고름=true; return true; }''')

# ── ⑨ 이름 칸 입력 — 한 글자라도 바뀌면 근육 · 묶음을 비운다(편집이면 그대로) ──
바꿈('''  else if(w==="새종목"){ U.새종목=v; if(U.새){ U.새.찾는중=true; 새찾기갱신(); } }''',
     '''  else if(w==="새종목"){ const 바뀜=v!==U.새종목; U.새종목=v; if(U.새){ U.새.찾는중=true;
      if(바뀜 && !U.새.편집){ U.새.근육={}; U.새.사전=null; U.새.정한=null; U.새.팝=null; U.새.묶음=기본묶음(); 새부위갱신(); }   // 10-06 v22 B ⑨
      새찾기갱신(); } }''')

# ── ⑨ 새부위갱신 · ⑧ 새팝 — 새찾기갱신 바로 뒤에 ──────────────────────────
바꿈('''  결.innerHTML=새찾기결과(); if(단) 단.outerHTML=새찾기단추(); }''',
     '''  결.innerHTML=새찾기결과(); if(단) 단.outerHTML=새찾기단추(); }
/* 10-06 v22 B ⑨ 이름을 치는 동안(다시 그리지 않아 칸이 안 닫힌다) 비운 근육을 그림 · 묶음 칩 · 부위 칩에만 바로 보인다 */
function 새부위갱신(){ const 속=document.querySelector("#폰 .새속"); if(!속||!U.새?.고름) return;
  const t=document.createElement("div"); t.innerHTML=새부위고르기();
  for(const q of [".새지도",".새묶음",".새근육들"]){ const a=속.querySelector(q), b=t.querySelector(q); if(a&&b) a.replaceWith(b); } }
/* 10-06 v22 B ⑧ 근육 그림을 누르면 — 누른 조각이 속한 묶음의 세부 부위 줄마다 [주동근][협응근][빼기](하나 고르기 = 칩).
   지금 값이 눌린 칩 · 누른 부위 줄은 --강조옅음 바탕. 고르면 바로 그림에 반영되고 팝업은 그대로 — [확인] · 바깥 누름으로 닫힘.
   주동근인 부위의 [협응근]은 막힘(그림 아래 칩 줄의 협응근 막힘과 같은 규칙 · 같은 토스트). 바깥 = 시트 전체를 덮는 투명한 막 */
const 팝역할 = [["P","주동근"],["Y","협응근"],["-","빼기"]];
function 새팝(){ const 새=U.새, m=새.근육, k=새.팝, 묶=세부부위.find(([,l])=>l.includes(k)); if(!묶) return "";
  const 칩=(p,r,글)=>{ const 켬=(m[p]||"-")===r, 막=r==="Y"&&m[p]==="P";
    return `<button class="칩${켬?" 켬":""}${막?" 막힘":""}" data-act="새팝역할" data-k="${p}" data-v="${r}" aria-pressed="${켬}"${막?' aria-disabled="true"':""}>${글}</button>`; };
  return `<div class="새팝가림" data-act="새팝닫기" data-enter="새팝가림"><div class="새팝" data-act="새팝안" role="dialog" aria-label="${esc(묶[0])} 세부 부위">
    <b class="새팝제목">${esc(묶[0])}</b>
    ${묶[1].map(p=>`<div class="새팝줄${p===k?" 누른":""}"><span class="채움 한줄">${esc(근이름(p))}</span>${팝역할.map(([r,글])=>칩(p,r,글)).join("")}</div>`).join("")}
    <button class="버튼 주 넓" data-act="새팝닫기">확인</button></div></div>`; }
/* 팝업이 떠 있을 때 시트 바깥(어두운 가림)을 눌러도 팝업만 닫는다 — 시트까지 닫지 않게. 가림의 [시트닫기]보다 먼저 받는다(같은 capture · 먼저 붙음) */
document.addEventListener("click", e=>{ if(!U.새?.팝 || U.시트?.종류!=="새종목") return; const g=e.target.closest?.("#폰 .가림"); if(!g || e.target!==g) return;
  e.stopImmediatePropagation(); e.preventDefault(); 행동("새팝닫기",{}); }, true);''')

# ── ⑧ 그림 조각 = 팝업 열기 (칩 줄은 그대로 data-act="새근육") ──────────────
바꿈('''${누?` data-act="새근육" data-v="${mm}"`:""}><title>''',
     '''${누?` data-act="새근육팝" data-v="${mm}"`:""}><title>''')   # v22 B ⑧
바꿈('''    ${새.고름?`<button class="버튼 주 넓 새저장" data-act="새저장">저장</button>`:""}`; }''',
     '''    ${새.고름?`<button class="버튼 주 넓 새저장" data-act="새저장">저장</button>`:""}${새.팝?새팝():""}`; }   // v22 B ⑧''')

# ── ⑧ case — case "새근육" 바로 뒤 ──────────────────────────────────────
바꿈('''      const g=세부부위.find(([,l])=>l.includes(d.v))?.[0]; if(g) U.새.묶음=g; break; }   // v19 D ④ 그림에서 눌러도 그 묶음으로''',
     '''      const g=세부부위.find(([,l])=>l.includes(d.v))?.[0]; if(g) U.새.묶음=g; break; }   // v19 D ④ 그림에서 눌러도 그 묶음으로
    /* 10-06 v22 B ⑧ 그림 조각 → 팝업(칠하지 않는다) · 팝업 칩 → 그 부위 역할 · [확인] · 바깥 → 닫힘 */
    case "새근육팝": { if(!U.새) break; U.새.팝=d.v; const g=세부부위.find(([,l])=>l.includes(d.v))?.[0]; if(g) U.새.묶음=g; break; }
    case "새팝역할": { const m=U.새?.근육; if(!m) break;
      if(d.v==="Y" && m[d.k]==="P"){ 토스트("이미 주동근으로 선택되어있습니다."); 다시=false; break; }   // 다시 그리면 토스트가 지워진다
      if(d.v==="-") delete m[d.k]; else m[d.k]=d.v; break; }
    case "새팝닫기": if(U.새) U.새.팝=null; break;
    case "새팝안": 다시=false; break;   // 팝업 안 빈 곳 — 바깥 막으로 번지지 않게''')

# ── ⑩ 주동근 없음 토스트 · 옛 '종목만들기' 길 ───────────────────────────────
바꿈('''토스트("주동근을 하나 이상 골라 주세요")''', '''토스트("근육 사진을 눌러서 목표 근육을 설정하세요")''')   # v22 B ⑩
바꿈('''    case "종목만들기": { const n=U.새종목.trim(); if(!n){''',
     '''    case "종목만들기": { if(!Object.values(U.새?.근육||{}).includes("P")){ 토스트("근육 사진을 눌러서 목표 근육을 설정하세요"); break; }   // 10-06 v22 B ⑩ 쓰이지 않는 옛 길 — 근육 없이 만들지 않게
      const n=U.새종목.trim(); if(!n){''')

# ── CSS — </style> 바로 앞 새 블록 (마지막 </style> = v21 블록 끝) ─────────────
css = '''/* ═══ 10-06 v22 B ⑧ 새 종목 시트 — 근육 그림 누름 팝업 ═══
   시트 전체를 덮는 투명한 막(바깥 누름 = 닫힘) · 팝업은 시트 아래쪽(그림이 가리지 않게) · 큰 상자 = 2 --강조 테두리 · 모서리 16 · 안 여백 14 (U1-6 · U3-4 · U3-7)
   줄 = 이름 15 + 칩 셋(28 · 하나 고르기 = 칩 U4-3) · 줄 높이 32 · 누른 줄 --강조옅음 바탕 + --강조 굵은 글 · [확인] 40(카드 안 버튼 U4-5) */
.시트:has(>.새속){position:relative}
.새팝가림{position:absolute;inset:0;z-index:4;display:flex;align-items:flex-end;padding:12px}
.새팝{width:100%;background:var(--면);border:2px solid var(--강조);border-radius:16px;padding:14px;display:flex;flex-direction:column;gap:4px}
.새팝제목{font-size:15px;font-weight:700;margin:0 4px 4px}
.새팝줄{display:flex;align-items:center;gap:4px;min-height:32px;padding:0 4px 0 8px;border-radius:8px}
.새팝줄>span{font-size:15px;font-weight:500;min-width:0}
.새팝줄.누른{background:var(--강조옅음)}
.새팝줄.누른>span{font-weight:700;color:var(--강조)}
.새팝 .칩{flex:none;justify-content:center;min-width:56px}
.새팝 .칩.막힘{opacity:.35;cursor:default}
.새팝>.버튼{margin-top:8px}
.새팝가림.들어옴{animation:흐려짐 .2s ease-out both}
'''
i = s.rfind('</style>', 0, s.index('<script>\n"use strict";'))   # 본 스크립트 바로 앞 </style> (v21 블록 끝)
assert s.count('@media (prefers-reduced-motion: reduce){ .운세트들 .세트줄.지금,.운띠 .운칸.지금,.노란점{animation:none!important} }\n</style>') == 1
s = s[:i] + css + s[i:]

pathlib.Path(OUT).write_text(s, encoding='utf-8')
print("✓ v22 B 적용 →", OUT)
