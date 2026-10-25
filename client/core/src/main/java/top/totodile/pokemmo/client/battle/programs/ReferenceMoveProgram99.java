package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 愤怒（招式 ID：99）的攻击／事件阶段动画。
 *
 * <p>招式描述：如果在使出招式后受到攻击的话, 会因愤怒的力量而提高攻击.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110099（名称）和 120099（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/RageAnimation.java
 *
 * <p>源文件 SHA-256：52c32269380808999b4af5f4aff1fafe03c0c588074b182e569e7c97779973d4
 */
final class ReferenceMoveProgram99 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep2 =
                ReferenceMoveTimeline.waitThenParallel(0.6f)
                        .append(owner.tintCombatant(14, 1.0f, 0.0f, 0.625f, color));
        color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep6 =
                animationStep2
                        .append(owner.tintCombatant(14, 1.0f, 0.625f, 0.0f, color))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1463, 2, 14, 0.0f, 0.9375f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1376, 2, 14, 916.6667f, 0.0f, owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 0, 0.0f, 880.0f))
                        .append(owner.adjustSoundTrack(2, 1, 640.0f, 160.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1464, 1, 14, 0.0f, 0.625f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1376,
                                        1,
                                        14,
                                        916.6667f,
                                        0.0f,
                                        owner.casterHandle));
        Color color2 = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep6
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1437,
                                                        1,
                                                        14,
                                                        1000.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 880.0f)),
                                owner.adjustSoundTrack(1, 1, 640.0f, 160.0f))
                        .append(owner.tintCombatant(14, 1.0f, 0.0f, 0.625f, color2));
        color2 = EncounterMoveAnimation.color(31);
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenNextParallel(
                                                                                animationStep7,
                                                                                owner.tintCombatant(
                                                                                        14, 1.0f,
                                                                                        0.625f,
                                                                                        0.0f,
                                                                                        color2)),
                                                                owner.scaleCombatant(
                                                                        14,
                                                                        2,
                                                                        1,
                                                                        0.016f,
                                                                        0.048f,
                                                                        0.30004883f,
                                                                        -0.30004883f),
                                                                0.2f)
                                                        .append(owner.focusCamera(1, 0.4f)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1427,
                                                        5,
                                                        16,
                                                        0.0f,
                                                        0.0f,
                                                        owner.casterHandle),
                                                0.4f)
                                        .onStart(owner.preloadParticleEffect(267))
                                        .append(owner.emitParticles(-1, 267, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 267, 1, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.032f,
                                                        0.30004883f,
                                                        -0.30004883f)),
                                owner.scheduleSound(
                                        (byte) 2, 1424, 2, 16, 0.0f, 0.9375f, owner.casterHandle),
                                0.6f)
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
