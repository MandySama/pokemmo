package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 陀螺球（招式 ID：360）的攻击／事件阶段动画。
 *
 * <p>招式描述：让身体高速旋转并撞击对手. 速度比对手越慢, 威力越大.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110360（名称）和 120360（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/GyroBallAnimation.java
 *
 * <p>源文件 SHA-256：fd426ffd84a7a956cc04781558364f1fb2640387104c99292bad06cf9feea785
 */
final class ReferenceMoveProgram360 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1876,
                                                                        2,
                                                                        14,
                                                                        0.0f,
                                                                        0.625f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(534))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 534, 0, 9, 8, 0.5f)),
                                                owner.emitParticles(-1, 534, 1, 9, 8, 0.5f),
                                                0.8f)
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(1.2f)
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1686,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(owner.emitParticles(-1, 534, 2, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 534, 3, 11, 8, 0.5f)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.048f, -0.19995117f, 0.19995117f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
