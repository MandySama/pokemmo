package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 钢翼（招式 ID：211）的攻击／事件阶段动画。
 *
 * <p>招式描述：用坚硬的翅膀敲打对手进行攻击. 有时会提高自己的防御.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110211（名称）和 120211（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/SteelWingAnimation.java
 *
 * <p>源文件 SHA-256：b784c563efb2cf371078454c2f4e10b7107bc46751e54dcdfb2b7df523d6dc89
 */
final class ReferenceMoveProgram211 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.6f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1438,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1438,
                                                                        2,
                                                                        14,
                                                                        166.66667f,
                                                                        0.703125f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(377)),
                                                owner.emitParticles(-1, 377, 0, 9, 8, 0.5f),
                                                0.8f)
                                        .append(owner.emitParticles(-1, 377, 1, 9, 11, 0.5f)),
                                owner.emitParticles(-1, 377, 2, 11, 8, 0.5f),
                                1.8f)
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1421, 1, 16, 0.0f, 0.9375f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1421,
                                        2,
                                        16,
                                        83.333336f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1421,
                                        1,
                                        16,
                                        166.66667f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1436, 2, 16, 250.0f, 0.9375f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1436,
                                        1,
                                        16,
                                        333.33334f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 3, 0.016f, 0.032f, 0.30004883f, -0.30004883f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
