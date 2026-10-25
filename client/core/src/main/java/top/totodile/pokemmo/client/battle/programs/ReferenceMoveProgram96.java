package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 瑜伽姿势（招式 ID：96）的攻击／事件阶段动画。
 *
 * <p>招式描述：唤醒身体深处沉睡的力量, 从而提高自己的攻击.
 *
 * <p>动画说明：调整场景着色；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110096（名称）和 120096（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/MeditateAnimation.java
 *
 * <p>源文件 SHA-256：7b9f1a612b2b791a0ef5e2b30b989955b9876614f0ecd7e72820245c7f082ef4
 */
final class ReferenceMoveProgram96 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        14, 1))
                                                        .append(
                                                                owner.tintEnvironment(
                                                                        4, 0, 0.0f, 0.625f, 0.05f))
                                                        .waitSeconds(0.6f)
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1708,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle)),
                                                owner.scaleCombatant(
                                                        14,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.096f,
                                                        0.8000488f,
                                                        -0.39990234f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1708,
                                                        2,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle)),
                                owner.scaleCombatant(
                                        14, 2, 1, 0.016f, 0.096f, -0.19995117f, 0.39990234f))
                        .append(owner.scaleCombatant(14, 1, 1, 0.016f, 0.064f, 1.0f, 1.0f))
                        .append(owner.tintEnvironment(4, 0, 0.625f, 0.0f, 0.05f))
                        .waitSeconds(0.6f)
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
