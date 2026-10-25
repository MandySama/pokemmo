package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 防御指令（招式 ID：455）的攻击／事件阶段动画。
 *
 * <p>招式描述：召唤手下, 让其附在自己的身体上. 可以提高自己的防御和特防.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整精灵叠加颜色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110455（名称）和 120455（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/DefendOrderAnimation.java
 *
 * <p>源文件 SHA-256：d0f3209cbe8307017a6019e31090175ceb1fe96f0053c8ca1d009becff0d077a
 */
final class ReferenceMoveProgram455 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .onStart(owner.setHealthHudHidden(18, true))
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
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        1,
                                                        14,
                                                        1333.3334f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 1, 1040.0f, 240.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1678,
                                                        2,
                                                        14,
                                                        166.66667f,
                                                        0.546875f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(630))
                                        .append(owner.emitParticles(-1, 630, 0, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 630, 1, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 630, 2, 9, 8, 0.5f)),
                                owner.emitParticles(-1, 630, 3, 9, 8, 0.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1848, 2, 14, 0.0f, 0.9375f, owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 1, 240.0f, 640.0f));
        Color color = EncounterMoveAnimation.color(30);
        ReferenceMoveTimeline animationStep10 =
                animationStep9
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1848,
                                        1,
                                        14,
                                        83.333336f,
                                        0.46875f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 1, 320.0f, 640.0f))
                        .append(owner.tintCombatant(14, 0.75f, 0.0f, 0.75f, color));
        Color color2 = EncounterMoveAnimation.color(30);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep10, owner.emitParticles(-1, 630, 4, 9, 8, 0.5f))
                        .append(owner.tintCombatant(14, 0.75f, 0.75f, 0.0f, color2))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
