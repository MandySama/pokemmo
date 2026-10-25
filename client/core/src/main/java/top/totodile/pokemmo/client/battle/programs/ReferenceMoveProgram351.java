package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 电击波（招式 ID：351）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手快速发出电击. 攻击必定会命中.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110351（名称）和 120351（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/ShockWaveAnimation.java
 *
 * <p>源文件 SHA-256：57d915fa88f87f1c6322ddfc9023d6746dbca84a8960e6358e0f8479cddcb11b
 */
final class ReferenceMoveProgram351 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep13 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .onStart(owner.preloadParticleEffect(524))
                                        .append(owner.emitParticles(-1, 524, 2, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 524, 3, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 524, 4, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 524, 0, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 524, 1, 9, 8, 0.5f))
                                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.75f, 0.05f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1497,
                                                        1,
                                                        14,
                                                        666.6667f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        1,
                                                        14,
                                                        2000.0f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 1280.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1458,
                                                        2,
                                                        14,
                                                        1166.6666f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1458,
                                                        2,
                                                        14,
                                                        1500.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1683,
                                                        1,
                                                        14,
                                                        2166.6667f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1682,
                                                        2,
                                                        14,
                                                        2166.6667f,
                                                        0.8984375f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1458,
                                        2,
                                        16,
                                        2916.6667f,
                                        0.8984375f,
                                        owner.casterHandle),
                                3.0f)
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(3.4f)
                        .beginParallel();
        Color color = EncounterMoveAnimation.color(1023);
        ReferenceMoveTimeline animationStep14 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        animationStep13
                                .append(owner.emitParticles(-1, 524, 5, 11, 8, 0.5f))
                                .append(owner.tintCombatant(16, 0.5f, 0.0f, 0.75f, color)),
                        owner.shakeCombatant(16, 2, 8, 0.032f, 0.016f, 0.30004883f, 0.0f));
        color = EncounterMoveAnimation.color(1023);
        result =
                animationStep14
                        .append(owner.tintCombatant(16, 0.5f, 0.75f, 0.0f, color))
                        .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.05f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
