package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 地球上投（招式 ID：69）的攻击／事件阶段动画。
 *
 * <p>招式描述：利用引力将对手甩飞出去. 给予对手和自己等级相同的伤害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110069（名称）和 120069（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SeismicTossAnimation.java
 *
 * <p>源文件 SHA-256：013c2f7f936c6c431ce4f21127e24b13584396eace24a1b606c324a3a386e4d9
 */
final class ReferenceMoveProgram69 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep3 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1895,
                                                        1,
                                                        16,
                                                        333.33334f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1895,
                                                        2,
                                                        16,
                                                        833.3333f,
                                                        0.9921875f,
                                                        owner.casterHandle)),
                                owner.tintEnvironment(2, 0, 0.0f, 0.8125f, 0.05f))
                        .onStart(owner.loadBattleBackground(10));
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(1, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(0, false);
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        ReferenceMoveTimeline.appendThenNextParallel(
                                        ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.appendThenNextParallel(
                                                        ReferenceMoveTimeline
                                                                .appendThenNextParallel(
                                                                        animationStep3
                                                                                .onStart(
                                                                                        ReferenceMoveTimeline
                                                                                                .callback(
                                                                                                        sceneFlagAction2))
                                                                                .append(
                                                                                        owner
                                                                                                .scrollBattleBackground(
                                                                                                        4,
                                                                                                        0,
                                                                                                        22,
                                                                                                        0.48f))
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
                                                                                                .focusCamera(
                                                                                                        1,
                                                                                                        0.4f))
                                                                                .endGroup()
                                                                                .beginParallel(),
                                                                        owner
                                                                                .scrollBattleBackground(
                                                                                        4, 0, 18,
                                                                                        0.32f)),
                                                        owner.scrollBattleBackground(
                                                                4, 0, 14, 0.16f)),
                                                owner.scrollBattleBackground(4, 0, 10, 0.16f)),
                                        owner.scrollBattleBackground(4, 0, -14, 0.16f))
                                .append(owner.scrollBattleBackground(4, 0, -22, 0.96f))
                                .append(owner.setSpriteAnimationPaused(14, 1))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1416,
                                                1,
                                                16,
                                                0.0f,
                                                0.9921875f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1376,
                                                1,
                                                16,
                                                1000.0f,
                                                0.0f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1418,
                                                2,
                                                16,
                                                0.0f,
                                                0.9921875f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1376,
                                                2,
                                                16,
                                                1000.0f,
                                                0.0f,
                                                owner.casterHandle))
                                .append(owner.adjustSoundTrack(1, 1, 640.0f, 320.0f))
                                .append(owner.adjustSoundTrack(2, 1, 640.0f, 320.0f))
                                .onStart(owner.preloadParticleEffect(230))
                                .append(owner.emitParticles(-1, 230, 0, 11, 8, 0.5f))
                                .append(owner.emitParticles(-1, 230, 1, 11, 8, 0.25f))
                                .append(owner.emitParticles(-1, 230, 2, 11, 8, 0.25f)),
                        owner.shakeCombatant(16, 3, 3, 0.032f, 0.016f, 0.30004883f, 0.0f));
        Runnable sceneFlagAction5 = owner.sceneFlag(1, true);
        Runnable sceneFlagAction6 = owner.sceneFlag(0, true);
        result =
                ReferenceMoveTimeline.appendAndClose(
                        animationStep10
                                .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                                .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                                .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                                .append(owner.setSpriteAnimationPaused(14, 0))
                                .append(owner.restoreCamera(0.4f)),
                        owner.tintEnvironment(2, 0, 0.8125f, 0.0f, 0.05f));
        return result;
    }
}
