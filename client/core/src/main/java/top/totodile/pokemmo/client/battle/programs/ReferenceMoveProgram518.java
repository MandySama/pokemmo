package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 水之誓约（招式 ID：518）的攻击／事件阶段动画。
 *
 * <p>招式描述：用水柱进行攻击. 如果和火组合, 威力就会提高, 天空中会挂上彩虹.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110518（名称）和 120518（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/WaterPledgeAnimation.java
 *
 * <p>源文件 SHA-256：b4aee38c7c2c9b8feccbf4fb17b1e2f177f9c8a453489a744ac7597d4f25f7a9
 */
final class ReferenceMoveProgram518 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .onStart(
                                                                                owner
                                                                                        .setHealthHudHidden(
                                                                                                18,
                                                                                                true))
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                683))
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        1, 0.25f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1489,
                                                                                        1,
                                                                                        16,
                                                                                        0.0f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1489,
                                                                                        1,
                                                                                        16,
                                                                                        666.6667f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1489,
                                                                                        1,
                                                                                        16,
                                                                                        1333.3334f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1445,
                                                                                        2,
                                                                                        16,
                                                                                        0.0f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1445,
                                                                                        2,
                                                                                        16,
                                                                                        666.6667f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1445,
                                                                                        2,
                                                                                        16,
                                                                                        1333.3334f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1376,
                                                                                        2,
                                                                                        16,
                                                                                        2333.3333f,
                                                                                        0.0f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                2,
                                                                                                0,
                                                                                                1280.0f,
                                                                                                960.0f))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                2,
                                                                                                1,
                                                                                                1120.0f,
                                                                                                1120.0f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 683, 7,
                                                                                        11, 8,
                                                                                        0.0f)),
                                                                owner.emitParticles(
                                                                        -1, 683, 1, 11, 8, 0.0f),
                                                                0.6f)
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        16, 1))
                                                        .append(
                                                                owner.scaleCombatant(
                                                                        16,
                                                                        2,
                                                                        10,
                                                                        0.016f,
                                                                        0.032f,
                                                                        0.30004883f,
                                                                        -0.100097656f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 683, 8, 11, 8, 0.0f)),
                                                owner.emitParticles(-1, 683, 2, 11, 8, 0.0f),
                                                1.2f)
                                        .append(owner.emitParticles(-1, 683, 6, 11, 8, 0.0f)),
                                owner.emitParticles(-1, 683, 0, 11, 8, 0.0f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
