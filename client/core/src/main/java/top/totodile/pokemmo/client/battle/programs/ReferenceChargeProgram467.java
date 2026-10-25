package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 暗影潜袭（招式 ID：467）的蓄力阶段动画。
 *
 * <p>招式描述：第1回合消失踪影, 第2回合攻击对手. 即使对手正受保护, 也能击中.
 *
 * <p>动画说明：调整场景着色；控制精灵显隐。
 *
 * <p>编号对应参考动画注册表；语言条目为 110467（名称）和 120467（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/ShadowForceAnimation.java
 *
 * <p>源文件 SHA-256：8f4c7967ac6bce1d2dd75aeedf6480100b00e9513109ca967526f95064333074
 */
final class ReferenceChargeProgram467 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence().beginParallel().waitSeconds(0.6f),
                                owner.tintEnvironment(4, 16912, 0.0f, 0.75f, 0.1f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1630,
                                        1,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1630,
                                        1,
                                        14,
                                        250.0f,
                                        0.46875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1630,
                                        1,
                                        14,
                                        500.0f,
                                        0.15625f,
                                        owner.casterHandle))
                        .append(owner.setCombatantVisibility(14, 3))
                        .beginSequence()
                        .waitSeconds(0.04f)
                        .beginParallel();
        animationStep4 =
                ReferenceMoveTimeline.appendVisibilityStep(owner, 14, 4, animationStep4, 0.08f);
        animationStep4 =
                ReferenceMoveTimeline.appendVisibilityStep(owner, 14, 3, animationStep4, 0.12f);
        animationStep4 =
                ReferenceMoveTimeline.appendVisibilityStep(owner, 14, 4, animationStep4, 0.16f);
        animationStep4 =
                ReferenceMoveTimeline.appendVisibilityStep(owner, 14, 3, animationStep4, 0.2f);
        animationStep4 =
                ReferenceMoveTimeline.appendVisibilityStep(owner, 14, 4, animationStep4, 0.24f);
        animationStep4 =
                ReferenceMoveTimeline.appendVisibilityStep(owner, 14, 3, animationStep4, 0.28f);
        animationStep4 =
                ReferenceMoveTimeline.appendVisibilityStep(owner, 14, 4, animationStep4, 0.32f);
        animationStep4 =
                ReferenceMoveTimeline.appendVisibilityStep(owner, 14, 3, animationStep4, 0.36f);
        animationStep4 =
                ReferenceMoveTimeline.appendVisibilityStep(owner, 14, 4, animationStep4, 0.4f);
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendVisibilityStep(
                                        owner, 14, 3, animationStep4, 0.44f)
                                .append(owner.setCombatantVisibility(14, 4))
                                .endGroup()
                                .endGroup()
                                .endGroup()
                                .beginParallel()
                                .append(owner.setCombatantVisibility(14, 3))
                                .waitSeconds(0.6f),
                        owner.tintEnvironment(4, 16912, 0.75f, 0.0f, 0.1f));
        return result;
    }
}
