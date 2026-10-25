package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 回旋踢（招式 ID：27）的攻击／事件阶段动画。
 *
 * <p>招式描述：一边使身体快速旋转, 一边踢飞对手进行攻击. 有时会使对手畏缩.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110027（名称）和 120027（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/RollingKickAnimation.java
 *
 * <p>源文件 SHA-256：2d5e1d855176270ae956dd2d904fd9e3326522e0a3fab410bebb46cb3a55fdf3
 */
final class ReferenceMoveProgram27 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenDelayedParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .sequence()
                                                                                        .beginParallel(),
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1421,
                                                                                        2,
                                                                                        14,
                                                                                        0.0f,
                                                                                        0.0f,
                                                                                        owner.casterHandle),
                                                                                0.04f)
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        1, 0.4f))
                                                                        .endGroup()
                                                                        .endGroup()
                                                                        .beginSequence()
                                                                        .waitSeconds(0.08f)
                                                                        .beginParallel()
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                189))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 189, 0,
                                                                                        11, 8,
                                                                                        0.5f)),
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1421,
                                                                        1,
                                                                        16,
                                                                        83.333336f,
                                                                        0.8984375f,
                                                                        owner.casterHandle),
                                                                0.12f),
                                                owner.emitParticles(-1, 189, 2, 11, 8, 0.5f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1703,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.703125f,
                                                        owner.casterHandle))
                                        .append(owner.emitParticles(-1, 189, 1, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, 0.30004883f, -0.30004883f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
