package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 王牌（招式 ID：376）的攻击／事件阶段动画。
 *
 * <p>招式描述：王牌招式的剩余PP越少, 招式的威力越大.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110376（名称）和 120376（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/TrumpCardAnimation.java
 *
 * <p>源文件 SHA-256：3d5f9ca747d4f01229d7c3bd78d84329be82cc0d966656ae631cd21c7ce44777
 */
final class ReferenceMoveProgram376 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .onStart(owner.preloadParticleEffect(551))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1483,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1514,
                                                                        2,
                                                                        16,
                                                                        750.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 551, 0, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 551, 1, 11, 8, 0.75f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 551, 2, 11, 8, 0.5f)),
                                                owner.emitParticles(-1, 551, 3, 11, 8, 0.5f),
                                                1.0f)
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.shakeCombatant(16, 2, 6, 0.016f, 0.032f, 0.19995117f, 0.0f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
