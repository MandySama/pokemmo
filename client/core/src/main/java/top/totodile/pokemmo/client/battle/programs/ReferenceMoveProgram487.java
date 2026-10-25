package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 浸水（招式 ID：487）的攻击／事件阶段动画。
 *
 * <p>招式描述：将大量的水泼向对手, 从而使其变成水属性.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110487（名称）和 120487（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/SoakAnimation.java
 *
 * <p>源文件 SHA-256：8daa01895c4d4fc04dacab8776552fb8921d4ad12b47397ed82277be8dd88334
 */
final class ReferenceMoveProgram487 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .onStart(owner.setHealthHudHidden(18, true))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1499,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 1, 800.0f, 160.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1718,
                                                        2,
                                                        16,
                                                        666.6667f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(657))
                                        .append(owner.emitParticles(-1, 657, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 657, 0, 11, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 657, 1, 11, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 657, 1, 11, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 657, 3, 11, 8, 0.25f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.emitParticles(-1, 657, 2, 11, 8, 0.25f),
                                0.2f),
                        owner.emitParticles(-1, 657, 3, 11, 8, 0.25f),
                        0.6f);
        Color color = EncounterMoveAnimation.color(32064);
        ReferenceMoveTimeline animationStep11 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep10
                                        .append(owner.emitParticles(-1, 657, 3, 11, 8, 0.25f))
                                        .append(owner.tintCombatant(16, 0.75f, 0.0f, 0.5f, color)),
                                owner.scaleCombatant(
                                        16, 2, 4, 0.016f, 0.032f, 0.19995117f, -0.19995117f))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.setSpriteAnimationPaused(16, 0));
        color = EncounterMoveAnimation.color(32064);
        result =
                animationStep11
                        .append(owner.tintCombatant(16, 0.75f, 0.5f, 0.0f, color))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
