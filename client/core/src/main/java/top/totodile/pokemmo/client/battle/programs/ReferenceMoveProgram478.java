package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 魔法空间（招式 ID：478）的攻击／事件阶段动画。
 *
 * <p>招式描述：制造出离奇的空间. 在5回合内所有宝可梦携带道具的效果都会消失.
 *
 * <p>动画说明：切换战斗背景；滚动战斗背景；调整场景着色；执行精灵位移；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110478（名称）和 120478（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/MagicRoomAnimation.java
 *
 * <p>源文件 SHA-256：3946374e07fc9aefa949cbc38b0e68858ee3f0538abb201521bed706ec735590
 */
final class ReferenceMoveProgram478 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, false);
        ReferenceMoveTimeline animationStep3 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .onStart(owner.loadBattleBackground(41))
                                                        .onStart(
                                                                ReferenceMoveTimeline.callback(
                                                                        sceneFlagAction2)),
                                                owner.tintEnvironment(4, 0, 0.0f, 0.9375f, 0.05f))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1884,
                                                        1,
                                                        2,
                                                        0.0f,
                                                        0.546875f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 1, 1280.0f, 480.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1455,
                                                        2,
                                                        2,
                                                        0.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(owner.tintEnvironment(3, 0, 0.9375f, 0.0f, 0.05f))
                                        .onStart(owner.setHealthHudHidden(18, true))
                                        .append(owner.scrollBattleBackground(1, 0, -24, 1.28f))
                                        .append(owner.setSpriteAnimationPaused(18, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        18, 2, 40, 0.0f, 0.016f, -1.0f, -1.0f)),
                                owner.shakeCombatant(18, 2, 1, 0.0f, 0.96f, 0.5f, 0.0f))
                        .append(owner.tintEnvironment(3, 0, 0.0f, 0.9375f, 0.05f))
                        .append(owner.tintEnvironment(2, 0, 0.9375f, 0.0f, 0.05f));
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        result =
                animationStep3
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                        .append(owner.setSpriteAnimationPaused(18, 0))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
