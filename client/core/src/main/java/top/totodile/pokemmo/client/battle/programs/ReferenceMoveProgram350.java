package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 岩石爆击（招式 ID：350）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手发射坚硬的岩石进行攻击. 连续攻击2-5次.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110350（名称）和 120350（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/RockBlastAnimation.java
 *
 * <p>源文件 SHA-256：9605e3b7f91a6a338964035ed93868d55aa0236bc1d2fff07fb6303fe692a58b
 */
final class ReferenceMoveProgram350 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1874,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1513,
                                                                        2,
                                                                        14,
                                                                        0.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(523)),
                                                owner.launchParticles(523, 1, 9, 11, 0.5f, 240.0f),
                                                0.4f)
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(owner.emitParticles(-1, 523, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 523, 2, 11, 8, 0.5f))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.032f,
                                                        -0.19995117f,
                                                        0.19995117f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1532,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle)),
                                owner.shakeCombatant(16, 2, 6, 0.016f, 0.032f, 0.19995117f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
