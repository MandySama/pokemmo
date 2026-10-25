package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 青焰（招式 ID：551）的攻击／事件阶段动画。
 *
 * <p>招式描述：用美丽而激烈的青焰包裹住对手进行攻击. 有时会让对手陷入灼伤状态.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110551（名称）和 120551（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/BlueFlareAnimation.java
 *
 * <p>源文件 SHA-256：bc1744ee6985c606b49c15fc8284db7b1299044b376f0e15dd5a2fd2f20ceec8
 */
final class ReferenceMoveProgram551 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep2 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1976,
                                        1,
                                        16,
                                        500.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .onStart(owner.loadBattleBackground(24))
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintEnvironment(4, 0, 0.0f, 1.0f, 0.075f))
                        .onStart(owner.setHealthHudHidden(18, true))
                        .endGroup()
                        .beginParallel();
        Runnable sceneFlagAction2 = owner.sceneFlag(2, 1 != 0);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, 0 != 0);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, 0 != 0);
        ReferenceMoveTimeline animationStep17 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                animationStep2
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                                        .onStart(owner.preloadParticleEffect(718)),
                                owner.emitParticles(-1, 718, 3, 11, 8, 0.5f),
                                0.4f)
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1426,
                                        1,
                                        16,
                                        0.0f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1426,
                                        2,
                                        16,
                                        133.33333f,
                                        0.8203125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1426,
                                        1,
                                        16,
                                        266.66666f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1426,
                                        2,
                                        16,
                                        400.0f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1426,
                                        1,
                                        16,
                                        583.3333f,
                                        0.7421875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1426,
                                        2,
                                        16,
                                        666.6667f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1426,
                                        1,
                                        16,
                                        800.0f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1426,
                                        2,
                                        16,
                                        933.3333f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1426,
                                        1,
                                        16,
                                        1166.6666f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1439,
                                        0,
                                        16,
                                        666.6667f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1439,
                                        1,
                                        16,
                                        1333.3334f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(0, 0, 656.0f, 1600.0f))
                        .append(owner.adjustSoundTrack(1, 0, 1296.0f, 2240.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1413,
                                        1,
                                        16,
                                        3333.3333f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1507,
                                        2,
                                        16,
                                        3166.6667f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.scrollBattleBackground(2, 0, 8, 0.032f))
                        .append(owner.emitParticles(-1, 718, 5, 11, 8, 0.25f));
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep20 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        animationStep17,
                                        owner.emitParticles(-1, 718, 4, 11, 8, 0.25f),
                                        1.0f)
                                .append(owner.tintCombatant(16, 1.25f, 0.0f, 0.5f, color))
                                .append(owner.setSpriteAnimationPaused(16, 1))
                                .endGroup()
                                .endGroup()
                                .beginSequence()
                                .waitSeconds(3.8f)
                                .beginParallel()
                                .append(owner.tintEnvironment(3, 0, 1.0f, 0.0f, 0.075f))
                                .append(
                                        owner.scaleCombatant(
                                                16,
                                                2,
                                                6,
                                                0.0f,
                                                0.064f,
                                                0.100097656f,
                                                -0.100097656f))
                                .append(owner.emitParticles(-1, 718, 2, 11, 8, 0.5f)),
                        owner.emitParticles(-1, 718, 0, 11, 8, 0.5f),
                        4.2f);
        Color color2 = EncounterMoveAnimation.color(31);
        Runnable sceneFlagAction5 = owner.sceneFlag(1, true);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenOuterParallel(
                                        animationStep20.append(
                                                owner.emitParticles(-1, 718, 1, 11, 8, 0.5f)),
                                        owner.tintEnvironment(3, 0, 0.0f, 1.0f, 0.075f)),
                                owner.tintCombatant(16, 0.75f, 0.5f, 0.0f, color2))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(0, true)))
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintEnvironment(4, 0, 1.0f, 0.0f, 0.075f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
