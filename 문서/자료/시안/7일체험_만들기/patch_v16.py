"""v16 (10-04 홍겸 님) — N대 상자: 칸 줄 전체 너비에서 좌우 15 비움(가운데) · 상자↔칸 사이 6→4 (−35%) · 숫자↔이름 사이 −35%"""
import sys, pathlib
IN, OUT = sys.argv[1], sys.argv[2]
s = pathlib.Path(IN).read_text(encoding='utf-8')
css = '''
/* ═══ 10-04 v16 — 홍겸 님 값 (새 값: 상자 좌우 15 · 사이 4 · 숫자 줄 22 · 이름 줄 14) ═══ */
.결과수.큰수 .큰합{grid-column:1 / -1;justify-self:stretch;width:auto;margin:0 15px}
.결과수.큰수{row-gap:4px}
.결과수.큰수 b{line-height:22px}
.결과수.큰수>div>span{line-height:14px}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]
pathlib.Path(OUT).write_text(s, encoding='utf-8'); print("v16 →", OUT)
