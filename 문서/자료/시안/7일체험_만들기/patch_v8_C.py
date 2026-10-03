"""7일 체험 시안 v8 C — 캘린더 ✎ 표시 3개 + 근육 '빨개지는 기준' 공식 (10-03 홍겸 님)
   python3 patch_v8_C.py 입력.html 출력.html

표시 → 고친 것
 vj5s 캘린더 "날짜누르면 오른쪽에 순간 스크롤생기면서 가운데화면이 밀림."
      → 날짜 판이 6px 아래에서 올라오며(.들어옴) 0.2초쯤 넘쳐 스크롤바(15px)가 생겼다 사라졌다 — 그동안 달력이 9px 밀렸다.
        폰 안 스크롤 칸은 스크롤바를 그리지 않는다(폰 앱처럼). 손가락 · 바퀴 스크롤은 그대로. 폰 안 모든 칸에 공통
 avk4 캘린더 "아래박스 줄간격 50% 줄여. 표시되는종목 왼쪽 오른쪽으로 나눠서 최대 9개 · 10번째 칸에 외 N종목 펼치기 (1줄로 바꿀수도 있음)"
      → 날짜 판 종목 목록 = 두 칸 격자(왼쪽 칸 위→아래, 다음 오른쪽 칸). 줄 위아래 여백 8 → 4.
        10개까지는 다 보이고, 넘으면 9개 + 10번째 칸 '외 N종목 ∨' (누르면 다 펼치고 '접기 ∧').
        칸 하나 = 번호 · 이름(한 줄, 말줄임) / 다음 줄 흐린 '5세트 · 60kg × 9회'(11). 기록한 날 목록도 같은 모양.
        '1줄로 바꿀 수도' → 상수 날판열 = 2 하나만 1 로 바꾸면 한 줄 목록
 s48f 캘린더 "일요일 숫자 빨간색으로 표시하랬지. 토요일은 파란색 글씨로 표시해."
      → 일요일 날짜 숫자 · 머리 '일' = --나쁨(빨강), 토요일 · '토' = --강조(파랑). 오늘(강조 바탕) · 끌어 놓는 칸(강조 바탕)은 그대로 강조글
 cxi1 후속 근육 기준 — 12부위 칸에 적힌 계산 메모 "○○운동 단일종목 중 최대 세트볼륨 X10 X휴식력" (하체 5부위는 '하체운동')
      → 기준(20단계) = (그 부위를 주동 P 로 쓰는 종목 — 하체 5부위는 하체 종목 전체 — 의 한 세트 무게×횟수 중 가장 큰 값) × 10 × 휴식력
        부위 칸에 숫자가 있으면 그 값이 우선. 공식 값이 없으면(주동 종목 기록이 없으면) 지금 규칙(이전 최대 → 오늘 계획)
        휴식력은 뜻이 확실치 않아 기본 1.0 — 근육 기준 칸 맨 위 숫자 칸에서 바꾼다 (db thresholds 문서 _settings)
"""
import sys, pathlib
들, 날 = pathlib.Path(sys.argv[1]), pathlib.Path(sys.argv[2])
s = 들.read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:80]!r}: {c}번")
    s = s.replace(old, new)

# ══ s48f — 일요일 빨강 · 토요일 파랑 (날짜 숫자 · 요일 머리) ══
바꿈('''function 캘린더(){
  const 오=오늘(), 달=U.보는달||오.slice(0,7), [y,m]=달.split("-").map(Number);''',
'''/* s48f 요일 → 칸 이름. 0 = 일요일(빨강) · 6 = 토요일(파랑) — 색은 CSS 에서 (--나쁨 · --강조) */
const 주말글 = i => i===0 ? "일요" : i===6 ? "토요" : "";
function 캘린더(){
  const 오=오늘(), 달=U.보는달||오.slice(0,7), [y,m]=달.split("-").map(Number);''')
바꿈('''    칸+=`<button class="칸날 번호 ${k===오?"오늘":""}''',
     '''    칸+=`<button class="칸날 번호 ${주말글((앞+d-1)%7)} ${k===오?"오늘":""}''')
바꿈('''${[..."일월화수목금토"].map(w=>`<div class="요일">${w}</div>`).join("")}''',
     '''${[..."일월화수목금토"].map((w,i)=>`<div class="요일 ${주말글(i)}">${w}</div>`).join("")}''')

# ══ avk4 — 날짜 판 종목 목록: 두 칸 격자 · 9개 + '외 N종목' ══
바꿈('''/* 종목 줄이 6개 이상이면 4줄 + '외 n종목' (10-03) — 5개까지는 접어도 높이가 같아 그냥 다 보인다 */
const 접는수 = 4;
function 접는목록(줄들, 키){ const n=줄들.length; if(n<=접는수+1) return 줄들.join("");
  const 펼=U.예펼침===키;
  return (펼?줄들:줄들.slice(0,접는수)).join("")
    + `<button class="예접기" data-act="예펼침" data-v="${키}" aria-expanded="${펼}"><span class="번"></span><span class="채움">${펼?"접기":`외 ${n-접는수}종목`}</span><span class="접힘표 ${펼?"펼":""}">${아이콘.아래}</span></button>`; }''',
'''/* avk4 날짜 판 종목 목록 — 홍겸 님 "왼쪽 오른쪽으로 나눠서 최대 9개 · 마지막 10번째 칸에 외 N종목 펼치기 (1줄로 바꿀 수도 있음)"
   날판열 = 칸 수 (1 로 바꾸면 한 줄 목록). 날판칸 = 접었을 때 칸 수 — 종목이 이보다 많으면 (날판칸-1)개 + '외 N종목'.
   10개까지는 접어도 높이가 같아 그냥 다 보인다. 칸은 왼쪽 칸을 위→아래로 채운 뒤 오른쪽 칸 (왼쪽 · 오른쪽으로 나눔) */
const 날판열 = 2, 날판칸 = 10;
function 접는목록(줄들, 키){ const n=줄들.length, 접=n>날판칸, 펼=접&&U.예펼침===키;
  const 칸들 = 접&&!펼 ? 줄들.slice(0,날판칸-1) : 줄들.slice();
  if(접) 칸들.push(`<button class="예접기" data-act="예펼침" data-v="${키}" aria-expanded="${펼}"><span class="번"></span><span class="채움">${펼?"접기":`외 ${n-(날판칸-1)}종목`}</span><span class="접힘표 ${펼?"펼":""}">${아이콘.아래}</span></button>`);
  return 칸들.join(""); }
/* 목록 틀 — 행 수를 넘겨 줘야 왼쪽 칸부터 위→아래로 채운다 (grid-auto-flow:column) */
const 날판틀 = (줄들, 키) => { const n=줄들.length, 칸수 = n>날판칸 ? (U.예펼침===키 ? n+1 : 날판칸) : n;
  return `<div class="예목록 날판목록" style="--열:${날판열};--행:${Math.ceil(칸수/날판열)}">${접는목록(줄들, 키)}</div>`; };
/* 칸 하나 = 번호 · 이름(한 줄, 말줄임 — 이름이니까) / 다음 줄 흐린 세트 글. 세트 글은 자르지 않고 '·' 에서만 줄을 바꾼다 */
const 날판줄 = (i, 이름, 곁들) => `<div class="예줄"><span class="번">${i+1}</span><span class="예글"><span class="예이름칸 한줄">${esc(이름)}</span><span class="숫 흐림">${곁들.filter(Boolean).map(x=>`<span>${x}</span>`).join(" · ")}</span></span></div>`;''')
바꿈('''  const 줄=실.종목.map((e,i)=>`<div class="예줄"><span class="번">${i+1}</span><span class="채움 한줄">${esc(e.이름)}</span><span class="숫 흐림">${e.세트.length}세트 · ${세트글(e.세트)}</span></div>`);
  return `<div class="예목록">${접는목록(줄, 키)}</div>`; }''',
'''  const 줄=실.종목.map((e,i)=>날판줄(i, e.이름, [`${e.세트.length}세트`, 세트글(e.세트)]));
  return 날판틀(줄, 키); }''')
바꿈('''    return `<div class="예줄"><span class="번">${i+1}</span><span class="채움 한줄">${esc(e.이름)}</span><span class="숫 흐림">${세.length}/${e.세트.length}세트${세.length?" · "+세트글(세):""}</span></div>`; });
  return 줄.length?`<div class="예목록">${접는목록(줄, 키)}</div>`:""; }''',
'''    return 날판줄(i, e.이름, [`${세.length}/${e.세트.length}세트`, 세.length?세트글(세):""]); });
  return 줄.length?날판틀(줄, 키):""; }''')

# ══ cxi1 후속 — 근육 기준 = 주동 종목 최대 세트 볼륨 × 10 × 휴식력 ══
바꿈('''/* 오늘 오른 단계 = 20 × 오늘 볼륨 ÷ 기준 — 기준은 (넣은 값) → 이전 최대 → (처음이면) 오늘 계획. 20 에서 멈춘다 */
function 오늘단계(종목들){ const v=잎볼륨(종목들), 계=잎볼륨(종목들,true), out={};
  for(const [l,x] of Object.entries(v)){ const 정=정한기준(l), 기=정>0?정:S.최대볼륨[l]>0?S.최대볼륨[l]:(계[l]||x); out[l]=Math.min(20,20*x/기); } return out; }''',
'''/* 10-03 cxi1 후속 — 홍겸 님이 12부위 칸 모두에 적은 계산 메모 "○○운동 단일종목 중 최대 세트볼륨 X10 X휴식력" (하체 5부위는 '하체운동')
   공식 기준 = (그 부위를 주동(P)으로 쓰는 종목들 — 하체 5부위는 하체 종목 전체 — 의 한 세트 무게×횟수 중 가장 큰 값) × 10 × 휴식력
   · 한 세트 = 지난 기록 전부(체크한 세트) + 오늘 종목(계획 · 한 세트 모두) [해석]. 무게는 잎볼륨과 같은 유효무게(맨몸 = 체중 60%)
   · 하체 종목 = 종목 칸이 '하체' 이거나 주동 근육이 하체 5부위 중 하나 (맨몸 스쿼트 · 런지 · 카프 레이즈도 들어간다)
   · 휴식력은 뜻이 아직 확실치 않다 — 기본 1.0, 근육 기준 칸 맨 위에서 바꾼다 */
const 하체부위 = ["glutes","quads","hamstrings","adductors","calves"];
const 근육기준설정 = {휴식력:1};
function 주동부위(이름){ const 부=new Set(), 표=S.종목표.find(x=>x.이름===이름);
  for(const [id,역] of Object.entries(종목근육(이름))) if(역==="P") for(const l of 잎(id)){ const k=잎부위(l); if(k) 부.add(k); }
  if((표&&표.칸==="하체") || 하체부위.some(k=>부.has(k))) 하체부위.forEach(k=>부.add(k));
  return 부; }
/* 부위 키 → {값: 가장 큰 한 세트 볼륨, 종목, w, r, 날} */
function 부위최대세트(종목들){ const 최={}, 캐={};
  const 봄=(e,세트들,날)=>{ const 부=캐[e.이름]||(캐[e.이름]=주동부위(e.이름)); if(!부.size) return;
    for(const st of 세트들){ const v=유효무게(+st.w||0)*(+st.r||0); if(!(v>0)) continue;
      for(const k of 부) if(!최[k]||v>최[k].값) 최[k]={값:v, 종목:e.이름, w:+st.w||0, r:+st.r||0, 날}; } };
  for(const [rk,r] of Object.entries(S.기록||{})) for(const e of r.종목||[]) 봄(e, (e.세트||[]).filter(st=>st.완료), rk.split("~")[0]);
  for(const e of 종목들||[]) 봄(e, e.세트||[], "오늘");
  return 최; }
const 공식기준 = (k, 최) => 최[k] ? 최[k].값*10*근육기준설정.휴식력 : 0;
/* 오늘 오른 단계 = 20 × 오늘 볼륨 ÷ 기준 — 기준은 (부위 칸에 넣은 값) → 공식 → (공식이 없으면) 이전 최대 → 오늘 계획. 20 에서 멈춘다 */
function 오늘단계(종목들){ const v=잎볼륨(종목들), 계=잎볼륨(종목들,true), 최=부위최대세트(종목들), out={};
  for(const [l,x] of Object.entries(v)){ const 정=정한기준(l), k=잎부위(l), 공=k?공식기준(k,최):0;
    const 기=정>0?정 : 공>0?공 : S.최대볼륨[l]>0?S.최대볼륨[l] : (계[l]||x); out[l]=Math.min(20,20*x/기); } return out; }''')

# ── 체험 막대(폰 밖) 근육 기준 칸: 휴식력 칸 · 공식 글 ──
바꿈('''      <div class="기준안내 흐린">넣은 볼륨(kg)이면 가장 빨강 · 비우면 지금 규칙(최대 → 계획)</div>
      <div class="기준목록" id="기준목록"></div>''',
'''      <div class="기준안내 흐린">부위 칸을 비우면 공식 = 주동 종목 최대 세트 볼륨 × 10 × 휴식력 (하체 5부위는 하체 종목 전체) · 숫자를 넣으면 그 값</div>
      <label class="기준설정"><b>휴식력</b><input class="기준수" id="휴식력칸" type="number" inputmode="decimal" min="0" step="0.1" placeholder="1.0" aria-label="휴식력 — 기준에 곱하는 수"><span class="기준설명">기준에 곱하는 수 · 비우면 1.0</span></label>
      <div class="기준목록" id="기준목록"></div>''')
# 한 줄 = 이름 · 숫자 · 메모, 다음 줄 = 흐린 공식 글 (공식 글이 길어 76px 칸에 안 들어간다)
바꿈('''    <input class="기준수" type="number" inputmode="decimal" min="0" step="10" placeholder="kg" aria-label="${이름} 가장 빨개지는 볼륨(kg)">
    <span class="기준지금"><span></span><span></span></span>
    <input class="기준메모" maxlength="300" placeholder="계산 메모" autocomplete="off" aria-label="${이름} 계산 메모"></div>`).join("");''',
'''    <input class="기준수" type="number" inputmode="decimal" min="0" step="10" placeholder="kg" aria-label="${이름} 가장 빨개지는 볼륨(kg)">
    <input class="기준메모" maxlength="300" placeholder="계산 메모" autocomplete="off" aria-label="${이름} 계산 메모">
    <span class="기준지금"><span></span><span></span></span></div>`).join("");''')
바꿈('''  /* 지금 규칙의 기준 — 잎마다 (이전 최대 → 오늘 계획) 중 가장 큰 잎 · 오늘 한 볼륨도 가장 큰 잎 */
  function 지금기준(k){ const ss=S&&S.세션, 계=ss?잎볼륨(ss.종목,true):{}, 오=ss?잎볼륨(ss.종목):{}; let 값=0, 출처="", 오늘=0;
    for(const l of 잎(k)){ const 최=S.최대볼륨[l]||0, b=최>0?최:(계[l]||0); if(b>값){ 값=b; 출처=최>0?"이전 최대":"오늘 계획"; } 오늘=Math.max(오늘,오[l]||0); }
    return {값, 출처, 오늘}; }
  function 기준글(){ if(칸.hidden||!S) return; for(const [k] of 기준부위){ const x=지금기준(k), t=줄[k].지금;
    /* 윗줄 = 지금 규칙의 기준과 출처(최대 = 이전 최대 · 계획 = 처음이라 오늘 계획), 아랫줄 = 오늘 한 볼륨 */
    t[0].textContent = x.값>0 ? `${콤(x.값)} · ${x.출처==="이전 최대"?"최대":"계획"}` : "기준 없음"; t[1].textContent = x.오늘>0 ? `오늘 ${콤(x.오늘)}` : ""; } }''',
'''  /* 지금 쓰는 기준 — 넣은 값 → 공식(주동 종목 최대 세트 볼륨 × 10 × 휴식력) → 옛 규칙(잎마다 이전 최대 → 오늘 계획 중 가장 큰 잎).
     오늘 = 오늘 한 볼륨이 가장 큰 잎 · 단계 = 그 부위 잎 중 가장 높은 단계 (그림과 같은 값) */
  const 휴글 = h => Number.isInteger(h) ? h.toFixed(1) : String(+h.toFixed(2));
  const 세트말 = x => `${x.종목} ${x.w>0?kg(x.w)+"kg":"맨몸"} × ${x.r}회`;
  function 지금기준(k){ const ss=S&&S.세션, 종=ss?ss.종목:[], 계=ss?잎볼륨(종,true):{}, 오=ss?잎볼륨(종):{}, 단=ss?오늘단계(종):{};
    const 최=부위최대세트(종)[k]||null, 휴=근육기준설정.휴식력, 공=최?최.값*10*휴:0, 넣=근육기준값[k]&&근육기준값[k].볼륨>0?근육기준값[k].볼륨:0;
    let 옛=0, 옛출처="", 오늘=0, 단계=0;
    for(const l of 잎(k)){ const m=S.최대볼륨[l]||0, b=m>0?m:(계[l]||0); if(b>옛){ 옛=b; 옛출처=m>0?"이전 최대":"오늘 계획"; } 오늘=Math.max(오늘,오[l]||0); 단계=Math.max(단계,단[l]||0); }
    return {값:넣||공||옛, 출처:넣?"넣은 값":공?"공식":옛출처, 오늘, 단계, 공, 최, 휴, 옛, 옛출처}; }
  function 기준글(){ if(칸.hidden||!S) return; for(const [k] of 기준부위){ const x=지금기준(k), t=줄[k].지금;
    /* 윗줄 = 공식 (예: 공식 5,400 = 540 × 10 × 1.0 · 벤치프레스 60kg × 9회). 공식 값이 없으면 대신 쓰는 옛 규칙. 넣은 값이 있으면 '넣은 값 우선' */
    const 공글 = x.최 ? `공식 ${콤(x.공)} = ${콤(x.최.값)} × 10 × ${휴글(x.휴)} · ${세트말(x.최)}`
      : `공식 없음(주동 종목 기록 없음)${x.옛>0?` → ${x.옛출처} ${콤(x.옛)}`:""}`;
    t[0].textContent = (x.출처==="넣은 값" ? "넣은 값 우선 · " : "") + 공글; t[0].title = t[0].textContent;
    t[1].textContent = x.오늘>0 ? `오늘 ${콤(x.오늘)} · ${Math.round(x.단계*10)/10}단계` : ""; } }''')
바꿈('''    const 글={부위:이름, 키:k, 볼륨:v.볼륨>0?v.볼륨:null, 메모:v.메모||"", 지금기준:Math.round(x.값), 기준출처:x.출처||null, 오늘:Math.round(x.오늘), 일차:S.일차, 바꾼때:new Date().toISOString()};''',
'''    const 글={부위:이름, 키:k, 볼륨:v.볼륨>0?v.볼륨:null, 메모:v.메모||"", 지금기준:Math.round(x.값), 기준출처:x.출처||null, 공식:Math.round(x.공),
      최대세트:x.최?{값:Math.round(x.최.값), 종목:x.최.종목, 무게:x.최.w, 횟수:x.최.r, 날:x.최.날}:null, 휴식력:x.휴, 오늘:Math.round(x.오늘), 일차:S.일차, 바꾼때:new Date().toISOString()};''')
# 휴식력 칸 — 치는 동안 모았다가 한 번 저장 (문서 id _settings). 비우거나 0 이하면 1.0
바꿈('''  단추.addEventListener("click", ()=>{ const 켬=칸.hidden;''',
'''  const 휴칸=document.getElementById("휴식력칸"); let 휴시계=0, 휴대기=false, 휴사슬=Promise.resolve();
  휴칸.addEventListener("input", ()=>{ const v=parseFloat(휴칸.value); 근육기준설정.휴식력 = isFinite(v)&&v>0 ? v : 1; 휴대기=true;
    clearTimeout(휴시계); 휴시계=setTimeout(()=>{ if(!db){ 휴대기=false; return; }
      const 글={휴식력: isFinite(v)&&v>0 ? v : null, 쓰는값:근육기준설정.휴식력, 설명:"근육 기준 공식의 휴식력 (null = 기본 1.0)", 일차:S.일차, 바꾼때:new Date().toISOString()};
      휴사슬=휴사슬.then(()=>db.collection("thresholds").doc("_settings").set(글)).catch(()=>{}).finally(()=>{ 휴대기=false; }); }, 700);
    기준글(); 그리기(); });
  단추.addEventListener("click", ()=>{ const 켬=칸.hidden;''')
바꿈('''      for(const doc of q.docs){ const k=doc.id; if(!줄[k]||대기[k]) continue; const x=doc.data()||{};''',
'''      for(const doc of q.docs){ const k=doc.id;
        if(k==="_settings"){ if(휴대기) continue; const h=(doc.data()||{}).휴식력, 새=typeof h==="number"&&h>0?h:1;
          if(새!==근육기준설정.휴식력){ 근육기준설정.휴식력=새; 다시=true; } if(document.activeElement!==휴칸) 휴칸.value = typeof h==="number"&&h>0 ? h : ""; continue; }
        if(!줄[k]||대기[k]) continue; const x=doc.data()||{};''')
바꿈('''      if(다시){ 단추글(); 그리기(); } }, ()=>{}); });''',
     '''      if(다시){ 단추글(); 그리기(); 기준글(); } }, ()=>{}); });''')

css = '''
/* ═══ 10-03 v8 C — 캘린더 ✎ 표시 (vj5s · avk4 · s48f) · 근육 기준 공식 (cxi1 후속) ═══ */
/* vj5s 폰 안 스크롤 칸은 스크롤바를 그리지 않는다 — 폰 앱처럼. 날짜를 누르면 날짜 판이 6px 아래에서 올라오며(.들어옴) 잠깐 넘쳐
   15px 스크롤바가 생겼다 사라졌고, 그만큼 달력이 옆으로 밀렸다. 스크롤바가 없으면 칸 폭이 절대 안 바뀐다. 손가락 · 바퀴 스크롤은 그대로 */
.폰 *{scrollbar-width:none}
.폰 *::-webkit-scrollbar{display:none}
/* s48f 일요일 빨강 · 토요일 파랑 — 날짜 숫자와 요일 머리. 오늘(강조 바탕) · 끌어 놓는 칸(강조 바탕)의 숫자는 강조글 그대로 */
.요일.일요,.칸날.일요:not(.오늘):not(.놓기) .일{color:var(--나쁨)}
.요일.토요,.칸날.토요:not(.오늘):not(.놓기) .일{color:var(--강조)}
/* avk4 종목 목록 두 칸 격자 — 왼쪽 칸 위→아래, 다음 오른쪽 칸. 줄 위아래 여백 8 → 4 (줄 간격 절반) */
.예목록.날판목록{display:grid;grid-template-columns:repeat(var(--열),minmax(0,1fr));grid-template-rows:repeat(var(--행),auto);grid-auto-flow:column;column-gap:12px}
.날판목록 .예줄{padding:4px 0;align-items:flex-start;gap:4px}
.날판목록 .예줄 .번{line-height:18px}
.예글{display:flex;flex-direction:column;min-width:0;flex:1}
.예이름칸{font-size:13px;line-height:18px;text-overflow:ellipsis}
.날판목록 .예줄 .숫{flex:none;white-space:normal;line-height:16px}
.날판목록 .예줄 .숫>span{white-space:nowrap}
.날판목록 .예접기{height:auto;min-height:32px;gap:4px}
.날판목록 .예접기 .채움{white-space:nowrap}
/* cxi1 후속 근육 기준 칸 — 한 줄 = 이름 · 숫자 · 메모, 다음 줄 = 흐린 공식 글. 맨 위 휴식력 칸도 같은 칸 나눔 */
.기준줄{grid-template-columns:60px 64px minmax(0,1fr);row-gap:2px}
/* 공식 글은 자르지 않는다 — 길면 다음 줄로 (폰 밖이라 높이 여유가 있다). 오늘 글은 그 뒤에 붙거나 다음 줄 */
.기준줄 .기준지금{grid-column:2/-1;flex-direction:row;flex-wrap:wrap;gap:0 8px;line-height:14px;white-space:normal}
.기준줄 .기준지금 span{white-space:normal;overflow:visible}
.기준줄 .기준지금 span:last-child{white-space:nowrap}
.기준설정{display:grid;grid-template-columns:60px 64px minmax(0,1fr);gap:4px;align-items:center;margin-top:4px}
.기준설정 b{font-size:13px;color:#fff;white-space:nowrap}
.기준설명{font-size:11px;opacity:.75;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;min-width:0}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]
날.write_text(s, encoding='utf-8')
print('✅', 날, len(s))
