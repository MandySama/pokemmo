package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 祈愿（招式 ID：273）的攻击／事件阶段动画。
 *
 * <p>招式描述：使用后, 下回合结束时使用方所在位置的宝可梦回复HP, 回复量为祈愿使用者最大HP的一半.
 *
 * <p>动画说明：播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色。
 *
 * <p>编号对应参考动画注册表；语言条目为 110273（名称）和 120273（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/WishAnimation.java
 *
 * <p>源文件 SHA-256：ca6ed5716677c33ca70a777a86b7f74caf6d9943d7763134aa1c32fe592cca8f
 */
final class ReferenceMoveProgram273 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, false);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .onStart(owner.loadBattleBackground(25))
                                                        .onStart(
                                                                ReferenceMoveTimeline.callback(
                                                                        sceneFlagAction2)),
                                                owner.tintEnvironment(4, 0, 0.0f, 0.8125f, 0.05f))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                                        .endGroup()
                                        .beginParallel()
                                        .append(owner.scrollBattleBackground(1, 0, 32, 0.96f))
                                        .append(owner.tintEnvironment(3, 0, 0.8125f, 0.0f, 0.05f))
                                        .onStart(owner.setHealthHudHidden(14, true))
                                        .onStart(owner.setHealthHudHidden(16, true))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1471,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1473,
                                                        2,
                                                        14,
                                                        750.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(438))
                                        .append(owner.emitParticles(-1, 438, 2, 0, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 438, 3, 0, 8, 0.5f))
                                        .beginSequence()
                                        .waitSeconds(0.4f)
                                        .beginParallel()
                                        .waitSeconds(0.6f)
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(0.8f)
                                        .beginParallel()
                                        .append(owner.emitParticles(-1, 438, 0, 9, 8, 0.4f)),
                                owner.tintEnvironment(3, 0, 0.0f, 0.8125f, 0.075f))
                        .onStart(owner.setHealthHudHidden(14, false))
                        .onStart(owner.setHealthHudHidden(16, false));
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        result =
                animationStep6
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                        .append(owner.tintEnvironment(2, 0, 0.8125f, 0.0f, 0.025f))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
