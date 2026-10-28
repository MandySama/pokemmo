# 神奥首徽章野生遭遇

来源：本机白金 ROM（SHA256 67de86a1bc8e7eb6479dbbbd6e8f5edfc2fa160576a38e40e06d2180ed63b110）的 ARM9 地图头 encounter 索引及 `fielddata/encountdata/pl_enc_data.narc`。结构核对 pret/pokeplatinum 的 `include/overlay006/wild_encounters.h`、`src/overlay006/wild_encounters.c`。

由 `server/game/sinnoh-reference/encounters.py` 从探针导出数据生成；包含201/202/203号道路、心齐湖/湖畔、黑金关口及矿坑。沿当前 WildEncounters 引擎的有效步数/概率处理。12个陆地槽采用原版白天替换槽2、3；水面保留5槽。夜间/清晨、广播、蜂蜜树、雷达、双卡带和钓鱼分支尚未接入，本文件不声称包含这些机制。不会直接改变剧情状态或发放徽章。
