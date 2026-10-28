# 真新镇开场剧情

对应 `opening.json`。完整触发条件与节点说明见 [opening.README.md](opening.README.md)。

## 首次劲敌战胜败与战后衔接

- 玩家选初始宝可梦后进入阶段 3，出口触发或与劲敌交谈可发起首次战斗。
- 允许失败。战后无论胜败都恢复队伍 HP、状态和 PP；胜利奖励 80，失败不发放胜利奖金。
- 结算时保存待播 `battle_complete`，保持阶段 3 和劲敌可见。该记录同时防止重复结算、重复发奖；重登可继续战后剧情。
- 战斗关闭后：劲敌告别对白（原生文本 1629752）→ 劲敌走向研究所出口 `(6,12)` → 阶段 3 改为 4 → 隐藏劲敌、结束剧情。不会在对白前直接隐藏。
- 胜败共用这段场景内告别；原版来源为 `PalletTown_ProfessorOaksLab_EventScript_EndRivalBattle`，与战斗中的胜败结算文案不同。

来源：参考服务端 `PalletStoryBattle.settle` 的允许战败与治疗规则；[原生地图脚本](https://github.com/pret/pokefirered/blob/master/data/maps/PalletTown_ProfessorOaksLab/scripts.inc) 的战后对白和离场顺序。参考服务端开场的 `battle_complete` 只有通知，缺少这段原生衔接，当前已补入。

回归：`KantoStoryTest` 覆盖真实认输后重登继续；`OpeningBattleContinuationTest` 覆盖胜败、治疗、重复结算和完整离场；UI 用例验证对白播放与离场前后可见性。
