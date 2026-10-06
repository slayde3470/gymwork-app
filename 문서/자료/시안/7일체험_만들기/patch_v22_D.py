"""v22 D (10-06 홍겸 님) — 운동 보고서(끝화면)
13-1 저장 시점 — 보고서가 열리는 그 순간(case "끝내기") 자동 저장. 세션은 닫지 않는다(‹ 로 돌아갈 수 있게).
     ‹ 로 돌아가 더 하고 다시 끝내면 **같은 키에 덮어쓴다** — 첫 저장 때 바뀐 것(기록 · 루틴 세트값 · 플랜 회차/측정/재기준 ·
     피로 · 최대볼륨 · 향상기록 · 미실시)을 세션(ss.저장)에 적어 두고, 다시 저장할 때 먼저 되돌린 뒤 같은 키로 다시 저장한다.
     (안 되돌리면 플랜 회차 +2 · 루틴 볼륨 두 번 오름). 체크한 세트가 없으면 저장하지 않는 규칙은 그대로 —
     앞서 저장했다면 그 기록도 되돌려 지운다. 날이 바뀔 때(세션정리)도 같은 키로 덮어쓴다.
     보고서 숫자(지난번 대비 ▲▼ · 큰 운동 · 플랜 미리보기)는 방금 저장한 자기 기록을 빼고 견준다.
13-2 아래 단추 셋(‹ 운동으로 돌아가기 · 운동 기록 저장하고 종료 · 기록 없이 종료)과 case "운동저장" · "운동버림" · ss.버림 · 그 CSS · 양끝글 삭제
13-3 왼쪽 아래 동그라미 ‹ = 운동으로(기존 case) · 오른쪽 아래 동그라미 › = 세션 닫고 스탯 화면(U.스탯 보기 "스탯").
     .화면 기준 absolute 로 떠 있어 보고서 목록을 넘겨도 제자리. 목록 아래 여백 = 단추 40 + 아래 12.
쓰는 법: python3 patch_v22_D.py IN.html OUT.html"""
import sys, pathlib
IN, OUT = sys.argv[1], sys.argv[2]
s = pathlib.Path(IN).read_text(encoding='utf-8')
def 바꿈(old, new):
    global s
    c = s.count(old)
    assert c == 1, f"❌ {old[:80]!r}: {c}번"
    s = s.replace(old, new)

# ── 13-1 운동저장하기 — 남김(세션 안 닫음) · 덮어쓰기 · 되돌리기 ─────────────────────
바꿈('''function 운동저장하기(){ const ss=S.세션; if(!ss) return null;
  if(!ss.종목.some(e=>e.세트.some(s=>s.완료))){ S.세션=null; 발자취("체크 없는 운동은 저장하지 않음"); return null; }   /* 22 버그 #4 */
  if(ss.끝==null) ss.끝=S.시계;
  const rec=세션기록(ss); let key=ss.날, n=2; while(S.기록[key]) key=`${ss.날}~${n++}`;
  const 향상앞=S.향상기록들.length;
  S.기록[key]=rec; 루틴반영(ss); 플랜반영(rec, ss.날); 피로저장(rec.종목, 모의시각()); delete S.미실시[ss.날]; S.세션=null; 발자취(`운동 저장 · ${rec.이름}`);
  업적알림(업적판정("저장", key, S.향상기록들.slice(향상앞))); 스탯남기기(); return key; }''',
'''/* 10-06 v22 D 13-1 — 남김=true 면 저장하고 세션은 둔다(보고서 ‹ 로 돌아갈 수 있게). 앞서 저장한 적이 있으면(ss.저장)
   그때 바꾼 것을 먼저 되돌리고 같은 키에 다시 저장한다 — 기록은 하나, 플랜 회차 · 루틴 볼륨은 한 번만 */
function 운동저장하기(남김){ const ss=S.세션; if(!ss) return null;
  const 옛키=저장되감기(ss);   /* 10-06 v22 D */
  if(!ss.종목.some(e=>e.세트.some(s=>s.완료))){ if(!남김) S.세션=null; 발자취("체크 없는 운동은 저장하지 않음"); return null; }   /* 22 버그 #4 */
  if(ss.끝==null) ss.끝=S.시계;
  const rec=세션기록(ss); let key=옛키||ss.날, n=2; if(!옛키) while(S.기록[key]) key=`${ss.날}~${n++}`;
  const 향상앞=S.향상기록들.length, r=루틴(ss.루틴id);
  const 앞={key, 루틴:r?r.종목.map(row=>row.세트.map(t=>({w:t.w,r:t.r,휴:t.휴}))):null,   /* 10-06 v22 D 되돌릴 것 */
    플랜:Object.fromEntries(S.플랜들.filter(p=>rec.종목.some(e=>e.플랜id===p.id)).map(p=>[p.id, JSON.stringify(p)])),
    피로:JSON.stringify(S.피로), 최대:JSON.stringify(S.최대볼륨), 미실시:S.미실시[ss.날]??null};
  S.기록[key]=rec; 루틴반영(ss); 플랜반영(rec, ss.날); 피로저장(rec.종목, 모의시각()); delete S.미실시[ss.날];
  앞.향상=S.향상기록들.slice(향상앞).map(h=>JSON.stringify(h));
  if(남김) ss.저장=앞; else S.세션=null;
  발자취(`운동 저장 · ${rec.이름}${옛키?" (덮어씀)":""}`);
  업적알림(업적판정("저장", key, S.향상기록들.slice(향상앞))); 스탯남기기(); return key; }
/* 10-06 v22 D — 앞서 보고서에서 저장한 것을 되돌린다(기록 지움 · 루틴 세트값 · 플랜 · 피로 · 최대볼륨 · 향상기록 · 미실시). 그 키를 돌려준다.
   업적은 '한 번 얻으면 취소 안 함' 그대로 둔다 */
function 저장되감기(ss){ const z=ss&&ss.저장; if(!z) return null; ss.저장=null;
  delete S.기록[z.key];
  const r=루틴(ss.루틴id); if(r&&z.루틴) r.종목.forEach((row,j)=>{ const 옛=z.루틴[j]; if(옛&&옛.length===row.세트.length) row.세트.forEach((t,k)=>Object.assign(t,옛[k])); });
  for(const [id,j] of Object.entries(z.플랜)){ const i=S.플랜들.findIndex(p=>p.id===id); if(i>=0) S.플랜들[i]=JSON.parse(j); }
  S.피로=JSON.parse(z.피로); S.최대볼륨=JSON.parse(z.최대);
  for(const h of z.향상||[]){ const i=S.향상기록들.map(x=>JSON.stringify(x)).lastIndexOf(h); if(i>=0) S.향상기록들.splice(i,1); }
  if(z.미실시!=null) S.미실시[ss.날]=z.미실시;
  return z.key; }''')

# 날이 바뀔 때 — 체크가 다 풀렸으면 앞서 저장한 것도 되돌린다
바꿈('''  if(!ss.종목.some(e=>e.세트.some(s=>s.완료))){ S.세션=null; 발자취("체크 없는 운동은 버림"); return; }''',
     '''  if(!ss.종목.some(e=>e.세트.some(s=>s.완료))){ 저장되감기(ss); S.세션=null; 발자취("체크 없는 운동은 버림"); return; }   /* 10-06 v22 D */''')

# ── 13-1 보고서 숫자 — 방금 저장한 자기 기록을 빼고 견준다 ──────────────────────────
바꿈('''const 앞목록=Object.entries(S.기록).filter(([k])=>저장키값?키순(k,저장키값)<0:k.split("~")[0]<=날)''',
     '''const 앞목록=Object.entries(S.기록).filter(([k])=>저장키값?키순(k,저장키값)<0:k.split("~")[0]<=날&&(!rec||k!==S.세션?.저장?.key))''')   # 10-06 v22 D
바꿈('''const 남=Object.entries(S.기록).filter(([k])=>k!==저장키값&&k.split("~")[0]<=날)''',
     '''const 남=Object.entries(S.기록).filter(([k])=>k!==(저장키값||ss?.저장?.key)&&k.split("~")[0]<=날)''')   # 10-06 v22 D
바꿈('''const 플글=x.플랜id?플랜미리(x,!!저장키값):"";''',
     '''const 플글=x.플랜id?플랜미리(x,!!저장키값||!!ss?.저장):"";''')   # 10-06 v22 D 저장됐으면 '반영됨'

# ── 13-2 · 13-3 보고서 아래 — 단추 셋 → 떠 있는 동그라미 ‹ › ──────────────────────
바꿈('''  return `<div class="화면"><div class="넘김 결과틀">''',
     '''  return `<div class="화면"><div class="넘김 결과틀${저장키값?"":" 떠있음"}">''')
바꿈('''      :`<div class="아랫줄 결과아래 결과셋"><button class="버튼 끝작 끝왼" data-act="운동으로" aria-label="운동으로 돌아가기"><i class="끝살" aria-hidden="true">‹</i>${양끝글("운동으로","돌아가기")}</button><button class="버튼 주 끝큰" data-act="운동저장">운동 기록 저장하고 종료</button><button class="버튼 끝작 끝오 나쁨${ss.버림?" 확인중":""}" data-act="운동버림"${ss.버림?' aria-label="다시 누르면 저장하지 않습니다"':' aria-label="기록없이 종료하기"'}>${ss.버림?`<span class="끝확인글">다시 누르면<br>저장하지 않습니다.</span>`:`${양끝글("기록없이","종료하기")}<i class="끝살" aria-hidden="true">›</i>`}</button></div>`}${시트()}</div>`; }''',
     '''      :`<button class="보고떠 왼" data-act="운동으로" aria-label="운동으로 돌아가기">${칩화살그림("M15 6l-6 6 6 6")}</button><button class="보고떠 오" data-act="보고스탯" aria-label="스탯 보기">${칩화살그림("M9 6l6 6-6 6")}</button>`}${시트()}</div>`; }   /* 10-06 v22 D 13-2 · 13-3 */''')
# 양끝글 — 부르는 곳이 없어졌다 (U4-6)
바꿈('''/* v18 홍겸 님 "8글자는 글자간격을 정렬" — 두 줄을 넓은 줄 폭에 맞춰 양끝 정렬 */
const 양끝글 = (...줄들) => `<span class="양끝" aria-hidden="true">${줄들.map(l=>`<span>${[...l].map(c=>`<i>${esc(c)}</i>`).join("")}</span>`).join("")}</span>`;
''', '')

# ── 13-1 · 13-2 · 13-3 행동 ────────────────────────────────────────────────
바꿈('''      if(ss&&ss.끝화면&&d.t!=="운동"){ 운동저장하기(); 토스트("저장했습니다"); }''',
     '''      if(ss&&ss.끝화면&&d.t!=="운동"){ S.세션=null; 발자취("보고서에서 나감 · 이미 저장됨"); }   // 10-06 v22 D 보고서가 열릴 때 이미 저장했다 — 세션만 닫는다''')
바꿈('''    case "끝내기": ss.끝화면=true; ss.끝=S.시계; ss.휴식=null; break;
    case "운동으로": ss.끝화면=false; ss.끝=null; ss.버림=false; break;
    case "운동저장": { const 키=운동저장하기(); U.탭="캘린더"; U.고른날=null; 토스트(키?"저장했습니다":"체크한 세트가 없어 저장하지 않았습니다"); break; }''',
     '''    case "끝내기": { ss.끝화면=true; ss.끝=S.시계; ss.휴식=null;   // 10-06 v22 D 13-1 보고서가 열리는 순간 저장(다시 끝내면 같은 기록에 덮어씀)
      const 키=운동저장하기(true); setTimeout(()=>토스트(키?"저장했습니다":"체크한 세트가 없어 저장하지 않았습니다"),0); break; }   // 다시 그린 뒤에 띄운다(옛 기록 없이 끝내기와 같은 방식)
    case "운동으로": ss.끝화면=false; ss.끝=null; break;
    case "보고스탯": S.세션=null; S.결과=null; U.탭="캘린더"; U.고른날=null; U.시트=null;   // 10-06 v22 D 13-3 › — 이미 저장됨 → 세션 닫고 스탯 화면
      U.스탯={보기:"스탯", 분류:"달성", 더:false, 고름:null, 단위:"일", 보고:false}; 발자취("보고서 › 스탯"); break;''')
바꿈('''    /* 10-03 m66q: 앱 마무리의 '기록 없이 끝내기' — 한 번 누르면 글이 바뀌고, 한 번 더 누르면 버린다 */
    case "운동버림": if(!ss.버림){ ss.버림=true; break; } S.세션=null; U.탭="캘린더"; U.고른날=null; 발자취("기록 없이 끝냄"); setTimeout(()=>토스트("기록 없이 끝냈습니다"),0); break;
''', '')

# ── CSS — 지운 단추의 CSS 삭제 ─────────────────────────────────────────────
바꿈('''/* 5 끝 단추 한 줄 — 돌아가기 1 : 저장 2 : 버림 1 (사이 8). 좁은 칸은 13 두 줄까지 */
.아랫줄.결과아래.결과셋{flex-direction:row;align-items:stretch}
.결과셋 .버튼{flex:1 1 0;min-width:0;height:40px;padding:0 4px;white-space:normal;word-break:keep-all;text-align:center;line-height:16px}
.결과셋 .버튼.끝큰{flex-grow:2}
/* '한 번 더 누르면 버립니다'(13 으로 141px)는 25% 칸(안쪽 71)에 13 두 줄로 못 들어간다 → 이때만 11 · 세 줄(줄 12) */
.결과셋 .버튼.확인중{font-size:11px;line-height:12px}
''', '')
바꿈('''/* 3 끝 단추 — 살표는 단추 왼쪽/오른쪽 세로 가운데(4 안쪽), 글은 가운데 두 줄(52). 안쪽 여백은 v17 그대로 4 —
   여백을 늘리면 flex 가 여백만큼 더 줘서 1:2:1 이 깨진다. 글이 가운데라 살표(4~13)와 안 겹친다 (360 에서도 글 왼끝 15) */
.결과셋 .버튼.끝작{position:relative}
.결과셋 .끝살{position:absolute;top:50%;transform:translateY(-50%);font-style:normal;font-size:15px;line-height:1}
.결과셋 .끝왼 .끝살{left:4px}
.결과셋 .끝오 .끝살{right:4px}
.결과셋 .양끝{display:inline-grid;line-height:16px}
.결과셋 .양끝>span{display:flex;justify-content:space-between}
.결과셋 .양끝 i{font-style:normal}
/* 4 버림 확인 — 빨간 상자 · 흰 글. 25% 칸에 11 로 (줄 12 · 길면 세 줄 = 36 ≤ 40) · 살표 없음 */
.결과셋 .버튼.끝작.확인중{background:var(--나쁨);border-color:var(--나쁨);color:var(--강조글);font-size:11px;line-height:12px}
''', '')

# ── CSS 새 블록 — </style> 마지막 것 바로 앞 ─────────────────────────────────
css = '''/* 10-06 v22 D 13-3 보고서 떠 있는 동그라미 ‹ (운동으로) · › (스탯) — .화면 기준 absolute: 목록을 넘겨도 제자리.
   지름 40(U3-3 보통 버튼) · 동그라미(U3-4) · 바탕 --면 · 테 1px --속선(U1-6 · U3-5 · .버튼과 같은 테) · 화살표 18(U3-6) · 좌우 · 아래 12(U3-2) */
.화면>.보고떠{position:absolute;bottom:12px;z-index:3;width:40px;height:40px;border-radius:50%;background:var(--면);border:1px solid var(--속선);color:var(--글);display:flex;align-items:center;justify-content:center}
.화면>.보고떠.왼{left:12px}
.화면>.보고떠.오{right:12px}
.화면>.보고떠 svg{width:18px;height:18px}
/* 목록 끝이 단추에 가리지 않게 — 아래 여백 = 단추 40 + 아래 12 (결과틀 아래 여백 8 이 단추 위 틈이 된다). 이미지 찍기(.보고찍틀)에는 안 붙는다 */
.화면>.결과틀.떠있음 .보고목록{padding-bottom:calc(40px + 12px)}
'''
끝 = s.rfind('</style>')
assert 끝 > 0, '</style> 없음'
s = s[:끝] + css + s[끝:]

for 남은 in ('운동버림', '"운동저장"', '결과셋', '양끝글', 'ss.버림'):
    assert 남은 not in s, f'❌ 아직 남음: {남은}'
pathlib.Path(OUT).write_text(s, encoding='utf-8')
print('✓ v22 D 적용 →', OUT)
