"""7day-v4.html → 7day-v5.html (10-03 홍겸 님 ✎ 표시 9개 · "지금까지 수정사항 반영해서 시안 바꿔봐. 앱 업데이트는 아직")

표시 → 고친 것
 244w 캘린더  "순서는 총 세트, 볼륨, 예상시간"          → 날짜 판 칩 순서 세트 · 볼륨 · 예상 시간
 h8d9 캘린더  "이동" (예정 X · 칩 ↑ · 운동 시작 ↓ · 변경 ↑) → '예정' 이름표 칸 없앰 · 칩과 변경을 루틴 이름 줄로 · 운동 시작은 맨 아래 한 줄
 g2lt 종목    "내용 -> '변경'"                          → 플랜 카드 단추 '고치기 · 지금 실력 넣기' → '변경'
 h8m2 고침    "맨 윗 부분은 띠 둘러야함"                 → 시트 머리를 띠(강조 바탕 · 강조글)로 — 시트 머리는 한 부품이라 모든 시트가 같이
 0ylt 고침    "이동. 1. 꾸준히늘리기 4X10 글씨 삭제"      → '훈련 방식' 을 머리 띠 오른쪽 단추로 · 그 글씨 삭제 · 닫기는 아래로(저장 | 닫기)
 6yzt 고침    "목표를 1RM 혹은 무게X횟수로 변경"          → 고침 시트에 [1RM][무게 × 횟수] (플랜 만들기 화면과 같은 칩줄) · 바꾸면 같은 실력으로 환산
 xlcr 고침    "주당 1~7 없애고 · 플랜 만들 때는 둠"       → 고침 시트에서 '주당' 줄 삭제 (만들기 화면은 그대로)
 vypy 방식    "세트·횟수는 자유 · 여유/보통/힘들게 · 세트 횟수 칸 삭제 · 5세트 8~15회 알고리즘" → 아래 ★
 hm83 넣기    (메모 없음 · '다 했음' 에 동그라미)         → '닫기' 로 (다른 시트와 같은 말) [Claude 추측]

★ 꾸준히 늘리기 (방식 1번) — 5세트 × 8~15회 이중 진행
  구조는 그대로(20 C-2): 엔진이 회차마다 목표 1RM 을 내고, 방식은 그것을 세트로 바꾼다.
  - 처방 1RM = 목표 1RM × 0.9 (다른 방식과 같은 '보통' 여유 · 플랜표.처방비율)
  - 무게 사다리: 플랜을 시작한 실력의 8회 무게에서 출발, 칸마다 ×1.184 (= 15회 무게 → 8회 무게, Epley 로 같은 실력)
  - 이번 회: 8회 이상 할 수 있는 가장 무거운 칸 → 그 무게로 몇 회 되는지(8~15) — 실력이 오르면 횟수가 8→15 로 오르고,
    15회를 넘을 때 무게가 한 칸 오르며 8회로 돌아간다
  - 세트·횟수 고르기 · 강도 3단계는 없앰 (09-30 의 '세트·횟수 자유' 를 홍겸 님이 10-03 에 바꿈)
"""
import pathlib
SP = pathlib.Path('/tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad')
s = (SP/'7day-v4.html').read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:80]!r}: {c}번")
    s = s.replace(old, new)

# ══ 1. 꾸준히 늘리기 — 5세트 × 8~15회 ══
바꿈('특징:"근육과 기초 근력을 함께 · 세트·횟수는 자유"', '특징:"근육과 기초 근력을 함께"')
바꿈('''  const s = 강도들[p.강도] || 강도들[1];
''', '')
바꿈('''    case 1: { const 세트=p.세트수>0?p.세트수:s.세트, 횟수=p.직접횟수>0?p.직접횟수:s.횟수; return [{무게:되ep(t,횟수)*0.9,횟수,세트}]; }''',
     '''    case 1: return [이중진행(t, p)];''')
바꿈('''function 방식세트(번호, t, p){''',
     '''/* 10-03 꾸준히 늘리기 = 5세트 × 8~15회 이중 진행 (홍겸 님 ✎ 표시)
   무게 사다리는 플랜을 시작한 실력의 8회 무게에서 출발해 칸마다 ×(1+15/30)/(1+8/30) — 15회 무게와 다음 칸 8회 무게가 같은 실력.
   이번 회 처방 1RM(목표 × 0.9)으로 8회 이상 되는 가장 무거운 칸을 고르고, 그 무게로 되는 횟수(8~15)를 낸다.
   → 실력이 오르면 횟수가 8 → 15 로 오르고, 넘치면 무게가 한 칸 오르며 8회로 돌아간다 */
const 이중 = {세트:5, 낮:8, 높:15};
function 이중진행(t, p){ const 폭=S.설정.무게폭||2.5, 비=(1+이중.높/30)/(1+이중.낮/30);
  const 기=(p&&p.시작1RM>0)?p.시작1RM:t, 첫=되ep(기,이중.낮)*플랜표.처방비율, 할=t*플랜표.처방비율;
  let i=Math.floor(Math.log(되ep(할,이중.낮)/첫)/Math.log(비)+1e-9); if(!isFinite(i)) i=0;
  const 무게=무게맞춤(첫*Math.pow(비,i),폭), 횟수=Math.min(이중.높, Math.max(이중.낮, Math.floor(30*(할/무게-1)+1e-9)));
  return {무게, 횟수, 세트:이중.세트}; }
function 방식세트(번호, t, p){''')
바꿈('''const 방식요약 = 대 => { if(대.방식번호!==1) return ""; const s=강도들[대.강도]; return `${대.세트수>0?대.세트수:s.세트}×${대.직접횟수>0?대.직접횟수:s.횟수}`; };''',
     '''const 방식요약 = 대 => 대.방식번호===1 ? `${이중.세트}×${이중.낮}~${이중.높}` : "";''')
바꿈('''  if(b.번호===1){ const 세=대.세트수>0?대.세트수:강도들[대.강도].세트, 회=대.직접횟수>0?대.직접횟수:강도들[대.강도].횟수, 직접=대.세트수>0||대.직접횟수>0;
    칸 = `<div class="칩줄">${강도들.map((s,j)=>`<button class="칩 ${대.강도===j&&!직접?"켬":""}" data-act="강도" data-n="${j}">${s.이름}</button>`).join("")}</div>
      <div class="줄 작"><span class="흐림">세트</span><span class="수칸"><button data-act="폼수" data-f="세트수" data-d="-1">−</button><b class="숫">${세}</b><button data-act="폼수" data-f="세트수" data-d="1">＋</button></span>
      <span class="흐림">횟수</span><span class="수칸"><button data-act="폼수" data-f="직접횟수" data-d="-1">−</button><b class="숫">${회}</b><button data-act="폼수" data-f="직접횟수" data-d="1">＋</button></span><span class="채움 굵" style="text-align:right;white-space:nowrap">총 ${세*회}회</span></div>`; }''',
     '''  if(b.번호===1) 칸 = `<div class="작 숫">${방식요약({방식번호:1})} · ${이중.높}회가 되면 무게를 올리고 ${이중.낮}회부터</div>`;''')
바꿈('''    case "강도": { const 대=대상폼(); 대.강도=+d.n; 대.세트수=0; 대.직접횟수=0; break; }
    case "폼수": { const 대=대상폼(), s=강도들[대.강도]; const 지금=대[d.f]>0?대[d.f]:(d.f==="세트수"?s.세트:s.횟수); 대[d.f]=Math.max(1,지금+(+d.d)); break; }
''', '')

# ══ 2. 플랜 고치기 시트 ══
바꿈('''  return `<div class="머리"><b>플랜 고치기</b><button class="닫기" data-act="시트닫기">닫기</button></div>
    <div class="줄">${고침입력("이름","이름","",true)}${맨몸(t)?고침입력("목표개수","목표 개수","회"):고침입력("목표무게",p.목표방식==="RM"?"목표 1RM":`목표 무게 (×${p.목표횟수}회)`,"kg")}</div>
    <div class="줄"><b class="작 채움">주당</b>${칩줄("고침값","주당",[1,2,3,4,5,6,7],g.주당)}</div>
    ${맨몸(t)?(t.보조?`${칩줄("고침값","보조모드",[0,1,2],g.보조모드,j=>["정자세","어시스트","과부하"][j])}${g.보조모드?`<div class="줄">${고침입력("보조무게",g.보조모드===1?"보조 무게":"추가 무게","kg")}</div>`:""}`:"")
      :`<button class="고르기" data-act="시트" data-t="방식" data-v="고침"><b>훈련 방식</b><span class="곁">${방.번호}. ${방.이름}${방식요약(g)?" · "+방식요약(g):""}</span>›</button>`}''',
     '''  return `<div class="머리"><b>플랜 고치기</b>${맨몸(t)?"":`<button class="작은흰" data-act="시트" data-t="방식" data-v="고침">훈련 방식 ›</button>`}</div>
    ${맨몸(t)?`<div class="줄">${고침입력("이름","이름","",true)}${고침입력("목표개수","목표 개수","회")}</div>`
      :`<div class="줄">${고침입력("이름","이름","",true)}</div>
    ${칩줄("고침목표방식","",["RM","회"],g.목표방식,v=>v==="RM"?"1RM":"무게 × 횟수")}
    <div class="줄">${고침입력("목표무게",g.목표방식==="RM"?"목표 1RM":"목표 무게","kg")}${g.목표방식==="회"?고침입력("목표횟수","횟수","회"):""}</div>`}
    ${맨몸(t)&&t.보조?`${칩줄("고침값","보조모드",[0,1,2],g.보조모드,j=>["정자세","어시스트","과부하"][j])}${g.보조모드?`<div class="줄">${고침입력("보조무게",g.보조모드===1?"보조 무게":"추가 무게","kg")}</div>`:""}`:""}''')
바꿈('''    <button class="버튼 주 넓" data-act="고침저장">저장</button>`; }''',
     '''    <div class="줄"><button class="버튼 주 채움" data-act="고침저장">저장</button><button class="버튼" data-act="시트닫기">닫기</button></div>`; }''')
바꿈('''U.고침={id:p.id, 이름:p.이름, 목표무게:p.목표무게, 목표개수:p.목표개수, 주당:p.주당,''',
     '''U.고침={id:p.id, 이름:p.이름, 목표방식:p.목표방식||"RM", 목표무게:p.목표무게, 목표횟수:p.목표횟수||1, 목표개수:p.목표개수, 주당:p.주당,''')
바꿈('''    case "고침값": U.고침[d.f]=+d.v; break;''',
     '''    case "고침값": U.고침[d.f]=+d.v; break;
    case "고침목표방식": { const g=U.고침, 폭=S.설정.무게폭||2.5, w=숫(g.목표무게), r=Math.max(1,Math.round(숫(g.목표횟수)||1));
      if(d.v===g.목표방식) break;   // 바꿀 때 같은 실력으로 환산 — 1RM 100 ↔ 75kg × 10회
      if(d.v==="회"){ const n=r>1?r:10; g.목표횟수=n; if(w>0) g.목표무게=무게맞춤(되ep(w,n),폭); }
      else { if(w>0) g.목표무게=무게맞춤(일RM(w,r),폭); g.목표횟수=1; }
      g.목표방식=d.v; break; }''')
바꿈('''새={...p, 이름:String(g.이름).trim()||t.이름, 주당:g.주당, 방식번호:g.방식번호,''',
     '''새={...p, 이름:String(g.이름).trim()||t.이름, 방식번호:g.방식번호,''')
바꿈('''      if(맨몸(t)) 새.목표개수=숫(g.목표개수); else 새.목표무게=숫(g.목표무게);''',
     '''      if(맨몸(t)) 새.목표개수=숫(g.목표개수); else { 새.목표방식=g.목표방식; 새.목표무게=숫(g.목표무게); 새.목표횟수=g.목표방식==="RM"?1:Math.max(1,Math.round(숫(g.목표횟수))); }''')

# ══ 3. 종목 탭 플랜 카드 단추 ══
바꿈('''data-act="플랜고치기" data-v="${p.id}">고치기 · 지금 실력 넣기</button>''',
     '''data-act="플랜고치기" data-v="${p.id}">변경</button>''')

# ══ 4. 시트 머리 = 띠 · '다 했음' → 닫기 ══
바꿈('''머리(`${esc(r.이름)}에 넣기`,"다 했음")''', '''머리(`${esc(r.이름)}에 넣기`)''')

# ══ 5. 캘린더 날짜 판 — 예정 이름표 없앰 · 칩 순서 · 변경 위로 · 운동 시작 맨 아래 ══
바꿈('''    <div class="넘김" style="padding:0"><div class="달력"''', '''    <div class="넘김 캘넘김" style="padding:0"><div class="달력"''')
바꿈('''function 예정상세(실, 키){ const 볼=''', '''function 예정칩(실){ const 볼=실.종목.reduce((a,e)=>a+e.세트.reduce((b,s)=>b+유효무게(s.w)*s.r,0),0);
  return `<div class="예칩"><span>${총세트(실)}세트</span><span>볼륨 ${콤마(Math.round(볼))}kg</span><span>예상 ${시간글(예상초(실))}</span></div>`; }
function 예정상세(실, 키){ const 볼=''')
바꿈('''  return `<div class="예칩"><span>예상 ${시간글(예상초(실))}</span><span>${총세트(실)}세트</span><span>볼륨 ${콤마(Math.round(볼))}kg</span></div><div class="예목록">${접는목록(줄, 키)}</div>`; }''',
     '''  return `<div class="예목록">${접는목록(줄, 키)}</div>`; }''')
바꿈('''      h+=`<div class="판줄"><span class="이름">${r.휴식일?"휴식":"예정"}</span><div class="채움"><b>${esc(r.이름)}</b>${r.휴식일?"":예정상세(실, "예"+k)}</div></div>
        <div class="줄 판단추">${k===오&&!r.휴식일?`<button class="버튼 주 채움" data-act="시작" data-v="${r.id}">운동 시작</button>`:""}<button class="버튼 ${k===오&&!r.휴식일?"":"채움"}" data-act="변경" data-k="${k}">변경</button></div>`; }
    else h+=`<div class="판줄"><span class="이름">예정</span><span class="채움 흐림 작">없음</span><button class="버튼 낮" data-act="시트" data-t="루틴고르기" data-k="${k}">루틴 넣기</button></div>`;''',
     '''      h+=`<div class="예머리"><b class="예이름">${esc(r.이름)}</b>${r.휴식일?`<span class="예칩"><span>휴식일</span></span>`:예정칩(실)}<button class="버튼 낮" data-act="변경" data-k="${k}">변경</button></div>
        ${r.휴식일?"":예정상세(실, "예"+k)}
        ${k===오&&!r.휴식일?`<div class="줄 판단추"><button class="버튼 주 채움" data-act="시작" data-v="${r.id}">운동 시작</button></div>`:""}`; }
    else h+=`<div class="예머리"><span class="예이름 흐림 작">예정 없음</span><span class="예칩"></span><button class="버튼 낮" data-act="시트" data-t="루틴고르기" data-k="${k}">루틴 넣기</button></div>`;''')

# ══ 6. CSS ══
css = '''
/* ═══ 10-03 표시 9개 ═══ */
/* 시트 머리 = 띠 (캘린더 년월 띠와 같은 값 · 강조 바탕 · 강조글) — 붙박이라 긴 시트도 머리가 남는다.
   top:-14px — 붙박이 기준 칸은 시트의 안쪽 여백(14)을 뺀 곳이라, 0 이면 띠 위에 흰 줄이 남고 아래 글을 덮는다 */
.시트 .머리{position:sticky;top:-14px;z-index:3;margin:-14px -14px 0;min-height:40px;padding:6px 12px;background:var(--강조);color:var(--강조글)}
.시트 .머리 .닫기{color:var(--강조글)}
/* 날짜 판 — 루틴 이름 줄에 칩 · 변경, 운동 시작은 맨 아래 */
.캘넘김{display:flex;flex-direction:column}
.캘넘김>*{flex-shrink:0}
.캘넘김>.판{flex:1 0 auto}
.예머리{display:flex;align-items:center;gap:8px;border-top:1px solid var(--선);padding-top:8px;min-width:0}
.예이름{flex:none;max-width:40%;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.예머리 .예칩{flex:1;min-width:0;margin-top:0}
.예머리 .버튼{flex:none}
.판단추{margin-top:auto}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]

(SP/'7day-v5.html').write_text(s, encoding='utf-8')
print("v5 →", f"{len(s.encode()):,} 바이트")
