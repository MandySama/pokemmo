package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 毒瓦斯（招式 ID：139）的攻击／事件阶段动画。
 *
 * <p>招式描述：将毒瓦斯吹到对手的脸上, 从而让对手陷入中毒状态.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110139（名称）和 120139（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/PoisonGasAnimation.java
 *
 * <p>源文件 SHA-256：2870d35258a3d23fe2fd7ea3b7b3505fdc5596fb5a7573fff0de99ead088191a
 */
final class ReferenceMoveProgram139 {

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
                                                                                                                                                                                                                        .append(
                                                                                                                                                                                                                                owner
                                                                                                                                                                                                                                        .scheduleSound(
                                                                                                                                                                                                                                                (byte)
                                                                                                                                                                                                                                                        2,
                                                                                                                                                                                                                                                (short)
                                                                                                                                                                                                                                                        1407,
                                                                                                                                                                                                                                                2,
                                                                                                                                                                                                                                                14,
                                                                                                                                                                                                                                                0.0f,
                                                                                                                                                                                                                                                0.78125f,
                                                                                                                                                                                                                                                owner.casterHandle))
                                                                                                                                                                                                                        .append(
                                                                                                                                                                                                                                owner
                                                                                                                                                                                                                                        .scheduleSound(
                                                                                                                                                                                                                                                (byte)
                                                                                                                                                                                                                                                        2,
                                                                                                                                                                                                                                                (short)
                                                                                                                                                                                                                                                        1376,
                                                                                                                                                                                                                                                2,
                                                                                                                                                                                                                                                14,
                                                                                                                                                                                                                                                1333.3334f,
                                                                                                                                                                                                                                                0.0f,
                                                                                                                                                                                                                                                owner.casterHandle))
                                                                                                                                                                                                                        .append(
                                                                                                                                                                                                                                owner
                                                                                                                                                                                                                                        .adjustSoundTrack(
                                                                                                                                                                                                                                                2,
                                                                                                                                                                                                                                                1,
                                                                                                                                                                                                                                                800.0f,
                                                                                                                                                                                                                                                480.0f))
                                                                                                                                                                                                                        .onStart(
                                                                                                                                                                                                                                owner
                                                                                                                                                                                                                                        .preloadParticleEffect(
                                                                                                                                                                                                                                                304)),
                                                                                                                                                                                                                owner
                                                                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                                                                304,
                                                                                                                                                                                                                                0,
                                                                                                                                                                                                                                9,
                                                                                                                                                                                                                                11,
                                                                                                                                                                                                                                0.5f,
                                                                                                                                                                                                                                1200.0f),
                                                                                                                                                                                                                0.04f),
                                                                                                                                                                                                owner
                                                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                                                304,
                                                                                                                                                                                                                0,
                                                                                                                                                                                                                9,
                                                                                                                                                                                                                11,
                                                                                                                                                                                                                0.5f,
                                                                                                                                                                                                                1200.0f),
                                                                                                                                                                                                0.08f),
                                                                                                                                                                                owner
                                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                                304,
                                                                                                                                                                                                0,
                                                                                                                                                                                                9,
                                                                                                                                                                                                11,
                                                                                                                                                                                                0.5f,
                                                                                                                                                                                                1200.0f),
                                                                                                                                                                                0.12f),
                                                                                                                                                                owner
                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                304,
                                                                                                                                                                                0,
                                                                                                                                                                                9,
                                                                                                                                                                                11,
                                                                                                                                                                                0.5f,
                                                                                                                                                                                1200.0f),
                                                                                                                                                                0.16f),
                                                                                                                                                owner
                                                                                                                                                        .launchParticles(
                                                                                                                                                                304,
                                                                                                                                                                0,
                                                                                                                                                                9,
                                                                                                                                                                11,
                                                                                                                                                                0.5f,
                                                                                                                                                                1200.0f),
                                                                                                                                                0.2f),
                                                                                                                                owner
                                                                                                                                        .launchParticles(
                                                                                                                                                304,
                                                                                                                                                0,
                                                                                                                                                9,
                                                                                                                                                11,
                                                                                                                                                0.5f,
                                                                                                                                                1200.0f),
                                                                                                                                0.24f),
                                                                                                                owner
                                                                                                                        .launchParticles(
                                                                                                                                304,
                                                                                                                                0,
                                                                                                                                9,
                                                                                                                                11,
                                                                                                                                0.5f,
                                                                                                                                1200.0f),
                                                                                                                0.28f),
                                                                                                owner
                                                                                                        .launchParticles(
                                                                                                                304,
                                                                                                                0,
                                                                                                                9,
                                                                                                                11,
                                                                                                                0.5f,
                                                                                                                1200.0f),
                                                                                                0.32f),
                                                                                owner
                                                                                        .launchParticles(
                                                                                                304,
                                                                                                0,
                                                                                                9,
                                                                                                11,
                                                                                                0.5f,
                                                                                                1200.0f),
                                                                                0.36f)
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        1, 0.6f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1455,
                                                                                        1,
                                                                                        16,
                                                                                        833.3333f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                1,
                                                                                                1440.0f,
                                                                                                480.0f)),
                                                                owner.launchParticles(
                                                                        304, 0, 9, 11, 0.5f,
                                                                        1200.0f),
                                                                0.4f),
                                                owner.launchParticles(304, 0, 9, 11, 0.5f, 1200.0f),
                                                0.64f)
                                        .append(owner.emitParticles(-1, 304, 1, 11, 8, 0.5f))
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
