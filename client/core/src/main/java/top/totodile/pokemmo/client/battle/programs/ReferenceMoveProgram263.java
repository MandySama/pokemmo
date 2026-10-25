package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 硬撑（招式 ID：263）的攻击／事件阶段动画。
 *
 * <p>招式描述：当自己处于中毒、麻痹、灼伤状态时, 向对手使出此招式的话, 威力会变成2倍.
 *
 * <p>动画说明：播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110263（名称）和 120263（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/FacadeAnimation.java
 *
 * <p>源文件 SHA-256：c58e2661ae5a404852bb4ef8d2a7e0a9b309989eb8874c9a9a28d84f36491122
 */
final class ReferenceMoveProgram263 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.waitThenParallel(0.6f)
                        .append(
                                owner.scaleCombatant(
                                        14, 2, 3, 0.016f, 0.096f, 0.30004883f, -0.30004883f))
                        .onStart(owner.preloadParticleEffect(429))
                        .append(owner.emitParticles(-1, 429, 0, 9, 8, 0.4f))
                        .append(owner.emitParticles(-1, 429, 1, 9, 8, 0.4f))
                        .append(owner.emitParticles(-1, 429, 2, 9, 8, 0.4f));
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                        animationStep4
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1412,
                                                1,
                                                14,
                                                0.0f,
                                                0.9921875f,
                                                owner.casterHandle))
                                .append(owner.adjustSoundTrack(1, 1, 640.0f, 480.0f)),
                        owner.tintCombatant(14, 0.75f, 0.0f, 0.5f, color),
                        0.12f);
        color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        animationStep5, owner.tintCombatant(14, 0.75f, 0.5f, 0.0f, color), 0.24f);
        color = EncounterMoveAnimation.color(31744);
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        animationStep6, owner.tintCombatant(14, 0.75f, 0.0f, 0.5f, color), 0.36f);
        color = EncounterMoveAnimation.color(31744);
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        animationStep7, owner.tintCombatant(14, 0.75f, 0.5f, 0.0f, color), 0.48f);
        color = EncounterMoveAnimation.color(1023);
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        animationStep8, owner.tintCombatant(14, 0.75f, 0.0f, 0.75f, color), 0.6f);
        color = EncounterMoveAnimation.color(1023);
        result =
                animationStep9
                        .append(owner.tintCombatant(14, 0.75f, 0.5f, 0.0f, color))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(owner.emitParticles(-1, 429, 3, 11, 8, 0.4f))
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.048f, 0.30004883f, -0.30004883f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1420,
                                        2,
                                        16,
                                        0.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
