package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 暗影拳（招式 ID：325）的攻击／事件阶段动画。
 *
 * <p>招式描述：使出混影之拳. 攻击必定会命中.
 *
 * <p>动画说明：播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110325（名称）和 120325（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/ShadowPunchAnimation.java
 *
 * <p>源文件 SHA-256：98c7f68f1e89828f4e0263e0197c87b9a2bb3b5a4e6a40161a98e5749eb3dcde
 */
final class ReferenceMoveProgram325 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Color color = EncounterMoveAnimation.color(0);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.waitThenParallel(0.6f),
                                owner.tintCombatant(14, 0.25f, 0.0f, 0.8125f, color))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1421, 2, 14, 0.0f, 0.9375f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1873,
                                        1,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(495))
                        .append(owner.shakeCombatant(14, 2, 1, 0.0f, 0.048f, 2.0f, 0.0f))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .append(owner.emitParticles(-1, 495, 3, 11, 8, 0.4f))
                        .append(owner.emitParticles(-1, 495, 0, 11, 8, 0.4f))
                        .append(owner.emitParticles(-1, 495, 1, 11, 8, 0.4f));
        Color color2 = EncounterMoveAnimation.color(0);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep6
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1420,
                                                        2,
                                                        16,
                                                        133.33333f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.064f, -0.19995117f, 0.19995117f))
                        .append(owner.tintCombatant(14, 0.25f, 0.8125f, 0.0f, color2))
                        .waitSeconds(0.6f)
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
