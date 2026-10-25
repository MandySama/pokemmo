package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 无理取闹（招式 ID：259）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手无理取闹, 令其不能连续2次使出相同招式.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110259（名称）和 120259（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/TormentAnimation.java
 *
 * <p>源文件 SHA-256：2f7acbf7ebe6e441fba5c0f5ec3aa63551af376b65506d67c76d06f2ee9cb417
 */
final class ReferenceMoveProgram259 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep15 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        14, 1))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1826,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.390625f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1826,
                                                                        2,
                                                                        14,
                                                                        250.0f,
                                                                        0.390625f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1826,
                                                                        1,
                                                                        14,
                                                                        500.0f,
                                                                        0.390625f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1826,
                                                                        2,
                                                                        14,
                                                                        750.0f,
                                                                        0.390625f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(425))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 425, 5, 9, 8, 0.25f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 425, 6, 9, 8, 0.25f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 425, 7, 9, 8, 0.25f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 425, 8, 9, 8, 0.25f)),
                                                owner.scaleCombatant(
                                                        14,
                                                        2,
                                                        4,
                                                        0.016f,
                                                        0.048f,
                                                        0.0f,
                                                        0.30004883f),
                                                0.2f)
                                        .append(owner.emitParticles(-1, 425, 0, 9, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 425, 1, 9, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 425, 2, 9, 8, 0.25f)),
                                owner.emitParticles(-1, 425, 3, 9, 8, 0.25f))
                        .append(owner.focusCamera(1, 0.4f))
                        .beginSequence()
                        .waitSeconds(0.4f)
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1827, 1, 16, 0.0f, 0.9375f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1827,
                                        1,
                                        16,
                                        300.0f,
                                        0.9375f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(22);
        ReferenceMoveTimeline animationStep16 =
                animationStep15
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1827,
                                        1,
                                        16,
                                        500.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.tintCombatant(16, 0.5f, 0.0f, 0.75f, color));
        Color color2 = EncounterMoveAnimation.color(22);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                animationStep16,
                                                owner.emitParticles(-1, 425, 4, 11, 8, 0.5f),
                                                0.6f)
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(owner.tintCombatant(16, 0.5f, 0.75f, 0.0f, color2)),
                                owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.0f, 0.30004883f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
