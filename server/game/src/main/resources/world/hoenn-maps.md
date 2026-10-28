# 丰缘地图索引

由 `server/game/generate-hoenn.py` 从参考服务端地图生成；参考地图 bank 减 50 对齐绿宝石原生 map group。518 张地图、2776 个 NPC 坐标已与本机 BPEE rev0 ROM 核对。

数据包括地图名、原生 bank/map、尺寸、NPC 图像／坐标／移动类型／隐藏标记、背景交互和坐标事件。文本表为原生脚本文本在 ROM 中的偏移，不导出 ROM 图片或正文。来源与 ROM 哈希保存在 JSON 的 source 字段。

索引补充了逐字节核对 ROM 的地图块、从原生 tileset 读取的完整行为表，以及参考端地图连接和传送定义，供秘传技与机关校验使用。参考端 behaviorData 长度不足以直接按原生 metatile 索引，故行为表重新从 ROM 生成。

这是地图定义索引，不代表全部剧情已实现。`HoennWorld` 按开局与主线阶段投影特殊 NPC；未接入的隐藏事件不默认显示。主线进度及机关限制见 `story/hoenn/mainline.md`。
