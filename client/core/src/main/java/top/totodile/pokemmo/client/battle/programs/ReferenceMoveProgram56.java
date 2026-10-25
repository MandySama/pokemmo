package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 水炮（招式 ID：56）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手猛烈地喷射大量水流进行攻击.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110056（名称）和 120056（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/HydroPumpAnimation.java
 *
 * <p>源文件 SHA-256：4228b08f87e40d3e0189ba15377f8745846f68261a996530eafbc7a012f535b9
 */
final class ReferenceMoveProgram56 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, false);
        ReferenceMoveTimeline animationStep14 =
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
                                                                                                                .appendThenDelayedOuterParallel(
                                                                                                                        ReferenceMoveTimeline
                                                                                                                                .appendThenDelayedParallel(
                                                                                                                                        ReferenceMoveTimeline
                                                                                                                                                .appendThenNextParallel(
                                                                                                                                                        ReferenceMoveTimeline
                                                                                                                                                                .sequence()
                                                                                                                                                                .beginParallel()
                                                                                                                                                                .waitSeconds(
                                                                                                                                                                        0.6f)
                                                                                                                                                                .onStart(
                                                                                                                                                                        owner
                                                                                                                                                                                .setHealthHudHidden(
                                                                                                                                                                                        18,
                                                                                                                                                                                        true))
                                                                                                                                                                .endGroup()
                                                                                                                                                                .beginParallel()
                                                                                                                                                                .onStart(
                                                                                                                                                                        owner
                                                                                                                                                                                .loadBattleBackground(
                                                                                                                                                                                        4))
                                                                                                                                                                .onStart(
                                                                                                                                                                        ReferenceMoveTimeline
                                                                                                                                                                                .callback(
                                                                                                                                                                                        sceneFlagAction2)),
                                                                                                                                                        owner
                                                                                                                                                                .tintEnvironment(
                                                                                                                                                                        4,
                                                                                                                                                                        0,
                                                                                                                                                                        0.0f,
                                                                                                                                                                        0.875f,
                                                                                                                                                                        0.025f))
                                                                                                                                                .append(
                                                                                                                                                        owner
                                                                                                                                                                .scrollBattleBackground(
                                                                                                                                                                        4,
                                                                                                                                                                        -20,
                                                                                                                                                                        0,
                                                                                                                                                                        2.08f))
                                                                                                                                                .onStart(
                                                                                                                                                        ReferenceMoveTimeline
                                                                                                                                                                .callback(
                                                                                                                                                                        sceneFlagAction3))
                                                                                                                                                .onStart(
                                                                                                                                                        ReferenceMoveTimeline
                                                                                                                                                                .callback(
                                                                                                                                                                        sceneFlagAction4))
                                                                                                                                                .append(
                                                                                                                                                        owner
                                                                                                                                                                .tintEnvironment(
                                                                                                                                                                        3,
                                                                                                                                                                        0,
                                                                                                                                                                        0.875f,
                                                                                                                                                                        0.375f,
                                                                                                                                                                        0.025f))
                                                                                                                                                .append(
                                                                                                                                                        owner
                                                                                                                                                                .scheduleSound(
                                                                                                                                                                        (byte)
                                                                                                                                                                                2,
                                                                                                                                                                        (short)
                                                                                                                                                                                1706,
                                                                                                                                                                        1,
                                                                                                                                                                        16,
                                                                                                                                                                        0.0f,
                                                                                                                                                                        0.9375f,
                                                                                                                                                                        owner.casterHandle))
                                                                                                                                                .append(
                                                                                                                                                        owner
                                                                                                                                                                .scheduleSound(
                                                                                                                                                                        (byte)
                                                                                                                                                                                2,
                                                                                                                                                                        (short)
                                                                                                                                                                                1417,
                                                                                                                                                                        2,
                                                                                                                                                                        16,
                                                                                                                                                                        0.0f,
                                                                                                                                                                        0.9765625f,
                                                                                                                                                                        owner.casterHandle))
                                                                                                                                                .append(
                                                                                                                                                        owner
                                                                                                                                                                .scheduleSound(
                                                                                                                                                                        (byte)
                                                                                                                                                                                2,
                                                                                                                                                                        (short)
                                                                                                                                                                                1376,
                                                                                                                                                                        2,
                                                                                                                                                                        16,
                                                                                                                                                                        666.6667f,
                                                                                                                                                                        0.0f,
                                                                                                                                                                        owner.casterHandle))
                                                                                                                                                .append(
                                                                                                                                                        owner
                                                                                                                                                                .scheduleSound(
                                                                                                                                                                        (byte)
                                                                                                                                                                                2,
                                                                                                                                                                        (short)
                                                                                                                                                                                1897,
                                                                                                                                                                        1,
                                                                                                                                                                        16,
                                                                                                                                                                        1500.0f,
                                                                                                                                                                        0.9921875f,
                                                                                                                                                                        owner.casterHandle))
                                                                                                                                                .append(
                                                                                                                                                        owner
                                                                                                                                                                .adjustSoundTrack(
                                                                                                                                                                        1,
                                                                                                                                                                        1,
                                                                                                                                                                        1920.0f,
                                                                                                                                                                        480.0f))
                                                                                                                                                .append(
                                                                                                                                                        owner
                                                                                                                                                                .scheduleSound(
                                                                                                                                                                        (byte)
                                                                                                                                                                                2,
                                                                                                                                                                        (short)
                                                                                                                                                                                1376,
                                                                                                                                                                        1,
                                                                                                                                                                        16,
                                                                                                                                                                        2500.0f,
                                                                                                                                                                        0.0f,
                                                                                                                                                                        owner.casterHandle))
                                                                                                                                                .append(
                                                                                                                                                        owner
                                                                                                                                                                .scheduleSound(
                                                                                                                                                                        (byte)
                                                                                                                                                                                2,
                                                                                                                                                                        (short)
                                                                                                                                                                                1475,
                                                                                                                                                                        2,
                                                                                                                                                                        16,
                                                                                                                                                                        1500.0f,
                                                                                                                                                                        0.9375f,
                                                                                                                                                                        owner.casterHandle))
                                                                                                                                                .onStart(
                                                                                                                                                        owner
                                                                                                                                                                .preloadParticleEffect(
                                                                                                                                                                        217))
                                                                                                                                                .append(
                                                                                                                                                        owner
                                                                                                                                                                .emitParticles(
                                                                                                                                                                        -1,
                                                                                                                                                                        217,
                                                                                                                                                                        4,
                                                                                                                                                                        9,
                                                                                                                                                                        8,
                                                                                                                                                                        0.5f)),
                                                                                                                                        owner
                                                                                                                                                .launchParticles(
                                                                                                                                                        217,
                                                                                                                                                        3,
                                                                                                                                                        9,
                                                                                                                                                        11,
                                                                                                                                                        0.5f,
                                                                                                                                                        360.0f),
                                                                                                                                        0.12f),
                                                                                                                        owner
                                                                                                                                .launchParticles(
                                                                                                                                        217,
                                                                                                                                        3,
                                                                                                                                        9,
                                                                                                                                        11,
                                                                                                                                        0.5f,
                                                                                                                                        240.0f),
                                                                                                                        0.24f)
                                                                                                                .append(
                                                                                                                        owner
                                                                                                                                .launchParticles(
                                                                                                                                        217,
                                                                                                                                        2,
                                                                                                                                        9,
                                                                                                                                        11,
                                                                                                                                        0.5f,
                                                                                                                                        1440.0f)),
                                                                                                        owner
                                                                                                                .launchParticles(
                                                                                                                        217,
                                                                                                                        3,
                                                                                                                        9,
                                                                                                                        11,
                                                                                                                        0.5f,
                                                                                                                        240.0f),
                                                                                                        0.36f)
                                                                                                .append(
                                                                                                        owner
                                                                                                                .emitParticles(
                                                                                                                        -1,
                                                                                                                        217,
                                                                                                                        0,
                                                                                                                        11,
                                                                                                                        8,
                                                                                                                        0.5f))
                                                                                                .append(
                                                                                                        owner
                                                                                                                .focusCamera(
                                                                                                                        1,
                                                                                                                        0.4f))
                                                                                                .append(
                                                                                                        owner
                                                                                                                .launchParticles(
                                                                                                                        217,
                                                                                                                        3,
                                                                                                                        9,
                                                                                                                        11,
                                                                                                                        0.5f,
                                                                                                                        240.0f)),
                                                                                        owner
                                                                                                .emitParticles(
                                                                                                        -1,
                                                                                                        217,
                                                                                                        0,
                                                                                                        11,
                                                                                                        8,
                                                                                                        0.5f),
                                                                                        0.48f),
                                                                        owner.launchParticles(
                                                                                217, 3, 9, 11, 0.5f,
                                                                                240.0f),
                                                                        0.6f)
                                                                .append(
                                                                        owner.launchParticles(
                                                                                217, 2, 9, 11, 0.5f,
                                                                                1440.0f))
                                                                .append(
                                                                        owner.launchParticles(
                                                                                217, 3, 9, 11, 0.5f,
                                                                                240.0f)),
                                                        owner.emitParticles(
                                                                -1, 217, 0, 11, 8, 0.5f),
                                                        0.72f)
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 217, 0, 11, 8, 0.5f)),
                                        owner.launchParticles(217, 3, 9, 11, 0.5f, 240.0f),
                                        0.92f),
                                owner.launchParticles(217, 3, 9, 11, 0.5f, 240.0f),
                                1.12f)
                        .append(owner.emitParticles(-1, 217, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 217, 1, 11, 8, 0.5f))
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, 0.30004883f, -0.30004883f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(1.72f)
                        .beginParallel()
                        .append(owner.tintEnvironment(3, 0, 0.375f, 0.875f, 0.05f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(16, 0));
        Runnable sceneFlagAction5 = owner.sceneFlag(1, true);
        result =
                ReferenceMoveTimeline.appendAndClose(
                        animationStep14
                                .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                                .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(0, true)))
                                .endGroup()
                                .beginParallel()
                                .onStart(owner.setHealthHudHidden(18, false)),
                        owner.tintEnvironment(2, 0, 0.875f, 0.0f, 0.025f));
        return result;
    }
}
