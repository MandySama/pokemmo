package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 泼沙（招式 ID：28）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手脸上泼沙子, 从而降低命中率.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110028（名称）和 120028（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SandAttackAnimation.java
 *
 * <p>源文件 SHA-256：c5e8b102368b15358bbf472a06cf1ce2e11572bdfed610834c0c67b3795fca25
 */
final class ReferenceMoveProgram28 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenDelayedOuterParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .appendThenDelayedParallel(
                                                                                                ReferenceMoveTimeline
                                                                                                        .waitThenParallel(
                                                                                                                0.6f),
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                1421,
                                                                                                                1,
                                                                                                                14,
                                                                                                                0.0f,
                                                                                                                0.8984375f,
                                                                                                                owner.casterHandle),
                                                                                                0.12f)
                                                                                        .onStart(
                                                                                                owner
                                                                                                        .preloadParticleEffect(
                                                                                                                190))
                                                                                        .append(
                                                                                                owner
                                                                                                        .launchParticles(
                                                                                                                190,
                                                                                                                2,
                                                                                                                9,
                                                                                                                11,
                                                                                                                0.5f,
                                                                                                                120.0f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .launchParticles(
                                                                                                                190,
                                                                                                                3,
                                                                                                                9,
                                                                                                                11,
                                                                                                                0.5f,
                                                                                                                120.0f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                1446,
                                                                                                                1,
                                                                                                                16,
                                                                                                                0.0f,
                                                                                                                0.8984375f,
                                                                                                                owner.casterHandle)),
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1447,
                                                                                        2,
                                                                                        16,
                                                                                        0.0f,
                                                                                        0.78125f,
                                                                                        owner.casterHandle),
                                                                                0.72f)
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        1, 0.4f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 190, 0,
                                                                                        11, 8,
                                                                                        0.5f)),
                                                                owner.emitParticles(
                                                                        -1, 190, 1, 11, 8, 0.5f),
                                                                0.92f)
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 190, 0, 11, 8, 0.5f)),
                                                owner.emitParticles(-1, 190, 1, 11, 8, 0.5f),
                                                1.12f)
                                        .append(owner.emitParticles(-1, 190, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 190, 1, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        3,
                                                        0.0f,
                                                        0.032f,
                                                        0.100097656f,
                                                        -0.100097656f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1446,
                                                        1,
                                                        16,
                                                        83.333336f,
                                                        0.703125f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        1420,
                                        2,
                                        16,
                                        83.333336f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
