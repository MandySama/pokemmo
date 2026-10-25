package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 真空波（招式 ID：410）的攻击／事件阶段动画。
 *
 * <p>招式描述：挥动拳头, 掀起真空波. 必定能够先制攻击.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110410（名称）和 120410（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/VacuumWaveAnimation.java
 *
 * <p>源文件 SHA-256：756ea147be97de9cbee8d77893d92c693dfd69bce0a2143356eea981f5705609
 */
final class ReferenceMoveProgram410 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .onStart(owner.setHealthHudHidden(18, true))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1425,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        1,
                                                        16,
                                                        500.0f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 480.0f))
                                        .append(owner.adjustSoundTrack(1, 1, 320.0f, 160.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1424,
                                                        1,
                                                        16,
                                                        500.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        500.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(585))
                                        .append(owner.emitParticles(-1, 585, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 585, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 585, 2, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 585, 3, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.064f, 0.30004883f, -0.30004883f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
