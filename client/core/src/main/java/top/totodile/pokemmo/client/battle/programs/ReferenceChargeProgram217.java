package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 礼物（招式 ID：217）的蓄力阶段动画。
 *
 * <p>招式描述：递给对手设有圈套的盒子进行攻击. 也有可能回复对手HP.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110217（名称）和 120217（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/PresentAnimation.java
 *
 * <p>源文件 SHA-256：019f9bfae7e92d5c1df9e034f5622129f15139cf70b727000165a303b68ada2f
 */
final class ReferenceChargeProgram217 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.325f))
                                                        .onStart(owner.preloadParticleEffect(383))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1510,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1541,
                                                                        2,
                                                                        16,
                                                                        500.0f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1475,
                                                                        1,
                                                                        16,
                                                                        500.0f,
                                                                        0.8984375f,
                                                                        owner.casterHandle)),
                                                owner.launchParticles(383, 3, 9, 11, 0.5f, 240.0f))
                                        .append(owner.emitParticles(-1, 383, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 383, 0, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(owner.emitParticles(-1, 383, 2, 11, 8, 0.5f)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.032f, 0.064f, 0.30004883f, -0.30004883f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.325f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
