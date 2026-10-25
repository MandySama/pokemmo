package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 力量互换（招式 ID：384）的攻击／事件阶段动画。
 *
 * <p>招式描述：利用超能力互换自己和对手的攻击以及特攻的能力变化.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110384（名称）和 120384（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/PowerSwapAnimation.java
 *
 * <p>源文件 SHA-256：30acab37b3ae48de434004099c1bebcfc70d9ecfc3f49350b18d420ef358d5df
 */
final class ReferenceMoveProgram384 {

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
                                                                                                                                                                                                                                16,
                                                                                                                                                                                                                                750.0f,
                                                                                                                                                                                                                                0.9375f,
                                                                                                                                                                                                                                owner.casterHandle))
                                                                                                                                                                                                        .append(
                                                                                                                                                                                                                owner
                                                                                                                                                                                                                        .focusCamera(
                                                                                                                                                                                                                                1,
                                                                                                                                                                                                                                0.25f))
                                                                                                                                                                                                        .onStart(
                                                                                                                                                                                                                owner
                                                                                                                                                                                                                        .preloadParticleEffect(
                                                                                                                                                                                                                                559)),
                                                                                                                                                                                                owner
                                                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                                                559,
                                                                                                                                                                                                                0,
                                                                                                                                                                                                                11,
                                                                                                                                                                                                                9,
                                                                                                                                                                                                                0.5f,
                                                                                                                                                                                                                360.0f),
                                                                                                                                                                                                0.06f),
                                                                                                                                                                                owner
                                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                                559,
                                                                                                                                                                                                0,
                                                                                                                                                                                                11,
                                                                                                                                                                                                9,
                                                                                                                                                                                                0.5f,
                                                                                                                                                                                                360.0f),
                                                                                                                                                                                0.12f),
                                                                                                                                                                owner
                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                559,
                                                                                                                                                                                0,
                                                                                                                                                                                11,
                                                                                                                                                                                9,
                                                                                                                                                                                0.5f,
                                                                                                                                                                                360.0f),
                                                                                                                                                                0.18f),
                                                                                                                                                owner
                                                                                                                                                        .launchParticles(
                                                                                                                                                                559,
                                                                                                                                                                0,
                                                                                                                                                                11,
                                                                                                                                                                9,
                                                                                                                                                                0.5f,
                                                                                                                                                                360.0f),
                                                                                                                                                0.24f),
                                                                                                                                owner
                                                                                                                                        .launchParticles(
                                                                                                                                                559,
                                                                                                                                                0,
                                                                                                                                                11,
                                                                                                                                                9,
                                                                                                                                                0.5f,
                                                                                                                                                360.0f),
                                                                                                                                0.3f)
                                                                                                                        .append(
                                                                                                                                owner
                                                                                                                                        .focusCamera(
                                                                                                                                                0,
                                                                                                                                                0.25f)),
                                                                                                                owner
                                                                                                                        .launchParticles(
                                                                                                                                559,
                                                                                                                                0,
                                                                                                                                11,
                                                                                                                                9,
                                                                                                                                0.5f,
                                                                                                                                360.0f),
                                                                                                                0.36f),
                                                                                                owner
                                                                                                        .launchParticles(
                                                                                                                559,
                                                                                                                0,
                                                                                                                11,
                                                                                                                9,
                                                                                                                0.5f,
                                                                                                                360.0f)),
                                                                                owner
                                                                                        .launchParticles(
                                                                                                559,
                                                                                                0,
                                                                                                9,
                                                                                                11,
                                                                                                0.5f,
                                                                                                360.0f),
                                                                                0.06f),
                                                                owner.launchParticles(
                                                                        559, 0, 9, 11, 0.5f,
                                                                        360.0f),
                                                                0.12f)
                                                        .append(
                                                                owner.launchParticles(
                                                                        559, 0, 9, 11, 0.5f,
                                                                        360.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1471,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.5078125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1452,
                                                                        2,
                                                                        16,
                                                                        666.6667f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1452,
                                                                        1,
                                                                        14,
                                                                        750.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(owner.focusCamera(1, 0.25f))
                                                        .endGroup()
                                                        .endGroup()
                                                        .beginSequence()
                                                        .waitSeconds(0.18f)
                                                        .beginParallel(),
                                                owner.launchParticles(559, 0, 9, 11, 0.5f, 360.0f),
                                                0.24f),
                                        owner.launchParticles(559, 0, 9, 11, 0.5f, 360.0f),
                                        0.3f),
                                owner.launchParticles(559, 0, 9, 11, 0.5f, 360.0f))
                        .append(owner.restoreCamera(0.25f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
