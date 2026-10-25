package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 大声咆哮（招式 ID：555）的攻击／事件阶段动画。
 *
 * <p>招式描述：没完没了地大声斥责, 从而降低对手的特攻.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110555（名称）和 120555（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/SnarlAnimation.java
 *
 * <p>源文件 SHA-256：54d6f7c85769c9ad8ada08777de0fc272df4a6262fa5afe9bc8948e5a5c64b06
 */
final class ReferenceMoveProgram555 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .onStart(owner.setHealthHudHidden(18, true))
                                        .append(owner.focusCamera(0, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1467,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1455,
                                                        2,
                                                        14,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 0, 0.0f, 1280.0f))
                                        .append(owner.adjustSoundTrack(2, 1, 960.0f, 320.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        2,
                                                        14,
                                                        1333.3334f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(726))
                                        .append(owner.emitParticles(-1, 726, 1, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 726, 3, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 726, 0, 9, 11, 0.5f))
                                        .append(owner.emitParticles(-1, 726, 2, 11, 8, 0.5f))
                                        .append(
                                                owner.scaleCombatant(
                                                        14,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.32f,
                                                        0.19995117f,
                                                        -0.19995117f)),
                                owner.shakeCombatant(14, 2, 10, 0.0f, 0.048f, 0.30004883f, 0.0f),
                                0.8f)
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 4, 0.016f, 0.032f, -0.19995117f, 0.19995117f))
                        .append(owner.focusCamera(1, 0.3f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1420,
                                        1,
                                        16,
                                        166.66667f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1420,
                                        1,
                                        16,
                                        250.0f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1420,
                                        1,
                                        16,
                                        333.33334f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.restoreCamera(0.3f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
