package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 闪光（招式 ID：148）的攻击／事件阶段动画。
 *
 * <p>招式描述：使出耀眼光芒, 从而降低对手的命中率.
 *
 * <p>动画说明：镜头聚焦施术者；调整场景着色；调整精灵叠加颜色；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110148（名称）和 120148（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/FlashAnimation.java
 *
 * <p>源文件 SHA-256：3827cac2fc88f51f72fc9527bf510926d03d3ede4c268520868fa072db94bad1
 */
final class ReferenceMoveProgram148 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Color color = EncounterMoveAnimation.color(0);
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.6f))
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintCombatant(14, 0.25f, 0.0f, 1.0f, color))
                        .append(owner.tintEnvironment(4, Short.MAX_VALUE, 0.0f, 0.9375f, 0.05f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1724, 1, 14, 0.0f, 0.78125f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1724,
                                        1,
                                        14,
                                        333.33334f,
                                        0.390625f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1724,
                                        1,
                                        14,
                                        666.6667f,
                                        0.15625f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1724,
                                        1,
                                        14,
                                        1000.0f,
                                        0.0390625f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1724, 2, 16, 0.0f, 0.78125f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1724,
                                        2,
                                        16,
                                        333.33334f,
                                        0.390625f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1724,
                                        2,
                                        16,
                                        666.6667f,
                                        0.15625f,
                                        owner.casterHandle));
        Color color2 = EncounterMoveAnimation.color(0);
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                        ReferenceMoveTimeline.appendThenNextParallel(
                                        animationStep8,
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1724,
                                                2,
                                                16,
                                                1000.0f,
                                                0.0390625f,
                                                owner.casterHandle))
                                .append(owner.tintCombatant(14, 0.25f, 1.0f, 1.0f, color2)),
                        owner.tintEnvironment(4, Short.MAX_VALUE, 0.9375f, 0.9375f, 0.025f),
                        1.0f);
        color2 = EncounterMoveAnimation.color(0);
        result =
                animationStep9
                        .append(owner.tintCombatant(14, 0.5f, 1.0f, 0.0f, color2))
                        .append(owner.tintEnvironment(4, Short.MAX_VALUE, 0.9375f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
