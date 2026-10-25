package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 保护色（招式 ID：293）的攻击／事件阶段动画。
 *
 * <p>招式描述：根据所在场所不同, 如水边、草丛和洞窟等, 可以改变自己的属性.
 *
 * <p>动画说明：镜头聚焦施术者；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110293（名称）和 120293（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/CamouflageAnimation.java
 *
 * <p>源文件 SHA-256：c5f43d9b0d906f041ac7ad2d2ba6c49f890d181eab8d96458a4513152c8af997
 */
final class ReferenceMoveProgram293 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep3 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1504,
                                                        1,
                                                        14,
                                                        166.66667f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 640.0f))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .onStart(owner.setHealthHudHidden(14, true)),
                                owner.tintCombatant(14, 0.5f, 0.0f, 0.75f, color))
                        .append(owner.shakeCombatant(14, 3, 3, 0.016f, 0.048f, 0.19995117f, 0.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1530,
                                        2,
                                        14,
                                        166.66667f,
                                        0.625f,
                                        owner.casterHandle));
        Color color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                        animationStep3.append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        2,
                                                        14,
                                                        1166.6666f,
                                                        0.0f,
                                                        owner.casterHandle)),
                                        owner.adjustSoundTrack(2, 1, 640.0f, 480.0f)),
                                owner.tintCombatant(14, 0.5f, 0.75f, 0.0f, color2))
                        .onStart(owner.setHealthHudHidden(14, false))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
