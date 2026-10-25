package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 心灵互换（招式 ID：391）的攻击／事件阶段动画。
 *
 * <p>招式描述：利用超能力互换自己和对手之间的能力变化.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110391（名称）和 120391（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/HeartSwapAnimation.java
 *
 * <p>源文件 SHA-256：af541445cfdaf95035ef777d2a333ae801162dd617b69cb4fe774a6d87d15ea3
 */
final class ReferenceMoveProgram391 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                        ReferenceMoveTimeline
                                                                .appendThenDelayedOuterParallel(
                                                                        ReferenceMoveTimeline
                                                                                .appendThenDelayedParallel(
                                                                                        ReferenceMoveTimeline
                                                                                                .appendThenOuterParallel(
                                                                                                        ReferenceMoveTimeline
                                                                                                                .appendThenDelayedOuterParallel(
                                                                                                                        ReferenceMoveTimeline
                                                                                                                                .appendThenDelayedOuterParallel(
                                                                                                                                        ReferenceMoveTimeline
                                                                                                                                                .appendThenDelayedOuterParallel(
                                                                                                                                                        ReferenceMoveTimeline
                                                                                                                                                                .appendThenDelayedOuterParallel(
                                                                                                                                                                        ReferenceMoveTimeline
                                                                                                                                                                                .appendThenDelayedParallel(
                                                                                                                                                                                        ReferenceMoveTimeline
                                                                                                                                                                                                .sequence()
                                                                                                                                                                                                .beginParallel()
                                                                                                                                                                                                .append(
                                                                                                                                                                                                        owner
                                                                                                                                                                                                                .focusCamera(
                                                                                                                                                                                                                        1,
                                                                                                                                                                                                                        0.3f))
                                                                                                                                                                                                .endGroup()
                                                                                                                                                                                                .beginParallel()
                                                                                                                                                                                                .append(
                                                                                                                                                                                                        owner
                                                                                                                                                                                                                .scheduleSound(
                                                                                                                                                                                                                        (byte)
                                                                                                                                                                                                                                2,
                                                                                                                                                                                                                        (short)
                                                                                                                                                                                                                                1473,
                                                                                                                                                                                                                        1,
                                                                                                                                                                                                                        16,
                                                                                                                                                                                                                        0.0f,
                                                                                                                                                                                                                        0.5078125f,
                                                                                                                                                                                                                        owner.casterHandle))
                                                                                                                                                                                                .append(
                                                                                                                                                                                                        owner
                                                                                                                                                                                                                .scheduleSound(
                                                                                                                                                                                                                        (byte)
                                                                                                                                                                                                                                2,
                                                                                                                                                                                                                        (short)
                                                                                                                                                                                                                                1452,
                                                                                                                                                                                                                        2,
                                                                                                                                                                                                                        14,
                                                                                                                                                                                                                        666.6667f,
                                                                                                                                                                                                                        0.9375f,
                                                                                                                                                                                                                        owner.casterHandle))
                                                                                                                                                                                                .append(
                                                                                                                                                                                                        owner
                                                                                                                                                                                                                .scheduleSound(
                                                                                                                                                                                                                        (byte)
                                                                                                                                                                                                                                2,
                                                                                                                                                                                                                        (short)
                                                                                                                                                                                                                                1452,
                                                                                                                                                                                                                        1,
                                                                                                                                                                                                                        16,
                                                                                                                                                                                                                        750.0f,
                                                                                                                                                                                                                        0.9375f,
                                                                                                                                                                                                                        owner.casterHandle))
                                                                                                                                                                                                .onStart(
                                                                                                                                                                                                        owner
                                                                                                                                                                                                                .preloadParticleEffect(
                                                                                                                                                                                                                        566)),
                                                                                                                                                                                        owner
                                                                                                                                                                                                .launchParticles(
                                                                                                                                                                                                        566,
                                                                                                                                                                                                        0,
                                                                                                                                                                                                        11,
                                                                                                                                                                                                        9,
                                                                                                                                                                                                        0.5f,
                                                                                                                                                                                                        360.0f),
                                                                                                                                                                                        0.06f),
                                                                                                                                                                        owner
                                                                                                                                                                                .launchParticles(
                                                                                                                                                                                        566,
                                                                                                                                                                                        0,
                                                                                                                                                                                        11,
                                                                                                                                                                                        9,
                                                                                                                                                                                        0.5f,
                                                                                                                                                                                        360.0f),
                                                                                                                                                                        0.12f),
                                                                                                                                                        owner
                                                                                                                                                                .launchParticles(
                                                                                                                                                                        566,
                                                                                                                                                                        0,
                                                                                                                                                                        11,
                                                                                                                                                                        9,
                                                                                                                                                                        0.5f,
                                                                                                                                                                        360.0f),
                                                                                                                                                        0.18f),
                                                                                                                                        owner
                                                                                                                                                .launchParticles(
                                                                                                                                                        566,
                                                                                                                                                        0,
                                                                                                                                                        11,
                                                                                                                                                        9,
                                                                                                                                                        0.5f,
                                                                                                                                                        360.0f),
                                                                                                                                        0.24f),
                                                                                                                        owner
                                                                                                                                .launchParticles(
                                                                                                                                        566,
                                                                                                                                        0,
                                                                                                                                        11,
                                                                                                                                        9,
                                                                                                                                        0.5f,
                                                                                                                                        360.0f),
                                                                                                                        0.3f)
                                                                                                                .append(
                                                                                                                        owner
                                                                                                                                .launchParticles(
                                                                                                                                        566,
                                                                                                                                        0,
                                                                                                                                        11,
                                                                                                                                        9,
                                                                                                                                        0.5f,
                                                                                                                                        360.0f))
                                                                                                                .append(
                                                                                                                        owner
                                                                                                                                .focusCamera(
                                                                                                                                        0,
                                                                                                                                        0.3f))
                                                                                                                .endGroup()
                                                                                                                .endGroup()
                                                                                                                .beginSequence()
                                                                                                                .waitSeconds(
                                                                                                                        0.36f)
                                                                                                                .beginParallel(),
                                                                                                        owner
                                                                                                                .launchParticles(
                                                                                                                        566,
                                                                                                                        0,
                                                                                                                        11,
                                                                                                                        9,
                                                                                                                        0.5f,
                                                                                                                        360.0f)),
                                                                                        owner
                                                                                                .launchParticles(
                                                                                                        566,
                                                                                                        0,
                                                                                                        9,
                                                                                                        11,
                                                                                                        0.5f,
                                                                                                        360.0f),
                                                                                        0.06f),
                                                                        owner.launchParticles(
                                                                                566, 0, 9, 11, 0.5f,
                                                                                360.0f),
                                                                        0.12f),
                                                        owner.launchParticles(
                                                                566, 0, 9, 11, 0.5f, 360.0f),
                                                        0.18f)
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1471,
                                                                1,
                                                                14,
                                                                0.0f,
                                                                0.5078125f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1452,
                                                                1,
                                                                16,
                                                                516.6667f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1452,
                                                                2,
                                                                16,
                                                                600.0f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.launchParticles(
                                                                566, 0, 9, 11, 0.5f, 360.0f))
                                                .append(owner.focusCamera(1, 0.3f))
                                                .endGroup()
                                                .endGroup()
                                                .beginSequence()
                                                .waitSeconds(0.24f)
                                                .beginParallel(),
                                        owner.launchParticles(566, 0, 9, 11, 0.5f, 360.0f),
                                        0.3f),
                                owner.launchParticles(566, 0, 9, 11, 0.5f, 360.0f))
                        .append(owner.restoreCamera(0.6f))
                        .endGroup()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.6f))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1455,
                                        2,
                                        16,
                                        0.0f,
                                        0.625f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        2,
                                        16,
                                        1416.6666f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 0, 0.0f, 1360.0f))
                        .append(owner.emitParticles(-1, 566, 1, 11, 8, 0.4f))
                        .append(owner.emitParticles(-1, 566, 2, 9, 8, 0.4f))
                        .append(owner.emitParticles(-1, 566, 2, 11, 8, 0.4f))
                        .append(owner.emitParticles(-1, 566, 3, 9, 8, 0.4f))
                        .append(owner.emitParticles(-1, 566, 3, 11, 8, 0.4f))
                        .append(owner.emitParticles(-1, 566, 1, 9, 8, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
