package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 流沙深渊（招式 ID：328）的攻击／事件阶段动画。
 *
 * <p>招式描述：将对手困在铺天盖地的沙暴中, 在4-5回合内进行攻击.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110328（名称）和 120328（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/SandTombAnimation.java
 *
 * <p>源文件 SHA-256：025dde4b92f6a59512c446c51cecca4fd25f9451f418bd46296ef2dddabccd39
 */
final class ReferenceMoveProgram328 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .onStart(owner.setHealthHudHidden(18, true))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1439,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1497,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        2,
                                                        16,
                                                        2166.6667f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        1,
                                                        16,
                                                        2166.6667f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 1, 1600.0f, 480.0f))
                                        .append(owner.adjustSoundTrack(2, 1, 1600.0f, 480.0f))
                                        .onStart(owner.preloadParticleEffect(498))
                                        .append(owner.emitParticles(-1, 498, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 498, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 498, 2, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .beginSequence()
                                        .waitSeconds(0.6f)
                                        .beginParallel(),
                                owner.shakeCombatant(16, 2, 8, 0.016f, 0.048f, 1.0f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
