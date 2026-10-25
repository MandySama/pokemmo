package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 念力（招式 ID：93）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手发送微弱的念力进行攻击. 有时会使对手混乱.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110093（名称）和 120093（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/ConfusionAnimation.java
 *
 * <p>源文件 SHA-256：2b6b53f786294be05ad5ac5b2d57ef77c8f25bff1ad418959dab70c84d1ef4ba
 */
final class ReferenceMoveProgram93 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep2 =
                ReferenceMoveTimeline.waitThenParallel(0.6f)
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(owner.tintEnvironment(2, 0, 0.0f, 0.75f, 0.05f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1450, 1, 14, 0.0f, 0.78125f, owner.casterHandle));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep3 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                        animationStep2
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1376,
                                                1,
                                                14,
                                                500.0f,
                                                0.0f,
                                                owner.casterHandle))
                                .append(owner.adjustSoundTrack(1, 1, 320.0f, 160.0f))
                                .append(
                                        owner.shakeCombatant(
                                                14, 2, 3, 0.016f, 0.032f, 0.30004883f, 0.0f)),
                        owner.tintCombatant(14, 0.5f, 0.0f, 0.5f, color),
                        0.2f);
        color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep3, owner.tintCombatant(14, 0.5f, 0.5f, 0.0f, color))
                        .append(owner.focusCamera(1, 0.4f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1461,
                                        1,
                                        16,
                                        166.66667f,
                                        0.8984375f,
                                        owner.casterHandle));
        Color color2 = EncounterMoveAnimation.color(22732);
        ReferenceMoveTimeline animationStep5 =
                animationStep4
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1452, 2, 16, 250.0f, 0.9375f, owner.casterHandle))
                        .append(owner.tintCombatant(16, 0.5f, 0.0f, 0.75f, color2))
                        .append(owner.scaleCombatant(16, 3, 3, 0.016f, 0.032f, -0.19995117f, 0.0f))
                        .append(owner.shakeCombatant(16, 2, 6, 0.032f, 0.016f, 0.19995117f, 0.0f))
                        .onStart(owner.preloadParticleEffect(262));
        Color color3 = EncounterMoveAnimation.color(22732);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                        animationStep5,
                                        owner.emitParticles(-1, 262, 0, 11, 8, 0.5f),
                                        0.44f),
                                owner.tintCombatant(16, 0.5f, 0.75f, 0.0f, color3))
                        .append(owner.tintEnvironment(2, 0, 0.75f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
