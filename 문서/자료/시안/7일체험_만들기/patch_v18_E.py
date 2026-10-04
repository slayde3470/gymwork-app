"""v18 E (10-04 홍겸 님) — 탭줄 순서 · 업적 줄 밀기 · 업적 '프로필' 체크 · 닉네임 줄 맞춤 · SNS 링크 칸 · 돋보기 화면 · 당겨서 새로고침
python3 patch_v18_E.py IN OUT

① "탭 순서: 캘린더 -> 돋보기 -> 루틴 -> 종목 -> 메모 -> 설정 -> 프로필"
   → 플랜 탭은 목록에 없어 지우지 않고 종목 다음에 둔다 (캘린더 · 돋보기 · 루틴 · 종목 · 플랜 · 메모 · 설정 · 프로필) — 홍겸 님께 확인할 일
② "업적 동그라미들이 화면을 넘어갔는데 좌우 휠이 되도록" → 손가락 가로 밀기 보조 · 마우스로 끌어 밀기 (휠은 v17 그대로)
③ "표시할 업적과 그렇지 않은 업적을 '업적' 화면에서 체크/해제. 기본은 전부 표시" → S.업적숨김 = {번호:true}
④ "프로필 이름 칸을 '3대 nkg 박스' 왼쪽 끝단이랑 맞춰 · 이름 줄을 아래로 25%" → 닉네임 줄 왼쪽 = 파란 상자 왼쪽(그린 뒤 재서) · 아래로 7 (줄 28 × .25)
⑤ "SNS 링크 칸 — 기본 1개, + 버튼으로 몇 개든"
⑥ 돋보기: 설명 삭제 · 사진 최대 12장(3 × 4, 3:4, 스크롤 없이) · [북마크] [돋보기 아이콘 + 흐린 '검색']
⑦ "모든 화면 — 맨 위에서 아래로 당기면 빈 줄 + 로딩 아이콘 0.5초 + 새로고침"
새 값: 닉네임 줄 아래로 7 · 체크 칸 폭 44 · 네모 18 · 북마크 단추 40 · 검색 칸 40 · 사진 12장 · 당김 줄 40 · 최대 56 · 따라옴 .5 · 도는 시간 .5s · 접힘 .2s
"""
import sys, pathlib
IN, OUT = sys.argv[1], sys.argv[2]
s = pathlib.Path(IN).read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:70]!r}: {c}번")
    s = s.replace(old, new)

# ── 아이콘: 북마크 ──
바꿈("    돋보기:a('<circle cx=\"11\" cy=\"11\" r=\"7\"/><path d=\"M20 20l-4-4\"/>'),",
     "    돋보기:a('<circle cx=\"11\" cy=\"11\" r=\"7\"/><path d=\"M20 20l-4-4\"/>'),\n"
     "    북마크:a('<path d=\"M6 3h12v18l-6-4.5L6 21z\"/>'),   // 10-04 v18 E ⑥")

# ── ① 탭줄 순서 ──
바꿈('''const 탭들=[["캘린더",아이콘.달력],["루틴",아이콘.루틴],["플랜",아이콘.과녁],["종목",아이콘.바벨],["설정",아이콘.톱니],["메모",아이콘.연필],["검색",아이콘.돋보기]];   // 10-04 v17 메모 · 돋보기''',
     '''const 탭들=[["캘린더",아이콘.달력],["검색",아이콘.돋보기],["루틴",아이콘.루틴],["종목",아이콘.바벨],["플랜",아이콘.과녁],["메모",아이콘.연필],["설정",아이콘.톱니]];   // 10-04 v18 E ① 캘린더 · 돋보기 · 루틴 · 종목 · (플랜 — 홍겸 님 목록에 없어 종목 다음에 둠, 확인할 일) · 메모 · 설정 · 프로필''')

# ── ③ 업적 화면 — 달성한 업적마다 오른쪽 '프로필' 체크 칸. 업적줄(대표 칭호 누름)과 따로인 형제 단추라 같이 눌리지 않는다 ──
바꿈('''  if(얻) return `<button class="업적줄 풀림" data-act="업적누름" data-v="${a.번호}" data-enter="업${a.번호}1">${머리}
    ${a.문구?`<div class="작">${esc(a.문구)}</div>`:""}<div class="조건글">${날글(얻.날)} 달성 · ${esc(a.조건)}</div>${a.플레이버?`<div class="플레이버">${esc(a.플레이버)}</div>`:""}</button>`;''',
     '''  if(얻) return `<div class="업적줄묶음"><button class="업적줄 풀림" data-act="업적누름" data-v="${a.번호}" data-enter="업${a.번호}1">${머리}
    ${a.문구?`<div class="작">${esc(a.문구)}</div>`:""}<div class="조건글">${날글(얻.날)} 달성 · ${esc(a.조건)}</div>${a.플레이버?`<div class="플레이버">${esc(a.플레이버)}</div>`:""}</button>${업적보임칸(a)}</div>`;''')
바꿈('''function 업적줄(a){''',
     '''/* 10-04 v18 E ③ 프로필에 보이기 — 기본은 전부 보임. 끈 것만 S.업적숨김[번호]=true */
const 업적보임 = 번호 => !(S.업적숨김||{})[String(번호)];
function 업적보임칸(a){ const 보=업적보임(a.번호);
  return `<button class="업적보임${보?" 켬":""}" data-act="업적보임" data-v="${a.번호}" role="checkbox" aria-checked="${보}" aria-label="프로필에 보이기"><span class="업적보임네모">${보?아이콘.체크:""}</span><span>프로필</span></button>`; }
function 업적줄(a){''')
바꿈('''    case "업적누름": {''',
     '''    case "업적보임": { const k=String(d.v); if(!S.업적?.[k]) break; S.업적숨김=S.업적숨김||{};   // 10-04 v18 E ③ 대표 칭호(업적누름)는 건드리지 않는다
      if(S.업적숨김[k]) delete S.업적숨김[k]; else S.업적숨김[k]=true; break; }
    case "업적누름": {''')
# 프로필 업적 줄 — 숨긴 것 빼고
바꿈('''function 최근업적줄(방=""){ const 목=업적목록();
  if(!목.length) return `<div class="인업적 빈 프묶음 작 옅음 번호"${번("프3")}${방}>아직 달성한 업적이 없습니다</div>`;''',
     '''function 최근업적줄(방=""){ const 전=업적목록(), 목=전.filter(a=>업적보임(a.번호));   // 10-04 v18 E ③ 숨긴 업적은 빼고
  if(!목.length) return `<div class="인업적 빈 프묶음 작 옅음 번호"${번("프3")}${방}>${전.length?"프로필에 보일 업적이 없습니다":"아직 달성한 업적이 없습니다"}</div>`;''')
# 끌어 옮기기 — 보이는 것끼리 옮기고, 숨긴 업적은 제자리
바꿈('''  else if(x.종류==="업적"){ const 목=업적목록().map(a=>String(a.번호)); if(옮김(목,x.원,x.대상)){ S.업적순서=목; S.설정.업적정렬="직접"; 발자취("업적 순서 바꿈"); } }''',
     '''  else if(x.종류==="업적"){ const 전=업적목록().map(a=>String(a.번호)), 목=전.filter(업적보임);   // 10-04 v18 E ③ 보이는 줄의 차례 = data-i
    if(옮김(목,x.원,x.대상)){ let j=0; S.업적순서=전.map(k=>업적보임(k)?목[j++]:k); S.설정.업적정렬="직접"; 발자취("업적 순서 바꿈"); } }''')

# ── ⑤ SNS 링크 — 칸 1개(있으면 있는 만큼) + [＋ 칸 추가]. 빈 칸은 저장 때 버린다 ──
바꿈('''/* 10-04 v17 ⑤ SNS 링크 — 최대 3개.''', '''/* 10-04 v17 ⑤ SNS 링크 — (v18 E: 몇 개든).''')
바꿈('''  else if(종==="링크"){ const 링=링크목록();   // 10-04 v17 ⑤ SNS 주소 칸 3개
    안 = 머리("SNS 링크") + [0,1,2].map(i=>`<input class="입력" data-in="링크" type="url" inputmode="url" autocomplete="off" placeholder="https://" aria-label="SNS 주소 ${i+1}" value="${esc(링[i]||"")}">`).join("") + `<button class="버튼 주 넓" data-act="링크저장">저장</button>`; }''',
     '''  else if(종==="링크"){   // 10-04 v18 E ⑤ 기본 칸 1개 · ＋ 로 몇 개든. 친 글은 U.링크칸 에 (칸을 더해 다시 그려도 그대로)
    if(U.링크칸?.시트!==U.시트){ const 링=링크목록(); U.링크칸={시트:U.시트, 값:링.length?링.slice():[""]}; }
    안 = 머리("SNS 링크") + U.링크칸.값.map((v,i)=>`<input class="입력" data-in="링크" data-i="${i}" type="url" inputmode="url" autocomplete="off" placeholder="https://" aria-label="SNS 주소 ${i+1}" value="${esc(v)}">`).join("")
      + `<button class="버튼 넓" data-act="링크칸더">＋ 칸 추가</button><button class="버튼 주 넓" data-act="링크저장">저장</button>`; }''')
바꿈('''    case "링크저장": S.설정.링크=[...document.querySelectorAll('#폰 [data-in="링크"]')].map(el=>링크주소(el.value)).filter(Boolean).slice(0,3); U.시트=null; break;   // 10-04 v17 ⑤''',
     '''    case "링크저장": S.설정.링크=[...document.querySelectorAll('#폰 [data-in="링크"]')].map(el=>링크주소(el.value)).filter(Boolean); U.링크칸=null; U.시트=null; break;   // 10-04 v17 ⑤ · v18 E 몇 개든 (빈 칸 · http(s) 아닌 꼴은 버림)
    case "링크칸더": if(U.링크칸){ U.링크칸.값.push(""); setTimeout(()=>[...document.querySelectorAll('#폰 [data-in="링크"]')].pop()?.focus(),0); } break;   // 10-04 v18 E ⑤''')

# ── ⑥ 돋보기 화면 ──
바꿈('''function 검색탭(){
  return `<div class="넘김 찾화면"><div class="쌓기">
    <input class="입력" type="search" placeholder="유저 검색" aria-label="유저 검색" autocomplete="off">
    <div class="작 옅음">다른 사람 사진 · 검색은 준비 중</div>
    <div class="찾판">${Array.from({length:9},()=>`<div class="찾칸"></div>`).join("")}</div>
  </div></div>`; }''',
     '''/* 10-04 v18 E ⑥ 맨 위 줄 = [북마크 40] [돋보기 아이콘 + 흐린 '검색' 칸]. 사진 최대 12장 = 3열 × 4줄 · 3:4 · 스크롤 없이
   (낮은 폰에서는 칸 비율 그대로 판 전체를 줄인다 — CSS .찾판틀 의 cqh). 북마크 · 검색은 아직 자리만 */
const 찾최대 = 12;
function 검색탭(){
  return `<div class="넘김 찾화면"><div class="쌓기">
    <div class="찾줄"><button class="찾북마크" aria-label="북마크">${아이콘.북마크}</button><label class="찾칸입력">${아이콘.돋보기}<input class="찾입력" type="search" placeholder="검색" aria-label="검색" autocomplete="off"></label></div>
    <div class="찾판틀"><div class="찾판">${Array.from({length:찾최대},()=>`<div class="찾칸"></div>`).join("")}</div></div>
  </div></div>`; }''')

css = '''
/* ═══ 10-04 v18 E — 업적 '프로필' 체크 · 닉네임 줄 · 돋보기 화면 · 당겨서 새로고침 ═══ */
/* ③ 업적 화면 — 달성한 줄 오른쪽 체크 칸 (폭 44 · 네모 18 · 글 11) */
.업적줄묶음{display:flex;align-items:stretch;border-bottom:1px solid var(--선)}
.업적줄묶음>.업적줄{border-bottom:0;flex:1;min-width:0}
.업적보임{flex:none;width:44px;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:4px;background:none;border:0;padding:0;font-family:inherit;font-size:11px;color:var(--옅음);cursor:pointer}
.업적보임.켬{color:var(--흐림)}
.업적보임네모{width:18px;height:18px;border-radius:4px;border:2px solid var(--속선);box-sizing:border-box;display:flex;align-items:center;justify-content:center}
.업적보임.켬 .업적보임네모{background:var(--강조);border-color:var(--강조);color:var(--강조글)}
.업적보임네모 svg{width:12px;height:12px}
/* ④ 닉네임 줄 — 왼쪽 = 파란 상자 왼쪽 (--닉왼 은 그린 뒤 JS 가 잰다 · 상자 너비가 바뀌어도 따라감). 아래로 7 = 줄 28 × 25% */
.인닉줄{padding-left:var(--닉왼,0px);position:relative;top:7px}
/* ⑥ 돋보기 — 스크롤 없이. 판 너비 = min(칸 너비, (남은 높이 − 줄 사이 2×3) × 9/16 + 칸 사이 2×2) → 3:4 칸 4줄이 남은 높이에 들어간다 */
.넘김.찾화면{display:flex;flex-direction:column}
.찾화면>.쌓기{flex:1;min-height:0}
.찾줄{display:flex;align-items:center;gap:8px;flex:none}
.찾북마크{flex:none;width:40px;height:40px;display:flex;align-items:center;justify-content:center;background:none;border:0;padding:0;color:var(--흐림);cursor:pointer}
.찾북마크 svg{width:18px;height:18px}
.찾칸입력{flex:1;min-width:0;height:40px;display:flex;align-items:center;gap:8px;padding:0 12px;border:1px solid var(--속선);border-radius:var(--r-작게);background:var(--면);color:var(--옅음);cursor:text}
.찾칸입력 svg{flex:none;width:18px;height:18px}
.찾입력{flex:1;min-width:0;height:100%;border:0;background:none;padding:0;font-family:inherit;font-size:15px;color:var(--글)}
.찾입력:focus{outline:none}
.찾칸입력:focus-within{border-color:var(--강조)}
.찾입력::placeholder{color:var(--옅음)}
.찾판틀{flex:1;min-height:0;container-type:size}
.찾판{width:min(100%, calc((100cqh - 6px) * 9 / 16 + 4px));margin:0 auto}
/* ⑦ 당겨서 새로고침 — 끄는 동안 스크롤 영역 속이 --당김 만큼 내려오고, 위에 빈 줄(.당김표)에 로딩 아이콘 */
.당기는중>*{transform:translateY(var(--당김,0px))!important}
.당김돌아감>*{transition:transform .2s ease-out!important}
.당김표{position:absolute;z-index:15;height:0;overflow:hidden;display:flex;align-items:center;justify-content:center;color:var(--강조);pointer-events:none}
.당김표.당김돌아감{transition:height .2s ease-out}
.당김아이콘{display:flex;width:18px;height:18px}
.당김아이콘 svg{width:18px;height:18px}
.당김표.도는중 .당김아이콘{animation:당김돎 .5s linear infinite}
@keyframes 당김돎{to{transform:rotate(360deg)}}
.폰.당김선택막음{-webkit-user-select:none;user-select:none}
@media (prefers-reduced-motion: reduce){.당김표.도는중 .당김아이콘{animation:none}.당김돌아감>*,.당김표.당김돌아감{transition:none!important}}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]

js = r'''
<script>
/* ═══ 10-04 v18 E — 업적 줄 밀기 · 닉네임 줄 맞춤 · SNS 칸 · 당겨서 새로고침 ═══ */
/* ⑦ 나중에 화면별로 끌 수 있게 — 여기에 화면키() 값을 넣으면 그 화면은 당겨도 새로고침하지 않는다 (예: "운동", "캘린더", "검색") */
const 새로고침끔 = [];
(()=>{ const 폰=document.getElementById("폰");

  /* ── ④ 닉네임 줄 왼쪽 = 파란 상자(.큰합) 왼쪽. 그린 뒤 · 창 크기가 바뀔 때 잰다.
     offsetLeft 를 더해 재므로 들어오는 움직임(transform) 중에도 값이 같다 ── */
  const 왼 = el => { let x=0; for(let e=el; e; e=e.offsetParent) x+=e.offsetLeft; return x; };
  function 닉맞춤(){ const 줄=폰.querySelector(".인닉줄"), 합=폰.querySelector(".인오른 .큰합"); if(!줄) return;
    const v = 합 ? Math.max(0, 왼(합)-왼(줄)) : 0, 글=v.toFixed(2)+"px";
    if(줄.style.getPropertyValue("--닉왼")!==글) 줄.style.setProperty("--닉왼", 글); }
  new MutationObserver(닉맞춤).observe(폰,{childList:true}); addEventListener("resize", 닉맞춤); 닉맞춤();

  /* ── ⑤ SNS 칸 — 치는 대로 U.링크칸 에 (＋ 로 다시 그려도 그대로) ── */
  document.addEventListener("input", e=>{ const el=e.target; if(el?.dataset?.in!=="링크"||!U.링크칸) return; U.링크칸.값[+el.dataset.i]=el.value; });

  /* ── ② 업적 줄 가로 밀기
     크로미움(폰 흉내 · 터치)에서는 브라우저 스크롤로 원래 밀린다. 그래도 폰에서 안 밀리는 때(앱 안 웹뷰가 가로 밀기를 가져가는 등)를 위해:
     가로로 24 넘게 밀었는데 줄이 그대로면 그때부터 손가락을 따라 scrollLeft 를 직접 옮긴다. 마우스는 눌러 끌면 밀린다(운띠와 같은 방식).
     꾹 눌러 끌기(순서 바꾸기)가 켜지면 그쪽이 먼저 ── */
  let 업밀=null;
  document.addEventListener("touchstart", e=>{ const l=e.target.closest?.("#폰 .인업적"); 업밀=null;
    if(!l||e.touches.length!==1||l.scrollWidth<=l.clientWidth) return; const t=e.touches[0];
    업밀={l, x:t.clientX, y:t.clientY, 옆:l.scrollLeft, 직접:false}; }, {passive:true});
  document.addEventListener("touchmove", e=>{ if(!업밀) return; if(끌?.켜짐){ 업밀=null; return; }
    const t=e.touches[0], dx=t.clientX-업밀.x, dy=t.clientY-업밀.y, l=업밀.l;
    if(!업밀.직접){ if(Math.abs(dx)<24||Math.abs(dx)<Math.abs(dy)) return;
      if(Math.abs(l.scrollLeft-업밀.옆)>1){ 업밀=null; return; }   // 브라우저가 밀고 있다 → 맡긴다
      업밀.직접=true; 업밀.x=t.clientX; 업밀.옆=l.scrollLeft; window.업적직접밀기=(window.업적직접밀기||0)+1; }
    if(e.cancelable) e.preventDefault(); l.scrollLeft=업밀.옆-(t.clientX-업밀.x); }, {passive:false});
  document.addEventListener("touchend", ()=>{ 업밀=null; }); document.addEventListener("touchcancel", ()=>{ 업밀=null; });
  let 업끌=null;
  document.addEventListener("pointerdown", e=>{ 업끌=null; if(e.pointerType==="touch"||e.button!==0) return;
    const l=e.target.closest?.("#폰 .인업적"); if(l&&l.scrollWidth>l.clientWidth) 업끌={l, x:e.clientX, 옆:l.scrollLeft, 켜짐:false}; });
  document.addEventListener("pointermove", e=>{ if(!업끌) return; if(끌?.켜짐){ 업끌=null; return; } const dx=e.clientX-업끌.x;
    if(!업끌.켜짐){ if(Math.abs(dx)<=8) return; 업끌.켜짐=true; }
    e.preventDefault(); 업끌.l.scrollLeft=업끌.옆-dx; });
  document.addEventListener("pointerup", ()=>{ if(업끌?.켜짐) 막음클릭=true; 업끌=null; });   // 끌고 난 뒤 생기는 클릭(업적 화면 열기)은 막는다
  document.addEventListener("pointercancel", ()=>{ 업끌=null; });

  /* ── ⑦ 당겨서 새로고침 (모든 화면 공용)
     폰 안 스크롤 영역(.넘김 · 운동 화면 세트 목록 .운세트들 · 보고서 .결과목록 · .보고목록)이 맨 위(scrollTop 0)일 때 아래로 끌면
     위에 빈 줄이 생기며 끈 만큼(× .5 · 최대 56) 내려온다. 40(줄 높이) 이상에서 놓으면 줄 40 에서 로딩 아이콘이 0.5초 돌고 → 그리기() → 줄이 접힌다(.2s).
     그보다 덜 끌고 놓으면 그냥 접힌다. 터치 · 마우스 끌기 · 휠 위로(맨 위에 막 닿은 관성은 빼고) 모두.
     먼저인 것: 꾹 눌러 끌기(켜진 뒤) · 종목 칸 줄(.운띠) · 업적 줄의 휠 · 입력 칸. 시트가 열려 있으면 하지 않는다. 움직임 줄임이면 움직임 없이 ── */
  const 영역=".넘김, .운세트들, .결과목록, .보고목록", 줄높이=40, 최대=56, 비율=.5;
  const 돌림그림=`<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true"><path d="M21 12a9 9 0 1 1-6.2-8.56"/></svg>`;
  let 당=null, 도는중=false;
  function 찾기(t, 마우스){ if(도는중||U.시트||!t?.closest||끌?.켜짐) return null;
    if(새로고침끔.includes(화면키())) return null;
    if(t.closest(".가림,.시트,.운띠,textarea,select,[contenteditable]")) return null;
    if(마우스 && t.closest("input")) return null;   // 마우스는 입력 칸에서 글 고르기가 먼저. 손가락은 입력 칸 위에서 시작해도 된다(폰은 누른 자리를 가까운 칸으로 붙인다)
    const el=t.closest(영역); if(!el||!폰.contains(el)) return null;
    for(let p=t; p&&p!==폰; p=p.parentElement) if(p.scrollTop>0.5) return null;   // 안쪽 · 바깥 스크롤 모두 맨 위여야
    return el; }
  function 자리(el){ let x=0, y=0; for(let e=el; e&&e!==폰; e=e.offsetParent){ x+=e.offsetLeft; y+=e.offsetTop; } return {x,y}; }
  function 표만들기(el){ const p=자리(el), 표=document.createElement("div"); 표.className="당김표"; 표.setAttribute("aria-hidden","true");
    표.style.cssText=`left:${p.x}px;top:${p.y}px;width:${el.clientWidth}px;height:0px`; 표.innerHTML=`<span class="당김아이콘">${돌림그림}</span>`; 폰.appendChild(표); return 표; }
  function 놓기(x){ x.el.classList.add("당기는중"); x.el.style.setProperty("--당김", x.d+"px"); x.표.style.height=x.d+"px";
    const 아=x.표.firstChild; 아.style.opacity=Math.min(1, x.d/줄높이).toFixed(2); 아.style.transform=`rotate(${Math.round(x.d*6)}deg)`; }
  function 치우기(el, 표){ el?.classList.remove("당기는중","당김돌아감"); el?.style.removeProperty("--당김"); 표?.remove(); }
  function 접기(el, 표){ if(움직임줄임()) return 치우기(el, 표);
    el.classList.add("당김돌아감"); 표.classList.add("당김돌아감"); void 표.offsetHeight;
    el.style.setProperty("--당김","0px"); 표.style.height="0px"; 표.firstChild.style.opacity="0";
    setTimeout(()=>치우기(el, 표), 220); }
  function 새로고침(el, 표){ 도는중=true; const 키=화면키(), 순=[...폰.querySelectorAll(영역)].indexOf(el), 줄임=움직임줄임();
    if(!줄임){ el.classList.add("당김돌아감"); 표.classList.add("당김돌아감"); }
    el.style.setProperty("--당김", 줄높이+"px"); 표.style.height=줄높이+"px"; 표.firstChild.style.opacity="1"; 표.firstChild.style.transform="";
    if(!줄임) 표.classList.add("도는중");
    setTimeout(()=>{ 도는중=false;
      그리기();   // 새로고침 — 지금은 다시 그리기만 (새로 받을 것이 없어도)
      window.당김새로고침=(window.당김새로고침||0)+1;
      if(줄임 || 화면키()!==키) return;   // 그사이 화면이 바뀌었으면 접는 움직임은 생략
      const 새=[...폰.querySelectorAll(영역)][순]; if(!새) return;
      const 새표=표만들기(새); 새.classList.add("당기는중"); 새.style.setProperty("--당김", 줄높이+"px"); 새표.style.height=줄높이+"px"; 새표.firstChild.style.opacity="1";
      접기(새, 새표); }, 500); }
  function 놓음(x){ if(!x?.켜짐) return; 폰.classList.remove("당김선택막음");
    if(x.d>=줄높이 && x.el.isConnected) 새로고침(x.el, x.표); else 접기(x.el, x.표); }
  /* 같은 판정 — 아래로 8 넘게, 세로가 더 크게 움직였을 때 켜진다. 위로 · 옆으로면 원래대로 */
  function 움직임(x, cx, cy){ const dx=cx-x.x, dy=cy-x.y;
    if(!x.켜짐){ if(끌?.켜짐) return false;
      if(dy<=8 || Math.abs(dx)>=dy){ if(Math.abs(dx)>8 || dy< -8) 당=null; return false; }
      if(끌&&!끌.켜짐){ clearTimeout(끌.타이머); 끌=null; }   // 꾹 누르기 기다리던 것은 그만
      x.켜짐=true; x.표=표만들기(x.el); }
    x.d=Math.min(최대, Math.max(0, dy-8)*비율 + 4); 놓기(x); return true; }
  // 터치
  document.addEventListener("touchstart", e=>{ if(e.touches.length!==1){ if(당?.켜짐) 놓음(당); 당=null; return; }
    if(당) return; const t=e.touches[0], el=찾기(e.target); 당 = el ? {el, x:t.clientX, y:t.clientY, 켜짐:false, d:0, 종류:"터치"} : null; }, {passive:true});
  document.addEventListener("touchmove", e=>{ if(!당||당.종류!=="터치") return; const t=e.touches[0];
    if(움직임(당, t.clientX, t.clientY) && e.cancelable) e.preventDefault(); }, {passive:false});
  document.addEventListener("touchend", ()=>{ if(당?.종류==="터치"){ 놓음(당); 당=null; } });
  document.addEventListener("touchcancel", ()=>{ if(당?.종류==="터치"){ if(당.켜짐) 접기(당.el, 당.표); 당=null; } });
  // 마우스 끌기
  document.addEventListener("pointerdown", e=>{ if(e.pointerType!=="mouse"||e.button!==0||당) return; const el=찾기(e.target, true); if(el) 당={el, x:e.clientX, y:e.clientY, 켜짐:false, d:0, 종류:"마우스"}; });
  document.addEventListener("pointermove", e=>{ if(!당||당.종류!=="마우스") return;
    if(움직임(당, e.clientX, e.clientY)){ e.preventDefault(); 폰.classList.add("당김선택막음"); getSelection?.()?.removeAllRanges(); } });
  document.addEventListener("pointerup", ()=>{ if(당?.종류!=="마우스") return; if(당.켜짐) 막음클릭=true; 놓음(당); 당=null; });   // 끌고 난 뒤 클릭은 막는다
  document.addEventListener("pointercancel", ()=>{ if(당?.종류==="마우스"){ if(당.켜짐) 접기(당.el, 당.표); 당=null; } });
  // 휠 위로 — 맨 위에서 굴린 만큼(× .3). 0.2초 멈추면 놓은 것으로
  let 앞휠={t:0, 움직임:false};
  document.addEventListener("wheel", e=>{ const now=performance.now();
    if(당&&당.종류!=="휠") return;
    const l=e.target.closest?.(".인업적"); if(l&&l.scrollWidth>l.clientWidth) return;   // 업적 줄은 휠이 가로 이동 (v17)
    if(Math.abs(e.deltaX)>Math.abs(e.deltaY)) return;
    const 양=e.deltaY*(e.deltaMode===1?16:e.deltaMode===2?400:1);
    if(당){ e.preventDefault(); 당.d=Math.min(최대, Math.max(0, 당.d-양*.3)); 놓기(당);
      clearTimeout(당.타이머); 당.타이머=setTimeout(()=>{ const x=당; 당=null; 놓음(x); }, 200); return; }
    if(양>=0){ 앞휠={t:now, 움직임:true}; return; }
    const el=찾기(e.target, true);
    if(!el){ 앞휠={t:now, 움직임:true}; return; }                                  // 아직 위로 넘기는 중
    if(앞휠.움직임 && now-앞휠.t<250){ 앞휠.t=now; return; }                          // 맨 위에 막 닿은 관성 — 당김 아님
    앞휠={t:now, 움직임:false}; e.preventDefault();
    당={el, 켜짐:true, d:0, 종류:"휠", 표:표만들기(el)}; 당.d=Math.min(최대, -양*.3); 놓기(당);
    당.타이머=setTimeout(()=>{ const x=당; 당=null; 놓음(x); }, 200); }, {passive:false});
})();
</script>
'''
끝 = s.rfind('</body>'); s = s[:끝] + js + s[끝:]
pathlib.Path(OUT).write_text(s, encoding='utf-8'); print("v18 E →", OUT, f"{len(s.encode()):,} 바이트")
