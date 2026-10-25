package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 觉醒力量（招式 ID：237）的攻击／事件阶段动画。
 *
 * <p>招式描述：技能的属性取决于不同的使用者. 属性可以用40个宝石修改.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110237（名称）和 120237（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/HiddenPowerAnimation.java
 *
 * <p>源文件 SHA-256：ca5ab6f287e3d978d1bb511fa756b5ed7ea9201abe99ef047c1b685ce1dfd06e
 */
final class ReferenceMoveProgram237 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(
                                                                owner.tintEnvironment(
                                                                        4, 0, 0.0f, 0.8125f,
                                                                        0.075f))
                                                        .onStart(owner.setHealthHudHidden(18, true))
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1483,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1417,
                                                                        2,
                                                                        14,
                                                                        0.0f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1376,
                                                                        2,
                                                                        14,
                                                                        1333.3334f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 0, 0.0f, 1280.0f))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        2, 0, 0.0f, 1280.0f))
                                                        .onStart(owner.preloadParticleEffect(404)),
                                                owner.emitParticles(-1, 404, 1, 9, 8, 0.5f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1509,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1358,
                                                        2,
                                                        14,
                                                        0.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(owner.emitParticles(-1, 404, 3, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 404, 4, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 404, 2, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 404, 0, 9, 8, 0.5f))
                                        .append(owner.restoreCamera(0.3f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .beginSequence()
                                        .waitSeconds(0.4f)
                                        .beginParallel(),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.064f, -0.30004883f, 0.30004883f))
                        .append(owner.tintEnvironment(4, 0, 0.8125f, 0.0f, 0.075f))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
