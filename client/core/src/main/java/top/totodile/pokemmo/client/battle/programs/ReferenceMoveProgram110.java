package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 缩入壳中（招式 ID：110）的攻击／事件阶段动画。
 *
 * <p>招式描述：缩入壳里保护身体, 从而提高自己的防御.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110110（名称）和 120110（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/WithdrawAnimation.java
 *
 * <p>源文件 SHA-256：d8006baaf883ed276c1d2ba599f8547c8835b4473facccdb52eace3ba2be9981
 */
final class ReferenceMoveProgram110 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        1,
                                                        14,
                                                        166.66667f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1424,
                                                        2,
                                                        14,
                                                        500.0f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1638,
                                                        2,
                                                        14,
                                                        666.6667f,
                                                        0.703125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        2,
                                                        14,
                                                        1666.6666f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 0, 640.0f, 800.0f))
                                        .append(owner.adjustSoundTrack(2, 1, 1280.0f, 320.0f))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        14,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.224f,
                                                        -0.19995117f,
                                                        -0.19995117f))
                                        .onStart(owner.preloadParticleEffect(275))
                                        .append(owner.emitParticles(-1, 275, 0, 9, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 275, 1, 9, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 275, 2, 9, 8, 0.25f)),
                                owner.emitParticles(-1, 275, 3, 9, 8, 0.25f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
