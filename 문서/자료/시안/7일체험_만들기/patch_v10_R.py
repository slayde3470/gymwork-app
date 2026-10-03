"""7일 체험 시안 — 운동 보고서 · 프로필 탭 (10-03 밤 홍겸 님 ✎ 표시 5개)
   python3 patch_v10_R.py 입력.html 출력.html

표시 → 고친 것
 0vnw 루틴 상자  "'달성' 삭제 · 테두리 말고 일반 종목박스처럼, 배경만 아주 흐리게 · 박스 크기 · 글씨 줄이기
                  + 프로필 박스 더 크게 · 사진 20% ↑ · 닉네임 10% ↓"
      → 루틴 상자 = 종목 칸(.보고칸)과 같은 1px --선 · 모서리 8, 바탕만 --면2. '달성' 딱지 없음.
        글자 한 단계씩 작게(이름 15→13 · 숫자 18→15 · 꼬리글 11 그대로 — 목록의 가장 작은 값), 틈 8→4 · 숫자/꼬리글 틈 4→0.
        프로필 사진 48 → 58(×1.2) · 닉네임 13 → 11(−10% 에 가장 가까운 목록 값). 프로필 = 테두리 상자(여백 12) · 숫자 18 Bold
 oaee 펼친 상세 세트 목록 → 왼쪽 칸 위→아래 먼저(1,2,3 | 4,5) — 캘린더 날짜 판 목록과 같은 grid-auto-flow:column
 pdb1 · ua0b 프로필 칸 → [N대] [스쿼트] [벤치] [데드] [+고른 것]. 숫자(+kg) 위 · 이름 아래. 이번 운동으로 바뀐 만큼 ▲(--오름)/▼(--내림) 단위 없이.
        '3대 1RM 합계' 줄 삭제. 4칸 이하는 한 줄, 5~6칸은 3칸씩 두 줄. 톱니(오른쪽 위에 겹쳐) → 시트 '결과 보고서 표시 방법'
        (보일 것: 프로필 · 루틴 결과 스위치 / 큰 운동: 스쿼트 · 벤치 · 데드 늘 켜짐 + OHP · 바벨 로우 · 스내치 · 클린 앤 저크 중 2개까지)
 rvva 탭줄 '메모' 칸 → 프로필 칸(글자 없는 동그란 사진 28). 프로필 탭 = 제목 · 사진 58 + 닉네임 · 큰 운동 칸 + 톱니 · 메모 줄.
        설정 탭 '운동 보고서' 묶음(프로필 표시 · 큰 운동 · 닉네임)은 톱니 시트와 프로필 탭으로 옮겨 뺐다
"""
import pathlib, sys
입력, 출력 = sys.argv[1], sys.argv[2]
s = pathlib.Path(입력).read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:80]!r}: {c}번")
    s = s.replace(old, new)
def 구간바꿈(시작, 끝, new):
    """시작 글부터 끝 글 바로 앞까지를 new 로 — 둘 다 한 번씩만 있어야 한다"""
    global s
    for x in (시작, 끝):
        if s.count(x) != 1: raise SystemExit(f"❌ {x[:80]!r}: {s.count(x)}번")
    a = s.index(시작); b = s.index(끝, a)
    s = s[:a] + new + s[b:]

# ══ 큰운동표 · 보고서 설정 · 큰 운동 칸 (보고서와 프로필 탭이 같이 쓴다) ══
구간바꿈('/* mxxq 큰 운동 — 3대', '/* mxxq 프로필 사진 — 종목 사진처럼', r'''/* 10-03 v10 pdb1 · ua0b 큰 운동 — [키(시트 칩 글), 칸 글, 이 시안에서 같은 종목으로 셀 이름들]. 순서 = 스쿼트 → 벤치 → 데드 → OHP → 바벨 로우 → 스내치 → 클린 앤 저크.
   앞 셋(SBD)은 늘 들어가고 뒤 넷에서 2개까지 더 고른다(합쳐 최대 5개). 이 시안 종목 목록에는 '오버헤드 프레스' · '펜들레이 로우' 만 있고
   스내치 · 클린 앤 저크는 없다 — 종목을 새로 만들지 않고, 종목 탭에서 그 이름으로 만들어 기록하면 셈에 들어간다(없으면 —) */
const 큰운동표 = [["스쿼트","스쿼트",["백 스쿼트","스쿼트","바벨 스쿼트","바벨 백 스쿼트"]], ["벤치","벤치",["벤치프레스","벤치 프레스","바벨 벤치프레스"]],
  ["데드","데드",["데드리프트","컨벤셔널 데드리프트","바벨 데드리프트"]],
  ["오버헤드 프레스","OHP",["오버헤드 프레스","바벨 오버헤드 프레스","밀리터리 프레스","OHP"]],
  ["바벨 로우","로우",["바벨 로우","바벨로우","펜들레이 로우","벤트오버 로우","벤트오버 바벨 로우","바벨 벤트오버 로우"]],
  ["스내치","스내치",["스내치","파워 스내치","바벨 스내치"]], ["클린 앤 저크","C&J",["클린 앤 저크","클린앤저크","클린 앤드 저크"]]];
/* ua0b 보고서 표시 방법 — S.설정.보고서보임 {프로필, 루틴} · S.설정.큰운동추가 [키…]. 옛 값(보고서프로필끔 · 큰운동 4/5)은 한 번 옮기고 지운다 */
function 보고설정(){ const s=S.설정;
  if(!s.보고서보임||typeof s.보고서보임!=="object") s.보고서보임={프로필:!s.보고서프로필끔, 루틴:true};
  if(!Array.isArray(s.큰운동추가)){ const n=+s.큰운동; s.큰운동추가 = n===5?["오버헤드 프레스","바벨 로우"] : n===4?["오버헤드 프레스"] : []; }
  delete s.보고서프로필끔; delete s.큰운동;
  const 보=s.보고서보임; 보.프로필=보.프로필!==false; 보.루틴=보.루틴!==false; if(!보.프로필&&!보.루틴) 보.루틴=true;   // 둘 다 꺼진 값은 받지 않는다
  s.큰운동추가 = 큰운동표.slice(3).map(x=>x[0]).filter(k=>s.큰운동추가.includes(k)).slice(0,2);
  return {보임:보, 추가:s.큰운동추가}; }
/* 큰 운동 값 — v = 이 기록까지(포함) 체크한 세트 중 가장 좋은 추정 1RM, 앞 = 이 기록 앞까지.
   rec 가 없으면(프로필 탭) 오늘까지 저장된 기록만. 맨 앞 칸 = N대 합계 */
function 큰운동값(rec, 저장키값){ const 날=저장키값?저장키값.split("~")[0]:(rec&&S.세션?S.세션.날:오늘());
  const 찬=e=>e.세트.filter(s=>s.완료);
  const 앞목록=Object.entries(S.기록).filter(([k])=>저장키값?키순(k,저장키값)<0:k.split("~")[0]<=날).map(([,r])=>r), 끝목록=rec?앞목록.concat([rec]):앞목록;
  const 최고=(목록,이름들)=>{ let v=0; for(const r of 목록) for(const e of r.종목) if(이름들.includes(e.이름)) for(const x of 찬(e)) if(x.w>0) v=Math.max(v,일RM(x.w,x.r)); return Math.round(v*10)/10; };
  const 추=보고설정().추가, 칸=큰운동표.filter(([키],j)=>j<3||추.includes(키)).map(([,글,이름들])=>({글, v:최고(끝목록,이름들), 앞:최고(앞목록,이름들)}));
  const 합=f=>Math.round(칸.reduce((a,x)=>a+x[f],0)*10)/10;
  return [{글:`${칸.length}대`, v:합("v"), 앞:합("앞")}, ...칸]; }
/* 큰 운동 칸 — 숫자 + kg (Bold) 위 · 이름(11 흐림) 아래 · 칸 사이 세로선(숫자 묶음 .결과수 와 같은 부품).
   차보임이면 숫자 옆에 바뀐 만큼 ▲3 / ▼3 (단위 없이 · 같으면 · 앞 기록이 없으면 안 씀) — 칸 폭이 모자라면 숫자 바로 아래로 내려간다.
   홍겸 님 10-03 "가로 1줄에 3대/sbd 나와야" — 칸 수(4~6)와 상관없이 늘 가로 한 줄. 칸이 좁아 ▲ 는 숫자 바로 아래로 간다.
   숫자 글자: 4칸 18 · 5칸 15 · 6칸 13 (CSS), 그래도 칸을 넘으면 큰값맞춤()이 한 줄 전체를 같은 크기로 한 단계씩 줄인다.
   톱니가 같은 상자에 있으면 칸 위를 16 비워 톱니 밑에 숫자가 깔리지 않게 */
/* 숫자가 칸을 넘으면 그 줄 숫자 전부를 같은 크기로 15 → 13 → 11 (한 줄 안 크기가 들쭉날쭉하지 않게) */
function 큰값맞춤(폰){ 폰.querySelectorAll(".결과수.큰수").forEach(판=>{ const bs=[...판.querySelectorAll(".큰값 b")];
  const 맞음=()=>bs.every(b=>{ const 칸=b.closest(".결과수.큰수>div"), z=getComputedStyle(칸); return b.getBoundingClientRect().width<=칸.clientWidth-parseFloat(z.paddingLeft)-parseFloat(z.paddingRight)+0.5; });
  bs.forEach(b=>b.style.fontSize=""); if(맞음()) return;
  const 처음=parseFloat(getComputedStyle(bs[0]).fontSize);
  for(const fs of [15,13,11]){ if(fs>=처음) continue; bs.forEach(b=>b.style.fontSize=fs+"px"); if(맞음()) break; } }); }
function 큰운동판(칸, 차보임, 톱니){ const 열=칸.length;
  return `<div class="결과수 큰수 열${열}${톱니?" 톱니비킴":""}" style="--열:${열}">${칸.map(x=>`<div><div class="큰값"><b>${x.v>0?차kg(x.v)+"kg":"—"}</b>${차보임&&x.v>0&&x.앞>0?보고차(x.v-x.앞,""):""}</div><span>${esc(x.글)}</span></div>`).join("")}</div>`; }
/* 프로필 동그라미 속 — 사진 / 닉네임 첫 글자 / 사람 그림 (보고서 · 프로필 탭 · 탭줄이 같이 쓴다) */
function 프로필그림(){ const 닉=String(S.설정.닉네임||"").trim(); return 프로필사진?`<img src="${프로필사진}" alt="">`:닉?`<b>${esc([...닉][0])}</b>`:사람그림; }
const 프로필고르기 = 큰 => `<label class="보고사진${큰?" "+큰:""}" aria-label="프로필 사진 고르기">${프로필그림()}<input type="file" accept="image/*" data-pf="사진" hidden></label>`;
/* ua0b 톱니 — 상자 오른쪽 위 모서리에 겹쳐 (새 줄 없이). 누르면 시트 '결과 보고서 표시 방법' */
const 톱니단추 = ()=> `<button class="톱니단추" data-act="시트" data-t="보고방식" aria-label="결과 보고서 표시 방법">${아이콘.톱니}</button>`;
function 보고방식시트(){ const {보임, 추가}=보고설정();
  return `<div class="머리"><b>결과 보고서 표시 방법</b><button class="닫기" data-act="시트닫기">닫기</button></div>
    <div class="이름표">보일 것</div>
    <div>${설정줄("프로필","",스위치(보임.프로필,"보고보임","프로필"))}<div class="구분"></div>${설정줄("루틴 결과","",스위치(보임.루틴,"보고보임","루틴"))}</div>
    <div class="이름표">큰 운동 · ${3+추가.length}대</div>
    <div class="칩줄 큰운동칩줄">${큰운동표.map(([키],j)=>{ const 늘=j<3, 켬=늘||추가.includes(키), 막=!켬&&추가.length>=2;
      return `<button class="칩${켬?" 켬":""}"${켬&&!늘?` data-enter="칩큰운동${esc(키)}"`:""} data-act="큰운동칩" data-v="${esc(키)}" aria-pressed="${켬}"${늘?' aria-disabled="true"':""}${막?" disabled":""}>${esc(키)}</button>`; }).join("")}</div>
    ${추가.length>=2?`<div class="아주작 옅음">최대 5개까지</div>`:""}`; }
''')

# ══ 결과뷰 — 프로필 상자 (pdb1 · ua0b · 0vnw) ══
구간바꿈('  /* mxxq 프로필 줄 — 큰 운동 1RM 은', '  /* 7jjc 종목 칸 · ya8r 상세', r'''  /* 10-03 v10 프로필 상자 = 테두리 상자: 왼쪽 사진 58 + 닉네임, 오른쪽 큰 운동 칸(바뀐 만큼 ▲▼). 톱니는 프로필 상자 오른쪽 위 — 프로필을 끄면 루틴 상자로 */
  const {보임}=보고설정(), 프로필보임=보임.프로필, 루틴보임=보임.루틴, 닉=String(S.설정.닉네임||"").trim();
  const 프로필 = 프로필보임 ? `<div class="보고상자 보고프로필 번호"${번("결1")}>
      <div class="보고나">${프로필고르기()}<span class="보고닉${닉?"":" 옅음"}">${닉?esc(닉):"닉네임"}</span></div>
      ${큰운동판(큰운동값(rec, 저장키값), true, true)}${톱니단추()}</div>` : "";
''')
바꿈('const 달성=기록달성(rec), 초=', 'const 초=')
# oaee 상세 세트 목록 — 행 수를 넘겨 왼쪽 칸부터 위→아래로
바꿈('<div class="보고세트들">', '<div class="보고세트들" style="--행:${Math.ceil(mx/2)}">')
# 0vnw 루틴 상자 — '달성' 없음 · 숫자 묶음은 늘 상자 안 · 프로필이 꺼져 있으면 톱니가 여기
바꿈('''    ${프로필보임?프로필:수}
    <div class="카드 보고루틴 번호"${번("결2")}><div class="줄"><b class="보고루틴이름">${esc(결과루틴이름(rec.이름))}</b><span class="알약 ${달성?"달성":"미달성"}">${달성?"달성":"미달성"}</span><span class="채움"></span>${루차!=null&&Math.abs(루차)>=0.05?`<span class="보고루차"><span>볼륨</span>${보고차(루차)}</span>`:""}</div>
      ${프로필보임?수:""}</div>''',
'''    ${프로필}
    ${루틴보임?`<div class="보고상자 보고루틴${프로필보임?"":" 톱니있음"} 번호"${번("결2")}><div class="줄 보고루틴머리"><b class="보고루틴이름">${esc(결과루틴이름(rec.이름))}</b><span class="채움"></span>${루차!=null&&Math.abs(루차)>=0.05?`<span class="보고루차"><span>볼륨</span>${보고차(루차)}</span>`:""}</div>
      ${수}${프로필보임?"":톱니단추()}</div>`:""}''')
# 보고서 위에서도 시트(톱니)가 열리게
바꿈('"기록하지 않고 종료"}</button></div></div>`}</div>`; }', '"기록하지 않고 종료"}</button></div></div>`}${시트()}</div>`; }')

# ══ 시트 '결과 보고서 표시 방법' ══
바꿈('''  else if(종==="메모"){''', '''  else if(종==="보고방식") 안 = 보고방식시트();   // 10-03 v10 ua0b
  else if(종==="메모"){''')

# ══ 누르기 — 스위치(하나는 남김) · 큰 운동 칩(2개까지) ══
바꿈('''    case "스위치": S.설정[d.f]=!S.설정[d.f]; break;''', '''    case "스위치": S.설정[d.f]=!S.설정[d.f]; break;
    /* 10-03 v10 ua0b 보고서 표시 방법 — 프로필 · 루틴 결과 중 하나는 늘 보인다 */
    case "보고보임": { const 보=보고설정().보임, 다른=d.f==="프로필"?"루틴":"프로필"; if(보[d.f]&&!보[다른]){ 토스트("하나는 보여야 합니다"); 다시=false; break; }   /* 다시 그리면 토스트가 지워진다 */ 보[d.f]=!보[d.f]; break; }
    /* 스쿼트 · 벤치 · 데드는 늘 켜짐(눌러도 그대로) · 나머지는 2개까지 */
    case "큰운동칩": { const 추=보고설정().추가, j=큰운동표.findIndex(x=>x[0]===d.v); if(j<3) break;
      if(추.includes(d.v)) 추.splice(추.indexOf(d.v),1); else if(추.length<2) 추.push(d.v); else { 토스트("최대 5개까지"); 다시=false; break; }
      보고설정(); break; }''')
바꿈('function 새칸채움(){ S.대표칭호=S.대표칭호??null;', 'function 새칸채움(){ S.대표칭호=S.대표칭호??null; 보고설정();')

# ══ rvva 탭줄 — '메모' 칸 → 프로필 칸 (글자 없이 사진만) ══
구간바꿈('function 탭줄(){', 'function 아래띠(){', r'''function 탭줄(){ const 탭들=[["캘린더",아이콘.달력],["루틴",아이콘.루틴],["플랜",아이콘.과녁],["종목",아이콘.바벨],["설정",아이콘.톱니]];
  /* 10-03 v10 rvva 여섯째 칸 = 프로필 — 글자 없이 동그란 사진 28. 켜지면 둘레 2px 강조 고리 */
  return `<nav class="탭줄">${탭들.map(([t,i])=>`<button data-act="탭" data-t="${t}" class="${U.탭===t?"켬":""}">${i}<span>${t}</span></button>`).join("")}<button data-act="탭" data-t="프로필" class="탭프로필${U.탭==="프로필"?" 켬":""}" aria-label="프로필"><span class="탭사진">${프로필그림()}</span></button></nav>`; }
''')
바꿈('U.탭==="종목"?종목탭():설정탭();', 'U.탭==="종목"?종목탭():U.탭==="프로필"?프로필탭():설정탭();')
바꿈('''    case "탭": if(d.t==="메모"){ U.시트={종류:"메모"}; break; }
''', '''    case "탭":
''')

# ══ 설정 탭 — '운동 보고서' 묶음 빼기 (톱니 시트 · 프로필 탭으로 옮김) ══
구간바꿈('    <div class="이름표">운동 보고서</div>', '    <div class="이름표">데이터</div>', '')

# ══ rvva 프로필 탭 ══
바꿈('''/* ── 시트 ── */
/* jmg1 · w0fo 종목 넣기 목록''', r'''/* ── 10-03 v10 rvva 프로필 탭 — 앞으로 홍겸 님이 꾸밀 화면. 지금은 최소한:
   제목 · 사진 58(눌러 바꾸기) + 닉네임 · 큰 운동 칸(보고서와 같은 함수 · ▲▼ 없이 · 오늘까지 기록) + 톱니 · 메모 줄 ── */
function 프로필탭(){ const 닉=String(S.설정.닉네임||"");
  return `<div class="넘김"><h1>프로필</h1><div class="쌓기">
    <div class="보고상자 프로필나 번호"${번("프0")}>${프로필고르기()}<label class="칸">닉네임<input class="입력" data-pf="닉네임" maxlength="12" autocomplete="off" placeholder="닉네임" value="${esc(닉)}"></label></div>
    <div class="보고상자 프로필큰 번호"${번("프1")}>${큰운동판(큰운동값(null,null), false, true)}${톱니단추()}</div>
    <button class="고르기 프로필메모" data-act="시트" data-t="메모"><b>메모</b><span class="곁">${S.메모.length?`${S.메모.length}개`:""}</span>›</button>
  </div></div>`; }

/* ── 시트 ── */
/* jmg1 · w0fo 종목 넣기 목록''')

# ══ 닉네임을 치면 동그라미 속 첫 글자도 바로 (다시 그리지 않아 입력 칸이 그대로) ══
바꿈('S.설정.닉네임=el.value.slice(0,12); 저장(); });', '''S.설정.닉네임=el.value.slice(0,12); 저장();
    /* 10-03 v10 — 사진이 없으면 동그라미 속 첫 글자도 따라 바뀐다 (보고서 · 프로필 탭 · 탭줄) */
    document.querySelectorAll("#폰 .보고사진, #폰 .탭사진").forEach(c=>{ const 입=c.querySelector("input"); c.innerHTML=프로필그림(); if(입) c.appendChild(입); }); });''')

css = '''
/* ═══ 10-03 v10 R 운동 보고서 · 프로필 탭 (✎ 0vnw · oaee · pdb1 · ua0b · rvva) ═══ */
/* 상자 공통 — 종목 칸(.보고칸)과 같은 1px --선 · 모서리 8 */
.보고상자{position:relative;border:1px solid var(--선);border-radius:8px;background:var(--면);padding:12px;min-width:0}
/* 0vnw 루틴 상자 — 바탕만 --면2 로 종목 칸과 구분. 글자 한 단계씩 작게(이름 13 · 숫자 15 · 꼬리글 11) · 틈 4 */
.보고상자.보고루틴{flex:none;display:flex;flex-direction:column;gap:4px;padding:8px 12px;background:var(--면2)}
.보고루틴이름{font-size:13px;line-height:18px}
.보고루틴 .결과수>div{gap:0}
.보고루틴 .결과수 b,.보고루틴 .결과수 b .올림수{font-size:15px;line-height:20px}
.보고루틴 .결과수>div>span{line-height:16px}
.보고루틴 .보고루차 .보고차{font-size:11px}
/* 프로필을 끄면 톱니가 루틴 상자 오른쪽 위로 — 이름 줄 오른쪽(볼륨 ▲)이 톱니 밑에 깔리지 않게 비운다 */
.보고루틴.톱니있음 .보고루틴머리{padding-right:24px}
/* 0vnw 프로필 상자 — 사진 58 (새 값: 48 × 1.2, 홍겸 님이 준 비율) · 닉네임 13 → 11 */
.보고상자.보고프로필{gap:12px}
.보고사진{width:58px;height:58px}
.보고닉{font-size:11px;line-height:16px}
/* pdb1 · ua0b 큰 운동 칸 — 숫자 묶음(.결과수)과 같은 부품: 숫자 18 Bold 위 · 이름 11 흐림 아래 · 칸 사이 세로 1px --선 */
.결과수.큰수{flex:1;min-width:0;display:grid;grid-template-columns:repeat(var(--열),minmax(0,1fr));row-gap:8px}
.결과수.큰수>div{padding:0 4px;gap:0}
.결과수.큰수.열3>div:nth-child(3n+1){border-left:0}
.결과수.큰수 b{font-size:18px;line-height:24px}
/* 늘 한 줄 — 5칸 15 · 6칸 13 (글자 목록 값). 칸 이름은 줄임말(벤치 · 데드 · OHP 처럼) — 로우 · C&J
   (6칸이면 칸이 약 48px 라 '바벨 로우' · '클린 앤 저크' 가 안 들어간다. 시트 칩은 원래 이름 그대로) */
.결과수.큰수.열5 b{font-size:15px}
.결과수.큰수.열6 b{font-size:13px}
/* 좁은 폰(상자 안쪽 331 이하)만 — 5~6칸은 한 줄에 숫자가 안 들어가 3칸씩 (폰 420 · 388 에서는 안 걸린다) */
@container (max-width:331px){ .보고프로필 .결과수.큰수.열5,.보고프로필 .결과수.큰수.열6{grid-template-columns:repeat(3,minmax(0,1fr))}
  .보고프로필 .결과수.큰수.열5>div:nth-child(3n+1),.보고프로필 .결과수.큰수.열6>div:nth-child(3n+1){border-left:0} }
.결과수.큰수>div>span{line-height:16px;max-width:100%;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.큰값{display:flex;flex-wrap:wrap;align-items:baseline;justify-content:center;column-gap:4px;max-width:100%;color:var(--글)}
.큰값 .보고차{margin-left:0}
.결과수 .보고차 span{color:inherit}
/* 톱니가 같은 상자에 있으면 첫 줄이 톱니(28) 밑으로 — 위 여백 12 + 16 */
.결과수.큰수.톱니비킴{padding-top:16px}
/* 사진 칸 72 → 64 (새 값: 사진 58 + 좌우 3) — 큰 운동 4칸이 폰 388 에서도 한 줄에 들어가게 */
.보고프로필 .보고나{width:64px}
/* 좁은 폰 — 칸에 숫자가 안 들어가면 두 칸씩. 기준(새 값) = 상자 안쪽 폭이 '칸마다 64'(숫자 '81.7kg' 18 Bold 약 56 + 여백 8,
   5~6칸은 합계 '1,000kg' 약 70 + 8 = 78) + 사진 칸 64 + 틈 12 보다 좁을 때. 폰 420 · 388 에서는 안 걸린다 */
.보고상자{container-type:inline-size}
@container (max-width:331px){ .보고프로필 .결과수.큰수.열4{grid-template-columns:repeat(2,minmax(0,1fr))} .보고프로필 .결과수.큰수.열4>div:nth-child(2n+1){border-left:0} }
@container (max-width:309px){ .보고프로필 .결과수.큰수.열3{grid-template-columns:repeat(2,minmax(0,1fr))} .보고프로필 .결과수.큰수.열3>div:nth-child(3n+1){border-left:1px solid var(--선)} .보고프로필 .결과수.큰수.열3>div:nth-child(2n+1){border-left:0} }
@container (max-width:255px){ .프로필큰 .결과수.큰수.열4{grid-template-columns:repeat(2,minmax(0,1fr))} .프로필큰 .결과수.큰수.열4>div:nth-child(2n+1){border-left:0} }
@container (max-width:233px){ .프로필큰 .결과수.큰수.열3{grid-template-columns:repeat(2,minmax(0,1fr))} .프로필큰 .결과수.큰수.열3>div:nth-child(3n+1){border-left:1px solid var(--선)} .프로필큰 .결과수.큰수.열3>div:nth-child(2n+1){border-left:0} }
/* ua0b 톱니 — 상자 오른쪽 위 모서리에 겹쳐 띄움 · 누르는 칸 28 · 아이콘 18 */
.톱니단추{position:absolute;top:0;right:0;width:28px;height:28px;display:flex;align-items:center;justify-content:center;color:var(--흐림);z-index:2}
.톱니단추 svg{width:18px;height:18px}
/* 시트 — 고를 수 없는 칩(2개를 이미 고름)은 흐리게 */
.칩:disabled{opacity:.35;cursor:default}
/* oaee 펼친 상세 세트 목록 — 왼쪽 칸 위→아래 먼저 (캘린더 날짜 판 목록과 같다) */
.보고세트들{grid-template-rows:repeat(var(--행),auto);grid-auto-flow:column}
/* rvva 탭줄 프로필 칸 — 글자 없이 동그란 사진 28 · 탭 높이 가운데 · 켜지면 둘레 2px 강조 고리 */
.탭줄 .탭프로필{justify-content:center;padding:0}
.탭사진{width:28px;height:28px;border-radius:50%;overflow:hidden;flex:none;display:flex;align-items:center;justify-content:center;background:var(--강조옅음);color:var(--강조);border:1px solid var(--선)}
.탭사진 img{width:100%;height:100%;object-fit:cover}
.탭사진 b{font-size:13px;font-weight:700}
.탭줄 .탭프로필.켬 .탭사진{box-shadow:0 0 0 2px var(--강조)}
/* rvva 프로필 탭 — 사진 + 닉네임 칸 · 큰 운동 칸(톱니 밑은 비움) · 메모 줄 */
.프로필나{display:flex;align-items:center;gap:12px}
.프로필메모{border-bottom:1px solid var(--선)}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]
# 다 그린 뒤 큰 운동 숫자가 칸을 넘으면 줄인다 (홍겸 님 '가로 1줄' — 6칸이면 칸이 좁다)
바꿈("  운자리맞춤(폰);   // 10-03 ✎ 9a8l · h6tu · i4gd", "  운자리맞춤(폰);   // 10-03 ✎ 9a8l · h6tu · i4gd\n  큰값맞춤(폰);   // 10-03 홍겸 님 '가로 1줄에 3대/sbd'")

pathlib.Path(출력).write_text(s, encoding='utf-8')
print("→", 출력, f"{len(s.encode()):,} 바이트")
