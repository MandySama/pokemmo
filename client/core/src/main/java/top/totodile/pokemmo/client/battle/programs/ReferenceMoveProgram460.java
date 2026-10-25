package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 亚空裂斩（招式 ID：460）的攻击／事件阶段动画。
 *
 * <p>招式描述：将对手连同周围的空间一起撕裂并给予伤害. 容易击中要害.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；切换战斗背景；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110460（名称）和 120460（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/SpacialRendAnimation.java
 *
 * <p>源文件 SHA-256：dee0830303b76b406e491fac85f43c77c849f2f3c0489d72c923447a5b7f52f5
 */
final class ReferenceMoveProgram460 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1407,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        1,
                                                        14,
                                                        1333.3334f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 1280.0f))
                                        .append(owner.adjustSoundTrack(1, 1, 960.0f, 320.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1907,
                                                        2,
                                                        14,
                                                        833.3333f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(635))
                                        .append(owner.emitParticles(-1, 635, 4, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 635, 2, 9, 8, 0.5f))
                                        .onStart(owner.loadBattleBackground(36))
                                        .append(owner.emitParticles(-1, 635, 0, 9, 8, 0.5f)),
                                owner.tintEnvironment(4, 0, 0.0f, 0.8125f, 0.025f))
                        .append(owner.focusCamera(1, 0.3f))
                        .endGroup()
                        .beginParallel();
        Runnable sceneFlagAction2 = owner.sceneFlag(0, 0 != 0);
        Runnable sceneFlagAction3 = owner.sceneFlag(1, 0 != 0);
        Runnable sceneFlagAction4 = owner.sceneFlag(2, 1 != 0);
        ReferenceMoveTimeline animationStep13 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep7
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                                        .append(owner.tintEnvironment(3, 0, 0.8125f, 0.0f, 0.025f))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1407,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        1,
                                                        16,
                                                        1333.3334f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 1280.0f))
                                        .append(owner.adjustSoundTrack(1, 1, 960.0f, 320.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1941,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(owner.emitParticles(-1, 635, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 635, 3, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 635, 5, 11, 8, 0.5f)),
                                owner.scaleCombatant(
                                        16, 2, 6, 0.016f, 0.032f, 0.19995117f, -0.19995117f)),
                        owner.tintEnvironment(3, 0, 0.0f, 0.8125f, 0.05f));
        Runnable sceneFlagAction5 = owner.sceneFlag(0, 1 != 0);
        Runnable sceneFlagAction6 = owner.sceneFlag(1, 1 != 0);
        result =
                ReferenceMoveTimeline.appendAndClose(
                        animationStep13
                                .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                                .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                                .append(owner.tintEnvironment(4, 0, 0.8125f, 0.0f, 0.05f))
                                .append(owner.restoreCamera(0.4f))
                                .append(owner.setSpriteAnimationPaused(16, 0)),
                        owner.scheduleSound((byte) 2, 1376, 2, 16, 0.0f, 0.0f, owner.casterHandle));
        return result;
    }
}
