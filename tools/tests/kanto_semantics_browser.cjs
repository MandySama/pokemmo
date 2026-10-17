const {chromium}=require('../../target/viewer-test/node_modules/playwright-core');
const assert=require('node:assert/strict');
const path=require('node:path');
const {pathToFileURL}=require('node:url');
(async()=>{
    const browser=await chromium.launch({headless:true,executablePath:process.env.GBA_TEST_BROWSER||'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe'});
    try{
        const page=await browser.newPage({viewport:{width:1440,height:1000}}),errors=[];
        page.on('pageerror',e=>errors.push(e.message));
        await page.goto(pathToFileURL(path.resolve(__dirname,'../../exports/fire-red-map-viewer/explorer.html')).href);
        let inspected=0;
        for(let start=0;start<425;start+=25){
            inspected+=await page.evaluate(start=>{
                let count=0;
                for(const m of D.maps.slice(start,start+25)){
                    showMap(m);
                    for(const kind of ['npcs','coordinate-events','background','map-scripts','warps']){
                        const e=m[kind][0];if(!e)continue;
                        select(kind,e);
                        if(document.getElementById('detail').innerText.includes('undefined'))throw new Error(key(m)+' '+kind+' undefined UI');
                        if(kind==='npcs'&&parseInt(e.script_offset,16)&&!document.querySelector('.dialogue-context'))throw new Error('No dialogue contexts '+key(m));
                        count++;
                    }
                }return count;
            },start);
        }
        await page.evaluate(()=>{showMap(maps.get('3/0'));select('coordinate-events',current['coordinate-events'][2]);});
        assert((await page.locator('.semantic-guide').innerText()).includes('必须满足的入口条件'));
        await page.locator('.dependency-list > summary').first().click();
        await page.locator('.dependency-list .event-link').first().click();
        assert.equal(await page.evaluate(()=>selected.kind),'map-scripts');
        assert(await page.locator('.semantic-guide').count()>0);
        await page.evaluate(()=>{showMap(maps.get('3/1'));select('npcs',current.npcs[0]);});
        await page.locator('.dialogue-context > summary').first().click();
        await page.screenshot({path:'target/kanto-semantics-general.png',fullPage:true});
        assert.equal(await page.locator('.event-sequence').count(),0);
        assert.deepEqual(errors,[]);
        console.log(`PASS: 425 maps, ${inspected} representative event panels, state-producer navigation, no execution sequence or browser errors`);
    }finally{await browser.close();}
})().catch(e=>{console.error(e);process.exitCode=1;});
