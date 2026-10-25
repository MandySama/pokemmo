package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 扮演（招式 ID：272）的攻击／事件阶段动画。
 *
 * <p>招式描述：扮演对手, 让自己的特性变得和对手相同.
 *
 * <p>动画说明：调整场景着色；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110272（名称）和 120272（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/RolePlayAnimation.java
 *
 * <p>源文件 SHA-256：eb091504b89396496f7487424af57958aab554b3fc2c94ef55febf9c3f8dfd6c
 */
final class ReferenceMoveProgram272 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .waitSeconds(0.6f)
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.tintEnvironment(4, 0, 0.0f, 0.75f, 0.05f),
                                0.6f)
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1421,
                                        1,
                                        14,
                                        0.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1421,
                                        2,
                                        14,
                                        83.333336f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1421,
                                        1,
                                        14,
                                        166.66667f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1421,
                                        2,
                                        14,
                                        250.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1421,
                                        1,
                                        14,
                                        333.33334f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1421,
                                        2,
                                        14,
                                        416.66666f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1421,
                                        1,
                                        14,
                                        500.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1421,
                                        2,
                                        14,
                                        583.3333f,
                                        0.8984375f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep10 =
                animationStep9
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1708,
                                        1,
                                        14,
                                        716.6667f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(owner.shakeCombatant(14, 2, 24, 0.0f, 0.016f, -2.5f, 0.0f))
                        .append(owner.tintCombatant(14, 0.5f, 0.0f, 0.75f, color));
        color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep11 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                animationStep10,
                                owner.tintCombatant(16, 0.5f, 0.0f, 0.75f, color),
                                1.2f)
                        .append(
                                owner.scaleCombatant(
                                        14, 3, 1, 0.016f, 0.048f, 0.19995117f, -0.39990234f));
        color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep12 =
                animationStep11
                        .append(owner.tintCombatant(14, 0.5f, 0.75f, 0.0f, color))
                        .waitSeconds(0.6f);
        color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                animationStep12,
                                owner.tintCombatant(16, 0.5f, 0.75f, 0.0f, color),
                                1.8f)
                        .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
