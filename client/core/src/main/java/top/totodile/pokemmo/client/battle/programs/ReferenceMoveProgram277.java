package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 魔法反射（招式 ID：277）的攻击／事件阶段动画。
 *
 * <p>招式描述：当对手使出会变成异常状态的招式或寄生种子等时, 会将对手的招式反射回去.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110277（名称）和 120277（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/MagicCoatAnimation.java
 *
 * <p>源文件 SHA-256：d1ff65146abaef50f379c8c759af0ebf1b9fe2d837a6eda454e3170969ece232
 */
final class ReferenceMoveProgram277 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1472,
                                                                        1,
                                                                        14,
                                                                        333.33334f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1433,
                                                                        2,
                                                                        14,
                                                                        1166.6666f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1473,
                                                                        1,
                                                                        14,
                                                                        1166.6666f,
                                                                        0.546875f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(443))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 443, 2, 9, 8, 0.375f)),
                                                owner.emitParticles(-1, 443, 0, 9, 8, 0.5f),
                                                0.32f),
                                        owner.emitParticles(-1, 443, 1, 9, 8, 0.5f),
                                        1.12f),
                                owner.emitParticles(-1, 443, 3, 9, 8, 0.5f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
