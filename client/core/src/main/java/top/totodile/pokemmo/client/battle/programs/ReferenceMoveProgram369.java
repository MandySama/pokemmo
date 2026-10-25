package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 急速折返（招式 ID：369）的攻击／事件阶段动画。
 *
 * <p>招式描述：在攻击之后急速返回, 和后备宝可梦进行替换.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；控制精灵显隐；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110369（名称）和 120369（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/UTurnAnimation.java
 *
 * <p>源文件 SHA-256：821177f76edad183a62c2eefa5bc85331cc304424a1e98adfd1bbbd5c506e5cd
 */
final class ReferenceMoveProgram369 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.25f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1505,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1510,
                                                        2,
                                                        16,
                                                        166.66667f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1543,
                                                        1,
                                                        14,
                                                        750.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        583.3333f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1452,
                                                        2,
                                                        14,
                                                        1416.6666f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(543))
                                        .append(owner.emitParticles(-1, 543, 3, 9, 8, 0.5f))
                                        .append(owner.launchParticles(543, 1, 9, 11, 0.5f, 360.0f))
                                        .append(owner.setCombatantVisibility(14, 3))
                                        .beginSequence()
                                        .waitSeconds(0.02f)
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.25f))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(0.4f)
                                        .beginParallel()
                                        .append(owner.emitParticles(-1, 543, 4, 11, 8, 0.5f))
                                        .append(owner.launchParticles(543, 2, 11, 9, 0.5f, 600.0f))
                                        .append(owner.emitParticles(-1, 543, 5, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(0.72f)
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.25f)),
                                owner.emitParticles(-1, 543, 0, 9, 8, 0.75f),
                                1.72f)
                        .append(owner.setCombatantVisibility(14, 4))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.restoreCamera(0.3f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
