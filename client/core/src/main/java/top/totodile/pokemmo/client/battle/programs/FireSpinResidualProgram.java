package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 火焰旋涡：火焰束缚每回合持续伤害动画。
 *
 * <p>Reference: cn/pokemmo/battle/animation/special/BattleBs0Animation.java; SHA-256
 * 90d9a73e9ead71fd7a85ac55d5a5133a1a68a64df1862a7134163216462e991d.
 */
final class FireSpinResidualProgram {
    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result = null;

        ReferenceMoveTimeline animationStep2;
        ReferenceMoveTimeline animationStep3 =
                ReferenceMoveTimeline.waitThenParallel(0.6f)
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .onStart(owner.preloadParticleEffect(247))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1805,
                                        1,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 1, 480.0f, 480.0f));
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                animationStep3, owner.emitParticles(-1, 247, 0, 9, 8, 0.4f), 0.12f)
                        .append(
                                owner.scaleCombatant(
                                        14, 2, 1, 0.0f, 0.48f, 0.100097656f, -0.100097656f));
        result =
                animationStep2 =
                        ReferenceMoveTimeline.appendThenOuterParallel(
                                        animationStep4, owner.emitParticles(-1, 247, 1, 9, 8, 0.4f))
                                .append(owner.setSpriteAnimationPaused(14, 0))
                                .endGroup()
                                .beginParallel()
                                .waitSeconds(0.6f)
                                .endGroup()
                                .beginParallel()
                                .endGroup();

        return result;
    }
}
