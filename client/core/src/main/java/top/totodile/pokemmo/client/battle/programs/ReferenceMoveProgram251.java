package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 围攻（招式 ID：251）的攻击／事件阶段动画。
 *
 * <p>招式描述：我方全员进行攻击. 同行的宝可梦越多, 招式的攻击次数越多.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110251（名称）和 120251（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/BeatUpAnimation.java
 *
 * <p>源文件 SHA-256：fb13c5667c5282a8d6c4e0dc73c565b9f249da039dde9cbc9611644a1f398e8c
 */
final class ReferenceMoveProgram251 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1420,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1424,
                                                        2,
                                                        16,
                                                        83.333336f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(417))
                                        .append(owner.emitParticles(-1, 417, 0, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.048f, -0.19995117f, 0.19995117f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
