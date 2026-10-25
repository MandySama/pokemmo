package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 终极冲击（招式 ID：416）的攻击／事件阶段动画。
 *
 * <p>招式描述：使出自己浑身力量突击对手. 下一回合自己将无法动弹.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110416（名称）和 120416（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/GigaImpactAnimation.java
 *
 * <p>源文件 SHA-256：073655f23d645c9264e6f6b43fd0942e005e8f46dffcb3a36de70a442937048b
 */
final class ReferenceMoveProgram416 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep14 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.3f)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1688,
                                        1,
                                        14,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1418,
                                        3,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        3,
                                        16,
                                        1000.0f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(3, 0, 0.0f, 640.0f))
                        .append(owner.adjustSoundTrack(3, 1, 640.0f, 320.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        16,
                                        1000.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        16,
                                        1166.6666f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        16,
                                        1500.0f,
                                        0.390625f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        16,
                                        1833.3334f,
                                        0.078125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1505,
                                        2,
                                        16,
                                        1000.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(591))
                        .append(owner.emitParticles(-1, 591, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 591, 1, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 591, 2, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 591, 3, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 591, 4, 11, 8, 0.5f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .beginSequence()
                        .waitSeconds(0.8f)
                        .beginParallel()
                        .onStart(owner.loadBattleBackground(2))
                        .append(owner.tintEnvironment(2, 0, 0.0f, 0.8125f, 0.025f))
                        .append(owner.scrollBattleBackground(2, 0, 4, 0.016f));
        Runnable sceneFlagAction2 = owner.sceneFlag(1, false);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction5 = owner.sceneFlag(1, true);
        Runnable sceneFlagAction6 = owner.sceneFlag(0, true);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep14
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(1.0f)
                                        .beginParallel(),
                                owner.scaleCombatant(
                                        16, 2, 4, 0.016f, 0.032f, -0.19995117f, 0.19995117f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .append(owner.tintEnvironment(2, 0, 0.8125f, 0.0f, 0.05f))
                        .append(owner.restoreCamera(0.3f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
