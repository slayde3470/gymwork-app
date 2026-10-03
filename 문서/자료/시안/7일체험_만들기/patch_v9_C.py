"""7일 체험 시안 v9 C — 캘린더 ✎ 표시 2개 (xdlh · xz0l) + 근육 기준 '휴식력' 저장 고치기 (10-03 밤 홍겸 님)
   python3 patch_v9_C.py 입력.html 출력.html

표시 → 고친 것
 xdlh 캘린더 "운동 달성이후 ui가 이상하게 바뀌었네. '기록' 이라는 글자랑 '한번더' 박스때문에 그런거같은데 이거 다 없애.
      원래 '운동시작' 있었던 곳에 '한번더' '운동 기록 삭제' 버튼 만들어야된다고 말했었어. 이미 예전에..."
      → 기록한 날 날짜 판 = 예정 날과 같은 모양: 이름 줄([이름][달성][세트][볼륨][시간]) + 종목 두 칸 목록. 왼쪽 '기록'('예시') 이름표 칸 · 작은 [한 번 더] 없앰.
        '운동 시작' 자리(판 맨 아래 붙박이)에 오늘이면 [한 번 더][운동 기록 삭제], 지난 날이면 [운동 기록 삭제].
        삭제 = 묻지 않고 그날 기록 전부를 지우고 아래띠 6초 [되돌리기] (U5-4 · 02 기능명세 2-6 '기록 삭제 = 그날 것 전부').
        같은 날 기록이 여럿('한 번 더')이면 판 안에 기록마다 이름 줄 · 목록을 차례로. [한 번 더] 는 그날 마지막 기록의 루틴
 xz0l 캘린더 "'띠' 에 들어가는 박스들은 '흰박스, 파란글씨' 로 변경. 날짜 옆 < > 이거 크기를 50% 키워주고 좌우로 10px정도씩 더 벌려줘.
      또, 2026년 10월 부분을 누르면 월단위로 변경할수있는 창이 뜨게 해줄래?"
      → .작은흰(띠 위 단추 공통) = 바탕 --강조글 · 글자 --강조 · 테두리 같은 색. 어두운 화면에서는 변수가 뒤집혀 '짙은 상자 · 밝은 파랑 글'
        ‹ › 글자 18 → 28 (1.5배) · 누르는 칸 32 → 40 · 년월과의 틈 4 → 12 (단추가 넓어진 4 와 합쳐 눈에 보이는 거리 약 +10).
        띠 높이 40 은 그대로(위아래 -6 겹침) · 폰 칸이 좁으면(화면 400 이하) ‹ › 폭 32 · 틈 8 — [스탯][업적] 이 접히지 않게
        '2026년 10월' 을 누르면 달 고르기 시트: 머리 띠 ‹ 2026년 › (해 넘기기) · 1~12월 3×4 칸 · 보는 달 = 고른 날 칸과 같은 표시
        · 오늘 달 = 오늘 날짜와 같은 파란 바탕 숫자 · 누르면 그 달로 가고 닫힘
 휴식력 저장 — db thresholds 에 '_settings' 문서가 하나도 없었다(10-03 21:30 ArtifactData 로 확인 · 12 부위 문서는 있음).
      왜 안 남았는지는 확인 못 함 [추측: 밑줄로 시작하는 id 가 막힘 — 다만 db 규약(0.2.66)은 '_' 를 허용 문자로 적고 있다 ·
      또는 치고 0.7초 안에 창을 닫아 저장 전에 끝남] →
      문서 id 'settings' 로 · 칸을 떠나거나(change) 창이 가려질 때 기다리지 않고 바로 저장 · 저장이 안 되면 칸 옆에 흐린 '저장 안 됨'
      · 읽을 때는 settings 를 먼저, 없으면 옛 _settings
"""
import sys, pathlib
들, 날 = pathlib.Path(sys.argv[1]), pathlib.Path(sys.argv[2])
s = 들.read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:80]!r}: {c}번")
    s = s.replace(old, new)

# ══ xz0l — 년월을 누르면 달 고르기 시트 ══
바꿈('<b>${y}년 ${m}월</b>',
     '<button class="년월글" data-act="달고르기" aria-haspopup="dialog"><b>${y}년 ${m}월</b></button>')
# xdlh 되돌리기 아래띠는 캘린더 안에 (지운 날 판을 보고 있을 때만 뜬다)
바꿈('${칸}</div>${날판(U.고른날||오)}</div>`;',
     '${칸}</div>${날판(U.고른날||오)}</div>${기록지움띠()}`;')

# ══ xdlh — 기록한 날 판: 이름표 칸 · 작은 [한 번 더] 없애고 예정 날과 같은 모양 ══
바꿈('''  for(const [rk,r] of 록){ const 달=기록달성(r);
    h+=`<div class="판줄"><span class="이름">${r.예시?"예시":"기록"}</span><div class="채움"><div class="줄"><b class="채움 한줄">${esc(r.이름)}</b><span class="알약 ${달?"달성":"미달성"}">${달?"달성":"미달성"}</span></div>
      <div class="맞춤 작 흐림 숫">${완료수(r)}/${전체수(r)}세트 · ${시간글(r.초)} · ${콤마(기록볼륨(r))}kg</div>${기록상세(r, "록"+rk)}</div>${k===오&&루틴(r.루틴id)?`<button class="버튼 낮" data-act="시작" data-v="${r.루틴id}">한 번 더</button>`:""}</div>`; }''',
'''  for(const [rk,r] of 록) h+=기록머리(r)+기록상세(r, "록"+rk);   // xdlh 기록이 여럿('한 번 더')이면 이름 줄 · 목록을 차례로''')
바꿈('''  return h+`</div>`; }''',
'''  if(록.length) h+=기록단추(k, 록);   // xdlh '운동 시작' 자리 = 맨 아래 붙박이
  return h+`</div>`; }''')
바꿈('''function 날판(k){''',
'''/* xdlh 기록 이름 줄 — 예정 날 이름 줄(.예머리)과 같은 부품: [이름][달성 알약][세트][볼륨][시간]. 왼쪽 '기록' 이름표 칸은 없다.
   세트 칩은 달성이면 '13세트'(알약이 이미 '다 했다'고 말한다 · 예정 칩과 같은 글) · 미달성이면 '10/13세트'(얼마나 했는지).
   '13/13세트' + 알약이면 360 폭(폰 칸 328)에서 칩이 두 줄로 접혔다 */
function 기록머리(r){ const 달=기록달성(r);
  return `<div class="예머리 록머리"><b class="예이름">${esc(r.이름)}</b><span class="알약 ${달?"달성":"미달성"}">${달?"달성":"미달성"}</span>`
    + `<div class="예칩"><span>${달?전체수(r):`${완료수(r)}/${전체수(r)}`}세트</span><span>볼륨 ${콤마(기록볼륨(r))}kg</span><span>${시간글(r.초)}</span></div></div>`; }
/* xdlh 맨 아래 줄 — 오늘: [한 번 더](그날 마지막 기록의 루틴 · 운동 시작과 같은 주 단추) [운동 기록 삭제] / 지난 날: [운동 기록 삭제] 만.
   삭제는 지우기라 글자 --나쁨 (U1-4). 높이 40 = 운동 시작과 같다 (U4-5) */
function 기록단추(k, 록){ const 끝=록[록.length-1][1], 다시=k===오늘()&&루틴(끝.루틴id)&&!루틴(끝.루틴id).휴식일 ? 끝.루틴id : null;
  return `<div class="줄 판단추 록단추">${다시?`<button class="버튼 주 채움" data-act="시작" data-v="${다시}">한 번 더</button>`:""}<button class="버튼 채움 나쁨" data-act="기록지움" data-k="${k}">운동 기록 삭제</button></div>`; }
/* xdlh 기록 삭제 — 묻지 않고 그날 기록 전부를 지우고 아래띠 6초 [되돌리기] (U5-4 · 02 2-6 '기록 삭제 = 그날 것 전부').
   기록만 지운다 — 근육 피로 · 최대 볼륨 · 플랜 진행 · 업적 · 스탯은 그대로 (앱도 '지우기만 하면 업적을 보지 않는다' · 00 2-28) */
let 기록지움번호 = 0;
function 기록지우기(k){ const 록=기록목록(k); if(!록.length) return; const id=++기록지움번호;
  U.기록지움={id, k, 록}; for(const [rk] of 록) delete S.기록[rk]; U.예펼침=null;
  발자취(`${날글(k)} 운동 기록 삭제${록.length>1?` (${록.length}개)`:""}`);
  setTimeout(()=>{ if(U.기록지움?.id!==id) return; U.기록지움=null;
    document.querySelectorAll("#폰 .록지움띠").forEach(el=>{ el.classList.add("나감띠"); setTimeout(()=>el.remove(),200); }); }, 6000); }
function 기록되돌리기(){ const x=U.기록지움; U.기록지움=null; if(!x) return;
  for(const [rk,r] of x.록) if(!S.기록[rk]) S.기록[rk]=r; U.고른날=x.k; 발자취(`${날글(x.k)} 운동 기록 되돌림`); }
function 기록지움띠(){ const x=U.기록지움; if(!x) return "";
  return `<div class="아래띠 록지움띠" role="status" data-enter="록지움${x.id}"><span class="채움">${날글(x.k)} 운동 기록을 지웠습니다</span><button data-act="기록되돌림">되돌리기</button></div>`; }
/* xz0l 달 고르기 시트 — 머리 띠는 캘린더 년월 띠와 같은 모양(‹ 해 › 가 한가운데 · 오른쪽 닫기). 아래 1~12월 3×4.
   보는 달 = 고른 날 칸과 같은 표시(강조 테두리 · 강조옅음 바탕) · 오늘 달 = 오늘 날짜 숫자와 같은 강조 바탕. 누르면 그 달로 가고 닫힌다 */
function 달고르기시트(){ const 오=오늘(), 이달=오.slice(0,7), 보=U.보는달||이달, 해=U.시트.해;
  return `<div class="머리 달머리"><span></span><span class="년월"><button class="달넘김" data-act="해넘김" data-d="-1" aria-label="이전 해">‹</button><b>${해}년</b><button class="달넘김" data-act="해넘김" data-d="1" aria-label="다음 해">›</button></span><button class="닫기" data-act="시트닫기">닫기</button></div>
    <div class="달칸들" role="group" aria-label="${해}년 달">${Array.from({length:12},(_,i)=>{ const v=`${해}-${String(i+1).padStart(2,"0")}`;
      return `<button class="달칸 ${v===보?"고름":""} ${v===이달?"오늘":""}" data-act="달고름" data-v="${v}"${v===보?` aria-current="true"`:""}><span>${i+1}월</span></button>`; }).join("")}</div>`; }
function 날판(k){''')

# 시트 · 행동 — 달 고르기 · 기록 삭제 · 되돌리기
바꿈('''  else if(종==="휴식"){''',
'''  else if(종==="달고르기") 안 = 달고르기시트();   // xz0l
  else if(종==="휴식"){''')
바꿈('''case "오늘달": U.보는달=null; U.고른날=null; break;''',
'''case "오늘달": U.보는달=null; U.고른날=null; break;
    case "달고르기": U.시트={종류:"달고르기", 해:+(U.보는달||오.slice(0,7)).slice(0,4)}; break;   // xz0l 년월을 누르면
    case "해넘김": U.시트.해 += +d.d; break;
    case "달고름": U.보는달 = d.v===오.slice(0,7) ? null : d.v; U.시트=null; break;
    case "기록지움": 기록지우기(d.k); break;   // xdlh
    case "기록되돌림": 기록되돌리기(); break;''')

# ══ 근육 기준 '휴식력' 저장 — 문서 id settings · 바로 저장 · '저장 안 됨' ══
바꿈('''<span class="기준설명">기준에 곱하는 수 · 비우면 1.0</span></label>''',
     '''<span class="기준설명">기준에 곱하는 수 · 비우면 1.0</span><span class="기준저장" id="휴저장" role="status" hidden>저장 안 됨</span></label>''')
바꿈('''  const 휴칸=document.getElementById("휴식력칸"); let 휴시계=0, 휴대기=false, 휴사슬=Promise.resolve();
  휴칸.addEventListener("input", ()=>{ const v=parseFloat(휴칸.value); 근육기준설정.휴식력 = isFinite(v)&&v>0 ? v : 1; 휴대기=true;
    clearTimeout(휴시계); 휴시계=setTimeout(()=>{ if(!db){ 휴대기=false; return; }
      const 글={휴식력: isFinite(v)&&v>0 ? v : null, 쓰는값:근육기준설정.휴식력, 설명:"근육 기준 공식의 휴식력 (null = 기본 1.0)", 일차:S.일차, 바꾼때:new Date().toISOString()};
      휴사슬=휴사슬.then(()=>db.collection("thresholds").doc("_settings").set(글)).catch(()=>{}).finally(()=>{ 휴대기=false; }); }, 700);
    기준글(); 그리기(); });''',
'''  /* 10-03 밤 — 휴식력 문서 id '_settings' → 'settings'. 실제 db 에 _settings 가 하나도 없었다(부위 12 문서는 있음 · 원인은 확인 못 함
     [추측: 밑줄로 시작하는 id 가 막힘(규약은 '_' 를 허용한다고 적음) · 또는 치고 0.7초 안에 창을 닫아 저장 전에 끝남]). 그래서
     ① id 를 밑줄 없이 ② 칸을 떠나거나(change · Enter) 창이 가려지면 기다리지 않고 바로 저장 ③ 못 남기면 칸 옆에 흐린 '저장 안 됨'(까닭은 title)
     ④ 저장소가 늦게 붙으면 붙는 대로 남긴다. 읽을 때는 settings 먼저, 없으면 옛 _settings */
  const 설정id="settings", 옛설정id="_settings";
  const 휴칸=document.getElementById("휴식력칸"), 휴표=document.getElementById("휴저장"); let 휴시계=0, 휴대기=false, 휴사슬=Promise.resolve(), 휴남은=null;
  const 휴알림=(안됨, 까닭="")=>{ 휴표.hidden=!안됨; 휴표.title = 안됨 ? `저장 안 됨${까닭?` — ${까닭}`:""}` : ""; };
  function 휴올림(){ clearTimeout(휴시계); if(!휴남은) return;
    if(!db){ 휴알림(true, "저장소에 연결되지 않음"); return; }   // 휴남은 은 두었다가 저장소가 붙으면 남긴다
    const 글=휴남은; 휴남은=null;
    휴사슬=휴사슬.then(()=>db.collection("thresholds").doc(설정id).set(글)).then(()=>휴알림(false), e=>휴알림(true, String(e?.code||e?.message||e||"")))
      .finally(()=>{ if(!휴남은) 휴대기=false; }); }
  휴칸.addEventListener("input", ()=>{ const v=parseFloat(휴칸.value); 근육기준설정.휴식력 = isFinite(v)&&v>0 ? v : 1; 휴대기=true;
    휴남은={휴식력: isFinite(v)&&v>0 ? v : null, 쓰는값:근육기준설정.휴식력, 설명:"근육 기준 공식의 휴식력 (null = 기본 1.0)", 일차:S.일차, 바꾼때:new Date().toISOString()};
    clearTimeout(휴시계); 휴시계=setTimeout(휴올림, 700);   // 치는 동안은 모았다가 멈추면 한 번
    기준글(); 그리기(); });
  휴칸.addEventListener("change", 휴올림);
  document.addEventListener("visibilitychange", ()=>{ if(document.hidden) 휴올림(); });''')
바꿈('''  쓰기("db").then(d=>{ db=d; if(!db) return;
    db.collection("thresholds").onSnapshot(q=>{ let 다시=false;
      for(const doc of q.docs){ const k=doc.id;
        if(k==="_settings"){ if(휴대기) continue; const h=(doc.data()||{}).휴식력, 새=typeof h==="number"&&h>0?h:1;
          if(새!==근육기준설정.휴식력){ 근육기준설정.휴식력=새; 다시=true; } if(document.activeElement!==휴칸) 휴칸.value = typeof h==="number"&&h>0 ? h : ""; continue; }''',
'''  쓰기("db").then(d=>{ db=d; if(!db) return; if(휴남은) 휴올림();
    db.collection("thresholds").onSnapshot(q=>{ let 다시=false;
      const 설=q.docs.find(x=>x.id===설정id) || q.docs.find(x=>x.id===옛설정id);
      if(설 && !휴대기){ const h=(설.data()||{}).휴식력, 새=typeof h==="number"&&h>0?h:1;
        if(새!==근육기준설정.휴식력){ 근육기준설정.휴식력=새; 다시=true; } if(document.activeElement!==휴칸) 휴칸.value = typeof h==="number"&&h>0 ? h : ""; }
      for(const doc of q.docs){ const k=doc.id;
        if(k===설정id||k===옛설정id) continue;''')

css = '''
/* ═══ 10-03 밤 v9 C — 캘린더 ✎ 표시 (xdlh · xz0l) · 근육 기준 휴식력 저장 ═══ */
/* xz0l 띠 위 단추 공통 = 흰 상자 · 파란 글씨 (홍겸 님). 색은 변수만 — 어두운 화면에서는 띠(밝은 파랑)와 반대로 짙은 상자 · 밝은 파랑 글.
   테두리는 1px 그대로 두고 색만 바탕과 같게 — 크기가 안 바뀐다. 이 클래스를 쓰는 곳(캘린더 [스탯][업적] · 날짜 띠 [변경][루틴 넣기][오늘]
   · 루틴 띠 [‹ 루틴] · 운동 띠 [그림] · 플랜 고치기 시트 머리 [훈련 방식 ›])이 다 같이 바뀐다 */
.작은흰{background:var(--강조글);color:var(--강조);border-color:var(--강조글)}
/* xz0l ‹ › 1.5배 — 글자 18 → 28 · 누르는 칸 32 → 40 (U3-3). 띠 높이 40 이 늘지 않게 위아래 -6(띠 안쪽 여백만큼) 겹친다.
   홍겸 님 '좌우로 10px 정도 더' — 눈에 보이는 거리(년월 글자 끝 ↔ ‹ 글자) 기준. 단추가 32 → 40 으로 넓어지며 글자가 이미 4 밖으로 나가므로
   틈은 4 → 12 (간격 단계 안). 합쳐서 글자 사이 약 +10 */
.달넘김{width:40px;height:40px;margin-block:-6px;font-size:28px;line-height:1}
.년월{gap:12px}
/* 폰 칸이 좁으면(화면 400 이하 → 폰 칸 368 이하) [스탯] · [업적] 이 두 줄로 접혔다 (360 에서 띠 높이 58) →
   년월 띠에서만 ‹ › 폭 32(높이 40 그대로) · 틈 8 · 띠 칸 사이 4. 루틴 세트 줄의 좁은 화면 규칙과 같은 문턱 */
@media (max-width:400px){
  .띠.년월띠{column-gap:4px}
  .년월띠 .년월{gap:8px}
  .년월띠 .달넘김{width:32px}
}
/* xz0l 년월 글자도 단추 — 누르면 달 고르기. 모양은 그대로(18 Bold · 강조글), 누르는 높이만 40 */
.년월글{height:40px;margin-block:-6px;display:inline-flex;align-items:center;color:var(--강조글);border-radius:8px}
/* 달 고르기 시트 — 머리 띠 = 세 칸(빈칸 | ‹ 해 › | 닫기), 캘린더 년월 띠와 같은 나눔 */
.시트 .머리.달머리{display:grid;grid-template-columns:1fr auto 1fr}
.달머리 .년월 b{flex:none}
.달머리 .닫기{justify-self:end}
.달칸들{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:8px;padding-top:8px}
.달칸{height:44px;border-radius:8px;border:1px solid var(--속선);background:var(--면);font-size:15px;font-weight:500;display:flex;align-items:center;justify-content:center;font-variant-numeric:tabular-nums}
.달칸.고름{border-color:var(--강조);background:var(--강조옅음);font-weight:700}
.달칸.오늘 span{background:var(--강조);color:var(--강조글);border-radius:8px;padding:0 8px;font-weight:700}
/* xdlh 기록 이름 줄 — 예정 이름 줄과 같은 부품. 알약은 이름에 딸린 표시라 이름 바로 뒤 틈 4(줄 틈 8 에서 -4), 칩은 남은 자리.
   360 폭(폰 칸 328)에서 [가슴·어깨][달성][13세트][볼륨 5,028kg][28분] 이 2px 모자라 두 줄로 접혔다. 더 길면(1시간 넘는 운동 등) 칩만 다음 줄로 */
.록머리 .알약{flex:none;margin-left:-4px}
.판단추.록단추 .버튼{min-width:0}
.록지움띠.나감띠{opacity:0;transition:opacity .2s}
/* 휴식력 칸 — 저장이 안 되면 칸 옆에 흐린 '저장 안 됨' */
.기준설정{grid-template-columns:60px 64px minmax(0,1fr) auto}
.기준저장{font-size:11px;opacity:.75;white-space:nowrap}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]
날.write_text(s, encoding='utf-8')
print('✅', 날, len(s))
