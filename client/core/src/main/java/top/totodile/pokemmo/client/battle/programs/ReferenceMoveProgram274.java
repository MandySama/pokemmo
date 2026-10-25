package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 借助（招式 ID：274）的攻击／事件阶段动画。
 *
 * <p>招式描述：向我方紧急求助, 从我方宝可梦已学会的招式中随机使用1个.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110274（名称）和 120274（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/AssistAnimation.java
 *
 * <p>源文件 SHA-256：802ba1789469329818dfce46bc8284ddb717dd49f42ee5f0351ce7738dc9e4b3
 */
final class ReferenceMoveProgram274 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1542,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1542,
                                                        1,
                                                        14,
                                                        83.333336f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1542,
                                                        1,
                                                        14,
                                                        166.66667f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1542,
                                                        1,
                                                        14,
                                                        250.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1542,
                                                        1,
                                                        14,
                                                        333.33334f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1542,
                                                        1,
                                                        14,
                                                        416.66666f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1542,
                                                        1,
                                                        14,
                                                        500.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1542,
                                                        1,
                                                        14,
                                                        583.3333f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1542,
                                                        1,
                                                        14,
                                                        666.6667f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1542,
                                                        1,
                                                        14,
                                                        750.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1445,
                                                        2,
                                                        14,
                                                        1083.3334f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        2,
                                                        14,
                                                        2000.0f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 0, 1040.0f, 960.0f))
                                        .append(owner.adjustSoundTrack(2, 1, 1600.0f, 320.0f))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .onStart(owner.preloadParticleEffect(439))
                                        .append(owner.emitParticles(-1, 439, 0, 9, 8, 0.25f)),
                                owner.emitParticles(-1, 439, 1, 9, 8, 0.25f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
