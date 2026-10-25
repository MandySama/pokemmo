package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 火之舞（招式 ID：552）的攻击／事件阶段动画。
 *
 * <p>招式描述：让火焰覆盖全身, 振翅攻击对手. 有时会提高自己的特攻.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110552（名称）和 120552（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/FieryDanceAnimation.java
 *
 * <p>源文件 SHA-256：61c8bb71e039560167ecbcf9c2d7ada788445248892c78b9e8caba8d6190ed42
 */
final class ReferenceMoveProgram552 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep2 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1818,
                                        0,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(0, 0, 0.0f, 800.0f));
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep12 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                        ReferenceMoveTimeline
                                                                .appendThenDelayedParallel(
                                                                        animationStep2
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        1376,
                                                                                                        0,
                                                                                                        16,
                                                                                                        833.3333f,
                                                                                                        0.0f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .focusCamera(
                                                                                                        1,
                                                                                                        0.4f))
                                                                                .onStart(
                                                                                        owner
                                                                                                .setHealthHudHidden(
                                                                                                        18,
                                                                                                        true))
                                                                                .endGroup()
                                                                                .beginParallel()
                                                                                .append(
                                                                                        owner
                                                                                                .tintEnvironment(
                                                                                                        2,
                                                                                                        31,
                                                                                                        0.0f,
                                                                                                        0.5f,
                                                                                                        0.075f))
                                                                                .append(
                                                                                        owner
                                                                                                .tintCombatant(
                                                                                                        16,
                                                                                                        1.25f,
                                                                                                        0.0f,
                                                                                                        0.5f,
                                                                                                        color))
                                                                                .append(
                                                                                        owner
                                                                                                .setSpriteAnimationPaused(
                                                                                                        16,
                                                                                                        1))
                                                                                .onStart(
                                                                                        owner
                                                                                                .preloadParticleEffect(
                                                                                                        719))
                                                                                .append(
                                                                                        owner
                                                                                                .emitParticles(
                                                                                                        -1,
                                                                                                        719,
                                                                                                        0,
                                                                                                        11,
                                                                                                        8,
                                                                                                        0.25f)),
                                                                        owner.emitParticles(
                                                                                -1, 719, 2, 11, 8,
                                                                                0.5f),
                                                                        0.2f)
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1561,
                                                                                1,
                                                                                16,
                                                                                0.0f,
                                                                                0.9921875f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1907,
                                                                                2,
                                                                                16,
                                                                                0.0f,
                                                                                0.9921875f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1907,
                                                                                1,
                                                                                16,
                                                                                1333.3334f,
                                                                                0.9921875f,
                                                                                owner.casterHandle)),
                                                        owner.emitParticles(
                                                                -1, 719, 1, 11, 8, 0.0f),
                                                        1.4f)
                                                .append(
                                                        owner.scaleCombatant(
                                                                16,
                                                                2,
                                                                23,
                                                                0.0f,
                                                                0.064f,
                                                                0.100097656f,
                                                                -0.100097656f)),
                                        owner.emitParticles(-1, 719, 2, 11, 8, 0.5f),
                                        1.6f),
                                owner.emitParticles(-1, 719, 0, 11, 8, 0.0f),
                                2.6f)
                        .append(owner.emitParticles(-1, 719, 3, 11, 8, 0.5f));
        Color color2 = EncounterMoveAnimation.color(31);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenOuterParallel(
                                                animationStep12,
                                                owner.emitParticles(-1, 719, 1, 11, 8, 0.25f))
                                        .append(owner.tintEnvironment(2, 31, 0.5f, 0.0f, 0.075f))
                                        .append(owner.tintCombatant(16, 0.75f, 0.5f, 0.0f, color2)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.0f, 0.032f, 0.100097656f, -0.100097656f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
