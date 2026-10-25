package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 铁头（招式 ID：442）的攻击／事件阶段动画。
 *
 * <p>招式描述：用钢铁般坚硬的头部进行攻击. 有时会使对手畏缩.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110442（名称）和 120442（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/IronHeadAnimation.java
 *
 * <p>源文件 SHA-256：e9c7bc11fae4f09fd37b230e86d6f314a7b23737e8d0779b5e6838d8a00274ee
 */
final class ReferenceMoveProgram442 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1413,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle)),
                                                owner.shakeCombatant(
                                                        14, 2, 1, 0.032f, 0.032f, 1.0f, 0.0f))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1424,
                                                        1,
                                                        16,
                                                        333.33334f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1448,
                                                        2,
                                                        16,
                                                        333.33334f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(617))
                                        .append(owner.emitParticles(-1, 617, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 617, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 617, 2, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 4, 0.016f, 0.032f, 0.19995117f, -0.19995117f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
