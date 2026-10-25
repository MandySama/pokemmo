package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 吐丝（招式 ID：81）的攻击／事件阶段动画。
 *
 * <p>招式描述：用口中吐出的丝缠绕对手, 从而大幅降低对手的速度.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110081（名称）和 120081（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/StringShotAnimation.java
 *
 * <p>源文件 SHA-256：0db1e4bd098a8ea6ccc93e30904b96e8d771e538c7473852c7d708b2ee25176b
 */
final class ReferenceMoveProgram81 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenDelayedOuterParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .appendThenDelayedOuterParallel(
                                                                                                ReferenceMoveTimeline
                                                                                                        .appendThenDelayedOuterParallel(
                                                                                                                ReferenceMoveTimeline
                                                                                                                        .appendThenDelayedOuterParallel(
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
                                                                                                                                                                                                        .append(
                                                                                                                                                                                                                owner
                                                                                                                                                                                                                        .focusCamera(
                                                                                                                                                                                                                                0,
                                                                                                                                                                                                                                0.3f)),
                                                                                                                                                                                                owner
                                                                                                                                                                                                        .tintEnvironment(
                                                                                                                                                                                                                2,
                                                                                                                                                                                                                0,
                                                                                                                                                                                                                0.0f,
                                                                                                                                                                                                                0.8125f,
                                                                                                                                                                                                                0.05f))
                                                                                                                                                                                        .append(
                                                                                                                                                                                                owner
                                                                                                                                                                                                        .scheduleSound(
                                                                                                                                                                                                                (byte)
                                                                                                                                                                                                                        2,
                                                                                                                                                                                                                (short)
                                                                                                                                                                                                                        1410,
                                                                                                                                                                                                                1,
                                                                                                                                                                                                                14,
                                                                                                                                                                                                                0.0f,
                                                                                                                                                                                                                0.9375f,
                                                                                                                                                                                                                owner.casterHandle))
                                                                                                                                                                                        .append(
                                                                                                                                                                                                owner
                                                                                                                                                                                                        .scheduleSound(
                                                                                                                                                                                                                (byte)
                                                                                                                                                                                                                        2,
                                                                                                                                                                                                                (short)
                                                                                                                                                                                                                        1445,
                                                                                                                                                                                                                2,
                                                                                                                                                                                                                16,
                                                                                                                                                                                                                833.3333f,
                                                                                                                                                                                                                0.546875f,
                                                                                                                                                                                                                owner.casterHandle))
                                                                                                                                                                                        .append(
                                                                                                                                                                                                owner
                                                                                                                                                                                                        .scheduleSound(
                                                                                                                                                                                                                (byte)
                                                                                                                                                                                                                        2,
                                                                                                                                                                                                                (short)
                                                                                                                                                                                                                        1376,
                                                                                                                                                                                                                2,
                                                                                                                                                                                                                16,
                                                                                                                                                                                                                1500.0f,
                                                                                                                                                                                                                0.0f,
                                                                                                                                                                                                                owner.casterHandle))
                                                                                                                                                                                        .append(
                                                                                                                                                                                                owner
                                                                                                                                                                                                        .scheduleSound(
                                                                                                                                                                                                                (byte)
                                                                                                                                                                                                                        2,
                                                                                                                                                                                                                (short)
                                                                                                                                                                                                                        1441,
                                                                                                                                                                                                                2,
                                                                                                                                                                                                                16,
                                                                                                                                                                                                                1500.0f,
                                                                                                                                                                                                                0.9375f,
                                                                                                                                                                                                                owner.casterHandle))
                                                                                                                                                                                        .onStart(
                                                                                                                                                                                                owner
                                                                                                                                                                                                        .preloadParticleEffect(
                                                                                                                                                                                                                245)),
                                                                                                                                                                                owner
                                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                                245,
                                                                                                                                                                                                0,
                                                                                                                                                                                                9,
                                                                                                                                                                                                11,
                                                                                                                                                                                                0.5f,
                                                                                                                                                                                                480.0f),
                                                                                                                                                                                0.02f),
                                                                                                                                                                owner
                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                245,
                                                                                                                                                                                0,
                                                                                                                                                                                9,
                                                                                                                                                                                11,
                                                                                                                                                                                0.5f,
                                                                                                                                                                                480.0f),
                                                                                                                                                                0.04f),
                                                                                                                                                owner
                                                                                                                                                        .launchParticles(
                                                                                                                                                                245,
                                                                                                                                                                0,
                                                                                                                                                                9,
                                                                                                                                                                11,
                                                                                                                                                                0.5f,
                                                                                                                                                                480.0f),
                                                                                                                                                0.06f),
                                                                                                                                owner
                                                                                                                                        .launchParticles(
                                                                                                                                                245,
                                                                                                                                                0,
                                                                                                                                                9,
                                                                                                                                                11,
                                                                                                                                                0.5f,
                                                                                                                                                480.0f),
                                                                                                                                0.08f),
                                                                                                                owner
                                                                                                                        .launchParticles(
                                                                                                                                245,
                                                                                                                                0,
                                                                                                                                9,
                                                                                                                                11,
                                                                                                                                0.5f,
                                                                                                                                480.0f),
                                                                                                                0.1f),
                                                                                                owner
                                                                                                        .launchParticles(
                                                                                                                245,
                                                                                                                0,
                                                                                                                9,
                                                                                                                11,
                                                                                                                0.5f,
                                                                                                                480.0f),
                                                                                                0.12f),
                                                                                owner
                                                                                        .launchParticles(
                                                                                                245,
                                                                                                0,
                                                                                                9,
                                                                                                11,
                                                                                                0.5f,
                                                                                                480.0f),
                                                                                0.14f),
                                                                owner.launchParticles(
                                                                        245, 0, 9, 11, 0.5f,
                                                                        480.0f),
                                                                0.16f)
                                                        .append(owner.focusCamera(1, 0.25f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 245, 2, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 245, 3, 11, 8, 0.5f)),
                                                owner.emitParticles(-1, 245, 1, 11, 8, 0.5f),
                                                0.96f)
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.288f, -0.30004883f, 0.100097656f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.tintEnvironment(2, 0, 0.8125f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
