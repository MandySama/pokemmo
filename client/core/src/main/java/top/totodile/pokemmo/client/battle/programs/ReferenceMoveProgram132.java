package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 缠绕（招式 ID：132）的攻击／事件阶段动画。
 *
 * <p>招式描述：用触手或青藤等缠绕进行攻击. 有时会降低对手的速度.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110132（名称）和 120132（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/ConstrictAnimation.java
 *
 * <p>源文件 SHA-256：90ac255ed808eeea0100b24dfcdcc81568ccc4d47a728ba49575b3294c620a81
 */
final class ReferenceMoveProgram132 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(1, 0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .onStart(owner.preloadParticleEffect(297))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1441,
                                        1,
                                        16,
                                        333.33334f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1441,
                                        2,
                                        16,
                                        666.6667f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(owner.emitParticles(-1, 297, 0, 11, 8, 0.25f))
                        .append(owner.emitParticles(-1, 297, 1, 11, 8, 0.25f))
                        .append(owner.emitParticles(-1, 297, 2, 11, 8, 0.25f))
                        .append(owner.emitParticles(-1, 297, 3, 11, 8, 0.25f))
                        .append(owner.emitParticles(-1, 297, 4, 11, 8, 0.25f))
                        .append(owner.emitParticles(-1, 297, 5, 11, 8, 0.25f))
                        .beginSequence()
                        .waitSeconds(0.2f)
                        .beginParallel()
                        .append(owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
