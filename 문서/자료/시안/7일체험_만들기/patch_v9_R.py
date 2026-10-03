"""7일 체험 시안 — '운동 끝' 결과 화면 → '운동 보고서' (10-03 홍겸 님 ✎ 표시 6개)
   python3 patch_v9_R.py 입력.html 출력.html

표시 → 고친 것
 mdot 띠 · 루틴 상자  "띠는 '루틴명 달성' 말고 '운동 보고서' · 띠 2줄 오른쪽 아래 년.월.일. 흰 글씨 작게 · 3번째 줄 · 날짜 지우기 ·
                       루틴 상자는 아래 종목 상자들과 구분 · 세트 / 총 볼륨 / 운동 시간 순서"
      → 띠 = '운동 보고서'(18 Bold 가운데) · 높이 56(두 줄) · 오른쪽 아래 '2026.10.03.'(11 강조글).
        루틴 상자 = .카드(2px 강조 테두리 · 모서리 16) · 종목 칸(1px 선 · 모서리 8)과 8 띄움. '14종목 · 달성도' 줄 · 날짜 삭제.
        띠에서 뺀 [달성/미달성] 알약은 루틴 이름 옆으로 옮겼다(정보는 남김). 숫자 순서 세트 / 총 볼륨 / 운동 시간
 wxgv 단추 · 구분선  → [운동 기록 저장하고 종료] · [기록하지 않고 종료]. 숫자 묶음 칸 사이마다 세로 1px --선 (칸이 몇 개든 사이마다)
 mxxq 프로필  → 띠 아래 = 프로필 줄: 동그란 사진 48(없으면 닉네임 첫 글자 · 닉네임도 없으면 사람 그림, 누르면 사진 고르기 —
        종목 사진처럼 이 브라우저에만) + 아래 닉네임. 오른쪽 = 3대/4대/5대 1RM(지금까지 가장 좋은 추정 1RM, 없으면 —)과 합계.
        세트 / 총 볼륨 / 운동 시간은 루틴 상자 안으로. 설정 탭 '운동 보고서' 묶음: 프로필 표시(끄면 숫자가 띠 아래로) · 큰 운동 3대/4대/5대 · 닉네임
 7jjc 종목 격자  → 두 칸 격자 · 칸 = 이름 / '1RM 78kg ▼2kg' / '볼륨 6,858kg ▲4,458kg'. 견주는 것은 '지난번'(그 종목을 한 바로 앞 기록)만,
        ▲ --오름 · ▼ --내림 · 같으면 표시 없음 · '1주 대비' '최고 대비' 글 없음. 폰 칸 860 높이에서 10칸이 한 화면
 ya8r 상세  → 칸을 누르면 그 줄 아래(두 칸 폭)로 상세: '1세트 · 60kg × 9회'(세트 → 무게 → 횟수) · 최고 세트 · 1주 · 최고 대비 칩 · 플랜 회차.
        다시 누르면 접힘 · 한 번에 하나. 격자만 넘어가고, 눌러도 넘긴 자리 그대로 · 상세가 아래로 숨으면 보이게 올린다
 gytb [플랜]  → 칸의 플랜 종목 이름 오른쪽 위 5px 겹쳐 [플랜] — 루틴 화면의 플랜딱지(.이름플랜 · .플랜표) 그대로
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

# ══ 결과뷰 → 운동 보고서 ══
구간바꿈('function 결과뷰(rec, 저장키값){', '\n\n/* ── 7일 막대 ── */', r'''/* ── 10-03 v9 R 운동 보고서 (홍겸 님 ✎ mdot · wxgv · mxxq · 7jjc · ya8r · gytb) ──
   띠 '운동 보고서' + 날짜 → 프로필 줄(사진 · 닉네임 · 큰 운동 1RM) → 루틴 상자(이름 · 숫자 묶음) → 종목 두 칸 격자(누르면 상세) → 단추 */
/* mxxq 큰 운동 — 3대(벤치 · 스쿼트 · 데드) / 4대(+오버헤드 프레스) / 5대(+펜들레이 로우). 다른 이름으로 만든 같은 종목도 센다 */
const 큰운동표 = [["벤치",["벤치프레스","벤치 프레스","바벨 벤치프레스"]], ["스쿼트",["백 스쿼트","스쿼트","바벨 스쿼트"]], ["데드",["데드리프트","컨벤셔널 데드리프트"]],
  ["OHP",["오버헤드 프레스","밀리터리 프레스"]], ["로우",["펜들레이 로우","바벨 로우","바벨로우"]]];
/* mxxq 프로필 사진 — 종목 사진처럼 기록과 따로, 이 브라우저에만 (고르는 곳은 아래 <script> 의 change) */
let 프로필사진 = null; try{ 프로필사진 = localStorage.getItem(저장키+"-프로필"); }catch(e){}
const 사람그림 = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="8" r="4"/><path d="M4 21c0-4.4 3.6-8 8-8s8 3.6 8 8"/></svg>`;
/* 보고서마다 펼친 칸 · 넘긴 자리 — 다른 보고서를 열면 처음부터 */
function 보고상태(키){ 키=String(키); if(!U.보고||U.보고.키!==키) U.보고={키, 펼침:null, 스크롤:0, 보임:false}; return U.보고; }
/* 7jjc 지난번 대비 — ▲ 숫자kg(--오름) / ▼ 숫자kg(--내림) / 같으면(0.05 미만) 아무것도 없음. 숫자는 0 부터 올라간다(m66q 칩과 같이) */
const 보고차 = (d, 단="kg") => { if(d==null||Math.abs(d)<0.05) return ""; const a=Math.round(Math.abs(d)*10)/10, 오=d>0;
  return `<span class="보고차 ${오?"오름":"내림"}">${오?"▲":"▼"}${올림수(a,차kg(a),"kg")}${단}</span>`; };
function 결과뷰(rec, 저장키값){ const 날=저장키값?저장키값.split("~")[0]:S.세션.날, ss=저장키값?null:S.세션, 상=보고상태(저장키값||ss.시작);
  /* 앱 Logic.kt 루틴향상 · 종목향상 — 1주 = 7일 전 ~ 전날 중 가장 좋은 날, 최고 = 지난 기록 전부. 볼륨은 지금 세트 수까지 잘라 견준다.
     저장된 뒤 보는 결과는 그 기록 자신을 빼고 견준다 (앱 10-01) — 상세(ya8r)에서만 보인다 */
  const 찬=e=>e.세트.filter(s=>s.완료), 볼=l=>l.reduce((a,s)=>a+s.w*s.r,0), rm=l=>l.reduce((a,s)=>Math.max(a,일RM(s.w,s.r)),0);
  const 남=Object.entries(S.기록).filter(([k])=>k!==저장키값&&k.split("~")[0]<=날), 한주=k=>{ const d=k.split("~")[0]; return d>=날더하기(날,-7)&&d<날; };
  const 최대=(l,f)=>{ const v=l.map(f).filter(x=>x>0); return v.length?Math.max(...v):null; }, 견줌=(a,b)=> b>0 ? a-b : null;
  /* 7jjc '지난번' = 이 운동 바로 앞 기록 (저장된 보고서는 그 기록보다 앞만) — 최근 것부터 */
  const 앞=남.filter(([k])=>!저장키값||키순(k,저장키값)<0).sort(([a],[b])=>키순(b,a));
  const 지금=rec.종목.flatMap(찬), n=지금.length, 루볼=볼(지금);
  const 루지=앞.find(([,r])=>r.루틴id===rec.루틴id&&r.종목.some(e=>찬(e).length)), 루차=루지?견줌(루볼,볼(루지[1].종목.flatMap(찬).slice(0,n))):null;
  const 달성=기록달성(rec), 초=Math.max(0,Math.round(rec.초||0)), 세트=완료수(rec);
  const [년,월,일]=날.split("-");
  /* wxgv 숫자 묶음 — 세트 / 총 볼륨 / 운동 시간 (mdot 순서), 칸 사이마다 세로선 */
  const 수=`<div class="결과수 번호"${번("결0")}>
      <div><b>${올림수(세트,String(세트),"정수")}</b><span>세트</span></div>
      <div><b>${올림수(Math.round(루볼),콤마(루볼))}kg</b><span>총 볼륨</span></div>
      <div><b>${올림수(초,분초(초),"시계")}</b><span>운동 시간</span></div></div>`;
  /* mxxq 프로필 줄 — 큰 운동 1RM 은 지금까지(이 운동까지) 체크한 세트 중 가장 좋은 추정 1RM */
  const 프로필보임=!S.설정.보고서프로필끔, 큰개수=[3,4,5].includes(+S.설정.큰운동)?+S.설정.큰운동:3, 닉=String(S.설정.닉네임||"").trim();
  let 프로필="";
  if(프로필보임){ const 지금까지=Object.entries(S.기록).filter(([k])=>저장키값?키순(k,저장키값)<=0:k.split("~")[0]<=날).map(([,r])=>r).concat(저장키값?[]:[rec]);
    const 큰=큰운동표.slice(0,큰개수).map(([글,이름들])=>{ let v=0; for(const r of 지금까지) for(const e of r.종목) if(이름들.includes(e.이름)) for(const x of 찬(e)) if(x.w>0) v=Math.max(v,일RM(x.w,x.r)); return {글, v:Math.round(v*10)/10}; });
    const 합=큰.reduce((a,x)=>a+x.v,0);
    프로필=`<div class="보고프로필 번호"${번("결1")}>
      <div class="보고나"><label class="보고사진" aria-label="프로필 사진 고르기">${프로필사진?`<img src="${프로필사진}" alt="">`:닉?`<b>${esc([...닉][0])}</b>`:사람그림}<input type="file" accept="image/*" data-pf="사진" hidden></label>
        <span class="보고닉${닉?"":" 옅음"}">${닉?esc(닉):"닉네임"}</span></div>
      <div class="보고큰"><div class="보고큰머리"><span>${큰개수}대 1RM 합계</span><b>${합>0?차kg(합)+"kg":"—"}</b></div>
        <div class="결과수 큰수">${큰.map(x=>`<div><b>${x.v>0?차kg(x.v):"—"}</b><span>${x.글}</span></div>`).join("")}</div></div></div>`; }
  /* 7jjc 종목 칸 · ya8r 상세 — 상세는 누른 칸이 있는 줄 바로 뒤에 두 칸 폭으로 */
  const 목=rec.종목.map((e,i)=>({e,i})).filter(x=>찬(x.e).length), 열칸=목.findIndex(x=>x.i===상.펼침);
  if(열칸<0) 상.펼침=null;
  const 뒤자리=열칸<0?-1:Math.min(열칸|1, 목.length-1);
  const 칸글=목.map(({e,i},p)=>{ const 세=찬(e), m=세.length, 지rm=rm(세), 지볼=볼(세), 맨=세.every(x=>x.w<=0);
    const 지난=앞.find(([,r])=>r.종목.some(x=>x.이름===e.이름&&찬(x).length)), 전=지난?지난[1].종목.filter(x=>x.이름===e.이름).flatMap(찬):[];
    /* 맨몸(무게 0)은 kg 대신 횟수로 — '최고 12회' / '합계 36회' */
    const 최회=Math.max(...세.map(x=>x.r)), 합회=세.reduce((a,x)=>a+x.r,0);
    const 줄1=맨?["최고",`${최회}회`,보고차(전.length?최회-Math.max(...전.map(x=>x.r)):null,"회")]:["1RM",`${차kg(지rm)}kg`,보고차(전.length?견줌(지rm,rm(전)):null)];
    const 줄2=맨?["합계",`${합회}회`,보고차(전.length?합회-전.slice(0,m).reduce((a,x)=>a+x.r,0):null,"회")]:["볼륨",`${콤마(지볼)}kg`,보고차(전.length?견줌(지볼,볼(전.slice(0,m))):null)];
    const 열=i===상.펼침, 이름=`<span class="이름플랜 보고이름"><b>${esc(e.이름)}</b>${e.플랜id?플랜딱지:""}</span>`;
    let h=`<button class="보고칸${열?" 펼침":""}" data-act="보고펼침" data-n="${i}" aria-expanded="${열}">${이름}
      ${[줄1,줄2].map(([a,b,c])=>`<span class="보고줄"><span class="보고값"><i>${a}</i> ${b}</span>${c}</span>`).join("")}</button>`;
    if(p===뒤자리){ const x=목[열칸].e, 세x=찬(x), mx=세x.length, rmx=rm(세x), 볼x=볼(세x);
      const 과=남.map(([k,r])=>[k,r.종목.filter(y=>y.이름===x.이름).flatMap(찬)]).filter(([,l])=>l.length), 주=과.filter(([k])=>한주(k));
      const 좋=세x.reduce((a,y)=>!a||일RM(y.w,y.r)>일RM(a.w,a.r)||(y.w<=0&&a.w<=0&&y.r>a.r)?y:a,null), 세글=y=>`${y.w>0?`${kg(y.w)}kg × `:""}${y.r}회`;
      const 플글=x.플랜id?플랜미리(x,!!저장키값):"";
      h+=`<div class="보고상세" data-enter="보고상세${상.키}-${상.펼침}">
        <div class="줄"><span class="이름플랜"><b>${esc(x.이름)}</b>${x.플랜id?플랜딱지:""}</span><span class="채움"></span><span class="작 흐림 숫">${mx}/${x.세트.length}세트</span></div>
        <div class="보고세트들">${x.세트.map((y,k)=>y.완료?`<span>${k+1}세트 · ${세글(y)}</span>`:"").join("")}</div>
        ${좋?`<div class="작">최고 세트 · ${세글(좋)}</div>`:""}
        ${rmx>0?향상줄(`1RM ${차kg(rmx)}kg`, 견줌(rmx,최대(주,([,l])=>rm(l))), 견줌(rmx,최대(과,([,l])=>rm(l)))):""}
        ${볼x>0?향상줄(`볼륨 ${콤마(볼x)}kg`, 견줌(볼x,최대(주,([,l])=>볼(l.slice(0,mx)))), 견줌(볼x,최대(과,([,l])=>볼(l.slice(0,mx))))):""}
        ${플글?`<div class="아주작 흐림">${플글}</div>`:""}</div>`; }
    return h; }).join("");
  return `<div class="화면"><div class="넘김 결과틀">
    <div class="띠 결과띠 보고띠"><div class="결과띠속" data-enter="결과띠${저장키값||ss.시작}"><b>운동 보고서</b></div><span class="보고날">${년}.${String(월).padStart(2,"0")}.${String(일).padStart(2,"0")}.</span></div>
    ${프로필보임?프로필:수}
    <div class="카드 보고루틴 번호"${번("결2")}><div class="줄"><b class="보고루틴이름">${esc(결과루틴이름(rec.이름))}</b><span class="알약 ${달성?"달성":"미달성"}">${달성?"달성":"미달성"}</span><span class="채움"></span>${루차!=null&&Math.abs(루차)>=0.05?`<span class="보고루차"><span>볼륨</span>${보고차(루차)}</span>`:""}</div>
      ${프로필보임?수:""}</div>
    <div class="보고목록 번호"${번("결3")} data-rk="${esc(상.키)}">${칸글||`<div class="보고빈 작 옅음">체크한 세트가 없습니다</div>`}</div></div>
    ${저장키값?`<div class="아랫줄 결과아래"><div class="작 옅음 결과저장글">기록은 저장되었습니다</div><button class="버튼 주" data-act="결과확인">확인</button></div>`
      :`<div class="아랫줄 결과아래"><button class="버튼 주" data-act="운동저장">운동 기록 저장하고 종료</button>
        <div class="줄"><button class="버튼 낮 채움" data-act="운동으로">운동으로 돌아가기</button><button class="버튼 낮 채움 나쁨" data-act="운동버림">${ss.버림?"한 번 더 누르면 버립니다":"기록하지 않고 종료"}</button></div></div>`}</div>`; }''')

# ══ ya8r 칸 누르기 — 펼침 / 접힘 ══
바꿈('''    case "결과확인": S.결과=null; U.탭="캘린더"; break;
''', '''    case "결과확인": S.결과=null; U.탭="캘린더"; break;
    /* 10-03 ya8r 운동 보고서 칸 — 누르면 그 줄 아래로 상세, 다시 누르면 접힘. 한 번에 하나. 펼치면 상세가 보이게 격자를 올린다(아래 <script>) */
    case "보고펼침": { const 상=U.보고; if(!상) break; const n=+d.n; 상.펼침 = 상.펼침===n ? null : n; 상.보임 = 상.펼침!=null; break; }
''')

# ══ mxxq 설정 탭 — '운동 보고서' 묶음 (스위치 · 칩줄 · 입력 = 설정 탭의 기존 부품) ══
바꿈('''    <div class="이름표">데이터</div>''',
'''    <div class="이름표">운동 보고서</div><div class="카드 번호"${번("설6")}>
      ${설정줄("보고서에 프로필 표시","끄면 세트 · 볼륨 · 시간이 띠 아래로",스위치(!s.보고서프로필끔,"스위치","보고서프로필끔"))}<div class="구분"></div>
      ${설정줄("큰 운동","프로필 옆에 1RM 과 합계","")}${칩줄("설정칩","큰운동",[3,4,5],[3,4,5].includes(+s.큰운동)?+s.큰운동:3,v=>v+"대")}<div class="구분" style="margin-top:10px"></div>
      ${설정줄("닉네임","프로필 사진 아래",`<input class="입력 닉네임칸" data-pf="닉네임" maxlength="12" autocomplete="off" placeholder="닉네임" aria-label="닉네임" value="${esc(s.닉네임||"")}">`)}</div>
    <div class="이름표">데이터</div>''')

# ══ 프로필 사진 고르기 · 닉네임 입력 · 격자 넘긴 자리 ══
js = r'''
<script>
/* ═══ 10-03 v9 R 운동 보고서 — 프로필 사진 · 닉네임 (mxxq) · 격자 넘긴 자리 · 펼친 상세 보이기 (ya8r) ═══ */
(()=>{
  const 폰=document.getElementById("폰");
  /* mxxq 동그라미를 누르면 사진 고르기 — 종목 사진과 같은 줄인사진(480px · jpeg), 기록과 따로 이 브라우저에만 */
  document.addEventListener("change", async e=>{ const el=e.target; if(el?.dataset?.pf!=="사진") return; const f=el.files&&el.files[0]; if(!f) return;
    try{ 프로필사진=await 줄인사진(f); }catch(_){ 토스트("이 사진은 읽지 못했습니다"); return; }
    try{ localStorage.setItem(저장키+"-프로필", 프로필사진); }catch(_){ 토스트("저장 공간이 모자라 사진은 이 화면에만 남습니다"); }
    발자취("프로필 사진"); 그리기(); });
  /* mxxq 닉네임 — 치는 대로 남긴다 (다시 그리지 않아 입력 칸이 그대로) */
  document.addEventListener("input", e=>{ const el=e.target; if(el?.dataset?.pf!=="닉네임") return; S.설정.닉네임=el.value.slice(0,12); 저장(); });
  /* ya8r 격자만 넘어간다 — 칸을 눌러 다시 그려도 넘긴 자리 그대로. 펼친 상세가 아래로 숨으면 그 칸부터 보이게 올린다 */
  document.addEventListener("scroll", e=>{ const l=e.target; if(!l?.classList?.contains("보고목록")||!U.보고||U.보고.키!==l.dataset.rk) return; U.보고.스크롤=l.scrollTop; }, true);
  new MutationObserver(()=>{ const l=폰.querySelector(".보고목록"); if(!l||!U.보고||U.보고.키!==l.dataset.rk) return;
    l.scrollTop=U.보고.스크롤||0;
    if(!U.보고.보임) return; U.보고.보임=false;
    const 상=l.querySelector(".보고상세"), 칸=l.querySelector(".보고칸.펼침"); if(!상||!칸) return;
    const 위=칸.offsetTop-4, 끝=상.offsetTop+상.offsetHeight+4, 보=l.clientHeight; let t=l.scrollTop;
    if(끝>t+보) t=Math.min(위, 끝-보); if(위<t) t=위;
    if(Math.abs(t-l.scrollTop)>1) l.scrollTo({top:t, behavior:움직임줄임()?"auto":"smooth"}); }).observe(폰,{childList:true});
})();
</script>
'''
끝 = s.rfind('</script>') + len('</script>'); s = s[:끝] + '\n' + js + s[끝:]

css = '''
/* ═══ 10-03 v9 R 운동 보고서 (✎ mdot · wxgv · mxxq · 7jjc · ya8r · gytb) ═══ */
/* mdot 띠 = 두 줄 높이(56). 제목은 가운데, 날짜는 오른쪽 아래 11 강조글 */
.결과띠.보고띠{position:relative;min-height:56px}
.보고날{position:absolute;right:12px;bottom:4px;font-size:11px;line-height:16px;font-weight:400;color:var(--강조글);font-variant-numeric:tabular-nums}
/* wxgv 숫자 묶음 — 칸이 몇 개든 똑같이 나누고 사이마다 세로 1px 선 */
.결과수{display:flex;gap:0}
.결과수>div{flex:1 1 0}
.결과수>div+div{border-left:1px solid var(--선)}
.결과수.큰수 b{font-size:15px}
/* mxxq 프로필 줄 — 왼쪽 동그란 사진 48 + 닉네임, 오른쪽 큰 운동 1RM (숫자 묶음과 같은 부품) */
.보고프로필{display:flex;align-items:center;gap:12px;flex:none}
.보고나{display:flex;flex-direction:column;align-items:center;gap:4px;width:72px;flex:none}
.보고사진{width:48px;height:48px;border-radius:50%;background:var(--강조옅음);color:var(--강조);border:1px solid var(--선);display:flex;align-items:center;justify-content:center;overflow:hidden;cursor:pointer;flex:none}
.보고사진 img{width:100%;height:100%;object-fit:cover}
.보고사진 b{font-size:22px;font-weight:700}
.보고사진 svg{width:28px;height:28px}
.보고닉{font-size:13px;font-weight:700;line-height:18px;max-width:100%;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.보고큰{flex:1;min-width:0;display:flex;flex-direction:column;gap:4px}
.보고큰머리{display:flex;align-items:baseline;justify-content:space-between;gap:8px;font-size:11px;color:var(--흐림)}
.보고큰머리 b{font-size:15px;font-weight:700;color:var(--글);font-variant-numeric:tabular-nums;white-space:nowrap}
/* mdot 루틴 상자 = .카드 (2px 강조 · 모서리 16) — 종목 칸(1px 선 · 모서리 8)과 모양으로 구분 */
.카드.보고루틴{flex:none;padding:8px 12px;display:flex;flex-direction:column;gap:8px}
.보고루틴이름{font-size:15px;font-weight:700;min-width:0;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.보고루틴 .알약{flex:none}
.보고루차{display:inline-flex;align-items:baseline;gap:4px;font-size:11px;color:var(--흐림);flex:none;white-space:nowrap}
/* 7jjc 종목 두 칸 격자 — 이것만 넘어간다 */
.보고목록{flex:1;min-height:0;overflow-y:auto;display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:4px;align-content:start;position:relative}
.보고칸{display:flex;flex-direction:column;gap:4px;min-width:0;padding:8px;border:1px solid var(--선);border-radius:8px;background:var(--면);text-align:left}
.보고칸.펼침{border-color:var(--강조);background:var(--강조옅음)}
.보고이름{max-width:100%}
.보고이름>b{font-size:13px;font-weight:700;line-height:18px}
.보고줄{display:flex;flex-wrap:wrap;align-items:baseline;gap:0 4px;min-width:0;font-size:13px;line-height:18px}
.보고값{white-space:nowrap;font-variant-numeric:tabular-nums}
.보고값 i{font-style:normal;font-size:11px;color:var(--흐림)}
.보고차{margin-left:auto;white-space:nowrap;font-size:11px;font-weight:700;font-variant-numeric:tabular-nums}
.보고차.오름{color:var(--오름)} .보고차.내림{color:var(--내림)}
.보고루차 .보고차{margin-left:0;font-size:13px}
.보고빈{grid-column:1/-1;padding:8px 4px}
/* ya8r 상세 — 누른 칸의 줄 바로 아래, 두 칸 폭. 세트는 두 줄로 나눠 높이를 아낀다 */
.보고상세{grid-column:1/-1;min-width:0;background:var(--면2);border-radius:8px;padding:8px 12px;display:flex;flex-direction:column;gap:4px}
.보고상세 .이름플랜 b{font-size:15px;font-weight:700}
.보고세트들{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:0 8px;font-size:13px;line-height:18px;font-variant-numeric:tabular-nums}
/* mxxq 설정 — 닉네임 칸 */
.닉네임칸{width:144px;flex:none}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]

pathlib.Path(출력).write_text(s, encoding='utf-8')
print("→", 출력, f"{len(s.encode()):,} 바이트")
