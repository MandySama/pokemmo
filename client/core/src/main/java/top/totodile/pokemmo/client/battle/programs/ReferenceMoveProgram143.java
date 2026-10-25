package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 神鸟猛击（招式 ID：143）的攻击／事件阶段动画。
 *
 * <p>招式描述：第2回合攻击对手. 偶尔使对手畏缩. 也容易击中要害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵缩放；控制精灵显隐；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110143（名称）和 120143（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SkyAttackAnimation.java
 *
 * <p>源文件 SHA-256：545c543a8af59c7e8d0efd8e01d2b2dc38c9227a81a058fa1ad75a8dbb5a3eef
 */
final class ReferenceMoveProgram143 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.waitThenParallel(0.6f)
                        .onStart(owner.loadBattleBackground(30))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                        .append(owner.tintEnvironment(4, 0, 0.0f, 1.0f, 0.025f))
                        .append(owner.setCombatantVisibility(14, 3))
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintEnvironment(3, 0, 1.0f, 0.0f, 0.05f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1505,
                                        1,
                                        14,
                                        166.66667f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1510,
                                        2,
                                        2,
                                        166.66667f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        2,
                                        2,
                                        1333.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 1, 800.0f, 480.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1537,
                                        1,
                                        16,
                                        750.0f,
                                        0.546875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1424,
                                        2,
                                        16,
                                        750.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(309));
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, false);
        ReferenceMoveTimeline animationStep11 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                        animationStep6
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction3))
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction4))
                                                .append(
                                                        owner.scrollBattleBackground(
                                                                4, 30, 0, 1.44f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 309, 2, 9, 11, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 309, 4, 9, 11, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 309, 5, 9, 8, 0.5f)),
                                        owner.emitParticles(-1, 309, 1, 9, 8, 0.5f),
                                        0.4f)
                                .append(owner.focusCamera(1, 0.4f))
                                .endGroup()
                                .endGroup()
                                .beginSequence()
                                .waitSeconds(0.6f)
                                .beginParallel()
                                .append(owner.emitParticles(-1, 309, 0, 11, 8, 0.5f))
                                .append(owner.setSpriteAnimationPaused(16, 1))
                                .append(
                                        owner.scaleCombatant(
                                                16,
                                                2,
                                                1,
                                                0.032f,
                                                0.064f,
                                                0.19995117f,
                                                -0.19995117f)),
                        owner.tintEnvironment(3, 0, 0.0f, 1.0f, 0.025f));
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        result =
                animationStep11
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                        .append(owner.tintEnvironment(2, 0, 1.0f, 0.0f, 0.025f))
                        .append(owner.setCombatantVisibility(14, 4))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
