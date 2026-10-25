package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 猛撞（招式 ID：36）的攻击／事件阶段动画。
 *
 * <p>招式描述：以惊人的气势撞向对手进行攻击. 自己也会受到少许伤害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110036（名称）和 120036（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/TakeDownAnimation.java
 *
 * <p>源文件 SHA-256：5f9ac236c61fe8346ad58a566d0250a9250c9ef98104a7f064369e22e0d65870
 */
final class ReferenceMoveProgram36 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence().beginParallel(),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle),
                                                0.08f)
                                        .append(owner.focusCamera(1, 0.25f))
                                        .onStart(owner.setHealthHudHidden(18, true))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(0.16f)
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1424,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(198))
                                        .append(owner.emitParticles(-1, 198, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 198, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 198, 2, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 198, 3, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.064f, 0.30004883f, -0.30004883f))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
