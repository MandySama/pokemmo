package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 回复封锁（招式 ID：377）的攻击／事件阶段动画。
 *
 * <p>招式描述：在5回合内无法通过招式、特性或携带的道具来回复HP.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110377（名称）和 120377（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/HealBlockAnimation.java
 *
 * <p>源文件 SHA-256：2c4960c15cb79c19f9583472099153faeb5562b70ad3a134bbbc21cab20e4631
 */
final class ReferenceMoveProgram377 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1517,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1518,
                                                                        2,
                                                                        16,
                                                                        750.0f,
                                                                        0.703125f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(552)),
                                                owner.emitParticles(-1, 552, 0, 11, 8, 0.5f),
                                                0.6f)
                                        .append(
                                                owner.tintEnvironment(
                                                        4, 15855, 0.0f, 0.75f, 0.025f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.tintCombatant(
                                        16, 0.25f, 0.0f, 0.625f, EncounterMoveAnimation.color(0)))
                        .append(
                                owner.tintCombatant(
                                        16, 0.25f, 0.625f, 0.0f, EncounterMoveAnimation.color(0)))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.tintEnvironment(4, 15855, 0.75f, 0.0f, 0.025f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
