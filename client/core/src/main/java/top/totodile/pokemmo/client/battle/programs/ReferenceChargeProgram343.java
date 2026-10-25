package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 渴望（招式 ID：343）的蓄力阶段动画。
 *
 * <p>招式描述：如果自身没携带道具, 可偷走对手道具. 对手是训练家时效果是暂时的.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110343（名称）和 120343（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/CovetAnimation.java
 *
 * <p>源文件 SHA-256：289c190db40d081061549dbe166dd44778fd3ddce85a71cacc3a84792c3c2f3c
 */
final class ReferenceChargeProgram343 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.appendThenOuterParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1513,
                                                                                        1,
                                                                                        14,
                                                                                        0.0f,
                                                                                        0.859375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1513,
                                                                                        1,
                                                                                        14,
                                                                                        83.333336f,
                                                                                        0.859375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1513,
                                                                                        1,
                                                                                        14,
                                                                                        166.66667f,
                                                                                        0.859375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1513,
                                                                                        1,
                                                                                        14,
                                                                                        250.0f,
                                                                                        0.859375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1513,
                                                                                        1,
                                                                                        14,
                                                                                        500.0f,
                                                                                        0.859375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1513,
                                                                                        1,
                                                                                        14,
                                                                                        583.3333f,
                                                                                        0.859375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1513,
                                                                                        1,
                                                                                        14,
                                                                                        666.6667f,
                                                                                        0.859375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1513,
                                                                                        1,
                                                                                        14,
                                                                                        750.0f,
                                                                                        0.859375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1452,
                                                                                        2,
                                                                                        16,
                                                                                        750.0f,
                                                                                        0.9375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .setSpriteAnimationPaused(
                                                                                                14,
                                                                                                1))
                                                                        .endGroup()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        0, 0.25f))
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                516))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 516, 1,
                                                                                        9, 8, 0.5f))
                                                                        .append(
                                                                                owner
                                                                                        .setSpriteAnimationPaused(
                                                                                                16,
                                                                                                1))
                                                                        .beginSequence()
                                                                        .waitSeconds(0.8f)
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        1, 0.25f)),
                                                                owner.shakeCombatant(
                                                                        16,
                                                                        2,
                                                                        7,
                                                                        0.016f,
                                                                        0.016f,
                                                                        0.19995117f,
                                                                        0.0f))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        16, 0))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scaleCombatant(
                                                                        16,
                                                                        2,
                                                                        1,
                                                                        0.016f,
                                                                        0.064f,
                                                                        -0.30004883f,
                                                                        0.30004883f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1787,
                                                                        1,
                                                                        2,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1434,
                                                                        2,
                                                                        2,
                                                                        0.0f,
                                                                        0.78125f,
                                                                        owner.casterHandle)),
                                                owner.launchParticles(516, 0, 11, 9, 0.5f, 552.0f),
                                                0.12f)
                                        .append(owner.focusCamera(0, 0.25f))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(0.92f)
                                        .beginParallel(),
                                owner.scaleCombatant(
                                        14, 2, 1, 0.016f, 0.064f, 0.30004883f, -0.30004883f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.25f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
