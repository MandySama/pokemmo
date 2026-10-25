package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 岩石封锁（招式 ID：317）的攻击／事件阶段动画。
 *
 * <p>招式描述：投掷岩石进行攻击. 封住对手的行动, 从而降低速度.
 *
 * <p>动画说明：播放参考粒子特效。
 *
 * <p>编号对应参考动画注册表；语言条目为 110317（名称）和 120317（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/RockTombAnimation.java
 *
 * <p>源文件 SHA-256：3d224102c48c497c437a3f04f9e0fc46743d69ffd615a941099c7331107f7ecd
 */
final class ReferenceMoveProgram317 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1416,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        1,
                                                        14,
                                                        1666.6666f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1418,
                                                        3,
                                                        14,
                                                        0.0f,
                                                        0.703125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        3,
                                                        14,
                                                        1666.6666f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(487)),
                                owner.emitParticles(-1, 487, 1, 0, 8, 0.4f),
                                0.4f)
                        .append(owner.emitParticles(-1, 487, 0, 11, 8, 0.4f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1699,
                                        2,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1699,
                                        2,
                                        16,
                                        166.66667f,
                                        0.78125f,
                                        owner.casterHandle))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
