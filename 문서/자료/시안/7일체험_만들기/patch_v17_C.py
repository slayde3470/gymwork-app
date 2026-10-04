"""v17 C (10-04 홍겸 님) — 프로필 탭 · 최근 업적 줄 · 인증판 · 탭줄 · 돋보기 화면 · SNS 링크
python3 patch_v17_C.py IN OUT

① "프로필 - 누르면 상,하,좌,우 랜덤 50px에서 페이드인되면서 제자리잡기. 페이드인되는시간 2배로"
   → 묶음(머리 · 업적 줄 · 사진판)마다 상하좌우 중 하나에서 50px 떨어져 들어옴. 시간 = 화면 들어옴(.들어옴 .24s) × 2 = .48s
② "업적은 최근 달성순 · 정렬(최신순, 오래된순, 가나다순)은 닉네임 옆 설정창 · 많으면 좌우 스크롤(휠도) · 꾸욱 눌러 순서 변경 ·
   새 업적은 제일 왼쪽" → S.설정.업적정렬 · S.업적순서
③ "맨 위 '프로필' 줄은 없애"
④ "'메모'는 맨 아래 탭에 · 돋보기 아이콘도 하나" → 탭줄 8칸, 돋보기 = 새 화면(준비 중). 북마크는 나중
⑤ "닉네임 쓸 때 오른쪽 링크 아이콘 → SNS 주소 칸 · 있으면 닉네임 옆 흐리고 작은 링크 아이콘 → 누르면 주소 팝업"
⑥ "프로필 화면에 스크롤 없게 · 사진은 8장, 한 줄에 4장"
새 값: 들어옴 거리 50 · 시간 .48s · 링크 아이콘 15(흐림 .5) · 링크 단추 28 · 인증샷 8장 4열
"""
import sys, pathlib
IN, OUT = sys.argv[1], sys.argv[2]
s = pathlib.Path(IN).read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:70]!r}: {c}번")
    s = s.replace(old, new)

# ── 아이콘: 돋보기 · 링크 ──
바꿈("    핀:a('<path d=\"M9 3h6l-1 6 3 3v2H7v-2l3-3z\"/><path d=\"M12 14v7\"/>'),",
     "    핀:a('<path d=\"M9 3h6l-1 6 3 3v2H7v-2l3-3z\"/><path d=\"M12 14v7\"/>'),\n"
     "    돋보기:a('<circle cx=\"11\" cy=\"11\" r=\"7\"/><path d=\"M20 20l-4-4\"/>'),\n"
     "    링크:a('<path d=\"M10 14a4 4 0 0 0 5.7 0l3-3a4 4 0 0 0-5.7-5.7l-1 1\"/><path d=\"M14 10a4 4 0 0 0-5.7 0l-3 3a4 4 0 0 0 5.7 5.7l1-1\"/>'),")

# ── ④ 탭줄 8칸: 캘린더 · 루틴 · 플랜 · 종목 · 설정 · 메모 · 검색(돋보기) · [프로필 사진] ──
바꿈('''function 탭줄(){ const 탭들=[["캘린더",아이콘.달력],["루틴",아이콘.루틴],["플랜",아이콘.과녁],["종목",아이콘.바벨],["설정",아이콘.톱니]];''',
     '''function 탭줄(){ const 탭들=[["캘린더",아이콘.달력],["루틴",아이콘.루틴],["플랜",아이콘.과녁],["종목",아이콘.바벨],["설정",아이콘.톱니],["메모",아이콘.연필],["검색",아이콘.돋보기]];   // 10-04 v17 메모 · 돋보기''')
바꿈('''class="${U.탭===t?"켬":""}">${i}<span>${t}</span></button>`).join("")}<button data-act="탭" data-t="프로필"''',
     '''class="${U.탭===t||(t==="메모"&&U.시트?.종류==="메모")?"켬":""}">${i}<span>${t}</span></button>`).join("")}<button data-act="탭" data-t="프로필"''')
# 메모 탭 = 메모 시트 (v9 전 방식 그대로 — 보던 화면 위에 시트)
바꿈('''    case "탭":\n''', '''    case "탭": if(d.t==="메모"){ U.시트={종류:"메모"}; break; }   // 10-04 v17 메모는 탭줄에서 — 보던 화면 위에 시트\n''')
바꿈('''U.탭==="프로필"?프로필탭():설정탭();''', '''U.탭==="프로필"?프로필탭():U.탭==="검색"?검색탭():설정탭();''')

# ── ①③⑤⑥ 프로필 탭 ──
# 프로필탭 · 최근업적줄 · 인증순 · 인증판 (v11 묶음) 을 통째로 갈아 끼운다
i = s.index('function 프로필탭(){'); j0 = s.index('function 인증판(){', i); j = s.index('</div>`; }', j0) + len('</div>`; }')
s = s[:i] + '''function 프로필탭(){ const 닉=String(S.설정.닉네임||""), 링=링크목록();
  /* 10-04 v17 ① 묶음마다 상하좌우 중 하나에서 50px 떨어져 들어온다 (화면에 들어올 때만 · .넘김.들어옴) */
  const 방=()=>{ const [x,y]=[[0,-50],[0,50],[-50,0],[50,0]][Math.floor(Math.random()*4)]; return ` style="--들x:${x}px;--들y:${y}px"`; };
  return `<div class="넘김 프로필넘김"><div class="쌓기">
    <div class="인머리 프묶음 번호"${번("프0")}${방()}>${프로필고르기("크게")}<div class="인오른">
      <div class="인닉줄"><input class="인닉" data-pf="닉네임" maxlength="12" autocomplete="off" placeholder="닉네임" aria-label="닉네임" value="${esc(닉)}">${링.length?`<button class="인링크보기" data-act="시트" data-t="링크목록" aria-label="SNS 링크 보기">${아이콘.링크}</button>`:""}<button class="인링크단추" data-act="시트" data-t="링크" aria-label="SNS 링크 적기">${아이콘.링크}</button></div>
      ${큰운동판(큰운동값(null,null), false, false)}</div>${톱니단추()}</div>
    ${최근업적줄(방())}
    ${인증판(방())}
  </div></div>`; }
/* 10-04 v17 ② 달성한 업적 전부 — 한 줄 가로 스크롤. 정렬 = S.설정.업적정렬 '최신순'(기본) · '오래된순' · '가나다순' · '직접'(꾹 눌러 끌었을 때)
   '직접'이면 S.업적순서(업적 번호) 차례. 순서에 없는 새 업적은 맨 왼쪽(새것이 더 왼쪽) */
function 업적목록(){ const 얻=S.업적||{}, 새먼저=(a,b)=>String(얻[b].날||"").localeCompare(String(얻[a].날||""))||(얻[b].순||0)-(얻[a].순||0);
  const 키들=Object.keys(얻).filter(k=>업적찾기(k)), 법=S.설정.업적정렬||"최신순";
  let 목;
  if(법==="오래된순") 목=키들.sort(새먼저).reverse();
  else if(법==="가나다순") 목=키들.sort((a,b)=>String(업적찾기(a).칭호).localeCompare(String(업적찾기(b).칭호),"ko"));
  else if(법==="직접"){ const 순=(S.업적순서||[]).map(String).filter(k=>얻[k]&&업적찾기(k)); 목=[...키들.filter(k=>!순.includes(k)).sort(새먼저), ...순]; }
  else 목=키들.sort(새먼저);
  return 목.map(업적찾기); }
function 최근업적줄(방=""){ const 목=업적목록();
  if(!목.length) return `<div class="인업적 빈 프묶음 작 옅음 번호"${번("프3")}${방}>아직 달성한 업적이 없습니다</div>`;
  return `<div class="인업적 프묶음 번호"${번("프3")}${방}>${목.map((a,i)=>`<button class="인업 등급-${a.숨김?"숨은":a.등급}" data-act="스탯열기" data-v="업적" data-drag="업적" data-drop="업적" data-i="${i}"><span class="인동">${esc([...a.칭호][0]||"")}</span><span class="인업글">${esc(a.칭호)}</span></button>`).join("")}</div>`; }
/* 운동 인증샷 — 10-04 v17 최대 8장 · 3:4 · 한 줄 4칸 (스크롤 없이). 고정(최대 3장)이 맨 앞, 나머지는 새것부터. 이 브라우저에만 */
const 인증최대 = 8;
function 인증순(){ return [...인증.filter(x=>x.고정).sort((a,b)=>a.고정-b.고정), ...인증.filter(x=>!x.고정).sort((a,b)=>b.때-a.때)]; }
function 인증판(방=""){ const 목=인증순();
  return `<div class="인판 프묶음 번호"${번("프2")}${방}>${목.map(x=>`<button class="인칸" data-act="시트" data-t="인증" data-v="${x.id}" aria-label="인증샷${x.고정?" (고정)":""}"><img src="${x.src}" alt="">${x.고정?`<span class="인핀">${아이콘.핀}</span>`:""}</button>`).join("")}${목.length<인증최대?`<label class="인칸 인더" aria-label="인증샷 올리기">＋<input type="file" accept="image/*" multiple data-pf="인증" hidden></label>`:""}</div>`; }
/* 10-04 v17 ⑤ SNS 링크 — 최대 3개. 앞에 아무것도 없으면 https:// 를 붙이고, http(s) 가 아닌 꼴은 저장하지 않는다 */
function 링크목록(){ return (Array.isArray(S.설정.링크)?S.설정.링크:[]).filter(x=>typeof x==="string"&&x.trim()); }
const 링크주소 = v => { v=String(v||"").trim(); return !v ? "" : /^https?:\\/\\//i.test(v) ? v : /^[a-z][a-z0-9+.-]*:/i.test(v) ? "" : "https://"+v; };   // http(s) 아닌 꼴(javascript: 등)은 버린다
/* 10-04 v17 ④ 돋보기 — 나중에 다른 유저 사진 · 피드 · 검색. 지금은 자리만 (다른 유저 화면의 톱니 자리 = 북마크, 나중) */
function 검색탭(){
  return `<div class="넘김 찾화면"><div class="쌓기">
    <input class="입력" type="search" placeholder="유저 검색" aria-label="유저 검색" autocomplete="off">
    <div class="작 옅음">다른 사람 사진 · 검색은 준비 중</div>
    <div class="찾판">${Array.from({length:9},()=>`<div class="찾칸"></div>`).join("")}</div>
  </div></div>`; }''' + s[j:]

# ⑥ 예전에 12장까지 저장했으면 앞 8장만 (보이는 차례 그대로 · 다음 저장 때 8장으로 남는다)
바꿈('let 인증 = []; try{ 인증 = JSON.parse(localStorage.getItem(저장키+"-인증")||"[]"); }catch(e){}',
     'let 인증 = []; try{ 인증 = JSON.parse(localStorage.getItem(저장키+"-인증")||"[]"); }catch(e){}\nif(인증.length>8) 인증 = 인증순().slice(0,8);   // 10-04 v17 인증샷 8장')
바꿈('  for(const f of fs){ if(인증.length>=12){ 넘침=true; break; }', '  for(const f of fs){ if(인증.length>=인증최대){ 넘침=true; break; }')
바꿈('if(넘침) 토스트("인증샷은 12장까지");', 'if(넘침) 토스트("인증샷은 8장까지");')

# ── 시트: SNS 링크 적기 · 링크 목록 · 톱니 시트에 '업적 정렬' ──
바꿈('  else if(종==="보고방식") 안 = 보고방식시트();',
     '''  else if(종==="보고방식") 안 = 보고방식시트() + (U.탭==="프로필"&&!S.결과 ? `<div class="이름표">업적 정렬</div>${칩줄("설정칩","업적정렬",["최신순","오래된순","가나다순"],S.설정.업적정렬||"최신순")}${S.설정.업적정렬==="직접"?`<div class="아주작 옅음">지금은 꾹 눌러 옮긴 순서</div>`:""}` : "");   // 10-04 v17 ②
  else if(종==="링크"){ const 링=링크목록();   // 10-04 v17 ⑤ SNS 주소 칸 3개
    안 = 머리("SNS 링크") + [0,1,2].map(i=>`<input class="입력" data-in="링크" type="url" inputmode="url" autocomplete="off" placeholder="https://" aria-label="SNS 주소 ${i+1}" value="${esc(링[i]||"")}">`).join("") + `<button class="버튼 주 넓" data-act="링크저장">저장</button>`; }
  else if(종==="링크목록"){ const 링=링크목록().map(링크주소).filter(Boolean);
    안 = 머리("SNS 링크") + 링.map(u=>`<a class="고르기 링크줄" href="${esc(u)}" target="_blank" rel="noopener noreferrer"><span class="링크아이콘">${아이콘.링크}</span><b class="채움">${esc(u.replace(/^https?:\\/\\//i,"").replace(/\\/$/,""))}</b>›</a>`).join(""); }''')
바꿈('    case "메모넣기":', '''    case "링크저장": S.설정.링크=[...document.querySelectorAll('#폰 [data-in="링크"]')].map(el=>링크주소(el.value)).filter(Boolean).slice(0,3); U.시트=null; break;   // 10-04 v17 ⑤
    case "메모넣기":''')
# '업적정렬' 칩은 기존 '설정칩'(글이면 글 그대로)으로 저장된다

# ── ② 꾹 눌러 끌기 — 업적 줄(가로). 체계는 그대로(data-drag / data-drop) ──
바꿈('글:el.querySelector("b,.루")?.textContent||""};', '글:el.querySelector("b,.루,.인업글")?.textContent||""};')
바꿈('''  if(끌.종류==="운칸"){ const 줄=끌.el.closest(".운띠"), r=줄?.getBoundingClientRect();''',
     '''  if(끌.종류==="업적"){ const 줄=끌.el.closest(".인업적"), r=줄?.getBoundingClientRect(); if(r){ if(e.clientX<r.left+32) 줄.scrollLeft-=10; else if(e.clientX>r.right-32) 줄.scrollLeft+=10; } }
  if(끌.종류==="운칸"){ const 줄=끌.el.closest(".운띠"), r=줄?.getBoundingClientRect();''')
바꿈('가로=끌.종류==="운칸", 아래=', '가로=끌.종류==="운칸"||끌.종류==="업적", 아래=')
바꿈('''  else if(x.종류==="플랜"){ 옮김(S.플랜들,x.원,x.대상); }''',
     '''  else if(x.종류==="플랜"){ 옮김(S.플랜들,x.원,x.대상); }
  else if(x.종류==="업적"){ const 목=업적목록().map(a=>String(a.번호)); if(옮김(목,x.원,x.대상)){ S.업적순서=목; S.설정.업적정렬="직접"; 발자취("업적 순서 바꿈"); } }   // 10-04 v17 ②''')

css = '''
/* ═══ 10-04 v17 C — 프로필 탭 · 업적 줄 · 인증샷 8장 · 탭줄 8칸 · 돋보기 · SNS 링크 ═══ */
/* ① 화면에 들어올 때 묶음마다 상하좌우 50 에서 · 시간 = 화면 들어옴(.24s) × 2. 화면 전체 움직임은 끈다(묶음이 대신) */
@keyframes 프들어옴{from{opacity:0;transform:translate(var(--들x,0),var(--들y,0))}to{opacity:1;transform:none}}
.넘김.프로필넘김{overflow-x:hidden}
.넘김.프로필넘김.들어옴{animation:none}
.넘김.프로필넘김.들어옴 .프묶음{animation:프들어옴 .48s ease-out both}
/* ③ 제목 줄이 없어진 자리 — 머리 위 여백 8 */
.프로필넘김>.쌓기{padding-top:8px}
/* ② 업적 줄 — 한 줄 · 가로 스크롤(스크롤바는 .폰 * 에서 숨김) · 꾹 눌러 좌우 끌기와 함께 */
.인업적{flex-wrap:nowrap;overflow-x:auto;overscroll-behavior-x:contain}
.인업적 .인업[data-drag]{touch-action:pan-x}
.인업.끌림{opacity:.5}
.인업.선위{box-shadow:inset 3px 0 0 var(--강조)}
.인업.선아래{box-shadow:inset -3px 0 0 var(--강조)}
/* ⑤ 닉네임 줄 — 칸은 글 길이만큼, 바로 옆에 흐리고 작은 링크(15). 쓰는 동안(닉네임 칸에 포커스)만 오른쪽에 링크 단추(28)
   :focus-within 이 아니라 :has(.인닉:focus) — 흐린 링크 단추를 누르는 순간 단추가 포커스를 받아 바뀌어 버리지 않게 */
.인닉줄{display:flex;align-items:center;gap:4px;height:28px;min-width:0;padding-right:28px}
.인닉줄 .인닉{field-sizing:content;width:auto;min-width:4em;max-width:100%;flex:0 1 auto}
.인닉줄:has(.인닉:focus) .인닉{flex:1 1 auto}
.인링크보기{flex:none;width:28px;height:28px;display:flex;align-items:center;justify-content:center;color:var(--흐림);opacity:.5}
.인링크보기 svg{width:15px;height:15px}
.인링크단추{display:none;flex:none;width:28px;height:28px;align-items:center;justify-content:center;color:var(--강조)}
.인링크단추 svg{width:18px;height:18px}
.인닉줄:has(.인닉:focus) .인링크단추{display:flex}
.인닉줄:has(.인닉:focus) .인링크보기{display:none}
.링크줄{text-decoration:none;color:var(--글)}
.링크줄 b{font-size:15px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;min-width:0}
.링크아이콘{flex:none;display:flex;color:var(--강조)}
.링크아이콘 svg{width:18px;height:18px}
/* ⑥ 인증샷 8장 · 한 줄 4칸 · 3:4 */
.인판{grid-template-columns:repeat(4,minmax(0,1fr))}
.인칸{font-size:22px}
/* ④ 탭줄 8칸 — 폭 360 에서 한 칸 45. 글이 넘치면 자르지 않고 칸 안에서 자간만 */
.탭줄 button{min-width:0;padding-left:0;padding-right:0}
.탭줄 button span{white-space:nowrap;letter-spacing:-0.04em}
/* ④ 돋보기 화면 — 빈 사진 격자 자리(3열 · 3:4 · 사이 2) */
.찾화면>.쌓기{padding-top:12px}
.찾판{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:2px}
.찾칸{aspect-ratio:3/4;background:var(--면2)}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]

js = '''
<script>
/* 10-04 v17 C — 업적 줄: 마우스 휠(세로)도 가로로 · 넘긴 자리 기억 · 링크 단추를 눌러도 닉네임 칸이 포커스를 잃지 않게 */
(()=>{ const 폰=document.getElementById("폰"); let 업x=0, 업수=-1;
  document.addEventListener("wheel", e=>{ const l=e.target.closest?.(".인업적"); if(!l||l.scrollWidth<=l.clientWidth) return;
    if(Math.abs(e.deltaY)>Math.abs(e.deltaX)){ l.scrollLeft+=e.deltaY; e.preventDefault(); } }, {passive:false});
  document.addEventListener("scroll", e=>{ if(e.target?.classList?.contains("인업적")) 업x=e.target.scrollLeft; }, true);
  new MutationObserver(()=>{ const l=폰.querySelector(".인업적"); if(!l) return; const n=l.children.length;
    if(n!==업수){ 업수=n; 업x=0; } l.scrollLeft=업x; }).observe(폰,{childList:true});
  document.addEventListener("mousedown", e=>{ if(e.target.closest?.(".인링크단추")) e.preventDefault(); });
})();
</script>
'''
끝 = s.rfind('</body>'); s = s[:끝] + js + s[끝:]
pathlib.Path(OUT).write_text(s, encoding='utf-8'); print("v17 C →", OUT, f"{len(s.encode()):,} 바이트")
