// 하젠하이데 근육 그림 만들기 — 규칙(07_근육지도규칙.md)에 맞는 SVG 를 만든다.
// 모양은 몸의 왼쪽 절반만 적고, 오른쪽은 x → 200-x 로 뒤집어 실제 좌표로 따로 적는다 (색을 좌우 따로 칠할 수 있게).
const fs=require("fs");
// [근육 id | null(바탕), 마디, d]
const 앞=[
 [null,"head","M100 8 C86 8 82 20 82 32 C82 46 90 56 100 56 Z"],
 ["scm","head","M92 52 L100 54 L100 70 L88 68 Z"],
 ["traps","torso","M90 58 C80 64 70 68 62 72 L88 70 Z"],
 ["delt_front","torso","M62 72 C52 74 46 84 46 96 C50 100 56 102 60 100 C62 90 68 80 74 76 Z"],
 ["delt_side","torso","M46 96 C42 106 42 116 46 122 L54 112 C56 106 58 104 60 100 C56 102 50 100 46 96 Z"],
 ["chest_upper","torso","M74 76 C84 72 94 72 100 74 L100 92 C90 92 78 94 66 98 C64 90 68 82 74 76 Z"],
 ["chest_lower","torso","M66 98 C78 94 90 92 100 92 L100 114 C92 122 80 124 70 118 C64 112 64 104 66 98 Z"],
 ["biceps","upperarm","M54 112 C48 124 46 138 48 152 C52 158 58 156 60 150 C62 136 62 122 58 110 Z"],
 ["triceps","upperarm","M46 122 C42 134 42 146 44 154 L48 152 C46 138 48 124 54 112 Z"],
 ["forearm","forearm","M44 158 C38 176 34 192 34 208 L42 210 C46 194 52 176 58 160 C54 158 48 156 44 158 Z"],
 ["forearm","forearm","M58 160 C56 178 52 194 46 210 L42 210 C46 194 50 178 52 162 Z"],
 [null,"forearm","M34 210 C30 218 32 230 38 234 C44 232 46 222 44 212 Z"],
 ["serratus","torso","M68 120 L74 124 L72 130 L66 128 Z M67 132 L73 136 L71 142 L66 140 Z M68 144 L73 148 L72 154 L68 152 Z"],
 ["abs","torso","M88 124 L99 122 L99 140 L88 141 Z M88 145 L99 144 L99 162 L88 163 Z M88 167 L99 166 L99 184 L89 185 Z M89 189 L99 188 L99 214 C94 212 90 204 89 196 Z"],
 ["obliques","torso","M74 124 C70 146 72 172 80 196 L88 200 L87 126 C83 122 78 122 74 124 Z"],
 [null,"torso","M80 198 C84 208 92 214 100 216 L100 222 C90 222 80 214 76 204 Z"],
 ["quads","thigh","M76 206 C66 234 64 268 72 304 L82 306 C78 272 80 238 86 216 Z"],
 ["quads","thigh","M86 216 C80 244 80 276 84 304 L94 304 C96 276 96 244 94 220 Z"],
 ["quads","thigh","M94 288 C90 298 90 308 96 316 C100 312 100 300 99 290 Z"],
 ["adductors","thigh","M94 220 L100 222 L100 284 C97 270 96 246 94 220 Z"],
 [null,"thigh","M76 308 L96 308 C98 318 94 326 86 328 C80 326 76 318 76 308 Z"],
 ["shin","shin","M78 332 C74 364 76 392 80 420 L86 420 C86 392 88 362 86 332 Z"],
 ["calves","shin","M86 332 C94 350 96 378 90 408 L86 410 C88 382 88 356 86 332 Z"],
 [null,"shin","M78 422 L90 422 C92 432 90 440 80 442 C74 440 74 430 78 422 Z"],
];
const 뒤=[
 [null,"head","M100 8 C86 8 82 20 82 32 C82 46 90 56 100 56 Z"],
 ["traps","torso","M100 50 C94 56 84 62 64 72 C76 84 88 104 100 140 Z"],
 ["delt_rear","torso","M64 72 C52 74 46 86 46 100 C52 104 58 102 62 96 C66 88 70 80 76 78 Z"],
 ["delt_side","torso","M46 100 C42 108 42 118 46 124 L54 114 C56 108 60 102 62 96 C58 102 52 104 46 100 Z"],
 ["rhomboids","torso","M76 80 C82 88 90 104 96 124 L88 126 C84 112 78 98 72 88 Z"],
 ["lats","torso","M70 94 C64 116 66 146 78 176 C86 172 94 164 98 150 L96 128 L88 128 C84 114 78 102 72 90 Z"],
 ["triceps","upperarm","M54 114 C46 126 44 140 46 156 C52 160 58 158 60 152 C62 138 60 124 56 112 Z"],
 ["forearm","forearm","M46 160 C38 178 34 194 34 210 L42 212 C48 196 54 178 60 160 C56 160 50 158 46 160 Z"],
 [null,"forearm","M34 212 C30 220 32 232 38 236 C44 234 46 224 44 214 Z"],
 ["lower_back","torso","M90 150 L100 150 L100 204 L92 202 C90 186 90 168 90 150 Z"],
 ["obliques","torso","M78 178 C78 190 80 198 82 204 L92 204 L90 164 C86 170 82 176 78 178 Z"],
 ["glutes","torso","M82 206 C70 214 68 236 76 252 C86 258 96 256 100 250 L100 208 Z"],
 ["hamstrings","thigh","M74 256 C68 280 70 300 76 318 L86 318 C84 298 84 276 86 258 Z"],
 ["hamstrings","thigh","M86 258 C86 280 88 300 94 318 L99 316 C100 296 100 276 99 256 Z"],
 [null,"thigh","M76 320 L98 320 C98 326 94 332 86 332 C80 332 76 326 76 320 Z"],
 ["calves","shin","M78 334 C70 352 72 372 80 388 C84 380 86 360 86 336 Z"],
 ["calves","shin","M86 336 C88 356 90 374 96 386 C100 370 98 350 92 336 Z"],
 ["calves","shin","M80 390 C82 402 84 414 84 420 L92 420 C92 408 94 396 96 388 C90 394 86 394 80 390 Z"],
 [null,"shin","M80 422 L92 422 C94 430 92 438 84 440 C78 438 76 430 80 422 Z"],
];
// 대신 칠하기 — 이 그림에 따로 없는 근육을 어느 조각이 대신 맡는지 (07 규칙 3-3)
const 대신={"front:chest_lower":"chest_mid","front:biceps":"brachialis coracobrachialis","front:triceps":"brachialis",
 "back:lats":"teres_major","back:delt_rear":"infraspinatus teres_minor","back:triceps":"anconeus",
 "front:quads":"tfl sartorius","back:calves":"tibialis_posterior"};
const 결={front:"M76 80 L96 86 M70 90 L98 94 M70 104 L98 102 M72 114 L96 110 M52 80 L58 98 M56 118 L52 146 M40 170 L38 200 M72 230 L74 290 M88 230 L88 296 M82 340 L82 408",
 back:"M72 72 L96 100 M80 70 L98 118 M74 110 L90 160 M70 128 L84 170 M52 124 L50 150 M84 216 L88 248 M76 270 L80 312 M92 270 L94 312 M80 346 L82 380"};
// 마디 — 움직임용. 관절 위치(pivot)는 왼쪽 기준, 오른쪽은 뒤집는다. 부모 → 자식 순서로 겹쳐 넣는다
const 관절={upperarm:[56,80],forearm:[50,156],thigh:[88,210],shin:[86,322]};
const 뒤집기=d=>d.replace(/([MLCQ])([^MLCQZ]*)/g,(m,c,args)=>c+args.trim().split(/[\s,]+/).map((v,i)=>i%2===0?String(200-+v):v).join(" ")+" ").replace(/\s+Z/g," Z").trim();
function 면(부품,이름,dx){
  const 쪽=["L","R"];
  const 조각=(side,seg)=>부품.filter(p=>p[1]===seg).map(([id,,d],i)=>{
    const dd=side==="L"?d:뒤집기(d);
    const cv=대신[이름+":"+id];
    return id?`<path class="muscle" id="${id}-${side}-${이름}${부품.filter(q=>q[0]===id).length>1?"-"+i:""}" data-muscle="${id}"${cv?` data-covers="${cv}"`:""} data-side="${side}" d="${dd}" fill="#D6DDD9"/>`
             :`<path class="base" data-side="${side}" d="${dd}" fill="#B9C3BE"/>`}).join("\n      ");
  const pv=(seg,side)=>{const[x,y]=관절[seg];return side==="L"?`${x},${y}`:`${200-x},${y}`};
  const 팔=side=>`<g id="seg-upperarm-${side}-${이름}" data-pivot="${pv("upperarm",side)}">\n      ${조각(side,"upperarm")}\n      <g id="seg-forearm-${side}-${이름}" data-pivot="${pv("forearm",side)}">\n      ${조각(side,"forearm")}\n      </g>\n    </g>`;
  const 다리=side=>`<g id="seg-thigh-${side}-${이름}" data-pivot="${pv("thigh",side)}">\n      ${조각(side,"thigh")}\n      <g id="seg-shin-${side}-${이름}" data-pivot="${pv("shin",side)}">\n      ${조각(side,"shin")}\n      </g>\n    </g>`;
  return `<g id="view-${이름}" transform="translate(${dx} 0)">
  <g id="seg-torso-${이름}" data-pivot="100,200">
    ${쪽.map(s=>조각(s,"head")+"\n    "+조각(s,"torso")).join("\n    ")}
    ${쪽.map(팔).join("\n    ")}
    ${쪽.map(다리).join("\n    ")}
  </g>
  <g class="detail" pointer-events="none"><path d="${결[이름]}"/><path d="${뒤집기(결[이름])}"/></g>
</g>`;
}
const svg=`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 440 460" data-hasenheide-body="1" data-rules-version="2">
<!-- 하젠하이데 근육 그림 v1 · 규칙: claude/07_근육지도규칙.md · 근육 id 는 muscle-registry.json 과 같아야 한다 -->
${면(앞,"front",0)}
${면(뒤,"back",240)}
</svg>`;
fs.writeFileSync("hasenheide-body.svg",svg);
const ids=[...new Set([...svg.matchAll(/data-muscle="([a-z_]+)"/g)].map(m=>m[1]))];
console.log(svg.length,"bytes",ids.length,"muscles:",ids.join(","));
