"""v26 — 10-21 홍겸 님: '돌아가는 종목 칸' (루틴 2개로 2주 루틴 돌리기). 시안은 모양만 — 다음 차례로 넘기는 계산은 앱에서.
- 펼친 종목 상자 아래 [+ 세트] 옆에 [+ 돌아가며] → 종목 넣기 시트에서 하나 고르면 그 칸이 '돌아가며' 칸이 된다
- 돌아가며 칸: 접힘 = [다음 할 종목 이름][돌아가며 n/N] · 펼침 = 종목 칩줄(누르면 그 종목 세트 보기 · '다음' 표시) + 세트 줄 + [+ 세트][+ 돌아가며][이 종목 빼기]
쓰는 법: python3 patch_v26.py IN.html OUT.html"""
import sys
s = open(sys.argv[1], encoding="utf-8").read()
def 바꿈(old, new):
    global s
    assert s.count(old) == 1, (s.count(old), old[:80])
    s = s.replace(old, new)

# ① 상자 그리기 — 돌아가며 칸이면 따로
바꿈('''  /* h627 · 9kht 세트 줄 = [번호][무게][횟수][휴식][휴지통] · 높이 28. 세트가 하나면 휴지통은 꺼 둔다 */
  const 하나 = e.세트.length<=1;''',
'''  if(e.돌림) return 돌림상자(e,i,빼기);   // 10-21 v26
  /* h627 · 9kht 세트 줄 = [번호][무게][횟수][휴식][휴지통] · 높이 28. 세트가 하나면 휴지통은 꺼 둔다 */
  const 하나 = e.세트.length<=1;''')

# ② 보통 상자 아래 [+ 세트] 옆에 [+ 돌아가며]
바꿈('''    <button class="버튼 낮 넓" data-act="루세트" data-i="${i}" data-d="1">+ 세트</button></div>`; }''',
'''    <div class="줄 돌림단추"><button class="버튼 낮" data-act="루세트" data-i="${i}" data-d="1">+ 세트</button><button class="버튼 낮" data-act="돌림추가" data-i="${i}">+ 돌아가며</button></div></div>`; }   /* 10-21 v26 [+ 돌아가며] */
/* 10-21 v26 돌아가는 종목 칸 — e.돌림 = 칸 안 종목들(지금 보는 것은 e 자신에 펼쳐 둔다 · e.본) · e.차례 = 다음에 할 것 */
function 돌림이름(e,k){ return k===e.본 ? e.이름 : e.돌림[k].이름; }
function 돌림맞춤(e,k){ e.돌림[e.본]={이름:e.이름, ...(e.종id?{종id:e.종id}:{}), 세트:e.세트}; const t=e.돌림[k]; e.이름=t.이름; if(t.종id) e.종id=t.종id; else delete e.종id; e.세트=t.세트; e.본=k; }
function 돌림상자(e,i,빼기){ const n=e.돌림.length, 오=e.차례, 펼=U.루펼침.has(e), 하나=e.세트.length<=1;
  const 머리=`<div class="루머리"><div class="채움 루접줄" data-act="루펼침" data-i="${i}" role="button" tabindex="0" aria-expanded="${펼}"><b>${esc(돌림이름(e,오))}</b><span class="알약 돌림표 숫" aria-label="돌아가며 ${n}개 중 ${오+1}번째">돌아가며 ${오+1}/${n}</span><span class="접힘표 ${펼?"펼":""}">▾</span></div>${빼기}</div>`;
  if(!펼) return `<div class="종목상자 루접힘 돌림칸" data-drag="종목줄" data-i="${i}" data-drop="종목줄">${머리}</div>`;
  return `<div class="종목상자 루펼 돌림칸" data-drag="종목줄" data-i="${i}" data-drop="종목줄">${머리}
    <div class="칩줄 돌림칩줄">${e.돌림.map((t,k)=>`<button class="칩 ${k===e.본?"켬":""}" data-act="돌림보기" data-i="${i}" data-k="${k}">${esc(돌림이름(e,k))}${k===오?`<i class="돌림다음">다음</i>`:""}</button>`).join("")}</div>
    <div class="세트머리 루 아주작 옅음"><span>세트</span><span>무게 kg</span><span>횟수</span><span>휴식</span><span></span></div>
    ${e.세트.map((s,k)=>`<div class="루세트"><span class="k">${k+1}</span>${루값("w",s.w,i,k)}${루값("r",s.r,i,k)}${루값("휴",s.휴,i,k)}<button class="루지움" data-act="루세트지움" data-i="${i}" data-k="${k}" aria-label="${k+1}세트 지우기"${하나?" disabled":""}>${아이콘.휴지통}</button></div>`).join("")}
    <div class="줄 돌림단추"><button class="버튼 낮" data-act="루세트" data-i="${i}" data-d="1">+ 세트</button><button class="버튼 낮" data-act="돌림추가" data-i="${i}">+ 돌아가며</button><button class="버튼 낮 돌림빼기" data-act="돌림빼기" data-i="${i}">이 종목 빼기</button></div></div>`; }''')

# ③ 누름
바꿈('''    case "루펼침": {''',
'''    case "돌림보기": { const e=루틴(U.루틴열림)?.종목[+d.i]; if(e?.돌림 && +d.k!==e.본) 돌림맞춤(e,+d.k); break; }   // 10-21 v26
    case "돌림추가": U.시트={종류:"종목넣기", 대상:"돌림", 돌림i:+d.i, 모드:"이 날만"}; U.방금=[]; U.칸고름="전체"; break;
    case "돌림빼기": { const e=루틴(U.루틴열림)?.종목[+d.i]; if(!e?.돌림) break; const k=e.본; e.돌림.splice(k,1);
      if(e.차례>k) e.차례--; if(e.차례>=e.돌림.length) e.차례=0;
      const m=Math.min(k,e.돌림.length-1), t=e.돌림[m]; e.이름=t.이름; if(t.종id) e.종id=t.종id; else delete e.종id; e.세트=t.세트; e.본=m;
      if(e.돌림.length<2){ delete e.돌림; delete e.본; delete e.차례; } break; }
    case "루펼침": {''')

# ④ 종목 넣기 시트 — 돌아가며로 열었으면 그 칸에 하나 더하고 닫는다
바꿈('''    case "종목넣기": { const r=넣을곳(), 운=r===S.세션, 열=`e:${d.v}`;''',
'''    case "종목넣기": if(U.시트?.대상==="돌림"){ const e=루틴(U.루틴열림)?.종목[U.시트.돌림i];   // 10-21 v26
        if(e){ const t=종목표찾기(d.v); if(!e.돌림){ e.돌림=[{이름:e.이름, ...(e.종id?{종id:e.종id}:{}), 세트:e.세트}]; e.본=0; e.차례=0; }
          e.돌림.push({이름:t?t.이름:d.v, ...(t?.id?{종id:t.id}:{}), 세트:종목기본세트(d.v)}); U.루펼침.add(e); }
        U.시트=null; break; }
    { const r=넣을곳(), 운=r===S.세션, 열=`e:${d.v}`;''')

# ⑤ 모양
바꿈('''.루격자{display:grid;''', '''/* 10-21 v26 돌아가는 종목 칸 */
.돌림표{flex:none;border:1px solid var(--강조);color:var(--강조)}
.돌림칩줄{margin:8px 0}
.돌림다음{font-style:normal;font-weight:400;font-size:11px;margin-left:4px}
.돌림단추{margin-top:8px}
.돌림단추>.버튼{flex:1 1 0;min-width:0}
.돌림빼기{color:var(--나쁨)}
.루격자{display:grid;''')

# ⑥ 시트 제목 — 돌아가며로 열었으면 '돌아가며 넣기 · 그 종목'
바꿈('''    안 = 머리(`${esc(r.이름)}에 넣기`, "확인")''',
'''    안 = 머리(U.시트.대상==="돌림" ? `돌아가며 넣기 · ${esc(r.종목[U.시트.돌림i]?.이름??"")}` : `${esc(r.이름)}에 넣기`, "확인")   /* 10-21 v26 */''')

open(sys.argv[2], "w", encoding="utf-8").write(s)
print("✓ v26 적용 →", sys.argv[2])
