package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 喷出（招式 ID：255）的攻击／事件阶段动画。
 *
 * <p>招式描述：将积蓄的力量撞向对手进行攻击. 积蓄得越多, 威力越大.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110255（名称）和 120255（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/SpitUpAnimation.java
 *
 * <p>源文件 SHA-256：490da607fe07e16c205752c689fe003de4290ca02668f096ecad9562742051d5
 */
final class ReferenceMoveProgram255 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.appendThenOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenNextParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .waitThenParallel(
                                                                                                0.6f)
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                1478,
                                                                                                                2,
                                                                                                                14,
                                                                                                                0.0f,
                                                                                                                0.9375f,
                                                                                                                owner.casterHandle)),
                                                                                owner
                                                                                        .scaleCombatant(
                                                                                                14,
                                                                                                1,
                                                                                                1,
                                                                                                0.016f,
                                                                                                0.128f,
                                                                                                0.6999512f,
                                                                                                0.5f))
                                                                        .append(
                                                                                owner
                                                                                        .shakeCombatant(
                                                                                                14,
                                                                                                2,
                                                                                                6,
                                                                                                0.0f,
                                                                                                0.032f,
                                                                                                0.30004883f,
                                                                                                0.0f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1867,
                                                                                        2,
                                                                                        14,
                                                                                        166.66667f,
                                                                                        0.546875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1376,
                                                                                        2,
                                                                                        14,
                                                                                        666.6667f,
                                                                                        0.0f,
                                                                                        owner.casterHandle)),
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1849,
                                                                        1,
                                                                        14,
                                                                        533.3333f,
                                                                        0.546875f,
                                                                        owner.casterHandle),
                                                                0.6f)
                                                        .append(
                                                                owner.scaleCombatant(
                                                                        14, 1, 1, 0.016f, 0.128f,
                                                                        1.0f, 1.0f))
                                                        .onStart(owner.preloadParticleEffect(421)),
                                                owner.emitParticles(-1, 421, 0, 9, 8, 0.4f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .waitSeconds(0.6f)
                                        .endGroup()
                                        .beginParallel(),
                                owner.emitParticles(-1, 421, 1, 11, 8, 0.4f),
                                0.4f)
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1420, 1, 16, 0.0f, 0.9375f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1420,
                                        2,
                                        16,
                                        83.333336f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1420,
                                        1,
                                        16,
                                        166.66667f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(owner.shakeCombatant(16, 2, 6, 0.0f, 0.032f, 0.30004883f, 0.0f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
