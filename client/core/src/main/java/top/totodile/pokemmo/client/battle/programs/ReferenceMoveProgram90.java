package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 地裂（招式 ID：90）的攻击／事件阶段动画。
 *
 * <p>招式描述：让对手掉落于地裂的裂缝中进行攻击. 只要命中就会一击濒死.
 *
 * <p>动画说明：播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵位移；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110090（名称）和 120090（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/FissureAnimation.java
 *
 * <p>源文件 SHA-256：0f9dc76a85f5fd8c6d0231af9fea473ecd8b971a70a65a2016d513b34cb5be38
 */
final class ReferenceMoveProgram90 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction3 = owner.sceneFlag(1, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(2, true);
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                        ReferenceMoveTimeline
                                                                .appendThenDelayedParallel(
                                                                        ReferenceMoveTimeline
                                                                                .appendThenNextParallel(
                                                                                        ReferenceMoveTimeline
                                                                                                .waitThenParallel(
                                                                                                        0.6f)
                                                                                                .append(
                                                                                                        owner
                                                                                                                .setSpriteAnimationPaused(
                                                                                                                        14,
                                                                                                                        1)),
                                                                                        owner
                                                                                                .tintEnvironment(
                                                                                                        2,
                                                                                                        0,
                                                                                                        0.0f,
                                                                                                        1.0f,
                                                                                                        0.025f))
                                                                                .append(
                                                                                        owner
                                                                                                .tintEnvironment(
                                                                                                        3,
                                                                                                        0,
                                                                                                        1.0f,
                                                                                                        0.0f,
                                                                                                        0.05f))
                                                                                .onStart(
                                                                                        owner
                                                                                                .loadBattleBackground(
                                                                                                        12))
                                                                                .append(
                                                                                        owner
                                                                                                .scrollBattleBackground(
                                                                                                        2,
                                                                                                        8,
                                                                                                        0,
                                                                                                        0.032f))
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
                                                                                                        sceneFlagAction4))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1417,
                                                                                                        1,
                                                                                                        16,
                                                                                                        0.0f,
                                                                                                        0.9375f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1376,
                                                                                                        1,
                                                                                                        16,
                                                                                                        2333.3333f,
                                                                                                        0.0f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1418,
                                                                                                        3,
                                                                                                        16,
                                                                                                        0.0f,
                                                                                                        0.859375f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1376,
                                                                                                        3,
                                                                                                        16,
                                                                                                        2333.3333f,
                                                                                                        0.0f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1488,
                                                                                                        2,
                                                                                                        16,
                                                                                                        666.6667f,
                                                                                                        0.9375f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1488,
                                                                                                        2,
                                                                                                        16,
                                                                                                        1000.0f,
                                                                                                        0.9375f,
                                                                                                        owner.casterHandle)),
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1488,
                                                                                2,
                                                                                16,
                                                                                1333.3334f,
                                                                                0.9375f,
                                                                                owner.casterHandle),
                                                                        0.4f)
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                257))
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                258)),
                                                        owner.emitParticles(
                                                                -1, 258, 0, 11, 8, -0.25f),
                                                        0.8f)
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 257, 0, 11, 8, 0.0f))
                                                .append(owner.setSpriteAnimationPaused(16, 1)),
                                        owner.shakeCombatant(
                                                16, 2, 14, 0.032f, 0.016f, 0.30004883f, 0.0f)),
                                owner.tintEnvironment(3, 0, 0.0f, 1.0f, 0.05f))
                        .append(owner.tintEnvironment(2, 0, 1.0f, 0.0f, 0.025f));
        Runnable sceneFlagAction5 = owner.sceneFlag(1, true);
        Runnable sceneFlagAction6 = owner.sceneFlag(0, true);
        result =
                animationStep10
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
