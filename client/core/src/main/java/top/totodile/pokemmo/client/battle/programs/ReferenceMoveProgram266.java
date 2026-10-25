package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 看我嘛（招式 ID：266）的攻击／事件阶段动画。
 *
 * <p>招式描述：引起对手的注意, 将对手的攻击全部转移到自己身上.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110266（名称）和 120266（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/FollowMeAnimation.java
 *
 * <p>源文件 SHA-256：7e3d6656c05c8fbc6af9ef18bbef0c7f7a9f5fd0154120bb6220482eb9d6b4ed
 */
final class ReferenceMoveProgram266 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1480,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.703125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        1,
                                                        14,
                                                        1333.3334f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 1, 640.0f, 640.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1552,
                                                        2,
                                                        14,
                                                        1250.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1552,
                                                        2,
                                                        14,
                                                        1583.3334f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .onStart(owner.preloadParticleEffect(432))
                                        .append(owner.emitParticles(-1, 432, 0, 9, 8, 0.5f)),
                                owner.emitParticles(-1, 432, 1, 9, 8, 0.5f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
