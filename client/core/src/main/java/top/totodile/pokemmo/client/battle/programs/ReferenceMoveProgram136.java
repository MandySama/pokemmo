package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 飞膝踢（招式 ID：136）的攻击／事件阶段动画。
 *
 * <p>招式描述：跳起后用膝盖撞对手进行攻击. 如果撞偏则自己会受到伤害.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110136（名称）和 120136（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/HighJumpKickAnimation.java
 *
 * <p>源文件 SHA-256：b2a0d809d1e5ad473b8fe568300e4fea03aaa4be0d8f7f6fdcb2e4df7f1015e9
 */
final class ReferenceMoveProgram136 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .append(
                                                                owner.shakeCombatant(
                                                                        14, 2, 1, 0.032f, 0.032f,
                                                                        1.0f, 1.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1421,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .waitSeconds(0.6f)
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1424,
                                                                        2,
                                                                        16,
                                                                        166.66667f,
                                                                        0.9375f,
                                                                        owner.casterHandle)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1420,
                                                        1,
                                                        16,
                                                        250.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(301))
                                        .append(owner.emitParticles(-1, 301, 0, 11, 8, 0.4f))
                                        .append(owner.emitParticles(-1, 301, 1, 11, 8, 0.4f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, 0.30004883f, -0.30004883f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
