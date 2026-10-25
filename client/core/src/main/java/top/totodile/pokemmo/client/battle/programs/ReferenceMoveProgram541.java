package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 扫尾拍打（招式 ID：541）的攻击／事件阶段动画。
 *
 * <p>招式描述：用坚硬的尾巴拍打对手进行攻击. 连续攻击2-5次.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110541（名称）和 120541（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/TailSlapAnimation.java
 *
 * <p>源文件 SHA-256：896d2011dc2831a87fd42615ba7713437fbc7955cb09cdccd98665172f8dd3bc
 */
final class ReferenceMoveProgram541 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .onStart(owner.setHealthHudHidden(18, true))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .onStart(owner.preloadParticleEffect(704))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 704, 2, 11, 8, 0.125f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 704, 1, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 704, 0, 11, 8, 0.5f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1651,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1427,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.390625f,
                                                        owner.casterHandle),
                                                0.2f)
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.064f, -0.39990234f, 0.39990234f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
