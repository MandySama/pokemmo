package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 叶刃（招式 ID：348）的攻击／事件阶段动画。
 *
 * <p>招式描述：像用剑一般操纵叶片切斩对手进行攻击. 容易击中要害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110348（名称）和 120348（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/LeafBladeAnimation.java
 *
 * <p>源文件 SHA-256：091a3db9cc13817dafab7d23674c3623ad5302a307517120a4a2fe52a53d06ad
 */
final class ReferenceMoveProgram348 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1423,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1423,
                                                                        1,
                                                                        16,
                                                                        366.66666f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1423,
                                                                        2,
                                                                        16,
                                                                        666.6667f,
                                                                        0.625f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1438,
                                                                        1,
                                                                        16,
                                                                        666.6667f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(521))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 521, 0, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 521, 1, 11, 8, 0.875f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 521, 2, 11, 8, 1.0f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 521, 3, 11, 8, 1.0f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 521, 4, 11, 8, 0.75f))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        16, 1)),
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.0f,
                                                        0.096f,
                                                        0.19995117f,
                                                        -0.19995117f),
                                                0.4f),
                                        owner.scaleCombatant(
                                                16, 2, 1, 0.0f, 0.096f, 0.19995117f, -0.19995117f),
                                        0.8f),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.0f, 0.096f, -0.19995117f, 0.19995117f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
