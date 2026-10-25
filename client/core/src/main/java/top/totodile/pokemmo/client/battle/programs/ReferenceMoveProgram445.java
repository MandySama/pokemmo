package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 诱惑（招式 ID：445）的攻击／事件阶段动画。
 *
 * <p>招式描述：♂诱惑♀或♀诱惑♂, 从而大幅降低对手的特攻.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110445（名称）和 120445（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/CaptivateAnimation.java
 *
 * <p>源文件 SHA-256：3770d7a11a05bfd66a862c1be73a64bd7d78215e85b061d3fd0b6930ca335310
 */
final class ReferenceMoveProgram445 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1786,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1786,
                                                        1,
                                                        14,
                                                        500.0f,
                                                        0.234375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1473,
                                                        2,
                                                        14,
                                                        0.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(620))
                                        .append(owner.emitParticles(-1, 620, 0, 9, 8, 0.5f)),
                                owner.emitParticles(-1, 620, 1, 9, 8, 0.5f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
