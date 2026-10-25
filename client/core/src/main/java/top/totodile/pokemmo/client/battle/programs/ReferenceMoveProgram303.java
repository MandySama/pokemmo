package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 偷懒（招式 ID：303）的攻击／事件阶段动画。
 *
 * <p>招式描述：偷懒休息. 回复自己最大HP的一半.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110303（名称）和 120303（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/SlackOffAnimation.java
 *
 * <p>源文件 SHA-256：011e083e5de5e6cfa5d409ef53bf7945bb61a037e962a1de18355623dfb677a2
 */
final class ReferenceMoveProgram303 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1964,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1780,
                                                        2,
                                                        14,
                                                        0.0f,
                                                        0.546875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scaleCombatant(
                                                        14,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.096f,
                                                        0.30004883f,
                                                        -0.30004883f))
                                        .onStart(owner.preloadParticleEffect(469))
                                        .append(
                                                owner.tintCombatant(
                                                        14,
                                                        1.0f,
                                                        0.0f,
                                                        0.75f,
                                                        EncounterMoveAnimation.color(
                                                                Short.MAX_VALUE))),
                                owner.emitParticles(-1, 469, 0, 9, 8, 0.5f))
                        .append(
                                owner.tintCombatant(
                                        14,
                                        1.0f,
                                        0.75f,
                                        0.0f,
                                        EncounterMoveAnimation.color(Short.MAX_VALUE)))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
