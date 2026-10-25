package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 真气拳（招式 ID：264）的攻击／事件阶段动画。
 *
 * <p>招式描述：集中精神出拳. 在招式使出前若受到攻击则会失败.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；切换战斗背景；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110264（名称）和 120264（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/FocusPunchAnimation.java
 *
 * <p>源文件 SHA-256：e753b126ad9a77dea96a23316f45ba712a18c17d580fae73097a76cd91b63c93
 */
final class ReferenceMoveProgram264 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep2 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle),
                                                0.16f)
                                        .append(owner.focusCamera(1, 0.3f))
                                        .onStart(owner.loadBattleBackground(24))
                                        .endGroup()
                                        .endGroup()
                                        .endGroup()
                                        .beginParallel(),
                                owner.tintEnvironment(2, 0, 0.0f, 0.8125f, 0.05f),
                                0.2f)
                        .append(owner.tintEnvironment(3, 0, 0.8125f, 0.0f, 0.05f));
        Runnable sceneFlagAction2 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction3 = owner.sceneFlag(1, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(2, true);
        ReferenceMoveTimeline animationStep11 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.appendThenOuterParallel(
                                        animationStep2
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
                                                                (short) 1407,
                                                                1,
                                                                16,
                                                                0.0f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1376,
                                                                1,
                                                                16,
                                                                333.33334f,
                                                                0.0f,
                                                                owner.casterHandle))
                                                .append(owner.adjustSoundTrack(1, 0, 0.0f, 320.0f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1420,
                                                                1,
                                                                16,
                                                                333.33334f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1475,
                                                                2,
                                                                16,
                                                                333.33334f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .onStart(owner.preloadParticleEffect(430))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 430, 2, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 430, 3, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 430, 4, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 430, 0, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 430, 1, 11, 8, 0.5f))
                                                .append(owner.setSpriteAnimationPaused(16, 1)),
                                        owner.scaleCombatant(
                                                16,
                                                2,
                                                8,
                                                0.016f,
                                                0.032f,
                                                -0.19995117f,
                                                0.19995117f)),
                                owner.tintEnvironment(3, 0, 0.0f, 1.0f, 0.05f),
                                0.32f)
                        .append(owner.tintEnvironment(2, 0, 1.0f, 0.0f, 0.025f));
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        Runnable sceneFlagAction6 = owner.sceneFlag(1, true);
        result =
                animationStep11
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .append(owner.restoreCamera(0.3f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
