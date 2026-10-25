package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 充电（招式 ID：268）的攻击／事件阶段动画。
 *
 * <p>招式描述：使自身进入充电状态, 下次使用电属性技能威力翻倍. 同时提升1级特防.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110268（名称）和 120268（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/ChargeAnimation.java
 *
 * <p>源文件 SHA-256：7b648b90999797c56899c42d75f0ad37aff80940297d571cec1a2d02871e928e
 */
final class ReferenceMoveProgram268 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1461,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1461,
                                                        2,
                                                        14,
                                                        266.66666f,
                                                        0.625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1461,
                                                        1,
                                                        14,
                                                        533.3333f,
                                                        0.46875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1461,
                                                        2,
                                                        14,
                                                        800.0f,
                                                        0.3125f,
                                                        owner.casterHandle))
                                        .append(owner.focusCamera(0, 0.4f))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.75f, 0.05f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1407,
                                                        1,
                                                        14,
                                                        833.3333f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        1,
                                                        14,
                                                        2500.0f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 800.0f, 1600.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1458,
                                                        2,
                                                        14,
                                                        2000.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(433))
                                        .append(owner.emitParticles(-1, 433, 0, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 433, 1, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 433, 2, 9, 8, 0.5f)),
                                owner.emitParticles(-1, 433, 3, 9, 8, 0.5f))
                        .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
