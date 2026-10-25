package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 空气利刃（招式 ID：314）的攻击／事件阶段动画。
 *
 * <p>招式描述：用锐利的风切斩对手进行攻击. 容易击中要害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110314（名称）和 120314（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/AirCutterAnimation.java
 *
 * <p>源文件 SHA-256：27245698203113fb359da6713079b94b84c531450bb8dff98044c56dcae82d34
 */
final class ReferenceMoveProgram314 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1815,
                                                                        1,
                                                                        16,
                                                                        333.33334f,
                                                                        0.625f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 0, 320.0f, 1280.0f))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 1, 320.0f, 1280.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1376,
                                                                        1,
                                                                        16,
                                                                        1666.6666f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1423,
                                                                        2,
                                                                        16,
                                                                        450.0f,
                                                                        0.9765625f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1423,
                                                                        2,
                                                                        16,
                                                                        583.3333f,
                                                                        0.9765625f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1423,
                                                                        2,
                                                                        16,
                                                                        750.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1423,
                                                                        2,
                                                                        16,
                                                                        833.3333f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(485))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 485, 0, 11, 8, 0.5f)),
                                                owner.emitParticles(-1, 485, 1, 11, 8, 0.5f),
                                                0.6f)
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.shakeCombatant(16, 2, 4, 0.016f, 0.048f, 1.0f, 0.0f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
