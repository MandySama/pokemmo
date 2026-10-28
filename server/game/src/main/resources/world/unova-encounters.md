# 合众第一枚徽章范围内的野生遭遇

对应 `unova-encounters.json`，仅包含 1 号道路（317）、2 号道路（319）、梦之遗迹（152）的普通草丛表。

来源为本地黑白 ROM：地图头 `a/0/1/2` 每条 48 字节、偏移 20 的遭遇索引，指向 `a/1/2/6`。本次三个条目都是单季 232 字节表，普通草丛的 12 个槽位从偏移 8 开始，每槽 4 字节（物种 16 位、最低／最高等级各 8 位）。槽位概率为 20、20、10、10、10、10、5、5、4、4、1、1。

结构核对工具：FrostsGen5Editor 的 EncounterNARC（提交 `572e4c2e242d18e6a2a3a9d166b143143d66c3f8`）。参考客户端 `MapTilePropertyReader.eq0` 读取地形第三个 short；`TileElevationCollisionCalculator` 将合众行为 4／5 注册为草丛。客户端按该行为触发，服务端复用现有 `WildEncounters` 步数与抽选机制，训练家战或剧情进行中不启动遭遇。

双打深草、水面、摇草、垂钓及其他地图尚未接入。此文件不改变剧情阶段；战斗经验、捕捉和队伍持久化复用现有战斗服务。

重建方法参见 `server/game/unova-reference/README.md`。
