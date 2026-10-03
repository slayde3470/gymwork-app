"""운동 화면 — 10-03 홍겸 님 ✎ 표시 7개 반영.   python3 patch_v7_work.py 입력.html 출력.html

표시 → 고친 것
 0yyw 운동 "띠좀... 만들어라.. ^^"            → 맨 위 머리 줄(‹ · 종목 이름 · 타이머)을 캘린더 년월 띠와 같은 부품 .띠 로 (강조 바탕 · 강조글)
                                                종목 이름은 잘리지 않는다 — 말줄임 없이 낱말 단위로 감긴다
 ozlo 운동 "옮겨..."                          → '플랜 15회차' 를 띠 안, 종목 이름 바로 뒤 같은 줄로
 iiwd 운동 "싹 삭제"                          → 근육 칩 줄([가슴 가운데][가슴 윗부분][+3]) 삭제
 zx75 운동 "이 정보들은 이쪽 박스 들어가게"     → '1RM · 1주 · 최고' 와 '달성 · 볼륨' 두 줄을 아래 상자(루틴 진행 · 종목 칸 줄) 맨 위로
                                                늘 두 줄로 고정 — 한 줄/두 줄이 값에 따라 바뀌면 첫 체크 때 상자 높이가 변해 화면이 움직인다
 9a8l 운동 "맨 아래 세트 체크하면 맨 위로 이동" → 원인: 그리기()가 화면을 통째로 새로 만들어 세트 목록 scrollTop 이 0 으로 돌아갔다.
                                                스크롤 자리를 적어 두고 다시 그린 뒤 같은 종목이면 그 자리로 되돌린다
 h6tu 운동 "화면 절반 아래 체크하면 아래 줄이 올라오게" → 목록이 넘칠 때만 · 보이는 목록 칸 절반보다 아래 세트를 체크하면
                                                다음 세트가 목록 가운데쯤 오도록 부드럽게 스크롤 (끝은 넘지 않음 · 위 절반 · 체크 풀기 · 다 보일 때는 그대로)
 khmo 운동 "박스가 좌우로 이동이 안 돼 · 꾹 눌러 끌면 같이" → 종목 칸 줄을 마우스(펜)로 누른 채 좌우로 끌면 따라 움직인다.
                                                8px 넘게 움직였으면 끌기 — 손 뗄 때의 칸 고르기 클릭은 막는다. 터치는 브라우저 원래 스크롤
 i4gd 운동 "운동 넘어갔는데 이동을 안 해"       → 보는 종목이 바뀌면(다음 · 이전 · 칸 누르기) 지금 종목 칸이 줄 가운데쯤 오게 부드럽게 가로 스크롤.
                                                같은 종목이면 손으로 넘겨 둔 자리를 그대로 둔다
"""
import sys, pathlib
if len(sys.argv) != 3: raise SystemExit("쓰는 법: python3 patch_v7_work.py 입력.html 출력.html")
s = pathlib.Path(sys.argv[1]).read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:80]!r}: {c}번")
    s = s.replace(old, new)

# ══ 1. iiwd 근육 칩 줄 삭제 — 칩을 만들던 계산도 함께 뺀다 (쓰는 곳이 없어짐) ══
바꿈('''  const 단=지금단계(ss.종목,모의시각()), 근=종목근육(e.이름);
  const 잎최대=id=>Math.max(0,...잎(id).map(l=>단[l]||0));
  const 근줄=Object.entries(근).sort((a,b)=>"PSY".indexOf(a[1])-"PSY".indexOf(b[1]));
  const 칩=근줄.slice(0,2).map(([id,r])=>
    `<span class="${r==="P"?"주":""}"><i style="background:${단계색(잎최대(id))||"var(--속선)"}"></i>${esc((근육이름[id]||id).replace(/ \\(.*\\)/,""))}</span>`).join("")
    + (근줄.length>2?`<span>+${근줄.length-2}</span>`:"");
''', '''  /* 10-03 ✎ iiwd "싹 삭제" — 머리 줄의 근육 칩([가슴 가운데][가슴 윗부분][+3])을 뺐다. 근육은 아래 그림 두 장이 보여 준다.
     ✎ 0yyw "띠좀 만들어라" — 머리 줄을 캘린더 년월 띠와 같은 부품(.띠)으로. ‹ · 타이머 · 그림 단추도 강조글.
     ✎ ozlo "옮겨" — '플랜 n회차'는 띠 안, 이름 바로 뒤 같은 줄. 이름은 말줄임 없이 감긴다(지금 종목 이름은 잘리면 안 된다).
     ✎ zx75 "이 정보들은 이쪽 박스 들어가게" — 이 종목 지표 두 줄(1RM · 1주 · 최고 / 달성 · 볼륨)을 아래 상자 맨 위로.
       늘 두 줄로 둔다 — 값에 따라 한 줄 ↔ 두 줄이 바뀌면 첫 체크 때 상자 높이가 변해 화면이 움직인다 */
''')

# ══ 2. 0yyw · ozlo · zx75 — 머리 줄 = 띠 · '플랜 n회차' 이름 옆 · 지표 두 줄은 아래 상자로 ══
바꿈('''    <div class="운머리 번호"${번("운0")}><button class="버튼 낮 운나감" data-act="탭" data-t="캘린더" aria-label="나가기">‹</button>
      <div class="운글"><div class="운이름">${esc(e.이름)}</div>
        ${e.플랜id?`<div class="운곁">플랜 ${e.회}회차${e.측정일?" · 측정일":""}</div>`:""}
        <div class="운근육">${칩||`<span>근육 정보 없음</span>`}</div>
        <div class="운지표"><span>${맨?"최고":"1RM"} <b>${오==null?"—":kg(Math.round(오*2)/2)+단위}</b></span>${비("1주",지.주)}${비("최고",지.최고)}</div>
        <div class="운지표"><span>달성 <b>${e.세트.length?Math.round(완(e)/e.세트.length*100):0}%</b></span><span>볼륨 <b>${콤마(Math.round(볼(e)))}</b>/${콤마(Math.round(볼(e,true)))}kg</span></div></div>
      <div class="운오른"><span class="숫 굵 큰" data-timer="경과"></span>${그림보임?"":`<button class="버튼 낮" data-act="배너보기">그림</button>`}</div></div>''',
'''    <div class="운머리 띠 번호"${번("운0")}><button class="운나감" data-act="탭" data-t="캘린더" aria-label="나가기">‹</button>
      <div class="운글"><b class="운이름">${esc(e.이름)}</b>${e.플랜id?`<span class="운곁">플랜 ${e.회}회차${e.측정일?" · 측정일":""}</span>`:""}</div>
      <div class="운오른">${그림보임?"":`<button class="작은흰" data-act="배너보기">그림</button>`}<span class="숫 굵 큰" data-timer="경과"></span></div></div>''')

바꿈('''    <div class="운아래 번호"${번("운띠")}>
      <div class="운요약">''',
'''    <div class="운아래 번호"${번("운띠")}>
      <div class="운지표들"><div class="운지표"><span>${맨?"최고":"1RM"} <b>${오==null?"—":kg(Math.round(오*2)/2)+단위}</b></span>${비("1주",지.주)}${비("최고",지.최고)}</div>
        <div class="운지표"><span>달성 <b>${e.세트.length?Math.round(완(e)/e.세트.length*100):0}%</b></span><span>볼륨 <b>${콤마(Math.round(볼(e)))}</b>/${콤마(Math.round(볼(e,true)))}kg</span></div></div>
      <div class="운요약">''')

# ══ 3. 9a8l · h6tu · i4gd — 두 스크롤(세트 목록 · 종목 칸 줄)을 다시 그려도 잇는다 ══
바꿈('''function 운세트줄(i,s,k,지금k){''',
'''/* 10-03 ✎ 9a8l "맨 아래 세트를 체크하면 맨 위로 이동" — 원인: 그리기()는 화면을 통째로 새로 만들어
   세트 목록(.운세트들)의 scrollTop 과 종목 칸 줄(.운띠)의 scrollLeft 가 0 으로 돌아갔다.
   스크롤될 때마다 자리를 적어 두고(운자리), 새로 그린 뒤 운자리맞춤()이 같은 운동 · 같은 종목이면 그 자리로 되돌린다.
   ✎ h6tu — 목록이 넘칠 때 보이는 목록 칸의 절반보다 아래 세트를 체크하면, 다음 세트가 가운데쯤 오게 부드럽게 올린다.
   ✎ i4gd — 보는 종목이 바뀌면 지금 종목 칸이 줄 가운데쯤 오게 부드럽게 가로로 넘긴다.
   자리는 다시 그린 직후(움직임이 시작되기 전) 한 번만 재고, 한 번만 움직인다 */
const 운자리 = {세션:null, 본:null, 위:0, 옆:0, 다음:null};
document.addEventListener("scroll", e=>{ const t=e.target; if(!t||!t.classList) return;
  if(t.classList.contains("운세트들")) 운자리.위=t.scrollTop; else if(t.classList.contains("운띠")) 운자리.옆=t.scrollLeft; }, true);
function 체크자리(i,k){ 운자리.다음=null;
  const e=S.세션?.종목[i]; if(!e||!e.세트[k]||e.세트[k].완료) return;          // 체크 풀기는 움직이지 않는다
  const 목=document.querySelector("#폰 .운세트들"), 줄=목?.querySelector(`[data-act="체크"][data-i="${i}"][data-k="${k}"]`)?.closest(".세트줄");
  if(!목||!줄||목.scrollHeight<=목.clientHeight+1) return;                        // 세트가 다 보이면 절대 움직이지 않는다
  if(줄.offsetTop+줄.offsetHeight/2-목.scrollTop <= 목.clientHeight/2) return;   // 보이는 칸의 위 절반 → 그대로
  const 다=e.세트.findIndex((s,j)=>j>k&&!s.완료); if(다>=0) 운자리.다음={i,k:다}; }
function 운자리맞춤(폰){ const ss=S.세션, 목=폰.querySelector(".운세트들"), 줄=폰.querySelector(".운띠"); if(!ss||!목||!줄) return;
  const 같은운동=운자리.세션===ss.시작, 같은종목=같은운동&&운자리.본===U.본, 다음=운자리.다음; 운자리.다음=null;
  const 움=움직임줄임()?"auto":"smooth";
  목.scrollTop = 같은종목 ? 운자리.위 : 0;
  if(같은운동) 줄.scrollLeft = 운자리.옆;
  운자리.세션=ss.시작; 운자리.본=U.본; 운자리.위=목.scrollTop; 운자리.옆=줄.scrollLeft;
  if(같은종목 && 다음 && 다음.i===U.본){ const r=목.querySelectorAll(".세트줄")[다음.k];
    if(r){ const 끝=목.scrollHeight-목.clientHeight, 목표=Math.max(0,Math.min(끝, r.offsetTop+r.offsetHeight/2-목.clientHeight/2));
      if(목표>목.scrollTop+1) 목.scrollTo({top:목표, behavior:움}); } }              // 아래 줄을 올리기만 한다
  const 칸=줄.querySelector(".운칸.지금");
  if(칸 && !같은종목){ const 끝=줄.scrollWidth-줄.clientWidth, 목표=Math.max(0,Math.min(끝, 칸.offsetLeft+칸.offsetWidth/2-줄.clientWidth/2));
    if(Math.abs(목표-줄.scrollLeft)>1) 줄.scrollTo({left:목표, behavior:같은운동?움:"auto"}); } }
/* 10-03 ✎ khmo "꾹 눌러서 왼쪽 오른쪽으로 이동하면 같이" — 종목 칸 줄을 마우스 · 펜으로 누른 채 좌우로 끌면 줄이 따라 움직인다.
   스크롤바는 그대로 숨김. 8px 넘게 움직였으면 끌기로 보고, 손을 뗄 때 생기는 클릭(칸 고르기)은 막음클릭으로 막는다.
   터치는 브라우저가 원래대로 넘긴다 */
let 띠끌=null;
document.addEventListener("pointerdown", e=>{ if(e.pointerType==="touch"||e.button!==0) return;
  const 줄=e.target.closest?.("#폰 .운띠"); if(!줄) return; 띠끌={줄, x:e.clientX, 옆:줄.scrollLeft, 켜짐:false}; });
document.addEventListener("pointermove", e=>{ if(!띠끌) return; const dx=e.clientX-띠끌.x;
  if(!띠끌.켜짐){ if(Math.abs(dx)<=8) return; 띠끌.켜짐=true; 띠끌.줄.classList.add("끄는중"); }
  e.preventDefault(); 띠끌.줄.scrollLeft=띠끌.옆-dx; });
const 띠끌끝=()=>{ if(!띠끌) return; if(띠끌.켜짐){ 막음클릭=true; 띠끌.줄.classList.remove("끄는중"); } 띠끌=null; };
document.addEventListener("pointerup", 띠끌끝); document.addEventListener("pointercancel", 띠끌끝);
function 운세트줄(i,s,k,지금k){''')

# 체크하기 전에(지금 화면 그대로일 때) 자리를 잰다
바꿈('''    case "체크": 체크(+d.i,+d.k); break;''',
     '''    case "체크": 체크자리(+d.i,+d.k); 체크(+d.i,+d.k); break;   // 10-03 ✎ h6tu — 체크 전 자리를 재 두고, 다시 그린 뒤 운자리맞춤()이 움직인다''')

# 다시 그린 직후 — 운동 화면의 두 스크롤 자리를 되돌린다 (.넘김 자리 맞추기 바로 앞)
바꿈('''  if(새){ if(k==="운동" && U.스크롤!=null){''',
     '''  운자리맞춤(폰);   // 10-03 ✎ 9a8l · h6tu · i4gd
  if(새){ if(k==="운동" && U.스크롤!=null){''')

# ══ 4. CSS ══
css = '''
/* ═══ 10-03 운동 화면 ✎ 표시 7개 ═══ */
/* 0yyw · ozlo — 머리 줄 = 띠 (.띠 와 같은 값: 강조 바탕 · 강조글 · 높이 40 · 여백 6/12 · 이름 18 굵게).
   이름은 말줄임 없이 낱말 단위로 감기고, '플랜 n회차'는 이름 바로 뒤에 붙어 함께 흐른다 */
.운머리.띠{background:var(--강조);color:var(--강조글);padding:6px 12px;align-items:center;border-bottom:0}
.운머리 .운나감{flex:none;height:32px;min-width:28px;font-size:18px;font-weight:700;color:var(--강조글)}
.운머리 .운글{line-height:1.3;word-break:keep-all}
.운머리 .운이름{display:inline;font-size:18px}
.운머리 .운곁{display:inline-block;margin:0 0 0 8px;font-size:13px;color:var(--강조글);white-space:nowrap}
.운머리 .운오른{flex-direction:row;align-items:center;gap:8px}
/* zx75 — 이 종목 지표 두 줄을 아래 상자 맨 위로. 루틴 진행과는 선 하나로 나눈다 */
.운지표들{display:flex;flex-direction:column;gap:4px;padding-bottom:8px;margin-bottom:8px;border-bottom:1px solid var(--선)}
.운지표들 .운지표{margin-top:0;min-width:0}
/* 9a8l · h6tu · i4gd — 세트 줄 · 종목 칸의 offsetTop/Left 를 스크롤 칸 기준으로 재려고 기준 칸으로 둔다 */
.운세트들,.운띠{position:relative}
/* khmo — 마우스로 끄는 동안 글자가 잡히지 않게 · 누른 칸이 작아진 채 남지 않게 */
.운띠{-webkit-user-select:none;user-select:none}
.운띠.끄는중{cursor:grabbing}
.운띠.끄는중 .운칸{transform:none}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]

pathlib.Path(sys.argv[2]).write_text(s, encoding='utf-8')
print("→", sys.argv[2], f"{len(s.encode()):,} 바이트")
