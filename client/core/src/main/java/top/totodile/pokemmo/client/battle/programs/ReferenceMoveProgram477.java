package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 意念移物（招式 ID：477）的攻击／事件阶段动画。
 *
 * <p>招式描述：利用超能力使对手浮起来. 在3回合内攻击会变得容易打中对手.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110477（名称）和 120477（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/TelekinesisAnimation.java
 *
 * <p>源文件 SHA-256：f30412cde403606a888b4fc0932875e4f69b37f25be11311e0424f5b8605fcfa
 */
final class ReferenceMoveProgram477 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.625f, 0.075f))
                        .append(owner.focusCamera(0, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintCombatant(14, 1.0f, 0.0f, 0.75f, color))
                        .onStart(owner.preloadParticleEffect(363))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1433,
                                        1,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.emitParticles(-1, 363, 0, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 363, 1, 9, 8, 0.5f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .beginSequence()
                        .waitSeconds(0.4f)
                        .beginParallel()
                        .append(owner.tintEnvironment(4, 0, 0.625f, 0.0f, 0.075f))
                        .append(owner.focusCamera(1, 0.25f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1777, 1, 16, 0.0f, 0.78125f, owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 1, 1280.0f, 160.0f));
        Color color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep5
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        1,
                                                        16,
                                                        1500.0f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .onStart(owner.setHealthHudHidden(16, true)),
                                owner.shakeCombatant(16, 2, 1, 0.016f, 0.32f, 1.0f, 2.0f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.tintCombatant(14, 1.0f, 0.75f, 0.0f, color2))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .onStart(owner.setHealthHudHidden(16, false))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
