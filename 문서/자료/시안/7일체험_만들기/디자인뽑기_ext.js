(() => {
const nost=document.createElement('style'); nost.textContent='*,*::before,*::after{animation:none!important;transition:none!important}'; document.head.appendChild(nost);
const phone = document.getElementById('폰');
const PR = phone.getBoundingClientRect();
const ifr = document.createElement('iframe'); ifr.style.display='none'; document.body.appendChild(ifr);
const D = ifr.contentDocument; D.open(); D.write('<!doctype html><html><body></body></html>'); D.close();
const defs = {};
function def(tag, ns){ const k=(ns?'s:':'')+tag; if(defs[k]) return defs[k];
  const e = ns? D.createElementNS('http://www.w3.org/2000/svg', tag) : D.createElement(tag);
  if(ns){ const s=D.createElementNS('http://www.w3.org/2000/svg','svg'); s.appendChild(e); D.body.appendChild(s);} else D.body.appendChild(e);
  const cs=getComputedStyle(e), o={}; for(const p of PROPS) o[p]=cs.getPropertyValue(p); defs[k]=o; return o; }
const INH = new Set(['color','font-family','font-size','font-weight','font-style','line-height','letter-spacing','text-align','white-space','word-break','overflow-wrap','visibility','cursor','font-variant-numeric','font-feature-settings','text-transform','fill','stroke','stroke-width','stroke-linecap','stroke-linejoin','stroke-dasharray','fill-rule','fill-opacity','stroke-opacity','paint-order','text-anchor','dominant-baseline']);
const PROPS = ['display','position','top','left','right','bottom','z-index','box-sizing','width','height','min-width','min-height','max-width','max-height',
 'margin-top','margin-right','margin-bottom','margin-left','padding-top','padding-right','padding-bottom','padding-left',
 'flex-direction','flex-wrap','flex-grow','flex-shrink','flex-basis','justify-content','align-items','align-self','align-content','order','gap','row-gap','column-gap',
 'justify-self','justify-items','grid-template-columns','grid-template-rows','grid-column','grid-row','grid-auto-flow',
 'overflow-x','overflow-y','background-color','background-image','background-size','background-position','background-repeat',
 'border-top-width','border-right-width','border-bottom-width','border-left-width','border-top-style','border-right-style','border-bottom-style','border-left-style',
 'border-top-color','border-right-color','border-bottom-color','border-left-color',
 'border-top-left-radius','border-top-right-radius','border-bottom-left-radius','border-bottom-right-radius',
 'box-shadow','opacity','transform','transform-origin','outline-style','outline-width','outline-color','outline-offset','object-fit','text-overflow','text-decoration-line','text-decoration-color','text-underline-offset','vertical-align','clip-path','mask-image','-webkit-mask-image','filter','backdrop-filter','mix-blend-mode','pointer-events','aspect-ratio','table-layout','border-collapse',
 ...INH];
const SIZEP=['width','height','min-width','min-height','max-width','max-height','top','left','right','bottom','margin-top','margin-right','margin-bottom','margin-left','flex-basis'];
const SVGP=new Set(['display','width','height','opacity','transform','transform-origin','fill','stroke','stroke-width','stroke-linecap','stroke-linejoin','stroke-dasharray','fill-rule','fill-opacity','stroke-opacity','color','visibility','overflow-x','overflow-y','vertical-align','flex-shrink','margin-top','margin-right','margin-bottom','margin-left','font-size','font-weight','font-family','text-anchor','dominant-baseline','paint-order','mix-blend-mode','filter','clip-path']);
const FORM=new Set(['button','input','select','textarea']);
const VOID = new Set(['br','hr','img','input','meta','link','area','base','col','embed','source','track','wbr']);
const esc = s => s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
const escA = s => s.replace(/&/g,'&amp;').replace(/"/g,'&quot;').replace(/</g,'&lt;');
function styleOf(el, ns, parentCS, isRoot, pseudo){
  const cs = getComputedStyle(el, pseudo||null); const d = def(pseudo?'span':el.localName, ns && !pseudo); const out=[];
  let spec=null;
  if(!pseudo && !ns && !isRoot){ const old=el.style.getPropertyValue('display'), oldp=el.style.getPropertyPriority('display'); el.style.setProperty('display','none','important'); const c2=getComputedStyle(el); spec={}; for(const p of SIZEP) spec[p]=c2.getPropertyValue(p); el.style.setProperty('display',old,oldp); if(!old) el.style.removeProperty('display'); }
  for(const p of PROPS){ let v=(spec&&SIZEP.includes(p))?spec[p]:cs.getPropertyValue(p); if(v==='') continue;
    if(p==='box-sizing'){ if(!ns && v==='content-box' && !isRoot && !pseudo) out.push('box-sizing: content-box'); continue; }
    if(INH.has(p)){ if(!isRoot && parentCS && parentCS.getPropertyValue(p)===v && !(FORM.has(el.localName) && v!==d[p])) continue; if(isRoot && v===d[p] && p!=='font-family' && p!=='color') continue; }
    else if(!(spec&&SIZEP.includes(p)) && v===d[p]) continue;
    if(/^(width|height)$/.test(p) && !ns && cs.display==='inline') continue;
    if(spec && SIZEP.includes(p)){ if(p.startsWith('margin')){ if(v===d[p]) continue; } else if(v==='auto'||v==='none'||(p.startsWith('min')&&v==='0px')) continue; }
    if(/^border-(top|right|bottom|left)-(color|style)$/.test(p)){ const side=p.split('-')[1]; if(cs.getPropertyValue('border-'+side+'-width')==='0px') continue; }
    if(ns && !SVGP.has(p)) continue;
    if(p==='box-sizing' && v==='border-box' && parentCS) continue;
    if(p==='font-family') v=v.replace(/"/g,"'");
    out.push(p+': '+v); }
  return [out, cs];
}
function ser(el, parentCS, isRoot){
  if(el.nodeType===3){ return esc(el.textContent); }
  if(el.nodeType!==1) return '';
  const tag=el.localName; if(['script','style','template','link','meta'].includes(tag)) return '';
  const ns = el.namespaceURI==='http://www.w3.org/2000/svg';
  const cs0=getComputedStyle(el); if(!ns && cs0.display==='none') return '';
  let [st, cs] = styleOf(el, ns, parentCS, isRoot);
  if(cs.display==='none') return '';
  if(isRoot){ st=st.filter(s=>!/^(position|top|left|right|bottom|margin|transform)/.test(s)); st.push('position: relative','width: '+PR.width+'px','height: '+PR.height+'px','overflow: hidden','box-sizing: border-box'); }
  if(cs.position==='fixed'){ const r=el.getBoundingClientRect(); st=st.filter(s=>!/^(position|top|left|right|bottom):/.test(s)); st.push('position: absolute','left: '+(r.left-PR.left)+'px','top: '+(r.top-PR.top)+'px','width: '+r.width+'px','height: '+r.height+'px'); }
  let a='';
  for(const at of el.attributes){ const n=at.name; if(n==='style'||n==='class'||n==='id'||n.startsWith('on')||n.startsWith('data-')||n==='contenteditable'||n==='tabindex'||n==='draggable') continue;
    if(n==='src' && at.value.startsWith('data:')) continue;
    a+=' '+n+'="'+escA(at.value)+'"'; }
  if(tag==='input' || tag==='textarea'){ if(el.value) a+=' value="'+escA(el.value)+'"'; }
  if(st.length) a+=' style="'+escA(st.join('; '))+'"';
  if(tag==='img' && el.getAttribute('src')?.startsWith('data:')) return '<div aria-label="사진" style="'+escA(st.join('; ')+'; background-color: #D9DEE5')+'"></div>';
  if(VOID.has(tag)) return '<'+tag+a+'>';
  let inner='';
  if(!ns){ for(const ps of ['::before','::after']){ const pc=getComputedStyle(el,ps); const c=pc.content; if(c && c!=='none' && c!=='normal'){
      const [pst]=styleOf(el,false,cs,false,ps); const txt=c.startsWith('"')?c.slice(1,-1):'';
      const piece='<span style="'+escA(pst.join('; '))+'">'+esc(txt)+'</span>'; if(ps==='::before') inner=piece+inner; else inner+='{AFTER}'+piece; } } }
  let kids=''; for(const k of el.childNodes) kids+=ser(k, cs, false);
  if(inner.includes('{AFTER}')){ const [b,af]=inner.split('{AFTER}'); inner=b+kids+af; } else inner+=kids;
  if(tag==='textarea') inner=esc(el.value||'');
  return '<'+tag+a+'>'+inner+'</'+tag+'>';
}
const out = ser(phone, null, true); ifr.remove(); nost.remove(); return out;
})()
