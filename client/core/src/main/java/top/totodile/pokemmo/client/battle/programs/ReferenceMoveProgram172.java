package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 火焰轮（招式 ID：172）的攻击／事件阶段动画。
 *
 * <p>招式描述：让火焰覆盖全身, 猛撞向对手进行攻击. 有时会让对手陷入灼伤状态.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110172（名称）和 120172（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/FlameWheelAnimation.java
 *
 * <p>源文件 SHA-256：4fe41973c0aa16216a1fb1ee205b59be57aa06e7219718488fe2f3d68f7f2342
 */
final class ReferenceMoveProgram172 {

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
                                                        .onStart(owner.preloadParticleEffect(338))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 338, 0, 9, 8, 0.5f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1425,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1535,
                                                                        2,
                                                                        14,
                                                                        0.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1376,
                                                                        1,
                                                                        14,
                                                                        916.6667f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1535,
                                                                        2,
                                                                        14,
                                                                        500.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 1, 560.0f, 320.0f)),
                                                owner.adjustSoundTrack(2, 1, 560.0f, 320.0f),
                                                0.8f)
                                        .append(
                                                owner.shakeCombatant(
                                                        14, 2, 1, 0.032f, 0.064f, 2.0f, 1.0f))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(1.04f)
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1426,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1885,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.emitParticles(-1, 338, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 338, 2, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, 0.19995117f, -0.19995117f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
