package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 鹦鹉学舌（招式 ID：119）的攻击／事件阶段动画。
 *
 * <p>招式描述：使用自己最后受到的技能来攻击目标. 但只能模仿对自己造成了伤害的技能.
 *
 * <p>动画说明：按参考源码构建此事件的时间线。
 *
 * <p>编号对应参考动画注册表；语言条目为 110119（名称）和 120119（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/MirrorMoveAnimation.java
 *
 * <p>源文件 SHA-256：b9b0cb7b69855b65e3f8aa38a18dc25cb189c3f7670930da81fbe8b14c9484fd
 */
final class ReferenceMoveProgram119 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result = ReferenceMoveTimeline.sequence().beginParallel().endGroup();
        return result;
    }
}
