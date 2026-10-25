package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 特性互换（招式 ID：285）的攻击／事件阶段动画。
 *
 * <p>招式描述：利用超能力互换自己和对手的特性.
 *
 * <p>动画说明：播放参考粒子特效；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110285（名称）和 120285（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/SkillSwapAnimation.java
 *
 * <p>源文件 SHA-256：e35149571705e6841d9c52063b762c698a4a9d050e3b3e451e7ebc940e0755fe
 */
final class ReferenceMoveProgram285 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
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
                                                                                                                                                                                        .appendThenDelayedOuterParallel(
                                                                                                                                                                                                ReferenceMoveTimeline
                                                                                                                                                                                                        .appendThenDelayedParallel(
                                                                                                                                                                                                                ReferenceMoveTimeline
                                                                                                                                                                                                                        .sequence()
                                                                                                                                                                                                                        .beginParallel()
                                                                                                                                                                                                                        .append(
                                                                                                                                                                                                                                owner
                                                                                                                                                                                                                                        .scheduleSound(
                                                                                                                                                                                                                                                (byte)
                                                                                                                                                                                                                                                        2,
                                                                                                                                                                                                                                                1510,
                                                                                                                                                                                                                                                1,
                                                                                                                                                                                                                                                16,
                                                                                                                                                                                                                                                0.0f,
                                                                                                                                                                                                                                                0.8984375f,
                                                                                                                                                                                                                                                owner.casterHandle))
                                                                                                                                                                                                                        .append(
                                                                                                                                                                                                                                owner
                                                                                                                                                                                                                                        .scheduleSound(
                                                                                                                                                                                                                                                (byte)
                                                                                                                                                                                                                                                        2,
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
                                                                                                                                                                                                                                                1452,
                                                                                                                                                                                                                                                1,
                                                                                                                                                                                                                                                14,
                                                                                                                                                                                                                                                750.0f,
                                                                                                                                                                                                                                                0.9375f,
                                                                                                                                                                                                                                                owner.casterHandle))
                                                                                                                                                                                                                        .onStart(
                                                                                                                                                                                                                                owner
                                                                                                                                                                                                                                        .preloadParticleEffect(
                                                                                                                                                                                                                                                452)),
                                                                                                                                                                                                                owner
                                                                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                                                                452,
                                                                                                                                                                                                                                0,
                                                                                                                                                                                                                                11,
                                                                                                                                                                                                                                9,
                                                                                                                                                                                                                                0.5f,
                                                                                                                                                                                                                                360.0f),
                                                                                                                                                                                                                0.06f),
                                                                                                                                                                                                owner
                                                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                                                452,
                                                                                                                                                                                                                0,
                                                                                                                                                                                                                11,
                                                                                                                                                                                                                9,
                                                                                                                                                                                                                0.5f,
                                                                                                                                                                                                                360.0f),
                                                                                                                                                                                                0.12f),
                                                                                                                                                                                owner
                                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                                452,
                                                                                                                                                                                                0,
                                                                                                                                                                                                11,
                                                                                                                                                                                                9,
                                                                                                                                                                                                0.5f,
                                                                                                                                                                                                360.0f),
                                                                                                                                                                                0.18f),
                                                                                                                                                                owner
                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                452,
                                                                                                                                                                                0,
                                                                                                                                                                                11,
                                                                                                                                                                                9,
                                                                                                                                                                                0.5f,
                                                                                                                                                                                360.0f),
                                                                                                                                                                0.24f),
                                                                                                                                                owner
                                                                                                                                                        .launchParticles(
                                                                                                                                                                452,
                                                                                                                                                                0,
                                                                                                                                                                11,
                                                                                                                                                                9,
                                                                                                                                                                0.5f,
                                                                                                                                                                360.0f),
                                                                                                                                                0.3f),
                                                                                                                                owner
                                                                                                                                        .launchParticles(
                                                                                                                                                452,
                                                                                                                                                0,
                                                                                                                                                11,
                                                                                                                                                9,
                                                                                                                                                0.5f,
                                                                                                                                                360.0f),
                                                                                                                                0.36f),
                                                                                                                owner
                                                                                                                        .launchParticles(
                                                                                                                                452,
                                                                                                                                0,
                                                                                                                                11,
                                                                                                                                9,
                                                                                                                                0.5f,
                                                                                                                                360.0f)),
                                                                                                owner
                                                                                                        .launchParticles(
                                                                                                                452,
                                                                                                                0,
                                                                                                                9,
                                                                                                                11,
                                                                                                                0.5f,
                                                                                                                360.0f),
                                                                                                0.06f),
                                                                                owner
                                                                                        .launchParticles(
                                                                                                452,
                                                                                                0,
                                                                                                9,
                                                                                                11,
                                                                                                0.5f,
                                                                                                360.0f),
                                                                                0.12f)
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1510,
                                                                                        1,
                                                                                        14,
                                                                                        0.0f,
                                                                                        0.8984375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1455,
                                                                                        2,
                                                                                        16,
                                                                                        500.0f,
                                                                                        0.9375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1376,
                                                                                        2,
                                                                                        16,
                                                                                        1833.3334f,
                                                                                        0.0f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                2,
                                                                                                0,
                                                                                                0.0f,
                                                                                                1600.0f)),
                                                                owner.launchParticles(
                                                                        452, 0, 9, 11, 0.5f,
                                                                        360.0f),
                                                                0.18f),
                                                owner.launchParticles(452, 0, 9, 11, 0.5f, 360.0f),
                                                0.24f),
                                        owner.launchParticles(452, 0, 9, 11, 0.5f, 360.0f),
                                        0.3f),
                                owner.launchParticles(452, 0, 9, 11, 0.5f, 360.0f),
                                0.9f)
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
