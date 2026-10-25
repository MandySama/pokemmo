package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 疯狂滚压（招式 ID：537）的攻击／事件阶段动画。
 *
 * <p>招式描述：旋转揉成团的身体压扁对手. 有时会使对手畏缩.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110537（名称）和 120537（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/SteamrollerAnimation.java
 *
 * <p>源文件 SHA-256：3fd7dd3fa734f80e665dd422cc1cf3cc8ffa3e49c5810b6acd8bd5cd22250b9d
 */
final class ReferenceMoveProgram537 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1448,
                                                        1,
                                                        16,
                                                        166.66667f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1424,
                                                        2,
                                                        16,
                                                        583.3333f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1753,
                                                        0,
                                                        16,
                                                        583.3333f,
                                                        0.625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1944,
                                                        1,
                                                        16,
                                                        583.3333f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 1, 800.0f, 1120.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        1,
                                                        16,
                                                        2000.0f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(701))
                                        .append(owner.emitParticles(-1, 701, 1, 11, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 701, 2, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 701, 3, 11, 8, 0.3250122f))
                                        .append(owner.emitParticles(-1, 701, 4, 11, 8, 0.5f)),
                                owner.emitParticles(-1, 701, 5, 11, 8, 0.25f),
                                0.48f)
                        .append(
                                owner.scaleCombatant(
                                        16, 1, 0, 0.0f, 0.064f, 1.3999023f, 0.39990234f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.scaleCombatant(16, 1, 0, 0.0f, 0.096f, 1.0f, 1.0f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
