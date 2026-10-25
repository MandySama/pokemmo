package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 精神击破（招式 ID：540）的攻击／事件阶段动画。
 *
 * <p>招式描述：将神奇的念波实体化攻击对手. 给予物理伤害.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；切换战斗背景；调整场景着色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110540（名称）和 120540（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/PsystrikeAnimation.java
 *
 * <p>源文件 SHA-256：d208510956319b81bb06fd8613f01b484a9708b7597c692beacba65ea3c98cc4
 */
final class ReferenceMoveProgram540 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        ReferenceMoveTimeline animationStep13 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .onStart(owner.loadBattleBackground(2))
                                                        .onStart(
                                                                ReferenceMoveTimeline.callback(
                                                                        sceneFlagAction2))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1590,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .onStart(owner.preloadParticleEffect(703))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 703, 5, 9, 8, 0.5f))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        14, 1)),
                                                owner.shakeCombatant(
                                                        14, 2, 10, 0.0f, 0.032f, 0.30004883f, 0.0f),
                                                0.6f)
                                        .append(owner.focusCamera(1, 0.4f)),
                                owner.tintEnvironment(4, 0, 0.0f, 1.0f, 0.075f),
                                1.0f)
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1623,
                                        2,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1906,
                                        2,
                                        16,
                                        666.6667f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        16,
                                        1166.6666f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        1283.3334f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        16,
                                        1400.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        1516.6666f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        16,
                                        1666.6666f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        1750.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.emitParticles(-1, 703, 1, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 703, 0, 11, 8, 0.5f))
                        .onStart(owner.setHealthHudHidden(18, true))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(1.2f)
                        .beginParallel();
        Runnable sceneFlagAction3 = owner.sceneFlag(1, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(0, false);
        ReferenceMoveTimeline animationStep16 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                animationStep13
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                                        .append(owner.emitParticles(-1, 703, 4, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 703, 2, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 703, 3, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(2.0f)
                                        .beginParallel()
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        5,
                                                        0.016f,
                                                        0.032f,
                                                        0.19995117f,
                                                        -0.19995117f)),
                                owner.tintEnvironment(4, 0, 1.0f, 0.0f, 0.075f),
                                2.4f),
                        owner.tintEnvironment(4, 0, 0.0f, 1.0f, 0.075f));
        Runnable sceneFlagAction5 = owner.sceneFlag(1, true);
        result =
                animationStep16
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(0, true)))
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintEnvironment(4, 0, 1.0f, 0.0f, 0.075f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
