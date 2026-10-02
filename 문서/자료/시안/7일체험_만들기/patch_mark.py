import pathlib
SP = pathlib.Path('/tmp/claude-0/-home-claude-gymwork-app/5490127f-d7d8-598c-b152-f49350fb76bc/scratchpad')
s = (SP/'7day-new.html').read_text(encoding='utf-8')
def 바꿈(old, new, n=1):
    global s
    c = s.count(old)
    if c != n: raise SystemExit(f"❌ {old[:60]!r}: {c}번")
    s = s.replace(old, new)

# ── 1. 단추 · 도구줄 · 그림판 ──
바꿈('      <button class="작은" data-act="처음부터">처음부터</button>',
     '      <button class="작은" data-act="처음부터">처음부터</button>\n'
     '      <button class="작은 표시단추" id="표시단추" type="button" aria-pressed="false">✎ 표시</button>')
바꿈('    <div class="날들" id="날들"></div>',
     '''    <div class="날들" id="날들"></div>
    <div class="표시줄" id="표시줄" hidden>
      <div class="표시안내">폰 화면 위에 손가락으로 빨갛게 그으세요. 그리는 동안은 앱을 누를 수 없습니다.</div>
      <div class="표시칸">
        <input id="표시메모" class="표시메모" maxlength="300" placeholder="무엇이 문제인지 한 줄 (적지 않아도 됨)" autocomplete="off">
        <button class="작은" id="표시되돌" type="button">되돌리기</button>
        <button class="작은" id="표시지움" type="button">지우기</button>
        <button class="작은 표시저장" id="표시저장" type="button">저장</button>
      </div>
    </div>
    <div class="표시알림" id="표시알림" role="status" hidden></div>''')
바꿈('  <div class="폰" id="폰"></div>',
     '  <div class="폰" id="폰"></div>\n  <canvas class="표시판" id="표시판" hidden aria-label="빨간 펜으로 표시하는 칸"></canvas>')

# ── 2. CSS ──
css = '''
/* ═══ 표시 도구 (10-02) — 폰 화면 위에 빨갛게 긋고 저장하면 Claude 가 읽어 간다 ═══ */
.틀{position:relative}
.표시단추[aria-pressed="true"]{background:var(--나쁨);border-color:var(--나쁨);color:#fff;font-weight:700}
.표시줄{display:flex;flex-direction:column;gap:8px}
.표시줄[hidden]{display:none}
.표시안내{font-size:11px;opacity:.85}
.표시칸{display:flex;gap:4px;align-items:center}
.표시메모{flex:1;min-width:0;height:32px;border-radius:8px;border:1px solid #ffffff55;background:#ffffff14;color:var(--체험글);padding:0 8px;font-size:13px;font-family:inherit}
.표시메모::placeholder{color:var(--체험글);opacity:.6}
.표시줄 .작은{height:32px;padding:0 8px}
.표시저장{background:var(--나쁨);border-color:var(--나쁨);color:#fff;font-weight:700}
.표시알림{font-size:11px;font-weight:700}
.표시판{position:absolute;z-index:60;touch-action:none;cursor:crosshair;border-radius:16px}
'''
끝 = s.rfind('</style>'); s = s[:끝] + css + s[끝:]

# ── 3. 스크립트 ──
js = r'''
<script>
/* ═══ 표시 도구 — 저장하면 db 의 'marks' 모음에 한 줄 (모음 이름은 영문만 된다), 화면 사진은 assets 에 ═══
   Claude 는 ArtifactData 로 'marks' 를 읽고, 그림 id 로 사진을 받아 본다 */
(()=>{
  const 폰=document.getElementById("폰"), 판=document.getElementById("표시판"), 단추=document.getElementById("표시단추");
  const 줄=document.getElementById("표시줄"), 메모칸=document.getElementById("표시메모"), 알림칸=document.getElementById("표시알림");
  const 저장단추=document.getElementById("표시저장");
  let 켬=false, 선들=[], 지금선=null, 알림시계=null, 남은수=0, 저장중=false;
  const 붓색=()=>getComputedStyle(document.documentElement).getPropertyValue("--나쁨").trim()||"#B3261E";
  const 쓰기=n=> (window.claude && typeof window.claude.use==="function") ? window.claude.use(n).catch(()=>null) : Promise.resolve(null);

  function 알림(글, 오래=false){ 알림칸.textContent=글; 알림칸.hidden=false; clearTimeout(알림시계); 알림시계=setTimeout(()=>{ 알림칸.hidden=true; }, 오래?9000:3500); }
  function 단추글(){ 단추.textContent = "✎ 표시" + (남은수>0?` · ${남은수}`:""); }

  function 맞추기(){ if(!켬) return; const dpr=window.devicePixelRatio||1;
    판.style.left=폰.offsetLeft+"px"; 판.style.top=폰.offsetTop+"px"; 판.style.width=폰.offsetWidth+"px"; 판.style.height=폰.offsetHeight+"px";
    판.width=Math.round(폰.offsetWidth*dpr); 판.height=Math.round(폰.offsetHeight*dpr); 다시그림(); }
  function 긋기(ctx, 줄들, w, h, 굵기){ ctx.strokeStyle=붓색(); ctx.lineWidth=굵기; ctx.lineCap="round"; ctx.lineJoin="round"; ctx.globalAlpha=.9;
    for(const l of 줄들){ if(!l.length) continue; ctx.beginPath(); ctx.moveTo(l[0][0]*w,l[0][1]*h);
      if(l.length===1) ctx.lineTo(l[0][0]*w+0.1,l[0][1]*h); for(let i=1;i<l.length;i++) ctx.lineTo(l[i][0]*w,l[i][1]*h); ctx.stroke(); }
    ctx.globalAlpha=1; }
  function 다시그림(){ const c=판.getContext("2d"); c.clearRect(0,0,판.width,판.height); 긋기(c, 지금선?[...선들,지금선]:선들, 판.width, 판.height, 4*(window.devicePixelRatio||1)); }

  function 켜기(v){ 켬=v; 단추.setAttribute("aria-pressed", v?"true":"false"); 줄.hidden=!v; 판.hidden=!v;
    if(v){ 맞추기(); } else { 지금선=null; } }
  단추.addEventListener("click", ()=>켜기(!켬));
  new ResizeObserver(맞추기).observe(폰); window.addEventListener("resize", 맞추기);

  const 자리=e=>{ const r=판.getBoundingClientRect(); return [Math.min(1,Math.max(0,(e.clientX-r.left)/r.width)), Math.min(1,Math.max(0,(e.clientY-r.top)/r.height))]; };
  판.addEventListener("pointerdown", e=>{ e.preventDefault(); 판.setPointerCapture(e.pointerId); 지금선=[자리(e)]; 다시그림(); });
  판.addEventListener("pointermove", e=>{ if(!지금선) return; const p=자리(e), q=지금선[지금선.length-1], r=판.getBoundingClientRect();
    if(Math.hypot((p[0]-q[0])*r.width,(p[1]-q[1])*r.height)<2) return; 지금선.push(p); 다시그림(); });
  const 끝=()=>{ if(!지금선) return; 선들.push(지금선.map(([x,y])=>[+x.toFixed(4),+y.toFixed(4)])); 지금선=null; 다시그림(); };
  판.addEventListener("pointerup", 끝); 판.addEventListener("pointercancel", 끝);
  document.getElementById("표시되돌").addEventListener("click", ()=>{ 선들.pop(); 다시그림(); });
  document.getElementById("표시지움").addEventListener("click", ()=>{ 선들=[]; 다시그림(); });

  /* 화면 사진 — html2canvas 를 처음 저장할 때만 불러온다 */
  let 도구=null;
  function 사진도구(){ if(window.html2canvas) return Promise.resolve(window.html2canvas); if(도구) return 도구;
    도구=new Promise((ok,no)=>{ const t=document.createElement("script"); t.src="https://cdnjs.cloudflare.com/ajax/libs/html2canvas/1.4.1/html2canvas.min.js";
      t.onload=()=>ok(window.html2canvas); t.onerror=()=>{ 도구=null; no(new Error("사진 도구를 못 불러옴")); }; document.head.appendChild(t); }); return 도구; }
  async function 화면사진(){ const h2c=await 사진도구(); const 배=getComputedStyle(폰).backgroundColor;
    const c=await h2c(폰,{backgroundColor:배, scale:1.5, logging:false, useCORS:true});
    const x=c.getContext("2d"); x.setTransform(1,0,0,1,0,0);   // html2canvas 가 남긴 이동 · 확대를 지우고 긋는다 (안 지우면 선이 폰 위치만큼 밀린다)
    긋기(x, 선들, c.width, c.height, 6);
    return await new Promise((ok,no)=>c.toBlob(b=>b?ok(b):no(new Error("사진 만들기 실패")),"image/jpeg",0.85)); }
  /* 그때 앱 상태 — 사진(data:)은 빼고. Claude 가 같은 화면을 다시 띄워 볼 때 쓴다 */
  function 상태글(){ return JSON.stringify({S, U:{탭:U.탭, 시트:U.시트||null, 루틴열림:U.루틴열림??null, 고른날:U.고른날??null, 본:U.본??null}},
    (k,v)=> typeof v==="string" && v.startsWith("data:") ? "(사진 생략)" : v); }

  저장단추.addEventListener("click", async ()=>{
    if(저장중) return; const 메모=메모칸.value.trim();
    if(!선들.length && !메모){ 알림("먼저 빨갛게 그어 주세요"); return; }
    저장중=true; 저장단추.textContent="저장 중…";
    try{
      const db=await 쓰기("db");
      if(!db){ 알림("이 화면에서는 저장할 수 없습니다 — claude.ai 에 로그인한 채 열어 주세요", true); return; }
      const assets=await 쓰기("assets"); let 그림=null, 상태=null, 그림오류=null;
      if(assets){
        try{ 그림=(await assets.upload(await 화면사진(), {type:"image/jpeg"})).id; }catch(e){ 그림오류=String(e?.code||e?.message||e); }
        try{ 상태=(await assets.upload(new Blob([상태글()],{type:"application/json"}), {type:"application/json"})).id; }catch(e){}
      } else 그림오류="사진 저장 권한 없음";
      await db.collection("marks").add({ 시각:new Date().toISOString(), 일차:S.일차, 날:오늘(), 화면:화면키(), 탭:U.탭,
        시트:U.시트?.종류||null, 본:U.본??null, 메모, 선:선들, 폭:폰.offsetWidth, 높이:폰.offsetHeight, 그림, 상태, 그림오류, 확인:false });
      선들=[]; 메모칸.value=""; 켜기(false);
      알림(그림?"저장했습니다 — Claude 에게 '표시해 놨어' 라고 말해 주세요":"선과 메모는 저장했습니다 (화면 사진은 못 찍음) — Claude 에게 말해 주세요", true);
    }catch(e){ 알림("저장하지 못했습니다 — "+(e?.code==="quota_exceeded"?"저장 칸이 찼습니다":"잠시 뒤 다시 눌러 주세요"), true); }
    finally{ 저장중=false; 저장단추.textContent="저장"; }
  });

  /* 아직 Claude 가 확인하지 않은 표시 수를 단추에 */
  쓰기("db").then(db=>{ if(!db) return;
    db.collection("marks").where("확인","==",false).onSnapshot(q=>{ 남은수=q.size; 단추글(); }, ()=>{}); });
})();
</script>
'''
바꿈('</script>\n\n</body></html>', '</script>\n' + js + '\n</body></html>')
(SP/'7day-mark.html').write_text(s, encoding='utf-8')
print("표시 도구 넣음 →", f"{len(s.encode()):,} 바이트")
