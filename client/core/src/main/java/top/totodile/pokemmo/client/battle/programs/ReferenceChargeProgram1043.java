package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 幽冥窃手（自定义动画注册 ID：1043）的蓄力阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111043（名称）和 121043（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/ThiefAnimation.java
 *
 * <p>源文件 SHA-256：281588cad7f5a7a5f9b02b8fbbb2090db40c0642b0b05293d950619aaeb7b02c
 */
final class ReferenceChargeProgram1043 {

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
                        .onStart(owner.preloadParticleEffect(335))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(owner.tintCombatant(16, 0.75f, 0.0f, 0.75f, color))
                        .append(owner.emitParticles(-1, 335, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 335, 2, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 335, 3, 11, 8, 0.5f))
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
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.appendThenOuterParallel(
                                                        animationStep6,
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1420,
                                                                2,
                                                                16,
                                                                0.0f,
                                                                0.703125f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.tintCombatant(
                                                                16, 0.75f, 0.75f, 0.0f, color2))
                                                .append(owner.setSpriteAnimationPaused(16, 0))
                                                .append(owner.restoreCamera(0.4f))
                                                .waitSeconds(0.4f)
                                                .endGroup()
                                                .beginParallel()
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1510,
                                                                1,
                                                                2,
                                                                0.0f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1358,
                                                                2,
                                                                14,
                                                                833.3333f,
                                                                0.9375f,
                                                                owner.casterHandle)),
                                        owner.launchParticles(335, 1, 11, 9, 0.5f, 552.0f),
                                        0.8f),
                                owner.scaleCombatant(
                                        14, 2, 1, 0.0f, 0.064f, 0.30004883f, -0.30004883f),
                                1.4f)
                        .append(owner.restoreCamera(0.6f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
