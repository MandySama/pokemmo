package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 飞弹针（招式 ID：42）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手发射锐针进行攻击. 连续攻击2-5次.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵位移。
 *
 * <p>编号对应参考动画注册表；语言条目为 110042（名称）和 120042（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/PinMissileAnimation.java
 *
 * <p>源文件 SHA-256：646acff698edd3b44adc468b5688594d3ea6360c108a511b1af032b6f018613d
 */
final class ReferenceMoveProgram42 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1539,
                                                                                        1,
                                                                                        14,
                                                                                        0.0f,
                                                                                        0.8984375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1539,
                                                                                        2,
                                                                                        14,
                                                                                        333.33334f,
                                                                                        0.8984375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1539,
                                                                                        1,
                                                                                        14,
                                                                                        666.6667f,
                                                                                        0.8984375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1475,
                                                                                        2,
                                                                                        16,
                                                                                        833.3333f,
                                                                                        0.8984375f,
                                                                                        owner.casterHandle))
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                203))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 203, 0,
                                                                                        11, 8,
                                                                                        0.5f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 203, 1,
                                                                                        11, 8,
                                                                                        0.5f)),
                                                                owner.launchParticles(
                                                                        203, 2, 9, 11, 0.5f,
                                                                        240.0f),
                                                                0.24f)
                                                        .append(
                                                                owner.shakeCombatant(
                                                                        16,
                                                                        2,
                                                                        3,
                                                                        0.032f,
                                                                        0.016f,
                                                                        0.30004883f,
                                                                        0.0f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 203, 1, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 203, 0, 11, 8, 0.5f)),
                                                owner.launchParticles(203, 2, 9, 11, 0.5f, 240.0f),
                                                0.48f)
                                        .append(
                                                owner.shakeCombatant(
                                                        16,
                                                        2,
                                                        3,
                                                        0.032f,
                                                        0.016f,
                                                        0.30004883f,
                                                        0.0f))
                                        .append(owner.launchParticles(203, 2, 9, 11, 0.5f, 240.0f))
                                        .append(owner.emitParticles(-1, 203, 0, 11, 8, 0.5f)),
                                owner.emitParticles(-1, 203, 1, 11, 8, 0.5f),
                                0.88f)
                        .append(owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
