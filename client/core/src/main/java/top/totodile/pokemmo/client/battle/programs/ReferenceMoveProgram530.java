package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 二连劈（招式 ID：530）的攻击／事件阶段动画。
 *
 * <p>招式描述：用身体坚硬的部分拍打对手进行攻击. 连续2次给予伤害.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110530（名称）和 120530（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/DualChopAnimation.java
 *
 * <p>源文件 SHA-256：f02b801001ad668da4fc5a2182a87cfc601a8d5608a434d762cad444a926ee82
 */
final class ReferenceMoveProgram530 {

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
                                                                                .onStart(
                                                                                        owner
                                                                                                .preloadParticleEffect(
                                                                                                        694))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1421,
                                                                                                        1,
                                                                                                        16,
                                                                                                        0.0f,
                                                                                                        0.9921875f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1423,
                                                                                                        2,
                                                                                                        16,
                                                                                                        183.33333f,
                                                                                                        0.9921875f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1421,
                                                                                                        1,
                                                                                                        16,
                                                                                                        733.3333f,
                                                                                                        0.9921875f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1423,
                                                                                                        2,
                                                                                                        16,
                                                                                                        833.3333f,
                                                                                                        0.9921875f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .emitParticles(
                                                                                                        -1,
                                                                                                        694,
                                                                                                        5,
                                                                                                        11,
                                                                                                        8,
                                                                                                        0.5f))
                                                                                .append(
                                                                                        owner
                                                                                                .emitParticles(
                                                                                                        -1,
                                                                                                        694,
                                                                                                        2,
                                                                                                        11,
                                                                                                        8,
                                                                                                        0.5f)),
                                                                        owner.emitParticles(
                                                                                -1, 694, 3, 11, 8,
                                                                                0.5f),
                                                                        0.2f)
                                                                .append(
                                                                        owner
                                                                                .setSpriteAnimationPaused(
                                                                                        16, 1)),
                                                        owner.scaleCombatant(
                                                                16,
                                                                2,
                                                                1,
                                                                0.016f,
                                                                0.032f,
                                                                0.30004883f,
                                                                -0.30004883f),
                                                        0.6f)
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 694, 4, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 694, 0, 11, 8, 0.5f)),
                                        owner.emitParticles(-1, 694, 1, 11, 8, 0.5f),
                                        0.8f),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, 0.30004883f, -0.30004883f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
