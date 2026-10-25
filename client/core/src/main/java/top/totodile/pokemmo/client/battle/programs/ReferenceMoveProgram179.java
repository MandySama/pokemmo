package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 绝处逢生（招式 ID：179）的攻击／事件阶段动画。
 *
 * <p>招式描述：竭尽全力进行攻击. 自己的HP越少, 招式的威力越大.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110179（名称）和 120179（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/ReversalAnimation.java
 *
 * <p>源文件 SHA-256：353adf1d7ecc3d39c51d710ca255e0d32b34cf2ba2424d2c2c6f3b61dc13cb1a
 */
final class ReferenceMoveProgram179 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.appendThenNextParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenNextParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .appendThenNextParallel(
                                                                                                ReferenceMoveTimeline
                                                                                                        .appendThenNextParallel(
                                                                                                                ReferenceMoveTimeline
                                                                                                                        .appendThenNextParallel(
                                                                                                                                ReferenceMoveTimeline
                                                                                                                                        .sequence()
                                                                                                                                        .beginParallel()
                                                                                                                                        .append(
                                                                                                                                                owner
                                                                                                                                                        .tintEnvironment(
                                                                                                                                                                4,
                                                                                                                                                                31710,
                                                                                                                                                                0.0f,
                                                                                                                                                                0.75f,
                                                                                                                                                                0.05f)),
                                                                                                                                owner
                                                                                                                                        .scheduleSound(
                                                                                                                                                (byte)
                                                                                                                                                        2,
                                                                                                                                                1485,
                                                                                                                                                1,
                                                                                                                                                2,
                                                                                                                                                0.0f,
                                                                                                                                                0.78125f,
                                                                                                                                                owner.casterHandle)),
                                                                                                                owner
                                                                                                                        .tintEnvironment(
                                                                                                                                4,
                                                                                                                                31710,
                                                                                                                                0.75f,
                                                                                                                                0.0f,
                                                                                                                                0.05f))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .tintEnvironment(
                                                                                                                                4,
                                                                                                                                31710,
                                                                                                                                0.0f,
                                                                                                                                0.75f,
                                                                                                                                0.05f)),
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                1485,
                                                                                                                1,
                                                                                                                2,
                                                                                                                0.0f,
                                                                                                                0.78125f,
                                                                                                                owner.casterHandle)),
                                                                                owner
                                                                                        .tintEnvironment(
                                                                                                4,
                                                                                                31710,
                                                                                                0.75f,
                                                                                                0.0f,
                                                                                                0.05f))
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        0, 0.4f))
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                346))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 346, 0,
                                                                                        9, 8, 0.5f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1487,
                                                                                        1,
                                                                                        14,
                                                                                        0.0f,
                                                                                        0.859375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1376,
                                                                                        1,
                                                                                        14,
                                                                                        1333.3334f,
                                                                                        0.0f,
                                                                                        owner.casterHandle)),
                                                                owner.adjustSoundTrack(
                                                                        1, 1, 960.0f, 320.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1486,
                                                                        1,
                                                                        2,
                                                                        250.0f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1432,
                                                                        2,
                                                                        2,
                                                                        250.0f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.shakeCombatant(
                                                                        14, 2, 1, 0.016f, 0.096f,
                                                                        1.0f, 0.0f)),
                                                owner.tintEnvironment(4, 31710, 0.0f, 0.75f, 0.05f))
                                        .append(owner.tintEnvironment(4, 31710, 0.75f, 0.0f, 0.05f))
                                        .append(owner.focusCamera(1, 0.35f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(owner.emitParticles(-1, 346, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 346, 2, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.0f,
                                                        0.032f,
                                                        -0.30004883f,
                                                        0.30004883f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1475,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2, 1420, 2, 16, 0.0f, 0.859375f, owner.casterHandle))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
