package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 下盘踢（招式 ID：490）的攻击／事件阶段动画。
 *
 * <p>招式描述：以敏捷的动作瞄准对手的脚进行攻击. 降低对手的速度.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110490（名称）和 120490（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/LowSweepAnimation.java
 *
 * <p>源文件 SHA-256：fb38c8689171ab3be7bbbce5bc3d02e13cef8197e038f642fcfe2fd1f3eb3c0a
 */
final class ReferenceMoveProgram490 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1415,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 1, 240.0f, 128.0f))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .beginSequence()
                                        .waitSeconds(0.4f)
                                        .beginParallel()
                                        .onStart(owner.preloadParticleEffect(185))
                                        .append(owner.emitParticles(-1, 185, 0, 11, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 185, 1, 11, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 185, 2, 11, 8, 0.25f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1653,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.0f, 0.096f, -0.30004883f, 0.30004883f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
