package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 龙之波动（招式 ID：406）的攻击／事件阶段动画。
 *
 * <p>招式描述：从大大的口中掀起冲击波攻击对手.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110406（名称）和 120406（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/DragonPulseAnimation.java
 *
 * <p>源文件 SHA-256：bedf6572abdcf3607c6009b729dc69e37a96e08d52e719d1764c2fd32c512608
 */
final class ReferenceMoveProgram406 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        0, 0.4f))
                                                                        .append(
                                                                                owner
                                                                                        .setSpriteAnimationPaused(
                                                                                                14,
                                                                                                1))
                                                                        .append(
                                                                                owner
                                                                                        .tintEnvironment(
                                                                                                4,
                                                                                                0,
                                                                                                0.0f,
                                                                                                0.8125f,
                                                                                                0.025f))
                                                                        .onStart(
                                                                                owner
                                                                                        .setHealthHudHidden(
                                                                                                18,
                                                                                                true))
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                581))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1445,
                                                                                        1,
                                                                                        14,
                                                                                        0.0f,
                                                                                        0.859375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1376,
                                                                                        1,
                                                                                        14,
                                                                                        1166.6666f,
                                                                                        0.0f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                0,
                                                                                                0.0f,
                                                                                                1120.0f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1425,
                                                                                        2,
                                                                                        14,
                                                                                        1166.6666f,
                                                                                        0.859375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1376,
                                                                                        2,
                                                                                        14,
                                                                                        2833.3333f,
                                                                                        0.0f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                2,
                                                                                                1,
                                                                                                2240.0f,
                                                                                                480.0f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1407,
                                                                                        1,
                                                                                        14,
                                                                                        1166.6666f,
                                                                                        0.8984375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1376,
                                                                                        1,
                                                                                        14,
                                                                                        2833.3333f,
                                                                                        0.0f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1460,
                                                                                        2,
                                                                                        16,
                                                                                        1666.6666f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1460,
                                                                                        2,
                                                                                        16,
                                                                                        1766.6666f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1460,
                                                                                        2,
                                                                                        16,
                                                                                        1866.6666f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1460,
                                                                                        2,
                                                                                        16,
                                                                                        1966.6666f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                0,
                                                                                                0.0f,
                                                                                                1600.0f))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                1,
                                                                                                2240.0f,
                                                                                                480.0f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 581, 1,
                                                                                        9, 8, 0.5f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 581, 2,
                                                                                        9, 8,
                                                                                        0.5f)),
                                                                owner.emitParticles(
                                                                        -1, 581, 3, 9, 8, 0.5f),
                                                                1.2f)
                                                        .append(owner.focusCamera(1, 0.8f)),
                                                owner.emitParticles(-1, 581, 0, 9, 11, 0.5f),
                                                1.6f)
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.shakeCombatant(16, 2, 14, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.tintEnvironment(4, 0, 0.8125f, 0.0f, 0.025f))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
