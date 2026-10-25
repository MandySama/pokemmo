package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 加农水炮（招式 ID：308）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手喷射水炮进行攻击. 下一回合自己将无法动弹.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110308（名称）和 120308（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/HydroCannonAnimation.java
 *
 * <p>源文件 SHA-256：caa7c32ff3e29b0c079479e8feb62a0a77cfd4232a66fe8394181c2bbc00e6b1
 */
final class ReferenceMoveProgram308 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1418,
                                                        3,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        3,
                                                        14,
                                                        2166.6667f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(3, 0, 0.0f, 960.0f))
                                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.9375f, 0.075f))
                                        .onStart(owner.preloadParticleEffect(475)),
                                owner.emitParticles(-1, 475, 3, 9, 8, 0.5f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1450,
                                        1,
                                        14,
                                        0.0f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        14,
                                        1666.6666f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1407,
                                        2,
                                        14,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        2,
                                        14,
                                        1666.6666f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 0, 0.0f, 800.0f))
                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 800.0f))
                        .append(owner.adjustSoundTrack(1, 1, 1120.0f, 480.0f))
                        .append(owner.adjustSoundTrack(2, 1, 1120.0f, 480.0f))
                        .append(owner.launchParticles(475, 0, 9, 11, 0.25f, 0.0f))
                        .append(owner.launchParticles(475, 1, 9, 11, 0.25f, 0.0f))
                        .append(owner.launchParticles(475, 2, 9, 11, 0.25f, 0.0f))
                        .onStart(owner.loadBattleBackground(30));
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, false);
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenOuterParallel(
                                        animationStep8
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction2))
                                                .append(
                                                        owner.tintEnvironment(
                                                                4, 0, 0.9375f, 0.0f, 0.025f))
                                                .append(
                                                        owner.scrollBattleBackground(
                                                                4, -32, 0, 1.6f))
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction3))
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction4))
                                                .beginSequence()
                                                .waitSeconds(0.4f)
                                                .beginParallel()
                                                .append(owner.restoreCamera(0.4f))
                                                .onStart(owner.setHealthHudHidden(14, true))
                                                .onStart(owner.setHealthHudHidden(16, true))
                                                .append(owner.setSpriteAnimationPaused(16, 1)),
                                        owner.shakeCombatant(
                                                16, 2, 8, 0.016f, 0.032f, -0.5f, 0.0f)),
                                owner.tintEnvironment(4, 0, 0.0f, 0.9375f, 0.05f))
                        .onStart(owner.setHealthHudHidden(14, false))
                        .onStart(owner.setHealthHudHidden(16, false))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                        .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
