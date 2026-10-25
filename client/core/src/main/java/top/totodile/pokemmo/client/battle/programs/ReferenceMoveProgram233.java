package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 借力摔（招式 ID：233）的攻击／事件阶段动画。
 *
 * <p>招式描述：会在对手之后进行攻击. 但是自己的攻击必定会命中.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110233（名称）和 120233（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/VitalThrowAnimation.java
 *
 * <p>源文件 SHA-256：45b775b55f5a81c44a7d354f7f8a05d2b622876092b4e2c0cd821217d13caaf5
 */
final class ReferenceMoveProgram233 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1421,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        1,
                                                        14,
                                                        583.3333f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.shakeCombatant(
                                                        14, 2, 1, 0.016f, 0.048f, 1.0f, 0.0f))
                                        .endGroup()
                                        .beginParallel()
                                        .waitSeconds(0.6f)
                                        .endGroup()
                                        .beginParallel()
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
                                                        2,
                                                        16,
                                                        166.66667f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(400))
                                        .append(owner.emitParticles(-1, 400, 0, 11, 8, 0.4f))
                                        .append(owner.emitParticles(-1, 400, 1, 11, 8, 0.4f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, -0.19995117f, 0.19995117f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
