package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 角撞（招式 ID：30）的攻击／事件阶段动画。
 *
 * <p>招式描述：用尖锐的角攻击对手.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110030（名称）和 120030（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/HornAttackAnimation.java
 *
 * <p>源文件 SHA-256：a767e7902ce7db830f097dcb8b1311adcb0aa93ce9f7d165d0187267e00cf797
 */
final class ReferenceMoveProgram30 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        14, 1))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        16, 1))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1421,
                                                                        1,
                                                                        16,
                                                                        166.66667f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(192))
                                                        .append(
                                                                owner.launchParticles(
                                                                        192, 2, 9, 11, 0.5f,
                                                                        240.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1424,
                                                                        1,
                                                                        16,
                                                                        500.0f,
                                                                        0.546875f,
                                                                        owner.casterHandle)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1904,
                                                        2,
                                                        16,
                                                        500.0f,
                                                        0.9921875f,
                                                        owner.casterHandle),
                                                0.2f)
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(0.4f)
                                        .beginParallel()
                                        .append(owner.emitParticles(-1, 192, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 192, 1, 11, 8, 0.5f)),
                                owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.25f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
