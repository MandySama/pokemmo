package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 打鼾（招式 ID：173）的攻击／事件阶段动画。
 *
 * <p>招式描述：在自己睡觉时, 发出噪音进行攻击. 有时会使对手畏缩.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110173（名称）和 120173（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/SnoreAnimation.java
 *
 * <p>源文件 SHA-256：afcbf863707721354aff7a6656bd1ff98b1f721452a86b28876bbeaa574c6c7e
 */
final class ReferenceMoveProgram173 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.6f))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1418,
                                        3,
                                        14,
                                        0.0f,
                                        0.625f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        3,
                                        14,
                                        1666.6666f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1462,
                                        1,
                                        14,
                                        0.0f,
                                        0.9765625f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1462,
                                        1,
                                        14,
                                        1000.0f,
                                        0.9765625f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(339))
                        .append(owner.emitParticles(-1, 339, 0, 9, 8, 0.4f))
                        .append(owner.emitParticles(-1, 339, 1, 9, 8, 0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, 0.19995117f, -0.19995117f))
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
