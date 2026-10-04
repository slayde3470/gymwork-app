"""v19 A (10-04 홍겸 님) — 루틴 · 플랜 · 설정 맨 위 띠 · 당겨서 새로고침은 띠 아래에서만 · 검색 탭 위 줄 고정
python3 patch_v19_A.py IN OUT

① "루틴화면에도 맨위 '루틴' 줄에 띠가 생겨야돼. '플랜' 탭의 '운동플랜'도 마찬가지. 글씨는 가운데로.
    설정탭은 띠 색을 중심색 말고 '회색'에 검정글씨로 처리하자."
   → 루틴 목록 · 운동 플랜 · 설정 의 h1 제목을 종목 탭과 같은 띠(.띠 · 40 · 18 Bold)로, 스크롤 영역(.넘김) 바깥 맨 위에. 글 가운데.
     루틴 하나를 연 화면은 원래 머리 띠(루틴상세띠)가 있어 그대로.
     설정 띠만 바탕 --선(회색) · 글 --글(검정. 다크에서는 밝은 글).
② "새로고침 애니메이션은 맨 위의 띠 아래에 나타나야해. 새로운 줄과 새로고침 애니메이션은 띠까지 움직이게 하지 않는다.
    검색탭의 경우에는 북마크, 검색엔진박스 줄은 고정하고 그 아래에 새로운 줄을 만들고 애니메이션을 나타내야해."
   → 당김 영역 맨 앞에 붙은 띠(.띠 — 보고서 띠처럼 스크롤 영역 안에 든 것)는 움직이지 않게(.당김고정) 하고,
     빈 줄(.당김표)은 그 띠 바로 아래에서 생긴다. 띠 위에서 시작한 끌기는 당김으로 치지 않는다.
     검색 탭은 [북마크][검색] 줄을 .넘김 바깥(.찾위)으로 꺼내 고정 → 빈 줄은 사진 격자 위에서 생긴다.
새 값: 없음 (띠 = 기존 .띠 · 회색 = 기존 --선 · 띠 아래 틈 12 = 종목 탭 .종목넘김 과 같은 값 · 검색 줄 위 12 / 아래 8 = v18 그대로)
"""
import sys, pathlib
IN, OUT = sys.argv[1], sys.argv[2]
s = pathlib.Path(IN).read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:70]!r}: {c}번")
    s = s.replace(old, new)

# ── ① 맨 위 제목 → 띠 (스크롤 영역 바깥) ──
바꿈('''  return `<div class="넘김"><h1>루틴</h1><div class="쌓기">''',
     '''  return `<div class="띠 가운데띠 루틴목록띠"><b class="채움">루틴</b></div><div class="넘김 띠아래"><div class="쌓기">''')   # 10-04 v19 A ① 띠 · 글 가운데
바꿈('''  return `<div class="넘김"><h1>운동 플랜</h1><div class="쌓기">''',
     '''  return `<div class="띠 가운데띠 플랜띠"><b class="채움">운동 플랜</b></div><div class="넘김 띠아래"><div class="쌓기">''')   # 10-04 v19 A ①
바꿈('''  return `<div class="넘김"><h1>설정</h1>''',
     '''  return `<div class="띠 가운데띠 설정띠"><b class="채움">설정</b></div><div class="넘김">''')   # 10-04 v19 A ① 회색 띠 · 첫 '신체 정보' 이름표의 위 여백 14 가 띠 아래 틈

# ── ② 검색 탭 — [북마크][검색] 줄을 스크롤 영역 바깥으로 (고정) ──
바꿈('''  return `<div class="넘김 찾화면"><div class="쌓기">
    <div class="찾줄"><button class="찾북마크" aria-label="북마크">${아이콘.북마크}</button><label class="찾칸입력">${아이콘.돋보기}<input class="찾입력" type="search" placeholder="검색" aria-label="검색" autocomplete="off"></label></div>
    <div class="찾판틀">''',
     '''  return `<div class="찾위"><div class="찾줄"><button class="찾북마크" aria-label="북마크">${아이콘.북마크}</button><label class="찾칸입력">${아이콘.돋보기}<input class="찾입력" type="search" placeholder="검색" aria-label="검색" autocomplete="off"></label></div></div>
    <div class="넘김 찾화면"><div class="쌓기">
    <div class="찾판틀">''')   # 10-04 v19 A ② 북마크 · 검색 줄은 고정. 당김 빈 줄은 그 아래(사진 격자 위)에서

# ── ② 당겨서 새로고침 — 띠는 움직이지 않는다 ──
바꿈('''    if(t.closest(".가림,.시트,.운띠,textarea,select,[contenteditable]")) return null;''',
     '''    if(t.closest(".가림,.시트,.운띠,textarea,select,[contenteditable]")) return null;
    if(t.closest(".띠")) return null;   // 10-04 v19 A ② 띠 위에서 시작한 끌기는 당김 아님 (띠는 움직이지 않는다)''')
바꿈('''  function 표만들기(el){ const p=자리(el), 표=document.createElement("div"); 표.className="당김표"; 표.setAttribute("aria-hidden","true");
    표.style.cssText=`left:${p.x}px;top:${p.y}px;width:${el.clientWidth}px;height:0px`;''',
     '''  /* 10-04 v19 A ② 영역 맨 앞에 붙은 띠(보고서 띠처럼 스크롤 영역 안에 든 것) — 움직이지 않게 표시하고, 빈 줄은 그 띠 바로 아래에서 */
  function 머리띠(el){ let 끝=null; for(const c of el.children){ if(c.classList.contains("띠")) 끝=c; else break; } return 끝; }
  function 표만들기(el){ const p=자리(el), 표=document.createElement("div"); 표.className="당김표"; 표.setAttribute("aria-hidden","true");
    let 띠아래=0; for(const c of el.children){ if(!c.classList.contains("띠")) break; c.classList.add("당김고정"); }
    const 띠=머리띠(el); if(띠) 띠아래=Math.max(0, 띠.getBoundingClientRect().bottom-el.getBoundingClientRect().top);
    표.style.cssText=`left:${p.x}px;top:${p.y+띠아래}px;width:${el.clientWidth}px;height:0px`;''')

css = '''
/* ═══ 10-04 v19 A — 루틴 · 플랜 · 설정 맨 위 띠 · 당김은 띠 아래 · 검색 줄 고정 ═══ */
/* ① 띠 — .띠 그대로(강조 바탕 · 강조글 · 40 · 6/12 · 18 Bold), 글 가운데. 종목 탭 띠와 같은 값 */
.가운데띠 b.채움{text-align:center}
.넘김.띠아래{padding-top:12px}   /* 띠 아래 틈 12 — 종목 탭(.종목넘김)과 같은 값 */
/* ① 설정 띠만 회색 바탕(--선) · 검정 글(--글). 다크에서는 --선 #253A50 · --글 #E5F0FA */
.띠.설정띠{background:var(--선);color:var(--글)}
/* ② 검색 — [북마크][검색] 줄은 스크롤 영역 바깥에 고정. 위 12 · 아래 8 은 v18 그대로(.쌓기 위 여백 12 · 줄 사이 8) */
.찾위{flex:none;padding:12px 12px 8px}
.넘김.찾화면>.쌓기{padding-top:0}
/* ② 당김 중에도 영역 맨 앞 띠는 제자리 */
.당기는중>.당김고정{transform:none!important}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]
pathlib.Path(OUT).write_text(s, encoding='utf-8'); print("v19 A →", OUT, f"{len(s.encode()):,} 바이트")
