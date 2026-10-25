package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 死神之怒（自定义动画注册 ID：1027）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色。
 *
 * <p>编号对应参考动画注册表；语言条目为 111027（名称）和 121027（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1027Animation.java
 *
 * <p>源文件 SHA-256：0365525bd637c63d45eec0cf5d969bed5100a66d5a126135835bd2d1585259ca
 */
final class ReferenceMoveProgram1027 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(0, 0 != 0);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .onStart(owner.setHealthHudHidden(18, true))
                                        .append(owner.focusCamera(0, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.8125f, 0.075f))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                                        .endGroup()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1487,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 480.0f))
                                        .onStart(owner.preloadParticleEffect(634))
                                        .append(owner.emitParticles(-1, 634, 3, 9, 8, 0.5f)),
                                owner.emitParticles(-1, 634, 4, 9, 8, 0.5f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1473,
                                        2,
                                        14,
                                        333.33334f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1941,
                                        1,
                                        14,
                                        1000.0f,
                                        0.9765625f,
                                        owner.casterHandle))
                        .append(owner.emitParticles(-1, 634, 0, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 634, 5, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 634, 6, 9, 8, 0.5f))
                        .endGroup();
        return result;
    }
}
