# 城都首徽章野生遭遇

来源：同剧情HGSS ROM SHA256；ARM9地图头第0字节为遇敌表索引，心金 `a/0/3/7` NARC。字段结构用 pret/pokeheartgold `include/wild_encounter.h` 与ROM每条196字节交叉核对：https://github.com/pret/pokeheartgold/blob/master/include/wild_encounter.h 。参考客户端无服务端野生概率实现，采用现有WildEncounters格式和抽签机制。

范围：29/30/31道路、喇叭芽之塔有遇敌表的楼层。地面普通白天12槽，权重20/20/10/10/10/10/5/5/4/4/1/1；等级、物种和遇敌率均取ROM。首层索引255表示无野生表，不生成。未接入早晚切换、广播、群聚、碎岩、垂钓、冲浪，未混入未经核实的概率。

触发：已经取得初始伙伴，地图对应草丛/洞窟行走，交由现有战斗引擎。此表不推进剧情；获胜/捕获走现有经验、捕获、背包机制。生成器`server/game/johto-reference/encounters.py`。
