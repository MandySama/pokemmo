package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 春风野火（自定义动画注册 ID：1032）的攻击／事件阶段动画。
 *
 * <p>招式描述：与另一侧的己方目标交换位置. 连续使用可能失败.
 *
 * <p>动画说明：镜头聚焦施术者；执行精灵位移；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111032（名称）和 121032（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/AllySwitchAnimation.java
 *
 * <p>源文件 SHA-256：09b8b3cff40664ec13222a104faa20e4c48c434c7063ab8c9d4c2ee7b173fc5a
 */
final class ReferenceMoveProgram1032 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenDelayedParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .sequence()
                                                                                        .beginParallel()
                                                                                        .append(
                                                                                                owner
                                                                                                        .focusCamera(
                                                                                                                0,
                                                                                                                0.4f))
                                                                                        .endGroup()
                                                                                        .beginParallel()
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                1437,
                                                                                                                5,
                                                                                                                2,
                                                                                                                0.0f,
                                                                                                                0.9921875f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                1437,
                                                                                                                5,
                                                                                                                2,
                                                                                                                266.66666f,
                                                                                                                0.9921875f,
                                                                                                                owner.casterHandle)),
                                                                                owner
                                                                                        .shakeCombatant(
                                                                                                14,
                                                                                                3,
                                                                                                2,
                                                                                                0.0f,
                                                                                                0.064f,
                                                                                                0.5f,
                                                                                                0.0f),
                                                                                0.64f)
                                                                        .onStart(
                                                                                owner
                                                                                        .setHealthHudHidden(
                                                                                                14,
                                                                                                true))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1437,
                                                                                        5,
                                                                                        2,
                                                                                        0.0f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1437,
                                                                                        5,
                                                                                        2,
                                                                                        200.0f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1437,
                                                                                        5,
                                                                                        2,
                                                                                        400.0f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle)),
                                                                owner.shakeCombatant(
                                                                        14, 3, 3, 0.0f, 0.048f,
                                                                        1.0f, 0.0f),
                                                                1.36f)
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1437,
                                                                        5,
                                                                        2,
                                                                        0.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1437,
                                                                        5,
                                                                        2,
                                                                        133.33333f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1437,
                                                                        5,
                                                                        2,
                                                                        266.66666f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1437,
                                                                        5,
                                                                        2,
                                                                        400.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle)),
                                                owner.shakeCombatant(
                                                        14, 3, 4, 0.0f, 0.032f, 1.5f, 0.0f),
                                                2.0f)
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1437,
                                                        5,
                                                        2,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1437,
                                                        5,
                                                        2,
                                                        66.666664f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1437,
                                                        5,
                                                        2,
                                                        133.33333f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1437,
                                                        5,
                                                        2,
                                                        200.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1437,
                                                        5,
                                                        2,
                                                        266.66666f,
                                                        0.9921875f,
                                                        owner.casterHandle)),
                                owner.shakeCombatant(14, 3, 5, 0.0f, 0.016f, 2.0f, 0.0f),
                                2.4f)
                        .append(owner.shakeCombatant(14, 1, 0, 0.0f, 0.0f, 0.0f, -32.0f))
                        .onStart(owner.setHealthHudHidden(14, false))
                        .append(owner.restoreCamera(0.25f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
