package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 磷火（招式 ID：261）的攻击／事件阶段动画。
 *
 * <p>招式描述：放出怪异的火焰, 从而让对手陷入灼伤状态.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110261（名称）和 120261（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/WillOWispAnimation.java
 *
 * <p>源文件 SHA-256：308a410c5d08539fb9fb9e51baa9972ba85b7c3974708368e2f74b3fbd0aaeee
 */
final class ReferenceMoveProgram261 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1455,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1467,
                                                                        2,
                                                                        14,
                                                                        0.0f,
                                                                        0.703125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1426,
                                                                        1,
                                                                        16,
                                                                        833.3333f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(427)),
                                                owner.launchParticles(427, 0, 9, 11, 0.5f, 0.0f),
                                                0.4f)
                                        .append(owner.focusCamera(1, 0.4f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(0.8f)
                                        .beginParallel()
                                        .append(owner.emitParticles(-1, 427, 1, 11, 8, 0.5f)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.064f, 0.19995117f, -0.19995117f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
