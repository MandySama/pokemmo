package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 金属爪（招式 ID：232）的攻击／事件阶段动画。
 *
 * <p>招式描述：用钢铁之爪劈开对手进行攻击. 有时会提高自己的攻击.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110232（名称）和 120232（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/MetalClawAnimation.java
 *
 * <p>源文件 SHA-256：d2284052eb129dfdc5f08087d144541aa72f92368a6e762dd3ea1a14619d45c7
 */
final class ReferenceMoveProgram232 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        14, 1))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1436,
                                                                        1,
                                                                        16,
                                                                        250.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1420,
                                                                        2,
                                                                        16,
                                                                        250.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1436,
                                                                        1,
                                                                        16,
                                                                        583.3333f,
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
                                                        .onStart(owner.preloadParticleEffect(398))
                                                        .onStart(owner.preloadParticleEffect(399))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 398, 0, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 398, 1, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 399, 0, 11, 8, 0.5f)),
                                                owner.emitParticles(-1, 399, 1, 11, 8, 0.5f),
                                                0.2f),
                                        owner.shakeCombatant(
                                                16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f),
                                        0.72f),
                                owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
