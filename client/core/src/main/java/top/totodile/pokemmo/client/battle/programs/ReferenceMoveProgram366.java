package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 顺风（招式 ID：366）的攻击／事件阶段动画。
 *
 * <p>招式描述：刮起猛烈的旋风, 在4回合内提高我方全员的速度.
 *
 * <p>动画说明：播放参考粒子特效；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110366（名称）和 120366（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/TailwindAnimation.java
 *
 * <p>源文件 SHA-256：25451a9d197a329dc10deff666b36f99a4c89786cffd1f11fde214becb7eb82c
 */
final class ReferenceMoveProgram366 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .waitSeconds(0.4f)
                                        .onStart(owner.preloadParticleEffect(540))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1497,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        1,
                                                        14,
                                                        1833.3334f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 1760.0f))
                                        .append(owner.adjustSoundTrack(1, 1, 1440.0f, 320.0f))
                                        .append(owner.emitParticles(-1, 540, 0, 9, 8, 0.5f)),
                                owner.emitParticles(-1, 540, 1, 9, 8, 0.0f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
