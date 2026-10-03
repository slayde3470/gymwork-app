"""v13 (10-04 홍겸 님) — ① 운동 화면에서 새 종목 넣기(아래 종목 칸 줄 끝 [＋] → 루틴과 같은 '종목 넣기' 시트, 지금 운동에 넣음)
② 큰 운동 표시: SBD 칸 위에 파란 상자(--강조 · --강조글) '3대 Nkg'. 너비 = SBD 세 칸과 같음, 4~5대가 되어도 세 칸 너비 고정(보고서 · 프로필 모두)"""
import sys, pathlib
IN, OUT = sys.argv[1], sys.argv[2]
s = pathlib.Path(IN).read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:70]!r}: {c}번")
    s = s.replace(old, new)

# ── ① 넣을 곳: 시트 대상이 '운동' 이면 지금 운동, 아니면 열린 루틴 ──
바꿈('function 넣기목록(r){', '''function 넣을곳(){ return U.시트?.대상==="운동" && S.세션 ? S.세션 : 루틴(U.루틴열림); }
const 운세트로 = 목 => 목.map(x=>({...x, 목r:x.r, 완료:false}));
function 넣기목록(r){''')
바꿈('  else if(종==="종목넣기"){ const r=루틴(U.루틴열림);', '  else if(종==="종목넣기"){ const r=넣을곳();')
바꿈('''    case "종목넣기": { const r=루틴(U.루틴열림), 열=`e:${d.v}`;''', '''    case "종목넣기": { const r=넣을곳(), 운=r===S.세션, 열=`e:${d.v}`;''')
바꿈('''      else { r.종목.push({이름:d.v, 세트:세트들(S.설정.기본세트,20,10,S.설정.기본휴식)}); U.방금.push(열); 업적알림(업적판정("루틴")); } break; }''',
     '''      else { const 세=세트들(S.설정.기본세트,20,10,S.설정.기본휴식); r.종목.push({이름:d.v, 세트:운?운세트로(세):세}); U.방금.push(열); if(!운) 업적알림(업적판정("루틴")); } break; }''')
바꿈('''    case "플랜넣기": { const r=루틴(U.루틴열림), p=플랜(d.v), 열=`p:${d.v}`;''', '''    case "플랜넣기": { const r=넣을곳(), 운=r===S.세션, p=플랜(d.v), 열=`p:${d.v}`;''')
바꿈('''      else { const e={이름:p.이름, 플랜id:p.id, 세트:[]}; e.세트=줄세트(e); r.종목.push(e); U.방금.push(열); 업적알림(업적판정("루틴")); } break; }''',
     '''      else { const e={이름:p.이름, 플랜id:p.id, 세트:[]}; e.세트=줄세트(e); if(운) e.세트=운세트로(e.세트); r.종목.push(e); U.방금.push(열); if(!운) 업적알림(업적판정("루틴")); } break; }''')
# 종목 칸 줄 끝 [＋]
바꿈('''<div class="ㅁ"><i style="width:${m?k/m*100:0}%"></i></div></button>`; }).join("")}</div></div>''',
     '''<div class="ㅁ"><i style="width:${m?k/m*100:0}%"></i></div></button>`; }).join("")}<button class="운칸 운더" data-act="시트" data-t="종목넣기" data-v="운동" aria-label="종목 넣기">＋</button></div></div>''')

# ── ② N대 파란 상자 ──
i = s.index('function 큰운동판(칸, 차보임, 톱니){'); j = s.index('</div>`; }', i) + len('</div>`; }')
s = s[:i] + '''function 큰운동판(칸, 차보임, 톱니){ const 합=칸[0], 들=칸.slice(1), 열=Math.max(3,들.length);
  /* 10-04 홍겸 님 — SBD 위에 파란 상자 'N대 Nkg'. 너비 = 앞 세 칸(SBD) 고정 */
  return `<div class="결과수 큰수 열${열}${톱니?" 톱니비킴":""}" style="--열:${열}"><p class="큰합"><b>${esc(합.글)} ${합.v>0?차kg(합.v)+"kg":"—"}</b>${차보임&&합.v>0&&합.앞>0&&Math.abs(합.v-합.앞)>=0.05?`<small>${합.v>합.앞?"▲":"▼"}${차kg(Math.abs(합.v-합.앞))}</small>`:""}</p>${들.map(x=>`<div><div class="큰값"><b>${x.v>0?차kg(x.v):"—"}</b>${차보임&&x.v>0&&x.앞>0?보고차(x.v-x.앞,""):""}</div><span>${esc(x.글)}</span></div>`).join("")}</div>`; }''' + s[j:]

css = '''
/* ═══ 10-04 v13 — 운동 화면 [＋] 종목 넣기 · N대 파란 상자 ═══ */
.운칸.운더{display:flex;align-items:center;justify-content:center;font-size:22px;font-weight:700;color:var(--강조);border-style:dashed;border-color:var(--속선)}
.결과수.큰수 .큰합{grid-column:1 / span 3;margin:0 0 4px;height:28px;border-radius:8px;background:var(--강조);color:var(--강조글);display:flex;align-items:center;justify-content:center;gap:4px;min-width:0;overflow:hidden;white-space:nowrap}
.결과수.큰수 .큰합 b{font-size:13px;line-height:28px}
.결과수.큰수 .큰합 small{font-size:11px;font-weight:700}
.결과수.큰수>div{grid-row:2}
/* 보고서: 톱니(오른쪽 위 28)와 파란 상자가 겹치지 않게 칸 묶음 오른쪽을 24 비움 */
.보고프로필 .결과수.큰수.톱니비킴{margin-right:24px}
.결과수.큰수>div:nth-child(2){border-left:0}
.결과수.큰수.열3>div:nth-child(3n+1){border-left:1px solid var(--선)}
.결과수.큰수.열3>div:nth-child(2){border-left:0}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]
pathlib.Path(OUT).write_text(s, encoding='utf-8'); print("v13 →", OUT)
