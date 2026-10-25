package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 流沙地狱：流沙束缚每回合持续伤害动画。
 *
 * <p>Reference: cn/pokemmo/battle/animation/special/BattlePa1Animation.java; SHA-256
 * 103cb5c3ba98d8ad345971990ce8762073bdcf1a43138b7d50cc429bc99f03a3.
 */
final class SandTombResidualProgram {
    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result = null;
        ReferenceMoveTimeline animationStep2 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .onStart(owner.preloadParticleEffect(498))
                        .waitSeconds(0.6f)
                        .onStart(owner.setHealthHudHidden(18, true))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1811,
                                        1,
                                        14,
                                        166.66667f,
                                        0.9921875f,
                                        owner.casterHandle));
        ReferenceMoveTimeline animationStep3 =
                animationStep2.append(
                        owner.scheduleSound(
                                (byte) 2,
                                (short) 1811,
                                1,
                                14,
                                300.0f,
                                0.9921875f,
                                owner.casterHandle));
        ReferenceMoveTimeline animationStep4 =
                animationStep3.append(
                        owner.scheduleSound(
                                (byte) 2,
                                (short) 1811,
                                1,
                                14,
                                433.33334f,
                                0.9921875f,
                                owner.casterHandle));
        ReferenceMoveTimeline animationStep5 =
                animationStep4.append(
                        owner.scheduleSound(
                                (byte) 2,
                                (short) 1811,
                                1,
                                14,
                                566.6667f,
                                0.9921875f,
                                owner.casterHandle));
        ReferenceMoveTimeline animationStep6 =
                animationStep5.append(
                        owner.scheduleSound(
                                (byte) 2,
                                (short) 1811,
                                1,
                                14,
                                700.0f,
                                0.9921875f,
                                owner.casterHandle));
        ReferenceMoveTimeline animationStep7 =
                animationStep6.append(
                        owner.scheduleSound(
                                (byte) 2,
                                (short) 1811,
                                1,
                                14,
                                833.3333f,
                                0.9921875f,
                                owner.casterHandle));
        ReferenceMoveTimeline animationStep8 =
                animationStep7.append(
                        owner.scheduleSound(
                                (byte) 2,
                                (short) 1811,
                                1,
                                14,
                                966.6667f,
                                0.9921875f,
                                owner.casterHandle));
        ReferenceMoveTimeline animationStep9 =
                animationStep8.append(
                        owner.scheduleSound(
                                (byte) 2, (short) 1810, 2, 14, 0.0f, 0.15625f, owner.casterHandle));
        ReferenceMoveTimeline animationStep10 =
                animationStep9.append(owner.emitParticles(-1, 498, 0, 9, 8, 0.4f));
        ReferenceMoveTimeline animationStep11 =
                animationStep10.append(owner.emitParticles(-1, 498, 1, 9, 8, 0.4f));
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep11
                                        .append(owner.emitParticles(-1, 498, 2, 9, 8, 0.4f))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .beginSequence()
                                        .waitSeconds(0.6f)
                                        .beginParallel(),
                                owner.shakeCombatant(14, 2, 8, 0.016f, 0.048f, 0.30004883f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup()
                        .beginParallel()
                        .endGroup();

        return result;
    }
}
