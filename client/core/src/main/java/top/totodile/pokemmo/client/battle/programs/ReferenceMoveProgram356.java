package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 重力（招式 ID：356）的攻击／事件阶段动画。
 *
 * <p>招式描述：在5回合内, 飘浮特性和飞行属性的宝可梦会被地面属性的招式击中. 飞向空中的招式也将无法使用.
 *
 * <p>动画说明：播放参考粒子特效；调整场景着色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110356（名称）和 120356（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/GravityAnimation.java
 *
 * <p>源文件 SHA-256：23e43f4240ee4c72fd2f39b2dc32d822d95e67f34856cc01451ff4292d9ad388
 */
final class ReferenceMoveProgram356 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .onStart(owner.setHealthHudHidden(18, true))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        16, 1))
                                                        .onStart(owner.preloadParticleEffect(529))
                                                        .onStart(owner.preloadParticleEffect(530))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1497,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1376,
                                                                        1,
                                                                        14,
                                                                        1833.3334f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 0, 0.0f, 1760.0f))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 1, 1440.0f, 320.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1535,
                                                                        2,
                                                                        16,
                                                                        750.0f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1,
                                                                        529,
                                                                        0,
                                                                        9,
                                                                        8,
                                                                        0.025024414f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1,
                                                                        530,
                                                                        0,
                                                                        9,
                                                                        8,
                                                                        0.025024414f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 529, 1, 0, 8, 0.0f)),
                                                owner.tintEnvironment(4, 0, 0.0f, 0.375f, 0.05f),
                                                0.4f)
                                        .append(owner.restoreCamera(1.05f))
                                        .append(
                                                owner.shakeCombatant(
                                                        16,
                                                        2,
                                                        15,
                                                        0.032f,
                                                        0.016f,
                                                        0.0f,
                                                        0.19995117f)),
                                owner.scaleCombatant(16, 2, 1, 0.016f, 0.352f, 0.0f, -0.6999512f))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.tintEnvironment(4, 0, 0.375f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
