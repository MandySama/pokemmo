package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 纹理2（招式 ID：176）的攻击／事件阶段动画。
 *
 * <p>招式描述：为了可以抵抗对手最后使用的招式, 从而使自己的属性发生变化.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110176（名称）和 120176（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/Conversion2Animation.java
 *
 * <p>源文件 SHA-256：2532dabf7d8238d9d582a30575c92c2adefabd0aac920f991b26a83ef5e65404
 */
final class ReferenceMoveProgram176 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .onStart(owner.preloadParticleEffect(343))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 343, 0, 11, 8, 0.5f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1358,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1471,
                                                                        2,
                                                                        16,
                                                                        833.3333f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1447,
                                                                        1,
                                                                        16,
                                                                        833.3333f,
                                                                        0.625f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1473,
                                                                        2,
                                                                        16,
                                                                        1833.3334f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1478,
                                                                        1,
                                                                        16,
                                                                        2000.0f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(owner.focusCamera(1, 0.4f)),
                                                owner.launchParticles(343, 1, 11, 9, 0.5f, 0.0f),
                                                1.2f)
                                        .append(owner.focusCamera(0, 0.4f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(1.6f)
                                        .beginParallel()
                                        .append(owner.emitParticles(-1, 343, 2, 9, 8, 0.5f)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, 0.19995117f, -0.19995117f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
