package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 地震（招式 ID：89）的攻击／事件阶段动画。
 *
 * <p>招式描述：利用地震的冲击, 攻击自己周围所有的宝可梦.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110089（名称）和 120089（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/EarthquakeAnimation.java
 *
 * <p>源文件 SHA-256：0d9bf20f6dbf8fdc858f5d76388d306744db41e9be65854675a8c922777b21c6
 */
final class ReferenceMoveProgram89 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.703125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        2,
                                                        16,
                                                        666.6667f,
                                                        0.703125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        2,
                                                        16,
                                                        833.3333f,
                                                        0.703125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        2,
                                                        16,
                                                        1166.6666f,
                                                        0.703125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        2,
                                                        16,
                                                        1333.3334f,
                                                        0.703125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1416,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9765625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1418,
                                                        3,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
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
                                                        (short) 1376,
                                                        2,
                                                        16,
                                                        1833.3334f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        3,
                                                        16,
                                                        1833.3334f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(3, 1, 1280.0f, 320.0f))
                                        .append(owner.adjustSoundTrack(2, 1, 960.0f, 800.0f))
                                        .append(owner.adjustSoundTrack(1, 1, 960.0f, 800.0f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .onStart(owner.preloadParticleEffect(256))
                                        .append(owner.emitParticles(-1, 256, 0, 11, 8, 0.0f))
                                        .append(owner.emitParticles(-1, 256, 1, 11, 8, 0.0f)),
                                owner.shakeCombatant(16, 2, 9, 0.016f, 0.032f, 0.30004883f, 0.0f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
