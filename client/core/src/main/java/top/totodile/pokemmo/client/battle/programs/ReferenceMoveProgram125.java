package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 骨棒（招式 ID：125）的攻击／事件阶段动画。
 *
 * <p>招式描述：用手中的骨头殴打对手进行攻击. 有时会使对手畏缩.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110125（名称）和 120125（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/BoneClubAnimation.java
 *
 * <p>源文件 SHA-256：fc90b8016996ab0f6774a56b5dade35255e9735e58a2cccf130d3dd21547225a
 */
final class ReferenceMoveProgram125 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1437,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(290)),
                                                owner.launchParticles(290, 0, 9, 11, 0.5f, 360.0f),
                                                0.4f)
                                        .append(owner.focusCamera(1, 0.4f))
                                        .append(owner.emitParticles(-1, 290, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 290, 2, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.032f,
                                                        0.30004883f,
                                                        -0.30004883f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1420,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.703125f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        1722,
                                        2,
                                        16,
                                        0.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
