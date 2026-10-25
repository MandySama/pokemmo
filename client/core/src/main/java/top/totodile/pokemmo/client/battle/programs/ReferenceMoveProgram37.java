package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 大闹一番（招式 ID：37）的攻击／事件阶段动画。
 *
 * <p>招式描述：在2-3回合内, 乱打一气地攻击对手. 大闹一番后自己会陷入混乱.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110037（名称）和 120037（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/ThrashAnimation.java
 *
 * <p>源文件 SHA-256：9c402d3fef483aa5356622138f7fe372efa65513c26de49c12d0792ba8f68c7a
 */
final class ReferenceMoveProgram37 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        0, 0.6f))
                                                                        .endGroup()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1420,
                                                                                        1,
                                                                                        16,
                                                                                        0.0f,
                                                                                        0.9375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1420,
                                                                                        2,
                                                                                        16,
                                                                                        250.0f,
                                                                                        0.9375f,
                                                                                        owner.casterHandle))
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                199))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 199, 0,
                                                                                        11, 8,
                                                                                        0.4f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 199, 2,
                                                                                        11, 8,
                                                                                        0.4f)),
                                                                owner.scaleCombatant(
                                                                        16,
                                                                        2,
                                                                        1,
                                                                        0.016f,
                                                                        0.032f,
                                                                        0.30004883f,
                                                                        -0.30004883f),
                                                                0.4f)
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1420,
                                                                        2,
                                                                        16,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1420,
                                                                        1,
                                                                        16,
                                                                        166.66667f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 199, 0, 11, 8, 0.525f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 199, 1, 11, 8, 0.525f)),
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.032f,
                                                        0.30004883f,
                                                        -0.30004883f),
                                                0.8f)
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        500.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(owner.emitParticles(-1, 199, 0, 11, 8, 0.275f))
                                        .append(owner.emitParticles(-1, 199, 2, 11, 8, 0.275f)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, 0.30004883f, -0.30004883f),
                                1.2f)
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        2,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(owner.emitParticles(-1, 199, 0, 11, 8, 0.4f))
                        .append(owner.emitParticles(-1, 199, 1, 11, 8, 0.4f))
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, 0.30004883f, -0.30004883f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
