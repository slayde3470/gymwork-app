"""v12 (10-04 홍겸 님) — 캘린더 칸 폭 같게 · 업적 [달성] 탭에 달성 못 한 것도 아래에 · 가려진 칭호 누르면 보임(숨은 업적은 그대로)"""
import sys, pathlib
IN, OUT = sys.argv[1], sys.argv[2]
s = pathlib.Path(IN).read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:70]!r}: {c}번")
    s = s.replace(old, new)
# 달성 탭: 달성(최근 것부터) → 구분 이름표 → 못 한 것
바꿈('''${목록.map(업적줄).join("")||`<div class="빈칸" style="margin-top:12px">아직 없습니다</div>`}''',
     '''${u.분류==="달성" ? 풀.slice().sort((a,b)=>얻[b.번호].순-얻[a.번호].순).map(업적줄).join("")+`<div class="이름표">아직 달성하지 못한 업적</div>`+업적표.filter(a=>!얻[a.번호]).map(업적줄).join("")
      : 목록.map(업적줄).join("")||`<div class="빈칸" style="margin-top:12px">아직 없습니다</div>`}''')
# 가려진 칭호 — 누르면 보이고 다시 누르면 가림 (숨은 업적 ??? 은 그대로)
바꿈('''`<span class="칭호표 잠김 등급-${등}" aria-label="잠긴 칭호">${esc(a.칭호)}</span>`''',
     '''`<button class="칭호표 잠김 등급-${등}${U.칭호보기===a.번호?" 보임":""}" data-act="칭호보기" data-v="${a.번호}" aria-label="잠긴 칭호 보기">${esc(a.칭호)}</button>`''')
바꿈('    case "업적분류더":', '    case "칭호보기": U.칭호보기 = U.칭호보기===d.v ? null : d.v; break;\n    case "업적분류더":')
css = '''
/* ═══ 10-04 v12 — 캘린더 7칸 폭 같게(글이 길어도 칸이 안 늘어남) · 가려진 칭호 보기 ═══ */
.달력{grid-template-columns:repeat(7,minmax(0,1fr))}
.칸날{min-width:0;overflow:hidden}
.칸날>*{max-width:100%;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
button.칭호표.잠김{font-family:inherit;background:none;cursor:pointer}
.칭호표.잠김.보임{color:var(--흐림);text-shadow:none}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]
pathlib.Path(OUT).write_text(s, encoding='utf-8'); print("v12 →", OUT)
