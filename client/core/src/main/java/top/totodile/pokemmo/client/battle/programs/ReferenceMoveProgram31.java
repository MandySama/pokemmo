package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 乱击（招式 ID：31）的攻击／事件阶段动画。
 *
 * <p>招式描述：用角或喙刺向对手进行攻击. 连续攻击2-5次.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110031（名称）和 120031（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/FuryAttackAnimation.java
 *
 * <p>源文件 SHA-256：074d2caeec6aea4789bfd27df174413bb8dbdd38b86b5db0fb7036471427b28e
 */
final class ReferenceMoveProgram31 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence().beginParallel(),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1421,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle),
                                                0.24f)
                                        .onStart(owner.preloadParticleEffect(193))
                                        .append(owner.emitParticles(-1, 193, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 193, 1, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.032f,
                                                        0.30004883f,
                                                        -0.30004883f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1420,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        1420,
                                        1,
                                        16,
                                        83.333336f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
