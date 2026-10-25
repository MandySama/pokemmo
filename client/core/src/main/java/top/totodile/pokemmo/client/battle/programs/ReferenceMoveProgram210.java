package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 连斩（招式 ID：210）的攻击／事件阶段动画。
 *
 * <p>招式描述：用镰刀或爪子等切斩对手进行攻击. 连续击中, 威力就会提高.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵缩放；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110210（名称）和 120210（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/FuryCutterAnimation.java
 *
 * <p>源文件 SHA-256：ab9bfd71cf17e43da49942d82c6653604a1c46d297e6eab22b17a86301151448
 */
final class ReferenceMoveProgram210 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.appendThenNextParallel(
                                                        ReferenceMoveTimeline
                                                                .appendThenNextParallel(
                                                                        ReferenceMoveTimeline
                                                                                .appendThenNextParallel(
                                                                                        ReferenceMoveTimeline
                                                                                                .sequence()
                                                                                                .beginParallel()
                                                                                                .append(
                                                                                                        owner
                                                                                                                .tintEnvironment(
                                                                                                                        4,
                                                                                                                        0,
                                                                                                                        0.0f,
                                                                                                                        1.0f,
                                                                                                                        0.025f)),
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        1485,
                                                                                                        1,
                                                                                                        16,
                                                                                                        0.0f,
                                                                                                        0.78125f,
                                                                                                        owner.casterHandle)),
                                                                        owner.tintEnvironment(
                                                                                4, 0, 1.0f, 0.0f,
                                                                                0.025f))
                                                                .append(
                                                                        owner.tintEnvironment(
                                                                                4, 0, 0.0f, 1.0f,
                                                                                0.025f)),
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1485,
                                                                2,
                                                                16,
                                                                0.0f,
                                                                0.78125f,
                                                                owner.casterHandle)),
                                                owner.tintEnvironment(4, 0, 1.0f, 0.0f, 0.025f))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .onStart(owner.preloadParticleEffect(376))
                                        .append(owner.emitParticles(-1, 376, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 376, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 376, 2, 11, 8, 0.5f))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        4,
                                                        0.016f,
                                                        0.016f,
                                                        0.100097656f,
                                                        -0.100097656f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1423,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2, 1420, 2, 16, 0.0f, 0.859375f, owner.casterHandle))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
