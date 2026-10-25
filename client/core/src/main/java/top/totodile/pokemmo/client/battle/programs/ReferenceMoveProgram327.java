package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 冲天拳（招式 ID：327）的攻击／事件阶段动画。
 *
 * <p>招式描述：用冲向天空般高高的上勾拳顶起对手进行攻击.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110327（名称）和 120327（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/SkyUppercutAnimation.java
 *
 * <p>源文件 SHA-256：4ba02d57e9f114579113a7bf22d59358c1b59180e67f2f0406b738f871b420a9
 */
final class ReferenceMoveProgram327 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(1, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(0, false);
        ReferenceMoveTimeline animationStep11 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.appendThenNextParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        1, 0.4f))
                                                                        .onStart(
                                                                                owner
                                                                                        .loadBattleBackground(
                                                                                                26))
                                                                        .onStart(
                                                                                ReferenceMoveTimeline
                                                                                        .callback(
                                                                                                sceneFlagAction2))
                                                                        .endGroup()
                                                                        .beginParallel(),
                                                                owner.tintEnvironment(
                                                                        4, 0, 0.0f, 0.9375f,
                                                                        0.025f))
                                                        .append(
                                                                owner.scrollBattleBackground(
                                                                        4, 0, 24, 1.12f))
                                                        .onStart(
                                                                ReferenceMoveTimeline.callback(
                                                                        sceneFlagAction3))
                                                        .onStart(
                                                                ReferenceMoveTimeline.callback(
                                                                        sceneFlagAction4))
                                                        .append(
                                                                owner.tintEnvironment(
                                                                        4, 0, 0.9375f, 0.0f,
                                                                        0.025f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1418,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.78125f,
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
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 0, 0.0f, 1120.0f))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 1, 800.0f, 480.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1475,
                                                                        2,
                                                                        16,
                                                                        333.33334f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1475,
                                                                        2,
                                                                        16,
                                                                        666.6667f,
                                                                        0.390625f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1475,
                                                                        2,
                                                                        16,
                                                                        1000.0f,
                                                                        0.234375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1475,
                                                                        2,
                                                                        16,
                                                                        1333.3334f,
                                                                        0.078125f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(497))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 497, 0, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 497, 1, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 497, 2, 11, 8, 0.5f)),
                                                owner.emitParticles(-1, 497, 3, 11, 8, 0.5f),
                                                0.4f)
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 4, 0.016f, 0.032f, -0.30004883f, 0.39990234f)),
                        owner.tintEnvironment(4, 0, 0.0f, 0.9375f, 0.05f));
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        result =
                ReferenceMoveTimeline.appendAndClose(
                        animationStep11
                                .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                                .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                                .endGroup()
                                .beginParallel()
                                .append(owner.setSpriteAnimationPaused(16, 0))
                                .append(owner.restoreCamera(0.4f)),
                        owner.tintEnvironment(4, 0, 0.9375f, 0.0f, 0.025f));
        return result;
    }
}
