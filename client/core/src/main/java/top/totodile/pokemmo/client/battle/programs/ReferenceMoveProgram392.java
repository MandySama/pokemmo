package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 水流环（招式 ID：392）的攻击／事件阶段动画。
 *
 * <p>招式描述：在自己身体的周围覆盖用水制造的幕. 每回合回复HP.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110392（名称）和 120392（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/AquaRingAnimation.java
 *
 * <p>源文件 SHA-256：71b695de3babcdf777c3b24e157d51b4cdb16328ef8f89e9a7252aa4d83db390
 */
final class ReferenceMoveProgram392 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .onStart(owner.setHealthHudHidden(18, true))
                                        .append(owner.focusCamera(0, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1461,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1461,
                                                        1,
                                                        14,
                                                        500.0f,
                                                        0.234375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1483,
                                                        2,
                                                        14,
                                                        0.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(567))
                                        .append(owner.emitParticles(-1, 567, 0, 9, 8, 0.5f)),
                                owner.emitParticles(-1, 567, 1, 9, 8, 0.5f))
                        .append(owner.restoreCamera(0.4f))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
