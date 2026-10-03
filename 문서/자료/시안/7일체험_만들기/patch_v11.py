"""v11 프로필 탭 인스타 꼴 · 캘린더에서 운동 보고서 보기 · 칸 이름 맞춤 (10-03 밤 홍겸 님 말로 한 요청)
python3 patch_v11.py IN OUT

"프로필 레이아웃을 이런식으로(인스타 프로필 사진). 프로필사진 · 닉네임 위치는 같게. 게시물~팔로잉 자리 = 3대(sbd)~5대.
 동그라미 자리 = 최근 달성 업적. 아래 사진칸 = 운동 인증샷(최대 12장, 3:4, 한 줄 3칸, 최대 4줄). 최대 3개 윗칸 고정."
"스쿼트의 스.. 잘림" → 칸 이름이 넘치면 자간을 좁힌다
"운동 끝나고 운동보고서를 볼 방법 — 캘린더 아래 박스 어딘가에 버튼" → 기록한 날 판 맨 아래 줄 [운동 보고서]
새 값: 프로필 탭 사진 88 · 업적 동그라미 64(칸 72) · 인증샷 칸 사이 2 — 작업일지에 적음
"""
import sys, pathlib
IN, OUT = sys.argv[1], sys.argv[2]
s = pathlib.Path(IN).read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:70]!r}: {c}번")
    s = s.replace(old, new)

# ── 핀 아이콘 ──
바꿈("    아래:a('<path d=\"M6 9l6 6 6-6\"/>'),",
     "    아래:a('<path d=\"M6 9l6 6 6-6\"/>'),\n    핀:a('<path d=\"M9 3h6l-1 6 3 3v2H7v-2l3-3z\"/><path d=\"M12 14v7\"/>'),")

# ── 프로필 탭 ──
i = s.index('function 프로필탭(){'); j = s.index('</div></div>`; }', i) + len('</div></div>`; }')
s = s[:i] + '''function 프로필탭(){ const 닉=String(S.설정.닉네임||""), n=인증.length;
  return `<div class="넘김"><h1>프로필</h1><div class="쌓기">
    <div class="인머리 번호"${번("프0")}>${프로필고르기("크게")}<div class="인오른">
      <input class="인닉" data-pf="닉네임" maxlength="12" autocomplete="off" placeholder="닉네임" aria-label="닉네임" value="${esc(닉)}">
      ${큰운동판(큰운동값(null,null), false, false)}</div>${톱니단추()}</div>
    ${최근업적줄()}
    <div class="이름표">운동 인증샷 <span class="옅음 숫">${n}/12</span></div>
    ${인증판()}
    <button class="고르기 프로필메모" data-act="시트" data-t="메모"><b>메모</b><span class="곁">${S.메모.length?`${S.메모.length}개`:""}</span>›</button>
  </div></div>`; }
/* 최근 달성 업적 — 인스타 하이라이트 자리. 달성한 날 늦은 것부터 4개 · 동그라미 테두리 = 등급색 · 누르면 업적 화면 */
function 최근업적줄(){ const 얻=S.업적||{};
  const 목=Object.entries(얻).sort(([,a],[,b])=>String(b.날||"").localeCompare(String(a.날||""))||(b.순||0)-(a.순||0)).map(([k])=>업적찾기(k)).filter(Boolean).slice(0,4);
  if(!목.length) return `<div class="인업적 빈 작 옅음 번호"${번("프3")}>아직 달성한 업적이 없습니다</div>`;
  return `<div class="인업적 번호"${번("프3")}>${목.map(a=>`<button class="인업 등급-${a.숨김?"숨은":a.등급}" data-act="스탯열기" data-v="업적"><span class="인동">${esc([...a.칭호][0]||"")}</span><span class="인업글">${esc(a.칭호)}</span></button>`).join("")}</div>`; }
/* 운동 인증샷 — 최대 12장 · 3:4 · 한 줄 3칸. 고정(최대 3장)이 맨 앞, 나머지는 새것부터. 이 브라우저에만(프로필 사진과 같게) */
function 인증순(){ return [...인증.filter(x=>x.고정).sort((a,b)=>a.고정-b.고정), ...인증.filter(x=>!x.고정).sort((a,b)=>b.때-a.때)]; }
function 인증판(){ const 목=인증순();
  return `<div class="인판 번호"${번("프2")}>${목.map(x=>`<button class="인칸" data-act="시트" data-t="인증" data-v="${x.id}" aria-label="인증샷${x.고정?" (고정)":""}"><img src="${x.src}" alt="">${x.고정?`<span class="인핀">${아이콘.핀}</span>`:""}</button>`).join("")}${목.length<12?`<label class="인칸 인더" aria-label="인증샷 올리기">＋<input type="file" accept="image/*" multiple data-pf="인증" hidden></label>`:""}</div>`; }''' + s[j:]

# 인증샷 저장소 — 프로필 사진 옆
바꿈('let 프로필사진 = null;', 'let 인증 = []; try{ 인증 = JSON.parse(localStorage.getItem(저장키+"-인증")||"[]"); }catch(e){}\nfunction 인증저장(){ try{ localStorage.setItem(저장키+"-인증", JSON.stringify(인증)); return true; }catch(e){ return false; } }\nlet 프로필사진 = null;')

# ── 시트: 인증샷 한 장 ──
바꿈('  else if(종==="메모"){', '''  else if(종==="인증"){ const x=인증.find(p=>p.id===U.시트.대상);
    안 = 머리("인증샷") + (x?`<img class="인큰사진" src="${x.src}" alt=""><div class="줄"><button class="버튼 채움" data-act="인증고정" data-v="${x.id}">${x.고정?"고정 풀기":"맨 위에 고정"}</button><button class="버튼 채움 나쁨" data-act="인증지움" data-v="${x.id}">지우기</button></div>`:""); }
  else if(종==="메모"){''')

# ── 누르기 ──
바꿈('    case "메모지움": S.메모.splice(+d.n,1); break;', '''    case "메모지움": S.메모.splice(+d.n,1); break;
    case "인증고정": { const x=인증.find(p=>p.id===d.v); if(!x) break;
      if(!x.고정 && 인증.filter(p=>p.고정).length>=3){ U.시트=null; 그리기(); 토스트("고정은 3장까지"); return; }
      x.고정 = x.고정 ? 0 : Date.now(); 인증저장(); U.시트=null; break; }
    case "인증지움": 인증=인증.filter(p=>p.id!==d.v); 인증저장(); U.시트=null; break;
    case "보고보기": S.결과={key:d.k}; U.시트=null; break;''')

# ── 캘린더 기록한 날 맨 아래 줄 [운동 보고서] ──
바꿈('''  return `<div class="줄 판단추 록단추">${다시?''', '''  return `<div class="줄 판단추 록단추"><button class="버튼 채움" data-act="보고보기" data-k="${록[록.length-1][0]}">운동 보고서</button>${다시?''')

# ── 칸 이름이 넘치면 자간 좁힘 (스쿼트 → 스…) ──
바꿈('  if(!맞음()) bs.forEach(b=>b.style.letterSpacing="-0.06em"); }); }',
     '  if(!맞음()) bs.forEach(b=>b.style.letterSpacing="-0.06em");\n  판.querySelectorAll(":scope>div>span").forEach(sp=>{ sp.style.letterSpacing=""; if(sp.scrollWidth>sp.clientWidth+0.5) sp.style.letterSpacing="-0.1em"; }); }); }')

css = '''
/* ═══ 10-03 v11 프로필 탭 (인스타 꼴) · 인증샷 · 캘린더 [운동 보고서] ═══ */
.인머리{position:relative;display:flex;align-items:center;gap:16px;padding:8px 0}
.보고사진.크게{width:88px;height:88px}
.보고사진.크게 b{font-size:28px}
.인오른{flex:1;min-width:0;display:flex;flex-direction:column;gap:8px}
.인닉{border:0;border-bottom:1px solid transparent;background:none;padding:0;height:28px;font-family:inherit;font-size:18px;font-weight:700;color:var(--글);width:100%;min-width:0;border-radius:0}
.인닉:focus{outline:none;border-bottom-color:var(--강조)}
.인닉::placeholder{color:var(--옅음)}
.인머리 .결과수.큰수{flex:none;width:100%}
.인업적{display:flex;gap:8px;padding:4px 0}
.인업적.빈{padding:8px 0}
.인업{width:72px;flex:none;display:flex;flex-direction:column;align-items:center;gap:4px;background:none;border:0;padding:0;cursor:pointer;font-family:inherit;color:var(--글)}
.인동{width:64px;height:64px;border-radius:50%;border:2px solid var(--등급색,var(--속선));background:var(--면2);display:flex;align-items:center;justify-content:center;font-size:22px;font-weight:700;box-sizing:border-box}
.인업글{font-size:11px;max-width:72px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.인판{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:2px}
.인칸{position:relative;aspect-ratio:3/4;padding:0;border:0;border-radius:0;background:var(--면2);overflow:hidden;cursor:pointer;display:flex;align-items:center;justify-content:center;color:var(--강조);font-size:28px;font-family:inherit}
.인칸 img{width:100%;height:100%;object-fit:cover;display:block}
.인더{border:1px dashed var(--속선);box-sizing:border-box}
.인핀{position:absolute;top:4px;right:4px;width:24px;height:24px;border-radius:50%;background:var(--강조);color:var(--강조글);display:flex;align-items:center;justify-content:center}
.인핀 svg{width:16px;height:16px}
.인큰사진{display:block;width:100%;max-height:50vh;object-fit:contain;border-radius:8px;margin-bottom:12px;background:var(--면2)}
.록단추 .버튼{min-width:0;padding-left:8px;padding-right:8px;white-space:nowrap}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]

# 인증샷 올리기 — 프로필 사진 고르기와 같은 곳(change)
js = '''
<script>
document.addEventListener("change", async e=>{ const el=e.target; if(el?.dataset?.pf!=="인증") return;
  const fs=[...(el.files||[])]; let 넘침=false, 못읽음=false, 못저장=false;
  for(const f of fs){ if(인증.length>=12){ 넘침=true; break; }
    try{ 인증.push({id:"p"+Date.now().toString(36)+Math.random().toString(36).slice(2,6), src:await 줄인사진(f), 때:Date.now(), 고정:0}); }catch(_){ 못읽음=true; } }
  el.value=""; if(!인증저장()) 못저장=true; 그리기();
  if(넘침) 토스트("인증샷은 12장까지"); else if(못읽음) 토스트("이 사진은 읽지 못했습니다"); else if(못저장) 토스트("저장 공간이 모자라 이 화면에만 남습니다"); });
</script>
'''
끝 = s.rfind('</body>'); s = s[:끝] + js + s[끝:]
pathlib.Path(OUT).write_text(s, encoding='utf-8')
print("v11 →", OUT, f"{len(s.encode()):,} 바이트")
