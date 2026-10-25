package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 月光（招式 ID：236）的攻击／事件阶段动画。
 *
 * <p>招式描述：回复自己的HP. 根据天气的不同, 回复量也会有所变化.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110236（名称）和 120236（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/MoonlightAnimation.java
 *
 * <p>源文件 SHA-256：1b80e34ed82e830a3175eb2ec2c1edf964fd84edef3d0c02d839ed86348707ae
 */
final class ReferenceMoveProgram236 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(1, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(0, false);
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                        ReferenceMoveTimeline.appendThenNextParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(owner.focusCamera(0, 0.4f)),
                                                        owner.tintEnvironment(
                                                                4, 0, 0.0f, 0.9375f, 0.05f))
                                                .onStart(owner.loadBattleBackground(23))
                                                .append(
                                                        owner.scrollBattleBackground(
                                                                4, 0, 1, 1.92f))
                                                .append(
                                                        owner.tintEnvironment(
                                                                4, 0, 0.9375f, 0.0f, 0.05f))
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction2))
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction3))
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction4))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1471,
                                                                1,
                                                                14,
                                                                333.33334f,
                                                                0.8984375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1471,
                                                                2,
                                                                14,
                                                                833.3333f,
                                                                0.3125f,
                                                                owner.casterHandle))
                                                .append(owner.setSpriteAnimationPaused(14, 1))
                                                .onStart(owner.preloadParticleEffect(403)),
                                        owner.emitParticles(-1, 403, 0, 9, 8, 0.5f)),
                                owner.tintEnvironment(4, 0, 0.0f, 0.9375f, 0.05f))
                        .append(owner.tintEnvironment(4, 0, 0.9375f, 0.0f, 0.05f));
        Runnable sceneFlagAction5 = owner.sceneFlag(1, true);
        Runnable sceneFlagAction6 = owner.sceneFlag(0, true);
        result =
                animationStep4
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
