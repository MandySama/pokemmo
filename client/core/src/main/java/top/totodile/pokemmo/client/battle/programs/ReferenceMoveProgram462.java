package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 捏碎（招式 ID：462）的攻击／事件阶段动画。
 *
 * <p>招式描述：用骇人的力量捏碎对手. 对手的HP越多, 威力越大.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110462（名称）和 120462（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/CrushGripAnimation.java
 *
 * <p>源文件 SHA-256：5bd0a8d22c8859ff8870eba1b750e0016d08ab0157008a4704e040a47d8aafec
 */
final class ReferenceMoveProgram462 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline
                                                                        .waitThenParallel(0.6f)
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                637))
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                638))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1407,
                                                                                        1,
                                                                                        16,
                                                                                        0.0f,
                                                                                        0.9375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1376,
                                                                                        1,
                                                                                        16,
                                                                                        1000.0f,
                                                                                        0.0f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                0,
                                                                                                0.0f,
                                                                                                480.0f))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                1,
                                                                                                640.0f,
                                                                                                320.0f))
                                                                        .append(
                                                                                owner
                                                                                        .launchParticles(
                                                                                                637,
                                                                                                0,
                                                                                                9,
                                                                                                11,
                                                                                                0.5f,
                                                                                                360.0f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 637, 2,
                                                                                        11, 8,
                                                                                        0.5f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 638, 0,
                                                                                        11, 8,
                                                                                        0.5f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 638, 1,
                                                                                        11, 8,
                                                                                        0.5f)),
                                                                owner.emitParticles(
                                                                        -1, 638, 2, 11, 8, 0.5f),
                                                                0.3f)
                                                        .append(owner.focusCamera(1, 0.5f))
                                                        .endGroup()
                                                        .endGroup()
                                                        .beginSequence()
                                                        .waitSeconds(1.0f)
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1424,
                                                                        2,
                                                                        16,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1475,
                                                                        1,
                                                                        16,
                                                                        166.66667f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        16, 1))
                                                        .append(
                                                                owner.scaleCombatant(
                                                                        16,
                                                                        2,
                                                                        4,
                                                                        0.0f,
                                                                        0.048f,
                                                                        0.30004883f,
                                                                        -0.30004883f))
                                                        .append(
                                                                owner.launchParticles(
                                                                        637, 1, 9, 11, 0.5f,
                                                                        360.0f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 637, 3, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 638, 0, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 638, 1, 11, 8, 0.5f)),
                                                owner.emitParticles(-1, 638, 2, 11, 8, 0.5f),
                                                1.3f)
                                        .append(owner.focusCamera(1, 0.5f))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(2.0f)
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1424,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1475,
                                                        1,
                                                        16,
                                                        166.66667f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 4, 0.0f, 0.048f, 0.30004883f, -0.30004883f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
