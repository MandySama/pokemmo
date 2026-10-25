package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 舍身冲撞（招式 ID：38）的攻击／事件阶段动画。
 *
 * <p>招式描述：拼命地猛撞向对手进行攻击. 自己也会受到不小的伤害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110038（名称）和 120038（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/DoubleEdgeAnimation.java
 *
 * <p>源文件 SHA-256：9e279e44f510988b91adb7c48ec4f3a28da8777d59de0c56859d55c81afe4d37
 */
final class ReferenceMoveProgram38 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep3 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .waitSeconds(0.6f)
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
                                                                        1666.6666f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 0, 0.0f, 1600.0f)),
                                                owner.adjustSoundTrack(1, 1, 960.0f, 640.0f))
                                        .append(owner.focusCamera(1, 0.4f)),
                                owner.tintEnvironment(2, 0, 0.0f, 1.0f, 0.05f))
                        .append(owner.tintEnvironment(3, 0, 1.0f, 0.0f, 0.05f))
                        .onStart(owner.loadBattleBackground(3));
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, false);
        ReferenceMoveTimeline animationStep10 =
                animationStep3
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1521,
                                        2,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(200))
                        .append(owner.emitParticles(-1, 200, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 200, 1, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 200, 2, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 200, 3, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 200, 4, 11, 8, 0.5f))
                        .append(owner.setSpriteAnimationPaused(16, 1));
        Runnable sceneFlagAction5 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction6 = owner.sceneFlag(0, true);
        Runnable sceneFlagAction7 = owner.sceneFlag(1, true);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                        animationStep10
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction5))
                                                .append(
                                                        owner.scrollBattleBackground(
                                                                2, 0, 10, 0.032f))
                                                .append(
                                                        owner.shakeCombatant(
                                                                16, 2, 1, 0.032f, 0.032f, 1.0f,
                                                                0.0f)),
                                        owner.scaleCombatant(
                                                16,
                                                2,
                                                1,
                                                0.032f,
                                                0.064f,
                                                0.30004883f,
                                                -0.30004883f)),
                                owner.tintEnvironment(3, 0, 0.0f, 1.0f, 0.025f))
                        .append(owner.tintEnvironment(2, 0, 1.0f, 0.0f, 0.025f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction7))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
