package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 挡路（招式 ID：335）的攻击／事件阶段动画。
 *
 * <p>招式描述：张开双手进行阻挡, 封住对手的退路, 使其不能逃走.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110335（名称）和 120335（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/BlockAnimation.java
 *
 * <p>源文件 SHA-256：f09f754171e8f884ee3c5d52e3bc3dd7c4d938f096e91df003f8b31e7f9259da
 */
final class ReferenceMoveProgram335 {

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
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1421,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1424,
                                                        2,
                                                        16,
                                                        283.33334f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(505))
                                        .append(owner.emitParticles(-1, 505, 1, 11, 8, 0.5f)),
                                owner.emitParticles(-1, 505, 0, 11, 8, 0.5f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
