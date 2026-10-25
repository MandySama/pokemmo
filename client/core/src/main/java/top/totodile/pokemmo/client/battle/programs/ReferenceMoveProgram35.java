package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 紧束（招式 ID：35）的攻击／事件阶段动画。
 *
 * <p>招式描述：使用长长的身体或藤蔓等, 在4-5回合内紧束对手进行攻击.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110035（名称）和 120035（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/WrapAnimation.java
 *
 * <p>源文件 SHA-256：e12567c91e319da7ed7c1ba1cc4604861dd1c37aabc1038a2d2e937db7df540f
 */
final class ReferenceMoveProgram35 {

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
                                                        .onStart(owner.preloadParticleEffect(197))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 197, 0, 11, 8, 0.375f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 197, 2, 11, 8, 0.375f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 197, 3, 11, 8, 0.375f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 197, 4, 11, 8, 0.375f))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        16, 1))
                                                        .append(
                                                                owner.scaleCombatant(
                                                                        16,
                                                                        2,
                                                                        5,
                                                                        0.016f,
                                                                        0.096f,
                                                                        -0.30004883f,
                                                                        0.30004883f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1441,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1441,
                                                                        2,
                                                                        16,
                                                                        166.66667f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1441,
                                                                        1,
                                                                        16,
                                                                        333.33334f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1441,
                                                                        2,
                                                                        16,
                                                                        583.3333f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1441,
                                                                        1,
                                                                        16,
                                                                        750.0f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1441,
                                                                        2,
                                                                        16,
                                                                        1000.0f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1441,
                                                                        1,
                                                                        16,
                                                                        1166.6666f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1441,
                                                                        2,
                                                                        16,
                                                                        1333.3334f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1441,
                                                                        1,
                                                                        16,
                                                                        1583.3334f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1441,
                                                                        2,
                                                                        16,
                                                                        1750.0f,
                                                                        0.8984375f,
                                                                        owner.casterHandle)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1441,
                                                        1,
                                                        16,
                                                        2000.0f,
                                                        0.8984375f,
                                                        owner.casterHandle),
                                                1.2f)
                                        .append(owner.emitParticles(-1, 197, 0, 11, 8, 0.375f))
                                        .append(owner.emitParticles(-1, 197, 2, 11, 8, 0.375f))
                                        .append(owner.emitParticles(-1, 197, 3, 11, 8, 0.375f)),
                                owner.emitParticles(-1, 197, 4, 11, 8, 0.375f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
