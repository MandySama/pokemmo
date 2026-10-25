package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 力量戏法（招式 ID：379）的攻击／事件阶段动画。
 *
 * <p>招式描述：利用超能力交换自己的攻击和防御的力量.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110379（名称）和 120379（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/PowerTrickAnimation.java
 *
 * <p>源文件 SHA-256：d81ea4ea88f2debff2fa4bfb172ee793ff2b388b82996e301bec54d55f9ee09e
 */
final class ReferenceMoveProgram379 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1873,
                                        1,
                                        14,
                                        0.0f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1873,
                                        1,
                                        14,
                                        500.0f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1783,
                                        2,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        2,
                                        14,
                                        1166.6666f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 1, 640.0f, 320.0f))
                        .onStart(owner.preloadParticleEffect(554))
                        .append(owner.emitParticles(-1, 554, 2, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 554, 0, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 554, 1, 9, 8, 0.5f))
                        .endGroup()
                        .beginParallel()
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
