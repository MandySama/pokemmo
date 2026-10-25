package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 极光束（招式 ID：62）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手发射虹色光束进行攻击. 有时会降低对手的攻击.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110062（名称）和 120062（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/AuroraBeamAnimation.java
 *
 * <p>源文件 SHA-256：fbb1e54a61bf772037ad91571b2610c1b0e9719111d44e5af3bec849c442814d
 */
final class ReferenceMoveProgram62 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .onStart(owner.preloadParticleEffect(223))
                                                        .append(
                                                                owner.launchParticles(
                                                                        223, 0, 9, 11, 0.5f, 0.0f))
                                                        .append(
                                                                owner.launchParticles(
                                                                        223, 1, 9, 11, 0.5f, 0.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1455,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1450,
                                                                        2,
                                                                        14,
                                                                        0.0f,
                                                                        0.15625f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1376,
                                                                        2,
                                                                        14,
                                                                        1500.0f,
                                                                        0.0f,
                                                                        owner.casterHandle)),
                                                owner.tintEnvironment(2, 0, 0.0f, 0.8125f, 0.075f),
                                                0.4f)
                                        .append(owner.focusCamera(1, 0.25f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.shakeCombatant(16, 2, 12, 0.016f, 0.032f, 0.19995117f, 0.0f))
                        .append(owner.restoreCamera(0.25f))
                        .append(owner.tintEnvironment(2, 0, 0.8125f, 0.0f, 0.075f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
