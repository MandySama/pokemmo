package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 幽冥窃手（自定义动画注册 ID：1042）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111042（名称）和 121042（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/ThiefAnimation.java
 *
 * <p>源文件 SHA-256：281588cad7f5a7a5f9b02b8fbbb2090db40c0642b0b05293d950619aaeb7b02c
 */
final class ReferenceMoveProgram1042 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Color color = EncounterMoveAnimation.color(21140);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1447,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.859375f,
                                                        owner.casterHandle)),
                                owner.shakeCombatant(14, 2, 1, 0.016f, 0.096f, 1.0f, 0.0f))
                        .append(owner.focusCamera(1, 0.4f))
                        .beginSequence()
                        .waitSeconds(0.32f)
                        .beginParallel()
                        .onStart(owner.preloadParticleEffect(334))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(owner.tintCombatant(16, 0.75f, 0.0f, 0.75f, color))
                        .append(owner.emitParticles(-1, 334, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 334, 1, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 334, 2, 11, 8, 0.5f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1478,
                                        1,
                                        16,
                                        0.0f,
                                        0.8984375f,
                                        owner.casterHandle));
        Color color2 = EncounterMoveAnimation.color(21140);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep6,
                                owner.scheduleSound(
                                        (byte) 2, 1420, 2, 16, 0.0f, 0.703125f, owner.casterHandle))
                        .append(owner.tintCombatant(16, 0.75f, 0.75f, 0.0f, color2))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
