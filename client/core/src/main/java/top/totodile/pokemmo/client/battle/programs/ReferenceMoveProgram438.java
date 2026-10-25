package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 强力鞭打（招式 ID：438）的攻击／事件阶段动画。
 *
 * <p>招式描述：激烈地挥舞青藤或触手摔打对手进行攻击.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110438（名称）和 120438（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/PowerWhipAnimation.java
 *
 * <p>源文件 SHA-256：c48cd2c409213477993a4b075249b9377c8d0ae19710509bed0c8b96bafa509b
 */
final class ReferenceMoveProgram438 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, false);
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .onStart(owner.loadBattleBackground(33))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.8125f, 0.075f))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(owner.scrollBattleBackground(2, 8, 0, 0.032f))
                                        .append(owner.tintEnvironment(3, 0, 0.8125f, 0.0f, 0.025f))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1445,
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
                                                        666.6667f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 640.0f))
                                        .append(owner.adjustSoundTrack(1, 1, 480.0f, 160.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        333.33334f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        583.3333f,
                                                        0.46875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        833.3333f,
                                                        0.15625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        1083.3334f,
                                                        0.0390625f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(613))
                                        .append(owner.emitParticles(-1, 613, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 613, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 613, 2, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 4, 0.016f, 0.032f, 0.19995117f, -0.19995117f)),
                        owner.tintEnvironment(3, 0, 0.0f, 0.8125f, 0.075f));
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        result =
                ReferenceMoveTimeline.appendAndClose(
                        animationStep10
                                .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                                .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                                .append(owner.restoreCamera(0.4f))
                                .append(owner.setSpriteAnimationPaused(16, 0)),
                        owner.tintEnvironment(2, 0, 0.8125f, 0.125f, 0.075f));
        return result;
    }
}
