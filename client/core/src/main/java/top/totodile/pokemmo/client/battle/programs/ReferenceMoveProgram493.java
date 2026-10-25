package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 单纯光束（招式 ID：493）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手发送谜之念波. 接收到念波的对手, 其特性会变为单纯.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110493（名称）和 120493（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/SimpleBeamAnimation.java
 *
 * <p>源文件 SHA-256：302f6cdfc331f43a6a7710db0071a9e7b3f257bf7e1264cd3008dc95fec2a457
 */
final class ReferenceMoveProgram493 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep2 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.waitThenParallel(0.6f),
                                owner.tintEnvironment(4, 0, 0.0f, 0.75f, 0.05f))
                        .append(owner.tintCombatant(14, 0.75f, 0.0f, 0.5f, color))
                        .append(
                                owner.scaleCombatant(
                                        14, 2, 6, 0.0f, 0.032f, 0.100097656f, -0.100097656f))
                        .onStart(owner.preloadParticleEffect(662))
                        .append(owner.launchParticles(662, 0, 9, 11, 0.5f, 840.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1450, 1, 14, 0.0f, 0.703125f, owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 1, 640.0f, 320.0f));
        Color color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                        ReferenceMoveTimeline
                                                                .appendThenDelayedOuterParallel(
                                                                        ReferenceMoveTimeline
                                                                                .appendThenDelayedOuterParallel(
                                                                                        ReferenceMoveTimeline
                                                                                                .appendThenDelayedOuterParallel(
                                                                                                        ReferenceMoveTimeline
                                                                                                                .appendThenDelayedParallel(
                                                                                                                        animationStep2,
                                                                                                                        owner
                                                                                                                                .scheduleSound(
                                                                                                                                        (byte)
                                                                                                                                                2,
                                                                                                                                        1376,
                                                                                                                                        1,
                                                                                                                                        14,
                                                                                                                                        1000.0f,
                                                                                                                                        0.0f,
                                                                                                                                        owner.casterHandle),
                                                                                                                        0.04f),
                                                                                                        owner
                                                                                                                .launchParticles(
                                                                                                                        662,
                                                                                                                        0,
                                                                                                                        9,
                                                                                                                        11,
                                                                                                                        0.5f,
                                                                                                                        840.0f),
                                                                                                        0.08f),
                                                                                        owner
                                                                                                .launchParticles(
                                                                                                        662,
                                                                                                        0,
                                                                                                        9,
                                                                                                        11,
                                                                                                        0.5f,
                                                                                                        840.0f),
                                                                                        0.12f),
                                                                        owner.launchParticles(
                                                                                662, 0, 9, 11, 0.5f,
                                                                                840.0f),
                                                                        0.16f),
                                                        owner.launchParticles(
                                                                662, 0, 9, 11, 0.5f, 840.0f),
                                                        0.2f)
                                                .append(
                                                        owner.tintCombatant(
                                                                14, 0.75f, 0.5f, 0.0f, color2)),
                                        owner.launchParticles(662, 0, 9, 11, 0.5f, 840.0f),
                                        0.24f)
                                .append(owner.launchParticles(662, 0, 9, 11, 0.5f, 840.0f))
                                .append(owner.focusCamera(1, 0.25f))
                                .endGroup()
                                .endGroup()
                                .beginSequence()
                                .waitSeconds(0.54f)
                                .beginParallel()
                                .append(owner.setSpriteAnimationPaused(16, 1))
                                .append(owner.emitParticles(-1, 662, 0, 11, 8, 0.5f)),
                        owner.scheduleSound(
                                (byte) 2,
                                (short) 1865,
                                2,
                                16,
                                0.0f,
                                0.9921875f,
                                owner.casterHandle),
                        0.84f);
        Color color3 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        animationStep5.append(owner.emitParticles(-1, 662, 0, 11, 8, 0.5f)),
                        owner.tintCombatant(16, 0.75f, 0.0f, 0.5f, color3),
                        1.14f);
        Color color4 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendThenOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                animationStep6,
                                                owner.emitParticles(-1, 662, 0, 11, 8, 0.5f),
                                                1.24f),
                                        owner.shakeCombatant(
                                                16, 2, 3, 0.0f, 0.032f, 0.30004883f, 0.0f))
                                .append(owner.tintCombatant(16, 0.75f, 0.5f, 0.0f, color4))
                                .append(owner.setSpriteAnimationPaused(16, 0))
                                .append(owner.restoreCamera(0.4f)),
                        owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.05f));
        return result;
    }
}
