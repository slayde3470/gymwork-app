"""v25 — 10-06 홍겸 님: v24 ②(보고서 종목 요약 한 줄 · 화살표만) 되돌림. v24 ①(루틴 상자 7글자 · 높이)은 그대로
쓰는 법: python3 patch_v25.py IN.html OUT.html"""
import sys, re
s = open(sys.argv[1], encoding="utf-8").read()
def 바꿈(old, new):
    global s
    assert s.count(old) == 1, (s.count(old), old[:80])
    s = s.replace(old, new)
바꿈('''      <span class="보고줄 보고한줄">${[줄1,줄2].map(([a,b,c])=>`<span class="보고값"><i>${a}</i> ${String(b).replace(/kg$/,"")}${보고화살(c)}</span>`).join("")}</span></button>`;   /* 10-06 v24 ② 한 줄 · 화살표만 · kg 은 뺀다(1RM · 볼륨은 kg — 상세에는 그대로) */''',
     '''      ${[줄1,줄2].map(([a,b,c])=>`<span class="보고줄"><span class="보고값"><i>${a}</i> ${b}</span>${c}</span>`).join("")}</button>`;   /* 10-06 v25 v24 ② 한 줄 되돌림(홍겸 님) */''')
# 보고화살 · 보고한줄맞춤 · 관찰자 덩어리 통째로 빼기
시작 = s.index("/* 10-06 v24 ② 요약 칸은 오르내림 화살표만")
끝 = s.index('window.addEventListener("resize", 보고한줄맞춤);\n', 시작) + len('window.addEventListener("resize", 보고한줄맞춤);\n')
assert 끝 - 시작 < 2000
s = s[:시작] + s[끝:]
바꿈('''/* ② 요약 칸 한 줄 — [1RM 76kg▼]  [볼륨 480kg▼] · 칸이 좁으면 글자 사이만 좁힌다(줄은 안 바꿈) */
.보고줄.보고한줄{flex-wrap:nowrap;gap:0 8px;justify-content:flex-start}
.보고한줄>.보고값{min-width:0;flex:none}
.보고한줄 .보고값 i{font-size:.85em}
.보고화살{margin-left:2px;font-size:10px}
''', '')
assert "보고한줄" not in s and "보고화살" not in s
open(sys.argv[2], "w", encoding="utf-8").write(s)
print("✓ v25 적용 →", sys.argv[2])
