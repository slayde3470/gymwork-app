"""7일 체험 시안 — 결과 화면 · 숫자/게이지 속도 · 근육 '빨개지는 기준' 임시 칸 (10-03 홍겸 님 ✎ 표시 3개)
   python3 patch_v7_res.py 입력.html 출력.html

표시 → 고친 것
 m66q 결과 화면  "지난번에 너무 좋았는데 왜 이렇게 바뀐거지? 다시 구성해봐. ▲ 숫자(빨강) · ▼ 숫자(파랑) · 같으면 아무 표시 없음.
                  1·2·3·4·5자리 이상 애니메이션 속도 다르게 넣으라고 했던 것도 빠졌네"
     → '지난번' = 앱 WorkoutScreen.kt 마무리() (09-27 시안 → 앱). 그 구성 그대로 다시 짰다:
        ① 제목 띠 [루틴 이름 루틴 · 달성/미달성] 가운데, 왼쪽부터 드러남(0.8초)
        ② 세트 · 시간 · 볼륨 세 숫자 — 0 부터 올라간다 (자릿수 시간)
        ③ 카드 — 위(면2): 루틴 이름 · 날짜 / 볼륨 [1주 대비 ▲][최고 대비 ▲] / N종목 · 달성도
                 아래(카드 안에서만 넘김): 세트를 한 종목마다 이름 / 1RM [1주][최고] / 볼륨 [1주][최고] (+ 시안의 플랜 회차 한 줄)
        ④ [기록 저장하고 끝내기] · [운동으로 돌아가기][기록 없이 끝내기] — 저장된 결과는 '기록은 저장되었습니다' + [확인]
        비교 칩: ▲ 숫자 kg(--오름) / ▼ 숫자 kg(--내림) / 같으면 칩을 그리지 않는다('유지' 도 없음). 칩 숫자도 0 부터 올라간다
        (앱의 '유지' 와 시안의 '+12.3%' 부호 글자는 이 규칙으로 바꿨다)
 nasu 스탯 화면  "숫자가 올라가거나 게이지가 올라가는 애니메이션 35% 더 천천히"
     → CSS 변수 --올림배수:1.35 한곳. 숫자 = 앱 Motion.kt 자릿수 표 × 배수, 게이지 채움(.게이지3 · 스탯 막대) = 0.7초 × 배수
 cxi1 근육 그림  "가슴운동 여러개 남았는데 벌써 시뻘개지다니. '빨개지는 기준'을 수치화할 수 있는 박스를 아티팩트에만 임시로"
     → 폰 밖 체험 막대에 [근육 기준] 단추 · 부위 12개 칸(가장 빨개지는 볼륨 kg + 계산 메모). 넣으면 그 부위는
        '이전 최대(처음이면 오늘 계획)' 대신 그 값을 20단계 기준으로 쓴다 — 바로 그림에 반영. db 모음 thresholds 에 남는다
"""
import pathlib, sys
입력, 출력 = sys.argv[1], sys.argv[2]
s = pathlib.Path(입력).read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:80]!r}: {c}번")
    s = s.replace(old, new)

# ══ cxi1 — 근육 단계 기준: 부위 칸에 넣은 값이 있으면 그것을 20단계 기준으로 ══
바꿈('''/* 오늘 오른 단계 = 20 × 오늘 볼륨 ÷ 이전 최대 (처음이면 오늘 계획) — 20 에서 멈춘다 */
function 오늘단계(종목들){ const v=잎볼륨(종목들), 계=잎볼륨(종목들,true), out={};
  for(const [l,x] of Object.entries(v)){ const 기=S.최대볼륨[l]>0?S.최대볼륨[l]:(계[l]||x); out[l]=Math.min(20,20*x/기); } return out; }''',
'''/* 10-03 cxi1 '빨개지는 기준' 임시 칸 (시안에서만) — 홍겸 님 "가슴운동 여러 개 남았는데 벌써 시뻘개지다니"
   부위마다 '가장 빨개지는 볼륨(kg)' 을 넣으면 그 부위의 잎은 이전 최대(처음이면 오늘 계획) 대신 그 값을 20단계 기준으로 쓴다.
   비우면 지금 규칙. 부위는 근육 나무(근육부모)의 큰 묶음 — 키는 그 묶음의 영문 id (db 문서 id 로도 쓴다) */
const 기준부위 = [["chest","가슴"],["back","등"],["shoulders","어깨"],["upper_arm_front","이두"],["upper_arm_back","삼두"],["forearm","전완"],
  ["core","복근·코어"],["glutes","둔근"],["quads","대퇴사두"],["hamstrings","햄스트링"],["adductors","내전근"],["calves","종아리"]];
const 근육기준값 = {};   // 키 → {볼륨:kg|null, 메모}
let 잎부위표 = null;
function 잎부위(l){ if(!잎부위표){ 잎부위표={}; for(const [k] of 기준부위) for(const x of 잎(k)) 잎부위표[x]=k; } return 잎부위표[l]; }
function 정한기준(l){ const x=근육기준값[잎부위(l)]; return x&&x.볼륨>0 ? x.볼륨 : 0; }
/* 오늘 오른 단계 = 20 × 오늘 볼륨 ÷ 기준 — 기준은 (넣은 값) → 이전 최대 → (처음이면) 오늘 계획. 20 에서 멈춘다 */
function 오늘단계(종목들){ const v=잎볼륨(종목들), 계=잎볼륨(종목들,true), out={};
  for(const [l,x] of Object.entries(v)){ const 정=정한기준(l), 기=정>0?정:S.최대볼륨[l]>0?S.최대볼륨[l]:(계[l]||x); out[l]=Math.min(20,20*x/기); } return out; }''')

# ══ m66q · nasu — 숫자 올라가는 시간: 앱 Motion.kt 자릿수 표 × 올림배수, 글꼴은 data-fmt 로 ══
바꿈('''  if(!움직임줄임()) root.querySelectorAll(".넘김.들어옴 [data-count]").forEach(el=>{ const 끝=+el.dataset.count, 시=performance.now(), 길=Math.min(2000,500+String(끝).length*250);
    const 돌=now=>{ const t=Math.min(1,(now-시)/길), v=끝*(1-Math.pow(1-t,3)); el.textContent=콤마(v); if(t<1) requestAnimationFrame(돌); }; requestAnimationFrame(돌); }); }''',
'''  /* 10-03 m66q · nasu: 시간은 자릿수 표(앱 Motion.kt) × 올림배수. 0 에서 시작해 끝에서 느려진다(ease-out cubic) */
  if(!움직임줄임()) root.querySelectorAll(".넘김.들어옴 [data-count]").forEach(el=>{ const 끝=+el.dataset.count, 시=performance.now(), 길=자릿수시간(끝), 꼴=숫자꼴[el.dataset.fmt]||콤마;
    const 돌=now=>{ const t=Math.min(1,Math.max(0,(now-시)/길)), v=끝*(1-Math.pow(1-t,3)); el.textContent=꼴(v); if(t<1) requestAnimationFrame(돌); }; 돌(시); }); }''')

# ══ m66q — 결과 화면을 앱 마무리() 구성으로 ══
바꿈('''function 결과뷰(rec, 저장키값){ const 날=저장키값?저장키값.split("~")[0]:S.세션.날;
  const 같은=Object.entries(S.기록).filter(([k,r])=>k!==저장키값&&r.루틴id===rec.루틴id&&k.split("~")[0]<=날).sort(([a],[b])=>a<b?-1:1);
  const 볼=기록볼륨(rec), 지난=같은.length?같은[같은.length-1]:null, 최고=같은.reduce((a,[,r])=>Math.max(a,기록볼륨(r)),0);
  const 퍼=(a,b)=> b>0?`${a>=b?"+":"−"}${Math.abs((a-b)/b*100).toFixed(1)}%`:"—";
  const 줄=rec.종목.map(e=>{ const 세=e.세트.filter(s=>s.완료); const 좋=세.reduce((a,s)=>!a||일RM(s.w,s.r)>일RM(a.w,a.r)?s:a,null);
    const 플글 = e.플랜id ? 플랜미리(e, !!저장키값) : "";
    return `<div class="판줄"><div class="채움"><div class="줄"><b class="채움">${esc(e.이름)}</b><span class="작 숫">${세.length}/${e.세트.length}</span></div>
      <div class="맞춤 아주작 흐림 숫">${좋?`최고 ${kg(좋.w)}kg × ${좋.r} · 1RM 약 ${kg(Math.round(일RM(좋.w,좋.r)*2)/2)}kg`:"한 세트도 안 함"}${플글?" · "+플글:""}</div></div></div>`; }).join("");
  return `<div class="화면"><div class="넘김"><h1>${저장키값?"지난 운동 결과":"운동 끝"}</h1><div class="작 흐림" style="margin-bottom:8px">${esc(rec.이름)} · ${날글(날)} (${요일(날)})${저장키값?" · 저장했습니다":""}</div>
    <div class="타일들 번호"${번("결0")}><div class="타일"><span>세트</span><b>${완료수(rec)}/${전체수(rec)}</b></div><div class="타일"><span>시간</span><b>${분초(rec.초)}</b></div><div class="타일"><span>볼륨 kg</span><b data-count="${Math.round(볼)}">${콤마(볼)}</b></div></div>
    <div class="판" style="padding:10px 0"><div class="판줄"><span class="이름">지난번</span><span class="채움 작">${지난?`${날글(지난[0])} 대비 <b>${퍼(볼,기록볼륨(지난[1]))}</b>`:"같은 루틴 기록 없음"}</span></div>
      <div class="판줄"><span class="이름">최고</span><span class="채움 작">${최고>0?(볼>최고?"<b>최고 기록 경신</b>":`최고 대비 <b>${퍼(볼,최고)}</b>`):"—"}</span></div>${줄}</div></div>
    <div class="아랫줄">${저장키값?`<button class="버튼 주 채움" data-act="결과확인">확인</button>`:`<button class="버튼" data-act="운동으로">돌아가기</button><button class="버튼 주 채움" data-act="운동저장">저장</button>`}</div></div>`; }''',
'''/* ── 10-03 m66q 결과 화면 — 앱 WorkoutScreen.kt 마무리() 구성 그대로 (홍겸 님 "지난번에 너무 좋았는데 왜 바뀌었지 · 다시 구성해봐") ──
   숫자 올라가는 시간 = 앱 Motion.kt 자릿수 표 (1자리 1초 · 2자리 1.25 · 3자리 1.5 · 4자리 2 · 5자리 2.5 · 6자리 3 · 그 뒤 한 자리에 0.5)
   × 올림배수 (nasu "35% 더 천천히" — CSS --올림배수 한곳. 게이지 채움도 같은 배수) */
const 올림배수 = parseFloat(getComputedStyle(document.documentElement).getPropertyValue("--올림배수")) || 1;
function 자릿수시간(v){ const n=String(Math.round(Math.abs(+v||0))).length;
  const 초 = n<=1?1 : n===2?1.25 : n===3?1.5 : n===4?2 : n===5?2.5 : 3+0.5*(n-6);
  return 초*1000*올림배수; }
/* 차이 kg 글 — 앱 kg글: 100 이상이거나 정수면 콤마 정수, 아니면 소수 한 자리 */
const 차kg = x => { const a=Math.abs(x); return a>=100||Math.abs(a-Math.round(a))<1e-9 ? 콤마(a) : a.toFixed(1); };
const 숫자꼴 = {정수:v=>String(Math.round(v)), 시계:v=>분초(v), kg:v=>차kg(Math.round(v*10)/10)};
const 결과루틴이름 = 이름 => 이름.includes("루틴") ? 이름 : `${이름} 루틴`;   // 앱 루틴표시
/* 올라가는 숫자 — 끝 글자만큼 자리(숫자 1ch · 쉼표/점/쌍점 0.4ch)를 미리 잡아 둬서 자릿수가 늘어도 옆 글자가 밀리지 않는다 */
const 자리폭 = 글 => { const 점=(글.match(/[,.:]/g)||[]).length; return +(글.length-점+점*0.4).toFixed(1); };
const 올림수 = (값, 글, 꼴="") => `<span class="올림수" style="min-width:${자리폭(글)}ch" data-count="${값}"${꼴?` data-fmt="${꼴}"`:""}>${글}</span>`;
/* 비교 칩 [1주 대비 ▲ 5kg] — 오르면 ▲ 빨강, 내리면 ▼ 파랑, 같으면(0.05kg 미만) 칩을 그리지 않는다 (m66q) */
function 대비칩(이름, d){ if(d==null||Math.abs(d)<0.05) return ""; const a=Math.round(Math.abs(d)*10)/10, 오=d>0;
  return `<span class="대비칩"><span>${이름}</span><b class="${오?"오름":"내림"}">${오?"▲":"▼"} ${올림수(a,차kg(a),"kg")}kg</b></span>`; }
/* 값 글은 자르지 않는다 — 폭이 모자라면 두 칩이 함께 다음 줄로 (시안 폰은 실제 폰보다 좁다) */
const 향상줄 = (앞글, 주, 최) => { const 칩=대비칩("1주 대비",주)+대비칩("최고 대비",최);
  return `<div class="향줄"><span class="향글">${앞글}</span>${칩?`<span class="칩묶음">${칩}</span>`:""}</div>`; };
function 결과뷰(rec, 저장키값){ const 날=저장키값?저장키값.split("~")[0]:S.세션.날, ss=저장키값?null:S.세션;
  /* 앱 Logic.kt 루틴향상 · 종목향상 — 1주 = 7일 전 ~ 전날 중 가장 좋은 날, 최고 = 지난 기록 전부. 볼륨은 지금 세트 수까지 잘라 견준다.
     저장된 뒤 보는 결과는 그 기록 자신을 빼고 견준다 (앱 10-01) */
  const 찬=e=>e.세트.filter(s=>s.완료), 볼=l=>l.reduce((a,s)=>a+s.w*s.r,0), rm=l=>l.reduce((a,s)=>Math.max(a,일RM(s.w,s.r)),0);
  const 남=Object.entries(S.기록).filter(([k])=>k!==저장키값&&k.split("~")[0]<=날), 한주=k=>{ const d=k.split("~")[0]; return d>=날더하기(날,-7)&&d<날; };
  const 최대=(l,f)=>{ const v=l.map(f).filter(x=>x>0); return v.length?Math.max(...v):null; }, 견줌=(a,b)=> b>0 ? a-b : null;
  const 지금=rec.종목.flatMap(찬), n=지금.length, 루볼=볼(지금);
  const 루과=남.filter(([,r])=>r.루틴id===rec.루틴id).map(([k,r])=>[k,r.종목.flatMap(찬)]).filter(([,l])=>l.length);
  const 루주=견줌(루볼,최대(루과.filter(([k])=>한주(k)),([,l])=>볼(l.slice(0,n)))), 루최=견줌(루볼,최대(루과,([,l])=>볼(l.slice(0,n))));
  const 달성=기록달성(rec), 달성도=전체수(rec)?Math.round(완료수(rec)*100/전체수(rec)):0;
  const 종목줄=rec.종목.filter(e=>찬(e).length).map(e=>{ const 세=찬(e), m=세.length, 지rm=rm(세), 지볼=볼(세);
    const 과=남.map(([k,r])=>[k,r.종목.filter(x=>x.이름===e.이름).flatMap(찬)]).filter(([,l])=>l.length), 주=과.filter(([k])=>한주(k));
    const 플글=e.플랜id?플랜미리(e,!!저장키값):"";
    return `<div class="결과종목"><b class="결과이름">${esc(e.이름)}</b>
      ${향상줄(`1RM ${차kg(지rm)}kg`, 견줌(지rm,최대(주,([,l])=>rm(l))), 견줌(지rm,최대(과,([,l])=>rm(l))))}
      ${향상줄(`볼륨 ${콤마(지볼)}kg`, 견줌(지볼,최대(주,([,l])=>볼(l.slice(0,m)))), 견줌(지볼,최대(과,([,l])=>볼(l.slice(0,m)))))}
      ${플글?`<div class="맞춤 아주작 흐림">${플글}</div>`:""}</div>`; }).join("");
  const 초=Math.max(0,Math.round(rec.초||0)), 세트=완료수(rec);
  return `<div class="화면"><div class="넘김 결과틀">
    <div class="띠 결과띠"><div class="결과띠속" data-enter="결과띠${저장키값||ss.시작}"><b>${esc(결과루틴이름(rec.이름))}</b><span class="알약 ${달성?"달성":"미달성"}">${달성?"달성":"미달성"}</span></div></div>
    <div class="결과수 번호"${번("결0")}>
      <div><b>${올림수(세트,String(세트),"정수")}</b><span>세트</span></div>
      <div><b>${올림수(초,분초(초),"시계")}</b><span>시간</span></div>
      <div><b>${올림수(Math.round(루볼),콤마(루볼))}kg</b><span>볼륨</span></div></div>
    <div class="카드 결과카드">
      <div class="결과위"><div class="줄"><b class="결과이름 한줄">${esc(결과루틴이름(rec.이름))}</b><span class="채움"></span><span class="작 옅음 숫">${날글(날)} (${요일(날)})</span></div>
        ${향상줄(`볼륨 ${콤마(루볼)}kg`, 루주, 루최)}
        <div class="아주작 옅음 숫">${rec.종목.length}종목 · 달성도 ${달성도}%</div></div>
      <div class="결과목록">${종목줄||`<div class="결과종목 작 옅음">체크한 세트가 없습니다</div>`}</div></div></div>
    ${저장키값?`<div class="아랫줄 결과아래"><div class="작 옅음 결과저장글">기록은 저장되었습니다</div><button class="버튼 주" data-act="결과확인">확인</button></div>`
      :`<div class="아랫줄 결과아래"><button class="버튼 주" data-act="운동저장">기록 저장하고 끝내기</button>
        <div class="줄"><button class="버튼 낮 채움" data-act="운동으로">운동으로 돌아가기</button><button class="버튼 낮 채움 나쁨" data-act="운동버림">${ss.버림?"한 번 더 누르면 버립니다":"기록 없이 끝내기"}</button></div></div>`}</div>`; }''')

# 기록 없이 끝내기 (앱 마무리의 셋째 단추) — 시안의 다른 지우기 단추처럼 두 번 눌러야 버린다
바꿈('''    case "운동으로": ss.끝화면=false; ss.끝=null; break;''',
     '''    case "운동으로": ss.끝화면=false; ss.끝=null; ss.버림=false; break;''')
바꿈('''    case "결과확인": S.결과=null; U.탭="캘린더"; break;
''', '''    case "결과확인": S.결과=null; U.탭="캘린더"; break;
    /* 10-03 m66q: 앱 마무리의 '기록 없이 끝내기' — 한 번 누르면 글이 바뀌고, 한 번 더 누르면 버린다 */
    case "운동버림": if(!ss.버림){ ss.버림=true; break; } S.세션=null; U.탭="캘린더"; U.고른날=null; 발자취("기록 없이 끝냄"); setTimeout(()=>토스트("기록 없이 끝냈습니다"),0); break;
''')

# ══ cxi1 — 체험 막대에 [근육 기준] 단추와 칸 (폰 밖) ══
바꿈('''      <button class="작은 표시단추" id="표시단추" type="button" aria-pressed="false">✎ 표시</button>
''', '''      <button class="작은 표시단추" id="표시단추" type="button" aria-pressed="false">✎ 표시</button>
      <button class="작은 기준단추" id="기준단추" type="button" aria-pressed="false" aria-controls="기준칸">근육 기준</button>
''')
바꿈('''    <div class="표시알림" id="표시알림" role="status" hidden></div>
''', '''    <div class="표시알림" id="표시알림" role="status" hidden></div>
    <div class="기준칸" id="기준칸" hidden>
      <div class="기준안내">시안에서만 쓰는 임시 칸 — 넣은 값은 Claude 가 읽어 갑니다</div>
      <div class="기준안내 흐린">넣은 볼륨(kg)이면 가장 빨강 · 비우면 지금 규칙(최대 → 계획)</div>
      <div class="기준목록" id="기준목록"></div>
    </div>
''')

js = r'''
<script>
/* ═══ 10-03 cxi1 '빨개지는 기준' 임시 칸 — 체험 막대(폰 밖)에만. 값은 db 모음 'thresholds' (문서 id = 부위 영문 키) ═══
   문서: {부위, 키, 볼륨(kg|null), 메모, 지금기준, 기준출처, 오늘, 일차, 바꾼때}. Claude 는 ArtifactData 로 읽는다.
   claude.ai 밖(로컬)에서는 브라우저 메모리에만 두고 조용히 넘어간다 */
(()=>{
  const 단추=document.getElementById("기준단추"), 칸=document.getElementById("기준칸"), 목록=document.getElementById("기준목록"), 폰=document.getElementById("폰");
  const 쓰기=n=> (window.claude && typeof window.claude.use==="function") ? window.claude.use(n).catch(()=>null) : Promise.resolve(null);
  const 콤=n=>Math.round(n).toLocaleString("ko-KR");
  let db=null; const 시계={}, 줄={}, 대기={}, 사슬={};
  목록.innerHTML = 기준부위.map(([k,이름])=>`<div class="기준줄" data-k="${k}"><b>${이름}</b>
    <input class="기준수" type="number" inputmode="decimal" min="0" step="10" placeholder="kg" aria-label="${이름} 가장 빨개지는 볼륨(kg)">
    <span class="기준지금"><span></span><span></span></span>
    <input class="기준메모" maxlength="300" placeholder="계산 메모" autocomplete="off" aria-label="${이름} 계산 메모"></div>`).join("");
  목록.querySelectorAll(".기준줄").forEach(el=>{ 줄[el.dataset.k]={수:el.querySelector(".기준수"), 메모:el.querySelector(".기준메모"), 지금:el.querySelector(".기준지금").children}; });

  /* 지금 규칙의 기준 — 잎마다 (이전 최대 → 오늘 계획) 중 가장 큰 잎 · 오늘 한 볼륨도 가장 큰 잎 */
  function 지금기준(k){ const ss=S&&S.세션, 계=ss?잎볼륨(ss.종목,true):{}, 오=ss?잎볼륨(ss.종목):{}; let 값=0, 출처="", 오늘=0;
    for(const l of 잎(k)){ const 최=S.최대볼륨[l]||0, b=최>0?최:(계[l]||0); if(b>값){ 값=b; 출처=최>0?"이전 최대":"오늘 계획"; } 오늘=Math.max(오늘,오[l]||0); }
    return {값, 출처, 오늘}; }
  function 기준글(){ if(칸.hidden||!S) return; for(const [k] of 기준부위){ const x=지금기준(k), t=줄[k].지금;
    /* 윗줄 = 지금 규칙의 기준과 출처(최대 = 이전 최대 · 계획 = 처음이라 오늘 계획), 아랫줄 = 오늘 한 볼륨 */
    t[0].textContent = x.값>0 ? `${콤(x.값)} · ${x.출처==="이전 최대"?"최대":"계획"}` : "기준 없음"; t[1].textContent = x.오늘>0 ? `오늘 ${콤(x.오늘)}` : ""; } }
  function 단추글(){ const n=Object.values(근육기준값).filter(x=>x&&x.볼륨>0).length; 단추.textContent="근육 기준"+(n?` · ${n}`:""); }

  function 올림(k){ if(!db) return; const 이름=기준부위.find(x=>x[0]===k)[1], v=근육기준값[k]||{}, x=지금기준(k);
    const 글={부위:이름, 키:k, 볼륨:v.볼륨>0?v.볼륨:null, 메모:v.메모||"", 지금기준:Math.round(x.값), 기준출처:x.출처||null, 오늘:Math.round(x.오늘), 일차:S.일차, 바꾼때:new Date().toISOString()};
    사슬[k]=(사슬[k]||Promise.resolve()).then(()=>db.collection("thresholds").doc(k).set(글)).catch(()=>{}).finally(()=>{ 대기[k]=false; }); }
  function 바꿈(k, 무엇, 값){ 근육기준값[k]={볼륨:null, 메모:"", ...(근육기준값[k]||{}), [무엇]:값}; 대기[k]=true;
    clearTimeout(시계[k]); 시계[k]=setTimeout(()=>올림(k), 700);   // 치는 동안은 모았다가 멈추면 한 번 저장
    if(무엇==="볼륨"){ 단추글(); 그리기(); } }
  목록.addEventListener("input", e=>{ const r=e.target.closest(".기준줄"); if(!r) return; const k=r.dataset.k;
    if(e.target.classList.contains("기준수")){ const v=parseFloat(e.target.value); 바꿈(k,"볼륨", isFinite(v)&&v>0 ? v : null); }
    else if(e.target.classList.contains("기준메모")) 바꿈(k,"메모", e.target.value); });

  단추.addEventListener("click", ()=>{ const 켬=칸.hidden; 칸.hidden=!켬; 단추.setAttribute("aria-pressed", 켬?"true":"false"); 기준글();
    window.dispatchEvent(new Event("resize")); });   // 막대 높이가 바뀌면 ✎ 표시 판도 폰 자리에 다시 맞춘다
  /* 화면을 다시 그릴 때마다(세트 체크 · 저장 · 날 바꿈) 기준 글을 새로 — 칸이 열려 있을 때만 */
  let 예약=false; new MutationObserver(()=>{ if(칸.hidden||예약) return; 예약=true; requestAnimationFrame(()=>{ 예약=false; 기준글(); }); }).observe(폰,{childList:true});

  쓰기("db").then(d=>{ db=d; if(!db) return;
    db.collection("thresholds").onSnapshot(q=>{ let 다시=false;
      for(const doc of q.docs){ const k=doc.id; if(!줄[k]||대기[k]) continue; const x=doc.data()||{};
        const 볼=typeof x.볼륨==="number"&&x.볼륨>0?x.볼륨:null, 메모=typeof x.메모==="string"?x.메모:"", 앞=근육기준값[k];
        if(앞&&앞.볼륨===볼&&앞.메모===메모) continue;
        근육기준값[k]={볼륨:볼, 메모}; 다시=true;
        if(document.activeElement!==줄[k].수) 줄[k].수.value = 볼??""; if(document.activeElement!==줄[k].메모) 줄[k].메모.value = 메모; }
      if(다시){ 단추글(); 그리기(); } }, ()=>{}); });
})();
</script>
'''
끝 = s.rfind('</script>') + len('</script>'); s = s[:끝] + '\n' + js + s[끝:]

css = '''
/* ═══ 10-03 결과 화면 (m66q) · 올림 속도 (nasu) · 근육 기준 칸 (cxi1) ═══ */
/* nasu: 숫자 올라가기 · 게이지 채움을 1.35배 천천히 — 이 값 하나로 (JS 자릿수시간도 여기서 읽는다) */
:root{--올림배수:1.35}
.게이지3 i{transition:width calc(.7s * var(--올림배수)) cubic-bezier(.2,.8,.2,1)}
.스탯칸.들어옴 .더,.체력칸.들어옴 .더{animation-duration:calc(.7s * var(--올림배수))}
/* m66q 결과 화면 — 앱 마무리(): 띠 · 세 숫자 · 카드(위 면2 / 아래 종목 목록만 넘김) · 단추. 화면 자체는 넘기지 않는다 */
.넘김.결과틀{overflow:hidden;display:flex;flex-direction:column;gap:8px;padding:12px 12px 8px}
.결과띠{justify-content:center;border-radius:8px;flex:none}
.결과띠속{display:flex;align-items:center;gap:8px;min-width:0}
.결과띠속 b{font-size:18px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;min-width:0}
.결과띠 .알약{flex:none}
.결과띠 .알약.미달성{background:var(--면)}
@keyframes 드러남{from{opacity:0;transform:translateX(-6px);clip-path:inset(0 100% 0 0)}to{opacity:1;transform:none;clip-path:inset(0 0 0 0)}}
.결과띠속.들어옴{animation:드러남 .8s cubic-bezier(.22,.61,.36,1) both}
.결과수{display:grid;grid-template-columns:repeat(3,1fr);gap:4px;flex:none}
.결과수>div{display:flex;flex-direction:column;align-items:center;gap:4px;min-width:0}
.결과수 b{font-size:18px;font-weight:700;font-variant-numeric:tabular-nums;white-space:nowrap}
.결과수 span{font-size:11px;color:var(--흐림)}
.결과수 b .올림수{font-size:18px;color:inherit}
.올림수{display:inline-block;text-align:right;font-variant-numeric:tabular-nums}
.결과카드{flex:1;min-height:0;display:flex;flex-direction:column;overflow:hidden;padding:0}
.결과위{background:var(--면2);padding:8px 12px;display:flex;flex-direction:column;gap:4px;border-bottom:1px solid var(--선);flex:none}
.결과이름{font-size:15px;font-weight:700;min-width:0}
.결과목록{flex:1;min-height:0;overflow-y:auto}
.결과종목{padding:8px 12px;display:flex;flex-direction:column;gap:4px;border-bottom:1px solid var(--선)}
.향줄{display:flex;flex-wrap:wrap;align-items:center;gap:4px 8px;min-width:0;font-size:13px;color:var(--흐림);line-height:20px}
.향글{flex:none;white-space:nowrap;font-variant-numeric:tabular-nums}
.칩묶음{display:inline-flex;gap:4px;flex:none}
.대비칩{display:inline-flex;align-items:center;gap:4px;flex:none;padding:0 4px;border:1px solid var(--속선);border-radius:8px;font-size:11px;line-height:18px;color:var(--글);white-space:nowrap}
.대비칩 b{font-weight:700;font-variant-numeric:tabular-nums}
.대비칩 b.오름{color:var(--오름)} .대비칩 b.내림{color:var(--내림)}
.아랫줄.결과아래{flex-direction:column;gap:8px}
.결과아래 .줄{gap:8px}
.결과저장글{text-align:center}
/* cxi1 근육 기준 칸 — 체험 막대(폰 밖) · .표시줄 모양을 본뜬다. 단추가 늘어 제목은 제 줄을 쓴다 */
.체험 .윗{flex-wrap:wrap}
.체험 .윗 b{flex:1 0 100%}
.기준단추[aria-pressed="true"]{background:#fff;border-color:#fff;color:#084B83;font-weight:700}
.기준칸{display:flex;flex-direction:column;gap:4px}
.기준칸[hidden]{display:none}
.기준안내{font-size:11px;font-weight:700}
.기준안내.흐린{font-weight:400;opacity:.8}
.기준목록{display:flex;flex-direction:column;gap:4px;max-height:32vh;overflow-y:auto;margin-top:4px}
.기준줄{display:grid;grid-template-columns:60px 64px 76px minmax(0,1fr);gap:4px;align-items:center}
.기준줄 b{font-size:13px;color:#fff;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.기준수,.기준메모{height:28px;min-width:0;width:100%;border-radius:8px;border:1px solid #ffffff55;background:#ffffff14;color:var(--체험글);padding:0 8px;font-size:13px;font-family:inherit}
.기준수{text-align:right;font-variant-numeric:tabular-nums;-moz-appearance:textfield}
.기준수::-webkit-outer-spin-button,.기준수::-webkit-inner-spin-button{-webkit-appearance:none;margin:0}
.기준수::placeholder,.기준메모::placeholder{color:var(--체험글);opacity:.6}
.기준지금{display:flex;flex-direction:column;font-size:11px;line-height:13px;opacity:.75;white-space:nowrap;overflow:hidden;font-variant-numeric:tabular-nums}
.기준지금 span{overflow:hidden;text-overflow:ellipsis}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]

pathlib.Path(출력).write_text(s, encoding='utf-8')
print("→", 출력, f"{len(s.encode()):,} 바이트")
