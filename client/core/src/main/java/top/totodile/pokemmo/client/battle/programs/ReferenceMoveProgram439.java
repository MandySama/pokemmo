package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 岩石炮（招式 ID：439）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手发射巨大的岩石进行攻击. 下一回合自己将无法动弹.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110439（名称）和 120439（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/RockWreckerAnimation.java
 *
 * <p>源文件 SHA-256：49762c0d5ba3506cc6049707b77502670d35901884e4ba32e346d73f86405221
 */
final class ReferenceMoveProgram439 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, false);
        ReferenceMoveTimeline animationStep12 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .onStart(owner.loadBattleBackground(34))
                                                        .onStart(
                                                                ReferenceMoveTimeline.callback(
                                                                        sceneFlagAction2))
                                                        .append(
                                                                owner.tintEnvironment(
                                                                        4, 0, 0.0f, 0.8125f,
                                                                        0.075f))
                                                        .onStart(
                                                                ReferenceMoveTimeline.callback(
                                                                        sceneFlagAction3))
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scrollBattleBackground(
                                                                        4, -36, 0, 2.56f))
                                                        .onStart(
                                                                ReferenceMoveTimeline.callback(
                                                                        sceneFlagAction4))
                                                        .onStart(owner.setHealthHudHidden(14, true))
                                                        .onStart(owner.setHealthHudHidden(16, true))
                                                        .append(
                                                                owner.tintEnvironment(
                                                                        3, 0, 0.8125f, 0.0f,
                                                                        0.075f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1416,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1376,
                                                                        1,
                                                                        14,
                                                                        1166.6666f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 0, 0.0f, 1120.0f))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 1, 960.0f, 160.0f))
                                                        .onStart(owner.preloadParticleEffect(614))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 614, 3, 9, 8, 0.5f)),
                                                owner.emitParticles(-1, 614, 4, 9, 8, 0.5f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1510,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(owner.launchParticles(614, 0, 9, 11, 0.5f, 360.0f))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .beginSequence()
                                        .waitSeconds(0.4f)
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1424,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        1,
                                                        16,
                                                        250.0f,
                                                        0.46875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        2,
                                                        16,
                                                        500.0f,
                                                        0.15625f,
                                                        owner.casterHandle))
                                        .append(owner.emitParticles(-1, 614, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 614, 2, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 4, 0.016f, 0.032f, 0.19995117f, -0.19995117f)),
                        owner.tintEnvironment(3, 0, 0.0f, 0.8125f, 0.075f));
        Runnable sceneFlagAction5 = owner.sceneFlag(1, true);
        result =
                animationStep12
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(owner.setHealthHudHidden(14, false))
                        .onStart(owner.setHealthHudHidden(16, false))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.tintEnvironment(4, 0, 0.8125f, 0.125f, 0.075f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(0, true)))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
