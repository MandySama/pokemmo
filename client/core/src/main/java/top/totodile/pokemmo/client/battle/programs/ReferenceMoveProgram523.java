package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 重踏（招式 ID：523）的攻击／事件阶段动画。
 *
 * <p>招式描述：用力踩踏地面并攻击自己周围所有的宝可梦. 降低对方的速度.
 *
 * <p>动画说明：执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110523（名称）和 120523（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/BulldozeAnimation.java
 *
 * <p>源文件 SHA-256：3040f7301d148de0534bfced558fddf461a1d21fb2ad5508719a7462c8ef9622
 */
final class ReferenceMoveProgram523 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                        .append(
                                                owner.scaleCombatant(
                                                        14,
                                                        2,
                                                        20,
                                                        0.0f,
                                                        0.032f,
                                                        0.19995117f,
                                                        -0.19995117f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1876,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.546875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1652,
                                                        2,
                                                        14,
                                                        583.3333f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1652,
                                                        2,
                                                        14,
                                                        750.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1652,
                                                        2,
                                                        14,
                                                        916.6667f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1748,
                                                        3,
                                                        14,
                                                        0.0f,
                                                        0.46875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        3,
                                                        14,
                                                        1333.3334f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .beginSequence()
                                        .waitSeconds(0.4f)
                                        .beginParallel()
                                        .waitSeconds(0.6f)
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.0f, 0.032f, 0.100097656f, -0.100097656f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
