package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 二连击（招式 ID：458）的攻击／事件阶段动画。
 *
 * <p>招式描述：使用尾巴等拍打对手进行攻击. 连续2次给予伤害.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110458（名称）和 120458（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/DoubleHitAnimation.java
 *
 * <p>源文件 SHA-256：85087f6e12f5f49cc0c3be19694ee2f2e5c939b02bd24459fa43a1ffd3913a0b
 */
final class ReferenceMoveProgram458 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
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
                                                                        (short) 1475,
                                                                        2,
                                                                        16,
                                                                        0.0f,
                                                                        0.234375f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(633))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 633, 1, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 633, 4, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 633, 5, 11, 8, 0.5f))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        16, 1)),
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
                                                        (short) 1420,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.234375f,
                                                        owner.casterHandle))
                                        .append(owner.emitParticles(-1, 633, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 633, 2, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 633, 3, 11, 8, 0.5f)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, -0.19995117f, 0.19995117f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
