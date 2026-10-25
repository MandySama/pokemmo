package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 溶解液（招式 ID：51）的攻击／事件阶段动画。
 *
 * <p>招式描述：将强酸泼向对手进行攻击. 有时会降低对手的特防.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110051（名称）和 120051（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/AcidAnimation.java
 *
 * <p>源文件 SHA-256：6f739a16c1018cafac8f40a7b872ed3ab0bf3c5807a957767a5f3fb4eb000f9d
 */
final class ReferenceMoveProgram51 {

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
                                                                                        .appendThenDelayedParallel(
                                                                                                ReferenceMoveTimeline
                                                                                                        .waitThenParallel(
                                                                                                                0.6f)
                                                                                                        .onStart(
                                                                                                                owner
                                                                                                                        .preloadParticleEffect(
                                                                                                                                212))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scheduleSound(
                                                                                                                                (byte)
                                                                                                                                        2,
                                                                                                                                (short)
                                                                                                                                        1453,
                                                                                                                                1,
                                                                                                                                16,
                                                                                                                                0.0f,
                                                                                                                                0.8984375f,
                                                                                                                                owner.casterHandle))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scheduleSound(
                                                                                                                                (byte)
                                                                                                                                        2,
                                                                                                                                (short)
                                                                                                                                        1454,
                                                                                                                                2,
                                                                                                                                16,
                                                                                                                                750.0f,
                                                                                                                                0.9375f,
                                                                                                                                owner.casterHandle))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scheduleSound(
                                                                                                                                (byte)
                                                                                                                                        2,
                                                                                                                                (short)
                                                                                                                                        1452,
                                                                                                                                1,
                                                                                                                                16,
                                                                                                                                916.6667f,
                                                                                                                                0.9375f,
                                                                                                                                owner.casterHandle)),
                                                                                                owner
                                                                                                        .launchParticles(
                                                                                                                212,
                                                                                                                1,
                                                                                                                9,
                                                                                                                11,
                                                                                                                0.5f,
                                                                                                                360.0f),
                                                                                                0.06f),
                                                                                owner
                                                                                        .launchParticles(
                                                                                                212,
                                                                                                1,
                                                                                                9,
                                                                                                11,
                                                                                                0.5f,
                                                                                                360.0f),
                                                                                0.12f),
                                                                owner.launchParticles(
                                                                        212, 1, 9, 11, 0.5f,
                                                                        360.0f),
                                                                0.18f),
                                                owner.launchParticles(212, 1, 9, 11, 0.5f, 360.0f),
                                                0.34f)
                                        .append(owner.focusCamera(1, 0.4f))
                                        .append(owner.emitParticles(-1, 212, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 212, 2, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 212, 3, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.096f, 0.30004883f, -0.30004883f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
