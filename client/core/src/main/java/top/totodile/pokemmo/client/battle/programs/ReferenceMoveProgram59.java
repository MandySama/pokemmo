package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 暴风雪（招式 ID：59）的攻击／事件阶段动画。
 *
 * <p>招式描述：将猛烈的暴风雪刮向对手进行攻击. 有时会让对手陷入冰冻状态.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110059（名称）和 120059（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/BlizzardAnimation.java
 *
 * <p>源文件 SHA-256：0869c884c83d3c4be8c06bfdf417b9f6cd0f8decde62fcfd7f6145a6a617d432
 */
final class ReferenceMoveProgram59 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction3 = owner.sceneFlag(1, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(2, true);
        ReferenceMoveTimeline animationStep16 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
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
                                                                                                                        .append(
                                                                                                                                owner
                                                                                                                                        .focusCamera(
                                                                                                                                                1,
                                                                                                                                                0.4f)),
                                                                                                                owner
                                                                                                                        .tintEnvironment(
                                                                                                                                2,
                                                                                                                                0,
                                                                                                                                0.0f,
                                                                                                                                1.0f,
                                                                                                                                0.025f))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .tintEnvironment(
                                                                                                                                3,
                                                                                                                                0,
                                                                                                                                1.0f,
                                                                                                                                0.0f,
                                                                                                                                0.05f))
                                                                                                        .onStart(
                                                                                                                owner
                                                                                                                        .preloadParticleEffect(
                                                                                                                                220))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .setSpriteAnimationPaused(
                                                                                                                                16,
                                                                                                                                1))
                                                                                                        .onStart(
                                                                                                                owner
                                                                                                                        .loadBattleBackground(
                                                                                                                                8))
                                                                                                        .onStart(
                                                                                                                ReferenceMoveTimeline
                                                                                                                        .callback(
                                                                                                                                sceneFlagAction2))
                                                                                                        .onStart(
                                                                                                                ReferenceMoveTimeline
                                                                                                                        .callback(
                                                                                                                                sceneFlagAction3))
                                                                                                        .onStart(
                                                                                                                ReferenceMoveTimeline
                                                                                                                        .callback(
                                                                                                                                sceneFlagAction4))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scrollBattleBackground(
                                                                                                                                1,
                                                                                                                                2400,
                                                                                                                                0,
                                                                                                                                2.88f))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scheduleSound(
                                                                                                                                (byte)
                                                                                                                                        2,
                                                                                                                                (short)
                                                                                                                                        1497,
                                                                                                                                1,
                                                                                                                                16,
                                                                                                                                0.0f,
                                                                                                                                0.546875f,
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
                                                                                                                                833.3333f,
                                                                                                                                0.0f,
                                                                                                                                owner.casterHandle))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scheduleSound(
                                                                                                                                (byte)
                                                                                                                                        2,
                                                                                                                                (short)
                                                                                                                                        1509,
                                                                                                                                1,
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
                                                                                                                                        1717,
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
                                                                                                                                1333.3334f,
                                                                                                                                0.0f,
                                                                                                                                owner.casterHandle))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scheduleSound(
                                                                                                                                (byte)
                                                                                                                                        2,
                                                                                                                                (short)
                                                                                                                                        2036,
                                                                                                                                0,
                                                                                                                                16,
                                                                                                                                166.66667f,
                                                                                                                                0.9921875f,
                                                                                                                                owner.casterHandle))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .adjustSoundTrack(
                                                                                                                                2,
                                                                                                                                0,
                                                                                                                                0.0f,
                                                                                                                                320.0f))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .adjustSoundTrack(
                                                                                                                                2,
                                                                                                                                0,
                                                                                                                                640.0f,
                                                                                                                                480.0f))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .adjustSoundTrack(
                                                                                                                                2,
                                                                                                                                1,
                                                                                                                                0.0f,
                                                                                                                                480.0f))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .adjustSoundTrack(
                                                                                                                                2,
                                                                                                                                1,
                                                                                                                                640.0f,
                                                                                                                                640.0f))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .emitParticles(
                                                                                                                                -1,
                                                                                                                                220,
                                                                                                                                3,
                                                                                                                                11,
                                                                                                                                8,
                                                                                                                                0.5f)),
                                                                                                owner
                                                                                                        .emitParticles(
                                                                                                                -1,
                                                                                                                220,
                                                                                                                4,
                                                                                                                11,
                                                                                                                8,
                                                                                                                0.5f),
                                                                                                0.08f),
                                                                                owner.emitParticles(
                                                                                        -1, 220, 3,
                                                                                        11, 8,
                                                                                        0.5f),
                                                                                0.16f),
                                                                owner.emitParticles(
                                                                        -1, 220, 3, 11, 8, 0.5f),
                                                                0.24f)
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 220, 3, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 220, 4, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 220, 0, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 220, 1, 11, 8, 0.5f)),
                                                owner.emitParticles(-1, 220, 2, 11, 8, 0.5f),
                                                1.04f),
                                        owner.scaleCombatant(
                                                16,
                                                2,
                                                1,
                                                0.016f,
                                                0.032f,
                                                0.30004883f,
                                                -0.30004883f),
                                        2.64f),
                                owner.tintEnvironment(3, 0, 0.0f, 1.0f, 0.05f))
                        .append(owner.tintEnvironment(2, 0, 1.0f, 0.0f, 0.025f));
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        Runnable sceneFlagAction6 = owner.sceneFlag(1, true);
        result =
                animationStep16
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
