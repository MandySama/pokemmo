package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 水枪（招式 ID：55）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手猛烈地喷射水流进行攻击.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110055（名称）和 120055（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/WaterGunAnimation.java
 *
 * <p>源文件 SHA-256：15797e586410bd9688ff4fdda8bc0eb4832d6c127d58893bc2c1bc130a79097d
 */
final class ReferenceMoveProgram55 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
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
                                                                                                                                                                        .appendThenDelayedOuterParallel(
                                                                                                                                                                                ReferenceMoveTimeline
                                                                                                                                                                                        .appendThenDelayedOuterParallel(
                                                                                                                                                                                                ReferenceMoveTimeline
                                                                                                                                                                                                        .appendThenDelayedParallel(
                                                                                                                                                                                                                ReferenceMoveTimeline
                                                                                                                                                                                                                        .sequence()
                                                                                                                                                                                                                        .beginParallel()
                                                                                                                                                                                                                        .onStart(
                                                                                                                                                                                                                                owner
                                                                                                                                                                                                                                        .setHealthHudHidden(
                                                                                                                                                                                                                                                18,
                                                                                                                                                                                                                                                true))
                                                                                                                                                                                                                        .waitSeconds(
                                                                                                                                                                                                                                0.6f)
                                                                                                                                                                                                                        .endGroup()
                                                                                                                                                                                                                        .beginParallel()
                                                                                                                                                                                                                        .onStart(
                                                                                                                                                                                                                                owner
                                                                                                                                                                                                                                        .preloadParticleEffect(
                                                                                                                                                                                                                                                216)),
                                                                                                                                                                                                                owner
                                                                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                                                                216,
                                                                                                                                                                                                                                1,
                                                                                                                                                                                                                                9,
                                                                                                                                                                                                                                11,
                                                                                                                                                                                                                                0.5f,
                                                                                                                                                                                                                                360.0f),
                                                                                                                                                                                                                0.02f),
                                                                                                                                                                                                owner
                                                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                                                216,
                                                                                                                                                                                                                1,
                                                                                                                                                                                                                9,
                                                                                                                                                                                                                11,
                                                                                                                                                                                                                0.5f,
                                                                                                                                                                                                                360.0f),
                                                                                                                                                                                                0.04f)
                                                                                                                                                                                        .append(
                                                                                                                                                                                                owner
                                                                                                                                                                                                        .scheduleSound(
                                                                                                                                                                                                                (byte)
                                                                                                                                                                                                                        2,
                                                                                                                                                                                                                1407,
                                                                                                                                                                                                                1,
                                                                                                                                                                                                                16,
                                                                                                                                                                                                                0.0f,
                                                                                                                                                                                                                0.78125f,
                                                                                                                                                                                                                owner.casterHandle))
                                                                                                                                                                                        .append(
                                                                                                                                                                                                owner
                                                                                                                                                                                                        .scheduleSound(
                                                                                                                                                                                                                (byte)
                                                                                                                                                                                                                        2,
                                                                                                                                                                                                                1408,
                                                                                                                                                                                                                2,
                                                                                                                                                                                                                14,
                                                                                                                                                                                                                0.0f,
                                                                                                                                                                                                                0.78125f,
                                                                                                                                                                                                                owner.casterHandle)),
                                                                                                                                                                                owner
                                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                                216,
                                                                                                                                                                                                1,
                                                                                                                                                                                                9,
                                                                                                                                                                                                11,
                                                                                                                                                                                                0.5f,
                                                                                                                                                                                                360.0f),
                                                                                                                                                                                0.06f),
                                                                                                                                                                owner
                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                216,
                                                                                                                                                                                1,
                                                                                                                                                                                9,
                                                                                                                                                                                11,
                                                                                                                                                                                0.5f,
                                                                                                                                                                                360.0f),
                                                                                                                                                                0.08f),
                                                                                                                                                owner
                                                                                                                                                        .launchParticles(
                                                                                                                                                                216,
                                                                                                                                                                1,
                                                                                                                                                                9,
                                                                                                                                                                11,
                                                                                                                                                                0.5f,
                                                                                                                                                                360.0f),
                                                                                                                                                0.1f),
                                                                                                                                owner
                                                                                                                                        .launchParticles(
                                                                                                                                                216,
                                                                                                                                                1,
                                                                                                                                                9,
                                                                                                                                                11,
                                                                                                                                                0.5f,
                                                                                                                                                360.0f),
                                                                                                                                0.12f),
                                                                                                                owner
                                                                                                                        .launchParticles(
                                                                                                                                216,
                                                                                                                                1,
                                                                                                                                9,
                                                                                                                                11,
                                                                                                                                0.5f,
                                                                                                                                360.0f),
                                                                                                                0.14f)
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .setSpriteAnimationPaused(
                                                                                                                                16,
                                                                                                                                1))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .launchParticles(
                                                                                                                                216,
                                                                                                                                1,
                                                                                                                                9,
                                                                                                                                11,
                                                                                                                                0.5f,
                                                                                                                                360.0f))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scaleCombatant(
                                                                                                                                16,
                                                                                                                                2,
                                                                                                                                6,
                                                                                                                                0.016f,
                                                                                                                                0.032f,
                                                                                                                                0.30004883f,
                                                                                                                                -0.100097656f))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .launchParticles(
                                                                                                                                216,
                                                                                                                                1,
                                                                                                                                9,
                                                                                                                                11,
                                                                                                                                0.5f,
                                                                                                                                360.0f))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .focusCamera(
                                                                                                                                1,
                                                                                                                                0.4f)),
                                                                                                owner
                                                                                                        .launchParticles(
                                                                                                                216,
                                                                                                                1,
                                                                                                                9,
                                                                                                                11,
                                                                                                                0.5f,
                                                                                                                360.0f),
                                                                                                0.16f)
                                                                                        .append(
                                                                                                owner
                                                                                                        .emitParticles(
                                                                                                                -1,
                                                                                                                216,
                                                                                                                0,
                                                                                                                11,
                                                                                                                8,
                                                                                                                0.5f)),
                                                                                owner
                                                                                        .launchParticles(
                                                                                                216,
                                                                                                1,
                                                                                                9,
                                                                                                11,
                                                                                                0.5f,
                                                                                                360.0f),
                                                                                0.18f)
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 216, 0,
                                                                                        11, 8,
                                                                                        0.5f)),
                                                                owner.launchParticles(
                                                                        216, 1, 9, 11, 0.5f,
                                                                        360.0f),
                                                                0.2f)
                                                        .append(
                                                                owner.launchParticles(
                                                                        216, 1, 9, 11, 0.5f,
                                                                        360.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1409,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1376,
                                                                        1,
                                                                        16,
                                                                        750.0f,
                                                                        0.0f,
                                                                        owner.casterHandle)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        2,
                                                        16,
                                                        750.0f,
                                                        0.0f,
                                                        owner.casterHandle),
                                                0.22f),
                                        owner.launchParticles(216, 1, 9, 11, 0.5f, 360.0f),
                                        0.24f),
                                owner.launchParticles(216, 1, 9, 11, 0.5f, 360.0f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
