package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 写生（招式 ID：166）的攻击／事件阶段动画。
 *
 * <p>招式描述：将对手使用的招式变成自己的招式. 使用1次后写生消失.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110166（名称）和 120166（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/SketchAnimation.java
 *
 * <p>源文件 SHA-256：238f607df7a57f2806c2a59793f57dcd462c8cab693fff33b4ea09c9adaf9182
 */
final class ReferenceMoveProgram166 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .onStart(owner.setHealthHudHidden(16, true))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1412,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1350,
                                                        1,
                                                        16,
                                                        2000.0f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1541,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1541,
                                                        2,
                                                        16,
                                                        666.6667f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1541,
                                                        2,
                                                        16,
                                                        1333.3334f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 1, 1280.0f, 800.0f))
                                        .onStart(owner.preloadParticleEffect(332))
                                        .append(owner.emitParticles(-1, 332, 0, 11, 8, 0.25f)),
                                owner.emitParticles(-1, 332, 1, 11, 8, 0.25f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .onStart(owner.setHealthHudHidden(16, false))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
