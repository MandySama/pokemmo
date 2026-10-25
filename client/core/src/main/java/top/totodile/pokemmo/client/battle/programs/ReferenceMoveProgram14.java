package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 剑舞（招式 ID：14）的攻击／事件阶段动画。
 *
 * <p>招式描述：激烈地跳起战舞提高气势. 大幅提高自己的攻击.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110014（名称）和 120014（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SwordsDanceAnimation.java
 *
 * <p>源文件 SHA-256：c57bb864f7cbd964dbd5640ad6e70881809ef4a1b309f655a4b7d010a7f4423e
 */
final class ReferenceMoveProgram14 {

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
                                                        1434,
                                                        2,
                                                        14,
                                                        0.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1435,
                                                        1,
                                                        14,
                                                        200.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1436,
                                                        2,
                                                        14,
                                                        383.33334f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1436,
                                                        2,
                                                        14,
                                                        916.6667f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(175))
                                        .append(owner.emitParticles(-1, 175, 0, 9, 8, 0.5f)),
                                owner.emitParticles(-1, 175, 1, 9, 8, 0.5f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
