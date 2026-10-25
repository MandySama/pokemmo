package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 电球（招式 ID：486）的攻击／事件阶段动画。
 *
 * <p>招式描述：用电气团撞向对手. 自己比对手速度越快, 威力越大.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110486（名称）和 120486（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/ElectroBallAnimation.java
 *
 * <p>源文件 SHA-256：c9413054982515335bef68a6c3573cd8ef77f6a35514a1ee8930bbdd42e36fe0
 */
final class ReferenceMoveProgram486 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        0, 0.4f))
                                                                        .endGroup()
                                                                        .beginParallel()
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                656))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 656, 2,
                                                                                        9, 8,
                                                                                        1.0f)),
                                                                owner.emitParticles(
                                                                        -1, 656, 5, 9, 8, 1.0f),
                                                                0.4f)
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 656, 1, 9, 8, 0.75f))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        16, 1))
                                                        .append(
                                                                owner.scaleCombatant(
                                                                        16,
                                                                        2,
                                                                        1,
                                                                        0.0f,
                                                                        0.032f,
                                                                        0.100097656f,
                                                                        -0.100097656f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1522,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1427,
                                                        1,
                                                        14,
                                                        1166.6666f,
                                                        0.9921875f,
                                                        owner.casterHandle),
                                                1.8f)
                                        .append(owner.launchParticles(656, 0, 9, 11, 0.5f, 360.0f))
                                        .append(owner.launchParticles(656, 3, 9, 11, 0.5f, 360.0f))
                                        .append(owner.focusCamera(1, 0.4f)),
                                owner.emitParticles(-1, 656, 4, 11, 8, 0.5f),
                                2.2f)
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 16, 0.0f, 0.048f, 0.100097656f, -0.100097656f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1849,
                                        2,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1522,
                                        1,
                                        16,
                                        500.0f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
