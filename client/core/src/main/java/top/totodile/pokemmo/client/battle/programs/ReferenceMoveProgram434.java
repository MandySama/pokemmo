package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 流星群（招式 ID：434）的攻击／事件阶段动画。
 *
 * <p>招式描述：从天空中向对手落下陨石. 使用之后因为反作用力, 自己的特攻会大幅降低.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110434（名称）和 120434（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/DracoMeteorAnimation.java
 *
 * <p>源文件 SHA-256：fb6802998de3e43e8abece68cc91594482ee09af5f78ab37c346f7d3cfa5ac8d
 */
final class ReferenceMoveProgram434 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .onStart(owner.preloadParticleEffect(609))
                                        .append(owner.emitParticles(-1, 609, 0, 11, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 609, 1, 11, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 609, 2, 11, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 609, 3, 11, 8, 0.25f))
                                        .append(owner.tintEnvironment(4, 12288, 0.0f, 0.75f, 0.05f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1509,
                                                        1,
                                                        16,
                                                        250.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1418,
                                                        3,
                                                        16,
                                                        1166.6666f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        3,
                                                        16,
                                                        3000.0f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1466,
                                                        1,
                                                        16,
                                                        1333.3334f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1466,
                                                        1,
                                                        16,
                                                        1500.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1466,
                                                        1,
                                                        16,
                                                        1666.6666f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1466,
                                                        1,
                                                        16,
                                                        1833.3334f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1466,
                                                        1,
                                                        16,
                                                        2000.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1466,
                                                        1,
                                                        16,
                                                        2166.6667f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1475,
                                                        1,
                                                        16,
                                                        2500.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1475,
                                                        1,
                                                        16,
                                                        2833.3333f,
                                                        0.390625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1475,
                                                        1,
                                                        16,
                                                        3166.6667f,
                                                        0.078125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1475,
                                                        2,
                                                        16,
                                                        1333.3334f,
                                                        0.7421875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1475,
                                                        2,
                                                        16,
                                                        1450.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1475,
                                                        2,
                                                        16,
                                                        1600.0f,
                                                        0.8203125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1475,
                                                        2,
                                                        16,
                                                        1816.6666f,
                                                        0.703125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1475,
                                                        2,
                                                        16,
                                                        1950.0f,
                                                        0.7421875f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        1475,
                                        2,
                                        16,
                                        2166.6667f,
                                        0.6640625f,
                                        owner.casterHandle),
                                1.2f)
                        .append(owner.focusCamera(1, 0.8f))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(1.4f)
                        .beginParallel()
                        .append(owner.scaleCombatant(16, 2, 6, 0.0f, 0.096f, 0.0f, -0.19995117f))
                        .append(owner.shakeCombatant(16, 2, 12, 0.016f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintEnvironment(4, 12288, 0.75f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
