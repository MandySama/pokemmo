package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 电磁炮（招式 ID：192）的攻击／事件阶段动画。
 *
 * <p>招式描述：发射大炮一样的电流进行攻击. 让对手陷入麻痹状态.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110192（名称）和 120192（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/ZapCannonAnimation.java
 *
 * <p>源文件 SHA-256：2ffb8ef1f17e2436d75e37847ccb76f5a989d87dae5e4d9763b71fd34f7bbfd5
 */
final class ReferenceMoveProgram192 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.waitThenParallel(0.6f)
                        .onStart(owner.preloadParticleEffect(358))
                        .append(owner.tintEnvironment(2, 0, 0.0f, 0.8125f, 0.075f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1458,
                                        1,
                                        14,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1425,
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
                                        1000.0f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 0, 0.0f, 320.0f))
                        .append(owner.launchParticles(358, 0, 9, 11, 0.5f, 360.0f))
                        .append(owner.focusCamera(1, 0.4f))
                        .append(owner.emitParticles(-1, 358, 1, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 358, 2, 11, 8, 0.5f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .beginSequence()
                        .waitSeconds(0.4f)
                        .beginParallel()
                        .onStart(owner.loadBattleBackground(19))
                        .append(owner.scrollBattleBackground(2, 0, 8, 0.016f));
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(1, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(0, false);
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep6
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9765625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1458,
                                                        1,
                                                        16,
                                                        166.66667f,
                                                        0.9375f,
                                                        owner.casterHandle)),
                                owner.scaleCombatant(
                                        16, 2, 7, 0.016f, 0.032f, 0.19995117f, -0.19995117f))
                        .append(owner.tintEnvironment(2, 0, 0.8125f, 0.0f, 0.075f));
        Runnable sceneFlagAction5 = owner.sceneFlag(1, true);
        Runnable sceneFlagAction6 = owner.sceneFlag(0, true);
        result =
                animationStep8
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
