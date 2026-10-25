package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 自由落体（招式 ID：507）的蓄力阶段动画。
 *
 * <p>招式描述：第1回合将对手带到空中, 第2回合将其摔下进行攻击. 被带到空中的对手不能动弹.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵显隐；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110507（名称）和 120507（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/SkyDropAnimation.java
 *
 * <p>源文件 SHA-256：cc754bd375ea02b26261ff1c0e393004aa41a1926a08fd54bd24b8b476681941
 */
final class ReferenceChargeProgram507 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline
                                                                        .waitThenParallel(0.6f),
                                                                owner.scaleCombatant(
                                                                        14,
                                                                        1,
                                                                        0,
                                                                        0.0f,
                                                                        0.384f,
                                                                        0.39990234f,
                                                                        0.39990234f),
                                                                0.16f)
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1455,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 0, 16.0f, 800.0f))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 1, 480.0f, 320.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1376,
                                                                        1,
                                                                        14,
                                                                        833.3333f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1466,
                                                                        2,
                                                                        16,
                                                                        500.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(owner.setCombatantVisibility(14, 3))
                                                        .onStart(owner.preloadParticleEffect(456))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 456, 0, 9, 8, 0.4f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 456, 2, 9, 8, 0.4f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 456, 3, 9, 8, 0.4f))
                                                        .endGroup()
                                                        .endGroup()
                                                        .beginSequence()
                                                        .waitSeconds(0.76f)
                                                        .beginParallel()
                                                        .endGroup()
                                                        .endGroup()
                                                        .beginSequence()
                                                        .waitSeconds(0.76f)
                                                        .beginParallel()
                                                        .onStart(owner.preloadParticleEffect(456))
                                                        .append(owner.focusCamera(1, 0.3f))
                                                        .append(
                                                                owner.launchParticles(
                                                                        456, 1, 9, 11, 0.5f,
                                                                        192.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1454,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1454,
                                                                        1,
                                                                        16,
                                                                        200.0f,
                                                                        0.546875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1454,
                                                                        1,
                                                                        16,
                                                                        400.0f,
                                                                        0.234375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1454,
                                                                        1,
                                                                        16,
                                                                        600.0f,
                                                                        0.15625f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1499,
                                                                        1,
                                                                        16,
                                                                        700.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1376,
                                                                        1,
                                                                        16,
                                                                        1250.0f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 0, 672.0f, 320.0f)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1478,
                                                        2,
                                                        16,
                                                        916.6667f,
                                                        0.9921875f,
                                                        owner.casterHandle),
                                                0.96f)
                                        .append(owner.setCombatantVisibility(16, 3))
                                        .onStart(owner.preloadParticleEffect(180))
                                        .append(owner.emitParticles(-1, 180, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 180, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 180, 2, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 180, 3, 11, 8, 0.5f)),
                                owner.emitParticles(-1, 180, 4, 11, 8, 0.5f))
                        .append(owner.scaleCombatant(14, 1, 0, 0.0f, 0.384f, 1.0f, 1.0f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
