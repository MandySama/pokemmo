package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 清除之烟（招式 ID：499）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手投掷特殊的泥块进行攻击. 使其能力变回原点.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110499（名称）和 120499（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/ClearSmogAnimation.java
 *
 * <p>源文件 SHA-256：6160ede5a97801237b7e2956820fa8e741f54c97de86ced78a431229667ac9e3
 */
final class ReferenceMoveProgram499 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenNextParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .sequence()
                                                                                        .beginParallel()
                                                                                        .onStart(
                                                                                                owner
                                                                                                        .setHealthHudHidden(
                                                                                                                18,
                                                                                                                true))
                                                                                        .append(
                                                                                                owner
                                                                                                        .focusCamera(
                                                                                                                0,
                                                                                                                0.4f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                (short)
                                                                                                                        1535,
                                                                                                                1,
                                                                                                                14,
                                                                                                                0.0f,
                                                                                                                0.9921875f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                (short)
                                                                                                                        1439,
                                                                                                                2,
                                                                                                                14,
                                                                                                                0.0f,
                                                                                                                0.625f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .adjustSoundTrack(
                                                                                                                2,
                                                                                                                1,
                                                                                                                1120.0f,
                                                                                                                160.0f))
                                                                                        .onStart(
                                                                                                owner
                                                                                                        .preloadParticleEffect(
                                                                                                                666)),
                                                                                owner
                                                                                        .launchParticles(
                                                                                                666,
                                                                                                1,
                                                                                                9,
                                                                                                11,
                                                                                                0.5f,
                                                                                                720.0f))
                                                                        .append(
                                                                                owner
                                                                                        .setSpriteAnimationPaused(
                                                                                                16,
                                                                                                1))
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        1, 0.45f)),
                                                                owner.scaleCombatant(
                                                                        16,
                                                                        2,
                                                                        1,
                                                                        0.0f,
                                                                        0.032f,
                                                                        0.100097656f,
                                                                        -0.100097656f),
                                                                0.4f)
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 666, 0, 11, 8, 0.5f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1895,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle)),
                                                owner.emitParticles(-1, 666, 2, 11, 8, 0.5f),
                                                0.6f),
                                        owner.emitParticles(-1, 666, 3, 11, 8, 0.5f),
                                        0.8f),
                                owner.emitParticles(-1, 666, 2, 11, 8, 0.5f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
