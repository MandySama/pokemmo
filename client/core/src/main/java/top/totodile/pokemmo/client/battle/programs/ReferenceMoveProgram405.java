package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 虫鸣（招式 ID：405）的攻击／事件阶段动画。
 *
 * <p>招式描述：利用振动发出音波进行攻击. 有时会降低对手的特防.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110405（名称）和 120405（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/BugBuzzAnimation.java
 *
 * <p>源文件 SHA-256：145253778fdc53b3129cbb1ec87d48f83be20caab009e66c2b1da8d555565ec0
 */
final class ReferenceMoveProgram405 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.25f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1572,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1572,
                                                        1,
                                                        14,
                                                        833.3333f,
                                                        0.546875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1450,
                                                        2,
                                                        14,
                                                        0.0f,
                                                        0.625f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 0, 0.0f, 1440.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        2,
                                                        14,
                                                        1250.0f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(580))
                                        .append(owner.emitParticles(-1, 580, 0, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 580, 1, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 580, 2, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 580, 3, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 580, 4, 9, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .endGroup()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.25f))
                                        .append(
                                                owner.shakeCombatant(
                                                        16,
                                                        2,
                                                        6,
                                                        0.016f,
                                                        0.032f,
                                                        0.19995117f,
                                                        0.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1728,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle)),
                                owner.adjustSoundTrack(1, 1, 320.0f, 480.0f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.25f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
