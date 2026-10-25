package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 毒粉（招式 ID：77）的攻击／事件阶段动画。
 *
 * <p>招式描述：撒出毒粉, 从而让对手陷入中毒状态.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110077（名称）和 120077（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/PoisonPowderAnimation.java
 *
 * <p>源文件 SHA-256：2f91b10bc077ae6eb0e3c6c044b7d3d6f9017941897a0bb7c15ac0552b33688f
 */
final class ReferenceMoveProgram77 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .onStart(owner.preloadParticleEffect(241))
                        .append(owner.emitParticles(-1, 241, 0, 11, 8, 0.75f))
                        .append(owner.emitParticles(-1, 241, 1, 11, 8, 0.75f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1718,
                                        2,
                                        16,
                                        0.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1718,
                                        1,
                                        16,
                                        333.33334f,
                                        0.234375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1452,
                                        2,
                                        16,
                                        666.6667f,
                                        0.9375f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(30740);
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                animationStep6,
                                owner.scheduleSound(
                                        (byte) 2,
                                        1452,
                                        1,
                                        16,
                                        833.3333f,
                                        0.3125f,
                                        owner.casterHandle),
                                0.6f)
                        .append(owner.tintCombatant(16, 3.25f, 0.0f, 0.5f, color));
        color = EncounterMoveAnimation.color(30740);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep7.append(
                                        owner.tintCombatant(16, 3.25f, 0.5f, 0.0f, color)),
                                owner.scaleCombatant(
                                        16, 2, 6, 0.032f, 0.016f, 0.020019531f, -0.020019531f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
