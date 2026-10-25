package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 尖石攻击（招式 ID：444）的攻击／事件阶段动画。
 *
 * <p>招式描述：用尖尖的岩石刺入对手进行攻击. 容易击中要害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110444（名称）和 120444（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/StoneEdgeAnimation.java
 *
 * <p>源文件 SHA-256：462e6797dd5d35dc9dfd17c41281baa7457dea547be0991f670f5ad1b19b54a2
 */
final class ReferenceMoveProgram444 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1416,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1376,
                                                                        1,
                                                                        16,
                                                                        1166.6666f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 0, 0.0f, 640.0f))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 1, 640.0f, 480.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1775,
                                                                        2,
                                                                        16,
                                                                        666.6667f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(619))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 619, 0, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 619, 1, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 619, 2, 11, 8, 0.5f)),
                                                owner.emitParticles(-1, 619, 3, 11, 8, 0.5f),
                                                0.4f)
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 4, 0.016f, 0.032f, -0.19995117f, 0.19995117f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
