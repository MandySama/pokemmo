package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 以牙还牙（招式 ID：371）的攻击／事件阶段动画。
 *
 * <p>招式描述：蓄力攻击. 如果能在对手之后攻击, 招式的威力会变成2倍.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110371（名称）和 120371（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/PaybackAnimation.java
 *
 * <p>源文件 SHA-256：5648b37c8865b65f15bff0f8c95d28f377224ce091d7a04edc90c67f47dd2567
 */
final class ReferenceMoveProgram371 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1504,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.46875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1752,
                                                                        2,
                                                                        14,
                                                                        0.0f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1433,
                                                                        2,
                                                                        14,
                                                                        1000.0f,
                                                                        0.390625f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1433,
                                                                        1,
                                                                        14,
                                                                        1333.3334f,
                                                                        0.15625f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(546))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 546, 0, 9, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 546, 1, 9, 8, 0.5f)),
                                                owner.emitParticles(-1, 546, 2, 9, 8, 0.5f))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        83.333336f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        166.66667f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        250.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        333.33334f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(owner.emitParticles(-1, 546, 3, 11, 8, 0.5f)),
                                owner.shakeCombatant(16, 2, 8, 0.016f, 0.016f, 0.19995117f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
