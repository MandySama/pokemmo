package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 回复指令（招式 ID：456）的攻击／事件阶段动画。
 *
 * <p>招式描述：召唤手下疗伤. 回复自己最大HP的一半.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整精灵叠加颜色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110456（名称）和 120456（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/HealOrderAnimation.java
 *
 * <p>源文件 SHA-256：b0dd2c7874317d4680eba8786c82655cabf627fbaa407e3fd646b7ce02d52ab0
 */
final class ReferenceMoveProgram456 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1875,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 1, 1440.0f, 320.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        1,
                                                        14,
                                                        1833.3334f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1678,
                                                        2,
                                                        14,
                                                        166.66667f,
                                                        0.625f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(631))
                                        .append(owner.emitParticles(-1, 631, 0, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 631, 1, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 631, 2, 9, 8, 0.5f)),
                                owner.emitParticles(-1, 631, 3, 9, 8, 0.5f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1358, 2, 14, 0.0f, 0.9375f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1358,
                                        1,
                                        14,
                                        333.33334f,
                                        0.703125f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(21460);
        ReferenceMoveTimeline animationStep11 =
                animationStep10
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1358,
                                        2,
                                        14,
                                        666.6667f,
                                        0.46875f,
                                        owner.casterHandle))
                        .append(owner.tintCombatant(14, 0.75f, 0.0f, 0.75f, color));
        Color color2 = EncounterMoveAnimation.color(21460);
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendThenNextParallel(
                                        animationStep11,
                                        owner.emitParticles(-1, 631, 4, 9, 8, 0.5f))
                                .append(owner.restoreCamera(0.4f)),
                        owner.tintCombatant(14, 0.75f, 0.75f, 0.0f, color2));
        return result;
    }
}
