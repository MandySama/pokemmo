package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 绑紧（招式 ID：20）的攻击／事件阶段动画。
 *
 * <p>招式描述：使用长长的身体或藤蔓等, 在4-5回合内绑紧对手进行攻击.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110020（名称）和 120020（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/BindAnimation.java
 *
 * <p>源文件 SHA-256：32e6b2a6c1c458bb2125915ebb787edc7c875404e77c9c69a8d2f004b8061901
 */
final class ReferenceMoveProgram20 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scaleCombatant(
                                                        14,
                                                        2,
                                                        3,
                                                        0.016f,
                                                        0.064f,
                                                        0.30004883f,
                                                        -0.30004883f))
                                        .onStart(owner.preloadParticleEffect(182))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .append(owner.emitParticles(-1, 182, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 182, 1, 11, 8, 0.5f))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        3,
                                                        0.016f,
                                                        0.064f,
                                                        -0.39990234f,
                                                        0.5f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1441,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9765625f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        1441,
                                        2,
                                        16,
                                        250.0f,
                                        0.9765625f,
                                        owner.casterHandle))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
