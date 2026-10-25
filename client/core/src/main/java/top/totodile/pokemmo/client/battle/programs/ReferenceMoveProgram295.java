package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 洁净光芒（招式 ID：295）的攻击／事件阶段动画。
 *
 * <p>招式描述：释放耀眼的光芒进行攻击. 有时会降低对手的特防.
 *
 * <p>动画说明：播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110295（名称）和 120295（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/LusterPurgeAnimation.java
 *
 * <p>源文件 SHA-256：fb4d07e39c089d7d5ff78e4cfeec52fb34e1a4cc24198a55326122c63fc61770
 */
final class ReferenceMoveProgram295 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.waitThenParallel(0.6f)
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1504,
                                        1,
                                        14,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1505,
                                        2,
                                        14,
                                        1166.6666f,
                                        0.9375f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(461))
                        .append(owner.emitParticles(-1, 461, 2, 9, 8, 0.4f))
                        .append(owner.emitParticles(-1, 461, 3, 9, 8, 0.4f))
                        .append(owner.emitParticles(-1, 461, 4, 9, 8, 0.4f));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep7 =
                animationStep6
                        .append(owner.emitParticles(-1, 461, 1, 9, 8, 0.4f))
                        .append(owner.tintCombatant(14, 1.0f, 0.0f, 0.75f, color));
        color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep12 =
                animationStep7
                        .append(owner.tintCombatant(16, 1.0f, 0.0f, 0.75f, color))
                        .append(owner.tintEnvironment(4, Short.MAX_VALUE, 0.0f, 0.75f, 0.1f))
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        16,
                                        0.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        166.66667f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        16,
                                        333.33334f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        500.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        16,
                                        666.6667f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(owner.shakeCombatant(16, 2, 10, 0.0f, 0.048f, 0.8000488f, 0.0f));
        Color color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep13 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep12, owner.emitParticles(-1, 461, 0, 11, 8, 0.4f))
                        .append(owner.tintCombatant(14, 1.0f, 0.75f, 0.0f, color2));
        color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                animationStep13
                        .append(owner.tintCombatant(16, 1.0f, 0.75f, 0.0f, color2))
                        .append(owner.tintEnvironment(4, Short.MAX_VALUE, 0.75f, 0.0f, 0.1f))
                        .waitSeconds(0.6f)
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
