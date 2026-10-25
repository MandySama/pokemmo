package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 冰冻伏特（招式 ID：553）的攻击／事件阶段动画。
 *
 * <p>招式描述：用覆盖着电流的冰块, 在第2回合撞向对手. 有时会让对手陷入麻痹状态.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110553（名称）和 120553（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/FreezeShockAnimation.java
 *
 * <p>源文件 SHA-256：aa3d9ad0eef3ce037f95bc10e3c95dff4887fb021773fba4fce65003c183417e
 */
final class ReferenceMoveProgram553 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .onStart(owner.loadBattleBackground(20))
                                                        .onStart(
                                                                ReferenceMoveTimeline.callback(
                                                                        sceneFlagAction2))
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        14, 1)),
                                                owner.tintEnvironment(4, 0, 0.0f, 1.0f, 0.075f))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                                        .onStart(owner.preloadParticleEffect(721))
                                        .onStart(owner.preloadParticleEffect(722))
                                        .onStart(owner.preloadParticleEffect(723))
                                        .append(owner.emitParticles(-1, 722, 1, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 723, 1, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 721, 3, 9, 8, 0.5f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1522,
                                                        0,
                                                        14,
                                                        250.0f,
                                                        0.546875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1488,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1707,
                                                        2,
                                                        14,
                                                        1166.6666f,
                                                        0.546875f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 0, 1120.0f, 480.0f)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        2,
                                        14,
                                        1666.6666f,
                                        0.0f,
                                        owner.casterHandle),
                                1.2f)
                        .append(owner.focusCamera(1, 0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(owner.launchParticles(721, 0, 9, 11, 0.5f, 240.0f))
                        .append(owner.launchParticles(722, 2, 9, 11, 0.5f, 240.0f))
                        .append(owner.launchParticles(723, 2, 9, 11, 0.5f, 240.0f));
        Runnable sceneFlagAction4 = owner.sceneFlag(1, false);
        ReferenceMoveTimeline animationStep17 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep8
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(1.6f)
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1521,
                                                        1,
                                                        16,
                                                        166.66667f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1521,
                                                        1,
                                                        16,
                                                        500.0f,
                                                        0.625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1521,
                                                        1,
                                                        16,
                                                        1000.0f,
                                                        0.4296875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1908,
                                                        2,
                                                        16,
                                                        166.66667f,
                                                        0.390625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1748,
                                                        3,
                                                        16,
                                                        0.0f,
                                                        0.390625f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(3, 0, 0.0f, 1600.0f))
                                        .append(owner.adjustSoundTrack(3, 1, 1120.0f, 320.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        3,
                                                        16,
                                                        1666.6666f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.tintEnvironment(3, 0, 1.0f, 0.0f, 0.075f))
                                        .append(owner.scrollBattleBackground(2, 0, 8, 0.032f))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        3,
                                                        3,
                                                        0.0f,
                                                        0.032f,
                                                        0.30004883f,
                                                        -0.30004883f))
                                        .append(owner.emitParticles(-1, 721, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 722, 0, 11, 8, 0.5f)),
                                owner.emitParticles(-1, 721, 2, 11, 8, 0.5f)),
                        owner.tintEnvironment(3, 0, 0.0f, 1.0f, 0.075f));
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        result =
                animationStep17
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintEnvironment(2, 0, 1.0f, 0.0f, 0.075f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
