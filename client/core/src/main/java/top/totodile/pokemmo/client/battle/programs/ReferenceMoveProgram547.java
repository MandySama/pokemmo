package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 古老之歌（招式 ID：547）的攻击／事件阶段动画。
 *
 * <p>招式描述：让对手听古老之歌, 打动对手的内心进行攻击. 有时会让对手陷入睡眠状态.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110547（名称）和 120547（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/RelicSongAnimation.java
 *
 * <p>源文件 SHA-256：0ea90dfdfbbadb9810e69d7e0ae3d5f16e18448ccafda4ea95dfd6756f29215b
 */
final class ReferenceMoveProgram547 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
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
                                                                                                                                                                                                                .appendThenDelayedOuterParallel(
                                                                                                                                                                                                                        ReferenceMoveTimeline
                                                                                                                                                                                                                                .appendThenDelayedParallel(
                                                                                                                                                                                                                                        ReferenceMoveTimeline
                                                                                                                                                                                                                                                .appendThenNextParallel(
                                                                                                                                                                                                                                                        ReferenceMoveTimeline
                                                                                                                                                                                                                                                                .sequence()
                                                                                                                                                                                                                                                                .beginParallel()
                                                                                                                                                                                                                                                                .append(
                                                                                                                                                                                                                                                                        owner
                                                                                                                                                                                                                                                                                .focusCamera(
                                                                                                                                                                                                                                                                                        0,
                                                                                                                                                                                                                                                                                        0.4f)),
                                                                                                                                                                                                                                                        owner
                                                                                                                                                                                                                                                                .tintEnvironment(
                                                                                                                                                                                                                                                                        4,
                                                                                                                                                                                                                                                                        Short
                                                                                                                                                                                                                                                                                .MAX_VALUE,
                                                                                                                                                                                                                                                                        0.0f,
                                                                                                                                                                                                                                                                        0.625f,
                                                                                                                                                                                                                                                                        0.075f))
                                                                                                                                                                                                                                                .append(
                                                                                                                                                                                                                                                        owner
                                                                                                                                                                                                                                                                .scheduleSound(
                                                                                                                                                                                                                                                                        (byte)
                                                                                                                                                                                                                                                                                2,
                                                                                                                                                                                                                                                                        (short)
                                                                                                                                                                                                                                                                                1916,
                                                                                                                                                                                                                                                                        1,
                                                                                                                                                                                                                                                                        14,
                                                                                                                                                                                                                                                                        0.0f,
                                                                                                                                                                                                                                                                        0.625f,
                                                                                                                                                                                                                                                                        owner.casterHandle))
                                                                                                                                                                                                                                                .append(
                                                                                                                                                                                                                                                        owner
                                                                                                                                                                                                                                                                .adjustSoundTrack(
                                                                                                                                                                                                                                                                        1,
                                                                                                                                                                                                                                                                        1,
                                                                                                                                                                                                                                                                        1600.0f,
                                                                                                                                                                                                                                                                        1600.0f))
                                                                                                                                                                                                                                                .onStart(
                                                                                                                                                                                                                                                        owner
                                                                                                                                                                                                                                                                .preloadParticleEffect(
                                                                                                                                                                                                                                                                        714)),
                                                                                                                                                                                                                                        owner
                                                                                                                                                                                                                                                .emitParticles(
                                                                                                                                                                                                                                                        -1,
                                                                                                                                                                                                                                                        714,
                                                                                                                                                                                                                                                        5,
                                                                                                                                                                                                                                                        9,
                                                                                                                                                                                                                                                        8,
                                                                                                                                                                                                                                                        0.5f),
                                                                                                                                                                                                                                        0.12f),
                                                                                                                                                                                                                        owner
                                                                                                                                                                                                                                .emitParticles(
                                                                                                                                                                                                                                        -1,
                                                                                                                                                                                                                                        714,
                                                                                                                                                                                                                                        6,
                                                                                                                                                                                                                                        9,
                                                                                                                                                                                                                                        8,
                                                                                                                                                                                                                                        0.5f),
                                                                                                                                                                                                                        0.24f),
                                                                                                                                                                                                        owner
                                                                                                                                                                                                                .launchParticles(
                                                                                                                                                                                                                        714,
                                                                                                                                                                                                                        2,
                                                                                                                                                                                                                        9,
                                                                                                                                                                                                                        11,
                                                                                                                                                                                                                        0.5f,
                                                                                                                                                                                                                        1800.0f),
                                                                                                                                                                                                        0.36f),
                                                                                                                                                                                        owner
                                                                                                                                                                                                .launchParticles(
                                                                                                                                                                                                        714,
                                                                                                                                                                                                        3,
                                                                                                                                                                                                        9,
                                                                                                                                                                                                        11,
                                                                                                                                                                                                        0.5f,
                                                                                                                                                                                                        1800.0f),
                                                                                                                                                                                        0.48f),
                                                                                                                                                                        owner
                                                                                                                                                                                .launchParticles(
                                                                                                                                                                                        714,
                                                                                                                                                                                        4,
                                                                                                                                                                                        9,
                                                                                                                                                                                        11,
                                                                                                                                                                                        0.5f,
                                                                                                                                                                                        1800.0f),
                                                                                                                                                                        0.6f),
                                                                                                                                                        owner
                                                                                                                                                                .launchParticles(
                                                                                                                                                                        714,
                                                                                                                                                                        2,
                                                                                                                                                                        9,
                                                                                                                                                                        11,
                                                                                                                                                                        0.5f,
                                                                                                                                                                        1800.0f),
                                                                                                                                                        0.72f),
                                                                                                                                        owner
                                                                                                                                                .launchParticles(
                                                                                                                                                        714,
                                                                                                                                                        4,
                                                                                                                                                        9,
                                                                                                                                                        11,
                                                                                                                                                        0.5f,
                                                                                                                                                        1800.0f),
                                                                                                                                        0.84f),
                                                                                                                        owner
                                                                                                                                .launchParticles(
                                                                                                                                        714,
                                                                                                                                        2,
                                                                                                                                        9,
                                                                                                                                        11,
                                                                                                                                        0.5f,
                                                                                                                                        1800.0f),
                                                                                                                        0.96f)
                                                                                                                .append(
                                                                                                                        owner
                                                                                                                                .launchParticles(
                                                                                                                                        714,
                                                                                                                                        3,
                                                                                                                                        9,
                                                                                                                                        11,
                                                                                                                                        0.5f,
                                                                                                                                        1800.0f))
                                                                                                                .append(
                                                                                                                        owner
                                                                                                                                .focusCamera(
                                                                                                                                        1,
                                                                                                                                        0.6f))
                                                                                                                .endGroup()
                                                                                                                .endGroup()
                                                                                                                .beginSequence()
                                                                                                                .waitSeconds(
                                                                                                                        1.08f)
                                                                                                                .beginParallel(),
                                                                                                        owner
                                                                                                                .launchParticles(
                                                                                                                        714,
                                                                                                                        4,
                                                                                                                        9,
                                                                                                                        11,
                                                                                                                        0.5f,
                                                                                                                        1800.0f),
                                                                                                        1.2f),
                                                                                        owner
                                                                                                .launchParticles(
                                                                                                        714,
                                                                                                        2,
                                                                                                        9,
                                                                                                        11,
                                                                                                        0.5f,
                                                                                                        1800.0f),
                                                                                        1.32f),
                                                                        owner.launchParticles(
                                                                                714, 4, 9, 11, 0.5f,
                                                                                1800.0f),
                                                                        1.52f)
                                                                .append(
                                                                        owner
                                                                                .setSpriteAnimationPaused(
                                                                                        16, 1))
                                                                .endGroup()
                                                                .endGroup()
                                                                .beginSequence()
                                                                .waitSeconds(2.32f)
                                                                .beginParallel()
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1867,
                                                                                2,
                                                                                16,
                                                                                0.0f,
                                                                                0.234375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1867,
                                                                                2,
                                                                                16,
                                                                                500.0f,
                                                                                0.15625f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scaleCombatant(
                                                                                16,
                                                                                2,
                                                                                6,
                                                                                0.0f,
                                                                                0.096f,
                                                                                0.19995117f,
                                                                                -0.19995117f))
                                                                .append(
                                                                        owner.shakeCombatant(
                                                                                16,
                                                                                2,
                                                                                20,
                                                                                0.0f,
                                                                                0.032f,
                                                                                0.30004883f,
                                                                                0.0f)),
                                                        owner.emitParticles(
                                                                -1, 714, 0, 11, 8, 0.5f),
                                                        2.52f)
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 714, 0, 11, 8, 0.5f)),
                                        owner.emitParticles(-1, 714, 1, 11, 8, 0.5f),
                                        2.72f)
                                .append(owner.emitParticles(-1, 714, 1, 11, 8, 0.5f))
                                .endGroup()
                                .endGroup()
                                .endGroup()
                                .beginParallel()
                                .endGroup()
                                .beginParallel()
                                .append(owner.restoreCamera(0.25f))
                                .append(owner.setSpriteAnimationPaused(16, 0)),
                        owner.tintEnvironment(4, Short.MAX_VALUE, 0.625f, 0.0f, 0.075f));
        return result;
    }
}
