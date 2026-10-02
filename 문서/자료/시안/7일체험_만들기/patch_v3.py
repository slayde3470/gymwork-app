"""10-02 표시 7개 반영 — 7day-mark.html(표시 도구까지) → 7day-v3.html
   쌓는 순서: patch7.py → patch_mark.py → patch_v3.py  (build.sh)"""
import pathlib
SP = pathlib.Path('/tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad')
s = (SP/'7day-mark.html').read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:70]!r}: {c}번")
    s = s.replace(old, new)

# ── 표시 1 — 캘린더 칸 글씨 겹침: 날짜13 + 루틴11 + 상태11 를 쌓으면 약 61px. 칸 56 → 64 ──
바꿈('.칸날{height:56px;border-radius:8px;padding:3px 2px;display:flex;flex-direction:column;align-items:center;gap:1px;',
     '.칸날{height:64px;border-radius:8px;padding:4px 2px;display:flex;flex-direction:column;align-items:center;gap:0;')

# ── 표시 2 · 3 — 날짜 판: 한 줄에 욱여넣던 루틴 정보를 세로로 (정보 칩 + 종목마다 한 줄) ──
바꿈('''<div class="채움"><b>${esc(r.이름)}</b>${r.휴식일?"":`<div class="맞춤 작 흐림">예상 ${시간글(예상초(실))} · ${총세트(실)}세트 · ${r.종목.map(e=>esc(e.이름)).join(" · ")}</div>`}</div></div>''',
     '''<div class="채움"><b>${esc(r.이름)}</b>${r.휴식일?"":예정상세(실)}</div></div>''')
바꿈('''      <div class="맞춤 작 흐림 숫">${완료수(r)}/${전체수(r)}세트 · ${시간글(r.초)} · ${콤마(기록볼륨(r))}kg</div></div>''',
     '''      <div class="맞춤 작 흐림 숫">${완료수(r)}/${전체수(r)}세트 · ${시간글(r.초)} · ${콤마(기록볼륨(r))}kg</div>${기록상세(r)}</div>''')
도우미 = r'''
/* 10-02 표시 2 · 3 — "가로로 표시되니까 운동이 다 안 나오잖아 · 아래로 여유 있게" */
const 폭글=(l,f)=>{ const a=Math.min(...l), b=Math.max(...l); return a===b?f(a):`${f(a)}~${f(b)}`; };
function 세트글(세){ if(!세.length) return ""; const w=세.map(s=>+s.w||0), r=세.map(s=>s.r);
  return (w.every(x=>x===0)?"맨몸":폭글(w,kg)+"kg")+" × "+폭글(r,x=>x)+"회"; }
function 예정상세(실){ const 볼=실.종목.reduce((a,e)=>a+e.세트.reduce((b,s)=>b+유효무게(s.w)*s.r,0),0);
  const 줄=실.종목.map((e,i)=>`<div class="예줄"><span class="번">${i+1}</span><span class="채움 한줄">${esc(e.이름)}</span><span class="숫 흐림">${e.세트.length}세트 · ${세트글(e.세트)}</span></div>`).join("");
  return `<div class="예칩"><span>예상 ${시간글(예상초(실))}</span><span>${총세트(실)}세트</span><span>볼륨 ${콤마(Math.round(볼))}kg</span></div><div class="예목록">${줄}</div>`; }
function 기록상세(r){ const 줄=r.종목.map((e,i)=>{ const 세=e.세트.filter(s=>s.완료);
    return `<div class="예줄"><span class="번">${i+1}</span><span class="채움 한줄">${esc(e.이름)}</span><span class="숫 흐림">${세.length}/${e.세트.length}세트${세.length?" · "+세트글(세):""}</span></div>`; }).join("");
  return 줄?`<div class="예목록">${줄}</div>`:""; }
'''
바꿈('function 날판(k){', 도우미 + 'function 날판(k){')

# ── 표시 5 — 그림 칸: 위쪽에 전신 1장 + 부위 1장 (설정 '근육 2장'). 쉬는 동안 커지던 것은 없앤다(흔들림) ──
바꿈('const e=ss.종목[ss.지금.i], 단=지금단계(ss.종목,모의시각()), 높=ss.휴식?168:124;',
     'const e=ss.종목[U.본??ss.지금.i], 단=지금단계(ss.종목,모의시각()), 높=144;   /* 10-02: 보는 종목 기준 · 높이 고정(쉬는 동안 124→168 로 커지며 화면이 흔들렸다) */')
바꿈('<div class="배너 번호" style="height:${높}px" data-anim-h="배너"',
     '<div class="배너 번호" style="height:${높}px" data-act="몸크게" aria-label="근육지도 크게 보기"')
바꿈('.배너{display:flex;gap:6px;padding:6px 12px 0;position:relative;flex:none;transition:height .35s cubic-bezier(.2,.8,.2,1)}',
     '.배너{display:flex;gap:8px;padding:8px 12px;position:relative;flex:none;box-sizing:border-box;background:var(--면);border-bottom:1px solid var(--선);cursor:pointer}')

# ── 표시 4 · 6 · 7 — 운동 화면 배치 ──
시작 = s.index('function 운동화면(){'); 끝 = s.index('function 운세트줄(')
새 = r'''function 운동화면(){ const ss=S.세션, n=ss.종목.length;
  /* 10-02 표시 4~7 — 맨 위: 지금 종목 정보(③) · 그 아래: 근육 그림 두 장(전신 · 부위) · 세트 ·
     아래: 루틴 진행 + 종목 띠(②) · 맨 아래: 이전 / 운동 끝내기 / 다음. 보는 종목(U.본)은 손으로만 바뀐다 */
  if(U.본==null||U.본>=n||U.본<0) U.본=Math.min(Math.max(ss.지금.i,0),n-1);
  const 본=U.본, e=ss.종목[본], 완=x=>x.세트.filter(s=>s.완료).length;
  const 전=ss.종목.reduce((a,x)=>a+x.세트.length,0), 끝낸=ss.종목.reduce((a,x)=>a+완(x),0);
  const 끝종=ss.종목.filter(x=>x.세트.length&&완(x)===x.세트.length).length;
  const 볼=(x,다)=>x.세트.reduce((a,s)=>a+((다||s.완료)?유효무게(s.w)*(다?(s.목r||s.r):s.r):0),0);
  const 총볼=ss.종목.reduce((a,x)=>a+볼(x),0), 총목=ss.종목.reduce((a,x)=>a+볼(x,true),0);
  const 단=지금단계(ss.종목,모의시각()), 근=종목근육(e.이름);
  const 잎최대=id=>Math.max(0,...잎(id).map(l=>단[l]||0));
  const 근줄=Object.entries(근).sort((a,b)=>"PSY".indexOf(a[1])-"PSY".indexOf(b[1]));
  const 칩=근줄.slice(0,2).map(([id,r])=>
    `<span class="${r==="P"?"주":""}"><i style="background:${단계색(잎최대(id))||"var(--속선)"}"></i>${esc((근육이름[id]||id).replace(/ \(.*\)/,""))}</span>`).join("")
    + (근줄.length>2?`<span>+${근줄.length-2}</span>`:"");
  const 세=e.세트.filter(s=>s.완료), 맨=맨몸(찾표(e.이름));
  const 오=세.length?(맨?Math.max(...세.map(s=>s.r)):Math.max(...세.map(s=>일RM(s.w,s.r)))):null;
  const 지=운최고(e.이름,맨), 단위=맨?"회":"kg";
  const 비=(이름,기)=>{ if(오==null||기==null) return `<span class="운칩 빈">${이름} —</span>`; const d=Math.round((오-기)*2)/2;
    return `<span class="운칩 ${d>0?"오름":d<0?"내림":""}">${이름} ${d===0?"유지":(d>0?"▲ ":"▼ ")+kg(Math.abs(d))+단위}</span>`; };
  const 다음i=ss.종목.findIndex((x,i)=>i>본&&완(x)<x.세트.length), 첫i=ss.종목.findIndex(x=>완(x)<x.세트.length);
  const 갈곳=다음i>=0?다음i:(첫i>=0&&첫i!==본?첫i:-1), 지금k=e.세트.findIndex(s=>!s.완료);
  const 그림보임=S.설정.배너!=="숨김"&&!ss.배너숨김;
  return `<div class="화면">
    <div class="운머리 번호"${번("운0")}><button class="버튼 낮 운나감" data-act="탭" data-t="캘린더" aria-label="나가기">‹</button>
      <div class="운글"><div class="운이름">${esc(e.이름)}</div>
        ${e.플랜id?`<div class="운곁">플랜 ${e.회}회차${e.측정일?" · 측정일":""}</div>`:""}
        <div class="운근육">${칩||`<span>근육 정보 없음</span>`}</div>
        <div class="운지표"><span>${맨?"최고":"1RM"} <b>${오==null?"—":kg(Math.round(오*2)/2)+단위}</b></span>${비("1주",지.주)}${비("최고",지.최고)}</div>
        <div class="운지표"><span>달성 <b>${e.세트.length?Math.round(완(e)/e.세트.length*100):0}%</b></span><span>볼륨 <b>${콤마(Math.round(볼(e)))}</b>/${콤마(Math.round(볼(e,true)))}kg</span></div></div>
      <div class="운오른"><span class="숫 굵 큰" data-timer="경과"></span>${그림보임?"":`<button class="버튼 낮" data-act="배너보기">그림</button>`}</div></div>
    ${배너()}
    <div class="운세트들"><div class="세트머리 아주작 옅음"><span>세트</span><span>kg</span><span>회</span><span>휴식</span><span>완료</span></div>
      ${e.세트.map((s,k)=>운세트줄(본,s,k,지금k)).join("")}
      <div class="줄"><button class="버튼 낮 채움" data-act="세트더" data-i="${본}">+ 세트</button>${e.세트.length>1?`<button class="버튼 낮" data-act="세트빼기" data-i="${본}">− 세트</button>`:""}</div></div>
    <div class="운아래 번호"${번("운띠")}>
      <div class="운요약"><b class="운루틴 한줄">${esc(ss.이름)}</b><span>종목 <b>${끝종}/${n}</b></span><span>세트 <b>${끝낸}/${전}</b></span><span>볼륨 <b>${콤마(Math.round(총볼))}</b>/${콤마(Math.round(총목))}</span></div>
      <div class="운막대"><i style="width:${전?끝낸/전*100:0}%"></i></div>
      <div class="운띠">${ss.종목.map((x,i)=>{ const m=x.세트.length, k=완(x), 다끝=m&&k===m;
        return `<button class="운칸 ${i===본?"지금":""} ${다끝&&i!==본?"끝":""}" data-act="본종목" data-i="${i}" aria-label="${esc(x.이름)} ${k}/${m}세트"><div class="ㅇ">${esc(x.이름)}</div><div class="ㅅ">${k}/${m}</div><div class="ㅁ"><i style="width:${m?k/m*100:0}%"></i></div></button>`; }).join("")}</div></div>
    <div class="아랫줄"><button class="버튼 운이전" data-act="이전종목" ${본>0?"":"disabled"}>‹ 이전</button><button class="버튼" data-act="끝내기">운동 끝내기</button>${갈곳>=0?`<button class="버튼 주 채움 한줄" data-act="본종목" data-i="${갈곳}">다음 · ${esc(ss.종목[갈곳].이름)} ›</button>`:`<button class="버튼 주 채움" data-act="끝내기">마무리</button>`}</div>
    ${시트()}</div>`; }
'''
s = s[:시작] + 새 + s[끝:]

# ── CSS (11 UI지침: 글자 11·13·15·18 · 여백 4·8·12·16 · 모서리 8/16 · 막대 8/4) ──
css = '''
/* ═══ 10-02 표시 반영 ═══ */
.예칩{display:flex;gap:4px;flex-wrap:wrap;margin-top:8px}
.예칩 span{height:28px;padding:0 8px;border:1px solid var(--속선);border-radius:8px;font-size:11px;color:var(--흐림);display:inline-flex;align-items:center;font-variant-numeric:tabular-nums}
.예목록{display:flex;flex-direction:column;margin-top:8px;border-top:1px solid var(--선)}
.예줄{display:flex;align-items:center;gap:8px;padding:8px 0;border-bottom:1px solid var(--선);font-size:13px;min-width:0}
.예줄 .번{width:16px;flex:none;font-size:11px;font-weight:700;color:var(--옅음);text-align:center}
.예줄 .숫{flex:none;font-size:11px;white-space:nowrap;font-variant-numeric:tabular-nums}
.운나감{flex:none}
.운오른{flex:none;display:flex;flex-direction:column;align-items:flex-end;gap:8px}
.운아래{flex:none;padding:8px 12px;background:var(--면2);border-top:1px solid var(--선)}
.운아래 .운요약{margin-top:0;align-items:baseline;min-width:0}
.운아래 .운루틴{flex:1;min-width:0;font-size:13px;color:var(--글)}
.운아래 .운막대{margin-top:8px}
.운아래 .운띠{padding:8px 0 0;background:none;border:0}
.운이전{flex:none}
.배너닫기{top:12px}
.그림이름{left:8px}
.판줄:has(.예목록){align-items:flex-start}   /* 목록이 길면 '예정' 이름표를 위에 붙인다 */
'''
끝c = s.rfind('</style>'); s = s[:끝c] + css + s[끝c:]

(SP/'7day-v3.html').write_text(s, encoding='utf-8')
print("표시 7개 반영 →", f"{len(s.encode()):,} 바이트")
