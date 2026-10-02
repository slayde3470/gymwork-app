"""7day-v3.html → 7day-v4.html (10-03 홍겸 님 "세트 수가 많아지면 어떻게 할래?" → "응. 넣어줘")

날짜 판의 종목 목록이 길어지면 '운동 시작' 단추가 화면 밖으로 밀려나던 것 (10 D2-10 '요약 판에는 요약만').
  ① 종목이 6개 이상이면 4줄만 보이고 다섯째 줄 자리에 '외 n종목 ∨' — 누르면 펼치고 '접기 ∧'
     (5종목까지는 접어도 높이가 같으므로 그냥 다 보인다)
  ② '운동 시작 · 변경' 줄은 판 맨 아래에 붙인다(sticky) — 펼쳐도 늘 보인다
  ③ 기록한 날의 종목 목록도 같은 규칙 (U0-2 같은 일은 같은 모양)
값: 줄 높이 32(U3-3) · 아이콘 18(U3-6 · 선 아이콘 D2-7) · 여백 8(U3-1) · 누를 수 있는 글 = 강조(U1-4) · 펼침 표시는 뒤집힘(D3-3)
"""
import pathlib
SP = pathlib.Path('/tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad')
s = (SP/'7day-v3.html').read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:70]!r}: {c}번")
    s = s.replace(old, new)

# ── 1. 선 아이콘: 아래 화살표 (펼침 · 열리면 뒤집힘) ──
바꿈('''    체크:`<svg viewBox="0 0 24 24"''',
     '''    아래:a('<path d="M6 9l6 6 6-6"/>'),
    체크:`<svg viewBox="0 0 24 24"''')

# ── 2. 접는 목록 ──
바꿈('''function 예정상세(실){''',
     '''/* 종목 줄이 6개 이상이면 4줄 + '외 n종목' (10-03) — 5개까지는 접어도 높이가 같아 그냥 다 보인다 */
const 접는수 = 4;
function 접는목록(줄들, 키){ const n=줄들.length; if(n<=접는수+1) return 줄들.join("");
  const 펼=U.예펼침===키;
  return (펼?줄들:줄들.slice(0,접는수)).join("")
    + `<button class="예접기" data-act="예펼침" data-v="${키}" aria-expanded="${펼}"><span class="번"></span><span class="채움">${펼?"접기":`외 ${n-접는수}종목`}</span><span class="접힘표 ${펼?"펼":""}">${아이콘.아래}</span></button>`; }
function 예정상세(실){''')
바꿈('''function 예정상세(실){ const 볼=''', '''function 예정상세(실, 키){ const 볼=''')
바꿈('''<span class="숫 흐림">${e.세트.length}세트 · ${세트글(e.세트)}</span></div>`).join("");''',
     '''<span class="숫 흐림">${e.세트.length}세트 · ${세트글(e.세트)}</span></div>`);''')
바꿈('''<div class="예목록">${줄}</div>`; }''', '''<div class="예목록">${접는목록(줄, 키)}</div>`; }''')

바꿈('''function 기록상세(r){ const 줄=r.종목.map(''', '''function 기록상세(r, 키){ const 줄=r.종목.map(''')
바꿈('''<span class="숫 흐림">${세.length}/${e.세트.length}세트${세.length?" · "+세트글(세):""}</span></div>`; }).join("");
  return 줄?`<div class="예목록">${줄}</div>`:""; }''',
     '''<span class="숫 흐림">${세.length}/${e.세트.length}세트${세.length?" · "+세트글(세):""}</span></div>`; });
  return 줄.length?`<div class="예목록">${접는목록(줄, 키)}</div>`:""; }''')

# ── 3. 날짜 판: 키 넘기기 · 단추 줄 붙박이 ──
바꿈('''  for(const [,r] of 록){ const 달=기록달성(r);''', '''  for(const [rk,r] of 록){ const 달=기록달성(r);''')
바꿈('''${기록상세(r)}</div>${k===오&&루틴(r.루틴id)''', '''${기록상세(r, "록"+rk)}</div>${k===오&&루틴(r.루틴id)''')
바꿈('''${r.휴식일?"":예정상세(실)}</div></div>
        <div class="줄">${k===오&&!r.휴식일?''',
     '''${r.휴식일?"":예정상세(실, "예"+k)}</div></div>
        <div class="줄 판단추">${k===오&&!r.휴식일?''')

# ── 4. 누르기 ──
바꿈('''    case "종목펼침": U.종목펼침 = U.종목펼침===d.v ? null : d.v; break;''',
     '''    case "종목펼침": U.종목펼침 = U.종목펼침===d.v ? null : d.v; break;
    case "예펼침": U.예펼침 = U.예펼침===d.v ? null : d.v; break;''')

# ── 5. CSS ──
css = '''
/* ═══ 10-03 날짜 판 — 종목이 많으면 접기 · 운동 시작 붙박이 ═══ */
.예접기{display:flex;align-items:center;gap:8px;width:100%;height:32px;padding:0;border:0;border-bottom:1px solid var(--선);background:none;font-size:13px;font-weight:700;color:var(--강조);text-align:left;font-family:inherit;cursor:pointer}
.예접기 .번{width:16px;flex:none}
.예접기 svg{width:18px;height:18px;display:block}
.판단추{position:sticky;bottom:0;z-index:2;background:var(--바탕);padding:8px 0;margin:-6px 0 -10px}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]

(SP/'7day-v4.html').write_text(s, encoding='utf-8')
print("v4 →", f"{len(s.encode()):,} 바이트")
