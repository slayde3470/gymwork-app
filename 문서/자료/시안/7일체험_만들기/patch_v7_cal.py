"""7day-v5.html → 캘린더 · 종목 탭 · 플랜 고치기 시트 (10-03 홍겸 님 ✎ 표시 5개)
   python3 patch_v7_cal.py 입력.html 출력.html

표시 → 고친 것
 pd7g 캘린더 "가운데로 옮기기. 왼쪽 끝단 박스엔 '스탯' 오른쪽 끝단 박스엔 '업적'"
      → 년월 띠를 세 칸(1fr · auto · 1fr)으로: [스탯] ‹ 2026년 10월 › [업적]. 년월은 언제나 한가운데.
        [스탯] = 스탯열기 data-v="스탯", [업적] = 스탯열기 data-v="업적" (행동 case 는 다른 사람이 data-v 를 읽게 고친다)
        [오늘] 은 날짜 판 띠로 옮겼다 — 아래 '왜' 참고
 upy8 캘린더 "구분선이 있으면 좋겠어" → 날 칸 사이 1px --선 (가로 · 세로). 칸 높이 64 그대로
 c77y 캘린더 "띠를 만들고, 맨 오른쪽으로 [변경] 박스 옮기기." → 날짜 판 제목을 .띠 로, 그 오른쪽 끝에 [변경]
        (예정 없는 날은 [루틴 넣기] [제안] · 기록만 있는 지난 날은 비움)
 qk0z 종목   "종목추가버튼은 맨 위 띠 박스 바로 아래에 위치해야함." → '종목 추가' 칸을 맨 아래에서 제목 바로 아래로
 bdtv 고침   "한칸에 이름 · 오른쪽에 목표 1RM 이나 무게×횟수 · '지금 실력 넣기' 는 좋은 표현? 현재 수행능력 ·
             큰 칸 필요? · 14회차는 표시할 필요 없음"
      → 첫 줄 = 이름(왼쪽) | 목표 [1RM][무게 × 횟수] + 입력(오른쪽) · '현재 수행능력' 한 줄 이름표 + 한 줄 입력 · 회차 글 삭제
"""
import sys, pathlib
들, 날 = pathlib.Path(sys.argv[1]), pathlib.Path(sys.argv[2])
s = 들.read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:80]!r}: {c}번")
    s = s.replace(old, new)

# ══ pd7g · upy8 — 캘린더 년월 띠 · 날 칸 구분선 ══
# 왜 [오늘] 을 년월 띠에서 뺐나: 360 폭에서 띠 안쪽은 302px — [스탯] 48 · ‹ 32 · 년월 110 · › 32 · [업적] 48 에 틈까지 더하면 꽉 찬다.
#   [오늘](48)까지 넣으면 넘치거나, 넣는 순간 ‹ › 가 옆으로 밀린다. ‹ › 가 밀리면 › 를 연달아 누를 때
#   두 번째 손가락이 [오늘] 에 떨어져 이번 달로 되돌아간다. 그래서 화살표 자리를 고정하고 [오늘] 은 날짜 판 띠로 보냈다.
# 왜 끝 주를 빈 칸으로 채우나 (upy8): 구분선이 날이 있는 칸에서만 끊기지 않고 달력 전체가 반듯한 격자가 되게
바꿈('''  return `<div class="띠 번호" data-drop="달"${번("캘0")}><button data-act="달" data-d="-1" aria-label="이전 달">‹</button><b class="채움">${y}년 ${m}월</b>${달!==오.slice(0,7)?`<button class="작은흰" data-act="오늘달">오늘</button>`:""}<button data-act="달" data-d="1" aria-label="다음 달">›</button><button class="작은흰 칭호칩 번호" data-act="스탯열기" aria-label="스탯 · 업적"${번("캘칩")}>${대표()?`『${esc(대표().칭호)}』`:"스탯"}</button></div>''',
     '''  for(let i=(앞+날수)%7; i&&i<7; i++) 칸+=`<div class="칸날 딴달"></div>`;   /* upy8 끝 주 채움 — 구분선이 반듯한 격자가 되게 */
  /* pd7g 왼쪽 끝 [스탯] · 가운데 ‹ 년월 › · 오른쪽 끝 [업적]. [오늘] 은 날짜 판 띠에 (위 패치 주석) */
  return `<div class="띠 번호 년월띠" data-drop="달"${번("캘0")}><button class="작은흰 번호" data-act="스탯열기" data-v="스탯"${번("캘칩")}>스탯</button><span class="년월"><button class="달넘김" data-act="달" data-d="-1" aria-label="이전 달">‹</button><b>${y}년 ${m}월</b><button class="달넘김" data-act="달" data-d="1" aria-label="다음 달">›</button></span><button class="작은흰" data-act="스탯열기" data-v="업적">업적</button></div>''')

# ══ c77y — 날짜 판 제목 = 띠 · 그 오른쪽 끝에 [변경] ══
바꿈('''function 날판(k){ const 오=오늘(), 록=기록목록(k);
  let h=`<div class="판 번호" data-enter="판${k}"${번("캘1")}><b class="큰">${날글(k)} (${요일(k)})${k===오?" · 오늘":""}</b>`;''',
     '''/* c77y 날짜 판 제목을 띠로 — 오른쪽 끝 단추: 예정이 있으면 [변경] · 없으면 [루틴 넣기] · 기록만 있는 날은 비움.
   pd7g 로 년월 띠에서 빠진 [오늘] 은 다른 달을 볼 때 그 왼쪽에 (같은 행동 '오늘달') */
function 날판띠(k, 예, r){ const 오=오늘(), 다른달=(U.보는달||오.slice(0,7))!==오.slice(0,7);
  const 오른 = !예 ? "" : r ? `<button class="작은흰" data-act="변경" data-k="${k}">변경</button>`
    : `<button class="작은흰" data-act="시트" data-t="루틴고르기" data-k="${k}">루틴 넣기</button>`;
  return `<div class="띠 판띠"><b class="채움 한줄">${날글(k)} (${요일(k)})${k===오?" · 오늘":""}</b>${다른달?`<button class="작은흰" data-act="오늘달">오늘</button>`:""}${오른}</div>`; }
function 날판(k){ const 오=오늘(), 록=기록목록(k), 예=k>=오 && !(k===오&&록.length);
  let h=`<div class="판 번호" data-enter="판${k}"${번("캘1")}>${날판띠(k, 예, 루틴(S.예정[k]))}`;''')
바꿈('''  if(k>=오 && !(k===오&&록.length)){
    if(r){ const 실=실제루틴(r);
      h+=`<div class="예머리"><b class="예이름">${esc(r.이름)}</b>${r.휴식일?`<span class="예칩"><span>휴식일</span></span>`:예정칩(실)}<button class="버튼 낮" data-act="변경" data-k="${k}">변경</button></div>''',
     '''  if(예){
    if(r){ const 실=실제루틴(r);
      h+=`<div class="예머리"><b class="예이름">${esc(r.이름)}</b>${r.휴식일?`<span class="예칩"><span>휴식일</span></span>`:예정칩(실)}</div>''')
바꿈('''    else h+=`<div class="예머리"><span class="예이름 흐림 작">예정 없음</span><span class="예칩"></span><button class="버튼 낮" data-act="시트" data-t="루틴고르기" data-k="${k}">루틴 넣기</button></div>`;''',
     '''    else h+=`<div class="예머리"><span class="예이름 흐림 작">예정 없음</span></div>`;''')

# ══ qk0z — 종목 추가 칸을 맨 위 제목 바로 아래로 ══
바꿈('''  return `<div class="넘김"><h1>종목</h1>
    <div class="이름표">운동 플랜</div>''',
     '''  return `<div class="넘김"><h1>종목</h1>
    <div class="이름표">종목 추가</div><div class="카드 쌓기" style="border-width:1px">${칩줄("새종목칸","",S.카테고리,U.새종목칸)}<div class="줄"><input class="입력 채움" data-in="새종목" placeholder="종목 이름" value="${esc(U.새종목)}"><button class="버튼 주" data-act="종목만들기">넣기</button></div></div>
    <div class="이름표">운동 플랜</div>''')
바꿈('''}).join("")}
    <div class="이름표">종목 추가</div><div class="카드 쌓기" style="border-width:1px">${칩줄("새종목칸","",S.카테고리,U.새종목칸)}<div class="줄"><input class="입력 채움" data-in="새종목" placeholder="종목 이름" value="${esc(U.새종목)}"><button class="버튼 주" data-act="종목만들기">넣기</button></div></div></div>`; }''',
     '''}).join("")}</div>`; }''')

# ══ bdtv — 플랜 고치기 시트 다시 짜기 (머리 띠 · [저장][닫기] 줄은 그대로) ══
바꿈('''    ${맨몸(t)?`<div class="줄">${고침입력("이름","이름","",true)}${고침입력("목표개수","목표 개수","회")}</div>`
      :`<div class="줄">${고침입력("이름","이름","",true)}</div>
    ${칩줄("고침목표방식","",["RM","회"],g.목표방식,v=>v==="RM"?"1RM":"무게 × 횟수")}
    <div class="줄">${고침입력("목표무게",g.목표방식==="RM"?"목표 1RM":"목표 무게","kg")}${g.목표방식==="회"?고침입력("목표횟수","횟수","회"):""}</div>`}''',
     '''    <div class="고침첫줄">${고침입력("이름","이름","",true)}${맨몸(t)?고침입력("목표개수","목표 개수","회")
      :`<div class="고침목표">${칩줄("고침목표방식","",["RM","회"],g.목표방식,v=>v==="RM"?"1RM":"무게 × 횟수")}<div class="줄">${고침칸("목표무게","kg",g.목표방식==="RM"?"목표 1RM":"무게")}${g.목표방식==="회"?고침칸("목표횟수","회","횟수"):""}</div></div>`}</div>''')
# '지금 실력 넣기' 회색 큰 상자 → '현재 수행능력' 한 줄 이름표 + 한 줄 입력. '· 14회차'(지금까지 운동한 회차)는 뺀다 — 홍겸 님 "표시할 필요 없음"
#   '지금 85kg' 이 1RM 인지 헷갈렸다 → '지금 1RM 85kg' (맨몸은 '지금 최대 1회')
바꿈('''    <div class="회색칸 쌓기"><div class="줄"><b class="작 채움">지금 실력 넣기</b><span class="아주작 흐림">지금 ${값글(t,지금진행값(p))} · ${p.한회}회차</span></div>
      <div class="줄">${맨몸(t)?고침입력("측정횟수","정자세 개수","회"):고침입력("측정무게","무게","kg")+고침입력("측정횟수","횟수","회")}<button class="버튼" data-act="측정넣기" style="align-self:flex-end">넣기</button></div></div>''',
     '''    <div class="줄 아주작 옅음 고침이름표"><span class="채움">현재 수행능력</span><span class="숫">지금 ${맨몸(t)?"최대":"1RM"} ${값글(t,지금진행값(p))}</span></div>
    <div class="줄">${맨몸(t)?고침칸("측정횟수","회","정자세 개수"):고침칸("측정무게","kg","무게")+고침칸("측정횟수","회","횟수")}<button class="버튼" data-act="측정넣기">넣기</button></div>''')
# 이름표 없이 칸 안에 자리글(placeholder)로 뜻을 적는 입력 — 한 줄에 무게 · 횟수 · 넣기가 들어가게
바꿈('''function 고침시트(){''',
     '''function 고침칸(f,단위,자리){ return `<span class="입력줄 채움"><input class="입력" data-in="고침" data-f="${f}" inputmode="decimal" placeholder="${자리}" aria-label="${자리}" value="${esc(U.고침[f])}">${단위?`<em>${단위}</em>`:""}</span>`; }
function 고침시트(){''')
바꿈('''발자취(`지금 실력 넣기 · ${p.이름}`);''', '''발자취(`현재 수행능력 · ${p.이름}`);''')

css = '''
/* ═══ 10-03 ✎ 표시 — 캘린더 · 종목 · 플랜 고치기 (patch_v7_cal) ═══ */
/* pd7g 년월 띠 = 세 칸. 양쪽 칸이 같은 1fr 이라 년월이 언제나 한가운데 · ‹ › 자리가 달마다 안 움직인다 */
.띠.년월띠{display:grid;grid-template-columns:1fr auto 1fr;gap:8px}
.년월띠>.작은흰:first-child{justify-self:start}
.년월띠>.작은흰:last-child{justify-self:end}
.년월{display:flex;align-items:center;gap:4px}
.년월 b{white-space:nowrap;font-variant-numeric:tabular-nums}
.달넘김{width:32px;height:32px;border-radius:8px;font-size:18px;display:inline-flex;align-items:center;justify-content:center;color:var(--강조글)}
/* upy8 날 칸 구분선 — 칸의 위 · 왼쪽 가장자리에 1px --선 (첫 열은 왼쪽 없음). 테두리(border)는 고름 · 끌기 표시가 쓰므로
   선은 ::after 로 그린다. 칸 모서리는 0 — 둥근 칸이면 선이 휜다 */
.달력{gap:0}
.칸날{border-radius:0}
.칸날::after{content:"";position:absolute;inset:-1px;border-top:1px solid var(--선);pointer-events:none}
.달력>.칸날:not(:nth-child(7n+1))::after{border-left:1px solid var(--선)}
.칸날.고름::after{display:none}
.칸날.딴달{visibility:visible}
/* c77y 날짜 판 제목 띠 — .띠 그대로(강조 바탕 · 강조글 · 40 · 18 Bold), 판 안쪽 여백만큼 밖으로 꺼내 폭을 꽉 채운다 */
.판 .판띠{margin:-10px -12px 0}
.판띠+.예머리,.판띠+.판줄{border-top:0}
/* bdtv 고침 첫 줄 — 이름(2) | 목표(3). 목표 칸에 칩줄 [1RM][무게 × 횟수] + 무게 · 횟수 두 칸이 360 폭에서도 들어가게 3 */
.고침첫줄{display:grid;grid-template-columns:minmax(0,2fr) minmax(0,3fr);gap:8px;align-items:end}
.고침목표{display:flex;flex-direction:column;gap:4px;min-width:0}
.고침목표 .칩줄{flex-wrap:nowrap}
.고침목표 .줄{gap:4px}
.입력줄.채움{min-width:0}
.고침이름표{margin-top:4px}
/* 고침 시트 입력 글자는 모두 15 (U2-6 입력) — label.칸(11) 안의 입력이 11 을 물려받아 이름 · 목표가 오른쪽 칸과 달라 보였다 */
[data-in="고침"].입력{font-size:15px}
/* 360 폭에서 목표 무게 칸에 '102.5' 가 잘리지 않게 안쪽 여백 8 */
.고침목표 .입력{padding:0 8px}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]
날.write_text(s, encoding='utf-8')
print('✅', 날, len(s))
