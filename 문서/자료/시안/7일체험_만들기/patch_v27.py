"""v27 — 10-21 홍겸 님: 돌아가며 칸을 '앱 전체에 하나'로 (같이 세기) + 한 차례에 종목 여러 개 + 운동 중 차례 바꾸기 + 설정 '건너뛴 차례'.
v26(루틴 안에만 있던 칸) 위에 쌓는다. 시안은 모양만 — 다음 차례로 넘기는 계산은 앱에서.
- S.돌림들 = [{id, 차례:[[{이름,종id,세트}...], ...], 다음}] · 루틴 줄은 {돌림id} 만 든다 → 여러 루틴에 넣으면 같은 칸
- 루틴 상세: 접힘 = [다음 차례 종목들][돌아가며 n/N] · 펼침 = 차례 칩줄 + 그 차례 종목 줄 + 들어간 루틴 + [+ 이 차례에 종목][+ 차례][이 차례 빼기]
- 종목 넣기 시트(루틴): 맨 위에 만들어 둔 돌아가며 칸 → 누르면 이 루틴에 넣기
- 운동 화면: 돌아가며에서 온 종목 이름 옆 [n/N ⇄] → 차례 고르기 시트
- 설정 > 운동 진행: '건너뛴 돌아가며 차례' [다음에 다시][다음 차례로]
쓰는 법: python3 patch_v27.py IN.html OUT.html"""
import sys
s = open(sys.argv[1], encoding="utf-8").read()
def 바꿈(old, new):
    global s
    assert s.count(old) == 1, (s.count(old), old[:80])
    s = s.replace(old, new)
def 덩어리(시작글, 끝글, new):
    global s
    a = s.index(시작글); b = s.index(끝글, a) + len(끝글)
    assert s.count(시작글) == 1 and b - a < 6000, (s.count(시작글), b - a)
    s = s[:a] + new + s[b:]

# 0) 상태에 돌림들
바꿈('''    루틴들:[], 예정:{},''', '''    루틴들:[], 돌림들:[], 예정:{},''')

# ① 상자 — v26 의 e.돌림 → e.돌림id
바꿈('''  if(e.돌림) return 돌림상자(e,i,빼기);   // 10-21 v26''', '''  if(e.돌림id) return 돌림상자(e,i,빼기);   // 10-21 v27''')

# ② v26 함수 셋 → v27
덩어리('''/* 10-21 v26 돌아가는 종목 칸 — e.돌림''', '''이 종목 빼기</button></div></div>`; }''',
'''/* 10-21 v27 돌아가며 칸 — 앱 전체에 하나(S.돌림들) · 루틴 줄은 {돌림id} 만. 여러 루틴에 넣으면 어느 루틴에서 하든 같은 차례를 잇는다 */
function 돌림(id){ return (S.돌림들||[]).find(g=>g.id===id); }
const 차례글 = t => t.map(x=>x.이름).join(" + ");
const 차례칩글 = t => t.length ? t[0].이름 + (t.length>1?` +${t.length-1}`:"") : "빈 차례";
function 돌림본(g){ const k=U.돌림본?.[g.id]; return k!=null && k<g.차례.length ? k : g.다음; }
function 돌림상자(e,i,빼기){ const g=돌림(e.돌림id); if(!g) return "";
  const n=g.차례.length, 오=g.다음, 펼=U.루펼침.has(e), 본=돌림본(g);
  const 머리=`<div class="루머리"><div class="채움 루접줄" data-act="루펼침" data-i="${i}" role="button" tabindex="0" aria-expanded="${펼}"><b>${esc(차례글(g.차례[오]))}</b><span class="알약 돌림표 숫" aria-label="돌아가며 ${n}차례 중 ${오+1}번째">돌아가며 ${오+1}/${n}</span><span class="접힘표 ${펼?"펼":""}">▾</span></div>${빼기}</div>`;
  if(!펼) return `<div class="종목상자 루접힘 돌림칸" data-drag="종목줄" data-i="${i}" data-drop="종목줄">${머리}</div>`;
  const 든곳=S.루틴들.filter(r=>r.종목.some(x=>x.돌림id===g.id)).map(r=>r.이름);
  return `<div class="종목상자 루펼 돌림칸" data-drag="종목줄" data-i="${i}" data-drop="종목줄">${머리}
    <div class="칩줄 돌림칩줄">${g.차례.map((t,k)=>`<button class="칩 ${k===본?"켬":""}" data-act="돌림보기" data-v="${g.id}" data-k="${k}"><span class="숫">${k+1}</span>&nbsp;${esc(차례칩글(t))}${k===오?`<i class="돌림다음">다음</i>`:""}</button>`).join("")}</div>
    <div class="돌림종목들">${g.차례[본].map((x,j)=>`<div class="돌림종목"><b>${esc(x.이름)}</b><span class="루플랜글 숫">${x.세트.length}세트 · ${kg(x.세트[0]?.w??0)} × ${x.세트[0]?.r??0}</span><button class="루지움" data-act="돌림종목빼기" data-v="${g.id}" data-k="${본}" data-j="${j}" aria-label="${esc(x.이름)} 빼기">${아이콘.휴지통}</button></div>`).join("") || `<div class="빈칸">이 차례에 종목을 넣어 주세요</div>`}</div>
    <div class="아주작 옅음 돌림든곳">${든곳.map(esc).join(" · ")}에 들어 있음 — 어느 루틴에서 하든 다음 차례로 넘어갑니다</div>
    <div class="줄 돌림단추"><button class="버튼 낮" data-act="돌림추가" data-v="${g.id}" data-k="${본}">+ 종목</button><button class="버튼 낮" data-act="돌림추가" data-v="${g.id}" data-k="-1">+ 차례</button><button class="버튼 낮 돌림빼기" data-act="돌림차례빼기" data-v="${g.id}" data-k="${본}">이 차례 빼기</button></div></div>`; }''')

# ③ 보통 상자 [+ 돌아가며] — 그 종목을 1차례로 칸을 만들고 바로 '새 차례' 넣기
바꿈('''<button class="버튼 낮" data-act="돌림추가" data-i="${i}">+ 돌아가며</button></div></div>`; }   /* 10-21 v26 [+ 돌아가며] */''',
     '''<button class="버튼 낮" data-act="돌림만들기" data-i="${i}">+ 돌아가며</button></div></div>`; }   /* 10-21 v26 [+ 돌아가며] · v27 칸 만들기 */''')

# ④ 누름 — v26 셋 → v27
덩어리('''    case "돌림보기": { const e=루틴(U.루틴열림)?.종목[+d.i];''', '''if(e.돌림.length<2){ delete e.돌림; delete e.본; delete e.차례; } break; }''',
'''    case "돌림보기": (U.돌림본??={})[d.v]=+d.k; break;   // 10-21 v27
    case "돌림만들기": { const r=루틴(U.루틴열림), e=r?.종목[+d.i]; if(!e) break;
      const g={id:"g"+Date.now(), 차례:[[{이름:e.이름, ...(e.종id?{종id:e.종id}:{}), 세트:e.세트}]], 다음:0}; S.돌림들.push(g);
      const 새={이름:"돌아가며", 돌림id:g.id, 세트:[]}; r.종목[+d.i]=새; U.루펼침.add(새);
      U.시트={종류:"종목넣기", 대상:"돌림", 돌림id:g.id, k:-1, 모드:"이 날만"}; U.방금=[]; U.칸고름="전체"; break; }
    case "돌림추가": U.시트={종류:"종목넣기", 대상:"돌림", 돌림id:d.v, k:+d.k, 모드:"이 날만"}; U.방금=[]; U.칸고름="전체"; break;
    case "돌림종목빼기": { const g=돌림(d.v), t=g?.차례[+d.k]; if(t) t.splice(+d.j,1); break; }
    case "돌림차례빼기": { const g=돌림(d.v); if(!g) break; const k=+d.k; g.차례.splice(k,1); if(g.다음>k) g.다음--; if(g.다음>=g.차례.length) g.다음=0; if(U.돌림본) delete U.돌림본[g.id];
      if(!g.차례.length){ S.돌림들=S.돌림들.filter(x=>x!==g); S.루틴들.forEach(r=>r.종목=r.종목.filter(x=>x.돌림id!==g.id)); } break; }
    case "돌림넣기": { const r=루틴(U.루틴열림); if(r&&돌림(d.v)) r.종목.push({이름:"돌아가며", 돌림id:d.v, 세트:[]}); break; }
    case "돌림차례고름": { const ss=S.세션, g=돌림(d.v); if(!ss||!g) break; const k=+d.k, 첫=ss.종목.findIndex(x=>x.돌림id===g.id);
      const 새=g.차례[k].map(x=>({이름:x.이름, ...(x.종id?{종id:x.종id}:{}), 플랜id:null, 돌림id:g.id, 돌림k:k, 세트:x.세트.map(s=>({w:s.w, r:s.r, 목r:s.r, 휴:s.휴, 완료:false}))}));
      ss.종목=ss.종목.filter(x=>x.돌림id!==g.id); ss.종목.splice(첫<0?ss.종목.length:첫,0,...새); U.본=Math.max(0,첫); U.시트=null; break; }''')

# ⑤ 종목 넣기 — 돌아가며 모드
덩어리('''    case "종목넣기": if(U.시트?.대상==="돌림"){''', '''        U.시트=null; break; }''',
'''    case "종목넣기": if(U.시트?.대상==="돌림"){ const g=돌림(U.시트.돌림id);   // 10-21 v27
        if(g){ const t=종목표찾기(d.v), 새={이름:t?t.이름:d.v, ...(t?.id?{종id:t.id}:{}), 세트:종목기본세트(d.v)};
          if(U.시트.k<0){ g.차례.push([새]); (U.돌림본??={})[g.id]=g.차례.length-1; } else g.차례[U.시트.k].push(새); }
        U.시트=null; break; }''')

# ⑥ 시트 제목
바꿈('''U.시트.대상==="돌림" ? `돌아가며 넣기 · ${esc(r.종목[U.시트.돌림i]?.이름??"")}` :''',
     '''U.시트.대상==="돌림" ? (U.시트.k<0 ? "돌아가며 · 새 차례" : `돌아가며 · ${U.시트.k+1}차례에 더하기`) :''')

# ⑦ 넣기 시트(루틴) 맨 위 — 만들어 둔 돌아가며 칸
바꿈('''      + `<div class="넣기목록" data-k="${esc(U.칸고름)}">${넣기목록(r)}</div>`''',
'''      + (U.시트.대상!=="돌림" && U.시트.대상!=="운동" && S.돌림들.length ? `<div class="이름표">돌아가며 칸</div><div class="쌓기 넣기돌림">${S.돌림들.map(g=>{ const 수=r.종목.filter(x=>x.돌림id===g.id).length;
          return `<button class="넣기칸 돌림넣칸${수?" 들어감":""}" data-act="돌림넣기" data-v="${g.id}"><span class="알약 돌림표 숫">${g.차례.length}차례</span><b class="채움">${esc(g.차례.map(차례칩글).join(" / "))}</b><span class="넣기체크" aria-hidden="true">${"✓".repeat(수)}</span></button>`; }).join("")}</div>` : "")   /* 10-21 v27 */
      + `<div class="넣기목록" data-k="${esc(U.칸고름)}">${넣기목록(r)}</div>`''')

# ⑧ 운동 시작 — 돌아가며 줄은 그 칸의 '다음' 차례 종목들로 편다
바꿈('''  const 종목 = r.종목.map(e=>{ let 계=null; if(e.플랜id){ const p=플랜(e.플랜id); 계=p&&다음회(p); }''',
'''  const 종목 = r.종목.flatMap(e=>{ const g=e.돌림id&&돌림(e.돌림id);   // 10-21 v27 돌아가며 → 다음 차례 종목들
    if(e.돌림id) return g ? g.차례[g.다음].map(x=>({이름:x.이름, ...(x.종id?{종id:x.종id}:{}), 플랜id:null, 돌림id:g.id, 돌림k:g.다음, 세트:x.세트.map(s=>({w:s.w, r:s.r, 목r:s.r, 휴:s.휴, 완료:false}))})) : [];
    let 계=null; if(e.플랜id){ const p=플랜(e.플랜id); 계=p&&다음회(p); }''')

# ⑨ 운동 화면 이름 옆 [n/N ⇄]
바꿈('''  const 이름칸=e.플랜id?''', '''  let 이름칸=e.플랜id?''')
바꿈('''    : `<b class="운이름">${esc(e.이름)}</b>`;''',
'''    : `<b class="운이름">${esc(e.이름)}</b>`;
  if(e.돌림id){ const g=돌림(e.돌림id); if(g) 이름칸+=`<button class="알약 돌림표 운돌림 숫" data-act="시트" data-t="돌림바꿈" data-v="${g.id}" aria-label="돌아가며 다른 차례로 바꾸기">${e.돌림k+1}/${g.차례.length} ⇄</button>`; }   // 10-21 v27''')

# ⑩ 차례 고르기 시트
바꿈('''  else if(종==="달고르기") 안 = 달고르기시트();''',
'''  else if(종==="돌림바꿈"){ const g=돌림(U.시트.대상), 지금=S.세션?.종목.find(x=>x.돌림id===g?.id)?.돌림k;   // 10-21 v27
    안 = 머리("오늘 할 차례", "닫기") + `<div class="쌓기">${(g?.차례||[]).map((t,k)=>`<button class="넣기칸 돌림넣칸${k===지금?" 들어감":""}" data-act="돌림차례고름" data-v="${g.id}" data-k="${k}"><span class="알약 돌림표 숫">${k+1}</span><b class="채움">${esc(차례글(t))}</b>${k===지금?`<span class="아주작 옅음">지금</span>`:""}</button>`).join("")}</div><div class="아주작 옅음" style="margin-top:8px">고른 차례를 한 것으로 치고, 다음엔 그 다음 차례가 나옵니다</div>`; }
  else if(종==="달고르기") 안 = 달고르기시트();''')

# ⑪ 설정 — 건너뛴 차례
바꿈('''      ${설정줄("넘어가기 전 확인","넘어가기 전에 한 번 묻기",스위치(s.넘어가기전확인,"스위치","넘어가기전확인"))}<div class="구분"></div>''',
'''      ${설정줄("넘어가기 전 확인","넘어가기 전에 한 번 묻기",스위치(s.넘어가기전확인,"스위치","넘어가기전확인"))}<div class="구분"></div>
      ${설정줄("건너뛴 돌아가며 차례","그 차례를 한 세트도 안 했을 때","")}${칩줄("설정칩","돌림건너뜀",["다시","넘어감"],s.돌림건너뜀??"다시",v=>v==="다시"?"다음에 다시":"다음 차례로")}<div class="구분" style="margin-top:8px"></div>''')   # 10-21 v27

# ⑫ 모양
바꿈('''.돌림빼기{color:var(--나쁨)}''', '''.돌림빼기{color:var(--나쁨)}
.돌림종목들{display:flex;flex-direction:column;gap:4px}
.돌림종목{display:flex;align-items:center;gap:8px;min-height:32px;padding:0 4px 0 8px;border:1px solid var(--속선);border-radius:8px}
.돌림종목>b{flex:1 1 auto;min-width:0;font-size:15px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.돌림든곳{margin-top:8px}
.돌림넣칸{display:flex;align-items:center;gap:8px;width:100%;min-height:44px;padding:0 12px;text-align:left}
.돌림넣칸>b{min-width:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.운돌림{margin-left:8px;vertical-align:middle;background:none;border-color:var(--강조글);color:var(--강조글)}   /* 강조 띠 위 = 강조글 (U1-2) */''')

open(sys.argv[2], "w", encoding="utf-8").write(s)
print("✓ v27 적용 →", sys.argv[2])
