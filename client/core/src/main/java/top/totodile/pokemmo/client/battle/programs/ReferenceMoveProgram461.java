package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 新月舞（招式 ID：461）的攻击／事件阶段动画。
 *
 * <p>招式描述：虽然自己陷入濒死, 但可以治愈后备上场的宝可梦的全部状态.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110461（名称）和 120461（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/LunarDanceAnimation.java
 *
 * <p>源文件 SHA-256：6a44fffbff936dc4051bdc42acbcf4160810a7db29825f9ff8451d5d2b99b310
 */
final class ReferenceMoveProgram461 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, false);
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .onStart(owner.loadBattleBackground(37))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.9375f, 0.025f))
                                        .append(owner.focusCamera(0, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                                        .endGroup()
                                        .beginParallel()
                                        .append(owner.tintEnvironment(4, 0, 0.9375f, 0.0f, 0.025f))
                                        .append(owner.scrollBattleBackground(4, 0, 2, 1.28f))
                                        .onStart(owner.preloadParticleEffect(636))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1471,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1358,
                                                        2,
                                                        14,
                                                        166.66667f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1358,
                                                        2,
                                                        14,
                                                        500.0f,
                                                        0.46875f,
                                                        owner.casterHandle))
                                        .append(owner.emitParticles(-1, 636, 0, 9, 8, 0.0f))
                                        .append(owner.emitParticles(-1, 636, 1, 9, 8, 0.0f))
                                        .append(owner.emitParticles(-1, 636, 2, 9, 8, 0.07501221f)),
                                owner.emitParticles(-1, 636, 3, 9, 8, 0.25f)),
                        owner.tintEnvironment(4, 0, 0.0f, 0.9375f, 0.025f));
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        result =
                animationStep8
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintEnvironment(4, 0, 0.9375f, 0.0f, 0.025f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
