package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 铁尾（招式 ID：231）的攻击／事件阶段动画。
 *
 * <p>招式描述：使用坚硬的尾巴摔打对手进行攻击. 有时会降低对手的防御.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110231（名称）和 120231（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/IronTailAnimation.java
 *
 * <p>源文件 SHA-256：f7013a9e4904b5907f779850a0de1adb3c9a8a6eba43ab27c30dc40a0ab628ef
 */
final class ReferenceMoveProgram231 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1449,
                                                                        1,
                                                                        14,
                                                                        116.666664f,
                                                                        0.546875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1701,
                                                                        2,
                                                                        14,
                                                                        0.0f,
                                                                        0.3125f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(397)),
                                                owner.emitParticles(-1, 397, 2, 9, 8, 0.5f))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1442,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        1,
                                                        16,
                                                        166.66667f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1436,
                                                        0,
                                                        16,
                                                        116.666664f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        2,
                                                        0.016f,
                                                        0.032f,
                                                        0.19995117f,
                                                        -0.19995117f))
                                        .append(owner.emitParticles(-1, 397, 3, 11, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 397, 1, 11, 8, 0.25f)),
                                owner.emitParticles(-1, 397, 0, 11, 8, 0.125f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
