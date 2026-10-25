package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 找伙伴（招式 ID：494）的攻击／事件阶段动画。
 *
 * <p>招式描述：用神奇的节奏跳舞. 使对手模仿自己的动作, 从而将特性变成一样.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110494（名称）和 120494（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/EntrainmentAnimation.java
 *
 * <p>源文件 SHA-256：f244a53648cc0c32e5c18e893576dbfe4e91114f7e48575a6f929210bd335938
 */
final class ReferenceMoveProgram494 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.appendThenNextParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1731,
                                                                                        1,
                                                                                        14,
                                                                                        166.66667f,
                                                                                        0.546875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1551,
                                                                                        2,
                                                                                        14,
                                                                                        166.66667f,
                                                                                        0.390625f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1551,
                                                                                        2,
                                                                                        14,
                                                                                        366.66666f,
                                                                                        0.390625f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1551,
                                                                                        2,
                                                                                        14,
                                                                                        566.6667f,
                                                                                        0.390625f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        0, 0.4f))
                                                                        .append(
                                                                                owner
                                                                                        .setSpriteAnimationPaused(
                                                                                                14,
                                                                                                1))
                                                                        .endGroup()
                                                                        .beginParallel(),
                                                                owner.scaleCombatant(
                                                                        14,
                                                                        2,
                                                                        2,
                                                                        0.0f,
                                                                        0.24f,
                                                                        -0.100097656f,
                                                                        0.100097656f))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        14, 0))
                                                        .beginSequence()
                                                        .waitSeconds(0.2f)
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        16, 1))
                                                        .endGroup()
                                                        .endGroup()
                                                        .endGroup()
                                                        .beginParallel(),
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        2,
                                                        0.0f,
                                                        0.16f,
                                                        -0.100097656f,
                                                        0.100097656f),
                                                0.08f)
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1731,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.546875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1551,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.390625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1551,
                                                        2,
                                                        16,
                                                        200.0f,
                                                        0.390625f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        1551,
                                        2,
                                        16,
                                        400.0f,
                                        0.390625f,
                                        owner.casterHandle))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
