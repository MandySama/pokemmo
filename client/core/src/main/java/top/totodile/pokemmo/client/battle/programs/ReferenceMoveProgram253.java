package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 吵闹（招式 ID：253）的攻击／事件阶段动画。
 *
 * <p>招式描述：在3回合内用骚乱攻击对手. 在此期间谁都不能入眠.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110253（名称）和 120253（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/UproarAnimation.java
 *
 * <p>源文件 SHA-256：74217c3d90bba61b5aaa70f35a408c35d3ed5a44aeb60b1452c165eb75960159
 */
final class ReferenceMoveProgram253 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .onStart(owner.setHealthHudHidden(18, true))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1501,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1500,
                                                        2,
                                                        14,
                                                        0.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1501,
                                                        1,
                                                        14,
                                                        250.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1501,
                                                        1,
                                                        14,
                                                        500.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1501,
                                                        1,
                                                        14,
                                                        750.0f,
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
                                        .append(owner.adjustSoundTrack(2, 1, 1120.0f, 160.0f))
                                        .onStart(owner.preloadParticleEffect(419))
                                        .append(owner.emitParticles(-1, 419, 0, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 419, 1, 9, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        14, 2, 3, 0.016f, 0.096f, 0.19995117f, -0.19995117f))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.restoreCamera(0.3f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
