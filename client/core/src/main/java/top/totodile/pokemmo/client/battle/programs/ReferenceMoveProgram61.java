package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 泡沫光线（招式 ID：61）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手猛烈地喷射泡沫进行攻击. 有时会降低对手的速度.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110061（名称）和 120061（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/BubbleBeamAnimation.java
 *
 * <p>源文件 SHA-256：e03c189313edc7b927e6aae659cf439ccb1377eb1b2d2b6bee346565fa544e11
 */
final class ReferenceMoveProgram61 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1480,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1529,
                                                                        1,
                                                                        16,
                                                                        1000.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1529,
                                                                        2,
                                                                        16,
                                                                        1166.6666f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1529,
                                                                        1,
                                                                        16,
                                                                        1333.3334f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1529,
                                                                        2,
                                                                        16,
                                                                        1500.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1529,
                                                                        1,
                                                                        16,
                                                                        1666.6666f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1529,
                                                                        2,
                                                                        16,
                                                                        1833.3334f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(222))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 222, 0, 9, 11, 0.625f)),
                                                owner.emitParticles(-1, 222, 1, 11, 8, 0.625f),
                                                0.6f)
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(1.4f)
                                        .beginParallel(),
                                owner.shakeCombatant(16, 2, 4, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
