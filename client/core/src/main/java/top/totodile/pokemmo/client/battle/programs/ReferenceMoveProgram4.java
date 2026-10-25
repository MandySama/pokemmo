package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 连续拳（招式 ID：4）的攻击／事件阶段动画。
 *
 * <p>招式描述：用拳头怒涛般的殴打对手进行攻击. 连续攻击2-5次.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110004（名称）和 120004（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/CometPunchAnimation.java
 *
 * <p>源文件 SHA-256：61856be08e10b7bda73937f038ce1e38111fa3b07d7ac2839c80da2ffd125311
 */
final class ReferenceMoveProgram4 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        1,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        2,
                                        16,
                                        83.333336f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        1,
                                        16,
                                        166.66667f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        2,
                                        16,
                                        250.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        1,
                                        16,
                                        333.33334f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        2,
                                        16,
                                        416.66666f,
                                        0.9375f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(164))
                        .append(owner.emitParticles(-1, 164, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 164, 1, 11, 8, 0.5f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 5, 0.016f, 0.032f, 0.19995117f, -0.19995117f))
                        .endGroup()
                        .beginParallel()
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
