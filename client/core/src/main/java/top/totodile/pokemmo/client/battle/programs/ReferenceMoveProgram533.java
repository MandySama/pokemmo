package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 圣剑（招式 ID：533）的攻击／事件阶段动画。
 *
 * <p>招式描述：用长角切斩对手进行攻击. 无视对手的能力变化, 直接给予伤害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110533（名称）和 120533（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/SacredSwordAnimation.java
 *
 * <p>源文件 SHA-256：bb6fcbb627dfead17af6cb8565cda265700b7878469184a1140b0cea5b4eff9e
 */
final class ReferenceMoveProgram533 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .onStart(owner.preloadParticleEffect(697))
                                        .append(owner.emitParticles(-1, 697, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 697, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 697, 2, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 697, 3, 11, 8, 0.5f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1495,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.390625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1651,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, 0.30004883f, -0.30004883f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
