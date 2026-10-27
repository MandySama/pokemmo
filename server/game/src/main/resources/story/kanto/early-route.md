# kanto.early_route_to_vermilion

来源：参考服务端 `resource/story/kanto/early_route_to_vermilion/chapter.jsonc`。

## 触发与分支

- `NPC` / `PewterCity_Gym` / `0` → `brock`；条件：`[{"type": "CAPTURE_TUTORIAL_COMPLETE"}, {"type": "BADGE_CLEAR", "value": 0}]`。
- `COORDINATE` / `Route22` / `Route22_EventScript_EarlyRivalTriggerTop` → `route22_early_rival`；条件：`[{"type": "BADGE_CLEAR", "value": 0}, {"type": "EXTRA_STORY_BIT_CLEAR", "value": 6}]`。
- `COORDINATE` / `Route22` / `Route22_EventScript_EarlyRivalTriggerMid` → `route22_early_rival`；条件：`[{"type": "BADGE_CLEAR", "value": 0}, {"type": "EXTRA_STORY_BIT_CLEAR", "value": 6}]`。
- `COORDINATE` / `Route22` / `Route22_EventScript_EarlyRivalTriggerBottom` → `route22_early_rival`；条件：`[{"type": "BADGE_CLEAR", "value": 0}, {"type": "EXTRA_STORY_BIT_CLEAR", "value": 6}]`。
- `NPC` / `MtMoon_B2F` / `2` → `fossil`；条件：`[{"type": "BADGE_SET", "value": 0}, {"type": "STORY_BIT_CLEAR", "value": 6}, {"type": "EXTRA_STORY_BIT_CLEAR", "value": 7}]`。
- `COORDINATE` / `MtMoon_B2F` / `MtMoon_B2F_EventScript_MiguelTrigger` → `fossil`；条件：`[{"type": "BADGE_SET", "value": 0}, {"type": "STORY_BIT_CLEAR", "value": 6}, {"type": "EXTRA_STORY_BIT_CLEAR", "value": 7}]`。
- `COORDINATE` / `CeruleanCity` / `(22, 6)` → `cerulean_rival`；条件：`[{"type": "BADGE_SET", "value": 0}, {"type": "STORY_BIT_SET", "value": 6}, {"type": "STORY_BIT_CLEAR", "value": 7}]`。
- `NPC` / `CeruleanCity` / `1` → `cerulean_rocket`；条件：`[{"type": "STORY_BIT_SET", "value": 8}, {"type": "EXTRA_STORY_BIT_CLEAR", "value": 5}]`。
- `COORDINATE` / `CeruleanCity` / `CeruleanCity_EventScript_GruntTriggerTop` → `cerulean_rocket`；条件：`[{"type": "STORY_BIT_SET", "value": 8}, {"type": "EXTRA_STORY_BIT_CLEAR", "value": 5}]`。
- `COORDINATE` / `CeruleanCity` / `CeruleanCity_EventScript_GruntTriggerBottom` → `cerulean_rocket`；条件：`[{"type": "STORY_BIT_SET", "value": 8}, {"type": "EXTRA_STORY_BIT_CLEAR", "value": 5}]`。
- `COORDINATE` / `CeruleanCity` / `(23, 6)` → `cerulean_rival`；条件：`[{"type": "BADGE_SET", "value": 0}, {"type": "STORY_BIT_SET", "value": 6}, {"type": "STORY_BIT_CLEAR", "value": 7}]`。
- `COORDINATE` / `CeruleanCity` / `(24, 6)` → `cerulean_rival`；条件：`[{"type": "BADGE_SET", "value": 0}, {"type": "STORY_BIT_SET", "value": 6}, {"type": "STORY_BIT_CLEAR", "value": 7}]`。
- `NPC` / `Route24` / `0` → `rocket`；条件：`[{"type": "STORY_BIT_SET", "value": 7}, {"type": "STORY_BIT_CLEAR", "value": 8}]`。
- `COORDINATE` / `Route24` / `Route24_EventScript_RocketTriggerLeft` → `rocket`；条件：`[{"type": "STORY_BIT_SET", "value": 7}, {"type": "STORY_BIT_CLEAR", "value": 8}]`。
- `COORDINATE` / `Route24` / `Route24_EventScript_RocketTriggerRight` → `rocket`；条件：`[{"type": "STORY_BIT_SET", "value": 7}, {"type": "STORY_BIT_CLEAR", "value": 8}]`。
- `NPC` / `Route25_SeaCottage` / `0` → `bill`；条件：`[{"type": "STORY_BIT_SET", "value": 8}, {"type": "STORY_BIT_CLEAR", "value": 9}]`。
- `NPC` / `CeruleanCity_Gym` / `2` → `misty`；条件：`[{"type": "STORY_BIT_SET", "value": 9}, {"type": "BADGE_CLEAR", "value": 1}]`。
- `MAP_READY` / `VermilionCity` / `(None, None)` → `vermilion_arrival`；条件：`[{"type": "BADGE_SET", "value": 1}, {"type": "STORY_BIT_CLEAR", "value": 11}]`。
- `NPC` / `VermilionCity` / `4` → `ss_anne_boarding`；条件：`[{"type": "STORY_BIT_SET", "value": 11}, {"type": "STORY_BIT_CLEAR", "value": 12}]`。
- `COORDINATE` / `SSAnne_2F_Corridor` / `(30, 6)` → `ss_anne_rival`；条件：`[{"type": "STORY_BIT_CLEAR", "value": 12}]`。
- `COORDINATE` / `SSAnne_2F_Corridor` / `SSAnne_2F_Corridor_EventScript_RivalTriggerMid` → `ss_anne_rival`；条件：`[{"type": "STORY_BIT_CLEAR", "value": 12}]`。
- `COORDINATE` / `SSAnne_2F_Corridor` / `SSAnne_2F_Corridor_EventScript_RivalTriggerRight` → `ss_anne_rival`；条件：`[{"type": "STORY_BIT_CLEAR", "value": 12}]`。
- `NPC` / `SSAnne_CaptainsOffice` / `0` → `captain`；条件：`[{"type": "STORY_BIT_SET", "value": 12}, {"type": "STORY_BIT_CLEAR", "value": 13}]`。
- `NPC` / `VermilionCity_Gym` / `0` → `surge`；条件：`[{"type": "STORY_BIT_SET", "value": 13}, {"type": "BADGE_CLEAR", "value": 2}]`。

## 持久化与边界

沿用参考节点、文本引用和队伍定义；奖励与标记按节点事务提交、幂等防重，战败不发胜利奖励；胜后续节点可重连恢复。地图机关和捕获战斗适配的实际验证范围见 `docs/reference/kanto-mainline.md`。
