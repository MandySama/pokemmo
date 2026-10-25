package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 下拽（自定义动画注册 ID：1014）的蓄力阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵显隐；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111014（名称）和 121014（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1014Animation.java
 *
 * <p>源文件 SHA-256：72bad0277bd43ec512c50ad1540ad6467c23a059fa2c9b6ec59d997604b333a2
 */
final class ReferenceChargeProgram1014 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1475,
                                                                                        1,
                                                                                        16,
                                                                                        83.333336f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1416,
                                                                                        1,
                                                                                        14,
                                                                                        1083.3334f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1376,
                                                                                        1,
                                                                                        14,
                                                                                        1666.6666f,
                                                                                        0.0f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                1,
                                                                                                1280.0f,
                                                                                                304.0f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1416,
                                                                                        1,
                                                                                        14,
                                                                                        1750.0f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1376,
                                                                                        1,
                                                                                        14,
                                                                                        2416.6667f,
                                                                                        0.0f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                1,
                                                                                                1920.0f,
                                                                                                384.0f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1416,
                                                                                        1,
                                                                                        14,
                                                                                        2416.6667f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1376,
                                                                                        1,
                                                                                        14,
                                                                                        3250.0f,
                                                                                        0.0f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                1,
                                                                                                2560.0f,
                                                                                                560.0f))
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        1, 0.4f))
                                                                        .append(
                                                                                owner
                                                                                        .setSpriteAnimationPaused(
                                                                                                16,
                                                                                                1))
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                260))
                                                                        .endGroup()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner
                                                                                        .setSpriteAnimationPaused(
                                                                                                16,
                                                                                                0))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 260, 1,
                                                                                        9, 8, 0.5f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 260, 2,
                                                                                        9, 8,
                                                                                        0.25f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 260, 3,
                                                                                        9, 8,
                                                                                        0.25f)),
                                                                owner.emitParticles(
                                                                        -1, 260, 4, 9, 8, 0.0f),
                                                                0.4f)
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 260, 4, 9, 8, 0.0f)),
                                                owner.shakeCombatant(
                                                        14, 2, 1, 0.016f, 0.256f, 0.0f, -6.0f),
                                                0.8f)
                                        .append(owner.setCombatantVisibility(14, 4)),
                                owner.emitParticles(-1, 260, 4, 9, 8, 0.0f),
                                1.8f)
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
