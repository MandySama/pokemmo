package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 气味侦测（招式 ID：316）的攻击／事件阶段动画。
 *
 * <p>招式描述：对幽灵属性宝可梦没有效果的招式以及闪避率高的对手, 使用后变得能够打中.
 *
 * <p>动画说明：执行精灵位移；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110316（名称）和 120316（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/OdorSleuthAnimation.java
 *
 * <p>源文件 SHA-256：feb55e6e31d0b2deac247f8a42fdcfe8d31646ec32a6e5c31acca2db22c5936c
 */
final class ReferenceMoveProgram316 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenOuterParallel(
                                        ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1421,
                                                                1,
                                                                16,
                                                                166.66667f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1421,
                                                                1,
                                                                16,
                                                                333.33334f,
                                                                0.46875f,
                                                                owner.casterHandle))
                                                .append(owner.setSpriteAnimationPaused(14, 1))
                                                .append(
                                                        owner.shakeCombatant(
                                                                16, 3, 16, 0.0f, 0.016f, 3.0f,
                                                                0.0f))
                                                .beginSequence()
                                                .waitSeconds(0.8f)
                                                .beginParallel()
                                                .waitSeconds(0.6f),
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1452,
                                                2,
                                                14,
                                                333.33334f,
                                                0.9375f,
                                                owner.casterHandle)),
                                owner.shakeCombatant(14, 3, 1, 0.016f, 0.032f, 0.5f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
