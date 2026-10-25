package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 熔岩风暴：熔岩束缚每回合持续伤害动画。
 *
 * <p>Reference: cn/pokemmo/battle/animation/special/BattleT600Animation.java; SHA-256
 * 468310208aadad94689975040f5a8bb01c7de16db7968d40c703c0d4a55c4ef0.
 */
final class MagmaStormResidualProgram {
    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result = null;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, 1 != 0);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, 0 != 0);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, 0 != 0);
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep2 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .onStart(owner.loadBattleBackground(38))
                                                        .onStart(
                                                                ReferenceMoveTimeline.callback(
                                                                        sceneFlagAction2)),
                                                owner.tintEnvironment(4, 0, 0.0f, 1.0f, 0.05f))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                                        .append(owner.tintEnvironment(3, 0, 0.8125f, 0.0f, 0.05f))
                                        .append(owner.scrollBattleBackground(4, 0, 24, 2.88f))
                                        .onStart(owner.setHealthHudHidden(18, true))
                                        .onStart(owner.preloadParticleEffect(639))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1806,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(14, 0))
                                        .beginSequence()
                                        .waitSeconds(0.32f)
                                        .beginParallel(),
                                owner.scaleCombatant(
                                        14, 2, 6, 0.032f, 0.064f, -0.100097656f, 0.100097656f),
                                0.64f),
                        owner.tintCombatant(14, 0.5f, 0.0f, 0.625f, color),
                        0.96f);
        color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep3 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        animationStep2, owner.tintCombatant(14, 0.5f, 0.625f, 0.0f, color), 1.28f);
        color = EncounterMoveAnimation.color(31);
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        Runnable sceneFlagAction6 = owner.sceneFlag(1, true);
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenOuterParallel(
                                        animationStep3,
                                        owner.tintCombatant(14, 0.5f, 0.0f, 0.625f, color)),
                                owner.tintEnvironment(3, 0, 0.0f, 1.0f, 0.05f))
                        .append(owner.tintEnvironment(2, 0, 1.0f, 0.0f, 0.025f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)));
        color = EncounterMoveAnimation.color(31);
        result =
                animationStep4
                        .append(owner.tintCombatant(14, 0.5f, 0.625f, 0.0f, color))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();

        return result;
    }
}
