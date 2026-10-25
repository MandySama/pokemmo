package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 预知未来（招式 ID：248）的蓄力阶段动画。
 *
 * <p>招式描述：在使用招式2回合后, 向对手发送一团念力进行攻击.
 *
 * <p>动画说明：调整场景着色；调整精灵叠加颜色；执行精灵位移。
 *
 * <p>编号对应参考动画注册表；语言条目为 110248（名称）和 120248（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/FutureSightAnimation.java
 *
 * <p>源文件 SHA-256：e80f2e6fc3af0d650e123b9bef1ac6a30b054bc0625c46fc6819c95fe7d7b0f6
 */
final class ReferenceChargeProgram248 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .waitSeconds(0.6f),
                                                owner.tintEnvironment(4, 0, 0.0f, 0.8125f, 0.075f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1965,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.tintCombatant(
                                                        14,
                                                        0.75f,
                                                        0.0f,
                                                        0.625f,
                                                        EncounterMoveAnimation.color(
                                                                Short.MAX_VALUE))),
                                owner.shakeCombatant(14, 2, 3, 0.0f, 0.064f, 0.5f, 0.0f))
                        .append(
                                owner.tintCombatant(
                                        14,
                                        0.75f,
                                        0.625f,
                                        0.0f,
                                        EncounterMoveAnimation.color(Short.MAX_VALUE)))
                        .append(owner.tintEnvironment(4, 0, 0.8125f, 0.0f, 0.075f))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
