package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 超音波（招式 ID：48）的攻击／事件阶段动画。
 *
 * <p>招式描述：从身体发出特殊的音波, 从而使对手混乱.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110048（名称）和 120048（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SupersonicAnimation.java
 *
 * <p>源文件 SHA-256：6bd984b367051e3a2c3549de6458b445d37de717ab06f3fdf91e69ef4ab6d36e
 */
final class ReferenceMoveProgram48 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendThenOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenDelayedOuterParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .appendThenDelayedParallel(
                                                                                                ReferenceMoveTimeline
                                                                                                        .appendThenNextParallel(
                                                                                                                ReferenceMoveTimeline
                                                                                                                        .sequence()
                                                                                                                        .beginParallel()
                                                                                                                        .waitSeconds(
                                                                                                                                0.6f),
                                                                                                                owner
                                                                                                                        .tintEnvironment(
                                                                                                                                4,
                                                                                                                                0,
                                                                                                                                0.0f,
                                                                                                                                0.75f,
                                                                                                                                0.05f))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scheduleSound(
                                                                                                                                (byte)
                                                                                                                                        2,
                                                                                                                                (short)
                                                                                                                                        1450,
                                                                                                                                1,
                                                                                                                                16,
                                                                                                                                0.0f,
                                                                                                                                0.859375f,
                                                                                                                                owner.casterHandle))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scheduleSound(
                                                                                                                                (byte)
                                                                                                                                        2,
                                                                                                                                (short)
                                                                                                                                        1376,
                                                                                                                                1,
                                                                                                                                16,
                                                                                                                                1166.6666f,
                                                                                                                                0.0f,
                                                                                                                                owner.casterHandle))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scheduleSound(
                                                                                                                                (byte)
                                                                                                                                        2,
                                                                                                                                (short)
                                                                                                                                        1451,
                                                                                                                                2,
                                                                                                                                16,
                                                                                                                                1166.6666f,
                                                                                                                                0.9375f,
                                                                                                                                owner.casterHandle))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scheduleSound(
                                                                                                                                (byte)
                                                                                                                                        2,
                                                                                                                                (short)
                                                                                                                                        1451,
                                                                                                                                1,
                                                                                                                                16,
                                                                                                                                1500.0f,
                                                                                                                                0.46875f,
                                                                                                                                owner.casterHandle))
                                                                                                        .onStart(
                                                                                                                owner
                                                                                                                        .preloadParticleEffect(
                                                                                                                                209)),
                                                                                                owner
                                                                                                        .launchParticles(
                                                                                                                209,
                                                                                                                0,
                                                                                                                9,
                                                                                                                11,
                                                                                                                0.5f,
                                                                                                                240.0f),
                                                                                                0.04f),
                                                                                owner
                                                                                        .launchParticles(
                                                                                                209,
                                                                                                0,
                                                                                                9,
                                                                                                11,
                                                                                                0.5f,
                                                                                                240.0f),
                                                                                0.08f),
                                                                owner.launchParticles(
                                                                        209, 0, 9, 11, 0.5f,
                                                                        240.0f),
                                                                0.28f)
                                                        .append(owner.focusCamera(1, 0.25f))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        16, 1)),
                                                owner.emitParticles(-1, 209, 1, 11, 8, 0.5f),
                                                0.88f),
                                        owner.shakeCombatant(
                                                16, 2, 3, 0.0f, 0.032f, 0.30004883f, 0.0f))
                                .append(owner.setSpriteAnimationPaused(16, 0))
                                .append(owner.restoreCamera(0.4f)),
                        owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.05f));
        return result;
    }
}
