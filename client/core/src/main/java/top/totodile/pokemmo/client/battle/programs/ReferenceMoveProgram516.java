package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 传递礼物（招式 ID：516）的攻击／事件阶段动画。
 *
 * <p>招式描述：当对手未携带道具时, 能够将自己携带的道具交给对手.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵缩放；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110516（名称）和 120516（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/BestowAnimation.java
 *
 * <p>源文件 SHA-256：f36da64d70f56fc6f1a20011271b1f3a2aaf453fa127ed2de38db0f2e42586ff
 */
final class ReferenceMoveProgram516 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .beginSequence()
                                                        .waitSeconds(0.6f)
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1665,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 1, 640.0f, 480.0f))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 0, 320.0f, 640.0f)),
                                                owner.scaleCombatant(
                                                        14,
                                                        2,
                                                        1,
                                                        0.0f,
                                                        0.096f,
                                                        0.19995117f,
                                                        -0.19995117f),
                                                0.8f)
                                        .onStart(owner.preloadParticleEffect(681)),
                                owner.launchParticles(681, 0, 9, 11, 0.5f, 552.0f),
                                1.2f)
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(1.6f)
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1468,
                                        2,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 1, 0.0f, 0.096f, 0.19995117f, -0.19995117f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .beginSequence()
                        .waitSeconds(0.4f)
                        .beginParallel()
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
