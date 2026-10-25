package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 快速防守（招式 ID：501）的攻击／事件阶段动画。
 *
 * <p>招式描述：保护己方全场不受到优先度招式影响一回合. 如果连续使用, 守住, 看穿以及挺住可能失败.
 *
 * <p>动画说明：播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110501（名称）和 120501（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/QuickGuardAnimation.java
 *
 * <p>源文件 SHA-256：26385052524bed93d6c1516c1994792e412c5e29ddcb88fbbaaa0affbf9e6866
 */
final class ReferenceMoveProgram501 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep2 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1434,
                                        1,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                animationStep2
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1630,
                                                                        2,
                                                                        14,
                                                                        333.33334f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .waitSeconds(0.4f)
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        14, 1))
                                                        .append(
                                                                owner.scaleCombatant(
                                                                        14,
                                                                        2,
                                                                        1,
                                                                        0.0f,
                                                                        0.32f,
                                                                        -0.100097656f,
                                                                        0.39990234f)),
                                                owner.tintCombatant(14, 1.0f, 0.625f, 0.0f, color),
                                                0.12f)
                                        .onStart(owner.preloadParticleEffect(668))
                                        .append(owner.emitParticles(-1, 668, 0, 9, 8, 0.625f))
                                        .append(owner.emitParticles(-1, 668, 1, 9, 8, 0.625f))
                                        .append(owner.emitParticles(-1, 668, 2, 9, 8, 0.625f))
                                        .append(owner.emitParticles(-1, 668, 3, 9, 8, 0.625f)),
                                owner.emitParticles(-1, 668, 4, 9, 8, 0.625f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
