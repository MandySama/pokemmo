# 真新镇劲敌家：娜娜美

来源：参考服务端 `map/kanto/pallet_town_rivals_house/PalletTown_RivalsHouse.json`；原生 `pret/pokefirered/data/maps/PalletTown_RivalsHouse/scripts.inc`。BPRE rev1 脚本地址 0x168DCE；JSON 台词为经现有 offsets 表核实的规范 ROM 偏移，不另造台词。地图资源只包含默认站位，原生 OnTransition 的覆盖此前漏接。

- 未领取城镇地图：娜娜美固定桌边 (5,4)，朝右；禁止默认右侧随机走动，地图道具仍在 (6,4)。
- 首次劲敌战斗前：弟弟在博士研究所；结束后恢复朝右。
- 战斗完成、图鉴前：听说你与弟弟对战。
- 已领取图鉴：说明差事→面向桌子→领取 #361 城镇地图（参考 Item.jsonc 的关都 ID）→桌上地图消失→面向玩家→原生获得消息。领取与隐藏标记同事务、重复对话/重连不重复发放。
- 同次进屋再谈：解释地图；离开重进：提醒宝可梦休息。领取后下次进屋恢复资源默认 (10,6)、范围 X=1/Y=3 随机走动。
- 通关后的亲密度评价按原生七档选用 ROM 文本；本次不扩展 500 步冷却与梳理服务。没有虚构梳理已生效的台词或奖励。

位置和地图隐藏通过角色投影生效，不修改基础地图坐标，不迁移/重置旧角色剧情阶段。地图剧情查看器列出开局及领取前后分支。

验证：`PalletDaisyTest` 覆盖桌边坐标/朝向、各阶段台词、地图唯一发放、地图剧情查看及重建服务后隐藏/站位；`PalletDaisyUiTest` 在 PC/移动布局各经实际按键完成领取与进出房间，截图已目视核对。加上原看牌女孩 3 项回归，共 6 项通过。使用新建临时测试存档，不操作原存档。

朝向修正：DIALOGUE 显式携带 facePlayer，客户端进入文本时才锁定并朝向玩家；intro 的原生 FaceOriginalDirection 在对话后恢复右向。通用 FACE_NPC/PLAYER 以目标 NPC 的实际投影坐标计算，不能借用看牌女孩的坐标。领取地图最后应面向玩家；其他分支不擅自增加原生脚本没有的转回动作。UI 测试同时检查对话中朝向与结束朝向，不只检查动作名。

朝向复验：`daisy-facing` 6 项通过；`daisy-facing-rendered` 再验证两端实际绘制帧，在精灵方向已绘制后才抓屏并继续文本。`daisy-false-text-1627532.png` 可见向下看玩家，`daisy-false-restored-right.png` 可见结束后朝右，均已目视核对。
