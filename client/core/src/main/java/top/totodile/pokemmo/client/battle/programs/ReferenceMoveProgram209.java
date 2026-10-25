package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 电光（招式 ID：209）的攻击／事件阶段动画。
 *
 * <p>招式描述：让电流覆盖全身, 猛撞向对手进行攻击. 有时会让对手陷入麻痹状态.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110209（名称）和 120209（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/SparkAnimation.java
 *
 * <p>源文件 SHA-256：f3702d7ec720ee2cbfafc5611db39bc5d7664f7a40c00458cb0c146195decd36
 */
final class ReferenceMoveProgram209 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1458,
                                        1,
                                        14,
                                        333.33334f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1483, 2, 14, 0.0f, 0.78125f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1376, 2, 14, 1500.0f, 0.0f, owner.casterHandle));
        Color color = EncounterMoveAnimation.color(13014);
        ReferenceMoveTimeline animationStep6 =
                animationStep4
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1421,
                                        1,
                                        14,
                                        2333.3333f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 0, 0.0f, 1440.0f))
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .append(owner.tintEnvironment(2, 13311, 0.0f, 0.5f, 0.05f))
                        .append(owner.tintCombatant(14, 0.5f, 0.0f, 0.875f, color))
                        .onStart(owner.preloadParticleEffect(375))
                        .append(owner.emitParticles(-1, 375, 1, 9, 8, 0.5f));
        Color color2 = EncounterMoveAnimation.color(13014);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.appendThenNextParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenNextParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .appendThenNextParallel(
                                                                                                ReferenceMoveTimeline
                                                                                                        .appendThenNextParallel(
                                                                                                                ReferenceMoveTimeline
                                                                                                                        .appendThenOuterParallel(
                                                                                                                                ReferenceMoveTimeline
                                                                                                                                        .appendThenDelayedParallel(
                                                                                                                                                animationStep6,
                                                                                                                                                owner
                                                                                                                                                        .emitParticles(
                                                                                                                                                                -1,
                                                                                                                                                                375,
                                                                                                                                                                2,
                                                                                                                                                                9,
                                                                                                                                                                8,
                                                                                                                                                                0.5f),
                                                                                                                                                1.2f)
                                                                                                                                        .append(
                                                                                                                                                owner
                                                                                                                                                        .tintEnvironment(
                                                                                                                                                                2,
                                                                                                                                                                13311,
                                                                                                                                                                0.5f,
                                                                                                                                                                0.0f,
                                                                                                                                                                0.05f)),
                                                                                                                                owner
                                                                                                                                        .tintCombatant(
                                                                                                                                                14,
                                                                                                                                                0.5f,
                                                                                                                                                0.875f,
                                                                                                                                                0.0f,
                                                                                                                                                color2)),
                                                                                                                owner
                                                                                                                        .tintEnvironment(
                                                                                                                                2,
                                                                                                                                13311,
                                                                                                                                0.0f,
                                                                                                                                0.5f,
                                                                                                                                0.025f)),
                                                                                                owner
                                                                                                        .tintEnvironment(
                                                                                                                2,
                                                                                                                13311,
                                                                                                                0.5f,
                                                                                                                0.0f,
                                                                                                                0.025f)),
                                                                                owner
                                                                                        .tintEnvironment(
                                                                                                2,
                                                                                                13311,
                                                                                                0.0f,
                                                                                                0.5f,
                                                                                                0.025f))
                                                                        .append(
                                                                                owner
                                                                                        .tintEnvironment(
                                                                                                2,
                                                                                                13311,
                                                                                                0.5f,
                                                                                                0.0f,
                                                                                                0.025f)),
                                                                owner.shakeCombatant(
                                                                        14, 2, 1, 0.016f, 0.032f,
                                                                        1.0f, 0.0f))
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1505,
                                                                        1,
                                                                        16,
                                                                        333.33334f,
                                                                        0.9375f,
                                                                        owner.casterHandle)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1458,
                                                        2,
                                                        16,
                                                        333.33334f,
                                                        0.9375f,
                                                        owner.casterHandle),
                                                0.24f)
                                        .append(owner.emitParticles(-1, 375, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 375, 3, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 375, 4, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 375, 5, 11, 8, 0.5f)),
                                owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
