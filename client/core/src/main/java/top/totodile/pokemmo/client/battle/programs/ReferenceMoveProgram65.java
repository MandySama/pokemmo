package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 啄钻（招式 ID：65）的攻击／事件阶段动画。
 *
 * <p>招式描述：一边旋转, 一边将尖喙刺入对手进行攻击.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110065（名称）和 120065（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/DrillPeckAnimation.java
 *
 * <p>源文件 SHA-256：3348dbaee24e56dcce70a63362bf174280664137b47c34dd770d6c0f771fff6a
 */
final class ReferenceMoveProgram65 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1489,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.shakeCombatant(
                                                        14,
                                                        2,
                                                        8,
                                                        0.016f,
                                                        0.032f,
                                                        0.19995117f,
                                                        0.0f))
                                        .onStart(owner.preloadParticleEffect(226))
                                        .append(owner.emitParticles(-1, 226, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 226, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 226, 2, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        8,
                                                        0.016f,
                                                        0.032f,
                                                        0.100097656f,
                                                        -0.100097656f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1883,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 1, 800.0f, 160.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        2,
                                                        16,
                                                        1000.0f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1815,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 1, 800.0f, 160.0f))
                                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 960.0f)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        1376,
                                        1,
                                        16,
                                        1000.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
