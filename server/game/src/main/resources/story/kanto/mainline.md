# kanto.mainline_to_champion

基础来源：参考服务端 `resource/story/kanto/mainline_to_champion/chapter.jsonc`。本地新增七楼富士老人的嘎啦嘎啦安抚前置，衔接用户授权补充的 `middle.json`。

## 触发与分支

- `NPC` / `PokemonTower_7F` / `0` → `tower_fuji`；条件：`[{"type": "BADGE_SET", "value": 2}, {"type": "EXTRA_STORY_BIT_SET", "value": 4}, {"type": "STORY_BIT_CLEAR", "value": 0}, {"type": "EVENT_FLAG_SET", "flag": "kanto/middle/marowak_appeased"}]`。
- `COORDINATE` / `PokemonTower_2F` / `PokemonTower_2F_EventScript_RivalTriggerRight` → `tower_rival`；条件：`[{"type": "BADGE_SET", "value": 2}, {"type": "EXTRA_STORY_BIT_CLEAR", "value": 4}]`。
- `COORDINATE` / `PokemonTower_2F` / `PokemonTower_2F_EventScript_RivalTriggerDown` → `tower_rival`；条件：`[{"type": "BADGE_SET", "value": 2}, {"type": "EXTRA_STORY_BIT_CLEAR", "value": 4}]`。
- `NPC` / `MtMoon_B2F` / `0` → `dome_fossil`；条件：`[{"type": "BADGE_SET", "value": 0}, {"type": "STORY_BIT_CLEAR", "value": 6}]`。
- `NPC` / `MtMoon_B2F` / `1` → `helix_fossil`；条件：`[{"type": "BADGE_SET", "value": 0}, {"type": "STORY_BIT_CLEAR", "value": 6}]`。
- `NPC` / `CinnabarIsland_PokemonLab_ExperimentRoom` / `1` → `fossil_lab`；条件：`[{"type": "STORY_BIT_SET", "value": 6}, {"type": "HAS_ITEM", "value": 357}]`。
- `NPC` / `CinnabarIsland_PokemonLab_ExperimentRoom` / `1` → `fossil_lab`；条件：`[{"type": "STORY_BIT_SET", "value": 6}, {"type": "HAS_ITEM", "value": 358}]`。
- `NPC` / `LavenderTown_VolunteerPokemonHouse` / `0` → `fuji_flute`；条件：`[{"type": "STORY_BIT_SET", "value": 0}, {"type": "NO_ITEM", "value": 350}]`。
- `NPC` / `RocketHideout_B4F` / `0` → `hideout_giovanni`；条件：`[{"type": "BADGE_SET", "value": 2}, {"type": "STORY_BIT_CLEAR", "value": 1}]`。
- `NPC` / `RocketHideout_B4F` / `3` → `lift_key`；条件：`[{"type": "EVENT_FLAG_SET", "flag": "kanto/trainer/RocketHideout_B4F/RocketHideout_B4F_EventScript_Grunt1"}, {"type": "EXTRA_STORY_BIT_CLEAR", "value": 2}]`。
- `NPC` / `CeladonCity_GameCorner` / `10` → `game_corner_rocket`；条件：`[{"type": "BADGE_SET", "value": 2}, {"type": "STORY_BIT_CLEAR", "value": 1}]`。
- `NPC` / `CeladonCity_Gym` / `6` → `erika`；条件：`[{"type": "BADGE_SET", "value": 2}, {"type": "BADGE_CLEAR", "value": 3}]`。
- `NPC` / `FuchsiaCity_WardensHouse` / `0` → `warden`；条件：`[{"type": "STORY_BIT_SET", "value": 15}, {"type": "HAS_ITEM", "value": 353}, {"type": "NO_ITEM", "value": 342}]`。
- `NPC` / `SafariZone_West` / `0` → `gold_teeth`；条件：`[{"type": "STORY_BIT_CLEAR", "value": 15}]`。
- `NPC` / `FuchsiaCity_SafariZone_Entrance` / `1` → `safari_entry`；条件：`[{"type": "NOT_IN_BATTLE"}, {"type": "NOT_IN_TRADE"}]`。
- `NPC` / `SafariZone_SecretHouse` / `0` → `safari_prize`；条件：`[{"type": "BADGE_SET", "value": 2}, {"type": "STORY_BIT_CLEAR", "value": 2}]`。
- `NPC` / `PokemonMansion_B1F` / `5` → `mansion_key`；条件：`[{"type": "BADGE_SET", "value": 4}, {"type": "STORY_BIT_CLEAR", "value": 4}]`。
- `NPC` / `FuchsiaCity_Gym` / `6` → `koga`；条件：`[{"type": "BADGE_SET", "value": 3}, {"type": "BADGE_CLEAR", "value": 4}]`。
- `COORDINATE` / `SilphCo_11F` / `(5, 15)` → `silph_giovanni`；条件：`[{"type": "STORY_BIT_SET", "value": 1}, {"type": "STORY_BIT_CLEAR", "value": 3}]`。
- `COORDINATE` / `SilphCo_11F` / `(6, 15)` → `silph_giovanni`；条件：`[{"type": "STORY_BIT_SET", "value": 1}, {"type": "STORY_BIT_CLEAR", "value": 3}]`。
- `COORDINATE` / `SilphCo_7F` / `(2, 4)` → `silph_rival`；条件：`[{"type": "STORY_BIT_SET", "value": 1}, {"type": "STORY_BIT_CLEAR", "value": 3}, {"type": "EXTRA_STORY_BIT_CLEAR", "value": 1}]`。
- `NPC` / `SilphCo_7F` / `1` → `lapras_gift`；条件：`[{"type": "STORY_BIT_SET", "value": 1}, {"type": "EXTRA_STORY_BIT_CLEAR", "value": 8}]`。
- `COORDINATE` / `SilphCo_7F` / `(2, 5)` → `silph_rival`；条件：`[{"type": "STORY_BIT_SET", "value": 1}, {"type": "STORY_BIT_CLEAR", "value": 3}, {"type": "EXTRA_STORY_BIT_CLEAR", "value": 1}]`。
- `NPC` / `SaffronCity_Gym` / `6` → `sabrina`；条件：`[{"type": "BADGE_SET", "value": 3}, {"type": "STORY_BIT_SET", "value": 3}, {"type": "BADGE_CLEAR", "value": 5}]`。
- `NPC` / `CinnabarIsland_Gym` / `7` → `blaine`；条件：`[{"type": "STORY_BIT_SET", "value": 4}, {"type": "BADGE_CLEAR", "value": 6}]`。
- `NPC` / `ViridianCity_Gym` / `7` → `giovanni`；条件：`[{"type": "BADGE_SET", "value": 6}, {"type": "STORY_BIT_SET", "value": 3}, {"type": "BADGE_CLEAR", "value": 7}]`。
- `NPC` / `PokemonLeague_LoreleisRoom` / `0` → `lorelei`；条件：`[{"type": "BADGE_SET", "value": 7}, {"type": "ELITE_STAGE_EQUALS", "value": 0}, {"type": "CHAMPION_CLEAR"}]`。
- `NPC` / `PokemonLeague_BrunosRoom` / `0` → `bruno`；条件：`[{"type": "BADGE_SET", "value": 7}, {"type": "ELITE_STAGE_EQUALS", "value": 1}, {"type": "CHAMPION_CLEAR"}]`。
- `NPC` / `PokemonLeague_AgathasRoom` / `0` → `agatha`；条件：`[{"type": "BADGE_SET", "value": 7}, {"type": "ELITE_STAGE_EQUALS", "value": 2}, {"type": "CHAMPION_CLEAR"}]`。
- `NPC` / `PokemonLeague_LancesRoom` / `0` → `lance`；条件：`[{"type": "BADGE_SET", "value": 7}, {"type": "ELITE_STAGE_EQUALS", "value": 3}, {"type": "CHAMPION_CLEAR"}]`。
- `MAP_READY` / `PokemonLeague_ChampionsRoom` / `(None, None)` → `champion`；条件：`[{"type": "BADGE_SET", "value": 7}, {"type": "ELITE_STAGE_EQUALS", "value": 4}, {"type": "CHAMPION_CLEAR"}]`。
- `COORDINATE` / `Route22` / `(33, 4)` → `route22_rival`；条件：`[{"type": "BADGE_SET", "value": 7}, {"type": "CHAMPION_CLEAR"}, {"type": "EXTRA_STORY_BIT_CLEAR", "value": 0}]`。
- `COORDINATE` / `Route22_NorthEntrance` / `Route22_NorthEntrance_EventScript_BoulderBadgeGuardTrigger` → `boulder_gate`；条件：`[{"type": "BADGE_CLEAR", "value": 7}]`。
- `COORDINATE` / `Route23` / `Route23_EventScript_CascadeBadgeGuardTrigger` → `cascade_gate`；条件：`[{"type": "BADGE_CLEAR", "value": 1}]`。
- `COORDINATE` / `Route23` / `Route23_EventScript_ThunderBadgeGuardTrigger` → `thunder_gate`；条件：`[{"type": "BADGE_CLEAR", "value": 2}]`。
- `COORDINATE` / `Route23` / `Route23_EventScript_RainbowBadgeGuardTrigger` → `rainbow_gate`；条件：`[{"type": "BADGE_CLEAR", "value": 3}]`。
- `COORDINATE` / `Route23` / `Route23_EventScript_SoulBadgeGuardTrigger` → `soul_gate`；条件：`[{"type": "BADGE_CLEAR", "value": 4}]`。
- `COORDINATE` / `Route23` / `Route23_EventScript_MarshBadgeGuardTrigger` → `marsh_gate`；条件：`[{"type": "BADGE_CLEAR", "value": 5}]`。
- `COORDINATE` / `Route23` / `Route23_EventScript_VolcanoBadgeGuardTrigger` → `volcano_gate`；条件：`[{"type": "BADGE_CLEAR", "value": 6}]`。
- `COORDINATE` / `Route23` / `Route23_EventScript_EarthBadgeGuardTrigger` → `earth_gate`；条件：`[{"type": "BADGE_CLEAR", "value": 7}]`。
- `COORDINATE` / `Route22` / `(33, 5)` → `route22_rival`；条件：`[{"type": "BADGE_SET", "value": 7}, {"type": "CHAMPION_CLEAR"}, {"type": "EXTRA_STORY_BIT_CLEAR", "value": 0}]`。
- `COORDINATE` / `Route22` / `(33, 6)` → `route22_rival`；条件：`[{"type": "BADGE_SET", "value": 7}, {"type": "CHAMPION_CLEAR"}, {"type": "EXTRA_STORY_BIT_CLEAR", "value": 0}]`。
- `MAP_READY` / `PokemonLeague_HallOfFame` / `(None, None)` → `hall_of_fame`；条件：`[{"type": "CHAMPION_SET"}]`。

奖励与标记按节点事务保存，幂等防重；战败不发胜利奖励，胜后续节点可重连恢复。地图机关与实际验证范围见 `docs/reference/kanto-mainline.md`。

园长在归还金牙且尚未领取怪力（342）时发放秘传机，不以是否已击败阿桔作为领取限制，避免先打道馆后漏领怪力。

## 新角色实走修正：电梯钥匙

钥匙 NPC #3 仅在 B4F 左侧小兵 Grunt1 实际被击败且未领取（extra bit 2 未设）时出现并可领取；不再要求电梯另一侧 Grunt3，避免必须先有钥匙才能满足领取钥匙前置的循环。领取后保持原奖励 356 和进度标记，重连不重复领取。实体显隐与剧情入口使用同一前置。

差异依据：参考服务端 `mainline_to_champion/chapter.jsonc` 错误混入 Grunt3；核对 [火红原生 B4F 脚本](https://github.com/pret/pokefirered/blob/master/data/maps/RocketHideout_B4F/scripts.inc) 的 `DefeatedGrunt1` 仅显示钥匙，Grunt2/3 属于坂木门禁。此处按原生交互修正参考数据死锁，不新增按钮／奖励。

## 联盟入口显隐修正

参考地图 IndigoPlateau_Exterior 的 #0／#1 使用 FLAG_HIDE_CREDITS_RIVAL／FLAG_HIDE_CREDITS_OAK，均位于入口 (11,6)。这两个实体仅由片尾脚本 addobject 临时加入，普通地图加载（冠军前后及无开场记录旧档）均不显示，不以取得八徽章或冠军标记直接生成，避免叠在门口阻断进入联盟。当前未复刻完整片尾演出，亦不擅自增加剧情节点。

原生核对：https://github.com/pret/pokefirered/blob/master/data/maps/IndigoPlateau_Exterior/scripts.inc 。显隐属于地图投影修复，mainline.json 的触发／奖励未改动。

## 联盟战败与旧断点恢复

四天王及三支冠军队伍的 START_TRAINER_BATTLE 同步指定 defeatNode=league_defeat。实际战败清零 elite 并保存失败续接节点，结算结束后恢复队伍并回到联盟中心护士前 (13,12)，从科拿重新挑战；无胜利标记或奖励。外部旧档若精英进度低于所在后续房间，则 MAP_READY 同样返回中心，避免关门后困在错误房间，冠军档不触发。恢复只针对联盟，不改变其他地图的复活机制。

依据：参考 BattleFinishSuccessPacket 的战败恢复和 StoryRuntime 的战斗续接边界，原生 overworld.c DoWhiteOut 的 ResetEliteFourEnd／恢复／返回治疗点顺序。联盟入口前的治疗点固定为联盟中心；当前适配通过既有 HEAL_PARTY 和 CHANGE_MAP 完成，未宣称完整白屏及护士受伤演出已复刻。原生依据：https://github.com/pret/pokefirered/blob/master/src/overworld.c 。

冠军入场：mainline.json 的 champion 节点先 MOVE_PLAYER 到 (6,9)，对应原生从 (6,19) 向上十格，再显示既有开战对白并按初始精灵分支开战。冠军室 #1 大木博士带 FLAG_HIDE_OAK_IN_CHAMP_ROOM，只在原生胜后 addobject 片段生成，普通场景不预先站在通道 (6,15)；当前既有胜后直接进入名人堂的流程保留，尚未复刻博士完整训话／离场片段。依据：https://github.com/pret/pokefirered/blob/master/data/maps/PokemonLeague_ChampionsRoom/scripts.inc 。

最终验证（2026-10-27）：league-final-regression 共 36 项通过（含 PC／移动布局及成长依赖），continuation-lapras-route 连续副本实际战胜冠军并进入名人堂，BUILD SUCCESSFUL；八徽章与 champion=1 已核验落档。保留原档和永久备份未修改，完整片尾演出仍未复刻。


## 名人堂结尾演出（2026-10-27，替代旧通知占位）

- `hall_of_fame`：冠军档进入名人堂后执行 `HALL_OF_FAME`。客户端按参考 `GdxSoundManager`（`f.ys_2`）播放：1 秒开场、每位队员 4 秒独立展示／鸣叫／属性粒子、0.5 秒过渡、10 秒全队与训练家合影／庆祝粒子／卷动背景／淡出。
- 确认键、取消键或鼠标／触摸主键弹出官方字符串 6150“跳过此动画吗?”；选否继续，选是结束并回执。无额外按钮。PC／移动使用同一演出；训练家分别按参考 `InventoryBagSlotComponent.HP` 的 6／4 倍显示。
- 首次进入演出保存 `character_hall_of_fame` 的独立队伍快照，按队伍槽位排序，包含倒下成员；数据来自 `PokemonStore`，不会增加金钱／经验或改写队伍。结束或确认跳过才把 `completed` 置 1，过期／重复回执不重复结算。重连前未完成会重播快照，已完成直接续接 `hall_exit`。
- `hall_exit`：回到联盟外部 (11,6)，恢复世界操作。坐标依据原生 `hall_of_fame.c::SetWarpsToRollCredits`，通过现有 CHANGE_MAP 适配；跳过与自然播放使用同一出口。
- 资源：直接使用 `D:/PokeMMO/data/sprites/textures/hof.png`、`bg_00..02.png`，音乐 region=2 / sequence=1016，`particles.pak` 中 `special/hof`、`special/hof_party_trail`、属性效果；主题来自参考 `main-widgets.xml::hall-of-fame`。精灵采用参考本身支持的 BW 动态精灵回退，当前未接外部替换模型 MOD。原训练家、等级、捕获来源和时间来自快照；本地没有累计在线时长，未编造该数值。

参考边界：提供的服务端 chapter.jsonc 仅有 NOTIFY + CLOSE_SCENE；本次把参考客户端完整的统一名人堂场景接入本地剧情。火红 ROM 的制作人员滚动字幕／逐城巡游、冠军室博士训话和联盟外部博士／劲敌临时演员并非这段客户端场景，尚未复刻；不把本次统一名人堂演出称为完整火红原版片尾。

参考：`Pokemmo2Pro-Client/src/main/java/cn/pokemmo/audio/GdxSoundManager.java`、`graphics/gl/GlBufferStateManager.qF`、`ui/widget/component/InventoryBagSlotComponent`；原生出口 https://github.com/pret/pokefirered/blob/master/src/hall_of_fame.c 。


## 2026-10-27 名人堂结尾验证

参考端统一名人堂场景已接入，主线 JSON 与同名 MD 已同步。`hall-final` 共 9 项通过：HallOfFameUiTest 2、HallOfFameTest 1、KantoMainlineTest 4、KantoMainlineUiTest 2。PC 使用真实通关档新副本完整播放三位成员与全队合影；移动布局实际按确认键打开提示、触摸选否续播、触摸再次打开提示并选是跳过。两条路径均在联盟外 (11,6) 结束，再用真实方向键走到 (11,7)，重新读取存档确认位置。断线重播使用同一快照，已完成断线续接出口，错误／重复回执不重复记录和结算；余额始终 378320，队员等级与 HP 未被演出修改。

保留可继续游玩的结尾副本：`E:/Wednesday/Java/pokemmo-saves/fresh-journey-20261027-ending/game.db`，同目录 `completion.json` 记录来源、位置、校验结果。最早原档仅只读校验，逻辑内容与永久保留备份一致；永久备份 SHA256 仍为 d9351e8f5d566a0c9c4a8e8d9639b8863145131246b2a0723b2508c2af2ec63e，通关原快照仍为 6f891e52fdd9c260982d0a24d43a76e74cdd0d9a125b032d8cf74f77d114135e，三份原件 integrity_check 均 ok。

实际截图：`client/build/story-diagnostics/hall-false-member-0.png`、`hall-false-party.png`、`hall-true-confirm-cancel.png`、`hall-true-confirm-skip.png`、`hall-false-walk-after-ending.png`。已目视核对：属性背景／粒子、动态精灵、训练家与倒下成员合影、中文跳过确认。灰度 hof.png 按参考亮度处理，避免 LibGDX Alpha 解码导致黑底／地图穿透。既有移动战斗 monsterframe-hp-progressbar 主题 warning 不属于本次改动。

边界：完成的是所提供参考客户端 ys_2/GdxSoundManager 的统一名人堂结尾，不含火红原版制作名单巡游、冠军室博士训话及联盟外部临时演员。详见 mainline.md，未虚构缺少的在线时长或外部模型 MOD 支持。
