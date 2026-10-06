import asyncio,sys,json
from playwright.async_api import async_playwright
EXT=open(''+__import__('os').path.dirname(__file__)+'/디자인뽑기_ext.js').read()
H="""window.클=(t,n=0)=>{const xs=[...document.querySelectorAll('#폰 *')].filter(x=>x.offsetParent!==null&&(x.innerText||'').trim()===t); xs.sort((a,b)=>a.querySelectorAll('*').length-b.querySelectorAll('*').length); const x=xs[n]; if(!x) return 'NO '+t; x.click(); return 'ok '+t;}; 1"""
async def main():
  screens=json.load(open(sys.argv[1]))
  async with async_playwright() as p:
    b=await p.chromium.launch()
    for name,steps in screens:
      pg=await b.new_page(viewport={'width':422,'height':868})
      await pg.goto('file:///home/claude/gymwork-app/문서/자료/시안/7일체험.html'); await pg.wait_for_timeout(700)
      await pg.evaluate("document.querySelector('.체험').style.display='none'; 1"); await pg.evaluate(H)
      for js in steps:
        try: r=await pg.evaluate(js)
        except Exception as e: r='ERR '+str(e)[:200]
        if r not in (1,None,True) : print(name, r)
        await pg.wait_for_timeout(600)
      await pg.wait_for_timeout(900)
      await pg.locator('#폰').screenshot(path=f'cap/{name}.png')
      html=await pg.evaluate(EXT); open(f'cap/{name}.html','w').write(html)
      print(name, len(html))
      await pg.close()
    await b.close()
asyncio.run(main())
