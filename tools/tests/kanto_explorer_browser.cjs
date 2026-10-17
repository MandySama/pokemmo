const {chromium} = require('../../target/viewer-test/node_modules/playwright-core');
const assert = require('node:assert/strict');
const path = require('node:path');
const {pathToFileURL} = require('node:url');

(async () => {
    const browser = await chromium.launch({headless: true,
        executablePath: process.env.GBA_TEST_BROWSER || 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe'});
    try {
        const page = await browser.newPage({viewport: {width: 1440, height: 1000}});
        const errors = [];
        page.on('pageerror', error => errors.push(error.message));
        await page.goto(pathToFileURL(path.resolve(__dirname, '../../exports/fire-red-map-viewer/explorer.html')).href);
        assert.equal(await page.locator('.city-dot').count(), 11);
        assert(await page.locator('#overview').isVisible());
        assert(await page.locator('#regionImage').evaluate(image => image.complete && image.naturalWidth === 192));
        await page.screenshot({path: 'target/kanto-explorer-overview.png', fullPage: true});
        await page.getByRole('button', {name: '真新镇', exact: true}).click();
        await page.locator('#mapImage').evaluate(image => image.decode());
        assert(await page.locator('#mapView').isVisible());
        assert.equal(await page.locator('#mapSelect').inputValue(), '3/0');
        assert.equal(await page.locator('.x-axis span').count(),24);
        assert.equal(await page.locator('.y-axis span').count(),20);
        assert.equal(await page.locator('.x-axis span').last().innerText(),'23');
        assert.equal(await page.locator('.y-axis span').last().innerText(),'19');
        assert(await page.evaluate(()=>{
            const stage=document.getElementById('stage').getBoundingClientRect();
            return Math.abs(document.querySelector('.x-axis').getBoundingClientRect().left-stage.left)<1
                &&Math.abs(document.querySelector('.y-axis').getBoundingClientRect().top-stage.top)<1;
        }));
        await page.locator('#stage').click({position:{x:16,y:16}});
        assert.deepEqual(await page.evaluate(()=>selectedCell),[0,0]);
        await page.locator('#stage').click({position:{x:208,y:176}});
        assert.deepEqual(await page.evaluate(()=>selectedCell),[6,5]);
        assert(await page.locator('.cell-highlight').isVisible());
        await page.locator('.marker[data-kind="npcs"]').first().click();
        assert.deepEqual(await page.evaluate(()=>selectedCell),[3,10]);
        assert(await page.locator('#scenario').isVisible());
        assert((await page.locator('.unlock-guide').innerText()).includes('在研究所选定初始宝可梦'));
        assert((await page.locator('.unlock-guide').innerText()).includes('没有先后依赖'));
        assert(await page.locator('.range-cell').count() > 0);
        assert((await page.locator('#detail').innerText()).includes('包含障碍格'));
        assert(!/WANDER_AROUND|0x[0-9A-F]{4}/.test(await page.locator('#detail').innerText()));
        assert.equal(await page.locator('.technical').getAttribute('open'), null);
        const scenarios = await page.locator('#scenario option').allTextContents();
        assert(scenarios.length > 1);
        await page.locator('#scenario').selectOption('0');
        assert((await page.locator('#detail').innerText()).includes('告示牌女孩 · 菜单教学'));
        assert(!/剧情条件 \d/.test(await page.locator('#detail').innerText()));
        assert.equal(await page.locator('.branch-choice').count(), 6);
        assert.deepEqual(await page.evaluate(()=>route.at(-1)),[2,10]);
        await page.locator('.branch-choice').nth(4).selectOption('1');
        assert.equal(await page.locator('.branch-choice').nth(5).inputValue(),'0');
        assert.deepEqual(await page.evaluate(()=>route.at(-1)),[4,10]);
        await page.locator('.branch-choice').nth(5).selectOption('1');
        assert.equal(await page.locator('.branch-choice').nth(4).inputValue(),'0');
        assert.deepEqual(await page.evaluate(()=>route.at(-1)),[2,10]);
        const initial = await page.evaluate(() => ({route, texts: [...document.querySelectorAll('.dialogue')].map(n => n.textContent)}));
        const branch = page.locator('.branch-choice').first();
        if (await branch.count()) {
            await branch.selectOption('1');
            const changed = await page.evaluate(() => ({route, texts: [...document.querySelectorAll('.dialogue')].map(n => n.textContent)}));
            assert.notDeepEqual(changed, initial);
        }
        await page.screenshot({path: 'target/kanto-explorer-npc.png', fullPage: true});
        assert((await page.locator('#detail').innerText()).includes('告示牌教学已经结束'));
        assert.equal(await page.locator('#detail .dialogue').count(),1);
        assert.equal(await page.locator('.event-sequence').count(),0);
        const triggerOption=await page.locator('#scenario option').evaluateAll(options=>options.find(o=>o.textContent.includes('(13, 2)'))?.value);
        assert(triggerOption!==undefined);
        await page.locator('#viewport').evaluate(n=>{n.scrollTop=n.scrollHeight;});
        await page.locator('#scenario').selectOption(triggerOption);
        assert.deepEqual(await page.evaluate(()=>selectedCell),[13,2]);
        assert(await page.locator('.cell-highlight').evaluate(n=>{
            const cell=n.getBoundingClientRect(),view=document.getElementById('viewport').getBoundingClientRect();
            return cell.top>=view.top&&cell.bottom<=view.bottom&&cell.left>=view.left&&cell.right<=view.right;
        }));
        await page.locator('#scenario').selectOption('-1');
        assert.deepEqual(await page.evaluate(()=>selectedCell),[3,10]);
        // Pallet's left entrance: Oak walks up/up/right/up/up/right/up/up,
        // then leads the player to the lab at (16,13). In-place facing adds no tile.
        const teaching = await page.evaluate(() => {
            const npc = current.npcs.find(n => +n.id === 3);
            const source = sources().find(s => s.kind === 'coordinate-events' && +s.e.x === 12 && targets(s,npc));
            choices = {'1463879:1':true, '1463916:1':true};
            return trace(source, npc);
        });
        assert.deepEqual(teaching.cells.slice(0,9), [[10,8],[10,7],[10,6],[11,6],[11,5],[11,4],[12,4],[12,3],[12,2]]);
        assert.deepEqual(teaching.cells.at(-1), [16,13]);
        assert.equal(teaching.visible, false);
        await page.evaluate(() => {
            const npc=current.npcs.find(n=>+n.id===3);select('npcs',npc);
            const related=sources().filter(s=>s.e===npc||targets(s,npc));
            sourceIndex=related.findIndex(s=>s.kind==='coordinate-events' && +s.e.x===12);inspect();
            choices={'1463879:1':true, '1463916:1':true};inspect();
        });
        assert(await page.locator('.route-cell').count()>20);
        await page.screenshot({path:'target/kanto-explorer-route.png',fullPage:true});
        await page.locator('.marker[data-kind="warps"]').first().click();
        await page.getByRole('button', {name: '进入目标地图 →'}).click();
        assert.notEqual(await page.locator('#mapSelect').inputValue(), '3/0');
        assert(await page.locator('.marker.active').count() === 1);
        await page.locator('#mapSelect').selectOption('3/0');
        await page.locator('#zoom').selectOption('4');
        assert.equal(await page.locator('#stage').evaluate(n => n.offsetWidth), await page.evaluate(() => +current.columns * 64));
        await page.locator('#stage').click({position:{x:416,y:352}});
        assert.deepEqual(await page.evaluate(()=>selectedCell),[6,5]);
        assert.equal(await page.locator('.cell-highlight').evaluate(n=>n.offsetWidth),64);
        assert.equal(await page.locator('.x-axis span').first().evaluate(n=>n.getBoundingClientRect().width),64);
        assert.equal(await page.locator('.y-axis span').first().evaluate(n=>n.getBoundingClientRect().height),64);
        await page.locator('#filters input').first().uncheck();
        assert.equal(await page.locator('.marker[data-kind="npcs"]').count(), 0);
        await page.locator('#home').click();
        assert(await page.locator('#overview').isVisible());
        await page.setViewportSize({width: 390, height: 844});
        await page.getByRole('button', {name: '真新镇', exact: true}).click();
        assert(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth));
        await page.screenshot({path: 'target/kanto-explorer-mobile.png', fullPage: true});
        assert.deepEqual(errors, []);
        console.log('PASS: overview, city entry, NPC range, branch switch, warp landing, zoom, filters, mobile; no JS errors');
    } finally { await browser.close(); }
})().catch(error => {console.error(error); process.exitCode = 1;});
