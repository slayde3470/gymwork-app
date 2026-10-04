"""v19 B (10-04 홍겸 님) — 운동 보고서 띠: ‹ › 지움 → 왼쪽 카메라(이미지 저장) · 오른쪽 공유
"운동보고서 띠의 < > 는 삭제하고, < 있던 위치에 작은 카메라 모양으로 운동보고서를 이미지로 저장할 수 있게 하는 기능을 넣고
 (운동기록 저장하고 종료줄과 박스부터 아래 캘린더~프로필 탭은 사진에 찍히지 않음. 운동보고서에 나타나는 종목이 많아서
 스크롤을 아래로 내려야 한다고 하더라도 한 이미지에 전부 찍히게 구성)
 > 위치에 '공유' 아이콘을 만들어서 공유하면 이미지를 인스타 스토리나 카카오톡 이런데에 공유할 수 있는 기능을 만들자."
찍히는 것 = 띠(제목 · 날짜) + 프로필 상자 + 루틴 상자 + 종목 칸 전부(스크롤로 가려진 것까지)
안 찍히는 것 = 아래 단추줄 · 탭줄 · 업적띠 · 토스트 · 카메라/공유 단추 자체 · 톱니(설정 단추)
"""
import sys, pathlib
IN, OUT = sys.argv[1], sys.argv[2]
s = pathlib.Path(IN).read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    k = s.count(old)
    if k != n: raise SystemExit(f"{k}번 (기대 {n}): {old[:80]}")
    s = s.replace(old, new)

# ── 아이콘 — 카메라 · 공유(안드로이드 모양: 점 셋을 잇는 선). 다른 아이콘과 같은 선 아이콘 ──
바꿈('''    체크:`<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"''',
     '''    카메라:a('<path d="M3 8.5A2 2 0 0 1 5 6.5h2.6L9.2 4h5.6l1.6 2.5H19a2 2 0 0 1 2 2V18a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><circle cx="12" cy="13" r="3.5"/>'),   // v19 B 보고서 이미지 저장
    공유:a('<circle cx="18" cy="5" r="2.5"/><circle cx="6" cy="12" r="2.5"/><circle cx="18" cy="19" r="2.5"/><path d="M8.2 13.3l7.6 4.4M15.8 6.3l-7.6 4.4"/>'),   // v19 B 보고서 공유
    체크:`<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"''')

# ── 띠: ‹ 업적 · › 스탯 지움 → 왼쪽 카메라 · 오른쪽 공유 (자리 · 크기 32×40 그대로). data-day = 파일 이름의 날짜 ──
바꿈('''<div class="띠 결과띠 보고띠" data-enter="결과띠${저장키값||ss.시작}"><button class="보고넘김 왼" data-act="스탯열기" data-v="업적" aria-label="업적">‹</button>''',
     '''<div class="띠 결과띠 보고띠" data-enter="결과띠${저장키값||ss.시작}" data-day="${날}"><button class="보고찍기 왼" data-shot="저장" aria-label="보고서를 이미지로 저장">${아이콘.카메라}</button>''')
바꿈('''</span></div><button class="보고넘김 오" data-act="스탯열기" data-v="스탯" aria-label="스탯">›</button></div>''',
     '''</span></div><button class="보고찍기 오" data-shot="공유" aria-label="보고서 이미지 공유">${아이콘.공유}</button></div>''')
# 쓰지 않게 된 ‹ › CSS 지움
바꿈('''.보고넘김{position:absolute;top:50%;margin-top:-20px;width:32px;height:40px;display:flex;align-items:center;justify-content:center;font-size:22px;line-height:1;color:var(--강조글);background:none;border:0;padding:0}
.보고넘김.왼{left:0}.보고넘김.오{right:0}
''', '')

css = '''
/* ═══ 10-04 v19 B — 보고서 띠 카메라 · 공유 (‹ › 자리 · 크기 그대로: 누르는 칸 32 × 40, 아이콘 18) ═══ */
.보고찍기{position:absolute;top:50%;margin-top:-20px;width:32px;height:40px;display:flex;align-items:center;justify-content:center;color:var(--강조글);background:none;border:0;padding:0}
.보고찍기.왼{left:0}.보고찍기.오{right:0}
.보고찍기 svg{width:18px;height:18px}
.보고찍기:disabled{opacity:.4}
/* 이미지용 복제본 — 화면 밖에 펼쳐 둔다(높이 제한 · 스크롤 없음 · 움직임 없음). 찍고 바로 지운다 */
.폰.보고찍틀{position:absolute;left:-10000px;top:0;flex:none;display:block;height:auto;min-height:0;max-height:none;overflow:visible;border:0;border-radius:0}
.보고찍틀 .넘김.결과틀{flex:none;height:auto;max-height:none;overflow:visible;transform:none}
.보고찍틀 .보고목록{flex:none;height:auto;max-height:none;overflow:visible}
.보고찍틀 *,.보고찍틀 *::before,.보고찍틀 *::after{animation:none!important;transition:none!important}
/* html2canvas 는 글자를 한 자씩 캔버스에 그리는데 캔버스는 '같은 폭 숫자'(tabular-nums)를 모른다 → '1' 뒤가 벌어져 '1 40' 처럼 찍힌다. 이미지에서만 보통 숫자로 */
.보고찍틀,.보고찍틀 *{font-variant-numeric:normal!important}
.보고찍틀 .보고찍기,.보고찍틀 .톱니단추{visibility:hidden}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]

js = '''<script>
/* ═══ 10-04 v19 B — 운동 보고서를 이미지로 저장 · 공유 ═══
   찍는 법: 보고서 내용(.결과틀)을 화면 밖에 복제해 펼친 뒤(높이 제한 · 스크롤 없이) html2canvas 로 한 장에 찍는다.
   그래서 종목이 많아 스크롤해야 보이는 칸까지 모두 들어가고, 아래 단추줄 · 탭줄 · 업적띠 · 토스트는 복제하지 않으니 안 찍힌다.
   html2canvas 는 ✎ 표시 도구와 같은 주소에서, 처음 누를 때만 불러온다 (이미 있으면 그대로 쓴다) */
(()=>{
  let 찍는중=false, 도구=null, 내림=null;
  function 그림도구(){ if(window.html2canvas) return Promise.resolve(window.html2canvas); if(도구) return 도구;
    도구=new Promise((ok,no)=>{ const t=document.createElement("script"); t.src="https://cdnjs.cloudflare.com/ajax/libs/html2canvas/1.4.1/html2canvas.min.js";
      t.onload=()=>ok(window.html2canvas); t.onerror=()=>{ 도구=null; no(new Error("이미지 도구를 못 불러옴")); }; document.head.appendChild(t); }); return 도구; }

  async function 보고이미지(){
    const 폰=document.getElementById("폰"), 원=폰.querySelector(".결과틀"); if(!원) throw new Error("보고서가 없습니다");
    const h2c=await 그림도구();
    const 틀=document.createElement("div"); 틀.className="폰 번호끔 보고찍틀"; 틀.setAttribute("aria-hidden","true");
    틀.style.width=원.getBoundingClientRect().width+"px";   // 폰 안 보고서와 같은 폭 → 줄바꿈 · 칸 배치가 화면과 같다
    const 복=원.cloneNode(true);
    [...복.children].forEach(c=>{ if(!c.matches(".보고띠, .보고상자, .보고목록")) c.remove(); });   // 보고서 내용만
    [복, ...복.querySelectorAll("*")].forEach(el=>{ el.classList.remove("들어옴","나감"); if(el.id) el.removeAttribute("id"); });
    복.style.transform="";
    /* 숫자 올라가는 움직임이 아직 도는 중이어도 끝난 값으로 */
    복.querySelectorAll("[data-count]").forEach(el=>{ const 꼴=숫자꼴[el.dataset.fmt]||콤마; el.textContent=꼴(+el.dataset.count); });
    복.querySelectorAll("input").forEach(el=>el.remove());
    틀.appendChild(복); document.body.appendChild(틀);
    try{
      await new Promise(r=>requestAnimationFrame(()=>r()));
      const 높=Math.ceil(틀.getBoundingClientRect().height), 배=Math.min(2, 16000/Math.max(1,높));   // 아주 긴 보고서는 캔버스 한도(약 16000) 안으로
      const c=await h2c(틀,{backgroundColor:getComputedStyle(폰).backgroundColor, scale:배, logging:false, useCORS:true});
      return await new Promise((ok,no)=>c.toBlob(b=>b?ok(b):no(new Error("이미지 만들기 실패")),"image/png"));
    } finally { 틀.remove(); }
  }
  const 파일이름=()=>`운동보고서_${document.querySelector("#폰 .보고띠")?.dataset.day||오늘()}.png`;

  /* 내려받기 — claude.ai 에서는 downloads 기능(보는 사람이 확인 창에서 허락), 그 밖에서는 <a download>.
     ※ 안드로이드 앱에서는 이 자리가 '갤러리에 저장'(MediaStore · Pictures/하젠하이데)으로 바뀐다 */
  async function 내려받기(blob, 이름){
    if(!내림 && window.claude && typeof window.claude.use==="function") 내림 = window.claude.use("downloads").catch(()=>null);   // claude.ai 밖(window.claude 없음)이면 기억하지 않는다
    const dl=내림 ? await 내림 : null;
    if(dl){ await dl.save({filename:이름, data:blob}); return; }
    const a=document.createElement("a"), u=URL.createObjectURL(blob); a.href=u; a.download=이름; document.body.appendChild(a); a.click(); a.remove();
    setTimeout(()=>URL.revokeObjectURL(u), 4000); }

  /* 공유 — 폰의 공유 창(인스타 스토리 · 카카오톡 등은 거기서 고른다). 파일 공유가 안 되는 기기는 저장으로 대신.
     ※ 안드로이드 앱에서는 이 자리가 공유 Intent(ACTION_SEND · image/png · FileProvider)로 바뀐다 */
  async function 공유하기(blob, 이름){
    const f=new File([blob], 이름, {type:"image/png"});
    if(navigator.canShare && navigator.share && navigator.canShare({files:[f]})){
      try{ await navigator.share({files:[f], title:"운동 보고서"}); return "공유"; }
      catch(e){ if(e && e.name==="AbortError") return "취소"; }   // 공유 창을 닫음 = 그대로 끝. 그 밖의 막힘은 아래 저장으로
    }
    토스트("이 기기에서는 공유 대신 저장합니다");
    await 내려받기(blob, 이름); return "대신저장"; }

  function 단추막기(v){ document.querySelectorAll("#폰 .보고찍기").forEach(b=>{ b.disabled=v; b.setAttribute("aria-busy", v?"true":"false"); }); }
  document.addEventListener("click", async e=>{
    const b=e.target.closest?.("#폰 .보고찍기[data-shot]"); if(!b) return;
    if(찍는중) return;   // 두 번 눌림 방지
    찍는중=true; 단추막기(true); const 할=b.dataset.shot;
    try{
      const blob=await 보고이미지(), 이름=파일이름();
      if(할==="공유"){ const r=await 공유하기(blob, 이름); if(r==="대신저장") setTimeout(()=>토스트("저장했습니다"),1600); }
      else { await 내려받기(blob, 이름); 토스트("저장했습니다"); }
    }catch(err){
      const c=err&&err.code;
      토스트(c==="declined" ? "저장하지 않았습니다" : "이미지를 만들지 못했습니다");
    }finally{ 찍는중=false; 단추막기(false); }
  });
  window.보고이미지 = 보고이미지;   // 시험용
})();
</script>
'''
바꿈('</body></html>', js + '</body></html>')
pathlib.Path(OUT).write_text(s, encoding='utf-8'); print("v19 B →", OUT)
