package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 盐水（招式 ID：362）的攻击／事件阶段动画。
 *
 * <p>招式描述：当对手的HP负伤到一半左右时, 招式威力会变成2倍.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110362（名称）和 120362（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/BrineAnimation.java
 *
 * <p>源文件 SHA-256：1ff52e9d27dee9ebef8648d32ef3d5b8b3a475870c020d9f556d3b8444faf548
 */
final class ReferenceMoveProgram362 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1511,
                                                        1,
                                                        16,
                                                        166.66667f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1407,
                                                        2,
                                                        16,
                                                        166.66667f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        2,
                                                        16,
                                                        1083.3334f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(536))
                                        .append(owner.emitParticles(-1, 536, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 536, 1, 11, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 536, 2, 11, 8, 0.25f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .beginSequence()
                                        .waitSeconds(0.2f)
                                        .beginParallel(),
                                owner.scaleCombatant(
                                        16, 2, 4, 0.016f, 0.032f, 0.19995117f, -0.19995117f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
