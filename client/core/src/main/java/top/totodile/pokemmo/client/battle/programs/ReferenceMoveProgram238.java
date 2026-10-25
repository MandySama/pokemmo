package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 十字劈（招式 ID：238）的攻击／事件阶段动画。
 *
 * <p>招式描述：用两手呈十字劈打对手进行攻击. 容易击中要害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110238（名称）和 120238（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/CrossChopAnimation.java
 *
 * <p>源文件 SHA-256：ea6e85d3d9ccf43fb6a4398e3305f0ad529a98e483ad4099728936e0c99e4bfa
 */
final class ReferenceMoveProgram238 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1437,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.390625f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1437,
                                                                        2,
                                                                        16,
                                                                        83.333336f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1437,
                                                                        1,
                                                                        16,
                                                                        833.3333f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1420,
                                                                        2,
                                                                        16,
                                                                        1050.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(405))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 405, 1, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 405, 2, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 405, 3, 11, 8, 0.5f)),
                                                owner.emitParticles(-1, 405, 4, 11, 8, 0.5f),
                                                1.0f)
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.0f,
                                                        0.096f,
                                                        0.30004883f,
                                                        -0.30004883f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(owner.emitParticles(-1, 405, 5, 11, 8, 0.5f)),
                                owner.emitParticles(-1, 405, 0, 11, 8, 0.5f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
