package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 健美☆（自定义动画注册 ID：3339）的攻击／事件阶段动画。
 *
 * <p>招式描述：使出全身力气绷紧肌肉, 从而提高自己的攻击和防御.
 *
 * <p>动画说明：执行精灵缩放；控制精灵帧动画。
 *
 * <p>变体映射：基础招式 ID 为 339；按参考本地化规则沿用基础描述，并在名称末尾添加 ☆。
 *
 * <p>编号对应参考动画注册表；语言条目为 110339（名称）和 120339（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/BulkUpAnimation.java
 *
 * <p>源文件 SHA-256：76614784cf4404ce4ba79e18cd83075d93ddebba5baefd01b6fb2cfacf1324d5
 */
final class ReferenceMoveProgram3339 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                        ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                .append(owner.setSpriteAnimationPaused(16, 1))
                                                .append(owner.setSpriteAnimationPaused(14, 1))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1452,
                                                                1,
                                                                14,
                                                                0.0f,
                                                                0.8984375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1452,
                                                                2,
                                                                14,
                                                                333.33334f,
                                                                0.8984375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1452,
                                                                1,
                                                                14,
                                                                666.6667f,
                                                                0.8984375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1452,
                                                                2,
                                                                14,
                                                                1166.6666f,
                                                                0.8984375f,
                                                                owner.casterHandle)),
                                        owner.scaleCombatant(
                                                14,
                                                3,
                                                1,
                                                0.016f,
                                                0.096f,
                                                0.39990234f,
                                                -0.39990234f)),
                                owner.scaleCombatant(
                                        14, 2, 1, 0.016f, 0.096f, 0.39990234f, 0.39990234f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
