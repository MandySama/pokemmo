package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 岩崩（招式 ID：157）的攻击／事件阶段动画。
 *
 * <p>招式描述：将大岩石猛烈地撞向对手进行攻击. 有时会使对手畏缩.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110157（名称）和 120157（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/RockSlideAnimation.java
 *
 * <p>源文件 SHA-256：20ac1a8bbbb2ec589f721a8082d6e4fd8c56432573227f30b5e22bbec52759cc
 */
final class ReferenceMoveProgram157 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenDelayedParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .sequence()
                                                                                        .beginParallel()
                                                                                        .append(
                                                                                                owner
                                                                                                        .focusCamera(
                                                                                                                1,
                                                                                                                0.4f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                1417,
                                                                                                                1,
                                                                                                                16,
                                                                                                                0.0f,
                                                                                                                0.9375f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                1416,
                                                                                                                0,
                                                                                                                16,
                                                                                                                0.0f,
                                                                                                                0.546875f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                1376,
                                                                                                                1,
                                                                                                                16,
                                                                                                                2333.3333f,
                                                                                                                0.0f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                1376,
                                                                                                                0,
                                                                                                                16,
                                                                                                                2333.3333f,
                                                                                                                0.0f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .adjustSoundTrack(
                                                                                                                0,
                                                                                                                1,
                                                                                                                1600.0f,
                                                                                                                640.0f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .adjustSoundTrack(
                                                                                                                1,
                                                                                                                1,
                                                                                                                1600.0f,
                                                                                                                640.0f))
                                                                                        .onStart(
                                                                                                owner
                                                                                                        .setHealthHudHidden(
                                                                                                                18,
                                                                                                                true))
                                                                                        .endGroup()
                                                                                        .beginParallel()
                                                                                        .onStart(
                                                                                                owner
                                                                                                        .preloadParticleEffect(
                                                                                                                321)),
                                                                                owner.emitParticles(
                                                                                        -1, 321, 1,
                                                                                        11, 8,
                                                                                        0.5f),
                                                                                0.12f)
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 321, 1,
                                                                                        11, 8,
                                                                                        0.5f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 321, 2,
                                                                                        11, 8,
                                                                                        0.25f)),
                                                                owner.emitParticles(
                                                                        -1, 321, 0, 11, 8, 0.25f),
                                                                0.24f)
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 321, 1, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 321, 2, 11, 8, 0.25f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 321, 0, 11, 8, 0.25f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1475,
                                                                        2,
                                                                        16,
                                                                        0.0f,
                                                                        0.703125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1475,
                                                                        2,
                                                                        16,
                                                                        333.33334f,
                                                                        0.703125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1475,
                                                                        2,
                                                                        16,
                                                                        500.0f,
                                                                        0.703125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1475,
                                                                        2,
                                                                        16,
                                                                        666.6667f,
                                                                        0.703125f,
                                                                        owner.casterHandle)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1475,
                                                        2,
                                                        16,
                                                        833.3333f,
                                                        0.703125f,
                                                        owner.casterHandle),
                                                0.64f)
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 6, 0.016f, 0.032f, 0.19995117f, -0.19995117f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
