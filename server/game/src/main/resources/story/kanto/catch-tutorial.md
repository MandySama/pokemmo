# kanto.viridian_city.catch_tutorial

来源：参考服务端 `resource/story/kanto/viridian_city/catch_tutorial/chapter.jsonc`。

## 触发与分支

- `MAP_READY` / `ViridianCity` / `(None, None)` → `capture`；条件：`[{"type": "STORY_STAGE_AT_LEAST", "value": 6}, {"type": "CAPTURE_TUTORIAL_INCOMPLETE"}, {"type": "HAS_CAPTURE_BALL"}, {"type": "HAS_BATTLE_POKEMON"}]`。
- `NPC` / `ViridianCity` / `3` → `capture`；条件：`[{"type": "STORY_STAGE_AT_LEAST", "value": 6}, {"type": "CAPTURE_TUTORIAL_INCOMPLETE"}, {"type": "HAS_CAPTURE_BALL"}, {"type": "HAS_BATTLE_POKEMON"}]`。
- `NPC` / `ViridianCity` / `3` → `no_ball`；条件：`[{"type": "STORY_STAGE_AT_LEAST", "value": 6}, {"type": "CAPTURE_TUTORIAL_INCOMPLETE"}, {"type": "NO_CAPTURE_BALL"}]`。
- `NPC` / `ViridianCity` / `3` → `completed`；条件：`[{"type": "STORY_STAGE_AT_LEAST", "value": 6}, {"type": "CAPTURE_TUTORIAL_COMPLETE"}]`。

## 持久化与边界

沿用参考节点、文本引用和队伍定义；奖励与标记按节点事务提交、幂等防重，战败不发胜利奖励；胜后续节点可重连恢复。地图机关和捕获战斗适配的实际验证范围见 `docs/reference/kanto-mainline.md`。

新角色实走修复：自动／NPC 捕获教学仅在队伍存在非蛋且 HP > 0 的精灵时开始，避免旧的全倒下存档反复尝试启动失败。全倒下可正常走到护士处恢复，再返城／与老人交互。普通持久战在失败结束确认时，按参考 `BattleFinishSuccessPacket` 恢复队伍 HP、PP 和状态；不自行增加缺少依据的扣钱或传送地点。
