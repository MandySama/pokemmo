package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 折弯汤匙（招式 ID：134）的攻击／事件阶段动画。
 *
 * <p>招式描述：折弯汤匙引开注意, 从而降低对手的命中率.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110134（名称）和 120134（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/KinesisAnimation.java
 *
 * <p>源文件 SHA-256：cee761e6cf99ae956f3044af9a87f07d83b1749614e27b93360bc8e0c53d91a9
 */
final class ReferenceMoveProgram134 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1478,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1461,
                                                        2,
                                                        14,
                                                        1166.6666f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1452,
                                                        1,
                                                        14,
                                                        1500.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(299))
                                        .append(owner.emitParticles(-1, 299, 0, 9, 8, 0.5f)),
                                owner.emitParticles(-1, 299, 1, 9, 8, 0.5f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
