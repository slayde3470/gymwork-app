"""✎ 표시를 그때 상태로 다시 띄워 선을 겹친다 (CLAUDE.md 2-1 의 3번)"""
import asyncio, json, glob, os, sys
from playwright.async_api import async_playwright
from PIL import Image, ImageDraw
SP=os.path.dirname(os.path.abspath(__file__)); M=SP+'/'+(sys.argv[2] if len(sys.argv)>2 else 'marks2')
F='file://'+SP+'/'+(sys.argv[1] if len(sys.argv)>1 else '7day-v3.html')
async def main():
  async with async_playwright() as p:
    b=await p.chromium.launch(); pg=await b.new_page(viewport={'width':900,'height':1100})
    for f in sorted(glob.glob(M+'/db/marks/*.json')):
      d=json.load(open(f)); d=d.get('data',d); mid=os.path.basename(f)[:4]
      st=M+'/'+(d.get('상태') or '')+'.json'
      if not os.path.exists(st): print(mid,'상태 없음'); continue
      상태=json.load(open(st))
      await pg.goto(F); await pg.wait_for_timeout(400)
      await pg.evaluate("""([s,w,h])=>{ S=s.S; const 시=s.U.시트; Object.assign(U, s.U, {시트:null});
        if(시 && (시.종류==='고침' || 시.대상==='고침') && S.플랜들.length){ 행동('플랜고치기',{v:S.플랜들[0].id}); }
        U.시트=시; const 폰=document.getElementById('폰');
        폰.style.flex='none'; 폰.style.width=w+'px'; 폰.style.height=h+'px'; 폰.style.maxHeight=h+'px'; 그리기(); }""", [상태, d['폭'], d['높이']])
      await pg.wait_for_timeout(900)
      el=await pg.query_selector('#폰'); out=f"{M}/r_{mid}.png"; await el.screenshot(path=out)
      im=Image.open(out).convert('RGB'); W,H=im.size; dr=ImageDraw.Draw(im)
      for l in d['선']:
        pts=[(x*W,y*H) for x,y in l]
        if len(pts)>1: dr.line(pts, fill=(220,30,30), width=4, joint='curve')
      im.save(out); print(mid, d['화면'], d.get('시트'), '→', out, im.size, '|', d['메모'][:40])
    await b.close()
asyncio.run(main())
