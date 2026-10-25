package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 残酷掠夺（自定义动画注册 ID：1004）的蓄力阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦施术者；调整场景着色；调整精灵叠加颜色；执行精灵缩放。
 *
 * <p>编号对应参考动画注册表；语言条目为 111004（名称）和 121004（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1004Animation.java
 *
 * <p>源文件 SHA-256：849e8d72af22e5e58406b04dc466682a721f977e8aad7e8d23d8e45fb4360fbc
 */
final class ReferenceChargeProgram1004 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
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
                                                                                                                0.6f))
                                                                                        .endGroup()
                                                                                        .beginParallel()
                                                                                        .append(
                                                                                                owner
                                                                                                        .tintEnvironment(
                                                                                                                4,
                                                                                                                0,
                                                                                                                0.0f,
                                                                                                                0.8125f,
                                                                                                                0.025f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scaleCombatant(
                                                                                                                14,
                                                                                                                2,
                                                                                                                2,
                                                                                                                0.032f,
                                                                                                                0.096f,
                                                                                                                0.30004883f,
                                                                                                                0.30004883f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                1535,
                                                                                                                1,
                                                                                                                14,
                                                                                                                0.0f,
                                                                                                                0.9375f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                1535,
                                                                                                                2,
                                                                                                                14,
                                                                                                                666.6667f,
                                                                                                                0.9375f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .tintCombatant(
                                                                                                                14,
                                                                                                                0.75f,
                                                                                                                0.0f,
                                                                                                                0.625f,
                                                                                                                EncounterMoveAnimation
                                                                                                                        .color(
                                                                                                                                Short
                                                                                                                                        .MAX_VALUE))),
                                                                                owner.tintCombatant(
                                                                                        16,
                                                                                        0.75f,
                                                                                        0.0f,
                                                                                        0.625f,
                                                                                        EncounterMoveAnimation
                                                                                                .color(
                                                                                                        0)),
                                                                                0.2f),
                                                                owner.tintCombatant(
                                                                        14,
                                                                        0.75f,
                                                                        0.625f,
                                                                        0.0f,
                                                                        EncounterMoveAnimation
                                                                                .color(
                                                                                        Short
                                                                                                .MAX_VALUE)),
                                                                0.4f),
                                                owner.tintCombatant(
                                                        14,
                                                        0.75f,
                                                        0.0f,
                                                        0.625f,
                                                        EncounterMoveAnimation.color(
                                                                Short.MAX_VALUE)),
                                                0.6f)
                                        .append(
                                                owner.tintCombatant(
                                                        14,
                                                        0.75f,
                                                        0.625f,
                                                        0.0f,
                                                        EncounterMoveAnimation.color(
                                                                Short.MAX_VALUE))),
                                owner.tintCombatant(
                                        16, 0.75f, 0.625f, 0.0f, EncounterMoveAnimation.color(0)))
                        .append(owner.tintEnvironment(4, 0, 0.8125f, 0.0f, 0.025f))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
