package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 骨头回力镖（招式 ID：155）的攻击／事件阶段动画。
 *
 * <p>招式描述：用手中的骨头投掷对手, 来回连续2次给予伤害.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵位移；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110155（名称）和 120155（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/BonemerangAnimation.java
 *
 * <p>源文件 SHA-256：ede01eb3562fcd1b856ac951714bbeebbc353b7dc900df07041152fe8ae47a16
 */
final class ReferenceMoveProgram155 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        14, 1))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1531,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1421,
                                                                        2,
                                                                        14,
                                                                        83.333336f,
                                                                        0.703125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1421,
                                                                        2,
                                                                        14,
                                                                        166.66667f,
                                                                        0.703125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1421,
                                                                        2,
                                                                        14,
                                                                        250.0f,
                                                                        0.703125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1421,
                                                                        2,
                                                                        14,
                                                                        333.33334f,
                                                                        0.703125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1421,
                                                                        2,
                                                                        14,
                                                                        416.66666f,
                                                                        0.703125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        2, 1, 640.0f, 320.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1532,
                                                                        1,
                                                                        16,
                                                                        500.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1421,
                                                                        2,
                                                                        16,
                                                                        583.3333f,
                                                                        0.703125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1421,
                                                                        2,
                                                                        16,
                                                                        666.6667f,
                                                                        0.703125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1421,
                                                                        2,
                                                                        16,
                                                                        750.0f,
                                                                        0.703125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1421,
                                                                        1,
                                                                        16,
                                                                        833.3333f,
                                                                        0.703125f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(319))
                                                        .append(
                                                                owner.launchParticles(
                                                                        319, 0, 9, 11, 0.5f,
                                                                        240.0f)),
                                                owner.emitParticles(-1, 319, 1, 11, 8, 0.5f),
                                                0.4f)
                                        .append(
                                                owner.shakeCombatant(
                                                        16,
                                                        2,
                                                        3,
                                                        0.032f,
                                                        0.016f,
                                                        0.30004883f,
                                                        0.0f)),
                                owner.launchParticles(319, 0, 11, 9, 0.25f, 240.0f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
