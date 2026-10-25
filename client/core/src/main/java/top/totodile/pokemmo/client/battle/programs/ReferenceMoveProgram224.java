package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 超级角击（招式 ID：224）的攻击／事件阶段动画。
 *
 * <p>招式描述：用坚硬且华丽的角狠狠地刺入对手进行攻击.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110224（名称）和 120224（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/MegahornAnimation.java
 *
 * <p>源文件 SHA-256：26de4f845dc2b43f80566d29c59c50105e57e32455a4fe6c56a95c62803a96c4
 */
final class ReferenceMoveProgram224 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        14, 1))
                                                        .append(
                                                                owner.shakeCombatant(
                                                                        14,
                                                                        2,
                                                                        6,
                                                                        0.032f,
                                                                        0.016f,
                                                                        0.30004883f,
                                                                        0.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1450,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1376,
                                                                        1,
                                                                        14,
                                                                        833.3333f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 0, 0.0f, 800.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1407,
                                                                        2,
                                                                        14,
                                                                        666.6667f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1376,
                                                                        2,
                                                                        14,
                                                                        1166.6666f,
                                                                        0.0f,
                                                                        owner.casterHandle)),
                                                owner.adjustSoundTrack(2, 0, 640.0f, 480.0f))
                                        .append(
                                                owner.shakeCombatant(
                                                        14, 2, 1, 0.016f, 0.064f, 2.0f, 0.0f))
                                        .append(owner.focusCamera(1, 0.55f))
                                        .append(owner.tintEnvironment(2, 0, 0.0f, 0.8125f, 0.05f))
                                        .onStart(owner.preloadParticleEffect(390))
                                        .append(owner.launchParticles(390, 3, 9, 11, 0.5f, 192.0f)),
                                owner.launchParticles(390, 4, 9, 11, 0.5f, 192.0f),
                                0.32f)
                        .onStart(owner.loadBattleBackground(1))
                        .append(owner.scrollBattleBackground(2, 0, 4, 0.016f));
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, false);
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        animationStep5
                                .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                                .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                                .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1475,
                                                1,
                                                16,
                                                166.66667f,
                                                0.9375f,
                                                owner.casterHandle))
                                .append(owner.emitParticles(-1, 390, 0, 11, 8, 0.5f))
                                .append(owner.emitParticles(-1, 390, 1, 11, 8, 0.5f))
                                .append(owner.emitParticles(-1, 390, 2, 11, 8, 0.5f))
                                .append(owner.setSpriteAnimationPaused(16, 1)),
                        owner.shakeCombatant(16, 2, 6, 0.032f, 0.016f, 0.30004883f, 0.0f));
        Runnable sceneFlagAction5 = owner.sceneFlag(1, true);
        Runnable sceneFlagAction6 = owner.sceneFlag(0, true);
        result =
                animationStep9
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintEnvironment(2, 0, 0.8125f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
