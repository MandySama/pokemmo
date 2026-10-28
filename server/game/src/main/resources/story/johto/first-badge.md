# 城都首枚徽章

源：`client/roms/心金.nds`（IPKJ，SHA256 `4b06de01c9f09b0437be4dc500cbafee743168f1811dbef18319af7f46b814b5`）。地图与NPC来自 ARM9 地图头和 `a/0/3/2`，训练家来自 `a/0/5/5`、`a/0/5/6`，文本ID来自 `a/0/2/7`。参考客户端 `HeartGoldSoulSilverRom`、`DpptTextBank`、`NdsRibbonMedalEntry`、`NdsRibbonRequirementEntry`；参考服务端无城都脚本，复用本项目合众 RegionalStory 的持久化动作机制。

起点主角家2F（64,7,6）；家1F母亲、空木研究所领取初始伙伴、吉花导游、宝可梦爷爷神奇蛋/大木图鉴、吉花宿敌、研究所交蛋、29号道路捕获教学、喇叭芽之塔长老、桔梗道馆阿速。stage 0..9；初始伙伴152/155/158，对应宿敌155/158/152。原生训练家队伍和招式，首战失败不赠徽章/推进，允许重试。badge与道具奖励以持久化标记防重复。

范围与适配：现有对话/选项/战斗协议，不执行完整HGSS脚本VM。捕获教学只接入原文对话及5球，不播放原生自动捕获演示；导游不播放原生逐点领走；电话、命名、妈妈存款、跟随精灵、首徽章后蛋事件未接入。未引入自造界面。支线NPC仅部分可交互，塔与道馆原生训练家可战斗。训练家奖金依据原生overlay12已逐字节核实的class倍率×末位等级×4，见johto-reference/prize-multipliers.md。野生概率沿用服务器现有机制。

本机汉化ROM字表与参考中文表不一致，文字采用官方安装现有繁中MOD覆盖，见客户端同目录texts.md。原始资源仅提取到build/johto，不打包ROM。

生成：`server/game/johto-reference/Probe.java`、`MapsProbe.java`、`generate.py`。对应状态/UI/连通性测试在 `client/desktop/src/johtoTest`。

物品转换：原生TM51/378不能直接+5000（BW为交换场地）。实际InventoryItems加载参考内建定义+data.pak后，规范item1610的machineMove=355/羽栖；本章奖励1610。TM70/397映射5397的machineMove=148/闪光已实际核实。

NPC外观：原生spriteId先按本机overlay1偏移137756逐字节核实的901项表转换为参考GY模型编号，保留nativeGraphics便于复核；方向按原生北0南1转换到客户端北1南0。详见johto-reference/npc-models.md。

验证（2026-10-28）：独立johtoAudit最终12项全部通过，含六组合状态恢复/防重、真实阿速战斗及奖金结算、全章节MapMovement步行连通、PC/Android实际选择、母亲/护士/阿速界面和真实招式回合。详情见johto-verification/城都剧情审计.md。
