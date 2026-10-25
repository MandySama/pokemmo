package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 污泥波（招式 ID：482）的攻击／事件阶段动画。
 *
 * <p>招式描述：用污泥波攻击自己周围所有的宝可梦. 有时会陷入中毒状态.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110482（名称）和 120482（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/SludgeWaveAnimation.java
 *
 * <p>源文件 SHA-256：15740d7122fadb795034f4d0889539073cec8087bc30d5e7144c6f7957d56362
 */
final class ReferenceMoveProgram482 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenDelayedOuterParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .appendThenDelayedOuterParallel(
                                                                                                ReferenceMoveTimeline
                                                                                                        .appendThenDelayedOuterParallel(
                                                                                                                ReferenceMoveTimeline
                                                                                                                        .appendThenDelayedOuterParallel(
                                                                                                                                ReferenceMoveTimeline
                                                                                                                                        .appendThenDelayedOuterParallel(
                                                                                                                                                ReferenceMoveTimeline
                                                                                                                                                        .appendThenDelayedOuterParallel(
                                                                                                                                                                ReferenceMoveTimeline
                                                                                                                                                                        .appendThenDelayedOuterParallel(
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
                                                                                                                                                                                                                                                0,
                                                                                                                                                                                                                                                0.3f))
                                                                                                                                                                                                                        .endGroup()
                                                                                                                                                                                                                        .beginParallel()
                                                                                                                                                                                                                        .onStart(
                                                                                                                                                                                                                                owner
                                                                                                                                                                                                                                        .preloadParticleEffect(
                                                                                                                                                                                                                                                653))
                                                                                                                                                                                                                        .append(
                                                                                                                                                                                                                                owner
                                                                                                                                                                                                                                        .scheduleSound(
                                                                                                                                                                                                                                                (byte)
                                                                                                                                                                                                                                                        2,
                                                                                                                                                                                                                                                (short)
                                                                                                                                                                                                                                                        1480,
                                                                                                                                                                                                                                                1,
                                                                                                                                                                                                                                                14,
                                                                                                                                                                                                                                                0.0f,
                                                                                                                                                                                                                                                0.546875f,
                                                                                                                                                                                                                                                owner.casterHandle))
                                                                                                                                                                                                                        .append(
                                                                                                                                                                                                                                owner
                                                                                                                                                                                                                                        .scheduleSound(
                                                                                                                                                                                                                                                (byte)
                                                                                                                                                                                                                                                        2,
                                                                                                                                                                                                                                                (short)
                                                                                                                                                                                                                                                        1863,
                                                                                                                                                                                                                                                2,
                                                                                                                                                                                                                                                14,
                                                                                                                                                                                                                                                0.0f,
                                                                                                                                                                                                                                                0.390625f,
                                                                                                                                                                                                                                                owner.casterHandle))
                                                                                                                                                                                                                        .append(
                                                                                                                                                                                                                                owner
                                                                                                                                                                                                                                        .adjustSoundTrack(
                                                                                                                                                                                                                                                1,
                                                                                                                                                                                                                                                0,
                                                                                                                                                                                                                                                0.0f,
                                                                                                                                                                                                                                                1280.0f))
                                                                                                                                                                                                                        .append(
                                                                                                                                                                                                                                owner
                                                                                                                                                                                                                                        .adjustSoundTrack(
                                                                                                                                                                                                                                                1,
                                                                                                                                                                                                                                                1,
                                                                                                                                                                                                                                                640.0f,
                                                                                                                                                                                                                                                640.0f))
                                                                                                                                                                                                                        .append(
                                                                                                                                                                                                                                owner
                                                                                                                                                                                                                                        .adjustSoundTrack(
                                                                                                                                                                                                                                                2,
                                                                                                                                                                                                                                                1,
                                                                                                                                                                                                                                                640.0f,
                                                                                                                                                                                                                                                640.0f)),
                                                                                                                                                                                                                owner
                                                                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                                                                653,
                                                                                                                                                                                                                                2,
                                                                                                                                                                                                                                9,
                                                                                                                                                                                                                                11,
                                                                                                                                                                                                                                0.5f,
                                                                                                                                                                                                                                1200.0f),
                                                                                                                                                                                                                0.02f),
                                                                                                                                                                                                owner
                                                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                                                653,
                                                                                                                                                                                                                2,
                                                                                                                                                                                                                9,
                                                                                                                                                                                                                11,
                                                                                                                                                                                                                0.5f,
                                                                                                                                                                                                                1200.0f),
                                                                                                                                                                                                0.04f),
                                                                                                                                                                                owner
                                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                                653,
                                                                                                                                                                                                2,
                                                                                                                                                                                                9,
                                                                                                                                                                                                11,
                                                                                                                                                                                                0.5f,
                                                                                                                                                                                                1200.0f),
                                                                                                                                                                                0.06f),
                                                                                                                                                                owner
                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                653,
                                                                                                                                                                                2,
                                                                                                                                                                                9,
                                                                                                                                                                                11,
                                                                                                                                                                                0.5f,
                                                                                                                                                                                1200.0f),
                                                                                                                                                                0.08f),
                                                                                                                                                owner
                                                                                                                                                        .launchParticles(
                                                                                                                                                                653,
                                                                                                                                                                2,
                                                                                                                                                                9,
                                                                                                                                                                11,
                                                                                                                                                                0.5f,
                                                                                                                                                                1200.0f),
                                                                                                                                                0.1f),
                                                                                                                                owner
                                                                                                                                        .launchParticles(
                                                                                                                                                653,
                                                                                                                                                2,
                                                                                                                                                9,
                                                                                                                                                11,
                                                                                                                                                0.5f,
                                                                                                                                                1200.0f),
                                                                                                                                0.12f),
                                                                                                                owner
                                                                                                                        .launchParticles(
                                                                                                                                653,
                                                                                                                                2,
                                                                                                                                9,
                                                                                                                                11,
                                                                                                                                0.5f,
                                                                                                                                1200.0f),
                                                                                                                0.14f),
                                                                                                owner
                                                                                                        .launchParticles(
                                                                                                                653,
                                                                                                                2,
                                                                                                                9,
                                                                                                                11,
                                                                                                                0.5f,
                                                                                                                1200.0f),
                                                                                                0.16f),
                                                                                owner
                                                                                        .launchParticles(
                                                                                                653,
                                                                                                2,
                                                                                                9,
                                                                                                11,
                                                                                                0.5f,
                                                                                                1200.0f),
                                                                                0.18f),
                                                                owner.launchParticles(
                                                                        653, 2, 9, 11, 0.5f,
                                                                        1200.0f),
                                                                0.2f),
                                                owner.launchParticles(653, 2, 9, 11, 0.5f, 1200.0f),
                                                0.42f)
                                        .append(owner.focusCamera(1, 0.4f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .endGroup()
                                        .endGroup()
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1536,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1887,
                                                        2,
                                                        16,
                                                        166.66667f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.emitParticles(-1, 653, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 653, 1, 11, 8, 0.5f)),
                                owner.scaleCombatant(
                                        16, 2, 4, 0.016f, 0.032f, 0.100097656f, -0.100097656f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
