package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 飞翔（招式 ID：19）的蓄力阶段动画。
 *
 * <p>招式描述：第1回合飞上天空, 第2回合攻击对手.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；控制精灵显隐；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110019（名称）和 120019（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/FlyAnimation.java
 *
 * <p>源文件 SHA-256：f6098a352c8182db5f53e0e23bb5619801e2307e63248cbcf8068195d7e536cc
 */
final class ReferenceChargeProgram19 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1489,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1497,
                                                        1,
                                                        14,
                                                        1000.0f,
                                                        0.46875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        1,
                                                        14,
                                                        1833.3334f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1538,
                                                        2,
                                                        14,
                                                        1166.6666f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 960.0f, 640.0f)),
                                owner.adjustSoundTrack(1, 1, 1440.0f, 320.0f))
                        .onStart(owner.preloadParticleEffect(180))
                        .append(owner.emitParticles(-1, 180, 0, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 180, 4, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 180, 1, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 180, 2, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 180, 3, 9, 8, 0.5f))
                        .append(owner.setCombatantVisibility(14, 3))
                        .endGroup()
                        .beginParallel()
                        .append(owner.restoreCamera(0.8f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
