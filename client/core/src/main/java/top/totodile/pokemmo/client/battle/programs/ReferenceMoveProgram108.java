package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 烟幕（招式 ID：108）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手喷出烟或墨汁等, 从而降低对手的命中率.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110108（名称）和 120108（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SmokescreenAnimation.java
 *
 * <p>源文件 SHA-256：c05c266a41bbb9c0421f7c0c140ad99475f4fe3398be9bc173fb4f9043d01ea6
 */
final class ReferenceMoveProgram108 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
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
                                                                                                                        1426,
                                                                                                                1,
                                                                                                                16,
                                                                                                                0.0f,
                                                                                                                0.9375f,
                                                                                                                owner.casterHandle))
                                                                                        .onStart(
                                                                                                owner
                                                                                                        .preloadParticleEffect(
                                                                                                                273)),
                                                                                owner
                                                                                        .launchParticles(
                                                                                                273,
                                                                                                0,
                                                                                                9,
                                                                                                11,
                                                                                                0.5f,
                                                                                                360.0f),
                                                                                0.4f)
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        1, 0.4f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1426,
                                                                                        1,
                                                                                        16,
                                                                                        333.33334f,
                                                                                        0.9375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1407,
                                                                                        2,
                                                                                        16,
                                                                                        333.33334f,
                                                                                        0.625f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1376,
                                                                                        2,
                                                                                        16,
                                                                                        1500.0f,
                                                                                        0.0f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                2,
                                                                                                1,
                                                                                                480.0f,
                                                                                                960.0f)),
                                                                owner.emitParticles(
                                                                        -1, 273, 1, 11, 8, 0.5f),
                                                                0.52f),
                                                owner.emitParticles(-1, 273, 1, 11, 8, 0.5f),
                                                0.62f)
                                        .append(owner.emitParticles(-1, 273, 1, 11, 8, 0.25f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, 0.30004883f, -0.30004883f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
