package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 连环巴掌（招式 ID：3）的攻击／事件阶段动画。
 *
 * <p>招式描述：用连环巴掌拍打对手进行攻击. 连续攻击2-5次.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110003（名称）和 120003（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/DoubleSlapAnimation.java
 *
 * <p>源文件 SHA-256：ce19cbc6d99a9d90e9e4078c1d8444c455d7d2b9ea3bcb33268c5539e0249505
 */
final class ReferenceMoveProgram3 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .endGroup()
                                        .beginParallel()
                                        .onStart(owner.preloadParticleEffect(163))
                                        .append(owner.emitParticles(-1, 163, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 163, 3, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 163, 4, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 163, 2, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        2,
                                                        0.096f,
                                                        0.032f,
                                                        0.100097656f,
                                                        -0.100097656f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1422,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9765625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1420,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.2734375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1422,
                                                        1,
                                                        16,
                                                        416.66666f,
                                                        0.9765625f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        1420,
                                        2,
                                        16,
                                        416.66666f,
                                        0.2734375f,
                                        owner.casterHandle))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
