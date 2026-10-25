package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 细雪（招式 ID：181）的攻击／事件阶段动画。
 *
 * <p>招式描述：将冰冷的细雪吹向对手进行攻击. 有时会让对手陷入冰冻状态.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110181（名称）和 120181（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/PowderSnowAnimation.java
 *
 * <p>源文件 SHA-256：5ae23202bddf4e64d6e50d81a5b8c230a5fc674ed045e44d76f96a48eaae82a3
 */
final class ReferenceMoveProgram181 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1457,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1358,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.625f,
                                                        owner.casterHandle))
                                        .append(owner.tintEnvironment(4, 0, 0.0f, 1.0f, 0.05f))
                                        .onStart(owner.preloadParticleEffect(347)),
                                owner.launchParticles(347, 0, 9, 11, 0.5f, 0.0f),
                                0.6f)
                        .append(owner.focusCamera(1, 0.35f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1488,
                                        2,
                                        16,
                                        1000.0f,
                                        0.9375f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep5 =
                animationStep4
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1488,
                                        1,
                                        16,
                                        1583.3334f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(owner.tintCombatant(16, 1.0f, 0.0f, 0.375f, color))
                        .append(owner.setSpriteAnimationPaused(16, 1));
        Color color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep5, owner.emitParticles(-1, 347, 1, 11, 8, 0.5f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.tintEnvironment(4, 0, 1.0f, 0.0f, 0.05f))
                        .append(owner.tintCombatant(16, 1.0f, 0.375f, 0.0f, color2))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
