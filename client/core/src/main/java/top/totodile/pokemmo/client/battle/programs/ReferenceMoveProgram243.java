package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 镜面反射（招式 ID：243）的攻击／事件阶段动画。
 *
 * <p>招式描述：从对手那里受到特殊攻击的伤害将以2倍返还给同一个对手.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110243（名称）和 120243（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/MirrorCoatAnimation.java
 *
 * <p>源文件 SHA-256：c6d0dee2ef9824de8fa72fb0d5981a4f96f13ee3d3a998df7b228584d0330fab
 */
final class ReferenceMoveProgram243 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1358,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1471,
                                                                        2,
                                                                        14,
                                                                        750.0f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(410))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1,
                                                                        410,
                                                                        2,
                                                                        9,
                                                                        8,
                                                                        0.3499756f)),
                                                owner.emitParticles(-1, 410, 0, 9, 8, 0.5f),
                                                0.2f),
                                        owner.emitParticles(-1, 410, 1, 9, 8, 0.5f),
                                        1.0f),
                                owner.emitParticles(-1, 410, 3, 9, 8, 0.5f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
