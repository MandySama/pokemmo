package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 冰锥（招式 ID：333）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手发射锋利的冰柱进行攻击. 连续攻击2-5次.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵位移；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110333（名称）和 120333（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/IcicleSpearAnimation.java
 *
 * <p>源文件 SHA-256：94044fcbf722f4d90844ab8170fab7c3aa76bbec93e15dc17ded4de1527d3c37
 */
final class ReferenceMoveProgram333 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                        ReferenceMoveTimeline
                                                                .appendThenDelayedParallel(
                                                                        ReferenceMoveTimeline
                                                                                .sequence()
                                                                                .beginParallel()
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1539,
                                                                                                        1,
                                                                                                        14,
                                                                                                        0.0f,
                                                                                                        0.8984375f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1539,
                                                                                                        2,
                                                                                                        14,
                                                                                                        416.66666f,
                                                                                                        0.8984375f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1539,
                                                                                                        1,
                                                                                                        14,
                                                                                                        833.3333f,
                                                                                                        0.8984375f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1488,
                                                                                                        2,
                                                                                                        16,
                                                                                                        833.3333f,
                                                                                                        0.8984375f,
                                                                                                        owner.casterHandle))
                                                                                .onStart(
                                                                                        owner
                                                                                                .preloadParticleEffect(
                                                                                                        503))
                                                                                .append(
                                                                                        owner
                                                                                                .emitParticles(
                                                                                                        -1,
                                                                                                        503,
                                                                                                        0,
                                                                                                        11,
                                                                                                        8,
                                                                                                        0.5f))
                                                                                .append(
                                                                                        owner
                                                                                                .emitParticles(
                                                                                                        -1,
                                                                                                        503,
                                                                                                        1,
                                                                                                        11,
                                                                                                        8,
                                                                                                        0.5f)),
                                                                        owner.emitParticles(
                                                                                -1, 503, 2, 9, 11,
                                                                                0.5f),
                                                                        0.24f)
                                                                .append(
                                                                        owner
                                                                                .setSpriteAnimationPaused(
                                                                                        16, 1))
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
                                                                                -1, 503, 0, 11, 8,
                                                                                0.5f))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 503, 1, 11, 8,
                                                                                0.5f)),
                                                        owner.emitParticles(
                                                                -1, 503, 2, 9, 11, 0.5f),
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
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 503, 0, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 503, 1, 11, 8, 0.5f)),
                                        owner.emitParticles(-1, 503, 2, 9, 11, 0.5f),
                                        0.88f),
                                owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup();
        return result;
    }
}
