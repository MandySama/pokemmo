package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 真气弹（招式 ID：411）的攻击／事件阶段动画。
 *
 * <p>招式描述：提高气势, 释放出全部力量. 有时会降低对手的特防.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110411（名称）和 120411（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/FocusBlastAnimation.java
 *
 * <p>源文件 SHA-256：4fcb5df2374a072346a531d684f79494f7dd2f7ecedbbe2be431edc9873f272c
 */
final class ReferenceMoveProgram411 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.4f)),
                                                owner.tintEnvironment(2, 0, 0.0f, 0.8125f, 0.075f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1707,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        1,
                                                        14,
                                                        1833.3334f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 1760.0f))
                                        .append(owner.adjustSoundTrack(1, 1, 1280.0f, 480.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1966,
                                                        1,
                                                        14,
                                                        1850.0f,
                                                        0.546875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1707,
                                                        2,
                                                        14,
                                                        1850.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        2,
                                                        16,
                                                        2833.3333f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 1, 2560.0f, 160.0f))
                                        .onStart(owner.preloadParticleEffect(586))
                                        .append(owner.emitParticles(-1, 586, 0, 9, 8, 0.5f)),
                                owner.emitParticles(-1, 586, 1, 9, 8, 0.5f))
                        .onStart(owner.loadBattleBackground(6));
        Runnable sceneFlagAction2 = owner.sceneFlag(1, false);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(2, true);
        ReferenceMoveTimeline animationStep11 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                animationStep8
                                                        .onStart(
                                                                ReferenceMoveTimeline.callback(
                                                                        sceneFlagAction2))
                                                        .onStart(
                                                                ReferenceMoveTimeline.callback(
                                                                        sceneFlagAction3))
                                                        .onStart(
                                                                ReferenceMoveTimeline.callback(
                                                                        sceneFlagAction4))
                                                        .append(
                                                                owner.scrollBattleBackground(
                                                                        4, -20, 0, 1.12f)),
                                                owner.launchParticles(586, 2, 9, 11, 0.5f, 720.0f),
                                                0.6f)
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .endGroup()
                                        .endGroup()
                                        .beginParallel()
                                        .append(owner.scrollBattleBackground(4, -20, 0, 1.6f))
                                        .append(owner.emitParticles(-1, 586, 3, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1785,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.3125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1686,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle)),
                                owner.shakeCombatant(16, 2, 8, 0.0f, 0.032f, 0.60009766f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(16, 0));
        Runnable sceneFlagAction5 = owner.sceneFlag(1, true);
        Runnable sceneFlagAction6 = owner.sceneFlag(0, true);
        result =
                animationStep11
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .append(owner.tintEnvironment(2, 0, 0.8125f, 0.0f, 0.075f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
