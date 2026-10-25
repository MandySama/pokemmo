package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 高科技光炮（招式 ID：546）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手放出光弹. 属性会根据自己携带的卡带不同而改变.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110546（名称）和 120546（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/TechnoBlastAnimation.java
 *
 * <p>源文件 SHA-256：0263cfdaa9fab1be48459defead816f3bab5863a5208828bb077b13515c8960f
 */
final class ReferenceMoveProgram546 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result = null;
        switch (owner.heldItem()) {
            default:
                {
                    result =
                            ReferenceMoveTimeline.appendThenOuterParallel(
                                            ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                            ReferenceMoveTimeline
                                                                    .appendThenDelayedOuterParallel(
                                                                            ReferenceMoveTimeline
                                                                                    .sequence()
                                                                                    .beginParallel()
                                                                                    .append(
                                                                                            owner
                                                                                                    .focusCamera(
                                                                                                            0,
                                                                                                            0.4f))
                                                                                    .beginSequence()
                                                                                    .waitSeconds(
                                                                                            0.4f)
                                                                                    .beginParallel()
                                                                                    .append(
                                                                                            owner
                                                                                                    .setSpriteAnimationPaused(
                                                                                                            16,
                                                                                                            1)),
                                                                            owner.tintEnvironment(
                                                                                    2, 0, 0.0f,
                                                                                    0.625f, 0.075f),
                                                                            1.0f)
                                                                    .append(
                                                                            owner.scheduleSound(
                                                                                    (byte) 2,
                                                                                    (short) 1846,
                                                                                    1,
                                                                                    14,
                                                                                    0.0f,
                                                                                    0.9921875f,
                                                                                    owner.casterHandle))
                                                                    .append(
                                                                            owner.scheduleSound(
                                                                                    (byte) 2,
                                                                                    (short) 1419,
                                                                                    2,
                                                                                    14,
                                                                                    583.3333f,
                                                                                    0.9921875f,
                                                                                    owner.casterHandle))
                                                                    .append(
                                                                            owner.scheduleSound(
                                                                                    (byte) 2,
                                                                                    (short) 1737,
                                                                                    1,
                                                                                    14,
                                                                                    916.6667f,
                                                                                    0.9921875f,
                                                                                    owner.casterHandle))
                                                                    .append(
                                                                            owner.scheduleSound(
                                                                                    (byte) 2,
                                                                                    (short) 1589,
                                                                                    2,
                                                                                    14,
                                                                                    1083.3334f,
                                                                                    0.9921875f,
                                                                                    owner.casterHandle))
                                                                    .onStart(
                                                                            owner
                                                                                    .preloadParticleEffect(
                                                                                            709))
                                                                    .append(
                                                                            owner.emitParticles(
                                                                                    -1, 709, 6, 9,
                                                                                    8, 0.5f))
                                                                    .append(
                                                                            owner.emitParticles(
                                                                                    -1, 709, 7, 9,
                                                                                    8, 0.5f))
                                                                    .append(
                                                                            owner.emitParticles(
                                                                                    -1, 709, 1, 9,
                                                                                    8, 0.5f)),
                                                            owner.emitParticles(
                                                                    -1, 709, 0, 9, 8, 0.5f),
                                                            2.4f)
                                                    .append(
                                                            owner.launchParticles(
                                                                    709, 2, 9, 11, 0.5f, 240.0f))
                                                    .append(owner.focusCamera(1, 0.3f))
                                                    .endGroup()
                                                    .endGroup()
                                                    .beginSequence()
                                                    .waitSeconds(2.8f)
                                                    .beginParallel()
                                                    .append(
                                                            owner.scheduleSound(
                                                                    (byte) 2,
                                                                    (short) 1593,
                                                                    1,
                                                                    16,
                                                                    0.0f,
                                                                    0.3125f,
                                                                    owner.casterHandle))
                                                    .append(
                                                            owner.scheduleSound(
                                                                    (byte) 2,
                                                                    (short) 1521,
                                                                    2,
                                                                    16,
                                                                    0.0f,
                                                                    0.9921875f,
                                                                    owner.casterHandle))
                                                    .append(
                                                            owner.emitParticles(
                                                                    -1, 709, 5, 11, 8, 0.5f))
                                                    .append(
                                                            owner.emitParticles(
                                                                    -1, 709, 3, 11, 8, 0.5f))
                                                    .append(
                                                            owner.emitParticles(
                                                                    -1, 709, 4, 11, 8, 0.5f)),
                                            owner.scaleCombatant(
                                                    16,
                                                    2,
                                                    1,
                                                    0.0f,
                                                    0.096f,
                                                    -0.30004883f,
                                                    0.30004883f))
                                    .append(owner.tintEnvironment(2, 0, 0.625f, 0.0f, 0.075f))
                                    .append(owner.restoreCamera(0.4f))
                                    .append(owner.setSpriteAnimationPaused(16, 0))
                                    .endGroup()
                                    .beginParallel()
                                    .endGroup();
                    return result;
                }
            case 5119:
                {
                    result =
                            ReferenceMoveTimeline.appendThenOuterParallel(
                                            ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                            ReferenceMoveTimeline
                                                                    .appendThenDelayedOuterParallel(
                                                                            ReferenceMoveTimeline
                                                                                    .sequence()
                                                                                    .beginParallel()
                                                                                    .append(
                                                                                            owner
                                                                                                    .focusCamera(
                                                                                                            0,
                                                                                                            0.4f))
                                                                                    .beginSequence()
                                                                                    .waitSeconds(
                                                                                            0.4f)
                                                                                    .beginParallel()
                                                                                    .append(
                                                                                            owner
                                                                                                    .setSpriteAnimationPaused(
                                                                                                            16,
                                                                                                            1)),
                                                                            owner.tintEnvironment(
                                                                                    2, 0, 0.0f,
                                                                                    0.625f, 0.075f),
                                                                            1.0f)
                                                                    .append(
                                                                            owner.scheduleSound(
                                                                                    (byte) 2,
                                                                                    (short) 1846,
                                                                                    1,
                                                                                    14,
                                                                                    0.0f,
                                                                                    0.9921875f,
                                                                                    owner.casterHandle))
                                                                    .append(
                                                                            owner.scheduleSound(
                                                                                    (byte) 2,
                                                                                    (short) 1419,
                                                                                    2,
                                                                                    14,
                                                                                    583.3333f,
                                                                                    0.9921875f,
                                                                                    owner.casterHandle))
                                                                    .append(
                                                                            owner.scheduleSound(
                                                                                    (byte) 2,
                                                                                    (short) 1737,
                                                                                    1,
                                                                                    14,
                                                                                    916.6667f,
                                                                                    0.9921875f,
                                                                                    owner.casterHandle))
                                                                    .append(
                                                                            owner.scheduleSound(
                                                                                    (byte) 2,
                                                                                    (short) 1589,
                                                                                    2,
                                                                                    14,
                                                                                    1083.3334f,
                                                                                    0.9921875f,
                                                                                    owner.casterHandle))
                                                                    .onStart(
                                                                            owner
                                                                                    .preloadParticleEffect(
                                                                                            713))
                                                                    .append(
                                                                            owner.emitParticles(
                                                                                    -1, 713, 6, 9,
                                                                                    8, 0.5f))
                                                                    .append(
                                                                            owner.emitParticles(
                                                                                    -1, 713, 7, 9,
                                                                                    8, 0.5f))
                                                                    .append(
                                                                            owner.emitParticles(
                                                                                    -1, 713, 1, 9,
                                                                                    8, 0.5f)),
                                                            owner.emitParticles(
                                                                    -1, 713, 0, 9, 8, 0.5f),
                                                            2.4f)
                                                    .append(
                                                            owner.launchParticles(
                                                                    713, 2, 9, 11, 0.5f, 240.0f))
                                                    .append(owner.focusCamera(1, 0.3f))
                                                    .endGroup()
                                                    .endGroup()
                                                    .beginSequence()
                                                    .waitSeconds(2.8f)
                                                    .beginParallel()
                                                    .append(
                                                            owner.scheduleSound(
                                                                    (byte) 2,
                                                                    (short) 1593,
                                                                    1,
                                                                    16,
                                                                    0.0f,
                                                                    0.3125f,
                                                                    owner.casterHandle))
                                                    .append(
                                                            owner.scheduleSound(
                                                                    (byte) 2,
                                                                    (short) 1521,
                                                                    2,
                                                                    16,
                                                                    0.0f,
                                                                    0.9921875f,
                                                                    owner.casterHandle))
                                                    .append(
                                                            owner.emitParticles(
                                                                    -1, 713, 5, 11, 8, 0.5f))
                                                    .append(
                                                            owner.emitParticles(
                                                                    -1, 713, 3, 11, 8, 0.5f))
                                                    .append(
                                                            owner.emitParticles(
                                                                    -1, 713, 4, 11, 8, 0.5f)),
                                            owner.scaleCombatant(
                                                    16,
                                                    2,
                                                    1,
                                                    0.0f,
                                                    0.096f,
                                                    -0.30004883f,
                                                    0.30004883f))
                                    .append(owner.tintEnvironment(2, 0, 0.625f, 0.0f, 0.075f))
                                    .append(owner.restoreCamera(0.4f))
                                    .append(owner.setSpriteAnimationPaused(16, 0))
                                    .endGroup()
                                    .beginParallel()
                                    .endGroup();
                    return result;
                }
            case 5118:
                {
                    result =
                            ReferenceMoveTimeline.appendThenOuterParallel(
                                            ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                            ReferenceMoveTimeline
                                                                    .appendThenDelayedOuterParallel(
                                                                            ReferenceMoveTimeline
                                                                                    .sequence()
                                                                                    .beginParallel()
                                                                                    .append(
                                                                                            owner
                                                                                                    .focusCamera(
                                                                                                            0,
                                                                                                            0.4f))
                                                                                    .beginSequence()
                                                                                    .waitSeconds(
                                                                                            0.4f)
                                                                                    .beginParallel()
                                                                                    .append(
                                                                                            owner
                                                                                                    .setSpriteAnimationPaused(
                                                                                                            16,
                                                                                                            1)),
                                                                            owner.tintEnvironment(
                                                                                    2, 0, 0.0f,
                                                                                    0.625f, 0.075f),
                                                                            1.0f)
                                                                    .append(
                                                                            owner.scheduleSound(
                                                                                    (byte) 2,
                                                                                    (short) 1846,
                                                                                    1,
                                                                                    14,
                                                                                    0.0f,
                                                                                    0.9921875f,
                                                                                    owner.casterHandle))
                                                                    .append(
                                                                            owner.scheduleSound(
                                                                                    (byte) 2,
                                                                                    (short) 1419,
                                                                                    2,
                                                                                    14,
                                                                                    583.3333f,
                                                                                    0.9921875f,
                                                                                    owner.casterHandle))
                                                                    .append(
                                                                            owner.scheduleSound(
                                                                                    (byte) 2,
                                                                                    (short) 1737,
                                                                                    1,
                                                                                    14,
                                                                                    916.6667f,
                                                                                    0.9921875f,
                                                                                    owner.casterHandle))
                                                                    .append(
                                                                            owner.scheduleSound(
                                                                                    (byte) 2,
                                                                                    (short) 1589,
                                                                                    2,
                                                                                    14,
                                                                                    1083.3334f,
                                                                                    0.9921875f,
                                                                                    owner.casterHandle))
                                                                    .onStart(
                                                                            owner
                                                                                    .preloadParticleEffect(
                                                                                            712))
                                                                    .append(
                                                                            owner.emitParticles(
                                                                                    -1, 712, 6, 9,
                                                                                    8, 0.5f))
                                                                    .append(
                                                                            owner.emitParticles(
                                                                                    -1, 712, 7, 9,
                                                                                    8, 0.5f))
                                                                    .append(
                                                                            owner.emitParticles(
                                                                                    -1, 712, 1, 9,
                                                                                    8, 0.5f)),
                                                            owner.emitParticles(
                                                                    -1, 712, 0, 9, 8, 0.5f),
                                                            2.4f)
                                                    .append(
                                                            owner.launchParticles(
                                                                    712, 2, 9, 11, 0.5f, 240.0f))
                                                    .append(owner.focusCamera(1, 0.3f))
                                                    .endGroup()
                                                    .endGroup()
                                                    .beginSequence()
                                                    .waitSeconds(2.8f)
                                                    .beginParallel()
                                                    .append(
                                                            owner.scheduleSound(
                                                                    (byte) 2,
                                                                    (short) 1593,
                                                                    1,
                                                                    16,
                                                                    0.0f,
                                                                    0.3125f,
                                                                    owner.casterHandle))
                                                    .append(
                                                            owner.scheduleSound(
                                                                    (byte) 2,
                                                                    (short) 1521,
                                                                    2,
                                                                    16,
                                                                    0.0f,
                                                                    0.9921875f,
                                                                    owner.casterHandle))
                                                    .append(
                                                            owner.emitParticles(
                                                                    -1, 712, 5, 11, 8, 0.5f))
                                                    .append(
                                                            owner.emitParticles(
                                                                    -1, 712, 3, 11, 8, 0.5f))
                                                    .append(
                                                            owner.emitParticles(
                                                                    -1, 712, 4, 11, 8, 0.5f)),
                                            owner.scaleCombatant(
                                                    16,
                                                    2,
                                                    1,
                                                    0.0f,
                                                    0.096f,
                                                    -0.30004883f,
                                                    0.30004883f))
                                    .append(owner.tintEnvironment(2, 0, 0.625f, 0.0f, 0.075f))
                                    .append(owner.restoreCamera(0.4f))
                                    .append(owner.setSpriteAnimationPaused(16, 0))
                                    .endGroup()
                                    .beginParallel()
                                    .endGroup();
                    return result;
                }
            case 5117:
                {
                    result =
                            ReferenceMoveTimeline.appendThenOuterParallel(
                                            ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                            ReferenceMoveTimeline
                                                                    .appendThenDelayedOuterParallel(
                                                                            ReferenceMoveTimeline
                                                                                    .sequence()
                                                                                    .beginParallel()
                                                                                    .append(
                                                                                            owner
                                                                                                    .focusCamera(
                                                                                                            0,
                                                                                                            0.4f))
                                                                                    .beginSequence()
                                                                                    .waitSeconds(
                                                                                            0.4f)
                                                                                    .beginParallel()
                                                                                    .append(
                                                                                            owner
                                                                                                    .setSpriteAnimationPaused(
                                                                                                            16,
                                                                                                            1)),
                                                                            owner.tintEnvironment(
                                                                                    2, 0, 0.0f,
                                                                                    0.625f, 0.075f),
                                                                            1.0f)
                                                                    .append(
                                                                            owner.scheduleSound(
                                                                                    (byte) 2,
                                                                                    (short) 1846,
                                                                                    1,
                                                                                    14,
                                                                                    0.0f,
                                                                                    0.9921875f,
                                                                                    owner.casterHandle))
                                                                    .append(
                                                                            owner.scheduleSound(
                                                                                    (byte) 2,
                                                                                    (short) 1419,
                                                                                    2,
                                                                                    14,
                                                                                    583.3333f,
                                                                                    0.9921875f,
                                                                                    owner.casterHandle))
                                                                    .append(
                                                                            owner.scheduleSound(
                                                                                    (byte) 2,
                                                                                    (short) 1737,
                                                                                    1,
                                                                                    14,
                                                                                    916.6667f,
                                                                                    0.9921875f,
                                                                                    owner.casterHandle))
                                                                    .append(
                                                                            owner.scheduleSound(
                                                                                    (byte) 2,
                                                                                    (short) 1589,
                                                                                    2,
                                                                                    14,
                                                                                    1083.3334f,
                                                                                    0.9921875f,
                                                                                    owner.casterHandle))
                                                                    .onStart(
                                                                            owner
                                                                                    .preloadParticleEffect(
                                                                                            711))
                                                                    .append(
                                                                            owner.emitParticles(
                                                                                    -1, 711, 6, 9,
                                                                                    8, 0.5f))
                                                                    .append(
                                                                            owner.emitParticles(
                                                                                    -1, 711, 7, 9,
                                                                                    8, 0.5f))
                                                                    .append(
                                                                            owner.emitParticles(
                                                                                    -1, 711, 1, 9,
                                                                                    8, 0.5f)),
                                                            owner.emitParticles(
                                                                    -1, 711, 0, 9, 8, 0.5f),
                                                            2.4f)
                                                    .append(
                                                            owner.launchParticles(
                                                                    711, 2, 9, 11, 0.5f, 240.0f))
                                                    .append(owner.focusCamera(1, 0.3f))
                                                    .endGroup()
                                                    .endGroup()
                                                    .beginSequence()
                                                    .waitSeconds(2.8f)
                                                    .beginParallel()
                                                    .append(
                                                            owner.scheduleSound(
                                                                    (byte) 2,
                                                                    (short) 1593,
                                                                    1,
                                                                    16,
                                                                    0.0f,
                                                                    0.3125f,
                                                                    owner.casterHandle))
                                                    .append(
                                                            owner.scheduleSound(
                                                                    (byte) 2,
                                                                    (short) 1521,
                                                                    2,
                                                                    16,
                                                                    0.0f,
                                                                    0.9921875f,
                                                                    owner.casterHandle))
                                                    .append(
                                                            owner.emitParticles(
                                                                    -1, 711, 5, 11, 8, 0.5f))
                                                    .append(
                                                            owner.emitParticles(
                                                                    -1, 711, 3, 11, 8, 0.5f))
                                                    .append(
                                                            owner.emitParticles(
                                                                    -1, 711, 4, 11, 8, 0.5f)),
                                            owner.scaleCombatant(
                                                    16,
                                                    2,
                                                    1,
                                                    0.0f,
                                                    0.096f,
                                                    -0.30004883f,
                                                    0.30004883f))
                                    .append(owner.tintEnvironment(2, 0, 0.625f, 0.0f, 0.075f))
                                    .append(owner.restoreCamera(0.4f))
                                    .append(owner.setSpriteAnimationPaused(16, 0))
                                    .endGroup()
                                    .beginParallel()
                                    .endGroup();
                    return result;
                }
            case 5116:
        }
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        0, 0.4f))
                                                                        .beginSequence()
                                                                        .waitSeconds(0.4f)
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner
                                                                                        .setSpriteAnimationPaused(
                                                                                                16,
                                                                                                1)),
                                                                owner.tintEnvironment(
                                                                        2, 0, 0.0f, 0.625f, 0.075f),
                                                                1.0f)
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1846,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1419,
                                                                        2,
                                                                        14,
                                                                        583.3333f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1737,
                                                                        1,
                                                                        14,
                                                                        916.6667f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1589,
                                                                        2,
                                                                        14,
                                                                        1083.3334f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(710))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 710, 6, 9, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 710, 7, 9, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 710, 1, 9, 8, 0.5f)),
                                                owner.emitParticles(-1, 710, 0, 9, 8, 0.5f),
                                                2.4f)
                                        .append(owner.launchParticles(710, 2, 9, 11, 0.5f, 240.0f))
                                        .append(owner.focusCamera(1, 0.3f))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(2.8f)
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1593,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.3125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1521,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.emitParticles(-1, 710, 5, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 710, 4, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 710, 4, 11, 8, 0.5f)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.0f, 0.096f, -0.30004883f, 0.30004883f))
                        .append(owner.tintEnvironment(2, 0, 0.625f, 0.0f, 0.075f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
