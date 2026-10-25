package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 蛮力（招式 ID：276）的攻击／事件阶段动画。
 *
 * <p>招式描述：发挥惊人的力量攻击对手. 自己的攻击和防御会降低.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110276（名称）和 120276（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/SuperpowerAnimation.java
 *
 * <p>源文件 SHA-256：3018c175bab4defb1dab6151092fe78da56f362c09311acb7d14933296a1ec2c
 */
final class ReferenceMoveProgram276 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep13 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .onStart(owner.setHealthHudHidden(18, true))
                                        .append(owner.focusCamera(0, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1418,
                                                        3,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        3,
                                                        14,
                                                        1666.6666f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(3, 0, 0.0f, 1600.0f))
                                        .append(owner.adjustSoundTrack(3, 1, 1120.0f, 480.0f))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .append(
                                                owner.shakeCombatant(
                                                        14,
                                                        2,
                                                        10,
                                                        0.016f,
                                                        0.032f,
                                                        0.19995117f,
                                                        0.0f))
                                        .onStart(owner.preloadParticleEffect(442))
                                        .append(owner.emitParticles(-1, 442, 2, 9, 8, 0.5f)),
                                owner.emitParticles(-1, 442, 3, 9, 8, 0.5f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        2,
                                        14,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintEnvironment(2, 0, 0.0f, 0.8125f, 0.05f))
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .beginParallel()
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
                                        (short) 1424,
                                        2,
                                        16,
                                        0.0f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        16,
                                        366.66666f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        750.0f,
                                        0.46875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        1083.3334f,
                                        0.234375f,
                                        owner.casterHandle))
                        .append(owner.emitParticles(-1, 442, 0, 11, 8, 0.5f))
                        .onStart(owner.loadBattleBackground(1))
                        .append(owner.emitParticles(-1, 442, 1, 11, 8, 0.5f))
                        .append(owner.scrollBattleBackground(2, 0, 4, 0.016f));
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(1, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction5 = owner.sceneFlag(1, true);
        Runnable sceneFlagAction6 = owner.sceneFlag(0, true);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenOuterParallel(
                                        animationStep13
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction2))
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction3))
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction4))
                                                .append(owner.setSpriteAnimationPaused(16, 1))
                                                .beginSequence()
                                                .waitSeconds(0.12f)
                                                .beginParallel(),
                                        owner.scaleCombatant(
                                                16,
                                                2,
                                                2,
                                                0.016f,
                                                0.032f,
                                                -0.19995117f,
                                                0.19995117f)),
                                owner.tintEnvironment(3, 0, 0.0f, 1.0f, 0.05f))
                        .append(owner.tintEnvironment(2, 0, 1.0f, 0.0f, 0.025f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
