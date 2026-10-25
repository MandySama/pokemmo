package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 能量球（招式 ID：412）的攻击／事件阶段动画。
 *
 * <p>招式描述：发射从自然收集的生命力量. 有时会降低对手的特防.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110412（名称）和 120412（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/EnergyBallAnimation.java
 *
 * <p>源文件 SHA-256：62f0e81b6d72f518c19d34fb7479792a2ea4a1d99998d33b0a15c50e2af306ee
 */
final class ReferenceMoveProgram412 {

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
                                                                                        0, 0.4f))
                                                                        .append(
                                                                                owner
                                                                                        .tintEnvironment(
                                                                                                4,
                                                                                                0,
                                                                                                0.0f,
                                                                                                0.75f,
                                                                                                0.05f))
                                                                        .append(
                                                                                owner
                                                                                        .setSpriteAnimationPaused(
                                                                                                14,
                                                                                                1))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1713,
                                                                                        1,
                                                                                        14,
                                                                                        0.0f,
                                                                                        0.9375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1376,
                                                                                        1,
                                                                                        14,
                                                                                        1416.6666f,
                                                                                        0.0f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                0,
                                                                                                0.0f,
                                                                                                1360.0f))
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                587)),
                                                                owner.emitParticles(
                                                                        -1, 587, 1, 9, 8, 0.5f),
                                                                1.6f)
                                                        .append(owner.focusCamera(1, 0.8f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1510,
                                                                        2,
                                                                        16,
                                                                        0.0f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1423,
                                                                        1,
                                                                        16,
                                                                        583.3333f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1423,
                                                                        2,
                                                                        16,
                                                                        666.6667f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1423,
                                                                        1,
                                                                        16,
                                                                        750.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1423,
                                                                        2,
                                                                        16,
                                                                        833.3333f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 1, 640.0f, 480.0f)),
                                                owner.emitParticles(-1, 587, 0, 9, 11, 0.5f),
                                                2.0f)
                                        .append(owner.emitParticles(-1, 587, 2, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
