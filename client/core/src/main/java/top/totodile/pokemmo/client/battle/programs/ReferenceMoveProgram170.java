package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 心之眼（招式 ID：170）的攻击／事件阶段动画。
 *
 * <p>招式描述：用心感受对手的行动, 下次攻击必定会击中对手.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110170（名称）和 120170（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/MindReaderAnimation.java
 *
 * <p>源文件 SHA-256：a46f21b3886dfc397fbe30616365906bb5b3b54c01bb142fb5af74b936cfb770
 */
final class ReferenceMoveProgram170 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.4f)),
                                                owner.tintEnvironment(4, 0, 0.0f, 0.8125f, 0.025f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1473,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1505,
                                                        2,
                                                        16,
                                                        666.6667f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(337))
                                        .append(owner.emitParticles(-1, 337, 1, 11, 8, 0.5f)),
                                owner.emitParticles(-1, 337, 0, 11, 8, 0.5f),
                                0.04f)
                        .append(owner.emitParticles(-1, 337, 2, 11, 8, 0.5f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .beginSequence()
                        .waitSeconds(0.4f)
                        .beginParallel()
                        .append(owner.tintEnvironment(4, 0, 0.8125f, 0.0f, 0.025f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
