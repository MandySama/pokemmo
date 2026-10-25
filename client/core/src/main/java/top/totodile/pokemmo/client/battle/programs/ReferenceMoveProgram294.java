package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 萤火（招式 ID：294）的攻击／事件阶段动画。
 *
 * <p>招式描述：凝视闪烁的光芒, 集中自己的精神, 从而巨幅提高特攻.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110294（名称）和 120294（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/TailGlowAnimation.java
 *
 * <p>源文件 SHA-256：ea84243850be1afbab001208c4163cb5340e8f9821fadc39db56713e7f40bdb0
 */
final class ReferenceMoveProgram294 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.75f, 0.1f))
                                        .append(owner.focusCamera(0, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1461,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1461,
                                                        2,
                                                        14,
                                                        83.333336f,
                                                        0.3125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1461,
                                                        2,
                                                        14,
                                                        583.3333f,
                                                        0.234375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1461,
                                                        1,
                                                        14,
                                                        666.6667f,
                                                        0.1171875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1461,
                                                        1,
                                                        14,
                                                        1050.0f,
                                                        0.078125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1461,
                                                        2,
                                                        14,
                                                        1133.3334f,
                                                        0.0390625f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(460)),
                                owner.emitParticles(-1, 460, 0, 9, 8, 0.5f))
                        .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.1f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
