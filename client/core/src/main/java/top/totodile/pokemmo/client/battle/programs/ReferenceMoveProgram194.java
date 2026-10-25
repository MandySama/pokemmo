package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 同命（招式 ID：194）的攻击／事件阶段动画。
 *
 * <p>招式描述：使出招式后, 当受到对手攻击陷入濒死时, 对手也会一同濒死. 连续使出则会失败.
 *
 * <p>动画说明：播放参考粒子特效；调整场景着色；调整精灵叠加颜色；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110194（名称）和 120194（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/DestinyBondAnimation.java
 *
 * <p>源文件 SHA-256：17a97096399b1760e7e9a80156f48f75db4284d802eb3be0faac5df57221ff04
 */
final class ReferenceMoveProgram194 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence().beginParallel(),
                                                owner.tintEnvironment(4, 0, 0.0f, 1.0f, 0.075f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1480,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        1,
                                                        14,
                                                        1333.3334f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 1, 640.0f, 640.0f))
                                        .onStart(owner.preloadParticleEffect(360)),
                                owner.launchParticles(360, 0, 9, 11, 0.5f, 600.0f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1478, 0, 16, 0.0f, 0.3125f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1728, 2, 16, 0.0f, 0.9375f, owner.casterHandle));
        Color color = EncounterMoveAnimation.color(31710);
        ReferenceMoveTimeline animationStep6 =
                animationStep5
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1376,
                                        2,
                                        16,
                                        1333.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 1, 960.0f, 320.0f))
                        .append(owner.adjustSoundTrack(2, 0, 0.0f, 1280.0f))
                        .append(owner.tintCombatant(14, 0.75f, 0.0f, 0.625f, color));
        color = EncounterMoveAnimation.color(31710);
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep6, owner.tintCombatant(16, 0.75f, 0.0f, 0.625f, color));
        color = EncounterMoveAnimation.color(31710);
        ReferenceMoveTimeline animationStep8 =
                animationStep7.append(owner.tintCombatant(14, 0.75f, 0.625f, 0.0f, color));
        color = EncounterMoveAnimation.color(31710);
        result =
                animationStep8
                        .append(owner.tintCombatant(16, 0.75f, 0.625f, 0.0f, color))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.tintEnvironment(4, 0, 1.0f, 0.0f, 0.075f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
