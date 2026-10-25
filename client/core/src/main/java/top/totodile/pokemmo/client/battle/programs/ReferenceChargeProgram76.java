package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 日光束（招式 ID：76）的蓄力阶段动画。
 *
 * <p>招式描述：第1回合收集满满的日光, 第2回合发射光束进行攻击.
 *
 * <p>动画说明：播放参考粒子特效；调整场景着色；调整精灵叠加颜色。
 *
 * <p>编号对应参考动画注册表；语言条目为 110076（名称）和 120076（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SolarBeamAnimation.java
 *
 * <p>源文件 SHA-256：84aca4d52361e09ea0b6a7a26d85b9c26143424b7e48946847710d5f9df4a9ce
 */
final class ReferenceChargeProgram76 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence().beginParallel().waitSeconds(0.6f),
                                owner.tintEnvironment(2, 0, 0.0f, 0.625f, 0.075f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1497,
                                        1,
                                        14,
                                        0.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1480,
                                        2,
                                        14,
                                        0.0f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        14,
                                        1833.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        2,
                                        14,
                                        1833.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 1920.0f))
                        .append(owner.adjustSoundTrack(2, 0, 0.0f, 1920.0f))
                        .append(owner.adjustSoundTrack(1, 1, 1120.0f, 640.0f))
                        .append(owner.adjustSoundTrack(2, 1, 1120.0f, 640.0f))
                        .onStart(owner.preloadParticleEffect(239));
        Color color = EncounterMoveAnimation.color(1023);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                animationStep5, owner.emitParticles(-1, 239, 0, 9, 8, 0.4f), 0.6f),
                        owner.tintCombatant(14, 0.25f, 0.0f, 0.8125f, color));
        color = EncounterMoveAnimation.color(1023);
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep6, owner.tintCombatant(14, 0.25f, 0.8125f, 0.0f, color));
        color = EncounterMoveAnimation.color(1023);
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep7, owner.tintCombatant(14, 0.25f, 0.0f, 0.8125f, color));
        color = EncounterMoveAnimation.color(1023);
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep8, owner.tintCombatant(14, 0.25f, 0.8125f, 0.0f, color));
        color = EncounterMoveAnimation.color(1023);
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep9, owner.tintCombatant(14, 0.25f, 0.0f, 0.8125f, color));
        color = EncounterMoveAnimation.color(1023);
        result =
                ReferenceMoveTimeline.appendAndClose(
                        animationStep10
                                .append(owner.tintCombatant(14, 0.25f, 0.8125f, 0.0f, color))
                                .endGroup()
                                .beginParallel()
                                .endGroup()
                                .beginParallel()
                                .waitSeconds(0.6f),
                        owner.tintEnvironment(2, 0, 0.625f, 0.0f, 0.075f));
        return result;
    }
}
