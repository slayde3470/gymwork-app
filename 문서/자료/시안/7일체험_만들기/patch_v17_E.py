"""v17 E (10-04 홍겸 님 "합계숫자랑 각 개별칸도 소수점 없애") — 큰 운동 칸 숫자와 그 ▲▼ 도 반올림 정수"""
import sys, pathlib
IN, OUT = sys.argv[1], sys.argv[2]
s = pathlib.Path(IN).read_text(encoding='utf-8')
old='''<b>${x.v>0?차kg(x.v):"—"}</b>${차보임&&x.v>0&&x.앞>0?보고차(x.v-x.앞,""):""}'''
assert s.count(old)==1
s=s.replace(old,'''<b>${x.v>0?Math.round(x.v):"—"}</b>${차보임&&x.v>0&&x.앞>0?보고차(Math.round(x.v)-Math.round(x.앞),""):""}''')
pathlib.Path(OUT).write_text(s, encoding='utf-8'); print("v17 E →", OUT)
