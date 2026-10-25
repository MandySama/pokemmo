package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 跳跃暗暗（自定义动画注册 ID：1023）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵显隐；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111023（名称）和 121023（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/BounceAnimation.java
 *
 * <p>源文件 SHA-256：1f4222802f532f38643af0097cc29ab80d68f21a1d2ad38cf39c6e72d86fbbaa
 */
final class ReferenceMoveProgram1023 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1407,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        2,
                                                        16,
                                                        666.6667f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1705,
                                                        1,
                                                        16,
                                                        500.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 0, 0.0f, 480.0f))
                                        .onStart(owner.preloadParticleEffect(512)),
                                owner.emitParticles(-1, 512, 0, 11, 8, 0.25f),
                                0.4f)
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 2, 0.0f, 0.064f, 0.30004883f, -0.30004883f))
                        .append(owner.emitParticles(-1, 512, 1, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 512, 2, 11, 8, 0.5f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.setCombatantVisibility(14, 4))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
