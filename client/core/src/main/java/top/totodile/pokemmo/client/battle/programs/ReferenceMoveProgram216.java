package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 报恩（招式 ID：216）的攻击／事件阶段动画。
 *
 * <p>招式描述：为了训练家而全力攻击对手. 亲密度越高, 威力越大.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110216（名称）和 120216（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/ReturnAnimation.java
 *
 * <p>源文件 SHA-256：850ecb57935b128b5cb6f1c35ef5a5cc7983e55aca5033081794d751e1c2c17f
 */
final class ReferenceMoveProgram216 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .append(
                                                                owner.scaleCombatant(
                                                                        14,
                                                                        2,
                                                                        2,
                                                                        0.032f,
                                                                        0.064f,
                                                                        0.19995117f,
                                                                        -0.19995117f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1489,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.8984375f,
                                                                        owner.casterHandle)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1490,
                                                        2,
                                                        14,
                                                        0.0f,
                                                        0.46875f,
                                                        owner.casterHandle))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .onStart(owner.preloadParticleEffect(382))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(owner.emitParticles(-1, 382, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 382, 1, 11, 8, 0.5f))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.032f,
                                                        0.064f,
                                                        -0.19995117f,
                                                        0.19995117f)),
                                owner.scheduleSound(
                                        (byte) 2, 1420, 1, 16, 0.0f, 0.9375f, owner.casterHandle))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
