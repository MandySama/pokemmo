package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 惩罚（招式 ID：386）的攻击／事件阶段动画。
 *
 * <p>招式描述：根据能力变化, 对手提高的力量越大, 招式的威力越大.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110386（名称）和 120386（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/PunishmentAnimation.java
 *
 * <p>源文件 SHA-256：a5b36f40a12f2c56ce16207fbcabd04fdc140e90d3f71e807c9afdbcfe3af287
 */
final class ReferenceMoveProgram386 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1435,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1420,
                                                                        1,
                                                                        14,
                                                                        500.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(561))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 561, 0, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 561, 1, 11, 8, 0.5f)),
                                                owner.emitParticles(-1, 561, 2, 11, 8, 0.5f),
                                                0.4f)
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, 0.19995117f, -0.19995117f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
