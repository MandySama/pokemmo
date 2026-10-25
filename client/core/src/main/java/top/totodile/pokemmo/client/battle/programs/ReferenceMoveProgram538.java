package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 棉花防守（招式 ID：538）的攻击／事件阶段动画。
 *
 * <p>招式描述：用软绵绵的绒毛包裹住自己的身体进行守护. 巨幅提高自己的防御.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110538（名称）和 120538（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/CottonGuardAnimation.java
 *
 * <p>源文件 SHA-256：1408757dbe45d7c9a7e49adf758f2a45007f7c13f1f8d888d63561f77fb49832
 */
final class ReferenceMoveProgram538 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1535,
                                        1,
                                        14,
                                        0.0f,
                                        0.3125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1410,
                                        2,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 0, 0.0f, 480.0f))
                        .append(owner.adjustSoundTrack(2, 1, 320.0f, 160.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1733,
                                        1,
                                        14,
                                        833.3333f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1733,
                                        2,
                                        14,
                                        1033.3334f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1733,
                                        1,
                                        14,
                                        1166.6666f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1733,
                                        2,
                                        14,
                                        1300.0f,
                                        0.703125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1733,
                                        1,
                                        14,
                                        1400.0f,
                                        0.46875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1733,
                                        2,
                                        14,
                                        1483.3334f,
                                        0.390625f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1733,
                                        1,
                                        14,
                                        1566.6666f,
                                        0.3125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1733,
                                        2,
                                        14,
                                        1650.0f,
                                        0.234375f,
                                        owner.casterHandle))
                        .onStart(owner.setHealthHudHidden(18, true))
                        .append(owner.focusCamera(0, 0.4f))
                        .onStart(owner.preloadParticleEffect(702))
                        .append(owner.emitParticles(-1, 702, 0, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 702, 3, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 702, 1, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 702, 2, 9, 8, 0.125f))
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .endGroup()
                        .beginParallel()
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
