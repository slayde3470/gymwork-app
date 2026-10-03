"""v15 (10-04 홍겸 님 비율 그대로) — N대 상자: kg 글자 −25% · 상자 너비 −15%(세 칸 가운데) · 상자와 SBD 사이 −50%
프로필: 닉네임 ↔ 상자 ↔ SBD 사이 −50% · 최근 업적 줄 −25%(동그라미 64→48 · 글 22→16) · '운동 인증샷 n/12' 글 지움"""
import sys, pathlib
IN, OUT = sys.argv[1], sys.argv[2]
s = pathlib.Path(IN).read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:70]!r}: {c}번")
    s = s.replace(old, new)
바꿈('''<p class="큰합"><b>${esc(합.글)} ${합.v>0?차kg(합.v)+"kg":"—"}</b>''', '''<p class="큰합"><b>${esc(합.글)} ${합.v>0?차kg(합.v)+`<small class="큰합단위">kg</small>`:"—"}</b>''')
바꿈('''    <div class="이름표">운동 인증샷 <span class="옅음 숫">${n}/12</span></div>\n''', '')
css = '''
/* ═══ 10-04 v15 — 홍겸 님이 준 비율 (새 값: 상자 85% · 사이 6 · 4 · 업적 48) ═══ */
.결과수.큰수 .큰합{justify-self:center;width:85%;margin-bottom:0}
.결과수.큰수 .큰합 .큰합단위{font-size:.75em;font-weight:700}
.결과수.큰수{row-gap:6px}
.인오른{gap:4px}
.인업{width:64px}
.인동{width:48px;height:48px;font-size:16px}
.인업글{max-width:64px}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]
pathlib.Path(OUT).write_text(s, encoding='utf-8'); print("v15 →", OUT)
