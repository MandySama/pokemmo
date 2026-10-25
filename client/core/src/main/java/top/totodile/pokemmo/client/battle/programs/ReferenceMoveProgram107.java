package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 变小（招式 ID：107）的攻击／事件阶段动画。
 *
 * <p>招式描述：蜷缩身体显得很小, 从而大幅提高自己的闪避率.
 *
 * <p>动画说明：镜头聚焦施术者；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110107（名称）和 120107（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/MinimizeAnimation.java
 *
 * <p>源文件 SHA-256：5a4be770cf36d96c2bcb8f5d064164dd7697cef456fc26f4d2135f9dc645d8fb
 */
final class ReferenceMoveProgram107 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        14, 1))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .onStart(owner.setHealthHudHidden(14, true))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1414,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1414,
                                                                        1,
                                                                        14,
                                                                        666.6667f,
                                                                        0.9375f,
                                                                        owner.casterHandle)),
                                                owner.scaleCombatant(
                                                        14, 2, 2, 0.016f, 0.128f, -0.5f, -0.5f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1483,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 800.0f))
                                        .append(owner.adjustSoundTrack(1, 1, 0.0f, 640.0f)),
                                owner.scaleCombatant(
                                        14, 2, 1, 0.016f, 0.256f, -0.89990234f, -0.89990234f),
                                0.64f)
                        .onStart(owner.setHealthHudHidden(14, false))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
