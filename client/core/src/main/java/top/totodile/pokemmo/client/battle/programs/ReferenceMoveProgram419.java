package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 雪崩（招式 ID：419）的攻击／事件阶段动画。
 *
 * <p>招式描述：如果受到对手的招式攻击, 就能给予该对手2倍威力的攻击.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110419（名称）和 120419（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/AvalancheAnimation.java
 *
 * <p>源文件 SHA-256：7c12de855573a28360af54420ee1369a545fb8fac27b0baebb9e6a554318215f
 */
final class ReferenceMoveProgram419 {

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
                                                                                                .sequence()
                                                                                                .beginParallel()
                                                                                                .onStart(
                                                                                                        owner
                                                                                                                .setHealthHudHidden(
                                                                                                                        18,
                                                                                                                        true))
                                                                                                .append(
                                                                                                        owner
                                                                                                                .tintEnvironment(
                                                                                                                        4,
                                                                                                                        Short
                                                                                                                                .MAX_VALUE,
                                                                                                                        0.0f,
                                                                                                                        0.8125f,
                                                                                                                        0.025f))
                                                                                                .append(
                                                                                                        owner
                                                                                                                .focusCamera(
                                                                                                                        1,
                                                                                                                        0.4f))
                                                                                                .beginSequence()
                                                                                                .waitSeconds(
                                                                                                        0.32f)
                                                                                                .beginParallel()
                                                                                                .append(
                                                                                                        owner
                                                                                                                .scheduleSound(
                                                                                                                        (byte)
                                                                                                                                2,
                                                                                                                        (short)
                                                                                                                                1417,
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
                                                                                                                                1416,
                                                                                                                        2,
                                                                                                                        16,
                                                                                                                        0.0f,
                                                                                                                        0.9375f,
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
                                                                                                                        2833.3333f,
                                                                                                                        0.0f,
                                                                                                                        owner.casterHandle))
                                                                                                .append(
                                                                                                        owner
                                                                                                                .scheduleSound(
                                                                                                                        (byte)
                                                                                                                                2,
                                                                                                                        (short)
                                                                                                                                1536,
                                                                                                                        1,
                                                                                                                        16,
                                                                                                                        1500.0f,
                                                                                                                        0.9921875f,
                                                                                                                        owner.casterHandle))
                                                                                                .append(
                                                                                                        owner
                                                                                                                .adjustSoundTrack(
                                                                                                                        1,
                                                                                                                        1,
                                                                                                                        2080.0f,
                                                                                                                        768.0f))
                                                                                                .append(
                                                                                                        owner
                                                                                                                .adjustSoundTrack(
                                                                                                                        2,
                                                                                                                        1,
                                                                                                                        1760.0f,
                                                                                                                        480.0f))
                                                                                                .onStart(
                                                                                                        owner
                                                                                                                .preloadParticleEffect(
                                                                                                                        594)),
                                                                                        owner
                                                                                                .emitParticles(
                                                                                                        -1,
                                                                                                        594,
                                                                                                        5,
                                                                                                        11,
                                                                                                        8,
                                                                                                        0.0f),
                                                                                        0.42f)
                                                                                .append(
                                                                                        owner
                                                                                                .emitParticles(
                                                                                                        -1,
                                                                                                        594,
                                                                                                        5,
                                                                                                        11,
                                                                                                        8,
                                                                                                        0.0f))
                                                                                .append(
                                                                                        owner
                                                                                                .emitParticles(
                                                                                                        -1,
                                                                                                        594,
                                                                                                        3,
                                                                                                        11,
                                                                                                        8,
                                                                                                        0.5f))
                                                                                .append(
                                                                                        owner
                                                                                                .emitParticles(
                                                                                                        -1,
                                                                                                        594,
                                                                                                        4,
                                                                                                        11,
                                                                                                        8,
                                                                                                        0.25f)),
                                                                        owner.emitParticles(
                                                                                -1, 594, 0, 11, 8,
                                                                                0.25f),
                                                                        0.52f)
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 594, 4, 11, 8,
                                                                                0.25f))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 594, 0, 11, 8,
                                                                                0.25f))
                                                                .append(owner.focusCamera(1, 0.8f))
                                                                .endGroup()
                                                                .endGroup()
                                                                .beginSequence()
                                                                .waitSeconds(0.72f)
                                                                .beginParallel()
                                                                .append(
                                                                        owner
                                                                                .setSpriteAnimationPaused(
                                                                                        16, 1)),
                                                        owner.scaleCombatant(
                                                                16,
                                                                2,
                                                                6,
                                                                0.016f,
                                                                0.032f,
                                                                0.30004883f,
                                                                -0.30004883f),
                                                        1.12f)
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 594, 2, 11, 8, 0.5f)),
                                        owner.emitParticles(-1, 594, 1, 11, 8, 0.5f))
                                .append(owner.restoreCamera(0.4f))
                                .onStart(owner.setHealthHudHidden(18, false))
                                .append(owner.setSpriteAnimationPaused(16, 0)),
                        owner.tintEnvironment(4, Short.MAX_VALUE, 0.8125f, 0.0f, 0.025f));
        return result;
    }
}
