package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 泥巴炸弹（招式 ID：426）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手发射坚硬的泥弹进行攻击. 有时会降低对手的命中率.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110426（名称）和 120426（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/MudBombAnimation.java
 *
 * <p>源文件 SHA-256：473ff42eb898984d5da48674c9fbbe7f11335f51d61ca5f2a99a0957abc4e20e
 */
final class ReferenceMoveProgram426 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1452,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.625f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1849,
                                                                        2,
                                                                        16,
                                                                        0.0f,
                                                                        0.3125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1475,
                                                                        1,
                                                                        16,
                                                                        1000.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1686,
                                                                        2,
                                                                        16,
                                                                        1000.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(601)),
                                                owner.launchParticles(601, 0, 9, 11, 0.5f, 648.0f),
                                                0.72f)
                                        .onStart(owner.setHealthHudHidden(18, true))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .append(owner.emitParticles(-1, 601, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 601, 2, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 601, 3, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(1.12f)
                                        .beginParallel(),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, 0.19995117f, -0.19995117f))
                        .append(owner.restoreCamera(0.4f))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
