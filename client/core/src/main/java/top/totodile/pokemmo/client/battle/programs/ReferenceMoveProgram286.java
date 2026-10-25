package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 封印（招式 ID：286）的攻击／事件阶段动画。
 *
 * <p>招式描述：如果对手有和自己相同的招式, 那么只有对手无法使用该招式.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110286（名称）和 120286（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/ImprisonAnimation.java
 *
 * <p>源文件 SHA-256：0bf3fdc94866c6368e9711fc2d7c70d5d4eb72c904ec6e5d7bd86c0138aad7ea
 */
final class ReferenceMoveProgram286 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1448,
                                                        2,
                                                        16,
                                                        416.66666f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1478,
                                                        1,
                                                        16,
                                                        1000.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(453))
                                        .append(owner.emitParticles(-1, 453, 3, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 453, 2, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 453, 4, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 453, 0, 11, 8, 0.5f)),
                                owner.emitParticles(-1, 453, 1, 11, 8, 0.5f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
