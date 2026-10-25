package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 猛推（招式 ID：292）的攻击／事件阶段动画。
 *
 * <p>招式描述：用张开着的双手猛推对手进行攻击. 连续攻击2-5次.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110292（名称）和 120292（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/ArmThrustAnimation.java
 *
 * <p>源文件 SHA-256：ae2ddb15628942b3f25a50e26b7c17933a0fbe13613164b9fb8c51947175d538
 */
final class ReferenceMoveProgram292 {

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
                                                                        (short) 1422,
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
                                                                        333.33334f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(459)),
                                                owner.emitParticles(-1, 459, 1, 11, 8, 0.5f),
                                                0.2f)
                                        .append(owner.emitParticles(-1, 459, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 459, 2, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 459, 3, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, -0.30004883f, 0.30004883f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
