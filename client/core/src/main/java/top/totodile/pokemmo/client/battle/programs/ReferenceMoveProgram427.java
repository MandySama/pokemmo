package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 精神利刃（招式 ID：427）的攻击／事件阶段动画。
 *
 * <p>招式描述：用实体化的心之利刃劈开对手. 容易击中要害.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110427（名称）和 120427（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/PsychoCutAnimation.java
 *
 * <p>源文件 SHA-256：0bd13c281a37a5b1cdaa306c995f10759092e469da49646ae35da781cf33a5ba
 */
final class ReferenceMoveProgram427 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        0, 0.4f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1783,
                                                                                        1,
                                                                                        14,
                                                                                        0.0f,
                                                                                        0.9375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1376,
                                                                                        1,
                                                                                        14,
                                                                                        1333.3334f,
                                                                                        0.0f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                0,
                                                                                                0.0f,
                                                                                                1264.0f))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                1,
                                                                                                1120.0f,
                                                                                                144.0f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1718,
                                                                                        2,
                                                                                        14,
                                                                                        0.0f,
                                                                                        0.78125f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1816,
                                                                                        1,
                                                                                        14,
                                                                                        1333.3334f,
                                                                                        0.78125f,
                                                                                        owner.casterHandle))
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                602))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 602, 1,
                                                                                        9, 8, 0.5f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1423,
                                                                                        2,
                                                                                        16,
                                                                                        1666.6666f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle)),
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1423,
                                                                        1,
                                                                        16,
                                                                        1833.3334f,
                                                                        0.46875f,
                                                                        owner.casterHandle),
                                                                0.1f)
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 602, 0, 9, 8, 0.5f)),
                                                owner.emitParticles(-1, 602, 3, 9, 8, 0.5f),
                                                1.3f)
                                        .append(owner.launchParticles(602, 2, 9, 11, 0.5f, 0.0f))
                                        .append(owner.restoreCamera(0.4f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(1.9f)
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.0f,
                                                        owner.casterHandle)),
                                owner.shakeCombatant(16, 2, 4, 0.016f, 0.032f, 0.19995117f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
