package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 充电光束（招式 ID：451）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手发射电击光束. 由于蓄满电流, 有时会提高自己的特攻.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110451（名称）和 120451（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/ChargeBeamAnimation.java
 *
 * <p>源文件 SHA-256：6a7014e24f75b94e27bde0dd4db4422123b0bb91a7d8de1b0f23b9d02503fb53
 */
final class ReferenceMoveProgram451 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep11 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.75f, 0.05f))
                        .onStart(owner.preloadParticleEffect(626))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1445,
                                        1,
                                        14,
                                        0.0f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        14,
                                        1500.0f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 1440.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1963,
                                        2,
                                        14,
                                        1500.0f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        2,
                                        14,
                                        2833.3333f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 1, 2240.0f, 480.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1407,
                                        1,
                                        14,
                                        1500.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        14,
                                        2833.3333f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 1440.0f))
                        .append(owner.adjustSoundTrack(1, 1, 2240.0f, 480.0f))
                        .append(owner.emitParticles(-1, 626, 1, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 626, 2, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 626, 3, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 626, 4, 9, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(13311);
        ReferenceMoveTimeline animationStep12 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        animationStep11,
                                                        owner.emitParticles(
                                                                -1, 626, 0, 9, 11, 0.5f),
                                                        1.6f)
                                                .append(owner.focusCamera(1, 0.8f)),
                                        owner.tintCombatant(16, 0.5f, 0.0f, 0.75f, color),
                                        2.0f)
                                .append(owner.setSpriteAnimationPaused(16, 1)),
                        owner.shakeCombatant(16, 2, 8, 0.032f, 0.016f, 0.30004883f, 0.0f),
                        2.6f);
        color = EncounterMoveAnimation.color(13311);
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendThenOuterParallel(
                                        animationStep12,
                                        owner.tintCombatant(16, 0.5f, 0.75f, 0.0f, color))
                                .append(owner.setSpriteAnimationPaused(14, 0))
                                .append(owner.setSpriteAnimationPaused(16, 0))
                                .append(owner.restoreCamera(0.4f)),
                        owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.05f));
        return result;
    }
}
