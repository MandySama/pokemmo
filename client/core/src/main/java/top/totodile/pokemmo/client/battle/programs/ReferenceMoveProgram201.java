package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 沙暴（招式 ID：201）的攻击／事件阶段动画。
 *
 * <p>招式描述：在5回合内扬起沙暴, 除岩石、地面和钢属性以外的宝可梦, 都会受到伤害. 岩石属性的特防还会提高.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110201（名称）和 120201（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/SandstormAnimation.java
 *
 * <p>源文件 SHA-256：253f895300688376b2eb75c5436a4e4417bbed4d480f62ae91f2a12f73ca9ec3
 */
final class ReferenceMoveProgram201 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .append(owner.tintEnvironment(4, 268, 0.0f, 0.875f, 0.05f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1497, 1, 14, 0.0f, 0.9375f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1376,
                                        1,
                                        14,
                                        1666.6666f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 1600.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1418, 3, 14, 0.0f, 0.78125f, owner.casterHandle));
        Color color = EncounterMoveAnimation.color(268);
        ReferenceMoveTimeline animationStep5 =
                animationStep4
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1376,
                                        3,
                                        14,
                                        1666.6666f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(3, 1, 960.0f, 640.0f))
                        .append(owner.tintCombatant(14, 0.5f, 0.0f, 0.625f, color));
        color = EncounterMoveAnimation.color(268);
        ReferenceMoveTimeline animationStep7 =
                animationStep5
                        .append(owner.tintCombatant(16, 0.5f, 0.0f, 0.625f, color))
                        .onStart(owner.preloadParticleEffect(367))
                        .append(owner.emitParticles(-1, 367, 0, 9, 8, 0.25f))
                        .append(owner.emitParticles(-1, 367, 1, 9, 8, 0.0f));
        Color color2 = EncounterMoveAnimation.color(268);
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                animationStep7, owner.emitParticles(-1, 367, 2, 9, 8, 0.0f), 0.88f)
                        .append(owner.tintEnvironment(4, 268, 0.875f, 0.0f, 0.05f))
                        .append(owner.tintCombatant(14, 0.5f, 0.625f, 0.0f, color2));
        color2 = EncounterMoveAnimation.color(268);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep8, owner.tintCombatant(16, 0.5f, 0.625f, 0.0f, color2))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
