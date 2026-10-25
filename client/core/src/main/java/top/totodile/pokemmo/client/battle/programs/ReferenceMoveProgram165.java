package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 挣扎（招式 ID：165）的攻击／事件阶段动画。
 *
 * <p>招式描述：耗光所有PP后的绝望一击. PvE对战中, 使用次数越多反伤越强.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110165（名称）和 120165（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/StruggleAnimation.java
 *
 * <p>源文件 SHA-256：d455db59db2cb4b3b4ede2cff25b3ba66eb354bc0375acbcd2e1e3def78f12fd
 */
final class ReferenceMoveProgram165 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.appendThenNextParallel(
                                                                ReferenceMoveTimeline
                                                                        .waitThenParallel(0.6f)
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1463,
                                                                                        1,
                                                                                        14,
                                                                                        0.0f,
                                                                                        0.390625f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1376,
                                                                                        1,
                                                                                        14,
                                                                                        333.33334f,
                                                                                        0.0f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1463,
                                                                                        1,
                                                                                        14,
                                                                                        500.0f,
                                                                                        0.390625f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1376,
                                                                                        1,
                                                                                        14,
                                                                                        833.3333f,
                                                                                        0.0f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                0,
                                                                                                0.0f,
                                                                                                160.0f))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                0,
                                                                                                160.0f,
                                                                                                160.0f))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                0,
                                                                                                480.0f,
                                                                                                160.0f))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                0,
                                                                                                640.0f,
                                                                                                160.0f)),
                                                                owner.shakeCombatant(
                                                                        14, 2, 2, 0.016f, 0.096f,
                                                                        1.0f, 0.0f))
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .onStart(owner.preloadParticleEffect(331))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 331, 0, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 331, 1, 11, 8, 0.5f))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        14, 1)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1420,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle),
                                                0.4f)
                                        .append(owner.emitParticles(-1, 331, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 331, 1, 11, 8, 0.5f)),
                                owner.scheduleSound(
                                        (byte) 2, 1420, 2, 16, 0.0f, 0.9375f, owner.casterHandle))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
