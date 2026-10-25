package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 神通力（招式 ID：326）的攻击／事件阶段动画。
 *
 * <p>招式描述：发出看不见的神奇力量进行攻击. 有时会使对手畏缩.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；执行精灵位移；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110326（名称）和 120326（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/ExtrasensoryAnimation.java
 *
 * <p>源文件 SHA-256：a05d65c3cffcaa33afa749413db6aa51e9f190fdf04e71ea212488b78947c6eb
 */
final class ReferenceMoveProgram326 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.6f)),
                                                owner.tintEnvironment(4, 0, 0.0f, 0.75f, 0.025f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1450,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.46875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        1,
                                                        14,
                                                        1000.0f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 1, 480.0f, 480.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1467,
                                                        2,
                                                        14,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(496)),
                                owner.emitParticles(-1, 496, 0, 9, 8, 0.4f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1501,
                                        1,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1429,
                                        2,
                                        14,
                                        1000.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(owner.shakeCombatant(16, 2, 10, 0.0f, 0.032f, 0.5f, 0.0f))
                        .endGroup()
                        .beginParallel()
                        .beginSequence()
                        .waitSeconds(0.4f)
                        .beginParallel()
                        .append(
                                owner.scaleCombatant(
                                        14, 2, 20, 0.0f, 0.016f, 0.19995117f, 0.19995117f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.025f))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
