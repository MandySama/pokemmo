package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 忍耐（招式 ID：117）的攻击／事件阶段动画。
 *
 * <p>招式描述：在2回合内忍受攻击, 受到的伤害会2倍返还给对手.
 *
 * <p>动画说明：按参考源码构建此事件的时间线。
 *
 * <p>编号对应参考动画注册表；语言条目为 110117（名称）和 120117（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/BideAnimation.java
 *
 * <p>源文件 SHA-256：58550ce4dc4e752e3a4f3d8c9132eccc2eb442c190b7412cbff8b9e0afb2da71
 */
final class ReferenceMoveProgram117 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result = ReferenceMoveTimeline.sequence().beginParallel().endGroup();
        return result;
    }
}
