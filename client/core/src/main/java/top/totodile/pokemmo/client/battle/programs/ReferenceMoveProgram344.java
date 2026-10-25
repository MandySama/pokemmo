package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 伏特攻击（招式 ID：344）的攻击／事件阶段动画。
 *
 * <p>招式描述：让电流覆盖全身猛撞向对手. 自己也会受到不小的伤害. 有时会让对手陷入麻痹状态.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110344（名称）和 120344（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/VoltTackleAnimation.java
 *
 * <p>源文件 SHA-256：fc14d9350c1e053ec9ae9933db7395eac35f65f80f8e89548013ee21ecf09441
 */
final class ReferenceMoveProgram344 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep13 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenDelayedParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .sequence()
                                                                                        .beginParallel()
                                                                                        .append(
                                                                                                owner
                                                                                                        .focusCamera(
                                                                                                                0,
                                                                                                                0.4f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .setSpriteAnimationPaused(
                                                                                                                14,
                                                                                                                1))
                                                                                        .append(
                                                                                                owner
                                                                                                        .tintEnvironment(
                                                                                                                2,
                                                                                                                0,
                                                                                                                0.0f,
                                                                                                                0.8125f,
                                                                                                                0.05f))
                                                                                        .onStart(
                                                                                                owner
                                                                                                        .preloadParticleEffect(
                                                                                                                517))
                                                                                        .append(
                                                                                                owner
                                                                                                        .emitParticles(
                                                                                                                -1,
                                                                                                                517,
                                                                                                                0,
                                                                                                                9,
                                                                                                                8,
                                                                                                                0.5f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .emitParticles(
                                                                                                                -1,
                                                                                                                517,
                                                                                                                1,
                                                                                                                9,
                                                                                                                8,
                                                                                                                0.5f)),
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1522,
                                                                                        1,
                                                                                        14,
                                                                                        166.66667f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle),
                                                                                1.4f)
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 517, 3,
                                                                                        9, 8,
                                                                                        0.25f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1427,
                                                                                        1,
                                                                                        14,
                                                                                        0.0f,
                                                                                        0.78125f,
                                                                                        owner.casterHandle)),
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1458,
                                                                        2,
                                                                        14,
                                                                        0.0f,
                                                                        0.78125f,
                                                                        owner.casterHandle),
                                                                1.8f)
                                                        .append(owner.focusCamera(1, 0.8f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 517, 4, 0, 0, 0.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1427,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.78125f,
                                                                        owner.casterHandle)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1458,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.78125f,
                                                        owner.casterHandle),
                                                2.2f)
                                        .append(owner.emitParticles(-1, 517, 3, 11, 8, 0.25f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1427,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.78125f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1458,
                                        2,
                                        14,
                                        0.0f,
                                        0.78125f,
                                        owner.casterHandle),
                                2.6f)
                        .onStart(owner.loadBattleBackground(20))
                        .append(owner.scrollBattleBackground(2, 0, 8, 0.016f));
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(1, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(0, false);
        ReferenceMoveTimeline animationStep18 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep13
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1849,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1885,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1885,
                                                        2,
                                                        16,
                                                        166.66667f,
                                                        0.390625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1885,
                                                        2,
                                                        16,
                                                        333.33334f,
                                                        0.15625f,
                                                        owner.casterHandle))
                                        .append(owner.emitParticles(-1, 517, 2, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.shakeCombatant(16, 2, 6, 0.032f, 0.016f, 0.39990234f, 0.0f))
                        .append(owner.tintEnvironment(2, 0, 0.8125f, 0.0f, 0.05f));
        Runnable sceneFlagAction5 = owner.sceneFlag(1, true);
        Runnable sceneFlagAction6 = owner.sceneFlag(0, true);
        result =
                animationStep18
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
