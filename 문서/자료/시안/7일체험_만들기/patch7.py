import re, sys, pathlib
SP = pathlib.Path('/tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad')
s = (SP/'7day-live.html').read_text(encoding='utf-8')
기록 = []
def 바꿈(old, new, n=1, 이름=None):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {이름 or old[:60]!r}: {c}번 찾음 (기대 {n})")
    s = s.replace(old, new); 기록.append(이름 or old[:40])

# ───────── 1. UI 지침 — 글자 (U2-1: 11·13·15·18·22·28) ─────────
def css_고침(fn):
    global s
    s = re.sub(r'<style>(.*?)</style>', lambda m: '<style>'+fn(m.group(1))+'</style>', s, flags=re.S)
글자 = {'9':'11','10':'11','12':'13','14':'15','16':'18','20':'22'}
css_고침(lambda c: re.sub(r'font-size:\s*(9|10|12|14|16|20)px', lambda m: f"font-size:{글자[m.group(1)]}px", c))

# ───────── 2. UI 지침 — 모서리 (U3-4: 8 · 16 · 원) · 진행 막대 (U3-8: 높이 8 · 모서리 4) ─────────
def 규칙(sel, prop, old, new):
    global s
    pat = re.compile(r'(' + re.escape(sel) + r'\{[^}]*?' + prop + r':\s*)' + re.escape(old) + r'(?=[;}])')
    s2, c = pat.subn(lambda m: m.group(1)+new, s)
    if c < 1: raise SystemExit(f"❌ {sel} {prop} {old}")
    s = s2; 기록.append(f"{sel} {prop} {old}→{new}")
for sel,old,new in [('.더미 i','1px','50%'),('.번호[data-n]::before','4px','8px'),('.칸날 .루','4px','8px'),
    ('.그림이름','4px','8px'),('.체험 .작은','6px','8px'),('.알약','6px','8px'),('.칸날.오늘 .일','6px','8px'),
    ('.작은사진','6px','8px'),('.게이지','10px','8px'),('.사진추가,.사진칸','10px','8px'),('.회블록','10px','8px'),
    ('.회색칸','10px','8px'),('.아래띠','12px','8px'),('.고정띠','12px','16px'),('.종목상자','12px','16px'),
    ('.빈칸','12px','8px'),('.그림판','12px','8px'),('.타일','12px','8px'),('.폰','22px','16px')]:
    규칙(sel,'border-radius',old,new)
규칙('.가는바','height','4px','8px'); 규칙('.가는바','border-radius','2px','4px')
규칙('.게이지3','height','10px','8px'); 규칙('.게이지3','border-radius','5px','4px')

# ───────── 3. UI 지침 — 누르는 높이 (U3-3: 28·32·40·44) · 아이콘 (U3-6: 16·18) ─────────
바꿈('grid-template-columns:36px 1fr 1fr 62px 36px','grid-template-columns:28px 1fr 1fr 64px 28px', n=s.count('grid-template-columns:36px 1fr 1fr 62px 36px') or 1, 이름='세트 줄 칸 폭')
규칙('.값칸','height','36px','40px'); 규칙('.값칸 button','width','26px','28px')
규칙('.체크','width','32px','28px'); 규칙('.체크','height','32px','28px')
규칙('.체크 svg','width','18px','16px'); 규칙('.체크 svg','height','18px','16px')
규칙('.휴식칸','height','36px','40px')
# U1-1 · U1-2 — 휴식 칸의 #fff · 섞기(blend) 를 없앤다. 채운 곳 = 강조 바탕 · 강조글 (v0.7.0 결정 그대로)
바꿈('.휴식칸 span{position:relative;mix-blend-mode:difference;color:#fff}',
     '.휴식칸 .밑{position:relative;color:var(--강조)}\n.휴식칸 .위{position:absolute;inset:0;background:var(--강조);color:var(--강조글);display:flex;align-items:center;justify-content:center}',
     이름='휴식 칸 흰 글자')

# ───────── 4. 새 운동 화면 CSS ─────────
새css = '''
/* ═══ 운동 중 — 10-02 새 시안: 한 화면에 한 종목 · 근육 그림 붙박이 ═══
   칸마다 높이가 정해져 있어 무엇을 눌러도 다른 칸이 움직이지 않는다 (11 UI지침 U5-6 · U5-7) */
.운전체{flex:none;padding:8px 12px;background:var(--면);border-bottom:1px solid var(--선)}
.운막대{height:8px;border-radius:4px;background:var(--면2);overflow:hidden}
.운막대 i{display:block;height:100%;background:var(--강조);border-radius:4px}
.운요약{display:flex;gap:12px;margin-top:8px;font-size:11px;color:var(--흐림);font-variant-numeric:tabular-nums}
.운요약 b{color:var(--글)}
.운띠{flex:none;display:flex;gap:4px;padding:8px 12px;background:var(--면2);border-bottom:1px solid var(--선);overflow-x:auto;scrollbar-width:none}
.운띠::-webkit-scrollbar{display:none}
.운칸{flex:0 0 72px;background:var(--면);border:1px solid var(--선);border-radius:8px;padding:4px 8px 8px;text-align:left;color:var(--글)}
.운칸.지금{border:2px solid var(--강조);padding:3px 7px 7px;background:var(--강조옅음)}
.운칸.끝{opacity:.55}
.운칸 .ㅇ{font-size:11px;font-weight:700;line-height:1.25;height:28px;overflow:hidden;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;word-break:keep-all}
.운칸 .ㅅ{font-size:11px;color:var(--흐림);font-variant-numeric:tabular-nums}
.운칸 .ㅁ{height:8px;border-radius:4px;background:var(--면2);margin-top:4px;overflow:hidden}
.운칸 .ㅁ i{display:block;height:100%;background:var(--강조)}
.운칸.끝 .ㅁ i{background:var(--좋음)}
.운넘{flex:none;display:flex;align-items:center;gap:8px;padding:4px 12px;background:var(--면);border-bottom:1px solid var(--선);font-size:11px}
.운넘 button{flex:1 1 0;min-width:0;height:32px;border-radius:8px;color:var(--흐림);overflow:hidden;text-overflow:ellipsis;white-space:nowrap;padding:0 4px}
.운넘 button:disabled{opacity:.35}
.운넘 .ㄴ{text-align:left}.운넘 .ㄷ{text-align:right}
.운넘 .ㅈ{flex:none;font-weight:700;font-variant-numeric:tabular-nums}
.운머리{flex:none;display:flex;gap:8px;align-items:flex-start;padding:8px 12px;background:var(--면);border-bottom:1px solid var(--선)}
.운글{flex:1;min-width:0}
.운이름{font-size:18px;font-weight:700;line-height:1.3;word-break:keep-all}
.운곁{font-size:11px;color:var(--흐림);margin-top:4px}
.운근육{display:flex;gap:4px;flex-wrap:nowrap;overflow:hidden;margin-top:8px}
.운근육 span{flex:none;height:28px;padding:0 8px;border:1px solid var(--속선);border-radius:8px;font-size:11px;color:var(--흐림);display:inline-flex;align-items:center;gap:4px}
.운근육 span.주{color:var(--글);border-color:var(--글);font-weight:700}
.운근육 i{width:8px;height:8px;border-radius:50%;flex:none}
.운지표{display:flex;gap:8px;flex-wrap:nowrap;white-space:nowrap;overflow:hidden;align-items:center;margin-top:8px;font-size:11px;color:var(--흐림);font-variant-numeric:tabular-nums}
.운지표 b{font-size:15px;color:var(--글)}
.운칩{height:28px;padding:0 8px;border-radius:8px;border:1px solid var(--속선);font-size:11px;display:inline-flex;align-items:center}
.운칩.오름{color:var(--나쁨);border-color:var(--나쁨)}
.운칩.내림{color:var(--강조);border-color:var(--강조)}
.운칩.빈{color:var(--옅음)}
.운몸{flex:0 0 88px;background:var(--면2);border:1px solid var(--선);border-radius:8px;padding:4px;display:flex;flex-direction:column;align-items:center;gap:4px}
.운몸속{width:80px;height:80px;display:flex;align-items:center;justify-content:center;overflow:hidden;border-radius:8px}
.운몸속 svg{width:100%;height:100%;display:block}
.운몸속 img{width:100%;height:100%;object-fit:cover;display:block}
.운몸 .글{font-size:11px;color:var(--옅음)}
.운세트들{flex:1;min-height:0;overflow-y:auto;padding:8px 12px;display:flex;flex-direction:column;gap:4px}
.큰몸{display:flex;justify-content:center}
.큰몸 svg{width:100%;height:auto;max-height:280px}
.몸눈금{display:flex;align-items:center;gap:8px;font-size:11px;color:var(--흐림)}
.몸눈금 .바{flex:1;height:8px;border-radius:4px}
.몸표{width:100%;border-collapse:collapse;font-size:13px}
.몸표 td,.몸표 th{padding:8px 0;border-bottom:1px solid var(--선);text-align:left}
.몸표 th{font-size:11px;color:var(--옅음);font-weight:400}
.몸표 .ㄱ{text-align:right;font-variant-numeric:tabular-nums;color:var(--흐림)}
.몸점{width:8px;height:8px;border-radius:50%;display:inline-block;margin-right:8px}
.시트사진{width:88px;height:88px;object-fit:cover;border-radius:8px}
'''
끝 = s.rfind('</style>')
s = s[:끝] + 새css + s[끝:]; 기록.append('새 CSS')

# ───────── 5. 새 운동 화면 (운동화면 · 운세트줄 · 운최고) ─────────
시작 = s.index('function 운동화면(){')
끝i = s.index('/* 운동 중 그림 칸 (08 3절)')
새화면 = r'''function 운동화면(){ const ss=S.세션, n=ss.종목.length;
  /* 10-02 — 한 화면에 한 종목. 보는 종목(U.본)은 손으로만 바뀐다. 체크해도 화면이 저절로 넘어가지 않는다 */
  if(U.본==null||U.본>=n||U.본<0) U.본=Math.min(Math.max(ss.지금.i,0),n-1);
  const 본=U.본, e=ss.종목[본], 완=x=>x.세트.filter(s=>s.완료).length;
  const 전=ss.종목.reduce((a,x)=>a+x.세트.length,0), 끝낸=ss.종목.reduce((a,x)=>a+완(x),0);
  const 끝종=ss.종목.filter(x=>x.세트.length&&완(x)===x.세트.length).length;
  const 볼=(x,다)=>x.세트.reduce((a,s)=>a+((다||s.완료)?유효무게(s.w)*(다?(s.목r||s.r):s.r):0),0);
  const 총볼=ss.종목.reduce((a,x)=>a+볼(x),0), 총목=ss.종목.reduce((a,x)=>a+볼(x,true),0);
  const 이=ss.종목[본-1], 다=ss.종목[본+1];
  const 단=지금단계(ss.종목,모의시각()), 근=종목근육(e.이름);
  const 잎최대=id=>Math.max(0,...잎(id).map(l=>단[l]||0));
  const 근줄=Object.entries(근).sort((a,b)=>"PSY".indexOf(a[1])-"PSY".indexOf(b[1]));
  const 칩=근줄.slice(0,2).map(([id,r])=>
    `<span class="${r==="P"?"주":""}"><i style="background:${단계색(잎최대(id))||"var(--속선)"}"></i>${esc((근육이름[id]||id).replace(/ \(.*\)/,""))}</span>`).join("")
    + (근줄.length>2?`<span>+${근줄.length-2}</span>`:"");   // 칩은 둘까지 + 나머지 수 — 잘려 보이지 않게 (U2-7)
  const 세=e.세트.filter(s=>s.완료), 맨=맨몸(찾표(e.이름));
  const 오=세.length?(맨?Math.max(...세.map(s=>s.r)):Math.max(...세.map(s=>일RM(s.w,s.r)))):null;
  const 지=운최고(e.이름,맨), 단위=맨?"회":"kg";
  const 비=(이름,기)=>{ if(오==null||기==null) return `<span class="운칩 빈">${이름} —</span>`; const d=Math.round((오-기)*2)/2;
    return `<span class="운칩 ${d>0?"오름":d<0?"내림":""}">${이름} ${d===0?"유지":(d>0?"▲ ":"▼ ")+kg(Math.abs(d))+단위}</span>`; };
  const 칩줄=비("1주",지.주)+비("최고",지.최고);
  const 다음i=ss.종목.findIndex((x,i)=>i>본&&완(x)<x.세트.length), 첫i=ss.종목.findIndex(x=>완(x)<x.세트.length);
  const 갈곳=다음i>=0?다음i:(첫i>=0&&첫i!==본?첫i:-1), 지금k=e.세트.findIndex(s=>!s.완료);
  const 모=S.설정.배너, 그림보임=모!=="숨김"&&!ss.배너숨김;
  let 그림="";
  if(그림보임){ const 사=사진목록(e.이름), 사진모=(모==="사진 1장"||모==="사진 2장")&&사.length;
    const 안=사진모?`<img src="${사[0]}" alt="${esc(e.이름)} 사진">`:몸그림(단,{자르기:확대상자(e.이름),키:"운몸"});
    그림=`<button class="운몸" data-act="몸크게" aria-label="근육지도 크게 보기"><span class="운몸속">${안}</span><span class="글">누르면 크게</span></button>`; }
  return `<div class="화면"><div class="머리줄"><button class="버튼 낮" data-act="탭" data-t="캘린더" aria-label="나가기">‹</button><b class="채움 한줄">${esc(ss.이름)}</b>${그림보임?"":`<button class="버튼 낮" data-act="배너보기">그림</button>`}<span class="숫 굵 큰" data-timer="경과"></span></div>
    <div class="운전체"><div class="운막대"><i style="width:${전?끝낸/전*100:0}%"></i></div>
      <div class="운요약"><span>종목 <b>${끝종}/${n}</b></span><span>세트 <b>${끝낸}/${전}</b></span><span>볼륨 <b>${콤마(Math.round(총볼))}</b>/${콤마(Math.round(총목))}kg</span></div></div>
    <div class="운띠 번호"${번("운띠")}>${ss.종목.map((x,i)=>{ const m=x.세트.length, k=완(x), 다끝=m&&k===m;
      return `<button class="운칸 ${i===본?"지금":""} ${다끝&&i!==본?"끝":""}" data-act="본종목" data-i="${i}" aria-label="${esc(x.이름)} ${k}/${m}세트"><div class="ㅇ">${esc(x.이름)}</div><div class="ㅅ">${k}/${m}</div><div class="ㅁ"><i style="width:${m?k/m*100:0}%"></i></div></button>`; }).join("")}</div>
    <div class="운넘"><button class="ㄴ" data-act="이전종목" ${이?"":"disabled"}>‹ ${이?esc(이.이름):"처음"}</button><span class="ㅈ">${본+1} / ${n}</span><button class="ㄷ" data-act="다음종목" ${다?"":"disabled"}>${다?esc(다.이름):"마지막"} ›</button></div>
    <div class="운머리 번호"${번("운0")}><div class="운글"><div class="운이름">${esc(e.이름)}</div>
        ${e.플랜id?`<div class="운곁">플랜 ${e.회}회차${e.측정일?" · 측정일":""}</div>`:""}
        <div class="운근육">${칩||`<span>근육 정보 없음</span>`}</div>
        <div class="운지표"><span>${맨?"최고":"1RM"} <b>${오==null?"—":kg(Math.round(오*2)/2)+단위}</b></span>${칩줄}</div>
        <div class="운지표"><span>달성 <b>${e.세트.length?Math.round(완(e)/e.세트.length*100):0}%</b></span><span>볼륨 <b>${콤마(Math.round(볼(e)))}</b>/${콤마(Math.round(볼(e,true)))}kg</span></div></div>${그림}</div>
    <div class="운세트들"><div class="세트머리 아주작 옅음"><span>세트</span><span>kg</span><span>회</span><span>휴식</span><span>완료</span></div>
      ${e.세트.map((s,k)=>운세트줄(본,s,k,지금k)).join("")}
      <div class="줄"><button class="버튼 낮 채움" data-act="세트더" data-i="${본}">+ 세트</button>${e.세트.length>1?`<button class="버튼 낮" data-act="세트빼기" data-i="${본}">− 세트</button>`:""}</div></div>
    <div class="아랫줄"><button class="버튼" data-act="끝내기">운동 끝내기</button>${갈곳>=0?`<button class="버튼 주 채움 한줄" data-act="본종목" data-i="${갈곳}">다음 · ${esc(ss.종목[갈곳].이름)} ›</button>`:`<button class="버튼 주 채움" data-act="끝내기">마무리</button>`}</div>
    ${시트()}</div>`; }
function 운세트줄(i,s,k,지금k){ const ss=S.세션, 쉼=ss.휴식&&ss.휴식.i===i&&ss.휴식.k===k;
  const 값=(f,v)=>`<div class="값칸"><button data-act="세트값" data-f="${f}" data-i="${i}" data-k="${k}" data-d="-1" aria-label="빼기">−</button><input data-in="세트값" data-f="${f}" data-i="${i}" data-k="${k}" inputmode="decimal" value="${f==="w"?kg(v):v}"><button data-act="세트값" data-f="${f}" data-i="${i}" data-k="${k}" data-d="1" aria-label="더하기">＋</button></div>`;
  return `<div class="세트줄 ${k===지금k&&!쉼?"지금":""}"><span class="k">${k+1}</span>${값("w",s.w)}${값("r",s.r)}
    ${쉼?`<button class="휴식칸" data-act="휴식건너뛰기" aria-label="휴식 건너뛰기"><span class="밑" data-timer="휴식"></span><span class="위" data-쉼바><span data-timer="휴식"></span></span></button>`:`<div class="휴식칸 빈 숫">${분초(s.휴)}</div>`}
    <button class="체크 ${s.완료?"켬":""}" data-enter="체${i}-${k}-${s.완료?1:0}" data-act="체크" data-i="${i}" data-k="${k}" aria-label="${k+1}세트 ${s.완료?"체크 풀기":"완료"}">${s.완료?아이콘.체크:""}</button></div>`; }
/* 같은 종목의 지난 기록 중 최고 (오늘 앞) · 1주 = 7일 전 ~ 전날 (v0.6.10) */
function 운최고(이름,맨){ const 오=오늘(), 주앞=날더하기(오,-7); let 최고=null, 주=null;
  for(const [k,r] of Object.entries(S.기록)){ const 날=k.split("~")[0]; if(날>=오) continue;
    for(const e of r.종목){ if(e.이름!==이름) continue;
      for(const s of e.세트){ if(!s.완료) continue; const v=맨?s.r:일RM(s.w,s.r);
        if(최고==null||v>최고) 최고=v; if(날>=주앞&&(주==null||v>주)) 주=v; } } }
  return {최고,주}; }
'''
s = s[:시작] + 새화면 + s[끝i:]; 기록.append('운동화면 교체')

# ───────── 6. 근육 시트 ─────────
바꿈('  else if(종==="메모"){', '''  else if(종==="근육"){ const ss=S.세션, 단=지금단계(ss?ss.종목:null,모의시각()), e=ss?ss.종목[U.본??0]:null;
    const 줄=Object.entries(단).filter(([,x])=>x>=0.5).sort((a,b)=>b[1]-a[1]).slice(0,12), 사=e?사진목록(e.이름):[];
    const 띠색=[1,5,10,15,20].map(k=>단계색(k)).join(",");
    안 = 머리("지금 몸") + `<div class="큰몸">${몸그림(단,{키:"시트전신"})}</div>
      <div class="몸눈금"><span>0</span><span class="바" style="background:linear-gradient(90deg,${띠색})"></span><span>20단계</span></div>
      ${e?`<div class="작">${esc(e.이름)} — ${근육글(종목근육(e.이름))}</div>`:""}
      ${사.length?`<div class="줄">${사.slice(0,3).map(u=>`<img class="시트사진" src="${u}" alt="${esc(e.이름)} 사진">`).join("")}</div>`:""}
      <table class="몸표"><thead><tr><th>근육</th><th class="ㄱ">단계</th></tr></thead><tbody>${줄.length?줄.map(([l,x])=>`<tr><td><span class="몸점" style="background:${단계색(x)}"></span>${esc((근육이름[l]||l).replace(/ \\(.*\\)/,""))}</td><td class="ㄱ">${Math.round(x)}</td></tr>`).join(""):`<tr><td colspan="2" class="옅음">아직 쌓인 피로가 없습니다</td></tr>`}</tbody></table>
      <div class="아주작 옅음">단계 = 20 × 오늘 유효 볼륨 ÷ 이전 최대 + 남은 피로 · 주동 1.0 · 보조 0.5 · 협응 0.25</div>
      <button class="버튼" data-act="그림감추기">운동 화면에서 그림 감추기</button>`; 높=true; }
  else if(종==="메모"){''', 이름='근육 시트')

# ───────── 7. 행동 · 체크 · 시작 · 시계 ─────────
바꿈('''    case "다음종목": { const n=ss.종목.length; for(let j=1;j<=n;j++){ const i=(ss.지금.i+j)%n, k=ss.종목[i].세트.findIndex(s=>!s.완료); if(k>=0){ ss.지금={i,k}; ss.접기[i]=false; U.스크롤=i; break; } } break; }''',
'''    case "다음종목": U.본=Math.min(ss.종목.length-1,(U.본??0)+1); break;
    case "이전종목": U.본=Math.max(0,(U.본??0)-1); break;
    case "본종목": U.본=+d.i; break;
    case "몸크게": U.시트={종류:"근육"}; break;
    case "그림감추기": S.세션.배너숨김=true; U.시트=null; break;''', 이름='종목 넘기기 동작')
바꿈('if(다.i!==i) U.스크롤=다.i;', '', 이름='체크 뒤 저절로 스크롤 없앰')
바꿈('U.탭="운동"; U.스크롤=0; 발자취(`운동 시작 · ${r.이름}`);', 'U.탭="운동"; U.스크롤=0; U.본=0; 발자취(`운동 시작 · ${r.이름}`);', 이름='시작 때 첫 종목')
바꿈("document.querySelectorAll('[data-bar]').forEach(el=>el.style.transform=`scaleX(${p})`); } }",
     "document.querySelectorAll('[data-bar]').forEach(el=>el.style.transform=`scaleX(${p})`);\n    document.querySelectorAll('[data-쉼바]').forEach(el=>el.style.clipPath=`inset(0 ${(p*100).toFixed(1)}% 0 0)`); } }", 이름='휴식 칸 줄어듦')

# ───────── 8. 22 버그 #4 — 체크한 세트가 없으면 저장하지 않는다 ─────────
바꿈('function 운동저장하기(){ const ss=S.세션; if(!ss) return null; if(ss.끝==null) ss.끝=S.시계;',
     'function 운동저장하기(){ const ss=S.세션; if(!ss) return null;\n  if(!ss.종목.some(e=>e.세트.some(s=>s.완료))){ S.세션=null; 발자취("체크 없는 운동은 저장하지 않음"); return null; }   /* 22 버그 #4 */\n  if(ss.끝==null) ss.끝=S.시계;', 이름='빈 기록 저장 막기')
바꿈('case "운동저장": 운동저장하기(); U.탭="캘린더"; U.고른날=null; 토스트("저장했습니다"); break;',
     'case "운동저장": { const 키=운동저장하기(); U.탭="캘린더"; U.고른날=null; 토스트(키?"저장했습니다":"체크한 세트가 없어 저장하지 않았습니다"); break; }', 이름='저장 알림')

# ───────── 9. 안내 한 줄 ─────────
안내 = "'+3시간'으로 근육이 풀리는 것을 볼 수 있습니다."
바꿈(안내, 안내 + " 운동 화면은 10-02 새 시안입니다 — 한 화면에 한 종목, 근육 그림은 오른쪽에 붙박이.", 이름='안내')

(SP/'7day-new.html').write_text(s, encoding='utf-8')
print("✅ 고친 곳", len(기록), "군데")
for x in 기록: print("  ·", x)
