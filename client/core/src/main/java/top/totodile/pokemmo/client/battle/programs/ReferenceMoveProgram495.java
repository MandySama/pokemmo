package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 您先请（招式 ID：495）的攻击／事件阶段动画。
 *
 * <p>招式描述：支援我方或对手的行动, 使其紧接着此招式之后行动.
 *
 * <p>动画说明：镜头聚焦施术者；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110495（名称）和 120495（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/AfterYouAnimation.java
 *
 * <p>源文件 SHA-256：3a3598c009f991dbb87977cfd7913ffcf74d985151150183d40cb9e7018c7c56
 */
final class ReferenceMoveProgram495 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenDelayedParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .sequence()
                                                                                        .beginParallel()
                                                                                        .append(
                                                                                                owner
                                                                                                        .focusCamera(
                                                                                                                0,
                                                                                                                0.4f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .setSpriteAnimationPaused(
                                                                                                                14,
                                                                                                                1))
                                                                                        .endGroup()
                                                                                        .beginParallel()
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                1731,
                                                                                                                1,
                                                                                                                14,
                                                                                                                0.0f,
                                                                                                                0.9921875f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                1731,
                                                                                                                1,
                                                                                                                14,
                                                                                                                1000.0f,
                                                                                                                0.9921875f,
                                                                                                                owner.casterHandle)),
                                                                                owner
                                                                                        .scaleCombatant(
                                                                                                14,
                                                                                                2,
                                                                                                1,
                                                                                                0.0f,
                                                                                                0.064f,
                                                                                                0.30004883f,
                                                                                                -0.30004883f),
                                                                                0.08f)
                                                                        .onStart(
                                                                                owner
                                                                                        .setHealthHudHidden(
                                                                                                14,
                                                                                                true)),
                                                                owner.shakeCombatant(
                                                                        14, 2, 1, 0.0f, 0.256f,
                                                                        0.0f, 3.0f),
                                                                0.68f)
                                                        .append(
                                                                owner.scaleCombatant(
                                                                        14,
                                                                        2,
                                                                        1,
                                                                        0.0f,
                                                                        0.064f,
                                                                        0.30004883f,
                                                                        -0.30004883f))
                                                        .onStart(
                                                                owner.setHealthHudHidden(14, false))
                                                        .endGroup()
                                                        .endGroup()
                                                        .endGroup()
                                                        .beginParallel()
                                                        .beginSequence()
                                                        .waitSeconds(0.12f)
                                                        .beginParallel(),
                                                owner.scaleCombatant(
                                                        14,
                                                        2,
                                                        1,
                                                        0.0f,
                                                        0.064f,
                                                        0.30004883f,
                                                        -0.30004883f),
                                                0.2f)
                                        .onStart(owner.setHealthHudHidden(14, true)),
                                owner.shakeCombatant(14, 2, 1, 0.0f, 0.32f, 0.0f, 4.0f),
                                1.0f)
                        .onStart(owner.setHealthHudHidden(14, false))
                        .append(
                                owner.scaleCombatant(
                                        14, 2, 1, 0.0f, 0.064f, 0.30004883f, -0.30004883f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
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
