package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 木槌（招式 ID：452）的攻击／事件阶段动画。
 *
 * <p>招式描述：用坚硬的躯体撞击对手进行攻击. 自己也会受到不小的伤害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110452（名称）和 120452（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/WoodHammerAnimation.java
 *
 * <p>源文件 SHA-256：bf0969a84f735f18edb57a7c153bc42d6de1bd10183d21348eb0cbf4084c0e3b
 */
final class ReferenceMoveProgram452 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        14, 1))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1715,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1475,
                                                                        2,
                                                                        16,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(627))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 627, 0, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 627, 1, 11, 8, 0.0f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1,
                                                                        627,
                                                                        2,
                                                                        11,
                                                                        8,
                                                                        0.025024414f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 627, 3, 11, 8, 0.0f)),
                                                owner.emitParticles(-1, 627, 4, 11, 8, 0.0f),
                                                0.1f)
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
