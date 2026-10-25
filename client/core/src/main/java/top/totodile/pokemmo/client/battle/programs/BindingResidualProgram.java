package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 绑紧／紧束：束缚生效后每回合触发的持续伤害动画。
 *
 * <p>Reference: cn/pokemmo/battle/animation/special/BattleEe00Animation.java; SHA-256
 * af57cdaf1a02f51334d3187280335df736d5191a15c08f176fea0d43cd1f2826.
 */
final class BindingResidualProgram {
    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result = null;

        ReferenceMoveTimeline animationStep2;
        ReferenceMoveTimeline animationStep3 =
                ReferenceMoveTimeline.waitThenParallel(0.6f)
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .onStart(owner.preloadParticleEffect(197))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1804,
                                        1,
                                        14,
                                        166.66667f,
                                        0.78125f,
                                        owner.casterHandle));
        ReferenceMoveTimeline animationStep4 =
                animationStep3.append(
                        owner.scheduleSound(
                                (byte) 2, 1804, 1, 14, 800.0f, 0.9921875f, owner.casterHandle));
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                animationStep4, owner.emitParticles(-1, 197, 0, 9, 8, 0.4f), 0.12f),
                        owner.scaleCombatant(14, 2, 5, 0.0f, 0.128f, -0.19995117f, 0.30004883f),
                        0.72f);
        result =
                animationStep2 =
                        ReferenceMoveTimeline.appendThenOuterParallel(
                                        animationStep5, owner.emitParticles(-1, 197, 0, 9, 8, 0.4f))
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
