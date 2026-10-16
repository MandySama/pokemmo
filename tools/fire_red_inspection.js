// Static inspection preserves alternate branches; it does not simulate a character save.
const originalInspect = inspect;
function paragraph(parent, text) {
    const p = document.createElement('p');
    p.textContent = text;
    parent.append(p);
}
function section(parent, title) {
    const h = document.createElement('h3');
    h.textContent = title;
    parent.append(h);
}
function scriptInfo(parent, event) {
    const info = scriptDetails[parseInt(event.script_offset, 16)];
    section(parent, '脚本与剧情归属');
    if (!info) {
        paragraph(parent, '没有可分析的脚本入口。');
        return;
    }
    paragraph(parent, info.scenes.length ? info.scenes.map(s => s.name + ' · ' + s.id).join('\n') :
        '未直接绑定项目剧情节点。可能是普通交互、支线或公共脚本，不能据此认定不是剧情。');
    paragraph(parent, '剧情标题为项目命名。以下按 ROM 控制流静态收集所有可达分支，并非当前角色必然发生的顺序；变量、选择和标记决定实际对白与行为。');
    const commands = info.records.map(r => r.command);
    const categories = [];
    if (info.texts.length) categories.push('对白');
    if (commands.some(c => /movement|object/.test(c))) categories.push('人物移动 / 显隐');
    if (commands.some(c => /trainerbattle/.test(c))) categories.push('训练家战斗');
    if (commands.some(c => /flag|var|goto_if|call_if/.test(c))) categories.push('条件判断 / 状态变化');
    if (commands.some(c => /item|pokemon/.test(c))) categories.push('道具 / 宝可梦操作');
    paragraph(parent, '按已解析指令识别用途：' + (categories.join('、') || '其他 / 待解析') + '。');
    section(parent, '可能出现的对白');
    for (const text of info.texts) {
        const d = document.createElement('details');
        const summary = document.createElement('summary');
        summary.textContent = text.address + ' · ' + text.text.slice(0, 45);
        d.append(summary);
        paragraph(d, text.text);
        paragraph(d, '引用指令：' + text.instruction + '；中文来自项目语言库，占位符保留。');
        parent.append(d);
    }
    if (!info.texts.length) paragraph(parent, '未发现直接文本引用；特殊函数或动态文本可能另行生成对白。');
    section(parent, '条件、分支与行为');
    paragraph(parent, '物件 255 为玩家；0x8000 起的编号可能是变量。applymovement 为移动序列，setobjectxy 为改坐标，add/removeobject 为显隐，set/checkflag 为标记操作。');
    for (const record of info.records) paragraph(parent,
        record.address + ' · ' + record.command + ' · ' + record.detail);
    if (info.missing.length) paragraph(parent, '导出指令未覆盖的地址（此入口分析不完整）：' + info.missing.join('、'));
}
inspect = function(kind, event) {
    originalInspect(kind, event);
    const parent = $('detail');
    if (kind === 'warps') {
        const target = byKey.get(event.target_group + '/' + event.target_map);
        if (event.dynamic_target === 'false' && target) {
            const landing = target.warps.find(w => +w.index === +event.target_warp);
            section(parent, '目标地点 / 房间');
            paragraph(parent, target.name + ' · ' + key(target) + ' · ' + target.columns + '×' + target.rows + ' 格');
            paragraph(parent, landing ? '目标事件落点：(' + landing.x + ', ' + landing.y + ')，高度 ' + landing.elevation : '目标事件索引未找到');
            const image = document.createElement('img');
            image.src = 'images/' + target.group + '-' + target.map + '.png';
            image.alt = target.name + '目标地图预览';
            image.style.cssText = 'max-width:100%;image-rendering:pixelated';
            parent.append(image);
        } else {
            paragraph(parent, '动态出口依赖进入此房间的历史，静态清单无法确定唯一返回房间。');
        }
    } else {
        if (kind === 'npcs') {
            section(parent, '日常活动与剧情变化');
            paragraph(parent, '初始位置 (' + event.x + ', ' + event.y + ')；移动类型 ' + event.movement +
                ' (' + event.movement_name + ')；活动范围 ' + event.range_x + '×' + event.range_y + '；隐藏标记 ' + event.flag + '。');
            paragraph(parent, '地图标记表示 ROM 初始位置，实际存档中的剧情移动与显隐可能改变它。');
            for (const otherKind of ['npcs', 'coordinate-events', 'map-scripts']) {
                for (const source of current[otherKind]) {
                    const info = scriptDetails[parseInt(source.script_offset, 16)];
                    const changes = info?.records.filter(r => r.actor === +event.id) || [];
                    if (!changes.length) continue;
                    const d = document.createElement('details');
                    const summary = document.createElement('summary');
                    summary.textContent = '本图关联行为：' + (types[otherKind] || '地图脚本') + ' #' + source.index;
                    d.append(summary);
                    for (const r of changes) paragraph(d, r.address + ' · ' + r.command + ' · ' + r.detail);
                    paragraph(d, '按当前地图物件编号匹配；跨地图调用及变量物件需结合脚本条件判断。');
                    parent.append(d);
                }
            }
        }
        scriptInfo(parent, event);
        const link = document.createElement('a');
        const editor = new URL('http://127.0.0.1:8765/');
        if (location.protocol === 'http:' && location.hostname === '127.0.0.1') editor.port = location.port;
        editor.searchParams.set('map', key(current));
        if (kind === 'npcs') editor.searchParams.set('npc', event.id);
        link.href = editor.href;
        link.textContent = managedEditorLabel;
        parent.append(link);
    }
};
const originalShow = show;
show = function(map) {
    originalShow(map);
    for (const [i, element] of [...$('scripts').children].entries()) {
        const event = map['map-scripts'][i];
        if (!event) continue;
        element.tabIndex = 0;
        element.style.cursor = 'pointer';
        const open = () => {
            $('detail').replaceChildren();
            paragraph($('detail'), '地图脚本 #' + event.index + ' · 类型 ' + event.type +
                ' · 条件 ' + event.variable + ' = ' + event.value);
            scriptInfo($('detail'), event);
        };
        element.onclick = open;
        element.onkeydown = e => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); open(); } };
    }
};
show(byKey.get('3/0') || current);
