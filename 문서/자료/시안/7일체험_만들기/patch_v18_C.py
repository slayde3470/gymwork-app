"""v18 C (10-04 홍겸 님) — 종목 탭 · 새 종목 만들기 시트 · 종목 사전 · 기본 세팅 세트 줄
① 종목 탭 맨 위 제목 h1 → 띠(.띠, 글 가운데)
② 카테고리 묶음 상자 없앰 → 종목마다 따로 상자. 접힌 상자 2열 격자 · 펼친 상자 줄 전체(grid-column:1/-1).
   카테고리 = 맨 위 칩 필터([전체] + S.카테고리) · [전체]일 때 상자 위 작은 이름표(11)
③ 이름 앞 사진 칸 = 사진 넣기(file input · 기존 사진넣기). 펼친 칸의 [사진 추가] 단추 삭제 (넣은 사진 줄 · 두 번 눌러 지우기는 그대로)
④ 기본 세팅 = 세트 줄 목록 [번호][kg][회][휴식][휴지통] + 아래 [+ 세트](앞 줄 값 복사). 설명 글 삭제.
   S.종목설정[이름] = {세트:[{w,r,휴}…]} · 옛 꼴 {세트:n,w,r,휴} 은 읽을 때 옮긴다. 종목넣기(루틴 · 운동)가 이 목록을 그대로
⑤ 새 종목 만들기 시트 — 이름 칸 오른쪽 끝 돋보기(쉬면 '찾기 켜기' · 켜지면 '확인') · 초성 검색 · 종목 사전 ·
   카테고리 → 세부 부위(근육 지도에서 칠해지는 부위만) · 부위마다 주동/보조/협응 · 저장하면 S.종목표 + 근육 → 근육 피로 지도가 그대로 쓴다.
   '종목 넣기' 시트의 [+ 새 종목 만들기](data-act="새종목열기", D)에서 열면 저장 · 닫기 후 그 시트로 돌아간다"""
import sys, pathlib
IN, OUT = sys.argv[1], sys.argv[2]
s = pathlib.Path(IN).read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:70]!r}: {c}번")
    s = s.replace(old, new)
def 사이바꿈(앞, 뒤, new):
    """앞(포함) ~ 뒤(제외) 를 new 로. 둘 다 정확히 한 번"""
    global s
    for m in (앞, 뒤):
        if s.count(m) != 1: raise SystemExit(f"❌ {m[:70]!r}: {s.count(m)}번")
    i = s.index(앞); j = s.index(뒤)
    if j <= i: raise SystemExit("❌ 순서")
    s = s[:i] + new + s[j:]

# ── 종목 사전 (패치 안 문자열) ─────────────────────────────────────────
사전 = r'''
벤치프레스|가슴|바벨 벤치프레스/플랫 벤치/벤치|
인클라인 벤치프레스|가슴|인클라인 바벨 프레스|
디클라인 벤치프레스|가슴|디클라인 바벨 프레스|
덤벨 벤치프레스|가슴|덤벨 프레스|
인클라인 덤벨 프레스|가슴|인클라인 덤벨 벤치프레스|
디클라인 덤벨 프레스|가슴||
스미스 머신 벤치프레스|가슴|스미스 벤치|
스미스 머신 인클라인 프레스|가슴|스미스 인클라인|
체스트 프레스 머신|가슴|머신 체스트 프레스/체스트 프레스|
인클라인 체스트 프레스 머신|가슴|머신 인클라인 프레스|
케이블 체스트 프레스|가슴||
덤벨 플라이|가슴|플랫 덤벨 플라이|
인클라인 덤벨 플라이|가슴||chest_upper:P,chest_mid:S,delt_front:Y
펙 덱 플라이|가슴|펙덱/버터플라이/머신 플라이|
케이블 크로스오버|가슴|케이블 플라이|
로우 투 하이 케이블 플라이|가슴|로우 케이블 플라이|chest_upper:P,chest_mid:S,delt_front:Y
덤벨 풀오버|가슴|풀오버|chest_mid:P,lats:S,serratus:S,triceps:Y
플로어 프레스|가슴|바벨 플로어 프레스|chest_mid:P,triceps:S,delt_front:Y
턱걸이|등|풀업/와이드 그립 풀업|
친업|등|언더그립 턱걸이/친 업|
어시스티드 풀업 머신|등|어시스트 풀업/어시스티드 턱걸이|
랫풀다운|등|랫 풀다운/와이드 그립 랫풀다운|
클로즈그립 랫풀다운|등|V바 랫풀다운/클로즈 그립 랫풀다운|
언더그립 랫풀다운|등|리버스 그립 랫풀다운|
스트레이트 암 풀다운|등|암 풀다운/스트레이트 암 케이블 풀다운|lats:P,teres_major:S,triceps:Y
바벨 로우|등|벤트오버 로우/벤트 오버 바벨 로우|
펜들레이 로우|등||
덤벨 로우|등|원암 덤벨 로우/원 암 로우|
시티드 케이블 로우|등|시티드 로우/케이블 로우|
T바 로우|등|티바 로우|
머신 로우|등|시티드 로우 머신/로우 머신|
체스트 서포티드 로우|등|인클라인 벤치 로우|
랙 풀|등|랙풀|lower_back:P,glutes:P,traps:S,hamstrings:S,lats:Y,forearm:Y
바벨 슈러그|등|슈러그|
덤벨 슈러그|등||
백 익스텐션|등|하이퍼 익스텐션/로만 체어|
백 스쿼트|하체|바벨 스쿼트/스쿼트|
프론트 스쿼트|하체|프런트 스쿼트|quads:P,glutes:S,adductors:S,lower_back:S,abs:Y
고블릿 스쿼트|하체||
스미스 머신 스쿼트|하체|스미스 스쿼트|
핵 스쿼트|하체|핵 스쿼트 머신|
박스 스쿼트|하체||
불가리안 스플릿 스쿼트|하체|불가리안/스플릿 스쿼트|
덤벨 런지|하체|런지|
워킹 런지|하체||
리버스 런지|하체||quads:P,glutes:P,adductors:S,hamstrings:Y
사이드 런지|하체||adductors:P,quads:P,glutes:S
덤벨 스텝업|하체|스텝업/박스 스텝업|quads:P,glutes:P,hamstrings:Y
레그 프레스|하체|레그 프레스 머신|
레그 익스텐션|하체||
라잉 레그 컬|하체|레그 컬|
시티드 레그 컬|하체||
데드리프트|하체|컨벤셔널 데드리프트/바벨 데드리프트|
스모 데드리프트|하체||glutes:P,adductors:P,quads:S,hamstrings:S,lower_back:S,traps:Y,forearm:Y
트랩바 데드리프트|하체|헥스바 데드리프트|
루마니안 데드리프트|하체|RDL/루마니안|
스티프 레그 데드리프트|하체|스티프 데드리프트|
굿모닝|하체|굿모닝 엑서사이즈|
힙 쓰러스트|하체|바벨 힙 쓰러스트/힙쓰러스트|
케이블 풀 스루|하체||glutes:P,hamstrings:S
케이블 킥백|하체|글루트 킥백/힙 킥백|glutes:P,hamstrings:Y
힙 어브덕션 머신|하체|어브덕션/힙 어브덕터|glutes:P
힙 어덕션 머신|하체|어덕션/힙 어덕터|adductors:P
스탠딩 카프 레이즈|하체|카프 레이즈|
시티드 카프 레이즈|하체||
노르딕 햄스트링 컬|맨몸|노르딕 컬|hamstrings:P,glutes:Y
오버헤드 프레스|어깨|OHP/밀리터리 프레스/바벨 숄더 프레스|
덤벨 숄더 프레스|어깨|시티드 덤벨 숄더 프레스|
머신 숄더 프레스|어깨|숄더 프레스 머신|
스미스 머신 숄더 프레스|어깨|스미스 숄더 프레스|
아놀드 프레스|어깨||delt_front:P,delt_side:S,triceps:S
비하인드 넥 프레스|어깨||delt_front:P,delt_side:P,triceps:S,traps:Y
푸시 프레스|어깨|푸쉬 프레스|delt_front:P,triceps:S,delt_side:S,quads:Y,glutes:Y
랜드마인 프레스|어깨||delt_front:P,chest_upper:S,triceps:S,serratus:Y
사이드 레터럴 레이즈|어깨|사레레/사이드 레이즈/덤벨 레터럴 레이즈|
케이블 레터럴 레이즈|어깨|케이블 사이드 레이즈|
머신 레터럴 레이즈|어깨|머신 사이드 레이즈|
덤벨 프론트 레이즈|어깨|프론트 레이즈/프런트 레이즈|
케이블 프론트 레이즈|어깨||
리어 델트 레이즈|어깨|벤트오버 레터럴 레이즈/벤트 오버 레이즈|
리버스 펙 덱 플라이|어깨|리버스 펙덱/리어 델트 머신|
케이블 리어 델트 플라이|어깨||
페이스 풀|어깨|페이스풀|delt_rear:P,rhomboids:S,traps:S
업라이트 로우|어깨||delt_side:P,traps:P,delt_front:S,biceps:Y
바벨 컬|팔|바벨 바이셉스 컬|
덤벨 컬|팔|덤벨 바이셉스 컬/얼터네이트 컬|
EZ바 컬|팔|이지바 컬|
해머 컬|팔|덤벨 해머 컬|
프리처 컬|팔|프리처 컬 머신|
인클라인 덤벨 컬|팔||biceps:P,brachialis:S,forearm:Y
컨센트레이션 컬|팔||
케이블 컬|팔|케이블 바이셉스 컬|
스파이더 컬|팔||
리버스 컬|팔|리버스 바벨 컬|forearm:P,brachialis:P,biceps:S
리스트 컬|팔|손목 컬|forearm:P
트라이셉스 푸시다운|팔|케이블 푸시다운/푸쉬다운|
로프 푸시다운|팔|로프 트라이셉스 푸시다운|
오버헤드 트라이셉스 익스텐션|팔|덤벨 오버헤드 익스텐션|triceps:P
케이블 오버헤드 익스텐션|팔|케이블 오버헤드 트라이셉스 익스텐션|triceps:P
스컬 크러셔|팔|라잉 트라이셉스 익스텐션/EZ바 스컬 크러셔|triceps:P
덤벨 킥백|팔|트라이셉스 킥백|
클로즈그립 벤치프레스|팔|클로즈 그립 벤치프레스/내로우 그립 벤치|triceps:P,chest_mid:S,delt_front:S
벤치 딥스|맨몸|체어 딥스|triceps:P,delt_front:S,chest_lower:Y
팔굽혀펴기|맨몸|푸시업/푸쉬업|
인클라인 푸시업|맨몸||chest_lower:P,chest_mid:S,triceps:S,delt_front:Y
디클라인 푸시업|맨몸||chest_upper:P,chest_mid:S,delt_front:S,triceps:S
다이아몬드 푸시업|맨몸|클로즈 푸시업|triceps:P,chest_mid:P,delt_front:S
파이크 푸시업|맨몸||delt_front:P,triceps:S,delt_side:Y
딥스|맨몸|평행봉 딥스|
인버티드 로우|맨몸|오스트레일리안 풀업|
맨몸 스쿼트|맨몸|에어 스쿼트|
점프 스쿼트|맨몸||quads:P,glutes:P,calves:S
피스톨 스쿼트|맨몸||quads:P,glutes:P,adductors:S,abs:Y
글루트 브릿지|맨몸|브릿지/힙 브릿지|
플랭크|맨몸||
사이드 플랭크|맨몸||obliques:P,abs:S,glutes:Y
크런치|맨몸||
리버스 크런치|맨몸||abs:P,obliques:Y
싯업|맨몸|윗몸일으키기|
행잉 레그 레이즈|맨몸||
라잉 레그 레이즈|맨몸|레그 레이즈|
바이시클 크런치|맨몸||abs:P,obliques:P
러시안 트위스트|맨몸||
앱 롤아웃|맨몸|AB 롤아웃/휠 롤아웃|abs:P,lats:S,obliques:S,delt_front:Y
마운틴 클라이머|맨몸||abs:P,obliques:S,delt_front:Y,quads:Y
버피|맨몸|버피 테스트|quads:P,chest_mid:S,delt_front:S,triceps:Y,abs:Y
데드 버그|맨몸|데드버그|abs:P,obliques:Y
할로우 바디 홀드|맨몸|할로우 홀드|abs:P,obliques:Y
슈퍼맨|맨몸|슈퍼맨 익스텐션|lower_back:P,glutes:S
'''.strip()
assert '`' not in 사전 and '${' not in 사전

# ── ⑤ 종목 데이터 — 종목표에 근육을 적어 둔 종목은 그것을 먼저 (근육 피로 지도 · 확대 그림 · 근육 기준이 모두 이 함수를 쓴다) ──
바꿈('''function 종목근육(이름){ for(const [k,m] of 근육규칙.규칙) if(k.some(w=>이름.includes(w))) return m;''',
     '''function 종목근육(이름){ const 표0=S.종목표.find(x=>x.이름===이름); if(표0&&표0.근육&&Object.keys(표0.근육).length) return 표0.근육;   // 10-04 v18 C ⑤ 새 종목 시트에서 고른 근육
  for(const [k,m] of 근육규칙.규칙) if(k.some(w=>이름.includes(w))) return m;''')

# U 처음값
바꿈('''U.플랜폼 = 폼초기(); U.종목펼침 = null;''',
     '''U.플랜폼 = 폼초기(); U.종목펼침 = null;
U.종목칸고름 = "전체"; U.새 = null;   // 10-04 v18 C 종목 탭 칩 필터 · 새 종목 시트 상태''')

# ── 종목 탭 (A 의 기본 세팅 ~ 종목탭 끝) 전체를 새로 ──
새코드 = r'''const 종목세트최대=10;
/* ═══ 10-04 v18 C ④ 기본 세팅 = 세트 줄 목록 — "기본세팅 - 세트 -+ 버튼을 없애고 운동화면처럼 '세트추가' 버튼을 아래줄에"
   S.종목설정[이름] = {세트:[{w,r,휴}…]}. 옛 꼴(v17 {세트:n,w,r,휴})은 읽을 때 n 줄로 옮긴다. 없으면 설정의 기본 세트 수 × 20kg · 10회 · 기본 휴식.
   종목넣기(루틴 · 운동)가 이 목록을 그대로 쓴다(운동에 넣을 땐 운세트로 → 목r · 완료:false). 플랜넣기는 그대로 플랜 처방 */
function 종목기본세트(이름){ const b=S.종목설정?.[이름];
  if(b && Array.isArray(b.세트) && b.세트.length) return b.세트.map(x=>({w:+x.w||0, r:Math.max(1,+x.r||1), 휴:+x.휴||S.설정.기본휴식}));
  if(b && typeof b.세트==="number") return 세트들(Math.max(1,Math.min(종목세트최대,b.세트)), b.w??20, b.r??10, b.휴??S.설정.기본휴식);
  return 세트들(S.설정.기본세트,20,10,S.설정.기본휴식); }
function 종목설정잡기(이름){ S.종목설정 ||= {}; const b=S.종목설정[이름];
  if(!b || !Array.isArray(b.세트) || !b.세트.length) S.종목설정[이름]={세트:종목기본세트(이름)}; return S.종목설정[이름]; }
function 종목세팅(이름, 플){ const 세=종목기본세트(이름), v=esc(이름), 하나=세.length<=1;
  const 칸=(f,k,글,입,말)=>`<div class="값칸"><button data-act="종세트값" data-v="${v}" data-k="${k}" data-f="${f}" data-d="-1" aria-label="${k+1}세트 ${말} 빼기">−</button>${입?`<input data-in="종세트" data-v="${v}" data-k="${k}" data-f="${f}" inputmode="decimal" aria-label="${k+1}세트 ${말}" value="${글}">`:`<span class="숫">${글}</span>`}<button data-act="종세트값" data-v="${v}" data-k="${k}" data-f="${f}" data-d="1" aria-label="${k+1}세트 ${말} 더하기">＋</button></div>`;
  return `<div class="종설"><div class="종설제목"><b>기본 세팅</b>${플?`<span class="아주작 옅음 한줄">플랜으로 넣으면 플랜 처방대로</span>`:""}</div>
    <div class="종세트머리 아주작 옅음"><span>세트</span><span>무게 kg</span><span>횟수</span><span>휴식</span><span></span></div>
    ${세.map((x,k)=>`<div class="종세트"><span class="k 숫">${k+1}</span>${칸("w",k,kg(x.w),true,"무게")}${칸("r",k,x.r,true,"횟수")}${칸("휴",k,분초(x.휴),false,"휴식")}<button class="종지움" data-act="종세트지움" data-v="${v}" data-k="${k}" aria-label="${k+1}세트 지우기"${하나?" disabled":""}>${아이콘.휴지통}</button></div>`).join("")}
    <button class="버튼 낮 넓" data-act="종세트더" data-v="${v}">+ 세트</button></div>`; }

/* ═══ 10-04 v18 C ⑤ 종목 사전 — 새 종목 시트의 초성 검색에 쓴다 ═══
   [확인 필요] 표준 종목명 · 근육 배정은 Claude 초안 (한국 헬스장에서 흔히 쓰는 표기 · 바벨 · 덤벨 · 머신 · 케이블 · 맨몸). 출처 확인 전.
   한 줄 = 이름 | 칸(S.카테고리) | 다른 이름(검색용 · '/' 로 가름) | 근육(id:역할 — 비우면 근육규칙의 낱말 규칙 → 세부 부위로 옮김)
   역할 = 근육규칙.역할 그대로 P 주동 1.0 · S 보조 0.5 · Y 협응 0.25 */
const 종목사전글 = `@@사전@@`;
const 종목사전 = 종목사전글.trim().split("\n").map(l=>{ const [이름,칸,별,근]=l.split("|").map(x=>(x||"").trim());
  return {이름, 칸, 별:별?별.split("/").map(x=>x.trim()).filter(Boolean):[], 근:근?Object.fromEntries(근.split(",").map(x=>x.split(":").map(y=>y.trim()))):null}; });
/* 세부 부위 — 근육 지도(몸조각)에서 실제로 칠해지는 부위만. 키 = 근육부모 · 근육이름 · 근육규칙이 쓰는 id 그대로.
   몸조각의 칠 조각 키(scm 목 · 대신 맡은 부위 일부)는 운동 종목에 거의 안 쓰여 뺐다 */
const 세부부위 = [["가슴",["chest_upper","chest_mid","chest_lower","serratus"]],["등",["lats","rhomboids","teres_major","traps","lower_back"]],
  ["어깨",["delt_front","delt_side","delt_rear"]],["팔",["biceps","brachialis","triceps","forearm"]],["복근",["abs","obliques"]],
  ["하체",["quads","hamstrings","glutes","adductors","calves","shin"]]];
const 세부키 = new Set(세부부위.flatMap(([,l])=>l));
const 칸묶음 = {가슴:"가슴", 등:"등", 하체:"하체", 어깨:"어깨", 팔:"팔"};
const 역할짧은 = {P:"주동", S:"보조", Y:"협응"}, 역순 = {P:3, S:2, Y:1};
const 근이름 = id => (근육이름[id]||id).replace(/ \(.*\)/,"");
/* 낱말 규칙 → 세부 부위 키로: 그 키면 그대로 · 조상이 세부 부위면 조상으로 · 자손이 세부 부위면 그 자손 모두로 (역할은 센 쪽) */
function 세부로(m){ const o={}, 넣=(k,r)=>{ if(!o[k]||역순[r]>역순[o[k]]) o[k]=r; };
  for(const [id,r] of Object.entries(m||{})){ if(세부키.has(id)){ 넣(id,r); continue; }
    let p=근육부모[id]; while(p&&!세부키.has(p)) p=근육부모[p]; if(p){ 넣(p,r); continue; }
    for(const k of 세부키){ let q=근육부모[k]; while(q&&q!==id) q=근육부모[q]; if(q===id) 넣(k,r); } }
  return o; }
function 낱말근육(이름){ for(const [k,m] of 근육규칙.규칙) if(k.some(w=>이름.includes(w))) return m; return null; }
const 사전근육 = x => x.근 ? {...x.근} : 세부로(낱말근육(x.이름) || 근육규칙.부위[x.칸] || {});
/* 근육 두 줄 — "주동근 : … / 협응근 : …" (협응근 줄 = 보조 S + 협응 Y) */
function 근육두줄(m){ const 줄=rs=>Object.entries(m||{}).filter(([,r])=>rs.includes(r)).sort((a,b)=>역순[b[1]]-역순[a[1]]).map(([id])=>근이름(id)).join(", ")||"—";
  return `<span class="근줄">주동근 : ${esc(줄(["P"]))}</span><span class="근줄">협응근 : ${esc(줄(["S","Y"]))}</span>`; }
/* 초성 검색 — 'ㅂㅊㅍ' → 벤치프레스 · '벤ㅊ' · '프레스' 처럼 섞어도 · 띄어쓰기 무시 · 부분 일치.
   치는 중인 마지막 글자에 받침이 붙어 있으면('벤치플') 받침을 다음 초성으로도 본다 */
const 초성표="ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ", 받침표="ㄱㄲㄳㄴㄵㄶㄷㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅄㅅㅆㅇㅈㅊㅋㅌㅍㅎ";
const 초성 = c => { const n=c.charCodeAt(0)-0xAC00; return n>=0&&n<11172 ? 초성표[Math.floor(n/588)] : c; };
const 다듬 = t => String(t??"").toLowerCase().replace(/\s+/g,"");
function 초성자리(글, q){ const a=[...다듬(글)], b=[...q]; if(!b.length) return -1;
  for(let i=0;i+b.length<=a.length;i++){ let 맞=true;
    for(let j=0;j<b.length;j++){ const x=a[i+j], y=b[j]; if(!(x===y || (초성표.includes(y) && 초성(x)===y))){ 맞=false; break; } }
    if(맞) return i; }
  return -1; }
function 검색꼴(q){ q=다듬(q); if(!q) return []; const 꼴=[q], c=q.at(-1), n=c.charCodeAt(0)-0xAC00;
  if(n>=0&&n<11172&&n%28){ const 받=받침표[n%28-1]; if(초성표.includes(받)) 꼴.push(q.slice(0,-1)+String.fromCharCode(c.charCodeAt(0)-n%28)+받); }
  return 꼴; }
function 종목찾기(q){ const 꼴=검색꼴(q), 결=[]; if(!꼴.length) return 결;
  const 점수=(이름들)=>{ let best=-1; 이름들.forEach((글,n)=>꼴.forEach(qq=>{ const i=초성자리(글,qq); if(i<0) return; const 점=(i===0?0:2)+(n?1:0); if(best<0||점<best) best=점; })); return best; };
  for(const x of 종목사전){ const 점=점수([x.이름,...x.별]); if(점>=0) 결.push({x,점}); }
  for(const t of S.종목표) if(!종목사전.some(x=>x.이름===t.이름)){ const 점=점수([t.이름]); if(점>=0) 결.push({x:{이름:t.이름,칸:t.칸,별:[],근:null},점}); }   // 직접 만든 종목도
  결.sort((a,b)=>a.점-b.점||a.x.이름.length-b.x.이름.length||a.x.이름.localeCompare(b.x.이름,"ko"));
  return 결.slice(0,8).map(r=>r.x); }

/* ═══ 10-04 v18 C ⑤ 새 종목 시트 ═══
   [이름 칸 ··· 돋보기] — 쉬는 동안 돋보기 = 찾기 켜기(칸에 손가락이 가도 켜진다). 켜지면 그 자리가 [확인] — 친 이름으로 정한다
   (사전 이름 · 다른 이름과 똑같으면 그 사전 종목 · 아니면 직접 만든 종목). 찾은 줄을 눌러도 정해진다.
   정해지면 카테고리 칩 → 세부 부위(묶음 칩 + 부위 칩) · 역할 칩(주동 · 보조 · 협응)을 골라 두고 부위를 누르면 그 역할, 같은 역할을 다시 누르면 뺀다.
   사전에서 고르면 칸 · 부위 · 역할이 미리 채워지고 바꿀 수 있다. 저장 → S.종목표 {이름, 칸, 근육} · 돌아감이 있으면 그 시트로 */
const 새초기 = ()=>({찾는중:false, 고름:false, 사전:null, 근육:{}, 묶음:"가슴", 역할:"P"});
function 기본묶음(){ const 새=U.새, P=Object.entries(새.근육).find(([,r])=>r==="P")?.[0];
  return 칸묶음[U.새종목칸] || (P && 세부부위.find(([,l])=>l.includes(P))?.[0]) || 세부부위[0][0]; }
function 사전적용(x){ U.새종목=x.이름; if(S.카테고리.includes(x.칸)) U.새종목칸=x.칸; U.새.근육=사전근육(x); U.새.사전=x.이름; U.새.묶음=기본묶음(); }
function 새확인(){ const n=String(U.새종목||"").trim(); if(!n){ 토스트("이름을 넣어 주세요"); return false; }
  const x=종목사전.find(y=>y.이름===n||y.별.includes(n));
  if(x){ if(U.새.사전!==x.이름) 사전적용(x); }
  else { U.새종목=n; if(!U.새.고름){ U.새.근육=세부로(낱말근육(n)||{}); U.새.사전=null; U.새.묶음=기본묶음(); } }
  U.새.찾는중=false; U.새.고름=true; return true; }
function 새찾기단추(){ return U.새?.찾는중 ? `<button class="새찾기단추 켬" data-act="새찾기" aria-label="확인">확인</button>`
  : `<button class="새찾기단추" data-act="새찾기" aria-label="찾기">${아이콘.돋보기}</button>`; }
function 새찾기결과(){ const 새=U.새; if(!새) return "";
  if(!새.찾는중) return 새.고름 ? "" : `<div class="아주작 옅음 한줄">초성으로도 찾습니다 · 예: ㅂㅊㅍ → 벤치프레스</div>`;
  const q=String(U.새종목||""); if(!다듬(q)) return `<div class="아주작 옅음 한줄">초성으로도 찾습니다 · 예: ㅂㅊㅍ → 벤치프레스</div>`;
  const l=종목찾기(q); if(!l.length) return `<div class="아주작 옅음 한줄">사전에 없습니다 · [확인]을 누르면 이 이름으로 만듭니다</div>`;
  return l.map(x=>{ const 있=S.종목표.some(t=>t.이름===x.이름);
    return `<button class="새결과줄 ${있?"있음":""}" data-act="새사전고름" data-v="${esc(x.이름)}"><b class="채움 한줄">${esc(x.이름)}</b><span class="아주작 옅음">${있?"이미 있음":esc(x.칸)}</span></button>`; }).join(""); }
function 새찾기갱신(){ const 폰=document.getElementById("폰"), 결=폰?.querySelector(".새찾기결과"), 단=폰?.querySelector(".새찾기단추"); if(!결) return;
  결.innerHTML=새찾기결과(); if(단) 단.outerHTML=새찾기단추(); }
function 새부위고르기(){ const 새=U.새, m=새.근육, 묶=세부부위.find(([g])=>g===새.묶음)||세부부위[0];
  const 수=g=>(세부부위.find(([x])=>x===g)?.[1]||[]).filter(k=>m[k]).length;
  return `<div class="이름표 새이름표">카테고리</div>${칩줄("새종목칸","",S.카테고리,U.새종목칸)}
    <div class="이름표 새이름표">세부 부위</div>
    <div class="새묶음">${칩줄("새묶음","",세부부위.map(([g])=>g),새.묶음,g=>수(g)?`${g} <b class="숫">${수(g)}</b>`:g)}</div>
    <div class="새역할줄"><span class="아주작 옅음">누르면</span>${칩줄("새역할","",["P","S","Y"],새.역할,r=>역할짧은[r])}</div>
    <div class="칩줄 새근육들">${묶[1].map(k=>`<button class="칩 근칩 ${m[k]?"역"+m[k]:""}" data-act="새근육" data-v="${k}" aria-pressed="${!!m[k]}">${esc(근이름(k))}${m[k]?`<small>${역할짧은[m[k]]}</small>`:""}</button>`).join("")}</div>
    <div class="새요약">${근육두줄(m)}</div>
    <button class="버튼 주 넓 새저장" data-act="새저장">저장</button>`; }
function 새종목시트(){ if(!U.새) U.새=새초기(); const 새=U.새;
  return `<div class="머리"><b>새 종목</b><button class="닫기" data-act="시트닫기">${U.시트.돌아감?"돌아가기":"닫기"}</button></div>
    <div class="새찾기줄"><input class="새찾기칸" data-in="새종목" placeholder="종목 이름" autocomplete="off" aria-label="종목 이름" value="${esc(U.새종목)}">${새찾기단추()}</div>
    <div class="새찾기결과">${새찾기결과()}</div>
    ${새.고름?새부위고르기():""}`; }
/* 이름 칸에 손가락이 가면 찾기가 켜진다 · Enter = [확인] */
document.addEventListener("focusin", e=>{ if(e.target?.dataset?.in==="새종목" && U.새 && !U.새.찾는중){ U.새.찾는중=true; 새찾기갱신(); } });
document.addEventListener("keydown", e=>{ if(e.key==="Enter" && e.target?.dataset?.in==="새종목" && U.새){ e.preventDefault(); U.새.찾는중=true; 행동("새찾기",{}); } });

/* ═══ 10-04 v18 C ②③ 종목 상자 — 종목마다 따로. 접히면 2열 격자의 한 칸 · 펼치면 줄 전체.
   [사진 칸 28 = 사진 넣기][이름 · ▾ / 주동근 : … / 협응근 : …]. 이름 쪽을 누르면 펼친다(전과 같은 '종목펼침').
   펼친 칸: 플랜 카드 → 기본 세팅 → 넣은 사진 줄(있을 때만 · 두 번 눌러 지우기 그대로) */
function 종목칸(x){ const l=사진목록(x.이름), 펼=U.종목펼침===x.이름, 플=S.플랜들.map((p,i)=>[p,i]).filter(([p])=>p.종목===x.이름), v=esc(x.이름);
  const 곁 = 플.length>1 ? `플랜 ${플.length}개` : !플.length&&찾표(x.이름) ? "플랜 가능" : "";
  return `<div class="종목칸 ${펼?"펼":""}"><div class="종목칸머리">
    <label class="작은사진 사진넣칸 ${l.length?"":"빈"}" aria-label="${v} 사진 넣기">${l.length?`<img src="${l[0]}" alt="">`:"＋"}<input type="file" accept="image/*" multiple data-in="사진" data-v="${v}" hidden></label>
    <button class="종목칸이름" data-act="종목펼침" data-v="${v}" aria-expanded="${펼}"><span class="종목칸첫줄"><span class="채움 한줄 종목줄이름">${플.length?`<span class="이름플랜"><b>${v}</b>${플랜딱지}</span>`:`<b>${v}</b>`}</span>${펼&&곁?`<span class="아주작 옅음">${곁}</span>`:""}<span class="아주작 옅음 접힘표 ${펼?"펼":""}">▾</span></span>${근육두줄(종목근육(x.이름))}</button></div>
    ${펼?`<div class="종목펼속" data-enter="펼속${v}">${플.map(([p,i])=>플랜카드(p,i)).join("")}${종목세팅(x.이름, 플.length>0)}${l.length?`<div class="사진줄">${l.map((src,j)=>`<button class="사진칸" data-act="사진지움" data-v="${v}" data-n="${j}" aria-label="사진 ${j+1} 지우기"><img src="${src}" alt="">${U.확인===`사진${x.이름}${j}`?'<span class="지움표">지우기</span>':''}</button>`).join("")}</div>`:""}</div>`:""}</div>`; }
/* z2a8 종목표에 없는 종목의 플랜도 잃지 않게 — '기타' 칸에 그 종목 상자로 (카테고리에 '기타' 가 있으면 거기에 더한다)
   10-04 v18 C ① 맨 위 = 띠(글 가운데) ② [+ 새 종목 만들기] · 칩 필터([전체] + 카테고리) · 카테고리마다 [작은 이름표] + 상자 격자
   (격자를 카테고리마다 따로 두어야 빈 칸 채우기(dense)가 다른 카테고리 상자를 끌어오지 않는다) */
function 종목탭(){ const 남은=[...new Set(S.플랜들.filter(p=>!S.종목표.some(x=>x.이름===p.종목)).map(p=>p.종목))].map(이름=>({이름, 칸:"기타"}));
  const 칸들 = 남은.length && !S.카테고리.includes("기타") ? [...S.카테고리,"기타"] : S.카테고리;
  const 고 = 칸들.includes(U.종목칸고름) ? U.종목칸고름 : "전체";
  const 묶 = (고==="전체"?칸들:[고]).map(c=>[c,[...S.종목표.filter(x=>x.칸===c), ...(c==="기타"?남은:[])]]).filter(([,l])=>l.length);
  return `<div class="띠 종목띠"><b class="채움">종목</b></div><div class="넘김 종목넘김">
    <button class="버튼 낮 넓" data-act="새종목열기">+ 새 종목 만들기</button>
    <div class="종목칩">${칩줄("종목칸고름","",["전체",...칸들],고)}</div>
    ${묶.map(([c,l])=>`<div class="종목묶음">${고==="전체"?`<div class="종목칸표">${esc(c)}</div>`:""}<div class="종목격자">${l.map(종목칸).join("")}</div></div>`).join("")||`<span class="작 옅음">없음</span>`}</div>`; }

'''.replace('@@사전@@', 사전)
사이바꿈('const 종목세트최대=10;\n', '/* ── 설정 ── */', 새코드)

# 시트 — 새 종목
바꿈('''  else if(종==="방식"){ 안 = 방식시트(); 높=true; }''',
     '''  else if(종==="새종목"){ 안 = 새종목시트(); 높=true; }   // 10-04 v18 C ⑤
  else if(종==="방식"){ 안 = 방식시트(); 높=true; }''')
# 닫기 — 돌아감이 있으면 그 시트로
바꿈('''    case "시트닫기": 시트닫기애니(); 다시=false; break;''',
     '''    case "시트닫기": if(U.시트?.종류==="새종목"&&U.시트.돌아감){ U.시트=U.시트.돌아감; break; }   // 10-04 v18 C ⑤ 종목 넣기에서 열었으면 그리로
      시트닫기애니(); 다시=false; break;''')

# 누르기 — 기본 세팅 세트 줄 (A 의 '종목설정값' 자리)
바꿈('''    case "종목설정값": { const b=종목설정잡기(d.v), dd=+d.d;   // 10-04 v17 ⑤
      if(d.f==="세트") b.세트=Math.max(1,Math.min(종목세트최대,b.세트+dd));
      else if(d.f==="w") b.w=Math.max(0,Math.round((b.w+dd*S.설정.무게폭)*10)/10);
      else if(d.f==="r") b.r=Math.max(1,b.r+dd);
      else b.휴=Math.max(휴식최소,Math.min(휴식최대,Math.round((b.휴+dd*휴식폭)/휴식폭)*휴식폭));
      break; }''',
     '''    /* 10-04 v18 C ④ 기본 세팅 세트 줄 — 무게 ± 설정의 무게 조절 폭 · 휴식 15초씩 0:15~5:00 · 마지막 한 줄은 안 지운다 · + 세트 = 앞 줄 값 복사(10줄까지) */
    case "종세트값": { const x=종목설정잡기(d.v).세트[+d.k], dd=+d.d; if(!x) break;
      if(d.f==="w") x.w=Math.max(0,Math.round((x.w+dd*S.설정.무게폭)*10)/10);
      else if(d.f==="r") x.r=Math.max(1,x.r+dd);
      else x.휴=Math.max(휴식최소,Math.min(휴식최대,Math.round((x.휴+dd*휴식폭)/휴식폭)*휴식폭));
      break; }
    case "종세트지움": { const b=종목설정잡기(d.v); if(b.세트.length>1) b.세트.splice(+d.k,1); break; }
    case "종세트더": { const b=종목설정잡기(d.v); if(b.세트.length>=종목세트최대){ 토스트(`${종목세트최대}세트까지`); 다시=false; break; } b.세트.push({...b.세트[b.세트.length-1]}); break; }
    case "종목칸고름": U.종목칸고름=d.v; break;''')
# 종목넣기 — 기본 세팅 목록 그대로
바꿈('''      else { const b=종목기본(d.v), 세=세트들(b.세트,b.w,b.r,b.휴); r.종목.push''',
     '''      else { const 세=종목기본세트(d.v); r.종목.push''')   # 10-04 v18 C ④
# 새 종목 시트 행동
바꿈('''    case "새종목칸": U.새종목칸=d.v; break;''',
     '''    case "새종목칸": U.새종목칸=d.v; if(U.새&&칸묶음[d.v]) U.새.묶음=칸묶음[d.v]; break;
    /* 10-04 v18 C ⑤ 새 종목 시트 — 종목 탭에서 열면 돌아감 없음 · 다른 시트(종목 넣기)에서 열면 그 시트를 돌아감으로 */
    case "새종목열기": { const 돌=U.시트&&U.시트.종류!=="새종목"?U.시트:null; U.새종목=""; U.새종목칸=S.카테고리[0]; U.새=새초기(); U.시트={종류:"새종목", 돌아감:돌}; break; }
    case "새찾기": if(!U.새) break; if(!U.새.찾는중){ U.새.찾는중=true; 새찾기갱신(); document.querySelector('#폰 [data-in="새종목"]')?.focus(); 다시=false; break; } if(!새확인()) 다시=false; break;   // 다시 그리면 토스트가 지워진다
    case "새사전고름": { if(S.종목표.some(t=>t.이름===d.v)){ 토스트("이미 있습니다"); 다시=false; break; }
      const x=종목사전.find(y=>y.이름===d.v); if(x) 사전적용(x); else U.새종목=d.v; U.새.찾는중=false; U.새.고름=true; break; }
    case "새묶음": U.새.묶음=d.v; break;
    case "새역할": U.새.역할=d.v; break;
    case "새근육": { const m=U.새.근육; if(m[d.v]===U.새.역할) delete m[d.v]; else m[d.v]=U.새.역할; break; }
    case "새저장": { if(U.새.찾는중 && !새확인()){ 다시=false; break; } const n=String(U.새종목||"").trim();
      if(!n){ 토스트("이름을 넣어 주세요"); 다시=false; break; } if(S.종목표.some(x=>x.이름===n)){ 토스트("이미 있습니다"); 다시=false; break; }
      if(!Object.values(U.새.근육).includes("P")){ 토스트("주동근을 하나 이상 골라 주세요"); 다시=false; break; }
      const 칸=U.새종목칸; S.종목표.push({이름:n, 칸, 근육:{...U.새.근육}}); 발자취(`종목 추가 · ${n}`);
      const 돌=U.시트.돌아감; U.새종목=""; U.새=null;
      if(돌){ U.시트=돌; if(돌.종류==="종목넣기") U.칸고름=칸; }
      else { U.시트=null; if(U.종목칸고름!=="전체"&&U.종목칸고름!==칸) U.종목칸고름=칸; }
      queueMicrotask(()=>토스트(`만들었습니다 · ${n}`)); break; }   // 다시 그린 뒤에 띄운다''')

# 입력
바꿈('''  else if(w==="새종목") U.새종목=v;''',
     '''  else if(w==="새종목"){ U.새종목=v; if(U.새){ U.새.찾는중=true; 새찾기갱신(); } }   // 10-04 v18 C ⑤ 치는 대로 찾는다(다시 그리지 않아 칸이 안 닫힌다)''')
바꿈('''  else if(w==="종목설정"){ const b=종목설정잡기(d.v); if(d.f==="w") b.w=Math.max(0,숫(v)); else b.r=Math.max(1,Math.round(숫(v))); }   // 10-04 v17 ⑤''',
     '''  else if(w==="종세트"){ const x=종목설정잡기(d.v).세트[+d.k]; if(x){ if(d.f==="w") x.w=Math.max(0,숫(v)); else x.r=Math.max(1,Math.round(숫(v))); } }   // 10-04 v18 C ④''')
바꿈('''  if(w==="루틴이름"||w==="루값"||w==="몸"||w==="종목설정") 그리기(); });''',
     '''  if(w==="루틴이름"||w==="루값"||w==="몸"||w==="종세트") 그리기(); });''')

css = '''
/* ═══ 10-04 v18 C — 종목 탭 · 새 종목 시트 ═══ */
/* ① 맨 위 띠 — .띠 그대로(강조 바탕 · 강조글 · 40 · 18 Bold), 글 가운데 */
.종목띠 b.채움{text-align:center}
.넘김.종목넘김{display:flex;flex-direction:column;gap:8px;padding-top:12px}
.종목넘김>*{flex:none}
.종목칩 .칩줄{flex-wrap:nowrap}
.종목칩 .칩{flex:1;justify-content:center;padding:0 4px;min-width:0}
/* ② 종목마다 상자 — 카테고리마다 2열 격자. 펼친 상자는 줄 전체 · 그 카테고리 안의 빈 칸은 뒤 상자가 채운다(dense) */
.종목묶음{display:flex;flex-direction:column;gap:4px}
.종목격자{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));grid-auto-flow:row dense;gap:8px}
.종목칸표{font-size:11px;font-weight:700;color:var(--흐림);margin:4px 4px 0}
.종목칸첫줄 .종목줄이름>b{display:block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.종목칸 .종목줄이름 .이름플랜>b{font-weight:700}   /* 플랜 있는 이름도 굵게 — 상자마다 이름 굵기 같게 */
.종목칸{border:1px solid var(--선);border-radius:var(--r-작게);background:var(--면);padding:8px;min-width:0}
.종목칸.펼{grid-column:1/-1;border-color:var(--속선)}
.종목칸머리{display:flex;gap:8px;align-items:flex-start;min-width:0}
/* ③ 사진 칸 = 사진 넣기 (28 · 비면 점선 + ＋) */
.사진넣칸{display:flex;align-items:center;justify-content:center;overflow:hidden;cursor:pointer;font-size:13px;color:var(--옅음)}
.사진넣칸 img{width:100%;height:100%;object-fit:cover;display:block}
.종목칸이름{flex:1;min-width:0;text-align:left;display:flex;flex-direction:column}
.종목칸첫줄{display:flex;align-items:center;gap:4px;min-height:28px;font-size:13px;min-width:0;width:100%}
.근줄{display:block;font-size:11px;color:var(--흐림);line-height:16px;min-width:0;max-width:100%;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.종목칸 .종목펼속{padding-top:8px}
/* ④ 기본 세팅 세트 줄 — [번호 16][kg][회][휴식][휴지통 28] · 칸 높이 28 (루틴 세트 줄과 같은 값칸 부품) */
.종세트머리,.종세트{display:grid;grid-template-columns:16px minmax(0,83fr) minmax(0,70fr) minmax(0,79fr) 28px;gap:4px;align-items:center}
.종세트머리 span{text-align:center;white-space:nowrap}
.종세트 .k{font-size:13px;font-weight:700;text-align:center}
.종세트 .값칸{height:28px}
.종세트 .값칸 button{width:24px;flex:none}
.종세트 .값칸 input,.종세트 .값칸 span{font-size:13px}
.종지움{width:28px;height:28px;display:inline-flex;align-items:center;justify-content:center;color:var(--옅음);border-radius:8px}
.종지움 svg{width:16px;height:16px}
.종지움:disabled{opacity:.35;cursor:default}
/* ⑤ 새 종목 시트 — 이름 칸(40) 안 오른쪽 끝 돋보기 44 · 켜지면 [확인](강조 바탕) */
.새찾기줄{display:flex;align-items:center;height:40px;border:1px solid var(--속선);border-radius:var(--r-작게);background:var(--면);overflow:hidden;flex:none}
.새찾기줄:focus-within{border-color:var(--강조)}
.새찾기칸{flex:1;min-width:0;height:100%;border:0;background:transparent;padding:0 12px;font-size:15px;color:var(--글)}
.새찾기칸:focus{outline:none}
.새찾기단추{flex:none;width:44px;height:40px;display:flex;align-items:center;justify-content:center;color:var(--흐림)}
.새찾기단추 svg{width:22px;height:22px}
.새찾기단추.켬{background:var(--강조);color:var(--강조글);font-size:13px;font-weight:700}
.새찾기결과{display:flex;flex-direction:column;flex:none}
.새찾기결과:empty{display:none}
.새결과줄{display:flex;align-items:center;gap:8px;min-height:40px;width:100%;text-align:left;border-bottom:1px solid var(--선);font-size:15px}
.새결과줄.있음 b{color:var(--옅음)}
.시트 .새이름표{margin:8px 4px 0}
.새묶음 .칩줄{flex-wrap:nowrap}
.새묶음 .칩{flex:1;justify-content:center;padding:0 4px;min-width:0;gap:4px}
.새역할줄{display:flex;align-items:center;gap:8px}
.근칩 small{font-size:11px;font-weight:700;margin-left:4px}
.근칩.역P{background:var(--강조);color:var(--강조글);border-color:var(--강조);font-weight:700}
.근칩.역S{background:var(--강조옅음);color:var(--강조);border-color:var(--강조);font-weight:700}
.근칩.역Y{border-style:dashed;border-color:var(--강조);color:var(--강조)}
.새요약{display:flex;flex-direction:column;padding:8px;border-radius:var(--r-작게);background:var(--면2);min-width:0}
.새저장{height:44px;flex:none}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]
pathlib.Path(OUT).write_text(s, encoding='utf-8'); print("v18 C →", OUT)
