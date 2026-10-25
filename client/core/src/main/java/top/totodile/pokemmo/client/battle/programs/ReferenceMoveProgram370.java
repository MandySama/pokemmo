package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 近身战（招式 ID：370）的攻击／事件阶段动画。
 *
 * <p>招式描述：放弃守护, 向对手的怀里突击. 自己的防御和特防会降低.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110370（名称）和 120370（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/CloseCombatAnimation.java
 *
 * <p>源文件 SHA-256：f7c58dbf89a5d877ca9d51bf4088bc4db15aabf8aa67c7640ce0959bc4547c7d
 */
final class ReferenceMoveProgram370 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep2 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1423,
                                        1,
                                        16,
                                        166.66667f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(owner.focusCamera(1, 0.4f))
                        .onStart(owner.loadBattleBackground(29));
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, false);
        ReferenceMoveTimeline animationStep15 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                animationStep2.onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction2)),
                                                owner.tintEnvironment(4, 0, 0.0f, 0.9375f, 0.025f))
                                        .append(owner.scrollBattleBackground(4, 24, 0, 1.44f))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                                        .append(owner.tintEnvironment(3, 0, 0.8125f, 0.0f, 0.025f))
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
                                                        (short) 1475,
                                                        1,
                                                        16,
                                                        333.33334f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        1,
                                                        16,
                                                        500.0f,
                                                        0.46875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        1,
                                                        16,
                                                        666.6667f,
                                                        0.3125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        1,
                                                        16,
                                                        833.3333f,
                                                        0.078125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        166.66667f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        250.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        333.33334f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        416.66666f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        500.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(545))
                                        .append(owner.emitParticles(-1, 545, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 545, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 545, 2, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.shakeCombatant(
                                                        16,
                                                        2,
                                                        16,
                                                        0.016f,
                                                        0.016f,
                                                        0.19995117f,
                                                        0.0f)),
                                owner.scaleCombatant(
                                        16, 2, 4, 0.016f, 0.032f, -0.19995117f, 0.19995117f)),
                        owner.tintEnvironment(3, 0, 0.0f, 0.9375f, 0.025f));
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        result =
                ReferenceMoveTimeline.appendAndClose(
                        animationStep15
                                .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                                .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                                .append(owner.setSpriteAnimationPaused(16, 0))
                                .append(owner.restoreCamera(0.4f)),
                        owner.tintEnvironment(2, 0, 0.9375f, 0.0f, 0.025f));
        return result;
    }
}
