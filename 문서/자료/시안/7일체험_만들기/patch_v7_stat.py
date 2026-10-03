"""스탯 · 업적 화면 — 10-03 홍겸 님 ✎ 표시 6개 반영.  python3 patch_v7_stat.py 입력.html 출력.html

표시 → 고친 것
 (열기) 캘린더 띠의 [스탯] [업적] 단추(다른 사람이 만든다)가 data-v 로 보기를 고른다 — 스탯열기 case 가 d.v 를 읽는다(없으면 스탯)
 rm2x 스탯  "위에 칸 없앰"                         → 맨 위 줄(✕ · 스탯 · 업적 · [스탯][업적]) 없앰. 나갈 길 = 아래 탭 줄(탭 case 가 U.스탯=null) · Esc
 wuyc 스탯  "스탯업적은 아래줄로 · 누르면 향상 정보" → [스탯][업적] 칩줄을 탭 줄 바로 위 한 줄로 · 맨 아래 설명 줄(X) 없앰
                                                    스탯 칸을 누르면 그 자리(칩줄 위)에 향상 그래프 — 선 = 1RM, 막대 = 한 번 볼륨, [일][주][달][년]
 gows 스탯  "값 없으면 빈 게이지 + 현재 측정값 없음 · 빨간 줄 = 삭제" → '—' 와 '~ 입력 뒤' 대신 빈 게이지 + '현재 측정값 없음' · '일부' 표시 지움
 jdyt 업적  "달성, 전체, + 셋만 · + 누르면 카테고리" → [달성][전체][+] · + 를 누르면 분류 칩(숨은 · 3대 합계 …)이 한 줄 펼쳐진다 · '풀림' → '달성'
 qat3 업적  "이 박스 삭제"                          → '풀림 7/107 … 업적 6/53 · 숨은 1/54' 줄 없앰 (대표 칭호 안내 줄은 남김 [판단])
 imv8 업적  "띠 두르고 '업적' 7/100 · 숨은 것은 달성해야 전체에" → 맨 위 = 캘린더와 같은 띠 '업적  7/54' (달성 / (숨지 않은 전체 + 달성한 숨은 것))
"""
import sys
src, dst = sys.argv[1], sys.argv[2]
s = open(src, encoding='utf-8').read()
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:80]!r}: {c}번")
    s = s.replace(old, new)

# ══ 1. 화면 셋 — 스탯화면 · 스탯판 · 업적판 ══
바꿈('''/* ── 스탯 · 업적 화면 (캘린더 띠의 작은 칩 → 전체 화면, ✕ 로 닫기) ── */''',
     '''/* ── 스탯 · 업적 화면 (캘린더 띠의 [스탯] [업적] → 전체 화면 · 아래 탭 줄이나 Esc 로 닫기) ── */''')

바꿈('''function 스탯화면(){ const u=U.스탯;
  return `<div class="화면"><div class="스탯머리 번호"${번("스0")}><button class="닫기x" data-act="스탯닫기" aria-label="닫기">✕</button><b class="채움">스탯 · 업적</b>${칩줄("스탯보기","",["스탯","업적"],u.보기)}</div>
    ${u.보기==="스탯"?스탯판():업적판()}${업적띠()}</div>`; }
function 스탯판(){ const {값,풀이}=스탯계산(오늘()), 전=비교기록(), u=U.스탯, 앞=s=>전?.값?.[s];
  const 칸 = s => { const v=값[s.이름], 없=s.상태==="나중"||s.상태==="새 칸";
    return `<button class="스탯칸 ${u.고름===s.이름?"고름":""}" data-act="스탯고름" data-v="${s.이름}" data-enter="스탯${s.이름}">
      <div class="줄"><span class="스탯이름">${s.이름}</span>${s.상태==="일부"?`<span class="아주작 옅음">일부</span>`:""}<span class="채움"></span><b class="스탯값 ${없||v==null?"옅음":""}">${스탯숫(v)}</b></div>
      ${없?`<div class="맞춤 아주작 옅음">${s.이유}</div>`:v==null?`<div class="맞춤 아주작 옅음">기록이 아직 없습니다</div>`:`<div class="줄 막대줄">${스탯막대(v,앞(s.이름))}${스탯차(v,앞(s.이름),전)}</div>`}</button>`; };
  const 체=스탯표.목록[0], v=값.체력, 고른=스탯표.목록.find(s=>s.이름===u.고름)||체, gv=값[고른.이름], gb=앞(고른.이름);
  return `<div class="넘김 스탯속">
    <button class="체력칸 번호 ${u.고름==="체력"?"고름":""}" data-act="스탯고름" data-v="체력" data-enter="스탯체력"${번("스1")}>
      <div class="줄"><b class="큰">체력</b><span class="아주작 옅음">일부</span><span class="채움"></span><b class="스탯큰값 숫"${v!=null?` data-count="${Math.round(v)}"`:""}>${스탯숫(v)}</b>${스탯차(v,앞("체력"),전)}</div>
      ${스탯막대(v,앞("체력"))}<div class="맞춤 아주작 옅음">${체.이유}</div></button>
    <div class="스탯판 번호"${번("스2")}>${스탯표.목록.slice(1).map(칸).join("")}</div></div>
    <div class="스탯풀이 번호"${번("스3")} data-enter="풀이${고른.이름}"><div class="줄"><b>${고른.이름}</b><span class="채움"></span><span class="작 흐림 숫">${전?(gb!=null?`${날글(전.날)} ${스탯숫(gb)} → `:`${날글(전.날)} — → `):"7일 전 기록 없음 · "}지금 ${스탯숫(gv)}</span>${스탯차(gv,gb,전)}</div><div class="맞춤 아주작 흐림">${esc(풀이[고른.이름]||"")}</div></div>`; }
function 업적판(){ const u=U.스탯, 얻=S.업적||{}, 풀=업적표.filter(a=>얻[a.번호]), 대=대표();
  const 분류들=[...new Set(업적표.map(a=>a.분류))];
  let 목록 = u.분류==="전체"?업적표 : u.분류==="풀림"?풀.slice().sort((a,b)=>얻[b.번호].순-얻[a.번호].순) : u.분류==="숨은"?업적표.filter(a=>a.숨김) : 업적표.filter(a=>a.분류===u.분류);
  return `<div class="업적위 번호"${번("업0")}><div class="줄 작"><b class="채움">풀림 ${풀.length}/${업적표.length}</b><span class="흐림 숫">업적 ${풀.filter(a=>!a.숨김).length}/${업적표.filter(a=>!a.숨김).length} · 숨은 ${풀.filter(a=>a.숨김).length}/${업적표.filter(a=>a.숨김).length}</span></div>
      <div class="맞춤 아주작 옅음">${대?`대표 『${esc(대.칭호)}』 · 풀린 칭호를 누르면 바뀝니다`:"풀린 칭호를 누르면 대표 칭호가 됩니다"}</div>
      <div class="가로밀기">${칩줄("업적분류","",["전체","풀림","숨은",...분류들],u.분류,v=>v==="풀림"?`풀림 ${풀.length}`:esc(짧은분류(v)))}</div></div>
    <div class="넘김 업적목록 번호"${번("업1")} data-enter="업적목록${u.분류}">${목록.map(업적줄).join("")||`<div class="빈칸" style="margin-top:12px">아직 없습니다</div>`}</div>`; }''',
'''/* rm2x · wuyc — 맨 위 줄 없음. [스탯][업적] 은 탭 줄 바로 위 한 줄, 그 아래 앱 탭 줄(누르면 닫힌다 · 탭 case 가 U.스탯=null) */
function 스탯화면(){ const u=U.스탯; if(!u.단위) u.단위="일"; if(!u.분류||u.분류==="풀림") u.분류="달성";
  return `<div class="화면">${u.보기==="스탯"?스탯판():업적판()}
    <div class="스탯아래 번호"${번("스0")}>${칩줄("스탯보기","",["스탯","업적"],u.보기)}</div>${업적띠()}</div>${탭줄()}`; }
/* gows — 값이 없으면 '—' · '~ 입력 뒤' 대신 빈 게이지 + '현재 측정값 없음'. 이름 옆 '일부' 는 지움 */
const 빈게이지 = ()=> `<div class="줄 막대줄"><div class="막대"></div><span class="없음글">현재 측정값 없음</span></div>`;
function 스탯판(){ const {값}=스탯계산(오늘()), 전=비교기록(), u=U.스탯, 앞=s=>전?.값?.[s];
  const 칸 = s => { const v=값[s.이름];
    return `<button class="스탯칸 ${u.고름===s.이름?"고름":""}" data-act="스탯고름" data-v="${s.이름}" data-enter="스탯${s.이름}" aria-pressed="${u.고름===s.이름}">
      <div class="줄"><span class="스탯이름">${s.이름}</span><span class="채움"></span>${v==null?"":`<b class="스탯값">${스탯숫(v)}</b>`}</div>
      ${v==null?빈게이지():`<div class="줄 막대줄">${스탯막대(v,앞(s.이름))}${스탯차(v,앞(s.이름),전)}</div>`}</button>`; };
  const 체=스탯표.목록[0], v=값.체력;
  return `<div class="넘김 스탯속">
    <button class="체력칸 번호 ${u.고름==="체력"?"고름":""}" data-act="스탯고름" data-v="체력" data-enter="스탯체력" aria-pressed="${u.고름==="체력"}"${번("스1")}>
      <div class="줄"><b class="큰">체력</b><span class="채움"></span>${v==null?"":`<b class="스탯큰값 숫" data-count="${Math.round(v)}">${스탯숫(v)}</b>${스탯차(v,앞("체력"),전)}`}</div>
      ${v==null?빈게이지():스탯막대(v,앞("체력"))}<div class="맞춤 아주작 옅음">${체.이유}</div></button>
    <div class="스탯판 번호"${번("스2")}>${스탯표.목록.slice(1).map(칸).join("")}</div></div>
    ${u.고름?스탯그래프(u.고름):""}`; }

/* ── wuyc 스탯 향상 그래프 — 누른 스탯 · [일][주][달][년] · 선 = 1RM · 막대 = 한 번 볼륨 ──
   축은 자동이 아니라 고정: 맨 위 = '3대 합 500kg 정도' 수행능력 (그 스탯에 쓰는 종목의 그 수준 1RM 합).
   막대 맨 위 = 그 수준 사람의 한 번 볼륨(1RM × 24 = 75% × 4세트 × 8회). 막대는 같은 칸 점보다 15px 아래에서 잘린다.
   1RM 과 무관한 스탯은 선 = 그 스탯 점수(0~100, 스탯기록), 막대 = 한 번 전체 볼륨(맨 위 10,000kg) [Claude 제안] */
const 그래프표 = {
  삼대500:{"벤치프레스":120, "백 스쿼트":170, "데드리프트":210, "오버헤드 프레스":75, "펜들레이 로우":110},   // 벤 · 스 · 데 = 500kg
  종목:{"근력":["벤치프레스","백 스쿼트","데드리프트"], "내 수행능력":["벤치프레스","백 스쿼트","데드리프트"],
        "밀기":["벤치프레스","오버헤드 프레스"], "당기기":["펜들레이 로우"], "하체":["백 스쿼트","데드리프트"]},
  볼륨배:24, 전체볼륨:10000, 점수위:100, 틈:15, 높이:120, 낮은높이:100, 칸수:{일:14, 주:8, 달:6, 년:5},   // 폰 높이 700 아래면 낮은높이 (그래프 아래 칸이 너무 줄지 않게)
};
/* 묶음 칸 — 오늘이 든 칸이 맨 오른쪽. 주 = 월~일 */
function 그래프칸(단위){ const 오=오늘(), n=그래프표.칸수[단위]||14, out=[], 월일=k=>`${+k.slice(5,7)}/${+k.slice(8)}`;
  for(let i=n-1;i>=0;i--){
    if(단위==="주"){ const w=날더하기(주키(오),-7*i); out.push({시:w, 끝:날더하기(w,6), 글:월일(w)}); }
    else if(단위==="달"){ const x=new Date(+오.slice(0,4), +오.slice(5,7)-1-i, 1); out.push({시:키(x), 끝:키(new Date(x.getFullYear(),x.getMonth()+1,0)), 글:`${x.getMonth()+1}월`}); }
    else if(단위==="년"){ const y=+오.slice(0,4)-i; out.push({시:`${y}-01-01`, 끝:`${y}-12-31`, 글:String(y)}); }
    else { const d=날더하기(오,-i); out.push({시:d, 끝:d, 글:월일(d)}); } }
  return out; }
/* 칸마다 — 선: 1RM 스탯은 칸의 마지막 운동 날 기준 최근 90일 최고 1RM 합(스탯과 같은 셈), 점수 스탯은 칸의 마지막 스탯기록.
   막대: 그 칸 운동들의 한 번 평균 볼륨(체크한 세트 · 맨몸은 유효무게). 기록이 없는 칸은 비운다 */
function 그래프값(이름, 단위){ const 종=그래프표.종목[이름], 줄=Object.entries(S.기록).sort(([a],[b])=>키순(a,b));
  const 든다 = e => !종 || 종.includes(정식이름(e));
  const 함 = r => r.종목.some(e=>든다(e) && e.세트.some(s=>s.완료));
  const 볼 = r => r.종목.reduce((a,e)=> 든다(e) ? a+e.세트.reduce((b,s)=>b+(s.완료?유효무게(s.w)*s.r:0),0) : a, 0);
  return 그래프칸(단위).map(c=>{ const 속=줄.filter(([k,r])=>{ const d=날짜만(k); return d>=c.시 && d<=c.끝 && 함(r); });
    const 막 = 속.length ? 속.reduce((a,[,r])=>a+볼(r),0)/속.length : null; let 선=null;
    if(종){ if(속.length){ const 기=날짜만(속[속.length-1][0]), 창=줄.filter(([k])=>{ const d=날짜만(k); return d<=기 && 날차(d,기)<스탯표.최근RM일; });
        선=종.reduce((a,t)=>a+최고RM(창,t),0); if(!(선>0)) 선=null; } }
    else { const 날들=Object.keys(S.스탯기록||{}).filter(d=>d>=c.시 && d<=c.끝).sort();
      for(let i=날들.length-1;i>=0;i--){ const v=S.스탯기록[날들[i]]?.[이름]; if(v!=null){ 선=v; break; } } }
    return {...c, 선, 막}; }); }
function 그래프차(l, 글){ if(l.length<2) return ""; const d=l[l.length-1]-l[l.length-2]; if(Math.abs(d)<0.5) return "";
  return ` <span class="차 ${d>0?"오름":"내림"}">${d>0?"▲ +":"▼ −"}${글(Math.abs(d))}</span>`; }
function 스탯그래프(이름){ const u=U.스탯, 종=그래프표.종목[이름], 값들=그래프값(이름,u.단위), T=그래프표;
  const 폰=document.getElementById("폰"), W=Math.max(200,(폰?.clientWidth||360)-24), H=(폰?.clientHeight||800)<700?T.낮은높이:T.높이, y0=16, yB=H-16, pH=yB-y0, n=값들.length, sw=W/n;
  const 선위=종?종.reduce((a,t)=>a+T.삼대500[t],0):T.점수위, 막위=종?선위*T.볼륨배:T.전체볼륨;
  const X=i=>sw*(i+.5), Y=v=>yB-pH*자름(v/선위,0,1), f=x=>x.toFixed(1), 빈=!값들.some(c=>c.선!=null);
  const 막폭=Math.max(4,Math.min(16,sw*.5)), 걸음=Math.ceil(n/5), 점=[]; let 막="", 아래="";
  값들.forEach((c,i)=>{ const py=c.선!=null?Y(c.선):null;
    if(!빈 && c.막>0){ let 위=yB-(pH-T.틈)*자름(c.막/막위,0,1); if(py!=null) 위=Math.max(위,py+T.틈);   // 점보다 15px 아래까지만
      if(yB-위>0.5) 막+=`<rect class="막" x="${f(X(i)-막폭/2)}" y="${f(위)}" width="${f(막폭)}" height="${f(yB-위)}"/>`; }
    if(py!=null) 점.push([X(i),py,c.선]);
    if((n-1-i)%걸음===0) 아래+=`<text x="${f(X(i))}" y="${H-2}" text-anchor="middle">${c.글}</text>`; });
  const 끝=점[점.length-1], 끝y=끝?(끝[1]-8<y0+12?끝[1]+16:끝[1]-8):0;   // 맨 위에 붙은 점은 값 글을 아래에
  const 그림 = `<svg class="그래프판" viewBox="0 0 ${W} ${H}" height="${H}" role="img" aria-label="${이름} ${u.단위} 단위 그래프">
    <line class="기준" x1="0" x2="${W}" y1="${y0}" y2="${y0}"/><line class="바닥" x1="0" x2="${W}" y1="${yB}" y2="${yB}"/>
    <text x="0" y="${y0-5}">${종?`3대 500 수준 · ${선위}kg`:`점수 ${선위}`}</text><text x="${W}" y="${y0-5}" text-anchor="end">볼륨 ${콤마(막위)}kg</text>
    ${막}${점.length>1?`<polyline class="선" points="${점.map(([x,y])=>`${f(x)},${f(y)}`).join(" ")}"/>`:""}
    ${점.map(([x,y],j)=>j===점.length-1?`<circle class="끝점" cx="${f(x)}" cy="${f(y)}" r="4"/>`:`<circle class="점" cx="${f(x)}" cy="${f(y)}" r="3"/>`).join("")}
    ${끝?`<text class="값글" x="${f(끝[0])}" y="${f(끝y)}" text-anchor="${끝[0]>W-24?"end":"middle"}">${Math.round(끝[2])}</text>`:""}
    ${빈?`<text class="빈글" x="${f(W/2)}" y="${f((y0+yB)/2+4)}" text-anchor="middle">현재 측정값 없음</text>`:""}${아래}</svg>`;
  const 선들=점.map(p=>p[2]), 막들=값들.filter(c=>c.막>0).map(c=>c.막);
  const 범선=`<svg class="범" width="16" height="8" viewBox="0 0 16 8" aria-hidden="true"><line class="선" x1="0" y1="4" x2="16" y2="4"/><circle class="점" cx="8" cy="4" r="3"/></svg>`;
  const 범막=`<svg class="범" width="8" height="8" viewBox="0 0 8 8" aria-hidden="true"><rect class="막" width="8" height="8"/></svg>`;
  const 글 = 빈 ? "" : `<div class="맞춤 아주작 흐림 그래프글">${범선} ${종?(종.length>1?"1RM 합":"1RM"):"점수"} <b class="숫">${종?kg(반01(선들[선들.length-1]))+"kg":Math.round(선들[선들.length-1])}</b>${그래프차(선들,x=>종?kg(반01(x)):String(Math.round(x)))}
    · ${범막} 한 번 볼륨 <b class="숫">${막들.length?콤마(막들[막들.length-1])+"kg":"—"}</b>${그래프차(막들,콤마)}</div>`;
  return `<div class="스탯그래프 그래프 번호"${번("스3")} data-enter="그래프${이름}">
    <div class="줄"><b class="그래프이름">${이름}</b><span class="채움"></span>${칩줄("스탯단위","",["일","주","달","년"],u.단위)}<button class="닫기x" data-act="스탯고름" data-v="" aria-label="그래프 닫기">✕</button></div>
    ${그림}${글}</div>`; }

/* jdyt · qat3 · imv8 — 맨 위 = 띠 '업적  달성/전체'. 숨은 업적은 달성해야 전체 수에 들어간다 (100개 · 숨은 10개 · 1개 달성 = 1/100, 숨은 것 달성 = 2/101)
   요약 줄 없앰. 분류 칩은 [달성][전체][+] — + 를 누르면 분류(숨은 · 3대 합계 …)가 한 줄 펼쳐진다 */
function 업적판(){ const u=U.스탯, 얻=S.업적||{}, 풀=업적표.filter(a=>얻[a.번호]), 대=대표();
  const 분류들=["숨은",...new Set(업적표.map(a=>a.분류))], 분모=업적표.filter(a=>!a.숨김).length+풀.filter(a=>a.숨김).length, 갈래=u.분류!=="달성"&&u.분류!=="전체";
  let 목록 = u.분류==="전체"?업적표 : u.분류==="달성"?풀.slice().sort((a,b)=>얻[b.번호].순-얻[a.번호].순) : u.분류==="숨은"?업적표.filter(a=>a.숨김) : 업적표.filter(a=>a.분류===u.분류);
  const 칩 = v => `<button class="칩 ${u.분류===v?"켬":""}"${u.분류===v?` data-enter="칩업적분류${v}"`:""} data-act="업적분류" data-v="${v}">${v}</button>`;
  return `<div class="띠 번호"${번("업띠")}><b class="채움">업적</b><b class="숫">${풀.length}/${분모}</b></div>
    <div class="업적위 번호"${번("업0")}><div class="맞춤 아주작 옅음">${대?`대표 『${esc(대.칭호)}』 · 달성한 칭호를 누르면 바뀝니다`:"달성한 칭호를 누르면 대표 칭호가 됩니다"}</div>
      <div class="칩줄">${칩("달성")}${칩("전체")}<button class="칩 ${갈래?"켬":""}" data-act="업적분류더" aria-expanded="${!!u.더}" aria-label="분류별로 보기">+</button></div>
      ${u.더?`<div class="가로밀기" data-enter="업적분류줄">${칩줄("업적분류","",분류들,u.분류,v=>esc(짧은분류(v)))}</div>`:""}</div>
    <div class="넘김 업적목록 번호"${번("업1")} data-enter="업적목록${u.분류}">${목록.map(업적줄).join("")||`<div class="빈칸" style="margin-top:12px">아직 없습니다</div>`}</div>`; }''')

# ══ 2. 행동 ══
바꿈('''    case "스탯열기": U.스탯={보기:"스탯", 분류:"전체", 고름:"체력"}; break;
    case "스탯닫기": U.스탯=null; break;
    case "스탯보기": U.스탯.보기=d.v; break;
    case "업적분류": U.스탯.분류=d.v; break;
    case "스탯고름": U.스탯.고름=d.v; break;''',
'''    case "스탯열기": U.스탯={보기:d.v==="업적"?"업적":"스탯", 분류:"달성", 더:false, 고름:null, 단위:"일"}; break;   // 캘린더 띠 [스탯] · [업적]
    case "스탯닫기": U.스탯=null; break;
    case "스탯보기": U.스탯.보기=d.v; break;
    case "업적분류": U.스탯.분류=d.v; break;
    case "업적분류더": U.스탯.더=!U.스탯.더; break;
    case "스탯단위": U.스탯.단위=d.v; break;
    /* 같은 칸을 다시 누르거나 ✕ 면 그래프를 닫는다. 그래프가 열리며 줄어든 칸 안에서 누른 칸이 가려지지 않게 */
    case "스탯고름": U.스탯.고름 = (!d.v || U.스탯.고름===d.v) ? null : d.v;
      if(U.스탯.고름) setTimeout(()=>{ const 넘=document.querySelector("#폰 .스탯속"), el=넘?.querySelector(".고름"); if(!el) return;
        const a=넘.getBoundingClientRect(), b=el.getBoundingClientRect(); if(b.bottom>a.bottom) 넘.scrollTop+=b.bottom-a.bottom+8; else if(b.top<a.top) 넘.scrollTop-=a.top-b.top+8; },0);
      break;''')
바꿈('''    case "업적띠보기": U.스탯={보기:"업적", 분류:"풀림", 고름:"체력"}; U.업적띠=null; U.시트=null; break;''',
     '''    case "업적띠보기": U.스탯={보기:"업적", 분류:"달성", 더:false, 고름:null, 단위:"일"}; U.업적띠=null; U.시트=null; break;''')

# ══ 3. CSS ══
바꿈('''.스탯머리{display:flex;align-items:center;gap:8px;padding:8px 12px;background:var(--면);border-bottom:1px solid var(--선);flex:none}
.스탯머리 b{font-size:18px;min-width:0;white-space:nowrap}
''', '')
바꿈('''.스탯풀이{flex:none;display:flex;flex-direction:column;gap:4px;padding:4px 12px 8px;background:var(--면);border-top:1px solid var(--선)}
''', '')
css = '''
/* ═══ 10-03 스탯 · 업적 ✎ 표시 (rm2x · wuyc · gows · jdyt · qat3 · imv8) ═══ */
/* [스탯][업적] — 탭 줄 바로 위 한 줄. 반씩 나눠 엄지가 덜 움직이게 */
.스탯아래{flex:none;padding:8px 12px;background:var(--면);border-top:1px solid var(--선)}
.스탯아래 .칩줄{flex-wrap:nowrap}
.스탯아래 .칩{flex:1;justify-content:center}
/* gows 빈 게이지 + '현재 측정값 없음' */
.없음글{font-size:11px;color:var(--옅음);white-space:nowrap;flex:none}
/* wuyc 향상 그래프 — 칩줄 위에 붙박이(위 칸만 스크롤) · 선 = 강조, 막대 = 속선 */
.스탯그래프{flex:none;display:flex;flex-direction:column;gap:8px;padding:8px 12px;background:var(--면);border-top:1px solid var(--선)}
.스탯그래프 .칩줄{flex-wrap:nowrap;flex:none}
.그래프이름{font-size:15px;min-width:0;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.그래프판{display:block;width:100%;overflow:visible}
.그래프 .기준{stroke:var(--선);stroke-width:1;stroke-dasharray:4 4}
.그래프 .바닥{stroke:var(--선);stroke-width:1}
.그래프 .막{fill:var(--속선)}
.그래프 .선{fill:none;stroke:var(--강조);stroke-width:2;stroke-linejoin:round;stroke-linecap:round}
.그래프 .점{fill:var(--강조)}
.그래프 .끝점{fill:var(--면);stroke:var(--강조);stroke-width:2}
.그래프판 text{font-size:11px;fill:var(--옅음);font-family:var(--글꼴)}
.그래프판 .값글{fill:var(--강조);font-weight:700}
.그래프판 .빈글{fill:var(--옅음)}
.그래프글 .범{display:inline-block;vertical-align:middle}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]

open(dst, 'w', encoding='utf-8').write(s)
print(f"{src} → {dst}", f"{len(s.encode()):,} 바이트")
