package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 双针（招式 ID：41）的攻击／事件阶段动画。
 *
 * <p>招式描述：将2根针刺入对手, 连续2次给予伤害. 有时会让对手陷入中毒状态.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵位移；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110041（名称）和 120041（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/TwineedleAnimation.java
 *
 * <p>源文件 SHA-256：272fc3518f451f489bbd73d03218bf5d6651be9cec07f90eb2cdf5befb07f2a6
 */
final class ReferenceMoveProgram41 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(
                                                                        owner
                                                                                .setSpriteAnimationPaused(
                                                                                        14, 1))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1421,
                                                                                1,
                                                                                14,
                                                                                333.33334f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1421,
                                                                                1,
                                                                                14,
                                                                                500.0f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1420,
                                                                                2,
                                                                                16,
                                                                                500.0f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1420,
                                                                                2,
                                                                                16,
                                                                                666.6667f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                202))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 202, 0, 11, 8,
                                                                                0.5f)),
                                                        owner.launchParticles(
                                                                202, 1, 9, 11, 0.5f, 240.0f),
                                                        0.2f)
                                                .append(owner.setSpriteAnimationPaused(16, 1))
                                                .append(
                                                        owner.shakeCombatant(
                                                                16,
                                                                2,
                                                                2,
                                                                0.032f,
                                                                0.016f,
                                                                0.30004883f,
                                                                0.0f)),
                                        owner.launchParticles(202, 1, 9, 11, 0.5f, 240.0f),
                                        0.6f),
                                owner.shakeCombatant(16, 2, 2, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
