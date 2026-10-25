package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 种子机关枪（招式 ID：331）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手猛烈地发射种子进行攻击. 连续攻击2-5次.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵位移；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110331（名称）和 120331（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/BulletSeedAnimation.java
 *
 * <p>源文件 SHA-256：3aed47b475b742c920be320269c979041cc7d8d5b7d5c05ed2f739bb52995d2d
 */
final class ReferenceMoveProgram331 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1513,
                                                        1,
                                                        14,
                                                        83.333336f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1513,
                                                        1,
                                                        14,
                                                        166.66667f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1513,
                                                        1,
                                                        14,
                                                        250.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1513,
                                                        1,
                                                        14,
                                                        333.33334f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1513,
                                                        1,
                                                        14,
                                                        416.66666f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1513,
                                                        1,
                                                        14,
                                                        500.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1513,
                                                        1,
                                                        14,
                                                        583.3333f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1513,
                                                        1,
                                                        14,
                                                        666.6667f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1513,
                                                        1,
                                                        14,
                                                        750.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1513,
                                                        1,
                                                        14,
                                                        833.3333f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        883.3333f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        966.6667f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        1050.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        1133.3334f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        1216.6666f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        1300.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        1383.3334f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        1466.6666f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(501)),
                                owner.launchParticles(501, 0, 9, 11, 0.5f, 0.0f),
                                0.8f)
                        .append(owner.emitParticles(-1, 501, 1, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 501, 2, 11, 8, 0.5f))
                        .append(owner.shakeCombatant(16, 2, 6, 0.016f, 0.032f, 0.5f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
