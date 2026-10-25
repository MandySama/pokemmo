package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 投球（招式 ID：140）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手投掷圆形物体进行攻击. 连续攻击2-5次.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110140（名称）和 120140（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/BarrageAnimation.java
 *
 * <p>源文件 SHA-256：0d546c58bb19d5033cfbf009d1fdc801a8e1db2cb6d0742177d8e6a3c24678cf
 */
final class ReferenceMoveProgram140 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .onStart(owner.preloadParticleEffect(305)),
                                                owner.launchParticles(305, 1, 9, 11, 0.0f, 432.0f),
                                                0.2f)
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        0,
                                                        14,
                                                        0.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1722,
                                                        2,
                                                        16,
                                                        333.33334f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        1,
                                                        16,
                                                        333.33334f,
                                                        0.390625f,
                                                        owner.casterHandle))
                                        .append(owner.emitParticles(-1, 305, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 305, 2, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 305, 3, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(0.52f)
                                        .beginParallel(),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, 0.30004883f, -0.30004883f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
