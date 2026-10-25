package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 换挡（招式 ID：508）的攻击／事件阶段动画。
 *
 * <p>招式描述：转动齿轮, 不仅提高自己的攻击, 还会大幅提高速度.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110508（名称）和 120508（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/ShiftGearAnimation.java
 *
 * <p>源文件 SHA-256：f74a0b985e50718611c5af95426f905dbd0eeca838e086790537c427020f764e
 */
final class ReferenceMoveProgram508 {

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
                                                                        (short) 1448,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1448,
                                                                        2,
                                                                        14,
                                                                        83.333336f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1678,
                                                                        1,
                                                                        14,
                                                                        666.6667f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1654,
                                                                        2,
                                                                        14,
                                                                        666.6667f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 1, 1280.0f, 160.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1376,
                                                                        1,
                                                                        14,
                                                                        1500.0f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(673))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 673, 6, 9, 8, 0.5f)),
                                                owner.emitParticles(-1, 673, 7, 9, 8, 0.5f),
                                                0.2f)
                                        .append(owner.emitParticles(-1, 673, 0, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 673, 1, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 673, 2, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 673, 3, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 673, 4, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 673, 5, 9, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(0.92f)
                                        .beginParallel(),
                                owner.scaleCombatant(
                                        14, 2, 12, 0.0f, 0.032f, 0.100097656f, -0.100097656f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
