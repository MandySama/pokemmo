package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 移花接木（招式 ID：492）的攻击／事件阶段动画。
 *
 * <p>招式描述：利用对手的力量进行攻击. 正和自己战斗的对手, 其攻击越高, 伤害越大.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110492（名称）和 120492（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/FoulPlayAnimation.java
 *
 * <p>源文件 SHA-256：7823e8869a8b5a9802d99900b3e1196705e05b8bbc3a56988d8587e0d9089b1e
 */
final class ReferenceMoveProgram492 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.6f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1489,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        450.0f,
                                                        0.9921875f,
                                                        owner.casterHandle),
                                                0.2f)
                                        .onStart(owner.preloadParticleEffect(661))
                                        .append(owner.emitParticles(-1, 661, 2, 11, 8, 0.4f))
                                        .append(owner.emitParticles(-1, 661, 1, 11, 8, 0.4f))
                                        .append(owner.emitParticles(-1, 661, 0, 11, 8, 0.4f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.0f, 0.096f, -0.30004883f, 0.30004883f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
