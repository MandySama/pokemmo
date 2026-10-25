package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 吹捧（招式 ID：260）的攻击／事件阶段动画。
 *
 * <p>招式描述：吹捧对手, 使其混乱. 同时还会提高对手的特攻.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵位移；执行精灵缩放。
 *
 * <p>编号对应参考动画注册表；语言条目为 110260（名称）和 120260（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/FlatterAnimation.java
 *
 * <p>源文件 SHA-256：4cb3a40f7c7e89a4ae8c631e2bdc1e65bc9b4e06605360975b583124edf360f8
 */
final class ReferenceMoveProgram260 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1493,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        1,
                                                        14,
                                                        2166.6667f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 1, 1600.0f, 480.0f))
                                        .waitSeconds(0.6f)
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1502,
                                                        2,
                                                        16,
                                                        500.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1502,
                                                        2,
                                                        16,
                                                        750.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(426))
                                        .append(owner.emitParticles(-1, 426, 0, 11, 8, 0.4f)),
                                owner.emitParticles(-1, 426, 1, 11, 8, 0.4f),
                                0.4f)
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 2, 0.016f, 0.064f, -0.19995117f, 0.19995117f))
                        .append(owner.shakeCombatant(16, 2, 2, 0.016f, 0.064f, 0.0f, 0.5f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
