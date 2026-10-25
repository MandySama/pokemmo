package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 彗星拳（招式 ID：309）的攻击／事件阶段动画。
 *
 * <p>招式描述：使出彗星般的拳头攻击对手. 有时会提高自己的攻击.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110309（名称）和 120309（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/MeteorMashAnimation.java
 *
 * <p>源文件 SHA-256：296589a34a9dddfb31d52838c30fa6f4036f4a916d2ae9169d43c061c26c7c8a
 */
final class ReferenceMoveProgram309 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.75f, 0.1f))
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1665,
                                        1,
                                        16,
                                        0.0f,
                                        0.390625f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1780,
                                        2,
                                        16,
                                        0.0f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        16,
                                        833.3333f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        2,
                                        16,
                                        666.6667f,
                                        0.9375f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(476))
                        .append(owner.emitParticles(-1, 476, 3, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 476, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 476, 1, 11, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep8
                                        .append(owner.emitParticles(-1, 476, 2, 11, 8, 0.5f))
                                        .append(owner.tintCombatant(16, 0.75f, 0.0f, 0.75f, color))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .beginSequence()
                                        .waitSeconds(0.6f)
                                        .beginParallel(),
                                owner.scaleCombatant(
                                        16, 2, 3, 0.016f, 0.032f, 0.19995117f, -0.19995117f))
                        .append(owner.restoreCamera(0.4f));
        color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendAndClose(
                        animationStep9
                                .append(owner.tintCombatant(16, 0.75f, 0.75f, 0.0f, color))
                                .append(owner.setSpriteAnimationPaused(16, 0)),
                        owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.1f));
        return result;
    }
}
