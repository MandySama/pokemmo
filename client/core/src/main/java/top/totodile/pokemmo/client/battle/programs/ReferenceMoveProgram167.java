package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 三连踢（招式 ID：167）的攻击／事件阶段动画。
 *
 * <p>招式描述：连续3次踢对手进行攻击. 每踢中一次, 威力就会提高.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110167（名称）和 120167（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/TripleKickAnimation.java
 *
 * <p>源文件 SHA-256：4af2b88bde20d53c209ecfa525b1f46f8d8f617ffc6f876b3f5ee3b7f401ab49
 */
final class ReferenceMoveProgram167 {

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
                                                                        1560,
                                                                        0,
                                                                        16,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(333)),
                                                owner.emitParticles(-1, 333, 1, 11, 8, 0.5f),
                                                0.04f)
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1420,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1475,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.032f,
                                                        0.032f,
                                                        -0.30004883f,
                                                        0.30004883f))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .append(owner.emitParticles(-1, 333, 0, 11, 8, 0.5f)),
                                owner.emitParticles(-1, 333, 1, 11, 8, 0.5f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
