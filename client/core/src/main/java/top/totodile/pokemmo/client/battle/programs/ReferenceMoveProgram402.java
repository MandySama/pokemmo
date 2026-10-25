package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 种子炸弹（招式 ID：402）的攻击／事件阶段动画。
 *
 * <p>招式描述：将外壳坚硬的大种子, 从上方砸下攻击对手.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110402（名称）和 120402（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/SeedBombAnimation.java
 *
 * <p>源文件 SHA-256：2374885def1ec2b6c3f65002bdb106c439013b48400b920c7eae3368a1a1a728
 */
final class ReferenceMoveProgram402 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .onStart(owner.preloadParticleEffect(577))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1513,
                                                                        1,
                                                                        16,
                                                                        83.333336f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1513,
                                                                        1,
                                                                        16,
                                                                        166.66667f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1513,
                                                                        1,
                                                                        16,
                                                                        250.0f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1513,
                                                                        1,
                                                                        16,
                                                                        333.33334f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1513,
                                                                        1,
                                                                        16,
                                                                        416.66666f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1513,
                                                                        1,
                                                                        16,
                                                                        500.0f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1513,
                                                                        1,
                                                                        16,
                                                                        583.3333f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1815,
                                                                        2,
                                                                        16,
                                                                        0.0f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        2, 0, 0.0f, 640.0f))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        2, 1, 480.0f, 160.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1376,
                                                                        2,
                                                                        16,
                                                                        666.6667f,
                                                                        0.9921875f,
                                                                        owner.casterHandle)),
                                                owner.launchParticles(577, 0, 9, 11, 0.5f, 432.0f),
                                                0.1f),
                                        owner.launchParticles(577, 0, 9, 11, 0.5f, 432.0f),
                                        0.2f),
                                owner.launchParticles(577, 0, 9, 11, 0.5f, 432.0f),
                                0.3f)
                        .append(owner.launchParticles(577, 0, 9, 11, 0.5f, 432.0f))
                        .append(owner.emitParticles(-1, 577, 1, 11, 8, 0.5f))
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(0.7f)
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        1,
                                        16,
                                        33.333332f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        2,
                                        16,
                                        116.666664f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        1,
                                        16,
                                        200.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        2,
                                        16,
                                        283.33334f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        1,
                                        16,
                                        366.66666f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        2,
                                        16,
                                        450.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        1,
                                        16,
                                        533.3333f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        2,
                                        16,
                                        616.6667f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        1,
                                        16,
                                        700.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        2,
                                        16,
                                        783.3333f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        1,
                                        16,
                                        866.6667f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        2,
                                        16,
                                        950.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 6, 0.016f, 0.032f, 0.30004883f, -0.30004883f))
                        .append(owner.emitParticles(-1, 577, 1, 11, 8, 0.5f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
