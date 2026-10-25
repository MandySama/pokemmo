package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 羽毛舞（招式 ID：297）的攻击／事件阶段动画。
 *
 * <p>招式描述：撒出羽毛, 笼罩在对手的周围. 大幅降低对手的攻击.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110297（名称）和 120297（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/FeatherDanceAnimation.java
 *
 * <p>源文件 SHA-256：b8b230e59fc77b59f90d6b2a599ca3459da3baa8b730a6667e7ca76a625b1bf8
 */
final class ReferenceMoveProgram297 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1478,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1478,
                                                        2,
                                                        14,
                                                        666.6667f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1478,
                                                        1,
                                                        14,
                                                        1333.3334f,
                                                        0.9921875f,
                                                        owner.casterHandle)),
                                owner.scaleCombatant(
                                        14, 2, 3, 0.0f, 0.32f, -0.39990234f, 0.39990234f))
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1438,
                                        1,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1845,
                                        2,
                                        16,
                                        0.0f,
                                        0.703125f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 0, 0.0f, 640.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1438,
                                        1,
                                        16,
                                        583.3333f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1845,
                                        2,
                                        16,
                                        666.6667f,
                                        0.390625f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 0, 720.0f, 640.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1845,
                                        2,
                                        16,
                                        1333.3334f,
                                        0.0390625f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 0, 1280.0f, 640.0f))
                        .onStart(owner.preloadParticleEffect(463));
        Color color = EncounterMoveAnimation.color(0);
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep9.append(owner.emitParticles(-1, 463, 0, 11, 8, 0.5f)),
                        owner.tintCombatant(16, 0.5f, 0.0f, 0.75f, color));
        color = EncounterMoveAnimation.color(0);
        ReferenceMoveTimeline animationStep11 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep10, owner.tintCombatant(16, 0.5f, 0.75f, 0.0f, color));
        color = EncounterMoveAnimation.color(0);
        ReferenceMoveTimeline animationStep12 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep11, owner.tintCombatant(16, 0.5f, 0.0f, 0.75f, color));
        color = EncounterMoveAnimation.color(0);
        ReferenceMoveTimeline animationStep13 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep12, owner.tintCombatant(16, 0.5f, 0.75f, 0.0f, color));
        color = EncounterMoveAnimation.color(0);
        ReferenceMoveTimeline animationStep14 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep13, owner.tintCombatant(16, 0.5f, 0.0f, 0.75f, color));
        color = EncounterMoveAnimation.color(0);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep14, owner.tintCombatant(16, 0.5f, 0.75f, 0.0f, color))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
