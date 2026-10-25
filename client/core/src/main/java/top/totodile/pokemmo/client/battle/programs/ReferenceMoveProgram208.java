package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 喝牛奶（招式 ID：208）的攻击／事件阶段动画。
 *
 * <p>招式描述：恢复自身最大HP量的一半.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整精灵叠加颜色；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110208（名称）和 120208（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/MilkDrinkAnimation.java
 *
 * <p>源文件 SHA-256：fd23d18bf31ca1ea0bcdbd8e3836f8e60c32684e97c8eacd5ce265fe427a4f16
 */
final class ReferenceMoveProgram208 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1452,
                                        1,
                                        14,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1729,
                                        1,
                                        14,
                                        500.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        14,
                                        1500.0f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1358,
                                        2,
                                        14,
                                        1333.3334f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .onStart(owner.preloadParticleEffect(374))
                        .append(owner.emitParticles(-1, 374, 0, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 374, 1, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 374, 2, 9, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                animationStep8, owner.emitParticles(-1, 374, 3, 9, 8, 0.5f), 1.2f),
                        owner.tintCombatant(14, 0.5f, 0.0f, 0.75f, color),
                        1.8f);
        color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep9, owner.tintCombatant(14, 0.5f, 0.75f, 0.0f, color))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
