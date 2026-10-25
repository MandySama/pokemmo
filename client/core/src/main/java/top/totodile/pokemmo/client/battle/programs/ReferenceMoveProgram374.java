package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 投掷（招式 ID：374）的攻击／事件阶段动画。
 *
 * <p>招式描述：快速投掷携带的道具进行攻击. PvP 中仅可消耗道具会被永久销毁. 根据道具不同, 威力和效果会改变.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110374（名称）和 120374（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/FlingAnimation.java
 *
 * <p>源文件 SHA-256：3585e5b741271c231de1b956a99844d14ad4b7042a7bfe130d50b59c99d4d203
 */
final class ReferenceMoveProgram374 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenDelayedOuterParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .appendThenDelayedParallel(
                                                                                                ReferenceMoveTimeline
                                                                                                        .waitThenParallel(
                                                                                                                0.6f)
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scheduleSound(
                                                                                                                                (byte)
                                                                                                                                        2,
                                                                                                                                (short)
                                                                                                                                        1459,
                                                                                                                                1,
                                                                                                                                16,
                                                                                                                                0.0f,
                                                                                                                                0.546875f,
                                                                                                                                owner.casterHandle))
                                                                                                        .onStart(
                                                                                                                owner
                                                                                                                        .preloadParticleEffect(
                                                                                                                                549)),
                                                                                                owner
                                                                                                        .launchParticles(
                                                                                                                549,
                                                                                                                0,
                                                                                                                9,
                                                                                                                11,
                                                                                                                0.5f,
                                                                                                                360.0f),
                                                                                                0.02f),
                                                                                owner
                                                                                        .launchParticles(
                                                                                                549,
                                                                                                0,
                                                                                                9,
                                                                                                11,
                                                                                                0.5f,
                                                                                                360.0f),
                                                                                0.04f),
                                                                owner.launchParticles(
                                                                        549, 0, 9, 11, 0.5f,
                                                                        360.0f),
                                                                0.08f)
                                                        .append(owner.focusCamera(1, 0.4f)),
                                                owner.launchParticles(549, 0, 9, 11, 0.5f, 360.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1468,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1469,
                                                        2,
                                                        16,
                                                        50.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 1, 0.0f, 480.0f))
                                        .append(owner.emitParticles(-1, 549, 2, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 549, 1, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.064f, 0.19995117f, -0.19995117f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
