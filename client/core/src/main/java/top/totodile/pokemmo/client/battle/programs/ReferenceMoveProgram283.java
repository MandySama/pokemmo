package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 蛮干（招式 ID：283）的攻击／事件阶段动画。
 *
 * <p>招式描述：给予伤害, 使对手的HP变得和自己的HP一样.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110283（名称）和 120283（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/EndeavorAnimation.java
 *
 * <p>源文件 SHA-256：c4a08647cf8b7966caafaceeba9f66248a0593ca07acea7748634c603b0806e0
 */
final class ReferenceMoveProgram283 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
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
                                                                                                1478,
                                                                                        0,
                                                                                        14,
                                                                                        0.0f,
                                                                                        0.859375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1478,
                                                                                        0,
                                                                                        14,
                                                                                        333.33334f,
                                                                                        0.859375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1420,
                                                                                        1,
                                                                                        16,
                                                                                        166.66667f,
                                                                                        0.9375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1420,
                                                                                        2,
                                                                                        16,
                                                                                        416.66666f,
                                                                                        0.9375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1420,
                                                                                        1,
                                                                                        16,
                                                                                        666.6667f,
                                                                                        0.9375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .scaleCombatant(
                                                                                                14,
                                                                                                2,
                                                                                                3,
                                                                                                0.016f,
                                                                                                0.064f,
                                                                                                0.30004883f,
                                                                                                -0.30004883f))
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                450))
                                                                        .append(
                                                                                owner
                                                                                        .setSpriteAnimationPaused(
                                                                                                16,
                                                                                                1))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 450, 1,
                                                                                        9, 8, 0.4f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 450, 2,
                                                                                        11, 8,
                                                                                        0.4f)),
                                                                owner.scaleCombatant(
                                                                        16,
                                                                        2,
                                                                        1,
                                                                        0.016f,
                                                                        0.048f,
                                                                        -0.19995117f,
                                                                        0.19995117f),
                                                                0.24f)
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 450, 2, 11, 8, 0.4f)),
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.048f,
                                                        -0.19995117f,
                                                        0.19995117f),
                                                0.48f)
                                        .append(owner.emitParticles(-1, 450, 2, 11, 8, 0.4f)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.048f, -0.19995117f, 0.19995117f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
