package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 魔法叶（招式 ID：345）的攻击／事件阶段动画。
 *
 * <p>招式描述：散落可以追踪对手的神奇叶片. 攻击必定会命中.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110345（名称）和 120345（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/MagicalLeafAnimation.java
 *
 * <p>源文件 SHA-256：2ff5995a5af739e01b65146eff89f49f9b23d68e2640bfce34d039c88422d758
 */
final class ReferenceMoveProgram345 {

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
                                                                        (short) 1413,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1412,
                                                                        2,
                                                                        14,
                                                                        166.66667f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1414,
                                                                        1,
                                                                        14,
                                                                        333.33334f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1376,
                                                                        2,
                                                                        14,
                                                                        1166.6666f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        2, 1, 480.0f, 640.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1415,
                                                                        1,
                                                                        16,
                                                                        1166.6666f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1415,
                                                                        2,
                                                                        16,
                                                                        1416.6666f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1415,
                                                                        1,
                                                                        16,
                                                                        1500.0f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(518))
                                                        .append(
                                                                owner.launchParticles(
                                                                        518, 0, 9, 11, 0.5f, 0.0f)),
                                                owner.emitParticles(-1, 518, 1, 9, 8, 0.5f),
                                                1.6f)
                                        .append(owner.focusCamera(1, 0.4f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        83.333336f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        1,
                                                        16,
                                                        166.66667f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        1,
                                                        16,
                                                        250.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(owner.emitParticles(-1, 518, 2, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 4, 0.016f, 0.032f, -0.19995117f, 0.19995117f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
