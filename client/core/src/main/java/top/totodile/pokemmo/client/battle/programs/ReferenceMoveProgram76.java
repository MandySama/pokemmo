package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 日光束（招式 ID：76）的攻击／事件阶段动画。
 *
 * <p>招式描述：第1回合收集满满的日光, 第2回合发射光束进行攻击.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110076（名称）和 120076（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SolarBeamAnimation.java
 *
 * <p>源文件 SHA-256：84aca4d52361e09ea0b6a7a26d85b9c26143424b7e48946847710d5f9df4a9ce
 */
final class ReferenceMoveProgram76 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(0, 0 != 0);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                        ReferenceMoveTimeline.sequence()
                                .beginParallel()
                                .append(owner.focusCamera(0, 0.4f))
                                .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1475,
                                                1,
                                                14,
                                                166.66667f,
                                                0.9375f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1505,
                                                2,
                                                14,
                                                166.66667f,
                                                0.859375f,
                                                owner.casterHandle))
                                .onStart(owner.preloadParticleEffect(240))
                                .append(owner.emitParticles(-1, 240, 2, 9, 8, 0.5f))
                                .append(owner.emitParticles(-1, 240, 1, 9, 8, 0.5f)),
                        owner.emitParticles(-1, 240, 0, 9, 8, 0.5f),
                        1.2f);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, 1 != 0);
        ReferenceMoveTimeline animationStep12 =
                animationStep6
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                        .append(owner.focusCamera(1, 0.2f))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(1.8f)
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1450,
                                        2,
                                        16,
                                        0.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1963,
                                        1,
                                        16,
                                        0.0f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        2,
                                        16,
                                        1333.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        16,
                                        1333.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 1, 960.0f, 320.0f))
                        .append(owner.emitParticles(-1, 240, 3, 11, 8, 0.0f))
                        .append(owner.emitParticles(-1, 240, 4, 11, 8, 0.0f));
        Color color = EncounterMoveAnimation.color(1023);
        ReferenceMoveTimeline animationStep13 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep12
                                        .append(owner.emitParticles(-1, 240, 5, 11, 8, 0.0f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(owner.tintCombatant(16, 0.75f, 0.0f, 0.75f, color)),
                                owner.scaleCombatant(
                                        16, 2, 7, 0.016f, 0.032f, 0.30004883f, -0.30004883f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0));
        color = EncounterMoveAnimation.color(1023);
        result =
                ReferenceMoveTimeline.appendAndClose(
                        animationStep13, owner.tintCombatant(16, 0.75f, 0.75f, 0.0f, color));
        return result;
    }
}
