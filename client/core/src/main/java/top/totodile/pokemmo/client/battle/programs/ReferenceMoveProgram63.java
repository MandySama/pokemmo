package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 破坏光线（招式 ID：63）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手发射强烈的光线进行攻击. 下一回合自己将无法动弹.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110063（名称）和 120063（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/HyperBeamAnimation.java
 *
 * <p>源文件 SHA-256：16c1dabf7990cb1cbed33ec357b3c23de5867f2533b8c0cce910ee1adebb7def
 */
final class ReferenceMoveProgram63 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep2 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .onStart(owner.loadBattleBackground(9))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1976,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle)),
                                owner.tintEnvironment(2, 0, 0.0f, 1.0f, 0.025f))
                        .append(owner.tintEnvironment(3, 0, 1.0f, 0.0f, 0.025f))
                        .append(owner.focusCamera(0, 0.4f));
        Runnable sceneFlagAction2 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction3 = owner.sceneFlag(1, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(2, true);
        ReferenceMoveTimeline animationStep13 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                animationStep2
                                                                        .onStart(
                                                                                ReferenceMoveTimeline
                                                                                        .callback(
                                                                                                sceneFlagAction2))
                                                                        .onStart(
                                                                                ReferenceMoveTimeline
                                                                                        .callback(
                                                                                                sceneFlagAction3))
                                                                        .onStart(
                                                                                ReferenceMoveTimeline
                                                                                        .callback(
                                                                                                sceneFlagAction4)),
                                                                owner.scrollBattleBackground(
                                                                        4, -30, 0, 2.24f),
                                                                0.12f)
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        14, 1))
                                                        .endGroup()
                                                        .endGroup()
                                                        .beginSequence()
                                                        .waitSeconds(0.52f)
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 1.45f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1424,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9375f,
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
                                                                        (short) 1963,
                                                                        2,
                                                                        14,
                                                                        0.0f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1420,
                                                                        2,
                                                                        16,
                                                                        833.3333f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1420,
                                                                        1,
                                                                        16,
                                                                        916.6667f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1420,
                                                                        2,
                                                                        16,
                                                                        1000.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1420,
                                                                        1,
                                                                        16,
                                                                        1083.3334f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 0, 0.0f, 800.0f))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 1, 1280.0f, 320.0f))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        2, 1, 960.0f, 640.0f))
                                                        .onStart(owner.preloadParticleEffect(224))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 224, 0, 9, 11, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 224, 1, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 224, 2, 9, 8, 0.5f)),
                                                owner.emitParticles(-1, 224, 3, 9, 8, 0.5f),
                                                0.8f),
                                        owner.shakeCombatant(
                                                16, 2, 8, 0.032f, 0.016f, 0.30004883f, 0.0f),
                                        1.96f),
                                owner.tintEnvironment(3, 0, 0.0f, 1.0f, 0.05f))
                        .append(owner.tintEnvironment(2, 0, 1.0f, 0.0f, 0.025f));
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        Runnable sceneFlagAction6 = owner.sceneFlag(1, true);
        result =
                animationStep13
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
