"""v17 B (10-04 홍겸 님) — 운동 보고서 · 큰운동판 · 톱니
1 보고서 들어올 때: 띠 = 위→아래 페이드인, 프로필 · 루틴 · 종목 칸 = 아래→위 페이드인, 시간 2배
2 날짜 = 제목 오른쪽 15 · 아래 15 / 띠 양 끝 ‹ 업적 · › 스탯 (보고서 위에서 열고, 닫으면 보고서로)
3 띠 높이 −15%   4 저장 뒤 '기록은 저장되었습니다' 지움
5 끝 단추 한 줄: ‹ 운동으로 돌아가기 25 / 운동 기록 저장하고 종료 50 / 기록하지 않고 종료 › 25
6 파란 상자 너비 85%   7 파란 상자 · 칸을 누르면 '○○ 상세' 시트(빈 틀)
8 아이콘.톱니 = 톱니 8개 + 가운데 구멍, 톱니단추 아이콘 18 → 15.3   9 N대 합계 = 반올림 정수
"""
import sys, pathlib
IN, OUT = sys.argv[1], sys.argv[2]
s = pathlib.Path(IN).read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    k = s.count(old)
    if k != n: raise SystemExit(f"{k}번 (기대 {n}): {old[:80]}")
    s = s.replace(old, new)

# ── 8 톱니바퀴 — 톱니 8개(바깥 10.5 · 안 7.6) + 가운데 구멍 r3. 설정 탭 아이콘도 같은 것을 쓴다 ──
바꿈('''톱니:a('<circle cx="12" cy="12" r="3"/><path d="M12 2v3M12 19v3M2 12h3M19 12h3M4.9 4.9 7 7M17 17l2.1 2.1M4.9 19.1 7 17M17 7l2.1-2.1"/>')''',
     '''톱니:a('<path d="M10.23 4.61L10.45 1.62A10.5 10.5 0 0 1 13.55 1.62L13.77 4.61A7.6 7.6 0 0 1 15.97 5.52L18.25 3.56A10.5 10.5 0 0 1 20.44 5.75L18.48 8.03A7.6 7.6 0 0 1 19.39 10.23L22.38 10.45A10.5 10.5 0 0 1 22.38 13.55L19.39 13.77A7.6 7.6 0 0 1 18.48 15.97L20.44 18.25A10.5 10.5 0 0 1 18.25 20.44L15.97 18.48A7.6 7.6 0 0 1 13.77 19.39L13.55 22.38A10.5 10.5 0 0 1 10.45 22.38L10.23 19.39A7.6 7.6 0 0 1 8.03 18.48L5.75 20.44A10.5 10.5 0 0 1 3.56 18.25L5.52 15.97A7.6 7.6 0 0 1 4.61 13.77L1.62 13.55A10.5 10.5 0 0 1 1.62 10.45L4.61 10.23A7.6 7.6 0 0 1 5.52 8.03L3.56 5.75A10.5 10.5 0 0 1 5.75 3.56L8.03 5.52A7.6 7.6 0 0 1 10.23 4.61Z"/><circle cx="12" cy="12" r="3"/>')''')

# ── 7 · 9 큰운동값: 칸마다 최고 기록 날짜(때)도 ──
바꿈('''  const 앞목록=Object.entries(S.기록).filter(([k])=>저장키값?키순(k,저장키값)<0:k.split("~")[0]<=날).map(([,r])=>r), 끝목록=rec?앞목록.concat([rec]):앞목록;
  const 최고=(목록,이름들)=>{ let v=0; for(const r of 목록) for(const e of r.종목) if(이름들.includes(e.이름)) for(const x of 찬(e)) if(x.w>0) v=Math.max(v,일RM(x.w,x.r)); return Math.round(v*10)/10; };
  const 추=보고설정().추가, 칸=큰운동표.filter(([키],j)=>j<3||추.includes(키)).map(([,글,이름들])=>({글, v:최고(끝목록,이름들), 앞:최고(앞목록,이름들)}));
  const 합=f=>Math.round(칸.reduce((a,x)=>a+x[f],0)*10)/10;
  return [{글:`${칸.length}대`, v:합("v"), 앞:합("앞")}, ...칸]; }''',
'''  const 앞목록=Object.entries(S.기록).filter(([k])=>저장키값?키순(k,저장키값)<0:k.split("~")[0]<=날).map(([k,r])=>[k.split("~")[0],r]), 끝목록=rec?앞목록.concat([[날,rec]]):앞목록;
  /* v17 — 값과 함께 그 값을 처음 낸 날(때)도 (상세 시트의 '최고 기록 날짜') */
  const 최고=(목록,이름들)=>{ let v=0, 때=null; for(const [d,r] of 목록) for(const e of r.종목) if(이름들.includes(e.이름)) for(const x of 찬(e)) if(x.w>0){ const m=일RM(x.w,x.r); if(m>v||(m===v&&때&&d<때)){ v=m; 때=d; } } return {v:Math.round(v*10)/10, 때}; };
  const 추=보고설정().추가, 칸=큰운동표.filter(([키],j)=>j<3||추.includes(키)).map(([,글,이름들])=>{ const 끝=최고(끝목록,이름들); return {글, v:끝.v, 앞:최고(앞목록,이름들).v, 때:끝.때}; });
  const 합=f=>Math.round(칸.reduce((a,x)=>a+x[f],0)*10)/10, 때들=칸.map(x=>x.때).filter(Boolean).sort();
  return [{글:`${칸.length}대`, v:합("v"), 앞:합("앞"), 때:때들.length?때들[때들.length-1]:null}, ...칸]; }''')

# ── 6 · 7 · 9 큰운동판: 파란 상자 · 칸 = 누르는 곳(상세 시트) · 합계는 반올림 정수 ──
바꿈('''  return `<div class="결과수 큰수 열${열}${톱니?" 톱니비킴":""}" style="--열:${열}"><p class="큰합"><b>${esc(합.글)} ${합.v>0?차kg(합.v)+`<small class="큰합단위">kg</small>`:"—"}</b>${차보임&&합.v>0&&합.앞>0&&Math.abs(합.v-합.앞)>=0.05?`<small>${합.v>합.앞?"▲":"▼"}${차kg(Math.abs(합.v-합.앞))}</small>`:""}</p>${들.map(x=>`<div><div class="큰값"><b>${x.v>0?차kg(x.v):"—"}</b>${차보임&&x.v>0&&x.앞>0?보고차(x.v-x.앞,""):""}</div><span>${esc(x.글)}</span></div>`).join("")}</div>`; }''',
'''  /* v17 홍겸 님 "3대~5대 수치에 소수점은 필요없어" — 합계와 그 ▲▼ 만 반올림 정수 (칸 숫자는 그대로) */
  const 합수=Math.round(합.v), 합차=Math.round(합.v)-Math.round(합.앞);
  /* v17 누르면 '○○ 상세' 시트 — 파란 상자 · 칸 모두 (div 그대로 두고 role=button: 칸 CSS 가 '>div' 로 잡혀 있다) */
  const 누름=글=>` data-act="시트" data-t="큰운동상세" data-v="${esc(글)}" role="button" tabindex="0" aria-label="${esc(글)} 상세"`;
  return `<div class="결과수 큰수 열${열}${톱니?" 톱니비킴":""}" style="--열:${열}"><p class="큰합"${누름(합.글)}><b>${esc(합.글)} ${합.v>0?합수+`<small class="큰합단위">kg</small>`:"—"}</b>${차보임&&합.v>0&&합.앞>0&&합차!==0?`<small>${합차>0?"▲":"▼"}${Math.abs(합차)}</small>`:""}</p>${들.map(x=>`<div${누름(x.글)}><div class="큰값"><b>${x.v>0?차kg(x.v):"—"}</b>${차보임&&x.v>0&&x.앞>0?보고차(x.v-x.앞,""):""}</div><span>${esc(x.글)}</span></div>`).join("")}</div>`; }
/* v17 '○○ 상세' 시트 — 지금 보고 있는 판과 같은 값(끝 보고서 · 저장된 보고서 · 프로필). 형태만 — 내용은 준비 중 */
function 큰운동상세시트(글){ const ss=S.세션;
  const 값들 = ss&&ss.끝화면 ? 큰운동값(세션기록(ss), null) : (!ss&&S.결과&&S.기록[S.결과.key]) ? 큰운동값(S.기록[S.결과.key], S.결과.key) : 큰운동값(null, null);
  const x=값들.find(y=>y.글===글)||{v:0, 때:null}, 합=x===값들[0];
  const 값글 = x.v>0 ? `${합?Math.round(x.v):차kg(x.v)}kg` : "—", 날글2 = x.때 ? x.때.split("-").join(".")+"." : "—";
  return `<div class="머리"><b>${esc(글)} 상세</b><button class="닫기" data-act="시트닫기">닫기</button></div>
    <div>${설정줄("지금 값","",`<b class="숫">${값글}</b>`)}<div class="구분"></div>${설정줄("최고 기록 날짜","",`<span class="숫">${날글2}</span>`)}</div>
    <div class="아주작 옅음">자세한 기록은 준비 중입니다</div>`; }''')

# 시트 고르기에 넣기
바꿈('''  else if(종==="보고방식") 안 = 보고방식시트();   // 10-03 v10 ua0b''',
     '''  else if(종==="보고방식") 안 = 보고방식시트();   // 10-03 v10 ua0b
  else if(종==="큰운동상세") 안 = 큰운동상세시트(U.시트.대상);   // v17 파란 상자 · 칸 누름''')

# ── 1 · 2 · 4 · 5 결과뷰 ──
# 2 띠: ‹ 업적 · 제목(+날짜) · › 스탯. data-enter 는 띠 전체로 (1)
바꿈('''    <div class="띠 결과띠 보고띠"><div class="결과띠속" data-enter="결과띠${저장키값||ss.시작}"><b>운동 보고서</b></div><span class="보고날">${년}.${String(월).padStart(2,"0")}.${String(일).padStart(2,"0")}.</span></div>
    ${프로필}''',
'''    <div class="띠 결과띠 보고띠" data-enter="결과띠${저장키값||ss.시작}"><button class="보고넘김 왼" data-act="스탯열기" data-v="업적" aria-label="업적">‹</button><div class="결과띠속"><b>운동 보고서</b><span class="보고날">${년}.${String(월).padStart(2,"0")}.${String(일).padStart(2,"0")}.</span></div><button class="보고넘김 오" data-act="스탯열기" data-v="스탯" aria-label="스탯">›</button></div>
    ${프로필}''')
# 1 상자들 들어옴 (아래→위)
바꿈('''  const 프로필 = 프로필보임 ? `<div class="보고상자 보고프로필 번호"${번("결1")}>''',
     '''  const 프로필 = 프로필보임 ? `<div class="보고상자 보고프로필 번호"${번("결1")} data-enter="보고프로필${상.키}">''')
바꿈('''    ${루틴보임?`<div class="보고상자 보고루틴${프로필보임?"":" 톱니있음"} 번호"${번("결2")}>''',
     '''    ${루틴보임?`<div class="보고상자 보고루틴${프로필보임?"":" 톱니있음"} 번호"${번("결2")} data-enter="보고루틴${상.키}">''')
바꿈('''    let h=`<button class="보고칸${열?" 펼침":""}" data-act="보고펼침" data-n="${i}" aria-expanded="${열}">''',
     '''    let h=`<button class="보고칸${열?" 펼침":""}" data-act="보고펼침" data-n="${i}" aria-expanded="${열}" data-enter="보고칸${상.키}-${i}">''')
# 4 · 5 아래 단추
바꿈('''    ${저장키값?`<div class="아랫줄 결과아래"><div class="작 옅음 결과저장글">기록은 저장되었습니다</div><button class="버튼 주" data-act="결과확인">확인</button></div>`
      :`<div class="아랫줄 결과아래"><button class="버튼 주" data-act="운동저장">운동 기록 저장하고 종료</button>
        <div class="줄"><button class="버튼 낮 채움" data-act="운동으로">운동으로 돌아가기</button><button class="버튼 낮 채움 나쁨" data-act="운동버림">${ss.버림?"한 번 더 누르면 버립니다":"기록하지 않고 종료"}</button></div></div>`}${시트()}</div>`; }''',
'''    ${저장키값?`<div class="아랫줄 결과아래"><button class="버튼 주" data-act="결과확인">확인</button></div>`
      :`<div class="아랫줄 결과아래 결과셋"><button class="버튼 끝작" data-act="운동으로">‹ 운동으로 돌아가기</button><button class="버튼 주 끝큰" data-act="운동저장">운동 기록 저장하고 종료</button><button class="버튼 끝작 나쁨${ss.버림?" 확인중":""}" data-act="운동버림">${ss.버림?"한 번 더 누르면 버립니다":"기록하지 않고 종료 ›"}</button></div>`}${시트()}</div>`; }''')

# ── 2 보고서 위에서 스탯 · 업적 열기 — 닫으면(탭 · Esc) 보고서로 ──
바꿈('''    case "스탯열기": U.스탯={보기:d.v==="업적"?"업적":"스탯", 분류:"달성", 더:false, 고름:null, 단위:"일"}; break;''',
     '''    case "스탯열기": U.스탯={보기:d.v==="업적"?"업적":"스탯", 분류:"달성", 더:false, 고름:null, 단위:"일", 보고:보고중()}; break;   // v17 보고:true 면 닫을 때 보고서로''')
바꿈('''    case "탭":
      if(ss&&ss.끝화면&&d.t!=="운동"){''',
     '''    case "탭":
      if(U.스탯?.보고&&보고중()){ U.스탯=null; break; }   // v17 보고서 위에 연 스탯 · 업적 — 탭을 누르면 닫고 보고서로 (저장 · 이동 없음)
      if(ss&&ss.끝화면&&d.t!=="운동"){''')
바꿈('''function 화면(){
  if(S.세션 && (U.탭==="운동" || S.세션.끝화면)){''',
'''/* v17 지금 운동 보고서가 보이는가 (끝 화면 · 저장된 기록 다시 보기) */
const 보고중 = ()=> !!(S.세션 ? S.세션.끝화면 : (S.결과 && S.기록[S.결과.key]));
function 화면(){
  if(U.스탯?.보고 && 보고중()) return 스탯화면();   // v17 보고서 ‹ › 로 연 업적 · 스탯
  if(S.세션 && (U.탭==="운동" || S.세션.끝화면)){''')

css = '''
/* ═══ 10-04 v17 B — 운동 보고서 (홍겸 님 값 · 새 값: 띠 높이 47.6 = 56×0.85 · 날짜 오른쪽 15 · 아래 15 · 파란 상자 85% · 톱니 15.3 = 18×0.85 · 들어옴 시간 ×2) ═══ */
/* 1 들어옴 — 띠는 위에서 아래로(.8s → 1.6s), 상자 · 칸은 아래에서 위로(.24s → .48s). 틀 전체가 같이 올라오던 것은 끔 */
@keyframes 보고내려옴{from{opacity:0;transform:translateY(-6px)}to{opacity:1;transform:none}}
@keyframes 보고올라옴{from{opacity:0;transform:translateY(6px)}to{opacity:1;transform:none}}
.넘김.결과틀.들어옴{animation:none}
.결과띠.보고띠.들어옴{animation:보고내려옴 1.6s cubic-bezier(.22,.61,.36,1) both}
.보고상자.들어옴,.보고칸.들어옴{animation:보고올라옴 .48s ease-out both}
/* 3 띠 높이 56 → 47.6 */
.결과띠.보고띠{min-height:47.6px}
/* 2 날짜 — 제목 글자 오른쪽 끝에서 15 오른쪽 · 제목 위 끝에서 15 아래 (아래첨자처럼) */
.보고띠 .결과띠속{position:relative;overflow:visible}
.보고띠 .보고날{left:calc(100% + 15px);top:15px;right:auto;bottom:auto;white-space:nowrap}
/* 2 ‹ 업적 · › 스탯 — 띠 양 끝, 누르는 칸 32 × 40 */
.보고넘김{position:absolute;top:50%;margin-top:-20px;width:32px;height:40px;display:flex;align-items:center;justify-content:center;font-size:22px;line-height:1;color:var(--강조글);background:none;border:0;padding:0}
.보고넘김.왼{left:0}.보고넘김.오{right:0}
/* 5 끝 단추 한 줄 — 돌아가기 1 : 저장 2 : 버림 1 (사이 8). 좁은 칸은 13 두 줄까지 */
.아랫줄.결과아래.결과셋{flex-direction:row;align-items:stretch}
.결과셋 .버튼{flex:1 1 0;min-width:0;height:40px;padding:0 4px;white-space:normal;word-break:keep-all;text-align:center;line-height:16px}
.결과셋 .버튼.끝큰{flex-grow:2}
/* '한 번 더 누르면 버립니다'(13 으로 141px)는 25% 칸(안쪽 71)에 13 두 줄로 못 들어간다 → 이때만 11 · 세 줄(줄 12) */
.결과셋 .버튼.확인중{font-size:11px;line-height:12px}
/* 6 파란 상자 — 칸 줄 전체 − 좌우 15 의 85%, 가운데 */
.결과수.큰수 .큰합{justify-self:center;width:calc((100% - 30px) * .85);margin:0}
/* 7 누르면 상세 */
.결과수.큰수 [data-act="시트"]{cursor:pointer}
/* 8 톱니 아이콘 18 → 15.3 */
.톱니단추 svg{width:15.3px;height:15.3px}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]
pathlib.Path(OUT).write_text(s, encoding='utf-8'); print("v17 B →", OUT)
