"""v10 W 운동 화면 세트 줄 (10-03 ✎ 29cb · j7s1) — python3 patch_v10_W.py IN OUT

29cb "끝난 운동줄을 연초록 말고 우리 중심색을 흐리게 만든걸로 해줘. 색깔이 많으면 UI가 지저분해보이더라고."
  → 끝난 세트 줄 바탕 --좋음옅음 → --면2 (중심색 아주 옅게). '지금' 줄(--강조옅음)보다 한 단계 옅어 둘이 갈린다.
j7s1 "체크 누르면 - + 버튼도 사라지고 게이지가 꽉 채워져야해. 세트 체크는 색깔의 통일성을 위해서 파란색에 흰색 글씨로 바꿔.
      또, 체크박스 안 숫자에는 볼드가 쳐져있으면 안돼. 체크 글자는 지금이 딱 좋아."
  → ① 쉬는 동안 휴식 칸의 − ＋ 를 감춘다 → 남은 시간 게이지가 칸 전체를 채운다 (게이지는 꽉 찬 채 시작해 줄어든다 · 그대로)
    ② 체크된 동그라미 = --강조 바탕 + --강조글 ✓ (초록 → 파랑)
    ③ 동그라미 안 번호는 보통 굵기(400). ✓ 그림은 그대로
color-mix 안 씀 (✎ 표시 사진 html2canvas 가 못 읽는다)
"""
import sys, pathlib
IN, OUT = sys.argv[1], sys.argv[2]
s = pathlib.Path(IN).read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:70]!r}: {c}번")
    s = s.replace(old, new)

# 확인만 — 고칠 자리가 그대로 있는지
바꿈('.운세트들 .세트번호.켬{background:var(--좋음);border-color:var(--좋음);color:var(--좋음글)}',
     '.운세트들 .세트번호.켬{background:var(--강조);border-color:var(--강조);color:var(--강조글)}')
바꿈('.운세트들 .세트줄.완료줄{background:var(--좋음옅음);border-radius:8px}',
     '.운세트들 .세트줄.완료줄{background:var(--면2);border-radius:8px}')

css = '''
/* ═══ 10-03 v10 W — 운동 세트 줄 (✎ 29cb · j7s1) ═══ */
/* j7s1 ③ 동그라미 안 번호는 보통 굵기 */
.운세트들 .세트번호{font-weight:400}
/* j7s1 ① 쉬는 동안 − ＋ 감춤 → 게이지 단추가 칸 전체 */
.운세트들 .휴칸.쉼 > button[data-act="세트값"]{display:none}
.운세트들 .휴칸.쉼 .쉼단추{flex:1 1 auto;width:100%}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]
pathlib.Path(OUT).write_text(s, encoding='utf-8')
print("v10 W →", OUT, f"{len(s.encode()):,} 바이트")
