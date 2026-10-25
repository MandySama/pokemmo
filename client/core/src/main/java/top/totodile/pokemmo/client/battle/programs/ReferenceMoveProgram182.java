package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 守住（招式 ID：182）的攻击／事件阶段动画。
 *
 * <p>招式描述：完全抵挡对手的攻击. 连续使出则容易失败.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110182（名称）和 120182（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/ProtectAnimation.java
 *
 * <p>源文件 SHA-256：7f40b9f8cacf71a64bad67c2c358449080b090590a98c5b56458e97b230469ea
 */
final class ReferenceMoveProgram182 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .onStart(owner.preloadParticleEffect(348))
                                        .append(owner.emitParticles(-1, 348, 0, 9, 8, 0.5f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1471,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        1358,
                                        2,
                                        14,
                                        333.33334f,
                                        0.625f,
                                        owner.casterHandle))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
