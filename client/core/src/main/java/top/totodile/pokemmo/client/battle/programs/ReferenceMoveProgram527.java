package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 电网（招式 ID：527）的攻击／事件阶段动画。
 *
 * <p>招式描述：用电网捉住对手进行攻击. 降低对手的速度.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110527（名称）和 120527（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/ElectrowebAnimation.java
 *
 * <p>源文件 SHA-256：e12d411dac17e779e82a0ef165b114a9bdea298e4a9c39e74c18788e05826f47
 */
final class ReferenceMoveProgram527 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.tintEnvironment(2, 0, 0.0f, 0.5f, 0.075f))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .onStart(owner.preloadParticleEffect(692))
                                        .append(owner.emitParticles(-1, 692, 3, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 692, 4, 11, 8, 0.5f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1759,
                                                        3,
                                                        16,
                                                        166.66667f,
                                                        0.3125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1522,
                                                        1,
                                                        16,
                                                        1000.0f,
                                                        0.390625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1491,
                                                        2,
                                                        16,
                                                        666.6667f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.0f,
                                                        0.032f,
                                                        0.100097656f,
                                                        -0.100097656f))
                                        .append(owner.emitParticles(-1, 692, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 692, 1, 11, 8, 0.5f)),
                                owner.emitParticles(-1, 692, 2, 11, 8, 0.5f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.tintEnvironment(2, 0, 0.5f, 0.0f, 0.075f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
