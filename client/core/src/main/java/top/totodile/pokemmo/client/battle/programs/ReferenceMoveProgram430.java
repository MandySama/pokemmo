package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 加农光炮（招式 ID：430）的攻击／事件阶段动画。
 *
 * <p>招式描述：将身体的光芒聚集在一点释放出去. 有时会降低对手的特防.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110430（名称）和 120430（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/FlashCannonAnimation.java
 *
 * <p>源文件 SHA-256：46aa064f9c5e1fd49c7591ca4d5413544d36b9f87257944214cc1f0b037fa292
 */
final class ReferenceMoveProgram430 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner
                                                                                        .tintEnvironment(
                                                                                                4,
                                                                                                0,
                                                                                                0.0f,
                                                                                                0.8125f,
                                                                                                0.075f))
                                                                        .onStart(
                                                                                owner
                                                                                        .setHealthHudHidden(
                                                                                                18,
                                                                                                true))
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        0, 0.4f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1487,
                                                                                        1,
                                                                                        14,
                                                                                        500.0f,
                                                                                        0.859375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                1,
                                                                                                1600.0f,
                                                                                                480.0f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1437,
                                                                                        2,
                                                                                        14,
                                                                                        2166.6667f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                605))
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                606))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 605, 0,
                                                                                        9, 8, 0.5f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 605, 1,
                                                                                        9, 8, 0.5f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 605, 2,
                                                                                        9, 8, 0.5f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 605, 3,
                                                                                        9, 8, 0.5f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 605, 4,
                                                                                        9, 8, 0.5f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 606, 3,
                                                                                        9, 8,
                                                                                        0.5f)),
                                                                owner.emitParticles(
                                                                        -1, 606, 2, 9, 8, 0.5f),
                                                                2.5f)
                                                        .append(
                                                                owner.launchParticles(
                                                                        606, 0, 9, 11, 0.5f,
                                                                        192.0f))
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .endGroup()
                                                        .endGroup()
                                                        .beginSequence()
                                                        .waitSeconds(2.82f)
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1483,
                                                                        2,
                                                                        16,
                                                                        0.0f,
                                                                        0.703125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1475,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle)),
                                                owner.emitParticles(-1, 606, 1, 11, 8, 0.5f),
                                                2.92f)
                                        .append(owner.emitParticles(-1, 606, 1, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(3.52f)
                                        .beginParallel()
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        8,
                                                        0.0f,
                                                        0.032f,
                                                        -0.19995117f,
                                                        0.19995117f)),
                                owner.shakeCombatant(16, 2, 10, 0.0f, 0.032f, 0.19995117f, 0.0f))
                        .append(owner.tintEnvironment(4, 0, 0.8125f, 0.0f, 0.075f))
                        .append(owner.restoreCamera(0.4f))
                        .onStart(owner.setHealthHudHidden(14, false))
                        .append(owner.setSpriteAnimationPaused(18, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
