package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 逆鳞（招式 ID：200）的攻击／事件阶段动画。
 *
 * <p>招式描述：在2-3回合内, 乱打一气地进行攻击. 大闹一番后自己会陷入混乱.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110200（名称）和 120200（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/OutrageAnimation.java
 *
 * <p>源文件 SHA-256：be1f30e257f1fac24d19fe0c9b0a76b11d10fe254ef26e44fe3ffab756849e8c
 */
final class ReferenceMoveProgram200 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.6f)),
                                owner.tintEnvironment(2, 0, 0.0f, 1.0f, 0.025f))
                        .append(owner.tintEnvironment(3, 0, 1.0f, 0.0f, 0.025f))
                        .onStart(owner.setHealthHudHidden(18, true))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1426,
                                        1,
                                        14,
                                        333.33334f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        1,
                                        14,
                                        833.3333f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        1,
                                        14,
                                        916.6667f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        1,
                                        14,
                                        1000.0f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        1,
                                        14,
                                        1083.3334f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1497,
                                        2,
                                        16,
                                        0.0f,
                                        0.625f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        2,
                                        16,
                                        1833.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 1, 1120.0f, 640.0f))
                        .append(owner.adjustSoundTrack(2, 0, 0.0f, 1600.0f))
                        .onStart(owner.loadBattleBackground(21))
                        .append(owner.scrollBattleBackground(1, -1200, 0, 1.92f));
        Runnable sceneFlagAction2 = owner.sceneFlag(2, 1 != 0);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, 0 != 0);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, 0 != 0);
        ReferenceMoveTimeline animationStep10 =
                animationStep8
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                        .onStart(owner.preloadParticleEffect(366))
                        .append(owner.emitParticles(-1, 366, 0, 9, 8, 0.4f))
                        .append(owner.emitParticles(-1, 366, 1, 9, 8, 0.4f));
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep11 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep10
                                        .append(owner.emitParticles(-1, 366, 2, 9, 8, 0.4f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.tintCombatant(
                                                        16, 0.25f, 0.0f, 0.8125f, color)),
                                owner.shakeCombatant(16, 2, 10, 0.016f, 0.032f, 0.5f, 0.0f))
                        .append(owner.tintEnvironment(3, 0, 0.0f, 1.0f, 0.025f));
        color = EncounterMoveAnimation.color(31);
        Runnable sceneFlagAction5 = owner.sceneFlag(1, true);
        Runnable sceneFlagAction6 = owner.sceneFlag(0, true);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep11,
                                owner.tintCombatant(16, 0.25f, 0.8125f, 0.0f, color))
                        .append(owner.tintEnvironment(2, 0, 1.0f, 0.0f, 0.025f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
