package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 火箭头锤（招式 ID：130）的蓄力阶段动画。
 *
 * <p>招式描述：第1回合把头缩进去, 从而提高防御. 第2回合攻击对手.
 *
 * <p>动画说明：镜头聚焦施术者；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110130（名称）和 120130（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SkullBashAnimation.java
 *
 * <p>源文件 SHA-256：309c5bec7ac2cbb40b9699b40a52ad41fc0aa6181728a25f6eddd29c205d13c9
 */
final class ReferenceChargeProgram130 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .append(
                                                                owner.shakeCombatant(
                                                                        14, 1, 1, 0.016f, 0.096f,
                                                                        -1.0f, 0.0f)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1421,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .endGroup()
                                        .beginParallel()
                                        .beginSequence()
                                        .waitSeconds(0.6f)
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1524,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        1524,
                                        1,
                                        14,
                                        666.6667f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(owner.shakeCombatant(14, 1, 1, 0.0f, 0.096f, 1.0f, 0.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1421, 1, 14, 0.0f, 0.859375f, owner.casterHandle))
                        .endGroup()
                        .beginParallel()
                        .endGroup()
                        .beginParallel()
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
