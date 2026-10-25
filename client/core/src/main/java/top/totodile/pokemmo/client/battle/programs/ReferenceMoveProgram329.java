package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 绝对零度（招式 ID：329）的攻击／事件阶段动画。
 *
 * <p>招式描述：给对手一击濒死. 如果是冰属性以外的宝可梦使用, 就会难以打中.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110329（名称）和 120329（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/SheerColdAnimation.java
 *
 * <p>源文件 SHA-256：33eff1522268cf58278b2b979026727807e7f28736d91074df8f505ae35d5aff
 */
final class ReferenceMoveProgram329 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(0, 0 != 0);
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .onStart(owner.setHealthHudHidden(18, true))
                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.8125f, 0.025f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1488,
                                        1,
                                        16,
                                        0.0f,
                                        0.546875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1488,
                                        1,
                                        16,
                                        500.0f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1709,
                                        2,
                                        16,
                                        333.33334f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1449,
                                        2,
                                        16,
                                        416.66666f,
                                        0.625f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(499))
                        .append(owner.emitParticles(-1, 499, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 499, 1, 11, 8, 0.125f));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep7
                                        .append(owner.emitParticles(-1, 499, 2, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.shakeCombatant(
                                                        16, 2, 4, 0.0f, 0.032f, 0.19995117f, 0.0f)),
                                owner.tintCombatant(16, 0.25f, 0.0f, 0.8125f, color))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(0, true)))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.tintEnvironment(4, 0, 0.8125f, 0.0f, 0.025f));
        color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                animationStep8
                        .append(owner.tintCombatant(16, 0.25f, 0.8125f, 0.0f, color))
                        .append(owner.restoreCamera(0.4f))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
