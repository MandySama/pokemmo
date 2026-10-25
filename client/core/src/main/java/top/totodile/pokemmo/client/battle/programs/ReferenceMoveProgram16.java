package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 起风（招式 ID：16）的攻击／事件阶段动画。
 *
 * <p>招式描述：用翅膀将刮起的狂风袭向对手进行攻击.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110016（名称）和 120016（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/GustAnimation.java
 *
 * <p>源文件 SHA-256：f57b7c180c9f38c7e1c550aefd9aaacc364f5b15081a3a9965253bee0d76f4e6
 */
final class ReferenceMoveProgram16 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        1, 0.4f))
                                                                        .endGroup()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1497,
                                                                                        1,
                                                                                        16,
                                                                                        166.66667f,
                                                                                        0.8984375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1376,
                                                                                        1,
                                                                                        16,
                                                                                        2000.0f,
                                                                                        0.0f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                0,
                                                                                                160.0f,
                                                                                                1280.0f))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                1,
                                                                                                960.0f,
                                                                                                960.0f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1717,
                                                                                        2,
                                                                                        16,
                                                                                        166.66667f,
                                                                                        0.78125f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1376,
                                                                                        2,
                                                                                        16,
                                                                                        2000.0f,
                                                                                        0.0f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                2,
                                                                                                0,
                                                                                                0.0f,
                                                                                                640.0f))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                2,
                                                                                                0,
                                                                                                960.0f,
                                                                                                640.0f))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                2,
                                                                                                1,
                                                                                                0.0f,
                                                                                                480.0f))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                2,
                                                                                                1,
                                                                                                960.0f,
                                                                                                960.0f))
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                177))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 177, 0,
                                                                                        11, 8,
                                                                                        0.25f)),
                                                                owner.emitParticles(
                                                                        -1, 177, 1, 11, 8, 0.5f),
                                                                0.4f)
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 177, 1, 11, 8, 0.25f)),
                                                owner.emitParticles(-1, 177, 0, 11, 8, 0.5f),
                                                0.8f)
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.shakeCombatant(16, 2, 6, 0.016f, 0.048f, 1.0f, 0.0f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
