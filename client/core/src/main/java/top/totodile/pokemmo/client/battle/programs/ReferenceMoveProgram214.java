package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 梦话（招式 ID：214）的攻击／事件阶段动画。
 *
 * <p>招式描述：从自己已学会的招式中任意使出1个. 只能在自己睡觉时使用.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110214（名称）和 120214（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/SleepTalkAnimation.java
 *
 * <p>源文件 SHA-256：1878ae5897315cafa8df61e91e55458578cce71636e77d1a1eb435475d7bd847
 */
final class ReferenceMoveProgram214 {

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
                                                        1478,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1462,
                                                        2,
                                                        14,
                                                        166.66667f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(380))
                                        .append(owner.emitParticles(-1, 380, 0, 9, 8, 0.5f)),
                                owner.emitParticles(-1, 380, 1, 9, 11, 0.5f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
