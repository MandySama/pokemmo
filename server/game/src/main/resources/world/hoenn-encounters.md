# 丰缘野外遭遇

来源：pret/pokeemerald `src/data/wild_encounters.json` 与 `include/constants/pokedex.h`，地图坐标使用已核对 ROM 的 `hoenn-maps.json`。生成器：`server/game/generate-hoenn-encounters.py`。

124 张原生地图，保留陆地／水面遭遇率、槽位权重和等级范围；物种转换为全国图鉴编号，避免绿宝石内部物种编号与现有战斗库错位。随机判定复用项目已有的有效步数遭遇机制。

仅覆盖现有机制支持的陆地和水面遭遇；未新增钓鱼、碎岩、特殊事件遭遇或 GBA 原生遭遇算法。
