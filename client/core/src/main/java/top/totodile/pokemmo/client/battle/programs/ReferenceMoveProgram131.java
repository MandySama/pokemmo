package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 尖刺加农炮（招式 ID：131）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手发射锐针进行攻击. 连续攻击2-5次.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵位移；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110131（名称）和 120131（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SpikeCannonAnimation.java
 *
 * <p>源文件 SHA-256：d2d8b105cbd1dd578d10dd9d59270bf291c777f237529665308f5cf43e0c38e5
 */
final class ReferenceMoveProgram131 {

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
                                                                        (short) 1411,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1376,
                                                                        1,
                                                                        14,
                                                                        1000.0f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 1, 480.0f, 480.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1420,
                                                                        2,
                                                                        16,
                                                                        333.33334f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1420,
                                                                        2,
                                                                        16,
                                                                        416.66666f,
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
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1420,
                                                                        2,
                                                                        16,
                                                                        583.3333f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(296))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 296, 1, 9, 11, 0.5f)),
                                                owner.emitParticles(-1, 296, 0, 11, 8, 0.5f),
                                                0.28f)
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
