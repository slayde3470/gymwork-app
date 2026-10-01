<title>하젠하이데 근육 지도</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;700&display=swap">
<style>
:root{
  --바탕:#F6F7F6;--면:#FFFFFF;--면2:#EFF2F0;--선:#DFE4E1;--글:#1A1D1B;--흐림:#5F7169;--옅음:#94A39C;--강조:#2E5E4E;--강조글:#FFFFFF;--강조옅음:#E3EEE8;
  --피부:#B9C3BE;--근육:#D6DDD9;--결:#AEB9B3;--틈:#EFF2F0;--경고:#A8641C;
}
@media (prefers-color-scheme:dark){:root:not([data-theme="light"]){
  --바탕:#111413;--면:#1B1F1D;--면2:#242926;--선:#323936;--글:#E4E7E5;--흐림:#A2B4AB;--옅음:#6E7E77;--강조:#8ED1B7;--강조글:#00372A;--강조옅음:#1F2E28;
  --피부:#3A433F;--근육:#4A5550;--결:#39423E;--틈:#242926;--경고:#E6B676;}}
:root[data-theme="dark"]{
  --바탕:#111413;--면:#1B1F1D;--면2:#242926;--선:#323936;--글:#E4E7E5;--흐림:#A2B4AB;--옅음:#6E7E77;--강조:#8ED1B7;--강조글:#00372A;--강조옅음:#1F2E28;
  --피부:#3A433F;--근육:#4A5550;--결:#39423E;--틈:#242926;--경고:#E6B676;}
*{box-sizing:border-box}
[hidden]{display:none!important}
body{margin:0;background:var(--바탕);color:var(--글);font-family:"Noto Sans KR",system-ui,sans-serif;font-size:15px;line-height:1.4;letter-spacing:-.01em;padding-inline:16px;padding-block:20px 40px;font-variant-numeric:tabular-nums}
button,select,textarea{font:inherit;color:inherit}
.wrap{max-width:920px;margin:0 auto;display:grid;gap:16px}
h1{font-size:22px;letter-spacing:-.02em;margin:0;line-height:1.25}
.sub{font-size:13px;color:var(--흐림);margin:4px 0 0}
.tabs{display:flex;gap:4px;background:var(--면);border:1px solid var(--선);border-radius:10px;padding:3px}
.tabs button{flex:1;height:36px;border:0;border-radius:8px;background:none;font-size:13px;font-weight:500;color:var(--흐림);cursor:pointer}
.tabs button.on{background:var(--강조);color:var(--강조글);font-weight:700}
.stage{display:grid;grid-template-columns:minmax(0,1.25fr) minmax(0,1fr);gap:16px;align-items:start}
@media (max-width:760px){.stage{grid-template-columns:1fr}}
.card{background:var(--면);border:1px solid var(--선);border-radius:16px;padding:16px;display:grid;gap:12px;align-content:start}
.fig svg{width:100%;height:auto;display:block}
.fig .muscle{fill:var(--근육);stroke:var(--틈);stroke-width:1.2;stroke-linejoin:round;transition:fill .6s ease;cursor:pointer}
.fig .base{fill:var(--피부);stroke:none}
.fig .detail path{fill:none;stroke:var(--결);stroke-width:.7;opacity:.8}
.fig .muscle.sel{stroke:var(--강조);stroke-width:2}
.lab{font-size:11px;font-weight:700;color:var(--옅음);letter-spacing:.04em}
.s{font-size:13px;color:var(--흐림)}
.chips{display:flex;gap:6px;flex-wrap:wrap}
.chip{height:28px;padding:0 12px;border-radius:99px;border:0;background:var(--면2);color:var(--흐림);font-size:13px;font-weight:500;cursor:pointer;white-space:nowrap}
.chip.on{background:var(--강조);color:var(--강조글);font-weight:700}
.btn{height:36px;border-radius:8px;border:1px solid var(--선);background:var(--면2);font-size:13px;font-weight:700;padding:0 12px;cursor:pointer}
.btn.p{background:var(--강조);border-color:var(--강조);color:var(--강조글)}
.legend{display:grid;grid-template-columns:repeat(21,1fr);gap:2px}
.legend i{height:14px;border-radius:3px;display:block}
.legend+.ticks{display:flex;justify-content:space-between;font-size:11px;color:var(--옅음)}
input[type=range]{width:100%;accent-color:var(--강조)}
.row{display:flex;align-items:center;gap:8px}
.list{display:grid;font-size:13px}
.list>div{display:grid;grid-template-columns:1fr 64px 44px 56px;gap:8px;align-items:center;padding:6px 0;border-top:1px solid var(--선)}
.sw{width:18px;height:18px;border-radius:5px;justify-self:end}
.rest{display:grid;grid-template-columns:auto 1fr;gap:6px 10px;align-items:center;font-size:13px}
.rest select{height:32px;border-radius:8px;border:1px solid var(--선);background:var(--면);padding:0 8px}
textarea{width:100%;min-height:120px;border-radius:10px;border:1px solid var(--선);background:var(--면);padding:10px;font-family:ui-monospace,monospace;font-size:12px}
.ok{color:var(--강조);font-weight:700}.bad{color:var(--경고);font-weight:700}
.rep{font-size:13px;line-height:1.7}
.big{font-size:18px;font-weight:700;line-height:1.3}
.roles>div{grid-template-columns:1fr auto}
.rchips{display:flex;gap:4px}
.rchips .chip{height:26px;padding:0 9px;font-size:12px}
.tree{font-size:13px;max-height:560px;overflow:auto;display:grid}
.tree div{display:flex;gap:6px;align-items:baseline;padding:3px 0;border-top:1px solid var(--선)}
.tree .la{font-size:11px;color:var(--옅음);font-style:italic}
.tree .st{width:14px;flex:none;color:var(--강조)}
.tree .deep{font-size:11px;color:var(--옅음)}
.tree .r{font-weight:700;padding-top:10px}
</style>

<div class="wrap">
  <div>
    <h1>근육 지도 · 실시간 피로</h1>
    <p class="sub">그림 · 근육 목록 · 색표를 따로 두고 규칙으로 이어 붙였습니다. 그림이 바뀌어도 규칙만 지키면 그대로 칠해집니다.</p>
  </div>
  <div class="tabs" id="tabs"><button class="on" data-t="work">운동 중 달성도</button><button data-t="rest">피로 회복</button><button data-t="tree">근육 목록</button><button data-t="check">새 그림 검사</button></div>
  <div class="stage">
    <div class="card">
      <div class="fig" id="fig"></div>
      <div>
        <div class="row" style="justify-content:space-between"><span class="lab">색표 · 20단계</span><span class="chips" id="scales"></span></div>
        <div class="legend" id="legend" style="margin-top:8px"></div>
        <div class="ticks"><span>0 기본</span><span>5</span><span>10</span><span>15</span><span>20</span></div>
      </div>
      <div class="row"><button class="btn" id="anim">움직임 예시 (팔 굽히기)</button><span class="s" id="pick">근육을 누르면 이름</span></div>
    </div>

    <div class="card" id="p-work">
      <div class="lab">예시 루틴 · 가슴, 팔 (18세트)</div>
      <div class="row"><input type="range" id="done" min="0" max="18" value="12" aria-label="끝낸 세트"><b id="donev" style="width:56px;text-align:right"></b></div>
      <div class="s" id="nowex"></div>
      <div class="list" id="wlist"></div>
      <div class="s">단계 = 20 × 오늘 쌓인 유효 볼륨 ÷ <b>이전 최대 볼륨</b> (임시 기준). 유효 볼륨 = 무게 × 횟수 × 역할 비중 — 주동근 1 · 보조근 0.5 · 협응근 0.25. 이전 최대를 넘으면 20에서 멈춘다. 처음 하는 근육은 오늘 계획한 볼륨이 기준.</div>
      <div class="lab" style="margin-top:4px">종목 근육 설정 · 기본값 / 커스텀</div>
      <div class="row"><select id="exsel" aria-label="종목" style="height:32px;border-radius:8px;border:1px solid var(--선);background:var(--면);padding:0 8px;flex:1"></select><button class="btn" id="exreset">기본값으로</button></div>
      <div class="list roles" id="exroles"></div>
      <div class="row"><select id="addm" aria-label="근육 더하기" style="height:32px;border-radius:8px;border:1px solid var(--선);background:var(--면);padding:0 8px;flex:1"></select><button class="btn" id="addbtn">더하기</button></div>
    </div>

    <div class="card" id="p-rest" hidden>
      <div class="lab">운동이 끝난 뒤 지난 시간</div>
      <div class="row"><input type="range" id="hour" min="0" max="168" value="6" aria-label="지난 시간"><b id="hourv" style="width:72px;text-align:right"></b></div>
      <div class="row"><button class="btn p" id="again">지금 가슴 운동 한 번 더</button><button class="btn" id="reset">처음으로</button></div>
      <div class="s" id="evlog"></div>
      <div class="list" id="rlist"></div>
      <div class="lab">부위별 휴식 시간</div>
      <div class="rest" id="restset"></div>
      <div class="s">가장 빨갛던 단계에서 휴식이 끝날 때까지 곧게 내려갑니다(빨강 → 주황 → 노랑 → 기본). 덜 쓴 근육은 휴식도 그만큼 짧습니다. 회복 전에 또 하면 남은 시간 위에 휴식이 더해집니다.</div>
    </div>

    <div class="card" id="p-tree" hidden>
      <div class="lab">온몸 근육 나무 · <span id="treecount"></span></div>
      <div class="s">● 그림에 직접 있음 · ◐ 부모나 다른 조각이 대신 칠함 · ○ 아직 칠할 곳 없음 · <i>속</i> = 겉에서 안 보이는 근육(기록만)</div>
      <div class="tree" id="tree"></div>
    </div>

    <div class="card" id="p-check" hidden>
      <div class="lab">GPT · 제미나이 등에서 만든 SVG 붙여넣기</div>
      <textarea id="svgin" placeholder="<svg ...> 전체를 붙여넣고 검사를 누르세요"></textarea>
      <div class="row"><button class="btn p" id="check">검사하고 칠해 보기</button><button class="btn" id="sample">지금 그림 넣어 보기</button><button class="btn" id="broken">틀린 예 넣어 보기</button></div>
      <div class="rep" id="report"></div>
    </div>
  </div>
</div>

<script>
const 그림원본=__SVG__;
const 목록=__CATALOG__;
const 색표묶음=__SCALES__;
const 종목표=__EXERCISES__;
const $=id=>document.getElementById(id);
const N=목록.stepCount;

/* ── 근육 나무 ── */
const 근육={},자식={};
목록.regions.forEach(r=>{근육[r.id]={id:r.id,name:r.name,kind:"region",parent:null};자식[r.id]=[]});
목록.muscles.forEach(m=>{근육[m.id]=m;자식[m.id]=자식[m.id]||[]});
목록.muscles.forEach(m=>{(자식[m.parent]=자식[m.parent]||[]).push(m.id)});
const 부위={};목록.regions.forEach(r=>부위[r.id]={...r});
const 조상=id=>{const a=[];let p=근육[id]?.parent;while(p){a.push(p);p=근육[p].parent}return a};
const 부위of=id=>{let x=id;while(근육[x]?.parent)x=근육[x].parent;return x};
const 잎들=id=>자식[id]?.length?자식[id].flatMap(잎들):[id];
const 모든잎=Object.keys(근육).filter(id=>근육[id].kind!=="region"&&!자식[id]?.length);

/* 값 내리기 — 어느 층에 걸린 단계든 그 아래 잎 모두에. 겹치면 큰 값 */
function 잎값(걸린){const r={};for(const t in 걸린)for(const l of 잎들(t))r[l]=Math.max(r[l]||0,걸린[t]);return r}
/* 값 올리기 — 어떤 층의 값 = 그 아래 잎 중 가장 큰 값 */
const 값=(잎,id)=>Math.max(0,...잎들(id).map(l=>잎[l]||0));

/* ── 색 ── */
let 색표=색표묶음.scales[0];
const hex=h=>[1,3,5].map(i=>parseInt(h.slice(i,i+2),16)/255);
const lin=c=>c<=.04045?c/12.92:((c+.055)/1.055)**2.4, gam=c=>c<=.0031308?c*12.92:1.055*c**(1/2.4)-.055;
function 단계색(k){
  if(k<=0)return null;
  const t=Math.min(1,k/N),st=색표.stops;
  if(t<=st[0].at)return st[0].color;
  for(let i=1;i<st.length;i++)if(t<=st[i].at){const a=st[i-1],b=st[i],u=(t-a.at)/(b.at-a.at);
    const A=hex(a.color).map(lin),B=hex(b.color).map(lin);
    return "#"+A.map((x,j)=>Math.round(gam(x+(B[j]-x)*u)*255).toString(16).padStart(2,"0")).join("")}
  return st.at(-1).color;
}
/* 그림 조각 하나 = data-muscle + data-covers 의 근육들. 그중 가장 큰 값으로 칠한다 */
const 조각근육=p=>[p.dataset.muscle,...(p.dataset.covers||"").split(/\s+/).filter(Boolean)];
function 칠하기(root,잎){root.querySelectorAll("[data-muscle]").forEach(p=>{const v=Math.max(...조각근육(p).map(id=>근육[id]?값(잎,id):0));p.style.fill=단계색(v)||""})}

/* ── 그림 ── */
$("fig").innerHTML=그림원본;
const F=()=>$("fig").querySelector("svg");
let 지금잎={};
$("fig").addEventListener("click",e=>{const fig=F();const p=e.target.closest("[data-muscle]");if(!p)return;fig.querySelectorAll(".sel").forEach(x=>x.classList.remove("sel"));fig.querySelectorAll(`[data-muscle="${p.dataset.muscle}"]`).forEach(x=>x.classList.add("sel"));
  const ids=조각근육(p).filter(id=>근육[id]);$("pick").textContent=ids.map(id=>`${근육[id].name} ${값(지금잎,id)}`).join(" · ")||p.dataset.muscle});

$("scales").innerHTML=색표묶음.scales.map((s,i)=>`<button class="chip${i?"":" on"}" data-s="${s.id}">${s.name}</button>`).join("");
$("scales").onclick=e=>{const b=e.target.closest("[data-s]");if(!b)return;색표=색표묶음.scales.find(s=>s.id===b.dataset.s);$("scales").querySelectorAll(".chip").forEach(c=>c.classList.toggle("on",c===b));다시()};
function 범례(){$("legend").innerHTML=[...Array(N+1).keys()].map(k=>`<i style="background:${단계색(k)||"var(--근육)"}" title="${k}단계"></i>`).join("")}

/* ── 운동 중 — 역할 비중 × 볼륨 ÷ 이전 최대 볼륨 (임시 기준 · 업데이트 예정) ── */
const 역할={};종목표.roles.forEach(r=>역할[r.id]=r);
const 루틴=[["인클라인 벤치프레스",5,60,10],["덤벨 플라이",5,14,12],["덤벨 컬",4,12,12],["해머 컬",4,14,10]].map(([n,s,kg,rep])=>({n,s,kg,rep,기본:{...종목표.exercises.find(x=>x.name===n).muscles}}));
const 커스텀={};  /* 종목 이름 → {근육 id: 역할} — 사용자가 바꾼 것만 */
const 근육설정=x=>커스텀[x.n]||x.기본;
const 총=루틴.reduce((a,x)=>a+x.s,0);
/* 끝낸 세트 수 목록 → 잎마다 유효 볼륨 */
function 볼륨잎(끝낸들){const v={};루틴.forEach((x,i)=>{const d=끝낸들[i]||0;if(!d)return;const 설정=근육설정(x);for(const k in 설정){const w=역할[설정[k]].weight*d*x.kg*x.rep;for(const l of 잎들(k))v[l]=(v[l]||0)+w}});return v}
const 순서대로=n=>루틴.map(x=>{const d=Math.max(0,Math.min(x.s,n));n-=x.s;return d});
/* 예시 기록 — 이전 최대 볼륨(근육마다 지금까지 가장 많이 쌓인 유효 볼륨, kg).
   지난 가슴날 = 오늘 계획의 90%. 지난 팔 · 어깨날에 팔과 어깨는 훨씬 많이 했다 → 오늘 보조근 · 협응근으로 쌓인 것은 조금만 빨개진다 */
const 다른날={triceps:3000,delt_front:3200,delt_side:2400,biceps:1400,brachialis:1200,brachioradialis:900,forearm_flexors:800,forearm_extensors:700};
function 이전최대(){const 계획=볼륨잎(루틴.map(x=>x.s)),r={};for(const l in 계획)r[l]=계획[l]*0.9;for(const k in 다른날)for(const l of 잎들(k))r[l]=Math.max(r[l]||0,다른날[k]);return r}
function 단계잎(볼){const 최대=이전최대(),r={};for(const l in 볼)r[l]=Math.min(N,Math.round(N*볼[l]/(최대[l]||볼[l])));return r}
const 달성잎=끝낸=>단계잎(볼륨잎(순서대로(끝낸)));
/* 목록은 '근육' 층으로 묶어 보여준다 (갈래는 근육으로 올림) */
const 근육층=Object.keys(근육).filter(id=>근육[id].kind==="muscle");
function 목록그리기(el,잎,남은){
  const 줄=근육층.map(id=>({id,v:값(잎,id)})).filter(x=>x.v>0).sort((a,b)=>b.v-a.v);
  el.innerHTML=줄.map(({id,v})=>`<div><span>${근육[id].name}</span><span class="s">${남은?시간글(Math.max(...잎들(id).map(l=>남은[l]||0))):부위[부위of(id)]?.name||""}</span><b style="text-align:right">${v}</b><span class="sw" style="background:${단계색(v)||"var(--근육)"}"></span></div>`).join("")||'<div><span class="ok">칠해진 근육 없음</span></div>';
}

/* ── 피로 회복 — 잎(가장 잘게 나눈 근육)마다 따로 센다 ── */
let 사건=[];
function 처음사건(){사건=[{t:0,s:달성잎(총),이름:"가슴, 팔 루틴"}]}
function 레벨(s,t){return t>=s.end?0:s.lv0*(s.end-t)/(s.end-s.start)}
function 피로(t){
  const 상태={};
  for(const ev of 사건.filter(e=>e.t<=t).sort((a,b)=>a.t-b.t))for(const k in ev.s){const s=ev.s[k];if(!s)continue;
    const 휴식=부위[부위of(k)]?.restHours??24;const 전=상태[k];const 지금=전?레벨(전,ev.t):0;const 남음=전?Math.max(0,전.end-ev.t):0;
    상태[k]={lv0:Math.min(N,지금+s),start:ev.t,end:ev.t+남음+휴식*s/N}}
  const r={},남은={};for(const k in 상태){r[k]=Math.ceil(레벨(상태[k],t)-1e-9);남은[k]=Math.max(0,상태[k].end-t)}
  return{r,남은};
}
const 시간글=h=>h<=0?"회복":h<24?`${Math.ceil(h)}시간`:`${Math.floor(h/24)}일 ${Math.ceil(h%24)}시간`;
const 휴식선택=[6,12,24,36,48,72,96,120,144,168];
function 휴식설정(){$("restset").innerHTML=목록.regions.map(g=>`<span>${g.name}</span><select data-g="${g.id}" aria-label="${g.name} 휴식">${휴식선택.map(h=>`<option value="${h}"${부위[g.id].restHours===h?" selected":""}>${h<24||h%24?h+"시간":h/24+"일"}</option>`).join("")}</select>`).join("")}
$("restset").onchange=e=>{const s=e.target.closest("[data-g]");if(s){부위[s.dataset.g].restHours=+s.value;다시()}};

/* ── 움직임 예시 ── */
let 움직임=null;
$("anim").onclick=()=>{
  if(움직임){cancelAnimationFrame(움직임);움직임=null;F().querySelectorAll('[id^="seg-forearm"]').forEach(g=>g.removeAttribute("transform"));$("anim").textContent="움직임 예시 (팔 굽히기)";return}
  $("anim").textContent="멈추기";const t0=performance.now();
  const step=now=>{const a=(1-Math.cos((now-t0)/700))*28;
    F().querySelectorAll('[id^="seg-forearm"][data-pivot]').forEach(g=>{const[x,y]=g.dataset.pivot.split(",").map(Number);const 쪽=g.id.includes("-R-")?-1:1;g.setAttribute("transform",`rotate(${쪽*a} ${x} ${y})`)});
    움직임=requestAnimationFrame(step)};움직임=requestAnimationFrame(step)};

/* ── 그림이 덮는 곳 — 근육마다 '직접 / 대신 / 없음' ── */
function 덮는곳(svg){
  const 직접=new Set(),대신=new Set();
  svg.querySelectorAll("[data-muscle]").forEach(p=>{직접.add(p.dataset.muscle);(p.dataset.covers||"").split(/\s+/).filter(Boolean).forEach(c=>대신.add(c))});
  const 상태=id=>{if(직접.has(id))return"●";const 위=[id,...조상(id)];if(위.some(x=>직접.has(x)||대신.has(x)))return"◐";if(자식[id]?.length&&잎들(id).every(l=>상태(l)!=="○"))return"◐";return"○"};
  return{직접,대신,상태};
}
function 나무(){
  const d=덮는곳(F());let 줄=[],셈={"●":0,"◐":0,"○":0};
  const 그려=(id,깊이)=>{const m=근육[id];const st=m.kind==="region"?"":d.상태(id);if(st&&!자식[id]?.length)셈[st]++;
    줄.push(`<div class="${m.kind==="region"?"r":""}" style="padding-left:${깊이*14}px"><span class="st">${st}</span><span>${m.name}</span>${m.latin?`<span class="la">${m.latin}</span>`:""}${m.depth==="deep"?'<span class="deep">속</span>':""}</div>`);
    (자식[id]||[]).forEach(c=>그려(c,깊이+1))};
  목록.regions.forEach(r=>그려(r.id,0));
  $("tree").innerHTML=줄.join("");
  $("treecount").textContent=`근육 ${목록.muscles.length}개 · 가장 잘게 ${모든잎.length}개 중 직접 ${셈["●"]} · 대신 ${셈["◐"]} · 없음 ${셈["○"]}`;
}

/* ── 새 그림 검사 ── */
function 검사(텍스트){
  const doc=new DOMParser().parseFromString(텍스트,"image/svg+xml"),svg=doc.querySelector("svg");
  if(!svg||doc.querySelector("parsererror"))return{줄:["SVG 로 읽히지 않습니다. <svg …> 부터 </svg> 까지 전부 붙여넣었는지 확인하세요."]};
  const 줄=[];
  const 쓰인=new Set();svg.querySelectorAll("[data-muscle]").forEach(p=>조각근육(p).forEach(x=>쓰인.add(x)));
  const 모름=[...쓰인].filter(id=>!근육[id]);
  const d=덮는곳(svg);
  const 겉없음=모든잎.filter(id=>근육[id].depth!=="deep"&&d.상태(id)==="○");
  const 부위없음=목록.regions.filter(r=>잎들(r.id).every(l=>d.상태(l)==="○")).map(r=>r.name);
  const 쪽없음=[...svg.querySelectorAll("[data-muscle]")].filter(p=>!["L","R","C"].includes(p.getAttribute("data-side"))).length;
  const 뷰=["front","back"].filter(v=>!svg.querySelector(`#view-${v}`));
  const 마디=svg.querySelectorAll("[id^='seg-'][data-pivot]").length;
  줄.push(`<span class="ok">그림에 쓰인 근육 ${[...쓰인].filter(id=>근육[id]).length}개</span> (목록의 어느 층이든 됨)`);
  if(모름.length)줄.push(`<span class="bad">목록에 없는 id ${모름.length}개</span> — ${모름.join(", ")} (무시됨 · 새 근육이면 muscle-catalog.json 에 먼저 추가)`);
  줄.push(부위없음.length?`<span class="bad">칠할 곳이 하나도 없는 부위</span> — ${부위없음.join(", ")}`:`<span class="ok">모든 부위에 칠할 곳 있음</span>`);
  줄.push(겉없음.length?`<span class="bad">겉 근육 중 칠할 곳 없는 것 ${겉없음.length}개</span> — ${겉없음.map(id=>근육[id].name).join(", ")} (칠해지지 않음 · data-covers 로 맡길 수 있음)`:`<span class="ok">겉 근육 모두 칠할 곳 있음</span>`);
  줄.push(쪽없음?`<span class="bad">data-side 없는 근육 조각 ${쪽없음}개</span> — L · R · C 중 하나`:`<span class="ok">좌우 표시 모두 있음</span>`);
  줄.push(뷰.length?`<span class="bad">view-${뷰.join(", view-")} 없음</span>`:`<span class="ok">앞 · 뒤 보기 있음</span>`);
  줄.push(마디?`<span class="ok">움직임 마디 ${마디}개</span>`:`<span class="bad">움직임 마디(seg-*, data-pivot) 없음</span> — 가만히 있는 그림으로는 쓸 수 있음`);
  return{줄,svg:svg.outerHTML};
}
$("check").onclick=()=>{const r=검사($("svgin").value);$("report").innerHTML=r.줄.join("<br>");if(r.svg){$("fig").innerHTML=r.svg;다시()}};
$("sample").onclick=()=>{$("svgin").value=그림원본;$("check").click()};
$("broken").onclick=()=>{$("svgin").value=그림원본.replace(/data-muscle="biceps"/g,'data-muscle="bicep"').replace(/ data-covers="brachialis coracobrachialis"/g,"").replace(/(data-muscle="abs") data-side="R"/,"$1");$("check").click()};


/* ── 종목 근육 설정 (기본값 / 커스텀) ── */
$("exsel").innerHTML=루틴.map((x,i)=>`<option value="${i}">${x.n}</option>`).join("");
$("addm").innerHTML='<option value="">근육 더하기…</option>'+(function 옵션(id,깊이){return (자식[id]||[]).map(c=>`<option value="${c}">${"　".repeat(깊이)}${근육[c].name}</option>`+옵션(c,깊이+1)).join("")}).call(null,null,0)+목록.regions.map(r=>`<option disabled>── ${r.name}</option>`+(function f(id,d){return (자식[id]||[]).map(c=>`<option value="${c}">${"　".repeat(d)}${근육[c].name}</option>`+f(c,d+1)).join("")})(r.id,1)).join("");
function 역할판(){
  const x=루틴[+$("exsel").value],설정=근육설정(x);
  $("exroles").innerHTML=Object.keys(설정).map(k=>`<div><span>${근육[k].name}${커스텀[x.n]&&x.기본[k]!==설정[k]?' <span class="s">· 바꿈</span>':""}</span><span class="rchips">${종목표.roles.map(r=>`<button class="chip${설정[k]===r.id?" on":""}" data-m="${k}" data-r="${r.id}">${r.name}</button>`).join("")}<button class="chip" data-m="${k}" data-r="">빼기</button></span></div>`).join("");
  $("exreset").disabled=!커스텀[x.n];
}
$("exsel").onchange=역할판;
$("exroles").onclick=e=>{const b=e.target.closest("[data-m]");if(!b)return;const x=루틴[+$("exsel").value];const c=커스텀[x.n]=커스텀[x.n]||{...x.기본};if(b.dataset.r)c[b.dataset.m]=b.dataset.r;else delete c[b.dataset.m];역할판();다시()};
$("addbtn").onclick=()=>{const id=$("addm").value;if(!id)return;const x=루틴[+$("exsel").value];const c=커스텀[x.n]=커스텀[x.n]||{...x.기본};if(!c[id])c[id]="synergist";역할판();다시()};
$("exreset").onclick=()=>{delete 커스텀[루틴[+$("exsel").value].n];역할판();다시()};

/* ── 화면 ── */
let 탭="work";
function 다시(){
  범례();const 대상=F();
  if(탭==="rest"){
    const t=+$("hour").value;$("hourv").textContent=`${t}시간`;
    const {r,남은}=피로(t);지금잎=r;칠하기(대상,r);목록그리기($("rlist"),r,남은);
    $("evlog").textContent=사건.map(e=>`${e.t}시간 뒤 ${e.이름}`).join(" · ");
  }else{
    const n=+$("done").value;$("donev").textContent=`${n}/${총}`;
    const r=달성잎(n);지금잎=r;칠하기(대상,r);목록그리기($("wlist"),r);
    let m=n,지금="";for(const x of 루틴){if(m<x.s){지금=`지금 ${x.n} ${m+1}세트째`;break}m-=x.s}
    $("nowex").textContent=지금||"루틴 끝";
  }
  if(!$("p-tree").hidden)나무();
}
$("done").oninput=다시;$("hour").oninput=다시;
$("again").onclick=()=>{const t=+$("hour").value;사건.push({t,s:단계잎(볼륨잎([5,5,0,0])),이름:"가슴 운동"});다시()};
$("reset").onclick=()=>{처음사건();$("hour").value=6;다시()};
$("tabs").onclick=e=>{const b=e.target.closest("[data-t]");if(!b)return;const t=b.dataset.t;$("tabs").querySelectorAll("button").forEach(x=>x.classList.toggle("on",x===b));["work","rest","tree","check"].forEach(k=>$("p-"+k).hidden=k!==t);탭=t==="rest"?"rest":"work";다시()};
처음사건();휴식설정();역할판();다시();
</script>
