package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 看穿（招式 ID：197）的攻击／事件阶段动画。
 *
 * <p>招式描述：完全抵挡对手的攻击. 连续使出则容易失败.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110197（名称）和 120197（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/DetectAnimation.java
 *
 * <p>源文件 SHA-256：290ff14a01c50a96d94c0fe0b41476884628d349d562a3d3bcd5e49b7aeda81e
 */
final class ReferenceMoveProgram197 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.tintEnvironment(4, 0, 0.0f, 1.0f, 0.075f))
                        .append(owner.focusCamera(0, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintCombatant(14, 1.0f, 0.0f, 0.75f, color))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1486,
                                        1,
                                        14,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1471,
                                        2,
                                        14,
                                        0.0f,
                                        0.703125f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(363))
                        .append(owner.emitParticles(-1, 363, 0, 9, 8, 0.5f));
        Color color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep4
                                        .append(owner.emitParticles(-1, 363, 1, 9, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(14, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.064f, -0.19995117f, 0.19995117f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.tintCombatant(14, 1.0f, 0.75f, 0.0f, color2))
                        .append(owner.tintEnvironment(4, 0, 1.0f, 0.0f, 0.075f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
