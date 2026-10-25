package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 原始之力（招式 ID：246）的攻击／事件阶段动画。
 *
 * <p>招式描述：用原始之力进行攻击. 有时会提高自己所有的能力.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110246（名称）和 120246（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/AncientPowerAnimation.java
 *
 * <p>源文件 SHA-256：e39013ff62409a2c81dd57b6b12d37267442fbebca640e5a07ac44ccb0fa7502
 */
final class ReferenceMoveProgram246 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1434,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1425,
                                                                        2,
                                                                        14,
                                                                        0.0f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1376,
                                                                        2,
                                                                        14,
                                                                        833.3333f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1416,
                                                                        2,
                                                                        16,
                                                                        916.6667f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        2, 0, 480.0f, 800.0f))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        2, 1, 1600.0f, 800.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1475,
                                                                        1,
                                                                        16,
                                                                        1716.6666f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1488,
                                                                        2,
                                                                        16,
                                                                        1716.6666f,
                                                                        0.703125f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(413))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 413, 3, 9, 8, 0.0f)),
                                                owner.launchParticles(413, 2, 9, 11, 0.5f, 0.0f),
                                                1.6f)
                                        .append(owner.focusCamera(1, 0.3f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(1.84f)
                                        .beginParallel()
                                        .append(owner.emitParticles(-1, 413, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 413, 0, 11, 8, 0.5f)),
                                owner.scaleCombatant(
                                        16, 2, 3, 0.0f, 0.064f, 0.19995117f, -0.19995117f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
