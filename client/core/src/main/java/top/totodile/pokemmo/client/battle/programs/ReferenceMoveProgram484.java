package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 重磅冲撞（招式 ID：484）的攻击／事件阶段动画。
 *
 * <p>招式描述：用沉重的身体撞向对手进行攻击. 自己比对手越重, 威力越大.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110484（名称）和 120484（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/HeavySlamAnimation.java
 *
 * <p>源文件 SHA-256：3d9dca0ccaa9faf6a58acca333f15ccd3cd1644e8fbf7119a06b59f464451e86
 */
final class ReferenceMoveProgram484 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1560,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1475,
                                                                        1,
                                                                        14,
                                                                        166.66667f,
                                                                        0.78125f,
                                                                        owner.casterHandle)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1652,
                                                        2,
                                                        14,
                                                        500.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .onStart(owner.preloadParticleEffect(582))
                                        .append(owner.emitParticles(-1, 582, 0, 11, 8, 0.25f))
                                        .onStart(owner.preloadParticleEffect(198))
                                        .append(owner.emitParticles(-1, 198, 1, 11, 8, 0.5f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1413,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1885,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.064f, 0.30004883f, -0.30004883f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
