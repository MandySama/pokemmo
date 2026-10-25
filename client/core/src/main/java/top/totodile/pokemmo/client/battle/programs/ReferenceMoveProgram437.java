package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 飞叶风暴（招式 ID：437）的攻击／事件阶段动画。
 *
 * <p>招式描述：用尖尖的叶片向对手卷起风暴. 使用之后因为反作用力自己的特攻会大幅降低.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110437（名称）和 120437（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/LeafStormAnimation.java
 *
 * <p>源文件 SHA-256：378b1ea8a7ba1633ec56331279e5dc589aab81d90bda7000f39444bd0ffcc9bd
 */
final class ReferenceMoveProgram437 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, false);
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.appendThenNextParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        0, 0.25f))
                                                                        .onStart(
                                                                                owner
                                                                                        .loadBattleBackground(
                                                                                                9))
                                                                        .onStart(
                                                                                ReferenceMoveTimeline
                                                                                        .callback(
                                                                                                sceneFlagAction2)),
                                                                owner.tintEnvironment(
                                                                        4, 0, 0.0f, 0.9375f,
                                                                        0.075f))
                                                        .append(
                                                                owner.scrollBattleBackground(
                                                                        4, -36, 0, 1.6f))
                                                        .onStart(
                                                                ReferenceMoveTimeline.callback(
                                                                        sceneFlagAction3))
                                                        .onStart(
                                                                ReferenceMoveTimeline.callback(
                                                                        sceneFlagAction4))
                                                        .append(
                                                                owner.tintEnvironment(
                                                                        4, 0, 0.9375f, 0.0f,
                                                                        0.075f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1497,
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
                                                                        833.3333f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 0, 0.0f, 640.0f))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 1, 480.0f, 160.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1759,
                                                                        2,
                                                                        14,
                                                                        500.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1423,
                                                                        2,
                                                                        16,
                                                                        1000.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1423,
                                                                        2,
                                                                        16,
                                                                        1250.0f,
                                                                        0.390625f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1423,
                                                                        2,
                                                                        16,
                                                                        1500.0f,
                                                                        0.078125f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(612))
                                                        .append(
                                                                owner.launchParticles(
                                                                        612, 0, 9, 11, 0.5f, 0.0f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 612, 1, 9, 8, 0.5f)),
                                                owner.emitParticles(-1, 612, 3, 9, 8, 0.5f),
                                                0.8f)
                                        .append(owner.focusCamera(1, 0.25f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(owner.emitParticles(-1, 612, 2, 11, 8, 0.5f)),
                                owner.scaleCombatant(
                                        16, 2, 4, 0.016f, 0.032f, 0.19995117f, -0.19995117f)),
                        owner.tintEnvironment(4, 0, 0.0f, 0.9375f, 0.025f));
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        result =
                ReferenceMoveTimeline.appendAndClose(
                        animationStep10
                                .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                                .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                                .endGroup()
                                .beginParallel()
                                .append(owner.setSpriteAnimationPaused(16, 0))
                                .append(owner.restoreCamera(0.3f)),
                        owner.tintEnvironment(4, 0, 0.9375f, 0.0f, 0.025f));
        return result;
    }
}
