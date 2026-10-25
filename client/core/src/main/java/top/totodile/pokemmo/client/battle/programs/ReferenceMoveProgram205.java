package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 滚动（招式 ID：205）的攻击／事件阶段动画。
 *
 * <p>招式描述：在5回合内连续滚动攻击对手. 招式每次击中, 威力就会提高.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110205（名称）和 120205（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/RolloutAnimation.java
 *
 * <p>源文件 SHA-256：beaeda160ffaf6eee689d46cc886a4d29518a35516049bc466166f7bffe00302
 */
final class ReferenceMoveProgram205 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.appendThenNextParallel(
                                                                ReferenceMoveTimeline
                                                                        .waitThenParallel(0.6f)
                                                                        .append(
                                                                                owner
                                                                                        .shakeCombatant(
                                                                                                14,
                                                                                                2,
                                                                                                1,
                                                                                                0.016f,
                                                                                                0.048f,
                                                                                                1.0f,
                                                                                                1.0f)),
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1443,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1416,
                                                                        2,
                                                                        14,
                                                                        0.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1376,
                                                                        2,
                                                                        16,
                                                                        1000.0f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(371))
                                                        .append(
                                                                owner.launchParticles(
                                                                        371, 0, 9, 11, 0.25f,
                                                                        480.0f)),
                                                owner.launchParticles(371, 1, 9, 11, 0.25f, 480.0f),
                                                0.6f)
                                        .append(owner.emitParticles(-1, 371, 2, 11, 8, 0.5f))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        4,
                                                        0.016f,
                                                        0.048f,
                                                        -0.100097656f,
                                                        0.100097656f))
                                        .append(
                                                owner.shakeCombatant(
                                                        16,
                                                        2,
                                                        4,
                                                        0.016f,
                                                        0.048f,
                                                        0.19995117f,
                                                        0.100097656f)),
                                owner.scheduleSound(
                                        (byte) 2, 1475, 0, 16, 0.0f, 0.9375f, owner.casterHandle))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
