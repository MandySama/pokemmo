package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 二连踢（招式 ID：24）的攻击／事件阶段动画。
 *
 * <p>招式描述：用2只脚踢飞对手进行攻击. 连续2次给予伤害.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110024（名称）和 120024（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/DoubleKickAnimation.java
 *
 * <p>源文件 SHA-256：b5d38fd3b0c22df8e42dc9a9d4726a53ac95bcd21dda328053af9c0a6537f2f7
 */
final class ReferenceMoveProgram24 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenDelayedParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .sequence()
                                                                                        .beginParallel()
                                                                                        .endGroup()
                                                                                        .beginParallel()
                                                                                        .onStart(
                                                                                                owner
                                                                                                        .preloadParticleEffect(
                                                                                                                186))
                                                                                        .append(
                                                                                                owner
                                                                                                        .emitParticles(
                                                                                                                -1,
                                                                                                                186,
                                                                                                                1,
                                                                                                                11,
                                                                                                                8,
                                                                                                                0.5f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                (short)
                                                                                                                        1421,
                                                                                                                1,
                                                                                                                16,
                                                                                                                0.0f,
                                                                                                                0.8984375f,
                                                                                                                owner.casterHandle)),
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1424,
                                                                                        2,
                                                                                        16,
                                                                                        0.0f,
                                                                                        0.8984375f,
                                                                                        owner.casterHandle),
                                                                                0.12f)
                                                                        .append(
                                                                                owner
                                                                                        .setSpriteAnimationPaused(
                                                                                                16,
                                                                                                1))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 186, 0,
                                                                                        11, 8,
                                                                                        0.5f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 186, 2,
                                                                                        11, 8,
                                                                                        0.5f)),
                                                                owner.scaleCombatant(
                                                                        16,
                                                                        2,
                                                                        1,
                                                                        0.0f,
                                                                        0.096f,
                                                                        -0.19995117f,
                                                                        0.19995117f),
                                                                0.32f)
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 186, 1, 11, 8, 0.5f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1420,
                                                                        2,
                                                                        16,
                                                                        0.0f,
                                                                        0.8984375f,
                                                                        owner.casterHandle)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1424,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle),
                                                0.52f)
                                        .append(owner.emitParticles(-1, 186, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 186, 2, 11, 8, 0.5f)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.0f, 0.096f, -0.19995117f, 0.19995117f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
