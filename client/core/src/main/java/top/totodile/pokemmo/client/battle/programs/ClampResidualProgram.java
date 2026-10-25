package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 贝壳夹击：夹击束缚每回合持续伤害动画。
 *
 * <p>Reference: cn/pokemmo/battle/animation/special/BattleRd2Animation.java; SHA-256
 * 501511b8d02014b4dea3a6e732dcc1572625c1538bf6a4513b5f0b2a9c47f073.
 */
final class ClampResidualProgram {
    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result = null;
        ReferenceMoveTimeline animationStep2 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .onStart(owner.preloadParticleEffect(293))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1421,
                                        1,
                                        14,
                                        166.66667f,
                                        0.859375f,
                                        owner.casterHandle));
        ReferenceMoveTimeline animationStep3 =
                animationStep2.append(
                        owner.scheduleSound(
                                (byte) 2,
                                (short) 1575,
                                2,
                                14,
                                500.0f,
                                0.859375f,
                                owner.casterHandle));
        ReferenceMoveTimeline animationStep4 =
                animationStep3.append(
                        owner.scheduleSound(
                                (byte) 2,
                                (short) 1693,
                                1,
                                14,
                                500.0f,
                                0.9375f,
                                owner.casterHandle));
        ReferenceMoveTimeline animationStep5 =
                animationStep4.append(owner.emitParticles(-1, 293, 1, 9, 8, 0.4f));
        ReferenceMoveTimeline animationStep6 =
                animationStep5.append(owner.emitParticles(-1, 293, 0, 9, 8, 0.4f));
        ReferenceMoveTimeline animationStep7 =
                animationStep6.append(owner.emitParticles(-1, 293, 2, 9, 8, 0.4f));
        ReferenceMoveTimeline animationStep8 =
                animationStep7.append(owner.emitParticles(-1, 293, 3, 9, 8, 0.4f));
        ReferenceMoveTimeline animationStep9 =
                animationStep8.append(owner.emitParticles(-1, 293, 1, 9, 8, 0.4f));
        ReferenceMoveTimeline animationStep10 =
                animationStep9.append(owner.emitParticles(-1, 293, 0, 9, 8, 0.4f));
        ReferenceMoveTimeline animationStep11 =
                animationStep10.append(owner.emitParticles(-1, 293, 2, 9, 8, 0.4f));
        result =
                animationStep11
                        .append(owner.emitParticles(-1, 293, 3, 9, 8, 0.4f))
                        .beginSequence()
                        .waitSeconds(0.4f)
                        .beginParallel()
                        .append(
                                owner.scaleCombatant(
                                        14, 2, 1, 0.0f, 0.128f, -0.19995117f, 0.19995117f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();

        return result;
    }
}
