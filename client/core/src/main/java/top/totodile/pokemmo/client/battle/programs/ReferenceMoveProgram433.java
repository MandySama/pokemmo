package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 戏法空间（招式 ID：433）的攻击／事件阶段动画。
 *
 * <p>招式描述：制造出离奇的空间. 在5回合内速度慢的宝可梦可以先行动.
 *
 * <p>动画说明：镜头聚焦施术者；切换战斗背景；调整场景着色；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110433（名称）和 120433（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/TrickRoomAnimation.java
 *
 * <p>源文件 SHA-256：457d65f3d4d0b0a438c83eed56e2f963e3d66b57fec17aadf06904fcc5933eee
 */
final class ReferenceMoveProgram433 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(1, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(0, false);
        ReferenceMoveTimeline animationStep2 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.appendThenNextParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(owner.focusCamera(0, 0.6f))
                                                                .endGroup()
                                                                .beginParallel()
                                                                .append(
                                                                        owner
                                                                                .setSpriteAnimationPaused(
                                                                                        14, 1))
                                                                .append(
                                                                        owner
                                                                                .setSpriteAnimationPaused(
                                                                                        16, 1)),
                                                        owner.tintEnvironment(
                                                                4, 0, 0.0f, 0.9375f, 0.05f))
                                                .onStart(owner.loadBattleBackground(31))
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction2))
                                                .endGroup()
                                                .beginParallel()
                                                .append(
                                                        owner.tintEnvironment(
                                                                4, 0, 0.9375f, 0.0f, 0.05f))
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction3))
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction4)),
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1505,
                                                true ? 1 : 0,
                                                14,
                                                0.0f,
                                                0.8984375f,
                                                owner.casterHandle),
                                        1.0f),
                                owner.tintEnvironment(4, 0, 0.0f, 0.9375f, 0.05f))
                        .append(owner.tintEnvironment(4, 0, 0.9375f, 0.0f, 0.05f));
        Runnable sceneFlagAction5 = owner.sceneFlag(1, true);
        Runnable sceneFlagAction6 = owner.sceneFlag(0, true);
        result =
                animationStep2
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .waitSeconds(0.6f)
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
