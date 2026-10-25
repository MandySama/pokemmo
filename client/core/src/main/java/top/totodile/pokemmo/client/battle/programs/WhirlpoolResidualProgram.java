package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 潮旋：漩涡束缚每回合持续伤害动画。
 *
 * <p>Reference: cn/pokemmo/battle/animation/special/BattleIz0Animation.java; SHA-256
 * f645fa7958d3502d4db917204dd52a9fc631e84ed1e89f79b8a0b727f6ed624b.
 */
final class WhirlpoolResidualProgram {
    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result = null;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, 1 != 0);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, 0 != 0);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, 0 != 0);
        ReferenceMoveTimeline animationStep2 =
                ReferenceMoveTimeline.waitThenParallel(0.6f)
                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.75f, 0.025f))
                        .onStart(owner.loadBattleBackground(28))
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintEnvironment(3, 0, 0.75f, 0.0f, 0.1f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                        .endGroup()
                        .beginParallel()
                        .append(owner.scrollBattleBackground(1, 60, 0, 1.92f))
                        .onStart(owner.preloadParticleEffect(416))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1810,
                                        1,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle));
        ReferenceMoveTimeline animationStep3 =
                animationStep2.append(owner.emitParticles(-1, 416, 0, 9, 8, 0.4f));
        ReferenceMoveTimeline animationStep4 =
                animationStep3.append(owner.emitParticles(-1, 416, 1, 9, 8, 0.4f));
        ReferenceMoveTimeline animationStep5 =
                animationStep4.append(owner.emitParticles(-1, 416, 2, 9, 8, 0.4f));
        Color color = EncounterMoveAnimation.color(20810);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                        animationStep5.append(owner.emitParticles(-1, 416, 3, 9, 8, 0.4f)),
                        owner.tintCombatant(14, 0.75f, 0.0f, 0.75f, color),
                        0.4f);
        color = EncounterMoveAnimation.color(20810);
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep6, owner.tintCombatant(14, 0.75f, 0.75f, 0.0f, color))
                        .append(owner.tintEnvironment(3, 0, 0.0f, 0.75f, 0.075f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                        .append(owner.tintEnvironment(2, 0, 0.75f, 0.0f, 0.075f))
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();

        return result;
    }
}
