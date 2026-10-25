package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 报复（招式 ID：279）的攻击／事件阶段动画。
 *
 * <p>招式描述：如果受到对手的招式攻击, 就能给予对手2倍的伤害.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110279（名称）和 120279（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/RevengeAnimation.java
 *
 * <p>源文件 SHA-256：c577208baba1767f75213abcc425ebfc5052e5cad9e984d30ba68229d25a126f
 */
final class ReferenceMoveProgram279 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(
                                                                owner.tintEnvironment(
                                                                        4, 0, 0.0f, 0.75f, 0.1f))
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .onStart(owner.preloadParticleEffect(445))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 445, 2, 9, 8, 0.5f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1440,
                                                                        2,
                                                                        14,
                                                                        0.0f,
                                                                        0.625f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1783,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1376,
                                                                        1,
                                                                        14,
                                                                        1666.6666f,
                                                                        0.0f,
                                                                        owner.casterHandle)),
                                                owner.adjustSoundTrack(1, 1, 640.0f, 800.0f))
                                        .append(owner.focusCamera(1, 0.25f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(owner.emitParticles(-1, 445, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 445, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 445, 3, 11, 8, 0.5f))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.048f,
                                                        -0.19995117f,
                                                        0.19995117f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1423,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2, 1420, 1, 16, 0.0f, 0.78125f, owner.casterHandle))
                        .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.1f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
