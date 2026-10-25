package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 破灭之愿☆（自定义动画注册 ID：3353）的蓄力阶段动画。
 *
 * <p>招式描述：使用招式2回合后, 会用无数道光束攻击对手.
 *
 * <p>动画说明：镜头聚焦施术者；调整场景着色；调整精灵叠加颜色；执行精灵缩放。
 *
 * <p>变体映射：基础招式 ID 为 353；按参考本地化规则沿用基础描述，并在名称末尾添加 ☆。
 *
 * <p>编号对应参考动画注册表；语言条目为 110353（名称）和 120353（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/DoomDesireAnimation.java
 *
 * <p>源文件 SHA-256：a562b3a6bffbe922a9559df897ec1328ff1f1b60d54e455fd310b4ad5d09443e
 */
final class ReferenceChargeProgram3353 {

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
