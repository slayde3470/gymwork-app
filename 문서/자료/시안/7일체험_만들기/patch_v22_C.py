"""v22 C (10-06 홍겸 님) — 운동 화면: 쉼 게이지 글 · '+ 세트' 자동 스크롤
⑫ "폰에서 휴식시간과 그 아래 '건너뛰기' 글씨가 잘려 보인다" — 홍겸 님이 비율로 준 값 그대로:
   · 글자 크기 15% 작게 — 남은 시간 15 → 12.75 · 건너뛰기 11 → 9.35
     (U2-1 여섯 단계(11 · 13 · 15 …) 밖의 값 — 홍겸 님이 비율로 직접 준 값이라 그대로 쓴다)
   · 자간 25% 넓게 — 지금 자간 = 0(단추라 브라우저 기본 normal). 0 의 25% 는 0 이라, 기준을 '글자 사이에 원래 보이는 빈틈'으로 잡았다:
     글꼴이 원래 두는 이웃 글자 잉크 사이 빈틈(글자 크기 기준) 숫자 '0:59' ≈ 0.094em · 한글 '건너뛰기' ≈ 0.17em (캔버스로 잼)
     → 그 25% 를 더한다: 숫자 +0.025em · 글 +0.04em
   · 숫자와 글 사이 30% 줄임 — '사이' = 숫자 잉크 아래끝 ~ 글 잉크 위끝의 빈틈(눈에 보이는 간격). 지금 4.84px → 목표 3.39px(대체 글꼴로 잼).
     줄 높이(line-height 1.1)는 그대로 둔다 — 건너뛰기 글은 overflow:hidden(… 처리)이라 줄 높이를 줄이면 글 위아래가 잘린다.
     대신 글(small)을 위로 당긴다(margin-top 음수 · 글자 크기 기준 em)
⑬ "세트를 추가할 때 세트 단추가 스크롤 칸 밖(아래)으로 넘어가면 그 단추가 보이도록 한 칸 자동 스크롤" —
   '+ 세트' 단추(data-act="세트더") 기준. 누른 뒤 그리기()가 다 끝난 직후(마이크로태스크 · 화면에 그려지기 전) 한 번 재고,
   단추 아래끝이 보이는 칸 아래로 넘어가면 단추가 보일 만큼만 한 번 부드럽게(움직임 줄임이면 바로) 내린다. 안 넘어가면 그대로.
   체크 h6tu 의 '다음 세트 가운데로'(운자리.다음)와 같은 함수 · 같은 방식(scrollTo · behavior)
쓰는 법: python3 patch_v22_C.py IN.html OUT.html"""
import sys, pathlib
IN, OUT = sys.argv[1], sys.argv[2]
s = pathlib.Path(IN).read_text(encoding='utf-8')
def 바꿈(old, new):
    global s
    c = s.count(old)
    assert c == 1, f"❌ {old[:80]!r}: {c}번"
    s = s.replace(old, new)

# ── ⑬ '+ 세트' 를 누르면 표를 남긴다 ─────────────────────────────────────────
바꿈('''    case "세트더": { const x=ss.종목[+d.i], l=x.세트[x.세트.length-1]; x.세트.push({w:l.w,r:l.r,목r:l.r,휴:l.휴,완료:false}); ss.접기[+d.i]=false; break; }''',
     '''    case "세트더": { const x=ss.종목[+d.i], l=x.세트[x.세트.length-1]; x.세트.push({w:l.w,r:l.r,목r:l.r,휴:l.휴,완료:false}); ss.접기[+d.i]=false; 운자리.세트더=true; break; }   /* 10-06 v22 C ⑬ 다시 그린 뒤 운자리맞춤()이 '+ 세트' 단추를 보이게 */''')

# ── ⑬ 다시 그린 직후 한 번 재고 한 번만 움직인다 ─────────────────────────────
바꿈('''      if(목표>목.scrollTop+1) 목.scrollTo({top:목표, behavior:움}); } }              // 아래 줄을 올리기만 한다''',
     '''      if(목표>목.scrollTop+1) 목.scrollTo({top:목표, behavior:움}); } }              // 아래 줄을 올리기만 한다
  /* 10-06 v22 C ⑬ '+ 세트' 를 눌렀으면 — 단추 아래끝이 보이는 칸 아래로 넘어갔을 때만, 단추가 다 보일 만큼 한 번 내린다(목록 아래 여백 8 까지 · 끝을 넘지 않게).
     잰 때 = 그리기()가 다 끝난 직후(마이크로태스크 · 화면에 그려지기 전) 한 번. 그리기() 안 이 자리에서는 아직 시계그리기()가 머리 시계를 채우기 전이라
     머리가 커지며(389 폭 +11px) 목록 칸이 줄어 잰 값이 틀렸다. offsetTop 은 들어옴 움직임(transform) 영향을 받지 않는다 (.운세트들 = position:relative) */
  const 세트더=운자리.세트더; 운자리.세트더=false;
  if(세트더) queueMicrotask(()=>{ const b=목.isConnected&&목.querySelector('[data-act="세트더"]'); if(!b) return;
    const 아래=b.offsetTop+b.offsetHeight, 보는끝=목.scrollTop+목.clientHeight; if(아래<=보는끝+0.5) return;   // 단추가 다 보이면 움직이지 않는다
    const 끝=목.scrollHeight-목.clientHeight, 목표=Math.min(끝, 아래+8-목.clientHeight);
    if(목표>목.scrollTop+1) 목.scrollTo({top:목표, behavior:움}); });''')

# ── ⑫ 쉼 게이지 글 — </style> 마지막 것 바로 앞에 새 블록 ─────────────────────
css = '''/* 10-06 v22 C ⑫ 쉼 게이지 글 잘림 — 홍겸 님 비율 그대로: 글자 15% 작게(15 → 12.75 · 11 → 9.35) · 자간 25% 넓게 · 숫자와 글 사이 30% 줄임.
   자간: 지금 0(normal) → 글꼴이 원래 두는 글자 사이 빈틈(숫자 ≈ 0.094em · 한글 ≈ 0.17em)의 25% 를 더함 = 숫자 +0.025em · 글 +0.04em.
   (긴 문구는 쉼글맞춤()이 넘칠 때만 −0.02em ~ 로 좁히는 것 그대로)
   사이: 숫자 잉크 아래 ~ 글 잉크 위 4.84px → 약 3.39px(글자를 줄여 3.67 · 나머지는 글을 0.03em 당김). 줄 높이 1.1 은 그대로(글은 overflow:hidden 이라 줄 높이를 줄이면 잘린다) · 글을 위로 당긴다 */
.운세트들 .쉼게이지 b{font-size:12.75px;letter-spacing:.025em}
.운세트들 .쉼게이지 small{font-size:9.35px;letter-spacing:.04em}
.운세트들 .쉼게이지 .쉼글>small{margin-top:-.03em}
'''
끝 = s.rfind('</style>')
assert 끝 > 0, '</style> 없음'
s = s[:끝] + css + s[끝:]

pathlib.Path(OUT).write_text(s, encoding='utf-8')
print('✓ v22 C 적용 →', OUT)
